/*
 * Tunable per-species parameters.
 * These are mutable because the tuning window updates them.
 */
public class SpeciesTuning
{
    public final SpeciesType type;

    public int maxEnergy = 20;
    public int stepEnergyLoss = 1;
    public int extraInfectedEnergyLoss = 1;

    public int breedingAge = 5;
    public double breedingProbability = 0.10;
    public int maxLitterSize = 3;
    public int breedingEnergyThreshold = 10;
    public int energyCostPerBaby = 1;

    public int biteSize = 25;
    public int energyPerVeg = 6;
    public int minVegToBreed = 0;

    public boolean heavy = false;
    public double rainMoveSkipChance = 0.50;

    public int attack = 8;
    public int defence = 6;
    public double baseKillChance = 0.72;
    public double dayKillMod = 1.0;
    public double nightKillMod = 1.0;

    public int daySenseRadius = 1;
    public int nightSenseRadius = 1;

    public boolean huntOnlyAtNight = false;

    // Makes a tuning object for a given species
    public SpeciesTuning(SpeciesType type)
    {
        this.type = type;
    }
}