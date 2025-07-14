package meico.mei;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory;
import meico.mpm.elements.maps.GenericMap;
import net.sf.saxon.s9api.*;
import net.sf.saxon.s9api.Serializer;
import nu.xom.*;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * This class is used for mei to msm conversion to hold temporary data, used in class Mei.
 * It contains functionality that is also useful in other (mostly XML-related) contexts.
 * @author Axel Berndt.
 */
public class Helper {
    /**
     * This method validates a file against a schema. If the validation fails it throws an exception.
     * @param file
     * @param schema
     * @throws SAXException
     * @throws IOException
     */
    public static void validateAgainstSchema(File file, URL schema) throws SAXException, IOException {
        (new XMLSyntaxSchemaFactory()).newSchema(schema).newValidator().validate(new StreamSource(file));  // create a new validator with the schema and validate the file, if this fails, it throws an exception
    }

    /**
     * This method validates an xml string against a schema. If the validation fails it throws an exception.
     * @param xml
     * @param schema
     * @throws SAXException
     * @throws IOException
     */
    public static void validateAgainstSchema(String xml, URL schema) throws SAXException, IOException {
        (new XMLSyntaxSchemaFactory()).newSchema(schema).newValidator().validate(new StreamSource(new StringReader(xml)));  // create a new validator with the schema and validate the file, if this fails, it throws an exception
    }

    /**
     * get the first child of an xml element
     * @param ofThis
     * @return
     */
    public static Element getFirstChildElement(Element ofThis) {
        if (ofThis == null)
            return null;

        Elements es = ofThis.getChildElements();
        if (es.size() == 0)
            return null;

        return es.get(0);
    }

    /**
     * XOM's method getFirstChild(String) sometimes doesn't seem to work even though an XPath query finds something. For these situations this method can be used as workaround.
     * @param ofThis
     * @param localname
     * @return
     */
    public static Element getFirstChildElement(Element ofThis, String localname) {
        if ((ofThis == null) || localname.isEmpty())
            return null;
        Nodes e = ofThis.query("child::*[local-name()='" + localname + "']");   // find the elements with the localname by an XPath query
        if (e.size() == 0) return null;                                         // if nothing found, return null
        return (Element)e.get(0);                                               // else return the first element
    }

    /**
     * this function became necessary because the XOM methods sometimes do not seem to work for whatever reason
     * @param name
     * @param ofThis
     * @return
     */
    public static Element getFirstChildElement(String name, Element ofThis) {
        if (ofThis == null)
            return null;

        for (int i=0; i < ofThis.getChildElements().size(); ++i) {
            if (ofThis.getChildElements().get(i).getLocalName().equals(name)) {
                return ofThis.getChildElements().get(i);
            }
        }
        return null;
    }

    /**
     * this method is an alternative to XOM's getChildElements(String name) which sometimes doesn't seem to work
     * @param name
     * @param ofThis
     * @return
     */
    public static LinkedList<Element> getAllChildElements(String name, Element ofThis) {
        if ((ofThis == null) || name.isEmpty())
            return null;
        Nodes e = ofThis.query("child::*[local-name()='" + name + "']");   // find the elements with the localname by an XPath query
        LinkedList<Element> es = new LinkedList<>();
        for (int i = 0; i < e.size(); ++i)
            es.add((Element)e.get(i));
        return es;
    }

    /**
     * Create a flat list of all descendants of a certain name (beginning with ofThis)
     * @param name
     * @param ofThis
     * @return
     */
    public static LinkedList<Element> getAllDescendantsByName(String name, Element ofThis) {
        if ((ofThis == null) || name.isEmpty())
            return null;
        LinkedList<Element> children = new LinkedList<>();
        for(Element ch : Helper.getAllChildElements(ofThis)) {
            if(ch.getLocalName().equals(name))
                children.add(ch);
            children.addAll(Helper.getAllDescendantsByName(name, ch));
        }
        return children;
    }

    /**
     * Create a flat list of all descendants with a certain attribute (beginning with ofThis)
     * @param attrName
     * @param ofThis
     * @return
     */
    public static LinkedList<Element> getAllDescendantsWithAttribute(String attrName, Element ofThis) {
        if ((ofThis == null) || attrName.isEmpty())
            return null;
        LinkedList<Element> children = new LinkedList<>();
        for(Element ch : Helper.getAllChildElements(ofThis)) {
            if(ch.getAttribute(attrName) != null)
                children.add(ch);
            children.addAll(Helper.getAllDescendantsWithAttribute(attrName, ch));
        }
        return children;
    }

    /**
     * this method is an alternative to XOM's getChildElements() which sometimes doesn't seem to work
     * @param ofThis
     * @return
     */
    public static LinkedList<Element> getAllChildElements(Element ofThis) {
        if (ofThis == null)
            return null;
        Nodes e = ofThis.query("child::*");   // find all children ofThis
        LinkedList<Element> es = new LinkedList<>();
        for (int i = 0; i < e.size(); ++i)
            es.add((Element)e.get(i));
        return es;
    }

