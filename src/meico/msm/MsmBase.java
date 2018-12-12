package meico.msm;

/**
 * This class is a primitive for Msm and Mpm.
 * @author Axel Berndt.
 */

import meico.mei.Helper;
import net.sf.saxon.s9api.Xslt30Transformer;
import nu.xom.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MsmBase {
    protected File file;
    protected Document data;                                // the data
    protected boolean isValid = false;                        // indicates whether the input file contained valid data code (true) or not (false); it is also false if no validation has been performed

    /**
     * constructor
     */
    public MsmBase() {
        this.file = null;
        this.data = null;                                            // empty document
        this.isValid = false;
    }

    /**
     * constructor
     *
     * @param document the data as XOM Document
     */
    public MsmBase(Document document) {
        this.file = null;
        this.data = document;
        this.isValid = false;
    }

    /**
     * constructor
     *
     * @param file the data file to be read
     * @throws IOException
     * @throws ParsingException
     */
    public MsmBase(File file) throws IOException, ParsingException {
        this(file, false);
    }

    /**
     * constructor
     * @param file
     * @param validate
     * @throws IOException
     * @throws ParsingException
     */
    public MsmBase(File file, boolean validate) throws IOException, ParsingException {
        this.readFromFile(file, validate);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @throws IOException
     * @throws ParsingException
     */
    public MsmBase(String xml) throws IOException, ParsingException {
        this(xml, false);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @param validate validate the code?
     * @throws IOException
     * @throws ParsingException
     */
    public MsmBase(String xml, boolean validate) throws IOException, ParsingException {
        Builder builder = new Builder(validate);                    // if the validate argument in the Builder constructor is true, the data should be valid
        try {
            this.data = builder.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ValidityException e) {                             // in case of a ValidityException (no valid data)
            this.data = e.getDocument();                             // make the XOM Document anyway, we may nonetheless be able to work with it
        }
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @throws IOException
     * @throws ParsingException
     */
    public MsmBase(InputStream inputStream) throws IOException, ParsingException {
        this(inputStream, false);
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @param validate
     * @throws IOException
     * @throws ParsingException
     */
    public MsmBase(InputStream inputStream, boolean validate) throws IOException, ParsingException {
        Builder builder = new Builder(validate);                    // if the validate argument in the Builder constructor is true, the data should be valid
        try {
            this.data = builder.build(inputStream);
        } catch (ValidityException e) {                             // in case of a ValidityException (no valid data code)
            this.data = e.getDocument();                             // make the XOM Document anyway, we may nonetheless be able to work with it
        }
    }

    /**
     * reads the data from an data file into this object
     * @param file
     * @param validate
     * @throws IOException
     * @throws ParsingException
     */
    protected synchronized void readFromFile(File file, boolean validate) throws IOException, ParsingException {
        this.file = file;

        if (!file.exists()) {
            System.err.println("No such file or directory: " + file.getPath());
            this.data = null;
            this.isValid = false;
            return;
        }

        // read file into the data instance of Document
        Builder builder = new Builder(validate);                    // if the validate argument in the Builder constructor is true, the data should be valid
        this.isValid = true;                                        // the musicXml code is valid until validation fails (ValidityException)
        try {
            this.data = builder.build(file);
        } catch (ValidityException e) {                             // in case of a ValidityException (no valid data code)
            this.isValid = false;                                   // set isValid false to indicate that the data code is not valid
            e.printStackTrace();                                    // output exception message
            for (int i = 0; i < e.getErrorCount(); i++) {           // output all validity error descriptions
                System.err.println(e.getValidityError(i));
            }
            this.data = e.getDocument();                             // make the XOM Document anyway, we may nonetheless be able to work with it
        }
    }

    /**
     * if the data is empty return false, else true
     *
     * @return false if the data is empty, else true
     */
    public boolean isValid() {
        return (this.isValid);
    }

    /**
     * validate the data
     * @return
     */
    public synchronized String validate(URL schema) {
        if (this.isEmpty()) return "No data present to be validated";    // no file, no validation

        this.isValid = true;                    // it is valid until the validator throws an exception
        String report = "Passed.";              // the validation report string, it will be overwritten if validation fails

        try {
            Helper.validateAgainstSchema(this.data.toXML(), schema);
//            Helper.validateAgainstSchema(this.file, new URL("http://www.music-encoding.org/schema/current/mei-CMN.rng"));     // this variant takes the schema from the web, the user has to be online for this!
        } catch (SAXException e) {              // invalid data
            this.isValid = false;
            report = "Failed. \n" + e.getMessage();
            e.printStackTrace();                // print the full error message
//            System.err.println(e.getMessage()); // print only the validation error message, not the complete stackTrace
        } catch (IOException e) {               // missing rng file
            this.isValid = false;
            report = "Failed.  Missing schema file!";
//            e.printStackTrace();
            System.err.println("Validation failed: missing schema file!");
        }
//        System.out.println("Validation of " + this.file.getName() + ": " + this.isValid);  // command line output of the result
        report = "Validation of " + this.file.getName() + ": " + report;
        return report;                          // return the result
    }

    /**
     * if the constructor was unable to load the file, the data is empty and no further operations
     *
     * @return true if the data document is empty, else false
     */
    public  boolean isEmpty() {
        return (this.data == null);
    }

    /**
     * @return String with the XML code
     */
    public String toXML() {
        return this.data.toXML();
    }

    /**
     * @return the data
     */
    public Document getDocument() {
        return this.data;
    }

    /**
     * a setter for the document
     * @param document
     */
    public synchronized void setDocument(Document document) {
        this.data = document;
    }

    /**
     * @return the root element of the data document
     */
    public Element getRootElement() {
        if (this.isEmpty())
            return null;
        return this.data.getRootElement();
    }

    /**
     * a getter that returns all part elements in the XML tree
     * @return
     */
    public Elements getParts() {
        return this.getRootElement().getChildElements("part");
    }

    /**
     * Generate a "raw" part element with its corresponding attributes and empty "header" and "dated" environments.
     * This element is not added to the document! It is up to the application to do this.
     * @param name
     * @param number
     * @param midiChannel
     * @param midiPort
     * @return the part element just generated
     */
    public static Element makePart(String name, String number, int midiChannel, int midiPort) {
        Element part = new Element("part");
        part.addAttribute(new Attribute("name", name));
        part.addAttribute(new Attribute("number", number));
        part.addAttribute(new Attribute("midi.channel", String.valueOf(midiChannel)));
        part.addAttribute(new Attribute("midi.port", String.valueOf(midiPort)));

        part.appendChild(new Element("header"));
        part.appendChild(new Element("dated"));

        return part;
    }

    /**
     * Generate a "raw" part element with its corresponding attributes and empty "header" and "dated" environments.
     * This element is not added to the document! It is up to the application to do this.
     * @param name
     * @param number
     * @param midiChannel
     * @param midiPort
     * @return the part element just generated
     */
    public static Element makePart(String name, int number, int midiChannel, int midiPort) {
        return makePart(name, String.valueOf(number), midiChannel, midiPort);
    }

    /**
     * search the given map for the first element with local-name name at or after the given midi.date
     * @param name
     * @param date
     * @param map
     * @return
     */
    public static Element getElementAtAfter(String name, double date, Element map) {
        Elements es;
        if (name.isEmpty())                     // if no specific name given
            es = map.getChildElements();        // search all elements
        else                                    // if specific name given
            es = map.getChildElements(name);    // search only the elements with this name

        for (int i=0; i < es.size(); ++i) {
            Element e = es.get(i);
            if ((e.getAttribute("midi.date") != null) && (Double.parseDouble(e.getAttributeValue("midi.date")) >= date))
                return e;
        }
        return null;
    }

    /**
     * search the given map and find the first element at or after the given midi.date
     * @param date
     * @param map
     * @return
     */
    public static Element getElementAtAfter(double date, Element map) {
        return Msm.getElementAtAfter("", date, map);
    }

    /**
     * search the given map and find the last element with the given local-name name before or at the given midi.date
     * @param name
     * @param date
     * @param map
     * @return
     */
    public static Element getElementBeforeAt(String name, double date, Element map) {
        Elements es;
        if (name.isEmpty())                     // if no specific name given
            es = map.getChildElements();        // search all elements
        else                                    // if specific name given
            es = map.getChildElements(name);    // search only the elements with this name

        for (int i=es.size()-1; i >= 0; --i) {
            Element e = es.get(i);
            if ((e.getAttribute("midi.date") != null) && (Double.parseDouble(e.getAttributeValue("midi.date")) <= date)) {
                return e;
            }
        }
        return null;
    }

    /**
     * search the given map and find the last element before or at the given midi.date
     * @param date
     * @param map
     * @return
     */
    public static Element getElementBeforeAt(double date, Element map) {
        return Msm.getElementBeforeAt("", date, map);
    }

    /**
     * this method removes all empty maps;
     * this is to make the data a bit smaller and less cluttered
     */
    public synchronized void deleteEmptyMaps() {
        if (this.isEmpty()) return;

        Nodes maps = this.getRootElement().query("descendant::*[contains(local-name(), 'Map')]");   // get all elements in the document that have a substring "Map" in their local-name
        for (int i=0; i < maps.size(); ++i) {                                           // go through all these elements
            Element map = (Element)maps.get(i);                                         // the map
            if (map.getChildCount() == 0)                                               // if the map has no children, it is empty
                map.getParent().removeChild(map);                                       // delete it
        }
    }

    /**
     * this getter returns the file
     *
     * @return a java File object (this file does not necessarily have to exist in the file system, but may be created there when writing the file with writeFile())
     */
    public File getFile() {
        return this.file;
    }

    /**
     * with this setter a new filename can be set
     *
     * @param filename the filename including the full path and extension
     */
    public synchronized void setFile(String filename) {
        this.file = new File(filename);
    }

    /**
     * writes the msm document to an msm file at this.file (it must be != null);
     * if there is already an msm file with this name, it is replaces!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeFile() {
        if (this.file == null) {
            System.err.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        if (this.isEmpty()) {
            System.err.println("Empty document, cannot write file.");
            return false;
        }

        return this.writeFile(this.file.getPath());
    }

    /**
     * writes the document to a file (filename should include the path and the extension)
     *
     * @param filename the filename string; it should include the path and the extension
     * @return true if success, false if an error occured
     */
    public synchronized boolean writeFile(String filename) {
        if (this.isEmpty()) {
            System.err.println("Empty document, cannot write file.");
            return false;
        }

        // create the file in the file system
        File file = new File(filename);
        file.getParentFile().mkdirs();                              // ensure that the directory exists
        try {
            file.createNewFile();                                   // create the file if it does not already exist
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        // open the FileOutputStream to write to the file
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, false);   // open file: second parameter (append) is false because we want to overwrite the file if already existing
        } catch (FileNotFoundException | NullPointerException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        // serialize the xml code (encoding, layout) and write it to the file via the FileOutputStream
        boolean returnValue = true;
        Serializer serializer = null;
        try {
            serializer = new Serializer(fileOutputStream, "UTF-8"); // connect serializer with FileOutputStream and specify encoding
            serializer.setIndent(4);                                // specify indents in xml code
            serializer.write(this.data);                             // write data to file
        } catch (NullPointerException | IOException e) {
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

    /**
     * transform the data via the given xsl file
     * @param xslt
     * @return result of the transform as XOM Document instance
     */
    public Document xslTransformToDocument(File xslt) {
        return Helper.xslTransformToDocument(this.data, xslt);
    }

    /**
     * transform the data via the given xsl transform
     * @param transform
     * @return result of the transform as XOM Document instance
     */
    public Document xslTransformToDocument(Xslt30Transformer transform) {
        return Helper.xslTransformToDocument(this.data, transform);
    }

    /**
     * transform the data via the given xsl file
     * @param xslt
     * @return result of the transform as String instance
     */
    public String xslTransformToString(File xslt) {
        return Helper.xslTransformToString(this.data, xslt);
    }

    /**
     * transform the data via the given xsl transform
     * @param transform
     * @return result of the transform as String instance
     */
    public String xslTransformToString(Xslt30Transformer transform) {
        return Helper.xslTransformToString(this.data, transform);
    }

}
