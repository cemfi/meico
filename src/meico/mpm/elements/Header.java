package meico.mpm.elements;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.styles.*;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class interfaces MPM header information.
 * @author Axel Berndt
 */
public class Header extends AbstractXmlSubtree {
    private HashMap<String, HashMap<String, GenericStyle>> styleDefs = new HashMap<>();  // this hashmap addresses the style definitions in the form styleDefs.get(styleType, styleDefName);

    /**
     * constructor
     * @throws InvalidDataException
     */
    private Header() throws InvalidDataException {
        this.parseData(new Element("header", Mpm.MPM_NAMESPACE));
    }

    /**
     * this constructor instantiates the Header object from an existing xml source handed over as XOM Element
     * @param xml
     * @throws InvalidDataException
     */
    private Header(Element xml) throws InvalidDataException {
        this.parseData(xml);
    }

    /**
     * header factory
     * @return
     */
    public static Header createHeader() {
        Header header;
        try {
            header = new Header();
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return header;
    }

    /**
     * header factory
     * @return
     */
    public static Header createHeader(Element xml) {
        Header header;
        try {
            header = new Header(xml);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return header;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    @Override
    protected void parseData(Element xml) throws InvalidDataException {
        if (xml == null)
            throw new InvalidDataException("Cannot generate Header object. XML Element is null.");

        this.setXml(xml);

        // make sure that this element is really a "header" element
        if (!this.getXml().getLocalName().equals("header")) {
            this.getXml().setLocalName("header");
        }

        // parse the style definitions
        Nodes styles = this.getXml().query("descendant::*[contains(local-name(), 'Styles')]");      // style definitions are organized in ...Styles elements, e.g. "articulationStyles", "tempoStyles" etc., it always has the substring "Styles", others are ignored
        for (int s = 0; s < styles.size(); ++s) {                                                   // for each collection of styles
            Element styleCollection = (Element) styles.get(s);                                      // get it as an element
            this.addStyleType(styleCollection);                                                     // add the collection of styles to this header
        }
    }

    /**
     * add a new empty style type to the header,
     * if a type with this name is already existent, it will be replaced
     * @param type the local name of the type, e.g. "tempoStyles" or "articulationStyles"
     * @return the style collection just added, converted into a hashmap
     */
    public HashMap<String, GenericStyle> addStyleType(String type) {
        if (type.isEmpty())
            return null;

        if (this.styleDefs.get(type) != null)       // if there is already such a style
            this.removeStyleType(type);             // delete it

        this.getXml().appendChild(new Element(type));
        HashMap<String, GenericStyle> s = new HashMap<>();
        this.styleDefs.put(type, s);
        return s;
    }

    /**
     * add a style type and all its children to the header,
     * if a type with this name is already existent, it will be replaced
     * @param xml the element to be added
     * @return the style collection just added, converted into a hashmap
     */
    public HashMap<String, GenericStyle> addStyleType(Element xml) {
        String type = xml.getLocalName();
        if (this.styleDefs.get(type) != null)                           // if there is already such a style
            this.removeStyleType(type);                                 // delete it

        LinkedList<Element> styleDefElements = Helper.getAllChildElements("styleDef", xml);   // get all the styleDef elements in the collection, others are ignored
        HashMap<String, GenericStyle> styleDefsMap = new HashMap<>();   // this hashmap makes the styleDef accessible by their name

        // get the styleDef as Element
        for (Element styleDef : styleDefElements) {                     // for each styleDef
            GenericStyle sd;
            switch (type) {
                case Mpm.ARTICULATION_STYLE:
                    sd = ArticulationStyle.createArticulationStyle(styleDef);
                    break;
                case Mpm.TEMPO_STYLE:
                    sd = TempoStyle.createTempoStyle(styleDef);
                    break;
                case Mpm.DYNAMICS_STYLE:
                    sd = DynamicsStyle.createDynamicsStyle(styleDef);
                    break;
                case Mpm.METRICAL_ACCENTUATION_STYLE:
                    sd = MetricalAccentuationStyle.createMetricalAccentuationStyle(styleDef);
                    break;
                case Mpm.RUBATO_STYLE:
                    sd = RubatoStyle.createRubatoStyle(styleDef);
                    break;
                case Mpm.ORNAMENTATION_STYLE:
                default:
                    sd = GenericStyle.createGenericStyle(styleDef);
            }

            if (sd == null)
                continue;
            styleDefsMap.put(sd.getName(), sd);                         // add the styleDef to the hashmap
        }

        Element parent = (Element) xml.getParent();
        if ((parent == null) || (parent != this.getXml())) {
            xml.detach();
            this.getXml().appendChild(xml);                             // add the xml code
        }
        this.styleDefs.put(type, styleDefsMap);                         // add the styleDefs collection, now converted into a hashmap, to the class' styleDefs hashmap
        return styleDefsMap;
    }

    /**
     * remove a style type
     * @param type
     */
    public void removeStyleType(String type) {
        if (this.styleDefs.remove(type) != null) {
            Element typeElt = this.getXml().getFirstChildElement(type);
            this.getXml().removeChild(typeElt);
        }
    }

    /**
     * access all style types and their subsequent style definitions
     * @return
     */
    public HashMap<String, HashMap<String, GenericStyle>> getAllStyleTypes() {
        return this.styleDefs;
    }

    /**
     * access all style definitions of the specified type
     * @param type
     * @return
     */
    public HashMap<String, GenericStyle> getAllStyleDefs(String type) {
        return this.styleDefs.get(type);
    }

    /**
     * access the specified styleDef
     * @param type
     * @param name
     * @return
     */
    public GenericStyle getStyleDef(String type, String name) {
        HashMap<String, GenericStyle> styleType = this.styleDefs.get(type);
        if (styleType == null)
            return null;
        return styleType.get(name);
    }

    /**
     * merge the specified styleDef into the specified style type
     * @param type if the type doe not exist it will be generated
     * @param styleDef if there is already a styleDef with this name it will be replaced
     */
    public void addStyleDef(String type, GenericStyle styleDef) {
        if (type.isEmpty() || (styleDef == null))
            return;

        HashMap<String, GenericStyle> styleCollection = this.styleDefs.get(type);   // get the style collection of the specified type
        if (styleCollection == null) {                                              // if no such style type present
            this.getXml().appendChild(new Element(type, Mpm.MPM_NAMESPACE));        // create it in the xml structure
            styleCollection = new HashMap<>();                                      // create its hashmap representation for the styleDefs hashmap
            this.styleDefs.put(type, styleCollection);                              // add it to the styleDefs hashmap
        }

        GenericStyle styleDefOld = styleCollection.get(styleDef.getName());         // is there already a styleDef with this name?
        if (styleDefOld != null)                                                    // if yes
            this.removeStyleDef(type, styleDef.getName());                          // remove it, it will be relaced by this new one

        this.getXml().getFirstChildElement(type, Mpm.MPM_NAMESPACE).appendChild(styleDef.getXml());    // add the styleDef to the xml structure
        styleCollection.put(styleDef.getName(), styleDef);                          // add it to the hashmap for this style type
    }

    /**
     * generate a new empty styleDef in the specified style type/collection
     * @param type if the type doe not exist it will be generated
     * @param name if there is already a styleDef with this name it will be replaced
     */
    public GenericStyle addStyleDef(String type, String name) {
        GenericStyle styleDef;
        switch (type) {
            case Mpm.DYNAMICS_STYLE:
                styleDef = DynamicsStyle.createDynamicsStyle(name);
                break;
            case Mpm.ARTICULATION_STYLE:
                styleDef = ArticulationStyle.createArticulationStyle(name);
                break;
            case Mpm.METRICAL_ACCENTUATION_STYLE:
                styleDef = MetricalAccentuationStyle.createMetricalAccentuationStyle(name);
                break;
            case Mpm.TEMPO_STYLE:
                styleDef = TempoStyle.createTempoStyle(name);
                break;
            case Mpm.RUBATO_STYLE:
                styleDef = RubatoStyle.createRubatoStyle(name);
                break;
//            case Mpm.ORNAMENTATION_STYLE:
//                styleDef = OrnamentationStyle ...
//                break;
            default:
                styleDef = GenericStyle.createGenericStyle(name);
        }

        if (styleDef == null)
            return null;
        this.addStyleDef(type, styleDef);
        return styleDef;
    }

    /**
     * remove the specified styleDef from the specified collection of styleDefs (style type)
     * @param type
     * @param name
     */
    public void removeStyleDef(String type, String name) {
        if (type.isEmpty())
            return;

        HashMap<String, GenericStyle> styleCollection = this.styleDefs.get(type);
        if (styleCollection == null)
            return;

        GenericStyle styleDef = styleCollection.remove(name);
        if (styleDef != null)
            this.getXml().getFirstChildElement("type").removeChild(styleDef.getXml());
    }
}