    /**
     * get the next sibling element of ofThis irrespective of its name
     * @param ofThis
     * @return
     */
    public static Element getNextSiblingElement(Element ofThis) {
        if (ofThis == null)
            return null;

        if (ofThis == ofThis.getDocument().getRootElement())                // if we are at the root of the document
            return null;                                                    // there can be no siblings, hence return null

        int index = ofThis.getParent().indexOf(ofThis);
        if (index >= (ofThis.getParent().getChildCount() - 1))
            return null;

        return (Element) ofThis.getParent().getChild(index + 1);
    }

    /**
     * get the next sibling element of ofThis with the given name
     * @param name
     * @param ofThis
     * @return
     */
    public static Element getNextSiblingElement(String name, Element ofThis) {
        if (ofThis == null)
            return null;

        if (ofThis == ofThis.getDocument().getRootElement())                // if we are at the root of the document
            return null;                                                    // there can be no siblings, hence return null

        Elements es = ((Element)ofThis.getParent()).getChildElements();     // get a list of all siblings
        Element candidate = null;                                           // when going from backward to forward through the list, the elements' name is checked. If it matches with the search name it can be a result candidate

        for (int i = es.size()-1; i >= 0; --i) {                            // go through the list from backward to forward
            if (es.get(i) == ofThis) {                                      // ofThis found
                return candidate;                                           // return the candidate (null if we did not pass an element with a matching name so far)
            }
            if (es.get(i).getLocalName().equals(name)) {                    // found an element with a matching name
                candidate = es.get(i);                                      // keep it as return candidate
            }
        }

        return null;                                                        // ofThis is the final element and has no next sibling
    }

    /**
     * get the previous sibling element of ofThis irrespective of its name
     * @param ofThis
     * @return
     */
    public static Element getPreviousSiblingElement(Element ofThis) {
        if (ofThis == null)
            return null;

        if (ofThis == ofThis.getDocument().getRootElement())                // if we are at the root of the document
            return null;                                                    // there can be no siblings, hence return null

        int index = ofThis.getParent().indexOf(ofThis);
        if (index == 0)
            return null;

        return (Element) ofThis.getParent().getChild(index - 1);
    }

    /**
     * get the previous sibling element of ofThis with a specific name
     * @param name
     * @param ofThis
     * @return
     */
    public static Element getPreviousSiblingElement(String name, Element ofThis) {
        if (ofThis == null)
            return null;

        if (ofThis == ofThis.getDocument().getRootElement())                // if we are at the root of the document
            return null;                                                    // there can be no siblings, hence return null

        Elements es = ((Element)ofThis.getParent()).getChildElements();     // get a list of all siblings
        Element candidate = null;

        for (int i=0; i < es.size(); ++i) {                                 // go through all siblings starting at the second (the first cannot have a predecessor)
            if (ofThis == es.get(i)) {                                      // if ofThis was found
                return candidate;                                           // the predecessor is the previous sibling
            }
            if (es.get(i).getLocalName().equals(name)) {                    // found an element with a matching name
                candidate = es.get(i);                                      // keep it as return candidate
            }
        }

        return null;                                                        // ofThis is the final element and has no next sibling
    }

    /**
     * Get all previous element Siblings up to ofThis parent of a specific name.
     * List is in order of distance to ofThis.
     * @param name
     * @param ofThis
     * @return
     */
    public static ArrayList<Element> getAllPreviousSiblingElements(String name, Element ofThis) {
        Elements allSiblings = ((Element) ofThis.getParent()).getChildElements();
        ArrayList<Element> prevSiblings = new ArrayList<>();
        for (int i = 0; i < allSiblings.size(); ++i) {
            Element sib = allSiblings.get(i);
            if (!sib.getLocalName().equals(name))   // if not the right name
                continue;                           // continue with the next
            if (sib == ofThis)                      // if we reached the pivot element ofThis
                break;                              // done, all further elements are not previous
            prevSiblings.add(sib);                  // found an element, add it to the list
        }
        return prevSiblings;
    }

    /**
     * this method adds element addThis to a timely sequenced list, the map, and ensures the timely order of the elements in the map;
     * therefore, addThis must contain the attribute "date"; if not, addThis is appended at the end
     * @param addThis an xml element (should have an attribute date)
     * @param map a timely sequenced list of elements with attribute date
     * @return the index of the element in the map or -1 if insertion failed
     */
    public static int addToMap(Element addThis, Element map) {
        if ((map == null) || (addThis == null))                                     // no map or no element to insert
            return -1;                                                              // no insertion

        if (addThis.getAttribute("date") == null) {                                 // no attribute date
            map.appendChild(addThis);                                               // simply append addThis to the end of the map
            return map.getChildCount()-1;                                           // and return the index
        }

        Nodes es = map.query("descendant::*[attribute::date]");                     // get all elements in the map that have an attribute date
        if (es.size() == 0) {                                                       // if there are no elements in the map with a date attribute
            map.appendChild(addThis);                                               // simply append addThis to the end of the map
            return map.getChildCount()-1;                                           // and return the index
        }

        double date = Double.parseDouble(addThis.getAttributeValue("date"));        // get the date of addThis
        for (int i = es.size()-1; i >= 0; --i) {                                    // go through the elements
            if (Double.parseDouble(((Element)es.get(i)).getAttributeValue("date")) <= date) {  // if the element directly before date is found
                int index = map.indexOf(es.get(i));                                 // get the index of the element just found
                map.insertChild(addThis, ++index);                                  // insert addThis right after the element
                return index;                                                       // return the index
            }
        }

        // if all elements in the map had a date later than addThis's date
        map.insertChild(addThis, 0);                                                // insert addThis at the front of the map (as first child)
        return 0;                                                                   // return the index
    }

