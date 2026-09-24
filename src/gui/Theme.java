package gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Central design-system constants: colors, spacing, and font scale.
 * Every panel pulls from here instead of hardcoding values, so the
 * whole application shares one consistent visual language.
 *
 * Fonts always go through FontProvider (never raw "SansSerif"/"Arial")
 * so Telugu/Devanagari rendering keeps working everywhere.
 */
public final class Theme {

    private Theme() {}

    // ---- Palette (deep agricultural green / fresh leaf green / warm cream) ----
    public static final Color PRIMARY_DARK   = new Color(0x1B3A2B); // deep green, sidebar background
    public static final Color PRIMARY        = new Color(0x2E7D32); // core brand green
    public static final Color PRIMARY_LIGHT  = new Color(0x4CAF50); // fresh leaf green, accents/CTAs
    public static final Color PRIMARY_HOVER  = new Color(0x66BB6A); // hover state for green buttons

    public static final Color BACKGROUND     = new Color(0xF7F6F1); // warm off-white / cream
    public static final Color SURFACE        = Color.WHITE;         // card backgrounds
    public static final Color SURFACE_ALT    = new Color(0xEFF3EC); // subtle tinted panels

    public static final Color TEXT_DARK      = new Color(0x1F2A22); // near-black charcoal green
    public static final Color TEXT_MUTED     = new Color(0x5B6B60); // secondary text
    public static final Color TEXT_ON_DARK   = Color.WHITE;
    public static final Color TEXT_ON_DARK_MUTED = new Color(0xC8D8CC);

    public static final Color BORDER         = new Color(0xE0E4DE);
    public static final Color SHADOW         = new Color(0, 0, 0, 28);

    public static final Color SIDEBAR_ACTIVE = new Color(0x2E7D32);
    public static final Color SIDEBAR_HOVER  = new Color(0x27492F);

    // ---- Spacing scale ----
    public static final int SPACE_XS = 4;
    public static final int SPACE_SM = 8;
    public static final int SPACE_MD = 16;
    public static final int SPACE_LG = 24;
    public static final int SPACE_XL = 32;

    // ---- Corner radius ----
    public static final int RADIUS_SM = 10;
    public static final int RADIUS_MD = 16;
    public static final int RADIUS_LG = 22;

    // ---- Typography (family always resolved via FontProvider) ----
    public static Font display()   { return FontProvider.sans(Font.BOLD, 30); }
    public static Font h1()        { return FontProvider.sans(Font.BOLD, 22); }
    public static Font h2()        { return FontProvider.sans(Font.BOLD, 17); }
    public static Font body()      { return FontProvider.sans(Font.PLAIN, 14); }
    public static Font bodyBold()  { return FontProvider.sans(Font.BOLD, 14); }
    public static Font small()     { return FontProvider.sans(Font.PLAIN, 12); }
    public static Font button()    { return FontProvider.sans(Font.BOLD, 14); }
    public static Font monospacedBody() { return FontProvider.monospaced(Font.PLAIN, 14); }
}