package meico.mei.ornament;

import meico.mei.Helper;
import meico.mei.Mei;
import meico.mei.MeiElementHelper;
import meico.supplementary.Stopwatch;
import nu.xom.Element;
import nu.xom.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class adds ornamentExpansions for ornaments, by creating notes to be played within a <supplied></supplied> after the principal note.
 * This is mainly for rendering via MPM.
 * These ornamentExpansions are inserted into the given MEI (the original MEI file stays untouched).
 * @author Lars Engeln
 */
public class OrnamentExpander {
    public Mei mei;
    private Map<String, OrnamentExpansion> ornamentExpansions   = new HashMap<String, OrnamentExpansion>(); // ornament's startid to ornamentExpansion
    private OrnamentDictionary ornamentDictionary               = new OrnamentDictionary();

    private ArrayList<String> prevOrnams                        = new ArrayList<String>(); // already expanded ornams with that are "previous" to another ornam
    private Map<String, Element> nextOrnams                     = new HashMap<String, Element>(); // prevId to nextElement - remember "next" ornament to be processed, if "prev"-Id have not been processed yet - so, if "prev"/"next" is not well sorted in the MEI

    private Map<String, Map<String, String>> currentAccids      = new HashMap<>(); // all accids in the current measure, "oct"->"pname"->"accid"
    private Map<String, String> currentKey                      = new HashMap<>(); // accids of the current key, "pname"->"accid"
    private MeiElementHelper currentMeasure                     = null;
    private ArrayList<MeiElementHelper> currentSlurs            = new ArrayList<>();   // cached slurs in the current measure
    private ArrayList<MeiElementHelper> currentGraces           = new ArrayList<>();   // cached graces in the current measure, to be added at the end of the measure
    private Map<String, MeiElementHelper> currentNotes          = new HashMap<>();   // cached notes in the current measure, to be used for grace note expansion

    /**
     * default constructor, if MEI is not yet available
     */
    public OrnamentExpander()  {
    }

    /**
     * adds expanded readings for ornaments
     * @param mei the MEI to be expanded
     * @return the input MEI with the added ornament expansions
     */
    public Mei expandOrnaments(Mei mei) {
        if (mei == null) {
            System.out.println("\nThe provided MEI object is null and cannot be expanded.");
            return null;
        }

        UUID.randomUUID(); // initalizes the random generator, so that it does not cause a delay during the expansion process

        System.out.println("\nInstructifying " + ((mei.getFile() != null) ? mei.getFile().getName() : "MEI data") + ".");
        Stopwatch stopwatch = new Stopwatch();

        this.mei = mei;

        // if no mei music data available
        if (        this.mei.isEmpty()
                || (this.mei.getMusic() == null)
                || (this.mei.getMusic().getFirstChildElement("body", this.mei.getMusic().getNamespaceURI()) == null))
            return mei;

        // get the list of body elements in the mei source
        Elements bodies = this.mei.getMusic().getChildElements("body", this.mei.getMusic().getNamespaceURI());
        for (int b = 0; b < bodies.size(); ++b)         // for each body
            this.expandOrnaments(bodies.get(b));        // expand each body`s ornaments

        for(Element element : nextOrnams.values()){     // do not forget someone, should never happen if MEI is well-defined
            this.expandOrnamentsElement(element);
        }
        for(MeiElementHelper grace : currentGraces) {     // do not forget someone, expand graces of last measure
            expandGrace(grace);
        }

        stopwatch.markTotal("MEI expansion of Ornaments finished.");

        return mei;
    }

