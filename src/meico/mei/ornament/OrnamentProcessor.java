package meico.mei.ornament;

import meico.mei.ConversionContext;
import meico.mei.Helper;
import meico.mei.MeiElementHelper;
import meico.mei.NoteProcessor;
import meico.mpm.Mpm;
import meico.mpm.elements.Part;
import meico.mpm.elements.maps.OrnamentationMap;
import meico.mpm.elements.maps.data.OrnamentData;
import meico.mpm.elements.styles.OrnamentationStyle;
import meico.mpm.elements.styles.defs.OrnamentDef;
import meico.msm.elements.MsmNoteElement;
import meico.supplementary.KeyValue;
import meico.xml.RichElement;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This class encapsulates the processing needed to convert ornaments from MEI to MSM/MPM.
 * As Processor, it is used by the Mei2MsmMpmConverter, that delegates elements like trill, turn, mordent, ornam, arpeg, fTrem, bTrem.
 */
public class OrnamentProcessor {
    protected ArrayList<KeyValue<Attribute, Boolean>> arpeggiosToSort = new ArrayList<>();  // for some arpeggios the note.order attribute must be sorted to get an up (true) or downwards (false) direction; this is done during postprocessing of mdiv elements when we know the notes' pitch values (also available via getAllNotesAndChords(), attribute pnum); this list holds all attributes note.order to be reordered and the corresponding direction (true=up, false=down)
    private final ConversionContext context;
    private final NoteProcessor noteProcessor;

    /**
     * The OrnamentProcessor does need the current Context (esp. information of current accidentals and notes within a measure)
     * and the noteProcessor, as chords can occur within e.g. a fTrem
     * @param context
     * @param noteProcessor
     */
    public OrnamentProcessor(ConversionContext context, NoteProcessor noteProcessor) {
        this.context = context;
        this.noteProcessor = noteProcessor;
    }

