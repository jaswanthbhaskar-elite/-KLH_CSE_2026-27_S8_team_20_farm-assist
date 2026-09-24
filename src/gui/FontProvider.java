package gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Central font-selection utility for Unicode rendering.
 *
 * WHY THIS EXISTS:
 * Every GUI panel previously built fonts using Java's logical family
 * names ("SansSerif", "Monospaced"). On Windows those resolve to fonts
 * like Arial/Tahoma, which have no Telugu or Devanagari glyphs — so
 * translated text rendered as tofu boxes (\u25A1) even though the
 * underlying UTF-8 string data was always correct. This class picks
 * an actually-available font that can display Telugu + Devanagari +
 * Latin, preferring Windows' built-in "Nirmala UI" (ships since
 * Windows 8, covers all three), with sensible fallbacks for other
 * platforms.
 *
 * This does NOT change any layout, color, size, or component
 * structure — it only changes which font FAMILY backs the same
 * style/size that was already being used everywhere.
 */
public final class FontProvider {

    // Representative characters used to verify a candidate font can
    // ACTUALLY render these scripts, not just that a family with a
    // plausible name exists.
    private static final String TELUGU_SAMPLE = "\u0c2a\u0c02\u0c1f";   // "పంట" (crop)
    private static final String DEVANAGARI_SAMPLE = "\u092b\u0938\u0932"; // "फसल" (crop)

    // Preference order: best real-world coverage first.
    private static final String[] CANDIDATE_FAMILIES = {
            "Nirmala UI",           // Windows 8+: Latin + Telugu + Devanagari, proper shaping
            "Noto Sans",            // Some distros bundle a multi-script build
            "Noto Sans Telugu",
            "Noto Sans Devanagari",
            "FreeSans",             // Linux fallback: basic coverage, no complex shaping
            "Unifont",              // Universal last resort: crude glyphs, but not tofu
    };

    private static final String RESOLVED_SANS_FAMILY = resolveUnicodeFamily();

    private FontProvider() {}

    private static String resolveUnicodeFamily() {
        Set<String> available = new LinkedHashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));

        for (String candidate : CANDIDATE_FAMILIES) {
            if (!available.contains(candidate)) continue;
            Font f = new Font(candidate, Font.PLAIN, 12);
            // Verify actual glyph coverage, not just that the name exists.
            if (canDisplay(f, TELUGU_SAMPLE) && canDisplay(f, DEVANAGARI_SAMPLE)) {
                return candidate;
            }
        }
        // Nothing with full coverage found — fall back to logical SansSerif.
        // English/Latin text is unaffected either way; Telugu/Hindi may
        // still show tofu on a system with no suitable font installed
        // at all (this is a genuine environment limitation, not a bug).
        return Font.SANS_SERIF;
    }

    private static boolean canDisplay(Font f, String sample) {
        return f.canDisplayUpTo(sample) == -1;
    }

    /** The Unicode-capable family name actually selected, for diagnostics/logging. */
    public static String getResolvedFamilyName() {
        return RESOLVED_SANS_FAMILY;
    }

    /** Replacement for new Font("SansSerif", style, size). */
    public static Font sans(int style, int size) {
        return new Font(RESOLVED_SANS_FAMILY, style, size);
    }

    /**
     * Replacement for new Font("Monospaced", style, size).
     * No widely-available font provides both fixed-width Latin AND
     * Telugu/Devanagari glyphs, so panels displaying translated content
     * intentionally use the same Unicode-capable sans font here too —
     * correct rendering takes priority over fixed-width alignment for
     * this project's translated text areas.
     */
    public static Font monospaced(int style, int size) {
        return new Font(RESOLVED_SANS_FAMILY, style, size);
    }

    /**
     * Applies the resolved font to the Swing UIManager defaults, so
     * components that never receive an explicit setFont(...) call
     * (plain JButton/JLabel/JComboBox/JTable instances scattered across
     * the panels) also render Telugu/Hindi correctly. Must be called
     * once, before any GUI components are constructed.
     */
    public static void applyGlobalDefaults() {
        Font uiFont = sans(Font.PLAIN, 13);
        String[] keys = {
                "Label.font", "Button.font", "ToggleButton.font", "TextField.font",
                "TextArea.font", "ComboBox.font", "List.font", "Table.font",
                "TableHeader.font", "TitledBorder.font", "CheckBox.font",
                "RadioButton.font", "ToolTip.font", "MenuItem.font", "Menu.font",
                "OptionPane.messageFont", "OptionPane.buttonFont"
        };
        for (String key : keys) {
            javax.swing.UIManager.put(key, uiFont);
        }
    }
}