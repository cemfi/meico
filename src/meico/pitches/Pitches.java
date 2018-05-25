package meico.pitches;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import java.io.*;
import java.util.ArrayList;

/**
 * This class represents the list of pitch features for a piece of music.
 * It supports also Chroma features.
 * @author Axel Berndt.
 */

public class Pitches {
    private File file = null;
    private ArrayList<FeatureVector> features;      // the timeframe-wise list of pitch features
    private meico.pitches.Key key;                  // the reference key for the pitch features

    /**
     * default constructor
     * creates an empty Pitches object with the Key set to Chroma type features
     */
    public Pitches() {
        this.key = new Key();               // create a default key
        this.features = new ArrayList<>();  // create an empty list of chroma features
    }

    /**
     * constructor
     * creates an empty Pitches object with the given key
     * @param key
     */
    public Pitches(meico.pitches.Key key) {
        this.key = key;
        this.features = new ArrayList<>();  // create an empty list of chroma features
    }


    /**
     * this getter returns the file
     *
     * @return a java File object (this file does not necessarily have to exist in the file system, but may be created there when writing the file with writePitches())
     */
    public File getFile() {
        return this.file;
    }

    /**
     * with this setter a new filename can be set
     *
     * @param filename the filename including the full path and .pch extension
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
    public ArrayList<FeatureVector> getFeatures() {
        return this.features;
    }

    /**
     * a getter that returns the number of pitch features.
     * @return
     */
    public int getFeatureCount() {
        return this.features.size();        // return the getSize of the features list
    }

    /**
     * returns the pitch feature vector at the given index or null if index out of bounds
     * @param index the index of the pitch feature should be in [0, features.getSize()-1], otherwise null is returned
     * @return
     */
    public FeatureVector getFeatureAt(int index) {
        try {
            return this.features.get(index);        // try to access the index in the features list
        } catch (IndexOutOfBoundsException e) {     // if the index is not in the list
            return null;                            // return null
        }
    }

    /**
     * add a pitch feature vector at the given index;
     * if there is already a vector at that index, their values will be added
     * @param index
     * @param feature
     * @return true if the operation has been performed successfully, otherwise false
     */
    public boolean addFeatureAt(int index, FeatureVector feature) {
        if (index < 0) return false;                                // an index below 0 makes no sense

        if (feature.getSize() != this.key.getSize()) {               // if the feature to be added does not match with the getSize of the reference frequencies vector
            System.err.println("Error: Dimensions of key and feature vector do not match. It cannot be added to the pitch features.");
            return false;
        }

        if (index >= this.features.size()) {                        // if the index is behind the last index
            // add enough "all-zero features" to fill up the list until the desired index
            for (int i = this.features.size(); i <= index; ++i) {
                FeatureVector filler = new FeatureVector(this.key); // create an "all-zero feature"
                this.features.add(filler);
            }
        }

        this.features.get(index).add(feature);                      // add the new feature to the feature vector in the list

        return true;
    }

    /**
     * converts this class instance into a JsonObject, including the key
     * @return
     */
    private JsonObject toJson() {
        JsonObject pitches = new JsonObject();      // the JSON instance of this class

        pitches.put("key", this.key.toJson());      // add the key

        // get all feature vectors into a JsonArray
        JsonArray feats = new JsonArray();
        for (FeatureVector fv : this.features) {
            feats.add(fv.toJson());
        }

        pitches.put("features", feats);

        return pitches;
    }

    /**
     * write the pitch features to a file with default filename
     * @return true if success, false if an error occured
     */
    public boolean writePitches() {
        if (this.file == null) {
            System.err.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        return this.writePitches(this.file.getPath(), false);
    }

    /**
     * write the pitch features to a file with the specified filename,
     * prettyPrint is set false for memory efficiency
     * @param filename
     * @return
     */
    public boolean writePitches(String filename) {
        return this.writePitches(filename, false);
    }

    /**
     * write pitch features to a file with specified filename (filename should include the path and the extension .pch)
     * @param filename the filename string; it should include the path and the extension .json
     * @param prettyPrint set true for better readability, set false for better memory efficiency
     * @return true if success, false if an error occured
     */
    public boolean writePitches(String filename, boolean prettyPrint) {
        // create the file in the file system
        File file = new File(filename);
        file.getParentFile().mkdirs();                  // ensure that the directory exists
        try {
            file.createNewFile();                       // create the file if it does not already exist
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        String json = this.toJson().toJson();           // generate output String
        if (prettyPrint)
            json = Jsoner.prettyPrint(json);            // Jsoner does the layouting of the output String (linebreaks, indentation etc.)

        // write into the file
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(json);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (this.file == null)
            this.file = file;

        return true;
    }
}