    /**
     * iterates the MEI XML tree to identify supported elements to get expanded
     * @param root
     */
    private void expandOrnaments(Element root) {
        Elements es = root.getChildElements();                                  // all child elements of root

        for (int i = 0; i < es.size(); ++i) {                                   // traverse the mei tree
            Element e = es.get(i);

            // process the element
            switch (e.getLocalName()) {
                case "turn":
                case "trill":
                case "mordent":
                case "ornam":
                    expandOrnamentsElement(e);                                  // collect ornaments
                    continue;
                case "graceGrp":                                                // collect graces, fiorituras
                    MeiElementHelper graceGrp = new MeiElementHelper(e, false);
                    currentGraces.add(graceGrp);
                    break;
                case "note":
                    MeiElementHelper note = new MeiElementHelper(e, false);
                    if(note.has("grace"))
                        currentGraces.add(note);                                // collect graces
                    else
                        currentNotes.put(note.getId(), note);                   // if not a grace, collect notes
                    String accid = note.get("accid");                           // collect accidentals
                    if(accid == null || accid.isEmpty())
                        break;
                    if(!currentAccids.containsKey(note.get("oct")))
                        currentAccids.put(note.get("oct"), new HashMap<>());
                    currentAccids.get(note.get("oct")).put(note.get("pname"), accid);
                    continue;
                case "chord":
                    MeiElementHelper chord = new MeiElementHelper(e, false);
                    if(chord.has("grace"))
                        currentGraces.add(chord);                               // collect graces
                    else
                        currentNotes.put(chord.getId(), chord);                 // if not a grace, collect chords
                    break;                                                      // process children of chord
                case "accid":                                                   // is been processed via "note"
                    break;
                case "keySig":                                                  // switch to new key signature
                    currentKey = new HashMap<>();
                    break;
                case "keyAccid":                                                // collect all key accidentals
                    MeiElementHelper keyAccid = new MeiElementHelper(e);
                    currentKey.put(keyAccid.get("pname"), keyAccid.get("accid"));
                    continue;
                case "measure":                                                 // new measure begins, so old one has ended
                    for(MeiElementHelper grace : currentGraces) {                 // if we have graces, expand them before we process the next measure (as we needed to collect slurs before)
                        expandGrace(grace);
                    }
                    currentMeasure  = new MeiElementHelper(e, false);
                    currentSlurs    = new ArrayList<>();
                    currentGraces   = new ArrayList<>();
                    currentNotes    = new HashMap<>();
                    currentAccids   = new HashMap<>();
                    break;
                case "slur":                                                    // need to collect slurs to check to which note a grace might be attached
                    MeiElementHelper slur = new MeiElementHelper(e, false);
                    currentSlurs.add(slur);
                    continue;
            }

            this.expandOrnaments(e);    // if breaked, process children
        }

    }

    /**
     * searches for the element with the given id in the list of elements
     * @param elements
     * @param id
     * @return the element with the given id, or null if no such element is found
     */
    private MeiElementHelper getElementWithId(ArrayList<MeiElementHelper> elements, String id) {
        for(MeiElementHelper element : elements) {
            if(element.getId() != null && element.getId().equals(id))
                return element;
        }
        return null;
    }

