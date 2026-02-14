import java.util.*;

/**
 * Dilophosaurus: nocturnal.
 * - Sleeps in DAY (no hunting, no energy drain, tries to stay in place)
 * - Hunts at NIGHT (adjacent hunt)
 * - Uses unified Attack/Defence combat model via tryKill(...)
 */
public class Dilophosaurus extends Carnivore
{
    private static final int BREEDING_AGE = 10;
    private static final int MAX_AGE = 90;
    private static final double BREEDING_PROBABILITY = 0.09;
    private static final int MAX_LITTER_SIZE = 3;

    private static final int FOOD_VALUE = 14;

    private static final int BREEDING_ENERGY_THRESHOLD = 6;
    private static final int ENERGY_COST_PER_BABY = 2;

    private static final double BASE_KILL_CHANCE = 0.90;

    private static final Random rand = Randomizer.getRandom();

    private int age;

    public Dilophosaurus(boolean randomAge, Location location)
    {
        super(location, FOOD_VALUE);

        if(randomAge) {
            age = rand.nextInt(MAX_AGE);

            int minStart = (int)(getMaxEnergy() * 0.60);
            setEnergy(minStart + rand.nextInt(getMaxEnergy() - minStart + 1));
        }
        else {
            age = 0;
            restoreToFullEnergy();
        }
    }

    @Override
    public int getAttack()
    {
        return 6;
    }

    public void act(Field currentField, Field nextFieldState)
    {
        incrementAge();

        if(!isAlive()) {
            return;
        }

        // DAY: sleep (no hunting, no energy drain)
        if(TimeManager.isDay()) {
            List<Location> freeLocations =
                nextFieldState.getFreeAdjacentLocations(getLocation());

            if(!freeLocations.isEmpty()) {
                giveBirth(currentField, nextFieldState, freeLocations);
            }

            Location here = getLocation();
            if(here != null && nextFieldState.getDinosaurAt(here) == null) {
                nextFieldState.placeDinosaur(this, here);
                return;
            }
            // If can't stay, fall through.
        }

        // NIGHT: active + energy drain
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
            }
            else {
                // If nowhere to move, just stay put (prevents predators dying in crowds)
                Location here = getLocation();
                if(here != null && nextFieldState.getDinosaurAt(here) == null) {
                    nextFieldState.placeDinosaur(this, here);
                } else {
                    setDead();
                }
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
        // Only hunts at night (sleeps in day)
        if(TimeManager.isDay()) return null;
    
        List<Location> adjacent = field.getAdjacentLocations(getLocation());
    
        // Strong at night + fog success penalty
        double timeMod = 1.15 * WeatherManager.predatorHuntModifier();
    
        for(Location loc : adjacent) {
            Dinosaur prey = field.getDinosaurAt(loc);
            if(prey == null || !prey.isAlive()) continue;
    
            // Dilophosaurus hunts Iguanadon only (your current rule)
            if(prey instanceof Iguanadon) {
                if(tryKill(prey, BASE_KILL_CHANCE, timeMod)) {
                    prey.setDead();
                    restoreToFullEnergy();
                    return loc;
                }
            }
        }
        return null;
    }

    private void giveBirth(Field currentField, Field nextFieldState, List<Location> freeLocations)
    {
        int births = breed(currentField);
        if(births > 0) {
            consumeEnergy(births * ENERGY_COST_PER_BABY);

            for(int b = 0; b < births && !freeLocations.isEmpty() && isAlive(); b++) {
                Location loc = freeLocations.remove(0);
                Dilophosaurus young = new Dilophosaurus(false, loc);
                nextFieldState.placeDinosaur(young, loc);
            }
        }
    }

    private int breed(Field currentField)
    {
        if(!canBreedThisStep()) return 0;
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