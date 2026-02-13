import java.awt.*;
import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

/**
 * A graphical view of the simulation grid.
 * The view displays a colored rectangle for each location representing its contents.
 * It supports:
 *  - Species colors
 *  - Sex-based brightness (♂ darker, ♀ brighter)
 *  - Day/Night "dark mode" background
 *  - A bottom legend that shows species colors + male/female swatches + live counts
 *
 * @author David J. Barnes and Michael Kölling
 * @version 7.1 (modified)
 */
public class SimulatorView extends JFrame
{
    // Day mode colors
    private static final Color EMPTY_COLOR = Color.white;
    private static final Color GRID_BORDER_DAY = Color.lightGray;

    // Night mode colors
    private static final Color NIGHT_EMPTY_COLOR = new Color(20, 20, 20);
    private static final Color NIGHT_GRID_BORDER = new Color(40, 40, 40);
    private static final Color NIGHT_TEXT_COLOR = new Color(230, 230, 230);

    private final JLabel stepLabel;
    private final FieldView fieldView;

    // Bottom legend panel (species swatches + counts)
    private final JPanel legendPanel;

    // A map for storing base colors for species
    private final Map<Class<?>, Color> colors;
    // A statistics object computing and storing simulation information
    private final FieldStats stats;

    // Per-step counts (so we can show counts next to legend items)
    private final Map<Class<?>, Integer> stepCounts = new HashMap<>();

    /**
     * Create a view of the given width and height.
     * @param height The simulation's height.
     * @param width  The simulation's width.
     */
    public SimulatorView(int height, int width)
    {
        stats = new FieldStats();
        colors = new LinkedHashMap<>();

        // Base species colors (sex shading is applied at draw-time)
        setColor(Iguanadon.class, Color.orange);
        setColor(Allosaurus.class, Color.blue);
        setColor(Carnotaurus.class, Color.red);
        setColor(Dilophosaurus.class, Color.magenta);
        setColor(Diabloceratops.class, Color.green);
        setColor(Ankylosaurus.class, Color.black);

        setTitle("Dinosaur Ecosystem Simulation");
        stepLabel = new JLabel("Step: 0", JLabel.CENTER);

        setLocation(100, 50);

        fieldView = new FieldView(height, width);

        legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        legendPanel.setOpaque(true);

        Container contents = getContentPane();
        contents.add(stepLabel, BorderLayout.NORTH);
        contents.add(fieldView, BorderLayout.CENTER);
        contents.add(legendPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    /**
     * Define a base color to be used for a given class of dinosaur.
     * @param dinosaurClass The dinosaur's Class object.
     * @param color The base color to be used for the given class.
     */
    public void setColor(Class<?> dinosaurClass, Color color)
    {
        colors.put(dinosaurClass, color);
    }

    /**
     * @return The base color to be used for a given class of dinosaur.
     */
    private Color getColor(Class<?> dinosaurClass)
    {
        Color col = colors.get(dinosaurClass);
        if(col == null) {
            return Color.gray;
        }
        return col;
    }

    /**
     * Backwards-compatible showStatus (defaults to DAY if not provided).
     */
    public void showStatus(int step, Field field)
    {
        showStatus(step, field, TimeOfDay.DAY);
    }

    /**
     * Show the current status of the field, including time-of-day rendering.
     * @param step Which iteration step it is.
     * @param field The field whose status is to be displayed.
     * @param timeOfDay Current time-of-day (DAY/NIGHT)
     */
    public void showStatus(int step, Field field, TimeOfDay timeOfDay)
    {
        if(!isVisible()) {
            setVisible(true);
        }

        boolean night = (timeOfDay == TimeOfDay.NIGHT);

        setTitle("Dinosaur Ecosystem Simulation (" + timeOfDay + ")");
        stepLabel.setForeground(night ? NIGHT_TEXT_COLOR : Color.black);
        stepLabel.setText("Step: " + step + " | " + timeOfDay);

        stats.reset();
        stepCounts.clear();

        fieldView.preparePaint(night);

        Color empty = night ? NIGHT_EMPTY_COLOR : EMPTY_COLOR;

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Dinosaur dinosaur = field.getDinosaurAt(new Location(row, col));
                if(dinosaur != null) {
                    stats.incrementCount(dinosaur.getClass());
                    stepCounts.put(dinosaur.getClass(),
                                   stepCounts.getOrDefault(dinosaur.getClass(), 0) + 1);

                    fieldView.drawMark(col, row, getColorForDinosaur(dinosaur));
                }
                else {
                    fieldView.drawMark(col, row, empty);
                }
            }
        }

        stats.countFinished();
        updateLegendWithCounts(night);

        fieldView.repaint();
    }

    /**
     * Get the display color for a dinosaur:
     * - base hue is species color
     * - males darker, females brighter
     */
    private Color getColorForDinosaur(Dinosaur dinosaur)
    {
        Color base = getColor(dinosaur.getClass());

        // Sex shading factors
        float factor = dinosaur.isFemale() ? 1.30f : 0.80f;
        return adjustBrightness(base, factor);
    }

