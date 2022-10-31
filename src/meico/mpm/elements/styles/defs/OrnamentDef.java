package meico.mpm.elements.styles.defs;

import meico.mei.Helper;
import meico.mpm.Mpm;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;

/**
 * This class interfaces MPM's ornamentDef elements.
 * @author Axel Berndt
 */
public class OrnamentDef extends AbstractDef {
    private TemporalSpread temporalSpread = null;
    private DynamicsGradient dynamicsGradient = null;

    /**
     * constructor, creates an empty/initial OrnamentDef
     * @param name
     * @throws Exception
     */
    private OrnamentDef(String name) throws Exception {
        Element e = new Element("ornamentDef", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("name", name));
        this.parseData(e);
    }

    /**
     * contructor to create a OrnamentDef instance from xml
     * @param xml
     * @throws Exception
     */
    private OrnamentDef(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * OrnamentDef factory
     * @param name
     * @return
     */
    public static OrnamentDef createOrnamentDef(String name) {
        OrnamentDef ornamentDef;
        try {
            ornamentDef = new OrnamentDef(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return ornamentDef;
    }

    /**
     * OrnamentDef factory
     * @param xml
     * @return
     */
    public static OrnamentDef createOrnamentDef(Element xml) {
        OrnamentDef ornamentDef;
        try {
            ornamentDef = new OrnamentDef(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return ornamentDef;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws Exception {
        super.parseData(xml);

        // make sure that this element is really a "ornamentDef" element
        if (!this.getXml().getLocalName().equals("ornamentDef")) {
            this.getXml().setLocalName("ornamentDef");
        }

        // parse the transformations that define the ornament
        for (Element transformer : this.getXml().getChildElements()) {
            // parse the transformers and skip invalid elements
            switch (transformer.getLocalName()) {
                case "dynamicsGradient": {
                    this.dynamicsGradient = new DynamicsGradient(transformer);
                    break;
                }
                case "temporalSpread": {
                    this.temporalSpread = new TemporalSpread(transformer);
                    break;
                }
            }
        }
    }

    /**
     * access the temporalSpread transformer
     * @return
     */
    public TemporalSpread getTemporalSpread() {
        return temporalSpread;
    }

    /**
     * set or remove the temporalSpread transformer
     * @param temporalSpread temporal spread data or null to just remove the transformer from the ornament definition
     */
    public void setTemporalSpread(TemporalSpread temporalSpread) {
        this.temporalSpread = temporalSpread;

        // remove old temporalSpread element; there can be only one in an ornamentDef
        for (Element old = Helper.getFirstChildElement("temporalSpread", this.getXml()); old != null; old = Helper.getFirstChildElement("temporalSpread", this.getXml())) {
            this.getXml().removeChild(old);
//            old.detach();
        }

        if (temporalSpread != null)
            this.getXml().appendChild(temporalSpread.generateXML());    // create xml data and add it to the XML tree structure
    }

    /**
     * set the temporalSpread transformer
     * @param frameStart
     * @param frameLength must be greater or equal to 0.0
     * @param frameDomain
     * @param intensity
     * @param noteOffShift
     */
    public void setTemporalSpread(double frameStart, double frameLength, TemporalSpread.FrameDomain frameDomain, double intensity, TemporalSpread.NoteOffShift noteOffShift) {
        TemporalSpread temporalSpread = new TemporalSpread();
        temporalSpread.frameStart = frameStart;
        temporalSpread.setFrameLength(frameLength);
        temporalSpread.frameDomain = frameDomain;
        temporalSpread.intensity = intensity;
        temporalSpread.noteOffShift = noteOffShift;
        this.setTemporalSpread(temporalSpread);
    }

    /**
     * access the dynamicsGradient transformer
     * @return
     */
    public DynamicsGradient getDynamicsGradient() {
        return dynamicsGradient;
    }

    /**
     * set the dynamicsGradient transformer
     * @param dynamicsGradient dynamics gradient data or null
     */
    public void setDynamicsGradient(DynamicsGradient dynamicsGradient) {
        this.dynamicsGradient = dynamicsGradient;

        // remove old dynamicsGradient element; there can be only one in an ornamentDef
        for (Element old = Helper.getFirstChildElement("dynamicsGradient", this.getXml()); old != null; old = Helper.getFirstChildElement("dynamicsGradient", this.getXml())) {
            this.getXml().removeChild(old);
//            old.detach();
        }

        if (dynamicsGradient != null)
            this.getXml().appendChild(dynamicsGradient.generateXML());  // create xml data and add it to the XML tree structure
    }

    /**
     * set the dynamicsGradient transformer
     * @param transitionFrom
     * @param transitionTo
     */
    public void setDynamicsGradient(double transitionFrom, double transitionTo) {
        DynamicsGradient dynamicsGradient = new DynamicsGradient();
        dynamicsGradient.transitionFrom = transitionFrom;
        dynamicsGradient.transitionTo = transitionTo;
        this.setDynamicsGradient(dynamicsGradient);
    }

    /**
     * generate a default ornament definition for the given name string
     * @param name
     * @return
     */
    public static OrnamentDef createDefaultOrnamentDef(String name) {
        OrnamentDef def = OrnamentDef.createOrnamentDef(name);
        if (def == null)
            return null;

        switch (name.trim().toLowerCase()) {
            case "arpeg":
            case "arpeggio":
                def.setDynamicsGradient(-1.0, 1.0);
                def.setTemporalSpread(-22.0, 44.0, TemporalSpread.FrameDomain.Ticks, 1.0, TemporalSpread.NoteOffShift.False);
        }

        return def;
    }

    /**
     * This class represents the temporalSpread transformer of ornamentDef
     * @author Axel Berndt
     */
    public static class TemporalSpread {
        public double frameStart = 0.0;
        private double frameLength = 0.0;    // must be >= 0.0
        public FrameDomain frameDomain = FrameDomain.Ticks;
        public double intensity = 1.0;
        public NoteOffShift noteOffShift = NoteOffShift.False;
        private String id = null;
        private Element xml;

        public enum FrameDomain {
            Ticks,
            Milliseconds
//            RelativeToNoteDuration
        }

        public enum NoteOffShift {
            False,
            True,
            Monophonic
        }

        /**
         * constructor
         */
        public TemporalSpread() {}

        /**
         * constructor
         * @param xml
         */
        public TemporalSpread(Element xml) {
            this.xml = xml;

            Attribute domain = Helper.getAttribute("time.unit", xml);
            if (domain == null)
                this.frameDomain = TemporalSpread.FrameDomain.Ticks;
            else {
                switch (domain.getValue()) {
                    case "milliseconds":
                        this.frameDomain = TemporalSpread.FrameDomain.Milliseconds;
                        break;
                    // TODO: TemporalSpread.FrameDomain.RelativeToNoteDuration?
                    case "ticks":
                    default:
//                                this.temporalSpread.frameDomain = TemporalSpread.FrameDomain.Ticks;   // unnecessary because default
                        break;
                }
            }

            Attribute start = Helper.getAttribute("frame.start", xml);
            if (start != null)
                this.frameStart = Double.parseDouble(start.getValue());

            Attribute length = Helper.getAttribute("frameLength", xml);
            if (length != null)
                this.setFrameLength(Double.parseDouble(length.getValue()));

            Attribute intensityAtt = Helper.getAttribute("intensity", xml);
            if (intensityAtt != null)
                this.intensity = Double.parseDouble(intensityAtt.getValue());

            Attribute noteoffShiftAtt = Helper.getAttribute("noteoff.shift", xml);
            if (noteoffShiftAtt != null) {
                switch (noteoffShiftAtt.getValue()) {
                    case "true":
                        this.noteOffShift = TemporalSpread.NoteOffShift.True;
                        break;
                    case "false":
                        this.noteOffShift = TemporalSpread.NoteOffShift.False;
                        break;
                    case "monophonic":
                        this.noteOffShift = TemporalSpread.NoteOffShift.Monophonic;
                        break;
                }
            }

            Attribute idAtt = Helper.getAttribute("id", xml);
            if (idAtt != null)
                this.id = idAtt.getValue();
        }

        /**
         * set the length of the frame
         * @param length must be positive, otherwise it defaults to 0.0
         */
        public void setFrameLength(double length) {
            this.frameLength = Math.max(0.0, length);
        }

        /**
         * get the frame length
         * @return
         */
        public double getFrameLength() {
            return this.frameLength;
        }

        /**
         * apply the temporal spread to the chord/note sequence;
         * the notes get new attributes ornament.date.offset or ornament.date.offset.milliseconds,
         * and ornament.duration.offset or ornament.duration.milliseconds
         * @param chordSequence the sequence of the chords/notes in which the temporal spread is applied
         */
        public void apply(ArrayList<ArrayList<Element>> chordSequence) {
            if (chordSequence.size() < 1)   // if there is no chord/note or just one
                return;                     // we don't do anything

            // process all chords/notes except for the final one
            ArrayList<Element> previous = null;
            if (chordSequence.size() > 1) {
                for (int i = 0; i < chordSequence.size() - 1; ++i) {    // for each chord/note until the pre-last
                    double dateOffset = (Math.pow(((double) i) / (chordSequence.size() - 1), this.intensity) * this.frameLength) + this.frameStart;
                    previous = this.setOrnamentDateAtts(dateOffset, chordSequence.get(i), previous);
                }
            }

            // place the final chord at frameEnd
            ArrayList<Element> finalchord = chordSequence.get(chordSequence.size() - 1);
            this.setOrnamentDateAtts(this.frameStart + this.frameLength, finalchord, previous);
        }

        /**
         * helper method for method apply() to set the ornament attributes on each note:
         *      - ornament.date.offset or ornament.milliseconds.date.offset (an offset),
         *      - ornament.duration or ornament.milliseconds.duration (absolute duration),
         *      - ornament.noteoff.shift (true/false)
         * @param dateOffset the offset to the date/milliseconds.date of the chord/notes
         * @param chord
         * @param previous the previous chord, so we can treat its duration according to the chords offset, or null
         * @return the chord, if its duration needs treatment along the processing of the next chord (then as previous); otherwise null
         */
        private ArrayList<Element> setOrnamentDateAtts(double dateOffset, ArrayList<Element> chord, ArrayList<Element> previous) {
            String dateAttName, durAttName;
            switch (this.frameDomain) {
                case Ticks:
                    dateAttName = "ornament.date.offset";
                    durAttName = "ornament.duration";
                    break;
                case Milliseconds:
                    dateAttName = "ornament.milliseconds.date.offset";
                    durAttName = "ornament.milliseconds.duration";
                    break;
                default:    // unknown domain
                    return null;
            }

            // set the ornament.date.offset or ornament.milliseconds.date.offset, resp.
            for (Element note : chord) {
                Attribute ornamentDateAtt = Helper.getAttribute(dateAttName, note);
                if (ornamentDateAtt != null) {
                    ornamentDateAtt.setValue(String.valueOf(dateOffset + Double.parseDouble(ornamentDateAtt.getValue())));
                } else
                    note.addAttribute(new Attribute(dateAttName, String.valueOf(dateOffset)));
            }

            // handle the ornament.duration or ornament.milliseconds.duration, resp.
            switch (this.noteOffShift) {
                case False:
                    return null;
                case True:
                    for (Element note : chord)
                        note.addAttribute(new Attribute("ornament.noteoff.shift", "true"));
                    return null;
                case Monophonic:
                    if (previous != null) {
                        for (Element prev : previous) {
                            Attribute prevDateOffsetAtt = Helper.getAttribute(dateAttName, prev);
                            if (prevDateOffsetAtt == null)
                                continue;
                            Attribute ornamentDurationAtt = Helper.getAttribute(durAttName, prev);
                            if (ornamentDurationAtt != null)
                                ornamentDurationAtt.setValue(String.valueOf(dateOffset - Double.parseDouble(prevDateOffsetAtt.getValue())));
                            else
                                prev.addAttribute(new Attribute(durAttName, String.valueOf(dateOffset - Double.parseDouble(prevDateOffsetAtt.getValue()))));
                        }
                    }
                    return chord;
                default:
                    return null;
            }
        }

        /**
         * a setter for the XML representation
         * @param xml
         */
        public void setXml(Element xml) {
            this.xml = xml;
        }

        /**
         * a getter for the XML representation
         * @return
         */
        public Element getXml() {
            if (this.xml == null)
                return this.generateXML();
            return this.xml;
        }

        /**
         * newly generate the XML code for this temporal spread and overwrite the XML data stored in this.xml so far
         * @return
         */
        public Element generateXML() {
            Element ts = new Element("temporalSpread", Mpm.MPM_NAMESPACE);

            if (this.frameStart != 0.0)
                ts.addAttribute(new Attribute("frame.start", Double.toString(this.frameStart)));
            if (this.frameLength != 0.0)
                ts.addAttribute(new Attribute("frameLength", Double.toString(this.frameLength)));

            switch (this.frameDomain) {
                case Ticks:
                    // not necessary because this is the default value when absent
                    break;
                case Milliseconds:
                    ts.addAttribute(new Attribute("time.unit", "milliseconds"));
                    break;
//            case RelativeToNoteDuration:
//                throw new UnsupportedDataTypeException("The feature TemporalSpread.FrameDomain.RelativeToNoteDuration is not yet supported.");
            }

            if (this.intensity != 1.0)
                ts.addAttribute(new Attribute("intensity", Double.toString(this.intensity)));

            switch (this.noteOffShift) {
                case False:
//                ts.addAttribute(new Attribute("noteoff.shift", "false"));     // not necessary because this is the default value in the absence of the attribute
                    break;
                case True:
                    ts.addAttribute(new Attribute("noteoff.shift", "true"));
                    break;
                case Monophonic:
                    ts.addAttribute(new Attribute("noteoff.shift", "monophonic"));
                    break;
            }

            if ((this.id != null) && !this.id.isEmpty()) {
                Attribute idAtt = new Attribute("id", this.id);
                idAtt.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");
                ts.addAttribute(idAtt);
            }

            this.setXml(ts);
            return this.xml;
        }

        /**
         * get the XML string
         * @return
         */
        public String toXml() {
            if (this.xml == null)
                return "";
            return this.xml.toXML();
        }

        /**
         * set the id
         * @param id a xml:id string or null
         */
        public void setId(String id) {
            Attribute idAtt = Helper.getAttribute("id", this.getXml());
            if (id == null) {
                if (idAtt != null) {
                    idAtt.detach();
                    this.id = null;
                }
                return;
            }

            if (idAtt == null) {
                this.id = id;
                idAtt = new Attribute("id", id);
                idAtt.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");    // set correct namespace
                this.getXml().addAttribute(idAtt);
                return;
            }

            this.id = id;
            idAtt.setValue(id);
        }

        /**
         * get the id
         * @return a string or null
         */
        public String getId() {
            return this.id;
        }
    }

    /**
     * This class represents the dynamicsGradient transformer of ornamentDef
     * @author Axel Berndt
     */
    public static class DynamicsGradient {
        public double transitionFrom = 0.0;
        public double transitionTo = 0.0;
        private String id = null;
        private Element xml = null;

        /**
         * constructor
         */
        public DynamicsGradient() {}

        /**
         * constructor
         * @param xml
         */
        public DynamicsGradient(Element xml) {
            this.xml = xml;

            Attribute att = Helper.getAttribute("transition.from", xml);
            if (att != null)                                                // if there is a transition.from value
                this.transitionFrom = Double.parseDouble(att.getValue());   // parse it; otherwise we would leave the default value

            att = Helper.getAttribute("transition.to", xml);
            if (att == null)                                                // if there is no transition.to value
                this.transitionTo = this.transitionFrom;                    // we assume constant dynamics, hence set transition.to = transition.from
            else                                                            // if, instead, we have a transition.to value
                this.transitionTo = Double.parseDouble(att.getValue());     // parse it

            Attribute idAtt = Helper.getAttribute("id", xml);
            if (idAtt != null)
                this.id = idAtt.getValue();
        }

        /**
         * apply the dynamics gradient and scale to the given chord/note sequence;
         * the notes get a new attribute ornament.dynamics, or, if it is already present, it will be edited accordingly
         * @param chordSequence the sequence of the chords/notes in which the dynamics gradient is applied
         * @param scale
         */
        public void apply(ArrayList<ArrayList<Element>> chordSequence, double scale) {
            if (chordSequence.size() > 1) {                                     // if we have more than one note in the chordSequence
                double constFac = (scale * (this.transitionTo - this.transitionFrom)) / (chordSequence.size() - 1);
                double fromVelocity = this.transitionFrom * scale;
                for (int n = 0; n < chordSequence.size(); ++n) {                // for each chord in the list
                    double ornamentDynamics = (constFac * n) + fromVelocity;    // compute its velocity (the value is relative to the basic dynamics)
                    this.setOrnamentDynamicsAtt(ornamentDynamics, chordSequence.get(n));
                }
            } else if (chordSequence.size() > 0) {                              // if there is only one chord/note in the chordSequence
                double ornamentDynamics = this.transitionTo * scale;
                this.setOrnamentDynamicsAtt(ornamentDynamics, chordSequence.get(0));
            }
        }

        /**
         * helper method for method apply() to set the ornament.dynamics attribute on each note in the given chord
         * @param ornamentDynamics
         * @param chord
         */
        private void setOrnamentDynamicsAtt(double ornamentDynamics, ArrayList<Element> chord) {
            for (Element note : chord) {
                Attribute ornamentDynamicsAtt = Helper.getAttribute("ornament.dynamics", note);
                if (ornamentDynamicsAtt != null) {                                          // if there is already an ornament.dynamics attribute
                    ornamentDynamics += Double.parseDouble(ornamentDynamicsAtt.getValue()); // add the values
                    ornamentDynamicsAtt.setValue(String.valueOf(ornamentDynamics));
                } else {
                    note.addAttribute(new Attribute("ornament.dynamics", String.valueOf(ornamentDynamics)));
                }
            }
        }

        /**
         * a setter for the XML representation
         * @param xml
         */
        public void setXml(Element xml) {
            this.xml = xml;
        }

        /**
         * a getter for the XML representation
         * @return
         */
        public Element getXml() {
            if (this.xml == null)
                return this.generateXML();
            return this.xml;
        }

        /**
         * newly generate the XML code for this temporal spread and overwrite the XML data stored in this.xml so far
         * @return
         */
        public Element generateXML() {
            Element dg = new Element("dynamicsGradient", Mpm.MPM_NAMESPACE);

            if (this.transitionFrom != 0.0)
                dg.addAttribute(new Attribute("transition.from", Double.toString(this.transitionFrom)));

            if (this.transitionTo != this.transitionFrom)
                dg.addAttribute(new Attribute("transition.to", Double.toString(this.transitionTo)));

            if ((this.id != null) && !this.id.isEmpty()) {
                Attribute idAtt = new Attribute("id", this.id);
                idAtt.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");
                dg.addAttribute(idAtt);
            }

            this.setXml(dg);
            return this.xml;
        }

        /**
         * get the XML string
         * @return
         */
        public String toXml() {
            if (this.xml == null)
                return "";
            return this.xml.toXML();
        }

        /**
         * set the id
         * @param id a xml:id string or null
         */
        public void setId(String id) {
            Attribute idAtt = Helper.getAttribute("id", this.getXml());
            if (id == null) {
                if (idAtt != null) {
                    idAtt.detach();
                    this.id = null;
                }
                return;
            }

            if (idAtt == null) {
                this.id = id;
                idAtt = new Attribute("id", id);
                idAtt.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");    // set correct namespace
                this.getXml().addAttribute(idAtt);
                return;
            }

            this.id = id;
            idAtt.setValue(id);
        }

        /**
         * get the id
         * @return a string or null
         */
        public String getId() {
            return this.id;
        }
    }
}
