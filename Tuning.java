import java.util.EnumMap;
import java.util.Map;

/*
 * Global tuning values + per-species tuning objects.
 * This is mutable on purpose because the tuning UI updates it live.
 */
public class Tuning
{
    private static final Map<SpeciesType, SpeciesTuning> species = new EnumMap<>(SpeciesType.class);

    public static int simDelayMs = 50;

    public static int vegInitialMin = 60;
    public static int vegInitialMax = 100;

    public static double vegRegrowChance = 0.65;
    public static int vegRegrowAmountDay = 3;
    public static int vegRegrowAmountNight = 3;

    public static int weatherChangeInterval = 60;
    public static double wClear = 0.50, wRain = 0.22, wFog = 0.18, wHeat = 0.10;

    public static int infectionMinDuration = 35;
    public static int infectionMaxDuration = 70;
    public static double adjacentSpreadChance = 0.08;
    public static double predatorEatInfectedChance = 0.70;
    public static int extraInfectedEnergyLoss = 1;
    public static int surviveEnergyThreshold = 8;
    public static int immunityDuration = 50;

    public static int initialInfections = 6;
    public static double spontaneousOutbreakChance = 0.0;

    public static double pAllosaurus = 0.010;
    public static double pCarnotaurus = 0.013;
    public static double pDilophosaurus = 0.010;

    public static double pIguanadon = 0.060;
    public static double pDiabloceratops = 0.045;
    public static double pAnkylosaurus = 0.030;

    // Default tuning values (these just felt "ok" when testing)
    static {
        species.put(SpeciesType.ALLOSAURUS, defaultAllo());
        species.put(SpeciesType.CARNOTAURUS, defaultCarno());
        species.put(SpeciesType.DILOPHOSAURUS, defaultDilo());

        species.put(SpeciesType.IGUANADON, defaultIgu());
        species.put(SpeciesType.DIABLOCERATOPS, defaultDiablo());
        species.put(SpeciesType.ANKYLOSAURUS, defaultAnky());
    }

    // Gets tuning for a species
    public static SpeciesTuning get(SpeciesType type)
    {
        return species.get(type);
    }

    // Default allosaurus settings
    private static SpeciesTuning defaultAllo() {
        SpeciesTuning t = new SpeciesTuning(SpeciesType.ALLOSAURUS);
        t.maxAge = 75; t.maxEnergy = 38; t.stepEnergyLoss = 1;
        t.breedingAge = 15; t.breedingProbability = 0.21; t.maxLitterSize = 3;
        t.breedingEnergyThreshold = 6; t.energyCostPerBaby = 3;
        t.attack = 21; t.baseKillChance = 0.7; t.dayKillMod = 1.0; t.nightKillMod = 0.5;
        t.daySenseRadius = 1; t.nightSenseRadius = 2;
        return t;
    }

    // Default carnotaurus settings
    private static SpeciesTuning defaultCarno() {
        SpeciesTuning t = new SpeciesTuning(SpeciesType.CARNOTAURUS);
        t.maxAge = 70; t.maxEnergy = 44; t.stepEnergyLoss = 1;
        t.breedingAge = 10; t.breedingProbability = 0.21; t.maxLitterSize = 2;
        t.breedingEnergyThreshold = 6; t.energyCostPerBaby = 2;
        t.attack = 21; t.baseKillChance = 0.67; t.dayKillMod = 1.2; t.nightKillMod = 0.4;
        t.daySenseRadius = 2; t.nightSenseRadius = 1;
        return t;
    }

    // Default dilo settings (night hunter)
    private static SpeciesTuning defaultDilo() {
        SpeciesTuning t = new SpeciesTuning(SpeciesType.DILOPHOSAURUS);
        t.maxAge = 480; t.maxEnergy = 100; t.stepEnergyLoss = 0;
        t.breedingAge = 10; t.breedingProbability = 0.31; t.maxLitterSize = 3;
        t.breedingEnergyThreshold = 5; t.energyCostPerBaby = 3;
        t.attack = 45; t.baseKillChance = 0.70; t.dayKillMod = 0.0; t.nightKillMod = 1.15;
        t.huntOnlyAtNight = true;
        t.daySenseRadius = 0; t.nightSenseRadius = 1;
        return t;
    }

    // Default iguanadon settings
    private static SpeciesTuning defaultIgu() {
        SpeciesTuning t = new SpeciesTuning(SpeciesType.IGUANADON);
        t.maxAge = 110; t.maxEnergy = 65; t.stepEnergyLoss = 1;
        t.breedingAge = 7; t.breedingProbability = 0.25; t.maxLitterSize = 3;
        t.breedingEnergyThreshold = 8; t.energyCostPerBaby = 1;
        t.biteSize = 20; t.energyPerVeg = 24;
        t.minVegToBreed = 2;
        t.defence = 50;
        return t;
    }

    // Default diabloceratops settings
    private static SpeciesTuning defaultDiablo() {
        SpeciesTuning t = new SpeciesTuning(SpeciesType.DIABLOCERATOPS);
        t.maxAge = 130; t.maxEnergy = 70; t.stepEnergyLoss = 1;
        t.breedingAge = 8; t.breedingProbability = 0.27; t.maxLitterSize = 2;
        t.breedingEnergyThreshold = 10; t.energyCostPerBaby = 2;
        t.biteSize = 16; t.energyPerVeg = 20;
        t.minVegToBreed = 2;
        t.heavy = true; t.rainMoveSkipChance = 0.50;
        t.defence = 50;
        return t;
    }

    // Default ankylosaurus settings
    private static SpeciesTuning defaultAnky() {
        SpeciesTuning t = new SpeciesTuning(SpeciesType.ANKYLOSAURUS);
        t.maxAge = 360; t.maxEnergy = 76; t.stepEnergyLoss = 1;
        t.breedingAge = 12; t.breedingProbability = 0.27; t.maxLitterSize = 2;
        t.breedingEnergyThreshold = 12; t.energyCostPerBaby = 2;
        t.biteSize = 14; t.energyPerVeg = 20;
        t.minVegToBreed = 2;
        t.heavy = true; t.rainMoveSkipChance = 0.50;
        t.defence = 50;
        return t;
    }
}