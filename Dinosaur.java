import java.util.List;
import java.util.Random;

/*
 * Base dinosaur class.
 * Stores alive/location, sex, energy, and disease timers.
 * act(...) is implemented by concrete dinos.
 */
public abstract class Dinosaur
{
    private static final Random rand = Randomizer.getRandom();

    private boolean alive;
    private Location location;

    private final boolean female;

    private final int maxEnergy;
    private int energy;

    private boolean infected;
    private int infectionTimer;
    private int immunityTimer;

    // Makes a dinosaur with max energy and random sex
    public Dinosaur(Location location, int maxEnergy)
    {
        this.alive = true;
        this.location = location;

        this.female = rand.nextBoolean();

        this.maxEnergy = maxEnergy;
        this.energy = maxEnergy;

        this.infected = false;
        this.infectionTimer = 0;
        this.immunityTimer = 0;
    }

    // Each dinosaur decides what to do per step
    public abstract void act(Field currentField, Field nextFieldState);

    // Checks alive state
    public boolean isAlive()
    {
        return alive;
    }

    // Kills the dinosaur and clears location (so it disappears)
    protected void setDead()
    {
        alive = false;
        location = null;
    }

    // Gets current location
    public Location getLocation()
    {
        return location;
    }

    // Updates location (used by act logic)
    protected void setLocation(Location location)
    {
        this.location = location;
    }

    // True if female (male is just !female)
    public boolean isFemale()
    {
        return female;
    }

    // Checks adjacency for a male of the same species (used by breeding rules)
    public boolean hasAdjacentMaleOfSameSpecies(Field currentField)
    {
        List<Location> adjacent = currentField.getAdjacentLocations(getLocation());
        for(Location loc : adjacent) {
            Dinosaur d = currentField.getDinosaurAt(loc);
            if(d != null && d.isAlive() && d.getClass() == this.getClass()) {
                if(!d.isFemale()) return true;
            }
        }
        return false;
    }

    // Max energy for this dino
    public int getMaxEnergy()
    {
        return maxEnergy;
    }

    // Current energy
    public int getEnergy()
    {
        return energy;
    }

    // Sets energy (clamped). If energy hits 0 it dies.
    protected void setEnergy(int value)
    {
        energy = Math.max(0, Math.min(maxEnergy, value));
        if(energy <= 0) setDead();
    }

    // Restores to max energy (usually after eating)
    public void restoreToFullEnergy()
    {
        setEnergy(maxEnergy);
    }

    // Adds energy (if alive)
    public void gainEnergy(int amount)
    {
        if(!alive) return;
        if(amount <= 0) return;
        setEnergy(energy + amount);
    }

    // Consumes energy (if alive)
    public void consumeEnergy(int amount)
    {
        if(!alive) return;
        if(amount <= 0) return;
        setEnergy(energy - amount);
    }

    // True if currently infected
    public boolean isInfected()
    {
        return infected;
    }

    // True if immune timer is active
    public boolean isImmune()
    {
        return immunityTimer > 0;
    }

    // Can become infected if alive and not currently infected and not immune
    public boolean canBeInfected()
    {
        return isAlive() && !infected && immunityTimer <= 0;
    }

    // Infects for a given duration (if possible)
    public void infect(int duration)
    {
        if(!canBeInfected()) return;

        infected = true;
        infectionTimer = Math.max(1, duration);
        immunityTimer = 0;
    }

    // Disease step logic (Simulator calls this before act)
    public void tickDisease(Field currentField)
    {
        if(!isAlive()) return;

        if(immunityTimer > 0) {
            immunityTimer--;
        }

        if(!infected) return;

        consumeEnergy(DiseaseManager.getExtraEnergyDrainWhileInfected());
        if(!isAlive()) return;

        DiseaseManager.attemptAdjacentSpread(this, currentField);

        infectionTimer--;

        if(infectionTimer <= 0) {
            if(getEnergy() >= DiseaseManager.getSurviveEnergyThreshold()) {
                infected = false;
                infectionTimer = 0;
                immunityTimer = DiseaseManager.getImmunityDuration();
            } else {
                setDead();
            }
        }
    }

    // Used by breeding logic (infected dinos can't breed)
    public boolean canBreedThisStep()
    {
        return isAlive() && !infected;
    }
}