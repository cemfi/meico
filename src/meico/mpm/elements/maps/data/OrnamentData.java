package meico.mpm.elements.maps.data;

import meico.mpm.elements.styles.OrnamentationStyle;
import meico.mpm.elements.styles.defs.OrnamentDef;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * this class is used to collect all relevant data to compute ornamentation
 * @author Axel Berndt
 */
public class OrnamentData {
    public Element xml = null;
    public String xmlId = null;

    public String styleName = "";
    public OrnamentationStyle style = null;
    public String ornamentDefName = null;
    public OrnamentDef ornamentDef = null;

    public double date = 0.0;                       // the date for which the data is assembled
    public double scale = 1.0;
    public ArrayList<String> noteOrder = null;

    /**
     * default constructor
     */
    public OrnamentData() {}

    /**
     * constructor from XML element parsing
     * @param xml MPM ornament element
     */
    public OrnamentData(Element xml) {
        this.xml = xml;

        this.date = Double.parseDouble(xml.getAttribute("date").getValue());
        this.ornamentDefName = xml.getAttribute("name.ref").getValue();

        Attribute scale = xml.getAttribute("scale");
        if (scale != null)
            this.scale = Double.parseDouble(scale.getValue());

        Attribute noteOrder = xml.getAttribute("note.order");
        if (noteOrder != null) {
            String no = noteOrder.getValue().trim();
            this.noteOrder = new ArrayList<>();
            if (no.equals("ascending pitch") || no.equals("descending pitch"))
                this.noteOrder.add(no);
            else
                this.noteOrder.addAll(Arrays.asList(no.replaceAll("#", "").split("\\s+")));
        }

        Attribute id = xml.getAttribute("id", "http://www.w3.org/XML/1998/namespace");
        if (id != null)
            this.xmlId = id.getValue();
    }

    /**
     * create a copy of this object
     * @return
     */
    @Override
    public OrnamentData clone() {
        OrnamentData clone = new OrnamentData();
        clone.xml = (this.xml == null) ? null : this.xml.copy();
        clone.xmlId = this.xmlId;
        clone.styleName = this.styleName;
        clone.style = this.style;
        clone.ornamentDefName = this.ornamentDefName;
        clone.ornamentDef = this.ornamentDef;
        clone.date = this.date;
        clone.scale = this.scale;
        if (this.noteOrder != null) {
            clone.noteOrder = new ArrayList<>();
            clone.noteOrder.addAll(this.noteOrder);
        }
        return clone;
    }
}
