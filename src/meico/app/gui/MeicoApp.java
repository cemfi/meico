package meico.app.gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import meico.Meico;

import java.io.*;

/**
 * This is a standalone application of meico with a graphical user interface.
 * It is based on JavaFX 2.
 * @author Axel Berndt.
 */
public class MeicoApp extends Application {
    private Stage stage;
    private StatusPanel statuspanel;
    private Player player;
    private TitledPane playerAccordion;
    private Workspace workspace;
    private WebBrowser web = null;
    protected TitledPane webAccordion;

    /**
     * the main method to compile and run meico as a JavaFX application
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        MeicoApp.launch(MeicoApp.class);    // this is the minimal call to launch meico's gui
    }

    /**
     * JavaFX init method is called when the application starts
     */
    @Override
    public void init() {
//        super.init();     // not necessary as it does nothing

        System.setProperty("prism.order", "sw");

        try {
            Settings.readSettings();
        } catch (IOException e) {
            System.err.println("File meico.cfg not found.");
        }

        // in window mode all the command line output and error messages are redirected to a log file, if a filename is given in Settings
//        if ((this.getParameters().getUnnamed().size() >= 2) && !(this.getParameters().getUnnamed().get(1).isEmpty())) { // this can be used to deliver the log file name as third parameter in MeicoApp.launch()
        if (Settings.makeLogfile && !Settings.logfile.isEmpty()) {                                                      // is there a nonempty string?
            try {
//                FileOutputStream log = new FileOutputStream(this.getParameters().getUnnamed().get(1));                  // use the string as filename
                FileOutputStream log = new FileOutputStream(Settings.logfile);                                          // use the string as filename
                PrintStream out = new PrintStream(log);                                                                 // make a PrintStream that outputs to the FileOutputStream that fills the log file
                System.setOut(out);                                                                                     // redirect the System.out stream to the PrintStream so that all console output goes to the log file
                System.setErr(out);                                                                                     // redirect the System.err stream to the PrintStream so that all console output goes to the log file
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * JavaFX start method is called after init()
     * @param stage
     */
    public void start(Stage stage) {
        System.out.println("Meico: MEI Converter version " + Meico.version + "\nrunning on " + System.getProperty("os.name") + " version " + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + "\nJava version " + System.getProperty("java.version") + "\nJavaFX version " + System.getProperty("javafx.version"));

        // load current settings from settings file if one is present, otherwise keep the initial settings in class Settings
        this.stage = stage;
        this.stage.setTitle((this.getParameters().getUnnamed().isEmpty() || this.getParameters().getUnnamed().get(1).isEmpty()) ? "Meico: MEI Converter " + meico.Meico.version : this.getParameters().getUnnamed().get(0));   // if the first argument is a nonempty string, it is used as window title
        this.stage.getIcons().add(new Image(this.getClass().getResource("/resources/graphics/meico_icon_flat.png").toString()));    // add an icon to the window header
//        stage.setFullScreen(true);                                                              // set on fullscreen

        // set the window's minimal dimensions
        this.stage.setMinWidth(300);
        this.stage.setMinHeight(300);

        // set the window's initial dimensions
        this.stage.setWidth(Settings.defaultWindowWidth);
        this.stage.setHeight(Settings.defaultWindowHeight);

        // layout
        VBox root = new VBox();
        root.setAlignment(Pos.BOTTOM_LEFT);
        root.setStyle(Settings.BACKGROUND_DARKGRAY);

        // contents
        this.statuspanel = new StatusPanel(this);
        this.player = new Player(this);
        this.web = new WebBrowser(this);
        this.workspace = new Workspace(this);

        // put contents into an accordion interface
        this.playerAccordion = new TitledPane();
        this.playerAccordion.setStyle(Settings.ACCORDION);
        this.playerAccordion.setText("Player & Settings");
        this.playerAccordion.setExpanded(false);
        this.playerAccordion.setAnimated(Settings.accordionAnimations);
        if (Settings.autoExpandPlayerOnMouseOver) {
            // automatically expand the player at mouse over
            this.playerAccordion.setOnMouseEntered(event -> {
                this.playerAccordion.setExpanded(true);
                event.consume();
            });
            // auto-collapse when mouse exits
            this.playerAccordion.setOnMouseExited(event -> {
                this.playerAccordion.setExpanded(false);
                event.consume();
            });
        }
        this.playerAccordion.setContent(this.player);

        root.getChildren().add(this.workspace);                                         // the sequence for adding the components is: workspace, (webAccordion), playerAcordion, statuspanel

        if (Settings.useInternalWebView) {
            this.webAccordion = new TitledPane();
            this.webAccordion.setStyle(Settings.ACCORDION);
            this.webAccordion.setText("WebView");
            this.webAccordion.setExpanded(false);
            this.webAccordion.setAnimated(Settings.accordionAnimations);
            this.webAccordion.setContent(this.web);
            root.getChildren().add(this.webAccordion);
        }

        root.getChildren().addAll(this.playerAccordion, this.statuspanel);

        // put all into a scene graph and display
        Scene scene = new Scene(root/*, 1500, 1000, new Color(0.1, 0.1, 0.1, 1.0)*/);   // the scene is the container for the scene graph, root is the root element of the scene graph
//        this.setFileDrop(scene);                                                        // assign a file drop listener to the scene
        this.stage.setScene(scene);                                                     // assign the scene to the stage
        this.stage.show();                                                              // display the stage, this creates the window and its content

        // keyboard input
        this.setKeyEventListener(this.stage);                                           // assign a key listener to the stage to close the application on ESC
    }

    /**
     * JavaFX stop method is called when the application closes
     */
    @Override
    public void stop() {
        Settings.writeSettingsFile();   // Save the current state of the settings to the settings file
        this.player.close();            // properly close the player
        System.exit(0);                 // this also terminates all threads
    }

    /**
     * Helper method that sets a listener to keyboard events
     * @param stage
     */
    private void setKeyEventListener(Stage stage) {
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCode() == KeyCode.ESCAPE) { // on ESC
                    stage.close();                  // terminate the application
                    t.consume();
                    return;
                }
                if ((t.getCode() == KeyCode.SPACE) || (t.getCode() == KeyCode.ENTER) || (t.getCode().ordinal() == 187)) {   // SPACE does not seem to work on some systems, alternative keys are ENTER and the play/pause key (one of the extra media keys on some keyboards)
                    if (player.isPlaying()) {
                        player.stop();
                    }
                    else {
                        player.play();
                    }
                    t.consume();
                    return;
                }
                t.consume();
            }
        });
    }

    /**
     * access to the statuspanel
     * @return
     */
    protected synchronized StatusPanel getStatuspanel() {
        return this.statuspanel;
    }

    /**
     * access the player
     * @return
     */
    protected synchronized Player getPlayer() {
        return this.player;
    }

    /**
     * access the stage
     * @return
     */
    protected synchronized Stage getStage() {
        return this.stage;
    }

    /**
     * access the webview
     * @return
     */
    protected synchronized WebBrowser getWeb() {
        return this.web;
    }

    /**
     * access the webAccordion (e.g. to auto-expand it when loading a score rendering)
     * @return
     */
    protected synchronized TitledPane getWebAccordion() {
        return this.webAccordion;
    }

    /**
     * access the workspace
     * @return
     */
    protected synchronized Workspace getWorkspace() {
        return this.workspace;
    }

    /**
     * switch the auto expansion of the player on or off
     * @param autoExpand
     */
    protected synchronized void setPlayerAccordion(boolean autoExpand) {
        this.playerAccordion.setOnMouseEntered(event -> {
            if (autoExpand)
                this.playerAccordion.setExpanded(true);
            event.consume();
        });
        // auto-collapse when mouse exits
        this.playerAccordion.setOnMouseExited(event -> {
            if (autoExpand)
                this.playerAccordion.setExpanded(false);
            event.consume();
        });
    }

    protected synchronized void setAccordionAnimation(boolean animate) {
        this.playerAccordion.setAnimated(animate);
        this.webAccordion.setAnimated(animate);
    }

