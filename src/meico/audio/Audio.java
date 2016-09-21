package meico.audio;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * This is only a rudimentary class that holds audio data. It will be elaborated further in the future.
 * Created by aberndt on 19.09.2016.
 */
public class Audio {
    private File file = null;                       // the audio file
    private AudioInputStream audioStream = null;    // audio data in stream

    /**
     * constructor, generates empty instance
     */
    public Audio() {
    }

    /**
     * constructor with AudioInputStream
     *
     * @param inputStream
     */
    public Audio(AudioInputStream inputStream) {
       this.audioStream = inputStream;
    }

    /**
     * constructor
     *
     * @param audioFile
     */
    public Audio(File audioFile) throws IOException, UnsupportedAudioFileException {
        this.loadFile(audioFile);
    }

    /**
     * constructor with AudioInputStream for the audio data and file object for potential storing to file system
     * @param inputStream
     * @param file
     */
    public Audio(AudioInputStream inputStream, File file) {
        this.audioStream = inputStream;
        this.file = file;
    }

    /**
     * a setter for the audio stream
     * @param stream
     */
    public void setAudioStream(AudioInputStream stream) {
        this.audioStream = stream;
    }

    /**
     * a getter for the audio stream
     * @return
     */
    public AudioInputStream getAudioStream() {
        return this.audioStream;
    }

    /**
     * loads the audio file into an AudioInputStream
     * @param audioFile
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public void loadFile(File audioFile) throws IOException, UnsupportedAudioFileException {
        this.file = audioFile;
        this.audioStream = AudioSystem.getAudioInputStream(audioFile);
    }

    /**
     * a setter for the file object
     * @param file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * a getter for the file
     * @return
     */
    public File getFile() {
        return this.file;
    }

    /**
     * (over-)write the file with the data in audioStream
     * @throws IOException
     */
    public void writeAudio() throws IOException {
        if (this.file == null) {
            System.out.println("Cannot write to the file system. Path and filename required.");
            return;
        }
        AudioSystem.write(this.audioStream, AudioFileFormat.Type.WAVE, this.file);
    }

    /**
     * writes the audioStream into a file
     * @param filename
     */
    public void writeAudio(String filename) throws IOException {
        this.file = new File(filename);
        this.writeAudio();
    }

    public void writeAudio(File file) {

    }

    /**
     * check if there is data in the file object
     * @return
     */
    public boolean isEmpty() {
        return (this.audioStream == null);
    }
}
