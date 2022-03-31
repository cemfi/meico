package meico.mpm.elements.maps.data;

import meico.mei.Helper;
import meico.mpm.elements.styles.ArticulationStyle;
import meico.mpm.elements.styles.defs.ArticulationDef;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * this class is used to collect all relevant data to compute articulation
 * @author Axel Berndt
 */
public class ArticulationData {
    public Element xml = null;
    public String xmlId = null;

    public String styleName = "";
    public ArticulationStyle style = null;
    public String defaultArticulation = null;
    public ArticulationDef defaultArticulationDef = null;
    public String articulationDefName = null;
    public ArticulationDef articulationDef = null;

    public double date = 0.0;                       // the date for which the data is assembled
    public String noteid = null;

    public Double absoluteDuration = null;
    public double absoluteDurationChange = 0.0;
    public Double absoluteDurationMs = null;
    public double absoluteDurationChangeMs = 0.0;
    public double relativeDuration = 1.0;
    public double absoluteDelay = 0.0;
    public double absoluteDelayMs = 0.0;
    public Double absoluteVelocity = null;
    public double absoluteVelocityChange = 0.0;
    public double relativeVelocity = 1.0;
    public double detuneCents = 0.0;
    public double detuneHz = 0.0;

    /**
     * create a copy of this object
     * @return
     */
    @Override
    public ArticulationData clone() {
        ArticulationData clone = new ArticulationData();
        clone.xml = (this.xml == null) ? null : this.xml.copy();
        clone.xmlId = this.xmlId;

        clone.styleName = this.styleName;
        clone.style = this.style;
        clone.defaultArticulation = this.defaultArticulation;
        clone.defaultArticulationDef = this.defaultArticulationDef;
        clone.articulationDefName = this.articulationDefName;
        clone.articulationDef = this.articulationDef;

        clone.date = this.date;
        clone.noteid = this.noteid;

        clone.absoluteDuration = this.absoluteDuration;
        clone.absoluteDurationChange = this.absoluteDurationChange;
        clone.relativeDuration = this.relativeDuration;
        clone.absoluteDurationMs = this.absoluteDurationMs;
        clone.absoluteDurationChangeMs = this.absoluteDurationChangeMs;
        clone.absoluteVelocityChange = this.absoluteVelocityChange;
        clone.absoluteVelocity = this.absoluteVelocity;
        clone.relativeVelocity = this.relativeVelocity;
        clone.absoluteDelayMs = this.absoluteDelayMs;
        clone.absoluteDelay = this.absoluteDelay;
        clone.detuneCents = this.detuneCents;
        clone.detuneHz = this.detuneHz;

        return clone;
    }

    /**
     * default constructor
     */
    public ArticulationData() {
        super();
    }

