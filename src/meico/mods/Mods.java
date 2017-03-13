package meico.mods;

import nu.xom.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by aberndt on 13.03.2017.
 */
public class Mods {
    private File file;
    private Document mods;
    private boolean modsValidation = false;

    /**
     * constructor
     */
    public Mods() {
        this.file = null;
        this.mods = null;                                     // empty document
        this.modsValidation = false;
    }

    /**
     * constructor
     *
     * @param mods the mods document of which to instantiate the mods object
     */
    public Mods(Document mods) {
        this.file = null;
        this.mods = mods;
        this.modsValidation = false;
    }

    /**
     * constructor
     *
     * @param file the mods file to be read
     */
    public Mods(File file) throws IOException, ParsingException {
        this.readModsFile(file, false);
    }

    /**
     * read an mods file
     * @param file
     * @param validation
     */
    protected void readModsFile(File file, boolean validation) throws IOException, ParsingException {
        this.file = file;

        if (!file.exists()) {
            System.out.println("No such file or directory: " + file.getPath());
            this.mods = null;
            this.modsValidation = false;
            return;
        }

        // read file into the mei instance of Document
        Builder builder = new Builder(validation);                  // if the validate argument in the Builder constructor is true, the mods should be valid
        this.modsValidation = true;                                 // the mods code is valid until validation fails (ValidityException)
        try {
            this.mods = builder.build(file);
        } catch (ValidityException e) {                             // in case of a ValidityException (no valid mods code)
            this.modsValidation = false;                            // set modsValidation false to indicate that the mods code is not valid
            e.printStackTrace();                                    // output exception message
            for (int i = 0; i < e.getErrorCount(); i++) {           // output all validity error descriptions
                System.out.println(e.getValidityError(i));
            }
            this.mods = e.getDocument();                            // make the XOM Document anyway, we may nonetheless be able to work with it
        }
    }

    /**
     * if the mods document is empty return false, else true
     *
     * @return false if the mods document is empty, else true
     */
    public boolean isValid() {
        return (this.modsValidation);
    }

    /**
     * if the constructor was unable to load the file, the mods document is empty and no further operations
     *
     * @return true if the mods document is empty, else false
     */
    public boolean isEmpty() {
        return (this.mods == null);
    }

    /**
     * @return the mods document
     */
    public Document getDocument() {
        return this.mods;
    }

    /**
     * a setter for the document
     * @param modsDocument
     */
    public void setDocument(Document modsDocument) {
        this.mods = modsDocument;
    }

    /**
     * @return the root element of the mods
     */
    public Element getRootElement() {
        if (this.isEmpty())
            return null;
        return this.mods.getRootElement();
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
     * @param filename the filename including the full path and .mods extension
     */
    public void setFile(String filename) {
        this.file = new File(filename);
    }

    /**
     * writes the mods document to an mods file at this.file (it must be != null);
     * if there is already an mods file with this name, it is replaces!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeMods() {
        if (this.file == null) {
            System.out.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        if (this.isEmpty()) {
            System.out.println("Empty document, cannot write file.");
            return false;
        }

        return this.writeMods(this.file.getPath());
    }

    /**
     * writes the mods document to a file (filename should include the path and the extension .mods)
     *
     * @param filename the filename string; it should include the path and the extension .mods
     * @return true if success, false if an error occured
     */
    public boolean writeMods(String filename) {
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

        // open the FileOutputStream to write to the file
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, false);   // open file: second parameter (append) is false because we want to overwrite the file if already existing
        } catch (FileNotFoundException | NullPointerException | SecurityException e) {
            e.printStackTrace();
            return false;
        }

        // serialize the xml code (encoding, layout) and write it to the file via the FileOutputStream
        boolean returnValue = true;
        Serializer serializer = null;
        try {
            serializer = new Serializer(fileOutputStream, "UTF-8"); // connect serializer with FileOutputStream and specify encoding
            serializer.setIndent(4);                                // specify indents in xml code
            serializer.write(this.mods);                        // write data from musicXml to file
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            returnValue = false;
        }

        // close the FileOutputStream
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            returnValue = false;
        }

        if (this.file == null)
            this.file = file;

        return returnValue;
    }
}
