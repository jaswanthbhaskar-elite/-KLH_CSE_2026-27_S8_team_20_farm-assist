package vision;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Map;

/**
 * Real photo-based disease classifier backed by ONNX Runtime for Java.
 *
 * MODEL: linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification
 * (Hugging Face) — a MobileNetV2 fine-tuned on the Kaggle "New Plant
 * Diseases Dataset" version of PlantVillage. 38 classes (including
 * healthy leaves). Reported eval accuracy: 95.4%.
 * https://huggingface.co/linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification
 *
 * That repository ships PyTorch weights, not ONNX. Convert once with
 * Hugging Face's official Optimum exporter (see README_PHOTO_ID_SETUP.md):
 *     pip install optimum[exporters]
 *     optimum-cli export onnx --model linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification model/
 *
 * PREPROCESSING (must match MobileNetV2ImageProcessor's documented
 * defaults exactly, from huggingface/transformers source —
 * image_processing_mobilenet_v2_fast.py):
 *   - resize: shortest edge to 256px, preserving aspect ratio
 *   - center crop: 224 x 224
 *   - rescale: pixel values / 255.0
 *   - normalize: mean = [0.5, 0.5, 0.5], std = [0.5, 0.5, 0.5]
 *     (this is IMAGENET_STANDARD_MEAN/STD, NOT the more common
 *     IMAGENET_DEFAULT_MEAN/STD [0.485,0.456,0.406]/[0.229,0.224,0.225]
 *     — MobileNetV2's processor specifically uses the STANDARD pair)
 *   - layout: NCHW, i.e. [1, 3, 224, 224], channel order R,G,B
 *
 * Dependency: com.microsoft.onnxruntime:onnxruntime (Maven Central).
 * This class is intentionally OUTSIDE src/ (see src-optional/) so the
 * project's existing plain-javac build of src/ keeps working without
 * this dependency on the classpath. See README_PHOTO_ID_SETUP.md.
 */
public class OnnxDiseaseClassifier implements DiseaseImageClassifier {

    private static final int TARGET_SIZE = 224;
    private static final int RESIZE_SHORT_EDGE = 256;
    private static final float MEAN = 0.5f;
    private static final float STD = 0.5f;

    private OrtEnvironment env;
    private OrtSession session;
    private DiseaseClassMapper mapper;
    private String unavailableReason;

    public OnnxDiseaseClassifier(String modelPath, String labelsPath, String mappingPath) {
        File modelFile = new File(modelPath);
        if (!modelFile.isFile()) {
            unavailableReason = "Model file not found at " + modelFile.getAbsolutePath() +
                    ". Export it first (see README_PHOTO_ID_SETUP.md): " +
                    "optimum-cli export onnx --model linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification model/";
            return;
        }

        try {
            mapper = new DiseaseClassMapper(labelsPath, mappingPath);
        } catch (Exception e) {
            unavailableReason = "Could not read label/mapping files (" + labelsPath + ", " +
                    mappingPath + "): " + e.getMessage();
            return;
        }

        if (mapper.getClassCount() == 0) {
            unavailableReason = "Label file " + labelsPath + " is empty. Run " +
                    "tools/extract_labels.py first (see README_PHOTO_ID_SETUP.md).";
            return;
        }

        try {
            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            session = env.createSession(modelPath, options);
        } catch (OrtException e) {
            unavailableReason = "Failed to load ONNX model at " + modelPath + ": " + e.getMessage();
            session = null;
        }
    }

    @Override
    public boolean isAvailable() {
        return session != null;
    }

    @Override
    public String getUnavailableReason() {
        return unavailableReason;
    }

