package meico.musicxml;

import meico.mei.Helper;
import meico.mei.Mei;
import meico.mpm.Mpm;
import meico.msm.Msm;
import meico.supplementary.KeyValue;
import meico.xml.XmlBase;
import net.sf.saxon.s9api.Xslt30Transformer;
import nu.xom.*;
import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.mxl.Mxl;
import org.audiveris.proxymusic.mxl.RootFile;
import org.audiveris.proxymusic.opus.Opus;
import org.audiveris.proxymusic.util.Marshalling;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.lang.String;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;

/**
 * This class represents a MusicXML document.
 * It relies heaily on the framework <a href="https://github.com/Audiveris/proxymusic">ProxyMusic</a>.
 * @author Axel Berndt
 */
public class MusicXml extends XmlBase {
    private static final boolean INJECT_SIGNATURE = false;  // if true, ProxyMusic signs the marshalled MusicXML files
    protected Object data;                                  // this replaces the data Document from XmlBase, its datatype is either ScorePartwise, ScoreTimewise or Opus, depending on the input data

    /**
     * constructor
     */
    public MusicXml() {
        ObjectFactory factory = new ObjectFactory();
        this.data = factory.createScorePartwise();
    }

    /**
     * constructor
     * @param scorePartwise
     */
    public MusicXml(ScorePartwise scorePartwise) {
        this.data = scorePartwise;
    }

    /**
     * constructor
     * @param scoreTimewise
     */
    public MusicXml(ScoreTimewise scoreTimewise) {
        this.data = scoreTimewise;
    }

    /**
     * constructor
     * @param opus
     */
    public MusicXml(Opus opus) {
        this.data = opus;
    }

