package meico.mpm.elements.maps;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.styles.ArticulationStyle;
import meico.mpm.elements.styles.defs.ArticulationDef;
import meico.supplementary.KeyValue;
import meico.mpm.elements.maps.data.ArticulationData;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class interfaces MPM's articulationMaps
 * @author Axel Berndt
 */
public class ArticulationMap extends GenericMap {
    /**
     * constructor, generates an empty ArticulationMap
     * @throws InvalidDataException
     */
    private ArticulationMap() throws InvalidDataException {
        super("articulationMap");
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws InvalidDataException
     */
    private ArticulationMap(Element xml) throws InvalidDataException {
        super(xml);
    }

    /**
     * ArticulationMap factory
     * @return
     */
    public static ArticulationMap createArticulationMap() {
        ArticulationMap d;
        try {
            d = new ArticulationMap();
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * ArticulationMap factory
     * @param xml
     * @return
     */
    public static ArticulationMap createArticulationMap(Element xml) {
        ArticulationMap d;
        try {
            d = new ArticulationMap(xml);
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
        this.setType("articulationMap");            // make sure this is really a "articulationMap"
    }

    /**
     * add an articulation element to the map
     * @param date
     * @param articulationDefName a reference to an articulationDef
     * @param noteid the xml:id reference to the note (should start with #), it is optional an can be set null
     * @return the index at which it has been inserted
     */
    public int addArticulation(double date, String articulationDefName, String noteid, String id) {
        Element e = new Element("articulation", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));

        if (articulationDefName == null)
            return -1;
        e.addAttribute(new Attribute("name.ref", articulationDefName));

        if (noteid != null)
            e.addAttribute(new Attribute("noteid", noteid));

        if (id != null)
            e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add an articulation element to the map, despite the date all attributes can be set null so they are not added to the element
     * @param date
     * @param absoluteDuration numeric value or null
     * @param absoluteDurationChange numeric value or null
     * @param relativeDuration numeric value or null
     * @param absoluteDurationMs numeric value or null
     * @param absoluteDurationChangeMs numeric value or null
     * @param absoluteVelocityChange numeric value or null
     * @param absoluteVelocity numeric value or null
     * @param relativeVelocity numeric value or null
     * @param absoluteDelayMs numeric value or null
     * @param absoluteDelay numeric value or null
     * @param detuneCents numeric value or null
     * @param detuneHz numeric value or null
     * @param noteid the xml:id reference to the note (should start with #), it is optional an can be set null
     * @return the index at which it has been inserted
     */
    public int addArticulation(double date, Double absoluteDuration, Double absoluteDurationChange, Double relativeDuration, Double absoluteDurationMs, Double absoluteDurationChangeMs, Double absoluteVelocityChange, Double absoluteVelocity, Double relativeVelocity, Double absoluteDelayMs, Double absoluteDelay, Double detuneCents, Double detuneHz, String noteid, String id) {
        Element e = new Element("articulation", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));

        if (id != null)
            e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id));
        if (noteid != null)
            e.addAttribute(new Attribute("noteid", noteid));
        if (absoluteDuration != null)
            e.addAttribute(new Attribute("absoluteDuration", Double.toString(absoluteDuration)));
        if (absoluteDurationChange != null)
            e.addAttribute(new Attribute("absoluteDurationChange", Double.toString(absoluteDurationChange)));
        if (relativeDuration != null)
            e.addAttribute(new Attribute("relativeDuration", Double.toString(relativeDuration)));
        if (absoluteDurationMs != null)
            e.addAttribute(new Attribute("absoluteDurationMs", Double.toString(absoluteDurationMs)));
        if (absoluteDurationChangeMs != null)
            e.addAttribute(new Attribute("absoluteDurationChangeMs", Double.toString(absoluteDurationChangeMs)));
        if (absoluteVelocityChange != null)
            e.addAttribute(new Attribute("absoluteVelocityChange", Double.toString(absoluteVelocityChange)));
        if (absoluteVelocity != null)
            e.addAttribute(new Attribute("absoluteVelocity", Double.toString(absoluteVelocity)));
        if (relativeVelocity != null)
            e.addAttribute(new Attribute("relativeVelocity", Double.toString(relativeVelocity)));
        if (absoluteDelayMs != null)
            e.addAttribute(new Attribute("absoluteDelayMs", Double.toString(absoluteDelayMs)));
        if (absoluteDelay != null)
            e.addAttribute(new Attribute("absoluteDelay", Double.toString(absoluteDelay)));
        if (detuneCents != null)
            e.addAttribute(new Attribute("detuneCents", Double.toString(detuneCents)));
        if (detuneHz != null)
            e.addAttribute(new Attribute("detuneHz", Double.toString(detuneHz)));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, false);
    }

    /**
     * add an articulation element to the articulationMap
     * @param data xml data will be ignored (use addElement() instead to add an xml representation)
     * @return
     */
    public int addArticulation(ArticulationData data) {
        Element e = new Element("articulation", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(data.date)));

        if (data.xmlId != null)
            e.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", data.xmlId));
        if (data.articulationDefName != null)
            e.addAttribute(new Attribute("name.ref", data.articulationDefName));
        else if (data.articulationDef != null)
            e.addAttribute(new Attribute("name.ref", data.articulationDef.getName()));
        if (data.noteid != null)
            e.addAttribute(new Attribute("noteid", data.noteid));
        if (data.absoluteDuration != null)
            e.addAttribute(new Attribute("absoluteDuration", Double.toString(data.absoluteDuration)));
        if (data.absoluteDurationChange != 0.0)
            e.addAttribute(new Attribute("absoluteDurationChange", Double.toString(data.absoluteDurationChange)));
        if (data.relativeDuration != 1.0)
            e.addAttribute(new Attribute("relativeDuration", Double.toString(data.relativeDuration)));
        if (data.absoluteDurationMs != null)
            e.addAttribute(new Attribute("absoluteDurationMs", Double.toString(data.absoluteDurationMs)));
        if (data.absoluteDurationChangeMs != 0.0)
            e.addAttribute(new Attribute("absoluteDurationChangeMs", Double.toString(data.absoluteDurationChangeMs)));
        if (data.absoluteVelocityChange != 0.0)
            e.addAttribute(new Attribute("absoluteVelocityChange", Double.toString(data.absoluteVelocityChange)));
        if (data.absoluteVelocity != null)
            e.addAttribute(new Attribute("absoluteVelocity", Double.toString(data.absoluteVelocity)));
        if (data.relativeVelocity != 1.0)
            e.addAttribute(new Attribute("relativeVelocity", Double.toString(data.relativeVelocity)));
        if (data.absoluteDelayMs != 0.0)
            e.addAttribute(new Attribute("absoluteDelayMs", Double.toString(data.absoluteDelayMs)));
        if (data.absoluteDelay != 0.0)
            e.addAttribute(new Attribute("absoluteDelay", Double.toString(data.absoluteDelay)));
        if (data.detuneCents != 0.0)
            e.addAttribute(new Attribute("detuneCents", Double.toString(data.detuneCents)));
        if (data.detuneHz != 0.0)
            e.addAttribute(new Attribute("detuneHz", Double.toString(data.detuneHz)));

        KeyValue<Double, Element> kv = new KeyValue<>(data.date, e);
        return this.insertElement(kv, false);
    }

