package meico.mpm.elements.maps;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.styles.TempoStyle;
import meico.supplementary.KeyValue;
import meico.mpm.elements.maps.data.TempoData;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;

/**
 * This class interfaces MPM's tempoMaps
 * @author Axel Berndt
 */
public class TempoMap extends GenericMap {
    /**
     * constructor, generates an empty tempoMap
     * @throws Exception
     */
    private TempoMap() throws Exception {
        super("tempoMap");
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws Exception
     */
    private TempoMap(Element xml) throws Exception {
        super(xml);
    }

    /**
     * TempoMap factory
     * @return
     */
    public static TempoMap createTempoMap() {
        TempoMap d;
        try {
            d = new TempoMap();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * TempoMap factory
     * @param xml
     * @return
     */
    public static TempoMap createTempoMap(Element xml) {
        TempoMap d;
        try {
            d = new TempoMap(xml);
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
        this.setType("tempoMap");            // make sure this is really a "tempoMap"
    }

    /**
     * add a tempo element to the map
     * @param date
     * @param bpm
     * @param transitionTo
     * @param beatLength
     * @param meanTempoAt
     * @param id
     * @return the index at which it has been inserted
     */
    public int addTempo(double date, String bpm, String transitionTo, double beatLength, double meanTempoAt, String id) {
        Element e = new Element("tempo", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("bpm", bpm));
        e.addAttribute(new Attribute("transition.to", transitionTo));
        e.addAttribute(new Attribute("beatLength", Double.toString(beatLength)));
        e.addAttribute(new Attribute("meanTempoAt", Double.toString(meanTempoAt)));
        e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a tempo element to the map
     * @param date
     * @param bpm
     * @param transitionTo
     * @param beatLength
     * @param meanTempoAt
     * @return the index at which it has been inserted
     */
    public int addTempo(double date, String bpm, String transitionTo, double beatLength, double meanTempoAt) {
        Element e = new Element("tempo", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("bpm", bpm));
        e.addAttribute(new Attribute("transition.to", transitionTo));
        e.addAttribute(new Attribute("beatLength", Double.toString(beatLength)));
        e.addAttribute(new Attribute("meanTempoAt", Double.toString(meanTempoAt)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a tempo element to the map
     * @param date
     * @param bpm a numeric or literal string, the latter should have a corresponding tempoDef in the tempoStyles/styleDef
     * @param beatLength a numeric or literal string, the latter should have a corresponding tempoDef in the tempoStyles/styleDef
     * @return the index at which it has been inserted
     */
    public int addTempo(double date, String bpm, double beatLength) {
        Element e = new Element("tempo", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("bpm", bpm));
        e.addAttribute(new Attribute("beatLength", Double.toString(beatLength)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add a tempo element to the map
     * @param data xml data will be ignored (use addElement() instead to add an xml representation)
     * @return
     */
    public int addTempo(TempoData data) {
        Element e = new Element("tempo", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(data.startDate)));

        if (data.bpmString != null)
            e.addAttribute(new Attribute("bpm", data.bpmString));
        else if (data.bpm != null)
            e.addAttribute(new Attribute("bpm", Double.toString(data.bpm)));
        else {
            System.err.println("Cannot add tempo, bpm not specified.");
            return -1;
        }

        if (data.transitionToString != null)
            e.addAttribute(new Attribute("transition.to", data.transitionToString));
        else if (data.transitionTo != null)
            e.addAttribute(new Attribute("transition.to", Double.toString(data.transitionTo)));

        if (data.meanTempoAt != null)
            e.addAttribute(new Attribute("meanTempoAt", Double.toString(data.meanTempoAt)));

        e.addAttribute(new Attribute("beatLength", Double.toString(data.beatLength)));

        if (data.xmlId != null)
            e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", data.xmlId));

        KeyValue<Double, Element> kv = new KeyValue<>(data.startDate, e);
        return this.insertElement(kv, false);
    }

    /**
     * collect all data that is needed to compute the tempo at the specified date
     * @param date
     * @return
     */
    private TempoData getTempoDataAt(double date){
        TempoData td;

        // retrieve tempo data from the tempoMap
        for (int i = this.getElementIndexBefore(date); i >= -1; --i) {
            td = this.getTempoDataOf(i);
            if (td != null)
                return td;
        }

        return null;
    }

    /**
     * this collects the tempo data of a specific element of this tempoMap, given via the index
     * @param index
     * @return the tempo data or null if the indexed element is invalid, if the index is lower than 0 or the tempoMap is empty default tempo data is returned
     */
    public TempoData getTempoDataOf(int index) {
        if (this.elements.isEmpty() || (index < 0))
            return null;

        if (index >= this.elements.size())
            index = this.elements.size() - 1;

        Element e = this.elements.get(index).getValue();
        if (e.getLocalName().equals("tempo")) {                                     // if it is a tempo element
            TempoData td = new TempoData();

            Attribute bpmAtt = Helper.getAttribute("bpm", e);
            if (bpmAtt == null)                                                     // if the tempo element defines no bpm
                return null;                                                        // it is invalid

            Attribute beatLengthAtt = Helper.getAttribute("beatLength", e);
            if (beatLengthAtt == null)                                              // if there is no beatLength defined in this tempo element
                return null;                                                        // search on

            td.startDate = this.elements.get(index).getKey();;                      // store the start date of the tempo instruction
            td.endDate = this.getEndDate(index);                                    // get the date of the subsequent tempo element
            td.xml = e;
            td.beatLength = Double.parseDouble(beatLengthAtt.getValue());

            Attribute att = Helper.getAttribute("xml:id", e);
            if (att != null)
                td.xmlId = att.getValue();

            // get the style that applies to this date
            for (int j = index; j >= 0; --j) {                                      // find the first style switch at or before date
                Element s = this.elements.get(j).getValue();
                if (s.getLocalName().equals("style")) {
                    td.styleName = Helper.getAttributeValue("name.ref", s);
                    break;
                }
            }
            TempoStyle gStyle = (TempoStyle) this.getStyle(Mpm.TEMPO_STYLE, td.styleName);     // read the tempo style
            if (gStyle != null)
                td.style = gStyle;

            td.bpmString = bpmAtt.getValue();
            td.bpm = TempoStyle.getNumericBpmValue(td.bpmString, td.style);         // get numeric tempo value

            att = Helper.getAttribute("transition.to", e);
            if (att != null) {
                td.transitionToString = att.getValue();
                td.transitionTo = TempoStyle.getNumericBpmValue(td.transitionToString, td.style);   // get numeric transition.to value

                if (td.transitionTo.equals(td.bpm)) {                               // if start tempo and target tempo are equal, we can simplify the data to constant tempo
                    td.transitionToString = null;
                    td.transitionTo = null;
                } else {                                                            // otherwise we need to parse some more attributes
                    att = Helper.getAttribute("meanTempoAt", e);
                    if (att != null) {
                        td.meanTempoAt = Double.parseDouble(att.getValue());
                        if (td.meanTempoAt <= 0.0) {                                // if meanTempoAt is smaller or equal to 0.0
                            td.bpmString = td.transitionToString;                   // we interpret the tempo instruction as constant tempo at the target value
                            td.bpm = td.transitionTo;
                            td.transitionToString = null;                           // no transition
                            td.transitionTo = null;
                        } else if (td.meanTempoAt >= 1.0) {                         // if meanTempoAt is greater or equal to 1.0
                            td.transitionToString = null;                           // we interpret the tempo instruction as constant tempo at the start value, not transition
                            td.transitionTo = null;
                        } else {                                                    // for any meaningfull value of meanTempoAt
                            td.exponent = TempoMap.computeExponent(td.meanTempoAt); // compute the exponent of the tempo curve
                        }
                    } else {                                                        // someone forgot to specify attribute meanTempoAt
                        td.meanTempoAt = 0.5;                                       // use default values
                        td.exponent = 1.0;
                    }
                }
            }
            return td;
        }
        return null;
    }

    /**
     * a helper method to retrieve the date at which the indexed tempo instruction ends, which is either the date of the subsequent instruction or Double.MAX_VALUE
     * @param index index of the current tempo instruction for which the endDate is needed
     * @return
     */
    private double getEndDate(int index) {
        // get the date of the subsequent tempo element
        double endDate = Double.MAX_VALUE;
        for (int j = index+1; j < this.elements.size(); ++j) {
            if (this.elements.get(j).getValue().getLocalName().equals("tempo")) {
                endDate = this.elements.get(j).getKey();
                break;
            }
        }
        return endDate;
    }

    /**
     * This method computes the exponent of the tempo curve segment as defined by one tempo instruction.
     * @param meanTempoAt the value of the meanTempoAt attribute should(!) be greater than 0.0 and smaller than 1.0.
     * @return the exponent of a continuous tempo transition function
     */
    private static double computeExponent(double meanTempoAt) {
//        if (meanTempoAt <= 0.0)         // values <= 0.0 should effectively set the starting tempo to the Value of the target tempo
//            return 0.0;
//        if (meanTempoAt >= 1.0)         // values >= 1.0 should effectively set the target tempo to the value of the starting tempo
//            return Double.MAX_VALUE;
        return Math.log(0.5) / Math.log(meanTempoAt);
    }

    /**
     * compute the tempo in bpm at the specified position according to this tempoMap
     * @param date
     * @return
     */
    public double getTempoAt(double date) {
        TempoData tempoData = this.getTempoDataAt(date);
        return TempoMap.getTempoAt(date, tempoData);
    }

    /**
     * compute the tempo in bpm from a given TempoData object and a date that should fall into the scope of the tempoData
     * @param date
     * @param tempoData the application should make sure that date is in the scope of tempoData
     * @return the tempo or 100.0 bpm if date lies out of scope or tempo data is insufficient
     */
    private static double getTempoAt(double date, TempoData tempoData) {
        if (tempoData == null)                                              // if no tempo data given
            return 100.0;                                                   // return default tempo 100.0 bpm

        // compute constant tempo
        if (tempoData.isConstantTempo())      // if no tempo defined to transition to or the value is equal to the start tempo, we have a constant tempo
            return tempoData.bpm;

        // compute continuous tempo transition
        if (date == tempoData.endDate)
            return tempoData.transitionTo;

        double result = (date - tempoData.startDate) / (tempoData.endDate - tempoData.startDate);
        result = Math.pow(result, tempoData.exponent);
        result = result * (tempoData.transitionTo - tempoData.bpm) + tempoData.bpm;
        return result;
    }

    /**
     * iterate through the specified map, compute the milliseconds dates for all elements and add the results as attributes milliseconds.date and milliseconds.date.end
     * @param map An instance of GenericMap, typically an MSM score element that has been parsed into such (e.g., GenericMap.createGenericMap(score)).
     * @param ppq the pulses per quarter timing resolution
     */
    public void renderTempoToMap(GenericMap map, int ppq) {
        if (map == null)
            return;

        int mapIndex = 0;

        // processing for the case of an empty tempoMap
        if (this.elements.isEmpty()) {
            for (; mapIndex < map.size(); ++mapIndex) {                                 // traverse the map elements
                KeyValue<Double, Element> mapEntry = map.elements.get(mapIndex);        // get the current map entry

                // convert the date
                double date = Double.parseDouble(Helper.getAttributeValue("date.perf", mapEntry.getValue()));
                double milliseconds = TempoMap.computeMillisecondsForNoTempo(date, ppq);
                mapEntry.getValue().addAttribute(new Attribute("milliseconds.date", Double.toString(milliseconds)));        // add the attribute

                // convert the duration
                Attribute durAtt = Helper.getAttribute("duration.perf", mapEntry.getValue());
                if (durAtt == null)
                    continue;
                double endDate = date + Double.parseDouble(durAtt.getValue());                                              // get the tick date of the end of the map element
                milliseconds = TempoMap.computeMillisecondsForNoTempo(endDate, ppq);                                        // compute the millisecond end date
                mapEntry.getValue().addAttribute(new Attribute("milliseconds.date.end", Double.toString(milliseconds)));    // add the attribute
            }
            return;
        }

        // process the map elements on the basis of this non-empty tempoMap
        ArrayList<TempoData> tempi = new ArrayList<>();
        ArrayList<KeyValue<Double, Integer>> pendingDurations = new ArrayList<>();

        for (int tempoIndex = 0; tempoIndex < this.size(); ++tempoIndex) {              // traverse the elements of this tempoMap
            TempoData td = this.getTempoDataOf(tempoIndex);                             // get the current tempo data
            if (td == null)                                                             // if the index did not point to a tempo element (could have been a style switch instead)
                continue;                                                               // take the next element until it is a tempo element to continue with the timing computations

            // compute the milliseconds date of the tempo instruction and add it to the tempi list so we do not have to do this again later on
            if (tempi.isEmpty())                                                                // if this is the first tempo instruction
                td.startDateMilliseconds = TempoMap.computeDiffTiming(td.startDate, ppq, null);
            else {
                TempoData prevTd = tempi.get(tempi.size() - 1);                                 // the milliseconds date must be computed on the basis of the previous tempo instruction
                td.startDateMilliseconds = TempoMap.computeDiffTiming(td.startDate, ppq, prevTd);
                td.startDateMilliseconds += tempi.get(tempi.size() - 1).startDateMilliseconds;  // add the previous tempo instruction's time to get the final timing
            }
            tempi.add(td);

            // compute the milliseconds dates of all map elements that fall under this tempo instruction
            double milliseconds;
            for (; mapIndex < map.size(); ++mapIndex) {                                 // traverse the map elements
                KeyValue<Double, Element> mapEntry = map.elements.get(mapIndex);        // get the current map entry
                if (mapEntry.getKey() > td.endDate)                                     // if the current map element is out of the scope of the current tempo data
                    break;                                                              // stop here and find the next tempo element first before continuing

                // compute the milliseconds dates
                double date = Double.parseDouble(Helper.getAttributeValue("date.perf", mapEntry.getValue()));
                if (mapEntry.getKey() <= td.startDate)                                                      // if we are before the current tempo instruction
                    milliseconds = TempoMap.computeDiffTiming(date, ppq, null);
                else
                    milliseconds = TempoMap.computeDiffTiming(date, ppq, td) + td.startDateMilliseconds;
                mapEntry.getValue().addAttribute(new Attribute("milliseconds.date", Double.toString(milliseconds)));    // add the attribute

                // duration has to be converted, too, but if this element has already a date.end attribute, we go on with this
                Attribute dateEndAtt = Helper.getAttribute("date.end.perf", mapEntry.getValue());   // some elements have already a date.end.perf attribute (e.g. section and all notes and rests that were processed by RubatoMap.renderRubatoToMap())
                if (dateEndAtt != null) {
                    double endDate = Double.parseDouble(dateEndAtt.getValue());                     // get the tick date of the end of the map element
                    pendingDurations.add(new KeyValue<>(endDate, mapIndex));                        // keep the mapIndex in the pendingDurations list to get back to it later
                    continue;
                }
                Attribute durAtt = Helper.getAttribute("duration.perf", mapEntry.getValue());       // if there was no date.end.perf attribute, we check the presence of a duration.perf attribute and generate date.end.perf from it
                if (durAtt != null) {
                    double endDate = date + Double.parseDouble(durAtt.getValue());                  // get the tick date of the end of the map element
                    mapEntry.getValue().addAttribute(new Attribute("date.end.perf", Double.toString(endDate)));      // add attribute date.end.perf
                    pendingDurations.add(new KeyValue<>(endDate, mapIndex));                        // keep the mapIndex in the pendingDurations list to get back to it later
                }
            }

            // check pending durations to fall under this tempo instruction and be processed now
            for (int i=0; i < pendingDurations.size(); ++i) {
                KeyValue<Double, Integer> pd = pendingDurations.get(i);
                double endDate = pd.getKey();
                if (endDate > td.endDate)
                    continue;
                if (endDate <= td.startDate)                                                    // if we are before the current tempo instruction
                    milliseconds = TempoMap.computeDiffTiming(endDate, ppq, null);
                else {
                    milliseconds = TempoMap.computeDiffTiming(endDate, ppq, td) + td.startDateMilliseconds;
                }
                map.elements.get(pd.getValue()).getValue().addAttribute(new Attribute("milliseconds.date.end", Double.toString(milliseconds)));    // add the attribute
                pendingDurations.remove(pd);
                --i;
            }

            if ((mapIndex >= map.size()) && pendingDurations.isEmpty())     // all map elements have been processed and no pending durations to be converted by future tempo instructions
                break;                                                      // no need to continue with remaining tempo elements
        }
    }

    /**
     * a static variant of the above method; iterate through the specified map, compute the milliseconds dates for all elements and add the results as attributes milliseconds.date and milliseconds.date.end;
     * this method includes a fallback mechanism if no tempoMap is provided
     * @param map an instance of GenericMap, typically an MSM score element that has been parsed into such (e.g., GenericMap.createGenericMap(score))
     * @param ppq the pulses per quarter timing resolution
     * @param tempoMap the tempoMap that is the basis of these computations, or null
     */
    public static void renderTempoToMap(GenericMap map, int ppq, TempoMap tempoMap) {
        if (tempoMap != null) {
            tempoMap.renderTempoToMap(map, ppq);
            return;
        }

        if (map == null)
            return;

        // if no tempoMap is given, 1 MIDI tick = 1 millisecond
        for (int i=0; i < map.size(); ++i) {
            Element e = map.getElement(i);
            Attribute dateAtt = Helper.getAttribute("date.perf", e);
            if (dateAtt != null)
                e.addAttribute(new Attribute("milliseconds.date", dateAtt.getValue()));
            Attribute endAtt = Helper.getAttribute("date.end.perf", e);
            if (endAtt != null)
                e.addAttribute(new Attribute("milliseconds.date.end", endAtt.getValue()));
            else {
                Attribute durAtt = Helper.getAttribute("duration.perf", e);
                if ((durAtt != null) && (dateAtt != null)) {
//                    e.addAttribute(new Attribute("milliseconds.duration", durAtt.getValue()));
                    double dateEnd = Double.parseDouble(dateAtt.getValue()) + Double.parseDouble(durAtt.getValue());
                    e.addAttribute(new Attribute("date.end.perf", Double.toString(dateEnd)));
                    e.addAttribute(new Attribute("milliseconds.date.end", Double.toString(dateEnd)));
                }
            }
        }
    }

    /**
     * collect all tempo instructions in this tempoMap and compute their milliseconds date
     * @param ppq
     * @return an ArrayList of TempoData instances with attribute startDateMilliseconds set
     */
    private ArrayList<TempoData> computeTimingOfTempoMap(int ppq) {
        ArrayList<TempoData> timedMap = new ArrayList<>();

        for (int tempoIndex = 0; tempoIndex < this.size(); ++tempoIndex) {              // traverse the elements of this tempoMap
            TempoData td = this.getTempoDataOf(tempoIndex);                             // get the current tempo data
            if (td == null)                                                             // if the index did not point to a tempo element (could have been a style switch instead)
                continue;                                                               // take the next element until it is a tempo element to continue with the timing computations

            if (timedMap.isEmpty())                                                                     // computation for the first tempo instruction
                td.startDateMilliseconds = TempoMap.computeDiffTiming(td.startDate, ppq, null);
            else {                                                                                      // computation for all further instructions
                TempoData prevTd = timedMap.get(timedMap.size() - 1);                                   // the milliseconds date must be computed on the basis of the previous tempo instruction
                td.startDateMilliseconds = TempoMap.computeDiffTiming(td.startDate, ppq, prevTd);
                td.startDateMilliseconds += timedMap.get(timedMap.size() - 1).startDateMilliseconds;    // add the previous tempo instructions time to get the final timing
            }
            timedMap.add(td);
        }

        return  timedMap;
    }

    /**
     * convenience method for timing computation; computes the milliseconds difference between the tick date and the date of the tempo instruction (tempoData);
     * in other word, how many milliseconds is date after the tempo instruction
     * @param date
     * @param ppq
     * @param tempoData a TempoData instance of the instruction that precedes date or null (if no tempo information is given)
     * @return date in milliseconds (in case of tempoData != null the result is the difference between the actual milliseconds date and the milliseconds date of the tempo instruction)
     */
    public static double computeDiffTiming(double date, int ppq, TempoData tempoData) {
        // no tempo data
        if (tempoData == null)
            return TempoMap.computeMillisecondsForNoTempo(date, ppq);

        // constant tempo
        if (tempoData.isConstantTempo())
            return TempoMap.computeMillisecondsForConstantTempo(date, ppq, tempoData);

        // continuous tempo transition
        return TempoMap.computeMillisecondsForTempoTransition(date, ppq, tempoData);
    }

    /**
     * timing computation for the case that no tempo data is given, e.g., before or at the first tempo instruction, a default tempo of 100 bpm and beatLength=0.25 are assumed
     * @param date
     * @param ppq
     * @return date in milliseconds
     */
    private static double computeMillisecondsForNoTempo(double date, int ppq) {
        return (600.0 * date) / ppq;
    }

    /**
     * timing computation for constant tempo
     * @param date
     * @param ppq
     * @param tempoData
     * @return the milliseconds difference between tempoData.startDate and date
     */
    private static double computeMillisecondsForConstantTempo(double date, int ppq, TempoData tempoData) {
        return ((15000.0 * (date - tempoData.startDate)) / (tempoData.bpm * tempoData.beatLength * ppq));   // compute the milliseconds timing within the previous tempo instruction's timeframe
    }

    /**
     * timing computation for continuous tempo transition; this method uses Simpson's rule for numerical integration
     * @param date
     * @param ppq
     * @param tempoData
     * @return the milliseconds difference between tempoData.startDate and date
     */
    private static double computeMillisecondsForTempoTransition(double date, int ppq, TempoData tempoData) {
        // the number of iterations of the Simpson's rule = N/2; 16th precision; N must be even!
        double N = 2.0 * (long)((date - tempoData.startDate) / (((double)ppq) / 4));
        if (N == 0.0)
            N = 2.0;

        // compute some often needed values
        double n = N / 2.0;                                                                                 // often needed, no need to compute it repetedly
        double x = (date - tempoData.startDate) / N;                                                        // often needed, too

        // compute Simpson's sum
        double resultConst = ((date - tempoData.startDate) * 5000.0) / (N * tempoData.beatLength * ppq);    // the constant part of the formula
        double resultSum = 1.0 / tempoData.bpm + 1.0 / TempoMap.getTempoAt(date, tempoData);
        for (int k = 1; k < n; ++k)                                                                         // the sum of all even pieces of the Simpson's sum
            resultSum += 2.0 / TempoMap.getTempoAt(tempoData.startDate + 2 * k * x, tempoData);
        for (int k = 1; k <= n; ++k)                                                                        // the sum of all uneven pieces of the Simpson's sum
            resultSum += 4.0 / TempoMap.getTempoAt(tempoData.startDate + (2 * k - 1) * x, tempoData);

        return resultConst * resultSum;
    }
}
