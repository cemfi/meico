package meico.mei;

import meico.mpm.elements.Performance;
import meico.msm.elements.MsmNoteElement;
import nu.xom.Element;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * The ConversionContext interfaces all needed access for Processors to perform their conversions.
 */
public interface ConversionContext {
    Element getCurrentPart();
    Performance getCurrentPerformance();
    HashMap<String, Element> getAllNotesAndChords();

    ArrayList<Object> computeControlEventTiming(Element event, Element msmPartContext);
    MsmNoteElement meiNote2MsmNote(MeiElementHelper meiNote);
}
