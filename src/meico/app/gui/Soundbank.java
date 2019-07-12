package meico.app.gui;

import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;

import java.io.File;

/**
 * This represents soundbanks/soundfonts in the meico GUI.
 * It is no replacement of javax.sound.midi.Soundbank which meico's MIDIPlayer uses for MIDI synthesis!
 * @author Axel Berndt
 */
class Soundbank {
    private DataObject graphicalInstance;
    private File file;                      // the audio file
    private boolean isActive = false;       // will be set true when meico's MIDI synthesis uses it

    /**
     * constructor
     * @param file
     */
    public Soundbank(File file, DataObject graphiccalInstance) {
        this.graphicalInstance = graphiccalInstance;
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
     * returns true as long as it is used for MIDI synthesis
     * @return
     */
    protected boolean isActive() {
        return this.isActive;
    }

    /**
     * triggers the usage of this for MIDI synthesis
     */
    protected synchronized void activate() {
        this.isActive = this.graphicalInstance.getWorkspace().getApp().getPlayer().setSoundbank(this.file);
        if (isActive) {
            StackPane p = (StackPane) this.graphicalInstance.getChildren().get(this.graphicalInstance.getChildren().size() - 1);    // make the graphical representation light up
            Glow glow = new Glow(0.8);
            p.setEffect(glow);
        }
    }

    /**
     * when another soundbank is used, this one should to be deactivated
     */
    protected synchronized void deactivate() {
        this.graphicalInstance.getWorkspace().getApp().getPlayer().setSoundbank(Settings.soundbank);
        this.silentDeactivation();
    }

    /**
     * switch flags and graphical layout to "off" but do not force the midiplayer to load the default soundbank
     */
    protected synchronized void silentDeactivation() {
        this.isActive = false;
        StackPane p = (StackPane) this.graphicalInstance.getChildren().get(this.graphicalInstance.getChildren().size() - 1);    // switch the light off
        p.setEffect(null);
    }
}
