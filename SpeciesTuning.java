/**
 * Per-species tunable parameters (mutable on purpose for debug tuning).
 */
public class SpeciesTuning
{
    // Shared / identity
    public final SpeciesType type;

    // Energy
    public int maxEnergy = 20;
    public int stepEnergyLoss = 1;                 // base energy drain per step
    public int extraInfectedEnergyLoss = 1;        // additional drain while infected

    // Breeding
    public int breedingAge = 5;
    public double breedingProbability = 0.10;
    public int maxLitterSize = 3;
    public int breedingEnergyThreshold = 10;
    public int energyCostPerBaby = 1;

    // Herbivore feeding (ignored by carnivores)
    public int biteSize = 25;                      // vegetation taken per step
    public int energyPerVeg = 6;                   // veg -> energy conversion (eaten/energyPerVeg)
    public int minVegToBreed = 0;                  // optional gating (0 disables)
    public boolean heavy = false;                  // heavy animals slow in rain
    public double rainMoveSkipChance = 0.50;       // heavy rain slow-down

    // Combat (ignored by herbivores partly)
    public int attack = 8;                         // carnivores
    public int defence = 6;                        // herbivores
    public double baseKillChance = 0.72;           // carnivores
    public double dayKillMod = 1.0;
    public double nightKillMod = 1.0;

    // Sensing radius if you do ranged hunting (optional)
    public int daySenseRadius = 1;
    public int nightSenseRadius = 1;

    // Dilo special
    public boolean huntOnlyAtNight = false;

    public SpeciesTuning(SpeciesType type)
    {
        this.type = type;
    }
}