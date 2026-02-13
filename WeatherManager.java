// WeatherManager.java
import java.util.Random;

/**
 * Global weather system that changes every fixed number of steps (weighted).
 */
public class WeatherManager
{
    // Change weather every N steps (so it doesn't flip constantly)
    private static final int CHANGE_INTERVAL = 60;

    // Weights (must sum to 1.0)
    private static final double W_CLEAR = 0.50;
    private static final double W_RAIN  = 0.22;
    private static final double W_FOG   = 0.18;
    private static final double W_HEAT  = 0.10;

    private static final Random rand = Randomizer.getRandom();

    private static WeatherState current = WeatherState.CLEAR;
    private static int stepsUntilChange = CHANGE_INTERVAL;

    // Track consecutive heatwave cycles to intensify scarcity
    private static int consecutiveHeatwaveCycles = 0;

    private WeatherManager() {}

    public static void reset()
    {
        current = WeatherState.CLEAR;
        stepsUntilChange = CHANGE_INTERVAL;
        consecutiveHeatwaveCycles = 0;
    }

    public static void updateOneStep()
    {
        stepsUntilChange--;
        if(stepsUntilChange <= 0) {
            // apply “cycle end” logic
            if(current == WeatherState.HEATWAVE) consecutiveHeatwaveCycles++;
            else consecutiveHeatwaveCycles = 0;

            current = rollNextWeather();
            stepsUntilChange = CHANGE_INTERVAL;
        }
    }

    public static WeatherState getWeather()
    {
        return current;
    }

    public static int getConsecutiveHeatwaveCycles()
    {
        return consecutiveHeatwaveCycles;
    }

    /**
     * Predators: fog reduces hunting effectiveness.
     */
    public static double predatorHuntModifier()
    {
        return (current == WeatherState.FOG) ? 0.80 : 1.0;
    }

    /**
     * Predators: fog can reduce sensing range.
     * You can use this when choosing a hunt radius.
     */
    public static int predatorRangePenalty()
    {
        return (current == WeatherState.FOG) ? 1 : 0;
    }

    /**
     * Vegetation regrowth multiplier.
     */
    public static double vegetationRegrowMultiplier()
    {
        if(current == WeatherState.RAIN) return 1.6;
        if(current == WeatherState.HEATWAVE) return 0.55;
        return 1.0;
    }

    /**
     * Vegetation cap during heatwaves. Intensifies if multiple cycles in a row.
     */
    public static int vegetationCap()
    {
        if(current != WeatherState.HEATWAVE) return 100;

        // First heatwave cycle caps at 80, then 70, then 60...
        int cap = 80 - (consecutiveHeatwaveCycles * 10);
        if(cap < 50) cap = 50;
        return cap;
    }

    private static WeatherState rollNextWeather()
    {
        double r = rand.nextDouble();
        if(r < W_CLEAR) return WeatherState.CLEAR;
        r -= W_CLEAR;
        if(r < W_RAIN) return WeatherState.RAIN;
        r -= W_RAIN;
        if(r < W_FOG) return WeatherState.FOG;
        return WeatherState.HEATWAVE;
    }
}