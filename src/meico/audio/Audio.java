package meico.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.SignalPipeline;
import com.tagtraum.jipes.SignalPump;
import com.tagtraum.jipes.audio.*;
import com.tagtraum.jipes.math.WindowFunction;
import com.tagtraum.jipes.universal.Mapping;
import meico.mei.Helper;
import meico.supplementary.ColorCoding;
import meico.supplementary.KeyValue;
import net.sourceforge.lame.lowlevel.LameDecoder;
import net.sourceforge.lame.lowlevel.LameEncoder;
import net.sourceforge.lame.mp3.*;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class represents audio data.
 * @author Axel Berndt.
 */
public class Audio {
    private static final String MP3 = "mp3";
    private static final String WAVE = "wav";

    private File file = null;                       // the audio file
    private byte[] audio;                           // the audio data
    private AudioFormat format = null;              // audio format data
    private String fileType = null;                 // the file format, e.g. "mp3" or "wav"

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
        String fileExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);   // get the file extension

        switch (fileExtension.toLowerCase()) {                                              // depending on the file extension the file has to be loaded in a different way
            case "wav":                                                                    // this is for wav files
                AudioInputStream stream = loadWavFileToAudioInputStream(file);
                this.audio = convertAudioInputStream2ByteArray(stream);
                this.format = stream.getFormat();
                stream.close();
                this.file = file;
                this.fileType = Audio.WAVE;
                break;
            case "mp3":                                                                    // this is for mp3 files
                KeyValue<AudioFormat, byte[]> decoded = decodeMp3ToPcm(file);
                this.format = decoded.getKey();
                this.audio = decoded.getValue();
//                this.file = new File(Helper.getFilenameWithoutExtension(file.getCanonicalPath()) + ".wav"); // we have pcm data, so we should make sure it is not stored as mp3
                this.file = file;
                this.fileType = Audio.MP3;
                break;
            default:                                                                        // this is for unsupported file formats/extensions
                throw new UnsupportedAudioFileException(fileExtension.toLowerCase() + " is not supported.");
        }
    }

    /**
     * this constructor reads audio data from the AudioInputStream and associates the file with it;
     * the file may differ from the input stream
     *
     * @param inputStream
     * @param file
     */
    public Audio(AudioInputStream inputStream, File file) {
        this.audio = convertAudioInputStream2ByteArray(inputStream);
        this.format = inputStream.getFormat();
        this.file = file;
        this.fileType = file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }

    /**
     * with this constructor all data is given explicitly
     *
     * @param audioData
     * @param format
     * @param file
     */
    public Audio(byte[] audioData, AudioFormat format, File file) {
        this.audio = audioData;
        this.format = format;
        this.file = file;
        this.fileType = file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }

    /**
     * loads the audio file into an AudioInputStream
     *
     * @param file
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public static AudioInputStream loadWavFileToAudioInputStream(File file) throws IOException, UnsupportedAudioFileException {
        return AudioSystem.getAudioInputStream(file);
    }

    /**
     * this method decodes an mp3 file to pcm
     *
     * @param file
     * @return tuplet with (AudioFormat, pcm byte array)
     */
    private static KeyValue<AudioFormat, byte[]> decodeMp3ToPcm(File file) throws IOException {
        LameDecoder decoder = new LameDecoder(file.getCanonicalPath());
        ByteBuffer buffer = ByteBuffer.allocate(1152 * 2 * decoder.getChannels());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while (decoder.decode(buffer))
            byteArrayOutputStream.write(buffer.array());

        byte[] pcmByteArray = byteArrayOutputStream.toByteArray();
        AudioFormat format = new AudioFormat(decoder.getSampleRate(), 16, decoder.getChannels(), true, false);

        decoder.close();

        return new KeyValue<>(format, pcmByteArray);
    }

    /**
     * convert an AudioInputStream to a byte array
     *
     * @param stream
     * @return
     */
    public static byte[] convertAudioInputStream2ByteArray(AudioInputStream stream) {
        byte[] array;
        try {
            array = new byte[(int) (stream.getFrameLength() * stream.getFormat().getFrameSize())];   // initialize the byte array with the length of the stream
            stream.read(array);         // write the stream's bytes into the byte array
        } catch (IOException e) {       // in case of an IOException
            e.printStackTrace();        // output error
            return new byte[0];         // return empty array
        }
        return array;
    }

    /**
     * convert a byte array (without audio file header, just pure audio data) into an AudioInputStream in a given AudioFormat
     *
     * @param array
     * @param format
     * @return
     */
    public static AudioInputStream convertByteArray2AudioInputStream(byte[] array, AudioFormat format) {
        ByteArrayInputStream bis = new ByteArrayInputStream(array);                 // byte array to ByteArrayInputStream
        AudioInputStream ais = new AudioInputStream(bis, format, (array.length / (2L * format.getChannels()))); // byteArrayInputStream to AudioInputStream
        return ais;                                                                 // return it
    }

    /**
     * This can be used to convert the byte array of an Audio object into an array of doubles between -1.0 and 1.0
     * which is far more convenient for audio analyses.
     *
     * @param array
     * @param format
     * @return an ArrayList of double arrays, each is an audio channel (stereo sequence is [left, right])
     */
    public static ArrayList<double[]> convertByteArray2DoubleArray(byte[] array, AudioFormat format) {
        ArrayList<double[]> channelList = new ArrayList<>();
        double maxVal = Math.pow(2, format.getSampleSizeInBits()) / 2.0;      //for 16 bit = 32768.0; division by 2.0 is for shifting the range to half positive, half negative

        int c2 = 2 * format.getChannels();
        int oneChanArrayLength = array.length / c2;
        for (int channel = 0; channel < format.getChannels(); ++channel) {
            int a = channel * 2;
            double[] output = new double[oneChanArrayLength];
            for (int i = 0; i < oneChanArrayLength; ++i) {
                int c2ia = c2 * i + a;
                output[i] = ((short) (((array[c2ia + 1] & 0xFF) << 8) | (array[c2ia] & 0xFF))) / maxVal;    // we assume little endian
            }
            channelList.add(output);
        }

//        // little endian, mono
//        if (format.getChannels() == 1) {        // mono
//            double[] output = new double[array.length / 2];
//            for (int i = 0; i < (array.length / 2); i++)
//                output[i] = ((short) (((array[2 * i + 1] & 0xFF) << 8) | (array[2 * i] & 0xFF))) / maxVal;
//            channelList.add(output);
//        }
//
//        // little endian, stereo, output is the sum of both channels
//        else if (format.getChannels() == 2) {   // stereo
//            double[] outputLeft = new double[array.length / 4];
//            double[] outputRight = new double[array.length / 4];
//            for (int i = 0; i < (array.length / 4); i++) {
//                outputLeft[i] = ((short) (((array[4 * i + 1] & 0xFF) << 8) | (array[4 * i] & 0xFF))) / maxVal;
//                outputRight[i] = ((short) (((array[4 * i + 3] & 0xFF) << 8) | (array[4 * i + 2] & 0xFF))) / maxVal;
//            }
//            channelList.add(outputLeft);
//            channelList.add(outputRight);
//        }
//
//        // if the audio format is inappropriate
//        else {
//            System.err.println("This audio has " + format.getChannels() + " channels. Only mono and stereo audio are supported.");
//        }

        return channelList;
    }

    /**
     * This method converts an input double array into a byte array.
     *
     * @param array
     * @param sampleSizeInBits this will mostly be 16; the value can be retrieved from an Audio object via audio.getFormat().getSampleSizeInBits()
     * @return
     */
    public static byte[] convertDoubleArray2ByteArray(double[] array, int sampleSizeInBits) {
        double maxVal = Math.pow(2, sampleSizeInBits) / 2.0;

        // assumes signed PCM, little endian
        byte[] output = new byte[2 * array.length];
        for (int i = 0; i < array.length; i++) {
            int b = (array[i] == 1.0) ? Short.MAX_VALUE : (short) (array[i] * maxVal);
            output[2 * i] = (byte) b;
            output[2 * i + 1] = (byte) (b >> 8);      // little endian
        }
        return output;
    }

    /**
     * Make a waveform image from the audio data.
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @return a BufferedImage instance
     */
    public BufferedImage exportWaveformImage(int width, int height) {
        ArrayList<double[]> channels = Audio.convertByteArray2DoubleArray(this.getAudio(), this.getFormat());
        ArrayList<BufferedImage> waveforms = new ArrayList<>();
        int heightSubdivision = (int) Math.floor((float) height / channels.size());                     // the pixel height of the sub-images
        int maxWidth = 0;

        // make a horizontal slice of the image for each channel
        for (double[] channel : channels) {
            BufferedImage waveform = Audio.convertWaveform2Image(channel, 0, channel.length-1, width, heightSubdivision);   // draw the waveform in the image
            waveforms.add(waveform);
            if (waveform.getWidth() > maxWidth)
                maxWidth = waveform.getWidth();
        }

        // write the waveform images into this.waveform
        BufferedImage waveform = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);          // we start with an empty image, all black
        for (int waveformNum = 0; waveformNum < waveforms.size(); ++waveformNum) {
            BufferedImage w = waveforms.get(waveformNum);
            int yOffset = waveformNum * heightSubdivision;                                              // the y pixel offset when writing the waveform into this.waveform
            for (int y = 0; y < w.getHeight(); ++y) {                                                   // for each pixel row
                for (int x = 0; x < w.getWidth(); ++x) {                                                // go through each pixel
                    waveform.setRGB(x, y + yOffset, w.getRGB(x, y));                                    // set the pixel color
                }
            }
        }

        return waveform;
    }

    /**
     * Make a waveform image from the input audio date.
     * @param audio the audio amplitude data normalized to [-1.0, 1.0]
     * @param leftmostSample where in the audio data should we start
     * @param rightmostSample where in the audio will we end
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @return the waveform image or null
     */
    public static BufferedImage convertWaveform2Image(double[] audio, int leftmostSample, int rightmostSample, int width, int height) {
        int sampleCount = rightmostSample - leftmostSample;     // how many samples are to be displayed in the panel frame
        float sample2xScaleFactor = (sampleCount > 0) ? (float) (width - 1) / sampleCount : 1;  // we need this value several times
        double[][] maxValues = new double[width][2];            // this array collects the max and min sample values to be rendered into a pixel column
        boolean[] isSet = new boolean[width];                   // this is to keep track of whether we have specific values for this pixel column (true) or should use the previous column's value (false), this is necessary when a sample stretches over several columns

        // compute the min and max amplitude values for each pixel column
        for (int i = 0; i < sampleCount; ++i) {
            int x = Math.round(sample2xScaleFactor * i);
            isSet[x] = true;
            int sampleIndex = leftmostSample + i;
            if (maxValues[x][0] < audio[sampleIndex])
                maxValues[x][0] = audio[sampleIndex];
            if (maxValues[x][1] > audio[sampleIndex])
                maxValues[x][1] = audio[sampleIndex];
        }

        // draw the waveform in a BufferedImage instance
        BufferedImage waveform = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);  // we start with an empty image, all black
        double yTranslationFactor = -0.5 * waveform.getHeight();                                // this value is needed several times to scale and translate the sample values (in [-1.0, 1.0]) to vertical pixel coordinates (in [waveform.getHeight(), 0])
        int yPositive = (int) Math.round(-yTranslationFactor);                                  // initial value corresponds with amplitude value 0.0
        int yNegative = yPositive;
        for (int x = 0; x < width; ++x) {                                                       // for each pixel column
            waveform.setRGB(x, yPositive, Color.DARK_GRAY.getRGB());                            // draw a dark gray center line

            // scale and translate the values to vertical pixel coordinates
            if (isSet[x]) {                                                                     // if we have a sample value, otherwise we use the vertical pixel coordinates of the previous column
                yPositive = (int) Math.round((maxValues[x][0] * yTranslationFactor) - yTranslationFactor);
                yNegative = (int) Math.round((maxValues[x][1] * yTranslationFactor) - yTranslationFactor);
            }

            // color the pixels from the lowest to highest value
            for (int y = yPositive; y < yNegative; ++y)
                waveform.setRGB(x, y, Color.WHITE.getRGB());
        }

        return waveform;
    }

    /**
     * Compute the CQT spectrogram with default values.
     *
     * @return
     */
    public synchronized ArrayList<LogFrequencySpectrum> exportConstantQTransformSpectrogram() throws IOException {
        return this.exportConstantQTransformSpectrogram(new WindowFunction.Hamming(2048), 1024, 20.0f, 10000.0f, 6);
    }

    /**
     * This computes a Contant Q Transform spectrogram and returns it as array of CQT slices.
     *
     * @param windowFunction
     * @param hopSize
     * @param minFrequency
     * @param maxFrequency
     * @param binsPerSemitone
     * @return
     * @throws IOException
     */
    public synchronized ArrayList<LogFrequencySpectrum> exportConstantQTransformSpectrogram(WindowFunction windowFunction, int hopSize, float minFrequency, float maxFrequency, int binsPerSemitone) throws IOException {
        return this.exportConstantQTransformSpectrogram(windowFunction, hopSize, minFrequency, maxFrequency, binsPerSemitone, new SignalPump<>());
    }

    /**
     * This computes a Contant Q Transform spectrogram and returns it as array of CQT slices.
     *
     * @param windowFunction
     * @param hopSize
     * @param minFrequency
     * @param maxFrequency
     * @param binsPerSemitone
     * @param pump In other dsp frameworks the pump might be called dispatcher, it delivers the audio frames. The application can provide its own pump. Via the pump the application can cancel the processing which is useful in multithreaded environments.
     * @return
     */
    public synchronized ArrayList<LogFrequencySpectrum> exportConstantQTransformSpectrogram(WindowFunction windowFunction, int hopSize, float minFrequency, float maxFrequency, int binsPerSemitone, SignalPump<AudioBuffer> pump) throws IOException {
        long startTime = System.currentTimeMillis();                    // we measure the time that the conversion consumes
        System.out.println("\nComputing CQT spectrogram (window: " + windowFunction + ", hop size: " + hopSize + ", min freq: " + minFrequency + ", max freq: " + maxFrequency + ", bins per semitone: " + binsPerSemitone + ").");

        SignalPipeline<AudioBuffer, LogFrequencySpectrum> cqtPipeline = new SignalPipeline<>(
                new Mono(),                                             // if there are more than one channel, reduce them to mono
                new SlidingWindow(windowFunction.getLength(), hopSize),
                new Mapping<AudioBuffer>(AudioBufferFunctions.createMapFunction(windowFunction)),
                new ConstantQTransform(minFrequency, maxFrequency, 12 * binsPerSemitone),
                new AbstractSignalProcessor<LogFrequencySpectrum, ArrayList<LogFrequencySpectrum>>("specID") {  // aggregate the CQTs to a spectrum with id "specID" (needed to access it in the results)
                    private final ArrayList<LogFrequencySpectrum> spectrogram = new ArrayList<>();

                    @Override
                    protected ArrayList<LogFrequencySpectrum> processNext(LogFrequencySpectrum input) throws IOException {
                        this.spectrogram.add(input);
                        return this.spectrogram;
                    }
                }
        );

        AudioSignalSource source = new AudioSignalSource(Audio.convertByteArray2AudioInputStream(this.getAudio(), this.getFormat()));
        pump.setSignalSource(source);                                   // in other dsp frameworks the pump might be called dispatcher, it delivers the audio frames
        pump.add(cqtPipeline);
        Map<Object, Object> results = pump.pump();

        System.out.println("Computing CQT spectrogram finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return (ArrayList<LogFrequencySpectrum>) results.get("specID");
    }

    /**
     * A convenient helper method to convert a spectrogram (as obtained by, e.g., method exportConstantQTransformSpectrogram()) into a BufferedImage.
     * @param spectrogram
     * @return
     */
    public static BufferedImage convertSpectrogramToImage(ArrayList<LogFrequencySpectrum> spectrogram) {
        return convertSpectrogramToImage(spectrogram, true, 0.1f, new ColorCoding(ColorCoding.INFERNO));
    }

    /**
     * A convenient helper method to convert a spectrogram (as obtained by, e.g., method exportConstantQTransformSpectrogram()) into a BufferedImage.
     * Input values are normalized.
     * @param spectrogram
     * @param normalize set true to normalize the spectrogram values for image rendering (the spectrogram remains unaltered)
     * @param gamma 1.0f corresponds to the normalized input values with no gamma changes
     * @param colorCoding class ColorCoding offers some constants for easy instantiation, e.g. new ColorCoding(ColorCoding.INFERNO)
     * @return
     */
    public static BufferedImage convertSpectrogramToImage(ArrayList<LogFrequencySpectrum> spectrogram, boolean normalize, float gamma, ColorCoding colorCoding) {
        if (spectrogram.isEmpty())                                              // make sure we have a non-empty spectrogram
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);         // if we don't, return a one pixel black image

        ArrayList<LogFrequencySpectrum> spectrogramCopy = spectrogram;

        if (normalize) {
            // first, normalize the array values (scale it so the highest value is 1.0)
            // find highest value, so we know how the amount to scale up, because that is also the scale ratio (max = 1.0)
            float highest = 0.0f;
            for (LogFrequencySpectrum spectrum : spectrogramCopy) {
                for (float binValue : spectrum.getData()) {
                    if (binValue > highest)
                        highest = binValue;
                }
            }

            if (highest == 0.0f)                                            // trivial case, just return a black image
                return new BufferedImage(spectrogramCopy.size(), spectrogramCopy.get(0).getData().length, BufferedImage.TYPE_INT_RGB);

            // we need to change the spectrum values, but we do it in the copy, not the original spectrogram
            spectrogramCopy = new ArrayList<>();
            for (LogFrequencySpectrum spectrum : spectrogram) {
                try {
                    spectrogramCopy.add((LogFrequencySpectrum) spectrum.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }

            // normalize the values
            if (highest != 1.0f) {                                          // only if necessary, just a little optimization
                for (LogFrequencySpectrum spectrum : spectrogramCopy) {
                    for (int y = 0; y < spectrum.getData().length; ++y) {
                        spectrum.getData()[y] /= highest;
                    }
                }
            }
        }

        // create the pixel array
        BufferedImage image = new BufferedImage(spectrogramCopy.size(), spectrogramCopy.get(0).getData().length, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < spectrogramCopy.size(); ++x) {
            for (int y = 0; y < spectrogramCopy.get(x).getData().length; ++y) {
                float value = (float) Math.pow(spectrogramCopy.get(x).getData()[y], gamma);         // apply gamma correction
//                Color color = new Color(value, value, value);                                   // create a gray color
                Color color = colorCoding.getColor(value);                                      // get the color for the value
                image.setRGB(x, -y + spectrogramCopy.get(x).getData().length - 1, color.getRGB());  // set the pixel's color
            }
        }

        // in case we want to save the image to a file, do this
//        try {
//            ImageIO.write(image, "png", new File("spectrogram.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return image;
    }

    /**
     * convert PCM encoded audio to MP3 encoding
     * @param pcm PCM data as byte array, this will be changes so be sure to call this method with a clone of the original if you want to keep the original
     * @param format audio format of PCM data
     * @return mp3 data as byte array
     */
    public synchronized byte[] encodePcmToMp3(byte[] pcm, AudioFormat format) {
        LameEncoder encoder = new LameEncoder(format, 256, MPEGMode.STEREO, Lame.QUALITY_HIGH, false);   // bitrate is 256; in this case VBR (=variable bitrate) is false

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
    public synchronized boolean isEmpty() {
        return ((this.audio == null) || (this.audio.length == 0));
    }

    /**
     * a setter for the file object
     * @param file
     */
    public synchronized void setFile(File file) {
        this.file = file;
        this.fileType = file.getName().substring(file.getName().lastIndexOf(".") + 1);

    }

    /**
     * a getter for the file
     * @return
     */
    public synchronized File getFile() {
        return this.file;
    }

    /**
     * a getter for the audio data
     * @return
     */
    public synchronized byte[] getAudio() {
        return this.audio;
    }

    public synchronized AudioFormat getFormat() {
        return this.format;
    }

    /**
     * a getter for the sample rate
     * @return
     */
    public synchronized float getSampleRate() {
        return this.format.getSampleRate();
    }

    /**
     * a getter for the sample size in bits
     * @return
     */
    public synchronized int getSampleSizeInBits() {
        return this.format.getSampleSizeInBits();
    }

    /**
     * a getter for the frame size
     * @return
     */
    public synchronized int getFrameSize() {
        return this.format.getFrameSize();
    }

    /**
     * a getter for the frame rate
     * @return
     */
    public synchronized float getFrameRate() {
        return this.format.getFrameRate();
    }

    /**
     * a getter for the number of channels
     * @return
     */
    public synchronized int getChannels() {
        return this.format.getChannels();
    }

    /**
     * a getter for the encoding
     * @return
     */
    public synchronized AudioFormat.Encoding getEncoding() {
        return this.format.getEncoding();
    }

    /**
     * a getter for the bigEndian
     * @return
     */
    public synchronized boolean isBigEndian() {
        return this.format.isBigEndian();
    }

    /**
     * (over-)write the file with the data in audioStream
     * @return true if success, false if an error occurred
     */
    public boolean writeAudio() {
        if (this.fileType.equals(Audio.WAVE))
            return this.writeAudio(this.file);

        // if this was initially no wave file (e.g. mp3 instead) we should change the file extension so we do not store a wave file as ".mp3"
        String filename;
        try {
            filename = Helper.getFilenameWithoutExtension(this.file.getCanonicalPath()) + ".wav";
        } catch (IOException e) {
            System.err.println("No file specified to write audio data.");   // print error message
            return false;                                                   // cancel
        }
        return this.writeAudio(filename);
    }

    /**
     * writes the audioStream into a file,
     * this will generate a wave file even if you give it another extension
     * @param filename
     * @return true if success, false if an error occurred
     */
    public synchronized boolean writeAudio(String filename) {
        File file = new File(filename);     // create the file with this filename
        file.getParentFile().mkdirs();      // ensure that the directory exists
        return this.writeAudio(file);       // write into it
    }

    /**
     * write the audio data to the file system,
     * this will generate a wave file even if you give it another extension
     * @param file
     * @return true if success, false if an error occurred
     */
    public synchronized boolean writeAudio(File file) {
        if (file == null) {                                                 // if no valid file
            System.err.println("No file specified to write audio data.");   // print error message
            return false;                                                   // cancel
        }

        if (this.file == null) {                                            // if no file has been specified, yet
            this.file = file;                                               // take this
            this.fileType = Audio.WAVE;
        }

        try {
            file.createNewFile();                                           // create the file if it does not already exist
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        AudioInputStream ais = Audio.convertByteArray2AudioInputStream(this.audio, this.format);  // convert the audio byte array to an AudioInputStream
        try {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);                        // write to file system
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
        if (this.fileType.equals(Audio.MP3))
            return this.writeMp3(Helper.getFilenameWithoutExtension(this.file.getAbsolutePath()) + ".mp3");

        // if this was initially no mp3 file (e.g. wave instead) we should change the file extension so we do not store a wave file as ".wav"
        String filename;
        try {
            filename = Helper.getFilenameWithoutExtension(this.file.getCanonicalPath()) + ".mp3";
        } catch (IOException e) {
            System.err.println("No file specified to write audio data.");   // print error message
            return false;                                                   // cancel
        }
        return this.writeMp3(filename);
    }

    /**
     * write audio data as MP3 to the file system with specified filename,
     * this will generate an mp3 file even if you give it another extension
     * @param filename
     * @return true if success, false if an error occurred
     */
    public synchronized boolean writeMp3(String filename) {
        File file = new File(filename);     // create the file with this filename
        file.getParentFile().mkdirs();      // ensure that the directory exists
        return this.writeMp3(file);         // write into it
    }

    /**
     * write audio data as MP3 to the file system to specified file,
     * this will generate an mp3 file even if you give it another extension
     * @param file
     * @return true if success, false if an error occurred
     */
    public synchronized boolean writeMp3(File file) {
        if (file == null) {                                                 // if no valid file
            System.err.println("No file specified to write audio data.");   // print error message
            return false;                                                   // cancel
        }

        if (this.file == null) {                                            // if no file has been specified, yet
            this.file = file;                                               // take this
            this.fileType = Audio.MP3;
        }

        file.getParentFile().mkdirs();                                      // ensure that the directory exists
        try {
            file.createNewFile();                                           // create the file if it does not already exist
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
