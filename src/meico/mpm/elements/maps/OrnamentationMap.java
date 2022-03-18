package meico.mpm.elements.maps;

import com.sun.istack.internal.NotNull;
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
     * @param noteIds set this null or leave it empty to omit it from the xml code
     * @param id set this null or leave it empty to omit it from the xml code
     * @return the index at which the element has been added
     */
    public int addOrnament(double date, @NotNull String nameRef, double scale, ArrayList<String> noteIds, String id) {
        Element ornament = new Element("ornament", Mpm.MPM_NAMESPACE);
        ornament.addAttribute(new Attribute("date", Double.toString(date)));
        ornament.addAttribute(new Attribute("name.ref", nameRef));

        if (scale != 1.0)
            ornament.addAttribute(new Attribute("scale", Double.toString(scale)));

        if ((noteIds != null) && !noteIds.isEmpty()) {
            String noteIdsString = "";
            for (String nid : noteIds)
                noteIdsString = noteIdsString.concat(" #" + nid);
            ornament.addAttribute(new Attribute("noteids", noteIdsString.trim()));
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
        Element ornament = new Element("ornament", Mpm.MPM_NAMESPACE);
        ornament.addAttribute(new Attribute("date", Double.toString(date)));
        ornament.addAttribute(new Attribute("name.ref", nameRef));

        KeyValue<Double, Element> kv = new KeyValue<>(date, ornament);
        return this.insertElement(kv, false);
    }

    /**
     * add an ornament element to the ornamentationMap
     * @param data xml data will be ignored (use addElement() instead to add an xml representation)
     * @return
     */
    public int addOrnament(OrnamentData data) {
        Element ornament = new Element("ornament", Mpm.MPM_NAMESPACE);
        ornament.addAttribute(new Attribute("date", Double.toString(data.date)));

        if (data.ornamentDef != null)
            ornament.addAttribute(new Attribute("name.ref", data.ornamentDef.getName()));
        else if (data.ornamentDefName != null)
            ornament.addAttribute(new Attribute("name.ref", data.ornamentDefName));
        else {
            System.err.println("Cannot add ornament: ornamentDef not specified.");
            return -1;
        }

        if (data.scale != 1.0)
            ornament.addAttribute(new Attribute("scale", Double.toString(data.scale)));

        if ((data.noteIds != null) && !data.noteIds.isEmpty()) {
            String noteIdsString = "";
            for (String nid : data.noteIds)
                noteIdsString = noteIdsString.concat(" #" + nid);
            ornament.addAttribute(new Attribute("noteids", noteIdsString.trim()));
        }

        if ((data.xmlId != null) && !data.xmlId.isEmpty())
            ornament.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", data.xmlId));

        KeyValue<Double, Element> kv = new KeyValue<>(data.date, ornament);
        return this.insertElement(kv, false);
    }
}
