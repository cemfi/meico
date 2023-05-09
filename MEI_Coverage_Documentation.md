# Meico: MEI Coverage Documentation

The following MEI elements are supported, i.e. processed by meico during MEI-to-MSM/MPM conversion. The set of supported elements will be extended in the future.


#### abbr
This element is processed only as child of `choice`.

#### accid
Attributes `ploc` and `oloc` are mandatory to determine the pitch to be affected by the accidental. These attributes can be missing if the `accid` is child of a `note` element that provides the `pname` and `oct` attribute instead. Otherwise, the `accid` is ignored. The effect of the accidental is read from attribute `accid.ges` (prioritized) or `accid`. If none is given the element is ignored, too. Meico will take the accidental into account until the end of the measure. Previous accidentals on the same pitch are overridden, only the last accidental on that pitch is applied to succeeding notes. Accidentals apply to all notes throughout all layers in the same staff as the `accid` element. Meico renders the following accidentals to semitone intervals in MSM:

| MEI accidental | Semitone interval in MSM |
|----------------|--------------------------|
|   `s` |    1 (= one semitone up)     |
|   `f` |   -1 (= one semitone down)   |
|  `ss` |    2 (= one whole tone up)   |
|   `x` |    2 (= one whole tone down) |
|  `ff` |   -2 |
|  `xs` |    3 |
|  `ts` |    3 |
|  `tf` |   -3 |
|   `n` |    0 |
|  `nf` |   -1 |
|  `ns` |    1 |
|  `su` |  1.5 |
|  `sd` |  0.5 (= one quarter tone up) |
|  `fu` | -0.5 (= one quarter tone down)|
|  `fd` | -1.5 |
|  `nu` |  0.5 |
|  `nd` | -0.5 |
| `1qf` | -0.5 |
| `3qf` | -1.5 |
| `1qs` |  0.5 |
| `3qs` |  1.5 |

While quarter tone intervals and pitches are numerically well represented in MSM they will get lost, i.e. rounded to semitone intervals, during Midi export.

#### add
There is no special processing routine for this element. Meico just processes its children.

#### app
MEI's critical apparatus environment contains one or more alternative encodings/readings via elements `lem` and `rdg`. If `app` contains none of them it is ignored. The children of `lem` and `rdg` elements are processed only in the `app` environment. Meico interprets `lem` as the favored reading and will, hence, render the children of the first `lem` child of `app`. Later `lem` elements are ignored. If no `lem` is given meico chooses the first `rdg` for rendering.

#### arpeg
Arpeggios encoded via MEI element `arpeg` are converted to MPM `ornament` elements and `ornamentDef` elements. Meico's default arpeggio is defined rather neutral, so it works in as many musical situations as possible. It's `ornamentDef` might be further edited and finetuned to better suit a particular style and musical context. 
```xml
<ornamentDef name="arpeggio">
    <dynamicsGradient transition.from="-1.0" transition.to="1.0"/>
    <temporalSpread frame.start="-22.0" frame.end="22.0"/>
</ornamentDef>
...
<ornament date="..." name.ref="arpeggio" scale="0.0" note.order="..."/>
```

If the `arpeg` has an `xml:id` it is also used for the MPM `ornament` element. 

Attributes `part` (priority) and `staff` can be used to associate the instruction with one or more `staff` elements, i.e. MSM `part` elements. If these attributes are omitted the instruction is treated as global, i.e. relevant to all musical parts. In attribute `part` - and in contrast to the MEI specification - the following values are supported: `%all` and space separated staff numbers. Attribute `layer` is not supported and meico will not search for the staffs that contain the specified layers. If `layer` is used in the MEI encoding `staff` should also be specified so the arpeggio instruction is assigned to the correct musical part at least. To avoid confusion between local (part specific) and global (all parts) arpeggios we recommend defining them generally global (`part="%all"`) and use plist to specify the notes and chords involved.

Mandatory for the interpretation of the `arpeg` is information about its association to MEI `note` or `chord` elements or, alternatively, information about its temporal location in the music. The easiest way is to provide the `note`/`chord` IDs via attribute `plist`. The temporal location can be specified via attributes `tstamp.ges`, `tstamp`, `startid`. If `plist` is given, the other attributes are not necessary and will be ignored by meico.

Attribute `order` is optional in MEI. If specified, meico will interpret value `"up"` as an arpeggio with ascending pitch, `"down"` as an arpeggio with descending pitch and `"nonarp"` will cause meico to do nothing. If attribute `order` is not given, meico will realize the arpeggio in the sequence specified in attribute `"plist"`. If neither `order` nor `plist` are given, meico will assume `order="up"` as default and apply it to all notes at the part and time position of the `arpeg`.

#### artic
Meico converts MEI `artic` elements to MPM `articulation` elements which are then associated with the notes to be articulated. If these `note` elements have no `xml:id` meico generates it. One of the attributes `artic` and `artic.ges` must be present for an articulation to be meaningful, the latter is prioritized. Meico supports any articulation denominator in these attributes, not only those allowed in the MEI specification. These are added to an MPM articulation `styleDef` where the user can specify the modifiers that the articulation applies to a note if the default articulation definitions do not satisfy. Meico undestands the following default articulations.

