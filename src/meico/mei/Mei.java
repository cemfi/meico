package meico.mei;

import meico.mpm.Mpm;
import meico.msm.Msm;
import meico.musicxml.MusicXml;
import meico.supplementary.KeyValue;
import meico.svg.SvgCollection;
import nu.xom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * This class holds the mei data from a source file in a XOM Document.
 * @author Axel Berndt.
 */

public class Mei extends meico.xml.XmlBase {
    /**
     * a default constructor that creates an empty Mei instance
     */
    public Mei() throws ParsingException, IOException {
        super(Mei.class.getResourceAsStream("/resources/minimal.mei"));
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
    public Mei(String xml) throws IOException, ParsingException, ParserConfigurationException, SAXException {
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
    public Mei(String xml, boolean validate, URL schema) throws IOException, ParsingException, ParserConfigurationException, SAXException {
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

        try {                                               // try to read the title from mei/meiHead/fileDesc/titleStmt/title
            title = Helper.getFirstChildElement("fileDesc", this.getMeiHead());
            title = Helper.getFirstChildElement("titleStmt", title);
            title = Helper.getFirstChildElement("title", title);
        } catch (NullPointerException ex1) {                // if that does not exist
            try {                                           // try to get the title from MEI 3.0 mei/meiHead/workDesc/work/titleStmt/title
                title = Helper.getFirstChildElement("workDesc", this.getMeiHead());
                title = Helper.getFirstChildElement("work", title);
                title = Helper.getFirstChildElement("titleStmt", title);
                title = Helper.getFirstChildElement("title", title);
            } catch (NullPointerException ex2) {            // if that does not exist
                try {                                       // try to get the title from MEI 4.0+ mei/meiHead/workList/work/title
                    title = Helper.getFirstChildElement("workList", this.getMeiHead());
                    title = Helper.getFirstChildElement("work", title);
                    title = Helper.getFirstChildElement("title", title);
                } catch (NullPointerException ex3) {
                    return (this.getFile() == null) ? "" : Helper.getFilenameWithoutExtension(this.getFile().getName());    // return the filename without extension or (if that does not exist either) return empty string
                }
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
     * Retrieve all mdiv elements from this MEI's music environment.
     * In case of nested mdivs, only the leaf mdivs are returned.
     * @return a list of mdiv elements; can be empty
     */
    public ArrayList<Element> getAllMdivs() {
        ArrayList<Element> result = new ArrayList<>();

        Element music = this.getMusic();
        if (music != null)
            result.addAll(this.getAllMdivs(music));

        return result;
    }

    /**
     * Recursively retrieve mdiv elements.
     * In case of nested mdivs, only the leaf mdivs are returned.
     * @param inThis search them in this subtree
     * @return
     */
    private ArrayList<Element> getAllMdivs(Element inThis) {
        ArrayList<Element> result = new ArrayList<>();

        for (Element e : inThis.getChildElements()) {
            switch (e.getLocalName()) {
                case "body":
                case "group":
                    result.addAll(this.getAllMdivs(e));
                    break;
                case "mdiv":
                    ArrayList<Element> subMdivs = this.getAllMdivs(e);  // check for nested mdivs
                    if (subMdivs.isEmpty())                             // if no nested mdivs in this mdiv
                        result.add(e);                                  // add this mdiv to the results
                    else                                                // otherwise
                        result.addAll(subMdivs);                        // add the leaf mdivs found in this subtree
                    break;
//                default:
//                    continue;
            }
        }

        return result;
    }

    /**
     * Retrieve all cariant encodings, i.e. all app and choice elements, in this MEI.
     * @return a list of Node objects that can be cast to Element
     */
    public Nodes getAllVariantEncodings() {
        return Mei.getAllVariantEncodings(this.getRootElement());
    }

    /**
     * Retrieve all variant encodings, i.e. all app and choice elements,
     * in the subtree of the specified element.
     * @param inThis
     * @return a list of Node objects that can be cast to Element
     */
    public static Nodes getAllVariantEncodings(Element inThis) {
        Nodes e = inThis.query("descendant::*[(local-name()='choice' or local-name()='app')]");
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
     * @param useOnlineVerovio
     * @param oneLineScore
     * @return a collection of SVGs; it can be empty if the conversion fails
     */
    public SvgCollection exportSvg(boolean useOnlineVerovio, boolean oneLineScore) {
        long startTime = System.currentTimeMillis();                            // we measure the time that the conversion consumes
        System.out.println("\nConverting " + ((this.file != null) ? this.file.getName() : "MEI data") + " to SVG.");

        SvgCollection svgs = new SvgCollection();                               // create an svg collection
        svgs.setTitle(this.getTitle());                                         // set the title of the svg collection to the title of this mei

        // this code block is just a debug output for development
//        List<ScriptEngineFactory> engines = (new ScriptEngineManager()).getEngineFactories();
//        for (ScriptEngineFactory f: engines) {
//            System.out.println(f.getLanguageName()+" "+f.getEngineName()+" "+f.getNames().toString()+" "+f.getLanguageVersion());
//        }

        // non-functional code
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("JavaScript");
//
//        String verovio = (useOnlineVerovio) ? VerovioProvider.getVerovio(this) : VerovioProvider.getLocalVerovio(this); // get the Verovio Toolkit script
//        if (verovio == null) {                                                  // if this fails
//            System.err.println("MEI to SVG conversion failed: Verovio Toolkit not available. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");
//            return svgs;                                                        // return empty svgs collection
//        }
//
//        engine.put("mei", this.toXML());
//        try {
//            engine.eval(verovio);   // TODO: this fails because Verovio requires a browser environment
//            // TODO: do the meaningful stuff ...
//        } catch (ScriptException e) {
//            System.err.println("MEI to SVG conversion failed: script evaluation failed. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");
//            return svgs;
//        }

        System.out.println("MEI to SVG conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return svgs;
    }

    /**
     * converts the mei data into msm format and returns a list of Msm instances, one per movement/mdiv; the thime resolution (pulses per quarter note) is 720 by default or more if required (for very short note durations)
     * @return the list of msm documents (movements) created
     */
    public List<Msm> exportMsm() {
        return this.exportMsm(720);                                             // do the conversion with a default value of pulses per quarter
    }

    /**
     * converts the mei data into msm format and returns a list of Msm instances, one per movement/mdiv, ppq (pulses per quarter) sets the time resolution
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @return the list of msm documents (movements) created
     */
    public List<Msm> exportMsm(int ppq) {
        return this.exportMsm(ppq, true, false, true);
    }

    /**
     * converts the mei data into msm format and returns a list of Msm instances, one per movement/mdiv, ppq (pulses per quarter) sets the time resolution
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
     * @param dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already done here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @return the list of msm documents (movements) created
     */
    public KeyValue<List<Msm>, List<Mpm>> exportMsmMpm(int ppq, boolean dontUseChannel10) {
        return this.exportMsmMpm(ppq, dontUseChannel10, false, true);
    }

    /**
     * converts the mei data into msm and mpm format and returns a tuplet of lists, one with the msms (one per movement/mdiv), the other with the corresponding mpms
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @param dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already done here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @param ignoreExpansions set this true to have a 1:1 conversion of MEI to MSM without the rearrangement that MEI's expansion elements produce
     * @return the list of msm documents (movements) created
     */
    public KeyValue<List<Msm>, List<Mpm>> exportMsmMpm(int ppq, boolean dontUseChannel10, boolean ignoreExpansions) {
        return this.exportMsmMpm(ppq, dontUseChannel10, ignoreExpansions, true);
    }

    /**
     * converts the mei data into msm and mpm format and returns a tuplet of lists, one with the msms (one per movement/mdiv), the other with the corresponding mpms
     * @param ppq the ppq resolution for the conversion; this is counterchecked with the minimal required resolution to capture the shortest duration in the mei data; if a higher resolution is necessary, this input parameter is overridden
     * @param dontUseChannel10 the flag says whether channel 10 (midi drum channel) shall be used or not; it is already done here, at the mei2msm conversion, because the msm should align with the midi file later on
     * @param ignoreExpansions set this true to have a 1:1 conversion of MEI to MSM without the rearrangement that MEI's expansion elements produce
     * @param cleanup set true to return a clean msm file or false to keep all the crap from the conversion
     * @return the list of msm documents (movements) created
     */
    public synchronized KeyValue<List<Msm>, List<Mpm>> exportMsmMpm(int ppq, boolean dontUseChannel10, boolean ignoreExpansions, boolean cleanup) {
        return (new Mei2MsmMpmConverter(ppq, dontUseChannel10, ignoreExpansions, cleanup)).convert(this);
    }

    /**
     * converts the MEI data to MusicXML and returns a list of MusicXml objects, one per movement/mdiv;
     * MEI expansions are resolved
     * @return
     */
    public synchronized List<MusicXml> exportMusicXml() {
        return this.exportMusicXml(false);
    }

    /**
     * converts the MEI data to MusicXML and returns a list of MusicXml objects, one per movement/mdiv
     * @param ignoreExpansions set this true to have a 1:1 conversion of MEI to MusicXML without the rearrangement that MEI's expansion elements produce
     * @return
     */
    public synchronized List<MusicXml> exportMusicXml(boolean ignoreExpansions) {
        return (new Mei2MusicXmlConverter(ignoreExpansions)).convert(this);
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

        ArrayList<String> notResolved = new ArrayList<>();                                              // store those ids that are not resolved
        HashMap<Element, String> previousPlaceholders = new HashMap<>();                                // this is a copy of the placeholders hashmap in the while loop; if it does not change from one iteration to the next, there is a placeholder referring to another placeholder referring back to the first; this cannot be resolved and leads to an infinite loop; this hashmap here is to detect this situation
        ArrayList<KeyValue<Element, HashMap<String, String>>> idReplacementsList = new ArrayList<>();   // this holds a list of key-value pairs (key=root of the copy subtree, (key=originalId, value=newId)) for later reference; one such list entry corresponds to one resolved copyof/sameas
        HashMap<Element, Element> isInSubtreeOf = new HashMap<>();                                      // this is filled with copied elements; for each element it says which subtree it belongs to via the subtree's root note: (key=a copy element, value=root of the copy subtree)

        System.out.print("Resolving copyofs and sameas's:");

        while (true) {                                                                                  // this loop can only be exited if no placeholders are left (it is possible that multiple runs are necessary when placeholders are within placeholders)
            HashMap<String, Element> elements = new HashMap<>();                                        // this hashmap will be filled with elements and their ids
            HashMap<Element, String> placeholders = new HashMap<>();                                    // this hashmap will be filled with placeholder elements that have a copyof attribute and the id in the copyof

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

            if (placeholders.isEmpty()) break;                                                          // we are done, this stops the while loop

            // detect placeholders that cannot be resolved but lead to infinite loops because of circular references
            if ((placeholders.values().containsAll(previousPlaceholders.values()))
                    && previousPlaceholders.values().containsAll(placeholders.values())) {              // if the same copyof references recur
                for (Map.Entry<Element, String> placeholder : placeholders.entrySet()) {
                    notResolved.add(placeholder.getKey().toXML());                                      // add all entries to the return list
                    placeholder.getKey().getParent().removeChild(placeholder.getKey());                 // delete all placeholders from the xml, we cannot resolve them anyway
//                    placeholder.getKey().detach();
                }
                System.err.print(" circular copyof or sameas referencing detected, cannot be resolved,");
                break;                                                                                  // stop the while loop
            }
            previousPlaceholders = placeholders;

            System.out.print(" " + placeholders.size() + " copyofs and sameas's ...");

            // replace all placeholders in the xml tree by copies of the source
            for (Map.Entry<Element, String> placeholder : placeholders.entrySet()) {                    // for each placeholder
                Element found = elements.get(placeholder.getValue());                                   // search the elements hashmap for the id

                if (found == null) {                                                                    // if no element with this id has been found
                    notResolved.add(placeholder.getKey().toXML());                                      // add entry to the return list
                    placeholder.getKey().getParent().removeChild(placeholder.getKey());                 // delete the placeholder from the xml, we cannot process it anyway
//                    placeholder.getKey().detach();
                    continue;                                                                           // continue with the next placeholder
                }

                // make the replacement
                Element copy = found.copy();                                                            // make a deep copy of the source

                // replace the placeholder element (that with the copyof/sameas attribute) by the copy subtree just created
                try {
                    placeholder.getKey().getParent().replaceChild(placeholder.getKey(), copy);          // replace the placeholder by it
//                System.out.println("replacing: " + placeholder.getKey().toElement() + "\nby\n" + copy.toElement() + "\n\n");
                } catch (NoSuchChildException | NullPointerException | IllegalAddException error) {     // if something went wrong, I don't know why as none of these exceptions should occur, just to be sure
                    error.printStackTrace();                                                            // print error
                    notResolved.add(placeholder.getKey().toXML());                                      // add entry to the return list
                    continue;
                }

                // add each element in copy to isInSubtreeOf
                copy.query("descendant-or-self::*").forEach(element -> {                            // for each copy element
                    isInSubtreeOf.put((Element) element, copy);                                         // put it in isInSubtreeOf
                });

                // generate new ids for those elements with a copied id
                HashMap<String, String> idReplacements = new HashMap<>();                                           // for each element with an ID, we replace its ID by a new one and store that mapping here (key=originalID, value=newID)
                Nodes ids = copy.query("descendant-or-self::*[@xml:id]");                                           // get all the nodes with an xml:id attribute
                for (int j = 0; j < ids.size(); ++j) {                                                              // go through all the nodes
                    Attribute idAtt = ((Element) ids.get(j)).getAttribute("id", "http://www.w3.org/XML/1998/namespace");
                    String originalId = idAtt.getValue();                                                           // get the original ID
                    String newId = originalId + "_meico_" + UUID.randomUUID().toString();                           // generate a new ID
                    idAtt.setValue(newId);                                                                          // and write into the attribute
                    idReplacements.put(originalId, newId);                                                          // store the ID replacement info
                }

                // but keep the possibly existing placeholder id for the copy's root node
                Attribute idAtt = placeholder.getKey().getAttribute("id", "http://www.w3.org/XML/1998/namespace");  // get the placeholder's xml:id
                if (idAtt != null) {                                                                                // if the placeholder has one
                    Attribute copyIdAtt = copy.getAttribute("id", "http://www.w3.org/XML/1998/namespace");
//                    idReplacements.put(copyIdAtt.getValue(), id.getValue());                                        // store the ID replacement info ... not needed as we do not touch references to the original of the copy's root note                }
                    copyIdAtt.setValue(idAtt.getValue());                                                           // set the copy's id to the id of the placeholder
                }
                idReplacementsList.add(new KeyValue<>(copy, idReplacements));                                       // add the id replacements hashmap to the list of id replacements hashmaps
            }
        }

        // handle elements that refer to elements just copied
        // QUESTION: Should we exclude the root of the subtree from this?
        // ANSWER: Yes. Imagine n notes, all copies of the 1st. Outside we have a slur starting at the 1st, ending at the nth. That slur should not be copied to each note!
        // TODO: generate ElementsByReferenceIds
        //   for each list entry in idReplacementsList
        //     for each ID that has been replaced
        //       for each element that refers to this ID
        //         if the element is outside the copy subtree (isInSubtreeOf(element) != root of current subtree)
        //           make a deep copy of the element and go on with this (do not touch the original)
        //         update all referencing attributes
        //         put the element on the list of elements with which we are done in this idReplacementList entry

        System.out.println(" done");

        if (!notResolved.isEmpty())
            System.out.println("The following placeholders could not be resolved:\n" + notResolved.toString());

        return notResolved;
    }
//    public synchronized ArrayList<String> resolveCopyofs() {
//        Element e = this.getRootElement();                                                              // this also includes the meiHead section, not only the music section, as there might be reference from music into the head
//        if (e == null) return null;
//
//        System.out.print("Resolving copyofs and sameas's:");
//
//        ArrayList<String> notResolved = new ArrayList<String>();                                             // store those ids that are not resolved
//        HashMap<Element, String> previousPlaceholders = new HashMap<Element, String>();                               // this is a copy of the placeholders hashmap in the while loop; if it does not change from one iteration to the next, there is a placeholder referring to another placeholder referring back to the first; this cannot be resolved and leads to an infinite loop; this hashmap here is to detect this situation
//        HashMap<String, ArrayList<KeyValue<Element, ArrayList<KeyValue<String, Attribute>>>>> elementsWithReferences = new HashMap<>();    // a HashMap of all IDs that an element refers to with the element and all IDs it references (ID, listOf(element, listOf(ID, attribute))))
//
//        // get all elements with references, these have to be processed after creating copies
//        for (Node node : e.query("descendant::*[attribute::*[starts-with(., '#')]]")) {                 // get all elements with attributes that begin with '#' and process each of them
//            if ((((Element) node).getAttribute("copyof") != null) || ((Element) node).getAttribute("sameas") != null)   // keep those elements that get replaced by copies out of the HashMap
//                continue;
//
//            KeyValue<Element, ArrayList<KeyValue<String, Attribute>>> elementAndItsReferences = new KeyValue<>((Element) node, new ArrayList<>());   // this holds the element and its references; if the references list stays empty, it needs no treatment
//
//            for (int a = 0; a < elementAndItsReferences.getKey().getAttributeCount(); ++a) {            // for each attribute that this element has
//                Attribute attribute = elementAndItsReferences.getKey().getAttribute(a);                 // get the attribute
//                String id = attribute.getValue();                                                       // get its value
//                if (!id.startsWith("#"))                                                                // if it is no reference to another element in this document
//                    continue;                                                                           // check next attribute
//                elementAndItsReferences.getValue().add(new KeyValue<>(id.substring(1), attribute));     // add the id to the list of references of this element, but leave the '#' away
//            }
//
////            if (elementAndItsReferences.getValue().isEmpty())                                           // if this element has no references (should not be the case)
////                continue;                                                                               // we are done with it
//
//            for (KeyValue<String, Attribute> reference : elementAndItsReferences.getValue()) {          // for each reference
//                if (!elementsWithReferences.containsKey(reference.getKey())) {                          // if the entry does not exist yet
//                    ArrayList<KeyValue<Element, ArrayList<KeyValue<String, Attribute>>>> list = new ArrayList<>();  // create it
//                    elementsWithReferences.put(reference.getKey(), list);                               // and add it to the HashMap
//                }
//                elementsWithReferences.get(reference.getKey()).add(elementAndItsReferences);            // make an entry in the HashMap
//            }
//        }
//
//        // this loop can only be exited if no placeholders are left (it is possible that multiple runs are necessary when placeholders are within placeholders)
//        while (true) {
//            HashMap<String, Element> elements = new HashMap<String, Element>();                                       // this hashmap will be filled with elements and their ids
//            HashMap<Element, String> placeholders = new HashMap<Element, String>();                                   // this hashmap will be filled with placeholder elements that have a copyof attribute and the id in the copyof
//
//            Nodes all = e.query("descendant::*[attribute::copyof or attribute::sameas or attribute::xml:id]");  // get all elements with a copyof, sameas or xml:id attribute
//            for (int i = 0; i < all.size(); ++i) {                                                      // for each of them
//                Element element = (Element) all.get(i);                                                 // make an Element out of it
//
//                Attribute a = element.getAttribute("copyof");                                           // get the copyof attribute, if there is one
//                if (a == null)                                                                          // no copyof attribute?
//                    a = element.getAttribute("sameas");                                                 // then maybe a sameas
//                if (a != null) {                                                                        // if there is a copyof or sameas attribute
//                    String copyof = a.getValue();                                                       // get its value
//                    if (copyof.charAt(0) == '#') copyof = copyof.substring(1);                          // local references within the document usually start with #; this must be excluded when searching for the id
//                    placeholders.put(element, copyof);                                                  // put that entry on the placeholder hashmap
//                    //continue;                                                                         // this element may also have an xml:id, so it must be added to the other list as well and we later on have the possibility to resolve references of placeholders to other placeholders
//                }
//
//                a = element.getAttribute("id", "http://www.w3.org/XML/1998/namespace");                 // get the element's xml:id
//                if (a != null) {                                                                        // if it has one
//                    elements.put(a.getValue(), element);                                                // put it on the elements hashmap
//                }
//            }
//
//            if (placeholders.isEmpty())
//                break;                                                                                  // we are done, this stops the while loop
//
//            // detect placeholders that cannot be resolved but lead to infinite loops because of circular references
//            if ((placeholders.values().containsAll(previousPlaceholders.values()))
//                    && previousPlaceholders.values().containsAll(placeholders.values())) {              // if the same copyof references recur
//                for (Map.Entry<Element, String> placeholder : placeholders.entrySet()) {
//                    notResolved.add(placeholder.getKey().toXML());                                      // add all entries to the return list
//                    placeholder.getKey().getParent().removeChild(placeholder.getKey());                 // delete all placeholders from the xml, we cannot resolve them anyway
////                    placeholder.getKey().detach();
//                }
//                System.err.print(" circular copyof or sameas referencing detected, cannot be resolved,");
//                break;                                                                                  // stop the while loop
//            }
//            previousPlaceholders = placeholders;
//
//            System.out.print(" " + placeholders.size() + " copyofs and sameas's ...");
//
//            // replace all placeholders in the xml tree by copies of the source
//            for (Map.Entry<Element, String> placeholder : placeholders.entrySet()) {                    // for each placeholder, i.e. element with copyof or sameas
//                Element found = elements.get(placeholder.getValue());                                   // search the elements hashmap for the id
//
//                if (found == null) {                                                                    // if no element with this id has been found
//                    notResolved.add(placeholder.getKey().toXML());                                      // add entry to the return list
//                    placeholder.getKey().getParent().removeChild(placeholder.getKey());                 // delete the placeholder from the xml, we cannot process it anyway
////                    placeholder.getKey().detach();
//                    continue;                                                                           // continue with the next placeholder
//                }
//
//                // make the replacement
//                Element copy = found.copy();                                                               // make a deep copy of the source
//
//                try {
//                    placeholder.getKey().getParent().replaceChild(placeholder.getKey(), copy);          // replace the placeholder by it
////                System.out.println("replacing: " + placeholder.getKey().toElement() + "\nby\n" + copy.toElement() + "\n\n");
//                } catch (NoSuchChildException | NullPointerException | IllegalAddException error) {     // if something went wrong, I don't know why as none of these exceptions should occur, just to be sure
//                    error.printStackTrace();                                                            // print error
//                    notResolved.add(placeholder.getKey().toXML());                                      // add entry to the return list
//                    continue;
//                }
//
//                // generate new ids for those elements with a copied id
//                HashMap<String, String> idReplacements = new HashMap<>();                                                   // this holds key-value pairs (originalId, newId) for later reference
//                Nodes ids = copy.query("descendant-or-self::*[@xml:id]");                                                   // get all the nodes with an xml:id attribute
//                for (int j = 0; j < ids.size(); ++j) {                                                                      // go through all the nodes
//                    Attribute idAtt = ((Element) ids.get(j)).getAttribute("id", "http://www.w3.org/XML/1998/namespace");
//                    String originalId = idAtt.getValue();                                                                   // get the original ID
//                    String newId = originalId + "_meico_" + UUID.randomUUID().toString();                                   // generate a new ID
//                    idAtt.setValue(newId);                                                                                  // and write into the attribute
//                    idReplacements.put(originalId, newId);                                                                  // store the ID replacement info
//                }
//
//                // but keep the possibly existing placeholder id for the copy's root node
//                Attribute id = placeholder.getKey().getAttribute("id", "http://www.w3.org/XML/1998/namespace");             // get the placeholder's xml:id
//                if (id != null) {                                                                                           // if the placeholder has one
//                    copy.getAttribute("id", "http://www.w3.org/XML/1998/namespace").setValue(id.getValue());                // set the copy's id to the id of the placeholder
////                    idReplacements.put(copy.getAttributeValue("id", "http://www.w3.org/XML/1998/namespace"), id.getValue()); // store the ID replacement info ... not needed as we do not touch references to the original of the copy's root note
//                }
//
//                // use idReplacements to process elements that refer to the originalIds
//                // QUESTION: Should we exclude the root of the subtree?
//                // ANSWER: Yes. Imagine 8 notes, all copies of the 1st. Outside we have a slur starting at the 1st, ending at the 8th. That slur should not be copied to each note!
//                // for readability: elementsWithReferences is a HashMap of this structure: (ID, listOf(element, listOf(ID, attribute))))
//                for (String originalId : idReplacements.keySet()) {                                                         // for each ID that got replaced
//                    ArrayList<KeyValue<Element, ArrayList<KeyValue<String, Attribute>>>> elementsAndReferences = elementsWithReferences.get(originalId);
//                    if (elementsAndReferences == null)                                                                      // if there are no elements referencing the ID just checked
//                        continue;                                                                                           // check the next ID
//
//                    for (KeyValue<Element, ArrayList<KeyValue<String, Attribute>>> elementAndReferences : elementsAndReferences) {  // for each element that refers to this ID
//                        if (!Helper.isChildOf(elementAndReferences.getKey(), found)) {                                      // if the element is not part of the subtree just copied
//                            Element elementCopy = elementAndReferences.getKey().copy();                                     // create a deep copy of the element
//
//                            // give it a unique ID
//                            Attribute elementCopyId = elementCopy.getAttribute("id", "http://www.w3.org/XML/1998/namespace");
//                            if (elementCopyId != null) {
//                                String origId = elementCopyId.getValue();                                                   // get the original ID
//                                String newId = origId + "_meico_" + UUID.randomUUID().toString();                           // generate a new ID
//                                elementCopyId.setValue(newId);                                                              // set the new ID
//                            }
//
//                            Helper.getParentElement(elementAndReferences.getKey()).appendChild(elementCopy);                // add it to the original's parent
//                        }
//
//                        // update all references that point inside the subtree with the new IDs, i.e. can be found in idReplacements
//                        boolean hasReferencesOutsideSubtree = false;
//                        for (KeyValue<String, Attribute> referenceAndAttribute : elementAndReferences.getValue()) {         // for each reference
//                            if (idReplacements.containsKey(referenceAndAttribute.getKey())) {                               // if the reference points inside the subtree
//                                referenceAndAttribute.getValue().setValue("#" + idReplacements.get(referenceAndAttribute.getKey()));    // replace the reference by the new ID
//                            } else {                                                                                        // if the referencing element also has references outside the subtree
//                                hasReferencesOutsideSubtree = true;                                                         // we will have to add it to the list of elements with references
//                            }
//                        }
//
//                        // if the referencing element also has references outside the subtree, put the element into the list of elements with references
//                        if (hasReferencesOutsideSubtree) {
//                            // put the element into the list of elements with references
//                            KeyValue<Element, ArrayList<KeyValue<String, Attribute>>> elementAndItsReferences = new KeyValue<>(elementAndReferences.getKey(), new ArrayList<>());   // this holds the element and its references; if the references list stays empty, it needs no treatment
//
//                            for (int a = 0; a < elementAndItsReferences.getKey().getAttributeCount(); ++a) {                // for each attribute that this element has
//                                Attribute attribute = elementAndItsReferences.getKey().getAttribute(a);                     // get the attribute
//                                String refId = attribute.getValue();                                                        // get its value
//                                if (!refId.startsWith("#"))                                                                 // if it is no reference to another element in this document
//                                    continue;                                                                               // check next attribute
//                                elementAndItsReferences.getValue().add(new KeyValue<>(refId.substring(1), attribute));      // add the id to the list of references of this element, but leave the '#' away
//                            }
//
////                            if (elementAndItsReferences.getValue().isEmpty())                                               // if this element has no references (should not be the case)
////                                continue;                                                                                   // we are done with it
//
//                            for (KeyValue<String, Attribute> reference : elementAndItsReferences.getValue()) {              // for each reference
//                                if (!elementsWithReferences.containsKey(reference.getKey())) {                              // if the entry does not exist yet
//                                    ArrayList<KeyValue<Element, ArrayList<KeyValue<String, Attribute>>>> list = new ArrayList<>();  // create it
//                                    elementsWithReferences.put(reference.getKey(), list);                                   // and add it to the HashMap
//                                }
//                                elementsWithReferences.get(reference.getKey()).add(elementAndItsReferences);                // make an entry in the HashMap
//                            }
//
//                        }
//                    }
//                }
//            }
//        }
//
//        System.out.println(" done");
//
//        if (!notResolved.isEmpty())
//            System.out.println("The following placeholders could not be resolved:\n" + notResolved.toString());
//
//        return notResolved;
//    }

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
    protected void removeRendElements() {
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
//            r.detach();
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
            for (int i = expansions.size() - 1; i >= 0; --i) {                              // delete all expansion elements from the regularizedRoot
                regularizedRoot.removeChild(expansions.get(i));
//                expansions.get(i).detach();
            }

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
//                    child.detach();
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
                if (child == null)
                    continue;

                try {
                    regularizedRoot.appendChild(child);                                     // try to append it to regularizedRoot, this will fail if it has already been added
                } catch (MultipleParentException e) {                                       // when it has already been added
                    Element copy = (Element) child.copy();                                  // make a deep copy of child
                    HashMap<String, String> idOldAndNew = new HashMap<>();                  // HashMap with (oldId, newId) pairs, so we can also update references to these IDs

                    Nodes cs = copy.query("descendant-or-self::*[@xml:id or @id]");         // find all elements with an id attribute
                    for (int i = 0; i < cs.size(); ++i) {                                   // give them all unique ids
                        Element c = (Element) cs.get(i);
                        Attribute id = Helper.getAttribute("id", c);
                        String newId = "meico_expansion_of_" + id.getValue() + "_" + UUID.randomUUID().toString();  // the new IDs are of the following form: "meico_oldID_newUUID"
                        idOldAndNew.put("#" + id.getValue(), "#" + newId);
                        id.setValue(newId);                                                 // set the new ID
                    }

                    // check the copy's children for references to copies that now have a new ID and update theirs references
                    Nodes copyDescendants = copy.query(".//*");                             // get all descendant elements of copy
                    for (Node cd : copyDescendants) {                // get all descendants of copy
                        Element copyDescendant = (Element) cd;
                        for (int a=0; a < copyDescendant.getAttributeCount(); ++a) {        // search all attributes if they hold a reference
                            Attribute attr = copyDescendant.getAttribute(a);                // get the attribute
                            String oldId = attr.getValue();                                 // get the attribute value
                            if (!oldId.startsWith("#"))                                     // references start with #; if this attribute value does not, it is no reference
                                continue;
                            String newId = idOldAndNew.get(oldId);                          // get the replacement ID
                            if (newId == null)                                              // if there is no replacement ID
                                continue;                                                   // we have nothing to replace
                            attr.setValue(newId);
                        }
                    }

                    regularizedRoot.appendChild(copy);                                      // add the copy
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
        System.out.print("Adding IDs to MEI:");
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

    /**
     * returns the layer element in the mei tree of ofThis
     * @param ofThis
     * @return the layer element or null if ofThis is not in a layer
     */
    protected static Element getLayer(Element ofThis) {
        for (Node e = ofThis.getParent(); e != ofThis.getDocument().getRootElement(); e = e.getParent()) {  // search for a layer element among the parents of ofThis
            if ((e instanceof Element) && (((Element)e).getLocalName().equals("layer")))                    // found one
                return (Element)e;
        }
        return null;
    }

    /**
     * returns the def or n attribute value of an mei layer element or empty string if it is no layer or both attributes are missing
     * @param layer
     * @return def, n or empty string
     */
    protected static String getLayerId(Element layer) {
        if ((layer == null) || !layer.getLocalName().equals("layer"))   // if the element is null or no layer
            return "";                                                  // return empty string
        if (layer.getAttribute("def") != null)                          // check for the def attribute (preferred over n)
            return layer.getAttributeValue("def");                      // return its string
        if (layer.getAttribute("n") != null)                            // check for the n attribute
            return layer.getAttributeValue("n");                        // return its string
        return "";                                                      // no def or n attribute, hence, return empty string
    }

    /**
     * returns the staff element in the mei tree of ofThis
     * @param ofThis
     * @return the staff element or null if ofThis is not in a staff
     */
    protected static Element getStaff(Element ofThis) {
        for (Node e = ofThis.getParent(); e != ofThis.getDocument().getRootElement(); e = e.getParent()) {  // search for a staff element among the parents of ofThis
            if ((e instanceof Element) && (((Element)e).getLocalName().equals("staff")))                    // found one
                return (Element)e;
        }
        return null;
    }

    /**
     * returns the def or n attribute value of an mei staff element or empty string if it is no staff or both attributes are missing
     * @param staff
     * @return def, n or empty string
     */
    protected static String getStaffId(Element staff) {
        if ((staff == null) || !staff.getLocalName().equals("staff"))   // if the element is null or no staff
            return "";                                                  // return empty string
        if (staff.getAttribute("def") != null)                          // check for the def attribute (preferred over n)
            return staff.getAttributeValue("def");                      // return its string
        if (staff.getAttribute("n") != null)                            // check for the n attribute
            return staff.getAttributeValue("n");                        // return its string
        return "";                                                      // no def or n attribute, hence, return empty string
    }

}