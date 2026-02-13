import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * A simple model of a dilophosaurus.
 * Dilophosaurs age, move, eat herbivores, and die.
 */
public class Dilophosaurus extends Carnivore
{
    private static final int BREEDING_AGE = 10;
    private static final int MAX_AGE = 90;
    private static final double BREEDING_PROBABILITY = 0.09;
    private static final int MAX_LITTER_SIZE = 3;

    private static final int HERBIVORE_FOOD_VALUE = 7;

    private static final Random rand = Randomizer.getRandom();

    private int age;
    private int foodLevel;

    public Dilophosaurus(boolean randomAge, Location location)
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

    private Location findFood(Field field)
    {
        List<Location> adjacent = field.getAdjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        Location foodLocation = null;
    
        while(foodLocation == null && it.hasNext()) {
            Location loc = it.next();
            Dinosaur dinosaur = field.getDinosaurAt(loc);
    
            // Dilophosaurus hunts Iguanadon only (for now).
            if(dinosaur instanceof Iguanadon iguanadon) {
                if(iguanadon.isAlive()) {
                    iguanadon.setDead();
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
                Dilophosaurus young = new Dilophosaurus(false, loc);
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