    /**
     * this method parses an input string, extracts all integer substrings and returns them as a list of integers
     * @param string
     * @return
     */
    public static ArrayList<Integer> extractAllIntegersFromString(String string) {
        string.replaceAll(" bis ", " -");
        string.replaceAll(" to ", " -");
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(string);

        ArrayList<Integer> results = new ArrayList<>();
        while (m.find()) {
            results.add(Integer.parseInt(m.group()));
        }
        return results;                                         // return the resulting list of integers
    }

    /**
     * create a flat copy of element e including its attributes but not its child elements
     * @param e
     * @return
     */
    public static Element cloneElement(Element e) {
        if (e == null) return null;

        Element clone = new Element(e.getLocalName());
        clone.setNamespaceURI(e.getNamespaceURI());
        for (int i = e.getAttributeCount()-1; i >= 0; --i) {
            clone.addAttribute(new Attribute(e.getAttribute(i).getLocalName(), e.getAttribute(i).getValue()));
        }

        return clone;
    }

    /**
     * returns the attribute with the specified name contained in ofThis, or null if that attribute does not exist, namespace is ignored
     * @param name
     * @param ofThis
     * @return
     */
    public static Attribute getAttribute(String name, Element ofThis) {
        if (ofThis == null) return null;

        Attribute a = ofThis.getAttribute(name);
        if (a != null) return a;

        a = ofThis.getAttribute(name, ofThis.getNamespaceURI());
        if (a != null) return a;

        a = ofThis.getAttribute(name, "http://www.w3.org/XML/1998/namespace");
        if (a != null) return a;

        return null;
    }

    /**
     * returns the vale of attribute name in Element ofThis as String, or empty string if attribute does not exist, namespace is ignored
     * @param name
     * @param ofThis
     * @return
     */
    public static String getAttributeValue(String name, Element ofThis) {
        Attribute a = getAttribute(name, ofThis);
        if (a == null) return "";
        return a.getValue();
    }

    /**
     * Add a UUID-based xml:id to the specified element.
     * Caution: If the element has already an xml:id, it will be overwritten!
     * @param toThis
     * @return
     */
    public static String addUUID(Element toThis) {
        String uuid = "meico_" + UUID.randomUUID().toString();              // generate new id
        Attribute a = new Attribute("id", uuid);                            // create an attribute
        a.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");      // set its namespace to xml
        toThis.addAttribute(a);                                             // add attribute to the element
        return uuid;
    }

    /**
     * copies the id attribute ofThis into toThis
     * @param ofThis
     * @param toThis
     * @return the newly created attribute
     */
    public static Attribute copyId(Element ofThis, Element toThis) {
//        return copyIdNoNs(ofThis, toThis);
        return copyIdNs(ofThis, toThis);
    }

    /**
     * copies the id attribute from ofThis (if present) into toThis, without namespace binding
     * @param ofThis
     * @param toThis
     * @return the newly created attribute
     */
    public static Attribute copyIdNoNs(Element ofThis, Element toThis) {
        Attribute id = Helper.getAttribute("id", ofThis);
        if (id != null) {
            Attribute newId = new Attribute("id", id.getValue());
            toThis.addAttribute(newId);
            return newId;
        }
        return null;
    }

    /**
     * copies the id attribute from ofThis (if present) into toThis, retaining its namespace
     * @param ofThis
     * @param toThis
     * @return the newly created attribute
     */
    private static Attribute copyIdNs(Element ofThis, Element toThis) {
        Attribute id = Helper.getAttribute("id", ofThis);
        if (id != null) {
            Attribute newId = id.copy();
            toThis.addAttribute(newId);
            return newId;
        }
        return null;
    }

    /**
     * returns the parent element of ofThis as element or null
     * @param ofThis
     * @return
     */
    public static Element getParentElement(Element ofThis) {
        if (ofThis == null)
            return null;
        for (Node e = ofThis.getParent(); e != null; e = e.getParent()) {
            if (e instanceof Element) return (Element)e;
        }
        return null;
    }

    /**
     * determines if the child is in the subtree of parent
     * @param child
     * @param parent
     * @return
     */
    public static boolean isChildOf(Element child, Element parent) {
        if ((child == null) || (parent == null))
            return false;
        for (Element par = Helper.getParentElement(child); par != null; par = Helper.getParentElement(par)) {
            if (par == parent)
                return true;
        }
        return false;
    }

    /**
     * Returns the closest element of a certain name along the parent tree.
     * ofThis is not checked for name, since it cannot be a predecessor of itself.
     * @param name
     * @param ofThis
     * @return
     */
    public static Element getClosestParent(String name, Element ofThis){
        for (Element parent = Helper.getParentElement(ofThis); parent != null; parent = Helper.getParentElement(parent)) {
            if(parent.getLocalName().equals(name))
                return parent;
        }
        return null;
    }

