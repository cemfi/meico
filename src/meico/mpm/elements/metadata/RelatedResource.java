package meico.mpm.elements.metadata;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This class represents an MPM resource element in the relatedResources list.
 * @author Axel Berndt
 */
public class RelatedResource extends AbstractXmlSubtree {
    private Attribute uri = null;
    private Attribute type = null;

    /**
     * this constructor instantiates the RelatedResource object from an existing xml source handed over as XOM Element
     * @param xml
     * @throws Exception
     */
    private RelatedResource(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * RelatedResource factory
     * @param xml
     * @return
     */
    public static RelatedResource createRelatedResource(Element xml) {
        RelatedResource relatedResource;
        try {
            relatedResource = new RelatedResource(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return relatedResource;
    }

    /**
     * This factory generates a RelatedResource object from raw data.
     * @param uri a non-null string
     * @param type a non-null string
     * @return
     */
    public static RelatedResource createRelatedResource(String uri, String type) {
        if ((uri == null) || (type == null))
            return null;

        Element resourceElt = new Element("resource", Mpm.MPM_NAMESPACE);
        RelatedResource relatedResource = RelatedResource.createRelatedResource(resourceElt);

        if (relatedResource == null)
            return null;

        relatedResource.setUri(uri);
        relatedResource.setType(type);

        return relatedResource;
    }

    /**
     * parse the xml data and set the according class variables
     * @param xml
     * @throws Exception
     */
    @Override
    protected void parseData(Element xml) throws Exception {
        if (xml == null)
            throw new Exception("Cannot generate RelatedResource object. XML Element is null.");

        this.setXml(xml);

        this.uri = Helper.getAttribute("uri", xml);
        if (this.uri == null) {
            this.uri = new Attribute("uri", "");
            this.getXml().addAttribute(this.uri);
        }

        this.type = Helper.getAttribute("type", xml);
        if (this.type == null) {
            this.type = new Attribute("type", "");
            this.getXml().addAttribute(this.type);
        }
    }

    /**
     * set the related resource's uri
     * @param uri
     */
    public void setUri(String uri) {
        this.uri.setValue(uri);
    }

    /**
     * get the uri of the related resource
     * @return
     */
    public String getUri() {
        return this.uri.getValue();
    }

    /**
     * set the related resource's type
     * @param type a string, whitespaces will be removes and only tokens are allowed in the XML
     */
    public void setType(String type) {
        this.type.setValue(type.replaceAll("\\s+", ""));
    }

    /**
     * get the related resource's type
     * @return
     */
    public String getType() {
        return this.type.getValue();
    }
}
