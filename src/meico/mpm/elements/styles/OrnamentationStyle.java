package meico.mpm.elements.styles;

import meico.mei.Helper;
import meico.mpm.elements.styles.defs.OrnamentDef;
import nu.xom.Element;

import java.util.LinkedList;

/**
 * This class interfaces ornamentation style definitions.
 * Basically, these define a string
 * and assotiate it with an instance of OrnamentDef.
 * @author Axel Berndt
 */
public class OrnamentationStyle extends GenericStyle<OrnamentDef> {
    /**
     * this constructor generates an empty styleDef for ornamentDefs to be added subsequently
     * @param name
     * @throws Exception
     */
    private OrnamentationStyle(String name) throws Exception {
        super(name);
    }

    /**
     * this constructor generates the object from xml input
     * @param xml
     * @throws Exception
     */
    private OrnamentationStyle(Element xml) throws Exception {
        super(xml);
    }

    /**
     * OrnamentationStyle factory
     * @param name
     * @return
     */
    public static OrnamentationStyle createOrnamentationStyle(String name) {
        OrnamentationStyle ornamentationStyle;
        try {
            ornamentationStyle = new OrnamentationStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return ornamentationStyle;
    }

    /**
     * OrnamentationStyle factory
     * @param name
     * @param id
     * @return
     */
    public static OrnamentationStyle createOrnamentationStyle(String name, String id) {
        OrnamentationStyle ornamentationStyle;
        try {
            ornamentationStyle = new OrnamentationStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        ornamentationStyle.setId(id);
        return ornamentationStyle;
    }

    /**
     * OrnamentationStyle factory
     * @param xml
     * @return
     */
    public static OrnamentationStyle createOrnamentationStyle(Element xml) {
        OrnamentationStyle ornamentationStyle;
        try {
            ornamentationStyle = new OrnamentationStyle(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return ornamentationStyle;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    public void parseData(Element xml) throws Exception {
        super.parseData(xml);

        // parse the ornamentDef elements (the children of this styleDef)
        LinkedList<Element> ornamentDefs = Helper.getAllChildElements("ornamentDef", this.getXml());
        for (Element def : ornamentDefs) {      // for each ornamentDef
            OrnamentDef od = OrnamentDef.createOrnamentDef(def);
            if (od == null)
                continue;
            this.defs.put(od.getName(), od);    // add the (name, OrnamentDef) pair to the lookup table
        }
    }

}
