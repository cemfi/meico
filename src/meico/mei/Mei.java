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
import org.xml.sax.SAXException;

public class Mei {

    private File file = null;                                       // the source file
    private Document mei = null;                                    // the mei document
    private boolean validMei = false;                               // indicates whether the input file contained valid mei code (true) or not (false)
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
            this.validMei = false;
            System.out.println("No such file or directory: " + file.getPath());
            return;
        }

        // read file into the mei instance of Document
        if (validate)                                               // if the mei file should be validated
            this.validate();                                        // do so, the result is stored in this.validMei
        Builder builder = new Builder(false);                    // we leave the validate argument false as XOM's built-in validator does not support RELAX NG
//        this.validMei = true;                                  // the mei code is valid until validation fails (ValidityException)
        try {
            this.mei = builder.build(file);
        }
        catch (ValidityException e) {                               // in case of a ValidityException (no valid mei code)
//            this.validMei = false;                             // set validMei false to indicate that the mei code is not valid
//            e.printStackTrace();                                    // output exception message
//            for (int i=0; i < e.getErrorCount(); i++) {             // output all validity error descriptions
//                System.out.println(e.getValidityError(i));
//            }
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
        return this.validMei;
    }

    /**
     * validate the data loaded into this.file and return whether it is proper mei according to mei-CMN.rng
     * @return
     */
    public boolean validate() {
        if (this.isEmpty()) return false;       // no file, no validation

        this.validMei = true;                   // it is valid until the validator throws an exception

        try {
            Helper.validateAgainstSchema(this.file, this.getClass().getResource("/resources/mei-CMN.rng"));
//            Helper.validateAgainstSchema(this.file, new URL("http://www.music-encoding.org/schema/current/mei-CMN.rng"));     // this variant takes the schema from the web, the user has to be online for this!
        } catch (SAXException e) {              // invalid mei
            this.validMei = false;
            e.printStackTrace();                // print the full error message
//            System.out.println(e.getMessage()); // print only the validation error message, not the complete stackTrace
        } catch (IOException e) {               // missing rng file
            this.validMei = false;
//            e.printStackTrace();
            System.out.println("Validation failed: missing file /resources/mei-CMN.rng!");
        }
        System.out.println("Validation of " + this.file.getName() + " against mei-CMN.rng (meiversion 3.0.0 2016): " + this.validMei);  // command line output of the result
        return this.validMei;                   // return the result
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
            System.out.println("The specified pulses per quarternote resolution (ppq) is too coarse to capture the shortest duration values in the mei source. Using the minimal required resolution of " + ppq + " instead");
        }

//        long t = System.currentTimeMillis();
        this.resolveTieElements();                                              // first resolve the ties in case they are affected by the copyof resolution which comes next
        this.resolveCopyofs();
        this.reorderElements();                                                 // control elements (e.g. tupletSpan) are often not placed in the timeline but at the end of the measure, this must be resolved