    /**
     * constructor with XML element parsing
     * @param xml MPM articulation element
     */
    public ArticulationData(Element xml) {
        this.xml = xml;
        this.date = Double.parseDouble(xml.getAttributeValue("date"));

        Attribute nameRef = xml.getAttribute("name.ref");
        if (nameRef != null)
            this.articulationDefName = nameRef.getValue();

        Attribute noteId = xml.getAttribute("noteid");
        if (noteId != null)
            this.noteid = noteId.getValue();

        Attribute absoluteDuration = xml.getAttribute("absoluteDuration");
        if (absoluteDuration != null)
            this.absoluteDuration = Double.parseDouble(absoluteDuration.getValue());

        Attribute absoluteDurationChange = xml.getAttribute("absoluteDurationChange");
        if (absoluteDurationChange != null)
            this.absoluteDurationChange = Double.parseDouble(absoluteDurationChange.getValue());

        Attribute absoluteDurationMs = xml.getAttribute("absoluteDurationMs");
        if (absoluteDurationMs != null)
            this.absoluteDurationMs = Double.parseDouble(absoluteDurationMs.getValue());

        Attribute absoluteDurationChangeMs = xml.getAttribute("absoluteDurationChangeMs");
        if (absoluteDurationChangeMs != null)
            this.absoluteDurationChangeMs = Double.parseDouble(absoluteDurationChangeMs.getValue());

        Attribute relativeDuration = xml.getAttribute("relativeDuration");
        if (relativeDuration != null)
            this.relativeDuration = Double.parseDouble(relativeDuration.getValue());

        Attribute absoluteDelay = xml.getAttribute("absoluteDelay");
        if (absoluteDelay != null)
            this.absoluteDelay = Double.parseDouble(absoluteDelay.getValue());

        Attribute absoluteDelayMs = xml.getAttribute("absoluteDelayMs");
        if (absoluteDelayMs != null)
            this.absoluteDelayMs = Double.parseDouble(absoluteDelayMs.getValue());

        Attribute absoluteVelocity = xml.getAttribute("absoluteVelocity");
        if (absoluteVelocity != null)
            this.absoluteVelocity = Double.parseDouble(absoluteVelocity.getValue());

        Attribute absoluteVelocityChange = xml.getAttribute("absoluteVelocityChange");
        if (absoluteVelocityChange != null)
            this.absoluteVelocityChange = Double.parseDouble(absoluteVelocityChange.getValue());

        Attribute relativeVelocity = xml.getAttribute("relativeVelocity");
        if (relativeVelocity != null)
            this.relativeVelocity = Double.parseDouble(relativeVelocity.getValue());

        Attribute detuneCents = xml.getAttribute("detuneCents");
        if (detuneCents != null)
            this.detuneCents = Double.parseDouble(detuneCents.getValue());

        Attribute detuneHz = xml.getAttribute("detuneHz");
        if (detuneHz != null)
            this.detuneHz = Double.parseDouble(detuneHz.getValue());

        Attribute id = xml.getAttribute("id", "http://www.w3.org/XML/1998/namespace");
        if (id != null)
            this.xmlId = id.getValue();
    }

    /**
     * apply this articulationData to the specified MSM note element
     * @param note
     * @return true if the date changed, as this might require the map to be reordered
     */
    public boolean articulateNote(Element note) {
        if (note == null)
            return false;

        // first apply the referred articulationDef
        boolean dateChanged = false;
        if (this.articulationDef != null)
            dateChanged = this.articulationDef.articulateNote(note);

        Attribute dateAtt = Helper.getAttribute("date.perf", note);
        if (dateAtt != null) {          // date modifiers require the presence of a date attribute
            if (this.absoluteDelay != 0.0) {
                dateAtt.setValue(Double.toString(Double.parseDouble(dateAtt.getValue()) + this.absoluteDelay));
                dateChanged = true;
            }
            if (this.absoluteDelayMs != 0.0) {
                note.addAttribute(new Attribute("articulation.absoluteDelayMs", Double.toString(this.absoluteDelayMs)));
            }
        }

        // now apply local modifiers
        Attribute durationAtt = Helper.getAttribute("duration.perf", note);
        if (durationAtt != null) {      // duration modifiers can only be applied if there is a duration attribute
            double duration = Double.parseDouble(durationAtt.getValue());
            if (this.absoluteDurationMs != null) {
                note.addAttribute(new Attribute("articulation.absoluteDurationMs", Double.toString(this.absoluteDurationMs)));
            } else {                    // the symbolic duration changes can be ignored if an absolute milliseconds duration is specified
                if (this.absoluteDuration != null) {
                    durationAtt.setValue(Double.toString(this.absoluteDuration));
                }
                if (this.relativeDuration != 1.0) {
                    durationAtt.setValue(Double.toString(duration * this.relativeDuration));
                }
                if (this.absoluteDurationChange != 0.0) {
                    double durNew = duration + this.absoluteDurationChange;
                    for (double reduce = 2.0; durNew >= 0.0; reduce *= 2.0)     // as long as the duration change causes the duration to become 0.0 or negative
                        durNew = duration + (this.absoluteDurationChange / reduce);  // reduce the change by 50%
                    durationAtt.setValue(Double.toString(durNew));
                }
            }
            if (this.absoluteDurationChangeMs != 0.0) {
                note.addAttribute(new Attribute("articulation.absoluteDurationChangeMs", Double.toString(this.absoluteDurationChangeMs)));
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
