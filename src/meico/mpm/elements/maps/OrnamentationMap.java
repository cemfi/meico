package meico.mpm.elements.maps;

import nu.xom.Element;

/**
 * This class interfaces MPM's ornamentationMaps
 * @author Axel Berndt
 */
public class OrnamentationMap extends GenericMap {
    /**
     * constructor, generates an empty OrnamentationMap
     * @throws Exception
     */
    private OrnamentationMap() throws Exception {
        super("ornamentationMap");
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws Exception
     */
    private OrnamentationMap(Element xml) throws Exception {
        super(xml);
    }

    /**
     * OrnamentationMap factory
     * @return
     */
    public static OrnamentationMap createOrnamentationMap() {
        OrnamentationMap d;
        try {
            d = new OrnamentationMap();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * OrnamentationMap factory
     * @param xml
     * @return
     */
    public static OrnamentationMap createOrnamentationMap(Element xml) {
        OrnamentationMap d;
        try {
            d = new OrnamentationMap(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws Exception {
        super.parseData(xml);
        this.setType("ornamentationMap");            // make sure this is really a "ornamentationMap"
    }
}