    /**
     * process an mei arpeg element
     * @param element
     */
    public void processArpeg(MeiElementHelper element) {
        // check if this is really an arpeggio
        String order = element.get("order");                                  // get order attribute
        if (order == null)
            return;

        order = order.trim();
        if((order == null) || order.equals("nonarp"))                       // if no arpeggio
            return;                                                         // cancel

        // compute the timing or get the necessary data to compute the end date later on
        ArrayList<Object> timingData = this.context.computeControlEventTiming(element.getElement(), this.context.getCurrentPart());
        if (timingData == null)                                             // if the event has been repositioned in accordance to a startid attribute
            return;                                                         // stop processing it right now

        // create ornament data
        OrnamentData od = new OrnamentData();
        od.date = (Double) timingData.get(0);
        od.ornamentDefName = "arpeggio";
        od.scale = 0.0;

        // read the xml:id
        String id = element.getId();
        if (id != null)
            od.xmlId = id;
        else
            od.xmlId = "meico_" + UUID.randomUUID().toString();
        od.correspondence = (id == null) ? null : id;

        // determine the note order
        int needsPostprocessing = 0;                                        // this will be set 1, if the note.order must be reordered with ascending pitch, and -1 for descending pitch
        String plist = element.get("plist");
        if (plist == null) {                                                // if we have no plist that specifies the note sequence
            if (order != null) {                                            // if we have an order attribute (otherwise we leave the note.order attribute away which is equal to "ascending pitch")
                od.noteOrder = new ArrayList<>();
                if (order.equals("down"))                            // if it is specified down
                    od.noteOrder.add("descending pitch");                   // set the note.order attribute
                else                                                        // in any other case (order ="up" or any unknown value)
                    od.noteOrder.add("ascending pitch");                    // set note.order="ascending pitch"
            }
        } else {                                                            // if we have a plist
            od.noteOrder = new ArrayList<>();
            for (String ref : plist.trim().split("\\s+")) {   // collect the references (sorting will come later)
                MeiElementHelper e = new MeiElementHelper(this.context.getAllNotesAndChords().get(ref.replace("#", "")));    // get the MEI element behind the reference
                if (e == null)                                              // if it is neither a note nore a chord
                    continue;                                               // ignore it
                if (e.getName().equals("note")) {                      // if it is a note
                    od.noteOrder.add(ref);                                  // add its reference to the note order list
                    continue;
                }
                if (e.getName().equals("chord")) {                     // if it is a chord, we retrieve its notes and add them to the note order list in the sequence they are defined in the chord

                    for (MeiElementHelper note : e.getChildrenAsMeiElements("note")) {  // get all note elements in the chord
                        String noteId = note.getId(); // get the note's id
                        if (noteId == null) {
                            note.setId("meico_" + UUID.randomUUID().toString());// if the note has no id, generate one
                        }
                        od.noteOrder.add("#" + noteId);          // add the id to the note order list
                    }
                }
            }

            // the sequence of the notes must be reordered to ensure that it matches with @order="up/down"; this will be done at the end of the mdiv conversion when all notes are converted and have a proper @pnum/@midi.pitch for each note
            if (order != null) {                                            // seems like a specific order is desired
                if (order.equals("down"))                 // if it should be with descending pitch
                    needsPostprocessing = -1;                               // set the indication - will be processed later
                else if (order.equals("up"))              // if ascending pitch
                    needsPostprocessing = 1;                                // set the indication - will be processed later
            }
        }

        // make sure that the arpeggio is defined in a global ornamentation style of name "MEI export"
        OrnamentationStyle ornamentationStyle = (OrnamentationStyle) this.context.getCurrentPerformance().getGlobal().getHeader().getStyleDef(Mpm.ORNAMENTATION_STYLE, "MEI export"); // get the global ornamentationSyles/styleDef element
        if (ornamentationStyle == null)                                                                                                                                         // if there is none
            ornamentationStyle = (OrnamentationStyle) this.context.getCurrentPerformance().getGlobal().getHeader().addStyleDef(Mpm.ORNAMENTATION_STYLE, "MEI export");                // create one
        if (ornamentationStyle.getDef(od.ornamentDefName) == null)
            ornamentationStyle.addDef(OrnamentDef.createDefaultOrnamentDef(od.ornamentDefName));

        // parse the staff attribute (space separated staff numbers)
        OrnamentationMap ornamentationMap;
        String attrVal = element.get("part");                                                                         // get the part attribute (MEI 4.0, https://github.com/music-encoding/music-encoding/issues/435)
        if (attrVal == null)                                                                                                    // if no part attribute
            attrVal = element.get("staff");                                                                              // find the staffs that this is associated to
        if ((attrVal == null) || attrVal.isEmpty() || attrVal.equals("%all")) {                                   // if no part or staff association is defined treat it as a global instruction
            ornamentationMap = (OrnamentationMap) this.context.getCurrentPerformance().getGlobal().getDated().getMap(Mpm.ORNAMENTATION_MAP);      // get the global ornamentationMap
            if (ornamentationMap == null) {                                                                                                 // if there is no global ornamentationMap
                ornamentationMap = (OrnamentationMap) this.context.getCurrentPerformance().getGlobal().getDated().addMap(Mpm.ORNAMENTATION_MAP);  // create one
                ornamentationMap.addStyleSwitch(0.0, "MEI export");                                                                         // set its start style reference
            }
            int index = ornamentationMap.addOrnament(od);                                           // add it to the map
            if (needsPostprocessing != 0)
                this.arpeggiosToSort.add(new KeyValue<>(Helper.getAttribute("note.order", ornamentationMap.getElement(index)), needsPostprocessing > 0));    // store the note.order attribute and arpeggio direction for reordering during postprocessing
        } else {                                                                                    // there are staffs, hence, local ornament instruction
            String staffString = attrVal;
            String[] staffs = staffString.split("\\s+");                                         // this creates an array of one or more integer strings (the staff numbers), they are separated by one or more whitespaces

            for (String staff : staffs) {                                                           // go through all the part numbers
                Part part = this.context.getCurrentPerformance().getPart(Integer.parseInt(staff));        // find that part in the performance data structure
                if (part == null)                                                                   // if not found
                    continue;                                                                       // continue with the next

                ornamentationMap = (OrnamentationMap) part.getDated().getMap(Mpm.ORNAMENTATION_MAP);// get the part's ornamentationMap
                if (ornamentationMap == null) {                                                     // if it has none so far
                    ornamentationMap = (OrnamentationMap) part.getDated().addMap(Mpm.ORNAMENTATION_MAP);    // create it
                    ornamentationMap.addStyleSwitch(0.0, "MEI export");              // set the style reference
                }

                OrnamentData odd = od.clone();

                int index = ornamentationMap.addOrnament(odd);                                      // add it to the map
                if (needsPostprocessing != 0)
                    this.arpeggiosToSort.add(new KeyValue<>(Helper.getAttribute("note.order", ornamentationMap.getElement(index)), needsPostprocessing > 0));    // store the note.order attribute and arpeggio direction for reordering during postprocessing
            }
        }
    }

