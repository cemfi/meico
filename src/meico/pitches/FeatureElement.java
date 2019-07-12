package meico.pitches;

import java.util.ArrayList;

/**
 * This class represents one element of a pitch feature vector, i.e. an energy value on a frequency or chroma band and note id associations to it.
 * @author Axel Berndt.
 */

public class FeatureElement {
    private double energy = 0.0;                                    // the energy value at this pitch or chroma band
    private ArrayList<String> noteIds = new ArrayList<String>();    // a list of note ids that contribute to the energy value

    /**
     * constructor
     */
    public FeatureElement() {
    }

    /**
     * constructor that sets an energy value but no note associations
     * @param energy an energy value
     */
    public FeatureElement(double energy) {
        this.energy = energy;
    }

    /**
     * constructor that leaves energy at 0.0 but sets note associations
     * @param noteIds note associations
     */
    public FeatureElement(ArrayList<String> noteIds) {
        this.noteIds = noteIds;
        this.cleanupMultipleEntries();
    }

    /**
     * constructor to set both, energy and note associations
     * @param energy an energy value
     * @param noteIds note associations
     */
    public FeatureElement(double energy, ArrayList<String> noteIds) {
        this.energy = energy;
        this.noteIds = noteIds;
        this.cleanupMultipleEntries();
    }

    /**
     * this method removes multiple entries of note ids and, hence, reduces memory consumption
     */
    private void cleanupMultipleEntries() {
        int size = this.noteIds.size();
        for (int i=0; i < size - 1; ++i) {
            String id1 = this.noteIds.get(i);
            int j = i + 1;
            while (j < size) {
                String id2 = this.noteIds.get(j);
                if (id2.equals(id1)) {
                    this.noteIds.remove(j);
                    --size;
                    continue;
                }
                ++j;
            }
        }
    }

    /**
     * setter for energy
     * @param energy
     */
    public void setEnergy(double energy) {
        this.energy = energy;
    }

    /**
     * adds the given amount of energy
     * @param energy
     */
    public void addEnergy(double energy) {
        this.energy += energy;
    }

    /**
     * add a note association to the list of note ids
     * @param noteId
     */
    public void addNoteId(String noteId) {
        // check if the id is already in the list, we do not need double entries
        for (String id : this.noteIds) {
            if (id.equals(noteId))          // found it
                return;                     // done
        }

        this.noteIds.add(noteId);           // add it
    }

    /**
     * add several note ids at once
     * @param ids
     */
    public void addNoteIds(ArrayList<String> ids) {
        for (String id : ids) {
            this.addNoteId(id);
        }
    }

    /**
     * removes an entry from the list of note ids
     * @param noteId
     * @return false if the entry was already in and has not been added a second time, otherwise true
     */
    public boolean removeNoteId(String noteId) {
        boolean thisIdWasInTheList = false;

        for (int i=0; i < this.noteIds.size(); ++i) {
            String id = this.noteIds.get(i);
            if (id.equals(noteId)) {
                this.noteIds.remove(i);
                thisIdWasInTheList = true;
                --i;
            }
        }

        return thisIdWasInTheList;
    }

    /**
     * getter for the energy value
     * @return
     */
    public double getEnergy() {
        return this.energy;
    }

    /**
     * getter for the note ids
     * @return
     */
    public ArrayList<String> getNoteIds() {
        return this.noteIds;
    }
}
