package ui;

import filters.FilterStep;
import filters.FilterType;
import filters.ImageFilters;
import imaging.ImageFiles;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.IntConsumer;

public class ImageFilterFrame extends JFrame {
    private final DefaultListModel<FilterStep> model = new DefaultListModel<>();
    private final JList<FilterStep> filterList = new JList<>(model);
    private final JPanel settings = new JPanel();
    private final ImagePanel originalPanel = new ImagePanel();
    private final ImagePanel resultPanel = new ImagePanel();
    private final JLabel status = new JLabel("Bitte ein Bild öffnen.");
    private final JButton open = new JButton("Bild öffnen …");
    private final JButton remove = new JButton("Entfernen");
    private final JButton up = new JButton("Nach oben");
    private final JButton down = new JButton("Nach unten");
    private final Timer updateTimer = new Timer(150, event -> processImage());
    private BufferedImage original;
    private SwingWorker<BufferedImage, Void> processing;
    private SwingWorker<BufferedImage, Void> loading;
    private long revision;

    public ImageFilterFrame() {
        super("Image Filter");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1280, 780);
        setLocationRelativeTo(null);
        updateTimer.setRepeats(false);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(open);
        add(toolbar, BorderLayout.NORTH);
        open.addActionListener(event -> chooseImage());

        JPanel sidebar = new JPanel(new BorderLayout(8, 8));
        sidebar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        sidebar.setPreferredSize(new Dimension(310, 600));
        JComboBox<FilterType> types = new JComboBox<>(FilterType.values());
        JButton add = new JButton("Hinzufügen");
        JPanel addControls = new JPanel(new BorderLayout(4, 4));
        addControls.add(types, BorderLayout.CENTER);
        addControls.add(add, BorderLayout.SOUTH);
        sidebar.add(addControls, BorderLayout.NORTH);
        filterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filterList.setVisibleRowCount(8);
        JPanel listArea = new JPanel(new BorderLayout(4, 4));
        JScrollPane listScroll = new JScrollPane(filterList);
        listScroll.setBorder(BorderFactory.createTitledBorder("Filter – Reihenfolge von oben nach unten"));
        listArea.add(listScroll, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridLayout(1, 3, 3, 3));
        buttons.add(remove);
        buttons.add(up);
        buttons.add(down);
        listArea.add(buttons, BorderLayout.SOUTH);
        sidebar.add(listArea, BorderLayout.CENTER);
        settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
        settings.setBorder(BorderFactory.createTitledBorder("Einstellungen"));
        settings.setPreferredSize(new Dimension(290, 240));
        sidebar.add(settings, BorderLayout.SOUTH);

