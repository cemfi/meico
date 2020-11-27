package meico.mpm.elements.styles;

import meico.mei.Helper;
import meico.mpm.elements.styles.defs.ArticulationDef;
import nu.xom.Element;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class interfaces articulation style definitions.
 * Basically, these define an articulation name string (such as "legato", "portato", "agogic accent")
 * and assotiate it with an instance of ArticulationDef.
 * @author Axel Berndt
 */
public class ArticulationStyle extends GenericStyle<ArticulationDef> {
    /**
     * this constructor generates an empty styleDef for dynamicsDefs to be added subsequently
     * @param name
     * @throws Exception
     */
    private ArticulationStyle(String name) throws Exception {
        super(name);
    }

    /**
     * this constructor generates the object from xml input
     * @param xml
     * @throws Exception
     */
    private ArticulationStyle(Element xml) throws Exception {
        super(xml);
    }

    /**
     * ArticulationStyle factory
     * @param name
     * @return
     */
    public static ArticulationStyle createArticulationStyle(String name) {
        ArticulationStyle articulationStyle;
        try {
            articulationStyle = new ArticulationStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return articulationStyle;
    }

    /**
     * ArticulationStyle factory
     * @param name
     * @param id
     * @return
     */
    public static ArticulationStyle createArticulationStyle(String name, String id) {
        ArticulationStyle articulationStyle;
        try {
            articulationStyle = new ArticulationStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        articulationStyle.setId(id);
        return articulationStyle;
    }

    /**
     * ArticulationStyle factory
     * @param xml
     * @return
     */
    public static ArticulationStyle createArticulationStyle(Element xml) {
        ArticulationStyle articulationStyle;
        try {
            articulationStyle = new ArticulationStyle(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return articulationStyle;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws Exception {
        super.parseData(xml);

        // parse the articulationDef elements (the children of this styleDef)
        LinkedList<Element> articDefs = Helper.getAllChildElements("articulationDef", this.getXml());
        for (Element articDef : articDefs) {                                // for each articulationDef
            ArticulationDef ad = ArticulationDef.createArticulationDef(articDef);
            if (ad == null)
                continue;
            this.defs.put(ad.getName(), ad);                             // add the (name, ArticulationDef) pair to the lookup table
        }
    }
}
