package meico.mpm.elements.maps;

import com.sun.istack.internal.NotNull;
import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.maps.data.OrnamentData;
import meico.supplementary.KeyValue;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;

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

    /**
     * add an ornament element to the ornamentationMap
     * @param date
     * @param nameRef
     * @param scale set this to 1.0 to omit it from the xml code
     * @param noteOrder set this null or leave it empty to omit it from the xml code; provide just one string with "ascending pitch" or "descending pitch" to set this
     * @param id set this null or leave it empty to omit it from the xml code
     * @return the index at which the element has been added
     */
    public int addOrnament(double date, @NotNull String nameRef, double scale, ArrayList<String> noteOrder, String id) {
        Element ornament = new Element("ornament", Mpm.MPM_NAMESPACE);
        ornament.addAttribute(new Attribute("date", Double.toString(date)));
        ornament.addAttribute(new Attribute("name.ref", nameRef));

        if (scale != 1.0)
            ornament.addAttribute(new Attribute("scale", Double.toString(scale)));

        if ((noteOrder != null) && !noteOrder.isEmpty()) {
            String noteIdsString = "";
            for (String nid : noteOrder) {
                if (nid.equals("ascending pitch") || nid.equals("descending pitch")) {
                    noteIdsString = nid;
                    break;
                } else {
                    noteIdsString = noteIdsString.concat(" #" + nid.trim().replace("#", ""));   // the replacement handles the case that the # is already in the string as we do not want to have multiple #
                }
            }
            ornament.addAttribute(new Attribute("note.order", noteIdsString.trim()));
        }

        if ((id != null) && !id.isEmpty())
            ornament.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id));

        KeyValue<Double, Element> kv = new KeyValue<>(date, ornament);
        return this.insertElement(kv, false);
    }

    /**
     * add an ornament element to the ornamentationMap
     * @param date
     * @param nameRef
     * @return the index at which the element has been added
     */
    public int addOrnament(double date, @NotNull String nameRef) {
        return this.addOrnament(date, nameRef, 1.0, null, null);
    }

    /**
     * add an ornament element to the ornamentationMap
     * @param data xml data will be ignored (use addElement() instead to add an xml representation)
     * @return
     */
    public int addOrnament(OrnamentData data) {
        if (data.ornamentDef != null)
            data.ornamentDefName = data.ornamentDef.getName();
        else if (data.ornamentDefName == null) {
            System.err.println("Cannot add ornament: ornamentDef or ornamentDefName must be specified.");
            return -1;
        }
        return this.addOrnament(data.date, data.ornamentDefName, data.scale, data.noteOrder, data.xmlId);
    }

    /**
     * On the basis of this ornamentationMap, edit the maps (MSM scores!).
     * This method is meant to be applied BEFORE the other timing and articulation transformations and AFTER dynamics and metrical accentuation rendering.
     * @param parts the MSM part elements which the ornamentationMap is applied to
     * @param ornamentationMap the global ornamentationMap
     */
    public static void renderGlobalOrnamentationToParts(ArrayList<Element> parts, OrnamentationMap ornamentationMap) {
        if ((ornamentationMap == null) || ornamentationMap.isEmpty())
            return;

        ArrayList<Element> mapsToOrnament = new ArrayList<>();
        for (Element part : parts) {
            Element s = Helper.getFirstChildElement("dated", part);
            if (s != null) {
                s = Helper.getFirstChildElement("score", s);
                if (s != null) {                                        // if the part has a score (this is where ornamentation is applied)
                    mapsToOrnament.add(s);                              // add it to the mapsToOrnament list
                }
            }
        }

        // global ornamentation rendering will add only modifier attributes to the notes; these will be rendered into performance attributes in the local processing later on
        ornamentationMap.renderGlobalOrnamentationMap(mapsToOrnament);
    }

    /**
     * On the basis of this ornamentationMap, edit the maps (MSM scores!).
     * This method is meant to be applied BEFORE the other timing and articulation transformations and AFTER dynamics and metrical accentuation rendering.
     * @param maps the MSM scores to which the ornamentationMap is applied
     */
    public void renderGlobalOrnamentationMap(ArrayList<Element> maps) {
        if ((maps == null) || maps.isEmpty())
            return;

        // unify the maps into one GenericMap
        GenericMap map = GenericMap.createGenericMap("unifiedScoreMap");
        if (map == null)
            return;
        for (Element m : maps) {
            if (m != null) {
                for (Element e : m.getChildElements())
                    map.addElement(e);
            }
        }

        this.renderOrnamentationMapIntoAttributes(map);
    }

    /**
     * On the basis of the specified ornamentationMap, add/edit the corresponding data to all note elements of the specified map.
     * Basically, that map should be an MSM score because only note elements will be processed.
     * @param map MSM score
     * @param ornamentationMap
     */
    public static void renderOrnamentationToMap(GenericMap map, OrnamentationMap ornamentationMap) {
        if (ornamentationMap != null)
            ornamentationMap.renderOrnamentationMap(map);
    }

    /**
     * On the basis of the specified ornamentationMap, add/edit the corresponding data to all note elements of the specified map.
     * Basically, that map should be an MSM score because only note elements will be processed.
     * @param map MSM score
     */
    public void renderOrnamentationMap(GenericMap map) {
        if (map == null)
            return;

        if (this.getLocalHeader() != null)  // this is a local ornamentationMap; global ones were already processed via renderGlobalOrnamentationMap(ArrayList<Element> maps)
            this.renderOrnamentationMapIntoAttributes(map);

        // TODO: render ornamentation modifier attributes into .perf and velocity attributes
        // TODO: Where should replacement be done (relevant for future ornamentation features)?
    }

    /**
     * Helper method for ornamentation rendering. This method does not add or edit any
     * performance attributes (xx.perf and velocity) on the map elements. It will only
     * add attributes that will later be used to set the performance attributes.
     * @param map an MSM score; ornaments are applied only to notes
     */
    private void renderOrnamentationMapIntoAttributes(@NotNull GenericMap map) {
        // TODO: compute and add/edit modifier attributes
    }
}
