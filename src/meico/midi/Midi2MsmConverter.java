package meico.midi;

import meico.mei.Helper;
import meico.msm.Msm;
import nu.xom.Attribute;
import nu.xom.Element;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This class does the MIDI to MSM conversion.
 * To use it, instantiate it with the constructor, then invoke convert().
 * See method meico.midi.Midi.exportMsm() for some sample code.
 * @author Axel Berndt.
 */

public class Midi2MsmConverter {
    private int midiFileFormat;
    private int channel = 0;                                            // this indicates the current midi channel that subsequent (meta) events are sent to
    private int port = 0;                                               // this represents the current midi port that subsequent events are sent to
    private Sequence sequence;
    private Track[] tracks;
    private Msm msm;
    private Element global;
    private Element currentPart;
    private String trackname = "";
    private HashMap<String, Element> parts = new HashMap<>();           // the int array should indicate "port,channel"
    private boolean useSharpsInsteadOfFlats = true;                     // this is needed for encoding accidentals, it is set according to the type of accidentals that the key signature uses
    private ArrayList<Element> pendingNotes = new ArrayList<>();        // this collects noteOn events until the corresponding noteOff is found
    private boolean useDefaultInstrumentNames;                          // set this false if a non GM compliant instruments dictionary is used

    /**
     * constructor
     * @param midiFileFormat
     * @param sequence
     * @param msm a minimal Msm instance to be filled with data, this will be the result
     */
    public Midi2MsmConverter(int midiFileFormat, boolean useDefaultInstrumentNames, Sequence sequence, Msm msm) {
        this.midiFileFormat = midiFileFormat;
        this.useDefaultInstrumentNames = useDefaultInstrumentNames;

        this.sequence = Midi.cloneSequence(sequence);           // make a working copy of the midi sequence
        if (this.sequence == null)                              // if failed to make a working copy
            this.sequence = sequence;                           // do the work with the original sequence (it may be altered and stay altered afterwards, hence, better the cloning works)

        System.out.print("Converting noteOn (with velocity 0) to noteOff events: ");
        System.out.println(Midi.noteOns2NoteOffs(this.sequence) + " events converted.");  // convert noteOns with velocity 0 to real noteOffs, this standardization is to prepare further processing

        this.tracks = this.sequence.getTracks();                // get the individual tracks from the sequence

        this.msm = msm;
        this.global = msm.getRootElement().getFirstChildElement("global");
        this.currentPart = this.global;                         // as far as no channel prefix or ShortEvent (with channel parameter) occurs, all generated msm elements go into global maps
    }

    /**
     * call this method to do the midi to msm conversion, the global msm will hold the result
     */
    public void convert() {
//        System.out.println(Midi.print(this.sequence));

        // parse the tracks, make MSM parts of it
        for (int t=0; t < this.tracks.length; ++t) {                                                    // go through all tracks
            Track track = this.tracks[t];                                                               // get the current track
            this.currentPart = this.global;

            // parse the track and make MSM markup from each midi event
            for (int e=0; e < track.size(); ++e) {                                                      // for all the events in the track
                MidiEvent event = track.get(e);                                                         // get the current event
                if (this.processShortEvent(event))                                                      // try processing it as short event
                    continue;
                if (this.processMetaEvent(event))                                                       // try processing it as meta event
                    continue;
                if (this.processSysexEvent(event))                                                      // try processing it as sysex event
                    continue;
                System.err.println("Unknown MIDI message: " + event.getMessage().getClass() + " at timecode " + event.getTick() + "."); // I have no idea what kind of event/message this could be
            }

            // close pending noteOns
            double endDate = (double) track.get(track.size() - 1).getTick();                            // get the date of the last event in this track (usually the EndOfTrack meta event)
            for (Element note : this.pendingNotes) {                                                    // for all pending notes
                double dur = endDate - Double.parseDouble(note.getAttributeValue("midi.date"));         // compute its duration so that it ends at the end of the track
                note.getAttribute("midi.duration").setValue(Double.toString(dur));                      // set its duration
            }
            this.pendingNotes.clear();
            this.trackname = "";
        }

        // add all parts to the msm object
        for (Map.Entry<String, Element> entry : this.parts.entrySet())
            msm.addPart(entry.getValue());
    }

