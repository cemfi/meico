package meico.mpm.elements.maps;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.maps.data.OrnamentData;
import meico.mpm.elements.styles.OrnamentationStyle;
import meico.supplementary.KeyValue;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class interfaces MPM's ornamentationMaps
 * @author Axel Berndt
 */
public class OrnamentationMap extends GenericMap {
    /**
     * constructor, generates an empty OrnamentationMap
     * @throws Exception
     */
    private OrnamentationMap() throws Exception {
        super("ornamentationMap");
    }

    /**
     * constructor, generates an instance from xml code
     * @param xml
     * @throws Exception
     */
    private OrnamentationMap(Element xml) throws Exception {
        super(xml);
    }

    /**
     * OrnamentationMap factory
     * @return
     */
    public static OrnamentationMap createOrnamentationMap() {
        OrnamentationMap d;
        try {
            d = new OrnamentationMap();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    /**
     * OrnamentationMap factory
     * @param xml
     * @return
     */
    public static OrnamentationMap createOrnamentationMap(Element xml) {
        OrnamentationMap d;
        try {
            d = new OrnamentationMap(xml);
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
        this.setType("ornamentationMap");            // make sure this is really a "ornamentationMap"
    }

    /**
     * add an ornament element to the ornamentationMap
     * @param date
     * @param nameRef
     * @param scale set this to 1.0 to omit it from the xml code
     * @param noteOrder set this null or leave it empty to omit it from the xml code; provide just one string with "ascending pitch" or "descending pitch" to set this
     * @param id set this null or leave it empty to omit it from the xml code
     * @return the index at which the element has been added
     */
    public int addOrnament(double date, String nameRef, double scale, ArrayList<String> noteOrder, String id) {
        Element ornament = new Element("ornament", Mpm.MPM_NAMESPACE);
        ornament.addAttribute(new Attribute("date", Double.toString(date)));
        ornament.addAttribute(new Attribute("name.ref", nameRef));

        if (scale != 1.0)
            ornament.addAttribute(new Attribute("scale", Double.toString(scale)));

        if ((noteOrder != null) && !noteOrder.isEmpty()) {
            String noteIdsString = "";
            for (String nid : noteOrder) {
                if (nid.equals("ascending pitch") || nid.equals("descending pitch")) {
                    noteIdsString = nid;
                    break;
                } else {
                    noteIdsString = noteIdsString.concat(" #" + nid.trim().replace("#", ""));   // the replacement handles the case that the # is already in the string as we do not want to have multiple #
                }
            }
            ornament.addAttribute(new Attribute("note.order", noteIdsString.trim()));
        }

        if ((id != null) && !id.isEmpty())
            ornament.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id));

        KeyValue<Double, Element> kv = new KeyValue<>(date, ornament);
        return this.insertElement(kv, false);
    }

    /**
     * add an ornament element to the ornamentationMap
     * @param date
     * @param nameRef
     * @return the index at which the element has been added
     */
    public int addOrnament(double date, String nameRef) {
        return this.addOrnament(date, nameRef, 1.0, null, null);
    }

    /**
     * add an ornament element to the ornamentationMap
     * @param data xml data will be ignored (use addElement() instead to add an xml representation)
     * @return
     */
    public int addOrnament(OrnamentData data) {
        if (data.ornamentDef != null)
            data.ornamentDefName = data.ornamentDef.getName();
        else if (data.ornamentDefName == null) {
            System.err.println("Cannot add ornament: ornamentDef or ornamentDefName must be specified.");
            return -1;
        }
        return this.addOrnament(data.date, data.ornamentDefName, data.scale, data.noteOrder, data.xmlId);
    }

    /**
     * this collects the ornament data of a specified element in this map, given via the index number
     * @param index
     * @return the ornament data or null if the indexed element is no ornament element or invalid
     */
    public OrnamentData getOrnamentDataOf(int index) {
        if (this.elements.isEmpty() || (index < 0))
            return null;

        if (index >= this.elements.size())
            index = this.elements.size() - 1;

        Element xml = this.elements.get(index).getValue();
        if (xml.getLocalName().equals("ornament")) {
            OrnamentData od = new OrnamentData();

            Attribute OrnamentDefAtt = Helper.getAttribute("name.ref", xml);
            if (OrnamentDefAtt == null) {
                System.err.println("Error processing MPM ornamentationMap: no name.ref defined in " + xml.toXML() + ".");
                return null;
            }
            od.ornamentDefName = OrnamentDefAtt.getValue();

            // get the style that applies to this date
            od.styleName = "";
            for (int j = index; j >= 0; --j) {                                  // find the first style switch at or before date
                Element s = this.elements.get(j).getValue();
                if (s.getLocalName().equals("style")) {
                    od.styleName = Helper.getAttributeValue("name.ref", s);
                    break;
                }
            }
            od.style = (OrnamentationStyle) this.getStyle(Mpm.ORNAMENTATION_STYLE, od.styleName);   // read the ornamentation style
            if (od.style == null) {                                             // if there is no style
                System.err.println("Error processing MPM ornamentationMap: Unknown ornamentation style \"" + od.styleName + "\". Ornament " + xml.toXML() + " cannot be processed.");
                return null;                                                    // we have no look up for the ornament ref.name, hence cancel
            }

            od.ornamentDef = od.style.getDef(od.ornamentDefName);
            if (od.ornamentDef == null) {
                System.err.println("Error processing MPM ornamentationMap: Unknown ornamentDef reference in " + xml.toXML() + ".");
                return null;
            }

            od.date = this.elements.get(index).getKey();
            od.xml = xml;

            Attribute noteOrderAtt = xml.getAttribute("note.order");
            if (noteOrderAtt != null) {
                String no = noteOrderAtt.getValue().trim();
                od.noteOrder = new ArrayList<>();
                if (no.equals("ascending pitch") || no.equals("descending pitch"))
                    od.noteOrder.add(no);
                else
                    od.noteOrder.addAll(Arrays.asList(no.replaceAll("#", "").split("\\s+")));
            }

            Attribute scaleAtt = Helper.getAttribute("scale", xml);
            if (scaleAtt != null)
                od.scale = Double.parseDouble(scaleAtt.getValue());

            Attribute att = Helper.getAttribute("xml:id", xml);
            if (att != null)
                od.xmlId = att.getValue();
        }

        return null;
    }
    /**
     * On the basis of this ornamentationMap, edit the maps (MSM scores!).
     * This method is meant to be applied BEFORE the other transformations.
     * It will add only attributes to the MSM note elements which will be applied to the performance attributes later.
     * @param parts the MSM part elements which the ornamentationMap is applied to
     * @param ornamentationMap the global ornamentationMap
     */
    public static void renderGlobalOrnamentationToParts(ArrayList<Element> parts, OrnamentationMap ornamentationMap) {
        if ((ornamentationMap == null) || ornamentationMap.isEmpty())
            return;

        ArrayList<GenericMap> mapsToOrnament = new ArrayList<>();
        for (Element part : parts) {
            Element s = Helper.getFirstChildElement("dated", part);
            if (s != null) {
                s = Helper.getFirstChildElement("score", s);
                if (s != null) {                                        // if the part has a score (this is where ornamentation is applied)
                    mapsToOrnament.add(GenericMap.createGenericMap(s)); // add it to the mapsToOrnament list
                }
            }
        }

        // global ornamentation rendering will add only modifier attributes to the notes; these will be rendered into performance attributes in the local processing later on
        ornamentationMap.renderGlobalOrnamentationMap(mapsToOrnament);
    }

    /**
     * On the basis of this ornamentationMap, edit the maps (MSM scores!).
     * This method is meant to be applied BEFORE the other transformations.
     * It will add only attributes to the MSM note elements which will be applied to the performance attributes later.
     * @param maps the MSM scores to which the ornamentationMap is applied
     */
    public void renderGlobalOrnamentationMap(ArrayList<GenericMap> maps) {
        if ((maps == null) || maps.isEmpty())
            return;

        this.apply(maps);
    }

    /**
     * On the basis of the specified ornamentationMap, add/edit the corresponding data to all note elements of the specified map.
     * Basically, that map should be an MSM score because only note elements will be processed.
     * @param map MSM score
     * @param ornamentationMap
     */
    public static void renderOrnamentationToMap(GenericMap map, OrnamentationMap ornamentationMap) {
        if (ornamentationMap != null)
            ornamentationMap.renderOrnamentationToMap(map);
    }

    /**
     * On the basis of the specified ornamentationMap, add/edit the corresponding data to all note elements of the specified map.
     * Basically, that map should be an MSM score because only note elements will be processed.
     * A global ornamentationMap should be processed via renderGlobalOrnamentationToParts() or renderGlobalOrnamentationMap()
     * before invokiing this method.
     * @param map MSM score
     */
    public void renderOrnamentationToMap(GenericMap map) {
        if (map == null)
            return;

        if (this.getLocalHeader() != null) { // this is a local ornamentationMap; global ones were already processed via renderGlobalOrnamentationMap(ArrayList<Element> maps)
            ArrayList<GenericMap> maps = new ArrayList<>();
            maps.add(map);
            this.apply(maps);
        }

        this.renderAllNonmillisecondsModifiersToMap(map);   // render ornamentation modifier attributes into .perf and velocity attributes
    }

    /**
     * Core part of the ornamentation rendering. This method does not add or edit any
     * performance attributes (xx.perf and velocity) on the map elements. It will only
     * add attributes that will later be used to set the performance attributes.
     * It also adds new notes and marks notes to be deleted from the performance via the respective OrnamentData.apply() invocation.
     * @param maps list of MSM scores
     */
    private void apply(ArrayList<GenericMap> maps) {
        if (maps.isEmpty())
            return;

        if ((this.getLocalHeader() == null) && (this.getGlobalHeader() == null)) {
            System.err.println("Error processing MPM ornamentationMap: no header defined to look up ornamentationStyle.");
            return;
        }

        // create a hashmap of all note elements, hashed by their ID, so we have quick access to them later on
        HashMap<String, Element> notes = new HashMap<>();
        for (GenericMap map : maps) {
            for (KeyValue<Double, Element> note : map.getAllElementsOfType("note")) {
                Attribute id = Helper.getAttribute("id", note.getValue());
                if (id != null)
                    notes.put(id.getValue(), note.getValue());
            }
        }

        OrnamentationStyle style = null;

        // process each ornament entry in this ornamentationMap
        for (int i = 0; i < this.size(); ++i) {
            Element ornamentXml = this.getElement(i);

            // get the lookup style for subsequent ornaments
            if (ornamentXml.getLocalName().equals("style")) {
                if (this.getLocalHeader() != null)
                    style = (OrnamentationStyle) this.getLocalHeader().getStyleDef(Mpm.ORNAMENTATION_STYLE, Helper.getAttributeValue("name.ref", ornamentXml));
                if ((style == null) && (this.getGlobalHeader() != null))
                    style = (OrnamentationStyle) this.getGlobalHeader().getStyleDef(Mpm.ORNAMENTATION_STYLE, Helper.getAttributeValue("name.ref", ornamentXml));
                continue;
            }

            if ((style == null) || !ornamentXml.getLocalName().equals("ornament"))  // without a style we cannot interpret any ornament's name.ref attribute; if it is no ornament we cannot process it either
                continue;           // skip the current element

            // read all data into an OrnamentData instance
            OrnamentData od = new OrnamentData();
            od.style = style;
//            od.styleName = style.getName();   // not required

            Attribute OrnamentDefAtt = Helper.getAttribute("name.ref", ornamentXml);
            if (OrnamentDefAtt == null)
                continue;
            od.ornamentDefName = OrnamentDefAtt.getValue();
            od.ornamentDef = od.style.getDef(od.ornamentDefName);
            if (od.ornamentDef == null)
                continue;

            od.date = this.elements.get(i).getKey();
//            od.xml = xml;     // not required

            Attribute scaleAtt = Helper.getAttribute("scale", ornamentXml);
            if (scaleAtt != null)
                od.scale = Double.parseDouble(scaleAtt.getValue());

//            Attribute att = Helper.getAttribute("xml:id", xml);   // not required
//            if (att != null)
//                od.xmlId = att.getValue();

            // determine the note order and collect the notes which the ornament will be applied to
            int noteOrderAscending = 1;                         // 1 = ascending pitch, -1 = descending pitch, 0 = ID sequence
            ArrayList<ArrayList<Element>> chordSequence = null; // this will hold the chord/note sequence
            Attribute noteOrderAtt = ornamentXml.getAttribute("note.order");
            if (noteOrderAtt != null) {
                String no = noteOrderAtt.getValue().trim();
                switch (no) {
                    case "ascending pitch":
//                        noteOrderAscending = 1;               // default initialization
                        break;
                    case "descending pitch":
                        noteOrderAscending = -1;
                        break;
                    default:                                    // attribute note.order is a list of reference IDs to be read into the notes list
                        // TODO: parse brackets to create "sub-chords"
                        od.noteOrder = new ArrayList<>(Arrays.asList(no.replaceAll("#", "").split("\\s+")));
                        if (od.noteOrder.isEmpty())             // empty note list, hence no notes to ornament
                            continue;                           // continue with the next ornament
                        chordSequence = new ArrayList<>();
                        noteOrderAscending = 0;
                        for (String ref : od.noteOrder) {
                            Element note = notes.get(ref);
                            if (note != null) {
                                ArrayList<Element> chord = new ArrayList<>();
                                chord.add(note);
                                chordSequence.add(chord);
                            }
                        }
                }
            }
            if (chordSequence == null) {                         // if no note sequence was specified so far
                chordSequence = new ArrayList<>();
                for (GenericMap map : maps) {
                    ArrayList<KeyValue<Double, Element>> notesAtDate = map.getAllElementsAt(od.date);
                    for (KeyValue<Double, Element> note : notesAtDate) {
                        if (note.getValue().getLocalName().equals("note")) {
                            ArrayList<Element> chord = new ArrayList<>();
                            chord.add(note.getValue());
                            chordSequence.add(chord);
                        }
                    }
                }
                if (chordSequence.isEmpty())
                    continue;

                // sort the chords in the indicated order on the basis of the chord's first note's pitch; notes within chord are not reordered
                int finalNoteOrderAscending = noteOrderAscending;
                chordSequence.sort((n1, n2) -> {
                    double pitch1 = Double.parseDouble(Helper.getAttributeValue("midi.pitch", n1.get(0)));
                    double pitch2 = Double.parseDouble(Helper.getAttributeValue("midi.pitch", n2.get(0)));
                    return ((int) Math.signum(pitch1 - pitch2)) * finalNoteOrderAscending;
                });
            }

            // apply the ornament to the notes; this will create/edit their ornament.xx attributes for further
            // processing later in the performance rendering pipeline; if new notes are generated, these will be
            // returned by apply() and added to the first map in maps
            for (ArrayList<Element> chord : od.apply(chordSequence)) {
                for (Element note : chord) {
                    maps.get(0).addElement(note);   // TODO: is there a better way to find out to which map the note should be added?
                }
            }
        }
    }

    /**
     * render ornamentation modifier attributes into .perf and velocity attributes
     * this includes attributes
     *      - ornament.dynamics
     *      - ornament.date.offset (an offset) ... ornament.milliseconds.date.offset comes later in the rendering pipeline,
     *      - ornament.duration (absolute duration) ... ornament.milliseconds.duration comes later in the rendering pipeline,
     *      - ornament.noteoff.shift (true or absent=false),
     * @param map
     */
    private void renderAllNonmillisecondsModifiersToMap(GenericMap map) {
        for (KeyValue<Double, Element> e : map.getAllElementsOfType("note")) {
            Element note = e.getValue();

            // add ornament.dynamics to the velocity value
            Attribute ornamentDynamics = Helper.getAttribute("ornament.dynamics", note);
            if (ornamentDynamics != null) {
                Attribute velocity = Helper.getAttribute("velocity", note);
                if (velocity != null) {                     // if this attribute is missing, we have no basic dynamics to add the ornament dynamics to, so this is mandatory
                    velocity.setValue(String.valueOf(Double.parseDouble(velocity.getValue()) + Double.parseDouble(ornamentDynamics.getValue())));
                }
            }

            // add ornament.date.offset to date.perf, set date.end.perf according to ornament.duration or ornament.noteoff.shift, resp.
            Attribute ornamentDateOffsetAtt = Helper.getAttribute("ornament.date.offset", note);
            if (ornamentDateOffsetAtt != null) {                                                        // if the ornament shifts the date of the event/note
                Attribute datePerfAtt = Helper.getAttribute("date.perf", note);                         // get the date of the note so far
                if (datePerfAtt != null) {                                                              // this attribute is mandatory for all further timing transformations
                    double datePerf = Double.parseDouble(datePerfAtt.getValue());                       // read its value
                    double ornamentDateOffset = Double.parseDouble(ornamentDateOffsetAtt.getValue());   // read the value of the offset
                    datePerfAtt.setValue(String.valueOf(datePerf + ornamentDateOffset));                // update the date with the offset value

                    Attribute dateEndPerfAtt = Helper.getAttribute("date.end.perf", note);              // get the end date attribute
                    Attribute durationPerfAtt = Helper.getAttribute("duration.perf", note);             // get the duration attribute

                    Attribute ornamentDurationAtt = Helper.getAttribute("ornament.duration", note);     // does the ornament set an absolute note duration?
                    if (ornamentDurationAtt != null) {                                                  // apply it to duration.perf and date.end.perf
                        if (durationPerfAtt != null)
                            durationPerfAtt.setValue(ornamentDurationAtt.getValue());                   // update the note's duration
                        else
                            note.addAttribute(new Attribute("duration.perf", ornamentDurationAtt.getValue()));
                        if (dateEndPerfAtt != null)
                            dateEndPerfAtt.setValue(String.valueOf(datePerf + ornamentDateOffset + Double.parseDouble(ornamentDurationAtt.getValue()))); // update the end date of the note
                        else
                            note.addAttribute(new Attribute("date.end.perf", String.valueOf(datePerf + ornamentDateOffset + Double.parseDouble(ornamentDurationAtt.getValue()))));
                    } else {                                                                            // act according to noteoff.shift
                        Attribute ornamentNoteoffShiftAtt = Helper.getAttribute("ornament.noteoff.shift", note);
                        if (ornamentNoteoffShiftAtt != null) {                                          // this attribute is only created when its value is "true", so we need to update date.end.perf; thus, duration stays the same
                            if (dateEndPerfAtt != null)
                                dateEndPerfAtt.setValue(String.valueOf(Double.parseDouble(dateEndPerfAtt.getValue()) + ornamentDateOffset)); // update the end date of the note
                        } else {                                                                        // ornament.noteOff.shift="false", so we need to update duration.perf; thus, date.end.perf stays the same
                        if (durationPerfAtt != null)
                            durationPerfAtt.setValue(String.valueOf(Double.parseDouble(durationPerfAtt.getValue()) - ornamentDateOffset));;
                        }
                    }
                }
            }
        }
    }

    /**
     * render ornamentation milliseconds modifier attributes into performance attributes:
     *      - ornament.milliseconds.date.offset into milliseconds.date
     *      - ornament.milliseconds.duration into milliseconds.date.end
     *      - ornament.noteoff.shift (true/false)
     * @param map
     * @param ornamentationMap
     */
    public static void renderMillisecondsModifiersToMap(GenericMap map, OrnamentationMap ornamentationMap) {
        if ((ornamentationMap == null) || (map == null))
            return;

        for (KeyValue<Double, Element> e : map.getAllElementsOfType("note")) {
            Element note = e.getValue();
            Attribute millisecondsDateAtt = Helper.getAttribute("milliseconds.date", note);
            if (millisecondsDateAtt == null)                                                                            // without this attribute we have no reference for all the transformations
                continue;
            double millisecondsDate = Double.parseDouble(millisecondsDateAtt.getValue());

            Attribute ornamentMillisecondsDateAtt = Helper.getAttribute("ornament.milliseconds.date.offset", note);
            double ornamentMillisecondsDateOffset = 0.0;
            if (ornamentMillisecondsDateAtt != null) {
                ornamentMillisecondsDateOffset = Double.parseDouble(ornamentMillisecondsDateAtt.getValue());
                millisecondsDateAtt.setValue(String.valueOf(millisecondsDate + ornamentMillisecondsDateOffset));
            }

            Attribute millisecondsDateEndAtt = Helper.getAttribute("milliseconds.date.end", note);
            Attribute ornamentMillisecondsDurationAtt = Helper.getAttribute("ornament.milliseconds.duration", note);    // does the ornament set an absolute duration?
            if (ornamentMillisecondsDurationAtt != null) {                                                              // apply it to milliseconds.date.end
                double ornamentMillisecondsDuration = Double.parseDouble(ornamentMillisecondsDurationAtt.getValue());   // get the new duration value
                if (millisecondsDateEndAtt != null)
                    millisecondsDateEndAtt.setValue(String.valueOf(millisecondsDate + ornamentMillisecondsDateOffset + ornamentMillisecondsDuration));  // set milliseconds.date.end
                else
                    note.addAttribute(new Attribute("milliseconds.date.end", String.valueOf(millisecondsDate + ornamentMillisecondsDateOffset + ornamentMillisecondsDuration)));
            } else {                                                                                                    // act according to noteoff.shift
                Attribute ornamentNoteoffShiftAtt = Helper.getAttribute("ornament.noteoff.shift", note);
                if (ornamentNoteoffShiftAtt != null) {                                                                  // this attribute is only created when its value is "true", so we need to update milliseconds.date.end.perf; thus, the duration stays the same
                    if (millisecondsDateEndAtt != null)
                        millisecondsDateEndAtt.setValue(String.valueOf(Double.parseDouble(millisecondsDateEndAtt.getValue()) + ornamentMillisecondsDateOffset)); // update the end date of the note
                } // else, ornament.noteOff.shift="false", so milliseconds.date.end remains unalteres
            }
        }
    }
}
