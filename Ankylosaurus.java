import java.util.List;
import java.util.Random;

public class Ankylosaurus extends Herbivore
{
    private static final int BREEDING_AGE = 12;
    private static final int MAX_AGE = 80;
    private static final double BREEDING_PROBABILITY = 0.07;
    private static final int MAX_LITTER_SIZE = 1;

    private static final int MAX_ENERGY = 28;
    private static final int BREEDING_ENERGY_THRESHOLD = 12;
    private static final int ENERGY_COST_PER_BABY = 2;

    private static final int BITE_SIZE = 28;
    private static final int ENERGY_PER_VEG = 6;

    private static final Random rand = Randomizer.getRandom();

    private int age;

    public Ankylosaurus(boolean randomAge, Location location)
    {
        super(location, MAX_ENERGY);
        age = 0;
        if(randomAge) age = rand.nextInt(MAX_AGE);
        restoreToFullEnergy();
    }

    @Override
    public int getDefence() { return 12; }

    public void act(Field currentField, Field nextFieldState)
    {
        incrementAge();
        if(!isAlive()) return;

        consumeEnergy(1);
        if(!isAlive()) return;

        eat(nextFieldState);
        
        List<Location> free = nextFieldState.getFreeAdjacentLocations(getLocation());
        if(!free.isEmpty()) {
            giveBirth(currentField, nextFieldState, free);
        }

        Location nextLocation = chooseBestVegetationMove(currentField, free);
        if(nextLocation != null) {
            setLocation(nextLocation);
            nextFieldState.placeDinosaur(this, nextLocation);
        } else {
            setDead();
            return;
        }
    }

    private void eat(Field nextFieldState)
    {
        Location loc = getLocation();
        int eaten = nextFieldState.consumeVegetationAt(loc, BITE_SIZE);
        int gained = eaten / ENERGY_PER_VEG;
        if(gained > 0) gainEnergy(gained);
    }

    private Location chooseBestVegetationMove(Field currentField, List<Location> freeLocations)
    {
        if(freeLocations == null || freeLocations.isEmpty()) return null;

        Location best = freeLocations.get(0);
        int bestVeg = currentField.getVegetationAt(best);

        for(Location loc : freeLocations) {
            int veg = currentField.getVegetationAt(loc);
            if(veg > bestVeg) {
                bestVeg = veg;
                best = loc;
            }
        }
        return best;
    }

    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) setDead();
    }

    private void giveBirth(Field currentField, Field nextFieldState, List<Location> freeLocations)
    {
        int births = breed(currentField);
        if(births > 0) {
            consumeEnergy(births * ENERGY_COST_PER_BABY);

            for(int b = 0; b < births && !freeLocations.isEmpty() && isAlive(); b++) {
                Location loc = freeLocations.remove(0);
                Ankylosaurus young = new Ankylosaurus(false, loc);
                nextFieldState.placeDinosaur(young, loc);
            }
        }
    }

    private int breed(Field currentField)
    {
        if(!isFemale()) return 0;
        if(age < BREEDING_AGE) return 0;
        if(getEnergy() < BREEDING_ENERGY_THRESHOLD) return 0;
        if(!hasAdjacentMaleOfSameSpecies(currentField)) return 0;

        if(rand.nextDouble() <= BREEDING_PROBABILITY) {
            return 1;
        }
        return 0;
    }
}