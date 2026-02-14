import java.util.List;
import java.util.Random;

/**
 * Central disease rules:
 * - Spread to adjacent dinosaurs with a chance each step
 * - Predators that eat infected prey can become infected with a high chance
 * - Infection lasts for a timer; end outcome handled in Dinosaur.tickDisease(...)
 */
public class DiseaseManager
{
    private static final Random rand = Randomizer.getRandom();

    // --- Tuning knobs (adjust later if needed) ---
    private static final int INFECTION_MIN_DURATION = 35;
    private static final int INFECTION_MAX_DURATION = 70;

    private static final double ADJACENT_SPREAD_CHANCE = 0.08;  // per neighbour per step
    private static final double PREDATOR_EAT_INFECTED_CHANCE = 0.70;

    // Extra energy drain while infected (in addition to normal step drain)
    private static final int EXTRA_ENERGY_DRAIN_WHILE_INFECTED = 1;

    // End-of-infection survival threshold: must have at least this energy to survive
    private static final int SURVIVE_ENERGY_THRESHOLD = 8;

    // Optional immunity after surviving infection
    private static final int IMMUNITY_DURATION = 50;
    
    //Chance of spontaneous infection
    private static final double SPONTANEOUS_INFECTION_CHANCE_PER_STEP = 0.002; // 0.2% chance per step total

    private DiseaseManager() {}

    public static void maybeStartNewOutbreak(Field field)
    {
        if(rand.nextDouble() > SPONTANEOUS_INFECTION_CHANCE_PER_STEP) return;
    
        List<Dinosaur> dinos = field.getDinosaurs();
        if(dinos.isEmpty()) return;
    
        Dinosaur d = dinos.get(rand.nextInt(dinos.size()));
        if(d != null && d.canBeInfected()) {
            d.infect(randomInfectionDuration());
        }
    }
    
    public static int randomInfectionDuration()
    {
        return INFECTION_MIN_DURATION + rand.nextInt(INFECTION_MAX_DURATION - INFECTION_MIN_DURATION + 1);
    }

    public static int getExtraEnergyDrainWhileInfected()
    {
        return EXTRA_ENERGY_DRAIN_WHILE_INFECTED;
    }

    public static int getSurviveEnergyThreshold()
    {
        return SURVIVE_ENERGY_THRESHOLD;
    }

    public static int getImmunityDuration()
    {
        return IMMUNITY_DURATION;
    }

    /**
     * If an infected dinosaur is adjacent to others, each neighbour has a chance to become infected.
     * Call this once per step for each infected dinosaur (order dependence is acceptable in this sim).
     */
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

    /**
     * Called when a predator successfully eats an infected prey.
     */
    public static void onPredatorAteInfectedPrey(Carnivore predator)
    {
        if(predator == null || !predator.isAlive()) return;
        if(!predator.canBeInfected()) return;

        if(rand.nextDouble() < PREDATOR_EAT_INFECTED_CHANCE) {
            predator.infect(randomInfectionDuration());
        }
    }
}