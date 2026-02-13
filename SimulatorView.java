import java.awt.*;
import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A graphical view of the simulation grid.
 * The view displays a colored rectangle for each location 
 * representing its contents. It uses a default background color.
 * Colors for each type of species can be defined using the
 * setColor method.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 7.0
 */
public class SimulatorView extends JFrame
{
    // Colors used for empty locations.
    private static final Color EMPTY_COLOR = Color.white;

    // Color used for objects that have no defined color.
    private static final Color UNKNOWN_COLOR = Color.gray;

    private final String STEP_PREFIX = "Step: ";
    private final String POPULATION_PREFIX = "Population: ";
    private final JLabel stepLabel;
    private final JLabel population;
    private final FieldView fieldView;
    
    //For the legend to show the colours and species
    private final JLabel legendLabel;
    private final JPanel legendPanel;
    
    // A map for storing colors for participants in the simulation
    private final Map<Class<?>, Color> colors;
    // A statistics object computing and storing simulation information
    private final FieldStats stats;

    /**
     * Create a view of the given width and height.
     * @param height The simulation's height.
     * @param width  The simulation's width.
     */
    public SimulatorView(int height, int width)
    {
        stats = new FieldStats();
        colors = new LinkedHashMap<>();
        
        //Carni colours
        setColor(Allosaurus.class, Color.blue);
        setColor(Carnotaurus.class, Color.red);
        setColor(Dilophosaurus.class, Color.magenta);

        //Herbi colours
        setColor(Diabloceratops.class, Color.yellow);
        setColor(Ankylosaurus.class, Color.pink);
        setColor(Iguanadon.class, Color.orange);
        
        setTitle("Allosaurus and Iguanadon Simulation");
        stepLabel = new JLabel(STEP_PREFIX, JLabel.CENTER);
        population = new JLabel(POPULATION_PREFIX, JLabel.CENTER);
        
        setLocation(100, 50);
        
        fieldView = new FieldView(height, width);

        Container contents = getContentPane();
        contents.add(stepLabel, BorderLayout.NORTH);
        contents.add(fieldView, BorderLayout.CENTER);
        // Bottom area: population + legend
        legendLabel = new JLabel("Legend: ♀ brighter  |  ♂ darker", JLabel.CENTER);
        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.add(population);
        legendPanel.add(legendLabel);
        legendPanel.add(createSpeciesLegendPanel());
        contents.add(legendPanel, BorderLayout.SOUTH);
        //pack it all to the screen
        pack();
        setVisible(true);
    }
    
    /**
     * Define a color to be used for a given class of dinosaur.
     * @param dinosaurClass The dinosaur's Class object.
     * @param color The color to be used for the given class.
     */
    public void setColor(Class<?> dinosaurClass, Color color)
    {
        colors.put(dinosaurClass, color);
    }

    /**
     * @return The color to be used for a given class of dinosaur.
     */
    private Color getColor(Class<?> dinosaurClass)
    {
        Color col = colors.get(dinosaurClass);
        if(col == null) {
            // no color defined for this class
            return UNKNOWN_COLOR;
        }
        else {
            return col;
        }
    }

