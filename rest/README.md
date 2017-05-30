# meico REST API

Author: [Simon Waloschek](https://github.com/sonovice)

meico REST API is a thin REST-like wrapper around meico.
### Starting
The API is designed to be executed in a docker container. A minimal `Dockerfile` is provided.

For developers, the `app.py` can be started without docker via python 3 after installing a Java Runtime
Environment (JRE) and the requirements from `requirements.txt`. **Be aware that the development server should
never be used in a production environment!**

### API Endpoint Description
The endpoint is named `meico`, therefore all calls to the API have to address `host:8001/meico`.

| Option                | Description                                                                                                                         |
|-----------------------|----------------------------------------------------------------------------------|
| `output`              | [str] specify the output type, accepts [mei, msm, midi, wav, mp3]                |
| `validate`            | [bool] validate uploaded MEI file                                                |
| `add_ids`             | [bool] add missing `xml:id`s to note, rest and chord elements in MEI             |
| `no_program_changes`  | [bool] suppress program change events in MIDI, all music will be played by piano |
| `dont_use_channel_10` | [bool] do not use channel 10 (drum channel) in MIDI and audio                    |
| `tempo`               | [float] set MIDI tempo (bpm), default is 120 bpm                                 |
| `movement`            | [uint] set the number of the desired movement to be processed                    |
| `soundbank`           | [str] use a specific sound bank file (.sf2, .dls) for wave file conversion       |
| `debug`               | [bool] write additional debug information in MEI and MSM output                  |

Example Queries (using cURL):
```bash
$ curl -F "mei=Beethoven.mei" host:8001/meico?output=mp3&tempo=115.5&soundbank=smg --output Beethoven.mp3
$ curl -F "mei=MyComposition.mei" host:8001/meico?output=msm&movement=1&add_ids=true --output MyComposition.msm
```

### Add Soundbanks
In order to add soundbanks to the API, the corresponding files (.sf2 or .dls) have to be
copied into the `api\soundbanks` folder. Furthermore, the list of all banks inside the
`app.py` has to be modified accordingly. The key of each dictionary entry specifies the
name used to select a specific soundbank when querying the API, the value has to match
the filename of the soundbank.

Example:
```python
SB_FILES = {
    'airfont': 'Airfont_340.dls',
    'arachno': 'Arachno SoundFont - Version 1.0.sf2',
    'fluidr3': 'FluidR3 GM2-2.sf2',
    'freefont': 'FreeFont.sf2',
    'musescore': 'GeneralUser GS MuseScore v1.442.sf2',
    'omega': 'OmegaGMGS2.sf2',
    'papelmedia': 'Papelmedia SF2 2006 (from VSampler).sf2',
    'sgm': 'SGM-V2.01.sf2',
    'heaven': 'Timbres Of Heaven GM_GS_XG_SFX V 3.2 Final.sf2',
    'yamaha': 'Yamaha_XG_Sound_Set.sf2'
}
```

