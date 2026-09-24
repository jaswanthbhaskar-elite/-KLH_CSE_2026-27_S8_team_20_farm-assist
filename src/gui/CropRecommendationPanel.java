package gui;

import algorithms.recommend.CropKnapsackDP;
import models.Crop;
import service.FarmAssistService;
import service.TranslationService;

import javax.swing.*;
import java.awt.*;

/**
 * Crop Recommendation screen. Asks for land/budget/water, then calls
 * FarmAssistService.recommendCrops(), which is backed by the
 * unchanged multi-constraint 0/1 Knapsack DP. Only the visual
 * presentation changed here.
 */
public class CropRecommendationPanel extends JPanel implements Localizable {

    private final FarmAssistService service;
    private final TranslationService translation;

    private final JLabel heading, landLabel, budgetLabel, waterLabel;
    private final JTextField landField, budgetField, waterField;
    private final StyledButton submitBtn;
    private final JButton backBtn;
    private final JTextArea resultsArea;

    public CropRecommendationPanel(FarmAssistService service, TranslationService translation, MainFrame frame) {
        this.service = service;
        this.translation = translation;

        setLayout(new BorderLayout(0, Theme.SPACE_MD));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG));

        heading = new JLabel();
        heading.setFont(Theme.h1());
        heading.setForeground(Theme.TEXT_DARK);
        heading.setIcon(IconPainter.of(IconPainter.Kind.CHART, 22, Theme.PRIMARY));
        heading.setIconTextGap(10);

        RoundedPanel formCard = new RoundedPanel(Theme.RADIUS_MD, true);
        formCard.setCardBackground(Theme.SURFACE);
        formCard.setLayout(new BorderLayout(0, Theme.SPACE_MD));
        formCard.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD));

        JPanel form = new JPanel(new GridLayout(3, 2, Theme.SPACE_MD, Theme.SPACE_SM));
        form.setOpaque(false);
        landLabel = styledLabel();
        budgetLabel = styledLabel();
        waterLabel = styledLabel();
        landField = styledField();
        budgetField = styledField();
        waterField = styledField();
        form.add(landLabel); form.add(landField);
        form.add(budgetLabel); form.add(budgetField);
        form.add(waterLabel); form.add(waterField);

        submitBtn = new StyledButton("Submit");
        backBtn = new JButton();
        backBtn.setFont(Theme.body());
        backBtn.setForeground(Theme.PRIMARY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_SM, 0));
        buttons.setOpaque(false);
        buttons.add(submitBtn);
        buttons.add(backBtn);

        formCard.add(form, BorderLayout.CENTER);
        formCard.add(buttons, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout(0, Theme.SPACE_MD));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(formCard, BorderLayout.SOUTH);

        RoundedPanel resultsCard = new RoundedPanel(Theme.RADIUS_MD, true);
        resultsCard.setCardBackground(Theme.SURFACE);
        resultsCard.setLayout(new BorderLayout());
        resultsCard.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD));

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(FontProvider.monospaced(Font.PLAIN, 14));
        resultsArea.setForeground(Theme.TEXT_DARK);
        resultsArea.setBackground(Theme.SURFACE);
        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.setBorder(null);
        resultsCard.add(scroll, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(resultsCard, BorderLayout.CENTER);

        backBtn.addActionListener(e -> frame.showCard(MainFrame.DASHBOARD));
        submitBtn.addActionListener(e -> runRecommendation());

        refreshLanguage();
    }

    private JLabel styledLabel() {
        JLabel l = new JLabel();
        l.setFont(Theme.bodyBold());
        l.setForeground(Theme.TEXT_DARK);
        return l;
    }

    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(Theme.body());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private void runRecommendation() {
        int land, budget, water;
        try {
            land = Integer.parseInt(landField.getText().trim());
            budget = Integer.parseInt(budgetField.getText().trim());
            water = Integer.parseInt(waterField.getText().trim());
        } catch (NumberFormatException ex) {
            resultsArea.setText("Please enter valid whole numbers for land, budget, and water.");
            return;
        }

        CropKnapsackDP.Result result = service.recommendCrops(land, budget, water);

        if (result.selectedCrops.isEmpty()) {
            resultsArea.setText("No crop combination fits within the given constraints.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(translation.t("Selected Crops")).append(":\n");
        for (Crop c : result.selectedCrops) {
            sb.append("- ").append(c.getName()).append("\n");
        }
        sb.append("\n").append(translation.t("Total Land")).append(": ").append(result.totalLand).append("\n");
        sb.append(translation.t("Total Budget")).append(": Rs.").append(result.totalBudget).append("\n");
        sb.append(translation.t("Total Water")).append(": ").append(result.totalWater).append("\n");
        sb.append(translation.t("Expected Profit")).append(": Rs.").append(result.totalProfit).append("\n");

        resultsArea.setText(sb.toString());
    }

    @Override
    public void refreshLanguage() {
        heading.setText(translation.t("Crop Recommendation"));
        landLabel.setText(translation.t("Available Land"));
        budgetLabel.setText(translation.t("Budget"));
        waterLabel.setText(translation.t("Water"));
        submitBtn.setText(translation.t("Submit"));
        backBtn.setText("\u2190 " + translation.t("Back"));
        resultsArea.setText("");
    }
}