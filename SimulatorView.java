import java.awt.*;
import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

/*
 * UI window that draws the field as a grid of colours.
 * Also shows a legend with counts and infected counts.
 * Night mode darkens everything and infected dinos get a purple-ish tint.
 */
public class SimulatorView extends JFrame
{
    private static final Color GRID_BORDER_DAY = Color.lightGray;

    private static final Color NIGHT_EMPTY_COLOR = new Color(20, 20, 20);
    private static final Color NIGHT_GRID_BORDER = new Color(40, 40, 40);
    private static final Color NIGHT_TEXT_COLOR = new Color(230, 230, 230);

    private static final Color INFECTION_TINT = new Color(160, 60, 200);
    private static final float INFECTION_BLEND = 0.45f;

    private final JLabel stepLabel;
    private final FieldView fieldView;
    private final JPanel legendPanel;

    private final Map<Class<?>, Color> colors;
    private final FieldStats stats;

    private final Map<Class<?>, Integer> stepCounts = new HashMap<>();
    private final Map<Class<?>, Integer> infectedCounts = new HashMap<>();

    // Makes the window and sets up the grid + legend layout
    public SimulatorView(int height, int width)
    {
        stats = new FieldStats();
        colors = new LinkedHashMap<>();

        // base colours (I just picked ones that look different)
        setColor(Iguanadon.class, Color.orange);
        setColor(Allosaurus.class, Color.blue);
        setColor(Carnotaurus.class, Color.red);
        setColor(Dilophosaurus.class, Color.cyan);
        setColor(Diabloceratops.class, Color.gray);
        setColor(Ankylosaurus.class, Color.pink);

        setTitle("Dinosaur Ecosystem Simulation");
        stepLabel = new JLabel("Step: 0", JLabel.CENTER);

        setLocation(100, 50);

        fieldView = new FieldView(height, width);

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

    // Sets base colour for a species
    public void setColor(Class<?> dinosaurClass, Color color)
    {
        colors.put(dinosaurClass, color);
    }

    // Gets base colour (fallback to grey if missing)
    private Color getColor(Class<?> dinosaurClass)
    {
        Color col = colors.get(dinosaurClass);
        if(col == null) return Color.gray;
        return col;
    }

    // Simple overload (defaults)
    public void showStatus(int step, Field field)
    {
        showStatus(step, field, TimeOfDay.DAY, WeatherState.CLEAR);
    }

    // Updates the title/labels and redraws the grid
    public void showStatus(int step, Field field, TimeOfDay timeOfDay, WeatherState weather)
    {
        if(!isVisible()) setVisible(true);

        boolean night = (timeOfDay == TimeOfDay.NIGHT);

        setTitle("Dinosaur Ecosystem Simulation (" + timeOfDay + ", " + weather + ")");
        stepLabel.setForeground(night ? NIGHT_TEXT_COLOR : Color.black);
        stepLabel.setText("Step: " + step + " | " + timeOfDay + " | Weather: " + weather);

        stats.reset();
        stepCounts.clear();
        infectedCounts.clear();

        fieldView.preparePaint(night);

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Location loc = new Location(row, col);
                Dinosaur d = field.getDinosaurAt(loc);

                if(d != null) {
                    stats.incrementCount(d.getClass());

                    stepCounts.put(d.getClass(), stepCounts.getOrDefault(d.getClass(), 0) + 1);

                    if(d.isInfected()) {
                        infectedCounts.put(d.getClass(), infectedCounts.getOrDefault(d.getClass(), 0) + 1);
                    }

                    fieldView.drawMark(col, row, getColorForDino(d));
                }
                else {
                    int veg = field.getVegetationAt(loc);
                    fieldView.drawMark(col, row, getVegetationColor(veg, night));
                }
            }
        }

        stats.countFinished();
        updateLegend(night);

