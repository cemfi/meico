### Version History


#### v0.9.3
- Added new methods `getAllMdivs()` to class `meico.mei.Mei` to retrieve all `mdiv` elements in the MEI's `music` environment. However, in case of nested `mdiv` elements, only the leaf ones are returned. I.e., the returned list will only contain `mdiv` elements that do not contain other `mdiv` elements.
- Added new methods `getAllVariantEncodings()` to class `meico.mei.Mei` to retrieve all `app` and `choice` elements from the MEI.

#### v0.9.2
- Expansion of search for the title in method `meico.mei.Mei.getTitle()`, so it supports MEI 3.0 (`workDesc`) and MEI 4.0+ (`workList`).


#### v0.9.1
- Some minor preparations in class `meico.mei.Mei2MusicXmlConverter` for MEI to MusicXML conversion.

#### v0.9.0
- Added basic MusicXML integration to meico and meicoApp. Both MusicXML file formats, raw (`.musicxml`, `.xml`) and compressed (`.mxl`) are supported for reading and writing.
- While the ProxyMusic framework does not support marshalling and demarshalling of MusicXML `score-timewise`, class `meico.musicxml.MusicXml` supports conversion of `score-timewise` to `score-partwise` and vice versa.
- Integration of MusicXML basic functionality in meicoApp.
- New class `meico.mei.Mei2MsmMpmConverter` has been added to implement a better modularization of different conversion options. All conversion functionality from MEI to MSM and MPM moved from `meico.mei.MEI` and `meico.mei.Helper` into this class. Class `meico.mei.Helper` still holds all static methods, as these are useful also outside of this particular conversion.
- Slight changes in methods `convert()` and `makeMovement()` of class `meico.mei.Mei2MsmMpmConverter`. The `relatedResources` entries in the MPM are now only with filenames and no longer with the absloute path on the local machine.
- Added MusicXML Coverage Documentation.
- XOM update to v1.3.8.
- Enhancement in `meico.xml.XmlBase.XmlBase(String xml)` constructor. The added exceptions may require some applications to extend their exception handling also for other classes that are based on `XmlBase`.
- Added new class `meico.supplementary.InputStream2StringConverter`.
- Verovio update to v3.15.0-5abc7c0 and fix (due to broken backward compatibility).

#### v0.8.48
- Added new methods `meico.midi.Midi.getMinimalPPQ()` that computes the minimal integer timing resolution (in pulses per quarter note) necessary for an accurate representation of a MIDI sequence.
- The eponymous method in class `meico.msm.Msm` has been updated with the same algorithm.
- Another addition is method `meico.midi.Midi.convertPPQ()` that allows to convert the timing basis of a MIDI sequence, similar to the eponymous method in class `meico.msm.Msm`.


#### v0.8.47
- Potential bus fix in method `meico.mpm.elements.maps.TempoMap.getTempoAt()` that ensures that the exponent attribute of a `TempoData` object is present.
- New methods were added to class `meico.mpm.elements.maps.TempoMap` that reduces (monotonous) series of successive tempo instructions to one instruction.
- MeicoApp now imports MIDI files with extension `.mid` (as before) and `.midi` (this is new).


#### v0.8.46
- Private method `meico.mpm.elements.maps.TempoMap.renderTempoToMap(double date, int ppq, TempoData tempoData)` has been made `public` and refactored to `computeDiffTiming()` to better describe its function.
- Class `meico.mpm.elements.maps.GenericMap` has new methods:
  - `contains()` to check whether a given XML element is an entry in this map.
  - `getElementIndexOf()` to determine the index of a given element.


#### v0.8.45
- Bugfix in method `meico.mpm.elements.Performance.getAllMsmPartsAffectedByGlobalMap()` which caused some global performance features not being applied correctly to all affected maps.


#### v0.8.44
- Bugfix in `meico.mpm.elements.maps.OrnamentationMap` methods `renderAllNonmillisecondsModifiersToMap()` and `renderMillisecondsModifiersToMap()`. Temporary attributes from the `temporalSpread` modifier were processed incorrectly.


#### v0.8.43
- Bugfix in class `meico.mpm.elements.styles.defs.OrnamentDef`, the attribute `time.units` of element `temporalSpread` is renamed to `time.unit` (without "s") in accordance to the MPM shema definition.


#### v0.8.42
- Added all necessary functionality to handle IDs in MPM elements `temporalSpread` and `dynamicsGradient`.


#### v0.8.41
- Added missing namespace to the creation of a element `relatedResources` in constructor `meico.elements.metadata.Metadata.Metadata(Author author, Comment comment, Collection<RelatedResource> relatedResources)`.
- JavaDoc update.


#### v0.8.40
- Update of MPM ornamentation-related code in correspondence with MPM version 2.0.3.


#### v0.8.39
- The following methods have been made `public`, so applications can request corresponding data from the classes.
  - `meico.mpm.elements.maps.ArticulationMap.getArticulationDataOf()`,
  - `meico.mpm.elements.maps.DynamicsMap.getDynamicsDataOf()`,
  - `meico.mpm.elements.maps.ImprecissionMap.getDistributionDataOf()`,
  - `meico.mpm.elements.maps.MetricalAccentuationMap.getMetricalAccentuationDataOf()`,
  - `meico.mpm.elements.maps.OrnamentationMap.getOrnamentationDataOf()`,
  - `meico.mpm.elements.maps.RubatoMap.getRubatoDataOf()`, and
  - `meico.mpm.elements.maps.TempoMap.getTempoDataOf()`.
- Update of meicoApp's internal Verovio to v3.11.0-e2d6db8.


#### v0.8.38
- Classes `meico.mpm.elements.styles.defs.OrnamentDef.DynamicsGradient` and `meico.mpm.elements.styles.defs.OrnamentDef.TemporalSpread` have been extended with references to their XML representations.
- JavaFX externals have been removed from meicoApp as they do not help with Java versions 11+.


#### v0.8.37
- New method `meico.mpm.Mpm.isInNamespace()`. to check whether a given element name is in the MPM namespace. One of the constructors of `meico.mpm.elements.map.GenericMap` uses this to apply the MPM namespace more properly - it can also be used to create maps that are not in MPM namespace, such as `score` or `channelVolumeMap` in MSM.
- Methods `meico.mpm.elements.maps.AsynchronyMap.renderAsynchronyToMap()` and `meico.mpm.elements.maps.ImprecisionMap.addOffsetsToAttributes()` have been enhanced, so they do no longer produce negative timings, i.e. shift the timing of events to negative milliseconds dates.

#### v0.8.36
- Bugfix in `meico.mpm.elements.Dated.addMap()`.


#### v0.8.35
- Export of MEI to MSM and MPM will now keep the `staffDef`'s ID as the ID of the MSM and MPM `part`.
- Two addition to the instruments dictionary: `Piano Left Hand` and `Piano Right Hand`.


#### v0.8.34
- Added support for attribute `xml:id` on MPM maps in class `meico.mpm.elements.maps.GenericMap`.
- `meico.mpm.elements.maps.GenericMap` constructor has now full support for MSM `score`.
- Fixed data integrity of MPM maps that get processed by rubato transformation during performance rendering.
- Optimization of the performance rendering process.
- New method `meico.msm.Msm.getPart()` to retrieve a specific `part` element.
- Ornamentation support added to MPM API.
  - New classes are `meico.mpm.elements.styles.OrnamentationStyle`, `meico.mpm.elements.styles.defs.OrnamentDef`, `meico.mpm.elements.styles.defs.OrnamentDef.DynamicsGradient`, `meico.mpm.elements.styles.defs.OrnamentDef.TemporalSpread`, `meico.mpm.elements.maps.OrnamentationMap`, and `meico.mpm.elements.maps.data.OrnamentData`.
  - Added MEI export for element `arpeg` to MPM ornamentation. New method `meico.mei.Mei.processArpeg()` and additions to `meico.mei.Mei.makeMovement()` and class `meico.mei.Helper`.
  - Ornamentation has been added to the performance rendering in `meico.mpm.elements.Performance.perform()`.
- Enhancement of method `meico.mei.Helper.computeControlEventTiming()`; in search for the timing of an MEI control event, it will now also look at the first reference in attribute `plist`.
- Deleted package `meico.midi.legacy`.

#### v0.8.33
- New method `meico.midi.Midi.exportAudio(Soundbank soundbank)` to convert MIDI to audio with a soundfont that is already loaded, so its corresponding `File` is no longer required.


#### v0.8.32
- Enhancement/reimplementation of method `meico.mpm.elements.maps.ArticulationMap.renderArticulationToMap_millisecondModifiers()` to allow more extreme delays and duration changes.


#### v0.8.31
- Reimplementation of method `meico.mpm.elements.maps.AsynchronyMap.renderAsynchronyToMap()`. The previous version did handle some note offsets incorrectly. This is fixed in the new version.


#### v0.8.30
- New method `meico.midi.Midi.addOffset()` to add a timing offset (in ticks) to all events in a MIDI sequence.
- In method `meico.audio.Audio.convertWaveform2Image()` a dark gray center line is added to the image rendering.
- Added another variant of method `meico.audio.Audio.convertWaveform2Image()` that uses an externally instantiated audio frame dispatcher `pump`, so the application can also cancel its processing.
- Generalized method `meico.audio.Audio.convertDoubleArray2ByteArray()` to allow more than just 16 bit sample size.
- Another generalization of method `meico.audio.Audio.convertByteArray2DoubleArray()` that should now also accept audio formats other than mono and stereo.


#### v0.8.29
- Bugfix in method `meico.audio.Audio.convertWaveform2Image()` that caused an `IndexOutOfBoundsException` when the amplitude of the signal is at maximum.


#### v0.8.28
- Minor fix in method `meico.supplementary.RandomNumberProvider.getValue(int index)` to ensure that only non-negative index values are processed.


#### v0.8.27
- A little optimization in method `meico.audio.Audio.convertSpectrogramToImage()`. In addition, this method has been extended with a new argument `normalize` to give applications the possibility to decide whether the spectrogram values should or should not be normalized for rendering.
- Internal Verovio update to v3.7.0-dev-e93a13d.


#### v0.8.26
- Minor fix in method `meico.audio.Audio.convertByteArray2DoubleArray()` so that it no longer prints an error message when there is no error.
- New methods `meico.audio.Audio.exportWaveformImage()` and `meico.audio.Audio.convertWaveform2Image()` that renders the audio data to a `BufferedImage` instance. The functionality was also added to the meicoApp GUI. However, the waveform image exported here is rather low-res by default. The methods allow larger pixel resolutions; applications can specify the image dimensions and the slice of audio to be rendered freely.


#### v0.8.25
- New supplementary class `meico.supplementary.ColorCoding` that simplifies the mapping of one-dimensional values to colors. Meico uses these for spectrogram visualizations.
- Advancements in method `meico.audio.Audio.convertSpectrogramToImage()`. It is now possible to create spectrogram images with one of several color codings.
- Updated Ant build script.
- Updated internal Verovio distribution to v3.5.0-dev-7cfde60.


