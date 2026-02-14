import java.util.List;
import java.util.Random;

/**
 * Common elements of all dinosaurs:
 * - alive/location
 * - sex (male/female)
 * - energy
 * - disease (infected + timer + optional immunity)
 */
public abstract class Dinosaur
{
    private static final Random rand = Randomizer.getRandom();

    // Core state
    private boolean alive;
    private Location location;

    // Sex
    private final boolean female;

    // Energy
    private final int maxEnergy;
    private int energy;

    // Disease state
    private boolean infected;
    private int infectionTimer;   // steps remaining
    private int immunityTimer;    // steps remaining

    public Dinosaur(Location location, int maxEnergy)
    {
        this.alive = true;
        this.location = location;

        this.female = rand.nextBoolean();

        this.maxEnergy = maxEnergy;
        this.energy = maxEnergy;  // default: start full
        this.infected = false;
        this.infectionTimer = 0;
        this.immunityTimer = 0;
    }

    // --- Simulation ---
    public abstract void act(Field currentField, Field nextFieldState);

    // --- Alive / location ---
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

    // --- Sex ---
    public boolean isFemale()
    {
        return female;
    }

    /**
     * Used by breeding logic: female requires at least one adjacent male of same species.
     */
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

    // --- Energy ---
    public int getMaxEnergy()
    {
        return maxEnergy;
    }

    public int getEnergy()
    {
        return energy;
    }

    protected void setEnergy(int value)
    {
        energy = Math.max(0, Math.min(maxEnergy, value));
        if(energy <= 0) setDead();
    }

    public void restoreToFullEnergy()
    {
        setEnergy(maxEnergy);
    }

    public void gainEnergy(int amount)
    {
        if(!alive) return;
        if(amount <= 0) return;
        setEnergy(energy + amount);
    }

    public void consumeEnergy(int amount)
    {
        if(!alive) return;
        if(amount <= 0) return;
        setEnergy(energy - amount);
    }

    // --- Disease API ---
    public boolean isInfected()
    {
        return infected;
    }

    public boolean isImmune()
    {
        return immunityTimer > 0;
    }

    public boolean canBeInfected()
    {
        return isAlive() && !infected && immunityTimer <= 0;
    }

    public void infect(int duration)
    {
        if(!canBeInfected()) return;

        infected = true;
        infectionTimer = Math.max(1, duration);
        // If you want infection to clear immunity, keep immunityTimer as-is or set to 0.
        // We'll clear immunity so infection is meaningful.
        immunityTimer = 0;
    }

    /**
     * Called once per step (from Simulator) BEFORE act().
     * - Infected: extra energy drain, spread attempt
     * - Timer expires: survive if energy high enough (gain immunity), else die
     * - Immunity timer ticks down
     */
    public void tickDisease(Field currentField)
    {
        if(!isAlive()) return;

        // Tick immunity if present
        if(immunityTimer > 0) {
            immunityTimer--;
        }

        if(!infected) return;

        // While infected, drain extra energy (weakness)
        consumeEnergy(DiseaseManager.getExtraEnergyDrainWhileInfected());
        if(!isAlive()) return;

        // Spread to adjacent dinos
        DiseaseManager.attemptAdjacentSpread(this, currentField);

        // Count down infection
        infectionTimer--;

        if(infectionTimer <= 0) {
            // Infection ends: survive if energy high enough
            if(getEnergy() >= DiseaseManager.getSurviveEnergyThreshold()) {
                infected = false;
                infectionTimer = 0;
                immunityTimer = DiseaseManager.getImmunityDuration();
            } else {
                setDead();
            }
        }
    }

    /**
     * Use this in breeding: infected dinos cannot breed.
     */
    public boolean canBreedThisStep()
    {
        return isAlive() && !infected;
    }
}