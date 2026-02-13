import java.awt.*;
import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

public class SimulatorView extends JFrame
{
    private static final Color GRID_BORDER_DAY = Color.lightGray;

    private static final Color NIGHT_EMPTY_COLOR = new Color(20, 20, 20);
    private static final Color NIGHT_GRID_BORDER = new Color(40, 40, 40);
    private static final Color NIGHT_TEXT_COLOR = new Color(230, 230, 230);

    private final JLabel stepLabel;
    private final FieldView fieldView;

    private final JPanel legendPanel;

    private final Map<Class<?>, Color> colors;
    private final FieldStats stats;

    private final Map<Class<?>, Integer> stepCounts = new HashMap<>();

    public SimulatorView(int height, int width)
    {
        stats = new FieldStats();
        colors = new LinkedHashMap<>();

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
        if(col == null) return Color.gray;
        return col;
    }

    public void showStatus(int step, Field field)
    {
        showStatus(step, field, TimeOfDay.DAY);
    }

    public void showStatus(int step, Field field, TimeOfDay timeOfDay)
    {
        if(!isVisible()) setVisible(true);

        boolean night = (timeOfDay == TimeOfDay.NIGHT);

        setTitle("Dinosaur Ecosystem Simulation (" + timeOfDay + ")");
        stepLabel.setForeground(night ? NIGHT_TEXT_COLOR : Color.black);
        stepLabel.setText("Step: " + step + " | " + timeOfDay);

        stats.reset();
        stepCounts.clear();

        fieldView.preparePaint(night);

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Location loc = new Location(row, col);
                Dinosaur dinosaur = field.getDinosaurAt(loc);

                if(dinosaur != null) {
                    stats.incrementCount(dinosaur.getClass());
                    stepCounts.put(dinosaur.getClass(), stepCounts.getOrDefault(dinosaur.getClass(), 0) + 1);
                    fieldView.drawMark(col, row, getColorForDinosaur(dinosaur));
                }
                else {
                    int veg = field.getVegetationAt(loc);
                    fieldView.drawMark(col, row, getVegetationColor(veg, night));
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

    // Vegetation key uses same colouring as grid.
    private Color getVegetationColor(int veg, boolean night)
    {
        float t = Math.max(0f, Math.min(1f, veg / 100f));

        // brown -> green
        float hue = 0.10f + (0.33f - 0.10f) * t;
        float sat = 0.10f + 0.75f * t;
        float bri = 0.10f + 0.80f * t;

        if(night) bri *= 0.60f;

        return Color.getHSBColor(hue, sat, clamp01(bri));
    }

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

        carnRow.add(createLegendItemWithCount("Allosaurus", Allosaurus.class, night));
        carnRow.add(createLegendItemWithCount("Carnotaurus", Carnotaurus.class, night));
        carnRow.add(createLegendItemWithCount("Dilophosaurus", Dilophosaurus.class, night));

        JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
        sep1.setForeground(night ? NIGHT_GRID_BORDER : GRID_BORDER_DAY);
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        herbRow.add(createLegendItemWithCount("Iguanadon", Iguanadon.class, night));
        herbRow.add(createLegendItemWithCount("Diabloceratops", Diabloceratops.class, night));
        herbRow.add(createLegendItemWithCount("Ankylosaurus", Ankylosaurus.class, night));

        // Vegetation scale row (new)
        JPanel vegRow = createVegetationLegendRow(night);

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

    private JPanel createVegetationLegendRow(boolean night)
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
                if(xScale < 1) xScale = GRID_VIEW_SCALING_FACTOR;

                yScale = size.height / gridHeight;
                if(yScale < 1) yScale = GRID_VIEW_SCALING_FACTOR;
            }

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