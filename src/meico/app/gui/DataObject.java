package meico.app.gui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import meico.app.Humanizer;
import meico.audio.Audio;
import meico.mei.Helper;
import meico.mei.Mei;
import meico.midi.Midi;
import meico.mpm.Mpm;
import meico.mpm.elements.Part;
import meico.mpm.elements.Performance;
import meico.mpm.elements.maps.ArticulationMap;
import meico.mpm.elements.maps.DynamicsMap;
import meico.mpm.elements.maps.GenericMap;
import meico.mpm.elements.maps.TempoMap;
import meico.msm.Msm;
import meico.musicxml.MusicXml;
import meico.pitches.Pitches;
import meico.supplementary.KeyValue;
import meico.svg.Svg;
import meico.svg.SvgCollection;
import meico.xml.XmlBase;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Xslt30Transformer;
import nu.xom.*;
import org.xml.sax.SAXException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * This class represents the data objects and provides graphical interface elements for it.
 * To add new items/functionality in a radial menu, add its label text to the corresponding array in makeRadialMenu() and add its functionality to setMenuItemInteraction().
 * @author Axel Berndt
 */
class DataObject extends Group {

    private Workspace workspace;                        // the workspace in which this is displayed
    private Object data;                                // the actual data (Mei, Msm, Pitches, Midi ...)
    private String label;                               // the text label
    private Color color;
    private Group menu = null;                          // the radial menu will be computed only once and is then readily available
    private boolean menuActive = false;
    private ArrayList<DataObjectLine> lines = new ArrayList<>();    // the list of connections lines to child data objects that were exported from this one
    private ArrayList<Thread> threads = new ArrayList<>();
    private boolean isActive = false;               // will be set true when it is activated for transformations

    /**
     * constructor
     * @param data could be a File object or an instance of any of the supported types (Mei, Msm, Pitches, Midi, Audio)
     * @param workspace
     * @throws NullPointerException
     * @throws InvalidMidiDataException
     * @throws ParsingException
     * @throws UnsupportedAudioFileException
     * @throws IOException
     */
    public DataObject(Object data, Workspace workspace) throws NullPointerException, InvalidMidiDataException, ParsingException, UnsupportedAudioFileException, IOException, SaxonApiException, SAXException, ParserConfigurationException {
        this.workspace = workspace;
        if (data instanceof File) {
            this.data = this.readFile((File) data);
        }
        else {
            this.data = Objects.requireNonNull(data);
        }
        this.label = this.getName();

        this.draw();                    // this creates the visual representation of the data (circle with label) and the corresponding radial menu
        this.setInteractionBehavior();
    }

    /**
     * constructor
     * @param mei
     * @param workspace
     * @throws NullPointerException
     */
    public DataObject(Mei mei, Workspace workspace) throws NullPointerException {
        this.workspace = workspace;
        this.data = mei;
        this.label = this.getName();

        this.draw();                    // this creates the visual representation of the data (circle with label) and the corresponding radial menu
        this.setInteractionBehavior();
    }

    /**
     * constructor
     * @param msm
     * @param workspace
     * @throws NullPointerException
     */
    public DataObject(Msm msm, Workspace workspace) throws NullPointerException {
        this.workspace = workspace;
        this.data = msm;
        this.label = this.getName();

        this.draw();                    // this creates the visual representation of the data (circle with label) and the corresponding radial menu
        this.setInteractionBehavior();
    }

    /**
     * free some memory
     */
    protected synchronized void clear() {
        for (Thread thread : this.threads) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
        this.threads.clear();

        for (DataObjectLine l : this.lines) {
            l.getPartner(this).forget(l);
            this.workspace.getContainer().getChildren().remove(l);
        }
        this.workspace = null;
        this.data = null;
        this.label = null;
        this.lines = null;
    }

    /**
     * removes the specified line
     * @param line
     */
    protected synchronized void forget(DataObjectLine line) {
        this.lines.remove(line);
    }

    /**
     * given an input file, from its extension this method decides which type of object to instantiate and returns the result or null if unknown
     * @param file
     */
    protected synchronized Object readFile(File file) throws InvalidMidiDataException, IOException, ParsingException, UnsupportedAudioFileException, SaxonApiException, SAXException, ParserConfigurationException {
        int index = file.getName().lastIndexOf(".");
        if (index < 0) {                                                    // if the file has no extension
            this.workspace.getApp().getStatuspanel().setMessage("Input file type is not supported by meico.");
            throw new IOException("File type is not supported by meico.");
        }

        String extension = file.getName().substring(index).toLowerCase();   // get the file extension
        switch (extension) {
            case ".xml":                                    // xml could be anything
               return readXmlFileToCorrectType(file);       // read the file and find out from its root element what it is
            case ".mei":
                return new Mei(file);
            case ".msm":
                return new Msm(file);
            case ".mpm":
                return new Mpm(file);
            case ".mid":
                return new Midi(file);
            case ".wav":
                return new Audio(file);
            case ".mp3":
//                this.workspace.getApp().getStatuspanel().setMessage("Input file type .mp3 is not yet supported by meico.");
                throw new IOException("File type .mp3 is not supported by meico.");
            case ".json":
//                this.workspace.getApp().getStatuspanel().setMessage("Input file type .json is not yet supported by meico.");
                throw new IOException("File type .json is not supported by meico.");
            case ".dls":
            case ".sf2":
                return new meico.app.gui.Soundbank(file, this);
            case ".xsl":
                return new meico.app.gui.XSLTransform(file);
            case ".rng":
                return new meico.app.gui.Schema(file);
            case ".txt":
                return new TxtData(file);
            case ".musicxml":
                return new MusicXml(file);
            case ".mxl":
                throw new IOException("File type .mxl (compressed MusicXML) is not supported by meico.");
            default:
//                this.workspace.getApp().getStatuspanel().setMessage("Input file type " + extension + " is not supported by meico.");
                throw new IOException("File type " + extension + " is not supported by meico.");
        }
    }

    /**
     * When an XML file is loaded it has to be identified as MEI, MSM, MusicXML etc. to create the right instance.
     * @param file
     * @return
     */
    public Object readXmlFileToCorrectType(File file) throws SAXException, ParsingException, ParserConfigurationException, IOException, SaxonApiException {
        XmlBase xml = new XmlBase(file);                                        // parse the xml file into an instance of XmlBase
        XmlBase o;                                                              // this will be the output object that has to get the right type

        switch (xml.getRootElement().getLocalName()) {                          // get the name of the root element in the xml tree
            case "mei":                                                         // seems to be an mei
                o = new Mei(xml.getDocument());
                break;
            case "msm":                                                         // seems to be an msm
                o = new Msm(xml.getDocument());
                break;
            case "mpm":                                                         // seems to be an mpm
                o = new Mpm(xml.getDocument());
                break;
            case "score-partwise":                                              // seems to be a musicxml
            case "score-timewise":
                o = new MusicXml(xml.getDocument());
                break;
            case "stylesheet":                                                  // seems to be an xslt
                return new meico.app.gui.XSLTransform(file);
            default:
                throw new IOException("Meico could not identify the type of data in this XML file as one of its supported types.");
        }

        o.setFile(file);                                                        // set the file variable
        return o;                                                               // return the object
    }

    /**
     * read the label from the data
     * @return
     */
    private synchronized String getFileName() {
        if (this.data instanceof Mei)
            return ((Mei)this.data).getFile().getName();
        else if (this.data instanceof SvgCollection) {
            SvgCollection svgs = (SvgCollection) this.data;
            return (svgs.isEmpty()) ? "empty SVG collection" : svgs.getSvgs().get(0).getFile().getName();
        }
        else if (this.data instanceof Msm)
            return ((Msm)this.data).getFile().getName();
        else if (this.data instanceof Mpm)
            return ((Mpm)this.data).getFile().getName();
        else if (this.data instanceof Performance)
            return ((Performance)this.data).getName();      // performances have no filename
        else if (this.data instanceof Midi)
            return ((Midi)this.data).getFile().getName();
        else if (this.data instanceof Audio)
            return ((Audio)this.data).getFile().getName();
        else if (this.data instanceof Pitches)
            return ((Pitches)this.data).getFile().getName();
        else if (this.data instanceof meico.app.gui.Soundbank)
            return ((meico.app.gui.Soundbank)this.data).getFile().getName();
        else if (this.data instanceof meico.app.gui.XSLTransform)
            return ((meico.app.gui.XSLTransform)this.data).getFile().getName();
        else if (this.data instanceof meico.app.gui.Schema)
            return ((meico.app.gui.Schema)this.data).getFile().getName();
        else if (this.data instanceof TxtData)
            return ((TxtData)this.data).getFile().getName();
        else if (this.data instanceof MusicXml)
            return ((MusicXml)this.data).getFile().getName();
        return "no filename";
    }

