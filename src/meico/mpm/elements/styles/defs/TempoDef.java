package meico.mpm.elements.styles.defs;

import meico.mei.Helper;
import meico.mpm.Mpm;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This class interfaces MPM's tempoDef elements.
 * @author Axel Berndt
 */
public class TempoDef extends AbstractDef {
    private double value = 0.0;

    /**
     * constructor to create a TempoDef from its name and value
     * @param name
     * @param value
     * @throws Exception
     */
    private TempoDef(String name, double value) throws Exception {
        Element e = new Element("tempoDef", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("name", name));
        e.addAttribute(new Attribute("value", Double.toString(value)));
        this.parseData(e);
    }

    /**
     * contructor to create a TempoDef instance from the xml
     * @param xml
     * @throws Exception
     */
    private TempoDef(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * TempoDef factory
     * @param name
     * @param value
     * @return
     */
    public static TempoDef createTempoDef(String name, double value) {
        TempoDef tempoDef;
        try {
            tempoDef = new TempoDef(name, value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return tempoDef;
    }

    /**
     * TempoDef factory
     * @param xml
     * @return
     */
    public static TempoDef createTempoDef(Element xml) {
        TempoDef tempoDef;
        try {
            tempoDef = new TempoDef(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return tempoDef;
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
            throw new Exception("Cannot generate TempoDef object. Missing value attribute.");
        }

        this.setXml(xml);

        // make sure that this element is really a "tempoDef" element
        if (!this.getXml().getLocalName().equals("tempoDef")) {
            this.getXml().setLocalName("tempoDef");
        }

        this.value = Double.parseDouble(value.getValue());
    }

    /**
     * get the TempoDef's value
     * @return
     */
    public double getValue() {
        return this.value;
    }

    /**
     * set the TempoDef's value
     * @param value
     */
    public void setValue(double value) {
        this.value = value;
        this.getXml().getAttribute("value").setValue(Double.toString(value));
    }

    /**
     * based on a tempo name create a default tempoDef
     * @param name
     * @return
     */
    public static TempoDef createDefaultTempoDef(String name) {
        return TempoDef.createTempoDef(name, TempoDef.getDefaultTempo(name));
    }

    /**
     * a convenient getter for some default tempo descriptor strings,
     * all values are averages from https://de.wikipedia.org/wiki/Tempo_(Musik)
     * @param descriptor
     * @return
     */
    public static double getDefaultTempo(String descriptor) {
        String des = descriptor.trim().toLowerCase();
        if (des.contains("grave"))          return 42.0;
        if (des.contains("largo"))          return 50.0;
        if (des.contains("lento"))          return 51.0;
        if (des.contains("adagio"))         return 79.0;
        if (des.contains("larghetto"))      return 69.0;
        if (des.contains("adagietto"))      return 66.0;
        if (des.contains("andante"))        return 101.0;
        if (des.contains("andantino"))      return 80.0;
        if (des.contains("maestoso"))       return 88.0;
        if (des.contains("moderato"))       return 106.0;
        if (des.contains("allegretto"))     return 110.0;
        if (des.contains("animato"))        return 121.0;
        if (des.contains("allegro"))        return 147.0;
        if (des.contains("assai"))          return 145.0;
        if (des.contains("vivace"))         return 164.0;
        if (des.contains("presto"))         return 189.0;
        if (des.contains("prestissimo"))    return 206.0;

        return 100.0;
    }

}
