package filters;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CancellationException;

public final class ImageFilters {
    private ImageFilters() { }
    public static BufferedImage applyAll(BufferedImage source, List<? extends ImageFilter> filters) {
        BufferedImage result = source;
        for (ImageFilter filter : filters) {
            checkCancelled();
            result = filter.apply(result);
        }
        return result;
    }
    private static void checkCancelled() {
        if (Thread.currentThread().isInterrupted()) throw new CancellationException();
    }
    private static int clamp(int value) { return Math.max(0, Math.min(255, value)); }
    private static int red(int p) { return (p >>> 16) & 255; }
    private static int green(int p) { return (p >>> 8) & 255; }
    private static int blue(int p) { return p & 255; }
    private static int rgb(int original, int r, int g, int b) {
        return (original & 0xff000000) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }
    private static int gray(int p) {
        return (299 * red(p) + 587 * green(p) + 114 * blue(p) + 500) / 1000;
    }
    // Am Bildrand wiederholen wir den nächstgelegenen Pixel.
    private static int pixelAt(BufferedImage image, int x, int y) {
        return image.getRGB(Math.max(0, Math.min(image.getWidth() - 1, x)),
                Math.max(0, Math.min(image.getHeight() - 1, y)));
    }
    private interface PixelOperation { int apply(int pixel); }
    private static BufferedImage map(BufferedImage source, PixelOperation operation) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            checkCancelled();
            for (int x = 0; x < source.getWidth(); x++) result.setRGB(x, y, operation.apply(source.getRGB(x, y)));
        }
        return result;
    }
    public static BufferedImage invert(BufferedImage source) {
        return map(source, p -> rgb(p, 255 - red(p), 255 - green(p), 255 - blue(p)));
    }
    public static BufferedImage threshold(BufferedImage source, int threshold) {
        return map(source, p -> {
            int value = gray(p) >= threshold ? 255 : 0;
            return rgb(p, value, value, value);
        });
    }
    public static BufferedImage brightnessContrast(BufferedImage source, int brightness, double contrast) {
        return map(source, p -> rgb(p,
                (int) Math.round((red(p) - 128) * contrast + 128 + brightness),
                (int) Math.round((green(p) - 128) * contrast + 128 + brightness),
                (int) Math.round((blue(p) - 128) * contrast + 128 + brightness)));
    }
    /** Gauß-Faltung: erst horizontal, dann vertikal. */
    public static BufferedImage gaussianBlur(BufferedImage source, int radius) {
        if (radius == 0) return map(source, p -> p);
        double sigma = Math.max(0.5, radius / 2.0);
        double[] weights = new double[2 * radius + 1];
        double sum = 0;
        for (int i = -radius; i <= radius; i++) {
            weights[i + radius] = Math.exp(-(i * i) / (2 * sigma * sigma));
            sum += weights[i + radius];
        }
        for (int i = 0; i < weights.length; i++) weights[i] /= sum;
        return blurPass(blurPass(source, weights, true), weights, false);
    }
    private static BufferedImage blurPass(BufferedImage source, double[] weights, boolean horizontal) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int radius = weights.length / 2;
        for (int y = 0; y < source.getHeight(); y++) {
            checkCancelled();
            for (int x = 0; x < source.getWidth(); x++) {
                double r = 0, g = 0, b = 0, alphaWeight = 0;
                // Unsichtbare Pixel sollen keine Farbsäume verursachen.
                for (int i = -radius; i <= radius; i++) {
                    int p = pixelAt(source, x + (horizontal ? i : 0), y + (horizontal ? 0 : i));
                    double weight = weights[i + radius] * (p >>> 24) / 255.0;
                    r += red(p) * weight;
                    g += green(p) * weight;
                    b += blue(p) * weight;
                    alphaWeight += weight;
                }
                if (alphaWeight > 0) { r /= alphaWeight; g /= alphaWeight; b /= alphaWeight; }
                result.setRGB(x, y, rgb(source.getRGB(x, y), (int) Math.round(r), (int) Math.round(g), (int) Math.round(b)));
            }
        }
        return result;
    }
    public static BufferedImage sharpen(BufferedImage source, double strength) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            checkCancelled();
            for (int x = 0; x < source.getWidth(); x++) {
                int center = source.getRGB(x, y);
                int[] neighbors = {pixelAt(source, x - 1, y), pixelAt(source, x + 1, y),
                        pixelAt(source, x, y - 1), pixelAt(source, x, y + 1)};
                int r = 0, g = 0, b = 0;
                for (int p : neighbors) { r += red(p); g += green(p); b += blue(p); }
                result.setRGB(x, y, rgb(center,
                        (int) Math.round(red(center) + strength * (4 * red(center) - r)),
                        (int) Math.round(green(center) + strength * (4 * green(center) - g)),
                        (int) Math.round(blue(center) + strength * (4 * blue(center) - b))));
            }
        }
        return result;
    }
    /** Sobel erkennt Helligkeitsänderungen in beiden Richtungen. */
    public static BufferedImage edges(BufferedImage source, double strength) {
        int[][] kernelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] kernelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            checkCancelled();
            for (int x = 0; x < source.getWidth(); x++) {
                int gx = 0, gy = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int value = gray(pixelAt(source, x + dx, y + dy));
                        gx += value * kernelX[dy + 1][dx + 1];
                        gy += value * kernelY[dy + 1][dx + 1];
                    }
                }
                int value = clamp((int) Math.round(Math.hypot(gx, gy) * strength));
                result.setRGB(x, y, rgb(source.getRGB(x, y), value, value, value));
            }
        }
        return result;
    }
}