| Descriptor                         | Modifyers                                                           |
|------------------------------------|---------------------------------------------------------------------|
| `accent`, `acc`                    | absoluteVelocityChange = 25.0                                       |
| `breath`, `cesura`, `caesura`      | absoluteDurationChangeMs = -400.0 <br> absoluteVelocityChange= -5.0 |
| `down bow`, `dnbow`                | no changes                                                          |
| `legatissimo`                      | absoluteDurationChangeMs = 250.0                                    |
| `legato`, `leg`                    | relativeDuration = 1.0                                              |
| `legatostop` (the end of a `slur`) | relativeDuration = 0.8 <br> relativeVelocity = 0.7                  |
| `marcato`, `marc`                  | relativeDuration = 0.8 <br> absoluteVelocityChange = 25.0           |
| `nonlegato`                        | relativeDuration = 0.95                                             |
| `pizzicato`, `pizz`, <br> `left-hand pizzicato`, `lhpizz` | absoluteDuration = 1.0                       |
| `portato`, `port`                  | relativeDuration = 0.8                                              |
| `sf`, `sfz`, `fz`, `sforzato`      | absoluteVelocity = 127.0 <br> relativeDuration = 0.8                |
| `snap`, `snap pizzicato`           | absoluteDuration = 1.0 <br> absoluteVelocityChange = 25.0           |
| `spiccato`, `spicc`                | absoluteDurationMs = 140.0 <br> absoluteVelocityChange = 25         |
| `staccato`, `stacc`                | absoluteDurationMs = 160.0 <br> absoluteVelocityChange = -5.0       |
| `staccatissimo`, `stacciss`        | absoluteDurationMs = 140.0 <br> absoluteVelocityChange = 5.0        |
| `standardArticulation`             | absoluteDurationChange = -70.0                                      |
| `tenuto`, `ten`                    | relativeDuration = 0.9 <br> absoluteVelocityChange = 12.0           |
| `up bow`, `upbow`                  | no changes                                                          |

This list can be subject to future changes! Combined articulations such as `"ten stacc"` are also supported and deconstructed into the individual MPM representatives. However, the result is that the modifiers of all articulations are applied to the note in the defined order. In case of `"ten stac"` this will not produce a portato! If you mean portato, then use the according denominator and specify its rendition seperately in MEI. Or introduce a new denominator `"myPortato"` and set its modifiers in the MPM `articulationDef` before generating expressive MIDI. 

Articulation `standardArticulation` is initially used as default articulation, i.e. the articulation that is applied to all notes with no other, specific articulation.

`artic` elements that are children of `note` elements are associated only to these notes. However, `artic` elements that are direct children of `chord` elements are applied to all `note` elements within this `chord` as far as these do not specify their own more local articulation.

#### beam
There is no special processing routine for this element. Meico just processes its children.

#### beatRpt
This repeats the musical contents of the last beat before this element. The length of one beat is taken from the underlying time signature's denominator or a quarter beat if no time signature is given.

#### breath
MEI's `breath` elements are converted to MPM `articulation` elements as their effect is to shorten preceding notes by the amount of time needed to breathe. Just as every articulation it should be associated with one or more MEI `note` or `chord` elements using one of the attributes `prev`, `follows` or `startid`. Be aware that, if neither `prev` nor `follows` is specified, `startid` should refer to the preceding(!) `note` or `chord`. If any of the prior two attributes is given `startid` will be ignored by meico and may, thus, refer to anything else. If none of the attributes is specified (not recommended!), `tstamp.ges` or `tstamp` can be used to associate the breath with a musical position and attribute `staff`/`part` to associate it with a staff. However, if it does not coincide with a `note` position it will have no effect on the music. If the `breath` has an `xml:id` the MPM encoding will use the same ID.

#### bTrem
At the moment, tremoli are not resolved into sequences of notes but interpreted as chords. This is preliminary until we address ornamentations in the further development.

#### choice
Similar to `app`, the `choice` element defines one or more alternative readings. The elements, i.e. subtrees, of which meico chooses one to render are (in order of priority) `corr`, `reg`, `expan`, `subst`, `choice`, `orig`, `unclear`, `sic`, and `abbr`. In case of another `choice` it is processed recursively. If none of these can be found as child of `choice` meico chooses the first child whatever type it is. The `cert` attribute, i.e. certainty rating, is not taken into account so far.

#### chord
If the `chord` is child of another `chord` meico will look for its duration data to be used if this inner chord does not define its own duration. If no duration is given from the parent of this `chord` meico will search the child elements for the longest duration and assign it to the `chord`. 

If the `chord` specifies attribute `artic.ges` or `artic` it is applied to all its child `note` elements, except for those that define their own articulations more locally (`artic.ges` is prioritized). Meico supports any articulation denominator in these attributes, not only those allowed in the MEI specification.

