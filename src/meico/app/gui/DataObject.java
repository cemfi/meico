package meico.app.gui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
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
import meico.audio.Audio;
import meico.mei.Helper;
import meico.mei.Mei;
import meico.midi.Midi;
import meico.msm.Msm;
import meico.musicxml.MusicXml;
import meico.pitches.Pitches;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Xslt30Transformer;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.xml.sax.SAXException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents the data objects and provides graphical interface elements for it.
 * To add new items/functionality in a radial menu, add its label text to the corresponding array in makeRadialMenu() and add its functionality to setMenuItemInteraction().
 * @author Axel Berndt
 */
public class DataObject extends Group {

    private Workspace workspace;                        // the workspace in which this is displayed
    private Object data;                                // the actual data (Mei, Msm, Pitches, Midi ...)
    private String label;                               // the text label
    private Color color;
    private Group menu = null;                          // the radial menu will be computed only once and is then readily available
    private boolean menuActive = false;
    private ArrayList<DataObjectLine> lines = new ArrayList<>();    // the list of connections lines to child data objects that were exported from this one
    private ArrayList<Thread> threads = new ArrayList<>();

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
            case ".mei":
                return new Mei(file);
            case ".msm":
                return new Msm(file);
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
                return new meico.app.gui.XSLTransform(file, this);
            case ".rng":
                return new meico.app.gui.Schema(file, this);
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
     * read the label from the data
     * @return
     */
    private synchronized String getFileName() {
        if (this.data instanceof Mei)
            return ((Mei)this.data).getFile().getName();
        else if (this.data instanceof Msm)
            return ((Msm)this.data).getFile().getName();
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
        return "unknown";
    }

