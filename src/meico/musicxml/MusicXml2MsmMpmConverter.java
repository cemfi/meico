package meico.musicxml;

import meico.Meico;
import meico.mei.Helper;
import meico.midi.InstrumentsDictionary;
import meico.mpm.Mpm;
import meico.mpm.elements.Part;
import meico.mpm.elements.Performance;
import meico.mpm.elements.metadata.Author;
import meico.mpm.elements.metadata.Comment;
import meico.mpm.elements.metadata.RelatedResource;
import meico.msm.Msm;
import meico.supplementary.KeyValue;
import nu.xom.Attribute;
import nu.xom.Element;
import org.audiveris.proxymusic.*;

import java.io.IOException;
import java.lang.String;
import java.util.ArrayList;
import java.util.UUID;

/**
 * This class does the conversion from MusicXML to MSM and MPM
 * To use it, instantiate it with the constructor, then invoke convert().
 * @author Axel Berndt
 */
public class MusicXml2MsmMpmConverter {
    private int ppq;                        // pulses per quarter time resolution
    private boolean cleanup;                // set true to return a clean msm file or false to keep all the crap from the conversion
    private MusicXml musicXml = null;
    private Msm msm = null;
    private Mpm mpm = null;
    private Performance performance = null;

    /**
     * constructor
     * @param ppq pulses per quarter time resolution
     * @param cleanup set true to return a clean msm file or false to keep all the crap from the conversion
     */
    public MusicXml2MsmMpmConverter(int ppq, boolean cleanup) {
        this.ppq = ppq;
        this.cleanup = cleanup;
    }

