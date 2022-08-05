![](https://github.com/cemfi/meico/blob/master/figures/meico_title_small.png)
## Application Package

[![GitHub release](https://img.shields.io/github/release/cemfi/meico.svg)](https://github.com/cemfi/meico/releases/latest) 
![Java compatibility](https://img.shields.io/badge/Java-1.8--10-blue)
[![documentation](https://img.shields.io/badge/doc-JavaDoc-green.svg)](https://github.com/cemfi/meico/blob/meicoApp/docs)
[![LGPL v3](https://img.shields.io/github/license/cemfi/meico.svg)](https://github.com/cemfi/meico/blob/meicoApp/LICENSE) 

Author: [Axel Berndt](https://github.com/axelberndt)<br>
[Center of Music and Film Informatics](http://www.cemfi.de/), Detmold

This is the meico application package. From meico version 0.8.0 on it is no longer part of the base library but is delivered separately in this GitHub branch and as a self-contained release asset `meicoApp.jar`.

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

With `meicoPy.py` we have also a demo script that shows the usage of [meico in Python](https://github.com/cemfi/meico/blob/meicoApp/meicoPy). We further provide a Python3-based [REST API for meico](https://github.com/cemfi/meico/tree/meicoApp/rest). However, both have not been updated for a while!

### License information

The meico application package makes use of the following third party libraries in addition to those that come with the [meico base library](https://github.com/cemfi/meico/tree/master):
- [Font Awesome](https://fontawesome.com/) v5.2.0 (the free solid icons font `fa-solid-900.ttf`), Fonticons, Inc., [SIL OFL 1.1 License](https://scripts.sil.org/OFL).
- [Verovio JavaScript Toolkit](https://www.verovio.org/index.xhtml) v3.11.0-e2d6db8 by Etienne Darbellay, Jean-François Marti, Laurent Pugin, Rodolfo Zitellini and others, GNU Lesser General Public License (LGPL) 3.0.
- [JavaFX](https://gluonhq.com/products/javafx/) v11.0.2, GPL 2.0.

We publish the meico application package under GNU GPL version 3.0. Meico's development was part of the ZenMEM project funded by the German Federal Ministry of Education and Research (2015-2019, funding code 01UG1414A–C). All MPM-related parts of meico were part of an R&D project that was funded by the [Fritz Thyssen Foundation](https://www.fritz-thyssen-stiftung.de/en/) (2019-2022). If you integrate meico or parts of it with your project make sure that you do not conflict with any of the above licenses.
