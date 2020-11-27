package meico.mpm.elements;

import meico.mei.Helper;
import meico.midi.Midi;
import meico.mpm.Mpm;
import meico.mpm.elements.maps.*;
import meico.msm.Msm;
import meico.supplementary.KeyValue;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class represents an mpm performance. One mpm document can hold several performances.
 * @author Axel Berndt.
 */

public class Performance extends AbstractXmlSubtree {
    private Attribute name = null;                              // the name of the performance
    private int pulsesPerQuarter = 720;                         // the timing resolution of symbolic time (midi.date etc.)
    private Global global = null;                               // the global performance information
    private ArrayList<Part> parts = new ArrayList<>();          // the local performance information
    private Attribute id = null;                                // the id attribute

    /**
     * This constructor generates an empty performance with only a name, global and dated environment.
     *
     * @param name the name of the performance
     */
    private Performance(String name) throws Exception {
        Element performance = new Element("performance", Mpm.MPM_NAMESPACE);
        Attribute nameAtt = new Attribute("name", name);
        performance.addAttribute(nameAtt);

        this.parseData(performance);
    }

    /**
     * this constructor instantiates the Performance object from an existing xml source handed over as XOM Element
     *
     * @param xml
     */
    private Performance(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * performance factory
     *
     * @param name the name of the performance
     * @return
     */
    public static Performance createPerformance(String name) {
        Performance performance;
        try {
            performance = new Performance(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return performance;
    }

    /**
     * performance factory
     *
     * @param name the name of the performance
     * @param pulsesPerQuarter
     * @return
     */
    public static Performance createPerformance(String name, int pulsesPerQuarter) {
        Performance performance;
        try {
            performance = new Performance(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        performance.setPulsesPerQuarter(pulsesPerQuarter);
        return performance;
    }

    /**
     * performance factory
     *
     * @param name the name of the performance
     * @param pulsesPerQuarter
     * @param id
     * @return
     */
    public static Performance createPerformance(String name, int pulsesPerQuarter, String id) {
        Performance performance;
        try {
            performance = new Performance(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        performance.setPulsesPerQuarter(pulsesPerQuarter);
        performance.setId(id);
        return performance;
    }

    /**
     * performance factory
     *
     * @param xml
     * @return
     */
    public static Performance createPerformance(Element xml) {
        Performance performance;
        try {
            performance = new Performance(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return performance;
    }

    /**
     * set the data of this performance, this parses the xml element and generates the according data structure
     *
     * @param xml
     * @throws Exception
     */
    @Override
    protected void parseData(Element xml) throws Exception {
        if (xml == null)
            throw new Exception("Cannot generate Performance object. XML Element is null.");

        Attribute name = Helper.getAttribute("name", xml);
        if ((name == null) || name.getValue().isEmpty()) {                          // each performance requires a name, if there is none or it is empty
            throw new Exception("Cannot generate Performance object. Attribute name is missing or empty.");  // throw exception
        }

        this.setXml(xml);
        this.name = Helper.getAttribute("name", this.getXml());
        this.id = Helper.getAttribute("id", this.getXml());

        // make sure that this element is really a "performance" element
        if (!this.getXml().getLocalName().equals("performance")) {
            this.getXml().setLocalName("performance");
        }

        // make sure the performance has a pulsesPerQuarter attribute
        Attribute ppqAtt = Helper.getAttribute("pulsesPerQuarter", this.getXml());
        if (ppqAtt == null) {                                                       // if there is no pulsesPerQuarter attribute
            ppqAtt = new Attribute("pulsesPerQuarter", "720");                      // generate one with default ppq of 720
            this.getXml().addAttribute(ppqAtt);                                     // add it to the xml
            this.pulsesPerQuarter = 720;                                            // set the corresponding class variable
        } else {
            this.pulsesPerQuarter = Integer.parseInt(ppqAtt.getValue());            // read the attribute value into the corresponding class variable
        }

        // make sure there is a global environment
        Element globalElt = Helper.getFirstChildElement("global", this.getXml());
        if (globalElt == null) {                                                    // if the performance has no global environment
            this.global = Global.createGlobal();
            this.getXml().appendChild(this.global.getXml());                        // add it to the performance
        } else {
            this.global = Global.createGlobal(globalElt);
        }
        // add the parts to this.parts
        LinkedList<Element> parts = Helper.getAllChildElements("part", this.getXml());
        for (Element element : parts) {
            Part part = Part.createPart(element);                                   // try to generate an MpmPart object from the xml data
            if (part == null)
                continue;                                                           // continue with the next part
            part.setGlobal(this.global);                                            // set the global environment
            this.parts.add(part);                                                   // otherwise, add it to the parts list
        }
    }

    /**
     * get the number of parts in this performance
     *
     * @return
     */
    public int size() {
        return this.parts.size();
    }

    /**
     * this returns all parts in this performance as an ArrayList
     *
     * @return
     */
    public ArrayList<Part> getAllParts() {
        return this.parts;
    }

    /**
     * Access the part with the specified number.
     * If there are more than one part with this number, the first in the list is returned.
     *
     * @param number
     * @return
     */
    public Part getPart(int number) {
        for (Part p : this.parts) {
            if (p.getNumber() == number)
                return p;
        }
        return null;
    }

    /**
     * Access the part with the specified name.
     * If there are more than one part with this name, the first in the list is returned.
     *
     * @param name
     * @return
     */
    public Part getPart(String name) {
        for (Part p : this.parts) {
            if (p.getName().equals(name))
                return p;
        }
        return null;
    }

    /**
     * Access the part by its MIDI channel and port
     * If there are more than one part with this channel and port (which is bad practise!), the first in the list is returned.
     *
     * @param midiChannel
     * @param midiPort
     * @return
     */
    public Part getPart(int midiChannel, int midiPort) {
        for (Part p : this.parts) {
            if ((p.getMidiChannel() == midiChannel) && (p.getMidiPort() == midiPort))
                return p;
        }
        return null;
    }

    /**
     * add the part to the performance,
     * caution: if another part with the same number exists already in this performance, getPart(number) will return only the first
     * @param part
     * @return success
     */
    public boolean addPart(Part part) {
        if ((part == null) || (this.parts.contains(part)))
            return false;

        Element parent = (Element) part.getXml().getParent();
        if ((parent == null) || (parent != this.getXml())) {
            part.getXml().detach();
            this.getXml().appendChild(part.getXml());   // add the xml code of the part to the performance's xml
        }
        part.setGlobal(this.getGlobal());           // link to the global environment where the part may find related information (styleDefs etc.)
        return this.parts.add(part);
    }

    /**
     * remove all parts with the specified number from this performance
     *
     * @param number
     */
    public void removePart(int number) {
        for (Part p : this.parts) {
            if (p.getNumber() == number) {
                this.parts.remove(p);
                this.getXml().removeChild(p.getXml());
//                p.getXml().detach();
            }
        }
    }

    /**
     * remove all parts with the specified name from this performance
     *
     * @param name
     */
    public void removePart(String name) {
        for (Part p : this.parts) {
            if (p.getName().equals(name)) {
                this.parts.remove(p);
                this.getXml().removeChild(p.getXml());
//                p.getXml().detach();
            }
        }
    }

    /**
     * remove the specified part from this performance
     *
     * @param part
     */
    public void removePart(Part part) {
        if (this.parts.remove(part)) {                  // if the part was in this performance and could be removed from the parts list
            this.getXml().removeChild(part.getXml());   // it can be removed from the xml structure
//            part.getXml().detach();
        }
    }

    /**
     * access the global information of this performance
     *
     * @return
     */
    public Global getGlobal() {
        return this.global;
    }

    /**
     * a getter for the performance's name
     *
     * @return
     */
    public String getName() {
        return this.name.getValue();
    }

    /**
     * set the performance's name,
     * if the performance is already part of an mpm it should be removed and re-added to make sure that it can be found under the new name
     *
     * @param name
     */
    public void setName(String name) {
        this.name.setValue(name);
    }

    /**
     * read the pulses per quarter timing resolution (relevant for to interpret midi.date values)
     *
     * @return
     */
    public int getPulsesPerQuarter() {
        return this.pulsesPerQuarter;
    }

    /**
     * read the pulses per quarter timing resolution (relevant for to interpret midi.date values)
     *
     * @return
     */
    public int getPPQ() {
        return this.getPulsesPerQuarter();
    }

    /**
     * Set the pulses per quarter timing resolution attribute.
     * Be careful with this, it does not change any midi date values!
     * @param ppq
     */
    public void setPulsesPerQuarter(int ppq) {
        this.pulsesPerQuarter = ppq;
        Helper.getAttribute("pulsesPerQuarter", this.getXml()).setValue(Integer.toString(ppq));
    }

    /**
     * Set the pulses per quarter timing resolution attribute.
     * Be careful with this, it does not change any midi date values!
     * @param ppq
     */
    public void setPPQ(int ppq) {
        this.setPulsesPerQuarter(ppq);
    }

    /**
     * this generates an Msm object from the input midi data and adds expression data (such as millisecond dates, durations, and velocity values) to it;
     * performance rendering will keep MIDI compliance
     * @param midi
     * @return an augmented MSM with performance related data
     */
    public Msm perform(Midi midi) {
        Msm msm = midi.exportMsm(true, true);
        return this.perform(msm);
    }

    /**
     * this add expression data (such as millisecond dates, durations, and velocity values) to the specified MSM
     * @param msm
     * @return an augmented MSM with performance related data
     */
    public Msm perform(Msm msm) {
        long startTime = System.currentTimeMillis();                                                            // we measure the time that the conversion consumes
        System.out.println("\nRendering performance \"" + this.getName() + "\" into \"" + msm.getTitle() + "\".");

        Msm clone = msm.clone();                                                                                // the original msm should remain unaltered, hence, we create a copy of it to work with and be return
        clone.setFile(Helper.getFilenameWithoutExtension(clone.getFile().getPath()) + "_" + this.getName() + ".msm");   // just to make sure that the original file will no be overwritten when the application writes this clone to the file system

        clone.convertPPQ(this.getPPQ());  // ppq check and convert if necessary (for all attributes date, date.end and duration)

        // get global msm maps
        RubatoMap globalRubatoMap = (RubatoMap) this.getGlobal().getDated().getMap(Mpm.RUBATO_MAP);                                         // get the global rubatoMap
        TempoMap globalTempoMap = (TempoMap) this.getGlobal().getDated().getMap(Mpm.TEMPO_MAP);                                             // get the global tempoMap
        AsynchronyMap globalAsynchronyMap = (AsynchronyMap) this.getGlobal().getDated().getMap(Mpm.ASYNCHRONY_MAP);                         // get the global asynchronyMap
        ImprecisionMap globalImprecisionMap_timing = (ImprecisionMap) this.getGlobal().getDated().getMap(Mpm.IMPRECISION_MAP_TIMING);       // get the global timing imprecisionMap
        ImprecisionMap globalImprecisionMap_dynamics = (ImprecisionMap) this.getGlobal().getDated().getMap(Mpm.IMPRECISION_MAP_DYNAMICS);   // get the global dynamics imprecisionMap
        ImprecisionMap globalImprecisionMap_toneduration = (ImprecisionMap) this.getGlobal().getDated().getMap(Mpm.IMPRECISION_MAP_TONEDURATION);   // get the global toneduration imprecisionMap
        ImprecisionMap globalImprecisionMap_tuning = (ImprecisionMap) this.getGlobal().getDated().getMap(Mpm.IMPRECISION_MAP_TUNING);       // get the global tuning imprecisionMap
        DynamicsMap globalDynamicsMap = (DynamicsMap) this.getGlobal().getDated().getMap(Mpm.DYNAMICS_MAP);                                 // get the global dynamicsMap
        MetricalAccentuationMap glbalMetricalAccentuationMap = (MetricalAccentuationMap) this.getGlobal().getDated().getMap(Mpm.METRICAL_ACCENTUATION_MAP); // get the global metricalAccentuationMap
        ArticulationMap globalArticulationMap = (ArticulationMap) this.getGlobal().getDated().getMap(Mpm.ARTICULATION_MAP);                 // get the global articulationMap
        ArrayList<GenericMap> maps = new ArrayList<>();                                                                                     // maps to be processed
        ArrayList<KeyValue<Double, Element>> cleanupList = new ArrayList<>();                                                               // maps that need cleanup at the end

        // the imprecisionMaps need millisecond dates, hence we add them to the maps list and the cleanup list so the millisseconds attributes are deleted afterwards
        if (globalImprecisionMap_timing != null) {
            maps.add(globalImprecisionMap_timing);
            cleanupList.addAll(globalImprecisionMap_timing.getAllElements());
        }
        if (globalImprecisionMap_dynamics != null) {
            maps.add(globalImprecisionMap_dynamics);
            cleanupList.addAll(globalImprecisionMap_dynamics.getAllElements());
        }
        if (globalImprecisionMap_toneduration != null) {
            maps.add(globalImprecisionMap_toneduration);
            cleanupList.addAll(globalImprecisionMap_toneduration.getAllElements());
        }
        if (globalImprecisionMap_tuning != null) {
            maps.add(globalImprecisionMap_tuning);
            cleanupList.addAll(globalImprecisionMap_tuning.getAllElements());
        }

        // process global data
        System.out.println("Processing global data.");
        Element globalDated = Helper.getFirstChildElement("dated", clone.getGlobal());
        Performance.addMsmMapToList("keySignatureMap", globalDated, maps);
        GenericMap globalTimeSignatureMap = Performance.addMsmMapToList("timeSignatureMap", globalDated, maps);
        Performance.addMsmMapToList("sectionMap", globalDated, maps);
        Performance.addMsmMapToList("sequencingMap", globalDated, maps);
        Performance.addMsmMapToList("markerMap", globalDated, maps);
        GenericMap globalPedalMap = Performance.addMsmMapToList("pedalMap", globalDated, maps);
        for (GenericMap m : maps) {                                                                     // for all maps in the list of maps for timing processing
            RubatoMap.renderRubatoToMap(m, globalRubatoMap);
            TempoMap.renderTempoToMap(m, this.getPPQ(), globalTempoMap);                                // compute millisecond dates and end dates
        }
        AsynchronyMap.renderAsynchronyToMap(globalPedalMap, globalAsynchronyMap);                       // add asynchrony offsets to the millisecond dates
        ImprecisionMap.renderImprecisionToMap(globalPedalMap, globalImprecisionMap_timing, true);       // add imprecision

        // process the msm parts
        Elements parts = clone.getParts();                                                                  // get the parts from the msm
        for (int p = 0; p < parts.size(); ++p) {
            Element msmPart = parts.get(p);

            // find the corresponding mpm part
            Part mpmPart = this.getPart(Integer.parseInt(Helper.getAttributeValue("number", msmPart)));     // try finding the corresponding mpm part via the number attribute
            if (mpmPart == null) {
                mpmPart = this.getPart(Helper.getAttributeValue("name", msmPart));                          // try finding the corresponding mpm part via the name attribute
                if (mpmPart == null) {
                    mpmPart = this.getPart(Integer.parseInt(Helper.getAttributeValue("midi.channel", msmPart)), Integer.parseInt(Helper.getAttributeValue("midi.port", msmPart)));  // try finding the corresponding mpm part via the attributes midi.channel and midi.port
                }
            }
            if (mpmPart == null)                                                                            // if no mpm part could be found
                System.err.println("Cannot find an MPM part that corresponds to MSM part " + Helper.getAttributeValue("number", msmPart) + " \"" + Helper.getAttributeValue("name", msmPart) + "\""); // error message
            else
                System.out.println("Performing part " + mpmPart.getNumber() + ": " + mpmPart.getName() /*+ ", midi channel " + mpmPart.getMidiChannel() + ", midi port " + mpmPart.getMidiPort()*/);

            // retrieve all msm maps in this part to be processed
            Element dated = Helper.getFirstChildElement("dated", msmPart);
            if (dated == null) continue;
            maps = new ArrayList<>();
            GenericMap score = Performance.addMsmMapToList("score", dated, maps);
            Performance.addMsmMapToList("keySignatureMap", dated, maps);
            GenericMap timeSignatureMap = Performance.addMsmMapToList("timeSignatureMap", dated, maps);
            Performance.addMsmMapToList("sectionMap", dated, maps);
            Performance.addMsmMapToList("sequencingMap", dated, maps);
            Performance.addMsmMapToList("markerMap", dated, maps);
            Performance.addMsmMapToList("programChangeMap", dated, maps);
            GenericMap pedalMap = Performance.addMsmMapToList("pedalMap", dated, maps);

            RubatoMap rubatoMap = null;
            TempoMap tempoMap = null;
            AsynchronyMap asynchronyMap = null;
            DynamicsMap dynamicsMap = null;
            MetricalAccentuationMap metricalAccentuationMap = null;
            ArticulationMap articulationMap = null;
            ImprecisionMap imprecisionMap_timing = null;
            ImprecisionMap imprecisionMap_dynamics = null;
            ImprecisionMap imprecisionMap_toneduration = null;
            ImprecisionMap imprecisionMap_tuning = null;
            if (mpmPart != null) {                                                                                      // if the performance has information for this part, get them, otherwise it applies only the global ones by default
                rubatoMap = (RubatoMap) mpmPart.getDated().getMap(Mpm.RUBATO_MAP);                                      // get rubatoMap
                tempoMap = (TempoMap) mpmPart.getDated().getMap(Mpm.TEMPO_MAP);                                         // get tempoMap
                asynchronyMap = (AsynchronyMap) mpmPart.getDated().getMap(Mpm.ASYNCHRONY_MAP);                          // get asynchronyMap
                dynamicsMap = (DynamicsMap) mpmPart.getDated().getMap(Mpm.DYNAMICS_MAP);                                // get dynamicsMap
                metricalAccentuationMap = (MetricalAccentuationMap) mpmPart.getDated().getMap(Mpm.METRICAL_ACCENTUATION_MAP);   // get metricalAccentuationMap
                articulationMap = (ArticulationMap) mpmPart.getDated().getMap(Mpm.ARTICULATION_MAP);                    // get articulationMap
                imprecisionMap_timing = (ImprecisionMap) mpmPart.getDated().getMap(Mpm.IMPRECISION_MAP_TIMING);         // get imprecisionMap.timing
                imprecisionMap_dynamics = (ImprecisionMap) mpmPart.getDated().getMap(Mpm.IMPRECISION_MAP_DYNAMICS);     // get imprecisionMap.dynamics
                imprecisionMap_toneduration = (ImprecisionMap) mpmPart.getDated().getMap(Mpm.IMPRECISION_MAP_TONEDURATION); // get imprecisionMap.toneduration
                imprecisionMap_tuning = (ImprecisionMap) mpmPart.getDated().getMap(Mpm.IMPRECISION_MAP_TUNING);         // get imprecisionMap.tuning
            }

            // if no local map choose global
            if (rubatoMap == null)
                rubatoMap = globalRubatoMap;
            if (tempoMap == null)
                tempoMap = globalTempoMap;
            if (asynchronyMap == null)
                asynchronyMap = globalAsynchronyMap;
            if (dynamicsMap == null)
                dynamicsMap = globalDynamicsMap;
            if (metricalAccentuationMap == null)
                metricalAccentuationMap = glbalMetricalAccentuationMap;
            if (articulationMap == null)
                articulationMap = globalArticulationMap;
            // the global imprecisionMaps have already milliseconds dates (required), the local does not, hence, they must be added to maps and to the cleanup list so the milliseconds attributes get deleted afterwards
            if (imprecisionMap_timing == null)
                imprecisionMap_timing = globalImprecisionMap_timing;
            else {
                maps.add(imprecisionMap_timing);
                cleanupList.addAll(imprecisionMap_timing.getAllElements());
            }
            if (imprecisionMap_dynamics == null)
                imprecisionMap_dynamics = globalImprecisionMap_dynamics;
            else {
                maps.add(imprecisionMap_dynamics);
                cleanupList.addAll(imprecisionMap_dynamics.getAllElements());
            }
            if (imprecisionMap_toneduration == null)
                imprecisionMap_toneduration = globalImprecisionMap_toneduration;
            else {
                maps.add(imprecisionMap_toneduration);
                cleanupList.addAll(imprecisionMap_toneduration.getAllElements());
            }
            if (imprecisionMap_tuning == null)
                imprecisionMap_tuning = globalImprecisionMap_tuning;
            else {
                maps.add(imprecisionMap_tuning);
                cleanupList.addAll(imprecisionMap_tuning.getAllElements());
            }

            // here comes the performance rendering of the part
            // some things should be done before the timing transformations
            GenericMap channelVolumeMap = DynamicsMap.renderDynamicsToMap(score, dynamicsMap);  // add dynamics data, must be done first because the tick timing will be altered by some articulations and rubato
            if (channelVolumeMap != null)                                                       // there could be a new map with sub-note dynamics controllers to be added to maps
                dated.appendChild(channelVolumeMap.getXml());                                   // add it to the MSM

            MetricalAccentuationMap.renderMetricalAccentuationToMap(score, metricalAccentuationMap, ((timeSignatureMap != null) ? timeSignatureMap : globalTimeSignatureMap), this.getPPQ());  // add metrical accentuations; we do this before the rubato transformation as this shifts the symbolic dates of the events
            ArticulationMap.renderArticulationToMap_noMillisecondModifiers(score, articulationMap); // add articulations except for millisecond modifiers

            // rubato and tempo transformations apply to all maps
            for (GenericMap m : maps) {                                                         // for all maps in the list of maps for timing processing
                RubatoMap.renderRubatoToMap(m, rubatoMap);                                      // rubato
                TempoMap.renderTempoToMap(m, this.getPPQ(), tempoMap);                          // compute millisecond dates and end dates
                // further performance features are applied only to specific maps, thus not processed here
            }

            // pedalMap
            AsynchronyMap.renderAsynchronyToMap(pedalMap, asynchronyMap);                       // add asynchrony offsets to the millisecond dates to the pedalMap
            ImprecisionMap.renderImprecisionToMap(pedalMap, imprecisionMap_timing, true);       // add imprecision to the pedalMap

            // channelVolumeMap
            TempoMap.renderTempoToMap(channelVolumeMap, this.getPPQ(), tempoMap);               // channelVolumeMap gets trandformed by the tempoMap but not the rubatoMap as the latter would create higher-frequency variations in the dynamics curve
            AsynchronyMap.renderAsynchronyToMap(channelVolumeMap, asynchronyMap);               // add asynchrony offsets to the millisecond dates to the channelVolumeMap

            // score
            if (score == null)      // if this msm part has no score
                continue;           // continue with the next msm part
            AsynchronyMap.renderAsynchronyToMap(score, asynchronyMap);                          // add asynchrony offsets to the millisecond dates
            ArticulationMap.renderArticulationToMap_millisecondModifiers(score, articulationMap); // apply articulations' millisecond modifiers

            ImprecisionMap.renderImprecisionToMap(score, imprecisionMap_timing, true);          // add timing imprecision
            ImprecisionMap.renderImprecisionToMap(score, imprecisionMap_dynamics, true);        // add dynamics imprecision
            ImprecisionMap.renderImprecisionToMap(score, imprecisionMap_toneduration, true);    // add toneduration imprecision
            ImprecisionMap.renderImprecisionToMap(score, imprecisionMap_tuning, true);          // add tuning imprecision
        }

        // cleanup: remove attribute milliseconds.date from all elements in the cleanup list
        for (KeyValue<Double, Element> e : cleanupList) {
            Attribute ms = Helper.getAttribute("milliseconds.date", e.getValue());
            if (ms != null)
                e.getValue().removeAttribute(ms);
        }

        System.out.println("Performance rendering finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return clone;
    }

    /**
     * a helper method to retrieve a certain map from an msm dated environment, generate a GenericMap object from it and add it to a LinkedList;
     * this simplifies code in method perform()
     * @param mapName
     * @param msmDated
     * @param list
     */
    private static GenericMap addMsmMapToList(String mapName, Element msmDated, ArrayList<GenericMap> list) {
        Element e = Helper.getFirstChildElement(mapName, msmDated);
        if (e != null) {
            GenericMap m = GenericMap.createGenericMap(e);
            if (m != null) {
                list.add(m);
                return m;
            }
        }
        return null;
    }

    /**
     * set the performance's id
     * @param id a xml:id string or null
     */
    public void setId(String id) {
        if (id == null) {
            if (this.id != null) {
                this.id.detach();
                this.id = null;
            }
            return;
        }

        if (this.id == null) {
            this.id = new Attribute("id", id);
            this.id.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");    // set correct namespace
            this.getXml().addAttribute(this.id);
            return;
        }

        this.id.setValue(id);
    }

    /**
     * get the performance's id
     * @return a string or null
     */
    public String getId() {
        if (this.id == null)
            return null;

        return this.id.getValue();
    }
}
