package meico.mpm.elements.styles;

import meico.mei.Helper;
import meico.mpm.elements.styles.defs.AccentuationPatternDef;
import nu.xom.Element;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class interfaces metrical accentuation style definitions.
 * Basically, these define a string and
 * and assotiate it with an instance of AccentuationPatternDef.
 * @author Axel Berndt
 */
public class MetricalAccentuationStyle extends GenericStyle {
    private HashMap<String, AccentuationPatternDef> accentuationPatternDefs;              // the lookup table for AccentuationPatternDef

    /**
     * this constructor generates an empty styleDef for AccentuationPatternDefs to be added subsequently
     * @param name
     * @throws Exception
     */
    private MetricalAccentuationStyle(String name) throws Exception {
        super(name);
    }

    /**
     * this constructor generates the object from xml input
     * @param xml
     * @throws Exception
     */
    private MetricalAccentuationStyle(Element xml) throws Exception {
        super(xml);
    }

    /**
     * MetricalAccentuationStyle factory
     * @param name
     * @return
     */
    public static MetricalAccentuationStyle createMetricalAccentuationStyle(String name) {
        MetricalAccentuationStyle metricalAccentuationStyle;
        try {
            metricalAccentuationStyle = new MetricalAccentuationStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return metricalAccentuationStyle;
    }

    /**
     * MetricalAccentuationStyle factory
     * @param xml
     * @return
     */
    public static MetricalAccentuationStyle createMetricalAccentuationStyle(Element xml) {
        MetricalAccentuationStyle metricalAccentuationStyle;
        try {
            metricalAccentuationStyle = new MetricalAccentuationStyle(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return metricalAccentuationStyle;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws Exception {
        super.parseData(xml);

        this.accentuationPatternDefs = new HashMap<>();

        // parse the MetricalAccentuationStyle elements (the children of this styleDef)
        LinkedList<Element> maDefs = Helper.getAllChildElements("accentuationPatternDef", this.getXml());
        for (Element maDef : maDefs) { // for each AccentuationPattern
            AccentuationPatternDef apd = AccentuationPatternDef.createAccentuationPatternDef(maDef);
            if (apd == null)
                continue;
            this.accentuationPatternDefs.put(apd.getName(), apd);     // add the (name, AccentuationPatternDef) pair to the lookup table
        }
    }

    /**
     * access the whole HashMap with (name, AccentuationPatternDef) pairs
     * @return
     */
    public HashMap<String, AccentuationPatternDef> getAllAccentuationPatternDefs() {
        return this.accentuationPatternDefs;
    }

    /**
     * retrieve a specific AccentuationPatternDef
     * @param name
     * @return
     */
    public AccentuationPatternDef getAccentuationPatternDef(String name) {
        return this.accentuationPatternDefs.get(name);
    }

    /**
     * add or (if a AccentuationPatternDef with this name is already existent) replace the AccentuationPatternDef
     * @param accentuationPatternDef the AccentuationPatternDef instance to be added, if there is already one with this name, it is replaced
     */
    public void addAccentuationPatternDef(AccentuationPatternDef accentuationPatternDef) {
        if (accentuationPatternDef == null) {
            System.err.println("Cannot add a null AccentuationPatternDef to the styleDef.");
            return;
        }
        removeAccentuationPatternDef(accentuationPatternDef.getName());               // if there is already a accentuationPatternDef with this name, remove it
        this.accentuationPatternDefs.put(accentuationPatternDef.getName(), accentuationPatternDef);
        this.getXml().appendChild(accentuationPatternDef.getXml());
    }

    /**
     * remove the specified AccentuationPatternDef from this styleDef
     * @param name
     */
    public void removeAccentuationPatternDef(String name) {
        AccentuationPatternDef rd = this.accentuationPatternDefs.get(name);    // get the xml element of this AccentuationPatternDef
        if (rd == null)                                 // if there is no such AccentuationPatternDef
            return;                                     // done

        this.accentuationPatternDefs.remove(name);      // remove the (name, values) lookup table entry
        this.getXml().removeChild(rd.getXml());         // remove the element from the xml
    }

    /**
     * get the number of AccentuationPatternDefs in this styleDef
     * @return
     */
    public int size() {
        return this.accentuationPatternDefs.size();
    }

    /**
     * does the styleDef contain AccentuationPatternDefs?
     * @return
     */
    public boolean isEmpty() {
        return this.accentuationPatternDefs.isEmpty();
    }
}
