import java.util.List;
import java.util.Random;

public class Iguanadon extends Herbivore
{
    private static final Random rand = Randomizer.getRandom();
    private int age;

    public Iguanadon(boolean randomAge, Location location)
    {
        super(location, Tuning.get(SpeciesType.IGUANADON).maxEnergy);
        age = randomAge ? rand.nextInt(40) : 0; // keep your old MAX_AGE style if you want
        if(randomAge) setEnergy((int)(getMaxEnergy() * 0.60));
    }

    @Override
    public int getDefence()
    {
        return Tuning.get(SpeciesType.IGUANADON).defence;
    }

    @Override
    public SpeciesType getSpeciesType()
    {
        return SpeciesType.IGUANADON;
    }

    @Override
    protected Dinosaur createYoung(Location loc)
    {
        return new Iguanadon(false, loc);
    }

    public void act(Field currentField, Field nextFieldState)
    {
        age++;
        if(age > 40) { setDead(); return; }

        SpeciesTuning t = Tuning.get(SpeciesType.IGUANADON);

        consumeEnergy(t.stepEnergyLoss);
        if(!isAlive()) return;

        eat(nextFieldState);

        List<Location> free = nextFieldState.getFreeAdjacentLocations(getLocation());
        if(!free.isEmpty()) giveBirth(currentField, nextFieldState, free);

        Location next = chooseBestVegetationMove(currentField, free);
        if(next != null) {
            setLocation(next);
            nextFieldState.placeDinosaur(this, next);
        } else {
            setDead();
        }
    }
}