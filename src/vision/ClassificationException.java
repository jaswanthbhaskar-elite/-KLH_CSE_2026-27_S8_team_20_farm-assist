package vision;

/**
 * Thrown when a real classifier backend IS available, but this
 * particular attempt failed — e.g. the selected file isn't a
 * readable image, or the model produced no usable output.
 *
 * Distinct from ClassifierUnavailableException, which means "no
 * backend at all". This means "backend present, this attempt failed".
 * Callers should show the message without a raw stack trace.
 */
public class ClassificationException extends Exception {
    public ClassificationException(String message) {
        super(message);
    }

    public ClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}