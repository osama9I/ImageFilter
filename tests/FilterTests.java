import filters.*;
import imaging.ImageFiles;
import ui.ImagePanel;
import ui.ImageFilterFrame;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CancellationException;

/** Ohne Testbibliothek: eine fehlgeschlagene Prüfung wirft einen AssertionError. */
public class FilterTests {
    private static int checks;

    public static void main(String[] args) throws Exception {
        testPixels();
        testNeighborhoods();
        testPipeline();
        testFiles();
        testDisplay();
        if (args.length > 0 && args[0].equals("--gui")) {
            SwingUtilities.invokeAndWait(() -> {
                ImageFilterFrame frame = new ImageFilterFrame();
                frame.setVisible(true);
                check(frame.isShowing(), "Fenster startet");
                frame.dispose();
            });
        }
        System.out.println("OK: " + checks + " Prüfungen bestanden.");
    }

    private static BufferedImage image(int... pixels) {
        BufferedImage image = new BufferedImage(pixels.length, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, pixels.length, 1, pixels, 0, pixels.length);
        return image;
    }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static void equal(BufferedImage a, BufferedImage b, String message) {
        check(a.getWidth() == b.getWidth() && a.getHeight() == b.getHeight(), message + " Größe");
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) check(a.getRGB(x, y) == b.getRGB(x, y), message);
        }
    }

    private static void testPixels() {
        BufferedImage source = image(0xff000000, 0xffffffff, 0x80ff0000, 0x00123456);
        BufferedImage before = image(0xff000000, 0xffffffff, 0x80ff0000, 0x00123456);
        equal(ImageFilters.invert(source), image(0xffffffff, 0xff000000, 0x8000ffff, 0x00edcba9), "Invert und Alpha");
        equal(ImageFilters.invert(ImageFilters.invert(source)), source, "Doppelte Invertierung");
        equal(ImageFilters.brightnessContrast(source, 0, 1), source, "Neutrale Helligkeit/Kontrast");
        equal(ImageFilters.brightnessContrast(image(0xfff01080), 30, 1), image(0xffff2e9e), "Helligkeit begrenzen");
        equal(ImageFilters.brightnessContrast(image(0xff4064c8), 0, 2), image(0xff0048ff), "Kontrast berechnen");
        equal(ImageFilters.threshold(image(0xff7f7f7f, 0xff808080, 0x80ffffff), 128),
                image(0xff000000, 0xffffffff, 0x80ffffff), "Schwellwertgrenze");
        equal(ImageFilters.threshold(image(0xffff0000, 0xff00ff00, 0xff0000ff), 128),
                image(0xff000000, 0xffffffff, 0xff000000), "Gewichtete Helligkeit");
        equal(source, before, "Original unverändert");
    }

    private static void testNeighborhoods() {
        BufferedImage flat = image(0xff505050, 0xff505050, 0xff505050);
        equal(ImageFilters.gaussianBlur(flat, 2), flat, "Gauß erhält konstante Farbe");
        equal(ImageFilters.sharpen(flat, 1), flat, "Schärfung erhält konstante Farbe");
        equal(ImageFilters.edges(flat, 1), image(0xff000000, 0xff000000, 0xff000000), "Keine Kanten auf konstanter Fläche");
        BufferedImage impulse = image(0xff000000, 0xffffffff, 0xff000000);
        BufferedImage blurred = ImageFilters.gaussianBlur(impulse, 1);
        int center = blurred.getRGB(1, 0) & 255;
        check(center > 0 && center < 255, "Gauß verteilt hellen Pixel");
        check((blurred.getRGB(0, 0) & 255) > 0, "Nachbar wird heller");
        check(blurred.getRGB(0, 0) == blurred.getRGB(2, 0), "Gauß ist symmetrisch");
        equal(ImageFilters.sharpen(image(0xff646464, 0xff969696, 0xff646464), 1),
                image(0xff323232, 0xfffafafa, 0xff323232), "Schärfung bekannte Werte");
        BufferedImage edge = ImageFilters.edges(image(0xff000000, 0xff000000, 0xffffffff), 1);
        check((edge.getRGB(0, 0) & 255) == 0 && (edge.getRGB(1, 0) & 255) == 255, "Sobel findet Übergang");
        equal(ImageFilters.gaussianBlur(image(0x00ff0000, 0xff0000ff, 0x00ff0000), 1),
                image(0x00000000, 0xff0000ff, 0x00000000), "Unsichtbares Rot erzeugt keinen Saum");
        for (FilterType type : FilterType.values()) {
            BufferedImage one = image(0x80706050);
            BufferedImage result = new FilterStep(type).apply(one);
            check(result != one, type + " liefert neues Bild");
            check(result.getWidth() == 1 && result.getHeight() == 1, type + " verarbeitet 1×1");
            check((result.getRGB(0, 0) >>> 24) == 128, type + " erhält Alpha");
            check(one.getRGB(0, 0) == 0x80706050, type + " verändert Original nicht");
        }
        BufferedImage square = new BufferedImage(9, 7, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 7; y++) for (int x = 0; x < 9; x++) square.setRGB(x, y, 0x80ffffff);
        equal(ImageFilters.gaussianBlur(square, 10), square, "Großer Radius auf kleinem Bild");
        equal(ImageFilters.gaussianBlur(square, 0), square, "Radius null");
        equal(ImageFilters.sharpen(square, 0), square, "Schärfe null");
    }

    private static void testPipeline() {
        BufferedImage source = image(0xff646464);
        FilterStep brightness = new FilterStep(FilterType.BRIGHTNESS_CONTRAST);
        brightness.setAmount(50);
        FilterStep threshold = new FilterStep(FilterType.THRESHOLD);
        check(ImageFilters.applyAll(source, List.of(brightness, threshold)).getRGB(0, 0) == 0xffffffff, "Reihenfolge Helligkeit vor Threshold");
        check(ImageFilters.applyAll(source, List.of(threshold, brightness)).getRGB(0, 0) == 0xff323232, "Umgekehrte Reihenfolge");
        equal(ImageFilters.applyAll(source, List.of()), source, "Leere Liste");
        FilterStep copy = brightness.copy();
        brightness.setAmount(100);
        check(copy.getAmount() == 50, "Einstellungskopie unabhängig");
        check(new FilterStep(FilterType.BRIGHTNESS_CONTRAST).getAmount() == 0, "Instanzen unabhängig");
        boolean cancelled = false;
        Thread.currentThread().interrupt();
        try { ImageFilters.invert(source); } catch (CancellationException expected) { cancelled = true; }
        finally { Thread.interrupted(); }
        check(cancelled, "Veraltete Berechnung abbrechbar");
    }

    private static void testFiles() throws IOException {
        Path directory = Files.createTempDirectory("filterimage-tests-");
        Path png = directory.resolve("test.png");
        Path jpg = directory.resolve("test.jpg");
        Path invalid = directory.resolve("invalid.png");
        Path gif = directory.resolve("other.gif");
        try {
            BufferedImage rgba = image(0x80ff0000, 0xff0000ff);
            check(ImageIO.write(rgba, "png", png.toFile()), "PNG erzeugt");
            equal(ImageFiles.read(png.toFile()), rgba, "PNG laden mit Transparenz");
            BufferedImage rgb = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
            check(ImageIO.write(rgb, "jpg", jpg.toFile()), "JPG erzeugt");
            BufferedImage loaded = ImageFiles.read(jpg.toFile());
            check(loaded.getWidth() == 4 && loaded.getHeight() == 3, "JPG laden");
            Files.writeString(invalid, "Kein Bild");
            boolean rejected = false;
            try { ImageFiles.read(invalid.toFile()); } catch (IOException expected) { rejected = true; }
            check(rejected, "Ungültige Datei abgewiesen");
            ImageIO.write(rgb, "gif", gif.toFile());
            rejected = false;
            try { ImageFiles.read(gif.toFile()); } catch (IOException expected) { rejected = true; }
            check(rejected, "Andere Formate abgewiesen");
        } finally {
            Files.deleteIfExists(png);
            Files.deleteIfExists(jpg);
            Files.deleteIfExists(invalid);
            Files.deleteIfExists(gif);
            Files.deleteIfExists(directory);
        }
    }

    private static void testDisplay() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ImagePanel panel = new ImagePanel();
            panel.setSize(300, 200);
            BufferedImage canvas = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            panel.paint(g);
            panel.setImage(image(0x80ff0000));
            panel.paint(g);
            g.dispose();
            check((canvas.getRGB(150, 100) >>> 24) == 255, "Bildanzeige auf Schachbrett gerendert");
        });
    }
}
