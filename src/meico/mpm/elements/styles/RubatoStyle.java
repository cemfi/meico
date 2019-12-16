package meico.mpm.elements.styles;

import com.sun.media.sound.InvalidDataException;
import meico.mei.Helper;
import meico.mpm.elements.styles.defs.RubatoDef;
import nu.xom.Element;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class interfaces rubato style definitions.
 * Basically, these define a string and
 * and assotiate it with an instance of RubatoDef.
 * @author Axel Berndt
 */
public class RubatoStyle extends GenericStyle {
    private HashMap<String, RubatoDef> rubatoDefs;             // the lookup table for rubatoDefs (name -> RubatoDef)

    /**
     * this constructor generates an empty styleDef for rubatoDefs to be added subsequently
     * @param name
     * @throws InvalidDataException
     */
    private RubatoStyle(String name) throws InvalidDataException {
        super(name);
    }

    /**
     * this constructor generates the object from xml input
     * @param xml
     * @throws InvalidDataException
     */
    private RubatoStyle(Element xml) throws InvalidDataException {
        super(xml);
    }

    /**
     * RubatoStyle factory
     * @param name
     * @return
     */
    public static RubatoStyle createRubatoStyle(String name) {
        RubatoStyle rubatoStyle;
        try {
            rubatoStyle = new RubatoStyle(name);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return rubatoStyle;
    }

    /**
     * RubatoStyle factory
     * @param xml
     * @return
     */
    public static RubatoStyle createRubatoStyle(Element xml) {
        RubatoStyle rubatoStyle;
        try {
            rubatoStyle = new RubatoStyle(xml);
        } catch (InvalidDataException e) {
            e.printStackTrace();
            return null;
        }
        return rubatoStyle;
    }

    /**
     * set the data of this object, this parses the xml element and generates the according data structure
     * @param xml
     */
    protected void parseData(Element xml) throws InvalidDataException {
        super.parseData(xml);

        this.rubatoDefs = new HashMap<>();

        // parse the rubatoDef elements (the children of this styleDef)
        LinkedList<Element> rubatoDefs = Helper.getAllChildElements("rubatoDef", this.getXml());
        for (Element def : rubatoDefs) {                       // for each rubatoDef
            RubatoDef rd = RubatoDef.createRubatoDef(def);
            if (rd == null)
                continue;
            this.rubatoDefs.put(rd.getName(), rd);                           // add the (name, RubatoDef) pair to the lookup table
        }
    }

    /**
     * access the whole HashMap with (name, RubatoDef) pairs
     * @return
     */
    public HashMap<String, RubatoDef> getAllRubatoDefs() {
        return this.rubatoDefs;
    }

    /**
     * retrieve a specific rubatoDef
     * @param name
     * @return
     */
    public RubatoDef getRubatoDef(String name) {
        return this.rubatoDefs.get(name);
    }

    /**
     * add or (if a rubatoDef with this name is already existent) replace the rubatoDef
     * @param rubatoDef the RubatoDef instance to be added, if there is already one with this name, it is replaced
     */
    public void addRubatoDef(RubatoDef rubatoDef) {
        if (rubatoDef == null) {
            System.err.println("Cannot add a null RubatoDef to the styleDef.");
            return;
        }
        removeRubatoDef(rubatoDef.getName());               // if there is already a rubatorDef with this name, remove it
        this.rubatoDefs.put(rubatoDef.getName(), rubatoDef);
        this.getXml().appendChild(rubatoDef.getXml());
    }

    /**
     * remove the specified rubatoDef from this styleDef
     * @param name
     */
    public void removeRubatoDef(String name) {
        RubatoDef rd = this.rubatoDefs.get(name);        // get the xml element of this rubatoDef
        if (rd == null)                                 // if there is no such rubatoDef
            return;                                     // done

        this.rubatoDefs.remove(name);                    // remove the (name, values) lookup table entry
        this.getXml().removeChild(rd.getXml());         // remove the element from the xml
    }

    /**
     * get the number of rubatoDefs in this styleDef
     * @return
     */
    public int size() {
        return this.rubatoDefs.size();
    }

    /**
     * does the styleDef contain rubatoDefs?
     * @return
     */
    public boolean isEmpty() {
        return this.rubatoDefs.isEmpty();
    }
}
