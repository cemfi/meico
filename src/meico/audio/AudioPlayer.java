package meico.audio;

import javax.sound.sampled.*;

/**
 * This class provides audio playback functionality.
 * @author Axel Berndt
 */
public class AudioPlayer {
    private Clip audioClip = null;                  // here comes the data to be played back
    private int playbackPositionInFrames = 0;       // this is used to pause the playback (store playback position, stop playback, later start at that position)
    private boolean isPlaying = false;              // is set true when playback is started, even when the audioClip did not yet send data (audioClip.isActive() and audioClip.isRunning() would still return false)
    private LineListener playbackListener = null;  // its job is to set isPlaying false when playback ends

    /**
     * constructor
     */
    public AudioPlayer() {
        // the playback listener will keep an eye on audioClip and set isPlaying accordingly
        this.playbackListener = new LineListener() {
            @Override
            public synchronized void update(LineEvent event) {
                LineEvent.Type eventType = event.getType();
                if (eventType == LineEvent.Type.START) {
                    isPlaying = true;
                }
                if ((eventType == LineEvent.Type.STOP) || (eventType == LineEvent.Type.CLOSE)) {
                    isPlaying = false;
                    if (getFramePosition() >= getAudioClip().getFrameLength()) {    // only if playback reached the end of the track
                        getAudioClip().setMicrosecondPosition(0);                   // playbackposition is set to start
                        playbackPositionInFrames = 0;                               // playbackposition is set to start
                    }
                }
            }
        };
    }

    /**
     * is the audioClip playing?
     * @return
     */
    public synchronized boolean isPlaying() {
        if (this.getAudioClip() == null)
            return false;
        return this.isPlaying;
    }

    /**
     * a getter for the audio clip
     * @return true for success, else false
     */
    public synchronized Clip getAudioClip() {
        return this.audioClip;
    }

    /**
     * a setter for the audio clip, can be used to load audio data without playing it back
     * @param audioClip
     * @return true for success, else false
     */
    public synchronized boolean setAudioData(Clip audioClip) {
        this.audioClip = audioClip;
        return (this.audioClip != null);
    }

    /**
     * a setter for the audio clip, can be used to load audio data without playing it back
     * @param audio
     * @return true for success, else false
     */
    public synchronized boolean setAudioData(Audio audio) {
        if (audio == null) return false;
        return this.setAudioData(audio.getAudio(), audio.getFormat());
    }

    /**
     * a setter for the audio clip, can be used to load audio data without playing it back
     * @param pcmAudio
     * @param format
     * @return true for success, else false
     */
    public synchronized boolean setAudioData(byte[] pcmAudio, AudioFormat format) {
        if ((pcmAudio == null) || (format == null))
            return false;

        if (this.isPlaying())
            this.getAudioClip().stop();

        try {
            if (this.audioClip == null)
                this.audioClip = AudioSystem.getClip();
            this.audioClip.open(format, pcmAudio, 0, pcmAudio.length);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            this.audioClip = null;
            return false;
        }

        this.audioClip.addLineListener(this.playbackListener);     // add the listener to keep this.isPlaying up to date
        return true;
    }

    /**
     * Given that the AudioPlayer has already loaded audio data (i.e. an audio clip), this can be used to start playback.
     * The playback will start at the beginning or at a specified position (via setPlaybackPosition() or pause()).
     */
    public synchronized void play() {
        if (this.audioClip == null) {
//            this.isPlaying = false;
            return;
        }
        this.audioClip.setFramePosition(this.playbackPositionInFrames);
        this.audioClip.start();
        this.isPlaying = true;
    }

    /**
     * Given that the AudioPlayer has already loaded audio data (i.e. an audio clip), this can be used to start playback.
     * The playback will start at the beginning or at a specified position.
     * @param playbackPositionInFrames
     */
    public synchronized void play(int playbackPositionInFrames) {
        this.playbackPositionInFrames = playbackPositionInFrames;
        this.play();
    }

    /**
     * Given that the AudioPlayer has already loaded audio data (i.e. an audio clip), this can be used to start playback.
     * The playback will start at the beginning or at a specified position.
     * @param relativePlaybackPosition
     */
    public synchronized void play(double relativePlaybackPosition) {
        if (this.getAudioClip() == null)
            return;
        if (relativePlaybackPosition >= 1.0)
            return;
        this.playbackPositionInFrames = (relativePlaybackPosition <= 0.0) ? 0 : (int)(this.audioClip.getFrameLength() * relativePlaybackPosition);
        this.play();
    }

    /**
     * start playing back the given audio data
     * @param audio
     */
    public synchronized void play(Audio audio) {
        this.stop();
        if (this.setAudioData(audio))
            this.play();
    }

