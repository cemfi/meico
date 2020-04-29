package meico.mpm.elements.maps.data;

import meico.mpm.elements.styles.DynamicsStyle;
import meico.mpm.elements.styles.defs.DynamicsDef;
import nu.xom.Element;

import java.util.ArrayList;

/**
 * this class is used to collect all relevant data to compute articulation
 * @author Axel Berndt
 */
public class DynamicsData {
    public Element xml = null;
    public String xmlId = null;

    public String styleName = "";
    public DynamicsStyle style = null;
    public String dynamicsDefString = null;
    public DynamicsDef dynamicsDef = null;

    public double startDate = 0.0;
    public Double endDate = null;               // this is set to the date of the subsequent dynamics element or to null to indicate that the dynamics computation routine has to use the end date of the track

    public String volumeString = null;
    public Double volume = null;

    public String transitionToString = null;
    public Double transitionTo = null;

    public Double curvature = null;
    public Double protraction = null;
    public boolean subNoteDynamics = false;

    private Double x1 = null;
    private Double x2 = null;

    /**
     * create a copy of this object
     * @return
     */
    @Override
    public DynamicsData clone() {
        DynamicsData clone = new DynamicsData();
        clone.xml = (this.xml == null) ? null : this.xml.copy();
        clone.xmlId = this.xmlId;
        clone.styleName = this.styleName;
        clone.style = this.style;
        clone.dynamicsDefString = this.dynamicsDefString;
        clone.dynamicsDef = this.dynamicsDef;
        clone.startDate = this.startDate;
        clone.endDate = this.endDate;
        clone.transitionToString = this.transitionToString;
        clone.transitionTo = this.transitionTo;
        clone.volumeString = this.volumeString;
        clone.volume = this.volume;
        clone.curvature = this.curvature;
        clone.protraction = this.protraction;
        clone.subNoteDynamics = this.subNoteDynamics;
        clone.x1 = this.x1;
        clone.x2 = this.x2;
        return clone;
    }

    /**
     * check whether this represents a constant dynamics instruction
     * @return
     */
    public boolean isConstantDynamics() {
        return (this.transitionTo == null) || (this.volume == null)  || this.transitionTo.equals(this.volume);
    }

    /**
     * For continuous dynamics transitions the dynamics curve is constructed from a cubic, S-shaped Bézier curve (P0, P1, P2, P3): _/̅
     * This method derives the x-coordinates of the inner two control points from the values of curvature and protraction. All other coordinates are fixed.
     */
    private void computeInnerControlPointsXPositions() {
        if (this.curvature == null)
            this.curvature = 0.0;

        if (this.protraction == null)
            this.protraction = 0.0;

        if (this.protraction == 0.0) {
            this.x1 = this.curvature;
            this.x2 = 1.0 - this.curvature;
            return;
        }

        this.x1 = this.curvature + ((Math.abs(this.protraction) + this.protraction) / (2.0 * this.protraction) - (Math.abs(this.protraction) / this.protraction) * this.curvature) * protraction;
        this.x2 = 1.0 - this.curvature + ((this.protraction - Math.abs(this.protraction)) / (2.0 * this.protraction) + (Math.abs(this.protraction) / this.protraction) * this.curvature) * this.protraction;
    }