    /**
     * Builds a legend panel showing each species with male/female shade swatches.
     * Male = darker, Female = brighter (same hue).
     */
    private JPanel createSpeciesLegendPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 2));
    
        addLegendItem(panel, "Iguanadon", getColor(Iguanadon.class));
        addLegendItem(panel, "Allosaurus", getColor(Allosaurus.class));
        addLegendItem(panel, "Carnotaurus", getColor(Carnotaurus.class));
        addLegendItem(panel, "Dilophosaurus", getColor(Dilophosaurus.class));
        addLegendItem(panel, "Diabloceratops", getColor(Diabloceratops.class));
        addLegendItem(panel, "Ankylosaurus", getColor(Ankylosaurus.class));
    
        return panel;
    }
    
    /**
     * Adds one legend item: ♂ swatch (darker) + ♀ swatch (brighter) + species name.
     */
    private void addLegendItem(JPanel panel, String name, Color baseColor)
    {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    
        // Match the same factors used in getColorForDinosaur(...)
        Color maleColor = adjustBrightness(baseColor, 0.80f);
        Color femaleColor = adjustBrightness(baseColor, 1.30f);
    
        JLabel maleSwatch = new JLabel("■");
        maleSwatch.setForeground(maleColor);
        maleSwatch.setFont(maleSwatch.getFont().deriveFont(Font.BOLD, 14f));
    
        JLabel maleText = new JLabel("♂");
    
        JLabel femaleSwatch = new JLabel("■");
        femaleSwatch.setForeground(femaleColor);
        femaleSwatch.setFont(femaleSwatch.getFont().deriveFont(Font.BOLD, 14f));
    
        JLabel femaleText = new JLabel("♀");
    
        JLabel speciesText = new JLabel(" " + name);
    
        item.add(maleSwatch);
        item.add(maleText);
        item.add(femaleSwatch);
        item.add(femaleText);
        item.add(speciesText);
    
        panel.add(item);
    }
    
    /**
     * Show the current status of the field.
     * @param step Which iteration step it is.
     * @param field The field whose status is to be displayed.
     */
    public void showStatus(int step, Field field)
    {
        if(!isVisible()) {
            setVisible(true);
        }
            
        stepLabel.setText(STEP_PREFIX + step);
        stats.reset();
        
        fieldView.preparePaint();

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Dinosaur dinosaur = field.getDinosaurAt(new Location(row, col));
                if(dinosaur != null) {
                    stats.incrementCount(dinosaur.getClass());
                    fieldView.drawMark(col, row, getColorForDinosaur(dinosaur));
                }
                else {
                    fieldView.drawMark(col, row, EMPTY_COLOR);
                }
            }
        }
        stats.countFinished();

        population.setText(POPULATION_PREFIX + stats.getPopulationDetails(field));
        fieldView.repaint();
    }

    /**
     * Get the display color for a dinosaur, including sex-based brightness adjustment.
     * Females are drawn brighter, males darker, using the same base species color.
     */
    private Color getColorForDinosaur(Dinosaur dinosaur)
    {
        Color base = getColor(dinosaur.getClass());
        if(base == UNKNOWN_COLOR || base == EMPTY_COLOR) {
            return base;
        }
    
        // Adjust brightness by sex:
        // Female = brighter, Male = darker.
        float factor = dinosaur.isFemale() ? 1.30f : 0.80f;
        return adjustBrightness(base, factor);
    }

    /**
     * Adjust brightness while keeping the same hue (HSV/HSB space).
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
     * Determine whether the simulation should continue to run.
     * @return true If there is more than one species alive.
     */
    public boolean isViable(Field field)
    {
        return stats.isViable(field);
    }
    
    /**
     * Provide a graphical view of a rectangular field. This is 
     * a nested class (a class defined inside a class) which
     * defines a custom component for the user interface. This
     * component displays the field.
     * This is rather advanced GUI stuff - you can ignore this 
     * for your project if you like.
     */
    private class FieldView extends JPanel
    {
        private final int GRID_VIEW_SCALING_FACTOR = 6;

        private final int gridWidth, gridHeight;
        private int xScale, yScale;
        Dimension size;
        private Graphics g;
        private Image fieldImage;

        /**
         * Create a new FieldView component.
         */
        public FieldView(int height, int width)
        {
            gridHeight = height;
            gridWidth = width;
            size = new Dimension(0, 0);
        }

        /**
         * Tell the GUI manager how big we would like to be.
         */
        public Dimension getPreferredSize()
        {
            return new Dimension(gridWidth * GRID_VIEW_SCALING_FACTOR,
                                 gridHeight * GRID_VIEW_SCALING_FACTOR);
        }

        /**
         * Prepare for a new round of painting. Since the component
         * may be resized, compute the scaling factor again.
         */
        public void preparePaint()
        {
            if(! size.equals(getSize())) {  // if the size has changed...
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
        }
        
        /**
         * Paint on grid location on this field in a given color.
         */
        public void drawMark(int x, int y, Color color)
        {
            g.setColor(color);
            g.fillRect(x * xScale, y * yScale, xScale-1, yScale-1);
        }

        /**
         * The field view component needs to be redisplayed. Copy the
         * internal image to screen.
         */
        public void paintComponent(Graphics g)
        {
            if(fieldImage != null) {
                Dimension currentSize = getSize();
                if(size.equals(currentSize)) {
                    g.drawImage(fieldImage, 0, 0, null);
                }
                else {
                    // Rescale the previous image.
                    g.drawImage(fieldImage, 0, 0, currentSize.width, currentSize.height, null);
                }
            }
        }
    }
}
