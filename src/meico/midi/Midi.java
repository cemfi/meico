package meico.midi;

import meico.audio.Audio;
import meico.mei.Helper;
import meico.mpm.elements.maps.TempoMap;
import meico.msm.Msm;

import javax.sound.midi.*;
import javax.sound.sampled.AudioInputStream;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;

/**
 * This class holds Midi data and provides som functionality for it.
 * @author Axel Berndt.
 */

public class Midi {
    private File file = null;               // the midi file
    private Sequence sequence = null;       // the midi sequence

    /**
     * the most primitive constructor creates an empty MIDI sequence with default PPQ of 720
     */
    public Midi() throws InvalidMidiDataException {
        this(720);
    }

    /**
     * constructor, creates an empty MIDI sequence with the given PPQ timing resolution
     */
    public Midi(int ppq) throws InvalidMidiDataException {
        this.sequence = new Sequence(Sequence.PPQ, ppq);
    }

    /**
     * constructor, instantiates a Midi object from a sequence and sets the midi file (a possibly existing file is not loaded and writeMidi() will overwrite it)
     *
     * @param sequence the sequence from which to instantiate the object
     * @param midifile target midi file
     */
    public Midi(Sequence sequence, File midifile) {
        this.sequence = sequence;
        this.file = midifile;
    }

    /**
     * constructor, instantiates this class from a midi sequence
     *
     * @param sequence
     */
    public Midi(Sequence sequence) {
        this.sequence = sequence;
    }

    /**
     * this constructor instantiates a Midi object from a midi file
     * @param midifile
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public Midi(File midifile) throws InvalidMidiDataException, IOException {
        this.readMidiFile(midifile);
    }

    /**
     * read a Midi file into the sequence
     * @param file
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    protected synchronized void readMidiFile(File file) throws InvalidMidiDataException, IOException {
        this.sequence = MidiSystem.getSequence(file);
        this.file = file;
    }

    /**
     * check if there is any midi data in the sequence
     * @return
     */
    public boolean isEmpty() {
        return (this.sequence == null);
    }

    /**
     * this getter returns the File object
     *
     * @return the midi file
     */
    public File getFile() {
        return this.file;
    }

    /**
     * with this setter a new filename can be set
     *
     * @param filename the filename including the full path and .mid extension
     */
    public synchronized void setFile(String filename) {
        this.file = new File(filename);
    }

    /**
     * a setter to set the file
     * @param file
     */
    public synchronized void setFile(File file) {
        this.file = file;
    }

    /**
     * determine the standard midi file format (0, 1 or 2) of the current midi file/sequence
     * @return
     */
    public synchronized int getMidiFileFormat() {
        int midiFileType;
        if ((this.file != null) && this.file.exists()) {                                                    // if there is a midi file
            try {
                midiFileType = MidiSystem.getMidiFileFormat(this.file).getType();                           // get the file type from it
            } catch (InvalidMidiDataException | IOException e) {                                            // if it fails
                e.printStackTrace();
                if (this.sequence.getTracks().length == 1) {                                                // if there is just one track in the sequence
                    System.err.println("Assuming standard midi file format 0.");
                    midiFileType = 0;                                                                       // it is probably type 0
                }
                else {
                    System.err.println("Assuming standard midi file format 1.");
                    midiFileType = 1;                                                                       // standard midi file format 1 is the most common, so assume this in the further processing
                }
            }
        } else {                                                                                            // if there is no midi file so far we cannot read its format
            int[] types = MidiSystem.getMidiFileTypes(this.sequence);                                       // which types are possible for the given sequence?
            if (types.length > 0) {                                                                         // if we get at least one suggestion
                midiFileType = types[types.length - 1];                                                     // take the highest
            }
            else {
                System.err.println("Failed to identify the standard midi file format for the given sequence.");
                if (this.sequence.getTracks().length == 1) {                                                // if there is just one track in the sequence
                    System.err.println("Assuming standard midi file format 0.");
                    midiFileType = 0;                                                                       // it is probably type 0
                }
                else {
                    System.err.println("Assuming standard midi file format 1.");
                    midiFileType = 1;                                                                       // standard midi file format 1 is the most common, so assume this in the further processing
                }
            }
        }

        return midiFileType;
    }

    /**
     * this getter returns the midi sequence
     *
     * @return the midi sequence
     */
    public Sequence getSequence() {
        return this.sequence;
    }

    /**
     * this setter set the sequence
     * @param sequence
     */
    public synchronized void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    /**
     * this getter returns the timing resolution of the Midi sequence (in PPQ) or throws an exception if the timing concept is not PPQ
     * @return
     * @throws Exception
     */
    public synchronized int getPPQ() throws Exception {
        if (this.sequence.getDivisionType() == Sequence.PPQ) {
            return this.sequence.getResolution();
        }
        throw new Exception("Error: MIDI timing is in SMTPE, not PPQ!");
    }

