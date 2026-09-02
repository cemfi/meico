package meico.mei;

import nu.xom.Element;

/**
 * The NoteProcessor processes notes and chords within the current conversion.
 * Here, it interfaces the needed function to get notes and chords be processed.
 * Thereby, it gives other processors (e.g. OrnamentProcessor) the possibility to process notes/chords if needed.
 */
public interface NoteProcessor {
    void processNote(Element xmlElement);
    void processChord(Element xmlElement);
}
