/**
 * A herbivore is a Dinosaur that does not hunt other dinosaurs.
 * This class exists to share herbivore-specific behaviour later
 * (e.g. eating vegetation, herd behaviour, defence, etc.).
 */
public abstract class Herbivore extends Dinosaur
{
    /**
     * Create a herbivore at a location.
     * @param location The herbivore's starting location.
     * @param maxEnergy Maximum energy for this herbivore.
     */
    public Herbivore(Location location, int maxEnergy)
    {
        super(location, maxEnergy);
    }
}