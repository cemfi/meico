package meico.app.gui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import meico.mei.Helper;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Xslt30Transformer;

import java.io.*;

/**
 * This provides some basic style definitions.
 * @author Axel Berndt
 */
class Settings {
    // fxml strings to be used in the setStyle() method of JavaFX objects
    protected static final String BACKGROUND_DARKGRAY = "-fx-background-color: #1a1a1a;";
    protected static final String BACKGROUND_GRAY = "-fx-background-color: #2b2b2b;";
    protected static final String SCROLLBAR = "-fx-color: #d0d0d0; -fx-inner-border: #c0c0c0; -fx-body-color: #d0d0d0;";
    protected static final String STATUSBAR = "-fx-background-color: #2b2b2b;-fx-border-color: #353535;-fx-text-fill: #989898;-fx-alignment: center-left;-fx-padding: 0px;";
    protected static final String MINI_BUTTON = "-fx-background-color: #2b2b2b;-fx-border-color: #353535;-fx-text-fill: #989898;-fx-alignment: center;-fx-padding: 0px;";
    protected static final String MINI_BUTTON_MOUSE_OVER = "-fx-background-color: #555555;-fx-border-color: #353535;-fx-text-fill: #d8d8d8;-fx-alignment: center;-fx-padding: 0px;";
    protected static final String MINI_BUTTON_PRESSED = "-fx-background-color: #252525;-fx-border-color: #353535;-fx-text-fill: #686868;-fx-alignment: center;-fx-padding: 0px;";
    protected static final String SLIDER = "-fx-background-color: transparent; -fx-control-inner-background: lightgrey; -fx-body-color: transparent;";
    protected static final String ICON = "-fx-background-color: transparent; -fx-border-width: 0px; -fx-text-fill: #989898; -fx-alignment: center;";
    protected static final String ICON_MOUSE_OVER = "-fx-background-color: transparent; -fx-border-width: 0px; -fx-text-fill: #d8d8d8; -fx-alignment: center;";
    protected static final String ICON_PRESSED = "-fx-background-color: transparent; -fx-border-width: 0px; -fx-text-fill: #686868; -fx-alignment: center;";
    protected static final String ACCORDION = "-fx-box-border: #353535; -fx-color: #323232; -fx-body-color: linear-gradient(#404040, #252525); -fx-inner-border: linear-gradient(#404040, #252525); -fx-text-fill: #989898; -fx-mark-color: #989898; -fx-mark-highlight-color: #d8d8d8;";
    protected static final String PLAYER = "-fx-background-color: #2b2b2b;-fx-border-width: 0px;-fx-alignment: center;";
    protected static final String WEB_BROWSER = Settings.BACKGROUND_GRAY + Settings.SCROLLBAR;
    protected static final String WORKSPACE = "-fx-background: transparent; -fx-background-color: transparent;" + Settings.SCROLLBAR;
    protected static final String DATA_OBJECT_LABEL = "-fx-fill: lightgray; -fx-font-weight: bold; -fx-line-spacing: 0em;";
    protected static final String WELCOME_MESSAGE_COLOR = "-fx-text-fill: white; -fx-opacity: 0.2;";
    protected static final String WELCOME_MESSAGE_STYLE = Settings.WELCOME_MESSAGE_COLOR + "-fx-font-size: " + (Settings.getSystemFont().getSize() * 1.8) + "pt; -fx-text-alignment: center; -fx-font-weight: normal; -fx-line-spacing: 320.0px;";
    protected static final String WELCOME_MESSAGE = "Drop your files here.\nMEI   MSM   MUSICXML   XML   TXT   MIDI   WAV   XSL   RNG   SF2   DLS";

    // global layout settings
    protected static boolean makeLogfile = true;                        // make a logfile
    protected static String logfile = "meico.log";                      // filename of log file
    protected static int defaultWindowWidth = 1200;                     // initial window width
    protected static int defaultWindowHeight = 800;                     // initial window height
    protected static boolean useInternalWebView = true;                 // if false, the external browser is called for Verovio and other online services
    protected static boolean autoExpandPlayerOnMouseOver = true;        // expand the player automatically at mouse over
    protected static boolean accordionAnimations = true;                // animate the expansion and collapsing of accordion elements (player, webview)
    protected static Font font = Font.getDefault();                     // the text font (icons come from another font, Font Awesome 5.2.0)

