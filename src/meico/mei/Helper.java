package meico.mei;

/**
 * This class is used for mei to msm conversion to hold temporary data, used in class Mei.
 * @author Axel Berndt.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory;
import meico.msm.Msm;
import nu.xom.*;
import nu.xom.xslt.XSLException;
import nu.xom.xslt.XSLTransform;
import org.xml.sax.SAXException;
import javax.xml.transform.stream.StreamSource;

public class Helper {

    protected int ppq = 720;                                            // default value for pulses per quarter
    protected int endingCounter = 0;                                    // a counter of ending elements in the mei source
    protected boolean dontUseChannel10 = true;                          // set this flag false if you allow to "misuse" the midi drum channel for other instruments; in standard midi output this produces weird results, but when you work with vst plugins etc. there is no reason to avoid channel 10
    protected Element currentMovement = null;
    protected Element currentPart = null;                               // this points to the current part element in the msm
    protected Element currentLayer = null;                              // this points to the current layer element in the mei source
    protected Element currentMeasure = null;
    protected Element currentChord = null;
    protected ArrayList<Element> accid = new ArrayList<Element>();      // holds accidentals that appear within measures to be considered during pitch computation
    protected ArrayList<Element> endids = new ArrayList<Element>();
    protected List<Msm> movements = new ArrayList<Msm>();

    /** constructor
     *
     */
    protected Helper() {
    }

    /** constructor
     *
     * @param ppq
     */
    protected Helper(int ppq) {
        this.ppq = ppq;
    }

    /** this method is called when making a new movement
     *
     */
    protected void reset() {
        this.endingCounter = 0;
        this.currentMovement = null;
        this.currentPart = null;
        this.currentLayer = null;
        this.currentMeasure = null;
        this.currentChord = null;
        this.accid.clear();
        this.endids.clear();
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
     *
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

    /** get the next sibling element of ofThis irrespective of its name
     *
     * @param ofThis
     * @return
     */
    public static Element getNextSiblingElement(Element ofThis) {
        if (ofThis == null) return null;

        if (ofThis == ofThis.getDocument().getRootElement())                // if we are at the root of the document
            return null;                                                    // there can be no siblings, hence return null

        Elements es = ((Element)ofThis.getParent()).getChildElements();     // get a list of all siblings

        for (int i = es.size()-2; i >= 0; i--) {                            // go through all siblings starting at the element before the last (the last one cannot have a successor)
            if (ofThis == es.get(i)) {                                      // if ofThis was found
                return es.get(i+1);                                         // the successor is the next sibling
            }
        }

        return null;                                                        // ofThis is the final element and has no next sibling
    }

    /** get the next sibling element of ofThis with the given name
     *
     * @param name
     * @param ofThis
     * @return
     */
    public static Element getNextSiblingElement(String name, Element ofThis) {
        if (ofThis == null) return null;

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

    /** get the previous sibling element of ofThis irrespective of its name
     *
     * @param ofThis
     * @return
     */
    public static Element getPreviousSiblingElement(Element ofThis) {
        if (ofThis == null) return null;

        if (ofThis == ofThis.getDocument().getRootElement())                // if we are at the root of the document
            return null;                                                    // there can be no siblings, hence return null

        Elements es = ((Element)ofThis.getParent()).getChildElements();     // get a list of all siblings

        for (int i=1; i < es.size(); ++i) {                                 // go through all siblings starting at the second (the first cannot have a predecessor)
            if (ofThis == es.get(i)) {                                      // if ofThis was found
                return es.get(i-1);                                         // the predecessor is the previous sibling
            }
        }

        return null;                                                        // ofThis is the final element and has no next sibling
    }

    /** get the previous sibling element of ofThis with a specific name
     *
     * @param name
     * @param ofThis
     * @return
     */
    public static Element getPreviousSiblingElement(String name, Element ofThis) {
        if (ofThis == null) return null;

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
     * this function became necessary because the XOM methods sometimes do not seem to work for whatever reason
     * @param name
     * @param ofThis
     * @return
     */
    public static Element getFirstChildElement(String name, Element ofThis) {
        for (int i=0; i < ofThis.getChildElements().size(); ++i) {
            if (ofThis.getChildElements().get(i).getLocalName().equals(name)) {
                return ofThis.getChildElements().get(i);
            }
        }
        return null;
    }

    /**
     * this method adds element addThis to a timely sequenced list, the map, and ensures the timely order of the elements in the map;
     * therefore, addThis must contain the attribute "midi.date"; if not, addThis is appended at the end
     * @param addThis an xml element (should have an attribute midi.date)
     * @param map a timely sequenced list of elements with attribute midi.date
     * @return the index of the element in the map or -1 if insertion failed
     */
    public static int addToMap(Element addThis, Element map) {
        if ((map == null) || (addThis == null))                                     // no map or no element to insert
            return -1;                                                              // no insertion

        if (addThis.getAttribute("midi.date") == null) {                            // no attribute midi.date
            map.appendChild(addThis);                                               // simply append addThis to the end of the map
            return map.getChildCount()-1;                                           // and return the index
        }

        Nodes es = map.query("descendant::*[attribute::midi.date]");                // get all elements in the map that have an attribute midi.date
        if (es.size() == 0) {                                                       // if there are no elements in the map with a midi.date attribute
            map.appendChild(addThis);                                               // simply append addThis to the end of the map
            return map.getChildCount()-1;                                           // and return the index
        }

        double date = Double.parseDouble(addThis.getAttributeValue("midi.date"));   // get the date of addThis
        for (int i = es.size()-1; i >= 0; --i) {                                    // go through the elements
            if (Double.parseDouble(((Element)es.get(i)).getAttributeValue("midi.date")) <= date) {  // if the element directly before date is found
                int index = map.indexOf(es.get(i));                                 // get the index of the element just found
                map.insertChild(addThis, ++index);                                  // insert addThis right after the element
                return index;                                                       // return the index
            }
        }

        // if all elements in the map had a date later than addThis's date
        map.insertChild(addThis, 0);                                                // insert addThis at the front of the map (as first child)
        return 0;                                                                   // return the index
    }

    /** compute the midi time of an mei element
     *
     * @return
     */
    protected double getMidiTime() {
        if (this.currentPart != null)                                                       // if we are within a staff environment
            return Double.parseDouble(this.currentPart.getAttributeValue("currentDate"));   // we have a more precise date somewhere within a measure

        if (this.currentMeasure != null)                                                    // if we are within a measure
            return Double.parseDouble(this.currentMeasure.getAttributeValue("midi.date"));  // take its take

        if (this.currentMovement == null)                                                   // if we are outside of any movement
            return 0.0;                                                                     // return 0.0

        // go through all parts, determine the latest currentDate and return it
        Elements parts = this.currentMovement.getChildElements("part");                     // get the list of all parts
        double latestDate = 0.0;                                                            // here comes the result
        for (int i = parts.size()-1; i >= 0; --i) {                                         // go through that list
            double date = Double.parseDouble(parts.get(i).getAttributeValue("currentDate"));// get the part's date
            if (latestDate < date)                                                          // if this part's date is later than latestDate so far
                latestDate = date;                                                          // set latestDate to date
        }
        return latestDate;                                                                  // return the latest date of all parts
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

    /** compute the length of one measure in midi ticks at the currentDate in the currentPart of the currentMovement; if no time signature information available it returns the length of a 4/4 measure
     *
     * @return
     */
    protected double getOneMeasureLength() {
        // get the value of one measure from the local or global timeSignatureMap
        Elements es = this.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
        if (es.size() == 0) {                                                                                                                                               // if local map empty
            es = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");    // get global entries
        }

        // get length of one measure (4/4 is default if information is insufficient)
        int denom = (es.size() == 0) ? 4 : Integer.parseInt(es.get(es.size()-1).getAttributeValue("denominator"));
        int num = (es.size() == 0) ? 4 : Integer.parseInt(es.get(es.size()-1).getAttributeValue("numerator"));

        return (4.0 * this.ppq * num) / denom;

    }

    /** compute the length of one measure with specified numerator and denominator values (the underlying time signature)
     *
     * @param numerator
     * @param denominator
     * @return
     */
    protected double computeMeasureLength(double numerator, int denominator) {
        return (4.0 * this.ppq * numerator) / denominator;

    }

    /** create a flat copy of element e including its attributes but not its child elements
     *
     * @param e
     * @return
     */
    public static Element cloneElement(Element e) {
        if (e == null) return null;

        Element clone = new Element(e.getLocalName());
        for (int i = e.getAttributeCount()-1; i >= 0; --i) {
            clone.addAttribute(new Attribute(e.getAttribute(i).getLocalName(), e.getAttribute(i).getValue()));
        }

        return clone;
    }

    /** returns the attribute with the specified name contained in ofThis, or null if that attribute does not exist, namespace is ignored
     *
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

    /** returns the vale of attribute name in Element ofThis as String, or empty string if attribute does not exist, namespace is ignored
     *
     * @param name
     * @param ofThis
     * @return
     */
    public static String getAttributeValue(String name, Element ofThis) {
        Attribute a = getAttribute(name, ofThis);
        if (a == null) return "";
        return a.getValue();
    }

    /**copies the id attribute ofThis into toThis
     *
     * @param ofThis
     * @param toThis
     */
    protected static void copyId(Element ofThis, Element toThis) {
//        copyIdNoNs(ofThis, toThis);
        copyIdNs(ofThis, toThis);
    }

    /** copies the id attribute from ofThis (if present) into toThis, without namespace
     *
     * @param ofThis
     * @param toThis
     */
    private static void copyIdNoNs(Element ofThis, Element toThis) {
        if (getAttribute("id", ofThis) != null) toThis.addAttribute(new Attribute("id", getAttributeValue("id", ofThis)));    // copy the id
    }

    /** copies the id attribute from ofThis (if present) into toThis, retaining its namespace
     *
     * @param ofThis
     * @param toThis
     */
    private static void copyIdNs(Element ofThis, Element toThis) {
        if (getAttribute("id", ofThis) != null) toThis.addAttribute((Attribute) getAttribute("id", ofThis).copy());    // copy the id including namespace
    }

    /** returns the parent element of ofThis as element or null
     *
     * @param ofThis
     * @return
     */
    public static Element getParentElement(Element ofThis) {
        for (Node e = ofThis.getParent(); e != ofThis.getDocument().getRootElement(); e = e.getParent()) {
            if (e instanceof Element) return (Element)e;
        }
        return null;
    }

    /** return part entry in current movement or null
     *
     * @param id
     * @return
     */
    protected Element getPart(String id) {
        if ((id == null) || (id.isEmpty())) return null;

        Elements parts = this.currentMovement.getChildElements("part");

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
            if ((e instanceof Element) && (((Element)e).getLocalName().equals("layer"))) {                  // found one
                return (Element)e;
            }
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
        else if (layer.getAttribute("n") != null) {
            toThis.addAttribute(new Attribute("layer", layer.getAttributeValue("n")));
        }
    }

    /** cleanup of the msm objects to remove all conversion related and no longer needed entries in the msm objects (miscMaps, currentDate and tie attributes)
     *
     * @param msms
     */
    protected static void msmCleanup(List<Msm> msms) {
        for (int i=0; i < msms.size(); ++i) {                       // go through all msm objects in the input list
            msmCleanup(msms.get(i));                                // make the cleanup
        }
    }

    /** make the cleanup of one msm object; this removes all miscMaps, currentDate, tie, and layer attributes
     *
     * @param msm
     */
    protected static void msmCleanup(Msm msm) {
        // delete all miscMaps
        Nodes n = msm.getRootElement().query("descendant::*[local-name()='miscMap'] | descendant::*[attribute::currentDate]/attribute::currentDate | descendant::*[attribute::tie]/attribute::tie | descendant::*[attribute::layer]/attribute::layer | descendant::*[local-name()='goto' and attribute::n]/attribute::n");
        for (int i=0; i < n.size(); ++i) {
            if (n.get(i) instanceof Element)
                n.get(i).getParent().removeChild(n.get(i));

            if (n.get(i) instanceof Attribute)
                ((Element) n.get(i).getParent()).removeAttribute((Attribute) n.get(i));
        }
        msm.deleteEmptyMaps();
    }

    /** return the first element in the endids list with an endid attribute value that equals id
     *
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

    /** check for pending elements with endid attributes to be finished when the element with this endid is found
     *
     * @param e
     */
    protected void checkEndid(Element e) {
        String id = "#" + Helper.getAttributeValue("id", e);                // get id of the current element
        for (int j = this.getEndid(id); j >= 0; j = this.getEndid(id)) {    // find all pending elements in the endid list to be finished at this element
            this.endids.get(j).addAttribute(new Attribute("end", Double.toString(this.getMidiTime() + this.computeDuration(e))));     // finish corresponding element
            this.endids.remove(j);                                          // remove element from list, it is finished
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
            gt.addAttribute(new Attribute("midi.date", Double.toString(date)));     // give it the date
            gt.addAttribute(new Attribute("activity", "1"));                        // process this goto at the first time, later on ignore it
            gt.addAttribute(new Attribute("target.date", "0"));                     // add the target.date attribute by default initialized with "0" (which means to start from the beginning)
            gt.addAttribute(new Attribute("target.id", ""));                        // add an empty target.id attribute (which means to start from the beginning)
            int index = Helper.addToMap(gt, sequencingMap);                         // insert the goto into the sequencingMap and store its index because we need to find the marker to jump to
            Nodes ns = sequencingMap.query("descendant::*[local-name()='marker' and attribute::message='repetition start']");       // get all the markers that are repetition start points
            for (int i = ns.size()-1; i >= 0; --i) {                                                                                // check them from back to front and find
                Element n = (Element)ns.get(i);                                                                                     // the element
                if (Double.parseDouble(n.getAttributeValue("midi.date")) < date) {                                                  // that has a midi.date right before the goto's midi.date
                    gt.getAttribute("target.date").setValue(n.getAttributeValue("midi.date"));                                      // take this as jump's target date
                    gt.getAttribute("target.id").setValue("#" + n.getAttributeValue("id", "http://www.w3.org/XML/1998/namespace")); // take this as the jump's target marker
                    break;                                                                                                          // done
                }
            }                                                                                                                       // if nothing was found in this for loop, target.date and target.id remain as initialized
        }

        // generate a marker (potential jump target) and insert it into the sequencingMap
        if (markerMessage != null) {                                                // if a marker should be generated
            Element marker = new Element("marker");                                 // do so
            marker.addAttribute(new Attribute("midi.date", Double.toString(date))); // give it a midi.date
            marker.addAttribute(new Attribute("message", markerMessage));           // set its message
            Attribute id = new Attribute("id", UUID.randomUUID().toString());       // give it a UUID
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
    protected static int processClefDis(Element scoreStaffDef) {
        return 0;

//        int oct = 0;
//        if (scoreStaffDef.getAttribute("clef.dis") != null)  {
//            switch (scoreStaffDef.getAttributeValue("clef.dis")) {
//                case "8":
//                    oct = 12;
//                    break;
//                case "15":
//                    oct = 24;
//                    break;
//                case "22":
//                    oct = 32;
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
//                oct = 0;
//        }
//
//        return oct;
    }

    /** compute midi tick duration of a note or rest, if fail return 0.0;
     * the stuff from data.DURATION.gestural is not supported! Because we need pure note values here.
     *
     * @param ofThis
     * @return
     */
    protected Double computeDuration(Element ofThis) {
        if (!ofThis.getLocalName().equals("bTrem")                  // for what kind of element shall the duration be computed?
                && !ofThis.getLocalName().equals("chord")
                && !ofThis.getLocalName().equals("fTrem")
                && !ofThis.getLocalName().equals("halfmRpt")
                && !ofThis.getLocalName().equals("mRest")
                && !ofThis.getLocalName().equals("mSpace")
                && !ofThis.getLocalName().equals("note")
                && !ofThis.getLocalName().equals("octave")
                && !ofThis.getLocalName().equals("rest")
                && !ofThis.getLocalName().equals("tuplet")
                && !ofThis.getLocalName().equals("space")) {        // if none of these
            return 0.0;                                             // return 0.0
        }

        double dur;                                                                         // here comes the resultant note/rest duration in midi ticks
        boolean chordEnvironment = (this.currentChord != null);                             // if we are in a chord environment set this true, else false
        Element focus = ofThis;                                                             // this will change to the chord environment, if there is one

        { // get basic duration (without dots, tuplets etc.)
            String sdur = "";                                                                       // the dur string

            if (ofThis.getAttribute("dur") != null) {                                               // if there is a dur attribute
                sdur = focus.getAttributeValue("dur");
            }
            else {
                if (chordEnvironment && (this.currentChord.getAttribute("dur") != null)) {          // if a chord environment defines a duration
                    focus = this.currentChord;                                                      // from now on, look only in the chord environment for all further duration related attributes
                    sdur = focus.getAttributeValue("dur");                                          // take this
                }
                else {                                                                              // check for local and global default durations with and without layer consideration
                    String layerId = getLayerId(getLayer(ofThis));                                  // store the layer id
                    Elements durdefaults = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("dur.default");                              // get all local default durations
                    if (durdefaults.size() == 0) {                                                                                                                                      // if there is none
                        durdefaults = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("dur.default");// get all global default durations
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
                for (Element t = focus.getFirstChildElement("dot"); t != null; t = Helper.getNextSiblingElement("dot", t)) {    // count the number of dot tags (one for each dot)
                    dots++;
                }
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
        Elements tps = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getFirstChildElement("tupletSpanMap").getChildElements("tupletSpan");     // get all tupletSpans in the map
        for (int i = tps.size()-1; i >= 0; --i) {                                                                                                               // go through all these tupletSpans, starting with the last
            if ((tps.get(i).getAttribute("end") != null) && (Double.parseDouble(tps.get(i).getAttributeValue("end")) <= this.getMidiTime())) {                  // if the tupletSpan is already over
                this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getFirstChildElement("tupletSpanMap").removeChild(tps.get(i));   // remove this tupletSpan from the map, it is no longer needed
                continue;                                                                                                                                       // continue with the previous element in tps
            }
            if ((Helper.getAttribute("endid", tps.get(i)) != null) && (Helper.getAttribute("id", ofThis) != null) && (Helper.getAttributeValue("endid", tps.get(i)).equals("#" + Helper.getAttributeValue("id", ofThis)))) {   // if the tupletSpan ends with ofThis
                this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getFirstChildElement("tupletSpanMap").removeChild(tps.get(i));   // remove this tupletSpan from the map, it is no longer needed
            }
            if ((tps.get(i).getAttribute("layer") != null) && !tps.get(i).getAttributeValue("layer").equals(Helper.getLayerId(this.currentLayer))) {            // if the tupletSpan is dedicated to a particular layer but the current layer is a different one
                continue;                                                                                                                                       // don't use this tupletSpan here, continue with the next
            }
            dur *= Double.parseDouble(tps.get(i).getAttributeValue("numbase")) / Integer.parseInt(tps.get(i).getAttributeValue("num"));                         // scale dur: dur*numbase/num ... this loop does not break here, because of the possibility of tuplets within tuplets
            // This calculation can come with numeric error. That error is given across to the onset time of succeeding notes. We compensate this error by making a clean currentTime computation with each measure element, so the error does not propagate beyond barlines.
        }

        return dur;
    }

    /** convert the duration string into decimal (e.g., 4 -> 1/4) and returns the result
     *
     * @param durString
     * @return
     */
    public static double duration2decimal(String durString) {
        switch (durString) {
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

    /** compute midi pitch of an mei note or return -1.0 if failed; the return is a double number that captures microtonality, too; 0.5 is a quarter tone
     * parameter pitchdata should be an empty ArrayList<String>, it is filled with pitchname, accidentals and octave of the computed midi pitch for further use
     *
     * @param ofThis
     * @param pitchdata
     * @return
     */
    protected double computePitch(Element ofThis, ArrayList<String> pitchdata) {
        String pname;                                                   // the attribute strings
        String accid = "";                                              // the accidental string
        String layerId = getLayerId(getLayer(ofThis));                  // get the current layer's id reference
        int oct = 0;                                                    // octave transposition value
        int trans = 0;                                                  // transposition
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
                return -1;                                              // cancel by returning -1
            }
        }

        // get the octave
        if (ofThis.getAttribute("oct.ges") != null) {                   // look for gestural oct attribute
            oct = Integer.parseInt(ofThis.getAttributeValue("oct.ges"));
        }
        else {
            if (ofThis.getAttribute("oct") != null) {                   // look for non-gestural oct attribute
                oct = Integer.parseInt(ofThis.getAttributeValue("oct"));
            }
            else {
                Elements octs = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("oct.default");                              // get all local default octave
                if (octs.size() == 0) {                                                                                                                                      // if there is none
                    octs = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("oct.default");// get all global default octave
                }
                for (int i=octs.size()-1; i >= 0; --i) {                                                                          // search from back to front
                    if ((octs.get(i).getAttribute("layer") == null) || octs.get(i).getAttributeValue("layer").equals(layerId)) {  // for a default octave with no layer dependency or a matching layer
                        oct = Integer.parseInt(octs.get(i).getAttributeValue("oct.default"));                                     // take this value
                        break;                                                                                                    // break the for loop
                    }
                }
                ofThis.addAttribute(new Attribute("oct", Integer.toString(oct)));                                                 // there was no oct attribute, so fill the gap with the computed value
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
                    this.accid.add(ofThis);                             // if not empty, insert it at the front of the accid list for reference when computing the pitch of later notes in this measure
                    checkKeySign = false;
                }
            }
            else {
                Element accidElement = getFirstChildElement("accid", ofThis);                                           // is there an accid child element instead of an attribute?
                if (accidElement != null) {
                    if (accidElement.getAttribute("accid.ges") != null) {                                               // does it have an accid.ges attribute
                        ofThis.addAttribute(new Attribute("accid.ges", accidElement.getAttributeValue("accid.ges")));   // make an attribute of it
                        accid = ofThis.getAttributeValue("accid.ges");                                                  // store the accidental string
                    }
                    else {
                        if (accidElement.getAttribute("accid") != null) {                                               // does it have an accid attribute
                            ofThis.addAttribute(new Attribute("accid", accidElement.getAttributeValue("accid")));       // make an attribute of it
                            accid = ofThis.getAttributeValue("accid");                                                  // store the accidental string
                        }
                    }
                    if (!accid.isEmpty()) {
                        this.accid.add(ofThis);                                                                         // if not empty, insert it at the front of the accid list for reference when computing the pitch of later notes in this measure
                        checkKeySign = false;
                    }
                }
                else {                                                                                                  // otherwise look for preceding accidentals in this measure
                    for (Element anAccid : this.accid) {                                                                // go through the accid list
                        if ((anAccid.getAttribute("pname") != null)                                                     // if it has a pitch attribute
                                && (anAccid.getAttributeValue("pname").equals(pname))                                   // the same pitch class as ofThis
                                && (anAccid.getAttribute("oct") != null)                                                // has an oct attribute
                                && (anAccid.getAttributeValue("oct").equals(Integer.toString(oct)))) {                  // the same octave transposition as ofThis

                            accid = anAccid.getAttributeValue("accid");                                                 // apply its accid attribute
                            checkKeySign = false;                                                                       // local accidentals overrule the key signature
                            break;                                                                                      // stop the for loop
                        }
                    }
                    if (checkKeySign) {                                                                                                 // if the note's pitch was defined by a pname attribute and had no local accidentals, we must check the key signature for accidentals
                        // get both, local and global keySignatureMap in the msm document and get the latest keySignature element in there, check its accidentals' pitch attribute if it is of the same pitch class as pname
                        Element keySigMapLocal = this.currentPart.getFirstChildElement("dated").getFirstChildElement("keySignatureMap");// get the local key signature map from mpm
                        Element keySigMapGlobal = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("keySignatureMap");  // get the global key signature map

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
                                && (Double.parseDouble(keySigLocal.getAttributeValue("midi.date")) < Double.parseDouble(keySigGlobal.getAttributeValue("midi.date"))))) {    // that is later than the local key signature
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
        }

        // transpositions
        if ((ofThis.getAttribute("pname.ges") == null) || (ofThis.getAttribute("oct.ges") == null)) {                                                                   // if pname.ges or oct.ges are given, it already includes transpositions
            // transposition; check for global and local transposition and addTransposition elements in the miscMaps; global and local transpositions add up; so-called addTranspositions (e.g. octaves) also add to the usual transpositions
            // go through all four lists and check for elements that apply here, global and local transpositions add up
            {
                Elements globalTrans = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("transposition");
                for (int i = globalTrans.size() - 1; i >= 0; --i) {                                                                                                     // go through the global transpositions
                    if ((globalTrans.get(i).getAttributeValue("midi.date") != null) && Double.parseDouble(globalTrans.get(i).getAttributeValue("midi.date")) > this.getMidiTime()) {  // if this transposition element is after ofThis
                        continue;                                                                                                                                       // continue searching
                    }
                    if ((globalTrans.get(i).getAttribute("end") != null) && (Double.parseDouble(globalTrans.get(i).getAttributeValue("end")) < this.getMidiTime())) {   // if it is before ofThis but the end date of this transposition (if one is specified) is before oThis
                        break;                                                                                                                                          // done
                    }
                    if ((globalTrans.get(i).getAttribute("layer") != null) && !globalTrans.get(i).getAttributeValue("layer").equals(layerId)) {                         // if this transposition is dedicated to a specific layer but not the current layer (layer of ofThis)
                        continue;                                                                                                                                       // continue searching
                    }
                    trans += Integer.parseInt(globalTrans.get(i).getAttributeValue("semi"));                                                                            // found a transposition that applies
                    break;                                                                                                                                              // done
                }
            }
            {
                Elements globalAddTrans = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("addTransposition");
                for (int i = globalAddTrans.size() - 1; i >= 0; --i) {                                                                                                  // go through the global addTranspositions
                    if ((globalAddTrans.get(i).getAttributeValue("midi.date") != null) && Double.parseDouble(globalAddTrans.get(i).getAttributeValue("midi.date")) > this.getMidiTime()) {    // if this transposition element is after ofThis
                        continue;
                    }
                    if ((globalAddTrans.get(i).getAttribute("end") != null) && (Double.parseDouble(globalAddTrans.get(i).getAttributeValue("end")) < this.getMidiTime())) {   // if it is before ofThis but the end date of this transposition (if one is specified) is before oThis
                        continue;
                    }
                    if ((globalAddTrans.get(i).getAttribute("layer") != null) && !globalAddTrans.get(i).getAttributeValue("layer").equals(layerId)) {                   // if this transposition is dedicated to a specific layer but not the current layer (layer of ofThis)
                        continue;                                                                                                                                       // continue searching
                    }
                    trans += Integer.parseInt(globalAddTrans.get(i).getAttributeValue("semi"));                                                                         // found a transposition that applies
                }
            }
            {
                Elements localTrans = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("transposition");
                for (int i = localTrans.size() - 1; i >= 0; --i) {                                                                                                      // go through the local transpositions
                    if ((localTrans.get(i).getAttributeValue("midi.date") != null) && Double.parseDouble(localTrans.get(i).getAttributeValue("midi.date")) > this.getMidiTime()) {// if this transposition element is after ofThis
                        continue;
                    }
                    if ((localTrans.get(i).getAttribute("end") != null) && (Double.parseDouble(localTrans.get(i).getAttributeValue("end")) < this.getMidiTime())) {     // if it is before ofThis but the end date of this transposition (if one is specified) is before oThis
                        break;
                    }
                    if ((localTrans.get(i).getAttribute("layer") != null) && !localTrans.get(i).getAttributeValue("layer").equals(layerId)) {                           // if this transposition is dedicated to a specific layer but not the current layer (layer of ofThis)
                        continue;                                                                                                                                       // continue searching
                    }
                    trans += Integer.parseInt(localTrans.get(i).getAttributeValue("semi"));                                                                             // found a transposition that applies
                    break;                                                                                                                                              // done
                }
            }
            {
                Elements localAddTrans = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("addTransposition");
                for (int i = localAddTrans.size() - 1; i >= 0; --i) {                                                                                                  // go through the global addTranspositions
                    if ((localAddTrans.get(i).getAttributeValue("midi.date") != null) && Double.parseDouble(localAddTrans.get(i).getAttributeValue("midi.date")) > this.getMidiTime()) {  // if this transposition element is after ofThis
                        continue;
                    }
                    if ((localAddTrans.get(i).getAttribute("end") != null) && (Double.parseDouble(localAddTrans.get(i).getAttributeValue("end")) < this.getMidiTime())) {   // if it is before ofThis but the end date of this transposition (if one is specified) is before oThis
                        continue;
                    }
                    if ((localAddTrans.get(i).getAttribute("layer") != null) && !localAddTrans.get(i).getAttributeValue("layer").equals(layerId)) {                     // if this transposition is dedicated to a specific layer but not the current layer (layer of ofThis)
                        continue;                                                                                                                                       // continue searching
                    }
                    trans += Integer.parseInt(localAddTrans.get(i).getAttributeValue("semi"));                                                                         // found a transposition that applies
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
        int outputOct = ((p1 - p2) / 12) - 1;               // octave (the lowest octave in midi is -1 in common western notation)
        String pitchname = "";                              //determine pitchname (may differ from pname because of transposition), here comes the result
        double outputAcc = accidentals;                     // accidentals for output (have to include accidentals that are introduced by transposition)
        if (trans == 0) pitchname = pname;                  // the trivial case
        else {                                              // because of transposition, thing become a bit more complicated, as accidentals that are introduced by the transposition have to be added to the regular accidentals
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
        pitchdata.add(Integer.toString(outputOct));

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
    public static Document xslTransformToDocument(Document input, File xslt) {
        try {
            Document stylesheet = (new Builder()).build(xslt);      //read the XSLT stylesheet
            XSLTransform transform = new XSLTransform(stylesheet);  // instantiate XSLTransform object from XSLT stylesheet
            Nodes output = transform.transform(input);              // do the transform
            return XSLTransform.toDocument(output);                 // create a Document instance from the output
        }
        catch (ParsingException ex) {
            System.err.println("Well-formedness error in " + ex.getURI() + ".");
            return null;
        }
        catch (IOException ex) {
            System.err.println("I/O error while reading input document or stylesheet.");
            return null;
        }
        catch (XMLException ex) {
            System.err.println("Result did not contain a single root.");
            return null;
        }
        catch (XSLException ex) {
            System.err.println("Stylesheet error.");
            return null;
        }
    }

    /**
     * a helper method to perform XSL transforms
     * @param input the in input xml document
     * @param xslt the XSLT stylesheet
     * @return the output string (null in case of an error)
     */
    public static String xslTransformToString(Document input, File xslt) {
        String result = "";
        Nodes output;

        try {
            Document stylesheet = (new Builder()).build(xslt);      //read the XSLT stylesheet  mei2musicxml.xsl
            XSLTransform transform = new XSLTransform(stylesheet);  // instantiate XSLTransform object from XSLT stylesheet
            output = transform.transform(input);                    // do the transform
        }
        catch (ParsingException ex) {
            System.err.println("Well-formedness error in " + ex.getURI() + ".");
            return null;
        }
        catch (IOException ex) {
            System.err.println("I/O error while reading input document or stylesheet.");
            return null;
        }
        catch (XMLException ex) {
            System.err.println("Result did not contain a single root.");
            return null;
        }
        catch (XSLException ex) {
            System.err.println("Stylesheet error.");
            return null;
        }

        // compile output string
        for (int i = 0; i < output.size(); i++) {
            result += output.get(i).toXML();
        }

        return result;
    }
}
