package gui;

import models.Disease;
import service.FarmAssistService;
import service.TranslationService;
import vision.ClassificationException;
import vision.ClassifierUnavailableException;
import vision.DiseaseImageClassifier;
import vision.PredictionResult;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Identify Disease from Photo screen.
 *
 * This panel depends ONLY on the DiseaseImageClassifier interface,
 * never on a specific model implementation, so it works unmodified
 * whether the real ONNX-backed classifier is available or not.
 *
 * IMPORTANT: this redesign only changes layout/presentation. The
 * classify(...) call, the SwingWorker background-thread execution,
 * and the exception handling are exactly the same control flow as
 * before — only how the result is DISPLAYED changed (structured
 * labels instead of one plain text block), per the UI/UX redesign
 * requirements. No preprocessing, model, or prediction logic touched.
 */
public class PhotoDiseasePanel extends JPanel implements Localizable {

    private final FarmAssistService service;
    private final TranslationService translation;
    private final DiseaseImageClassifier classifier;

    private final JLabel heading;
    private final StyledButton chooseBtn, identifyBtn;
    private final JButton backBtn;
    private final JLabel imagePreview;

    // Structured result display (right column) — presentation only.
    private final JLabel statusLabel;
    private final JLabel diseaseNameLabel;
    private final JLabel confidenceLabel;
    private final JProgressBar confidenceBar;
    private final JLabel symptomsHeading, treatmentHeading;
    private final JTextArea symptomsArea, treatmentArea;
    private final JPanel resultDetailPanel;

    private File selectedFile;

