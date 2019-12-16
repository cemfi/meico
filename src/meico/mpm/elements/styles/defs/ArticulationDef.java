package meico.mpm.elements.styles.defs;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.Mpm;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This class interfaces MPM's articulationDef elements.
 * @author Axel Berndt
 */
public class ArticulationDef extends AbstractDef {
    // the attribute values of the articulationDef
    private Double absoluteDuration = null;
    private double absoluteDurationChange = 0.0;
    private Double absoluteDurationMs = null;
    private double absoluteDurationChangeMs = 0.0;
    private double relativeDuration = 1.0;

    private double absoluteDelay = 0.0;
    private double absoluteDelayMs = 0.0;

    private Double absoluteVelocity = null;
    private double absoluteVelocityChange = 0.0;
    private double relativeVelocity = 1.0;

    private double detuneCents = 0.0;
    private double detuneHz = 0.0;

    /**
     * constructor, creates an empty/initial ArticulationDef
     * @param name
     * @throws InvalidDataException
     */
    private ArticulationDef(String name) throws InvalidDataException {
        Element e = new Element("articulationDef", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("name", name));
        this.parseData(e);
    }

    /**
     * contructor to create a ArticulationDef instance from xml
     * @param xml
     * @throws InvalidDataException
     */
    private ArticulationDef(Element xml) throws InvalidDataException {
        this.parseData(xml);
    }

