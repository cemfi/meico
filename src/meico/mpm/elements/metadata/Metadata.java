package meico.mpm.elements.metadata;

import com.sun.media.sound.InvalidDataException;
import meico.mpm.Mpm;
import meico.xml.AbstractXmlSubtree;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Text;

import java.util.ArrayList;

/**
 * This class interfaces the metadata of the MPM instance.
 * @author Axel Berndt
 */
public class Metadata extends AbstractXmlSubtree {
    private ArrayList<Author> authors = new ArrayList<>();     // the list of authors
    private ArrayList<Element> comments = new ArrayList<>();    // the list of comments

    /**
     * this constructor instantiates the Metadata object from an existing xml source handed over as XOM Element
     * @param xml
     */
    private Metadata(Element xml) throws InvalidDataException {
        this.parseData(xml);
    }

    /**
     * this constructor creates a new Metadata object from an author and/or comment
     * @param author an Author object or null
     * @param comment a String or null
     */
    private Metadata(Author author, String comment) throws InvalidDataException {
        Element metadata = new Element("metadata", Mpm.MPM_NAMESPACE);

        if (author != null)
            metadata.appendChild(author.getXml());

        if (comment != null) {
            Element com = new Element("comment", Mpm.MPM_NAMESPACE);
            com.appendChild(new Text(comment));
            metadata.appendChild(com);
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
        } catch (InvalidDataException e) {
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
            metadata = new Metadata(author, null);
        } catch (InvalidDataException e) {
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
            metadata = new Metadata(null, comment);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return metadata;
    }

    /**
     * this factory generates a Metadata object from an author and/or comment
     * @param author
     * @param comment
     * @return
     */
    public static Metadata createMetadata(Author author, String comment) {
        Metadata metadata;
        try {
            metadata = new Metadata(author, comment);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return metadata;
    }

    /**
     * parse the xml and set the class variables
     * @param xml
     * @throws InvalidDataException
     */
    @Override
    protected void parseData(Element xml) throws InvalidDataException {
        if (xml == null)
            throw new InvalidDataException("Cannot generate Metadata object. XML Element is null.");

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
                default:
                    break;
            }
        }

        if ((this.authors.size() + this.comments.size()) == 0)
            throw new InvalidDataException("Cannot generate Metadata object. It must contain at least one author or comment");
    }

    /**
     * add an author to the metadata
     * @param author
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
        this.comments.remove(index);
    }
}