    // data item layout settings
    protected static double dataItemSize = 5.25;                        // adjust the size of the data items (circles) in relation to the underlying font size (width of letter M)
    protected static double mWidth = Settings.setMWidth();
    protected static double dataItemRadius = Settings.setDataItemRadius();
    protected static double dataItemStrokeWidth = Settings.setDataItemStrokeWidth();
    protected static double dataItemMenuItemHeight = Settings.setDataItemMenuItemHeight();
    protected static double dataItemMenuItemDistanceFromCircle = Settings.setDataItemMenuItemDistanceFromCircle();
    protected static double dataItemMenuItemLabelIndent = Settings.setDataItemMenuItemLabelIndent();
    protected static double dataItemMenuItemOuterIndent = Settings.setDataItemMenuItemOuterIndent();
    protected static double dataItemOpacity = 0.4;                      // opacity of data items
    protected static double dataItemSaturation = 0.6;                   // saturation of data item color
    protected static double newDataObjectDistance = Settings.setNewDataObjectDistance();
    protected static double multipleDataObjectCreationAngle = 30;       // when more than one data object are created (e.g. MEI->MSM with several mdivs), they are centered arround their parent data object and separated by this angle

    // conversion settings
    protected static int Mei2Msm_ppq = 720;                             // the timing resolution of exported MSMs and derivated MIDIs
    protected static boolean Mei2Msm_dontUseChannel10 = true;
    protected static boolean Mei2Msm_ignoreExpansions = false;
    protected static boolean Mei2Msm_msmCleanup = true;
    protected static double Msm2Midi_defaultTempo = 120;                // default tempo of the MIDI sequence in bpm
    protected static boolean Msm2Midi_generateProgramChanges =  true;   // generate program change events

    // Pitches settings
    protected static boolean savePitchesWithPrettyPrint = false;        // text formatting of the JSON (if true, it consumes far more memory)

    // MIDI player settings
    protected static File soundbank = null;                             // set this null to use the default soundbank

    // XSL Transform settings
    protected static File xslFile = null;                               // here it is possible to set a default XSLT
    protected static Xslt30Transformer xslTransform = null;             // the actual transformer instance

    // XML schema settings
    protected static File schema = null;                                // here it is possible to set a default schema

    // Score rendering settings
    protected static boolean oneLineScore = false;                      // if true, the score rendering in the WebView will output all music in one line
    protected static boolean useLatestVerovio = true;                   // if set true, meico will try to use the latest online available version of verovio-toolkit.js
    protected static String scoreFont = "Leipzig";                      // the font used for score rendering

