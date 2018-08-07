package meico.app.gui;

import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * This class creates a WebView if Settings.useInternalWebView is set true. Otherwise it will redirect all URL calls to the system's default browser.
 * @author Axel Berndt
 */
public class WebBrowser extends VBox {
    private MeicoApp app;               // the parent application
    private WebEngine webEngine = null;

    /**
     * constructor
     * @param app
     */
    public WebBrowser(MeicoApp app) {
        super();
        this.app = app;

        if (Settings.useInternalWebView) {
            WebView browser = new WebView();
            browser.setStyle(Settings.WEB_BROWSER);
            this.minHeightProperty().bind(app.getStage().heightProperty().multiply(0.7));    // ensure that the WebView - when expanded - get at least 70% of window height

            this.webEngine = browser.getEngine();
            this.getChildren().addAll(browser);
            this.openURL("https://www.verovio.org/index.xhtml");                        // set a start page
        }
    }

    /**
     * call this method to go to a specific URL
     * @param url
     */
    public synchronized void openURL(String url) {
        if (this.webEngine != null)                         // if the internal WebView is set true
            this.webEngine.load(url);                       // use it to open the URL
        else                                                // otherwise
            this.app.getHostServices().showDocument(url);   // use the system's default browser
    }

    /**
     * prints the specified string in the WebView
     * @param content
     * @param formatting
     */
    public synchronized void printContent(String content, boolean formatting) {
        String c = content;
        if (formatting) {
            c = content.replaceAll("\n", "<br>").replaceAll("\t", "&emsp;").replaceAll("\\s", "&nbsp;");
        }
        if (this.webEngine != null)
            this.webEngine.loadContent(c);
    }
}