    /**
     * this converts midi meta events (midi event with a MetaMessage) to msm
     * @param event the event to be converted
     * @return true if the event is a meta event, otherwise false as it cannot be converted by this method
     */
    private boolean processMetaEvent(MidiEvent event) {
        if (!(event.getMessage() instanceof MetaMessage))
            return false;

        MetaMessage m = (MetaMessage) event.getMessage();
//        System.out.println(m.getType());

        switch (m.getType()) {
            case EventMaker.META_Sequence_Number:
                break;

            case EventMaker.META_Text_Event:
                break;

            case EventMaker.META_Copyright_Notice:
                break;

            case EventMaker.META_Track_Name: {                                                                  // these events can occur before we are in a specific channel/part
                this.trackname += ((this.trackname.isEmpty()) ? "" : " - ") + (new String(m.getData())).trim(); // store the track name in a global variable for later reference
                if (this.currentPart != this.global) {                                                          // if we are already local
                    Attribute nameAtt = this.currentPart.getAttribute("name");
                    String name = nameAtt.getValue();
                    nameAtt.setValue(this.trackname + ((name.isEmpty()) ? "" : (": " + name)));                 // append the track name at the beginning of the part's name string plus a separator ":" to separate track name and channel name (if there is a channel name to separate it from)
                }
                break;
            }
            case EventMaker.META_Instrument_Name: {
                if (this.currentPart != this.global) {                                                          // it makes no sense to give the global environment a track or instrument name
                    String name;
                    String instName = (new String(m.getData())).trim();
                    Attribute nameAtt = this.currentPart.getAttribute("name");
                    String namePart = nameAtt.getValue();
                    if (namePart.isEmpty())
                        name = instName;
                    else if (namePart.equals(this.trackname))
                        name = this.trackname + instName;
                    else
                        name = namePart + " - " + instName;

                    nameAtt.setValue(name);  // set the part's name to the channel name, keep the track name at the beginning if there is one
                }
                break;
            }

            case EventMaker.META_Lyric:
                break;

            case EventMaker.META_Marker: {
                Element marker = new Element("marker");
                marker.addAttribute(new Attribute("midi.date", Double.toString((double)event.getTick())));      // get the date of the event
                marker.addAttribute(new Attribute("message", new String(m.getData())));                         // get its text
                Element markerMap = this.currentPart.getFirstChildElement("dated").getFirstChildElement("markerMap");
                Helper.addToMap(marker, markerMap);                                                             // add marker to markerMap
                break;
            }

            case EventMaker.META_Cue_Point:
                break;

            case EventMaker.META_Program_Name:
                break;

            case EventMaker.META_Device_Name:
                break;

            case EventMaker.META_Midi_Channel_Prefix: {                                                         // all meta messages that follow go to this channel
                this.channel = (short) m.getData()[0];
                String index = this.port + "," + this.channel;
                if (!this.parts.containsKey(index))
                    this.parts.put(index, makePart(this.trackname, this.port, this.channel));                   // TODO: if port and channel are switched subsequently this can cause the creation of an inbetween part, that part will remain empty and should be deleted during cleanup!
                this.currentPart = this.parts.get(index);
                break;
            }

            case EventMaker.META_Midi_Port: {                                                                   // all messages that follow go to this port
                this.port = (short) m.getData()[0];
                String index = this.port + "," + this.channel;
                if (!this.parts.containsKey(index))
                    this.parts.put(index, makePart(this.trackname, this.port, this.channel));                   // TODO: if port and channel are switched subsequently this can cause the creation of an inbetween part, that part will remain empty and should be deleted during cleanup!
                this.currentPart = this.parts.get(index);
                break;
            }

            case EventMaker.META_End_of_Track:
                break;

            case EventMaker.META_Set_Tempo:                                                                     // tempo is not part of the MSM specification, hence, it is ignored
                break;

            case EventMaker.META_SMTPE_Offset:
                break;

            case EventMaker.META_Time_Signature: {                                                              // decoding time signature messages is well documented at http://somascape.org/midi/tech/mfile.html
                Element ts = new Element("timeSignature");                                                      // create an element
                ts.addAttribute(new Attribute("midi.date", Double.toString((double)event.getTick())));          // get the date
                ts.addAttribute(new Attribute("numerator", Double.toString((double)m.getData()[0])));           // store numerator
                ts.addAttribute(new Attribute("denominator", Integer.toString((int)(Math.pow(2, (int)m.getData()[1])))));   // store denominator
                Element tsMap = this.currentPart.getFirstChildElement("dated").getFirstChildElement("timeSignatureMap");
                Helper.addToMap(ts, tsMap);                                                                     // add timeSignature to timeSignaturerMap
                break;
            }

            case EventMaker.META_Key_Signature: {                                                               // decode key signature and make an msm representation of it
                Element ks = new Element("keySignature");                                                       // create an element
                ks.addAttribute(new Attribute("midi.date", Double.toString((double) event.getTick())));         // compute date
                LinkedList<Element> accidentals = new LinkedList<>();                                           // create an empty list which will be filled with the accidentals of this key signature
                int accidCount = (int) m.getData()[0];                                                          // this variable holds how many accidentals

                this.useSharpsInsteadOfFlats = accidCount > 0;                                                  // later for encoding pitchnames and accidentals this indicator is used to decide which type of accidentals should be used; the key signature uses flats, hence, these are used as accidentals as well (and vice versa); this may not be the best solution as there can be sharps in a flat key signature, but for the moment it serves our purpose

                // generate msm accidentals and add them to the accidentals list
                String[] acs = (accidCount > 0) ? new String[]{"5.0", "0.0", "7.0", "2.0", "9.0", "4.0", "11.0"} : new String[]{"11.0", "4.0", "9.0", "2.0", "7.0", "0.0", "5.0"};  // the sequence of (midi) pitches to apply the accidentals
                String[] acsn = (accidCount > 0) ? new String[]{"F", "C", "G", "D", "A", "E", "B"} : new String[]{"B", "E", "A", "D", "G", "C", "F"};                               // the sequence of pitches to apply the accidentals
                for (int i = 0; i < Math.abs(accidCount); ++i) {                                                // create the accidentals
                    Element accidental = new Element("accidental");                                             // create an accidental element for the msm keySignature
                    accidental.addAttribute(new Attribute("midi.pitch", acs[i]));                               // add the pitch attribute that says which pitch class is affected by the accidental
                    accidental.addAttribute(new Attribute("pitchname", acsn[i]));                               // also store the pitch name, this is easier to read in the msm
                    accidental.addAttribute(new Attribute("value", (accidCount > 0) ? "1.0" : "-1.0"));         // add the decimal value of the accidental as attribute (1=sharp, -1=flat)
                    accidentals.add(accidental);                                                                // add it to the accidentals list
                }

                // add all generated accidentals as children to the msm keySignature element
                for (Element accidental : accidentals)                                                          // for each accidentals
                    ks.appendChild(accidental);                                                                 // add it to the msm keySignature

                // add the key signature to the key signature map
                Element ksMap = this.currentPart.getFirstChildElement("dated").getFirstChildElement("keySignatureMap");
                Helper.addToMap(ks, ksMap);                                                                     // add timeSignature to timeSignaturerMap
                break;
            }

            case EventMaker.META_Sequence_specific_Meta_event:
                break;

            default:
                break;
        }
        return true;
    }

