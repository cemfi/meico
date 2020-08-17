package meico.msm;

import meico.mpm.elements.Performance;
import meico.pitches.FeatureVector;
import meico.pitches.Key;
import meico.pitches.Pitches;
import meico.mei.Helper;
import meico.midi.*;
import meico.supplementary.KeyValue;
import nu.xom.*;
import org.xml.sax.SAXException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * This class holds data in msm format (Musical Sequence Markup).
 * @author Axel Berndt.
 */

public class Msm extends AbstractMsm {
    private static final int CONTROL_CHANGE_DENSITY = 10;       // in MPM-to-MIDI export a series of control change events may be generated (e.g. due to sub-note dynamics); this constant limits their density, i.e. how much of them are generated for a timeframe; the value says that at max one is generated every CONTROL_CHANGE_DENSITY milliseconds

    /**
     * constructor
     */
    public Msm() {
        super();
    }

    /**
     * constructor
     *
     * @param msm the msm document of which to instantiate the Msm object
     */
    public Msm(Document msm) {
        super(msm);
    }

    /**
     * constructor
     *
     * @param file the msm file to be read
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(File file) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file);
    }

    /**
     * constructor
     * @param file
     * @param validate
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(File file, boolean validate, URL schema) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file, validate, schema);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(String xml) throws IOException, ParsingException {
        super(xml);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @param validate validate the code?
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(String xml, boolean validate, URL schema) throws IOException, ParsingException {
        super(xml, validate, schema);
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(InputStream inputStream) throws IOException, ParsingException {
        super(inputStream);
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @param validate
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(InputStream inputStream, boolean validate, URL schema) throws IOException, ParsingException {
        super(inputStream, validate, schema);
    }

    /**
     * this factory creates an initial Msm instance with empty global maps
     * @param title
     * @param id an id string for the root element or null, in the latter case a random UUID will be created
     * @param ppq
     * @return
     */
    public static Msm createMsm(String title, String id, int ppq) {
        Element root = new Element("msm");                                          // create the root element of the msm/xml tree
        root.addAttribute(new Attribute("title", title));                           // add a title attribute to it

        Attribute idAttribute = new Attribute("id", (id == null) ? UUID.randomUUID().toString() : id);  // make new id attribute
        idAttribute.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");    // set correct namespace
        root.addAttribute(idAttribute);                                             // and it to the MSM movement element

        // create global containers
        Element global = new Element("global");
        Element dated = new Element("dated");
        Element header = new Element("header");

        root.addAttribute(new Attribute("pulsesPerQuarter", Integer.toString(ppq)));// add the attribute to the root

        dated.appendChild(new Element("timeSignatureMap"));                         // global time signatures
        dated.appendChild(new Element("keySignatureMap"));                          // global key signatures
        dated.appendChild(new Element("markerMap"));                                // global rehearsal marks
        dated.appendChild(new Element("sectionMap"));                               // global map of section structure
        dated.appendChild(new Element("phraseMap"));                                // global map of phrase structure
        dated.appendChild(new Element("sequencingMap"));                            // global sequencingMap
        dated.appendChild(new Element("pedalMap"));                                 // global map for pedal instructions
        dated.appendChild(new Element("miscMap"));                                  // a temporal map that is filled with content that may be useful during processing but will be deleted in the final MSM

        global.appendChild(header);
        global.appendChild(dated);
        root.appendChild(global);

        return new Msm(new Document(root));
    }

    /**
     * create a copy of this object
     * @return the copy of this Msm object
     */
    @Override
    public Msm clone() {
        Msm clone = new Msm(this.getDocument().copy());
        clone.isValid = this.isValid();
        clone.setFile(this.getFile());
        return clone;
    }

    /**
     * This getter method returns the title string from the root element's attribute title. If missing, use the filename without extension or return "".
     * @return
     */
    public String getTitle() {
        Attribute title;

        try {                                               // try to read the title attribute
            title = Helper.getAttribute("title", this.getRootElement());
        } catch (NullPointerException ex) {                 // if that does not exist
            return (this.getFile() != null) ? Helper.getFilenameWithoutExtension(this.getFile().getName()) : "";    // return the filename without extension or (if that does not exist either) return empty string
        }

        return title.getValue();                            // return the title string
    }

    /**
     * this getter returns the timing resolution (pulses per quarternote) of the MSM
     * @return
     */
    public int getPPQ() {
        Attribute ppq;

        try {
            ppq = Helper.getAttribute("pulsesPerQuarter", this.getRootElement());
        }
        catch (NullPointerException ex) {
            return 0;
        }

        return Integer.parseInt(ppq.getValue());
    }

    /**
     * this getter returns the timing resolution (pulses per quarternote) of the MSM
     * @return
     */
    public int getPulsesPerQuarter() {
        return this.getPPQ();
    }

    /**
     * Set the pulses per quarter timing resolution attribute.
     * Be careful with this, it does not change any midi date values!
     * It is safer to invoke convertPPQ().
     * @param ppq
     */
    public void setPulsesPerQuarter(int ppq) {
        this.getRootElement().getAttribute("pulsesPerQuarter").setValue(Integer.toString(ppq));
    }

    /**
     * Set the pulses per quarter timing resolution attribute.
     * Be careful with this, it does not change any midi date values!
     * It is safer to invoke convertPPQ().
     * @param ppq
     */
    public void setPPQ(int ppq) {
        this.setPulsesPerQuarter(ppq);
    }

    /**
     * this method converts the timing basis, i.e., it sets the new ppq value and converts all attributes date, date.end and duration in the whole document
     * @param ppq
     */
    public void convertPPQ(int ppq) {
        int ppqOld = this.getPPQ();
        if (ppqOld == ppq)
            return;

        System.out.println("Converting timing basis of \"" + this.getTitle() + "\" from " + this.getPulsesPerQuarter() + " to " + ppq + " pulses per quarter note.");

        this.setPPQ(ppq);

        // find all attributes date, date.end and duration, and convert their values
        Nodes atts = this.getRootElement().query("descendant::*[attribute::date]/attribute::date | descendant::*[attribute::date.end]/attribute::date.end | descendant::*[attribute::duration]/attribute::duration");
        for (int i = 0; i < atts.size(); ++i) {
            Attribute att = (Attribute) atts.get(i);
            att.setValue(Double.toString(((Double.parseDouble(att.getValue()) * ppq) / ppqOld)));
        }
    }

    /**
     * this method converts the timing basis, i.e., it sets the new ppq value and converts all attributes date, date.end and duration in the whole document
     * @param ppq
     */
    public void convertPulsesPerQuarter(int ppq) {
        this.convertPPQ(ppq);
    }

