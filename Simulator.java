import java.util.*;

public class Simulator
{
    private static final int DEFAULT_WIDTH = 120;
    private static final int DEFAULT_DEPTH = 80;

    private static final double ALLOSAURUS_CREATION_PROBABILITY = 0.010;
    private static final double CARNOTAURUS_CREATION_PROBABILITY = 0.008;
    private static final double DILOPHOSAURUS_CREATION_PROBABILITY = 0.008;

    private static final double IGUANADON_CREATION_PROBABILITY = 0.060;
    private static final double DIABLOCERATOPS_CREATION_PROBABILITY = 0.025;
    private static final double ANKYLOSAURUS_CREATION_PROBABILITY = 0.020;

    private Field field;
    private int step;
    private final SimulatorView view;
    
    
    //For tuning stuff
    private volatile boolean running = false;
    private Thread runnerThread;    
    
    //TUNING METHODS
        public void openTuningWindow()
    {
        new TuningWindow(this);
    }
    
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
    
    public void stopContinuous()
    {
        running = false;
    }
    //END OF TUNING METHODS

    
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
        openTuningWindow();
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
        WeatherManager.updateOneStep();

        Field nextFieldState = new Field(field.getDepth(), field.getWidth());
        nextFieldState.copyVegetationFrom(field);

        // IMPORTANT: tick disease BEFORE acting so:
        // - infected loses extra energy
        // - breeding can be disabled
        // - infection can spread based on current positions
        List<Dinosaur> dinosaurs = field.getDinosaurs();
        
        
        
        for(Dinosaur d : dinosaurs) {
            if(d != null && d.isAlive()) {
                DiseaseManager.maybeStartNewOutbreak(field);
                d.tickDisease(field);
            }
        }

        for(Dinosaur d : dinosaurs) {
            if(d != null && d.isAlive()) {
                d.act(field, nextFieldState);
            }
        }

        nextFieldState.regrowVegetation(TimeManager.getTimeOfDay(), WeatherManager.getWeather());

        field = nextFieldState;

        reportStats();
        view.showStatus(step, field, TimeManager.getTimeOfDay(), WeatherManager.getWeather());
    }

    public void infectRandomDinosaur()
    {
        List<Dinosaur> dinos = field.getDinosaurs();
        Collections.shuffle(dinos, Randomizer.getRandom());
    
        for(Dinosaur d : dinos) {
            if(d != null && d.canBeInfected()) {
                d.infect(DiseaseManager.randomInfectionDuration());
                break;
            }
        }
    }
    
    public void reset()
    {
        step = 0;
        TimeManager.reset();
        WeatherManager.reset();
        populate();
        view.showStatus(step, field, TimeManager.getTimeOfDay(), WeatherManager.getWeather());
    }

    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Location location = new Location(row, col);

                double roll = rand.nextDouble();

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
        try { Thread.sleep(milliseconds); }
        catch(InterruptedException e) { }
    }
}