//    /**
//     * This sets file drop frunctionality to a Scene object.
//     * @param scene
//     */
//    private void setFileDrop(Scene scene) {
//        scene.setOnDragOver(new EventHandler<DragEvent>() {
//            @Override
//            public void handle(DragEvent event) {
//                Dragboard dragboard = event.getDragboard();
//                if (dragboard.hasFiles()) {
//                    event.acceptTransferModes(TransferMode.MOVE);
//                } else {
//                    event.consume();
//                }
//            }
//        });
//
//        // Dropping over surface
//        scene.setOnDragDropped(new EventHandler<DragEvent>() {
//            @Override
//            public void handle(DragEvent event) {
//                Dragboard dragboard = event.getDragboard();
//                boolean success = false;
//                if (dragboard.hasFiles()) {
//                    success = true;
//                    String filePath = null;
//                    for (File file : dragboard.getFiles()) {
//                        filePath = file.getAbsolutePath();
//                        statuspanel.setMessage("File drop " + filePath);
//                        if (file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase().equals(".wav")) {
//                            try {
//                                Audio audio = new Audio(file);
//                                player.play(audio);
//                            } catch (IOException | UnsupportedAudioFileException e) {
//                                e.printStackTrace();
//                            }
//                        } else if (file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase().equals(".mid")) {
//                            try {
//                                Midi midi = new Midi(file);
//                                player.play(midi);
//                            } catch (InvalidMidiDataException | IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//                event.setDropCompleted(success);
//                event.consume();
//            }
//        });
//    }

}
