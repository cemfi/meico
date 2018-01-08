package meico.chroma;

import java.util.ArrayList;

/**
 * Tis class represents the list of chroma features for a piece of music.
 * @author Axel Berndt.
 */

public class Chroma {

    public ArrayList<double[]> features;   // the list of chroma features
    public meico.chroma.Key key;           // the reference key for the chroma features

    /**
     * default constructor
     * creates an empty Chroma object for an equal temperament with pitch class chroma features
     */
    public Chroma() {
        this.key = new Key();               // create a default key
        this.features = new ArrayList<>();  // create an empty list of chroma features
    }
}
