package meico.msm;

/**
 * This is a helper class for processing MSM sequencingMaps.
 * @author Axel Berndt.
 */

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;

import java.security.InvalidParameterException;

/**
 * this class is used to represent goto elements from msm sequencingMaps, used in methods Msm.applySequencingMapToMap() and Mei.processEnding()
 */
public class Goto {
    public double date = 0.0;               // the midi.date attribute
    public double targetDate = 0.0;         // the target.date attribute
    public String targetId = "";            // the target.id attribute
    public Element target = null;
    public Element source = null;           // the source element in the msm document
    public String activity = "1";           // this indicates when the goto is processed and when it is ignored
    public int counter = 0;                 // this counter is used to keep track of how often the goto is passed (typically a repetition is ignored at the second time)

    /**
     * constructor, better use Goto(Element gt) as connstructor, it is safer and more convenient
     * @param date
     * @param targetDate
     * @param targetId
     * @param source
     * @param activity
     */
    public Goto(double date, double targetDate, String targetId, String activity, Element source) {
        this.date = date;
        this.source = source;
        this.activity = activity;
        this.targetDate = targetDate;

        if (targetId != null) {
            if (targetId.startsWith("#"))
                targetId = targetId.substring(1, targetId.length() - 1);
            this.targetId = targetId;
        }
    }

    /**
     * constructor
     * @param gt
     */
    public Goto(Element gt) throws InvalidParameterException {
        Attribute a = gt.getAttribute("midi.date");                                                 // get its midi.date attribute
        if (a == null)                                                                              // if it has none
            throw new InvalidParameterException("Missing attribute midi.date in " + gt.toXML());    // the Goto instance cannot be created
        this.date = Double.parseDouble(gt.getAttributeValue("midi.date"));                          // get the date as double

        // get its target.id and target element
        if ((gt.getAttribute("target.id") != null) && !gt.getAttributeValue("target.id").isEmpty()) {                   // if there is a nonempty attribute target.id
            this.targetId = gt.getAttributeValue("target.id").trim();                                                   // get target.id

            if (this.targetId.startsWith("#"))                                                                          // remove the # at the start
                this.targetId = this.targetId.substring(1, this.targetId.length());

            Nodes targetCandidates = gt.getParent().query("descendant::*[attribute::xml:id='" + this.targetId + "']");  // find target element (must be a sibling of gt)
            if (targetCandidates.size() > 0) this.target = (Element) targetCandidates.get(0);                           // get it
        }

        // determine target date
        a = gt.getAttribute("target.date");                                                                                 // get its target.date
        if (a == null) {                                                                                                    // if it has none
            if (this.target == null)                                                                                        // and there is no target specified otherwise
                throw new InvalidParameterException("Missing attribute target.date or a valid target.id in " + gt.toXML()); // the Goto instance cannot be created
            try {
                this.targetDate = Double.parseDouble(this.target.getAttributeValue("midi.date"));                           // get the date from the target
            } catch (Exception e) {                                                                                         // if it fails
                throw new InvalidParameterException("The target of " + gt.toXML() + " has no valid attribute midi.date.");  // the Goto instance cannot be created
            }
        }
        else {                                                                                                              // it has the target.date attribute
            this.targetDate = Double.parseDouble(gt.getAttributeValue("target.date"));                                      // get the date as double
        }

        this.activity = (gt.getAttribute("activity") == null) ? "1" : gt.getAttributeValue("activity"); // get the activity string
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

    /**
     * call this method when you come across the goto during the processing of sequencingMaps,
     * it will increase the counter and return whether it is active (true) or passive (false)
     * @return
     */
    public boolean isActive() {
        boolean active = false;

        if ((this.counter < this.activity.length()) && (this.activity.charAt(counter) == '1'))
            active = true;

        counter++;
        return active;
    }
}
