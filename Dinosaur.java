/**
 * Common elements of all dinosaurs.
 *
 * Adds a shared energy system:
 * - energy decreases when an animal spends energy (predators each step for now)
 * - if energy reaches 0, the dinosaur dies
 * - energy can be restored when eating (predators) or later by plants (herbivores)
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
        this.energy = maxEnergy; // default start fully energised
    }

    /**
     * Act.
     * @param currentField The current state of the field.
     * @param nextFieldState The new state being built.
     */
    public abstract void act(Field currentField, Field nextFieldState);

    /**
     * Check whether the dinosaur is alive or not.
     * @return true if the dinosaur is still alive.
     */
    public boolean isAlive()
    {
        return alive;
    }

    /**
     * Indicate that the dinosaur is no longer alive.
     */
    protected void setDead()
    {
        alive = false;
        location = null;
    }

    /**
     * Return the dinosaur's location.
     * @return The dinosaur's location.
     */
    public Location getLocation()
    {
        return location;
    }

    /**
     * Set the dinosaur's location.
     * @param location The new location.
     */
    protected void setLocation(Location location)
    {
        this.location = location;
    }

    // -------------------------
    // Energy API (shared)
    // -------------------------

    /**
     * @return current energy (0..maxEnergy)
     */
    public int getEnergy()
    {
        return energy;
    }

    /**
     * @return maximum energy
     */
    public int getMaxEnergy()
    {
        return maxEnergy;
    }

    /**
     * Set energy directly (clamped to 0..maxEnergy).
     * If energy becomes 0, the dinosaur dies.
     */
    protected void setEnergy(int newEnergy)
    {
        energy = Math.min(maxEnergy, Math.max(0, newEnergy));
        if(energy <= 0) {
            setDead();
        }
    }

    /**
     * Reduce energy by a positive amount.
     * If energy becomes 0, the dinosaur dies.
     */
    protected void consumeEnergy(int amount)
    {
        if(amount <= 0) return;
        setEnergy(energy - amount);
    }

    /**
     * Increase energy by a positive amount, up to maxEnergy.
     */
    protected void gainEnergy(int amount)
    {
        if(amount <= 0) return;
        setEnergy(energy + amount);
    }

    /**
     * Fully restore energy (used by predators after eating).
     */
    protected void restoreToFullEnergy()
    {
        setEnergy(maxEnergy);
    }
}