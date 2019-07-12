package meico.msm;

import nu.xom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;

/**
 * This class is a primitive for Msm and Mpm.
 * @author Axel Berndt.
 */

public abstract class AbstractMsm extends meico.xml.XmlBase {

    /**
     * constructor
     */
    public AbstractMsm() {
        super();
    }

    /**
     * constructor
     *
     * @param document the data as XOM Document
     */
    public AbstractMsm(Document document) {
        super(document);
    }

    /**
     * constructor
     *
     * @param file the data file to be read
     * @throws IOException
     * @throws ParsingException
     */
    public AbstractMsm(File file) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file);
    }

    /**
     * constructor
     * @param file
     * @param validate
     * @throws IOException
     * @throws ParsingException
     */
    public AbstractMsm(File file, boolean validate, URL schema) throws IOException, ParsingException, SAXException, ParserConfigurationException {
        super(file, validate, schema);
    }

    /**
     * constructor
     * @param xml xml code as UTF8 String
     * @throws IOException
     * @throws ParsingException
     */
    public AbstractMsm(String xml) throws IOException, ParsingException {
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
    public AbstractMsm(String xml, boolean validate, URL schema) throws IOException, ParsingException {
        super(xml, validate, schema);
    }

    /**
     * constructor
     * @param inputStream read from this input stream
     * @throws IOException
     * @throws ParsingException
     */
    public AbstractMsm(InputStream inputStream) throws IOException, ParsingException {
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
    public AbstractMsm(InputStream inputStream, boolean validate, URL schema) throws IOException, ParsingException {
        super(inputStream, validate, schema);
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
        return AbstractMsm.makePart(name, String.valueOf(number), midiChannel, midiPort);
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
}