    /**
     * returns the title of the encoded music (for MEI and MSM) or the filename (in any other case)
     * @return
     */
    private synchronized String getName() {
        String name = "";
        if (this.data instanceof Mei)
            name = ((Mei)this.data).getTitle();
        else if (this.data instanceof SvgCollection)
            name = ((SvgCollection)this.data).getTitle();
        else if (this.data instanceof Msm)
            name = ((Msm)this.data).getTitle();
        else if (this.data instanceof Performance)
            name = ((Performance)this.data).getName();
        return (name.isEmpty()) ? this.getFileName() : name;    // if the name string is (still) empty, return the filename
    }

    /**
     * returns the class of this' data
     * @return
     */
    protected Class<?> getDataType() {
        return this.data.getClass();
    }

    /**
     * a getter to access the data
     * @return
     */
    protected Object getData() {
        return this.data;
    }

    /**
     * define the interactivity
     */
    private synchronized void setInteractionBehavior() {
        // drag interaction
        this.getChildren().get(this.getChildren().size() - 1).setOnMouseDragged(e -> {                                  // drag should only work when clicking the center circle, not any surrounding element; the center circle is added as last element (see draw())
            this.toFront();                                                                                             // move this object to the front in the z-buffer so that it is painted on top of the others

            Point2D localDragCoordinates = this.workspace.getContainer().sceneToLocal(e.getSceneX(), e.getSceneY());    // from the global scene position of the drag gesture compute the local position in the container
            this.setLayout(Math.max(localDragCoordinates.getX() + 2 * Settings.dataItemStrokeWidth, 0.0), Math.max(localDragCoordinates.getY() + 2 * Settings.dataItemStrokeWidth, 0.0));     // set the new position, restrict drag into negative space (due to stroke width there is a little offset of 2*strokeWidth)

            for (DataObjectLine l : this.lines)                                                                         // all the lines that go from here to other objects ...
                l.draw();                                                                                               // need to be updated in accordance to the new object position

            e.consume();
        });

        // when clicked (mouse released) open/close the radial menu
        this.getChildren().get(this.getChildren().size() - 1).setOnMouseReleased(e -> {
            this.toFront();                                                                                             // move this object to the front in the z-buffer so that it is painted on top of the others
            if (this.menu == null) {                                                                                    // if there is no menu
                e.consume();
                return;                                                                                                 // done
            }
            if (e.isDragDetect()) {                                                                                     // open/close the menu only if the click is not part of a drag interaction
                if (this.menuActive) {                                                                                  // if menu is open
                    this.getChildren().remove(this.menu);                                                               // close it
                    this.menuActive = false;
                }
                else {                                                                                                  // if menu is closed
                    this.getChildren().add(this.getChildren().size() - 1, this.menu);                                   // open it, place it right behind the circle
                    this.menuActive = true;
                }
            }
            e.consume();
        });

        // double clicks are a quick and convenient way to activate and deactivate soundbanks, xslts, performances and to play back midi and audio data
        this.getChildren().get(this.getChildren().size() - 1).setOnMouseClicked(e -> {
            if(e.getClickCount() == 2){
                if (this.data instanceof Midi) {
                    this.workspace.getApp().getPlayer().play((Midi)this.data);
                }
                else if (this.data instanceof Audio) {
                    this.workspace.getApp().getPlayer().play((Audio) this.data);
                }
                else if (this.data instanceof Soundbank) {
                    Thread thread = new Thread(() -> {
                        RotateTransition ani = this.startComputeAnimation();
                        meico.app.gui.Soundbank soundbank = (meico.app.gui.Soundbank)this.data;
                        if (!this.isActive()) {
                            this.workspace.silentDeactivationOfAllSoundbanks();
                            soundbank.activate();
                            this.activate();
                        }
                        else {
                            soundbank.deactivate();
                            this.deactivate();
                        }
                        this.stopComputeAnimation(ani);
                    });
                    this.start(thread);
                }
                else if (this.data instanceof XSLTransform) {
                    Thread thread = new Thread(() -> {
                        RotateTransition ani = this.startComputeAnimation();
                        if (!this.isActive()) {
                            this.workspace.deactivateAllXSLTs();
                            this.activate();
                        }
                        else {
                            this.deactivate();
                        }
                        this.stopComputeAnimation(ani);
                    });
                    this.start(thread);
                }
                else if (this.data instanceof Schema) {
                    Thread thread = new Thread(() -> {
                        RotateTransition ani = this.startComputeAnimation();
                        if (!this.isActive()) {
                            this.workspace.deactivateAllSchemas();
                            this.activate();
                        }
                        else {
                            this.deactivate();
                        }
                        this.stopComputeAnimation(ani);
                    });
                    this.start(thread);
                }
                else if (this.data instanceof Performance) {
                    Thread thread = new Thread(() -> {
                        RotateTransition ani = this.startComputeAnimation();
                        if (!this.isActive()) {
                            this.workspace.deactivateAllPerformances();
                            this.activate();
                        }
                        else {
                            this.deactivate();
                        }
                        this.stopComputeAnimation(ani);
                    });
                    this.start(thread);
                }
            }
        });

        // mouse over
        this.getChildren().get(this.getChildren().size() - 1).setOnMouseEntered(e -> this.workspace.getApp().getStatuspanel().setMessage(this.data.getClass().getSimpleName().toUpperCase() + ": " + this.getFileName()));    // print datatype and filename in statusbar

        // mouse over ended
        this.getChildren().get(this.getChildren().size() - 1).setOnMouseExited(e -> this.workspace.getApp().getStatuspanel().setMessage("")); // empty the statusbar
    }

    /**
     * create the visual appearance of the data
     */
    private synchronized void draw() {
        double radius = Settings.dataItemRadius;
        double strokeWidth = Settings.dataItemStrokeWidth;
        this.color = Settings.classToColor(this.data);

        // the center circle
        Circle circle = new Circle(radius - strokeWidth / 2);   // half of the stroke width adds to the radius, this is to compensate it
        circle.setFill(this.color.darker());
        circle.setStrokeWidth(strokeWidth);
        circle.setStroke(this.color);
        circle.setOpacity(Settings.dataItemOpacity);

        // the text label
        String label = (this.label.length() < 48) ? this.label : this.label.substring(0, 45) + "...";  // get the text label; if it is too long, cut it and add "..." at the end
        Text text = new Text(label);
        text.setFont(Settings.font);
        text.setStyle(Settings.DATA_OBJECT_LABEL);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(radius * 1.5);

        StackPane circleStack = new StackPane(circle, text);    // put circle and text in a StackPane (center alignment is done automatically)

        this.menu = this.makeRadialMenu(radius);         // the radial menu is just generated but not displayed

//        Path ring = this.drawSemiRing(Settings.font.getSize() * Settings.dataItemSize, Settings.font.getSize() * Settings.dataItemSize, 80, Settings.font.getSize() * (Settings.dataItemSize + 1), Color.RED);
        this.getChildren().addAll(/*first add all the others*/ circleStack);    // add them all to this Group, the center circle is last, so it is painted on top
    }

