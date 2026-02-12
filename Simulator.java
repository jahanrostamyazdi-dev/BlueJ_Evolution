import java.util.*;

/**
 * A simple predator-prey simulator, based on a rectangular field containing 
 * iguanadons and allosaurs.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 7.1
 */
public class Simulator
{
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 120;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 80;
    
    //Carnivore creation probabilities
    private static final double ALLOSAURUS_CREATION_PROBABILITY = 0.010;
    private static final double CARNOTAURUS_CREATION_PROBABILITY = 0.008;
    private static final double DILOPHOSAURUS_CREATION_PROBABILITY = 0.010;
    //Herbivores creation probabilities
    private static final double IGUANADON_CREATION_PROBABILITY = 0.060;
    private static final double DIABLOCERATOPS_CREATION_PROBABILITY = 0.020;
    private static final double ANKYLOSAURUS_CREATION_PROBABILITY = 0.015;

    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private final SimulatorView view;

    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be >= zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }
        
        field = new Field(depth, width);
        view = new SimulatorView(depth, width);

        reset();
    }
    
    /**
     * Run the simulation from its current state for a reasonably long 
     * period (4000 steps).
     */
    public void runLongSimulation()
    {
        simulate(700);
    }
    
    /**
     * Run the simulation for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps)
    {
        reportStats();
        for(int n = 1; n <= numSteps && field.isViable(); n++) {
            simulateOneStep();
            delay(50);         // adjust this to change execution speed
        }
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each allosaurus and iguanadon.
     */
    public void simulateOneStep()
    {
        step++;
        // Use a separate Field to store the starting state of
        // the next step.
        Field nextFieldState = new Field(field.getDepth(), field.getWidth());

        List<Dinosaur> dinosaurs = field.getDinosaurs();
        for (Dinosaur anDinosaur : dinosaurs) {
            anDinosaur.act(field, nextFieldState);
        }
        
        // Replace the old state with the new one.
        field = nextFieldState;

        reportStats();
        view.showStatus(step, field);
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        populate();
        view.showStatus(step, field);
    }
    
    /**
     * Randomly populate the field with dinosaurs.
     */
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
    
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                double prob = rand.nextDouble();
                Location location = new Location(row, col);
    
                // Predators (smaller total probability)
                if(prob <= ALLOSAURUS_CREATION_PROBABILITY) {
                    field.placeDinosaur(new Allosaurus(true, location), location);
                }
                else if(prob <= ALLOSAURUS_CREATION_PROBABILITY + CARNOTAURUS_CREATION_PROBABILITY) {
                    field.placeDinosaur(new Carnotaurus(true, location), location);
                }
                else if(prob <= ALLOSAURUS_CREATION_PROBABILITY + CARNOTAURUS_CREATION_PROBABILITY + DILOPHOSAURUS_CREATION_PROBABILITY) {
                    field.placeDinosaur(new Dilophosaurus(true, location), location);
                }
    
                // Herbivores
                else if(prob <= ALLOSAURUS_CREATION_PROBABILITY + CARNOTAURUS_CREATION_PROBABILITY + DILOPHOSAURUS_CREATION_PROBABILITY
                              + IGUANADON_CREATION_PROBABILITY) {
                    field.placeDinosaur(new Iguanadon(true, location), location);
                }
                else if(prob <= ALLOSAURUS_CREATION_PROBABILITY + CARNOTAURUS_CREATION_PROBABILITY + DILOPHOSAURUS_CREATION_PROBABILITY
                              + IGUANADON_CREATION_PROBABILITY + DIABLOCERATOPS_CREATION_PROBABILITY) {
                    field.placeDinosaur(new Diabloceratops(true, location), location);
                }
                else if(prob <= ALLOSAURUS_CREATION_PROBABILITY + CARNOTAURUS_CREATION_PROBABILITY + DILOPHOSAURUS_CREATION_PROBABILITY
                              + IGUANADON_CREATION_PROBABILITY + DIABLOCERATOPS_CREATION_PROBABILITY + ANKYLOSAURUS_CREATION_PROBABILITY) {
                    field.placeDinosaur(new Ankylosaurus(true, location), location);
                }
                // else leave empty
            }
        }
    }

    /**
     * Report on the number of each type of dinosaur in the field.
     */
    public void reportStats()
    {
        //System.out.print("Step: " + step + " ");
        field.fieldStats();
    }
    
    /**
     * Pause for a given time.
     * @param milliseconds The time to pause for, in milliseconds
     */
    private void delay(int milliseconds)
    {
        try {
            Thread.sleep(milliseconds);
        }
        catch(InterruptedException e) {
            // ignore
        }
    }
}
