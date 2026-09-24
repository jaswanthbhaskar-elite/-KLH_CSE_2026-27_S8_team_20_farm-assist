package vision;

import java.io.File;

/**
 * Honest placeholder implementation used whenever no real inference
 * backend could be loaded — either because the ONNX Runtime Java
 * dependency isn't on the classpath at all, or because the real
 * OnnxDiseaseClassifier failed to initialize (model file missing,
 * labels file missing, etc.).
 *
 * WHY THIS EXISTS (do not remove without reading this):
 * Real photo-based disease identification requires:
 *   1. A pretrained image-classification model (this project uses
 *      linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification
 *      from Hugging Face — a MobileNetV2 fine-tuned on the PlantVillage
 *      dataset, 38 classes, 95.4% eval accuracy — exported to ONNX).
 *   2. An offline Java inference runtime: ONNX Runtime for Java
 *      (com.microsoft.onnxruntime:onnxruntime, Maven Central).
 *
 * This class deliberately does NOT fake a prediction (no color
 * heuristics, no filename matching, no random guesses) because doing
 * so would misrepresent the project's actual capabilities.
 *
 * See README_PHOTO_ID_SETUP.md at the project root for the exact,
 * step-by-step setup process to enable the real classifier.
 */
public class UnavailableDiseaseClassifier implements DiseaseImageClassifier {

    private final String reason;

    /** Default reason: the ONNX Runtime Java class could not even be found. */
    public UnavailableDiseaseClassifier() {
        this("Photo-based disease identification is not enabled in this build. " +
                "The ONNX Runtime Java library and/or the exported model files were not " +
                "found. See README_PHOTO_ID_SETUP.md at the project root for exact setup steps.");
    }

    /** Use this constructor to report a specific, environment-accurate reason. */
    public UnavailableDiseaseClassifier(String reason) {
        this.reason = reason;
    }

    @Override
    public PredictionResult classify(File imageFile) throws ClassifierUnavailableException {
        throw new ClassifierUnavailableException(reason);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getUnavailableReason() {
        return reason;
    }
}