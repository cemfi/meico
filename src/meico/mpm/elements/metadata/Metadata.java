package meico.mpm.elements.metadata;

import meico.mei.Helper;
import meico.mpm.Mpm;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * This class interfaces the metadata of the MPM instance.
 * @author Axel Berndt
 */
public class Metadata extends AbstractXmlSubtree {
    private final ArrayList<Author> authors = new ArrayList<>();                      // the list of authors
    private final ArrayList<Element> comments = new ArrayList<>();                    // the list of comments
    private final ArrayList<RelatedResource> relatedResources = new ArrayList<>();    // the related resources

    /**
     * this constructor instantiates the Metadata object from an existing xml source handed over as XOM Element
     * @param xml
     * @throws Exception
     */
    private Metadata(Element xml) throws Exception {
        this.parseData(xml);
    }

    /**
     * this constructor creates a new Metadata object from an author and/or comment
     * @param author an Author object or null
     * @param comment a String or null
     * @param relatedResources
     * @throws Exception
     */
    private Metadata(Author author, String comment, Collection<RelatedResource> relatedResources) throws Exception {
        Element metadata = new Element("metadata", Mpm.MPM_NAMESPACE);

        if (author != null)
            metadata.appendChild(author.getXml());

        if (comment != null) {
            Element com = new Element("comment", Mpm.MPM_NAMESPACE);
            com.appendChild(new Text(comment));
            metadata.appendChild(com);
        }

        if ((relatedResources != null) && !relatedResources.isEmpty()) {
            Element relatedResourcesElt = new Element("relatedResources");
            metadata.appendChild(relatedResourcesElt);
            for (RelatedResource resource : relatedResources)
                relatedResourcesElt.appendChild(resource.getXml());
        }

        this.parseData(metadata);
    }