    @Override
    public PredictionResult classify(File imageFile)
            throws ClassifierUnavailableException, ClassificationException {

        if (session == null) {
            throw new ClassifierUnavailableException(unavailableReason);
        }

        float[] inputData = preprocess(imageFile);

        try (OnnxTensor inputTensor = OnnxTensor.createTensor(
                env, FloatBuffer.wrap(inputData), new long[]{1, 3, TARGET_SIZE, TARGET_SIZE})) {

            String inputName = session.getInputNames().iterator().next();
            Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, inputTensor);

            try (OrtSession.Result result = session.run(inputs)) {
                // Iterate rather than index positionally — documented,
                // version-stable way to read the first (only) output.
                OnnxValue outputValue = result.iterator().next().getValue();
                float[][] logits = (float[][]) outputValue.getValue();
                float[] scores = logits[0];

                float[] probabilities = softmax(scores);

                int bestIndex = 0;
                for (int i = 1; i < probabilities.length; i++) {
                    if (probabilities[i] > probabilities[bestIndex]) bestIndex = i;
                }
                System.out.println("TOP 5 PREDICTIONS:");

int[] topIndices = new int[5];

for (int j = 0; j < 5; j++) {
    int best = -1;

    for (int i = 0; i < probabilities.length; i++) {
        boolean alreadyUsed = false;

        for (int k = 0; k < j; k++) {
            if (topIndices[k] == i) {
                alreadyUsed = true;
                break;
            }
        }

        if (!alreadyUsed && (best == -1 || probabilities[i] > probabilities[best])) {
            best = i;
        }
    }

    topIndices[j] = best;

    System.out.printf(
        "%d. %s = %.2f%%%n",
        j + 1,
        mapper.getRawLabel(best),
        probabilities[best] * 100
    );
}


                String rawLabel = mapper.getRawLabel(bestIndex);
                if (rawLabel == null) {
                    throw new ClassificationException(
                            "Model returned class index " + bestIndex +
                            " which has no entry in model/labels.txt (expected " +
                            mapper.getClassCount() + " classes). The labels file may not " +
                            "match this model — re-run tools/extract_labels.py.");
                }

                String diseaseName = mapper.getDiseaseName(rawLabel);
                System.out.println("RAW MODEL LABEL = [" + rawLabel + "]");
System.out.println("MAPPED DISEASE = [" + diseaseName + "]");
System.out.println("CONFIDENCE = " + probabilities[bestIndex]);
                return new PredictionResult(rawLabel, diseaseName, probabilities[bestIndex]);
            }
        } catch (OrtException e) {
            throw new ClassificationException("Inference failed: " + e.getMessage(), e);
        }
    }

    private float[] softmax(float[] logits) {
        float max = Float.NEGATIVE_INFINITY;
        for (float v : logits) if (v > max) max = v;
        double sum = 0;
        double[] exps = new double[logits.length];
        for (int i = 0; i < logits.length; i++) {
            exps[i] = Math.exp(logits[i] - max);
            sum += exps[i];
        }
        float[] result = new float[logits.length];
        for (int i = 0; i < logits.length; i++) {
            result[i] = (float) (exps[i] / sum);
        }
        return result;
    }

    /**
     * Loads and preprocesses the image to match MobileNetV2ImageProcessor's
     * documented pipeline exactly: resize shortest edge to 256 (preserving
     * aspect ratio), center-crop 224x224, rescale to [0,1], normalize with
     * mean=0.5/std=0.5 per channel, and lay out as NCHW float32.
     */
    private float[] preprocess(File imageFile) throws ClassificationException {
        BufferedImage original;
        try {
            original = ImageIO.read(imageFile);
        } catch (Exception e) {
            throw new ClassificationException("Could not read image file: " + e.getMessage(), e);
        }
        if (original == null) {
            throw new ClassificationException(
                    "This file is not a readable image (unsupported or corrupt format).");
        }

        // Ensure RGB (no alpha, no grayscale/indexed color surprises).
        BufferedImage rgb = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();

        // Resize: shortest edge -> 256, preserving aspect ratio.
        int w = rgb.getWidth(), h = rgb.getHeight();
        double scale = (double) RESIZE_SHORT_EDGE / Math.min(w, h);
        int newW = (int) Math.round(w * scale);
        int newH = (int) Math.round(h * scale);

        BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(rgb, 0, 0, newW, newH, null);
        g2.dispose();

        // Center crop to 224x224.
        int cropX = (newW - TARGET_SIZE) / 2;
        int cropY = (newH - TARGET_SIZE) / 2;
        BufferedImage cropped = resized.getSubimage(
                Math.max(0, cropX), Math.max(0, cropY), TARGET_SIZE, TARGET_SIZE);

        // Build NCHW float array: rescale to [0,1], normalize with mean/std = 0.5.
        float[] data = new float[3 * TARGET_SIZE * TARGET_SIZE];
        int channelStride = TARGET_SIZE * TARGET_SIZE;

        for (int y = 0; y < TARGET_SIZE; y++) {
            for (int x = 0; x < TARGET_SIZE; x++) {
                int rgbValue = cropped.getRGB(x, y);
                float r = ((rgbValue >> 16) & 0xFF) / 255.0f;
                float gVal = ((rgbValue >> 8) & 0xFF) / 255.0f;
                float b = (rgbValue & 0xFF) / 255.0f;

                int pixelIndex = y * TARGET_SIZE + x;
                data[0 * channelStride + pixelIndex] = (r - MEAN) / STD;
                data[1 * channelStride + pixelIndex] = (gVal - MEAN) / STD;
                data[2 * channelStride + pixelIndex] = (b - MEAN) / STD;
            }
        }
        return data;
    }
}