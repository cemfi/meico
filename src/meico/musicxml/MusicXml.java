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
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;

/**
 * This class represents a MusicXML document.
 * It relies heaily on the framework <a href="https://github.com/Audiveris/proxymusic">ProxyMusic</a>.
 * @author Axel Berndt
 */
public class MusicXml extends XmlBase {
    private ScorePartwise scorePartwise = null;
    private ScoreTimewise scoreTimewise = null;
    private Opus opus = null;

    /**
     * constructor
     */
    public MusicXml() {
        ObjectFactory factory = new ObjectFactory();
        this.scorePartwise = factory.createScorePartwise();
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
     * constructor
     * @param file MusicXML file, compressed MusicXML (.mxl) is also supported
     * @throws IOException
     * @throws Marshalling.UnmarshallingException
     */
    public MusicXml(File file) throws IOException, Marshalling.UnmarshallingException {
        this(Files.newInputStream(file.toPath()));
        this.setFile(file);
    }

    public ArrayList<MusicXml> fromFile(File file) {
        ArrayList<MusicXml> out = new ArrayList<>();

        String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
        if (extension.equals("mxl")) {
            Mxl.Input mxlInput;
            try {
                mxlInput = new Mxl.Input(file);
            } catch (IOException | Mxl.MxlException | JAXBException e) {
                e.printStackTrace();
                return out;
            }

            for (RootFile rootFile : mxlInput.getRootFiles()) {
                ZipEntry zipEntry;
                try {
                    zipEntry = mxlInput.getEntry(rootFile.fullPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                ..........https://www.w3.org/2021/06/musicxml40/tutorial/compressed-mxl-files/
            }
        }

        try {
            out.add(new MusicXml(file));
        } catch (IOException | Marshalling.UnmarshallingException e) {
            e.printStackTrace();
        }

        return  out;
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
     * @param inputStream
     * @throws Marshalling.UnmarshallingException
     * @throws IOException
     */
    public MusicXml(InputStream inputStream) throws Marshalling.UnmarshallingException, IOException {
        Object o = Marshalling.unmarshal(inputStream);
        inputStream.close();
        if (o instanceof ScorePartwise)
            this.scorePartwise = (ScorePartwise) o;
        else if (o instanceof Opus)
            this.opus = (Opus) o;
        else if (o instanceof ScoreTimewise)        // not yet supported by ProxyMusic, but maybe in the future
            this.scoreTimewise = (ScoreTimewise) o;
        else
            throw new IllegalArgumentException("Input stream cannot be unmarshalled into an instance of ScorePartwise, ScoreTimewise or Opus.");
    }

    /**
     * deterine if this object holds MusicXML data
     * @return
     */
    @Override
    public boolean isEmpty() {
        return (this.opus == null) && (this.scorePartwise == null) && (this.scoreTimewise == null);
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
        Object o;
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

        if (o instanceof ScorePartwise) {
            this.scorePartwise = (ScorePartwise) o;
            this.scoreTimewise = null;
            this.opus = null;
        } else if (o instanceof Opus) {
            this.opus = (Opus) o;
            this.scorePartwise = null;
            this.scoreTimewise = null;
        } else if (o instanceof ScoreTimewise) {        // not yet supported by ProxyMusic, but maybe in the future
            this.scoreTimewise = (ScoreTimewise) o;
            this.scorePartwise = null;
            this.opus = null;
        } else {
            System.err.println("Input stream cannot be unmarshalled into an instance of ScorePartwise, ScoreTimewise or Opus.");
        }
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
     * returns the MusicXML data as XML string or null if this is empty
     * @return
     */
    @Override
    public synchronized String toXML() {
        String out = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            if (this.scorePartwise != null) {
                Marshalling.marshal(this.scorePartwise, outputStream, false, 4);
                out = outputStream.toString();
            }
            else if (this.scoreTimewise != null) {        // not yet supported by ProxyMusic
//                Marshalling.marshal(this.scoreTimewise, outputStream, true, 4);
//                out = outputStream.toString();
                throw new DataFormatException("MusicXML ScoreTimewise format is not supported for marshalling to String.");
            }
            else if (this.opus != null) {
                Marshalling.marshal(this.opus, outputStream);
                out = outputStream.toString();
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

    @Override
    public boolean isValid() {
        return true;
    }

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

    @Override
    public Element getRootElement() {
        System.err.println("MusicXml.getRootElement() is not supported.");
        return null;
    }

    @Override
    public int removeAllElements(String localName) {
        System.err.println("MusicXml.removeAllElements() is not supported.");
        return 0;
    }

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
}
