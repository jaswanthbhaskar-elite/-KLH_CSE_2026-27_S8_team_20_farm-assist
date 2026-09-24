package gui;

import service.FarmAssistService;
import service.FarmAssistService.DiseaseMatch;
import service.FarmAssistService.SymptomSearchResult;
import service.TranslationService;

import javax.swing.*;
import java.awt.*;

/**
 * Multi-Symptom Disease Search screen. Internally uses
 * FarmAssistService.searchBySymptoms(), which is powered by
 * Aho-Corasick (multi-pattern matching in a single pass). Only the
 * visual presentation changed here.
 */
public class SymptomSearchPanel extends JPanel implements Localizable {

    private final FarmAssistService service;
    private final TranslationService translation;

    private final JLabel heading;
    private final JTextField queryField;
    private final StyledButton searchBtn;
    private final JButton backBtn;
    private final JTextArea resultsArea;

    public SymptomSearchPanel(FarmAssistService service, TranslationService translation, MainFrame frame) {
        this.service = service;
        this.translation = translation;

        setLayout(new BorderLayout(0, Theme.SPACE_MD));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG));

        heading = new JLabel();
        heading.setFont(Theme.h1());
        heading.setForeground(Theme.TEXT_DARK);
        heading.setIcon(IconPainter.of(IconPainter.Kind.SEARCH, 22, Theme.PRIMARY));
        heading.setIconTextGap(10);

        RoundedPanel searchBar = new RoundedPanel(Theme.RADIUS_MD, true);
        searchBar.setCardBackground(Theme.SURFACE);
        searchBar.setLayout(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_SM, Theme.SPACE_SM));
        searchBar.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_SM, Theme.SPACE_MD, Theme.SPACE_SM, Theme.SPACE_MD));

        queryField = new JTextField(34);
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
        SymptomSearchResult result = service.searchBySymptoms(query);

        StringBuilder sb = new StringBuilder();

        if (result.matchedSymptoms.isEmpty()) {
            resultsArea.setText(translation.t("No matching diseases found"));
            return;
        }

        sb.append(translation.t("Matched Symptoms")).append(":\n");
        for (String s : result.matchedSymptoms) {
            sb.append("- ").append(s).append("\n");
        }
        sb.append("\n").append(translation.t("Possible Diseases")).append(":\n");

        int rank = 1;
        for (DiseaseMatch dm : result.rankedDiseases) {
            sb.append(rank).append(". ").append(dm.diseaseName)
              .append(" - ").append(dm.matchCount).append("\n");
            rank++;
        }
        resultsArea.setText(sb.toString());
    }

    @Override
    public void refreshLanguage() {
        heading.setText(translation.t("Search by Multiple Symptoms"));
        searchBtn.setText(translation.t("Search"));
        backBtn.setText("\u2190 " + translation.t("Back"));
        resultsArea.setText("");
    }
}