package meico.mpm.elements.styles;

import meico.mei.Helper;
import meico.mpm.elements.styles.defs.RubatoDef;
import nu.xom.Element;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class interfaces rubato style definitions.
 * Basically, these define a string and
 * and assotiate it with an instance of RubatoDef.
 * @author Axel Berndt
 */
public class RubatoStyle extends GenericStyle<RubatoDef> {
    /**
     * this constructor generates an empty styleDef for rubatoDefs to be added subsequently
     * @param name
     * @throws Exception
     */
    private RubatoStyle(String name) throws Exception {
        super(name);
    }

    /**
     * this constructor generates the object from xml input
     * @param xml
     * @throws Exception
     */
    private RubatoStyle(Element xml) throws Exception {
        super(xml);
    }

    /**
     * RubatoStyle factory
     * @param name
     * @return
     */
    public static RubatoStyle createRubatoStyle(String name) {
        RubatoStyle rubatoStyle;
        try {
            rubatoStyle = new RubatoStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return rubatoStyle;
    }

    /**
     * RubatoStyle factory
     * @param name
     * @param id
     * @return
     */
    public static RubatoStyle createRubatoStyle(String name, String id) {
        RubatoStyle rubatoStyle;
        try {
            rubatoStyle = new RubatoStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        rubatoStyle.setId(id);
        return rubatoStyle;
    }

    /**
     * RubatoStyle factory
     * @param xml
     * @return
     */
    public static RubatoStyle createRubatoStyle(Element xml) {
        RubatoStyle rubatoStyle;
        try {
            rubatoStyle = new RubatoStyle(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return rubatoStyle;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws Exception {
        super.parseData(xml);

        // parse the rubatoDef elements (the children of this styleDef)
        LinkedList<Element> rubatoDefs = Helper.getAllChildElements("rubatoDef", this.getXml());
        for (Element def : rubatoDefs) {                       // for each rubatoDef
            RubatoDef rd = RubatoDef.createRubatoDef(def);
            if (rd == null)
                continue;
            this.defs.put(rd.getName(), rd);                           // add the (name, RubatoDef) pair to the lookup table
        }
    }
}
