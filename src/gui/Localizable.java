package gui;

/**
 * Implemented by any GUI panel whose visible text should update when
 * the user changes the selected language. MainFrame calls
 * refreshLanguage() on every registered panel after a language change.
 */
public interface Localizable {
    void refreshLanguage();
}