    /**
     * constructor
     * @param document an instance of a XOM Document
     * @throws Marshalling.UnmarshallingException
     * @throws IOException
     */
    private MusicXml(Document document) throws Marshalling.UnmarshallingException, IOException {
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
    private MusicXml(String xml) throws Marshalling.UnmarshallingException, IOException {
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
    public static MusicXml from(File file) {
        String content;
        String filename = file.getPath();
        String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
        if (extension.equals("mxl")) {                                          // if it is a compressed MusicXML
            StringBuilder stringBuilder = new StringBuilder();
            try {
                Mxl.Input mxlInput = new Mxl.Input(file);
                RootFile musicXmlRootFile = mxlInput.getRootFiles().get(0);     // the first rootfile entry should be the MusicXML root, according to https://www.w3.org/2021/06/musicxml40/tutorial/compressed-mxl-files/
                ZipEntry zipEntry = mxlInput.getEntry(musicXmlRootFile.fullPath);
                InputStream inputStream = mxlInput.getInputStream(zipEntry);

//                MusicXml out = new MusicXml(inputStream);
                // we do not instantiate the MusicXml from the file directly; we read the file into a String, so we can react if it is a score-timewise that has to be converted into a score-partwise
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                bufferedReader.close();
            } catch (IOException | Mxl.MxlException | JAXBException e) {
                e.printStackTrace();
                return null;
            }
            content = stringBuilder.toString();
            filename = Helper.getFilenameWithoutExtension(file.getPath()) + ".musicxml";
        } else {                                                                // if it is a raw/uncompressed MuxicXml file
            try {
//                return new MusicXml(file);
                // we do not instantiate the MusicXml from the file directly; we read the file into a String, so we can react if it is a score-timewise that has to be converted into a score-partwise
                content = new String(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        MusicXml out = MusicXml.from(content);
        if (out != null)
            out.setFile(filename);
        return out;
    }

    /**
     * Use this constructor to create a MusicXml instance from a XOM Document.
     * @param document
     * @return the MusicXml instance or null
     */
    public static MusicXml from(Document document) {
        if (document == null)
            return null;

        switch (document.getRootElement().getLocalName()) {
            case "score-timewise":
                // ProxyMusic does not support unmarshalling of score-timewise; so we first have to convert the document into a score-partwise, create a MusicXML and make a score-timewise from it again
                Document preprocessedDocument = toScorePartwise(document);
                if (preprocessedDocument != null) {
                    try {
                        return new MusicXml(preprocessedDocument).toScoreTimewise();
                    } catch (Marshalling.UnmarshallingException | IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "score-partwise":
            case "opus":
                try {
                    return new MusicXml(document);
                } catch (Marshalling.UnmarshallingException | IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    /**
     * Use this factory method to create a MusicXml instance from an XML string.
     * @param xml
     * @return the MusicXml instance or null
     */
    public static MusicXml from(String xml) {
        if (xml == null)
            return null;

        try {
            return MusicXml.from((new XmlBase(xml)).getDocument());
        } catch (IOException | ParsingException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ProxyMusic does not support unmarshalling of score-timewise MusicXML. This method is meant to
     * work around this limitation. It converts a score-timewise XML Document to a score-partwise one, so
     * it can be used to create a MusicXml object.
     * @param scoreTimewise
     * @return
     */
    public static Document toScorePartwise(Document scoreTimewise) {
        switch (scoreTimewise.getRootElement().getLocalName()) {
            case "score-timewise":          // it is a score-timewise
                break;                      // continue with the processing
            case "score-partwise":          // if it is already a score-partwise
                return scoreTimewise;       // no processing necessary; just return it
            default:                        // data not convertible
                System.out.println("A " + scoreTimewise.getRootElement().getLocalName() + " document cannot be converted into a score-partwise representation.");
                return null;                // cancel and return nothing
        }

        Document scorePartwise = (new MusicXml()).getDocument();
        Element root = scorePartwise.getRootElement();

        // create deep copies of all elements in scoreTimewise and add them to scorePartwise, except for the measures
        Elements firstChildren = scoreTimewise.getRootElement().getChildElements();
        ArrayList<Element> measures = new ArrayList<>();
        Element partList = null;
        for (Element e : firstChildren) {
            if (e.getLocalName().equals("measure")) {
                measures.add(e);
                continue;
            }

            Element clone = e.copy();
            root.appendChild(clone);

            if (clone.getLocalName().equals("part-list"))
                partList = clone;
        }

        // create the parts
        HashMap<String, Element> partMap = new HashMap<>();
        if (partList != null) {
            for (Element part : partList.getChildElements("score-part")) {
                Attribute id = part.getAttribute("id");
                if (id == null)
                    continue;
                Element newPart = new Element("part");
                newPart.addAttribute(new Attribute("id", id.getValue()));
                root.appendChild(newPart);
                partMap.put(id.getValue(), newPart);
            }
        }

        // translate the measures' parts to the parts' measures
        for (Element measure : measures) {
            // for each measure's part create a measure in the scorePartwise part
            for (Element part : measure.getChildElements("part")) {
                Attribute id = part.getAttribute("id");
                if (id == null)
                    continue;

                Element newPart = partMap.get(id.getValue());
                if (newPart == null)
                    continue;

                // clone the part contents and add them to the partwise measure
                Element measureClone = Helper.cloneElement(measure);    // flat clone of the measure element to be used as partwise measure
                newPart.appendChild(measureClone);
                for (Element measureChild : part.getChildElements())
                    measureClone.appendChild(measureChild.copy());
            }
        }

        System.out.println(scorePartwise.toXML());

        return scorePartwise;
    }

    /**
     * Convert this object into a score-partwise representation of the MusicXML.
     * If this is already a score-partwise MusicXML, the result is this.
     * If this is an opus MusicXML, the result is null.
     * Otherwise, the result is a new MusicXml instance with the converted data.
     * @return the result or null
     */
    public MusicXml toScorePartwise() {
        if (this.data instanceof ScorePartwise)
            return this;

        if (this.data instanceof Opus)
            return null;

        ScorePartwise scorePartwise = new ScorePartwise();
        ScoreTimewise scoreTimewise = (ScoreTimewise) this.data;

        scorePartwise.setWork(scoreTimewise.getWork());
        scorePartwise.setDefaults(scoreTimewise.getDefaults());
        scorePartwise.setIdentification(scoreTimewise.getIdentification());
        scorePartwise.setVersion(scoreTimewise.getVersion());
        scorePartwise.setMovementTitle(scoreTimewise.getMovementTitle());
        scorePartwise.setMovementNumber(scoreTimewise.getMovementNumber());
        scorePartwise.getCredit().addAll(scoreTimewise.getCredit());
        scorePartwise.setPartList(scoreTimewise.getPartList());

        // create parts in scorePartwise
        for (Object part : scoreTimewise.getPartList().getPartGroupOrScorePart()) {
            if (!(part instanceof ScorePart))
                continue;
            ScorePartwise.Part spwPart = new ScorePartwise.Part();
            scorePartwise.getPart().add(spwPart);
            spwPart.setId(part);
        }

        // translate the measures' parts to the parts' measures
        for (ScoreTimewise.Measure stwMeasure : scoreTimewise.getMeasure()) {       // for each measure
            // for each measure's part create a measure in the scorePartwise part
            for (ScoreTimewise.Measure.Part stwPart : stwMeasure.getPart()) {      // for each part in the measure
                // find the corresponding scorePartwise part
                ScorePartwise.Part spwPart = null;
                for (ScorePartwise.Part p : scorePartwise.getPart()) {
                    if (p.getId().equals(stwPart.getId())) {
                        spwPart = p;
                        break;
                    }
                }

                // if no scorePartwise part was found, create one
                if (spwPart == null) {
                    spwPart = new ScorePartwise.Part();
                    scorePartwise.getPart().add(spwPart);
                    spwPart.setId(stwPart);
                }

                // create a measure in the corresponding scorePartwise part and fill it
                ScorePartwise.Part.Measure spwMeasure = new ScorePartwise.Part.Measure();
                spwPart.getMeasure().add(spwMeasure);
                spwMeasure.setImplicit(stwMeasure.getImplicit());
                spwMeasure.setNumber(stwMeasure.getNumber());
                spwMeasure.setWidth(stwMeasure.getWidth());
                spwMeasure.setNonControlling(stwMeasure.getNonControlling());
                spwMeasure.getNoteOrBackupOrForward().addAll(stwPart.getNoteOrBackupOrForward());   // add the contents to the measure
            }
        }

        MusicXml out = new MusicXml(scorePartwise);

        if (this.file != null)
            out.setFile(Helper.getFilenameWithoutExtension(this.file.getPath()) + "_as_score-partwise.musicxml");

        return out;
    }

    /**
     * Convert this object into a score-timewise representation of the MusicXML.
     * If this is already a score-timewise MusicXML, the result is this.
     * If this is an opus MusicXML, the result is null.
     * Otherwise, the result is a new MusicXml instance with the converted data.
     * @return the result or null
     */
    public MusicXml toScoreTimewise() {
        if (this.data instanceof ScoreTimewise)
            return this;

        if (this.data instanceof Opus)
            return null;

        ScoreTimewise scoreTimewise = new ScoreTimewise();
        ScorePartwise scorePartwise = (ScorePartwise) this.data;

        scoreTimewise.setWork(scorePartwise.getWork());
        scoreTimewise.setDefaults(scorePartwise.getDefaults());
        scoreTimewise.setIdentification(scorePartwise.getIdentification());
        scoreTimewise.setVersion(scorePartwise.getVersion());
        scoreTimewise.setMovementTitle(scorePartwise.getMovementTitle());
        scoreTimewise.setMovementNumber(scorePartwise.getMovementNumber());
        scoreTimewise.getCredit().addAll(scorePartwise.getCredit());
        scoreTimewise.setPartList(scorePartwise.getPartList());

        // translate each part's measure to parts in a measure
        for (ScorePartwise.Part spwPart : scorePartwise.getPart()) {                // for each part
            for (int measureNumber = 0; measureNumber < spwPart.getMeasure().size(); ++measureNumber) { // for each measure in the part
                ScorePartwise.Part.Measure spwMeasure = spwPart.getMeasure().get(measureNumber);

                // find or create the corresponding score-timewise measure
                ScoreTimewise.Measure stwMeasure;
                while (scoreTimewise.getMeasure().size() <= measureNumber) {
                    stwMeasure = new ScoreTimewise.Measure();
                    scoreTimewise.getMeasure().add(stwMeasure);
                    stwMeasure.setImplicit(spwMeasure.getImplicit());
                    stwMeasure.setNumber(spwMeasure.getNumber());
                    stwMeasure.setWidth(spwMeasure.getWidth());
                    stwMeasure.setNonControlling(spwMeasure.getNonControlling());
                }
                stwMeasure = scoreTimewise.getMeasure().get(measureNumber);

                // create and add the part to the measure
                ScoreTimewise.Measure.Part stwPart = new ScoreTimewise.Measure.Part();
                stwPart.setId(spwPart.getId());
                stwPart.getNoteOrBackupOrForward().addAll(spwMeasure.getNoteOrBackupOrForward());
                stwMeasure.getPart().add(stwPart);
            }
        }

        MusicXml out = new MusicXml(scoreTimewise);

        if (this.file != null)
            out.setFile(Helper.getFilenameWithoutExtension(this.file.getPath()) + "_as_score-timewise.musicxml");

        return out;
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
     * reflect any editings done afterward on the unmarshalled datastructure. The Document should be of type
     * score-partwise or opus. If it is score-timewise, use the from() factory instead to create a new MusicXml
     * instance.
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

        SAXParserFactory parserFactory = SAXParserFactory.newInstance();    // create a SAX parser (see https://stackoverflow.com/questions/51072419/how-use-xmlreaderfactory-now-because-this-is-deprecated)
        SAXParser parser;
        XMLReader xmlreader;
        try {
            parser = parserFactory.newSAXParser();
            xmlreader = parser.getXMLReader();                        // with the SAX parser create an xml reader
            xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);  // disable fetching of DTD as this usually does not work with XOM (see https://stackoverflow.com/questions/8081214/ignoring-dtd-when-parsing-xml)
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            return null;
        }

        Builder builder = new Builder(xmlreader);                           // if the validate argument in the Builder constructor is true, the data should be valid

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

        String out = "";

        switch (this.getType()) {
            case scorePartwise:
                ScorePartwise sp = (ScorePartwise) this.data;
                if (sp.getWork() != null) {
                    if (sp.getWork().getWorkNumber() != null)
                        out += sp.getWork().getWorkNumber();
                    if (sp.getWork().getWorkTitle() != null)
                        out += out.isEmpty() ? sp.getWork().getWorkTitle() : " " + sp.getWork().getWorkTitle();
                }
                if (sp.getMovementNumber() != null)
                    out += out.isEmpty() ? sp.getMovementNumber() : " " + sp.getMovementNumber();
                if (sp.getMovementTitle() != null)
                    out += out.isEmpty() ? sp.getMovementTitle() : " " + sp.getMovementTitle();
                break;
            case scoreTimewise:
                ScoreTimewise st = (ScoreTimewise) this.data;
                if (st.getWork() != null) {
                    if (st.getWork().getWorkNumber() != null)
                        out += st.getWork().getWorkNumber();
                    if (st.getWork().getWorkTitle() != null)
                        out += out.isEmpty() ? st.getWork().getWorkTitle() : " " + st.getWork().getWorkTitle();
                }
                if (st.getMovementNumber() != null)
                    out += out.isEmpty() ? st.getMovementNumber() : " " + st.getMovementNumber();
                if (st.getMovementTitle() != null)
                    out += out.isEmpty() ? st.getMovementTitle() : " " + st.getMovementTitle();
                break;
            case opus:
                Opus op = (Opus) this.data;
                if (op.getTitle() != null)
                    out = op.getTitle();
                break;
            case unknown:
            default:
                break;
        }

        if (out != null)
            return out;

        if (this.getFile() != null)
            return this.getFile().getName();

        return "";
    }

    /**
     * get the part-list of the MusicXML
     * @return the PartList object or null if none exists
     */
    public PartList getPartList() {
        switch (this.getType()) {
            case scorePartwise:
                return ((ScorePartwise) this.data).getPartList();
            case scoreTimewise:
                return ((ScoreTimewise) this.data).getPartList();
            case opus:
            case unknown:
            default:
                break;
        }
        return null;
    }

    /**
     * convert the MusicXML to an MSM and MPM pair
     * @return
     */
    public KeyValue<Msm, Mpm> exportMsmMpm() {
        return this.exportMsmMpm(720, true);
    }

    /**
     * convert the MusicXML to an MSM and MPM pair
     * @param ppq pulses per quarter time resolution
     * @param cleanup set true to return a clean msm file or false to keep all the crap from the conversion
     * @return
     */
    public KeyValue<Msm, Mpm> exportMsmMpm(int ppq, boolean cleanup) {
        return (new MusicXml2MsmMpmConverter(ppq, cleanup)).convert(this);
    }

    /**
     * convert MusicXML to MEI
     * @return
     */
    public Mei exportMei() {
        System.err.println("MusicXML to MEI conversion is not yet supported.");
        return null;
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
                    Marshalling.marshal((ScorePartwise) this.data, outputStream, INJECT_SIGNATURE, 4);
                    out = outputStream.toString();
                    break;
                case scoreTimewise:                     // not yet supported by ProxMusic, so we have to convert to ScorePartwise
//                    Marshalling.marshal((ScoreTimewise) this.data, outputStream, INJECT_SIGNATURE, 4);
                    Marshalling.marshal((ScorePartwise) this.toScorePartwise().data, outputStream, INJECT_SIGNATURE, 4);
                    out = outputStream.toString();
                    break;
                case opus:
                    Marshalling.marshal((Opus) this.data, outputStream);
                    out = outputStream.toString();
                    break;
                case unknown:
                default:
                    throw new Marshalling.MarshallingException(new Throwable("Unknown data format for MusicXML, unable to marshal."));
            }
        } catch (Marshalling.MarshallingException e) {
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

    /**
     * writes the MusicXML to a file at this.file (it must be != null);
     * if there is already a file with this name, it is replaces!
     * @return true if success, false if an error occured
     */
    @Override
    public boolean writeFile() {
        return this.writeMusicXml();
    }

    /**
     * writes the MusicXML to a file (filename should include the path and the extension)
     * @param filename the filename string; it should include the path and the extension
     * @return true if success, false if an error occured
     */
    @Override
    public synchronized boolean writeFile(String filename) {
        return this.writeMusicXml(filename);
    }

    /**
     * writes the MusicXML to a file at this.file (it must be != null);
     * if there is already a file with this name, it is replaces!
     * @return true if success, false if an error occured
     */
    public boolean writeMusicXml() {
        if (this.file == null) {
            System.err.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        if (this.isEmpty()) {
            System.err.println("Empty document, cannot write file.");
            return false;
        }

        return this.writeMusicXml(this.file.getPath());
    }

    /**
     * writes the MusicXML to a file (filename should include the path and the extension)
     * @param filename the filename string; it should include the path and the extension
     * @return true if success, false if an error occured
     */
    public synchronized boolean writeMusicXml(String filename) {
        if (this.isEmpty()) {
            System.err.println("Empty document, cannot write file.");
            return false;
        }

        boolean isMxl = filename.substring(filename.lastIndexOf('.')).equals(".mxl");
        if (isMxl) {
            System.out.println("According to the file extension, a Compressed MusicXML should be written. Switching to the corresponding method.");
            return this.writeCompressedMusicXml(filename);
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

        OutputStream os;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        boolean success = true;
        try {
            switch (this.getType()) {
                case scorePartwise:
                    Marshalling.marshal((ScorePartwise) this.data, os, INJECT_SIGNATURE, 4);
                    break;
                case scoreTimewise:
//                    Marshalling.marshal((ScoreTimewise) this.data, os, INJECT_SIGNATURE, 4);                  // not supported by ProxyMusic
                    Marshalling.marshal((ScorePartwise) this.toScorePartwise().data, os, INJECT_SIGNATURE, 4);  // so we convert it to ScorePartwise
                    break;
                case opus:
                    Marshalling.marshal((Opus) this.data, os);
                    break;
                case unknown:
                default:
                    throw new Marshalling.MarshallingException(new Throwable("Unknown data format for MusicXML, unable to marshal."));
            }
        } catch (Marshalling.MarshallingException e) {
            e.printStackTrace();
            success = false;
        }

        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.file == null)
            this.file = file;

        return success;
    }

    /**
     * writes the MusicXML to a .mxl file with the path and name of this.file (it must be != null);
     * if there is already a file with this name, it is replaces!
     * @return true if success, false if an error occured
     */
    public boolean writeCompressedMusicXml() {
        if (this.file == null) {
            System.err.println("Cannot write to the file system. Path and filename are not specified.");
            return false;
        }

        if (this.isEmpty()) {
            System.err.println("Empty document, cannot write file.");
            return false;
        }

        return this.writeCompressedMusicXml(Helper.getFilenameWithoutExtension(this.file.getPath()) + ".mxl");
    }

    /**
     * writes the MusicXML to an .mxl file (filename should include the path and the extension)
     * @param filename the filename string; it should include the path and the extension
     * @return true if success, false if an error occured
     */
    public synchronized boolean writeCompressedMusicXml(String filename) {
        if (this.isEmpty()) {
            System.err.println("Empty document, cannot write file.");
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

        Mxl.Output mof;
        try {
            mof = new Mxl.Output(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        boolean success = true;
        OutputStream zos = mof.getOutputStream();
        String rootfilename = Helper.getFilenameWithoutExtension(file.getName()) + ".musicxml";
        try {
            mof.addEntry(new RootFile(rootfilename, RootFile.MUSICXML_MEDIA_TYPE));
            switch (this.getType()) {
                case scorePartwise:
                    Marshalling.marshal((ScorePartwise) this.data, zos, INJECT_SIGNATURE, 4);
                    break;
                case scoreTimewise:
//                    Marshalling.marshal((ScoreTimewise) this.data, zos, INJECT_SIGNATURE, 4);
                    Marshalling.marshal((ScorePartwise) this.toScorePartwise().data, zos, INJECT_SIGNATURE, 4);
                    break;
                case opus:
                    Marshalling.marshal((Opus) this.data, zos);
                    break;
                case unknown:
                default:
                    throw new Marshalling.MarshallingException(new Throwable("Unknown data format for MusicXML, unable to marshal."));
            }
        } catch (Marshalling.MarshallingException | Mxl.MxlException e) {
            e.printStackTrace();
            success = false;
        }

        try {
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.file == null)
            this.file = new File(rootfilename);

        return success;
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
