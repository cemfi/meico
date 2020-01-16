package meico.mpm.elements.maps;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.styles.GenericStyle;
import meico.mpm.elements.styles.MetricalAccentuationStyle;
import meico.supplementary.KeyValue;
import meico.mpm.elements.maps.data.MetricalAccentuationData;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;

/**
 * This class interfaces MPM's metricalAccentuationMaps
 * @author Axel Berndt
 */
public class MetricalAccentuationMap extends GenericMap {
    /**
     * constructor, generates an empty MetricalAccentuationMap
     * @throws InvalidDataException
     */
    private MetricalAccentuationMap() throws InvalidDataException {
        super("metricalAccentuationMap");
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws InvalidDataException
     */
    private MetricalAccentuationMap(Element xml) throws InvalidDataException {
        super(xml);
    }

    /**
     * MetricalAccentuationMap factory
     * @return
     */
    public static MetricalAccentuationMap createMetricalAccentuationMap() {
        MetricalAccentuationMap d;
        try {
            d = new MetricalAccentuationMap();
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * MetricalAccentuationMap factory
     * @param xml
     * @return
     */
    public static MetricalAccentuationMap createMetricalAccentuationMap(Element xml) {
        MetricalAccentuationMap d;
        try {
            d = new MetricalAccentuationMap(xml);
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
        this.setType("metricalAccentuationMap");            // make sure this is really a "metricalAccentuationMap"
    }

    /**
     * add an accentuationPattern element to the map
     * @param date
     * @param accentuationPatternDefName a reference to an accentuationPatternDef
     * @param scale
     * @param loop
     * @return
     */
    public int addAccentuationPattern(double date, String accentuationPatternDefName, double scale, boolean loop) {
        Element e = new Element("accentuationPattern", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("name.ref", accentuationPatternDefName));
        e.addAttribute(new Attribute("scale", Double.toString(scale)));
        e.addAttribute(new Attribute("loop", Boolean.toString(loop)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add an accentuationPattern element to the map
     * @param date
     * @param accentuationPatternDefName a reference to an accentuationPatternDef
     * @param scale
     * @return
     */
    public int addAccentuationPattern(double date, String accentuationPatternDefName, double scale) {
        Element e = new Element("accentuationPattern", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("name.ref", accentuationPatternDefName));
        e.addAttribute(new Attribute("scale", Double.toString(scale)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add an accentuationPattern element to the map
     * @param data xml data will be ignored (use addElement() instead to add an xml representation)
     * @return
     */
    public int addAccentuationPattern(MetricalAccentuationData data) {
        Element e = new Element("accentuationPattern", Mpm.MPM_NAMESPACE);

        if (data.accentuationPatternDef == null) {
            System.err.println("Cannot add accentuationPattern, accentuationPatternDef not specified.");
            return -1;
        }

        e.addAttribute(new Attribute("date", Double.toString(data.startDate)));

        if (data.xmlId != null)
            e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", data.xmlId));

        if (data.accentuationPatternDef != null)
            e.addAttribute(new Attribute("name.ref", data.accentuationPatternDef.getName()));
        else if (data.accentuationPatternDefName != null)
            e.addAttribute(new Attribute("name.ref", data.accentuationPatternDefName));

        e.addAttribute(new Attribute("scale", Double.toString(data.scale)));
        e.addAttribute(new Attribute("loop", Boolean.toString(data.loop)));

        KeyValue<Double, Element> kv = new KeyValue<>(data.startDate, e);
        return this.insertElement(kv, false);
    }

    /**
     * get the accentuation data for a specified date
     * @param date
     * @return
     */
    private MetricalAccentuationData getMetricalAccentuationDataAt(double date) {
        for (int i = this.getElementIndexBeforeAt(date); i >= 0; --i) {
            MetricalAccentuationData ad = this.getMetricalAccentuationDataOf(i);
            if (ad != null)
                return ad;
        }
        return null;
    }

    /**
     * this collects the metrical accentuation data of a specified element in this map, given via the index number
     * @param index
     * @return the metrical accentuation data or null if the indexed element is invalid (or a style switch)
     */
    private MetricalAccentuationData getMetricalAccentuationDataOf(int index) {
        if (this.elements.isEmpty() || (index < 0))
            return null;

        if (index >= this.elements.size())
            index = this.elements.size() - 1;

        Element e = this.elements.get(index).getValue();
        if (e.getLocalName().equals("accentuationPattern")) {                   // it is a accentuationPattern element
            MetricalAccentuationData md = new MetricalAccentuationData();

            Attribute accentuationPatternDefAtt = Helper.getAttribute("name.ref", e);
            if (accentuationPatternDefAtt == null)
                return null;
            md.accentuationPatternDefName = accentuationPatternDefAtt.getValue();

            Attribute scaleAtt = Helper.getAttribute("scale", e);
            if (scaleAtt == null)
                return null;
            md.scale = Double.parseDouble(scaleAtt.getValue());

            md.startDate = this.elements.get(index).getKey();
            md.endDate = this.getEndDate(index);                                // get the date of the subsequent accentuationPattern element
            md.xml = e;

            Attribute att = Helper.getAttribute("xml:id", e);
            if (att != null)
                md.xmlId = att.getValue();

            att = Helper.getAttribute("loop", e);
            if (att != null)
                md.loop = Boolean.parseBoolean(att.getValue());

            att = Helper.getAttribute("stickToMeasures", e);
            if (att != null)
                md.stickToMeasures = Boolean.parseBoolean(att.getValue());

            // get the style that applies to this date
            md.styleName = "";
            for (int j = index; j >= 0; --j) {                                  // find the first style switch at or before date
                Element s = this.elements.get(j).getValue();
                if (s.getLocalName().equals("style")) {
                    md.styleName = Helper.getAttributeValue("name.ref", s);
                    break;
                }
            }
            GenericStyle gStyle = this.getStyle(Mpm.METRICAL_ACCENTUATION_STYLE, md.styleName); // read the metrical accentuation style
            if (gStyle != null) {
                md.style = (MetricalAccentuationStyle) gStyle;
                md.accentuationPatternDef = md.style.getAccentuationPatternDef(md.accentuationPatternDefName);
                return md;      // only if we have accentuationPatternDef data we have what we need for working with the accentuation pattern
            }
        }

        return null;
    }

    /**
     * a helper method to retrieve the date at which the indexed accentuationPattern instruction ends, which is either the date of the subsequent instruction or Double.MAX_VALUE
     * @param index index of the current accentuationPattern instruction for which the end date is needed
     * @return
     */
    private double getEndDate(int index) {
        // get the date of the subsequent accentuationPattern element
        double endDate = Double.MAX_VALUE;
        for (int j = index+1; j < this.elements.size(); ++j) {
            if (this.elements.get(j).getValue().getLocalName().equals("accentuationPattern")) {
                endDate = this.elements.get(j).getKey();
                break;
            }
        }
        return endDate;
    }

    /**
     * on the basis of this metricalAccentuationMap, apply the accentuations to all velocity attributes of each map element
     * @param map the map (preferably an MSM score) to which the metrical accentuations should be applied
     * @param timeSignatureMap
     * @param ppq
     */
    public void renderMetricalAccentuationToMap(GenericMap map, GenericMap timeSignatureMap, int ppq){
        if ((map == null) || this.elements.isEmpty())
            return;

        double ppq4 = 4.0 * ppq;
        int timeSignIndex = -1;
        double tsDate = 0.0;
        double tsNumerator = 4.0;
        int tsDenominator = 4;
        double ticksPerBeat = ppq;
        double tickLengthOfOneMeasure = ticksPerBeat * tsNumerator;

        int mapIndex = 0;
        for (int accIndex = 0; accIndex < this.size(); ++accIndex) {
            MetricalAccentuationData md = this.getMetricalAccentuationDataOf(accIndex);
            if (md == null)
                continue;

            double patternLengthTicks = (md.accentuationPatternDef.getLength() * ppq4) / tsDenominator;

            for (; mapIndex < map.size(); ++mapIndex) {                             // traverse the map elements
                KeyValue<Double, Element> mapEntry = map.elements.get(mapIndex);    // get the current map entry

                if (mapEntry.getKey() < md.startDate)                               // if this map entry is before the current accentuationPattern
                    continue;                                                       // go on until we are at of after the accentuationPattern's date

                Attribute velocityAtt = Helper.getAttribute("velocity", mapEntry.getValue());           // get the velocity attribute (there must be one after dynamics has been rendered)
                if (velocityAtt == null)                                                                // if this element has no velocity attribute (e.g. if it is a rest element)
                    continue;                                                                           // go on with the next

                // we need to make sure that the time signature data is still up to date
                if (timeSignatureMap != null) {
                    boolean update = false;
                    for (int tsIndex = timeSignIndex + 1; tsIndex < timeSignatureMap.size(); ++tsIndex) {
                        if (timeSignatureMap.getAllElements().get(tsIndex).getKey() > mapEntry.getKey())
                            break;
                        timeSignIndex = tsIndex;
                        update = true;
                    }
                    if (update) {
                        KeyValue<Double, Element> timeSign = timeSignatureMap.getAllElements().get(timeSignIndex);
                        tsDate = timeSign.getKey();
                        tsNumerator = Double.parseDouble(Helper.getAttributeValue("numerator", timeSign.getValue()));
                        tsDenominator = Integer.parseInt(Helper.getAttributeValue("denominator", timeSign.getValue()));
                        ticksPerBeat = ppq4 / tsDenominator;
                        tickLengthOfOneMeasure = ticksPerBeat * tsNumerator;
                        patternLengthTicks = (md.accentuationPatternDef.getLength() * ppq4) / tsDenominator;
                    }
                }

                if ((mapEntry.getKey() >= md.endDate)                                                   // if the current map element is out of the scope of the current accentuationPattern element
                        || (!md.loop && (mapEntry.getKey() >= (md.startDate + patternLengthTicks))))    // if this is a oneshot accentuationPattern and map entry is already after its end
                    break;                                                                              // stop here and find the next accentuationPattern element first before continuing

                double beat;
                if (md.stickToMeasures)
                    beat = 1.0 + ((mapEntry.getKey() - tsDate) % tickLengthOfOneMeasure) / ticksPerBeat;// get the beat position of the event
                else
                    beat = 1.0 + ((mapEntry.getKey() - tsDate) % patternLengthTicks) / ticksPerBeat;    // get the beat position of the event

                double velocity = Double.parseDouble(velocityAtt.getValue());                           // get the current velocity value
                double scale = md.scale;
                double accentuation = md.accentuationPatternDef.getAccentuationAt(beat);                // compute accentuation

                // reduce the scale when it exceeds the (MIDI) limits, this is treated differently during MSM to expressive MIDI rendering
//                double lowerVelocityLimit = 0.0;
//                double upperVelocityLimit = 127.0;
//                if ((Math.signum(accentuation) > 0.0) && ((velocity + scale) > upperVelocityLimit))
//                    scale = upperVelocityLimit - velocity;
//                else if ((Math.signum(accentuation) < 0.0) && ((velocity - scale) < lowerVelocityLimit))
//                    scale = velocity - lowerVelocityLimit;

                velocityAtt.setValue(Double.toString((velocity + (accentuation * scale))));              // add the accentuation and set the velocity attribute
            }
        }
    }

    /**
     * on the basis of the specified metricalAccentuationMap, apply the accentuations to all velocity attributes of each map element;
     * @param map
     * @param metricalAccentuationMap
     * @param timeSignatureMap
     * @param ppq
     */
    public static void renderMetricalAccentuationToMap(GenericMap map, MetricalAccentuationMap metricalAccentuationMap, GenericMap timeSignatureMap, int ppq) {
        if (metricalAccentuationMap != null)
            metricalAccentuationMap.renderMetricalAccentuationToMap(map, timeSignatureMap, ppq);
    }
}