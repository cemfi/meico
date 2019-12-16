package meico.mpm.elements.styles;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.elements.styles.defs.ArticulationDef;
import nu.xom.Element;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class interfaces articulation style definitions.
 * Basically, these define an articulation name string (such as "legato", "portato", "agogic accent")
 * and assotiate it with an instance of ArticulationDef.
 * @author Axel Berndt
 */
public class ArticulationStyle extends GenericStyle {
    private HashMap<String, ArticulationDef> articulationDefs;     // the lookup table for tempoDefs (name -> ArticulationDef)

    /**
     * this constructor generates an empty styleDef for dynamicsDefs to be added subsequently
     * @param name
     * @throws InvalidDataException
     */
    private ArticulationStyle(String name) throws InvalidDataException {
        super(name);
    }

    /**
     * this constructor generates the object from xml input
     * @param xml
     * @throws InvalidDataException
     */
    private ArticulationStyle(Element xml) throws InvalidDataException {
        super(xml);
    }

    /**
     * ArticulationStyle factory
     * @param name
     * @return
     */
    public static ArticulationStyle createArticulationStyle(String name) {
        ArticulationStyle articulationStyle;
        try {
            articulationStyle = new ArticulationStyle(name);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return articulationStyle;
    }

    /**
     * ArticulationStyle factory
     * @param xml
     * @return
     */
    public static ArticulationStyle createArticulationStyle(Element xml) {
        ArticulationStyle articulationStyle;
        try {
            articulationStyle = new ArticulationStyle(xml);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return articulationStyle;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws InvalidDataException {
        super.parseData(xml);

        this.articulationDefs = new HashMap<>();

        // parse the articulationDef elements (the children of this styleDef)
        LinkedList<Element> articDefs = Helper.getAllChildElements("articulationDef", this.getXml());
        for (Element articDef : articDefs) {                                // for each articulationDef
            ArticulationDef ad = ArticulationDef.createArticulationDef(articDef);
            if (ad == null)
                continue;
            this.articulationDefs.put(ad.getName(), ad);                             // add the (name, ArticulationDef) pair to the lookup table
        }
    }

    /**
     * access the whole HashMap with (name, ArticulationDef) pairs
     * @return
     */
    public HashMap<String, ArticulationDef> getAllArticulationDefs() {
        return this.articulationDefs;
    }

    /**
     * retrieve a specific articulationDef
     * @param name
     * @return
     */
    public ArticulationDef getArticulationDef(String name) {
        return this.articulationDefs.get(name);
    }

    /**
     * add or (if a articulationDef with this name is already existent) replace the articulationDef
     * @param articulationDef the ArticulationDef instance to be added, if there is already one with this name, it is replaced
     */
    public void addArticulationDef(ArticulationDef articulationDef) {
        if (articulationDef == null) {
            System.err.println("Cannot add a null ArticulationDef to the styleDef.");
            return;
        }
        removeArticulationDef(articulationDef.getName());               // if there is already a articulationDef with this name, remove it
        this.articulationDefs.put(articulationDef.getName(), articulationDef);
        this.getXml().appendChild(articulationDef.getXml());
    }

    /**
     * remove the specified articulationDef from this styleDef
     * @param name
     */
    public void removeArticulationDef(String name) {
        ArticulationDef ad = this.articulationDefs.get(name);    // get the xml element of this articulationDef
        if (ad == null)                                         // if there is no such articulationDef
            return;                                             // done

        this.articulationDefs.remove(name);                      // remove the (name, values) lookup table entry
        this.getXml().removeChild(ad.getXml());                 // remove the element from the xml
    }

    /**
     * get the number of articulationDefs in this styleDef
     * @return
     */
    public int size() {
        return this.articulationDefs.size();
    }

    /**
     * does the styleDef contain articulationDefs?
     * @return
     */
    public boolean isEmpty() {
        return this.articulationDefs.isEmpty();
    }
}