    /**
     * searches for the corresponding principal note of a grace note, by searching for slurs and surrounding notes.
     * If no slur is found, the grace note is before the principal note if there is a following note,
     * and after if there is a previous note. If both are found, the grace note is places according to "unacc"/"acc"
     * @param element
     * @param graceIsBefore
     * @return true if grace is before corresponding, false if is after
     */
    private MeiElementHelper getCorrespondingNoteOfGrace(MeiElementHelper element, Map<String, MeiElementHelper> graceNotes, AtomicBoolean graceIsBefore) {
        MeiElementHelper principalNote = null;

        if(this.currentMeasure == null)
            return null;

        ArrayList<MeiElementHelper> slurs = this.currentSlurs;

        // check if grace note is slurred to/from a note
        for(MeiElementHelper slur : slurs) {
            MeiElementHelper note = graceNotes.get(slur.get("startid"));
            if (note != null) {                             // if a slur start from a grace
                String endid = slur.get("endid");
                Element principalNoteElement = Helper.getFirstDescendantById(Helper.getParentElement(element.getElement()), endid);
                if (principalNoteElement != null) {         // and if that slur ends on a note
                    principalNote = new MeiElementHelper(principalNoteElement);
                    graceIsBefore.set(true);                // the grace is before its principal
                    return principalNote;                   // and the principal has been found
                }
            }                                               // if the principal was not found:
            note = graceNotes.get(slur.get("endid"));
            if (note != null) {                             // if a slur ends on a grace
                String startid = slur.get("startid");
                Element principalNoteElement = Helper.getFirstDescendantById(Helper.getParentElement(element.getElement()), startid);
                if (principalNoteElement != null) {         // and if that slur started from a note
                    principalNote = new MeiElementHelper(principalNoteElement);
                    graceIsBefore.set(false);               // grace is after its principal
                    return principalNote;                   // and the principal has been found
                }
            }
        }

        // No slur found: walk siblings until possible correspondence (e.g. a note or chord) is found (or null)
        Element previousElement = Helper.getPreviousSiblingElement(element.getElement());
        while (previousElement != null
                && !previousElement.getLocalName().equals("note")
                && !previousElement.getLocalName().equals("chord")
                && !previousElement.getLocalName().equals("beam")
                && !previousElement.getLocalName().equals("rest")
                && !previousElement.getLocalName().equals("space")
        )
            previousElement = Helper.getPreviousSiblingElement(previousElement);

        Element nextElement = Helper.getNextSiblingElement(element.getElement());
        while (nextElement != null
                && !nextElement.getLocalName().equals("note")
                && !nextElement.getLocalName().equals("chord")
                && !nextElement.getLocalName().equals("beam")
                && !nextElement.getLocalName().equals("rest")
                && !nextElement.getLocalName().equals("space")
        )
            nextElement = Helper.getNextSiblingElement(nextElement);

        //TODO: resolve note from chord/beam ?

        // now check all possibilities
        if (nextElement == null && previousElement == null) {
            graceIsBefore.set(true);
            return null;            // no principal found, as no other elements in this measure
        }
        if (nextElement != null && previousElement == null) {
            principalNote = new MeiElementHelper(nextElement);
            graceIsBefore.set(true);
            return principalNote;
        }
        if (previousElement != null && nextElement == null) {
            principalNote = new MeiElementHelper(previousElement);
            graceIsBefore.set(false);
            return principalNote;
        }

        // if both != null
        // prioritise a note over a rest, and a rest over a space
        if (nextElement.getLocalName().equals("rest") && previousElement.getLocalName().equals("rest")) {
            graceIsBefore.set(true);
            return null;
        }
        if (!nextElement.getLocalName().equals("rest") && previousElement.getLocalName().equals("rest")) {
            principalNote = new MeiElementHelper(nextElement);
            graceIsBefore.set(true);
            return principalNote;
        }
        if (!previousElement.getLocalName().equals("rest") && nextElement.getLocalName().equals("rest")) {
            principalNote = new MeiElementHelper(previousElement);
            graceIsBefore.set(false);
            return principalNote;
        }

        // if we were left with two surrounding notes, check the "grace" attribute
        String graceType = element.getAttributeValue("grace");
        if(graceType == null)
            graceType = "";

        if(graceType.equals("acc")) {
            principalNote = new MeiElementHelper(nextElement);
            graceIsBefore.set(true);
            return principalNote;
        }
        if(graceType.equals("unacc")) {
            principalNote = new MeiElementHelper(previousElement);
            graceIsBefore.set(false);
            return principalNote;
        }

        // if the "grace" attribute is not provided, prioritise a grace before a note over a grace after a note
        principalNote = new MeiElementHelper(nextElement);
        graceIsBefore.set(true);
        return principalNote;
    }