    /**
     * create the radial menu around the center circle
     */
    private synchronized Group makeRadialMenu(double innerRadius) {
        double itemHeight = Settings.dataItemMenuItemHeight;      // the aperture angle of the menu items
        double outerRadius;
        Group menu = new Group();

        if (this.data instanceof Mei) {
            String[] leftItems = {"Show", "Validate", "Add IDs", "Resolve copyof/sameas", "Resolve Expansions", "Reload", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {"to MSM & MPM", "to MIDI", "to Audio", "Score Rendering", "XSL Transform"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof SvgCollection) {
            String[] leftItems = {"Show", "Validate", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {"XSL Transform"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof Msm) {
            String[] leftItems = {"Show", "Validate", "Remove Rests", "Expand Repetitions", "Reload", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {"to MIDI", "to Expressive MIDI", "to Chroma", "to Absolute Pitches", "XSL Transform"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof Mpm) {
            String[] leftItems = {"Show", "Validate", "Make Tempo Global", "Make Dynamics Global", "Reload", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {"get Performances", "XSL Transform"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof Performance) {
            String[] leftItems = {"Show", "Activate", "Deactivate", "Add Humanizing", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof Midi) {
            String[] leftItems = {"Play", "NoteOffs to NoteOns", "NoteOns to NoteOffs", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {"to Audio", "to MSM", "Humanize (experimental)"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof Audio) {
            String[] leftItems = {"Play", "Save (mp3)", "Save (wav)", "Save As", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof Pitches) {
            String[] leftItems = {"Show", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof meico.app.gui.Soundbank) {
            String[] leftItems = {"Activate", "Deactivate", "Set As Default", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof meico.app.gui.XSLTransform) {
            String[] leftItems = {"Activate", "Deactivate", "Set As Default", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof meico.app.gui.Schema) {
            String[] leftItems = {"Activate", "Deactivate", "Set As Default", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof TxtData) {
            String[] leftItems = {"Show", "Validate", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {"XSL Transform"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof MusicXml) {
            String[] leftItems = {"Show", "Validate", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {/*TODO:"to MEI",*/ "XSL Transform"};
            outerRadius = innerRadius + this.computeVisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        return menu;
    }

    /**
     * this creates on item in the radial menu and its interaction behavior
     * @param text
     * @param angle
     * @param apertureAngle
     * @param innerRadius
     * @param outerRadius
     * @return
     */
    private synchronized Group makeMenuItem(String text, double angle, double apertureAngle, double innerRadius, double outerRadius) {
        double distanceFromCircle = Settings.dataItemMenuItemDistanceFromCircle;    // this specifies the distance between circle and menu item
        double labelIndent = Settings.dataItemMenuItemLabelIndent;                  // shift the label a bit into the item body so that it does not start right at the border

        // make the item label
        Text label = new Text(text);
        label.setFont(Settings.font);
        label.setFill(this.color);
//        label.setStyle("-fx-line-spacing: 0em;");
//        label.setFont(Settings.getFont("/resources/fonts/someFont.otf-ttf", 12, this));

        angle = angle % 360;                                                        // reduce the angle to the lowest necessary number

        if (((angle > 90) && (angle < 270)) || ((angle < -90) && (angle > -270))) { // when the menu item is on the left side, the rotation would be more than 90 degree and the text would be upside down
            label.setRotate(180);                                                   // turn the text by 120 degree
            label.setTextAlignment(TextAlignment.RIGHT);                            // set text alignment to the right side
            label.setTranslateY(Settings.font.getSize() * 0.5);                     // center the label vertically in the item body
        }
        else {                                                                      // if the menu item is on the right side
            label.setTextAlignment(TextAlignment.LEFT);                             // just make sure that the alignment is to the left
            label.setTranslateY(Settings.font.getSize() * 0.35);                    // center the label vertically in the item body
        }

        label.setTranslateX(innerRadius + distanceFromCircle + labelIndent);        // put the label to the right of the circle (from here rotation will later be done)

        // make the item body
        Path body = this.drawArc(apertureAngle, innerRadius + distanceFromCircle, outerRadius + Settings.dataItemMenuItemOuterIndent);    // make the item body (1.75 makes it a bit longer so that the longest label does not reach the border)
        body.setFill(this.color.darker().darker());
        body.setOpacity(Settings.dataItemOpacity);
        body.setStrokeWidth(innerRadius * 0.025);
//        body.setStroke(new Color(color.getRed() * 0.1, color.getGreen() * 0.1, color.getBlue() * 0.1, Settings.dataItemOpacity / 2));
        body.setStroke(this.color.deriveColor(0, 1, 0.1, 0.2));
        body.setFillRule(FillRule.EVEN_ODD);

        Group menuItem = new Group(body, label);                                    // this Group object represets the menu item incl. all its components

        this.setMenuItemInteraction(menuItem, label, body);                         // define the interaction

        // Transformations
        Translate translate = new Translate(innerRadius, innerRadius);              // movement to the right of the circle

        Rotate rotate = new Rotate(angle);                                          // rotation around the circle
        rotate.setPivotX(innerRadius);                                              // set x-coordinate of the center of the circle
        rotate.setPivotY(innerRadius);                                              // set y-coordinate of the center of the circle

        menuItem.getTransforms().addAll(rotate, translate);                         // apply all transformations

        return menuItem;
    }

    /**
     * this contructs the geometry of a radial menu item
     * @param apertureAngle
     * @param innerRadius
     * @param outerRadius
     * @return
     */
    private synchronized Path drawArc(double apertureAngle, double innerRadius, double outerRadius) {
        double halfAngle = Math.toRadians(apertureAngle) / 2;

        double x1 = innerRadius * Math.cos(halfAngle);
        double y1 = innerRadius * Math.sin(halfAngle);

        double x2 = outerRadius * Math.cos(halfAngle);
        double y2 = outerRadius * Math.sin(halfAngle);

        double x3 = outerRadius * Math.cos(-halfAngle);
        double y3 = outerRadius * Math.sin(-halfAngle);

        double x4 = innerRadius * Math.cos(-halfAngle);
        double y4 = innerRadius * Math.sin(-halfAngle);

        MoveTo p1 = new MoveTo(x1, y1);
        LineTo p2 = new LineTo(x2, y2);
        ArcTo p3 = new ArcTo(outerRadius, outerRadius, apertureAngle, x3, y3, false, false);
        LineTo p4 = new LineTo(x4, y4);
        ArcTo p5 = new ArcTo(innerRadius, innerRadius, apertureAngle, x1, y1, false, true);

        return new Path(p1, p2, p3, p4, p5);
    }

    /**
     * this method starts a compute animation on this data object
     * @return
     */
    private synchronized RotateTransition startComputeAnimation() {
        // a spinner icon
        Text icon = new Text("\uf110");
//        icon.setFont(Settings.getIconFont(Settings.font.getSize() * 7, this));
        icon.setFont(Settings.getIconFont(Settings.dataItemRadius * 1.5, this));
        icon.setFill(Color.LIGHTGRAY);
        icon.setOpacity(Settings.dataItemOpacity * 0.5);
        icon.setTextAlignment(TextAlignment.CENTER);

        // spin the icon
        RotateTransition rot = new RotateTransition(Duration.millis(1500), icon);
        rot.setByAngle(360);
        rot.setInterpolator(Interpolator.LINEAR);
        rot.setCycleCount(Timeline.INDEFINITE);
        rot.setAutoReverse(false);
        rot.play();

        // add the spinning icon to the JavaFX thread
        Platform.runLater(() -> ((StackPane)this.getChildren().get(this.getChildren().size() - 1)).getChildren().add(icon));

        return rot;
    }

    /**
     * this method stops the given compute animation on this data object
     * @param animation
     */
    private synchronized void stopComputeAnimation(RotateTransition animation) {
        animation.stop();                                                                                                       // stop the animation
        Node node = animation.getNode();                                                                                        // get the animated node (spinner icon)
        animation.setNode(null);                                                                                                // remove it from the animation
        Platform.runLater(() -> ((StackPane)this.getChildren().get(this.getChildren().size() - 1)).getChildren().remove(node)); // remove the node from this data object's circle stack (always the last element in this' group)
    }

    /**
     * for the given menu item and based on its label text define its interaction
     * @param item
     * @param label
     * @param body
     */
    private synchronized void setMenuItemInteraction(Group item, Text label, Path body) {
        String command = label.getText();           // get the label text, i.e. the command

        // the close command is similar for all types of data objects, hence it is defined here only once
        if (command.equals("Close")) {
            this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> this.getWorkspace().remove(this));
            return;
        }

        // object type-specific commands
        if (this.getData() instanceof Mei) {             // MEI-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.getWorkspace().getApp().getWeb() != null) {
                                Platform.runLater(() -> {
                                    this.getWorkspace().getApp().getWeb().printContent((Helper.prettyXml(((Mei) this.getData()).toXML())), true);
                                    if (this.getWorkspace().getApp().getWebAccordion() != null) {
                                        this.getWorkspace().getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.getWorkspace().getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
                                    }
                                });
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Validate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.validate();
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Add IDs":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage(((Mei)this.getData()).addIds() + " IDs added.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Resolve copyof/sameas":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            int notResolved = ((Mei)this.getData()).resolveCopyofsAndSameas().size();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Resolving copyof and sameas: " + ((notResolved == 0) ? "done." :  (notResolved + " could not be resolved.")));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Resolve Expansions":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            ((Mei)this.getData()).resolveExpansions();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Resolving expansions: done.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Reload":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.reload();
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving MEI data " + ((((Mei)this.getData()).writeMei(((Mei)this.getData()).getFile().getAbsolutePath())) ? ("to " + ((Mei)this.getData()).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Mei mei = ((Mei)this.getData());                                     // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(mei.getFile().getParent());          // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(mei.getFile().getName());            // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MEI files (*.mei)", "*.mei"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.getWorkspace().getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving MEI data " + ((mei.writeMei(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                this.stopComputeAnimation(ani);
                            });
                            this.start(thread);
                        }
                    });
                    break;
//                case "Close":
//                    break;
                case "to MSM & MPM":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MEI to MSM and MPM ...");
                            KeyValue<List<Msm>, List<Mpm>> mspm = ((Mei)this.getData()).exportMsmMpm(Settings.Mei2Msm_ppq, Settings.Mei2Msm_dontUseChannel10, Settings.Mei2Msm_ignoreExpansions, Settings.Mei2Msm_msmCleanup);   // do the conversion
                            if (this.getWorkspace() != null) {                                       // it is possible that the data object has been removed from workspace in the meantime
                                ArrayList<Object> lo = new ArrayList<>();                       // sort the msms and mpms in this list
                                for (int i = 0; i < mspm.getKey().size(); ++i) {
                                    lo.add(mspm.getKey().get(i));                               // add msm
                                    lo.add(mspm.getValue().get(i));                             // add the corresponding mpm right next to the msm
                                }
                                this.addSeveralChildren(mouseEvent, lo);
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MEI to MSM and MPM: done.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "to MIDI":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MEI to MIDI ...");
                            List<Msm> msms = ((Mei)this.getData()).exportMsm(Settings.Mei2Msm_ppq, Settings.Mei2Msm_dontUseChannel10, Settings.Mei2Msm_ignoreExpansions, Settings.Mei2Msm_msmCleanup);   // do the conversion
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                ArrayList<Midi> midis = new ArrayList<>();
                                for (Msm msm : msms)
                                    midis.add(msm.exportMidi(Settings.Msm2Midi_defaultTempo, Settings.Msm2Midi_generateProgramChanges));
                                this.addSeveralChildren(mouseEvent, midis);
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MEI to MIDI: done.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "to Audio":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MEI to Audio ...");
                            List<Msm> msms = ((Mei)this.getData()).exportMsm(Settings.Mei2Msm_ppq, Settings.Mei2Msm_dontUseChannel10, Settings.Mei2Msm_ignoreExpansions, Settings.Mei2Msm_msmCleanup);   // do the conversion
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                ArrayList<Audio> audios = new ArrayList<>();
                                for (Msm msm : msms)
                                    audios.add(msm.exportMidi(Settings.Msm2Midi_defaultTempo, Settings.Msm2Midi_generateProgramChanges).exportAudio(Settings.soundbank));
                                this.addSeveralChildren(mouseEvent, audios);
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MEI to Audio: done.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Score Rendering":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("MEI Score Rendering via Verovio ...");
                            // do Verovio score rendering
                            Platform.runLater(() -> {
                                Mei mei = (Mei)this.getData();
                                String verovio = null;                                                          // this will get the HTML code to be shown in the WebView
                                verovio = VerovioGenerator.generate(mei.toXML(), this);                         // generate that HTML code

//                                SvgCollection svgs = ((Mei)this.data).exportSvg(Settings.useLatestVerovio, Settings.oneLineScore);  // do the conversion TODO: these two line would be nicer instead of the stuff below that is only displaying SVGs, but it is not functional, yet
//                                this.addOneChild(mouseEvent, svgs);                                                                 // add the newly generated SvgCollection to the workspace

                                this.getWorkspace().getApp().getWeb().printContent(verovio, false);             // show it in the WebView
                                if (this.getWorkspace().getApp().getWebAccordion() != null) {
                                    this.getWorkspace().getApp().getWebAccordion().setText("Score: " + this.label);  // change the title string of the WebView
                                    this.getWorkspace().getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
                                }
                            });
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });

                    // TODO: this is an experimental alternative to the code above ... and it is not recommended
//                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
//                        Thread thread = new Thread(() -> {
//                            RotateTransition ani = this.startComputeAnimation();
//                            this.getWorkspace().getApp().getStatuspanel().setMessage("MEI to SVG Score Rendering via Verovio ...");
//
//                            // do Verovio score rendering
//                            Platform.runLater(() -> {
//                                Mei mei = (Mei) this.getData();
//                                String verovio = null;                                                          // this will get the HTML code to be shown in the WebView
//                                verovio = VerovioGenerator.generate(mei.toXml(), this);                         // generate that HTML code
//
//                                WebEngine webEngine = new WebEngine();
//                                webEngine.setJavaScriptEnabled(true);
//                                webEngine.getLoadWorker().stateProperty().addListener((ov, t, t1) -> {                          // create a listener that retreives the result of the conversion when the webEngine is done
//                                    if (t1 == Worker.State.SUCCEEDED) {
//                                        SvgCollection svgs = new SvgCollection();                                               // this will get the resulting SVG collection
//                                        svgs.setTitle("SVG Score: " + mei.getTitle());                                          // set collection title
//
//                                        String filename = Helper.getFilenameWithoutExtension(mei.getFile().getPath());          // get the filename without extension
//                                        int pageCount = (int) webEngine.executeScript("svgs.length");                           // request the number of pages
//
//                                        for (int i = 0; i < pageCount; ++i) {                                                   // for each page
//                                            try {
//                                                Svg svg = new Svg((String) webEngine.executeScript("getSvgPage(" + i + ")"));   // get its content, i.e. its SVG code
//                                                svg.setFile(filename + "-" + String.format("%04d", i) + ".svg");                // create filename incl. page numbering
//                                                svgs.add(svg);                                                                  // add it to the collection
//                                            } catch (IOException | ParsingException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//
//                                        this.addOneChild(mouseEvent, svgs);                                                     // add the newly generated SvgCollection to the workspace
//                                        this.stopComputeAnimation(ani);
//                                    }
//                                });
//
//                                webEngine.loadContent(verovio, "text/html");
//                            });
//                        });
//                        this.start(thread);
//                    });
                    break;
                case "XSL Transform":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to MEI ...");
                            XSLTransform activeXslt = this.getWorkspace().getActiveXSLT();
                            String xslFilename = "";
                            Xslt30Transformer xslt = null;
                            if (activeXslt != null) {
                                xslFilename = activeXslt.getFile().getName();
                                xslt = activeXslt.getTransform();
                            }
                            else {
                                if ((Settings.xslFile != null) && (Settings.xslTransform != null)) {
                                    xslFilename = Settings.getXslFile().getName();
                                    xslt = Settings.getXslTransform();
                                }
                            }
                            if (xslt != null) {
                                Mei mei = (Mei)this.getData();
                                String string = mei.xslTransformToString(xslt);
                                if (string != null) {
                                    TxtData txt = new TxtData(string, new File(Helper.getFilenameWithoutExtension(mei.getFile().getPath()) + "-" + Helper.getFilenameWithoutExtension(xslFilename) + ".txt"));    // do the xsl transform
                                    this.addOneChild(mouseEvent, txt);
                                    this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to MEI: done.");
                                }
                            }
                            else {
                                this.getWorkspace().getApp().getStatuspanel().setMessage("No XSL Transform activated.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof SvgCollection) {    // txt file-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            Platform.runLater(() -> {
                                if (this.getWorkspace().getApp().getWeb() != null) {
                                    String html = VerovioGenerator.generate((SvgCollection)this.getData(), Settings.oneLineScore, this);
                                    this.getWorkspace().getApp().getWeb().printContent(html, false);             // webEngine, do your job

                                    if (this.getWorkspace().getApp().getWebAccordion() != null) {
                                        this.getWorkspace().getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.getWorkspace().getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
                                    }
                                }
                            });
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Validate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.validate();
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            SvgCollection svgs = (SvgCollection)this.getData();
                            if (!svgs.isEmpty()) {
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving SVG data " + ((svgs.writeSvgs()) ? ("to " + svgs.get(0).getFile().getAbsolutePath() + " ...") : "failed."));
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        SvgCollection svgs = (SvgCollection) this.getData();
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(svgs.get(0).getFile().getParent());  // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(svgs.get(0).getFile().getName());    // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SVG files (*.svg)", "*.svg"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.getWorkspace().getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving SVG data " + ((svgs.writeSvgs(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + " ...") : "failed."));
                                this.stopComputeAnimation(ani);
                            });
                            this.start(thread);
                        }
                    });
                    break;
//                case "Close":
//                    break;
                case "XSL Transform":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to SVG ...");
                            XSLTransform activeXslt = this.getWorkspace().getActiveXSLT();
                            String xslFilename = "";
                            Xslt30Transformer xslt = null;
                            if (activeXslt != null) {
                                xslFilename = activeXslt.getFile().getName();
                                xslt = activeXslt.getTransform();
                            }
                            else {
                                if ((Settings.xslFile != null) && (Settings.xslTransform != null)) {
                                    xslFilename = Settings.getXslFile().getName();
                                    xslt = Settings.getXslTransform();
                                }
                            }
                            if (xslt != null) {
                                SvgCollection svgs = (SvgCollection)this.getData();
                                ArrayList<TxtData> txts = new ArrayList<>();
                                for (int i=0; i < svgs.size(); ++i) {
                                    Svg svg = svgs.get(i);
                                    String string = svg.xslTransformToString(xslt);
                                    if (string != null) {
                                        txts.add(new TxtData(string, new File(Helper.getFilenameWithoutExtension(svg.getFile().getPath()) + "-" + Helper.getFilenameWithoutExtension(xslFilename) + ".txt")));    // do the xsl transform
                                        this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to SVG: done.");
                                    }
                                }
                                this.addSeveralChildren(mouseEvent, txts);
                            }
                            else {
                                this.getWorkspace().getApp().getStatuspanel().setMessage("No XSL Transform activated.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof Msm) {        // MSM-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.getWorkspace().getApp().getWeb() != null) {
                                Platform.runLater(() -> {
                                    this.getWorkspace().getApp().getWeb().printContent((Helper.prettyXml(((Msm) this.getData()).toXML())), true);
                                    if (this.getWorkspace().getApp().getWebAccordion() != null) {
                                        this.getWorkspace().getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.getWorkspace().getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
                                    }
                                });
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Validate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.validate();
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Remove Rests":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            ((Msm)this.getData()).removeRests();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Rests removed.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Expand Repetitions":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            Msm msm = (Msm)this.getData();
                            ArrayList<GenericMap> articulationMaps = new ArrayList<>();

                            // apply the sequencingMaps to MPM data first
                            for (DataObjectLine line : this.lines) {
                                DataObject p = line.getPartner(this);
                                if (p.getData() instanceof Mpm) {
                                    Element globalSequencingMap = msm.getRootElement().getFirstChildElement("global").getFirstChildElement("dated").getFirstChildElement("sequencingMap");
                                    for (Performance performance : ((Mpm)p.getData()).getAllPerformances()) {
                                        if (globalSequencingMap != null) {
                                            HashMap<String, GenericMap> maps = performance.getGlobal().getDated().getAllMaps();
                                            for (GenericMap map : maps.values()) {
                                                map.applySequencingMap(globalSequencingMap);
                                                if (map instanceof ArticulationMap)     // in articulationMaps the elements have notid attribute that has to be updated after resolving the sequencingmaps in MSM
                                                    articulationMaps.add(map);          // so keep the articulationMaps for later reference
                                            }
                                        }
                                        Elements msmParts = msm.getParts();
                                        ArrayList<Part> mpmParts = performance.getAllParts();
                                        for (int pa=0; pa < performance.size(); ++pa) {
                                            Element msmPart = msmParts.get(pa);
                                            Element sequencingMap = msmPart.getFirstChildElement("dated").getFirstChildElement("sequencingMap");
                                            if (sequencingMap == null) {
                                                sequencingMap = globalSequencingMap;
                                                if (sequencingMap == null)
                                                    continue;
                                            }
                                            for (GenericMap map : mpmParts.get(pa).getDated().getAllMaps().values()) {
                                                map.applySequencingMap(sequencingMap);
                                                if (map instanceof ArticulationMap)     // in articulationMaps the elements have notid attribute that has to be updated after resolving the sequencingmaps in MSM
                                                    articulationMaps.add(map);          // so keep the articulationMaps for later reference
                                            }
                                        }
                                    }
                                    break;
                                }
                            }

                            // apply the sequencingMaps to MSM data, this will also delete the sequencingMaps
                            HashMap<String, String> repetitionIDs = msm.resolveSequencingMaps();

                            // update the articulationMap's elements' notid attributes
                            for (GenericMap map : articulationMaps) {
                                Helper.updateMpmNoteidsAfterResolvingRepetitions(map, repetitionIDs);
                            }

                            this.getWorkspace().getApp().getStatuspanel().setMessage("SequencingMaps resolved, repetitions expanded into through-composed form.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Reload":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.reload();
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving MSM data " + ((((Msm)this.getData()).writeMsm()) ? ("to " + ((Msm)this.getData()).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Msm msm = ((Msm)this.getData());                                     // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(msm.getFile().getParent());          // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(msm.getFile().getName());            // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MSM files (*.msm)", "*.msm"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.getWorkspace().getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving MSM data " + ((msm.writeMsm(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                this.stopComputeAnimation(ani);
                            });
                            this.start(thread);
                        }
                    });
                    break;
//                case "Close":
//                    break;
                case "to MIDI":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MSM to MIDI ...");
                            Midi midi = ((Msm)this.getData()).exportMidi(Settings.Msm2Midi_defaultTempo, Settings.Msm2Midi_generateProgramChanges);   // do the conversion
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, midi);
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MSM to MIDI: done.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "to Expressive MIDI":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MSM to expressive MIDI ...");

                            Performance performance = this.getWorkspace().getActivePerformance();           // find the currently active performance to generate an expressive midi sequence
                            if (performance != null) {
                                Midi midi = ((Msm) this.getData()).exportExpressiveMidi(performance, true); // generate the expressive midi
                                if ((midi != null) && (this.getWorkspace() != null)) {                      // it is possible that the data object has been removed from workspace in the meantime or that no midi object has been generated
                                    this.addOneChild(mouseEvent, midi);
                                    this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MSM to expressive MIDI: done.");
                                }
                            } else {
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MSM to expressive MIDI: failed. An MPM performance needs to be activated first.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "to Chroma":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MSM to Chroma ...");
                            Pitches chroma = ((Msm)this.getData()).exportChroma();   // do the conversion
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, chroma);
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MSM to Chroma: done.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "to Absolute Pitches":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MSM to absolute pitches ...");
                            Pitches pitches = ((Msm)this.getData()).exportPitches();   // do the conversion
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, pitches);
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MSM to absolute pitches: done.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "XSL Transform":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to MSM ...");
                            XSLTransform activeXslt = this.getWorkspace().getActiveXSLT();
                            String xslFilename = "";
                            Xslt30Transformer xslt = null;
                            if (activeXslt != null) {
                                xslFilename = activeXslt.getFile().getName();
                                xslt = activeXslt.getTransform();
                            }
                            else {
                                if ((Settings.xslFile != null) && (Settings.xslTransform != null)) {
                                    xslFilename = Settings.getXslFile().getName();
                                    xslt = Settings.getXslTransform();
                                }
                            }
                            if (xslt != null) {
                                Msm msm = (Msm)this.getData();
                                String string = msm.xslTransformToString(xslt);
                                if (string != null) {
                                    TxtData txt = new TxtData(string, new File(Helper.getFilenameWithoutExtension(msm.getFile().getPath()) + "-" + Helper.getFilenameWithoutExtension(xslFilename) + ".txt"));    // do the xsl transform
                                    this.addOneChild(mouseEvent, txt);
                                    this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to MSM: done.");
                                }
                            }
                            else {
                                this.getWorkspace().getApp().getStatuspanel().setMessage("No XSL Transform activated.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof Mpm) {        // MPM-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.getWorkspace().getApp().getWeb() != null) {
                                Platform.runLater(() -> {
                                    this.getWorkspace().getApp().getWeb().printContent((Helper.prettyXml(((Mpm) this.getData()).toXML())), true);
                                    if (this.getWorkspace().getApp().getWebAccordion() != null) {
                                        this.getWorkspace().getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.getWorkspace().getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
                                    }
                                });
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Validate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.validate();
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Make Tempo Global":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Merging all local tempo maps into the global ...");

                            ArrayList<Performance> performances = ((Mpm)this.getData()).getAllPerformances();
                            for (Performance performance : performances) {
                                TempoMap globalTempoMap = (TempoMap) performance.getGlobal().getDated().getMap(Mpm.TEMPO_MAP);
                                if (globalTempoMap == null)
                                    globalTempoMap = (TempoMap) performance.getGlobal().getDated().addMap(Mpm.TEMPO_MAP);

                                ArrayList<Part> parts = performance.getAllParts();
                                for (Part part : parts) {
                                    TempoMap localTempoMap = (TempoMap) part.getDated().getMap(Mpm.TEMPO_MAP);
                                    if (localTempoMap != null) {
                                        ArrayList<KeyValue<Double, Element>> es = localTempoMap.getAllElements();
                                        for (KeyValue<Double, Element> e : es) {
                                            e.getValue().detach();
                                            globalTempoMap.addElement(e.getValue());
                                        }
                                        part.getDated().removeMap(Mpm.TEMPO_MAP);
                                    }
                                }
                            }

                            this.getWorkspace().getApp().getStatuspanel().setMessage("Merging all local tempo maps into the global: done.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Make Dynamics Global":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Merging all local dynamics maps into the global ...");

                            ArrayList<Performance> performances = ((Mpm)this.getData()).getAllPerformances();
                            for (Performance performance : performances) {
                                DynamicsMap globalDynamicsMap = (DynamicsMap) performance.getGlobal().getDated().getMap(Mpm.DYNAMICS_MAP);
                                if (globalDynamicsMap == null)
                                    globalDynamicsMap = (DynamicsMap) performance.getGlobal().getDated().addMap(Mpm.DYNAMICS_MAP);

                                ArrayList<Part> parts = performance.getAllParts();
                                for (Part part : parts) {
                                    DynamicsMap localDynamicsMap = (DynamicsMap) part.getDated().getMap(Mpm.DYNAMICS_MAP);
                                    if (localDynamicsMap != null) {
                                        ArrayList<KeyValue<Double, Element>> es = localDynamicsMap.getAllElements();
                                        for (KeyValue<Double, Element> e : es) {
                                            e.getValue().detach();
                                            globalDynamicsMap.addElement(e.getValue());
                                        }
                                        part.getDated().removeMap(Mpm.DYNAMICS_MAP);
                                    }
                                }
                            }

                            this.getWorkspace().getApp().getStatuspanel().setMessage("Merging all local dynamics maps into the global: done.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Reload":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.reload();
                            boolean performancesOnWorkspace = false;
                            for (DataObjectLine l : this.lines) {
                                DataObject o = l.getPartner(this);
                                if (o.getData() instanceof Performance) {
                                    performancesOnWorkspace = true;
                                    Platform.runLater(() -> this.getWorkspace().remove(o));
                                }
                            }
                            if (performancesOnWorkspace) {
                                Point2D center = this.getCenterPoint();                 // the center coordinated of this data object
                                double distance = Settings.newDataObjectDistance;
                                double x = center.getX() + distance;
                                double y = center.getY();
                                ArrayList<DataObject> performanceObjects = this.addSeveralChildren(x, y, ((Mpm) this.getData()).getAllPerformances());
                                for (DataObject performanceObject : performanceObjects)
                                    this.getWorkspace().addToPerformances(performanceObject);
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving MPM data " + ((((Mpm)this.getData()).writeMpm()) ? ("to " + ((Mpm)this.getData()).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Mpm mpm = ((Mpm)this.getData());                                     // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(mpm.getFile().getParent());          // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(mpm.getFile().getName());            // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MPM files (*.mpm)", "*.mpm"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.getWorkspace().getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving MPM data " + ((mpm.writeMpm(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                this.stopComputeAnimation(ani);
                            });
                            this.start(thread);
                        }
                    });
                    break;
//                case "Close":
//                    break;
                case "get Performances":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Extracting Performances from MPM ...");
                            ArrayList<DataObject> performanceObjects = this.addSeveralChildren(mouseEvent, ((Mpm) this.getData()).getAllPerformances());
                            for (DataObject performanceObject : performanceObjects)
                                this.getWorkspace().addToPerformances(performanceObject);
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Extracting Performances from MPM: done, " + performanceObjects.size() + " performances found.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "XSL Transform":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to MPM ...");
                            XSLTransform activeXslt = this.getWorkspace().getActiveXSLT();
                            String xslFilename = "";
                            Xslt30Transformer xslt = null;
                            if (activeXslt != null) {
                                xslFilename = activeXslt.getFile().getName();
                                xslt = activeXslt.getTransform();
                            }
                            else {
                                if ((Settings.xslFile != null) && (Settings.xslTransform != null)) {
                                    xslFilename = Settings.getXslFile().getName();
                                    xslt = Settings.getXslTransform();
                                }
                            }
                            if (xslt != null) {
                                Mpm mpm = (Mpm)this.getData();
                                String string = mpm.xslTransformToString(xslt);
                                if (string != null) {
                                    TxtData txt = new TxtData(string, new File(Helper.getFilenameWithoutExtension(mpm.getFile().getPath()) + "-" + Helper.getFilenameWithoutExtension(xslFilename) + ".txt"));    // do the xsl transform
                                    this.addOneChild(mouseEvent, txt);
                                    this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to MPM: done.");
                                }
                            }
                            else {
                                this.getWorkspace().getApp().getStatuspanel().setMessage("No XSL Transform activated.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof Performance) {        // Performance-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.getWorkspace().getApp().getWeb() != null) {
                                Platform.runLater(() -> {
                                    this.getWorkspace().getApp().getWeb().printContent((Helper.prettyXml(((Performance) this.getData()).toXml())), true);
                                    if (this.getWorkspace().getApp().getWebAccordion() != null) {
                                        this.getWorkspace().getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.getWorkspace().getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
                                    }
                                });
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Activate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (!this.isActive()) {
                                this.getWorkspace().deactivateAllPerformances();
                                this.activate();
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Deactivate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.isActive()) {
                                this.deactivate();
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Add Humanizing":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Adding basic humanizing data to the performance ...");
                            Humanizer.addHumanizing((Performance) this.getData());
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Adding basic humanizing data to the performance: done.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
//                case "Close":
//                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof Midi) {       // MIDI-specific commands
            switch (command) {
                case "Play":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> this.getWorkspace().getApp().getPlayer().play((Midi)this.getData()));
                    break;
                case "NoteOffs to NoteOns":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting noteOff to noteOn events: " + (((Midi)this.getData()).noteOffs2NoteOns()) + " events converted.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "NoteOns to NoteOffs":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting noteOn (with velocity 0) to noteOff events: " + (((Midi)this.getData()).noteOns2NoteOffs()) + " events converted.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving MIDI data " + ((((Midi)this.getData()).writeMidi()) ? ("to " + ((Midi)this.getData()).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Midi midi = ((Midi)this.getData());                             // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(midi.getFile().getParent());         // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(midi.getFile().getName());           // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MIDI files (*.mid)", "*.mid"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.getWorkspace().getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving MIDI data " + ((midi.writeMidi(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                this.stopComputeAnimation(ani);
                            });
                            this.start(thread);
                        }
                    });
                    break;
//                case "Close":
//                    break;
                case "to Audio":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MIDI to audio ...");
                            Audio audio = ((Midi)this.getData()).exportAudio(this.getWorkspace().getActiveSoundbank());   // do the conversion
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, audio);
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MIDI to audio: done.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "to MSM":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MIDI to MSM ...");
                            Msm msm = ((Midi)this.getData()).exportMsm();        // do the conversion
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, msm);
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MIDI to MSM: done.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Humanize (experimental)":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Adding humanizing to MIDI data ...");
                            Midi midi = Humanizer.humanize((Midi)this.getData());
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, midi);
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Adding humanizing to MIDI data: done.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof Audio) {      // Audio-specific commands
            switch (command) {
                case "Play":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> this.getWorkspace().getApp().getPlayer().play((Audio)this.getData()));
                    break;
                case "Save (mp3)":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving audio data as MP3 ...");
                            RotateTransition ani = this.startComputeAnimation();
                            boolean success = ((Audio)this.getData()).writeMp3();
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving audio data as MP3 " + ((success) ? ("to " + Helper.getFilenameWithoutExtension(((Audio) this.getData()).getFile().getAbsolutePath()) + ".mp3.") : "failed."));
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save (wav)":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving audio data ...");
                            RotateTransition ani = this.startComputeAnimation();
                            boolean success = ((Audio)this.getData()).writeAudio();
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving audio data " + ((success) ? ("to " + ((Audio) this.getData()).getFile().getAbsolutePath() + ".") : "failed."));
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Audio audio = ((Audio)this.getData());                               // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(audio.getFile().getParent());        // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(audio.getFile().getName());          // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Wave files (*.wav)", "*.wav"), new FileChooser.ExtensionFilter("MP3 files (*.mp3)", "*.mp3"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.getWorkspace().getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            String type = file.getName();
                            type = type.substring(type.lastIndexOf(".")).toLowerCase();
                            switch (type) {
//                                case ".wav":  // default behavior
//                                    break;
                                case ".mp3": {
                                    this.getWorkspace().getApp().getStatuspanel().setMessage("Saving audio data as MP3 ...");
                                    Thread thread = new Thread(() -> {
                                        RotateTransition ani = this.startComputeAnimation();
                                        boolean success = audio.writeMp3(file.getAbsolutePath());
                                        if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving audio data as MP3" + ((success) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                        }
                                        this.stopComputeAnimation(ani);
                                    });
                                    this.start(thread);
                                    break;
                                }
                                default: {
                                    this.getWorkspace().getApp().getStatuspanel().setMessage("Saving audio data ...");
                                    Thread thread = new Thread(() -> {
                                        RotateTransition ani = this.startComputeAnimation();
                                        boolean success = audio.writeAudio(file.getAbsolutePath());
                                        if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving audio data " + ((success) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                        }
                                        this.stopComputeAnimation(ani);
                                    });
                                    this.start(thread);
                                    break;
                                }
                            }
                        }
                    });
                    break;
