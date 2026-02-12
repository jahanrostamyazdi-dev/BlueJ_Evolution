import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * A simple model of a carnotaurus.
 * Carnotauruses age, move, eat herbivores, and die.
 */
public class Carnotaurus extends Carnivore
{
    // Shared characteristics (class variables).
    private static final int BREEDING_AGE = 12;
    private static final int MAX_AGE = 120;
    private static final double BREEDING_PROBABILITY = 0.07;
    private static final int MAX_LITTER_SIZE = 2;

    // Food value of a single herbivore meal.
    private static final int HERBIVORE_FOOD_VALUE = 8;

    private static final Random rand = Randomizer.getRandom();

    // Individual characteristics.
    private int age;
    private int foodLevel;

    public Carnotaurus(boolean randomAge, Location location)
    {
        super(location);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        } else {
            age = 0;
        }
        foodLevel = rand.nextInt(HERBIVORE_FOOD_VALUE);
    }

    public void act(Field currentField, Field nextFieldState)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            List<Location> freeLocations =
                    nextFieldState.getFreeAdjacentLocations(getLocation());
            if(!freeLocations.isEmpty()) {
                giveBirth(nextFieldState, freeLocations);
            }

            Location nextLocation = findFood(currentField);
            if(nextLocation == null && !freeLocations.isEmpty()) {
                nextLocation = freeLocations.remove(0);
            }

            if(nextLocation != null) {
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

    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }

    /**
     * Look for herbivores adjacent to the current location.
     * Only the first live herbivore is eaten.
     */
    private Location findFood(Field field)
    {
        List<Location> adjacent = field.getAdjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        Location foodLocation = null;

        while(foodLocation == null && it.hasNext()) {
            Location loc = it.next();
            Dinosaur dinosaur = field.getDinosaurAt(loc);

            // For now: eat any herbivore.
            if(dinosaur instanceof Herbivore herbivore) {
                if(herbivore.isAlive()) {
                    herbivore.setDead();
                    foodLevel = HERBIVORE_FOOD_VALUE;
                    foodLocation = loc;
                }
            }
        }
        return foodLocation;
    }

    private void giveBirth(Field nextFieldState, List<Location> freeLocations)
    {
        int births = breed();
        if(births > 0) {
            for(int b = 0; b < births && !freeLocations.isEmpty(); b++) {
                Location loc = freeLocations.remove(0);
                Carnotaurus young = new Carnotaurus(false, loc);
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