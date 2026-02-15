import java.util.List;
import java.util.Random;

/*
 * Diabloceratops herbivore.
 * Similar to iguanadon but older + heavy in rain (sometimes skips movement).
 */
public class Diabloceratops extends Herbivore
{
    private static final Random rand = Randomizer.getRandom();
    private int age;

    public Diabloceratops(boolean randomAge, Location location)
    {
        super(location, Tuning.get(SpeciesType.DIABLOCERATOPS).maxEnergy);
        age = randomAge ? rand.nextInt(60) : 0;
        if(randomAge) setEnergy((int)(getMaxEnergy() * 0.60));
    }

    @Override
    public int getDefence()
    {
        return Tuning.get(SpeciesType.DIABLOCERATOPS).defence;
    }

    @Override
    public SpeciesType getSpeciesType()
    {
        return SpeciesType.DIABLOCERATOPS;
    }

    @Override
    protected Dinosaur createYoung(Location loc)
    {
        return new Diabloceratops(false, loc);
    }

    // Does one sim step (age, eat, breed, move)
    public void act(Field currentField, Field nextFieldState)
    {
        age++;
        if(age > 70) { setDead(); return; }

        SpeciesTuning t = Tuning.get(SpeciesType.DIABLOCERATOPS);

        consumeEnergy(t.stepEnergyLoss);
        if(!isAlive()) return;

        eat(nextFieldState);

        List<Location> free = nextFieldState.getFreeAdjacentLocations(getLocation());
        if(!free.isEmpty()) giveBirth(currentField, nextFieldState, free);

        // heavy + rain => sometimes it just doesn't move
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