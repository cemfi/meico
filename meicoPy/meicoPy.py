# This demonstrates the usage of meico in Python.
# It requires the package JPype1-py3 to be installed (pip install JPype1-py3)
# and the file meico.jar (https://github.com/cemfi/meico/releases/latest)
# to be placed in the same folder as this script.
# Author: Axel Berndt

import os
import sys
from argparse import ArgumentParser
import jpype    # this package (JPype1-py3) enables Java integration in Python


def convert_mei(validate=False, add_ids=False, resolve_copyofs=False, export_msm=False, export_midi=False, no_program_changes=False, dont_use_channel_10=False, tempo=100, export_wav=False, export_mp3=False, soundbank=None, debug=False, mei_file=None):
    """
    This function processes and converts the input file (mei_file) according to the flags and parameters set.
    :param validate: set True to validate the MEI source against MEI schema
    :param add_ids: set True to generate xml:ids in the MEI source
    :param resolve_copyofs: set True to replace elements in the MEI source with a cofyof attribute by copys of the reference elements
    :param export_msm: set True to export MSM
    :param export_midi: set True to export MIDI
    :param no_program_changes: set True to suppress the generation of MIDI Program Change messages
    :param dont_use_channel_10: set True to suppress the use of MIDI channel 10 (drum channel)
    :param tempo: set the tempo of the MIDI and audio file (im beats per minute, bpm)
    :param export_wav: set True to export audio/Wave
    :param export_mp3: set True to export audio in MP3 format
    :param soundbank: path to a sound bank (.sf2 or .dls file) to be used for MIDI-to-audio conversion
    :param debug: set True to write additional debug versions of MEI and MSM
    :param mei_file: the path to the MEI source file
    :return:
    """

    # check if meico.jar is present
    if not os.path.isfile(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'meico.jar')):

        print('Cannot find meico.jar. Please place it in the same folder as this Python script.', file=sys.stderr)
        return 69

    # check if MEI file is a valid path to a file
    if (mei_file is None) or (not os.path.isfile(mei_file)):
        print('Cannot find MEI input file. Please check that the path and file name are correct.', file=sys.stderr)
        return 66

    # start the JavaVM and set the class path to meico.jar (has to be placed in the same directory as the Python script)
    jpype.startJVM(jpype.getDefaultJVMPath(), '-ea', '-Djava.class.path=' + os.path.join(os.path.dirname(os.path.abspath(__file__)), 'meico.jar'))

    File = jpype.java.io.File                                   # the Java File class
    meiFile = File(os.path.realpath(mei_file))                  # the MEI file as a Java File object, ensure that the absolute (canonical) path is used (so, when writing the output files to the same path, everything is consistent; alternatively, applications can specify their own output path when calling the write{Mei,Msm,Midi,Audio}() methods)

    Mei = jpype.JPackage('meico').mei.Mei                       # the Mei class
    try:
        mei = Mei(meiFile, validate)                            # instantiate an Mei object from the MEI file and validate it against MEI schema (if desired)
    except jpype.JavaException as e:                            # actually an IOException or ParsingException in Java
        print('MEI file is not valid.', file=sys.stderr)        # error message
        print(e.message(), file=sys.stderr)                     # actual exception message
        jpype.shutdownJVM()                                     # stop the JavaVM
        return 65

    if mei.isEmpty():                                           # check if MEI is empty
        print('MEI file could not be loaded.', file=sys.stderr) # error message
        jpype.shutdownJVM()                                     # stop the JavaVM
        return 66

    if resolve_copyofs:
        print('Processing MEI: resolving copyofs.')
        mei.resolveCopyofs()                                    # this call is part of the exportMsm() method but can also be called alone to expand the mei source and write it to the file system

    if add_ids:
        print('Processing MEI: adding xml:ids.')
        mei.addIds()                                            # generate ids for note, rest, mRest, multiRest, and chord elements that have no xml:id attribute

    if resolve_copyofs or add_ids:
        print('Writing MEI to file system: ' + mei.getFile().getPath().replace('.mei', '-meico.mei'))
        mei.writeMei()                                          # this outputs an expanded mei file with more xml:id attributes and resolved copyofs

    if not (export_msm or export_midi or export_wav):
        jpype.shutdownJVM()                                     # stop the JavaVM
        return 0

    print('Converting MEI to MSM.')
    msms = mei.exportMsm(720, dont_use_channel_10, not debug)   # usually, the application should use mei.exportMsm(720); the cleanup flag is just for debugging (in debug mode no cleanup is done)
    if msms.isEmpty():                                          # did something come out? if not
        print('No MSM data created.', file=sys.stderr)          # error message
        jpype.shutdownJVM()                                     # stop the JavaVM
        return 1

    if debug:
        print('Writing -debug.mei to file system.')
        mei.writeMei(mei.getFile().getPath().replace('.mei', '-debug.mei')) # After the msm export, there is some new stuff in the mei ... mainly the date and dur attribute at measure elements (handy to check for numeric problems that occured during conversion), some ids and expanded copyofs. This was required for the conversion and can be output with this function call. It is, however, mainly interesting for debugging.

    for i in range(msms.size()):                                # process all MSM objects just exported from MEI
        print('Processing MSM: movement ' + str(i))
        msm = msms.get(i)                                       # get the MSM instance

        print('Processing MSM: removing rests.')
        msm.removeRests()                                       # purge the data (some applications may keep the rests from the MEI; these should not call this function)

        print('Processing MSM: expanding sequencingMaps.')
        msm.resolveSequencingMaps()                             # instead of using sequencingMaps (which encode repetitions, dacapi etc.) resolve them and, hence, expand all scores and maps according to the sequencing information (be aware: the sequencingMaps are deleted because they no longer apply)

        if export_msm:
            print('Writing MSM to file system.')
            msm.writeMsm()                                      # write the MSM file to the file system

        if export_midi or export_wav or export_mp3:
            print('Converting MSM to MIDI.')
            midi = msm.exportMidi(float(tempo), not no_program_changes) # do the conversion to MIDI

            if export_midi:
                print('Writing MIDI to file system: ' + midi.getFile().getPath())
                try:
                    midi.writeMidi()                            # write midi file to the file system
                except jpype.JException(jpype.java.lang.IOException) as e:  # in case of a Java IOException
                    print(e.message, file=sys.stderr)           # print exception message

            if export_wav or export_mp3:
                print('Converting MIDI to Audio.')
                soundbank = os.path.realpath(soundbank)         # get absolute path (just to be sure)
                if os.path.isfile(soundbank):                   # if sound bank file does exist
                    audio = midi.exportAudio(File(soundbank))   # generate Audio object using the specified soundbank
                else:                                           # if no soundbank
                    audio = midi.exportAudio()                  # do the MIDI-to-audio rendering with Java's built-in soundfont

                if export_wav:
                    print('Writing Wave file to file system: ' + audio.getFile().getPath())
                    try:
                        audio.writeAudio()                          # write the Wave file
                    except jpype.JException(jpype.java.lang.IOException) as e:  # in case of a Java IOException
                        print(e.message, file=sys.stderr)           # print exception message

                if export_mp3:
                    print('Writing MP3 file to file system: ' + audio.getFile().getPath())
                    try:
                        audio.writeMp3()                            # write the MP3 file
                    except jpype.JException(jpype.java.lang.IOException) as e:  # in case of a Java IOException
                        print(e.message, file=sys.stderr)           # print exception message

    jpype.shutdownJVM()                                         # stop the JavaVM
    return 0