    /**
     * returns the title of the encoded music (for MEI and MSM) or the filename (in any other case)
     * @return
     */
    private synchronized String getName() {
        String name = "";
        if (this.data instanceof Mei)
            name = ((Mei)this.data).getTitle();
        else if (this.data instanceof Msm)
            name = ((Msm)this.data).getTitle();
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

        // double clicks are a quick and convenient way to activate and deactivate soundbanks and xslts and to play back midi and audio data
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
                        if (!soundbank.isActive()) {
                            this.workspace.silentDeactivationOfAllSoundbanks();
                            soundbank.activate();
                        }
                        else {
                            soundbank.deactivate();
                        }
                        this.stopComputeAnimation(ani);
                    });
                    this.start(thread);
                }
                else if (this.data instanceof XSLTransform) {
                    Thread thread = new Thread(() -> {
                        RotateTransition ani = this.startComputeAnimation();
                        meico.app.gui.XSLTransform xsltransform = (meico.app.gui.XSLTransform) this.data;
                        if (!xsltransform.isActive()) {
                            this.workspace.deactivateAllXSLTs();
                            xsltransform.activate();
                        }
                        else {
                            xsltransform.deactivate();
                        }
                        this.stopComputeAnimation(ani);
                    });
                    this.start(thread);
                }
                else if (this.data instanceof Schema) {
                    Thread thread = new Thread(() -> {
                        RotateTransition ani = this.startComputeAnimation();
                        meico.app.gui.Schema schema = (meico.app.gui.Schema) this.data;
                        if (!schema.isActive()) {
                            this.workspace.deactivateAllSchemas();
                            schema.activate();
                        }
                        else {
                            schema.deactivate();
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
            String[] leftItems = {"Show", "Validate", "Add IDs", "Resolve Copyofs", "Resolve Expansions", "Reload", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {"to MSM", "to MIDI", "to Audio", "Score Rendering", "XSL Transform"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof Msm) {
            String[] leftItems = {"Show", "Validate", "Remove Rests", "Expand Repetitions", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {"to MIDI", "to Chroma", "to Absolute Pitches", "XSL Transform"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof Midi) {
            String[] leftItems = {"Play", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {"to Audio"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof Audio) {
            String[] leftItems = {"Play", "Save (mp3)", "Save (wav)", "Save As", "Close"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof Pitches) {
            String[] leftItems = {"Show", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof meico.app.gui.Soundbank) {
            String[] leftItems = {"Activate", "Deactivate", "Set As Default", "Close"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof meico.app.gui.XSLTransform) {
            String[] leftItems = {"Activate", "Deactivate", "Set As Default", "Close"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof meico.app.gui.Schema) {
            String[] leftItems = {"Activate", "Deactivate", "Set As Default", "Close"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof TxtData) {
            String[] leftItems = {"Show", "Validate", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {"XSL Transform"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(rightItems);
            for (int i = 0; i < rightItems.length; ++i) {
                Group item = this.makeMenuItem(rightItems[i], -(((float)(rightItems.length - 1) * itemHeight) / 2) + (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
        }
        else if (this.data instanceof MusicXml) {
            String[] leftItems = {"Show", "Validate", "Save", "Save As", "Close"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(leftItems);
            for (int i = 0; i < leftItems.length; ++i) {
                Group item = this.makeMenuItem(leftItems[i], 180 + (((float)(leftItems.length - 1) * itemHeight) / 2) - (i * itemHeight), itemHeight, innerRadius, outerRadius);
                menu.getChildren().add(item);
            }
            String[] rightItems = {/*TODO: "to MEI",*/ "XSL Transform"};
            outerRadius = innerRadius + this.computevisualLengthOfLongestString(rightItems);
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
            this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> this.workspace.remove(this));
            return;
        }

        // object type-specific commands
        if (this.data instanceof Mei) {             // MEI-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.workspace.getApp().getWeb() != null) {
                                Platform.runLater(() -> {
                                    this.workspace.getApp().getWeb().printContent((Helper.prettyXml(((Mei) this.data).toXML())), true);
                                    if (this.workspace.getApp().getWebAccordion() != null) {
                                        this.workspace.getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.workspace.getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
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
                            this.workspace.getApp().getStatuspanel().setMessage(((Mei)this.data).addIds() + " IDs added.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Resolve Copyofs":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            int notResolved = ((Mei)this.data).resolveCopyofs().size();
                            this.workspace.getApp().getStatuspanel().setMessage("Resolving copyofs: " + ((notResolved == 0) ? "done." :  (notResolved + " could not be resolved.")));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Resolve Expansions":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            ((Mei)this.data).resolveExpansions();
                            this.workspace.getApp().getStatuspanel().setMessage("Resolving expansions: done.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Reload":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.reloadMei();
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.workspace.getApp().getStatuspanel().setMessage("Saving MEI data " + ((((Mei)this.data).writeMei(((Mei)this.data).getFile().getAbsolutePath())) ? ("to " + ((Mei)this.data).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Mei mei = ((Mei)this.data);                                     // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(mei.getFile().getParent());          // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(mei.getFile().getName());            // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MEI files (*.mei)", "*.mei"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.workspace.getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.workspace.getApp().getStatuspanel().setMessage("Saving MEI data " + ((mei.writeMei(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                this.stopComputeAnimation(ani);
                            });
                            this.start(thread);
                        }
                    });
                    break;
//                case "Close":
//                    break;
                case "to MSM":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.workspace.getApp().getStatuspanel().setMessage("Converting MEI to MSM ...");
                            List<Msm> msms = ((Mei)this.data).exportMsm(Settings.Mei2Msm_ppq, Settings.Mei2Msm_dontUseChannel10, Settings.Mei2Msm_ignoreExpansions, Settings.Mei2Msm_msmCleanup);   // do the conversion
                            if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addSeveralChildren(mouseEvent, msms);
                                this.workspace.getApp().getStatuspanel().setMessage("Converting MEI to MSM: done.");
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
                            this.workspace.getApp().getStatuspanel().setMessage("Converting MEI to MIDI ...");
                            List<Msm> msms = ((Mei)this.data).exportMsm(Settings.Mei2Msm_ppq, Settings.Mei2Msm_dontUseChannel10, Settings.Mei2Msm_ignoreExpansions, Settings.Mei2Msm_msmCleanup);   // do the conversion
                            if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                ArrayList<Midi> midis = new ArrayList<>();
                                for (Msm msm : msms)
                                    midis.add(msm.exportMidi(Settings.Msm2Midi_defaultTempo, Settings.Msm2Midi_generateProgramChanges));
                                this.addSeveralChildren(mouseEvent, midis);
                                this.workspace.getApp().getStatuspanel().setMessage("Converting MEI to MIDI: done.");
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
                            this.workspace.getApp().getStatuspanel().setMessage("Converting MEI to Audio ...");
                            List<Msm> msms = ((Mei)this.data).exportMsm(Settings.Mei2Msm_ppq, Settings.Mei2Msm_dontUseChannel10, Settings.Mei2Msm_ignoreExpansions, Settings.Mei2Msm_msmCleanup);   // do the conversion
                            if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                ArrayList<Audio> audios = new ArrayList<>();
                                for (Msm msm : msms)
                                    audios.add(msm.exportMidi(Settings.Msm2Midi_defaultTempo, Settings.Msm2Midi_generateProgramChanges).exportAudio(Settings.soundbank));
                                this.addSeveralChildren(mouseEvent, audios);
                                this.workspace.getApp().getStatuspanel().setMessage("Converting MEI to Audio: done.");
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
                            this.workspace.getApp().getStatuspanel().setMessage("MEI Score Rendering via Verovio ...");
//                            this.getWorkspace().getApp().getHostServices().showDocument(getClass().getResource("/resources/Verovio/verovio.html").toString());  // display score rendering in system standard browser, this is not finished as the content of the HTML is never filled with score data
                            // do Verovio score rendering
                            Platform.runLater(() -> {
                                String verovio = null;                                                          // this will get the HTML code to be shown in the WebView
                                verovio = VerovioGenerator.generate(((Mei)this.getData()).toXML(), this);       // generate that HTML code
                                this.getWorkspace().getApp().getWeb().printContent(verovio, false);             // show it in the WebView
                                if (this.workspace.getApp().getWebAccordion() != null) {
                                    this.workspace.getApp().getWebAccordion().setText("Score: " + this.label);  // change the title string of the WebView
                                    this.workspace.getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
                                }
                            });
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "XSL Transform":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.workspace.getApp().getStatuspanel().setMessage("Applying currently active XSLT to MEI ...");
                            XSLTransform activeXslt = this.workspace.getActiveXSLT();
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
                                Mei mei = (Mei)this.data;
                                String string = mei.xslTransformToString(xslt);
                                if (string != null) {
                                    TxtData txt = new TxtData(string, new File(Helper.getFilenameWithoutExtension(mei.getFile().getPath()) + "-" + Helper.getFilenameWithoutExtension(xslFilename) + ".txt"));    // do the xsl transform
                                    this.addOneChild(mouseEvent, txt);
                                    this.workspace.getApp().getStatuspanel().setMessage("Applying currently active XSLT to MEI: done.");
                                }
                            }
                            else {
                                this.workspace.getApp().getStatuspanel().setMessage("No XSL Transform activated.");
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
        else if (this.data instanceof Msm) {        // MSM-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.workspace.getApp().getWeb() != null) {
                                Platform.runLater(() -> {
                                    this.workspace.getApp().getWeb().printContent((Helper.prettyXml(((Msm) this.data).toXML())), true);
                                    if (this.workspace.getApp().getWebAccordion() != null) {
                                        this.workspace.getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.workspace.getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
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
                            ((Msm)this.data).removeRests();
                            this.workspace.getApp().getStatuspanel().setMessage("Rests removed.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Expand Repetitions":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            ((Msm)this.data).resolveSequencingMaps();
                            this.workspace.getApp().getStatuspanel().setMessage("SequencingMaps resolved, repetitions expanded into trough-composed form.");
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.workspace.getApp().getStatuspanel().setMessage("Saving MSM data " + ((((Msm)this.data).writeMsm()) ? ("to " + ((Msm)this.data).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Msm msm = ((Msm)this.data);                                     // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(msm.getFile().getParent());          // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(msm.getFile().getName());            // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MSM files (*.msm)", "*.msm"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.workspace.getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.workspace.getApp().getStatuspanel().setMessage("Saving MSM data " + ((msm.writeMsm(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
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
                            this.workspace.getApp().getStatuspanel().setMessage("Converting MSM to MIDI ...");
                            Midi midi = ((Msm)this.data).exportMidi(Settings.Msm2Midi_defaultTempo, Settings.Msm2Midi_generateProgramChanges);   // do the conversion
                            if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, midi);
                                this.workspace.getApp().getStatuspanel().setMessage("Converting MSM to MIDI: done.");
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
                            this.workspace.getApp().getStatuspanel().setMessage("Converting MSM to Chroma ...");
                            Pitches chroma = ((Msm)this.data).exportChroma();   // do the conversion
                            if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, chroma);
                                this.workspace.getApp().getStatuspanel().setMessage("Converting MSM to Chroma: done.");
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
                            this.workspace.getApp().getStatuspanel().setMessage("Converting MSM to absolute pitches ...");
                            Pitches pitches = ((Msm)this.data).exportPitches();   // do the conversion
                            if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, pitches);
                                this.workspace.getApp().getStatuspanel().setMessage("Converting MSM to absolute pitches: done.");
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
                            this.workspace.getApp().getStatuspanel().setMessage("Applying currently active XSLT to MSM ...");
                            XSLTransform activeXslt = this.workspace.getActiveXSLT();
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
                                Msm msm = (Msm)this.data;
                                String string = msm.xslTransformToString(xslt);
                                if (string != null) {
                                    TxtData txt = new TxtData(string, new File(Helper.getFilenameWithoutExtension(msm.getFile().getPath()) + "-" + Helper.getFilenameWithoutExtension(xslFilename) + ".txt"));    // do the xsl transform
                                    this.addOneChild(mouseEvent, txt);
                                    this.workspace.getApp().getStatuspanel().setMessage("Applying currently active XSLT to MSM: done.");
                                }
                            }
                            else {
                                this.workspace.getApp().getStatuspanel().setMessage("No XSL Transform activated.");
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
        else if (this.data instanceof Midi) {       // MIDI-specific commands
            switch (command) {
                case "Play":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> this.workspace.getApp().getPlayer().play((Midi)this.data));
                    break;
                case "Save":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            this.workspace.getApp().getStatuspanel().setMessage("Saving MIDI data " + ((((Midi)this.data).writeMidi()) ? ("to " + ((Midi)this.data).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Midi midi = ((Midi)this.data);                                  // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(midi.getFile().getParent());         // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(midi.getFile().getName());           // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MIDI files (*.mid)", "*.mid"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.workspace.getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.workspace.getApp().getStatuspanel().setMessage("Saving MIDI data " + ((midi.writeMidi(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
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
                            this.workspace.getApp().getStatuspanel().setMessage("Converting MIDI to audio ...");
                            Audio audio = ((Midi)this.data).exportAudio(this.workspace.getActiveSoundbank());   // do the conversion
                            if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, audio);
                                this.workspace.getApp().getStatuspanel().setMessage("Converting MIDI to audio: done.");
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
        else if (this.data instanceof Audio) {      // Audio-specific commands
            switch (command) {
                case "Play":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> this.workspace.getApp().getPlayer().play((Audio)this.data));
                    break;
                case "Save (mp3)":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            this.workspace.getApp().getStatuspanel().setMessage("Saving audio data as MP3 ...");
                            RotateTransition ani = this.startComputeAnimation();
                            boolean success = ((Audio)this.data).writeMp3();
                            if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.workspace.getApp().getStatuspanel().setMessage("Saving audio data as MP3 " + ((success) ? ("to " + Helper.getFilenameWithoutExtension(((Audio) this.data).getFile().getAbsolutePath()) + ".mp3.") : "failed."));
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save (wav)":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            this.workspace.getApp().getStatuspanel().setMessage("Saving audio data ...");
                            RotateTransition ani = this.startComputeAnimation();
                            boolean success = ((Audio)this.data).writeAudio();
                            if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.workspace.getApp().getStatuspanel().setMessage("Saving audio data " + ((success) ? ("to " + ((Audio) this.data).getFile().getAbsolutePath() + ".") : "failed."));
                            }
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Audio audio = ((Audio)this.data);                               // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(audio.getFile().getParent());        // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(audio.getFile().getName());          // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Wave files (*.wav)", "*.wav"), new FileChooser.ExtensionFilter("MP3 files (*.mp3)", "*.mp3"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.workspace.getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            String type = file.getName();
                            type = type.substring(type.lastIndexOf(".")).toLowerCase();
                            switch (type) {
//                                case ".wav":  // default behavior
//                                    break;
                                case ".mp3": {
                                    this.workspace.getApp().getStatuspanel().setMessage("Saving audio data as MP3 ...");
                                    Thread thread = new Thread(() -> {
                                        RotateTransition ani = this.startComputeAnimation();
                                        boolean success = audio.writeMp3(file.getAbsolutePath());
                                        if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                            this.workspace.getApp().getStatuspanel().setMessage("Saving audio data as MP3" + ((success) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
                                        }
                                        this.stopComputeAnimation(ani);
                                    });
                                    this.start(thread);
                                    break;
                                }
                                default: {
                                    this.workspace.getApp().getStatuspanel().setMessage("Saving audio data ...");
                                    Thread thread = new Thread(() -> {
                                        RotateTransition ani = this.startComputeAnimation();
                                        boolean success = audio.writeAudio(file.getAbsolutePath());
                                        if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                            this.workspace.getApp().getStatuspanel().setMessage("Saving audio data " + ((success) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
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
        else if (this.data instanceof Pitches) {    // Pitches-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.workspace.getApp().getWeb() != null) {
                                Platform.runLater(() -> {
                                    this.workspace.getApp().getWeb().printContent(((Pitches) this.data).getAsString(true), true);
                                    if (this.workspace.getApp().getWebAccordion() != null) {
                                        this.workspace.getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.workspace.getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
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
                            this.workspace.getApp().getStatuspanel().setMessage("Saving JSON data " + ((((Pitches)this.data).writePitches(Settings.savePitchesWithPrettyPrint)) ? ("to " + ((Pitches)this.data).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Pitches pitches = ((Pitches)this.data);                         // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(pitches.getFile().getParent());      // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(pitches.getFile().getName());        // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.workspace.getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.workspace.getApp().getStatuspanel().setMessage("Saving pitch data " + ((pitches.writePitches(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
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
        else if (this.data instanceof meico.app.gui.Soundbank) {    // Soundbank-specific commands
            switch (command) {
                case "Set As Default":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            meico.app.gui.Soundbank soundbank = (meico.app.gui.Soundbank)this.data;
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
                            meico.app.gui.Soundbank soundbank = (meico.app.gui.Soundbank)this.data;
                            if (!soundbank.isActive()) {
                                this.workspace.silentDeactivationOfAllSoundbanks();
                                soundbank.activate();
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
                            meico.app.gui.Soundbank soundbank = (meico.app.gui.Soundbank)this.data;
                            if (soundbank.isActive()) {
                                soundbank.deactivate();
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
        else if (this.data instanceof meico.app.gui.XSLTransform) {    // XSLT-specific commands
            switch (command) {
                case "Set As Default":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            meico.app.gui.XSLTransform xsltransform = (meico.app.gui.XSLTransform) this.data;
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
                            meico.app.gui.XSLTransform xsltransform = (meico.app.gui.XSLTransform) this.data;
                            if (!xsltransform.isActive()) {
                                this.workspace.deactivateAllXSLTs();
                                xsltransform.activate();
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
                            meico.app.gui.XSLTransform xsltransform = (meico.app.gui.XSLTransform) this.data;
                            if (xsltransform.isActive()) {
                                xsltransform.deactivate();
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
        else if (this.data instanceof meico.app.gui.Schema) {    // XML schema-specific commands
            switch (command) {
                case "Set As Default":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            meico.app.gui.Schema schema = (meico.app.gui.Schema) this.data;
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
                            meico.app.gui.Schema schema = (meico.app.gui.Schema) this.data;
                            if (!schema.isActive()) {
                                this.workspace.deactivateAllSchemas();
                                schema.activate();
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
                            meico.app.gui.Schema schema = (meico.app.gui.Schema) this.data;
                            if (schema.isActive()) {
                                schema.deactivate();
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
        else if (this.data instanceof TxtData) {    // txt file-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            Platform.runLater(() -> {
                                if (this.workspace.getApp().getWeb() != null) {
                                    this.workspace.getApp().getWeb().printContent(((TxtData) this.data).getString(), true);
                                    if (this.workspace.getApp().getWebAccordion() != null) {
                                        this.workspace.getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.workspace.getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
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
                            this.workspace.getApp().getStatuspanel().setMessage("Saving TXT data " + ((((TxtData)this.data).writeTxtData()) ? ("to " + ((TxtData)this.data).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        TxtData txt = (TxtData) this.data;
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(txt.getFile().getParent());          // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(txt.getFile().getName());            // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.workspace.getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.workspace.getApp().getStatuspanel().setMessage("Saving pitch data " + ((txt.writeTxtData(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
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
                            this.workspace.getApp().getStatuspanel().setMessage("Applying currently active XSLT to text data ...");
                            XSLTransform activeXslt = this.workspace.getActiveXSLT();
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
                            if ((xslt != null) && (!((TxtData) this.data).getString().isEmpty())) {
                                // try interpreting the string as xml data
                                Builder builder = new Builder(false);                       // we leave the validate argument false as XOM's built-in validator does not support RELAX NG
                                Document input = null;
                                try {
                                    input = builder.build(new ByteArrayInputStream(((TxtData)this.data).getString().getBytes(StandardCharsets.UTF_8)));
                                } catch (ValidityException e) {                             // in case of a ValidityException
                                    input = e.getDocument();                                // make the XOM Document anyway, we may nonetheless be able to work with it
                                } catch (ParsingException | IOException e) {
                                    this.workspace.getApp().getStatuspanel().setMessage(e.toString());
                                    e.printStackTrace();
                                    input = null;
                                }
                                if (input != null) {
                                    String string = Helper.xslTransformToString(input, xslt);
                                    if (string != null) {
                                        TxtData txt = new TxtData(string, new File(Helper.getFilenameWithoutExtension(((TxtData)this.data).getFile().getPath()) + "-" + Helper.getFilenameWithoutExtension(xslFilename) + ".txt"));    // do the xsl transform
                                        this.addOneChild(mouseEvent, txt);
                                        this.workspace.getApp().getStatuspanel().setMessage("Applying currently active XSLT to text: done.");
                                    }
                                }
                            }
                            else {
                                this.workspace.getApp().getStatuspanel().setMessage("No XSL Transform activated.");
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
        else if (this.data instanceof MusicXml) {    // txt file-specific commands
            switch (command) {
                case "Show":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        Thread thread = new Thread(() -> {
                            RotateTransition ani = this.startComputeAnimation();
                            if (this.workspace.getApp().getWeb() != null) {
                                Platform.runLater(() -> {
                                    this.workspace.getApp().getWeb().printContent((Helper.prettyXml(((MusicXml) this.data).toXML())), true);
                                    if (this.workspace.getApp().getWebAccordion() != null) {
                                        this.workspace.getApp().getWebAccordion().setText(this.label);              // change the title string of the WebView
                                        this.workspace.getApp().getWebAccordion().setExpanded(true);                // auto-expand the WebAccordion
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
                            this.workspace.getApp().getStatuspanel().setMessage("Saving MusicXML data " + ((((MusicXml)this.data).writeMusicXml()) ? ("to " + ((MusicXml)this.data).getFile().getAbsolutePath() + ".") : "failed."));
                            this.stopComputeAnimation(ani);
                        });
                        this.start(thread);
                    });
                    break;
                case "Save As":
                    this.menuItemInteractionGeneric(item, label, body, (MouseEvent mouseEvent) -> {
                        MusicXml mxl = ((MusicXml)this.data);                                     // the object
                        FileChooser chooser = new FileChooser();                        // the file chooser to select file location and name
                        File initialDir = new File(mxl.getFile().getParent());          // get the directory of the object's source file
                        if (!initialDir.exists())                                       // if that does not exist
                            initialDir = new File(System.getProperty("user.dir"));      // get the current working directory
                        chooser.setInitialDirectory(initialDir);                        // set the chooser's initial directory
                        chooser.setInitialFileName(mxl.getFile().getName());            // set the initial filename
                        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MusicXML files (*.musicxml)", "*.musicxml"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
                        File file = chooser.showSaveDialog(this.workspace.getApp().getStage());   // show the save dialog
                        if (file != null) {                                             // if it returns a file to save the data, do the save operation
                            Thread thread = new Thread(() -> {
                                RotateTransition ani = this.startComputeAnimation();
                                this.workspace.getApp().getStatuspanel().setMessage("Saving MSM data " + ((mxl.writeMusicXml(file.getAbsolutePath())) ? ("to " + file.getAbsolutePath() + ".") : "failed."));
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
                            this.workspace.getApp().getStatuspanel().setMessage("Converting MusicXML to MEI ...");
                            Mei mei = ((MusicXml)this.data).exportMei(Settings.useLatestVerovio);   // do the conversion
                            if (this.workspace != null) {                   // it is possible that the data object has been removed from workspace in the meantime
                                this.addOneChild(mouseEvent, mei);
                                this.workspace.getApp().getStatuspanel().setMessage("Converting MusicXML to MEI: done.");
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
                            this.workspace.getApp().getStatuspanel().setMessage("Applying currently active XSLT to MEI ...");
                            XSLTransform activeXslt = this.workspace.getActiveXSLT();
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
                                MusicXml mxl = (MusicXml)this.data;
                                String string = mxl.xslTransformToString(xslt);
                                if (string != null) {
                                    TxtData txt = new TxtData(string, new File(Helper.getFilenameWithoutExtension(mxl.getFile().getPath()) + "-" + Helper.getFilenameWithoutExtension(xslFilename) + ".txt"));    // do the xsl transform
                                    this.addOneChild(mouseEvent, txt);
                                    this.workspace.getApp().getStatuspanel().setMessage("Applying currently active XSLT to MEI: done.");
                                }
                            }
                            else {
                                this.workspace.getApp().getStatuspanel().setMessage("No XSL Transform activated.");
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
     * what the item actually does must be provided via parameter action
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
     * after converting MEI to something else, its data is altered, this method is called to get the original version
     */
    private void reloadMei() {
        if (this.data instanceof Mei) {
            try {
                this.data = new Mei(((Mei) this.data).getFile());
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
    private synchronized void addSeveralChildren(MouseEvent mouseEvent, List objects) {
        if (objects == null) return;
        Point2D center = this.getCenterPoint();                                                                     // the center coordinated of this data object
        Point2D clickPoint = this.workspace.getContainer().sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY()); // the click coordinates
        Point2D cn = clickPoint.subtract(center).normalize();                                                       // transformed click coordinates with center shifted to origin and click point vector normalized
        Affine rot = new Affine();                                                                                  // initialize an affine transformation (we need a rotation)
        rot.appendRotation(Settings.multipleDataObjectCreationAngle);                                               // this sets the rotation angle at which multiple data objects are seperated from each other
        for (Object object : objects) {                                                                             // for each output Msm
            Point2D p = cn.multiply(Settings.newDataObjectDistance).add(center);                                    // from cn go to the actual position at which to display the new data object
            if (this.makeChild(object, p))
                cn = rot.transform(cn);
        }
    }

    /**
     * this creates an instance of DataObject from the specified object,
     * draws it at the specified position in the workspace,
     * draws a connecting line between this and the new object, and puts the line in both's list of lines
     * @param object
     * @param at
     * @return
     */
    protected synchronized boolean makeChild(Object object, Point2D at) {
        boolean returnValue = true;
        try {
            DataObject dataObject = new DataObject(object, this.workspace);                                     // embed the Msm data in a DataObject instance
            Platform.runLater(() -> {                                                                           // add the data object and a connecting line to the workspace (must be done in the JavaFX thread)
                this.workspace.addDataObjectAt(dataObject, Math.max(at.getX(), 0), Math.max(at.getY(), 0));
                DataObjectLine line = this.workspace.addDataObjectLine(this, dataObject);
                this.lines.add(line);
                dataObject.lines.add(line);
            });
        } catch (InvalidMidiDataException | ParsingException | IOException | UnsupportedAudioFileException | SaxonApiException | SAXException | ParserConfigurationException e) {
            this.workspace.getApp().getStatuspanel().setMessage(e.toString());
            e.printStackTrace();
            returnValue = false;
        }
        return returnValue;
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
    private synchronized double computevisualLengthOfLongestString(String[] array) {
        int len = 0;
        for (int i = 0; i < array.length; ++i) {
            int s = array[i].length();
            if (s > len)
                len = s;
        }
        double length = Settings.mWidth * (len + 2) / 2;
        return length;
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
            else if (this.data instanceof Msm) {
                Msm msm = (Msm) this.data;
                report = msm.validate(schemaURL);
                isValid = msm.isValid();
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
}
