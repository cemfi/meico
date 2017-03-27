package meico.msm;

/**
 * Created by aberndt on 09.11.2016.
 */

import nu.xom.Attribute;
import nu.xom.Element;

/**
 * this class is used to represent goto elements from msm sequencingMaps, used in methods Msm.applySequencingMapToMap() and Mei.processEnding()
 */
public class Goto {
    public double date = 0.0;               // the midi.date attribute
    public double targetDate = 0.0;         // the target.date attribute
    public String targetId = "";            // the target.id attribute
    public Element source = null;           // the source element in the msm document
    public String activity = "1";           // this indicates when the goto is processed and when it is ignored
    public int counter = 0;                 // this counter is used to keep track of how often the goto is passed (typically a repetition is ignored at the second time)

    /**
     * constructor
     * @param date
     * @param targetDate
     * @param targetId
     * @param source
     * @param activity
     */
    public Goto(double date, double targetDate, String targetId, String activity, Element source) {
        this.date = date;
        this.targetDate = targetDate;
        if (targetId.startsWith("#"))
            targetId = targetId.substring(1, targetId.length()-1);
        this.targetId = targetId;
        this.source = source;
        this.activity = activity;
    }

    /**
     * creates and returns an XML element of the goto
     * @return
     */
    public Element toElement() {
        Element gt = new Element("goto");                                           // make a goto element
        gt.addAttribute(new Attribute("midi.date", Double.toString(date)));         // give it the date
        gt.addAttribute(new Attribute("activity", activity));                       // process this goto at the second time, later on ignore it
        gt.addAttribute(new Attribute("target.date", Double.toString(targetDate))); // add the target.date attribute
        gt.addAttribute(new Attribute("target.id", "#" + targetId));                // add the target.id attribute
        return gt;
    }
}