    /**
     * retrieve the tempo map from the MIDI data
     * @return
     */
    public synchronized TempoMap getTempoMap() {
        TempoMap tempoMap = TempoMap.createTempoMap();
        if (tempoMap == null)
            return null;

        Track[] tracks = this.sequence.getTracks();                                     // get the individual tracks from the sequence
        for (Track track : tracks) {                                                    // go through all tracks
            for (int e = 0; e < track.size(); ++e) {                                    // for all the events in the track
                MidiEvent event = track.get(e);                                         // get the current event
                if (event.getMessage() instanceof MetaMessage) {                        // if it is a meta message
                    MetaMessage m = (MetaMessage) event.getMessage();                   // get the message
                    if (m.getType() == EventMaker.META_Set_Tempo) {                     // if it is a tempo event (that is what we are looking for)
                        int mpq = EventMaker.byteArrayToInt(m.getData());               // get the milliseconds per quarter note tempo value
                        double bpm = 60000000.0 / mpq;                                  // compute quarter notes per minute tempo
                        tempoMap.addTempo(event.getTick(), Double.toString(bpm), 0.25); // add a new tempo element to the MPM tempoMap
                    }
                }
            }
        }

        return tempoMap;
    }

    /**
     * returns the length of the midi sequence in ticks
     * @return
     */
    public synchronized long getTickLength() {
        return this.sequence.getTickLength();
    }

    /**
     * returns the length of the midi sequence in microseconds
     * @return
     */
    public synchronized long getMicrosecondLength() {
        return this.sequence.getMicrosecondLength();
    }

    /**
     * print some basic MIDI data to a string
     * @param sequence
     */
    public static String print(Sequence sequence) {
        if (sequence == null) {
            return "No midi data loaded.";
        }
//        System.out.println("Printing midi data ...");

        StringBuilder print = new StringBuilder();

        for (int t = 0; t < sequence.getTracks().length; ++t) {
            print.append("Track ").append(t).append(" contains ").append(sequence.getTracks()[t].size()).append(" events.\n");
            for (int e = 0; e < sequence.getTracks()[t].size(); ++e) {
                print.append("@").append(sequence.getTracks()[t].get(e).getTick()).append(" ");
                if (sequence.getTracks()[t].get(e).getMessage() instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) sequence.getTracks()[t].get(e).getMessage();
                    print.append("Channel: ").append(sm.getChannel()).append(" Command: ").append(sm.getCommand()).append(" ");
                    switch (sm.getCommand()) {
                        case ShortMessage.NOTE_ON: {
                            int key = sm.getData1();
                            int velocity = sm.getData2();
                            print.append("noteOn, " + " key: ").append(key).append(" velocity: ").append(velocity);
                            break;
                        }
                        case ShortMessage.NOTE_OFF: {
                            int key = sm.getData1();
                            int velocity = sm.getData2();
                            print.append("noteOff, " + " key: ").append(key).append(" velocity: ").append(velocity);
                            break;
                        }
                        case ShortMessage.PROGRAM_CHANGE: {
                            int prg = sm.getData1();
                            print.append("program change, " + " number: ").append(prg);
                        }
                        default: {
                            print.append("Other message: ").append(sequence.getTracks()[t].get(e).getMessage().getClass());
                        }
                    }
                }
                else {
                    print.append("Other message: ").append(sequence.getTracks()[t].get(e).getMessage().getClass());
                }
                print.append("\n");
            }
            print.append("---");
        }

