package meico.midi.legacy;

import meico.midi.legacy.Event;

import java.util.List;

/**
 * Created by aberndt on 08.02.2016.
 *
 * This class shall represent a midi track and hold a sequence of midi events
 */
@Deprecated
public class TrackOld {
    private short port;         // the output device
    public List<Event> seq;     // the sequence of midi events

    /**
     * constructor
     * @param port
     */
    public TrackOld(short port) {
        this.port = port;
    }

    /**
     * add an event to the sequence, ensure the timely order of all events in the sequence
     * @param e
     */
    public void add(Event e) {
        int i = 0;
        for (; (i < this.seq.size()) && (e.getDate().compareTo(this.seq.get(i).getDate()) < 1); ++i)  // search for the last event before or at the date of e
            ;
        this.seq.add(i, e);     // append e at position i
    }

    /**
     * remove the first occurrene of the element in the sequence
     * @param e
     */
    public void remove(Event e) {
        this.seq.remove(e);
    }

    /**
     * this method removes all events of the specified type from sequence
     * @param type
     */
    public void clearFromType(short type) {
        for (int i = 0; i < this.seq.size(); ++i)
            while (this.seq.get(i).getType() == type)
                this.seq.remove(i);
    }

    /**
     * This method finds an event in the sequence and returns its index or -1 if not found
     * @param e
     * @return index of searched element or -1 if not found
     */
    public int findEvent(Event e) {
        for (int i = 0; i < this.seq.size(); ++i)
            if (this.seq.get(i).equals(e))
                return i;
        return -1;
    }

    /**
     * A setter to edit the output port of this track.
     * @param port
     */
    public void setPort(short port) {
        this.port = port;
    }

    public short getPort() {
        return this.port;
    }
}
