import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * A simple model of a dilophosaurus.
 * Dilophosaurs age, move, eat iguanadons, and die.
 */
public class Dilophosaurus extends Carnivore
{
    private static final int BREEDING_AGE = 10;
    private static final int MAX_AGE = 90;
    private static final double BREEDING_PROBABILITY = 0.09;
    private static final int MAX_LITTER_SIZE = 3;

    private static final int FOOD_VALUE = 7;

    private static final Random rand = Randomizer.getRandom();

    private int age;

    public Dilophosaurus(boolean randomAge, Location location)
    {
        super(location, FOOD_VALUE);

        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            setEnergy(rand.nextInt(getMaxEnergy()) + 1);
        } else {
            age = 0;
            restoreToFullEnergy();
        }
    }

    public void act(Field currentField, Field nextFieldState)
    {
        incrementAge();
        consumeEnergy(1);

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
                    restoreToFullEnergy();
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