    /**
     * this converts midi short events (midi event with a ShortMesage) to msm
     * @param event the event to be converted
     * @return true if the event is a short event, otherwise false as it cannot be converted by this method
     */
    private boolean processShortEvent(MidiEvent event) {
        if (!(event.getMessage() instanceof ShortMessage))
            return false;

        ShortMessage m = (ShortMessage) event.getMessage();
//        System.out.println(m.getCommand());

        // These messages have an explicit channel parameter, thus they do not necessarily go to this.currentPart. Instead, we have to check whether their part exists already and create it if not.
        // TODO: check if the part exists
        int chan = m.getChannel();
        String index = this.port + "," + chan;
        if (!this.parts.containsKey(index))
            this.parts.put(index, makePart(this.trackname, this.port, chan));                                   // TODO: if port and channel are switched subsequently this can cause the creation of an inbetween part, that part will remain empty and should be deleted during cleanup!
        Element part = this.parts.get(index);

        switch(m.getCommand()) {
            case EventMaker.NOTE_OFF: {
                double pitch = (double) m.getData1();
                for (int i = 0; i < this.pendingNotes.size(); ++i) {                                            // search the pendingNotes list for the first note with the same pitch
                    Element note = this.pendingNotes.get(i);
                    double p = Double.parseDouble(note.getAttributeValue("midi.pitch"));
                    if (pitch == p) {
                        double dur = ((double) event.getTick()) - Double.parseDouble(note.getAttributeValue("midi.date"));
                        note.getAttribute("midi.duration").setValue(Double.toString(dur));
                        this.pendingNotes.remove(i);
                        break;
                    }
                }
                break;
            }
            case EventMaker.NOTE_ON: {
                double pitch = (double) m.getData1();
                Element note = new Element("note");
                String[] pnameAccid = {"", ""};                                                                 // convert midi pitch value to pitchname and accidental strings
                Helper.midi2PnameAndAccid(this.useSharpsInsteadOfFlats, pitch, pnameAccid);
                note.addAttribute(new Attribute("midi.date", Double.toString((double) event.getTick())));
                note.addAttribute(new Attribute("midi.pitch", Double.toString(pitch)));
//                note.addAttribute(new Attribute("midi.velocity", Double.toString((double) m.getData2())));    // not represented in msm
                note.addAttribute(new Attribute("pitchname", pnameAccid[0]));
                note.addAttribute(new Attribute("accidentals", pnameAccid[1]));
                note.addAttribute(new Attribute("midi.duration", ""));                                          // to be  added once the corresponding noteOff is found
                Helper.addToMap(note, part.getFirstChildElement("dated").getFirstChildElement("score"));
                this.pendingNotes.add(note);
                break;
            }
            case EventMaker.POLY_AFTERTOUCH:
                break;
            case EventMaker.CONTROL_CHANGE:
                break;
            case EventMaker.PROGRAM_CHANGE: {
                Attribute nameAtt = part.getAttribute("name");
                String instName = InstrumentsDictionary.getInstrumentName((short) m.getData1(), this.useDefaultInstrumentNames);    // generate an instrument name from the program change number using the instruments dictionary
                if (nameAtt.getValue().equals(this.trackname)) {                                                // if the part has no name or just the track name
                    nameAtt.setValue(((this.trackname.isEmpty()) ? "" : (this.trackname + ": ")) + instName);   // set the name, keeping the track name at the beginning if there is one
                } else {                                                                                        // if there is already more than the track name
                    nameAtt.setValue(nameAtt.getValue() + " - " + instName);                                    // append the instrument name after a separator "-"
                }
                break;
            }
            case EventMaker.CHANNEL_AFTERTOUCH:
                break;
            case EventMaker.PITCH_BEND:
                break;
            case EventMaker.SYSEX_START:
                break;
            case EventMaker.MIDI_TIME_CODE:
                break;
            case EventMaker.SONG_POSITION_POINTER:
                break;
            case EventMaker.SONG_SELECT:
                break;
            case EventMaker.UNDEF1:
                break;
            case EventMaker.UNDEF2:
                break;
            case EventMaker.TUNE_REQUEST:
                break;
            case EventMaker.SYSEX_END:
                break;
            case EventMaker.TIMING_CLOCK:
                break;
            case EventMaker.UNDEF3:
                break;
            case EventMaker.START:
                break;
            case EventMaker.CONTINUE:
                break;
            case EventMaker.STOP:
                break;
            case EventMaker.UNDEF4:
                break;
            case EventMaker.ACTIVE_SENSING:
                break;
            case EventMaker.SYSTEM_RESET:       // or META_EVENT
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * this converts midi sysex events (midi event with a SysexMesage) to msm;
     * actually, this method does nothing as there is no msm representation of sysex messages right now
     * @param event the event to be converted
     * @return true if the event is a sysex event, otherwise false as it cannot be converted by this method
     */
    private boolean processSysexEvent(MidiEvent event) {
        if (!(event.getMessage() instanceof SysexMessage))
            return false;

        return true;
    }

    /**
     * this is a shortcut for creating an msm part
     * @param partName
     * @param port
     * @param channel
     * @return
     */
    private Element makePart(String partName, int port, int channel) {
        return Msm.makePart(partName, "", channel, port);
    }
}