    /**
     * Returns the closest element that contains a certain attribute name along the parent tree.
     * ofThis is not checked for the attribute, since it cannot be a predecessor of itself.
     * @param attrName
     * @param ofThis
     * @return
     */
    public static Element getClosestParentByAttribute(String attrName, Element ofThis){
        Element parent = Helper.getParentElement(ofThis);
        while(parent != null && !parent.equals(ofThis.getDocument().getRootElement())){
            String attr = Helper.getAttributeValue(attrName, parent);
            if(attr != null && !attr.isEmpty())
                return parent;
            parent = Helper.getParentElement(parent);
        }
        return null;
    }

    /**
     * When articulationMaps are expanded via GenericMap.applySequencingMap() the noteid attribute is not updated.
     * Therefor, we get a HashMap from Msm.resolveRepetitions() and apply it to the already expanded articulationMap via this method.
     * It is used in classes meico.app.gui.DataObject and meico.app.Main. At the moment of invoking this method the maps have been expanded and only the noteids need to be updated.
     * @param map
     * @param noteIdMappings
     */
    public static void updateMpmNoteidsAfterResolvingRepetitions(GenericMap map, HashMap<String, String> noteIdMappings) {
        for (Map.Entry<String, String> mapping : noteIdMappings.entrySet()) {                                   // for all mappings
            Nodes ns = map.getXml().query("descendant::*[attribute::noteid = '#" + mapping.getKey() + "']");    // get all elements with the noteid attribute and the specific value
            if (ns.size() < 2)                                                                                  // if there is none or only one
                continue;                                                                                       // no need to change that value, the first one keeps the original

            String current = mapping.getKey();                                          // this string will be set to the subsequent values: "originalID" -> "meico_repetition_1_originalID" -> "meico_repetition_2_originalID" -> and so on
//            System.out.println(ns.get(0).toXML());
            for (int i = 1; i < ns.size(); ++i) {                                       // iterate through the elements that refer to this noteid starting with the second (the first one keeps its original value)
                current = noteIdMappings.get(current);                                  // get the next value
                Attribute a = Helper.getAttribute("noteid", ((Element) ns.get(i)));     // get the attribute
                a.setValue("#" + current);                                              // set the attribute value
//                System.out.println(ns.get(i).toXML());
            }
//            System.out.println("");
        }
    }

    /**
     * convert the MEI duration string into decimal (e.g., 4 -&gt; 1/4) and returns the result
     * @param durString
     * @return
     */
    public static double meiDuration2decimal(String durString) {
        switch (durString) {
            case "maxima":return 8.0;
            case "long":  return 4.0;
            case "breve": return 2.0;
            case "1":     return 1.0;
            case "2":     return 0.5;
            case "4":     return 0.25;
            case "8":     return 0.125;
            case "16":    return 0.0625;
            case "32":    return 0.03125;
            case "64":    return 0.015625;
            case "128":   return 0.0078125;
            case "256":   return 0.00390625;
            case "512":   return 0.001953125;
            case "1024":  return 0.0009765625;
            case "2048":  return 0.00048828125;
        }
        return 0.0;
    }

    /**
     * convert MEI duration to MusicXML duration
     * @param durString
     * @return
     */
    public static String meiDuration2MusicxmlDuration(String durString) {
        switch (durString) {
            case "maxima":
            case "long":
            case "breve": return durString;
            case "1":     return "whole";
            case "2":     return "half";
            case "4":     return "quarter";
            case "8":     return "eighth";
            case "16":    return durString + "th";
            case "32":    return durString + "nd";
            case "64":
            case "128":
            case "256":   return durString + "th";
            case "512":   return durString + "nd";
            case "1024":
            case "2048":  return durString + "th";
        }
        return durString;
    }

    /**
     * convert a duration specified in pulses (based on ppq) to decimal format
     * @param pulses
     * @param ppq
     * @return
     */
    public static double pulseDuration2decimal(double pulses, int ppq) {
        return pulses / (ppq * 4.0);
    }

    /**
     * generate an HTML Unicode string with the note/rest value and dots according to the specified duration
     * @param duration
     * @param isRest
     * @return
     */
    public static String decimalDuration2HtmlUnicode(double duration, boolean isRest) {
        if (duration < 0.0078125)
            return isRest ? "rest" : "note";
        if (duration < 0.015625)
            return (isRest ? "&#119106;" : "&#119140;") + durationRemainder2UnicodeDots(0.0078125, duration - 0.0078125);
        if (duration < 0.03125)
            return (isRest ? "&#119105;" : "&#119139;") + durationRemainder2UnicodeDots(0.015625, duration - 0.015625);
        if (duration < 0.0625)
            return (isRest ? "&#119104;" : "&#119138;") + durationRemainder2UnicodeDots(0.03125, duration - 0.03125);
        if (duration < 0.125)
            return (isRest ? "&#119103;" : "&#119137;") + durationRemainder2UnicodeDots(0.0625, duration - 0.0625);
        if (duration < 0.25)
            return (isRest ? "&#119102;" : "&#119136;") + durationRemainder2UnicodeDots(0.125, duration - 0.125);
        if (duration < 0.5)
            return (isRest ? "&#119101;" : "&#119135;") + durationRemainder2UnicodeDots(0.25, duration - 0.25);
        if (duration < 1.0)
            return (isRest ? "&#119100;" : "&#119134;") + durationRemainder2UnicodeDots(0.5, duration - 0.5);
        if (duration < 2.0)
            return (isRest ? "&#119099;" : "&#119133;") + durationRemainder2UnicodeDots(1.0, duration - 1.0);
        if (duration < 4.0)
            return (isRest ? "2 &#119098;" : "&#119132;") + durationRemainder2UnicodeDots(2.0, duration - 2.0);
        if (duration < 8.0)
            return (isRest ? "4 &#119098;" : "&#119223;") + durationRemainder2UnicodeDots(4.0, duration - 4.0);
        if (duration == 8.0)
            return (isRest ? "8 &#119098;" : "&#119222;");
        else
            return isRest ? "rest" : "note";
    }

