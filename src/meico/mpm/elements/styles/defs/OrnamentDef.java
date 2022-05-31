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
                    this.dynamicsGradient = new DynamicsGradient();

                    this.dynamicsGradient.setXml(transformer);

                    Attribute att = Helper.getAttribute("transition.from", transformer);
                    if (att != null)                                                                // if there is a transition.from value
                        this.dynamicsGradient.transitionFrom = Double.parseDouble(att.getValue());  // parse it; otherwise we would leave the default value

                    att = Helper.getAttribute("transition.to", transformer);
                    if (att == null)                                                                // if there is no transition.to value
                        this.dynamicsGradient.transitionTo = this.dynamicsGradient.transitionFrom;  // we assume constant dynamics, hence set transition.to = transition.from
                    else                                                                            // if, instead, we have a transition.to value
                        this.dynamicsGradient.transitionTo = Double.parseDouble(att.getValue());    // parse it

                    break;
                }
                case "temporalSpread": {
                    this.temporalSpread = new TemporalSpread();

                    this.temporalSpread.setXml(transformer);

                    Attribute att1 = Helper.getAttribute("milliseconds.frame.start", transformer);
                    Attribute att2 = Helper.getAttribute("milliseconds.frame.end", transformer);
                    if ((att1 != null) || (att2 != null)) {                                         // if we have frame values in milliseconds, these will dominate over other domains, i.e. non-milliseconds frame attributes are ignored
                        this.temporalSpread.frameDomain = TemporalSpread.FrameDomain.Milliseconds;
                        if (att1 != null)
                            this.temporalSpread.frameStart = Double.parseDouble(att1.getValue());
                        if (att2 != null)
                            this.temporalSpread.frameEnd = Double.parseDouble(att2.getValue());
                    } else {                                                                        // if, instead, we have frame values in ticks, we parse these
                        att1 = Helper.getAttribute("frame.start", transformer);
                        att2 = Helper.getAttribute("frame.end", transformer);
                        if ((att1 != null) || (att2 != null)) {
                            this.temporalSpread.frameDomain = TemporalSpread.FrameDomain.Ticks;
                            if (att1 != null)
                                this.temporalSpread.frameStart = Double.parseDouble(att1.getValue());
                            if (att2 != null)
                                this.temporalSpread.frameEnd = Double.parseDouble(att2.getValue());
                        }
                        // TODO: what if TemporalSpread.FrameDomain.RelativeToNoteDuration?
                    }
                    if (this.temporalSpread.frameStart > this.temporalSpread.frameEnd) {            // if the frame values clash, exchange them
                        double temp = this.temporalSpread.frameStart;
                        this.temporalSpread.frameStart = this.temporalSpread.frameEnd;
                        this.temporalSpread.frameEnd = temp;
//                        switch (this.temporalSpread.frameDomain) {                                  // update the xml
//                            case Milliseconds:
//                                ...
//                                break;
//                            case Ticks:
//                                ...
//                                break;
//                            case RelativeToNoteDuration:
//                                ...
//                                break;
//                        }
                    }

                    att1 = Helper.getAttribute("intensity", transformer);
                    if (att1 != null)
                        this.temporalSpread.intensity = Double.parseDouble(att1.getValue());

                    att1 = Helper.getAttribute("noteoff.shift", transformer);
                    if (att1 != null) {
                        switch (att1.getValue()) {
                            case "true":
                                this.temporalSpread.noteOffShift = TemporalSpread.NoteOffShift.True;
                                break;
                            case "false":
                                this.temporalSpread.noteOffShift = TemporalSpread.NoteOffShift.False;
                                break;
                            case "monophonic":
                                this.temporalSpread.noteOffShift = TemporalSpread.NoteOffShift.Monophonic;
                                break;
                        }
                    }
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
     * set the temporalSpread transformer
     * @param temporalSpread temporal spread data or null
     */
    public void setTemporalSpread(TemporalSpread temporalSpread) {
        this.temporalSpread = temporalSpread;

        // remove old temporalSpread element; there can be only one in an ornamentDef
        for (Element old = Helper.getFirstChildElement("temporalSpread", this.getXml()); old != null; old = Helper.getFirstChildElement("temporalSpread", this.getXml())) {
            this.getXml().removeChild(old);
//            old.detach();
        }

        if (temporalSpread == null)
            return;

        // make sure that the frame values do not clash
        if (temporalSpread.frameStart > temporalSpread.frameEnd) {
            System.err.println("Frame value clash: frameStart is not allowed to be greater than frameEnd! Swapping their values to ensure validity.");
            double temp = temporalSpread.frameStart;
            temporalSpread.frameStart = temporalSpread.frameEnd;
            temporalSpread.frameEnd = temp;
        }

        // create xml data
        Element ts = new Element("temporalSpread", Mpm.MPM_NAMESPACE);
        switch (temporalSpread.frameDomain) {
            case Ticks:
                if (temporalSpread.frameStart != 0.0)
                    ts.addAttribute(new Attribute("frame.start", Double.toString(temporalSpread.frameStart)));
                if (temporalSpread.frameEnd != 0.0)
                    ts.addAttribute(new Attribute("frame.end", Double.toString(temporalSpread.frameEnd)));
                break;
            case Milliseconds:
                if (temporalSpread.frameStart != 0.0)
                    ts.addAttribute(new Attribute("milliseconds.frame.start", Double.toString(temporalSpread.frameStart)));
                if (temporalSpread.frameEnd != 0.0)
                    ts.addAttribute(new Attribute("milliseconds.frame.end", Double.toString(temporalSpread.frameEnd)));
                break;
//            case RelativeToNoteDuration:
//                throw new UnsupportedDataTypeException("The feature TemporalSpread.FrameDomain.RelativeToNoteDuration is not yet supported.");
        }

        if (temporalSpread.intensity != 1.0)
            ts.addAttribute(new Attribute("intensity", Double.toString(temporalSpread.intensity)));

        switch (temporalSpread.noteOffShift) {
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

        this.temporalSpread.setXml(ts);

        this.getXml().appendChild(ts);
    }

    /**
     * set the temporalSpread transformer
     * @param frameStart
     * @param frameEnd
     * @param frameDomain
     * @param intensity
     * @param noteOffShift
     */
    public void setTemporalSpread(double frameStart, double frameEnd, TemporalSpread.FrameDomain frameDomain, double intensity, TemporalSpread.NoteOffShift noteOffShift) {
        TemporalSpread temporalSpread = new TemporalSpread();
        temporalSpread.frameStart = frameStart;
        temporalSpread.frameEnd = frameEnd;
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

        if (dynamicsGradient == null)
            return;

        // create xml data
        Element dg = new Element("dynamicsGradient", Mpm.MPM_NAMESPACE);

        if (dynamicsGradient.transitionFrom != 0.0)
            dg.addAttribute(new Attribute("transition.from", Double.toString(dynamicsGradient.transitionFrom)));

        if (dynamicsGradient.transitionTo != dynamicsGradient.transitionFrom)
            dg.addAttribute(new Attribute("transition.to", Double.toString(dynamicsGradient.transitionTo)));

        this.dynamicsGradient.setXml(dg);

        this.getXml().appendChild(dg);
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
                def.setTemporalSpread(-22.0, 22.0, TemporalSpread.FrameDomain.Ticks, 1.0, TemporalSpread.NoteOffShift.False);
        }

        return def;
    }

    /**
     * This class represents the temporalSpread transformer of ornamentDef
     * @author Axel Berndt
     */
    public static class TemporalSpread {
        public double frameStart = 0.0;
        public double frameEnd = 0.0;
        public FrameDomain frameDomain = FrameDomain.Ticks;
        public double intensity = 1.0;
        public NoteOffShift noteOffShift = NoteOffShift.False;
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
                double constFac = this.frameEnd - this.frameStart;
                for (int i = 0; i < chordSequence.size() - 1; ++i) {    // for each chord/note until the pre-last
                    double dateOffset = (Math.pow(((double) i) / (chordSequence.size() - 1), this.intensity) * constFac) + this.frameStart;
                    previous = this.setOrnamentDateAtts(dateOffset, chordSequence.get(i), previous);
                }
            }

            // place the final chord at frameEnd
            this.setOrnamentDateAtts(this.frameEnd, chordSequence.get(chordSequence.size() - 1), previous);

        }

        /**
         * helper method for method apply() to set the ornament attributes on each note:
         *      - ornament.date.offset or ornament.milliseconds.date.offset (an offset),
         *      - ornament.duration or ornament.milliseconds.duration (absolute duration),
         *      - ornament.noteoff.shift (true/false)
         * @param dateOffset
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
                    dateOffset += Double.parseDouble(ornamentDateAtt.getValue());
                    ornamentDateAtt.setValue(String.valueOf(dateOffset));
                } else
                    note.addAttribute(new Attribute(dateAttName, String.valueOf(dateOffset)));
            }

            // handle the ornament.duration or ornament.milliseconds.duration, resp.
            switch (this.noteOffShift) {
                case False:
                    return null;
                case True:
                    for (Element note : chord)
                        if (this.noteOffShift == NoteOffShift.True)
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
                    return chord;
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
    }

    /**
     * This class represents the dynamicsGradient transformer of ornamentDef
     * @author Axel Berndt
     */
    public static class DynamicsGradient {
        public double transitionFrom = 0.0;
        public double transitionTo = 0.0;
        private Element xml = null;

        /**
         * constructor
         */
        public DynamicsGradient() {}

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
    }
}
