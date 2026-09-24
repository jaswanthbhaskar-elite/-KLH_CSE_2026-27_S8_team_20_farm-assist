package gui;

import javax.swing.JLabel;

import java.awt.BorderLayout;

import java.awt.Cursor;
import java.awt.Dimension;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A clickable premium feature card: icon circle, title, short
 * description, and a trailing arrow. Used in the dashboard's feature
 * grid. Title/description text is set externally via setters so the
 * owning panel can keep them updated through TranslationService on
 * language change (this class does not depend on TranslationService
 * itself, keeping it reusable and simple).
 */
public class FeatureCard extends RoundedPanel {

    private final JLabel titleLabel;
    private final JLabel descriptionLabel;
    private final JLabel arrowLabel;
    private final IconBadge iconBadge;

    public FeatureCard(IconPainter.Kind icon, Runnable onClick) {
        super(Theme.RADIUS_MD, true);
        setCardBackground(Theme.SURFACE);
        setLayout(new BorderLayout(Theme.SPACE_MD, Theme.SPACE_SM));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(
                Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD, Theme.SPACE_MD));
        setPreferredSize(new Dimension(270, 175));

        iconBadge = new IconBadge(icon);
        add(iconBadge, BorderLayout.WEST);

        titleLabel = new JLabel();
        titleLabel.setFont(FontProvider.sans(Font.BOLD, 15));
        titleLabel.setForeground(Theme.TEXT_DARK);

        descriptionLabel = new JLabel();
        descriptionLabel.setFont(Theme.small());
        descriptionLabel.setForeground(Theme.TEXT_MUTED);

        javax.swing.JPanel textPanel = new javax.swing.JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new javax.swing.BoxLayout(textPanel, javax.swing.BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        descriptionLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        textPanel.add(titleLabel);
        textPanel.add(javax.swing.Box.createVerticalStrut(4));
        textPanel.add(descriptionLabel);
        add(textPanel, BorderLayout.CENTER);

        arrowLabel = new JLabel(IconPainter.of(IconPainter.Kind.ARROW_RIGHT, 18, Theme.PRIMARY));
        JPanelRight arrowWrap = new JPanelRight(arrowLabel);
        add(arrowWrap, BorderLayout.EAST);

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { onClick.run(); }
            @Override
            public void mouseEntered(MouseEvent e) { setCardBackground(Theme.SURFACE_ALT); repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { setCardBackground(Theme.SURFACE); repaint(); }
        });
    }

    public void setTitle(String text) {
        // Wrap via HTML (like description) so long/wide-font titles don't
        // get truncated with "..." — this shows up especially with wider
        // fallback fonts used when Nirmala UI/Noto Sans aren't installed.
        titleLabel.setText("<html><body style='width:175px'>" + text + "</body></html>");
    }
    public void setDescription(String text) {
        // Wrap long descriptions using simple HTML since JLabel has no built-in wrap.
        descriptionLabel.setText("<html><body style='width:175px'>" + text + "</body></html>");
    }

    /** Small colored circular badge behind the feature icon. */
    private static class IconBadge extends javax.swing.JPanel {
        private final IconPainter.Kind kind;
        IconBadge(IconPainter.Kind kind) {
            this.kind = kind;
            setOpaque(false);
            setPreferredSize(new Dimension(52, 52));
        }
        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.SURFACE_ALT);
            g2.fillOval(0, 0, 52, 52);
            g2.dispose();
            IconPainter.of(kind, 28, Theme.PRIMARY).paintIcon(this, g, 12, 12);
        }
    }

    /** Right-aligned, vertically-centered wrapper for the trailing arrow. */
    private static class JPanelRight extends javax.swing.JPanel {
        JPanelRight(JLabel label) {
            setOpaque(false);
            setLayout(new java.awt.GridBagLayout());
            add(label);
        }
    }
}