Chords with a `grace` attribute are ignored at the moment. These will be subject to the further development when ornamentations are addressed.

#### @copyof
Many MEI elements do also offer an attribute called `copyof`. It can be used for a "slacker" encoding: Whenever an element is similar to another one it sufficed to write it out only once and then refer to that one later on. Meico resolves these "copyof elements" during preprocessing. Of course, this requires the reference id to be actually existent. MEI does even allow consecutive and recursive copyof references, i.e. the copyof refers to another copyof element or an element that containes further copyof elements. Meico can resolve these, too, and it detects circular cases that cannot be resolved because there is no initial element to copy. Resolving a copyof element means that meico will replace it with a deep copy (including all subtrees) of the referred element. During the resolution of copyofs `xml:id` attributes will get duplicated; meico will concatenate it with a newly generated UUID and ensure that each ID ocurs only once.

#### corr
A correction, can occur as "standalone" or in the `choice` environment, usually paired with the `sic` element. In both cases, meico processes its children.

#### del
The `del` element contains information deleted, marked as deleted, or otherwise indicated as superfluous or spurious. Hence, by default, meico omits its content. The deletion is processed only if it is child of a `restore` element as this negates all deletions throughout its whole subtree.

#### dot
This element is proccessed as child of a `note` or `rest` for the computation of durations. Outside of such an environment the meaning of the `dot` is unclear and meico cannot interpret it in a musically meanignful way.

#### dynam
Literal dynamics instructions are of two different types, instantaneous (p, mf, ff, ...) or continuous (cresc., decresc., dim., ...). Meico will generate a continuous MPM dynamics instruction if the MEI `dynam` contains one of the three substrings `"cresc"`, `"decresc"`, `"dim"`. In every other case meico will generate an instantaneous instruction in MPM. The initial volume of a continuous dynamcis transition is taken from the previous instruction. The string of every instantateous instruction will also be added in MPM to a global dynamics style definition, where the user/application can set or edit the associated numeric value.If the `dynam` element does not specify a descriptor string as value, i.e. inner text, meico will try to use the text of attribute `label` instead, if that exists. Meico's parser understands the following descriptor strings and generates default values:

| dynamics instruction (non-case sensitive) | numeric value  (MIDI velocity) |
|-------------------------------------------|--------------------------------|
| pppp, pianissimopianissimo                | 5.0                            |
| ppp, pianopianissimo                      | 12.0                           |
| pp, pianissimo                            | 36.0                           |
| p, piano                                  | 48.0                           |
| mp, mezzopiano                            | 64.0                           |
| mf, mezzoforte                            | 83.0                           |
| f, forte                                  | 97.0                           |
| ff, fortissimo                            | 111.0                          |
| fff, fortefortissimo                      | 120.0                          |
| ffff, fortissimofortissimo                | 125.0                          |
| sf, sforzato                              | 127.0                          |
| ...dim..., ...decresc...                  |                                |
| ...cresc...                               |                                |
| any other descriptor                      | 74.0                           |

Depending on the soundfont/synthesizer these values may be more or less appropriate. The users can edit these values in the MPM dynamics style definition and add further descriptor-value mappings. If a continuous instruction or `hairpin` is followed by another continuous instruction or `hairpin` or no further instruction at all, the in-between values are unclear and shall be edited by the user in the corresponding MPM `dynamicsMap`.

If the `dynam` has an `xml:id` it is also applied in the MPM encoding. Attributes `part` (priority) and `staff` can be used to associate the instruction with one or more `staff` elements, i.e. MSM `part` elements. If these attributes are omitted the instruction is treated as global, i.e. relevant to all musical parts. In attribute `part` - and in contrast to the MEI specification - the following values are supported: `%all` and space separated staff numbers. Attribute `layer` is not supported and meico will not search for the staffs that contain the specified layers. If `layer` is used in the MEI encoding `staff` should also be specified so the dynamics instruction is assigned to the correct musical part at least. 

Mandatory for the interpretation of the `dynam` is information about its temporal location in the music. The easiest way is placing it within a `verse` element that itself is a child of a note, so the performance instruction gets the same timing position. Alternatively, attributes `tstamp.ges`, `tstamp`, `startid` or `plist` can be used. For continuous dynamics the attributes `dur`, `tstamp2.ges`, `tstamp2` and `endid` are supported (in this exact priority, i.e. `endid` is only used if none of the other three is given). The `endid` can also be the id of the subsequent `dynam` element. Continuous dynamics transitions without one of the previous four attributes end at the subsequent instruction. Our recommendation: If there is a dynamics instruction anyway the attributes should not be used at all to avoid potential contradictory or erroneous encodings.

