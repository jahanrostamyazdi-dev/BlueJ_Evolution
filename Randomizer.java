import java.util.Random;

/*
 * Shared random generator.
 * Fixed seed means the sim is repeatable (which was super useful for testing).
 */
public class Randomizer
{
    private static final int SEED = 6969;

    private static final boolean useShared = true;
    private static final Random rand = new Random(SEED);

    public Randomizer()
    {
    }

    // Returns the Random used by the sim
    public static Random getRandom()
    {
        if(useShared) {
            return rand;
        }
        else {
            return new Random();
        }
    }

    // Reset seed (only matters if useShared is true)
    public static void reset()
    {
        if(useShared) {
            rand.setSeed(SEED);
        }
    }
}