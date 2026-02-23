import java.util.List;
import java.util.Random;

/*
 * Base herbivore logic (eat vegetation, move towards veg, breeding helper).
 * Concrete herbivores implement defence + species type + createYoung.
 */
public abstract class Herbivore extends Dinosaur
{
    private static final Random rand = Randomizer.getRandom();

    protected Herbivore(Location location, int maxEnergy)
    {
        super(location, maxEnergy);
    }

    // Herbivores have defence value used in carnivore kill chance
    public abstract int getDefence();

    // Used to pull tuning values
    public abstract SpeciesType getSpeciesType();

    // Eat vegetation at current position (uses tuning bite size etc)
    protected void eat(Field nextFieldState)
    {
        SpeciesTuning t = Tuning.get(getSpeciesType());

        int taken = nextFieldState.consumeVegetationAt(getLocation(), t.biteSize);
        int gained = taken / Math.max(1, t.energyPerVeg);
        gainEnergy(gained);
    }

    // Chooses the adjacent free tile with most vegetation
    protected Location chooseBestVegetationMove(Field currentField, List<Location> free)
    {
        if(free == null || free.isEmpty()) return null;

        Location best = free.get(0);
        int bestVeg = currentField.getVegetationAt(best);

        for(Location loc : free) {
            int v = currentField.getVegetationAt(loc);
            if(v > bestVeg) {
                bestVeg = v;
                best = loc;
            }
        }

        return best;
    }

    // Common breeding logic for herbivores (similar to carnivores)
    protected int breed(Field currentField)
    {
        if(!canBreedThisStep()) return 0;

        SpeciesTuning t = Tuning.get(getSpeciesType());

        if(getEnergy() < t.breedingEnergyThreshold) return 0;
        if(getAge() < t.breedingAge) return 0;

        if(t.minVegToBreed > 0) {
            if(currentField.getVegetationAt(getLocation()) < t.minVegToBreed) return 0;
        }

        if(!isFemale()) return 0;
        if(!hasAdjacentMaleOfSameSpecies(currentField)) return 0;

        if(rand.nextDouble() > t.breedingProbability) return 0;

        int births = rand.nextInt(Math.max(1, t.maxLitterSize)) + 1;

        int cost = births * Math.max(0, t.energyCostPerBaby);
        if(cost > 0) consumeEnergy(cost);

        if(!isAlive()) return 0;
        return births;
    }

    // Spawns newborns into free spaces
    protected void giveBirth(Field currentField, Field nextFieldState, List<Location> free)
    {
        int births = breed(currentField);

        for(int b = 0; b < births && !free.isEmpty(); b++) {
            Location loc = free.remove(0);
            Dinosaur young = createYoung(loc);
            nextFieldState.placeDinosaur(young, loc);
        }
    }

    // Newborn factory
    protected abstract Dinosaur createYoung(Location loc);
}