        JSplitPane images = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                imageArea("Originalbild", originalPanel), imageArea("Bearbeitetes Bild", resultPanel));
        images.setResizeWeight(0.5);
        JSplitPane layout = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, images);
        layout.setDividerLocation(320);
        add(layout, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        add(status, BorderLayout.SOUTH);

        add.addActionListener(event -> {
            model.addElement(new FilterStep((FilterType) types.getSelectedItem()));
            filterList.setSelectedIndex(model.size() - 1);
            scheduleUpdate();
        });
        remove.addActionListener(event -> {
            int index = filterList.getSelectedIndex();
            if (index < 0) return;
            model.remove(index);
            if (!model.isEmpty()) filterList.setSelectedIndex(Math.min(index, model.size() - 1));
            showSettings();
            scheduleUpdate();
        });
        up.addActionListener(event -> moveFilter(-1));
        down.addActionListener(event -> moveFilter(1));
        filterList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) showSettings();
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent event) {
                updateTimer.stop();
                if (processing != null) processing.cancel(true);
                if (loading != null) loading.cancel(true);
            }
        });
        showSettings();
    }

    private JPanel imageArea(String title, ImagePanel panel) {
        JPanel area = new JPanel(new BorderLayout());
        area.setBorder(BorderFactory.createTitledBorder(title));
        area.add(panel);
        area.setMinimumSize(new Dimension(150, 150));
        return area;
    }

    private void moveFilter(int direction) {
        int index = filterList.getSelectedIndex();
        int target = index + direction;
        if (index < 0 || target < 0 || target >= model.size()) return;
        FilterStep step = model.remove(index);
        model.add(target, step);
        filterList.setSelectedIndex(target);
        scheduleUpdate();
    }

    private void showSettings() {
        settings.removeAll();
        int index = filterList.getSelectedIndex();
        remove.setEnabled(index >= 0);
        up.setEnabled(index > 0);
        down.setEnabled(index >= 0 && index < model.size() - 1);
        FilterStep step = filterList.getSelectedValue();
        if (step == null) settings.add(new JLabel("Einen Filter auswählen."));
        else switch (step.getType()) {
            case INVERT -> settings.add(new JLabel("Invert benötigt keine Einstellungen."));
            case GAUSSIAN_BLUR -> slider("Radius (0 = aus)", 0, 10, step.getAmount(), step::setAmount);
            case THRESHOLD -> slider("Schwellwert", 0, 255, step.getAmount(), step::setAmount);
            case EDGE_DETECTION -> slider("Kantenstärke (%)", 0, 300, step.getAmount(), step::setAmount);
            case SHARPENING -> slider("Schärfe (%)", 0, 300, step.getAmount(), step::setAmount);
            case BRIGHTNESS_CONTRAST -> {
                slider("Helligkeit", -255, 255, step.getAmount(), step::setAmount);
                slider("Kontrast (%)", 0, 300, step.getContrast(), step::setContrast);
            }
        }
        settings.revalidate();
        settings.repaint();
    }

    private void slider(String text, int min, int max, int value, IntConsumer change) {
        JLabel label = new JLabel(text + ": " + value);
        JSlider slider = new JSlider(min, max, value);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        slider.addChangeListener(event -> {
            change.accept(slider.getValue());
            label.setText(text + ": " + slider.getValue());
            scheduleUpdate();
        });
        settings.add(label);
        settings.add(slider);
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("JPG- oder PNG-Bild öffnen");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Bilder (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        open.setEnabled(false);
        status.setText("Bild wird geladen …");
        loading = new SwingWorker<>() {
            @Override protected BufferedImage doInBackground() throws Exception { return ImageFiles.read(file); }
            @Override protected void done() {
                if (isCancelled()) return;
                open.setEnabled(true);
                try {
                    original = get();
                    originalPanel.setImage(original);
                    resultPanel.setImage(original);
                    setTitle("Image Filter – " + file.getName());
                    scheduleUpdate();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException exception) {
                    showError("Bild konnte nicht geöffnet werden", exception.getCause());
                }
            }
        };
        loading.execute();
    }

    private void scheduleUpdate() {
        revision++;
        if (processing != null) processing.cancel(true);
        if (original != null) {
            status.setText("Bild wird verarbeitet …");
            updateTimer.restart();
        }
    }

    private void processImage() {
        if (original == null) return;
        // Kopien verhindern, dass Regleränderungen eine laufende Berechnung beeinflussen.
        List<FilterStep> snapshot = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) snapshot.add(model.get(i).copy());
        BufferedImage source = original;
        long currentRevision = revision;
        processing = new SwingWorker<>() {
            @Override protected BufferedImage doInBackground() { return ImageFilters.applyAll(source, snapshot); }
            @Override protected void done() {
                if (isCancelled() || currentRevision != revision) return;
                try {
                    resultPanel.setImage(get());
                    status.setText(source.getWidth() + " × " + source.getHeight() + " Pixel | " + snapshot.size() + " Filter | Fertig");
                } catch (CancellationException ignored) {
                    // Eine neuere Einstellung wird bereits berechnet.
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException exception) {
                    showError("Filter konnte nicht angewendet werden", exception.getCause());
                }
            }
        };
        processing.execute();
    }

    private void showError(String title, Throwable cause) {
        status.setText(title + ".");
        String detail = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
        JOptionPane.showMessageDialog(this, detail, title, JOptionPane.ERROR_MESSAGE);
    }
}