    /**
     * this method generates a style switch (an MPM style element) and adds it to the map
     * @param date
     * @param styleName a reference to a styleDef
     * @param defaultArticulation a reference to an articulationDef
     * @return the index at which it has been inserted
     */
    public int addStyleSwitch(double date, String styleName, String defaultArticulation) {
        Element e = new Element("style", Mpm.MPM_NAMESPACE);
        e.addAttribute(new Attribute("date", Double.toString(date)));
        e.addAttribute(new Attribute("name.ref", styleName));
        e.addAttribute(new Attribute("defaultArticulation", defaultArticulation));

        KeyValue<Double, Element> kv = new KeyValue<>(date, e);
        return this.insertElement(kv, true);
    }

    /**
     * collect all data that is needed to compute the articulation at the specified date
     * @param date
     * @return an ArrayList of all articulations at the specific date
     */
    private ArrayList<ArticulationData> getArticulationDataAt(double date) {
        ArrayList<ArticulationData> ads = new ArrayList<>();
        int index = this.getElementIndexBeforeAt(date);

        for (int i = index; i >= 0; --i) {
            if (this.elements.get(i).getKey() < date)   // it has to be exactly at the date, not before and, of course, not after
                break;

            ArticulationData ad = this.getArticulationDataOf(i);
            if (ad != null)
                ads.add(0, ad);                         // add the articulation at the front, as we were traversing the map from back to front
        }

        // if no articulation is defined for this particular date, provide the style and standard articulation data
        if (ads.isEmpty()) {
            ArticulationData ad = new ArticulationData();
            ad.date = date;
            this.findStyle(index, ad);
            ads.add(ad);
        }
        return ads;
    }