    /**
     * Rebuild the bottom legend each step to show current counts.
     * Each species shows: ♂ swatch + ♀ swatch + "Name: count" in base species color.
     */
    private void updateLegendWithCounts(boolean night)
    {
        legendPanel.removeAll();
        legendPanel.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);

        addLegendItemWithCount(legendPanel, "Iguanadon", Iguanadon.class, night);
        addLegendItemWithCount(legendPanel, "Allosaurus", Allosaurus.class, night);
        addLegendItemWithCount(legendPanel, "Carnotaurus", Carnotaurus.class, night);
        addLegendItemWithCount(legendPanel, "Dilophosaurus", Dilophosaurus.class, night);
        addLegendItemWithCount(legendPanel, "Diabloceratops", Diabloceratops.class, night);
        addLegendItemWithCount(legendPanel, "Ankylosaurus", Ankylosaurus.class, night);

        legendPanel.revalidate();
        legendPanel.repaint();
    }

    private void addLegendItemWithCount(JPanel panel, String name, Class<?> speciesClass, boolean night)
    {
        Color base = getColor(speciesClass);

        // Match the same factors used in getColorForDinosaur(...)
        Color maleColor = adjustBrightness(base, 0.80f);
        Color femaleColor = adjustBrightness(base, 1.30f);

        int count = stepCounts.getOrDefault(speciesClass, 0);

        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setOpaque(true);
        item.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);

        JLabel maleSwatch = new JLabel("■");
        maleSwatch.setForeground(maleColor);
        maleSwatch.setFont(maleSwatch.getFont().deriveFont(Font.BOLD, 14f));

        JLabel maleText = new JLabel("♂");
        maleText.setForeground(night ? NIGHT_TEXT_COLOR : Color.black);

        JLabel femaleSwatch = new JLabel("■");
        femaleSwatch.setForeground(femaleColor);
        femaleSwatch.setFont(femaleSwatch.getFont().deriveFont(Font.BOLD, 14f));

        JLabel femaleText = new JLabel("♀");
        femaleText.setForeground(night ? NIGHT_TEXT_COLOR : Color.black);

        JLabel speciesText = new JLabel(" " + name + ": " + count);
        speciesText.setForeground(night ? adjustBrightness(base, 1.20f) : base);

        item.add(maleSwatch);
        item.add(maleText);
        item.add(femaleSwatch);
        item.add(femaleText);
        item.add(speciesText);

        panel.add(item);
    }

    /**
     * Adjust brightness while keeping the same hue (HSB).
     * factor > 1.0 brightens, factor < 1.0 darkens.
     */
    private Color adjustBrightness(Color color, float factor)
    {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float newB = clamp01(hsb[2] * factor);
        return Color.getHSBColor(hsb[0], hsb[1], newB);
    }

    private float clamp01(float v)
    {
        return Math.max(0f, Math.min(1f, v));
    }

    /**
     * Provide a graphical view of a rectangular field.
     * Custom Swing component that renders the grid.
     */
    private class FieldView extends JPanel
    {
        private final int GRID_VIEW_SCALING_FACTOR = 6;

        private final int gridWidth, gridHeight;
        private int xScale, yScale;
        private Dimension size;
        private Graphics g;
        private Image fieldImage;

        public FieldView(int height, int width)
        {
            gridHeight = height;
            gridWidth = width;
            size = new Dimension(0, 0);
        }

        public Dimension getPreferredSize()
        {
            return new Dimension(gridWidth * GRID_VIEW_SCALING_FACTOR,
                                 gridHeight * GRID_VIEW_SCALING_FACTOR);
        }

        public void preparePaint(boolean night)
        {
            if(!size.equals(getSize())) {
                size = getSize();
                fieldImage = fieldView.createImage(size.width, size.height);
                g = fieldImage.getGraphics();

                xScale = size.width / gridWidth;
                if(xScale < 1) {
                    xScale = GRID_VIEW_SCALING_FACTOR;
                }
                yScale = size.height / gridHeight;
                if(yScale < 1) {
                    yScale = GRID_VIEW_SCALING_FACTOR;
                }
            }

            // Fill background with "grid line" colour so the 1px gaps show as grid.
            g.setColor(night ? NIGHT_GRID_BORDER : GRID_BORDER_DAY);
            g.fillRect(0, 0, size.width, size.height);
        }

        public void drawMark(int x, int y, Color color)
        {
            g.setColor(color);
            // xScale-1/yScale-1 leaves a 1px border for grid lines.
            g.fillRect(x * xScale, y * yScale, xScale - 1, yScale - 1);
        }

        public void paintComponent(Graphics g)
        {
            if(fieldImage != null) {
                Dimension currentSize = getSize();
                if(size.equals(currentSize)) {
                    g.drawImage(fieldImage, 0, 0, null);
                }
                else {
                    g.drawImage(fieldImage, 0, 0, currentSize.width, currentSize.height, null);
                }
            }
        }
    }
}