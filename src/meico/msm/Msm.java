package meico.msm;

/**
 * This class holds data in msm format (Musical Sequence Markup).
 * @author Axel Berndt.
 */

import meico.chroma.Chroma;
import meico.mei.Helper;
import meico.midi.*;
import nu.xom.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Msm {

    private File file;
    private Document msm;                                         // the msm document
    private boolean msmValidation = false;                        // indicates whether the input file contained valid msm code (true) or not (false); it is also false if no validation has been performed

    /**
     * constructor
     */
    public Msm() {
        this.file = null;
        this.msm = null;                                            // empty document
        this.msmValidation = false;
    }

    /**
     * constructor
     *
     * @param msm the msm document of which to instantiate the Msm object
     */
    public Msm(Document msm) {
        this.file = null;
        this.msm = msm;
        this.msmValidation = false;
    }

    /**
     * constructor
     *
     * @param file the msm file to be read
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(File file) throws IOException, ParsingException {
        this(file, false);
    }

    /**
     * constructor
     * @param file
     * @param validate
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(File file, boolean validate) throws IOException, ParsingException {
        this.readMsmFile(file, validate);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(String xml) throws IOException, ParsingException {
        this(xml, false);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @param validate validate the code?
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(String xml, boolean validate) throws IOException, ParsingException {
        Builder builder = new Builder(validate);                    // if the validate argument in the Builder constructor is true, the msm should be valid
        try {
            this.msm = builder.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ValidityException e) {                               // in case of a ValidityException (no valid mei code)
            this.msm = e.getDocument();                             // make the XOM Document anyway, we may nonetheless be able to work with it
        }
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(InputStream inputStream) throws IOException, ParsingException {
        this(inputStream, false);
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @param validate
     * @throws IOException
     * @throws ParsingException
     */
    public Msm(InputStream inputStream, boolean validate) throws IOException, ParsingException {
        Builder builder = new Builder(validate);                    // if the validate argument in the Builder constructor is true, the msm should be valid
        try {
            this.msm = builder.build(inputStream);
        } catch (ValidityException e) {                               // in case of a ValidityException (no valid mei code)
            this.msm = e.getDocument();                             // make the XOM Document anyway, we may nonetheless be able to work with it
        }
    }

    /**
     * reads the data from an MSM file into this object
     * @param file
     * @param validate
     * @throws IOException
     * @throws ParsingException
     */
    protected void readMsmFile(File file, boolean validate) throws IOException, ParsingException {
        this.file = file;

        if (!file.exists()) {
            System.err.println("No such file or directory: " + file.getPath());
            this.msm = null;
            this.msmValidation = false;
            return;
        }

        // read file into the msm instance of Document
        Builder builder = new Builder(validate);                  // if the validate argument in the Builder constructor is true, the msm should be valid
        this.msmValidation = true;                                  // the musicXml code is valid until validation fails (ValidityException)
        try {
            this.msm = builder.build(file);
        } catch (ValidityException e) {                             // in case of a ValidityException (no valid msm code)
            this.msmValidation = false;                             // set msmValidation false to indicate that the msm code is not valid
            e.printStackTrace();                                    // output exception message
            for (int i = 0; i < e.getErrorCount(); i++) {           // output all validity error descriptions
                System.err.println(e.getValidityError(i));
            }
            this.msm = e.getDocument();                             // make the XOM Document anyway, we may nonetheless be able to work with it
        }
    }

    /**
     * if the msm document is empty return false, else true
     *
     * @return false if the msm document is empty, else true
     */
    public boolean isValid() {
        return (this.msmValidation);
    }

    /**
     * if the constructor was unable to load the file, the msm document is empty and no further operations
     *
     * @return true if the msm document is empty, else false
     */
    public boolean isEmpty() {
        return (this.msm == null);
    }

    /**
     * @return String with the XML code
     */
    public String toXML() {
        return this.msm.toXML();
    }

    /**
     * @return the msm document
     */
    public Document getDocument() {
        return this.msm;
    }

    /**
     * a setter for the document
     * @param msmDocument
     */
    public void setDocument(Document msmDocument) {
        this.msm = msmDocument;
    }

    /**
     * @return the root element of the msm
     */
    public Element getRootElement() {
        if (this.isEmpty())
            return null;
        return this.msm.getRootElement();
    }

    /**
     * search the given map for the first element with local-name name at or after the given midi.date
     * @param name
     * @param date
     * @param map
     * @return
     */
    public static Element getElementAtAfter(String name, double date, Element map) {
        Elements es;
        if (name.isEmpty())                     // if no specific name given
            es = map.getChildElements();        // search all elements
        else                                    // if specific name given
            es = map.getChildElements(name);    // search only the elements with this name

        for (int i=0; i < es.size(); ++i) {
            Element e = es.get(i);
            if ((e.getAttribute("midi.date") != null) && (Double.parseDouble(e.getAttributeValue("midi.date")) >= date))
                return e;
        }
        return null;
    }

    /**
     * search the given map and find the first element at or after the given midi.date
     * @param date
     * @param map
     * @return
     */
    public static Element getElementAtAfter(double date, Element map) {
        return Msm.getElementAtAfter("", date, map);
    }

    /**
     * search the given map and find the last element with the given local-name name before or at the given midi.date
     * @param name
     * @param date
     * @param map
     * @return
     */
    public static Element getElementBeforeAt(String name, double date, Element map) {
        Elements es;
        if (name.isEmpty())                     // if no specific name given
            es = map.getChildElements();        // search all elements
        else                                    // if specific name given
            es = map.getChildElements(name);    // search only the elements with this name

        for (int i=es.size()-1; i >= 0; --i) {
            Element e = es.get(i);
            if ((e.getAttribute("midi.date") != null) && (Double.parseDouble(e.getAttributeValue("midi.date")) <= date)) {
                return e;
            }
        }
        return null;
    }

    /**
     * search the given map and find the last element before or at the given midi.date
     * @param date
     * @param map
     * @return
     */
    public static Element getElementBeforeAt(double date, Element map) {
        return Msm.getElementBeforeAt("", date, map);
    }

    /**
     * removes all rest elements from the score lists;
     * this method is not part of the mei.exportMsm() cleanup procedure as some applications may still need the rests;
     * others who don't, can call this method to remove all rest elements and get a purged msm
     */
    public void removeRests() {
        if (this.isEmpty()) return;

        Nodes r = this.getRootElement().query("descendant::*[local-name()='rest']");    // select all rest elements
        for (int i = 0; i < r.size(); ++i)
            r.get(i).getParent().removeChild(r.get(i));                                 // remove them
    }

    /**
     * this method removes all empty maps (timeSignatureMap, keySignatureMap, markerMap, sequencingMap etc.);
     * this is to make the msm document a bit smaller and less cluttered
     */
    public void deleteEmptyMaps() {
        if (this.isEmpty()) return;

        Nodes maps = this.getRootElement().query("descendant::*[contains(local-name(), 'Map')]");   // get all elements in the document that have a substring "Map" in their local-name
        for (int i=0; i < maps.size(); ++i) {                                           // go through all these elements
            Element map = (Element)maps.get(i);                                         // the map
            if (map.getChildCount() == 0)                                               // if the map has no children, it is empty
                map.getParent().removeChild(map);                                       // delete it
        }
    }

    /**
     * this method expands all global and local maps according to the sequencingMaps;
     * if a nonempty, local sequencingMap is given in a certain part, that part ignores the global sequencingMap
     */
    public void resolveSequencingMaps() {
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
                eCopy.getAttribute("midi.date").setValue(Double.toString(currentDate + dateOffset));                    // update its date

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

            dateOffset += gt.date - gt.targetDate;                          // update the dateOffset
            currentDate = gt.targetDate;                                    // update currentDate
            i = -1;                                                         // start searching for the next goto
        }

        // last goto has been processed, now do the rest until the end marker
        for (Element e = Msm.getElementAtAfter(currentDate, map); e != null; e = Helper.getNextSiblingElement(e)) { // go through the map elements, starting the first element at or after the goto's target.date, and then go on with the next sibling
            currentDate = Double.parseDouble(e.getAttributeValue("midi.date"));                                     // read its date
            Element eCopy = (Element) e.copy();                                                                     // make a deep copy of the element
            eCopy.getAttribute("midi.date").setValue(Double.toString(currentDate + dateOffset));                    // update its date

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
     * this getter returns the file
     *
     * @return a java File object (this file does not necessarily have to exist in the file system, but may be created there when writing the file with writeMsm())
     */
    public File getFile() {
        return this.file;
    }

    /**
     * with this setter a new filename can be set
     *
     * @param filename the filename including the full path and .msm extension
     */
    public void setFile(String filename) {
        this.file = new File(filename);
    }

    /**
     * writes the msm document to an msm file at this.file (it must be != null);
     * if there is already an msm file with this name, it is replaces!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeMsm() {
        if (this.file == null) {
            System.err.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        if (this.isEmpty()) {
            System.err.println("Empty document, cannot write file.");
            return false;
        }

        return this.writeMsm(this.file.getPath());
    }

    /**
     * writes the msm document to a file (filename should include the path and the extension .msm)
     *
     * @param filename the filename string; it should include the path and the extension .msm
     * @return true if success, false if an error occured
     */
    public boolean writeMsm(String filename) {
        if (this.isEmpty()) {
            System.err.println("Empty document, cannot write file.");
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

        // open the FileOutputStream to write to the file
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, false);   // open file: second parameter (append) is false because we want to overwrite the file if already existing
        } catch (FileNotFoundException | NullPointerException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        // serialize the xml code (encoding, layout) and write it to the file via the FileOutputStream
        boolean returnValue = true;
        Serializer serializer = null;
        try {
            serializer = new Serializer(fileOutputStream, "UTF-8"); // connect serializer with FileOutputStream and specify encoding
            serializer.setIndent(4);                                // specify indents in xml code
            serializer.write(this.msm);                             // write data from msm to file
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            returnValue = false;
        }

        // close the FileOutputStream
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            returnValue = false;
        }

        if (this.file == null)
            this.file = file;

        return returnValue;
    }

    /**
     * transform this MSM via the given xsl file
     * @param xslt
     * @return result of the transform as XOM Document instance
     */
    public Document xslTransformToDocument(File xslt) {
        return Helper.xslTransformToDocument(this.msm, xslt);
    }

    /**
     * transform this MSM via the given xsl file
     * @param xslt
     * @return result of the transform as String instance
     */
    public String xslTransformToString(File xslt) {
        return Helper.xslTransformToString(this.msm, xslt);
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
        if (this.isEmpty())                                                 // if there is no data
            return null;                                                    // return null

        // create an empty midi sequence
        int ppq = Integer.parseInt(this.getRootElement().getFirstChildElement("global").getFirstChildElement("header").getFirstChildElement("pulsesPerQuarter").getAttributeValue("ppq"));  // read the ppq resolution from the msm's global/header/pulsesPerQuarter element
        Sequence seq = null;
        try {
            seq = new Sequence(Sequence.PPQ, ppq);                          // create the midi sequence
        } catch (InvalidMidiDataException e) {                              // if failed for some reason
            e.printStackTrace();                                            // print error message
            return null;                                                    // return null
        }

        // parse the msm, create MidiEvent objects (MidiMessage object with a tick value), add them to a Sequence object (each TrackOld represents a part)
        Track track = seq.createTrack();                                    // create the first midi track; it is used for global meta data (tempo, time signature, key signature, marker)

        this.makeInitialTempo(bpm, track);  // this method does not create an exhaustive tempo map; this is left to the performance rendering after extracting a music performance markup structure from mei; however, to specify at least a basic tempo for the midi sequence created here, we generate one tempo event at the beginning with the specified bpm
        this.parseMarkerMap(this.getRootElement().getFirstChildElement("global"), track);          // parse markerMap
        this.parseTimeSignatureMap(this.getRootElement().getFirstChildElement("global"), track);   // parse timeSignatureMap
        this.parseKeySignatureMap(this.getRootElement().getFirstChildElement("global"), track);    // parse keySignatureMap

        // parse the parts
        for (Element part = this.getRootElement().getFirstChildElement("part"); part != null; part = Helper.getNextSiblingElement("part", part)) {  // go through all parts in the msm document
            if (part.getAttribute("midi.channel") == null) continue;                                                 // no channel information, cancel this part element's processing and continue with the next part

//            { // this stuff is used, when tracks represent ports, not parts!
//              // select the midi track, or create it if necessary
//                int port = Integer.parseInt(part.getAttributeValue("midi.port"));                                    // the port number
//                while ((seq.getTracks().length - 1) < port) seq.createTrack();                                  // create as many tracks as necessary, so that the port number corresponds to the track number in seq (port 0 = seq.getTracks().[0])
//                track = seq.getTracks()[port];                                                                  // select the track
//            }

            track = seq.createTrack();                                                                          // create a new midi track for this part and write all further data into it

            // parse the score, keySignatureMap, timeSignatureMap, markerMap to midi
            this.partName(part, track, generateProgramChanges);                                                         // scan the part attribute name for a known string to create a gm program change and instrument name event

            // the following meta events seem to be supported only on the master track (i.e., track 0, the global) but not in the other tracks
//            this.parseKeySignatureMap(part, track);                                                             // parse keySignatureMap
//            this.parseTimeSignatureMap(part, track);                                                            // parse timeSignatureMap
//            this.parseMarkerMap(part, track);                                                                   // parse markerMap

            this.parseScore(part, track);                                                       // parse score
        }

        // TODO: AllNotesOff at the end

        // create the meico.Midi object
        if (this.getFile() == null)                                                                             // if this instance of msm has no file information
            return new Midi(seq);                                                                               // create the Midi instance only with the sequence (the midi file data are initialized as null) and return it

        if (this.file != null) {
            File midiFile = new File(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".mid"); // set the filename extension of the Midi object to "mid"
            return new Midi(seq, midiFile);                                                                         // create and return the Midi object
        }
        else {
            return new Midi(seq);
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
     *  scan the part attribute name for a known string to create a gm program change and instrument name event
     * @param part
     * @param track the track that shall correspond to the part
     * @param generateProgramChanges if true, program change events are generated (useful for MIR and as a cheap kind of piano reduction)
     */
    private void partName(Element part, Track track, boolean generateProgramChanges) {
        short chan = Short.parseShort(part.getAttributeValue("midi.channel"));

        if ((part.getAttribute("name") == null) || part.getAttributeValue("name").isEmpty()) {          // if there is no name
            if (generateProgramChanges)
                track.add(EventMaker.createProgramChange(chan, 0, (short)0));                           // add program change event for Acoustic Grand Piano
            track.add(EventMaker.createInstrumentName(0, ""));                                          // add an empty instrument name event to the track
            return;
        }

        String name = part.getAttributeValue("name");

        if (generateProgramChanges)
            track.add(EventMaker.createProgramChange(chan, 0, name));                                   // add program change event
        track.add(EventMaker.createInstrumentName(0, name));                                            // add an instrument name event to the track
    }

    /**
     * parse the elements in the score map of part (part.dated.score) to midi events and add them to track
     *
     * @param part  the msm source
     * @param track the midi track
     */
    private void parseScore(Element part,  Track track) {
        if ((part.getFirstChildElement("dated") == null)
                || (part.getFirstChildElement("dated").getFirstChildElement("score") == null)
                || (part.getAttribute("midi.channel") == null))                                                      // if no sufficient information
            return;                                                                                             // cancel

        int chan = Integer.parseInt(part.getAttributeValue("midi.channel"));                                         // get the midi channel number

        for (Element n = part.getFirstChildElement("dated").getFirstChildElement("score").getFirstChildElement("note"); n != null; n = Helper.getNextSiblingElement("note", n)) {   // go through all note elements in score
//            switch (n.getLocalName()) {
//                case "rest":                                                                                    // rests are not represented in midi
//                    break;
//                case "note":                                                                                    // for note elements create note_on and note_off events
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
            long date = Math.round(Double.parseDouble(e.getAttributeValue("midi.date")));
            int accids = (e.getAttribute("accidentals") == null) ? 0 : Integer.parseInt(e.getAttributeValue("accidentals"));
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
    public double getEndDate() {
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
    public Chroma exportChroma() {
        Chroma chroma = new Chroma();                                                                   // create Chroma object with equal temperament and A = 440 Hz
        chroma.setFile(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".chr");          // set a filename for the chroma
        int numFrames = (int) this.getEndDate();                                                        // one frame corresponds with one Midi tick, this computes the number of frames that the music will have

        // add as much zero vectors to chroma as the number of frames/ticks of the music
        double[] feature = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        for (int i = 0; i < numFrames; ++i)
            chroma.addFeatureAt(i, feature);

        // for each note in the music add its chroma vectors to the chroma object
        Elements parts = this.getRootElement().getChildElements("part");                                // get all parts
        for (int i = 0; i < parts.size(); ++i) {                                                        // in each part
            Elements notes = parts.get(i).getFirstChildElement("dated").getFirstChildElement("score").getChildElements("note");    // navigate to the note elements
            for (int j = notes.size()-1; j >= 0; --j) {                                                 // go through all notes
                Element note = notes.get(j);                                                            // get a note

                int date = (int)Double.parseDouble(note.getAttributeValue("midi.date"));                // get its date
                int offset = date + (int)Double.parseDouble(note.getAttributeValue("midi.duration"));   // compute its offset date

                double pitch = Double.parseDouble(note.getAttributeValue("midi.pitch"));                // get its pitch
                int pitchClass = ((int) Math.round(pitch)) % 12;                                        // compute pitch class

                feature = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};     // create the chroma feature vector
                feature[pitchClass] = 1.0;

                for (int k = date; k < offset; ++k)                                                     // for as long as the note duration says
                    chroma.addFeatureAt(k, feature);                                                    // add the feature vector to chroma (midi tick-wise)
            }
        }

        return chroma;      // output the result
    }
}