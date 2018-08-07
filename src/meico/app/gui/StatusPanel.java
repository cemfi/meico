package meico.app.gui;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * This class creates a status bar.
 * @author Axel Berndt
 */
public class StatusPanel extends HBox {
    private MeicoApp app;                                // this is a link to the parent application
    private TextField message;                           // this text field prints status messages
    private Button clearScreen;                          // this button clears the workspace
    private boolean mouseOverClearScreenButton = false;  // this is needed to keep track of when the mouse is over the button and when not in relation to when it is clicked/released

    /**
     * constructor
     */
    public StatusPanel(MeicoApp app) {
        super();

        this.app = app;

        this.message = this.makeMessageBar();                           // create the message bar
        HBox.setHgrow(this.message, Priority.ALWAYS);                   // this autoresizes the message field

        this.clearScreen = this.makeClearScreenButton();                // create the button that clears the workspace

        this.getChildren().addAll(this.message, this.clearScreen);      // from left to right add the message field and the clear screen button
    }

    /**
     * set the message that is displayed in the status bar
     * @param message
     */
    public synchronized void setMessage(String message) {
        if (message != null)
            this.message.setText(message);
    }

    /**
     * define the status bars's message field
     * @return
     */
    private synchronized TextField makeMessageBar() {
        TextField message = new TextField("");
        message.setEditable(false);                                 // the status bar is not editable
//        message.prefWidthProperty().bind(this.widthProperty());    // it resizes automatically
//        message.setFont(Settings.getStandardFont());
        message.setStyle(Settings.STATUSBAR);
        return message;
    }

    /**
     * define the clear screen button
     * @return
     */
    private synchronized Button makeClearScreenButton() {
        Button clearScreen = new Button("  ×  ");
        clearScreen.setStyle(Settings.MINI_BUTTON);

        clearScreen.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            if (clearScreen.isPressed())
                clearScreen.setStyle(Settings.MINI_BUTTON_PRESSED);
            else
                clearScreen.setStyle(Settings.MINI_BUTTON_MOUSE_OVER);
            this.mouseOverClearScreenButton = true;
            this.setMessage("Clear workspace / Close all");
            e.consume();
        });

        clearScreen.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            if (clearScreen.isPressed())
                clearScreen.setStyle(Settings.MINI_BUTTON_PRESSED);
            else
                clearScreen.setStyle(Settings.MINI_BUTTON);
            this.mouseOverClearScreenButton = false;
            this.setMessage("");
            e.consume();
        });

        clearScreen.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> clearScreen.setStyle(Settings.MINI_BUTTON_PRESSED));

        clearScreen.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (this.mouseOverClearScreenButton) {
                clearScreen.setStyle(Settings.MINI_BUTTON_MOUSE_OVER);
                this.app.getWorkspace().clearAll();
            }
            else
                clearScreen.setStyle(Settings.MINI_BUTTON);
            e.consume();
        });

        return clearScreen;
    }
}
