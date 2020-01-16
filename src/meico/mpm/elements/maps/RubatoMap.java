package meico.mpm.elements.maps;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.styles.GenericStyle;
import meico.mpm.elements.styles.RubatoStyle;
import meico.supplementary.KeyValue;
import meico.mpm.elements.maps.data.RubatoData;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;

/**
 * This class interfaces MPM's rubatoMaps
 * @author Axel Berndt
 */
public class RubatoMap extends GenericMap {
    /**
     * constructor, generates an empty RubatoMap
     * @throws InvalidDataException
     */
    private RubatoMap() throws InvalidDataException {
        super("rubatoMap");
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws InvalidDataException
     */
    private RubatoMap(Element xml) throws InvalidDataException {
        super(xml);
    }

    /**
     * RubatoMap factory
     * @return
     */
    public static RubatoMap createRubatoMap() {
        RubatoMap d;
        try {
            d = new RubatoMap();
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * RubatoMap factory
     * @param xml
     * @return
     */
    public static RubatoMap createRubatoMap(Element xml) {
        RubatoMap d;
        try {
            d = new RubatoMap(xml);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws InvalidDataException {
        super.parseData(xml);
        this.setType("rubatoMap");            // make sure this is really a "rubatoMap"
    }

    /**
     * add a rubato element to the map
     * @param date
     * @param frameLength
     * @param intensity
     * @param lateStart
     * @param earlyEnd
     * @param loop
     * @return
     */
    public int addRubato(double date, double frameLength, double intensity, double lateStart, double earlyEnd, boolean loop) {
        Element e = new Element("rubato", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("frameLength", Double.toString(frameLength)));
        e.addAttribute(new Attribute("intensity", Double.toString(RubatoMap.ensureIntensityBoundaries(intensity))));

        Double[] le = RubatoMap.ensureLateStartEarlyEndBoundaries(lateStart, earlyEnd);
        e.addAttribute(new Attribute("lateStart", Double.toString(le[0])));
        e.addAttribute(new Attribute("earlyEnd", Double.toString(le[1])));
        e.addAttribute(new Attribute("loop", Boolean.toString(loop)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a rubato element to the map
     * @param date
     * @param rubatoDefName a reference to a rubatoDef
     * @param loop
     * @return
     */
    public int addRubato(double date, String rubatoDefName, boolean loop) {
        Element e = new Element("rubato", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("name.ref", rubatoDefName));
        e.addAttribute(new Attribute("loop", Boolean.toString(loop)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a rubato element to the map
     * @param data xml data will be ignored (use addElement() instead to add an xml representation)
     * @return
     */
    public int addRubato(RubatoData data) {
        if (data == null) {
            System.err.println("Cannot add rubato, RubatoData object is null.");
            return -1;
        }

        Element e = new Element("rubato", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(data.startDate)));

        if (data.xmlId != null)
            e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", data.xmlId));

        if (data.rubatoDef != null)
            e.addAttribute(new Attribute("name.ref", data.rubatoDef.getName()));

        if (data.frameLength == null) {
            if (data.rubatoDef == null) {
                System.err.println("Cannot add rubato, frameLength not specified.");
                return -1;
            }
            data.frameLength = data.rubatoDef.getFrameLength();
        }
        e.addAttribute(new Attribute("frameLength", Double.toString(data.frameLength)));

        data.intensity = RubatoMap.ensureIntensityBoundaries(data.intensity);
        e.addAttribute(new Attribute("intensity", Double.toString(data.intensity)));

        Double[] le = RubatoMap.ensureLateStartEarlyEndBoundaries(data.lateStart, data.earlyEnd);
        if (le[0] != null)
            e.addAttribute(new Attribute("lateStart", Double.toString(le[0])));

        if (le[1] != null)
            e.addAttribute(new Attribute("earlyEnd", Double.toString(le[1])));

        e.addAttribute(new Attribute("loop", Boolean.toString(data.loop)));

        KeyValue<Double, Element> kv = new KeyValue<>(data.startDate, e);
        return this.insertElement(kv, false);
    }

    /**
     * make sure that intensity has avalid value
     * @param intensity
     * @return
     */
    private static double ensureIntensityBoundaries(double intensity) {
        if (intensity == 0.0) {
            System.err.println("Invalid rubato intensity = 0.0 is set to 0.01.");
            return 0.01;
        }
        if (intensity < 0.0) {
            System.err.println("Invalid rubato intensity < 0.0 is inverted.");
            return intensity * -1.0;
        }
        return intensity;
    }

    /**
     * ensure validity and consistency of lateStart and earlyEnd
     * @param lateStart
     * @param earlyEnd
     * @return
     */
    private static Double[] ensureLateStartEarlyEndBoundaries(Double lateStart, Double earlyEnd) {
        Double[] le = new Double[]{lateStart, earlyEnd};
        if ((lateStart != null) && (lateStart < 0.0)) {
            System.err.println("Invalid rubato lateStart < 0.0 is set to 0.0.");
            le[0] = 0.0;
        }
        if ((earlyEnd != null) && (earlyEnd > 1.0)) {
            System.err.println("Invalid rubato earlyEnd > 1.0 is set to 1.0.");
            le[1] = 1.0;
        }
        if ((lateStart != null) && (earlyEnd != null) && (lateStart >= earlyEnd)) {
            System.err.println("Invalid rubato lateStart >= earlyEnd, setting them to 0.0 and 1.0.");
            le[0] = 0.0;
            le[1] = 1.0;
        }
        return le;
    }

    /**
     * collect all data that is needed to compute the rubato at the specified date
     * @param date
     * @return
     */
    private RubatoData getRubatoDataAt(double date) {
        for (int i = this.getElementIndexBeforeAt(date); i >= 0; --i) {
            RubatoData rd = this.getRubatoDataOf(i);
            if (rd != null)
                return rd;
        }
        return null;
    }

    /**
     * this collects the rubato data of a specific element of this rubatoMap, given via the index
     * @param index
     * @return the rubato data or null if the indexed element is invalid, if the index is lower than 0 or the rubatoMap is empty default rubato data is returned
     */
    private RubatoData getRubatoDataOf(int index) {
        if (this.elements.isEmpty() || (index < 0))
            return null;

        if (index >= this.elements.size())
            index = this.elements.size() - 1;

        Element e = this.elements.get(index).getValue();
        if (e.getLocalName().equals("rubato")) {                                            // and it is a rubato element
            RubatoData rd = new RubatoData();
            rd.startDate = this.elements.get(index).getKey();;                              // store the start date of the rubato instruction
            rd.endDate = this.getEndDate(index);                                            // get the date of the subsequent rubato element

            // parse the other attributes
            rd.xml = e;

            Attribute att = Helper.getAttribute("xml:id", e);
            if (att != null)
                rd.xmlId = att.getValue();

            // get the style that applies to this date
            for (int j = index; j >= 0; --j) {                                              // find the first style switch at or before date
                Element s = this.elements.get(j).getValue();
                if (s.getLocalName().equals("style")) {
                    rd.styleName = Helper.getAttributeValue("name.ref", s);
                    break;
                }
            }
            GenericStyle gStyle = this.getStyle(Mpm.DYNAMICS_STYLE, rd.styleName);          // read the rubato style
            if (gStyle != null)
                rd.style = (RubatoStyle) gStyle;

            att = Helper.getAttribute("name.ref", e);
            if (att != null) {                                                              // name.ref attribute is mandatory
                rd.rubatoDefString = att.getValue();
                rd.rubatoDef = rd.style.getRubatoDef(rd.rubatoDefString);
            }

            att = Helper.getAttribute("frameLength", e);
            if (att != null)
                rd.frameLength = Double.parseDouble(att.getValue());
            else if (rd.rubatoDef != null)
                rd.frameLength = rd.rubatoDef.getFrameLength();
            else
                return null;                                                                // the frameLength attribute is mandatory

            att = Helper.getAttribute("loop", e);
            if (att != null)
                rd.loop = Boolean.parseBoolean(att.getValue());

            att = Helper.getAttribute("intensity", e);
            if (att != null)
                rd.intensity = Double.parseDouble(att.getValue());
            else if (rd.rubatoDef != null)
                rd.intensity = rd.rubatoDef.getIntensity();
            rd.intensity = RubatoMap.ensureIntensityBoundaries(rd.intensity);

            att = Helper.getAttribute("lateStart", e);
            if (att != null)
                rd.lateStart = Double.parseDouble(att.getValue());
            else if (rd.rubatoDef != null)
                rd.lateStart = rd.rubatoDef.getLateStart();

            att = Helper.getAttribute("earlyEnd", e);
            if (att != null)
                rd.earlyEnd = Double.parseDouble(att.getValue());
            else if (rd.rubatoDef != null)
                rd.earlyEnd = rd.rubatoDef.getEarlyEnd();

            Double[] le = RubatoMap.ensureLateStartEarlyEndBoundaries(rd.lateStart, rd.earlyEnd);
            rd.lateStart = le[0];
            rd.earlyEnd = le[1];

            return rd;
        }
        return null;
    }

    /**
     * a helper method to retrieve the date at which the indexed rubato instruction ends, which is either the date of the subsequent instruction or Double.MAX_VALUE
     * @param index index of the current rubato instruction for which the endDate is needed
     * @return
     */
    private double getEndDate(int index) {
        // get the date of the subsequent rubato element
        double endDate = Double.MAX_VALUE;
        for (int j = index+1; j < this.elements.size(); ++j) {
            if (this.elements.get(j).getValue().getLocalName().equals("rubato")) {
                endDate = this.elements.get(j).getKey();
                break;
            }
        }
        return endDate;
    }

    /**
     * compute the date to which the input date is shifted by the specified rubato
     * @param date
     * @param rubatoData
     * @return
     */
    private static double computeRubatoTransformation(double date, RubatoData rubatoData) {
        double localDate = (date - rubatoData.startDate) % rubatoData.frameLength;      // compute the position of the map element within the rubato frame
        double d = (Math.pow(localDate / rubatoData.frameLength, rubatoData.intensity) * (rubatoData.earlyEnd - rubatoData.lateStart) + rubatoData.lateStart) * rubatoData.frameLength;
        return date + d - localDate;
    }

    /**
     * on the basis of this rubatoMap, apply the rubato transformations to all date and duration attributes of each map element
     * @param map
     */
    public void renderRubatoToMap(GenericMap map) {
        if ((map == null) || this.elements.isEmpty())
            return;

        ArrayList<KeyValue<Double, Attribute>> pendingDurations = new ArrayList<>();
        int mapIndex = 0;
        for (int rubIndex = 0; rubIndex < this.size(); ++rubIndex) {
            RubatoData rd = this.getRubatoDataOf(rubIndex);
            if (rd == null)
                continue;

            for (; mapIndex < map.size(); ++mapIndex) {                             // traverse the map elements
                KeyValue<Double, Element> mapEntry = map.elements.get(mapIndex);    // get the current map entry

                if (mapEntry.getKey() < rd.startDate)                               // if this map entry is before the current rubato
                    continue;                                                       // go on until we are at of after the rubato's date

                if ((mapEntry.getKey() >= rd.endDate)                                           // if the current map element is out of the scope of the current rubato element
                    || (!rd.loop && (mapEntry.getKey() >= (rd.startDate + rd.frameLength))))    // if this is a oneshot rubato and map entry is already after its frame end
                    break;                                                                      // stop here and find the next rubato element first before continuing

                // compute rubato transformation
                double oldDate = mapEntry.getKey();
                Attribute dateAtt = Helper.getAttribute("date", mapEntry.getValue());
                mapEntry.setKey(RubatoMap.computeRubatoTransformation(mapEntry.getKey(), rd));
                dateAtt.setValue(Double.toString(mapEntry.getKey()));

                // duration has to be converted, too, but if this element has already a date.end attribute, we go on with this
                Attribute dateEndAtt = Helper.getAttribute("date.end", mapEntry.getValue());    // some elements have already a date.end attribute (e.g. section)
                if (dateEndAtt != null) {
                    double endDate = Double.parseDouble(dateEndAtt.getValue());                 // get the tick date of the end of the map element
                    pendingDurations.add(new KeyValue<>(endDate, dateEndAtt));                  // keep the mapIndex in the pendingDurations list to get back to it later
                    continue;
                }
                Attribute durAtt = Helper.getAttribute("duration", mapEntry.getValue());        // if there was no date.end attribute, we check the presence of a duration attribute and generate date.end from it
                if (durAtt != null) {
                    double endDate = oldDate + Double.parseDouble(durAtt.getValue());           // get the tick date of the end of the map element
                    dateEndAtt = new Attribute("date.end", Double.toString(endDate));
                    mapEntry.getValue().addAttribute(dateEndAtt);                               // add attribute date.end
                    pendingDurations.add(new KeyValue<>(endDate, dateEndAtt));                  // keep the mapIndex in the pendingDurations list to get back to it later
                }
            }

            // check pending date.end attributes to fall under this rubato instruction and be processed now
            for (int i=0; i < pendingDurations.size(); ++i) {
                KeyValue<Double, Attribute> pd = pendingDurations.get(i);
                double dateEnd = pd.getKey();
                if ((dateEnd >= rd.endDate)                                                     // if the current map element is out of the scope of the current rubato element
                        || (!rd.loop && (dateEnd >= (rd.startDate + rd.frameLength))))          // if this is a oneshot rubato and map entry is already after its frame end
                    break;                                                                      // stop here and find the next rubato element first before continuing

                pd.getValue().setValue(Double.toString(RubatoMap.computeRubatoTransformation(dateEnd, rd)));

                pendingDurations.remove(pd);
                --i;
            }
        }
    }

    /**
     * on the basis of the specified rubatoMap, apply the rubato transformations to all date and duration attributes of each map element
     * @param map
     * @param rubatoMap
     */
    public static void renderRubatoToMap(GenericMap map, RubatoMap rubatoMap) {
        if (rubatoMap != null)
            rubatoMap.renderRubatoToMap(map);
     }
}
