import os
import tempfile
import threading

import hug
import jpype

from falcon.status_codes import HTTP_BAD_REQUEST

MEICO_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'meico.jar')
SB_BASEDIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'soundbanks')

# Dictionary of soundbanks
SB_FILES = {
    """Example:
    
    'airfont': 'Airfont_340.dls',
    'arachno': 'Arachno SoundFont - Meico 1.0.sf2',
    'fluidr3': 'FluidR3 GM2-2.sf2',
    'freefont': 'FreeFont.sf2',
    'musescore': 'GeneralUser GS MuseScore v1.442.sf2',
    'omega': 'OmegaGMGS2.sf2',
    'papelmedia': 'Papelmedia SF2 2006 (from VSampler).sf2',
    'sgm': 'SGM-V2.01.sf2',
    'heaven': 'Timbres Of Heaven GM_GS_XG_SFX V 3.2 Final.sf2',
    'yamaha': 'Yamaha_XG_Sound_Set.sf2'
    """
}
NULL = open(os.devnull, 'w')

# Start Java Virtual Machine (JVM)
jpype.startJVM(
    jpype.getDefaultJVMPath(),
    '-ea',
    '-Djava.class.path=' + MEICO_PATH
)

temp_directories = []  # List for all temporary directory objects to clean up


def clean_up():
    """Tries to delete temporary directories"""
    error = False
    for dir in temp_directories:
        try:
            dir.cleanup()
        except PermissionError:
            error = True
    if error:
        threading.Timer(5, clean_up).start()  # Call clean_up() again periodically


@hug.request_middleware()
def make_temporary_directory(request, response):
    """Generates temporary directory for current context."""
    temp_dir = tempfile.TemporaryDirectory()
    request.context['temp_dir'] = temp_dir


@hug.response_middleware()
def delete_temporary_directory(request, response, resource):
    """Cleans up files after request."""
    temp_directories.append(request.context['temp_dir'])
    threading.Timer(10, clean_up).start()  # Clean up in 10 Seconds


@hug.post(output=hug.output_format.file)
def meico(body, request, response,
          output: hug.types.one_of(['mei', 'msm', 'midi', 'wav', 'mp3']),
          validate: hug.types.smart_boolean = False,
          add_ids: hug.types.smart_boolean = False,
          no_program_changes: hug.types.smart_boolean = False,
          dont_use_channel_10: hug.types.smart_boolean = True,
          ignore_expansions: hug.types.smart_boolean = False,
          debug: hug.types.smart_boolean = False,
          movement: hug.types.number = 0,
          tempo: hug.types.float_number = 120,
          soundbank: hug.types.one_of(SB_FILES.keys()) = None):
    file_list = list(body.keys())  # Get list of files from request
    temp_dir = request.context['temp_dir'].name  # Get temporary directory for current context

    # Java class definitions
    File = jpype.java.io.File
    Mei = jpype.JPackage('meico').mei.Mei

    # Check if MEI file provided
    if 'mei' not in file_list:
        response.status = HTTP_BAD_REQUEST
        return {'errors': 'No MEI file provided.'}

    try:
        mei_xml = body['mei'].decode('utf-8')  # Extract MEI data from body
        mei = Mei(mei_xml, validate)  # Read in MEI data
        if add_ids:
            mei.addIds()
        msms = mei.exportMsm(720, dont_use_channel_10, ignore_expansions, not debug)  # Generate MSMs
        msm = msms.get(movement)  # Select the desired MSM by movement number
        msm.resolveSequencingMaps()  # Resolve repetitions etc.

        if output == 'mei':
            file_path = os.path.join(temp_dir, 'meico.mei')
            mei.writeMei(file_path)
        elif output == 'msm':
            if not debug:
                msm.removeRests()
            file_path = os.path.join(temp_dir, 'meico.msm')
            msm.writeMsm(file_path)
        elif output == 'midi':
            midi = msm.exportMidi(float(tempo), not no_program_changes)
            file_path = os.path.join(temp_dir, 'meico.mid')
            midi.writeMidi(file_path)
        elif output == 'wav' or output == 'mp3':
            midi = msm.exportMidi(float(tempo), not no_program_changes)
            audio = midi.exportAudio() if soundbank is None else midi.exportAudio(File(os.path.join(SB_BASEDIR, SB_FILES[soundbank])))
            if output == 'wav':
                file_path = os.path.join(temp_dir, 'meico.wav')
                audio.writeAudio(file_path)
            elif output == 'mp3':
                file_path = os.path.join(temp_dir, 'meico.mp3')
                audio.writeMp3(file_path)
        return file_path
    except jpype.JavaException as e:
        print(e.stacktrace())
        response.status = HTTP_BAD_REQUEST
        return {'errors': 'Error during processing of MEI file.'}


# Start development server
# !!!DO NOT USE IN PRODUCTION!!!
if __name__ == '__main__':
    hug.API(__name__).http.serve(port=8001)
