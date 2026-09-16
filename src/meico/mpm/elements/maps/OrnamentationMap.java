package meico.mpm.elements.maps;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.mpm.elements.maps.data.OrnamentData;
import meico.mpm.elements.styles.OrnamentationStyle;
import meico.mpm.elements.styles.defs.OrnamentDef;
import meico.msm.elements.MsmNoteElement;
import meico.xml.RichElement;
import meico.supplementary.KeyValue;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.*;

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
     * Creates a deep copy of this OrnamentationMap.
     * @return a new OrnamentationMap with the same data as this one
     */
    public OrnamentationMap clone() {
        OrnamentationMap clone = createOrnamentationMap();

        clone.setId(this.getId());
        clone.setType(this.getType());
        clone.setHeaders(this.getGlobalHeader(), this.getLocalHeader());
        for(KeyValue<Double, Element> elem : this.getAllElements()) {
            KeyValue<Double, Element> e = new KeyValue<>(elem.getKey(), elem.getValue().copy());
            clone.insertElement(e);
        }

        return clone;
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
    public int addOrnament(double date, String nameRef, double scale, ArrayList<String> noteOrder, ArrayList<Element> childNotes, int repetitions, String id, String correspondence) {
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
                    noteIdsString = noteIdsString.concat(" " + nid.trim());
                }
            }
            ornament.addAttribute(new Attribute("note.order", noteIdsString.trim()));
        }

        if((childNotes != null)) {
            for (Element childNote : childNotes) {
                ornament.appendChild(childNote);
            }
        }

        ornament.addAttribute(new Attribute("repetitions", String.valueOf(repetitions)));

        if ((id != null) && !id.isEmpty())
            ornament.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", id));

        if ((correspondence != null) && !correspondence.isEmpty())
            ornament.addAttribute(new Attribute("noteid", correspondence));

        KeyValue<Double, Element> kv = new KeyValue<>(date, ornament);
        return this.insertElement(kv, false);
    }

    /**
     * add an ornament element to the ornamentationMap
     * @param date
     * @param nameRef
     * @param scale
     * @param noteOrder
     * @param id
     * @return the index at which the element has been added
     */
    public int addOrnament(double date, String nameRef, double scale, ArrayList<String> noteOrder, String id) {
        return this.addOrnament(date, nameRef, scale, noteOrder, null, 0, id, null);
    }

    /**
     * add an ornament element to the ornamentationMap
     * @param date
     * @param nameRef
     * @return the index at which the element has been added
     */
    public int addOrnament(double date, String nameRef) {
        return this.addOrnament(date, nameRef, 1.0, null, null, 0, null, null);
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
        return this.addOrnament(data.date, data.ornamentDefName, data.scale, data.noteOrder, data.notes, data.repetitions, data.xmlId, data.correspondence);
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

            od.correspondence = Helper.getAttributeValue("noteid", xml);

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
     * @return returns a map of principal note IDs and the IDs of the notes that have been added to the map for that principal note
     */
    public static Map<String, ArrayList<String>> renderGlobalOrnamentationToParts(ArrayList<Element> parts, OrnamentationMap ornamentationMap) {
        if ((ornamentationMap == null) || ornamentationMap.isEmpty())
            return new HashMap<>();

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
        return ornamentationMap.renderGlobalOrnamentationMap(mapsToOrnament);
    }

    /**
     * On the basis of this ornamentationMap, edit the maps (MSM scores!).
     * This method is meant to be applied BEFORE the other transformations.
     * It will add only attributes to the MSM note elements which will be applied to the performance attributes later.
     * @param maps the MSM scores to which the ornamentationMap is applied
     * @return returns a map of principal note IDs and the IDs of the notes that have been added to the map for that principal note
     */
    public Map<String, ArrayList<String>> renderGlobalOrnamentationMap(ArrayList<GenericMap> maps) {
        if ((maps == null) || maps.isEmpty())
            return new HashMap<>();

        return new HashMap<>(); // this.apply(maps);
    }

    /**
     * On the basis of the specified ornamentationMap, add/edit the corresponding data to all note elements of the specified map.
     * Basically, that map should be an MSM score because only note elements will be processed.
     * @param map MSM score
     * @param ornamentationMap
     * @return returns a map of principal note IDs and the IDs of the notes that have been added to the map for that principal note
     */
    public static Map<String, ArrayList<String>> renderOrnamentationToMap(GenericMap map, OrnamentationMap ornamentationMap) {
        if (ornamentationMap != null)
            return ornamentationMap.renderOrnamentationToMap(map);
        return new HashMap<>();
    }

    /**
     * On the basis of the specified ornamentationMap, add/edit the corresponding data to all note elements of the specified map.
     * Basically, that map should be an MSM score because only note elements will be processed.
     * A global ornamentationMap should be processed via renderGlobalOrnamentationToParts() or renderGlobalOrnamentationMap()
     * before invokiing this method.
     * @param map MSM score
     * @return returns a map of principal note IDs and the IDs of the notes that have been added to the map for that principal note
     */
    public Map<String, ArrayList<String>> renderOrnamentationToMap(GenericMap map) {
        Map<String, ArrayList<String>> addedNotes = new HashMap<>();
        if (map == null)
            return addedNotes;

        if (this.getLocalHeader() != null) { // this is a local ornamentationMap; global ones were already processed via renderGlobalOrnamentationMap(ArrayList<Element> maps)
            ArrayList<GenericMap> maps = new ArrayList<>();
            maps.add(map);
            //addedNotes = this.apply(maps);
        }

        this.renderAllNonmillisecondsModifiersToMap(map);   // render ornamentation modifier attributes into .perf and velocity attributes

        //this.sanitizeOverlaps(map);
        return addedNotes;
    }

    /**
     * All ornamentation notes are added to the map (including resolving repetitions like in trills),
     * and the performance attributes of the notes are set according to the ornamentation data.
     * This is meant to be applied before all other transformations,
     * as it will add new notes to the map which might be processed by the other transformations as well.
     * @param map
     * @return returns a map of principal note IDs and the IDs of the notes that have been added to the map for that principal note
     */
    public Map<String, ArrayList<String>> applyNotesToMaps(GenericMap map) {
        ArrayList<Element> toBeRemoved = new ArrayList<>();
        ArrayList<KeyValue<Double, Element>> notes = map.getAllElements();
        Map<String, ArrayList<String>> addedNotes = new HashMap<>();

        for (int i = 0; i < this.size(); ++i) {  // for each ornament
            RichElement ornament = new RichElement(this.getElement(i));
            String correspondenceId = ornament.get("noteid");
            MsmNoteElement principalNote = getElementById(notes, correspondenceId);

            if(principalNote == null) {
                if(ornament.has("note.order")) // in case of an arpeggio, i.e.
                    ornament.set("note.order.perf", ornament.get("note.order"));
                continue;
            }

            ornament.copyValue("date", principalNote);
            ArrayList<MsmNoteElement> children = new MsmNoteElement(ornament.getElement()).getChildrenAsMsmElements();
            ArrayList<String> noteOrder = new ArrayList<>();
            Map<Integer, Integer> repeats = new HashMap<>();

            if(ornament.has("note.order"))
                noteOrder = new ArrayList<>(Arrays.asList(ornament.get("note.order").replaceAll(":\\|:", ":| |:").split(" ")));

            int chordIndex = 0;
            int repeatStart = chordIndex;
            ArrayList<String> chords = new ArrayList<>();
            StringBuilder chord = new StringBuilder("[");
            boolean isCollectingChord = false;

            for(int j = 0; j < noteOrder.size(); /**/) {
                String order = noteOrder.get(j);

                if(!isCollectingChord) {
                    chord = new StringBuilder("[");
                }

                if(order.equals("[")) {
                    isCollectingChord = true;
                    j++;
                    continue;
                }

                if(order.contains("#")) {
                    String o = order.replaceAll("#", "");
                    chord.append(" ").append(o);
                    noteOrder.set(j, o);
                    j++;
                }

                if(order.equals("]")) {
                    isCollectingChord = false;
                    j++;
                }

                if(order.contains("|")) {
                    switch (order) {
                        case "|:":
                            repeatStart = chordIndex;
                            break;
                        case ":|":
                            repeats.put(repeatStart, chordIndex);
                            break;
                        case "|":
                            break;
                    }

                    noteOrder.remove(j);
                    continue;
                }

                if(!isCollectingChord) {
                    chord.append(" ]");
                    chords.add(chord.toString());
                    chordIndex++;
                }
            }

            if(!repeats.isEmpty()) { // insert a repetition, if it is needed;
                ArrayList<String> notesToAdd = new ArrayList<>();
                int rptStart = repeats.keySet().iterator().next();
                int rptEnd = repeats.get(rptStart);
                int rptNotesAmount = rptEnd - rptStart;

                double maxNotes = chords.size();
                String repetitions = ornament.get("repetitions");
                if(repetitions != null && !repetitions.equals("-1")) {
                    maxNotes = (Double.parseDouble(repetitions) + 1.0) * rptNotesAmount; // play at least once ("+ 1.0"), if no repetition ("repetitions == 0")
                }
                else {
                    //Double rel = principalNote.getDuration() / noteOrder.size();
                    int rptNoteLength = 150;
                    maxNotes = Math.ceil((principalNote.getAsDouble("milliseconds.date.end") - principalNote.getAsDouble("milliseconds.date")) / rptNoteLength);
                }

                while(maxNotes >= (notesToAdd.size() + chords.size() + rptNotesAmount)) {
                    for (int k = rptStart; k < rptEnd; ++k) {
                        notesToAdd.add(chords.get(k));
                    }
                }
                for(MsmNoteElement child : children) {
                    if(child.getId().equals(chords.get(rptStart).replaceAll("\\[|\\]", "").trim())) {
                        MsmNoteElement note = new MsmNoteElement(child.getElement());
                        if(note.has("intm") && note.get("intm").equals("0.0hs"))
                            notesToAdd.add(chords.get(rptStart)); // always land on principal note of the repetition, might add doubles -> need to sanitize
                        break;
                    }
                }
                for(String n : notesToAdd) {
                    chords.add(rptEnd, n);
                    rptEnd++;
                }

            }

            String chordsString = String.join(" ", chords);

            noteOrder = new ArrayList<String>(Arrays.asList(chordsString.split(" ")));

            MsmNoteElement lastNote = null;
            boolean hasSamePitches = true;
            MsmNoteElement firstNote = children.get(0);
            for(MsmNoteElement child : children) {
                if(!child.get("midi.pitch").equals(firstNote.get("midi.pitch"))) {
                    hasSamePitches = false;
                    break;
                }
            }

            for (int j = 0; j < noteOrder.size();) {
                String order = noteOrder.get(j);

                if(order.equals("[") || order.equals("]")) {
                    j++;
                    continue;
                }

                MsmNoteElement note = null;
                for(MsmNoteElement child : children) {
                    if(child.getId().equals(order)) {
                        note = new MsmNoteElement(child.getElement(), true);
                        break;
                    }
                }
                if(!hasSamePitches && note != null && lastNote != null && note.get("midi.pitch").equals(lastNote.get("midi.pitch"))) { // sanitze double notes, which can occur due to repetitions; if the note is the same as the last one, we can skip it, as it would be redundant
                    note = null;
                }

                if(note == null) {
                    noteOrder.remove(j);
                    continue;
                }
                lastNote = note;

                copyNotePerfInformation(note, principalNote);

                noteOrder.set(j, note.getId());
                map.addElement(note.getElement());
                addedNotes.computeIfAbsent(correspondenceId, k -> new ArrayList<>()).add(note.getId());
                ++j;
            }
            ornament.set("note.order.perf", String.join(" ", noteOrder));
        }

        for(Element element : toBeRemoved) {
            map.removeElement(Helper.getAttributeValue("id", element));
        }

        return addedNotes;
    }

    /**
     * returns the element with id from givin elements.
     * @param elements
     * @param id
     * @return possibly null if not found
     */
    private static MsmNoteElement getElementById(ArrayList<KeyValue<Double, Element>> elements, String id) {
        MsmNoteElement result = null;
        for(KeyValue<Double, Element> dateElement : elements) {  // find the element
            MsmNoteElement candidate = new MsmNoteElement(dateElement.getValue());
            if (candidate.getId().equals(id)) {
                result = candidate;
                break;
            }
        }
        return result;
    }

    /**
     * copy the performance attributes of the original note to the ornament note.
     * @param note
     * @param principalNote
     */
    private static void copyNotePerfInformation(MsmNoteElement note, MsmNoteElement principalNote) {
        note.createNewId(); // we want a new ID as we might generated multiple notes from the seed note
        if(principalNote == null)
            return;

        for(int i = 0; i < principalNote.getElement().getAttributeCount(); i++) {
            Attribute attr = principalNote.getElement().getAttribute(i);
            if(     !( attr.getLocalName().equals("id")
                    || attr.getLocalName().equals("intm")
                    || attr.getLocalName().equals("midi.pitch")
                    || attr.getLocalName().equals("octave")
                    || attr.getLocalName().equals("accidentals")
                    || attr.getLocalName().equals("pitchname")))
                note.getElement().addAttribute(new Attribute(attr.getLocalName(), attr.getValue()));
        }
    }

    /**
     * Core part of the ornamentation rendering. This method does not add or edit any
     * performance attributes (xx.perf and velocity) on the map elements. It will only
     * add attributes that will later be used to set the performance attributes.
     * It also adds new notes and marks notes to be deleted from the performance via the respective OrnamentData.apply() invocation.
     * @param maps list of MSM scores
     */
    private ArrayList<OrnamentEntry> apply(ArrayList<GenericMap> maps) {
        ArrayList<OrnamentEntry> ornamentEntries = new ArrayList<>();

        if (maps.isEmpty())
            return ornamentEntries;

        if ((this.getLocalHeader() == null) && (this.getGlobalHeader() == null)) {
            System.err.println("Error processing MPM ornamentationMap: no header defined to look up ornamentationStyle.");
            return ornamentEntries;
        }

        Map<String, ArrayList<String>> addedNotes = new HashMap<>();
        for(GenericMap map : maps) {
            addedNotes = applyNotesToMaps(map);
        }

        // create a hashmap of all note elements, hashed by their ID, so we have quick access to them later on
        HashMap<String, Element> notes = getNotes(maps);

        // Phase 1: collect all OrnamentData and their chordSequences, grouped by groupId
        OrnamentationStyle style = null;

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

            if ((style == null) || !ornamentXml.getLocalName().equals("ornament"))
                continue;

            OrnamentData od = new OrnamentData(this.elements.get(i).getValue());
            od.style = style;

            Attribute OrnamentDefAtt = Helper.getAttribute("name.ref", ornamentXml);
            if (OrnamentDefAtt == null)
                continue;
            od.ornamentDefName = OrnamentDefAtt.getValue();
            od.ornamentDef = od.style.getDef(od.ornamentDefName);
            if (od.ornamentDef == null)
                continue;

            // determine the chord sequence
            int noteOrderAscending = 0;
            ArrayList<ArrayList<Element>> chordSequence = new ArrayList<>();
            Attribute noteOrderAtt = ornamentXml.getAttribute("note.order.perf");
            if (noteOrderAtt != null) {
                String no = noteOrderAtt.getValue().trim();
                switch (no) {
                    case "ascending pitch":
                        noteOrderAscending = 1;
                        break;
                    case "descending pitch":
                        noteOrderAscending = -1;
                        break;
                }

                od.noteOrder = new ArrayList<>(Arrays.asList(no.replaceAll("#", "").split("\\s+")));
                if (od.noteOrder.isEmpty())
                    continue;

                noteOrderAscending = 0;

                ArrayList<Element> chord = new ArrayList<>();
                boolean isCollectingChord = false;
                for (String ref : od.noteOrder) {
                    if(!isCollectingChord)
                        chord = new ArrayList<>();

                    if(ref.equals("[")) {
                        isCollectingChord = true;
                        continue;
                    }
                    if(ref.equals("]")) {
                        isCollectingChord = false;
                        if(!chord.isEmpty()) {
                            chordSequence.add(chord);
                        }
                        continue;
                    }

                    Element note = notes.get(ref);
                    if (note != null) {
                        chord.add(note);
                    }

                    if(!isCollectingChord && !chord.isEmpty()) {
                        chordSequence.add(chord);
                    }
                }
            }

            if (chordSequence.isEmpty())
                continue;

            int finalNoteOrderAscending = noteOrderAscending;
            if(finalNoteOrderAscending != 0) {
                chordSequence.sort((n1, n2) -> {
                    double pitch1 = Double.parseDouble(Helper.getAttributeValue("midi.pitch", n1.get(0)));
                    double pitch2 = Double.parseDouble(Helper.getAttributeValue("midi.pitch", n2.get(0)));
                    return ((int) Math.signum(pitch1 - pitch2)) * finalNoteOrderAscending;
                });
            }

            ornamentEntries.add(new OrnamentEntry(od, chordSequence));
        }

        return ornamentEntries;
    }

    /**
     * returns a map with note ids to their note element
     * @param maps the genericMap from where we collect the notes
     * @return
     */
    private static HashMap<String, Element> getNotes(ArrayList<GenericMap> maps) {
        HashMap<String, Element> notes = new HashMap<>();
        for (GenericMap map : maps) {
            for (KeyValue<Double, Element> note : map.getAllElementsOfType("note")) {
                Attribute id = Helper.getAttribute("id", note.getValue());
                if (id != null)
                    notes.put(id.getValue(), note.getValue());
            }
            for (KeyValue<Double, Element> note : map.getAllElementsOfType("rest")) {
                Attribute id = Helper.getAttribute("id", note.getValue());
                if (id != null)
                    notes.put(id.getValue(), note.getValue());
            }
        }
        return notes;
    }

    /**
     * spaces ornaments across the principal note's duration
     * @param maps
     * @return
     */
    private Map<String, ArrayList<String>> spaceOrnaments(ArrayList<GenericMap> maps, ArrayList<OrnamentEntry> ornamentEntries) {
        Map<String, ArrayList<String>> addedNotes = new HashMap<>();

        if (maps.isEmpty())
            return addedNotes;

        if ((this.getLocalHeader() == null) && (this.getGlobalHeader() == null)) {
            System.err.println("Error processing MPM ornamentationMap: no header defined to look up ornamentationStyle.");
            return addedNotes;
        }

        // create a hashmap of all note elements, hashed by their ID, so we have quick access to them later on
        HashMap<String, Element> notes = getNotes(maps);
        Map<String, ArrayList<OrnamentEntry>> groups = new LinkedHashMap<>();
        for (OrnamentEntry entry : ornamentEntries) {
            groups.computeIfAbsent(entry.od.correspondence, k -> new ArrayList<>()).add(entry);
        }


        MsmNoteElement lastNote = null; // where we might render into
        // For each group: compute proportional distribution and apply
        for (Map.Entry<String, ArrayList<OrnamentEntry>> groupEntry : groups.entrySet()) {
            ArrayList<OrnamentEntry> group = groupEntry.getValue();
            String correspondenceId = groupEntry.getKey();

            MsmNoteElement principalNote = null;
            if(notes.containsKey(correspondenceId)) {
                principalNote = new MsmNoteElement(notes.get(correspondenceId));
            }

            if (group.isEmpty())
                continue;

            // determine the principal note duration in ticks from the first chord of the first entry
            double principalDuration = getPrincipalDuration(group.get(0).chordSequence);
            if(principalNote != null) {
                principalDuration = principalNote.getMillisecondsDuration();
                if(principalNote.has("ornament.duration")) {
                    principalDuration = principalNote.getAsDouble("ornament.duration");
                }
            }

            // separate into "front" (not atEnd) and "end" (atEnd) ornaments, preserving order
            ArrayList<OrnamentEntry> frontOrnaments = new ArrayList<>();
            ArrayList<OrnamentEntry> endOrnaments = new ArrayList<>();
            for (OrnamentEntry entry : group) {
                if(principalNote != null && principalNote.has("ornament.milliseconds.date.offset")) {
                    entry.od.date = principalNote.getMillisecondsDate() + principalNote.getAsDouble("ornament.milliseconds.date.offset");
                    new MsmNoteElement(entry.od.xml).set("milliseconds.date", entry.od.date);
                    //new MsmElement(entry.od.xml).set("date", entry.od.date);

                    for(ArrayList<Element> chord : entry.chordSequence) {
                        for(Element note : chord)
                            new MsmNoteElement(note).set("milliseconds.date", entry.od.date);
                    }
                }

                if (isAtEnd(entry.od))
                    endOrnaments.add(entry);
                else
                    frontOrnaments.add(entry);
            }

            // resolve each ornament's raw frameLength in ticks
            double totalRawLength = 0.0;
            ArrayList<Double> rawLengths = new ArrayList<>();
            ArrayList<Double> rawStarts = new ArrayList<>();
            totalRawLength = getTotalRawLength(group, principalDuration, rawLengths, rawStarts);

            // proportional scaling if total exceeds principal note duration
            double scaleFactor = (totalRawLength > principalDuration && totalRawLength > 0.0)
                    ? principalDuration / totalRawLength
                    : 1.0;

            // apply front ornaments sequentially from the beginning
            double cursor = 0.0;
            for (OrnamentEntry entry : frontOrnaments) {
                int idx = group.indexOf(entry);
                double effectiveLength = rawLengths.get(idx) * scaleFactor;
                double effectiveStart = cursor + rawStarts.get(idx);
                KeyValue<Double, Double> entryResult = entry.od.apply(entry.chordSequence, effectiveStart, effectiveLength, lastNote);
                entry.effectiveStart = entryResult.getKey() + entry.od.date;
                entry.effectiveLength = entryResult.getValue();
                entry.effectiveEnd = entry.effectiveStart + entry.effectiveLength;

                cursor = entryResult.getKey() + entryResult.getValue();
                //entry.calcEffectives();
                lastNote = entry.getLastNote();
            }

            double neededSpace = 0.0f;
            for (int i = endOrnaments.size() - 1; i >= 0; i--) {
                OrnamentEntry entry = endOrnaments.get(i);
                int idx = group.indexOf(entry);
                double effectiveLength = rawLengths.get(idx) * scaleFactor;
                neededSpace += effectiveLength;
            }

            neededSpace = getTotalRawLength(endOrnaments, principalDuration, new ArrayList<>(), new ArrayList<>());

            cursor = Math.max(cursor, principalDuration - neededSpace);

            // apply end ornaments from the end backwards
            double endCursor = principalDuration;
            for (OrnamentEntry entry : endOrnaments) {
                int idx = group.indexOf(entry);
                double effectiveLength = rawLengths.get(idx) * scaleFactor;
                double effectiveStart = cursor + rawStarts.get(idx);
                KeyValue<Double, Double> entryResult = entry.od.apply(entry.chordSequence, effectiveStart, effectiveLength, lastNote);
                entry.effectiveStart = entryResult.getKey() + entry.od.date;
                entry.effectiveLength = entryResult.getValue();
                entry.effectiveEnd = entry.effectiveStart + entry.effectiveLength;

                cursor = entryResult.getKey() + entryResult.getValue();
                lastNote = entry.getLastNote();
            }
            lastNote = null;

            // Cut the principal note: carve the ornament time frame(s) out of the principal
            // note's sounding duration so that ornament notes and principal note do not overlap.
            //
            // For each fragment we silence the original element and re-insert a new "split note"
            // that covers only the ornament-free window [cursor, endCursor).
            if (principalNote != null && ((cursor >= 0.0) || (endCursor < principalDuration))) {
                double principalStart = principalNote.getDate();
                if(principalNote.has("ornament.milliseconds.date.offset"))
                    principalStart += principalNote.getAsDouble("ornament.milliseconds.date.offset");
                double principalEnd  = principalStart + principalDuration;

                Map<Double, Double> principalLeftovers = new LinkedHashMap<>();
                double lastEnd = Double.MAX_VALUE;
                int ornamentEntryIndex = 0;
                for(OrnamentEntry entry : group) {
                    if (ornamentEntryIndex == 0 && principalStart < entry.effectiveStart) {
                        principalLeftovers.put(principalStart, entry.effectiveStart);
                    }
                    ornamentEntryIndex++;

                    if(entry.effectiveStart <= lastEnd) {
                        lastEnd = entry.effectiveEnd;
                        continue;
                    }
                    else {
                        principalLeftovers.put(lastEnd, entry.effectiveStart);
                    }

                    lastEnd = entry.effectiveEnd;
                }
                if(lastEnd < principalEnd)
                    principalLeftovers.put(lastEnd, principalEnd);

                GenericMap map = null;
                for(GenericMap m : maps)
                    if(m.contains(principalNote.getElement()))
                        map = m;
                if(map == null)
                    continue;

                // remove principal
                map.removeElement(principalNote.getId());

                //(re-)add all "leftovers"
                int leftoverIndex = 0;
                for(Map.Entry<Double, Double> leftover : principalLeftovers.entrySet()) {
                    double leftoverDuration = leftover.getValue() - leftover.getKey();
                    if(leftoverDuration <= 1)
                        continue;

                    MsmNoteElement extendThis = null;
                    OrnamentEntry ornamEntry = null;
                    for(OrnamentEntry entry : group) {
                        ornamEntry = entry;
                        ArrayList<Element> lastChord = entry.chordSequence.get(entry.chordSequence.size()-1);
                        for(Element element : lastChord) {
                            MsmNoteElement note = new MsmNoteElement(element);
                            double noteEnd = note.getMillisecondsDate() + note.getAsDouble("ornament.milliseconds.date.offset") + note.getAsDouble("ornament.milliseconds.duration");
                            if(note.get("midi.pitch").equals(principalNote.get("midi.pitch")) && noteEnd >= leftover.getKey()) {
                                extendThis = note;
                                break;
                            }
                            // TODO: (?) search here following ornam to check if first note == principleNote -> extend/set date
                        }
                        if(extendThis != null)
                            break;
                    }
                    if(extendThis != null) {
                        extendThis.set("ornament.milliseconds.duration", String.valueOf(leftover.getValue() - extendThis.getMillisecondsDate() - extendThis.getAsDouble("ornament.milliseconds.date.offset")));
                        continue;
                    }

                    MsmNoteElement note = new MsmNoteElement(principalNote.getClonedElement());
                    note.setId(note.getId() + "_split" + leftoverIndex++);
                    note.set("milliseconds.date", leftover.getKey());
                    note.set("milliseconds.date.end", leftover.getValue());
                    if(ornamEntry != null) {
                        ornamEntry.chordSequence.add(new ArrayList<>(Arrays.asList(note.getElement()))); // add the split note the ornament entry
                    }
                    map.addElement(note.getElement());
                    addedNotes.computeIfAbsent(correspondenceId, k -> new ArrayList<>()).add(note.getId());
                }
            }
        }
        return addedNotes;
    }

    /**
     * returns the total raw length of all ornament entries of the group that is within the duration
     * @param group
     * @param duration
     * @param rawLengths will be filled
     * @param rawStarts will be filled
     * @return
     */
    private static double getTotalRawLength(ArrayList<OrnamentEntry> group, double duration, ArrayList<Double> rawLengths, ArrayList<Double> rawStarts) {
        double totalRawLength = 0.0f;
        for (OrnamentEntry entry : group) {
            double[] resolved = resolveFrameValues(entry.od, duration);
            double len = resolved[1];
            double start = resolved[0];
            rawLengths.add(len);
            rawStarts.add(start);
            double lengthUsedWithinPrincipal = len;
            if(start < 0)
                lengthUsedWithinPrincipal = Math.max(0.0, len+start);
            else if (start > 0)
                lengthUsedWithinPrincipal = Math.min(start+len, start+duration);
            totalRawLength += lengthUsedWithinPrincipal;
        }
        return totalRawLength;
    }

    /**
     * helper class to collect ornament data and chord sequences for grouped processing
     */
    private static class OrnamentEntry {
        final OrnamentData od;
        final ArrayList<ArrayList<Element>> chordSequence;
        double effectiveStart;
        double effectiveLength;
        double effectiveEnd;
        OrnamentEntry(OrnamentData od, ArrayList<ArrayList<Element>> chordSequence) {
            this.od = od;
            this.chordSequence = chordSequence;
        }

        /**
         * returns the first note of the chord sequence (date-wise), or null if none found
         * @return the first note, or null if none found
         */
        private MsmNoteElement getFirstNote () {
            Element candidate = null;

            double firstStartDate = Double.MAX_VALUE;
            for(ArrayList<Element> chord : chordSequence) {
                for(Element note : chord) {
                    double startDate = Double.parseDouble(Helper.getAttributeValue("ornament.date.offset", note)) + Double.parseDouble(Helper.getAttributeValue("ornament.duration", note));
                    if(startDate < firstStartDate) {
                        firstStartDate = startDate;
                        candidate = note;
                    }
                }
            }
            if(candidate == null)
                return null;
            return new MsmNoteElement(candidate);
        }

        /**
         * returns the last note of the chord sequence (date-wise), or null if none found
         * @return the last note, or null if none found
         */
        private MsmNoteElement getLastNote() {
            Element candidate = null;

            double latestEndDate = -Double.MAX_VALUE;
            for(ArrayList<Element> chord : chordSequence) {
                for(Element note : chord) {
                    double endDate = Double.parseDouble(Helper.getAttributeValue("ornament.milliseconds.date.offset", note)) + Double.parseDouble(Helper.getAttributeValue("ornament.milliseconds.duration", note));
                    if(endDate > latestEndDate) {
                        latestEndDate = endDate;
                        candidate = note;
                    }
                }
            }
            if(candidate == null)
                return null;
            return new MsmNoteElement(candidate);
        }
    }

    /**
     * get the principal note's duration from the first note in the chord sequence
     * @param chordSequence
     * @return duration in ticks, or 0.0 if not found
     */
    private static double getPrincipalDuration(ArrayList<ArrayList<Element>> chordSequence) {
        for (ArrayList<Element> chord : chordSequence) {
            for (Element note : chord) {
                Attribute durAtt = Helper.getAttribute("duration", note);
                if (durAtt != null)
                    return Double.parseDouble(durAtt.getValue());
            }
        }
        return 0.0;
    }

    /**
     * check whether the ornament is anchored at the end of the principal note,
     * as defined by its OrnamentDef's TemporalSpread alignment
     * @param od the ornament data
     * @return true if alignment is "atEnd"
     */
    private static boolean isAtEnd(OrnamentData od) {
        return od.ornamentDef != null
                && od.ornamentDef.getTemporalSpread() != null
                && od.ornamentDef.getTemporalSpread().isAtEnd();
    }

    /**
     * resolve the raw frameStart and frameLength values of an ornament's TemporalSpread to ticks.
     * Returns [frameStart, frameLength] in ticks.
     * @param od
     * @param principalDuration
     * @return double[2] with [frameStart, frameLength]
     */
    private static double[] resolveFrameValues(OrnamentData od, double principalDuration) {
        double start = 0.0;
        double length = 0.0;

        if (od.ornamentDef != null && od.ornamentDef.getTemporalSpread() != null) {
            OrnamentDef.TemporalSpread ts = od.ornamentDef.getTemporalSpread();
            start = ts.frameStart.getValue();
            length = ts.frameLength.getValue();

            if (ts.frameStart.isRelative())
                start = (start * 0.01) * principalDuration;
            if (ts.frameLength.isRelative())
                length = (length * 0.01) * principalDuration;
        }

        return new double[]{ start, length };
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
                            durationPerfAtt.setValue(String.valueOf(Double.parseDouble(durationPerfAtt.getValue()) - ornamentDateOffset));
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

        ArrayList<GenericMap> maps = new ArrayList<GenericMap>();
        maps.add(map);

        ArrayList<OrnamentEntry> ornamentEntries = ornamentationMap.apply(maps);

        ornamentationMap.spaceOrnaments(maps, ornamentEntries);

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
                } // else, ornament.noteOff.shift="false", so milliseconds.date.end remains unaltered
            }
        }
    }

    public static void sanitizeOverlaps(GenericMap map) {
        // sanitize overlapping notes with same midi.pitch

        // get all notes (as we have generated new ones for the ornaments)
        HashMap<String, MsmNoteElement> latestMidiPitch = new HashMap<String, MsmNoteElement>();

        for (KeyValue<Double, Element> note : map.getAllElementsOfType("note")) {
            MsmNoteElement msmNote = new MsmNoteElement(note.getValue());
            String midiPitch = msmNote.get("midi.pitch");
            if(latestMidiPitch.containsKey(midiPitch)) {
                MsmNoteElement latestNote = latestMidiPitch.get(midiPitch);
                double endsAt = latestNote.getAsDouble("date.perf") + latestNote.getAsDouble("duration.perf");
                if (endsAt > msmNote.getAsDouble("date.perf")) {
                    double duration = latestNote.getAsDouble("duration.perf") - (endsAt - msmNote.getAsDouble("date.perf"));
                    if (duration <= 0.0)
                        map.removeElement(latestNote.getId());
                    else
                        latestNote.set("duration.perf", duration);
                }
            }
            latestMidiPitch.put(midiPitch, msmNote);
        }
    }
}
