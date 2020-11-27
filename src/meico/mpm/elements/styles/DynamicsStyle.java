package meico.mpm.elements.styles;

import meico.mei.Helper;
import meico.mpm.elements.styles.defs.DynamicsDef;
import nu.xom.Element;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class interfaces dynamics style definitions.
 * Basically, these define a string (such as "p", "sf", "forte", "as loud as possible")
 * and assotiate it with an instance of DynamicsDef.
 * @author Axel Berndt
 */
public class DynamicsStyle extends GenericStyle<DynamicsDef> {
    /**
     * this constructor generates an empty styleDef for dynamicsDefs to be added subsequently
     * @param name
     * @throws Exception
     */
    private DynamicsStyle(String name) throws Exception {
        super(name);
    }

    /**
     * this constructor generates the object from xml input
     * @param xml
     * @throws Exception
     */
    private DynamicsStyle(Element xml) throws Exception {
        super(xml);
    }

    /**
     * DynamicsStyle factory
     * @param name
     * @return
     */
    public static DynamicsStyle createDynamicsStyle(String name) {
        DynamicsStyle dynamicsStyle;
        try {
            dynamicsStyle = new DynamicsStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dynamicsStyle;
    }

    /**
     * DynamicsStyle factory
     * @param name
     * @param id
     * @return
     */
    public static DynamicsStyle createDynamicsStyle(String name, String id) {
        DynamicsStyle dynamicsStyle;
        try {
            dynamicsStyle = new DynamicsStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        dynamicsStyle.setId(id);
        return dynamicsStyle;
    }

    /**
     * DynamicsStyle factory
     * @param xml
     * @return
     */
    public static DynamicsStyle createDynamicsStyle(Element xml) {
        DynamicsStyle dynamicsStyle;
        try {
            dynamicsStyle = new DynamicsStyle(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dynamicsStyle;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws Exception {
        super.parseData(xml);

        // parse the dynamicsDef elements (the children of this styleDef)
        LinkedList<Element> dynamicsDefs = Helper.getAllChildElements("dynamicsDef", this.getXml());
        for (Element def : dynamicsDefs) { // for each dynamicsDef
            DynamicsDef dd = DynamicsDef.createDynamicsDef(def);
            if (dd == null)
                continue;
            this.defs.put(dd.getName(), dd);     // add the (name, DynamicsDef) pair to the lookup table
        }
    }

    /**
     * get the numeric value of a dynamics string
     * @param dynamicsString
     * @return the numeric dynamics value or 100.0 if everything else fails
     */
    public double getNumericValue(String dynamicsString) {
        DynamicsDef dynamicsDef = this.getDef(dynamicsString);
        if (dynamicsDef != null)
            return dynamicsDef.getValue();
        try {
            return Double.parseDouble(dynamicsString);
        } catch (NumberFormatException e) {
            System.err.println("Failed to convert dynamics string \"" + dynamicsString + "\" to double. No dynamicsDef, no number format.");
            return 100.0;
        }
    }

    /**
     * get the numeric value of a dynamics string, this is the static variant of the above method
     * @param dynamicsString
     * @param style an instance of DynamicsStyle or null
     * @return the numeric dynamics value or 100.0 if everything else fails
     */
    public static double getNumericValue(String dynamicsString, DynamicsStyle style) {
        DynamicsDef dynamicsDef = (style != null) ? style.getDef(dynamicsString) : null;
        if (dynamicsDef != null)
            return dynamicsDef.getValue();

        try {
            return Double.parseDouble(dynamicsString);
        } catch (NumberFormatException e) {
            System.err.println("Failed to convert dynamics string \"" + dynamicsString + "\" to double. No dynamicsDef, no number format.");
            return 100.0;
        }
    }
}