#### ending
MEI `ending` elements are transformed to entries in an MSM `sequencingMap`. If there is only one `ending`, playback will skip it at repetition by default. Meico tries to realize the order of the endings according to the numbering in the endings' `n` attribute. This attribute should contain an integer substring (e.g., `"1"`, `"1."`, `"2nd"`, `"Play this at the 3rd time!"`, but not `"first"` or `"Play this at the third time!"`). (Sub-)String `"fine"`/`"Fine"`/`"FINE"` will also be recognized. Strings such as `"1-3"` will be recognized as `"1"`, this means that more complex instructions will not be recognized correctly due to the lack of unambiguous, binding formalization (meico is no "guessing machine"). If meico does not find attribute `n`, it tries to parse attribute `label`. If neither of them is provided or meico fails to extract a meaningful numbering, the endings are played in order of appearance in the MEI source.

After exporting MSM from MEI it is necessary to expand repetitions, i.e. resolve the `sequencingMap`, in MSM to get the "through-composed" music. Meico's commandline implementation does this automatically. In the window mode gui it has to be triggered explicitly.

Meico tries to cover a variety of repetition and ending constellations but it is virtually impossible to cover all the possible situations that MEI allows (such as nested repetitions and repetitions within endings). More complex situations should be encoded using MEI's `expansion` environment. In this case, the user should not expand repetitions/resolve the MSM `sequencingMap` (as described previously) but resolve expansions. In MEI, repetitions/endings and expansions can be used alternatively or complementarily. Hence, it is up to the user to decide what is necessary for a correct transformation of MEI code to other formats.

#### expan
There is no special processing routine for this element. Meico just processes its children.

#### expansion
These elements are resolved during the preprocessing of MEI data. This functionality of meico can also be used to transform an MEI with `expansion` elements into a "through-composed" MEI without `expansion` elements. It rearranges MEI music data according to what is indicated in the `expansion`'s `plist` attribute. In contrast to repetitions/endings, this will not be written into an MSM `sequencingMap`.

Subtrees that do not appear in the `plist` are not performed and will be deleted. If subtrees need to be duplicated, all `xml:id` attributes are edited to avoid multiple occurences of the same id. If a node has multiple concurrent `expansion` children, i.e. an `expansion` has `expansion` siblings, only the first will be realized. Expansions can be used on several levels of the MEI tree, expansions within expansions are supported.

The MEI definition allows the use of `expansion` elements only as children of `lem`, `rdg`, `ending`, and `section` and only to rearrange siblings of these types. Meico is more open in this regard. The use of expansions is supported anywhere in the `music` subtree of `mei` and can be used to rearrange siblings of any kind (for instance, the sequence of `measure` elements).

MEI's `expansion` elements can be regarded as the gestural counterparts of repetition barlines and Da Capi etc. But within a subtree there might still be inner repetitions that are not described by expansions. Hence, meico can do both: during MEI-to-MSM conversion expansions are realized, and during MSM processing repetitions (encoded in the `sequencingMap`) are resolved. It is possible for the user/application to prevent either of these steps if `expansion` elements were used to encode the repetitions.

#### fTrem
At the moment, tremoli are not resolved into sequences of notes but interpreted as chords. This is preliminary until we address ornamentations in the further development.

#### hairpin
Hairpins indicate a continuous dynamics transition. Their processing is similar to the corresponding `dynam` element.

#### halfmRpt
This creates a copy of the preceding timeframe. This timeframe is `0.5 * length of one measure`.

