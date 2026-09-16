package meico.mpm.elements.styles.defs;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.maps.data.TemporalValue;
import meico.msm.elements.MsmNoteElement;
import meico.supplementary.KeyValue;
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
     * create a deep copy of this
     * @return
     */
    @Override
    public OrnamentDef clone() {
        Element xmlCopy = this.getXml().copy();
        return OrnamentDef.createOrnamentDef(xmlCopy);
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
    public void setTemporalSpread(double frameStart, double frameLength, TemporalValue.Domain frameDomain, double intensity, TemporalSpread.NoteOffShift noteOffShift) {
       setTemporalSpread(TemporalValue.create(frameStart, frameDomain), TemporalValue.create(frameLength, frameDomain), intensity, noteOffShift, false);
    }
    public void setTemporalSpread(double frameStart, double frameLength, TemporalValue.Domain frameDomain, double intensity, TemporalSpread.NoteOffShift noteOffShift, boolean atEnd) {
       setTemporalSpread(TemporalValue.create(frameStart, frameDomain), TemporalValue.create(frameLength, frameDomain), intensity, noteOffShift, atEnd);
    }
    public void setTemporalSpread(TemporalValue frameStart, TemporalValue frameLength, double intensity, TemporalSpread.NoteOffShift noteOffShift) {
        setTemporalSpread(frameStart, frameLength, intensity, noteOffShift, false);
    }
    public void setTemporalSpread(TemporalValue frameStart, TemporalValue frameLength, double intensity, TemporalSpread.NoteOffShift noteOffShift, boolean atEnd) {
        TemporalSpread temporalSpread = new TemporalSpread();
        temporalSpread.frameStart = frameStart;
        temporalSpread.frameLength = frameLength;
        temporalSpread.intensity = intensity;
        temporalSpread.noteOffShift = noteOffShift;
        temporalSpread.alignment = atEnd ? "at end" : "at start";
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

        String shortname = name.trim().toLowerCase();

        switch (shortname) {
            case "arpeg":
            case "arpeggio":
                def.setDynamicsGradient(-1.0, 1.0);
                def.setTemporalSpread(-22.0, 66.0, TemporalValue.Domain.Ticks, 1.0, TemporalSpread.NoteOffShift.False);
                break;
            case "mordent":
            case "upper mordent":
            case "lower mordent":
                def.setDynamicsGradient(1.0, -1.0);
                def.setTemporalSpread(0, 180.0, TemporalValue.Domain.Ticks, 0.9, TemporalSpread.NoteOffShift.Monophonic);
                break;
            case "fioritura":
                def.setDynamicsGradient(1.0, 1.0);
                def.setTemporalSpread(0, 100, TemporalValue.Domain.Relative,1.0, TemporalSpread.NoteOffShift.Monophonic);
                break;
            case "grace unacc":
                def.setDynamicsGradient(1.0, -1.0);
                def.setTemporalSpread(-90.0, 90.0, TemporalValue.Domain.Ticks, 1.0, TemporalSpread.NoteOffShift.Monophonic);
                break;
            case "grace acc":
                def.setDynamicsGradient(1.0, -1.0);
                def.setTemporalSpread(0, 90.0, TemporalValue.Domain.Ticks, 1.0, TemporalSpread.NoteOffShift.Monophonic);
                break;
            case "grace acc delayed":
                def.setDynamicsGradient(1.0, -1.0);
                def.setTemporalSpread(0, 90.0, TemporalValue.Domain.Ticks, 1.0, TemporalSpread.NoteOffShift.Monophonic, true);
                break;
            case "grace unacc delayed":
                def.setDynamicsGradient(1.0, -1.0);
                def.setTemporalSpread(0, 90.0, TemporalValue.Domain.Ticks, 1.0, TemporalSpread.NoteOffShift.Monophonic, true);
                break;
            case "turn delayed":
            case "upper turn delayed":
            case "lower turn delayed":
                def.setDynamicsGradient(1.0, -1.0);
                def.setTemporalSpread(0, 50, TemporalValue.Domain.Relative, 1.0, TemporalSpread.NoteOffShift.Monophonic, true);
                break;
            case "tremolo":
                def.setDynamicsGradient(1.0, 0.0);
                def.setTemporalSpread(0, 100, TemporalValue.Domain.Relative, 1.0f, TemporalSpread.NoteOffShift.Monophonic);
                break;
            default:
                def.setDynamicsGradient(-1.0, 1.0);
                def.setTemporalSpread(0, 80, TemporalValue.Domain.Relative, 0.9, TemporalSpread.NoteOffShift.Monophonic);
        }

        return def;
    }

    /**
     * This class represents the temporalSpread transformer of ornamentDef
     * @author Axel Berndt
     */
    public static class TemporalSpread {
        public TemporalValue frameStart = TemporalValue.create(0.0, TemporalValue.Domain.Ticks);
        public TemporalValue frameLength = TemporalValue.create(100.0, TemporalValue.Domain.Relative);    // must be >= 0.0
        public double intensity = 1.0;
        public NoteOffShift noteOffShift = NoteOffShift.False;
        public String alignment = "at start"; // "at start" (default) or "at end" – controls whether the ornament is anchored at the start or end of the principal note
        private String id = null;
        private Element xml;

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
            frameStart.setDomain(TemporalValue.Domain.Ticks);
            frameLength.setDomain(TemporalValue.Domain.Ticks);

            if (domain != null) {
                switch (domain.getValue()) {
                    case "milliseconds":
                        frameStart.setDomain(TemporalValue.Domain.Milliseconds);
                        frameLength.setDomain(TemporalValue.Domain.Milliseconds);
                        break;
                    case "relative":
                        frameStart.setDomain(TemporalValue.Domain.Relative);
                        frameLength.setDomain(TemporalValue.Domain.Relative);
                        break;
                    // TODO: TemporalValue.Domain.RelativeToNoteDuration?
                    case "ticks":
                    default:
                        // unnecessary because default
                }
            }

            Attribute start = Helper.getAttribute("frame.offset", xml);
            if(start == null) {
                start = Helper.getAttribute("frame.start", xml);
            }
            if (start != null)
                this.frameStart.setValue(start.getValue());

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

            Attribute alignmentAtt = Helper.getAttribute("alignment", xml);
            if (alignmentAtt != null)
                this.alignment = alignmentAtt.getValue(); // "at start" or "at end"
        }

        /**
         * set the length of the frame
         * @param length must be positive, otherwise it defaults to 0.0
         */
        public void setFrameLength(double length) {
            this.frameLength.setValue(Math.max(0.0, length));
        }

        public void setFrameLengthDomain(TemporalValue.Domain domain) {
            this.frameLength.setDomain(domain);
        }
        /**
         * get the frame length
         * @return
         */
        public double getFrameLength() {
            return this.frameLength.getValue();
        }
        public TemporalValue.Domain getFrameLengthDomain() {
            return this.frameLength.getDomain();
        }


        /**
         * apply the temporal spread to the chord/note sequence;
         * the notes get new attributes ornament.date.offset or ornament.date.offset.milliseconds,
         * and ornament.duration.offset or ornament.duration.milliseconds
         * @param chordSequence the sequence of the chords/notes in which the temporal spread is applied
         */
        public void apply(ArrayList<ArrayList<Element>> chordSequence) {
            apply(chordSequence, null, null, null);
        }

        /**
         * apply the temporal spread with optional effective frameStart/frameLength overrides (in ticks).
         * These overrides are used when multiple ornaments share the same principal note
         * and their frameLengths need proportional distribution.
         * @param chordSequence the sequence of the chords/notes in which the temporal spread is applied
         * @param effectiveFrameStart if non-null, overrides the computed frame start (in ticks)
         * @param effectiveFrameLength if non-null, overrides the computed frame length (in ticks)
         * @param lastNote of latest ornament in which we might render into
         * @return computed spaced start and length (relative in ticks) or null
         */
        public KeyValue<Double, Double> apply(ArrayList<ArrayList<Element>> chordSequence, Double effectiveFrameStart, Double effectiveFrameLength, MsmNoteElement lastNote) {
            if (chordSequence.size() < 1)   // if there is no chord/note or just one
                return null;     // we don't do anything

            double length = this.frameLength.getValue();
            double start  = this.frameStart.getValue();

            if(this.frameLength.isRelative() || this.frameStart.isRelative()) {
                // if the frame length is relative, we have to compute the absolute frame length according to the duration of the chord/note sequence
                double d = -1.0;
                for (ArrayList<Element> chord : chordSequence) {
                    for (Element note : chord) {
                        d = Double.parseDouble(Helper.getAttributeValue("milliseconds.date.end", note)) - Double.parseDouble(Helper.getAttributeValue("milliseconds.date", note));
                    }
                    if(d >= 0.0)
                        break;
                }
                if(this.frameLength.isRelative())
                    length = (length * 0.01) * d;
                if(this.frameStart.isRelative())
                    start = (start * 0.01) * d;
            }

            // apply effective overrides if provided (from multi-ornament proportional distribution)
            if (effectiveFrameStart != null)
                start = effectiveFrameStart;
            if (effectiveFrameLength != null)
                length = effectiveFrameLength;

            double spacedStart = start;
            double spacedLength = length;

            // if atEnd, place the frame at the end of the principal note's duration;
            // frameStart is treated as an offset, so atEnd with frameStart=0
            // means the ornament ends exactly at the note's end
            if (this.isAtEnd() && effectiveFrameStart == null) {
                double principalDuration = -1.0;
                for (ArrayList<Element> chord : chordSequence) {
                    for (Element note : chord) {
                        Attribute durAtt = Helper.getAttribute("duration", note);
                        if (durAtt != null) {
                            principalDuration = Double.parseDouble(durAtt.getValue());
                            break;
                        }
                    }
                    if (principalDuration >= 0.0)
                        break;
                }
                if (principalDuration >= 0.0)
                    start = principalDuration - length + start;
            }

            double lastDateOffset = spacedStart;

            // process all chords/notes; spacing as if there were n+1 positions,
            // so each note has equal space and the last note still has room to sound until frameEnd
            ArrayList<Element> previous = null;
            if(lastNote != null) {
                previous = new ArrayList<>();
                previous.add(lastNote.getElement());
                lastDateOffset = lastNote.getAsDouble("ornament.milliseconds.date.offset");
            }


            for (int i = 0; i < chordSequence.size(); ++i) {
                boolean removedNote = false;
                if(i == 0 && lastNote != null) {
                    // check if we render into an existing note (occurs if another ornament is already applied)

                    for(Element n : chordSequence.get(i)) {
                        MsmNoteElement note = new MsmNoteElement(n);
                        if(note.get("midi.pitch").equals(lastNote.get("midi.pitch"))) {
                            note.removeParent();
                            removedNote = true;
                            lastDateOffset = lastNote.getAsDouble("ornament.milliseconds.date.offset");
                        }
                    }
                }
                double dateOffset = (Math.pow(((double) i) / chordSequence.size(), this.intensity) * length) + start;
                double lastDuration = dateOffset - lastDateOffset;
                lastDateOffset = dateOffset;

                if(i == 0) {
                    spacedStart = dateOffset;
                }

                previous = this.setOrnamentDateAtts(dateOffset, lastDuration, chordSequence.get(i), previous);
                if(removedNote) { // expand note up to 2nd if 1st note has been removed
                    previous = new ArrayList<>();
                    previous.add(lastNote.getElement());
                    lastDateOffset = lastNote.getAsDouble("ornament.milliseconds.date.offset");
                }
            }

            double lastDuration = spacedStart + spacedLength - lastDateOffset;

            this.setOrnamentDateAtts(lastDateOffset, lastDuration, new ArrayList<Element>(), previous);

            KeyValue<Double, Double> result = new KeyValue<>(spacedStart, spacedLength);
            return result;
        }


        /**
         * helper method for method apply() to set the ornament attributes on each note:
         *      - ornament.date.offset, ornament.milliseconds.date.offset, or ornament.relative.date.offset (an offset),
         *      - ornament.duration, ornament.milliseconds.duration (absolute duration), or ornament.relative.duration)
         *      - ornament.noteoff.shift (true/false)
         * @param dateOffset the offset to the date/milliseconds.date of the chord/notes
         * @param duration
         * @param chord
         * @param previous the previous chord, so we can treat its duration according to the chords offset, or null
         * @return the chord, if its duration needs treatment along the processing of the next chord (then as previous); otherwise null
         */
        private ArrayList<Element> setOrnamentDateAtts(double dateOffset, double duration, ArrayList<Element> chord, ArrayList<Element> previous) {
            String dateAttName, durAttName;
            switch (this.frameStart.getDomain()) {
                case Ticks:
                case Relative: // at this moment values are in ticks
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

            dateAttName = "ornament.milliseconds.date.offset";
            durAttName = "ornament.milliseconds.duration";

            // set the ornament[.*].date.offset
            for (Element note : chord) {
                Attribute ornamentDateAtt = Helper.getAttribute(dateAttName, note);
                if (ornamentDateAtt != null) {
                    ornamentDateAtt.setValue(String.valueOf(dateOffset + Double.parseDouble(ornamentDateAtt.getValue())));
                } else
                    note.addAttribute(new Attribute(dateAttName, String.valueOf(dateOffset)));


                Attribute durationAtt = Helper.getAttribute("duration", note);
                if(durationAtt == null)
                    continue;
                Attribute ornamentDurAtt = Helper.getAttribute(durAttName, note);
                if (ornamentDurAtt != null) {
                    ornamentDurAtt.setValue(Helper.getAttributeValue("duration", note));
                } else
                    note.addAttribute(new Attribute(durAttName, Helper.getAttributeValue("duration", note)));

            }

            // handle the ornament[.*].duration
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
                                ornamentDurationAtt.setValue(String.valueOf(duration));
                            else
                                prev.addAttribute(new Attribute(durAttName, String.valueOf(duration)));
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

            if (this.frameStart.getValue() != 0.0)
                ts.addAttribute(new Attribute("frame.start", Double.toString(this.frameStart.getValue())));
            if (this.frameLength.getValue() != 0.0)
                ts.addAttribute(new Attribute("frameLength", Double.toString(this.frameLength.getValue())));

            switch (this.frameStart.getDomain()) {
                case Ticks:
                    // not necessary because this is the default value when absent
                    ts.addAttribute(new Attribute("time.unit", "ticks"));
                    break;
                case Milliseconds:
                    ts.addAttribute(new Attribute("time.unit", "milliseconds"));
                    break;
                case Relative:
                    ts.addAttribute(new Attribute("time.unit", "relative"));
                    break;
//            case RelativeToNoteDuration:
//                throw new UnsupportedDataTypeException("The feature TemporalValue.Domain.RelativeToNoteDuration is not yet supported.");
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

            if (this.isAtEnd())
                ts.addAttribute(new Attribute("alignment", "at end"));

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

        /**
         * returns true if this ornament is anchored at the end of the principal note ("at end"),
         * false if it is anchored at the start ("at start", default)
         * @return true if alignment is "at end"
         */
        public boolean isAtEnd() {
            return "at end".equals(this.alignment);
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
