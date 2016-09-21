package meico.midi;

import com.sun.media.sound.AudioSynthesizer;

import javax.sound.midi.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The code in class is based on class MidiToWavRenderer.java from http://www.jfugue.org/4/download.html (last access: Sept. 2016).
 * It is an add-on to the JFugue library which is distributed under LGPL license for all development purposes.
 *
 * Created by Axel Berndt on 19.09.2016.
 */
public class Midi2WavRenderer {
    private Synthesizer synth = null;       // the synthesizer object, used for midi to wav conversion

    /**
     * constuctor
     *
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public Midi2WavRenderer() throws MidiUnavailableException, InvalidMidiDataException, IOException {
        this.synth = MidiSystem.getSynthesizer();
    }

    /**
     * load a soundbank for midi playback and wave rendering
     *
     * @param soundbankFile
     * @return
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    private Soundbank loadSoundbank(File soundbankFile) throws MidiUnavailableException, InvalidMidiDataException, IOException, UnsupportedSoundbankException {
        Soundbank soundbank = MidiSystem.getSoundbank(soundbankFile);
        if (!this.synth.isSoundbankSupported(soundbank)) {
            throw new UnsupportedSoundbankException("Soundbank not supported by synthesizer!");
        }
        return soundbank;
    }

    /**
     * Creates a WAV file based on the Sequence, using the sounds from the specified soundbank;
     * to prevent memory problems, this method asks for an array of patches (instruments) to load.
     *
     * @param soundbankFile
     * @param patches an array of Patch objects, each specifying the bank index and program change number
     * @param sequence
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public void renderMidi2Wave(File soundbankFile, int[] patches, Sequence sequence) throws MidiUnavailableException, InvalidMidiDataException, IOException, UnsupportedSoundbankException {
        Soundbank soundbank = loadSoundbank(soundbankFile);         // Load soundbank

        // Open the Synthesizer and load the requested instruments
        this.synth.open();
        this.synth.unloadAllInstruments(soundbank);
        for (int patch : patches) {
            this.synth.loadInstrument(soundbank.getInstrument(new Patch(0, patch)));
        }

        renderMidi2Wave(sequence);
    }

    /**
     * Creates a WAV file based on the Sequence, using the default soundbank.
     *
     * @param sequence
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public AudioInputStream renderMidi2Wave(Sequence sequence) throws MidiUnavailableException, InvalidMidiDataException, IOException {
        AudioSynthesizer synth = findAudioSynthesizer();
        if (synth == null) {
            System.err.println("No AudioSynthesizer was found!");
            return null;
        }

        AudioFormat format = new AudioFormat(96000, 24, 2, true, false);
        Map<String, Object> p = new HashMap<String, Object>();
        p.put("interpolation", "sinc");
        p.put("max polyphony", "1024");
        AudioInputStream stream = synth.openStream(format, p);

        // Play Sequence into AudioSynthesizer Receiver.
        double total = send(sequence, synth.getReceiver());

        // Calculate how long the WAVE file needs to be.
        long len = (long) (stream.getFormat().getFrameRate() * (total + 4));
        stream = new AudioInputStream(stream, stream.getFormat(), len);

        this.synth.close();

        return stream;
    }

    /**
     * Find available AudioSynthesizer.
     */
    private AudioSynthesizer findAudioSynthesizer() throws MidiUnavailableException {
        // First check if default synthesizer is AudioSynthesizer.
        Synthesizer synth = MidiSystem.getSynthesizer();
        if (synth instanceof AudioSynthesizer) {
            return (AudioSynthesizer)synth;
        }

        // If default synthesizer is not AudioSynthesizer, check others.
        MidiDevice.Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info aMidiDeviceInfo : midiDeviceInfo) {
            MidiDevice dev = MidiSystem.getMidiDevice(aMidiDeviceInfo);
            if (dev instanceof AudioSynthesizer) {
                return (AudioSynthesizer) dev;
            }
        }

        // No AudioSynthesizer was found, return null.
        return null;
    }

    /**
     * Send entry MIDI Sequence into Receiver using timestamps.
     */
    private double send(Sequence seq, Receiver recv) {
        float divtype = seq.getDivisionType();
        assert (seq.getDivisionType() == Sequence.PPQ);
        Track[] tracks = seq.getTracks();
        int[] trackspos = new int[tracks.length];
        int mpq = 500000;
        int seqres = seq.getResolution();
        long lasttick = 0;
        long curtime = 0;
        while (true) {
            MidiEvent selevent = null;
            int seltrack = -1;
            for (int i = 0; i < tracks.length; i++) {
                int trackpos = trackspos[i];
                Track track = tracks[i];
                if (trackpos < track.size()) {
                    MidiEvent event = track.get(trackpos);
                    if ((selevent == null) || (event.getTick() < selevent.getTick())) {
                        selevent = event;
                        seltrack = i;
                    }
                }
            }
            if (seltrack == -1)
                break;
            trackspos[seltrack]++;
            long tick = selevent.getTick();
            if (divtype == Sequence.PPQ)
                curtime += ((tick - lasttick) * mpq) / seqres;
            else
                curtime = (long) ((tick * 1000000.0 * divtype) / seqres);
            lasttick = tick;
            MidiMessage msg = selevent.getMessage();
            if (msg instanceof MetaMessage) {
                if (divtype == Sequence.PPQ) {
                    if (((MetaMessage) msg).getType() == 0x51) {
                        byte[] data = ((MetaMessage) msg).getData();
                        mpq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                    }
                }
            }
            else {
                if (recv != null)
                    recv.send(msg, curtime);
            }
        }
        return curtime / 1000000.0;
    }
}