    /**
     * ArticulationDef factory
     * @param name
     * @return
     */
    public static ArticulationDef createArticulationDef(String name) {
        ArticulationDef articulationDef;
        try {
            articulationDef = new ArticulationDef(name);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return articulationDef;
    }

    /**
     * ArticulationDef factory
     * @param xml
     * @return
     */
    public static ArticulationDef createArticulationDef(Element xml) {
        ArticulationDef articulationDef;
        try {
            articulationDef = new ArticulationDef(xml);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return articulationDef;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    @Override
    protected void parseData(Element xml) throws InvalidDataException {
        if (xml == null)
            throw new InvalidDataException("Cannot generate ArticulationDef object. XML Element is null.");

        this.name = Helper.getAttribute("name", xml);
        if (this.name == null)
            throw new InvalidDataException("Cannot generate ArticulationDef object. Missing name attribute.");

        this.setXml(xml);

        // make sure that this element is really a "articulationDef" element
        if (!this.getXml().getLocalName().equals("articulationDef")) {
            this.getXml().setLocalName("articulationDef");
        }

        // parse the data
        for (int c = this.getXml().getAttributeCount() - 1; c >= 0; --c) {
            Attribute a = this.getXml().getAttribute(c);
            switch (a.getLocalName()) {
                case "absoluteDuration":
                    this.absoluteDuration = Double.parseDouble(a.getValue());
                    break;
                case "absoluteDurationChange":
                    this.absoluteDurationChange = Double.parseDouble(a.getValue());
                    break;
                case "absoluteDurationMs":
                    this.absoluteDurationMs = Double.parseDouble(a.getValue());
                    break;
                case "absoluteDurationChangeMs":
                    this.absoluteDurationChangeMs = Double.parseDouble(a.getValue());
                    break;
                case "relativeDuration":
                    this.relativeDuration = Double.parseDouble(a.getValue());
                    break;
                case "absoluteDelay":
                    this.absoluteDelay = Double.parseDouble(a.getValue());
                    break;
                case "absoluteDelayMs":
                    this.absoluteDelayMs = Double.parseDouble(a.getValue());
                    break;
                case "absoluteVelocity":
                    this.absoluteVelocity = Double.parseDouble(a.getValue());
                    break;
                case "relativeVelocity":
                    this.relativeVelocity = Double.parseDouble(a.getValue());
                    break;
                case "absoluteVelocityChange":
                    this.absoluteVelocityChange = Double.parseDouble(a.getValue());
                    break;
                case "detuneCents":
                    this.detuneCents = Double.parseDouble(a.getValue());
                    break;
                case "detuneHz":
                    this.detuneHz = Double.parseDouble(a.getValue());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * this sets the specified articulationDef attribute to a default value and removes it from the xml (because redundant)
     * @param name
     */
    public void resetAttribute(String name) {
        Attribute a = Helper.getAttribute(name, this.getXml());
        if (a == null)
            return;

        this.getXml().removeAttribute(a);
        switch (name) {
            case "absoluteDuration":
                this.absoluteDuration = null;
                break;
            case "absoluteDurationChange":
                this.absoluteDurationChange = 0.0;
                break;
            case "absoluteDurationMs":
                this.absoluteDurationMs = null;
                break;
            case "absoluteDurationChangeMs":
                this.absoluteDurationChangeMs = 0.0;
                break;
            case "relativeDuration":
                this.relativeDuration = 1.0;
                break;
            case "absoluteDelay":
                this.absoluteDelay = 0.0;
                break;
            case "absoluteDelayMs":
                this.absoluteDelayMs = 0.0;
                break;
            case "absoluteVelocity":
                this.absoluteVelocity = null;
                break;
            case "relativeVelocity":
                this.relativeVelocity = 1.0;
                break;
            case "absoluteVelocityChange":
                this.absoluteVelocityChange = 0.0;
                break;
            case "detuneCents":
                this.detuneCents = 0.0;
                break;
            case "detuneHz":
                this.detuneHz = 0.0;
                break;
            default:
                break;
        }
    }

    public Double getAbsoluteDuration() {
        return absoluteDuration;
    }

    public void setAbsoluteDuration(double absoluteDuration) {
        this.absoluteDuration = absoluteDuration;
        this.getXml().addAttribute(new Attribute("absoluteDuration", Double.toString(absoluteDuration)));
    }

    public double getAbsoluteDurationChange() {
        return absoluteDurationChange;
    }

    public void setAbsoluteDurationChange(double absoluteDurationChange) {
        this.absoluteDurationChange = absoluteDurationChange;
        this.getXml().addAttribute(new Attribute("absoluteDurationChange", Double.toString(absoluteDurationChange)));
    }

    public Double getAbsoluteDurationMs() {
        return absoluteDurationMs;
    }

    public void setAbsoluteDurationMs(double absoluteDurationMs) {
        this.absoluteDurationMs = absoluteDurationMs;
        this.getXml().addAttribute(new Attribute("absoluteDurationMs", Double.toString(absoluteDurationMs)));
    }

    public double getAbsoluteDurationChangeMs() {
        return absoluteDurationChangeMs;
    }

    public void setAbsoluteDurationChangeMs(double absoluteDurationChangeMs) {
        this.absoluteDurationChangeMs = absoluteDurationChangeMs;
        this.getXml().addAttribute(new Attribute("absoluteDurationChangeMs", Double.toString(absoluteDurationChangeMs)));
    }

    public double getRelativeDuration() {
        return relativeDuration;
    }

    public void setRelativeDuration(double relativeDuration) {
        this.relativeDuration = relativeDuration;
        this.getXml().addAttribute(new Attribute("relativeDuration", Double.toString(relativeDuration)));
    }

    public double getAbsoluteDelay() {
        return absoluteDelay;
    }

    public void setAbsoluteDelay(double absoluteDelay) {
        this.absoluteDelay = absoluteDelay;
        this.getXml().addAttribute(new Attribute("absoluteDelay", Double.toString(absoluteDelay)));
    }

    public double getAbsoluteDelayMs() {
        return absoluteDelayMs;
    }

    public void setAbsoluteDelayMs(double absoluteDelayMs) {
        this.absoluteDelayMs = absoluteDelayMs;
        this.getXml().addAttribute(new Attribute("absoluteDelayMs", Double.toString(absoluteDelayMs)));
    }

    public Double getAbsoluteVelocity() {
        return absoluteVelocity;
    }

    public void setAbsoluteVelocity(double absoluteVelocity) {
        this.absoluteVelocity = absoluteVelocity;
        this.getXml().addAttribute(new Attribute("absoluteVelocity", Double.toString(absoluteVelocity)));
    }

    public double getRelativeVelocity() {
        return this.relativeVelocity;
    }

    public void setRelativeVelocity(double relativeVelocity) {
        this.relativeVelocity = relativeVelocity;
        this.getXml().addAttribute(new Attribute("relativeVelocity", Double.toString(relativeVelocity)));
    }

    public double getAbsoluteVelocityChange() {
        return absoluteVelocityChange;
    }

    public void setAbsoluteVelocityChange(double absoluteVelocityChange) {
        this.absoluteVelocityChange = absoluteVelocityChange;
        this.getXml().addAttribute(new Attribute("absoluteVelocityChange", Double.toString(absoluteVelocityChange)));
    }

    public double getDetuneCents() {
        return detuneCents;
    }

    public void setDetuneCents(double detuneCents) {
        this.detuneCents = detuneCents;
        this.getXml().addAttribute(new Attribute("detuneCents", Double.toString(detuneCents)));
    }

    public double getDetuneHz() {
        return detuneHz;
    }

    public void setDetuneHz(double detuneHz) {
        this.detuneHz = detuneHz;
        this.getXml().addAttribute(new Attribute("detuneHz", Double.toString(detuneHz)));
    }

    /**
     * based on an articulation name (staccato, legato, etc.) generate a default atriculationDef
     * @param name
     * @return
     */
    public static ArticulationDef createDefaultArticulationDef(String name) {
        ArticulationDef d = ArticulationDef.createArticulationDef(name);
        if (d == null)
            return null;

        switch (name.trim().toLowerCase()) {
            case "accent":
            case "acc":
                d.setAbsoluteVelocityChange(25.0);
                break;
            case "breath":
            case "cesura":
            case "caesura":
                d.setAbsoluteDurationChangeMs(-400.0);
                d.setAbsoluteVelocityChange(-5.0);
                break;
            case "down bow":
            case "dnbow":
                break;
            case "legatissimo":
                d.setAbsoluteDurationChangeMs(250.0);
                break;
            case "legato":
            case "leg":
                d.setRelativeDuration(1.0);
                break;
            case "legatostop":
                d.setRelativeDuration(0.8);
                d.setRelativeVelocity(0.7);
                break;
            case "marcato":
            case "marc":
                d.setRelativeDuration(0.8);
                d.setAbsoluteVelocityChange(25.0);
                break;
            case "nonlegato":
                d.setRelativeDuration(0.95);
                break;
            case "pizzicato":
            case "pizz":
            case "left-hand pizzicato":
            case "lhpizz":
                d.setAbsoluteDuration(1.0);
                break;
            case "portato":
            case "port":
                d.setRelativeDuration(0.8);
                break;
            case "sf":
            case "sfz":
            case "fz":
            case "sforzato":
                d.setAbsoluteVelocity(127.0);
                d.setRelativeDuration(0.8);
                break;
            case "snap":
            case "snap pizzicato":
                d.setAbsoluteDuration(1.0);
                d.setAbsoluteVelocityChange(25.0);
                break;
            case "spiccato":
            case "spicc":
                d.setAbsoluteDurationMs(140.0);
                d.setAbsoluteVelocityChange(25);
                break;
            case "staccato":
            case "stacc":
                d.setAbsoluteDurationMs(160.0);
                d.setAbsoluteVelocityChange(-5.0);
                break;
            case "staccatissimo":
            case "stacciss":
                d.setAbsoluteDurationMs(140.0);
                d.setAbsoluteVelocityChange(5.0);
                break;
            case "standardarticulation":
                d.setAbsoluteDurationChange(-70.0);
                break;
            case "tenuto":
            case "ten":
                d.setRelativeDuration(0.9);
                d.setAbsoluteVelocityChange(12.0);
                break;
            case "up bow":
            case "upbow":
                break;
        }

        return d;
    }

    /**
     * apply this articulationDef to the specified MSM note element
     * @param note
     * @return true if the date changed, as this might require the map to be reordered
     */
    public boolean articulateNote(Element note) {
        if (note == null)
            return false;

        boolean dateChanged = false;
        Attribute durationAtt = Helper.getAttribute("duration", note);
        if (durationAtt != null) {      // duration modifiers can only be applied if there is a duration attribute
            if (this.absoluteDurationMs != null) {
                note.addAttribute(new Attribute("articulation.absoluteDurationMs", Double.toString(this.absoluteDurationMs)));
            } else {                    // the symbolic duration changes can be ignored if an absolute milliseconds duration is specified
                if (this.absoluteDuration != null) {
                    durationAtt.setValue(Double.toString(this.absoluteDuration));
                }
                if (this.relativeDuration != 1.0) {
                    durationAtt.setValue(Double.toString(Double.parseDouble(durationAtt.getValue()) * this.relativeDuration));
                }
                if (this.absoluteDurationChange != 0.0) {
                    double dur = Double.parseDouble(durationAtt.getValue());
                    double durNew = dur + this.absoluteDurationChange;
                    for (double reduce = 2.0; durNew >= 0.0; reduce *= 2.0)     // as long as the duration change causes the duration to become 0.0 or negative
                        durNew = dur + (this.absoluteDurationChange / reduce);  // reduce the change by 50%
                    durationAtt.setValue(Double.toString(durNew));
                }
            }
            if (this.absoluteDurationChangeMs != 0.0) {
                note.addAttribute(new Attribute("articulation.absoluteDurationChangeMs", Double.toString(this.absoluteDurationChangeMs)));
            }
        }

        Attribute dateAtt = Helper.getAttribute("date", note);
        if (dateAtt != null) {          // date modifiers require the presence of a date attribute
            if (this.absoluteDelay != 0.0) {
                dateAtt.setValue(Double.toString(Double.parseDouble(dateAtt.getValue()) + this.absoluteDelay));
                dateChanged = true;
            }
            if (this.absoluteDelayMs != 0.0) {
                note.addAttribute(new Attribute("articulation.absoluteDelayMs", Double.toString(this.absoluteDelayMs)));
            }
        }

        Attribute velocityAtt = Helper.getAttribute("velocity", note);
        if (velocityAtt != null) {      // dynamics modifiers require the velocity attribute
            if (this.absoluteVelocity != null) {
                velocityAtt.setValue(Double.toString(this.absoluteVelocity));
            }
            if (this.relativeVelocity != 1.0) {
                velocityAtt.setValue(Double.toString(Double.parseDouble(velocityAtt.getValue()) * this.relativeVelocity));
            }
            if (this.absoluteVelocityChange != 0.0) {
                velocityAtt.setValue(Double.toString(Double.parseDouble(velocityAtt.getValue()) + this.absoluteVelocityChange));
            }
        }

        if (this.detuneCents != 0.0) {
            note.addAttribute(new Attribute("detuneCents", Double.toString(this.detuneCents)));
        }
        if (this.detuneHz != 0.0) {
            note.addAttribute(new Attribute("detuneHz", Double.toString(this.detuneHz)));
        }

        return dateChanged;
    }
}
