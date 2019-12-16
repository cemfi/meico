package meico.mpm.elements.maps;

import com.sun.media.sound.InvalidDataException;
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
     * @throws InvalidDataException
     */
    private AsynchronyMap() throws InvalidDataException {
        super("asynchronyMap");
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws InvalidDataException
     */
    private AsynchronyMap(Element xml) throws InvalidDataException {
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
        } catch (InvalidDataException e) {
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

        ArrayList<KeyValue<Double[], Attribute>> pendingDurations = new ArrayList<>();
        int mapIndex = map.getElementIndexAtAfter(this.elements.get(0).getKey());   // we start with the first map element at or after the first

        for (int asynIndex = 0; asynIndex < this.size(); ++asynIndex) {             // traverse the asynchronyMap elements
            double asynEndDate = (asynIndex < (this.elements.size() - 1)) ? this.elements.get(asynIndex + 1).getKey() : Double.MAX_VALUE;   // get the date of the subsequent asynchrony element or (if we are at the last element) get the largest possible value
            double offset = Double.parseDouble(Helper.getAttributeValue("milliseconds.offset", this.getElement(asynIndex)));            // get the offset

            for (; mapIndex < map.size(); ++mapIndex) {                             // traverse the map elements
                KeyValue<Double, Element> mapEntry = map.elements.get(mapIndex);    // get the current map entry

                if (mapEntry.getKey() >= asynEndDate)                               // if the current map element is out of the scope of the current asynchrony element
                    break;                                                          // stop here and find the next asynchrony element first before continuing

                // add offset to milliseconds.date
                Attribute att = Helper.getAttribute("milliseconds.date", mapEntry.getValue());  // get the map element's milliseconds.date attribute
                if (att == null)                                                    // if it has none
                    continue;                                                       // continue with the next map element
                double ms = Double.parseDouble(att.getValue()) + offset;            // parse the attribute to double and add the offset
                att.setValue(Double.toString(ms));                                  // write the updated value to the attribute

                // duration needs to be updated, too, but only if it is within the scope of this asnchrony element
                Attribute dateEndAtt = Helper.getAttribute("date.end", mapEntry.getValue());
                Attribute msDateEndAtt = Helper.getAttribute("milliseconds.date.end", mapEntry.getValue()); // get the map element's milliseconds.date.end attribute
                if ((dateEndAtt == null) || (msDateEndAtt == null))                                         // if it misses one the required attributes
                    continue;                                                                               // we are done with this map element
                double dateEnd = Double.parseDouble(dateEndAtt.getValue());                                 // get the tick date of the end of the map element
                double msDateEnd = Double.parseDouble(msDateEndAtt.getValue());                             // parse the milliseconds.date.end attribute to double

                pendingDurations.add(new KeyValue<>(new Double[]{dateEnd, msDateEnd}, msDateEndAtt));       // keep the attribute in the pendingDurations list to get back to it later
            }

            // add offset to milliseconds.date.end attributes
            for (int i=0; i < pendingDurations.size(); ++i) {
                KeyValue<Double[], Attribute> pd = pendingDurations.get(i);
                double dateEnd = pd.getKey()[0];
                if (dateEnd >= asynEndDate)                                     // check whether date.end falls into the scope of this asynchrony element
                    continue;

                double msDateEnd = pd.getKey()[1] + offset;
                pd.getValue().setValue(Double.toString(msDateEnd));

                pendingDurations.remove(pd);
                --i;
            }
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
