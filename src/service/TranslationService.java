package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Offline dictionary-based translation for a bounded, known vocabulary
 * of UI labels and messages (English -> Telugu / Hindi).
 *
 * This is intentionally NOT a general-purpose machine translation
 * engine. Farm Assist's translatable content (menu labels, field
 * names, result headings) is small and fixed, so a simple
 * HashMap<English, Translated> lookup loaded from a local text file
 * is the correct-complexity solution: no internet dependency, no
 * external API cost, and fully reliable during an offline demo.
 *
 * Data files: data/translations_te.txt, data/translations_hi.txt
 * Format: EnglishKey|TranslatedText
 *
 * If a key has no translation for the selected language, this falls
 * back to the original English text rather than showing a blank or
 * an error.
 */
public class TranslationService {

    public enum Language {
        ENGLISH, TELUGU, HINDI
    }

    private final Map<String, String> teMap = new HashMap<>();
    private final Map<String, String> hiMap = new HashMap<>();
    private Language currentLanguage = Language.ENGLISH;

    public TranslationService(String teFile, String hiFile) {
        loadInto(teFile, teMap);
        loadInto(hiFile, hiMap);
    }

    private void loadInto(String filePath, Map<String, String> map) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                int sep = line.indexOf('|');
                if (sep < 0) continue; // skip malformed rows
                String key = line.substring(0, sep).trim();
                String value = line.substring(sep + 1).trim();
                map.put(key, value);
            }
        } catch (IOException e) {
            System.out.println("Error loading translation file " + filePath + ": " + e.getMessage());
        }
    }

    public void setLanguage(Language language) {
        this.currentLanguage = language;
    }

    public Language getLanguage() {
        return currentLanguage;
    }

    /**
     * Translates `englishKey` into the currently selected language.
     * Falls back to the original English text if no translation exists
     * for that key (missing translation, or language is English).
     */
    public String t(String englishKey) {
        Map<String, String> activeMap = switch (currentLanguage) {
            case TELUGU -> teMap;
            case HINDI -> hiMap;
            case ENGLISH -> null;
        };
        if (activeMap == null) {
            return englishKey;
        }
        return activeMap.getOrDefault(englishKey, englishKey);
    }
}
