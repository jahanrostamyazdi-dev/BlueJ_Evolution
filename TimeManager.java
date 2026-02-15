/*
 * Tracks day/night cycle.
 * Day lasts DAY_LENGTH steps then switches to night for the same amount.
 */
public class TimeManager
{
    private static final int DAY_LENGTH = 50;
    private static TimeOfDay current = TimeOfDay.DAY;

    // Reset time back to day
    public static void reset()
    {
        current = TimeOfDay.DAY;
    }

    // Update based on step number
    public static void updateForStep(int step)
    {
        int phase = (step / DAY_LENGTH) % 2;
        current = (phase == 0) ? TimeOfDay.DAY : TimeOfDay.NIGHT;
    }

    // Get current time of day
    public static TimeOfDay getTimeOfDay()
    {
        return current;
    }

    // Convenience
    public static boolean isDay()
    {
        return current == TimeOfDay.DAY;
    }

    public static boolean isNight()
    {
        return current == TimeOfDay.NIGHT;
    }
}