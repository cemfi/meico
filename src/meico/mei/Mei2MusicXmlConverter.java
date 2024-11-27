package meico.mei;

import meico.musicxml.MusicXml;
import nu.xom.*;
import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.util.Marshalling;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.String;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class does the conversion from MEI to MusicXML.
 * To use it, instantiate it with the constructor, then invoke convert().
 * See method meico.mei.Mei.exportMusicXml() for some sample code.
 *
 * @author Matthias Nowakowski
 */
public class Mei2MusicXmlConverter {
    private Mei mei = null;                         // the MEI to be converted
    private boolean ignoreExpansions = false;       // set this true to have a 1:1 conversion of MEI to MusicXML without the rearrangement that MEI's expansion elements produce
    private final XPathContext xPathContext = new XPathContext();

    private final LinkedList<MusicXml> mxmls = new LinkedList<>();    // Hold all completed MusicXMLs
    private ScorePartwise header = new ScorePartwise();   // Dummy Object to hold all Elements for the MusicXML "Header" (from <work> down to including <part-list>)
    private ScorePartwise originalHeader = null;
    private LinkedList<ScorePartwise> workList = new LinkedList<>(); // Hold all Worklist objects as ScorePartwise to be then applied in processScore or processParts; Each mdiv with a score and/or parts element should has its own mdiv.
    private ScorePartwise currentScorePartwise;
    private ScoreTimewise currentScoreTimewise;
    private boolean pwIsCurrent = false;
    private boolean twIsCurrent = false;

    private ScorePartwise.Part currentPartPW;
    private ScoreTimewise.Measure.Part currentPartTW;
    private ScorePartwise.Part.Measure currentMeasurePW;
    private ScoreTimewise.Measure currentMeasureTW;

    private String currentVoice;
    private DefinitionType currentDefinition;
    private DefinitionType defDiff = new DefinitionType();
    private Note currentNote;

    private List<String> partListIds = new ArrayList<>();
    private PartList partList = null;
    private LinkedList<Barline> barlines = new LinkedList<>();
    private LinkedList<Element> measureListMEI = new LinkedList<>();
    private LinkedList<Element> tieListMEI = new LinkedList<>();
    private LinkedList<Element> prevMeasureTieListMEI = new LinkedList<>();
    private LinkedList<String> tieBlacklist = new LinkedList<>();

    private final StringBuilder sourceBuilder = new StringBuilder(); // only one is needed since it create the <source> element in MusicXML

    private final String endLine = ", ";
    private int divisions = 0;

    /**
     * constructor
     *
     * @param ignoreExpansions set this true to have a 1:1 conversion of MEI to MSM without the rearrangement that MEI's expansion elements produce
     */
    public Mei2MusicXmlConverter(boolean ignoreExpansions) {
        this.ignoreExpansions = ignoreExpansions;
        this.xPathContext.addNamespace("mei", "http://www.music-encoding.org/ns/mei");
    }

    /**
     * start the conversion process
     *
     * @param mei the Mei object to be converted
     * @return
     */
    public List<MusicXml> convert(Mei mei) {
        try {
            if (mei == null) {
                System.out.println("\nThe provided MEI object is null and cannot be converted.");
                return new ArrayList<>();                                           // return empty lists
            }


            long startTime = System.currentTimeMillis();                            // we measure the time that the conversion consumes
            System.out.println("\nConverting " + ((mei.getFile() != null) ? mei.getFile().getName() : "MEI data") + " to MusicXML.");

            this.mei = mei;

            // if meiHead and music are not present at all.
            if (mei.getMeiHead() == null && mei.getMusic() == null) {
                Nodes nodes = mei.getRootElement().query("*");
                String warning = "\nThe converter expects <meiHead> or <music> as children of <mei>.";
                if (nodes.size() == 0) {
                    System.out.println(warning + " No Children can be found.");
                    return new ArrayList<>();
                }
                ArrayList<String> s = new ArrayList<>();
                for (Node node : nodes) {
                    s.add(((Element) node).getLocalName());
                }
                System.out.println(warning + " Elements " + String.join(", ", s) + " are not allowed.");
                return new ArrayList<>();
            }

            Document orig = this.mei.getDocument().copy();                          // the document will be altered during conversion, thus we keep the original to restore it after the process
            this.initHeader(); // init a "header" (score partwise) for musicxml independent of header in mei
            this.convertHead(mei.getMeiHead());
            this.concatSourceBuilder(); // all SourceBuilder Strings can only be ready, when conversion of meiHead is done (Strings are dispersed over different elements and attributes within the header)

            // convert Mei music
            if (this.mei.isEmpty() || (this.mei.getMusic() == null) || (this.mei.getMusic().getFirstChildElement("body", this.mei.getMusic().getNamespaceURI()) == null)) {
                this.mxmls.add(new MusicXml(this.originalHeader)); // if no mei music data show all processed header
                for (ScorePartwise w : this.workList) { // there can be also some seperate worklist information which where not processed for partlists
                    this.mxmls.add(new MusicXml(w));
                }
            } else {
                this.mei.resolveCopyofsAndSameas();                                     // replace the slacker elements with copyof and sameas attributes by copies of the referred elements
                if (!this.ignoreExpansions)
                    this.mei.resolveExpansions();               // if expansions should be realized, render expansion elements in the MEI score to a "through-composed"/regularized score without expansions

                this.convertMusic(mei.getMusic());
            }

            // Set Filenames for XMLs
            if (this.mxmls.size() == 1)                                                                                           // if only one musicxml object (no numbering needed)
                this.mxmls.get(0).setFile(Helper.getFilenameWithoutExtension(this.mei.getFile().getPath()) + ".musicxml");                 // replace the file extension mei with msm and make this the filename
            else if (this.mxmls.size() > 1) {                                                                                                          // multiple musicxml objects created (or none)
                for (int i = 0; i < this.mxmls.size(); ++i) {                                                                     // for each musicxml object
                    this.mxmls.get(i).setFile(Helper.getFilenameWithoutExtension(this.mei.getFile().getPath()) + "-" + i + ".musicxml");   // replace the extension by the number and the .musicxml extension
                }
            }

            // cleanup
            this.mei.setDocument(orig);                                             // restore the unaltered version of the mei data
            System.out.println("MEI to MusicXML conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");
            return this.mxmls;
        }catch (Exception e){
            e.printStackTrace();
            return mxmls;
        }
    }

    /**
     * Apply convert method by flag (convert in Header or in Music).
     * @param e
     * @param convertInHeader true: use convertHead, false: use convertMusic
     */
    private void convert(Element e, boolean convertInHeader){
        Consumer<Element> c;
        if(convertInHeader){
            c = this::convertHead;
        }else{
            c = this::convertMusic;
        }
        c.accept(e);
    }

    /**
     * MeiHeader is analyzed separately, since this information can be attached to several MusicXML created in convertMusic()
     *
     * @param element element Element of the input file
     */
    private void convertHead(Element element) {
        if(element == null) return;
        Elements es = element.getChildElements();
        for (Element e : es) {
            switch (e.getLocalName()) { //!!! break will result in processing children of e (since convert() is called recursively), continue will go on with next sibling Element.
                case "altId":
                case "abbr":
                case "accMat":
                case "accessRestrict":
                case "accid":
                case "acquisition":
                    continue;

                case "actor":
                    if(e.query("*").size() == 0){
                        this.addToCredit(e);
                        continue;
                    }else{
                        break;
                    }

                case "add":
                case "addDesc":
                case "addName":
                    continue;

                    // all these cases behave in the same way, just with different element names.
                case "address":
                case "pubPlace":
                case "distributor":
                    //this.processAddress(e);
                    break;

                case "addrLine":
                    this.processAddrLine(e);
                    break;

                case "ambNote":
                case "ambitus":
                case "analytic":
                case "anchoredText":
                case "annot":
                case "arpeg":
                case "artic":
                    continue;

                case "app":
                    this.processApp(e, true);
                    continue;

                case "appInfo":
                    break;

                case "application":
                    this.processApplication(e);
                    continue;

                case "argument":
                    continue;

                    // all these cases behave in the same way, just with different string.
                    // if new processing steps for the following cases are used, please consider to add a branch, e.g:
                    // if(applyTitleStmtRestrictions()){
                    //      NEW PROCESSING
                    // }else{
                    //  this.addToCreator();
                    //  this.addToCredit();
                    //  continue;
                    //}
                case "arranger":
                case "author":
                case "editor":
                case "funder":
                case "librettist":
                case "lyricist":
                case "sponsor":
                    //if(applyTitleStmtRestrictions()){continue;}
                    this.addToCreator(e, e.getLocalName());
                    this.addToCredit(e);
                    continue;

                case "attacca":
                case "audience":
                case "avFile":
                case "attUsage":
                    continue;

                case "availability":
                    this.processMisc(e, false);
                    continue;

                case "back":
                case "barLine":
                case "barre":
                case "beam":
                case "beamSpan":
                case "beatRpt":
                case "bend":
                case "bibl":
                case "biblList":
                case "biblScope":
                case "biblStruct":
                case "bifolium":
                case "binding":
                case "bindingDesc":
                    continue;

                case "body":
                    break;

                case "bracketSpan":
                case "breath":
                case "bTrem":
                case "byline":
                case "caption":
                case "caesura":
                case "captureMode":
                case "captureForm":
                case "castGrp":
                case "castItem":
                case "castList":
                case "catchWords":
                case "category":
                case "catRel":
                case "cb":
                case "cc":
                case "chan":
                case "chanPr":
                    continue;

                case "change":
                    this.updateHeaderMisc(e, false);
                    continue;

                case "changeDesc":
                    continue;

                case "choice":
                    this.processChoice(e, true);
                    continue;

                case "chord":
                case "chordDef":
                case "chordMember":
                case "chordTable":
                case "classDecls":
                case "classification":
                case "clef":
                case "clefGrp":
                case "clip":
                case "colLayout":
                case "collation":
                case "colophon":
                    continue;

                case "componentList": // componentList should go directly into <work> in the header
                    break;

                case "composer":
                    break;

                case "contentItem":
                case "contents":
                case "context":
                case "contributor":
                    continue;

                case "corpName":
                    this.addToSource(e);
                    continue;

                case "corr":
                    break;

                case "correction":
                case "cpMark":
                case "curve":
                case "custos":
                case "cue":
                case "cutout":
                case "creation":
                case "damage":
                    continue;

                case "date":
                    this.processDate(e);
                    continue;

                case "decoDesc":
                case "decoNote":
                case "dedicatee":
                case "dedication":
                    continue;

                case "del":
                    break;

                case "depth":
                case "desc":
                case "dim":
                case "dimensions":
                case "dir":
                case "div":
                case "divLine":
                case "domainsDecl":
                case "dot":
                case "dynam":
                case "edition":
                case "editorialDecl":
                    continue;

                case "encodingDesc": // has no value, children will be processed
                    break;

                case "ending":
                case "epigraph":
                case "episema":
                case "eventList":
                case "exhibHist":
                case "expan":
                case "expansion":
                case "explicit":
                case "extData":
                case "extMeta":
                case "extent":
                case "expression":
                case "expressionList":
                case "f":
                case "facsimile":
                case "famName":
                case "fb":
                case "fermata":
                case "fig":
                case "figDesc":
                case "fileChar":
                    continue;

                case "fileDesc":
                    //this.processFileDesc(e);
                    break;

                case "fing":
                case "fingGrp":
                case "foliaDesc":
                case "foliation":
                case "folium":
                case "foreName":
                case "fTrem":
                case "front":
                case "gap":
                case "genDesc":
                case "genName":
                case "genState":
                case "genre":
                case "gliss":
                case "graceGrp":
                case "graphic":
                case "group":
                case "grpSym":
                case "hairpin":
                case "half":
                case "halfRpt":
                case "hand":
                case "handList":
                case "handShift":
                case "harm":
                case "harpPedal":
                case "head":
                case "height":
                case "heraldry":
                case "hex":
                case "hispanTick":
                case "history":
                    continue;
                case "identifier":
                    if(Helper.getClosest("title", e) != null){
                        this.processTitle(e);
                    }
                    continue;
                case "incip": // incipit has score elements in it. Will not be processed in header. no mapping to musicxml
                case "incipCode":
                case "inciptext":
                case "inscription":
                case "instrDef":
                case "instrGrp":
                case "interpretation":
                case "imprimatur":
                case "imprint":
                case "item":
                case "itemList":
                case "key":
                case "keyAccid":
                case "keySig":
                case "l":
                case "label": // can be found here in incipits, but won't be processed
                case "labelAbbr":
                case "language":
                case "langUsage":
                case "layer":
                case "layerDef":
                case "layout":
                case "layoutDesc":
                case "lb":
                    continue;

                case "lem":
                    break;

                case "lg":
                case "li":
                case "ligature":
                case "line":
                case "liquescent":
                case "list":
                case "locus":
                case "locusGrp":
                case "lv":
                    continue;

                case "manifestation":
                    this.processManifestation(e); // manifestations will always be copied to Misc and just parsed for heigth and width
                    continue; //, therefore no further traversal is needed

                case "manifestationList":
                    break; // each manifestation will be processed separately

                case "mapping":
                case "marker":
                case "mdiv":
                    break;

                case "meter":
                case "measure":
                case "mensur":
                case "mensuration":
                case "metaMark":
                case "metaText":
                case "meterSig":
                case "meterSigGrp":
                case "midi":
                case "mNum":
                case "monogr":
                case "mordent":
                case "multiRest":
                case "multiRpt":
                case "mRest":
                case "mRpt":
                case "mRpt2":
                case "mSpace":
                case "name":
                case "nameLink":
                case "namespace":
                case "nc":
                case "ncGrp":
                case "neume":
                case "normalization":
                case "note":
                case "noteOff":
                case "noteOn":
                    continue;

                case "notesStmt":
                    break;

                case "num":
                case "octave":
                case "oLayer":
                case "orig":
                case "oricus":
                case "ornam":
                case "ossia":
                case "oStaff":
                case "otherChar":
                    continue;

                case "p":
                    this.updateHeaderMisc(e, true);
                    continue;

                case "pad":
                case "part":
                case "parts":
                case "patch":
                case "pb":
                case "pedal":
                case "performance":
                case "perfDuration":
                case "perfMedium":
                case "perfRes":
                case "perfResList":
                case "periodName":
                    continue;

                case "persName":
                    this.processPersName(e);
                    continue;

                case "pgDesc":
                case "pgFoot":
                    continue;

                case "pgHead":
                    this.processPgHead(e);
                    continue; // pgHead is only has possible worktitle and composer

                case "phrase":
                case "physDesc":
                case "physloc":
                case "physMedium":
                case "plateNum":
                case "playingSpeed":
                case "plica":
                case "port":
                    continue;

                case "postCode":
                case "postBox":
                case "street":
                case "bloc":
                case "country":
                case "district":
                case "geogFeat":
                case "geogName":
                case "region":
                case "settlement":
                    this.addToSource(e);
                    continue;

                case "price":
                case "prog":
                    continue;

                case "projectDesc":
                    break;

                case "propName":
                case "propValue":
                case "proport":
                case "provenance":
                case "ptr":
                    continue;

                case "publisher":
                    this.processPublisher(e);
                    continue;

                case "pubStmt":
                    break;

                case "q":
                case "quilisma":
                case "quote":
                case "recipient":
                case "recording":
                case "ref":
                case "referain":
                    continue;

                case "reg":
                case "rdg":
                    break;

                case "reh":
                case "relation":
                case "relationList":
                case "relatedItem":
                case "rend":
                case "resp":
                    continue;

                case "respStmt":
                    //this.processRespStmt(e);
                    break;

                case "repeatMark":
                case "repository":
                case "rest":
                case "restore":
                    continue;

                case "revisionDesc":
                    break;

                case "role":
                case "roleDesc":
                case "roleName":
                    continue;

                case "samplingDecl":
                    break;

                case "rubric":
                case "sb":
                case "score":
                case "scoreDef":
                case "scoreFormat":
                case "scriptDesc":
                case "secFolio":
                case "section":
                case "seal":
                case "sealDesc":
                case "seg":
                case "segmentation":
                case "seqNum":
                case "series":
                    continue;

                case "seriesStmt":
                    break;

                case "sic":
                case "signatures":
                case "signiLet":
                case "slur":
                case "soundChan":
                case "source":
                case "sp":
                case "space":
                case "speaker":
                case "specRepro":
                    continue;

                case "sourceDesc":
                    break;

                case "stack":
                case "staff":
                case "staffDef":
                case "staffGrp":
                case "stageDir":
                case "stamp":
                case "stdVals":
                case "stem":
                case "strophicus":
                case "styleName":
                case "subst":
                case "supplied":
                case "support":
                case "supportDesc":
                case "surface":
                case "syl":
                case "syllable":
                case "symbol":
                case "symbolDef":
                case "symbolTable":
                case "sysReq":
                case "table":
                case "tagsDecl":
                case "tagUsage":
                case "taxonomy":
                case "td":
                case "tempo":
                case "term":
                case "termList":
                case "textLang":
                case "th":
                case "tie":
                case "titlePage":
                    continue;

                case "title":
                    // for now, title only occurs in composer and titleStmt
                    this.processTitle(e);
                    break;

                case "titlePart":
                    this.processTitle(e);
                    continue;

                case "titleStmt":
                    break;

                case "tr":
                case "trackConfig":
                case "treatHist":
                case "treadShed":
                case "trill":
                case "trkName":
                case "tup":
                case "tuplet":
                case "tupletSpan":
                case "turn":
                case "typeDesc":
                case "typeNote":
                case "unclear":
                    continue;

                case "unpub":
                    this.updateHeaderMisc("unpub", "Unpublished");
                    continue;

                case "useRestrict":
                case "vel":
                case "verse":
                case "volta":
                case "watermark":
                case "when":
                case "width":
                    continue;

                case "work":
                    this.processWork(e);
                    break;

                case "workList":
                    this.workList = new LinkedList<>();
                    break;

                case "zone":
                    continue;

                default:
                    break;
            }
            this.convertHead(e);
        }
    }

