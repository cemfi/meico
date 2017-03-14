###Version History


####v0.2.12
- Bugfix in commandline mode.
- Reworked filename generation. New method `meico.mei.Helper.getFilenameWithoutExtension(String filename)`.
- Added Saxon v9.7.0.15 HE to the externals to process XSLT Stylesheets from the Music Encoding Initiative.
- Added further conversions. These are using the Music Encoding Initiative's XSLT stylesheets from the [MEI Encoding Tools GitHub](https://github.com/music-encoding/encoding-tools) page. However, they are a bit buggy sometimes ... and slow!

    | Conversion               | implemented in method                 | comment                                           |
    |--------------------------|---------------------------------------|---------------------------------------------------|
    | MEI to MusicXML          | `meico.mei.Mei.exportMusicXml()`      | buggy                                             |
    | MusicXML to MEI (v3.0.0) | `meico.musicxml.MusicXml.exportMei()` | not functional, yet, because of XSLT syntax error |
    | MEI to MARC              | `meico.mei.Mei.exportMarc()`          | requires more testing                             |
    | MEI to MODS              | `meico.mei.Mei.exportMods()`          | requires more testing                             |
    | MEI to MUP (v1.0.3)      | `meico.mei.Mei.exportMup()`           | requires more testing                             |

- A series of new Classes has been added accordingly: `meico.musicxml.MusicXml`, `meico.marc.Marc`, `meico.mods.Mods`, and `meico.mup.Mup`.
- Two new helper methods have been added to `meico.mei.helper`:
    - `public static Document xslTransformToDocument(Document input, Document stylesheet)` and
    - `public static String xslTransformToString(Document input, Document stylesheet)`.
- These adds are not part of the window mode and meicoPy, yet, butt will be integrated in a future update.


####v0.2.11
- Bugfix in `meico.msm.Msm.resolveSequencingMaps()`.
- Added meicoPy, a Python demo script. This demonstrates the usage of meico within Python. It is a reimplementation of meico's command line mode in Python.


####v0.2.10
- Some adds to the instruments dictionary.


####v0.2.9
- When creating MSM `goto` elements from endings, the elements get a temporal attribute `n` (derives from the respective MEI `ending` element which also has an `n` attribute). In MSM this attribute is only required for the conversion and should not be present in the final output MSM. Method `Helper.msmCleanup()` has been extended accordingly.
- If an MEI `ending` has no attribute `n`, meico now checks for attribute `label` and takes this to search for numbering information. However, in case that both attributes are present, attribute `n` is preferred.


