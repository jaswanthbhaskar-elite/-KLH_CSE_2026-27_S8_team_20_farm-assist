package gui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A rounded, filled button with a hover-state color shift. Replaces
 * plain JButton for primary actions (Search, Submit, CTA buttons)
 * across the redesigned UI. Text rendering still goes through
 * FontProvider via Theme.button(), so Telugu/Hindi keep working.
 */
public class StyledButton extends JButton {

    private Color baseColor;
    private Color hoverColor;
    private boolean hovering = false;

    public StyledButton(String text, Color baseColor, Color hoverColor) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        setFont(Theme.button());
        setForeground(Theme.TEXT_ON_DARK);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 22, 10, 22));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
        });
    }

    /** Convenience constructor: primary green button. */
    public StyledButton(String text) {
        this(text, Theme.PRIMARY, Theme.PRIMARY_HOVER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isEnabled() ? (hovering ? hoverColor : baseColor) : Theme.BORDER);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS_SM, Theme.RADIUS_SM);
        g2.dispose();
        super.paintComponent(g);
    }
}