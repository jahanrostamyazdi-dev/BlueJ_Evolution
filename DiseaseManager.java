import java.util.List;
import java.util.Random;

/*
 * Disease rules for the sim.
 * Dinosaurs keep their own infection/immunity timers, this class just does the shared rules.
 * (I kept values here because it was easier than threading them through everything.)
 */
public class DiseaseManager
{
    private static final Random rand = Randomizer.getRandom();

    private static final int INFECTION_MIN_DURATION = 35;
    private static final int INFECTION_MAX_DURATION = 70;

    private static final double ADJACENT_SPREAD_CHANCE = 0.08;
    private static final double PREDATOR_EAT_INFECTED_CHANCE = 0.70;

    private static final int EXTRA_ENERGY_DRAIN_WHILE_INFECTED = 1;
    private static final int SURVIVE_ENERGY_THRESHOLD = 8;

    private static final int IMMUNITY_DURATION = 50;

    private static final double SPONTANEOUS_INFECTION_CHANCE_PER_STEP = 0.002;

    private DiseaseManager() {}

    // Randomly infects something sometimes (called from Simulator loop)
    public static void maybeStartNewOutbreak(Field field)
    {
        if(rand.nextDouble() > SPONTANEOUS_INFECTION_CHANCE_PER_STEP) return;

        List<Dinosaur> dinos = field.getDinosaurs();
        if(dinos.isEmpty()) return;

        Dinosaur d = dinos.get(rand.nextInt(dinos.size()));
        if(d != null && d.canBeInfected()) {
            d.infect(randomInfectionDuration());
            // System.out.println("[disease] outbreak on " + d.getClass().getSimpleName());
        }
    }

    // Random duration between min/max
    public static int randomInfectionDuration()
    {
        return INFECTION_MIN_DURATION + rand.nextInt(INFECTION_MAX_DURATION - INFECTION_MIN_DURATION + 1);
    }

    // Extra energy drain for infected dinos
    public static int getExtraEnergyDrainWhileInfected()
    {
        return EXTRA_ENERGY_DRAIN_WHILE_INFECTED;
    }

    // Energy needed to survive when the infection ends
    public static int getSurviveEnergyThreshold()
    {
        return SURVIVE_ENERGY_THRESHOLD;
    }

    // Immunity time after surviving infection
    public static int getImmunityDuration()
    {
        return IMMUNITY_DURATION;
    }

    // Spread check to adjacent neighbours
    public static void attemptAdjacentSpread(Dinosaur source, Field currentField)
    {
        if(source == null || !source.isAlive()) return;
        if(!source.isInfected()) return;

        List<Location> adjacent = currentField.getAdjacentLocations(source.getLocation());
        for(Location loc : adjacent) {
            Dinosaur other = currentField.getDinosaurAt(loc);
            if(other == null || !other.isAlive()) continue;

            if(other.canBeInfected() && rand.nextDouble() < ADJACENT_SPREAD_CHANCE) {
                other.infect(randomInfectionDuration());
            }
        }
    }

    // Predators can catch it from prey (high chance)
    public static void onPredatorAteInfectedPrey(Carnivore predator)
    {
        if(predator == null || !predator.isAlive()) return;
        if(!predator.canBeInfected()) return;

        if(rand.nextDouble() < PREDATOR_EAT_INFECTED_CHANCE) {
            predator.infect(randomInfectionDuration());
        }
    }
}