package gui;

import service.TranslationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dark-green vertical navigation sidebar: brand header, one nav item
 * per feature (with icon + active-state highlighting), and a small
 * motivational footer. Purely a navigation/visual component — all
 * actual screen switching still goes through MainFrame.showCard(...)
 * via the provided callback, so CardLayout logic is untouched.
 */
public class Sidebar extends JPanel implements Localizable {

    private final TranslationService translation;
    private final Consumer<String> onNavigate;
    private final List<NavItem> items = new ArrayList<>();
    private String activeCard;

    public Sidebar(TranslationService translation, Consumer<String> onNavigate, String initialActive) {
        this.translation = translation;
        this.onNavigate = onNavigate;
        this.activeCard = initialActive;

        setLayout(new BorderLayout());
        setBackground(Theme.PRIMARY_DARK);
        setPreferredSize(new Dimension(230, 10));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // ---- Brand header ----
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel logoLine = new JLabel(IconPainter.of(IconPainter.Kind.LEAF, 22, Theme.PRIMARY_LIGHT));
        JLabel brandName = new JLabel("Farm Assist");
        brandName.setFont(Theme.h1());
        brandName.setForeground(Theme.TEXT_ON_DARK);

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandRow.setOpaque(false);
        brandRow.add(logoLine);
        brandRow.add(brandName);
        brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html><body style='width:150px'>Smart Farming. Better Future.</body></html>");
        tagline.setFont(Theme.small());
        tagline.setForeground(Theme.TEXT_ON_DARK_MUTED);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagline.setBorder(BorderFactory.createEmptyBorder(4, 30, 0, 0));

        brand.add(brandRow);
        brand.add(tagline);
        add(brand, BorderLayout.NORTH);

        // ---- Nav items ----
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        addNavItem(navPanel, IconPainter.Kind.HOME, "Dashboard", MainFrame.DASHBOARD);
        addNavItem(navPanel, IconPainter.Kind.LEAF, "Crop Information", MainFrame.CROP_SEARCH);
        addNavItem(navPanel, IconPainter.Kind.BUG, "Disease Information", MainFrame.DISEASE_SEARCH);
        addNavItem(navPanel, IconPainter.Kind.BAG, "Fertilizer Information", MainFrame.FERTILIZER_SEARCH);
        addNavItem(navPanel, IconPainter.Kind.SEARCH, "Search by Multiple Symptoms", MainFrame.SYMPTOM_SEARCH);
        addNavItem(navPanel, IconPainter.Kind.SEARCH, "Search Agricultural Corpus", MainFrame.CORPUS_SEARCH);
        addNavItem(navPanel, IconPainter.Kind.CHART, "Crop Recommendation", MainFrame.CROP_RECOMMEND);
        addNavItem(navPanel, IconPainter.Kind.DROPLET, "Fertilizer Allocation", MainFrame.FERTILIZER_ALLOCATION);
        addNavItem(navPanel, IconPainter.Kind.CAMERA, "Identify Disease from Photo", MainFrame.PHOTO_DISEASE);
        addNavItem(navPanel, IconPainter.Kind.INFO, "About", MainFrame.ABOUT);

        JScrollPane scroll = new JScrollPane(navPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll, BorderLayout.CENTER);

        // ---- Footer quote ----
        RoundedPanel footer = new RoundedPanel(Theme.RADIUS_SM, false);
        footer.setCardBackground(new Color(0x14261B));
        footer.setLayout(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel quote = new JLabel("<html><i>\"Healthy Crops, Happy Farmers, Better Tomorrow.\"</i></html>");
        quote.setFont(Theme.small());
        quote.setForeground(Theme.TEXT_ON_DARK_MUTED);
        footer.add(quote, BorderLayout.CENTER);

        JPanel footerWrap = new JPanel(new BorderLayout());
        footerWrap.setOpaque(false);
        footerWrap.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        footerWrap.add(footer, BorderLayout.CENTER);
        add(footerWrap, BorderLayout.SOUTH);

        refreshLanguage();
    }

    private void addNavItem(JPanel container, IconPainter.Kind icon, String englishKey, String cardName) {
        NavItem item = new NavItem(icon, englishKey, cardName);
        items.add(item);
        container.add(item);
    }

    public void setActive(String cardName) {
        this.activeCard = cardName;
        for (NavItem item : items) item.updateState();
    }

    @Override
    public void refreshLanguage() {
        for (NavItem item : items) item.updateLabel();
    }

    /** One clickable navigation row: icon + label, with active/hover states. */
    private class NavItem extends JPanel {
        private final IconPainter.Kind icon;
        private final String englishKey;
        private final String cardName;
        private final JLabel label;
        private final JLabel iconLabel;
        private boolean hovering = false;

        NavItem(IconPainter.Kind icon, String englishKey, String cardName) {
            this.icon = icon;
            this.englishKey = englishKey;
            this.cardName = cardName;

            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            iconLabel = new JLabel();
            label = new JLabel();
            label.setFont(Theme.body());
            add(iconLabel);
            add(label);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onNavigate.accept(cardName); }
                @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
            });

            updateState();
        }

        void updateLabel() {
            label.setText(translation.t(englishKey));
        }

        void updateState() {
            boolean active = cardName.equals(activeCard);
            Color fg = Theme.TEXT_ON_DARK;
            iconLabel.setIcon(IconPainter.of(icon, 18, fg));
            label.setForeground(fg);
            label.setFont(active ? Theme.bodyBold() : Theme.body());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            boolean active = cardName.equals(activeCard);
            if (active || hovering) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? Theme.SIDEBAR_ACTIVE : Theme.SIDEBAR_HOVER);
                g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, Theme.RADIUS_SM, Theme.RADIUS_SM);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}