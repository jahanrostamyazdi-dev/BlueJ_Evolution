import java.util.List;
import java.util.Random;

/**
 * A simple model of an iguanadon.
 * Iguanadons age, move, breed, and die.
 */
public class Iguanadon extends Herbivore
{
    private static final int BREEDING_AGE = 5;
    private static final int MAX_AGE = 40;
    private static final double BREEDING_PROBABILITY = 0.12;
    private static final int MAX_LITTER_SIZE = 4;

    // Herbivore max energy for now (used later for breeding thresholds, disease, plants).
    private static final int MAX_ENERGY = 20;

    private static final Random rand = Randomizer.getRandom();

    private int age;

    public Iguanadon(boolean randomAge, Location location)
    {
        super(location, MAX_ENERGY);
        age = 0;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
        // For now herbivores start full energy and do not spend it yet (plants come later).
        restoreToFullEnergy();
    }

    public void act(Field currentField, Field nextFieldState)
    {
        incrementAge();
        if(isAlive()) {
            List<Location> freeLocations =
                nextFieldState.getFreeAdjacentLocations(getLocation());
            if(!freeLocations.isEmpty()) {
                giveBirth(nextFieldState, freeLocations);
            }
            if(!freeLocations.isEmpty()) {
                Location nextLocation = freeLocations.get(0);
                setLocation(nextLocation);
                nextFieldState.placeDinosaur(this, nextLocation);
            }
            else {
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

    private void giveBirth(Field nextFieldState, List<Location> freeLocations)
    {
        int births = breed();
        if(births > 0) {
            for(int b = 0; b < births && !freeLocations.isEmpty(); b++) {
                Location loc = freeLocations.remove(0);
                Iguanadon young = new Iguanadon(false, loc);
                nextFieldState.placeDinosaur(young, loc);
            }
        }
    }

    private int breed()
    {
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            return rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return 0;
    }

    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}