    /**
     * computes the minimal integer timing resolution necessary for a rhythmically reasonably accurate representation of the score data in this MSM
     * @return
     */
    public int getMinimalPPQ() {
        int ppq = this.getPPQ();
        int minDur = ppq;                                                                                                       // this will get the shortest note duration
        int minDateDif = ppq;                                                                                                   // this will hold the smallest number of ticks between notes on the time grid

        Elements parts = this.getParts();
        for (int p=0; p < parts.size(); ++p) {                                                                                  // go through all parts
            Elements notes = parts.get(p).getFirstChildElement("dated").getFirstChildElement("score").getChildElements("note"); // get all notes in its score

            for (int n=0; n < notes.size(); ++n) {                                                                              // go through all notes
                Element note = notes.get(n);
                int dur = (int) Math.round(Double.parseDouble(note.getAttributeValue("duration")));                        // get the note's duration (rounding is necessary for avoiding numeric problems with tripltes)
                dur %= ppq;                                                                                                     // this operation returns 0.0 if the duration is an integer multiple of ppq and it returns something > 0.0 in case of a dotted or smaller note value
                if ((dur != 0.0) && (dur < minDur))                                                                             // in case of a shorter or dotted note that is even shorter than the shortest we had so far
                    minDur = dur;                                                                                               // store it

                int date = (int) Math.round(Double.parseDouble(note.getAttributeValue("date")));                           // get the note's date (rounding is necessary for avoiding numeric problems with tripltes)
                date %= ppq;                                                                                                    // this operation returns 0.0 if the note is on the quarternote grid and it returns something > 0.0 when it is in-between
                if ((date != 0.0) && (date < minDateDif))                                                                       // if we found evidence for a finer grid than the quarternote grid of ppq
                    minDateDif = date;                                                                                          // store the value
            }
        }

        int minPPQDur = ppq / minDur;                               // compute the smallest number of pulses per quarter from the durations
        int minPPQDate = ppq / minDateDif;                          // compute the smallest number of pulses per quarter from the dates

        return Math.max(minPPQDate, minPPQDur);                     // return the larger number of the above computations
    }

    /**
     * Generate a "raw" part element with its corresponding attributes and empty "header" and "dated" environments.
     * This element is not added to the document! It is up to the application to do this.
     * @param name
     * @param number
     * @param midiChannel
     * @param midiPort
     * @return the part element just generated
     */
    public static Element makePart(String name, String number, int midiChannel, int midiPort) {
        Element part = AbstractMsm.makePart(name, number, midiChannel, midiPort);

        // add some MSM-specific maps to the dated environment
        Element dated = part.getFirstChildElement("dated");
        dated.appendChild(new Element("timeSignatureMap"));
        dated.appendChild(new Element("keySignatureMap"));
        dated.appendChild(new Element("markerMap"));
        dated.appendChild(new Element("sequencingMap"));
        dated.appendChild(new Element("pedalMap"));
        dated.appendChild(new Element("phraseMap"));
        Element miscMap = new Element("miscMap");
        dated.appendChild(miscMap);
        miscMap.appendChild(new Element("tupletSpanMap"));
        dated.appendChild(new Element("score"));

        return part;
    }

    /**
     * Generate a "raw" part element with its corresponding attributes and empty "header" and "dated" environments.
     * This element is not added to the document! It is up to the application to do this.
     * @param name
     * @param number
     * @param midiChannel
     * @param midiPort
     * @return the part element just generated
     */
    public static Element makePart(String name, int number, int midiChannel, int midiPort) {
        return Msm.makePart(name, String.valueOf(number), midiChannel, midiPort);
    }

    /**
     * add the specified part to the xml structure
     * @param part
     */
    public void addPart(Element part) {
        this.getRootElement().appendChild(part);
    }

    /**
     * a getter that returns all part elements in the XML tree
     * @return
     */
    public Elements getParts() {
        return this.getRootElement().getChildElements("part");
    }

    /**
     * a getter for the global environment
     * @return
     */
    public Element getGlobal() {
        return this.getRootElement().getFirstChildElement("global");
    }

    /**
     * a convenience method to generate timeSignature elements
     * @param date
     * @param numerator
     * @param denominator
     * @param id
     * @return
     */
    public static Element makeTimeSignature(double date, double numerator, int denominator, String id) {
        Element e = new Element("timeSignature");

        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("numerator", Double.toString(numerator)));
        e.addAttribute(new Attribute("denominator", Integer.toString(denominator)));

