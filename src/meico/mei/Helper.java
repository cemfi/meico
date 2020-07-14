package meico.mei;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory;
import meico.mpm.Mpm;
import meico.mpm.elements.Part;
import meico.mpm.elements.Performance;
import meico.mpm.elements.maps.GenericMap;
import meico.mpm.elements.maps.data.TempoData;
import meico.mpm.elements.styles.TempoStyle;
import meico.mpm.elements.styles.defs.TempoDef;
import meico.msm.Msm;
import meico.supplementary.KeyValue;
import net.sf.saxon.s9api.*;
import net.sf.saxon.s9api.Serializer;
import nu.xom.*;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * This class is used for mei to msm conversion to hold temporary data, used in class Mei.
 * @author Axel Berndt.
 */
public class Helper {
    protected int ppq = 720;                                            // default value for pulses per quarter
    protected int endingCounter = 0;                                    // a counter of ending elements in the mei source
    protected boolean dontUseChannel10 = true;                          // set this flag false if you allow to "misuse" the midi drum channel for other instruments; in standard midi output this produces weird results, but when you work with vst plugins etc. there is no reason to avoid channel 10
    protected Element currentMsmMovement = null;
    protected Element currentMdiv = null;
    protected Element currentWork = null;
    protected Element currentPart = null;                               // this points to the current part element in the msm
    protected Element currentLayer = null;                              // this points to the current layer element in the mei source
    protected Element currentMeasure = null;
    protected Element currentChord = null;
    protected ArrayList<Element> accid = new ArrayList<>();             // holds accidentals that appear within measures to be considered during pitch computation
    protected ArrayList<Element> endids = new ArrayList<>();            // msm and mpm elements that will be terminated at the time position of an mei element with a specified endid
    protected ArrayList<Element> tstamp2s = new ArrayList<>();          // mpm elements that will be terminated at a position in another measure indicated by attribute tstamp2
    protected ArrayList<Element> lyrics = new ArrayList<>();            // this is used to collect lyrics converted from mei syl elements to be added to an msm note
    protected HashMap<String, Element> allNotesAndChords = new HashMap<>(); // when converting a new mdiv this hashmap is created first to accelarate lookup for notes and chords via xml:id
    protected Performance currentPerformance = null;                    // a quick link to the current movement's current performance
    protected List<Msm> movements = new ArrayList<>();                  // this list holds the resulting Msm objects after performing MEI-to-MSM conversion
    protected List<Mpm> performances = new ArrayList<>();               // this list holds the resulting Mpm objects after performing MEI-to-MSM conversion

    /**
     * constructor
     */
    protected Helper() {
    }

    /**
     * constructor
     * @param ppq
     */
    protected Helper(int ppq) {
        this.ppq = ppq;
    }

    /**
     * this method is called when making a new movement
     */
    protected void reset() {
        this.endingCounter = 0;
        this.currentMsmMovement = null;
        this.currentMdiv = null;
        this.currentWork = null;
        this.currentPerformance = null;
        this.currentPart = null;
        this.currentLayer = null;
        this.currentMeasure = null;
        this.currentChord = null;
        this.accid.clear();
        this.endids.clear();
        this.tstamp2s.clear();
        this.lyrics.clear();
        this.allNotesAndChords.clear();
    }

    /**
     * when a new MEI mdiv is processed this method generates a hashmap of all notes and chords, so we don't have to do it again during processing (e.g. in method isSameLayer() etc.)
     * @param mdiv
     */
    public void indexNotesAndChords(Element mdiv) {
        this.allNotesAndChords.clear();
        Nodes nodes = mdiv.query("descendant::*[(local-name()='note' or local-name()='chord') and attribute::xml:id]");

        for (int i=0; i < nodes.size(); ++i) {
            Element node = (Element) nodes.get(i);
            this.allNotesAndChords.put(Helper.getAttributeValue("id", node), node);
        }
    }

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
        if (ofThis == null) return null;

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
        if ((ofThis == null) || localname.isEmpty()) return null;
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
        if ((ofThis == null) || name.isEmpty()) return null;
        Nodes e = ofThis.query("child::*[local-name()='" + name + "']");   // find the elements with the localname by an XPath query
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
     * compute the midi time of an mei element
     * @return
     */
    protected double getMidiTime() {
        if (this.currentPart != null)                                                       // if we are within a staff environment
            return Double.parseDouble(this.currentPart.getAttributeValue("currentDate"));   // we have a more precise date somewhere within a measure

        if (this.currentMeasure != null)                                                    // if we are within a measure
            return Double.parseDouble(this.currentMeasure.getAttributeValue("date"));       // take it

        if (this.currentMsmMovement == null)                                                // if we are outside of any movement
            return 0.0;                                                                     // return 0.0

        // go through all parts, determine the latest currentDate and return it
        Elements parts = this.currentMsmMovement.getChildElements("part");                  // get the list of all parts
        double latestDate = 0.0;                                                            // here comes the result
        for (int i = parts.size()-1; i >= 0; --i) {                                         // go through that list
            double date = Double.parseDouble(parts.get(i).getAttributeValue("currentDate"));// get the part's date
            if (latestDate < date)                                                          // if this part's date is later than latestDate so far
                latestDate = date;                                                          // set latestDate to date
        }
        return latestDate;                                                                  // return the latest date of all parts
    }

