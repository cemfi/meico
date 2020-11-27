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
public class TempoStyle extends GenericStyle<TempoDef> {
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
     * @param name
     * @param id
     * @return
     */
    public static TempoStyle createTempoStyle(String name, String id) {
        TempoStyle tempoStyle;
        try {
            tempoStyle = new TempoStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        tempoStyle.setId(id);
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

        // parse the tempoDef elements (the children of this styleDef)
        LinkedList<Element> tempoDefs = Helper.getAllChildElements("tempoDef", this.getXml());
        for (Element def : tempoDefs) { // for each tempoDef
            TempoDef td = TempoDef.createTempoDef(def);
            if (td == null)
                continue;
            this.defs.put(td.getName(), td);     // add the (name, TempoDef) pair to the lookup table
        }
    }

    /**
     * get the numeric value of a tempo string
     * @param tempoString
     * @return the numeric bpm value or 100.0 if everything else fails
     */
    public double getNumericBpmValue(String tempoString) {
        TempoDef tempoDef = this.getDef(tempoString);
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
        TempoDef tempoDef = (style != null) ? style.getDef(tempoString) : null;
        if (tempoDef != null)
            return tempoDef.getValue();

        try {
            return Double.parseDouble(tempoString);
        } catch (NumberFormatException e) {
            System.err.println("Failed to convert tempo string \"" + tempoString + "\" to double. No tempoDef, no number format.");
            return 100.0;
        }
    }
}
