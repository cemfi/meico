package meico.app;

/**
 * @author Axel Berndt
 */
public class InvalidFileTypeException extends Exception {

    public InvalidFileTypeException(String message) {
        super(message);
    }

    public InvalidFileTypeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
