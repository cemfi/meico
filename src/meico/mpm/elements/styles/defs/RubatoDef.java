package meico.mpm.elements.styles.defs;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.maps.RubatoMap;
import meico.supplementary.KeyValue;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This class interfaces MPM rubatoDef elements.
 * @author Axel Berndt
 */
public class RubatoDef extends AbstractDef {
    // the attribute values of the rubatoDef
    private double frameLength = 0.0;
    private double intensity = 1.0;
    private double lateStart = 0.0;
    private double earlyEnd = 1.0;

    /**
     * constructor, creates an empty rubatoDef
     * @param name
     * @throws InvalidDataException
     */
    private RubatoDef(String name) throws InvalidDataException {
        Element e = new Element("rubatoDef", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("name", name));
        this.parseData(e);
    }

    /**
     * constructor to create a RubatoDef instance from the values
     * @param name
     * @param frameLength
     * @param intensity
     * @param lateStart
     * @param earlyEnd
     * @throws InvalidDataException
     */
    private RubatoDef(String name, double frameLength, double intensity, double lateStart, double earlyEnd) throws InvalidDataException {
        Element e = new Element("rubatoDef", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("name", name));
        e.addAttribute(new Attribute("frameLength", "" + frameLength));
        e.addAttribute(new Attribute("intensity", "" + intensity));
        e.addAttribute(new Attribute("lateStart", "" + lateStart));
        e.addAttribute(new Attribute("earlyEnd", "" + earlyEnd));
        this.parseData(e);
    }

    /**
     * contructor to create a RubatoDef instance from the xml
     * @param xml
     * @throws InvalidDataException
     */
    private RubatoDef(Element xml) throws InvalidDataException {
        this.parseData(xml);
    }