    /**
     * This is a helper method for decimalDuration2HtmlUnicode().
     * From a decimal duration value, take the undotted note value and the remainder. This method computes the number of dots and
     * @param undottedNoteValue
     * @param remainder
     * @return
     */
    private static String durationRemainder2UnicodeDots(double undottedNoteValue, double remainder) {
        String dots = "";
        double v = undottedNoteValue / 2.0;
        for (double r = remainder; (r >= v) && (r >= 0.0078125); v /= 2.0) {
            dots = dots.concat(".");
            r -= v;
        }
        return dots;
    }

    /**
     * compute the decimal value of the accidental (1 = 1 semitone)
     * @param accid the string to be converted
     * @return the decimal value of the accidental
     */
    public static double accidString2decimal(String accid) {
        double accidentals = 0.0;
        switch (accid) {
            case "s":    accidentals = 1;    break;
            case "f":    accidentals = -1;   break;
            case "ss":   accidentals = 2;    break;
            case "x":    accidentals = 2;    break;
            case "ff":   accidentals = -2;   break;
            case "xs":   accidentals = 3;    break;
            case "ts":   accidentals = 3;    break;
            case "tf":   accidentals = -3;   break;
            case "n":    break;
            case "nf":   accidentals = -1;   break;
            case "ns":   accidentals = 1;    break;
            case "su":   accidentals = 1.5;  break;
            case "sd":   accidentals = 0.5;  break;
            case "fu":   accidentals = -0.5; break;
            case "fd":   accidentals = -1.5; break;
            case "nu":   accidentals = 0.5;  break;
            case "nd":   accidentals = -0.5; break;
            case "1qf":  accidentals = -0.5; break;
            case "3qf":  accidentals = -1.5; break;
            case "1qs":  accidentals = 0.5;  break;
            case "3qs":  accidentals = 1.5;  break;
        }
        return accidentals;
    }

    /**
     * Convert a decimal accidental to an MEI accidental string
     * @param accid
     * @return
     */
    public static String accidDecimal2String(double accid) {
        if (accid == 1.0)
            return "s";
        if (accid == -1.0)
            return "f";
        if (accid == 2.0)
            return "ss";
        if (accid == -2.0)
            return "ff";
        if (accid == 3.0)
            return "xs";
        if (accid == -3.0)
            return "tf";
        if (accid == 0.0)
            return "n";
        if (accid == -0.5)
            return "1qf";
        if (accid == -1.5)
            return "3qf";
        if (accid == 0.5)
            return "1qs";
        if (accid == 1.5)
            return "3qs";
        return "";
    }

    /**
     * convert an MEI accid string to a MusicXML accid string
     * @param accid
     * @return
     */
    public static String meiAccid2MusicxmlAccid(String accid){
        String accidental = "";
        switch (accid) {
            case "s":    accidental = "sharp";    break;
            case "f":    accidental = "flat";   break;
            case "ss":   accidental = "sharp-sharp";    break;
            case "x":    accidental = "double-sharp";    break;
            case "ff":   accidental = "flat-flat";   break;
            case "xs":
            case "ts":   accidental = "triple-sharp";    break;
            case "tf":   accidental = "triple-flat";   break;
            case "n":    accidental = "natural"; break;
            case "nf":   accidental = "natural-flat";   break;
            case "ns":   accidental = "natural-sharp";    break;
            case "su":   accidental = "sharp-up";  break;
            case "sd":   accidental = "sharp-down";  break;
            case "fu":   accidental = "flat-up"; break;
            case "fd":   accidental = "flat-down"; break;
            case "nu":   accidental = "natural-up";  break;
            case "nd":   accidental = "natural-down"; break;
            case "1qf":  accidental = "quarter-flat"; break;
            case "3qf":  accidental = "three-quarters-flat"; break;
            case "1qs":  accidental = "quarter-sharp";  break;
            case "3qs":  accidental = "three-quarters-sharp";  break;
        }
        return accidental;
    }