    /**
     * expands a grace note or a graceGrp to an OrnamentExpansion,
     * by searching for the corresponding principal note.
     * The OrnamentExpansion is inserted into the given MEI (the original MEI file stays untouched).
     * @param element
     */
    private void expandGrace(MeiElementHelper element) {
        // collect the grace notes
        Map<String, MeiElementHelper> notes = this.collectAllNotes(element);
        AtomicBoolean graceIsBefore = new AtomicBoolean(true);

        // get principal note and remember whether the grace is before it
        MeiElementHelper principalNote = getCorrespondingNoteOfGrace(element, notes, graceIsBefore);
        if(principalNote == null)
            return;

        String graceType = element.get("grace");
        if(graceType == null || !graceType.equals("unacc"))
            graceType = "acc";

        // build the ornament label from grace type and placement
        String ornamentName = "grace " + graceType;
        if(!graceIsBefore.get()) {
            ornamentName = ornamentName + " delayed";
        }
        // use fioritura when the principal target is a space
        if(principalNote.getElement().getLocalName().equals("space")) {
            ornamentName = "fioritura";
        }

        OrnamentExpansion ornamentExpansion = new OrnamentExpansion();
        // link the generated expansion to the original grace element
        ornamentExpansion.addCorrespondence(principalNote);
        ornamentExpansion.getGroupElement().set("corresp", element.getId());
        ornamentExpansion.setLabel(ornamentName);

        boolean principalIsNote = principalNote.getName().equals("note");
        String dur = notes.values().iterator().next().get("dur");

        // use the principal note for interval mapping, or the last note when the target is a chord
        MeiElementHelper halfStepsTo = principalNote;
        if(!principalIsNote) {
           halfStepsTo = notes.get(notes.size() - 1);
        }

        // copy the notes in encounter order and stamp their halfstep distance to the target note
        for (MeiElementHelper n : notes.values()) {
            MeiElementHelper note = new MeiElementHelper(n.getElement(), true);
            note.setId(note.getId() + "_grace");

            if(halfStepsTo != null) {
                double halfsteps = getHalfstepsBetween(halfStepsTo, note);
                note.set("intm", String.valueOf(halfsteps) + "hs");
            }

            ornamentExpansion.addElement(note);
        }

        appendOrnamentExpansion(principalNote, ornamentExpansion, !graceIsBefore.get());
    }

    /**
     * Recursively collects all note and chord elements from the given MeiElement.
     * Traverses the entire element tree regardless of nesting depth.
     * @param element the element to collect notes from
     * @return a list of all MeiElement notes and chords found
     */
    private Map<String, MeiElementHelper> collectAllNotes(MeiElementHelper element) {
        Map<String, MeiElementHelper> notes = new LinkedHashMap<String, MeiElementHelper>();
        String elementName = element.getName();

        // If this element is a note or chord, add it to the list
        if (elementName.equals("note") || elementName.equals("chord")) {
            notes.put(element.getId(), element);
        }

        // Recursively traverse all children
       ArrayList<MeiElementHelper> children = element.getChildrenAsMeiElements();
        for (MeiElementHelper child : children) {
            notes.putAll(collectAllNotes(new MeiElementHelper(child)));
        }

        return notes;
    }

    /**
     * returns the element's "full"name, e.g. "upper mordent", "lower turn", "double cadence lower prefix"
     * @param element
     * @return
     */
    private String getOrnamentFullName(MeiElementHelper element) {
        String form = element.get("form");

        String name = element.getName();

        if(name.equals("ornam")) {
            MeiElementHelper symbol = new MeiElementHelper(element.getFirstChildByName("symbol"));
            return getOrnamentFullNameFromSymbol(symbol);
        }
        if(form != null && !form.isEmpty() && !form.equals("unknown"))
            name = form + " " + name;

        return name;
    }
    /**
     * returns the element's "full"name from the symbol's glyphName, e.g. "double cadence lower prefix"
     * @param symbol
     * @return
     */
    private String getOrnamentFullNameFromSymbol(MeiElementHelper symbol) {
        if(symbol == null)
            return null;

        String glyphName = symbol.get("glyph.name");
        if(glyphName == null)
            return null;

        String prefix = "ornamentPrecomp";
        glyphName = glyphName.startsWith(prefix) ? glyphName.substring(prefix.length()) : glyphName;

        return String.join(" ", glyphName.split("(?=[A-Z])") ).toLowerCase();
    }

