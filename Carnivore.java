import java.util.Random;

/**
 * A carnivore is a Dinosaur that hunts other dinosaurs for food.
 * Includes a shared combat helper using an Attack/Defence probability model.
 */
public abstract class Carnivore extends Dinosaur
{
    private static final Random rand = Randomizer.getRandom();

    public Carnivore(Location location, int maxEnergy)
    {
        super(location, maxEnergy);
    }

    /**
     * @return attack rating for this carnivore (higher = better hunter)
     */
    public abstract int getAttack();

    /**
     * Attempt to kill a prey dinosaur using:
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

        // clamp
        if(chance < 0) chance = 0;
        if(chance > 1) chance = 1;

        return rand.nextDouble() <= chance;
    }
}