    /**
     * RubatoDef factory
     * @param name
     * @return
     */
    public static RubatoDef createRubatoDef(String name) {
        RubatoDef rubatoDef;
        try {
            rubatoDef = new RubatoDef(name);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return rubatoDef;
    }

    /**
     * RubatoDef factory
     * @param name
     * @param frameLength
     * @param intensity
     * @param lateStart
     * @param earlyEnd
     * @return
     */
    public static RubatoDef createRubatoDef(String name, double frameLength, double intensity, double lateStart, double earlyEnd) {
        RubatoDef rubatoDef;
        try {
            rubatoDef = new RubatoDef(name, frameLength, intensity, lateStart, earlyEnd);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return rubatoDef;
    }

    /**
     * RubatoDef factory
     * @param xml
     * @return
     */
    public static RubatoDef createRubatoDef(Element xml) {
        RubatoDef rubatoDef;
        try {
            rubatoDef = new RubatoDef(xml);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return rubatoDef;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    @Override
    protected void parseData(Element xml) throws InvalidDataException {
        if (xml == null)
            throw new InvalidDataException("Cannot generate RubatoDef object. XML Element is null.");

        this.name = Helper.getAttribute("name", xml);
        if (this.name == null)
            throw new InvalidDataException("Cannot generate RubatoDef object. Missing name attribute.");

        this.setXml(xml);

        // make sure that this element is really a "rubatoDef" element
        if (!this.getXml().getLocalName().equals("rubatoDef")) {
            this.getXml().setLocalName("rubatoDef");
        }

        Attribute frameLength = Helper.getAttribute("frameLength", this.getXml());  // the frameLength attribute is also mandatory
        if (frameLength == null)                                            // if it is missing
            throw new InvalidDataException("Cannot generate RubatoDef object. Missing attribute frameLength.");

        Attribute intensity = Helper.getAttribute("intensity", this.getXml());      // get the intensity attribute
        if (intensity == null) {                                            // if missing
            intensity = new Attribute("intensity", "" + this.intensity);    // generate a default one
            this.getXml().addAttribute(intensity);
        } else {
            intensity.setValue("" + RubatoDef.ensureIntensityBoundaries(Double.parseDouble(intensity.getValue())));
        }

        Attribute lateStart = Helper.getAttribute("lateStart", this.getXml());      // get lateStart attribute
        if (lateStart == null) {                                            // if missing
            lateStart = new Attribute("lateStart", "" + this.lateStart);    // generate a default one
            this.getXml().addAttribute(lateStart);
        }
        Attribute earlyEnd = Helper.getAttribute("earlyEnd", this.getXml());        // get earlyEnd attribute
        if (earlyEnd == null) {                                             // if missing
            earlyEnd = new Attribute("earlyEnd", "" + this.earlyEnd);       // generate a default one
            this.getXml().addAttribute(earlyEnd);
        }
        KeyValue<Double, Double> le = RubatoDef.ensureLateStartEarlyEndBoundaries(Double.parseDouble(lateStart.getValue()), Double.parseDouble(earlyEnd.getValue()));
        lateStart.setValue("" + le.getKey());
        earlyEnd.setValue("" + le.getValue());

        // set the values
        this.frameLength = Double.parseDouble(frameLength.getValue());
        this.intensity = Double.parseDouble(intensity.getValue());
        this.lateStart = le.getKey();
        this.earlyEnd = le.getValue();
    }

    /**
     * access the frameLength attribute
     * @return
     */
    public double getFrameLength() {
        return frameLength;
    }

    /**
     * set the frameLength attribute
     * @param frameLength
     */
    public void setFrameLength(double frameLength) {
        this.frameLength = Math.max(frameLength, 0.0);
        this.getXml().getAttribute("frameLength").setValue("" + this.frameLength);
    }

    /**
     * access intensity attribute
     * @return
     */
    public double getIntensity() {
        return intensity;
    }

    /**
     * set the intensity attribute
     * @param intensity
     */
    public void setIntensity(double intensity) {
        this.intensity = RubatoDef.ensureIntensityBoundaries(intensity);
        this.getXml().getAttribute("intensity").setValue(Double.toString(this.intensity));
    }

    /**
     * access lateStart attribute
     * @return
     */
    public double getLateStart() {
        return lateStart;
    }

    /**
     * set lateStart attribute
     * @param lateStart
     */
    public void setLateStart(double lateStart) {
        if (lateStart >= this.earlyEnd) {                // if lateStart is greater than earlyEnd
            System.err.println("Setting lateStart >= earlyEnd is not allowed.");
            return;
        }
        if (lateStart < 0.0) {
            System.err.println("Invalid rubato lateStart < 0.0 is set to 0.0.");
            lateStart = 0.0;
        }
        this.lateStart = lateStart;
        this.getXml().getAttribute("lateStart").setValue(Double.toString(this.lateStart));
    }

    /**
     * access earlyEnd attribute
     * @return
     */
    public double getEarlyEnd() {
        return earlyEnd;
    }

    /**
     * set earlyEnd attribute
     * @param earlyEnd
     */
    public void setEarlyEnd(double earlyEnd) {
        if (this.lateStart >= earlyEnd) {                // if lateStart is greater than earlyEnd
            System.err.println("Setting earlyEnd <= lateStart is not allowed.");
            return;
        }
        if (earlyEnd > 1.0) {
            System.err.println("Invalid rubato earlyEnd > 1.0 is set to 1.0.");
            earlyEnd = 1.0;
        }
        this.earlyEnd = earlyEnd;
        this.getXml().getAttribute("earlyEnd").setValue(Double.toString(this.earlyEnd));
    }

    /**
     * set lateStart and earlyEnd at once
     * @param lateStart
     * @param earlyEnd
     */
    public void setLateStartAndEarlyEnd(double lateStart, double earlyEnd) {
        KeyValue<Double, Double> le = RubatoDef.ensureLateStartEarlyEndBoundaries(lateStart, earlyEnd);

        this.earlyEnd = le.getValue();
        this.getXml().getAttribute("earlyEnd").setValue(Double.toString(this.earlyEnd));

        this.lateStart = le.getKey();
        this.getXml().getAttribute("lateStart").setValue(Double.toString(this.lateStart));
    }

    /**
     * make sure that intensity has avalid value
     * @param intensity
     * @return
     */
    private static double ensureIntensityBoundaries(double intensity) {
        if (intensity == 0.0) {
            System.err.println("Invalid rubato intensity = 0.0 is set to 0.01.");
            return 0.01;
        }
        if (intensity < 0.0) {
            System.err.println("Invalid rubato intensity < 0.0 is inverted.");
            return intensity * -1.0;
        }
        return intensity;
    }

    /**
     * ensure validity and consistency of lateStart and earlyEnd
     * @param lateStart
     * @param earlyEnd
     * @return
     */
    private static KeyValue<Double, Double> ensureLateStartEarlyEndBoundaries(Double lateStart, Double earlyEnd) {
        KeyValue<Double, Double> le = new KeyValue<>(lateStart, earlyEnd);
        if ((lateStart != null) && (lateStart < 0.0)) {
            System.err.println("Invalid rubato lateStart < 0.0 is set to 0.0.");
            le.setKey(0.0);
        }
        if ((earlyEnd != null) && (earlyEnd > 1.0)) {
            System.err.println("Invalid rubato earlyEnd > 1.0 is set to 1.0.");
            le.setValue(1.0);
        }
        if ((lateStart != null) && (earlyEnd != null) && (lateStart >= earlyEnd)) {
            System.err.println("Invalid rubato lateStart >= earlyEnd, setting them to 0.0 and 1.0.");
            le.setKey(0.0);
            le.setValue(1.0);
        }
        return le;
    }

}
