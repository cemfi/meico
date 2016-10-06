# meico: MEI Converter
[![GitHub release](https://img.shields.io/github/release/cemfi/meico.svg)](https://github.com/cemfi/meico/releases/latest) [![LGPL v3](https://img.shields.io/github/license/cemfi/meico.svg)](https://github.com/cemfi/meico/blob/master/LICENSE) [![Java compatibility 1.7+](https://img.shields.io/badge/java-1.7%2B-blue.svg)](http://java.com)

Author: [Axel Berndt](https://github.com/axelberndt)<br>
MEI support: [Benjamin W. Bohl](https://github.com/bwbohl)<br>
Contributor: [Simon Waloschek](https://github.com/sonovice)<br>
[Center of Music and Film Informatics](http://www.cemfi.de/), Detmold

Meico is a converter framework for MEI files. Even though MEI is a quasi-standard for digital music editions, there is few software support for it. If you want to listen to the music in your MEI file, you need a MIDI or audio export. If you want to process the musical data (e.g., for Music Information Retrieval), there are many better suited formats and representations than MEI. With meico we address these issues. Meico implements methods to convert MEI data into the MSM (Musical Sequence Markup) format, an intermediate format that we defined for further use in other projects. From MSM, the MIDI export and audio rendering are quite straight forward. Currently, meico is a beta release. The following features are implemented:

- MEI to MSM conversion (with variable time resolution in pulses per quarter, ppq)
- MSM to MIDI conversion
- MIDI to audio conversion (with freely choosable SoundFont and Downloadable Sounds)
- MEI processing functions (validation, xml:id generation, resolution of elements with copyof attribute)
- MSM processing functions (remove rest elements from the score)
- an instrument dictionary that uses several string matching algorithms to map staff names to MIDI program change numbers
- basic MIDI and audio playback
- two standalone modes (command line mode, window mode).

There are several features open, though. Currently, meico ignores any MEI data that is concerned with expressive performance (tempo, dynamics, articulation, ornamentation). Repetitions are not resolved. Several MEI elements and attributes are not supported so far (e.g. meterSigGrp, uneume, lyrics). The MEI file must be unambiguous, i.e., it should not contain any variants (app, choice etc.). A tool to resolve ambiguity is under construction and will soon be published. We are also developing a schematron rule set to give detailed feedback on the supported and unsupported MEI elements when an MEI file is loaded into meico.

###How to use meico?

Meico can be used in several different ways. The jar file (see the GitHub release page) is a standalone runnable Java program. We have tested it under Windows, Mac OS and Linux. The only prerequisite is that you have a Java 1.7 (or higher) Runtime Environment installed on your computer. 

Starting the standalone jar without any command line options will start the windowed gui mode of meico. Simply drag your MEI, MSM, MIDI, and Wave files into the window. You can find context information on each interface element in the tooltips and statusbar. There are several additional functions accessible via right click. If you have several mdivs in your MEI document you will get an individual MSM instance for each movement. Conversion from MIDI to audio may take some time when it is a long piece. We have not built in a progress display yet. Just be patient until the conversion button changes its color back and the audio data appears. To get better quality sounds than Java's built-in default instruments (those used for the Midi playback function), we recommend downloading one of [these soundfonts](https://sourceforge.net/projects/androidframe/files/soundfonts/) and use it via right clicking the Midi-to-audio conversion button, option "Choose soundbank". 

![A screenshot of the meico graphical user interface.](https://raw.githubusercontent.com/cemfi/meico/master/figures/meico-screenshot.png)

The command line mode expects the following command line options:
usage: `java -jar meico.jar [OPTIONS] FILE`

- `-?`, `--help`: show this help text
- `-v`, `--validate`: validate loaded MEI file
- `-a`, `--add-ids`: add missing `xml:id`s to note, rest and chord elements in MEI; meico will output a revised MEI file
- `-r`, `--resolve-copy-ofs`: resolve elements with `copyof` attributes into selfcontained elements with unique `xml:id`; meico will output a revised MEI file
- `-m`, `--msm`: convert to MSM
- `-i`, `--midi`: convert to MIDI (and internally to MSM)
- `-p`, `--no-program-changes`: suppress program change events in MIDI
- `-c`, `--dont-use-channel-10`: do not use channel 10 (drum channel) in MIDI
- `-t argument`, `--tempo argument`: set MIDI tempo (bpm), default is 120 bpm
- `-w`, `--wav`: convert to Wave (and internally to MSM and MIDI)
- `-s argument`, `--soundbank argument` use a specific sound bank file (.sf2, .dls) for Wave conversion
- `-d`, `--debug`: write additional debug versions of MEI and MSM
- The final argument should always be a path to a valid MEI file (e.g., `"C:\myMeiCollection\test.mei"`); always in quotes! This is the only mandatory argument if you want to convert something.

The third way of using meico is as a Java programming library. Its `Mei`, `Msm`, `Midi`, and `Audio` classes are the most important to work with. Class `meico.app.MeiCoApp` demonstrates the use of meico (method `commandLineMode()` is best suited as tutorial). Unfortunately, we have no API documentation, yet. But the source files are extensively commented and should suffice as makeshift. Meico can quickly be built using Ant, just go to your meico directory and enter `ant`.

###License information

Meico makes use of the following third party libraries:
- XOM v1.2.10 by Elliotte Rusty Harold, GNU Lesser General Public License (LGPL) version 2.1.
- Java-String-Similarity v0.13 by Thibault Debatty, MIT license.
- MigLayout v4.0 by Mikael Grev (MiG InfoCom AB), BSD and GPL license.
- the FileDrop class v1.1.1 by Robert Harder, Nathan Blomquist and Joshua Gerth, Public Domain release.
- Jing v20091111 by James Clark (Thai Open Source Software Center Ltd), see `copying.txt` provided in file `jing-20091111.jar`.
- MEI Common Music Notation Schema (`mei-CMN.rng`), Educational Community License (ECL) 2.0.
- parts of `MidiToWavRenderer.java`, an add-on to the JFugue library, LGPL license.

We publish meico under GNU LGPL version 3. Meico development is part of the ZenMEM project which is funded by the German Federal Ministry of Education and Research (funding code 01UG1414A–C).
If you use meico in your project make sure that you do not conflict with any of the above licenses.

