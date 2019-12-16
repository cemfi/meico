package meico.mpm.elements;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This class interfaces the part-specific information, as opposed to global information that apply to all parts.
 * @author Axel Berndt
 */
public class Part extends AbstractXmlSubtree {
    private Global global = null;       // a link to the global environment
    private Header header = null;       // the header environment
    private Dated dated = null;         // the dated environment

    private Attribute name = null;      // the name of the part
    private int number = 0;             // the part number
    private int midiChannel = 0;        // the midi channel
    private int midiPort = 0;           // the midi port

    /**
     * constructor for a plain/empty Part object
     * @param name the name of the part, can be "" (empty string)
     * @param number
     * @param midiChannel
     * @param midiPort
     * @throws InvalidDataException
     */
    private Part(String name, int number, int midiChannel, int midiPort) throws InvalidDataException {
        Element part = new Element("part", Mpm.MPM_NAMESPACE);
        part.addAttribute(new Attribute("name", name));
        part.addAttribute(new Attribute("number", Integer.toString(number)));
        part.addAttribute(new Attribute("midi.channel", Integer.toString(midiChannel)));
        part.addAttribute(new Attribute("midi.port", Integer.toString(midiPort)));
        this.parseData(part);
    }

    /**
     * this constructor instantiates the MpmGlobal object from an existing xml source handed over as XOM Element
     * @param xml
     */
    private Part(Element xml) throws InvalidDataException {
        this.parseData(xml);
    }

    /**
     * part factory
     * @param name the name of the part, can be "" (empty string)
     * @param number
     * @param midiChannel
     * @param midiPort
     * @return
     */
    public static Part createPart(String name, int number, int midiChannel, int midiPort) {
        Part part;
        try {
            part = new Part(name, number, midiChannel, midiPort);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return part;
    }

    /**
     * part factory
     * @param xml
     * @return
     */
    public static Part createPart(Element xml) {
        Part part;
        try {
            part = new Part(xml);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return part;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    @Override
    protected void parseData(Element xml) throws InvalidDataException {
        if (xml == null)
            throw new InvalidDataException("Cannot generate Part object. XML Element is null.");

        this.name = Helper.getAttribute("name", xml);
        if (this.name == null) {                                                                                        // each part requires a name, if there is none
            this.name = new Attribute("name", "");                                                                      // generate an empty name attribute
            this.getXml().addAttribute(this.name);                                                                      // and add it to the element
//            throw new InvalidDataException("Cannot generate MpmPart object. Attribute name is missing.");               // throw exception
        }

        Attribute number = Helper.getAttribute("number", xml);
        if ((number == null) || number.getValue().isEmpty()) {                                                          // each part requires a number, if there is none or it is empty
            throw new InvalidDataException("Cannot generate Part object. Attribute number is missing or empty.");       // throw exception
        }

        Attribute midiChannelAtt = Helper.getAttribute("midi.channel", xml);
        if ((midiChannelAtt == null) || midiChannelAtt.getValue().isEmpty()) {                                          // each part requires a midi.channel, if there is none or it is empty
            throw new InvalidDataException("Cannot generate Part object. Attribute midi.channel is missing or empty."); // throw exception
        }

        Attribute midiPortAtt = Helper.getAttribute("midi.port", xml);
        if ((midiPortAtt == null) || midiPortAtt.getValue().isEmpty()) {                                                // each part requires a midi.port, if there is none or it is empty
            throw new InvalidDataException("Cannot generate Part object. Attribute midi.port is missing or empty.");    // throw exception
        }

        this.setXml(xml);
        this.number = Integer.parseInt(number.getValue());
        this.midiChannel = Integer.parseInt(midiChannelAtt.getValue());
        this.midiPort = Integer.parseInt(midiPortAtt.getValue());

        // make sure that this element is really a "part" element
        if (!this.getXml().getLocalName().equals("part")) {
            this.getXml().setLocalName("part");
        }

        // make sure there is a header environment
        Element headerElt = Helper.getFirstChildElement("header", this.getXml());
        if (headerElt == null) {
            this.header = Header.createHeader();
            this.getXml().appendChild(this.header.getXml());
        } else {
            this.header = Header.createHeader(headerElt);
        }

        // make sure there is a dated environment
        Element datedElt = Helper.getFirstChildElement("dated", this.getXml());
        if (datedElt == null) {
            this.dated = Dated.createDated();
            this.getXml().appendChild(this.dated.getXml());
        } else {
            this.dated = Dated.createDated(datedElt);
        }

        if (this.dated == null)
            throw new InvalidDataException("Cannot generate Part object. Failed to generate Dated object.");

        this.dated.setEnvironment(this.global, this);                                                                   // link the global and local environment
    }

    /**
     * access the header environment
     * @return
     */
    public Header getHeader() {
        return this.header;
    }

    /**
     * access the dated environment
     * @return
     */
    public Dated getDated() {
        return this.dated;
    }

    /**
     * read the name of the part
     * @return
     */
    public String getName() {
        return this.name.getValue();
    }

    /**
     * edit the part's name
     * @param name
     */
    public void setName(String name) {
        this.name.setValue(name);
    }

    /**
     * get the part's number
     * @return
     */
    public int getNumber() {
        return this.number;
    }

    /**
     * set the part's number
     * @param number
     */
    public void setNumber(int number) {
        this.number = number;
        Helper.getAttribute("number", this.getXml()).setValue(Integer.toString(this.number));
    }

    /**
     * get the part's midi.channel value
     * @return
     */
    public int getMidiChannel() {
        return this.midiChannel;
    }

    /**
     * set the part's midi.channel value
     * @param midiChannel
     */
    public void setMidiChannel(int midiChannel) {
        this.midiChannel = midiChannel;
        Helper.getAttribute("midi.channel", this.getXml()).setValue(Integer.toString(this.midiChannel));
    }

    /**
     * get the part's midi.port value
     * @return
     */
    public int getMidiPort() {
        return this.midiPort;
    }

    /**
     * set the part's midi.port value
     * @param midiPort
     */
    public void setMidiPort(int midiPort) {
        this.midiPort = midiChannel;
        Helper.getAttribute("midi.port", this.getXml()).setValue(Integer.toString(this.midiPort));
    }

    /**
     * set the link to the global environment
     * @param global
     */
    public void setGlobal(Global global) {
        this.global = global;
        this.getDated().setEnvironment(this.global, this); // update the dated environment which does the same for all maps
    }

    /**
     * access the global environment that this part links to
     * @return
     */
    public Global getGlobal() {
        return this.global;
    }
}
