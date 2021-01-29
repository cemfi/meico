package meico.mpm.elements.maps.data;

import meico.mpm.elements.styles.RubatoStyle;
import meico.mpm.elements.styles.defs.RubatoDef;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * this class is used to collect all relevant data to compute articulation
 * @author Axel Berndt
 */
public class RubatoData {
    public Element xml = null;
    public String xmlId = null;

    public String styleName = "";
    public RubatoStyle style = null;
    public String rubatoDefString = null;
    public RubatoDef rubatoDef = null;

    public double startDate = 0.0;
    public Double endDate = null;

    public Double frameLength = null;
    public Double intensity = 1.0;
    public Double lateStart = 0.0;
    public Double earlyEnd = 1.0;

    public boolean loop = false;

    /**
     * default constructor
     */
    public RubatoData() {}

    /**
     * constructor from XML element parsing
     * @param xml MPM rubato element
     */
    public RubatoData(Element xml) {
        this.xml = xml;
        this.startDate = Double.parseDouble(xml.getAttributeValue("date"));

        Attribute nameRef = xml.getAttribute("name.ref");
        if (nameRef != null)
            this.rubatoDefString = nameRef.getValue();

        Attribute frameLength = xml.getAttribute("frameLength");
        this.frameLength = (frameLength != null) ? Double.parseDouble(frameLength.getValue()) : null;

        Attribute intensity = xml.getAttribute("intensity");
        this.intensity = (intensity != null) ? Double.parseDouble(intensity.getValue()) : null;

        Attribute lateStart = xml.getAttribute("lateStart");
        this.lateStart = (lateStart != null) ? Double.parseDouble(lateStart.getValue()) : null;

        Attribute earlyEnd = xml.getAttribute("earlyEnd");
        this.earlyEnd = (earlyEnd != null) ? Double.parseDouble(earlyEnd.getValue()) : null;

        Attribute loop = xml.getAttribute("loop");
        if (loop != null)
            this.loop = Boolean.parseBoolean(loop.getValue());

        Attribute id = xml.getAttribute("id", "http://www.w3.org/XML/1998/namespace");
        if (id != null)
            this.xmlId = id.getValue();
    }

    /**
     * create a copy of this object
     * @return
     */
    @Override
    public RubatoData clone() {
        RubatoData clone = new RubatoData();
        clone.xml = (this.xml == null) ? null : this.xml.copy();
        clone.xmlId = this.xmlId;
        clone.styleName = this.styleName;
        clone.style = this.style;
        clone.startDate = this.startDate;
        clone.endDate = this.endDate;
        clone.rubatoDefString = this.rubatoDefString;
        clone.rubatoDef = this.rubatoDef;
        clone.frameLength = this.frameLength;
        clone.intensity = this.intensity;
        clone.lateStart = this.lateStart;
        clone.earlyEnd = this.earlyEnd;
        clone.loop = this.loop;
        return clone;
    }
}
