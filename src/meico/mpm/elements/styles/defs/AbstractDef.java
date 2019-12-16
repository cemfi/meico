package meico.mpm.elements.styles.defs;

import meico.xml.AbstractXmlSubtree;
import nu.xom.Attribute;

/**
 * This abstract class reduces the amount of copy code in the ...Def classes.
 * @author Axel Berndt
 */
public abstract class AbstractDef extends AbstractXmlSubtree {
    protected Attribute name;             // a quick link to the name attribute

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
}
