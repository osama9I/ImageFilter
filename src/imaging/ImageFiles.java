package imaging;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

public final class ImageFiles {
    private ImageFiles() { }

    public static BufferedImage read(File file) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            if (input == null) throw new IOException("Die Datei kann nicht geöffnet werden.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("Keine gültige JPG- oder PNG-Datei.");
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("png") && !format.equals("jpeg") && !format.equals("jpg")) {
                    throw new IOException("Bitte ein JPG- oder PNG-Bild auswählen.");
                }
                reader.setInput(input, true, true);
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        }
    }
}
