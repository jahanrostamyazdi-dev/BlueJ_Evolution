/**
 * A herbivore is a Dinosaur that does not hunt other dinosaurs.
 * Provides a defence rating used by the combat system.
 */
public abstract class Herbivore extends Dinosaur
{
    public Herbivore(Location location, int maxEnergy)
    {
        super(location, maxEnergy);
    }

    /**
     * @return defence rating for this herbivore (higher = harder to kill)
     */
    public abstract int getDefence();
}