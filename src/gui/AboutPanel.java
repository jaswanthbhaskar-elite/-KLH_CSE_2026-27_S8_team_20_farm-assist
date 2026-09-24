package gui;

import service.TranslationService;

import javax.swing.*;
import java.awt.*;

/**
 * About / Algorithms screen — the one place algorithm names are shown,
 * for academic/viva demonstration purposes. Not part of the normal
 * farmer-facing flow. Only the visual presentation changed here; the
 * algorithm-mapping content itself is unchanged (aside from correcting
 * the photo-identification line to reflect that ONNX inference is now
 * actually wired in, which is a documentation-text fix, not a logic
 * change).
 */
public class AboutPanel extends JPanel implements Localizable {

    private final TranslationService translation;
    private final JLabel heading;
    private final JTextArea infoArea;
    private final JButton backBtn;

    private static final String ALGORITHM_MAPPING =
            "Farm Assist - Algorithm to Feature Mapping\n\n" +
            "Crop Information Search       -> KMP (Knuth-Morris-Pratt)\n" +
            "Disease Information Search    -> Rabin-Karp (rolling hash)\n" +
            "Fertilizer Information Search -> Simple linear lookup\n" +
            "Multi-Symptom Disease Search  -> Aho-Corasick (trie + failure links)\n" +
            "Crop Recommendation           -> 0/1 Knapsack Dynamic Programming\n" +
            "Fertilizer Allocation         -> Edmonds-Karp Maximum Flow\n" +
            "Typo tolerance (all searches) -> Edit Distance (Levenshtein) DP\n" +
            "Translation                   -> Offline HashMap lookup\n" +
            "Photo Disease Identification  -> Pretrained MobileNetV2 image classifier\n" +
            "                                  via ONNX Runtime for Java\n\n" +
            "Each algorithm has exactly one fixed, meaningful application in this project.\n" +
            "The farmer never chooses an algorithm; the application selects the appropriate\n" +
            "one automatically based on the feature being used.";

    public AboutPanel(TranslationService translation, MainFrame frame) {
        this.translation = translation;

        setLayout(new BorderLayout(0, Theme.SPACE_MD));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG));

        heading = new JLabel();
        heading.setFont(Theme.h1());
        heading.setForeground(Theme.TEXT_DARK);
        heading.setIcon(IconPainter.of(IconPainter.Kind.INFO, 22, Theme.PRIMARY));
        heading.setIconTextGap(10);

        RoundedPanel card = new RoundedPanel(Theme.RADIUS_MD, true);
        card.setCardBackground(Theme.SURFACE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD));

        infoArea = new JTextArea(ALGORITHM_MAPPING);
        infoArea.setEditable(false);
        infoArea.setFont(FontProvider.monospaced(Font.PLAIN, 13));
        infoArea.setForeground(Theme.TEXT_DARK);
        infoArea.setBackground(Theme.SURFACE);
        JScrollPane scroll = new JScrollPane(infoArea);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

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

        add(heading, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refreshLanguage();
    }

    @Override
    public void refreshLanguage() {
        heading.setText(translation.t("About"));
        backBtn.setText("\u2190 " + translation.t("Back"));
    }
}