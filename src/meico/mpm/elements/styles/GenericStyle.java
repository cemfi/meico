package meico.mpm.elements.styles;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This class interfaces MPM styleDef elements.
 * @author Axel Berndt
 */
public class GenericStyle extends AbstractXmlSubtree {
    private Attribute name;                                     // a quick link to the name of the styleDef

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
}
