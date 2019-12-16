package meico.mpm.elements.maps.data;

import meico.mpm.elements.styles.RubatoStyle;
import meico.mpm.elements.styles.defs.RubatoDef;
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
    public double intensity = 1.0;
    public Double lateStart = 0.0;
    public Double earlyEnd = 1.0;

    public boolean loop = false;

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
