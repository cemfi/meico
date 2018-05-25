package meico.app;

import meico.Meico;
import meico.audio.Audio;
import meico.pitches.Pitches;
import meico.mei.Helper;
import meico.mei.Mei;
import meico.msm.Msm;
import nu.xom.ParsingException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a standalone application of meico that implements the commandline interface. If meico is executed without commandline options, class MeicoApp is started that implements a GUI for meico.
 * @author Axel Berndt.
 */
public class Main {
    /**
     * the main method to run meico
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {                         // if meico.jar is called without command line arguments
            new MeicoApp("Meico: MEI Converter v" + Meico.version, true);// start meico in window mode
        }
        else                                            // in case of command line arguments
            System.exit(commandLineMode(args));         // run the command line mode
    }

    /**
     * call this method to start the program in command line mode
     * it shows you all you need if you want to use meico in your application
     *
     * @param args see help output below
     */
    public static int commandLineMode(String[] args) {
        for (String arg : args) {
            if (arg.equals("-?") || arg.equals("--help")) {
                System.out.println("Meico: MEI Converter v" + Meico.version + "\nMeico requires the following arguments:\n");
                System.out.println("[-?] or [--help]                        show this help text");
                System.out.println("[-v] or [--validate]                    validate loaded MEI file");
                System.out.println("[-a] or [--add-ids]                     add xml:ids to note, rest and chord elements in MEI, as far as they do not have an id; meico will output a revised MEI file");
                System.out.println("[-r] or [--resolve-copy-ofs]            resolve elements with 'copyof' attributes into selfcontained elements with own xml:id; meico will output a revised MEI file");
                System.out.println("[-e] or [--ignore-expansions]           expansions in MEI indicate a rearrangement of the source material, use this option to prevent this step");
                System.out.println("[-x argument argument] or [--xslt argument argument] apply an XSL transform (first argument) to the MEI source and store the result with file extension defined by second argument");
                System.out.println("[-m] or [--msm]                         convert to MSM");
                System.out.println("[-o] or [--chroma]                      convert to chromas");
                System.out.println("[-h] or [--pitches]                     convert to pitches");
                System.out.println("[-i] or [--midi]                        convert to MIDI (and internally to MSM)");
                System.out.println("[-p] or [--no-program-changes]          suppress program change events in MIDI");
                System.out.println("[-c] or [--dont-use-channel-10]         do not use channel 10 (drum channel) in MIDI");
                System.out.println("[-t argument] or [--tempo argument]     set MIDI tempo (bpm), default is 120 bpm");
                System.out.println("[-w] or [--wav]                         convert to Wave (and internally to MSM and MIDI)");
                System.out.println("[-3] or [--mp3]                         convert to MP3");
                System.out.println("[-s argument] or [--soundbank argument] use a specific sound bank file (.sf2, .dls) for Wave conversion");
                System.out.println("[-d] or [--debug]                       write additional debug version of MEI and MSM");
                System.out.println("\nThe final argument should always be a path to a valid mei file (e.g., \"C:\\myMeiCollection\\test.mei\"); always in quotes! This is the only mandatory argument if you want to convert something.");
//                System.exit(0);
            }
        }

        // what does the user want meico to do with the file just loaded?
        boolean validate = false;
        boolean addIds = false;
        boolean resolveCopyOfs = false;
        boolean ignoreExpansions = false;
        File xslt = null;
        String xsltOutputExtension = "";
        boolean msm = false;
        boolean chroma = false;
        boolean pitches = false;
        boolean midi = false;
        boolean wav = false;
        boolean mp3 = false;
        boolean generateProgramChanges = true;
        boolean dontUseChannel10 = false;
        boolean debug = false;
        double tempo = 120;
        File soundbank = null;
        for (int i = 0; i < args.length-1; ++i) {
            if ((args[i].equals("-v")) || (args[i].equals("--validate"))) { validate = true; continue; }
            if ((args[i].equals("-a")) || (args[i].equals("--add-ids"))) { addIds = true; continue; }
            if ((args[i].equals("-r")) || (args[i].equals("--resolve-copy-ofs"))) { resolveCopyOfs = true; continue; }
            if ((args[i].equals("-e")) || (args[i].equals("--ignore-expansions"))) { ignoreExpansions = true; continue; }
            if ((args[i].equals("-m")) || (args[i].equals("--msm"))) { msm = true; continue; }
            if ((args[i].equals("-o")) || (args[i].equals("--chroma"))) { chroma = true; continue; }
            if ((args[i].equals("-h")) || (args[i].equals("--pitches"))) { pitches = true; continue; }
            if ((args[i].equals("-i")) || (args[i].equals("--midi"))) { midi = true; continue; }
            if ((args[i].equals("-w")) || (args[i].equals("--wav"))) { wav = true; continue; }
            if ((args[i].equals("-3")) || (args[i].equals("--mp3"))) { mp3 = true; continue; }
            if ((args[i].equals("-p")) || (args[i].equals("--no-program-changes"))) { generateProgramChanges = false; continue; }
            if ((args[i].equals("-c")) || (args[i].equals("--dont-use-channel-10"))) { dontUseChannel10 = true; continue; }
            if ((args[i].equals("-d")) || (args[i].equals("--debug"))) { debug = true; continue; }
            if ((args[i].equals("-t")) || (args[i].equals("--tempo"))) { tempo = Integer.parseInt(args[++i]); continue; }
            if ((args[i].equals("-x")) || (args[i].equals("--xslt"))) {
                xslt = new File(args[++i]);
                try {
                    xslt = new File(xslt.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    xslt = null;
                }
                xsltOutputExtension = args[++i];
                continue;
            }
            if ((args[i].equals("-s")) || (args[i].equals("--soundbank"))) {
                soundbank = new File(args[++i]);
                try {
                    soundbank = new File(soundbank.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    soundbank = null;
                }
                continue;
            }
            System.err.println("Error: Invalid argument: " + args[i] + ".");    // an invalid argument
//            return 64;
        }

        // load the file
        File meiFile;
        try {
            System.out.println("Loading file: " + args[args.length-1]);
            meiFile = new File(args[args.length-1]);                    // load mei file
            meiFile = new File(meiFile.getCanonicalPath());             // ensure that the absolute path in the file object

        } catch (NullPointerException | IOException error) {
            error.printStackTrace();                                    // print error to console
            System.err.println("MEI file could not be loaded.");
            return 66;
        }
        Mei mei;
        try {
            mei = new Mei(meiFile, validate);                           // read an mei file
        } catch (IOException | ParsingException e) {
            System.err.println("MEI file is not valid.");
            e.printStackTrace();
            return 65;
        }
        if (mei.isEmpty()) {
            System.err.println("MEI file could not be loaded.");
            return 66;
        }

        // xsl transform
        if (xslt != null) {
            System.out.println("Performing XSL transform.");
            String xsltOutput = mei.xslTransformToString(xslt);
            Helper.writeStringToFile(xsltOutput, Helper.getFilenameWithoutExtension(mei.getFile().getPath()) + "." + xsltOutputExtension);
        }

        // optional mei processing functions
        if (resolveCopyOfs) {
            System.out.println("Processing MEI: resolving copyofs.");
            mei.resolveCopyofs();                       // this call is part of the exportMsm() method but can also be called alone to expand the mei source and write it to the file system
        }
        if (addIds) {
            System.out.println("Processing MEI: adding xml:ids.");
            mei.addIds();                               // generate ids for note, rest, mRest, multiRest, and chord elements that have no xml:id attribute
        }
        if (resolveCopyOfs || addIds) {
            mei.writeMei();                             // this outputs an expanded mei file with more xml:id attributes and resolved copyof's
        }

        if (!(msm || midi || wav || mp3)) return 0;     // if no conversion is required, we are done here

        // convert mei -> msm -> midi
        System.out.println("Converting MEI to MSM.");
        List<Msm> msms = mei.exportMsm(720, dontUseChannel10, ignoreExpansions, !debug);    // usually, the application should use mei.exportMsm(720); the cleanup flag is just for debugging (in debug mode no cleanup is done)
        if (msms.isEmpty()) {
            System.err.println("MSM data could not be created.");
            return 1;
        }

        // instead of using sequencingMaps (which encode repetitions, dacapi etc.) in the msm objects we resolve them and, hence, expand all scores and maps according to the sequencing information
        for (int i=0; i < msms.size(); ++i) {           // we do this in all msm objects just exported from mei
            msms.get(i).resolveSequencingMaps();        // this is the function call to do it (be aware: the sequencingMaps are deleted because they no longer apply)
        }

        if (debug) {
            mei.writeMei(Helper.getFilenameWithoutExtension(mei.getFile().getPath()) + "-debug.mei"); // After the msm export, there is some new stuff in the mei ... mainly the date and dur attribute at measure elements (handy to check for numeric problems that occured during conversion), some ids and expanded copyofs. This was required for the conversion and can be output with this function call. It is, however, mainly interesting for debugging.
        }

        if (msm) {
            System.out.println("Writing MSM to file system: ");
            for (Msm msm1 : msms) {
                if (!debug) msm1.removeRests();  // purge the data (some applications may keep the rests from the mei; these should not call this function)
                msm1.writeMsm();                 // write the msm file to the file system
                System.out.println("\t" + msm1.getFile().getPath());
            }
        }

        if (chroma) {
            System.out.println("Converting MSM to chromas.");
            List<Pitches> chromas = new ArrayList<>();
            for (int i = 0; i < msms.size(); ++i) {
                chromas.add(msms.get(i).exportChroma());
            }
            System.out.println("Writing chroma to file system: ");
            for (int i = 0; i < chromas.size(); ++i) {
                String filename = Helper.getFilenameWithoutExtension(chromas.get(i).getFile().getPath()) + "-chroma.json";
                chromas.get(i).writePitches(filename);
                System.out.println("\t" + chromas.get(i).getFile().getPath());
            }
        }

        if (pitches) {
            System.out.println("Converting MSM to pitches.");
            List<Pitches> pitchesList = new ArrayList<>();
            for (int i = 0; i < msms.size(); ++i) {
                pitchesList.add(msms.get(i).exportPitches());
            }
            System.out.println("Writing pitches to file system: ");
            for (int i = 0; i < pitchesList.size(); ++i) {
                String filename = Helper.getFilenameWithoutExtension(pitchesList.get(i).getFile().getPath()) + "-pitches.json";
                pitchesList.get(i).writePitches(filename);
                System.out.println("\t" + pitchesList.get(i).getFile().getPath());
            }
        }

        if (!(midi || wav || mp3)) return 0;     // if no further conversion is required, we are done here

        List<meico.midi.Midi> midis = new ArrayList<>();
        System.out.println("Converting MSM to MIDI.");
        for (int i = 0; i < msms.size(); ++i) {
            midis.add(msms.get(i).exportMidi(tempo, generateProgramChanges));    // convert msm to midi
        }

        if (midi) {
            System.out.println("Writing MIDI to file system: ");
            for (int i = 0; i < midis.size(); ++i) {
                midis.get(i).writeMidi();    // write midi file to the file system
                System.out.println("\t" + midis.get(i).getFile().getPath());
            }
        }

        if (!(wav || mp3)) return 0;            // if no further conversion is required, we are done here

        List<meico.audio.Audio> audios = new ArrayList<>();
        System.out.println("Converting MIDI to Audio.");
        for (int i = 0; i < midis.size(); ++i) {
            Audio a = midis.get(i).exportAudio(soundbank);     // this generates an Audio object
            if (a == null)
                continue;
            audios.add(a);
        }

        if (wav) {
            System.out.println("Writing Wave to file system: ");
            for (int i = 0; i < audios.size(); ++i) {
                audios.get(i).writeAudio();
                System.out.println("\t" + audios.get(i).getFile().getPath());
            }
        }

        if (mp3) {
            System.out.println("Writing MP3 to file system: ");
            for (int i = 0; i < audios.size(); ++i) {
                audios.get(i).writeMp3();
                System.out.println("\t" + Helper.getFilenameWithoutExtension(audios.get(i).getFile().getPath()) + ".mp3");
            }
        }

        return 0;
    }
}
