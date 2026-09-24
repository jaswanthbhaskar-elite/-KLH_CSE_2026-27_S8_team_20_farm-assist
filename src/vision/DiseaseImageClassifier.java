package vision;

import java.io.File;

/**
 * Interface boundary for photo-based crop disease identification.
 *
 * The rest of the application depends ONLY on this interface, never
 * on a specific model implementation, so a real inference backend
 * (an ONNX Runtime Java classifier loading a pretrained plant-disease
 * CNN) can be dropped in without touching GUI code.
 *
 * IMPORTANT (academic honesty requirement):
 * Any implementation of this interface must perform genuine image
 * classification using an actual trained model. Implementations must
 * NOT guess based on file name, average pixel color, or any other
 * non-model heuristic, and must NOT claim a heuristic is AI-based
 * detection.
 */
public interface DiseaseImageClassifier {

    /**
     * Classifies the given image file.
     *
     * @throws ClassifierUnavailableException if no real model backend
     *         is available at all (dependency/model files missing).
     * @throws ClassificationException if a backend is available but
     *         this specific attempt failed (unreadable image, decode
     *         failure, inference error).
     */
    PredictionResult classify(File imageFile)
            throws ClassifierUnavailableException, ClassificationException;

    /** Whether a real, working model backend is currently available. */
    boolean isAvailable();

    /**
     * Human-readable reason the backend is unavailable, for display to
     * the user. Only meaningful when isAvailable() is false. Should
     * describe what's actually missing on THIS machine (e.g. "model
     * file not found at ..."), not a generic message copied from
     * somewhere else, since the real cause varies by environment.
     */
    String getUnavailableReason();
}