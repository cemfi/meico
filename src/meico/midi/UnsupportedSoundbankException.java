package meico.midi;

/**
 * Created by Axel Berndt on 19.09.2016.
 */
public class UnsupportedSoundbankException extends Exception {

    public UnsupportedSoundbankException(String message) {
        super(message);
    }

    public UnsupportedSoundbankException(String message, Throwable throwable) {
        super(message, throwable);
    }}