//                case "Close":
//                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof Pitches) {    // Pitches-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.getWorkspace().getApp().getWeb() != null) {
                                Platform.runLater(() -> {
                                    this.getWorkspace().getApp().getWeb().printContent(((Pitches) this.getData()).getAsString(true), true);
                                    if (this.getWorkspace().getApp().getWebAccordion() != null) {
                                        this.getWorkspace().getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.getWorkspace().getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
                                    }
                                });
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving JSON data " + ((((Pitches)this.getData()).writePitches(Settings.savePitchesWithPrettyPrint)) ? ("to " + ((Pitches)this.getData()).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Pitches pitches = ((Pitches)this.getData());                         // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(pitches.getFile().getParent());      // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(pitches.getFile().getName());        // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.getWorkspace().getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving pitch data " + ((pitches.writePitches(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                this.stopComputeAnimation(ani);
                            });
                            this.start(thread);
                        }
                    });
                    break;
//                case "Close":
//                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof meico.app.gui.Soundbank) {    // Soundbank-specific commands
            switch (command) {
                case "Set As Default":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            meico.app.gui.Soundbank soundbank = (meico.app.gui.Soundbank)this.getData();
                            Settings.setSoundbank(soundbank.getFile());
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Activate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            meico.app.gui.Soundbank soundbank = (meico.app.gui.Soundbank)this.getData();
                            if (!this.isActive()) {
                                this.getWorkspace().silentDeactivationOfAllSoundbanks();
                                soundbank.activate();
                                this.activate();
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Deactivate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            meico.app.gui.Soundbank soundbank = (meico.app.gui.Soundbank)this.getData();
                            if (this.isActive()) {
                                soundbank.deactivate();
                                this.deactivate();
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
//                case "Close":
//                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof meico.app.gui.XSLTransform) {    // XSLT-specific commands
            switch (command) {
                case "Set As Default":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            meico.app.gui.XSLTransform xsltransform = (meico.app.gui.XSLTransform) this.getData();
                            Settings.setXSLT(xsltransform.getFile());
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Activate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (!this.isActive()) {
                                this.getWorkspace().deactivateAllXSLTs();
                                this.activate();
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Deactivate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.isActive()) {
                                this.deactivate();
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
//                case "Close":
//                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof meico.app.gui.Schema) {    // XML schema-specific commands
            switch (command) {
                case "Set As Default":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            meico.app.gui.Schema schema = (meico.app.gui.Schema) this.getData();
                            Settings.setSchema(schema.getFile());
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Activate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (!this.isActive()) {
                                this.getWorkspace().deactivateAllSchemas();
                                this.activate();
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Deactivate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.isActive()) {
                                this.deactivate();
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
//                case "Close":
//                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof TxtData) {    // txt file-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            Platform.runLater(() -> {
                                if (this.getWorkspace().getApp().getWeb() != null) {
                                    this.getWorkspace().getApp().getWeb().printContent(((TxtData) this.getData()).getString(), true);
                                    if (this.getWorkspace().getApp().getWebAccordion() != null) {
                                        this.getWorkspace().getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.getWorkspace().getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
                                    }
                                }
                            });
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Validate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.validate();
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving TXT data " + ((((TxtData)this.getData()).writeTxtData()) ? ("to " + ((TxtData)this.getData()).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        TxtData txt = (TxtData) this.getData();
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(txt.getFile().getParent());          // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(txt.getFile().getName());            // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.getWorkspace().getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving pitch data " + ((txt.writeTxtData(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                this.stopComputeAnimation(ani);
                            });
                            this.start(thread);
                        }
                    });
                    break;
