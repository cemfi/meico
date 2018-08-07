package meico.app.gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.util.Duration;
import meico.audio.Audio;
import meico.audio.AudioPlayer;
import meico.midi.Midi;
import meico.midi.MidiPlayer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.io.File;

/**
 * The Player class provides the GUI for audio and Midi playback.
 * @author Axel Berndt
 */
public class Player extends HBox {
    private MeicoApp app;                                   // this is a link to the parent application

    private MidiPlayer midiplayer = null;                   // depending on the data to be played back, the Midi or ...
    private AudioPlayer audioplayer;                        // audio player will be instantiated
    private Object currentlyActivePlayer = null;            // this iwill indicate the player that is currently in use
    private MetaEventListener midiplaybackListener;         // this listener reacts on events related to midi playback
    private LineListener audioplaybackListener;             // this listener reacts on events related to audio playback

    private Slider slider;                                  // the slider that indicates the playback position
    private Timeline sliderTimeline = null;                 // this animates the slider during playback
    private Button playButton;                              // the play/stop button
    private boolean mouseOverPlayButton = false;            // this is needed to keep track of when the mouse is over the button and when not in relation to when it is clicked/released
    private Button settingsButton;                          // this button opens the meico settings dialog
    private boolean mouseOverSettingsButton = false;        // this is needed to keep track of when the mouse is over the button and when not in relation to when it is clicked/released

    /**
     * constructor
     */
    public Player(MeicoApp app) {
        super();

        this.app = app;

        // initialize the playback listeners
        this.audioplaybackListener = this.initAudioPlaybackListener();
        this.midiplaybackListener = this.initMidiPlaybackListener();

        // initialize midi and audio player
        try {
            this.midiplayer = new MidiPlayer();
            this.midiplayer.getSequencer().addMetaEventListener(this.midiplaybackListener); // this listener needs to be assigned only once as the midi player keeps its one and only sequencer
            if (Settings.soundbank != null)
                this.midiplayer.loadSoundbank(Settings.soundbank);
            else
                this.midiplayer.loadDefaultSoundbank();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
        this.audioplayer = new AudioPlayer();

        // general style settings of the player
        this.setStyle(Settings.PLAYER);

        // create the player's elements
        this.slider = this.makeSlider();
        this.playButton = this.makePlayButton();
        this.settingsButton = this.makeSettingsButton();

        this.getChildren().addAll(this.playButton, this.slider, this.settingsButton);       // from left to right add the play button and the slider
    }

    /**
     * switch the soundbank for MIDI synthesis
     * @param file
     * @return
     */
    protected synchronized boolean setSoundbank(File file) {
        if (this.midiplayer == null)
            return false;

        if (!this.midiplayer.loadSoundbank(file)) {
            this.midiplayer.loadDefaultSoundbank();
            return false;
        }
        return true;
    }

    /**
     * This must be called on termination of the application in order to properly close the midi player.
     * It helps keeping the constructor method less cluttered.
     */
    public synchronized void close() {
        this.midiplayer.close();
    }

    /**
     * this initializes the midi playback listener that ensures that the playback button gets reset when playback finishes
     * @return
     */
    private synchronized MetaEventListener initMidiPlaybackListener() {
        MetaEventListener listener = new MetaEventListener() {  // Add a listener for meta message events to detect when ...
            public synchronized void meta(MetaMessage event) {
                if (currentlyActivePlayer == midiplayer) {
                    if (event.getType() == 47) {                                    // the sequencer is done playing
                        Platform.runLater(() -> {                                   // this has to be put around all operations that have to be performed within the JavaFX thread
                            setPlayButton(false);                                   // these are the actual operations
                            sliderTimeline.stop();                                  // stop slider timeline
                            slider.setValue(midiplayer.getMicrosecondPosition());   //
                        });                                                         //
                        midiplayer.setTickPosition(0);
                    }
                }
            }
        };
        return listener;
    }

    /**
     * this.initializes the audio playback listener that ensures that the playback button gets reset when playback finishes
     * @return
     */
    private synchronized LineListener initAudioPlaybackListener() {
        LineListener listener = new LineListener() {
            @Override
            public synchronized void update(LineEvent event) {
                if (currentlyActivePlayer == audioplayer) {
                    LineEvent.Type eventType = event.getType();
//                if (eventType == LineEvent.Type.START) {
//                    setPlayButton(true);
//                }
                    if ((eventType == LineEvent.Type.STOP) || (eventType == LineEvent.Type.CLOSE)) {    // audio playback stopped
                        Platform.runLater(() -> {                                   // this has to be put around all operations that have to be performed within the JavaFX thread
                            setPlayButton(false);                                   // these are the actual operations
                            sliderTimeline.stop();                                  // stop slider timeline
                            slider.setValue(audioplayer.getMicrosecondPosition());  //
                        });                                                         //
                    }
                }
            }
        };
        return listener;
    }

    /**
     * define the setting button
     * @return
     */
    private synchronized Button makeSettingsButton() {
        Button settings = new Button("\uf0ad");
        settings.setFont(Settings.getIconFont(Font.getDefault().getSize() * 2, this));
        settings.setStyle(Settings.ICON);

        settings.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            if (settings.isPressed())
                settings.setStyle(Settings.ICON_PRESSED);
            else
                settings.setStyle(Settings.ICON_MOUSE_OVER);
            this.mouseOverSettingsButton = true;
            this.app.getStatuspanel().setMessage("Meico settings");
            e.consume();
        });