    /**
     * compute the string value of accidental decimal value (1 = 1 semitone)
     * @param accid double value of accidental
     * @return the string value of the accidental
     */
    public static String accidDecimal2unicodeString(double accid) {
        if (accid == 0.0) {
            return "";
        } else if (accid == 1.0) {
            return "&#9839;";
        } else if (accid == -1.0) {
            return "&#9837;";
        } else if (accid == 2.0) {
            return "&#119082;";
        } else if (accid == -2.0) {
            return "&#119083;";
        } else if (accid == 3.0) {
            return "&#119082;&#9839;";
        } else if (accid == -3.0) {
            return "&#9837;&#9837;&#9837;";
        } else if (accid == 1.5) {
            return "&#119088;";
        } else if (accid == 0.5) {
            return "&#119090;";
        } else if (accid == -0.5) {
            return "&#119091;";
        } else if (accid == -1.5) {
            return "&#119085;";
        }
        return "?";
    }

    /**
     * converts an mei pname to a midi pitch number in the first midi octave
     * @param pname the pname string
     * @return the midi pitch number in the first midi octave (one octave below the first MEI CMN octave)
     */
    public static double pname2midi(String pname) {
        switch (pname) {      // get value of attribute (first character of the array, it hopefully has only one character!)
            case "b#":
            case "B#":
            case "bs":
            case "Bs":
            case "c":
            case "C":  return 0.0;
            case "c#":
            case "C#":
            case "cs":
            case "Cs":
            case "db":
            case "Db":
            case "df":
            case "Df": return 1.0;
            case "d":
            case "D":  return 2.0;
            case "d#":
            case "D#":
            case "ds":
            case "Ds":
            case "eb":
            case "Eb":
            case "ef":
            case "Ef": return 3.0;
            case "fb":
            case "Fb":
            case "ff":
            case "Ff":
            case "e":
            case "E":  return 4.0;
            case "e#":
            case "E#":
            case "es":
            case "Es":
            case "f":
            case "F":  return 5.0;
            case "f#":
            case "F#":
            case "fs":
            case "Fs":
            case "gb":
            case "Gb":
            case "gf":
            case "Gf": return 6.0;
            case "g":
            case "G":  return 7.0;
            case "g#":
            case "G#":
            case "gs":
            case "Gs":
            case "ab":
            case "Ab":
            case "af":
            case "Af": return 8.0;
            case "a":
            case "A":  return 9.0;
            case "cb":
            case "Cb":
            case "cf":
            case "Cf":
            case "b":
            case "B":  return 11.0;
            default:   return -1.0;
        }
    }

    /**
     * converts a midi pitch value to a pitch name string (which inclused enharmonic equivalents)
     * @param midipitch the midi pitch value
     * @return the pitch name string
     */
    public static String midi2pname(double midipitch) {
        int pitchclass = (int)Math.round(midipitch % 12.0);
        switch (pitchclass) {
            case 0:  return "C";
            case 1:  return "C# Db";    // Never change these enharmonic writings, method midiPitch2Mei() relys on this!
            case 2:  return "D";
            case 3:  return "D# Eb";
            case 4:  return "E";
            case 5:  return "F";
            case 6:  return "F# Gb";
            case 7:  return "G";
            case 8:  return "G# Ab";
            case 9:  return "A";
            case 10: return "A# Bb";
            case 11: return "B";
            default: return "";
        }
    }

    /**
     * convert a midi pitch value to a pitch name string witout accidental, the accidental will be ecoded in a separate string;
     * this method is used during MIDI to MSM conversion in class meico.midi.Midi2MSMConverter
     * @param useSharpInsteadOfFlat use sharp or flat for accidental?
     * @param midipitch the midi pitch value
     * @param pnameAccid the output pitch name without accidental, the output accidental string (MSM style)
     */
    public static void midi2PnameAndAccid(boolean useSharpInsteadOfFlat, double midipitch, String[] pnameAccid) {
        if (pnameAccid.length < 2) {
            System.err.println("Error in method meico.mei.Helper.midi2PnameAndAccid: Array length of pnameAccid should be at least 2.");
            return;
        }

        int pitchclass = (int)Math.round(midipitch % 12.0);
        switch (pitchclass) {
            case 0:
                pnameAccid[0] = "C";
                pnameAccid[1] = "0.0";
                return;
            case 1:
                if (useSharpInsteadOfFlat) {
                    pnameAccid[0] = "C";
                    pnameAccid[1] = "1.0";
                }
                else {
                    pnameAccid[0] = "D";
                    pnameAccid[1] = "-1.0";
                }
                return;
            case 2:
                pnameAccid[0] = "D";
                pnameAccid[1] = "0.0";
                return;
            case 3:
                if (useSharpInsteadOfFlat) {
                    pnameAccid[0] = "D";
                    pnameAccid[1] = "1.0";
                }
                else {
                    pnameAccid[0] = "E";
                    pnameAccid[1] = "-1.0";
                }
                return;
            case 4:
                pnameAccid[0] = "E";
                pnameAccid[1] = "0.0";
                return;
            case 5:
                pnameAccid[0] = "F";
                pnameAccid[1] = "0.0";
                return;
            case 6:
                if (useSharpInsteadOfFlat) {
                    pnameAccid[0] = "F";
                    pnameAccid[1] = "1.0";
                }
                else {
                    pnameAccid[0] = "G";
                    pnameAccid[1] = "-1.0";
                }
                return;
            case 7:
                pnameAccid[0] = "G";
                pnameAccid[1] = "0.0";
                return;
            case 8:
                if (useSharpInsteadOfFlat) {
                    pnameAccid[0] = "G";
                    pnameAccid[1] = "1.0";
                }
                else {
                    pnameAccid[0] = "A";
                    pnameAccid[1] = "-1.0";
                }
                return;
            case 9:
                pnameAccid[0] = "A";
                pnameAccid[1] = "0.0";
                return;
            case 10:
                if (useSharpInsteadOfFlat) {
                    pnameAccid[0] = "A";
                    pnameAccid[1] = "1.0";
                }
                else {
                    pnameAccid[0] = "B";
                    pnameAccid[1] = "-1.0";
                }
                return;
            case 11:
                pnameAccid[0] = "B";
                pnameAccid[1] = "0.0";
                return;
            default:
                pnameAccid[0] = "";
                pnameAccid[1] = "";
        }
    }