        if (id != null)
            e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id));

        return e;
    }

    /**
     * removes all rest elements from the score lists;
     * this method is not part of the mei.exportMsm() cleanup procedure as some applications may still need the rests;
     * others who don't, can call this method to remove all rest elements and get a purged msm
     */
    public synchronized void removeRests() {
        if (this.isEmpty()) return;

        Nodes r = this.getRootElement().query("descendant::*[local-name()='rest']");    // select all rest elements
        for (int i = 0; i < r.size(); ++i)
            r.get(i).getParent().removeChild(r.get(i));                                 // remove them
    }

    /**
     * this method expands all global and local maps according to the sequencingMaps;
     * if a local sequencingMap (can be empty) is given in a certain part, that part ignores the global sequencingMap
     * @return a hashmap with xml:id mappings for those elements that have been copied and needed an updated id, the key-value pair is (id of the original, id of the clone), and later (id of the previous clone, id of another clone)
     */
    public synchronized HashMap<String, String> resolveSequencingMaps() {
        HashMap<String, String> repetitionIDs = new HashMap<>();
        if (this.isEmpty()) return repetitionIDs;

        Element globalSequencingMap = this.getRootElement().getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("sequencingMap"); // get the global sequencingMap (or null if there is none)
        Elements parts = this.getRootElement().getChildElements("part");                                // get all the parts
        Element part, sequencingMap;                                                                    // these elements are used in the for loop that comes next

        // expand global maps
        if (globalSequencingMap != null) {
            Elements maps = this.getRootElement().getFirstChildElement("global").getFirstChildElement("dated").getChildElements();
            for (int j = 0; j < maps.size(); ++j) {                                                     // go through all maps
                Element map = maps.get(j);                                                              // one map
                if ((map.getChildCount() == 0)                                                          // do not expand sequencingMaps
                        || map.getLocalName().equals("miscMap")                                         // ignore miscMaps as they will be deleted anyway
                        || map.getLocalName().equals("sequencingMap"))                                  // or if the map is empty
                    continue;                                                                           // continue with the next

                Element newMap = Msm.applySequencingMapToMap(globalSequencingMap, map, repetitionIDs);  // apply the global sequencingMap to it
                if (newMap != null)
                    this.getRootElement().getFirstChildElement("global").getFirstChildElement("dated").replaceChild(map, newMap);   // replace the old map by the new one
            }
        }

        // go through all parts and expand their maps according to the underlying sequencingMaps
        for (int i = 0; i < parts.size(); ++i) {                                                        // for each part
            part = parts.get(i);                                                                        // get it as element
            sequencingMap = part.getFirstChildElement("dated").getFirstChildElement("sequencingMap");   // get the part's local sequencingMap if there is one
            boolean localMap = true;
            if (sequencingMap == null) {                                                                // if there is none
                localMap = false;
                sequencingMap = globalSequencingMap;                                                    // gegt the global sequencingMap
                if ((sequencingMap == null) || (sequencingMap.getChildCount() == 0))                    // if there is none or it is empty
                    continue;                                                                           // continue with the next part
            }

            // go through the score and all maps (except the sequencingMap itself) and apply the sequencingMap to them
//            Nodes maps = part.query("descendant::*[local-name()='score' or (contains(local-name(), 'Map') and not((local-name()='sequencingMap') or (local-name()='miscMap')))]");    // get the score and all maps
            Elements maps = part.getFirstChildElement("dated").getChildElements();
            for (int j = 0; j < maps.size(); ++j) {                                                     // go through all maps
                Element map = maps.get(j);                                                              // one map
                if ((map.getChildCount() == 0)                                                          // do not expand sequencingMaps
                        || map.getLocalName().equals("miscMap")                                         // ignore miscMaps as they will be deleted anyway
                        || map.getLocalName().equals("sequencingMap"))                                  // or if the map is empty
                    continue;                                                                           // continue with the next

                Element newMap = Msm.applySequencingMapToMap(sequencingMap, map, repetitionIDs);        // apply the sequencingMap to it
                if (newMap != null) map.getParent().replaceChild(map, newMap);                          // replace the old map by the new one
            }

            // delete the local sequencingMap (because it does not apply anymore)
            if (localMap)
                part.getFirstChildElement("dated").removeChild(sequencingMap);
        }

        // delete the global sequencingMap (because it does not apply anymore)
        if (globalSequencingMap != null)
            this.getRootElement().getFirstChildElement("global").getFirstChildElement("dated").removeChild(globalSequencingMap);

        return repetitionIDs;
    }

    /**
     * apply the sequencingMap to the map; this expands the map
     * @param sequencingMap
     * @param map
     * @param repetitionIDs this hashmap will be filled with mappings of xml:id's that are extended to avoid double occurrences
     * @return the expanded map (to replace the old map) or null (to keep the old map)
     */
    public static Element applySequencingMapToMap(Element sequencingMap, Element map, HashMap<String, String> repetitionIDs) {
        Elements gs = sequencingMap.getChildElements("goto");               // get the gotos
        if (gs.size() == 0) return null;                                    // if there are no gotos in the sequencingMap, i.e. nothing to expand, return null

        // make an ArrayList of Goto instances
        ArrayList<Goto> gotos = new ArrayList<>();                          // this is the list
        for (int i=0; i < gs.size(); ++i) {                                 // fill the goto list, got through all gotos
            try {
                gotos.add(new Goto(gs.get(i)));                             // from the goto element create a Goto instance
            } catch (Exception e) {                                         // if this fails
                e.printStackTrace();                                        // print the exception and continue with the next
            }
        }

        // create a new map and fill it by traversing the original map as indicated by the goto elements
        Element newMap = Helper.cloneElement(map);                          // make a flat copy of the map (no children so far) to refill it according to the sequencingMap

        double currentDate = 0.0;                                           // start at date 0.0
        double dateOffset = 0.0;                                            // this sums up the offsets that come form from inserting repetitions
        for (int i=0; i < gotos.size(); ++i) {                              // find the next goto
            Goto gt = gotos.get(i);                                         // get the next goto
            if ((gt.date < currentDate) || !gt.isActive()) continue;        // if the goto is before currentDate or it is not active continue with the next

            // copy everything between currentDate and gt.date from the original map into newMap
            for (Element e = Msm.getElementAtAfter(currentDate, map); e != null; e = Helper.getNextSiblingElement(e)) { // go through the map elements, starting the first element at or after the goto's target.date, and then go on with the next sibling
                currentDate = Double.parseDouble(e.getAttributeValue("date"));                                          // read its date
                if (currentDate >= gt.date) break;                                                                      // if the element's date is at or after the goto (keep in mind, the goto is active) don't copy further, break the loop
                Element eCopy = e.copy();                                                                               // make a deep copy of the element
                eCopy.getAttribute("date").setValue(Double.toString(currentDate + dateOffset));                         // draw its date

                Attribute endDate = e.getAttribute("date.end");                                                         // get the date.end attribute
                if (endDate != null) {                                                                                  // if the element has one, update it, too
                    double dur = Double.parseDouble(endDate.getValue()) - Double.parseDouble(e.getAttributeValue("date"));
                    eCopy.getAttribute("date.end").setValue(Double.toString(currentDate + dur + dateOffset));
                }

                Attribute repetitionCounter = e.getAttribute("repetitionCounter");                                      // get the counter of how often we have already repeated this element
                if (repetitionCounter != null) {                                                                        // this is not the first time we process this element
                    int reps = 1 + Integer.parseInt(e.getAttributeValue("repetitionCounter"));                          // increase repetition counter
                    e.getAttribute("repetitionCounter").setValue(Integer.toString(reps));                               // write it to the attribute
                    Attribute id = eCopy.getAttribute("id", "http://www.w3.org/XML/1998/namespace");                    // get the id of eCopy or null if it has none
                    if (id != null) {                                                                                   // if it has an xml:id, it would appear twice now; this is not valid, so we have to make a new id
                        String prevId = id.getValue();                                                                  // get the base ID
                        String newId = "meico_repetition_" + reps + "_" + prevId;                                       // generate a new ID including the base ID
                        id.setValue(newId);                                                                             // set the attribute

                        // the key of the hashmap entry should be the ID of the previous iteration, not the base ID
                        for (int r = reps-1; r > 0; --r)                                                                // hence, we iterate through the hashmap as often as there were previous iterations
                            prevId = repetitionIDs.get(prevId);                                                         // and keep the last of them to be used as key
                        repetitionIDs.put(prevId, newId);                                                               // add the old-to-new-ID mapping to the hashmap, the old ID is the one from the previous iteration
                    }
                }
                else {                                                                                                  // this is the first time we process this element
                    e.addAttribute(new Attribute("repetitionCounter", "0"));                                            // add an attribute to count the repetitions
                }
                newMap.appendChild(eCopy);                                  // append the copy to the new map
            }

            dateOffset += gt.date - gt.targetDate;                          // draw the dateOffset
            currentDate = gt.targetDate;                                    // draw currentDate
            i = -1;                                                         // start searching for the next goto
        }

        // last goto has been processed, now do the rest until the end marker
        for (Element e = Msm.getElementAtAfter(currentDate, map); e != null; e = Helper.getNextSiblingElement(e)) { // go through the map elements, starting the first element at or after the goto's target.date, and then go on with the next sibling
            currentDate = Double.parseDouble(e.getAttributeValue("date"));                                          // read its date
            Element eCopy = e.copy();                                                                               // make a deep copy of the element
            eCopy.getAttribute("date").setValue(Double.toString(currentDate + dateOffset));                         // draw its date

            Attribute endDate = e.getAttribute("date.end");                                                         // get the date.end attribute
            if (endDate != null) {                                                                                  // if the element has one, update it, too
                double dur = Double.parseDouble(endDate.getValue()) - Double.parseDouble(e.getAttributeValue("date"));
                eCopy.getAttribute("date.end").setValue(Double.toString(currentDate + dur + dateOffset));
            }

            Attribute repetitionCounter = e.getAttribute("repetitionCounter");                                      // get the counter of how often we have already repeated this element
            if (repetitionCounter != null) {                                                                        // this is not the first time we process this element
                int reps = 1 + Integer.parseInt(e.getAttributeValue("repetitionCounter"));                          // increase repetition counter
                e.getAttribute("repetitionCounter").setValue(Integer.toString(reps));                               // write it to the attribute
                Attribute id = eCopy.getAttribute("id", "http://www.w3.org/XML/1998/namespace");                    // get the id of eCopy or null if it has none
                if (id != null) {                                                                                   // if it has an xml:id, it would appear twice now; this is not valid, so we have to make a new id
                    String prevId = id.getValue();                                                                  // get the base ID
                    String newId = "meico_repetition_" + reps + "_" + prevId;                                       // generate a new ID including the base ID
                    id.setValue(newId);                                                                             // set the attribute

                    // the key of the hashmap entry should be the ID of the previous iteration, not the base ID
                    for (int r = reps-1; r > 0; --r)                                                                // hence, we iterate through the hashmap as often as there were previous iterations
                        prevId = repetitionIDs.get(prevId);                                                         // and keep the last of them to be used as key
                    repetitionIDs.put(prevId, newId);                                                               // add the old-to-new-ID mapping to the hashmap, the old ID is the one from the previous iteration
                }
            }

            newMap.appendChild(eCopy);                                      // append the copy to the new map
        }

        // cleanup: delete all repetitionCounter attributes from all map and newMap elements
        Nodes rs = map.query("descendant::*[@repetitionCounter]");
        for (int i = rs.size()-1; i >= 0; --i) {
            Element r = (Element) rs.get(i);
            r.removeAttribute(r.getAttribute("repetitionCounter"));
        }
        rs = newMap.query("descendant::*[@repetitionCounter]");
        for (int i = rs.size()-1; i >= 0; --i) {
            Element r = (Element) rs.get(i);
            r.removeAttribute(r.getAttribute("repetitionCounter"));
        }

        return newMap;
    }

    /**
     * writes the msm document to an msm file at this.file (it must be != null);
     * if there is already an msm file with this name, it is replaces!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeMsm() {
        return this.writeFile();
    }

    /**
     * writes the msm document to a file (filename should include the path and the extension .msm)
     *
     * @param filename the filename string; it should include the path and the extension .msm
     * @return true if success, false if an error occured
     */
    public synchronized boolean writeMsm(String filename) {
        return this.writeFile(filename);
    }

    /**
     * converts the msm data into a midi sequence and create a meico.Midi object from it; the tempo is 120bpm by default
     * @return the midi object created or null if this msm object is empty or something else wnet wrong
     */
    public Midi exportMidi() {
        return this.renderMidi(120, true, false);
    }

    /**
     * converts the msm data into a midi sequence and create a meico.Midi object from it; the tempo is 120bpm by default
     * @param generateProgramChanges if true, program change events are generated (useful for MIR and as a cheap kind of piano reduction); but be careful: if your channel is set on trumpet it would not be set on piano automatically, you have to take care!
     * @return
     */
    public Midi exportMidi(boolean generateProgramChanges) { return this.renderMidi(120, generateProgramChanges, false);}

    /**
     * converts the msm data into a midi sequence and create a midi object from it
     * @param bpm the tempo of the midi track
     * @return
     */
    public Midi exportMidi(double bpm) { return this.renderMidi(bpm, true, false);}

    /**
     * converts the msm data into a midi sequence and create a midi object from it
     * @param bpm the tempo of the midi track
     * @param generateProgramChanges if true, program change events are generated (useful for MIR and as a cheap kind of piano reduction); but be careful: if your channel is set on trumpet it would not be set on piano automatically, you have to take care!
     * @return the midi object created or null if this msm object is empty or something else went wrong
     */
    public Midi exportMidi(double bpm, boolean generateProgramChanges) {
        return this.renderMidi(bpm, generateProgramChanges, false);
    }

    /**
     * This method should only be invoked when performance rendering has already been applied to this MSM so it has the additional expression attributes (milliseconds.date, velocity etc.).
     * It renders the MIDI on the basis of these attributes. If they are missing, the non-performance specific counterpart attributes are used. But be warned, the result may be surprising due to potential data inconsistency!
     * @return
     */
    public Midi exportExpressiveMidi() {
        return this.exportExpressiveMidi(null);
    }

    /**
     * this method applies the specified performance to the msm data and exports expressive midi
     * @param performance
     * @return
     */
    public Midi exportExpressiveMidi(Performance performance) {
        if (performance == null)
            return this.renderMidi(83.33, true, true);

        Msm expressiveMsm = performance.perform(this);
        return expressiveMsm.renderMidi(83.33, true, true);
    }

    /**
     * this method applies the specified performance to the msm data and exports expressive midi
     * @param performance
     * @param generateProgramChanges
     * @return
     */
    public Midi exportExpressiveMidi(Performance performance, boolean generateProgramChanges) {
        if (performance == null)
            return this.renderMidi(83.33, true, true);

        Msm expressiveMsm = performance.perform(this);
        return expressiveMsm.renderMidi(83.33, generateProgramChanges, true);
    }

    /**
     * converts the msm data into a midi sequence and create a midi object from it
     * @param bpm the tempo of the midi track
     * @param generateProgramChanges if true, program change events are generated (useful for MIR and as a cheap kind of piano reduction); but be careful: if your channel is set on trumpet it would not be set on piano automatically, you have to take care!
     * @param exportExpressiveMidi set true to make performance rendering and export expressive MIDI
     * @return the midi object created or null if this msm object is empty or something else went wrong
     */
    private Midi renderMidi(double bpm, boolean generateProgramChanges, boolean exportExpressiveMidi) {
        long startTime = System.currentTimeMillis();                        // we measure the time that the conversion consumes
        System.out.println("\nConverting " + ((this.file != null) ? this.file.getName() : "MSM data") + " to MIDI.");

        if (this.isEmpty())                                                 // if there is no data
            return null;                                                    // return null

        // create an empty midi sequence
        int ppq = this.getPPQ();
        Sequence seq;
        try {
            seq = new Sequence(Sequence.PPQ, ppq);                          // create the midi sequence
        } catch (InvalidMidiDataException e) {                              // if failed for some reason
            e.printStackTrace();                                            // print error message
            return null;                                                    // return null
        }

        // parse the msm, create MidiEvent objects (MidiMessage object with a tick value), add them to a Sequence object (each Track represents a part)
        Track track = seq.createTrack();            // create the first midi track; it is used for global meta data (tempo, time signature, key signature, marker)

        if (exportExpressiveMidi) {                 // if we want to output expressive midi, we render the midi events on the basis of milliseconds dates
            this.makeMillisecondTickTempo(track);   // set the midi clock tempo so that one tick is equal to one millisecond
            this.fitVelocities(0.0, 127.0);       // check MIDI compliance of the velocity and channelVolume values, scale them down if necessary
        }
        else {                                      // if we output raw midi, the dates are based on symbolic timing
            this.makeInitialTempo(bpm, track);      // this method does not create an exhaustive tempo map; this is left to the performance rendering after extracting a music performance markup structure from mei; however, to specify at least a basic tempo for the midi sequence created here, we generate one tempo event at the beginning with the specified bpm
        }

        this.parseMarkerMap(this.getRootElement().getFirstChildElement("global"), track, exportExpressiveMidi);         // parse markerMap
        this.parseTimeSignatureMap(this.getRootElement().getFirstChildElement("global"), track, exportExpressiveMidi);  // parse timeSignatureMap
        this.parseKeySignatureMap(this.getRootElement().getFirstChildElement("global"), track, exportExpressiveMidi);   // parse keySignatureMap
//        this.parsePedalMap(this.getRootElement().getFirstChildElement("global"), track, exportExpressiveMidi);          // parse pedalMap

        // parse the parts, each part becomes a midi track
        for (Element part = this.getRootElement().getFirstChildElement("part"); part != null; part = Helper.getNextSiblingElement("part", part)) {  // go through all parts in the msm document
            if (part.getAttribute("midi.channel") == null) continue;                                            // no channel information, cancel this part element's processing and continue with the next part

            // create and prepare the midi channel from the part
            track = seq.createTrack();                                                                          // create a new midi track for this part and write all further data into it

            short port = 0;
            if (part.getAttribute("midi.port") != null)                                                         // if midi port is specified in MSM (should be)
                port = Short.parseShort(part.getAttributeValue("midi.port"));                                   // get the port number
            MidiEvent portEvent = EventMaker.createMidiPortEvent(0, port);                                      // create midi event
            track.add(portEvent);                                                                               // add it to the track

            short chan = Short.parseShort(part.getAttributeValue("midi.channel"));                              // get the MIDI channel number
            MidiEvent channelPrefix = EventMaker.createChannelPrefix(0, chan);                                  // create a channel prefix event that says all subsequent meta messages go to this channel
            track.add(channelPrefix);                                                                           // add the event to the track

            // parse the score, keySignatureMap, timeSignatureMap, markerMap to midi
            boolean reallyGenerateProgramChanges = generateProgramChanges;
            if (reallyGenerateProgramChanges) {
                reallyGenerateProgramChanges = !this.parseProgramChangeMap(part, track, chan, exportExpressiveMidi);
            }
            this.processPartName(part, track, chan, reallyGenerateProgramChanges);                              // scan the part attribute name for a known string to create a gm program change and instrument name event ... but only if there is no programChangeMap providing an initial program change number


            // if there are local meta events to be generated
            this.parseKeySignatureMap(part, track, exportExpressiveMidi);                                       // parse keySignatureMap
            this.parseTimeSignatureMap(part, track, exportExpressiveMidi);                                      // parse timeSignatureMap
            this.parseMarkerMap(part, track, exportExpressiveMidi);                                             // parse markerMap

            this.parseChannelVolumeMap(part, track, exportExpressiveMidi);                                      // parse the channelVolumeTrack (only in expressive MIDI mode)

//            this.parsePedalMap(part, track, exportExpressiveMidi);                                            // parse pedalMap

            this.processScore(part, track, exportExpressiveMidi);                                               // parse score
        }

        // TODO: AllNotesOff at the end

        // create the meico.Midi object
        if (this.getFile() != null) {
            File midiFile = new File(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".mid");    // set the filename extension of the Midi object to "mid"
            System.out.println("MSM to MIDI conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");
            return new Midi(seq, midiFile);                                                                     // create and return the Midi object
        }

        System.out.println("MSM to MIDI conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return new Midi(seq);                                                                                   // the MSM has no file information create the Midi instance only from the sequence and with file=null
    }

    /**
     * This method checks whether the velocity values hold the specified limits. If not, they are scaled down.
     * @param min
     * @param max
     */
    private void fitVelocities(double min, double max) {
        // if min is greater than max, switch the values
        if (min > max) {
            double x = min;
            min = max;
            max = x;
        }

        // find all velocity attributes and get their values
        ArrayList<KeyValue<Double, Attribute>> velocities = new ArrayList<>();      // a list of tuplets with the attributes and their value
        double lowest = Double.MAX_VALUE;                                           // this will get the lowest velocity value
        double highest = Double.MIN_VALUE;                                          // this will get the highest velocity value
        Elements parts = this.getParts();
        for (Element part : parts) {                                                // in each part
            Element dated = Helper.getFirstChildElement("dated", part);             // get the part's dated environment
            if (dated == null)
                continue;
            Element score = Helper.getFirstChildElement("score", dated);            // get the score element
            if (score == null)
                continue;
            LinkedList<Element> notes = Helper.getAllChildElements("note", score);  // get all note elements in the score
            for (Element note : notes) {                                            // for each note
                Attribute velAtt = Helper.getAttribute("velocity", note);           // get its velocity attribute
                if (velAtt == null)
                    continue;
                double value = Double.parseDouble(velAtt.getValue());               // read the attribute's value into a double
                if (value < lowest)                                                 // if this is lower than the lowest so far
                    lowest = value;                                                 // keep the value
                else if (value > highest)                                           // if the value is greater than the highest so far
                    highest = value;                                                // keep the value
                velocities.add(new KeyValue<>(value, velAtt));                      // create a tuplet and add it to the ArrayList
            }
        }

        boolean scaleLowerHalf = (lowest < min);
        boolean scaleUpperHalf = (highest > max);
        if (!(scaleLowerHalf || scaleUpperHalf))                                    // if the velocity values hold the limits
            return;                                                                 // we are done

        // otherwise we need to apply compression
        System.out.println("Warning: velocity values [" + lowest + ", " + highest + "] break the specified limits [" + min + ", " + max + "] and will be compressed.");
        Msm.computePartwiseCompression(velocities, lowest, highest, min, max);
    }

    /**
     * This method computes a compression of an unlimited input domain (x, the value to be mapped is element of that domain) to a limited output domain (limited by min and max).
     * It uses a semicircle to define a projection into the interval [min, max].
     * The problem with this method: The projection is always the same and will practically never include the min and max values. The min-max range is practically never fully used.
     * @param x
     * @param min
     * @param max
     * @return
     */
    private static double computeSemicircleCompression(double x, double min, double max) {
        /* ___________________ input domain
         *      /       \
         *     |         |
         *     +---------+ output domain
         */
        double radius = (max - min) / 2.0;
        double xNorm = x - min - radius;
        double xResultNorm = (radius * xNorm) / Math.sqrt((xNorm * xNorm) + (radius * radius));
        return xResultNorm + min + radius;
    }

    /**
     * This method computes a compression of a limited input domain (lowest &le; x &le; highest) to a limited output domain (limited by min and max).
     * It uses a partwise linear mapping. It tries to limit the range of compression depending on how much the limits are broken by lowest and highest value.
     * @param attributes the values to be mapped according to the compession
     * @param lowest
     * @param highest
     * @param min
     * @param max
     */
    private static void computePartwiseCompression(ArrayList<KeyValue<Double, Attribute>> attributes, double lowest, double highest, double min, double max) {
        // on the basis of the lowest and highest value (the extremes of the input domain), compute the range to be compresed, i.e. [lowest, lowerCompMax] and [upperCompMin, highest]
        double lowerCompMax = min;
        double upperCompMin = max;
        if (lowest < min)
            lowerCompMax = max - (((max - min) * (max - min)) / (max - lowest));
        if (highest > max)
            upperCompMin = min + (((max - min) * (max - min)) / (highest - min));
        if (lowerCompMax > upperCompMin) {
            lowerCompMax = (lowerCompMax + upperCompMin) / 2.0;
            upperCompMin = lowerCompMax;
        }

        // the rolloffFactor (0.0 < rolloffFactor < 1.0) lowers the degree of compression for values within the range [min, max] the higher it is set; values beyond the limits will be more compressed
        double rolloffFactor = 0.66;
        double upperRolloff1 = 0.0, upperRolloff2 = 0.0, lowerRolloff1 = 0.0, lowerRolloff2 = 0.0;
        double upperRaise = upperCompMin;
        double lowerRaise = min;
        if (highest > max) {
            upperRolloff1 = ((max - upperCompMin) * rolloffFactor) / (max - upperCompMin);
            upperRolloff2 = ((1.0 - rolloffFactor) * (max - upperCompMin)) / (highest - max);
            upperRaise = upperCompMin + (rolloffFactor * (max - upperCompMin));
//        } else {
//            upperRolloff1 = (max - upperCompMin) / (highest - upperCompMin);
//            upperRolloff2 = upperRolloff1;
        }
        if (lowest < min) {
            lowerRolloff1 = ((lowerCompMax - min) * (1.0 - rolloffFactor)) / (min - lowest);
            lowerRolloff2 = ((lowerCompMax - min) * rolloffFactor) / (lowerCompMax - lowest);
            lowerRaise = min + ((lowerCompMax - min) * (1.0 - rolloffFactor));
//        } else {
//            lowerRolloff1 = (lowerCompMax - min) / (lowerCompMax - lowest);
//            lowerRolloff2 = lowerRolloff1;
        }

        for (KeyValue<Double, Attribute> attribute : attributes) {
            double x = attribute.getKey();
            double result = x;

            if (x < lowerCompMax) {
//                result = (((lowerCompMax - min) * (x - lowest)) / (lowerCompMax - lowest)) + min;                                       // interpolation with one linear segment
                result = (x >= min) ? (lowerRolloff2 * (x - min)) + lowerRaise : (lowerRolloff1 * (x - lowest)) + min;                  // interpolation with two linear segments
            } else if (x > upperCompMin) {
//                result = (((max - upperCompMin) * (x - upperCompMin)) / (highest - upperCompMin)) + upperCompMin;                       // interpolation with one linear segment
                result = (x <= max) ? (upperRolloff1 * (x - upperCompMin)) + upperCompMin : (upperRolloff2 * (x - max)) + upperRaise;   // interpolation with two linear segments
            }
            else {
                continue;
            }
//            System.out.println("DEBUG " + x + " -> " + attribute.getValue().getValue());
            attribute.getValue().setValue(Double.toString(result));
        }
    }

    /**
     * This method does not create an exhaustive tempo map; this is left to the performance rendering after extracting a music performance markup structure from mei.
     * However, to specify at least a basic tempo for the midi sequence, created here, we generate one tempo event at the beginning with the specified bpm.
     * The beatlength (beats! per minute) is taken from the denominator of the first global time signature element. If none can be found, 1/4 is the default value.
     * @param bpm
     * @param track
     */
    private void makeInitialTempo(double bpm, Track track) {
        double beatlength;
        // if there are global time signature information, take the denominator value as beatlength, otherwise default beatlength is 1/4
        try {
            beatlength = 1.0 / Integer.parseInt(this.getRootElement().getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature").getAttributeValue("denominator"));
        } catch (NumberFormatException | NullPointerException e) {
            beatlength = 0.25;
        }
        track.add(EventMaker.createTempo(0, bpm, beatlength));
    }

    /**
     * This method creates an initial tempo where one midi tick corresponds with one millisecond. This is the tempo setting for expressive midi export as the event timing is set by the event dates and not by tempo.
     * @param track
     */
    private void makeMillisecondTickTempo(Track track) {
        track.add(EventMaker.createTempo(0, 60000.0 / this.getPPQ(), 0.25));
    }

    /**
     * scan the part attribute name for a known string to create a gm program change and instrument name event
     * @param part
     * @param track the track that shall correspond to the part
     * @param generateProgramChanges if true, program change events are generated (useful for MIR and as a cheap kind of piano reduction)
     */
    private void processPartName(Element part, Track track, short channel, boolean generateProgramChanges) {
        if ((part.getAttribute("name") == null) || part.getAttributeValue("name").isEmpty()) {          // if there is no name
            if (generateProgramChanges)
                track.add(EventMaker.createProgramChange(channel, 0, EventMaker.PC_Acoustic_Grand_Piano));  // add program change event for Acoustic Grand Piano
            return;
        }

        String name = part.getAttributeValue("name");

        if (generateProgramChanges) {
            track.add(EventMaker.createProgramChange(channel, 0, name));                                // add program change event
        }
        track.add(EventMaker.createTrackName(0, name));                                                 // add track name event to the track
    }

    /**
     * parse the elements of the programChangeMap and generate the respective MIDI events
     * @param part
     * @param track
     * @param channel
     * @return true if there is at least one program change at date 0.0
     */
    private boolean parseProgramChangeMap(Element part, Track track, short channel, boolean exportExpressiveMidi) {
        if (part.getFirstChildElement("dated") == null)
            return false;

        Element programChangeMap = part.getFirstChildElement("dated").getFirstChildElement("programChangeMap");
        if ((programChangeMap == null) || (programChangeMap.getChildCount() == 0))
            return false;

        boolean weHaveAnInitialPrgCh = false;
        for (Element n = programChangeMap.getFirstChildElement("programChange"); n != null; n = Helper.getNextSiblingElement("programChange", n)) {   // go through all programChange elements in the map
            long date = exportExpressiveMidi ? Msm.readMillisecondsDateFromElement(n) : Math.round(Double.parseDouble(Helper.getAttributeValue("date", n)));
            if (date == 0.0)
                weHaveAnInitialPrgCh = true;
            short value = Short.parseShort(n.getAttributeValue("value"));
            track.add(EventMaker.createProgramChange(channel, date, value));        // add program change event
        }
        return weHaveAnInitialPrgCh;
    }

    /**
     * parse the elements in the score map of part (part.dated.score) to midi events and add them to track
     * @param part  the msm source
     * @param track the midi track
     * @param exportExpressiveMidi set true to use the milliseconds dates and durations instead of the raw date and duration attributes
     */
    private void processScore(Element part, Track track, boolean exportExpressiveMidi) {
        if ((part.getFirstChildElement("dated") == null)
                || (part.getFirstChildElement("dated").getFirstChildElement("score") == null)
                || (part.getAttribute("midi.channel") == null))                                                      // if no sufficient information
            return;                                                                                                  // cancel

        int chan = Integer.parseInt(part.getAttributeValue("midi.channel"));                                         // get the midi channel number

        for (Element n = part.getFirstChildElement("dated").getFirstChildElement("score").getFirstChildElement("note"); n != null; n = Helper.getNextSiblingElement("note", n)) {   // go through all note elements in score
            int pitch = Math.round(Float.parseFloat(Helper.getAttributeValue("midi.pitch", n)));                    // Math.round(float) returns int; so far pitches are well captured by number type float

            if (exportExpressiveMidi) {                                                                             // if expressive midi should be exported, we need to use the milliseconds date and duration
                long date = Msm.readMillisecondsDateFromElement(n);

                Attribute velocityAtt = Helper.getAttribute("velocity", n);                                         // get the velocity attribute
                int velocity = (velocityAtt == null) ? 100 : Math.round(Float.parseFloat(velocityAtt.getValue()));  // if there is no velocity attribute set velocity to 100 by default, otherwise Math.round(float) outputs the integer velocity
                track.add(EventMaker.createNoteOn(chan, date, pitch, velocity));

                long dateEnd;
                Attribute endAtt = Helper.getAttribute("milliseconds.date.end", n);
                if (endAtt == null) {
                    System.err.println("Missing attribute \"milliseconds.date.end\" in element " + n.toXML() + ". Using attribute \"duration\" instead.");
                    long dur = Math.round(Double.parseDouble(Helper.getAttributeValue("duration", n)));
                    dateEnd = date + dur;
                } else {
                    dateEnd = Math.round(Double.parseDouble(endAtt.getValue()));
                }
                track.add(EventMaker.createNoteOff(chan, dateEnd, pitch, 0));
            } else {
                long date = Math.round(Double.parseDouble(Helper.getAttributeValue("date", n)));                    // Math.round(double) returns long
                track.add(EventMaker.createNoteOn(chan, date, pitch, 100));

                long dur = Math.round(Double.parseDouble(Helper.getAttributeValue("duration", n)));
                track.add(EventMaker.createNoteOff(chan, date + dur, pitch, 0));
            }

            // TODO: process text (not implemented in mei-to-msm-export, yet, but planned to be added in the future)
        }
    }

    /**
     * convert the channelVolumeMap into a sequence of MIDI controls change events
     * @param part
     * @param track
     * @param exportExpressiveMidi
     */
    private void parseChannelVolumeMap(Element part, Track track, boolean exportExpressiveMidi) {
        if (!exportExpressiveMidi                               // channelVolumeMap is exported only in expressive MIDI mode
                || (part.getFirstChildElement("dated") == null)
                || (part.getAttribute("midi.channel") == null))
            return;

        int chan = Integer.parseInt(part.getAttributeValue("midi.channel"));                                        // get the midi channel number
        Element cvMap = Helper.getFirstChildElement("channelVolumeMap", part.getFirstChildElement("dated"));

        if (cvMap == null) {                                                                                        // if no channelVolumeMap
            track.add(EventMaker.createControlChange(chan, 0, EventMaker.CC_Channel_Volume, 100));                  // make sure the channel volume is set to default
            return;                                                                                                 // cancel
        }

        long prevDate = Long.MAX_VALUE;
        Elements es = cvMap.getChildElements();
        for (int i = es.size() - 1; i >= 0; --i) {                                                                  // traverse the map from back to front
            Element e = es.get(i);

            long date = Msm.readMillisecondsDateFromElement(e);

            boolean mandatory = (Helper.getAttribute("mandatory", e) != null);                                      // if the element has a @mandatory it must be converted to MIDI even if the CONTROL_CHANGE_DENSITY is coarser
            if (!mandatory && (date >= (prevDate - Msm.CONTROL_CHANGE_DENSITY)))                                    // several channelVolume events at the same date make no sense, so we skip all of these and take only the last (first in this for loop as it goes from back to front); this does, however, not apply to mandatory events
                continue;
            prevDate = date;
            int value = Math.round(Float.parseFloat(Helper.getAttributeValue("value", e)));
            track.add(EventMaker.createControlChange(chan, date, EventMaker.CC_Channel_Volume, value));
        }

        // make sure that the channelVolume is set to the default value of 100 at the beginning of the track
        if (prevDate > 0) {                                                                                         // but only if the track does not already start with sub-note dynamics
            track.add(EventMaker.createControlChange(chan, 0, EventMaker.CC_Channel_Volume, 100));
        }
    }

    /**
     * parse the elements in the keySignatureMap of part (part.dated.keySignatureMap) to midi events and add them to track
     *
     * @param part  the msm source
     * @param track the midi track
     */
    private void parseKeySignatureMap(Element part,  Track track, boolean exportExpressiveMidi) {
        if ((part.getFirstChildElement("dated") == null)
                || (part.getFirstChildElement("dated").getFirstChildElement("keySignatureMap") == null))        // if no sufficient information
            return;                                                                                             // cancel

        for (Element e = part.getFirstChildElement("dated").getFirstChildElement("keySignatureMap").getFirstChildElement("keySignature"); e != null; e = Helper.getNextSiblingElement("keySignature", e)) {   // go through all elements in the keySignatureMap
            long date;
            if (exportExpressiveMidi) {
                date = Msm.readMillisecondsDateFromElement(e);
            }
            else
                date = Math.round(Double.parseDouble(e.getAttributeValue("date")));                             // get the date of the key signature

            int accids = 0;
            for (Element a = e.getFirstChildElement("accidental"); a != null; a = Helper.getNextSiblingElement("accidental", a)) {  // count the accidentals (-=flats +=sharps)
                if (a.getAttribute("value") != null) {
                    double value = Double.parseDouble(a.getAttributeValue("value"));
                    if (value > 1.0) {
                        accids++;
                        continue;
                    }
                    if (value < 1.0) {
                        accids--;
                    }
                }
            }
            track.add(EventMaker.createKeySignature(date, accids));
        }
    }

    /**
     * parse the timeSignatureMap and create time signature events from it
     * @param part
     * @param track
     */
    private void parseTimeSignatureMap(Element part,  Track track, boolean exportExpressiveMidi) {
        if ((part.getFirstChildElement("dated") == null)
                || (part.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap") == null))       // if no sufficient information
            return;                                                                                             // cancel

        for (Element e = part.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature"); e != null; e = Helper.getNextSiblingElement("timeSignature", e)) {   // go through all elements in the keySignatureMap
            long date;
            if (exportExpressiveMidi)
                date = Msm.readMillisecondsDateFromElement(e);
            else
                date = Math.round(Double.parseDouble(e.getAttributeValue("date")));

            int numerator = (e.getAttribute("numerator") == null) ? 4 : (int)Math.round(Double.parseDouble(e.getAttributeValue("numerator")));
            int denominator = (e.getAttribute("denominator") == null) ? 4 : (int)Math.round(Double.parseDouble(e.getAttributeValue("denominator")));
            track.add(EventMaker.createTimeSignature(date, numerator, denominator));
        }
    }

    /**
     * parse the markerMap and create marker events from it
     * @param part
     * @param track
     */
    private void parseMarkerMap(Element part,  Track track, boolean exportExpressiveMidi) {
        if ((part.getFirstChildElement("dated") == null)
                || (part.getFirstChildElement("dated").getFirstChildElement("markerMap") == null))              // if no sufficient information
            return;                                                                                             // cancel

        String message;                                                                                         // the marker message

        for (Element e = part.getFirstChildElement("dated").getFirstChildElement("markerMap").getFirstChildElement("marker"); e != null; e = Helper.getNextSiblingElement("marker", e)) {
            try {
                message = e.getAttributeValue("message");
            } catch (NullPointerException | NumberFormatException error) {
                message = "marker";
            }

            if (exportExpressiveMidi)
                track.add(EventMaker.createMarker(Msm.readMillisecondsDateFromElement(e), message));
            else
                track.add(EventMaker.createMarker(Math.round(Double.parseDouble(e.getAttributeValue("date"))), message));
        }
    }

    /**
     * returns the date of the last note's offset (not in milliseconds but in MIDI ticks!)
     * @return
     */
    public synchronized double getEndDate() {
        double latestOffset = 0.0;
        Elements parts = this.getRootElement().getChildElements("part");                            // get all parts

        for (int i = 0; i < parts.size(); ++i) {                                                    // in each part
            Elements notes = parts.get(i).getFirstChildElement("dated").getFirstChildElement("score").getChildElements("note");    // navigate to the note elements

            // compute the offest of each note and keep the last one
            for (int j = notes.size()-1; j >= 0; --j) {                                             // go through all notes
                Element note = notes.get(j);                                                        // get the note
                double date = Double.parseDouble(note.getAttributeValue("date"));                   // get its date
                double dur = Double.parseDouble(note.getAttributeValue("duration"));                // get its duration
                double offset = date + dur;                                                         // compute the offset date
                if (offset > latestOffset)                                                          // if its after the last offset known so far
                    latestOffset = offset;                                                          // set this to the last offset
            }
        }

        return latestOffset;
    }

    /**
     * export standard chroma features with 12 semitones in equal temperament and A = 440 Hz
     * @return
     */
    public Pitches exportChroma() {
        return this.exportPitches(new Key(Key.chromaReferenceFrequenciesEqualTemperament440, true));
    }

    /**
     * export absolute pitches from the MSM score data with 12 semitones per octave in equal temperament and A = 440 Hz,
     * this conforms to the MIDI standard with 0 being the lowest and 127 the highest possible pitch.
     * @return
     */
    public Pitches exportPitches() {
        return this.exportPitches(new Key(Key.midiReferenceFrequenciesEqualTemperament440, false));
    }

    /**
     * export absolute pitches from the MSM score data
     * @param key the key with reference frequencies and octave modulo setting
     * @return
     */
    public Pitches exportPitches(Key key) {
        long startTime = System.currentTimeMillis();                                                    // we measure the time that the conversion consumes
        System.out.println("\nConverting " + ((this.file != null) ? this.file.getName() : "MSM data") + " to pitch data.");
        Pitches pitches = new Pitches(key); // create Pitches object with equal temperament and A = 440 Hz
        pitches.setFile(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".json");        // set a filename for the pitches

        int minPPQ = this.getMinimalPPQ();
        double timingReductionFactor = (double)this.getPPQ() / minPPQ;                                  // for memory efficiency it is highly required reduce the frame count, this here is the factor for this
        System.out.print("timing is reduced to " + minPPQ + " ppq ... ");

        // for each note in the music add its pitches vectors to the pitches object
        Elements parts = this.getRootElement().getChildElements("part");                                // get all parts
        for (int i = 0; i < parts.size(); ++i) {                                                        // in each part
            Elements notes = parts.get(i).getFirstChildElement("dated").getFirstChildElement("score").getChildElements("note");    // navigate to the note elements
            for (int j = notes.size()-1; j >= 0; --j) {                                                 // go through all notes
                Element note = notes.get(j);                                                            // get a note

                int date = (int)Double.parseDouble(note.getAttributeValue("date"));                     // get its date
                int noteOff = date + (int)Double.parseDouble(note.getAttributeValue("duration"));       // compute its noteOff date

                double pitch = Double.parseDouble(note.getAttributeValue("midi.pitch"));                // get its pitch
                if (key.getOctaveModulo()) pitch %= key.getSize();                                      // if the feature represents pitch classes do the modulo operation on the pitch value
                else if (pitch > (key.getSize()-1)) pitch = key.getSize()-1;                            // clip extremely high pitch values at highest possible value
                else if (pitch < 0.0) pitch = 0.0;                                                      // clip pitch values lower than 0.0

                // create the FeatureVector
                FeatureVector feature = new FeatureVector(key);
                feature.getFeatureElement((int) pitch).addEnergy(1.0);

                // associate this note's xml:id with the FeatureElement
                Attribute noteId =  note.getAttribute("id", "http://www.w3.org/XML/1998/namespace");
                if (noteId != null)
                    feature.getFeatureElement((int) pitch).addNoteId(noteId.getValue());

                // do timing reduction
                date /= timingReductionFactor;
                noteOff /= timingReductionFactor;

                // generate pitch data
                for (int k = date; k < noteOff; ++k)                                                    // for as long as the note duration says
                    pitches.addFeatureAt(k, feature);                                                   // add the feature vector to pitches (midi tick-wise)
            }
        }

        System.out.println("MSM to pitch data conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return pitches;      // output the result
    }

    /**
     * a helper method for parsing the milliseconds date of an element
     * @param e
     * @return
     */
    private static long readMillisecondsDateFromElement(Element e) {
        Attribute dateAtt = Helper.getAttribute("milliseconds.date", e);
        if (dateAtt == null) {
            System.err.println("Missing attribute \"milliseconds.date\" in element " + e.toXML() + ". Using attribute \"date\" instead.");
            dateAtt = Helper.getAttribute("date", e);
        }
        return Math.round(Double.parseDouble(dateAtt.getValue()));                                     // Math.round(double) returns long
    }

    /**
     * this method adds xml:ids to all note and rest elements, as far as they do not have an id
     * @return the generated ids count
     */
    public synchronized int addIds() {
        System.out.print("Adding IDs to MSM:");
        Element root = this.getRootElement();
        if (root == null) {
            System.err.println(" Error: no root element found");
            return 0;
        }

        Nodes e = root.query("descendant::*[(local-name()='note' or local-name()='rest') and not(@xml:id)]");
        for (int i = 0; i < e.size(); ++i)                                     // go through all the nodes
            Helper.addUUID((Element) e.get(i));                                // add the xml:id attribute with a UUID

        System.out.println(" done");

        return e.size();
    }
}