package meico.xml;

import com.sun.media.sound.InvalidDataException;
import nu.xom.Element;

/**
 * This is the prototype for classes that occur within the Mpm data structure.
 * @author Axel Berndt
 */

public abstract class AbstractXmlSubtree {
    private Element xml = null;   // the actual xml data

    /**
     * a getter for the XOM Element/xml representation
     * @return
     */
    public Element getXml() {
        return this.xml;
    }

    /**
     * this sets the xml data
     * not for the public, hence, protected
     * @param xml
     */
    protected void setXml(Element xml) {
        this.xml = xml;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     * @throws InvalidDataException
     */
    protected abstract void parseData(Element xml) throws InvalidDataException;

    /**
     * @return String with the XML code
     */
    public synchronized String toXml() {
        if (this.xml == null)
            return "";
        return this.xml.toXML();
    }
}