        fieldView.repaint();
    }

    // Final colour for a dino = base colour + sex brightness + infection tint
    private Color getColorForDino(Dinosaur dino)
    {
        Color base = getColor(dino.getClass());

        float factor = dino.isFemale() ? 1.30f : 0.80f;
        Color sexCol = adjustBrightness(base, factor);

        if(dino.isInfected()) {
            return blend(sexCol, INFECTION_TINT, INFECTION_BLEND);
        }
        return sexCol;
    }

    // Turns veg amount (0..100) into a colour (night is darker)
    private Color getVegetationColor(int veg, boolean night)
    {
        float t = Math.max(0f, Math.min(1f, veg / 100f));

        float hue = 0.10f + (0.33f - 0.10f) * t;
        float sat = 0.10f + 0.75f * t;
        float bri = 0.10f + 0.80f * t;

        if(night) bri *= 0.60f;

        return Color.getHSBColor(hue, sat, clamp01(bri));
    }

    // Rebuilds the legend panel (counts change every frame)
    private void updateLegend(boolean night)
    {
        legendPanel.removeAll();
        legendPanel.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);

        JPanel carnRow = new JPanel(new GridLayout(1, 3, 12, 0));
        JPanel herbRow = new JPanel(new GridLayout(1, 3, 12, 0));

        carnRow.setOpaque(true);
        herbRow.setOpaque(true);
        carnRow.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);
        herbRow.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);

        carnRow.add(makeLegendItem("Allosaurus", Allosaurus.class, night));
        carnRow.add(makeLegendItem("Carnotaurus", Carnotaurus.class, night));
        carnRow.add(makeLegendItem("Dilophosaurus", Dilophosaurus.class, night));

        JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
        sep1.setForeground(night ? NIGHT_GRID_BORDER : GRID_BORDER_DAY);
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        herbRow.add(makeLegendItem("Iguanadon", Iguanadon.class, night));
        herbRow.add(makeLegendItem("Diabloceratops", Diabloceratops.class, night));
        herbRow.add(makeLegendItem("Ankylosaurus", Ankylosaurus.class, night));

        JPanel vegRow = makeVegetationLegendRow(night);

        legendPanel.add(wrapWithPadding(carnRow, 4, 6, 4, 6, night));
        legendPanel.add(sep1);
        legendPanel.add(wrapWithPadding(herbRow, 4, 6, 4, 6, night));

        JSeparator sep2 = new JSeparator(SwingConstants.HORIZONTAL);
        sep2.setForeground(night ? NIGHT_GRID_BORDER : GRID_BORDER_DAY);
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        legendPanel.add(sep2);
        legendPanel.add(wrapWithPadding(vegRow, 4, 6, 6, 6, night));

        legendPanel.revalidate();
        legendPanel.repaint();
    }

    // Builds the vegetation key row
    private JPanel makeVegetationLegendRow(boolean night)
    {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row.setOpaque(true);
        row.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);

        JLabel label = new JLabel("Vegetation:");
        label.setForeground(night ? NIGHT_TEXT_COLOR : Color.black);
        row.add(label);

        addVegKey(row, 0, night);
        addVegKey(row, 25, night);
        addVegKey(row, 50, night);
        addVegKey(row, 75, night);
        addVegKey(row, 100, night);

        return row;
    }

    // Adds a coloured square + number for one veg level
    private void addVegKey(JPanel row, int value, boolean night)
    {
        Color col = getVegetationColor(value, night);

        JLabel swatch = new JLabel("■");
        swatch.setForeground(col);
        swatch.setFont(swatch.getFont().deriveFont(Font.BOLD, 14f));

        JLabel text = new JLabel(String.valueOf(value));
        text.setForeground(night ? NIGHT_TEXT_COLOR : Color.black);

        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setOpaque(true);
        item.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);
        item.add(swatch);
        item.add(text);

        row.add(item);
    }

    // Creates one legend entry with male/female colour and counts
    private JPanel makeLegendItem(String name, Class<?> speciesClass, boolean night)
    {
        Color base = getColor(speciesClass);
        Color maleColor = adjustBrightness(base, 0.80f);
        Color femaleColor = adjustBrightness(base, 1.30f);

        int count = stepCounts.getOrDefault(speciesClass, 0);
        int inf = infectedCounts.getOrDefault(speciesClass, 0);

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

        JLabel speciesText = new JLabel(" " + name + ": " + count + " (Inf: " + inf + ")");
        speciesText.setForeground(night ? adjustBrightness(base, 1.20f) : base);

        item.add(maleSwatch);
        item.add(maleText);
        item.add(femaleSwatch);
        item.add(femaleText);
        item.add(speciesText);

        return item;
    }

    // Wraps rows so spacing looks less cramped
    private JPanel wrapWithPadding(JPanel row, int top, int left, int bottom, int right, boolean night)
    {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(true);
        wrapper.setBackground(night ? NIGHT_EMPTY_COLOR : Color.white);
        wrapper.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        wrapper.add(row, BorderLayout.CENTER);
        return wrapper;
    }

    // Brightness helper (HSB makes it easy)
    private Color adjustBrightness(Color color, float factor)
    {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float newB = clamp01(hsb[2] * factor);
        return Color.getHSBColor(hsb[0], hsb[1], newB);
    }

    // clamp 0..1
    private float clamp01(float v)
    {
        return Math.max(0f, Math.min(1f, v));
    }

    // Basic linear blend
    private Color blend(Color a, Color b, float t)
    {
        t = clamp01(t);
        int r = (int)(a.getRed()   * (1 - t) + b.getRed()   * t);
        int g = (int)(a.getGreen() * (1 - t) + b.getGreen() * t);
        int bl = (int)(a.getBlue()  * (1 - t) + b.getBlue()  * t);
        return new Color(r, g, bl);
    }

    /*
     * Panel that actually draws the grid onto an image buffer.
     * (Buffering makes repaint faster, otherwise Swing is kinda slow.)
     */
    private class FieldView extends JPanel
    {
        private final int GRID_VIEW_SCALING_FACTOR = 6;

        private final int gridWidth, gridHeight;
        private int xScale, yScale;
        private Dimension size;
        private Graphics g;
        private Image fieldImage;

        // Stores the grid size in cells
        public FieldView(int height, int width)
        {
            gridHeight = height;
            gridWidth = width;
            size = new Dimension(0, 0);
        }

        // Swing asks this when laying out the frame
        public Dimension getPreferredSize()
        {
            return new Dimension(gridWidth * GRID_VIEW_SCALING_FACTOR,
                    gridHeight * GRID_VIEW_SCALING_FACTOR);
        }

        // Prepares the buffer + background
        public void preparePaint(boolean night)
        {
            if(!size.equals(getSize())) {
                size = getSize();
                fieldImage = fieldView.createImage(size.width, size.height);
                g = fieldImage.getGraphics();

                xScale = size.width / gridWidth;
                if(xScale < 1) xScale = GRID_VIEW_SCALING_FACTOR;

                yScale = size.height / gridHeight;
                if(yScale < 1) yScale = GRID_VIEW_SCALING_FACTOR;
            }

            g.setColor(night ? NIGHT_GRID_BORDER : GRID_BORDER_DAY);
            g.fillRect(0, 0, size.width, size.height);
        }

        // Draws one cell
        public void drawMark(int x, int y, Color color)
        {
            g.setColor(color);
            g.fillRect(x * xScale, y * yScale, xScale - 1, yScale - 1);
        }

        // Standard paint hook
        public void paintComponent(Graphics g)
        {
            if(fieldImage != null) {
                Dimension currentSize = getSize();
                if(size.equals(currentSize)) {
                    g.drawImage(fieldImage, 0, 0, null);
                } else {
                    g.drawImage(fieldImage, 0, 0, currentSize.width, currentSize.height, null);
                }
            }
        }
    }
}