package gui;

import service.FarmAssistService;
import service.FarmAssistService.CorpusSearchResult;
import service.TranslationService;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Agricultural Corpus Search screen.
 *
 * Purely a presentation layer, same as the other search panels: all it
 * does is call the EXISTING FarmAssistService corpus-search methods
 * (added previously, backed by io.CorpusLoader + the unmodified KMP /
 * RabinKarp / AhoCorasick classes) and display what comes back.
 *
 *   - "Search Term" button       -> FarmAssistService.searchCorpus(term),
 *                                    which runs KMP and Rabin-Karp over
 *                                    the full corpus for one term.
 *   - "Search Multiple Terms"    -> FarmAssistService.searchCorpusMultiTerm(terms),
 *     button                        which runs Aho-Corasick once to find
 *                                    several comma-separated terms in a
 *                                    single pass.
 *
 * No search algorithm, data file, or other panel is touched here.
 */
public class CorpusSearchPanel extends JPanel implements Localizable {

    private final FarmAssistService service;
    private final TranslationService translation;

    private final JLabel heading;
    private final JLabel hint;
    private final JTextField queryField;
    private final StyledButton singleSearchBtn;
    private final StyledButton multiSearchBtn;
    private final JButton backBtn;
    private final JTextArea resultsArea;

    public CorpusSearchPanel(FarmAssistService service, TranslationService translation, MainFrame frame) {
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

        hint = new JLabel();
        hint.setFont(Theme.small());
        hint.setForeground(Theme.TEXT_DARK);
        hint.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        RoundedPanel searchBar = new RoundedPanel(Theme.RADIUS_MD, true);
        searchBar.setCardBackground(Theme.SURFACE);
        searchBar.setLayout(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_SM, Theme.SPACE_SM));
        searchBar.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_SM, Theme.SPACE_MD, Theme.SPACE_SM, Theme.SPACE_MD));

        queryField = new JTextField(28);
        queryField.setFont(Theme.body());
        queryField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        singleSearchBtn = new StyledButton("Search Term");
        multiSearchBtn = new StyledButton("Search Multiple Terms", Theme.PRIMARY_LIGHT, Theme.PRIMARY_HOVER);

        searchBar.add(queryField);
        searchBar.add(singleSearchBtn);
        searchBar.add(multiSearchBtn);

        JPanel top = new JPanel(new BorderLayout(0, Theme.SPACE_SM));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(searchBar, BorderLayout.CENTER);
        top.add(hint, BorderLayout.SOUTH);

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

        singleSearchBtn.addActionListener(e -> runSingleTermSearch());
        multiSearchBtn.addActionListener(e -> runMultiTermSearch());
        queryField.addActionListener(e -> runSingleTermSearch());

        refreshLanguage();
    }

    /** "Search Term" -> FarmAssistService.searchCorpus() -> KMP + Rabin-Karp over the full corpus. */
    private void runSingleTermSearch() {
        String term = queryField.getText().trim();

        if (term.isEmpty()) {
            resultsArea.setText(translation.t("Please enter a search term."));
            return;
        }

        CorpusSearchResult result = service.searchCorpus(term);

        StringBuilder sb = new StringBuilder();
        sb.append(translation.t("KMP + Rabin-Karp Search")).append(" - \"").append(term).append("\"\n\n");
        sb.append(translation.t("KMP occurrences")).append(": ").append(result.kmpPositions.size()).append("\n");
        sb.append(translation.t("Rabin-Karp occurrences")).append(": ").append(result.rabinKarpPositions.size()).append("\n\n");

        if (result.snippets.isEmpty()) {
            sb.append(translation.t("No matches found in the corpus."));
        } else {
            sb.append(translation.t("Sample context")).append(":\n");
            for (String snippet : result.snippets) {
                sb.append("- ").append(snippet).append("\n");
            }
        }

        resultsArea.setText(sb.toString());
        resultsArea.setCaretPosition(0);
    }

    /** "Search Multiple Terms" -> FarmAssistService.searchCorpusMultiTerm() -> Aho-Corasick, one pass. */
    private void runMultiTermSearch() {
        String input = queryField.getText().trim();

        if (input.isEmpty()) {
            resultsArea.setText(translation.t("Please enter one or more search terms."));
            return;
        }

        List<String> terms = Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .toList();

        List<String> foundTogether = service.searchCorpusMultiTerm(terms);

        StringBuilder sb = new StringBuilder();
        sb.append(translation.t("Aho-Corasick Search (all terms in a single pass)")).append("\n\n");
        sb.append(translation.t("Terms searched")).append(": ").append(String.join(", ", terms)).append("\n\n");

        if (foundTogether.isEmpty()) {
            sb.append(translation.t("None of the given terms were found in the corpus."));
        } else {
            sb.append(translation.t("Found in one pass")).append(": ").append(String.join(", ", foundTogether));
        }

        resultsArea.setText(sb.toString());
        resultsArea.setCaretPosition(0);
    }

    @Override
    public void refreshLanguage() {
        heading.setText(translation.t("Search Agricultural Corpus"));
        hint.setText(translation.t("Tip: separate multiple terms with commas, e.g. nitrogen, aphids, drip irrigation"));
        singleSearchBtn.setText(translation.t("Search Term"));
        multiSearchBtn.setText(translation.t("Search Multiple Terms"));
        backBtn.setText("\u2190 " + translation.t("Back"));
        resultsArea.setText("");
    }
}