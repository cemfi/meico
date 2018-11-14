package meico.app.gui;

import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;

import java.io.File;

/**
 * This represents XML schemas in the meico GUI.
 * @author Axel Berndt
 */
public class Schema {
    private DataObject graphicalInstance;
    private File file;                              // the schema file
    private boolean isActive = false;               // will be set true when it is activated for validation

    /**
     * constructor
     * @param schema
     * @param graphiccalInstance
     */
    public Schema(File schema, DataObject graphiccalInstance) {
        this.graphicalInstance = graphiccalInstance;
        this.file = schema;
    }

    /**
     * a getter for the file
     * @return
     */
    public File getFile() {
        return this.file;
    }

    /**
     * returns true as long as it is activated
     * @return
     */
    protected boolean isActive() {
        return this.isActive;
    }

    /**
     * triggers the usage of this schema
     */
    protected synchronized void activate() {
        this.isActive = true;
        StackPane p = (StackPane) this.graphicalInstance.getChildren().get(this.graphicalInstance.getChildren().size() - 1);    // make the graphical representation light up
        Glow glow = new Glow(0.8);
        p.setEffect(glow);
    }

    /**
     * when another schema is activated, this one should to be deactivated
     */
    protected synchronized void deactivate() {
        this.isActive = false;
        StackPane p = (StackPane) this.graphicalInstance.getChildren().get(this.graphicalInstance.getChildren().size() - 1);    // switch the light off
        p.setEffect(null);
    }
}
