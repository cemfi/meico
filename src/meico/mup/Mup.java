package meico.mup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by aberndt on 13.03.2017.
 */
public class Mup {
    private File file;
    private String mup;

    /**
     * constructor
     */
    public Mup() {
        this.file = null;
        this.mup = null;                                     // empty document
    }

    /**
     * constructor
     *
     * @param mup the MUP code
     */
    public Mup(String mup) {
        this.file = null;
        this.mup = mup;
    }

    /**
     * constructor
     *
     * @param file the mup file to be read
     */
    public Mup(File file) throws IOException {
        this.file = file;
        this.mup =
        this.readMupFile(file.getCanonicalPath(), StandardCharsets.UTF_8);
    }

    /**
     * read the MUP file into a String
     * @param path
     * @param encoding
     * @return
     * @throws IOException
     */
    static String readMupFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * if the constructor was unable to load the file, the mup is empty and no further operations
     *
     * @return true if the mup is empty, else false
     */
    public boolean isEmpty() {
        return ((this.mup == null) || (this.mup.isEmpty()));
    }

    /**
     * @return the mup code string
     */
    public String getDocument() {
        return this.mup;
    }

    /**
     * a setter for the musp document
     * @param mupDocument
     */
    public void setDocument(String mupDocument) {
        this.mup = mupDocument;
    }

    /**
     * this getter returns the file
     *
     * @return a java File object (this file does not necessarily have to exist in the file system, but may be created there when writing the file with writeMods())
     */
    public File getFile() {
        return this.file;
    }

    /**
     * with this setter a new filename can be set
     *
     * @param filename the filename including the full path and .mup extension
     */
    public void setFile(String filename) {
        this.file = new File(filename);
    }

    /**
     * writes the mup document to an mods file at this.file (it must be != null);
     * if there is already an mup file with this name, it is replaces!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeMup() {
        if (this.file == null) {
            System.out.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        if (this.isEmpty()) {
            System.out.println("Empty document, cannot write file.");
            return false;
        }

        return this.writeMup(this.file.getPath());
    }

    /**
     * writes the mup document to a file (filename should include the path and the extension .mup)
     *
     * @param filename the filename string; it should include the path and the extension .mup
     * @return true if success, false if an error occured
     */
    public boolean writeMup(String filename) {
        if (this.isEmpty()) {
            System.out.println("Empty document, cannot write file.");
            return false;
        }

        // create the file in the file system
        File file = new File(filename);
        file.getParentFile().mkdirs();                              // ensure that the directory exists
        try {
            file.createNewFile();                                   // create the file if it does not already exist
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        boolean returnValue = true;
        try(  PrintWriter out = new PrintWriter(filename)  ){
            out.println(this.mup);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            returnValue = false;
        }

        if (this.file == null)
            this.file = file;

        return returnValue;
    }
}
