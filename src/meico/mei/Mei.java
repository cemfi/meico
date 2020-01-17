package meico.mei;

import java.io.*;
import java.net.URL;
import java.util.*;

import meico.mpm.Mpm;
import meico.mpm.elements.Part;
import meico.mpm.elements.maps.ArticulationMap;
import meico.mpm.elements.maps.DynamicsMap;
import meico.mpm.elements.maps.GenericMap;
import meico.mpm.elements.maps.TempoMap;
import meico.mpm.elements.maps.data.DynamicsData;
import meico.mpm.elements.maps.data.TempoData;
import meico.mpm.elements.styles.ArticulationStyle;
import meico.mpm.elements.styles.DynamicsStyle;
import meico.mpm.elements.styles.defs.ArticulationDef;
import meico.mpm.elements.styles.defs.DynamicsDef;
import meico.supplementary.KeyValue;
import meico.mpm.elements.Performance;
import meico.msm.Goto;
import meico.svg.SvgCollection;
import nu.xom.*;
import meico.msm.Msm;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * This class holds the mei data from a source file in a XOM Document.
 * @author Axel Berndt.
 */

public class Mei extends meico.xml.XmlBase {

    private Helper helper = null;                                   // some variables and methods to make life easier

    /**
     * a default constructor that creates an empty Mei instance
     */
    public Mei() {
        super();
    }

    /**
     * constructor
     *
     * @param mei the mei document of which to instantiate the MEI object
     */
    public Mei(Document mei) {
        super(mei);
    }

