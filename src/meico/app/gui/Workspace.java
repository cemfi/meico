package meico.app.gui;

import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.sf.saxon.s9api.SaxonApiException;
import nu.xom.ParsingException;
import org.xml.sax.SAXException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This part of the interface will be the place where all the data is shown, dropped, converted, ...
 * @author Axel Berndt
 */
class Workspace extends ScrollPane {
    private MeicoApp app;                                           // this is a link to the parent application
    private Pane container;                                         // this is the "drawing area", all contents go in here, Pane features a better behavior than Group in this context (resizes only in positive direction, Pane local bounds start always at (0, 0) in top left whereas Group's bounds start at the top-leftmost child at (0+x, 0+y)).
    private StackPane welcomeMessage;
    private ArrayList<DataObject> data = new ArrayList<>();         // this holds the data that is presented in the workspace area
    private ArrayList<DataObject> soundbanks = new ArrayList<>();   // the list of soundbanks that are present in the workspace (they are also contained in this.data)
    private ArrayList<DataObject> xslts = new ArrayList<>();        // the list of XSLTs that are present in the workspace (they are also contained in this.data)
    private ArrayList<DataObject> schemas = new ArrayList<>();      // the list of schemas (RNGs) that are present in the workspace (they are also contained in this.data)

    public Workspace(MeicoApp app) {
        super();
        this.app = app;                                         // link to the parent app

        // layout
        this.setStyle(Settings.WORKSPACE);                      // this is strange but necessary to set the "whole" background transparent
        VBox.setVgrow(this, Priority.ALWAYS);                   // audomatically maximize the height of this ScrollPane
//        this.setMaxWidth(Double.MAX_VALUE);                     // also maximize its width (this happens automatically within a VBox)
        this.setPannable(true);                                 // enable panning in this ScrollPane

        this.container = new Pane();                            // create the "drawing area"

        this.welcomeMessage = this.makeWelcomeMessage();        // make a welcome message that shows which file types can be imported
        this.container.getChildren().add(this.welcomeMessage);  // add the message to the container so it will be displayed

        this.setFileDrop();                                     // set file drop functionality on the workspace
        this.setContent(this.container);                        // set it as the content of the scrollpane
    }

    /**
     * make a welcome message that shows which file types can be imported
     * @return
     */
    private synchronized StackPane makeWelcomeMessage() {
        // the message text
        Label message = new Label(Settings.WELCOME_MESSAGE);    // the welcome message text as label
        message.setFont(Settings.font);                         // set font
        message.setStyle(Settings.WELCOME_MESSAGE_STYLE);       // set style

        // the file drop icon
        Label dropIcon = new Label("\uf56f");                   // icon
        dropIcon.setFont(Settings.getIconFont(120, this));      // icon size
        dropIcon.setStyle(Settings.WELCOME_MESSAGE_COLOR);      // style
        dropIcon.setTranslateX(-12.0);                          // center aethetically
        dropIcon.setTranslateY(-4.0);                           // center aethetically

        // dashed rectangle arround drop icon
        Rectangle rectangle = new Rectangle(250, 250);          // dimensions
        rectangle.setArcWidth(86.0);                            // rounded corner
        rectangle.setArcHeight(86.0);                           // rounded corner
        rectangle.setFill(new Color(0, 0, 0, 0.0));             // background transparent
        rectangle.setStroke(new Color(1, 1, 1, 0.25));          // stroke
        rectangle.setStrokeWidth(7.0);                          // stroke width
        rectangle.getStrokeDashArray().addAll(17.0, 27.0);      // dash it
//        rectangle.setStrokeDashOffset(25.0);

        // stack all the above elements together
        StackPane pane = new StackPane();
        pane.getChildren().addAll(rectangle, dropIcon, message);
        pane.layoutXProperty().bind(this.widthProperty().subtract(pane.widthProperty()).divide(2));   // center the message in the workspace
        pane.layoutYProperty().bind(this.heightProperty().subtract(pane.heightProperty()).divide(2)); // center the message in the workspace

        // scale the whole thing with the height of the stage
        pane.scaleXProperty().bind(this.heightProperty().divide(800));
        pane.scaleYProperty().bind(this.heightProperty().divide(800));

        return pane;
    }

    /**
     * get the container that holds all contents of the workspace
     * @return
     */
    protected synchronized Pane getContainer() {
        return this.container;
    }

    /**
     * access the app
     * @return
     */
    protected synchronized MeicoApp getApp() {
        return this.app;
    }

    /**
     * remove all data objects from the workspace (more precisely from the container)
     */
    protected synchronized void clearAll() {
        for (DataObject object : this.data)
            object.clear();
        this.container.getChildren().clear();
        this.data.clear();
        this.soundbanks.clear();
        this.xslts.clear();
        this.schemas.clear();
        this.app.getPlayer().setSoundbank(null);
        System.gc();    // force garbage collector to do its job
    }

    /**
     * removes the specified object from the workspace
     * @param object
     */
    protected synchronized void remove(DataObject object) {
        this.data.remove(object);

        if (object.getDataType() == meico.app.gui.Soundbank.class)
            this.soundbanks.remove(object);
        else if (object.getDataType() == meico.app.gui.XSLTransform.class)
            this.xslts.remove(object);
        else if (object.getDataType() == Schema.class)
            this.schemas.remove(object);

        this.container.getChildren().remove(object);
        object.clear();
//        System.gc();    // force garbage collector to do its job
    }