#### v0.8.24
- Added package information to the documentation.
- Added Constant Q Transform spectrogram export to class `meico.audio.Audio`. The method is `exportConstantQTransformSpectrogram()`.
    - Method `convertSpectrogramToImage()` makes it convenient to convert the spectrogram data to pixel data.
    - A new external has been added to the meico ecosystem. [Jipes](https://www.tagtraum.com/jipes/) is an open source signal processing library by Hendrik Schreiber. It can be used to build any further audio analysis methods following the same pattern as the CQT export.
    - MeicoApp has been extended accordingly with a new export option in the commandline application and in the graphical application.


#### v0.8.23
- Fixed constructor `meico.midi.MidiPlayer.MidiPlayer(Midi midi)`. Now it assigns the MIDI data to its sequencer.
- Added new method `meico.midi.MidiPlayer.setMidiOutputPort()` which allows setting an output port different from the internal Gervill synthesizer. 
    - The internal `Synthesizer` instance is kept alife, so that any preloaded soundfonts are immediately available when switching back.
    - To switch back to Gervill is important not to provide a new instance but to use the original one, like this `midiPlayer.setMidiOutputPort(midiPlayer.getSynthesizer())`. A shortcut is this `midiPlayer.setMidiOutputPort(null)` that will automatically choose the internal synthesizer as default.
- Verovio update in meicoApp.


#### v0.8.22
- Bugfix in methods `meico.mpm.elements.maps.DynamicsMap.getDynamicsDataOf()` and `meico.mpm.elements.maps.RubatoMap.getRubatoDataOf()`.


#### v0.8.21
- Little enhancement in methods `meico.mpm.elements.maps.ArticulationMap.addStyleSwitch()` that is now also able to handle argument `defaultArticulation == null`.
- Added constructors to classes `meico.mpm.elements.maps.data.ArticulationData`, `MetricalAccentuationData`, `RubatoData`, `DynamicsData`, `TempoData`, and `DistributionData` so they can now be instantiated from a corresponding XML element.
- Fix in method `meico.mpm.elements.maps.MetricalAccentuationMap.addAccentuationPattern(MetricalAccentuationData data)`. It has rejected input data that defined an `accentuationPatternDef` name but not the corresponding object itself. However, this is the usual situation when parsing an XML element. So this had to be fixed.
    - Same for method `meico.mpm.elements.maps.RubatoMap.addRubato(RubatoData data)`.
- Bugfix: corrected constant value `meico.mpm.Mpm.RUBATOR_STYLE = "RubatorStyles"` to `"RubatoStyles"`.
- Introduced some constants to class `meico.mpm.elements.maps.data.DistributionData` that provide the default strings for the type of the distribution. These are now used in class `meico.mpm.elements.maps.ImprecisionMap` instead of the hard-coded strings, so it will be easier to alter and extend those constants in future updates.


#### v0.8.20
- In classes `meico.mpm.elements.maps.GenericMap` and `meico.mpm.elements.maps.ArticulationMap` a variant of method `addStyleSwitch()` has been added that supports input of an ID string.


#### v0.8.19
- Added new class `meico.mpm.elements.metadata.Comment` and incorporated it in all classes that handle MPM metadata comment elements.
- In class `meico.mpm.elements.metadata.Metadata` some new methods have been added: `removeAuthor()` and `removeComment()`.
- Enhancement in method `meico.mpm.Mpm.addPerformance()` to cancel if the input `Performance` object is null. It also got a `boolean` return value to indicate the success of the action.
- The same has been added to method `meico.mpm.elements.Performance.addPart()`. It returns `false` also if the part is already in the performance.
- The same has been added to methods `meico.mpm.elements.metadata.Metadata.addAuthor()`, `addComment()` and `addRelatedResource()`. Instead of an index they return `-1`.
- Bugfixes in methods `meico.mpm.elements.metadata.Author.setId()` and `removeAuthor()`.
- Classes `meico.mpm.elements.Part`, `Performance` have been extended to provide access also to the element's `xml:id`.
- New methods `clear()` and `renameStyleDef()` in class `meico.mpm.elements.Header` to remove all content of from the respective MPM element.
- New method `clear()` in class `meico.mpm.elements.Dated` to remove all content of from the respective MPM element.
- Bugfix in method `meico.mpm.elements.Header.removeStyleType()`, `removeStyleDef()`, `addStyleType()`.
- Lots of enhancement and refactoring of all classes in packages `meico.mpm.elements.styles` and `meico.mpm.elements.styles.defs`.
- Correction of `rubatoDef` factory method `meico.mpm.elements.styles.defs.RubatoDef.createRubatoDef(String name)` to `createRubatoDef(String name, double frameLength)`. A `rubatoDef` without a `frameLangth` is invalid. Hence the factory could never create a `rubatoDef`. This is fixed.
- Refactoring of method `meico.mpm.elements.styles.defs.AccentuationPatternDef.getSize()` to `size()` to follow the convention used in all other classes.
- Added another method `addAccentuation()` to class `meico.mpm.elements.styles.defs.AccentuationPatternDef` that supports input of an `id` string.


#### v0.8.18
- Enhancement of class `meico.mpm.elements.metadata.Metadata` so it can be instatiated with related resources.


#### v0.8.17
- Made class `meico.midi.InstrumentsDirectory` public, so it can be used outside of its package.
- Extended method `meico.mei.Mei.makePart()`. 
    - This addresses issue [#23](https://github.com/cemfi/meico/issues/23) where the staff label did not suffice to properly indicate which General MIDI instrument should be chosen during the MIDI export. 
    - Thus, support for MEI element `instrDef` has been added. It should be used as follows.
      ````
      <staffDef clef.line="2" clef.shape="G" lines="5" n="1" label="unhelpful label">
          <instrDef midi.instrname="Violin" midi.instrnum="40"/>
      </staffDef>
      ````
      Only one of the attributes `midi.instrnum` (prioritized) and `midi.instrname` is required. The former should have values from 0 to 127 (not 1 to 128!). A list of General MIDI instrument names and numbers can be found on [Wikipedia](https://en.wikipedia.org/wiki/General_MIDI) (here the numbers must be decreased by 1!).
    - Meico will add a `programChangeMap` to the MSM part during export and use this instead of the label to generate the corresponding MIDI messages during MIDI export.
    - The MEI Coverage Documentation has been updated accordingly and provides further information.

#### v0.8.16
- Little tweak in method `meico.mpm.maps.DynamicsMap.renderDynamicsToMap()`: If a `dynamicsMap` does not have a `dynamics` element at date 0.0 but there are `notes` to be performed before the first `dynamics` instruction, they now get a default velocity value.


#### v0.8.15
- Little tweak in method `meico.mei.Mei.processMeasure()` as it struggled to remove and inserting `timeSignature` elements from `timeSignatureMap` when the measure does not comply with the given time signature.
- Indexing correction in method `meico.mpm.elements.styles.defs.AccentuationPatternDef.addAccentuationToArrayList()` so accentuations are added in the correct order to patterns.
- In method `meico.mei.Mei.processSyl()` attribute `wordpos` was mandatory (with values `i` or `m`) to process attribte `con`. This did not work for some cases. So, `wordpos` is now being ignored and `con` will always be processed.


#### v0.8.14
- Added method `meico.mei.Helper.addUUID()` which encapsulates the generation of unique `xml:id` attributes. The corresponding code in method `meico.msm.Msm.addIds()` has been adapted.
- Added another method `addAccentuationPattern()` to class `meico.mpm.elements.maps.MetricalAccentuationMap` which allows setting attribute `stickToMeasures`.
- Minor fix in method `meico.mei.Mei.addArticulationToMap()` which generated a `noteid` string from `null` instead of setting the string `null`.
- After update 0.8.12, method `getCurrentTimeSignature()` in class `meico.mei.Helper` required more context as input to determine the time signature information. It has been updated accordingly. This required adaptations also in several other methods in classes `meico.mei.Helper` and `meico.mei.Mei` which have been done, too.


#### v0.8.13
- Fix of the processing of `<accid>` elements where the parent `<note>` has different graphical and gestural pitch, see issue [#17](https://github.com/cemfi/meico/issues/17).


#### v0.8.12
- Enhancement of the processing of MEI `tie` element.
- Bugfix in method `meico.mei.Mei.processBreath()` that generated a wrong default articulation.
- Bugfix in method `meico.mpm.elements.maps.ArticulationMap.renderArticulationToMap_noMillisecondModifiers()`: NullPointerException in an error message.
- Optimization of methods `meico.mei.Helper.getPreviousSiblingElement()` and `getNextSiblingElement()`.
- An extensive overhaul of method `meico.mei.Mei.processMeasure()` which is now able to handle the situation that no global time signature is given (via `scoreDef`) and only locally defined in the `staffDef` elements.
- Some code polishing.


#### v0.8.11
- Another bugfix: The `endid` of MEI `tie` elements was not properly resolved.


#### v0.8.10
- Bugfix: If an MEI `space` element was in a `layer` environment, it was falsely interpreted as textual gap. However, it is a musical gap and should be interpreted as rest.


#### v0.8.9
- Updated MPM API to MPM v2.1.0. This update moved element `relatedResources` into element `metadata`.
- Added new class `meico.mpm.elements.metadata.RelatedResource`.
- Bugfix: MEI to MPM conversion generated tempo style switches while there was no tempo style to switch to. This has been fixed.


#### v0.8.8
- Added a getter to class `meico.mpm.Mpm` to provide access to the metadata: `getMetadata()`.
- Added support for MEI `verse` and `syl` elements, so they are converted to MSM `lyrics` elements.
- Added support for MEI `dynam` and `tempo` elements that are positioned within a `verse` environment.
- New method `processSpace()` for processing MEI `space` elements. These elements are usually interpreted as rests. However, this should not be done when they encode a textual space, e.g. in lyrics. That is what the method ensures.


#### v0.8.7
- Enhancement in method `meico.msm.Msm.parseProgramChangeMap()` so MIDI program change events can also be generated after date 0.0. This makes it possible to switch instrument/timbre during the music.


#### v0.8.6
- Another bugfix in method `meico.mpm.elements.styles.defs.ArticulationDef.articulateNote()` so style switches with no attribute `defaultArticulation` (it is optional) are supported.


#### v0.8.5
- New methods in class `meico.mei.Helper`: `pulseDuration2decimal()`, `decimalDuration2HtmlUnicode()`, `durationRemainder2UnicodeDots()`, `accidDecimal2unicodeString()`. These can beused to generate Unicode strings from note value and pitch information.
- Bugfix in method `meico.mei.Mei.makeMovement()`. It checks for the file to be not null before accessing it.
- Bugfix in method `meico.mpm.elements.styles.defs.ArticulationDef.articulateNote()` that cause articulation rendering running into an infinite loop when `absoluteDurationChange` is checked to create only non-negative durations.


#### v0.8.4
- Another bugfix in method `meico.mpm.elements.maps.DynamicsMap.renderDynamicsToMap()`. Seems like the previous update solved one bug and introduced another.
- Enhancement in method `meico.mpm.elements.maps.DynamicsMap.getDynamicsDataOf()`. It is now possible to set `subNoteDynamics="true"` even in a constant dynamics segment. This can be useful after a continuous segment to avoid sudden steps of the MIDI channel volume controller.


#### v0.8.3
- Added a new Output option to meicoApp. It is now possible during expressive MIDI rendering to also get an MSM that is enriched with performance data (milliseconds dates, velocities, etc.). This is mostly relevant for debugging purposes with no direct practical use for end users, yet.
- Changed method `meico.app.gui.StatusPanel.setMessage()` to write the output message not directly but from a JavaFX thread. This should eliminate some of the minor (non-blocking) exceptions that are thrown every now and then.
- Bugfix in method `meico.mpm.elements.maps.RubatoMap.renderRubatoToMap()` that caused some end date computations to return NaN.
- Bugfix in method `meico.mpm.elements.maps.data.DynamicsData.getSubNoteDynamicsSegment()` that cause decrescendi not being rendered with sub-note dynamics.
- Optimizations in method `meico.mpm.elements.maps.DynamicsMap.renderDynamicsToMap()`.
- New additions to class `meico.mpm.elements.maps.GenericMap`: methods `getFirstElement()`, `getLastElement()` and `isEmpty()`.


#### v0.8.2
- Bugfix in class `meico.msm.Msm`: If the MSM provided a `programChangeMap` for each `part`, only the first was correctly rendered to MIDI. This has been fixed.


#### v0.8.1
- New functionality added to class `meico.msm.Msm`: method `addIds()` adds `xml:id` to each `note` and `rest` element that does not have one. MeicoApp has been updated accordingly.


#### v0.8.0
- Reorganization of the meico programming library and application code as well as its release assets.
    - Meico is basically a programming library. All content of package `meico.app` is example code for meico's usage in application projects. However, for the graphical meico application certain JavaFX dependencies had to be added to meico which complicated development for other applications that do not rely on JavaFX. Thus, we decided to split the meico code base into the core functionality and the example applications.
    - The release assets include 
        - `meico.jar` (the headless core functionality to be used by other development projects) and 
        - `meicoApp.jar` (the standalone runnable `jar` with the commandline and graphical application, it includes `meico.jar`).
    - The codebase has been reorganized accordingly.
        - The `master` branch contains the headless meico, no application code or application-related dependencies. This is all you need to develop your own meico application.
        - The `meicoApp` branch contains the `meico.app` package and all other demo applications (meicoPy and the REST demo).
    - Class `meico.Meico` has been stripped down. It no longer contains any launcher code, only meico's current version number. MeicoApp lanches from its own main class `meico.app.Main`.
    - Class `meico.supplementary.VerovioProvider` as been removed. Verovio cannot be part of the headless meico base. It works only with a browser environment and that is only present in meicoApp (which contains Verovio).
    - File `README.md` has been updated. Livenses are also a bit different for those who use the headless meico package as JavaFX, Verovio and Font Awesome are used only in meicoApp and not in the base package.


#### v0.7.10
- Class `meico.mpm.Mpm`, when instantiated, parses the XML data and generates an exception if no element `metadata` was found. However, this is no encoding error and should not produce error messages. This has been fixed.
- Local Verovio update to v2.7.0-dev-02b4f36.
- Added new class `meico.supplementary.VerovioProvider` to be used in other classes (`Mei`, `MusicXml`) as a convenient handle to get the Verovio Toolkit script.


#### v0.7.9
- Method `meico.audio.Audio.convertByteArray2DoubleArray()` has been generalized to also work with sample sizes other than 16 bit. The method will no longer mix the two stereo channels into one, but returns an ArrayList with both channels, one double array each.
- Replaced the Java LAME sources in package `meico.audio` by external file `net.sourceforge.lame-3.98.4.jar`.
- In class `meico.audio.Audio` the new method `decodeMp3ToPcm()` has been added. With this, MP3 files can be imported, are decoded to PCM audio and can be stored as WAV files.
- Methods `writeAudio()` and `writeMp3()` in class `meico.audio.Audio` have been improved to make sure that a wave file is not stored with `.mp3` extension and vice versa.
- Some minor JavaDocs improvements and `README.md` update.
- Changed `com.sun.media.sound.InvalidDataException` by `Exception` for compatibility with newer Java versions.


#### v0.7.8
- Added MPM metadata support to package `meico.mpm`.
- The MPM metadata will also be generated during MEI-to-MPM export.
- Added a JavaDoc shield to `README.md`


#### v0.7.7
- Some refactoring in classes `meico.mpm.elements.maps.ImprecisionMap` and `meico.mpm.elements.maps.data.DistributionData` to avoid naming confusion with the MPM specification.
- Added MPM validation to class `meico.app.gui.DataObject`.
- In class `meico.mpm.Mpm`
    - Fix: No generation of an empty element `relatedResources`.
    - Extension: Methods for the deletion of elements from `relatedResources` have been added.


#### v0.7.6
- Added support for attribute `seed` to the MPM distribution elements and imprecision maps.


#### v0.7.5
- Some refactoring of MPM attributes to be compliant with the schema definition.
- In the GUI app, when an MPM is reloaded and its performances are displayed on the workspace, these performance objects do not update together with the MPM and, thus, are no longer consistent with their parent. This is confusing to the user. Hence, they are removed now and newly created from the updated data.


#### v0.7.4
- Enhancement in method `meico.mei.Mei.processMeasure()`. If a measure does not comply with the underlying time signature meico needs to add another `timeSignature` element in the `timeSignatureMap`. However, the subsequent measure may comply with the original time signature. Hence, at the end of the non-compliant measure meico should switch back to the original time signature. This is what it does now.
- Minor stability fix in method `meico.mei.Mei.addDynamicsToMpm()`.


#### v0.7.3
- MPM attributes `startStyle` and `defaultArticulation` have been removed from all `...-Map` elements. Initial styles are indicated by style switches (e.g. `<style date="0.0" name.ref="my initial style"/>` and for articulation maps `<style date="0.0" name.ref="my initial style" defaultArticulation="nonlegato"/>`) at the beginning of of the map. The corresponding code changes are in classes `meico.mei.Mei`, `meico.mpm.maps.ArticulationMap`, `DynamicsMap`, `GenericMap`, `MetricalAccentuationMap`, `RubatoMap`, `TempoMap`.
- In class `meico.mpm.Mpm` element `referenceMusic` is renamed to `relatedResources` and element `reference` has been renamed to `resource`.


#### v0.7.2
- Bugfix in `meico.mei.Mei.addArticulationToMap()` so it really generates articulations.


#### v0.7.1
- Extended the comandline application `meico.app.Main`, added flag `[-ex], [--expressive]`, so it does export expressive MIDI and audio.


#### v0.7.0 Music Performance Markup (MPM) API Integration
- Updated XOM to version 1.3.2. Updated the Ant script `build.xml` accordingly.
- New format added: Music Performance Markup (MPM):
    - The corresponding package is `meico.mpm`. This package contains the full API to generate, edit, store and parse MPM data and render them to expressive MIDI sequences.
    - Added several overloaded methods `meico.mei.Mei.exportMsmMpm()` which return two lists, one with  the MSMs, the other with the corresponding MPMs.
    - New methods in class `meico.mei.Mei`: 
        - `processDynam()` and `addDynamicsToMpm()` are used for converting MEI elements `dynam` and `hairpin` to MPM. 
        - `processTempo()` and `addTempoToMpm()` are used for converting MEI `tempo` elements to MPM.
        - `processArtic()` and `addArticulationToMPM()` are used for converting MEI articulation data (in elements `artic`, `note` and `chord`) to MPM.
        - `processBreath()` is used for converting MEI `breath` elements to MPM.
        - `processSlur()` and `meico.mei.Helper.checkSlurs()` are used to convert MEI `slur` elements to MEI `slur` attributes and ultimately to MPM legato articulations.
    - Meico's processing of MEI `note` and `chord` elements has been extended to support articulation, i.e. attributes `artic` and `artic.ges`.
    - New additions in class `meico.mei.Helper`: `mpmPostprocessing()`, list `tstamp2s`, `parseTempo()`, `updateMpmNoteidsAfterResolvingRepetitions()`.
    - Added MPM to the graphical user interface via class `meico.app.gui.DataObject`.
    - In MEI tempo is mostly associated only to the staff where it is printed even though it applies to all parts. There are several ways to encode the association, e.g. by `part="%all"`. If such is not specified meico will create only a local MPM `tempoMap`. The resulting performance will sound "surprising" as the parts become asynchronous. To cope with this in the way it might have been intended (all parts follow the same `tempoMap`) the GUI representation of an MPM object offers the option to make all local tempi global, i.e. merge all local tempo maps into one global tempo map.
    - Resolving `sequencingMap` in the GUI is also applied to the sibling MPM object.
    - The commandline application in class `meico.app.Main` has been extended accordingly.
- To preserve conformity between MSM and MPM the following MSM attributes have been renamed: `midi.date` into `date`, `midi.duration` into `duration`, `end` into `date.end`.
- New additions to class `meico.msm.Msm`: 
    - `renderMidi()` holds the actual algorithm that is invoked by the `exportMidi()` method and is extended by methods for generating MIDI from expressive data (e.g., milliseconds dates that have been computed from class `meico.mpm.elements.Performance`).
    - `exportExpressiveMidi()` is used to generate and export expressive MIDI data.
    - `parseChannelVolumeMap()` is used to render a part's `channelVolumeMap` into a sequence of MIDI channelVolume control change messages. That map is generated during performance rendering in method `meico.mpm.elements.Performance.perform()` and added to augmented MSM that it returns.
    - `parseProgramChangeMap()`, these maps can be used to add MIDI program change events and to keep consistency when the MSM is generated from a MIDI file; the method generates the corresponding MIDI events during MSM-to-MIDI export.
    - `convertPulsesPerQuarter()`/`convertPPQ()` is used to match the timing basis of the MSM object with the timing basis of a performance. It can be used in other contexts as well to change an MSM's timing basis.
    - `fitVelocities()`, `computeSemicircleCompression()`, `computePartwiseCompression` are used when the range of velocity values goes beyond the MIDI specification \[0, 127\]. 
- Changed the method signature of `meico.mei.Mei.convert()` to void. The result of the conversion is accessed via `meico.mei.Helper.movements` anyway. Method `meico.mei.Mei.exportMsm()` has been adapted accordingly.
- MEI preprocessing before conversion to MSM/MPM got an additional method `meico.mei.Mei.removeRendElements()`. This replaces all `rend` elements by their contents which reduces on-the-fly processing effort. These elements are an optional intermediate layer and not relevant for this particular conversion.
- Added class `AbstractXmlSubtree` to package `meico.xml`.
- Added package `meico.supplementary` for classes that are not specific to but useful across several other classes. 
    - First addition to this package is class `KeyValue` that represents data tuplets.
    - Second addition is class `RandomNumberProvider`. It offers several distribution types: uniform, Gaussian, triangular, Brownian, Compensating Triangular, and distribution list. The provider generates a consistent series of pseudo random numbers that is accessed via an index. The index can be floating point, this returns a linear interpolation of the neighboring integer index positions. The randomization can be reseeded. The random number series can be exported as `meico.audio.Audio` object and, thus, stored to the file system as `.wav` or `.mp3` file. 
- Moved activation and deactivation methods and flag from classes `meico.app.gui.Schema`, `meico.app.gui.Soundbank`, `meico.app.gui.XSLTransform` to class `meico.app.gui.DataObject` as they all do the same and non-activatable data objects can simply ignore this functionality. Respective code in class `meico.app.gui.Workspace` has been adapted.
- Applied slide changes to the graphical user interface's color scheme of data objects (which is computed in `meico.app.gui.Settings.classToColor()`) for better discriminability of different types of data objects.
- New additions to class `meico.mei.Helper`: `indexNotesAndChords()`, `getCurrentTimeSignature()`, `tstampToTicks()`, `computeControlEventTiming()`, `parseTempo()`, `reorderMeasureContent()`, `isSameLayer()`, `getStaff()`, `getStaffId()`, `checkSlurs()`, `addSlurId()`.
- Methods `meico.mei.Mei.reorderElements()` and `resolveTieElements()` have been removed. 
    - They did not take care of critical apparatus, `choice`, etc. The mechanism to reorder MEI control events according to attribute `startid` has been integrated with method `meico.mei.Mei.processMeasure`/`meico.mei.Helper.reorderMeasureContent()` and `meico.mei.Helper.computeControlEventTiming()`. This solves the aforementioned problem and is more efficient than the preprocessing procedure was. 
    - Processing of MEI `tie` elements has been redone with methods `meico.mei.Mei.processTie()`, `meico.mei.Helper.getTie()` and `checkTies()` and integrated with methods `meico.mei.Mei.processNote()` and `processChord()`.
- Method `meico.mei.Helper.computeDuration()` has been made more flexible. So far, it refused to compute anything as long as the MEI element was not within a `staff` environment. This, however, is only necessary if the element has no `dur` attribute. If that attribute is present the duration can be computed even outside of the `staff` environment - as the method does now.
- Enahncements in method `meico.mei.Helper.computePitch()`: 
    - Bugfix: The end of transpositions included the first note after the transposition.
    - Enhancement: For transpositions and octavings attribute `layer` is now taken into account, even if multiple layers are specified in it.
- The signature of methods `meico.mei.Helper.copyId()` has changed. They produce a return value which is the Attribute they generate. 
- Method `meico.mei.Mei.makeMovement()` has been extended. It will now keep a link to the corresponding `work` element in `meiHead/workList`. This is used during conversion to find time signature and tempo information if missing at the respective point in the `music` environment. After converting the contents of the `mdiv` method `makeMovement()` checks the existence of a global initial tempo in MPM. If missing, it is generated from the `work`'s `tempo` element (if there is one).
- Methods `meico.mei.Mei.processPhrase()`, `processPedal()`, `processOctave()`, `processTupletSpan()` have been redone. The processing of the corresponding MEI elements supports now attributes `tstamp.ges`, `tstamp`, `startid`, `dur`, `tstamp2.ges`, `tstamp2`, `endid`, `part` and `staff`.
- MEI elements `oLayer` and `oStaff` have been added to the processing in method `meico.mei.Mei.convert()`. They are processed by the same routines as MEI `layer` and `staff` elements.
- Method `meico.msm.Msm.applySequencingMapToMap()` has been updated to update also attributes `date.end`.
- New additions to class `meico.audio.Audio`:
    - `convertByteArray2DoubleArray()` makes analyses of audio data more convenient as it converts `Audio` object's byte array into an array of doubles between -1.0 and 1.0.
    - `convertDoubleArray2ByteArray()` converts an input double array into a byte array.
- Bugfix in method `meico.audio.Audio.convertByteArray2AudioInputStream()`: The computation of the length of the `AudioInputStream` did not take the number of channels from `AudioFormat` into account. This is fixed.
- Time consumption analysis and commandline/logfile output have been added to all export methods.
- Additions to the instruments dictionary: `Oboe d'amore`, `Oboe da caccia` and all clarinet names with an appended `" in"` so it matches better with, e.g., `Clarinet in E` and will not be confused with `Clarino in`.
- Method `meico.mei.Mei.processMeasure()` has been extended. After processing its contents and if the measure's length does not confirm the underlying time signature, intermediate `timeSignature` entries are added to the global MSM `timeSignatureMap` so that it follows the measure's timing.
- Method `meico.msm.resolveSequencingMaps()` did not expand global maps so far. This has been added. It will now also return a HashMap with the `xml:id` strings that have been extended to avoid multiple IDs in one XML document. This HashMap is used to update the `noteid` attributes in MPM `articulationMap`s because the repetitions are also expanded in MPM (if there is one that is linked to the MSM).
- Method `meico.mei.Helper.cloneElement()` does now also clone the namespace URI.
- In method `meico.app.gui.DataObject.addSeveralChildren()`, which is executed whenever more than one data object is created, a slight spiral effect has been added so that the overlapping of circles is a bit reduced. 
- In class `meico.app.gui.DataObject`, method `reloadMei()` has been renamed to `reload()` and extended so that it can also be used to reload MSM and MPM data. This functionality has also been added to the GUI, i.e. the radial menu of both object types.
- New additions to class `meico.midi.EventMaker`: 
    - method `createControlChange()`,
    - method `byteArrayToInt()`.
- In class `meico.midiMidi2MsmConverter` in method `processShortEvent()` noteOn velocity has been added to the MSM `note` elements, so it can be further incorporated in MPM performance rendering (as far as it is not overwritten by a `dynamicsMap`).
- New Addition to class `meico.midi.Midi`: method `getTempoMap()` parses the MIDI sequence, collects all tempo events and generates an MPM `tempoMap` from it.
- In package `meico.app` a new class has been added, called `Humanizer`. It demos a little bit of the MPM functionality by generating some basic humanizing and applying it to a Midi object. It is also integrated in the graphical user interface, just import or create a MIDI object and to the click the new option "Humanize (experimental)" on the right part of its radial menu.
- Added the full JavaDoc of meico to the repository.
- Verovio update to v2.4.0-dev-facbfa6.


#### v0.6.12
- Fixed license information for Saxon in `README.md`. Thanks to Peter Stadler!
- Made several classes in package `meico.app.gui` package-private as they will never be accessed from outside the package and are not intended to.
- Made class `meico.msm.MsmBase` abstract and renamed it to `AbstractMsm`.
- Removed MSM's global header element `pulsesPerQuarter`. Instead an eponimous Attribute has been added to the root note of the MSM.
- New additions to class `meico.xml.XmlBase`: Methods `removeAllElements(String localName)` and `removeAllAttributes(String attributeName)` can be used for processing of all XML based formats.


#### v0.6.11
- New conversion option: MIDI to MSM.
    - New method `meico.midi.Midi.exportMsm()` implemented.
    - The actual conversion is implemented in class `meico.midi.Midi2MsmConverter`. To use it, instantiate it with its constructor, then invoke convert(), see method `meico.midi.Midi.exportMsm()` for sample code.
    - Standard MIDI file formats 0, 1 and 2 are supported.
    - Relevant MIDI events that are parsed into MSM data are: NOTE_OFF, NOTE_ON, PROGRAM_CHANGE, META_Track_Name, META_Instrument_Name, META_Marker, META_Midi_Channel_Prefix, META_Midi_Port, META_Time_Signature, META_Key_Signature. More may be added with future updates, e.g. META_Lyric.  
    - META_Track_Name and META_Instrument_Name events as well as PROGRAM_CHANGE events are incorporated to create meaningful MSM part names.
    - New method `midi2PnameAndAccid()` in class `meico.mei.Helper` that converts a MIDI pitch to a pitch name string and an MSM-conform accidentals string.
    - Added methods `getInstrumentName()` and a String array with the GM standard instrument names to class `meico.midi.InstrumentsDictionary`. Given a program change number, it retruns the instrument's name, i.e. either the standard GM names or the first string that is associated with this program change number in the dictionary.
    - Made methods `meico.mei.Helper.msmCleanup()` public to be accessible from class `meico.midi.Midi`.
    - The conversion option for MIDI objects in the graphical user interface has been added in class `meico.app.gui.DataObject`.
- Updates in class `meico.msm.Msm`:
    - Class `meico.msm.Msm` got a static method `createMsm()` that creates a minimal `Msm` instance with the basic xml markup. This simplifies the code in method `meico.mei.Mei.makeMovement()`.
    - Moved method `meico.msm.MsmBase.getParts()` to class `meico.msm.Msm` as it is different in a future implementation of the format MPM (Music Performance Markup).
    - Added new methods to class `meico.msm.Msm`: `addPart()`, `makePart()`. The latter extends the eponimous methods in class `MsmBase` with some Msm-specific map creations that will be different in MPM. Some adaptations in method `meico.mei.Mei.makePart()` have been done in accordance to this.
    - The MIDI export in class `meico.msm.Msm` has been extended a little bit.
- New additions to the instruments dictionary: Superior, Superius, Contratenor, Clarino, Clarino in, Clarini, Clarini in, Ocarina, Gefäßflöte, and Kugelflöte. Many thanks to Richard Freedman for his suggestions.
- Bugfixes
    - In method `meico.mei.Helper.computePitch()` the octave of preceding accidentals was not correctly recognized.
    - `accid` elements that occur in transposing staffs/layers are not assigned to the correct pitches and octaves due to the transposition context. This has been fixed in method `meico.mei.Mei.processAccid()` by adopting the untransposed attributes `pname` and `oct` (non-gestural) from the parent `note`.
    - In method `meico.mei.Mei.processDot()` an incorrect condition has been corrected.
    - In method `meico.msm.Msm.parseKeySignatureMap()` the computation of accidentals was corrected.
- Added some commandline/logfile output to all export methods for better readability.
- Added some missing MIDI meta messages to class `meico.midi.EventMaker`.


#### v0.6.10
- Added support for MEI attribute `sameas` to class `meico.mei.Mei` in method `resolveCopyOfs()`. It is interpreted in the same way as attribute `copyof`, i.e. all attributes and children of the referred element are copied over to the referring element.
    - Added method `meico.mei.Mei.resoveCopyofsAndSameas()` which ultimately just invokes `resolveCopyOfs()` but has a more appropriate naming.
    - Classes `meico.app.Main` and `meico.app.gui.DataObject` as well as files `README.md` and `documentation.md` have been adapted accordingly.


#### v0.6.9
- Added a new commandline option to meico `-n`/`--ignore-repetitions`. This should be used to prevent the expansion of repetition marks. It should solve the situation when expansions and repetitions in MEI are used redundantly and meico (commandline) would do it twice.
- Fixed bug on the settings window of the gui app (class `meico.app.gui.Settings`). JavaFx's `Spinner` class does not commit a value when changed via text input. Thus, whenever a value (e.g. the tempo) was changed this way, the user had to press ENTER to commit. To fix this an event listener has been added that forces the commit when the value changes even without the need to press ENTER.
- The window for preferences settings is made resizable and scrollable. This will hopefully workaround the limited window size issue that some users experience on their machines.


#### v0.6.8
- Reverted the JavaFX update from v0.6.7 to keep compatibility to OpenJDK 9.


#### v0.6.7
- Reverted changes in `verovio.html` to v0.6.5 as this was more stable. Some experimental suff has been added that does not affect its performance.
- A new package `meico.svg` with classes to hold SVG data has been added.
    - It is also integrated in the graphical user interface. 
    - However, there is no generic SVG export from MEI, yet, even with Verovio.
- JavaFX dependencies have been updated to version 12.0.1.
- Internal Verovio update to v2.1.0-dev-b010e32.
- Added configuration option `scoreFont` (default value is `Leipzig`) to set Verovio's rendering font. To set another font, file `meico.cfg` must be edited. It is no yet built into the preferences page of the GUI.


#### v0.6.6
- Minor revisions in `verovio.html`.
- Updated internal Verovio to v2.1.0-dev-865f210.
- Added `measure` elements to the `addIds` functionality of class `meico.mei.Mei`.


#### v0.6.5
- `.xml` import and type recognition now supported: Added method `meico.app.gui.DataObject.readXmlFileToCorrectType()`. It is called when a `.xml` file is loaded. So far, meico rejected to load such files as it was unclear which kind of data it holds. The new method loads it as an `XmlBase` object and reads its root name to find out whether it is an MEI, MSM, MusicXML, or XSLT. An instance of the corresponding type is then created.
- Added floating point support to time signatures in methods `meico.mei.Helper.getOneMeasureLength()` and in class `meico.mei.Mei` methods `processMeasure()`, `makeTimeSignature()`, `processBeatRpt()`, `processMRpt2()`, and `makeMeasureRest()`.


#### v0.6.4
- Bugfix in `meico.mei.Mei.processAccid()`.
- Added error catching in methods `meico.mei.Helper.computePitch()` and `meico.mei Helper.computeDuration()`.


#### v0.6.3
- Bugfix in commandline mode `meico.app.Main`. It forgot to make pitch and chroma export.
- Enhancement in class `meico.mei.Mei` methods `processStaffDef()`, `processStaff()`, `processLayer()` to support nested structures (`staffDef` within `staff` etc.).
- Updated internal Verovio to v2.1.0-dev-533442f.

#### v0.6.2
- Updated internal Verovio to v2.0.0-dev-61da81a.
- Added package and class `meico.xml.XmlBase` as a base class for all XML-based classes in meico.
    - Refactored class `meico.msm.MsmBase` accordingly.
    - Class `meico.mei.Mei` has been refactored, too, to extend `XmlBase`.
    - Disabled fetching of DTD in method `meico.xml.XmlBase.readFromFile()` as this causes problems in XOM preventing parsing XML files with a DOCTYPE declaration.
- Added floating point support for transpositions in MEI.
- Added class `meico.musicxml.MusicXml`.
    - So far, it is just a dummy class that parses uncompressed MusicXML, writes to the file system, offers XSL transform, validation and access to the data ... standard XML functionality in meico. There are currently no export methods to convert to or from MusicXML.
    - `.musicxml` files can be imported in the GUI app. XSL transform and validation are fully functional.
    - This will hopefully grow further in the future.

#### v0.6.1
- Added new methods to class `meico.midi.Midi`: `getPPQ()`, `getTickLength()` and `getMicrosecondLength`.
- Minor corrections in the constructor methods of classe `meico.midi.Midi`.
- Added new method variant of `meico.mei.Helper.validateAgainstSchema()` that takes an input string instead of a file. With this validation does not require the data to be present in the file system.
- Note: We did some experiments with XML schema files in XSD format. However, meico uses the Saxon 9 HE version which supports only RNG schema files for validation, in contrast to the EE version. Thus, XSD files should be converted to RNG format to be used in meico.
- Added new class `meico.msm.MsmBase` which forms the base class for MSM and (later on) MPM classes. Hence, class `meico.msm.Msm` is now refactored to extend `MsmBase`.
    - Method `meico.mei.Mei.makePart()` has been edited to utilize the `makePart()` functionality of `MsmBase`.
    - Added new method `meico.msm.MsmBase.validate()` to validate the MSM and MPM code against a specified schema. Though, at the moment there is no corresponding schema definition, at least for MSM.

#### v0.6.0
- Added `System.setProperty("prism.order", "sw");` to method `meico.app.gui.MeicoApp.init()`. This fixes a graphics glitch that occurs every now and then.
- Meico's MIDI to audio renderer relies on the package `sun.com.media.sound`. However, since Java 9 this package is marked as deprecated. It was still accessible at runtime but is going to disappear from Oracle JDK in the near future. It seems to have disappeared from OpenJDK as well.
    - To solve this the Gervill Sound Synthesizer `gervill.jar` has been added to the `externals`. It provides the required package, so no code changes were necessary.
    - Since it is published under GNU GPL 2.0, we have to change meico's license model as well. The new license is GNU GPL 3.0.
- Some JavaFX 11 modules had to be added, too, as it is no longer included in any JDK (neither Oracle nor Open): `javafx.base.jar`, `javafx.controls.jar`, `javafx.graphics.jar`, `javafx.media.jar`, and `javafx.web.jar`.
    - These are also licensed under GNU GPL 2.0.
    - In GUI mode, JavaFX version number has been added to the log file/commandline output at startup.
- Because of incompatible licensing models we had to remove the MEI Common Music Notation Schema `mei-CMN.rng` from this project.
    - Instead, RNG files can now be imported and used in the same way as XSLTs and soundfonts, i.e. drag and drop it into the workspace, activate it and validate the MEI.
    - Classes `meico.mei.Mei`, `meico.app.gui.Settings`, `meico.app.gui.Workspace`, `meico.app.gui.DataObject`, and `meico.app.Main` had to be adapted.
    - New class `meico.app.gui.Schema` has been added.
    - It is possible to set a default schema that meico will remember with every restart. Once this is done, meico behaves in the same way users of previous versions are used to.
- Updated copyright notice in class `meico.midi.Midi2AudioRenderer` and the license information in `README.md`.


#### v0.5.5 Verovio Integration
- Added a welcome message in the workspace: `"Drop your files here: MEI, MSM, TXT, MIDI, WAV, XSL, SF2, DLS."` and a file drop icon. This should clarify the first step of meico's usage to every beginner and provides a list of file formats that can be imported. The whole thing is responsive to window height, i.e. resizing the window will adjust the scale of the message. It will disappear after successfully dropping/importing the first file.
- In class `meico.app.gui.Settings` there were some InputStreams that have not been closed. This has been fixed.
- Disabled the option to switch off the internal WebView. Not all information shown internally can be forwarded to an external browser.
- [Verovio](https://www.verovio.org) integration:
    - The motivation behind adding the webview to the GUI was to show Verovio's score renderings in it. This is now finally implemented.
    - Added a prototype HTML document `/resources/Verovio/verovio.html`.  It contains a placeholder `"MeiCode"` which will be replaced by actual MEI code.
    - The new class `meico.app.gui.VerovioGenerator` provides the necessary methods to generate an HTML code that calls Verovio and passes the MEI code to it.
    - Classes `meico.app.gui.DataObject` and `meico.app.gui.WebBrowser` have been adapted.
    - In class `meico.app.gui.Settings` a new flag `oneLineScore` has been added. If true, the score rendering will print all music in one system. This can be specified by the user in the settings.
    - In the settings the user can decide whether to use the local version of Verovio that is packed into meico's jar or the latest online available version (requires internet connection, of course).
    - Do not expect too much, Verovio works suboptimal in Java's JavaScript engine.
- Whenever something is shown in the WebView, i.e. when clicking on "Score Rendering" or "Show",
    - the WebView window will expand automatically and
    - its title string will change to express what is shown.
- If an MEI or MSM does not provide a title, its visual representation in the GUI will be an empty circle. This has been changed. If the title string is empty, the filename will be printed.
- Class `meico.app.gui.WebBrowser` has extended `VBox` which cased a problem when resizing the window. The render area did not resize. This has been fixed by extending `StackPane` instead of `VBox`.
- Added two new methods `prettyXml()` and `repeatString()` to class `meico.mei.Helper` to generate a formatted XML string from an unformatted one, basically for printing MEI and MSM code in the WebView. The corresponding options have been added to their menues.


#### v0.5.4
- Some tweaks in the Travis CI script `.travis.yml`. Because of bad OpenJFX support Travis CI reports build fails with OpenJDK. At the moment only the Oracle JDK builds can be trusted.
- Finished the MEI Coverage Documentation `documentation.md`.
- Some minor code revisions, nothing essential.


#### v0.5.3
- Bugfix in method `meico.mei.Mei.readMeiFile()`: If argument `validate` is set `true`, it was trying to validate the MEI before it was actually loaded.
- Generated an up-to-date Ant script `build.xml` and added `<manifest> <attribute name="Main-Class" value="meico.app.Main"/> </manifest>` to ensure that there is a main manifest attribute.
- Thanks to David Weigl for reporting these issues!


#### v0.5.2
- So far, XSL transform functions of meico did support only XSLT 1.0 and 2.0. In some Java versions also 3.0 stylesheets worked but not in general. This issue has been solved.
    - Update [Saxon](http://saxon.sourceforge.net/)  to version 9.8.0.14 HE.
    - Due to signature issues the files `TE-050AC.RSA` and `TE-050AC.SF` had to be removed from the `META-INF` folder of `saxon9he.jar`.
    - In class `meico.mei.Helper` all XSLTransform processing methods have been redone and new ones have been added in order to overcome the afformentioned issues. XOM and Java were accessing Saxon's old transformer functionality only which does not support XSLT 3.0. The reworked versions access Saxon directly now and use its `Xslt30Transformer` for all stylesheets. Classes `meico.mei.Mei`, `meico.msm.Msm`, `meico.app.gui.XSLTransform`, `meico.app.gui.Workspace`, and `meico.app.gui.DataObject` have beed adapted accordingly.
    - Some further optimizations to XSL transforms have been implemented, esp. in classes `meico.app.gui.Settings` and `meico.app.gui.DataObject`.
- Added a custom meico icon to the window titlebar. It replaces Java's default icon. This and further icon files have been added to `resources/figures`.
- When closing objects in the workspace the garbage collector does not seem to free their allocated memory automatically. A call of `System.gc()` has been added to class `meico.app.gui.Workspace` method `clearAll()` to force the garbage collection.
- When changing the settings for accordion animation and auto expansion of the player a restart is no longer necessary necessary.
- Added method `main()` to class `meico.app.gui.MeicoApp` so it can be compiled and run as self-contained JavaFX application.


#### v0.5.1
- Bugfix: make logfile when checked in the settings.
- If a file drop fails, the exception message is sent to the statuspanel.
- The conversion option "Score Rendering" has been removed from MEI menu as long as Verovio integration is not yet functional.
- Updated `README.md`.


#### v0.5.0
- New graphical user interface:
    - The new desktop application is located in package `meico.app.gui` in class `MeicoApp`.
    - Lots of new file formats are droppable. Here is an overview of all supported file extensions: `.mei`, `.msm`, `.mid`, `.wav`, `.txt`, `.xsl`, `.sf2`, `.dls`.
    - In addition to these import formats there are the following export formats that cannot be imported: `.mp3`, `.json` (encodes pitch/chroma information).
    - Radial menus provide access to all operations that can be applied to the different data types. As a convenience shortcut MIDI and audio playback can be triggered by doubleclick. Soundbanks and XSLTs can also be activated via doubleclick.
    - The following stuff has been deleted from the project as they are no longer required:
        - classe `meico.app.MeicoApp` has been removed,
        - class `meico.app.FileDrop` has been removed,
        - the layout manager MigLayout has been removed from `externals`,
        - all the graphics from the old GUI have been removed from the `resources/graphics` folder.
    - Font Awesome has been added.
    - Meico uses the default system font. Layouting reacts on different font measurements.
    - Added keyboard input to trigger playback: SPACE (does not work on all systems), ENTER and the play/pause key (one of the extra media keys on some keyboards).
    - All operations are now processed in seperate threads. This prevents interface freezing and allows to run several operations in parallel. While an operation is processed, a "computing/please wait" animation is shown on the corresponding data item.
    - A separate window offers some preferences settings to customize meico a little bit and preload certain soundbanks and XSLTs. However, most of the settings will become active only after restarting meico. All settings are stored in a file `meico.cfg` when closing meico and restored at the next startup. The file will be generated if not yet existent.
- Class `meico.Meico` got two new static methods `launch()` and `launch(String windowTitle, String logFileName)`. Applications can conveniently launch meico's graphical user interface by calling one of these methods, e.g. `Meico.launch()` or `Meico.launch("My window title")`.
- Some revisions to classes `meico.midi.MidiPlayer` and `meico.audio.AudioPlayer` to ensure data integrity and correct feedback on method call `isPlaying()` (esp. in `AudioPlayer`). Class `MidiPlayer` got also two further getter methods `getMicrosecondLength()` and `getTickLength()`.
- The output of method `meico.mei.Mei.validate()` has been changed from `boolean` to `String`. Now it returns the whole validation report. To get a `boolean` call `validate()` and then `isValid()`.
- Added lots of `synchronized`s so that meico should run more stable in multithreaded environments.
- Up to now, the MEI data was altered during conversion rendering it invalid. It has no use except for debugging purposes. We now ensured, that the original version is reset in method `meico.mei.Mei.exportMsm(int ppq, boolean dontUseChannel10, boolean ignoreExpansions, boolean cleanup)` after the conversion process. The altered version is kept only if attribute cleanup is set false (debug mode).
- XOM library has been updated to version 1.2.11.
- Known issue: Loading XSLTs does not work on several Java versions. The issue seems to originate from the XOM library. In tests with Java 1.8.0_172 everything works fine. So at the moment this is the recommended Java version for running meico, at least regarding the use of XSLTs. everything else works alo in later versions.


#### v0.4.0
- Added `NullPointerException` to audio rendering Exception handling in `meico.app.MeicoApp`, subclass `Midi4Gui`.
- MIDI playback overhaul:
    - Extracted all MIDI playback related parts from `meico.midi.Midi` to new class `meico.midi.MidiPlayer`. Class `Midi` holds the data and its processing methods, class `MidiPlayer` provides playback and synthesis functionality.
    - Removed all MIDI playback methods from class `Midi`. Hence, this part is no longer backwards compatible.
    - Class `MidiPlayer` features lots of new getter, setter and play methods.
    - An application that wants to play MIDI data should do this:
        ```
        MidiPlayer player = new MidiPlayer();
        player.loadSoundbank(soundbank); // this is optional; soundbank is a File or URL object
        player.play(myMidi);             // myMidi is an instance of Sequence or meico's Midi class
        ... music plays ...
        player.stop();
        player.close();                  // if not needed anymore, close it
        ```
    - Soundbanks can now be loaded and applied to MIDI playback. In GUI mode, users do not have to render audio to get the "high-quality" sound output. Hence, setup of the soundbank has been shifted to the MIDI name (right click). Here users can select the `.sf2` or `.dls` file to be used or switch back to Java's default soundbank. It is even possible to switch the soundbank during playback.
    - The corresponding parts of `meico.app.MeicoApp` have been adapted. There is no longer a player for each MIDI instance but only one global MidiPlayer instance that is fed with the MIDI data (either a `Sequence` object or a `Midi` object) to be played back.
- Audio playback overhaul:
    - Extracted all audio playback related parts from `meico.audio.Audio` to new class `meico.audio.AudioPlayer`. Class `Audio` holds the data and its processing methods, class `AudioPlayer` provides playback functionality.
    - Removed all audio playback methods from class `Audio`. Hence, this part is no longer backwards compatible.
    - Class `AudioPlayer` features lots of new getter, setter and play methods.
    - An application that wants to play audio data should do this:
        ```
        AudioPlayer player = new AudioPlayer();
        player.play(myAudio);   // myAudio is an instance of meico's Audio class
        ... music plays ...
        player.stop();
        ```
- Added method `exportAudio(URL soundbankUrl)` to class `meico.midi.Midi` so that soundbanks do not have to be local files and URLs do not have to be decoded to files by the application.
- Slight revisions to class `meico.audio.Audio` to ensure data integrity after MP3 conversion.
- A note concerning MIDI to audio rendering: Meico's MIDI to audio renderer relies on the package `sun.com.media.sound`. However, Java 9 and later versions do no longer provide access to this package at compile time. It is still accessible at runtime. Hence, meico should be compiled with Java 8 and can run with later versions (tested until Java 10). But at some point they will probably make this package inaccessible also at runtime. A workaround for this is using the Gervill Sound Synthesizer (search `gervill.jar` in the internet and add it to `externals`) that provides the required package, so no code changes are necessary. However, consider that Gervill is licensed under GNU GPL-2.0 while meico is under GNU LGPL-3.0!


#### v0.3.8-rev
- Never trust automated refactoring. Some strange side effects have been fixed.


#### v0.3.8
- Added library JSON.simple v3.0.2
- Switched the output of class `meico.pitches.Pitches` to JSON format. The default output file extension will also be `.json` from now on.
- The whole data structure of pitch (and chroma) data has been revised. Some new classes have been added to package `meico.pitches`, namely `FeatureVector` and `FeatureElement`.
- To make the pitches data structure and output file as memory efficient as possible, the timing resolution is reduced to the minimum ppq necessary to preserve accurate rhythm and note durations. MSM's ppq remains unchanged!
- Previos function calls stay the same to ensure backwards compatibility. Hence, we do not increment the beta version number.
- New methods `getParts()`, `getPPQ()` and `getMinimalPPQ()` in class `meico.msm.MSM`.


#### v0.3.7
- Updated Saxon parser to version 9.8.0.11 HE.
- Made the v0.3.6 fix a bit safer.


#### v0.3.6
- Fixed missing `accid.ges` support in `meico.mei.Helper.computePitch()` for preceding `accid` elements.


#### v0.3.5
- Another bugfix: processing of `accid` elements preceding to a `note` element has been fixed.


#### v0.3.4
- Little bugfix in `meico.mei.Mei.processAccid()`. In case of `note` elements with `accid` children that do not provite an `oloc` attribute, the `octave` is read from the `note`. Now it is done correct.


#### v0.3.3
- Updated `meico.mei.Mei.processNote()` so that first its child elements (`accid`, `dot`) are processed before processing the `note` itself.
- Extended processing of `accid` elements to look for parent notes in case the `oloc` or `ploc` attribute are missing.
- New method added `meico.mei.Mei.processDot()`: `dot` elements have been processed directly during the processing of `note` and `rest` elements which lead to not considering the critical apparatus elements and, hence, not recognizing dots within that environment. This should be fixed now.


#### v0.3.2
- In window mode the checkbox "Do not resolve expansions" has been renamed to "Resolve expansions". This should be more intuitive than checking a negated statement.
- More documentation has been added.
- The root element of MSM documents has been renamed from `meta` to `msm`.
- During MEI-to-MSM conversion, when resolving MEI elements `beatRpt`, `halfmRpt`, `mRpt`, `mRpt2`, and `multiRpt`, elements with duplicate IDs were created. This is taken care of now.
- Added a new method to class `meico.mei.Mei` that returns the title of the music, `getTitle()`.
- Method `meico.mei.Mei.makeMovement()` has been extended. It creates now also a `title` attribute in the MSM root element `msm`. That `title` attribute is a concatenation of the work's title and the `mdiv` element's attributes `n` and `label`.


#### v0.3.1
- Added class `meico.Meico` that holds the version number of meico. It can be accessed via `Meico.version`.
- Added a new package `meico.pitches` with classes `Pitches` and `Key`.
- Added new methods to class `meico.msm.Msm`:
    - `getEndDate()` returns the date of the last note offset, i.e. the length of the music in MIDI ticks.
    - `exportChroma()` converts MSM to a sequence of chroma features with 12 semitones in equal temperament and A = 440 Hz.
    - `exportPitches()` converts MSM to a sequence of absolute pitch vectors with 12 semitones per octave in equal temperament and A = 440 Hz. This conforms with the MIDI standard, i.e. 0 is the lowest and 127 the highest possible pitch. This method is overloaded. The more general version of this method takes an instance of `meico.pitches.Key` as parameter.
- Some adds to the commandline app:
    - Added new option `-o` and `--chroma` to get chroma export.
    - Added new option `-h` and `--pitches` to get absolute pitches export.
    - Added the current version number to the output of option `-?`/`--help`.
    - File `README.md` has been updated accordingly.
- Chroma/pitches export has also been added to the window mode gui, available via right click on an MSM object.
- File `README.md` has been updated with the new pitches/chroma functions.
- Redirected several error messages to `System.err` instead of `System.out`. This makes output in the commandline and log file more consistent.


#### v0.3.0
- Two new types of maps have been added to the MSM format, `sectionMap` and `phraseMap`. Their contents derive from `section` and `phrase` elements in MEI. They indicate musical sections and phrases. The MEI-to-MSM conversion has been extended by the corresponding routines (`meico.mei.Mei.processSection()` and `meico.mei.Mei.processPhrase()`).
- Added a new method `meico.mei.Helper.getMidiTimeAsString()` which is useful to avoid unnecessary String-to-Double-to-String conversions.
- Added support for MEI `expansion` elements.
    - New methods added to class `meico.mei.Mei`: `resolveExpansions(Element root)` and `resolveExpansions()`. The latter is public and can be called by applications to convert an MEI with `expansion` elements into a "through-composed" MEI without `expansion` elements.
    - A new parameter `ignoreExpansions` has been added to method `meico.mei.Mei.exportMsm(int ppq, boolean dontUseChannel10, boolean ignoreExpansions, boolean msmCleanup)`. So meico can be forced to convert the MEI data either as it is or in its rearranged form as indicated by `expansion` elements. There is also a method `meico.mei.Mei.exportMsm(int ppq, boolean dontUseChannel10, boolean ignoreExpansions)`, if you have used a previous version of meico be sure to check consistency with your `exportMsm()` calls as there is a slight backwards incompatibility at this point!
    - A new parameter (`-e`, `--ignore-expansions`) has been added to the commandline app.
    - The Python demo `meicoPy.py` got the same parameter now.
    - In the window app a checkbox has been added to the MEI-to-MSM conversion options saying `Do not resolve expansion elements`.
    - The REST api has also been updated and features the new parameter `ignore_expansions`.
- The resolution of MSM `sequencingMaps`, i.e. repetitions in MEI, has been redone and should work properly now.
    - Method `meico.msm.Msm.applySequencingMapToMap()` has been rewritten.
    - Class `meico.msm.Goto` has a second constructor method `Goto(Element gt)` which is much safer and more convenient than `Goto(double date, double targetDate, String targetId, String activity, Element source)`. It features also an new method `isActive()` that is used during the
- MEI `staffDef` elements do not necessarily need to have an attribute `label` but could also have a child element `label`. Support for this latter type has been added to method `meico.mei.Mei.makePart()`.
- `Continuo` has been added to the instruments dictionary as an instance of `Harpsichord`.


#### v0.2.24
- Added MEI `section` elements to method `meico.mei.Mei.addIds()`, so sections without an id get one.
- Added method `meico.mei.Mei.processSection()` to experiment with interpreting MEI sections as potential repetition starts. But sections cannot generally be interpreted this way. So this function has been commented out again.
- Initiated an MEI Coverage Documentation in `documentation.md`. Detailed descriptions need to be added in the future.


#### v0.2.23
- MSM `movement` ids had no namespace. Now they are in the xml namespace.
- Repetitions started always at an `rptstart` or the beginning of the piece. End barlines where ignored, so far. Meico now interprets `<measure left/right="end">` as a potential repetition start.


#### v0.2.22
- Bugfix in `meico.mei.Mei.processApp()`.
- Bugfix in `meico.mei.Mei.processLayer()`.
- Added method `meico.mei.Helper.getFirstChildElement(Element ofThis, String localname)`. It is a workaround for the XOM method `getFirstChildElement(String name)` which sometimes does not seem to work properly.
- Enhancements in the processing of `choice` elements.
- Added support for `restore` elements.
    - It negates all `del` children (by adding an Attribute `restore-meico="true"`). So the deletions will be considered during conversion.
    - The processing of `del` elements has been enhanced, accordingly.
    - But be aware, in our implementation a `restore` does not overrule a parent `del` element! Hence, putting a `del` around a `restore` effectively deletes the restoration.


#### v0.2.21
- Added support for critical apparatus, i.e. the elements `app`, `lem` and `rdg`.
    - This is part of the MEI-to-MSM conversion process in `meico.mei.Mei.convert()` and is implemented in function `meico.mei.Mei.processApp()`.
    - If an `app` element occurs, meico chooses the first (and hopefully only) `lem` element to process. All other child elements are ignored. If no `lem` is in `app`, meico chooses the first `rdg` element.
    - If any other choice is desired, the user has to resolve this manually. A tool for generating "variant-free", unambiguous MEI code is the [MEI Sequence Editor](http://nashira.uni-paderborn.de:5555/seditor). This tool goes through all variants and lets the user make the decision. It then outputs the corresponding MEI file.
- Added editorial element support, i.e. all the elements that can occur in the `choice` and `subst` environment.
    - The elements `sic` and `corr` are processed in both cases of occurence, as "standalone" (always processed) and in the `choice` environment as `sic`-`corr` pair (`corr` is chosen).
    - The same for `orig` and `reg`. In the `choice` environment `reg` is favored.
- Added also the elements `add`, `expan`, `supplied`, `unclear` to the supported elements in MEI-to-MSM conversion. The `del` element is also processed now, but basically ignored.
- The `cert` attribute (certainty) is not taken into account so far. And also the `restore` environment is not processed, yet.


#### v0.2.20
- Some adds to the instruments dictionary.
- Bugfix in `Msm.applySequencingMapToMap()`.
    - Only the first repetition was rendered correctly. Now also the later repetitions are correclty processed.
    - Issue with duplicate `xml:id`'s is solved.
- Optimizations in `Msm.resolveSequencingMaps()`.
- Bugfix in GUI layout when loading a `wav` file.
- The library Java-String-Similarity has been updated to v1.0.0.
    - Updated Ant script `build.xml` accordingly.
- Removed OracleJDK 7 from Travis CI builds `.travis.yml` as it is no longer supported by Travis CI. Java 7 compatibility can still be checked with OpenJDK 7.


#### v0.2.19
- Changed MP3 encoding in method `meico.audio.Audio.encodePcmToMp3()` to `Lame.QUALITY_HIGH`.
- Little additions to all `README.md` files.
- Added method `meico.midi.Midi.writeMidi(String filename)`.
- The `write...` methods in classes `meico.midi.Midi` and `meico.audio.Audio` did not ensure that the file actually exists in the file system or is created if not. This has been fixed.
- All `write...` methods in classes `meico.midi.Midi` and `meico.audio.Audio` return a `boolean` on the success of the file writing. Exception throwing has been removed. Classes `meico.app.Main`, `meico.app.MeicoApp` and `meicoPy.py` have been adapted accordingly.
- Added a REST interface implementation (to be found in directory `rest`). Corresponding startup and usage documentation can be found in the `README.md` file at this directory.
- Added `requirements.txt` to meicoPy.


#### v0.2.18
- Added MP3 export to `meicoPy.py`.
- Added more constructor methods in `meico.mei.Mei` and `meico.msm.Msm` to instantiate also from a Java InputStream.


#### v0.2.17
- Added method `getDocument()` to class `meico.mei.Mei` and `toXML()` to classes `meico.mei.Mei` and `meico.msm.Msm`.
- Bugfix in methods `meico.mei.Mei.exportMei()`, `meico.mei.Mei.exportMsm()` and `meico.msm.Msm.exportMidi()`: create filname only if source file name is given, otherwise create export object with `null` as filename.
- Added new constructors to classes `meico.mei.Mei` and `meico.msm.Msm` that take the input code as Java String.
- Added `openjdk8`, `openjdk7`, and `oraclejdk9` to the TravisCI tests.


#### v0.2.16
- In preparation of further application modes for meico package `meico.app` has been restructured.
    - Entry point is class `meico.app.Main` method `main()`. The class also implements the commandline mode.
    - The window mode (GUI) is implemented in class `meico.app.MeicoApp`. Note the slight change from `MeiCoApp` to `MeicoApp`!
    - `MANIFEST.MF` was updated accordingly.


#### v0.2.15
- Added MP3 export of PCM encoded audio data.
    - Added [Java LAME](https://github.com/nwaldispuehl/java-lame) sources to package `meico.audio`. Meico is 3.98.4. License is LGPL 3.0.
    - Added new methods to class `meico.audio.Audio`:
        - `public byte[] encodePcmToMp3(byte[] pcm, AudioFormat format)`,
        - `public byte[] getAudioAsMp3()`,
        - `public void writeMp3()`, `public void writeMp3(String filename)`, and `public void writeMp3(File file)`.
    - Commandline mode has been extended accordingly. The new option is `-3` or `--mp3` to get audio export as MP3.
    - Window mode has been extended, too. Left click on the audio save button will output an MP3 file. Via right click, the user can specify an arbitrary filename. If the extension is `.mp3` or `.wav`, audio will be stored in the corresponding format. In all other cases, the output is a Wave file, by default, but with the specified extension.
- Method `meico.msm.Goto.toXML()` has been renamed to `toElement()` as it returns a XOM Element instance.


#### v0.2.14
- The generated ids in method `meico.mei.Helper.barline2SequencingCommand()` could start numerical which is not XML conform. This is fixed.
- Added methods to class `meico.msm.Msm`:
    - `public Document xslTransformToDocument(File xslt)`
    - `public String xslTransformToString(File xslt)`
- Updated Ant build script.


#### v0.2.13
- The XSLT-based new conversions from v0.2.12 were redone and generalized for several reasons.
    - Licensing for the stylesheets is unclear/undefined. So they were removed from this repository. They can be obtained from the official [MEI Encoding Tools GitHub](https://github.com/music-encoding/encoding-tools) page.
    - Packages `data`, `mods`, `marc`, and `mup` where removed from this repository including all contained classes.
    - The new export methods from v0.2.12 (see table) were replaced by two new, more generic methods in `meico.mei.Mei`: `public Document xslTransformToDocument(File xslt)` and `public String xslTransformToString(File xslt)`. This allows users to apply any XSLT stylesheets and obtain the result either as XOM Document instance or Java String. This should be a more flexible, less restrictive solution. For conversion of an Mei instance `myMei` to MusicXML, the user obtains the file `mei2musicxml.xsl` and calls `myMei.xsltTransformToDocument("path\\to\\mei2musicxml.xsl")`. This returns the resulting Document that can be processed further or written to the file system.
- New method `writeStringToFile(String string, String filename)` in `meico.mei.Helper`.


#### v0.2.12
- Bugfix in commandline mode.
- Reworked filename generation. New method `meico.mei.Helper.getFilenameWithoutExtension(String filename)`.
- Added Saxon v9.7.0.15 HE to the externals to process XSLT Stylesheets from the Music Encoding Initiative.
- Added further conversions. These are using the Music Encoding Initiative's XSLT stylesheets from the [MEI Encoding Tools GitHub](https://github.com/music-encoding/encoding-tools) page. However, they are a bit buggy sometimes ... and slow!

    | Conversion               | implemented in method                 | comment                                           |
    |--------------------------|---------------------------------------|---------------------------------------------------|
    | MEI to MusicXML          | `meico.mei.Mei.exportMusicXml()`      | buggy                                             |
    | MusicXML to MEI (v3.0.0) | `meico.data.MusicXml.exportMei()` | not functional, yet, because of XSLT syntax error |
    | MEI to MARC              | `meico.mei.Mei.exportMarc()`          | requires more testing                             |
    | MEI to MODS              | `meico.mei.Mei.exportMods()`          | requires more testing                             |
    | MEI to MUP (v1.0.3)      | `meico.mei.Mei.exportMup()`           | requires more testing                             |

- A series of new Classes has been added accordingly: `meico.data.MusicXml`, `meico.marc.Marc`, `meico.mods.Mods`, and `meico.mup.Mup`.
- Two new helper methods have been added to `meico.mei.Helper`:
    - `public static Document xslTransformToDocument(Document input, Document stylesheet)` and
    - `public static String xslTransformToString(Document input, Document stylesheet)`.
- These adds are not part of the window mode and meicoPy, yet, butt will be integrated in a future draw.


#### v0.2.11
- Bugfix in `meico.msm.Msm.resolveSequencingMaps()`.
- Added meicoPy, a Python demo script. This demonstrates the usage of meico within Python. It is a reimplementation of meico's command line mode in Python.


#### v0.2.10
- Some adds to the instruments dictionary.


#### v0.2.9
- When creating MSM `goto` elements from endings, the elements get a temporal attribute `n` (derives from the respective MEI `ending` element which also has an `n` attribute). In MSM this attribute is only required for the conversion and should not be present in the final output MSM. Method `Helper.msmCleanup()` has been extended accordingly.
- If an MEI `ending` has no attribute `n`, meico now checks for attribute `label` and takes this to search for numbering information. However, in case that both attributes are present, attribute `n` is preferred.


#### v0.2.8
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


#### v0.2.7
- In MEI, global (score-wise) and local (staff-wise and layer-wise) key signatures can be mixed. Rule of thumb is, the latest key signature before a `note` is the one that has to be considered. So far, meico ignored global data if there was a local entry once. This lead to some wrong results if global entries come after local (e.g., at the beginning it may be encoded in `staffDef` elements but later in `scoreDef` elements; see, for instance, `Hummel_Concerto_for_trumpet.mei` in the sample encodings). This issue is now fixed. If local and global key signature information deviate from each other meico trys to add global data to the local `keySignatureMap` in MPM where necessary. However, this is done ad hoc in method `Helper.computePitch()` in a very local context. Hence, it is not 100% perfect. This means, if the necessity for local copies occurs somewhere within the piece, past `keySignature` elements will be missing until this point.
- New method `Helper.addToMap()`. This is from now on used to insert new child elements into maps (sequential lists with elements that have an attribute `midi.date`) and ensure the timely order of the elements. All relevant methods in classes `Helper`and `Mei` have been adapted accordingly.


#### v0.2.6
- Slight enhancements of `Midi.play()` and `Midi.stop()`.
- Some code polishing in classes `meico.mei.Mei` and `meico.mei.Helper`.
- Better support of MEI `layer` elements during MEI-to-MSM conversion.
    - New `Helper` methods: `getLayer()`, `getLayerId()`, and `addLayerAttribute()`.
    - Affected methods have been revised. All MEI-to-MSM conversion methods that generate `note`, `rest`, `timeSignature`, `keySignature`, `transposition`, `dur.default`, and `oct.default` elements in MSM add a temporary layer attribute that is deleted during `Helper.msmCleanup()`.
    - Method `Mei.processRepeat()` considers layers, i.e., if a repetition takes place within a `layer` environment (e.g., beatRpt), then only those elements are repeated that belong to this layer.
    - Method `Mei.processStaffDef()` has been extended. So far, its child elements were ignored. Now, they are processed.
- Bugfix in `Helper.computePitch()`. Partly (not always) wrong conversion of accidental string to numeric value has been fixed.


#### v0.2.5
- Added method `Helper.midi2pname()`.
- Extended method `Helper.pname2midi()`.
- Changes to MSM accidental elements:
    - Renamed attribute `pitch` into `midi.pitch`.
    - Added attribute `pitchname` for better readability.
    - Both attributes can be used equivalently. Method `Helper.computePitch()` checks for both, preferring `midi.pitch` over `pitchname`.
- Removed files `MidiDataInputStream.java`, `MidiDataOutputStream.java`, `MidiFileReader.java`, `MidiFileWriter.java`, and `ExtendedMidiFileFormat.java` from package `meico.midi`. These were elements of the GNU Classpath project by Free Software Foundation. Since we dropped Java 6 compatibility these resources were no longer necessary. The corresponding methods `readMidi()` and `writeMidi()` have been redone.
- Added exit codes for errors in command line mode.
- Updated `README.md`.


#### v0.2.4
- Added audio playback methods `play()` and `stop()` to class `meico.audio.Audio`.
- Added audio playback button to the window mode graphical user interface.
    - Ensured that all playback is exclusive, i.e. starting one playback will stop any other currently running playback both for midi and audio.
    - Ensured that the playback of a Midi or audio instance stops when the instance is deleted or overridden.
- Java 7 conform code polishing.


#### v0.2.3
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


#### v0.2.2
- Fixed Midi-to-Audio conversion. Export of wave files works now.
- Updated `README.md`.


#### v0.2.1
- Fixed delay and "hiccup" in Midi playback when initializing and starting a sequencer of a `Midi` object for the first time.
- Added content to method `Audio.writeAudio(File file)` (so far it was empty).
- Added format data to class `Audio`. New getter methods have been added.
- In class `Audio` audio data are no longer represented as `AudioInputStream` but as byte array. Constructors have been adapted. Class `MeicoApp` has also been adapted in subclass `Audio4Gui`.
- Deactivated methods `Audio.writeAudio()` until byte-array-to-AudioInputStream conversion works properly.
- Fixed issues with the playback buttons in window mode.
- Updated `README.md`.


#### v0.2.0
- Added subpackage `graphics` to `resources` and moved all graphics resources into it.
- Added audio export in Wave format.
    - Added new command line options (`[-w]` or `[--wav]`) to trigger wave export in command line mode.
    - Modified `Midi.exportAudio()` (former `Midi.exportWav()`) to create and return an instance of `meico.audio.Audio`.
    - Added several methods to class `Audio`. Audio data is now represented in an `AudioInputStream` and can be written into a file.
    - Modified method `meico.midi.Midi2AudioRenderer`. Its rendering methods return an `AudioInputStream` object instead of directly writing a file into the file system.
    - Extended the window mode graphical user interface to include the new functionalities.
    - Updated `README.md`.
- Instead of using one global midi sequencer for Midi playback in class `meico.app.MeicoApp` (window mode) I switched to the built-in local sequencers in class `meico.midi.Midi`.
- Added tooltips in window mode for better user guidance.
- Introduced some layouting variables in class `meico.app.MeicoApp` for better editing of the window mode graphical user interface via global variables.


#### v0.1.4
- Added basic audio export to midi package (`meico.midi.Midi2AudioRenderer.java`).
- Added `UnsupportedSoundbankException.java` to package `meico.midi`.
- Added test audio output to command line mode.


#### v0.1.3
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
- Minor corrections in `MeicoApp.commandLineMode()`.


#### v0.1.2
- Bugfix in command line mode: missing path when writing `"-debug.mei"` and the file has been loaded via relative path.
- Added `S.`, `A.`, `T.`, `B.` to the instrument dictionary for ChoirOhs.
- Method `InstrumentsDictionary.getProgramChange()` outputs its string matching results to the command line or log file, resp.
- Missing `accid.ges` attribute processing in `Helper.computePitch()` added.


#### v0.1.1
- Renamed the `dur` attribute in MSM notes and rests into `midi.duration`.
- Further renamings: `date` into `midi.date`, `pitch` into `midi.pitch`, `channel.midi` into `midi.channel`, and `port.midi` into `midi.port`.
- Added `Bassus`, `Cantus`, `Singstimme`, `Singstimmen`, `Pianoforte`, `Trumpet in`, `Trompete in` to the instruments dictionary.
- Added a flag to the window mode constructor method `MeicoApp(String title, boolean makeLogFile)`. The redirection of console output into a log file is done only when `makeLogFile` is set true.
- Bugfixing in `Mei.processStaff()` and `Helper.getPart()`.
- `tie` elements are now processed (new method `Mei.resolveTieElements()`); they are resolved into `tie` attributes of `note` elements during the preprocessing. Hence, meico now supports the use of `tie`elements and is no longer restricted to `tie` attributes only. However, users should not mix `tie` and `slur` elements; the latter are not and will not be processed as ties!
- Method `Mei.resolveCopyOfs()` rewritten. It is not only faster now. It might happen (and does happen in the MEI sample library) that a placeholder element (the one with the `copyof` attribute) copies elements that again contain placeholders; it requires multiple runs to resolve this. The new implementation can handle circular referencing (cannot be resolved and would otherwise lead to infinite loops). Furthermore, if the placeholder element has an `xml:id` this id is no longer overwritten by the newly generated ids.
- Method `Mei.reorderElements()` (part of the MEI preprocessing) has been rewritter and is much faster now.


#### v0.1.0 (beta release)
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


#### v0.0.7
- Added flag to the mei-to-msm conversion to avoid the midi drum channel, added it to the window mode and command line options
	- `[-c]` or `[--dont-use-channel-10]`: the flag says whether channel 10 (midi drum channel) shall be used or not; it is already done at mei-to-msm convertion, because the msm should align with the midi file later on
- Changed id generation for copyOf resolution into a combined id: `source id + ":" + generated id`; hence, applications can now trace them back to the source element
- Minor bugfix of command line option `[--no--program-changes]` to `[--no-program-changes]`
- Minor ui corrections for window mode
- Adding new attributes, `date.midi` and `dur.midi`, to mei note and rest elements during conversion. This is only for debugging purpose and appears only in the `-debug.mei` file when running the command line mode with `--debug` flag.
- Also a `pnum` is added to the mei `note` elements in the debug version which holds the calculated midi pitch value.


#### v0.0.6
- Added `Canto`, `Quinto` and `Tenore` to the `VoiceOhs` in the instruments dictionary.
- In `MeicoApp`'s `commandLineMode()` a relative path can be used; the absolute path is derived automatically. Hence, users do not have to write down whole paths in the command line from now on.
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

#### v0.0.5
Bugs fixed in `Mei.java` that were introduced by rewriting `convert()` for Java 1.6 compatibility.

#### v0.0.4
Java 1.6+ compatibility
instrument dictionary extended (`church organ`: `upper`, `lower`, `pedal`)

#### v0.0.3
So far, msm part names were the `staffDef` labels. Now, they are created from an eventually existing parent `staffGrp` label and the `staffDef` label (`"staffGrp label" + " " + "staffDef label"`).

#### v0.0.2
Java 7+ compatibility

#### v0.0.1
first release (requires Java 8+)
