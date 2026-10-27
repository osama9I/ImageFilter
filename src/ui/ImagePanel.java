package ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Nur die Anzeige wird skaliert; die Bilddaten behalten ihre Auflösung. */
public class ImagePanel extends JPanel {
    private BufferedImage image;

    public ImagePanel() { setBackground(new Color(235, 235, 235)); }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (image == null) {
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawString("Bitte ein JPG- oder PNG-Bild öffnen.", 15, 30);
            return;
        }
        Graphics2D g = (Graphics2D) graphics.create();
        double scale = Math.min((double) Math.max(1, getWidth() - 20) / image.getWidth(),
                (double) Math.max(1, getHeight() - 20) / image.getHeight());
        int width = Math.max(1, (int) (image.getWidth() * scale));
        int height = Math.max(1, (int) (image.getHeight() * scale));
        int left = (getWidth() - width) / 2;
        int top = (getHeight() - height) / 2;
        g.clipRect(left, top, width, height);
        for (int y = top; y < top + height; y += 16) {
            for (int x = left; x < left + width; x += 16) {
                g.setColor(((x - left) / 16 + (y - top) / 16) % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY);
                g.fillRect(x, y, 16, 16);
            }
        }
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, left, top, width, height, null);
        g.dispose();
    }
}
