package meico.xml;

import meico.mei.Helper;
import nu.xom.Element;
import nu.xom.Attribute;

import java.util.*;

/**
 * This class is an object-oriented wrapper of some Helper function. It is meant as easy access of MEI/MSM element data.
 * @author Lars Engeln
 */
public class RichElement {
    protected Element element;
    protected String id = null;

    /**
     * constructor from an XML Element. The element will be used directly, i.e. changes to the RichElement will change the given element.
     * @param element
     */
    public RichElement(Element element) {
        this(element, false);
    }

    /**
     * constructor from an XML Element. If deepCopy is true, the given element will be cloned and changes to the RichElement will not change the given element.
     * @param element
     * @param deepCopy
     */
    public RichElement(Element element, boolean deepCopy) {
        if(deepCopy) {
            this.element = element.copy();
        }
        else this.element = element;

        initId();
    }
    /**
     * constructor from a given XML Element name. The element will be created with name and a new id will be set.
     * @param localName
     */
    public RichElement(String localName)  {
        this.element = Helper.createElement(localName, false);
        initId();
    }

    /**
     * initializes the id of this element. If the element already has an id, it will be used. Otherwise, a new id will be generated and set to the element.
     */
    private void initId() {
        setId(this.get("id"));
        if(this.id == null) {
            this.id = Helper.addUUID(this.element, false);
        }
    }

    /**
     * returns the id of this element
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     * sets the id of this element and adds it as an attribute to the element. The id will be set in the xml namespace.
     * @param id
     */
    public void setId(String id) {
        if(id == null) return;
        this.id = id;
        Attribute a = new Attribute("id", this.id);                              // create an attribute
        a.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");      // set its namespace to xml
        this.element.addAttribute(a);                                                 // add attribute to the element
    }

    /**
     * generates a new id and sets it to the element. The id will be set in the xml namespace.
     * @return
     */
    public String createNewId() {
        this.id = Helper.addUUID(this.element, true, true);
        return getId();
    }

    /**
     * returns the XML Element
     * @return
     */
    public Element getElement() { return element; }

    /**
     * returns a clone of the XML Element
     * @return
     */
    public Element getClonedElement() {
        return element.copy();
    }

    /**
     * returns the Elements tag-name, i.e. the name in MEI
     * @return
     */
    public String getName() { return element.getLocalName(); }

    /**
     * sets namespace URI
     * @param namespace
     */
    public void setNamespace(String namespace) {
        element.setNamespaceURI(namespace);
    }

    /**
     * checks whether the Attribute with attributeName exists
     * @param attributeName
     * @return
     */
    public boolean has(String attributeName) {
        return Helper.getAttribute(attributeName, this.element) != null;
    }

    /**
     * returns the value of attributeName if the element has it.
     * @param attributeName
     * @return value of attributeName or null if this attribute is not set.
     */
    public String get(String attributeName) {
        String val = Helper.getAttributeValue(attributeName, this.element);
        if(val != null && !val.isEmpty())
            return val;
        return null;
    }
    /**
     * returns the value of attributeName as Double. Hereby ".ges" (e.g. "accid.ges") is preferred.
     * If the element does not have such an Attribute, the matching child (e.g. <accid/>) will be searched.
     * @param attributeName
     * @return value of attributeName (preferring ".ges") or null if this attribute is not set.
     */
    public Double getAsDouble(String attributeName) {
        String value = get(attributeName);
        if(value == null)
            return null;
        return Double.valueOf(value);
    }
    /**
     * returns the value of attributeName as Integer. Hereby ".ges" (e.g. "accid.ges") is preferred.
     * If the element does not have such an Attribute, the matching child (e.g. <accid/>) will be searched.
     * @param attributeName
     * @return value of attributeName (preferring ".ges") or null if this attribute is not set.
     */
    public Integer getAsInteger(String attributeName) {
        String value = get(attributeName);
        if(value == null)
            return null;
        return Integer.valueOf(value);
    }

    /**
     * returns the value of a boolean Attribute. Example: turn.is("delayed");
     * @param attributeName
     * @return defaults to false
     */
    public boolean is(String attributeName) {
        if(!has(attributeName))
            return false;
        return Boolean.parseBoolean(get(attributeName));
    }

