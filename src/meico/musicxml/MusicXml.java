package meico.musicxml;

import meico.mei.Helper;
import meico.xml.XmlBase;
import net.sf.saxon.s9api.Xslt30Transformer;
import nu.xom.*;
import org.audiveris.proxymusic.ObjectFactory;
import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.ScoreTimewise;
import org.audiveris.proxymusic.mxl.Mxl;
import org.audiveris.proxymusic.mxl.RootFile;
import org.audiveris.proxymusic.opus.Opus;
import org.audiveris.proxymusic.util.Marshalling;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.URL;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;

/**
 * This class represents a MusicXML document.
 * It relies heaily on the framework <a href="https://github.com/Audiveris/proxymusic">ProxyMusic</a>.
 * @author Axel Berndt
 */
public class MusicXml extends XmlBase {
    protected Object data = null;

    /**
     * constructor
     */
    public MusicXml() {
        ObjectFactory factory = new ObjectFactory();
        this.data = factory.createScorePartwise();
    }

    /**
     * constructor
     * @param document an instance of a XOM Document
     * @throws Marshalling.UnmarshallingException
     * @throws IOException
     */
    public MusicXml(Document document) throws Marshalling.UnmarshallingException, IOException {
        this(document.toXML());
    }

    /**
     * constructor; this constructor is private as the handling of compressed and uncompressed MusicXML files needs
     * different treatment that is implemented in the factory method fromFile()
     * @param file MusicXML file; for compressed MusicXML (.mxl) use the fromFile() factory!
     * @throws IOException
     * @throws Marshalling.UnmarshallingException
     */
    private MusicXml(File file) throws IOException, Marshalling.UnmarshallingException {
        this(Files.newInputStream(file.toPath()));
        this.setFile(file);
    }

