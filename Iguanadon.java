import java.util.List;
import java.util.Random;

/*
 * Iguanadon herbivore.
 * Eats veg, breeds, moves to greener tiles, and dies of old age eventually.
 */
public class Iguanadon extends Herbivore
{
    private static final Random rand = Randomizer.getRandom();

    // Makes one at location (randomAge just picks a random starting age/energy)
    public Iguanadon(boolean randomAge, Location location)
    {
        super(location, Tuning.get(SpeciesType.IGUANADON).maxEnergy);
        if (randomAge)
        {
            setAge(rand.nextInt(40));
            setEnergy((int)(getMaxEnergy() * 0.60));
        }

        // System.out.println("[spawn] igu age=" + age + " loc=" + location);
    }

    // Defence used by carnivores for kill chance
    @Override
    public int getDefence()
    {
        return Tuning.get(SpeciesType.IGUANADON).defence;
    }

    // Species type for tuning lookup
    @Override
    public SpeciesType getSpeciesType()
    {
        return SpeciesType.IGUANADON;
    }

    // Creates a newborn
    @Override
    protected Dinosaur createYoung(Location loc)
    {
        return new Iguanadon(false, loc);
    }

    // One step of behaviour (age, drain energy, eat, breed, move)
    public void act(Field currentField, Field nextFieldState)
    {
        SpeciesTuning t = Tuning.get(SpeciesType.IGUANADON);

        consumeEnergy(t.stepEnergyLoss);
        if(!isAlive()) return;

        incrementAge();
        if(getAge() > t.maxAge) { setDead(); return; }

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