    /**
     * this opens a dialog window to edit the preferences settings in meico
     */
    protected static void openSettingsDialog(MeicoApp app) {
        // general layouting
        double spacing = Font.getDefault().getSize() * 0.75;
        Stage stage = new Stage();

        // title
        Label title = new Label("Meico: MEI Converter");
        title.setFont(Font.font(Font.getDefault().getSize() * 1.5));
//        title.setStyle("-fx-font-weight: bold;");
        title.setTextFill(Color.GRAY);
        title.setStyle("-fx-font-weight: bold;");

        Label author = new Label("Author: Axel Berndt");
        author.setTextFill(Color.GRAY);

        Label subtitle = new Label("Preferences Settings");
        subtitle.setTextFill(Color.GRAY);

        AnchorPane titlePane = new AnchorPane(title, author);
        AnchorPane.setLeftAnchor(title, 0.0);
        AnchorPane.setRightAnchor(author, 0.0);

        Separator separator1 = new Separator(Orientation.HORIZONTAL);
        separator1.setOpacity(0.5);

        // general settings
        Label generalSettingsLabel = new Label("General Settings");
        generalSettingsLabel.setTextFill(Color.GRAY);
        generalSettingsLabel.setStyle("-fx-font-weight: bold;");

        // initial window size
        SpinnerValueFactory<Integer> widthValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(300, 10000, Settings.defaultWindowWidth);
        Spinner<Integer> windowWidth = new Spinner<>();
        windowWidth.setValueFactory(widthValueFactory);
        windowWidth.setEditable(true);
        windowWidth.focusedProperty().addListener((observable, oldValue, newValue) -> {   //method to force commit on text input on JavaFx spinners, see https://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
            if (!newValue) {
                windowWidth.increment(0); // won't change value, but will commit editor
            }
        });
        windowWidth.getEditor().setAlignment(Pos.CENTER_RIGHT);
        windowWidth.setPrefWidth(Settings.mWidth * 7);

        Label windowWidthLabel = new Label("Initial window width        ");
        windowWidthLabel.setTextFill(Color.GRAY);
        VBox.setVgrow(windowWidthLabel, Priority.ALWAYS);
        windowWidthLabel.setPadding(new Insets(0, 0, 0, spacing));
        windowWidthLabel.setAlignment(Pos.CENTER_LEFT);

        SpinnerValueFactory<Integer> heightValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(300, 10000, Settings.defaultWindowHeight);
        Spinner<Integer> windowHeight = new Spinner<>();
        windowHeight.setValueFactory(heightValueFactory);
        windowHeight.setEditable(true);
        windowHeight.focusedProperty().addListener((observable, oldValue, newValue) -> {   //method to force commit on text input on JavaFx spinners, see https://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
            if (!newValue) {
                windowHeight.increment(0); // won't change value, but will commit editor
            }
        });
        windowHeight.getEditor().setAlignment(Pos.CENTER_RIGHT);
        windowHeight.setPrefWidth(Settings.mWidth * 7);

        Label windowHeightLabel = new Label("Initial window height");
        windowHeightLabel.setTextFill(Color.GRAY);
        VBox.setVgrow(windowHeightLabel, Priority.ALWAYS);
        windowHeightLabel.setPadding(new Insets(0, 0, 0, spacing));
        windowHeightLabel.setAlignment(Pos.CENTER_LEFT);

        FlowPane windowSizePane = new FlowPane(spacing, spacing);
        windowSizePane.getChildren().addAll(windowWidth, windowWidthLabel, windowHeight, windowHeightLabel);

        // webview
//        CheckBox webview = new CheckBox("Use internal WebView, uncheck to use the system default browser instead (requires restart)");
//        webview.setSelected(Settings.useInternalWebView);
//        webview.setTextFill(Color.GRAY);

        // autoexpanding player
        CheckBox player = new CheckBox("Expand player automatically on mouseover");
        player.setSelected(Settings.autoExpandPlayerOnMouseOver);
        player.setTextFill(Color.GRAY);
        player.selectedProperty().addListener((observable, oldValue, newValue) -> app.setPlayerAccordion(newValue));

        // autoexpanding player
        CheckBox accordion = new CheckBox("Animate expansion and collapsing of accordion menu items (WebView, player)");
        accordion.setSelected(Settings.accordionAnimations);
        accordion.setTextFill(Color.GRAY);
        accordion.selectedProperty().addListener((observable, oldValue, newValue) -> app.setAccordionAnimation(newValue));

        // logfile
        CheckBox logfile = new CheckBox("Make logfile \"meico.log\" in program folder (requires restart)");
        logfile.setSelected(Settings.makeLogfile);
        logfile.setTextFill(Color.GRAY);

        // debug mode
        CheckBox debug = new CheckBox("Debug mode, only for development purpose, it will alter your MEI data! (requires restart)");
        debug.setSelected(!Settings.Mei2Msm_msmCleanup);
        debug.setTextFill(Color.GRAY);

        Separator separator2 = new Separator(Orientation.HORIZONTAL);
        separator2.setOpacity(0.5);

        // conversion options
        Label conversionOptions = new Label("Conversion Options");
        conversionOptions.setTextFill(Color.GRAY);
        conversionOptions.setStyle("-fx-font-weight: bold;");

        // timing resolution
        SpinnerValueFactory<Integer> timingValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, Settings.Mei2Msm_ppq);
        Spinner<Integer> ppq = new Spinner<>();
        ppq.setValueFactory(timingValueFactory);
        ppq.setEditable(true);
        ppq.focusedProperty().addListener((observable, oldValue, newValue) -> {   //method to force commit on text input on JavaFx spinners, see https://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
            if (!newValue) {
                ppq.increment(0); // won't change value, but will commit editor
            }
        });
        ppq.getEditor().setAlignment(Pos.CENTER_RIGHT);
        ppq.setPrefWidth(Settings.mWidth * 7);

        Label ppqLabel = new Label("Timing resolution in pulses per quarternote, relevant for MEI to MSM and MIDI conversion\n(common values are 360 and 720)");
        ppqLabel.setTextFill(Color.GRAY);
        VBox.setVgrow(ppqLabel, Priority.ALWAYS);
        ppqLabel.setPadding(new Insets(0, 0, 0, spacing));
        ppqLabel.setAlignment(Pos.CENTER_LEFT);

        FlowPane ppqPane = new FlowPane(spacing, spacing);
        ppqPane.getChildren().addAll(ppq, ppqLabel);

        // don't use channel 10
        CheckBox channel10 = new CheckBox("Do not use channel 10 (MIDI drum channel, default is checked)");
        channel10.setSelected(Settings.Mei2Msm_dontUseChannel10);
        channel10.setTextFill(Color.GRAY);

        // resolve expan elements
        CheckBox expan = new CheckBox("Resolve <expan> elements in MEI before conversion (default is checked)");
        expan.setSelected(!Settings.Mei2Msm_ignoreExpansions);
        expan.setTextFill(Color.GRAY);

