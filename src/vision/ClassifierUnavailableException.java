package vision;

/**
 * Thrown when photo-based disease identification cannot run because
 * no real trained model backend is currently integrated. Callers
 * should surface this honestly to the user instead of guessing.
 */
public class ClassifierUnavailableException extends Exception {
    public ClassifierUnavailableException(String message) {
        super(message);
    }
}
