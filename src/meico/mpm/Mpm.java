package meico.mpm;

import meico.mei.Helper;
import meico.mpm.elements.Performance;
import meico.msm.AbstractMsm;
import nu.xom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class holds data in mpm format (Music Performance Markup).
 * @author Axel Berndt.
 */

public class Mpm extends AbstractMsm {
    public static final String MPM_NAMESPACE                    = "http://www.cemfi.de/mpm/ns/1.0";

    // type constants of style definitions in the header environment, the application may create and process more style types by addressing them with their local name
    public static final String ARTICULATION_STYLE               = "articulationStyles";
    public static final String ORNAMENTATION_STYLE              = "ornamentationStyles";
    public static final String DYNAMICS_STYLE                   = "dynamicsStyles";
    public static final String METRICAL_ACCENTUATION_STYLE      = "metricalAccentuationStyles";
    public static final String TEMPO_STYLE                      = "tempoStyles";
    public static final String RUBATO_STYLE                     = "rubatorStyles";

    // map type constants that occur in the dated environment, the application may create and process more map types by addressing them with their local name
    public static final String ARTICULATION_MAP                 = "articulationMap";
    public static final String ORNAMENTATION_MAP                = "ornamentationMap";
    public static final String DYNAMICS_MAP                     = "dynamicsMap";
    public static final String METRICAL_ACCENTUATION_MAP        = "metricalAccentuationMap";
    public static final String TEMPO_MAP                        = "tempoMap";
    public static final String RUBATO_MAP                       = "rubatoMap";
    public static final String ASYNCHRONY_MAP                   = "asynchronyMap";
    public static final String IMPRECISION_MAP                  = "imprecisionMap";
    public static final String IMPRECISION_MAP_TIMING           = "imprecisionMap.timing";
    public static final String IMPRECISION_MAP_DYNAMICS         = "imprecisionMap.dynamics";
    public static final String IMPRECISION_MAP_TONEDURATION     = "imprecisionMap.toneduration";
    public static final String IMPRECISION_MAP_TUNING           = "imprecisionMap.tuning";

    private Element referenceMusic = null;
    private ArrayList<Performance> performances = new ArrayList<>();

    /**
     * constructor
     */
    public Mpm() {
        super();
        this.init(); // create a plain empty xml structure
    }

    /**
     * constructor
     *
     * @param mpm the mpm document of which to instantiate the Mpm object
     */
    public Mpm(Document mpm) {
        super(mpm);
        this.parseData();
    }