        settings.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            if (settings.isPressed())
                settings.setStyle(Settings.ICON_PRESSED);
            else
                settings.setStyle(Settings.ICON);
            this.mouseOverSettingsButton = false;
            this.app.getStatuspanel().setMessage("");
            e.consume();
        });

        settings.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> settings.setStyle(Settings.ICON_PRESSED));

        settings.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (this.mouseOverSettingsButton) {
                settings.setStyle(Settings.ICON_MOUSE_OVER);
                Settings.openSettingsDialog(this.app);
            }
            else
                settings.setStyle(Settings.ICON);
            e.consume();
        });

        return settings;
    }

    /**
     * define the play/stop button
     * @return
     */
    private synchronized Button makePlayButton() {
        Button play = new Button("\uf04b");
        play.setFont(Settings.getIconFont(Font.getDefault().getSize() * 2, this));
        play.setStyle(Settings.ICON);

        play.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            if (play.isPressed())
                play.setStyle(Settings.ICON_PRESSED);
            else
                play.setStyle(Settings.ICON_MOUSE_OVER);
            this.mouseOverPlayButton = true;
            this.app.getStatuspanel().setMessage("Start and stop playback");
            e.consume();
        });

        play.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            if (play.isPressed())
                play.setStyle(Settings.ICON_PRESSED);
            else
                play.setStyle(Settings.ICON);
            this.mouseOverPlayButton = false;
            this.app.getStatuspanel().setMessage("");
            e.consume();
        });

        play.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            play.setStyle(Settings.ICON_PRESSED);
            e.consume();
        });

        play.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (this.mouseOverPlayButton) {
                play.setStyle(Settings.ICON_MOUSE_OVER);
                if (this.isPlaying()) {
                    this.stop();
                }
                else {
                    this.play();
                }
            }
            else
                play.setStyle(Settings.ICON);
            e.consume();
        });

        return play;
    }

    /**
     * set the playback button symbol to play (false) or stop (true)
     * @param play
     */
    public synchronized void setPlayButton(boolean play) {
        if (play)
            this.playButton.setText("\uf04d");
        else
            this.playButton.setText("\uf04b");
    }

    /**
     * define the playback timeline slider
     * @return
     */
    private synchronized Slider makeSlider() {
        Slider slider = new Slider();

        // layout
        slider.setStyle(Settings.SLIDER);
        HBox.setHgrow(slider, Priority.ALWAYS);                  // this autoresizes the slider

        // operation
        slider.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (this.sliderTimeline != null)
                this.sliderTimeline.stop();  // stop slider timeline
            e.consume();
        });

        // when the slider is set by the user, playback jumps to the set position
        slider.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
//                this.app.statuspanel.setMessage("DEBUG: set playback position " + (slider.getValue()/1000000) + " / " + (int)(slider.getMax()/1000000));
            long position = Math.round(slider.getValue());
            if (this.currentlyActivePlayer == this.midiplayer) {
                this.midiplayer.setMicrosecondPosition(position);
                if (this.midiplayer.isPlaying())
                    this.letTheSliderSlide(position, this.midiplayer);
                e.consume();
                return;
            }
            if (this.currentlyActivePlayer == this.audioplayer) {
                this.audioplayer.setMicrosecondPosition(position);
                if (this.audioplayer.isPlaying())
                    this.letTheSliderSlide(position, this.audioplayer);
                e.consume();
                return;
            }
            e.consume();
        });

        // this variant of the above functionality produces sound glitches, but it works, too
