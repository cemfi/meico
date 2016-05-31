package meico.midi;

/**
 * @author Axel Berndt.
 */

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

public class Midi {

    private Sequence sequence;              // the midi sequence
    private Sequencer sequencer = null;     // a sequencer to play back midi sequences
    private File file;                      // the midi file

    /**
     * the most primitive constructor
     */
    public Midi() {
        this.sequence = null;
        this.file = null;
    }

    /**
     * constructor, creates an empty MidiOld instance
     */
    public Midi(int ppq) throws InvalidMidiDataException {
        this.file = null;
        this.sequence = new Sequence(Sequence.PPQ, 720);
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
        this.file = null;
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
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     */
    public void play() throws MidiUnavailableException, InvalidMidiDataException {
        if (this.sequencer == null)                     // if no sequencer created so far
            this.sequencer = MidiSystem.getSequencer(); // create a sequencer instance
        else                                            // otherwise there is a sequencer that may even play midi data at the moment
            this.stop();                                // stop it

        this.sequencer.open();
        this.sequencer.setSequence(this.sequence);      // assign the midi sequence to the sequencer
        this.sequencer.start();                         // start playback
    }

    /**
     * stop midi playback
     */
    public void stop() {
        if (this.sequencer != null) {
            this.sequencer.stop();
            this.sequencer.setMicrosecondPosition(0);
        }
    }
}

