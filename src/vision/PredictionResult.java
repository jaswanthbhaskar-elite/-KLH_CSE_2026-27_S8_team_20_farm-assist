package vision;

/**
 * Result of a single image classification attempt.
 *
 * rawModelLabel : the class label exactly as produced by the model
 *                 (e.g. "Tomato___Late_blight" for a PlantVillage-style
 *                 model). Never null when a prediction succeeds.
 * diseaseName   : rawModelLabel mapped to a name that exists in
 *                 data/diseases.txt, via DiseaseClassMapper. Null if
 *                 no mapping entry was found for this raw label.
 * confidence    : softmax probability of the predicted class, in [0,1].
 */
public class PredictionResult {
    public final String rawModelLabel;
    public final String diseaseName;
    public final double confidence;

    public PredictionResult(String rawModelLabel, String diseaseName, double confidence) {
        this.rawModelLabel = rawModelLabel;
        this.diseaseName = diseaseName;
        this.confidence = confidence;
    }
}