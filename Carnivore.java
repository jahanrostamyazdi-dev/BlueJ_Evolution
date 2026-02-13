/**
 * A carnivore is a Dinosaur that hunts other dinosaurs for food.
 * This class exists to share carnivore-specific behaviour later
 * (e.g. hunting logic, attack success, hunger systems, etc.).
 */
public abstract class Carnivore extends Dinosaur
{
    /**
     * Create a carnivore at a location.
     * @param location The carnivore's starting location.
     * @param maxEnergy Maximum energy for this carnivore.
     */
    public Carnivore(Location location, int maxEnergy)
    {
        super(location, maxEnergy);
    }
}