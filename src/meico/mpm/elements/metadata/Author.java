package meico.mpm.elements.metadata;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Text;

/**
 * This class interfaces the author element of MPM.
 * @author Axel Berndt
 */
public class Author extends AbstractXmlSubtree {
    private Text name = null;
    private Attribute number = null;
    private Attribute id = null;

    /**
     * this constructor instantiates the Author object from an existing xml source handed over as XOM Element
     * @param xml
     * @throws Exception
     */
    private Author(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * author factory
     * @param xml
     * @return
     */
    public static Author createAuthor(Element xml) {
        Author author;
        try {
            author = new Author(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return author;
    }

    /**
     * This factory generates an Author object from raw data.
     * @param name a non-null string, empty string is allowed
     * @param number an integer or null
     * @param id a string or null
     * @return
     */
    public static Author createAuthor(String name, Integer number, String id) {
        if (name == null)
            return null;

        Element authorElt = new Element("author", Mpm.MPM_NAMESPACE);
        Author author = Author.createAuthor(authorElt);

        if (author == null)
            return null;

        author.setName(name);
        author.setNumber(number);
        author.setId(id);

        return author;
    }

    /**
     * parse the author element and set the according class variables
     * @param xml
     * @throws Exception
     */
    @Override
    protected void parseData(Element xml) throws Exception {
        if (xml == null)
            throw new Exception("Cannot generate Author object. XML Element is null.");

        this.setXml(xml);

        if ((xml.getChildCount() == 0) || !(xml.getChild(0) instanceof Text)) {  // if no text node is given in the xml source
            this.name = new Text("");                                            // generate a placeholder text node
            xml.appendChild(this.name);                                          // add it to the xml
        } else
            this.name = (Text) xml.getChild(0);

        this.number = Helper.getAttribute("number", xml);
        this.id = Helper.getAttribute("id", xml);
    }

    /**
     * set the author's name
     * @param name
     */
    public void setName(String name) {
        this.name.setValue(name);
    }

    /**
     * get the author's name
     * @return
     */
    public String getName() {
        return this.name.getValue();
    }

    /**
     * set the author's number
     * @param number an integer or null
     */
    public void setNumber(Integer number) {
        if (number == null) {
            if (this.number != null) {
                this.number.detach();
                this.number = null;
            }
            return;
        }

        if (this.number == null) {
            this.number = new Attribute("number", String.valueOf(number));
            this.getXml().addAttribute(this.number);
            return;
        }

        this.number.setValue(String.valueOf(number));
    }

    /**
     * get the author's number
     * @return an integer or null
     */
    public Integer getNumber() {
        if (this.number == null)
            return null;

        return Integer.parseInt(this.number.getValue());
    }

    /**
     * set the author's id
     * @param id a xml:id string or null
     */
    public void setId(String id) {
        if (id == null) {
            if (this.id != null) {
                this.id.detach();
                this.id = null;
            }
            return;
        }

        if (this.id == null) {
            this.id = new Attribute("id", id);
            this.id.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");    // set correct namespace
            this.getXml().addAttribute(this.id);
            return;
        }

        this.id.setValue(id);
    }

    /**
     * get the author's id
     * @return a string or null
     */
    public String getId() {
        if (this.id == null)
            return null;

        return this.id.getValue();
    }
}