    /**
     * start playing back the given audio data
     * @param audio
     * @param relativePlaybackPosition a relative playback position in [0.0-1.0) to start
     */
    public synchronized void play(Audio audio, double relativePlaybackPosition) {
        this.stop();
        if (!this.setAudioData(audio) || relativePlaybackPosition >= 1.0)
            return;
        this.playbackPositionInFrames = (relativePlaybackPosition <= 0.0) ? 0 : (int)(this.audioClip.getFrameLength() * relativePlaybackPosition);
        this.play();
    }

    /**
     * start playing back the given audio data
     * @param pcmAudio PCM encoded audio data in a byte array
     * @param format audio format information
     * @param relativePlaybackPosition a relative playback position in [0.0-1.0) to start
     */
    public synchronized void play(byte[] pcmAudio, AudioFormat format, double relativePlaybackPosition) {
        this.stop();
        if (!this.setAudioData(pcmAudio, format) || (relativePlaybackPosition >= 1.0))
            return;
        this.play(relativePlaybackPosition);
    }

    /**
     * pause playback, i.e. keep audio data buffered (in audioClip) and store the playback position
     */
    public synchronized void pause() {
        if (!this.isPlaying()) {
//            this.isPlaying = false;
            return;
        }
        this.getAudioClip().stop();
        this.playbackPositionInFrames = this.getAudioClip().getFramePosition();
//        this.isPlaying = false;
    }

    /**
     * stop audio playback, audio data is deleted from the buffer and has to be reloaded
     */
    public synchronized void stop() {
        if (this.getAudioClip() == null) {
//            this.isPlaying = false;
            return;
        }

        if (this.getAudioClip().isActive())
            this.getAudioClip().stop();
        if (this.getAudioClip().isOpen())
            this.getAudioClip().close();

        this.audioClip.removeLineListener(this.playbackListener);
        this.audioClip = null;
        this.playbackPositionInFrames = 0;
//        this.isPlaying = false;
    }

    /**
     * returns the frame count of the audio clip or 0 if none is loaded
     * @return
     */
    public synchronized int getFrameLength() {
        if (this.getAudioClip() == null)
            return 0;
        return this.getAudioClip().getFrameLength();
    }

    /**
     * returns the length of the audio clip in microseconds or 0 if none is loaded
     * @return
     */
    public synchronized long getMicrosecondLength() {
        if (this.getAudioClip() == null)
            return 0;
        return this.getAudioClip().getMicrosecondLength();
    }

    /**
     * obtains the current playback position in micoroseconds
     * @return
     */
    public synchronized long getMicrosecondPosition() {
        if (this.getAudioClip() == null)
            return 0;
        return this.getAudioClip().getMicrosecondPosition();
    }

    /**
     * obtains the current playback position in micoroseconds
     * @return
     */
    public synchronized long getFramePosition() {
        if (this.getAudioClip() != null)
            return this.getAudioClip().getFramePosition();
        return 0;
    }

    /**
     * obtains the current playback position, expressed as relative value between 0.0 (beginning) and 1.0 (end)
     * @return
     */
    public synchronized double getRelativePosition() {
        if (this.getAudioClip() != null)
            return (double)this.getAudioClip().getFramePosition() / (double)this.getAudioClip().getFrameLength();
        return 0.0;
    }

    /**
     * a setter for the playback position
     * @param microseconds
     */
    public synchronized void setMicrosecondPosition(long microseconds) {
        if (this.getAudioClip() == null) {
            this.playbackPositionInFrames = (int) ((this.getAudioClip().getFormat().getSampleRate()) / 1000000 * microseconds);
            return;
        }
        this.playbackPositionInFrames = (int)((double)(microseconds * this.getAudioClip().getFrameLength()) / this.getAudioClip().getMicrosecondLength());
        this.getAudioClip().setFramePosition(this.playbackPositionInFrames);
    }

    /**
     * a setter for the playback position
     * @param frames
     */
    public synchronized void setFramePosition(int frames) {
        this.playbackPositionInFrames = frames;
        if (this.getAudioClip() != null)
            this.getAudioClip().setFramePosition(this.playbackPositionInFrames);
    }

    /**
     * a setter for the playback position
     * @param relativePosition vallues in [0.0, 1.0)
     */
    public synchronized void setRelativePlaybackPosition(double relativePosition) {
        if (this.getAudioClip() == null) {
            this.playbackPositionInFrames = 0;
            return;
        }
        if (relativePosition >= 1.0)
            this.playbackPositionInFrames = this.getAudioClip().getFrameLength();
        else if (relativePosition <= 0.0)
            this.playbackPositionInFrames = 0;
        else
            this.playbackPositionInFrames = (int)(this.getAudioClip().getFrameLength() * relativePosition);
        this.getAudioClip().setFramePosition(this.playbackPositionInFrames);
    }
}
