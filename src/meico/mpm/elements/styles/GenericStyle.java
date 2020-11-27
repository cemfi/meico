package meico.mpm.elements.styles;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.styles.defs.AbstractDef;
import meico.mpm.elements.styles.defs.ArticulationDef;
import meico.mpm.elements.styles.defs.TempoDef;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.HashMap;

/**
 * This class interfaces MPM styleDef elements.
 * @author Axel Berndt
 */
public class GenericStyle<E extends AbstractDef> extends AbstractXmlSubtree {
    private Attribute name;                 // a quick link to the name of the styleDef
    protected Attribute id = null;          // the id attribute
    protected HashMap<String, E> defs;      // the lookup table for the defs

    /**
     * constructor, generates an empty styleDef with the specified name
     * @param name
     * @throws Exception
     */
    protected GenericStyle(String name) throws Exception {
        Element styleDef = new Element("styleDef", Mpm.MPM_NAMESPACE);
        styleDef.addAttribute(new Attribute("name", name));
        this.parseData(styleDef);
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws Exception
     */
    protected GenericStyle(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * GenericStyle factory
     * @param name
     * @return
     */
    public static GenericStyle createGenericStyle(String name) {
        GenericStyle genericStyle;
        try {
            genericStyle = new GenericStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return genericStyle;
    }

    /**
     * GenericStyle factory
     * @param name
     * @param id
     * @return
     */
    public static GenericStyle createGenericStyle(String name, String id) {
        GenericStyle genericStyle;
        try {
            genericStyle = new GenericStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        genericStyle.setId(id);
        return genericStyle;
    }

    /**
     * GenericStyle factory
     * @param xml
     * @return
     */
    public static GenericStyle createGenericStyle(Element xml) {
        GenericStyle genericStyle;
        try {
            genericStyle = new GenericStyle(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return genericStyle;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    @Override
    protected void parseData(Element xml) throws Exception {
        if (xml == null)
            throw new Exception("Cannot generate GenericStyleDef object. XML Element is null.");

        this.name = Helper.getAttribute("name", xml);
        if (this.name == null)
            throw new Exception("Cannot generate GenericStyleDef object. Missing name attribute.");

        this.setXml(xml);

        // make sure that this element is really a "styleDef" element
        if (!this.getXml().getLocalName().equals("styleDef")) {
            this.getXml().setLocalName("styleDef");
        }

        this.id = Helper.getAttribute("id", this.getXml());

        this.defs = new HashMap<>();
    }

    /**
     * get the name of the styleDef
     * @return
     */
    public String getName() {
        return this.name.getValue();
    }

    /**
     * set the name of the styleDef
     * @param name
     */
    protected void setName(String name) {
        this.name.setValue(name);
    }

    /**
     * set the id
     * @param id a xml:id string or null
     */
    public void setId(String id) {
        if (id == null) {
            if (this.id != null) {
                this.id.detach();
                this.id = null;
            }
            return;
        }

        if (this.id == null) {
            this.id = new Attribute("id", id);
            this.id.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");    // set correct namespace
            this.getXml().addAttribute(this.id);
            return;
        }

        this.id.setValue(id);
    }

    /**
     * get the id
     * @return a string or null
     */
    public String getId() {
        if (this.id == null)
            return null;

        return this.id.getValue();
    }

    /**
     * access the whole HashMap with (name, ...Def) pairs
     * @return
     */
    public HashMap<String, E> getAllDefs() {
        return this.defs;
    }

    /**
     * retrieve a specific def
     * @param name
     * @return
     */
    public E getDef(String name) {
        return this.defs.get(name);
    }

    /**
     * add or (if a def with this name is already existent) replace the def
     * @param def the ...Def instance to be added, if there is already one with this name, it is replaced
     */
    public void addDef(E def) {
        if (def == null) {
            System.err.println("Cannot add a null object to the styleDef.");
            return;
        }
        removeDef(def.getName());               // if there is already a def with this name, remove it
        this.defs.put(def.getName(), def);
        this.getXml().appendChild(def.getXml());
    }

    /**
     * remove the specified def from this styleDef
     * @param name
     */
    public void removeDef(String name) {
        E ad = this.defs.get(name);                     // get the xml element of this def
        if (ad == null)                                 // if there is no such def
            return;                                     // done

        this.defs.remove(name);                         // remove the (name, values) lookup table entry
        this.getXml().removeChild(ad.getXml());         // remove the element from the xml
//        ad.getXml().detach();
    }

    /**
     * get the number of defs in this styleDef
     * @return
     */
    public int size() {
        return this.defs.size();
    }

    /**
     * does the styleDef contain defs?
     * @return
     */
    public boolean isEmpty() {
        return this.defs.isEmpty();
    }
}