    /**
     * Postprocesses arpeggios, namely reorder the note.order attribute now that we have a proper pitch value for each note
     */
    public void postProcessArpeg() {
        for(KeyValue<Attribute, Boolean> arpeggioNoteOrder :this.arpeggiosToSort)
        {                // for each note.order attribute to be reordered
            ArrayList<KeyValue<String, Double>> notePitchList = new ArrayList<>();
            for (String noteId : arpeggioNoteOrder.getKey().getValue().replaceAll("#", "").split("\\s+")) { // deserialize the note.order string to a list of note IDs
                Element note = this.context.getAllNotesAndChords().get(noteId);
                if (note == null)
                    continue;

                Attribute pitchAtt = Helper.getAttribute("pnum", note);
                if (pitchAtt == null)
                    continue;

                double pitch = Double.parseDouble(pitchAtt.getValue());
                notePitchList.add(new KeyValue<>(noteId, pitch));
            }

            // sort the notes according to the indicated order
            notePitchList.sort((n1, n2) -> (int) ((arpeggioNoteOrder.getValue()) ? Math.signum(n1.getValue() - n2.getValue()) : Math.signum(n2.getValue() - n1.getValue())));

            // concatenate the note IDs in a string and set new attribute value for note.order
            String noteIdsString = "";
            for (KeyValue<String, Double> noteId : notePitchList)
                noteIdsString = noteIdsString.concat(" #" + noteId.getKey().trim().replace("#", ""));
            arpeggioNoteOrder.getKey().setValue(noteIdsString.trim());
        }
    }

    /**
     * Process MEI tremolo elements and expand them into ornament data.
     * @param element
     */
    public void processTrem(MeiElementHelper element) {
        ArrayList<Object> timingData = this.context.computeControlEventTiming(element.getElement(), this.context.getCurrentPart());
        if (timingData == null)                                             // if the event has been repositioned in accordance to a startid attribute
            return;

        ArrayList<MeiElementHelper> notes = new ArrayList<>();
        ArrayList<MeiElementHelper> chords = new ArrayList<>();


        // collect all notes, plus collect them as chords while preserving the original chords
        for(MeiElementHelper elem : element.getChildrenAsMeiElements()) {
            if(elem.getName().equals("note")) {
                notes.add(elem);
                MeiElementHelper chord = new MeiElementHelper("chord");
                chord.appendChild(new MeiElementHelper(elem.getElement(), true));
                chords.add(chord);
            }
            else if(elem.getName().equals("chord")) {
                ArrayList<MeiElementHelper> chordNotes = elem.getChildrenAsMeiElements("note");
                if(chordNotes.isEmpty())
                    continue;
                notes.addAll(chordNotes);
                MeiElementHelper chord = new MeiElementHelper("chord");
                for (MeiElementHelper chordNote : chordNotes) {
                    chord.appendChild(new MeiElementHelper(chordNote.getElement(), true));
                }
                chords.add(chord);
            }
        }

        if(notes.isEmpty())
            return;

        // processChords to get the 'normalized' attributes, as all chords are treated
        MeiElementHelper allNotesChord = new MeiElementHelper("chord");
        notes.forEach(allNotesChord::appendChild);
        this.noteProcessor.processChord(allNotesChord.getElement());

        // tremolandi attributes: https://music-encoding.org/guidelines/v5/content/cmn.html#cmnTrem
        String unitdurAttr  = element.get("unitdur");
        String numAttr      = element.get("num");
        String stemModAttr  = element.get("stem.mod");

        // MEI exposes the tremolo rate via different attributes depending on the notation style
        int repetitions = -1;
        if (unitdurAttr != null) {
            int unitdur = Integer.parseInt(unitdurAttr);
            int dur = Integer.parseInt(notes.get(0).get("dur"));
            repetitions = unitdur / dur - 1; // "- 1" as the note is played once regulary + "repetitions"
        }
        else if (numAttr != null) {
            repetitions = Integer.parseInt(numAttr) - 1; // "- 1" as the note is played once regulary + "repetitions"
        }
        else if (stemModAttr != null) {
            int stemMod = Integer.parseInt(stemModAttr);
            int dur = Integer.parseInt(notes.get(0).get("dur"));
            repetitions = stemMod / dur - 1; // "- 1" as the note is played once regulary + "repetitions"
        }

        // create ornament data
        OrnamentData od = new OrnamentData();
        od.xmlId = element.getId();
        od.correspondence = notes.get(0).getId();
        od.date = (Double) timingData.get(0);
        od.ornamentDefName = "tremolo";
        od.scale = 0.0;
        od.notes = new ArrayList<>();
        od.noteOrder = new ArrayList<String>();
        od.repetitions = repetitions;

        // encode the (alternating) tremolo pattern into the noteOrder sequence
        od.noteOrder.add("|:");
        for (MeiElementHelper chord : chords) {
            od.noteOrder.add("[");
            for(MeiElementHelper note : chord.getChildrenAsMeiElements("note")) {
                MsmNoteElement msmNote = context.meiNote2MsmNote(new MeiElementHelper(note.getElement()));
                if (msmNote != null) {
                    msmNote.set("interval.chromatic", 0.0);
                    msmNote.remove("pitchname");
                    msmNote.remove("accidentals");
                    msmNote.remove("octave");
                    od.notes.add(msmNote.getElement());
                }

                od.noteOrder.add("#" + note.getId());
            }
            od.noteOrder.add("]");
        }
        od.noteOrder.add(":|");

        addToOrnamentationMap(notes.get(0), od);
    }

