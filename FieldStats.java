import java.util.HashMap;
import java.util.Map;

/*
 * Collects population stats for the UI / console.
 * It lazily counts stuff when asked (because counting every placement is annoying).
 */
public class FieldStats
{
    private final Map<Class<?>, Counter> counters;
    private boolean countsValid;

    // Starts empty
    public FieldStats()
    {
        counters = new HashMap<>();
        countsValid = true;
    }

    // Returns a string like "Allosaurus: 10 Iguanadon: 50 ..."
    public String getPopulationDetails(Field field)
    {
        StringBuilder details = new StringBuilder();

        if(!countsValid) {
            generateCounts(field);
        }

        for(Class<?> key : counters.keySet()) {
            Counter info = counters.get(key);
            details.append(info.getName())
                   .append(": ")
                   .append(info.getCount())
                   .append(' ');
        }

        return details.toString();
    }

    // Marks stats as invalid and zeros counters (next call will regenerate)
    public void reset()
    {
        countsValid = false;
        for(Class<?> key : counters.keySet()) {
            Counter count = counters.get(key);
            count.reset();
        }
    }

    // Adds one count for the given class
    public void incrementCount(Class<?> dinosaurClass)
    {
        Counter count = counters.get(dinosaurClass);
        if(count == null) {
            count = new Counter(dinosaurClass.getName());
            counters.put(dinosaurClass, count);
        }
        count.increment();
    }

    // Marks counting as finished
    public void countFinished()
    {
        countsValid = true;
    }

    // The sim is viable if both herb + carn are present (delegates to Field)
    public boolean isViable(Field field)
    {
        return field.isViable();
    }

    // Counts everything currently in the field
    private void generateCounts(Field field)
    {
        reset();

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Dinosaur d = field.getDinosaurAt(new Location(row, col));
                if(d != null) {
                    incrementCount(d.getClass());
                }
            }
        }

        countsValid = true;
    }
}