package gui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Random;

/**
 * An original, hand-drawn stylized plant/leaf illustration used in the
 * splash screen and dashboard hero area, in place of a bundled stock
 * photo. Painted procedurally with Graphics2D (gradients + leaf
 * shapes + soft "bokeh" circles), so there is no external image
 * asset, no licensing concern, and no added binary dependency.
 *
 * Deterministic seed so the composition looks the same on every run
 * rather than jittering between launches.
 */
public class PlantIllustration extends JPanel {

    private final boolean dark;

    public PlantIllustration(boolean darkBackground) {
        this.dark = darkBackground;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background gradient: deep green to a slightly lighter green.
        GradientPaint bg = dark
                ? new GradientPaint(0, 0, new Color(0x0F2A1B), w, h, new Color(0x1F4530))
                : new GradientPaint(0, 0, Theme.SURFACE_ALT, w, h, Theme.BACKGROUND);
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // Soft glowing circles for depth ("bokeh").
        Random rnd = new Random(42);
        for (int i = 0; i < 6; i++) {
            int r = 40 + rnd.nextInt(90);
            int cx = rnd.nextInt(Math.max(1, w));
            int cy = rnd.nextInt(Math.max(1, h));
            g2.setColor(new Color(255, 255, 255, dark ? 10 : 14));
            g2.fill(new Ellipse2D.Double(cx - r / 2.0, cy - r / 2.0, r, r));
        }

        // A cluster of stylized leaves, rising from lower-left toward
        // upper-right, at varying sizes for depth.
        drawLeafStem(g2, w * 0.30, h * 1.05, w * 0.34, h * 0.35, 0.9);
        drawLeafStem(g2, w * 0.55, h * 1.05, w * 0.60, h * 0.18, 1.15);
        drawLeafStem(g2, w * 0.78, h * 1.05, w * 0.72, h * 0.45, 0.75);

        g2.dispose();
    }

    /** Draws one stem with a pair of leaves, from (baseX,baseY) toward (tipX,tipY). */
    private void drawLeafStem(Graphics2D g2, double baseX, double baseY, double tipX, double tipY, double scale) {
        Color stemColor = dark ? new Color(0x6FBF7A) : new Color(0x4CAF50);
        Color leafColor = dark ? new Color(0x8FDD9A, true) : new Color(0x66BB6A);

        g2.setStroke(new java.awt.BasicStroke((float) (4 * scale)));
        g2.setColor(stemColor);
        Path2D stem = new Path2D.Double();
        stem.moveTo(baseX, baseY);
        double midX = (baseX + tipX) / 2 + 20 * scale;
        double midY = (baseY + tipY) / 2;
        stem.curveTo(baseX, baseY - 60 * scale, midX, midY, tipX, tipY);
        g2.draw(stem);

        // Leaves along the stem.
        double[] positions = {0.35, 0.6, 0.85};
        for (int i = 0; i < positions.length; i++) {
            double t = positions[i];
            double x = baseX + (tipX - baseX) * t;
            double y = baseY + (tipY - baseY) * t;
            double leafSize = 55 * scale * (1.0 - t * 0.3);
            double angle = (i % 2 == 0 ? -1 : 1) * 0.6;
            drawLeaf(g2, x, y, leafSize, angle, leafColor);
        }
        // Tip leaf.
        drawLeaf(g2, tipX, tipY, 65 * scale, -0.2, leafColor);
    }

    private void drawLeaf(Graphics2D g2, double x, double y, double size, double angle, Color color) {
        Graphics2D lg = (Graphics2D) g2.create();
        lg.translate(x, y);
        lg.rotate(angle);
        Path2D leaf = new Path2D.Double();
        leaf.moveTo(0, 0);
        leaf.curveTo(size * 0.6, -size * 0.5, size, -size * 0.1, size * 0.1, size * 0.05);
        leaf.curveTo(size, size * 0.15, size * 0.6, size * 0.55, 0, 0);
        leaf.closePath();
        lg.setColor(color);
        lg.fill(leaf);
        lg.setColor(color.darker());
        lg.setStroke(new java.awt.BasicStroke(1.5f));
        lg.draw(new java.awt.geom.Line2D.Double(0, 0, size * 0.75, 0));
        lg.dispose();
    }
}