    /**
     * compute the midi time of an mei element and return it as String
     * @return
     */
    protected String getMidiTimeAsString() {
        if (this.currentPart != null)                                                       // if we are within a staff environment
            return this.currentPart.getAttributeValue("currentDate");                       // we have a more precise date somewhere within a measure

        if (this.currentMeasure != null)                                                    // if we are within a measure
            return this.currentMeasure.getAttributeValue("date");                           // take it

        if (this.currentMsmMovement == null)                                                 // if we are outside of any movement
            return "0.0";                                                                   // return 0.0

        // go through all parts, determine the latest currentDate and return it
        Elements parts = this.currentMsmMovement.getChildElements("part");                   // get the list of all parts
        double latestDate = 0.0;                                                            // here comes the result
        for (int i = parts.size()-1; i >= 0; --i) {                                         // go through that list
            double date = Double.parseDouble(parts.get(i).getAttributeValue("currentDate"));// get the part's date
            if (latestDate < date)                                                          // if this part's date is later than latestDate so far
                latestDate = date;                                                          // set latestDate to date
        }
        return Double.toString(latestDate);                                                 // return the latest date of all parts
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
     * compute the length of one measure in midi ticks at the currentDate in the currentPart of the currentMovement; if no time signature information available it returns the length of a 4/4 measure
     * @return
     */
    protected double getOneMeasureLength() {
        double[] ts = this.getCurrentTimeSignature();
        return (4.0 * this.ppq * ts[0]) / ts[1];
    }

    /**
     * get the current time signature as tuplet of doubles [numerator, denominator]
     * @return
     */
    protected double[] getCurrentTimeSignature() {
        // get the value of one measure from the local or global timeSignatureMap
        Elements es = null;
        if (this.currentPart != null)                                                                                                                                           // we are within a part
            es = this.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");                                     // try to get its timeSignature
        if ((es == null) || (es.size() == 0))                                                                                                                                   // if we are outside a part or the local map is empty
            es = this.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");  // get global entries
        if ((es.size() == 0) && (this.currentWork != null)) {                                                                                                                   // get the meter element from meiHead
            Element meter = this.currentWork.getFirstChildElement("meter");
            if (meter != null) {
                Attribute count = meter.getAttribute("count");
                Attribute unit = meter.getAttribute("unit");
                return new double[]{((count == null) ? 4.0 : Double.parseDouble(count.getValue())), ((unit == null) ? 4.0 : Double.parseDouble(unit.getValue()))};
            }
        }

        // get length of one measure (4/4 is default if information is insufficient)
        double denom = (es.size() == 0) ? 4.0 : Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator"));
        double num = (es.size() == 0) ? 4.0 : Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator"));

        return new double[]{num, denom};
    }

    /**
     * compute the length of one measure with specified numerator and denominator values (the underlying time signature)
     * @param numerator
     * @param denominator
     * @return
     */
    protected double computeMeasureLength (double numerator, double denominator) {
        return (4.0 * this.ppq * numerator) / denominator;

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
     * copies the id attribute ofThis into toThis
     * @param ofThis
     * @param toThis
     * @return the newly created attribute
     */
    protected static Attribute copyId(Element ofThis, Element toThis) {
//        return copyIdNoNs(ofThis, toThis);
        return copyIdNs(ofThis, toThis);
    }

    /**
     * copies the id attribute from ofThis (if present) into toThis, without namespace
     * @param ofThis
     * @param toThis
     * @return the newly created attribute
     */
    private static Attribute copyIdNoNs(Element ofThis, Element toThis) {
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
        for (Node e = ofThis.getParent(); e != ofThis.getDocument().getRootElement(); e = e.getParent()) {
            if (e instanceof Element) return (Element)e;
        }
        return null;
    }

    /**
     * return part entry in current movement or null
     * @param id
     * @return
     */
    protected Element getPart(String id) {
        if ((id == null) || (id.isEmpty())) return null;

        Elements parts = this.currentMsmMovement.getChildElements("part");

        for (int i = parts.size()-1; i >= 0; --i) {                 // search all part entries in this movement
            if (parts.get(i).getAttributeValue("number").equals(id) || Helper.getAttributeValue("id", parts.get(i)).equals(id))    // for the id
                return parts.get(i);                                // return if found
        }

        return null;                                                // nothing found, return nullptr
    }

    /**
     * returns the layer element in the mei tree of ofThis
     * @param ofThis
     * @return the layer element or null if ofThis is not in a layer
     */
    protected static Element getLayer(Element ofThis) {
        for (Node e = ofThis.getParent(); e != ofThis.getDocument().getRootElement(); e = e.getParent()) {  // search for a layer element among the parents of ofThis
            if ((e instanceof Element) && (((Element)e).getLocalName().equals("layer")))                    // found one
                return (Element)e;
        }
        return null;
    }

    /**
     * returns the def or n attribute value of an mei layer element or empty string if it is no layer or both attributes are missing
     * @param layer
     * @return def, n or empty string
     */
    protected static String getLayerId(Element layer) {
        if ((layer == null) || !layer.getLocalName().equals("layer"))   // if the element is null or no layer
            return "";                                                  // return empty string
        if (layer.getAttribute("def") != null)                          // check for the def attribute (preferred over n)
            return layer.getAttributeValue("def");                      // return its string
        if (layer.getAttribute("n") != null)                            // check for the n attribute
            return layer.getAttributeValue("n");                        // return its string
        return "";                                                      // no def or n attribute, hence, return empty string
    }

    /**
     * returns the staff element in the mei tree of ofThis
     * @param ofThis
     * @return the staff element or null if ofThis is not in a staff
     */
    protected static Element getStaff(Element ofThis) {
        for (Node e = ofThis.getParent(); e != ofThis.getDocument().getRootElement(); e = e.getParent()) {  // search for a staff element among the parents of ofThis
            if ((e instanceof Element) && (((Element)e).getLocalName().equals("staff")))                    // found one
                return (Element)e;
        }
        return null;
    }

    /**
     * returns the def or n attribute value of an mei staff element or empty string if it is no staff or both attributes are missing
     * @param staff
     * @return def, n or empty string
     */
    protected static String getStaffId(Element staff) {
        if ((staff == null) || !staff.getLocalName().equals("staff"))   // if the element is null or no staff
            return "";                                                  // return empty string
        if (staff.getAttribute("def") != null)                          // check for the def attribute (preferred over n)
            return staff.getAttributeValue("def");                      // return its string
        if (staff.getAttribute("n") != null)                            // check for the n attribute
            return staff.getAttributeValue("n");                        // return its string
        return "";                                                      // no def or n attribute, hence, return empty string
    }

    /**
     * this method writes the layer's ref or n id to a layer attribute and adds that to ofThis
     * @param toThis an element that must be child of a layer element in mei
     */
    protected void addLayerAttribute(Element toThis) {
        Element layer = this.currentLayer;              // get the current layer from the current mei processing
//        if (layer == null) layer = getLayer(toThis);    // if no current layer, search the parents of toThis for a layer element
        if (layer == null) return;                      // if still no layer found, we are done

        // add the value of the layer's def or n attribute to toThis as attribute layer
        if (layer.getAttribute("def") != null) {
            toThis.addAttribute(new Attribute("layer", layer.getAttributeValue("def")));
        }
        else if (layer.getAttribute("n") != null)
            toThis.addAttribute(new Attribute("layer", layer.getAttributeValue("n")));
    }

    /**
     * cleanup of the msm objects to remove all conversion related and no longer needed entries in the msm objects (miscMaps, currentDate and tie attributes)
     * @param msms
     */
    public static void msmCleanup(List<Msm> msms) {
        for (int i=0; i < msms.size(); ++i) {                       // go through all msm objects in the input list
            msmCleanup(msms.get(i));                                // make the cleanup
        }
    }

    /**
     * make the cleanup of one msm object; this removes all miscMaps, currentDate, tie, and layer and lots of further non-MSM confrom attributes
     * @param msm
     */
    public static void msmCleanup(Msm msm) {
        // delete all miscMaps and non-msm conform attributes
        Nodes n = msm.getRootElement().query("descendant::*[local-name()='miscMap'] | descendant::*[attribute::currentDate]/attribute::currentDate | descendant::*[attribute::tie]/attribute::tie | descendant::*[attribute::layer]/attribute::layer | descendant::*[attribute::endid]/attribute::endid | descendant::*[attribute::tstamp2]/attribute::tstamp2 | descendant::*[local-name()='goto' and attribute::n]/attribute::n");
        for (int i=0; i < n.size(); ++i) {
            if (n.get(i) instanceof Element)
                n.get(i).getParent().removeChild(n.get(i));

            if (n.get(i) instanceof Attribute)
                ((Element) n.get(i).getParent()).removeAttribute((Attribute) n.get(i));
        }
        msm.deleteEmptyMaps();
    }

    /**
     * some mpm data is not in its final state (e.g., dynamics elements with an end attribute), this method makes these final
     * @param mpms
     */
    public static void mpmPostprocessing(List<Mpm> mpms) {
        for (int i=0; i < mpms.size(); ++i) {                       // go through all mpm objects in the input list
            mpmPostprocessing(mpms.get(i));                         // do the postprocessing
        }
    }

    /**
     * some mpm data is not in its final state (e.g., dynamics elements with an end attribute), this method makes these final
     * @param mpm
     */
    public static void mpmPostprocessing(Mpm mpm) {
        ArrayList<GenericMap> maps = new ArrayList<>();

        for (int p=0; p < mpm.size(); ++p) {                                                                                // go through all performances
            Performance perf = mpm.getPerformance(p);

            // collect all global and local dynamicsMaps and tempoMaps
            GenericMap aMap = perf.getGlobal().getDated().getMap(Mpm.DYNAMICS_MAP);
            if (aMap != null)
                maps.add(aMap);

            aMap = perf.getGlobal().getDated().getMap(Mpm.TEMPO_MAP);
            if (aMap != null)
                maps.add(aMap);

            ArrayList<Part> parts = perf.getAllParts();
            for (int pp=0; pp < perf.size(); ++pp) {                                                                        // go through all parts
                Part part = parts.get(pp);

                aMap = part.getDated().getMap(Mpm.DYNAMICS_MAP);
                if (aMap != null)
                    maps.add(aMap);

                aMap = part.getDated().getMap(Mpm.TEMPO_MAP);
                if (aMap != null)
                    maps.add(aMap);
            }
        }

        // go through all the maps' elements and finalize them
        for (GenericMap map : maps) {
            for (int e=0; e < map.size(); ++e) {
                Element d = map.getElement(e);

                // handle remaining endid attributes
                Attribute endid = d.getAttribute("endid");
                if (endid != null)                                                                                          // if the instruction still has an endid (i.e., it never occured during conversion and the end is unknown)
                    d.removeAttribute(endid);                                                                               // just remove it, it is not part of the MPM specification

                // handle remaining tstamp2 attributes
                Attribute tstamp2 = d.getAttribute("tstamp2");
                if (tstamp2 != null)                                                                                        // if the instruction still has a tstamp2 (i.e., it never occured during conversion and the end is unknown)
                    d.removeAttribute(tstamp2);                                                                             // just remove it, it is not part of the MPM specification

                Attribute end = d.getAttribute("date.end");
                if (end != null) {                                                                                          // if it has an end attribute
                    double endDate = Double.parseDouble(end.getValue());                                                    // get the end date
                    d.removeAttribute(end);                                                                                 // remove the attribute, it is not part of the MPM specification
                    Element next = map.getElement(e + 1);                                                                   // get the subsequent element in the map
                    if ((next == null) || (Double.parseDouble(next.getAttributeValue("date")) > endDate)) {                 // if the end date is before the next instruction in the map or there is no next instruction
                        Attribute t = d.getAttribute("transition.to");                                                      // is there a transition.to attribute? if not we have nothing meaningful to do here
                        if (t != null) {                                                                                    // if there is a transition.to
                            String elementType = d.getLocalName();                                                          // get the type of the element
                            Element endElement = new Element(elementType, Mpm.MPM_NAMESPACE);                               // create a new instruction
                            endElement.addAttribute(new Attribute("date", Double.toString(endDate)));                                   // its date is the end date

                            switch (elementType) {
                                case "dynamics":
                                    endElement.addAttribute(new Attribute("volume", t.getValue()));                         // its volume is the transition.to value
                                    break;
                                case "tempo":
                                    endElement.addAttribute(new Attribute("bpm", t.getValue()));                            // its bpm is the transition.to value
                                    break;
                                default:
                                    continue;
                            }
                            map.addElement(endElement);                                                                     // insert it behind thew current element
                        }
                    }
                }
            }
        }
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
     * helper method to generate MPM TempoData from an MEI tempo element,
     * only the timing data is not computed here
     * @param tempo
     * @return
     */
    public TempoData parseTempo(Element tempo) {
        TempoData tempoData = new TempoData();                                                                      // tempo data to generate an entry in an MPM tempoMap

        // determine numeric tempo if such a value is specified
        Attribute mm = tempo.getAttribute("mm");
        if (mm != null)                                                                                             // if there is a Maezel's Metronome value
            tempoData.bpmString = mm.getValue();                                                                          // take this as the bpm value
        else {
            Attribute midiBpm = tempo.getAttribute("midi.bpm");
//            tempoData.beatLength = 0.25;                                                                          // not necessary because it is initialized with 0.25
            if (midiBpm != null)                                                                                    // if there is a MIDI bpm attribute (always to the basis of a quarter note)
                tempoData.bpmString = midiBpm.getValue();                                                                 // take this as bpm value
            else {
                Attribute midiMspb = tempo.getAttribute("midi.mspb");
                if (midiMspb != null)                                                                               // if there is a microseconds per quarter note attribute
                    tempoData.bpmString = Double.toString((60000000.0 / (Double.parseDouble(midiMspb.getValue()))));      // compute the bpm value from it
            }
        }

        // compute beatLength
        Attribute mmUnit = tempo.getAttribute("mm.unit");
        tempoData.beatLength = (mmUnit != null) ? Helper.duration2decimal(mmUnit.getValue()) : (1.0 / this.getCurrentTimeSignature()[1]);    // use the specified mm.unit for beatLength or (if missing) use the denominator of the underlying time signature
        Attribute mmDots = tempo.getAttribute("mm.dots");
        if (mmDots != null) {                                                                                   // are there dots involved in the beatLength
            int dots = Integer.parseInt(mmDots.getValue());                                                     // get their number
            for (double d = tempoData.beatLength; dots > 0; --dots) {                                           // for each dot; variable d holds what has to be added to the beatLength value
                d /= 2;                                                                                         // half d
                tempoData.beatLength += d;                                                                      // add to beatLength
            }
        }

        // process tempo descriptor, i.e. the value of the MEI element
        String descriptor = tempo.getValue();                                                                                                               // the textual representation of a tempo instruction
        if (descriptor.isEmpty()) {                                                                             // if no value/text at this element
            Attribute label = tempo.getAttribute("label");                                                      // try attribute label
            if (label != null) descriptor = label.getValue();                                                   // if there is a label attribute, use its value
        }
        if (!descriptor.isEmpty()) {                                                                                                                        // a textual instruction is given
            if (descriptor.contains("rit") || descriptor.contains("rall") || descriptor.contains("largando") || descriptor.contains("calando")) {           // slow down
                if (tempoData.bpmString == null)
                    tempoData.bpmString = "?";
                tempoData.transitionToString = "-";
            }
            else if (descriptor.contains("accel") || descriptor.contains("string")) {                                                                       // accelerate
                if (tempoData.bpmString == null)
                    tempoData.bpmString = "?";
                tempoData.transitionToString = "+";
            }
            else {                                                                                                                                          // an instantaneous instruction that might be added to the global styleDef
                TempoStyle tempoStyle = (TempoStyle) this.currentPerformance.getGlobal().getHeader().getStyleDef(Mpm.TEMPO_STYLE, "MEI export");            // get the global tempoSyles/styleDef element
                if (tempoStyle == null)                                                                                                                     // if there is none
                    tempoStyle = (TempoStyle) this.currentPerformance.getGlobal().getHeader().addStyleDef(Mpm.TEMPO_STYLE, "MEI export");                   // create one

                if ((tempoStyle != null) && (tempoStyle.getTempoDef(descriptor) == null)) {                                                                 // if there is a descriptor string for this tempo instruction
                    // use the specified tempo or, if not defined, try to create a default numeric value for the descriptor string
                    if (tempoData.bpmString == null)
                        tempoStyle.addTempoDef(TempoDef.createDefaultTempoDef(descriptor));
                    else
                        tempoStyle.addTempoDef(TempoDef.createTempoDef(descriptor, Double.parseDouble(tempoData.bpmString)));
                }
                tempoData.bpmString = descriptor;
            }
        }
        if (tempoData.bpmString == null) {          // if no textual descriptor and no bpm is given
            System.err.println("Cannot process MEI element " + tempo.toXML() + ". No text or any of the attributes 'mm', 'midi.bpm', 'midi.mspb', or 'label' is specified.");
            return null;                            // no sufficient information, cancel
        }

        if (tempoData.transitionToString != null)
            tempoData.meanTempoAt = 0.5;            // by default we create a very neutral/mechanico tempo transition, this should be edited by the user/application

        // read the xml:id
        Attribute id = Helper.getAttribute("id", tempo);
        tempoData.xmlId = (id == null) ? null : id.getValue();

        return tempoData;
    }

    /**
     * this method moves all subtrees of a measure that are non staff subtrees, i.e. they are control event subtrees, to the front as these have to be processed before the staffs
     * @param measure
     */
    protected static void reorderMeasureContent(Element measure) {
        Elements subtrees = measure.getChildElements();                                         // get all children of the measure

        for (int i = subtrees.size()-1; i >= 0; --i) {                                          // for each child
            Element subtree = subtrees.get(i);                                                  // get it as element
            if (subtree.query("descendant-or-self::*[local-name()='staff' or local-name()='oStaff']").size() == 0) {     // if this subtree contains no staff element it is a control event subtree
                subtree.detach();                                                               // remove it from the measure
                measure.insertChild(subtree, 0);                                                // and add it at the front of the measure
            }
        }
    }

    /**
     * return the first element in the endids list with an endid attribute value that equals id
     * @param id
     * @return the index in the endid list or -1 if not found
     */
    private int getEndid(String id) {
        for (int i=0; i < this.endids.size(); ++i) {                        // go through the list of pending elements to be ended
            if (this.endids.get(i).getAttributeValue("endid").equals(id))   // found
                return i;                                                   // return it
        }
        return -1;
    }

    /**
     * check for pending elements with endid attributes to be finished when the element with this endid is found,
     * note that this will compute the end date including(!) the duration of the element (except for slurs) that endid pointes to, i.e. it includes the endid element
     * @param e
     */
    protected void checkEndid(Element e) {
        String id = "#" + Helper.getAttributeValue("id", e);                                                                            // get id of the current element
        for (int j = this.getEndid(id); j >= 0; j = this.getEndid(id)) {                                                                // find all pending elements in the endid list to be finished at this element
            this.endids.get(j).addAttribute(new Attribute("date.end", Double.toString(this.getMidiTime() + ((this.endids.get(j).getLocalName().equals("slur")) ? 0.0 : this.computeDuration(e)))));  // finish corresponding element, only slurs should not include the duration
            this.endids.remove(j);                                                                                                      // remove element from list, it is finished
        }
    }

    /**
     * this method is for note elements to check whether one of the pending slurs applies for it
     * @param e
     */
    protected void checkSlurs(Element e) {
        Elements slurs = this.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("slur");

        for (int i = slurs.size() - 1; i >= 0; --i) {                                                                                                                   // go through the global slurs
            if ((slurs.get(i).getAttributeValue("date") != null) && (Double.parseDouble(slurs.get(i).getAttributeValue("date")) > this.getMidiTime())) {                // if this slur element is after e
                continue;                                                                                                                                               // continue searching
            }
            if (slurs.get(i).getAttribute("date.end") != null) {                                                                                                        // if it is before e
                double endDate = Double.parseDouble(slurs.get(i).getAttributeValue("date.end"));
                if (endDate < this.getMidiTime()) {                                                                                                                     // if the end date of this slur (if one is specified) is before e
                    continue;
                }
                if (endDate == this.getMidiTime()) {                                                                                                                    // if the end date of this slur (if one is specified) is at e
                    e.addAttribute(new Attribute("slur", "t"));                                                                                                         // set the slur attribute to terminal
                    this.addSlurId(slurs.get(i), e);
                    return;                                                                                                                                             // no need to look for further slurs
                }
            }
            e.addAttribute(new Attribute("slur", "im"));
            this.addSlurId(slurs.get(i), e);
        }

        if (this.currentPart != null) {
            String layerId = getLayerId(getLayer(e));                                                                                                                   // get the current layer's id reference
            slurs = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("slur");

            for (int i = slurs.size() - 1; i >= 0; --i) {                                                                                                               // go through the local slurs
                if (!Helper.isSameLayer(slurs.get(i), layerId)) {                                                                                                       // check whether this slur is dedicated to a specific layer but not the current layer (layer of ofThis)
                    continue;
                }
                if ((slurs.get(i).getAttributeValue("date") != null) && (Double.parseDouble(slurs.get(i).getAttributeValue("date")) > this.getMidiTime())) {            // if this slur element is after ofThis
                    continue;
                }
                if (slurs.get(i).getAttribute("date.end") != null) {                                                                                                    // if it is before e
                    double endDate = Double.parseDouble(slurs.get(i).getAttributeValue("date.end"));
                    if (endDate < this.getMidiTime()) {                                                                                                                 // if the end date of this slur (if one is specified) is before e
                        continue;
                    }
                    if (endDate == this.getMidiTime()) {                                                                                                                // if the end date of this slur (if one is specified) is at e
                        e.addAttribute(new Attribute("slur", "t"));                                                                                                     // set the slur attribute to terminal
                        this.addSlurId(slurs.get(i), e);
                        return;                                                                                                                                         // no need to look for further slurs
                    }
                }
                e.addAttribute(new Attribute("slur", "im"));
                this.addSlurId(slurs.get(i), e);
            }
        }
    }

    /**
     * a helper method to make the code of method checkSlurs() a bit more compact
     * @param fromThis
     * @param toThis
     */
    private void addSlurId(Element fromThis, Element toThis) {
        Attribute slurid = Helper.getAttribute("id", fromThis);
        if (slurid != null) {
            toThis.addAttribute(new Attribute("slurid", slurid.getValue() + "_meico_" + UUID.randomUUID().toString()));
        }
    }

    /**
     * this method converts the string of a barline (MEI element measure in attributes left and right) to an msm sequencing command (marker and/or goto element) and adds it to the global sequencingMap
     * @param barline the string that can be read in MEI measure attributes "left" and "right"
     * @param date the midi date
     * @param sequencingMap the sequencingMap to add the elements to
     */
    protected void barline2SequencingCommand(String barline, double date, Element sequencingMap) {
        String markerMessage = null;
        boolean makeGoto = false;

        // what does the barline say?
        switch (barline) {
            case "end":                                                             // it is an end line
                markerMessage = "fine";                                             // set a marker message (actually unneccessary at the end of the score nut requires for dacapo-al-fine situations)
                break;
            case "rptstart":                                                        // it is a repetition start point
                markerMessage = "repetition start";                                 // set marker message
                break;
            case "rptboth":                                                         // it is a repetition start and end point
                markerMessage = "repetition start";                                 // set marker message
                makeGoto = true;                                                    // trigger generation of a goto element in the sequencingMap
                break;
            case "rptend":                                                          // a repetition end point
                makeGoto = true;                                                    // trigger generation of a goto element in the sequencingMap
                break;
            default:                                                                // all other types of barlines
                return;                                                             // are irrelevant for the sequencing
        }

        // create a goto element and insert it into the sequencingMap
        if (makeGoto) {                                                             // if a goto element has to be generated
            Element gt = new Element("goto");                                       // make it
            gt.addAttribute(new Attribute("date", Double.toString(date)));     // give it the date
            gt.addAttribute(new Attribute("activity", "1"));                        // process this goto at the first time, later on ignore it
            gt.addAttribute(new Attribute("target.date", "0"));                     // add the target.date attribute by default initialized with "0" (which means to start from the beginning)
            gt.addAttribute(new Attribute("target.id", ""));                        // add an empty target.id attribute (which means to start from the beginning)
            int index = Helper.addToMap(gt, sequencingMap);                         // insert the goto into the sequencingMap and store its index because we need to find the marker to jump to
            Nodes ns = sequencingMap.query("descendant::*[local-name()='marker' and (@message='repetition start' or @message='fine')]");  // get all the markers that are repetition start points or fines
            for (int i = ns.size()-1; i >= 0; --i) {                                                                                // check them from back to front and find
                Element n = (Element)ns.get(i);                                                                                     // the element
                if (Double.parseDouble(n.getAttributeValue("date")) < date) {                                                  // that has a date right before the goto's date
                    gt.getAttribute("target.date").setValue(n.getAttributeValue("date"));                                      // take this as jump's target date
                    gt.getAttribute("target.id").setValue("#" + n.getAttributeValue("id", "http://www.w3.org/XML/1998/namespace")); // take this as the jump's target marker
                    break;                                                                                                          // done
                }
            }                                                                                                                       // if nothing was found in this for loop, target.date and target.id remain as initialized
        }

        // generate a marker (potential jump target) and insert it into the sequencingMap
        if (markerMessage != null) {                                                // if a marker should be generated
            Element marker = new Element("marker");                                 // do so
            marker.addAttribute(new Attribute("date", Double.toString(date))); // give it a date
            marker.addAttribute(new Attribute("message", markerMessage));           // set its message
            Attribute id = new Attribute("id", "meico_" + UUID.randomUUID().toString());       // give it a UUID
            id.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");         // set its namespace to xml
            marker.addAttribute(id);                                                // add the id attribute to the marker
            Helper.addToMap(marker, sequencingMap);                                 // add the marker to the sequencingMap
        }
    }

    /**
     * This method interprets the clef.dis and clef.dis.place attribute as a transposition that is not encoded in the note elements.
     * In the mei sample set, however, this is not the case which leads to wrong octave transpositions of the respective notes.
     * Hence, I inserted a return 0 at the beginning.
     * If you want meico to feature the transponing behavior, remove the return 0 line and uncomment the remaining code.
     * @param scoreStaffDef the scoreDef or staffDef element from mei
     * @return the octave transposition that derives from the clef.dis or clef.dis.place attribute
     */
    protected static double processClefDis(Element scoreStaffDef) {
        return 0.0;

//        double oct = 0.0;
//        if (scoreStaffDef.getAttribute("clef.dis") != null)  {
//            switch (scoreStaffDef.getAttributeValue("clef.dis")) {
//                case "8":
//                    oct = 12.0;
//                    break;
//                case "15":
//                    oct = 24.0;
//                    break;
//                case "22":
//                    oct = 32.0;
//            }
//            if (scoreStaffDef.getAttribute("clef.dis.place") != null) {
//                switch (scoreStaffDef.getAttributeValue("clef.dis.place")) {
//                    case "above":
//                        break;
//                    case "below":
//                        oct *= -1;
//                        break;
//                }
//            }
//            else
//                oct = 0.0;
//        }
//
//        return oct;
    }

    /**
     * convert a tstamp value to midi ticks,
     * not suited for tstamp2!
     * @param tstamp
     * @return
     */
    protected double tstampToTicks(String tstamp) {
        if ((tstamp == null) || tstamp.isEmpty() || (this.currentMeasure == null))      // if there is no tstamp or it is empty or we are outside a measure (tstamps are only meaningful within a measure)
            return this.getMidiTime();                                                  // just return the current time

        double date = Double.parseDouble(tstamp);                                       // convert the tstamp value to double
        date = (date < 1.0) ? 0.0 : (date - 1.0);                                       // date == 0.0 is the barline, first beat is at date 1.0, timing-wise both are equal

        double denom = this.getCurrentTimeSignature()[1];                               // get the current denominator
        double tstampToTicksConversionFactor = (4.0 * this.ppq) / denom;                // multiply a tstamp with this and you get the midi tick value (don't forget to add the measure date!)

        return (date * tstampToTicksConversionFactor) + Double.parseDouble(this.currentMeasure.getAttributeValue("date"));
    }

    /**
     * MEI control events are usually placed out of timing at the end of a measure. If they use @startid meico places them right before the referred element. Otherwise the timing has to be computed from @tstamp.ges or @tstamp.
     * The same is true for the duration of control events. It is computed from @dur, @tstamp2.ges, @tstamp2, or @endid (in this priority).
     * This method helps in handling this.
     * @param event the MEI control event
     * @return an ArrayList of the following form (double date, Double endDate, Attribute tstamp2, Attribute endid), except for date every other entry can be null if no such data is present or applicable! The return value can also be null when the timing should better be computed on the basis of attribute startid, in that case this method does the repositioning of the event automatically and the invoking method should cancel this event's processing right now and get back to this event later on
     */
    protected ArrayList<Object> computeControlEventTiming(Element event) {
        // read the tstamp or, if missing, process startid
        Attribute att = event.getAttribute("tstamp.ges");
        if (att == null) {
            att = event.getAttribute("tstamp");
            if ((att == null) && (event.getAttribute("dontRepositionMeAgain") == null)) {                           // if there is no tstamp information at all and this element has not yet been repositioned on the basis of startid
                Attribute startidAtt = event.getAttribute("startid");                                               // try finding a startid attribute
                if (startidAtt != null) {
                    String startid = startidAtt.getValue().trim().replace("#", "");                                 // get the id string
                    Element node = this.allNotesAndChords.get(startid);
                    if (node != null) {
                        Element parent = (Element) node.getParent();
                        event.detach();                                                                             // detach the event
                        parent.insertChild(event, parent.indexOf(node));                                            // and insert it at the position
//                        event.removeAttribute(startidAtt);                                                        // remove attribute startid so this element is not replaced again when reaching it during the further processing
                        event.addAttribute(new Attribute("dontRepositionMeAgain", "true"));                         // make an indication that this element has been repositioned on the basis of startid
                        return null;                                                                                // this control event has been replaced and should be processed later
                    }
                }
            }
        }
        String tstamp = (att == null) ? null  : att.getValue();
        Double date = this.tstampToTicks(tstamp);                   // the midi date of the instruction from tstamp or, if null, the current midi date

        // read dur, tstamp2 or endid
        Attribute tstamp2 = null;
        Attribute endid = null;                                     // if no tstamp2 will be found, maybe there is an endid attribute
        Double endDate = null;                                      // there might be an end date for this event
        if (event.getAttribute("dur") != null) {                    // if there is a dur attribute
            endDate = date + this.computeDuration(event);           // compute the duration
        }
        else {
            tstamp2 = event.getAttribute("tstamp2.ges");            // get the tstamp2.ges attribute
            if (tstamp2 == null)                                    // if no tstamp2.ges
                tstamp2 = event.getAttribute("tstamp2");            // try finding tstamp2
            if (tstamp2 != null) {                                  // if a tstamp2.ges or tstamp2 was found
                String[] ts2 = tstamp2.getValue().split("m\\+");    // the first field of this array sais how many barlines will be crossed, the second is the usual tstamp (e.g., 2m+3.5), if only one field is present it is within this same measure
                if (ts2.length == 0)                                // if the tstamp2 string is invalid (empty or only "m+")
                    tstamp2 = null;                                 // ignore this attribute, the next if statement will check for an endid attribute
                else if (ts2.length == 1) {
                    endDate = this.tstampToTicks(ts2[0]);
                    tstamp2 = null;
                } else if (ts2[0].equals("0")) {
                    endDate = this.tstampToTicks(ts2[1]);
                    tstamp2 = null;
                }
            }
            endid = event.getAttribute("endid");                    // store also the endid attribute, if present
        }

        ArrayList<Object> result = new ArrayList<>();
        result.add(date);
        result.add(endDate);
        result.add(tstamp2);
        result.add(endid);

        return result;
    }

    /**
     * compute midi tick duration of a note or rest, if fail return 0.0;
     * the stuff from data.DURATION.gestural is not supported! Because we need pure note values here.
     * @param ofThis
     * @return
     */
    protected Double computeDuration(Element ofThis) {
        if ((!ofThis.getLocalName().equals("bTrem")                  // for what kind of element shall the duration be computed?
                && !ofThis.getLocalName().equals("chord")
                && !ofThis.getLocalName().equals("dynam")
                && !ofThis.getLocalName().equals("fTrem")
                && !ofThis.getLocalName().equals("halfmRpt")
                && !ofThis.getLocalName().equals("mRest")
                && !ofThis.getLocalName().equals("mSpace")
                && !ofThis.getLocalName().equals("note")
                && !ofThis.getLocalName().equals("octave")
                && !ofThis.getLocalName().equals("rest")
                && !ofThis.getLocalName().equals("tuplet")
                && !ofThis.getLocalName().equals("space"))) {       // if none of these
            return 0.0;                                             // return 0.0
        }

        double dur;                                                                         // here comes the resultant note/rest duration in midi ticks
        boolean chordEnvironment = (this.currentChord != null);                             // if we are in a chord environment set this true, else false
        Element focus = ofThis;                                                             // this will change to the chord environment, if there is one

        { // get basic duration (without dots, tuplets etc.)
            String sdur = "";                                                                       // the dur string
//            if (ofThis.getAttribute("dur.ges") != null) {                                           // if there is a dur.ges attribute
//                sdur = focus.getAttributeValue("dur.ges");
//            }
//            else
            if (ofThis.getAttribute("dur") != null) {                                               // if there is a dur attribute
                sdur = focus.getAttributeValue("dur");
            }
            else {
                if (chordEnvironment && (this.currentChord.getAttribute("dur") != null)) {          // if a chord environment defines a duration
                    focus = this.currentChord;                                                      // from now on, look only in the chord environment for all further duration related attributes
                    sdur = focus.getAttributeValue("dur");                                          // take this
                }
                else {                                                                              // check for local and global default durations with and without layer consideration
                    if (this.currentPart == null) {                                                 // we have to be in a staff environment for this
                        return 0.0;                                                                 // if not return 0.0
                    }
                    String layerId = getLayerId(getLayer(ofThis));                                  // store the layer id
                    Elements durdefaults = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("dur.default");                              // get all local default durations
                    if (durdefaults.size() == 0) {                                                                                                           // if there is none
                        durdefaults = this.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("dur.default");// get all global default durations
                    }
                    for (int i=durdefaults.size()-1; i >= 0; --i) {                                                                                 // search from back to front
                        if ((durdefaults.get(i).getAttribute("layer") == null) || durdefaults.get(i).getAttributeValue("layer").equals(layerId)) {  // for a default duration with no layer dependency or a matching layer
                            sdur = durdefaults.get(i).getAttributeValue("dur");                                                                     // take this value
                            break;                                                                                                                  // break the for loop
                        }
                    }
                    if (sdur.isEmpty()) {                                                           // nothing found
                        return 0.0;                                                                 // cancel
                    }
                }
            }

            switch (sdur) {
                case "breve":  dur = 8.0 * this.ppq;  break;
                case "long":   dur = 16.0 * this.ppq; break;
                default:       dur = (4.0 * this.ppq) / Integer.parseInt(sdur);         // compute midi tick duration
            }
        }

        { // dots
            int dots = 0;
            if (focus.getAttribute("dots") != null) {                                  // if dotted note value through attribute
                dots = Integer.parseInt(focus.getAttributeValue("dots"));              // get the number of dots
            }
            else {                                                                      // if dotted through child tags
                if (focus.getAttribute("childDots") != null)
                    dots = Integer.parseInt(focus.getAttributeValue("childDots"));      // get the number of dots from child elements
                if ((dots == 0) && chordEnvironment && (this.currentChord.getAttribute("dots") != null)){   // if no dotting information so far, check chord environment for dots
                    dots = Integer.parseInt(this.currentChord.getAttributeValue("dots"));                   // get the number of dots
                }
            }

            for (double d = dur; dots > 0; --dots) {                                    // for each dot; variable d holds what has to be added to the dur value
                d /= 2;                                                                 // half d
                dur += d;                                                               // add to dur
            }
        }

        // tuplets
        // TODO: what about the tuplet attribute (without the tuplet environment); how to read and process this?
        for (Element e = Helper.getParentElement(focus); (e != null) && (!e.getLocalName().equals("mdiv")); e = Helper.getParentElement(e)) {  // search for tuplet environment among the parents
            if (e.getLocalName().equals("tuplet")) {                                                                                            // if the ofThis lies within a tuplet
                if ((e.getAttribute("numbase") == null) || (e.getAttribute("num") == null)) {                                                   // insufficient information to compute the note duration
                    return 0.0;                                                                                                                 // cancel
                }
                dur *= Double.parseDouble(e.getAttributeValue("numbase")) / Integer.parseInt(e.getAttributeValue("num"));                       // scale dur: dur*numbase/num ... this loop does not break here, because of the possibility of tuplets within tuplets
                // This calculation can come with numeric error. That error is given across to the onset time of succeeding notes. We compensate this error by making a clean currentTime computation with each measure element, so the error does not propagate beyond barlines.
            }
        }

        // tupletSpans
        LinkedList<Element> tps;
        if (this.currentPart != null) {                                                                                                                                             // we have to be in a staff environment for this
            tps = Helper.getAllChildElements("tupletSpan", this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getFirstChildElement("tupletSpanMap"));   // get all local tupletSpans
        } else {
            tps = Helper.getAllChildElements("tupletSpan", this.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getFirstChildElement("tupletSpanMap")); // get all globalo tupletSpans
        }

        for (int i = tps.size() - 1; i >= 0; --i) {                                                                                                             // go through all these tupletSpans, starting with the last
            Element ts = tps.get(i);
            if ((ts.getAttribute("date.end") != null) && (Double.parseDouble(ts.getAttributeValue("date.end")) <= this.getMidiTime())) {                        // if the tupletSpan is already over
                this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getFirstChildElement("tupletSpanMap").removeChild(ts);           // remove this tupletSpan from the map, it is no longer needed
                continue;                                                                                                                                       // continue with the previous element in tps
            }
            if (!Helper.isSameLayer(ts, Helper.getLayerId(this.currentLayer))) {                                                                                 // check whether this is dedicated to a specific layer but not the current layer (layer of ofThis)
                continue;
            }
            if (Double.parseDouble(ts.getAttributeValue("date")) <= this.getMidiTime())                                                                         // make sure the tupletSpan has already started
                dur *= Double.parseDouble(ts.getAttributeValue("numbase")) / Integer.parseInt(ts.getAttributeValue("num"));                                     // scale dur: dur*numbase/num ... this loop does not break here, because of the possibility of tuplets within tuplets
                // This calculation can come with numeric error. That error is given through to the onset time of subsequent notes and rests. We compensate this error by making a clean currentTime computation with each measure element, so the error does not propagate beyond barlines.
        }

        return dur;
    }

    /**
     * convert the duration string into decimal (e.g., 4 -&gt; 1/4) and returns the result
     * @param durString
     * @return
     */
    public static double duration2decimal(String durString) {
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
            case 1:  return "C# Db";
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
     * check wether the layer attribute of an MEI control event e contains a layerId
     * @param e
     * @param layerId
     * @return true if it contains the layerId or e has no layer attribute (quasi global to all layers), otherwise false
     */
    public static boolean isSameLayer(Element e, String layerId) {
        if (e.getAttribute("layer") != null) {                                      // if this transposition is dedicated to a specific layer but not the current layer (layer of ofThis)
            String[] layers = e.getAttributeValue("layer").trim().split("\\s+");
            for (String layer : layers) {
                if (layer.equals(layerId)) {
                    return true;
                }
            }
            return false;
        }
        return true;                                                                // if e is not dedicated to a specific layer, it is dedicated to all layers
    }

    /**
     * this is a helper to work with startid and endid in MEI control events
     * @param startid
     * @param endid
     * @return the layer's attribute value def, n or empty string
     */
    public String isSameLayer(String startid, String endid) {
        Element start = this.allNotesAndChords.get(startid.trim().replace("#", ""));
        if (start == null)
            return "";

        Element end = this.allNotesAndChords.get(endid.trim().replace("#", ""));
        if (end == null)
            return "";

        String startLayerId = Helper.getLayerId(Helper.getLayer(start));    // get the layer of the first element
        if (startLayerId.isEmpty())                                         // if not defined
            return "";                                                      // done

        String endLayerId = Helper.getLayerId(Helper.getLayer(end));        // get its layer id
        if (!startLayerId.equals(endLayerId))                               // if it is not equal to the previous
            return "";                                                      // done

        return startLayerId;                                                // we reached this point, hence there must be at least two elements with the specified xml:ids, all being in the same layer
    }

    /**
     * this is a helper to work with startid and endid in MEI control events
     * @param startid
     * @param endid
     * @return the staff's attribute value def, n or empty string
     */
    public String isSameStaff(String startid, String endid) {
        Element start = this.allNotesAndChords.get(startid.trim().replace("#", ""));
        if (start == null)
            return "";

        Element end = this.allNotesAndChords.get(endid.trim().replace("#", ""));
        if (end == null)
            return "";

        String startStaffId = Helper.getStaffId(Helper.getStaff(start));    // get the staff of the first element
        if (startStaffId.isEmpty())                                         // if not defined
            return "";                                                      // done

        String endStaffId = Helper.getStaffId(Helper.getStaff(end));        // get its staff id
        if (!startStaffId.equals(endStaffId))                               // if it is not equal to the previous
            return "";                                                      // done

        return startStaffId;                                                // we reached this point, hence there must be at least two elements with the specified xml:ids, all being in the same staff
    }

    /**
     * compute midi pitch of an mei note or return -1.0 if failed; the return is a double number that captures microtonality, too; 0.5 is a quarter tone
     * parameter pitchdata should be an empty ArrayList&gt;String&lt;, it is filled with pitchname, accidentals and octave of the computed midi pitch for further use
     *
     * @param ofThis
     * @param pitchdata
     * @return
     */
    protected double computePitch(Element ofThis, ArrayList<String> pitchdata) {
        String pname;                                                   // the attribute strings
        String accid = "";                                              // the accidental string
        String layerId = getLayerId(getLayer(ofThis));                  // get the current layer's id reference
        double oct = 0.0;                                               // octave transposition value
        double trans = 0;                                               // transposition
        boolean checkKeySign = false;                                   // is set true

        // get the attributes, prefer gesturals

        // get the pitch name
        if ((ofThis.getAttribute("pname.ges") != null) && !ofThis.getAttributeValue("pname.ges").equals("none")) {
            pname = ofThis.getAttributeValue("pname.ges");
        }
        else {
            if (ofThis.getAttribute("pname") != null) {
                pname = ofThis.getAttributeValue("pname");
                checkKeySign = true;                                    // the key signature must be checked for accidentals later on; this is done only when the non-gestural pname attribute has been used
            }
            else {                                                      // if no pitch class specified we cannot do anything
                return -1.0;                                            // cancel by returning -1
            }
        }

        // get the octave
        if (ofThis.getAttribute("oct.ges") != null) {                   // look for gestural oct attribute
            oct = Double.parseDouble(ofThis.getAttributeValue("oct.ges"));
        }
        else {
            if (ofThis.getAttribute("oct") != null) {                   // look for non-gestural oct attribute
                oct = Double.parseDouble(ofThis.getAttributeValue("oct"));
            }
            else {
                if (this.currentPart != null) {
                    Elements octs = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("oct.default");                              // get all local default octave
                    if (octs.size() == 0) {                                                                                                                                      // if there is none
                        octs = this.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("oct.default");// get all global default octave
                    }
                    for (int i = octs.size() - 1; i >= 0; --i) {                                                                          // search from back to front
                        if ((octs.get(i).getAttribute("layer") == null) || octs.get(i).getAttributeValue("layer").equals(layerId)) {  // for a default octave with no layer dependency or a matching layer
                            oct = Double.parseDouble(octs.get(i).getAttributeValue("oct.default"));                                     // take this value
                            break;                                                                                                    // break the for loop
                        }
                    }
                }
                ofThis.addAttribute(new Attribute("oct", Double.toString(oct)));                                                 // there was no oct attribute, so fill the gap with the computed value
            }
        }

        // get accidental
        if (ofThis.getAttribute("accid.ges") != null) {                 // look for gestural accid attribute
            accid = ofThis.getAttributeValue("accid.ges");
            checkKeySign = false;
        }
        else {
            if (ofThis.getAttribute("accid") != null) {                 // look for non-gestural accid attribute
                accid = ofThis.getAttributeValue("accid");              // store the accidental string
                if (!accid.isEmpty()) {
//                    this.accid.add(ofThis);                             // if not empty, insert it at the front of the accid list for reference when computing the pitch of later notes in this measure; this is done in Mei.processAccid() and Mei.processNote()
                    checkKeySign = false;
                }
            }
            else {                                                      // look for preceding accid elements, this includes accid child elements of the note as they were processed in advance
                for (int i = this.accid.size()-1; i >= 0; --i) {                                    // go through the accid list
                    Element anAccid = this.accid.get(i);
                    if ((anAccid.getAttribute("pname") != null)                                     // if it has a pname attribute
                            && (anAccid.getAttributeValue("pname").equals(pname))                   // the same pitch class as ofThis
                            && (anAccid.getAttribute("oct") != null)                                // has an oct attribute
                            && (Double.parseDouble(anAccid.getAttributeValue("oct")) == oct)) {     // the same octave transposition as ofThis

                        // read the accid.ges or accid attribute
                        if (anAccid.getAttribute("accid.ges") != null)
                            accid = anAccid.getAttributeValue("accid.ges");
                        else if (anAccid.getAttribute("accid") != null)
                            accid = anAccid.getAttributeValue("accid");

                        // local accidentals overrule the key signature, but an empty accid string is interpreted as no accid and, hence, does not overrule the key signature
                        checkKeySign = accid.isEmpty();

                        break;
                    }
                }
                if (checkKeySign) {                                                                                                 // if the note's pitch was defined by a pname attribute and had no local accidentals, we must check the key signature for accidentals
                    // get both, local and global keySignatureMap in the msm document and get the latest keySignature element in there, check its accidentals' pitch attribute if it is of the same pitch class as pname
                    Element keySigMapLocal = (this.currentPart == null) ? null : this.currentPart.getFirstChildElement("dated").getFirstChildElement("keySignatureMap");// get the local key signature map from mpm
                    Element keySigMapGlobal = this.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("keySignatureMap");  // get the global key signature map

                    Element keySigLocal = null;
                    if (keySigMapLocal != null) {
                        Elements keySigsLocal = keySigMapLocal.getChildElements("keySignature");                                    // get the local keySignature elements
                        for (int i = keySigsLocal.size() - 1; i >= 0; --i) {                                                        // search for the last key signature that ...
                            if ((keySigsLocal.get(i).getAttribute("layer") == null) || keySigsLocal.get(i).getAttributeValue("layer").equals(layerId)) {  // either has no layer dependency or has a matching layer attribute
                                keySigLocal = keySigsLocal.get(i);                                                                  // take this one
                                break;                                                                                              // break the for loop
                            }
                        }
                    }

                    Element keySigGlobal = null;
                    if (keySigMapGlobal != null) {
                        Elements keySigsGlobal = keySigMapGlobal.getChildElements("keySignature");                                  // get the global keySignature elements
                        for (int i = keySigsGlobal.size() - 1; i >= 0; --i) {                                                       // search for the last key signature that ...
                            if ((keySigsGlobal.get(i).getAttribute("layer") == null) || keySigsGlobal.get(i).getAttributeValue("layer").equals(layerId)) {  // either has no layer dependency or has a matching layer attribute (yes, a scoreDef can be within a layer in mei!)
                                keySigGlobal = keySigsGlobal.get(i);                                                                // take this one
                                break;                                                                                              // break the for loop
                            }
                        }
                    }

                    Element keySig = keySigLocal;                                                                                   // start with the local key signature
                    if ((keySig == null)                                                                                            // if no local keySignature
                            || ((keySigGlobal != null)                                                                              // or a global key signature ...
                            && (Double.parseDouble(keySigLocal.getAttributeValue("date")) < Double.parseDouble(keySigGlobal.getAttributeValue("date"))))) {    // that is later than the local key signature
                        keySig = keySigGlobal;                                                                                      // take the global

                        // Shall the global keySignature element be added to the local map? Yes, this makes a correct msm representation of might be meant in mei. No, this is not what is encoded in mei.
                        // Trade-off: Do it only if the local map is not empty. Caution, as long as the local map is empty, global entries aill not be copied and will be missing in the resulting msm.
                        // Why doing this here and not in method Mei.makeKeySignature()? In mei the first key signature definition may occur before any staffs (parts in msm) are generated.
                        assert keySigMapLocal != null;                                                                              // there should always be a local key signature map, because it is automatically created when the part is created
                        if ((keySigGlobal != null) && (keySigMapLocal.getChildCount() > 0)) {                                       // if the global keySignature element was not null and the local map is not empty
                            addToMap((Element)keySigGlobal.copy(), keySigMapLocal);                                                 // make a deep copy of the global keySignature element and append it to the local map
                        }
                    }

                    if (keySig != null) {                                                                                       // if we have a key signature
                        Elements keySigAccids = keySig.getChildElements("accidental");                                          // get its accidentals
                        for (int i = 0; i < keySigAccids.size(); ++i) {                                                         // check the accidentals for a matching pitch class
                            Element a = keySigAccids.get(i);                                                                    // take an accidental
                            double aPitch;
                            if (a.getAttribute("midi.pitch") != null)                                                           // if it has a midi.pitch atrtibute
                                aPitch = Double.parseDouble(a.getAttributeValue("midi.pitch"));                                 // get its pitch value
                            else if (a.getAttribute("pitchname") != null)                                                       // else if it has a pitchname attribute
                                aPitch = Helper.pname2midi(a.getAttributeValue("pitchname"));                                   // get its pitch value
                            else                                                                                                // without a midi.pitch and pitchname attribute the accidental is invalid
                                continue;                                                                                       // hence, continue with the next
                            double pitchOfThis = Helper.pname2midi(pname) % 12;                                                 // get the current note's pitch as midi value modulo 12
                            if (aPitch == pitchOfThis) {                                                                        // the accidental indeed affects the pitch ofThis
                                accid = a.getAttributeValue("value");                                                           // get the accidental's value
                                break;                                                                                          // done here, break the for loop
                            }
                        }
                    }
                }
            }
        }

        // transpositions
        if ((ofThis.getAttribute("pname.ges") == null) || (ofThis.getAttribute("oct.ges") == null)) {                                                                   // if pname.ges or oct.ges are given, it already includes transpositions
            // transposition; check for global and local transposition and addTransposition elements in the miscMaps; global and local transpositions add up; so-called addTranspositions (e.g. octaves) also add to the usual transpositions
            // go through all four lists and check for elements that apply here, global and local transpositions add up
            {
                Elements globalTrans = this.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("transposition");
                for (int i = globalTrans.size() - 1; i >= 0; --i) {                                                                                                     // go through the global transpositions
                    if ((globalTrans.get(i).getAttributeValue("date") != null) && (Double.parseDouble(globalTrans.get(i).getAttributeValue("date")) > this.getMidiTime())) {  // if this transposition element is after ofThis
                        continue;                                                                                                                                       // continue searching
                    }
                    if ((globalTrans.get(i).getAttribute("date.end") != null) && (Double.parseDouble(globalTrans.get(i).getAttributeValue("date.end")) <= this.getMidiTime())) {   // if it is before ofThis but the end date of this transposition (if one is specified) is before or at oThis
                        break;                                                                                                                                          // done
                    }
                    if (!Helper.isSameLayer(globalTrans.get(i), layerId)) {                                                                                             // check whether this transposition is dedicated to a specific layer but not the current layer (layer of ofThis)
                        continue;
                    }
                    trans += Double.parseDouble(globalTrans.get(i).getAttributeValue("semi"));                                                                          // found a transposition that applies
                    break;                                                                                                                                              // done
                }
            }
            {
                Elements globalAddTrans = this.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("addTransposition");
                for (int i = globalAddTrans.size() - 1; i >= 0; --i) {                                                                                                  // go through the global addTranspositions
                    if ((globalAddTrans.get(i).getAttributeValue("date") != null) && (Double.parseDouble(globalAddTrans.get(i).getAttributeValue("date")) > this.getMidiTime())) {    // if this transposition element is after ofThis
                        continue;
                    }
                    if ((globalAddTrans.get(i).getAttribute("date.end") != null) && ((Double.parseDouble(globalAddTrans.get(i).getAttributeValue("date.end")) <= this.getMidiTime()))) {   // if it is before or at ofThis but the end date of this transposition (if one is specified) is before oThis
                        continue;
                    }
                    if (!Helper.isSameLayer(globalAddTrans.get(i), layerId)) {                                                                                          // check whether this transposition is dedicated to a specific layer but not the current layer (layer of ofThis)
                        continue;
                    }
                    trans += Double.parseDouble(globalAddTrans.get(i).getAttributeValue("semi"));                                                                       // found a transposition that applies
                }
            }
            if (this.currentPart != null) {
                {
                    Elements localTrans = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("transposition");
                    for (int i = localTrans.size() - 1; i >= 0; --i) {                                                                                                      // go through the local transpositions
                        if ((localTrans.get(i).getAttributeValue("date") != null) && (Double.parseDouble(localTrans.get(i).getAttributeValue("date")) > this.getMidiTime())) {// if this transposition element is after ofThis
                            continue;
                        }
                        if ((localTrans.get(i).getAttribute("date.end") != null) && (Double.parseDouble(localTrans.get(i).getAttributeValue("date.end")) <= this.getMidiTime())) {     // if it is before or at ofThis but the end date of this transposition (if one is specified) is before oThis
                            break;
                        }
                        if (!Helper.isSameLayer(localTrans.get(i), layerId)) {                                                                                              // check whether this transposition is dedicated to a specific layer but not the current layer (layer of ofThis)
                            continue;
                        }
                        trans += Double.parseDouble(localTrans.get(i).getAttributeValue("semi"));                                                                           // found a transposition that applies
                        break;                                                                                                                                              // done
                    }
                }
                {
                    Elements localAddTrans = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("addTransposition");
                    for (int i = localAddTrans.size() - 1; i >= 0; --i) {                                                                                                  // go through the global addTranspositions
                        if ((localAddTrans.get(i).getAttributeValue("date") != null) && (Double.parseDouble(localAddTrans.get(i).getAttributeValue("date")) > this.getMidiTime())) {  // if this transposition element is after ofThis
                            continue;
                        }
                        if ((localAddTrans.get(i).getAttribute("date.end") != null) && (Double.parseDouble(localAddTrans.get(i).getAttributeValue("date.end")) <= this.getMidiTime())) {   // if it is before or at ofThis but the end date of this transposition (if one is specified) is before oThis
                            continue;
                        }
                        if (!Helper.isSameLayer(localAddTrans.get(i), layerId)) {                                                                                            // check whether this transposition is dedicated to a specific layer but not the current layer (layer of ofThis)
                            continue;
                        }
                        trans += Double.parseDouble(localAddTrans.get(i).getAttributeValue("semi"));                                                                         // found a transposition that applies
                    }
                }
            }
        }

        double pitch = Helper.pname2midi(pname);            // here comes the result
        if (pitch == -1.0)                                  // if no valid pitch name found
            return -1.0;                                    // cancel

        double initialPitch = pitch;                        // need this to compute the untransposed pitchname for the pitchdata list

        // octave transposition that is directly at the note as an attribute oct
        pitch += 12 * (oct + 1);

        // accidentals
        double accidentals = (checkKeySign) ? ((accid.isEmpty()) ? 0.0 : Double.parseDouble(accid)) : Helper.accidString2decimal(accid);    // if the accidental string was taken from the msm key signature it is already numeric, otherwise it is still an mei accidental string
        pitch += accidentals;

        // transposition
        pitch += trans;

        // fill the pitchdata list
        int p1 = (int)(initialPitch + (12 * oct) + trans);  // pitch without accidentals
        int p2 = p1 % 12;                                   // pitch class without accidentals
        double outputOct = ((double)(p1 - p2) / 12) - 1;    // octave (the lowest octave in midi is -1 in common western notation)
        double outputAcc = accidentals;                     // accidentals for output (have to include accidentals that are introduced by transposition)
        String pitchname = pname;                           // determine pitchname (may differ from pname because of transposition), here comes the result
        if (trans != 0) {                                   // because of transposition, things become a bit more complicated, as accidentals that are introduced by the transposition have to be added to the regular accidentals
            switch (p2) {
                case 0:
                    pitchname = "c";
                    break;
                case 1:
                    if (trans > 0) { pitchname = "c"; outputAcc += 1; }
                    else { pitchname = "d"; outputAcc -= 1; }
                    break;
                case 2:
                    pitchname = "d";
                    break;
                case 3:
                    if (trans > 0) { pitchname = "d"; outputAcc += 1; }
                    else { pitchname = "e"; outputAcc -= 1; }
                    break;
                case 4:
                    pitchname = "e";
                    break;
                case 5:
                    pitchname = "f";
                    break;
                case 6:
                    if (trans > 0) { pitchname = "f"; outputAcc += 1; }
                    else { pitchname = "g"; outputAcc -= 1; }
                    break;
                case 7:
                    pitchname = "g";
                    break;
                case 8:
                    if (trans > 0) { pitchname = "g"; outputAcc += 1; }
                    else { pitchname = "a"; outputAcc -= 1; }
                    break;
                case 9:
                    pitchname = "a";
                    break;
                case 10:
                    if (trans > 0) { pitchname = "a"; outputAcc += 1; }
                    else { pitchname = "b"; outputAcc -= 1; }
                    break;
                case 11:
                    pitchname = "b";
            }
        }
        pitchdata.add(pitchname);
        pitchdata.add(Double.toString(outputAcc));
        pitchdata.add(Double.toString(outputOct));

        return pitch;
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
