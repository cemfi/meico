package meico.app;

/**
 * This is a standalone application that runs all the conversions.
 * @author Axel Berndt.
 */

import meico.audio.Audio;
import meico.mei.Mei;
import meico.midi.Midi;
import meico.msm.Msm;

import net.miginfocom.swing.MigLayout;
import nu.xom.ParsingException;

import javax.sound.midi.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MeiCoApp extends JFrame {

    private List<Mei4Gui> music = new ArrayList<Mei4Gui>();
    private Sequencer sequencer;

    // some global interface elements
    private final JLabel statusMessage = new JLabel();             // the message component of the statusbar
    private final JLabel fileListPanel = new JLabel();             // a container for the list of loaded and generated files, here, the main interactions take place
    private final JPanel backgroundPanel = new JPanel();           // the conainer of everything that happens in the work area of the window
    private final JPanel statusPanel = new JPanel();               // the container of the statusbar components
    private final JLabel dropLabel = new JLabel("Drop your MEI, MSM, Midi, and Wave files here.", JLabel.CENTER);                               // a text label that tells the user to drop files
//    private final JLabel dropLabel = new JLabel("Drop your mei, msm and midi files here.", new ImageIcon(getClass().getResource("/resources/drop-inverse.png")), JLabel.CENTER);
    private final JLabel meilabel = new JLabel(new ImageIcon(getClass().getResource("/resources/graphics/mei-inverse.png")), JLabel.CENTER);    // mei logo
    private final JLabel msmlabel = new JLabel(new ImageIcon(getClass().getResource("/resources/graphics/msm-inverse.png")), JLabel.CENTER);    // msm logo
    private final JLabel midilabel = new JLabel(new ImageIcon(getClass().getResource("/resources/graphics/midi-inverse.png")), JLabel.CENTER);  // midi logo
    private final JLabel audiolabel = new JLabel(new ImageIcon(getClass().getResource("/resources/graphics/audio-inverse.png")), JLabel.CENTER);  // audio logo
    private final JLabel loadIcon = new JLabel(new ImageIcon(getClass().getResource("/resources/graphics/open-small.png")));                    // a clickable icon in the statusbar to start the filo open dialog
    private final JLabel closeAllIcon = new JLabel("\u2716");      // a clickable icon to close all loaded data in the work area, it is placed in the statusbar

    // layout variables
    private final int windowWidth = 1020;
    private final int windowHeight = 500;
    private final int fontSize = 14;
    private final int fontStyle = Font.BOLD;
    private final String fontName = "default";
    private final double iconScaleFactor = 0.85;
    private final ImageIcon saveIcon = new ImageIcon((new ImageIcon(getClass().getResource("/resources/graphics/save-gray.png")).getImage()).getScaledInstance((int)(25 * this.iconScaleFactor), (int)(26 * this.iconScaleFactor), Image.SCALE_AREA_AVERAGING));
    private final ImageIcon convertIcon = new ImageIcon((new ImageIcon(getClass().getResource("/resources/graphics/convert-gray.png")).getImage()).getScaledInstance((int)(25 * this.iconScaleFactor), (int)(18 * this.iconScaleFactor), Image.SCALE_AREA_AVERAGING));
    private final double fileNameFieldWidtch = 16.5;  // in percent of the whole window width
    private final double iconFieldWidth = 3.5;       // in percent of the whole window width


    /**
     * the main method to run meico
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {               // if meico.jar is called without command line arguments
            new MeiCoApp("meico - MEI Converter", true);                 // start meico in window mode
        }
        else                                // in case of command line arguments
            commandLineMode(args);          // run the command line mode
    }

    /**
     * call this method to start the program in command line mode
     * it shows you all you need if you want to use meico in your application
     *
     * @param args The following parameter strings are used
     *             - "-?" or "--help": for this command line help text. If you use this, any other arguments are skipped.
     *             - "-v" or "--validation": to activate validation of mei files loaded
     *             - "-a" or "--add-ids": to add xml:ids to note, rest and chord elements in mei, as far as they do not have an id; meico will output a revised mei file
     *             - "-r" or "--resolve-copy-ofs": mei elements with a copyOf attribute are resolved into selfcontained elements with an own xml:id; meico will output a revised mei file
     *             - "-m" or "--msm": converts mei to msm; meico will write an msm file to the path of the mei
     *             - "-i" or "--midi": converts mei to msm to midi; meico will output a midi file to the path of the mei
     *             - "-w" or "--wav": converts mei (to midi, internally) to wav; meico will output a wave file to the path of the mei
     *             - "-p" or "--no-program-changes" call this to suppress the generation of program change events in midi
     *             - "-c" or "--dont-use-channel-10": the flag says whether channel 10 (midi drum channel) shall be used or not; it is already dont here, at the mei2msm conversion, because the msm should align with the midi file later on
     *             - "-d" or "--debug": to write debug versions of mei and msm
     *             - "-t" or "--tempo" followed by the tempo in bpm: to set the tempo of the midi file; if this is not used the tempo is always 120 bpm
     *             - Path tho the mei file (e.g., D:\Arbeit\Software\Java\MEI Converter\test files\Hummel_Concerto_for_trumpet.mei), this should always be the last parameter -  always in quotes!
     */
    public static void commandLineMode(String[] args) {
        for (String arg : args) {
            if (arg.equals("-?") || arg.equals("--help")) {
                System.out.println("Meico requires the following arguments:\n");
                System.out.println("[-?] or [--help]                    for this command line help text. If you use this, any other arguments are skipped");
                System.out.println("[-v] or [--validation]              to activate validation of mei files loaded");
                System.out.println("[-a] or [--add-ids]                 to add xml:ids to note, rest and chord elements in mei, as far as they do not have an id; meico will output a revised mei file");
                System.out.println("[-r] or [--resolve-copy-ofs]        mei elements with a copyOf attribute are resolved into selfcontained elements with an own xml:id; meico will output a revised mei file");
                System.out.println("[-m] or [--msm]                     converts mei to msm; meico will write an msm file to the path of the mei");
                System.out.println("[-i] or [--midi]                    converts mei (to msm, internally) to midi; meico will output a midi file to the path of the mei");
                System.out.println("[-w] or [--wav]                     converts mei (to midi, internally) to wav; meico will output a wave file to the path of the mei");
                System.out.println("[-p] or [--no-program-changes]      call this to suppress the generation of program change events in midi");
                System.out.println("[-c] or [--dont-use-channel-10]     the flag says whether channel 10 (midi drum channel) shall be used or not; it is already done at mei-to-msm convertion, because the msm should align with the midi file later on");
                System.out.println("[-t argument] or [--tempo argument] this sets the tempo of the midi file; the argument must be a floating point number; if not used the tempo is always 120 bpm");
                System.out.println("[-d] or [--debug]                   to write debug versions of the mei and msm files  to the path");
                System.out.println("\nThe final argument should always be a path to a valid mei file (e.g., \"C:\\myMeiCollection\\test.mei\"); always in quotes! This is the only mandatory argument if you want to convert something.");
                return;
            }
        }

        // what does the user want meico to do with the file just loaded?
        boolean validate = false;
        boolean addIds = false;
        boolean resolveCopyOfs = false;
        boolean msm = false;
        boolean midi = false;
        boolean wav = false;
        boolean generateProgramChanges = true;
        boolean dontUseChannel10 = false;
        boolean debug = false;
        double tempo = 120;
        for (int i = 0; i < args.length-1; ++i) {
            if ((args[i].equals("-v")) || (args[i].equals("--validation"))) { validate = true; continue; }
            if ((args[i].equals("-a")) || (args[i].equals("--add-ids"))) { addIds = true; continue; }
            if ((args[i].equals("-r")) || (args[i].equals("--resolve-copy-ofs"))) { resolveCopyOfs = true; continue; }
            if ((args[i].equals("-m")) || (args[i].equals("--msm"))) { msm = true; continue; }
            if ((args[i].equals("-i")) || (args[i].equals("--midi"))) { midi = true; continue; }
            if ((args[i].equals("-w")) || (args[i].equals("--wav"))) { wav = true; continue; }
            if ((args[i].equals("-p")) || (args[i].equals("--no-program-changes"))) { generateProgramChanges = false; continue; }
            if ((args[i].equals("-c")) || (args[i].equals("--dont-use-channel-10"))) { dontUseChannel10 = true; continue; }
            if ((args[i].equals("-d")) || (args[i].equals("--debug"))) { debug = true; }
            if ((args[i].equals("-t")) || (args[i].equals("--tempo"))) { tempo = Integer.parseInt(args[i+1]); }
        }

        // load the file
        File meiFile;
        try {
            System.out.println("Loading file: " + args[args.length-1]);
            meiFile = new File(args[args.length-1]);                    // load mei file
            meiFile = new File(meiFile.getCanonicalPath());             // ensure that the absolute path in the file object

        } catch (NullPointerException error) {
            error.printStackTrace();                                    // print error to console
            return;
        } catch (IOException error) {
            error.printStackTrace();                                    // print error to console
            return;
        }
        Mei mei = null;
        try {
            mei = new Mei(meiFile, validate);                           // read an mei file
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (ParsingException e) {
            e.printStackTrace();
            return;
        }
        if (mei.isEmpty()) {
            System.out.println("Failed.");
            return;
        }

        // optional mei processing functions
        if (resolveCopyOfs) {
            System.out.println("Processing mei: resolving copyOfs.");
            mei.resolveCopyofs();                       // this call is part of the exportMsm() method but can also be called alone to expand the mei source and write it to the file system
        }
        if (addIds) {
            System.out.println("Processing mei: adding xml:ids.");
            mei.addIds();                               // generate ids for note, rest, mRest, multiRest, and chord elements that have no xml:id attribute
        }
        if (resolveCopyOfs || addIds) {
            mei.writeMei();                             // this outputs an expanded mei file with more xml:id attributes and resolved copyof's
        }

        if (!(msm || midi || wav)) return;             // if no conversion is required, we are done here

        // convert mei -> msm -> midi
        System.out.println("Converting mei to msm.");
        List<Msm> msms = mei.exportMsm(720, dontUseChannel10, !debug);    // usually, the application should use mei.exportMsm(720); the cleanup flag is just for debugging (in debug mode no cleanup is done)
        if (msms.isEmpty()) {
            System.out.println("No msm data created.");
            return;
        }

        if (debug) {
            mei.writeMei(mei.getFile().getPath().substring(0, mei.getFile().getPath().length() - 4) + "-debug.mei"); // After the msm export, there is some new stuff in the mei ... mainly the date and dur attribute at measure elements (handy to check for numeric problems that occured during conversion), some ids and expanded copyofs. This was required for the conversion and can be output with this function call. It is, however, mainly interesting for debugging.
        }

        if (msm) {
            System.out.println("Writing msm to file system: ");
            for (Msm msm1 : msms) {
                if (!debug) msm1.removeRests();  // purge the data (some applications may keep the rests from the mei; these should not call this function)
                msm1.writeMsm();                 // write the msm file to the file system
                System.out.println("\t" + msm1.getFile().getPath());
            }
        }

        List<meico.midi.Midi> midis = new ArrayList<Midi>();
        if (midi || wav) {                      // midi conversion is also required for wav export
            System.out.println("Converting msm to midi and writing midi to file system: ");
            for (int i = 0; i < msms.size(); ++i) {
                midis.add(msms.get(i).exportMidi(tempo, generateProgramChanges));    // convert msm to midi
                try {
                    midis.get(i).writeMidi();           // write midi file to the file system
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("\t" + midis.get(i).getFile().getPath());
            }
        }

        List<meico.audio.Audio> audios = new ArrayList<Audio>();
        if (wav) {
            System.out.println("Converting midi to wave and writing wav file to file system: ");
            for (meico.midi.Midi m : midis) {
                Audio a = m.exportAudio();     // this generates an Audio object
                if (a == null)
                    continue;
                audios.add(a);
                try {
                    a.writeAudio();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("\t" + a.getFile().getPath());

            }
        }
    }

    /**
     * The constructor method which starts meico in window mode with a default title.
     * This allows other applications to start the meico window simply by instantiation.
     * <tt>MeiCoApp m = new MeiCoApp();</tt>
     */
    public MeiCoApp() {
        this("meico - MEI Converter", true);
    }

    /**
     * The constructor method which starts meico in window mode with a user defined title.
     * @param title the title of the meico window
     * @param makeLogFile set this true to redirect the console output to the file "meico.log"
     */
    public MeiCoApp(String title, boolean makeLogFile) {
        super(title);

        // in window mode all the command line output and error messages are redirected to a log file
        if (makeLogFile) {
            try {
                FileOutputStream log = new FileOutputStream("meico.log");
                PrintStream out = new PrintStream(log);
                System.setOut(out);
                System.setErr(out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // initialize the midi sequencer for midi playback
        try {
            this.sequencer = MidiSystem.getSequencer();
            this.sequencer.open();
        } catch (MidiUnavailableException e) {
            this.setStatusMessage(e.toString());
            this.sequencer = null;
        }
        if (sequencer != null) {
            // TODO: load a higher quality soundbank

            this.sequencer.addMetaEventListener(new MetaEventListener() {               // Add a listener for meta message events to detect when ...
                public void meta(MetaMessage event) {
                    if (event.getType() == 47) {                                        // ... the sequencer is done playing
                        stopAllMidiPlayback();                                                 // switch all playMaidi buttons to triangle
                    }
                }
            });
        }

        // set the OS' look and feel (this is mainly relevant for the JFileChooser that is used later)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());        // try to use the system's look and feel
        }
        catch (Throwable ex) {                                                          // if failed
                                                                                        // it hopefully keeps the old look and feel (not tested)
        }

        // keyboard input via key binding
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Exit");            // close the window when ESC pressed
        this.getRootPane().getActionMap().put("Exit", new AbstractAction(){             // define the "Exit" action
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();                                                              // close the window (if this is the only window, this will terminate the JVM)
                System.exit(0);                                                         // the program may still run, enforce exit
            }
        });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "Load");                // start file open dialog to load a file when F1 is pressed
        this.getRootPane().getActionMap().put("Load", new AbstractAction(){             // define the "Load" action
            @Override
            public void actionPerformed(ActionEvent e) {
                fileOpenDialog();                                                       // start file open dialog
            }
        });

        // add the filedrop function to this JFrame
        new FileDrop(null, this.getRootPane(), new FileDrop.Listener() {                // for debugging information replace the null argument by System.out
            public void filesDropped(java.io.File[] files) {                            // this is the fileDrop listener
                setStatusMessage("");

                for (File file : files)                                                 // for each file that has been dropped
                    loadFile(file);                                                     // load it

                doRepaint();
                toFront();                                                              // after the file drop force this window to have the focus
            }
        });

        // some general window settings
        //this.setBounds(100, 100, 1000, 400);                                          // set window size and position
        this.setSize(this.windowWidth, this.windowHeight);                              // set window size
        this.setResizable(true);                                                        // don't allow resizing
        this.setLocationRelativeTo(null);                                               // set window position
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);                   // what happens when the X is clicked?
        this.setLayout(new BorderLayout());                                             // the layout manager of the main window frame

        // prepare the components
        this.makeStatusbar();                                                           // compile the statusbar
        this.getContentPane().add(this.statusPanel, BorderLayout.SOUTH);                // display it
        this.makeMainPanel();
        this.getContentPane().add(this.backgroundPanel, BorderLayout.CENTER);           // display it

        this.setVisible(true);                                                          // show the frame
    }

    /**
     * load a file
     * @param file this is the file to be loaded
     */
    private void loadFile(File file) {
        try {
//            this.music.add(new MeiCoMusicObject(file));
            this.music.add(new Mei4Gui(file, this));
        } catch (InvalidFileTypeException e) {
            this.setStatusMessage(e.toString());            // if it is neither of the above file formats, output a statusbar message
        } catch (ParsingException e) {
            this.setStatusMessage(e.toString());            // if it is neither of the above file formats, output a statusbar message
        } catch (IOException e) {
            this.setStatusMessage(e.toString());            // if it is neither of the above file formats, output a statusbar message
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method starts the file chooser to open/load a file into meico
     */
    private void fileOpenDialog() {
        JFileChooser chooser = new JFileChooser();                              // open the fileopen dialog
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("all supported files (*.mei, *.msm, *.mid)", "mei", "msm", "mid"));  // make only suitable file types choosable
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("digital music edition (*.mei)", "mei"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("musical sequence markup (*.msm)", "msm"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("midi file (*.mid)", "mid"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("audio file (*.wav)", "wav"));
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {      // file open has been approved
            loadFile(chooser.getSelectedFile());                                // load it
            this.doRepaint();
        }
    }

    /**
     * When the content of backgroundPanel changes, it needs to be repainted. That is done with this method.
     */
    private void doRepaint() {
        this.makeMainPanel();                                               // update the working area
        this.getContentPane().validate();                                   // and
        this.getContentPane().repaint();                                    // repaint
    }

    /**
     * this is  called to output a line in the status message
     * @param message the message text
     */
    private void setStatusMessage(String message) {
        this.statusMessage.setText(message);
    }

    /**
     * a helper method that sets up the statusbar
     */
    private void makeStatusbar() {
        // the statusbar
        this.statusPanel.setLayout(new MigLayout(/*Layout Constraints*/ "", /*Column constraints*/ "", /*Row constraints*/ "0[]0"));
        this.statusMessage.setForeground(Color.DARK_GRAY);                              // the text color
        this.statusMessage.setHorizontalAlignment(SwingConstants.LEFT);                 // text alignment

        // add a file load icon to the statusbar
        this.loadIcon.setPreferredSize(new Dimension(16, 16));                          // make the icon very small
        this.loadIcon.setHorizontalAlignment(JLabel.CENTER);
        this.loadIcon.setToolTipText("<html>open file load dialog</html>");
        this.loadIcon.addMouseListener(new MouseAdapter() {                             // add a mouse listener to the button
            @Override
            public void mouseClicked(MouseEvent e) {                                    // when clicked
                fileOpenDialog();                                                       // open the file load dialog
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setStatusMessage("File open dialog");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setStatusMessage("");
            }
        });

        // add a close all icon to the statusbar
        this.closeAllIcon.setPreferredSize(new Dimension(16, 16));
        this.closeAllIcon.setHorizontalAlignment(JLabel.CENTER);
        this.closeAllIcon.setForeground(Color.DARK_GRAY);                               // the text color
        this.closeAllIcon.setFont(new Font(fontName, Font.PLAIN, 15));                 // font type, style and size
        this.closeAllIcon.setToolTipText("<html>clear the workspace</html>");
        this.closeAllIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((sequencer != null) && sequencer.isOpen())
                    sequencer.stop();
                music.clear();
                doRepaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setStatusMessage("Clear the workspace");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setStatusMessage("");
            }
        });

        this.statusPanel.add(this.statusMessage, "alignx left, aligny center, push");   // add the status label, i.e. the text message, to the status panel
        this.statusPanel.add(this.loadIcon, "right");                                   // add the load icon to the status panel
        this.statusPanel.add(this.closeAllIcon, "right");                               // add the closeAll icon to the status panel
    }

    /**
     * method to setup the main workspace
     */
    private void makeMainPanel() {
        this.backgroundPanel.removeAll();                                                   // clear background panel to make it anew

        // set the background panel
        this.backgroundPanel.setBackground(Color.DARK_GRAY);
        this.backgroundPanel.setLayout(new MigLayout(/*Layout Constraints*/ "", /*Column constraints*/ "", /*Row constraints*/ "")); // use the MigLayout manager also within the background panel

        // add the file format logos
        this.backgroundPanel.add(meilabel, "center, pushx, gap 20 20 30 30");
        this.backgroundPanel.add(msmlabel, "center, pushx, gap 20 20 30 30");
        this.backgroundPanel.add(midilabel, "center, pushx, gap 20 20 30 30");
        this.backgroundPanel.add(audiolabel, "center, pushx, gap 20 20 30 30, wrap");

        if (this.music.isEmpty()) {                                                         // if no files are loaded
            // create a file drop label
            this.dropLabel.setForeground(Color.GRAY);                                       // text color
            this.dropLabel.setHorizontalTextPosition(JLabel.CENTER);                        // center the label text within the label
            this.dropLabel.setFont(new Font("default", Font.PLAIN, 20));                    // font type, style and size
            this.backgroundPanel.add(dropLabel, "span, grow, push");                        // add it to the background panel
//            this.dropLabel.addMouseListener(new MouseAdapter() {                             // add a mouse listener to the button
//                @Override
//                public void mouseClicked(MouseEvent e) {                                    // when clicked
//                    fileOpenDialog();                                                       // open the file load dialog
//                }
//            });
        }
        else {                                                                              // otherwise there are files loaded with which we want to interact
            this.fileListPanel.removeAll();
            this.fileListPanel.setOpaque(false);                                            // set the file list panel transparent
            this.fileListPanel.setLayout(new BoxLayout(this.fileListPanel, BoxLayout.PAGE_AXIS));

            for (Mei4Gui m : this.music) {
                this.fileListPanel.add(m.getPanel());
            }

            this.backgroundPanel.add(this.fileListPanel, "span, grow, push");               // add the file list panel to the background panel
        }
    }

    private void stopAllMidiPlayback() {
        for (Mei4Gui mei : music) {
            for (Mei4Gui.Msm4Gui msm : mei.msm) {
                if ((msm.midi == null) || msm.midi.isEmpty())
                    continue;
                msm.midi.stop();
            }
        }
    }

    /**
     * an Mei extension for the windowed meico app
     */
    private class Mei4Gui extends Mei {
        private final JPanel panel;             // the actual gui extension
        private List<Msm4Gui> msm;              // corresponding msm objects
        private MeiCoApp app;                   // reference to the meico app
        private boolean idsAdded;
        private boolean copyofsResolved;
        private int ppq;
        private boolean dontUseChannel10 = true;

        /**
         * constructor
         * @param file
         */
        public Mei4Gui(File file, MeiCoApp app) throws InvalidFileTypeException, IOException, ParsingException, UnsupportedAudioFileException {
            this.msm = new ArrayList<Msm4Gui>();

            if (file.getName().substring(file.getName().length()-4).equals(".mei")) {           // if it is an mei file
                this.readMeiFile(file, false);                                                  // load it
            }
            else {                                                                              // otherwise try loading it as an msm (or midi) object
                this.msm.add(new Msm4Gui(file, app));                                                // load it into the msms list
            }

            this.panel = new JPanel();                                                          // initialize the gui component
            this.app = app;                                                                     // store the reference to the meico app
            this.idsAdded = false;
            this.copyofsResolved = false;
            this.ppq = 720;
        }

        /**
         * This method draws and returns the panel that the MeiCoApp displays.
         * @return
         */
        public JPanel getPanel() {
            // create the panel component and its content
            this.panel.removeAll();
            this.panel.setOpaque(false);
            this.panel.setLayout(new MigLayout(/*Layout Constraints*/ "wrap 12", /*Column constraints*/ "[left, " + fileNameFieldWidtch + "%:" + fileNameFieldWidtch + "%:" + fileNameFieldWidtch + "%][right, " + iconFieldWidth + "%:" + iconFieldWidth + "%:" + iconFieldWidth + "%][right, " + iconFieldWidth + "%:" + iconFieldWidth + "%:" + iconFieldWidth + "%][left, " + fileNameFieldWidtch + "%:" + fileNameFieldWidtch + "%:" + fileNameFieldWidtch + "%][right, " + iconFieldWidth + "%:" + iconFieldWidth + "%:" + iconFieldWidth + "%][right, " + iconFieldWidth + "%:" + iconFieldWidth + "%:" + iconFieldWidth + "%][left, " + fileNameFieldWidtch + "%:" + fileNameFieldWidtch + "%:" + fileNameFieldWidtch + "%][right, " + iconFieldWidth + "%:" + iconFieldWidth + "%:" + iconFieldWidth + "%][right, " + iconFieldWidth + "%:" + iconFieldWidth + "%:" + iconFieldWidth + "%][right, " + iconFieldWidth + "%:" + iconFieldWidth + "%:" + iconFieldWidth + "%][left, " + fileNameFieldWidtch + "%:" + fileNameFieldWidtch + "%:" + fileNameFieldWidtch + "%][right, " + iconFieldWidth + "%:" + iconFieldWidth + "%:" + iconFieldWidth + "%]", /*Row constraints*/ ""));

            int skip = 0;

            // mei components
            if (this.isEmpty()) {
                skip = 3;
            }
            else {
                JPopupMenu meiNamePop = new JPopupMenu("MEI Processing");
                meiNamePop.setEnabled(true);
                JMenuItem validate = new JMenuItem(new AbstractAction("Validate") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showMessageDialog(null, "Validation of " + getFile().getName() + " against mei-CMN.rng (meiversion 3.0.0 2016): " + validate() + ((isValid()) ? "." : ".\n Please read file meico.log for a detailed validation error message."));
                    }
                });
                validate.setEnabled(true);

                JMenuItem addIDs = new JMenuItem(new AbstractAction("Add IDs") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addIds();
                        idsAdded = true;
                        this.setEnabled(false);
                    }
                });
                addIDs.setEnabled(!this.idsAdded);

                JMenuItem resolveCopyofs = new JMenuItem(new AbstractAction("Resolve Copyofs") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        resolveCopyofs();
                        copyofsResolved = true;
                        this.setEnabled(false);
                    }
                });
                resolveCopyofs.setEnabled(!this.copyofsResolved);

                JMenuItem reload = new JMenuItem(new AbstractAction("Reload Original MEI") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        msm = new ArrayList<Msm4Gui>();
                        try {
                            readMeiFile(getFile(), false);
                        } catch (IOException err) {
                            app.setStatusMessage(err.toString());
                        } catch (ParsingException err) {
                            app.setStatusMessage(err.toString());
                        }
                        idsAdded = false;
                        copyofsResolved = false;
                        app.doRepaint();
                    }
                });
                reload.setEnabled(true);

                meiNamePop.add(validate);
                meiNamePop.add(addIDs);
                meiNamePop.add(resolveCopyofs);
                meiNamePop.add(reload);

                JLabel meiName = new JLabel(this.getFile().getName());
                meiName.setOpaque(true);
                meiName.setBackground(Color.LIGHT_GRAY);
                meiName.setForeground(Color.DARK_GRAY);
                meiName.setBorder(new EmptyBorder(0, 4, 0, 0));
                meiName.setFont(new Font(fontName, fontStyle, fontSize));
                meiName.setToolTipText("<html>" + this.getFile().getPath() + "<br>RIGHT CLICK: further MEI processing functions</html>");
                meiName.setComponentPopupMenu(meiNamePop);
                meiName.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        app.setStatusMessage(getFile().getPath());
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        app.setStatusMessage("");
                    }
                });

                final JLabel saveMei = new JLabel(saveIcon, JLabel.CENTER);
                saveMei.setOpaque(true);
                saveMei.setBackground(Color.LIGHT_GRAY);
                saveMei.setForeground(Color.DARK_GRAY);
                saveMei.setToolTipText("<html>LEFT CLICK: quick save with default filename <br> RIGHT CLICK: open save dialog</html>");
                saveMei.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        saveMei.setBackground(new Color(232, 232, 232));
                        app.setStatusMessage("Save MEI file to file system");
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (saveMei.getBackground() != Color.GRAY)
                            saveMei.setBackground(Color.LIGHT_GRAY);
                        app.setStatusMessage("");
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                        saveMei.setBackground(Color.GRAY);
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (saveMei.contains(e.getPoint())) {                                           // right click for file save dialog
                            if (e.isPopupTrigger()) {
                                JFileChooser chooser = new JFileChooser();                              // open the file save dialog
                                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {      // file save has been approved
                                    writeMei(chooser.getSelectedFile().getAbsolutePath());              // save it
                                }
                            }
                            else {                                                                      // quick save with default filename with left mouse button
                                writeMei();
                            }
                            saveMei.setBackground(new Color(232, 232, 232));
                        }
                        else
                            saveMei.setBackground(Color.LIGHT_GRAY);
                    }
                });

                final JTextField ppqField = new JTextField(Integer.toString(this.ppq), 10);
                ppqField.setHorizontalAlignment(SwingConstants.RIGHT);
                ppqField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        String input = ppqField.getText().trim();
                        String clean = "";
                        for (int i = 0; i < input.length(); ++i) {
                            if (Character.isDigit(input.charAt(i))) {
                                clean = clean + input.charAt(i);
                            }
                        }
                        if (clean.isEmpty())
                            clean = "720";
                        ppq = Integer.parseInt(clean);
                        ppqField.setText(Integer.toString(ppq));
                    }
                });
                ppqField.addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ppqField.getParent().getParent().setVisible(false);
                    }
                });

                JLabel ppqLabel = new JLabel("ppq");
                JPanel ppqPanel = new JPanel();
                JLabel ppqSetup = new JLabel("Set Time Resolution");
                JPopupMenu mei2msmPop = new JPopupMenu("Conversion Options");
                mei2msmPop.setEnabled(true);
                ppqPanel.add(ppqSetup);
                ppqPanel.add(ppqField);
                ppqPanel.add(ppqLabel);
                mei2msmPop.add(ppqPanel);

                final JCheckBoxMenuItem dontUseChannel10CheckBox = new JCheckBoxMenuItem("Do not use channel 10 (midi drum channel)", this.dontUseChannel10);
                dontUseChannel10CheckBox.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        dontUseChannel10 = !dontUseChannel10;
                        dontUseChannel10CheckBox.setState(dontUseChannel10);
                    }
                });
                mei2msmPop.add(dontUseChannel10CheckBox);

                final JLabel mei2msm = new JLabel(convertIcon, JLabel.CENTER);
                mei2msm.setOpaque(true);
                mei2msm.setBackground(Color.LIGHT_GRAY);
                mei2msm.setForeground(Color.DARK_GRAY);
                mei2msm.setToolTipText("<html>LEFT CLICK: convert to msm<br>RIGHT CLICK: conversion options</html>");
                mei2msm.setComponentPopupMenu(mei2msmPop);
                mei2msm.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        mei2msm.setBackground(new Color(232, 232, 232));
                        app.setStatusMessage("Convert to MSM");
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (mei2msm.getBackground() != Color.GRAY)
                            mei2msm.setBackground(Color.LIGHT_GRAY);
                        app.setStatusMessage("");
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e))
                            mei2msm.setBackground(Color.GRAY);
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            if (mei2msm.contains(e.getPoint())) {
                                msm.clear();
                                for (Msm m : exportMsm(ppq, dontUseChannel10)) {
                                    msm.add(new Msm4Gui(m, app));
                                }
                                mei2msm.setBackground(new Color(232, 232, 232));
                                app.doRepaint();
                            }
                            else
                                mei2msm.setBackground(Color.LIGHT_GRAY);
                        }
                    }
                });

                this.panel.add(meiName, "pushx, height 35px!, width " + fileNameFieldWidtch + "%!");
                this.panel.add(saveMei, "pushx, height 35px!, width " + iconFieldWidth + "%!");
                this.panel.add(mei2msm, "pushx, height 35px!, width " + iconFieldWidth + "%!");
            }

            // msm, midi and audio components
            for (Msm4Gui m : this.msm) {
                // the msm components
                if (m.isEmpty()) {
                    skip += 3;
                }
                else {
                    this.panel.add(m.getPanel()[0], "pushx, height 35px!, width " + fileNameFieldWidtch + "%!, skip " + skip);
                    this.panel.add(m.getPanel()[1], "pushx, height 35px!, width " + iconFieldWidth + "%!");
                    this.panel.add(m.getPanel()[2], "pushx, height 35px!, width " + iconFieldWidth + "%!");
                    skip = 0;
                }

                // the midi components
                if (m.midi == null) {
                    skip = 9;       // skip the 4 midi cells and the 4 mei cells of the next line
                }
                else {
                    if (m.midi.isEmpty()) {
                        skip = 10;
                    }
                    else {
                        this.panel.add(m.midi.getPanel()[0], "pushx, height 35px!, width " + fileNameFieldWidtch + "%!, skip " + skip);
                        this.panel.add(m.midi.getPanel()[1], "pushx, height 35px!, width " + iconFieldWidth + "%!");
                        this.panel.add(m.midi.getPanel()[2], "pushx, height 35px!, width " + iconFieldWidth + "%!");
                        this.panel.add(m.midi.getPanel()[3], "pushx, height 35px!, width " + iconFieldWidth + "%!");
                        skip = 0;       // all further msm and midi components that belong to this mei instance have 3 skips in the panel so that they are placed in the correct column
                    }

                    if ((m.midi.audio == null) || m.midi.audio.isEmpty()) {
                        skip = 5;
                    }
                    else {
                        this.panel.add(m.midi.audio.getPanel()[0], "pushx, height 35px!, width " + fileNameFieldWidtch + "%!, skip " + skip);
                        this.panel.add(m.midi.audio.getPanel()[1], "pushx, height 35px!, width " + iconFieldWidth + "%!");
                        skip = 3;
                    }
                }
            }

            return this.panel;
        }

        /**
         * an Msm extension for the windowed meico app
         */
        private class Msm4Gui extends Msm {
            private final JLabel[] panel;             // the actual gui extension
            private Midi4Gui midi;
            private MeiCoApp app;
            private boolean restsRemoved;
            private double bpm;
            private boolean generateProgramChanges = true;

            public Msm4Gui(Msm msm, MeiCoApp app) {
                this.setFile(msm.getFile().getPath());
                this.setDocument(msm.getDocument());
                this.panel = new JLabel[3];         // the name label, save label and msm2midi label
                this.midi = null;
                this.app = app;
                this.restsRemoved = false;
                this.bpm = 120.0;
            }

            /**
             * constructor
             * @param file
             */
            public Msm4Gui(File file, MeiCoApp app) throws InvalidFileTypeException, IOException, ParsingException, UnsupportedAudioFileException {
                this.midi = null;

                if (file.getName().substring(file.getName().length()-4).equals(".msm")) {       // if it is an msm file
                    this.readMsmFile(file, false);                                              // load it
                }
                else {                                                                          // otherwise try loading it as a midi file
                    this.midi = new Midi4Gui(file, app);
                }

                this.panel = new JLabel[3];     // the name label, save label and msm2midi label
                this.app = app;
                this.restsRemoved = false;
                this.bpm = 120.0;
            }

            /**
             * This method draws and returns the panel that the MeiCoApp displays.
             * @return
             */
            public JLabel[] getPanel() {
                if (this.isEmpty()) {                   // if no msm data loaded
                    this.panel[0] = new JLabel();       // return
                    this.panel[1] = new JLabel();       // empty
                    this.panel[2] = new JLabel();       // labels
                }
                else {
                    JPopupMenu msmNamePop = new JPopupMenu("MSM Processing");
                    msmNamePop.setEnabled(true);
                    JMenuItem removeRests = new JMenuItem(new AbstractAction("Remove Rests") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            removeRests();
                            restsRemoved = true;
                            this.setEnabled(false);
                        }
                    });
                    removeRests.setEnabled(!this.restsRemoved);
                    msmNamePop.add(removeRests);


                    JLabel msmName = new JLabel(this.getFile().getName());
                    msmName.setOpaque(true);
                    msmName.setBackground(Color.LIGHT_GRAY);
                    msmName.setForeground(Color.DARK_GRAY);
                    msmName.setBorder(new EmptyBorder(0, 4, 0, 0));
                    msmName.setFont(new Font(fontName, fontStyle, fontSize));
                    msmName.setToolTipText("<html>" + this.getFile().getPath() + "<br>RIGHT CLICK: further MSM processing functions</html>");
                    msmName.setComponentPopupMenu(msmNamePop);
                    msmName.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            app.setStatusMessage(getFile().getPath());
                        }
                        @Override
                        public void mouseExited(MouseEvent e) {
                            app.setStatusMessage("");
                        }
                    });

                    final JLabel saveMsm = new JLabel(saveIcon, JLabel.CENTER);
                    saveMsm.setOpaque(true);
                    saveMsm.setBackground(Color.LIGHT_GRAY);
                    saveMsm.setForeground(Color.DARK_GRAY);
                    saveMsm.setToolTipText("<html>LEFT CLICK: quick save with default filename <br> RIGHT CLICK: open save dialog</html>");
                    saveMsm.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            saveMsm.setBackground(new Color(232, 232, 232));
                            app.setStatusMessage("Save MSM file to file system");
                        }
                        @Override
                        public void mouseExited(MouseEvent e) {
                            if (saveMsm.getBackground() != Color.GRAY)
                                saveMsm.setBackground(Color.LIGHT_GRAY);
                            app.setStatusMessage("");
                        }
                        @Override
                        public void mousePressed(MouseEvent e) {
                            saveMsm.setBackground(Color.GRAY);
                        }
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if (saveMsm.contains(e.getPoint())) {
                                if (e.isPopupTrigger()) {                                                   // right click for file save dialog
                                    JFileChooser chooser = new JFileChooser();                              // open the fileopen dialog
                                    if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {      // file save has been approved
                                        writeMsm(chooser.getSelectedFile().getAbsolutePath());              // save it
                                    }
                                }
                                else {                                                                      // quick save with default filename with left mouse button
                                    writeMsm();
                                }
                                saveMsm.setBackground(new Color(232, 232, 232));
                            }
                            else
                                saveMsm.setBackground(Color.LIGHT_GRAY);
                        }
                    });

                    final JTextField bpmField = new JTextField(Double.toString(this.bpm), 10);
                    bpmField.setHorizontalAlignment(SwingConstants.RIGHT);
                    bpmField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            String input = bpmField.getText().trim();
                            String clean = "";
                            boolean dec = false;
                            for (int i = 0; i < input.length(); ++i) {
                                if (Character.isDigit(input.charAt(i))) {
                                    clean = clean + input.charAt(i);
                                }
                                if (input.charAt(i) == '.' && !dec) {
                                    dec = true;
                                    clean = clean + input.charAt(i);
                                }
                            }
                            if (clean.isEmpty())
                                clean = "120.0";
                            bpm = Double.parseDouble(clean);
                            bpmField.setText(Double.toString(bpm));
                        }
                    });
                    bpmField.addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            bpmField.getParent().getParent().setVisible(false);
                        }
                    });

                    final JCheckBoxMenuItem generateProgramChangesCheckBox = new JCheckBoxMenuItem("Generate Program Change Messages", this.generateProgramChanges);
                    generateProgramChangesCheckBox.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            generateProgramChanges = !generateProgramChanges;
                            generateProgramChangesCheckBox.setState(generateProgramChanges);
                            // TODO: if the new state is false send piano program changes to all channels so that the old settings are gone
                        }
                    });

                    JLabel ppqLabel = new JLabel("bpm");
                    JPanel bpmPanel = new JPanel();

                    JLabel bpmSetup = new JLabel("Set Tempo");
                    JPopupMenu msm2midiPop = new JPopupMenu("Conversion Options");
                    msm2midiPop.setEnabled(true);
                    bpmPanel.add(bpmSetup);
                    bpmPanel.add(bpmField);
                    bpmPanel.add(ppqLabel);
                    msm2midiPop.add(bpmPanel);
                    msm2midiPop.add(generateProgramChangesCheckBox);

                    final JLabel msm2midi = new JLabel(convertIcon, JLabel.CENTER);
                    msm2midi.setOpaque(true);
                    msm2midi.setBackground(Color.LIGHT_GRAY);
                    msm2midi.setForeground(Color.DARK_GRAY);
                    msm2midi.setComponentPopupMenu(msm2midiPop);
                    msm2midi.setToolTipText("<html>LEFT CLICK: convert to Midi<br>RIGHT CLICK: conversion options</html>");
                    msm2midi.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            msm2midi.setBackground(new Color(232, 232, 232));
                            app.setStatusMessage("Convert to Midi");
                        }
                        @Override
                        public void mouseExited(MouseEvent e) {
                            if (msm2midi.getBackground() != Color.GRAY)
                                msm2midi.setBackground(Color.LIGHT_GRAY);
                            app.setStatusMessage("");
                        }
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (SwingUtilities.isLeftMouseButton(e))
                                msm2midi.setBackground(Color.GRAY);
                        }
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if (SwingUtilities.isLeftMouseButton(e)) {
                                if (msm2midi.contains(e.getPoint())) {
                                    midi = new Midi4Gui(exportMidi(bpm, generateProgramChanges), app);
                                    msm2midi.setBackground(new Color(232, 232, 232));
                                    app.doRepaint();
                                }
                                else
                                    msm2midi.setBackground(Color.LIGHT_GRAY);
                            }
                        }
                    });

                    this.panel[0] = msmName;
                    this.panel[1] = saveMsm;
                    this.panel[2] = msm2midi;

                }

                return this.panel;
            }

            /**
             * a Midi extension for the windowed meico app
             */
            private class Midi4Gui extends meico.midi.Midi {
                protected final JLabel[] panel;             // the actual gui extension
                private Audio4Gui audio = null;
                private MeiCoApp app;

                /**
                 * constructor
                 * @param file
                 */
                public Midi4Gui(File file, MeiCoApp app) throws InvalidFileTypeException, IOException, UnsupportedAudioFileException {
                    if (file.getName().substring(file.getName().length()-4).equals(".mid")) {      // if it is not a midi file
                        try {
                            this.readMidiFile(file);
                        } catch (InvalidMidiDataException e) {
                            throw new InvalidFileTypeException(file.getName() + " invalid Midi file!");
                        } catch (IOException e) {
                            throw new InvalidFileTypeException(file.getName() + " invalid Midi file!");
                        }
                    }
                    else {
                        this.audio = new Audio4Gui(file, app);
                    }

                    this.panel = new JLabel[4];     // the name label, save label, play, and convert label
                    this.app = app;
                }

                /**
                 * constructor
                 */
                public Midi4Gui(meico.midi.Midi midi, MeiCoApp app) {
                    super(midi.getSequence(), midi.getFile());
                    this.panel = new JLabel[4];
                    this.app = app;
                }

                /**
                 * This method draws and returns the panel that the MeiCoApp displays.
                 * @return
                 */
                public JLabel[] getPanel() {
                    if (this.isEmpty()) {                   // if no msm data loaded
                        this.panel[0] = new JLabel();       // return
                        this.panel[1] = new JLabel();       // empty
                        this.panel[2] = new JLabel();       // labels
                        this.panel[3] = new JLabel();       //
                    }
                    else {
                        JLabel midiName = new JLabel(this.getFile().getName());
                        midiName.setOpaque(true);
                        midiName.setBackground(Color.LIGHT_GRAY);
                        midiName.setForeground(Color.DARK_GRAY);
                        midiName.setBorder(new EmptyBorder(0, 4, 0, 0));
                        midiName.setFont(new Font(fontName, fontStyle, fontSize));
                        midiName.setToolTipText("<html>" + this.getFile().getPath() + "</html>");
                        midiName.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                app.setStatusMessage(getFile().getPath());
                            }
                            @Override
                            public void mouseExited(MouseEvent e) {
                                app.setStatusMessage("");
                            }
                        });

                        final JLabel saveMidi = new JLabel(saveIcon, JLabel.CENTER);
                        saveMidi.setOpaque(true);
                        saveMidi.setBackground(Color.LIGHT_GRAY);
                        saveMidi.setForeground(Color.DARK_GRAY);
                        saveMidi.setToolTipText("<html>LEFT CLICK: quick save with default filename<br>RIGHT CLICK: open save dialog</html>");
                        saveMidi.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                saveMidi.setBackground(new Color(232, 232, 232));
                                app.setStatusMessage("Save Midi file to file system");
                            }
                            @Override
                            public void mouseExited(MouseEvent e) {
                                if (saveMidi.getBackground() != Color.GRAY)
                                    saveMidi.setBackground(Color.LIGHT_GRAY);
                                app.setStatusMessage("");
                            }
                            @Override
                            public void mousePressed(MouseEvent e) {
                                saveMidi.setBackground(Color.GRAY);
                            }
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                if (saveMidi.contains(e.getPoint())) {
                                    if (SwingUtilities.isLeftMouseButton(e)) {                                  // quick save with default filename with left mouse button
                                        try {
                                            writeMidi();
                                        } catch (IOException err) {
                                            app.setStatusMessage(err.toString());
                                        }
                                    }
                                    else {                                                                      // svae dialog with right mouse button
                                        JFileChooser chooser = new JFileChooser();                              // open the fileopen dialog
                                        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {      // file save has been approved
                                            try {
                                                writeMidi(chooser.getSelectedFile());                               // save it
                                            } catch (IOException err) {
                                                app.setStatusMessage(err.toString());
                                            }
                                        }
                                    }
                                    saveMidi.setBackground(new Color(232, 232, 232));
                                }
                                else
                                    saveMidi.setBackground(Color.LIGHT_GRAY);
                            }
                        });

                        final JLabel playMidi = new JLabel("\u25BA");
                        playMidi.setFont(new Font(fontName, fontStyle, fontSize));
                        playMidi.setHorizontalAlignment(JLabel.CENTER);
                        playMidi.setOpaque(true);
                        playMidi.setBackground(Color.LIGHT_GRAY);
                        playMidi.setForeground(Color.DARK_GRAY);
                        playMidi.setToolTipText("<html>play Midi file</html>");
                        playMidi.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                playMidi.setBackground(new Color(232, 232, 232));
                                app.setStatusMessage("Play Midi file");
                            }
                            @Override
                            public void mouseExited(MouseEvent e) {
                                if (playMidi.getBackground() != Color.GRAY)
                                    playMidi.setBackground(Color.LIGHT_GRAY);
                                app.setStatusMessage("");
                            }
                            @Override
                            public void mousePressed(MouseEvent e) {
                                playMidi.setBackground(Color.GRAY);
                            }
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                if (playMidi.contains(e.getPoint())) {
                                    if (playMidi.getText().equals("\u25A0")) {
                                        stop();
                                        playMidi.setText("\u25BA");
                                        playMidi.setBackground(new Color(232, 232, 232));
                                        return;
                                    }
                                    app.stopAllMidiPlayback();
                                    playMidi.setBackground(new Color(232, 232, 232));
                                    try {
                                        play();
                                    } catch (MidiUnavailableException e1) {
                                        e1.printStackTrace();
                                        return;
                                    } catch (InvalidMidiDataException e1) {
                                        e1.printStackTrace();
                                        return;
                                    }
                                    playMidi.setText("\u25A0");
                                }
                                else
                                    playMidi.setBackground(Color.LIGHT_GRAY);
                            }
                        });

                        final JLabel midi2audio = new JLabel(convertIcon, JLabel.CENTER);
                        midi2audio.setOpaque(true);
                        midi2audio.setBackground(Color.LIGHT_GRAY);
                        midi2audio.setForeground(Color.DARK_GRAY);
                        midi2audio.setToolTipText("<html>LEFT CLICK: convert to audio</html>");
                        midi2audio.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                midi2audio.setBackground(new Color(232, 232, 232));
                                app.setStatusMessage("Convert to audio");
                            }
                            @Override
                            public void mouseExited(MouseEvent e) {
                                if (midi2audio.getBackground() != Color.GRAY)
                                    midi2audio.setBackground(Color.LIGHT_GRAY);
                                app.setStatusMessage("");
                            }
                            @Override
                            public void mousePressed(MouseEvent e) {
                                if (SwingUtilities.isLeftMouseButton(e))
                                    midi2audio.setBackground(Color.GRAY);
                            }
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                if (SwingUtilities.isLeftMouseButton(e)) {
                                    if (midi2audio.contains(e.getPoint())) {
                                        try {
                                            Audio a = exportAudio();
                                            audio = new Audio4Gui(a.getAudioStream(), a.getFile(), app);
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        } catch (UnsupportedAudioFileException e1) {
                                            e1.printStackTrace();
                                        }
                                        midi2audio.setBackground(new Color(232, 232, 232));
                                        app.doRepaint();
                                    }
                                    else
                                        midi2audio.setBackground(Color.LIGHT_GRAY);
                                }
                            }
                        });

                        this.panel[0] = midiName;
                        this.panel[1] = saveMidi;
                        this.panel[2] = playMidi;
                        this.panel[3] = midi2audio;
                    }

                    return this.panel;
                }

                private class Audio4Gui extends meico.audio.Audio {
                    protected final JLabel[] panel;             // the actual gui extension
                    private MeiCoApp app;

                    /**
                     * constructor
                     * @param file
                     */
                    public Audio4Gui(AudioInputStream inputStream, File file, MeiCoApp app) throws IOException, UnsupportedAudioFileException {
                        super(inputStream, file);
                        this.panel = new JLabel[2];     // the name label, save label and play label
                        this.app = app;
                    }

                    /**
                     *
                     * @param file
                     * @param app
                     * @throws IOException
                     * @throws UnsupportedAudioFileException
                     */
                    public Audio4Gui(File file, MeiCoApp app) throws IOException, UnsupportedAudioFileException {
                        super(file);
                        this.panel = new JLabel[2];     // the name label, save label and play label
                        this.app = app;
                    }

                    /**
                     * This method draws and returns the panel that the MeiCoApp displays.
                     * @return
                     */
                    public JLabel[] getPanel() {
                        if (this.isEmpty()) {                   // if no msm data loaded
                            this.panel[0] = new JLabel();       // return
                            this.panel[1] = new JLabel();       // empty
                        }
                        else {
                            JLabel audioName = new JLabel(this.getFile().getName());
                            audioName.setOpaque(true);
                            audioName.setBackground(Color.LIGHT_GRAY);
                            audioName.setForeground(Color.DARK_GRAY);
                            audioName.setBorder(new EmptyBorder(0, 4, 0, 0));
                            audioName.setFont(new Font(fontName, fontStyle, fontSize));
                            audioName.setToolTipText("<html>" + this.getFile().getPath() + "</html>");
                            audioName.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseEntered(MouseEvent e) {
                                    app.setStatusMessage(getFile().getPath());
                                }
                                @Override
                                public void mouseExited(MouseEvent e) {
                                    app.setStatusMessage("");
                                }
                            });

                            final JLabel saveAudio = new JLabel(saveIcon, JLabel.CENTER);
                            saveAudio.setOpaque(true);
                            saveAudio.setBackground(Color.LIGHT_GRAY);
                            saveAudio.setForeground(Color.DARK_GRAY);
                            saveAudio.setToolTipText("<html>LEFT CLICK: quick save with default filename <br> RIGHT CLICK: open save dialog</html>");
                            saveAudio.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseEntered(MouseEvent e) {
                                    saveAudio.setBackground(new Color(232, 232, 232));
                                    app.setStatusMessage("Save audio file to file system");
                                }
                                @Override
                                public void mouseExited(MouseEvent e) {
                                    if (saveAudio.getBackground() != Color.GRAY)
                                        saveAudio.setBackground(Color.LIGHT_GRAY);
                                    app.setStatusMessage("");
                                }
                                @Override
                                public void mousePressed(MouseEvent e) {
                                    saveAudio.setBackground(Color.GRAY);
                                }
                                @Override
                                public void mouseReleased(MouseEvent e) {
                                    if (saveAudio.contains(e.getPoint())) {
                                        if (SwingUtilities.isLeftMouseButton(e)) {                                  // quick save with default filename with left mouse button
                                            try {
                                                writeAudio();
                                            } catch (IOException err) {
                                                app.setStatusMessage(err.toString());
                                            }
                                        }
                                        else {                                                                      // svae dialog with right mouse button
                                            JFileChooser chooser = new JFileChooser();                              // open the fileopen dialog
                                            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {      // file save has been approved
                                                try {
                                                    writeAudio(chooser.getSelectedFile().getAbsolutePath());        // save it
                                                } catch (IOException err) {
                                                    app.setStatusMessage(err.toString());
                                                }
                                            }
                                        }
                                        saveAudio.setBackground(new Color(232, 232, 232));
                                    }
                                    else
                                        saveAudio.setBackground(Color.LIGHT_GRAY);
                                }
                            });

                            this.panel[0] = audioName;
                            this.panel[1] = saveAudio;
                        }

                        return this.panel;
                    }
                }
            }
        }
    }
}