    /**
     * This collects all data of the articulation at the specified map position. If it is no articulation (e.g. a style switch instead) the return value is null.
     * @param index
     * @return
     */
    private ArticulationData getArticulationDataOf(int index) {
        if (this.elements.isEmpty() || (index < 0))
            return null;

        if (index >= this.elements.size())
            index = this.elements.size() - 1;

        Element e = this.getElement(index);
        if (!e.getLocalName().equals("articulation"))        // it must be an articulation element
            return null;

        ArticulationData ad = new ArticulationData();
        ad.xml = e;
        ad.date = this.elements.get(index).getKey();

        Attribute att = Helper.getAttribute("xml:id", e);
        if (att != null)
            ad.xmlId = att.getValue();

        att = Helper.getAttribute("noteid", e);
        if (att != null)
            ad.noteid = att.getValue().substring(1);        // referenced ids start with #, so we take the id string from index 1 and through the # away

        this.findStyle(index, ad);  // get the style that applies to this articulation (not necessarily the same for all articulations at this date as there can be intermediate style switches)

        att = Helper.getAttribute("name.ref", e);
        if (att != null) {
            ad.articulationDefName = att.getValue();
            if (ad.style != null)
                ad.articulationDef = ad.style.getArticulationDef(ad.articulationDefName);
        }

        att = Helper.getAttribute("absoluteDuration", e);
        if (att != null)
            ad.absoluteDuration = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("absoluteDurationChange", e);
        if (att != null)
            ad.absoluteDurationChange = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("relativeDuration", e);
        if (att != null)
            ad.relativeDuration = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("absoluteDurationMs", e);
        if (att != null)
            ad.absoluteDurationMs = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("absoluteDurationChangeMs", e);
        if (att != null)
            ad.absoluteDurationChangeMs = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("absoluteVelocityChange", e);
        if (att != null)
            ad.absoluteVelocityChange = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("absoluteVelocity", e);
        if (att != null)
            ad.absoluteVelocity = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("relativeVelocity", e);
        if (att != null)
            ad.relativeVelocity = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("absoluteDelayMs", e);
        if (att != null)
            ad.absoluteDelayMs = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("absoluteDelay", e);
        if (att != null)
            ad.absoluteDelay = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("detuneCents", e);
        if (att != null)
            ad.detuneCents = Double.parseDouble(att.getValue());

        att = Helper.getAttribute("detuneHz", e);
        if (att != null)
            ad.detuneHz = Double.parseDouble(att.getValue());

        return ad;
    }

    /**
     * get the style that applies to the articulation at the specified index
     * @param index the index for which the style data is needed (could be a &lt;style/&gt; element itself)
     * @param ad style and defaultArticulation will be stored in this ArticulationData object
     */
    private void findStyle(int index, ArticulationData ad) {
        // get the style that applies to this articulation (not necessarily the same for all articulations at this date as there can be intermediate style switches)
        Attribute att;
        for (int j = index; j >= 0; --j) {                                  // find the last style switch at or before the articulation
            Element s = this.elements.get(j).getValue();
            if (s.getLocalName().equals("style")) {
                ad.styleName = Helper.getAttributeValue("name.ref", s);
                ad.style = (ArticulationStyle) this.getStyle(Mpm.ARTICULATION_STYLE, ad.styleName); // read the articulation style

                att = Helper.getAttribute("defaultArticulation", s);
                if (att != null) {
                    ad.defaultArticulation = att.getValue();
                    if (ad.style != null)
                        ad.defaultArticulationDef = ad.style.getArticulationDef(ad.defaultArticulation);
                }
                return;
            }
        }
        ad.styleName = null;
    }

