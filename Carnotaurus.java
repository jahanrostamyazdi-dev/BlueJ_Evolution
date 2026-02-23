import java.util.List;

/*
 * Carnotaurus predator.
 * In this version it hunts Iguanadon mostly and has a bigger day sense radius.
 */
public class Carnotaurus extends Carnivore
{
    public Carnotaurus(boolean randomAge, Location location)
    {
        super(location, Tuning.get(SpeciesType.CARNOTAURUS).maxEnergy);

        if(randomAge) {
            setEnergy((int)(getMaxEnergy() * 0.60));
        }
    }

    @Override
    public int getAttack()
    {
        return Tuning.get(SpeciesType.CARNOTAURUS).attack;
    }

    // Day/night modifier + fog modifier
    private double timeKillMod()
    {
        SpeciesTuning t = Tuning.get(SpeciesType.CARNOTAURUS);

        double mod = TimeManager.isNight() ? t.nightKillMod : t.dayKillMod;
        mod *= WeatherManager.predatorHuntModifier();

        return mod;
    }

    // One step: drain energy, breed, hunt, move/stay/die
    public void act(Field currentField, Field nextFieldState)
    {
        consumeEnergy(Tuning.get(SpeciesType.CARNOTAURUS).stepEnergyLoss);
        incrementAge();
        if(!isAlive()) return;

        List<Location> free = nextFieldState.getFreeAdjacentLocations(getLocation());

        if(!free.isEmpty()) {
            int births = breed(currentField);
            for(int b = 0; b < births && !free.isEmpty(); b++) {
                Location loc = free.remove(0);
                nextFieldState.placeDinosaur(new Carnotaurus(false, loc), loc);
            }
        }

        Location next = findFood(currentField);
        if(next == null && !free.isEmpty()) next = free.remove(0);

        if(next != null) {
            setLocation(next);
            nextFieldState.placeDinosaur(this, next);
        } else {
            Location here = getLocation();
            if(here != null && nextFieldState.getDinosaurAt(here) == null) {
                nextFieldState.placeDinosaur(this, here);
            } else {
                setDead();
            }
        }
    }

    // Breeding rules (same-ish as other predators)
    private int breed(Field currentField)
    {
        if(!canBreedThisStep()) return 0;

        SpeciesTuning t = Tuning.get(SpeciesType.CARNOTAURUS);

        if(getEnergy() < t.breedingEnergyThreshold) return 0;
        if(getAge() < t.breedingAge) return 0;
        if(!isFemale()) return 0;
        if(!hasAdjacentMaleOfSameSpecies(currentField)) return 0;

        if(Randomizer.getRandom().nextDouble() > t.breedingProbability) return 0;

        int births = Randomizer.getRandom().nextInt(Math.max(1, t.maxLitterSize)) + 1;
        consumeEnergy(births * Math.max(0, t.energyCostPerBaby));
        if(!isAlive()) return 0;

        return births;
    }

    // Hunts Iguanadon (and uses radius in day)
    private Location findFood(Field field)
    {
        SpeciesTuning t = Tuning.get(SpeciesType.CARNOTAURUS);

        int dayRadius = t.daySenseRadius - WeatherManager.predatorRangePenalty();
        if(dayRadius < 1) dayRadius = 1;

        List<Location> search = TimeManager.isDay()
                ? field.getLocationsWithinRadius(getLocation(), dayRadius)
                : field.getAdjacentLocations(getLocation());

        double timeMod = timeKillMod();

        for(Location loc : search) {
            Dinosaur prey = field.getDinosaurAt(loc);
            if(prey == null || !prey.isAlive()) continue;

            if(prey instanceof Iguanadon) {
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