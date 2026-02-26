import java.util.Random;

/*
 * Global weather controller.
 * Every so often it changes the weather using weighted chances.
 * Heatwaves also track how many cycles in a row happened (so it gets harsher).
 */
public class WeatherManager
{
    private static final int CHANGE_INTERVAL = 60;

    private static final Random rand = Randomizer.getRandom();

    private static WeatherState current = WeatherState.CLEAR;
    private static int stepsUntilChange = CHANGE_INTERVAL;

    private static int consecutiveHeatwaveCycles = 0;

    private WeatherManager() {}

    // Reset weather back to normal
    public static void reset()
    {
        current = WeatherState.CLEAR;
        stepsUntilChange = CHANGE_INTERVAL;
        consecutiveHeatwaveCycles = 0;
    }

    // Called each sim step (counts down and swaps weather if needed)
    public static void updateOneStep()
    {
        stepsUntilChange--;
        if(stepsUntilChange <= 0) {
            if(current == WeatherState.HEATWAVE) consecutiveHeatwaveCycles++;
            else consecutiveHeatwaveCycles = 0;

            current = rollNextWeather();
            stepsUntilChange = CHANGE_INTERVAL;

            // System.out.println("[weather] now " + current + " heatStreak=" + consecutiveHeatwaveCycles);
        }
    }

    // Gets current weather
    public static WeatherState getWeather()
    {
        return current;
    }

    // Used for vegetation cap logic
    public static int getConsecutiveHeatwaveCycles()
    {
        return consecutiveHeatwaveCycles;
    }

    // Fog makes hunting a bit worse
    public static double predatorHuntModifier()
    {
        return (current == WeatherState.FOG) ? 0.80 : 1.0;
    }

    // Fog can also reduce sensing range
    public static int predatorRangePenalty()
    {
        return (current == WeatherState.FOG) ? 1 : 0;
    }

    // Vegetation grows faster in rain, slower in heat
    public static double vegetationRegrowMultiplier()
    {
        if(current == WeatherState.RAIN) return 1.6;
        if(current == WeatherState.HEATWAVE) return 0.55;
        return 1.0;
    }

    // Max vegetation during heatwaves (drops more if heatwaves repeat)
    public static int vegetationCap()
    {
        if(current != WeatherState.HEATWAVE) return 100;

        int cap = 80 - (consecutiveHeatwaveCycles * 10);
        if(cap < 50) cap = 50;
        return cap;
    }

    // Rolls next weather based on weights
    private static WeatherState rollNextWeather()
    {
        double r = rand.nextDouble();
        if(r < Tuning.wClear) return WeatherState.CLEAR;

        r -= Tuning.wClear;
        if(r < Tuning.wRain) return WeatherState.RAIN;

        r -= Tuning.wRain;
        if(r < Tuning.wFog) return WeatherState.FOG;

        return WeatherState.HEATWAVE;
    }
}