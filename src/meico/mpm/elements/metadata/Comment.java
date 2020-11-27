package meico.mpm.elements.metadata;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Text;

/**
 * This class interfaces the comment element of MPM.
 * @author Axel Berndt
 */
public class Comment extends AbstractXmlSubtree {
    private Text text = null;
    private Attribute id = null;

    /**
     * this constructor instantiates the Comment object from an existing xml source handed over as XOM Element
     * @param xml
     * @throws Exception
     */
    private Comment(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * comment factory
     * @param xml
     * @return
     */
    public static Comment createComment(Element xml) {
        Comment comment;
        try {
            comment = new Comment(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return comment;
    }

    /**
     * This factory generates a comment object from raw data.
     * @param text a string or null (for an empty comment)
     * @param id an id string or null
     * @return
     */
    public static Comment createComment(String text, String id) {
        Element commentElt = new Element("comment", Mpm.MPM_NAMESPACE);
        Comment comment = Comment.createComment(commentElt);

        if (comment == null)
            return null;

        comment.setText(text);
        comment.setId(id);

        return comment;
    }

    /**
     * parse the comment element and set the according class variables
     * @param xml
     * @throws Exception
     */
    @Override
    protected void parseData(Element xml) throws Exception {
        if (xml == null)
            throw new Exception("Cannot generate Comment object. XML Element is null.");

        this.setXml(xml);

        if ((xml.getChildCount() == 0) || !(xml.getChild(0) instanceof Text)) {  // if no text node is given in the xml source
            this.text = new Text("");                                            // generate a placeholder text node
            xml.appendChild(this.text);                                          // add it to the xml
        } else
            this.text = (Text) xml.getChild(0);

        this.id = Helper.getAttribute("id", xml);
    }

    /**
     * set the comment's text
     * @param text
     */
    public void setText(String text) {
        this.text.setValue(text);
    }

    /**
     * get the comment's text
     * @return
     */
    public String getText() {
        return this.text.getValue();
    }

    /**
     * set the comment's id
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
     * get the comment's id
     * @return a string or null
     */
    public String getId() {
        if (this.id == null)
            return null;

        return this.id.getValue();
    }
}
