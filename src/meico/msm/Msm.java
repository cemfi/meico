package meico.msm;

import meico.pitches.FeatureVector;
import meico.pitches.Key;
import meico.pitches.Pitches;
import meico.mei.Helper;
import meico.midi.*;
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
     * this creates an initial Msm instance with empty global maps
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
     * This getter method returns the title string from the root element's attribute title. If missing, use the filename without extension or return "".
     * @return
     */
    public String getTitle() {
        Attribute title;

        try {                                               // try to read the title attribute
            title = this.getRootElement().getAttribute("title");
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
            ppq = this.getRootElement().getAttribute("pulsesPerQuarter");
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
                int dur = (int) Math.round(Double.parseDouble(note.getAttributeValue("midi.duration")));                        // get the note's duration (rounding is necessary for avoiding numeric problems with tripltes)
                dur %= ppq;                                                                                                     // this operation returns 0.0 if the duration is an integer multiple of ppq and it returns something > 0.0 in case of a dotted or smaller note value
                if ((dur != 0.0) && (dur < minDur))                                                                             // in case of a shorter or dotted note that is even shorter than the shortest we had so far
                    minDur = dur;                                                                                               // store it

                int date = (int) Math.round(Double.parseDouble(note.getAttributeValue("midi.date")));                           // get the note's date (rounding is necessary for avoiding numeric problems with tripltes)
                date %= ppq;                                                                                                    // this operation returns 0.0 if the note is on the quarternote grid and it returns something > 0.0 when it is in-between
                if ((date != 0.0) && (date < minDateDif))                                                                       // if we found evidence for a finer grid than the quarternote grid of ppq
                    minDateDif = date;                                                                                          // store the value
            }
        }

        int minPPQDur = ppq / minDur;                               // compute the smallest number of pulses per quarter from the durations
        int minPPQDate = ppq / minDateDif;                          // compute the smallest number of pulses per quarter from the dates

        return (minPPQDate > minPPQDur) ? minPPQDate : minPPQDur;   // return the larger number of the above computations
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
        Element miscMap = new Element("miscMap");
        dated.appendChild(miscMap);
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
     * if a nonempty, local sequencingMap is given in a certain part, that part ignores the global sequencingMap
     */
    public synchronized void resolveSequencingMaps() {
        if (this.isEmpty()) return;

        Element globalSequencingMap = this.getRootElement().getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("sequencingMap"); // get the global sequencingMap (or null if there is none)
        Elements parts = this.getRootElement().getChildElements("part");                                // get all the parts
        Element part, sequencingMap;                                                                    // these elements are used in the for loop that comes next

        // go through all parts and expand their maps according to the underlying sequencingMaps
        for (int i = 0; i < parts.size(); ++i) {                                                          // for each part
            sequencingMap = globalSequencingMap;
            part = parts.get(i);                                                                        // get it as element
            Element localSequencingMap = part.getFirstChildElement("dated").getFirstChildElement("sequencingMap");   // get the part's local sequencingMap if there is one
            if (localSequencingMap != null) sequencingMap = localSequencingMap;                         // if there is a local sequencingMap use it as definitive sequencingMap in this part
            else if (sequencingMap == null) continue;                                                   // otherwise the global sequencingMap is used, but in case there is none, we can continue with the next part
            if (sequencingMap.getChildCount() == 0) continue;                                           // if the sequencingMap is empty, we can continue with the next part

            // go through the score and all maps (except the sequencingMap itself) and apply the sequencingMap to them
            Nodes maps = part.query("descendant::*[local-name()='score' or (contains(local-name(), 'Map') and not((local-name()='sequencingMap') or (local-name()='miscMap')))]");    // get the score and all maps
            for (int j = 0; j < maps.size(); ++j) {                                                       // go through all maps
                Element map = (Element) maps.get(j);                                                     // one map
                if (map.getChildCount() == 0) continue;                                                 // if it is empty, continue with the next map
                Element newMap = this.applySequencingMapToMap(sequencingMap, map);                      // apply the sequencingMap to it
                if (newMap != null) map.getParent().replaceChild(map, newMap);                          // replace the old map by the new one
            }

            // delete the localSequencingMap (because it does not apply anymore)
            if (localSequencingMap != null)
                part.getFirstChildElement("dated").removeChild(localSequencingMap);
        }

        // delete the global sequencingMap (because it does not apply anymore)
        if (globalSequencingMap != null)
            this.getRootElement().getFirstChildElement("global").getFirstChildElement("dated").removeChild(globalSequencingMap);
    }

    /**
     * apply the sequencingMap to the map; this expands the map
     * @param sequencingMap
     * @param map
     * @return the expanded map (to replace the old map) or null (to keep the old map)
     */
    private Element applySequencingMapToMap(Element sequencingMap, Element map) {
        Elements gs = sequencingMap.getChildElements("goto");               // get the gotos
        if (gs.size() == 0) return null;                                    // if there are no gotos in the sequencingMap, i.e. nothing to expand, return null

        // make an ArrayList of Goto instances
        ArrayList<Goto> gotos = new ArrayList<Goto>();                      // this is the list
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
            if (gt.date < currentDate) continue;                            // if the goto is before currentDate continue with the next
            if (!gt.isActive()) continue;                                   // if the goto is not active continue with the next

            // copy everything between currentDate and gt.date from the original map into newMap
            for (Element e = Msm.getElementAtAfter(currentDate, map); e != null; e = Helper.getNextSiblingElement(e)) { // go through the map elements, starting the first element at or after the goto's target.date, and then go on with the next sibling
                currentDate = Double.parseDouble(e.getAttributeValue("midi.date"));                                     // read its date
                if (currentDate >= gt.date) break;                                                                      // if the element's date is at or after the goto (keep in mind, the goto is active) don't copy further, break the loop
                Element eCopy = (Element) e.copy();                                                                     // make a deep copy of the element
                eCopy.getAttribute("midi.date").setValue(Double.toString(currentDate + dateOffset));                    // draw its date

                Attribute repetitionCounter = e.getAttribute("repetitionCounter");                                      // get the counter of how often we have already repeated this element
                if (repetitionCounter == null) {                                                                        // if we pass this element the first time
                    e.addAttribute(new Attribute("repetitionCounter", "0"));                                            // add an attribute to count the repetitions
                }
                else {                                                                                                  // this is not the first time we process this element
                    int reps = 1 + Integer.parseInt(e.getAttributeValue("repetitionCounter"));                          // increase repetition counter
                    e.getAttribute("repetitionCounter").setValue(Integer.toString(reps));                               // write it to the attribute
                    Attribute id = eCopy.getAttribute("id", "http://www.w3.org/XML/1998/namespace");                    // get the id of eCopy or null if it has none
                    if (id != null) id.setValue("meico_repetition_" + reps + "_" + id.getValue());                      // if it has an xml:id, it would appear twice now; this is not valid, so we have to make a new id
                }

                newMap.appendChild(eCopy);                                  // append the copy to the new map
            }

            dateOffset += gt.date - gt.targetDate;                          // draw the dateOffset
            currentDate = gt.targetDate;                                    // draw currentDate
            i = -1;                                                         // start searching for the next goto
        }

        // last goto has been processed, now do the rest until the end marker
        for (Element e = Msm.getElementAtAfter(currentDate, map); e != null; e = Helper.getNextSiblingElement(e)) { // go through the map elements, starting the first element at or after the goto's target.date, and then go on with the next sibling
            currentDate = Double.parseDouble(e.getAttributeValue("midi.date"));                                     // read its date
            Element eCopy = (Element) e.copy();                                                                     // make a deep copy of the element
            eCopy.getAttribute("midi.date").setValue(Double.toString(currentDate + dateOffset));                    // draw its date

            Attribute repetitionCounter = e.getAttribute("repetitionCounter");                                      // get the counter of how often we have already repeated this element
            if (repetitionCounter != null) {                                                                        // this is not the first time we process this element
                int reps = 1 + Integer.parseInt(e.getAttributeValue("repetitionCounter"));                          // increase repetition counter
                e.getAttribute("repetitionCounter").setValue(Integer.toString(reps));                               // write it to the attribute
                Attribute id = eCopy.getAttribute("id", "http://www.w3.org/XML/1998/namespace");                    // get the id of eCopy or null if it has none
                if (id != null) id.setValue("meico_repetition_" + reps + "_" + id.getValue());                      // if it has an xml:id, it would appear twice now; this is not valid, so we have to make a new id
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
     *
     * @return the midi object created or null if this msm object is empty or something else wnet wrong
     */
    public Midi exportMidi() {
        return this.exportMidi(120, true);
    }

    /**
     * converts the msm data into a midi sequence and create a meico.Midi object from it; the tempo is 120bpm by default
     *
     * @param generateProgramChanges if true, program change events are generated (useful for MIR and as a cheap kind of piano reduction); but be careful: if your channel is set on trumpet it would not be set on piano automatically, you have to take care!
     * @return
     */
    public Midi exportMidi(boolean generateProgramChanges) { return this.exportMidi(120, generateProgramChanges);}

    /**
     * converts the msm data into a midi sequence and create a midi object from it
     *
     * @param bpm the tempo of the midi track
     * @return
     */
    public Midi exportMidi(double bpm) { return this.exportMidi(bpm, true);}

    /**
     * converts the msm data into a midi sequence and create a midi object from it
     *
     * @param bpm the tempo of the midi track
     * @param generateProgramChanges if true, program change events are generated (useful for MIR and as a cheap kind of piano reduction); but be careful: if your channel is set on trumpet it would not be set on piano automatically, you have to take care!
     * @return the midi object created or null if this msm object is empty or something else went wrong
     */
    public Midi exportMidi(double bpm, boolean generateProgramChanges) {
        System.out.println("Converting " + ((this.file != null) ? this.file.getName() : "MSM data") + " to MIDI.");

        if (this.isEmpty())                                                 // if there is no data
            return null;                                                    // return null

        // create an empty midi sequence
        int ppq = this.getPPQ();
        Sequence seq = null;
        try {
            seq = new Sequence(Sequence.PPQ, ppq);                          // create the midi sequence
        } catch (InvalidMidiDataException e) {                              // if failed for some reason
            e.printStackTrace();                                            // print error message
            return null;                                                    // return null
        }

        // parse the msm, create MidiEvent objects (MidiMessage object with a tick value), add them to a Sequence object (each Track represents a part)
        Track track = seq.createTrack();                                                                        // create the first midi track; it is used for global meta data (tempo, time signature, key signature, marker)

        this.makeInitialTempo(bpm, track);  // this method does not create an exhaustive tempo map; this is left to the performance rendering after extracting a music performance markup structure from mei; however, to specify at least a basic tempo for the midi sequence created here, we generate one tempo event at the beginning with the specified bpm
        this.parseMarkerMap(this.getRootElement().getFirstChildElement("global"), track);                       // parse markerMap
        this.parseTimeSignatureMap(this.getRootElement().getFirstChildElement("global"), track);                // parse timeSignatureMap
        this.parseKeySignatureMap(this.getRootElement().getFirstChildElement("global"), track);                 // parse keySignatureMap
//        this.parsePedalMap(this.getRootElement().getFirstChildElement("global"), track);                        // parse pedalMap

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
            this.processPartName(part, track, chan, generateProgramChanges);                                    // scan the part attribute name for a known string to create a gm program change and instrument name event

            // if there are local meta events to be generated
            this.parseKeySignatureMap(part, track);                                                             // parse keySignatureMap
            this.parseTimeSignatureMap(part, track);                                                            // parse timeSignatureMap
            this.parseMarkerMap(part, track);                                                                   // parse markerMap
//            this.parsePedalMap(part, track);                                                                    // parse pedalMap

            this.processScore(part, track);                                                                     // parse score
        }

        // TODO: AllNotesOff at the end

        // create the meico.Midi object
        if (this.getFile() != null) {
            File midiFile = new File(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".mid");    // set the filename extension of the Midi object to "mid"
            return new Midi(seq, midiFile);                                                                     // create and return the Midi object
        }
        return new Midi(seq);                                                                                   // the MSM has no file information create the Midi instance only from the sequence and with file=null
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
     *  scan the part attribute name for a known string to create a gm program change and instrument name event
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

        if (generateProgramChanges)
            track.add(EventMaker.createProgramChange(channel, 0, name));                                // add program change event
        track.add(EventMaker.createTrackName(0, name));                                                 // add track name event to the track
    }

    /**
     * parse the elements in the score map of part (part.dated.score) to midi events and add them to track
     *
     * @param part  the msm source
     * @param track the midi track
     */
    private void processScore(Element part, Track track) {
        if ((part.getFirstChildElement("dated") == null)
                || (part.getFirstChildElement("dated").getFirstChildElement("score") == null)
                || (part.getAttribute("midi.channel") == null))                                                      // if no sufficient information
            return;                                                                                                  // cancel

        int chan = Integer.parseInt(part.getAttributeValue("midi.channel"));                                         // get the midi channel number

        for (Element n = part.getFirstChildElement("dated").getFirstChildElement("score").getFirstChildElement("note"); n != null; n = Helper.getNextSiblingElement("note", n)) {   // go through all note elements in score
//            switch (n.getLocalName()) {
//                case "rest":                                                                                       // rests are not represented in midi
//                    break;
//                case "note":                                                                                       // for note elements create note_on and note_off events
                    int pitch = Math.round(Float.parseFloat(n.getAttributeValue("midi.pitch")));                     // Math.round(float) returns int; so far pitches are well captured by number type float
                    long date = Math.round(Double.parseDouble(n.getAttributeValue("midi.date")));                    // Math.round(double) returns long
                    long dur = Math.round(Double.parseDouble(n.getAttributeValue("midi.duration")));
                    track.add(EventMaker.createNoteOn(chan, date, pitch, 100));
                    track.add(EventMaker.createNoteOff(chan, date + dur, pitch, 100));
//                    break;
//            }
            // TODO: process text (not implemented in mei-to-msm-export, yet, but planned to be added in the future)
        }
    }

    /**
     * parse the elements in the keySignatureMap of part (part.dated.keySignatureMap) to midi events and add them to track
     *
     * @param part  the msm source
     * @param track the midi track
     */
    private void parseKeySignatureMap(Element part,  Track track) {
        if ((part.getFirstChildElement("dated") == null)
                || (part.getFirstChildElement("dated").getFirstChildElement("keySignatureMap") == null))        // if no sufficient information
            return;                                                                                             // cancel

        for (Element e = part.getFirstChildElement("dated").getFirstChildElement("keySignatureMap").getFirstChildElement("keySignature"); e != null; e = Helper.getNextSiblingElement("keySignature", e)) {   // go through all elements in the keySignatureMap
            long date = Math.round(Double.parseDouble(e.getAttributeValue("midi.date")));                       // get the date of the key signature
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
    private void parseTimeSignatureMap(Element part,  Track track) {
        if ((part.getFirstChildElement("dated") == null)
                || (part.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap") == null))       // if no sufficient information
            return;                                                                                             // cancel

        for (Element e = part.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature"); e != null; e = Helper.getNextSiblingElement("timeSignature", e)) {   // go through all elements in the keySignatureMap
            long date = Math.round(Double.parseDouble(e.getAttributeValue("midi.date")));
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
    private void parseMarkerMap(Element part,  Track track) {
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
            track.add(EventMaker.createMarker(Math.round(Double.parseDouble(e.getAttributeValue("midi.date"))), message));
        }
    }

    /**
     * returns the date when the last note offset
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
                double date = Double.parseDouble(note.getAttributeValue("midi.date"));              // get its date
                double dur = Double.parseDouble(note.getAttributeValue("midi.duration"));           // get its duration
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
        System.out.println("Converting " + ((this.file != null) ? this.file.getName() : "MSM data") + " to pitch data.");
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

                int date = (int)Double.parseDouble(note.getAttributeValue("midi.date"));                // get its date
                int noteOff = date + (int)Double.parseDouble(note.getAttributeValue("midi.duration"));  // compute its noteOff date

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

        System.out.println("done");

        return pitches;      // output the result
    }
}