    /**
     * sets (adds/overrides) the Attribute attributeName with value
     * @param attributeName
     * @param value
     */
    public void set(String attributeName, String value) {
        this.element.addAttribute(new Attribute(attributeName, value));
    }
    /**
     * sets (adds/overrides) the Attribute attributeName with value
     * @param attributeName
     * @param value
     */
    public void set(String attributeName, double value) {
        this.element.addAttribute(new Attribute(attributeName, Double.toString(value)));
    }
    /**
     * sets (adds/overrides) the Attribute attributeName with value
     * @param attributeName
     * @param value
     */
    public void set(String attributeName, int value) {
        this.element.addAttribute(new Attribute(attributeName, Integer.toString(value)));
    }

    public void remove(String attributeName) {
        Attribute attribute = Helper.getAttribute(attributeName, this.element);
        if(attribute != null)
            this.element.removeAttribute(attribute);
    }
    /**
     * sets (adds/overrides) the Attribute attributeName fromThis
     * @param attributeName
     * @param fromThis
     */
    public void copyValue(String attributeName, RichElement fromThis) {
        String value = fromThis.get(attributeName);
        if(value != null) {
            set(attributeName, value);
        }
    }
    /**
     * appends the child to this element. If the child already has a parent, it will be removed from it.
     * @param child
     */
    public void appendChild(Element child) {
        child.setNamespaceURI(this.element.getNamespaceURI());
        this.element.appendChild(child);
    }
    /**
     * appends the child to this element. If the child already has a parent, it will be removed from it.
     * @param child
     */
    public void appendChild(RichElement child) {
        child.removeParent();
        this.element.appendChild(child.getElement());
    }
    /**
     * returns the first child with the given name as RichElement
     * @param name
     * @return
     */
    public RichElement getFirstChildByName(String name) {
        Element child = Helper.getFirstChildElement(name, this.element);
        if(child == null)
            return null;
        return new RichElement(child);
    }

    /**
     * returns all children as RichElements
     * @return
     */
    public ArrayList<RichElement> getChildren() {
        LinkedList<Element> elements = Helper.getAllChildElements(this.element);
        ArrayList<RichElement> children = new ArrayList<>();
        elements.forEach(elem -> children.add(new RichElement(elem)));
        return children;
    }

    /**
     * returns all children with the given name as RichElements
     * @param name
     * @return
     */
    public ArrayList<RichElement> getChildrenOfType(String name) {
        LinkedList<Element> elements = Helper.getAllChildElements(name, this.element);
        ArrayList<RichElement> children = new ArrayList<>();
        elements.forEach(elem -> children.add(new RichElement(elem)));
        return children;
    }

    /**
     * direct attribute access without .ges fallback or child search, returns null if not found
     * @param attributeName
     * @return
     */
    public String getAttributeValue(String attributeName) {
        Attribute a = Helper.getAttribute(attributeName, this.element);
        if (a == null)
            return null;
        return a.getValue();
    }

    /**
     * returns the Attribute's value from a child with same name like attributeName (Example: attributeName = "accid" -> find child <accid accid="n"/>)
     * @param attributeName
     * @return
     */
    public String getFromChild(String attributeName) {
        return getFromChild(attributeName, Collections.emptyList());
    }

    /**
     * returns the Attribute's value from a child with same name like attributeName (Example: attributeName = "accid" -> find child <accid accid="n"/>)
     * @param attributeName
     * @param ignoredElementNames a List of Elements where not to search for the correct child (e.g. if it is within <del/>, thereby marked as deleted)
     * @return
     */
    public String getFromChild(String attributeName, List<String> ignoredElementNames) {
        java.util.LinkedList<Element> children = Helper.getAllChildElements(this.element);

        for(Element child : children) {
            String elementName = child.getLocalName();
            if(elementName.equals(attributeName)) {
                return (new RichElement(child)).get(attributeName);
            }

            for(String ignoredElementName : ignoredElementNames)
                if(elementName.equals(ignoredElementName)) {
                    return null;
                }

            return (new RichElement(child).getFromChild(attributeName, ignoredElementNames));
        }

        return null;
    }

    /**
     * returns the parent as RichElement
     * @return
     */
    public RichElement getParent() {
        Element parent = Helper.getParentElement(this.element);
        if(parent == null)
            return null;
        return new RichElement(parent);
    }
    /**
     * checks whether this element has a parent
     * @return
     */
    public boolean hasParent() {
        RichElement parent = getParent();
        if(parent == null)
            return false;
        return true;
    }
    /**
     * removes this element from its parent
     */
    public void removeParent() {
        if(hasParent())
            getParent().element.removeChild(this.element);
    }
}
