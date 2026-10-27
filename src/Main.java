import javax.swing.SwingUtilities;
import ui.ImageFilterFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageFilterFrame().setVisible(true));
    }
}
