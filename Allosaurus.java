import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * A simple model of an allosaurus.
 * Allosaurs age, move, eat herbivores, and die.
 */
public class Allosaurus extends Carnivore
{
    // Characteristics shared by all allosaurs (class variables).
    private static final int BREEDING_AGE = 15;
    private static final int MAX_AGE = 150;
    private static final double BREEDING_PROBABILITY = 0.08;
    private static final int MAX_LITTER_SIZE = 2;

    // Energy gained from a successful meal (also acts as max energy for now).
    private static final int IGUANADON_FOOD_VALUE = 9;

    // Chance to successfully kill Ankylosaurus (armoured prey).
    private static final double ANKYLO_KILL_CHANCE = 0.20;

    private static final Random rand = Randomizer.getRandom();

    // Individual characteristics.
    private int age;

    public Allosaurus(boolean randomAge, Location location)
    {
        super(location, IGUANADON_FOOD_VALUE);

        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            // Random starting energy so predators don't all start identical.
            setEnergy(rand.nextInt(getMaxEnergy()) + 1); // 1..maxEnergy
        }
        else {
            age = 0;
            restoreToFullEnergy();
        }
    }

    public void act(Field currentField, Field nextFieldState)
    {
        incrementAge();
        consumeEnergy(1); // hunger / energy drain per step

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

    /**
     * Look for prey adjacent to the current location.
     */
    private Location findFood(Field field)
    {
        List<Location> adjacent = field.getAdjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        Location foodLocation = null;

        while(foodLocation == null && it.hasNext()) {
            Location loc = it.next();
            Dinosaur dinosaur = field.getDinosaurAt(loc);

            if(dinosaur instanceof Iguanadon iguanadon) {
                if(iguanadon.isAlive()) {
                    iguanadon.setDead();
                    restoreToFullEnergy();
                    foodLocation = loc;
                }
            }
            else if(dinosaur instanceof Diabloceratops diabloceratops) {
                if(diabloceratops.isAlive()) {
                    diabloceratops.setDead();
                    restoreToFullEnergy();
                    foodLocation = loc;
                }
            }
            else if(dinosaur instanceof Ankylosaurus ankylosaurus) {
                if(ankylosaurus.isAlive()) {
                    if(rand.nextDouble() <= ANKYLO_KILL_CHANCE) {
                        ankylosaurus.setDead();
                        restoreToFullEnergy();
                        foodLocation = loc;
                    }
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
                Allosaurus young = new Allosaurus(false, loc);
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