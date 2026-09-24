package gui;

import service.TranslationService;
import service.TranslationService.Language;

import javax.swing.*;
import java.awt.*;

/**
 * Top header bar: a "Welcome, Farmer!" greeting on the left and the
 * language selector on the right. Kept intentionally simple since
 * branding now lives in the sidebar. The language selector continues
 * to drive the existing TranslationService/Localizable refresh flow —
 * no localization logic changed, only its visual container.
 */
public class HeaderBar extends JPanel implements Localizable {

    private final TranslationService translation;
    private final JLabel greeting;
    private final JComboBox<String> languageSelector;

    public HeaderBar(TranslationService translation, Runnable onLanguageChanged) {
        this.translation = translation;

        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel(IconPainter.of(IconPainter.Kind.HOME, 18, Theme.TEXT_MUTED));
        greeting = new JLabel("Welcome, Farmer!");
        greeting.setFont(Theme.bodyBold());
        greeting.setForeground(Theme.TEXT_DARK);
        left.add(icon);
        left.add(greeting);
        add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JLabel langIcon = new JLabel(IconPainter.of(IconPainter.Kind.LANGUAGE, 16, Theme.TEXT_MUTED));
        languageSelector = new JComboBox<>(new String[]{
                "English", "\u0c24\u0c46\u0c32\u0c41\u0c17\u0c41", "\u0939\u093f\u0928\u094d\u0926\u0940"});
        languageSelector.setFont(Theme.body());
        languageSelector.addActionListener(e -> {
            int index = languageSelector.getSelectedIndex();
            Language lang = switch (index) {
                case 1 -> Language.TELUGU;
                case 2 -> Language.HINDI;
                default -> Language.ENGLISH;
            };
            translation.setLanguage(lang);
            onLanguageChanged.run();
        });
        right.add(langIcon);
        right.add(languageSelector);
        add(right, BorderLayout.EAST);

        refreshLanguage();
    }

    @Override
    public void refreshLanguage() {
        greeting.setText(translation.t("Welcome, Farmer!"));
    }
}