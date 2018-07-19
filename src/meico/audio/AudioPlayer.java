package meico.audio;

import javax.sound.sampled.*;

public class AudioPlayer {
    private Clip audioClip = null;              // here comes the data to be played back
    private int playbackPositionInFrames = 0;   // this is used to pause the playback (store playback position, stop playback, later start at that position)

    /**
     * constructor
     */
    public AudioPlayer() {
    }

    /**
     * is the audioClip playing?
     * @return
     */
    public boolean isPlaying() {
        if (this.audioClip == null)
            return false;
        return this.audioClip.isRunning();
    }

    /**
     * a getter for the audio clip
     * @return true for success, else false
     */
    public Clip getAudioClip() {
        return this.audioClip;
    }

    /**
     * a setter for the audio clip, can be used to load audio data without playing it back
     * @param audioClip
     * @return true for success, else false
     */
    public boolean setAudioData(Clip audioClip) {
        this.audioClip = audioClip;
        return (this.audioClip != null);
    }

    /**
     * a setter for the audio clip, can be used to load audio data without playing it back
     * @param audio
     * @return true for success, else false
     */
    public boolean setAudioData(Audio audio) {
        if (audio == null) return false;
        return this.setAudioData(audio.getAudio(), audio.getFormat());
    }

    /**
     * a setter for the audio clip, can be used to load audio data without playing it back
     * @param pcmAudio
     * @param format
     * @return true for success, else false
     */
    public boolean setAudioData(byte[] pcmAudio, AudioFormat format) {
        if ((pcmAudio == null) || (format == null))
            return false;

        try {
            this.audioClip = AudioSystem.getClip();
            this.audioClip.open(format, pcmAudio, 0, pcmAudio.length);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            this.audioClip = null;
            return false;
        }
        return true;
    }

    /**
     * Given that the AudioPlayer has already loaded audio data (i.e. an audio clip), this can be used to start playback.
     * The playback will start at the beginning or at a specified position (via setPlaybackPosition() or pause()).
     */
    public void play() {
        if (this.getAudioClip() == null)
            return;

        this.audioClip.setFramePosition(this.playbackPositionInFrames);

        if (!this.getAudioClip().isRunning())
            this.audioClip.start();
    }

    /**
     * Given that the AudioPlayer has already loaded audio data (i.e. an audio clip), this can be used to start playback.
     * The playback will start at the beginning or at a specified position.
     * @param playbackPositionInFrames
     */
    public void play(int playbackPositionInFrames) {
        this.playbackPositionInFrames = playbackPositionInFrames;
        this.play();
    }

    /**
     * Given that the AudioPlayer has already loaded audio data (i.e. an audio clip), this can be used to start playback.
     * The playback will start at the beginning or at a specified position.
     * @param relativePlaybackPosition
     */
    public void play(double relativePlaybackPosition) {
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
    public void play(Audio audio) {
        this.stop();
        if (this.setAudioData(audio))
            this.play();
    }

    /**
     * start playing back the given audio data
     * @param audio
     * @param relativePlaybackPosition a relative playback position in [0.0-1.0) to start
     */
    public void play(Audio audio, double relativePlaybackPosition) {
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
    public void play(byte[] pcmAudio, AudioFormat format, double relativePlaybackPosition) {
        this.stop();
        if (!this.setAudioData(pcmAudio, format) || (relativePlaybackPosition >= 1.0))
            return;
        this.play(relativePlaybackPosition);
    }

    /**
     * pause playback, i.e. keep audio data buffered (in audioClip) and store the playback position
     */
    public void pause() {
        if ((this.getAudioClip() == null) || !this.getAudioClip().isRunning())
            return;
        this.playbackPositionInFrames = this.getAudioClip().getFramePosition();
        this.getAudioClip().stop();
    }

    /**
     * stop audio playback, audio data is deleted from the buffer and has to be reloaded
     */
    public void stop() {
        if (this.getAudioClip() != null) {
            if (this.getAudioClip().isRunning()) this.getAudioClip().stop();
            if (this.getAudioClip().isOpen()) this.getAudioClip().close();
            this.audioClip = null;
            this.playbackPositionInFrames = 0;
        }
    }

    /**
     * returns the frame count of the audio clip or 0 if none is loaded
     * @return
     */
    public int getFrameLength() {
        if (this.getAudioClip() == null)
            return 0;
        return this.getAudioClip().getFrameLength();
    }

    /**
     * returns the length of the audio clip in microseconds or 0 if none is loaded
     * @return
     */
    public long getMicrosecondLength() {
        if (this.getAudioClip() == null)
            return 0;
        return this.getAudioClip().getMicrosecondLength();
    }

    /**
     * obtains the current playback position in micoroseconds
     * @return
     */
    public long getMicrosecondPosition() {
        if (this.getAudioClip() == null)
            return 0;
        return this.getAudioClip().getMicrosecondPosition();
    }

    /**
     * obtains the current playback position in micoroseconds
     * @return
     */
    public long getFramePosition() {
        if (this.getAudioClip() != null)
            return this.getAudioClip().getFramePosition();
        return 0;
    }

    /**
     * obtains the current playback position, expressed as relative value between 0.0 (beginning) and 1.0 (end)
     * @return
     */
    public double getRelativePosition() {
        if (this.getAudioClip() != null)
            return (double)this.getAudioClip().getFramePosition() / (double)this.getAudioClip().getFrameLength();
        return 0.0;
    }

    /**
     * a setter for the playback position
     * @param microseconds
     */
    public void setMicrosecondPosition(long microseconds) {
        if (this.getAudioClip() == null) {
            this.playbackPositionInFrames = (int) (0.0441 * microseconds);
            return;
        }
        this.playbackPositionInFrames = (int)((double)(microseconds * this.getAudioClip().getFrameLength()) / this.getAudioClip().getMicrosecondLength());
        this.getAudioClip().setFramePosition(this.playbackPositionInFrames);
    }

    /**
     * a setter for the playback position
     * @param frames
     */
    public void setFramePosition(int frames) {
        this.playbackPositionInFrames = frames;
        if (this.getAudioClip() != null)
            this.getAudioClip().setFramePosition(this.playbackPositionInFrames);
    }

    /**
     * a setter for the playback position
     * @param relativePosition vallues in [0.0, 1.0)
     */
    public void setRelativePlaybackPosition(double relativePosition) {
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
