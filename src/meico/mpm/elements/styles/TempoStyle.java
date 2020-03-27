package meico.mpm.elements.styles;

import meico.mei.Helper;
import meico.mpm.elements.styles.defs.TempoDef;
import nu.xom.Element;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class interfaces tempo style definitions.
 * Basically, these define a string (such as "Allegro", "fast", "as slow as possible")
 * and assotiate it with an instance of TempoDef.
 * @author Axel Berndt
 */
public class TempoStyle extends GenericStyle {
    private HashMap<String, TempoDef> tempoDefs;              // the lookup table for TempoDef

    /**
     * this constructor generates an empty styleDef for tempoDefs to be added subsequently
     * @param name
     * @throws Exception
     */
    private TempoStyle(String name) throws Exception {
        super(name);
    }

    /**
     * this constructor generates the object from xml input
     * @param xml
     * @throws Exception
     */
    private TempoStyle(Element xml) throws Exception {
        super(xml);
    }

    /**
     * TempoStyle factory
     * @param name
     * @return
     */
    public static TempoStyle createTempoStyle(String name) {
        TempoStyle tempoStyle;
        try {
            tempoStyle = new TempoStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return tempoStyle;
    }

    /**
     * TempoStyle factory
     * @param xml
     * @return
     */
    public static TempoStyle createTempoStyle(Element xml) {
        TempoStyle tempoStyle;
        try {
            tempoStyle = new TempoStyle(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return tempoStyle;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    public void parseData(Element xml) throws Exception {
        super.parseData(xml);

        this.tempoDefs = new HashMap<>();

        // parse the tempoDef elements (the children of this styleDef)
        LinkedList<Element> tempoDefs = Helper.getAllChildElements("tempoDef", this.getXml());
        for (Element def : tempoDefs) { // for each tempoDef
            TempoDef td = TempoDef.createTempoDef(def);
            if (td == null)
                continue;
            this.tempoDefs.put(td.getName(), td);     // add the (name, TempoDef) pair to the lookup table
        }
    }

    /**
     * get the numeric value of a tempo string
     * @param tempoString
     * @return the numeric bpm value or 100.0 if everything else fails
     */
    public double getNumericBpmValue(String tempoString) {
        TempoDef tempoDef = this.getTempoDef(tempoString);
        if (tempoDef != null)
            return tempoDef.getValue();
        try {
            return Double.parseDouble(tempoString);
        } catch (NumberFormatException e) {
            System.err.println("Failed to convert tempo string \"" + tempoString + "\" to double. No tempoDef, no number format.");
            return 100.0;
        }
    }

    /**
     * get the numeric value of a tempo string, this is the static variant of the above method
     * @param tempoString
     * @param style an instance of TempoStyle or null
     * @return the numeric bpm value or 100.0 if everything else fails
     */
    public static double getNumericBpmValue(String tempoString, TempoStyle style) {
        TempoDef tempoDef = (style != null) ? style.getTempoDef(tempoString) : null;
        if (tempoDef != null)
            return tempoDef.getValue();

        try {
            return Double.parseDouble(tempoString);
        } catch (NumberFormatException e) {
            System.err.println("Failed to convert tempo string \"" + tempoString + "\" to double. No tempoDef, no number format.");
            return 100.0;
        }
    }

    /**
     * access the whole HashMap with (name, TempoDef) pairs
     * @return
     */
    public HashMap<String, TempoDef> getAllTempoDefs() {
        return this.tempoDefs;
    }

    /**
     * retrieve a specific TempoDef
     * @param name
     * @return
     */
    public TempoDef getTempoDef(String name) {
        return this.tempoDefs.get(name);
    }

    /**
     * add or (if a TempoDef with this name is already existent) replace the TempoDef
     * @param tempoDef the TempoDef instance to be added, if there is already one with this name, it is replaced
     */
    public void addTempoDef(TempoDef tempoDef) {
        if (tempoDef == null) {
            System.err.println("Cannot add a null TempoDef to the styleDef.");
            return;
        }
        removeTempoDef(tempoDef.getName());               // if there is already a tempoDef with this name, remove it
        this.tempoDefs.put(tempoDef.getName(), tempoDef);
        this.getXml().appendChild(tempoDef.getXml());
    }

    /**
     * remove the specified TempoDef from this styleDef
     * @param name
     */
    public void removeTempoDef(String name) {
        TempoDef rd = this.tempoDefs.get(name);    // get the xml element of this tempoDef
        if (rd == null)                                 // if there is no such tempoDef
            return;                                     // done

        this.tempoDefs.remove(name);                  // remove the (name, values) lookup table entry
        this.getXml().removeChild(rd.getXml());         // remove the element from the xml
    }

    /**
     * get the number of tempoDefs in this styleDef
     * @return
     */
    public int size() {
        return this.tempoDefs.size();
    }

    /**
     * does the styleDef contain tempoDefs?
     * @return
     */
    public boolean isEmpty() {
        return this.tempoDefs.isEmpty();
    }
}