//        slider.valueProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                app.statuspanel.setMessage("DEBUG: set playback position " + (newValue.intValue()/1000000) + " / " + (int)(slider.getMax()/1000000));
//                if (currentlyActivePlayer == midiplayer) {
//                    midiplayer.setMicrosecondPosition(newValue.longValue());
//                    e.consume();
//                    return;
//                }
//                if (currentlyActivePlayer == audioplayer) {
//                    audioplayer.setMicrosecondPosition(newValue.longValue());
//                    e.consume();
//                    return;
//                }
//                e.consume();
//            }
//        });

        return slider;
    }

    /**
     * Is playback running?
     * @return
     */
    public synchronized boolean isPlaying() {
        if (this.currentlyActivePlayer == null)
            return false;
        if (this.currentlyActivePlayer == this.midiplayer)
            return this.midiplayer.isPlaying();
        if (this.currentlyActivePlayer == this.audioplayer)
            return this.audioplayer.isPlaying();
        return false;
    }

    /**
     * continue playback of the currently active player (midi or audio) at its current playback position
     */
    public synchronized void play() {
        if (this.currentlyActivePlayer == null) {
            this.app.getStatuspanel().setMessage("No music chosen to be played back");
            this.setPlayButton(false);
        } else if (this.currentlyActivePlayer == this.midiplayer) {
            this.midiplayer.play();
            this.letTheSliderSlide(this.midiplayer.getMicrosecondPosition(), this.midiplayer);    // trigger the slider to start sliding
            this.setPlayButton(true);
        } else if (this.currentlyActivePlayer == audioplayer) {
            this.audioplayer.play();
            this.letTheSliderSlide(this.audioplayer.getMicrosecondPosition(), this.audioplayer);    // trigger the slider to start sliding
            this.setPlayButton(true);
        }
    }

    /**
     * stop playback (it actually pauses playback)
     */
    public synchronized void stop() {
        if (this.currentlyActivePlayer == null) {
            ;
        } else if (this.currentlyActivePlayer == this.midiplayer) {
            this.midiplayer.pause();
        } else if (this.currentlyActivePlayer == this.audioplayer) {
            this.audioplayer.pause();
        }
        this.sliderTimeline.stop();
        this.setPlayButton(false);
    }

    /**
     * don't just pause but stop the playback
     */
    private synchronized void fullStop() {
        if (this.audioplayer != null) {
            if (this.audioplayer.getAudioClip() != null)
                this.audioplayer.getAudioClip().removeLineListener(this.audioplaybackListener);
            this.audioplayer.stop();
        }
        if (this.midiplayer != null) {
            this.midiplayer.stop();
        }
        this.sliderTimeline.stop();
        this.setPlayButton(false);
        this.currentlyActivePlayer = null;
    }

    /**
     * a convenient play method that detects the type of the object to be played back automatically
     * @param o
     */
    public synchronized void play(Object o) {
        if (o instanceof Midi)
            this.play((Midi)o);
        else if (o instanceof Audio)
            this.play((Audio)o);
    }

    /**
     * stop the current playback, load and play the new midi data
     * @param midi
     */
    public synchronized void play(Midi midi) {
//        this.fullStop();
        if (this.audioplayer != null) {
            if (this.audioplayer.getAudioClip() != null)
                this.audioplayer.getAudioClip().removeLineListener(this.audioplaybackListener);
            this.audioplayer.stop();
            if (sliderTimeline != null)
                this.sliderTimeline.stop();
        }

        try {
            this.midiplayer.play(midi);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            this.setPlayButton(false);
            this.currentlyActivePlayer = null;
            this.app.getStatuspanel().setMessage("Starting audio playback failed");
            return;
        }

        this.slider.setMax(this.midiplayer.getMicrosecondLength());
        this.letTheSliderSlide(0, this.midiplayer);    // trigger the slider to start sliding

        this.setPlayButton(true);
        this.currentlyActivePlayer = this.midiplayer;
    }

    /**
     * stop the current playback, load and play the new audio data
     * @param audio
     */
    public synchronized void play(Audio audio) {
//        this.fullStop();
        if (this.midiplayer != null) {
            this.midiplayer.stop();
            if (sliderTimeline != null)
                this.sliderTimeline.stop();
        }

        if (this.audioplayer.getAudioClip() != null)
            this.audioplayer.getAudioClip().removeLineListener(this.audioplaybackListener);

        this.audioplayer.play(audio);
        if (!this.audioplayer.isPlaying()) {
            this.setPlayButton(false);
            this.currentlyActivePlayer = null;
            this.app.getStatuspanel().setMessage("Starting audio playback failed");
            return;
        }

        this.slider.setMax(this.audioplayer.getMicrosecondLength());
        this.letTheSliderSlide(0, this.audioplayer);    // trigger the slider to start sliding

        this.setPlayButton(true);
        this.currentlyActivePlayer = this.audioplayer;
        this.audioplayer.getAudioClip().addLineListener(this.audioplaybackListener);
    }

    /**
     * This makes the slider slide for as long as the player needs to reach the end of the music.
     * So this is not really monitoring the player's playback position. But it is faked well enough.
     * @param from start time in microseconds
     * @param midiplayer
     */
    private synchronized void letTheSliderSlide(long from, MidiPlayer midiplayer) {
        this.sliderTimeline = new Timeline();
        this.sliderTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(slider.valueProperty(), from)),
                new KeyFrame(Duration.millis(((double)midiplayer.getMicrosecondLength() - from) / 1000), new KeyValue(slider.valueProperty(), midiplayer.getMicrosecondLength()))
        );
        this.sliderTimeline.play();
    }

    /**
     * This makes the slider slide for as long as the player needs to reach the end of the music.
     * So this is not really monitoring the player's playback position. But it is faked well enough.
     * @param from start time in microseconds
     * @param audioplayer
     */
    private synchronized void letTheSliderSlide(long from, AudioPlayer audioplayer) {
        this.sliderTimeline = new Timeline();
        this.sliderTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(slider.valueProperty(), from)),
                new KeyFrame(Duration.millis(((double)audioplayer.getMicrosecondLength() - from) / 1000), new KeyValue(slider.valueProperty(), audioplayer.getMicrosecondLength()))
        );
        this.sliderTimeline.play();
    }
}
