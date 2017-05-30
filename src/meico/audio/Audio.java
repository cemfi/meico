package meico.audio;

import meico.audio.lame.lowlevel.LameEncoder;
import meico.audio.lame.mp3.Lame;
import meico.audio.lame.mp3.MPEGMode;
import meico.mei.Helper;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This class represents audio data.
 * @author Axel Berndt.
 */
public class Audio {
    private File file = null;                       // the audio file
    private byte[] audio;                           // the audio data
    private AudioFormat format = null;              // audio format data

    // playback data used in methods play() and stop()
    private Clip audioClip = null;
    private AudioInputStream audioInputStream = null;

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
        stream.close();
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
     * convert PCM encoded audio to MP3 encoding
     * @param pcm PCM data as byte array
     * @param format audio format of PCM data
     * @return mp3 data as byte array
     */
    public byte[] encodePcmToMp3(byte[] pcm, AudioFormat format) {
        LameEncoder encoder = new LameEncoder(format, 256, MPEGMode.STEREO, Lame.QUALITY_HIGHEST, true);   // bitrate is 256; in this case VBR (=variable bitrate) is true

        ByteArrayOutputStream mp3 = new ByteArrayOutputStream();
        byte[] buffer = new byte[encoder.getPCMBufferSize()];

        int bytesToTransfer = Math.min(buffer.length, pcm.length);
        int bytesWritten;
        int currentPcmPosition = 0;
        while (0 < (bytesWritten = encoder.encodeBuffer(pcm, currentPcmPosition, bytesToTransfer, buffer))) {
            currentPcmPosition += bytesToTransfer;
            bytesToTransfer = Math.min(buffer.length, pcm.length - currentPcmPosition);
            mp3.write(buffer, 0, bytesWritten);
        }

        encoder.close();
        return mp3.toByteArray();
    }

    /**
     * returns audio data of this object as byte array MP3 encoded
     * @return byte array of MP3 encoded audio data
     */
    public byte[] getAudioAsMp3() {
        return this.encodePcmToMp3(this.audio, this.format);
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
     * is the audioClip playing?
     * @return
     */
    public boolean isPlaying() {
        if (this.audioClip == null) return false;
        return this.audioClip.isRunning();
    }

    public Clip getAudioClip() {
        return this.audioClip;
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

    /**
     * write audio data as MP3 to the file system
     * @throws IOException
     */
    public void writeMp3() throws IOException {
        this.writeMp3(Helper.getFilenameWithoutExtension(this.file.getAbsolutePath()) + ".mp3");
    }

    /**
     * write audio data as MP3 to the file system with specified filename
     * @param filename
     */
    public void writeMp3(String filename) throws IOException {
        File file = new File(filename);     // create the file with this filename
        this.writeMp3(file);                // write into it
    }

    /**
     * write audio data as MP3 to the file system to specified file
     * @param file
     * @throws IOException
     */
    public void writeMp3(File file) throws IOException {
        if (file == null) {                                                 // if no valid file
            System.err.println("No file specified to write audio data.");   // print error message
            return;                                                         // cancel
        }

        byte[] mp3 = this.getAudioAsMp3();                                  // convert PCM encoded audio to MP3 encoding
        Files.write(file.toPath(), mp3);
    }

    /**
     * start playing back the audio data
     *
     * @throws LineUnavailableException
     * @throws IOException
     */
    public void play() throws LineUnavailableException, IOException {
        if (this.isEmpty()) return;
        this.stop();

        DataLine.Info info = new DataLine.Info(Clip.class, this.format);
        this.audioClip = (Clip) AudioSystem.getLine(info);
        this.audioInputStream = convertByteArray2AudioInputStream(this.audio, this.format);
        this.audioClip.open(this.audioInputStream);
        this.audioClip.start();
    }

    /**
     * stop audio playback
     */
    public void stop() {
        if (this.audioClip != null) {
            if (this.audioClip.isRunning()) this.audioClip.stop();
            if (this.audioClip.isOpen()) this.audioClip.close();
            this.audioClip = null;
        }

        if (this.audioInputStream != null) {
            try {
                this.audioInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.audioInputStream = null;
        }
    }
}
