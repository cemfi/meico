package meico.musicxml;

import meico.mei.Helper;
import meico.mei.Mei;
import nu.xom.*;
import org.xml.sax.SAXException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * TODO
 *  - A musicXML Java library for some format-specific functionality
 *     - https://github.com/Audiveris/proxymusic
 *  - Verovio for MusicXML2MEI conversion and SVG export?
 *  - SVG2PDF ...
 *
 * @author Axel Berndt
 */

public class MusicXml extends meico.xml.XmlBase {

    /**
     * constructor
     */
    public MusicXml() {
        super();
    }

    /**
     * constructor
     *
     * @param document the data as XOM Document
     */
    public MusicXml(Document document) {
        super(document);
    }

    /**
     * constructor
     *
     * @param file the data file to be read, should be an uncompressed musicxml file!
     * @throws IOException
     * @throws ParsingException
     */
    public MusicXml(File file) throws IOException, ParsingException, SAXException, ParserConfigurationException {
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
    public MusicXml(File file, boolean validate, URL schema) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file, validate, schema);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @throws IOException
     * @throws ParsingException
     */
    public MusicXml(String xml) throws IOException, ParsingException {
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
    public MusicXml(String xml, boolean validate, URL schema) throws IOException, ParsingException {
        super(xml, validate, schema);
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @throws IOException
     * @throws ParsingException
     */
    public MusicXml(InputStream inputStream) throws IOException, ParsingException {
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
    public MusicXml(InputStream inputStream, boolean validate, URL schema) throws IOException, ParsingException {
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
        System.out.println("Converting " + ((this.file != null) ? this.file.getName() : "MusicXml data") + " to MEI.");

        ScriptEngineManager manager = new ScriptEngineManager();                // init Script Manager
        ScriptEngine engine = manager.getEngineByName("JavaScript");            // create Script Engine for JavaScript

        // get Verovio
        String verovio = (useOnlineVerovio) ? readTextFromURL("https://www.verovio.org/javascript/develop/verovio-toolkit.js") : null;  // if the online version of Verovio is demanded, try to get it
        if (verovio == null) {                                                  // if online Verovio is not available or the internal Verovio should be used
            try {
                verovio = this.readLocalVerovio();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        engine.put("data", this.toXML());
        Mei mei = null;

        try {
            engine.eval(verovio);                                                                               // this imports Verovio in the script engine's context, however, TODO: this fails
            String script = "var vrvToolkit = new verovio.toolkit(); var mei = vrvToolkit.getMEI(0, true);";    // getMEI(int:pageNumber, bool:trueMei)
            engine.eval(script);
            mei = new Mei((String)engine.get("mei"));
        } catch (ScriptException | IOException | ParsingException e) {
            e.printStackTrace();
            return null;
        }

        if (this.getFile() != null)
            mei.setFile(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".mei"); // set the filename extension of the export object to mei

        return mei;
    }

    /**
     * This method reads the file "/resources/Verovio/verovio-toolkit.js" from the jar.
     * @return
     * @throws IOException
     */
    private String readLocalVerovio() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/resources/Verovio/verovio-toolkit.js");  // open input stream

        // source of the following code block: https://stackoverflow.com/questions/309424/how-to-read-convert-an-inputstream-into-a-string-in-java
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1)
            result.write(buffer, 0, length);

        is.close();                 // close input stream
        return result.toString();   // return html code as string
    }

    /**
     * this method returns all text from a specified URL
     * source: https://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
     * @param url
     * @return
     */
    public static String readTextFromURL(String url) {
        try {
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