    /**
     * checks and prepares the OrnamentExpansion creation, and calls (if sufficient) createOrnamentExpansion
     * @param element
     */
    private void expandOrnamentsElement(Element element) {
        MeiElementHelper ornament = new MeiElementHelper(element);
        String ornamFullName = getOrnamentFullName(ornament);
        if(ornamFullName == null || ornamFullName.equals("") || !ornamentDictionary.has(ornamFullName))
            return;     // if I am not yet supported

        if (checkForCombinedOrnaments(ornament))
            return;     // if I am in combination with another ornament that is previous to me, but has not been processed, yet

        String startid = ornament.get("startid");
        if(startid == null)
            return;     // if the corresponding note cannot be identified
        startid = startid.replace("#", "");

        Element principalNoteElement = Helper.getFirstDescendantById(Helper.getParentElement(ornament.getElement()), startid);
        if (principalNoteElement == null)
            return;     // if the corresponding note is not available
        MeiElementHelper principalNote = new MeiElementHelper(principalNoteElement);

        if(ornament.get("staff") == null && ornament.get("part") == null) {
            MeiElementHelper parent = principalNote;
            do {
                parent = new MeiElementHelper(parent.getParent());
                if(parent != null && (parent.getName().equals("staff") || parent.getName().equals("part")))
                    if(parent.has("n")) {
                        ornament.set(parent.getName(), parent.get("n"));
                        break;
                    }
            } while (parent != null && !parent.getName().equals("part"));
        }

        OrnamentExpansion ornamentExpansion = createOrnamentExpansion(ornamFullName, principalNote, ornament);
        appendOrnamentExpansion(principalNote, ornamentExpansion, true);

        checkForNextOrnament(ornament);
    }

    /**
     * creates an OrnamentExpansion with ornamentName for the ornament, by creating notes to be played within a <supplied></supplied> addition after the principal note.
     * This OrnamentExpansion is inserted into the given MEI (the original MEI file stays untouched).
     * @param ornamentName
     * @param principalNote
     * @param ornament
     * @return
     */
    private OrnamentExpansion createOrnamentExpansion(String ornamentName, MeiElementHelper principalNote, MeiElementHelper ornament) {
        OrnamentExpansion ornamentExpansion = new OrnamentExpansion();
        ornamentExpansion.addCorrespondence(principalNote); // sets the corresponds of the OrnamentExpansion to the ornament, as the ornament has a correspondence to the principalNote via "startid"
        ornamentExpansion.getGroupElement().set("corresp", ornament.getId());

        String delayed = ornament.get("delayed");
        if(delayed == null || delayed.equals("false"))                  // check if ornament is delayed (to be spaced "at end")
            delayed = "";
        else
            delayed = " delayed";
        ornamentExpansion.setLabel(ornamentName + delayed);

        List<String> alterations = ornamentDictionary.get(ornamentName);    // get alterations from dictionary

        int pnDur = Integer.parseInt(principalNote.get("dur"));
        int noteDuration = 32;

        // collect ornament`s accidental denotions
        String principalAccid = getCurrentAccid(principalNote);
        String upperAccid = ornament.get("accidupper");
        String lowerAccid = ornament.get("accidlower");
        boolean isFirstPrincipal = principalAccid != null;
        boolean isFirstUpper = upperAccid != null;
        boolean isFirstLower = lowerAccid != null;

        for (String alterationEntry : alterations) {
            // keep barline markers in the ornament stream so repeats survive the expansion
            if(alterationEntry.equals("|:")) {
                MeiElementHelper repeat = new MeiElementHelper("barLine");
                repeat.set("form", "rptstart");
                ornamentExpansion.addElement(repeat);
                continue;
            }
            if(alterationEntry.equals(":|")) {
                MeiElementHelper repeat = new MeiElementHelper("barLine");
                repeat.set("form", "rptend");
                ornamentExpansion.addElement(repeat);
                continue;
            }
            if(alterationEntry.equals(":|:")) {
                MeiElementHelper repeat = new MeiElementHelper("barLine");
                repeat.set("form", "rptboth");
                ornamentExpansion.addElement(repeat);
                continue;
            }

            // turn each alteration step into a concrete note copy of the principal
            MeiElementHelper note = new MeiElementHelper("note");
            note.set("dur", String.valueOf(noteDuration));
            note.set("oct", principalNote.get("oct"));
            note.set("pname", principalNote.get("pname"));

            // move the note diatonically first, then handle the accidental in the following
            int alteration = Integer.parseInt(alterationEntry);
            Helper.shiftNoteDiatonicly(note.getElement(), alteration);

            String accid = getCurrentAccid(note);
            if(!accid.isEmpty())
                note.set("accid", accid);                   // explicitly set the accid

            // use the given principal, upper, or lower accidental for the first note of each kind
            switch (alteration) {
                case 0:
                    setAccidGes(note, principalAccid, isFirstPrincipal);
                    isFirstPrincipal = false;
                    break;
                case 1:
                    setAccidGes(note, upperAccid, isFirstUpper);
                    isFirstUpper = false;
                    break;
                case -1:
                    setAccidGes(note, lowerAccid, isFirstLower);
                    isFirstLower = false;
                    break;
            }

            // store the distance from the principal note
            double halfsteps = getHalfstepsBetween(principalNote, note);
            note.set("intm", String.valueOf(halfsteps)+"hs");

            ornamentExpansion.addElement(note);
        }

        return ornamentExpansion;
    }

