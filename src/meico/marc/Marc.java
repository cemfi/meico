package meico.marc;

import nu.xom.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by aberndt on 13.03.2017.
 */
public class Marc {
    private File file;
    private Document marc;
    private boolean marcValidation = false;

    /**
     * constructor
     */
    public Marc() {
        this.file = null;
        this.marc = null;                                     // empty document
        this.marcValidation = false;
    }

    /**
     * constructor
     *
     * @param marc the marc document of which to instantiate the marc object
     */
    public Marc(Document marc) {
        this.file = null;
        this.marc = marc;
        this.marcValidation = false;
    }

    /**
     * constructor
     *
     * @param file the marc file to be read
     */
    public Marc(File file) throws IOException, ParsingException {
        this.readMarcFile(file, false);
    }

    /**
     * read an marc file
     * @param file
     * @param validation
     */
    protected void readMarcFile(File file, boolean validation) throws IOException, ParsingException {
        this.file = file;

        if (!file.exists()) {
            System.out.println("No such file or directory: " + file.getPath());
            this.marc = null;
            this.marcValidation = false;
            return;
        }

        // read file into the mei instance of Document
        Builder builder = new Builder(validation);                  // if the validate argument in the Builder constructor is true, the marc should be valid
        this.marcValidation = true;                                 // the marc code is valid until validation fails (ValidityException)
        try {
            this.marc = builder.build(file);
        } catch (ValidityException e) {                             // in case of a ValidityException (no valid marc code)
            this.marcValidation = false;                            // set marcValidation false to indicate that the marc code is not valid
            e.printStackTrace();                                    // output exception message
            for (int i = 0; i < e.getErrorCount(); i++) {           // output all validity error descriptions
                System.out.println(e.getValidityError(i));
            }
            this.marc = e.getDocument();                            // make the XOM Document anyway, we may nonetheless be able to work with it
        }
    }

    /**
     * if the marc document is empty return false, else true
     *
     * @return false if the marc document is empty, else true
     */
    public boolean isValid() {
        return (this.marcValidation);
    }

    /**
     * if the constructor was unable to load the file, the marc document is empty and no further operations
     *
     * @return true if the marc document is empty, else false
     */
    public boolean isEmpty() {
        return (this.marc == null);
    }

    /**
     * @return the marc document
     */
    public Document getDocument() {
        return this.marc;
    }

    /**
     * a setter for the document
     * @param marcDocument
     */
    public void setDocument(Document marcDocument) {
        this.marc = marcDocument;
    }

    /**
     * @return the root element of the marc
     */
    public Element getRootElement() {
        if (this.isEmpty())
            return null;
        return this.marc.getRootElement();
    }

    /**
     * this getter returns the file
     *
     * @return a java File object (this file does not necessarily have to exist in the file system, but may be created there when writing the file with writeMarc())
     */
    public File getFile() {
        return this.file;
    }

    /**
     * with this setter a new filename can be set
     *
     * @param filename the filename including the full path and .musicXml extension
     */
    public void setFile(String filename) {
        this.file = new File(filename);
    }

    /**
     * writes the marc document to an marc file at this.file (it must be != null);
     * if there is already an musicXml file with this name, it is replaces!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeMarc() {
        if (this.file == null) {
            System.out.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        if (this.isEmpty()) {
            System.out.println("Empty document, cannot write file.");
            return false;
        }

        return this.writeMarc(this.file.getPath());
    }

    /**
     * writes the marc document to a file (filename should include the path and the extension .marc)
     *
     * @param filename the filename string; it should include the path and the extension .marc
     * @return true if success, false if an error occured
     */
    public boolean writeMarc(String filename) {
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
            serializer.write(this.marc);                        // write data from musicXml to file
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