    /** constructor; reads the mei file without validation
     *
     * @param file a File object
     * @throws IOException
     * @throws ParsingException
     */
    public Mei(File file) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file);
    }

    /** constructor
     *
     * @param file a File object
     * @param validate set true to activate validation of the mei document, else false
     * @param schema URL to MEI schema
     * @throws IOException
     * @throws ParsingException
     */
    public Mei(File file, boolean validate, URL schema) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file, validate, schema);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @throws IOException
     * @throws ParsingException
     */
    public Mei(String xml) throws IOException, ParsingException {
        super(xml);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @param validate validate the code?
     * @param schema URL to MEI schema
     * @throws IOException
     * @throws ParsingException
     */
    public Mei(String xml, boolean validate, URL schema) throws IOException, ParsingException {
        super(xml, validate, schema);
    }

    /**
     * constructor
     * @param inputStream read xml code from this input stream
     * @throws IOException
     * @throws ParsingException
     */
    public Mei(InputStream inputStream) throws IOException, ParsingException {
        super(inputStream);
    }

    /**
     * constructor
     * @param inputStream read xml code from this input stream
     * @param validate
     * @param schema URL to MEI schema
     * @throws IOException
     * @throws ParsingException
     */
    public Mei(InputStream inputStream, boolean validate, URL schema) throws IOException, ParsingException {
        super(inputStream, validate, schema);
    }

    /** 
     * writes the mei document to a ...-meico.mei file at the same location as the original mei file; this method is mainly relevant for debug output after calling exportMsm()
     * @return true if success, false if an error occured
     */
    public boolean writeMei() {
        String filename = Helper.getFilenameWithoutExtension(this.getFile().getPath()) + "-meico.mei";   // replace the file extension ".mei" by "-meico.mei"
        return this.writeFile(filename);
    }

    /** 
     * writes the mei document to a file (filename should include the path and the extension .mei); this method is mainly relevant for debug output after calling exportMsm()
     * @param filename the filename string including the complete path!
     * @return true if success, false if an error occured
     */
    public boolean writeMei(String filename) {
        return this.writeFile(filename);
    }

    /**
     * @return the meiHead element or null if this instance is not valid
     */
    public Element getMeiHead() {
        if (this.isEmpty())
            return null;

        Element e = this.getRootElement().getFirstChildElement("meiHead");
        if (e == null)
            e = this.getRootElement().getFirstChildElement("meiHead", this.getRootElement().getNamespaceURI());

        return e;
    }

    /**
     * This getter method returns the title string from either fileDesc or workDesc. If none is given, it returns the filename. If not given either, "" is returned.
     * @return
     */
    public String getTitle() {
        Element title;

        try {                                               // try to read the title from mei/meiHead/workDesc/work/titleStmt/title
            title = Helper.getFirstChildElement("fileDesc", this.getMeiHead());
            title = Helper.getFirstChildElement("titleStmt", title);
            title = Helper.getFirstChildElement("title", title);
        } catch (NullPointerException ex1) {                // if that does not exist
            try {                                           // try to get the title from  mei/meiHead/fileDesc/titleStmt/title
                title = Helper.getFirstChildElement("workDesc", this.getMeiHead());
                title = Helper.getFirstChildElement("work", title);
                title = Helper.getFirstChildElement("titleStmt", title);
                title = Helper.getFirstChildElement("title", title);
            } catch (NullPointerException ex2) {            // if that does not exist
                return (this.getFile() == null) ? "" : Helper.getFilenameWithoutExtension(this.getFile().getName());    // return the filename without extension or (if that does not exist either) return empty string
            }
        }
        return (title != null) ? title.getValue() : ((this.getFile() == null) ? "" : Helper.getFilenameWithoutExtension(this.getFile().getName()));  // return the title string
    }

    /**
     * @return the music element or null if this instance is not valid
     */
    public Element getMusic() {
        if (this.isEmpty())
            return null;

        Element e = this.getRootElement().getFirstChildElement("music");
        if (e == null)
            e = this.getRootElement().getFirstChildElement("music", this.getRootElement().getNamespaceURI());

        return e;
    }

    /**
     * convert MEI to SVG
     * @return
     */
    public SvgCollection exportSvg() {
        return this.exportSvg(true, false);
    }

    /**
     * convert MEI to SVG
     * TODO: so far, this is just a placeholder, cannot evaluate Verovio in the Nashorn engine, same problem as in MusicXml.exportMei()
     * @param useLatestVerovio
     * @param oneLineScore
     * @return
     */
    public SvgCollection exportSvg(boolean useLatestVerovio, boolean oneLineScore) {
        System.out.println("\nConverting " + ((this.file != null) ? this.file.getName() : "MEI data") + " to SVG.");

        throw new UnsupportedOperationException("Functionality Mei.exportSvg() is not implemented yet.");
//        return null;
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
        return this.exportMsm(ppq, true, false, true);
    }

    /** converts the mei data into msm format and returns a list of Msm instances, one per movement/mdiv, ppq (pulses per quarter) sets the time resolution
     *
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @param  dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @return the list of msm documents (movements) created
     */
    public List<Msm> exportMsm(int ppq, boolean dontUseChannel10) {
        return this.exportMsm(ppq, dontUseChannel10, false, true);
    }

    /**
     * converts the mei data into msm format and returns a list of Msm instances, one per movement/mdiv, ppq (pulses per quarter) sets the time resolution
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @param dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @param ignoreExpansions set this true to have a 1:1 conversion of MEI to MSM without the rearrangement that MEI's expansion elements produce
     * @return the list of msm documents (movements) created
     */
    public List<Msm> exportMsm(int ppq, boolean dontUseChannel10, boolean ignoreExpansions) {
        return this.exportMsm(ppq, dontUseChannel10, ignoreExpansions, true);
    }

    /**
     * converts the mei data into msm format and returns a list of Msm instances, one per movement/mdiv, ppq (pulses per quarter) sets the time resolution
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @param dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @param cleanup set true to return a clean msm file or false to keep all the crap from the conversion
     * @param ignoreExpansions set this true to have a 1:1 conversion of MEI to MSM without the rearrangement that MEI's expansion elements produce
     * @return the list of msm documents (movements) created
     */
    public List<Msm> exportMsm(int ppq, boolean dontUseChannel10, boolean ignoreExpansions, boolean cleanup) {
        return this.exportMsmMpm(ppq, dontUseChannel10, ignoreExpansions, cleanup).getKey();
    }

    /**
     * converts the mei data into msm and mpm format and returns a tuplet of lists, one with the msms (one per movement/mdiv), the other with the corresponding mpms
     * @return the list of msm documents (movements) created
     */
    public KeyValue<List<Msm>, List<Mpm>> exportMsmMpm() {
        return this.exportMsmMpm(720);                                             // do the conversion with a default value of pulses per quarter
    }

    /**
     * converts the mei data into msm and mpm format and returns a tuplet of lists, one with the msms (one per movement/mdiv), the other with the corresponding mpms
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @return the list of msm documents (movements) created
     */
    public KeyValue<List<Msm>, List<Mpm>> exportMsmMpm(int ppq) {
        return this.exportMsmMpm(ppq, true, false, true);
    }

    /**
     * converts the mei data into msm and mpm format and returns a tuplet of lists, one with the msms (one per movement/mdiv), the other with the corresponding mpms
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @param dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @return the list of msm documents (movements) created
     */
    public KeyValue<List<Msm>, List<Mpm>> exportMsmMpm(int ppq, boolean dontUseChannel10) {
        return this.exportMsmMpm(ppq, dontUseChannel10, false, true);
    }

    /**
     * converts the mei data into msm and mpm format and returns a tuplet of lists, one with the msms (one per movement/mdiv), the other with the corresponding mpms
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @param dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @param ignoreExpansions set this true to have a 1:1 conversion of MEI to MSM without the rearrangement that MEI's expansion elements produce
     * @return the list of msm documents (movements) created
     */
    public KeyValue<List<Msm>, List<Mpm>> exportMsmMpm(int ppq, boolean dontUseChannel10, boolean ignoreExpansions) {
        return this.exportMsmMpm(ppq, dontUseChannel10, ignoreExpansions, true);
    }

    /**
     * converts the mei data into msm and mpm format and returns a tuplet of lists, one with the msms (one per movement/mdiv), the other with the corresponding mpms
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @param dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @param cleanup set true to return a clean msm file or false to keep all the crap from the conversion
     * @param ignoreExpansions set this true to have a 1:1 conversion of MEI to MSM without the rearrangement that MEI's expansion elements produce
     * @return the list of msm documents (movements) created
     */
    public synchronized KeyValue<List<Msm>, List<Mpm>> exportMsmMpm(int ppq, boolean dontUseChannel10, boolean ignoreExpansions, boolean cleanup) {
        long startTime = System.currentTimeMillis();                            // we measure the time that the conversion consumes
        System.out.println("\nConverting " + ((this.file != null) ? this.file.getName() : "MEI data") + " to MSM and MPM.");

        if (this.isEmpty() || (this.getMusic() == null) || (this.getMusic().getFirstChildElement("body", this.getMusic().getNamespaceURI()) == null))      // if no mei music data available
            return new KeyValue<>(new ArrayList<Msm>(), new ArrayList<Mpm>());  // return empty lists

        // check whether the  shortest duration in the mei (note value can go down to 2048th) is captured by the defined ppq resolution; adjust ppq automatically and output a message
        int minPPQ = this.computeMinimalPPQ();                                  // compute the minimal required ppq resolution
        if (minPPQ > ppq) {                                                     // if it is greater than the specified resolution
            ppq = minPPQ;                                                       // adjust the specified ppq to ensure viable results
            System.out.println("The specified pulses per quarternote resolution (ppq) is too coarse to capture the shortest duration values in the mei source with integer values. Using the minimal required resolution of " + ppq + " instead");
        }

        Document orig = null;
        if (cleanup)
            orig = (Document)this.data.copy();                                  // the document will be altered during conversion, thus we keep the original to restore it after the process

//        long t = System.currentTimeMillis();
        this.resolveCopyofsAndSameas();                                         // replace the slacker elements with copyof and sameas attributes by copies of the referred elements
        this.removeRendElements();                                              // only the content of the rend elements is relevant, move these one level up replacing the rend with it
        if (!ignoreExpansions) this.resolveExpansions();                        // if expansions should be realized, render expansion elements in the MEI score to a "through-composed"/regularized score without expansions
//        System.out.println("Time consumed: " + (System.currentTimeMillis()-t));

        this.helper = new Helper(ppq);                                          // some variables and methods to make life easier
        this.helper.dontUseChannel10 = dontUseChannel10;                        // set the flag that says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on

        Elements bodies = this.getMusic().getChildElements("body", this.getMusic().getNamespaceURI());  // get the list of body elements in the mei source
        for (int b = 0; b < bodies.size(); ++b)                                 // for each body
            this.convert(bodies.get(b));                                        // convert each body to msm, the resulting Msms can then be found in this.helper.movements

        // the list of Msm instances, each one is an mdiv in mei
        LinkedList<Msm> msms = new LinkedList<>(this.helper.movements);         // get the resulting msms for further processing and returning
        LinkedList<Mpm> mpms = new LinkedList<>(this.helper.performances);      // ret the resulting performance

        Helper.mpmPostprocessing(mpms);                                         // finalize all mpm data

        this.helper = null;                                                     // as this is a class variable it would remain in memory after this method, so it has to be nulled for garbage collection

        // cleanup
        if (cleanup){
            this.data = orig;                                                   // restore the unaltered version of the mei data
            Helper.msmCleanup(msms);                                            // cleanup of the msm objects to remove all conversion related and no longer needed entries in the msm objects
        }

        // generate a dummy file name in the msm objects
        if (this.file != null) {
            if (msms.size() == 1)                                                                                           // if only one msm object (no numbering needed)
                msms.get(0).setFile(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".msm");                 // replace the file extension mei with msm and make this the filename
            else {                                                                                                          // multiple msm objects created (or none)
                for (int i = 0; i < msms.size(); ++i) {                                                                     // for each msm object
                    msms.get(i).setFile(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + "-" + i + ".msm");   // replace the extension by the number and the .msm extension
                }
            }
            if (mpms.size() == 1) {                                                                                         // if only one msm object (no numbering needed)
                mpms.get(0).setFile(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".mpm");                 // replace the file extension mei with msm and make this the filename
                mpms.get(0).addRelatedResource(msms.get(0).getFile().getAbsolutePath(), "msm");                              // add the msm to the reference music
            }
            else {                                                                                                          // multiple msm objects created (or none)
                for (int i = 0; i < mpms.size(); ++i) {                                                                     // for each msm object
                    mpms.get(i).setFile(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + "-" + i + ".mpm");   // replace the extension by the number and the .msm extension
                    mpms.get(i).addRelatedResource(msms.get(i).getFile().getAbsolutePath(), "msm");                          // add the corresponding msm to the reference music
                }
            }
        }

        System.out.println("MEI to MSM/MPM conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return new KeyValue<>(msms, mpms);
    }

    /**
     * recursively traverse the mei tree (depth first) starting at the root element and return the list of Msm instances; root indicates the root of the subtree,
     * the resulting Msm objects are stored in this.helper.movements
     * @param root the root of the subtree to be processed
     */
    private void convert(Element root) {
        Elements es = root.getChildElements();                                  // all child elements of root

        for (int i = 0; i < es.size(); ++i) {                                   // element beginHere traverses the mei tree
            Element e = es.get(i);                                              // get the element

            this.helper.checkEndid(e);                                          // check for pending elements with endid attributes to be finished when the element with this endid is found

            // process the element
            switch (e.getLocalName()) {
                case "abbr":                                                    // abbreviation
                    continue;                                                   // TODO: What to do with this? Can be child of choice and is handled in this.processChoice(). However, it is basically ignored.

                case "accid":                                                   // process accid elements that are not children of notes
                    this.processAccid(e);
                    continue;

                case "add":                                                     // marks an addition to the text
                    break;                                                      // process the contents

                case "anchorText":
                    continue;                                                   // can be ignored

                case "annot":
                    continue;                                                   // TODO: ignore

                case "app":                                                     // critical apparatus, may contain lem and rdg elements
                    this.processApp(e);
                    continue;

                case "arpeg":
                    continue;                                                   // TODO: ignored at the moment but relevant for expressive performance later on

                case "artic":                                                   // an indication of how to play a note or chord
                    this.processArtic(e);
                    continue;

                case "barline":
                    continue;                                                   // can be ignored

                case "beam":                                                    // contains the notes to be beamed TODO: relevant for expressive performance
                    break;

                case "beamSpan":
                    continue;                                                   // TODO: may be relavant for expressive phrasing

                case "beatRpt":
                    this.processBeatRpt(e);
                    continue;

                case "bend":
                    continue;                                                   // TODO: relevant for expressive performance

                case "breath":                                                  // an indication of a point at which the performer on an instrument requiring breath (including the voice) may breathe
                    this.processBreath(e);
                    continue;

                case "bTrem":
                    this.processChord(e);                                       // bTrems are treated as chords
                    continue;                                                   // continue with the next sibling

                case "caesura":                                                 // TODO: relevant for expressive performance
                    continue;

                case "choice":                                                  // the children of a choice element are alternatives of which meico has to choose one
                    this.processChoice(e);
                    continue;

                case "chord":
                    if (e.getAttribute("grace") != null)                        // TODO: at the moment we ignore grace notes and grace chords; later on, for expressive performances, we should handle these somehow
                        continue;
                    this.processChord(e);
                    continue;                                                   // continue with the next sibling

                case "chordTable":
                    continue;                                                   // can be ignored

                case "clef":
                    continue;                                                   // TODO: can this be ignored or is it of any relevance to pitch computation?

                case "clefGrp":
                    continue;                                                   // TODO: can this be ignored or is it of any relevance to pitch computation?

                case "corr":                                                    // a correction, cann occur as "standalone" or in the choice environment, usually paired with the sic element
                    break;                                                      // nothing special about this element to process, just process its subtree

                case "curve":
                    continue;                                                   // can be ignored

                case "custos":
                    continue;                                                   // can be ignored

                case "damage":
                    continue;                                                   // TODO: ignore

                case "del":                                                     // contains information deleted, marked as deleted, or otherwise indicated as superfluous or spurious in the copy text by an author, scribe, annotator, or corrector
                    this.processDel(e);
                    continue;

                case "dir":
                    continue;                                                   // TODO: relevant for expressive performance

                case "div":
                    continue;                                                   // can be ignored

                case "dot":
                    this.processDot(e);
                    continue;                                                   // there should be no children, so continue with the next element

                case "dynam":                                                   // indication of the volume of a note, phrase, or section of music
                    this.processDynam(e);
                    continue;

                case "ending":                                                  // relevant in the context of repetitions
                    this.processEnding(e);
                    continue;

                case "expan":                                                   // the expansion of an abbreviation
                    break;                                                      // nothing special about this element to process, but dive into it and process its children

                case "expansion":                                               // indicates how a section may be programmatically expanded into its 'through-composed' form
                    continue;                                                   // expansions are treated during preprocessing, here they are ignored

                case "fermata":
                    continue;                                                   // TODO: relevant for expressive performance

                case "fTrem":
                    this.processChord(e);                                       // fTrems are treated as chords
                    continue;                                                   // continue with the next sibling

                case "gap":
                    continue;                                                   // TODO: What to do with this?

                case "gliss":
                    continue;                                                   // TODO: relevant for expressive performance

                case "grpSym":
                    continue;                                                   // can be ignored

                case "hairpin":                                                 // indicates continuous dynamics expressed on the score as wedge-shaped graphics, e.g. < and >.
                    this.processDynam(e);
                    continue;

                case "halfmRpt":
                    this.processHalfmRpt(e);
                    break;

                case "handShift":
                    continue;                                                   // TODO: What to do with this?

                case "harm":
                    continue;                                                   // can be ignored

                case "harpPedal":
                    continue;                                                   // can be ignored

                case "incip":
                    continue;                                                   // can be ignored

                case "ineume":
                    continue;                                                   // ignored, this implementation focusses on common modern notation

                case "instrDef":
                    continue;                                                   // ignore this tag as this converter handles midi stuff individually

                case "instrGrp":
                    continue;                                                   // ignore this tag as this converter handles midi stuff individually

                case "keyAccid":
                    continue;                                                   // this element is processed within a keySig; if it occurs outside of a keySig environment it is invalid, hence, ignored

                case "keySig":
                    this.processKeySig(e);
                    break;

                case "label":
                    continue;                                                   // can be ignored

                case "layer":
                    this.processLayer(e);
                    continue;

                case "layerDef":
                    this.processLayerDef(e);
                    break;

                case "lb":
                    continue;                                                   // can be ignored

                case "lem":                                                     // this element is part of the critical apparatus (child of app)
                    continue;                                                   // it is processed by this.processApp()

                case "line":
                    continue;                                                   // can be ignored

                case "lyrics":                                                  // TODO: should I do anything more with it than just diving into it?
                    break;

                case "mdiv":
                    this.makeMovement(e);
                    continue;

                case "measure":
                    this.processMeasure(e);                                     // this creates the date and dur attribute and adds them to the measure
                    continue;

                case "mensur":
                    continue;                                                   // TODO: ignore

                case "meterSig":
                    this.processMeterSig(e);
                    break;

                case "meterSigGrp":                                             // TODO: I have no idea how to handle this, at the moment I go through it and process the contained meterSig elements as if they were standing alone
                    break;

                case "midi":
                    continue;                                                   // ignore this tag as this converter handles midi stuff individually

                case "mordent":
                    continue;                                                   // TODO: relevant for expressive performance

                case "mRest":
                    this.processMeasureRest(e);
                    continue;

                case "mRpt":
                    this.processMRpt(e);
                    break;

                case "mRpt2":
                    this.processMRpt2(e);
                    break;

                case "mSpace":
                    this.processMeasureRest(e);                                 // interpret it as an mRest, i.e. measure rest
                    continue;

                case "multiRest":
                    this.processMultiRest(e);
                    continue;

                case "multiRpt":
                    this.processMultiRpt(e);
                    break;

                case "note":
                    if (e.getAttribute("grace") != null)                        // TODO: at the moment we ignore grace notes and grace chords; later on, for expressive performances, we should handle these somehow
                        continue;
                    this.processNote(e);
                    continue;                                                   // no need to go deeper as any child of this tag is already processed

                case "octave":
                    this.processOctave(e);
                    break;

                case "oLayer":                                                  // layer that contains an alternative to material in another layer
                    this.processLayer(e);
                    continue;

                case "orig":                                                    // contains material which is marked as following the original, rather than being normalized or corrected
                    break;                                                      // when it does not appear in a choice environment as member of an orig-reg pair it has to be processed

                case "ossia":
                    continue;                                                   // TODO: ignored for the moment but may be included later on

                case "oStaff":                                                  // staff that holds an alternative passage which may be played instead of the original material
                    this.processStaff(e);
                    continue;

                case "parts":                                                   // just dive into it
                    break;

                case "part":                                                    // just dive into it
                    break;

                case "pb":
                    continue;                                                   // can be ignored

                case "pedal":
                    this.processPedal(e);
                    continue;

                case "pgFoot":
                    continue;                                                   // can be ignored

                case "pgFoot2":
                    continue;                                                   // can be ignored

                case "pgHead":
                    continue;                                                   // can be ignored

                case "pgHead2":
                    continue;                                                   // can be ignored

                case "phrase":                                                  // indication of 1) a "unified melodic idea" or 2) performance technique
                    this.processPhrase(e);                                      // this contains a recursive call of convert()
                    continue;

                case "proport":
                    continue;                                                   // TODO: ignore

                case "rdg":                                                     // this element is part of the critical apparatus (child of app)
                    continue;                                                   // it is processed by this.processApp()

                case "reg":                                                     // contains material which has been regularized or normalized in some sense
                    break;                                                      // process its content

                case "reh":
                    this.processReh(e);                                         // TODO: generate midi jump marks
                    continue;

                case "rend":
                    continue;                                                   // can be ignored, actually they are removed during preprocessing by removeRendElements()

                case "rest":
                    this.processRest(e);                                        // process the rest
                    continue;                                                   // no need to dive deeper

                case "restore":                                                 // indicates restoration of material to an earlier state by cancellation of an editorial or authorial marking or instruction
                    this.processRestore(e);                                     // set all del elements in this restore to restore-meico="true"
                    break;                                                      // process its contents

                case "sb":
                    continue;                                                   // can be ignored

                case "scoreDef":
                    this.processScoreDef(e);
                    break;

                case "score":                                                   // just dive into it
                    break;

                case "section":                                                 // Segment of music data.
                    this.processSection(e);                                     // this contains a recursive call of convert()
                    continue;

                case "sic":                                                     // indicates an apparent error, usually paired with the corr element, but if not, its content should be processed
                    break;

                case "space":
                    this.processRest(e);                                        // handle it like a rest
                    continue;

                case "slur":                                                    // indication of 1) a "unified melodic idea" or 2) performance technique
                    this.processSlur(e);                                        // meico interprets these as legati
                    continue;

                case "stack":
                    continue;                                                   // can be ignored

                case "staff":
                    this.processStaff(e);
                    continue;

                case "staffDef":
                    this.processStaffDef(e);
                    continue;

                case "staffGrp":                                                // may contain staffDefs but needs no particular processing, attributes are ignored
                    break;

                case "subst":                                                   // groups transcriptional elements when the combination is to be regarded as a single intervention in the text
                    break;                                                      // process its contents

                case "supplied":                                                // contains material supplied by the transcriber or editor in place of text which cannot be read, either because of physical damage or loss in the original or because it is illegible for any reason
                    break;                                                      // process its content

                case "syl":
                    continue;                                                   // TODO: can be included in MIDI, too; useful for choir synthesis

                case "syllable":
                    continue;                                                   // ignored, this implementation focusses on common modern notation

                case "symbol":
                    continue;                                                   // can be ignored

                case "symbolTable":
                    continue;                                                   // can be ignored

                case "tempo":
                    this.processTempo(e);                                       // text and symbols descriptive of tempo, mood, or style, e.g., "allarg.", "a tempo", "cantabile", "Moderato", "♩=60", "Moderato ♩ =60")
                    continue;

                case "tie":
                    this.processTie(e);
                    continue;                                                   // tie are handled in the preprocessing, they can be ignored here

                case "timeline":
                    continue;                                                   // can be ignored

                case "trill":
                    continue;                                                   // TODO: relevant for expressive performance

                case "tuplet":
                    if (this.processTuplet(e))
                        continue;
                    break;

                case "tupletSpan":
                    this.processTupletSpan(e);
                    continue;                                                   // TODO: how do I have to handle this?

                case "turn":
                    continue;                                                   // TODO: relevant for expressive performance

                case "unclear":                                                 // contains material that cannot be transcribed with certainty because it is illegible or inaudible in the source
                    break;                                                      // process the contents

                case "uneume":
                    continue;                                                   // ignored, this implementation focusses on common modern notation

                case "verse":
                    continue;                                                   // TODO: ignored

                default:
                    continue;                                                   // ignore it and its children

            }
            this.convert(e);
        }

        return;
    }

    /**
     * this function gets an mdiv and creates an instance of Msm
     * @param mdiv an mei mdiv element
     */
    private void makeMovement(Element mdiv) {
        // specify the title attribute for this MSM; concatenate work title and movement label
        String titleString = this.getTitle();
        Attribute mdivN = mdiv.getAttribute("n");
        if (mdivN != null) titleString += " - " + mdivN.getValue();
        Attribute mdivLabel = mdiv.getAttribute("label");
        if (mdivLabel != null) titleString += " - " + mdivLabel.getValue();

        // store the same id at the mei source and the msm, maybe it is needed later on
        String movementId;
        Attribute id = Helper.getAttribute("id", mdiv);
        if (id != null) {                                                           // if mdiv has an id, reuse it
            movementId = id.getValue();                                             // get its value
        }
        else {                                                                      // otherwise generate a unique id
            movementId = "meico_" + UUID.randomUUID().toString();                   // generate id string
            mdiv.addAttribute(new Attribute("id", movementId));                     // add it to the MEI mdiv
        }

        Msm msm = Msm.createMsm(titleString, movementId, this.helper.ppq);          // create Msm instance
        this.helper.movements.add(msm);                                             // add it to the movements list

        Mpm mpm = Mpm.createMpm();                                                  // generate an Mpm object
        mpm.addRelatedResource(this.file.getAbsolutePath(), "mei");                  // add the mei as music reference
        Performance performance = Performance.createPerformance("MEI export performance");       // generate a Performance object
        if (performance == null) {                                                  // make sure it is not null
            System.err.println("Failed to generate an instance of Performance. Skipping mdiv " + titleString);
            return;
        }
        performance.setPulsesPerQuarter(this.helper.ppq);                           // set its ppq
        mpm.addPerformance(performance);                                            // add the performance to the mpm
        this.helper.performances.add(mpm);                                          // add it to the performances list

        this.helper.reset();                                                        // reset the helper variables
        this.helper.currentMdiv = mdiv;                                             // store current mdiv for later reference
        this.helper.currentMsmMovement = msm.getRootElement();                      // store root of current MSM movement for later reference
        this.helper.currentPerformance = performance;                               // store the link to the current performance for later reference
        this.helper.indexNotesAndChords(this.helper.currentMdiv);                   // create an index of all notes and chords in this mdiv, this makes things faster later on

        // find the corresponding work element in  meiHead
        String n = (mdiv.getAttribute("n") == null) ? null : mdiv.getAttributeValue("n");
        String[] decls = (mdiv.getAttribute("decls") == null) ? null : mdiv.getAttributeValue("decls").split("\\s+");
        Element workList = Helper.getFirstChildElement("workList", this.getMeiHead());  // MEI 4.0 has workList
        if (workList == null)
            workList = Helper.getFirstChildElement("workDesc", this.getMeiHead());      // MEI 3.0 has workDesc instead
        if (workList != null) {
            LinkedList<Element> works = Helper.getAllChildElements("work", workList);
            switch (works.size()) {
                case 0:
                    break;
                case 1:
                    this.helper.currentWork = works.get(0);                             // that's it
                    break;
                default: {
                    if (decls != null) {
                        for (Element work : works) {
                            String workId = Helper.getAttributeValue("id", work);
                            boolean found = false;
                            for (String decl : decls) {
                                if (decl.substring(1).equals(workId)) {
                                    this.helper.currentWork = work;
                                    found = true;
                                    break;
                                }
                            }
                            if (found)
                                break;
                        }
                    }
                    if ((this.helper.currentWork == null) && (n != null)) {
                        for (Element work : works) {
                            if (n.equals(Helper.getAttributeValue("n", work))) {
                                this.helper.currentWork = work;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (msm.isEmpty()) {                        // create a new instance of Msm with a new Document and a unique id (size of the movements list so far), if something went wrong stop diving into this subtree
            System.err.println("Skipping mdiv. Failed to initialize required data objects.");
            return;
        }
        this.convert(mdiv);                         // process the content of the mdiv

        GenericMap globalTempoMap = this.helper.currentPerformance.getGlobal().getDated().getMap(Mpm.TEMPO_MAP);
        if (((globalTempoMap == null) || (globalTempoMap.getElementBeforeAt(0.0) == null)) && (this.helper.currentWork != null)) {  // if the global tempoMap has no initial tempo and if we have a work element in meiHead
            Element tempo = Helper.getFirstChildElement("tempo", this.helper.currentWork);
            if (tempo != null) {                                                                                                    // and it contains a tempo element
                TempoData tempoData = this.helper.parseTempo(tempo);
                if (tempoData != null) {
                    if (globalTempoMap == null) {
                        globalTempoMap = this.helper.currentPerformance.getGlobal().getDated().addMap(Mpm.TEMPO_MAP);               // make sure there is a global tempoMap
                        globalTempoMap.addStyleSwitch(0.0, "MEI export");                                                           // set its start style reference
                    }
                    tempoData.startDate = 0.0;
                    ((TempoMap) globalTempoMap).addTempo(tempoData);
                }
            }
        }
    }

    /** process an mei scoreDef element
     * @param scoreDef an mei scoreDef element
     */
    private void processScoreDef(Element scoreDef) {
        if (this.helper.currentPart != null) {                                                      // if we are already in a specific part, these infos are treaded as local
            this.processStaffDef(scoreDef);
            return;
        }

        scoreDef.addAttribute(new Attribute("date", helper.getMidiTimeAsString()));

        // otherwise all entries are done in globally maps
        Element s;

        // time signature
        s = this.makeTimeSignature(scoreDef);                                                       // create a time signature element, or null if there is no such data
        if (s != null) {                                                                            // if succeeded
            Helper.addToMap(s, this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap"));  // insert it into the global time signature map
        }

        // key signature
        s = this.makeKeySignature(scoreDef);                                                        // create a key signature element, or null if there is no such data
        if (s != null) {                                                                            // if succeeded
            Helper.addToMap(s, this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("keySignatureMap"));   // insert it into the global key signature map
        }

        // store default values in miscMap
        if ((scoreDef.getAttribute("dur.default") != null)) {                                       // if there is a default duration defined
            Element d = new Element("dur.default");                                                 // make an entry in the miscMap
            d.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString())); // add the current date
            d.addAttribute(new Attribute("dur", scoreDef.getAttributeValue("dur.default")));        // copy the value
            Helper.copyId(scoreDef, d);                                                             // copy the id
            Helper.addToMap(d, this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap"));   // make an entry in the miscMap
        }

        if (scoreDef.getAttribute("octave.default") != null) {                                      // if there is a default octave defined
            Element d = new Element("oct.default");
            d.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString())); // add the current date
            d.addAttribute(new Attribute("oct", scoreDef.getAttributeValue("octave.default")));     // copy the value
            Helper.copyId(scoreDef, d);                                                             // copy the id
            Helper.addToMap(d, this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap"));   // make an entry in the miscMap
        }

        {   // if there is a transposition (we only support the trans.semi attribute, not trans.diat)
            double trans = 0;
            trans = (scoreDef.getAttribute("trans.semi") == null) ? 0.0 : Double.parseDouble(scoreDef.getAttributeValue("trans.semi"));
            trans += Helper.processClefDis(scoreDef);
            Element d = new Element("transposition");                                               // create a transposition entry
            d.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));          // add the current date
            d.addAttribute(new Attribute("semi", Double.toString(trans)));                          // copy the value or write "0" for no transposition (this is to cancel previous entries)
            Helper.copyId(scoreDef, d);                                                             // copy the id
            Helper.addToMap(d, this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap"));   // make an entry in the miscMap
        }

        // MIDI channel and port information are ignored as these are assigned automatically by this converter
        // attribute ppq is ignored ase the converter defines an own ppq resolution
        // TODO: tuning is defined by attributes tune.pname, tune.Hz and tune.temper; for the moment these are ignored

        Helper.addToMap(Helper.cloneElement(scoreDef), this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap"));   // make a flat copy of the element and put it into the global miscMap
    }

    /** process an mei staffDef element
     * @param staffDef an mei staffDef element
     */
    private void processStaffDef(Element staffDef) {
        Element parentPart = this.helper.currentPart;                                                       // if we are already in a staff environment, store it, otherwise it is null
        this.helper.currentPart = this.makePart(staffDef);                                                  // create a part element in movement, or get Element pointer if this part exists already

        staffDef.addAttribute(new Attribute("date", helper.getMidiTimeAsString()));

        // handle local time signature entry
        Element t = this.makeTimeSignature(staffDef);                                                       // create a time signature element, or null if there is no such data
        if (t != null) {                                                                                    // if succeeded
            Helper.addToMap(t, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap")); // insert it into the global time signature map
        }

        // handle local key signature entry
        t = this.makeKeySignature(staffDef);																// create a key signature element, or nullptr if there is no such data
        if (t != null) {                                                                                    // if succeeded
            Helper.addToMap(t, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("keySignatureMap"));  // insert it into the global key signature map
        }

        // store default values in miscMap
        if ((staffDef.getAttribute("dur.default") != null)) {                                               // if there is a default duration defined
            Element d = new Element("dur.default");                                                         // make an entry in the miscMap
            d.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));                  // add the current date
            d.addAttribute(new Attribute("dur", staffDef.getAttributeValue("dur.default")));                // copy the value
            Helper.copyId(staffDef, d);                                                                     // copy the id
            Helper.addToMap(d, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap"));  // make an entry in the miscMap
        }

        if ((staffDef.getAttribute("octave.default", staffDef.getNamespaceURI()) != null)) {                // if there is a default duration defined
            Element d = new Element("oct.default");                                                         // make an entry in the miscMap
            d.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));                  // add the current date
            d.addAttribute(new Attribute("oct", staffDef.getAttributeValue("octave.default")));             // copy the value
            Helper.copyId(staffDef, d);                                                                     // copy the id
            Helper.addToMap(d, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap"));  // make an entry in the miscMap
        }


        {   // if there is a transposition (we only support the trans.semi attribute, not trans.diat)
            double trans = 0;
            trans = (staffDef.getAttribute("trans.semi") == null) ? 0.0 : Double.parseDouble(staffDef.getAttributeValue("trans.semi"));
            trans += Helper.processClefDis(staffDef);
            Element d = new Element("transposition");                                                       // create a transposition entry
            d.addAttribute(new Attribute("semi", Double.toString(trans)));                                  // copy the value or write "0" for no transposition (this is to cancel previous entries)
            d.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));
            Helper.copyId(staffDef, d);                                                                     // copy the id
            Helper.addToMap(d, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap"));  // make an entry in the miscMap
        }

        // attribute ppq is ignored as the converter defines an own ppq resolution
        // TODO: tuning is defined by attributes tune.pname, tune.Hz and tune.temper; for the moment these are ignored

        Helper.addToMap(Helper.cloneElement(staffDef), this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap"));  // make a flat copy of the element and put it into the global miscMap

        // process the child elements
        this.convert(staffDef);                                     // process the staff's children
        this.helper.accid.clear();                                  // accidentals are valid within one measure, but not in the succeeding measures, so forget them
        this.helper.currentPart = parentPart;                       // after this staff entry and its children are processed, set currentPart back to the parent staff
    }

    /** process an mei staff element
     * @param staff an mei staff element
     */
    private void processStaff(Element staff) {
        Attribute ref = staff.getAttribute("def");                              // get the part entry, try the def attribute first
        if (ref == null) ref = staff.getAttribute("n");                         // otherwise the n attribute
        Element s = this.helper.getPart((ref == null) ? "" : ref.getValue());   // get the part
        Element parentPart = this.helper.currentPart;                           // if we are already in a staff environment, store it, otherwise it is null

        if (s != null) {
//            s.addAttribute(new Attribute("currentDate", (this.helper.currentMeasure != null) ? this.helper.currentMeasure.getAttributeValue("date") : "0.0"));  // set currentDate of processing
            s.addAttribute(new Attribute("currentDate", this.helper.getMidiTimeAsString()));  // set currentDate of processing
            this.helper.currentPart = s;                                                               // if that part entry was found, return it
        }
        else {            // the part was not found, create one
            System.out.println("There is an undefined staff element in the score with no corresponding staffDef.\n" + staff.toXML() + "\nGenerating a new part for it.");  // output notification
            this.helper.currentPart = this.makePart(staff);                                            // generate a part and return it
        }

        // everything within the staff will be treated as local to the corresponding part, thanks to helper.currentPart being != null
        this.convert(staff);                                        // process the staff's children
        this.helper.accid.clear();                                  // accidentals are valid within one measure, but not in the succeeding measures, so forget them
        this.helper.currentPart = parentPart;                       // after this staff entry and its children are processed, set currentPart back to the parent staff
    }

    /** process an mei layerDef element
     * @param layerDef an mei layerDef element
     */
    private void processLayerDef(Element layerDef) {
        layerDef.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));

        if (layerDef.getAttribute("dur.default") != null) {                                                         // if there is a default duration defined
            Element d = new Element("dur.default");
            this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(d);   // make an entry in the miscMap
            d.addAttribute(new Attribute("dur", layerDef.getAttributeValue("dur.default")));                        // copy the value
            Helper.copyId(layerDef, d);                                                                             // copy the id
            this.helper.addLayerAttribute(d);                                                                       // add an attribute that indicates the layer
        }

        if (layerDef.getAttribute("octave.default") != null) {                                                      // if there is a default octave defined
            Element d = new Element("oct.default");
            this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").appendChild(d);   // make an entry in the miscMap
            d.addAttribute(new Attribute("oct", layerDef.getAttributeValue("octave.default")));                     // copy the value
            Helper.copyId(layerDef, d);                                                                             // copy the id
            this.helper.addLayerAttribute(d);                                                                       // add an attribute that indicates the layer
        }

        if (this.helper.currentPart == null) {                                                                      // if the layer is globally defined
            Helper.addToMap(Helper.cloneElement(layerDef), this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap"));   // make a copy of the element and put it into the global miscMap
            return;
        }

        Helper.addToMap(Helper.cloneElement(layerDef), this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap"));  // otherwise make a flat copy of the element and put it into the local miscMap
    }

    /**
     * process an mei layer element
     * @param layer
     */
    private void processLayer(Element layer) {
        Element parentLayer = this.helper.currentLayer;                                                                 // if we are already in a staff environment, store it, otherwise it is null
        this.helper.currentLayer = layer;                                                                               // keep track of this current layer as long as we process its children

        String oldDate = this.helper.currentPart.getAttribute("currentDate").getValue();                                // store currentDate in oldDate for later use

        this.convert(layer);                                                                                            // process everything within this environment

        layer.addAttribute(new Attribute("currentDate", this.helper.currentPart.getAttribute("currentDate").getValue()));// store the currentDate in the layer element to later determine the latest of these dates as the staff's part's currentDate
        this.helper.accid.clear();                                                                                      // accidentals are valid only within one layer, so forget them
        this.helper.currentLayer = parentLayer;                                                                         // we are done processing this layer, get back to the parent layer or null
        if (Helper.getNextSiblingElement("layer", layer) != null)                                                       // if there are more layers in this staff environment
            this.helper.currentPart.getAttribute("currentDate").setValue(oldDate);                                      // set back to the old currentDate, because each layer is a parallel to the other layers
        else {                                                                                                          // no further layers in this staff environment, this was the last layer in this staff
            // take the latest layer-specific currentDate as THE definitive currentDate of this part
            Nodes layers = layer.getParent().query("child::*[local-name()='layer']");
            double latestDate = Double.parseDouble(this.helper.currentPart.getAttribute("currentDate").getValue());
            for (int j = layers.size() - 1; j >= 0; --j) {
                double date = Double.parseDouble(((Element)layers.get(j)).getAttributeValue("currentDate"));            // get the layer's date
                if (latestDate < date)                                                                                  // if this layer's date is later than latestDate so far
                    latestDate = date;                                                                                  // set latestDate to date
            }
            this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(latestDate));                  // write it to the part for later reference
        }
    }

    /**
     * process an mei app element (critical apparatus),
     * in this run the method also processes lem and rdg elements (the two kinds of child elements of app)
     * @param app
     */
    private void processApp(Element app) {
        Element takeThisReading = Helper.getFirstChildElement(app, "lem");  // get the first (and hopefully only) lem element, this is the desired reading

        if (takeThisReading == null) {                                      // if there is no lem element
            takeThisReading = Helper.getFirstChildElement(app, "rdg");      // choose the first rdg element (they are all of equal value)
            if (takeThisReading == null) {                                  // if there is no reading
                return;                                                     // nothing to do, return
            }
        }

        this.convert(takeThisReading);                                      // process the chosen reading
    }

    /**
     * process an mei choice element,
     * it has to choose one the alternative subtrees to process further,
     * in here we can find the elements abbr, choice, corr, expan, orig, reg, sic, subst, unclear,
     * TODO: this implementation does not take the cert attribute (certainty rating) into account
     * @param choice
     */
    private void processChoice(Element choice) {
        String[] prefOrder = {"corr", "reg", "expan", "subst", "choice", "orig", "unclear", "sic", "abbr"};   // define the order of preference of elements to choose in this environment

        // make the choice
        Element c = null;                                           // this will hold the chosen element for processing
        for (int i=0; (c == null) && (i < prefOrder.length); ++i) { // search for the preferred types of elements in order of preference
            c = Helper.getFirstChildElement(choice, prefOrder[i]);
        }

        if (c != null) {
            if (c.getLocalName().equals("choice"))                  // if we chose a choice
                this.processChoice(c);                              // process it recursively
            else
                this.convert(c);                                    // process it
            return;                                                 // done
        }

        // nothing found
        c = choice.getChildElements().get(0);                       // then take the first child whatever it is
        if (c != null)                                              // if the choice element was not empty and we finally made a decision
            this.convert(c);                                        // process it
    }

    /**
     * Process an mei restore element.
     * However, there is an ambiguity in the MEI definition: Restore negates del in both cases, when the del is parent of restore and when when del is child of restore.
     * Whith this implementation we follow the latter interpretation, i.e. restore negates all del children (all, not only the first generation of dels!).
     * @param restore
     */
    private void processRestore(Element restore) {
        Nodes dels = restore.query("descendant::*[local-name()='del']");// get all del children

        for (int i=0; i < dels.size(); ++i) {                           // for each del
            Element d = (Element) dels.get(i);                          // get it as Element
            d.addAttribute(new Attribute("restored-meico", "true"));    // add an attribute which indicates that this del is restored; this will be recognized by method processDel()
        }
    }

    /**
     * process an mei del element,
     * this method basically checks if this del is restored and, thus, has to be processed or not
     * @param del
     */
    private void processDel(Element del) {
        Attribute restored = del.getAttribute("restored-meico");        // does this del have a meico-generated restore attribute?
        if ((restored != null) && (restored.getValue().equals("true"))) // and is it true?
            this.convert(del);                                          // then process the contents of this del element
    }

    /**
     * process an mei ending element, it basically creates entries in the global msm sequencingMap
     * @param ending
     */
    private void processEnding(Element ending) {
        double startDate = this.helper.getMidiTime();                                                                               // get the time at the beginning of the ending
        int endingCount = this.helper.endingCounter++;                                                                              // get the ending count and increase the counter afterwards
        Element sequencingMap = helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("sequencingMap");  // the sequencingMap

        // get the number of the ending, if given, otherwise n will be MIN_VALUE
        String endingText = "";                                                                                                     // this will get the text of attribute n or label
        ArrayList<Integer> endingNumbers;                                                                                           // this will hold all integers that can be extracted from the ending text (attribute n or label)
        String activity = "1";                                                                                                      // this ending will be played at least once (the first time if it is ending 1, the second time if it is a later ending, in that case a preceding "0" will be added later on)
        int n = Integer.MIN_VALUE;                                                                                                  // this is the number of the ending, the stupid MIN_VALUE will be replaced by a meaningful value during the following lines; if not, there is no numbering
        if (ending.getAttribute("n") != null) endingText = ending.getAttributeValue("n");                                           // if we have an attribute n, take this as ending text
        else if (ending.getAttribute("label") != null) endingText = ending.getAttributeValue("label");                              // otherwise, if there is an attribute label, take that
        if (endingText.toLowerCase().contains("fine"))                                                                              // if the ending text says fine
            n = Integer.MAX_VALUE;                                                                                                  // set n to the max integer value
        else {                                                                                                                      // otherwise
            endingNumbers = Helper.extractAllIntegersFromString(endingText);                                                        // search the ending text for integers
            if (!endingNumbers.isEmpty()) {                                                                                         // if there is at least one int in the ending text
                n = endingNumbers.get(0);                                                                                           // take the first as ending number
            }
        }

        // generate an id for the marker that is generated to indicate the start of this ending in the msm sequencingMap
        Attribute endingLabel = ending.getAttribute("id", "http://www.w3.org/XML/1998/namespace");
        String markerId = "endingMarker_" + ((endingLabel == null) ? UUID.randomUUID().toString() : endingLabel.getValue());        // if the ending has an id, use it, otherwise create a new one

        // create an ending marker
        Element marker = new Element("marker");                                                                                     // create the marker
        marker.addAttribute(new Attribute("date", Double.toString(startDate)));                                                // give it the startDate of the ending
        marker.addAttribute(new Attribute("message", "ending" + ((ending.getAttribute("n") == null) ? ((ending.getAttribute("label") == null) ? (": " + ending.getAttributeValue("label")) : endingCount) : (" " + ending.getAttributeValue("n")))));   // create the message from either the n attribute, the label attribute or the endingCount
        Attribute id = new Attribute("id", markerId);                                                                               // give it the markerId
        id.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");                                                             // set its namespace to xml
        marker.addAttribute(id);                                                                                                    // add the id attribute to the marker
        Helper.addToMap(marker, sequencingMap);                                                                                     // add it to the global sequencingMap

        // create goto and add to map
        // find the last repetition start marker before or at the date of this
        Nodes ns = sequencingMap.query("descendant::*[local-name()='marker' and attribute::message='repetition start']");           // get all repetition start markers
        Element repetitionStartMarker = null;                                                                                       // here comes the one we are looking for
        for (int i=ns.size()-1; i >= 0; --i) {                                                                                      // search all the repetition start markers from back to front so we find the last marker that matches our condition
            Element e = (Element)ns.get(i);                                                                                         // make it an element
            if ((e.getAttribute("date") != null) && (Double.parseDouble(e.getAttributeValue("date")) <= startDate)) {     // does it have a date and is that date before the ending's startDate
                repetitionStartMarker = e;                                                                                          // this is the one we are looking for
                break;                                                                                                              // done
            }
        }
        // find the first ending marker after the repetition start marker
        boolean noPreviousEndings = false;                                                                                          // this will be set true if this is the first ending (requires a special treatment later on)
        double find1stEndingMarkerAfterThisDate = (repetitionStartMarker == null) ? 0.0 : Double.parseDouble(repetitionStartMarker.getAttributeValue("date")); // if we found a repetition start marker get its date, otherwise the date is 0.0
        Nodes ends = sequencingMap.query("descendant::*[local-name()='marker' and contains(attribute::message, 'ending')]");        // get all ending markers
        double dateOfGoto = Double.MAX_VALUE;                                                                                       // this will be filled with something meaningfull throughout the following lines
        for (int i=0; i < ends.size(); ++i) {                                                                                       // go through all ending markers
            Element end = (Element)ends.get(i);                                                                                     // make it an element
            if (((repetitionStartMarker != null) && (end.getParent().indexOf(end) < end.getParent().indexOf(repetitionStartMarker)))// if the ending marker is before the repetition start marker, it cannot be the one we are looking for
                    || (end.getAttribute("date") == null)) {                                                                   // or if the element has no date, it is ignored
                continue;                                                                                                           // so continue with the next
            }
            if (end == marker) {                                                                                                    // if we found the marker that we just created some lines above, this is the first ending
                noPreviousEndings = true;                                                                                           // set the respective flag
                dateOfGoto = startDate;                                                                                             // set the date to the startDate of the ending
                break;                                                                                                              // we are done here
            }
            double firstEndingMarkerDate = Double.parseDouble(end.getAttributeValue("date"));                                  // get the ending's date
            if (firstEndingMarkerDate >= find1stEndingMarkerAfterThisDate) {                                                        // if the ending marker's date is at or after the repetition start marker, we found it
                dateOfGoto = firstEndingMarkerDate;                                                                                 // put the date of the ending marker into variable dateOfGoto
                break;                                                                                                              // done
            }
        }
        // generate the goto element
        Goto gotoObj = new Goto(dateOfGoto, startDate, markerId, "0"+activity, null);                                               // create a Goto object
        Element gt = gotoObj.toElement();                                                                                           // make an XML element from it
        gt.addAttribute(new Attribute("n", Integer.toString(n)));                                                                   // add the numbering ()temporary, will be deleted during msmCleanup)

        // add the goto to the global sequencingMap and try to take care of the order according to the numbering of the endings (on the basis of mei attribute n)
        if (n == Integer.MIN_VALUE)                                                                                                 // if no meaningful ending number was found
            Helper.addToMap(gt, sequencingMap);                                                                                     // simply add it to the global sequencingMap after other gotos that might be there at the same date
        else {                                                                                                                      // otherwise there is a meaningful numbering and we try to insert the goto
            Nodes gotosAtSameDate = sequencingMap.query("descendant::*[local-name()='goto' and attribute::date='" + gotoObj.date + "']");  // get all gotos at the same date as the new goto
            if (gotosAtSameDate.size() == 0) {                                                                                      // if it is the first ending
                gt.addAttribute(new Attribute("first", "true"));                                                                    // this temporary attribute indicates that this goto is from the first ending and should be deleted if other endings follow
                gt.getAttribute("target.id").setValue("");                                                                          // there is no marker at the end of this ending and the targetDate will be known after the children of this ending are processed
                Helper.addToMap(gt, sequencingMap);                                                                                 // simply add to the map, it is the first element, so no order to take care of
            }
            else {                                                                                                                  // there are already other gotos
                int index;
                for (index=0; index < gotosAtSameDate.size(); ++index) {                                                            // go through all the gotos at the same date
                    Element gtast = (Element)gotosAtSameDate.get(index);                                                            // make it an Element
                    if (gtast.getAttribute("n") == null) continue;                                                                  // continue if it has no n attribute
                    if (Integer.parseInt(gtast.getAttributeValue("n")) > n) break;                                                  // if the goto's n i larger than the new goto's number, we found the one in front of which we add the new goto
                }
                if (index == 0) gt.getAttribute("activity").setValue(activity);                                                     // if the insertion would be before the first goto, this goto is immediately active
                Element firstGoto = (Element)gotosAtSameDate.get(0);                                                                // get the first goto
                if (index >= gotosAtSameDate.size()) Helper.addToMap(gt, sequencingMap);                                            // if the index is after the last goto at the dame date, we cann simply add the new goto at the end
                else sequencingMap.insertChild(gt, sequencingMap.indexOf((gotosAtSameDate.size() == 0) ? marker : gotosAtSameDate.get(index)));  // otherwise insert the new goto at its respective position inbetween
                if (firstGoto.getAttribute("first") != null) sequencingMap.removeChild(firstGoto);                                  // in any case, if the first goto is a first ending's goto, remove it

            }
        }

        this.convert(ending);   // process everything within the ending

        if (noPreviousEndings)  // if this was the first ending, it might be that no further ending will follow; however, this first ending should be left out at repetition; so we create a preliminary goto that does exactly this and should be removed if other endings follow later on
            gt.getAttribute("target.date").setValue(this.helper.getMidiTimeAsString());
    }

    /**
     * process MEI phrase elements
     * @param phrase
     */
    private void processPhrase(Element phrase) {
        // compute the timing or get the necessary data to compute the end date later on
        ArrayList<Object> timingData = this.helper.computeControlEventTiming(phrase);
        if (timingData == null)                                                                                 // if the event has been repositioned in accordance to a startid attribute
            return;                                                                                             // stop processing it right now
        Double date = (Double) timingData.get(0);
        Double endDate = (Double) timingData.get(1);
        Attribute tstamp2 = (Attribute) timingData.get(2);
        Attribute endid = (Attribute) timingData.get(3);

        String[] staffs;
        Attribute att = phrase.getAttribute("part");                                                            // get the part attribute (MEI 4.0, https://github.com/music-encoding/music-encoding/issues/435)
        if (att == null)                                                                                        // if no part attribute
            att = phrase.getAttribute("staff");                                                                 // find the staffs that this is associated to
        if ((att == null) || att.getValue().isEmpty() || att.getValue().equals("%all")) {                       // if no part or staff association is defined treat it as a global instruction
            Element phraseMapEntry = new Element("phrase");                                                     // create a phrase element
            phraseMapEntry.addAttribute(new Attribute("date", date.toString()));                                // give it a date attribute

            if (phrase.getAttribute("label") != null)                                                           // if the phrase has a label
                phraseMapEntry.addAttribute(new Attribute("label", phrase.getAttributeValue("label")));         // store it also in the MSM phrase
            else if (phrase.getAttribute("n") != null)                                                          // or if it has an n attribute
                phraseMapEntry.addAttribute(new Attribute("label", phrase.getAttributeValue("n")));             // take this as label

            Helper.copyId(phrase, phraseMapEntry);                                                              // copy the xml:id

            if (endDate != null) {
                phraseMapEntry.addAttribute(new Attribute("date.end", endDate.toString()));                     // add the date.end attribute to the element
            } else if (tstamp2 != null) {                                                                       // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
                phraseMapEntry.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));                      // add the tstamp2 attribute to the element (must be deleted later!)
                this.helper.tstamp2s.add(phraseMapEntry);                                                       // add the element to the helper's tstamp2s list
            } else if (endid != null) {                                                                         // if this phrase element has to be terminated with at an endid-referenced element
                phraseMapEntry.addAttribute(new Attribute("endid", endid.getValue()));                          // add the endid attribute to the element (must be deleted later!)
                this.helper.endids.add(phraseMapEntry);                                                         // add the element to the helper's endids list
            }

            Element phraseMap = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("phraseMap"); // find the global phraseMap (there is no local phraseMap as this cannot be encoded in MEI)
            Helper.addToMap(phraseMapEntry, phraseMap);                                                         // insert it to the map
        }
        else {                                                                                                  // there are staffs, hence, local phrase instruction
            String staffString = att.getValue();
            staffs = staffString.split("\\s+");                                                                 // this creates an array of one or more integer strings (the staff numbers), they are separated by one or more whitespaces

            Elements parts = this.helper.currentMsmMovement.getChildElements("part");
            for (String staff : staffs) {                                                                       // go through all the part numbers
                for (int p = 0; p < parts.size(); ++p) {                                                        // find the corresponding MSM part
                    if (!parts.get(p).getAttributeValue("number").equals(staff))
                        continue;

                    Element phraseMapEntry = new Element("phrase");                                             // create a phrase element
                    phraseMapEntry.addAttribute(new Attribute("date", date.toString()));                        // give it a date attribute

                    if (phrase.getAttribute("label") != null)                                                   // if the phrase has a label
                        phraseMapEntry.addAttribute(new Attribute("label", phrase.getAttributeValue("label"))); // store it also in the MSM phrase
                    else if (phrase.getAttribute("n") != null)                                                  // or if it has an n attribute
                        phraseMapEntry.addAttribute(new Attribute("label", phrase.getAttributeValue("n")));     // take this as label

                    Helper.copyId(phrase, phraseMapEntry);                                                      // copy the xml:id
                    Attribute id = phraseMapEntry.getAttribute("id", "http://www.w3.org/XML/1998/namespace");   // get the id or null if it has none
                    if (id != null) id.setValue("meico_copyId_" + staff + "_" + id.getValue());                 // if it has an xml:id, it would appear twice now; this is not valid, so we have to make a new id

                    if (endDate != null) {
                        phraseMapEntry.addAttribute(new Attribute("date.end", endDate.toString()));             // add the date.end attribute to the element
                    } else if (tstamp2 != null) {                                                               // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
                        phraseMapEntry.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));              // add the tstamp2 attribute to the element (must be deleted later!)
                        this.helper.tstamp2s.add(phraseMapEntry);                                               // add the element to the helper's tstamp2s list
                    } else if (endid != null) {                                                                 // if this phrase element has to be terminated with at an endid-referenced element
                        phraseMapEntry.addAttribute(new Attribute("endid", endid.getValue()));                  // add the endid attribute to the element (must be deleted later!)
                        this.helper.endids.add(phraseMapEntry);                                                 // add the element to the helper's endids list
                    }

                    Element phraseMap = parts.get(p).getFirstChildElement("dated").getFirstChildElement("phraseMap");
                    Helper.addToMap(phraseMapEntry, phraseMap);                                                 // insert it to the map
                    this.helper.addLayerAttribute(phraseMapEntry);                                              // add an attribute that indicates the layer (this will only take effect if the element has a @startid as this will cause the element to be placed within a layer during preprocessing)
                }
            }
        }
    }

    /**
     * process MEI section elements
     * @param section
     */
    private void processSection(Element section) {
        // create an entry in the global sectionMap
        Element sectionMapEntry = new Element("section");                                                   // create a section element
        sectionMapEntry.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));        // give it a date attribute

        if (section.getAttribute("label") != null)                                                          // if the section has a label
            sectionMapEntry.addAttribute(new Attribute("label", section.getAttributeValue("label")));       // store it also in the MSM section
        else if (section.getAttribute("n") != null)                                                         // or if it has an n attribute
                sectionMapEntry.addAttribute(new Attribute("label", section.getAttributeValue("n")));       // take this as label

        Helper.copyId(section, sectionMapEntry);                                                            // copy the xml:id

        Element sectionMap = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("sectionMap");   // find the global sectionMap (there is no local sectionMap as this cannot be encoded in MEI)
        sectionMap.appendChild(sectionMapEntry);                                                            // add the section element to the sectionMap

        this.convert(section);                                                                              // process the contents of this section element

        sectionMapEntry.addAttribute(new Attribute("date.end", this.helper.getMidiTimeAsString()));    // add the date when the section ends (in MEI this is redundant with the date of the succeeding section, but in general section can overlap)
    }

    /**
     * process an mei measure element
     * @param measure an mei measure element
     */
    private void processMeasure(Element measure) {
        double startDate = this.helper.getMidiTime();                                                           // get the date at the beginning of the measure
        measure.addAttribute(new Attribute("date", Double.toString(startDate)));                                // set the measure's date in attribute date

        // compute the duration of this measure
        double dur1 = 0.0;                                                                                      // this will hold the duration of the measure according to the underlying time signature
        if ((this.helper.currentPart != null) && (this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature") != null)) {      // if there is a local time signature map that is not empty
            Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");                                             // get the timeSignature elements
            dur1 = this.helper.computeMeasureLength(Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator")), Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator")));  // compute the measure's (preliminary) length from the time signature
        }
        else if (this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature") != null) {  // if there is a global time signature map
            Elements es = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");       // get the timeSignature elements
            dur1 = this.helper.computeMeasureLength(Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator")), Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator")));  // compute the measure's (preliminary) length from the time signature
        }

        measure.addAttribute(new Attribute("midi.dur", Double.toString(dur1)));                                 // add attribute midi.dur and store the official (time signature defined) duration in it (this will be adapted some lines below if necessary)

        this.helper.currentMeasure = measure;                                                                   // set the state variable currentMeasure to this measure

        // process pending msm/mpm elements with a tstamp2 attribute
        for (int i=0; i < this.helper.tstamp2s.size(); ++i) {                                                   // for all tstamp2 containing elements
            Element e = this.helper.tstamp2s.get(i);                                                            // get the element
            Attribute att = e.getAttribute("tstamp2");                                                          // get its tstamp2 attribute
            String[] tstamp2 = att.getValue().split("m\\+");                                                    // separate measures and position part
            int measures = Integer.parseInt(tstamp2[0]) - 1;                                                    // decrease the measures part of the tstamp2 string
            if (measures <= 0) {                                                                                // we finally arrived at the measure indicated in tstamp2
                double endDate = this.helper.tstampToTicks(tstamp2[1]);                                         // compute the endDate
                e.addAttribute(new Attribute("date.end", Double.toString(endDate)));                            // add new attribute date.end, will be resolved during mpmPostprocessing()
                e.removeAttribute(att);                                                                         // remove the tstamp2 attribute
                this.helper.tstamp2s.remove(i);                                                                 // remove the element from the tstamp2s list
                i--;                                                                                            // make sure we do not miss the succeeding element in the list
            } else {                                                                                            // we have to cross more barlines
                att.setValue(measures + "m+" + tstamp2[1]);                                                     // update the tstamp2 string
            }
        }

        Helper.reorderMeasureContent(measure);                                                                  // shift all control event subtrees to the beginning, all subtrees with staff should come after

        // process the contents of the measure
        this.convert(measure);                                                                                  // process everything within the measure
        this.helper.accid.clear();                                                                              // accidentals are valid within one measure, but not in the subsequent measures, so forget them

        // draw the duration of the measure
        this.helper.currentMeasure = null;                                                                      // this has to be set null so that getMidiTime() does not return the measure's date
        double endDate = this.helper.getMidiTime();                                                             // get the current date
        double dur2 = endDate - startDate;                                                                      // duration of the measure's content (ideally it is equal to the measure duration, but could also be over- or underful)
        boolean durNotTimeSig = false;                                                                          // this will be set true if the measure's duration does not comply the time signature
        if (dur1 >= dur2) {                                                                                     // if the measure is underfull or properly filled
            if ((measure.getAttribute("metcon") != null) && (measure.getAttributeValue("metcon").equals("false"))) {    // if the measure's length does not have to correspond with the time signature
                measure.getAttribute("midi.dur").setValue(Double.toString(dur2));                               // take the duration from the fill state of the measure
                durNotTimeSig = true;
            }
            else {                                                                                              // if the measure has to follow the time signature
                // keep the measures official (time signature defined) length as set some lines above
                endDate = startDate + dur1;                                                                     // in case it is underfull this ensures that the endDate of the measure is in accordance with the time signature and filled up with rests if necessary
            }
        }
        else {                                                                                                  // if the measure is overfull
            measure.getAttribute("midi.dur").setValue(Double.toString(dur2));                                   // take this longer duration as the measure's length
            durNotTimeSig = true;
        }

        // the measure length does not confirm the time signature, hence we need a new time signature entry at the measure's startDate and  another one ate the end so the next measure has the official time signature again
        if (durNotTimeSig) {
            double[] numDenom = this.helper.getCurrentTimeSignature();                                          // get the current time signature numerator and denominator
            double num = (dur2 * numDenom[1]) / (this.helper.ppq * 4.0);                                        // from the actual duration of the measure and the denominator of the time signature compute the new numerator
            Element ts = Msm.makeTimeSignature(startDate, num, (int)numDenom[1], null);                         // generate a new timeSignature element to be added to the global msm timeSignatureMap

            Element tsMap = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap");   // we are global here, so get the global timeSignatureMap
            Elements tss = tsMap.getChildElements();                                                            // get all the child elements in the timeSignatureMap so far
            for (int i = tss.size() - 1; i >= 0; --i) {                                                         // check the timeSignature elements from back to front
                Element t = tss.get(i);                                                                         // get the element
                if (Double.parseDouble(t.getAttributeValue("date")) >= startDate)                               // if its date is at or after the startDate of this measure
                    tsMap.removeChild(t);                                                                       // it conflicts with the new timeSignature that we have to assign here, hence, remove it
            }

            tsMap.appendChild(ts);                                                                              // add the new timeSignature element to the global timeSignatureMap
            Element tsBack = Msm.makeTimeSignature(endDate, numDenom[0], (int)numDenom[1], null);               // generate a timeSignature element to switch back to the official time signature at the end of the measure
            tsMap.appendChild(tsBack);                                                                          // add it to the map
        }

        // go through all msm parts and set the currentDate attribute to endDate
        Elements parts = this.helper.currentMsmMovement.getChildElements("part");                               // get all parts
        for (int i=0; i < parts.size(); ++i)                                                                    // go through all the parts
            parts.get(i).getAttribute("currentDate").setValue(Double.toString(endDate));                        // set their currentDate attribute

        // process barlines (end mark, repetition)
        if (measure.getAttribute("left") != null)                                                               // if the measure has a "left" attribute
            this.helper.barline2SequencingCommand(measure.getAttributeValue("left"), startDate, helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("sequencingMap"));   // create an msm sequencingCommand from this add it to the global sequencingMap
        if (measure.getAttribute("right") != null)                                                              // if the measure has a "right" attribute
            this.helper.barline2SequencingCommand(measure.getAttributeValue("right"), endDate, helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("sequencingMap"));  // create an msm sequencingCommand from this add it to the global sequencingMap
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
            Helper.addToMap(s, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap")); // insert it into the local time signature map
        }
        else {                                          // global entry
            Helper.addToMap(s, this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap"));  // insert it into the global time signature map
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
            Helper.addToMap(s, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("keySignatureMap"));  // insert it into the local key signature map
        }
        else {                                          // global entry
            Helper.addToMap(s, this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("keySignatureMap"));   // insert it into the global key signature map
        }
    }

    /**
     * process accid elements that are not children of notes
     * @param accid an accid element
     */
    private void processAccid(Element accid) {
        // find the parental note if there is one
        Element parentNote = (Element)accid.getParent();                                // the accid might be child of a note, find that note
        for (; (parentNote != null); parentNote = (Element) parentNote.getParent()) {   // check parental nodes to find a note
            if (parentNote.getLocalName().equals("note"))                               // found a note
                break;
            if (parentNote.getLocalName().equals("layer")) {                            // found a layer, stop searching, there is no note
                parentNote = null;
                break;
            }
        }

        Attribute accidAtt = accid.getAttribute("accid");                               // get the accid attribute
        Attribute accidGesAtt = accid.getAttribute("accid.ges");                        // get the accid.ges attribute
        if ((accidAtt == null)) {                                                       // this accid is not visible (hence, it applies only to its parent note)
            if ((accidGesAtt != null)                                                   // but it has a gestural attribute accid.ges
                    && (parentNote != null)                                             // and it has a parent note
                    && (parentNote.getAttribute("accid.ges") == null)) {                // and the parent note has no preexistent accid.ges (if it has one it dominates)
                parentNote.addAttribute(new Attribute("accid.ges", accidGesAtt.getValue()));    // add the accid.ges attribute to the note
            }
            return;                                                                     // done, no need to add this accid to the helper.accid list since it applies only to this parent note or none if no parent note is given
        }

        // determin pitchname
        Attribute ploc = accid.getAttribute("ploc");                                    // get the pitch class
        Attribute oloc = accid.getAttribute("oloc");                                    // get the octave
        String pname = null;
        if (ploc != null) {                                                             // first check for a ploc attribute
            pname = ploc.getValue();                                                    // get its value string
        } else {                                                                        // if no ploc
            if (parentNote != null) {                                                   // is there a parent note?
                if (parentNote.getAttribute("pname") != null) {                         // prefer its pname (untransposed pitch)
                    pname = parentNote.getAttributeValue("pname");                      // get the pname value string
                } else {                                                                // only if the notated/untransposed pname is not available
                    if ((parentNote.getAttribute("pname.ges") != null) && !parentNote.getAttributeValue("pname.ges").equals("none")) {  // try to find a gestural pname
                        pname = parentNote.getAttributeValue("pname.ges");              // get its value string
                    } else {                                                            // if the note did not have a pname either
                        return;                                                         // impossible to assign the accidental to a pitch, the accid is ignored, done
                    }
                }
            }
            else {                                                                      // not parent note
                return;                                                                 // impossible to assign the accidental to a pitch, the accid is ignored, done
            }
        }
        accid.addAttribute(new Attribute("pname", pname));                              // store the ploc/pname value in the pname attribute (compatible with note so it can be processed similarly in Helper.computePitch())


        // determine octave
        String oct = null;
        if (oloc != null) {                                                             // first check for the oloc attribute
            oct = oloc.getValue();                                                      // get its value string
        } else {                                                                        // if no oloc
            if (parentNote != null) {                                                   // is there a parent note?
                if (parentNote.getAttribute("oct") != null) {                           // prefer its oct (untransposed octave)
                    oct = parentNote.getAttributeValue("oct");                          // get its oct value string
                } else {
                    if (parentNote.getAttribute("oct.ges") != null) {                   // try to find a gestural oct
                        oct = parentNote.getAttributeValue("oct.ges");                  // get its value string
                    } else {                                                            // no oct.ges on the note
                        if (this.helper.currentPart != null) {                          // try finding a default octave
                            Elements octs = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("oct.default");                              // get all local default octave
                            if (octs.size() == 0) {                                                                                                                                             // if there is none
                                octs = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getChildElements("oct.default");// get all global default octave
                            }
                            for (int i = octs.size() - 1; i >= 0; --i) {                                                                                                                        // search from back to front
                                if ((octs.get(i).getAttribute("layer") == null) || octs.get(i).getAttributeValue("layer").equals(Helper.getLayerId(Helper.getLayer(accid)))) {                  // for a default octave with no layer dependency or a matching layer
                                    oct = octs.get(i).getAttributeValue("oct.default");                                                                                                         // take this value
                                    break;                                                                                                                                                      // break the for loop
                                }
                            }
                            if (oct == null)                                            // if no octave information was found
                                return;                                                 // this accidental cannot be processed in Helper.computePitch(), so we stop here
                        }
                        else {
                            return;
                        }
                    }
                }
            }
            else {
                return;
            }
        }
        accid.addAttribute(new Attribute("oct", oct));                                  // make an oct attribute and add it to the accidental so it is compatible with note elements an can be processed similarly in Helper.computePitch()

        this.helper.addLayerAttribute(accid);                                           // add an attribute that indicates the layer

        if (accid.getAttribute("accid") != null)                                        // remember this accidental for the rest of the measure only if it is visual, gestural is only for the current note
            this.helper.accid.add(accid);
    }

    /**
    * process MEI dot elements
    * @param dot element
    */
    private void processDot(Element dot) {
        Element parentNote = null;                                                      // this element makes only sense in the context of a note or rest
        for (Element e = (Element)dot.getParent(); (e != null) && !(e.getLocalName().equals("layer")); e = (Element)e.getParent()) { // find the parent note
            if (e.getLocalName().equals("note") || e.getLocalName().equals("rest")) {   // found a note/rest
                parentNote = e;                                                         // keep it in variable parentNote
                break;                                                                  // stop the for loop
            }
        }
        
        if (parentNote == null)                                                         // if no parent note or rest has been found
            return;                                                                     // the meaning of the dot is unclear and it will not be further processed
        
        // add this dot to the childDots counter at the parent note/rest
        Attribute d = parentNote.getAttribute("childDots");
        if (d != null) {                                                                // does the counter attribute exist? if yes
            d.setValue(Integer.toString(1 + Integer.parseInt(d.getValue())));           // add 1 to it
        }
        else                                                                            // otherwise create the attribute
            parentNote.addAttribute(new Attribute("childDots", "1"));                   // and set it to 1
    }

    /** make a part entry in xml/msm from an mei staffDef and insert into movement, if it exists already, return it
     *
     * @param staffDef an mei staffDef element
     * @return an msm part element
     */
    private Element makePart(Element staffDef) {
        Element part = this.helper.getPart(staffDef.getAttributeValue("n"));                                   // search for that part in the xml data created so far

        if (part != null) {                                                                                    // if already in the list
            return part;                                                                                       // return it
        }

        String label = "";
        if (((Element)staffDef.getParent()).getLocalName().equals("staffGrp"))                                 // if there is a staffGrp as parent element
            if (((Element)staffDef.getParent()).getAttribute("label") != null)                                 // and it has a label
                label = ((Element)staffDef.getParent()).getAttributeValue("label");                            // use it in the msm part name
        if (staffDef.getAttribute("label") != null)                                                            // does the staffDef iteself have a name
            label += (label.isEmpty()) ? staffDef.getAttributeValue("label") : " " + staffDef.getAttributeValue("label"); // append it to the label string so far (with a space between staffGrp label and staffDef label)
        else {                                                                                                  // if no attribute label is present
            Element labelElement = Helper.getFirstChildElement("label", staffDef);                              // there could still be a child element named label
            if (labelElement != null) {                                                                         // if so
                label += (label.isEmpty()) ? labelElement.getValue() : " " + labelElement.getValue();           // get its string content
            }
        }

        String number;
        if (staffDef.getAttribute("n") != null) {
            number = staffDef.getAttributeValue("n");                                                           // take the n attribute
        }
        else {                                                                                                  // otherwise generate an id
            number = Integer.toString((-1 * this.helper.currentMsmMovement.getChildElements("part").size()));   // take the number of parts so far and negate it (all generated part numbers will be negative, this should not conflict with the actual part numbers and it is an expressive statement when reading the MSM/MPM later on)
            staffDef.addAttribute(new Attribute("n", number));                                                  // add this number also to the staffDef
        }

        int midiChannel = 0;
        int midiPort = 0;
        Elements ps = this.helper.currentMsmMovement.getChildElements("part");
        if (ps.size() > 0) {
            Element p = ps.get(ps.size()-1);                                                                    // choose last part entry
            midiChannel = (Integer.parseInt(p.getAttributeValue("midi.channel")) + 1) % 16;                     // increment channel counter mod 16
            if ((midiChannel == 9) && this.helper.dontUseChannel10)                                             // if the drum channel should be avoided
                ++midiChannel;                                                                                  // do so
            midiPort = (midiChannel == 0) ? (Integer.parseInt(p.getAttributeValue("midi.port")) + 1) % 256 : Integer.parseInt(p.getAttributeValue("midi.port"));	// increment port counter if channels of previous port are full
        }

        part = Msm.makePart(label, number, midiChannel, midiPort);                                              // create MSM part element

        part.addAttribute(new Attribute("currentDate", (this.helper.currentMeasure != null) ? this.helper.currentMeasure.getAttributeValue("date") : "0.0"));    // set currentDate of processing

        this.helper.currentMsmMovement.appendChild(part);                                                       // insert it into movement

        Part performancePart = Part.createPart(label, Integer.parseInt(number), midiChannel, midiPort);         // create MPM part
        if (performancePart != null)
            this.helper.currentPerformance.addPart(performancePart);                                            // add it to the performance

        return part;
    }

    /**
     * make a time signature entry from an mei scoreDef, staffDef or meterSig element and return it or return null if no sufficient information
     * @param meiSource an mei scoreDef, staffDef or meterSig element
     * @return an msm element for the timeSignatureMap
     */
    protected Element makeTimeSignature(Element meiSource) {
        Element s = new Element("timeSignature");                                                   // create an element
        Helper.copyId(meiSource, s);                                                                // copy the id

        // date of the element
        s.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));                   // compute the date

        // count and unit are preferred in the processing; if not given take sym
        if (((meiSource.getAttribute("count") != null) && (meiSource.getAttribute("unit") != null)) || ((meiSource.getAttribute("meter.count") != null) && (meiSource.getAttribute("meter.unit") != null))) {
            // the meter.count attribute may also be like "2+5.5+3.857"
            String str = (meiSource.getLocalName().equals("meterSig")) ? meiSource.getAttributeValue("count") : meiSource.getAttributeValue("meter.count");
            Double result = 0.0;
            String num = "";
            for (int i = 0; i < str.length(); ++i) {
                if (((str.charAt(i) >= '0') && (str.charAt(i) <= '9')) || (str.charAt(i) == '.')) { // if character is a number/digit or a decimal dot
                    num += str.charAt(i);                                                           // add to num to parse it as double
                    continue;
                }
                // in any other case parse the string in num as a double and begin with a new
                result += (num.isEmpty()) ? 0.0 : Double.parseDouble(num);
                num = "";
            }
            result += (num.isEmpty()) ? 0.0 : Double.parseDouble(num);
            s.addAttribute(new Attribute("numerator", Double.toString(result)));                // store numerator
            s.addAttribute(new Attribute("denominator", (meiSource.getLocalName().equals("meterSig")) ? meiSource.getAttributeValue("unit") : meiSource.getAttributeValue("meter.unit")));        // store denominator
            this.helper.addLayerAttribute(s);                                                   // add an attribute that indicates the layer
            return s;
        }
        else {      // process meter.sym / sym
            if ((meiSource.getAttribute("sym") != null) || (meiSource.getAttribute("meter.sym") != null)) {
                String str = (meiSource.getLocalName().equals("meterSig")) ? meiSource.getAttributeValue("sym") : meiSource.getAttributeValue("meter.sym");
                if (str.equals("common")) {
                    s.addAttribute(new Attribute("numerator", "4"));                        // store numerator
                    s.addAttribute(new Attribute("denominator", "4"));                      // store denominator
                    this.helper.addLayerAttribute(s);                                       // add an attribute that indicates the layer
                    return s;
                } else if (str.equals("cut")) {
                    s.addAttribute(new Attribute("numerator", "2"));                        // store numerator
                    s.addAttribute(new Attribute("denominator", "2"));                      // store denominator
                    this.helper.addLayerAttribute(s);                                       // add an attribute that indicates the layer
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
        s.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));                       // compute date

        LinkedList<Element> accidentals = new LinkedList<>();                                           // create an empty list which will be filled with the accidentals of this key signature

        String sig = "";                                                                                // indicates where the key lies in the circle of fifths, can also be "mixed"
        String mixed = "";                                                                              // the string value of a sig.mixed or key.sig.mixed attribute

        if (meiSource.getLocalName().equals("scoreDef") || meiSource.getLocalName().equals("staffDef")) {   // if meiSource is a scoreDef or staffDef
            // scoreDefs and staffDefs may also contain keySigs, but this will be processed when method convert() dives into them, here, we process only attributes that indicate a key signature
            // read the key signature related attributes
            if (meiSource.getAttribute("key.sig") != null)
                sig = meiSource.getAttributeValue("key.sig");
            else return null;                                                                           // no key.sig attribut means no key signature change, hence, skip
            if (meiSource.getAttribute("key.sig.mixed") != null)
                mixed = meiSource.getAttributeValue("key.sig.mixed");
        }
        else if (meiSource.getLocalName().equals("keySig")) {                                           // if it is a keySig element
            // read the key signature related attributes
            if (meiSource.getAttribute("sig") != null)                                                  // if this attribute is not present meico interprets it as C major and does not skip as it does above (for scoreDefs and staffDefs); and there may of course be some keyAccid children
                sig = meiSource.getAttributeValue("sig");
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
                    System.err.println("No valid value in attribute pname: " + accids.get(i).toXML());  // error message
                    continue;                                                                           // continue with the next keyAccid
                }
                Element accidental = new Element("accidental");                                                                                         // create an accidental element for the msm keySignature
                accidental.addAttribute(new Attribute("midi.pitch", Double.toString(pitch)));                                                           // add the pitch attribute that says which pitch class is affected by the accidental
                accidental.addAttribute(new Attribute("pitchname", accids.get(i).getAttributeValue("pname")));                                          // also store the pitch name, this is easier to read in the msm
                accidental.addAttribute(new Attribute("value", Double.toString(Helper.accidString2decimal(accids.get(i).getAttributeValue("accid"))))); // add the decimal value of the accidental as attribute (+1=sharp, -1=flat, and so on)
                accidentals.add(accidental);                                                                                                            // add it to the accidentals list
            }
        }

        // process sig, accid, pname and mixed to generate msm accidentals from them
        if (accidentals.isEmpty() && !sig.isEmpty()) {                                                  // if the meiSource is a keySig element and had keyAccid children, these overrule the attributes and, hence, the attributes will not be processed, this part will be skipped; same if there is no signature data
            if (sig.equals("mixed")) {                                                                  // process an unorthodox key signature, e.g. "a4 c5ss e5f"
                if (!mixed.isEmpty()) {                                                                 // is there something in the mixed string
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
                        accidental.addAttribute(new Attribute("midi.pitch", Double.toString(pitch)));   // add the pitch attribute that says which pitch class is affected by the accidental
                        accidental.addAttribute(new Attribute("pitchname", ac.substring(0, 1)));        // also store the pitch name, this is easier to read in the msm
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
                        System.err.println("Unknown sig or key.sig attribute value in " + meiSource.toXML() + ". Assume 0 in the further processing.");     // output error message
                }
                // generate msm accidentals and add them to the accidentals list
                String[] acs = (accidCount > 0) ? new String[]{"5.0", "0.0", "7.0", "2.0", "9.0", "4.0", "11.0"} : new String[]{"11.0", "4.0", "9.0", "2.0", "7.0", "0.0", "5.0"};  // the sequence of (midi) pitches to apply the accidentals
                String[] acsn = (accidCount > 0) ? new String[]{"F", "C", "G", "D", "A", "E", "B"} : new String[]{"B", "E", "A", "D", "G", "C", "F"};                               // the sequence of pitches to apply the accidentals
                for (int i=0; i < Math.abs(accidCount); ++i) {                                           // create the accidentals
                    Element accidental = new Element("accidental");                                      // create an accidental element for the msm keySignature
                    accidental.addAttribute(new Attribute("midi.pitch", acs[i]));                        // add the pitch attribute that says which pitch class is affected by the accidental
                    accidental.addAttribute(new Attribute("pitchname", acsn[i]));                        // also store the pitch name, this is easier to read in the msm
                    accidental.addAttribute(new Attribute("value", (accidCount > 0) ? "1.0" : "-1.0"));  // add the decimal value of the accidental as attribute (1=sharp, -1=flat)
                    accidentals.add(accidental);                                                         // add it to the accidentals list
                }
            }
        }

        // add all generated accidentals as children to the msm keySignature element
        for (Element accidental : accidentals) {                                                        // for each accidentals
            s.appendChild(accidental);                                                                  // add to the msm keySignature
        }

        this.helper.addLayerAttribute(s);                                                               // add an attribute that indicates the layer

        return s;                                                                                       // return the msm keySignature element
    }

    /** process an mei chord element; this method is also used to process bTrem and fTrem elements
     *
     * @param chord an mei chord, bTrem or fTrem element
     */
    private void processChord(Element chord) {
        if (this.helper.currentPart == null)                                // if we are not within a part, we don't know where to assign the chord; hence we skip its processing
            return;

        // inherit attributes of the surrounding environment
        if (this.helper.currentChord != null) {                                                                     // if we are already within a chord or bTrem or fTrem environment
            if ((chord.getAttribute("dur") == null) && (this.helper.currentChord.getAttribute("dur") != null)) {    // if duration attribute missing, but there is one in the environment
                chord.addAttribute(new Attribute("dur", this.helper.currentChord.getAttributeValue("dur")));        // take this
            }
            if ((chord.getAttribute("dots") == null) && (this.helper.currentChord.getAttribute("dots") != null)) {  // if dots attribute missing, but there is one in the environment
                chord.addAttribute(new Attribute("dots", this.helper.currentChord.getAttributeValue("dots")));      // take this
            }
        }

        this.helper.checkTies(chord);                                       // check for pending ties

        // make sure that we have a duration for this chord
        double dur = 0.0;                                                   // this holds the duration of the chord
        if (chord.getAttribute("dur") != null) {                            // if the chord has a dur attribute
            dur = this.helper.computeDuration(chord);                       // compute its duration
        }
        else {                                                              // if the dur attribute is missing
            Nodes durs = chord.query("descendant::*[attribute::dur]");      // get all child elements with a dur attribute
            double idur = 0.0;
            for (int i=0; i < durs.size(); ++i) {                           // for each child element with a dur attribute
                idur = this.helper.computeDuration((Element)durs.get(i));   // compute its duration
                if (idur > dur) dur = idur;                                 // if it is longer than the longest duration so far, store this in variable dur
            }
        }

        Element f = this.helper.currentChord;                               // we could already be within a chord or bTrem or fTrem environemnt; this should be stored to return to it afterwards
        this.helper.currentChord = chord;                                   // set the temp.chord pointer to this chord

        this.helper.checkSlurs(chord);                                      // check pending slurs to find out if this chord should be legato articulated

        if (chord.query("descendant::*[local-name()='artic']").size() > 0)  // if this chord has articulation children, these will potentially be relevant to all notes within this chord
            chord.addAttribute(new Attribute("hasArticulations", "true"));  // set a "flag" to signal this to the note processing in method processNote()
        this.processArtic(chord);                                           // if the chord has attributes artic.ges or artic, this method call makes sure these are processed

        this.convert(chord);                                                // process everything within this chord
        this.helper.currentChord = f;                                       // foget the pointer to this chord and return to the surrounding environment or nullptr
        if (this.helper.currentChord == null) {                             // we are done with all chord/bTrem/fTrem environments
            this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString((Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + dur))); // draw currentDate
        }
    }

    /**
     * process an mei tuplet element (requires a dur attribute)
     * @param tuplet
     * @return true (tuplet has a dur attribute), else false
     */
    private boolean processTuplet(Element tuplet) {
        if (tuplet.getAttribute("dur") != null) {
            double cd = Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate"));   // store the current date for use afterwards
            this.convert(tuplet);                                        // process the child elements
            double dur = this.helper.computeDuration(tuplet);
            this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(cd + dur));    // this compensates for numeric problems with the single note durations within the tuplet
            return true;
        }
        return false;
    }

    /**
     * process an mei tupletSpan element; the element MUST be in a staff environment
     * @param tupletSpan an mei tupletSpan element
     */
    private void processTupletSpan(Element tupletSpan) {
        // check validity of information
        if ((tupletSpan.getAttribute("num") == null) || (tupletSpan.getAttribute("numbase") == null)){   // no num or numbase attribute
            System.err.println("Cannot process MEI element " + tupletSpan.toXML() + ". Attributes 'num' and 'numbase' both need to be specified.");
            return;                                                                                      // cancel
        }

        // compute the timing or get the necessary data to compute the end date later on
        ArrayList<Object> timingData = this.helper.computeControlEventTiming(tupletSpan);
        if (timingData == null)                                                                         // if the event has been repositioned in accordance to a startid attribute
            return;                                                                                     // stop processing it right now
        Double date = (Double) timingData.get(0);
        Double endDate = (Double) timingData.get(1);
        Attribute tstamp2 = (Attribute) timingData.get(2);
        Attribute endid = (Attribute) timingData.get(3);

        Attribute att = tupletSpan.getAttribute("part");                                                // get the part attribute (MEI 4.0, https://github.com/music-encoding/music-encoding/issues/435)
        if (att == null)                                                                                // if no part attribute
            att = tupletSpan.getAttribute("staff");                                                     // find the staffs that this is associated to
        if ((att == null) || att.getValue().isEmpty() || att.getValue().equals("%all")) {               // if no part or staff association is defined treat it as a global instruction
            // make a clone of the element and store its tick date
            Element clone = Helper.cloneElement(tupletSpan);
            clone.addAttribute(new Attribute("date", date.toString()));

            if (endDate != null) {
                clone.addAttribute(new Attribute("date.end", endDate.toString()));                      // add the date.end attribute to the element
            } else if (tstamp2 != null) {                                                               // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
                clone.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));                       // add the tstamp2 attribute to the element (must be deleted later!)
                this.helper.tstamp2s.add(clone);                                                        // add the element to the helper's tstamp2s list
            } else if (endid != null) {                                                                 // if this element has to be terminated with an endid-referenced element
//                clone.addAttribute(new Attribute("endid", endid.getValue()));                           // add the endid attribute to the element (must be deleted later!)
                this.helper.endids.add(clone);                                                          // add the element to the helper's endids list
            }

            // add element to the local miscMap/tupletSpanMap; during duration computation (helper.computeDuration()) this map is scanned for applicable entries
            Element tsMap = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap").getFirstChildElement("tupletSpanMap"); // find the global tupletSpanMap
            Helper.addToMap(clone, tsMap);                                                              // insert in global tupletSpanMap
        }
        else {                                                                                          // there are staffs, hence, local octave transposition instruction
            String staffString = att.getValue();
            String[] staffs = staffString.split("\\s+");                                                // this creates an array of one or more integer strings (the staff numbers), they are separated by one or more whitespaces

            Elements parts = this.helper.currentMsmMovement.getChildElements("part");
            for (String staff : staffs) {                                                               // go through all the part numbers
                for (int p = 0; p < parts.size(); ++p) {                                                // find the corresponding MSM part
                    if (!parts.get(p).getAttributeValue("number").equals(staff))
                        continue;

                    // make a clone of the element and store its tick date
                    Element clone = Helper.cloneElement(tupletSpan);
                    clone.addAttribute(new Attribute("date", date.toString()));

                    Attribute id = clone.getAttribute("id", "http://www.w3.org/XML/1998/namespace");    // get the id or null if it has none
                    if (id != null) id.setValue("meico_copyId_" + staff + "_" + id.getValue());         // if it has an xml:id, it would appear twice now; this is not valid, so we have to make a new id

                    if (endDate != null) {
                        clone.addAttribute(new Attribute("date.end", endDate.toString()));              // add the date.end attribute to the element
                    } else if (tstamp2 != null) {                                                       // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
                        clone.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));               // add the tstamp2 attribute to the element (must be deleted later!)
                        this.helper.tstamp2s.add(clone);                                                // add the element to the helper's tstamp2s list
                    } else if (endid != null) {                                                         // if this pedal element has to be terminated with at an endid-referenced element
//                        clone.addAttribute(new Attribute("endid", endid.getValue()));                   // add the endid attribute to the element (must be deleted later!)
                        this.helper.endids.add(clone);                                                  // add the element to the helper's endids list
                    }

                    // add element to the local miscMap/tupletSpanMap; during duration computation (helper.computeDuration()) this map is scanned for applicable entries
                    Element tsMap = parts.get(p).getFirstChildElement("dated").getFirstChildElement("miscMap").getFirstChildElement("tupletSpanMap");
                    Helper.addToMap(clone, tsMap);                                                      // insert in global tupletSpanMap
                    this.helper.addLayerAttribute(clone);                                               // add an attribute that indicates the layer (this will only take effect if the element has a @startid as this will cause the element to be placed within a layer during preprocessing)
                }
            }
        }
    }

    /**
     * process an mei dynam element
     * @param dynam
     */
    private void processDynam(Element dynam) {
        DynamicsData dd = new DynamicsData();

        // parse the instruction
        switch (dynam.getLocalName()) {                                                                     // which kind of dynamics instruction is this?
            case "dynam":                                                                                   // an MEI dynam element (verbal dynamics instruction)
                dd.volumeString = dynam.getValue();                                                               // read the instruction
                if (dd.volumeString.isEmpty()) {                                                                  // if no value/text at this element
                    Attribute label = dynam.getAttribute("label");                                          // try attribute label
                    if (label != null) dd.volumeString = label.getValue();                                        // if there is a label attribute, use its value
                }
                if (dd.volumeString.isEmpty()) {                                                                  // empty instructions cannot be interpreted
                    System.err.println("Cannot process MEI element " + dynam.toXML() + ". No value or label specified.");
                    return;
                }
                if (dd.volumeString.contains("dim") || dd.volumeString.contains("decresc")) {                           // is it a dim or decresc
                    dd.volumeString = "?";                                                                        // mark the volume to be read from the previous instruction
                    dd.transitionToString = "-";                                                                  // mark the transitionTo attribute to be less than the volume and read from the subsequent instruction
                } else if (dd.volumeString.contains("cresc")) {                                                   // is it a cresc
                    dd.volumeString = "?";                                                                        // mark the volume to be read from the previous instruction
                    dd.transitionToString = "+";                                                                  // mark the transitionTo attribute to be greater than the volume and read from the subsequent instruction
                } else {                                                                                    // this instruction might be added to the global styleDef
                    DynamicsStyle dynamicsStyle = (DynamicsStyle) this.helper.currentPerformance.getGlobal().getHeader().getStyleDef(Mpm.DYNAMICS_STYLE, "MEI export"); // get the global dynamicsSyles/styleDef element
                    if (dynamicsStyle == null)                                                                                                                          // if there is none
                        dynamicsStyle = (DynamicsStyle) this.helper.currentPerformance.getGlobal().getHeader().addStyleDef(Mpm.DYNAMICS_STYLE, "MEI export");           // create one

                    if ((dynamicsStyle != null) && (dynamicsStyle.getDynamicsDef(dd.volumeString) == null))       // it is obviously an instantanious dynamics instruction, but if its string is not defined in the global styleDef for dynamics
                        dynamicsStyle.addDynamicsDef(DynamicsDef.createDefaultDynamicsDef(dd.volumeString));      // add it to the styleDef and try to create a default numeric value for the volume literal
                }
                break;
            case "hairpin":                                                                                 // a hairpin (symbolic cresc. or dim. instruction)
                dd.volumeString = "?";
                Attribute form = dynam.getAttribute("form");
                if (form == null) {                                                                         // if the mandatory attribute "form" is missing
                    System.err.println("Cannot process MEI element " + dynam.toXML() + ". Attribute 'form' is missing.");
                    return;                                                                                 // we cannot interpret the instruction, hence, we ignore it
                }
                if (form.getValue().equals("cres"))                                                         // if it is a cresc.
                    dd.transitionToString = "+";
                else if (form.getValue().equals("dim"))                                                     // if it is a decresc.
                    dd.transitionToString = "-";
                else {                                                                                      // if it is unknown
                    System.err.println("Cannot process MEI element " + dynam.toXML() + ". Value of attribute 'form' is neither 'cres' nor 'dim'.");
                    return;
                }
                break;
            default:
                System.err.println("Unknown MEI dynamics instruction " + dynam.toXML() + ".");
                return;
        }

        if (dd.transitionToString != null) {
            dd.curvature = 0.0;
            dd.protraction = 0.0;
        }

        // compute the timing or get the necessary data to compute the end date later on
        ArrayList<Object> timingData = this.helper.computeControlEventTiming(dynam);
        if (timingData == null)                                                                                 // if the event has been repositioned in accordance to a startid attribute
            return;                                                                                             // stop processing it right now
        dd.startDate = (Double) timingData.get(0);
        dd.endDate = (Double) timingData.get(1);
        Attribute tstamp2 = (Attribute) timingData.get(2);
        Attribute endid = (Attribute) timingData.get(3);

        // read the xml:id
        Attribute id = Helper.getAttribute("id", dynam);
        dd.xmlId = (id == null) ? null : id.getValue();

        // parse the staff attribute (space separated staff numbers)
        DynamicsMap dynamicsMap;
        Attribute att = dynam.getAttribute("part");                                                                         // get the part attribute (MEI 4.0, https://github.com/music-encoding/music-encoding/issues/435)
        if (att == null)                                                                                                    // if no part attribute
            att = dynam.getAttribute("staff");                                                                              // find the staffs that this is associated to
        if ((att == null) || att.getValue().isEmpty() || att.getValue().equals("%all")) {                                   // if no part or staff association is defined treat it as a global instruction
            dynamicsMap = (DynamicsMap) this.helper.currentPerformance.getGlobal().getDated().getMap(Mpm.DYNAMICS_MAP);     // get the global dynamicsMap
            if (dynamicsMap == null) {                                                                                      // if there is no global dynamicsMap
                dynamicsMap = (DynamicsMap) this.helper.currentPerformance.getGlobal().getDated().addMap(Mpm.DYNAMICS_MAP); // create one
                dynamicsMap.addStyleSwitch(0.0, "MEI export");                                                              // set its start style reference
            }

            this.addDynamicsToMpm(dd, dynamicsMap, endid, tstamp2);
        }
        else {                                                                                      // there are staffs, hence, local dynamics instruction
            boolean multiIDs = false;
            String staffString = att.getValue();
            String[] staffs = staffString.split("\\s+");                                            // this creates an array of one or more integer strings (the staff numbers), they are separated by one or more whitespaces

            for (String staff : staffs) {                                                           // go through all the part numbers
                Part part = this.helper.currentPerformance.getPart(Integer.parseInt(staff));        // find that part in the performance data structure
                if (part == null)                                                                   // if not found
                    continue;                                                                       // continue with the next

                dynamicsMap = (DynamicsMap) part.getDated().getMap(Mpm.DYNAMICS_MAP);               // get the part's dynamicsMap
                if (dynamicsMap == null) {                                                          // if it has none so far
                    dynamicsMap = (DynamicsMap) part.getDated().addMap(Mpm.DYNAMICS_MAP);           // create it
                    dynamicsMap.addStyleSwitch(0.0, "MEI export");                                  // set the style reference
                }

                DynamicsData ddd = dd.clone();
                if ((dd.xmlId != null) && multiIDs)
                    ddd.xmlId = dd.xmlId + "_meico_" + UUID.randomUUID().toString();

                this.addDynamicsToMpm(ddd, dynamicsMap, endid, tstamp2);

                multiIDs = true;
            }
        }
    }

    /**
     * a helper method to add a dynamics instruction to an MPM dynamicsMap
     * @param dynamicsData
     * @param dynamicsMap
     * @param endid
     * @param tstamp2
     * @return the index at which the instruction has been added to the dynamicsMap
     */
    private int addDynamicsToMpm(DynamicsData dynamicsData, DynamicsMap dynamicsMap, Attribute endid, Attribute tstamp2) {
        ArrayList<KeyValue<Double, Element>> previousDynamics = dynamicsMap.getAllElements();

        // there might be a previously continuous instruction waiting to get a meaningful transition.to value or if this is a continuous instruction that needs to get a meaningful volume value from its predecessor
        for (int i = previousDynamics.size() - 1; i >= 0; --i) {
//            if (!previousDynamics.get(i).getValue().getLocalName().equals("dynamics"))                      // this check is not necessary in this context, MEI-to-MSM/MPM export does not generate style switches
//                continue;
            if (previousDynamics.get(i).getKey() > dynamicsData.startDate)                                  // if the date of this instruction is after the current one
                continue;                                                                                   // search on

            if (dynamicsData.transitionToString == null) {                                                        // if it is an instantaneous dynamics instruction
                Attribute trans = previousDynamics.get(i).getValue().getAttribute("transition.to");         // get the transition.to attribute, if the instruction has one it is a continuous dynamics transition
                if (trans != null)                                                                          // found a previously continuous transition (e.g., cresc. f)
                    trans.setValue(dynamicsData.volumeString);                                                    // set its transition.to to the volume if the new, the subsequent, dynamics instruction
            } else {                                                                                        // if, however, the new instruction to be added is a continuous one (cresc., decresc., dim.), it inherits its volume value from the previous dynamics instruction
                Attribute trans = previousDynamics.get(i).getValue().getAttribute("transition.to");         // if the previous instruction was a continuous one, too (e.g., cresc. decresc.)
                if (trans != null)                                                                          // this new one will start with the dynamics value that the previous one reached at the end
                    dynamicsData.volumeString = trans.getValue();
                else                                                                                        // otherwise it is simply the volume of the previous instruction (e.g. p cresc.)
                    dynamicsData.volumeString = previousDynamics.get(i).getValue().getAttributeValue("volume");
            }
            break;                                                                                          // done, no need to search for further previous dynamics instructions
        }
        if (dynamicsData.volumeString == null)                                                              // no volume found
            dynamicsData.volumeString = "?";                                                                // set placeholder volume

        int index = dynamicsMap.addDynamics(dynamicsData);                                                  // add it to the map
        if (index < 0)
            return index;
        Element dynamics = dynamicsMap.getElement(index);                                                   // get the element just created
        if (dynamicsData.endDate != null) {
            dynamics.addAttribute(new Attribute("date.end", dynamicsData.endDate.toString()));              // add the date.end attribute to the element (will be resolved during mpmPostprocessing())
        } else if (tstamp2 != null) {                                                                       // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
            dynamics.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));                            // add the tstamp2 attribute to the element (must be deleted later!)
            this.helper.tstamp2s.add(dynamics);                                                             // add the element to the helper's tstamp2s list
        } else if (endid != null) {                                                                         // if this dynamics element has to be terminated with at an endid-referenced element
            dynamics.addAttribute(new Attribute("endid", endid.getValue()));                                // add the endid attribute to the element (must be deleted later!)
            this.helper.endids.add(dynamics);                                                               // add the element to the helper's endids list
        }

        return index;
    }

    /**
     * process an MEI tempo element
     * @param tempo
     */
    private void processTempo(Element tempo) {
        TempoData tempoData = this.helper.parseTempo(tempo);                                                        // tempo data to generate an entry in an MPM tempoMap
        if (tempoData == null)
            return;

        // compute the timing or get the necessary data to compute the end date later on
        ArrayList<Object> timingData = this.helper.computeControlEventTiming(tempo);
        if (timingData == null)                                                                                     // if the event has been repositioned in accordance to a startid attribute
            return;                                                                                                 // stop processing it right now
        tempoData.startDate = (Double) timingData.get(0);
        tempoData.endDate = (Double) timingData.get(1);
        Attribute tstamp2 = (Attribute) timingData.get(2);
        Attribute endid = (Attribute) timingData.get(3);

        // parse the staff attribute (space separated staff numbers)
        TempoMap tempoMap;
        Attribute att = tempo.getAttribute("part");                                                                 // get the part attribute (MEI 4.0, https://github.com/music-encoding/music-encoding/issues/435)
        if (att == null)                                                                                            // if no part attribute
            att = tempo.getAttribute("staff");                                                                      // find the staffs that this is associated to
        if ((att == null) || att.getValue().isEmpty() || att.getValue().equals("%all")) {                           // if no part or staff association is defined treat it as a global instruction
            tempoMap = (TempoMap) this.helper.currentPerformance.getGlobal().getDated().getMap(Mpm.TEMPO_MAP);      // get the global tempoMap
            if (tempoMap == null) {                                                                                 // if there is no global tempoMap
                tempoMap = (TempoMap) this.helper.currentPerformance.getGlobal().getDated().addMap(Mpm.TEMPO_MAP);  // create one
                tempoMap.addStyleSwitch(0.0, "MEI export");                                                         // set its start style reference
            }

            // add the new tempo instruction
            this.addTempoToMpm(tempoData, tempoMap, endid, tstamp2);
        }
        else {                                                                                      // there are staffs, hence, local tempo instruction
            boolean multiIDs = false;
            String staffString = att.getValue();
            String[] staffs = staffString.split("\\s+");                                            // this creates an array of one or more integer strings (the staff numbers), they are separated by one or more whitespaces

            for (String staff : staffs) {                                                           // go through all the part numbers
                Part part = this.helper.currentPerformance.getPart(Integer.parseInt(staff));        // find that part in the performance data structure
                if (part == null)                                                                   // if not found
                    continue;                                                                       // continue with the next

                tempoMap = (TempoMap) part.getDated().getMap(Mpm.TEMPO_MAP);                        // get the part's tempoMap
                if (tempoMap == null) {                                                             // if it has none so far
                    tempoMap = (TempoMap) part.getDated().addMap(Mpm.TEMPO_MAP);                    // create it
                    tempoMap.addStyleSwitch(0.0, "MEI export");                                     // set the style reference
                }

                TempoData td = tempoData.clone();
                if ((tempoData.xmlId != null) && multiIDs)
                    td.xmlId = tempoData.xmlId + "_meico_" + UUID.randomUUID().toString();

                // generate and add the new tempo instruction
                this.addTempoToMpm(td, tempoMap, endid, tstamp2);
                multiIDs = true;
            }
        }
    }

    /**
     * a helper method to add a tempo instruction to an MPM tempoMap
     * @param tempoData
     * @param tempoMap
     * @param endid
     * @param tstamp2
     * @return the index at which the instruction has been added to the tempoMap
     */
    private int addTempoToMpm(TempoData tempoData, TempoMap tempoMap, Attribute endid, Attribute tstamp2) {
        ArrayList<KeyValue<Double, Element>> previousTempo = tempoMap.getAllElements();

        // there might be a previously continuous instruction waiting to get a meaningful transition.to value or if this is a continuous instruction that needs to get a meaningful bpm value from its predecessor
        for (int i = previousTempo.size() - 1; i >= 0; --i) {
//            if (!previousTempo.get(i).getValue().getLocalName().equals("tempo"))                  // this check is not necessary in this context, MEI-to-MSM/MPM export does not generate style switches
//                continue;
            if (previousTempo.get(i).getKey() > tempoData.startDate)                                // if the date of this instruction is after the current one
                continue;                                                                           // search on

            if (tempoData.transitionToString == null) {                                             // if it is an instantaneous tempo instruction
                Attribute trans = previousTempo.get(i).getValue().getAttribute("transition.to");    // get the transition.to attribute, if the instruction has one it is a continuous tempo transition
                if (trans != null) {                                                                // found a previously continuous transition (e.g., accel. allegro)
                    trans.setValue(tempoData.bpmString);                                            // set its transition.to to the bpm if the new, the subsequent, bpm instruction
                }
            } else {                                                                                // if, however, the new instruction to be added is a continuous one (accel., rit., rall. ...), it inherits its bpm value from the previous tempo instruction
                Attribute trans = previousTempo.get(i).getValue().getAttribute("transition.to");    // if the previous instruction was a continuous one, too (e.g., accel., rit., rall. ...)
                if (trans != null)                                                                  // this new one will start with the tempo value that the previous one reached at the end
                    tempoData.bpmString = trans.getValue();
                else                                                                                // otherwise it is simply the tempo of the previous instruction (e.g. allegro rit.)
                    tempoData.bpmString = previousTempo.get(i).getValue().getAttributeValue("bpm");
            }
            break;                                                                                  // done, no need to search for further previous tempo instructions
        }

        int index = tempoMap.addTempo(tempoData);                                                   // add it to the map
        Element tempo = tempoMap.getElement(index);                                                 // get the element just created
        if (tempoData.endDate != null) {
            tempo.addAttribute(new Attribute("date.end", tempoData.endDate.toString()));            // add the date.end attribute to the element (will be resolved during mpmPostprocessing())
        } else if (tstamp2 != null) {                                                               // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
            tempo.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));                       // add the tstamp2 attribute to the element (must be deleted later!)
            this.helper.tstamp2s.add(tempo);                                                        // add the element to the helper's tstamp2s list
        } else if (endid != null) {                                                                 // if this tempo element has to be terminated with at an endid-referenced element
            tempo.addAttribute(new Attribute("endid", endid.getValue()));                           // add the endid attribute to the element (must be deleted later!)
            this.helper.endids.add(tempo);                                                          // add the element to the helper's endids list
        }

        return index;
    }

    /**
     * process MEI elements that contain attributes atric, artic.ges and slur,
     * this includes elements artic, note and chord
     * @param artic
     */
    private void processArtic(Element artic) {
        if (this.helper.currentPart == null)                // if we are not within a part, we don't know where to assign the artic; hence we skip its processing
            return;

        Attribute att = artic.getAttribute("artic.ges");    // first try to find the gestural articulation attribute artic.ges
        Attribute slur = artic.getAttribute("slur");
        if (att == null) {                                  // if failed
            att = artic.getAttribute("artic");              // try to find the artic attribute
            if ((att == null) && (slur == null))            // if failed, too, and there is also no slur
                return;                                     // there is no articulation information
        }

        // get the xmlid of the artic, if it has one
        String xmlid = null;
        Attribute articId = Helper.getAttribute("id", artic);
        if (articId != null)
            xmlid = articId.getValue();

        // make sure there is a styleDef in MPM for articulation definitions because MEI uses only descriptors and no numerical specification of articulations
        ArticulationStyle articulationStyle = (ArticulationStyle) this.helper.currentPerformance.getGlobal().getHeader().getStyleDef(Mpm.ARTICULATION_STYLE, "MEI export"); // get the global articulationStyle/styleDef element
        if (articulationStyle == null) {                                                                                                                                    // if there is none
            articulationStyle = (ArticulationStyle) this.helper.currentPerformance.getGlobal().getHeader().addStyleDef(Mpm.ARTICULATION_STYLE, "MEI export");               // create one
            articulationStyle.addArticulationDef(ArticulationDef.createDefaultArticulationDef("nonlegato"));
        }

        // find the local articulationMap
        double date = this.helper.getMidiTime();
        Part part = this.helper.currentPerformance.getPart(Integer.parseInt(this.helper.currentPart.getAttributeValue("number")));  // find the current part in MPM
        ArticulationMap map = (ArticulationMap) part.getDated().getMap(Mpm.ARTICULATION_MAP);                                       // find the local articulationMap
        if (map == null) {                                                                                                          // if there is none
            map = (ArticulationMap) part.getDated().addMap(Mpm.ARTICULATION_MAP);                                                   // create one
            map.addStyleSwitch(0.0, "MEI export", "nonlegato");                                                                     // set its initial style
        }

        for (Element parent = artic; (parent != null) && (parent != this.getRootElement()); parent = (Element) parent.getParent()) {    // search through the parent elements, start with the element itself becuase it could already be the note or chord itself
            if (parent.getLocalName().equals("note")) {                                                                         // found a note
                String noteId = Helper.getAttributeValue("id", parent);                                                         // get its xml:id
                if (noteId.isEmpty()) {                                                                                         // it has no xml:id
                    noteId = "meico_" + UUID.randomUUID().toString();                                                           // generate one
                    parent.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", noteId));               // add it to the note
                }
                if (att != null)
                    this.addArticulationToMap(date, att.getValue(), xmlid, noteId, map, articulationStyle);                     // make articulation entry in the map
                if (slur != null) {                                                                                             // if there is a slur attribute with value i or m
                    String slurid = (artic.getAttribute("slurid") == null) ? null : artic.getAttributeValue("slurid");          // read the xml:id of the slur element that created this slur attribute
                    if (slur.getValue().contains("t"))                                                                          // for a terminal legato
                        this.addArticulationToMap(date, "legatoStop", slurid, noteId, map, articulationStyle);                    // make a legatoStop articulation entry in the map
                    else if (slur.getValue().contains("i") || slur.getValue().contains("m"))                                    // for an initial or medial legato
                        this.addArticulationToMap(date, "legato", slurid, noteId, map, articulationStyle);                        // make a legato articulation entry in the map
                }
                return;                                                                                                         // done
            }
            if (parent.getLocalName().equals("chord")) {                                                                                    // found a chord, this means that the articulation has to be applied to all notes within the chord
                boolean multiIDs = false;
                boolean multiSlurIDs = false;
                // copy the artic to all notes within this chord that are not yet processed, for all others generate the articulation entry in MPM
                Nodes notes = parent.query("descendant::*[local-name()='note']");                                                           // get all note elements within this chord
                for (int i = 0; i < notes.size(); ++i) {                                                                                    // for each note element
                    Element note = (Element) notes.get(i);
                    Nodes subArtics = note.query("descendant::*[local-name()='artic']");                                                    // get its child articulations
                    if ((note.getAttribute("artic") != null) || (note.getAttribute("artic.ges") != null) || (subArtics.size() > 0))         // if the note has local articulation data
                        continue;                                                                                                           // it overwrites the present ones, hence we do not add the present articulation to that note

                    // if the note has @date (a debug attribute generated by meico) it has been processed already and we have to generate the mpm articulation; if not, we just add a copy of the articulation to it and it will be processed as child of the note
                    if (note.getAttribute("date") != null) {                                                                                // this note has already been processed
                        String noteId = Helper.getAttributeValue("id", note);
                        if (att != null) {
                            this.addArticulationToMap(date, att.getValue(), ((xmlid == null) ? null : (xmlid + ((multiIDs) ? ("_meico_" + UUID.randomUUID().toString()) : ""))), noteId, map, articulationStyle);   // make articulation entry in the map with an updated the id to avoid duplicates
                            multiIDs = true;
                        }
                        if (slur != null) {                                                                                                                                                     // if there is a slur attribute with value i or m
                            String slurid = null;
                            if (artic.getAttribute("slurid") != null) {
                                slurid = artic.getAttributeValue("slurid");                                                                                                                     // read the xml:id of the slur element that created this slur attribute
                                note.addAttribute(new Attribute("slurid", (multiSlurIDs) ? slurid + "_meico_" + UUID.randomUUID().toString() : slurid));                                        // add it also to the note
                                multiSlurIDs = true;
                            }
                            if (slur.getValue().contains("t"))                                                                                                                                  // for a terminal legato
                                this.addArticulationToMap(date, "legatoStop", slurid, noteId, map, articulationStyle);                                                                          // make a legatoStop articulation entry in the map
                            else if (slur.getValue().contains("i") || slur.getValue().contains("m"))                                                                                            // for an initial or medial legato
                                this.addArticulationToMap(date, "legato", slurid, noteId, map, articulationStyle);                                                                              // make a legato articulation entry in the map
                        }
                    } else {                                                                                                                                                                    // this note has not yet been processed (or it will never be since it is within a choice, app etc.)
                        if (att != null) {
                            Element newArtic = new Element("artic");                                                                                                                            // create an artic element
                            newArtic.addAttribute(new Attribute(att.getLocalName(), att.getValue()));                                                                                           // the artic element gets the artic.ges or artic attribute of this element
                            if (xmlid != null)                                                                                                                                                  // if it has an xml:id
                                newArtic.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", xmlid + ((multiIDs) ? ("_meico_" + UUID.randomUUID().toString()) : ""))); // add it also to the the copies but with a slightly updated id to avoid duplicats
                            note.appendChild(newArtic);                                                                                                                                         // add it to the note
                            multiIDs = true;
                        }
                        if (slur != null) {                                                                                                                                                     // if there is an attribute slur in the chord
                            note.addAttribute(new Attribute("slur", slur.getValue()));                                                                                                          // add the slur to its note as well
                            if (artic.getAttribute("slurid") != null) {
                                String slurid = artic.getAttributeValue("slurid");                                                                                                              // read the xml:id of the slur element that created this slur attribute
                                note.addAttribute(new Attribute("slurid", (multiSlurIDs) ? slurid + "_meico_" + UUID.randomUUID().toString() : slurid));                                        // add it also to the note
                                multiSlurIDs = true;
                            }
                        }
                    }
                }
                return;                                                                                                                     // done
            }
            if (((parent == this.helper.currentLayer) || parent.getLocalName().equals("staff") || (parent == this.helper.currentMeasure))   // the articulation cannot be associated with any meaningful element
                    && (att != null)) {
                this.addArticulationToMap(date, att.getValue(), xmlid, null, map, articulationStyle);                                       // make articulation entry in the map
                return;                                                                                                                     // done
            }
        }
    }

    /**
     * a helper method to insert an articulation in MPM's articulationMap and styleDef
     * @param date
     * @param articulation
     * @param noteid
     * @param articulationMap
     * @param articulationStyle
     */
    private void addArticulationToMap(double date, String articulation, String id, String noteid, ArticulationMap articulationMap, ArticulationStyle articulationStyle) {
        String[] articulations = articulation.trim().split("\\s+");                 // get all articulation specifiers as individual strings

        for (String artic : articulations) {
            if (articulationStyle.getArticulationDef(artic) == null) {
                ArticulationDef def = ArticulationDef.createDefaultArticulationDef(artic);
                if (def == null) {
                    System.err.println("Failed to generate articulationDef for \"" + artic + "\".");
                    continue;
                }
                articulationStyle.addArticulationDef(def);
            }
            articulationMap.addArticulation(date, artic, "#" + noteid, id);     // generate an articulation for the given id at the given date and with the specific descriptor
        }
    }

    /**
     * process an MEI breath element
     * @param breath
     */
    private void processBreath(Element breath) {
        if (this.helper.currentMeasure == null)                                     // make sure we are in a measure environment
            return;

        // get the xmlid of the artic, if it has one
        String xmlid = null;
        Attribute id = Helper.getAttribute("id", breath);
        if (id != null)
            xmlid = id.getValue();

        // the breath must specify the notes/chords that precede the breath and are, thus, articulated/shortened by it
        String[] prevs = null;
        Attribute att = breath.getAttribute("prev");                                // attribute prev is MEI 4.0
        if (att == null) {
            att = breath.getAttribute("follows");                                   // attribute follows is MEI 3.0 and 4.0
            if (att == null) {
                att = breath.getAttribute("startid");                               // attribute startid should not point to the successor but predecessor of the breath!
                if (att == null) {
                    att = breath.getAttribute("tstamp.ges");                        // try tstamp.ges
                    if (att == null) {
                        att = breath.getAttribute("tstamp");                        // try tstamp
                        if (att == null) {                                          // if nothing was found
                            System.err.println("Cannot process MEI element " + breath.toXML() + ". At least one of the attributes 'prev', 'follows' or 'startid' should be specified to indicate the preceding notes or chords affected by the breath. Alternatively, but not recommended(!), attribute 'tstamp.ges' or 'tstamp' may be defined at the risk that the breath does not coincide with a note's date and will, thus, have no effect on the music.");
                            return;                                                 // we give up
                        }
                    }

                    // create the articulation from tstamp/tstamp.ges including the risk that it does not fall on a note's date and will, thus, have no effect on the music
                    System.out.println("MEI element " + breath.toXML() + " is not associated with a note or chord. If its 'tstamp.ges' or 'tstamp' does not coincied with a note it will have no effect on the musc!");
                    double date = this.helper.tstampToTicks(att.getValue());                                            // compute the midi date of the instruction from tstamp

                    // make sure there is a styleDef in MPM for articulation definitions because MEI uses only descriptors an no numerical specification of articulations
                    ArticulationStyle articulationStyle = (ArticulationStyle) this.helper.currentPerformance.getGlobal().getHeader().getStyleDef(Mpm.ARTICULATION_STYLE, "MEI export"); // get the global articulationStyle/styleDef element
                    if (articulationStyle == null) {                                                                                                                                    // if there is none
                        articulationStyle = (ArticulationStyle) this.helper.currentPerformance.getGlobal().getHeader().addStyleDef(Mpm.ARTICULATION_STYLE, "MEI export");               // create one
                        articulationStyle.getArticulationDef("defaultArticulation");                                                                                                              // the articulation style should define a defauult articulation which is here called defaultArticulation
                    }

                    // find or generate the required articulationMaps and generate and insert the articulation instruction there
                    ArticulationMap articulationMap;
                    att = breath.getAttribute("part");                                                                  // get the part attribute (MEI 4.0, https://github.com/music-encoding/music-encoding/issues/435)
                    if (att == null)                                                                                    // if no part attribute
                        att = breath.getAttribute("staff");                                                             // find the staffs that this is associated to
                    if ((att == null) || att.getValue().isEmpty() || att.getValue().equals("%all")) {                   // if no part or staff association is defined treat it as a global instruction
                        articulationMap = (ArticulationMap) this.helper.currentPerformance.getGlobal().getDated().getMap(Mpm.ARTICULATION_MAP);     // get the global articulationMap
                        if (articulationMap == null) {                                                                                              // if there is no global articulationMap
                            articulationMap = (ArticulationMap) this.helper.currentPerformance.getGlobal().getDated().addMap(Mpm.ARTICULATION_MAP); // create one
                            articulationMap.addStyleSwitch(0.0, "MEI export", "defaultArticulation");                   // set its start style reference
                        }
                        this.addArticulationToMap(date, "breath", xmlid, null, articulationMap, articulationStyle);     // add the new articulation instruction
                    }
                    else {                                                                                              // there are staffs, hence, local articulation instruction
                        String staffString = att.getValue();
                        String[] staffs = staffString.split("\\s+");                                                    // this creates an array of one or more integer strings (the staff numbers), they are separated by one or more whitespaces
                        boolean multiIds = false;

                        for (String staff : staffs) {                                                                   // go through all the part numbers
                            Part part = this.helper.currentPerformance.getPart(Integer.parseInt(staff));                // find that part in the performance data structure
                            if (part == null)                                                                           // if not found
                                continue;                                                                               // continue with the next

                            articulationMap = (ArticulationMap) part.getDated().getMap(Mpm.ARTICULATION_MAP);           // get the part's articulationMap
                            if (articulationMap == null) {                                                              // if it has none so far
                                articulationMap = (ArticulationMap) part.getDated().addMap(Mpm.ARTICULATION_MAP);       // create it
                                articulationMap.addStyleSwitch(0.0, "MEI export", "defaultArticulation");               // set the style reference
                            }
                            this.addArticulationToMap(date, "breath", (xmlid == null) ? null :  ((multiIds) ? (xmlid + "_meico_" + UUID.randomUUID().toString()) : xmlid), null, articulationMap, articulationStyle); // generate and add the new articulation instruction
                            multiIds = true;
                        }
                    }
                    return;                                                                                             // done
                }
            }
        }
        prevs = att.getValue().trim().replace("#", "").split("\\s+");                                                   // get all ids of the notes/chords

        // create breath articulations in MEI and add them tho the notes/chords indicated by their ids
        boolean multiIds = false;
        for (String prev : prevs) {                                         // for all ids
            Element note = this.helper.allNotesAndChords.get(prev);
            if (note != null) {                                             // if there is one
                Element artic = new Element("artic");                       // create an artic element
                artic.addAttribute(new Attribute("artic.ges", "breath"));   // with articulation instruction "breath"
                if (xmlid != null) {                                        // and xml:id if it has one
                    artic.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", ((multiIds) ? (xmlid + "_meico_" + UUID.randomUUID().toString()) : xmlid)));    // if multiple articulations are generated, avoid equal ids
                    multiIds = true;
                }
                note.appendChild(artic);                                    // and add it to the note so it will be processed as an articulation later on
            }
        }
    }

    /**
     * process MEI tie elements
     * @param tie
     */
    private void processTie(Element tie) {
        if ((this.helper.currentMeasure == null)                // we process ties only when they are in a measure environment
                || (tie.getAttribute("startid") == null)        // and have a startid
                || (tie.getAttribute("endid") == null))         // and have an endid
            return;

        // find the startid note and set its tie attribute
        Element note = this.helper.allNotesAndChords.get(tie.getAttributeValue("startid").trim().replace("#", ""));
        if (note != null) {
            Attribute a = note.getAttribute("tie");             // get its tie attribute if it has one
            if (a != null) {                                    // if the note has already a tie attribute
                if (a.getValue().equals("t"))                   // but it says that the tie ends here
                    a.setValue("m");                            // make an intermediate tie out of it
                else if (a.getValue().equals("n"))              // but it says "no tie"
                    a.setValue("i");                            // make an initial tie out of it
            }
            else {                                              // otherwise the element had no tie attribute
                note.addAttribute(new Attribute("tie", "i"));   // hence, we add an initial tie attribute
            }

            this.helper.ties.add(tie);                          // add the tie to the ties list
        }
    }

    /**
     * process MEI slur elements
     * @param slur
     */
    private void processSlur(Element slur) {
        if (this.helper.currentMeasure == null)                                                         // we process slurs only when they are in a measure environment
            return;

        // get the xmlid of the slur, if it has one
        String xmlid = null;
        Attribute id = Helper.getAttribute("id", slur);
        if (id != null)
            xmlid = id.getValue();

        // if a plist attribute is specified with all the notes/chords that are affected by the slur, we take this as the basis for adding the corresponding slur attributes to them
        Attribute plistAtt = slur.getAttribute("plist");
        if (plistAtt != null) {
            // make sure that startid is in the plist
            if (slur.getAttribute("startid") != null) {
                String startid = slur.getAttributeValue("startid");
                if (!plistAtt.getValue().contains(startid))
                    plistAtt.setValue(startid + " " + plistAtt.getValue());
            }

            // make sure that endid is in the plist
            if (slur.getAttribute("endid") != null) {
                String endid = slur.getAttributeValue("endid");
                if (!plistAtt.getValue().contains(endid))
                    plistAtt.setValue(plistAtt.getValue() + " " + endid);
            }

            String[] plist = plistAtt.getValue().trim().replace("#", "").split("\\s+");                 // get all ids of the notes/chords
            boolean multiIds = false;

            for (int i = plist.length - 2; i >= 0; --i) {                                               // for all ids in the plist except for the last one (the end of the legato bow is not played legato)
//                Element note = notes.get(plist[i]);                                                     // find the corresponding note/chord in the HashMap
                Element note = this.helper.allNotesAndChords.get(plist[i]);
                if (note != null) {                                                                     // if there is one
                    note.addAttribute(new Attribute("slur", "im"));                                     // give it a slur attribute
                    if (xmlid != null) {
                        note.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", ((multiIds) ? xmlid + "_meico_" + UUID.randomUUID().toString() : xmlid)));
                        multiIds = true;
                    }
                }
            }

            if (plist.length > 2) {
//                Element note = notes.get(plist[plist.length-1]);                                        // find the last entry in the plist
                Element note = this.helper.allNotesAndChords.get(plist[plist.length-1]);
                if (note != null) {                                                                     // if there is one
                    note.addAttribute(new Attribute("slur", "t"));                                      // give it a terminal slur
                    if (xmlid != null) {
                        note.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", ((multiIds) ? xmlid + "_meico_" + UUID.randomUUID().toString() : xmlid)));
                    }
                }
            }
            return;                                                                                     // done
        }

        // compute the timing or get the necessary data to compute the end date later on
        ArrayList<Object> timingData = this.helper.computeControlEventTiming(slur);
        if (timingData == null)                                                                         // if the event has been repositioned in accordance to a startid attribute
            return;                                                                                     // stop processing it right now
        Double date = (Double) timingData.get(0);
        Double endDate = (Double) timingData.get(1);
        Attribute tstamp2 = (Attribute) timingData.get(2);
        Attribute endid = (Attribute) timingData.get(3);
        Attribute startid = slur.getAttribute("startid");

        // check whether startid and endid are in the same staff and layer
        String staffId = "";
        String layerId = "";
        if ((startid != null) && (endid != null)) {
            if (slur.getAttribute("staff") == null) {
                staffId = this.helper.isSameStaff(startid.getValue(), endid.getValue());
                if (!staffId.isEmpty())
                    slur.addAttribute(new Attribute("staff", staffId));
            }
            if ((slur.getAttribute("staff") != null) && (slur.getAttribute("layer") == null)) {         // looking for the layer makes only sense if we are in a specific staff
                layerId = this.helper.isSameLayer(startid.getValue(), endid.getValue());
                if (!layerId.isEmpty())
                    slur.addAttribute(new Attribute("layer", layerId));
            }
        }

        // process: slur element to slur attribute
        Attribute att = slur.getAttribute("part");                                                      // get the part attribute (MEI 4.0, https://github.com/music-encoding/music-encoding/issues/435)
        if (att == null)                                                                                // if no part attribute
            att = slur.getAttribute("staff");                                                           // find the staffs that this is associated to
        if ((att == null) || att.getValue().isEmpty() || att.getValue().equals("%all")) {               // if no part or staff association is defined treat it as a global instruction
            Element slurMisc = new Element("slur");                                                     // create a slur element
            slurMisc.addAttribute(new Attribute("date", date.toString()));                              // give it a date attribute
            Helper.copyId(slur, slurMisc);                                                              // copy the xml:id

            if (endid != null) {                                                                        // if this element has to be terminated with an endid-referenced element
                slurMisc.addAttribute(new Attribute("endid", endid.getValue()));                        // add the endid attribute to the element (must be deleted later!)
                this.helper.endids.add(slurMisc);
            }

            if (endDate != null)                                                                        // if there is an endDate known
                slurMisc.addAttribute(new Attribute("date.end", endDate.toString()));                   // add the date.end attribute to the element

            if (tstamp2 != null) {                                                                      // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
                slurMisc.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));                    // add the tstamp2 attribute to the element (must be deleted later!)
                this.helper.tstamp2s.add(slurMisc);
            }

            Element miscMap = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap"); // find the global miscMap
            Helper.addToMap(slurMisc, miscMap);                                                         // insert in global miscMap
        }
        else {
            // there are staffs, hence, local slur
            String staffString = att.getValue();
            String[] staffs = staffString.split("\\s+");                                                // this creates an array of one or more integer strings (the staff numbers), they are separated by one or more whitespaces
            Elements parts = this.helper.currentMsmMovement.getChildElements("part");
            boolean multiIds = false;

            for (String staff : staffs) {                                                               // go through all the part numbers
                for (int p = 0; p < parts.size(); ++p) {                                                // find the corresponding MSM part
                    if (!parts.get(p).getAttributeValue("number").equals(staff))
                        continue;

                    Element slurMisc = new Element("slur");                                             // create a slur element
                    slurMisc.addAttribute(new Attribute("date", date.toString()));                      // give it a date attribute
                    if (xmlid != null) {
                        slurMisc.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", ((multiIds) ? xmlid + "_meico_" + UUID.randomUUID().toString() : xmlid)));
                        multiIds = true;
                    }

                    slurMisc.addAttribute(new Attribute("staff", staff));

                    if (!layerId.isEmpty())
                        slurMisc.addAttribute(new Attribute("layer", layerId));

                    if (endid != null) {                                                                // if this element has to be terminated with an endid-referenced element
                        slurMisc.addAttribute(new Attribute("endid", endid.getValue()));                // add the endid attribute to the element (must be deleted later!)
                        this.helper.endids.add(slurMisc);
                    }

                    if (endDate != null)
                        slurMisc.addAttribute(new Attribute("date.end", endDate.toString()));           // add the date.end attribute to the element

                    if (tstamp2 != null) {                                                              // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
                        slurMisc.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));            // add the tstamp2 attribute to the element (must be deleted later!)
                        this.helper.tstamp2s.add(slurMisc);
                    }

                    Element miscMap = parts.get(p).getFirstChildElement("dated").getFirstChildElement("miscMap");
                    Helper.addToMap(slurMisc, miscMap);
                }
            }
        }
    }

    /**
     * process an mei reh element (rehearsal mark)
     * @param reh an mei reh element
     */
    private void processReh(Element reh) {
        // global or local?
        Element markerMap = (this.helper.currentPart == null) ? null : this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("markerMap");                                     // choose local markerMap
        if (markerMap == null)                                                                                                                                                                      // if outside a local scope
            markerMap = (this.helper.currentMsmMovement == null) ? null : this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("markerMap");  // choose global markerMap
        if (markerMap == null)                                                                                                                                                                      // if outside a movement scope
            return;                                                                                                                                                                                 // that marker cannot be put anywere, cancel

        // create marker element
        Element marker = new Element("marker");
        Helper.copyId(reh, marker);                                                                     // copy a possibly present xml:id
        marker.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));                  // store the date of the element
        marker.addAttribute(new Attribute("message", reh.getValue()));                                  // store its text or empty string
        this.helper.addLayerAttribute(marker);                                                          // add an attribute that indicates the layer

        Helper.addToMap(marker, markerMap);     // add to the markerMap
    }

    /** process an mei beatRpt element
     *
     * @param beatRpt an mei beatRpt element
     */
    private void processBeatRpt(Element beatRpt) {
        // get the value of one beat from the local or global timeSignatureMap
        Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
        if (es.size() == 0) {                                                                                                       // if local map empty
            es = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature"); // get global entries
        }

        double beatLength = (es.size() == 0) ? 4 : Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator"));        // store the denominator value; if still no time signature information, one beat is 1/4 by default
        beatLength = (4.0 * this.helper.ppq) / beatLength;                                                                          // compute the length of one beat in midi ticks

        this.processRepeat(beatLength);
    }

    /**
     * process an mei mRpt elemnet
     * @param mRpt an mei mRpt elemnet
     */
    private void processMRpt(Element mRpt) {
        this.processRepeat(this.helper.getOneMeasureLength());
    }


    /**
     * process an mei mRpt2 element
     * @param mRpt2 an mei mRpt2 element
     */
    private void processMRpt2(Element mRpt2) {
        double timeframe = this.helper.getOneMeasureLength();

        // get the value of one measure from the local or global timeSignatureMap
        Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
        if (es.size() == 0) {                                                       // if local map empty
            es = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature"); // get global entries
        }

        // check the timeSignatureMap for time signature changes between this and the previous measure
        if (es.size() != 0) {                                                       // this check is only possible if there is time signature information
            if ((this.helper.getMidiTime() - (2.0 * timeframe)) < (Double.parseDouble(es.get(es.size()-1).getAttributeValue("date")))) {    // if the last time signature element is within the timeframe
                Element second = Helper.cloneElement(es.get(es.size()-1));          // get the last time signature element
                Element first;
                if (es.size() < 2) {                                                // if no second to last time signature element exists
                    first = new Element("timeSignature");                           // create one with default time signature 4/4
                    first.addAttribute(new Attribute("numerator", "4"));
                    first.addAttribute(new Attribute("denominator", "4"));
                }
                else {                                                              // otherwise
                    first = Helper.cloneElement(es.get(es.size() - 2));             // get the second to last time signature element
                }
                first.addAttribute(new Attribute("date", this.helper.currentPart.getAttributeValue("currentDate")));  // draw date of first  to currentDate

                // set date of the last time signature element to the beginning of currentDate + 1 measure
                double timeframe2 = (4.0 * this.helper.ppq * Double.parseDouble(first.getAttributeValue("numerator"))) / Double.parseDouble(first.getAttributeValue("denominator"));    // compute the length of one measure of time signature element first
                second.getAttribute("date").setValue(Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + timeframe2));                   // draw date of second time signature element

                // add both instructions to the timeSignatureMap
                Helper.addToMap(first, (Element)es.get(0).getParent());
                Helper.addToMap(second, (Element)es.get(0).getParent());

                timeframe += timeframe2;
            }
        }

        this.processRepeat(timeframe);
    }

    /**
     * process an mei multiRpt element
     * @param multiRpt an mei multiRpt element
     */
    private void processMultiRpt(Element multiRpt) {
        double timeframe = 0;                                                                                                                                                           // here comes the length of the timeframe to be repeated
        double currentDate = this.helper.getMidiTime();
        double measureLength = currentDate - this.helper.getOneMeasureLength();                                                                                                         // length of one measure in ticks

        // get time signature element
        Elements ts = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
        if (ts.size() == 0)                                                                                                                                                             // if local map empty
            ts = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");   // get global entries
        int timesign = ts.size() - 1;                                                                                                                                                   // get index of the last element in ts
        double tsdate = (timesign > 0) ? Double.parseDouble(ts.get(timesign).getAttributeValue("date")) : 0.0;                                                                          // get the date of the current time signature

        // go back measure-wise, check for time signature changes, sum up the measure lengths into the timeframe variable
        for (int measureCount = (multiRpt.getAttribute("num") == null) ? 1 : (int)(Double.parseDouble(multiRpt.getAttributeValue("num"))); measureCount > 0; --measureCount) {          // for each measure
            timeframe += measureLength;                                                                                                                                                 // add its length to the timeframe for repetition
            while (tsdate >= (currentDate - timeframe)) {                                                                                                                               // if we pass the date of the current time signature (and maybe others, too)
                --timesign;                                                                                                                                                             // choose predecessor in the ts list
                tsdate = ((timesign) > 0) ? Double.parseDouble(ts.get(timesign).getAttributeValue("date")) : 0.0;                                                                       // get its date
                measureLength = ((timesign) > 0) ? this.helper.computeMeasureLength(Double.parseDouble(ts.get(timesign).getAttributeValue("numerator")), Double.parseDouble(ts.get(timesign).getAttributeValue("denominator"))) : this.helper.computeMeasureLength(4, 4);   // draw measureLength
            }
        }

        // copy the time signature elements we just passed and append them to the timeSignatureMap
        if (ts.size() != 0) {
            Element tsMap = (Element)ts.get(0).getParent();                                                                                         // get the map
            for(++timesign; timesign < ts.size(); ++timesign) {                                                                                     // go through all time signature elements we just passed
                Element clone = Helper.cloneElement(ts.get(timesign));                                                                              // clone the element
                clone.getAttribute("date").setValue(Double.toString(Double.parseDouble(clone.getAttributeValue("date")) + timeframe));              // draw its date
                Helper.addToMap(clone, tsMap);
            }
        }

        this.processRepeat(timeframe);
    }

    /**
     * process an mei halfmRpt element
     * @param halfmRpt an mei halfmRpt element
     */
    private void processHalfmRpt(Element halfmRpt) {
        this.processRepeat(0.5 * this.helper.getOneMeasureLength());
    }

    /**
     * repeats the material at the end of the score map, attribute timeframe specifies the length of the frame to be repeatetd (in midi ticks)
     * @param timeframe the timeframe to be repeated in midi ticks
     */
    private void processRepeat(double timeframe) {
        if ((this.helper.currentPart == null)                                                                                       // if no part
        || (this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").getChildElements().size() == 0)) {  // or no music data
            return;                                                                                                                 // nothing to repeat, hence, cancel
        }

        double currentDate = Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate"));                          // get the current date
        double startDate = currentDate - timeframe;                                                                                 // compute the date of the beginning of the timeframe to be repeated
        String layer = Helper.getLayerId(this.helper.currentLayer);                                                                 // get the id of the current layer
        Stack<Element> els = new Stack<Element>();

        // go back in the score map, copy all elements with date at and after the last beat, recalculate the date (date += beat value)
        for (Element e = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").getChildElements().get(this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").getChildElements().size()-1); e != null; e = Helper.getPreviousSiblingElement(e)) {
            double date = Double.parseDouble(e.getAttributeValue("date"));                                                          // get date of the element
            if (date < startDate) break;                                                                                            // if all elements from the previous beat were collected, break the for loop
            if (layer.isEmpty() || ((e.getAttribute("layer") != null) && e.getAttributeValue("layer").equals(layer))) {             // if no need to consider layers or the layer of e matches the currentLayer
                Element copy = Helper.cloneElement(e);                                                                              // make a new element
                copy.getAttribute("date").setValue(Double.toString(date + timeframe));                                              // draw its date attribute
                Attribute id = Helper.getAttribute("id", copy);                                                                     // get the id attribute
                if (id != null)                                                                                                     // if the element has an id
                    id.setValue("meico_repeats_" + id.getValue() + "_" + UUID.randomUUID().toString());                             // give it a new unique one of the following form: "meico_repeats_oldID_newUUID"
                els.push(copy);                                                                                                     // push the copy onto the els stack
            }
        }

        // append the elements in the els stack to the score map
        for (; !els.empty(); els.pop()) {
            Helper.addToMap(els.peek(), this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score"));       // append element to score and pop from stack
        }

        this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(currentDate + timeframe));                     // draw currentDate counter
    }


    /**
     * process a complete measure rest in mei, the measure rest MUST be in a staff/layer environment!
     * @param mRest an mei mRest element
     */
    private void processMeasureRest(Element mRest) {
        if (this.helper.currentPart == null) return;                                                    // if we are not within a part, we don't know where to assign the rest; hence we skip its processing

        Element rest = this.makeMeasureRest(mRest);                                                     // make rest element

        if (rest == null)                                                                               // if failed
            return;

        Helper.addToMap(rest, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score"));                     // insert in movement
        this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + Double.parseDouble(rest.getAttributeValue("duration"))));  // draw currentDate
    }

    /**
     * make a rest that lasts a complete measure
     * @param meiMRest an mei measureRest element
     * @return an msm rest element
     */
    private Element makeMeasureRest(Element meiMRest) {
        Element rest = new Element("rest");                             // this is the new rest element
        Helper.copyId(meiMRest, rest);                                  // copy the id
        double dur = 0.0;                                               // its duration

        // compute duration
        if ((this.helper.currentPart != null) && (this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature") != null)) {    // if there is a local time signature map that is not empty
            Elements es = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
            dur = (4.0 * this.helper.ppq * Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator"))) / Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator"));
        }
        else if (this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getFirstChildElement("timeSignature") != null) {   // if there is a global time signature map
            Elements es = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("timeSignatureMap").getChildElements("timeSignature");
            dur = (4.0 * this.helper.ppq * Double.parseDouble(es.get(es.size()-1).getAttributeValue("numerator"))) / Double.parseDouble(es.get(es.size()-1).getAttributeValue("denominator"));
        }
        if (dur == 0.0) {                                               // if duration could not be computed
            return null;                                                // cancel
        }

        rest.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));        // compute date
        rest.addAttribute(new Attribute("duration", Double.toString(dur)));                 // store in rest element
        this.helper.addLayerAttribute(rest);                                                // add an attribute that indicates the layer
        return rest;
    }

    /**
     * make a rest that lasts several measures and insert it into the score
     * @param multiRest an mei multiRest element
     */
    private void processMultiRest(Element multiRest) {
        if (this.helper.currentPart == null) return;                                        // if we are not within a part, we don't know where to assign the rest; hence we skip its processing

        Element rest = this.makeMeasureRest(multiRest);                                     // generate a one measure rest
        if (rest == null) return;                                                           // if failed to create a rest, cancel

        rest.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));        // compute date
        Helper.addToMap(rest, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score")); // insert the rest into the score

        int num = (multiRest.getAttribute("num") == null) ? 1 : Integer.parseInt(multiRest.getAttributeValue("num"));
        if (num > 1)                                                                        // if multiple measures (more than 1)
            rest.getAttribute("duration").setValue(Double.toString(Double.parseDouble(rest.getAttributeValue("duration")) * num));    // rest duration of one measure times the number of measures

        this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + Double.parseDouble(rest.getAttributeValue("duration")))); // draw currentDate counter
    }

    /**
     * process an mei rest element
     * @param rest an mei rest element
     */
    private void processRest(Element rest) {
        Element s = new Element("rest");                                                    // this is the new rest element
        Helper.copyId(rest, s);                                                             // copy the id
        s.addAttribute(new Attribute("date", this.helper.getMidiTimeAsString()));           // compute date

        double dur = this.helper.computeDuration(rest);                                     // compute note duration in midi ticks
        if (dur == 0.0) return;                                                             // if failed, cancel

        s.addAttribute(new Attribute("duration", Double.toString(dur)));                                       // else store attribute
        this.helper.addLayerAttribute(s);                                                                           // add an attribute that indicates the layer
        this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(Double.parseDouble(this.helper.currentPart.getAttributeValue("currentDate")) + dur));  // draw currentDate counter
        Helper.addToMap(s, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score"));    // insert the new note into the part->dated->score

        // this is just for the debugging in mei
        rest.addAttribute(new Attribute("date", s.getAttributeValue("date")));
        rest.addAttribute(new Attribute("midi.dur", s.getAttributeValue("duration")));
    }

    /**
     * process an mei octave element
     * @param octave an mei octave element
     */
    private void processOctave(Element octave) {
        if ((octave.getAttribute("dis") == null) || (octave.getAttribute("dis.place") == null)) {       // if no transposition information
            System.err.println("Cannot process MEI element " + octave.toXML() + ". Missing attribute 'dis' or 'dis.place'.");
            return;                                                                                     // cancel because of insufficient information
        }

//        if ((endDate == null) && (tstamp2 == null) && (endid == null)) {                                // unknown end
//            System.err.println("Cannot process MEI element " + octave.toXML() + ". None of the attributes 'dur', 'tstamp2.ges', 'tstamp2', or 'endid' is given to terminate the octaving.");
//            return;                                                                                     // cancel
//        }

        // compute the amount of transposition in semitones
        double result;
        switch (octave.getAttributeValue("dis")) {
            case "8":  result = 12.0; break;
            case "15": result = 24.0; break;
            case "22": result = 36.0; break;
            default:
                System.err.println("An invalid octave transposition occured (dis=" + octave.getAttributeValue("dis") + ").");
                return;
        }

        // direction of transposition
        if (octave.getAttributeValue("dis.place").equals("below")) {
            result = -result;
        }
        else if (!octave.getAttributeValue("dis.place").equals("above")){
            System.err.println("An invalid octave transposition occured (dis.place=" + octave.getAttributeValue("dis.place") + ").");
            return;
        }

        // compute the timing or get the necessary data to compute the end date later on
        ArrayList<Object> timingData = this.helper.computeControlEventTiming(octave);
        if (timingData == null)                                                                         // if the event has been repositioned in accordance to a startid attribute
            return;                                                                                     // stop processing it right now
        Double date = (Double) timingData.get(0);
        Double endDate = (Double) timingData.get(1);
        Attribute tstamp2 = (Attribute) timingData.get(2);
        Attribute endid = (Attribute) timingData.get(3);

        Attribute att = octave.getAttribute("part");                                                    // get the part attribute (MEI 4.0, https://github.com/music-encoding/music-encoding/issues/435)
        if (att == null)                                                                                // if no part attribute
            att = octave.getAttribute("staff");                                                         // find the staffs that this is associated to
        if ((att == null) || att.getValue().isEmpty() || att.getValue().equals("%all")) {               // if no part or staff association is defined treat it as a global instruction
            Element trans = new Element("addTransposition");                                            // create an addTransposition element
            trans.addAttribute(new Attribute("date", date.toString()));                                 // give it a date attribute
            trans.addAttribute(new Attribute("semi", Double.toString(result)));                         // write the semitone transposition into the element
            Helper.copyId(octave, trans);                                                               // copy the xml:id

            if (endDate != null) {
                trans.addAttribute(new Attribute("date.end", endDate.toString()));                      // add the date.end attribute to the element
            } else if (tstamp2 != null) {                                                               // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
                trans.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));                       // add the tstamp2 attribute to the element (must be deleted later!)
                this.helper.tstamp2s.add(trans);                                                        // add the element to the helper's tstamp2s list
            } else if (endid != null) {                                                                 // if this element has to be terminated with an endid-referenced element
                trans.addAttribute(new Attribute("endid", endid.getValue()));                           // add the endid attribute to the element (must be deleted later!)
                this.helper.endids.add(trans);                                                          // add the element to the helper's endids list
            }

            Element miscMap = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("miscMap"); // find the global miscMap
            Helper.addToMap(trans, miscMap);                                                            // insert in global miscMap
        }
        else {                                                                                          // there are staffs, hence, local octave transposition instruction
            String staffString = att.getValue();
            String[] staffs = staffString.split("\\s+");                                                // this creates an array of one or more integer strings (the staff numbers), they are separated by one or more whitespaces
            boolean multiIDs = false;

            Elements parts = this.helper.currentMsmMovement.getChildElements("part");
            for (String staff : staffs) {                                                               // go through all the part numbers
                for (int p = 0; p < parts.size(); ++p) {                                                // find the corresponding MSM part
                    if (!parts.get(p).getAttributeValue("number").equals(staff))
                        continue;

                    Element trans = new Element("addTransposition");                                    // create a pedal element
                    trans.addAttribute(new Attribute("date", date.toString()));                         // give it a date attribute
                    trans.addAttribute(new Attribute("semi", Double.toString(result)));                 // write the semitone transposition into the element

                    Helper.copyId(octave, trans);                                                       // copy the xml:id
                    Attribute id = trans.getAttribute("id", "http://www.w3.org/XML/1998/namespace");    // get the id or null if it has none
                    if (id != null)
                        id.setValue(id.getValue() + ((multiIDs) ? "_meico_" + UUID.randomUUID().toString() : ""));  // if it has an xml:id, it would appear twice now; this is not valid, so we have to make a new id

                    if (endDate != null) {
                        trans.addAttribute(new Attribute("date.end", endDate.toString()));              // add the date.end attribute to the element
                    } else if (tstamp2 != null) {                                                       // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
                        trans.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));               // add the tstamp2 attribute to the element (must be deleted later!)
                        this.helper.tstamp2s.add(trans);                                                // add the element to the helper's tstamp2s list
                    } else if (endid != null) {                                                         // if this pedal element has to be terminated with at an endid-referenced element
                        trans.addAttribute(new Attribute("endid", endid.getValue()));                   // add the endid attribute to the element (must be deleted later!)
                        this.helper.endids.add(trans);                                                  // add the element to the helper's endids list
                    }

                    Element miscMap = parts.get(p).getFirstChildElement("dated").getFirstChildElement("miscMap");
                    Helper.addToMap(trans, miscMap);
                    this.helper.addLayerAttribute(trans);                                               // add an attribute that indicates the layer (this will only take effect if the element has a @startid as this will cause the element to be placed within a layer during preprocessing)
                    multiIDs = true;
                }
            }
        }
    }

    /**
     * process an mei pedal element
     * @param pedal an mei pedal element
     */
    private void processPedal(Element pedal) {
        if (pedal.getAttribute("dir") == null) {                                                                // if no pedal information}
            System.err.println("Cannot process MEI element " + pedal.toXML() + ". Missing attribute 'dir'.");
            return;                                                                                             // cancel because of insufficient information
        }

        // compute the timing or get the necessary data to compute the end date later on
        ArrayList<Object> timingData = this.helper.computeControlEventTiming(pedal);
        if (timingData == null)                                                                                 // if the event has been repositioned in accordance to a startid attribute
            return;                                                                                             // stop processing it right now
        Double date = (Double) timingData.get(0);
        Double endDate = (Double) timingData.get(1);
        Attribute tstamp2 = (Attribute) timingData.get(2);
        Attribute endid = (Attribute) timingData.get(3);

//        if ((endDate == null) && (tstamp2 == null) && (endid == null)) {                                        // unknown end
//            System.err.println("Cannot process MEI element " + pedal.toXML() + ". None of the attributes 'dur', 'tstamp2.ges', 'tstamp2', or 'endid' is given to terminate the pedal.");
//            return;                                                                                             // cancel
//        }

        Attribute att = pedal.getAttribute("part");                                                             // get the part attribute (MEI 4.0, https://github.com/music-encoding/music-encoding/issues/435)
        if (att == null)                                                                                        // if no part attribute
            att = pedal.getAttribute("staff");                                                                  // find the staffs that this is associated to
        if ((att == null) || att.getValue().isEmpty() || att.getValue().equals("%all")) {                       // if no part or staff association is defined treat it as a global instruction
            Element pedalMapEntry = new Element("pedal");                                                       // create a pedal element
            pedalMapEntry.addAttribute(new Attribute("date", date.toString()));                                 // give it a date attribute
            pedalMapEntry.addAttribute(new Attribute("state", pedal.getAttributeValue("dir")));                 // pedal state can be "down", "up", "half", and "bounce" (release then immediately depress the pedal)
            Helper.copyId(pedal, pedalMapEntry);                                                                // copy the xml:id

            if (endDate != null) {
                pedalMapEntry.addAttribute(new Attribute("date.end", endDate.toString()));                      // add the date.end attribute to the element
            } else if (tstamp2 != null) {                                                                       // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
                pedalMapEntry.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));                       // add the tstamp2 attribute to the element (must be deleted later!)
                this.helper.tstamp2s.add(pedalMapEntry);                                                        // add the element to the helper's tstamp2s list
            } else if (endid != null) {                                                                         // if this pedal element has to be terminated with at an endid-referenced element
                pedalMapEntry.addAttribute(new Attribute("endid", endid.getValue()));                           // add the endid attribute to the element (must be deleted later!)
                this.helper.endids.add(pedalMapEntry);                                                          // add the element to the helper's endids list
            }

            Element pedalMap = this.helper.currentMsmMovement.getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("pedalMap"); // find the global pedalMap
            Helper.addToMap(pedalMapEntry, pedalMap);                                                           // insert in global pedalMap
        }
        else {                                                                                                  // there are staffs, hence, local pedal instruction
            String staffString = att.getValue();
            String[] staffs = staffString.split("\\s+");                                                        // this creates an array of one or more integer strings (the staff numbers), they are separated by one or more whitespaces
            boolean multiIDs = false;

            Elements parts = this.helper.currentMsmMovement.getChildElements("part");
            for (String staff : staffs) {                                                                       // go through all the part numbers
                for (int p = 0; p < parts.size(); ++p) {                                                        // find the corresponding MSM part
                    if (!parts.get(p).getAttributeValue("number").equals(staff))
                        continue;

                    Element pedalMapEntry = new Element("pedal");                                               // create a pedal element
                    pedalMapEntry.addAttribute(new Attribute("date", date.toString()));                         // give it a date attribute
                    pedalMapEntry.addAttribute(new Attribute("state", pedal.getAttributeValue("dir")));         // pedal state can be "down", "up", "half", and "bounce" (release then immediately depress the pedal)

                    Helper.copyId(pedal, pedalMapEntry);                                                        // copy the xml:id
                    Attribute id = pedalMapEntry.getAttribute("id", "http://www.w3.org/XML/1998/namespace");    // get the id or null if it has none
                    if (id != null)
                        id.setValue(id.getValue() + ((multiIDs) ? "_meico_" + UUID.randomUUID().toString() : ""));  // if it has an xml:id, it would appear twice now; this is not valid, so we have to make a new id

                    if (endDate != null) {
                        pedalMapEntry.addAttribute(new Attribute("date.end", endDate.toString()));              // add the date.end attribute to the element
                    } else if (tstamp2 != null) {                                                               // if this element must be terminated in another measure via a tstamp2.ges or tstamp2 attribute
                        pedalMapEntry.addAttribute(new Attribute("tstamp2", tstamp2.getValue()));               // add the tstamp2 attribute to the element (must be deleted later!)
                        this.helper.tstamp2s.add(pedalMapEntry);                                                // add the element to the helper's tstamp2s list
                    } else if (endid != null) {                                                                 // if this pedal element has to be terminated with at an endid-referenced element
                        pedalMapEntry.addAttribute(new Attribute("endid", endid.getValue()));                   // add the endid attribute to the element (must be deleted later!)
                        this.helper.endids.add(pedalMapEntry);                                                  // add the element to the helper's endids list
                    }

                    Element pedalMap = parts.get(p).getFirstChildElement("dated").getFirstChildElement("pedalMap");
                    Helper.addToMap(pedalMapEntry, pedalMap);
                    this.helper.addLayerAttribute(pedalMapEntry);                                               // add an attribute that indicates the layer (this will only take effect if the element has a @startid as this will cause the element to be placed within a layer during preprocessing)
                    multiIDs = true;
                }
            }
        }
    }

    /**
     * process an mei note element
     * @param note an mei note element
     */
    private void processNote(Element note) {
        if (this.helper.currentPart == null)                                    // if we are not within a part, we don't know where to assign the note; hence we skip its processing
            return;

        if ((this.helper.currentChord != null)                                                                                              // if this note is within a chord
                && (this.helper.currentChord.getAttribute("hasArticulations") != null)                                                      // and if that chord contains articulations (the attribute is generate by meico only in this case), these may potentially be relevant to this note
                && (Helper.getAttribute("id", note) == null)) {                                                                             // and if the note has no id, yet (mandatory for associating the articulation with it)
            note.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", "meico_" + UUID.randomUUID().toString()));    // generate one
        }

        this.convert(note);                                                     // look for and process what is in the note (e.g. accid, dot etc.) before

        this.helper.checkSlurs(note);                                           // check pending slurs to find out if this note should be legato articulated

        this.processArtic(note);                                                // if the note has attributes artic.ges or artic, this method call will make sure that the corresponding MPM articulations are generated

        double date = this.helper.getMidiTime();

        Element s = new Element("note");                                        // create a note element
        Helper.copyId(note, s);                                                 // copy the id
        s.addAttribute(new Attribute("date", Double.toString(date)));           // compute the date of the note

        // compute midi pitch
        ArrayList<String> pitchdata = new ArrayList<>();                        // this is to store pitchname, accidentals and octave as additional attributes of the note
        double pitch = this.helper.computePitch(note, pitchdata);               // compute pitch of the note
        if (pitch == -1) return;                                                // if failed, cancel
        s.addAttribute(new Attribute("midi.pitch", Double.toString(pitch)));    // store resulting pitch in the note
        s.addAttribute(new Attribute("pitchname", pitchdata.get(0)));           // store pitchname as additional attribute
        s.addAttribute(new Attribute("accidentals", pitchdata.get(1)));         // store accidentals as additional attribute
        s.addAttribute(new Attribute("octave", pitchdata.get(2)));              // store octave as additional attribute

        if (note.getAttribute("accid") != null) {                               // if the note has a visual accidental
            this.helper.accid.add(note);                                        // remember the accidental for the rest of the measure (only if it is visual, gestural is only for the current note)
        }

        // compute midi duration
        double dur = this.helper.computeDuration(note);                         // compute note duration in midi ticks
        if (dur == 0.0) return;                                                 // if failed, cancel
        s.addAttribute(new Attribute("duration", Double.toString(dur)));

        // draw currentDate counter
        if (this.helper.currentChord == null)                                   // the next instruction must be suppressed in the chord environment
            this.helper.currentPart.getAttribute("currentDate").setValue(Double.toString(date + dur));  // draw currentDate counter

        //adding some attributes to the mei source, this is only for the debugging in mei
        note.addAttribute(new Attribute("pnum", String.valueOf(pitch)));
        note.addAttribute(new Attribute("date", String.valueOf(date)));
        note.addAttribute(new Attribute("midi.dur", String.valueOf(dur)));

        // handle ties
        this.helper.checkTies(note);                                            // check for pending tie elements
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
            case 't': {                                                        // the tie ends here
                Nodes ps = this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score").query("descendant::*[local-name()='note' and @tie]");    // select all preceding msm notes with a tie attribute
                for (int i = ps.size() - 1; i >= 0; --i) {                                                                                                              // check each of them
                    Element p = ((Element) ps.get(i));
                    if (p.getAttributeValue("midi.pitch").equals(s.getAttributeValue("midi.pitch"))                                                                               // if the pitch is equal
                            && ((Double.parseDouble(p.getAttributeValue("date")) + Double.parseDouble(p.getAttributeValue("duration"))) == date)                             // and the tie note and this note are next to each other (there is zero time between them and they do not overlap)
                    ) {
                        p.addAttribute(new Attribute("duration", Double.toString(Double.parseDouble(p.getAttributeValue("duration")) + dur)));                          // add this duration to the preceeding note with the same pitch
                        if (tie == 't')                                         // terminal tie
                            p.removeAttribute(p.getAttribute("tie"));           // delete tie attribute
                        return;                                                 // this note is not to be stored in the score, it only extends its predecessor; remark: if no fitting note is found, this note will be stored in the score map because this line is not reached
                    }
                }
            }
        }

        this.helper.addLayerAttribute(s);                                       // add an attribute that indicates the layer

        Helper.addToMap(s, this.helper.currentPart.getFirstChildElement("dated").getFirstChildElement("score"));    // insert the new note into the part->dated->score
    }

    /**
     * this function can be used by the application to determine the minimal time resolution (pulses per quarternote) required to represent the shortest note value (found in mei, can go down to 2048) in midi; tuplets are not considered
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

    /**
     * the slacker attribute copyof may occur in the mei document and needs to be resolved before starting the conversion;
     * this method replaces elements with the copyof attribute by copies of the referred elements;
     * it may also be used to expand an mei document and free it from copyofs;
     *
     * this method does also include the processing of attribute sameas, which is similar to copyof
     *
     * @return null (no document loaded), an ArrayList with those ids that could not be resolved, or an empty ArrayList if everything went well
     */
    public synchronized ArrayList<String> resolveCopyofs() {
        Element e = this.getRootElement();                                                              // this also includes the meiHead section, not only the music section, as there might be reference from music into the head
        if (e == null) return null;

        ArrayList<String> notResolved = new ArrayList<String>();                                             // store those ids that are not resolved
        HashMap<Element, String> previousPlaceholders = new HashMap<Element, String>();                               // this is a copy of the placeholders hashmap in the while loop; if it does not change from one iteration to the next, there is a placeholder refering to another placeholder refering back to the first; this cannot be resolved and leads to an infinite loop; this hashmap here is to detect this situation

        System.out.print("Resolving copyofs and sameas's:");

        while (true) {                                                                                  // this loop can only be exited if no placeholders are left (it is possible that multiple runs are necessary when placeholders are within placeholders)
            HashMap<String, Element> elements = new HashMap<String, Element>();                                       // this hashmap will be filled with elements and their ids
            HashMap<Element, String> placeholders = new HashMap<Element, String>();                                   // this hashmap will be filled with placeholder elements that have a copyof attribute and the id in the copyof

            Nodes all = e.query("descendant::*[attribute::copyof or attribute::sameas or attribute::xml:id]");  // get all elements with a copyof, sameas or xml:id attribute
            for (int i = 0; i < all.size(); ++i) {                                                      // for each of them
                Element element = (Element) all.get(i);                                                 // make an Element out of it

                Attribute a = element.getAttribute("copyof");                                           // get the copyof attribute, if there is one
                if (a == null)                                                                          // no copyof attribute?
                    a = element.getAttribute("sameas");                                                 // then maybe a sameas
                if (a != null) {                                                                        // if there is a copyof or sameas attribute
                    String copyof = a.getValue();                                                       // get its value
                    if (copyof.charAt(0) == '#') copyof = copyof.substring(1);                          // local references within the document usually start with #; this must be excluded when searching for the id
                    placeholders.put(element, copyof);                                                  // put that entry on the placeholder hashmap
                    //continue;                                                                         // this element may also have an xml:id, so it must be added to the other list as well and we later on have the possibility to resolve references of placeholders to other placeholders
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
                System.err.print(" circular copyof or sameas referencing detected, cannot be resolved,");
                break;                                                                                  // stop the while loop
            }
            previousPlaceholders = placeholders;

            System.out.print(" " + placeholders.size() + " copyofs and sameas's ...");

            // replace alle placeholders in the xml tree by copies of the source
            for (Map.Entry<Element, String> placeholder : placeholders.entrySet()) {                    // for each placeholder
                Element found = elements.get(placeholder.getValue());                                   // search the elements hashmap for the id

                if (found == null) {                                                                    // if no element with this id has been found
                    notResolved.add(placeholder.getKey().toXML());                                      // add entry to the return list
                    placeholder.getKey().getParent().removeChild(placeholder.getKey());                 // delete the placeholder from the xml, we cannot process it anyway
                    continue;                                                                           // continue with the next placeholder
                }

                // make the replacement
                Element copy = found.copy();                                                               // make a deep copy of the source

                try {
                    placeholder.getKey().getParent().replaceChild(placeholder.getKey(), copy);          // replace the placeholder by it
//                System.out.println("replacing: " + placeholder.getKey().toElement() + "\nby\n" + copy.toElement() + "\n\n");
                } catch (NoSuchChildException | NullPointerException | IllegalAddException error) {     // if something went wrong, I don't know why as none of these exceptions should occur, just to be sure
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
                    copy.getAttribute("id", "http://www.w3.org/XML/1998/namespace").setValue(id.getValue());     // set the copy's id to the id of the placeholder
                }
            }
        }

        System.out.println(" done");

        if (!notResolved.isEmpty())
            System.out.println("The following placeholders could not be resolved:\n" + notResolved.toString());

        return notResolved;
    }

    /**
     * this method resolves all occurrences of attributes copyof and sameas
     * @return
     */
    public synchronized ArrayList<String> resolveCopyofsAndSameas() {
        return this.resolveCopyofs();
    }

    /**
     * rend elements are additional and totally optional visual information that introduce additional processing effort to mei to msm conversion;
     * the only relevant information are its contents; this method replaces all rends by their contents
     */
    private void removeRendElements() {
        Element e = this.getMusic();
        if (e == null) return;                                          // if there is no music, cancel

        System.out.print("Replacing rend elements by their values:");

        int count = 0;
        Nodes rends = e.query("descendant::*[local-name()='rend']");    // get all rend elements
        for (int i = 0; i < rends.size(); ++i) {                        // for each of them
            Element r = (Element) rends.get(i);                         // make an Element from it
            Element parent = (Element) r.getParent();                   // get its parent
            if (parent == null)
                continue;

            parent.appendChild(r.getValue());
            parent.removeChild(r);
            count++;
        }

        System.out.println(" done, " + count + " rends replaced");
    }

    /**
     * Expansion elements in MEI indicate the sequence in which sibling section and ending elements have to be arranged.
     * This method creates a regularized, i.e. "through-composed", MEI that renders the expansions.
     */
    public synchronized void resolveExpansions() {
        System.out.print("Resolving Expansions:");
        this.getRootElement().replaceChild(this.getMusic(), this.resolveExpansions(this.getMusic()));   // replace the whole music subtree by its regularized version
        System.out.println(" done");
    }

    /**
     * Expansion elements in MEI indicate the sequence in which sibling section and ending elements have to be arranged.
     * This method creates a regularized, i.e. "through-composed", MEI that renders the expansions.
     * The MEI tree is scanned recursively and expansions are resolved.
     * @param root from this element on the whole subtree will be resolved
     * @return the regularized version root (can be used to replace root)
     */
    private synchronized Element resolveExpansions(Element root) {
        Element regularizedRoot = (Element) root.copy();                                    // create a deep copy of root to be edited and returned
        Element expansion = Helper.getFirstChildElement("expansion", regularizedRoot);      // this will hold the expansion element to resolve, or null if there is none
        List<String> plist = null;                                                          // this will hold all the xml:id's from the expansion's plist in the order to be played, i.e., the plist says how to rearrange the expansion's siblings

        // first some cleanup, find and remove stuff so it causes no processing effort later on
        if (expansion != null) {
            // remove all expansion elements from this regularizedRoot
            Elements expansions = regularizedRoot.getChildElements("expansion");            // get all expansion elements that are present as direct children of regularizedRoot
            for (int i = expansions.size() - 1; i >= 0; --i)                                // delete all expansion elements from the regularizedRoot
                regularizedRoot.removeChild(expansions.get(i));

            // parse the plist and write its content to expansionSequence
            if (expansion.getAttribute("plist") != null) {                                  // if the expansion has a plist attribute
                plist = Arrays.asList(expansion.getAttributeValue("plist").trim().replaceAll("#","").split("\\s+")); // fill plist with the xml:id's from the plist attribute; before this, leading and trailing whitespaces are removed, multiple whitespaces are reduced, # are removed, what remains are the pure xml:id's stored in a List object
            }
            else                                                                            // an expansion with no plist is not valid (meico does not interpret this as an empty plist which would simply clear the whole subtree)
                expansion = null;                                                           // set expansion to null so it won't cause further processing effort
        }

        // for efficiency reasons we make a depth first recursive resolution, this means bottom-up, first go down, then do the resolution
        Elements children = regularizedRoot.getChildElements();                             // get all child elements of regularizedRoot
        for (int i = children.size() - 1; i >= 0; --i) {                                    // go through all children of regularizedRoot
            Element child = children.get(i);                                                // get the current child element

            if (expansion != null) {                                                        // if there is an expression element with a plist attribute
                Attribute childId = Helper.getAttribute("id", child);                       // get the child's id
                if (childId == null || !plist.contains(childId.getValue())) {               // if it does not have one, it cannot be in the plist and will not be played or the id is not in the plist, again the child will not be played
                    regularizedRoot.removeChild(child);                                     // hence, delete it
                    continue;                                                               // continue with the next child
                }
            }

            regularizedRoot.replaceChild(child, this.resolveExpansions(child));             // replace this child by its regularization
        }

        // now do the regularization on the current regularizedRoot's children, i.e. duplicate and rearrange its children as indicated by the plist
        if (expansion != null) {                                                            // if there is an expansion element
            HashMap<String, Element> childHash = new HashMap<String, Element>();            // HashMap with (id, element) pairs to be filled with the children of regularizedRoot

            // detache all children from regularizedRoot and put them into the HashMap
            for (Element child = Helper.getFirstChildElement(regularizedRoot); child != null; child = Helper.getFirstChildElement(regularizedRoot)) {
                child.detach();                                                             // detach the child
                String id = Helper.getAttributeValue("id", child);                          // get its id
                childHash.put(id, child);                                                   // fill the HashMap
            }

            // now append the former children according to the plist
            for (String aPlist : plist) {                                                   // for each plist entry
                Element child = childHash.get(aPlist);                                      // get the child with the id from the HashMap
                if (child != null) {
                    try {
                        regularizedRoot.appendChild(child);                                 // try to append it to regularizedRoot, this will fail if it has already been added
                    } catch (MultipleParentException e) {                                   // when it has already been added
                        Element copy = (Element) child.copy();                              // make a deep copy of child

                        Nodes cs = copy.query("descendant-or-self::*[@xml:id or @id]");     // find all elements with an id attribute
                        for (int i = 0; i < cs.size(); ++i) {                               // give them all unique ids
                            Element c = (Element) cs.get(i);
                            Attribute id = Helper.getAttribute("id", c);
                            id.setValue("meico_expansion_of_" + id.getValue() + "_" + UUID.randomUUID().toString()); // the new IDs are of the following form: "meico_oldID_newUUID"
                        }

                        regularizedRoot.appendChild(copy);                                  // add the copy
                    }
                }
            }
        }

        return regularizedRoot;
    }

    /** this method adds ids to note, rest, ... and chord elements in mei, as far as they do not have an id
     *
     * @return the generated ids count
     */
    public synchronized int addIds() {
        System.out.print("Adding IDs:");
        Element root = this.getRootElement();
        if (root == null) {
            System.err.println(" Error: no root element found");
            return 0;
        }

        Nodes e = root.query("descendant::*[(local-name()='measure' or local-name()='note' or local-name()='rest' or local-name()='mRest' or local-name()='multiRest' or local-name()='chord' or local-name()='tuplet' or local-name()='mdiv' or local-name()='reh' or local-name()='section') and not(@xml:id)]");
        for (int i = 0; i < e.size(); ++i) {                                    // go through all the nodes
            String uuid = "meico_" + UUID.randomUUID().toString();              // generate new ids for them
            Attribute a = new Attribute("id", uuid);                            // create an attribute
            a.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");      // set its namespace to xml
            ((Element) e.get(i)).addAttribute(a);                               // add attribute to the node
        }

        System.out.println(" done");

        return e.size();
    }
}