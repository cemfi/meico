package meico.chroma;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Tis class represents the list of chroma features for a piece of music.
 * @author Axel Berndt.
 */

public class Chroma {
    private File file;
    private ArrayList<double[]> features;   // the list of chroma features
    private meico.chroma.Key key;           // the reference key for the chroma features

    /**
     * default constructor
     * creates an empty Chroma object for an equal temperament with pitch class chroma features
     */
    public Chroma() {
        this.file = null;
        this.key = new Key();               // create a default key
        this.features = new ArrayList<>();  // create an empty list of chroma features
    }

    /**
     * constructor
     * creates an empty Chroma object with the given key
     * @param key
     */
    public Chroma(meico.chroma.Key key) {
        this.file = null;
        this.key = key;
        this.features = new ArrayList<>();  // create an empty list of chroma features
    }

    /**
     * constructor which adds the first feature to the features list
     * @param key
     * @param firstFeature
     * @throws Exception if the dimensions of key and firstFeature do not match
     */
    public Chroma(meico.chroma.Key key, double[] firstFeature) throws Exception {
        if (firstFeature.length != key.getSize())
            throw new Exception("Dimensions of key and feature vector do not match.");

        this.file = null;
        this.key = key;
        this.features = new ArrayList<>();             // create an empty list of chroma features
        this.addFeatureAt(0, firstFeature);     // add the first feature
    }

    /**
     * this constructor initializes the Chroma object with the given key and list of chroma features
     * @param key
     * @param features
     */
    public Chroma(meico.chroma.Key key, ArrayList<double[]> features) throws Exception {
        // check consistency of input data
        for (int i = 0; i < features.size(); ++i) {
            if (features.get(i).length != key.getSize())
                throw new Exception("Dimensions of key and feature vector " + i + " do not match.");
        }

        this.file = null;
        this.key = key;
        this.features = features;
    }

    /**
     * this getter returns the file
     *
     * @return a java File object (this file does not necessarily have to exist in the file system, but may be created there when writing the file with writeChroma())
     */
    public File getFile() {
        return this.file;
    }

    /**
     * with this setter a new filename can be set
     *
     * @param filename the filename including the full path and .chr extension
     */
    public void setFile(String filename) {
        this.file = new File(filename);
    }

    /**
     * a getter for the key
     * @return
     */
    public Key getKey() {
        return this.key;
    }

    /**
     * this getter returns the whole features ArrayList
     * @return
     */
    public ArrayList<double[]> getFeatures() {
        return features;
    }

    /**
     * a getter that returns the number of chroma features.
     * @return
     */
    public int getFeatureCount() {
        return this.features.size();        // return the size of the features list
    }

    /**
     * returns the chroma vector at the given index or null if index out of bounds
     * @param index the index of the chroma feature should be in [0, features.size()-1], otherwise null is returned
     * @return
     */
    public double[] getFeatureAt(int index) {
        try {
            return this.features.get(index);        // try to access the index in the features list
        } catch (IndexOutOfBoundsException e) {     // if the index is not in the list
            return null;                            // return null
        }
    }

    /**
     * add a chroma feature vector at the given index;
     * if there is already a vector at that index, their values will be added
     * @param index
     * @param feature
     * @return true if the operation has been performed successfully, otherwise false
     */
    public boolean addFeatureAt(int index, double[] feature) {
        if (index < 0) return false;                                // an index below 0 makes no sense

        if (feature.length != this.key.getSize()) {                 // if the feature to be added does not match with the size of the reference frequencies vector
            System.err.println("Error: Dimensions of key and feature vector do not match. It cannot be added to the chroma features.");
            return false;
        }

        if (index >= this.features.size()) {                        // if the index is behind the last index
            // create an "all-zero feature"
            double[] filler = new double[feature.length];
            for (int i=0; i < feature.length; ++i)
                filler[i] = 0;

            // add enough copies of the filler to fill up the list until the desired index
            for (int i = this.features.size(); i <= index; ++i) {
                this.features.add(filler);
            }
        }

        // add the new feature to the feature vector in the list
        double[] f = this.features.get(index);
        for (int i=0; i < feature.length; ++i)
            f[i] += feature[i];

        return true;
    }

    /**
     * write the chroma features to a file with default filename
     * @return true if success, false if an error occured
     */
    public boolean writeChroma() {
        if (this.file == null) {
            System.err.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        return this.writeChroma(this.file.getPath());
    }

    /**
     * write chroma features to a file with specified filename (filename should include the path and the extension .chr)
     * @param filename the filename string; it should include the path and the extension .chr
     * @return true if success, false if an error occured
     */
    public boolean writeChroma(String filename) {
        // create the file in the file system
        File file = new File(filename);
        file.getParentFile().mkdirs();                              // ensure that the directory exists
        try {
            file.createNewFile();                                   // create the file if it does not already exist
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        // write into the file
        try(PrintWriter out = new PrintWriter(filename)){
            for (int i=0; i < features.size(); ++i)
                out.println(Arrays.toString(this.features.get(i)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        if (this.file == null)
            this.file = file;

        return true;
    }
}
