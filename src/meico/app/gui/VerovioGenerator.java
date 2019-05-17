package meico.app.gui;

import meico.svg.SvgCollection;

import java.io.*;

/**
 * This class provides tools to generate Verovio code (for score rendering).
 * @author Axel Berndt
 */

public class VerovioGenerator {
    /**
     * This method reads the file "/resources/Verovio/verovio.html" from the jar.
     * This file provides the prototype html document. It contains a placeholder "replaceMe" to insert the MEI code.
     * @param caller
     * @return
     * @throws IOException
     */
    public static String readPrototypeHtml(Object caller) throws IOException {
        InputStream is = caller.getClass().getResourceAsStream("/resources/Verovio/verovio.html");  // open input stream

        // source of the following code block: https://stackoverflow.com/questions/309424/how-to-read-convert-an-inputstream-into-a-string-in-java
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1)
            result.write(buffer, 0, length);

        is.close();                 // close input stream
        return result.toString();   // return html code as string
    }

    /**
     * This generates HTML/JavaScript code for a Verovio score rendering.
     * @param mei the music to be rendered
     * @param caller
     * @return
     */
    public static String generate(String mei, Object caller) {
        return VerovioGenerator.generate(mei, Settings.useLatestVerovio, Settings.oneLineScore, Settings.scoreFont, caller);
    }

    /**
     * This generates HTML/JavaScript code for a Verovio score rendering.
     * @param mei the music to be rendered
     * @param useLatestVerovio
     * @param oneLineScore
     * @param font the font used for score rendering
     * @param caller
     * @return
     */
    public static String generate(String mei, boolean useLatestVerovio, boolean oneLineScore, String font, Object caller) {
        String html;
        try {
            html = VerovioGenerator.readPrototypeHtml(caller);                                              // load the prototype html
        } catch (IOException e) {                                                                           // if failed
            e.printStackTrace();                                                                            // print exception message to the commandline/log file
            return "<html>Error: Failed to read prototype HTML. <br><br> " + e.toString() + "</html>";      // print the exception message also to the WebView to give an immediate feedback
        }

        if (!useLatestVerovio || !WebBrowser.isNetAvailable("https://www.verovio.org/javascript/develop/verovio-toolkit.js"))                                                      // if the internal Verovio should be used or no internet connection available
            html = html.replace("https://www.verovio.org/javascript/develop/verovio-toolkit.js", caller.getClass().getResource("/resources/Verovio/verovio-toolkit.js").toExternalForm());  // replace the online reference in the HTML by the local reference

        html = html.replace("oneLineScore", "" + oneLineScore)                                              // the oneLineScore flag is also set
                .replace("Leipzig", font)
                .replace("MeiCode", mei.replace("\n", "").replace("\r", "").replace("\"", "\\\""));         // replace the placeholder string "MeiCode" by actual MEI code; that MEI code should be free of linebreaks and quotation marks ("), hence these are replaced before adding it to the html

        return html;
    }

    /**
     *
     * @param svgs
     * @param caller
     * @return
     */
    public static String generate(SvgCollection svgs, boolean oneLineScore, Object caller) {
        String html;
        try {
            html = VerovioGenerator.readPrototypeHtml(caller);                                              // load the prototype html
        } catch (IOException e) {                                                                           // if failed
            e.printStackTrace();                                                                            // print exception message to the commandline/log file
            return "<html>Error: Failed to read prototype HTML. <br><br> " + e.toString() + "</html>";      // print the exception message also to the WebView to give an immediate feedback
        }

        html = html.replace("oneLineScore", "" + oneLineScore);                                             // the oneLineScore flag is also set

        String svgCode = "";
        for (int i=0; i < svgs.size(); ++i) {
            String svg = svgs.get(i).toXML();
            svg = svg.replace("\n", "").replace("\r", "").replace("\"", "\\\"");
            if (i == 0)
                svgCode = svgCode.concat("\"" + svg + "\"");
            else
                svgCode = svgCode.concat(", \"" + svg + "\"");
        }

        html = html.replace("var svg = []", "var svg = [" + svgCode + "]");

        return html;
    }
}
