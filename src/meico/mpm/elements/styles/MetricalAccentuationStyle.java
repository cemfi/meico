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
public class MetricalAccentuationStyle extends GenericStyle<AccentuationPatternDef> {
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
     * @param name
     * @param id
     * @return
     */
    public static MetricalAccentuationStyle createMetricalAccentuationStyle(String name, String id) {
        MetricalAccentuationStyle metricalAccentuationStyle;
        try {
            metricalAccentuationStyle = new MetricalAccentuationStyle(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        metricalAccentuationStyle.setId(id);
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

        // parse the MetricalAccentuationStyle elements (the children of this styleDef)
        LinkedList<Element> maDefs = Helper.getAllChildElements("accentuationPatternDef", this.getXml());
        for (Element maDef : maDefs) { // for each AccentuationPattern
            AccentuationPatternDef apd = AccentuationPatternDef.createAccentuationPatternDef(maDef);
            if (apd == null)
                continue;
            this.defs.put(apd.getName(), apd);     // add the (name, AccentuationPatternDef) pair to the lookup table
        }
    }
}