//                case "Close":
//                    break;
                case "XSL Transform":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to text data ...");
                            XSLTransform activeXslt = this.getWorkspace().getActiveXSLT();
                            String xslFilename = "";
                            Xslt30Transformer xslt = null;
                            if (activeXslt != null) {
                                xslFilename = activeXslt.getFile().getName();
                                xslt = activeXslt.getTransform();
                            }
                            else {
                                if ((Settings.xslFile != null) && (Settings.xslTransform != null)) {
                                    xslFilename = Settings.getXslFile().getName();
                                    xslt = Settings.getXslTransform();
                                }
                            }
                            if ((xslt != null) && (!((TxtData) this.getData()).getString().isEmpty())) {
                                // try interpreting the string as xml data
                                Builder builder = new Builder(false);                       // we leave the validate argument false as XOM's built-in validator does not support RELAX NG
                                Document input = null;
                                try {
                                    input = builder.build(new ByteArrayInputStream(((TxtData)this.getData()).getString().getBytes(StandardCharsets.UTF_8)));
                                } catch (ValidityException e) {                             // in case of a ValidityException
                                    input = e.getDocument();                                // make the XOM Document anyway, we may nonetheless be able to work with it
                                } catch (ParsingException | IOException e) {
                                    this.getWorkspace().getApp().getStatuspanel().setMessage(e.toString());
                                    e.printStackTrace();
                                    input = null;
                                }
                                if (input != null) {
                                    String string = Helper.xslTransformToString(input, xslt);
                                    if (string != null) {
                                        TxtData txt = new TxtData(string, new File(Helper.getFilenameWithoutExtension(((TxtData)this.getData()).getFile().getPath()) + "-" + Helper.getFilenameWithoutExtension(xslFilename) + ".txt"));    // do the xsl transform
                                        this.addOneChild(mouseEvent, txt);
                                        this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to text: done.");
                                    }
                                }
                            }
                            else {
                                this.getWorkspace().getApp().getStatuspanel().setMessage("No XSL Transform activated.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                default:
                    break;
            }
        }
        else if (this.getData() instanceof MusicXml) {    // txt file-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.getWorkspace().getApp().getWeb() != null) {
                                Platform.runLater(() -> {
                                    this.getWorkspace().getApp().getWeb().printContent((Helper.prettyXml(((MusicXml) this.getData()).toXML())), true);
                                    if (this.getWorkspace().getApp().getWebAccordion() != null) {
                                        this.getWorkspace().getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.getWorkspace().getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
                                    }
                                });
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Validate":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.validate();
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Saving MusicXML data " + ((((MusicXml)this.getData()).writeMusicXml()) ? ("to " + ((MusicXml)this.getData()).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        MusicXml mxl = ((MusicXml)this.getData());                                     // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(mxl.getFile().getParent());          // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(mxl.getFile().getName());            // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MusicXML files (*.musicxml)", "*.musicxml"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.getWorkspace().getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Saving MSM data " + ((mxl.writeMusicXml(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                this.stopComputeAnimation(ani);
                            });
                            this.start(thread);
                        }
                    });
                    break;