    /**
     * recursively traverse the mei tree (depth first) starting at the root element and return the list of Msm instances; root indicates the root of the subtree,
     * the resulting Msm objects are stored in this.movements
     * @param root the root of the subtree to be processed
     */
    private void convertMusic(Element root) {
        Elements es = root.getChildElements();                                  // all child elements of root
        for (int i = 0; i < es.size(); ++i) {                                   // element beginHere traverses the mei tree
            Element e = es.get(i);                                              // get the element

            //this.checkEndid(e);                                          // check for pending elements with endid attributes to be finished when the element with this endid is found

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
                    continue;

                case "arpeg":                                                   // indicates that the notes of a chord are to be performed successively rather than simultaneously
                    break;

                case "artic":                                                   // an indication of how to play a note or chord
                    continue;

                case "barline":
                    continue;                                                   // can be ignored

                case "beam":                                                    // contains the notes to be beamed TODO: relevant for expressive performance
                    break;

                case "beamSpan":
                    continue;                                                   // TODO: may be relevant for expressive phrasing

                case "beatRpt":
                    continue;

                case "bend":
                    continue;                                                   // TODO: relevant for expressive performance

                case "body":
                    break;

                case "breath":                                                  // an indication of a point at which the performer on an instrument requiring breath (including the voice) may breathe
                    //this.processBreath(e);
                    continue;

                case "bTrem":
                    // continue with whatever is a child of btrem
                    break;

                case "caesura":                                                 // TODO: relevant for expressive performance
                    continue;

                case "choice":                                                  // the children of a choice element are alternatives of which meico has to choose one
                    //this.processChoice(e);
                    continue;

                case "chord":
                    if (e.getAttribute("grace") != null)                        // TODO: at the moment we ignore grace notes and grace chords; later on, for expressive performances, we should handle these somehow
                        continue;
                    break;                                                   // continue with the next sibling

                case "chordTable":
                    continue;                                                   // can be ignored

                case "clef":
                    this.processClef(e);
                    continue;

                case "clefGrp":
                    break;                                                   // TODO: can this be ignored or is it of any relevance to pitch computation?

                case "corr":                                                    // a correction, can occur as "standalone" or in the choice environment, usually paired with the sic element
                    break;                                                      // nothing special about this element to process, just process its subtree

                case "curve":
                    continue;                                                   // can be ignored

                case "custos":
                    continue;                                                   // can be ignored

                case "damage":
                    continue;                                                   // TODO: ignore

                case "del":                                                     // contains information deleted, marked as deleted, or otherwise indicated as superfluous or spurious in the copy text by an author, scribe, annotator, or corrector
                    //this.processDel(e);
                    continue;

                case "dir":
                    continue;                                                   // TODO: relevant for expressive performance

                case "div":
                    continue;                                                   // can be ignored

                case "dot":
                    this.processDot(e);
                    continue;                                                   // there should be no children, so continue with the next element

                case "dynam":                                                   // indication of the volume of a note, phrase, or section of music
                    //this.processDynam(e);
                    continue;

                case "ending":                                                  // relevant in the context of repetitions
                    //this.processEnding(e);
                    break;

                case "expan":                                                   // the expansion of an abbreviation
                    break;                                                      // nothing special about this element to process, but dive into it and process its children

                case "expansion":                                               // indicates how a section may be programmatically expanded into its 'through-composed' form
                    continue;                                                   // expansions are treated during preprocessing, here they are ignored

                case "fermata":
                    continue;                                                   // TODO: relevant for expressive performance

                case "fTrem":
                    //this.processChord(e);                                       // fTrems are treated as chords
                    break;                                                   // continue with the next sibling

                case "gap":
                    continue;                                                   // TODO: What to do with this?

                case "gliss":
                    continue;                                                   // TODO: relevant for expressive performance

                case "grpSym":
                    continue;                                                   // can be ignored

                case "hairpin":                                                 // indicates continuous dynamics expressed on the score as wedge-shaped graphics, e.g. < and >.
                    //this.processDynam(e);
                    continue;

                case "halfmRpt":
                    //this.processHalfmRpt(e);
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
                    continue;                                                   // ignore this tag as these elements are handled in method makePart()

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
                    if (Helper.getFirstChildElement(e, "mdiv") == null){ // each inner mdiv is equivalent to one work in the workList
                        this.processMdiv(e);
                    }
                        break;

                case "measure":
                    if(this.twIsCurrent ) {
                        this.processMeasureTW(e);
                        break;
                    }else if(this.pwIsCurrent){
                        this.processMeasurePW(e);
                        break;
                    }
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
                    this.processNote(e);
                    continue;

                case "mRpt":
                    //this.processMRpt(e);
                    break;

                case "mRpt2":
                    //this.processMRpt2(e);
                    break;

                case "mSpace":
                    this.processNote(e);                                 // interpret it as an mRest, i.e. measure rest
                    continue;

                case "multiRest":
                    //this.processMultiRest(e);
                    continue;

                case "multiRpt":
                    //this.processMultiRpt(e);
                    break;

                case "note":
                    if (e.getAttribute("grace") != null)                        // TODO: at the moment we ignore grace notes and grace chords; later on, for expressive performances, we should handle these somehow
                        continue;
                    this.processNote(e);
                    break;                                                   // no need to go deeper as any child of this tag is already processed

                case "octave":
                    //this.processOctave(e);
                    break;

                case "oLayer":                                                  // layer that contains an alternative to material in another layer
                    //this.processLayer(e);
                    continue;

                case "orig":                                                    // contains material which is marked as following the original, rather than being normalized or corrected
                    break;                                                      // when it does not appear in a choice environment, we do not need any special processing for this

                case "ornam":
                    continue;                                                   // TODO: relevant for expressive performance

                case "part":
                    //this.processPart(e);                                        // part occur only in <parts> element, next can be scoreDef, section etc.
                   break;

                case "parts":
                    this.processParts(e);
                    break;

                case "pb":
                    break;                                                   // may have a pgHead

                case "pgHead":
                    this.processPgHead(e);
                    continue; // pgHead for now only has possible worktitle and composer

                case "phrase":                                                  // a phrase element represents a single phrase within a section of music; currently, there are no use cases to parse it
                    continue;                                                   // TODO: are there use cases to process a phrase?

                case "physDesc":
                    continue;                                                   // can be ignored

                case "port":
                    continue;                                                   // TODO: relevant for expressive performance

                case "pp":
                    continue;                                                   // TODO: can be ignored?

                case "rdg":
                    continue;                                                   // this element is part of the critical apparatus (child of app); it is processed by this.processApp()

                case "ref":
                    continue;                                                   // TODO: What to do with this?

                case "reg":
                    continue;                                                   // TODO: What to do with this?

                case "reh":
                    continue;                                                   // TODO: relevant for expressive performance

                case "rend":
                    continue;                                                   // TODO: relevant for expressive performance

                case "repeat":
                    continue;                                                   // can be ignored, it is of no relevance to the analysis

                case "rest":                                                    // TODO: at the moment, I ignore grace notes and grace rests; later on, for expressive performances, we should handle these somehow
                    if (e.getAttribute("grace") != null)
                        continue;
                    this.processNote(e);
                    break;                                                   // no need to go deeper as any child of this tag is already processed

                case "score":
                    this.processScore(e);
                    break;

                case "scoreDef":
                    this.processScoreDef(e);
                    break;

                case "section":
                    if(this.pwIsCurrent){
                        this.processSectionPW(e);
                    }else if(this.twIsCurrent){
                        //this.processSectionTW(e);
                    }
                    break;                                                      // do not need to do anything, just traverse the children

                case "seg":
                    continue;                                                   // TODO: What to do with this?

                case "sic":                                                     // this element is part of the critical apparatus; this implementation treats it as part of a choice
                    continue;                                                   // it is processed by this.processChoice()

                case "slash":
                    //this.processSlash(e);
                    continue;

                case "slur":                                                    // indicates a slur, phrase mark, or tie
                    //this.processSlur(e);
                    continue;

                case "smufl":                                                   // the element contains the SMuFL name of a music symbol
                    continue;

                case "sound":
                    continue;                                                   // can be ignored

                case "space":
                    this.processNote(e);
                    continue;

                case "staff":                                                   //should find some layers now
                    if(this.twIsCurrent){
                        this.processStaffTW(e);
                        break;
                    }
                    break;

                case "staffDef":
                    // staffDefs in Measures, Sections etc will be examined when layer is looking for corresponding defs, prevents creating new Defs with double numbers
                    if(this.twIsCurrent){
                        if (!Helper.getParentElement(e).getLocalName().equals("staffGrp")){
                            break;
                        }
                    }else if(this.pwIsCurrent){ // in part wise a scoredef is at the beginning of every part (hopefully)
                        Element scoreDef = Helper.getClosest("scoreDef", e);
                        if(scoreDef != null){
                            if(!Helper.getParentElement(scoreDef).getLocalName().equals("part")){
                                break;
                            }
                        }else{ // the current scoreDef is not the first in the part/ section, so don't use it for creating partlist
                            if(Helper.getPreviousSiblingElement("scoreDef", e) != null || Helper.getPreviousSiblingElement("staffDef", e) != null || Helper.getParentElement(e).getLocalName().equals("section") ){
                                break;
                            }
                        }
                    }
                    this.processStaffDef(e); // processing staffDefs is just for creating part-list at this point. Be careful to not assign score-parts multiple times.
                        break;

                case "staffGrp":
                    if(Helper.getClosest("section", e) != null) continue; // if the staffGrp is within a section, just continue (the section staffGrps are processed while looking for corresponding defs (this.findCorrespondingDefinition())
                    this.processStaffGrp(e);
                    continue; // recursion takes place in function, because start and stop attribute of part-group is clear that way

                case "stem":
                    this.processStem(e);
                    continue;

                case "syl":
                    continue;                                                   // can be ignored

                case "tempo":
                    //this.processTempo(e);
                    continue;

                case "tenuto":
                    //this.processArtic(e);
                    continue;

                case "tier":
                    continue;                                                   // can be ignored

                case "tie":
                    //this.processTie(e);
                    continue;

                case "trem":
                    // trems are treated as chords
                    break;                                                   // continue with the next sibling

                case "trill":
                    continue;                                                   // TODO: relevant for expressive performance

                case "tuplet":
                    break;

                case "verse":
                    continue;                                                   // can be ignored

                case "view":
                    continue;                                                   // can be ignored

                case "vocal":
                    continue;                                                   // can be ignored

                case "zone":
                    continue;                                                   // TODO: ignore

                default:                                                        // we do not care about any other element
            }

            this.convertMusic(e);                                               // traverse subtree
        }
    }


    /**
     * Initialize header + map info from to MusicXML. Further Elements will be mapped in convertHeader
     *
     */
    private void initHeader() {
        this.header.setWork(new Work());
        this.header.setMovementNumber("");
        this.header.setMovementTitle("");
        this.header.setIdentification(new Identification());
        this.header.getIdentification().setEncoding(new Encoding());
        this.header.setDefaults(new Defaults());
        this.header.getCredit();
        this.header.setPartList(new PartList());

        //Some defaults
        String meicoString = "meico";
        this.addTypedTextToEncoding(meicoString, "", "software");
        LocalDate ld = LocalDate.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = ld.format(f);
        this.addTypedTextToEncoding(date, "", "encoding-date");
    }

    /**
     * Process Pers Name Element. In header most of the time it is a part of creator and credit information combined.
     * Exceptions may apply.
     * @param persName
     */
    private void processPersName(Element persName) {
        Attribute role = persName.getAttribute("role");
        if(role == null){
            return;
        }
        String roleVal = role.getValue().trim();
        switch (roleVal) {
            case "author":
            case "composer":
            case "lyricist":
                this.addToCreator(persName, roleVal);
                this.addToCredit(persName.getValue().trim(), roleVal);
                break;
            case "encoder":
                this.addTypedTextToEncoding(persName.getValue().trim(), "encoder", "encoder");
                break;
        }
    }

    /**
     * Helper Function to add new Entry to Identification/Encoding
     * @param value Value to be hold in the Element
     * @param typeVal Value of the attribute "type"
     * @param qnameString Element Name
     */
    private void addTypedTextToEncoding(String value, String typeVal, String qnameString){
        TypedText tt = new TypedText();
        tt.setValue(value);
        if(typeVal != null && !typeVal.isEmpty()) {
            tt.setType(typeVal);
        }
        QName qname = new QName(qnameString);
        this.header.getIdentification().getEncoding().getEncodingDateOrEncoderOrSoftware().add(
                new JAXBElement<>(qname, TypedText.class, tt)
        );
    }

    /**
     * Process title Element and add info to header element
     * @param title title or titlePart Element
     */
    private void processTitle(Element title){
        if(title.getChildCount() == 0) return; // check for empty titles
        String attr = title.getAttributeValue("type");
        Node titleChild = title.getChild(0); // ensure to get just the first title part (and not the subsequent titleParts since these are processed separately
        if(titleChild == null) return;
        String value = titleChild.getValue();
        if(value == null || value.isEmpty()) {return;} // If there is value, there can be no mapping to the work title
        if((title.getLocalName().equals("title") && (attr == null || attr.isEmpty())) || title.getLocalName().equals("identifier")) {attr = "main";} //else{return;} // don't do this for titlePart since title needs no attributes to be mapped correctly
        if(Helper.getClosest("titleStmt", title) == null) return; // {attr = "subordinate";} // Every other title that is not in a titleStmt will be a subtitle
        value = value.trim();
        String workTitle = this.header.getWork().getWorkTitle();
        switch (attr) {
            case "uniform":
                this.header.getWork().setWorkTitle(value);
                break;
            case "main":
                workTitle = this.header.getWork().getWorkTitle();
                if(workTitle == null || workTitle.isEmpty()) {
                    this.header.getWork().setWorkTitle(value);
                }else{
                    workTitle += ". " + value;
                    this.header.getWork().setWorkTitle(workTitle);
                }
                LinkedList<Credit> creditsToRemove = new LinkedList<>();
                for(Credit c : this.header.getCredit()){ // remove worktitle from credit to set new one
                    for(Object o : c.getCreditTypeOrLinkOrBookmark()){
                        if(o instanceof String){
                            if(o.equals("title")) creditsToRemove.add(c);
                        }
                    }
                }
                for(Credit c : creditsToRemove){
                    this.header.getCredit().remove(c);
                }
                this.addToCredit(this.header.getWork().getWorkTitle(), "title");
                break;
            case "subordinate":
                Pattern pattern = Pattern.compile("(n.{0,2}\\d+|op.{0,2}\\d+)", Pattern.CASE_INSENSITIVE); //the subordinate type might include the work number. In this case the pattern is looking for strings like "nr.1, no1, n.1, op"...
                Matcher matcher = pattern.matcher(value);
                boolean hasFound = false;
                while(matcher.find()) {
                    hasFound = true;
                    String wn =  this.header.getWork().getWorkNumber();
                    if(wn != null && wn.trim().isEmpty()) {
                        wn += ", " + matcher.group();
                    }else{
                        wn = matcher.group();
                    }
                    this.header.getWork().setWorkNumber(wn);
                }

                if(!hasFound){ // else: just add the subordinate title as an additional part to the work title.
                    if(this.header.getWork().getWorkTitle() != null && this.header.getWork().getWorkTitle().equals(value)) break; // break if subtitle is alread in work.title
                    this.addToCredit(value, "subtitle");
//                    String wt = this.header.getWork().getWorkTitle();
//                    wt += ". " + value;
                    //this.header.getWork().setWorkTitle(wt);
                }
                break;
            case "alternative":
                this.header.setMovementTitle(value);
                break;
            case "number":
                this.header.getWork().setWorkNumber(value);
                break;
        }
    }

    /**
     * Take the element and write it to SourceBuilder.
     * Will be concatenated and progressively and added to <source> in MusicXML at the end.
     *
     * @param e any Element suitable for source information
     */
    private void addToSource(Element e){
        String val = e.getValue();
        if(val == null || val.isEmpty()) {return;}
        val = this.sanitize(val.trim());
        StringBuilder addressBuilder = new StringBuilder();
        addressBuilder.insert(0, Character.toUpperCase(e.getLocalName().charAt(0)) + e.getLocalName().substring(1) +  ":"); // Distributor and Address have very similar children
        addressBuilder.append(this.endLine);
        addressBuilder.append(val).append(this.endLine);
        this.sourceBuilder.append(addressBuilder);

    }

    /**
     * Add a new Creator.
     * @param e  Element to be parsed for the Element Value
     * @param typeName type name to enter in MusicXML. If none is given, take same as e.getValue
     */
    private void addToCreator(Element e, String typeName){
        if(typeName == null || typeName.isEmpty()) {
            return;
        }
        TypedText creator = new TypedText();
        creator.setType(typeName);
        creator.setValue(this.sanitize(e.getValue()));
        List<TypedText> cList = this.header.getIdentification().getCreator();
        boolean creatorFound = false;
        for(TypedText c : cList){
            if (c.equals(creator)) {
                creatorFound = true;
                break;
            }
        }
        if(!creatorFound) {
            this.header.getIdentification().getCreator().add(creator);
        }
    }

    /**
     * Add a new Credit. Each Credit has one credit-type and one credit-words
     * @param e  Element to be parsed for credit-type and credit-words
    */
    private void addToCredit(Element e){
        Credit c = new Credit();
        c.getCreditTypeOrLinkOrBookmark().add(e.getLocalName()); // this is "credit-type" based on type (=string)
        FormattedText cwords = new FormattedText();
        cwords.setValue(this.sanitize(e.getValue()));
        c.getCreditTypeOrLinkOrBookmark().add(cwords); // this is "credit-words" based on type (=FormattedText)
        this.header.getCredit().add(c);
    }

    /**
     * Add a new Credit. Each Credit has one credit-type and one credit-words
     * @param cwordsVal Value for Credit-Words
     * @param ctypeVal Value for Credit-Type
     */
    private void addToCredit(String cwordsVal, String ctypeVal){
        Credit c = new Credit();
        c.getCreditTypeOrLinkOrBookmark().add(ctypeVal); // this is "credit-type" based on type (=string)
        FormattedText cwords = new FormattedText();
        cwords.setValue(this.sanitize(cwordsVal));
        c.getCreditTypeOrLinkOrBookmark().add(cwords); // this is "credit-words" based on type (=FormattedText)
        c.setPage(BigInteger.ONE);
        this.header.getCredit().add(c);
    }

    /**
     * Add the sourceBuilder contents to //Identification/Source
     */
    private void concatSourceBuilder(){
        if(this.sourceBuilder.length() > 0) {
            String src = this.sanitize(this.sourceBuilder.toString());
            if(this.originalHeader == null) return;
            this.originalHeader.getIdentification().setSource(src);
            for(ScorePartwise sp : this.workList){
                sp.getIdentification().setSource(src);
            }
        }
    }


    /**
     * Process a mei app element (critical apparatus),
     * in this run the method also processes lem and rdg elements (the two kinds of child elements of app)
     * @param app
     */
    private void processApp(Element app, boolean processInHeader) {
        Element takeThisReading = Helper.getFirstChildElement(app, "lem");  // get the first (and hopefully only) lem element, this is the desired reading

        if (takeThisReading == null) {                                      // if there is no lem element
            takeThisReading = Helper.getFirstChildElement(app, "rdg");      // choose the first rdg element (they are all of equal value)
            if (takeThisReading == null) {                                  // if there is no reading
                return;                                                     // nothing to do, return
            }
        }

       this.convert(app, processInHeader);
    }

    /**
     * Process application name. p, ptr and ref elements are neglected.
     * @param application application Element
     */
    private void processApplication(Element application) {
        Element name = Helper.getFirstChildElement(application, "name");
        if(name != null) {
            this.addTypedTextToEncoding(this.sanitize(name.getValue()), null, "software");
        }
    }

    /**
     * Process address line and add to source builder.
     * @param addrLine
     */
    private void processAddrLine(Element addrLine){
        String val = addrLine.getValue();
        if(val == null || val.isEmpty()) {return;}
        this.sourceBuilder.append(this.sanitize(val)).append(this.endLine);
    }

    /**
     * Process an mei choice element,
     * it has to choose one the alternative subtrees to process further,
     * in here we can find the elements abbr, choice, corr, expan, orig, reg, sic, subst, unclear,
     * TODO: this implementation does not take the cert attribute (certainty rating) into account
     * @param choice
     */
    private void processChoice(Element choice, boolean processInHeader) {
        String[] prefOrder = {"corr", "reg", "expan", "subst", "choice", "orig", "unclear", "sic", "abbr"};   // define the order of preference of elements to choose in this environment

        // make the choice
        Element c = null;                                           // this will hold the chosen element for processing
        for (int i=0; (c == null) && (i < prefOrder.length); ++i) { // search for the preferred types of elements in order of preference
            c = Helper.getFirstChildElement(choice, prefOrder[i]);
        }

        if (c != null) {
            if (c.getLocalName().equals("choice"))                  // if we chose a choice
                this.processChoice(c, processInHeader);                              // process it recursively
            else
               this.convert(c, processInHeader);                              // convert
            return;                                                 // done
        }

        // nothing found
        c = choice.getChildElements().get(0);                       // then take the first child whatever it is
        if (c != null)                                              // if the choice element was not empty and we finally made a decision
            this.convert(c, processInHeader);                                        // process it
    }


    /**
     * Add publisher to sourceBuilder.
     * @param publisher
     */
    public void processPublisher(Element publisher) {
        String val = publisher.getValue();
        if(val == null || val.isEmpty()) {return;}
        this.sourceBuilder.append("Publisher: ").append(this.sanitize(val)).append(this.endLine);
    }

    /**
     * Add given Date to source string. (!Will not create a new date.)
     * @param date date element
     */
    private void processDate(Element date) {
        String dateVal = date.getValue().trim();
        String dateAttr = date.getAttributeValue("isodate");
        String dateString = "";
        if(dateAttr != null && !dateAttr.isEmpty()){
            dateString = dateAttr;
        }
        if(dateVal != null && !dateVal.isEmpty()){  // Preference for existing element value
            dateString = dateVal;
        }
        if(dateString.isEmpty()){return;}
        this.sourceBuilder.append("Date: ").append(dateString).append(this.endLine);
    }

    /**
     * Process work instances. Each work is part of a worklist.
     * Set this work as new header to be processed further recursively.
     * @param work
     */
    private void processWork(Element work) {
        this.checkAndSetOriginalHeader();
        ScorePartwise clonedHeader = this.clone(this.originalHeader);
        if(Helper.getFirstChildElement(work, "componentList") != null){ // a componentList implies that there are sub works which are listed under the main work
            return;
        }
        this.workList.add(clonedHeader); // processing Work implies always an existing worklist
        this.header = clonedHeader;
    }

    /**
     * Clone any ScorePartwise Object.
     * ScoreTimewise is not supported as by proxymusic 3.0.1.
     * @param score
     * @return
     */
    private ScorePartwise clone(ScorePartwise score)  {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Marshalling.marshal(score, outputStream, false, 4);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            Object sp = Marshalling.unmarshal(inputStream);

            outputStream.close();
            inputStream.close();

            return (ScorePartwise) sp;
        }catch(Marshalling.MarshallingException | Marshalling.UnmarshallingException | IOException m){
            m.printStackTrace();
            return null;
        }
    }

