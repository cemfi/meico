package meico.mpm.elements.styles.defs;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.supplementary.KeyValue;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class interfaces MPM's articulationPattern elements.
 * @author Axel Berndt
 */
public class AccentuationPatternDef extends AbstractDef {
    private double length = 4.0;                                                        // the length of the accentuation pattern in beats (not midi ticks!)
    private ArrayList<KeyValue<double[], Element>> accentuations = new ArrayList<>();   // the list of accentuations in the form ([beat, value, transition.from, transition.to], Element)

    /**
     * constructor creates an empty accentuationPatternDef
     * @param name
     * @throws InvalidDataException
     */
    private AccentuationPatternDef(String name, double length) throws InvalidDataException {
        Element e = new Element("accentuationPatternDef", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("name", name));
        e.addAttribute(new Attribute("length", Double.toString(length)));
        this.parseData(e);
    }

    /**
     * contructor to create a AccentuationPatternDef instance from the xml
     * @param xml
     * @throws InvalidDataException
     */
    private AccentuationPatternDef(Element xml) throws InvalidDataException {
        this.parseData(xml);
    }

    /**
     * AcctentuationPatternDef factory
     * @param name
     * @param length
     * @return
     */
    public static AccentuationPatternDef createAccentuationPatternDef(String name, double length) {
        AccentuationPatternDef accentuationPatternDef;
        try {
            accentuationPatternDef = new AccentuationPatternDef(name, length);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return accentuationPatternDef;
    }

    /**
     * AcctentuationPatternDef factory
     * @param xml
     * @return
     */
    public static AccentuationPatternDef createAccentuationPatternDef(Element xml) {
        AccentuationPatternDef accentuationPatternDef;
        try {
            accentuationPatternDef = new AccentuationPatternDef(xml);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return accentuationPatternDef;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws InvalidDataException {
        if (xml == null)
            throw new InvalidDataException("Cannot generate AccentuationPatternDef object. XML Element is null.");

        // parse the dynamicsDef element
        this.name = Helper.getAttribute("name", xml);                       // get its name attribute
        if (this.name == null) {                                            // if no name
            throw new InvalidDataException("Cannot generate AccentuationPatternDef object. Missing name attribute.");
        }

        this.setXml(xml);

        // make sure that this element is really a "accentuationPatternDef" element
        if (!this.getXml().getLocalName().equals("accentuationPatternDef")) {
            this.getXml().setLocalName("accentuationPatternDef");
        }

        Attribute length = Helper.getAttribute("length", this.getXml());    // get the length attribute
        if (length == null) {                                               // if missing
            length = new Attribute("length", Double.toString(this.length)); // generate a default one
            this.getXml().addAttribute(length);
        }
        this.length = Double.parseDouble(length.getValue());

        // parse the accentuations
        LinkedList<Element> acs = Helper.getAllChildElements("accentuation", this.getXml());
        // get the accentuation element
        for (Element ac : acs) {
            Attribute att = Helper.getAttribute("beat", ac);                // get its beat attribute
            if (att == null)                                                // it is mandatory, if missing
                continue;                                                   // continue with the next accentuation
            double[] accentuation = {Double.parseDouble(att.getValue()), 0.0, 0.0, 0.0};    // initialize the accentuation with default values

            att = Helper.getAttribute("value", ac);
            if (att != null)
                accentuation[1] = Double.parseDouble(att.getValue());

            att = Helper.getAttribute("transition.from", ac);
            if (att != null)
                accentuation[2] = Double.parseDouble(att.getValue());
            else
                accentuation[2] = accentuation[1];

            att = Helper.getAttribute("transition.to", ac);
            if (att != null)
                accentuation[3] = Double.parseDouble(att.getValue());
            else
                accentuation[3] = accentuation[2];

            this.addAccentuationToArrayList(accentuation, ac);              // add the accentuation to this.accentuations
            this.sortXml();                                                 // the xml accentuations can be unsorted, this makes sure it is sorted and synchronous with the internally maintained arraylists this.accentuations and this.accentuationElements
        }
    }

    /**
     * create and add an accentuation to this accentuationPatternDef
     * @param beat
     * @param value
     * @param transitionFrom
     * @param transitionTo
     * @return the index at which it has been added
     */
    public int addAccentuation(double beat, double value, double transitionFrom, double transitionTo) {
        Element accElt = new Element("accentuation", Mpm.MPM_NAMESPACE);                                            // create an xml representation of the accentuation to be added
        accElt.addAttribute(new Attribute("beat", Double.toString(beat)));
        accElt.addAttribute(new Attribute("value", Double.toString(value)));
        accElt.addAttribute(new Attribute("transition.from", Double.toString(transitionFrom)));
        accElt.addAttribute(new Attribute("transition.to", Double.toString(transitionTo)));
        int index = this.addAccentuationToArrayList(new double[]{beat, value, transitionFrom, transitionTo}, accElt);   // add the accentuation to this.accentuations
        this.getXml().insertChild(accElt, index);                                                                   // add it to the xml
        return index;
    }

    /**
     * add a given xml representation of an MPM accentuation element to this accentuationPatternDef
     * @param xml the beat attribute is mandatory for this element
     * @return the index at which it has been added
     */
    public int addAccentuation(Element xml) {
        // make sure that this element is really a "accentuation" element
        if (!xml.getLocalName().equals("accentuation"))
            xml.setLocalName("accentuation");

        // parse it
        Attribute att = xml.getAttribute("beat");                                       // get its beat attribute
        if (att == null)                                                                // it is mandatory, if missing
            return -1;                                                                  // cancel
        double[] accentuation = {Double.parseDouble(att.getValue()), 0.0, 0.0, 0.0};    // initialize the accentuation with default values

        att = xml.getAttribute("value");
        if (att != null)
            accentuation[1] = Double.parseDouble(att.getValue());

        att = xml.getAttribute("transition.from");
        if (att != null)
            accentuation[2] = Double.parseDouble(att.getValue());
        else
            accentuation[2] = accentuation[1];

        att = xml.getAttribute("transition.to");
        if (att != null)
            accentuation[3] = Double.parseDouble(att.getValue());
        else
            accentuation[3] = accentuation[2];

        // add it
        int index = this.addAccentuationToArrayList(accentuation, xml);                 // add the accentuation to this.accentuations
        this.getXml().insertChild(xml, index);                                          // add it to the xml
        return index;
    }

    /**
     * add the accentuation at the right beat position (the xml can be unsorted, this.accentuations should be sorted)
     * @param accentuation the accentuation {beat, value, transition.from, transition.to}
     * @return the index at which the accentuation has been added
     */
    private int addAccentuationToArrayList(double[] accentuation, Element xml) {
        for (int j=this.accentuations.size()-1; j >= 0; --j) {                      // go through the accentuations list
            if (accentuation[0] <= this.accentuations.get(j).getKey()[0]) {         // is the beat of the accentuation to be added at or after the pivot accentuation?
                this.accentuations.add(j, new KeyValue<>(accentuation, xml));       // add it after it
                return j;
            }
        }
        this.accentuations.add(new KeyValue<>(accentuation, xml));               // if the beat is before the first accentuation, add it at the beginning
        return 0;
    }

    /**
     * The accentuation pattern elements can be unsorted in the xml source. This method sorts them according to the list maintained internally.
     */
    private void sortXml() {
        Element xml = this.getXml();
        for (int i = 0; i < this.accentuations.size(); ++i) {           // for each accentuation
            Element accentuation = this.accentuations.get(i).getValue();
            xml.removeChild(accentuation);                              // remove the accentuation wherever it is
            xml.insertChild(accentuation, i);                           // and add it at its correct index
        }
    }

    /**
     * remove an accentuation from this accentuationPatternDef
     * @param index
     */
    public void removeAccentuation(int index) {
        if (index >= this.accentuations.size())
            return;

        this.getXml().removeChild(this.accentuations.get(index).getValue());    // remove the accentuation from the xml
        this.accentuations.remove(index);                                       // remove its values from the accentuations list
    }

    /**
     * access the accentuation pattern
     * @return a sorted list of arrays of the form ([beat, value, transition.from, transition.to], Element)
     */
    public ArrayList<KeyValue<double[], Element>> getAllAccentuations() {
        return this.accentuations;
    }

    /**
     * access the accentuation at the specified index
     * @param index
     * @return the accentuation as an array of the form {beat, value, transition.from, transition.to} or null if index out of bounds
     */
    public double[] getAccentuationAttributes(int index) {
        if (index >= this.accentuations.size())
            return null;

        return this.accentuations.get(index).getKey();
    }

    /**
     * access the accentuation element
     * @param index
     * @return the accentuation element or null if index out of bounds
     */
    public Element getAccentuationXml(int index) {
        if (index >= this.accentuations.size())
            return null;

        return this.accentuations.get(index).getValue();
    }

    /**
     * compute the accentuation value for a given position within the accentuation pattern
     * @param beatPosition 1.0 (not 0.0!) is the beginning of the pattern (in musical terms: the 1st beat)
     * @return the accentuation value; it needs to be scaled to actual velocity
     */
    public double getAccentuationAt(double beatPosition) {
        if (beatPosition < this.accentuations.get(0).getKey()[0])                       // if the position is before or at the first accentuation
            return 0.0;                                                                 // return 0.0

        if (beatPosition >= (this.length + 1.0))                                        // if the beatPosition is at or after the length of the accentuation pattern
            return this.accentuations.get(this.accentuations.size() - 1).getKey()[3];   // return the transition.to value of the last accentuation

        // find the accentuation directly before or at beatPosition
        double[] accentuation = null;                                                   // this will become the accentuation directly before or at beatPosition
        double segmentEnd = this.length + 1.0;                                          // the end date (in beats) of the accentuation segment
        for (int i = this.accentuations.size() - 1; i >= 0; --i) {                      // got through all accentuation in this pattern
            accentuation = this.accentuations.get(i).getKey();
            if (beatPosition == accentuation[0])                                        // if the beatPosition is exactly at the accentuation's position
                return accentuation[1];                                                 // return its value
            if (beatPosition > accentuation[0]) {                                       // beatPosition is between two accentuations or after the last one
                if (i > (this.accentuations.size() - 1))                                // if it is between two accentuations
                    segmentEnd = this.accentuations.get(i+1).getKey()[0];               // store the beate position of the subsequent accentuation (which marks the end date of this accentuation segment)
                break;                                                                  // found all required information
            }
        }

        // compute the accentuation value
        return (((beatPosition - accentuation[0]) * (accentuation[3] - accentuation[2])) / (segmentEnd - accentuation[0])) + accentuation[2];
    }

    /**
     * get the count of accentuations in this accentuation pattern
     * @return the count of accentuations in this accentuation pattern
     */
    public int getSize() {
        return this.accentuations.size();
    }

    /**
     * read the length of the accentuation pattern in beats
     * @return the length of the accentuation pattern in beats
     */
    public double getLength() {
        return this.length;
    }

    /**
     * set the length of the accentuation pattern in midi ticks
     * @param length the length of the accentuation pattern in beats
     */
    public void setLength(double length) {
        this.length = length;
        this.getXml().getAttribute("length").setValue(Double.toString(length));
    }
}
