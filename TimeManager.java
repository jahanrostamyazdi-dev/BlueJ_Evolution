/**
 * Global day/night controller.
 * Keeps a simple cycle: DAY for DAY_LENGTH steps, then NIGHT for DAY_LENGTH steps, repeat.
 */
public class TimeManager
{
    private static final int DAY_LENGTH = 50; // change to taste
    private static TimeOfDay current = TimeOfDay.DAY;

    public static void reset()
    {
        current = TimeOfDay.DAY;
    }

    public static void updateForStep(int step)
    {
        int phase = (step / DAY_LENGTH) % 2;
        current = (phase == 0) ? TimeOfDay.DAY : TimeOfDay.NIGHT;
    }

    public static TimeOfDay getTimeOfDay()
    {
        return current;
    }

    public static boolean isDay()
    {
        return current == TimeOfDay.DAY;
    }

    public static boolean isNight()
    {
        return current == TimeOfDay.NIGHT;
    }
}