    /**
     * constructor
     * @param xml MusicXML string
     * @throws Marshalling.UnmarshallingException
     * @throws IOException
     */
    public MusicXml(String xml) throws Marshalling.UnmarshallingException, IOException {
        this(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * constructor
     * @param inputStream this method will also close the inputStream
     * @throws Marshalling.UnmarshallingException
     * @throws IOException
     */
    public MusicXml(InputStream inputStream) throws Marshalling.UnmarshallingException, IOException {
        this.data = Marshalling.unmarshal(inputStream);
        inputStream.close();
    }

    /**
     * use this factory to read a file (.musicxml, .xml, .mxl) into a MusicXml instance
     * @param file the input file should be of type .musicxml, .xml or .mxl
     * @return the MusicXml object or null
     */
    public static MusicXml fromFile(File file) {
        String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
        if (extension.equals("mxl")) {                                          // if it is a compressed MusicXML
            try {
                Mxl.Input mxlInput = new Mxl.Input(file);
                RootFile musicXmlRootFile = mxlInput.getRootFiles().get(0);     // the first rootfile entry should be the MusicXML root, according to https://www.w3.org/2021/06/musicxml40/tutorial/compressed-mxl-files/
                ZipEntry zipEntry = mxlInput.getEntry(musicXmlRootFile.fullPath);
                InputStream inputStream = mxlInput.getInputStream(zipEntry);
                MusicXml out = new MusicXml(inputStream);
                out.setFile(Helper.getFilenameWithoutExtension(file.getPath()) + ".musicxml");
                return out;
            } catch (IOException | Marshalling.UnmarshallingException | Mxl.MxlException | JAXBException e) {
                e.printStackTrace();
            }
        }
        else {                                                                  // if it is a raw/uncompressed MuxicXml file
            try {
                return new MusicXml(file);
            } catch (IOException | Marshalling.UnmarshallingException e) {
                e.printStackTrace();
            }
        }

        return null;                                                            // if the file could not be unmarshalled, return null
    }

    /**
     * deterine if this object holds MusicXML data
     * @return
     */
    @Override
    public boolean isEmpty() {
        return this.data == null;
    }

    /**
     * Load and unmarshall a new XML document into this object. This will effectively replace all MusicXML data
     * stored in this object so far. The document itself will not be stored in this object as it will not
     * reflect any editings done afterward on the unmarshalled datastructure.
     * @param document
     */
    @Override
    public synchronized void setDocument(Document document) {
        String xml = document.toXML();
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Object o = null;
        try {
            o = Marshalling.unmarshal(inputStream);
        } catch (Marshalling.UnmarshallingException e) {
            e.printStackTrace();
            try {
                inputStream.close();
            } catch (IOException ex) {
                e.printStackTrace();
            }
            return;
        }

        if (o != null)
            this.data = o;
        else
            System.err.println("Input stream cannot be unmarshalled into an instance of ScorePartwise, ScoreTimewise or Opus.");
    }

    /**
     * from the MusicXML data in this object construct a Document
     * @return
     */
    @Override
    public Document getDocument() {
        Document document = null;
        Builder builder = new Builder(false);           // if the validate argument in the Builder constructor is true, the data should be valid
        try {
            document = builder.build(new ByteArrayInputStream(this.toXML().getBytes(StandardCharsets.UTF_8)));
        } catch (ValidityException e) {                 // in case of a ValidityException (no valid data)
            document = e.getDocument();                 // make the XOM Document anyway, we may nonetheless be able to work with it
        } catch (ParsingException | IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    /**
     * access the MusicXML data structure in this object
     * @return
     */
    public Object getData() {
        return this.data;
    }

    /**
     * query the type of MusicXML data in this object
     * @return
     */
    public MusicXmlType getType() {
        if (this.data instanceof ScorePartwise)
            return MusicXmlType.scorePartwise;
        if (this.data instanceof ScoreTimewise)
            return MusicXmlType.scoreTimewise;
        if (this.data instanceof Opus)
            return MusicXmlType.opus;
        return MusicXmlType.unknown;
    }

    /**
     * get the title of the MusicXML
     * @return
     */
    public String getTitle() {
        if (this.data == null)
            return "";

        switch (this.getType()) {
            case scorePartwise:
                ScorePartwise sp = (ScorePartwise) this.data;
                if ((sp.getWork() != null) && (sp.getWork().getWorkTitle() != null))
                    return sp.getWork().getWorkTitle();
                break;
            case scoreTimewise:
                ScoreTimewise st = (ScoreTimewise) this.data;
                if ((st.getWork() != null) && (st.getWork().getWorkTitle() != null))
                    return st.getWork().getWorkTitle();
                break;
            case opus:
                Opus op = (Opus) this.data;
                if (op.getTitle() != null)
                    return op.getTitle();
                break;
            case unknown:
            default:
                break;
        }

        if (this.getFile() != null)
            return this.getFile().getName();

        return "";
    }

    /**
     * returns the MusicXML data as XML string or null if this is empty
     * @return
     */
    @Override
    public synchronized String toXML() {
        String out = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            switch (this.getType()) {
                case scorePartwise:
                    Marshalling.marshal((ScorePartwise) this.data, outputStream, false, 4);
                    out = outputStream.toString();
                    break;
                case scoreTimewise:                     // not yet supported by ProxMusic
//                    Marshalling.marshal((ScoreTimewise) this.data, outputStream, false, 4);
//                    out = outputStream.toString();
//                    break;
                    throw new DataFormatException("MusicXML ScoreTimewise format is not supported for marshalling to String.");
                case opus:
                    Marshalling.marshal((Opus) this.data, outputStream);
                    out = outputStream.toString();
                    break;
                case unknown:
                default:
                    break;
            }
        } catch (Marshalling.MarshallingException | DataFormatException e) {
            e.printStackTrace();
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }

    /**
     * check validity of the XML; always true for MusicXML, as XML code is generated via marshalling of the data structure
     * @return
     */
    @Override
    public boolean isValid() {
        return true;
    }

    /**
     * XML validation is not supported for MusicXml instances
     * @param schema
     * @return
     */
    @Override
    public synchronized String validate(URL schema) {
        return "MusicXml.validate() is not supported.";
    }

    @Override
    public boolean writeFile() {
        throw new UnsupportedOperationException("MusicXml.writeFile() is not supported.");
    }

    @Override
    public synchronized boolean writeFile(String filename) {
        throw new UnsupportedOperationException("MusicXml.writeFile() is not supported.");
    }

    /**
     * this method is inactive; invoke MusicXml.getDocument().getRootElement() instead
     * @return
     */
    @Override
    public Element getRootElement() {
        System.err.println("MusicXml.getRootElement() is not supported.");
        return null;
    }

    /**
     * this method is inactive for MusicXml instances
     * @param localName the elements to be removed
     * @return
     */
    @Override
    public int removeAllElements(String localName) {
        System.err.println("MusicXml.removeAllElements() is not supported.");
        return 0;
    }

    /**
     * this method is inactive for MusicXml instances
     * @param attributeName the attribute name
     * @return
     */
    @Override
    public int removeAllAttributes(String attributeName) {
        System.err.println("MusicXml.removeAllAttributes() is not supported.");
        return 0;
    }

    /**
     * generate an XML document and apply an XSL Transform to it
     * @param xslt
     * @return result of the transform as XOM Document instance
     */
    @Override
    public Document xslTransformToDocument(File xslt) {
        return Helper.xslTransformToDocument(this.getDocument(), xslt);
    }

    /**
     * generate an XML document and apply an XSL Transform to it
     * @param transform
     * @return result of the transform as XOM Document instance
     */
    @Override
    public Document xslTransformToDocument(Xslt30Transformer transform) {
        return Helper.xslTransformToDocument(this.getDocument(), transform);
    }

    /**
     * generate an XML document and apply an XSL Transform to it
     * @param xslt
     * @return result of the transform as String instance
     */
    @Override
    public String xslTransformToString(File xslt) {
        return Helper.xslTransformToString(this.toXML(), xslt);
    }

    /**
     * generate an XML document and apply an XSL Transform to it
     * @param transform
     * @return result of the transform as String instance
     */
    @Override
    public String xslTransformToString(Xslt30Transformer transform) {
        return Helper.xslTransformToString(this.toXML(), transform);
    }

    /**
     * enumeration of the different datatypes behind a MusicXml instance
     */
    public enum MusicXmlType {
        scorePartwise,
        scoreTimewise,
        opus,
        unknown
    }
}
