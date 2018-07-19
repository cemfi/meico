package meico.audio;

import meico.audio.lame.lowlevel.LameDecoder;
import meico.audio.lame.lowlevel.LameEncoder;
import meico.audio.lame.mp3.Lame;
import meico.audio.lame.mp3.MPEGMode;
import meico.mei.Helper;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;

/**
 * This class represents audio data.
 * @author Axel Berndt.
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
     * constructor; use this one to load and decode MP3 files
     *
     * @param file
     */
    public Audio(File file) throws IOException, UnsupportedAudioFileException {
        String fileExtension = file.getName().substring(file.getName().lastIndexOf("."));   // get the file extension

        switch (fileExtension.toLowerCase()) {                                              // depending on the file extension the file has to be loaded in a different way
            case ".wav":                                                                    // this is for wav files
                AudioInputStream stream = loadWavFileToAudioInputStream(file);
                this.audio = convertAudioInputStream2ByteArray(stream);
                this.format = stream.getFormat();
                stream.close();
                break;
            case ".mp3":                                                                    // this is for mp3 files
                LameDecoder decoder = new LameDecoder(file.getCanonicalPath());
                throw new UnsupportedAudioFileException(file.getName().substring(file.getName().lastIndexOf(".")) + " import is not yet supported.");
//                InputStream inStream = new FileInputStream(file);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                byte[] buffer = new byte[8192];
//                int bytesRead;
//                while ((bytesRead = inStream.read(buffer)) > 0) {
//                    baos.write(buffer, 0, bytesRead);
//                }
//                this.audio = baos.toByteArray();
//                inStream.close();
//                break;
            default:                                                                        // this is for unsupported file formats/extensions
                throw new UnsupportedAudioFileException(file.getName().substring(file.getName().lastIndexOf(".")) + " is not supported.");
        }

        this.file = file;
    }

    /**
     * this constructor reads audio data from the AudioInputStream and associates the file with it;
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
    public static AudioInputStream loadWavFileToAudioInputStream(File file) throws IOException, UnsupportedAudioFileException {
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
     * @param pcm PCM data as byte array, this will be changes so be sure to call this method with a clone of the original if you want to keep the original
     * @param format audio format of PCM data
     * @return mp3 data as byte array
     */
    public byte[] encodePcmToMp3(byte[] pcm, AudioFormat format) {
        LameEncoder encoder = new LameEncoder(format, 256, MPEGMode.STEREO, Lame.QUALITY_HIGH, true);   // bitrate is 256; in this case VBR (=variable bitrate) is true

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
     * returns audio data of this object as byte array MP3 encoded, the original data (this.getAudio() or this.audio) stay unaltered
     * @return byte array of MP3 encoded audio data
     */
    public byte[] getAudioAsMp3() {
        return this.encodePcmToMp3(this.audio.clone(), this.format);
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
     * @return true if success, false if an error occurred
     */
    public boolean writeAudio() {
        return this.writeAudio(this.file);
    }

    /**
     * writes the audioStream into a file
     * @param filename
     * @return true if success, false if an error occurred
     */
    public boolean writeAudio(String filename) {
        File file = new File(filename);     // create the file with this filename
        file.getParentFile().mkdirs();                              // ensure that the directory exists
        return this.writeAudio(file);              // write into it
    }

    /**
     * write the audio data to the file system
     * @param file
     * @return true if success, false if an error occurred
     */
    public boolean writeAudio(File file) {
        if (file == null) {                                                 // if no valid file
            System.err.println("No file specified to write audio data.");   // print error message
            return false;                                                         // cancel
        }

        if (this.file == null)                                              // if no file has been specified, yet
            this.file = file;                                               // take this

        try {
            file.createNewFile();                                           // create the file if it does not already exist
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
        AudioInputStream ais = convertByteArray2AudioInputStream(this.audio, this.format);  // convert the audio byte array to an AudioInputStream
        try {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);                            // write to file system
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * write audio data as MP3 to the file system
     * @return true if success, false if an error occurred
     */
    public boolean writeMp3() {
        return this.writeMp3(Helper.getFilenameWithoutExtension(this.file.getAbsolutePath()) + ".mp3");
    }

    /**
     * write audio data as MP3 to the file system with specified filename
     * @param filename
     * @return true if success, false if an error occurred
     */
    public boolean writeMp3(String filename) {
        File file = new File(filename);     // create the file with this filename
        file.getParentFile().mkdirs();      // ensure that the directory exists
        return this.writeMp3(file);         // write into it
    }

    /**
     * write audio data as MP3 to the file system to specified file
     * @param file
     * @return true if success, false if an error occurred
     */
    public boolean writeMp3(File file) {
        if (file == null) {                                                 // if no valid file
            System.err.println("No file specified to write audio data.");   // print error message
            return false;                                                         // cancel
        }

        file.getParentFile().mkdirs();                              // ensure that the directory exists
        try {
            file.createNewFile();                                   // create the file if it does not already exist
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        byte[] mp3 = this.getAudioAsMp3();                                  // convert PCM encoded audio to MP3 encoding
        try {
            Files.write(file.toPath(), mp3);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