def main(arguments, mei_file):
    """
    Before starting the conversion itself, this function parses the command line arguments. It corresponds with meico's native command line mode.
    :param arguments: holds all command line arguments except the script itself and the final parameter, which is the MEI file reference
    :param mei_file: holds the MEI file reference
    :return:
    """

    parser = ArgumentParser()               # instantiate the command line argument parser

    # set the command line arguments
    parser.add_argument('-v', '--validate', action='store_true', default=False, help='Check validity of MEI file.')
    parser.add_argument('-a', '--add-ids', action='store_true', default=False, help='Add xml:ids to note, rest and chord elements in MEI, as far as they do not have an id; meico will output a revised MEI file.')
    parser.add_argument('-r', '--resolve-copy-ofs', action='store_true', default=False, help='Resolve elements with \'copyof\' attributes into selfcontained elements with own xml:id; meico will output a revised MEI file.')
    parser.add_argument('-m', '--msm', action='store_true', default=False, help='Convert to MSM.')
    parser.add_argument('-i', '--midi', action='store_true', default=False, help='Convert to MIDI (and internally to MSM).')
    parser.add_argument('-p', '--no-program-changes', action='store_true', default=False, help='Suppress program change events in MIDI.')
    parser.add_argument('-c', '--dont-use-channel-10', action='store_true', default=False, help='Do not use channel 10 (drum channel) in MIDI.')
    parser.add_argument('-t', '--tempo', action='store', default=100, help='Set MIDI tempo (bpm), default is 120 bpm.')
    parser.add_argument('-w', '--wav', action='store_true', default=False, help='Convert to Wave (and internally to MSM and MIDI).')
    parser.add_argument('-3', '--mp3', action='store_true', default=False, help='Convert to MP3 (and internally to MSM and MIDI).')
    parser.add_argument('-s', '--soundbank', action='store', default=None, help='Use a specific sound bank file (.sf2, .dls) for Wave conversion.')
    parser.add_argument('-d', '--debug', action='store_true', default=False, help='Write additional debug version of MEI and MSM.')

    args = parser.parse_args(arguments)     # parse the command line arguments
    if args.debug:                          # in debug mode
        print(args)                         # output the parsing result

    # call the conversion method
    return convert_mei(validate = args.validate,
                       add_ids = args.add_ids,
                       resolve_copyofs = args.resolve_copy_ofs,
                       export_msm = args.msm,
                       export_midi = args.midi,
                       no_program_changes = args.no_program_changes,
                       dont_use_channel_10 = args.dont_use_channel_10,
                       tempo = args.tempo,
                       export_wav= args.wav,
                       export_mp3=args.mp3,
                       soundbank = args.soundbank,
                       debug = args.debug,
                       mei_file = mei_file)


# entry point to this script
if __name__ == "__main__":
    main(sys.argv[1:-1], sys.argv[-1])
