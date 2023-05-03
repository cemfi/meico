package meico.mei;

import meico.musicxml.MusicXml;
import nu.xom.Document;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class does the conversion from MEI to MusicXML.
 * To use it, instantiate it with the constructor, then invoke convert().
 * See method meico.mei.Mei.exportMusicXml() for some sample code.
 * @author Axel Berndt
 */
public class Mei2MusicXmlConverter {
    private Mei mei = null;                         // the MEI to be converted
    private boolean ignoreExpansions = false;       // set this true to have a 1:1 conversion of MEI to MSM without the rearrangement that MEI's expansion elements produce

    /**
     * constructor
     * @param ignoreExpansions set this true to have a 1:1 conversion of MEI to MSM without the rearrangement that MEI's expansion elements produce
     */
    public Mei2MusicXmlConverter(boolean ignoreExpansions) {
        this.ignoreExpansions = ignoreExpansions;
    }

    public List<MusicXml> convert(Mei mei) {
        if (mei == null) {
            System.out.println("\nThe provided MEI object is null and cannot be converted.");
            return new ArrayList<>();                                           // return empty lists
        }

        long startTime = System.currentTimeMillis();                            // we measure the time that the conversion consumes
        System.out.println("\nConverting " + ((mei.getFile() != null) ? mei.getFile().getName() : "MEI data") + " to MusicXML.");

        this.mei = mei;

        Document orig = this.mei.getDocument().copy();                          // the document will be altered during conversion, thus we keep the original to restore it after the process

        LinkedList<MusicXml> out = new LinkedList<>();

        // TODO ...

        // cleanup
        this.mei.setDocument(orig);                                             // restore the unaltered version of the mei data

        System.out.println("MEI to MusicXML conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return out;
    }
}
