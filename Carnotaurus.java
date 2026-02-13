import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Carnotaurus extends Carnivore
{
    private static final int BREEDING_AGE = 12;
    private static final int MAX_AGE = 120;
    private static final double BREEDING_PROBABILITY = 0.07;
    private static final int MAX_LITTER_SIZE = 2;

    private static final int FOOD_VALUE = 16;
    private static final int BREEDING_ENERGY_THRESHOLD = 5;
    private static final int ENERGY_COST_PER_BABY = 2;

    private static final Random rand = Randomizer.getRandom();

    private int age;

    public Carnotaurus(boolean randomAge, Location location)
    {
        super(location, FOOD_VALUE);

        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            int minStart = (int)(getMaxEnergy() * 0.60);
            setEnergy(minStart + rand.nextInt(getMaxEnergy() - minStart + 1));
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
                giveBirth(currentField, nextFieldState, freeLocations);
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

            if(dinosaur instanceof Iguanadon iguanadon && iguanadon.isAlive()) {
                iguanadon.setDead();
                restoreToFullEnergy();
                foodLocation = loc;
            }
        }
        return foodLocation;
    }

    private void giveBirth(Field currentField, Field nextFieldState, List<Location> freeLocations)
    {
        int births = breed(currentField);
        if(births > 0) {
            consumeEnergy(births * ENERGY_COST_PER_BABY);

            for(int b = 0; b < births && !freeLocations.isEmpty() && isAlive(); b++) {
                Location loc = freeLocations.remove(0);
                Carnotaurus young = new Carnotaurus(false, loc);
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