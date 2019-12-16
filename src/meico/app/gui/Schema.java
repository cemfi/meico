package meico.app.gui;

import java.io.File;

/**
 * This represents XML schemas in the meico GUI.
 * @author Axel Berndt
 */
class Schema {
    private File file;                              // the schema file

    /**
     * constructor
     * @param schema
     */
    public Schema(File schema) {
        this.file = schema;
    }

    /**
     * a getter for the file
     * @return
     */
    public File getFile() {
        return this.file;
    }
}
