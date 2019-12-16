package meico.mpm.elements.maps.data;

import meico.mpm.elements.styles.TempoStyle;
import nu.xom.Element;

/**
 * this class is used to collect all relevant data to compute articulation
 * @author Axel Berndt
 */
public class TempoData {
//    public double date = 0.0;                   // the date for which the data is assembled
    public Element xml = null;
    public String xmlId = null;

    public String styleName = "";
    public TempoStyle style = null;

    public double startDate = 0.0;              // the date at which the tempo instruction starts
    public Double startDateMilliseconds = null; // to be used during timing computations, not by the application
    public Double endDate = null;               // this is set to the date of the subsequent tempo element or to null to indicate that the tempo computation routine has to use the end date of the track

    public String bpmString = null;
    public Double bpm = null;
    public String transitionToString = null;
    public Double transitionTo = null;

    public double beatLength = 0.25;

    public Double meanTempoAt = null;
    public Double exponent = null;

    /**
     * create a copy of this object
     * @return
     */
    @Override
    public TempoData clone() {
        TempoData clone = new TempoData();
//        clone.date = this.date;
        clone.xml = (this.xml == null) ? null : this.xml.copy();
        clone.xmlId = this.xmlId;
        clone.styleName = this.styleName;
        clone.style = this.style;
        clone.startDate = this.startDate;
        clone.endDate = this.endDate;
        clone.bpmString = this.bpmString;
        clone.bpm = this.bpm;
        clone.transitionToString = this.transitionToString;
        clone.transitionTo = this.transitionTo;
        clone.beatLength = this.beatLength;
        clone.meanTempoAt = this.meanTempoAt;
        clone.exponent = this.exponent;
        return clone;
    }

    /**
     * check whether this represents a constant tempo instruction
     * @return
     */
    public boolean isConstantTempo() {
        return (this.transitionTo == null) || (this.bpm == null)  || this.transitionTo.equals(this.bpm);
    }
}
