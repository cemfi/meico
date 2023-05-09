# Meico: MusicXML Coverage Documentation

The following MusicXML elements are supported, i.e. processed by meico during MEI-to-MSM/MPM conversion. The set of supported elements will be extended in the future.


#### group-name
The string value of the `group-name` element will be part of the `part-name`s in MSM and MPM. E.g., if the group's name is `"Trumpets"` and the part names are `"1"`, `"2"` and `"3"`, then the MSM part names will be `"Trumpets 1"`, `"Trumpets 2"` and `"Trumpets 3"`.

#### movement-number
The value of this element is used to compose the title string: `work-number + work-title + movement-number + movement-title`.

#### movement-title
The value of this element is used to compose the title string: `work-number + work-title + movement-number + movement-title`.

#### part-group
The `group-name` elements are needed to involve them in the part names in MSM and MPM. The `type` attribute is considered to indicate which `score-part` elements belong to the group. The `number` attribute is used to distinguish part groups from each other.

#### part-list
This element needs no special processing. Meico processes its children.

#### part-name
The string value of this element becomes the part name in MSM and MPM, possibly in a composed string involving the `group-name` prior to the `part-name`. 

#### score-part
The `score-part`'s ID will be the MSM and MPM `part`'s ID. Children of this element are further processed.

#### score-partwise
The root element of a partwise organized  MusicXML requires no specific processing. Meico just processes its children.

#### score-timewise
The root element of a timewise organized  MusicXML requires no specific processing. Meico just processes its children.

#### work
This element needs no special processing. Meico processes its children.

#### work-number
The value of this element is used to compose the title string: `work-number + work-title + movement-number + movement-title`.

#### work-title
The value of this element is used to compose the title string: `work-number + work-title + movement-number + movement-title`.
