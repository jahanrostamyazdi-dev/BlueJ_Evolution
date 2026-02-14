import java.util.*;

/**
 * Carnotaurus: faster in DAY.
 * - Hunts Iguanadon only (your current rule)
 * - Day speed: searches radius 2 in DAY, adjacent in NIGHT
 * - Uses unified Attack/Defence combat model via tryKill(...)
 */
public class Carnotaurus extends Carnivore
{
    private static final int BREEDING_AGE = 12;
    private static final int MAX_AGE = 120;
    private static final double BREEDING_PROBABILITY = 0.07;
    private static final int MAX_LITTER_SIZE = 2;

    private static final int FOOD_VALUE = 16;

    private static final int BREEDING_ENERGY_THRESHOLD = 7;
    private static final int ENERGY_COST_PER_BABY = 2;

    private static final double BASE_KILL_CHANCE = 0.92;

    private static final Random rand = Randomizer.getRandom();

    private int age;

    public Carnotaurus(boolean randomAge, Location location)
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
        return 8;
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
        // Day advantage normally radius 2 in day, adjacent at night.
        // Fog reduces sensing range by 1 (so day radius 2 becomes 1).
        int dayRadius = 2 - WeatherManager.predatorRangePenalty();
        if(dayRadius < 1) dayRadius = 1;
    
        List<Location> search = TimeManager.isDay()
                ? getLocationsWithinRadius(field, getLocation(), dayRadius)
                : field.getAdjacentLocations(getLocation());
    
        // Slightly weaker at night + fog success penalty
        double timeMod = (TimeManager.isNight() ? 0.95 : 1.0) * WeatherManager.predatorHuntModifier();
    
        for(Location loc : search) {
            Dinosaur prey = field.getDinosaurAt(loc);
            if(prey == null || !prey.isAlive()) continue;
    
            // Carnotaurus hunts Iguanadon only (your current rule)
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

    private List<Location> getLocationsWithinRadius(Field field, Location centre, int radius)
    {
        Set<Location> set = new HashSet<>();
        for(int r = centre.row() - radius; r <= centre.row() + radius; r++) {
            for(int c = centre.col() - radius; c <= centre.col() + radius; c++) {
                if(r >= 0 && r < field.getDepth() && c >= 0 && c < field.getWidth()) {
                    Location loc = new Location(r, c);
                    if(!loc.equals(centre)) set.add(loc);
                }
            }
        }
        List<Location> list = new ArrayList<>(set);
        Collections.shuffle(list, Randomizer.getRandom());
        return list;
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