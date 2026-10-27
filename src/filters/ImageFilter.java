package filters;

import java.awt.image.BufferedImage;

/** Die Eingabe bleibt unverändert; ein Filter liefert ein neues Bild. */
public interface ImageFilter {
    BufferedImage apply(BufferedImage source);
}
