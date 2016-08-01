package meico.mei;

/**
 * This class is used for mei to msm conversion to hold temporary data, used in class Mei.
 * @author Axel Berndt.
 */

import java.util.ArrayList;
import java.util.List;

import meico.msm.Msm;
import nu.xom.*;

public class Helper {

    protected int ppq = 720;                            // default value for pulses per quarter
    protected boolean dontUseChannel10 = true;          // set this flag false if you allow to "misuse" the midi drum channel for other instruments; in standard midi output this produces weird results, but when you work with vst plugins etc. there is no reason to avoid channel 10
    protected Element currentMovement = null;
    protected Element currentPart = null;
    protected Element currentMeasure = null;
    protected Element currentChord = null;
    protected ArrayList<Element> accid = new ArrayList<Element>();
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
        this.currentMovement = null;
        this.currentPart = null;
        this.currentMeasure = null;
        this.currentChord = null;
        this.accid.clear();
        this.endids.clear();
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

    /** compute the midi time of an mei element
     *
     * @return
     */
    protected double getMidiTime() {
        if (this.currentPart != null)                                                       // if we are within a staff environment
            return Double.parseDouble(this.currentPart.getAttributeValue("currentDate"));   // we have a more precise date somewhere within a measure

        if (this.currentMeasure != null)                                                    // if we are within a measure
            return Double.parseDouble(this.currentMeasure.getAttributeValue("date"));       // take its take

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

    /** compute the length of one measure in midi ticks at the currentDate in the currentPart of the currentMovement; if no time signature information available it returns the length of a 4/4 measure
     *
     * @return
     */
    protected double getOneMeasureLength() {
        // get the value of one measure from the local or global timeSignatureMap
        Elements es = this.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element");
        if (es.size() == 0) {                                                                                                                                               // if local map empty
            es = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element");    // get global entries
        }

        // get length of one measure (4/4 is default if information is insufficient)
        int denom = (es.size() == 0) ? 4 : Integer.parseInt(es.get(es.size()-1).getAttributeValue("denominator"));
        int num = (es.size() == 0) ? 4 : Integer.parseInt(es.get(es.size()-1).getAttributeValue("numerator"));

        return (4.0 * this.ppq * num) / denom;

    }

    /** compute the length of one measure with specified numerator and denominator values
     *
     * @param numerator
     * @param denominator
     * @return
     */
    protected double computeMeasureLength(double numerator, int denominator) {
        return (4.0 * this.ppq * numerator) / denominator;

    }

    /** create a copy of element e including its attributes but not its child elements
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

    /**
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

    /** return part entry in movement or null
     *
     * @param id
     * @return
     */
    protected Element getPart(String id) {
        if (id.isEmpty()) return null;

        Elements parts = this.currentMovement.getChildElements("part");

        for (int i = parts.size()-1; i >= 0; --i) {                 // search all part entries in this movement
            if (parts.get(i).getAttributeValue("number").equals(id) || Helper.getAttributeValue("id", parts.get(i)).equals(id))    // for the id
                return parts.get(i);                                // return if found
        }

        return null;                                                // nothing found, return nullptr
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

    /** make the cleanup of one msm object; this removes all miscMaps, currentDate and tie attributes
     *
     * @param msm
     */
    protected static void msmCleanup(Msm msm) {
        // delete all miscMaps
        Nodes n = msm.getRootElement().query("descendant::*[local-name()='miscMap'] | descendant::*[attribute::currentDate]/attribute::currentDate | descendant::*[attribute::tie]/attribute::tie");
        for (int i=0; i < n.size(); ++i) {
            if (n.get(i) instanceof Element)
                n.get(i).getParent().removeChild(n.get(i));

            if (n.get(i) instanceof Attribute)
                ((Element) n.get(i).getParent()).removeAttribute((Attribute) n.get(i));
        }
    }

    /** return the first element in the endids list with an endid attribute value that equals id
     *
     * @param id
     * @return
     */
    private int getEndid(String id) {
        for (int i=0; i < this.endids.size(); ++i) {           // go through the list of pending elements to be ended
            if (this.endids.get(i).getAttributeValue("endid").equals(id))
                return i;
        }
        return -1;
    }

    /** check for pending elements with endid attributes to be finished when the element with this endid is found
     *
     * @param e
     */
    protected void checkEndid(Element e) {
        String id = "#" + Helper.getAttributeValue("id", e);
        for (int j = this.getEndid(id); j >= 0; j = this.getEndid(id)) {    // get id of the current element and find all pending elements in the endid list to be finished at this element
            this.endids.get(j).addAttribute(new Attribute("end", Double.toString(this.getMidiTime() + this.computeDuration(e))));     // finish corresponding element
            this.endids.remove(j);                                          // remove element from list, it is finished
        }
    }

    /**
     * This method interprets the clef.dis and clef.dis.place attribute as a transposition that is not encoded in the note elements.
     * In the mei sample set, however, this is not the case which leads to wrong octave transpositions of the respective notes.
     * Hence, I insertes a return 0 at the beginning.
     * If you want meico to feature the transponing behavior, remove the return 0 line and comment in the remaining code.
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
                && !ofThis.getLocalName().equals("space")) {        // if none of these
            return 0.0;                                             // return 0.0
        }

        double dur;                                                                         // here comes the resultant note/rest duration in midi ticks
        boolean chordEnvironment = (this.currentChord != null);                             // if we are in a chord environment set this true, else false
        Element focus = ofThis;                                                             // this will change to the chord environment, if there is one

        { // get basic duration (without dots, tuplets etc.)
            String sdur;                                                                            // the dur string

            if (ofThis.getAttribute("dur") != null) {                                               // if there is a dur attribute
                sdur = focus.getAttributeValue("dur");
            }
            else {
                if (chordEnvironment && (this.currentChord.getAttribute("dur") != null)) {          // if a chord environment defines a duration
                    focus = this.currentChord;                                                     // from now on, look only in the chord environment for all further duration related attributes
                    sdur = focus.getAttributeValue("dur");                              // take this
                }
                else {
                    Elements durdefaults = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("dur.default");
                    if (durdefaults.size() > 0) {                                                   // search for a default duration in the local miscMap
                        sdur = durdefaults.get(durdefaults.size()-1).getAttributeValue("dur");      // take this
                    }
                    else {
                        durdefaults = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("dur.default");
                        if (durdefaults.size() > 0) {                                               // search for a default duration in the global miscMap
                            sdur = durdefaults.get(durdefaults.size()-1).getAttributeValue("dur");  // take this
                        }
                        else {                                                                      // nothing found
                            return 0.0;                                                             // cancel
                        }
                    }
                }
            }

            if (sdur.equals("breve")) {
                dur = 8.0 * this.ppq;

            } else if (sdur.equals("long")) {
                dur = 16.0 * this.ppq;

            } else {
                dur = (4.0 * this.ppq) / Integer.parseInt(sdur);                                // compute midi tick duration
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
        // TODO: es gibt auch das tuplet-Attribut; wie muss ich das lesen und verarbeiten? Manchmal wird keine Tuplet-Umgebung gedruckt; dann fehlt es auch in der MEI und nur das tuplet-Attribut gibt Aufschluss.
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
        if (durString.equals("long")) {
            return 4.0;
        } else if (durString.equals("breve")) {
            return 2.0;
        } else if (durString.equals("1")) {
            return 1.0;
        } else if (durString.equals("2")) {
            return 0.5;
        } else if (durString.equals("4")) {
            return 0.25;
        } else if (durString.equals("8")) {
            return 0.125;
        } else if (durString.equals("16")) {
            return 0.0625;
        } else if (durString.equals("32")) {
            return 0.03125;
        } else if (durString.equals("64")) {
            return 0.015625;
        } else if (durString.equals("128")) {
            return 0.0078125;
        } else if (durString.equals("256")) {
            return 0.00390625;
        } else if (durString.equals("512")) {
            return 0.001953125;
        } else if (durString.equals("1024")) {
            return 0.0009765625;
        } else if (durString.equals("2048")) {
            return 0.00048828125;
        }
        return 0.0;
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
        String accid = "";                                              //
        int oct = 0;                                                    // octave transposition value
        int trans = 0;                                                  // transposition

        // get the attributes, prefer gesturals

        // get the pitch name
        if ((ofThis.getAttribute("pname.ges") != null) && !ofThis.getAttributeValue("pname.ges").equals("none")) {
            pname = ofThis.getAttributeValue("pname.ges");
        }
        else {
            if (ofThis.getAttribute("pname") != null) {
                pname = ofThis.getAttributeValue("pname");
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
                Elements octs = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("oct.default");
                if (octs.size() != 0) {                                 // look for local default octave transposition
                    oct = Integer.parseInt(octs.get(octs.size()-1).getAttributeValue("oct.default"));
                }
                else {
                    octs = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("oct.default");
                    if (octs.size() != 0) {                             // look for global default octave transposition
                        oct = Integer.parseInt(octs.get(octs.size()-1).getAttributeValue("oct.default"));
                    }
                }
                ofThis.addAttribute(new Attribute("oct", Integer.toString(oct)));   // there was no oct attribute, so fill the gap with the computed value
            }
        }

        // get accidental
        if (ofThis.getAttribute("accid.ges") != null) {                 // look for gestural accid attribute
            accid = ofThis.getAttributeValue("accid.ges");
        }
        else {
            if (ofThis.getAttribute("accid") != null) {                 // look for non-gestural accid attribute
                accid = ofThis.getAttributeValue("accid");
                if (!accid.isEmpty()) this.accid.add(ofThis);           // if not empty, insert it at the front of the accid list for reference when computing the pitch of later notes in this measure
            }
            else {
                Element accidElement = getFirstChildElement("accid", ofThis);   // is there an accid child element instead of an attribute?

                if ((accidElement != null) && (accidElement.getAttribute("accid") != null)) {                               // if there are valid accid elements in the note environment
                    ofThis.addAttribute(new Attribute("accid", accidElement.getAttributeValue("accid")));                   // make an attribute of it
                    accid = ofThis.getAttributeValue("accid");
                    if (!accid.isEmpty()) this.accid.add(ofThis);                                                           // if not empty, insert it at the front of the accid list for reference when computing the pitch of later notes in this measure
                }
                else {                                                                                                      // otherwise look for preceding accidentals in this measure
                    for (int i=0; i < this.accid.size(); ++i) {                                                             // go through the accid list
                        if ((this.accid.get(i).getAttribute("pname") != null)                                               // if it has a pitch attribute
                                && (this.accid.get(i).getAttributeValue("pname").equals(ofThis.getAttributeValue("pname"))) // the same pitch class as ofThis
                                && (this.accid.get(i).getAttribute("oct") != null)                                          // has an oct attribute
                                && (this.accid.get(i).getAttributeValue("oct").equals(ofThis.getAttributeValue("oct")))) {  // the same octave transposition as ofThis

                            accid = this.accid.get(i).getAttributeValue("accid");                                           // apply its accid attribute
                            break;                                                                                          // stop the for loop
                        }
                    }
                }
            }
        }

        // transpositions
        if ((ofThis.getAttribute("pname.ges") == null) || (ofThis.getAttribute("oct.ges") == null)) {                                                                   // if pname.ges or oct.ges are given, it already includes transpositions
            // transposition; check for global and local transposition and addTransposition elements in the miscMaps; global and local transpositions add up; so-called addTranspositions (e.g. octaves) also add to the usual transpositions
            {
                Elements globalTrans = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("transposition");
                // go through all four lists and check for elements that apply here, global and local transpositions add up
                for (int i = globalTrans.size() - 1; i >= 0; --i) {                                                                                                     // go through the global transpositions
                    if ((globalTrans.get(i).getAttributeValue("date") != null) && Double.parseDouble(globalTrans.get(i).getAttributeValue("date")) > this.getMidiTime()) {  // if ofThis is after this transposition element
                        continue;
                    }
                    if ((globalTrans.get(i).getAttribute("end") != null) && (Double.parseDouble(globalTrans.get(i).getAttributeValue("end")) < this.getMidiTime())) {   // but the end date of this transposition (if one is specified) is before oThis
                        break;
                    }
                    trans += Integer.parseInt(globalTrans.get(i).getAttributeValue("semi"));                                                                            // found a transposition that applies
                    break;                                                                                                                                              // done
                }
            }
            {
                Elements globalAddTrans = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("addTransposition");
                for (int i = globalAddTrans.size() - 1; i >= 0; --i) {                                                                                                  // go through the global addTranspositions
                    if ((globalAddTrans.get(i).getAttributeValue("date") != null) && Double.parseDouble(globalAddTrans.get(i).getAttributeValue("date")) > this.getMidiTime()) {    // if ofThis is after this transposition element
                        continue;
                    }
                    if ((globalAddTrans.get(i).getAttribute("end") != null) && (Double.parseDouble(globalAddTrans.get(i).getAttributeValue("end")) < this.getMidiTime())) {   // but the end date of this transposition (if one is specified) is before oThis
                        continue;
                    }
                    trans += Integer.parseInt(globalAddTrans.get(i).getAttributeValue("semi"));                                                                         // found a transposition that applies
                }
            }
            {
                Elements localTrans = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("transposition");
                for (int i = localTrans.size() - 1; i >= 0; --i) {                                                                                                      // go through the local transpositions
                    if ((localTrans.get(i).getAttributeValue("date") != null) && Double.parseDouble(localTrans.get(i).getAttributeValue("date")) > this.getMidiTime()) {// if ofThis is after this transposition element
                        continue;
                    }
                    if ((localTrans.get(i).getAttribute("end") != null) && (Double.parseDouble(localTrans.get(i).getAttributeValue("end")) < this.getMidiTime())) {     // but the end date of this transposition (if one is specified) is before oThis
                        break;
                    }
                    trans += Integer.parseInt(localTrans.get(i).getAttributeValue("semi"));                                                                             // found a transposition that applies
                    break;                                                                                                                                              // done
                }
            }
            {
                Elements localAddTrans = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("addTransposition");
                for (int i = localAddTrans.size() - 1; i >= 0; --i) {                                                                                                  // go through the global addTranspositions
                    if ((localAddTrans.get(i).getAttributeValue("date") != null) && Double.parseDouble(localAddTrans.get(i).getAttributeValue("date")) > this.getMidiTime()) {  // if ofThis is after this transposition element
                        continue;
                    }
                    if ((localAddTrans.get(i).getAttribute("end") != null) && (Double.parseDouble(localAddTrans.get(i).getAttributeValue("end")) < this.getMidiTime())) {   // but the end date of this transposition (if one is specified) is before oThis
                        continue;
                    }
                    trans += Integer.parseInt(localAddTrans.get(i).getAttributeValue("semi"));                                                                         // found a transposition that applies
                }
            }
        }

        double pitch = -1.0;            // here comes the result
        switch (pname.charAt(0)) {      // get value of attribute (first character of the array, it hopefully has only one character!)
            case 'c':
            case 'C':   pitch = 12; break;
            case 'd':
            case 'D':   pitch = 14; break;
            case 'e':
            case 'E':   pitch = 16; break;
            case 'f':
            case 'F':   pitch = 17; break;
            case 'g':
            case 'G':   pitch = 19; break;
            case 'a':
            case 'A':   pitch = 21; break;
            case 'b':
            case 'B':   pitch = 23; break;
            default:                    // if no valid pitch name found
                return -1;              // cancel
        }

        double initialPitch = pitch;    // need this to compute the untransposed pitchname for the pitchdata list

        // octave transposition that is directly at the note as an attribute oct
        pitch += 12 * oct;

        // accidentals
        double accidentals = 0;
        if (accid.equals("s")) {
            accidentals = 1;
        } else if (accid.equals("f")) {
            accidentals = -1;
        } else if (accid.equals("ss")) {
            accidentals = 2;
        } else if (accid.equals("x")) {
            accidentals = 2;
        } else if (accid.equals("ff")) {
            accidentals = -2;
        } else if (accid.equals("xs")) {
            accidentals = 3;
        } else if (accid.equals("ts")) {
            accidentals = 3;
        } else if (accid.equals("tf")) {
            accidentals = -3;
        } else if (accid.equals("n")) {
        } else if (accid.equals("nf")) {
            accidentals = -1;
        } else if (accid.equals("ns")) {
            accidentals = 1;
        } else if (accid.equals("su")) {
            accidentals = 1.5;
        } else if (accid.equals("sd")) {
            accidentals = 0.5;
        } else if (accid.equals("fu")) {
            accidentals = -0.5;
        } else if (accid.equals("fd")) {
            accidentals = -1.5;
        } else if (accid.equals("nu")) {
            accidentals = 0.5;
        } else if (accid.equals("nd")) {
            accidentals = -0.5;
        } else if (accid.equals("1qf")) {
            accidentals = -0.5;
        } else if (accid.equals("3qf")) {
            accidentals = -1.5;
        } else if (accid.equals("1qs")) {
            accidentals = 0.5;
        } else if (accid.equals("3qs")) {
            accidentals = 1.5;
        } else {
        }
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


    /** compute midi pitch of an mei note or return -1 if failed; this version of the method does not mix gestural and non-gestural attributes; if pname.ges is present only gestural attributes are used, else only non-gesturals are used
     *
     * @param ofThis
     * @return
     */
    @Deprecated
    protected int computePitchOld(Element ofThis) {
        String pname;                                                   // the attribute strings
        String accid = "";                                              //
        int oct;                                                        // octave transposition value
        int trans = 0;                                                  // transposition

        // accidentals preprocessing: non-gestural accidentals have to be processed even if gestural attributes are used later on
        // in this implementation accid attributes at the note element overrule accid elements within the note environment
        if ((ofThis.getAttribute("accid") != null)                      // if the note has an accid attribute
                && !ofThis.getAttributeValue("accid").isEmpty()         // that is not empty
                && (ofThis.getAttribute("pname") != null)) {            // and the note has a pitch attribute
            this.accid.add(ofThis);                                     // insert it at the front of the accid list for reference when computing the pitch of later notes in this measure
        }
        else if ((ofThis.getFirstChildElement("accid") != null)         // if there are accid elements in the note environment
                && (ofThis.getFirstChildElement("accid").getAttribute("accid") != null)             // that defines a
                && !ofThis.getFirstChildElement("accid").getAttributeValue("accid").isEmpty()       // nonempty accidental
                && (ofThis.getAttribute("pname") != null)) {                                        // and the note has a pitch attribute
            ofThis.addAttribute(new Attribute("accid", ofThis.getFirstChildElement("accid").getAttributeValue("accid")));   // make an attribute of it
            this.accid.add(ofThis);                                     // insert it at the front of the accid list for reference when computing the pitch of later notes in this measure
        }

        // get the attributes (gestural or nongestural; don't mix them!)
        if (ofThis.getAttribute("pname.ges") != null) {                 // check and read gestural attributes
            pname = ofThis.getAttributeValue("pname.ges");              // get gestural pitch name
            accid = (ofThis.getAttribute("accid.ges") != null) ? ofThis.getAttributeValue("accid.ges") : "";            // get gestural accidentals
            oct = (ofThis.getAttribute("oct.ges") != null) ? Integer.parseInt(ofThis.getAttributeValue("oct.ges")) : 0; // get gestural octave transposition
        }
        else {                                                          // non-gestural attributes
            // initial pitch
            if (ofThis.getAttribute("pname") == null) {                 // if that note has no pitch
                return -1;                                              // cancel
            }
            pname = ofThis.getAttributeValue("pname");                  // get pitch name

            // octave transposition
            if (ofThis.getAttribute("oct") != null) {                   // is there an octave transposition
                oct = Integer.parseInt(ofThis.getAttributeValue("oct"));
            }
            else {
                Elements es = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("oct.default");
                if (es.size() != 0) {                                   // is there a local default octave transposition
                    oct = Integer.parseInt(es.get(es.size()-1).getAttributeValue("oct"));
                }
                else {
                    es = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("oct.default");
                    if (es.size() != 0) {                               // is there a global default octave transposition
                        oct = Integer.parseInt(es.get(es.size()-1).getAttributeValue("oct"));
                    }
                    else {
                        oct = 0;                                        // default oct value
                    }
                    ofThis.addAttribute(new Attribute("oct", Integer.toString(oct)));   // there was no oct attribute, so fill the gap with the computed value
                }
            }

            // accidentals
            if ((ofThis.getAttribute("accid") != null)                          // if an accidental is present
                    && !ofThis.getAttributeValue("accid").isEmpty()) {          // and non-empty
                accid = ofThis.getAttributeValue("accid");                      // get it
            }
            else {                                                              // otherwise look for preceding accidentals in this measure
                for (int i=0; i < this.accid.size(); ++i) {                     // go through the accid list
                    if ((this.accid.get(i).getAttribute("pname") != null)                                           // if it has a pitch attribute
                        && (this.accid.get(i).getAttributeValue("pname").equals(ofThis.getAttributeValue("pname"))) // the same pitch class as ofThis
                        && (this.accid.get(i).getAttribute("oct") != null)                                          // has an oct attribute
                        && (this.accid.get(i).getAttributeValue("oct").equals(ofThis.getAttributeValue("oct")))) {  // the same octave transposition as ofThis

                        accid = this.accid.get(i).getAttributeValue("accid");   // apply this accid attribute
                        break;                                                  // stop the for loop
                    }
                }
            }

            // transposition
            Elements es = this.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("transposition");
            if (es.size() > 0) {                                               // look for local transposition information
                trans = Integer.parseInt(es.get(es.size()-1).getAttributeValue("semi"));
            }
            else {
                es = this.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("transposition");
                if (es.size() > 0){                                             // look for global transposition information
                    trans = Integer.parseInt(es.get(es.size()-1).getAttributeValue("semi"));
                }
            }
            // !!! this solution allows no combined transposition, e.g. a staffDef defines "Trumpet in B" and somewhere in the score an octave element appears
        }

        int pitch;                                                              // here comes the result

        // get value of attribute (first character of the array, it hopefully has only one character!)
        switch (pname.charAt(0)) {
            case 'c':
            case 'C':
                pitch = 12;
                break;
            case 'd':
            case 'D':
                pitch = 14;
                break;
            case 'e':
            case 'E':
                pitch = 16;
                break;
            case 'f':
            case 'F':
                pitch = 17;
                break;
            case 'g':
            case 'G':
                pitch = 19;
                break;
            case 'a':
            case 'A':
                pitch = 21;
                break;
            case 'b':
            case 'B':
                pitch = 23;
                break;
            default:
                return -1;
        }

        // octave transposition that are directly on the note as an attribute oct; transpositions other than by this attribute have to be added after parsing the complete mei tree because relevant tags can occur later in the tree and in other subtrees
        pitch += 12 * oct;

        // accidentals
        if (accid.equals("s")) {
            pitch++;

        } else if (accid.equals("f")) {
            pitch--;

        } else if (accid.equals("ss")) {
            pitch += 2;

        } else if (accid.equals("x")) {
            pitch += 2;

        } else if (accid.equals("ff")) {
            pitch -= 2;

        } else if (accid.equals("xs")) {
            pitch += 3;

        } else if (accid.equals("ts")) {
            pitch += 3;

        } else if (accid.equals("tf")) {
            pitch -= 3;

        } else if (accid.equals("n")) {
        } else if (accid.equals("nf")) {
            pitch--;

        } else if (accid.equals("ns")) {
            pitch++;

        } else if (accid.equals("su")) {
            pitch++;    // TODO: actually +1.5, here it's just a semi; later maybe done by pitchbend

        } else if (accid.equals("sd")) {
            pitch++;    // TODO: actually +0,5, here it's just a semi; later maybe done by pitchbend

        } else if (accid.equals("fu")) {
            pitch--;    // TODO: actually -0,5, here it's just a semi; later maybe done by pitchbend

        } else if (accid.equals("fd")) {
            pitch--;    // TODO: actually -1,5, here it's just a semi; later maybe done by pitchbend

        } else if (accid.equals("nu")) {
            pitch++;    // TODO: actually +0,5, here it's just a semi; later maybe done by pitchbend

        } else if (accid.equals("nd")) {
            pitch--;    // TODO: actually -0,5, here it's just a semi; later maybe done by pitchbend

        } else {
        }

        // transposition
        pitch += trans;

        return pitch;
    }

}
