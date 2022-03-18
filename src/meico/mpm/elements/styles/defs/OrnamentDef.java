package meico.mpm.elements.styles.defs;

import meico.mei.Helper;
import meico.mpm.Mpm;
import nu.xom.Attribute;
import nu.xom.Element;

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
                    ts.addAttribute(new Attribute("frame.end", Double.toString(temporalSpread.frameStart)));
                break; 
            case Milliseconds:
                if (temporalSpread.frameStart != 0.0)
                    ts.addAttribute(new Attribute("milliseconds.frame.start", Double.toString(temporalSpread.frameStart)));
                if (temporalSpread.frameEnd != 0.0)
                    ts.addAttribute(new Attribute("milliseconds.frame.end", Double.toString(temporalSpread.frameStart)));
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

        this.getXml().appendChild(ts);
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

        this.getXml().appendChild(dg);
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
    }

    /**
     * This class represents the dynamicsGradient transformer of ornamentDef
     * @author Axel Berndt
     */
    public static class DynamicsGradient {
        public double transitionFrom = 0.0;
        public double transitionTo = 0.0;

        /**
         * constructor
         */
        public DynamicsGradient() {}
    }

}