    /**
     * returns the halfsteps between principalNote and auxiliryNote
     * @param principalNote
     * @param auxiliaryNote
     * @return
     */
    private double getHalfstepsBetween(MeiElementHelper principalNote, MeiElementHelper auxiliaryNote) {
        double halfsteps = 0.0;

        String priAccid = getCurrentAccid(principalNote);
        String auxAccid = getCurrentAccid(auxiliaryNote);

        halfsteps = Helper.getHalfstepsBetween(principalNote.get("pname"), auxiliaryNote.get("pname"));
        halfsteps = halfsteps + (12 * (Integer.parseInt(auxiliaryNote.get("oct")) - Integer.parseInt(principalNote.get("oct"))));

        halfsteps = halfsteps - Helper.accidString2decimal(priAccid);
        halfsteps = halfsteps + Helper.accidString2decimal(auxAccid);

        return halfsteps;
    }

    /**
     * returns the current accid for the note. If note has no accid, the measure's accid (with fallback to the current key) will be returned.
     * @param note
     */
    private String getCurrentAccid(MeiElementHelper note) {
        if(note.has("accid"))
            return note.get("accid");

        String accid = "";
        if(currentAccids.containsKey(note.get("oct")) && currentAccids.get(note.get("oct")).containsKey(note.get("pname"))) {
            accid = currentAccids.get(note.get("oct")).get(note.get("pname"));
        }
        else if(currentKey.containsKey(note.get("pname"))) {
            accid = currentKey.get(note.get("pname"));
        }

        return accid;
    }

    /**
     * sets the accid(.ges) Attribute for having these information explicitly in the resulting ornaments ornamentExpansion notes. This simplifies OrnamentExpansion merging.
     * @param note
     * @param accid
     * @param setAccidToo
     */
    private static void setAccidGes(MeiElementHelper note, String accid, boolean setAccidToo) {
        if(accid == null)
            return;
        if(setAccidToo) {
            note.set("accid", accid);
        }
        note.set("accid.ges", accid);
    }