        // default MIDI temo
        SpinnerValueFactory<Double> tempoValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 10000, Settings.Msm2Midi_defaultTempo);
        Spinner<Double> tempo = new Spinner<>();
        tempo.setValueFactory(tempoValueFactory);
        tempo.setEditable(true);
        tempo.focusedProperty().addListener((observable, oldValue, newValue) -> {   //method to force commit on text input on JavaFx spinners, see https://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
            if (!newValue) {
                tempo.increment(0); // won't change value, but will commit editor
            }
        });
        tempo.getEditor().setAlignment(Pos.CENTER_RIGHT);
        tempo.setPrefWidth(Settings.mWidth * 7);

        Label tempoLabel = new Label("Default MIDI tempo");
        tempoLabel.setTextFill(Color.GRAY);
        VBox.setVgrow(tempoLabel, Priority.ALWAYS);
        tempoLabel.setPadding(new Insets(0, 0, 0, spacing));
        tempoLabel.setAlignment(Pos.CENTER_LEFT);

        FlowPane tempoPane = new FlowPane(spacing, spacing);
        tempoPane.getChildren().addAll(tempo, tempoLabel);

        // generate program changes
        CheckBox prog = new CheckBox("Generate MIDI program change events, tries to match staff names with instruments in General MIDI (default is checked)");
        prog.setSelected(Settings.Msm2Midi_generateProgramChanges);
        prog.setTextFill(Color.GRAY);

        // Default soundbank
        TextField soundbankField = new TextField((Settings.soundbank == null) ? "Java Default Soundbank" : Settings.soundbank.getAbsolutePath());
        soundbankField.setMinWidth(Settings.mWidth * 30);
        soundbankField.setMaxWidth(Settings.mWidth * 30);
        soundbankField.setEditable(false);
        soundbankField.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: grey; -fx-padding: 6px;");

        File originalSoundbank = Settings.soundbank;

        Button soundbankFileButton = new Button("\uf07c");
        soundbankFileButton.setFont(Settings.getIconFont(Font.getDefault().getSize() +3, app));
        soundbankFileButton.setOnMouseReleased(event -> {
            FileChooser chooser = new FileChooser();                                    // the file chooser to select file location and name
            if ((Settings.soundbank == null) || !Settings.soundbank.exists())
                chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            else
                chooser.setInitialDirectory(new File(Settings.soundbank.getParent()));
            chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All supported files (*.sf2, *.dls)", "*.sf2", "*.dls"), new FileChooser.ExtensionFilter("Soundfont files (*.sf2)", "*.sf2"), new FileChooser.ExtensionFilter("Downloadable Sounds files (*.dls)", "*.dls"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                Settings.setSoundbank(file);
                soundbankField.setText(file.getAbsolutePath());
            }
        });

        Button soundbankDefault = new Button("Default Soundbank");
        soundbankDefault.setOnMouseReleased(event -> {
            Settings.setSoundbank(null);
            soundbankField.setText("Java Default Soundbank");
        });

        Label soundbankLabel = new Label("Choose soundbank\nfor MIDI synthesis");
        soundbankLabel.setTextFill(Color.GRAY);
        VBox.setVgrow(soundbankLabel, Priority.ALWAYS);
        soundbankLabel.setPadding(new Insets(0, 0, 0, spacing));
        soundbankLabel.setAlignment(Pos.CENTER_LEFT);

        FlowPane soundbankPane = new FlowPane(spacing, spacing);
        soundbankPane.getChildren().addAll(soundbankField, soundbankFileButton, soundbankDefault, soundbankLabel);

        // default xslt
        TextField xsltField = new TextField((Settings.xslFile == null) ? "No XSLT specified" : Settings.xslFile.getAbsolutePath());
        xsltField.setMinWidth(Settings.mWidth * 30);
        xsltField.setMaxWidth(Settings.mWidth * 30);
        xsltField.setEditable(false);
        xsltField.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: grey; -fx-padding: 6px;");

        File originalXslt = Settings.xslFile;

        Button xsltFileButton = new Button("\uf07c");
        xsltFileButton.setFont(Settings.getIconFont(Font.getDefault().getSize() +3, app));
        xsltFileButton.setOnMouseReleased(event -> {
            FileChooser chooser = new FileChooser();                                    // the file chooser to select file location and name
            if ((Settings.xslFile == null) || !Settings.xslFile.exists())
                chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            else
                chooser.setInitialDirectory(new File(Settings.xslFile.getParent()));
            chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XSL Transform files (*.xsl)", "*.xsl"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                Settings.setXSLT(file);
                xsltField.setText(file.getAbsolutePath());
            }
        });

        Button noXSLT = new Button("\uf00d");
        noXSLT.setFont(Settings.getIconFont(Font.getDefault().getSize() +3, app));
        noXSLT.setOnMouseReleased(event -> {
            Settings.setXSLT(null);
            xsltField.setText("No XSLT specified");
        });

        Label xsltLabel = new Label("Choose default XSL Transform");
        xsltLabel.setTextFill(Color.GRAY);
        VBox.setVgrow(xsltLabel, Priority.ALWAYS);
        xsltLabel.setPadding(new Insets(0, 0, 0, spacing));
        xsltLabel.setAlignment(Pos.CENTER_LEFT);

        FlowPane xsltPane = new FlowPane(spacing, spacing);
        xsltPane.getChildren().addAll(xsltField, xsltFileButton, noXSLT, xsltLabel);

        // default XML schema
        TextField schemaField = new TextField((Settings.schema == null) ? "No XML Schema specified" : Settings.schema.getAbsolutePath());
        schemaField.setMinWidth(Settings.mWidth * 30);
        schemaField.setMaxWidth(Settings.mWidth * 30);
        schemaField.setEditable(false);
        schemaField.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: grey; -fx-padding: 6px;");

        File originalSchema = Settings.schema;

        Button schemaFileButton = new Button("\uf07c");
        schemaFileButton.setFont(Settings.getIconFont(Font.getDefault().getSize() +3, app));
        schemaFileButton.setOnMouseReleased(event -> {
            FileChooser chooser = new FileChooser();                                    // the file chooser to select file location and name
            if ((Settings.schema == null) || !Settings.schema.exists())
                chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            else
                chooser.setInitialDirectory(new File(Settings.schema.getParent()));
            chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Schema files (*.rng)", "*.rng"), new FileChooser.ExtensionFilter("All files", "*.*"));   // some extensions to filter
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                Settings.setSchema(file);
                schemaField.setText(file.getAbsolutePath());
            }
        });

        Button noSchema = new Button("\uf00d");
        noSchema.setFont(Settings.getIconFont(Font.getDefault().getSize() +3, app));
        noSchema.setOnMouseReleased(event -> {
            Settings.setSchema(null);
            schemaField.setText("No XML Schema specified");
        });

        Label schemaLabel = new Label("Choose default XML Schema");
        schemaLabel.setTextFill(Color.GRAY);
        VBox.setVgrow(schemaLabel, Priority.ALWAYS);
        schemaLabel.setPadding(new Insets(0, 0, 0, spacing));
        schemaLabel.setAlignment(Pos.CENTER_LEFT);

        FlowPane schemaPane = new FlowPane(spacing, spacing);
        xsltPane.getChildren().addAll(schemaField, schemaFileButton, noSchema, schemaLabel);

        // JSON pretty print
        CheckBox jsonPretty = new CheckBox("JSON output with pretty print (contains formatting, better readable but takes more memory)");
        jsonPretty.setSelected(Settings.savePitchesWithPrettyPrint);
        jsonPretty.setTextFill(Color.GRAY);

        Separator separator3 = new Separator(Orientation.HORIZONTAL);
        separator3.setOpacity(0.5);

        // Score rendering
        CheckBox oneLineScore = new CheckBox("Score rendering in one score line (no system breaks)");
        oneLineScore.setSelected(Settings.oneLineScore);
        oneLineScore.setTextFill(Color.GRAY);

        // latest online Verovio version
        CheckBox latestVerovio = new CheckBox("Use latest Verovio version (requires internet connection)");
        latestVerovio.setSelected(Settings.useLatestVerovio);
        latestVerovio.setTextFill(Color.GRAY);

        // Save, Cancel
        Button save = new Button("Save Settings");
        save.setPrefSize(200, 40);
        save.setOnMouseReleased(event -> {
            Settings.defaultWindowWidth = windowWidth.getValue();
            Settings.defaultWindowHeight = windowHeight.getValue();
//            Settings.useInternalWebView = webview.isSelected();
            Settings.autoExpandPlayerOnMouseOver = player.isSelected();
            Settings.accordionAnimations = accordion.isSelected();
            Settings.makeLogfile = logfile.isSelected();
            Settings.Mei2Msm_msmCleanup = !debug.isSelected();
            Settings.Mei2Msm_ppq = ppq.getValue();
            Settings.Mei2Msm_ignoreExpansions = !expan.isSelected();
            Settings.Mei2Msm_dontUseChannel10 = channel10.isSelected();
            Settings.Msm2Midi_generateProgramChanges = prog.isSelected();
            Settings.Msm2Midi_defaultTempo = tempo.getValue();
            Settings.savePitchesWithPrettyPrint = jsonPretty.isSelected();
            Settings.oneLineScore = oneLineScore.isSelected();
            Settings.useLatestVerovio = latestVerovio.isSelected();
            stage.close();
        });

        Button cancel = new Button("Cancel");
        cancel.setPrefSize(200, 40);
        cancel.setOnMouseReleased(event -> {
            Settings.setSoundbank(originalSoundbank);
            Settings.setXSLT(originalXslt);
            Settings.setSchema(originalSchema);
            stage.close();
        });

        FlowPane closePane = new FlowPane(spacing, spacing);
        closePane.setAlignment(Pos.CENTER);
        closePane.getChildren().addAll(save, cancel);

        // add all elemnts to the container and show the window
        VBox container = new VBox();
        container.setStyle(Settings.BACKGROUND_GRAY);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(spacing, spacing, spacing, spacing));
        container.setSpacing(spacing * 1.25);
        container.getChildren().addAll(titlePane, subtitle, separator1, generalSettingsLabel, windowSizePane/*, webview*/, player, accordion, logfile, debug, separator2, conversionOptions, ppqPane, tempoPane, expan, channel10, prog, soundbankPane, xsltPane, schemaPane, jsonPretty, oneLineScore, latestVerovio, separator3, closePane);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle(Settings.BACKGROUND_GRAY);
        scrollPane.setContent(container);
        Scene scene = new Scene(scrollPane/*, 300, 300*/);

