package gui;

import models.Disease;
import service.FarmAssistService;
import service.TranslationService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Disease Information Search screen. Internally uses
 * FarmAssistService.searchDiseases(), which is fixed to Rabin-Karp
 * (plus fuzzy fallback) — no algorithm choice is shown to the user.
 * Only the visual presentation changed here.
 */
public class DiseaseSearchPanel extends JPanel implements Localizable {

    private final FarmAssistService service;
    private final TranslationService translation;

    private final JLabel heading;
    private final JTextField queryField;
    private final StyledButton searchBtn;
    private final JButton backBtn;
    private final JTextArea resultsArea;

    public DiseaseSearchPanel(FarmAssistService service, TranslationService translation, MainFrame frame) {
        this.service = service;
        this.translation = translation;

        setLayout(new BorderLayout(0, Theme.SPACE_MD));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG));

        heading = new JLabel();
        heading.setFont(Theme.h1());
        heading.setForeground(Theme.TEXT_DARK);
        heading.setIcon(IconPainter.of(IconPainter.Kind.BUG, 22, Theme.PRIMARY));
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
        List<Disease> results = service.searchDiseases(query);

        if (results.isEmpty()) {
            resultsArea.setText(translation.t("No matching diseases found"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Disease d : results) {
            sb.append(d.getName()).append("\n");
            sb.append("  ").append(translation.t("Symptoms")).append(": ").append(d.getSymptoms()).append("\n");
            sb.append("  ").append(translation.t("Treatment")).append(": ").append(d.getTreatment()).append("\n\n");
        }
        resultsArea.setText(sb.toString());
    }

    @Override
    public void refreshLanguage() {
        heading.setText(translation.t("Disease Information"));
        searchBtn.setText(translation.t("Search"));
        backBtn.setText("\u2190 " + translation.t("Back"));
        resultsArea.setText("");
    }
}