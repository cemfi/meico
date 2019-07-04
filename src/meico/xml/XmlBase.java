package meico.xml;

import meico.mei.Helper;
import net.sf.saxon.s9api.Xslt30Transformer;
import nu.xom.*;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * This class is a primitive for all XML-based classes in meico.
 * @author Axel Berndt
 */

public class XmlBase {

    protected File file = null;             // the data file
    protected Document data = null;         // the xom Document representation of the XML data
    protected boolean isValid = false;      // indicates whether the input file contained valid data code (true) or not (false); it is also false if no validation has been performed

    /**
     * constructor
     */
    public XmlBase() {
        this.file = null;
        this.data = null;
        this.isValid = false;
    }

    /**
     * constructor
     *
     * @param document the data as XOM Document
     */
    public XmlBase(Document document) {
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
    public XmlBase(File file) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        this(file, false, null);
    }

    /**
     * constructor
     * @param file
     * @param validate
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public XmlBase(File file, boolean validate, URL schema) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        this.readFromFile(file, validate, schema);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @throws IOException
     * @throws ParsingException
     */
    public XmlBase(String xml) throws IOException, ParsingException {
        this(xml, false, null);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @param validate validate the code?
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public XmlBase(String xml, boolean validate, URL schema) throws IOException, ParsingException {
        Builder builder = new Builder(false);                    // if the validate argument in the Builder constructor is true, the data should be valid
        try {
            this.data = builder.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ValidityException e) {                             // in case of a ValidityException (no valid data)
            this.data = e.getDocument();                            // make the XOM Document anyway, we may nonetheless be able to work with it
        }
        if (validate && (schema != null))                           // if the mei file should be validated
            System.out.println(this.validate(schema));              // do so, the result is stored in this.isValid
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @throws IOException
     * @throws ParsingException
     */
    public XmlBase(InputStream inputStream) throws IOException, ParsingException {
        this(inputStream, false, null);
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @param validate
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public XmlBase(InputStream inputStream, boolean validate, URL schema) throws IOException, ParsingException {
        Builder builder = new Builder(false);                    // if the validate argument in the Builder constructor is true, the data should be valid
        try {
            this.data = builder.build(inputStream);
        } catch (ValidityException e) {                             // in case of a ValidityException (no valid data code)
            this.data = e.getDocument();                            // make the XOM Document anyway, we may nonetheless be able to work with it
        }
        if (validate && (schema != null))                           // if the mei file should be validated
            System.out.println(this.validate(schema));              // do so, the result is stored in this.isValid
    }

    /**
     * reads the data from an data file into this object
     * @param file
     * @param validate
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    protected synchronized void readFromFile(File file, boolean validate, URL schema) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        this.file = file;

        if (!file.exists()) {
            System.err.println("No such file or directory: " + file.getPath());
            this.data = null;
            this.isValid = false;
            return;
        }

        // read file into the data instance of Document
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();    // create a SAX parser (see https://stackoverflow.com/questions/51072419/how-use-xmlreaderfactory-now-because-this-is-deprecated)
        SAXParser parser = parserFactory.newSAXParser();
        XMLReader xmlreader = parser.getXMLReader();                        // with the SAX parser create an xml reader
        xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);  // disable fetching of DTD as this usually does not work with XOM (see https://stackoverflow.com/questions/8081214/ignoring-dtd-when-parsing-xml)
        Builder builder = new Builder(xmlreader);                           // if the validate argument in the Builder constructor is true, the data should be valid
        this.isValid = false;
        try {
            this.data = builder.build(file);
        } catch (ValidityException e) {                                     // in case of a ValidityException (no valid data code)
//            this.isValid = false;                                           // set isValid false to indicate that the data code is not valid
//            e.printStackTrace();                                            // output exception message
//            for (int i = 0; i < e.getErrorCount(); i++) {                   // output all validity error descriptions
//                System.err.println(e.getValidityError(i));
//            }
            this.data = e.getDocument();                                    // make the XOM Document anyway, we may nonetheless be able to work with it
        }

        if (validate && (schema != null))                                   // if the mei file should be validated
            System.out.println(this.validate(schema));                      // do so, the result is stored in this.isValid
    }

    /**
     * if the data is empty return false, else true
     *
     * @return false if the data is empty, else true
     */
    public boolean isValid() {
        return this.isValid;
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
     * this getter returns the file
     *
     * @return a java File object (this file does not necessarily have to exist in the file system, but may be created there when writing the file with writeFile())
     */
    public File getFile() {
        return this.file;
    }

    /**
     * a setter to change the file
     * @param file
     */
    public synchronized void setFile(File file) {
        this.file = file;
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
     * writes the data document to a file at this.file (it must be != null);
     * if there is already a file with this name, it is replaces!
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
    public synchronized String toXML() {
        if (this.isEmpty())
            return "";
        return this.data.toXML();
    }

    /**
     * @return the data
     */
    public Document getDocument() {
        if (this.isEmpty())
            return null;
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
     * transform the data via the given xsl file
     * @param xslt
     * @return result of the transform as XOM Document instance
     */
    public Document xslTransformToDocument(File xslt) {
        if (this.isEmpty())
            return null;
        return Helper.xslTransformToDocument(this.data, xslt);
    }

    /**
     * transform the data via the given xsl transform
     * @param transform
     * @return result of the transform as XOM Document instance
     */
    public Document xslTransformToDocument(Xslt30Transformer transform) {
        if (this.isEmpty())
            return null;
        return Helper.xslTransformToDocument(this.data, transform);
    }

    /**
     * transform the data via the given xsl file
     * @param xslt
     * @return result of the transform as String instance
     */
    public String xslTransformToString(File xslt) {
        if (this.isEmpty())
            return null;
        return Helper.xslTransformToString(this.data, xslt);
    }

    /**
     * transform the data via the given xsl transform
     * @param transform
     * @return result of the transform as String instance
     */
    public String xslTransformToString(Xslt30Transformer transform) {
        if (this.isEmpty())
            return null;
        return Helper.xslTransformToString(this.data, transform);
    }
}
