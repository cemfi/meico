package meico.mpm.elements.maps.data;

import meico.mpm.elements.styles.OrnamentationStyle;
import meico.mpm.elements.styles.defs.OrnamentDef;
import meico.msm.elements.MsmNoteElement;
import meico.supplementary.KeyValue;
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

    public String correspondence = null;
    public double date = 0.0;                       // the date for which the data is assembled
    public double scale = 0.0;
    public ArrayList<String> noteOrder = null;
    public ArrayList<Element> notes = null;
    public int repetitions = 0;

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

        Attribute corresp = xml.getAttribute("noteid");
        if(corresp != null)
            this.correspondence = corresp.getValue();

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

        this.notes = new ArrayList<>();
        xml.getChildElements("ornamentNote").forEach(note -> { this.notes.add(note); });

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
        clone.correspondence = this.correspondence;
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
        if (this.notes != null) {
           clone.notes = new ArrayList<>();
           clone.notes.addAll(this.notes);
        }
        clone.repetitions = this.repetitions;
        return clone;
    }

    /**
     * Apply the ornament to the given chord/note sequence. This will only add
     * corresponding attributes to the notes; their realization in performance
     * attributes is done later during performance rendering. This method will
     * also return new notes to be added to the chordSequence's underlying map.
     * If notes should be deleted from the performance, they are marked by an according attribute.
     * @param chordSequence the sequence of the chords/notes in which the ornament is applied
     * @return computed spaced start and length (relative in ticks) or null
     */
    public KeyValue<Double, Double> apply(ArrayList<ArrayList<Element>> chordSequence) {
        return apply(chordSequence, null, null, null);
    }

    /**
     * Apply the ornament with optional effective frameStart/frameLength overrides.
     * These overrides are used when multiple ornaments share the same principal note
     * and their frameLengths need proportional distribution.
     * @param chordSequence the sequence of the chords/notes in which the ornament is applied
     * @param effectiveFrameStart if non-null, overrides the ornamentDef's frameStart (in ticks)
     * @param effectiveFrameLength if non-null, overrides the ornamentDef's frameLength (in ticks)
     * @param lastNote the last note of the previous chord sequence, used for temporal spread; if null, the temporal spread is applied as if there is no preceding note
     * @return computed spaced start and length (relative in ticks) or null
     */
    public KeyValue<Double, Double> apply(ArrayList<ArrayList<Element>> chordSequence, Double effectiveFrameStart, Double effectiveFrameLength, MsmNoteElement lastNote) {
        KeyValue<Double, Double> result = null;
        if (this.ornamentDef == null)
            return result;

        ArrayList<ArrayList<Element>> tempChordSequence = new ArrayList<>(chordSequence);   // a note sequence to apply the further transformations

        if (this.ornamentDef.getDynamicsGradient() != null)
            this.ornamentDef.getDynamicsGradient().apply(tempChordSequence, this.scale);

        if (this.ornamentDef.getTemporalSpread() != null)
            result = this.ornamentDef.getTemporalSpread().apply(tempChordSequence, effectiveFrameStart, effectiveFrameLength, lastNote);

        return result;
    }
}