    public PhotoDiseasePanel(FarmAssistService service, TranslationService translation,
                              DiseaseImageClassifier classifier, MainFrame frame) {
        this.service = service;
        this.translation = translation;
        this.classifier = classifier;

        setLayout(new BorderLayout(0, Theme.SPACE_MD));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG));

        heading = new JLabel();
        heading.setFont(Theme.h1());
        heading.setForeground(Theme.TEXT_DARK);
        heading.setIcon(IconPainter.of(IconPainter.Kind.CAMERA, 22, Theme.PRIMARY));
        heading.setIconTextGap(10);

        // ---- LEFT: upload + preview ----
        RoundedPanel leftCard = new RoundedPanel(Theme.RADIUS_MD, true);
        leftCard.setCardBackground(Theme.SURFACE);
        leftCard.setLayout(new BorderLayout(0, Theme.SPACE_MD));
        leftCard.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD));

        imagePreview = new JLabel("", SwingConstants.CENTER);
        imagePreview.setOpaque(true);
        imagePreview.setBackground(Theme.SURFACE_ALT);
        imagePreview.setForeground(Theme.TEXT_MUTED);
        imagePreview.setFont(Theme.body());
        imagePreview.setPreferredSize(new Dimension(320, 320));
        imagePreview.setBorder(BorderFactory.createDashedBorder(Theme.BORDER, 2, 4));

        chooseBtn = new StyledButton("Choose Image", Theme.PRIMARY, Theme.PRIMARY_HOVER);
        identifyBtn = new StyledButton("Identify Disease", Theme.PRIMARY_LIGHT, Theme.PRIMARY_HOVER);

        JPanel leftButtons = new JPanel(new GridLayout(1, 2, Theme.SPACE_SM, 0));
        leftButtons.setOpaque(false);
        leftButtons.add(chooseBtn);
        leftButtons.add(identifyBtn);

        leftCard.add(imagePreview, BorderLayout.CENTER);
        leftCard.add(leftButtons, BorderLayout.SOUTH);

        // ---- RIGHT: structured result ----
        RoundedPanel rightCard = new RoundedPanel(Theme.RADIUS_MD, true);
        rightCard.setCardBackground(Theme.SURFACE);
        rightCard.setLayout(new BorderLayout(0, Theme.SPACE_SM));
        rightCard.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD));

        statusLabel = new JLabel("", SwingConstants.LEFT);
        statusLabel.setFont(Theme.body());
        statusLabel.setForeground(Theme.TEXT_MUTED);

        diseaseNameLabel = new JLabel(" ");
        diseaseNameLabel.setFont(Theme.display());
        diseaseNameLabel.setForeground(Theme.PRIMARY);

        confidenceLabel = new JLabel(" ");
        confidenceLabel.setFont(Theme.bodyBold());
        confidenceLabel.setForeground(Theme.TEXT_DARK);

        confidenceBar = new JProgressBar(0, 100);
        confidenceBar.setForeground(Theme.PRIMARY_LIGHT);
        confidenceBar.setPreferredSize(new Dimension(10, 10));
        confidenceBar.setVisible(false);

        symptomsHeading = sectionHeading();
        treatmentHeading = sectionHeading();
        symptomsArea = readOnlyArea();
        treatmentArea = readOnlyArea();

        resultDetailPanel = new JPanel();
        resultDetailPanel.setOpaque(false);
        resultDetailPanel.setLayout(new BoxLayout(resultDetailPanel, BoxLayout.Y_AXIS));
        addLeftAligned(resultDetailPanel, diseaseNameLabel);
        resultDetailPanel.add(Box.createVerticalStrut(6));
        addLeftAligned(resultDetailPanel, confidenceLabel);
        addLeftAligned(resultDetailPanel, confidenceBar);
        resultDetailPanel.add(Box.createVerticalStrut(16));
        addLeftAligned(resultDetailPanel, symptomsHeading);
        addLeftAligned(resultDetailPanel, symptomsArea);
        resultDetailPanel.add(Box.createVerticalStrut(12));
        addLeftAligned(resultDetailPanel, treatmentHeading);
        addLeftAligned(resultDetailPanel, treatmentArea);
        resultDetailPanel.setVisible(false);

        JPanel rightContent = new JPanel(new BorderLayout(0, Theme.SPACE_SM));
        rightContent.setOpaque(false);
        rightContent.add(statusLabel, BorderLayout.NORTH);
        rightContent.add(resultDetailPanel, BorderLayout.CENTER);

        rightCard.add(rightContent, BorderLayout.CENTER);

        JPanel columns = new JPanel(new GridLayout(1, 2, Theme.SPACE_MD, 0));
        columns.setOpaque(false);
        columns.add(leftCard);
        columns.add(rightCard);

        backBtn = new JButton();
        backBtn.setFont(Theme.body());
        backBtn.setForeground(Theme.PRIMARY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.CENTER);
        top.add(backBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(columns, BorderLayout.CENTER);

        chooseBtn.addActionListener(e -> chooseImage());
        identifyBtn.addActionListener(e -> identify());
        backBtn.addActionListener(e -> frame.showCard(MainFrame.DASHBOARD));

        refreshLanguage();
    }

    private JLabel sectionHeading() {
        JLabel l = new JLabel();
        l.setFont(Theme.h2());
        l.setForeground(Theme.TEXT_DARK);
        return l;
    }

    private JTextArea readOnlyArea() {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(Theme.body());
        a.setForeground(Theme.TEXT_MUTED);
        a.setOpaque(false);
        a.setRows(2);
        return a;
    }

    private void addLeftAligned(JPanel container, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(c);
    }

    // ---- Everything below is the exact same control flow as before; ----
    // ---- only the final display step (showPrediction) changed.       ----

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "bmp"));
        int outcome = chooser.showOpenDialog(this);
        if (outcome == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(scaled));
            imagePreview.setText(null);
            clearResult();
        }
    }

    private void identify() {
        if (selectedFile == null) {
            showStatus(translation.t("No image selected"), Theme.TEXT_MUTED);
            return;
        }

        if (!classifier.isAvailable()) {
            showStatus(translation.t("Feature unavailable") + ": " + classifier.getUnavailableReason(), Theme.TEXT_MUTED);
            return;
        }

        setBusy(true);
        showStatus(translation.t("Detecting..."), Theme.PRIMARY);
        resultDetailPanel.setVisible(false);

        // Run inference off the Event Dispatch Thread so the UI never freezes.
        SwingWorker<PredictionResult, Void> worker = new SwingWorker<>() {
            private Exception failure;

            @Override
            protected PredictionResult doInBackground() {
                try {
                    return classifier.classify(selectedFile);
                } catch (ClassifierUnavailableException | ClassificationException ex) {
                    failure = ex;
                    return null;
                }
            }

            @Override
            protected void done() {
                setBusy(false);
                if (failure != null) {
                    showStatus(translation.t("Feature unavailable") + ": " + failure.getMessage(), Theme.TEXT_MUTED);
                    return;
                }
                try {
                    PredictionResult result = get();
                    showPrediction(result);
                } catch (Exception ex) {
                    // Defensive: should not happen since exceptions are caught above,
                    // but never show a raw stack trace to the user either way.
                    showStatus(translation.t("Feature unavailable") + ": Unexpected error during identification.",
                            Theme.TEXT_MUTED);
                }
            }
        };
        worker.execute();
    }

    private void setBusy(boolean busy) {
        chooseBtn.setEnabled(!busy);
        identifyBtn.setEnabled(!busy);
    }

    private void showStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    private void clearResult() {
        statusLabel.setText("");
        resultDetailPanel.setVisible(false);
        confidenceBar.setVisible(false);
    }

    private void showPrediction(PredictionResult result) {
        statusLabel.setText("");

        if (result.diseaseName != null) {
            diseaseNameLabel.setText(result.diseaseName);
        } else {
            diseaseNameLabel.setText(result.rawModelLabel + " (unmapped)");
        }

        int confidencePercent = (int) Math.round(result.confidence * 100);
        confidenceLabel.setText(translation.t("Confidence") + ": " + confidencePercent + "%");
        confidenceBar.setVisible(true);
        confidenceBar.setValue(confidencePercent);

        symptomsHeading.setText(translation.t("Symptoms"));
        treatmentHeading.setText(translation.t("Treatment"));

        if (result.diseaseName != null) {
            List<Disease> matches = service.searchDiseases(result.diseaseName);
            if (!matches.isEmpty()) {
                Disease d = matches.get(0);
                symptomsArea.setText(d.getSymptoms());
                treatmentArea.setText(d.getTreatment());
            } else {
                symptomsArea.setText("—");
                treatmentArea.setText("No record found in disease database for this name.");
            }
        } else {
            symptomsArea.setText("—");
            treatmentArea.setText("No matching entry in disease database for this raw model label.");
        }

        resultDetailPanel.setVisible(true);
        revalidate();
        repaint();
    }

    @Override
    public void refreshLanguage() {
        heading.setText(translation.t("Identify Disease from Photo"));
        chooseBtn.setText(translation.t("Choose Image"));
        identifyBtn.setText(translation.t("Identify Disease"));
        backBtn.setText("\u2190 " + translation.t("Back"));
        if (selectedFile == null) {
            imagePreview.setText(translation.t("No image selected"));
        }
    }
}