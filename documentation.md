# Meico: MEI Coverage Documentation

The following MEI elements are supported, i.e. processed, by meico. The set of supported elements will be extended in the future. Details on how meico interprets these elements, their attributes and child elements will be added soon.


#### abbr
This element is processed only in the context as child of `choice`.

#### accid

#### add
There is no special processing routine for this element. Meico just processes its contents.

#### app

#### beam
There is no special processing routine for this element. Meico just processes its contents.

#### beatRpt

#### bTrem
At the moment, tremoli are not resolved into sequences of notes but interpreted as chords. This is preliminary until we address ornamentations in the further development.

#### choice

#### chord

#### corr
There is no special processing routine for this element. Meico just processes its contents.

#### del

#### dot
This element is proccessed as child of a note, rest etc.

#### ending

#### expan
There is no special processing routine for this element. Meico just processes its contents.

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
This element is part of the critical apparatus, child of `app` and processed in this context.

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

#### rdg
This element is part of the critical apparatus, child of `app` and processed in this context.

#### reg
This element is processed as part of the `choice` environment and also outside of that environment, assuming it is part of an `orig`-`reg` pair.

#### reh

#### rest

#### restore
Meico sets all `del` elements in this `restore` to `restore-meico="true"` and processes all contents.

#### scoreDef

#### score
There is no special processing routine for this element. Meico just processes its contents.

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