    /**
     * start the conversion process
     * @param musicXml
     * @return a pair of Msm and Mpm instances; these can be null if an error occurs
     */
    public KeyValue<Msm, Mpm> convert(MusicXml musicXml) {
        if (musicXml == null) {
            System.out.println("\nThe provided MusicXML object is null and cannot be converted.");
            return new KeyValue<>(null, null);                                                      // return empty pair
        }

        switch (musicXml.getType()) {
            case scorePartwise:
                break;
            case scoreTimewise:
                break;
            case opus:
                System.out.println("MusicXML Opus type cannot be converted.");
                return new KeyValue<>(null, null);                                                  // return empty pair
            case unknown:
            default:
                System.out.println("Unknown MusicXML type. Cannot be converted.");
                return new KeyValue<>(null, null);                                                  // return empty pair
        }

        long startTime = System.currentTimeMillis();                                                // we measure the time that the conversion consumes
        System.out.println("\nConverting " + ((musicXml.getFile() != null) ? musicXml.getFile().getName() : "MusicXML data") + " to MSM and MPM.");

        this.musicXml = musicXml;

        // initialize the Msm and Mpm instances
        String title = this.musicXml.getTitle();
        String id = "meico_" + UUID.randomUUID().toString();
        this.msm = Msm.createMsm(title, id, this.ppq);
        if (this.msm.isEmpty()) {                                                                   // if something went wrong stop the process
            System.err.println("Failed to initialize and instance of Msm.");
            return new KeyValue<>(null, null);
        }
        this.mpm = Mpm.createMpm();
        if (this.musicXml.getFile() != null) {
            String filename = Helper.getFilenameWithoutExtension(this.musicXml.getFile().getPath());
            this.msm.setFile(filename + ".msm");
            this.mpm.setFile(filename + ".mpm");
            ArrayList<RelatedResource> relatedResources = new ArrayList<>();
            relatedResources.add(RelatedResource.createRelatedResource(this.musicXml.getFile().getAbsolutePath(), "musicxml"));
            Comment comment = Comment.createComment("This MPM has been generated from '" + this.musicXml.getFile().getName() + "' using the meico MEI converter v" + Meico.version + ".", null);
            this.mpm.addMetadata(Author.createAuthor("meico", null, null), comment, relatedResources);
        } else {
            Comment comment = Comment.createComment("This MPM has been generated from MEI code using the meico MEI converter v" + Meico.version + ".", null);
            this.mpm.addMetadata(Author.createAuthor("meico", null, null), comment, null);
        }

        this.performance = Performance.createPerformance("MusicXML export performance", this.ppq);  // generate a Performance object
        if (this.performance == null)                                                               // check it is null
            System.err.println("Failed to generate an instance of Performance.");
        this.mpm.addPerformance(this.performance);                                                  // add the performance to the mpm

        // convert
        this.processPartList();                                                                     // convert the MusicXML part-list to MSM and MPM parts

        // TODO: ...

        // cleanup
        if (this.cleanup)
            Helper.msmCleanup(this.msm);                                                            // cleanup of the msm objects to remove all conversion related and no longer needed entries in the msm objects

        System.out.println("MusicXML to MSM and MPM conversion finished. Time consumed: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        return new KeyValue<>(this.msm, this.mpm);
    }

    /**
     * convert the MusicXML part-list to MSM and MPM parts
     */
    private void processPartList() {
        PartList partList = this.musicXml.getPartList();
        if (partList == null)
            return;

        String groupName = "";
        int number = 0;                                                             // default initial value
        int midiChannel = 0;                                                        // default initial value
        int midiPort = 0;                                                           // default initial value
        for (Object entry : partList.getPartGroupOrScorePart()) {
            // keep the part-group elements so we can add their names to the part names
            if (entry instanceof PartGroup) {
                PartGroup partGroup = (PartGroup) entry;
                switch (partGroup.getType()) {
                    case START:
                        if (partGroup.getGroupName() != null)
                            groupName = partGroup.getGroupName().getValue();
                        break;
                    case STOP:
                        groupName = "";
                        break;
                }
                continue;
            }

            // ignore all elements except ScoreParts
            if (!(entry instanceof ScorePart))
                continue;

            // convert MusicXml score-part to MSM and MPM part
            ScorePart scorePart = (ScorePart) entry;
            String id = scorePart.getId();                                          // required attribute
            String name = groupName + " " + scorePart.getPartName().getValue();     // required child element

            boolean foundPort = false;
            boolean foundChannel = false;
            boolean foundProgramChange = false;
            int midiInstrNum = 0;
            if (!scorePart.getMidiDeviceAndMidiInstrument().isEmpty()) {
                for (Object m : scorePart.getMidiDeviceAndMidiInstrument()) {
                    if (foundPort && foundChannel && foundProgramChange)            // if all information was found
                        break;                                                      // stop the loop

                    if ((!foundPort) && (m instanceof MidiDevice) && (((MidiDevice) m).getPort() != null)) {
                        midiPort = ((MidiDevice) m).getPort() - 1;
                        foundPort = true;
                        continue;
                    }

                    if (m instanceof MidiInstrument) {
                        MidiInstrument midiInstrument = (MidiInstrument) m;
                        if ((!foundChannel) && (midiInstrument.getMidiChannel() != null)) {
                            midiChannel = midiInstrument.getMidiChannel() - 1;
                            foundChannel = true;
                        }
                        if ((!foundProgramChange) && (midiInstrument.getMidiProgram() != null)) {
                            midiInstrNum = midiInstrument.getMidiProgram() - 1;
                            foundProgramChange = true;
                        }
                        continue;
                    }
                }
            }

            // create the MSM part and add it to the MSM
            Element part = Msm.makePart(name, number, midiChannel, midiPort);
            Attribute partId = new Attribute("id", id);
            partId.setNamespace("xml", "http://www.w3.org/XML/1998/namespace");
            part.addAttribute(partId);
            this.msm.addPart(part);

            // create the MPM part and add it to the performance
            this.performance.addPart(Part.createPart(name, number, midiChannel, midiPort, id));

            // if no program change number could be found so far, and we have a score-instrument element in the MusicXML, try to get a program change number from its name via the InstrumentsDictionary
            if ((!foundProgramChange) && (!scorePart.getScoreInstrument().isEmpty())) {
                ScoreInstrument scoreInstrument = scorePart.getScoreInstrument().get(0);
                try {
                    midiInstrNum = (new InstrumentsDictionary()).getProgramChange(scoreInstrument.getInstrumentName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                foundProgramChange = true;
            }

            // if we have a program change number, we can generate a programChangeMap in the MSM part
            if (foundProgramChange) {
                Element programChange = new Element("programChange");                                           // create a programChange element
                programChange.addAttribute(new Attribute("date", "0.0"));                                       // set its date
                programChange.addAttribute(new Attribute("value", Integer.toString(midiInstrNum)));             // set its value
                Element programChangeMap = new Element("programChangeMap");                                     // create a programChangeMap
                part.getFirstChildElement("dated").appendChild(programChangeMap);                               // add it to the part's dated environment
                programChangeMap.appendChild(programChange);                                                    // add the programChange element to the programChangeMap
            }

            number++;
            if (++midiChannel >= 16) {
                midiChannel = 0;
                midiPort++;
            }
        }
    }
}
