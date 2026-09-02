package meico.msm.elements;

import meico.mei.Helper;
import meico.xml.RichElement;
import nu.xom.Element;

import java.util.*;

/**
 * This helper-class is an object-oriented wrapper of some `Helper` function. It is meant as easy access of MSM (note) element data without fully supporting all MSM features.
 * @author Lars Engeln
 */
public class MsmNoteElement extends RichElement {

    /**
     * constructor from XML element
     * @param element
     */
    public MsmNoteElement(Element element) {
        super(element);
    }

    /**
     * constructor from XML element with option for deep copy
     * @param element
     * @param deepCopy
     */
    public MsmNoteElement(Element element, boolean deepCopy) {
        super(element, deepCopy);
    }

    /**
     * constructor from local name
     * @param localName
     */
    public MsmNoteElement(String localName)  {
        super(localName);
    }

    /**
     * returns the date
     * @return
     */
    public Double getDate() {
        return getAsDouble("date");
    }

    /**
     * returns the pitchname
     * @return
     */
    public String getNoteName() {
        return get("pitchname");
    }

    /**
     * returns the duration
     * @return
     */
    public Double getDuration() {
        return getAsDouble("duration");
    }

    /**
     * returns the octave
     * @return
     */
    public Double getOctave() {
        return getAsDouble("octave");
    }

    /**
     * returns the duration in Milliseconds by using "milliseconds.date"-"milliseconds.date.end"
     * @return
     */
    public Double getMillisecondsDuration() {
        return getMillisecondsEnd() - getMillisecondsDate();
    }

    /**
     * returns "milliseconds.date"
     * @return
     */
    public Double getMillisecondsDate() {
        return getAsDouble("milliseconds.date");
    }

    /**
     * returns "milliseconds.date.end"
     * @return
     */
    public Double getMillisecondsEnd() {
        return getAsDouble("milliseconds.date.end");
    }

    /**
     * compares this note to another note and returns true if they have the same pitchname and octave, false otherwise
     * @param note
     * @return
     */
    public boolean isSameNote(MsmNoteElement note) {
        if (note == null) return false;
        if (!this.getNoteName().equals(note.getNoteName())) return false;
        if (!this.getOctave().equals(note.getOctave())) return false;
        return true;
    }

    /**
     * returns all children as MsmElements
     * @return
     */
    public ArrayList<MsmNoteElement> getChildrenAsMsmElements() {
        LinkedList<Element> elements = Helper.getAllChildElements(this.element);
        ArrayList<MsmNoteElement> children = new ArrayList<>();
        elements.forEach(elem -> children.add(new MsmNoteElement(elem)));
        return children;
    }
}