    /**
     * constructor
     *
     * @param file the mpm file to be read
     * @throws IOException
     * @throws ParsingException
     */
    public Mpm(File file) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file);
        this.parseData();
    }

    /**
     * constructor
     * @param file
     * @param validate
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public Mpm(File file, boolean validate, URL schema) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file, validate, schema);
        this.parseData();
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @throws IOException
     * @throws ParsingException
     */
    public Mpm(String xml) throws IOException, ParsingException {
        super(xml);
        this.parseData();
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @param validate validate the code?
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public Mpm(String xml, boolean validate, URL schema) throws IOException, ParsingException {
        super(xml, validate, schema);
        this.parseData();
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @throws IOException
     * @throws ParsingException
     */
    public Mpm(InputStream inputStream) throws IOException, ParsingException {
        super(inputStream);
        this.parseData();
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @param validate
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public Mpm(InputStream inputStream, boolean validate, URL schema) throws IOException, ParsingException {
        super(inputStream, validate, schema);
        this.parseData();
    }

    /**
     * an Mpm factory
     * @return
     */
    public static Mpm createMpm() {
        return new Mpm();
    }

    /**
     * this parses the xml data and generates Performance objects from it that go into the performances ArrayList
     */
    private void parseData() {
//        System.out.println("Parsing MPM data ...");

        // get the link to the referenceMusic tag
        this.referenceMusic = Helper.getFirstChildElement("referenceMusic", this.getRootElement());
        if (this.referenceMusic == null) {                                          // if it does not exist
//            System.out.println("No referenceMusic found. Generating an empty one.");
            this.referenceMusic = new Element("referenceMusic", Mpm.MPM_NAMESPACE); // create it
            this.getRootElement().appendChild(this.referenceMusic);                 // and add it to the xml tree
        }

        // parse the performances
        LinkedList<Element> perfs = Helper.getAllChildElements("performance", this.getRootElement());
//        System.out.println(perfs.size() + " performances found.");

        for (Element perf : perfs) {                                        // go through all performance elements
            Performance p = Performance.createPerformance(perf);            // generate an instance of class Performance from it
            if (p == null)
                continue;
            this.performances.add(p);
        }
    }

    /**
     * a helper method for the constructor Mpm(), it create an initial mpm document with a root and a referenceMusic element
     * @return the root element
     */
    private Element init() {
        Element root = new Element("mpm", Mpm.MPM_NAMESPACE);    // the second string defines the namespace
        this.referenceMusic = new Element("referenceMusic", Mpm.MPM_NAMESPACE);
        root.appendChild(this.referenceMusic);
        this.data = new Document(root);
        return root;
    }

    /**
     * add a music reference to the referenceMusic list
     * @param uri
     * @param type
     * @return a link to the element just added
     */
    public Element addReferenceMusic(String uri, String type) {
        Element ref = new Element("reference", Mpm.MPM_NAMESPACE);
        ref.addAttribute(new Attribute("uri", uri));
        ref.addAttribute(new Attribute("type", type));
        this.referenceMusic.appendChild(ref);
        return ref;
    }

    /**
     * a getter for the referenceMusic element
     * @return
     */
    public Element getReferenceMusic() {
        return this.referenceMusic;
    }

    /**
     * get the number of performances in this mpm
     * @return
     */
    public int size() {
        return this.performances.size();
    }

    /**
     * Get a performance by name.
     * If the mpm holds more than one performance with this name, this method will return only the first. Use getAllPerformances() to access all performances and find the right one.
     * @param name
     * @return the performance or null if there is no performance with this name
     */
    public Performance getPerformance(String name) {
        for (Performance p : this.performances) {
            if (p.getName().equals(name))
                return p;
        }
        return null;
    }

    /**
     * access a performance by index
     * @param i
     * @return
     */
    public Performance getPerformance(int i) {
        if (i >= this.performances.size())
            return null;
        return this.performances.get(i);
    }

    /**
     * this returns all performances in this mpm as an ArrayList
     * @return
     */
    public ArrayList<Performance> getAllPerformances() {
        return this.performances;
    }

    /**
     * add a performance to this mpm, but caution: if another performance with the same name exists already in this mpm, accessing it via getPerformance(name) will return only the first in the list
     * @param performance
     */
    public void addPerformance(Performance performance) {
        this.getRootElement().appendChild(performance.getXml());
        this.performances.add(performance);
    }

    /**
     * generate a performance and add it to this mpm, but caution: if another performance with the same name exists already in this mpm, accessing it via getPerformance(name) will return only the first in the list
     * @param name
     * @return
     */
    public Performance addPerformance(String name) {
        Performance performance = Performance.createPerformance(name);
        if (performance == null)
            return null;
        this.addPerformance(performance);
        return performance;
    }

    /**
     * remove all performances with the specified name from this mpm
     * @param name
     */
    public void removePerformance(String name) {
        for (Performance p : this.performances) {               // all performances
            if (p.getName().equals(name)) {                     // with this name
                this.performances.remove(p);                    // get removed
                this.getRootElement().removeChild(p.getXml());  // also from the xml structure
            }
        }
    }

    /**
     * remove the specified performance from this mpm
     * @param performance
     */
    public void removePerformance(Performance performance) {
        if (this.performances.remove(performance))                      // if the performance was in this mpm and could be removed from the performances list
            this.getRootElement().removeChild(performance.getXml());    // it can be removed from the xml structure
    }

    /**
     * writes the mpm document to an mpm file at this.file (it must be != null);
     * if there is already an msm file with this name, it is replaces!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeMpm() {
        return this.writeFile();
    }

    /**
     * writes the mpm document to a file (filename should include the path and the extension .mpm)
     *
     * @param filename the filename string; it should include the path and the extension .mpm
     * @return true if success, false if an error occured
     */
    public synchronized boolean writeMpm(String filename) {
        return this.writeFile(filename);
    }
}
