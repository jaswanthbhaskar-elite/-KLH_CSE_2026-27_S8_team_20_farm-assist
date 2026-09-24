package gui;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Hand-drawn flat vector icons via Graphics2D — no external icon
 * library or image files, so no new dependency and no licensing
 * concerns. Each icon is a simple, modern, single-color glyph drawn
 * to fit an arbitrary square size.
 */
public final class IconPainter {

    private IconPainter() {}

    public enum Kind { HOME, LEAF, BUG, BAG, SEARCH, CHART, DROPLET, CAMERA, INFO, ARROW_RIGHT, LANGUAGE }

    public static Icon of(Kind kind, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(color);
                g2.setStroke(new java.awt.BasicStroke(Math.max(1.5f, size / 10f),
                        java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                draw(g2, kind, size);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static void draw(Graphics2D g2, Kind kind, int s) {
        switch (kind) {
            case HOME -> {
                Path2D p = new Path2D.Double();
                p.moveTo(s * 0.5, s * 0.12);
                p.lineTo(s * 0.88, s * 0.42);
                p.lineTo(s * 0.76, s * 0.42);
                p.lineTo(s * 0.76, s * 0.88);
                p.lineTo(s * 0.24, s * 0.88);
                p.lineTo(s * 0.24, s * 0.42);
                p.lineTo(s * 0.12, s * 0.42);
                p.closePath();
                g2.draw(p);
                g2.draw(new RoundRectangle2D.Double(s * 0.42, s * 0.60, s * 0.16, s * 0.28, 3, 3));
            }
            case LEAF -> {
                Path2D leaf = new Path2D.Double();
                leaf.moveTo(s * 0.5, s * 0.1);
                leaf.curveTo(s * 0.95, s * 0.15, s * 0.9, s * 0.75, s * 0.5, s * 0.92);
                leaf.curveTo(s * 0.1, s * 0.75, s * 0.05, s * 0.15, s * 0.5, s * 0.1);
                leaf.closePath();
                g2.draw(leaf);
                g2.draw(new java.awt.geom.Line2D.Double(s * 0.5, s * 0.2, s * 0.5, s * 0.85));
            }
            case BUG -> {
                g2.draw(new Ellipse2D.Double(s * 0.3, s * 0.32, s * 0.4, s * 0.5));
                g2.draw(new Ellipse2D.Double(s * 0.38, s * 0.14, s * 0.24, s * 0.22));
                for (double dx : new double[]{-1, 1}) {
                    g2.draw(new java.awt.geom.Line2D.Double(s * 0.5, s * 0.4, s * 0.5 + dx * s * 0.28, s * 0.3));
                    g2.draw(new java.awt.geom.Line2D.Double(s * 0.5, s * 0.55, s * 0.5 + dx * s * 0.32, s * 0.55));
                    g2.draw(new java.awt.geom.Line2D.Double(s * 0.5, s * 0.7, s * 0.5 + dx * s * 0.28, s * 0.82));
                }
            }
            case BAG -> {
                g2.draw(new RoundRectangle2D.Double(s * 0.18, s * 0.32, s * 0.64, s * 0.56, 8, 8));
                Path2D handle = new Path2D.Double();
                handle.moveTo(s * 0.34, s * 0.32);
                handle.curveTo(s * 0.34, s * 0.1, s * 0.66, s * 0.1, s * 0.66, s * 0.32);
                g2.draw(handle);
            }
            case SEARCH -> {
                g2.draw(new Ellipse2D.Double(s * 0.15, s * 0.15, s * 0.5, s * 0.5));
                g2.draw(new java.awt.geom.Line2D.Double(s * 0.58, s * 0.58, s * 0.88, s * 0.88));
            }
            case CHART -> {
                g2.draw(new java.awt.geom.Line2D.Double(s * 0.15, s * 0.88, s * 0.15, s * 0.15));
                g2.draw(new java.awt.geom.Line2D.Double(s * 0.15, s * 0.88, s * 0.9, s * 0.88));
                g2.fill(new RoundRectangle2D.Double(s * 0.28, s * 0.55, s * 0.14, s * 0.33, 3, 3));
                g2.fill(new RoundRectangle2D.Double(s * 0.48, s * 0.38, s * 0.14, s * 0.5, 3, 3));
                g2.fill(new RoundRectangle2D.Double(s * 0.68, s * 0.22, s * 0.14, s * 0.66, 3, 3));
            }
            case DROPLET -> {
                Path2D drop = new Path2D.Double();
                drop.moveTo(s * 0.5, s * 0.1);
                drop.curveTo(s * 0.85, s * 0.5, s * 0.8, s * 0.9, s * 0.5, s * 0.9);
                drop.curveTo(s * 0.2, s * 0.9, s * 0.15, s * 0.5, s * 0.5, s * 0.1);
                drop.closePath();
                g2.draw(drop);
            }
            case CAMERA -> {
                g2.draw(new RoundRectangle2D.Double(s * 0.12, s * 0.3, s * 0.76, s * 0.54, 8, 8));
                g2.draw(new Ellipse2D.Double(s * 0.36, s * 0.42, s * 0.28, s * 0.28));
                g2.fill(new RoundRectangle2D.Double(s * 0.38, s * 0.16, s * 0.24, s * 0.16, 4, 4));
            }
            case INFO -> {
                g2.draw(new Ellipse2D.Double(s * 0.1, s * 0.1, s * 0.8, s * 0.8));
                g2.fill(new Ellipse2D.Double(s * 0.46, s * 0.26, s * 0.08, s * 0.08));
                g2.draw(new java.awt.geom.Line2D.Double(s * 0.5, s * 0.44, s * 0.5, s * 0.72));
            }
            case ARROW_RIGHT -> {
                g2.draw(new java.awt.geom.Line2D.Double(s * 0.2, s * 0.5, s * 0.75, s * 0.5));
                g2.draw(new java.awt.geom.Line2D.Double(s * 0.55, s * 0.3, s * 0.78, s * 0.5));
                g2.draw(new java.awt.geom.Line2D.Double(s * 0.55, s * 0.7, s * 0.78, s * 0.5));
            }
            case LANGUAGE -> {
                g2.draw(new Ellipse2D.Double(s * 0.1, s * 0.1, s * 0.8, s * 0.8));
                g2.draw(new java.awt.geom.Line2D.Double(s * 0.1, s * 0.5, s * 0.9, s * 0.5));
                g2.draw(new Ellipse2D.Double(s * 0.32, s * 0.1, s * 0.36, s * 0.8));
            }
        }
    }
}