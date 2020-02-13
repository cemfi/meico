package meico.mpm.elements.maps;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.supplementary.KeyValue;
import meico.mpm.elements.maps.data.DistributionData;
import meico.supplementary.RandomNumberProvider;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * This class interfaces MPM's diverse imprecisionMaps
 * @author Axel Berndt
 */
public class ImprecisionMap extends GenericMap {
    private static final int TIMING         = 1;
    private static final int DYNAMICS       = 2;
    private static final int TONEDURATION   = 3;
    private static final int TUNING         = 4;

    /**
     * constructor, generates an empty imprecisionMap,
     * The application should specify the domain before it can be used!
     * @param domain "timing", "dynamics", "toneduration", "tuning" or anything; even is allowed
     * @throws InvalidDataException
     */
    private ImprecisionMap(String domain) throws InvalidDataException {
        super("imprecisionMap" + ((domain == null) || domain.isEmpty() ? "" : ("." + domain)));
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws InvalidDataException
     */
    private ImprecisionMap(Element xml) throws InvalidDataException {
        super(xml);
    }

    /**
     * ImprecisionMap factory
     * @param domain "timing", "dynamics", "toneduration", "tuning" or anything; even "" is allowed
     * @return
     */
    public static ImprecisionMap createImprecisionMap(String domain) {
        ImprecisionMap d;
        try {
            d = new ImprecisionMap(domain);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * ImprecisionMap factory
     * @param xml
     * @return
     */
    public static ImprecisionMap createImprecisionMap(Element xml) {
        ImprecisionMap d;
        try {
            d = new ImprecisionMap(xml);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }
    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws InvalidDataException {
        super.parseData(xml);

        String localname = this.getXml().getLocalName();
        if (!localname.contains("imprecisionMap"))
            throw new InvalidDataException("Cannot generate ImprecisionMap object. Local name \"" + xml.getLocalName() + "\" must contain the substring \"imprecisionMap\".");

        String[] domain = localname.split(Pattern.quote("."));                                  // the domain is specified after the "." (e.g. imprecisionMap.timing)
        if (domain.length < 2)                                                                  // if there is no "." or nothing the follows it
            System.out.println("Don't forget to specify the domain of the imprecisionMap!");    // print a warning message
   }

    /**
     * set the imprecision domain
     * @param domain "timing", "dynamics", "toneduration", "tuning" or anything; even "" and null are allowed
     */
    public void setDomain(String domain) {
        if ((domain == null) || domain.isEmpty()) {
            this.getXml().setLocalName("imprecisionMap");
            return;
        }

        this.getXml().setLocalName("imprecisionMap" + "." + domain);

        Attribute detuneUnitAtt = this.getXml().getAttribute("detuneUnit");
        if (domain.equals("tuning")) {                                              // in case of a tuning imprecision map make sure that attribute detuneUnit is present
            if (detuneUnitAtt == null)
                this.getXml().addAttribute(new Attribute("detuneUnit", "cents"));
        }
        else if (detuneUnitAtt != null) {                                           // in any other case and if there is a (now meaningless) detuneUnit
            this.getXml().removeAttribute(detuneUnitAtt);                           // remove it
        }
    }

    /**
     * get the domain of this imprecisionMap
     * @return
     */
    public String getDomain() {
        String[] domain = this.getXml().getLocalName().split(Pattern.quote("."));

        if (domain.length < 2)
            return "";

        return domain[1];
    }

    /**
     * for a tuning imprecision map, specify the unit
     * @param unit "cents", "Hertz", "Hz"
     */
    public void setDetuneUnit(String unit) {
        if (unit.equals("Hertz"))
            unit = "Hz";
        this.getXml().addAttribute(new Attribute("detuneUnit", unit));
    }

    /**
     * get the unit of the tuning imprecision map
     * @return the unit or an empty string
     */
    public String getDetuneUnit() {
        if (this.getXml().getAttribute("detuneUnit") == null)
            return "";
        return this.getXml().getAttributeValue("detuneUnit");
    }

    /**
     * add a distribution.uniform element to the map
     * @param date
     * @param lowerLimit
     * @param upperLimit
     * @return the index at which it has been inserted
     */
    public int addDistributionUniform(double date, double lowerLimit, double upperLimit) {
        return this.addDistributionUniform(date, lowerLimit, upperLimit, null);
    }

    /**
     * add a distribution.uniform element to the map
     * @param date
     * @param lowerLimit
     * @param upperLimit
     * @param seed
     * @return the index at which it has been inserted
     */
    public int addDistributionUniform(double date, double lowerLimit, double upperLimit, Long seed) {
        Element e = new Element("distribution.uniform", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("limit.lower", Double.toString(lowerLimit)));
        e.addAttribute(new Attribute("limit.upper", Double.toString(upperLimit)));

        if (seed != null)
            e.addAttribute(new Attribute("seed", Long.toString(seed)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a distribution.gaussian element to the map,
     * this is a gaussian distribution (expectation value for the offset is 0.0; asynchrony should be used to shift it elsewhere)
     * @param date
     * @param standardDeviation
     * @param lowerLimit
     * @param upperLimit
     * @return the index at which it has been inserted
     */
    public int addDistributionGaussian(double date, double standardDeviation, double lowerLimit, double upperLimit) {
        return this.addDistributionGaussian(date, standardDeviation, lowerLimit, upperLimit, null);
    }

    /**
     * add a distribution.gaussian element to the map,
     * this is a gaussian distribution (expectation value for the offset is 0.0; asynchrony should be used to shift it elsewhere)
     * @param date
     * @param standardDeviation
     * @param lowerLimit
     * @param upperLimit
     * @param seed
     * @return the index at which it has been inserted
     */
    public int addDistributionGaussian(double date, double standardDeviation, double lowerLimit, double upperLimit, Long seed) {
        Element e = new Element("distribution.gaussian", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("deviation.standard", Double.toString(standardDeviation)));
        e.addAttribute(new Attribute("limit.lower", Double.toString(lowerLimit)));
        e.addAttribute(new Attribute("limit.upper", Double.toString(upperLimit)));

        if (seed != null)
            e.addAttribute(new Attribute("seed", Long.toString(seed)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a distribution.triangular element to the map,
     * this specifies a triangular distribution with lower and upper limit, the peak (i.e., highest probability) at the mode value
     * @param date
     * @param lowerLimit
     * @param upperLimit
     * @param mode the peak of the trtiangle (i.e., highest probability)
     * @param lowerClip lower clip border
     * @param upperClip upper clip border
     * @return the index at which it has been inserted
     */
    public int addDistributionTriangular(double date, double lowerLimit, double upperLimit, double mode, double lowerClip, double upperClip) {
        return this.addDistributionTriangular(date, lowerLimit, upperLimit, mode, lowerClip, upperClip, null);
    }

    /**
     * add a distribution.triangular element to the map,
     * this specifies a triangular distribution with lower and upper limit, the peak (i.e., highest probability) at the mode value
     * @param date
     * @param lowerLimit
     * @param upperLimit
     * @param mode the peak of the trtiangle (i.e., highest probability)
     * @param lowerClip lower clip border
     * @param upperClip upper clip border
     * @param seed
     * @return the index at which it has been inserted
     */
    public int addDistributionTriangular(double date, double lowerLimit, double upperLimit, double mode, double lowerClip, double upperClip, Long seed) {
        Element e = new Element("distribution.triangular", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("limit.lower", Double.toString(lowerLimit)));
        e.addAttribute(new Attribute("limit.upper", Double.toString(upperLimit)));
        e.addAttribute(new Attribute("mode", Double.toString(mode)));
        e.addAttribute(new Attribute("clip.lower", Double.toString(lowerClip)));
        e.addAttribute(new Attribute("clip.upper", Double.toString(upperClip)));

        if (seed != null)
            e.addAttribute(new Attribute("seed", Long.toString(seed)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a distribution.correlated.brownianNoise element to the map,
     * this represents Brownian noise, the performance renderer generates this by a random walk algorithm
     * @param date
     * @param maxStepWidth the maximum step width in the random walk algorithm
     * @param lowerLimit
     * @param upperLimit
     * @param millisecondsTimingBasis the timing basis (time steps) of the distribution, changing this value will transpose the distribution
     * @return the index at which it has been inserted
     */
    public int addDistributionBrownianNoise(double date, double maxStepWidth, double lowerLimit, double upperLimit, double millisecondsTimingBasis) {
        return this.addDistributionBrownianNoise(date, maxStepWidth, lowerLimit, upperLimit, millisecondsTimingBasis, null);
    }

    /**
     * add a distribution.correlated.brownianNoise element to the map,
     * this represents Brownian noise, the performance renderer generates this by a random walk algorithm
     * @param date
     * @param maxStepWidth the maximum step width in the random walk algorithm
     * @param lowerLimit
     * @param upperLimit
     * @param millisecondsTimingBasis the timing basis (time steps) of the distribution, changing this value will transpose the distribution
     * @param seed
     * @return the index at which it has been inserted
     */
    public int addDistributionBrownianNoise(double date, double maxStepWidth, double lowerLimit, double upperLimit, double millisecondsTimingBasis, Long seed) {
        Element e = new Element("distribution.correlated.brownianNoise", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("stepWidth.max", Double.toString(maxStepWidth)));
        e.addAttribute(new Attribute("limit.lower", Double.toString(lowerLimit)));
        e.addAttribute(new Attribute("limit.upper", Double.toString(upperLimit)));
        e.addAttribute(new Attribute("milliseconds.timingBasis", Double.toString(millisecondsTimingBasis)));

        if (seed != null)
            e.addAttribute(new Attribute("seed", Long.toString(seed)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a distribution.correlated.compensatingTriangle element to the map,
     * in this variant of the triangular distribution the mode (triangle peak) is wandering with the random values,
     * this method is an alternative to brownianNoise and is experimental
     * @param date
     * @param degreeOfCorrelation Must be >= 0.0. To avoid outliers (beyond the lower and upper limit) this value should be >= 1.0. 1.0 keeps the triangle's left and right edge at the lower and upper limit. The greater this value, the narrower is the triangle while wandering around between the limits.
     * @param lowerLimit
     * @param upperLimit
     * @param lowerClip lower clip border
     * @param upperClip upper clip border
     * @param millisecondsTimingBasis the timing basis (time steps) of the distribution, changing this value will transpose the distribution
     * @return the index at which it has been inserted
     */
    public int addDistributionCompensatingTriangle(double date, double degreeOfCorrelation, double lowerLimit, double upperLimit, double lowerClip, double upperClip, double millisecondsTimingBasis) {
        return this.addDistributionCompensatingTriangle(date, degreeOfCorrelation, lowerLimit, upperLimit, lowerClip, upperClip, millisecondsTimingBasis, null);
    }

    /**
     * add a distribution.correlated.compensatingTriangle element to the map,
     * in this variant of the triangular distribution the mode (triangle peak) is wandering with the random values,
     * this method is an alternative to brownianNoise and is experimental
     * @param date
     * @param degreeOfCorrelation Must be >= 0.0. To avoid outliers (beyond the lower and upper limit) this value should be >= 1.0. 1.0 keeps the triangle's left and right edge at the lower and upper limit. The greater this value, the narrower is the triangle while wandering around between the limits.
     * @param lowerLimit
     * @param upperLimit
     * @param lowerClip lower clip border
     * @param upperClip upper clip border
     * @param millisecondsTimingBasis the timing basis (time steps) of the distribution, changing this value will transpose the distribution
     * @param seed
     * @return the index at which it has been inserted
     */
    public int addDistributionCompensatingTriangle(double date, double degreeOfCorrelation, double lowerLimit, double upperLimit, double lowerClip, double upperClip, double millisecondsTimingBasis, Long seed) {
        Element e = new Element("distribution.correlated.compensatingTriangle", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("degreeOfCorrelation", Double.toString((Math.max(degreeOfCorrelation, 0.0)))));
        e.addAttribute(new Attribute("limit.lower", Double.toString(lowerLimit)));
        e.addAttribute(new Attribute("limit.upper", Double.toString(upperLimit)));
        e.addAttribute(new Attribute("clip.lower", Double.toString(lowerClip)));
        e.addAttribute(new Attribute("clip.upper", Double.toString(upperClip)));
        e.addAttribute(new Attribute("milliseconds.timingBasis", Double.toString(millisecondsTimingBasis)));

        if (seed != null)
            e.addAttribute(new Attribute("seed", Long.toString(seed)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a distribution.list element to the map, it should already contain all its measurement children,
     * this is a table of values of deviations from 0.0 (perfect value),
     * the values may have been obtained by measurements of human musicians' performances
     * @param date
     * @param list
     * @param millisecondsTimingBasis the timing basis (time steps) of the distribution, changing this value will transpose the distribution
     * @return the index at which it has been inserted
     */
    public int addDistributionList(double date, Element list, double millisecondsTimingBasis) {
        list.addAttribute(new Attribute("date", Double.toString(date)));
        list.addAttribute(new Attribute("milliseconds.timingBasis", Double.toString(millisecondsTimingBasis)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, list);
        return this.insertElement(kv, false);
    }

    /**
     * add a distribution element to the imprecisionMap
     * @param data xml data will be ignored (use addElement() instead to add an xml representation)
     * @return
     */
    public int addDistribution(DistributionData data) {
        if (data.type.isEmpty()) {
            System.err.println("Cannot add distribution, type not specified.");
            return -1;
        }

        Element e = new Element(data.type, Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(data.startDate)));

        if (data.xmlId != null)
            e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", data.xmlId));

        switch (data.type) {
            case "distribution.uniform":
                if (data.lowerLimit == null) {
                    System.err.println("Cannot add distribution, lowerLimit not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("limit.lower", Double.toString(data.lowerLimit)));
                if (data.upperLimit == null) {
                    System.err.println("Cannot add distribution, upperLimit not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("limit.upper", Double.toString(data.upperLimit)));
                if (data.seed != null)
                    e.addAttribute(new Attribute("seed", Long.toString(data.seed)));
                break;
            case "distribution.gaussian":
                if (data.standardDeviation == null) {
                    System.err.println("Cannot add distribution, standardDeviation not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("deviation.standard", Double.toString(data.standardDeviation)));
                if (data.lowerLimit == null) {
                    System.err.println("Cannot add distribution, lowerLimit not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("limit.lower", Double.toString(data.lowerLimit)));
                if (data.upperLimit == null) {
                    System.err.println("Cannot add distribution, upperLimit not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("limit.upper", Double.toString(data.upperLimit)));
                if (data.seed != null)
                    e.addAttribute(new Attribute("seed", Long.toString(data.seed)));
                break;
            case "distribution.triangular":
                if (data.lowerLimit == null) {
                    System.err.println("Cannot add distribution, lowerLimit not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("limit.lower", Double.toString(data.lowerLimit)));
                if (data.upperLimit == null) {
                    System.err.println("Cannot add distribution, upperLimit not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("limit.upper", Double.toString(data.upperLimit)));
                if (data.mode == null) {
                    System.err.println("Cannot add distribution, mode not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("mode", Double.toString(data.mode)));
                if (data.lowerClip == null) {
                    System.err.println("Cannot add distribution, lowerClip not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("clip.lower", Double.toString(data.lowerClip)));
                if (data.upperClip == null) {
                    System.err.println("Cannot add distribution, upperClip not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("clip.upper", Double.toString(data.upperClip)));
                if (data.seed != null)
                    e.addAttribute(new Attribute("seed", Long.toString(data.seed)));
                break;
            case "distribution.correlated.brownianNoise":
                if (data.maxStepWidth == null) {
                    System.err.println("Cannot add distribution, maxStepWidth not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("stepWidth.max", Double.toString(data.maxStepWidth)));
                if (data.lowerLimit == null) {
                    System.err.println("Cannot add distribution, lowerLimit not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("limit.lower", Double.toString(data.lowerLimit)));
                if (data.upperLimit == null) {
                    System.err.println("Cannot add distribution, upperLimit not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("limit.upper", Double.toString(data.upperLimit)));
                e.addAttribute(new Attribute("milliseconds.timingBasis", Double.toString(data.millisecondsTimingBasis)));
                if (data.seed != null)
                    e.addAttribute(new Attribute("seed", Long.toString(data.seed)));
                break;
            case "distribution.correlated.compensatingTriangle":
                if (data.lowerLimit == null) {
                    System.err.println("Cannot add distribution, lowerLimit not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("limit.lower", Double.toString(data.lowerLimit)));
                if (data.upperLimit == null) {
                    System.err.println("Cannot add distribution, upperLimit not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("limit.upper", Double.toString(data.upperLimit)));
                if (data.degreeOfCorrelation == null) {
                    System.err.println("Cannot add distribution, degreeOfCorrelation not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("degreeOfCorrelation", Double.toString((Math.max(data.degreeOfCorrelation, 0.0)))));
                if (data.lowerClip == null) {
                    System.err.println("Cannot add distribution, lowerClip not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("clip.lower", Double.toString(data.lowerClip)));
                if (data.upperClip == null) {
                    System.err.println("Cannot add distribution, upperClip not specified.");
                    return -1;
                }
                e.addAttribute(new Attribute("clip.upper", Double.toString(data.upperClip)));
                e.addAttribute(new Attribute("milliseconds.timingBasis", Double.toString(data.millisecondsTimingBasis)));
                if (data.seed != null)
                    e.addAttribute(new Attribute("seed", Long.toString(data.seed)));
                break;
            case "distribution.list":
                for (int i=0; i < data.distributionList.size(); ++i) {
                    Element m = new Element("measurement");
                    m.addAttribute(new Attribute("value", Double.toString(data.distributionList.get(i))));
                    e.appendChild(m);
                }
                e.addAttribute(new Attribute("milliseconds.timingBasis", Double.toString(data.millisecondsTimingBasis)));
                break;
            default:
                System.err.println("Cannot add distribution, unknown distribution type.");
                return -1;
        }
        KeyValue<Double, Element> kv = new KeyValue<>(data.startDate, e);
        return this.insertElement(kv, false);
    }

    /**
     * collect all data that is needed to compute the distribution/offset at the specified date
     * @param date
     * @return
     */
    private DistributionData getDistributionDataAt(double date) {
        for (int i = this.getElementIndexBeforeAt(date); i >= 0; --i) {
            DistributionData dd = this.getDistributionDataOf(i);
            if (dd != null)
                return dd;
        }
        return null;
    }

    /**
     * collect all distribution data of the index-specified map element
     * @param index
     * @return
     */
    private DistributionData getDistributionDataOf(int index) {
        if (this.elements.isEmpty() || (index < 0))
            return null;

        if (index >= this.elements.size())
            index = this.elements.size() - 1;

        Element e = this.getElement(index);
        if (e.getLocalName().startsWith("distribution.")) {
            DistributionData dd = new DistributionData();
            dd.startDate = this.elements.get(index).getKey();
            dd.endDate = (index < (this.size() - 1)) ? this.elements.get(index + 1).getKey() : Double.MAX_VALUE;    // get the date of the subsequent imprecision element
            dd.type = e.getLocalName();
            dd.xml = e;

            Attribute att = Helper.getAttribute("xml:id", e);
            if (att != null)
                dd.xmlId = att.getValue();

            att = Helper.getAttribute("deviation.standard", e);
            if (att != null)
                dd.standardDeviation = Double.parseDouble(att.getValue());

            att = Helper.getAttribute("stepWidth.max", e);
            if (att != null)
                dd.maxStepWidth = Double.parseDouble(att.getValue());

            att = Helper.getAttribute("degreeOfCorrelation", e);
            if (att != null)
                dd.degreeOfCorrelation = Double.parseDouble(att.getValue());

            att = Helper.getAttribute("mode", e);
            if (att != null)
                dd.mode = Double.parseDouble(att.getValue());

            att = Helper.getAttribute("limit.upper", e);
            if (att != null)
                dd.upperLimit = Double.parseDouble(att.getValue());

            att = Helper.getAttribute("limit.lower", e);
            if (att != null)
                dd.lowerLimit = Double.parseDouble(att.getValue());

            att = Helper.getAttribute("clip.lower", e);
            if (att != null)
                dd.lowerClip = Double.parseDouble(att.getValue());

            att = Helper.getAttribute("clip.upper", e);
            if (att != null)
                dd.upperClip = Double.parseDouble(att.getValue());

            att = Helper.getAttribute("seed", e);
            if (att != null)
                dd.seed = Long.parseLong(att.getValue());

            att = Helper.getAttribute("milliseconds.timingBasis", e);
            if (att != null)
                dd.millisecondsTimingBasis = Double.parseDouble(att.getValue());

            Elements measurements = e.getChildElements("measurement");
            for (int j = 0; j < measurements.size(); ++j) {
                Element m = measurements.get(j);
                att = Helper.getAttribute("value", m);
                if (att != null) {
                    dd.distributionList.add(Double.parseDouble(att.getValue()));
                }
            }
            return dd;
        }
        return null;
    }

    /**
     * On the basis of the specified imprecisionMap, apply the corresponding transformations to all elements of the specified map.
     * For correlated distributions, fhis method includes a handover between subsequent imprecision elements, i.e. the final value of the previous becomes the first of the next.
     * @param map
     * @param shakePolyphonicPart If this map/MSM score is polyphonic all voices would perform the exact same imprecision. By setting this flag true, this is shaken up a little bit.
     */
    public void renderImprecisionToMap(GenericMap map, boolean shakePolyphonicPart) {
        if ((map == null) || this.elements.isEmpty())
            return;

        int domain;
        switch (this.getDomain()) {
            case "timing":
                domain = ImprecisionMap.TIMING;
                break;
            case "dynamics":
                domain = ImprecisionMap.DYNAMICS;
                break;
            case "toneduration":
                domain = ImprecisionMap.TONEDURATION;
                break;
            case "tuning":
                domain = ImprecisionMap.TUNING;
                break;
            default:                // unknown or unimplemented domain of the imprecisionMap
                return;             // we do not know where to apply the distribution data, hence, we are done
        }

        ArrayList<KeyValue<Double[], Attribute>> pendingDurations = new ArrayList<>();
        HashMap<Double, ArrayList<KeyValue<Double, Attribute>>> offsets = new HashMap<>();  // all imprecision offsets go in here (msDate, list(offset, attribute))
        int mapIndex = 0;
        DistributionData dd = null;
        RandomNumberProvider random = null;
        for (int impIndex = 0; impIndex < this.size(); ++impIndex) {
            DistributionData ddPrev = dd;                   // keep a reference to the previous distribution data

            dd = this.getDistributionDataOf(impIndex);
            if (dd == null) {
                dd = ddPrev;
                continue;
            }

            // initialize the seed, generate correlated distribution functions
            switch (dd.type) {
                case "distribution.uniform":
                    random = RandomNumberProvider.createRandomNumberProvider_uniformDistribution(dd.lowerLimit, dd.upperLimit);
                    break;
                case "distribution.gaussian":
                    random = RandomNumberProvider.createRandomNumberProvider_gaussianDistribution(dd.standardDeviation, dd.lowerLimit, dd.upperLimit);
                    break;
                case "distribution.triangular":
                    random = RandomNumberProvider.createRandomNumberProvider_triangularDistribution(dd.lowerLimit, dd.upperLimit, dd.mode, dd.lowerClip, dd.upperClip);
                    break;
                case "distribution.correlated.brownianNoise": {
                        Double imprecisionValueHandover = ImprecisionMap.getHandoverValue(random, ddPrev, dd);    // before we go on with this distribution element we need to provide a handover value from the previous
                        random = RandomNumberProvider.createRandomNumberProvider_brownianNoiseDistribution(dd.maxStepWidth, dd.lowerLimit, dd.upperLimit);
                        ImprecisionMap.doHandover(imprecisionValueHandover, random);    // let this imprecision element start where the previous ended
                    }
                    break;
                case "distribution.correlated.compensatingTriangle": {
                        Double imprecisionValueHandover = ImprecisionMap.getHandoverValue(random, ddPrev, dd);    // before we go on with this distribution element we need to provide a handover value from the previous
                        random = RandomNumberProvider.createRandomNumberProvider_compensatingTriangleDistribution(dd.degreeOfCorrelation, dd.lowerLimit, dd.upperLimit, dd.lowerClip, dd.upperClip);
                        ImprecisionMap.doHandover(imprecisionValueHandover, random);    // let this imprecision element start where the previous ended
                    }
                    break;
                case "distribution.list":
                    random = RandomNumberProvider.createRandomNumberProvider_distributionList(dd.distributionList);
                    break;
                default:                                                            // unknown or unimplemented distribution
                    continue;                                                       // continue with the next
            }

            if (dd.seed != null)            // if a specific seed has been defined
                random.setSeed(dd.seed);    // set it

            // make sure that the timing resolution is specified, and if not, compute a reasonable value
            if (dd.millisecondsTimingBasis == null) {
                // if we are in the timing domain we have to set the timing resolution so that permutation of subsequent events is avoided
                if (domain == ImprecisionMap.TIMING) {
                    switch (dd.type) {
                        case "distribution.uniform":
                        case "distribution.gaussian":
                        case "distribution.correlated.brownianNoise":
                            dd.millisecondsTimingBasis = dd.upperLimit - dd.lowerLimit;
                            break;
                        case "distribution.triangular":
                        case "distribution.correlated.compensatingTriangle":
                            dd.millisecondsTimingBasis = dd.upperClip - dd.lowerClip;
                            break;
                        case "distribution.list":
                            KeyValue<Double, Double> minMax = dd.getMinAndMaxValueInDistributionList();
                            if (minMax != null)
                                dd.millisecondsTimingBasis = minMax.getValue() - minMax.getKey();
                            break;
                        default:
                            break;
                    }
                }
                // if the timing resolution is still null or invalid, set a default value
                if ((dd.millisecondsTimingBasis == null) || (dd.millisecondsTimingBasis <= 0.0))
                    dd.millisecondsTimingBasis = 100.0;                             // The human brain has a timing grid of approx. 300ms to react and correct etc. Hopwever, motor variances may occur on affect every note individually. Hence, we set the default timing resolution to this compromise value.
            }

            // apply distribution to map elements
            for (; mapIndex < map.size(); ++mapIndex) {                             // traverse the map elements
                KeyValue<Double, Element> mapEntry = map.elements.get(mapIndex);    // get the current map entry

                if (mapEntry.getKey() < dd.startDate)                               // if this map entry is before the current distribution element
                    continue;                                                       // go on until we are at of after the distribution element's date

                if (mapEntry.getKey() >= dd.endDate)                                // if the current map element is out of the scope of the current distribution element
                    break;                                                          // stop here and find the next distribution element first before continuing

                Attribute msDateAtt = Helper.getAttribute("milliseconds.date", mapEntry.getValue());
                if (msDateAtt == null)                                              // imprecisions are always milliseconds based, hence the map entry must have attribute milliseconds.date
                    continue;                                                       // no milliseconds date, no imprecision, go on with the next map entry

                double msDate, index;
                KeyValue<Double, Attribute> offset;                                 // a tuplet with the offset value and the attribute to add it to

                // compute and apply next imprecision value to the map element's attributes
                switch (domain) {
                    case ImprecisionMap.TIMING: {
                            msDate = Double.parseDouble(msDateAtt.getValue());
                            index = msDate / dd.millisecondsTimingBasis;
                            offset = new KeyValue<>(random.getValue(index), msDateAtt);

                            // same is necessary for milliseconds.date.end
                            Attribute msEndAtt = Helper.getAttribute("milliseconds.date.end", mapEntry.getValue());
                            if (msEndAtt != null) {
                                double msDateEnd = Double.parseDouble(msEndAtt.getValue());
                                pendingDurations.add(new KeyValue<>(new Double[]{Double.parseDouble(Helper.getAttributeValue("milliseconds.date.end", mapEntry.getValue())), msDateEnd}, msEndAtt)); // this can be outside of the scope of the current distribution element, in that case the computation should be done by a later one
                            }
                        }
                        break;
                    case ImprecisionMap.TONEDURATION: { // this is potentially not under the current distribution element, however, its tick date idicates the notes to be affected, not the date.end
                            Attribute msEndAtt = Helper.getAttribute("milliseconds.date.end", mapEntry.getValue());
                            if (msEndAtt != null) {
                                msDate = Double.parseDouble(msEndAtt.getValue());
                                index = msDate / dd.millisecondsTimingBasis;
                                offset = new KeyValue<>(random.getValue(index), msEndAtt);
                            } else
                                continue;
                        }
                        break;
                    case ImprecisionMap.DYNAMICS:
                        Attribute velAtt = Helper.getAttribute("velocity", mapEntry.getValue());
                        if (velAtt == null)
                            continue;
                        msDate = Double.parseDouble(msDateAtt.getValue());
                        index = msDate / dd.millisecondsTimingBasis;
                        offset = new KeyValue<>(random.getValue(index), velAtt);
                        break;
                    case ImprecisionMap.TUNING:
                        msDate = Double.parseDouble(msDateAtt.getValue());
                        index = msDate / dd.millisecondsTimingBasis;
                        Attribute tuneAtt = Helper.getAttribute("tuning.offset", mapEntry.getValue());
                        if (tuneAtt == null) {
                            tuneAtt = new Attribute("tuning.offset", "0.0");
                            mapEntry.getValue().addAttribute(tuneAtt);
                        }
                        offset = new KeyValue<>(random.getValue(index), tuneAtt);
                        break;
                    default:
                        continue;
                }

                // add the offset and attribute link to the list for further reference
                ImprecisionMap.addToOffsetsMap(offsets, msDate, offset);
            }

            // offset the milliseconds.date.end attributes
            for (int i=0; i < pendingDurations.size(); ++i) {
                KeyValue<Double[], Attribute> pd = pendingDurations.get(i);
                double dateEnd = pd.getKey()[0];

                if (dateEnd >= dd.endDate)      // check whether date.end falls into the scope of this distribution element
                    break;

                double msDate = pd.getKey()[1];
                double endIndex = (msDate / dd.millisecondsTimingBasis);
                KeyValue<Double, Attribute> offset = new KeyValue<>(random.getValue(endIndex), pd.getValue());
                ImprecisionMap.addToOffsetsMap(offsets, msDate, offset);    // add the offset and attribute link to the list for further reference

                pendingDurations.remove(pd);
                --i;
            }
        }

        if (shakePolyphonicPart) {
            if (domain == ImprecisionMap.TIMING)
                ImprecisionMap.shakeTimingOffsets(offsets); // shake the offsets
            else
                ImprecisionMap.shakeOffsets(offsets);       // shake the offsets
        }

        ImprecisionMap.addOffsetsToAttributes(offsets);     // add offsets to corresponding attributes
    }

    /**
     * on the basis of the specified imprecisionMap, apply the corresponding transformations to all elements of the specified map
     * @param map
     * @param imprecisionMap
     * @param shakePolyphonicPart If this map/MSM score is polyphonic all voices would perform the exact same imprecision. By setting this flag true, this is shaken up a little bit.
     */
    public static void renderImprecisionToMap(GenericMap map, ImprecisionMap imprecisionMap, boolean shakePolyphonicPart) {
        if (imprecisionMap != null)
            imprecisionMap.renderImprecisionToMap(map, shakePolyphonicPart);
    }

    /**
     * a helper method for adding offsets to a hashmap of offsets
     * @param offsetsMap
     * @param millisecondsDate
     * @param offsetAndAttribute
     */
    private static void addToOffsetsMap(HashMap<Double, ArrayList<KeyValue<Double, Attribute>>> offsetsMap, double millisecondsDate, KeyValue<Double, Attribute> offsetAndAttribute) {
        ArrayList<KeyValue<Double, Attribute>> list = offsetsMap.get(millisecondsDate);
        if (list == null) {                         // no offsets on that date so far
            list = new ArrayList<>();               // create a list
            list.add(offsetAndAttribute);           // add the current offset to the list
            offsetsMap.put(millisecondsDate, list); // add the list under the current date to the hashmap
        } else {                                    // there are already other offsets on that date
            list.add(offsetAndAttribute);           // add this to the list
        }
    }

    /**
     * a helper method to get the handover value
     * @param randomPrev the random number provider that hands over its last value to the next
     * @param ddPrev the distribution element from which the handover should be done
     * @param ddNext the distribution element to which the handover should be done
     * @return
     */
    private static Double getHandoverValue(RandomNumberProvider randomPrev, DistributionData ddPrev, DistributionData ddNext) {
        if ((ddPrev == null) || (randomPrev == null))
            return null;

        Attribute ddMsDateEndAtt = Helper.getAttribute("milliseconds.date", ddNext.xml);
        if (ddMsDateEndAtt == null)
            return null;

        Double ddMsDateEnd = Double.parseDouble(ddMsDateEndAtt.getValue());
        double endIndex = ddMsDateEnd / ddPrev.millisecondsTimingBasis;
        return randomPrev.getValue(endIndex);
    }

    /**
     * The first value in the random number series of a correlated distribution should be initialized at the last value of the preceding distribution element.
     * If that value is null (because there is no preceding distribution or ...) the first value is set to a random value within a restricted range, i.e. half of the lower and upper limit, as we do not want the imprecision to start with extreme values.
     * This method will cause the RandomNumberProvider to create a totally new series of random numbers; hence, use it only at the beginning before you start working with the values!
     * @param value the last value of the preceding distribution element, or null
     * @param random the RandomNuberProvider to be initialized with the specified value
     */
    private static void doHandover(Double value, RandomNumberProvider random) {
        if (value != null)
            random.setInitialValue(value);
        else {
            double scaleFactor = (random.getUpperLimit() - random.getLowerLimit()) * 0.5;     // the initial value should not be at the extremes, thus we limit the range of the initial value by 0.5
            double firstValue = (Math.random() * scaleFactor) + random.getLowerLimit() + (scaleFactor * 0.5);
            random.setInitialValue(firstValue);
        }
    }

    /**
     * This seeks elements in the specified offsets hashmap with the same milliseconds.date(.end) and shakes their imprecision offsets.
     * Only one randomly chosen element for each date keeps its original offset.
     * @param offsets
     */
    private static void shakeOffsets(HashMap<Double, ArrayList<KeyValue<Double, Attribute>>> offsets) {
        for (Map.Entry<Double, ArrayList<KeyValue<Double, Attribute>>> entries : offsets.entrySet()) {
            if (entries.getValue().size() < 2)                                      // if there is only one element at the date
                continue;                                                           // no need to do anything

            int keepOffset = (new Random()).nextInt(entries.getValue().size());     // choose randomly which element should keep the orioginal offset

            // use trianglular distributions to shift the offsets
            for (int i = 0; i < entries.getValue().size(); ++i) {
                if (i == keepOffset)                                                // if this element should keep the original offset
                    continue;                                                       // leave it unaltered

                KeyValue<Double, Attribute> entry = entries.getValue().get(i);
                entry.setKey(ImprecisionMap.shake(entries.getValue().get(i).getKey())); // shake entry.getKey()
            }
        }
    }

    /**
     * When shaking timing offsets we have to take care of collisions,
     * i.e., noteOn and noteOff events with the same pitch and at the same milliseconds date should not be shifted apart.
     * Hence, the timing shaking is a bit extended compared to the usual shakeOffsets() method.
     * @param offsets
     */
    private static void shakeTimingOffsets(HashMap<Double, ArrayList<KeyValue<Double, Attribute>>> offsets) {
        for (Map.Entry<Double, ArrayList<KeyValue<Double, Attribute>>> entries : offsets.entrySet()) {
            if (entries.getValue().size() < 2)                                      // if there is only one element at the date
                continue;                                                           // no need to do anything

            int keepOffset = (new Random()).nextInt(entries.getValue().size());     // choose randomly which element should keep the orioginal offset

            HashMap<Double, Double> pitchOffsetTuplet = new HashMap<>();            // events with the same pitch should get the same offset

            // as this applies also to the element that keeps its offset, it should be added to the hashmap first
            KeyValue<Double, Attribute> keeper = entries.getValue().get(keepOffset);
            Attribute pitchAtt = Helper.getAttribute("midi.pitch", (Element) keeper.getValue().getParent());
            if (pitchAtt != null) {
                Double pitch = Double.parseDouble(pitchAtt.getValue());
                pitchOffsetTuplet.put(pitch, keeper.getKey());
            }

            // use trianglular distributions to shift the offsets
            for (int i = 0; i < entries.getValue().size(); ++i) {
                if (i == keepOffset)                                                // if this element should keep the original offset
                    continue;                                                       // leave it unaltered

                KeyValue<Double, Attribute> entry = entries.getValue().get(i);

                // check whether we have already an offset value for this pitch
                pitchAtt = Helper.getAttribute("midi.pitch", (Element) entry.getValue().getParent());;
                if (pitchAtt != null) {
                    Double pitch = Double.parseDouble(pitchAtt.getValue());
                    Double offset = pitchOffsetTuplet.get(pitch);
                    if (offset != null) {
                        entry.setKey(offset);
                        continue;
                    }
                }

                entry.setKey(ImprecisionMap.shake(entry.getKey()));     // shake entry.getKey()

                // add this (pitch, offset) tuplet to the hashmap
                if (pitchAtt != null) {
                    Double pitch = Double.parseDouble(pitchAtt.getValue());
                    pitchOffsetTuplet.put(pitch, entry.getKey());
                }
            }
        }
    }

    /**
     * A helper method for the shaking mechanisms in methods shakeOffsets() and shakeTimingOffsets().
     * The input offset is reduced by a random amount via triangular distribution. But we keep the direction of the offset.
     * Furthermore, the maximum amount of reduction is limited to half of the offset.
     * So the parameters of the triangular distribution are: (limits are offset and offset/2, mode = offset).
     * @param offset
     * @return
     */
    private static double shake(double offset) {
        double of = offset * 0.5; //0.0;        // the shifted offset is allowed to be half less of the original offset, but not inverse and certainly not more since this could break the limits
        if (offset < 0.0)
            return RandomNumberProvider.createRandomNumberProvider_triangularDistribution(offset, of, of, offset, of).getValue(0);
        return RandomNumberProvider.createRandomNumberProvider_triangularDistribution(of, offset, offset, of, offset).getValue(0);
    }

    /**
     * While method renderImprecisionToMap() computes the imprecision offsets, this method adds the values to the corresponding attributes.
     * This method is called at the end of renderImprecisionToMap().
     * @param offsets
     */
    private static void addOffsetsToAttributes(HashMap<Double, ArrayList<KeyValue<Double, Attribute>>> offsets) {
        for (Map.Entry<Double, ArrayList<KeyValue<Double, Attribute>>> entries : offsets.entrySet()) {
            for (KeyValue<Double, Attribute> entry : entries.getValue()) {
                double attValue = Double.parseDouble(entry.getValue().getValue());
                entry.getValue().setValue(Double.toString((attValue + entry.getKey())));
            }
        }
    }
}