    /**
     * On the basis of this articulationMap, edit the map (preferably an MSM score).
     * This method is meant to be applied BEFORE the other timing transformations and AFTER dynamics rendering.
     * Changes in the millisecond domain are added to the notes as attributes and need to be applied later via method renderArticulationToMap_millisecondModifiers()!
     * @param map
     */
    public void renderArticulationToMap_noMillisecondModifiers(GenericMap map) {
        if (map == null)
            return;

        // make a hashmap (note element, articulation data list) for all notes with a specific (i.e. non-default) articulation
        HashMap<Element, ArrayList<ArticulationData>> noteArtics = new HashMap<>();
        boolean mapTimingChanged = false;                                           // if an articulation changed the symbolic timing of a note, it has to be reordered, this flag signals this case
        for (int articIndex = 0; articIndex < this.size(); ++articIndex) {
            ArticulationData ad = this.getArticulationDataOf(articIndex);
            if (ad == null)                                                         // if this is no articulation (e.g. a style switch instead)
                continue;                                                           // go on with the next element

            if (ad.noteid != null) {                                                // if this articulation is for a specific note
                int index = map.getElementIndexByID(ad.noteid);                     // find the corresponding note
                if (index < 0)
                    continue;
                if (map.getAllElements().get(index).getKey() != ad.date)            // check consistency of the dates
                    System.err.println("Warning: articulation date and referee date do not match!\n    " + ad.xml.toXML() + "\n    " + map.getAllElements().get(index).getValue().toXML()); // print warning if inconsistent
                Element note = map.getAllElements().get(index).getValue();
                ArrayList<ArticulationData> adList = noteArtics.computeIfAbsent(note, k -> new ArrayList<>());  // find or generate the hashmap entry
                adList.add(ad);                                                     // add the articulation data to it
                continue;                                                           // the articulation is for a specific note, so it does not apply to any other notes at the same date an d we continue with the next articulation
            }

            // if no noteid is specified, the articulation is potentially relevant to all map elements at the same date
            ArrayList<KeyValue<Double, Element>> elements = map.getAllElementsAt(ad.date);  // collect all potentially relevant map elements at the same date as the articulation instruction
            for (KeyValue<Double, Element> element : elements) {                    // for each of these elements
                if (!element.getValue().getLocalName().equals("note"))              // if it is no note, it cannot be articulated
                    continue;                                                       // go on with the next element
                ArrayList<ArticulationData> adList = noteArtics.computeIfAbsent(element.getValue(), k -> new ArrayList<>());    // find or generate the hashmap entry
                adList.add(ad);                                                     // add the articulation data to it
            }
        }

        // create a list of styles/switches
        ArrayList<KeyValue<Double, ArticulationDef>> defaultArticulations = new ArrayList<>();          // an arraylist of (date, default ArticulationDef) tuplets
        ArrayList<KeyValue<Double, Element>> styleSwitchList = this.getAllElementsOfType("style");      // collect all style switches and put them into the list
        for (KeyValue<Double, Element> styleEntry : styleSwitchList) {
            ArticulationStyle aStyle = (ArticulationStyle) this.getStyle(Mpm.ARTICULATION_STYLE, Helper.getAttributeValue("name.ref", styleEntry.getValue()));
            if (aStyle != null) {
                ArticulationDef aDef = aStyle.getArticulationDef(Helper.getAttributeValue("defaultArticulation", styleEntry.getValue()));
                if (aDef == null)
                    System.err.println("Warning: attribute " + Helper.getAttribute("defaultArticulation", this.getXml()).toXML() + " in style element refers to an unknown articulationDef.");
                defaultArticulations.add(new KeyValue<>(styleEntry.getKey(), aDef));
            }
        }

        // articulate the map elements
        int defaultArticulationIndex = 0;
        for (int mapIndex = 0; mapIndex < map.size(); ++mapIndex) {
            KeyValue<Double, Element> mapEntry = map.elements.get(mapIndex);
            if (!mapEntry.getValue().getLocalName().equals("note"))                 // if this map entry is no note, it cannot be articulated
                continue;                                                           // go on with the next element

            ArrayList<ArticulationData> artics = noteArtics.get(mapEntry.getValue());
            if (artics != null) {                                                   // apply the articulations to the associated note
                for (ArticulationData artic : artics) {                             // each articulation that is associated with this note element
                    mapTimingChanged |= artic.articulateNote(mapEntry.getValue());  // apply articulation
                }
                continue;
            }

            // otherwise apply the default articulation
            if (defaultArticulations.isEmpty())                                     // if we have such data
                continue;

            // make sure we use the latest default articulation
            while (((defaultArticulationIndex + 1) < defaultArticulations.size()) && (defaultArticulations.get(defaultArticulationIndex + 1).getKey() <= mapEntry.getKey()))
                defaultArticulationIndex++;

            ArticulationDef defaultArticulationDef = defaultArticulations.get(defaultArticulationIndex).getValue();
            if (defaultArticulationDef == null)                                     // if the last style switch did not define a default articulation
                continue;                                                           // leave this note unaltered

            mapTimingChanged |= defaultArticulationDef.articulateNote(mapEntry.getValue()); // apply default articulation and if it changed the note's timing
        }

        // correct map order due to timing changes
        if (mapTimingChanged)
            map.sort();
    }

