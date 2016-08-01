package meico.mei;

/**
 * This class holds the mei data from a source file in a XOM Document.
 * @author Axel Berndt.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import nu.xom.*;
import meico.msm.Msm;

public class Mei {

    private File file = null;                                       // the source file
    private Document mei = null;                                    // the mei document
    private boolean meiValidation = false;                          // indicates whether the input file contained valid mei code (true) or not (false)
    private Helper helper = null;                                   // some variables and methods to make life easier

    /**
     * a default constructor that creates an empty Mei instance
     */
    public Mei() {
    }

    /** constructor; reads the mei file without validation
     *
     * @param file a File object
     */
    public Mei(File file) throws IOException, ParsingException {
        this.readMeiFile(file, false);
    }

    /** constructor
     *
     * @param file a File object
     * @param validate set true to activate validation of the mei document, else false
     */
    public Mei(File file, boolean validate) throws IOException, ParsingException {
        this.readMeiFile(file, validate);
    }

    /** read an mei file
     *
     * @param file a File object
     * @param validate set true to activate validation of the mei document, else false
     */
    protected void readMeiFile(File file, boolean validate) throws IOException, ParsingException {
        this.file = file;

        if (!file.exists()) {
            this.meiValidation = false;
            System.out.println("No such file or directory: " + file.getPath());
            return;
        }

        // read file into the mei instance of Document
        Builder builder = new Builder(validate);                    // if the validate argument is true, the mei should be valid; TODO: this validator does not support RNG validation, hence, throws a ValidityException even for valid mei code
        this.meiValidation = true;                                  // the mei code is valid until validation fails (ValidityException)
        try {
            this.mei = builder.build(file);
        }
        catch (ValidityException e) {                               // in case of a ValidityException (no valid mei code)
            this.meiValidation = false;                             // set meiValidation false to indicate that the mei code is not valid
            e.printStackTrace();                                    // output exception message
            for (int i=0; i < e.getErrorCount(); i++) {             // output all validity error descriptions
                System.out.println(e.getValidityError(i));
            }
            this.mei = e.getDocument();                             // make the XOM Document anyway, we may nonetheless be able to work with it
        }
    }

    /** returns the File object from which the mei data originates
     *
     * @return the File object
     */
    public File getFile() {
        return this.file;
    }

    /**
     * a setter to change the file
     * @param file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /** writes the mei document to a ...-meico.mei file at the same location as the original mei file; this method is mainly relevant for debug output after calling exportMsm()
     *
     * @return true if success, false if an error occured
     */
    public boolean writeMei() {
        String filename = this.file.getPath();
        filename = filename.substring(0, filename.length()-4) + "-meico.mei";   // replace the file extension ".mei" by "-meico.mei"
        return this.writeMei(filename);
    }

    /** writes the mei document to a file (filename should include the path and the extension .mei); this method is mainly relevant for debug output after calling exportMsm()
     *
     * @param filename the filename string including the complete path!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeMei(String filename) {
        // create the file in the file system
        File file = new File(filename);
        file.getParentFile().mkdirs();                              // ensure that the directory exists
        try {
            file.createNewFile();                                   // create the file if it does not already exist
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }

        // open the FileOutputStream to write to the file
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, false);   // open file: second parameter (append) is false because we want to overwrite the file if already existing
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        } catch (FileNotFoundException  e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        // serialize the xml code (encoding, layout) and write it to the file via the FileOutputStream
        boolean returnValue = true;
        Serializer serializer = null;
        try {
            serializer = new Serializer(fileOutputStream, "UTF-8"); // connect serializer with FileOutputStream and specify encoding
            serializer.setIndent(4);                                // specify indents in xml code
            serializer.write(this.mei);                             // write data from mei to file
        } catch (IOException e) {
            e.printStackTrace();
            returnValue = false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            returnValue = false;
        }

        // close the FileOutputStream
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            returnValue = false;
        }

        if (this.file == null)
            this.file = file;

        return returnValue;
    }

    /** if the constructor was unable to load the file, the mei document is empty and no further operations
     *
     * @return true if the mei document is empty, otherwise false
     */
    public boolean isEmpty() {
        return (this.mei == null);
    }

    /**
     * @return true if the validation of the mei file (see constructor method) succeeded and false if failed
     */
    public boolean isValid() {
        return this.meiValidation;
    }

    /**
     * @return root element of the mei document or null
     */
    public Element getRootElement() {
        if (this.isEmpty())
            return null;
        return mei.getRootElement();
    }

    /**
     * @return the <meiHead> element or null if this instance is not valid
     */
    public Element getMeiHead() {
        if (this.isEmpty())
            return null;

        Element e = this.mei.getRootElement().getFirstChildElement("meiHead");
        if (e == null)
            e = this.mei.getRootElement().getFirstChildElement("meiHead", this.mei.getRootElement().getNamespaceURI());

        return e;
    }

    /**
     * @return the <music> element or null if this instance is not valid
     */
    public Element getMusic() {
        if (this.isEmpty())
            return null;

        Element e = this.mei.getRootElement().getFirstChildElement("music");
        if (e == null)
            e = this.mei.getRootElement().getFirstChildElement("music", this.mei.getRootElement().getNamespaceURI());

        return e;
    }

    /** converts the mei data into msm format and returns a list of Msm instances, one per movement/mdiv; the thime resolution (pulses per quarter note) is 720 by default or more if required (for very short note durations)
     *
     * @return the list of msm documents (movements) created
     */
    public List<Msm> exportMsm() {
        return this.exportMsm(720);                                             // do the conversion with a default value of pulses per quarter
    }

    /** converts the mei data into msm format and returns a list of Msm instances, one per movement/mdiv, ppq (pulses per quarter) sets the time resolution
     *
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @return the list of msm documents (movements) created
     */
    public List<Msm> exportMsm(int ppq) {
        return this.exportMsm(ppq, true, true);
    }

    /** converts the mei data into msm format and returns a list of Msm instances, one per movement/mdiv, ppq (pulses per quarter) sets the time resolution
     *
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @param  dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @return the list of msm documents (movements) created
     */
    public List<Msm> exportMsm(int ppq, boolean dontUseChannel10) {
        return this.exportMsm(ppq, dontUseChannel10, true);
    }

    /** converts the mei data into msm format and returns a list of Msm instances, one per movement/mdiv, ppq (pulses per quarter) sets the time resolution
     *
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @param  dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @param msmCleanup set true to return a clean msm file or false to keep all the crap from the conversion
     * @return the list of msm documents (movements) created
     */
    public List<Msm> exportMsm(int ppq, boolean dontUseChannel10, boolean msmCleanup) {
        if (this.isEmpty() || (this.getMusic() == null) || (this.getMusic().getFirstChildElement("body", this.getMusic().getNamespaceURI()) == null))      // if no mei music data available
            return new ArrayList<Msm>();                                        // return empty list

        // check whether the  shortest duration in the mei (note value can go down to 2048th) is captured by the defined ppq resolution; adjust ppq automatically and output a message
        int minPPQ = this.computeMinimalPPQ();                                  // compute the minimal required ppq resolution
        if (minPPQ > ppq) {                                                     // if it is greater than the specified resolution
            ppq = minPPQ;                                                       // adjust the specified ppq to ensure viable results
            System.out.println("The specified pulses per quarternote resolution (ppq) is too coarse to capture the shortest duration values in the mei source. Using the minimal requred resolution of " + ppq + " instead");
        }

        this.resolveCopyofs();
        this.reorderElements();                                                 // control elements (e.g. tupletSpan) are often not placed in the timeline but at the end of the measure, this must be resolved

        this.helper = new Helper(ppq);                                          // some variables and methods to make life easier
        this.helper.dontUseChannel10 = dontUseChannel10;                        // set the flag that says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on
        List<Msm> msms = this.convert(this.getMusic().getFirstChildElement("body", this.getMusic().getNamespaceURI()));
        this.helper = null;                                                     // as this is a class variable it would remain in memory after this method, so it has to be nulled for garbage collection

        if (msmCleanup) Helper.msmCleanup(msms);                                // cleanup of the msm objects to remove all conversion related and no longer needed entries in the msm objects

        // generate a dummy file name in the msm objects
        if (msms.size() == 1)                                                                                                       // if only one msm object (no numbering needed)
            msms.get(0).setFile(this.getFile().getPath().substring(0, this.getFile().getPath().length()-3) + "msm");                // replace the file extension mei with msm and make this the filename
        else {                                                                                                                      // multiple msm objects created (or none)
            for (int i=0; i < msms.size(); ++i) {                                                                                   // for each msm object
                msms.get(i).setFile(this.getFile().getPath().substring(0, this.getFile().getPath().length()-4) + "-" + i + ".msm"); // replace the extension by the number and the .msm extension
            }
        }

        return msms;
    }

    /** recursively traverse the mei tree (depth first) starting at the root element and return the list of Msm instances; root indicates the root of the subtree
     *
     * @param root the root of the subtree to be processed
     * @return a list of msm documents, i.e., the movements created
     */
    private List<Msm> convert(Element root) {
        Elements es = root.getChildElements();                                  // all child elements of root

        for (int i = 0; i < es.size(); ++i) {                                   // element beginHere traverses the mei tree
            Element e = es.get(i);                                              // get the element

            this.helper.checkEndid(e);                                          // check for pending elements with endid attributes to be finished when the element with this endid is found

            // process the element
            if (e.getLocalName().equals("accid")) {
                continue;                                                   // this element is proccessed as child of a note, not here
            } else if (e.getLocalName().equals("add")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("anchorText")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("annot")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("app")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("arpeg")) {
                continue;                                                   // TODO: ignored at the moment but relevant for expressive performance later on
            } else if (e.getLocalName().equals("artic")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("barline")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("beam")) {// contains the notes to be beamed TODO: relevant for expressive performance

            } else if (e.getLocalName().equals("beamSpan")) {
                continue;                                                   // TODO: may be relavant for expressive phrasing
            } else if (e.getLocalName().equals("beatRpt")) {
                this.processBeatRpt(e);
                continue;
            } else if (e.getLocalName().equals("bend")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("breath")) {
                continue;                                                   // TODO: relevant for expressive performance - cesura
            } else if (e.getLocalName().equals("bTrem")) {
                Element f = this.helper.currentChord;                       // we could already be within a chord or bTrem or fTrem environemnt; this should be stored to return to it afterwards
                this.helper.currentChord = e;                               // handle it just like a chord
                this.convert(e);                                            // process everything within this environment
                this.helper.currentChord = f;                               // foget the pointer to this chord and return to the surrounding environment or nullptr
                if (this.helper.currentChord == null)                       // we are done with all chord/bTrem environments
                this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString((Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + this.helper.computeDuration(e)))); // update currentDate
                continue;                                                   // continue with the next sibling
            } else if (e.getLocalName().equals("choice")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("chord")) {
                if (e.getAttribute("grace") != null)                        // TODO: at the moment we ignore grace notes and grace chords; later on, for expressive performances, we should handle these somehow
                    continue;
                this.preProcessChord(e);                                    // preprocessing of the chord element
                Element f = this.helper.currentChord;                       // we could already be within a chord or bTrem or fTrem environemnt; this should be stored to return to it afterwards
                this.helper.currentChord = e;                               // set the temp.chord pointer to this chord
                this.convert(e);                                            // process everything within this chord
                this.helper.currentChord = f;                               // foget the pointer to this chord and return to the surrounding environment or nullptr
                if (this.helper.currentChord == null)                       // we are done with all chord/bTrem environments
                this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString((Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + this.helper.computeDuration(e)))); // update currentDate
                continue;                                                       // continue with the next sibling
            } else if (e.getLocalName().equals("chordTable")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("clef")) {
                continue;                                                   // TODO: can this be ignored or is it of any relevance to pitch computation?
            } else if (e.getLocalName().equals("clefGrp")) {
                continue;                                                   // TODO: can this be ignored or is it of any relevance to pitch computation?
            } else if (e.getLocalName().equals("corr")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("curve")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("custos")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("damage")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("del")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("dir")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("div")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("dot")) {
                continue;                                                   // this element is proccessed as child of a note, rest etc., not here
            } else if (e.getLocalName().equals("dynam")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("ending")) {// TODO: What can I do with this? Could be relevant for expressive performance (phrasing) na dto generate sectionStructure

            } else if (e.getLocalName().equals("fermata")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("fTrem")) {
                Element f = this.helper.currentChord;                       // we could already be within a chord or bTrem or fTrem environemnt; this should be stored to return to it afterwards
                this.helper.currentChord = e;                               // handle it just like a chord
                this.convert(e);                                            // process everything within this environment
                this.helper.currentChord = f;                               // foget the pointer to this chord and return to the surrounding environment or nullptr
                if (this.helper.currentChord == null)                       // we are done with all chord/bTrem environments
                this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString((Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + this.helper.computeDuration(e)))); // update currentDate
                continue;                                                   // continue with the next sibling
            } else if (e.getLocalName().equals("gap")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("gliss")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("grpSym")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("hairpin")) {
                continue;                                                   // TODO: relevant for expressive performance, cresc./decresc.
            } else if (e.getLocalName().equals("halfmRpt")) {
                this.processHalfmRpt(e);

                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("handShift")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("harm")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("harpPedal")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("ineume")) {
                continue;                                                   // ignored, this implementation focusses on common modern notation
            } else if (e.getLocalName().equals("instrDef")) {
                continue;                                                   // ignore this tag as this converter handles midi stuff individually
            } else if (e.getLocalName().equals("instrGrp")) {
                continue;                                                   // ignore this tag as this converter handles midi stuff individually
            } else if (e.getLocalName().equals("keyAccid")) {
                continue;                                                   // this element is processed within a keySig; if it occurs outside of a keySig environment it is invalid, hence, ignored
            } else if (e.getLocalName().equals("keySig")) {
                this.processKeySig(e);

            } else if (e.getLocalName().equals("label")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("layer")) {
                String oldDate = this.helper.currentPart.getAttribute("currentDate").getValue();    // store currentDate in oldDate for later use
                this.convert(e);                                            // process everything within this environment
                e.addAttribute(new Attribute("currentDate", this.helper.currentPart.getAttribute("currentDate").getValue()));   // store the currentDate in the layer element to later determine the latest of these dates as the staff'spart's currentDate
                this.helper.accid.clear();                                  // accidentals are valid only within one layer, so forget them
                if (Helper.getNextSiblingElement("layer", e) != null)       // if there are more layers in this staff environment
                    this.helper.currentPart.getAttribute("currentDate").setValue(oldDate); // set back to the old currentDate, because each layer is a parallel to the other layers
                else {                                                      // no further layers in this staff environment, this was the last layer in this staff
                    // take the latest layer-specific currentDate as THE currentDate of this part
                    Elements layers = ((Element) e.getParent()).getChildElements("layer");   // get all layers in this staff
                    double latestDate = Double.parseDouble(this.helper.currentPart.getAttribute("currentDate").getValue());
                    for (int j = layers.size() - 1; j >= 0; --j) {
                        double date = Double.parseDouble(layers.get(j).getAttributeValue("currentDate"));   // get the part's date
                        if (latestDate < date)                                                              // if this part's date is later than latestDate so far
                            latestDate = date;                                                              // set latestDate to date
                    }
                    this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(latestDate));
                }
                continue;
            } else if (e.getLocalName().equals("layerDef")) {
                this.processLayerDef(e);

            } else if (e.getLocalName().equals("lb")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("line")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("lyrics")) {// TODO: should I do anything more with it than just diving into it?

            } else if (e.getLocalName().equals("mdiv")) {
                if (this.makeMovement(e).isEmpty()) continue;               // create a new instance of Msm with a new Document and a unique id (size of the movements list so far), if something went wrong (I don't know how, just to be on the save side) stop diving into this subtree

            } else if (e.getLocalName().equals("measure")) {
                this.processMeasure(e);                                     // this creates the date and dur attribute and adds them to the measure
                this.helper.currentMeasure = e;
                this.convert(e);                                            // process everything within this environment
                this.helper.accid.clear();                                  // accidentals are valid within one measure, but not in the succeeding measures, so forget them
                // update the duration of the measure; if the measure is overful, take the respective duration; if underful, keep the defined duration in accordance to its time signature
                Element cm = this.helper.currentMeasure;
                this.helper.currentMeasure = null;                          // this has to be set null so that getMidiTime() does not return the measure's date
                double dur1 = Double.parseDouble(cm.getAttributeValue("dur"));                                  // duration of the measure
                double dur2 = this.helper.getMidiTime() - Double.parseDouble(cm.getAttributeValue("date"));     // duration of the measure's content (ideally it is equal to the measure duration, but could also be over- or underful)
                cm.getAttribute("dur").setValue(Double.toString((dur1 >= dur2) ? dur1 : dur2));                 // take the longer duration as the measure's definite duration
                continue;
            } else if (e.getLocalName().equals("mensur")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("meterSig")) {
                this.processMeterSig(e);

            } else if (e.getLocalName().equals("meterSigGrp")) {// TODO: I have no idea how to handle this, at the moment I go through it and process the contained meterSig elements as if they were standing alone

            } else if (e.getLocalName().equals("midi")) {
                continue;                                                   // ignore this tag as this converter handles midi stuff individually
            } else if (e.getLocalName().equals("mordent")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("mRest")) {
                this.processMeasureRest(e);

            } else if (e.getLocalName().equals("mRpt")) {
                this.processMRpt(e);

            } else if (e.getLocalName().equals("mRpt2")) {
                this.processMRpt2(e);

            } else if (e.getLocalName().equals("mSpace")) {
                this.processMeasureRest(e);                                    // interpret it as an mRest, i.e. measure rest

            } else if (e.getLocalName().equals("multiRest")) {
                this.processMultiRest(e);

            } else if (e.getLocalName().equals("multiRpt")) {
                this.processMultiRpt(e);

            } else if (e.getLocalName().equals("note")) {
                this.processNote(e);
                continue;                                                   // no need to go deeper as any child of this tag is already processed
            } else if (e.getLocalName().equals("octave")) {
                this.processOctave(e);

            } else if (e.getLocalName().equals("orig")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("ossia")) {
                continue;                                                   // TODO: ignored for the moment but may be included later on
            } else if (e.getLocalName().equals("parts")) {
                continue;                                                   // TODO: For the moment, parts are ignored, but have to be handled later on: create additional part entries and midi channels from them.
            } else if (e.getLocalName().equals("part")) {
                continue;                                                   // TODO: For the moment, part is ignored, but has to be handled later on
            } else if (e.getLocalName().equals("pb")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("pedal")) {
                this.processPedal(e);

            } else if (e.getLocalName().equals("pgFoot")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("pgFoot2")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("pgHead")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("pgHead2")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("phrase")) {// dive into it TODO: make an entry in the phraseStructure map

            } else if (e.getLocalName().equals("proport")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("rdg")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("reg")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("reh")) {
                this.processReh(e);// TODO: generate midi jump marks
                continue;
            } else if (e.getLocalName().equals("rend")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("rest")) {
                this.processRest(e);

            } else if (e.getLocalName().equals("restore")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("sb")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("scoreDef")) {
                this.processScoreDef(e);

            } else if (e.getLocalName().equals("score")) {// just dive into it

            } else if (e.getLocalName().equals("section")) {// TODO: What can I do with this? I have to dive into it, as it may contain musical data. I might also use it to generate a sectionStructure map.

            } else if (e.getLocalName().equals("sic")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("space")) {
                this.processRest(e);                                        // handle it like a rest

            } else if (e.getLocalName().equals("slur")) {
                continue;                                                   // TODO: relevant for expressive performance; it indicates legato articulation
            } else if (e.getLocalName().equals("stack")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("staff")) {
                this.helper.currentPart = this.processStaff(e);             // everything within this tag is local to this part
                this.convert(e);                                            // go on recursively with the processing
                this.helper.accid.clear();                                  // accidentals are valid within one measure, but not in the succeeding measures, so forget them
                this.helper.currentPart = null;                             // after this staff entry and its children are processed, set part to nullptr, because there can be global information between the staff entries in mei
                continue;
            } else if (e.getLocalName().equals("staffDef")) {
                this.processStaffDef(e);

            } else if (e.getLocalName().equals("staffGrp")) {// may contain staffDefs but needs no particular processing, attributes are ignored

            } else if (e.getLocalName().equals("subst")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("supplied")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("syl")) {
                continue;                                                   // TODO: can be included in MIDI, too; useful for choir synthesis
            } else if (e.getLocalName().equals("syllable")) {
                continue;                                                   // ignored, this implementation focusses on common modern notation
            } else if (e.getLocalName().equals("symbol")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("symbolTable")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("tempo")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("tie")) {
                continue;                                                   // we do not process these elements; in our implementation it is mandatory to use the tie attributes of tied note elements
            } else if (e.getLocalName().equals("timeline")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("trill")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("tuplet")) {
                if (e.getAttribute("dur") != null) {
                    double cd = Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate"));   // store the current date for use afterwards
                    this.convert(e);                                        // process the child elements
                    this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(cd + this.helper.computeDuration(e)));   // this compensates for numeric problems with the single note durations within the tuplet
                    continue;
                }

            } else if (e.getLocalName().equals("tupletSpan")) {
                this.processTupletSpan(e);
                continue;                                                   // TODO: how do I have to handle this?
            } else if (e.getLocalName().equals("turn")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("unclear")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("uneume")) {
                continue;                                                   // ignored, this implementation focusses on common modern notation
            } else if (e.getLocalName().equals("verse")) {
                continue;                                                   // TODO: ignored
            } else {
                continue;                                                   // ignore it and its children
            }
            this.convert(e);
        }

        return helper.movements;
    }

    /** this function gets an mdiv and creates an instance of Msm
     *
     * @param mdiv an mei mdiv element
     * @return an msm meta element (the root of an msm document)
     */
    private Msm makeMovement(Element mdiv) {
        Element movement = new Element("meta");

        // store the same id at the mei source and the msm, maybe it is needed later on
        Attribute id = Helper.getAttribute("id", mdiv);
        if (id != null) {                                                           // if mdiv has an id
            movement.addAttribute(new Attribute("id", id.getValue()));              // reuse it
        }
        else {                                                                      // otherwise generate a unique id
            String uuid = UUID.randomUUID().toString();
            mdiv.addAttribute(new Attribute("id", uuid));
            movement.addAttribute(new Attribute("id", uuid));
        }

        // create global containers
        Element global = new Element("global");
        Element dated = new Element("dated");
        Element header = new Element("header");

        Element ppq = new Element("pulsesPerQuarter");                                // a global ppq element
        ppq.addAttribute(new Attribute("ppq", Integer.toString(this.helper.ppq)));    // a default ppq value

        header.appendChild(ppq);
        dated.appendChild(new Element("timeSignatureMap"));
        dated.appendChild(new Element("keySignatureMap"));
        dated.appendChild(new Element("miscMap"));
        dated.appendChild(new Element("markerMap"));

        global.appendChild(header);
        global.appendChild(dated);
        movement.appendChild(global);

        Msm msm = new Msm(new Document(movement));                                  // create the Msm object
        this.helper.movements.add(msm);                                             // add it to the movements list
        this.helper.reset();                                                        // reset the helper variables
        this.helper.currentMovement = msm.getRootElement();                         // store root of current movement for later reference

        return msm;                                                                 // create a new instance of Msm with a new instance of Document
    }

    /** process an mei scoreDef element
     *
     * @param scoreDef an mei scoreDef element
     */
    private void processScoreDef(Element scoreDef) {
        if (this.helper.currentPart != null) {                                                      // if we are already in a specific part, these infos are treaded as local
            this.processStaffDef(scoreDef);
            return;
        }

        scoreDef.addAttribute(new Attribute("date", Double.toString(helper.getMidiTime())));

        // otherwise all entries are done in globally maps
        Element s;

        // time signature
        s = this.makeTimeSignature(scoreDef);                                                       // create a time signature element, or null if there is no such data
        if (s != null) {                                                                            // if succeeded
            this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").appendChild(s);  // insert it into the global time signature map
        }

        // key signature
        s = this.makeKeySignature(scoreDef);                                                        // create a key signature element, or null if there is no such data
        if (s != null) {                                                                            // if succeeded
            this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("keySignatureMap").appendChild(s);   // insert it into the global key signature map
        }

        // store default values in miscMap
        if ((scoreDef.getAttribute("dur.default") != null)) {                                       // if there is a default duration defined
            Element d = new Element("dur.default");                                                 // make an entry in the miscMap
            d.addAttribute(new Attribute("dur", scoreDef.getAttributeValue("dur.default")));        // copy the value
            Helper.copyId(scoreDef, d);                                                             // copy the id
            this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(d);   // make an entry in the miscMap
        }

        if (scoreDef.getAttribute("octave.default") != null) {                                      // if there is a default octave defined
            Element d = new Element("oct.default");
            d.addAttribute(new Attribute("oct", scoreDef.getAttributeValue("octave.default")));     // copy the value
            Helper.copyId(scoreDef, d);                                                             // copy the id
            this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(d);   // make an entry in the miscMap
        }

        {   // if there is a transposition (we only support the trans.semi attribute, not trans.diat)
            int trans = 0;
            trans = (scoreDef.getAttribute("trans.semi") == null) ? 0 : Integer.parseInt(scoreDef.getAttributeValue("trans.semi"));
            trans += Helper.processClefDis(scoreDef);
            Element d = new Element("transposition");                                               // create a transposition entry
            d.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));
            d.addAttribute(new Attribute("semi", Integer.toString(trans)));                         // copy the value or write "0" for no transposition (this is to cancel previous entries)
            Helper.copyId(scoreDef, d);                                                             // copy the id
            this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(d);    // make an entry in the miscMap
        }

        // MIDI channel and port information are ignored as these are assigned automatically by this converter
        // attribute ppq is ignored ase the converter defines an own ppq resolution
        // TODO: tuning is defined by attributes tune.pname, tune.Hz and tune.temper; for the moment these are ignored

        this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(Helper.cloneElement(scoreDef));    // make a copy of the element and put it into the global miscMap
    }

    /** process an mei staffDef element
     *
     * @param staffDef an mei staffDef element
     */
    private void processStaffDef(Element staffDef) {
        Element part = this.makePart(staffDef);                                                             // create a part element in movement, or get Element pointer if this part exists already

        staffDef.addAttribute(new Attribute("date", Double.toString(helper.getMidiTime())));

        // handle local time signature entry
        Element t = this.makeTimeSignature(staffDef);                                                       // create a time signature element, or null if there is no such data
        if (t != null) {                                                                                    // if succeeded
            part.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").appendChild(t);     // insert it into the global time signature map
        }

        // handle local key signature entry
        t = this.makeKeySignature(staffDef);																// create a key signature element, or nullptr if there is no such data
        if (t != null) {                                                                                    // if succeeded
            part.getFirstChildElement("dated").getFirstChildElement("keySignatureMap").appendChild(t);      // insert it into the global key signature map
        }

        // store default values in miscMap
        if ((staffDef.getAttribute("dur.default") != null)) {                                               // if there is a default duration defined
            Element d = new Element("dur.default");                                                         // make an entry in the miscMap
            d.addAttribute(new Attribute("dur", staffDef.getAttributeValue("dur.default")));                // copy the value
            Helper.copyId(staffDef, d);                                                                     // copy the id
            part.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(d);              // make an entry in the miscMap
        }

        if ((staffDef.getAttribute("octave.default", staffDef.getNamespaceURI()) != null)) {                // if there is a default duration defined
            Element d = new Element("oct.default");                                                         // make an entry in the miscMap
            d.addAttribute(new Attribute("oct", staffDef.getAttributeValue("octave.default")));             // copy the value
            Helper.copyId(staffDef, d);                                                                     // copy the id
            part.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(d);              // make an entry in the miscMap
        }


        {   // if there is a transposition (we only support the trans.semi attribute, not trans.diat)
            int trans = 0;
            trans = (staffDef.getAttribute("trans.semi") == null) ? 0 : Integer.parseInt(staffDef.getAttributeValue("trans.semi"));
            trans += Helper.processClefDis(staffDef);
            Element d = new Element("transposition");                                                       // create a transposition entry
            d.addAttribute(new Attribute("semi", Integer.toString(trans)));  // copy the value or write "0" for no transposition (this is to cancel previous entries)
            d.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));
            Helper.copyId(staffDef, d);                                                                     // copy the id
            part.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(d);              // make an entry in the miscMap
        }

        // attribute ppq is ignored ase the converter defines an own ppq resolution
        // TODO: tuning is defined by attributes tune.pname, tune.Hz and tune.temper; for the moment these are ignored

        part.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(Helper.cloneElement(staffDef));    // make a copy of the element and put it into the global miscMap
    }

    /** process an mei staff element
     *
     * @param staff an mei staff element
     * @return an msm part element
     */
    Element processStaff(Element staff) {
        Attribute ref = staff.getAttribute("def");                              // get the part entry, try the def attribute first
        if (ref == null) ref = staff.getAttribute("n");                         // otherwise the n attribute
        Element s = this.helper.getPart((ref == null) ? "" : ref.getValue());   // get the part

        if (s != null) {
//            s.addAttribute(new Attribute("currentDate", (this.helper.currentMeasure != null) ? this.helper.currentMeasure.getAttributeValue("date") : "0.0"));  // set currentDate of processing
            s.addAttribute(new Attribute("currentDate", Double.toString(this.helper.getMidiTime())));  // set currentDate of processing
            return s;                                                           // if that part entry was found, return it
        }

        // the part was not found, create one
        System.out.println("There is an undefined staff element in the score (no corresponding staffDef with attribute " + ref + "). Generating a new part for it.");  // output notification
        return this.makePart(staff);                                            // generate a part and return it
    }

    /** process an mei layerDef element
     *
     * @param layerDef an mei layerDef element
     */
    private void processLayerDef(Element layerDef) {
        layerDef.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));

        if (layerDef.getAttribute("dur.default") != null) {                                                         // if there is a default duration defined
            Element d = new Element("dur.default");
            this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(d);   // make an entry in the miscMap
            d.addAttribute(new Attribute("dur", layerDef.getAttributeValue("dur.default")));                        // copy the value
            Helper.copyId(layerDef, d);                                                                             // copy the id
        }

        if (layerDef.getAttribute("octave.default") != null) {                                                      // if there is a default octave defined
            Element d = new Element("oct.default");
            this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(d);   // make an entry in the miscMap
            d.addAttribute(new Attribute("oct", layerDef.getAttributeValue("octave.default")));                     // copy the value
            Helper.copyId(layerDef, d);                                                                             // copy the id
        }

        if (this.helper.currentPart == null) {                                                                      // if the layer is globally defined
            this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(Helper.cloneElement(layerDef));   // make a copy of the element and put it into the global miscMap
            return;
        }

        this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(Helper.cloneElement(layerDef));  // otherwise make a copy of the element and put it into the local miscMap
    }

    /** process an mei measure element
     *
     * @param measure an mei measure element
     */
    private void processMeasure(Element measure) {
        measure.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));    // set the measure's date

        // compute the duration of this measure
        double dur = 0.0;                                               // its duration

        if ((this.helper.currentPart != null) && (this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("element") != null)) {    // if there is a local time signature map that is not empty
            Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element");
            dur = this.helper.computeMeasureLength(Integer.parseInt(es.get(es.size()-1).getAttributeValue("numerator")), Integer.parseInt(es.get(es.size()-1).getAttributeValue("denominator")));
        }
        else if (this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("element") != null) {   // if there is a global time signature map
            Elements es = this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element");
            dur = this.helper.computeMeasureLength(Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator")), Integer.parseInt(es.get(es.size()-1).getAttributeValue("denominator")));
        }

        measure.addAttribute(new Attribute("dur", Double.toString(dur)));
    }

    /** process an mei meterSig element
     *
     * @param meterSig an mei meterSig element
     */
    private void processMeterSig(Element meterSig) {
        Element s = this.makeTimeSignature(meterSig);   // create a time signature element, or nullptr if there is no sufficient data

        if (s == null) return;                          // if failed, cancel

        // insert in time signature map
        if (this.helper.currentPart != null) {          // local entry
            this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").appendChild(s);  // insert it into the local time signature map
        }
        else {                                          // global entry
            this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").appendChild(s);   // insert it into the global time signature map
        }
    }

    /** process an mei keySig element
     *
     * @param keySig an mei keySig element
     */
    private void processKeySig(Element keySig) {
        Element s = this.makeKeySignature(keySig);      // create a key signature element, or nullptr if there is no sufficient data

        if (s == null) return;                          // if failed

        // insert in key signature map
        if (this.helper.currentPart != null) {          // local entry
            this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("keySignatureMap").appendChild(s);   // insert it into the local key signature map
        }
        else {                                          // global entry
            this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("keySignatureMap").appendChild(s);    // insert it into the global key signature map
        }
    }

    /** make a part entry in xml from an mei staffDef and insert into movement, if it exists already, return it
     *
     * @param staffDef an mei staffDef element
     * @return an msm part element
     */
    private Element makePart(Element staffDef) {
        Element part = this.helper.getPart(staffDef.getAttributeValue("n"));                                   // search for that part in the xml data created so far

        if (part != null) {                                                                                    // if already in the list
            return part;                                                                                       // return it
        }

        part = new Element("part");                                                                            // create part element

        String label = "";
        if (((Element)staffDef.getParent()).getLocalName().equals("staffGrp"))                                 // if there is a staffGrp as parent element
            if (((Element)staffDef.getParent()).getAttribute("label") != null)                                 // and it has a label
                label = ((Element)staffDef.getParent()).getAttributeValue("label");                            // use it in the msm part name
        if (staffDef.getAttribute("label") != null)                                                            // does the staffDef iteself have a name
            label += (label.isEmpty()) ? staffDef.getAttributeValue("label") : " " + staffDef.getAttributeValue("label"); // append it to the label string so far (with a space between staffGrp label and staffDef label)

        if (!label.isEmpty())
            part.addAttribute(new Attribute("name", label));


        if (staffDef.getAttribute("n") != null) {
            part.addAttribute(new Attribute("number", staffDef.getAttributeValue("n")));                       // take the n attribute instead
        }
        else {                                                                                             // otherwise generate an id
            String id = UUID.randomUUID().toString();                                                      // ids of generated parts start with UUID
            staffDef.addAttribute(new Attribute("n", id));
            part.addAttribute(new Attribute("number", id));
        }

        {
            Elements ps = this.helper.currentMovement.getChildElements("part");
            if (ps.size() == 0) {
                part.addAttribute(new Attribute("channel", "0"));                                              // set midi channel
                part.addAttribute(new Attribute("port", "0"));                                                 // set midi port
            }
            else {
                Element p = ps.get(ps.size()-1);                                                            // choose last part entry
                int channel = (Integer.parseInt(p.getAttributeValue("channel")) + 1) % 16;                  // increment channel counter mod 16
                if ((channel == 9) && this.helper.dontUseChannel10)                                                    // if the drum channel should be avoided
                    ++channel;                                                                                  // do so
                int port = (channel == 0) ? (Integer.parseInt(p.getAttributeValue("port")) + 1) % 256 : Integer.parseInt(p.getAttributeValue("port"));	// increment port counter if channels of previous port are full
                part.addAttribute(new Attribute("channel", Integer.toString(channel)));                          // set midi channel
                part.addAttribute(new Attribute("port", Integer.toString(port)));                                // set midi port
            }
        }

        Element dated = new Element("dated");
        dated.appendChild(new Element("timeSignatureMap"));
        dated.appendChild(new Element("keySignatureMap"));
        dated.appendChild(new Element("markerMap"));
        Element miscMap = new Element("miscMap");
        miscMap.appendChild(new Element("tupletSpanMap"));
        dated.appendChild(miscMap);
        dated.appendChild(new Element("score"));
        part.appendChild(new Element("header"));
        part.appendChild(dated);
        part.addAttribute(new Attribute("currentDate", (this.helper.currentMeasure != null) ? this.helper.currentMeasure.getAttributeValue("date") : "0.0"));    // set currentDate of processing

        this.helper.currentMovement.appendChild(part);                                                         // insert it into movement

        return part;
    }

    /** make a time signature entry from an mei scoreDef, staffDef or meterSig element and return it or return null if no sufficient information
     *
     * @param meiSource an mei scoreDef, staffDef or meterSig element
     * @return an msm element for the timeSignatureMap
     */
    private Element makeTimeSignature(Element meiSource) {
        Element s = new Element("element");                                                 // create an element
        Helper.copyId(meiSource, s);                                                        // copy the id

        // date of the element
        s.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));  // compute the date

        // count and unit are preferred in the processing; if not givven take sym
        if (((meiSource.getAttribute("count") != null) && (meiSource.getAttribute("unit") != null)) || ((meiSource.getAttribute("meter.count") != null) && (meiSource.getAttribute("meter.unit") != null))) {
            // the meter.count attribute may also be like "2+5.5+3.857"
            String str = (meiSource.getLocalName().equals("meterSig")) ? meiSource.getAttributeValue("count") : meiSource.getAttributeValue("meter.count");
            Double result = 0.0;
            String num = "";
            for (int i = 0; i < str.length(); ++i) {
                if (((str.charAt(i) >= '0') && (str.charAt(i) <= '9')) || (str.charAt(i) == '.')) { // if character is a number/digit or a decimal dot
                    num += str.charAt(i);                                                       // add to num to parse it as double
                    continue;
                }
                // in any other case parse the string in num as a double and begin with a new
                result += (num.isEmpty()) ? 0.0 : Double.parseDouble(num);
                num = "";
            }
            result += (num.isEmpty()) ? 0.0 : Double.parseDouble(num);
            s.addAttribute(new Attribute("numerator", Double.toString(result)));               // store numerator

            s.addAttribute(new Attribute("denominator", (meiSource.getLocalName().equals("meterSig")) ? meiSource.getAttributeValue("unit") : meiSource.getAttributeValue("meter.unit")));        // store denominator
            return s;
        }
        else {      // process meter.sym / sym
            if ((meiSource.getAttribute("sym") != null) || (meiSource.getAttribute("meter.sym") != null)) {
                String str = (meiSource.getLocalName().equals("meterSig")) ? meiSource.getAttributeValue("sym") : meiSource.getAttributeValue("meter.sym");
                if (str.equals("common")) {
                    s.addAttribute(new Attribute("numerator", "4"));                        // store numerator
                    s.addAttribute(new Attribute("denominator", "4"));                      // store denominator
                    return s;
                } else if (str.equals("cut")) {
                    s.addAttribute(new Attribute("numerator", "2"));                        // store numerator
                    s.addAttribute(new Attribute("denominator", "2"));                      // store denominator
                    return s;
                }
            }
        }
        return null;
    }

    /** make a key signature entry from an mei scoreDef, staffDef or keySig element and return it or return null if no sufficient information
     *
     * @param meiSource an mei scoreDef, staffDef or keySig element
     * @return an msm element for the keySignatureMap or null
     */
    private Element makeKeySignature(Element meiSource) {
        Element s = new Element("element");                                                             // create an element
        Helper.copyId(meiSource, s);                                                                    // copy the id
        s.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));              // compute date

        // process scoreDef and staffDef
        if (meiSource.getLocalName().equals("scoreDef") || meiSource.getLocalName().equals("staffDef")) {   // if meiSource is a scoreDef or staffDef
            if (meiSource.getAttribute("key.sig") != null) {                                            // if there are no key signature data, return nullptr
                String accidentals = meiSource.getAttributeValue("key.sig");                            // get key signature string

                switch (accidentals.charAt(accidentals.length()-1)) {                                   // check last character for flats (f) or sharps (s)
                    case 'f':                                                                           // if flats
                        accidentals = accidentals.substring(0, accidentals.length()-1);                 // remove 'f' from string
                        s.addAttribute(new Attribute("accidentals", "-" + accidentals));                // store minus number of accidentals
                        break;
                    case 's':                                                                           // if sharps
                        accidentals = accidentals.substring(0, accidentals.length()-1);                 // remove 's' from string
                        s.addAttribute(new Attribute("accidentals", accidentals));                      // store positive number of accidentals
                        break;
                    case '0':                                                                           // no accidentals from now on
                        s.addAttribute(new Attribute("accidentals", "0"));                              // store it
                        break;
                    default:                                                                            // what if neither 'f' nor 's'?
                        System.out.println("There is an invalid key.sig attribute: " + accidentals);    // output error message
                }
                return s;
            }
            return null;                                                                                // cancel
        }

        if (!meiSource.getLocalName().equals("keySig"))                                                 // if meiSource is also no keySig
            return null;                                                                                // it is no valid key signature related element, hence, cancel

        // process keySig
        int flats = 0;                                                                                  // number of flats
        int sharps = 0;                                                                                 // number of sharps
        Elements acs = meiSource.getChildElements("keyAccid");                                          // get all keyAccid elements
        for (int i = acs.size()-1; i >= 0; --i) {                                                       // count the accids
            if (acs.get(i).getAttribute("accid") == null) continue;                                     // if none given, continue
            // the following does not justice to mei, there you can define key signatures with whatever accidentals on whatever notes (eg. a key signature with two flats on e and f and one sharp on g); this is nothing that a midi time signature can capture, the mei note pitches must be given correctly so the time signature does not matter!
            String s1 = acs.get(i).getAttributeValue("accid");
            if (s1.equals("s") || s1.equals("ss") || s1.equals("x") || s1.equals("xs") || s1.equals("ts") || s1.equals("ns") || s1.equals("su") || s1.equals("sd") || s1.equals("nu") || s1.equals("1qs") || s1.equals("3qs")) {
                sharps++;

            } else if (s1.equals("f") || s1.equals("ff") || s1.equals("tf") || s1.equals("nf") || s1.equals("fu") || s1.equals("fd") || s1.equals("nd") || s1.equals("1qf") || s1.equals("3qf")) {
                flats++;
            }
        }
        s.addAttribute(new Attribute("accidentals", Integer.toString(sharps-flats)));                   // store the result of sharps-flats
        return s;
    }

    /** preprocessing of an mei chord element
     *
     * @param chord an mei chord element
     */
    private void preProcessChord(Element chord) {
        // inherit attributes of the surrounding environment
        if (this.helper.currentChord != null) {                                                                     // if we are already within a chord or bTrem or fTrem environment
            if ((chord.getAttribute("dur") == null) && (this.helper.currentChord.getAttribute("dur") != null)) {    // if duration attribute missing, but there is one in the environment
                chord.addAttribute(new Attribute("dur", this.helper.currentChord.getAttributeValue("dur")));        // take this
            }
            if ((chord.getAttribute("dots") == null) && (this.helper.currentChord.getAttribute("dots") != null)) {  // if dots attribute missing, but there is one in the environment
                chord.addAttribute(new Attribute("dots", this.helper.currentChord.getAttributeValue("dots")));      // take this
            }
        }
    }

    /** process an mei tupletSpan element; the element MUST be in a staff environment; this method does not process tstamp and tstamp2 or tstamp.ges or tstamp.real; there MUST be a dur, dur.ges or endid attribute
     *
     * @param tupletSpan an mei tupletSpan element
     */
    private void processTupletSpan(Element tupletSpan) {
        // check validity of information
        if ((this.helper.currentPart == null)                                                                   // the tupletSpan is not in a staff environment, so I have no idea where it belongs to and to which note and rest elements it applies
//                || ((tupletSpan.getAttribute("startid") == null) && (tupletSpan.getAttribute("tstamp") == null) && (tupletSpan.getAttribute("tstamp.ges") == null) && (tupletSpan.getAttribute("tstamp.real") == null)) // or no starting information
                || ((tupletSpan.getAttribute("dur") == null) && (tupletSpan.getAttribute("dur.ges") == null) && (tupletSpan.getAttribute("endid") == null))  // or no ending information
                || (tupletSpan.getAttribute("num") == null) || (tupletSpan.getAttribute("numbase") == null)){   // and no num or numbase attribute
            return;                                                                                                                                                                                                     // cancel
        }

        // make a clone of the element and store its tick date
        Element clone = Helper.cloneElement(tupletSpan);
        clone.addAttribute(new Attribute("date", this.helper.currentPart.getAttributeValue("currentDate")));

        // compute duration if already possible (if a du or dur.ges attribute is given) and set the end attribute accordingly
        double dur = this.helper.computeDuration(tupletSpan);                               // compute duration
        if (dur > 0.0) {                                                                    // if success
            clone.addAttribute(new Attribute("end", Double.toString(this.helper.getMidiTime() + dur))); // compute end date of the transposition and store in attribute end
        }

        // add element to the local miscMap/tupletSpanMap; during duration computation (helper.computeDuration()) this map is scanned for applicable entries
        this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getFirstChildElement("tupletSpanMap").appendChild(clone);
    }

    /** process an mei reh element (rehearsal mark)
     *
     * @param reh an mei reh element
     */
    private void processReh(Element reh) {
        // global or local?
        Element markerMap = (this.helper.currentPart == null) ? null : this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("markerMap");                                     // choose local markerMap
        if (markerMap == null)                                                                                                                                                                      // if outside a local scope
            markerMap = (this.helper.currentMovement == null) ? null : this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("markerMap");  // choose global markerMap
        if (markerMap == null)                                                                                                                                                                      // if outside a movement scope
            return;                                                                                                                                                                                 // that marker cannot be put anywere, cancel

        // create marker element
        Element marker = new Element("element");
        Helper.copyId(reh, marker);                                                                 // copy a possibly present xml:id
        marker.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));     // store the date of the element
        marker.addAttribute(new Attribute("message", reh.getValue()));                              // store its text or empty string
        Helper.copyId(reh, marker);                                                                 // copy the id

        markerMap.appendChild(marker);      // add to the markerMap
    }

    /** process an mei beatRpt element
     *
     * @param beatRpt an mei beatRpt element
     */
    private void processBeatRpt(Element beatRpt) {
        // get the value of one beat from the local or global timeSignatureMap
        Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element");
        if (es.size() == 0) {                                                                                                       // if local map empty
            es = this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element"); // get global entries
        }

        double beatLength = (es.size() == 0) ? 4 : Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator"));        // store the denominator value; if still no time signature information, one beat is 1/4 by default
        beatLength = (4.0 * this.helper.ppq) / beatLength;                                                                          // compute the length of one beat in midi ticks

        this.processRepeat(beatLength);
    }

    /** process an mei mRpt elemnet
     *
     * @param mRpt an mei mRpt elemnet
     */
    private void processMRpt(Element mRpt) {
        this.processRepeat(this.helper.getOneMeasureLength());
    }


    /** process an mei mRpt2 element
     *
     * @param mRpt2 an mei mRpt2 element
     */
    private void processMRpt2(Element mRpt2) {
        double timeframe = this.helper.getOneMeasureLength();

        // get the value of one measure from the local or global timeSignatureMap
        Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element");
        if (es.size() == 0) {                                                       // if local map empty
            es = this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element"); // get global entries
        }

        // check the timeSignatureMap for time signature changes between this and the previous measure
        if (es.size() != 0) {                                                       // this check is only possible if there is time signature information
            if ((this.helper.getMidiTime() - (2.0 * timeframe)) < (Double.parseDouble(es.get(es.size()-1).getAttributeValue("date")))) {    // if the last time signature element is within the timeframe
                Element second = Helper.cloneElement(es.get(es.size()-1));          // get the last time signature element
                Element first;
                if (es.size() < 2) {                                                // if no second to last time signature element exists
                    first = new Element("element");                                 // create one with default time signature 4/4
                    first.addAttribute(new Attribute("numerator", "4"));
                    first.addAttribute(new Attribute("denominator", "4"));
                }
                else {                                                              // otherwise
                    first = Helper.cloneElement(es.get(es.size() - 2));             // get the second to last time signature element
                }
                first.addAttribute(new Attribute("date", this.helper.currentPart.getAttributeValue("currentDate")));  // update date of first  to currentDate

                // set date of the last time signature element to the beginning of currentDate + 1 measure
                double timeframe2 = (4.0 * this.helper.ppq * Integer.parseInt(first.getAttributeValue("numerator"))) / Integer.parseInt(first.getAttributeValue("denominator"));    // compute the length of one measure of time signature element first
                second.getAttribute("date").setValue(Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + timeframe2));                   // update date of second time signature element

                // add both instructions to the timeSignatureMap
                es.get(0).getParent().appendChild(first);
                es.get(0).getParent().appendChild(second);

                timeframe += timeframe2;
            }
        }

        this.processRepeat(timeframe);
    }

    /** process an mei multiRpt element
     *
     * @param multiRpt an mei multiRpt element
     */
    private void processMultiRpt(Element multiRpt) {
        double timeframe = 0;                                                                   // here comes the length of the timeframe to be repeated
        double currentDate = this.helper.getMidiTime();
        double measureLength = currentDate - this.helper.getOneMeasureLength();                 // length of one measure in ticks

        // get time signature element
        Elements ts = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element");
        if (ts.size() == 0)                                                                                                                                                         // if local map empty
            ts = this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element");     // get global entries
        int timesign = ts.size() - 1;                                                                                                                                               // get index of the last element in ts
        double tsdate = (timesign > 0) ? Double.parseDouble(ts.get(timesign).getAttributeValue("date")) : 0.0;                                                                      // get the date of the current time signature

        // go back measure-wise, check for time signature changes, sum up the measure lengths to variable timeframe
        for (int measureCount = (multiRpt.getAttribute("num") == null) ? 1 : Integer.parseInt(multiRpt.getAttributeValue("num")); measureCount > 0; --measureCount) {        // for each measure
            timeframe += measureLength;                                                                                         // add its length to the timeframe for repetition
            while (tsdate >= (currentDate - timeframe)) {                                                                       // if we pass the date of the current time signature (and maybe others, too)
                --timesign;                                                                                                     // choose predecessor in the ts list
                tsdate = ((timesign) > 0) ? Double.parseDouble(ts.get(timesign).getAttributeValue("date")) : 0.0;               // get its date
                measureLength = ((timesign) > 0) ? this.helper.computeMeasureLength(Integer.parseInt(ts.get(timesign).getAttributeValue("numerator")), Integer.parseInt(ts.get(timesign).getAttributeValue("denominator"))) : this.helper.computeMeasureLength(4, 4);   // update measureLength
            }
        }

        // copy the time signature elements we just passed and append them to the timeSignatureMap
        if (ts.size() != 0) {
            Element tsMap = (Element)ts.get(0).getParent();                                     // get the map
            for(++timesign; timesign < ts.size(); ++timesign) {                                 // go through all time signature elements we just passed
                Element clone = Helper.cloneElement(ts.get(timesign));                          // clone the element
                clone.getAttribute("date").setValue(Double.toString(Double.parseDouble(clone.getAttributeValue("date")) + timeframe));  // update its date
                tsMap.appendChild(clone);
            }
        }

        this.processRepeat(timeframe);
    }

    /** process an mei halfmRpt element
     *
     * @param halfmRpt an mei halfmRpt element
     */
    private void processHalfmRpt(Element halfmRpt) {
        this.processRepeat(0.5 * this.helper.getOneMeasureLength());
    }

    /** repeats the material at the end of the score map, attribute timeframe specifies the length of the frame to be repeatetd (in midi ticks)
     *
     * @param timeframe the timeframe to be repeated in midi ticks
     */
    private void processRepeat(double timeframe) {
        if ((this.helper.currentPart == null)                                                                                       // if no part
        || (this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").getChildElements().size() == 0)) {  // or no music data
            return;                                                                                                                 // nothing to repeat, hence, cancel
        }

        double currentDate = Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate"));
        double datePrevBeat = currentDate - timeframe;
        Stack<Element> els = new Stack<Element>();

        // go back in the score map, copy all elements with date at and after the last beat, recalculate the date (date += beat value)
        for (Element e = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").getChildElements().get(this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").getChildElements().size()-1); e != null; e = Helper.getPreviousSiblingElement(e)) {
            double date = Double.parseDouble(e.getAttributeValue("date"));                                                          // get date of the element
            if (date < datePrevBeat)                                                                                                // if all elements from the previous beat were collected
                break;                                                                                                              // break the for loop
            els.push(Helper.cloneElement(e)).getAttribute("date").setValue(Double.toString(date + timeframe));                      // make a new element, push onto the els stack, and update its date value
        }

        // append the elements in the els stack to the score map
        for (; !els.empty(); els.pop()) {
            this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").appendChild(els.peek());            // append element to score and pop from stack
        }

        this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(currentDate + timeframe));                     // update currentDate counter
    }


    /** process a complete measure rest in mei, the measure rest MUST be in a staff/layer environment!
     *
     * @param mRest an mei mRest element
     */
    private void processMeasureRest(Element mRest) {
        if (this.helper.currentPart == null) return;                                                    // if we are not within a part, we don't know where to assign the rest; hence we skip its processing

        Element rest = this.makeMeasureRest(mRest);                                                     // make rest element

        if (rest == null)                                                                               // if failed
            return;

        this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").appendChild(rest);                          // insert in movement
        this.helper.currentPart.addAttribute(new Attribute("currentDate", Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + Double.parseDouble(rest.getAttributeValue("dur")))));  // update currentDate
    }

    /** make a rest that lasts a complete measure
     *
     * @param meiMRest an mei measureRest element
     * @return an msm rest element
     */
    private Element makeMeasureRest(Element meiMRest) {
        Element rest = new Element("rest");                             // this is the new rest element
        Helper.copyId(meiMRest, rest);                                   // copy the id
        double dur = 0.0;                                               // its duration

        // compute duration
        if ((this.helper.currentPart != null) && (this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("element") != null)) {    // if there is a local time signature map that is not empty
            Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element");
            dur = (4.0 * this.helper.ppq * Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator"))) / Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator"));
        }
        else if (this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("element") != null) {   // if there is a global time signature map
            Elements es = this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("element");
            dur = (4.0 * this.helper.ppq * Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator"))) / Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator"));
        }
        if (dur == 0.0) {                                               // if duration could not be computed
            return null;                                                // cancel
        }

        rest.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));       // compute date
        rest.addAttribute(new Attribute("dur", Double.toString(dur)));                              // store in rest element

        return rest;
    }

    /** make a rest that lasts several measures and insert it into the score
     *
     * @param multiRest an mei multiRest element
     */
    private void processMultiRest(Element multiRest) {
        if (this.helper.currentPart == null) return;                                        // if we are not within a part, we don't know where to assign the rest; hence we skip its processing

        Element rest = this.makeMeasureRest(multiRest);                                     // generate a one measure rest
        if (rest == null) return;                                                           // if failed to create a rest, cancel

        rest.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));   // compute date
        this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").appendChild(rest);  // insert the rest into the score

        int num = (multiRest.getAttribute("num") == null) ? 1 : Integer.parseInt(multiRest.getAttributeValue("num"));
        if (num > 1)                                                                        // if multiple measures (more than 1)
            rest.getAttribute("dur").setValue(Double.toString(Double.parseDouble(rest.getAttributeValue("dur")) * num));    // rest duration of one measure times the number of measures

        this.helper.currentPart.addAttribute(new Attribute("currentDate", Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + Double.parseDouble(rest.getAttributeValue("dur")))));  // update currentDate counter
    }

    /** process an mei rest element
     *
     * @param rest an mei rest element
     */
    private void processRest(Element rest) {
        Element s = new Element("rest");                                                    // this is the new rest element
        Helper.copyId(rest, s);                                                             // copy the id
        s.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));  // compute date

        double dur = this.helper.computeDuration(rest);                                     // compute note duration in midi ticks
        if (dur == 0.0) return;                                                             // if failed, cancel

        s.addAttribute(new Attribute("dur", Double.toString(dur)));                         // else store attribute
        this.helper.currentPart.addAttribute(new Attribute("currentDate", Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + dur)));    // update currentDate counter
        this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").appendChild(s); // insert the new note into the part->dated->score

        // this is just for the debugging in mei
        rest.addAttribute(new Attribute("date.midi", s.getAttributeValue("date")));
        rest.addAttribute(new Attribute("dur.midi", s.getAttributeValue("dur")));
    }

    /** process an mei octave element; this method does not process tstamp and tstamp2 or tstamp.ges or tstamp.real; there MUST be a dur, dur.ges or endid attribute
     *
     * @param octave an mei octave element
     */
    private void processOctave(Element octave) {
        if ((octave.getAttribute("dis") == null)
                || (octave.getAttribute("dis.place") == null)                                                                                                          // if no transposition information
//                || ((octave.getAttribute("startid") == null) && (octave.getAttribute("tstamp") == null) && (octave.getAttribute("tstamp.ges") == null) && (octave.getAttribute("tstamp.real") == null)) // or no starting information
                || ((octave.getAttribute("dur") == null) /*&& (octave.getAttribute("dur.ges") == null) */
                && (octave.getAttribute("endid") == null) /*&& (octave.getAttribute("tstamp2") == null)*/)) {          // or no ending information
            return;         // cancel because of insufficient information
        }

        // compute the amount of transposition in semitones
        int result;
        if (octave.getAttributeValue("dis").equals("8")) {
            result = 12;

        } else if (octave.getAttributeValue("dis").equals("15")) {
            result = 24;

        } else if (octave.getAttributeValue("dis").equals("22")) {
            result = 36;

        } else {
            System.out.println("An invalid octave transposition occured (dis=" + octave.getAttributeValue("dis") + ").");
            return;
        }

        // direction of transposition
        if (octave.getAttributeValue("dis.place").equals("below")) {
            result = -result;
        }
        else if (!octave.getAttributeValue("dis.place").equals("above")){
            System.out.println("An invalid octave transposition occured (dis.place=" + octave.getAttributeValue("dis.place") + ").");
            return;
        }

        Element s = new Element("addTransposition");                                        // create an addTransposition element (it adds to other transpositions, e.g. from the staffDef or scoreDef)
        Helper.copyId(octave, s);                                                           // copy the id
        s.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));  // compute starting date of transposition
        s.addAttribute(new Attribute("semi", Integer.toString(result)));                    // write the semitone transposition into the element

        // compute duration or store endid for later reference
        double dur = this.helper.computeDuration(octave);                                   // compute duration
        if (dur > 0.0) {                                                                    // if success
            s.addAttribute(new Attribute("end", Double.toString(this.helper.getMidiTime() + dur))); // compute end date of the transposition and store in attribute end
        }
        else {                                                                              // duration computation failed
            s.addAttribute(new Attribute("endid", octave.getAttributeValue("endid")));      // store endid for later reference
            this.helper.endids.add(s);                                                      // and append element to the endids list
        }

        // insert in local or global miscMap
        if (this.helper.currentPart == null) {                                              // if global information
            this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(s);    // insert in global miscMap
            return;
        }
        this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(s);   // otherwise local information: insert in the part's miscMap
    }

    /** process an mei pedal element
     *
     * @param pedal an mei pedal element
     */
    private void processPedal(Element pedal) {
        if ((pedal.getAttribute("dir") == null)                                                                                                                                                         // if no pedal information
//                || ((pedal.getAttribute("startid") == null) && (pedal.getAttribute("tstamp") == null) && (pedal.getAttribute("tstamp.ges") == null) && (pedal.getAttribute("tstamp.real") == null))
                || (pedal.getAttribute("endid") == null)
                ) {  // or no starting information
            return;         // cancel because of insufficient information
        }

        Element s = new Element("pedal");                                                               // create pedal element
        Helper.copyId(pedal, s);                                                                        // copy the id
        s.addAttribute(new Attribute("date", Double.toString(this.helper.getMidiTime())));              // compute starting of the pedal
        s.addAttribute(new Attribute("state", pedal.getAttributeValue("dir")));                         // pedal state can be "down", "up", "half", and "bounce" (release then immediately depress the pedal)

        s.addAttribute(new Attribute("endid", pedal.getAttributeValue("endid")));                       // store endid for later reference
        this.helper.endids.add(s);                                                                      // and append element to the endids list


        // make an entry in the global or local miscMap from which later on midi ctrl events can be generated
        if (this.helper.currentPart == null) {                                                          // if global information
            this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(s);    // insert in global miscMap
            return;
        }
        this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(s);   // otherwise local information: insert in the part's miscMap
    }

    /** process an mei note element
     *
     * @param note an mei note element
     */
    private void processNote(Element note) {
        if (this.helper.currentPart == null) return;                            // if we are not within a part, we don't know where to assign the note; hence we skip its processing

        if (note.getAttribute("grace") != null) {                               // TODO: grace notes are relevant to expressive performance and need to be handled individually
            return;
        }

        double date = this.helper.getMidiTime();

        Element s = new Element("note");                                        // create a note element
        Helper.copyId(note, s);                                                 // copy the id
        s.addAttribute(new Attribute("date", Double.toString(date)));  // compute the date of the note

        // compute midi pitch
        ArrayList<String> pitchdata = new ArrayList<String>();                  // this is to store pitchname, accidentals and octave as additional attributes of the note
        double pitch = this.helper.computePitch(note, pitchdata);               // compute pitch of the note
        if (pitch == -1) return;                                                // if failed, cancel
        s.addAttribute(new Attribute("pitch", Double.toString(pitch)));         // store resulting pitch in the note
        s.addAttribute(new Attribute("pitchname", pitchdata.get(0)));           // store pitchname as additional attribute
        s.addAttribute(new Attribute("accidentals", pitchdata.get(1)));         // store accidentals as additional attribute
        s.addAttribute(new Attribute("octave", pitchdata.get(2)));              // store octave as additional attribute

        // compute midi duration
        double dur = this.helper.computeDuration(note);                         // compute note duration in midi ticks
        if (dur == 0.0) return;                                                 // if failed, cancel
        s.addAttribute(new Attribute("dur", Double.toString(dur)));

        // update currentDate counter
        if (this.helper.currentChord == null)                                   // the next instruction must be suppressed in the chord environment
            this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(date + dur));  // update currentDate counter

        //adding some attributes to the mei source, this is only for the debugging in mei
        note.addAttribute(new Attribute("date.midi", String.valueOf(date)));
        note.addAttribute(new Attribute("dur.midi", String.valueOf(dur)));

        // handle ties
        char tie = 'n';                                                         // what kind of tie is it? i: initial, m: medial, t: terminal, n: nothing
        if (note.getAttribute("tie") != null) {                                 // if the note has a tie attribute
            tie = note.getAttributeValue("tie").charAt(0);                      // get its value (first character of the array, it hopefully has only one character!)
        }
        else if ((this.helper.currentChord != null) && (this.helper.currentChord.getAttribute("tie") != null)) {    // or if the chord environment has a tie attribute
            tie = this.helper.currentChord.getAttributeValue("tie").charAt(0);  // get its value (first character of the array, it hopefully has only one character!)
        }

        switch (tie) {
            case 'n':
                break;
            case 'i':
                s.addAttribute(new Attribute("tie", "true"));                   // indicate that this notes is tied to its successor (with same pitch)
                break;
            case 'm':
            case 't':
                Nodes ps = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").query("descendant::*[local-name()='note' and @tie]");    // select all preceding msm notes with a tie attribute
                for (int i = ps.size() - 1; i >= 0; --i) {                                                                                                               // check each of them
                    Element p = ((Element) ps.get(i));
                    if (p.getAttributeValue("pitch").equals(s.getAttributeValue("pitch"))                                                                               // if the pitch is equal
                            && ((Double.parseDouble(p.getAttributeValue("date")) + Double.parseDouble(p.getAttributeValue("dur"))) == date)                             // and the tie note and this note are next to each other (there is zero time between them and they do not overlap)
                            ) {
                        p.addAttribute(new Attribute("dur", Double.toString(Double.parseDouble(p.getAttributeValue("dur")) + dur)));    // add this duration to the preceeding note with the same pitch
                        if (tie == 't')                                         // terminal tie
                            p.removeAttribute(p.getAttribute("tie"));           // delete tie attribute
                        return;                                                 // this note is not to be stored in the score, it only extends its predecessor; remark: if no fitting note is found, this note will be stored in the score map because this line is not reached
                    }
                }
        }

        this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").appendChild(s);     // insert the new note into the part->dated->score
    }

    /** this function can be used by the application to determine the minimal time resolution (pulses per quarternote) required to represent the shortest note value (found in mei, can go down to 2048) in midi; tuplets are not considered
     *
     * @return the minimal required time resolution to represent the shortest duration in this mei
     */
    public int computeMinimalPPQ() {
        Element e = this.getMusic();                                            // get the music element
        if (e == null) return 0;                                                // none found, no music, return 0

        // traverse the mei tree, starting at the music element, and find the shortest duration (greatest value of dur/dur.ges attribute)
//        Nodes durs = e.query(".//*[@dur]");                                     // get all nodes that have a dur attribute
        Nodes durs = e.query("descendant::*[attribute::dur]");                  // get all nodes that have a dur attribute
        double dur = 4.0;                                                       // initial value is "long"
        for (int i = durs.size()-1; i >= 0; --i) {
            double d = (((Element) durs.get(i)).getAttribute("dur") != null) ? Helper.duration2decimal(((Element) durs.get(i)).getAttributeValue("dur")) : 4.0;  // get the dur value
            int dots = (((Element)durs.get(i)).getAttribute("dots") != null) ? Integer.parseInt(((Element)durs.get(i)).getAttributeValue("dots")) : 0;          // dotted values require the prcision to be doubled
            for (; dots > 0; --dots)                                            // for each dot; variable d holds what has to be added to the dur value
                d /= 2;                                                         // half d
            if (dur > d) dur = d;
        }

        double result = 0.25 / dur;                                             // this is the result, how much ticks are the minimum required to represent the shortest note value in the mei

        if (result < 1)                                                         // if the shortest note value is longer than 1/4
            return 1;

        if ((result - (int)result) != 0)                                        // if result is non-integer
            return (int)result + 1;

        return (int)result;                                                     // else return the int cast of the result
    }

    /** the slacker attribute copyof may occur in the mei document and needs to be resolved before starting the conversion;
     * this method replaces elements with the copyof attribute by copies of the referred elements;
     * it may also be used to expand an mei document and free it from copyofs;
     *
     * @return null (no document loaded), an ArrayList with those ids that could not be resolved, or an empty ArrayList if everything went well
     */
    public ArrayList<String> resolveCopyofs() {
        Element e = this.getRootElement();
        if (e == null) return null;

        Nodes placeholders = e.query("descendant::*[attribute::copyof]");               // get all nodes that have a copyof attribute
        ArrayList<String> notfound = new ArrayList<String>();                           // store those references that cannot be found

        // resolve the copyofs
        for (int i = placeholders.size()-1; i >= 0; --i) {                              // go through all elements with copyof attribute
            String ref = ((Element) placeholders.get(i)).getAttributeValue("copyof");   // get the copyof string

            if (ref.charAt(0) == '#')                                                   // local references within the document should start with #
                ref = ref.substring(1);                                                 // remove the # from the string to get the pure id

            Node source;
            try {
                source = e.query("//*[@xml:id='" + ref + "']").get(0);                  // find the first source element with the id
            }
            catch (XPathException error) {                                              // something went wrong with the XPath query
                error.printStackTrace();                                                // print error to console
                continue;
            }
            catch (IndexOutOfBoundsException error) {                                   // the get(0) call didn't work because no source could be found
                notfound.add(ref);                                                      // append the id string that was not found
                continue;
            }

            // replace the placeholder with the deep copy of the source
            Node copy = source.copy();                                                          // make a deep copy of the node
            try {
                placeholders.get(i).getParent().replaceChild(placeholders.get(i), copy);        // replace the placeholder by it
            }
            catch (NoSuchChildException error) {   // if something went wrong, I don't know why as none of these exceptions should occur, just to be sure
                error.printStackTrace();                                                        // print error
                notfound.add(ref);                                                              // append the id string that was not found
            }
            catch (NullPointerException error) {   // if something went wrong, I don't know why as none of these exceptions should occur, just to be sure
                error.printStackTrace();                                                        // print error
                notfound.add(ref);                                                              // append the id string that was not found
            }
            catch (IllegalAddException error) {   // if something went wrong, I don't know why as none of these exceptions should occur, just to be sure
                error.printStackTrace();                                                        // print error
                notfound.add(ref);                                                              // append the id string that was not found
            }

            // generate new ids for those elements with a copied id
            Nodes ids = copy.query("descendant-or-self::*[@xml:id]");                           // get all the nodes with an xml:id attribute
            for (int j = 0; j < ids.size(); ++j) {                                              // go through all the nodes
                Element idElement = (Element) ids.get(j);
                String uuid = idElement.getAttributeValue("id", "http://www.w3.org/XML/1998/namespace") + ":" + UUID.randomUUID().toString();   // generate new ids for them
                idElement.getAttribute("id", "http://www.w3.org/XML/1998/namespace").setValue(uuid);    // and write into the attribute
            }
            System.out.print("\rResolving copyofs " + i);
        }
        System.out.print("\r");

        return notfound;
    }

    /** this function tries to put some malplaced elements at the right place in the timeline (e.g. tupletSpans at the end of a measure are placed before their startid element)
     *
     * @return null (no document loaded), an ArrayList with those ids that could not be reordered, or an empty ArrayList if everything went well without reordering
     */
    public ArrayList<String> reorderElements() {
        Element e = this.getRootElement();
        if (e == null) return null;

        Nodes ns = e.query("descendant::*[attribute::startid]");                        // get all nodes that have a startid attribute
        ArrayList<String> notfound = new ArrayList<String>();                           // store those references that cannot be found

        // replace all elemenets in ns
        // TODO: dynamics instructions are also often placed at the end of the measure and with a tstamp
        for (int i = ns.size()-1; i >= 0; --i) {                                        // go through all elements with startid attribute
            String ref = ((Element) ns.get(i)).getAttributeValue("startid");            // get the startid string

            if (ref.charAt(0) == '#')                                                   // local references within the document should start with #
                ref = ref.substring(1);                                                 // remove the # from the string to get the pure id

            // find the deepest node to start searching for the id (always starting at the root is inefficient)
            Element start;
            for (start = (Element)ns.get(i).getParent(); start.getLocalName().equals("mdiv"); start = (Element)start.getParent()) {  // search the parents until the mdiv
                if (start.getLocalName().equals("measure")) break;                      // if a measure was found start here
            }

            // move the node
            Node idNode;
            try {
                idNode = start.query("//*[@xml:id='" + ref + "']").get(0);              // find the first source element with the id
            }
            catch (XPathException error) {                                              // something went wrong with the XPath query
                error.printStackTrace();                                                // print error to console
                continue;
            }
            catch (IndexOutOfBoundsException error) {                                   // the get(0) call didn't work because no source could be found
                notfound.add(ref);                                                      // append the id string that was not found
                continue;
            }

            // insert a copy of this node before idNode
            ns.get(i).detach();
            idNode.getParent().insertChild(ns.get(i), idNode.getParent().indexOf(idNode));
            System.out.print("\rRestucturing mei " + i);
        }
        System.out.print("\r");

        return notfound;
    }

    /** this method adds ids to note, rest and chord elements in mei, as far as they do not have an id
     *
     * @return the generated ids count
     */
    public int addIds() {
        Element root = this.getRootElement();
        if (root == null) return 0;

        Nodes e = root.query("descendant::*[(local-name()='note' or local-name()='rest' or local-name()='mRest' or local-name()='multiRest' or local-name()='chord' or local-name()='tuplet' or local-name()='mdiv' or local-name()='reh') and not(@xml:id)]");
        for (int i = 0; i < e.size(); ++i) {                                    // go through all the nodes
            String uuid = UUID.randomUUID().toString();                         // generate new ids for them
            Attribute a = new Attribute("id", uuid);                            // create an attribute
            a.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");      // set its namespace to xml
            ((Element) e.get(i)).addAttribute(a);                               // add attribute to the node
        }


        return e.size();
    }
}