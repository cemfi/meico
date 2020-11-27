package meico.mpm.elements;

import meico.mpm.Mpm;
import meico.mpm.elements.maps.*;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Element;
import nu.xom.Nodes;

import java.util.HashMap;

/**
 * This class interfaces MPM's dated environment.
 * @author Axel Berndt
 */
public class Dated extends AbstractXmlSubtree {
    private HashMap<String, GenericMap> maps = new HashMap<>();
    private Global global = null;                                   // link to the global environment
    private Part part = null;                                       // link to the local environment, leave null if this is a global dated environment

    /**
     * constructor
     * @throws Exception
     */
    private Dated() throws Exception {
        this.parseData(new Element("dated", Mpm.MPM_NAMESPACE));
    }

    /**
     * this constructor instantiates the Dated environment object from an existing xml source handed over as XOM Element
     * @param xml
     * @throws Exception
     */
    private Dated(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * dated environment factory
     * @return
     */
    public static Dated createDated() {
        Dated dated;
        try {
            dated = new Dated();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dated;
    }

    /**
     * this factory instantiates the Dated environment object from an existing xml source handed over as XOM Element
     * @param xml
     * @return
     */
    public static Dated createDated(Element xml) {
        Dated dated;
        try {
            dated = new Dated(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dated;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    @Override
    protected void parseData(Element xml) throws Exception {
        if (xml == null)
            throw new Exception("Cannot generate Dated object. XML Element is null.");

        this.setXml(xml);

        // make sure that this element is really a "dated" element
        if (!this.getXml().getLocalName().equals("dated")) {
            this.getXml().setLocalName("dated");
        }

        // parse the style definitions
        Nodes maps = this.getXml().query("descendant::*[contains(local-name(), 'Map') or local-name()='score']");   // get all the maps (feature the substring "Map" in their local name or the local name is score (see MSM))
        for (int s = 0; s < maps.size(); ++s) {                                             // for each map
//            Element mapElt = (Element) maps.get(s);                                         // get it as an element
//            GenericMap map = GenericMap.createGenericMap(mapElt);
//            if (map == null)
//                continue;

            this.addMap((Element) maps.get(s));                                            // add it to the data structure/hashmap
        }
    }

    public GenericMap addMap(Element xml) {
        if (xml == null)
            return null;

        String type = xml.getLocalName();

        GenericMap m;
        switch (type) {                                 // if the map is of a known type, generate the corresponding object type
            case Mpm.DYNAMICS_MAP:
                m = DynamicsMap.createDynamicsMap(xml);
                break;
            case Mpm.METRICAL_ACCENTUATION_MAP:
                m = MetricalAccentuationMap.createMetricalAccentuationMap(xml);
                break;
            case Mpm.TEMPO_MAP:
                m = TempoMap.createTempoMap(xml);
                break;
            case Mpm.RUBATO_MAP:
                m = RubatoMap.createRubatoMap(xml);
                break;
            case Mpm.ASYNCHRONY_MAP:
                m = AsynchronyMap.createAsynchronyMap(xml);
                break;
            case Mpm.ARTICULATION_MAP:
                m = ArticulationMap.createArticulationMap(xml);
                break;
            case Mpm.IMPRECISION_MAP:
            case Mpm.IMPRECISION_MAP_TIMING:
            case Mpm.IMPRECISION_MAP_DYNAMICS:
            case Mpm.IMPRECISION_MAP_TONEDURATION:
            case Mpm.IMPRECISION_MAP_TUNING:
                m = ImprecisionMap.createImprecisionMap(xml);
                break;
            case Mpm.ORNAMENTATION_MAP:
                m = OrnamentationMap.createOrnamentationMap();
                break;
            default:
                m = GenericMap.createGenericMap(type);
        }

        return this.addMap(m);
    }

    /**
     * Generate a new empty map with the specified local name and add it to the dated environment.
     * If such a map is already present it will be replaced by the new one.
     * @param type
     * @return
     */
    public GenericMap addMap(String type) {
        if (type.isEmpty())
            return null;

        GenericMap m;
        switch (type) {                                 // if the map is of a known type, generate the corresponding object type
            case Mpm.DYNAMICS_MAP:
                m = DynamicsMap.createDynamicsMap();
                break;
            case Mpm.METRICAL_ACCENTUATION_MAP:
                m = MetricalAccentuationMap.createMetricalAccentuationMap();
                break;
            case Mpm.TEMPO_MAP:
                m = TempoMap.createTempoMap();
                break;
            case Mpm.RUBATO_MAP:
                m = RubatoMap.createRubatoMap();
                break;
            case Mpm.ASYNCHRONY_MAP:
                m = AsynchronyMap.createAsynchronyMap();
                break;
            case Mpm.ARTICULATION_MAP:
                m = ArticulationMap.createArticulationMap();
                break;
            case Mpm.IMPRECISION_MAP:
                m = ImprecisionMap.createImprecisionMap("");
                break;
            case Mpm.IMPRECISION_MAP_TIMING:
                m = ImprecisionMap.createImprecisionMap("timing");
                break;
            case Mpm.IMPRECISION_MAP_DYNAMICS:
                m = ImprecisionMap.createImprecisionMap("dynamics");
                break;
            case Mpm.IMPRECISION_MAP_TONEDURATION:
                m = ImprecisionMap.createImprecisionMap("toneduration");
                break;
            case Mpm.IMPRECISION_MAP_TUNING:
                m = ImprecisionMap.createImprecisionMap("tuning");
                break;
            case Mpm.ORNAMENTATION_MAP:
                m = OrnamentationMap.createOrnamentationMap();
                break;
            default:
                m = GenericMap.createGenericMap(type);
        }

        return this.addMap(m);
    }

    /**
     * Add the specified map to the dated environment.
     * If such a map is already present it will be replaced by the new one.
     * @param map the map to be added
     * @return
     */
    public GenericMap addMap(GenericMap map) {
        if (map == null)
            return null;

        if (this.maps.containsKey(map.getType()))   // if there is already such a map
            this.removeMap(map.getType());          // delete it

        Header globalHeader = (this.global == null) ? null : this.global.getHeader();
        Header localHeader = (this.part == null) ? null : this.part.getHeader();
        map.setHeaders(globalHeader, localHeader);

        this.maps.put(map.getType(), map);

        Element parent = (Element) map.getXml().getParent();
        if (parent == null)
            this.getXml().appendChild(map.getXml());                                 // add the xml code
        else if (parent != this.getXml()) {
            map.getXml().detach();
            this.getXml().appendChild(map.getXml());                                 // add the xml code
        }

        return map;
    }

    /**
     * remove the map of the specified type from the dated environment
     * @param type
     */
    public void removeMap(String type) {
        GenericMap m = this.maps.remove(type);
        if (m != null) {
            this.getXml().removeChild(m.getXml());
//            m.getXml().detach();
        }
    }

    /**
     * remove all maps from dated
     */
    public void clear() {
       this.getXml().removeChildren();
       this.maps.clear();
    }

    /**
     * access the map of the specified type
     * @param type
     * @return
     */
    public GenericMap getMap(String type) {
        return this.maps.get(type);
    }

    /**
     * access the whole hashmap with all the maps in it
     * @return
     */
    public HashMap<String, GenericMap> getAllMaps() {
        return this.maps;
    }

    /**
     * this is used to set the global and part environment of the dated data
     * @param global
     * @param part
     */
    protected void setEnvironment(Global global, Part part) {
        this.global = global;
        this.part = part;

        Header globalHeader = (this.global == null) ? null : this.global.getHeader();
        Header localHeader = (this.part == null) ? null : this.part.getHeader();

        // update the links for all maps
        for (GenericMap map : this.maps.values())
            map.setHeaders(globalHeader, localHeader);
    }

    /**
     * get this dated's link to the global environment
     * @return
     */
    public Global getGlobal() {
        return this.global;
    }

    /**
     * get this dated's link to the local environment
     * @return
     */
    public Part getPart() {
        return this.part;
    }
}
