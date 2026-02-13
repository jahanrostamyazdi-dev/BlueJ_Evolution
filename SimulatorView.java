import java.awt.*;
import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

/**
 * A graphical view of the simulation grid.
 * Displays:
 *  - species colors
 *  - sex-based brightness (♂ darker, ♀ brighter)
 *  - day/night "dark mode" background
 *  - bottom legend in 2 fixed rows:
 *      Top: 3 carnivores
 *      Divider
 *      Bottom: 3 herbivores
 *    Each legend item shows ♂/♀ swatches + live count.
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

    // Bottom legend panel container
    private final JPanel legendPanel;

    // Base colors per species
    private final Map<Class<?>, Color> colors;
    // Stats (still used to keep compatibility if you want)
    private final FieldStats stats;

    // Per-step counts for legend display
    private final Map<Class<?>, Integer> stepCounts = new HashMap<>();

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

        // Legend container: vertical stack (row1, divider, row2)
        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setOpaque(true);

        Container contents = getContentPane();
        contents.add(stepLabel, BorderLayout.NORTH);
        contents.add(fieldView, BorderLayout.CENTER);
        contents.add(legendPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    public void setColor(Class<?> dinosaurClass, Color color)
    {
        colors.put(dinosaurClass, color);
    }

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

    private Color getColorForDinosaur(Dinosaur dinosaur)
    {
        Color base = getColor(dinosaur.getClass());
        float factor = dinosaur.isFemale() ? 1.30f : 0.80f;
        return adjustBrightness(base, factor);
    }

    /**
     * Legend in 2 fixed rows (3 carnivores on top, 3 herbivores below) with a divider.
     */
    private void updateLegendWithCounts(boolean night)
    {
        legendPanel.removeAll();
        legendPanel.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);

        JPanel carnRow = new JPanel(new GridLayout(1, 3, 12, 0));
        JPanel herbRow = new JPanel(new GridLayout(1, 3, 12, 0));

        carnRow.setOpaque(true);
        herbRow.setOpaque(true);
        carnRow.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);
        herbRow.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);

        // Top row: Carnivores
        carnRow.add(createLegendItemWithCount("Allosaurus", Allosaurus.class, night));
        carnRow.add(createLegendItemWithCount("Carnotaurus", Carnotaurus.class, night));
        carnRow.add(createLegendItemWithCount("Dilophosaurus", Dilophosaurus.class, night));

        // Divider
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(night ? NIGHT_GRID_BORDER : GRID_BORDER_DAY);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Bottom row: Herbivores
        herbRow.add(createLegendItemWithCount("Iguanadon", Iguanadon.class, night));
        herbRow.add(createLegendItemWithCount("Diabloceratops", Diabloceratops.class, night));
        herbRow.add(createLegendItemWithCount("Ankylosaurus", Ankylosaurus.class, night));

        legendPanel.add(wrapWithPadding(carnRow, 4, 6, 4, 6, night));
        legendPanel.add(sep);
        legendPanel.add(wrapWithPadding(herbRow, 4, 6, 6, 6, night));

        legendPanel.revalidate();
        legendPanel.repaint();
    }

    private JPanel createLegendItemWithCount(String name, Class<?> speciesClass, boolean night)
    {
        Color base = getColor(speciesClass);

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

        return item;
    }

    private JPanel wrapWithPadding(JPanel row, int top, int left, int bottom, int right, boolean night)
    {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(true);
        wrapper.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);
        wrapper.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        wrapper.add(row, BorderLayout.CENTER);
        return wrapper;
    }

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