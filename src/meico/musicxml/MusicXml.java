package meico.musicxml;

import meico.mei.Helper;
import meico.mei.Mei;
import nu.xom.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by aberndt on 10.03.2017.
 */
public class MusicXml {
    private File file;
    private Document musicXml;                                    // the musicXml document
    private boolean musicXmlValidation = false;                   // indicates whether the input file contained valid musicXml code (true) or not (false); it is also false if no validation has been performed

    /**
     * constructor
     */
    public MusicXml() {
        this.file = null;
        this.musicXml = null;                                     // empty document
        this.musicXmlValidation = false;
    }

    /**
     * constructor
     *
     * @param musicXml the musicXml document of which to instantiate the MusicXml object
     */
    public MusicXml(Document musicXml) {
        this.file = null;
        this.musicXml = musicXml;
        this.musicXmlValidation = false;
    }

    /**
     * constructor
     *
     * @param file the musicXml file to be read
     */
    public MusicXml(File file) throws IOException, ParsingException {
        this.readMusicXmlFile(file, false);
    }

    /**
     * read an musicXml file
     * @param file
     * @param validation
     */
    protected void readMusicXmlFile(File file, boolean validation) throws IOException, ParsingException {
        this.file = file;

        if (!file.exists()) {
            this.musicXml = null;
            this.musicXmlValidation = false;
            System.out.println("No such file or directory: " + file.getPath());
            return;
        }

        // read file into the mei instance of Document
        Builder builder = new Builder(validation);                  // if the validate argument in the Builder constructor is true, the musicXml should be valid
        this.musicXmlValidation = true;                             // the musicXml code is valid until validation fails (ValidityException)
        try {
            this.musicXml = builder.build(file);
        } catch (ValidityException e) {                             // in case of a ValidityException (no valid musicXml code)
            this.musicXmlValidation = false;                        // set musicXmlValidation false to indicate that the musicXml code is not valid
            e.printStackTrace();                                    // output exception message
            for (int i = 0; i < e.getErrorCount(); i++) {           // output all validity error descriptions
                System.out.println(e.getValidityError(i));
            }
            this.musicXml = e.getDocument();                        // make the XOM Document anyway, we may nonetheless be able to work with it
        }
    }

    /**
     * if the musicXml document is empty return false, else true
     *
     * @return false if the musicXml document is empty, else true
     */
    public boolean isValid() {
        return (this.musicXmlValidation);
    }

    /**
     * if the constructor was unable to load the file, the musicXml document is empty and no further operations
     *
     * @return true if the musicXml document is empty, else false
     */
    public boolean isEmpty() {
        return (this.musicXml == null);
    }

    /**
     * @return the musicXml document
     */
    public Document getDocument() {
        return this.musicXml;
    }

    /**
     * a setter for the document
     * @param musicXmlDocument
     */
    public void setDocument(Document musicXmlDocument) {
        this.musicXml = musicXmlDocument;
    }

    /**
     * @return the root element of the musicXml
     */
    public Element getRootElement() {
        if (this.isEmpty())
            return null;
        return this.musicXml.getRootElement();
    }

    /**
     * this getter returns the file
     *
     * @return a java File object (this file does not necessarily have to exist in the file system, but may be created there when writing the file with writeMusicXml())
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
     * writes the musicXml document to an musicxml file at this.file (it must be != null);
     * if there is already an musicXml file with this name, it is replaces!
     *
     * @return true if success, false if an error occured
     */
    public boolean writeMusicXml() {
        if (this.file == null) {
            System.out.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        if (this.isEmpty()) {
            System.out.println("Empty document, cannot write file.");
            return false;
        }

        return this.writeMusicXml(this.file.getPath());
    }

    /**
     * writes the musicXml document to a file (filename should include the path and the extension .musicXml)
     *
     * @param filename the filename string; it should include the path and the extension .musicXml
     * @return true if success, false if an error occured
     */
    public boolean writeMusicXml(String filename) {
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
            serializer.write(this.musicXml);                        // write data from musicXml to file
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

    /**
     * convert MusicXML to MEI
     * @return the Mei instance
     */
    public Mei exportMei() {
        Document stylesheet = null;
        try {
            stylesheet = (new Builder()).build(this.getClass().getResourceAsStream("/resources/xslt/musicxml2mei-3.0.xsl"));    //read the XSLT stylesheet musicxml2mei-3.0.xsl
        }
        catch (ParsingException ex) {
            System.err.println("Well-formedness error in " + ex.getURI() + ".");
        }
        catch (IOException ex) {
            System.err.println("I/O error while reading input document or stylesheet.");
        }

        Document result = Helper.xslTransformToDocument(this.musicXml, stylesheet);         // do the transform (result can be null!)
        Mei mei = new Mei(result);                                                          // create Mei instance from result Document (if result==null then mei.isEmpty()==true)
        mei.setFile(Helper.getFilenameWithoutExtension(this.getFile().getPath()) + ".mei"); // replace the file extension mei with musicXml and make this the filename
        return mei;                                                                         // return the Mei instance

    }
}
