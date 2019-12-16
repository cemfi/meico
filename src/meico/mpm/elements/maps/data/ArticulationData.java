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

        // now apply local modifiers
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
