import java.util.List;
import java.util.Random;

/*
 * Ankylosaurus herbivore.
 * Tougher defence + very old age, also heavy in rain like diabloceratops.
 */
public class Ankylosaurus extends Herbivore
{
    private static final Random rand = Randomizer.getRandom();

    public Ankylosaurus(boolean randomAge, Location location)
    {
        super(location, Tuning.get(SpeciesType.ANKYLOSAURUS).maxEnergy);
        if (randomAge)
        {
            setAge(rand.nextInt(90));
            setEnergy((int)(getMaxEnergy() * 0.60));
        }
    }

    @Override
    public int getDefence()
    {
        return Tuning.get(SpeciesType.ANKYLOSAURUS).defence;
    }

    @Override
    public SpeciesType getSpeciesType()
    {
        return SpeciesType.ANKYLOSAURUS;
    }

    @Override
    protected Dinosaur createYoung(Location loc)
    {
        return new Ankylosaurus(false, loc);
    }

    public void act(Field currentField, Field nextFieldState)
    {
        SpeciesTuning t = Tuning.get(SpeciesType.ANKYLOSAURUS);

        consumeEnergy(t.stepEnergyLoss);
        if(!isAlive()) return;

        incrementAge();
        if(getAge() > t.maxAge) { setDead(); return; }

        eat(nextFieldState);

        List<Location> free = nextFieldState.getFreeAdjacentLocations(getLocation());
        if(!free.isEmpty()) giveBirth(currentField, nextFieldState, free);

        if(t.heavy && WeatherManager.getWeather() == WeatherState.RAIN) {
            if(Randomizer.getRandom().nextDouble() < t.rainMoveSkipChance) {
                Location here = getLocation();
                if(here != null && nextFieldState.getDinosaurAt(here) == null) {
                    nextFieldState.placeDinosaur(this, here);
                    return;
                }
            }
        }

        Location next = chooseBestVegetationMove(currentField, free);
        if(next != null) {
            setLocation(next);
            nextFieldState.placeDinosaur(this, next);
        } else {
            setDead();
        }
    }
}