package meico.mpm.elements.maps.data;

import meico.mpm.elements.styles.MetricalAccentuationStyle;
import meico.mpm.elements.styles.defs.AccentuationPatternDef;
import nu.xom.Element;

/**
 * this class is used to collect all relevant data to compute articulation
 * @author Axel Berndt
 */
public class MetricalAccentuationData {
    public Element xml = null;
    public String xmlId = null;

    public String styleName = "";
    public MetricalAccentuationStyle style = null;

    public String accentuationPatternDefName = null;
    public AccentuationPatternDef accentuationPatternDef = null;

    public double startDate = 0.0;
    public Double endDate = null;
    public double scale = 1.0;
    public boolean loop = false;
    public boolean stickToMeasures = true;

    /**
     * create a copy of this object
     * @return
     */
    @Override
    public MetricalAccentuationData clone() {
        MetricalAccentuationData clone = new MetricalAccentuationData();
        clone.xml = (this.xml == null) ? null : this.xml.copy();
        clone.xmlId = this.xmlId;
        clone.styleName = this.styleName;
        clone.style = this.style;
        clone.startDate = this.startDate;
        clone.endDate = this.endDate;
        clone.accentuationPatternDefName = this.accentuationPatternDefName;
        clone.accentuationPatternDef = this.accentuationPatternDef;
        clone.scale = this.scale;
        clone.loop = this.loop;
        clone.stickToMeasures = this.stickToMeasures;
        return clone;
    }
}
