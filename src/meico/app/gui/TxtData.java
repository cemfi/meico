package meico.app.gui;

import meico.mei.Helper;

import java.io.*;
import java.nio.file.Files;

/**
 * This is a wrapper around Javas String datatype and represents a .txt file, only for the purpose of integration with the meico GUI, i.e. its visualization as a data object.
 * @author Axel Berndt
 */
public class TxtData {
    private File file;                  // the file
    private String string;    // the character string

    /**
     * constructor
     */
    public TxtData(String string, File file) {
        this.file = file;
        this.string = string;
    }

    /**
     * constructor
     * @param file
     * @throws IOException
     */
    public TxtData(File file) throws IOException {
        this.file = file;
        this.string = new String(Files.readAllBytes(file.toPath()));
    }

    /**
     * a getter for the file
     * @return
     */
    public File getFile() {
        return this.file;
    }

    /**
     * write string to file
     * @return
     */
    public boolean writeTxtData() {
        return Helper.writeStringToFile(this.string, this.file.getAbsolutePath());
    }

    /**
     * another file writer with a specific filename
     * @param filename
     * @return
     */
    public boolean writeTxtData(String filename) {
        return Helper.writeStringToFile(this.string, filename);
    }

    /**
     * a getter for the character string
     * @return
     */
    public String getString() {
        return this.string;
    }
}
