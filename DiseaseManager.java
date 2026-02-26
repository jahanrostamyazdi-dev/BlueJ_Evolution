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

    private DiseaseManager() {}

    // Randomly infects something sometimes (called from Simulator loop)
    public static void maybeStartNewOutbreak(Field field)
    {
        if(rand.nextDouble() > Tuning.spontaneousOutbreakChance) return;

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
        return Tuning.infectionMinDuration + rand.nextInt(Tuning.infectionMaxDuration - Tuning.infectionMinDuration + 1);
    }

    // Extra energy drain for infected dinos
    public static int getExtraEnergyDrainWhileInfected()
    {
        return Tuning.extraInfectedEnergyLoss;
    }

    // Energy needed to survive when the infection ends
    public static int getSurviveEnergyThreshold()
    {
        return Tuning.surviveEnergyThreshold;
    }

    // Immunity time after surviving infection
    public static int getImmunityDuration()
    {
        return Tuning.immunityDuration;
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

            if(other.canBeInfected() && rand.nextDouble() < Tuning.adjacentSpreadChance) {
                other.infect(randomInfectionDuration());
            }
        }
    }

    // Predators can catch it from prey (high chance)
    public static void onPredatorAteInfectedPrey(Carnivore predator)
    {
        if(predator == null || !predator.isAlive()) return;
        if(!predator.canBeInfected()) return;

        if(rand.nextDouble() < Tuning.predatorEatInfectedChance) {
            predator.infect(randomInfectionDuration());
        }
    }
}