    /**
     * Map midi pitch to octave.
     * @param midiPitch
     * @return
     */
    public static int midi2Octave(double midiPitch){
        return (int) Math.floor(midiPitch / 12.0) - 1;
    }

    /**
     * convert MIDI pitch number to MEI strings
     * @param midiPitch
     * @param useSharpInsteadOfFlat
     * @return a String array with [pname, accidental, octave]
     */
    public static String[] midiPitch2Mei(double midiPitch, boolean useSharpInsteadOfFlat) {
        String pnameWithAcc = Helper.midi2pname(midiPitch);

        // pname and accidental
        String pname;
        String acc = "";
        if (pnameWithAcc.length() < 2)
            pname = pnameWithAcc;
        else {                                              // if the string is longer than 1 character, it contains a # for the first and a b for the enharmonic second in pname
            if (useSharpInsteadOfFlat) {                    // use the first version
                pname = pnameWithAcc.substring(0, 1);
                acc = "s";
            } else {                                        // use the enharmonic counterpart
                pname = pnameWithAcc.substring(3, 4);
                acc = "f";
            }
        }
        pname = pname.toLowerCase();

        // octave
        String oct = Integer.toString(Helper.midi2Octave(midiPitch));

        return new String[]{pname, acc, oct};
    }

    /**
     * just a little helper method to separate the filename from the extension
     * @param filename filename string incl. extension (may include the complete path)
     * @return filename/path without extension
     */
    public static String getFilenameWithoutExtension(String filename){
        int i = filename.lastIndexOf('.');

        if (i == 0)
            return filename;

        return filename.substring(0, i);
    }

