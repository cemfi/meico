# Meico: MEI to MusicXML Documentation

The following MEI elements are supported, i.e. processed by meico during MEI-to-MusicXML conversion. 
The set of supported elements will be extended in the future.

The notation of MusicXML elements is based on xquery syntax. The `//` selector is used to mark that the element might be at any location. More context is given in the surrounding text.
The root is never given since it may be either `score-partwise` or `score-timewise`.

Although both root options are processed meico currently converts every MusicXML to `score-partwise`.


The symbol "x **=>** y" is used as a shorthand for "value of MEI element x is mapped to MusicXML element y", as e.g in `rest` => `//note/rest`. If there is no value, it just refers to the creation of the empty element y.

The documentation is split into elements which occur in [**meiHead**](#mei-header-elements) and [**music**](#mei-music-elements). 
___

## MEI HEADER ELEMENTS

### actor
`actor` => `//credit`.

Element name ("actor") => `//credit[@type]`.

### address
Is not processed by itself, but children may contain information which contribute to composition of the `//identification/source` value string.

### addrLine
Address line is part of the `//identification/source` value string. 

### app
MEI's critical apparatus environment contains one or more alternative encodings/readings via elements `lem` and `rdg`. 
If `app` contains none of them it is ignored. The children of `lem` and `rdg` elements are processed only in the `app` environment. 
Meico interprets `lem` as the favored reading and will, hence, render the children of the first `lem` child of `app`. Later `lem` elements are ignored. If no `lem` is given meico chooses the first `rdg` for rendering.

### application
Contains information about the used technology to create the current encoding.
This may include the software which helped to create the encoding like note setting software (Sibelius, MuseScore, Dorico, etc.), automatic conversion software or xsl transformation scripts.
All these values are mapped to `//identification/encoding/software`.

### arranger
Both part of `//identification/creator` and `//credit` elements. The element name is mapped to the respective `@type` attribute.

### author
Both part of `//identification/creator` and `//credit` elements. The element name is mapped to the respective `@type` attribute.
If this Element has a [`persName` child](#persName), prioritize that.

### availability
Groups elements that describe the availability of and access to a bibliographic item, including an MEI-encoded document.
May be important information, that contributes to the understanding of the address, publication place or distributor.
A string representation of the MEI starting with the value this element is mapped to `//identification/miscellanous/miscellaneous-field`. 
The attribute`@name` contains the MEI path up to this element. 

### bloc
Contributes to the address part of `//identification/source` value string.

### change
Individual change within the revision description (revisionDesc).
A string representation of the MEI starting with the value this element is mapped to `//identification/miscellanous/miscellaneous-field`.
The attribute`@name` contains the MEI path up to this element.

### choice
Similar to `app`, the `choice` element defines one or more alternative readings. The elements, i.e. subtrees, of which meico chooses one to render are (in order of priority) `corr`, `reg`, `expan`, `subst`, `choice`, `orig`, `unclear`, `sic`, and `abbr`. In case of another `choice` it is processed recursively. If none of these can be found as child of `choice` meico chooses the first child whatever type it is. The `cert` attribute, i.e. certainty rating, is not taken into account so far.

### corpName
Organization/ Group is part of the `//identification/source` value string.

### country
Contributes to the address part of `//identification/source` value string.

### date
Date is part of the `//identification/source` value string. Date may also be coded in attribute `@isodate` (prioritize).

### distributor
Is not processed by itself, but children may contain information which contribute to composition of the `//identification/source` value string.

### district
Contributes to the address part of `//identification/source` value string.

### editor
Both part of `//identification/creator` and `//credit` elements. The element name is mapped to the respective `@type` attribute.

### funder
Both part of `//identification/creator` and `//credit` elements. The element name is mapped to the respective `@type` attribute.

### geogFeat
Contributes to the address part of `//identification/source` value string.

### geogName
Contributes to the address part of `//identification/source` value string.

### librettist
Both part of `//identification/creator` and `//credit` elements. The element name is mapped to the respective `@type` attribute.

### lyricist
Both part of `//identification/creator` and `//credit` elements. The element name is mapped to the respective `@type` attribute.
If this Element has a [`persName` child](#persName), prioritize that.

### manifestation 
The Value of is mapped to `//identification/miscellanous/miscellaneous-field`. The attribute`@name` contains the MEI path up to this element.

It may also contain information about layout (in any `layout` element in a descendant):
- first occurrence of `height` => `//defaults/page-layout/height`
- first occurrence of `width` => `//defaults/page-layout/width`

### p
May contain arbitrary information which cannot be mapped to any MusicXML element.
A string representation of the MEI starting with the value this element is mapped to `//identification/miscellanous/miscellaneous-field`.
The attribute`@name` contains the MEI path up to this element.

### persName
- As a child of `author`, `composer` or `lyricist` the value is part of `//identification/creator` and `//credit` elements.
This child is only mandatory for `composer`.
Attribute `@role` is mapped to `@type`in MusixXML. 

- As a child of `encoder` the value is mapped to `//identification/encoding/encoder`.

### pgHead
Might contain Information about the title and the composer in attribute `rend[@type]` elements.
`@type='composer'` is processed like [`persName` element](#persname). `@type='title'` is processed like [`title` element](#title).

### postBox
Contributes to the address part of `//identification/source` value string.
 
### postCode
Contributes to the address part of `//identification/source` value string.

### publisher
Publisher is part of `//identification/source` value string.

### pubPlace
Publication place not processed by itself, but children may contain information which contribute to composition of the `//identification/source` value string.

### region
Contributes to the address part of `//identification/source` value string.

### settlement
Contributes to the address part of `//identification/source` value string.

### sponsor
Both part of `//identification/creator` and `//credit` elements. The element name is mapped to the respective `@type` attribute.

### street
Contributes to the address part of `//identification/source` value string.

### title
The `title` may contain several different vallues for the attribute `@type` which contribute to different parts of the title on MusicXML.
 
#### @type
 - `uniform` Collective title. Will be used as the complete title in `//work/work-title`.
 - `main` Main title. Will be set the main title `//work/work-title`. If there is already a main title present, add this to the main title value (as kind of a subtitle).
 - `subordinate` May include work numbers which include prefixes like "nr.", "no", "op" etc. and is added to `//work/work-number`. Otherwise any arbitrary string is processed like `@type='main'`.
- `alternative` => `//movement-title`.
- `number` => `//work/work-number`.

### titlePart
Is processed the same way as [`title` element](#title).

### unpub
Indicate that the bibliographic resource is unpublished.
Add the value _Unpublished_ to `//identification/miscellanous/miscellaneous-field`. The attribute`@name` contains the MEI path up to this element.

### work
As children of `//workList` or `//workList/work/componentList` each work determines one separate MusicXML to be exported. This might be e.g. individual movements. 
Each works gets the same header that was processed so far and gets enriched with information from further elements in `music`.

___
## MEI MUSIC ELEMENTS
### accid 
`accid` => `//note/accidental`.

`accid` => `//note/pitch/alter`.

- `//note/accidental`: sign of the accidental to be displayed
- `//note/pitch/alter`: actual pitch alteration of the note to be played (in semitones)

| MEI accidental | Alter interval in MusicXML     | Accidental value in MusicXML                             |
|----------------|--------------------------------|----------------------------------------------------------|
|   `s` | 1 (= one semitone up)          | _sharp_                                                  |
|   `f` | -1 (= one semitone down)       | _flat_                                                   |
|  `ss` | 2 (= one whole tone up)        | _sharp-sharp_                                            |
|   `x` | 2 (= one whole tone down)      | _double-sharp_                                           |
|  `ff` | -2                             | _flat-flat_                                              |
|  `xs` | 3                              | _triple-sharp_ (is used in lack of analogue in MusicXML) |
|  `ts` | 3                              | _triple-sharp_                                           |
|  `tf` | -3                             | _triple-flat_                                            |
|   `n` | 0                              | _natural_                                                |
|  `nf` | -1                             | _natural-flat_                                           |
|  `ns` | 1                              | _natural-sharp_                                          |
|  `su` | 1.5                            | _sharp-up_                                               |
|  `sd` | 0.5 (= one quarter tone up)    | _sharp-down_                                             |
|  `fu` | -0.5 (= one quarter tone down) | _flat-up_                                                |
|  `fd` | -1.5                           | _flat-down_                                              |
|  `nu` | 0.5                            | _natural-up_                                             |
|  `nd` | -0.5                           | _natural-down_                                           |
| `1qf` | -0.5                           | _quarter-flat_                                           |
| `3qf` | -1.5                           | _three-quarters-flat_                                    |
| `1qs` | 0.5                            | _quarter-sharp_                                          |
| `3qs` | 1.5                            | _three-quarters-sharp_                                   |


#### @accid.ges
If this attribute is present, neglect the mapping to `//note/accidental` 
since the element just indicates if a accidental has to be printed or not. 
As a fallback, the value of this attribute may used as an alternative for the actual value of the `accid` element, if it is not present.

### barline

#### @rptstart, @rptend, @rptboth
Repetitions are mapped to `//barline/repeat` with their respective direction. 
`@rptboth` is resolved as two separate `//barline/repeat` elements at the same position with each forward and backward directions.
The position (whether to be printed on the left or right boundary of the measure) is determined by `//barline[@location]`. 
It depends on the attributes `@left` and `@right` of the corresponding MEI `measure` element.

### bTrem
All child elements which correspond to printed symbols (`note`, `chord`) will occur in the MusicXML.
Tremolo symbols within the note are currently missing.

Processing the MusicXML `//note` element the chord might yield duration attributes such as `@dur` and `@dots`.
If the `note` child of `bTrem` will have a duration, prioritize that.

### chord
Processing the MusicXML `//note` element the chord might yield duration attributes such as `@dur` and `@dots`.
If the `note` child of `chord` will have a duration, prioritize that.

Elements belonging to a chord are marked by the occurrence of the `//note/chord` element in all subsequent notes.
Therefore the first note of the chord lacks the `//chord` element.
A two note chord might look like this in MusicXML:
```xml
<note>
    <pitch>
        <step>E</step>
        <octave>4</octave>
    </pitch>
    <duration>2</duration>
    <type>eighth</type>
</note>
<note>
    <chord/>
    <pitch>
        <step>E</step>
        <octave>5</octave>
    </pitch>
    <duration>2</duration>
    <type>eighth</type>
</note>
```

### clef
A `clef` in a `staffDef` always corresponds as a `//measure/attributes/clef` just before the first `//measure/note`. This puts the clef at the beginning of a measure (left to the left bar line).

As any descendant element within a `layer` element a clef can be printed at corresponding position and mapped to a `//measure/attributes/clef` element. 
This type always has higher priority than a `clef` in a `staffDef` and may overwrite its set values, if there is conflicting information.

#### @shape
`clef[@shape]` => `//clef/sign`.

#### @line
`clef[@line]` => `//clef/line`.

#### @dis, @dis.place

| `@dis` | `@dis.place` | `//clef/clef-octave-change`|
|--------|--------------|---- |
| 8      | _above_      | 1    |
| 8      | _below_      | -1 |
| 15     | _above_      | 2 |
| 15     | _below_      | -2 |

### dot
The value of a `dot` element corresponds to the number of `//note/dot` elements,
i.e. a value of "2" in `dot` results in two separate `//dot` elements within a `//note` in MusicXML.

May occur as a child of `note` and `rest`.

### fTrem
All child elements which correspond to printed symbols (`note`, `chord`) will occur in the MusicXML.
Tremolo symbols within the note are currently missing.

Processing the MusicXML `//note` element the chord might yield duration attributes such as `@dur` and `@dots`.
If the `note` child of `bTrem` will have a duration, prioritize that.

### keySig
A `keySig` always corresponds to a `//measure/attributes/key` at the position within the `layer`.
Occurrences in `staffDef` or as a descendant of `layer` are processed the same. This puts the key at the beginning of a measure (left to the left bar line or beginning of first measure).

The closer `keySig`is to `layer` the higher the priority.

#### @sig
The number on the first position determines the number of sharps/flats and is mapped to `//key/fifths`.
Sharps are coded as "s" and flats as "f". Only flats alter the fifths to "-". For example:

"3f" => `//key/fifths` = -3 

"2s" => `//key/fifths` = 2

### layer
This element is closely linked to the determination of `//voice` in `//part`. The transition to a set of `//note` with a new voice value is marked by a `//measure/backup` element.
That uses the accumulated durations of the previous `//note` elements to determine its `//duration`.

When processing a layer meico always checks if `//measure/attributes` will be still the same.
For this the given MEI is traversed upwards from the given layer and looks for [`staffDef` elements](#staffDef) and [`scoreDef` elements](#scoreDef) which might contribute to the attributes.
The current `//measure` then just uses the differences to the attributes of the previous measure (on the same layer level). In general: definitions closer to the given layer have higher priority.

#### @n
Determines the value of `//note/voice`. If no `@n` is present the index position + 1 within the parent element (`staff`) will be used instead. 

### mdiv
Each (inner) mdiv represents a new MusicXML file. Whether it will be `score-partwise>` or `score-timewise` is determined by descendant elements. (See [`score`](#score) or [`parts`](#parts)).

#### @n
`mdiv[@n]` => `//movement-number`.

### measure
If `measure` is child of a `score`element, it will be processed as part of as `score-timewise` MusicXML.

If `measure` is child of a `part`element, it will be processed as part of as `score-partwise` MusicXML. 

#### @left
Is used to determine `@placement` of the [`//barline/repeat` symbol](#rptstart-rptend-rptboth).

#### @n
Determines the value of `//measure[@number]`. If no `@n` is present the index position + 1 within the parent element (`part` or `score`) will be used instead.

#### @right
Is used to determine `@placement` of the [`//barline/repeat` symbol](#rptstart-rptend-rptboth).


### meterSig
A `meterSig` always corresponds to a `//measure/attributes/time` at the beginning of a `//measure`.  
There are no new times in the middle of a measure.

#### @count, @unit
If `@sym` is not present, both attributes have to be present to process `meterSig`. 
- `@count` => `//time/beats`
- `@unit` => `//time/beat-type`

#### @sym
Has first priority to be mapped. `@count` and `@unit` do not have to be present. 
The value of the attribute is directly mapped to `//time//symbol`.

### mRest
Place a `//note/rest` element into the MusicXML and `//note/rest[@measure='yes']`. 
Additionally `//note/duration` is set based on the current meter signature information. `//note` always require `//duration, so
if there is none (e.g. meter and unit are missing for some reason), insert the value of `//divisions` (= quarter note).

### mSpace
Processed same as [`mRest`](#mrest) but with `//note[@print-object='no']`.

### note

`note` => `//note`

#### @accid, @accid.ges
Use this attribute to determine `//note/accidental` and `//note/pitch/alter`. (See mapping table in [`accid` element](#accid-)).
If `@accid` is not set, look for `@accid.ges` as a fallback.

#### @dots
Can only be processed in combination with `@dur`. Each dot adds half of the duration of the previous dot.

E.g. duration = 4; 1 dot = 4 + 4/2; 2 dots = 4 + 4/2 + 4/2/2, etc.

Also add multiple `//note/dot`, based on the number of this attribute.

#### @dur
Prioritize this attribute. If `note` has no `@dur` look for it in the closest wrapping element (such as `chord`, `bTrem`, `fTrem`, etc.).

The `//note/duration` and `//note/type` is computed based on the first (and possibly only) `//attributes/division` which represents the divisions of a quarter note.
For the `@dur` value has to be converted into units of divisions first. MuseScore for example always checks, if the type and the duration match, so a duration of 0 always results 
in an error and the note cannot be created.

The values "breve", "long" and "maxima" are processed as fractions of whole notes: When whole note = 1, then "breve" = 0.5, "long" = 0.25, "maxima" = 0.125.

#### @pname, @pname.ges
Is directly mapped to `//note/pitch/step` (in upper case). 
If `@pname` is not set, look for `@pname.ges` as a fallback.

#### @oct, @oct.ges
Is directly mapped to `//note/pitch/octave` (in upper case).
If `@oct` is not set, look for `@oct.ges` as a fallback.

#### @pnum
Last fallback if no accididental, pitch and octave can be determined.
Represents the MIDI number of the note. Will be use flat accidentals as a default with no information.

#### @stem.dir
Value is directly mapped to `//note/stem` ("up" or "down") if present.

#### @tie

Values may be "i" (initial), "m" (medium) or "t" (terminal). "i" is processed the same way as [`tie[@startid]` attribute](#startid). "t" is processed the same way as [`tie[@endid]` attribute](#endid).
"m" is a mixture of both.

The attribute has a lower priority than the actual element, if there is conflicting information.

#### @tuplet 
May have a list of indexed "i" and "t", e.g. `note[@tie="i1 i2 t1]`.

For each  "i" => `//note/notations/tuplet[@type='start']`

For each matching "t" => `//note/notation/tuplet[@type='stop']`

`//note/time-modification/actual-notes` is set by the number of the elements between corresponding "i" an "t". `//note/time-modification/normal-notes` is set by the next smaller exponential of 2 of `//note/time-modification/actual-notes`. 

E.g. `//actual-notes : //normal-notes`: 3:2, 4:4, 5:4, 6:4, 7:4, 8:8, 9:8, etc.

The attribute has a lower priority than the `tuplet` parent, if there is conflicting information.


### rest
`rest` => `//note/rest`

#### @dots
Can only be processed in combination with `@dur`. Each dot adds half of the duration of the previous dot.

E.g. duration = 4; 1 dot = 4 + 4/2; 2 dots = 4 + 4/2 + 4/2/2, etc.

Also add multiple `//note/dot`, based on the number of this attribute.

#### @dur
The `//note/duration` and `//note/type` is computed based on the first (and possibly only) `//attributes/division` which represents the divisions of a quarter note.
For the `@dur` value has to be converted into units of divisions first. MuseScore for example always checks, if the type and the duration match, so a duration of 0 always results
in an error and the note cannot be created.

The values "breve", "long" and "maxima" are processed as fractions of whole notes: When whole note = 1, then "breve" = 0.5, "long" = 0.25, "maxima" = 0.125.

Opposite to `note` this element cannot have a predecessor with `@dur`.

### section
In meico element is only internally processed when creating a `score-partwise` document to help keep track of the current parts. 

### scoreDef
Top definition of the score. May yield information about clef, key and meter as well as layout.
Information on the top level of is always overwritten by information closer to a given note, layer, staff.

When processing steps are defined as follows:
```
1. Find definitions in the current layer:
   - clef
   - keySig
   - meterSig

2. Find closest definitions in the parent staff in the following order:
   - layerDef
   - staffDef

3. Find closest definitions in the parent measure in the following order:
   - staffDef
   - scoreDef
        - staffGrp: : loop step 3. until no definitions are found

4. Find closest definitions in the parent (part or score) in the following order:
   - staffDef
   - scoreDef
        - staffGrp: loop step 4. until no definitions are found
 

5. For the current //measure/staff/layer, process steps 1 - 4.

6. For the previous //measure/staff/layer, process steps 1 - 4.

7. Compare both sets of definitions to find differences in the current definitions.

8. if there are differences: write them in new //measure/attributes (in MusicXML).


```
#### <u>**Shared Attributes**</u>
#### @clef.dis
Processed the same way as [`clef[@dis]` attribute](#dis-displace).

#### @clef.dis.place
Processed the same way as [`clef[@dis.place]` attribute](#dis-displace).

#### @clef.line
Processed the same way as [`clef[@line]` attribute](#line).

#### @clef.shape
Processed the same way as [`clef[@shape]` attribute](#shape).

#### @dur.default
Acts as a default for durations. If no duration can be found for a element that usually has `@dur` (`note`, `chord`, `bTrem`, `fTrem`, etc.), take this value.

#### @keysig
Processed the same way as [`keySig[@sig]` attribute](#sig).

#### @key.sig 
The same as `@keysig`, but guarantees compatibility with previous MEI Versions (< 5.0).

#### @meter.count
Processed the same way as [`meterSig[@count]` attribute](#count-unit).

#### @meter.sym
Processed the same way as [`meterSig[@sym]` attribute](#sym).

#### @meter.unit
Processed the same way as [`meterSig[@unit]` attribute](#count-unit).

#### @num.default
Along with `@numbase.default`, describes the default duration as a ratio. num.default is the first value in the ratio.

Use this value if no duration value can be found.

#### @numbase.default
Along with `@num.default`, describes the default duration as a ratio. numbase.default is the second value in the ratio.

Use this value if no duration value can be found.

#### @oct.default
Acts as a default for octave. If no octave can be found for a element that usually has `@oct` (`note`, `chord`, `bTrem`, `fTrem`, etc.), take this value.

#### @ppq
Pulses per quarter. 

`section[@ppq]` => `//measure/attributes/divisions`.

<br/>

#### <u>**Unique Attributes**</u>

The following attributes are used to determine default layout information in `//defaults` descendants.

#### @lyric.name
`scoreDef[@lyric.name]` => `//lyric-font`

#### @music.name
`scoreDef[@music.name]` => `//music-font`

#### @page.botmar
`scoreDef[@page.botmar]` => `//page-layout/bottom-margin`

#### @page.height
`scoreDef[@page.height]` => `//page-layout/page-height`

#### @page.leftmar
`scoreDef[@page.leftmar]` => `//page-layout/left-margin`

#### @page.rightmar
`scoreDef[@page.rightmar]` => `//page-layout/right-margin`

#### @page.topmar
`scoreDef[@page.topmar]` => `//page-layout/top-margin`

#### @page.width
`scoreDef[@page.width]` => `//page-layout/page-width`

#### @spacing.system
`scoreDef[@spacing.system]` => `//system-layout/system-distance`

#### @spacing.staff
`scoreDef[@spacing.staff]` => `//staff-layout/staff-distance`

#### @system.leftmar
`scoreDef[@system.leftmar]` => `//system-layout/system-margins/left-margin`

#### @system.rightmar
`scoreDef[@system.rightmar]` => `//system-layout/system-margins/right-margin`

#### @system.topmar
`scoreDef[@system.topmar]` => `//system-layout/top-system-distance`

#### @text.name
`scoreDef[@text.name]` => `//word-font`

### space
Is processed the same way as [`rest` element](#rest) but with `//note[@print-object='no']`. Cannot be processed when it has no duration information.
### staffDef
This element is used to create the the `//part-list`. For each `staffDef` a new `score-part` and `part` is created in order.

Child `label` => `//score-part/part-name`.

Shared attributes are processed the same as in [`scoreDef` element](#uunique-attributesu).

### staffGrp
`staffGrp` => `//part-list/part-group`

This element is processed as a part of [`scoreDef` element](#scoreDef). 

May contain `staffDef` or nested `staffGrp` elements.

### tie
Ties are processed as part of the note processing. Ties may occur as `tie` elements as a child of `measure`. The element has a higher priority than the corresponding `note[@tie]`

#### @curvedir
`tie[@curvedir='above']` => `//note/notations/tied/orientation=over` and `//note/notations/tied/placement=above`

`tie[@curvedir='below']` => `//note/notations/tied/orientation=under` and `//note/notations/tied/placement=below`

#### @endid
`tie[@endid]` => `//note/tie[@type='stop']` and `//note/notations/tied[@type='stop']`

#### @startid
`tie[@startid]` => `//note/tie[@type='start']` and `//note/notations/tied[@type='start']`

### tuplet
This element has multiple `note` elements as children. Only the first and the last `note` will get `//note/time-modification` and `//note/notations/tuplet`. All tuplets will get brackets by default.

- first note => `//note/notations/tuplet[@type='start']`

- last note => `//note/notation/tuplet[@type='stop']`

#### @num
This attribute is required to correctly compute the `//time-modifications`.

`tuplet[@num]` => `//note/time-modification/actual-notes`

#### @numbase
`tuplet[@numbase]` => `//note/time-modification/normal-notes`

If no numbase is set compute `//note/time-modification/actual-notes` from `@num`. `//note/time-modification/normal-notes` is set by the next smaller exponential of 2 of `//note/time-modification/actual-notes`.

E.g. `//actual-notes : //normal-notes`: 3:2, 4:4, 5:4, 6:4, 7:4, 8:8, 9:8, etc.