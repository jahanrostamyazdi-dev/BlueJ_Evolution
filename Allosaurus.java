import java.util.List;

/*
 * Allosaurus predator for the sim.
 * It loses energy per turn, can breed if it has enough energy + male nearby,
 * and it hunts certain herbivores. If it's boxed in and can't be placed in the
 * next field state, it dies (otherwise the sim can get weirdly stuck).
 *
 * (This class ended up a bit long but it's basically "do the turn".)
 */
public class Allosaurus extends Carnivore
{
    // Constructor: makes one at a location. randomAge just means "not full energy at start".
    public Allosaurus(boolean randomAge, Location location)
    {
        super(location, Tuning.get(SpeciesType.ALLOSAURUS).maxEnergy);

        if(randomAge) {
            // not sure what a good % is but this looked ok in testing
            int start = (int)(getMaxEnergy() * 0.60);
            setEnergy(start);
        }

        // System.out.println("[spawn] allo at " + location + " energy=" + getEnergy());
    }

    // Attack value comes from tuning (so we can balance without changing code)
    @Override
    public int getAttack()
    {
        return Tuning.get(SpeciesType.ALLOSAURUS).attack;
    }

    // Used in hunting: day/night changes it and weather can mess it up too.
    private double timeKillMod()
    {
        SpeciesTuning t = Tuning.get(SpeciesType.ALLOSAURUS);

        double mod = TimeManager.isNight() ? t.nightKillMod : t.dayKillMod;

        // fog/rain whatever: lowers hunting a bit
        mod *= WeatherManager.predatorHuntModifier();

        return mod;
    }

    // Main behaviour each step: drain energy, maybe breed, then hunt/move.
    public void act(Field currentField, Field nextFieldState)
    {
        consumeEnergy(Tuning.get(SpeciesType.ALLOSAURUS).stepEnergyLoss);
        incrementAge();
        if(!isAlive()) return;

        SpeciesTuning t = Tuning.get(SpeciesType.ALLOSAURUS);
        if(getAge() > t.maxAge) { setDead(); return; }

        // free spaces in NEXT field (so we don't collide)
        List<Location> freeLocs = nextFieldState.getFreeAdjacentLocations(getLocation());

        // babies first if there is space (otherwise it just wastes time)
        if(!freeLocs.isEmpty()) {
            int births = breed(currentField);

            // System.out.println("[allo] births=" + births + " at " + getLocation());

            for(int b = 0; b < births && !freeLocs.isEmpty(); b++) {
                Location loc = freeLocs.remove(0);
                nextFieldState.placeDinosaur(new Allosaurus(false, loc), loc);
            }
        }

        // hunt; if nothing found, just wander
        Location nextLoc = findFood(currentField);
        if(nextLoc == null && !freeLocs.isEmpty()) {
            nextLoc = freeLocs.remove(0);
        }

        if(nextLoc != null) {
            setLocation(nextLoc);
            nextFieldState.placeDinosaur(this, nextLoc);
        } else {
            // If blocked, stay if possible; if thats not possible then die.
            Location hereLoc = getLocation();
            if(hereLoc != null && nextFieldState.getDinosaurAt(hereLoc) == null) {
                nextFieldState.placeDinosaur(this, hereLoc);
            } else {
                setDead();
            }
        }
    }

    // Figures out if breeding happens this turn and returns number of babies (0 if none).
    private int breed(Field currentField)
    {
        if(!canBreedThisStep()) return 0;

        SpeciesTuning t = Tuning.get(SpeciesType.ALLOSAURUS);

        if(getEnergy() < t.breedingEnergyThreshold) return 0;
        if(getAge() < t.breedingAge) return 0;
        if(!isFemale()) return 0;
        if(!hasAdjacentMaleOfSameSpecies(currentField)) return 0;

        // probability roll
        if(Randomizer.getRandom().nextDouble() > t.breedingProbability) return 0;

        int births = Randomizer.getRandom().nextInt(Math.max(1, t.maxLitterSize)) + 1;

        // energy cost for babies (prevents infinite breeding)
        consumeEnergy(births * Math.max(0, t.energyCostPerBaby));
        if(!isAlive()) return 0;

        return births;
    }

    // ENTIRE findFood (left as its own thing because act() was getting too big)
    private Location findFood(Field field)
    {
        SpeciesTuning t = Tuning.get(SpeciesType.ALLOSAURUS);

        int nightRadius = t.nightSenseRadius - WeatherManager.predatorRangePenalty();
        if(nightRadius < 1) nightRadius = 1;

        List<Location> search = TimeManager.isNight()
                ? field.getLocationsWithinRadius(getLocation(), nightRadius)
                : field.getAdjacentLocations(getLocation());

        double timeMod = timeKillMod();

        // TODO: could maybe make prey list part of tuning later? but ok for now
        for(Location loc : search) {
            Dinosaur prey = field.getDinosaurAt(loc);
            if(prey == null || !prey.isAlive()) continue;

            if(prey instanceof Iguanadon || prey instanceof Diabloceratops || prey instanceof Ankylosaurus) {

                // System.out.println("[hunt] saw " + prey.getClass().getSimpleName() + " at " + loc);

                if(tryKill(prey, t.baseKillChance, timeMod)) {
                    prey.setDead();
                    restoreToFullEnergy();
                    return loc;
                }
            }
        }

        return null;
    }
}