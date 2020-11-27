package meico.mpm.elements.styles.defs;

import meico.mei.Helper;
import meico.mpm.Mpm;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This class interfaces MPM's dynamicsDef elements.
 * @author Axel Berndt
 */
public class DynamicsDef extends AbstractDef {
    private double value = 0.0;

    /**
     * constructor to create a DynamicsDef from its name and value
     * @param name
     * @param value
     * @throws Exception
     */
    private DynamicsDef(String name, double value) throws Exception {
        Element e = new Element("dynamicsDef", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("name", name));
        e.addAttribute(new Attribute("value", Double.toString(value)));
        this.parseData(e);
    }

    /**
     * contructor to create a DynamicsDef instance from xml
     * @param xml
     * @throws Exception
     */
    private DynamicsDef(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * DynamicsDef factory
     * @param name
     * @param value
     * @return
     */
    public static DynamicsDef createDynamicsDef(String name, double value) {
        DynamicsDef dynamicsDef;
        try {
            dynamicsDef = new DynamicsDef(name, value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dynamicsDef;
    }

    /**
     * DynamicsDef factory
     * @param xml
     * @return
     */
    public static DynamicsDef createDynamicsDef(Element xml) {
        DynamicsDef dynamicsDef;
        try {
            dynamicsDef = new DynamicsDef(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dynamicsDef;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws Exception {
        super.parseData(xml);

        // parse the dynamicsDef element
        Attribute value = Helper.getAttribute("value", xml);                                    // get its value attribute
        if (value == null) {                                                            // if no value
            throw new Exception("Cannot generate DynamicsDef object. Missing value attribute.");
        }

        // make sure that this element is really a DynamicsDef element
        if (!this.getXml().getLocalName().equals("dynamicsDef")) {
            this.getXml().setLocalName("dynamicsDef");
        }

        this.value = Double.parseDouble(value.getValue());
    }

    /**
     * get the dynamicsDef's value
     * @return
     */
    public double getValue() {
        return this.value;
    }

    /**
     * set the dynamicsDef's value
     * @param value
     */
    public void setValue(double value) {
        this.value = value;
        this.getXml().getAttribute("value").setValue(Double.toString(value));
    }

    /**
     * based on a dynamics name create a default dynamicsDef
     * @param name
     * @return
     */
    public static DynamicsDef createDefaultDynamicsDef(String name) {
        return DynamicsDef.createDynamicsDef(name, DynamicsDef.getDefaultVolumeLevel(name));
    }

    /**
     * a convenient getter for some default dynamics values
     * @param dynamics
     * @return
     */
    public static double getDefaultVolumeLevel(String dynamics) {
        switch (dynamics.trim().toLowerCase()) {
            case "pppp":
            case "pianissimopianissimo":
                return 5.0;
            case "ppp":
            case "pianopianissimo":
                return 12.0;
            case "pp":
            case "pianissimo":
                return 36.0;
            case "p":
            case "piano":
                return 48.0;
            case "mp":
            case "mezzopiano":
                return 64.0;
            case "mf":
            case "mezzoforte":
                return 83.0;
            case "f":
            case "forte":
                return 97.0;
            case "ff":
            case "fortissimo":
                return 111.0;
            case "fff":
            case "fortefortissimo":
                return 120.0;
            case "ffff":
            case "fortissimofortissimo":
                return 125.0;
            case "sf":
            case "sfz":
            case "fz":
            case "sforzato":
                return 127.0;
            default:
                return 74.0;
        }
    }

}
