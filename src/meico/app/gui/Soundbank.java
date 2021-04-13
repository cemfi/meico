package meico.app.gui;

import java.io.File;

/**
 * This represents soundbanks/soundfonts in the meico GUI.
 * It is no replacement of javax.sound.midi.Soundbank which meico's MIDIPlayer uses for MIDI synthesis!
 * @author Axel Berndt
 */
class Soundbank {
    private DataObject graphicalInstance;
    private File file;                      // the audio file

    /**
     * constructor
     * @param file
     * @param graphicalInstance
     */
    public Soundbank(File file, DataObject graphicalInstance) {
        this.graphicalInstance = graphicalInstance;
        this.file = file;
    }

    /**
     * a getter for the file
     * @return
     */
    public File getFile() {
        return this.file;
    }

    /**
     * triggers the usage of this for MIDI synthesis
     */
    protected synchronized boolean activate() {
        return this.graphicalInstance.getWorkspace().getApp().getPlayer().setSoundbank(this.file);
    }

    /**
     * when another soundbank is used, this one should to be deactivated
     */
    protected synchronized void deactivate() {
        this.graphicalInstance.getWorkspace().getApp().getPlayer().setSoundbank(Settings.soundbank);
    }
}
