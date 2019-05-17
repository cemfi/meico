package meico.svg;

import meico.xml.XmlBase;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * This class interfaces SVG data.
 * One such SVG is one page of the score.
 *
 * @author Axel Berndt
 */

public class Svg extends XmlBase {
    /**
     * constructor
     */
    public Svg() {
        super();
    }

    /**
     * constructor
     *
     * @param document the data as XOM Document
     */
    public Svg(Document document) {
        super(document);
    }

    /**
     * constructor
     *
     * @param file the data file to be read, should be an uncompressed SVG file!
     * @throws IOException
     * @throws ParsingException
     */
    public Svg(File file) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file);
    }

    /**
     * constructor
     * @param file the data file to be read, should be an uncompressed SVG file!
     * @param validate
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public Svg(File file, boolean validate, URL schema) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file, validate, schema);
    }

    /**
     * constructor
     * @param svg xml code as UTF8 String
     * @throws IOException
     * @throws ParsingException
     */
    public Svg(String svg) throws IOException, ParsingException {
        super(svg);
    }

    /**
     * constructor
     * @param svg xml code as UTF8 String
     * @param validate validate the code?
     * @param schema can be null
     * @throws IOException
     * @throws ParsingException
     */
    public Svg(String svg, boolean validate, URL schema) throws IOException, ParsingException {
        super(svg, validate, schema);
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @throws IOException
     * @throws ParsingException
     */
    public Svg(InputStream inputStream) throws IOException, ParsingException {
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
    public Svg(InputStream inputStream, boolean validate, URL schema) throws IOException, ParsingException {
        super(inputStream, validate, schema);
    }

    /**
     * writes the data document to a file at this.file (it must be != null);
     * if there is already a file with this name, it is replaces!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeSvg() {
        return super.writeFile();
    }

    /**
     * writes the document to a file (filename should include the path and the extension)
     *
     * @param filename the filename string; it should include the path and the extension
     * @return true if success, false if an error occured
     */
    public synchronized boolean writeSvg(String filename) {
        return super.writeFile(filename);
    }


}
