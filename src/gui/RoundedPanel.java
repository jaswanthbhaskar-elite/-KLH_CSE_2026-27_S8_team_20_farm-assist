package gui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A JPanel with rounded corners, an optional subtle drop shadow, and a
 * configurable background color. Used as the base "card" container
 * throughout the redesigned UI (feature cards, result panels, form
 * panels) instead of plain rectangular JPanels.
 */
public class RoundedPanel extends JPanel {

    private final int radius;
    private final boolean shadow;
    private Color background;

    public RoundedPanel(int radius, boolean shadow) {
        this.radius = radius;
        this.shadow = shadow;
        this.background = Theme.SURFACE;
        setOpaque(false);
    }

    public void setCardBackground(Color color) {
        this.background = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int shadowOffset = shadow ? 3 : 0;
        int w = getWidth() - shadowOffset;
        int h = getHeight() - shadowOffset;

        if (shadow) {
            g2.setColor(Theme.SHADOW);
            g2.fillRoundRect(shadowOffset, shadowOffset, w, h, radius, radius);
        }

        g2.setColor(background);
        g2.fillRoundRect(0, 0, w, h, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}