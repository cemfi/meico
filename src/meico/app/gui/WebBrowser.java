package meico.app.gui;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class creates a WebView if Settings.useInternalWebView is set true. Otherwise it will redirect all URL calls to the system's default browser.
 * @author Axel Berndt
 */
public class WebBrowser extends StackPane {
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
            this.minHeightProperty().bind(app.getStage().heightProperty().multiply(0.7));   // ensure that the WebView - when expanded - get at least 70% of window height

            this.webEngine = browser.getEngine();
            this.webEngine.setJavaScriptEnabled(true);                                      // JavaScript should be enabled by default, this is just to be sure that it is
            this.getChildren().add(browser);
            this.openURL("https://www.verovio.org/index.xhtml");                            // set a start page
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
     * @param showPlainText if false the content will be interpreted as HTML, if true the content will be shown as plain text with no formatting
     */
    public synchronized void printContent(String content, boolean showPlainText) {
        String c = content;
        if (this.webEngine != null)
            this.webEngine.loadContent(c, (showPlainText) ? "text/plain" : "text/html");    // show content as plain text or as html
    }

    /**
     * prints the specified string in the WebView as HTML
     * @param content
     */
    public synchronized void printContent(String content) {
        this.printContent(content, false);
    }

    /**
     * checks internet connection and availability of a desired resource
     * @param urlString
     * @return
     */
    public static boolean isNetAvailable(String urlString) {
        try {
            URL url = new URL(urlString);
            final URLConnection conn = url.openConnection();
            conn.connect();
            conn.getInputStream().close();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }
}
