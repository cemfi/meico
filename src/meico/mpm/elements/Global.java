package meico.mpm.elements;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Element;

/**
 * This class interfaces the global information, as opposed to local, i.e. part-specific information.
 * @author Axel Berndt
 */
public class Global extends AbstractXmlSubtree {
    private Header header = null;       // the header environment
    private Dated dated = null;         // the dated environment

    /**
     * constructor
     * @throws Exception
     */
    private Global() throws Exception {
        this.parseData(new Element("global", Mpm.MPM_NAMESPACE));
    }

    /**
     * this constructor instantiates the Global object from an existing xml source handed over as XOM Element
     * @param xml
     * @throws Exception
     */
    private Global(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * global factory
     * @return
     */
    public static Global createGlobal() {
        Global global;
        try {
            global = new Global();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return global;
    }

    /**
     * global factory
     * @param xml
     * @return
     */
    public static Global createGlobal(Element xml) {
        Global global;
        try {
            global = new Global(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return global;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    @Override
    protected void parseData(Element xml) throws Exception {
        if (xml == null)
            throw new Exception("Cannot generate Global object. XML Element is null.");

        this.setXml(xml);

        // make sure that this element is really a "global" element
        if (!this.getXml().getLocalName().equals("global")) {
            this.getXml().setLocalName("global");
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
            throw new Exception("Cannot generate Global object. Failed to generate Dated object.");

        this.dated.setEnvironment(this, null);      // link the global (and local) environment
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
}
