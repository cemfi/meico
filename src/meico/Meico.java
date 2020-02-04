package meico;

import meico.app.gui.MeicoApp;

/**
 * This class provides the current version number of meico and a convenient way to launch meico's desktop application.
 * @author Axel Berndt
 */
public class Meico {
    public static final String version = "0.7.6";

    /**
     * A convenient launcher for the meico gui app.
     * Call it by <code>Meico.launch()</code>.
     */
    public static void launch() {
        MeicoApp.launch(MeicoApp.class);
    }

    /**
     * A convenient, more elaborate launcher for the meico gui app.
     * Call it by <code>Meico.launch("My window title")</code>.
     * @param windowTitle the window title string
     */
    public static void launch(String windowTitle) {
        MeicoApp.launch(MeicoApp.class, windowTitle);
    }
}
