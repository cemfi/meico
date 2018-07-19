package meico.midi;

/**
 * @author Axel Berndt.
 */

import meico.audio.Audio;
import meico.mei.Helper;

import javax.sound.midi.*;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class Midi {

    private File file = null;               // the midi file
    private Sequence sequence = null;       // the midi sequence

    /**
     * the most primitive constructor
     */
    public Midi() {
    }

    /**
     * constructor, creates an empty MidiOld instance
     */
    public Midi(int ppq) throws InvalidMidiDataException {
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
    protected void readMidiFile(File file) throws InvalidMidiDataException, IOException {
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
            System.err.println("No midi data loaded.");
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
    public boolean writeMidi(String filename) {
        File file = new File(filename);     // create the file with this filename
        file.getParentFile().mkdirs();      // ensure that the directory exists
        return this.writeMidi(file);
    }

    /**
     * write the sequence to a midi file
     * @param file
     * @return true if success, false if an error occurred
     */
    public boolean writeMidi(File file) {
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

        return audio;                   // return the Audio object
    }
}

