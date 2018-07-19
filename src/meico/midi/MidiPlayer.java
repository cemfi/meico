package meico.midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class MidiPlayer {
    private Sequencer sequencer = null;         // a sequencer to playback midi sequences
    private Synthesizer synthesizer = null;     // the synthesizer object, this is where soundbanks can be loaded
    private Soundbank soundbank = null;         // the soundbank that is used to synthesize the sounds
    private long playbackPositionInTicks = 0;   // this is used to pause the playback (store playback position, stop playback, later start at that position)

    /**
     * constructor
     */
    public MidiPlayer() throws MidiUnavailableException {
        this.initSynthesizer();
        this.initSequencer();
    }

    /**
     * initialize the synthesizer
     * @throws MidiUnavailableException
     */
    private void initSynthesizer() throws MidiUnavailableException {
        if (this.synthesizer == null) {
            this.synthesizer = MidiSystem.getSynthesizer();
        }

        if (!this.synthesizer.isOpen())
            this.synthesizer.open();

        this.soundbank = this.synthesizer.getDefaultSoundbank();    // the Java default soundbank is usually already loaded
    }

    /**
     * initialize the sequencer for Midi playback;
     * @return return true if successful, else false
     */
    private void initSequencer() throws MidiUnavailableException {
        if (this.sequencer == null)
            this.sequencer = MidiSystem.getSequencer(false);    // this obtains a Sequencer instance that is NOT! (that's the false parameter) connected to a default receiver device

        if (!this.sequencer.isOpen())
            this.sequencer.open();

        this.sequencer.getTransmitter().setReceiver(this.synthesizer.getReceiver());
    }

    /**
     * closes all activity of this MidiPlayer
     */
    public void close() {
        this.stop();
        this.sequencer.close();
        this.synthesizer.close();
    }

    /**
     * load a soundbank into a synthesizer for midi playback and audio rendering from a url
     * @param soundbankUrl
     * @return true (success), false (failed)
     */
    public boolean loadSoundbank(URL soundbankUrl) {
        if (this.synthesizer == null)
            return false;

        File soundbankFile;  // get the file behind the url

        try {
            soundbankFile = new File(URLDecoder.decode(soundbankUrl.getFile(), "UTF-8"));
        } catch (UnsupportedEncodingException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        return loadSoundbank(soundbankFile);    // return the soundbank object
    }

    /**
     * load a soundbank into a synthesizer for midi playback and audio rendering from a file
     *
     * @param soundbankFile
     * @return true (success), false (failed)
     */
    public boolean loadSoundbank(File soundbankFile) {
        if ((this.synthesizer == null) || (soundbankFile == null))
            return false;

        this.synthesizer.unloadAllInstruments(this.soundbank);
        this.soundbank = null;

        try {
            this.soundbank = MidiSystem.getSoundbank(soundbankFile);
        } catch (InvalidMidiDataException | IOException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        if (!this.synthesizer.isSoundbankSupported(this.soundbank)) {
            System.err.println("Soundbank not supported by synthesizer!");
            return false;
        }

        System.out.print("Loading soundbank " + this.soundbank.getName() + ": ");
        boolean b = this.synthesizer.loadAllInstruments(this.soundbank);
        System.out.println((b) ? "done" : "failed");

        return b;
    }

    /**
     * loads Java's default soundbank into the synthesizer
     * @return
     */
    public boolean loadDefaultSoundbank() {
        if (this.synthesizer == null)
            return false;

        this.synthesizer.unloadAllInstruments(this.soundbank);
        this.soundbank = this.synthesizer.getDefaultSoundbank();

        return this.synthesizer.loadAllInstruments(this.soundbank);
    }

    /**
     * a getter for the soundbank that is used for midi playback
     * @return
     */
    public Soundbank getSoundbank() {
        return this.soundbank;
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
     * a getter for the synthesizer
     * @return
     */
    public Synthesizer getSynthesizer() {
        return this.synthesizer;
    }

    /**
     * start playback at current playback position
     */
    public void play() {
        this.setTickPosition(this.playbackPositionInTicks);
        if (this.sequencer != null && !this.sequencer.isRunning())
            this.sequencer.start();
    }

    /**
     * start playback at current playback position
     * @param playbackPositionInTicks
     */
    public void play(long playbackPositionInTicks) {
        this.setTickPosition(playbackPositionInTicks);
        if (this.sequencer != null && !this.sequencer.isRunning())
            this.sequencer.start();
    }

    /**
     * start playback at current playback position
     * @param playbackPositionInMicroseconds
     */
    public void play(int playbackPositionInMicroseconds) {
        this.setMicrosecondPosition(playbackPositionInMicroseconds);
        if (this.sequencer != null && !this.sequencer.isRunning())
            this.sequencer.start();
    }

    /**
     * start playing the midi sequence
     * @param sequence midi sequence
     * @throws InvalidMidiDataException
     */
    public void play(Sequence sequence) throws InvalidMidiDataException {
        this.play(sequence, 0);
    }

    /**
     * start playing the midi sequence
     * @param midi the Midi instance to be played back
     * @throws InvalidMidiDataException
     */
    public void play(Midi midi) throws InvalidMidiDataException {
        this.play(midi.getSequence(), 0);
    }

    /**
     * start playing the midi sequence at the given relative position (between 0.0 and 1.0)
     * @param sequence midi sequence
     * @param relativePlaybackPosition relative playback position to start, should be in [0.0, 1.0]
     * @throws InvalidMidiDataException
     */
    public void play(Sequence sequence, double relativePlaybackPosition) throws InvalidMidiDataException {
        long startDate = (long)((double)sequence.getTickLength() * relativePlaybackPosition);
        this.play(sequence, startDate);
    }

    /**
     * start playing the midi sequence at the given relative position (between 0.0 and 1.0)
     * @param midi the Midi instance to be played back
     * @param relativePlaybackPosition relative playback position to start, should be in [0.0, 1.0]
     * @throws InvalidMidiDataException
     */
    public void play(Midi midi, double relativePlaybackPosition) throws InvalidMidiDataException {
        Sequence sequence = midi.getSequence();
        long startDate = (long)((double)sequence.getTickLength() * relativePlaybackPosition);
        this.play(sequence, startDate);
    }

    /**
     * start playing the midi sequence at the given relative position (between 0.0 and 1.0)
     * @param midi the Midi instance to be played back
     * @param playbackPositionInTicks this sets the tick position where to start the playback
     * @throws InvalidMidiDataException
     */
    public void play(Midi midi, long playbackPositionInTicks) throws InvalidMidiDataException {
        this.play(midi.getSequence(), playbackPositionInTicks);
    }

    /**
     * start playing the midi sequence
     * @param sequence midi sequence
     * @param playbackPositionInTicks this sets the tick position where to start the playback
     * @throws InvalidMidiDataException
     */
    public void play(Sequence sequence, long playbackPositionInTicks) throws InvalidMidiDataException {
        if (this.sequencer.isRunning())
            this.sequencer.stop();                              // stop it

        this.sequencer.setSequence(sequence);                   // assign the midi sequence to the sequencer

        if (playbackPositionInTicks >= sequence.getTickLength())// playback cannot run beyond the end of the sequence
            return;
        if (playbackPositionInTicks < 0)                        // playback cannot start before the beginning of the sequence
            playbackPositionInTicks = 0;

        this.playbackPositionInTicks = playbackPositionInTicks; // set playback position
        this.play();                                            // start playback
    }

    /**
     * pause the playback
     */
    public void pause() {
        if ((this.sequencer == null) || !this.sequencer.isOpen())
            return;

        this.playbackPositionInTicks = this.sequencer.getTickPosition();
        this.sequencer.stop();
    }

    /**
     * stop midi playback
     */
    public void stop() {
        if ((this.sequencer == null) || !this.sequencer.isOpen())
            return;

        this.sequencer.stop();
        this.playbackPositionInTicks = 0;
//        this.sequencer.close();   // causes slowdowns when restarting the playback because it has to be reopened
//        this.sequencer = null;
    }

    /**
     * indicates whether the playback/sequencer is currently running
     * @return
     */
    public boolean isPlaying() {
        if (this.sequencer != null)
            return this.sequencer.isRunning();
        return false;
    }

    /**
     * obtains the current position in the sequence, expressed in MIDI ticks
     * @return
     */
    public long getTickPosition() {
        if (this.sequencer != null)
            return this.sequencer.getTickPosition();
        return 0;
    }

    /**
     * obtains the current position in the sequence, expressed in micoroseconds
     * @return
     */
    public long getMicrosecondPosition() {
        if (this.sequencer != null)
            return this.sequencer.getMicrosecondPosition();
        return 0;
    }

    /**
     * obtains the current position in the sequence, expressed as relative value between 0.0 (beginning) and 1.0 (end)
     * @return
     */
    public double getRelativePosition() {
        if (this.sequencer != null)
            return (double)this.sequencer.getTickPosition() / (double)this.sequencer.getTickLength();
        return 0.0;
    }

    /**
     * a setter for the playback position
     * @param ticks
     */
    public void setTickPosition(long ticks) {
        this.playbackPositionInTicks = ticks;
        if (this.sequencer != null)
            this.sequencer.setTickPosition(this.playbackPositionInTicks);
    }

    /**
     * a setter for the playback position
     * @param microseconds
     */
    public void setMicrosecondPosition(long microseconds) {
        if (this.sequencer != null) {
            this.sequencer.setMicrosecondPosition(microseconds);
            this.playbackPositionInTicks = this.sequencer.getTickPosition();
        }
    }

    /**
     * a setter for the playback position
     * @param relativePosition
     */
    public void setRelativePosition(double relativePosition) {
        if ((this.sequencer == null) || (this.sequencer.getSequence() == null)) {
            this.playbackPositionInTicks = 0;
            return;
        }
        if (relativePosition <= 0.0)
            this.sequencer.setTickPosition(0);
        else if (relativePosition >= 1.0)
            this.sequencer.setTickPosition(this.sequencer.getTickLength());
        else
            this.sequencer.setTickPosition((long)((double)this.sequencer.getSequence().getTickLength() * relativePosition));

        this.playbackPositionInTicks = this.sequencer.getTickPosition();
    }
}
