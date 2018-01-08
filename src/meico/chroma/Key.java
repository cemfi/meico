package meico.chroma;

/**
 * This class provides the key for the Chroma class with reference frequencies etc.
 * @author Axel Berndt.
 */

public class Key {

    public double[] referenceFrequencies;          // the reference frequencies
    public boolean octaveModulo;                   // is the chroma freature represent pitch classes (true) or absolute pitches (false)

    /**
     * default constructor
     * creates key for equal tempered tuning over concert a 440 Hz
     */
    public Key() {
        this(new double[]{261.6, 277.2, 293.7, 311.1, 329.6, 349.2, 370.0, 392.0, 415.3, 440.0, 466.2, 493.9}, true);
    }

    /**
     * constructor
     * @param referenceFrequencies
     * @param octaveModulo
     */
    public Key(double[] referenceFrequencies, boolean octaveModulo) {
        this.octaveModulo = octaveModulo;
        this.referenceFrequencies = referenceFrequencies;
        this.normalize();
    }

    /**
     * a getter for the reference frequencies
     * @return
     */
    public double[] getReferenceFrequencies() {
        return this.referenceFrequencies;
    }

    /**
     * a getter to return whether the chroma features represent pitch classes (true) or absolute pitches (false)
     * @return
     */
    public boolean getOctaveModulo() {
        return this.octaveModulo;
    }

    /**
     * This methhod scales the reference frequencies down so that the lowest frequency is 1.
     * However, this method has only an effect if octaveModulo is true!
     */
    public void normalize() {
        if (!this.octaveModulo) return;                                                         // if this key represents absolute pitches, normalization should not be applied

        double[] normalizedFreqs = new double[this.referenceFrequencies.length];
        for (int i = this.referenceFrequencies.length - 1; i >= 0; --i) {                       // for each reference frequency
            normalizedFreqs[i] = this.referenceFrequencies[i] / this.referenceFrequencies[0];   // devide it by the first/lowest frequency
        }
        this.referenceFrequencies = normalizedFreqs;
    }
}
