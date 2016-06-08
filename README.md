# meico: MEI Converter

Author: Axel Berndt<br>
MEI support: Benjamin W. Bohl<br>
Center of Music and Film Informatics, Detmold

Meico is a converter framework for MEI files. Even though MEI is a quasi-standard for digital music editions, there is few software support for it. If you want to use your MEI data in a music notation program, you need to convert it to MusicXML. If you want to listen to the music in your MEI file, you need a MIDI export. With meico we address these issues. Meico implements methods to convert MEI data into the MSM (Musical Sequence Markup) format, an intermediate format that we defined for further use in other projects. From MSM, the MIDI export is quite straight forward. Currently, meico is an alpha release. The following features are implemented:

- MEI to MSM conversion (with variable time resolution in pulses per quarter, ppq)
- MSM to MIDI conversion
- MEI processing functions (xml:id generation, resolution of elements with copyof attribute)
- MSM processing functions (remove rest elements from the score)
- an instrument dictionary that uses several string matching algorithms to map staff names to MIDI program change numbers
- basic MIDI playback
- two standalone modes (command line mode, gui mode).

There are several features open, though. Currently, meico ignores any MEI data that is concerned with expressive performance (tempo, dynamics, articulation, ornamentation). Repetitions are not resolved. Several MEI elements and attributes are not supported so far (e.g. meterSigGrp, part, uneume). The MEI file must be unambiguous, i.e., it should not contain variants (app, choice etc.). A tool to resolve ambiguity is under construction and will soon be published. We are also developing a schematron rule set to give detailed feedback on the supported and unsupported MEI elements when an MEI file is loaded into meico.

###How to use meico?

Meico can be used in several different ways. The jar file is a standalone runnable Java program. We have tested it under Windows, Mac OS and Linux. The only prerequisite is that you have a Java 1.6 (or higher) Runtime Environment installed on your computer. 

Starting the standalone jar without any command line options will start the windowed gui mode of meico. Simply drag your MEI, MSM and MIDI files into the window. You can find context information on each interface element in the statusbar. There are several additional functions accessible via right click. Conversion from MEI to MSM may take some time when the MEI source is very large. We have not built in a progress display, so far. Just be patient until the conversion button changes its color back and the MSM data appears. If you have several mdivs in your MEI document you will get an individual MSM instance for each movement.

The command line mode expects the following command line options:
- `[?]` or `[help]` for command line help text. If you use this, any other arguments are skipped.
- `[addIds]` to add xml:ids to note, rest and chord elements in MEI, as far as they do not have an xml:id; meico will output a revised MEI file
- `[resolveCopyOfs]` MEI elements with a copyOf attribute are resolved into self-contained elements with an own xml:id; meico will output a revised MEI file
- `[msm]` converts MEI to MSM; meico will write an MSM file to the path of the MEI file
- `[midi]` converts MEI (to MSM, internally) to MIDI; meico will output a MIDI file to the path of the MEI file
- `[debug]` to write debug versions of the MEI and MSM files to the path of the MEI file
- The final argument should always be a path to a valid MEI file (e.g., `C:\myMeiCollection\test.mei`); always in quotes! This is the only mandatory argument if you want to convert something.

The third way of using meico is as a Java programming library. Its Mei, Msm and Midi class are the most important to work with. The MeiCoApp.java in package meico.app demonstrates the use of meico (the method commandLineMode() is best suited as tutorial). Unfortunately, we have no API documentation, yet. But the source files are extensively commented and should suffice as makeshift.

###License information

Meico makes use of the following third party libraries:
- XOM v1.2.10 by Elliotte Rusty Harold, GNU Lesser General Public License (LGPL) version 2.1
- Java-String-Similarity v0.13 by Thibault Debatty, MIT license
- MigLayout v4.0 by Mikael Grev (MiG InfoCom AB), BSD and GPL license
- parts of GNU Classpath by Free Software Foundation, Inc., GNU GPL license
- the FileDrop class v1.1.1 by Robert Harder, Nathan Blomquist and Joshua Gerth, Public Domain release.

We publish meico under GNU LGPL version 3. The meico development is part of the ZenMEM project which is funded by the German Federal Ministry of Education and Research (funding code 01UG1414A–C).
If you use meico in your project make sure that you do not conflict with any of the above licenses.

