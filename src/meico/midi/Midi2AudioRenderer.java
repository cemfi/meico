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
 * This class is based on the Midi2WavRenderer class Karl Helgason and, thus, inherits the following copyright notice.
 *
 * Copyright (c) 2007 by Karl Helgason
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created by Axel Berndt on 19.09.2016.
 **/
public class Midi2AudioRenderer {
    private Synthesizer synth;       // the Synthesizer object, used for midi to wav conversion

    /**
     * constuctor
     *
     * @throws MidiUnavailableException
     */
    public Midi2AudioRenderer() throws MidiUnavailableException {
        this.synth = MidiSystem.getSynthesizer();
    }

    /**
     * load a soundbank into a synthesizer for midi playback and audio rendering from a url
     * @param soundbankUrl
     * @param synth
     * @return the soundbank from the url or (if something went wrong with it) the default soundbank
     */
    public static Soundbank loadSoundbank(URL soundbankUrl, Synthesizer synth) {
        if (soundbankUrl == null)
            return synth.getDefaultSoundbank();

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
        if (soundbankFile == null)
            return synth.getDefaultSoundbank();

        Soundbank soundbank;
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
     * @param soundbankURL
     * @return
     * @throws MidiUnavailableException
     */
    public AudioInputStream renderMidi2Audio(Sequence sequence, URL soundbankURL) throws MidiUnavailableException {
        Soundbank soundbank = loadSoundbank(soundbankURL, this.synth);
        return this.renderMidi2Audio(sequence, soundbank, 44100, 16, 2);
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
        Soundbank soundbank = loadSoundbank(soundbankFile, this.synth);
        return this.renderMidi2Audio(sequence, soundbank, 44100, 16, 2);
    }

    /**
     * creates an AudioInputStream based on the sequence
     *
     * @param sequence
     * @param soundbank
     * @param sampleRate
     * @param sampleSizeInBits
     * @param channels
     * @return
     * @throws MidiUnavailableException
     */
    public AudioInputStream renderMidi2Audio(Sequence sequence, Soundbank soundbank, float sampleRate, int sampleSizeInBits, int channels) throws MidiUnavailableException {
//        AudioSynthesizer synth = this.findAudioSynthesizer();
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
