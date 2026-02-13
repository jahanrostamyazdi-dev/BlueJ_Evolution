import java.util.List;
import java.util.Random;

public class Diabloceratops extends Herbivore
{
    private static final int BREEDING_AGE = 8;
    private static final int MAX_AGE = 60;
    private static final double BREEDING_PROBABILITY = 0.10;
    private static final int MAX_LITTER_SIZE = 2;

    private static final int MAX_ENERGY = 24;
    private static final int BREEDING_ENERGY_THRESHOLD = 14;
    private static final int ENERGY_COST_PER_BABY = 3;

    private static final Random rand = Randomizer.getRandom();

    private int age;

    public Diabloceratops(boolean randomAge, Location location)
    {
        super(location, MAX_ENERGY);
        age = 0;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
        restoreToFullEnergy();
    }

    public void act(Field currentField, Field nextFieldState)
    {
        incrementAge();
        if(isAlive()) {
            List<Location> freeLocations =
                nextFieldState.getFreeAdjacentLocations(getLocation());

            if(!freeLocations.isEmpty()) {
                giveBirth(currentField, nextFieldState, freeLocations);
            }

            if(!freeLocations.isEmpty()) {
                Location nextLocation = freeLocations.get(0);
                setLocation(nextLocation);
                nextFieldState.placeDinosaur(this, nextLocation);
            } else {
                setDead();
            }
        }
    }

    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    private void giveBirth(Field currentField, Field nextFieldState, List<Location> freeLocations)
    {
        int births = breed(currentField);
        if(births > 0) {
            consumeEnergy(births * ENERGY_COST_PER_BABY);

            for(int b = 0; b < births && !freeLocations.isEmpty() && isAlive(); b++) {
                Location loc = freeLocations.remove(0);
                Diabloceratops young = new Diabloceratops(false, loc);
                nextFieldState.placeDinosaur(young, loc);
            }
        }
    }

    private int breed(Field currentField)
    {
        if(!isFemale()) return 0;
        if(!canBreed()) return 0;
        if(getEnergy() < BREEDING_ENERGY_THRESHOLD) return 0;
        if(!hasAdjacentMaleOfSameSpecies(currentField)) return 0;

        if(rand.nextDouble() <= BREEDING_PROBABILITY) {
            return rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return 0;
    }

    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}