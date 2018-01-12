package meico.chroma;

/**
 * This class provides the key for the Chroma class with reference frequencies etc.
 * @author Axel Berndt.
 */

public class Key {
    private double[] referenceFrequencies;          // the reference frequencies
    private boolean octaveModulo;                   // is the chroma freature represent pitch classes (true) or absolute pitches (false)

    /**
     * default constructor
     * creates key for equal tempered tuning over concert a 440 Hz
     */
    public Key() {
        this(new double[]{261.63, 277.18, 293.66, 311.13, 329.63, 349.23, 369.99, 392.0, 415.3, 440.0, 466.16, 493.88}, true);
    }

    /**
     * constructor
     * @param referenceFrequencies
     * @param octaveModulo
     */
    public Key(double[] referenceFrequencies, boolean octaveModulo) {
        this.octaveModulo = octaveModulo;
        this.referenceFrequencies = referenceFrequencies;
    }

    /**
     * a getter for the reference frequencies
     * @return
     */
    public double[] getReferenceFrequencies() {
        return this.referenceFrequencies;
    }

    /**
     * this getter returns the size/length of the vector of reference frequencies
     * @return
     */
    public int getSize() {
        return this.referenceFrequencies.length;
    }

    /**
     * a getter to return whether the chroma features represent pitch classes (true) or absolute pitches (false)
     * @return
     */
    public boolean getOctaveModulo() {
        return this.octaveModulo;
    }
}