    /**
     * compute parameter t of the Bézier curve that corresponds to time position date
     * @param date time position
     * @return
     */
    private double getTForDate(double date) {
        // computational solution (exact but inefficient)
//        double a = -4.0 * Math.pow(this.x1, 3.0) * date + 4.0 * Math.pow(this.x1, 3.0) + 6.0 * Math.pow(this.x1, 2.0) * this.x2 * date + 9.0 * Math.pow(date, 2.0) * Math.pow(this.x1, 2.0) - 12.0 * date * Math.pow(this.x1, 2.0) - 3.0 * Math.pow(this.x1, 2.0) * Math.pow(this.x2, 2.0) - 18.0 * Math.pow(date, 2.0) * this.x1 * this.x2 + 6.0 * date * this.x1 * this.x2 + 6.0 * Math.pow(date, 2.0) * this.x1 + 6.0 * this.x1 * Math.pow(this.x2, 2.0) * date + Math.pow(date, 2.0) + 9.0 * Math.pow(date, 2.0) * Math.pow(this.x2, 2.0) - 4.0 * date * Math.pow(this.x2, 3.0) - 6.0 * Math.pow(date, 2.0) * this.x2;
//        a = (a >= 0.0) ? Math.sqrt(a) : 0.0;
//        double b = -8.0 * Math.pow(this.x1, 3.0) + 12.0 * Math.pow(this.x1, 2.0) * this.x2 - 24.0 * Math.pow(this.x1, 3.0) + 12.0 * this.x1 * Math.pow(this.x2, 2.0) + 12.0 * this.x1 * this.x2 + 36.0 * date * Math.pow(this.x1, 2.0) - 72.0 * date * this.x1 * this.x2 + 24.0 * date * this.x1 + 36.0 * date * Math.pow(this.x2, 2.0) - 24.0 * date * this.x2 + 4.0 * date - 8.0 * Math.pow(this.x2, 3.0) + 12.0 * a * this.x1 - 12.0 * a * this.x2 + 4.0 * a;
//        b = (b >= 0.0) ? Math.pow(b, 1.0 / 3.0) : 0.0;
//        double t = Math.pow(this.x1, 2.0) - this.x1 * this.x2 - this.x1 + Math.pow(this.x2, 2.0);
//        t = b / 2.0 + 2.0 * this.x1 - this.x2 + 2.0 / b * t;
//        t = t / (3.0 * this.x1 - 3.0 * this.x2 + 1.0);
//        return t;

        // numerical solution (not exact, however integer-precise and more efficient)
        if (date == this.startDate)
            return 0.0;

        if (date == this.endDate)
            return 1.0;

        if (this.x1 == null)    // ||(x2 == null)
            this.computeInnerControlPointsXPositions();

        // values that are often required
        double s = this.endDate - this.startDate;
        date = date - this.startDate;
        double u = (3.0 * this.x1) - (3.0 * this.x2) + 1.0;
        double v = (-6.0 * this.x1) + (3.0 * this.x2);
        double w = 3.0 * this.x1;

        // binary search for the t that is integer precise on the x-axis/time domain
        double t = 0.5;
        double diffX = ((((u * t) + v) * t + w) * t * s) - date;
        for (double tt = 0.25; Math.abs(diffX) >= 1.0; tt *= 0.5) { // while the difference in the x-domain is >= 1.0
            if (diffX > 0.0)                                        // if t is too small
                t -= tt;
            else                                                    // if t is too big
                t += tt;
            diffX = ((((u * t) + v) * t + w) * t * s) - date;       // compute difference
        }
        return t;
    }

    /**
     * compute the dynamics value at the given tick position
     * @param date
     * @return
     */
    public double getDynamicsAt(double date) {
        if ((date < this.startDate) || this.isConstantDynamics())
            return this.volume;

        if (date >= this.endDate)
            return this.transitionTo;

        double t = this.getTForDate(date);
        return ((((3.0 - (2.0 * t)) * t * t) * (this.transitionTo - this.volume)) + this.volume);
    }

    /**
     * this method works directly with the parameter t to specify a point on the Bézier curve
     * and returns a tuplet [date, volume]
     * @param t
     * @return
     */
    private double[] getDateDynamics(double t) {
        double[] result = new double[2];

        double x1_3 = 3.0 * this.x1;
        double x2_3 = 3.0 * this.x2;
        double u = x1_3 - x2_3 + 1.0;
        double v = (-6.0 * this.x1) + x2_3;
        result[0] = ((((u * t) + v) * t + x1_3) * t * (this.endDate - this.startDate)) + this.startDate;

        result[1] = ((((3.0 - (2.0 * t)) * t * t) * (this.transitionTo - this.volume)) + this.volume);

        return result;
    }

    /**
     * This method generates a list of [date, volume] tuplets that can be rendered into a sequence of channelVolume events.
     * @param maxStepSize this sets the maximum volume step size between two adjacent tuplets
     * @return
     */
    public ArrayList<double[]> getSubNoteDynamicsSegment(double maxStepSize) {
        if (this.x1 == null)    // ||(x2 == null)
            this.computeInnerControlPointsXPositions();

        ArrayList<Double> ts = new ArrayList<>();
        ts.add(0.0);
        ts.add(1.0);
        ArrayList<double[]> series = new ArrayList<>();
        series.add(this.getDateDynamics(0.0));                  // we start with the first value
        series.add(this.getDateDynamics(1.0));                  // and end up with the last value

        // generate further tuplets in-between each two adjacent tuplets as long as their value difference is greater than maxStepSize; this here is basically a depth-first algorithm
        for (int i = 0; i < ts.size() - 1; ++i) {
            while (Math.abs(series.get(i+1)[1] - series.get(i)[1]) > maxStepSize) {
                double t = (ts.get(i) + ts.get(i+1)) * 0.5;
                ts.add(i+1, t);
                series.add(i+1, this.getDateDynamics(t));
            }
        }

//        series.remove(series.size() - 1);       // remove the last event as dynamics instructions cover the interval [startDate, endDate); No! Not necessary as method DynamicsMap.renderDynamicsToMap() uses this one to set a default volume if a non-sub-note dynamics segment follows

        return series;
    }
}
