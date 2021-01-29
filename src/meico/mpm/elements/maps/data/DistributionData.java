package meico.mpm.elements.maps.data;

import meico.supplementary.KeyValue;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import java.util.ArrayList;

/**
 * this class is used to collect all relevant data to compute imprecision
 * @author Axel Berndt
 */
public class DistributionData {
    public static final String UNIFORM = "distribution.uniform";
    public static final String GAUSSIAN = "distribution.gaussian";
    public static final String TRIANGULAR = "distribution.triangular";
    public static final String BROWNIAN = "distribution.correlated.brownianNoise";
    public static final String COMPENSATING_TRIANGLE = "distribution.correlated.compensatingTriangle";
    public static final String LIST = "distribution.list";

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

    public Double lowerClip = null;
    public Double upperClip = null;

    public Long seed = null;

    public Double millisecondsTimingBasis = null;

    public ArrayList<Double> distributionList = new ArrayList<>();

    /**
     * default constructor
     */
    public DistributionData() {}

    /**
     * constructor from XML element parsing
     * @param xml MPM distribution element
     */
    public DistributionData(Element xml) {
        this.xml = xml;
        this.type = xml.getLocalName();

        Attribute date = xml.getAttribute("date");
        if (date != null)
            this.startDate = Double.parseDouble(date.getValue());

        Attribute id = xml.getAttribute("id", "http://www.w3.org/XML/1998/namespace");
        if (id != null)
            this.xmlId = id.getValue();

        Attribute seed = xml.getAttribute("seed");
        if (seed != null)
            this.seed = Long.parseLong(seed.getValue());

        Attribute lowerLimit = xml.getAttribute("limit.lower");
        if (lowerLimit != null)
            this.lowerLimit = Double.parseDouble(lowerLimit.getValue());

        Attribute upperLimit = xml.getAttribute("limit.upper");
        if (upperLimit != null)
            this.upperLimit = Double.parseDouble(upperLimit.getValue());

        Attribute lowerClip = xml.getAttribute("clip.lower");
        if (lowerClip != null)
            this.lowerClip = Double.parseDouble(lowerClip.getValue());

        Attribute upperClip = xml.getAttribute("clip.upper");
        if (upperClip != null)
            this.upperClip = Double.parseDouble(upperClip.getValue());

        Attribute mode = xml.getAttribute("mode");
        if (mode != null)
            this.mode = Double.parseDouble(mode.getValue());

        Attribute standardDeviation = xml.getAttribute("deviation.standard");
        if (standardDeviation != null)
            this.standardDeviation = Double.parseDouble(standardDeviation.getValue());

        Attribute millisecondsTimingBasis = xml.getAttribute("milliseconds.timingBasis");
        if (millisecondsTimingBasis != null)
            this.millisecondsTimingBasis = Double.parseDouble(millisecondsTimingBasis.getValue());

        Attribute degreeOfCorrelation = xml.getAttribute("degreeOfCorrelation");
        if (degreeOfCorrelation != null)
            this.degreeOfCorrelation = Double.parseDouble(degreeOfCorrelation.getValue());

        Attribute maxStepWidth = xml.getAttribute("stepWidth.max");
        if (maxStepWidth != null)
            this.maxStepWidth = Double.parseDouble(maxStepWidth.getValue());

        Elements measurements = xml.getChildElements("measurement");
        for (Element measurement : measurements) {
            Attribute value = measurement.getAttribute("value");
            if (value != null)
                this.distributionList.add(Double.parseDouble(value.getValue()));
        }
    }

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
        clone.lowerClip = this.lowerClip;
        clone.upperClip = this.upperClip;
        clone.seed = this.seed;
        clone.millisecondsTimingBasis = this.millisecondsTimingBasis;
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
