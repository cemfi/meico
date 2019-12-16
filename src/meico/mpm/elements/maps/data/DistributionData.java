package meico.mpm.elements.maps.data;

import meico.supplementary.KeyValue;
import nu.xom.Element;

import java.util.ArrayList;

/**
 * this class is used to collect all relevant data to compute imprecision
 * @author Axel Berndt
 */
public class DistributionData {
    public Element xml = null;
    public String xmlId = null;
    public double startDate = 0.0;              // the time position of the distribution element
    public Double endDate = null;

    public String type = "";                    // the type of distribution, i.e. the local name of the element

    public Double standardDeviation = null;

    public Double maxStepWidth = null;

    public Double degreeOfCorrelation = null;

    public Double mode = null;

    public Double lowerLimit = null;
    public Double upperLimit = null;

    public Double minValue = null;
    public Double maxValue = null;

    public Double timingBasisMilliseconds = null;

    public ArrayList<Double> distributionList = new ArrayList<>();

    /**
     * create a copy of this object
     * @return
     */
    @Override
    public DistributionData clone() {
        DistributionData clone = new DistributionData();
        clone.xml = (this.xml == null) ? null : this.xml.copy();
        clone.xmlId = this.xmlId;
        clone.startDate = this.startDate;
        clone.endDate = this.endDate;

        clone.type = this.type;
        clone.standardDeviation = this.standardDeviation;
        clone.maxStepWidth = this.maxStepWidth;
        clone.degreeOfCorrelation = this.degreeOfCorrelation;
        clone.mode = this.mode;
        clone.lowerLimit = this.lowerLimit;
        clone.upperLimit = this.upperLimit;
        clone.minValue = this.minValue;
        clone.maxValue = this.maxValue;
        clone.timingBasisMilliseconds = this.timingBasisMilliseconds;
        clone.distributionList = new ArrayList<>(this.distributionList);

        return clone;
    }

    /**
     * deternime the minimum and maximum value in the distribution list
     * @return a KeyValue tuplet (min value, max value)
     */
    public KeyValue<Double, Double> getMinAndMaxValueInDistributionList() {
        if (this.distributionList.isEmpty())
            return null;

        double min = this.distributionList.get(0);
        double max = min;

        for (Double d : this.distributionList) {
            if (d < min)
                min = d;
            else if (d > max)
                max = d;
        }

        return new KeyValue<>(min, max);
    }
}
