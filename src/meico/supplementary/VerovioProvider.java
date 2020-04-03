package meico.supplementary;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class provides methods to retreive verovio-toolkit.js.
 * @author Axel Berndt
 */
public class VerovioProvider {
    /**
     * This method retreives the latest online Verovio Toolkit JavaScript script. If not available, it returns the local version that ships with meico.
     * @param caller
     * @return the Verovio Toolkit JavaScript script or null if even the local version cannot be read
     */
    public static String getVerovio(Object caller) {
        String verovio = VerovioProvider.getOnlineVerovio();    // if the online version of Verovio is available, get it
        if (verovio == null) {                                  // if online Verovio is not available or the internal Verovio should be used
            System.out.println("Cannot access Verovio Toolkit online. Using local version instead.");
            verovio = VerovioProvider.getLocalVerovio(caller);
        }
        return verovio;
    }

    /**
     * This method reads the file "/resources/Verovio/verovio-toolkit.js" from the jar.
     * @return
     */
    public static String getLocalVerovio(Object caller) {
        InputStream is = caller.getClass().getResourceAsStream("/resources/Verovio/verovio-toolkit.js");  // open input stream

        // source of the following code block: https://stackoverflow.com/questions/309424/how-to-read-convert-an-inputstream-into-a-string-in-java
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = is.read(buffer)) != -1)
                result.write(buffer, 0, length);
            is.close();                 // close input stream
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return result.toString();       // return html code as string
    }

    /**
     * this method returns online available Verovio script
     * source: https://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
     * @return the Verovio Toolkit script or null if not available
     */
    public static String getOnlineVerovio() {
        String url = "https://www.verovio.org/javascript/develop/verovio-toolkit.js";
        try {
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