    /**
     * process ornaments like trills, turns, graceGrps, .. that are provided by a (generated) supplied into ornamentData
     * @param element
     * @return true if element was an ornament that has been processed, false if element was not a proper ornament
     */
    public boolean processOrnament(MeiElementHelper element) {
        if(!checkIfOrnament(element))
            return false;

        ArrayList<Object> timingData = this.context.computeControlEventTiming(element.getElement(), this.context.getCurrentPart());
        if (timingData == null)                                                     // if the event has been repositioned in accordance to a startid attribute
            return false;                                                           // stop processing it right now

        String ornamentName = "ornam";
        String elementId = element.getId();
        ArrayList<String> segmentLabels = new ArrayList<>();

        if(element.has("label"))
            ornamentName = element.get("label");

        ArrayList<MeiElementHelper> children = element.getChildrenAsMeiElements();
        ArrayList<MeiElementHelper> graceGrps = new ArrayList<>(children); // by definition each child is a graceGrp

        // get the principal
        elementId = element.get("corresp").replace("#", "").trim();
        Element principal = this.context.getAllNotesAndChords().get(elementId);
        timingData.set(0, Double.parseDouble(principal.getAttributeValue("date")));

        // preserve per-segment labels so combined expansions can be split again
        for (MeiElementHelper child : children) {
            String label = child.get("label");
            if (label != null && !label.isEmpty())
                segmentLabels.add(label);
        }

        int segmentCount = graceGrps.size();

        // create one ornamentData per ornament (graceGrp)
        for (int s = 0; s < segmentCount; s++) {
            MeiElementHelper graceGrp = graceGrps.get(s);
            String correspId = graceGrp.get("corresp");

            OrnamentData od = new OrnamentData();
            od.xmlId = correspId != null ? correspId : UUID.randomUUID().toString();
            od.correspondence = elementId;
            od.date = (Double) timingData.get(0);
            od.ornamentDefName = (segmentLabels.size() > s) ? segmentLabels.get(s) : ornamentName;
            od.scale = 0.0;
            od.notes = new ArrayList<>();
            od.noteOrder = new ArrayList<>();

            for (MeiElementHelper elem : graceGrp.getChildrenAsMeiElements()) {
                addMeiNoteToOrnamentData(elem, od);
            }

            addToOrnamentationMap(new MeiElementHelper(principal), od);
        }
        return true;
    }

    /**
     * check if the element is a supplied generated by meico which is a preprocessed ornament
     * @param element
     * @return
     */
    protected boolean checkIfOrnament(MeiElementHelper element) {
        return      element.getName().equals("supplied")
                &&  element.has("reason")
                &&  element.get("reason").startsWith("ornament expansion")
                &&  element.has("corresp");
    }

    /**
     * adds elem to data by converting the MEI note to MPM note and updating note.order
     * @param element MEI note
     * @param data
     */
    private void addMeiNoteToOrnamentData(MeiElementHelper element, OrnamentData data) {
        if (element.getName().equals("barLine")) {
            data.noteOrder.add(getRptString(element));
            data.repetitions = -1; // if we have a barline, we got a repetitive moment, that is going to be guessed ("-1") while "perform"
            return;
        }

        MsmNoteElement msmNote = context.meiNote2MsmNote(new MeiElementHelper(element.getElement()));
        if (msmNote != null) {
            if(element.has("intm")) {
                String intm = element.get("intm");
                intm = intm.replaceAll("hs", "").trim();
                msmNote.set("interval.chromatic", intm);
            }
            msmNote.remove("pitchname");
            msmNote.remove("accidentals");
            msmNote.remove("octave");
            data.notes.add(msmNote.getElement());
        }

        data.noteOrder.add("#" + element.getId());
    }

