package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Premium launch/welcome screen shown briefly when the application
 * starts, before the main dashboard appears. Purely presentational —
 * it does not touch FarmAssistService, TranslationService, or the
 * classifier; GuiMain constructs MainFrame exactly as before and just
 * shows this first.
 *
 * The hero visual is the real project photo at assets/crop.jpg,
 * loaded via ImageIcon/Image (no Base64, no procedural redraw).
 */
public class SplashScreen extends JWindow {

    private static final String IMAGE_PATH = "assets/crop.jpg";

    public SplashScreen(Runnable onFinished) {
        setSize(720, 440);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY_DARK, 1));

        // ---- Left: branding + welcome message ----
        JPanel left = new JPanel();
        left.setBackground(Theme.SURFACE);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 32));

        JLabel leafIcon = new JLabel(IconPainter.of(IconPainter.Kind.LEAF, 40, Theme.PRIMARY));
        leafIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcome = new JLabel("<html>Welcome to<br><span style='color:#2E7D32;'>Farm Assist</span></html>");
        welcome.setFont(Theme.display());
        welcome.setForeground(Theme.TEXT_DARK);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcome.setBorder(BorderFactory.createEmptyBorder(16, 0, 12, 0));

        JLabel subtitle = new JLabel("<html><body style='width:260px'>" +
                "Your intelligent companion for smarter farming decisions.</body></html>");
        subtitle.setFont(Theme.body());
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JProgressBar loading = new JProgressBar();
        loading.setIndeterminate(true);
        loading.setForeground(Theme.PRIMARY_LIGHT);
        loading.setBorder(BorderFactory.createEmptyBorder());
        loading.setPreferredSize(new Dimension(220, 6));
        loading.setMaximumSize(new Dimension(220, 6));
        loading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel loadingLabel = new JLabel("Loading Farm Assist...");
        loadingLabel.setFont(Theme.small());
        loadingLabel.setForeground(Theme.TEXT_MUTED);
        loadingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 6, 0));

        left.add(leafIcon);
        left.add(welcome);
        left.add(subtitle);
        left.add(Box.createVerticalGlue());
        left.add(loadingLabel);
        left.add(loading);

        // ---- Right: the real project hero photo, loaded via ImageIcon ----
        ImageIcon loadedIcon = new ImageIcon(IMAGE_PATH);
        Image heroImage = (loadedIcon.getIconWidth() > 0) ? loadedIcon.getImage() : null;

        HeroImagePanel heroPanel = new HeroImagePanel(
                heroImage, "Rooted in data. Growing with you.");
        heroPanel.setPreferredSize(new Dimension(320, 440));
        heroPanel.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 16));
        heroPanel.setOpaque(false);

        JPanel heroWrap = new JPanel(new BorderLayout());
        heroWrap.setBackground(Theme.SURFACE);
        heroWrap.add(heroPanel, BorderLayout.CENTER);

        root.add(left, BorderLayout.CENTER);
        root.add(heroWrap, BorderLayout.EAST);
        setContentPane(root);

        // Show briefly, then hand off to the real application window.
        Timer timer = new Timer(2200, e -> {
            dispose();
            onFinished.run();
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Paints the given image inside a rounded-corner frame, scaled to
     * fill the panel while preserving aspect ratio (aspect-fill /
     * "cover" behavior — no stretching, no distortion; any overflow is
     * cropped rather than letterboxed, which is what makes it read as
     * an intentional hero photo rather than a pasted-in picture). A
     * subtle bottom gradient plus a short caption is drawn on top for
     * a polished, editorial feel. Falls back to a plain placeholder
     * (no crash, no fake image) if the file couldn't be loaded.
     */
    private static class HeroImagePanel extends JPanel {
        private final Image image;
        private final String caption;

        HeroImagePanel(Image image, String caption) {
            this.image = image;
            this.caption = caption;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth(), h = getHeight();
            int radius = Theme.RADIUS_LG;
            RoundRectangle2D frame = new RoundRectangle2D.Double(0, 0, w, h, radius, radius);
            g2.setClip(frame);

            g2.setColor(Theme.PRIMARY_DARK);
            g2.fillRect(0, 0, w, h);

            if (image != null) {
                int imgW = image.getWidth(this);
                int imgH = image.getHeight(this);
                if (imgW > 0 && imgH > 0) {
                    // Aspect-fill: scale so the image fully covers the
                    // frame on both axes, then center-crop the overflow.
                    double scale = Math.max((double) w / imgW, (double) h / imgH);
                    int drawW = (int) Math.ceil(imgW * scale);
                    int drawH = (int) Math.ceil(imgH * scale);
                    int x = (w - drawW) / 2;
                    int y = (h - drawH) / 2;
                    g2.drawImage(image, x, y, drawW, drawH, this);
                }

                // Bottom gradient for caption legibility over the photo.
                GradientPaint gradient = new GradientPaint(
                        0, h * 0.55f, new Color(0, 0, 0, 0),
                        0, h, new Color(0, 0, 0, 150));
                g2.setPaint(gradient);
                g2.fillRect(0, (int) (h * 0.55), w, (int) (h * 0.45) + 1);

                g2.setColor(Color.WHITE);
                g2.setFont(Theme.bodyBold());
                g2.drawString(caption, 18, h - 22);
            } else {
                // Honest fallback — no fake image, no crash — if the
                // file genuinely can't be found on disk.
                g2.setColor(Theme.TEXT_ON_DARK_MUTED);
                g2.setFont(Theme.small());
                g2.drawString("Image not found: " + IMAGE_PATH, 16, h / 2);
            }

            g2.setClip(null);
            g2.setColor(Theme.PRIMARY_DARK);
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, radius, radius));
            g2.dispose();
        }
    }
}