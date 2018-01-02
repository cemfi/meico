# Meico: MEI Coverage Documentation

The following MEI elements are supported, i.e. processed, by meico. The set of supported elements will be extended in the future. Details on how meico interprets these elements, their attributes and child elements will be added soon.


#### abbr
This element is processed only as child of `choice`.

#### accid
Attributes `ploc` and `oloc` are mandatory to determine the pitch to be affected by the accidental. Otherwise it is ignored. The effect of the accidental is read from attribute `accid.ges` (prioritized) or `accid`. If none is given the element is ignored. Meico will take the accidental into account until the end of the measure. Previous accidentals on the same pitch are overridden, only the last accidental on that pitch is applied to succeeding notes. Accidentals apply to all notes throughout all layers in the same staff as the `accid` element. Meico renders the following accidentals to semitone intervals in MSM:

| MEI accidental | Semitone interval in MSM |
|----------------|--------------------------|
|   `s` |    1 (= a semitone up)     |
|   `f` |   -1 (= a semitone down)   |
|  `ss` |    2 (= a whole tone up)   |
|   `x` |    2 (= a whole tone down) |
|  `ff` |   -2 |
|  `xs` |    3 |
|  `ts` |    3 |
|  `tf` |   -3 |
|   `n` |    0 |
|  `nf` |   -1 |
|  `ns` |    1 |
|  `su` |  1.5 |
|  `sd` |  0.5 (= a quarter tone up) |
|  `fu` | -0.5 (= a quarter tone down)|
|  `fd` | -1.5 |
|  `nu` |  0.5 |
|  `nd` | -0.5 |
| `1qf` | -0.5 |
| `3qf` | -1.5 |
| `1qs` |  0.5 |
| `3qs` |  1.5 |

While quarter tone intervals and pitches are numerically well represented in MSM they will get lost, i.e. rounded to semitone intervals, during Midi export.

#### add
There is no special processing routine for this element. Meico just processes its contents.

#### app
MEI's critical apparatus environment contains one or more alternative encodings/readings via elements `lem` and `rdg`. If `app` contains none of them it is ignored. The contents of `lem` and `rdg` elements are processed only in the `app` environment. Meico interprets `lem` as the favored reading and will, hence, render the contents of the first `lem` child of `app`. Later `lem` elements are ignored. If no `lem` is given meico chooses the first `rdg` for rendering.

#### beam
There is no special processing routine for this element. Meico just processes its contents.

#### beatRpt
This repeats the musical contents of the last beat before this element. The length of one beat is taken from the underlying time signature's denominator or a quarter beat if no time signature is given.

#### bTrem
At the moment, tremoli are not resolved into sequences of notes but interpreted as chords. This is preliminary until we address ornamentations in the further development.

#### choice
Similar to `app`, the `choice` element defines one or more alternative readings. The elements, i.e. subtrees, of which meico chooses one to render are (in order of priority) `corr`, `reg`, `expan`, `subst`, `choice`, `orig`, `unclear`, `sic`, and `abbr`. In case of another `choice` it is processed recursively. If none of these can be found as child of `choice` meico chooses the first child whatever type it is. The `cert` attrubute, i.e. certainty rating, is not taken into account so far.

#### chord
If the `chord` is child of another `chord` meico will look for its duration data to be used if this inner chord does not define its own duration. If no duration is given from the parent ot the `chord` itself meico will search the child elements for the longest duration and assign it to the `chord`. Chords with a `grace` attribute are ignored at the moment. These will be subject to the further development when ornamentations are addressed.

#### corr
A correction, can occur as "standalone" or in the `choice` environment, usually paired with the `sic` element. In both cases, meico processes its contents.

#### del
The `del` element contains information deleted, marked as deleted, or otherwise indicated as superfluous or spurious. Hence, by default, meico omits its content. The deletion is processed only if it is child of a `restore` element as this negates all deletions throughout its whole subtree.

#### dot
This element is proccessed as child of a note, rest etc. for the computation of durations. Outside of such an environment the meaning of the `dot` is unclear and meico cannot interpret it in a musically meanignful way.

#### ending

#### expan
There is no special processing routine for this element. Meico just processes its contents.

#### expansion
These elements are resolved during the preprocessing of MEI data. This functionality of meico can also be used to transform an MEI with `expansion` elements into a "through-composed" MEI without `expansion` elements. It rearranges MEI music data according to what is indicated in the `expansion`'s `plist` attribute. In contrast to repetitions, this will not be written into an MSM `sequencingMap`.

