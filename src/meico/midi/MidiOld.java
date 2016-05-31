package meico.midi;

/**
 * Created by Axel Berndt.
 */

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Deprecated
public class MidiOld {

    private File file;              // the midi file
    private int ppq;                // the pulses per quarter resolution of the midi timing
    public List<TrackOld> tracks;      // each track represents a midi channel

    /**
     * constructor, creates an empty MidiOld instance
     */
    public MidiOld(int ppq) {
        this.ppq = ppq;
        this.tracks = null;
        this.file = null;
    }

    /**
     * constructor, instantiates this class from a midi file
     *
     * @param midifile the midi file from which to read the sequence
     */
    public MidiOld(File midifile) {
        Sequence sequence;
        try {
            sequence = (new MidiFileReader()).getSequence(midifile);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        this.parseInputSequence(sequence);
        this.file = midifile;
    }

    /**
     * constructor, instantiates this class from a midi sequence
     *
     * @param sequence
     */
    public MidiOld(Sequence sequence) {
        this.file = null;
        this.parseInputSequence(sequence);
    }

    /**
     * parse a midi sequence and instantiate the corresponding lokal variables
     * @param sequence
     */
    private void parseInputSequence(Sequence sequence) {
        if (sequence.getDivisionType() != Sequence.PPQ) {
            throw new IllegalArgumentException( "Unsupported midi timing division type. Currently, only PPQ is supported.");
            // TODO: instead of an exception it would be better to make the conversion here
        }

        this.ppq = sequence.getResolution();

        // TODO: create the tracks list
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
     * this getter returns the midi sequence
     *
     * @return the midi sequence
     */
    public Sequence getSequence() {
        Sequence sequence = null;
        // generate a midi sequence from the data in the tracks

        // TODO: get the java-midi tracks from all tracks and create a multitrack sequence
        // TODO: or let the tracks output their bit strings and put them together into a midifile

        return sequence;
    }

    /**
     * TODO: this function should be shifted into a helper class
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
    public boolean writeMidi() {
        if (this.file == null) {
            System.out.println("Cannot write to the file system. Path and filename required.");
            return false;
        }

        return this.writeMidi(this.file);
    }

    /**
     * @param file
     * @return
     */
    public boolean writeMidi(File file) {
        try {
            (new MidiFileWriter()).write(this.getSequence(), 1, file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * create a new TrackOld instance and add it to the tracks list
     * @param port
     * @return
     */
    public TrackOld makeTrack(short port) {
        TrackOld track = new TrackOld(port);
        if (!this.tracks.add(track))
            return null;
        return track;
    }
}

