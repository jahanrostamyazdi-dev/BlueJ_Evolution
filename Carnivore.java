import java.util.Random;

/*
 * Base carnivore logic.
 * tryKill(...) uses attack vs prey defence + base kill chance + time modifier.
 * Also transfers disease if the prey was infected (chance-based).
 */
public abstract class Carnivore extends Dinosaur
{
    private static final Random rand = Randomizer.getRandom();

    public Carnivore(Location location, int maxEnergy)
    {
        super(location, maxEnergy);
    }

    // Carnivores have attack stat (from tuning)
    public abstract int getAttack();

    // Attempts a kill using the formula from the assignment write-up
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
            if(prey.isInfected()) {
                DiseaseManager.onPredatorAteInfectedPrey(this);
            }
        }

        return success;
    }
}