#### instrDef
If the staff label did not suffice to properly indicate which General MIDI instrument should be chosen during the MIDI export, this element can be used to provide clarity. This element is supported only in the `staffDef` environment, i.e. as child or sub-child of a `staffDef` element such as demonstrated in the MEI Encoding Guidelines in the section on [Recording General MIDI Instrumentation](https://music-encoding.org/guidelines/v4/content/integration.html#midiInstruments). Here is another example.
````xml
<staffDef clef.line="2" clef.shape="G" lines="5" n="1" label="unhelpful label">
    <instrDef midi.instrname="Violin" midi.instrnum="40"/>
</staffDef>
````
Only one of the attributes `midi.instrnum` (prioritized) and `midi.instrname` is required. The former should have values from 0 to 127 (not 1 to 128!). A list of General MIDI instrument names and numbers can be found on [Wikipedia](https://en.wikipedia.org/wiki/General_MIDI) (here the numbers must be decreased by 1!). Meico will generate an according `programChangeMap` in the MSM export and use it instead of the staff labels to trigger the correct instruments.

In the presence of multiple `instrDef` elements (maybe via `layerDef` subtrees or an `instrGrp`) meico will choose only the first of them for export. Meico does not support multiple instruments per staff as this requires a different handling of all the information in the staff MIDI-wise. This is reserved to the user who can import the MIDI data to a Digital Audio Worktation (DAW) and further produce the music.

#### instrGrp
This element is deliberately ignored. Meico handles and generates MIDI-related information individually, more comprehensive and more consistent than MEI does.

#### keyAccid
This element is processed within a `keySig`. If it occurs outside of a `keySig` environment it is invalid, hence, ignored. To interpret a `keyAccid` element in a musically meaningful way it has to provide attributes `pname` and `accid`.

#### keySig
In addition to all common key signatures meico supports also the more unorthodox ones such as mixed key signatures that use more than one type of accidentals. In fact, any combination of `keyAccid` child elements will be interpreted by meico. Also the attributes `sig` and `sig.mixed` will be interpreted. For supported accidental strings see element `accid`. If accidentals are defined through both aforementioned ways (`keyAccid` AND attributes), meico will combine both. The `keySig` will be processed lyer sensitively, so the editor should sure that it is defined in the right scope.

#### layer
After processing all children of a `layer` element meico computes the length of the musical snippet and compares it to all other parallel layers of the same `staff` parent. This information is then used to treat overful and underful measures. If accidentals occur in a `layer` they will not affect parallel layers of the same `staff`.

#### layerDef
Meico supports default duration (attribute `dur.default`) and default octave (attribute `octave.default`). It is also sensitive to the scope in which the `layerDef` is defined, globally before entering a `staff` environment or locally within a `staff` environment.

#### lem
This element is part of the critical apparatus, child of `app` and processed only in this context.

#### lyrics
There is no special processing routine for this element so far. Meico just processes its contents as far as it is suported.

#### mdiv
When converting MEI to MSM, MIDI etc. meico will generate a separate, self-contained instance for each `mdiv`, i.e. movement. This means, exporting a musical work with three movements to MIDI result in three MIDI instances, one for each movement. The movement title will be a concatenation of the work title, the `mdiv`'s attributes `n` and `label` (as far as any of these is given). Attributes `decls` an `n` will be used to link to the `work` element in `meiHead/workList` which can be used to complement missing time signature and tempo information. If instrument names are defined only in the first movement they will not be adopted in later movements. Hence, in the MIDI export, instrumentation will be reset to the default Acoustic Grand Piano. This is because passing the instrumentation on to subsequent movements in the same order might work in many cases but the underlying assumption cannot be generalized safely.

#### measure
First of all, meico processes the contents of a measure. Then it "forgets" inline accidentals as these are valid only until the barline. Then meico computes the duration of the measure according to its actual contents. If this does not confirm the underlying time signature (see `meterSig`) the measure is either underfull or overfull. Overfull measures will always be extended in order to avoid overlapping with the succeeding measure. An underfull measure will be shortened if attribute `metcon="false"` or filled up with rests if `metcon="true"`. In any of the cases when the time siganture is not confirmed, meico will generate intermediate `timeSignature` entries in MSM's global `timeSignatureMap` to match with the measure's timing.

Meico does also process `left` and `right` barline attributes if they are given. These are converted to sequencing commands in MSM and stored in a global `sequencingMap`. If the application wants these to be considered in the MIDI export etc. it should call method `meico.Msm.resolveSequencingMaps()` on the MSM instance. This will expand the musical material into a "through-composed" form.

#### meter
If no time signature information can be found for a certain point in the MEI `music` context, meico uses the `meter` element in `meiHead/workList/work`. Attributes `count` and `unit` are supported.

#### meterSig
Meico expects the attribute pair (`count`, `unit`) or attribute `sym`. For the latter, values `"common"` (corresponds to 4/4) and `"cut"` (corresponds to 2/2) are supported. The value of `count` can be a decimal number or an additive expression that evaluates to a decimal number, such as `"2+5.5+3.857"`. Meico's `meterSig` routine is also scope sensitive (`score`, `staff`, `layer`).

#### meterSigGrp
There is no special processing routine for this element. Meico just processes its contents. However, meico currently assumes the last entry to be the time signature of the subsequent measures. There is currently no routine to switch automatically if a measure does not conform with the current time signature.

#### midi
This element is deliberately ignored. Meico handles and generates MIDI-related information individually, more comprehensive and more consistent than MEI does.

#### mRest
A measure rest is a rest with the duration of one measure. This requires an underlying time signature/`meterSig`. The `mRest` element must be child of a `staff`/`layer`! Outside of a `staff` it will be ignored.

#### mRpt
This creates a copy of the preceding timeframe. This timeframe has the length of one measure.

#### mRpt2
This creates a copy of the preceding timeframe. This timeframe is `2 * length of one measure`. If this includes a time signature change meico will take care of this, too.

#### mSpace
This is interpreted as `mRest`.

#### multiRest
This is interpreted as a series of measure rests. Attribute `num` is required to define the number of rest measures. If it is not given the `multiRest` is interpreted as one `mRest`, i.e. `num="1"`.

#### multiRpt
This is interpreted as a series of measure repeats/`mRpt`. Attribute `num` is required to define the number of repetitions. If it is not given the `multiRest` is interpreted as one `mRpt`, i.e. `num="1"`.

#### note
Concerning pitch computation, meico supports the following pitch-related attributes: `pname`, `pname.ges`, `oct`, `oct.ges`, `accid`, `accid.ges`. Gestural attributes dominate over non-gesturals. Default octaves and `octave` elements are supported. Accidentals via child `accid` elements and preceeding accidentals are also supported. Futhermore, meico keeps track of key signatures and transpositions.

For computing the duration of a note, meico supports the following duration-related attributes: `dur`, `dots`, `tie`. These can also be provided by a parental `chord` element and it does not necessarily have to be a direct parent. Dots may also be given by child `dot` elements. Ties can also be defined by `tie` elements. Meico supports an arbitrary amount of dots. The `tuplet` attribute is not yet processed but meico has full support of `tuplet` elements, these can even be nested (i.e. tuplets within tuplets). Default durations are also supported.

If the `note` specifies one of the attributes `artic` and `artic.ges` (the latter is prioritized) meico will generate the corresponding MPM data from it. It is processed in the same way as element `artic`. Meico supports any articulation denominator in these attributes, not only those allowed in the MEI specification.

Attribute `syl` is also supported and will be converted to an MSM `lyrics` element that is child of the MSM `note`.

Grace notes are not yet supported.

#### octave
Attribute `dis` or `dis.place` is mandatory. Valid values of `dis` are `"8"`, `"15"` and `"22"`. Valid values of `dis.place` are `"below"` and `"above"`. The processing is sensitive to `layer` environment of the `startid` associate (but only if there is a `startid` attribute) and to attribute `layer`.

Attribute `staff` can be used to associate the octave instruction with one or more staffs, i.e. MSM `part` elements. If this attribute is omitted the `octave` is treated as global, i.e. relevant to all musical parts.

For computing the `date` of the pedal instruction, attributes `tstamp.ges`, `tstamp`, `startid` and `plist` are supported. For the optional end date, attributes `dur`, `tstamp2.ges`, `tstamp2` and `endid` are supported (in this exact priority, i.e. `endid` is only used if none of the other three is given).

#### oLayer
This element is processed by the same routine as MEI `layer` elements.

#### orig
This element is processed as part of the `choice` environment and also outside of that environment, assuming it is part of an `orig`-`reg` pair.

#### oStaff
This element is processed by the same routine as MEI `staff` elements.

#### parts
There is no special processing routine for this element. Meico just processes its children.

#### part
There is no special processing routine for this element. Meico just processes its children.

#### pedal
Meico requires attribute `dir`. This is sensitive to the `staff` attribute but not `layer`. In the course of MEI-to-MSM conversion pedal instructions are stored in the MSM `pedalMap` (global or local, depending on whether it is associated to one or more staffs). However, pedaling is not yet implemented in MIDI export.

Attributes `part` (priority) and `staff` can be used to associate the instruction with one or more `staff` elements, i.e. MSM `part` elements. If these attributes are omitted the instruction is treated as global, i.e. relevant to all musical parts. In attribute `part` - and in contrast to the MEI specification - the following values are supported: `%all` and space separated staff numbers.

For computing the `date` of the pedal instruction, attributes `tstamp.ges`, `tstamp`, `startid` and `plist` are supported. For the optional end date, attributes `dur`, `tstamp2.ges`, `tstamp2` and `endid` are supported (in this exact priority, i.e. `endid` is only used if none of the other three is given).

#### phrase
Meico fills an MSM `phraseMap` with data on the phrase structure of the music. The MSM representation looks like this: `<phrase date="" label="" xml:id="" date.end=""/>`. It encodes the start and end date of the phrase in midi ticks. Attribute `label` is created from the MEI `prase` element's attribute `label` (prioritized) or `n`. Attribute `xml:id` is copied from the MEI source. If these attributes are missing, the MSM representation won't have them neither.

Attributes `part` (priority) and `staff` can be used to associate the instruction with one or more `staff` elements, i.e. MSM `part` elements. If these attributes are omitted the instruction is treated as global, i.e. relevant to all musical parts. In attribute `part` - and in contrast to the MEI specification - the following values are supported: `%all` and space separated staff numbers.

For computing the start date of the phrase, attributes `tstamp.ges`, `tstamp`, `startid` and `plist` are supported. For the end date, attributes `dur`, `tstamp2.ges`, `tstamp2` and `endid` are supported (in this exact priority, i.e. `endid` is only used if none of the other three is given).

#### rdg
This element is part of the critical apparatus, child of `app` and processed only in this context.

#### reg
This element is processed as part of the `choice` environment and also outside of that environment, assuming it is part of an `orig`-`reg` pair.

#### reh
This element represents a rehearsal mark. From this meico creates a `marker` element in MSM and adds it to the global or local (depending on the environment) `markerMap`.

#### rend
This element is optional and features information that is not relevant for meico's MEI to MSM/MPM conversion. Its contents, however, are. Hence, all `rend` elements are replaced by their contents during preprocessing.

#### rest
During MEI-to-MSM export meico creates `rest` elements also in MSM and keeps the `xml:id` of the source element. Some applications may need this information, others may not. The latter can savely delete all rests from the MSM by calling method `meico.MSM.removeRests()`. The commandline application does it automatically. The GUI mode offers it as a separate function. The duration is computed by the same routine as for `note` elements.

#### restore
There is an ambiguity in the MEI definition: `restore` negates `del` elements in both cases, when the `del` is parent of `restore` and when when `del` is child of `restore`. With meico we follow the latter interpretation, i.e. `restore` negates all `del` children (all, not only the first generation of `del` elements!). Hence, meico will process all contents of such affected deletions.

#### @sameas
The processing of elements with this attribute is similar to the processing of `@copyof`.

#### scoreDef
For time signature the following attributes are supported: `meter.count`, `meter.unit`, and `meter.sym`. For key signature meico supportsattributes `key.sig` and `key.sig.mixed`. Further supported attributes are `dur.default`, `octave.default` and `trans.semi`. MIDI-related information are deliberately ignored as meico generates and handles these more consistent and comprehensive. If a `scoreDef` ocurs within a `staff` environment, it is interpreted as a `staffDef`.

#### score
There is no special processing routine for this element. Meico just processes its children.

#### section
Meico processes contents of this elements. Additionally, meico fills an MSM `sectionMap` with data on the section structure of the music. The MSM representation looks like this: `<section midi.date="" label="" xml:id="" midi.date.end=""/>`. It encodes the start and end date of the section in midi ticks. Attribute `label` is created from the MEI `section` element's attribute `label` (prioritized) or `n`. Attribute `xml:id` is copied from the MEI source. If these attributes are missing, the MSM representation won't have them neither.

#### sic
There is no special processing routine for this element. Meico just processes its children.

#### slur
Meico interprets an MEI `slur` as a legato instruction. All but the final `note` or `chord` that participate in the `slur` are articulated legato in the MPM export. The final element's articulation is denominated `"legatoStop"` to indicate that this might be articulated different (perhaps even shorter) than the standard articulation (which is called `"standardArticulation"` in the MPM export).

If a `slur` element defines a `plist`, meico uses this to identify all `note` and `chord` elements that participate in that `slur`. If `startid` and `endid` are not contained in the `plist`, meico adds them. Other element types than `note` and `chord` are ignored. The final entry in the `plist` will be articulated `"legatoStop"`. If multiple notes should be articulated this way, the final element should be a `chord` that includes all those notes.

If (and only if) no `plist` is specified, meico will compute the start date from `tstamp.ges`, `tstamp`, `startid` or `plist` and the end date from `dur`, `tstamp2.ges`, `tstamp2` or `endid`. Attributes `staff` and `layer` are taken into account to determine the notes to be articulated. In the absence of a `staff` or `layer` attribute meico checks whether `startid` and `endid` are in the same staff or layer, respectively, and articulates only notes within this same staff or layer. If they are in different layers, the whole staff content under the slur will be articulated. If even the staff relation is indefinite, all musical content under the `slur` is played legato. The final notes of a `slur`, i.e. those notes that are at the slur's end, are always articulated `"legatoStop"` even if another `slur` starts at this note or goes further.

#### space
If this element encodes a textual gap (e.g. in lyrics) it has no musical meaning and is ignored. Otherwise, it is interpreted as `rest`.

#### staff
Meico requires either attribute `def` or `n` to associate the `staff` element and its contents with a preceding `staffDef`. If none is give or there is no corresponding `staffDef` meico will generate a new `part` during MSM export.

#### staffDef
During MEI-to-MSM conversion a `staffDef` will initiate the generation of a new `part` element in MSM. Attribute `label` will be concatenated with a parental `staffGrp` `label`, if such exists, and will be used for the labeling of the MSM `part` element. It will also be used to match an instrument with the staff and generate the corresponding MIDI Program Changes. Attribute `n` should also be present to make the correct associations with succeeding `staff` elements.

For time signature the following attributes are supported: `meter.count`, `meter.unit`, and `meter.sym`. For key signature meico supportsattributes `key.sig` and `key.sig.mixed`. Further supported attributes are `dur.default`, `octave.default` and `trans.semi`. MIDI-related information are deliberately ignored as meico generates and handles these more consistent and comprehensive.

#### staffGrp
There is no special processing routine for this element. Meico just processes its children. During the processing of `staffDef` elements meico will also check for a parental `staffGrp` to use it `label` attribute for the naming of MSM `part` elements and instrument matching and to generate MIDI Program Changes.

#### subst
This element is processed as part of the `choice` environment and also outside of that environment.

#### supplied
There is no special processing routine for this element. Meico just processes its children.

#### syl
MEI `syl` elements are only processed if they are descendants of a `note` to be associated with. Meico generates an MSM `lyrics` element from it. If it have a parental `verse` element meico uses their attribute `n` to add a verse number.

#### tempo
Tempo instructions require either a metronomic value or a textual value, i.e. a descriptor string such as `"Allegro"`, `"rit."` etc. Supported attributes for numeric tempo values are `mm`, `midi.bpm` and `midi.mspb` (if several are present this is the priority order). To specify the length of one metronomic beat meico supports attributes `mm.unit` and `mm.dots`. Meico's parser understands the following descriptor strings and generates corresponding tempo values:

| tempo descriptor (non-case sensitive) | beats per minute |
|---------------------------------------|------------------|
| ...grave...                           | 42.0             |
| ...largo...                           | 50.0             |
| ...lento...                           | 51.0             |
| ...adagietto...                       | 66.0             |
| ...larghetto...                       | 69.0             |
| ...adagio...                          | 79.0             |
| ...andantino...                       | 80.0             |
| ...maestoso...                        | 88.0             |
| ...andante...                         | 101.0            |
| ...moderato...                        | 106.0            |
| ...allegretto...                      | 110.0            |
| ...animato...                         | 121.0            |
| ...assai...                           | 145.0            |
| ...allegro...                         | 147.0            |
| ...vivace...                          | 164.0            |
| ...presto...                          | 189.0            |
| ...prestissimo...                     | 206.0            |
| ...rit..., ...rall..., ...largando..., ...calando... |   |
| ...accel..., ...string...             |                  |
| any other descriptor                  | 100.0            |

If the `tempo` element does not specify a descriptor string as value, i.e. inner text, meico will try to use the text of attribute `label` instead, if that exists. These default values are averages from [https://de.wikipedia.org/wiki/Tempo_(Musik)](https://de.wikipedia.org/wiki/Tempo_(Musik)). The users may edit these values in the MPM tempo style definition and add further descriptor-value mappings. If a continuous transition is followed by another continuous transition or no further instruction at all, the in-between values are unclear and shall be edited by the user in the corresponding MPM `tempoMap`.

If the `tempo` has an `xml:id` it is also applied in the MPM encoding. Attributes `part` (priority) and `staff` can be used to associate the instruction with one or more `staff` elements, i.e. MSM `part` elements. If these attributes are omitted the instruction is treated as global, i.e. relevant to all musical parts. In attribute `part` - and in contrast to the MEI specification - the following values are supported: `%all` and space separated staff numbers. Attribute `layer` is not supported and meico will not search for the staffs that contain the specified layers. 

Mandatory for the interpretation of the `tempo` is information about its temporal location in the music. The easiest way is placing it within a `verse` element that itself is a child of a note, so the performance instruction gets the same timing position. Alternatively, attributes `tstamp.ges`, `tstamp`, `startid`, or `plist` can be used. For continuous tempo transitions the attributes `dur`, `tstamp2.ges`, `tstamp2` and `endid` are supported (in this exact priority, i.e. `endid` is only used if none of the other three is given). The `endid` can also be the id of the subsequent `tempo` element. Continuous tempo transitions without one of the previous four attributes end at the subsequent instruction. Our recommendation: If there is a tempo instruction anyway the attributes should not be used at all to avoid potential contradictory or erroneous encodings.

#### tie
Meico requires attributes `startid` and `endid`. The editor should ensure that the corresponding `note` or `chord` elements actually exist. Cross-layer tieing is supported, cross-staff tieing is not. Furthermore, it is mandatory that both notes have the exact same pitch, including accidentals! Even though the end note of a tie is ususally notated without accidental (hence, no `accid` attribute or child element) it must then have an `accid.ges` attribute or it will not be tied to the previous note and played with the notated wrong pitch! Each `tie` element is internally resolved into `tie` attributes. Meico is able to handle a mixture of `tie` elements and already existing `tie` attributes on `note` or `chord` elements. Tieing of non-adjacent notes is not supported.

#### title
This element is located in the `meiHead` environment, more precisely in one of these two subtrees `mei/meiHead/fileDesc/titleStmt` or `mei/meiHead/workDesc/work/titleStmt`. Meico prefers the former and uses the latter only in absence of the former. Meico keeps the title string during MEI-to-MSM conversion and writes it to MSM's root element `msm`. However, if more than one `title` element are present, meico uses only the first!

#### tuplet
These elements are processed during the computation of durations. Required attributes are `dur`, `numbase` and `num`. Meico does also support nested and overlapping tuplets.

#### tupletSpan
Required attributes `num` and `numbase`. The processing of this element is sensitive to `layer`, but only if attribute `startid` is used to indicate the beginning of the `tupletSpan` and that referred element stands within a `layer` environment.

Attributes `part` (priority) and `staff` can be used to associate the instruction with one or more `staff` elements, i.e. MSM `part` elements. If these attributes are omitted the instruction is treated as global, i.e. relevant to all musical parts. In attribute `part` - and in contrast to the MEI specification - the following values are supported: `%all` and space separated staff numbers.

For computing the `date` of the `tupletSpan`, attributes `tstamp.ges`, `tstamp`, `startid` and `plist` are supported. For the end date, attributes `dur`, `tstamp2.ges`, `tstamp2` and `endid` are supported (in this exact priority, i.e. `endid` is only used if none of the other three is given).

#### unclear
This element is processed as part of the `choice` environment and also outside of that environment.

#### verse
There is no special processing routine for this element. Meico just processes its children. When a child `syl` element is processed it looks here to find a verse number (attribute `n`). However, it is optional.

#### work
Meico uses `meter` signature and `tempo` information from this element's children if they cannot be found in the `music` environment.

#### workDesc
This element occurs in MEI 3.0 `meiHead`. It holds the `work` elements that meico processes, as described above.

#### workList
This element occurs in MEI 4.0 `meiHead`. It holds the `work` elements that meico processes, as described above.
