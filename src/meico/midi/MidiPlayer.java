package meico.midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * This class provides Midi playback functinality.
 * @author Axel Berndt
 */
public class MidiPlayer {
    private Sequencer sequencer = null;         // a sequencer to playback midi sequences
    private Synthesizer synthesizer = null;     // the synthesizer object, this is where soundbanks can be loaded
    private MidiDevice midiDevice = null;       // the music may be output to another receiver instead of the synthesizer
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
     * constructor
     * @param midi
     * @throws MidiUnavailableException
     */
    public MidiPlayer(Midi midi) throws MidiUnavailableException {
        this();
        try {
            this.sequencer.setSequence(midi.getSequence());
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    /**
     * initialize the synthesizer
     * @throws MidiUnavailableException
     */
    private void initSynthesizer() throws MidiUnavailableException {
        if (this.synthesizer == null) {
            this.synthesizer = MidiSystem.getSynthesizer();
            this.midiDevice = this.synthesizer;
        }

        this.soundbank = this.synthesizer.getDefaultSoundbank();    // the Java default soundbank is usually already loaded

        if (!this.synthesizer.isOpen()) {
            this.synthesizer.open();                                // This may sometimes produce a Java WARNING message, esp. on Windows when you run meico in an IDE. It can be ignored. To get rid of it, run your IDE in admin mode.
        }
    }

    /**
     * initialize the sequencer for Midi playback;
     */
    private void initSequencer() throws MidiUnavailableException {
        if (this.sequencer == null)
            this.sequencer = MidiSystem.getSequencer(false);    // this obtains a Sequencer instance that is NOT! (that's the false parameter) connected to a default receiver device

        if (!this.sequencer.isOpen())
            this.sequencer.open();

        this.sequencer.getTransmitter().setReceiver(this.midiDevice.getReceiver());
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
    public synchronized boolean loadSoundbank(URL soundbankUrl) {
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
    public synchronized boolean loadSoundbank(File soundbankFile) {
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
    public synchronized boolean loadDefaultSoundbank() {
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
    public synchronized Soundbank getSoundbank() {
        return this.soundbank;
    }

    /**
     * a getter for the sequencer object
     *
     * @return
     */
    public synchronized Sequencer getSequencer() {
        return this.sequencer;
    }

    /**
     * a sequencer setter
     * @param sequencer
     */
    public synchronized void setSequencer(Sequencer sequencer) {
        this.sequencer = sequencer;
    }

    /**
     * a getter for the synthesizer
     * @return
     */
    public synchronized Synthesizer getSynthesizer() {
        return this.synthesizer;
    }

    /**
     * switch the MIDI output to another output port
     * @param midiDevice
     * @return success
     */
    public synchronized boolean setMidiOutputPort(MidiDevice midiDevice) {
        boolean success = true;

        if (this.midiDevice == midiDevice) {                                             // the requested MIDI device is already served
            return success;
        }

        long playbackPosition = this.sequencer.getTickPosition();
        boolean wasPlaying = this.sequencer.isRunning();
        Sequence sequence = this.sequencer.getSequence();
        this.sequencer.close();

//        for (Receiver receiver : this.sequencer.getReceivers())                         // close all receivers that are currently open
//            receiver.close();
        if (this.midiDevice != this.synthesizer)                                        // we should not close the synthesizer as it would forget the soundfont it might have loaded
            this.midiDevice.close();                                                    // any other device should be closed to free its resources

        if ((midiDevice == null) || (midiDevice instanceof Sequencer) || (midiDevice.getMaxReceivers() == 0)) { // if the output port is invalid
            this.midiDevice = this.synthesizer;                                         // use the synthesizer as default instead
            success = false;
        } else {
            this.midiDevice = midiDevice;
        }

        try {
            if (!this.midiDevice.isOpen()) {
                this.midiDevice.open();
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            this.midiDevice = this.synthesizer;                                         // use the synthesizer as default instead
            success = false;
        }

        try {
            this.sequencer.getTransmitter().setReceiver(this.midiDevice.getReceiver()); // assign the new port to the sequencer
            this.sequencer.open();                                                      // reopen the sequencer
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            return false;
        }

        if (sequence != null) {
            try {
                this.sequencer.setSequence(sequence);
                this.sequencer.setTickPosition(playbackPosition);
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
                wasPlaying = false;
            }
        }

        if (wasPlaying)
            this.sequencer.start();

        return success;
    }

    /**
     * start playback at current playback position
     */
    public synchronized void play() {
        this.setTickPosition(this.playbackPositionInTicks);
        if ((this.sequencer != null) && (this.sequencer.getSequence() != null) && !this.sequencer.isRunning())
            this.sequencer.start();
    }

    /**
     * start playback at current playback position
     * @param playbackPositionInTicks
     */
    public synchronized void play(long playbackPositionInTicks) {
        this.setTickPosition(playbackPositionInTicks);
        if ((this.sequencer != null) && (this.sequencer.getSequence() != null) && !this.sequencer.isRunning())
            this.sequencer.start();
    }

    /**
     * start playback at current playback position
     * @param playbackPositionInMicroseconds
     */
    public synchronized void play(int playbackPositionInMicroseconds) {
        this.setMicrosecondPosition(playbackPositionInMicroseconds);
        if ((this.sequencer != null) && (this.sequencer.getSequence() != null) && !this.sequencer.isRunning())
            this.sequencer.start();
    }

    /**
     * start playing the midi sequence
     * @param sequence midi sequence
     * @throws InvalidMidiDataException
     */
    public synchronized void play(Sequence sequence) throws InvalidMidiDataException {
        this.play(sequence, 0);
    }

    /**
     * start playing the midi sequence
     * @param midi the Midi instance to be played back
     * @throws InvalidMidiDataException
     */
    public synchronized void play(Midi midi) throws InvalidMidiDataException {
        this.play(midi.getSequence(), 0);
    }

    /**
     * start playing the midi sequence at the given relative position (between 0.0 and 1.0)
     * @param sequence midi sequence
     * @param relativePlaybackPosition relative playback position to start, should be in [0.0, 1.0]
     * @throws InvalidMidiDataException
     */
    public synchronized void play(Sequence sequence, double relativePlaybackPosition) throws InvalidMidiDataException {
        long startDate = (long)((double)sequence.getTickLength() * relativePlaybackPosition);
        this.play(sequence, startDate);
    }

    /**
     * start playing the midi sequence at the given relative position (between 0.0 and 1.0)
     * @param midi the Midi instance to be played back
     * @param relativePlaybackPosition relative playback position to start, should be in [0.0, 1.0]
     * @throws InvalidMidiDataException
     */
    public synchronized void play(Midi midi, double relativePlaybackPosition) throws InvalidMidiDataException {
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
    public synchronized void play(Midi midi, long playbackPositionInTicks) throws InvalidMidiDataException {
        this.play(midi.getSequence(), playbackPositionInTicks);
    }

    /**
     * start playing the midi sequence
     * @param sequence midi sequence
     * @param playbackPositionInTicks this sets the tick position where to start the playback
     * @throws InvalidMidiDataException
     */
    public synchronized void play(Sequence sequence, long playbackPositionInTicks) throws InvalidMidiDataException {
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
    public synchronized void pause() {
        if ((this.sequencer == null) || !this.sequencer.isOpen())
            return;

        this.playbackPositionInTicks = this.sequencer.getTickPosition();
        this.sequencer.stop();
    }

    /**
     * stop midi playback
     */
    public synchronized void stop() {
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
    public synchronized boolean isPlaying() {
        if (this.sequencer != null)
            return this.sequencer.isRunning();
        return false;
    }

    /**
     * obtains the current position in the sequence, expressed in MIDI ticks
     * @return
     */
    public synchronized long getTickPosition() {
        if (this.sequencer != null)
            return this.sequencer.getTickPosition();
        return 0;
    }

    /**
     * obtains the current position in the sequence, expressed in micoroseconds
     * @return
     */
    public synchronized long getMicrosecondPosition() {
        if (this.sequencer != null)
            return this.sequencer.getMicrosecondPosition();
        return 0;
    }

    /**
     * obtains the current position in the sequence, expressed as relative value between 0.0 (beginning) and 1.0 (end)
     * @return
     */
    public synchronized double getRelativePosition() {
        if (this.sequencer != null)
            return (double)this.sequencer.getTickPosition() / (double)this.sequencer.getTickLength();
        return 0.0;
    }

    /**
     * a setter for the playback position
     * @param ticks
     */
    public synchronized void setTickPosition(long ticks) {
        this.playbackPositionInTicks = ticks;
        if (this.sequencer != null)
            this.sequencer.setTickPosition(this.playbackPositionInTicks);
    }

    /**
     * a setter for the playback position
     * @param microseconds
     */
    public synchronized void setMicrosecondPosition(long microseconds) {
        if (this.sequencer != null) {
            this.sequencer.setMicrosecondPosition(microseconds);
            this.playbackPositionInTicks = this.sequencer.getTickPosition();
        }
    }

    /**
     * a setter for the playback position (0.0 start, 1.0 end)
     * @param relativePosition
     */
    public synchronized void setRelativePosition(double relativePosition) {
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

    /**
     * returns the length of the midi sequence in microseconds or 0 if none is loaded
     * @return
     */
    public synchronized long getMicrosecondLength() {
        if (this.sequencer != null)
            return this.sequencer.getMicrosecondLength();
        return 0;
    }

    /**
     * returns the length of the midi sequence in ticks or 0 if none is loaded
     * @return
     */
    public synchronized long getTickLength() {
        if (this.sequencer != null)
            return this.sequencer.getTickLength();
        return 0;
    }
}