        return print.toString();
    }

    /**
     * In MIDI noteOff events are often encoded as notOn events with velocity 0.
     * With this method these events are converted to real noteOffs.
     * The sequence to be altered is this Midi object's sequence.
     * @return the number of events changed
     */
    public synchronized int noteOns2NoteOffs() {
        return Midi.noteOns2NoteOffs(this.sequence);
    }

    /**
     * In MIDI noteOff events are often encoded as notOn events with velocity 0.
     * With this method these events are converted to real noteOffs.
     * @param sequence the sequence to be altered
     * @return the number of events changed
     */
    public static int noteOns2NoteOffs(Sequence sequence) {
        int eventsChanged = 0;
        for (int t = 0; t < sequence.getTracks().length; ++t) {                                     // for all tracks
            for (int e = 0; e < sequence.getTracks()[t].size(); ++e) {                              // for all events in the track
                if (sequence.getTracks()[t].get(e).getMessage() instanceof ShortMessage) {          // if it is a ShortMessage
                    ShortMessage sm = (ShortMessage) sequence.getTracks()[t].get(e).getMessage();   // cast it to a ShortMessage
                    if ((sm.getCommand() == ShortMessage.NOTE_ON) && (sm.getData2() == 0)) {        // if this is a noteOn with velocity 0, which should be a notOff
                        try {
                            sm.setMessage(EventMaker.NOTE_OFF, sm.getChannel(), sm.getData1(), 0);  // convert it to noteOff
                            eventsChanged++;
                        } catch (InvalidMidiDataException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        return eventsChanged;
    }

    /**
     * In MIDI noteOff events are often encoded as notOn events with velocity 0.
     * With this method noteOffs are replaced by noteOns.
     * The sequence to be altered is this Midi object's sequence.
     * @return the number of events changed
     */
    public synchronized int noteOffs2NoteOns() {
        return Midi.noteOffs2NoteOns(this.sequence);
    }

    /**
     * In MIDI noteOff events are often encoded as notOn events with velocity 0.
     * With this method noteOffs are replaced by noteOns.
     * @param sequence the sequence to be altered
     * @return the number of events changed
     */
    public static int noteOffs2NoteOns(Sequence sequence) {
        int eventsChanged = 0;
        for (int t = 0; t < sequence.getTracks().length; ++t) {                                     // for all tracks
            for (int e = 0; e < sequence.getTracks()[t].size(); ++e) {                              // for all events in the track
                if (sequence.getTracks()[t].get(e).getMessage() instanceof ShortMessage) {          // if it is a ShortMessage
                    ShortMessage sm = (ShortMessage) sequence.getTracks()[t].get(e).getMessage();   // cast it to a ShortMessage
                    if (sm.getCommand() == ShortMessage.NOTE_OFF) {                                 // if this is a noteOff
                        try {
                            sm.setMessage(EventMaker.NOTE_ON, sm.getChannel(), sm.getData1(), 0);   // convert it to a noteOn with 0 velocity
                            eventsChanged++;
                        } catch (InvalidMidiDataException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        return eventsChanged;
    }

    /**
     * adds an offset (in ticks) to all events in this MIDI's sequence
     * @param offsetInTicks
     */
    public void addOffset(long offsetInTicks) {
        if (offsetInTicks == 0)
            return;

        for (Track track : this.getSequence().getTracks()) {
            for (int i=0; i < track.size(); ++i) {
                MidiEvent e = track.get(i);
                e.setTick(Math.max(0, e.getTick() + offsetInTicks));    // we do not allow negative tick values
            }
        }
    }

    /**
     * this creates a copy of the input sequence
     * @param sequence the sequence to be cloned
     * @return the clone of the input sequence or null
     */
    public static Sequence cloneSequence(Sequence sequence) {
        Sequence cloneSeq;
        try {
            cloneSeq = new Sequence(sequence.getDivisionType(), sequence.getResolution());                      // create a new sequence with the same timing as the original sequence
        } catch (InvalidMidiDataException | NullPointerException e) {                                           // if it fails
            e.printStackTrace();                                                                                // print exception message
            return null;                                                                                        // return null (the application should test this before it starts working with it)
        }

        Track[] tracks = sequence.getTracks();                                                                  // get all the tracks of the original sequence
        for (Track track : tracks) {                                                                            // go through all tracks
            Track newTrack = cloneSeq.createTrack();                                                            // create a new empty track in the new sequence
            for (int e = 0; e < track.size(); ++e) {                                                            // go through all the midi events in the original track
                MidiEvent event = track.get(e);                                                                 // get the current track
                MidiEvent newEvent = new MidiEvent((MidiMessage) event.getMessage().clone(), event.getTick());  // create a clone of the original event by cloning the midi message and the event's tick date
                newTrack.add(newEvent);                                                                         // add the cloned event to the new track
            }
        }

        return cloneSeq;                                                                                        // return the resulting sequence
    }

    /**
     * write the sequence to a midi file; this overwrites the midi file indicated by attribute file (must be != null)
     *
     * @return true if success, false if an error occurred
     */
    public synchronized boolean writeMidi() {
        if (this.file == null) {
            System.err.println("Cannot write to the file system. Path and filename required.");
            return false;
        }

        return this.writeMidi(this.file);
    }

    /**
     * write the sequence to a midi file with the specified path and filename
     * @param filename
     * @return true if success, false if an error occurred
     */
    public synchronized boolean writeMidi(String filename) {
        File file = new File(filename);     // create the file with this filename
        file.getParentFile().mkdirs();      // ensure that the directory exists
        return this.writeMidi(file);
    }

    /**
     * write the sequence to a midi file
     * @param file
     * @return true if success, false if an error occurred
     */
    public synchronized boolean writeMidi(File file) {
        try {
            file.createNewFile();                                   // create the file if it does not already exist
            MidiSystem.write(this.sequence, 1, file);
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * this is an audio file exporter that uses the default soundbank for synthesis
     * @return
     */
    public Audio exportAudio() {
        return this.exportAudio((File) null);
    }

    /**
     * this is an audio exporter that uses the specified soundbank or, if null, the default soundbank for synthesis
     * @param soundbankUrl a valid URL to a soundbank file or null to use the default soundbank
     * @return
     */
    public Audio exportAudio(URL soundbankUrl) {
        File soundbankFile = null;
        try {
            soundbankFile = new File(URLDecoder.decode(soundbankUrl.getFile(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this.exportAudio(soundbankFile);
    }

    /**
     * this is an audio exporter that uses the specified soundbank or, if null, the default soundbank for synthesis
     * @param soundbankFile a valid soundbank file or null to use the default soundbank
     * @return
     */
    public Audio exportAudio(File soundbankFile) {
        long startTime = System.currentTimeMillis();                            // we measure the time that the conversion consumes
        System.out.println("\nConverting " + ((this.file != null) ? this.file.getName() : "MIDI data") + " to audio.");
        Midi2AudioRenderer renderer;                // an instance of the renderer
        try {
            renderer = new Midi2AudioRenderer();    // initialize the renderer
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            return null;
        }

        AudioInputStream stream = null;             // the stream that the renerer fills
        try {
            stream = renderer.renderMidi2Audio(this.sequence, soundbankFile);   // do rendering of midi sequence into audio stream
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }

        if (stream == null)                         // if rendering failed
            return null;                            // return null

        Audio audio;                                // create Audio object
        if (this.file != null) {
            audio = new Audio(stream, new File(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".wav"));  // set its file name, derived from this name but with different file type extension
        }
        else {
            audio = new Audio(stream);
        }

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("MIDI to audio conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return audio;                   // return the Audio object
    }

    /**
     * this is an audio exporter that uses the specified soundbank for synthesis
     * @param soundbank a Soundbank object or null to use the default soundfont
     * @return
     */
    public Audio exportAudio(Soundbank soundbank) {
        long startTime = System.currentTimeMillis();                            // we measure the time that the conversion consumes
        System.out.println("\nConverting " + ((this.file != null) ? this.file.getName() : "MIDI data") + " to audio.");
        Midi2AudioRenderer renderer;                // an instance of the renderer
        try {
            renderer = new Midi2AudioRenderer();    // initialize the renderer
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            return null;
        }

        AudioInputStream stream = null;             // the stream that the renerer fills
        try {
            stream = renderer.renderMidi2Audio(this.sequence, soundbank, 44100, 16, 2); // do rendering of midi sequence into audio stream
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }

        if (stream == null)                         // if rendering failed
            return null;                            // return null

        Audio audio;                                // create Audio object
        if (this.file != null) {
            audio = new Audio(stream, new File(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".wav"));  // set its file name, derived from this name but with different file type extension
        }
        else {
            audio = new Audio(stream);
        }

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("MIDI to audio conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return audio;                   // return the Audio object
    }

    /**
     * convert the MIDI data to MSM
     * @return
     */
    public Msm exportMsm() {
        return this.exportMsm(true, true);
    }

    /**
     * convert the MIDI data to MSM
     * @param cleanup set true to return a clean msm file or false to keep all the crap from the conversion process
     * @param useDefaultInstrumentNames
     * @return
     */
    public Msm exportMsm(boolean useDefaultInstrumentNames, boolean cleanup) {
        long startTime = System.currentTimeMillis();                            // we measure the time that the conversion consumes
        System.out.println("\nConverting " + ((this.file != null) ? this.file.getName() : "MIDI data") + " to MSM.");
        int ppq;
        try {
            ppq = this.getPPQ();                                                                            // try to read the ppq value from the MIDI sequence
        } catch (Exception e) {                                                                             // if ppq cannot be read from the MIDI sequence
            e.printStackTrace();
            System.err.println("Assuming MIDI time resolution of 360 pulses per quarter.");
            ppq = 360;                                                                                      // assume 360 (standard in most DSPs)
        }

        Msm msm = Msm.createMsm(Helper.getFilenameWithoutExtension(this.getFile().getName()), null, ppq);   // create minimal Msm instance
        msm.setFile(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".msm");                 // set the file of the msm

        /* Which standard midi file format is used? This changes the event processing a bit:
         * in a format 0 file all non-channel information are global,
         * in a format 1 file the first part contains global information,
         * in a format 2 file all is local and no global track exists
         */
        int midiFileFormat = this.getMidiFileFormat();
        System.out.println("MIDI file format " + midiFileFormat + " detected.");

        Midi2MsmConverter converter = new Midi2MsmConverter(midiFileFormat, useDefaultInstrumentNames, this.sequence, msm);          // create a midi to msm converter instance
        converter.convert();                                                                                // do the conversion, msm will hold the result

        // cleanup the msm code, remove empty maps
        if (cleanup)
            Helper.msmCleanup(msm);

        System.out.println("MIDI to MSM conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return msm;                                                                                         // return the result
    }
}