    /**
     * metadata factory
     * @param xml
     * @return
     */
    public static Metadata createMetadata(Element xml) {
        Metadata metadata;
        try {
            metadata = new Metadata(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return metadata;
    }

    /**
     * this factory generates a Metadata object from a comment
     * @param author
     * @return
     */
    public static Metadata createMetadata(Author author) {
        Metadata metadata;
        try {
            metadata = new Metadata(author, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return metadata;
    }

    /**
     * this factory generates a Metadata object from an author
     * @param comment
     * @return
     */
    public static Metadata createMetadata(String comment) {
        Metadata metadata;
        try {
            metadata = new Metadata(null, comment, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return metadata;
    }

    /**
     * this factory generates a Metadata object from a collection of related resources
     * @param relatedResources
     * @return
     */
    public static Metadata createMetadata(Collection<RelatedResource> relatedResources) {
        Metadata metadata;
        try {
            metadata = new Metadata(null, null, relatedResources);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return metadata;
    }

    /**
     * this factory generates a Metadata object from an author and/or comment
     * @param author
     * @param comment
     * @param relatedResources
     * @return
     */
    public static Metadata createMetadata(Author author, String comment, Collection<RelatedResource> relatedResources) {
        Metadata metadata;
        try {
            metadata = new Metadata(author, comment, relatedResources);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return metadata;
    }

    /**
     * parse the xml and set the class variables
     * @param xml
     * @throws Exception
     */
    @Override
    protected void parseData(Element xml) throws Exception {
        if (xml == null)
            throw new Exception("Cannot generate Metadata object. XML Element is null.");

        this.setXml(xml);

        // parse the child elements of metadata
        Elements children = this.getXml().getChildElements();
        for (int i = 0; i < children.size(); ++i) {
            Element child = children.get(i);
            switch (child.getLocalName()) {
                case "author":
                    Author author = Author.createAuthor(child);
                    if (author != null)
                        this.authors.add(author);
                    break;
                case "comment":
                    this.comments.add(child);
                    break;
                case "relatedResources":
                    LinkedList<Element> resources = Helper.getAllChildElements("resource", child);
                    for (Element resource : resources) {
                        RelatedResource r = RelatedResource.createRelatedResource(resource);
                        if (r != null)
                            this.relatedResources.add(r);
                    }
                default:
                    break;
            }
        }

        if (((this.authors.size() + this.comments.size()) == 0) && this.relatedResources.isEmpty())
            throw new Exception("Cannot generate empty Metadata object. It must contain at least one author, comment or related resource.");
    }

    /**
     * add an author to the metadata
     * @param author
     * @return the index at which it has been added
     */
    public int addAuthor(Author author) {
        this.getXml().appendChild(author.getXml());
        this.authors.add(author);
        return this.authors.size() - 1;
    }

    /**
     * access the list of authors
     * @return
     */
    public ArrayList<Author> getAuthors() {
        return this.authors;
    }

    /**
     * get the author at the specified index
     * @param index
     * @return the author or null if the author list is shorter
     */
    public Author getAuthor(int index) {
        if (this.authors.size() <= index)
            return null;

        return this.authors.get(index);
    }

    /**
     * find all authors with the specified name (there can be more than one)
     * @param name
     * @return
     */
    public ArrayList<Author> getAuthor(String name) {
        ArrayList<Author> auts = new ArrayList<>();
        for (Author aut : this.authors) {
            if (aut.getName().equals(name))
                auts.add(aut);
        }
        return auts;
    }

    /**
     * remove all authors with the specified name from the metadata
     * @param name
     */
    public void removeAuthor(String name) {
        ArrayList<Author> auts = this.getAuthor(name);  // find all authors with the specified name (there can be more than one)
        for (Author aut : auts) {                       // remove them
            aut.getXml().detach();
            auts.remove(aut);
        }
    }

    /**
     * add a comment to the metadata
     * @param comment
     * @return the index at which it has been added
     */
    public int addComment(String comment) {
        if (comment == null)
            comment = "";

        Element c = new Element("comment", Mpm.MPM_NAMESPACE);
        c.appendChild(comment);
        this.getXml().appendChild(c);
        this.comments.add(c);
        return this.comments.size() - 1;
    }

    /**
     * access the comments
     * @return
     */
    public ArrayList<Element> getComments() {
        return this.comments;
    }

    /**
     * read the comment at the specified index
     * @param index
     * @return
     */
    public String getComment(int index) {
        return this.comments.get(index).getValue();
    }

    /**
     * remove the comment at index i
     * @param index the index of the comment object
     */
    public void removeComment(int index) {
        Element comment = this.comments.get(index);
        comment.detach();
        this.getXml().removeChild(comment);
        this.comments.remove(index);
    }

    /**
     * add a related resource to the metadata
     * @param relatedResource
     * @return the index where the resource is added
     */
    public int addRelatedResource(RelatedResource relatedResource) {
        Element relatedResourcesElt = Helper.getFirstChildElement("relatedResources", this.getXml());
        if (relatedResourcesElt == null) {
            relatedResourcesElt = new Element("relatedResources", Mpm.MPM_NAMESPACE);
            this.getXml().appendChild(relatedResourcesElt);
        }

        relatedResourcesElt.appendChild(relatedResource.getXml());
        this.relatedResources.add(relatedResource);
        return this.relatedResources.size() - 1;
    }

    /**
     * access the list of related resources
     * @return
     */
    public ArrayList<RelatedResource> getRelatedResources() {
        return this.relatedResources;
    }

    /**
     * access a specific related resource via its index
     * @param index
     * @return
     */
    public RelatedResource getRelatedResource(int index) {
        if (this.relatedResources.size() <= index)
            return null;

        return this.relatedResources.get(index);
    }

    /**
     * remove a related resource
     * @param index
     */
    public void removeRelatedResource(int index) {
        RelatedResource relatedResource = this.relatedResources.get(index);
        this.removeRelatedResource(relatedResource);
    }

    /**
     * remove a related resource
     * @param relatedResource
     */
    public void removeRelatedResource(RelatedResource relatedResource) {
        if (relatedResource == null)
            return;

        Element relatedResourcesElt = Helper.getFirstChildElement("relatedResources", this.getXml());
        if (relatedResourcesElt == null)
            return;

        relatedResource.getXml().detach();
        relatedResourcesElt.removeChild(relatedResource.getXml());
        this.relatedResources.remove(relatedResource);

        // it is not allowed to have an empty relatedResources list, so make sure it is deleted when empty
        if (relatedResourcesElt.getChildCount() == 0) {
            relatedResourcesElt.detach();
            this.getXml().removeChild(relatedResourcesElt);
        }
    }
}
