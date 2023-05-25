![](https://github.com/cemfi/meico/blob/master/figures/meico_title_small.png)

[![GitHub release](https://img.shields.io/github/release/cemfi/meico.svg)](https://github.com/cemfi/meico/releases/latest) 
[![documentation](https://img.shields.io/badge/doc-JavaDoc-green.svg)](http://cemfi.github.io/meico/)
[![LGPL v3](https://img.shields.io/github/license/cemfi/meico.svg)](https://github.com/cemfi/meico/blob/master/LICENSE) 

Author: [Axel Berndt](https://github.com/axelberndt)<br>
MEI support: [Benjamin W. Bohl](https://github.com/bwbohl), [Johannes Kepper](https://github.com/kepper)<br>
Contributor: [Simon Waloschek](https://github.com/sonovice)<br>
[Center of Music and Film Informatics](http://www.cemfi.de/), Detmold

#### Java Compatibility
- meico ![Java compatibility 1.8+](https://img.shields.io/badge/java-1.8%2B-blue.svg)
- meicoApp ![Java compatibility](https://img.shields.io/badge/Java-1.8--10-blue)

Meico is a converter framework for MEI files. MEI offers an invaluable combination of symbolic music data and additional information far beyond the typical metadata found in other formats. All this is often based on musicological research and features an accordingly high scientific quality. Digital music editions motivate new interesting research questions highly relevant to the field of Music Information Retrieval (MIR) and the demand to gain deeper insight into subjects such as composition styles, performance practices, historical change processes in music tradition, and how all these reflect in the musical works edited. In this, MIR can make valuable contributions to musicology, for instance by providing tools to work on large corpora of MEI encoded music. Further application scenarios include digital music stand technology, music notation and music production. Even though MEI is a quasi-standard for digital music editions, there is few software support for it. Processing MEI encoded music is by far not a trivial task and many application scenarios have their own more established and efficient formats. With meico we address these issues. Meico implements methods to convert MEI data to several other formats, making MEI encodings accessible to a variety of applications. We presented meico at the Audio Mostly conference in 2018, the paper can be found [here](http://www.cemfi.de/wp-content/papercite-data/pdf/berndt-2018-meico.pdf) and in the ACM Digital Library. The following features are implemented:

- MEI to MSM conversion (with variable time resolution in pulses per quarter, ppq),
- MEI to MPM conversion (performance data is exported together with MSM)
- MSM conversion to MIDI (raw and expressive performance) and sequences of chroma and absolute pitch vectors,
- MIDI to PCM audio and MP3 conversion (with freely choosable SoundFont and Downloadable Sounds),
- MIDI to MSM conversion,
- PCM audio (WAV) to MP3 conversion and vice versa,
- MEI score rendering (based on Verovio, only in the meicoApp),
- MEI processing functions (validation, `xml:id` generation, resolution of elements with `copyof` attribute, conversion of `expansion` elements into "through-composed" MEI code ...),
- MusicXML processing (via the ProxyMusic functionalities),
- MSM processing functions (validation, remove rest elements from the score, expand repetitions encoded in the `sequencingMap` ...),
- MPM processing functions (validation, making part-specific tempo and dynamics instructions global, meico integrates the official MPM API with all its functionality for creation, editing and performance rendering)
- an instrument dictionary and several string matching algorithms to map staff names to MIDI program change numbers,
- basic MIDI and audio playback,
- two standalone modes (command line mode, desktop gui mode),
- a REST API,
- processing of XML sources with XSLT stylesheets, e.g. to convert MEI to MusicXML using the Music Encoding Initiative's `mei2musicxml.xsl` stylesheet from the [MEI Encoding Tools GitHub](https://github.com/music-encoding/encoding-tools).

There are several features open, though. Currently, meico ignores any MEI data that is concerned with ornamentation (trill, mordent, arpeggio, tremolo). We would also like to include MusicXML support. Several MEI elements and attributes are not supported so far (e.g. `meterSigGrp`, `uneume`, `lyrics`). Meico implements a default method to resolve ambiguity (e.g., choose a reading from different alternatives in MEI). If other choices should be made, the user can use [MEI Sequence Editor](http://nashira.uni-paderborn.de:5555/seditor) to prepare an unambiguous MEI file.

### How to use meico?

Meico can be used in several different ways. The file `meicoApp.jar` (see the [latest release](https://github.com/cemfi/meico/releases/latest)) is a standalone runnable Java program. We have tested it under Windows, Mac OS and Linux. The only prerequisite is that you have a Java 1.8 (or higher) Runtime Environment installed on your system.

Starting the standalone application `meicoApp.jar` without any command line options will start the window mode. Simply drag your files into the window. The startup screen lists the supported input file formats. You can find context information on each interface element in the statusbar. If you have several `mdiv` elements (movements) in your MEI document you will get an individual MSM/MPM pair for each movement on conversion. Conversion from MIDI to audio may take some time when it is a long piece. To get better quality sounds than Java's built-in default instruments, we recommend using a higher-quality soundbank, such as one of [these soundfonts](https://sourceforge.net/projects/androidframe/files/soundfonts/). Simply drag and drop them into the workspace and activate them via their menu or double click. XSL files are used in the same way. If you want to apply an XSL Transform to your MEI or other XML data, drop the XSL on the workspace, activate it and transform. Soundfonts and XSLTs can also be set as standard so that this loading procedure is not necessary on the next startup.

![A screenshot of the meico graphical user interface.](https://github.com/cemfi/meico/blob/master/figures/meico-screenshot_01.png)

The command line mode expects the following command line options:

Usage: `java -jar meicoApp.jar [OPTIONS] FILE`

| Option                            | Description                                                                                                                         |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `-?`, `--help`                    | show this help text                                                                                                                 |
| `-v FILE`, `--validate FILE`      | validate loaded MEI file against given schema (e.g. `C:\mei-CMN.rng`)                                                               |
| `-a`, `--add-ids`                 | add missing `xml:id`s to note, rest and chord elements in MEI;<br>meico will output a revised MEI file                              |
| `-r`, `--resolve-copy-ofs`        | resolve elements with `copyof` and `sameas` attributes into selfcontained elements<br>with unique `xml:id`; meico will output a revised MEI file |
| `-n`, `--ignore-repetitions`      | meico automatically expands repetition marks, use this option to prevent this step                                                  |
| `-e`, `--ignore-expansions`       | expansions in MEI indicate a rearrangement of the source material, use this option to prevent this step                             |
| `-ex`, `--expressive`             | convert to expressive MIDI                                                                                                          |
| `-x FILE argument`, `--xslt FILE argument` | apply an XSL transform `FILE` (e.g. `C:\mei2musicxml.xsl`) to the MEI source and store the result with file extension defined by `argument` (e.g. `"mxl"`) |
| `-m`, `--msm`                     | convert to MSM                                                                                                                      |
| `-f`, `--mpm`                     | convert to MPM                                                  |
| `-o`, `--chroma`                  | convert to chromas                                                                                                                  |
| `-h`, `--pitches`                 | convert to pitches                                                                                                                  |
| `-i`, `--midi`                    | convert to MIDI                                                                               |
| `-p`, `--no-program-changes`      | suppress program change events in MIDI, all music will be played by piano                                                           |
| `-c`, `--dont-use-channel-10`     | do not use channel 10 (drum channel) in MIDI                                                                                        |
| `-t argument`, `--tempo argument` | set MIDI tempo (bpm), default is 120 bpm                                                                                            |
| `-w`, `--wav`                     | convert to Wave                                                                                                                     |
| `-3`, `--mp3`                     | convert to MP3                                                                                                                      |
| `-q`, `--cqt`                     | convert the audio to CQT spectrogram                                                                                                |
| `-s FILE`, `--soundbank FILE`     | use a specific sound bank file (.sf2, .dls) for Wave conversion                                                                     |
| `-d`, `--debug`                   | write additional debug versions of MEI and MSM                                                                                      |


The final argument should always be a path to a valid MEI file (e.g., `"C:\myMeiCollection\test.mei"`); always in quotes! This is the only mandatory argument if you want to convert something.

The third way of using meico is as a Java programming library. For this, use the "headless" meico framework `meico.jar` that is free from application-related overhead and dependencies. Its `Mei`, `Msm`, `Mpm`, `Midi`, and `Audio` classes are the most important to work with. In the `meicoApp` branch we demonstrate its use, the [commandline app](https://github.com/cemfi/meico/blob/meicoApp/src/meico/app/Main.java) is probably the best way to start with. A [Javadoc](http://cemfi.github.io/meico/) is provided with this repository.

### Build Instructions

Meico can quickly be built using [Ant](http://ant.apache.org/):
```bash
$ git clone https://github.com/cemfi/meico.git
$ cd meico
$ ant
```
The resulting `meico.jar` can be found in `out/artifacts/meico`.

### Music Performance Markup

Since version 0.7.0 meico integrates the official Application Programming Interface (API) for the Music Performance Markup (MPM) format. The format's schema definition and documentation can be found [here](https://github.com/axelberndt/MPM). Basically, this format provides means to describe in a formalized way how a musical work is played by musicians. This includes aspects such as timing, dynamics and articulation. However, MPM is not only meant to serve analytical purposes such as musicological performance research. It is also designed for performance modelling, i.e., users can specify expressive performances themselves and render them into expressive MIDI sequences. This is where the MPM API comes into play. It provides all that is required to develop applications for the creation and editing of MPMs. This includes a fully-fledged rendering engine that is fed with an MSM (or MIDI, or MEI, meico can convert them to MSM) and generates an augmented MSM that can be exported to expressive MIDI or audio. In meico's graphical user interface the users only need to have an MPM object in the workspace, activate one of its performaces and then export an MSM to expressive MIDI. Furthermore, package `meico.app` contains a demo class called `Humanizer`; its functionality is integrated in the graphical user interface and can be used to add some basic humanizing to the performance. 

Some words on the MEI to MPM export: Meico generates only the performance information that are encoded in MEI, nothing additional. So, do not expect this to make for a good or even humanly expressive performance. It is, however, good for proof-reading, e.g. to check whether a performance instruction is associated to the right staffs/parts, or if a tempo instruction has been encoded as text instead of tempo etc. It may also serve as a starting point to be edited and develop a more elaborate interpretation. Meico does its best to, e.g., suggest bpm values for literal tempo instructions and set default parameters of articulation definitions (see the [MEI Coverage Documentation](https://github.com/cemfi/meico/tree/master/MEI_Coverage_Documentation.md) for all details). But meico's default articulations, tempi, and dynamics may not be the right for a specific musical context. All this is meant to be refined and extended by the users according to aethetic demands or measurements of specific human performances etc. Editing tools and introductory workshops are planned in 2020-2022.

### License information

Meico makes use of the following third party libraries:
- [XOM](http://www.xom.nu/) v1.3.8 by Elliotte Rusty Harold, GNU Lesser General Public License (LGPL) version 2.1.
- [Java-String-Similarity](https://github.com/tdebatty/java-string-similarity) v1.0.0 by Thibault Debatty, MIT license.
- [Jing](http://www.thaiopensource.com/relaxng/jing.html) v20091111 by James Clark (Thai Open Source Software Center Ltd), see `copying.txt` provided in file `jing-20091111.jar`.
- [Saxon](http://saxon.sourceforge.net/) v9.8.0.14 HE by Saxonica (founder Michael Kay), Mozilla Public License 1.0 (MPL), Mozilla Public License 2.0 (MPL 2.0).
- [JSON.simple](https://cliftonlabs.github.io/json-simple/) v3.0.2 by Yidong Fang, Chris Nokleberg, Dave Hughes, and Davin Loegering, Apache License 2.0.
- [Java LAME](https://github.com/nwaldispuehl/java-lame) v3.98.4 by Ken Händel and Nico Waldispühl, GNU LGPL version 3.0.
- [Midi2WavRenderer](https://github.com/cemfi/meico/tree/master/src/meico/midi/Midi2AudioRenderer.java) by Karl Helgason, copyright notice in the class header.
- [Gervill Software Sound Synthesizer](https://sourceforge.net/projects/rasmusdsp/files/gervill/Gervill%201.0/) v1.0.1 by Karl Helgason, GPL 2.0.
- [Jipes](https://www.tagtraum.com/jipes/) v0.9.17 by Hendrik Schreiber, GNU Lesser General Public License (LGPL) version 2.1.
- [ProxyMusic](https://github.com/Audiveris/proxymusic) v3.0.1 by Hervé Bitteur, Maxim Poliakovski and Peter Greth, GNU LGPL version 3.0.
- [SLF4J API](https://www.slf4j.org/) v2.0.7 and [SLF4J Simple](https://www.slf4j.org/) v2.0.7 by QOS.CH Sarl, MIT license.

We publish meico under GNU GPL version 3.0. Meico's development was part of the ZenMEM project funded by the German Federal Ministry of Education and Research (2015-2019, funding code 01UG1414A–C). All MPM-related parts of meico were part of an R&D project that was funded by the [Fritz Thyssen Foundation](https://www.fritz-thyssen-stiftung.de/en/) (2019-2022). If you integrate meico or parts of it with your project make sure that you do not conflict with any of the above licenses.