    /**
     * On the basis of the specified articulationMap, edit the map (preferably an MSM score).
     * This method is meant to be applied BEFORE the other timing transformations and AFTER dynamics rendering.
     * Changes in the millisecond domain are added to the notes as attributes and need to be applied later!
     * @param map
     * @param articulationMap
     */
    public static void renderArticulationToMap_noMillisecondModifiers(GenericMap map, ArticulationMap articulationMap) {
        if (articulationMap != null)
            articulationMap.renderArticulationToMap_noMillisecondModifiers(map);
    }

    /**
     * On the basis of the specified articulationMap, edit the map (preferably an MSM score).
     * This method is meant to be applied AFTER asynchrony has been added to the map and BEFORE imprecision is applied.
     * Also, renderArticulationToMap_noMillisecondModifiers() must have been applied to the map BEFORE this method is invoked
     * as this method expecting the attributes (articulation.absoluteDelayMs, articulation.absoluteDurationMs, articulation.absoluteDurationChangeMs)
     * that have been generated there.
     * @param map
     */
    public void renderArticulationToMap_millisecondModifiers(GenericMap map) {
        if (map == null)
            return;

        for (KeyValue<Double, Element> entry : map.elements) {
            Attribute dateAtt = Helper.getAttribute("milliseconds.date", entry.getValue());
            if (dateAtt == null)
                continue;
            double date = Double.parseDouble(dateAtt.getValue());

            Attribute endAtt = Helper.getAttribute("milliseconds.date.end", entry.getValue());

            Attribute absoluteDelayMs = Helper.getAttribute("articulation.absoluteDelayMs", entry.getValue());
            if (absoluteDelayMs != null) {
                double delay = Double.parseDouble(absoluteDelayMs.getValue());
                double dateNew = date + delay;

                if (endAtt != null) {
                    double end = Double.parseDouble(endAtt.getValue());
                    if (dateNew >= end) {                       // if the delay goes beyond the end date of the note
                        double delayNew = (end - date) / 2.0;
                        dateNew = date + delayNew;              // reduce the delay to half of the time between date and end
                        System.out.println("Note " + entry.getValue().toXML() + " cannot be delayed by " + delay + " milliseconds. Reducing delay to " + delayNew + " milliseconds.");
                    }
                }
                dateAtt.setValue(Double.toString(dateNew));

//                ((Element)absoluteDelayMs.getParent()).removeAttribute(absoluteDelayMs);
                absoluteDelayMs.detach();
            }

            if (endAtt == null)
                continue;

            Attribute absoluteDurationMs = Helper.getAttribute("articulation.absoluteDurationMs", entry.getValue());
            if (absoluteDurationMs != null) {
                endAtt.setValue(Double.toString(date + Double.parseDouble(absoluteDurationMs.getValue())));
//                ((Element)absoluteDurationMs.getParent()).removeAttribute(absoluteDurationMs);
                absoluteDurationMs.detach();
            }

            Attribute absoluteDurationChangeMs = Helper.getAttribute("articulation.absoluteDurationChangeMs", entry.getValue());
            if (absoluteDurationChangeMs != null) {
                double end = Double.parseDouble(endAtt.getValue());
                double durChange = Double.parseDouble(absoluteDurationChangeMs.getValue());
                double endNew = end + durChange;
                for (double reduce = 2.0; endNew <= date; reduce *= 2.0)    // as long as the duration change causes the duration to become 0.0 or negative
                    endNew = end + (durChange / reduce);                    // reduce the change by 50%
                endAtt.setValue(Double.toString(endNew));
//                ((Element)absoluteDurationChangeMs.getParent()).removeAttribute(absoluteDurationChangeMs);
                absoluteDurationChangeMs.detach();
            }
        }
    }

    /**
     * On the basis of the specified articulationMap, edit the map (preferably an MSM score).
     * This method is meant to be applied AFTER asynchrony has been added to the map and BEFORE imprecision is applied.
     * Also, renderArticulationToMap_noMillisecondModifiers() must have been applied to the map BEFORE this method is invoked
     * as this method expecting the attributes (articulation.absoluteDelayMs, articulation.absoluteDurationMs, articulation.absoluteDurationChangeMs)
     * that have been generated there.
     * @param map
     * @param articulationMap
     */
    public static void renderArticulationToMap_millisecondModifiers(GenericMap map, ArticulationMap articulationMap) {
        if (articulationMap != null)
            articulationMap.renderArticulationToMap_millisecondModifiers(map);
    }
}
