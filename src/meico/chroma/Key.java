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
        this.octaveModulo = true;
        this.referenceFrequencies = new double[]{261.6, 277.2, 293.7, 311.1, 329.6, 349.2, 370.0, 392.0, 415.3, 440.0, 466.2, 493.9};
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
}
