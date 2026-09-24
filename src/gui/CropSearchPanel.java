package gui;

import models.Crop;
import service.FarmAssistService;
import service.TranslationService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Crop Information Search screen. Internally uses
 * FarmAssistService.searchCrops(), which is fixed to KMP (plus a
 * typo-tolerant fuzzy fallback) — the user never sees or chooses
 * an algorithm. Only the visual presentation changed here.
 */
public class CropSearchPanel extends JPanel implements Localizable {

    private final FarmAssistService service;
    private final TranslationService translation;

    private final JLabel heading;
    private final JTextField queryField;
    private final StyledButton searchBtn;
    private final JButton backBtn;
    private final JTextArea resultsArea;

    public CropSearchPanel(FarmAssistService service, TranslationService translation, MainFrame frame) {
        this.service = service;
        this.translation = translation;

        setLayout(new BorderLayout(0, Theme.SPACE_MD));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG));

        heading = new JLabel();
        heading.setFont(Theme.h1());
        heading.setForeground(Theme.TEXT_DARK);
        heading.setIcon(IconPainter.of(IconPainter.Kind.LEAF, 22, Theme.PRIMARY));
        heading.setIconTextGap(10);

        RoundedPanel searchBar = new RoundedPanel(Theme.RADIUS_MD, true);
        searchBar.setCardBackground(Theme.SURFACE);
        searchBar.setLayout(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_SM, Theme.SPACE_SM));
        searchBar.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_SM, Theme.SPACE_MD, Theme.SPACE_SM, Theme.SPACE_MD));

        queryField = new JTextField(28);
        queryField.setFont(Theme.body());
        queryField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        searchBtn = new StyledButton("Search");
        searchBar.add(queryField);
        searchBar.add(searchBtn);

        JPanel top = new JPanel(new BorderLayout(0, Theme.SPACE_MD));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(searchBar, BorderLayout.SOUTH);

        RoundedPanel resultsCard = new RoundedPanel(Theme.RADIUS_MD, true);
        resultsCard.setCardBackground(Theme.SURFACE);
        resultsCard.setLayout(new BorderLayout());
        resultsCard.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD));

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        resultsArea.setFont(FontProvider.monospaced(Font.PLAIN, 14));
        resultsArea.setForeground(Theme.TEXT_DARK);
        resultsArea.setBackground(Theme.SURFACE);
        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.setBorder(null);
        resultsCard.add(scroll, BorderLayout.CENTER);

        backBtn = new JButton();
        backBtn.setFont(Theme.body());
        backBtn.setForeground(Theme.PRIMARY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> frame.showCard(MainFrame.DASHBOARD));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_SM, 0, 0, 0));
        bottom.add(backBtn);

        add(top, BorderLayout.NORTH);
        add(resultsCard, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> runSearch());
        queryField.addActionListener(e -> runSearch());

        refreshLanguage();
    }

    private void runSearch() {
        String query = queryField.getText().trim();
        List<Crop> results = service.searchCrops(query);

        if (results.isEmpty()) {
            resultsArea.setText(translation.t("No matching crops found"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Crop c : results) {
            sb.append(c.getName()).append("\n");
            sb.append("  ").append(translation.t("Soil")).append(": ").append(c.getSoilType()).append("\n");
            sb.append("  ").append(translation.t("Water")).append(": ").append(c.getWaterRequirement()).append("\n");
            sb.append("  ").append(translation.t("Season")).append(": ").append(c.getSeason()).append("\n");
            sb.append("  ").append(translation.t("Expected Profit")).append(": Rs.").append(c.getExpectedProfit()).append("\n\n");
        }
        resultsArea.setText(sb.toString());
    }

    @Override
    public void refreshLanguage() {
        heading.setText(translation.t("Crop Information"));
        searchBtn.setText(translation.t("Search"));
        backBtn.setText("\u2190 " + translation.t("Back"));
        resultsArea.setText("");
    }
}