//                case "Close":
//                    break;
                case "to MEI":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MusicXML to MEI ...");
                            Mei mei = ((MusicXml)this.getData()).exportMei(Settings.useLatestVerovio);   // do the conversion
                            if (this.getWorkspace() != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, mei);
                                this.getWorkspace().getApp().getStatuspanel().setMessage("Converting MusicXML to MEI: done.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "XSL Transform":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to MEI ...");
                            XSLTransform activeXslt = this.getWorkspace().getActiveXSLT();
                            String xslFilename = "";
                            Xslt30Transformer xslt = null;
                            if (activeXslt != null) {
                                xslFilename = activeXslt.getFile().getName();
                                xslt = activeXslt.getTransform();
                            }
                            else {
                                if ((Settings.xslFile != null) && (Settings.xslTransform != null)) {
                                    xslFilename = Settings.getXslFile().getName();
                                    xslt = Settings.getXslTransform();
                                }
                            }
                            if (xslt != null) {
                                MusicXml mxl = (MusicXml)this.getData();
                                String string = mxl.xslTransformToString(xslt);
                                if (string != null) {
                                    TxtData txt = new TxtData(string, new File(Helper.getFilenameWithoutExtension(mxl.getFile().getPath()) + "-" + Helper.getFilenameWithoutExtension(xslFilename) + ".txt"));    // do the xsl transform
                                    this.addOneChild(mouseEvent, txt);
                                    this.getWorkspace().getApp().getStatuspanel().setMessage("Applying currently active XSLT to MEI: done.");
                                }
                            }
                            else {
                                this.getWorkspace().getApp().getStatuspanel().setMessage("No XSL Transform activated.");
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * a generic method to define the interaction of a menu item;
     * what the item actually does must be provided via parameter inter
     * @param item
     * @param label
     * @param body
     * @param inter
     */
    private synchronized void menuItemInteractionGeneric(Group item, Text label, Path body, Interaction inter) {
        item.setOnMouseEntered(e -> {
            label.setFill(Color.LIGHTGRAY);
            body.setFill(this.color.darker());
            e.consume();
        });
        item.setOnMouseExited(e -> {
            label.setFill(this.color);
            body.setFill(this.color.darker().darker());
            e.consume();
        });
        item.setOnMousePressed(e -> {
            label.setFill(Color.LIGHTGRAY);
            body.setFill(this.color);
            e.consume();
        });
        item.setOnMouseReleased(e -> {
            if (e.isDragDetect()) {                 // do the action only if the click was not part of a drag gesture
                label.setFill(Color.LIGHTGRAY);
                body.setFill(this.color.darker());
                try {
                    inter.action(e);                 // here is the action
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            else {
                label.setFill(this.color);
                body.setFill(this.color.darker().darker());
            }
            e.consume();
        });

    }

    /**
     * This method defines an action that is triggered by clicking a radial menu item.
     * It is overwritten in method setMenuItemInteraction() by the actual action.
     * This interface construct is only necessary to pass the action as a parameter to other methods
     */
    private interface Interaction {
        void action(MouseEvent emouseEvent);
    }

    /**
     * start a thread and add its reference to the threads list (for maintenance purposes)
     * @param thread
     */
    private void start(Thread thread) {
        this.threads.add(thread);               // add the thread to the list
        thread.start();                         // start it
        
        // remove threads that are no longer alive from the list
        for (Thread t : this.threads) {
            if (!t.isAlive())
                this.threads.remove(t);
        }
    }

    /**
     * this method can be called to reload the original data, e.g. when it has been altered
     */
    private void reload() {
        if (this.data instanceof Mei) {
            if (!((Mei) this.data).getFile().exists())
                return;
            try {
                this.data = new Mei(((Mei) this.data).getFile());
                this.workspace.getApp().getStatuspanel().setMessage("Data reloaded.");
            } catch (IOException | ParsingException | SAXException | ParserConfigurationException e) {
                this.workspace.getApp().getStatuspanel().setMessage(e.toString());
                e.printStackTrace();
            }
        }
        if (this.data instanceof Msm) {
            if (!((Msm) this.data).getFile().exists())
                return;
            try {
                this.data = new Msm(((Msm) this.data).getFile());
                this.workspace.getApp().getStatuspanel().setMessage("Data reloaded.");
            } catch (IOException | ParsingException | SAXException | ParserConfigurationException e) {
                this.workspace.getApp().getStatuspanel().setMessage(e.toString());
                e.printStackTrace();
            }
        }
        if (this.data instanceof Mpm) {
            if (!((Mpm) this.data).getFile().exists())
                return;
            try {
                this.data = new Mpm(((Mpm) this.data).getFile());
                this.workspace.getApp().getStatuspanel().setMessage("Data reloaded.");
            } catch (IOException | ParsingException | SAXException | ParserConfigurationException e) {
                this.workspace.getApp().getStatuspanel().setMessage(e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * add one descendant data object to the workspace
     * @param mouseEvent
     * @param data
     */
    private synchronized void addOneChild(MouseEvent mouseEvent, Object data) {
        if (data == null) return;
        Point2D center = this.getCenterPoint();                                                                     // the center coordinated of this data object
        Point2D clickPoint = this.workspace.getContainer().sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY()); // the click coordinates
        Point2D cn = clickPoint.subtract(center).normalize();                                                       // transformed click coordinates with center shifted to origin and click point vector normalized
        Point2D cp = cn.multiply(Settings.newDataObjectDistance);
        Point2D p = cp.add(center);                                                                                 // from cn go to the actual position at which to display the new data object
        this.makeChild(data, p);
    }


    /**
     * adding more than one descendants requires them to be spread around their parent, this is done here
     * @param mouseEvent
     * @param objects
     */
    private synchronized ArrayList<DataObject> addSeveralChildren(MouseEvent mouseEvent, List objects) {
        return this.addSeveralChildren(mouseEvent.getSceneX(), mouseEvent.getSceneY(), objects);
    }

    /**
     * adding more than one descendants requires them to be spread around their parent, this is done here
     * @param x
     * @param y
     * @param objects
     */
    private synchronized ArrayList<DataObject> addSeveralChildren(double x, double y, List objects) {
        ArrayList<DataObject> children = new ArrayList<>();
        DataObject msm = null;
        if (objects == null) return children;
        Point2D center = this.getCenterPoint();                                                     // the center coordinated of this data object
        Point2D clickPoint = this.workspace.getContainer().sceneToLocal(x, y);                      // the click coordinates
        Point2D cn = clickPoint.subtract(center).normalize();                                       // transformed click coordinates with center shifted to origin and click point vector normalized
        Affine rot = new Affine();                                                                  // initialize an affine transformation (we need a rotation)
        rot.appendRotation(Settings.multipleDataObjectCreationAngle);                               // this sets the rotation angle at which multiple data objects are seperated from each other
        double distance = Settings.newDataObjectDistance;
        for (Object object : objects) {                                                             // for each output Msm
            Point2D p = cn.multiply(distance).add(center);                                          // from cn go to the actual position at which to display the new data object
            distance += Settings.dataItemRadius * 0.1;
            DataObject child = this.makeChild(object, p);
            if (child != null) {
                cn = rot.transform(cn);
                children.add(child);
                if (child.getData() instanceof Msm) {
                    msm = child;
                } else if ((msm != null) && (child.getData() instanceof Mpm)) {
                    DataObject finalMsm = msm;
                    Platform.runLater(() -> {                                                       // add the data object and a connecting line to the workspace (must be done in the JavaFX thread)
                        DataObjectLine line = this.workspace.addDataObjectLine(finalMsm, child);
                        finalMsm.lines.add(line);
                        child.lines.add(line);
                    });
                }
            }
        }
        return children;
    }

    /**
     * this creates an instance of DataObject from the specified object,
     * draws it at the specified position in the workspace,
     * draws a connecting line between this and the new object, and puts the line in both's list of lines
     * @param object
     * @param at
     * @return
     */
    protected synchronized DataObject makeChild(Object object, Point2D at) {
        try {
            DataObject dataObject = new DataObject(object, this.workspace);                                     // embed the Msm data in a DataObject instance
            Platform.runLater(() -> {                                                                           // add the data object and a connecting line to the workspace (must be done in the JavaFX thread)
                this.workspace.addDataObjectAt(dataObject, Math.max(at.getX(), 0), Math.max(at.getY(), 0));
                DataObjectLine line = this.workspace.addDataObjectLine(this, dataObject);
                this.lines.add(line);
                dataObject.lines.add(line);
            });
            return dataObject;
        } catch (InvalidMidiDataException | ParsingException | IOException | UnsupportedAudioFileException | SaxonApiException | SAXException | ParserConfigurationException e) {
            this.workspace.getApp().getStatuspanel().setMessage(e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * a convenience method for placing the graphical representation of this
     * @param x
     * @param y
     */
    protected synchronized void setLayout(double x, double y) {
        double halfTheButtonSize = Settings.font.getSize() * Settings.dataItemSize;
        this.setLayoutX(x - halfTheButtonSize);
        this.setLayoutY(y - halfTheButtonSize);
    }

    /**
     * helper method to get the length of the longest string in the array (just an approximation)
     * @param array
     * @return
     */
    private synchronized double computeVisualLengthOfLongestString(String[] array) {
        int len = 0;
        for (int i = 0; i < array.length; ++i) {
            int s = array[i].length();
            if (s > len)
                len = s;
        }
        return Settings.mWidth * (len + 2) / 2;
    }

    /**
     * returns the center coordinates of this data object's circle
     * @return
     */
    protected synchronized Point2D getCenterPoint() {
        return new Point2D(this.getLayoutX() + Settings.dataItemRadius, this.getLayoutY() + Settings.dataItemRadius);
    }

    /**
     * getter for the color
     * @return
     */
    protected synchronized Color getColor() {
        return this.color;
    }

    /**
     * returns this' workspace
     * @return
     */
    protected synchronized Workspace getWorkspace() {
        return this.workspace;
    }

    private void validate() {
        Schema activeSchema = this.workspace.getActiveSchema();                                     // get active schema
        File schemaFile = (activeSchema != null) ? activeSchema.getFile() : Settings.getSchema();   // is there a schema activated in the workspace or a default schema file?

        URL schemaURL = null;                                                                       // this is needed for validation, must be != null
        if (schemaFile != null) {
            try {
                schemaURL = schemaFile.toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                this.workspace.getApp().getStatuspanel().setMessage("Schema " + schemaFile.getAbsolutePath() + " not accessible.");
            }
        }
        else {
            this.workspace.getApp().getStatuspanel().setMessage("No Schema activated.");
        }

        if (schemaURL != null) {
            String report;
            boolean isValid;
            if (this.data instanceof Mei) {
                Mei mei = (Mei) this.data;
                report = mei.validate(schemaURL);
                isValid = mei.isValid();
            }
            else if (this.data instanceof SvgCollection) {
                String rep = "";
                boolean valid = true;
                SvgCollection svgs = (SvgCollection) this.data;
                for (int i=0; i < svgs.size(); ++i) {
                    Svg svg = svgs.get(i);
                    rep = rep.concat("\n\n---" + "Validation Report for " + svg.getFile().getName() + "------------------------\n\n" + svg.validate(schemaURL));
                    valid = valid && svg.isValid();
                }
                report = rep;
                isValid = valid;
            }
            else if (this.data instanceof Msm) {
                Msm msm = (Msm) this.data;
                report = msm.validate(schemaURL);
                isValid = msm.isValid();
            }
            else if (this.data instanceof Mpm) {
                Mpm mpm = (Mpm) this.data;
                report = mpm.validate(schemaURL);
                isValid = mpm.isValid();
            }
            else if (this.data instanceof TxtData) {
                TxtData txt = (TxtData) this.data;
                boolean validationSuccess = true;
                String reportString = "Passed";
                try {
                    Helper.validateAgainstSchema(txt.getString(), schemaURL);
                } catch (SAXException e) {              // invalid
                    validationSuccess = false;
                    reportString = "Failed. \n" + e.getMessage();
                    e.printStackTrace();                // print the full error message
                } catch (IOException e) {               // missing rng file
                    validationSuccess = false;
                    reportString = "Failed.  Missing schema file!";
                    System.err.println("Validation failed: missing schema file!");
                }
                report = "Validation of " + this.getFileName() + ": " + reportString;
                isValid = validationSuccess;
            }
            else if (this.data instanceof MusicXml) {
                MusicXml mxl = (MusicXml) this.data;
                report = mxl.validate(schemaURL);
                isValid = mxl.isValid();
            }
            else {
                report = "Unknown input datatype.";
                isValid = false;
            }

            Platform.runLater(() -> {                                      // the following stuff must be done by the JavaFX thread
                Alert alert = new Alert(Alert.AlertType.INFORMATION);       // a good documentation on JavaFX dialogs: http://code.makery.ch/blog/javafx-dialogs-official/
                alert.setTitle("Validation Report");
                alert.setHeaderText("Validation of " + this.getFileName());
                alert.setContentText(((isValid) ? "Passed.\n\n" : "Failed.\n\n"));
                TextArea reportArea = new TextArea(report);
                reportArea.setEditable(false);
                reportArea.setWrapText(true);
                alert.getDialogPane().setExpandableContent(reportArea);
                alert.showAndWait();
            });
        }
    }

    /**
     * returns true as long as it is activated
     * @return
     */
    protected boolean isActive() {
        return this.isActive;
    }

    /**
     * triggers the usage of this
     */
    protected synchronized void activate() {
        this.isActive = true;
        StackPane p = (StackPane) this.getChildren().get(this.getChildren().size() - 1);    // make the graphical representation light up
        Glow glow = new Glow(0.8);
        p.setEffect(glow);
    }

    /**
     * when another data object of this type is activated, this one should to be deactivated
     */
    protected synchronized void deactivate() {
        this.isActive = false;
        StackPane p = (StackPane) this.getChildren().get(this.getChildren().size() - 1);    // switch the light off
        p.setEffect(null);
    }
}
