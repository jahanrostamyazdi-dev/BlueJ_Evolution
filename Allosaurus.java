import java.util.List;

public class Allosaurus extends Carnivore
{
    public Allosaurus(boolean randomAge, Location location)
    {
        super(location, Tuning.get(SpeciesType.ALLOSAURUS).maxEnergy);

        // Optional: randomise energy slightly for variety
        if(randomAge) {
            int start = (int)(getMaxEnergy() * 0.60);
            setEnergy(start);
        }
    }

    @Override
    public int getAttack()
    {
        return Tuning.get(SpeciesType.ALLOSAURUS).attack;
    }

    private double timeKillMod()
    {
        SpeciesTuning t = Tuning.get(SpeciesType.ALLOSAURUS);
        double mod = TimeManager.isNight() ? t.nightKillMod : t.dayKillMod;
        // Fog reduces hunting success
        mod *= WeatherManager.predatorHuntModifier();
        return mod;
    }

    public void act(Field currentField, Field nextFieldState)
    {
        consumeEnergy(Tuning.get(SpeciesType.ALLOSAURUS).stepEnergyLoss);
        if(!isAlive()) return;

        List<Location> free = nextFieldState.getFreeAdjacentLocations(getLocation());

        if(!free.isEmpty()) {
            // breeding using tuning
            int births = breed(currentField);
            for(int b = 0; b < births && !free.isEmpty(); b++) {
                Location loc = free.remove(0);
                nextFieldState.placeDinosaur(new Allosaurus(false, loc), loc);
            }
        }

        Location next = findFood(currentField);
        if(next == null && !free.isEmpty()) next = free.remove(0);

        if(next != null) {
            setLocation(next);
            nextFieldState.placeDinosaur(this, next);
        } else {
            // Stay put if blocked (prevents overcrowding wipeouts)
            Location here = getLocation();
            if(here != null && nextFieldState.getDinosaurAt(here) == null) {
                nextFieldState.placeDinosaur(this, here);
            } else {
                setDead();
            }
        }
    }

    private int breed(Field currentField)
    {
        if(!canBreedThisStep()) return 0;

        SpeciesTuning t = Tuning.get(SpeciesType.ALLOSAURUS);

        if(getEnergy() < t.breedingEnergyThreshold) return 0;
        if(!isFemale()) return 0;
        if(!hasAdjacentMaleOfSameSpecies(currentField)) return 0;

        if(Randomizer.getRandom().nextDouble() > t.breedingProbability) return 0;

        int births = Randomizer.getRandom().nextInt(Math.max(1, t.maxLitterSize)) + 1;
        consumeEnergy(births * Math.max(0, t.energyCostPerBaby));
        if(!isAlive()) return 0;
        return births;
    }

    // ENTIRE findFood
    private Location findFood(Field field)
    {
        SpeciesTuning t = Tuning.get(SpeciesType.ALLOSAURUS);

        int nightRadius = t.nightSenseRadius - WeatherManager.predatorRangePenalty();
        if(nightRadius < 1) nightRadius = 1;

        List<Location> search = TimeManager.isNight()
                ? field.getLocationsWithinRadius(getLocation(), nightRadius)
                : field.getAdjacentLocations(getLocation());

        double timeMod = timeKillMod();

        for(Location loc : search) {
            Dinosaur prey = field.getDinosaurAt(loc);
            if(prey == null || !prey.isAlive()) continue;

            if(prey instanceof Iguanadon || prey instanceof Diabloceratops || prey instanceof Ankylosaurus) {
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