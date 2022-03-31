package meico.mpm.elements.maps.data;

import meico.mpm.elements.styles.OrnamentationStyle;
import meico.mpm.elements.styles.defs.OrnamentDef;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * this class is used to collect all relevant data to compute ornamentation
 * @author Axel Berndt
 */
public class OrnamentData {
    public Element xml = null;
    public String xmlId = null;

    public String styleName = "";
    public OrnamentationStyle style = null;
    public String ornamentDefName = null;
    public OrnamentDef ornamentDef = null;

    public double date = 0.0;                       // the date for which the data is assembled
    public double scale = 0.0;
    public ArrayList<String> noteOrder = null;

    /**
     * default constructor
     */
    public OrnamentData() {}

    /**
     * constructor from XML element parsing
     * @param xml MPM ornament element
     */
    public OrnamentData(Element xml) {
        this.xml = xml;

        this.date = Double.parseDouble(xml.getAttribute("date").getValue());
        this.ornamentDefName = xml.getAttribute("name.ref").getValue();

        Attribute scale = xml.getAttribute("scale");
        if (scale != null)
            this.scale = Double.parseDouble(scale.getValue());

        Attribute noteOrder = xml.getAttribute("note.order");
        if (noteOrder != null) {
            String no = noteOrder.getValue().trim();
            this.noteOrder = new ArrayList<>();
            if (no.equals("ascending pitch") || no.equals("descending pitch"))
                this.noteOrder.add(no);
            else
                this.noteOrder.addAll(Arrays.asList(no.replaceAll("#", "").split("\\s+")));
        }

        Attribute id = xml.getAttribute("id", "http://www.w3.org/XML/1998/namespace");
        if (id != null)
            this.xmlId = id.getValue();
    }

    /**
     * create a copy of this object
     * @return
     */
    @Override
    public OrnamentData clone() {
        OrnamentData clone = new OrnamentData();
        clone.xml = (this.xml == null) ? null : this.xml.copy();
        clone.xmlId = this.xmlId;
        clone.styleName = this.styleName;
        clone.style = this.style;
        clone.ornamentDefName = this.ornamentDefName;
        clone.ornamentDef = this.ornamentDef;
        clone.date = this.date;
        clone.scale = this.scale;
        if (this.noteOrder != null) {
            clone.noteOrder = new ArrayList<>();
            clone.noteOrder.addAll(this.noteOrder);
        }
        return clone;
    }

    /**
     * Apply the ornament to the given chord/note sequence. This will only add
     * corresponding attributes to the notes; their realization in performance
     * attributes is done later during performance rendering. This method will
     * also return new notes to be added to the chordSequence's underlying map.
     * If notes should be deleted from the performance, they are marked by an according attribute.
     * @param chordSequence the sequence of the chords/notes in which the ornament is applied
     * @return sequence of chords/notes to be added to the chordSequence's underlying map or null
     */
    public ArrayList<ArrayList<Element>> apply(ArrayList<ArrayList<Element>> chordSequence) {
        ArrayList<ArrayList<Element>> chordsToAdd = new ArrayList<>();                      // if new notes are added to the underlying map, these will be collected in this list and returned at the end

        if (this.ornamentDef == null)
            return chordsToAdd;

        ArrayList<ArrayList<Element>> tempChordSequence = new ArrayList<>(chordSequence);   // a note sequence to apply the further transformations

        // TODO: if new notes are generated that might add to or replace the notes in the sequence,
        //  do this here and forward this incl. the new notes to the dynamicsGradient via tempChordSequence;
        //  in case of replacement, the notes to be deleted get a corresponding attribute and

        if (this.ornamentDef.getDynamicsGradient() != null)
            this.ornamentDef.getDynamicsGradient().apply(tempChordSequence, this.scale);

        if (this.ornamentDef.getTemporalSpread() != null)
            this.ornamentDef.getTemporalSpread().apply(tempChordSequence);

        return chordsToAdd;
    }
}