//    /**
//     * Set Part name in part list
//     * @param label
//     */
//    private void processLabel(Element label) {
//        String partName = label.getValue();
//        if(partName != null && !partName.isEmpty()){
//            ScorePart lastSp = this.getLastScorePart();
//            if(lastSp != null){
//                PartName pn = new PartName();
//                pn.setValue(partName.trim());
//                lastSp.setPartName(pn);
//            }
//        }
//    }
//
//    /**
//     * Set part abbreviation in part list
//     * @param labelAbbr
//     */
//    private void processLabelAbbr(Element labelAbbr) {
//        String partName = labelAbbr.getValue();
//        if(partName != null && !partName.isEmpty()){
//            ScorePart lastSp = this.getLastScorePart();
//            if(lastSp != null){
//                PartName pn = new PartName();
//                pn.setValue(partName.trim());
//                lastSp.setPartAbbreviation(pn);
//            }
//        }
//    }
//
//    /**
//     * Find the last ScorePart in the Partlist of the header Object
//     * @return
//     */
//    private ScorePart getLastScorePart(){
//        List<Object> pList = this.partList.getPartGroupOrScorePart();
//        int pListSize = pList.size();
//        Object o = null;
//        int backwardsIndex = pListSize-1;
//        while(!(o instanceof ScorePart) && backwardsIndex > 0){
//            o = pList.get(backwardsIndex);
//            backwardsIndex -=1;
//        }
//        if(o instanceof ScorePart){
//            return (ScorePart) o;
//        }
//        return null;
//    }
//
//    private PartGroup getLastPartGroup(){
//        List<Object> pList = this.partList.getPartGroupOrScorePart();
//        int pListSize = pList.size();
//        Object o = null;
//        int backwardsIndex = pListSize-1;
//        while(!(o instanceof PartGroup) && backwardsIndex > 0){
//            o = pList.get(backwardsIndex);
//            backwardsIndex -=1;
//        }
//        if(o instanceof PartGroup){
//            return (PartGroup) o;
//        }
//        return null;
//    }

    /**
     * Map element to miscellaneous field, if Element is important but has not a clear mapping in MusicXML.
     * Warning: ManifestationList might appear after Worklist and should be applied to all Header in Worklist (See: updateHeaderMisc()).
     * @param e
     * @param readValue true: use getValue Method to get Element string; false: copy entire XML tree down of e
     */
    private void processMisc(Element e, boolean readValue){
        if(e.getValue() == null){return;}
        String path = this.getXMLPath(e, "/"); // get path to referenced in miscellaneous field name attribute
        Miscellaneous misc = this.header.getIdentification().getMiscellaneous();
        if(misc == null) {
           this.header.getIdentification().setMiscellaneous(new Miscellaneous());
        }
        MiscellaneousField miscField = new MiscellaneousField();
        miscField.setName(path);
        if(readValue) {
            miscField.setValue(this.sanitize(e.getValue()));
        }else{
            miscField.setValue(this.sanitize(e.toXML()));
        }
        this.header.getIdentification().getMiscellaneous().getMiscellaneousField().add(miscField);
    }

    /**
     * Map element to miscellaneous field, if Element is important but has not a clear mapping in MusicXML.
     * Fieldname and value can be set arbitrarily.
     * @param fieldName
     * @param val
     */
    private void processMisc(String fieldName, String val){
        if(fieldName == null || val == null){return;}
        val = this.sanitize(val);
        Miscellaneous misc = this.header.getIdentification().getMiscellaneous();
        if(misc == null) {
            this.header.getIdentification().setMiscellaneous(new Miscellaneous());
        }
        MiscellaneousField miscField = new MiscellaneousField();
        miscField.setName(fieldName);
        miscField.setValue(val);
        this.header.getIdentification().getMiscellaneous().getMiscellaneousField().add(miscField);
    }

    /**
     * Update the miscellanous field for all headers.
     * @param e
     * @param readValue true: use getValue Method to get Element string; false: copy entire XML tree down of e
     */
    public void updateHeaderMisc(Element e, boolean readValue){
        this.checkAndSetOriginalHeader();
        this.header = this.originalHeader;
        this.processMisc(e, readValue);
        for(ScorePartwise h: this.workList) {
            this.header = h;
            this.processMisc(e, readValue);
        }
    }

    /**
     * Update the miscellanous field for all headers with arbitrary fieldName and values.
     * @param fieldName
     * @param val
     */
    public void updateHeaderMisc(String fieldName, String val){
        this.checkAndSetOriginalHeader();
        this.header = this.originalHeader;
        this.processMisc(fieldName, val);
        for(ScorePartwise h: this.workList) {
            this.header = h;
            this.processMisc(fieldName, val);
        }
    }

    /**
     * Find path to Element (without indices as you might see in xpath) and concatenate string with separator
     * @param e Element
     * @param sep separator
     * @return path to this Element
     */
    private String getXMLPath(Element e, String sep){
        Element temp = e;
        StringBuilder path = new StringBuilder(e.getLocalName());
        do{
            temp = Helper.getParentElement(temp);
            if(temp == null){break;}
            path.insert(0, sep);
            path.insert(0, temp.getLocalName());
        }while(temp != this.mei.getMeiHead() || temp != this.mei.getMusic());
        return path.toString();
    }


    /**
     * Process Manifestation Element. The Manifestation are some information about the physical correlate of the musical text.
     * Just layout info is important for now.
     * For now, only height and width in any layout element are processed for default settings.
     * Everything else first throw into miscellaneous
     * @param manifestation manifestation Element
     */
    private void processManifestation(Element manifestation) {
        Nodes height = manifestation.query(".//mei:layout//mei:height", this.xPathContext); // find height  Element that is somewhere hidden under Layout
        Nodes width = manifestation.query(".//mei:layout//mei:width", this.xPathContext); // analogous to height
        Defaults defaults = this.header.getDefaults();
        if(height.size() > 0 && width.size() > 0){
            if(defaults.getPageLayout() == null) {
               defaults.setPageLayout(new PageLayout());
            }
            try {
                defaults.getPageLayout().setPageHeight(new BigDecimal(height.get(0).getValue().trim()));
                defaults.getPageLayout().setPageWidth(new BigDecimal(width.get(0).getValue().trim()));
            }catch(NullPointerException e){
                e.printStackTrace();
            }
        }

        // everything else goes to misc
        this.updateHeaderMisc(manifestation, false);
    }


    /**
     * If the mDiv has a "n" attribute, write it ot the movement number in the corresponding header.
     * mdivs children will be considered to be separate scores.
     * @param mdiv
     */
    private void processMdiv(Element mdiv) {
        String movementN = mdiv.getAttributeValue("n");
        if(movementN != null && !movementN.isEmpty()){
            this.header.setMovementNumber(movementN);
        }
    }

    /**
     * For now find potential worktitle or composer
     * @param pgHead
     */
    private void processPgHead(Element pgHead) {
        Nodes title = pgHead.query("//mei:rend[@type='title']", this.xPathContext);
        Nodes composer = pgHead.query("//mei:rend[@type='composer']", this.xPathContext);

        String titString = this.header.getWork().getWorkTitle();
        if(title.size() > 0) {
            String titVal = ((Element) title.get(0)).getValue().trim();
            boolean hasTit = titString.equals(titVal);
            if(!hasTit) this.header.getWork().setWorkTitle(((Element) title.get(0)).getValue().trim());

        }

        String composerName = "";
        if(composer.size() > 0) {
            boolean hasComposer = false;
            for(Credit c : this.header.getCredit()){
                for(Object o :c.getCreditTypeOrLinkOrBookmark()){
                    composerName = composer.get(0).getValue().trim();
                    if(o.toString().equals(composerName)){
                        hasComposer = true;
                    }
                }
            }
            if(!hasComposer) {
                this.addToCreator((Element) composer.get(0), "composer");
                this.addToCredit(composerName, "composer");
            }
        }
    }

    /**
     * Copies the header information to the currently processed score.
     */
    public void copyHeaderToScore(){
        ScorePartwise copy = this.clone(this.header);
        if(copy == null) return;
        if(this.twIsCurrent) {
            this.currentScoreTimewise.setDefaults(copy.getDefaults());
            this.currentScoreTimewise.setIdentification(copy.getIdentification());
            this.currentScoreTimewise.setVersion(copy.getVersion());
            this.currentScoreTimewise.setWork(copy.getWork());
            this.currentScoreTimewise.setMovementTitle(copy.getMovementTitle());
            this.currentScoreTimewise.setMovementNumber(copy.getMovementNumber());
            this.currentScoreTimewise.setPartList(copy.getPartList());
            for(Credit c : copy.getCredit()){
                this.currentScoreTimewise.getCredit().add(c);
            }
        }else if(this.pwIsCurrent){
            this.currentScorePartwise.setDefaults(copy.getDefaults());
            this.currentScorePartwise.setIdentification(copy.getIdentification());
            this.currentScorePartwise.setVersion(copy.getVersion());
            this.currentScorePartwise.setWork(copy.getWork());
            this.currentScorePartwise.setMovementTitle(copy.getMovementTitle());
            this.currentScorePartwise.setMovementNumber(copy.getMovementNumber());
            this.currentScorePartwise.setPartList(copy.getPartList());
            for(Credit c : copy.getCredit()){
                this.currentScorePartwise.getCredit().add(c);
            }
        }
    }

    private void processStaffGrp(Element staffGrp) {
        PartGroup pgStart = new PartGroup();
        pgStart.setType(StartStop.START);
        this.partList.getPartGroupOrScorePart().add(pgStart);

        this.convertMusic(staffGrp); // process staffGrp recursively here to determine end of partGroup

        PartGroup pgStop = new PartGroup();
        pgStop.setType(StartStop.STOP);
        this.partList.getPartGroupOrScorePart().add(pgStop);

        this.addPartlistToScore();
        this.partList = new PartList(); // reset partlist for next staffgrp
    }

    /**
     * Read Information from `staffDef`.
     * Is used to create //part-list (including //part-groups and //score-parts).
     * Saves partListIds to retrieve //score-parts from score.partList later.
     * @param staffDef
     */
    private void processStaffDef(Element staffDef) {
        ScorePart scorePart = new ScorePart();
        int num = 1;
        if(!this.partListIds.isEmpty()){ // Fill partListId with ids of the parts. Parts are based on staffGrp Children in a score or any children (exept section)
            String lastId = this.partListIds.get(this.partListIds.size()-1);
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(lastId);
            matcher.find(); //no IF, since I build the ids always with digits, but necessary to allow matcher.group() to find smth
            String numberStr = matcher.group();
            num += Integer.parseInt(numberStr);

        }
        scorePart.setId("s" + num);
        // partname
        PartName partName = new PartName();
        Element label = Helper.getFirstChildElement("label", staffDef);
        if(label != null){
            partName.setValue(label.getValue());
        }
        scorePart.setPartName(partName); // score-part has to contain at least one child-element (cannot be empty)

        this.partListIds.add(scorePart.getId());
        this.partList.getPartGroupOrScorePart().add(scorePart);
        if(!Objects.requireNonNull(Helper.getParentElement(staffDef)).getLocalName().equals("staffGrp")) this.addPartlistToScore(); // add only when there is no staffGrp as parent, which means that the call of this method isn't nested in processStaffGrp

    }

    /**
     * DEclare currentPartTW and add it to the currentMeasure
     * @param staff
     */
    public void processStaffTW(Element staff){
        String staffN = staff.getAttributeValue("n");
        ScorePart sp = null;
        if(staffN != null) { // if score-part cannot be found by n attribute, create a new score-part at this position.
            sp = this.findScorePartByN(staff.getAttributeValue("n"));

        }else{ // if there is no staffN, try to find the staffIdx and the corresponding score part
            int staffIdx = Helper.getAllChildElements("staff", Helper.getParentElement(staff)).indexOf(staff);
            if(staffIdx >= 0){
                sp =  this.findScorePartByN("" + (staffIdx +1));
            }
        }
        if(sp == null){
            int idx = -1;
            if(this.currentPartTW != null) {
                idx = this.currentScoreTimewise.getPartList().getPartGroupOrScorePart().indexOf(this.currentPartTW);
            }
            String id = "";
            if(idx == -1) { // create an intermediate id to be added to the part-list
                id = "1_1"; // 1_1 is the first score part which will be created to which no score part could be found
            }else if(this.currentPartTW != null){
                String cpID = ((ScorePart) this.currentPartTW.getId()).getId(); //(String) this.currentPartTW.getId();
                if(cpID != null && !cpID.isEmpty()) {
                    char lastDigitC = cpID.charAt(cpID.length() - 1);
                    int lastDigitInt = Character.getNumericValue(lastDigitC);
                    lastDigitInt += 1;
                    id += "_" + lastDigitInt;
                }else{
                    id = String.valueOf(UUID.randomUUID()); // last resort
                }
            }
            id = "s" + id;
            ScorePart scorePart = new ScorePart();
            scorePart.setId(id);
            PartName pn = new PartName();
            pn.setValue("Unknown Part");
            scorePart.setPartName(pn);
            this.currentScoreTimewise.getPartList().getPartGroupOrScorePart().add(idx + 1, scorePart);
            sp = scorePart;
        }
        ScoreTimewise.Measure.Part p = new ScoreTimewise.Measure.Part();
        p.setId(sp);
        this.currentPartTW = p;
        for(Barline bl : this.barlines){
            this.addObjectToMeasureOrPart(bl);
        } // barlines will be initiated before every measure
        this.currentMeasureTW.getPart().add(p);
    }

    /**
     * Update this.partList for current score after processing
     */
    private void addPartlistToScore(){
        ScorePartwise sp = new ScorePartwise(); // Cloning here is the safest way to transfer Objects
        sp.setPartList(this.partList);

        for(Object p : Objects.requireNonNull(this.clone(sp)).getPartList().getPartGroupOrScorePart()) // there can be multiple staffgroups so just add each Part or Group one by one
        {
            if(this.twIsCurrent){
                this.currentScoreTimewise.getPartList().getPartGroupOrScorePart().add(p);
            }else if(this.pwIsCurrent){
                this.currentScorePartwise.getPartList().getPartGroupOrScorePart().add(p);
            }
        }
    }

    /**
     * Write a new measure to the current score-timewise.
     * @param measure
     */
    private void createMeasureListTW(Element measure){
        ScoreTimewise.Measure m =  new ScoreTimewise.Measure();
        List<ScoreTimewise.Measure> mList = this.currentScoreTimewise.getMeasure();

        String n = ""; // add numbers on the order in the measure list
        if (!mList.isEmpty()) {
            //n = mList.get(mList.size() - 1).getNumber();
            n += (mList.size() + 1);
        } else {
            n = "1";
        }

        m.setNumber(n);
        mList.add(m);
    }


    /**
     *  Write a new measure to the current score-partwise.
     *  Preprocess tielist and barline repetitions and store for later use.
     * @param measure
     */
    private void processMeasurePW(Element measure){
        ScorePartwise.Part.Measure m =  new ScorePartwise.Part.Measure();
        List<ScorePartwise.Part.Measure> mList = this.currentPartPW.getMeasure();

        if(this.measureListMEI.isEmpty()) {
            Nodes ml = Helper.getClosest("part", measure).query(".//mei:measure", this.xPathContext); //Helper.getAllChildElements("measure", Helper.getParentElement(measure));
            for (Node node : ml) {
                this.measureListMEI.add((Element) node);
            }
        }
        if(!this.tieListMEI.isEmpty()) {
            this.prevMeasureTieListMEI = (LinkedList<Element>) this.tieListMEI.clone();
        }
        this.tieListMEI = Helper.getAllChildElements("tie", measure);

        String n = ""; // add numbers on the order in the measure list
        if (!mList.isEmpty()) {
            n += (mList.size() + 1);
        } else {
            n = "1";
        }

        m.setNumber(n);
        this.currentMeasurePW = m;
        this.barlines = new LinkedList<>();
        this.createRepeatFromMeasure(measure);
        for(Barline bl : this.barlines){
            this.addObjectToMeasureOrPart(bl);
        }
        mList.add(this.currentMeasurePW);
    }

    /**
     * This element is closely linked to the determination of `//voice` in `//part`. The transition to a set of `//note` with a new voice value is marked by a `//measure/backup` element.
     * That uses the accumulated durations of the previous `~/note` elements to determine its `//duration`. For this the convertMusic() recursion continues in this method.
     *
     * @param layer
     */
    private void processLayer(Element layer){
        String voice = layer.getAttributeValue("n");
        voice = voice != null ? voice : this.findElementInParent(layer).get("num").toString();
        this.currentVoice = voice;

        // check if the previous Attribute in the current context is the same,
        // if not add the new Attribute Element.
        if(this.isPrevAttributeDifferent(layer)){
            Attributes localAttr = this.createAttributesFromDiffDefinition(); // needs localaddributes as "copy" of currentAttribute, since currentAttr can change over time
            this.defDiff = null; // don't forget to set null, since it is crucial to test for empty defDiff in isPrev...
            if (this.twIsCurrent ) this.currentPartTW.getNoteOrBackupOrForward().add(localAttr);
            if (this.pwIsCurrent ) this.currentMeasurePW.getNoteOrBackupOrForward().add(localAttr);
        }

        this.convertMusic(layer); // recursion will be initiated here since further processing could apply after the layer is completely mapped.

        // get all the accumulated durations if there is a layer sibling
        // place backward before processing next layer
        Element layerSibling = Helper.getNextSiblingElement("layer", layer);
        if(layerSibling != null) {
            BigDecimal accumulatedDurs = BigDecimal.ZERO;
            if (this.twIsCurrent) accumulatedDurs = this.accumulate(this.currentPartTW.getNoteOrBackupOrForward());
            if (this.pwIsCurrent) accumulatedDurs = this.accumulate(this.currentMeasurePW.getNoteOrBackupOrForward());
            if(accumulatedDurs.compareTo(BigDecimal.ZERO) != 0 || Helper.getAllChildElements(layer).isEmpty()) { // there is a backup with duration zero, if the previous layer has a space with no durations at all
                Backup backup = new Backup();
                backup.setDuration(accumulatedDurs);
                this.addObjectToMeasureOrPart(backup);
            }
        }
    }

    /**
     * A `keySig` always corresponds as a `~/measure/attributes/key` just before the first `~/measure/note`.
     * This puts the key at the beginning of a measure (left to the left bar line or beginning of first measure).
     * Occurrences in `staffDef` or as a descendant of `layer` are processed the same.
     * @param keySig
     */
    private void processKeySig(Element keySig){
        String keySigVal = keySig.getAttributeValue("sig");
        if(keySigVal == null || keySigVal.isEmpty()) return;
        Key key = new Key();
        BigInteger fifths = new BigInteger(keySigVal.substring(0, 1)); // number of fifths is determined by first number in sig attribute
        if(keySigVal.contains("f")){ // if it's flat, make number negative
            fifths = fifths.multiply(new BigInteger("-1"));
        }
        key.setFifths(fifths);
        if(Helper.getParentElement(keySig).getLocalName().equals("layer")){
            Attributes attr = new Attributes();
            attr.getKey().add(key);
            this.addObjectToMeasureOrPart(attr);
        }
    }

    /**
     * Process clef information of clef element.
     * May include shape, line dis and dis.place to determine the actual clef. No defaults.
     * The clef is written to an attribue element which is placed at beginning of part or measure.
     * Clef will be placed at position of occurrence.
     * @param clefElement
     */
    private void processClef(Element clefElement) {
        Clef clef = new Clef();
        String clefShape = clefElement.getAttributeValue("shape");
        String clefLine = clefElement.getAttributeValue("line");
        String dis = clefElement.getAttributeValue("dis");
        String disPlace = clefElement.getAttributeValue("dis.place");
        boolean addClef = false;
        if(clefLine != null && !clefLine.isEmpty()){
            clef.setLine(new BigInteger(clefLine));
            addClef = true;
        }
        if(clefShape != null && !clefShape.isEmpty() && addClef){
            clef.setSign(ClefSign.valueOf(clefShape));
        }else{
            addClef = false;
        }

        if(dis != null && !dis.isEmpty() && disPlace!= null && !disPlace.isEmpty()){
            int change = 0;
            switch(dis){
                case "8":
                    change = 1;
                    break;
                case "15":
                    change = 2;
            }
            if(disPlace.equals("below")) change *= -1;
            clef.setClefOctaveChange(new BigInteger(String.valueOf(change)));
        }

        if(addClef){
            if(Helper.getParentElement(clefElement).getLocalName().equals("layer")){
                Attributes attr = new Attributes();
                attr.getClef().add(clef);
                this.addObjectToMeasureOrPart(attr);
            }
        }
    }

    /**
     * A `meterSig` always corresponds to a `//measure/attributes/time` at the beginning of a `//measure`.
     * There are no new times in the middle of a measure.
     *
     * If `@sym` is not present, both attributes have to be present to process `meterSig`.
     * - `@count` => `//time/beats`
     * - `@unit` => `//time/beat-type`
     *
     * Has first priority to be mapped. `@count` and `@unit` do not have to be present.
     * The attribute is directly mapped to `//time//symbol`.
     *
     * @param meterSig
     */
    private void processMeterSig(Element meterSig){
        Time time = new Time();
        String meterSym = meterSig.getAttributeValue("sym");
        String meterCount = meterSig.getAttributeValue("count");
        String meterUnit = meterSig.getAttributeValue("unit");
        List<JAXBElement<String>> timeSig = time.getTimeSignature();
        boolean addTime = false;
        if(meterSym != null && !meterSym.isEmpty()){
            time.setSymbol(TimeSymbol.fromValue(meterSym));
            addTime = true;
        }
        if(meterCount != null && !meterCount.isEmpty()){
            QName qname = new QName("beats");
            timeSig.add(
                    new JAXBElement<>(qname, String.class, meterCount)
            );
            addTime = true;
        }
        if(meterUnit != null && !meterUnit.isEmpty() && addTime){
            QName qname = new QName("beat-type");
            timeSig.add(
                    new JAXBElement<>(qname, String.class, meterUnit)
            );
        }else{
            addTime = false;
        }
        if(addTime){
//            if(this.currentAttributes == null) this.currentAttributes = new Attributes();
//            this.currentAttributes.getTime().clear();
//            this.currentAttributes.getTime().add(time);
        }
    }

    /**
     * Add Object to current part or current measure depending on the current processed score (timewise or partwise).
     * Is useful, if there are attribute changes in the middle of the measure.
     * Accepts Note.class, Backup.class, Forward.class, Direction.class, Attributes.class, Harmony.class, FiguredBass.class, Print.class, Sound.class, Barline.class, Grouping.class, Link.class, Bookmark.class.
     * @param o
     */
    private void addObjectToMeasureOrPart(Object o){
        Class<?>[] allowedClasses = new Class<?>[]{ Note.class, Backup.class, Forward.class, Direction.class, Attributes.class, Harmony.class, FiguredBass.class, Print.class, Sound.class, Barline.class, Grouping.class, Link.class, Bookmark.class};
        boolean doAdd = false;
        for (Class<?> c : allowedClasses) {
            if (c.isInstance(o)) {
                doAdd = true;
                break;
            }
        }
        if (this.twIsCurrent && doAdd) this.currentPartTW.getNoteOrBackupOrForward().add(o);
        if (this.pwIsCurrent && doAdd) this.currentMeasurePW.getNoteOrBackupOrForward().add(o);
    }


    /**
     * Find if previous definition is different from current one, set new this.currentDefinition anf this.diffDef.
     * @param e
     * @return true: previous definition is different; false: previous definition is the same
     */
    private boolean isPrevAttributeDifferent(Element e){
        Element parentStaff = Helper.getParentElement(e);
        Element parentMeasure = Helper.getParentElement(parentStaff);
        Element prevMeasure = Helper.getPreviousSiblingElement("measure", parentMeasure);
        Element section = Helper.getParentElement(parentMeasure);
        if(prevMeasure == null && section.getLocalName().equals("section") && Helper.getFirstChildElement("measure", section).equals(parentMeasure)){ // must be first measure in section to assign, then look for last measure in previous section
            Element prevSection = Helper.getPreviousSiblingElement("section", section); // find previous section
            if(prevSection != null){
                List<Element> measureChildren = Helper.getAllChildElements("measure", prevSection);
                measureChildren = measureChildren == null || measureChildren.isEmpty() ? Helper.getAllDescendantsByName("measure", prevSection) : measureChildren; // measures can be hidden in endings
                if(measureChildren != null && !measureChildren.isEmpty()) prevMeasure = measureChildren.get(measureChildren.size() - 1); // get last measure in measure list
            }
        }

        DefinitionType prevDefinition = null; // Definition of the previous staff/part
        DefinitionType currentDef = this.findCorrespondingDefinition(parentStaff); // Defintition from the current staff/part, will be compared to prevDefinition
        //some special treatment for timewise: find the definition of the previous part (= staff of MEI)
        if(this.twIsCurrent && e.getLocalName().equals("layer") && prevMeasure != null){ // find prevDefinition
            String staffN = parentStaff.getAttributeValue("n");
            int staffIdx = 0;
            Element targetStaff = null;
            if(staffN == null || staffN.isEmpty()){ // Find a targetstaff to which a definition should be found
                staffIdx = Helper.getAllChildElements("staff", parentMeasure).indexOf(parentStaff);
                targetStaff = Helper.getAllChildElements("staff", prevMeasure).get(staffIdx);

            }else{ // if staff n is not to be found, find element by index (compared with n attribute of element)
                List<Element> sList =  Helper.getAllChildElements("staff", prevMeasure);
                for(Element el : sList){
                    String elN = el.getAttributeValue("n");
                    if(elN != null && !elN.isEmpty()) {
                        if (elN.equals(staffN)) {
                            targetStaff = el;
                            break;
                        }
                    }
                }
                if(targetStaff == null){
                    if(Integer.parseInt(staffN) - 1 == Helper.getAllChildElements("staff", prevMeasure).size()){
                        // if out of bounds: there is a missing staff in the previous measure, despite having a staffDef
                        if(this.twIsCurrent) this.addPartToPrevMeasure(); // adding a missing part is currently only implemented in Timewise
                        // return true to progress without changing the currentDefinition
                        return true;
                    }else{
                        targetStaff = Helper.getAllChildElements("staff", prevMeasure).get(Integer.parseInt(staffN) - 1);
                    }
                }
            }

            if(targetStaff == null){
                prevDefinition = this.findCorrespondingDefinition(e); // last resort, if no targetStaff can be found;
            }else{
                prevDefinition = this.findCorrespondingDefinition(targetStaff);
            }
        }else{ // only for partwise scores
            prevDefinition = this.currentDefinition; // from the perspective of the current processed Element, the previous Definition is the one which was considered to be the current one.
        }


        if(prevMeasure == null || this.currentDefinition == null){
            this.currentDefinition = currentDef;
            this.defDiff = currentDef;
            return true;
        }
        boolean b = false; // if definitions cannot be found, then a change in attributes is asserted (will return isPrevAttributeDifferent = true)
        if(prevDefinition != null && currentDef != null) {
            this.currentDefinition = currentDef;
            b = prevDefinition.equals(currentDef);
            if (!b) { // the definitions may not be equal, so lets check if at least prevdef contains currentDef
                b = prevDefinition.hasSubset(currentDef);
                this.defDiff = prevDefinition.getDiff();//diff was created while checking for subset
                //if clef changed, transfer all other clef related attributes
                if(this.defDiff.getClefShape() != null){
                    this.defDiff.setClefLine(currentDef.getClefLine());
                    this.defDiff.setClefDisPlace(currentDef.getClefDisPlace());
                    this.defDiff.setClefDis(currentDef.getClefDis());
                }
                //if meter changed, transfer all other meter related attributes
                if(this.defDiff.getMeterCount() != null || this.defDiff.getMeterUnit() != null || this.defDiff.getMeterSym() != null){
                    this.defDiff.setMeterCount(currentDef.getMeterCount());
                    this.defDiff.setMeterUnit(currentDef.getMeterUnit());
                    this.defDiff.setMeterSym(currentDef.getMeterSym());
                }
            }
            if (!b) { // currentDef is not equal du prevDef and is also not in the subset of prevDef. So, there is a new currentDefinition.
                this.currentDefinition = currentDef;
            }
        }

        return !b;
    }

    /**
     * Add a part to previous mesaure (in Timewise), if there is a staff (in MEI) missing.
     */
    public void addPartToPrevMeasure(){
        List<ScoreTimewise.Measure> mList = this.currentScoreTimewise.getMeasure();
        int measureIdx = mList.indexOf(this.currentMeasureTW); // find idx of currentMeasure
        ScoreTimewise.Measure prevMeasure = mList.get(measureIdx-1); // get previous measure
        ScoreTimewise.Measure.Part newPart = new ScoreTimewise.Measure.Part(); //create a new part for this measure
        int partIdx = this.currentMeasureTW.getPart().indexOf(this.currentPartTW);
        ScorePart sp = findScorePartByN("" + (partIdx+1));
        if(sp == null) return;
        // find corresponding duration for note of new ScorePart (any part is sufficient)
        ScoreTimewise.Measure.Part prevPart = this.currentMeasureTW.getPart().get(partIdx-1);
        BigDecimal dur = this.accumulate(prevPart.getNoteOrBackupOrForward());
        if(dur.compareTo(BigDecimal.ZERO) == 0) return; // dur must be > 0 or else the measure cannot be defined
        newPart.setId(sp);
        Note n = new Note();
        n.setVoice("1");
        NoteType nt = new NoteType();
        nt.setValue(Helper.duration2word(dur.toString()));
        n.setType(nt);
        n.setRest(new Rest()); // new Note is rest with duration of any sibling measure
        n.setDuration(dur);
        newPart.getNoteOrBackupOrForward().add(n);
        prevMeasure.getPart().add(newPart);
    }

    /**
     * Process note Element and add it to the given part or measure, depending on partwise or timewise score.
     * @param note
     */
    private void processNote(Element note){
        this.currentNote = this.createNote(note);
        if(this.currentNote != null){
           if(this.twIsCurrent){
               this.currentPartTW.getNoteOrBackupOrForward().add(this.currentNote);
           }else if(this.pwIsCurrent){
               this.currentMeasurePW.getNoteOrBackupOrForward().add(this.currentNote);
           }
        }
    }

    /**
     * Map the value of accid element to accidental and alteration.
     * If accid.ges attribute is present, neglect the accidental.
     * @param accid
     */
    private void processAccid(Element accid){
        String accidVal = accid.getAttributeValue("accid");
        accidVal = accidVal == null ? accid.getAttributeValue("accid.ges") : accidVal;
        if(accidVal != null && !accidVal.isEmpty()) {
            Pitch pitch = this.currentNote.getPitch();
            pitch.setAlter(BigDecimal.valueOf(Helper.accidString2decimal(accidVal)));
            if (accid.getAttributeValue("accid.ges") == null) {
                Accidental acc = new Accidental();
                acc.setValue(AccidentalValue.fromValue(Helper.accidString2word(accidVal)));
                this.currentNote.setAccidental(acc);
            }
        }
    }

    /**
     * Determine the number of dot elements within a note  element.
     * The value of a `dot` element corresponds to the number of `~/note/dot` elements,
     * i.e. a value of "2" in `dot` results in two separate `~/dot` elements within a `~/note` in MusicXML.
     * May occur as a child of `note` and `rest`.
     * @param dot
     */
    private void processDot(Element dot){
        String dotVal = dot.getValue();
        try {
            if (dotVal != null && !dotVal.isEmpty()) {
                this.currentNote.setPrintDot(YesNo.YES);
                this.currentNote.getDot().clear();
                for (int i = 0; i < Integer.parseInt(dotVal); i++) {
                    EmptyPlacement ep = new EmptyPlacement();
                    ep.setPlacement(AboveBelow.ABOVE);
                    this.currentNote.getDot().add(ep);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @param stem
     */
    private void processStem(Element stem){
        String stemVal = stem.getAttributeValue("dir");
        if(stemVal != null && !stemVal.isEmpty()){
            Stem s = new Stem();
            s.setValue(StemValue.fromValue(stemVal));
        }
    }


    /**
     * Accumulated durations of given notes.
     * Can be used to determine durations for //forward or //backup Elements
     * @param oList
     * @return
     */
    private BigDecimal accumulate(List<Object> oList){
        BigDecimal accumulatedDurs = BigDecimal.ZERO;
        for(Object n : oList){
            if(n instanceof Note){
                accumulatedDurs = accumulatedDurs.add(((Note) n).getDuration());
            }
        }
        return accumulatedDurs;
    }

    /**
     * Returns map that has a "num" and a "element" key.
     * num: Index + 1 in the sequence of similar children of its parent.
     * @param e
     * @return i
     */
    private Map<String, Object> findElementInParent(Element e){
        Element parent = (Element) e.getParent();
        List<Element> eList = Helper.getAllChildElements(e.getLocalName(), parent);
        Map<String, Object> map = new HashMap<>();
        int counter = 1;
        for(Element child : eList){
            if(child.equals(e)){
                map.put("num", counter);
                map.put("element", e);
            }
            counter++;
        }
        return map;
    }

    /**
     * Map `scoreDef[@music.name]` => `//defaults/music-font`
     * Map `scoreDef[@text.name]` => `//defaults/muisc-font`
     * @param fontname
     * @param targetElement
     */
    private void addEmptyFontDefaults(String fontname, String targetElement){
        if(fontname == null || fontname.isEmpty()) return;
        EmptyFont emptyFont = new EmptyFont();
        emptyFont.setFontFamily(fontname);
        ArrayList<String> acceptedTargets = new ArrayList<>(Arrays.asList("word-font", "music-font"));
        if(!acceptedTargets.contains(targetElement)){return;}
        if(targetElement.equals("music-font")) {this.header.getDefaults().setMusicFont(emptyFont);}
        if(targetElement.equals("word-font")) {this.header.getDefaults().setWordFont(emptyFont);}
    }

    /**
     * Maps `scoreDef[@lyric.name]` => `//defaults/lyric-font`.
     * Has own method, sice it does not use Emptyfont.class to set value.
     * @param fontname
     */
    private void addLyricFontDefaults(String fontname){
        if(fontname == null || fontname.isEmpty()) return;
        LyricFont lyricFont = new LyricFont();
        lyricFont.setFontFamily(fontname);
        if(fontname != null && !fontname.isEmpty()){this.header.getDefaults().getLyricFont().add(lyricFont);}
    }

    /**
     * Process layout information to be set //defaults: /page-layout, /system-layout, /staff-layout.
     * @param value
     * @param targetElement
     * @param layoutPrefix
     */
    private void addLayoutDefault(String value, String targetElement, String layoutPrefix){
        ArrayList<String> acceptedPrefix = new ArrayList<>(Arrays.asList("page", "system", "staff"));
        Defaults pageDefaults = this.header.getDefaults();

        if(pageDefaults.getPageLayout() == null){
            pageDefaults.setPageLayout(new PageLayout());
        }
        if(pageDefaults.getSystemLayout() == null){
            pageDefaults.setSystemLayout(new SystemLayout());
        }

        PageLayout pageLayout = pageDefaults.getPageLayout();
        SystemLayout systemLayout = pageDefaults.getSystemLayout();
        List<StaffLayout> staffLayouts = pageDefaults.getStaffLayout(); // is existent or will be created by proxymusic


        if(!acceptedPrefix.contains(value)){return;}
        BigDecimal val = new BigDecimal(value);

        if(layoutPrefix.equals("page")){
            PageMargins margins = new PageMargins();
            switch (targetElement){
                case "scaling":
                    //TODO: Some Processing has to be made here
                    break;
                case "height":
                case "page-height":
                    pageLayout.setPageHeight(val);
                    break;
                case "width":
                case "page-width":
                    pageLayout.setPageWidth(val);
                    break;
                case "left-margin":
                    margins.setLeftMargin(val);
                    break;
                case "right-margin":
                    margins.setRightMargin(val);
                    break;
                case "top-margin":
                    margins.setTopMargin(val);
                    break;
                case "bottom-margin":
                    margins.setBottomMargin(val);
                    break;
                default:
                    System.out.println(targetElement + " is not implemented yet, or is not a valid element name");
            }

            pageLayout.getPageMargins().add(margins);

        }else if(layoutPrefix.equals("system")){
            SystemMargins margins = new SystemMargins();
            switch (targetElement){
                case "left-margin":
                    margins.setLeftMargin(val);
                    break;
                case "right-margin":
                    margins.setRightMargin(val);
                    break;
                case "distance":
                case "system-distance":
                    systemLayout.setSystemDistance(val);
                    break;
                case "top-system-distance":
                    systemLayout.setTopSystemDistance(val);
                    break;
                default:
                    System.out.println(targetElement + " is not implemented yet, or is not a valid element name");
            }
        }else if(layoutPrefix.equals("staff")){
            StaffLayout staffLayout = new StaffLayout();
            switch(targetElement){
                case "distance":
                case "staff-distance":
                    staffLayout.setStaffDistance(val);
                    staffLayouts.add(staffLayout);
            }

        }
    }

    /**
     * Process layout information, if found.
     * Attributes that may contribute to staff information, are processed in findCorrespondingDefintion().
     * @param scoreDef
     */
    private void processScoreDef(Element scoreDef) {
        String textName = scoreDef.getAttributeValue("text.name"); // /defaults/word-font
        String musicName = scoreDef.getAttributeValue("music.name"); // defaults/music-font
        String lyricName = scoreDef.getAttributeValue("lyric.name"); // /default/lyric-font
        String fontname = scoreDef.getAttributeValue("fontname"); // may be applicable to lyric-font and word-font

        // Process fonts that may hide in the scoreDef attributes
        if(fontname != null && !fontname.isEmpty()){
            textName = fontname;
            lyricName = fontname;
        }

        this.addEmptyFontDefaults(textName, "word-font");
        this.addEmptyFontDefaults(musicName, "music-font");
        this.addLyricFontDefaults(lyricName);

        //Page Layout Values
        String pageHeight = scoreDef.getAttributeValue("page.height");
        String pageWidth = scoreDef.getAttributeValue("page.width");
        String pageLeftMargin = scoreDef.getAttributeValue("page.leftmar");
        String pageRightMargin = scoreDef.getAttributeValue("page.rightmar");
        String pageTopMargin = scoreDef.getAttributeValue("page.topmar");
        String pageBottomMargin = scoreDef.getAttributeValue("page.botmar");
        String pageScale = scoreDef.getAttributeValue("page.scale");
        String prefix = "page";

        this.addLayoutDefault(pageHeight, "page-height", prefix);
        this.addLayoutDefault(pageWidth, "page-width", prefix);
        this.addLayoutDefault(pageLeftMargin, "left-margin", prefix);
        this.addLayoutDefault(pageRightMargin, "right-margin", prefix);
        this.addLayoutDefault(pageTopMargin, "top-margin", prefix);
        this.addLayoutDefault(pageBottomMargin, "bottom-margin", prefix);
        this.addLayoutDefault(pageScale, "scaling", prefix);

        //System Layout Values
        String systemRightMargin = scoreDef.getAttributeValue("system.rightmar");
        String systemLeftMargin = scoreDef.getAttributeValue("system.leftmar");
        String systemTopMargin = scoreDef.getAttributeValue("system.topmar");
        String systemSpacing = scoreDef.getAttributeValue("spacing.system");
        prefix = "system";
        this.addLayoutDefault(systemRightMargin, "right-margin", prefix);
        this.addLayoutDefault(systemLeftMargin, "left-margin", prefix);
        this.addLayoutDefault(systemTopMargin, "top-system-distance", prefix);
        this.addLayoutDefault(systemSpacing, "system-distance", prefix);

        //Staff layout values
        prefix = "staff";
        String staffDistance = scoreDef.getAttributeValue("spacing.staff");
        this.addLayoutDefault(staffDistance, "staff-distance", prefix);
    }

    /**
     * Process score Element to fill MusicXML.
     * All MusicXMLs will have the same header since they are all derived from one MEI. Contents may change with further processing.
     *
     * @param score score element
     */
    private void processScore(Element score) {
        // first find the corresponding header to this work (if it exists)
        // Add the header information at the end, since it can change while processing.
        if(this.workList != null && !this.workList.isEmpty()){
            try {
                this.header = this.workList.get(this.getInnerMdivIdx((Element) score.getParent())); // get header for this work, index of inner mdiv should match index in worklist. Score wll be built on the given header.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.checkAndSetPartList();

        // init new score-timewise and set global bools for current processing score.
        // some elements might behave differently with ScoreTimewise and ScorePartwise
        this.nullifyPartwise();
        this.divisions = 0;
        this.measureListMEI = new LinkedList<>();
        this.currentScoreTimewise = new ScoreTimewise();
        this.partListIds = new ArrayList<>();
        this.twIsCurrent = true;
        this.copyHeaderToScore();

        this.mxmls.add(new MusicXml(currentScoreTimewise));
    }

    /**
     * Process parts Element to fill MusicXML.
     * All MusicXMLs will have the same header since they are all derived from one MEI. Contents may change with further processing.
     *
     * @param parts score element
     */
    private void processParts(Element parts) {

        if(this.workList != null && !this.workList.isEmpty()){
            try {
                this.header = this.workList.get(this.getInnerMdivIdx((Element) parts.getParent()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

       this.checkAndSetPartList();

        // init new score-partwise and set global bools for current processing score.
        // some elements might behave differently with ScoreTimewise and ScorePartwise
        this.nullifyTimewise();
        this.divisions = 0;
        this.measureListMEI = new LinkedList<>();
        this.currentScorePartwise = new ScorePartwise(); // give this MusicXML íts own header, since it can change during processing
        this.partListIds = new ArrayList<>();
        this.pwIsCurrent = true;
        this.twIsCurrent = false;
        this.copyHeaderToScore();

        this.mxmls.add(new MusicXml(this.currentScorePartwise));
    }

    /**
     * Keep track of the current parts.
     *
     * @param section
     */
    private void processSectionPW (Element section){
        if(Helper.getFirstChildElement("measure", section) == null) return; // no measures = no musical information to be processed, and can't be mapped to part-list

        Element part = Helper.getClosest("part", section);
        int sectionIdx = Objects.requireNonNull(Helper.getAllChildElements("section", part)).indexOf(section);
        if(sectionIdx > 0 ) return; // set the currentPart just during the first section in the part
        List<Element> parts = Helper.getAllChildElements("part", Helper.getClosest("parts", section));
        int partIdx = parts.indexOf(part);
        String partId = this.partListIds.get(partIdx);
        Object scorePart = findScorePartById(partId);
        ScorePartwise.Part p = new ScorePartwise.Part();
        p.setId(scorePart);
        this.currentPartPW = p;
        this.currentScorePartwise.getPart().add(p);
    }

    /**
     * Find ScorePart by ID.
     *
     * @param id
     * @return if there is no PartList , return null;
     */
    private ScorePart findScorePartById(String id){
        PartList pList = null;
        if(this.twIsCurrent){ // load PartList from currentScore since field partList is a temp element to sequentially transfer Elements to score.
            pList = this.currentScoreTimewise.getPartList();
        }else if(this.pwIsCurrent){
            pList = this.currentScorePartwise.getPartList();
        }
        if(pList == null) return null;

        for(Object sp : pList.getPartGroupOrScorePart()){
            if(sp instanceof ScorePart){
                if(((ScorePart)sp).getId().equals(id)){
                    return (ScorePart)sp;
                }
            }
        }
        return null;
    }

    /**
     * Find ScorePart by N-Attribute (or any number, if known).
     * ID of ScorePart is a combination of "s" + n ("s1", "s2", etc.).
     * @param n
     * @return
     */
    private ScorePart findScorePartByN(String n){
        PartList pList = null;
        if(this.twIsCurrent){ // load PartList from currentScore since field partList is a temp element to sequentially transfer Elements to score.
            pList = this.currentScoreTimewise.getPartList();
        }else if(this.pwIsCurrent){
            pList = this.currentScorePartwise.getPartList();
        }
        if(pList == null) return null;

        for(Object sp : pList.getPartGroupOrScorePart()){
            if(sp instanceof ScorePart){
                String nFromId = ((ScorePart) sp).getId().substring(1);
                if(nFromId.equals(n) && !((ScorePart) sp).getId().contains("_") ){ // only consider the forms like "s1", not "s1_1"
                    return (ScorePart)sp;
                }
            }
        }
        return null;
    }

    /**
     * For each measure, go through all parts and process following staffs, layers, etc.
     * @param measure
     */
    private void processMeasureTW(Element measure){

        this.createMeasureListTW(measure);

        if(this.measureListMEI.isEmpty()) {
            Nodes mList = Helper.getClosest("score", measure).query(".//mei:measure", this.xPathContext);
            for (Node node : mList) {
                this.measureListMEI.add((Element) node);
            }
        }
        if(!this.tieListMEI.isEmpty()) {
            this.prevMeasureTieListMEI = (LinkedList<Element>) this.tieListMEI.clone();
        }
        this.tieListMEI = Helper.getAllChildElements("tie", measure);

        int measureIdx = this.measureListMEI.indexOf(measure);
        this.currentMeasureTW = this.currentScoreTimewise.getMeasure().get(measureIdx);
        this.barlines = new LinkedList<>();
        this.createRepeatFromMeasure(measure); // create repetitions first. They only can be added later, when staff is known in TimeWise.
    }


    /**
     * Create repetitions form measure attribute right and left (can be extended for any process which
     * @param measure
     */
    private void createRepeatFromMeasure(Element measure){
        // right and left can occur both with values "rpstart", "rpend", "rptBoth"
        String right = measure.getAttributeValue("right");
        String left = measure.getAttributeValue("left");

        ArrayList<String> allowed = new ArrayList<>(Arrays.asList("rptstart", "rptend", "rptboth"));
        // ADD RIGHT to end of measure after processing.
        if(right != null && !right.isEmpty()){
            if(!allowed.contains(right)) return;
            Barline bl = new Barline();
            bl.setLocation(RightLeftMiddle.RIGHT);
            Repeat rpt = new Repeat();
            if(right.equals("rptstart")){
                rpt.setDirection(BackwardForward.FORWARD);
            }else if(right.equals("rptend")){
                rpt.setDirection(BackwardForward.BACKWARD);
            }else if(right.equals("rptboth")){
                rpt.setDirection(BackwardForward.BACKWARD);
                Barline bl2 = new Barline();
                bl.setLocation(RightLeftMiddle.RIGHT);
                Repeat rpt2 = new Repeat();
                rpt2.setDirection(BackwardForward.FORWARD);
                this.barlines.add(bl2);
            }
            bl.setRepeat(rpt);
            this.barlines.add(bl);
        }
        if(left != null && !left.isEmpty()){
            if(!allowed.contains(left)) return;
            Barline bl = new Barline();
            bl.setLocation(RightLeftMiddle.LEFT);
            Repeat rpt = new Repeat();
            if(left.equals("rptstart")){
                rpt.setDirection(BackwardForward.FORWARD);
            }else if(left.equals("rptend")){
                rpt.setDirection(BackwardForward.BACKWARD);
            }else if(left.equals("rptboth")){
                rpt.setDirection(BackwardForward.BACKWARD);
                Barline bl2 = new Barline();
                bl.setLocation(RightLeftMiddle.RIGHT);
                Repeat rpt2 = new Repeat();
                rpt2.setDirection(BackwardForward.FORWARD);
                this.barlines.add(bl2);
            }
            bl.setRepeat(rpt);
            this.barlines.add(bl);

        }
    }
    /**
     * Find the index number of the current mdiv.
     * @param mdiv
     * @return any i, if way
     */
    private int getInnerMdivIdx(Element mdiv) throws Exception {
        return this.mei.getAllMdivs().indexOf(mdiv);
    }

    /**
     * clone this header to original header, if is null
     */
    private void checkAndSetOriginalHeader(){
        if(this.originalHeader == null){
            this.originalHeader = clone(this.header);
        }
    }

    /**
     * Create new part list
     */
    private void checkAndSetPartList(){
        if(this.partList == null){
            this.partList = new PartList();
        }
    }

    /**
     * Reset all fields that belong to partwise
     */
    private void nullifyPartwise(){
        this.currentScorePartwise = null;
        this.currentPartPW = null;
        this.currentMeasurePW = null;
        this.pwIsCurrent = false;
    }

    /**
     * Reset all fields that belong to timewise
     */
    private void nullifyTimewise(){
        this.currentScoreTimewise = null;
        this.currentPartTW = null;
        this.currentMeasureTW = null;
        this.twIsCurrent = false;
    }

    /**
     * Clean all unnecessary white space.
     * @param s
     * @return
     */
    private String sanitize(String s){
        s = s.trim();
        s = s.replaceAll("\\r\\n|\\r|\\n", " "); // eliminate all new lines
        s = s.replaceAll("\\s{2,}", " "); // eliminate all whitespace that is at least 2 chars long
        return s;
    }

    /**
     * create Attributes from this.currentDefinition
     * @return
     */
    private Attributes createAttributesFromCurrentDefinition(){
        if(this.currentDefinition == null) return null;
        String clefLine = this.currentDefinition.getClefLine();
        String clefShape = this.currentDefinition.getClefShape();
        String dis = this.currentDefinition.getClefDis();
        String disPlace = this.currentDefinition.getClefDisPlace();
        String meterSym = this.currentDefinition.getMeterSym();
        String meterCount = this.currentDefinition.getMeterCount();
        String meterUnit = this.currentDefinition.getMeterUnit();
        String keySig = this.currentDefinition.getKeySig();

        return this.createAttributes(clefLine, clefShape, dis, disPlace, meterSym, meterCount, meterUnit, keySig);
    }


    /**
     * create Attributes from this.defDiff
     * @return
     */
    private Attributes createAttributesFromDiffDefinition(){
        if(this.defDiff == null) return null;
        String clefLine = this.defDiff.getClefLine();
        String clefShape = this.defDiff.getClefShape();
        String dis = this.defDiff.getClefDis();
        String disPlace = this.defDiff.getClefDisPlace();
        String meterSym = this.defDiff.getMeterSym();
        String meterCount = this.defDiff.getMeterCount();
        String meterUnit = this.defDiff.getMeterUnit();
        String keySig = this.defDiff.getKeySig();

        return this.createAttributes(clefLine, clefShape, dis, disPlace, meterSym, meterCount, meterUnit, keySig);
    }

    /**
     * Map element contents to Attributes Element.
     * Currently used to extract information from staffDef (such as clef, meter and key).
     * @param e
     * @return
     */
    public Attributes createAttributesFromElement(Element e){
        String clefLine = e.getLocalName().equals("clef") ? e.getAttributeValue("line") : e.getAttributeValue("clef.line");
        String clefShape = e.getLocalName().equals("clef") ? e.getAttributeValue("shape") : e.getAttributeValue("clef.shape");
        String dis = e.getLocalName().equals("clef") ? e.getAttributeValue("dis") : e.getAttributeValue("clef.dis");
        String disPlace = e.getLocalName().equals("clef") ? e.getAttributeValue("disPlace") : e.getAttributeValue("clef.dis.place");
        String meterSym = e.getAttributeValue("meter.sym");
        String meterCount = e.getLocalName().equals("meterSig") ? e.getAttributeValue("count") :e.getAttributeValue("meter.count");
        String meterUnit = e.getLocalName().equals("meterSig") ? e.getAttributeValue("unit") : e.getAttributeValue("meter.unit");
        String keySig = e.getLocalName().equals("keySig") ? e.getAttributeValue("sig ") : e.getAttributeValue("key.signature");

        return this.createAttributes(clefLine, clefShape, dis, disPlace, meterSym, meterCount, meterUnit, keySig);
    }

    /**
     * Create a new //attributes element to be inserted into the current measure.
     * @param clefLine
     * @param clefShape
     * @param dis
     * @param disPlace
     * @param meterSym
     * @param meterCount
     * @param meterUnit
     * @param keySig
     * @return
     */
    private Attributes createAttributes(String clefLine, String clefShape, String dis, String disPlace, String meterSym, String meterCount, String meterUnit, String keySig) {
        Attributes attributes = new Attributes();
        boolean doReturn = false;
        Clef clef = new Clef();
        boolean addClef = false;
        if(clefLine != null && !clefLine.isEmpty()){ // clefline must be present! Otherwise, there is no chance to know where to place it.
            clef.setLine(new BigInteger(clefLine));
            addClef = true;
        }
        if(clefShape != null && !clefShape.isEmpty() && addClef){
            clef.setSign(ClefSign.valueOf(clefShape));
        }else{
            addClef = false;
        }

        if(dis != null && !dis.isEmpty() && disPlace!= null && !disPlace.isEmpty()){ // Map dis and dis.place (makes only sence when both are present)
            int change = 0;
            switch(dis){
                case "8":
                    change = 1;
                    break;
                case "15":
                    change = 2;
            }
            if(disPlace.equals("below")) change *= -1;
            clef.setClefOctaveChange(new BigInteger(String.valueOf(change)));
        }
        if(addClef) attributes.getClef().add(clef); doReturn = true;

        Time time = new Time(); // create time!!!
        List<JAXBElement<String>> timeSig = time.getTimeSignature();
        boolean addTime = false;
        if(meterSym != null && !meterSym.isEmpty()){ // meter sym can be used by itself if count and unit are missing
            time.setSymbol(TimeSymbol.fromValue(meterSym));
            addTime = true;
        }
        if(meterCount != null && !meterCount.isEmpty()){
            QName qname = new QName("beats");
            timeSig.add(
                    new JAXBElement<>(qname, String.class, meterCount)
            );
            addTime = true;
        }
        if(meterUnit != null && !meterUnit.isEmpty() && addTime){
            QName qname = new QName("beat-type");
            timeSig.add(
                    new JAXBElement<>(qname, String.class, meterUnit)
            );
        }else{
            addTime = false;
        }
        if(addTime) attributes.getTime().add(time); doReturn = true;

        if(keySig != null && !keySig.isEmpty()){ // Keysig
            Key key = new Key();
            BigInteger fifths = new BigInteger(keySig.substring(0, 1));
            if(keySig.contains("f")){
                fifths = fifths.multiply(new BigInteger("-1"));
            }
            key.setFifths(fifths);
            attributes.getKey().add(key);
            doReturn = true;
        }

        String ppq = this.currentDefinition.getPpq();
        BigDecimal divisions;
        if(this.divisions == 0) {
            if (ppq != null && !ppq.isEmpty()) {
                divisions = BigDecimal.valueOf(Long.parseLong(ppq));
            } else {
                divisions = this.findDivisions();
            }
            this.divisions = divisions.intValue();
        }

        if(this.divisions > 0){ // set divisions
            attributes.setDivisions(BigDecimal.valueOf(this.divisions));
            doReturn = true;
        }

        if(doReturn) return attributes;
        return null;
    }

    /**
     * Process Element which has attributes which can be mapped to //note,
     * such as 'note', 'rest', 'mRest', 'space', 'mSpace'
     * @param e
     */
    private Note createNote(Element e){
        Note n = new Note();
        Rest rest;
        n.setVoice(this.currentVoice);
        switch(e.getLocalName()){
            case "mSpace":
                n.setPrintObject(YesNo.NO);
            case "mRest":
                rest = new Rest();
                rest.setMeasure(YesNo.YES);
                n.setRest(rest);
                break;
            case "space":
                n.setPrintObject(YesNo.NO);
            case "rest":
                rest = new Rest();
                rest.setMeasure(YesNo.NO);
                n.setRest(rest);
                break;
            case "note": // first take the proper attribute, then *.ges attribute, if nothing found
                Pitch pitch = new Pitch();
                String step = e.getAttributeValue("pname");
                step = step == null ? e.getAttributeValue("pname.ges") : step;

                String accid = e.getAttributeValue("accid");
                accid = accid == null ? e.getAttributeValue("accid.ges") : accid;

                String octave = e.getAttributeValue("oct");
                octave = octave == null ? e.getAttributeValue("oct.ges") : octave;

                //take pnum only as a fallback, when there is no step, accid or octave
                if(e.getAttribute("pnum") != null && step == null && accid == null && octave == null){
                    step = e.getAttributeValue("pnum");
                    String[] arr = new String[3];
                    Helper.midi2PnameAccidOct(false, Double.parseDouble(step), arr);
                    if(arr[0] != null && !arr[0].isEmpty()){
                        step = arr[0];
                        accid = Helper.accidDecimal2String(arr[1]);
                        octave = arr[2].substring(0,1);
                    }
                }

                if(step != null && !step.isEmpty()){
                    pitch.setStep(Step.fromValue(step.toUpperCase()));
                }
                if(accid != null && !accid.isEmpty()){
                    pitch.setAlter(BigDecimal.valueOf(Helper.accidString2decimal(accid)));
                    if(e.getAttributeValue("accid.ges") == null) { // when accid.ges is found, don't write the accidental, alteration is sufficient
                        Accidental acc = new Accidental();
                        acc.setValue(AccidentalValue.fromValue(Helper.accidString2word(accid)));
                        n.setAccidental(acc);
                    }
                }
                String defaultOct = this.currentDefinition.getOctDefault(); // check if default oct is present,
                if(octave != null && !octave.isEmpty()){
                    pitch.setOctave(Integer.parseInt(octave));
                }else if(defaultOct != null && !defaultOct.isEmpty()){
                    pitch.setOctave(Integer.parseInt(defaultOct));
                }
                n.setPitch(pitch);

                // if note has a chord as a parent, add chord tag to all elements following the first note (= first note has no chord tag)
                Element parentChord = Helper.getClosest("chord", e);
                if(parentChord != null){
                    int noteIdx = Helper.getAllChildElements("note", parentChord).indexOf(e);
                    if(noteIdx >= 1){
                        n.setChord(new Empty());
                    }

                }

                Stem stem = new Stem();
                String stemDir = parentChord != null ? parentChord.getAttributeValue("stem.dir") : e.getAttributeValue("stem.dir");
                if(stemDir != null && !stemDir.isEmpty()){
                    stem.setValue(StemValue.fromValue(stemDir));
                    n.setStem(stem);
                }
//                else {
//                    if (this.currentVoice.equals("1")) {
//                        stem.setValue(StemValue.UP);
//                    } else {
//                        stem.setValue(StemValue.DOWN);
//                    }
//                }
                //n.setStem(stem);
                break;
            default:
                System.out.println("Creating a note Element for " + e.getLocalName() + " is not implemented yet.");
                return null;
        }

        // tuplet
        this.processTuplet(n, e);

        //ties
        this.processTies(n, e);

        //Set Duration and NoteType
        //If the note has no duration attribute itself, find the next parent with duration
        //can be chord, bTrem, fTrem, trem,
        Element closestWithDuration = Helper.getClosestByAttr("dur", e);
        Element elementWithDuration = (e.getAttributeValue("dur") == null ||
                e.getAttributeValue("dur").isEmpty()) &&
                closestWithDuration != null ?
                closestWithDuration : e;
        elementWithDuration = elementWithDuration == null ? e : elementWithDuration; // if there is no duration, this is probably a mRest

        String durDefault = this.currentDefinition.getDurDefault();
        String durAttr = elementWithDuration.getAttributeValue("dur");
        String dotAttr =  elementWithDuration.getAttributeValue("dots");

        if(durDefault != null && !durDefault.isEmpty() && durAttr == null){
            durAttr = durDefault;
        }
        if(durAttr != null){
            NoteType nt = new NoteType();
            nt.setValue(Helper.duration2word(durAttr));
            n.setType(nt);
        }
        if(dotAttr != null && !dotAttr.isEmpty()){
            n.setPrintDot(YesNo.YES);
            for(int i = 0; i < Integer.parseInt(dotAttr); i++){
                EmptyPlacement ep = new EmptyPlacement();
                ep.setPlacement(AboveBelow.ABOVE);
                n.getDot().add(ep);
            }
        }
        BigDecimal duration = this.getDuration(elementWithDuration);
        if(duration != null && duration.compareTo(BigDecimal.ZERO) != 0){ // no zeros allowed! a note with no duration is no note
            n.setDuration(duration);
            return n;
        }
        return null;
    }

    /**
     * Tuplets can be a parent element of the note or an attribute of the note.
     * Process num and numbase if the parent is present. Otherwise, map the tuplet attribute to their respective note.
     * @param note
     * @param e
     */
    private void processTuplet(Note note, Element e){
        Element parentTuplet = Helper.getClosest("tuplet", e);
        String tupletAttr = e.getAttributeValue("tuplet");
        String num = "";
        String numbase = "";
        boolean isFirst = false;
        boolean isLast = false;
        if(parentTuplet != null ) { // if there is a tuplet parent element
            num = parentTuplet.getAttributeValue("num");
            numbase = parentTuplet.getAttributeValue("numbase");
            if(num == null || num.isEmpty()) return; // tuplet parent may be present but cannot process ratios
            int numInt = Integer.parseInt(num);
            numbase = (numbase == null || numbase.isEmpty()) && (numInt & (numInt - 1)) == 0 ? "" + numInt : "" + (Integer.highestOneBit(numInt) );
            List<Element> descendants = Helper.getAllDescendantsWithAttribute("dur", parentTuplet);
            isFirst = descendants.indexOf(e) == 0;
            Collections.reverse(descendants); // reverse list to find last element
            isLast = descendants.indexOf(e) == 0;
            this.addTimeModification(note, num, numbase, isFirst, isLast);
        }else if(tupletAttr != null && !tupletAttr.isEmpty()) { // will enter this block when element with Attribute tuplet is found
            String[] tupletVals = tupletAttr.split(" "); // tuplet attribute can contain a list of values devided by spaces
            for(String tVal : tupletVals) {
                Element parentLayer = Helper.getClosest("layer", e); // search beginning with layer, since e may be e.g. in a beam.
                // all tuplet elements have to be in same descendant tree; tuplets can't cross measure boundaries
                List<Element> tupletElements = Helper.getAllDescendantsWithAttribute("tuplet", parentLayer);
                if (tupletElements == null || tupletElements.isEmpty()) break;
                num = "" + tupletElements.size();
                int numInt = Integer.parseInt(num);
                //String tupletNum = tVal.substring(1); //do I even need this?
                if (tVal.contains("i")) {
                    isFirst = true;
                } else if (tVal.contains("t")) {
                    isLast = true;
                }
                // NUM: Number of elements in tuplet; Numbase: 3:2, 4:4, 5:4, 6:4, 7:4, 8:8, 9:8 => if num is 2^x take num, else next smaller 2^x)
                numbase = (numInt & (numInt - 1)) == 0 ? "" + numInt : "" + (Integer.highestOneBit(numInt) );
                this.addTimeModification(note, num, numbase, isFirst, isLast);
            }
        }
    }

    /**
     * Add Timemodification and corresponding Notations to a note.
     * Will be used while processing tuplets
     * @param note
     * @param num actual-notes
     * @param numbase normal-notes
     * @param isFirst is first element of tuplet
     * @param isLast is last element of tuplet
     */
    private void addTimeModification(Note note, String num, String numbase, boolean isFirst, boolean isLast){
        if(numbase != null && !numbase.isEmpty()
                && num != null && !num.isEmpty()
        ){
            TimeModification tm = new TimeModification();
            tm.setActualNotes(new BigInteger(num));
            tm.setNormalNotes(new BigInteger(numbase));
            note.setTimeModification(tm);
            if(isFirst || isLast) {
                Notations notations = new Notations();
                Tuplet tuplet = new Tuplet();
                if(isFirst){
                    tuplet.setBracket(YesNo.YES);
                    tuplet.setType(StartStop.START);
                }else if(isLast){
                    tuplet.setType(StartStop.STOP);
                }
                notations.getTiedOrSlurOrTuplet().add(tuplet);
                note.getNotations().add(notations);
            }
        }
    }

    /**
     * Process ties for a note element.
     * Always prefer the latest tie declaration (even if it occurs after the actual note).
     * If start and stop attributes are not set yet, use note[@tie].
     * @param note
     * @param noteElement
     */
    private void processTies(Note note, Element noteElement){
        LinkedList<Element> combinedList = new LinkedList<>(); // combine list to find out whether there are also ties in the previous measure that apply to the current measure (might happen with measure boundaries)
        combinedList.addAll(this.prevMeasureTieListMEI);
        combinedList.addAll(this.tieListMEI);
        String noteId = Helper.getAttributeValue("id", noteElement);
        for(Element tieElement : combinedList){ // if there are tieElements in the measure
            String startId = tieElement.getAttributeValue("startid");
            startId = startId == null ? null : startId.substring(1); // startId always starts with a #
            String endId = tieElement.getAttributeValue("endid");
            endId = endId == null ? null : endId.substring(1); // endId always starts with a #
            String position = noteElement.getAttributeValue("curvedir");

            boolean startIdIsNull = startId == null || startId.isEmpty();
            boolean endIdIsNull = endId == null || endId.isEmpty();
            if(startIdIsNull && endIdIsNull) continue; // must have some sort of startid or endid, else try next entry

            if(noteId == null || noteId.isEmpty()) return; // return completely when note has no id at all; no association can be made
            Notations notations = new Notations();
            Tied tied = new Tied();
            notations.getTiedOrSlurOrTuplet().add(tied);
            Tie tie;
            if(position != null && !position.isEmpty()){ // curvedir has implications for placement and orientation in MusicXML
                if(position.equals("above")){
                    tied.setOrientation(OverUnder.OVER);
                    tied.setPlacement(AboveBelow.ABOVE);
                }else if(position.equals("below")){
                    tied.setOrientation(OverUnder.UNDER);
                    tied.setPlacement(AboveBelow.BELOW);
                }
            }

            //check start and endid to create new tie and tied elements
            if(noteId.equals(startId)){
                tie = new Tie();
                // always get the latest declaration of the tie
                note.getTie().removeIf(t -> t.getType().equals(StartStop.START));
                note.getTie().add(tie);
                note.getNotations().add(notations);
                tie.setType(StartStop.START);
                tied.setType(StartStopContinue.START);
            }else if(noteId.equals(endId)) {
                tie = new Tie();
                // always get the latest declaration of the tie
                // for this, remove the type elements from list
                note.getTie().removeIf(t -> t.getType().equals(StartStop.STOP));
                note.getTie().add(tie);
                note.getNotations().add(notations);
                tie.setType(StartStop.STOP);
                tied.setType(StartStopContinue.STOP);
            }

            boolean hasStart = false;
            boolean hasStop = false;
            for(Tie t : note.getTie()){ // if the Tie is already defined for start and stop, may only have one each
                if(t.getType().equals(StartStop.START)) hasStart = true;
                if(t.getType().equals(StartStop.STOP)) hasStop = true;
            }
            if(hasStart && hasStop && !this.tieBlacklist.contains(noteId)){ // at this point it is clear that note has a start and a stop
                this.tieBlacklist.add(noteId);
            }
        }

        String tieAttr = noteElement.getAttributeValue("tie"); // here the tie attribute of the note will be processed
        if(tieAttr != null && !tieAttr.isEmpty()){
            if(this.tieBlacklist.contains(noteId)) return; // there are already all starts ad stops set for this note
            Notations notations = new Notations();
            Tied tied = new Tied();
            notations.getTiedOrSlurOrTuplet().add(tied);
            Tie tie = new Tie();
            note.getTie().add(tie);
            note.getNotations().add(notations);
            // in this case above and below cannot be mapped from the MEI!
            if(tieAttr.equals("i")){
                tie.setType(StartStop.START);
                tied.setType(StartStopContinue.START);
            }else if(tieAttr.equals("m")){// m gets two ties, since it is an intermediate between two notes.
                tie.setType(StartStop.STOP);
                tied.setType(StartStopContinue.STOP);
                if(note.getTie().size() < 2) {
                    Tie tie2 = new Tie();
                    tie2.setType(StartStop.START);
                    note.getTie().add(tie2);
                    Tied tied2 = new Tied();
                    tied2.setType(StartStopContinue.START);
                    notations.getTiedOrSlurOrTuplet().add(tied2);
                }
            }else if(tieAttr.equals("t")){
                tie.setType(StartStop.STOP);
                tied.setType(StartStopContinue.STOP);
            }
        }
    }

    /**
     * Find divisions of current MEI to add to //attributes/divisions Element.
     * A division is the number of symbolic durations that are needed to create a quaver, dependent on the shortest duration in the piece.
     *
     * Durations in MEI are represented as denominators: dur=4 => 1/4.
     * Examples: Shortest duration = 16 => division = 4.
     * Shortest duration = 8 => division = 2.
     *
     * Durations in //note elements are multiples of these divisions.
     * @return division
     */
    private BigDecimal findDivisions(){
        Nodes durationNodes = this.mei.getMusic().query("//*[@dur]");
        double smallestDur = 0.0;
        for(Node d : durationNodes){
            Element e = (Element) d;
            String dots = e.getAttributeValue("dots");
            int dotInt = 0;
            if(dots != null && !dots.isEmpty()){
                dotInt = Integer.parseInt(dots);
            }

            double dur = 0.0;
            String durString = e.getAttributeValue("dur");
            if(durString != null && !durString.isEmpty()){
                if(durString.matches(".*\\d.*")){
                    dur = Double.parseDouble(durString);
                }else{
                    dur = Helper.duration2decimal(durString);
                    if (dur == 2.0) {
                        dur = 0.5;
                    } else if (dur == 4.0) {
                        dur = 0.25;
                    } else if (dur == 8.0) {
                        dur = 0.125;
                    }
                }
            }
            if(dotInt > 0.0){ // Dots may modify the length of the duration. 1 dot => add half of duration, 2 dots => add half of duration and add quarter of duration
                dur = (dur/2.0)*(2^(dotInt+2)); // only the largest number is interesting here
            }

            if(dur > smallestDur) smallestDur = dur;
        }
        return BigDecimal.valueOf(smallestDur/4);

        //return BigDecimal.valueOf(this.mei.computeMinimalPPQ());
    }



    /**
     * Find corresponding definition to given Element (probably a note, rest, etc... something that is held within a <layer></layer>).
     * Follow all possible nodes up to the root,
     * Check regularly if all attributes are set, to get sure that all closest attributes can be applied and stop the function.
     * @param e
     * @return dType
     */
    private DefinitionType findCorrespondingDefinition(Element e){
        Element targetElement = e;
        DefinitionType dType = new DefinitionType();
        String staffN = "";
        if(targetElement.getLocalName().equals("layer")) { // I am within a layer, so next to the layer there can be a staffDef,
            this.setStaffDefOrLayerDefToDtype(dType, targetElement); // attempt to fill dType first
            targetElement = Helper.getParentElement(targetElement);
        }
        if(dType.isFull()) return dType;
        // next: try staff
        if(targetElement.getLocalName().equals("staff")){
            staffN = targetElement.getAttributeValue("n");
            // if staffN can't be found, use its index + 1 instead
            staffN = staffN == null || staffN.isEmpty() ? "" + (Objects.requireNonNull(Helper.getAllChildElements("staff", Helper.getParentElement(targetElement))).indexOf(targetElement) + 1) : staffN;
            this.setStaffDefOrLayerDefToDtype(dType, targetElement);
            targetElement = Helper.getParentElement(targetElement);
        }

        if(dType.isFull()) return dType;
        // next: try measure
        if(targetElement.getLocalName().equals("measure")){ // can there be a case where a scoreDef appears after <measure> Element?
            List<Element> scoreDefs = Helper.getAllPreviousSiblingElements("scoreDef", targetElement); // go through all previous scoreDefs (maybe multiple per section)
            Element staff = Helper.getPreviousSiblingElement("staff", targetElement);
            Element prevMeasure = Helper.getPreviousSiblingElement("measure", targetElement);
            List<Element> staffDefCandidates = Helper.getAllPreviousSiblingElements("staffDef", targetElement);
            Element staffDefCandidate = null;
            for(Element sdc : staffDefCandidates){ // staffDefCandidate must be last staffDef for given staff
                String sdcN = sdc.getAttributeValue("n");
                if(sdcN != null && staffN.equals(sdcN)){
                    staffDefCandidate = sdc;
                    break; //
                }
            }
            Element measureParent = Helper.getParentElement(targetElement);
            //Get Staffdef information, if staffDef is just before targetElement (= after previous measure).
            // Calling "getPreviousSiblingElement(ofThis)" does not work, since there are arbitrary text nodes in the tree.
            if(prevMeasure != null && staffDefCandidate != null
                    //&& measureParent.indexOf(staffDefCandidate) > measureParent.indexOf(prevMeasure)
            ){
                this.setStaffDefOrLayerDefToDtype(dType, staffDefCandidate);
            }

            if(staffN != null && !staffN.isEmpty()){
                //find staffGrp with fitting staffDef
                for(Element scoreDef : scoreDefs){
                    Element staffDef = this.findStaffDefInScoreDef(staffN, scoreDef);
                    if(staffDef != null) {
                        this.setStaffDefOrLayerDefToDtype(dType, staffDef);
                    }
                    this.setStaffDefOrLayerDefToDtype(dType, scoreDef);
                }


            }else if(staff != null) { // there can also be a staff in the section!
                Element staffDef = Helper.getFirstChildElement("staffDef", staff);
                if(staffDef != null){
                    this.setStaffDefOrLayerDefToDtype(dType, staffDef);
                }else {
                    this.setStaffDefOrLayerDefToDtype(dType, staff);
                }
            }
            targetElement = Helper.getParentElement(targetElement);
        }

        if(dType.isFull()) return dType;
        // next: try section (also loop through nested upwards)
        do {
            if (targetElement.getLocalName().equals("section")) {
                this.setStaffDefOrLayerDefToDtype(dType, targetElement);
                List<Element> scoreDefs = Helper.getAllPreviousSiblingElements("scoreDef", targetElement);
                Element prevSection = Helper.getPreviousSiblingElement("section", targetElement);
                if (prevSection != null) {
                    // find staffDefs in previous section that might contribute to the current Definition
                    List<Element> prevMeasures = Helper.getAllChildElements("measure", prevSection);
                    Element prevMeasure = null;
                    if (!prevMeasures.isEmpty()){
                        Collections.reverse(prevMeasures);
                        prevMeasure = prevMeasures.get(0);
                    }// last measure in section is first in reversed list
                    List<Element> staffDefCandidates = Helper.getAllChildElements("staffDef", prevSection);
                    if(!staffDefCandidates.isEmpty()) {
                        Collections.reverse(staffDefCandidates); // last staffDefs should be on top (closer to the queried measure)
                        Element staffDefCandidate = null;
                        for (Element sdc : staffDefCandidates) { // staffDefCandidate must be last staffDef for given staff
                            String sdcN = sdc.getAttributeValue("n");
                            if (sdcN != null && staffN.equals(sdcN)) {
                                staffDefCandidate = sdc;
                                break; //
                            }
                        }

                        //Get Staffdef information, if staffDef is just before targetElement (= after previous measure).
                        // Calling "getPreviousSiblingElement(ofThis)" does not work, since there can be arbitrary text nodes in the tree.
                        if (prevMeasure != null && staffDefCandidate != null) {
                            this.setStaffDefOrLayerDefToDtype(dType, staffDefCandidate);
                        }
                    }
                }

                if (staffN != null && !staffN.isEmpty()) {
                    //find staffGrp with fitting staffDef
                    for (Element scoreDef : scoreDefs) {
                        Element staffDef = this.findStaffDefInScoreDef(staffN, scoreDef);
                        if (staffDef != null) {
                            this.setStaffDefOrLayerDefToDtype(dType, staffDef);
                        }
                        this.setStaffDefOrLayerDefToDtype(dType, scoreDef);
                    }
                }
            }
            targetElement = Helper.getClosest("section", targetElement);
        }while(targetElement != null);

        return dType;
    }

    /**
     * Finding a StaffDef in a ScoreDef is used on multiple occasions. Separate method is declared here.
     * StaffDef is identified through n Attribute of staff.
     * StaffDefs are expected in staffGrp.
     * @param staffN n Attribute of staff
     * @param scoreDef
     * @return staffDef
     */
    private Element findStaffDefInScoreDef(String staffN, Element scoreDef){
        Element staffGrp = Helper.getFirstChildElement("staffGrp", scoreDef);
        Element staffDef = null;
        List<Element> staffDefs = this.getStaffDefsInStaffGrp(staffGrp); //staffDefs will be found recursively in method, will return flat list ofs staffDefs
        for(Element sd : staffDefs) { // iterate through all staffDefs in staffGrp
            String nVal = sd.getAttributeValue("n");
            nVal = nVal == null || nVal.isEmpty() ? "" + (staffDefs.indexOf(sd) + 1) : nVal; // if there is no n Attribute, get index + 1 as substitute.
            if (nVal.equals(staffN)) {
                staffDef = sd;
                break;
            }

        }
        return staffDef;
    }

    /**
     * Find all staffDefs recursively to make flat List of staffDefs in nested staffGrp.
     * @param staffGrp
     * @return staffDefs
     */
    private List<Element> getStaffDefsInStaffGrp(Element staffGrp){
        List<Element> nestedStaffGrp = Helper.getAllChildElements("staffGrp", staffGrp);
        List<Element> staffDefs = new ArrayList<>();
        if(nestedStaffGrp != null) {
            for (Element nsg : nestedStaffGrp) {
                staffDefs.addAll(this.getStaffDefsInStaffGrp(nsg));
            }
        }
        List<Element> localStaffDefs = Helper.getAllChildElements("staffDef", staffGrp);
        if(localStaffDefs != null) staffDefs.addAll(localStaffDefs);
        return staffDefs;
    }

    /**
     * Fill DefinitionType with scoreDef, staffDef or layerDef information (layerDef info has priority)
     * @param dType
     * @param targetElement
     * @return
     */
    private void setStaffDefOrLayerDefToDtype(DefinitionType dType, Element targetElement) {
        Element staffDef = targetElement;
        if (!targetElement.getLocalName().equals("staffDef")){
           staffDef = Helper.getPreviousSiblingElement("staffDef", targetElement); // staffDef can be one of the previous siblings or the targetElement itself
        }
        if(staffDef != null) {
            List<String> allowedChildren = new ArrayList<>(Arrays.asList("clef", "meterSig", "keySig", "layerDef"));
            List<Element> staffDefChildren = Helper.getAllChildElements(staffDef); // within this staffDef can be a layerDef
            for (Element ch : staffDefChildren) { // overwrite dType attributed from with layerDef info, if there is any (has higher priority)
                if(!allowedChildren.contains(ch.getLocalName())) continue;
                try {
                    if (ch.getLocalName().equals("layerDef") && ch.getAttributeValue("n").equals(targetElement.getAttributeValue("n"))) { // this layerDef can only be the one with the same n as my current layer
                        dType.setAttributes(ch);
                    }else{
                        dType.setAttributes(ch);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            dType.setAttributes(staffDef);
        }

        if(targetElement.getLocalName().equals("scoreDef")){
            dType.setAttributes(targetElement);
        }
    }

    /**
     * Find duration
     * For mRest: based on unit and count
     * For all other: based on @dur and @dots
     * @param e
     * @return
     */
    private BigDecimal getDuration(Element e){
        if(e.getLocalName().equals("mRest")){
            String meterUnit = this.currentDefinition.getMeterUnit();
            String meterCount = this.currentDefinition.getMeterCount();
            if(meterUnit != null && meterCount != null){
                return this.computeMeasureDuration(meterCount, meterUnit);
            }
            return this.computeMeasureDuration("4", "4"); // just return anything valid, if the meter is not known
        }
        String durVal = e.getAttributeValue("dur");
        String dotsVal = e.getAttributeValue("dots");

        double duration = 0.0;
        if(durVal == null || durVal.isEmpty()) return BigDecimal.ZERO;
        if(durVal.matches(".*\\d.*")){
            duration = Double.parseDouble(durVal);
        }else{
            duration = Helper.duration2decimal(durVal);
            if (duration == 2.0) {
                duration = 0.5;
            } else if (duration == 4.0) {
                duration = 0.25;
            } else if (duration == 8.0) {
                duration = 0.125;
            }
        }

        ArrayList<Float> dots = new ArrayList<>(); // all dots are processed separately before added to durRatio
        if(dotsVal != null && !dotsVal.isEmpty()){
            for(int i = 0; i < Integer.parseInt(dotsVal); i++){
                float f = (float) i;
                dots.add((float) ((duration / 2.0) * Math.pow(2.0F, f + 2.0F))); // each dot divide the duration by 2^(index of dot).

            }
        }

        float baseRatio = (float) ((1.0 / 4.0) * (1.0 /  (float)this.divisions));
        float durRatio = (float) (1.0/duration);
        for(Float f : dots){
            durRatio += ((float) 1.0 /f);
        }
        float result = durRatio / baseRatio;
        int dur = 0;
        //try { // check, if result is plausible, since duration has always to be integer
            dur = (int) result;
//        }catch (Exception ex){
//            System.err.println("Float could not be converted to int: " + durRatio  + "/" + baseRatio + "=" + result);
//            ex.printStackTrace();
//        }

        return BigDecimal.valueOf(dur);
    }

    /**
     * Compute the duration of a whole measure (in MusicXML) from given meterCount and meterUnit.
     * Is mainly used for defining mRest duration.
     * @param meterCount
     * @param meterUnit
     * @return
     */
    public BigDecimal computeMeasureDuration(String meterCount, String meterUnit){
        if(meterUnit != null && !meterUnit.isEmpty() && meterCount != null && !meterCount.isEmpty()){
            float u = Float.parseFloat(meterUnit);
            float c = Float.parseFloat(meterCount);
            return BigDecimal.valueOf((c / u * 4.0F) * this.divisions);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Class to hold Information from definitions for a corresponding part.
     * Fields are immutable, since in the parent class the definitions are built from the layer up and so yields the most specific definition information according to the starting element.
     * This definition fields can be applied to Attributes.
     */
    private class DefinitionType {
        private String clefDis;
        private String clefDisPlace;
        private String clefLine;
        private String clefShape;
        private String keySig;
        private String octDefault;
        private String ppq;
        private String durDefault;
        private String label;
        private String meterSym;
        private String meterCount;
        private String meterUnit;
        private String n;
        private String numDefault;
        private String numBaseDefault;

        private boolean overwrite = false; // a flag if some values have to be overwritten

        private DefinitionType diff;

        /**
         * Compute ratio duration from numbase.default aund num.default
         * @return
         */
        public String getNumRatio(){
            if(this.numDefault != null && !this.numDefault.isEmpty() && this.numBaseDefault != null && !this.numBaseDefault.isEmpty()){
                float num = Float.parseFloat(this.numDefault);
                float numBase = Float.parseFloat(this.numBaseDefault);
                float result = num / numBase;
                int resultInt;
                try{
                   resultInt = Integer.parseInt(this.numBaseDefault);
                }catch (Exception e){
                    System.err.println("Float could not be converted to int: " + result);
                    return null;
                }
                return Integer.toString(resultInt);
            }
            return null;
        }

        public boolean isNullEmpty(String s){
            if(this.overwrite) return true;
            return s == null || s.isEmpty();
        }

        public String getClefDis() {
            return clefDis;
        }

        public void setClefDis(String clefDis) {
            if(this.isNullEmpty(clefDis)) return;
            if(!this.isNullEmpty(this.clefDis)) return;
            this.clefDis = clefDis;
        }

        public String getClefDisPlace() {
            return clefDisPlace;
        }

        public void setClefDisPlace(String clefDisPlace) {
            if(this.isNullEmpty(clefDisPlace)) return;
            if(!this.isNullEmpty(this.clefDisPlace)) return;
            this.clefDisPlace = clefDisPlace;
        }

        public String getClefLine() {
            return clefLine;
        }

        public void setClefLine(String clefLine) {
            if(this.isNullEmpty(clefLine)) return;
            if(!this.isNullEmpty(this.clefLine)) return;
            this.clefLine = clefLine;
        }

        public String getClefShape() {
            return clefShape;
        }

        public void setClefShape(String clefShape) {
            if(this.isNullEmpty(clefShape)) return;
            if(!this.isNullEmpty(this.clefShape)) return;
            this.clefShape = clefShape;
        }

        public String getKeySig() {
            return keySig;
        }

        public void setKeySig(String keySig) {
            if(this.isNullEmpty(keySig)) return;
            if(!this.isNullEmpty(this.keySig)) return;
            this.keySig = keySig;
        }

        public String getOctDefault() {
            return octDefault;
        }

        public void setOctDefault(String octDefault) {
            if(this.isNullEmpty(octDefault)) return;
            if(!this.isNullEmpty(this.octDefault)) return;
            this.octDefault = octDefault;
        }

        public String getPpq() {
            return ppq;
        }

        public void setPpq(String ppq) {
            if(this.isNullEmpty(ppq)) return;
            if(!this.isNullEmpty(this.ppq)) return;
            this.ppq = ppq;
        }

        public String getDurDefault() {
            return durDefault;
        }

        public void setDurDefault(String durDefault) {
            if(this.isNullEmpty(durDefault)) return;
            if(!this.isNullEmpty(this.durDefault)) return;
            this.durDefault = durDefault;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            if(this.isNullEmpty(label)) return;
            if(!this.isNullEmpty(this.label)) return;
            this.label = label;
        }

        public String getMeterSym() {return this.meterSym;}

        public void setMeterSym(String meterSym) {
            if(this.isNullEmpty(meterSym)) return;
            if(!this.isNullEmpty(this.meterSym)) return;
            this.meterSym = meterSym;
        }

        public String getMeterCount() {
            return meterCount;
        }

        public void setMeterCount(String meterCount) {
            if(this.isNullEmpty(meterCount)) return;
            if(!this.isNullEmpty(this.meterCount)) return;
            this.meterCount = meterCount;
        }

        public String getMeterUnit() {
            return meterUnit;
        }

        public void setMeterUnit(String meterUnit) {
            if(this.isNullEmpty(meterUnit)) return;
            if(!this.isNullEmpty(this.meterUnit)) return;
            this.meterUnit = meterUnit;
        }

        public String getN() {
            return n;
        }

        public void setN(String n) {
            if(this.isNullEmpty(n)) return;
            if(!this.isNullEmpty(this.n)) return;
            this.n = n;
        }

        public String getNumDefault() {
            return numDefault;
        }

        public void setNumDefault(String numDefault) {
            if(this.isNullEmpty(numDefault)) return;
            if(!this.isNullEmpty(this.numDefault)) return;
            this.numDefault = numDefault;
        }

        public String getNumBaseDefault() {
            return numBaseDefault;
        }

        public void setNumBaseDefault(String numBaseDefault) {
            if(this.isNullEmpty(numBaseDefault)) return;
            if(!this.isNullEmpty(this.numBaseDefault)) return;
            this.numBaseDefault = numBaseDefault;
        }

        public DefinitionType getDiff(){
            return this.diff;
        }

        /**
         * Set all attributes from a given element.
         * May be scoreDef or children of scoreDef such as clef, meterSig and keySig
         * Each attribute can only be set once.
         * @param e
         */
        public void setAttributes(Element e){
            this.setClefLine(e.getAttributeValue("line")); // will apply for clef Element
            this.setClefShape(e.getAttributeValue("shape")); // will apply for clef Element
            this.setClefDis(e.getAttributeValue("dis")); // will apply for clef Element
            this.setClefDisPlace(e.getAttributeValue("dis.place")); // will apply for clef Element
            this.setClefLine(e.getAttributeValue("clef.line")); // will apply for scoreDef Element
            this.setClefShape(e.getAttributeValue("clef.shape")); // will apply for scoreDef Element
            this.setClefDis(e.getAttributeValue("clef.dis")); // will apply for scoreDef Element
            this.setClefDisPlace(e.getAttributeValue("clef.dis.place")); // will apply for scoreDef Element
            this.setKeySig(e.getAttributeValue("keysig")); // will apply for scoreDef Element
            this.setKeySig(e.getAttributeValue("key.sig")); // will apply for scoreDef Element (process spelling in MEI versions < 5.0)
            this.setKeySig(e.getAttributeValue("sig")); // will apply for keySig Element
            this.setOctDefault(e.getAttributeValue("oct.default")); // will apply for scoreDef Element
            this.setPpq(e.getAttributeValue("ppq")); // will apply for scoreDef Element
            this.setDurDefault(e.getAttributeValue("dur.default")); // will apply for scoreDef Element
            this.setLabel(e.getAttributeValue("label")); // will apply for scoreDef Element
            this.setMeterSym(e.getAttributeValue("sym")); // will apply for meterSig Element
            this.setMeterCount(e.getAttributeValue("count")); // will apply for meterSig Element
            this.setMeterUnit(e.getAttributeValue("unit")); // will apply for meterSig Element
            this.setMeterSym(e.getAttributeValue("meter.sym")); // will apply for scoreDef Element
            this.setMeterCount(e.getAttributeValue("meter.count")); // will apply for scoreDef Element
            this.setMeterUnit(e.getAttributeValue("meter.unit")); // will apply for scoreDef Element
            this.setN(e.getAttributeValue("n")); // will apply for scoreDef Element
            this.setNumDefault(e.getAttributeValue("num.default")); // will apply for scoreDef Element
            this.setNumBaseDefault(e.getAttributeValue("numbase.default")); // will apply for scoreDef Element
        }

        /**
         * Set Attributes for give element, but allow overwriting values.
         * @param e
         * @param overwrite
         */
        public void setAttributes(Element e, boolean overwrite){
            this.overwrite = overwrite;
            this.setAttributes(e);
        }

        /**
         * Check if all fields have some kind of value in them (not null)
         * @return
         */
        public boolean isFull() {
            boolean b = false;
           for(Field field : this.getClass().getDeclaredFields()){
               try {
                   field.setAccessible(true);
                   Object val = field.get(this);
                   if(val == null) {
                       b = false;
                       break;
                   }
                   if(((String)val).isEmpty()){
                       b = false;
                       break;
                   }else{
                       b = true;
                   }
               }catch (IllegalAccessException e){
                   b = false;
                   break;
               }
           }

           return b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clefDis, clefDisPlace, clefLine, clefShape, keySig, octDefault, ppq, durDefault, label, meterCount, meterUnit, n, numDefault, numBaseDefault);
        }


        /**
         * Compare if each String field has the same conentent.
         * @param o
         * @return
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DefinitionType that = (DefinitionType) o;
            boolean b = true;

            if (this.clefDis != null && that.clefDis != null) { // both are string, therefore test equality
                b = this.clefDis.equals(that.clefDis);
            } else if (this.clefDis != null || that.clefDis != null) { // otherwise one is null, so they are not equal.
                return false;
            } // no further checks (both are null and therefore equal), since b is still true until now
            if(!b) return false; // return false, since at least this one member is false, so both instances can't be equal anymore.

            if (this.clefDisPlace != null && that.clefDisPlace != null) {
                b = this.clefDisPlace.equals(that.clefDisPlace);
            } else if (this.clefDisPlace != null || that.clefDisPlace != null) {
                return false;
            }
            if(!b) return false;

            if (this.clefLine != null && that.clefLine != null) {
                b = this.clefLine.equals(that.clefLine);
            } else if (this.clefLine != null || that.clefLine != null) {
                return false;
            }
            if(!b) return false;

            if (this.clefShape != null && that.clefShape != null) {
                b = this.clefShape.equals(that.clefShape);
            } else if (this.clefShape != null || that.clefShape != null) {
                return false;
            }
            if(!b) return false;

            if (this.keySig != null && that.keySig != null) {
                b = this.keySig.equals(that.keySig);
            } else if (this.keySig != null || that.keySig != null) {
                return false;
            }
            if(!b) return false;

            if (this.octDefault != null && that.octDefault != null) {
                b = this.octDefault.equals(that.octDefault);
            } else if (this.octDefault != null || that.octDefault != null) {
                return false;
            }
            if(!b) return false;

            if (this.ppq != null && that.ppq != null) {
                b = this.ppq.equals(that.ppq);
            } else if (this.ppq != null || that.ppq != null) {
                return false;
            }
            if(!b) return false;

            if (this.durDefault != null && that.durDefault != null) {
                b = this.durDefault.equals(that.durDefault);
            } else if (this.durDefault != null || that.durDefault != null) {
                return false;
            }
            if(!b) return false;

            if (this.label != null && that.label != null) {
                b = this.label.equals(that.label);
            } else if (this.label != null || that.label != null) {
                return false;
            }
            if(!b) return false;

            if (this.meterSym != null && that.meterSym != null) {
                b = this.meterSym.equals(that.meterSym);
            } else if (this.meterSym != null || that.meterSym != null) {
                return false;
            }
            if(!b) return false;

            if (this.meterCount != null && that.meterCount != null) {
                b = this.meterCount.equals(that.meterCount);
            } else if (this.meterCount != null || that.meterCount != null) {
                return false;
            }
            if(!b) return false;

            if (this.meterUnit != null && that.meterUnit != null) {
                b = this.meterUnit.equals(that.meterUnit);
            } else if (this.meterUnit != null || that.meterUnit != null) {
                return false;
            }
            if(!b) return false;

            if (this.n != null && that.n != null) {
                b = this.n.equals(that.n);
            } else if (this.n != null || that.n != null) {
                return false;
            }
            if(!b) return false;

            if (this.numDefault != null && that.numDefault != null) {
                b = this.numDefault.equals(that.numDefault);
            } else if (this.numDefault != null || that.numDefault != null) {
                return false;
            }
            if(!b) return false;

            if (this.numBaseDefault != null && that.numBaseDefault != null) {
                b = this.numBaseDefault.equals(that.numBaseDefault);
            } else if (this.numBaseDefault != null || that.numBaseDefault != null) {
                return false;
            }
            if(!b) return false;

            return b;
        }

        /**
         * Check if B is fully contained in A.
         * True if elements of B are equal to Elements of A, or null.
         * @param ofThis
         * @return
         */
        public boolean hasSubset(DefinitionType ofThis) {
            if (ofThis == null) return false;
            if (this == ofThis) return true;

            this.diff = new DefinitionType();
            boolean b = true;

            b = this.checkAndSetDifference(this.clefDis, ofThis.clefDis, b, this.diff::setClefDis);
            b = this.checkAndSetDifference(this.clefDisPlace, ofThis.clefDisPlace, b, this.diff::setClefDisPlace);
            b = this.checkAndSetDifference(this.clefLine, ofThis.clefLine, b, this.diff::setClefLine);
            b = this.checkAndSetDifference(this.clefShape, ofThis.clefShape, b, this.diff::setClefShape);
            b = this.checkAndSetDifference(this.keySig, ofThis.keySig, b, this.diff::setKeySig);
            b = this.checkAndSetDifference(this.octDefault, ofThis.octDefault, b, this.diff::setOctDefault);
            b = this.checkAndSetDifference(this.ppq, ofThis.ppq, b, this.diff::setPpq);
            b = this.checkAndSetDifference(this.durDefault, ofThis.durDefault, b, this.diff::setDurDefault);
            b = this.checkAndSetDifference(this.label, ofThis.label, b, this.diff::setLabel);
            b = this.checkAndSetDifference(this.meterSym, ofThis.meterSym, b, this.diff::setMeterSym);
            b = this.checkAndSetDifference(this.meterCount, ofThis.meterCount, b, this.diff::setMeterCount);
            b = this.checkAndSetDifference(this.meterUnit, ofThis.meterUnit, b, this.diff::setMeterUnit);
            b = this.checkAndSetDifference(this.n, ofThis.n, b, this.diff::setN);
            b = this.checkAndSetDifference(this.numDefault, ofThis.numDefault, b, this.diff::setNumDefault);
            b = this.checkAndSetDifference(this.numBaseDefault, ofThis.numBaseDefault, b, this.diff::setNumBaseDefault);

            return b;
        }

        /**
         * Helper to apply functions and variables to reflections.
         * @param thisField
         * @param otherField
         * @param currentStatus
         * @param setter
         * @return
         */
        private boolean checkAndSetDifference(Object thisField, String otherField, boolean currentStatus, Consumer<String> setter) {
//            if(currentStatus) {
//                if (thisField != null && otherField != null) {
//                    currentStatus = thisField.equals(otherField);
//                } else if (thisField == null && otherField != null) {
//                    currentStatus = false;
//                }
//                if (!currentStatus) {
//                    setter.accept(otherField);
//                }
//            }
            boolean localStatus = currentStatus;
            if (thisField != null && otherField != null) { // this.diff is only filled when the local status is "false" meaning that a difference in this and ofthis is found
                localStatus = thisField.equals(otherField);
                if(!localStatus){
                    setter.accept(otherField);
                }
            } else if (thisField == null && otherField != null) {
                localStatus = false;
                setter.accept(otherField);
            }
            if(currentStatus){ // the status to be retuned by "hasSubset" will only change once, when one difference is found
                currentStatus = localStatus;
            }
            return currentStatus;
        }


        /**
         * Check if dType is contained by this. If yes, then return diff, else return null.
         * @param dType
         * @return
         */
        public DefinitionType getDiffRight(DefinitionType dType){
            boolean b = this.hasSubset(dType);
            if(!b) return this.diff;
            return null;
        }
    }
}