//        System.out.println("Time consumed: " + (System.currentTimeMillis()-t));

        this.helper = new Helper(ppq);                                          // some variables and methods to make life easier
        this.helper.dontUseChannel10 = dontUseChannel10;                        // set the flag that says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on

        LinkedList<Msm> msms = new LinkedList<Msm>();                           // the list of Msm instances, each one is an mdiv in mei
        Elements bodies = this.getMusic().getChildElements("body", this.getMusic().getNamespaceURI());  // get the list of body elements in the mei source
        for (int b = 0; b < bodies.size(); ++b) {                               // for each body
            msms.addAll(this.convert(bodies.get(b)));                           // convert each body to msm and add the output list to msms
        }
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
            if (e.getLocalName().equals("accid")) {                         // process accid elements that are not children of notes
                this.processAccid(e);
                continue;
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
            } else if (e.getLocalName().equals("beam")) {
                // contains the notes to be beamed TODO: relevant for expressive performance
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
                this.processChord(e);                                       // bTrems are treated as chords
                continue;                                                   // continue with the next sibling
            } else if (e.getLocalName().equals("choice")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("chord")) {
                if (e.getAttribute("grace") != null)                        // TODO: at the moment we ignore grace notes and grace chords; later on, for expressive performances, we should handle these somehow
                    continue;
                this.processChord(e);
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
                this.processChord(e);                                       // fTrems are treated as chords
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
            } else if (e.getLocalName().equals("handShift")) {
                continue;                                                   // TODO: ignore
            } else if (e.getLocalName().equals("harm")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("harpPedal")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("incip")) {
                    continue;                                               // can be ignored
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
                double dur1 = Double.parseDouble(cm.getAttributeValue("midi.dur"));                                  // duration of the measure
                double dur2 = this.helper.getMidiTime() - Double.parseDouble(cm.getAttributeValue("midi.date"));     // duration of the measure's content (ideally it is equal to the measure duration, but could also be over- or underful)
                cm.getAttribute("midi.dur").setValue(Double.toString((dur1 >= dur2) ? dur1 : dur2));                 // take the longer duration as the measure's definite duration
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
            } else if (e.getLocalName().equals("parts")) {                  // just dive into it
            } else if (e.getLocalName().equals("part")) {                   // just dive into it
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
                continue;                                                   // tie are handled in the preprocessing, they can be ignored here
            } else if (e.getLocalName().equals("timeline")) {
                continue;                                                   // can be ignored
            } else if (e.getLocalName().equals("trill")) {
                continue;                                                   // TODO: relevant for expressive performance
            } else if (e.getLocalName().equals("tuplet")) {
                if (e.getAttribute("dur") != null) {
                    double cd = Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate"));   // store the current date for use afterwards
                    this.convert(e);                                        // process the child elements
                    double dur = this.helper.computeDuration(e);
                    this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(cd + dur));    // this compensates for numeric problems with the single note durations within the tuplet
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
            String uuid = "meico_" + UUID.randomUUID().toString();
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

        scoreDef.addAttribute(new Attribute("midi.date", Double.toString(helper.getMidiTime())));

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
            d.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));
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

        staffDef.addAttribute(new Attribute("midi.date", Double.toString(helper.getMidiTime())));

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
            d.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));
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
//            s.addAttribute(new Attribute("currentDate", (this.helper.currentMeasure != null) ? this.helper.currentMeasure.getAttributeValue("midi.date") : "0.0"));  // set currentDate of processing
            s.addAttribute(new Attribute("currentDate", Double.toString(this.helper.getMidiTime())));  // set currentDate of processing
            return s;                                                           // if that part entry was found, return it
        }

        // the part was not found, create one
        System.out.println("There is an undefined staff element in the score with no corresponding staffDef.\n" + staff.toXML() + "\nGenerating a new part for it.");  // output notification
        return this.makePart(staff);                                            // generate a part and return it
    }

    /** process an mei layerDef element
     *
     * @param layerDef an mei layerDef element
     */
    private void processLayerDef(Element layerDef) {
        layerDef.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));

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
        measure.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));    // set the measure's date

        // compute the duration of this measure
        double dur = 0.0;                                               // its duration

        if ((this.helper.currentPart != null) && (this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature") != null)) {    // if there is a local time signature map that is not empty
            Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
            dur = this.helper.computeMeasureLength(Integer.parseInt(es.get(es.size()-1).getAttributeValue("numerator")), Integer.parseInt(es.get(es.size()-1).getAttributeValue("denominator")));
        }
        else if (this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature") != null) {   // if there is a global time signature map
            Elements es = this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
            dur = this.helper.computeMeasureLength(Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator")), Integer.parseInt(es.get(es.size()-1).getAttributeValue("denominator")));
        }

        measure.addAttribute(new Attribute("midi.dur", Double.toString(dur)));
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

    /**
     * process accid elements that are not children of notes
     * @param accid an accid element
     */
    private void processAccid(Element accid) {
        if (accid.getAttribute("ploc") == null)                                         // If the equivalent to the note's pname attribute, here called ploc, is missing? These are the only ones that we process here to determine the pitch of an accidental.
            return;                                                                     // cancel

        Attribute accidAttribute = accid.getAttribute("accid.ges");                     // get the accid.ges attribute
        if (accidAttribute == null)                                                     // if there is none
            accidAttribute = accid.getAttribute("accid");                               // get the accid attribute
        if (accidAttribute == null)                                                     // if also missing
            return;                                                                     // not enough information to process it, cancel

        // make the accid compatible to note elements (ploc -> pname, oloc -> oct) so it can be added to the helper.accid list and processed in the same way as the notes in there, see method Helper.computePitch()
        accid.addAttribute(new Attribute("pname", accid.getAttributeValue("ploc")));    // store the ploc value in the pname attribute
        if (accid.getAttribute("oloc") != null)                                         // if there is the equivalent to the oct (octave transposition) attribute in notes
            accid.addAttribute(new Attribute("oct", accid.getAttributeValue("oloc")));  // store it in an attribute named oct

        this.helper.accid.add(accid);
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
        else {                                                                                                 // otherwise generate an id
            String id = "meico_" + UUID.randomUUID().toString();                                               // ids of generated parts start with UUID
            staffDef.addAttribute(new Attribute("n", id));
            part.addAttribute(new Attribute("number", id));
        }

        {
            Elements ps = this.helper.currentMovement.getChildElements("part");
            if (ps.size() == 0) {
                part.addAttribute(new Attribute("midi.channel", "0"));                                              // set midi channel
                part.addAttribute(new Attribute("midi.port", "0"));                                                 // set midi port
            }
            else {
                Element p = ps.get(ps.size()-1);                                                            // choose last part entry
                int channel = (Integer.parseInt(p.getAttributeValue("midi.channel")) + 1) % 16;                  // increment channel counter mod 16
                if ((channel == 9) && this.helper.dontUseChannel10)                                                    // if the drum channel should be avoided
                    ++channel;                                                                                  // do so
                int port = (channel == 0) ? (Integer.parseInt(p.getAttributeValue("midi.port")) + 1) % 256 : Integer.parseInt(p.getAttributeValue("midi.port"));	// increment port counter if channels of previous port are full
                part.addAttribute(new Attribute("midi.channel", Integer.toString(channel)));                          // set midi channel
                part.addAttribute(new Attribute("midi.port", Integer.toString(port)));                                // set midi port
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
        part.addAttribute(new Attribute("currentDate", (this.helper.currentMeasure != null) ? this.helper.currentMeasure.getAttributeValue("midi.date") : "0.0"));    // set currentDate of processing

        this.helper.currentMovement.appendChild(part);                                                         // insert it into movement

        return part;
    }

    /** make a time signature entry from an mei scoreDef, staffDef or meterSig element and return it or return null if no sufficient information
     *
     * @param meiSource an mei scoreDef, staffDef or meterSig element
     * @return an msm element for the timeSignatureMap
     */
    private Element makeTimeSignature(Element meiSource) {
        Element s = new Element("timeSignature");                                                 // create an element
        Helper.copyId(meiSource, s);                                                        // copy the id

        // date of the element
        s.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));  // compute the date

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
        Element s = new Element("keySignature");                                                        // create an element
        Helper.copyId(meiSource, s);                                                                    // copy the id
        s.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));         // compute date

        LinkedList<Element> accidentals = new LinkedList<Element>();                                    // create an empty list which will be filled with the accidentals of this key signature

        String sig = "";                                                                                // indicates where the key lies in the circle of fifths, can also be "mixed"
//        String accid = "";                                                                              // one single accidental
//        String pname = "";                                                                              // a pitch name
        String mixed = "";                                                                              // the string value of a sig.mixed or key.sig.mixed attribute

        if (meiSource.getLocalName().equals("scoreDef") || meiSource.getLocalName().equals("staffDef")) {   // if meiSource is a scoreDef or staffDef
            // scoreDefs and staffDefs may also contain keySigs, but this will be processed when method convert() dives into them, here, we process only attributes that indicate a key signature
            // read the key signature related attributes
            if (meiSource.getAttribute("key.sig") != null)
                sig = meiSource.getAttributeValue("key.sig");
            else return null;                                                                           // no key.sig attribut means no key signature change, hence, skip
//            if (meiSource.getAttribute("key.accid") != null)
//                accid = meiSource.getAttributeValue("key.accid");
//            if (meiSource.getAttribute("key.pname") != null)
//                pname = meiSource.getAttributeValue("key.pname");
            if (meiSource.getAttribute("key.sig.mixed") != null)
                mixed = meiSource.getAttributeValue("key.sig.mixed");
        }
        else if (meiSource.getLocalName().equals("keySig")) {                                           // if it is a keySig element
            // read the key signature related attributes
            if (meiSource.getAttribute("sig") != null)                                                  // if this attribute is not present meico interprets it as C major and does not skip as it does above (for scoreDefs and staffDefs); and there may of course be some keyAccid children
                sig = meiSource.getAttributeValue("sig");
//            if (meiSource.getAttribute("accid") != null)
//                accid = meiSource.getAttributeValue("accid");
//            if (meiSource.getAttribute("pname") != null)
//                pname = meiSource.getAttributeValue("pname");
            if (meiSource.getAttribute("sig.mixed") != null)
                mixed = meiSource.getAttributeValue("sig.mixed");

            // process keyAccid children
            Elements accids = meiSource.getChildElements("keyAccid");                                   // get all keyAccid elements
            for (int i=0; i < accids.size(); ++i) {                                                     // go through all the keyAccid elements
                if ((accids.get(i).getAttribute("pname") == null)                                       // if there is no pitch name, we don't know where to apply the accidental
                        || (accids.get(i).getAttribute("accid") == null)) {                             // if there is no accid, there is no need for an accidental
                    System.out.println("The following keyAccid element requires a pname and accid attribute for processing in meico: " + accids.get(i).toXML());
                    continue;                                                                           // skip this keyAccid element and continue with the next
                }
                double pitch = Helper.pname2midi(accids.get(i).getAttributeValue("pname"));             // get the pitch class that the accidental is applied to
                if (pitch < 0.0) {                                                                      // if invalid
                    System.out.println("No valid value in attribute pname: " + accids.get(i).toXML());  // error message
                    continue;                                                                           // continue with the next keyAccid
                }
                Element accidental = new Element("accidental");                                                                                         // create an accidental element for the msm keySignature
                accidental.addAttribute(new Attribute("pitch", Double.toString(pitch)));                                                                // add the pitch attribute that says which pitch class is affected by the accidental
                accidental.addAttribute(new Attribute("value", Double.toString(Helper.accidString2decimal(accids.get(i).getAttributeValue("accid"))))); // add the decimal value of the accidental as attribute (+1=sharp, -1=flat, and so on)
                accidentals.add(accidental);                                                                                                            // add it to the accidentals list
            }
        }

        // process sig, accid, pname and mixed to generate msm accidentals from them
        if (accidentals.isEmpty() && !sig.isEmpty()) {                                                  // if the meiSource is a keySig element and had keyAccid children, these overrule the attributes and, hence, the attributes will not be processed, this part will be skipped; same if there is no signature data
            if (sig.equals("mixed")) {                                                                  // process an unorthodox key signature, e.g. "a4 c5ss e5f"
                if (!mixed.isEmpty()) {                                                                 // is there something standing in the mixed string
                    String[] acs = mixed.split(" ");                                                    // split the space separated mixed string into an array of single strings
                    for (String ac : acs) {                                                             // for each accidental string extracted from the mixed string
                        double pitch = Helper.pname2midi(ac.substring(0, 1));                           // the first character designates the pitch to be affected by the accidental
                        if (pitch < 0.0)                                                                // if there is no valide pitch character
                            continue;                                                                   // skip this substring and continue with the next

                        if (ac.charAt(ac.length()-1) >= '0' && ac.charAt(ac.length()-2) <= '9')         // if the last character is a number, there is actually no accidental on this pitch
                            continue;                                                                   // hence, skip this and continue with the next

                        boolean secondLastIsDigit = (ac.charAt(ac.length()-2) >= '0' && ac.charAt(ac.length()-2) <= '9');       // is the second last character a number? if no the accidental is given by the final 2 chars, otherwise only by the last char
                        double accid = Helper.accidString2decimal(ac.substring(ac.length() - ((secondLastIsDigit) ? 1 : 2)));   // take the accid substring and convert it to decimal

                        Element accidental = new Element("accidental");                                 // create an accidental element for the msm keySignature
                        accidental.addAttribute(new Attribute("pitch", Double.toString(pitch)));        // add the pitch attribute that says which pitch class is affected by the accidental
                        accidental.addAttribute(new Attribute("value", Double.toString(accid)));        // add the decimal value of the accidental as attribute (+1=sharp, -1=flat, and so on)
                        accidentals.add(accidental);                                                    // add it to the accidentals list
                    }
                }
            }
            else {                                                                                      // process a regular key signature
                int accidCount;                                                                         // this variable holds how many accidentals
                switch (sig.charAt(sig.length()-1)) {                                                   // get the direction
                    case 'f':
                        accidCount = Integer.parseInt(sig.substring(0, sig.length()-1));                // get the accidentals count
                        accidCount *= -1;                                                               // flats are negative direction (see the sharps array below, with flats we start at the end and go back)
                        break;
                    case 's':
                        accidCount = Integer.parseInt(sig.substring(0, sig.length()-1));                // get the accidentals count
                        break;
                    case '0':
                        accidCount = 0;                                                                 // no accidentals, accidCount = 0
                        break;
                    default:
                        accidCount = 0;                                                                 // no accidentals that meico can understand
                        System.out.println("Unknown sig or key.sig attribute value in " + meiSource.toXML() + ". Assume 0 in the further processing.");     // output error message
                }
                // generate msm accidentals and add them to the accidentals list
                String[] acs = (accidCount > 0) ? new String[]{"5.0", "0.0", "7.0", "2.0", "9.0", "4.0", "11.0"} : new String[]{"11.0", "4.0", "9.0", "2.0", "7.0", "0.0", "5.0"};  // the sequence of (midi) pitches to apply the accidentals
                for (int i=0; i < Math.abs(accidCount); ++i) {                                           // create the accidentals
                    Element accidental = new Element("accidental");                                      // create an accidental element for the msm keySignature
                    accidental.addAttribute(new Attribute("pitch", acs[i]));                             // add the pitch attribute that says which pitch class is affected by the accidental
                    accidental.addAttribute(new Attribute("value", (accidCount > 0) ? "1.0" : "-1.0"));  // add the decimal value of the accidental as attribute (1=sharp, -1=flat)
                    accidentals.add(accidental);                                                         // add it to the accidentals list
                }
            }
        }

        // add all generated accidentals as children to the msm keySignature element
        for (Element accidental : accidentals) {                                                        // for each accidentals
            s.appendChild(accidental);                                                                  // add to the msm keySignature
        }

        return s;                                                                                       // return the msm keySignature element
    }

    /** process an mei chord element; this method is also used to process bTrem and fTrem elements
     *
     * @param chord an mei chord, bTrem or fTrem element
     */
    private void processChord(Element chord) {
        // inherit attributes of the surrounding environment
        if (this.helper.currentChord != null) {                                                                     // if we are already within a chord or bTrem or fTrem environment
            if ((chord.getAttribute("dur") == null) && (this.helper.currentChord.getAttribute("dur") != null)) {    // if duration attribute missing, but there is one in the environment
                chord.addAttribute(new Attribute("dur", this.helper.currentChord.getAttributeValue("dur")));        // take this
            }
            if ((chord.getAttribute("dots") == null) && (this.helper.currentChord.getAttribute("dots") != null)) {  // if dots attribute missing, but there is one in the environment
                chord.addAttribute(new Attribute("dots", this.helper.currentChord.getAttributeValue("dots")));      // take this
            }
        }

        // make sure that we have a duration for this chord
        double dur = 0.0;                                                   // this holds the duration of the chord
        if (chord.getAttribute("dur") != null) {                            // if the chord has a dur attribute
            dur = this.helper.computeDuration(chord);                       // compute its duration
        }
        else {                                                              // if the dur attribute is missing, TODO: search the children for the longest dur + dots attribute and add it to this element
            Nodes durs = chord.query("descendant::*[attribute::dur]");      // get all child elements with a dur attribute
            double idur = 0.0;
            for (int i=0; i < durs.size(); ++i) {                           // for each child element with a dur attribute
                idur = this.helper.computeDuration((Element)durs.get(i));   // compute its duration
                if (idur > dur) dur = idur;                                 // if it is longer than the longest duration so far, store this in variable dur
            }
        }

        Element f = this.helper.currentChord;                               // we could already be within a chord or bTrem or fTrem environemnt; this should be stored to return to it afterwards
        this.helper.currentChord = chord;                                   // set the temp.chord pointer to this chord
        this.convert(chord);                                                // process everything within this chord
        this.helper.currentChord = f;                                       // foget the pointer to this chord and return to the surrounding environment or nullptr
        if (this.helper.currentChord == null) {                             // we are done with all chord/bTrem/fTrem environments
            this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString((Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + dur))); // update currentDate
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
            return;                                                                                             // cancel
        }

        // make a clone of the element and store its tick date
        Element clone = Helper.cloneElement(tupletSpan);
        clone.addAttribute(new Attribute("midi.date", this.helper.currentPart.getAttributeValue("currentDate")));

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
        Element marker = new Element("marker");
        Helper.copyId(reh, marker);                                                                     // copy a possibly present xml:id
        marker.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));    // store the date of the element
        marker.addAttribute(new Attribute("message", reh.getValue()));                                  // store its text or empty string
        Helper.copyId(reh, marker);                                                                     // copy the id

        markerMap.appendChild(marker);      // add to the markerMap
    }

    /** process an mei beatRpt element
     *
     * @param beatRpt an mei beatRpt element
     */
    private void processBeatRpt(Element beatRpt) {
        // get the value of one beat from the local or global timeSignatureMap
        Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
        if (es.size() == 0) {                                                                                                       // if local map empty
            es = this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature"); // get global entries
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
        Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
        if (es.size() == 0) {                                                       // if local map empty
            es = this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature"); // get global entries
        }

        // check the timeSignatureMap for time signature changes between this and the previous measure
        if (es.size() != 0) {                                                       // this check is only possible if there is time signature information
            if ((this.helper.getMidiTime() - (2.0 * timeframe)) < (Double.parseDouble(es.get(es.size()-1).getAttributeValue("midi.date")))) {    // if the last time signature element is within the timeframe
                Element second = Helper.cloneElement(es.get(es.size()-1));          // get the last time signature element
                Element first;
                if (es.size() < 2) {                                                // if no second to last time signature element exists
                    first = new Element("timeSignature");                                 // create one with default time signature 4/4
                    first.addAttribute(new Attribute("numerator", "4"));
                    first.addAttribute(new Attribute("denominator", "4"));
                }
                else {                                                              // otherwise
                    first = Helper.cloneElement(es.get(es.size() - 2));             // get the second to last time signature element
                }
                first.addAttribute(new Attribute("midi.date", this.helper.currentPart.getAttributeValue("currentDate")));  // update date of first  to currentDate

                // set date of the last time signature element to the beginning of currentDate + 1 measure
                double timeframe2 = (4.0 * this.helper.ppq * Integer.parseInt(first.getAttributeValue("numerator"))) / Integer.parseInt(first.getAttributeValue("denominator"));    // compute the length of one measure of time signature element first
                second.getAttribute("midi.date").setValue(Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + timeframe2));                   // update date of second time signature element

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
        Elements ts = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
        if (ts.size() == 0)                                                                                                                                                         // if local map empty
            ts = this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");     // get global entries
        int timesign = ts.size() - 1;                                                                                                                                               // get index of the last element in ts
        double tsdate = (timesign > 0) ? Double.parseDouble(ts.get(timesign).getAttributeValue("midi.date")) : 0.0;                                                                      // get the date of the current time signature

        // go back measure-wise, check for time signature changes, sum up the measure lengths to variable timeframe
        for (int measureCount = (multiRpt.getAttribute("num") == null) ? 1 : Integer.parseInt(multiRpt.getAttributeValue("num")); measureCount > 0; --measureCount) {        // for each measure
            timeframe += measureLength;                                                                                         // add its length to the timeframe for repetition
            while (tsdate >= (currentDate - timeframe)) {                                                                       // if we pass the date of the current time signature (and maybe others, too)
                --timesign;                                                                                                     // choose predecessor in the ts list
                tsdate = ((timesign) > 0) ? Double.parseDouble(ts.get(timesign).getAttributeValue("midi.date")) : 0.0;               // get its date
                measureLength = ((timesign) > 0) ? this.helper.computeMeasureLength(Integer.parseInt(ts.get(timesign).getAttributeValue("numerator")), Integer.parseInt(ts.get(timesign).getAttributeValue("denominator"))) : this.helper.computeMeasureLength(4, 4);   // update measureLength
            }
        }

        // copy the time signature elements we just passed and append them to the timeSignatureMap
        if (ts.size() != 0) {
            Element tsMap = (Element)ts.get(0).getParent();                                     // get the map
            for(++timesign; timesign < ts.size(); ++timesign) {                                 // go through all time signature elements we just passed
                Element clone = Helper.cloneElement(ts.get(timesign));                          // clone the element
                clone.getAttribute("midi.date").setValue(Double.toString(Double.parseDouble(clone.getAttributeValue("midi.date")) + timeframe));  // update its date
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
            double date = Double.parseDouble(e.getAttributeValue("midi.date"));                                                          // get date of the element
            if (date < datePrevBeat)                                                                                                // if all elements from the previous beat were collected
                break;                                                                                                              // break the for loop
            els.push(Helper.cloneElement(e)).getAttribute("midi.date").setValue(Double.toString(date + timeframe));                      // make a new element, push onto the els stack, and update its date value
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
        this.helper.currentPart.addAttribute(new Attribute("currentDate", Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + Double.parseDouble(rest.getAttributeValue("midi.duration")))));  // update currentDate
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
        if ((this.helper.currentPart != null) && (this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature") != null)) {    // if there is a local time signature map that is not empty
            Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
            dur = (4.0 * this.helper.ppq * Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator"))) / Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator"));
        }
        else if (this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature") != null) {   // if there is a global time signature map
            Elements es = this.helper.currentMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
            dur = (4.0 * this.helper.ppq * Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator"))) / Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator"));
        }
        if (dur == 0.0) {                                               // if duration could not be computed
            return null;                                                // cancel
        }

        rest.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));       // compute date
        rest.addAttribute(new Attribute("midi.duration", Double.toString(dur)));                         // store in rest element

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

        rest.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));   // compute date
        this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").appendChild(rest);  // insert the rest into the score

        int num = (multiRest.getAttribute("num") == null) ? 1 : Integer.parseInt(multiRest.getAttributeValue("num"));
        if (num > 1)                                                                        // if multiple measures (more than 1)
            rest.getAttribute("midi.duration").setValue(Double.toString(Double.parseDouble(rest.getAttributeValue("midi.duration")) * num));    // rest duration of one measure times the number of measures

        this.helper.currentPart.addAttribute(new Attribute("currentDate", Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + Double.parseDouble(rest.getAttributeValue("midi.duration")))));  // update currentDate counter
    }

    /** process an mei rest element
     *
     * @param rest an mei rest element
     */
    private void processRest(Element rest) {
        Element s = new Element("rest");                                                    // this is the new rest element
        Helper.copyId(rest, s);                                                             // copy the id
        s.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));  // compute date

        double dur = this.helper.computeDuration(rest);                                     // compute note duration in midi ticks
        if (dur == 0.0) return;                                                             // if failed, cancel

        s.addAttribute(new Attribute("midi.duration", Double.toString(dur)));                    // else store attribute
        this.helper.currentPart.addAttribute(new Attribute("currentDate", Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + dur)));    // update currentDate counter
        this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").appendChild(s); // insert the new note into the part->dated->score

        // this is just for the debugging in mei
        rest.addAttribute(new Attribute("midi.date", s.getAttributeValue("midi.date")));
        rest.addAttribute(new Attribute("midi.dur", s.getAttributeValue("midi.duration")));
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
        s.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));  // compute starting date of transposition
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
        s.addAttribute(new Attribute("midi.date", Double.toString(this.helper.getMidiTime())));              // compute starting of the pedal
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
        s.addAttribute(new Attribute("midi.date", Double.toString(date)));      // compute the date of the note

        // compute midi pitch
        ArrayList<String> pitchdata = new ArrayList<String>();                  // this is to store pitchname, accidentals and octave as additional attributes of the note
        double pitch = this.helper.computePitch(note, pitchdata);               // compute pitch of the note
        if (pitch == -1) return;                                                // if failed, cancel
        s.addAttribute(new Attribute("midi.pitch", Double.toString(pitch)));         // store resulting pitch in the note
        s.addAttribute(new Attribute("pitchname", pitchdata.get(0)));           // store pitchname as additional attribute
        s.addAttribute(new Attribute("accidentals", pitchdata.get(1)));         // store accidentals as additional attribute
        s.addAttribute(new Attribute("octave", pitchdata.get(2)));              // store octave as additional attribute

        // compute midi duration
        double dur = this.helper.computeDuration(note);                         // compute note duration in midi ticks
        if (dur == 0.0) return;                                                 // if failed, cancel
        s.addAttribute(new Attribute("midi.duration", Double.toString(dur)));

        // update currentDate counter
        if (this.helper.currentChord == null)                                   // the next instruction must be suppressed in the chord environment
            this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(date + dur));  // update currentDate counter

        //adding some attributes to the mei source, this is only for the debugging in mei
        note.addAttribute(new Attribute("pnum", String.valueOf(pitch)));
        note.addAttribute(new Attribute("midi.date", String.valueOf(date)));
        note.addAttribute(new Attribute("midi.dur", String.valueOf(dur)));

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
            case 'i':                                                           // the tie starts here
                s.addAttribute(new Attribute("tie", "true"));                   // indicate that this notes is tied to its successor (with same pitch)
                break;
            case 'm':                                                           // intermedieate tie
            case 't':                                                           // the tie ends here
                Nodes ps = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").query("descendant::*[local-name()='note' and @tie]");    // select all preceding msm notes with a tie attribute
                for (int i = ps.size() - 1; i >= 0; --i) {                                                                                                              // check each of them
                    Element p = ((Element) ps.get(i));
                    if (p.getAttributeValue("midi.pitch").equals(s.getAttributeValue("midi.pitch"))                                                                               // if the pitch is equal
                            && ((Double.parseDouble(p.getAttributeValue("midi.date")) + Double.parseDouble(p.getAttributeValue("midi.duration"))) == date)                             // and the tie note and this note are next to each other (there is zero time between them and they do not overlap)
                            ) {
                        p.addAttribute(new Attribute("midi.duration", Double.toString(Double.parseDouble(p.getAttributeValue("midi.duration")) + dur)));                          // add this duration to the preceeding note with the same pitch
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
        Element e = this.getRootElement();                                                              // this also includes the meiHead section, not only the music section, as there might be reference from music into the head
        if (e == null) return null;

        ArrayList<String> notResolved = new ArrayList<String>();                                             // store those ids that are not resolved
        HashMap<Element, String> previousPlaceholders = new HashMap<Element, String>();                               // this is a copy of the placeholders hashmap in the while loop; if it does not change from one iteration to the next, there is a placeholder refering to another placeholder refering back to the first; this cannot be resolved and leads to an infinite loop; this hashmap here is to detect this situation

        System.out.print("Resolving copyofs:");

        while (true) {                                                                                  // this loop can only be exited if no placeholders are left (it is possible that multiple runs are necessary when placeholders are within placeholders)
            HashMap<String, Element> elements = new HashMap<String, Element>();                                       // this hashmap will be filled with elements and their ids
            HashMap<Element, String> placeholders = new HashMap<Element, String>();                                   // this hashmap will be filled with placeholder elements that have a copyof attribute and the id in the copyof

            Nodes all = e.query("descendant::*[attribute::copyof or attribute::xml:id]");               // get all elements with a copyof or xml:id attribute
            for (int i = 0; i < all.size(); ++i) {                                                      // for each of them
                Element element = (Element) all.get(i);                                                 // make an Element out of it

                Attribute a = element.getAttribute("copyof");                                           // get the copyof attribute, if there is one
                if (a != null) {                                                                        // if there is a copyof attribute
                    String copyof = a.getValue();                                                       // get its value
                    if (copyof.charAt(0) == '#') copyof = copyof.substring(1);                          // local references within the document usually start with #; this must be excluded when searching for the id
                    placeholders.put(element, copyof);                                                  // put that entry on the placeholder hashmap
                    //continue;                                                                         // this elemnt may also have an xml:id, so it must be added to the other list as well and we later on have the possibility to resolve references of placeholders to other placeholders
                }

                a = element.getAttribute("id", "http://www.w3.org/XML/1998/namespace");                 // get the element's xml:id
                if (a != null) {                                                                        // if it has one
                    elements.put(a.getValue(), element);                                                // put it on the elements hashmap
                }
            }

            if (placeholders.size() == 0) break;                                                        // we are done, this stops the while loop

            // detect placeholders that cannot be resolved but lead to infinite loops because of circular references
            if ((placeholders.values().containsAll(previousPlaceholders.values()))
                    && previousPlaceholders.values().containsAll(placeholders.values())) {              // if the same copyof references recur
                for (Map.Entry<Element, String> placeholder : placeholders.entrySet()) {
                    notResolved.add(placeholder.getKey().toXML());                                      // add all entries to the return list
                    placeholder.getKey().getParent().removeChild(placeholder.getKey());                 // delete all placeholders from the xml, we cannot resolve them anyway
                }
                System.out.print(" circular copyof referencing detected, cannot be resolved,");
                break;                                                                                  // stop the while loop
            }
            previousPlaceholders = placeholders;

            System.out.print(" " + placeholders.size() + " copyofs ...");

            // replace alle placeholders in the xml tree by copies of the source
            for (Map.Entry<Element, String> placeholder : placeholders.entrySet()) {                    // for each placeholder
                Element found = elements.get(placeholder.getValue());                                   // search the elements hashmap for the id

                if (found == null) {                                                                    // if no element with this id has been found
                    notResolved.add(placeholder.getKey().toXML());                                      // add entry to the return list
                    placeholder.getKey().getParent().removeChild(placeholder.getKey());                 // delete the placeholder from the xml, we cannot process it anyway
                    continue;                                                                           // continue with the next placeholder
                }

                // make the replacement
                Node copy = found.copy();                                                               // make a deep copy of the source

                try {
                    placeholder.getKey().getParent().replaceChild(placeholder.getKey(), copy);          // replace the placeholder by it
//                System.out.println("replacing: " + placeholder.getKey().toXML() + "\nby\n" + copy.toXML() + "\n\n");
                } catch (NoSuchChildException error) {                                                  // if something went wrong, I don't know why as none of these exceptions should occur, just to be sure
                    error.printStackTrace();                                                            // print error
                    notResolved.add(placeholder.getKey().toXML());                                      // add entry to the return list
                    continue;
                } catch (NullPointerException error) {                                                  // if something went wrong, I don't know why as none of these exceptions should occur, just to be sure
                    error.printStackTrace();                                                            // print error
                    notResolved.add(placeholder.getKey().toXML());                                      // add entry to the return list
                    continue;
                } catch (IllegalAddException error) {                                                   // if something went wrong, I don't know why as none of these exceptions should occur, just to be sure
                    error.printStackTrace();                                                            // print error
                    notResolved.add(placeholder.getKey().toXML());                                      // add entry to the return list
                    continue;
                }

                // generate new ids for those elements with a copied id
                Nodes ids = copy.query("descendant-or-self::*[@xml:id]");                                                   // get all the nodes with an xml:id attribute
                for (int j = 0; j < ids.size(); ++j) {                                                                      // go through all the nodes
                    Element idElement = (Element) ids.get(j);
                    String uuid = idElement.getAttributeValue("id", "http://www.w3.org/XML/1998/namespace") + "_meico_" + UUID.randomUUID().toString();   // generate new ids for them
                    idElement.getAttribute("id", "http://www.w3.org/XML/1998/namespace").setValue(uuid);                    // and write into the attribute
                }

                // but keep the possibly existing placeholder id for the copy's root node
                Attribute id = placeholder.getKey().getAttribute("id", "http://www.w3.org/XML/1998/namespace");             // get the placeholder's xml:id
                if (id != null) {                                                                                           // if the placeholder has one
                    ((Element) copy).getAttribute("id", "http://www.w3.org/XML/1998/namespace").setValue(id.getValue());     // set the copy's id to the id of the placeholder
                }
            }
        }

        System.out.println(" done");

        if (!notResolved.isEmpty())
            System.out.println("The following placeholders could not be resolved:\n" + notResolved.toString());

        return notResolved;
    }

    /**
     * this method recodes tie elements as tie attributes in the corresponing note elements;
     * therefore, the tie element MUST have a startid and an endid attribute; tstamp and staff alone do not generally suffice for an unambiguous resolution.
     *
     * @return null (no document loaded), an ArrayList with those tie elements that could not be resolved, or an empty ArrayList if everything went well
     */
    public ArrayList<String> resolveTieElements() {
        Element e = this.getMusic();
        if (e == null) return null;                                                                 // if there is no music, cancel

        ArrayList<String> notResolved = new ArrayList<String>();                                         // store those tie elements that are not resolved
        HashMap<String, Element> notes = new HashMap<String, Element>();                                          // this hashmap will be filled with notes and their ids
        ArrayList<Element> ties = new ArrayList<Element>();                                               // this list will be filled with tie elements that have startid and endid attributes

        System.out.print("Resolving tie elements:");

        Nodes tiesAndNotes = e.query("descendant::*[local-name()='tie' or local-name()='note']");   // get all note and tie elements
        for (int i = 0; i < tiesAndNotes.size(); ++i) {                                             // for each of them
            Element tn = (Element)tiesAndNotes.get(i);                                              // make an Element out of it
            if (tn.getLocalName().equals("note")) {                                                 // if it is a note
                Attribute id = tn.getAttribute("id", "http://www.w3.org/XML/1998/namespace");       // get its xml:id
                if (id != null) {                                                                   // if it has an xml:id
                    notes.put(id.getValue(), tn);                                                   // add it to the hashmap
                }
                continue;
            }
            if (tn.getLocalName().equals("tie")) {                                                  // if it is a tie element
                if ((tn.getAttribute("startid") == null) || (tn.getAttribute("endid") == null)) {   // if startid and/or endid are missing, no unambiguous assignment to a note element possible
                    notResolved.add(tn.toXML());                                                    // make an entry into the return list
                    tn.getParent().removeChild(tn);                                                 // delete the tie element from the xml, we cannot process it anyway
                    continue;
                }
                ties.add(tn);                                                                       // if the tie has a startid and endid, it is now added to the ties list
            }
        }

        System.out.print(" " + ties.size() + " elements ... ");

        // replace the tie elements by tie attributes in the notes
        for (Element tie : ties) {                                                  // for each tie element in the ties list
            String startid = tie.getAttributeValue("startid");
            String endid = tie.getAttributeValue("endid");
            if (startid.charAt(0) == '#') startid = startid.substring(1);           // local references within the document usually start with #; this must be excluded when searching for the id
            if (endid.charAt(0) == '#') endid = endid.substring(1);                 // local references within the document usually start with #; this must be excluded when searching for the id
            Element startNote = notes.get(startid);                                 // get the note with this tie's startid
            Element endNote = notes.get(endid);                                     // get the note with this tie's endid

            if ((startNote == null) || (endNote == null)) {                         // if no corresponding notes were found
                notResolved.add(tie.toXML());                                       // make an entry into the return list
                tie.getParent().removeChild(tie);                                   // delete the tie element from the xml, we cannot process it anyway
                continue;                                                           // continue with the next entry in ties
            }

            // add/edit tie attribute at the startid note
            Attribute a = startNote.getAttribute("tie");                            // get its tie attribute if it has one
            if (a != null) {                                                        // if the note has already a tie attribute
                if (a.getValue().equals("t"))                                       // but it says that the tie ends here
                    a.setValue("m");                                                // make an intermediate tie out of it
                else if (a.getValue().equals("n"))                                  // but it says "no tie"
                    a.setValue("i");                                                // make an initial tie out of it
            }
            else {                                                                  // otherwise the element had no tie attribute
                startNote.addAttribute(new Attribute("tie", "i"));                  // hence, we add an initial tie attribute
            }

            // add/edit tie attribute at the endid note
            a = endNote.getAttribute("tie");                                        // get its tie attribute if it has one
            if (a != null) {                                                        // if the note has already a tie attribute
                if (a.getValue().equals("i"))                                       // but it says that the tie is initial
                    a.setValue("m");                                                // make an intermediate tie out of it
                else if (a.getValue().equals("n"))                                  // but it says "no tie"
                    a.setValue("t");                                                // make a terminal tie out of it
            }
            else {                                                                  // otherwise the element had no tie attribute
                endNote.addAttribute(new Attribute("tie", "t"));                    // hence, we add an terminal tie attribute
            }

            tie.getParent().removeChild(tie);                                       // delete the tie element from the xml (all the other information are not needed any further)
        }

        System.out.println("done");

        if (!notResolved.isEmpty())
            System.out.println("The following ties could not be resolved:\n" + notResolved.toString());

        return notResolved;
    }

    /** this method tries to put some "malplaced" elements at the right place in the timeline (e.g., tupletSpans at the end of a measure are placed before their startid element);
     * this method works only with startids, tstamps are not resolved as it is impossible to resolve these during the preprocessing - this is left to the postprocessing
     *
     * @return null (no document loaded), an ArrayList with those ids that could not be reordered, or an empty ArrayList if everything went well without reordering
     */
    public ArrayList<String> reorderElements() {
        Element e = this.getRootElement();
        if (e == null) return null;

        ArrayList<String> notResolved = new ArrayList<String>();                                // store those elements that cannot be replaced because the startdid was not found
        HashMap<String, Element> elements = new HashMap<String, Element>();                              // this hashmap will be filled with elements and their ids
        HashMap<Element, String> shiftMe = new HashMap<Element, String>();                               // this hashmap will be filled with "malplaced" elements and their startids

        System.out.print("Restucturing mei:");

        Nodes all = e.query("descendant::*[attribute::startid or attribute::xml:id]");     // get all elements with a startid (potential candidate for replacement) or xml:id attribute
        for (int i = 0; i < all.size(); ++i) {                                             // for each of them
            Element element = (Element) all.get(i);                                        // make an Element out of it

            Attribute a = element.getAttribute("startid");                                  // get the startid attribute, if there is one
            if (a != null) {                                                                // if there is a startid attribute
                String startid = a.getValue();                                              // get its value
                if (startid.charAt(0) == '#') startid = startid.substring(1);               // local references within the document usually start with #; this must be excluded when searching for the id
                shiftMe.put(element, startid);                                              // put that entry on the shiftMe hashmap
                //continue;                                                                 // this element may also have an xml:id, so we go on
            }

            a = element.getAttribute("id", "http://www.w3.org/XML/1998/namespace");         // get the element's xml:id
            if (a != null) {                                                                // if it has one
                elements.put(a.getValue(), element);                                        // put it on the elements hashmap
            }
        }

        System.out.print(" " + shiftMe.size() + " elements for repositioning ...");
        // replace alle placeholders in the xml tree by copies of the source
        for (Map.Entry<Element, String> shiftThis : shiftMe.entrySet()) {                   // for each potential candidate for repositioning
            Element found = elements.get(shiftThis.getValue());                             // search the elements hashmap for the id

            if (found == null) {                                                            // if no element with this id has been found
                notResolved.add(shiftThis.getKey().toXML());                                // add entry to the return list
                continue;                                                                   // continue with the next candidate
            }

            if (Helper.getNextSiblingElement(shiftThis.getKey()) == found)                  // the element is already well-placed
                continue;                                                                   // continue with the next candidate

            // check if the id is contained in shiftThis.getKey() by a child element; we cannot shift an element to its child
            Nodes isIn = shiftThis.getKey().query("descendant::*[attribute::xml:id='" + shiftThis.getValue() + "']");
            if (isIn.size() > 0) {                                                          // the id refers to a child, shiftThis.getKey() cannot be replaced within itself
                System.out.println(shiftThis.getKey() + " will not be shifted. " + isIn.size() + "\n");
                continue;                                                                   // continue with the next candidate
            }

            // make the repositioning
            shiftThis.getKey().detach();                                                    // take it out of the xml tree
            found.getParent().insertChild(shiftThis.getKey(), found.getParent().indexOf(found));    // and insert it directly before the found element

        }

        System.out.println(" done");

        if (!notResolved.isEmpty())
            System.out.println("The following elements could not be repositioned:\n" + notResolved.toString());

        return notResolved;
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
            String uuid = "meico_" + UUID.randomUUID().toString();                         // generate new ids for them
            Attribute a = new Attribute("id", uuid);                            // create an attribute
            a.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");      // set its namespace to xml
            ((Element) e.get(i)).addAttribute(a);                               // add attribute to the node
        }


        return e.size();
    }
}