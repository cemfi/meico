package meico.supplementary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class provides a method to convert an InputStream into a String.
 */
public class InputStream2StringConverter {
    /**
     * Convert the provided InputsStream into a String.
     * @param inputStream
     * @return the String representation of the InputStream
     * @throws IOException
     */
    public static String convert(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = inputStream.read(buffer)) != -1; )
            result.write(buffer, 0, length);

        // StandardCharsets.UTF_8.name() > JDK 7
        return result.toString("UTF-8");
    }
}