    /**
     * writes the mup document to a file (filename should include the path and the extension .mup)
     *
     * @param filename the filename string; it should include the path and the extension .mup
     * @return true if success, false if an error occured
     */
    public static boolean writeStringToFile(String string, String filename) {
        if (string == null) {
            System.err.println("String undefined!");
            return false;
        }

        if (filename == null) {
            System.err.println("Filename undefined!");
            return false;
        }

        // create the file in the file system
        File file = new File(filename);
        file.getParentFile().mkdirs();                              // ensure that the directory exists
        try {
            file.createNewFile();                                   // create the file if it does not already exist
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        try( PrintWriter out = new PrintWriter(filename)  ){
            out.println(string);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * a helper method to perform XSL transforms
     * @param input the in input xml document
     * @param xslt the XSLT stylesheet
     * @return the output Document of the transform or null if output not contains a single root or stylesheet error occurs
     */
    public synchronized static Document xslTransformToDocument(Document input, File xslt) {
        String xml = Helper.xslTransformToString(input.toXML(), xslt);
        assert xml != null;

        Builder builder = new Builder(false);   // we leave the validate argument false
        try {
            return builder.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ParsingException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * a helper method to perform XSL transforms
     * @param input the in input xml document
     * @param transformer the XSLT stylesheet
     * @return the output Document of the transform or null if output not contains a single root or stylesheet error occurs
     */
    public synchronized static Document xslTransformToDocument(Document input, Xslt30Transformer transformer) {
        String xml = Helper.xslTransformToString(input.toXML(), transformer);
        assert xml != null;

        Builder builder = new Builder(false);   // we leave the validate argument false
        try {
            return builder.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ParsingException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * a helper method to perform XSL transforms
     * @param input the in input xml document
     * @param xslt the XSLT stylesheet
     * @return the output string (null in case of an error)
     */
    public synchronized static String xslTransformToString(Document input, File xslt) {
        return Helper.xslTransformToString(input.toXML(), xslt);
    }

    /**
     * a helper method to perform XSL transforms
     * @param input the in input xml document
     * @param transformer the XSLT stylesheet
     * @return the output string (null in case of an error)
     */
    public synchronized static String xslTransformToString(Document input, Xslt30Transformer transformer) {
        return Helper.xslTransformToString(input.toXML(), transformer);
    }

    public synchronized static String xslTransformToString(String input, Xslt30Transformer transformer) {
        StreamSource source = new StreamSource(new StringReader(input));    // get the input string in a StreamSource
        StringWriter output = new StringWriter();                           // the output goes into a StringWriter
        Processor processor = new Processor(false);                         // we need a Processor
        Serializer destination = processor.newSerializer(output);           // the transformer need an instance of Destination to output, here we use a Serializer as destination and it outputs into the StringWriter
        try {
            transformer.applyTemplates(source, destination);                // do the transform
        } catch (SaxonApiException e) {
            e.printStackTrace();
            return null;
        }
        return output.toString();                                           // return the buffer of the StringWriter as String
    }

    /**
     * a helper method to perform XSL transforms
     * https://www.saxonica.com/html/documentation/using-xsl/embedding/s9api-transformation.html
     * @param input
     * @param xslt
     * @return
     */
    public synchronized static String xslTransformToString(String input, File xslt) {
        StreamSource source = new StreamSource(new StringReader(input));    // get the input string in a StreamSource
        StringWriter output = new StringWriter();                           // the output goes into a StringWriter
        Processor processor = new Processor(false);                         // we need a Processor
        Serializer destination = processor.newSerializer(output);           // the transformer need an instance of Destination to output, here we use a Serializer as destination and it outputs into the StringWriter

        // do the transform with XSLT 1.0 and 2.0 transformer
//        try {
//            XsltTransformer transformer = Helper.makeXsltTransformer(xslt, processor, source, destination);                    // from the executable get the transformer
//            transformer.transform();
//        } catch (SaxonApiException | FileNotFoundException e) {
//            e.printStackTrace();
//            return null;
//        }
//        return output.toString();

        Xslt30Transformer transformer30;
        try {
            transformer30 = Helper.makeXslt30Transformer(xslt);  // make a transformer
            transformer30.applyTemplates(source, destination);              // do the transform
        } catch (SaxonApiException | FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return output.toString();                                           // return the buffer of the StringWriter as String
    }

    /**
     * compile an XSLT 1.0 or 2.0 compatible Transformer from a given xslt stylesheet using the given processor and set the source and destination
     * @param xslt
     * @param processor
     * @param source
     * @param destination
     * @return
     * @throws SaxonApiException
     * @throws FileNotFoundException
     */
    public synchronized static XsltTransformer makeXsltTransformer(File xslt, Processor processor, Source source, Destination destination) throws SaxonApiException, FileNotFoundException {
        // get XSLT stylesheet as (Stream)Source object
        FileReader fileReader;
        fileReader = new FileReader(xslt);
        Source streamSource = new StreamSource(fileReader);

        // create the transformer
//        Processor processor = new Processor(false);                         // we need a Processor
        XsltCompiler compiler = processor.newXsltCompiler();                // from it create a compiler that compiles the stylesheet to an executable
        XsltExecutable executable;
        executable = compiler.compile(streamSource);                        // compile the stylesheet and get the executable
        XsltTransformer transformer = executable.load();                    // from the executable get the transformer
        transformer.setDestination(destination);
        transformer.setSource(source);

        return transformer;
    }

    /**
     * compile an XSLT 3.0 Transformer from a given xslt stylesheet using the given Processor instance
     * @param xslt
     * @param processor
     * @return
     * @throws SaxonApiException
     * @throws FileNotFoundException
     */
    public synchronized static Xslt30Transformer makeXslt30Transformer(File xslt, Processor processor) throws SaxonApiException, FileNotFoundException {
        // get XSLT stylesheet as (Stream)Source object
        FileReader fileReader;
        fileReader = new FileReader(xslt);
        Source streamSource = new StreamSource(fileReader);

        // create the transformer
//        Processor processor = new Processor(false);                         // we need a Processor
        XsltCompiler compiler = processor.newXsltCompiler();                // from it create a compiler that compiles the stylesheet to an executable
        XsltExecutable executable;
        executable = compiler.compile(streamSource);                        // compile the stylesheet and get the executable

        return executable.load30();                                         // from the executable get the transformer
    }

    /**
     * compile an XSLT 3.0 Transformer from a given xslt stylesheet
     * @param xslt
     * @return
     * @throws SaxonApiException
     * @throws FileNotFoundException
     */
    public synchronized static Xslt30Transformer makeXslt30Transformer(File xslt) throws SaxonApiException, FileNotFoundException {
        return Helper.makeXslt30Transformer(xslt, new Processor(false));
    }

    /**
     * given a string of XML code, this method prettyfies it
     * @param xml
     * @return
     */
    public static String prettyXml(String xml) {
        if (xml == null || xml.trim().length() == 0) return "";

        int stack = 0;
        StringBuilder pretty = new StringBuilder();
        String[] rows = xml.trim().replaceAll(">", ">\n").replaceAll("<", "\n<").split("\n");

        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == null || rows[i].trim().length() == 0) continue;

            String row = rows[i].trim();
            if (row.startsWith("<?")) {
                pretty.append(row + "\n");
            } else if (row.startsWith("</")) {
                String indent = repeatString(--stack);
                pretty.append(indent + row + "\n");
            } else if (row.startsWith("<") && !row.endsWith("/>")) {
                String indent = repeatString(stack++);
                pretty.append(indent + row + "\n");
                if (row.endsWith("]]>")) stack--;
            } else {
                String indent = repeatString(stack);
                pretty.append(indent + row + "\n");
            }
        }

        return pretty.toString().trim();
    }

    /**
     * just a helper method for prettyXml()
     * @param stack
     * @return
     */
    private static String repeatString(int stack) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < stack; i++) {
            indent.append("  ");
        }
        return indent.toString();
    }
}
