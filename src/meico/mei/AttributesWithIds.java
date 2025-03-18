package meico.mei;

import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a helper class for method Mei.resolveCopyofs(). It collects all
 * attributes in the specified subtree and sorts them into HashMaps depending
 * on whether they are IDs, copyof/sameas or other references.
 */
public class AttributesWithIds {
    private final Element root;
    private HashMap<Attribute, String> copyofs = new HashMap<>();
    private HashMap<String, Attribute> ids = new HashMap<>();
    private HashMap<String, ArrayList<Attribute>> references = new HashMap<>();

    /**
     * constructor
     * @param root
     */
    public AttributesWithIds(Element root) {
        assert root != null;
        this.root = root;

        this.root.query("descendant-or-self::*").forEach(node -> {
            Element element = (Element) node;
            for (int a = 0; a < element.getAttributeCount(); ++a) {
                Attribute attribute = element.getAttribute(a);

                if ((attribute.getType() == Attribute.Type.ID) || (attribute.getLocalName().equals("id"))) {
                    this.ids.put(attribute.getValue(), attribute);
                    continue;
                }
                if (attribute.getLocalName().equals("copyof") || attribute.getLocalName().equals("sameas")) {
                    this.copyofs.put(attribute, attribute.getValue().substring(1)); // get the ID string without the #
                    continue;
                }
                if (attribute.getValue().startsWith("#")) {
                    String id = attribute.getValue().substring(1);
                    ArrayList<Attribute> attList = this.references.computeIfAbsent(id, k -> new ArrayList<>());
                    attList.add(attribute);
                }
            }
        });
//        System.out.println("copyof set size " + this.copyofs.size());
//        System.out.println("id set size " + this.ids.size());
//        System.out.println("reference set size " + this.references.size());
    }

    /**
     * getter for the copyofs HashMap
     * @return
     */
    public HashMap<Attribute, String> getCopyofs() {
        return this.copyofs;
    }

    public void updateCopyofs() {
        this.copyofs = new HashMap<>();
        this.root.query("descendant-or-self::*").forEach(node -> {
            Element element = (Element) node;
            for (int a = 0; a < element.getAttributeCount(); ++a) {
                Attribute attribute = element.getAttribute(a);

                if (attribute.getLocalName().equals("copyof") || attribute.getLocalName().equals("sameas")) {
                    this.copyofs.put(attribute, attribute.getValue().substring(1)); // get the ID string without the #
                    continue;
                }
            }
        });
    }

    /**
     * find element by ID
     * @param id
     * @return
     */
    public Element getElementById(String id) {
        Attribute attribute = this.ids.get(id);
        return (attribute == null) ? null : (Element) attribute.getParent();
    }
}
