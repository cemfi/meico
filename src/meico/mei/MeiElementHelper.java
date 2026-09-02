package meico.mei;

import meico.xml.RichElement;
import nu.xom.Element;

import java.util.*;

/**
 * This helper-class is an object-oriented wrapper of some `Helper` function. It is meant as easy access of MEI (note) element data without fully supporting all MEI features.
 * @author Lars Engeln
 */
public class MeiElementHelper extends RichElement {

    /**
     * constructor from XML element
     * @param element
     */
    public MeiElementHelper(Element element) {
        super(element);
    }

    /**
     * constructor from RichElement
     * @param element
     */
    public MeiElementHelper(RichElement element) {
        super(element.getElement());
    }

    /**
     * constructor from XML element with deep copy option
     * @param element
     * @param deepCopy
     */
    public MeiElementHelper(Element element, boolean deepCopy) {
        super(element, deepCopy);
        setNamespace("http://www.music-encoding.org/ns/mei");
    }

    /**
     * constructor from local name
     * @param localName
     */
    public MeiElementHelper(String localName)  {
        super(localName);
        setNamespace("http://www.music-encoding.org/ns/mei");
    }

    /**
     * returns the value of attributeName if the element has it. Hereby ".ges" (e.g. "accid.ges") is preferred.
     * If the element does not have such an Attribute, the matching child (e.g. <accid/>) will be search.
     * @param attributeName
     * @return value of attributeName (preferring ".ges") or null if this attribute is not set.
     */
    public String get(String attributeName) {
        if(has(attributeName + ".ges"))
            return Helper.getAttributeValue(attributeName + ".ges", this.element);

        if(has(attributeName))
            return Helper.getAttributeValue(attributeName, this.element);

        return getFromChild(attributeName, Arrays.asList("damage","del","sic"));
    }

    /**
     * returns all children as MeiElements
     * @return
     */
    public ArrayList<MeiElementHelper> getChildrenAsMeiElements() {
        LinkedList<Element> elements = Helper.getAllChildElements(this.element);
        ArrayList<MeiElementHelper> children = new ArrayList<>();
        elements.forEach(elem -> children.add(new MeiElementHelper(elem)));
        return children;
    }

    /**
     * returns all children with the given name as MeiElements
     * @param name
     * @return
     */
    public ArrayList<MeiElementHelper> getChildrenAsMeiElements(String name) {
        LinkedList<Element> elements = Helper.getAllChildElements(name, this.element);
        ArrayList<MeiElementHelper> children = new ArrayList<>();
        elements.forEach(elem -> children.add(new MeiElementHelper(elem)));
        return children;
    }
}
