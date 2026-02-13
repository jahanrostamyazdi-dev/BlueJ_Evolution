import java.util.*;

/**
 * Dinosaur ecosystem simulator.
 */
public class Simulator
{
    private static final int DEFAULT_WIDTH = 120;
    private static final int DEFAULT_DEPTH = 80;

    // You likely already changed these for 6 species elsewhere.
    // Keep your current probabilities if you already tuned them.
    private static final double ALLOSAURUS_CREATION_PROBABILITY = 0.012;
    private static final double CARNOTAURUS_CREATION_PROBABILITY = 0.010;
    private static final double DILOPHOSAURUS_CREATION_PROBABILITY = 0.010;

    private static final double IGUANADON_CREATION_PROBABILITY = 0.070;
    private static final double DIABLOCERATOPS_CREATION_PROBABILITY = 0.025;
    private static final double ANKYLOSAURUS_CREATION_PROBABILITY = 0.015;

    private Field field;
    private int step;
    private final SimulatorView view;

    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }

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

    public void runLongSimulation()
    {
        simulate(700);
    }

    public void simulate(int numSteps)
    {
        reportStats();
        for(int n = 1; n <= numSteps && field.isViable(); n++) {
            simulateOneStep();
            delay(50);
        }
    }

    public void simulateOneStep()
    {
        step++;

        TimeManager.updateForStep(step);

        // Create next state and COPY vegetation forward
        Field nextFieldState = new Field(field.getDepth(), field.getWidth());
        nextFieldState.copyVegetationFrom(field);

        List<Dinosaur> dinosaurs = field.getDinosaurs();
        for (Dinosaur anDinosaur : dinosaurs) {
            anDinosaur.act(field, nextFieldState);
        }

        // Regrow vegetation AFTER animals have eaten this step
        nextFieldState.regrowVegetation(TimeManager.getTimeOfDay());

        field = nextFieldState;

        reportStats();
        view.showStatus(step, field, TimeManager.getTimeOfDay());
    }

    public void reset()
    {
        step = 0;
        TimeManager.reset();
        populate();
        view.showStatus(step, field, TimeManager.getTimeOfDay());
    }

    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Location location = new Location(row, col);

                double roll = rand.nextDouble();

                // predators first
                if(roll <= ALLOSAURUS_CREATION_PROBABILITY) {
                    field.placeDinosaur(new Allosaurus(true, location), location);
                }
                else if(roll <= ALLOSAURUS_CREATION_PROBABILITY + CARNOTAURUS_CREATION_PROBABILITY) {
                    field.placeDinosaur(new Carnotaurus(true, location), location);
                }
                else if(roll <= ALLOSAURUS_CREATION_PROBABILITY + CARNOTAURUS_CREATION_PROBABILITY + DILOPHOSAURUS_CREATION_PROBABILITY) {
                    field.placeDinosaur(new Dilophosaurus(true, location), location);
                }
                else {
                    // herbivores
                    double herbRoll = rand.nextDouble();
                    if(herbRoll <= IGUANADON_CREATION_PROBABILITY) {
                        field.placeDinosaur(new Iguanadon(true, location), location);
                    }
                    else if(herbRoll <= IGUANADON_CREATION_PROBABILITY + DIABLOCERATOPS_CREATION_PROBABILITY) {
                        field.placeDinosaur(new Diabloceratops(true, location), location);
                    }
                    else if(herbRoll <= IGUANADON_CREATION_PROBABILITY + DIABLOCERATOPS_CREATION_PROBABILITY + ANKYLOSAURUS_CREATION_PROBABILITY) {
                        field.placeDinosaur(new Ankylosaurus(true, location), location);
                    }
                }
            }
        }
    }

    public void reportStats()
    {
        field.fieldStats();
    }

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