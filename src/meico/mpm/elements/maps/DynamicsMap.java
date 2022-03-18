package meico.mpm.elements.maps;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.styles.DynamicsStyle;
import meico.mpm.elements.styles.GenericStyle;
import meico.mpm.elements.styles.defs.DynamicsDef;
import meico.supplementary.KeyValue;
import meico.mpm.elements.maps.data.DynamicsData;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;

/**
 * This class interfaces MPM's dynamicsMaps
 * @author Axel Berndt
 */
public class DynamicsMap extends GenericMap {
    /**
     * constructor, generates an empty dynamicsMap
     * @throws Exception
     */
    private DynamicsMap() throws Exception {
        super("dynamicsMap");
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws Exception
     */
    private DynamicsMap(Element xml) throws Exception {
        super(xml);
    }

    /**
     * DynamicsMap factory
     * @return
     */
    public static DynamicsMap createDynamicsMap() {
        DynamicsMap d;
        try {
            d = new DynamicsMap();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * DynamicsMap factory
     * @param xml
     * @return
     */
    public static DynamicsMap createDynamicsMap(Element xml) {
        DynamicsMap d;
        try {
            d = new DynamicsMap(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws Exception {
        super.parseData(xml);
        this.setType("dynamicsMap");            // make sure this is really a "dynamicsMap"
    }

    /**
     * add a dynamics element to the dynamicsMap
     * @param date
     * @param volume a numeric or literal string, the latter should have a corresponding dynamicsDef in the dynamicsStyles/styleDef
     * @param transitionTo a numeric or literal string, the latter should have a corresponding dynamicsDef in the dynamicsStyles/styleDef
     * @param curvature
     * @param protraction
     * @param subNoteDynamics
     * @param id
     * @return the index at which it has been inserted
     */
    public int addDynamics(double date, String volume, String transitionTo, double curvature, double protraction, boolean subNoteDynamics, String id) {
        Element e = new Element("dynamics", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("volume", volume));
        e.addAttribute(new Attribute("transition.to", transitionTo));
        e.addAttribute(new Attribute("curvature", Double.toString(DynamicsMap.ensureCurvatureBoundaries(curvature))));
        e.addAttribute(new Attribute("protraction", Double.toString(DynamicsMap.ensureProtractionBoundaries(protraction))));
        if (subNoteDynamics)
            e.addAttribute(new Attribute("subNoteDynamics", "true"));
        e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a dynamics element to the dynamicsMap
     * @param date
     * @param volume a numeric or literal string, the latter should have a corresponding dynamicsDef in the dynamicsStyles/styleDef
     * @param transitionTo a numeric or literal string, the latter should have a corresponding dynamicsDef in the dynamicsStyles/styleDef
     * @param curvature
     * @param protraction
     * @param subNoteDynamics
     * @return the index at which it has been inserted
     */
    public int addDynamics(double date, String volume, String transitionTo, double curvature, double protraction, boolean subNoteDynamics) {
        Element e = new Element("dynamics", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("volume", volume));
        e.addAttribute(new Attribute("transition.to", transitionTo));
        e.addAttribute(new Attribute("curvature", Double.toString(DynamicsMap.ensureCurvatureBoundaries(curvature))));
        e.addAttribute(new Attribute("protraction", Double.toString(DynamicsMap.ensureProtractionBoundaries(protraction))));
        if (subNoteDynamics)
            e.addAttribute(new Attribute("subNoteDynamics", "true"));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a dynamics element to the dynamicsMap, this one features no subNotDynamics
     * @param date
     * @param volume a numeric or literal string, the latter should have a corresponding dynamicsDef in the dynamicsStyles/styleDef
     * @param transitionTo a numeric or literal string, the latter should have a corresponding dynamicsDef in the dynamicsStyles/styleDef
     * @param curvature
     * @param protraction
     * @param id
     * @return the index at which it has been inserted
     */
    public int addDynamics(double date, String volume, String transitionTo, double curvature, double protraction, String id) {
        Element e = new Element("dynamics", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("volume", volume));
        e.addAttribute(new Attribute("transition.to", transitionTo));
        e.addAttribute(new Attribute("curvature", Double.toString(DynamicsMap.ensureCurvatureBoundaries(curvature))));
        e.addAttribute(new Attribute("protraction", Double.toString(DynamicsMap.ensureProtractionBoundaries(protraction))));
        e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a dynamics element to the dynamicsMap, this one features no subNotDynamics
     * @param date
     * @param volume a numeric or literal string, the latter should have a corresponding dynamicsDef in the dynamicsStyles/styleDef
     * @param transitionTo a numeric or literal string, the latter should have a corresponding dynamicsDef in the dynamicsStyles/styleDef
     * @param curvature
     * @param protraction
     * @return the index at which it has been inserted
     */
    public int addDynamics(double date, String volume, String transitionTo, double curvature, double protraction) {
        Element e = new Element("dynamics", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("volume", volume));
        e.addAttribute(new Attribute("transition.to", transitionTo));
        e.addAttribute(new Attribute("curvature", Double.toString(DynamicsMap.ensureCurvatureBoundaries(curvature))));
        e.addAttribute(new Attribute("protraction", Double.toString(DynamicsMap.ensureProtractionBoundaries(protraction))));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a dynamics element to the dynamicsMap
     * @param date
     * @param volume a numeric or literal string, the latter should have a corresponding dynamicsDef in the dynamicsStyles/styleDef
     * @param id
     * @return the index at which it has been inserted
     */
    public int addDynamics(double date, String volume, String id) {
        Element e = new Element("dynamics", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("volume", volume));
        e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a dynamics element to the dynamicsMap
     * @param date
     * @param volume a numeric or literal string, the latter should have a corresponding dynamicsDef in the dynamicsStyles/styleDef
     * @return the index at which it has been inserted
     */
    public int addDynamics(double date, String volume) {
        Element e = new Element("dynamics", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("volume", volume));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a dynamics element to the dynamicsMap
     * @param data xml data will be ignored (use addElement() instead to add an xml representation)
     * @return
     */
    public int addDynamics(DynamicsData data) {
        Element e = new Element("dynamics", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(data.startDate)));

        if (data.volumeString != null)
            e.addAttribute(new Attribute("volume", data.volumeString));
        else if (data.volume != null)
            e.addAttribute(new Attribute("volume", Double.toString(data.volume)));
        else {
            System.err.println("Cannot add dynamics, volume not specified.");
            return -1;
        }

        if (data.transitionToString != null)
            e.addAttribute(new Attribute("transition.to", data.transitionToString));
        else if (data.transitionTo != null)
            e.addAttribute(new Attribute("transition.to", Double.toString(data.transitionTo)));

        if (data.curvature != null) {
            data.curvature = DynamicsMap.ensureCurvatureBoundaries(data.curvature);
            e.addAttribute(new Attribute("curvature", Double.toString(data.curvature)));
        }

        if (data.protraction != null) {
            data.protraction = DynamicsMap.ensureProtractionBoundaries(data.protraction);
            e.addAttribute(new Attribute("protraction", Double.toString(data.protraction)));
        }

        if (data.subNoteDynamics)   // sadd and set this attribute only if it is true
            e.addAttribute(new Attribute("subNoteDynamics", "true"));

        if (data.xmlId != null)
            e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", data.xmlId));

        KeyValue<Double, Element> kv = new KeyValue<>(data.startDate, e);
        return this.insertElement(kv, false);
    }

    /**
     * this little helper method makes sure that the curvature value is valid
     * @param curvature
     * @return
     */
    private static double ensureCurvatureBoundaries(double curvature) {
        if (curvature < 0.0) {
            System.err.println("Invalid curvature value: " + curvature + " < 0.0. Setting it to 0.0.");
            return 0.0;
        }
        else if (curvature > 1.0) {
            System.err.println("Invalid curvature value: " + curvature + " > 1.0. Setting it to 1.0.");
            return 1.0;
        }
        return curvature;
    }

    /**
     * this little helper method makes sure that the protraction value is valid
     * @param protraction
     * @return
     */
    private static double ensureProtractionBoundaries(double protraction) {
        if (protraction < -1.0) {
            System.err.println("Invalid protraction value: " + protraction + " < -1.0. Setting it to -1.0.");
            return -1.0;
        }
        else if (protraction > 1.0) {
            System.err.println("Invalid protraction value: " + protraction + " > 1.0. Setting it to 1.0.");
            return 1.0;
        }
        return protraction;
    }

    /**
     * collect all data that is needed to compute the dynamics at the specified date
     * @param date
     * @return
     */
    private DynamicsData getDynamicsDataAt(double date){
        for (int i = this.getElementIndexBeforeAt(date); i >= 0; --i) {
            DynamicsData dd = this.getDynamicsDataOf(i);
            if (dd != null)
                return dd;
        }
        return null;
    }

    /**
     * this collects the dynamics data of a specific element of this dynamicsMap, given via the index
     * @param index
     * @return the dynamics data or null if the indexed element is invalid, if the index is lower than 0 or the dynamicsMap is empty default dynamics data is returned
     */
    private DynamicsData getDynamicsDataOf(int index) {
        if (this.elements.isEmpty() || (index < 0))
            return null;

        if (index >= this.elements.size())
            index = this.elements.size() - 1;

        Element e = this.elements.get(index).getValue();
        if (e.getLocalName().equals("dynamics")) {                                      // and it is a dynamics element
            DynamicsData dd = new DynamicsData();

            dd.startDate = this.elements.get(index).getKey();;                          // store the start date of the dynamics instruction
            dd.endDate = this.getEndDate(index);                                        // get the date of the subsequent dynamics element
            dd.xml = e;

            Attribute att = Helper.getAttribute("xml:id", e);
            if (att != null)
                dd.xmlId = att.getValue();

            // get the style that applies to this date
            for (int j = index; j >= 0; --j) {                                          // find the first style switch at or before date
                Element s = this.elements.get(j).getValue();
                if (s.getLocalName().equals("style")) {
                    dd.styleName = Helper.getAttributeValue("name.ref", s);
                    break;
                }
            }
            dd.style = (DynamicsStyle) this.getStyle(Mpm.DYNAMICS_STYLE, dd.styleName);      // read the dynamics style
            if (dd.style != null) {
                att = Helper.getAttribute("name.ref", e);
                if (att != null) {                                                          // name.ref attribute is mandatory
                    dd.dynamicsDefString = att.getValue();
                    dd.dynamicsDef = dd.style.getDef(dd.dynamicsDefString);
                }
            }

            att = Helper.getAttribute("volume", e);
            if (att == null)                                                            // if there is no volume defined in this dynamics element
                return null;                                                            // search on
            dd.volumeString = att.getValue();
            dd.volume = DynamicsStyle.getNumericValue(dd.volumeString, dd.style);

            att = Helper.getAttribute("transition.to", e);
            if (att != null) {
                dd.transitionToString = att.getValue();
                dd.transitionTo = DynamicsStyle.getNumericValue(dd.transitionToString, dd.style);

                att = Helper.getAttribute("curvature", e);
                if (att != null)
                    dd.curvature = DynamicsMap.ensureCurvatureBoundaries(Double.parseDouble(att.getValue()));

                att = Helper.getAttribute("protraction", e);
                if (att != null)
                    dd.protraction = DynamicsMap.ensureProtractionBoundaries(Double.parseDouble(att.getValue()));
            } else {                                                                    // this is to enable sub-note dynamics in constant dynamics segments (quite a special case and of minor practical relevance but with this is is possible)
                dd.transitionToString = dd.volumeString;
                dd.transitionTo = dd.volume;
                dd.curvature = 0.0;
                dd.protraction = 0.0;
            }

            att = Helper.getAttribute("subNoteDynamics", e);                            // read sub-note dynamics
            if (att != null)
                dd.subNoteDynamics = Boolean.parseBoolean(att.getValue());
            else
                dd.subNoteDynamics = false;

            return dd;
        }
        return null;
    }

    /**
     * a helper method to retrieve the date at which the indexed dynamics instruction ends, which is either the date of the subsequent instruction or Double.MAX_VALUE
     * @param index index of the current dynamics instruction for which the endDate is needed
     * @return
     */
    private double getEndDate(int index) {
        // get the date of the subsequent dynamics element
        double endDate = Double.MAX_VALUE;
        for (int j = index+1; j < this.elements.size(); ++j) {
            if (this.elements.get(j).getValue().getLocalName().equals("dynamics")) {
                endDate = this.elements.get(j).getKey();
                break;
            }
        }
        return endDate;
    }

    /**
     * On the basis of this dynamicsMap, add the corresponding dynamics data to all note elements of the specified map.
     * Basically, that map should be an MSM score.
     * @param map
     * @return the channelVolumeMap with su-note dynamics data or null if there is none; this channelVolumeMap should be added to the MSM part
     */
    public GenericMap renderDynamicsToMap(GenericMap map) {
        if ((map == null) || this.elements.isEmpty())
            return null;

        // a channelVolumeMap will get the channelVolume events for sub-note dynamics
        GenericMap chanVolMap = GenericMap.createGenericMap("channelVolumeMap");    // create a new channelVolumeMap

        int mapIndex = 0;
        for (int dynamicsIndex = 0; dynamicsIndex < this.size(); ++dynamicsIndex) {
            DynamicsData dd = this.getDynamicsDataOf(dynamicsIndex);
            if (dd == null)
                continue;

            if (chanVolMap != null) {                                                           // if we have a non-null channelVolumeMap
                // generate an MSM representation of the series of volume/expression controller events for sub-note dynamics
                if (dd.subNoteDynamics && (dynamicsIndex < (this.size() - 1))) {                // if sub-note dyynamics is active for this dynamics instruction and this is not the last dynamics instruction in the dynamicsMap
                    DynamicsMap.generateSubNoteDynamics(dd, chanVolMap);                        // render this dynamics instruction's curve segment to the channelVolumeMap

                    for (; mapIndex < map.size(); ++mapIndex) {                                 // traverse the map elements
                        KeyValue<Double, Element> mapEntry = map.elements.get(mapIndex);        // get the current map entry
                        if ((mapEntry.getKey() < dd.startDate)                                  // if this map entry is before the current dynamics
                                || !mapEntry.getValue().getLocalName().equals("note"))          // or if the map entry is no note element
                            continue;                                                           // leave it unaltered and go on until we are at or after the dynamics' date and it is a note element
                        if (mapEntry.getKey() >= dd.endDate)                                    // if the current map element is out of the scope of the current dynamics data
                            break;                                                              // stop here and find the next dynamics element first before continuing
                        mapEntry.getValue().addAttribute(new Attribute("velocity", "100.0"));   // all velocities are set to 100.0, loudness is controlled via a series of controller events
                    }

                    continue;
                }

                // the remainder is for non-sub-note dynamics

                // regardless of constant or continuous dynamics transition, with no sub-note dynamics do this;
                // and this is also done when we reached the last dynamics instruction because creating a sub-note dynamics series of events until eternity (Double.MAX_DOUBLE) makes no sense;
                // this way we also ensure that the channelVolume of the MIDI track will always be set to the default value of 100 at the end of the music
                if (chanVolMap.isEmpty() || !chanVolMap.getLastElement().getAttributeValue("value").equals("100.0")) {
                    Element e = new Element("volume", Mpm.MPM_NAMESPACE);                       // create an entry in the channelVolumeMap
                    e.addAttribute(new Attribute("date", Double.toString(dd.startDate)));       // at the date of this non-sub-note dynamics instruction
                    e.addAttribute(new Attribute("value", "100.0"));                            // set the channel volume slider to default 100
                    e.addAttribute(new Attribute("mandatory", "true"));                         // make sure this one will mandatorily be rendered to MIDI during MSM to MIDI export
                    chanVolMap.addElement(e);
                }
            }

            // apply dynamics to the map elements' velocity
            for (; mapIndex < map.size(); ++mapIndex) {                                         // traverse the map elements
                KeyValue<Double, Element> mapEntry = map.elements.get(mapIndex);                // get the current map entry
                if (!mapEntry.getValue().getLocalName().equals("note"))                         // if the map entry is no note element
                    continue;                                                                   // leave it unaltered and go on until we are at or after the dynamics' date and it is a note element
                if (mapEntry.getKey() < dd.startDate) {                                         // if the map entry is before the current dynamics instruction
                    mapEntry.getValue().addAttribute(new Attribute("velocity", "100.0"));       // create and set attribute velocity at the note element with a default value
                    continue;
                }
                if (mapEntry.getKey() >= dd.endDate)                                            // if the current map element is out of the scope of the current dynamics data
                    break;                                                                      // stop here and find the next dynamics element first before continuing
                mapEntry.getValue().addAttribute(new Attribute("velocity", Double.toString(dd.getDynamicsAt(mapEntry.getKey()))));  // create and set attribute velocity at the note element
            }
        }

        return chanVolMap;      // return the channelVolumeMap
    }

    /**
     * On the basis of the specified dynamicsMap, add the corresponding dynamics data to all note elements of the specified map.
     * Basically, that map should be an MSM score because only note elements will be processed.
     * @param map
     * @param dynamicsMap
     * @return the channelVolumeMap with su-note dynamics data or null if there is none; this channelVolumeMap should be added to the MSM part
     */
    public static GenericMap renderDynamicsToMap(GenericMap map, DynamicsMap dynamicsMap) {
        if (dynamicsMap != null) {
            return dynamicsMap.renderDynamicsToMap(map);
        }

        if (map == null)
            return null;

        // if no dynamicsMap is given, set default velocity for all note elements
        for (int i=0; i < map.size(); ++i) {
            Element e = map.getElement(i);

            if (!e.getLocalName().equals("note"))
                continue;

            e.addAttribute(new Attribute("velocity", "100.0"));
        }
        return null;
    }

    /**
     * a helper method for the implementation of sub-note dynamics,
     * it generates a series volume events (the MSM pendant to the eponimous MIDI events) and adds them to the specified channelVolumeMap,
     * the basis for this is the specified DynamicsData object
     * @param dynamicsData
     * @param channelVolumeMap
     */
    private static void generateSubNoteDynamics(DynamicsData dynamicsData, GenericMap channelVolumeMap) {
        ArrayList<double[]> subNoteDynamicsSegment = dynamicsData.getSubNoteDynamicsSegment(2.0);
        ArrayList<Element> es = new ArrayList<>();

        for (double[] event : subNoteDynamicsSegment) {
            Element e = new Element("volume", Mpm.MPM_NAMESPACE);
            e.addAttribute(new Attribute("date", Double.toString(event[0])));
            e.addAttribute(new Attribute("value", Double.toString(event[1])));
//            System.out.println(e.toXML());
            channelVolumeMap.addElement(e);
            es.add(e);
        }

        es.get(0).addAttribute(new Attribute("mandatory", "true"));                 // the first element is marked as mandatory, so the MSM to MIDI export will generate them even if CONTROL_CHANGE_DENSITY is set coarser
//        es.get(es.size() - 1).addAttribute(new Attribute("mandatory", "true"));     // the last is marked mandatory only if we get back to non-sub-note dynamics, this is done in method renderDynamicsToMap()

//        System.out.println("Generated " + channelVolumeMap.size() + " sub-note dynamics events.");
    }
}