Subtrees that do not appear in the `plist` are not performed and will be deleted. If subtrees need to be duplicated, all `xml:id` attributes are edited to avoid multiple occurences of the same id. If a node has multiple concurrent `expansion` children, i.e. an `expansion` has `expansion` siblings, only the first will be realized. Expansions can be used on several levels of the MEI tree, expansions within expansions are supported.

The MEI definition allows the use of `expansion` elements only as children of `lem`, `rdg`, `ending`, and `section` and only to rearrange siblings of these types. Meico is more open in this regard. The use of expansions is supported anywhere in the `music` subtree of `mei` and can be used to rearrange siblings of any kind (for instance, the sequence of `measure` elements).

MEI's `expansion` elements can be regarded as the gestural counterparts of repetition barlines and Da Capi etc. But within a subtree there might still be inner repetitions that are not described by expansions. Hence, meico can do both: during MEI-to-MSM conversion expansions are realized, and during MSM processing repetitions (encoded in the `sequencingMap`) are resolved. It is possible for the user/application to prevent either of these steps if `expansion` elements were used to encode the repetitions.

#### fTrem
At the moment, tremoli are not resolved into sequences of notes but interpreted as chords. This is preliminary until we address ornamentations in the further development.

#### halfmRpt

#### instrDef
This element is deliberately ignored. Meico handles and generates MIDI-related information individually, more comprehensive and more consistent than MEI does.

#### instrGrp
This element is deliberately ignored. Meico handles and generates MIDI-related information individually, more comprehensive and more consistent than MEI does.

#### keyAccid
This element is processed within a `keySig`. If it occurs outside of a keySig environment it is invalid, hence, ignored.

#### keySig

#### layer

#### layerDef

#### lem
This element is part of the critical apparatus, child of `app` and processed only in this context.

#### lyrics
There is no special processing routine for this element so far. Meico just processes its contents as far as it is suported.

#### mdiv

#### measure

#### meterSig

#### meterSigGrp
There is no special processing routine for this element. Meico just processes its contents. However, meico currently assumes the last entry to be the time signature of the following measures. There is currently no routine to switch automatically if a measure does not conform with this.

#### midi
This element is deliberately ignored. Meico handles and generates MIDI-related information individually, more comprehensive and more consistent than MEI does.

#### mRest

#### mRpt

#### mRpt2

#### mSpace
This is interpreted as `mRest`.

#### multiRest

#### multiRpt

#### note

#### octave

#### orig
This element is processed as part of the `choice` environment and also outside of that environment, assuming it is part of an `orig`-`reg` pair.

#### parts
There is no special processing routine for this element. Meico just processes its contents.

#### part
There is no special processing routine for this element. Meico just processes its contents.

#### pedal

#### phrase
Meico processes contents of this elements. Additionally, meico fills an MSM `phraseMap` with data on the phrase structure of the music. The MSM representation looks like this: `<phrase midi.date="" label="" xml:id="" midi.date.end=""/>`. It encodes the start and end date of the phrase in midi ticks. Attribute `label` is created from the MEI `prase` element's attribute `label` (prioritized) or `n`. Attribute `xml:id` is copied from the MEI source. If these attributes are missing, the MSM representation won't have them neither.

#### rdg
This element is part of the critical apparatus, child of `app` and processed only in this context.

#### reg
This element is processed as part of the `choice` environment and also outside of that environment, assuming it is part of an `orig`-`reg` pair.

#### reh

#### rest

#### restore
There is an ambiguity in the MEI definition: `restore` negates `del` elements in both cases, when the `del` is parent of `restore` and when when `del` is child of `restore`. With meico we follow the latter interpretation, i.e. `restore` negates all `del` children (all, not only the first generation of `del` elements!). Hence, meico will process all contents of such affected deletions.

#### scoreDef

#### score
There is no special processing routine for this element. Meico just processes its contents.

#### section
Meico processes contents of this elements. Additionally, meico fills an MSM `sectionMap` with data on the section structure of the music. The MSM representation looks like this: `<section midi.date="" label="" xml:id="" midi.date.end=""/>`. It encodes the start and end date of the section in midi ticks. Attribute `label` is created from the MEI `section` element's attribute `label` (prioritized) or `n`. Attribute `xml:id` is copied from the MEI source. If these attributes are missing, the MSM representation won't have them neither.

#### sic
There is no special processing routine for this element. Meico just processes its contents.

#### space
This is interpreted as `rest`.

#### staff

#### staffDef

#### staffGrp
There is no special processing routine for this element. Meico just processes its contents.

#### subst
This element is processed as part of the `choice` environment and also outside of that environment.

#### supplied
There is no special processing routine for this element. Meico just processes its contents.

#### tie

#### tuplet

#### tupletSpan

#### unclear
This element is processed as part of the `choice` environment and also outside of that environment.
