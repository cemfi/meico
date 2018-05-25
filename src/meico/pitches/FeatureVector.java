package meico.pitches;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

/**
 * This class represents one pitch feature, i.e. an array of FeatureElements that form the feature vector.
 * @author Axel Berndt.
 */

public class FeatureVector {
    private FeatureElement[] feature = null;                // a feature vector is an array of feature elements

    /**
     * constructor
     * @param feature the feature vector, i.e. array of FeatureElements
     */
    public FeatureVector(FeatureElement[] feature) {
        this.feature = feature;
    }

    /**
     * constructor
     * @param key initializes a 0-vector that conforms the given key
     */
    public FeatureVector(Key key) {
        this.feature = new FeatureElement[key.getSize()];
        for (int i = 0; i < this.feature.length; ++i) {
            this.feature[i] = new FeatureElement();
        }
    }

    /**
     * retrun the getSize of the feature vector
     * @return
     */
    public int getSize() {
        return this.feature.length;
    }

    /**
     * this method returns the element of this vector at the given index
     * @param index
     * @return
     */
    public FeatureElement getFeatureElement(int index) {
        return this.feature[index];
    }

    /**
     * This adds up the energy and note ids of two feature vectors.
     * But be aware that the vectors should have the same size. Otherwise correctness of this operation cannot be ensured.
     * @param feature the feature vector to be added
     * @return
     */
    public boolean add(FeatureVector feature) {
        int maxIndex = (this.getSize() < feature.getSize()) ? this.getSize() : feature.getSize();   // to avoid IndexOutOfBounds we can only iterate to the smallest maximum index of both vectors

        for (int i=0; i < maxIndex; ++i) {                          // go through the vector elements
            FeatureElement e = feature.getFeatureElement(i);        // get the element of the vector to be added
            this.feature[i].addEnergy(e.getEnergy());               // add its energy
            this.feature[i].addNoteIds(e.getNoteIds());             // add its note ids
        }

        return this.feature.length == feature.getSize();             // if this is true, everything is fine, otherwise it is at the discretion of the application
    }

    /**
     * This adds only an energy vector to the feature vector, no ids.
     * But be aware that the vectors should have the same size. Otherwise correctness of this operation cannot be ensured.
     * @param energy
     * @return
     */
    public boolean addEnergy(double[] energy) {
        int maxIndex = (this.feature.length < energy.length) ? this.feature.length : energy.length;   // to avoid IndexOutOfBounds we can only iterate to the smallest maximum index of both vectors

        for (int i=0; i < maxIndex; ++i)                            // go through the vector elements
            this.feature[i].addEnergy(energy[i]);                   // add energy

        return this.feature.length == energy.length;                // if this is true, everything is fine, otherwise it is at the discretion of the application
    }

    /**
     * converts this class instance into a JSONObject
     * @return
     */
    protected JsonObject toJson() {
        JsonArray energyVector = new JsonArray();               // the energy vector (e.g. chroma vector)
        JsonArray idVector = new JsonArray();                   // the corresponding array of note ids

        for (int i=0; i < this.feature.length; ++i) {           // go through all feature elements
            FeatureElement fe = feature[i];
            energyVector.add(fe.getEnergy());                   // write its energy level to the json energy vector

            JsonArray ids = new JsonArray();
            ids.addAll(fe.getNoteIds());                        // collect all the note ids that contribute to the energy
            idVector.add(ids);                                  // fill the note id vector
        }

        // create the json representative of this feature vector
        JsonObject json = new JsonObject();
        json.put("nrg", energyVector);
        json.put("ids", idVector);

        return json;
    }
}
