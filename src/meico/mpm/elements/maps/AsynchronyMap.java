package meico.mpm.elements.maps;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.supplementary.KeyValue;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;

/**
 * This class interfaces MPM's asynchronyMaps
 * @author Axel Berndt
 */
public class AsynchronyMap extends GenericMap {
    /**
     * constructor, generates an empty AsynchronyMap
     * @throws Exception
     */
    private AsynchronyMap() throws Exception {
        super("asynchronyMap");
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws Exception
     */
    private AsynchronyMap(Element xml) throws Exception {
        super(xml);
    }

    /**
     * AsynchronyMap factory
     * @return
     */
    public static AsynchronyMap createAsynchronyMap() {
        AsynchronyMap d;
        try {
            d = new AsynchronyMap();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * AsynchronyMap factory
     * @param xml
     * @return
     */
    public static AsynchronyMap createAsynchronyMap(Element xml) {
        AsynchronyMap d;
        try {
            d = new AsynchronyMap(xml);
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
        this.setType("asynchronyMap");            // make sure this is really a "asynchronyMap"
    }

    /**
     * add an asynchrony element to the map
     * @param date
     * @param millisecondsOffset
     * @return
     */
    public int addAsynchrony(double date, double millisecondsOffset) {
        Element e = new Element("asynchrony", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("milliseconds.offset", Double.toString(millisecondsOffset)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * get the asynchrony, i.e. thie milliseconds offset, at the specified date
     * @param date
     * @return
     */
    public double getAsynchronyAt(double date) {
        int i = this.getElementIndexBeforeAt(date);
        if (i < 0)
            return 0.0;

        while (!this.elements.get(i).getValue().getLocalName().equals("asynchrony"))
            if (--i < 0)
                return 0.0;

        return Double.parseDouble(Helper.getAttributeValue("milliseconds.offset", this.elements.get(i).getValue()));

//        for (int i = this.elements.size() - 1; i >= 0; --i) {
//            if ((this.elements.get(i).getKey() <= date)                                     // found the element before or at date
//                && this.elements.get(i).getValue().getLocalName().equals("asynchrony")) {   // and it is a asynchrony element
//
//                return Double.parseDouble(Helper.getAttributeValue("milliseconds.offset", this.elements.get(i).getValue());
//            }
//        }
//        return 0.0;
    }

    /**
     * on the basis of this asynchronyMap, add the corresponding offsets to the millisecond.date and millisecond.date.end attributes of each map element
     * @param map This map's elements must have attributes millisecond.date and millisecond.date.end! This means, it must have been processed by TempoMap.addMillisecondsToMap() before it can be processed here.
     */
    public void renderAsynchronyToMap(GenericMap map) {
        if ((map == null) || this.elements.isEmpty())
            return;

        ArrayList<KeyValue<Double, Element>> mapEntries = new ArrayList<>(map.getAllElements());        // make a list of all map entries; we will remove every entry from this list once it is completely processed
        ArrayList<KeyValue<Double, Element>> done = new ArrayList<>();

        for (int asynIndex = 0; asynIndex < this.size(); ++asynIndex) {                                 // traverse the asynchronyMap elements
            double asynEndDate = (asynIndex < (this.elements.size() - 1)) ? this.elements.get(asynIndex + 1).getKey() : Double.MAX_VALUE;   // get the date of the subsequent asynchrony element or (if we are at the last element) get the largest possible value
            double offset = Double.parseDouble(Helper.getAttributeValue("milliseconds.offset", this.getElement(asynIndex)));            // get the offset

            for (int mapIndex = 0; mapIndex < mapEntries.size(); ++mapIndex) {                          // traverse the (remaining) map elements
                KeyValue<Double, Element> mapEntry = mapEntries.get(mapIndex);                          // get the current map entry

                if (mapEntry.getKey() >= asynEndDate)                                                   // if the current map element is out of the scope of the current asynchrony element
                    break;                                                                              // stop here and find the next asynchrony element first before continuing

                // process the entry's milliseconds date
                double startDateMs = 0.0;
                if (mapEntry.getKey() >= this.elements.get(asynIndex).getKey()) {                       // if the entry starts at or after the asynchrony instruction
                    Attribute att = Helper.getAttribute("milliseconds.date", mapEntry.getValue());      // get the map element's milliseconds.date attribute
                    if (att != null) {                                                                  // if we have an attribute date
                        startDateMs = Double.parseDouble(att.getValue()) + offset;                      // parse the attribute value to double and add the offset
                        att.setValue(Double.toString(startDateMs));                                     // write the updated value to the attribute
                    }
                }

                // process the entry's milliseconds end date
                Attribute dur = Helper.getAttribute("duration", mapEntry.getValue());                   // get the duration attribute
                if (dur == null) {                                                                      // without a duration attribute we cannot say it the entry's end date falls into the scope of the current or any asynchrony segment
                    done.add(mapEntry);                                                                 // hence, we are done with this map entry
                    continue;
                }

                double end = Double.parseDouble(dur.getValue()) + mapEntry.getKey();                    // use the duration to compute the tick end date
                if (end >= asynEndDate)                                                                 // if it is after the current asynchrony segment
                    continue;                                                                           // keep the entry for later processing, go on with the next entry

                if (end >= this.elements.get(asynIndex).getKey()) {                                     // if the entry's end date falls in the scope of the current asynchrony segment
                    Attribute att = Helper.getAttribute("milliseconds.date.end", mapEntry.getValue());  // get the map element's milliseconds.date.end attribute
                    if (att != null) {                                                                  // if we have said attribute
                        double ms = Double.parseDouble(att.getValue()) + offset;                        // parse the attribute value to double and add the offset
                        att.setValue(Double.toString(Math.max(ms, startDateMs+1)));                      // write the updated value to the attribute; however, we do not shift the end date before the start date; in that case we set it to the start date + 1ms
                    }
                }

                done.add(mapEntry);                                                                     // we are done with this entry, so it can be removed from the list
            }

            // remove all map entries that are finished, so the next iteration does process only the remaining entries
            for (KeyValue<Double, Element> removeMe : done)
                mapEntries.remove(removeMe);
            done.clear();
        }
    }

    /**
     * on the basis of the specified asynchronyMap, add the corresponding offsets to the millisecond.date and millisecond.date.end attributes of each map element
     * @param map This map's elements must have attributes millisecond.date and millisecond.date.end! This means, it must have been processed by TempoMap.addMillisecondsToMap() before it can be processed here.
     * @param asynchronyMap
     */
    public static void renderAsynchronyToMap(GenericMap map, AsynchronyMap asynchronyMap) {
        if (asynchronyMap != null)
            asynchronyMap.renderAsynchronyToMap(map);
    }
}
