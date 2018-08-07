# meico: MEI Converter
[![GitHub release](https://img.shields.io/github/release/cemfi/meico.svg)](https://github.com/cemfi/meico/releases/latest) [![LGPL v3](https://img.shields.io/github/license/cemfi/meico.svg)](https://github.com/cemfi/meico/blob/master/LICENSE) [![Java compatibility 1.8+](https://img.shields.io/badge/java-1.8%2B-blue.svg)](https://travis-ci.org/cemfi/meico)

Author: [Axel Berndt](https://github.com/axelberndt)<br>
MEI support: [Benjamin W. Bohl](https://github.com/bwbohl), [Johannes Kepper](https://github.com/kepper)<br>
Contributor: [Simon Waloschek](https://github.com/sonovice)<br>
[Center of Music and Film Informatics](http://www.cemfi.de/), Detmold

Meico is a converter framework for MEI files. Even though MEI is a quasi-standard for digital music editions, there is few software support for it. If you want to listen to the music in your MEI file, you need a MIDI or audio export. If you want to process the musical data (e.g., for Music Information Retrieval), there are many better suited formats and representations than MEI. With meico we address these issues. Meico implements methods to convert MEI data into the MSM (Musical Sequence Markup) format, an intermediate format that we defined for further use in other projects. From MSM, the MIDI export and audio rendering are quite straight forward. Currently, meico is a beta release. The following features are implemented:

- MEI to MSM conversion (with variable time resolution in pulses per quarter, ppq),
- MSM conversion to MIDI and sequences of chroma and absolute pitch vectors,
- MIDI to audio PCM and MP3 conversion (with freely choosable SoundFont and Downloadable Sounds),
- MEI processing functions (validation, `xml:id` generation, resolution of elements with `copyof` attribute, conversion of `expansion` elements into "through-composed" MEI code),
- MSM processing functions (remove rest elements from the score, expand repetitions encoded in the `sequencingMap`),
- an instrument dictionary that uses several string matching algorithms to map staff names to MIDI program change numbers,
- basic MIDI and audio playback,
- two standalone modes (command line mode, window mode),
- processing of XML sources with XSLT stylesheets, e.g. to convert MEI to MusicXML using the Music Encoding Initiative's `mei2musicxml.xsl` stylesheet from the [MEI Encoding Tools GitHub](https://github.com/music-encoding/encoding-tools).

There are several features open, though. Currently, meico ignores any MEI data that is concerned with expressive performance (tempo, dynamics, articulation, ornamentation). Several MEI elements and attributes are not supported so far (e.g. `meterSigGrp`, `uneume`, `lyrics`). Meico implements a default method to resolve ambiguity (e.g., choose a reading from different alternatives in MEI). If other choices should be made, the user can use [MEI Sequence Editor](http://nashira.uni-paderborn.de:5555/seditor) to prepare an unambiguous MEI file.

### How to use meico?

Meico can be used in several different ways. The jar file (see the [latest release](https://github.com/cemfi/meico/releases/latest)) is a standalone runnable Java program. We have tested it under Windows, Mac OS and Linux. The only prerequisite is that you have a Java 1.7 (or higher) Runtime Environment installed on your computer. 

Starting the standalone jar without any command line options will start the window mode of meico. Simply drag your MEI, MSM, MIDI, and Wave files into the window. You can find context information on each interface element in the tooltips and statusbar. There are several additional functions accessible via right click. If you have several mdivs in your MEI document you will get an individual MSM instance for each movement. Conversion from MIDI to audio may take some time when it is a long piece. We have not built in a progress display yet. Just be patient until the conversion button changes its color back and the audio data appears. To get better quality sounds than Java's built-in default instruments (those used for the Midi playback function), we recommend downloading one of [these soundfonts](https://sourceforge.net/projects/androidframe/files/soundfonts/) and use it via right clicking the Midi-to-audio conversion button, option "Choose soundbank". 

![A screenshot of the meico graphical user interface.](https://raw.githubusercontent.com/cemfi/meico/master/figures/meico-screenshot-new.png)

The command line mode expects the following command line options:

Usage: `java -jar meico.jar [OPTIONS] FILE`

| Option                            | Description                                                                                                                         |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `-?`, `--help`                    | show this help text                                                                                                                 |
| `-v`, `--validate`                | validate loaded MEI file                                                                                                            |
| `-a`, `--add-ids`                 | add missing `xml:id`s to note, rest and chord elements in MEI;<br>meico will output a revised MEI file                              |
| `-r`, `--resolve-copy-ofs`        | resolve elements with `copyof` attributes into selfcontained elements<br>with unique `xml:id`; meico will output a revised MEI file |
| `-e`, `--ignore-expansions`       | expansions in MEI indicate a rearrangement of the source material, use this option to prevent this step                             |
| `-x FILE argument`, `--xslt FILE argument` | apply an XSL transform `FILE` (e.g. `C:\mei2musicxml.xsl`) to the MEI source and store the result with file extension defined by `argument` (e.g. `"mxl"`) |
| `-m`, `--msm`                     | convert to MSM                                                                                                                      |
| `-o`, `--chroma`                  | convert to chromas                                                                                                                  |
| `-h`, `--pitches`                 | convert to pitches                                                                                                                  |
| `-i`, `--midi`                    | convert to MIDI (and internally to MSM)                                                                                             |
| `-p`, `--no-program-changes`      | suppress program change events in MIDI, all music will be played by piano                                                           |
| `-c`, `--dont-use-channel-10`     | do not use channel 10 (drum channel) in MIDI                                                                                        |
| `-t argument`, `--tempo argument` | set MIDI tempo (bpm), default is 120 bpm                                                                                            |
| `-w`, `--wav`                     | convert to Wave (and internally to MSM and MIDI)                                                                                    |
| `-3`, `--mp3`                     | convert to MP3 (and internally to MSM and MIDI)                                                                                     |
| `-s FILE`, `--soundbank FILE`     | use a specific sound bank file (.sf2, .dls) for Wave conversion                                                                     |
| `-d`, `--debug`                   | write additional debug versions of MEI and MSM                                                                                      |


The final argument should always be a path to a valid MEI file (e.g., `"C:\myMeiCollection\test.mei"`); always in quotes! This is the only mandatory argument if you want to convert something.

The third way of using meico is as a Java programming library. Its `Mei`, `Msm`, `Midi`, and `Audio` classes are the most important to work with. Class `meico.app.Main` demonstrates the use of meico (method `commandLineMode()` is best suited as tutorial). With `meicoPy.py` we have also a demo script that shows the usage of [meico in Python](https://github.com/cemfi/meico/tree/master/meicoPy). Unfortunately, we have no API documentation, yet. But the source files are extensively commented and should suffice as makeshift.

We further provide a Python3-based [REST API for meico](https://github.com/cemfi/meico/tree/master/rest).

### Build Instructions

Meico can quickly be built using [Ant](http://ant.apache.org/):
```bash
$ git clone https://github.com/cemfi/meico.git
$ cd meico
$ ant
```
The resulting `meico.jar` can be found in `out/artifacts/meico`.

**A note concerning MIDI to audio rendering:** Meico's MIDI to audio renderer relies on the package `sun.com.media.sound`. However, Java 9 and later versions do no longer provide access to this package at compile time. It is still accessible at runtime. Hence, meico should be compiled with Java 8 and can run with later versions (tested until Java 10). But at some point they will probably make this package inaccessible also at runtime. A workaround for this is using the Gervill Sound Synthesizer (search `gervill.jar` in the internet and add it to `externals`) that provides the required package, so no code changes are necessary. However, consider that Gervill is licensed under GNU GPL-2.0 while meico is under GNU LGPL-3.0!

### License information

Meico makes use of the following third party libraries:
- [XOM](http://www.xom.nu/) v1.2.11 by Elliotte Rusty Harold, GNU Lesser General Public License (LGPL) version 2.1.
- [Java-String-Similarity](https://github.com/tdebatty/java-string-similarity) v1.0.0 by Thibault Debatty, MIT license.
- [Jing](http://www.thaiopensource.com/relaxng/jing.html) v20091111 by James Clark (Thai Open Source Software Center Ltd), see `copying.txt` provided in file `jing-20091111.jar`.
- [Saxon](http://saxon.sourceforge.net/) v9.8.0.11 HE by James Clark (Thai Open Source Software Center Ltd), Mozilla Public License Meico 2.0.
- [JSON.simple](https://cliftonlabs.github.io/json-simple/) v3.0.2 by Yidong Fang, Chris Nokleberg, Dave Hughes, and Davin Loegering, Apache License 2.0.
- [Java LAME](https://github.com/nwaldispuehl/java-lame) v3.98.4 by Ken Händel and Nico Waldispühl, GNU LGPL version 3.0.
- [MEI Common Music Notation Schema](https://github.com/music-encoding/music-encoding) (`mei-CMN.rng`), Educational Community License (ECL) 2.0.
- parts of `MidiToWavRenderer.java`, an add-on to the [JFugue](http://www.jfugue.org/download.html) library, LGPL license.
- [Font Awesome](https://fontawesome.com/) v5.2.0 (the free solid icons font `fa-solid-900.ttf`), Fonticons, Inc., [SIL OFL 1.1 License](https://scripts.sil.org/OFL).

We publish meico under GNU LGPL version 3.0 Meico development is part of the ZenMEM project which is funded by the German Federal Ministry of Education and Research (funding code 01UG1414A–C).
If you use meico in your project make sure that you do not conflict with any of the above licenses.

