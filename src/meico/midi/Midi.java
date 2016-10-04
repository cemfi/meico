package meico.midi;

/**
 * @author Axel Berndt.
 */

import meico.audio.Audio;

import javax.sound.midi.*;
import javax.sound.midi.spi.MidiFileReader;
import javax.sound.midi.spi.MidiFileWriter;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;

public class Midi {

    private File file = null;               // the midi file
    private Sequence sequence = null;       // the midi sequence
    private Sequencer sequencer = null;     // a sequencer to playback midi sequences

    /**
     * the most primitive constructor
     */
    public Midi() {
        this.initSequencer();   // initialize a sequencer
    }

    /**
     * constructor, creates an empty MidiOld instance
     */
    public Midi(int ppq) throws InvalidMidiDataException {
        this();
        this.sequence = new Sequence(Sequence.PPQ, 720);
    }

    /**
     * constructor, instantiates a Midi object from a sequence and sets the midi file (a possibly existing file is not loaded and writeMidi() will overwrite it)
     *
     * @param sequence the sequence from which to instantiate the object
     * @param midifile target midi file
     */
    public Midi(Sequence sequence, File midifile) {
        this();
        this.sequence = sequence;
        this.file = midifile;
    }

    /**
     * constructor, instantiates this class from a midi sequence
     *
     * @param sequence
     */
    public Midi(Sequence sequence) {
        this();
        this.sequence = sequence;
    }

    /**
     * this constructor instantiates a Midi object from a midi file
     * @param midifile
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public Midi(File midifile) throws InvalidMidiDataException, IOException {
        this();
        this.readMidiFile(midifile);
    }

    /**
     * initialize the sequencer for Midi playback;
     * this is automatically done during initialization, but if that fails the application might call this method to try again
     *
     * @return return true if successful, else false
     */
    public boolean initSequencer() {
        if (this.sequencer != null)     // if it is already initialized
            return true;                // we are done

        try {
            this.sequencer = MidiSystem.getSequencer();
//            this.sequencer.open();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * read a Midi file into the sequence
     * @param file
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    protected void readMidiFile(File file) throws InvalidMidiDataException, IOException {
        this.sequence = (new MidiFileReader()).getSequence(file);
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
    public void setFile(String filename) {
        this.file = new File(filename);
    }

    /**
     * a setter to set the file
     * @param file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * a getter for the sequencer object
     *
     * @return
     */
    public Sequencer getSequencer() {
        return this.sequencer;
    }

    /**
     * a sequencer setter
     * @param sequencer
     */
    public void setSequencer(Sequencer sequencer) {
        this.sequencer = sequencer;
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
    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    /**
     * @param sequence
     */
    public void print(Sequence sequence) {
        if (sequence == null) {
            System.out.println("No midi data loaded.");
            return;
        }
        System.out.println("Printing midi data ...");
        for (int t = 0; t < sequence.getTracks().length; ++t) {
            System.out.println("TrackOld " + t + " contains " + sequence.getTracks()[t].size() + " events.");
            for (int e = 0; e < sequence.getTracks()[t].size(); ++e) {
                System.out.print("@" + sequence.getTracks()[t].get(e).getTick() + " ");
                if (sequence.getTracks()[t].get(e).getMessage() instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) sequence.getTracks()[t].get(e).getMessage();
                    System.out.print("Channel: " + sm.getChannel() + " Command: " + sm.getCommand() + " ");
                    if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        int key = sm.getData1();
                        int velocity = sm.getData2();
                        System.out.print("noteOn, " + " key: " + key + " velocity: " + velocity);
                    } else if (sm.getCommand() == ShortMessage.NOTE_OFF) {
                        int key = sm.getData1();
                        int velocity = sm.getData2();
                        System.out.print("noteOff, " + " key: " + key + " velocity: " + velocity);
                    }
                }
                else {
                    System.out.print("Other message: " + sequence.getTracks()[t].get(e).getMessage().getClass());
                }
                System.out.println();
            }
            System.out.println("---");
        }
    }

    /**
     * write the sequence to a midi file; this overwrites the midi file indicated by attribute file (must be != null)
     *
     * @return true if success, false if an error occurred
     */
    public void writeMidi() throws IOException {
        if (this.file == null) {
            System.out.println("Cannot write to the file system. Path and filename required.");
            return;
        }

        this.writeMidi(this.file);
    }

    /**
     * @param file
     * @return
     */
    public void writeMidi(File file) throws IOException {
        (new MidiFileWriter()).write(this.getSequence(), 1, file);
    }

    /**
     * start playing the midi sequence
     * @throws InvalidMidiDataException
     */
    public void play() throws InvalidMidiDataException {
        if (this.sequencer == null) {                   // if no sequencer created so far (should be done at initialization)
            if (!this.initSequencer()) {                // try again, if it still fails
                System.err.println("Midi playback failed: no Midi sequencer initialized."); // output error message
                return;                                 // skip
            }
        }
        else                                            // otherwise there is a sequencer that may even play midi data at the moment
            if (this.sequencer.isOpen()) this.stop();   // stop it

        try {
            this.sequencer.open();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            return;
        }

        this.sequencer.setSequence(this.sequence);      // assign the midi sequence to the sequencer
        this.sequencer.start();                         // start playback
    }

    /**
     * stop midi playback
     */
    public void stop() {
        if ((this.sequencer != null) && (this.sequencer.isOpen())) {
            this.sequencer.stop();
            this.sequencer.setMicrosecondPosition(0);
            this.sequencer.close();
        }
    }

    /**
     * this is an audio file exporter that uses the default soundbank for synthesis
     * @return
     */
    public Audio exportAudio() {
        return this.exportAudio(null);
    }

    /**
     * this is an audio exporter that uses the specified soundbank or, if null, the default soundbank for synthesis
     * @param soundbankFile a valid soundbank file or null to use the default soundbank
     * @return
     */
    public Audio exportAudio(File soundbankFile) {
        Midi2AudioRenderer renderer;                // an instance of the renderer
        try {
            renderer = new Midi2AudioRenderer();    // initialize the renderer
        } catch (MidiUnavailableException | InvalidMidiDataException | IOException e) {
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
            audio = new Audio(stream, new File(this.file.getPath().substring(0, this.file.getPath().length() - 3) + "wav"));  // set its file name, derived from this name but with different file type extension
        }
        else {
            audio = new Audio(stream);
        }

        return audio;                   // return the Audio object
    }
}

