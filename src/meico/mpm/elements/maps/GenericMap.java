package meico.mpm.elements.maps;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.styles.GenericStyle;
import meico.msm.Msm;
import meico.xml.AbstractXmlSubtree;
import meico.mpm.elements.Header;
import meico.supplementary.KeyValue;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class interfaces MPM maps on a more general level. It forms the basis for the more dedicated classes for specific maps.
 * @author Axel Berndt
 */
public class GenericMap extends AbstractXmlSubtree {
    protected ArrayList<KeyValue<Double, Element>> elements = new ArrayList<>();    // this is a list of the map elements (with a date attribute), the form is (date, Element)
    private Header globalHeader = null;                                             // the link to the global header environment for later reference (styleDefs)
    private Header localHeader = null;                                              // the link to this part's header environment for later reference (styleDefs); leave this null if it is a global map

    /**
     * constructor
     * @param type
     * @throws Exception
     */
    protected GenericMap(String type) throws Exception {
        if (type.isEmpty() || !type.contains("Map")) {
            throw new Exception("Cannot generate GenericMap object. Local name \"" + type + "\" must be non-empty and contain the substring \"Map\", e.g. \"tempoMap\".");
        }
        this.parseData(new Element(type, Mpm.MPM_NAMESPACE));
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws Exception
     */
    protected GenericMap(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * GenericMap factory
     * @param name
     * @return
     */
    public static GenericMap createGenericMap(String name) {
        GenericMap genericMap;
        try {
            genericMap = new GenericMap(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return genericMap;
    }

    /**
     * GenericMap factory
     * @param xml
     * @return
     */
    public static GenericMap createGenericMap(Element xml) {
        GenericMap genericMap;
        try {
            genericMap = new GenericMap(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return genericMap;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    @Override
    protected void parseData(Element xml) throws Exception, Exception {
        if (xml == null)
            throw new Exception("Cannot generate GenericMap object. XML Element is null.");

        if (!xml.getLocalName().contains("Map") && !xml.getLocalName().equals("score")) {       // the local name must contain the string "Map" or be equal to "score"; the latter is needed for being able to parse MSM score elements into a GenericMap
            throw new Exception("Cannot generate GenericMap object. Local name \"" + xml.getLocalName() + "\" must contain the substring \"Map\", e.g. \"tempoMap\", or be equal to \"score\".");
        }

        this.elements = new ArrayList<>();
        this.setXml(xml);

        // parse the child elements and sort them according to their date
        Elements es = this.getXml().getChildElements();
        for (int i = 0; i < es.size(); ++i) {                       // for each element
            Element e = es.get(i);
            Attribute d = Helper.getAttribute("date", e);
            if (d == null)                                          // if it has no date
                continue;                                           // ignore it

            if (e.getLocalName().equals("style") && (Helper.getAttribute("name.ref", e) == null))   // if it is a style switch but has no name.ref attribute
                continue;                                           // ignore it

            double date = Double.parseDouble(d.getValue());         // get its date
            int index = 0;                                          // the index where it will be inserted
            for (int j = this.elements.size() - 1; j >= 0; --j) {   // for each element's date so far
                if (date >= this.elements.get(j).getKey()) {        // the element is behind it, where it should be inserted
                    index = ++j;                                    // set the index for the insertion
                    break;
                }
            }

            this.elements.add(index, new KeyValue<>(date, e));      // insert the element
        }

        this.sortXml();                                             // the xml elements can be unsorted, this makes sure it is sorted
    }

    /**
     * The elements can be unsorted in the xml source. This method sorts them according to their date attribute.
     */
    private void sortXml() {
        Element xml = this.getXml();
        for (int i = 0; i < this.elements.size(); ++i) {    // for each element
            Element e = this.elements.get(i).getValue();
            xml.removeChild(e);                             // remove the element wherever it is
            xml.insertChild(e, i);                          // and add it at its correct index
        }
    }

    /**
     * If an element's date is changed, this method can be invoked to make sure that the map is still sorted correctly.
     */
    public void sort() {
        for (KeyValue<Double, Element> e : this.elements) {
            double date = Double.parseDouble(Helper.getAttributeValue("date", e.getValue()));
            if (e.getKey() != date)
                e.setKey(date);
        }

        for (int i = 1; i < this.size(); ++i) {
            KeyValue<Double, Element> e = this.elements.get(i);

            int moveToIndex = i;
            for (int j = i-1; (j >= 0) && (e.getKey() < this.elements.get(j).getKey()); --j)
                moveToIndex = j;

            if (moveToIndex != i)
                Collections.swap(this.elements, i, moveToIndex);    // side note i must be >0
        }

        this.sortXml();
    }

    /**
     * get the type of the map
     * @return
     */
    public String getType() {
        return this.getXml().getLocalName();
    }

    /**
     * set the type of the map
     * @param type
     */
    protected void setType(String type) {
        if (type.isEmpty() || !type.contains("Map")) {
            System.err.println("Cannot set the specified map type. Local name \"" + type + "\" must be non-empty and contain the substring \"Map\", e.g. \"tempoMap\".");
            return;
        }
        this.getXml().setLocalName(type);
    }

    /**
     * Link the global and local header environment for later reference (styleDefs).
     * Applications should not be required to edit these links. They are maintained
     * automatically once the map is added to a Dated object.
     * @param globalHeader the global header environment
     * @param localHeader this part's header environment
     */
    public void setHeaders(Header globalHeader, Header localHeader) {
        this.globalHeader = globalHeader;
        this.localHeader = localHeader;
    }

    /**
     * access the global header environment
     * @return the global header or null
     */
    public Header getGlobalHeader() {
        return this.globalHeader;
    }

    /**
     * access this part's header environment
     * @return the local header or null
     */
    public Header getLocalHeader() {
        return this.localHeader;
    }

    /**
     * access all elements
     * @return a sorted list of (date, Element) pairs
     */
    public ArrayList<KeyValue<Double, Element>> getAllElements() {
        return this.elements;
    }

    /**
     * get all elements of a specific type
     * @param type
     * @return
     */
    public ArrayList<KeyValue<Double, Element>> getAllElementsOfType(String type) {
        ArrayList<KeyValue<Double, Element>> list = new ArrayList<>();
        for (KeyValue<Double, Element> e : this.elements) {
            if (e.getValue().getLocalName().equals(type))
                list.add(e);
        }
        return list;
    }

    /**
     * access all elements at the specified date
     * @param date
     * @return
     */
    public ArrayList<KeyValue<Double, Element>> getAllElementsAt(double date) {
        ArrayList<KeyValue<Double, Element>> results = new ArrayList<>();
        int index = getElementIndexAtAfter(date);
        if (index >= 0) {
            results.add(this.elements.get(index));
            for (++index; (index < this.size()) && (this.elements.get(index).getKey() == date); ++index) {
                results.add(this.elements.get(index));
            }
        }
        return results;
    }

    /**
     * access the first element in the map
     * @return the first element or null if the map is empty
     */
    public Element getFirstElement() {
        if (this.elements.isEmpty())
            return null;
        return this.elements.get(0).getValue();
    }

    /**
     * access the last element in the map
     * @return the last element or null if the map is empty
     */
    public Element getLastElement() {
        if (this.elements.isEmpty())
            return null;
        return this.elements.get(this.size() - 1).getValue();
    }

    /**
     * access an element's xml representation
     * @param index
     * @return
     */
    public Element getElement(int index) {
        if ((index >= this.elements.size()) || (index < 0))
            return null;

        return this.elements.get(index).getValue();
    }

    /**
     * access an element via its xml:id string
     * @param id
     * @return
     */
    public Element getElementByID(String id) {
        int index = this.getElementIndexByID(id);
        if (index < 0)
            return null;
        return this.elements.get(index).getValue();
    }

    /**
     * get the index of an element in the map via its xml:id string
     * @param id
     * @return the index of the element or -1 if there not in the map
     */
    public int getElementIndexByID(String id) {
        for (int i = 0; i < this.size(); ++i) {
            Element e = this.elements.get(i).getValue();
            Attribute a = Helper.getAttribute("id", e);
            if ((a != null) && a.getValue().equals(id))
                return i;
        }
        return -1;
    }

    /**
     * access the element that is before or at the specified date
     * @param date
     * @return the element or null if none can be found before the date
     */
    public Element getElementBeforeAt(double date) {
        int index = this.getElementIndexBeforeAt(date);
        return (index < 0) ? null : this.elements.get(index).getValue();

        // a less efficient search method
//        for (int i = this.elements.size() - 1; i >= 0; --i) {
//            if (this.elements.get(i).getKey() <= date)
//                return this.elements.get(i).getValue();
//        }
//        return null;
    }

    /**
     * access the first element that comes after the specified date
     * @param date
     * @return the element or null if none can be found after the date
     */
    public Element getElementAfter(double date) {
        int index = this.getElementIndexAfter(date);
        return (index < 0) ? null : this.elements.get(index).getValue();

        // a less efficient search method
//        for (int i = 0; i < this.elements.size(); ++i) {
//            if (this.elements.get(i).getKey() > date)
//                return this.elements.get(i).getValue();
//        }
//        return null;
    }

    /**
     * get the index of the last element before or at the specified date (binary search)
     * @param date
     * @return the index or null if there is no element before or at the date
     */
    public int getElementIndexBeforeAt(double date) {
        if (this.elements.isEmpty() || (this.elements.get(0).getKey() > date))  // if there is nothing or it starts after the date
            return -1;                                                        // nothing

        if (this.elements.get(this.elements.size()-1).getKey() <= date)         // if the last element is before or at the date (this case will occur relatively often due to the way maps are filled)
            return this.elements.size()-1;                                      // return the last element index

        // binary search
        int first = 0;
        int last = this.elements.size() - 1;
        int mid = last / 2;
        while (first <= last) {
            if (this.elements.get(mid + 1).getKey() <= date)
                first = mid + 1;
            else if (this.elements.get(mid).getKey() <= date)
                return mid;
            else
                last = mid - 1;
            mid = (first + last) / 2;
        }
        return -1;
    }

    /**
     * get the index of the last element before (not at!) the specified date (binary search)
     * @param date
     * @return
     */
    public int getElementIndexBefore(double date) {
        if (this.elements.isEmpty() || (this.elements.get(0).getKey() >= date)) // if there is nothing or it starts at or after the date
            return -1;                                                          // nothing

        if (this.elements.get(this.elements.size()-1).getKey() < date)          // if the last element is before the date (this case will occur relatively often due to the way maps are filled)
            return this.elements.size()-1;                                      // return the last element index

        // binary search
        int first = 0;
        int last = this.elements.size() - 1;
        int mid = last / 2;
        while (first <= last) {
            if (this.elements.get(mid).getKey() >= date)
                last = mid;
            else if (this.elements.get(mid + 1).getKey() >= date)
                return mid;
            else
                first = mid + 1;
            mid = (first + last) / 2;
        }
        return -1;
    }

    /**
     * get the index of the first element after the specified date (binary search)
     * @param date
     * @return the index or null if there is no element after the date
     */
    public int getElementIndexAfter(double date) {
        if (this.elements.isEmpty() || (this.elements.get(this.elements.size()-1).getKey() <= date))    // if there is nothing after the date
            return -1;                                                                                // done

        if (this.elements.get(0).getKey() > date)       // if the first element is already after date
            return 0;                                   // return 0

        // binary search
        int first = 0;
        int last = this.elements.size() - 1;
        int mid = last / 2;
        while (first <= last) {
            if (this.elements.get(mid).getKey() > date)
                last = mid - 1;
            else if (this.elements.get(mid + 1).getKey() > date)
                return mid + 1;
            else
                first = mid + 1;
            mid = (first + last) / 2;
        }
        return -1;
    }

    /**
     * get the index of the first element at or after the specified date (binary search)
     * @param date
     * @return the index or null if there is no element at or after the date
     */
    public int getElementIndexAtAfter(double date) {
        if (this.elements.isEmpty() || (this.elements.get(this.elements.size()-1).getKey() < date))     // if there is nothing at or after the date
            return -1;                                                                                  // done

        if (this.elements.get(0).getKey() >= date)      // if the first element is already at or after date
            return 0;                                   // return 0

        // binary search
        int first = 0;
        int last = this.elements.size() - 1;
        int mid = last / 2;
        while (first <= last) {
            if (this.elements.get(mid).getKey() >= date)
                last = mid - 1;
            else if (this.elements.get(mid + 1).getKey() >= date)
                return mid + 1;
            else
                first = mid + 1;
            mid = (first + last) / 2;
        }
        return -1;
    }

    /**
     * insert the element in the map
     * @param xml it must be non-null and contain an attribute date
     * @return the index at which it has been inserted or -1 if insertion failed
     */
    public int addElement(Element xml) {
        if (xml == null) {
            System.err.println("Cannot add the Element to GenericMap. XML Element is null.");
            return -1;
        }

        Attribute dateAtt = xml.getAttribute("date");
        if (dateAtt == null) {
            System.err.println("Cannot add the Element to GenericMap. Missing attribute 'date'.");
            return -1;
        }

        if (xml.getLocalName().equals("style") && (xml.getAttribute("name.ref") == null)) {   // if it is a style switch but has no name.ref attribute
            System.err.println("Cannot add the Element to GenericMap. Attribute name.ref is mandatory for style elements but missing.");
            return -1;
        }

        double date = Double.parseDouble(dateAtt.getValue());
        KeyValue<Double, Element> e = new KeyValue<>(date, xml);
        return this.insertElement(e, false);
    }

    /**
     * insert the specified map element at the right position, the element should be constructed in method addElement()
     * @param element a key value pair
     * @param firstAtDate set this true so the element will be placed before all other elements at the same date, false to be added as the last element at this date
     * @return the index at which it has been inserted
     */
    protected int insertElement(KeyValue<Double, Element> element, boolean firstAtDate) {
        if (firstAtDate) {
            for (int i = 0; i < this.elements.size(); ++i) {                // go through all elements in the map so far
                if (this.elements.get(i).getKey() >= element.getKey()) {    // found the one that will be directly after the new element
                    this.elements.add(i, element);                          // insert the new element at the corresponding index
                    this.getXml().insertChild(element.getValue(), i);       // insert it also in the xml at the right position
                    return i;                                               // return the index
                }
            }
        } else {
            for (int i = this.elements.size() - 1; i >= 0; --i) {           // go through all elements in the map so far
                if (this.elements.get(i).getKey() <= element.getKey()) {    // found the one that will be directly before the new element
                    int index = i + 1;
                    this.elements.add(index, element);                      // insert the new element at the corresponding index
                    this.getXml().insertChild(element.getValue(), index);   // insert it also in the xml at the right position
                    return index;                                           // return the index
                }
            }
        }
        this.elements.add(0, element);                                      // if the map is empty or its elements are all after the date of the new element, insert the element at the front
        this.getXml().insertChild(element.getValue(), 0);                   // insert it also at the front of the xml
        return 0;                                                           // return the index
    }

    /**
     * insert the specified map element at the right position, the element should be constructed in method addElement(),
     * the element will be added after other elements at the same date
     * @param element a key value pair
     * @return the index at which it has been inserted
     */
    protected int insertElement(KeyValue<Double, Element> element) {
        return this.insertElement(element, false);
    }

    /**
     * remove the map entry at the specified index
     * @param index
     */
    public void removeElement(int index) {
        if (index >= this.elements.size())
            return;

        Element e = this.elements.get(index).getValue();
        this.getXml().removeChild(e);
        this.elements.remove(index);
    }

    /**
     * remove the map entry that holds the specified xml element
     * @param xml
     */
    public void removeElement(Element xml) {
        for (KeyValue<Double, Element> e : this.elements) {
            if (e.getValue() == xml) {
                this.getXml().removeChild(xml);
                this.elements.remove(e);
                return;
            }
        }
    }

    /**
     * this method generates a style switch (an MPM style element) and adds it to the map
     * @param date
     * @param styleName a reference to a styleDef
     * @return the index at which it has been inserted
     */
    public int addStyleSwitch(double date, String styleName) {
        Element e = new Element("style", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("name.ref", styleName));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, true);
    }

    /**
     * get the style name for the specified date
     * @param date
     * @return
     */
    public String getStyleNameAt(double date) {
        ArrayList<KeyValue<Double, Element>> list = this.getAllElementsOfType("style");
        for (int i = list.size() - 1; i >= 0; --i) {
            if (list.get(i).getKey() <= date) {
                return Helper.getAttributeValue("name.ref", list.get(i).getValue());
            }
        }
        return null;
    }

    /**
     * this method retrieves the styleDef from the local or global header, and returns the result
     * @param styleType
     * @param styleName
     * @return the style or null
     */
    public GenericStyle getStyle(String styleType, String styleName) {
        if ((styleName == null) || styleName.isEmpty())
            return null;

        GenericStyle style = null;
        if (this.getLocalHeader() != null)
            style = this.getLocalHeader().getStyleDef(styleType, styleName);
        if ((style == null) && (this.getGlobalHeader() != null))
            style = this.getGlobalHeader().getStyleDef(styleType, styleName);

        return style;
    }

    /**
     * retrieve the style definition of a specific type that applies to a specific time position
     * @param date
     * @param styleType
     * @return a GenericStyle object or null
     */
    public GenericStyle getStyleAt(double date, String styleType) {
        String styleName = this.getStyleNameAt(date);
        return this.getStyle(styleType, styleName);
    }

    /**
     * get the number of entries in the map
     * @return
     */
    public int size() {
        return this.elements.size();
    }

    /**
     * Is the map empty?
     * @return
     */
    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    /**
     * apply the specified MSM sequencingMap to this map; this replaces the current map by the expanded one
     * @param sequencingMap an MSM sequencingMap element
     * @return success
     */
    public boolean applySequencingMap(Element sequencingMap) {
        HashMap<String, String> repetitionIDs = new HashMap<>();
        Element newMap = Msm.applySequencingMapToMap(sequencingMap, this.getXml(), repetitionIDs);
        if (newMap == null)                                         // the sequencingMap was empty, hence, no changes to this map
            return true;                                            // we are done

        // copy the old state in case the parsing of the new map fails
        ArrayList<KeyValue<Double, Element>> saveCopy = (ArrayList<KeyValue<Double, Element>>) this.elements.clone();
        Element parent = (Element) this.getXml().getParent();       // get the parent xml element
        parent.appendChild(newMap);                                 // integrate the new map into the xml tree
        Element oldXmlData = this.getXml();

        try {
            this.parseData(newMap);                                 // parse the new map
        } catch (Exception e) {                          // if this fails restore the old state
            e.printStackTrace();
            this.setXml(oldXmlData);
            this.elements = saveCopy;
            return false;
        }

        parent.removeChild(oldXmlData);                             // remove the old map from the xml tree
        return true;
    }

    /**
     * This method iterates through all elements of the map. If they have an attribute of the specified name, its value will be replaced according to the hashmap's mappings (current value -&gt; new value). If the current value does not appear in the hashmap it is left unaltered.
     * @param attributeName
     * @param valueMappings
     */
    public void updateAttributeValues(String attributeName, HashMap<String, String> valueMappings) {
        for (KeyValue<Double, Element> e : this.elements) {                 // iterate through all elements
            Attribute a = Helper.getAttribute(attributeName, e.getValue()); // find the attribute
            if (a == null)                                                  // if it does not have such an attribute
                continue;                                                   // continue with the next element

            String newValue = valueMappings.get(a.getValue());              // get its new value
            if (newValue != null)                                           // if there is a mapping to update it, the new value is != null
                a.setValue(newValue);                                       // set the new value
        }
    }
}
