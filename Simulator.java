import java.util.*;

/*
 * Runs the whole dinosaur simulation.
 * Keeps the current Field, steps time/weather/disease, and asks each dino to act.
 * Also has the continuous run stuff because the tuning window needed it.
 */
public class Simulator
{
    private static final int DEFAULT_WIDTH = 120;
    private static final int DEFAULT_DEPTH = 80;

    private Field field;
    private int step;
    private final SimulatorView view;

    // continuous run stuff for the tuning window
    private volatile boolean running = false;
    private Thread runnerThread;

    // Makes a simulator with default size
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }

    // Makes a simulator with a custom field size
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
        openTuningWindow();
    }

    // Opens the tuning window (it manages itself)
    public void openTuningWindow()
    {
        new TuningWindow(this);
    }

    // Runs the sim in a background thread so the UI stays responsive
    public void startContinuous()
    {
        if(running) return;
        running = true;

        runnerThread = new Thread(() -> {
            while(running && field.isViable()) {
                simulateOneStep();
                delay(Tuning.simDelayMs);
            }
            running = false;
        });

        runnerThread.setDaemon(true);
        runnerThread.start();
    }

    // Stops the continuous run loop
    public void stopContinuous()
    {
        running = false;
    }

    // Quick "long run" helper
    public void runLongSimulation()
    {
        simulate(700);
    }

    // Runs N steps or until one side dies out
    public void simulate(int numSteps)
    {
        reportStats();
        for(int n = 1; n <= numSteps && field.isViable(); n++) {
            simulateOneStep();
            delay(50);
        }
    }

    // Does one step (time/weather/disease -> act -> regrow -> show)
    public void simulateOneStep()
    {
        step++;

        TimeManager.updateForStep(step);
        WeatherManager.updateOneStep();

        Field nextField = new Field(field.getDepth(), field.getWidth());
        nextField.copyVegetationFrom(field);

        // System.out.println("[step] " + step + " time=" + TimeManager.getTimeOfDay() + " weather=" + WeatherManager.getWeather());

        List<Dinosaur> dinos = field.getDinosaurs();

        // disease BEFORE acting so infected dinos lose energy + can't breed this step
        for(Dinosaur d : dinos) {
            if(d != null && d.isAlive()) {
                DiseaseManager.maybeStartNewOutbreak(field);
                d.tickDisease(field);
            }
        }

        for(Dinosaur d : dinos) {
            if(d != null && d.isAlive()) {
                d.act(field, nextField);
            }
        }

        nextField.regrowVegetation(TimeManager.getTimeOfDay(), WeatherManager.getWeather());

        field = nextField;

        reportStats();
        view.showStatus(step, field, TimeManager.getTimeOfDay(), WeatherManager.getWeather());
    }

    // Infects a random dino (mostly used to test the disease feature)
    public void infectRandomDinosaur()
    {
        List<Dinosaur> dinos = field.getDinosaurs();
        Collections.shuffle(dinos, Randomizer.getRandom());

        for(Dinosaur d : dinos) {
            if(d != null && d.canBeInfected()) {
                d.infect(DiseaseManager.randomInfectionDuration());
                // System.out.println("[test] infected " + d.getClass().getSimpleName());
                break;
            }
        }
    }

    // Reset back to step 0 and repopulate
    public void reset()
    {
        step = 0;
        TimeManager.reset();
        WeatherManager.reset();
        populate();
        view.showStatus(step, field, TimeManager.getTimeOfDay(), WeatherManager.getWeather());
    }

    // Places dinos randomly using the spawn probabilities
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Location loc = new Location(row, col);

                double roll = rand.nextDouble();

                if(roll <= Tuning.pAllosaurus) {
                    field.placeDinosaur(new Allosaurus(true, loc), loc);
                }
                else if(roll <= Tuning.pAllosaurus + Tuning.pCarnotaurus) {
                    field.placeDinosaur(new Carnotaurus(true, loc), loc);
                }
                else if(roll <= Tuning.pAllosaurus + Tuning.pCarnotaurus + Tuning.pDilophosaurus) {
                    field.placeDinosaur(new Dilophosaurus(true, loc), loc);
                }
                else {
                    double herbRoll = rand.nextDouble();

                    if(herbRoll <= Tuning.pIguanadon) {
                        field.placeDinosaur(new Iguanadon(true, loc), loc);
                    }
                    else if(herbRoll <= Tuning.pIguanadon + Tuning.pDiabloceratops) {
                        field.placeDinosaur(new Diabloceratops(true, loc), loc);
                    }
                    else if(herbRoll <= Tuning.pIguanadon + Tuning.pDiabloceratops + Tuning.pAnkylosaurus) {
                        field.placeDinosaur(new Ankylosaurus(true, loc), loc);
                    }
                }
            }
        }
    }

    // Prints counts to the console
    public void reportStats()
    {
        field.fieldStats();
    }

    // Small sleep helper (keeps UI usable)
    private void delay(int milliseconds)
    {
        try { Thread.sleep(milliseconds); }
        catch(InterruptedException e) { }
    }
}