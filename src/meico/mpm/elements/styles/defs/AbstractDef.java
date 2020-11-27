package meico.mpm.elements.styles.defs;

import meico.mei.Helper;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This abstract class reduces the amount of copy code in the ...Def classes.
 * @author Axel Berndt
 */
public abstract class AbstractDef extends AbstractXmlSubtree {
    protected Attribute name;               // a quick link to the name attribute
    private Attribute id = null;            // the id attribute

    protected void parseData(Element xml) throws Exception {
        if (xml == null)
            throw new Exception("Cannot generate AbstractDef object. XML Element is null.");

        this.name = Helper.getAttribute("name", xml);
        if (this.name == null)
            throw new Exception("Cannot generate AbstractDef object. Missing name attribute.");

        this.setXml(xml);

        this.id = Helper.getAttribute("id", this.getXml());
    }

    /**
     * get the name of the rubatoDef
     * @return
     */
    public String getName() {
        return this.name.getValue();
    }

    /**
     * set the name of the rubatoDef
     * @param name
     */
    protected void setName(String name) {
        this.name.setValue(name);
    }

    /**
     * set the part's id
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
     * get the part's id
     * @return a string or null
     */
    public String getId() {
        if (this.id == null)
            return null;

        return this.id.getValue();
    }
}