    /**
     * flattens a nested hierarchy of a graceGrp's to a plain list of all occurring graceGrps. Elements like single notes not being in a graceGrp will get (grace)grouped ("grp1 note1 note2 grp2 note3" will become "grp1 grp3 grp2 grp4").
     * @param element graceGrp with nested graceGrp's
     * @return list of graceGrp
     */
    private ArrayList<MeiElementHelper> flattenGraceGrp(MeiElementHelper element) {
        ArrayList<MeiElementHelper> children = element.getChildrenAsMeiElements();
        ArrayList<MeiElementHelper> graceGrps = new ArrayList<>();

        MeiElementHelper graceGrp = new MeiElementHelper("graceGrp");
        for (MeiElementHelper child : children) {
            if(child.getName().equals("graceGrp") || child.getName().equals("beam")) {
                if(!graceGrp.getChildren().isEmpty()) {
                    graceGrps.add(graceGrp);
                    graceGrp = new MeiElementHelper("graceGrp");
                }

                graceGrps.addAll(flattenGraceGrp(child));                   // flatten all children
                continue;
            }

            graceGrp.appendChild(child);                                    // add element to a new graceGrp, if it was not a graceGrp itself
        }

        if(!graceGrp.getChildren().isEmpty()) {
            graceGrps.add(graceGrp);
        }
        return graceGrps;
    }

    /**
     * appends the ornamentExpansion directly after the principal Note in the MEI (the original MEI file stays untouched).
     * @param principalNote
     * @param ornamentExpansion
     * @param appendLast
     */
    private void appendOrnamentExpansion(MeiElementHelper principalNote, OrnamentExpansion ornamentExpansion, boolean appendLast) {
        OrnamentExpansion existingOrnamentExpansion = ornamentExpansions.get(principalNote.getId());
        if(existingOrnamentExpansion == null) {
            ornamentExpansions.put(principalNote.getId(), ornamentExpansion);
            Helper.appendChildAfterSibling(ornamentExpansion.getOrnamentExpansionElement().getElement(), principalNote.getElement());
            return;
        }

        if(appendLast) {
            existingOrnamentExpansion.append(ornamentExpansion);
        }
        else {
            ornamentExpansion.append(existingOrnamentExpansion);
            existingOrnamentExpansion.getOrnamentExpansionElement().removeParent();

            ornamentExpansions.put(principalNote.getId(), ornamentExpansion);
            Helper.appendChildAfterSibling(ornamentExpansion.getOrnamentExpansionElement().getElement(), principalNote.getElement());
        }
    }

    /**
     * resolves combined ornaments (ornaments for the same principle note, that are written on top of each other) that refer with "prev"/"next" (hopefully) to each other
     * @param ornament
     * @return
     */
    private boolean checkForCombinedOrnaments(MeiElementHelper ornament) {
        if(prevOrnams.contains(ornament.getId())) // I found myself, so I have been expanded already
            return true;
        if(ornament.has("prev")) {
            String prevId = ornament.get("prev");
            if(!prevOrnams.contains(prevId)) {
                nextOrnams.put(ornament.getId(), ornament.getElement()); // remember me for later, do not expandOrnaments me right now
                return true;
            }
        }
        if(ornament.has("next")) {
            prevOrnams.add(ornament.getId()); // put me in the list, that the next one does not wait for me
        }
        return false;
    }

    /**
     * checks and processes already occurred "next" ornament, if the just processed ornament has a "next".
     * @param ornament
     */
    private void checkForNextOrnament(MeiElementHelper ornament) {
        if(nextOrnams.get(ornament.getId()) != null)
            nextOrnams.remove(ornament.getId());
        // if someone is already waiting for me
        Element nextOrnam = nextOrnams.get(ornament.get("next"));
        if(nextOrnam != null) {
            nextOrnams.remove(ornament.get("next")); // remove myself if I was in there
            expandOrnamentsElement(nextOrnam);
        }
    }
}