####v0.2.8
- Added method `Msm.deleteEmptyMaps()`. It removes all empty maps, i.e. all elements with a substring `"Map"` in the local-name (`timeSignatureMap`, `keySignatureMap`, `markerMap`, `sequencingMap` etc.), from the XML tree. This is to make the MSM file a bit smaller and less cluttered.
- Added methods `getElementAtAfter()` and `getElementBeforeAt()` to class `Msm`, helper methods for navigation within maps.
- Repetition support implemented in meico, see the following lines for details.
- Added local and global `sequencingMap` elements to MSM. These hold information about the arrangement, i.e. repetitions, jumps etc. These will be encoded via elements `marker` (same as in the `markerMap` but with a UUID) and `goto` (uses the `marker` UUIDs to indicate jump target).
    - The `marker` attribute `message` describes the meaning of the marker:<br> `<marker midi.date="..." message="..." xml:id="..."/>`
        
        | `message=`           | description                                                                                                                          |
        |----------------------|--------------------------------------------------------------------------------------------------------------------------------------|
        | `"fine"`             | the end mark of the piece; it is generated from MEI `measure`attributes `left/right="end"`                                           |
        | `"repetition start"` | indicates a possible jump target; it is generated from MEI `measure`attributes `left/right="rptstart"` or `left/right="rptboth"`     |
        | `"ending ..."`       | this marker derives from an MEI `ending` element which starts at this position; the `n` or `label` of the ending are also given here |
     
    - If an MEI `measure`'s attribute `left` or `right` has the value `"rptend"` or `"rptboth"`, an MSM `goto` element is generated and added to the `sequencingMap`. The format of the `goto` element is as follows:<br> `<goto midi.date="..." activity="..." target.date="..." target.id="..."/>`
        
        | Attribute     | Description                                                                                       |
        |---------------|---------------------------------------------------------------------------------------------------|
        | `midi.date`   | the position of the jump mark on the midi ticks timeline                                          |
        | `activity`    | documents in a bit sequence when the `goto` is active and when it is inactive; e.g., `"1001"` would mean that the first time the playback reaches the `goto` it is active (do the jump), the next two times inactive (ignore it), then once more active, and from then on always inactive; for standard repetitions in music `activity="1"`; the attribute is optional; if it is missing it is assumed as `activity="1"` by default |
        | `target.date` | the midi ticks position to jump to                                                                |
        | `target.id`   | the `xml:id` of the marker to jump to (the marker's `midi.date` should be equal to `target.date`) |

    - Class `meico.msm.Msm` implements method `resolveSequencingMaps()` which the user can call to expand all other maps and the parts' scores according to the global and local sequencing information. This will delete all `sequencingMap` elements from the MSM tree as they no longer apply to the data. In case of a local `sequencingMap` a part ignores the global one. MEI-to-MSM conversion, however, generates only a global `sequencingMap`.
        - Private method `Msm.applySequencingMapToMap()` has been added.
        - Class `meico.msm.Goto` has been added to represent `goto` elements from the `sequencingMap` and make processing more convenient. Application developers cann ignore this class as it is only of relevance for meico's internal processing. 
    - MSM tool to resolve `sequencingMap` elements has been added to window mode (MSM option "Expand Repetitions") and command line mode (here, it is done automatically).
    - If elements that have an `xml:id` are repeated/copied, the id is changed to `[original id] + "_repetition"`.
- Extended processing of MEI `measure` elements in method `Mei.processMeasure()`.
    - Attribute `metcon` is now taken into account if a measure is underfull. If `metcon == "false"`, the measure can be shorter than what is defined by the time signature. An underfull measure with no `metcon` attribute or `metcon != "false"` will be filled up with rests. Overfull measures, however, will always be extended.
    - Barlines of measures are encoded in attributes `left` and `right`. If these have sequencing-related information (`rptstart`, `rptend`, `rptboth`, or `end`) the respective information in the global MSM `sequencingMap` are generated. Therefore, the new method `Helper.barline2SequencingCommand()` has been added which is called from `Mei.processMeasure()`.
- Processing of MEI `ending` elements added via method `Mei.processEnding()`.
    - If there is only one ending, playback will skip it at repetition by default.
    - Meico tries to realize the order of the endings according to the numbering in the endings' `n` attribute. This attribute should contain an integer substring (e.g., `"1"`, `"1."`, `"2nd"`, `"Play this at the 3rd time!"`, but not `"first"` or `"Play this at the third time!"`). (Sub-)String `"fine"`/`"Fine"`/`"FINE"` will also be recognized. Strings such as `"1-3"` will be recognized as `"1"`, this means that more complex instructions will not be recognized correctly due to the lack of unambiguous, binding formalization (meico is no "guessing machine"). If meico fails to find an `n` attribute or extract a meaningful numbering from it, the endings are played in order of appearance in the MEI source. Meico does not analyse attribute `label`; so the editor should always encode the numbering in attribute `n`.
    - We tried to cover a variety of repetition and ending constellations but it is virtually impossible to cover all the crude situations that MEI allows (e.g., nested repetitions, repetitions within endings). So be not disappointed if some unorthodox situation from your special music encoding project does not work as expected.


####v0.2.7
- In MEI, global (score-wise) and local (staff-wise and layer-wise) key signatures can be mixed. Rule of thumb is, the latest key signature before a `note` is the one that has to be considered. So far, meico ignored global data if there was a local entry once. This lead to some wrong results if global entries come after local (e.g., at the beginning it may be encoded in `staffDef` elements but later in `scoreDef` elements; see, for instance, `Hummel_Concerto_for_trumpet.mei` in the sample encodings). This issue is now fixed. If local and global key signature information deviate from each other meico trys to add global data to the local `keySignatureMap` in MPM where necessary. However, this is done ad hoc in method `Helper.computePitch()` in a very local context. Hence, it is not 100% perfect. This means, if the necessity for local copies occurs somewhere within the piece, past `keySignature` elements will be missing until this point.
- New method `Helper.addToMap()`. This is from now on used to insert new child elements into maps (sequential lists with elements that have an attribute `midi.date`) and ensure the timely order of the elements. All relevant methods in classes `Helper`and `Mei` have been adapted accordingly.


####v0.2.6
- Slight enhancements of `Midi.play()` and `Midi.stop()`.
- Some code polishing in classes `meico.mei.Mei` and `meico.mei.Helper`.
- Better support of MEI `layer` elements during MEI-to-MSM conversion.
    - New `Helper` methods: `getLayer()`, `getLayerId()`, and `addLayerAttribute()`.
    - Affected methods have been revised. All MEI-to-MSM conversion methods that generate `note`, `rest`, `timeSignature`, `keySignature`, `transposition`, `dur.default`, and `oct.default` elements in MSM add a temporary layer attribute that is deleted during `Helper.msmCleanup()`.
    - Method `Mei.processRepeat()` considers layers, i.e., if a repetition takes place within a `layer` environment (e.g., beatRpt), then only those elements are repeated that belong to this layer.
    - Method `Mei.processStaffDef()` has been extended. So far, its child elements were ignored. Now, they are processed.
- Bugfix in `Helper.computePitch()`. Partly (not always) wrong conversion of accidental string to numeric value has been fixed.


####v0.2.5
- Added method `Helper.midi2pname()`.
- Extended method `Helper.pname2midi()`.
- Changes to MSM accidental elements:
    - Renamed attribute `pitch` into `midi.pitch`.
    - Added attribute `pitchname` for better readability.
    - Both attributes can be used equivalently. Method `Helper.computePitch()` checks for both, preferring `midi.pitch` over `pitchname`.
- Removed files `MidiDataInputStream.java`, `MidiDataOutputStream.java`, `MidiFileReader.java`, `MidiFileWriter.java`, and `ExtendedMidiFileFormat.java` from package `meico.midi`. These were elements of the GNU Classpath project by Free Software Foundation. Since we dropped Java 6 compatibility these resources were no longer necessary. The corresponding methods `readMidi()` and `writeMidi()` have been redone.
- Added exit codes for errors in command line mode.
- Updated `README.md`.


####v0.2.4
- Added audio playback methods `play()` and `stop()` to class `meico.audio.Audio`.
- Added audio playback button to the window mode graphical user interface.
    - Ensured that all playback is exclusive, i.e. starting one playback will stop any other currently running playback both for midi and audio.
    - Ensured that the playback of a Midi or audio instance stops when the instance is deleted or overridden.
- Java 7 conform code polishing.


####v0.2.3
- Added Soundbank support for Midi-to-audio rendering. 
    - Instead of Java's default sounds the user can now freely choose a `.dls` or `.sf2` file from the file system and use this for the synthesis of audio files. 
    - A corresponding conversion option has been added to the window mode. Just right click the Midi-to-audio conversion button and click the `Choose soundbank` option, then select a corresponding file from the file system. I recommend testing [these](https://sourceforge.net/projects/androidframe/files/soundfonts/) SoundFonts.
    - A command line option has been added: `[-s "C:\mySoundfonts\mySoundfont.sf2"]` or `[--soundbank "C:\mySoundfonts\mySoundfont.sf2"]` to choose a specific `.sf2` or `.dls` file for higher quality sounds rendering.
- Deleted method `Midi2AudioRenderer.renderMidi2Audio(File soundbankFile, int[] patches, Sequence sequence)`.
- Bugfix in class `meico.midi.Midi`: the `sequencer` has been opened in the constructor but was never closed. Now it is opened when method `start()` is called and closed when method `stop()` is called. Thus, it is only open during Midi playback.
- Added Ant build script `build.xml`, thanks to Simon Waloschek.
- Added Travis CI continuous integration system `.travic.yml`, thanks to Simon Waloschek.
- Updated `README.md`.
- Added `Violoncello` to the instruments dictionary.


####v0.2.2
- Fixed Midi-to-Audio conversion. Export of wave files works now.
- Updated `README.md`.


####v0.2.1
- Fixed delay and "hiccup" in Midi playback when initializing and starting a sequencer of a `Midi` object for the first time.
- Added content to method `Audio.writeAudio(File file)` (so far it was empty).
- Added format data to class `Audio`. New getter methods have been added.
- In class `Audio` audio data are no longer represented as `AudioInputStream` but as byte array. Constructors have been adapted. Class `MeiCoApp` has also been adapted in subclass `Audio4Gui`.
- Deactivated methods `Audio.writeAudio()` until byte-array-to-AudioInputStream conversion works properly.
- Fixed issues with the playback buttons in window mode.
- Updated `README.md`.


####v0.2.0
- Added subpackage `graphics` to `resources` and moved all graphics resources into it.
- Added audio export in Wave format.
    - Added new command line options (`[-w]` or `[--wav]`) to trigger wave export in command line mode.
    - Modified `Midi.exportAudio()` (former `Midi.exportWav()`) to create and return an instance of `meico.audio.Audio`.
    - Added several methods to class `Audio`. Audio data is now represented in an `AudioInputStream` and can be written into a file.
    - Modified method `meico.midi.Midi2AudioRenderer`. Its rendering methods return an `AudioInputStream` object instead of directly writing a file into the file system.
    - Extended the window mode graphical user interface to include the new functionalities.
    - Updated `README.md`.
- Instead of using one global midi sequencer for Midi playback in class `meico.app.MeiCoApp` (window mode) I switched to the built-in local sequencers in class `meico.midi.Midi`.
- Added tooltips in window mode for better user guidance.
- Introduced some layouting variables in class `meico.app.MeiCoApp` for better editing of the window mode graphical user interface via global variables.


####v0.1.4
- Added basic audio export to midi package (`meico.midi.Midi2AudioRenderer.java`).
- Added `UnsupportedSoundbankException.java` to package `meico.midi`.
- Added test audio output to command line mode.


####v0.1.3
- Renamed the `element` elements in the MSM format. In the `timeSignatureMap` they are called `timeSignature`, in `keySignatureMap` they are called `keySignature` and in the `markerMap`they are now called `marker`.
- Method `Helper.computePitchOld()` deleted.
- Added method `Mei.processAccid()`, so `accid` elements are now processed not only as children of `note` elements (as until now) but also when they are siblings to `note` elements and the like. However, the attributes `ploc` and `oloc` are required to associate the accidental with a pitch.
- Ids generated by meico (UUIDs) did not validate. This issue is fixed. Generated ids start with `"meico_"`.
- Added support for multiple `body` elements in an MEI `music` element. This is no valid MEI, though, but meico won't crash if it occurs anyway.
- Added processing of `incip`, `parts`, and `part` elements.
- Little bugfix with `halfmRpt`.
- Added method `Helper.pname2midi()` as this functionality is used at several occasions.
- Key signature processing (`scoreDef`, `staffDef`, `keySig`) in method `Mei.makeKeySignature()` has been redone. 
    - More cases and unorthodox/mixed key signatures are now covered, also in the MSM representation. 
    - Nonetheless, `keyAccid` elements require the `pname` attribute so that meico can associate the accidental with a pitch class.
    - If not indicated by `keyAccid` elements, `scoreDef`, `staffDef`, and `keySig` require at least the `key.sig` or `sig` attribute to denote a key signature. 
    - Attribute `key.sig.mixed` or `sig.mixed` are also supported.
    - The octave position of the key signature accidentals is ignored. It is not clearly defined if an accidental affects only this one octave or all octaves of that pitch class. In meico we decided to interpret it in the latter way.
    - Modified method `Helper.computePitch()` to consider key signature accidentals.
- The processing of `chord` elements (`bTrem` and `fTrem` are processed similarly) has been redone. If a `chord` element has no duration data in its attributes (`dur`, `dots`) and does not inherit it from a parent element, its duration is now specified by the longest child element.
- Added `Prinzipal`, `Soprano`, `Baritone`, `Euphonium`, `Chant` to the instruments dictionary.
- Bugfix in `Mei.reorderElements()`, relevant in the case that a `startid` attribute refers to an element within the element.
- Minor corrections in `MeiCoApp.commandLineMode()`.


####v0.1.2
- Bugfix in command line mode: missing path when writing `"-debug.mei"` and the file has been loaded via relative path.
- Added `S.`, `A.`, `T.`, `B.` to the instrument dictionary for ChoirOhs.
- Method `InstrumentsDictionary.getProgramChange()` outputs its string matching results to the command line or log file, resp.
- Missing `accid.ges` attribute processing in `Helper.computePitch()` added.


####v0.1.1
- Renamed the `dur` attribute in MSM notes and rests into `midi.duration`.
- Further renamings: `date` into `midi.date`, `pitch` into `midi.pitch`, `channel.midi` into `midi.channel`, and `port.midi` into `midi.port`.
- Added `Bassus`, `Cantus`, `Singstimme`, `Singstimmen`, `Pianoforte`, `Trumpet in`, `Trompete in` to the instruments dictionary.
- Added a flag to the window mode constructor method `MeiCoApp(String title, boolean makeLogFile)`. The redirection of console output into a log file is done only when `makeLogFile` is set true.
- Bugfixing in `Mei.processStaff()` and `Helper.getPart()`.
- `tie` elements are now processed (new method `Mei.resolveTieElements()`); they are resolved into `tie` attributes of `note` elements during the preprocessing. Hence, meico now supports the use of `tie`elements and is no longer restricted to `tie` attributes only. However, users should not mix `tie` and `slur` elements; the latter are not and will not be processed as ties!
- Method `Mei.resolveCopyOfs()` rewritten. It is not only faster now. It might happen (and does happen in the MEI sample library) that a placeholder element (the one with the `copyof` attribute) copies elements that again contain placeholders; it requires multiple runs to resolve this. The new implementation can handle circular referencing (cannot be resolved and would otherwise lead to infinite loops). Furthermore, if the placeholder element has an `xml:id` this id is no longer overwritten by the newly generated ids.
- Method `Mei.reorderElements()` (part of the MEI preprocessing) has been rewritter and is much faster now.


####v0.1.0 (beta release)<br>
- Moved unused code in the `meico.midi` package into the `legacy` sub-package.
- Added validation of MEI files against `mei-CMN.rng` version 3.0.0 (August 2016).
    - The file `mei-CMN.rng` from [MEI GitHub repository](https://github.com/music-encoding/music-encoding/blob/develop/schemata/mei-CMN.rng) was added to the resources. 
    - The `Jing` library (version 20091111) was added to the externals (notice the `copyright.txt` in file `jing-20091111.jar`).
    - The method `validateAgainstSchema()` is implemented in class `Helper` and called by `validate()` through `readMeiFile()` in class `Mei`. 
    - New addition to command line mode: `[-v]` or `[--validate]`: to activate validation of mei files loaded.
    - In window mode files are read without validation by default. Right click on the MEI file loaded to trigger validation and get a popup message on the success. 
    - Applications may call method `isValid()` in class `Mei` after the file was read to check validity. If the file has been loaded without validation it remains `false` by default. To do the validation afterwards, call `validate()`.
- In window mode all command line outputs (`System.out` and `System.err`) are now redirected into a log file `meico.log`.
- Added `Bratsche` to the instruments dictionary.
- Replaced the `:` in the id generation for copyOf resolution into `_`.


####v0.0.7<br>
- Added flag to the mei-to-msm conversion to avoid the midi drum channel, added it to the window mode and command line options
	- `[-c]` or `[--dont-use-channel-10]`: the flag says whether channel 10 (midi drum channel) shall be used or not; it is already done at mei-to-msm convertion, because the msm should align with the midi file later on
- Changed id generation for copyOf resolution into a combined id: `source id + ":" + generated id`; hence, applications can now trace them back to the source element
- Minor bugfix of command line option `[--no--program-changes]` to `[--no-program-changes]`
- Minor ui corrections for window mode
- Adding new attributes, `date.midi` and `dur.midi`, to mei note and rest elements during conversion. This is only for debugging purpose and appears only in the `-debug.mei` file when running the command line mode with `--debug` flag.
- Also a `pnum` is added to the mei `note` elements in the debug version which holds the calculated midi pitch value.


####v0.0.6<br>
- Added `Canto`, `Quinto` and `Tenore` to the `VoiceOhs` in the instruments dictionary.
- In `MeiCoApp`'s `commandLineMode()` a relative path can be used; the absolute path is derived automatically. Hence, users do not have to write down whole paths in the command line from now on.
- Fixed bug in the processing of note accidentals when `accid` elements are used instead of `accid` attributes.
- Fixed bug in `Helper.getNextSiblingElement(Element ofThis)`.
- Method `exportMidi()` in `Msm.java` now has a flag to suppress the generation of program change events (useful in some applications). The flag is also supported in command line mode (add `[--no-program-changes]` to your call) and window mode (right click on the msm to midi conversion button).
- Added an optional tempo parameter to the command line mode `[-t argument]` or `[--tempo argument]`.
- Changed the format of the command line parameters to follow POSIX standard.
	- `[-?]` or `[--help]`: for this command line help text. If you use this, any other arguments are skipped.
	- `[-a]` or `[--add-ids]`: to add xml:ids to note, rest and chord elements in mei, as far as they do not have an id; meico will output a revised mei file
	- `[-r]` or `[--resolve-copy-ofs]`: mei elements with a `copyOf` attribute are resolved into selfcontained elements with an own `xml:id`; meico will output a revised mei file
	- `[-m]` or `[--msm]`: converts mei to msm; meico will write an msm file to the path of the mei
	- `[-i]` or `[--midi]`: converts mei to msm to midi; meico will output a midi file to the path of the mei
	- `[-p]` or `[--no-program-changes]`: call this to suppress the generation of program change events in midi
	- `[-t argument]` or `[--tempo argument]`: this sets the tempo of the midi file; the argument must be a floating point number; if this is not used the tempo is always 120 bpm
	- `[-d]` or `[--debug]`: to write debug versions of mei and msm
	- Path tho the mei file (e.g., `"D:\Arbeit\Software\Java\MEI Converter\test files\Hummel_Concerto_for_trumpet.mei"`), this should always be the last parameter -  always in quotes!

####v0.0.5<br>
Bugs fixed in `Mei.java` that were introduced by rewriting `convert()` for Java 1.6 compatibility.

####v0.0.4<br>
Java 1.6+ compatibility<br>
instrument dictionary extended (`church organ`: `upper`, `lower`, `pedal`)

####v0.0.3<br>
So far, msm part names were the `staffDef` labels. Now, they are created from an eventually existing parent `staffGrp` label and the `staffDef` label (`"staffGrp label" + " " + "staffDef label"`).

####v0.0.2<br>
Java 7+ compatibility

####v0.0.1<br>
first release (requires Java 8+)
