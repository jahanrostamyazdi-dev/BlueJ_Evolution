import java.util.List;
import java.util.Random;

public abstract class Herbivore extends Dinosaur
{
    private static final Random rand = Randomizer.getRandom();

    protected Herbivore(Location location, int maxEnergy)
    {
        super(location, maxEnergy);
    }

    public abstract int getDefence();
    public abstract SpeciesType getSpeciesType();

    /**
     * Eat vegetation at current tile using tuning parameters.
     */
    protected void eat(Field nextFieldState)
    {
        SpeciesTuning t = Tuning.get(getSpeciesType());

        int taken = nextFieldState.consumeVegetationAt(getLocation(), t.biteSize);
        int gained = taken / Math.max(1, t.energyPerVeg);
        gainEnergy(gained);
    }

    /**
     * Choose next move: prefer adjacent tile with highest vegetation (among free tiles).
     */
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

    /**
     * Breeding helper: number of births based on tuning, energy threshold,
     * female + adjacent male requirement, and infection blocking.
     */
    protected int breed(Field currentField)
    {
        if(!canBreedThisStep()) return 0;

        SpeciesTuning t = Tuning.get(getSpeciesType());

        if(getEnergy() < t.breedingEnergyThreshold) return 0;

        // Optional veg gating for breeding (carrying capacity)
        if(t.minVegToBreed > 0) {
            if(currentField.getVegetationAt(getLocation()) < t.minVegToBreed) return 0;
        }

        // Females only + need adjacent male of same species
        if(!isFemale()) return 0;
        if(!hasAdjacentMaleOfSameSpecies(currentField)) return 0;

        if(rand.nextDouble() > t.breedingProbability) return 0;

        int births = rand.nextInt(Math.max(1, t.maxLitterSize)) + 1;

        // Energy cost per baby
        int cost = births * Math.max(0, t.energyCostPerBaby);
        if(cost > 0) consumeEnergy(cost);

        // If cost killed us, births should be 0
        if(!isAlive()) return 0;
        return births;
    }

    protected void giveBirth(Field currentField, Field nextFieldState, List<Location> free)
    {
        int births = breed(currentField);
        for(int b = 0; b < births && !free.isEmpty(); b++) {
            Location loc = free.remove(0);
            Dinosaur young = createYoung(loc);
            nextFieldState.placeDinosaur(young, loc);
        }
    }

    /**
     * Factory for newborns.
     */
    protected abstract Dinosaur createYoung(Location loc);
}