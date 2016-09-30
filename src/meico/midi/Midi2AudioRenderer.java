package meico.midi;

import com.sun.media.sound.AudioSynthesizer;

import javax.sound.midi.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * The code in class is based on class MidiToWavRenderer.java from http://www.jfugue.org/4/download.html (last access: Sept. 2016).
 * It is an add-on to the JFugue library which is distributed under LGPL license for all development purposes.
 *
 * Created by Axel Berndt on 19.09.2016.
 */
public class Midi2AudioRenderer {
    private Synthesizer synth = null;       // the synthesizer object, used for midi to wav conversion

    /**
     * constuctor
     *
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public Midi2AudioRenderer() throws MidiUnavailableException, InvalidMidiDataException, IOException {
        this.synth = MidiSystem.getSynthesizer();
    }

    /**
     * load a soundbank into a synthesizer for midi playback and audio rendering from a url
     * @param soundbankUrl
     * @param synth
     * @return the soundbank from the url or (if something went wrong with it) the default soundbank
     */
    public static Soundbank loadSoundbank(URL soundbankUrl, Synthesizer synth) {
        File soundbankFile;  // get the file behind the url
        try {
            soundbankFile = new File(URLDecoder.decode(soundbankUrl.getFile(), "UTF-8"));
        } catch (UnsupportedEncodingException | NullPointerException e) {
            e.printStackTrace();
            return synth.getDefaultSoundbank();
        }
        return loadSoundbank(soundbankFile, synth);    // return the soundbank object
    }

    /**
     * load a soundbank into a synthesizer for midi playback and audio rendering from a file
     *
     * @param soundbankFile
     * @param synth
     * @return the soundbank from the file or (if something went wrong with it) the default soundbank
     */
    public static Soundbank loadSoundbank(File soundbankFile, Synthesizer synth) {
        Soundbank soundbank;

        if (soundbankFile == null)
            return synth.getDefaultSoundbank();

        try {
            soundbank = MidiSystem.getSoundbank(soundbankFile);
        } catch (InvalidMidiDataException | IOException | NullPointerException e) {
            e.printStackTrace();
            return synth.getDefaultSoundbank();
        }

        if (!synth.isSoundbankSupported(soundbank)) {
//            throw new UnsupportedSoundbankException("Soundbank not supported by synthesizer!");
            System.err.println("Soundbank not supported by synthesizer!");
            return synth.getDefaultSoundbank();
        }
        return soundbank;
    }

    /**
     * creates an AudioInputStream based on the sequence and uses the standard soundbank for synthesis
     *
     * @param sequence
     * @throws MidiUnavailableException
     */
    public AudioInputStream renderMidi2Audio(Sequence sequence) throws MidiUnavailableException {
        return this.renderMidi2Audio(sequence, null, 44100, 16, 2);
    }

    /**
     * creates an AudioInputStream based on the sequence and uses the given soundbank for synthesis
     *
     * @param sequence
     * @param soundbankFile
     * @return
     * @throws MidiUnavailableException
     */
    public AudioInputStream renderMidi2Audio(Sequence sequence, File soundbankFile) throws MidiUnavailableException {
        return this.renderMidi2Audio(sequence, soundbankFile, 44100, 16, 2);
    }

    /**
     * creates an AudioInputStream based on the sequence
     *
     * @param sequence
     * @param soundbankFile can be a valid URL or null
     * @param sampleRate
     * @param sampleSizeInBits
     * @param channels
     * @return
     * @throws MidiUnavailableException
     */
    public AudioInputStream renderMidi2Audio(Sequence sequence, File soundbankFile, float sampleRate, int sampleSizeInBits, int channels) throws MidiUnavailableException {
        Soundbank soundbank = loadSoundbank(soundbankFile, this.synth);

        AudioSynthesizer synth = this.findAudioSynthesizer();
        if (synth == null) {
            System.err.println("No AudioSynthesizer was found!");
            return null;
        }

        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, true, false);
        Map<String, Object> p = new HashMap<String, Object>();
        p.put("interpolation", "sinc");
        p.put("max polyphony", "1024");
        AudioInputStream stream = synth.openStream(format, p);

        if (soundbank != null) {
            synth.unloadAllInstruments(synth.getDefaultSoundbank());
            synth.loadAllInstruments(soundbank);
        }

        // Play Sequence into AudioSynthesizer Receiver.
        double total = send(sequence, synth.getReceiver());

        // Calculate how long the WAVE file needs to be.
        long len = (long) (stream.getFormat().getFrameRate() * (total + 4));
        stream = new AudioInputStream(stream, stream.getFormat(), len);

//        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, new File("temp.wav"));

        this.synth.close();

        return stream;
    }

    /**
     * Find available AudioSynthesizer.
     * @return
     * @throws MidiUnavailableException
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

        return null;        // No AudioSynthesizer was found, return null.
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
