package filters;

import java.awt.image.BufferedImage;

/** Jede Listenposition besitzt eigene Einstellungen. */
public class FilterStep implements ImageFilter {
    private final FilterType type;
    private int amount;
    private int contrast = 100;

    public FilterStep(FilterType type) {
        this.type = type;
        amount = switch (type) {
            case GAUSSIAN_BLUR -> 2;
            case THRESHOLD -> 128;
            case EDGE_DETECTION, SHARPENING -> 100;
            default -> 0;
        };
    }

    public FilterType getType() { return type; }
    public int getAmount() { return amount; }
    public int getContrast() { return contrast; }
    public void setAmount(int value) {
        int min = type == FilterType.BRIGHTNESS_CONTRAST ? -255 : 0;
        int max = switch (type) {
            case GAUSSIAN_BLUR -> 10;
            case EDGE_DETECTION, SHARPENING -> 300;
            default -> 255;
        };
        if (value < min || value > max) throw new IllegalArgumentException("Ungültige Filterstärke");
        amount = value;
    }
    public void setContrast(int value) {
        if (value < 0 || value > 300) throw new IllegalArgumentException("Ungültiger Kontrast");
        contrast = value;
    }
    public FilterStep copy() {
        FilterStep copy = new FilterStep(type);
        copy.amount = amount;
        copy.contrast = contrast;
        return copy;
    }
    @Override
    public BufferedImage apply(BufferedImage source) {
        return switch (type) {
            case INVERT -> ImageFilters.invert(source);
            case BRIGHTNESS_CONTRAST -> ImageFilters.brightnessContrast(source, amount, contrast / 100.0);
            case THRESHOLD -> ImageFilters.threshold(source, amount);
            case GAUSSIAN_BLUR -> ImageFilters.gaussianBlur(source, amount);
            case EDGE_DETECTION -> ImageFilters.edges(source, amount / 100.0);
            case SHARPENING -> ImageFilters.sharpen(source, amount / 100.0);
        };
    }
    @Override
    public String toString() { return type.toString(); }
}