//        stage.setMinHeight(790);
        stage.setMaxHeight(795);
//        stage.setMinWidth(705);
        stage.setMaxWidth(705);
        stage.setTitle("Meico Preferences");
        stage.getIcons().add(new Image(app.getClass().getResource("/resources/graphics/meico_icon_flat.png").toString()));    // add an icon to the window header
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setResizable(true);
        stage.show();
    }

    /**
     * read the settings file meico.cfg
     * @throws IOException
     */
    protected static void readSettings() throws IOException {
        File file = new File("meico.cfg");
        BufferedReader br = new BufferedReader(new FileReader(file));

        String attribute = "";
        // build (key, value) pairs where the key is the instrument name string and value is the program change number (pc) and add them to the dict map
        for(String line = br.readLine(); line != null; line = br.readLine()) {  // read all the lines in instruments.dict
            if (line.isEmpty()                                                  // an empty line
                    || (line.charAt(0) == '%'))                                 // this is a comment line
                continue;                                                       // ignore it

            if (line.charAt(0) == '#') {                                        // this is an attribute name
                attribute = line.substring(2);                                  // get attribute name
                continue;
            }

            // read the attribute value
            switch (attribute) {
                case "defaultWindowWidth":
                    Settings.defaultWindowWidth = Integer.parseInt(line);
                    break;
                case "defaultWindowHeight":
                    Settings.defaultWindowHeight = Integer.parseInt(line);
                    break;
//                case "useInternalWebView":
//                    Settings.useInternalWebView = !line.equals("0");    // everything else but "0" switches the webview on
//                    break;
                case "autoExpandPlayerOnMouseOver":
                    Settings.autoExpandPlayerOnMouseOver = !line.equals("0");    // everything else but "0" switches the webview on
                    break;
                case "accordionAnimations":
                    Settings.accordionAnimations = !line.equals("0");    // everything else but "0" switches the webview on
                    break;
                case "logfile":
                    Settings.makeLogfile = line.equals("1");
                    break;
                case "debug":
                    Settings.Mei2Msm_msmCleanup = !line.equals("1");    // only "1" triggers debug mode
                    break;
                case "ppq":
                    Settings.Mei2Msm_ppq = Integer.parseInt(line);
                    break;
                case "resolveExpansions":
                    Settings.Mei2Msm_ignoreExpansions = line.equals("0");  // everything else but "0" switches it on
                    break;
                case "dontUseChannel10":
                    Settings.Mei2Msm_dontUseChannel10 = !line.equals("0");  // everything else but "0" switches it on
                    break;
                case "generateProgramChanges":
                    Settings.Msm2Midi_generateProgramChanges = !line.equals("0");
                    break;
                case "tempo":
                    Settings.Msm2Midi_defaultTempo = Double.parseDouble(line);
                    break;
                case "prettyJson":
                    Settings.savePitchesWithPrettyPrint = line.equals("1");
                    break;
                case "oneLineScore":
                    Settings.oneLineScore = line.equals("1");
                    break;
                case "useLatestVerovio":
                    Settings.useLatestVerovio = line.equals("1");
                    break;
                case "scoreFont":
                    Settings.scoreFont = line;
                    break;
                case "soundbank":
                    Settings.setSoundbank((line.equals("default")) ? null : new File(line));
                    if ((Settings.soundbank != null) && !Settings.soundbank.exists())
                        Settings.setSoundbank(null);
                    break;
                case "xslt":
                    Settings.setXSLT((line.equals("none")) ? null : new File(line));
                    if ((Settings.xslFile != null) && !Settings.xslFile.exists())
                        Settings.setXSLT(null);
                    break;
                case "schema":
                    Settings.setSchema((line.equals("none")) ? null : new File(line));
                    if ((Settings.schema != null) && !Settings.schema.exists())
                        Settings.setSchema(null);
                    break;
                default:
                    break;
            }
        }

        br.close(); // close reader
    }

    protected static void writeSettingsFile() {
        String output = "% Meico settings\n\n# defaultWindowWidth\n" + Settings.defaultWindowWidth
                + "\n\n# defaultWindowHeight\n" + Settings.defaultWindowHeight
//                + "\n\n# useInternalWebView\n" + (Settings.useInternalWebView ? "1" : "0")
                + "\n\n# autoExpandPlayerOnMouseOver\n" + (Settings.autoExpandPlayerOnMouseOver ? "1" : "0")
                + "\n\n# accordionAnimations\n" + (Settings.accordionAnimations ? "1" : "0")
                + "\n\n# logfile\n" + (Settings.makeLogfile ? "1" : "0")
                + "\n\n# debug\n" + (Settings.Mei2Msm_msmCleanup ? "0" : "1")
                + "\n\n# ppq\n" + Settings.Mei2Msm_ppq
                + "\n\n# resolveExpansions\n" + (Settings.Mei2Msm_ignoreExpansions ? "0" : "1")
                + "\n\n# dontUseChannel10\n" + (Settings.Mei2Msm_dontUseChannel10 ? "1" : "0")
                + "\n\n# generateProgramChanges\n" + (Settings.Msm2Midi_generateProgramChanges ? "1" : "0")
                + "\n\n# tempo\n" + Settings.Msm2Midi_defaultTempo
                + "\n\n# prettyJson\n" + (Settings.savePitchesWithPrettyPrint ? "1" : "0")
                + "\n\n# oneLineScore\n" + (Settings.oneLineScore ? "1" : "0")
                + "\n\n# useLatestVerovio\n" + (Settings.useLatestVerovio ? "1" : "0")
                + "\n\n# scoreFont\n" + Settings.scoreFont
                + "\n\n# soundbank\n" + ((Settings.soundbank == null) ? "default" : Settings.soundbank.getAbsolutePath())
                + "\n\n# xslt\n" + ((Settings.xslFile == null) ? "none" : Settings.xslFile.getAbsolutePath())
                + "\n\n# schema\n" + ((Settings.schema == null) ? "none" : Settings.schema.getAbsolutePath())
                +"\n";

        PrintWriter writer;
        try {
            writer = new PrintWriter("meico.cfg", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        writer.print(output);
        writer.close();
    }

    /**
     * Load the icon font from within the jar.
     * @param caller an instance of a class in the meico package
     * @return
     */
    protected static synchronized Font getIconFont(Object caller) {
//        Font font = Font.loadFont(caller.getClass().getResource("/resources/fonts/fa-solid-900.ttf").toExternalForm(), Font.getDefault().getSize());  // this does not work in IDEs
        InputStream is = caller.getClass().getResourceAsStream("/resources/fonts/fa-solid-900.ttf");
        Font font = Font.loadFont(is, Font.getDefault().getSize());
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return font;
    }

    /**
     * Load the icon font from within the jar.
     * @param size set the size of the font
     * @param caller an instande of a class in the meico package
     * @return
     */
    protected static synchronized Font getIconFont(double size, Object caller) {
        InputStream is = caller.getClass().getResourceAsStream("/resources/fonts/fa-solid-900.ttf");
        Font font = Font.loadFont(is, size);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return font;
    }

    /**
     * returns the designated font
     * @param fontFileName if the font is part of the meico package the path starts with "/resources/fonts/" the file can be a ttf or otf
     * @param size
     * @param caller
     * @return
     */
    protected static synchronized Font getFont(String fontFileName, double size, Object caller) {
        InputStream is = caller.getClass().getResourceAsStream(fontFileName);
        Font font = Font.loadFont(is, size);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return font;
    }

    /**
     * obtains the standard font of the operating system
     * @return
     */
    protected static synchronized Font getSystemFont() {
        return Font.getDefault();
    }

    /**
     * set a new standard font
     * @param font
     */
    protected static synchronized void setFont(Font font) {
        Settings.font = font;
        Settings.mWidth = Settings.setMWidth();
        Settings.dataItemRadius = Settings.setDataItemRadius();
        Settings.dataItemStrokeWidth = Settings.setDataItemStrokeWidth();
        Settings.dataItemMenuItemHeight = Settings.setDataItemMenuItemHeight();
        Settings.dataItemStrokeWidth = Settings.setDataItemMenuItemDistanceFromCircle();
        Settings.dataItemMenuItemLabelIndent = Settings.setDataItemMenuItemLabelIndent();
        Settings.dataItemMenuItemOuterIndent = Settings.setDataItemMenuItemOuterIndent();
        Settings.newDataObjectDistance = Settings.setNewDataObjectDistance();
    }

    /**
     * returns the width of letter M for the given font
     * @return
     */
    protected static synchronized double setMWidth() {
        Text m = new Text("M");
        m.setFont(Settings.font);
        return m.getLayoutBounds().getWidth();
    }

    /**
     * from the type of the given object derive a color value
     * @param object
     * @return
     */
    protected static synchronized Color classToColor(Object object) {
        int mixupSeed = 74367;
        double colorAngle = 150.0;
        int colorSpread = 360;
        return Color.hsb(((object.getClass().getSimpleName().hashCode() % mixupSeed) % colorSpread) + colorAngle, Settings.dataItemSaturation, 1.0);
    }

    /**
     * this returns a random color with full saturation and brightness
     * @return
     */
    protected static synchronized Color getRandomColor() {
        return Color.hsb(Math.random() * 360, 1.0, 1.0);
    }

    /**
     * the aperture angle of the menu items
     * @return
     */
    protected static synchronized double setDataItemMenuItemHeight() {
        return Settings.font.getSize() + 0.9;
    }

    /**
     * compute the radius of a data item
     * @return
     */
    protected static synchronized double setDataItemRadius() {
        return Settings.mWidth * Settings.dataItemSize;
    }

    /**
     * compute the stroke width of a data item
     * @return
     */
    protected static synchronized double setDataItemStrokeWidth() {
        return Settings.dataItemRadius * 0.05;
    }

    /**
     * this computes the distance between circle and menu item
     * @return
     */
    protected static synchronized double setDataItemMenuItemDistanceFromCircle() {
        return Settings.dataItemRadius * 0.05;
    }

    /**
     * shift the label a bit into the item body so that it does not start right at the border
     * @return
     */
    protected static synchronized double setDataItemMenuItemLabelIndent() {
        return Settings.mWidth * 0.75;
    }

    /**
     * make the item body a bit longer so that the longest label does not reach the border
     * @return
     */
    protected static synchronized double setDataItemMenuItemOuterIndent() {
        return Settings.dataItemMenuItemLabelIndent * 2.5;
    }

    /**
     * the distance at which child items are created from their parent data object
     * @return
     */
    protected static synchronized double setNewDataObjectDistance() {
        return Settings.dataItemRadius * 4.75;
    }

    /**
     * set the soundbank to be used for MIDI synthesis
     * @param soundbank
     */
    protected static synchronized void setSoundbank(File soundbank) {
        Settings.soundbank = soundbank;
    }

    /**
     * set a default xslt
     * @param xslt
     */
    protected static synchronized void setXSLT(File xslt) {
        Settings.xslFile = xslt;

        if (Settings.xslFile == null) {
            Settings.xslTransform = null;
            return;
        }

        try {
            Settings.xslTransform = Helper.makeXslt30Transformer(xslt);
        } catch (FileNotFoundException | SaxonApiException e) {
            e.printStackTrace();
            Settings.xslFile = null;
            Settings.xslTransform = null;
        }

        // old Java version
//        TransformerFactory tFactory = TransformerFactory.newInstance();
//        try {
//            Settings.xslTransform = tFactory.newTransformer(new StreamSource(Settings.xslFile));
//        } catch (TransformerConfigurationException e) {
//            e.printStackTrace();
//            Settings.xslFile = null;
//            Settings.xslTransform = null;
//        }
    }

    /**
     * get the default XSLT
     * @return
     */
    protected static synchronized Xslt30Transformer getXslTransform() {
        return Settings.xslTransform;
    }

    /**
     * get the file of the default XSLT
     * @return
     */
    protected static synchronized File getXslFile() {
        return Settings.xslFile;
    }

    /**
     * set the default XML schema
     * @param schema an rng file
     */
    protected static synchronized void setSchema(File schema) {
        Settings.schema = schema;
    }

    /**
     * get the file of the default XML schema file
     * @return
     */
    protected static synchronized File getSchema() {
        return Settings.schema;
    }

}
