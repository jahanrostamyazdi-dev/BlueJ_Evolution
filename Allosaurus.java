import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Allosaurus extends Carnivore
{
    private static final int BREEDING_AGE = 15;
    private static final int MAX_AGE = 150;
    private static final double BREEDING_PROBABILITY = 0.08;
    private static final int MAX_LITTER_SIZE = 2;

    private static final int FOOD_VALUE = 18;
    private static final int BREEDING_ENERGY_THRESHOLD = 6;
    private static final int ENERGY_COST_PER_BABY = 2;

    private static final double ANKYLO_KILL_CHANCE = 0.20;

    private static final Random rand = Randomizer.getRandom();

    private int age;

    public Allosaurus(boolean randomAge, Location location)
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
        // At night, Allosaurus can "see" further (radius 2).
        List<Location> search = TimeManager.isNight()
                ? getLocationsWithinRadius(field, getLocation(), 2)
                : field.getAdjacentLocations(getLocation());
    
        for(Location loc : search) {
            Dinosaur dinosaur = field.getDinosaurAt(loc);
    
            // Night hunting bonus (prey less aware)
            double huntMultiplier = TimeManager.isNight() ? 1.15 : 1.0;
    
            if(dinosaur instanceof Iguanadon iguanadon && iguanadon.isAlive()) {
                if(rand.nextDouble() <= 0.90 * huntMultiplier) {
                    iguanadon.setDead();
                    restoreToFullEnergy();
                    return loc;
                }
            }
            else if(dinosaur instanceof Diabloceratops diabloceratops && diabloceratops.isAlive()) {
                if(rand.nextDouble() <= 0.75 * huntMultiplier) {
                    diabloceratops.setDead();
                    restoreToFullEnergy();
                    return loc;
                }
            }
            else if(dinosaur instanceof Ankylosaurus ankylosaurus && ankylosaurus.isAlive()) {
                if(rand.nextDouble() <= ANKYLO_KILL_CHANCE * huntMultiplier) {
                    ankylosaurus.setDead();
                    restoreToFullEnergy();
                    return loc;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns all locations within a square radius (Chebyshev distance) around a centre.
     * Radius 1 = adjacent; radius 2 = two tiles out.
     */
    private List<Location> getLocationsWithinRadius(Field field, Location centre, int radius)
    {
        java.util.Set<Location> set = new java.util.HashSet<>();
        for(int r = centre.row() - radius; r <= centre.row() + radius; r++) {
            for(int c = centre.col() - radius; c <= centre.col() + radius; c++) {
                if(r >= 0 && r < field.getDepth() && c >= 0 && c < field.getWidth()) {
                    Location loc = new Location(r, c);
                    if(!loc.equals(centre)) set.add(loc);
                }
            }
        }
        java.util.List<Location> list = new java.util.ArrayList<>(set);
        java.util.Collections.shuffle(list, Randomizer.getRandom());
        return list;
    }

    private void giveBirth(Field currentField, Field nextFieldState, List<Location> freeLocations)
    {
        int births = breed(currentField);
        if(births > 0) {
            consumeEnergy(births * ENERGY_COST_PER_BABY);

            for(int b = 0; b < births && !freeLocations.isEmpty() && isAlive(); b++) {
                Location loc = freeLocations.remove(0);
                Allosaurus young = new Allosaurus(false, loc);
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