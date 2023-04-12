package meico.musicxml;

import meico.mei.Mei;
import meico.xml.XmlBase;
import nu.xom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;

/**
 * TODO
 *  - A musicXML Java library for some format-specific functionality
 *     - https://github.com/Audiveris/proxymusic
 *  - Verovio for MusicXML2MEI conversion and SVG export?
 *  - SVG2PDF ...
 *
 * @author Axel Berndt
 */

public class MusicXmlLegacy extends XmlBase {

    /**
     * constructor
     */
    public MusicXmlLegacy() {
        super();
    }

    /**
     * constructor
     *
     * @param document the data as XOM Document
     */
    public MusicXmlLegacy(Document document) {
        super(document);
    }

    /**
     * constructor
     *
     * @param file the data file to be read, should be an uncompressed musicxml file!
     * @throws IOException
     * @throws ParsingException
     */
    public MusicXmlLegacy(File file) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file);
    }

    /**
     * constructor
     * @param file the data file to be read, should be an uncompressed musicxml file!
     * @param validate
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public MusicXmlLegacy(File file, boolean validate, URL schema) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file, validate, schema);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @throws IOException
     * @throws ParsingException
     */
    public MusicXmlLegacy(String xml) throws IOException, ParsingException {
        super(xml);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @param validate validate the code?
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public MusicXmlLegacy(String xml, boolean validate, URL schema) throws IOException, ParsingException {
        super(xml, validate, schema);
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @throws IOException
     * @throws ParsingException
     */
    public MusicXmlLegacy(InputStream inputStream) throws IOException, ParsingException {
        super(inputStream);
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @param validate
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public MusicXmlLegacy(InputStream inputStream, boolean validate, URL schema) throws IOException, ParsingException {
        super(inputStream, validate, schema);
    }

    /**
     * writes the data document to a file at this.file (it must be != null);
     * if there is already a file with this name, it is replaces!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeMusicXml() {
        return super.writeFile();
    }

    /**
     * writes the document to a file (filename should include the path and the extension)
     *
     * @param filename the filename string; it should include the path and the extension
     * @return true if success, false if an error occured
     */
    public synchronized boolean writeMusicXml(String filename) {
        return super.writeFile(filename);
    }

    /**
     * converts the MusicXML data to MEI
     * @param useOnlineVerovio true (uses the latest version of Verovio, requires Internet connection), false (use meico's internal Verovio version)
     * @return the Mei instance or null if failed
     * TODO: The current implementation based on Verovio does not work! And even if, the conversion of MusicXML features to MEI would be rather limited.
     */
    public Mei exportMei(boolean useOnlineVerovio) {
        return null;

//        long startTime = System.currentTimeMillis();                            // we measure the time that the conversion consumes
//        System.out.println("\nConverting " + ((this.file != null) ? this.file.getName() : "MusicXml data") + " to MEI.");
//
//        ScriptEngineManager manager = new ScriptEngineManager();                // init Script Manager
//        ScriptEngine engine = manager.getEngineByName("JavaScript");            // create Script Engine for JavaScript
//
//        String verovio = (useOnlineVerovio) ? VerovioProvider.getVerovio(this) : VerovioProvider.getLocalVerovio(this); // get the Verovio Toolkit script
//        if (verovio == null) {
//            System.err.println("MusicXML to MEI conversion failed: Verovio Toolkit not available. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");
//            return null;
//        }
//
//        engine.put("data", this.toXML());
//        Mei mei = null;
//
//        try {
//            engine.eval(verovio);                                                                               // this imports Verovio in the script engine's context, however, TODO: this fails because Verovio requires a browser environment
//            String script = "var vrvToolkit = new verovio.toolkit(); var mei = vrvToolkit.getMEI(0, true);";    // getMEI(int:pageNumber, bool:trueMei)
//            engine.eval(script);
//            mei = new Mei((String)engine.get("mei"));
//        } catch (ScriptException | IOException | ParsingException e) {
//            System.err.println("MusicXML to MEI conversion failed: script evaluation failed. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");
////            e.printStackTrace();
//            return null;
//        }
//
//        if (this.getFile() != null)
//            mei.setFile(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".mei"); // set the filename extension of the export object to mei
//
//        System.err.println("MusicXML to MEI conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");
//        return mei;
    }
}
