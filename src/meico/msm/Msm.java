package meico.msm;

/**
 * This class holds data in msm format (Musical Sequence Markup).
 * @author Axel Berndt.
 */

import meico.mei.Helper;
import meico.midi.*;
import nu.xom.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.*;

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
     */
    public Msm(File file) throws IOException, ParsingException {
        this.readMsmFile(file, false);
    }

    /**
     * read an msm file
     * @param file
     * @param validation
     */
    protected void readMsmFile(File file, boolean validation) throws IOException, ParsingException {
        this.file = file;

        if (!file.exists()) {
            System.out.println("No such file or directory: " + file.getPath());
            this.msm = null;
            this.msmValidation = false;
            return;
        }

        // read file into the mei instance of Document
        Builder builder = new Builder(false);                       // if the validate argument in the Builder constructor is true, the msm should be valid
        this.msmValidation = true;                                  // the mei code is valid until validation fails (ValidityException)
        try {
            this.msm = builder.build(file);
        } catch (ValidityException e) {                             // in case of a ValidityException (no valid msm code)
            this.msmValidation = false;                             // set msmValidation false to indicate that the msm code is not valid
            e.printStackTrace();                                    // output exception message
            for (int i = 0; i < e.getErrorCount(); i++) {           // output all validity error descriptions
                System.out.println(e.getValidityError(i));
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
            System.out.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        if (this.isEmpty()) {
            System.out.println("Empty document, cannot write file.");
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
            System.out.println("Empty document, cannot write file.");
            return false;
        }

        // create the file in the file system
        File file = new File(filename);
        file.getParentFile().mkdirs();                              // ensure that the directory exists
        try {
            file.createNewFile();                                   // create the file if it does not already exist
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }

        // open the FileOutputStream to write to the file
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, false);   // open file: second parameter (append) is false because we want to overwrite the file if already existing
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        } catch (SecurityException e) {
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
        } catch (NullPointerException e) {
            e.printStackTrace();
            returnValue = false;
        } catch (IOException e) {
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

        File midiFile = new File(this.getFile().getPath().substring(0, this.getFile().getPath().length() - 3) + "mid"); // set the filename extension of the Midi object to "mid"
        return new Midi(seq, midiFile);                                                                         // create and return the Midi object
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
        } catch (NumberFormatException e) {
            beatlength = 0.25;
        } catch (NullPointerException e) {
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
            } catch (NullPointerException error) {
                message = "marker";
            } catch (NumberFormatException error) {
                message = "marker";
            }
            track.add(EventMaker.createMarker(Math.round(Double.parseDouble(e.getAttributeValue("midi.date"))), message));
        }
    }
}