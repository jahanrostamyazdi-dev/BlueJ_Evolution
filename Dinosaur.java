
/**
 * Common elements of allosaurs and iguanadons.
 *
 * @author David J. Barnes and Michael Kölling
 * @version 7.0
 */
public abstract class Dinosaur
{
    // Whether the dinosaur is alive or not.
    private boolean alive;
    // The dinosaur's position.
    private Location location;

    /**
     * Constructor for objects of class Dinosaur.
     * @param location The dinosaur's location.
     */
    public Dinosaur(Location location)
    {
        this.alive = true;
        this.location = location;
    }
    
    /**
     * Act.
     * @param currentField The current state of the field.
     * @param nextFieldState The new state being built.
     */
    abstract public void act(Field currentField, Field nextFieldState);
    
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
}
