import gui.FontProvider;
import gui.MainFrame;
import gui.SplashScreen;
import service.FarmAssistService;
import service.TranslationService;
import vision.DiseaseImageClassifier;
import vision.UnavailableDiseaseClassifier;

import javax.swing.*;
import java.lang.reflect.Constructor;

/**
 * Entry point for the Java Swing desktop application.
 * The original console application (Main.java) is untouched and
 * still runs independently as a fallback / regression baseline.
 */
public class GuiMain {

    private static final String MODEL_PATH = "model/plant_disease_mobilenetv2.onnx";
    private static final String LABELS_PATH = "model/labels.txt";
    private static final String MAPPING_PATH = "data/model_class_to_disease.txt";

    public static void main(String[] args) {
        // Must run before any Swing component is constructed, so
        // components that rely on UIManager defaults (rather than an
        // explicit setFont call) also get a Telugu/Devanagari-capable font.
        FontProvider.applyGlobalDefaults();

        FarmAssistService service = new FarmAssistService(
                "data/crops.txt",
                "data/diseases.txt",
                "data/fertilizers.txt",
                "data/requirements.txt",
                "data/symptoms.txt"
        );

        TranslationService translation = new TranslationService(
                "data/translations_te.txt",
                "data/translations_hi.txt"
        );

        DiseaseImageClassifier classifier = loadClassifier();

        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen(() -> {
                MainFrame frame = new MainFrame(service, translation, classifier);
                frame.setVisible(true);
            });
            splash.setVisible(true);
        });
    }

    /**
     * Tries to load the real ONNX-backed classifier via reflection, so
     * this file (which IS part of the default src/ build) never has a
     * hard compile-time dependency on vision.OnnxDiseaseClassifier
     * (which lives in src-optional/ and requires the ONNX Runtime jar
     * to compile). If the class isn't found, or fails to load for any
     * reason, this falls back to the honest UnavailableDiseaseClassifier
     * with a specific, accurate reason for THIS environment.
     *
     * See README_PHOTO_ID_SETUP.md for how to compile and enable the
     * real classifier.
     */
    private static DiseaseImageClassifier loadClassifier() {
        try {
            Class<?> clazz = Class.forName("vision.OnnxDiseaseClassifier");
            Constructor<?> ctor = clazz.getConstructor(String.class, String.class, String.class);
            DiseaseImageClassifier instance = (DiseaseImageClassifier)
                    ctor.newInstance(MODEL_PATH, LABELS_PATH, MAPPING_PATH);
            return instance;
        } catch (ClassNotFoundException e) {
            return new UnavailableDiseaseClassifier(
                    "Photo-based disease identification is not compiled into this build. " +
                    "The OnnxDiseaseClassifier class (in src-optional/vision/) was not found " +
                    "on the classpath — it must be compiled separately with the ONNX Runtime " +
                    "jar available. See README_PHOTO_ID_SETUP.md at the project root for the " +
                    "exact two-step build process."
            );
        } catch (Exception e) {
            return new UnavailableDiseaseClassifier(
                    "Photo-based disease identification failed to initialize: " +
                    e.getClass().getSimpleName() + ": " + e.getMessage() +
                    ". See README_PHOTO_ID_SETUP.md for setup steps."
            );
        }
    }
}