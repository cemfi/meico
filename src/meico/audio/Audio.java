package meico.audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * This is only a rudimentary class that holds audio data. It will be elaborated further in the future.
 * Created by aberndt on 19.09.2016.
 */
public class Audio {
    private File file = null;                       // the audio file
    private byte[] audio;                           // the audio data
    private AudioFormat format = null;              // audio format data

    /**
     * constructor, generates empty instance
     */
    public Audio() {
        this.audio = new byte[0];                   // initialize an empty array
    }

    /**
     * constructor with AudioInputStream
     *
     * @param inputStream
     */
    public Audio(AudioInputStream inputStream) {
        this.audio = convertAudioInputStream2ByteArray(inputStream);
        this.format = inputStream.getFormat();
    }

    /**
     * constructor
     *
     * @param file
     */
    public Audio(File file) throws IOException, UnsupportedAudioFileException {
        AudioInputStream stream = loadFileToAudioInputStream(file);
        this.audio = convertAudioInputStream2ByteArray(stream);
        this.format = stream.getFormat();
        this.file = file;
    }

    /**
     * this constructor reades audio data from the AudioInputStream and associates the file with it;
     * the file may differ from the input stream
     * @param inputStream
     * @param file
     */
    public Audio(AudioInputStream inputStream, File file) {
        this.audio = convertAudioInputStream2ByteArray(inputStream);
        this.format = inputStream.getFormat();
        this.file = file;
    }

    /**
     * with this constructor all data is given explicitly
     * @param audioData
     * @param format
     * @param file
     */
    public Audio(byte[] audioData, AudioFormat format, File file) {
        this.audio = audioData;
        this.format = format;
        this.file = file;
    }

    /**
     * loads the audio file into an AudioInputStream
     * @param file
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public static AudioInputStream loadFileToAudioInputStream(File file) throws IOException, UnsupportedAudioFileException {
        return AudioSystem.getAudioInputStream(file);
    }

    /**
     * convert an AudioInputStream to a byte array
     * @param stream
     * @return
     */
    public static byte[] convertAudioInputStream2ByteArray(AudioInputStream stream) {
        byte[] array;
        try {
            array = new byte[(int)(stream.getFrameLength() * stream.getFormat().getFrameSize())];   // initialize the byte array with the length of the stream
            stream.read(array);         // write the stream's bytes into the byte array
        } catch (IOException e) {       // in case of an IOException
            e.printStackTrace();        // output error
            return new byte[0];         // return empty array
        }
        return array;
    }

    /**
     * convert a byte array (without audio file header, just pure audio data) into an AudioInputStream in a given AudioFormat
     * @param array
     * @param format
     * @return
     */
    public static AudioInputStream convertByteArray2AudioInputStream(byte[] array, AudioFormat format) {
        ByteArrayInputStream bis = new ByteArrayInputStream(array);             // byte array to ByteArrayInputStream
        AudioInputStream ais = new AudioInputStream(bis, format, array.length); // byteArrayInputStream to AudioInputStream
        return ais;                                                             // return it
    }

    /**
     * check if there is data in the audio byte array
     * @return
     */
    public boolean isEmpty() {
        return ((this.audio == null) || (this.audio.length == 0));
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
     * a getter for the audio data
     * @return
     */
    public byte[] getAudio() {
        return this.audio;
    }

    public AudioFormat getFormat() {
        return this.format;
    }

    /**
     * a getter for the sample rate
     * @return
     */
    public float getSampleRate() {
        return this.format.getSampleRate();
    }

    /**
     * a getter for the sample size in bits
     * @return
     */
    public int getSampleSizeInBits() {
        return this.format.getSampleSizeInBits();
    }

    /**
     * a getter for the frame size
     * @return
     */
    public int getFrameSize() {
        return this.format.getFrameSize();
    }

    /**
     * a getter for the frame rate
     * @return
     */
    public float getFrameRate() {
        return this.format.getFrameRate();
    }

    /**
     * a getter for the number of channels
     * @return
     */
    public int getChannels() {
        return this.format.getChannels();
    }

    /**
     * a getter for the encoding
     * @return
     */
    public AudioFormat.Encoding getEncoding() {
        return this.format.getEncoding();
    }

    /**
     * a getter for the bigEndian
     * @return
     */
    public boolean isBigEndian() {
        return this.format.isBigEndian();
    }

    /**
     * (over-)write the file with the data in audioStream
     * @throws IOException
     */
    public void writeAudio() throws IOException {
        this.writeAudio(this.file);
    }

    /**
     * writes the audioStream into a file
     * @param filename
     */
    public void writeAudio(String filename) throws IOException {
        File file = new File(filename);     // create the file with this filename
        this.writeAudio(file);              // write into it
    }

    /**
     * write the audio data to the file system
     * @param file
     * @throws IOException
     */
    public void writeAudio(File file) throws IOException {
        if (file == null) {                                                 // if no valid file
            System.err.println("No file specified to write audio data.");   // print error message
            return;                                                         // cancel
        }

        if (this.file == null)                                              // if no file has been specified, yet
            this.file = file;                                               // take this

        AudioInputStream ais = convertByteArray2AudioInputStream(this.audio, this.format);  // convert the audio byte array to an AudioInputStream
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);                            // write to file system
    }
}