    /**
     * create a data object from a given file
     * @param data
     * @return
     * @throws InvalidMidiDataException
     * @throws ParsingException
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    private synchronized DataObject makeDataObject(Object data) throws InvalidMidiDataException, ParsingException, IOException, UnsupportedAudioFileException, SaxonApiException, SAXException, ParserConfigurationException {
        return new DataObject(data, this);
    }

    /**
     * add a data object to the workspace
     * @param dataObject
     * @param x
     * @param y
     */
    protected synchronized void addDataObjectAt(DataObject dataObject, double x, double y) {
        dataObject.setLayout(x, y);
        this.container.getChildren().add(dataObject);
        this.data.add(dataObject);

        if (dataObject.getDataType() == meico.app.gui.Soundbank.class)            // if it is a soundbank
            this.addToSoundbanks(dataObject);                       // add it also to the soundbanks
        else  if (dataObject.getDataType() == XSLTransform.class)   // if it is an xslt
            this.addToXSLTs(dataObject);                            // add it also to the xslts
        else  if (dataObject.getDataType() == Schema.class)   // if it is an xslt
            this.addToSchemas(dataObject);                            // add it also to the xslts
    }

    /**
     * draws a line between two data objects
     * @param parent
     * @param child
     */
    protected synchronized DataObjectLine addDataObjectLine(DataObject parent, DataObject child) {
        DataObjectLine line = new DataObjectLine(parent, child);
        this.container.getChildren().add(0, line);                  // add it behind all other elements so that it does not get in the way with mouse interaction
        return line;
    }

    /**
     * this adds the given DataObject to the soundbanks list
     * @param soundbank
     */
    protected synchronized void addToSoundbanks(DataObject soundbank) {
        this.soundbanks.add(soundbank);
    }

    /**
     * This deactivates all soundbanks in the workspace, but only visually.
     * It does not force the midiplayer to load a default soundbank so the one
     * that is currently loaded will still be used for MIDI synthesis.
     * But now another soundbank can be loaded and set active without worrying about the others.
     */
    protected synchronized void silentDeactivationOfAllSoundbanks() {
        for (DataObject o : this.soundbanks) {
            ((meico.app.gui.Soundbank)o.getData()).silentDeactivation();
        }
    }

    /**
     * returns the soundbank that is currently used for MIDI synthesis so that it may also be used for MIDI to audio rendering
     * @return
     */
    protected File getActiveSoundbank() {
        for (DataObject o : this.soundbanks) {
            meico.app.gui.Soundbank s = (meico.app.gui.Soundbank)o.getData();
            if (s.isActive())
                return s.getFile();
        }
        return Settings.soundbank;
    }

    /**
     * this adds the given DataObject to the xslts list
     * @param xslt
     */
    protected synchronized void addToXSLTs(DataObject xslt) {
        this.xslts.add(xslt);
    }

    /**
     * returns the XSLTransform that is currently activated
     * @return
     */
    protected XSLTransform getActiveXSLT() {
        for (DataObject o : this.xslts) {
            meico.app.gui.XSLTransform x = (meico.app.gui.XSLTransform)o.getData();
            if (x.isActive())
                return x;
        }
        return null;
    }

    /**
     * This deactivates all xsl transforms.
     */
    protected synchronized void deactivateAllXSLTs() {
        for (DataObject o : this.xslts) {
            ((meico.app.gui.XSLTransform)o.getData()).deactivate();
        }
    }

    /**
     * this adds the given DataObject to the schemas list
     * @param schema
     */
    protected synchronized void addToSchemas(DataObject schema) {
        this.schemas.add(schema);
    }

    /**
     * returns the SchemaFile that is currently activated
     * @return
     */
    protected Schema getActiveSchema() {
        for (DataObject o : this.schemas) {
            Schema schema = (Schema)o.getData();
            if (schema.isActive())
                return schema;
        }
        return null;
    }

    /**
     * This deactivates all schemas.
     */
    protected synchronized void deactivateAllSchemas() {
        for (DataObject o : this.schemas) {
            ((Schema)o.getData()).deactivate();
        }
    }

    /**
     * This sets file drop frunctionality to a Region object.
     */
    private synchronized void setFileDrop() {
        this.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // Dropping over surface
        this.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                success = true;
                String filePath = null;
                for (File file : dragboard.getFiles()) {
                    filePath = file.getAbsolutePath();
                    this.app.getStatuspanel().setMessage("File drop " + filePath);
                    Point2D local = this.container.sceneToLocal(event.getSceneX(), event.getSceneY());// get local drop coordinates in container
                    try {
                        DataObject data = this.makeDataObject(file);
                        this.addDataObjectAt(data, local.getX(), local.getY());

                        // once the user successfully drops/imports the first file the welcome message is removed and set null to free memory and avoid expensive tests
                        if (this.welcomeMessage != null) {
                            this.container.getChildren().remove(this.welcomeMessage);
                            this.welcomeMessage = null;
                        }
                    } catch (ParsingException | InvalidMidiDataException | IOException | UnsupportedAudioFileException | SaxonApiException | SAXException | ParserConfigurationException e) {
                        this.app.getStatuspanel().setMessage(e.toString());
                        e.printStackTrace();
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
