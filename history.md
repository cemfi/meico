###Version History

####v0.1.1
- Renamed the `dur` attribute in MSM notes and rests into `duration`.
- Added `Bassus`, `Cantus`, `Singstimme`, `Singstimmen`, `Pianoforte`, `Trumpet in`, `Trompete in` to the instruments dictionary.
- Added a flag to the window mode constructor method `MeiCoApp(String title, boolean makeLogFile)`. The redirection of console output into a log file is done only when `makeLogFile` is set true.
- Bugfixing in `Mei.processStaff()` and `Helper.getPart()`.
- `tie` elements are now processed (new method `Mei.resolveTieElements()`); they are resolved into `tie` attributes of `note` elements during the preprocessing. Hence, meico now supports the use of `tie`elements and is no longer restricted to `tie` attributes. However, users should not mix `tie` and `slur` elements; the latter are not and will not be processed as ties.
- Method `Mei.resolveCopyOfs()` rewritten. It is not only faster now. It might happen that a placeholder element (the one with the `copyof` attribute) copies elements that again contain placeholders; it requires multiple runs to resolve this. The new implementation can handle circular referencing (cannot be resolved and would otherwise lead to infinite loops). Furthermore, if the placeholder element has an `xml:id` this id is no longer overwritten by the newly generated ids.
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
- Minor bugfix of commandline option `[--no--program-changes]` to `[--no-program-changes]`
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
