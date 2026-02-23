import java.util.List;

/*
 * Dilophosaurus predator.
 * Special rule: can be "night only" hunter, so it basically chills during the day.
 */
public class Dilophosaurus extends Carnivore
{
    public Dilophosaurus(boolean randomAge, Location location)
    {
        super(location, Tuning.get(SpeciesType.DILOPHOSAURUS).maxEnergy);

        if(randomAge) {
            setEnergy((int)(getMaxEnergy() * 0.60));
        }
    }

    @Override
    public int getAttack()
    {
        return Tuning.get(SpeciesType.DILOPHOSAURUS).attack;
    }

    // Dilo gets a strong night modifier and (usually) zero day mod if night-only is enabled
    private double timeKillMod()
    {
        SpeciesTuning t = Tuning.get(SpeciesType.DILOPHOSAURUS);

        double mod = TimeManager.isNight() ? t.nightKillMod : t.dayKillMod;

        mod *= WeatherManager.predatorHuntModifier();
        return mod;
    }

    // One sim step: drain energy, maybe sleep in day, breed, hunt, move
    public void act(Field currentField, Field nextFieldState)
    {
        consumeEnergy(Tuning.get(SpeciesType.DILOPHOSAURUS).stepEnergyLoss);
        incrementAge();
        if(!isAlive()) return;

        SpeciesTuning t = Tuning.get(SpeciesType.DILOPHOSAURUS);

        if(t.huntOnlyAtNight && TimeManager.isDay()) {
            Location here = getLocation();
            if(here != null && nextFieldState.getDinosaurAt(here) == null) {
                nextFieldState.placeDinosaur(this, here);
            }
            return;
        }

        List<Location> free = nextFieldState.getFreeAdjacentLocations(getLocation());

        if(!free.isEmpty()) {
            int births = breed(currentField);
            for(int b = 0; b < births && !free.isEmpty(); b++) {
                Location loc = free.remove(0);
                nextFieldState.placeDinosaur(new Dilophosaurus(false, loc), loc);
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

    // Breeding rules (same style as other carnivores)
    private int breed(Field currentField)
    {
        if(!canBreedThisStep()) return 0;

        SpeciesTuning t = Tuning.get(SpeciesType.DILOPHOSAURUS);

        if(getEnergy() < t.breedingEnergyThreshold) return 0;
        if(!isFemale()) return 0;
        if(!hasAdjacentMaleOfSameSpecies(currentField)) return 0;

        if(Randomizer.getRandom().nextDouble() > t.breedingProbability) return 0;

        int births = Randomizer.getRandom().nextInt(Math.max(1, t.maxLitterSize)) + 1;
        consumeEnergy(births * Math.max(0, t.energyCostPerBaby));
        if(!isAlive()) return 0;

        return births;
    }

    // Finds prey (Dilo just hunts Iguanadon in this version)
    private Location findFood(Field field)
    {
        SpeciesTuning t = Tuning.get(SpeciesType.DILOPHOSAURUS);
        if(t.huntOnlyAtNight && TimeManager.isDay()) return null;

        List<Location> adjacent = field.getAdjacentLocations(getLocation());
        double timeMod = timeKillMod();

        for(Location loc : adjacent) {
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