    /**
     * helper method to get the ornament noteorder/dictionary string representation of a MEI barline repeat sign
     * @param element
     * @return
     */
    private String getRptString(MeiElementHelper element) {
        String rptStr = "";
        switch(element.get("form")) {
            case "rptstart":
                rptStr = "|:";
                break;
            case "rptboth":
                rptStr = ":|:";

                break;
            case "rptend":
                rptStr = ":|";
                break;
        }
        return rptStr;
    }


    /**
     * helper method to add the ornamentation data to the correct ornamentationMap(s) in MPM
     * and to make sure that the corresponding styleDef is defined in MPM
     * @param element
     * @param data
     */
    private void addToOrnamentationMap(MeiElementHelper element, OrnamentData data) {                   // TODO: use in processArpeg
        // make sure that the ornamentationStyle is defined in a global ornamentation style of name "MEI export"
        OrnamentationStyle ornamentationStyle = (OrnamentationStyle) this.context.getCurrentPerformance()
                .getGlobal().getHeader().getStyleDef(Mpm.ORNAMENTATION_STYLE, "MEI export");      // get the global ornamentationSyles/styleDef element
        if (ornamentationStyle == null)                                                                                                                         // if there is none
            ornamentationStyle = (OrnamentationStyle) this.context.getCurrentPerformance()
                    .getGlobal().getHeader().addStyleDef(Mpm.ORNAMENTATION_STYLE, "MEI export");  // create one
        if (ornamentationStyle.getDef(data.ornamentDefName) == null)
            ornamentationStyle.addDef(OrnamentDef.createDefaultOrnamentDef(data.ornamentDefName));

        // parse the staff attribute (space separated staff numbers)
        OrnamentationMap ornamentationMap;
        String attrVal = element.get("part");                                                           // get the part attribute (MEI 4.0, https://github.com/music-encoding/music-encoding/issues/435)

        if (attrVal == null)                                                                            // if no part attribute
            attrVal = element.get("staff");                                                             // find the staffs that this is associated to

        String elName = element.getName();
        if(attrVal == null && (elName.equals("supplied")
                            || elName.equals("graceGrp")
                            || elName.equals("note")
                            || elName.equals("chord")
                            || elName.equals("fTrem")
                            || elName.equals("bTrem"))) {
            RichElement parent = element;
            do {                                                                                        // search staff and get its "n"
                parent = parent.getParent();
                if(parent != null && (parent.getName().equals("staff") || parent.getName().equals("part")))
                    attrVal = parent.get("n");
            } while (attrVal == null && parent != null && !parent.getName().equals("part"));
        }

        if ((attrVal == null) || attrVal.isEmpty() || attrVal.equals("%all")) {                         // if no part or staff association is defined treat it as a global instruction
            ornamentationMap = (OrnamentationMap) this.context.getCurrentPerformance().getGlobal().getDated().getMap(Mpm.ORNAMENTATION_MAP);      // get the global ornamentationMap
            if (ornamentationMap == null) {                                                             // if there is no global ornamentationMap
                ornamentationMap = (OrnamentationMap) this.context.getCurrentPerformance().getGlobal().getDated().addMap(Mpm.ORNAMENTATION_MAP);  // create one
                ornamentationMap.addStyleSwitch(0.0, "MEI export");                      // set its start style reference
            }
            int index = ornamentationMap.addOrnament(data);                                             // add it to the map
        }
        else {                                                                                          // there are staffs, hence, local ornament instruction
            boolean multiIDs = false;
            String staffString = attrVal;
            String[] staffs = staffString.split("\\s+");                                             // this creates an array of one or more integer strings (the staff numbers), they are separated by one or more whitespaces

            for (String staff : staffs) {                                                               // go through all the part numbers
                Part part = this.context.getCurrentPerformance().getPart(Integer.parseInt(staff));      // find that part in the performance data structure
                if (part == null)                                                                       // if not found
                    continue;                                                                           // continue with the next

                ornamentationMap = (OrnamentationMap) part.getDated().getMap(Mpm.ORNAMENTATION_MAP);    // get the part's ornamentationMap
                if (ornamentationMap == null) {                                                         // if it has none so far
                    ornamentationMap = (OrnamentationMap) part.getDated().addMap(Mpm.ORNAMENTATION_MAP);// create it
                    ornamentationMap.addStyleSwitch(0.0, "MEI export");                  // set the style reference
                }

                OrnamentData odd = data.clone();
                if ((data.xmlId != null) && multiIDs)
                    odd.xmlId = data.xmlId + "_meico_" + UUID.randomUUID().toString();

                int index = ornamentationMap.addOrnament(odd);                                          // add it to the map

                multiIDs = true;
            }
        }
    }
}
