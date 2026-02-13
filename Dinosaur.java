import java.util.List;
import java.util.Random;

/**
 * Common elements of all dinosaurs.
 *
 * Shared systems:
 * - Energy (0..maxEnergy)
 * - Sex (MALE/FEMALE) assigned at birth
 * - Utility method to check for adjacent male of same species
 */
public abstract class Dinosaur
{
    // Whether the dinosaur is alive or not.
    private boolean alive;
    // The dinosaur's position.
    private Location location;

    // Energy system
    private final int maxEnergy;
    private int energy;

    // Sex system
    private final Sex sex;

    // Shared random generator
    private static final Random rand = Randomizer.getRandom();

    /**
     * Constructor for objects of class Dinosaur.
     * @param location The dinosaur's location.
     * @param maxEnergy The maximum energy this dinosaur can have.
     */
    public Dinosaur(Location location, int maxEnergy)
    {
        this.alive = true;
        this.location = location;
        this.maxEnergy = maxEnergy;
        this.energy = maxEnergy;

        // Assign a random sex at birth.
        this.sex = rand.nextBoolean() ? Sex.MALE : Sex.FEMALE;
    }

    /**
     * Act.
     * @param currentField The current state of the field.
     * @param nextFieldState The new state being built.
     */
    public abstract void act(Field currentField, Field nextFieldState);

    public boolean isAlive()
    {
        return alive;
    }

    protected void setDead()
    {
        alive = false;
        location = null;
    }

    public Location getLocation()
    {
        return location;
    }

    protected void setLocation(Location location)
    {
        this.location = location;
    }

    // -------------------------
    // Energy API (shared)
    // -------------------------

    public int getEnergy()
    {
        return energy;
    }

    public int getMaxEnergy()
    {
        return maxEnergy;
    }

    protected void setEnergy(int newEnergy)
    {
        energy = Math.min(maxEnergy, Math.max(0, newEnergy));
        if(energy <= 0) {
            setDead();
        }
    }

    protected void consumeEnergy(int amount)
    {
        if(amount <= 0) return;
        setEnergy(energy - amount);
    }

    protected void gainEnergy(int amount)
    {
        if(amount <= 0) return;
        setEnergy(energy + amount);
    }

    protected void restoreToFullEnergy()
    {
        setEnergy(maxEnergy);
    }

    // -------------------------
    // Sex API (shared)
    // -------------------------

    public Sex getSex()
    {
        return sex;
    }

    public boolean isMale()
    {
        return sex == Sex.MALE;
    }

    public boolean isFemale()
    {
        return sex == Sex.FEMALE;
    }

    /**
     * Returns true if there is at least one adjacent MALE dinosaur
     * of the same species (same class) in the CURRENT field.
     */
    protected boolean hasAdjacentMaleOfSameSpecies(Field currentField)
    {
        List<Location> adjacent = currentField.getAdjacentLocations(getLocation());
        for(Location loc : adjacent) {
            Dinosaur d = currentField.getDinosaurAt(loc);
            if(d != null && d.isAlive()) {
                if(d.getClass() == this.getClass() && d.isMale()) {
                    return true;
                }
            }
        }
        return false;
    }
}