package filters;

public enum FilterType {
    GAUSSIAN_BLUR("Gaussian Blur"), THRESHOLD("Binarization / Thresholding"),
    EDGE_DETECTION("Edge Detection"), SHARPENING("Sharpening"),
    INVERT("Invert"), BRIGHTNESS_CONTRAST("Brightness / Contrast");

    private final String label;
    FilterType(String label) { this.label = label; }
    @Override
    public String toString() { return label; }
}
