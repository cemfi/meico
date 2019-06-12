# Meico: MEI Coverage Documentation

The following MEI elements are supported, i.e. processed, by meico. The set of supported elements will be extended in the future.


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

#### beam
There is no special processing routine for this element. Meico just processes its children.

#### beatRpt
This repeats the musical contents of the last beat before this element. The length of one beat is taken from the underlying time signature's denominator or a quarter beat if no time signature is given.

#### bTrem
At the moment, tremoli are not resolved into sequences of notes but interpreted as chords. This is preliminary until we address ornamentations in the further development.

#### choice
Similar to `app`, the `choice` element defines one or more alternative readings. The elements, i.e. subtrees, of which meico chooses one to render are (in order of priority) `corr`, `reg`, `expan`, `subst`, `choice`, `orig`, `unclear`, `sic`, and `abbr`. In case of another `choice` it is processed recursively. If none of these can be found as child of `choice` meico chooses the first child whatever type it is. The `cert` attribute, i.e. certainty rating, is not taken into account so far.

#### chord
If the `chord` is child of another `chord` meico will look for its duration data to be used if this inner chord does not define its own duration. If no duration is given from the parent of this `chord` meico will search the child elements for the longest duration and assign it to the `chord`. Chords with a `grace` attribute are ignored at the moment. These will be subject to the further development when ornamentations are addressed.

#### @copyof
Many MEI elements do also offer an attribute called `copyof`. It can be used for a "slacker" encoding: Whenever an element is similar to another one it sufficed to write it out only once and then refer to that one later on. Meico resolves these "copyof elements" during preprocessing. Of course, this requires the reference id to be actually existent. MEI does even allow consecutive and recursive copyof references, i.e. the copyof refers to another copyof element or an element that containes further copyof elements. Meico can resolve these, too, and it detects circular cases that cannot be resolved because there is no initial element to copy. Resolving a copyof element means that meico will replace it with a deep copy (including all subtrees) of the referred element. During the resolution of copyofs `xml:id` attributes will get duplicated; meico will concatenate it with a newly generated UUID and ensure that each ID ocurs only once.

#### corr
A correction, can occur as "standalone" or in the `choice` environment, usually paired with the `sic` element. In both cases, meico processes its children.

#### del
The `del` element contains information deleted, marked as deleted, or otherwise indicated as superfluous or spurious. Hence, by default, meico omits its content. The deletion is processed only if it is child of a `restore` element as this negates all deletions throughout its whole subtree.

#### dot
This element is proccessed as child of a `note` or `rest` for the computation of durations. Outside of such an environment the meaning of the `dot` is unclear and meico cannot interpret it in a musically meanignful way.

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

#### halfmRpt
This creates a copy of the preceding timeframe. This timeframe is `0.5 * length of one measure`.

#### instrDef
This element is deliberately ignored. Meico handles and generates MIDI-related information individually, more comprehensive and more consistent than MEI does.

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
When converting MEI to MSM, MIDI etc. meico will generate a separate, self-contained instance for each `mdiv`, i.e. movement. This means, exporting a musical work with three movements to MIDI result in three MIDI instances, one for each movement. The movement title will be a concatenation of the work title, the `mdiv`'s attributes `n` and `label` (as far as any of these is given). If instrument names are defined only in the first movement they will not be adopted in later movements. Hence, in the MIDI export, instrumentation will be reset to the default Acoustic Grand Piano. This is because passing the instrumentation on to subsequent movements in the same order might work in many cases but the underlying assumption cannot be generalized safely.

#### measure
First of all, meico processes the contents of a measure. Then it "forgets" inline accidentals as these are valid only until the barline. Then meico computes the duration of the measure according to its actual contents. If this does not confirm the underlying time signature (see `meterSig`) the measure is either underfull or overfull. Overfull measures will always be extended in order to avoid overlapping with the succeeding measure. An underfull measure will be shortened if attribute `metcon="false"` or filled up with rests if `metcon="true"`.

Meico does also process `left` and `right` barline attributes if they are given. These are converted to sequencing commands in MSM and stored in a global `sequencingMap`. If the application wants these to be considered in the MIDI export etc. it should call method `meico.Msm.resolveSequencingMaps()` on the MSM instance. This will expand the musical material into a "through-composed" form.

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

Grace notes are not yet supported.

#### octave
Meico requires attributes `startid`, `endid`, `dur` and `dis` or `dis.place`. The `tstamp` attributes are not supported. Valid values of `dis` are `"8"`, `"15"` and `"22"`. Valid values of `dis.place` are `"below"` and `"above"`. The routine for duration computation is the same as for `note` elements. The processing is sensitive to the `satff` and `layer` environment.

#### orig
This element is processed as part of the `choice` environment and also outside of that environment, assuming it is part of an `orig`-`reg` pair.

#### parts
There is no special processing routine for this element. Meico just processes its children.

#### part
There is no special processing routine for this element. Meico just processes its children.

#### pedal
Meico requires attributes `startid`, `endid` and `dir`. Attribute `tstamp` is not supported. This is sensitive to the `satff` and `layer` environment. In the course of MEI-to-MSM conversion pedal instructions are stored in the MSM `pedalMap` (global or local, depending on the scope). However, these are not yet considered during MIDI export.

#### phrase
Meico processes contents of this elements. Additionally, meico fills an MSM `phraseMap` with data on the phrase structure of the music. The MSM representation looks like this: `<phrase midi.date="" label="" xml:id="" midi.date.end=""/>`. It encodes the start and end date of the phrase in midi ticks. Attribute `label` is created from the MEI `prase` element's attribute `label` (prioritized) or `n`. Attribute `xml:id` is copied from the MEI source. If these attributes are missing, the MSM representation won't have them neither.

#### rdg
This element is part of the critical apparatus, child of `app` and processed only in this context.

#### reg
This element is processed as part of the `choice` environment and also outside of that environment, assuming it is part of an `orig`-`reg` pair.

#### reh
This element represents a rehearsal mark. From this meico creates a `marker` element in MSM and adds it to the global or local (depending on the environment) `markerMap`.

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

#### space
This is interpreted as `rest`.

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

#### tie
Meico requires attributes `startid` and `endid`. The editor should ensure that the corresponding `note` elements actually exist and belong to the same layer, i.e. cross-layer tieing is not supported. Each `tie` element is resolved into `tie` attributes during preprocessing. Meico is able to handle a mixture of `tie` elements and already existing `tie` attributes on `note` elements. Tieing of non-adjacent notes is not supported.

#### title
This element is located in the `meiHead` environment, more precisely in one of these two subtrees `mei/meiHead/fileDesc/titleStmt` or `mei/meiHead/workDesc/work/titleStmt`. Meico prefers the former and uses the latter only in absence of the former. Meico keeps the title string during MEI-to-MSM conversion and writes it to MSM's root element `msm`. However, if more than one `title` element are present, meico uses only the first!

#### tuplet
These elements are processed during the computation of durations. Required attributes are `dur`, `numbase` and `num`. Meico does also support nested and overlapping tuplets.

#### tupletSpan
Required attributes are `startid`, `num` and `numbase`. Also either of the attributes `dur` and `endid` is required. The processing of this element is `layer` sensitive. Hence, `startid` and `endid` should refer to elements in the same `layer` environment.

#### unclear
This element is processed as part of the `choice` environment and also outside of that environment.
