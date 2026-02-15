/*
 * Simple counter used by FieldStats.
 * Just stores a name and a count.
 */
public class Counter
{
    private final String name;
    private int count;

    // Makes a counter for some species/type name
    public Counter(String name)
    {
        this.name = name;
        count = 0;
    }

    // Gets the name label
    public String getName()
    {
        return name;
    }

    // Gets the current count
    public int getCount()
    {
        return count;
    }

    // Adds one
    public void increment()
    {
        count++;
    }

    // Resets back to 0
    public void reset()
    {
        count = 0;
    }
}