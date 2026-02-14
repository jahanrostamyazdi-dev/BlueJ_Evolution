import java.util.Random;

/**
 * Carnivore base class with unified attack/defence kill model.
 * Also handles infection transfer when eating infected prey.
 */
public abstract class Carnivore extends Dinosaur
{
    private static final Random rand = Randomizer.getRandom();

    public Carnivore(Location location, int maxEnergy)
    {
        super(location, maxEnergy);
    }

    public abstract int getAttack();

    /**
     * successChance = baseChance * (attack/(attack+defence)) * timeOfDayModifier
     */
    protected boolean tryKill(Dinosaur prey, double baseChance, double timeOfDayModifier)
    {
        if(prey == null || !prey.isAlive()) return false;

        int attack = getAttack();
        int defence = 0;

        if(prey instanceof Herbivore herb) {
            defence = herb.getDefence();
        }

        double ratio = (attack + defence) == 0 ? 0.0 : ((double) attack / (attack + defence));
        double chance = baseChance * ratio * timeOfDayModifier;

        if(chance < 0) chance = 0;
        if(chance > 1) chance = 1;

        boolean success = rand.nextDouble() <= chance;

        if(success) {
            // If prey is infected, predator has high chance of becoming infected.
            if(prey.isInfected()) {
                DiseaseManager.onPredatorAteInfectedPrey(this);
            }
        }

        return success;
    }
}