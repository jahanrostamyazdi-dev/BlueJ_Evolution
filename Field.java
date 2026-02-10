import java.util.*;

/**
 * Represent a rectangular grid of field positions.
 * Each position is able to store a single dinosaur/object.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 7.0
 */
public class Field
{
    // A random number generator for providing random locations.
    private static final Random rand = Randomizer.getRandom();
    
    // The dimensions of the field.
    private final int depth, width;
    // Dinosaurs mapped by location.
    private final Map<Location, Dinosaur> field = new HashMap<>();
    // The dinosaurs.
    private final List<Dinosaur> dinosaurs = new ArrayList<>();

    /**
     * Represent a field of the given dimensions.
     * @param depth The depth of the field.
     * @param width The width of the field.
     */
    public Field(int depth, int width)
    {
        this.depth = depth;
        this.width = width;
    }

    /**
     * Place a dinosaur at the given location.
     * If there is already a dinosaur at the location it will
     * be lost.
     * @param anDinosaur The dinosaur to be placed.
     * @param location Where to place the dinosaur.
     */
    public void placeDinosaur(Dinosaur anDinosaur, Location location)
    {
        assert location != null;
        Object other = field.get(location);
        if(other != null) {
            dinosaurs.remove(other);
        }
        field.put(location, anDinosaur);
        dinosaurs.add(anDinosaur);
    }
    
    /**
     * Return the dinosaur at the given location, if any.
     * @param location Where in the field.
     * @return The dinosaur at the given location, or null if there is none.
     */
    public Dinosaur getDinosaurAt(Location location)
    {
        return field.get(location);
    }

    /**
     * Get a shuffled list of the free adjacent locations.
     * @param location Get locations adjacent to this.
     * @return A list of free adjacent locations.
     */
    public List<Location> getFreeAdjacentLocations(Location location)
    {
        List<Location> free = new LinkedList<>();
        List<Location> adjacent = getAdjacentLocations(location);
        for(Location next : adjacent) {
            Dinosaur anDinosaur = field.get(next);
            if(anDinosaur == null) {
                free.add(next);
            }
            else if(!anDinosaur.isAlive()) {
                free.add(next);
            }
        }
        return free;
    }

    /**
     * Return a shuffled list of locations adjacent to the given one.
     * The list will not include the location itself.
     * All locations will lie within the grid.
     * @param location The location from which to generate adjacencies.
     * @return A list of locations adjacent to that given.
     */
    public List<Location> getAdjacentLocations(Location location)
    {
        // The list of locations to be returned.
        List<Location> locations = new ArrayList<>();
        if(location != null) {
            int row = location.row();
            int col = location.col();
            for(int roffset = -1; roffset <= 1; roffset++) {
                int nextRow = row + roffset;
                if(nextRow >= 0 && nextRow < depth) {
                    for(int coffset = -1; coffset <= 1; coffset++) {
                        int nextCol = col + coffset;
                        // Exclude invalid locations and the original location.
                        if(nextCol >= 0 && nextCol < width && (roffset != 0 || coffset != 0)) {
                            locations.add(new Location(nextRow, nextCol));
                        }
                    }
                }
            }
            
            // Shuffle the list. Several other methods rely on the list
            // being in a random order.
            Collections.shuffle(locations, rand);
        }
        return locations;
    }

    /**
     * Print out the number of allosaurs and iguanadons in the field.
     */
    public void fieldStats()
    {
        int numAllosaurs = 0, numIguanadons = 0;
        for(Dinosaur anDinosaur : field.values()) {
            if(anDinosaur instanceof Allosaurus allosaurus) {
                if(allosaurus.isAlive()) {
                    numAllosaurs++;
                }
            }
            else if(anDinosaur instanceof Iguanadon iguanadon) {
                if(iguanadon.isAlive()) {
                    numIguanadons++;
                }
            }
        }
        System.out.println("Iguanadons: " + numIguanadons +
                           " Allosaurs: " + numAllosaurs);
    }

    /**
     * Empty the field.
     */
    public void clear()
    {
        field.clear();
    }

    /**
     * Return whether there is at least one iguanadon and one allosaurus in the field.
     * @return true if there is at least one iguanadon and one allosaurus in the field.
     */
    public boolean isViable()
    {
        boolean iguanadonFound = false;
        boolean allosaurusFound = false;
        Iterator<Dinosaur> it = dinosaurs.iterator();
        while(it.hasNext() && ! (iguanadonFound && allosaurusFound)) {
            Dinosaur anDinosaur = it.next();
            if(anDinosaur instanceof Iguanadon iguanadon) {
                if(iguanadon.isAlive()) {
                    iguanadonFound = true;
                }
            }
            else if(anDinosaur instanceof Allosaurus allosaurus) {
                if(allosaurus.isAlive()) {
                    allosaurusFound = true;
                }
            }
        }
        return iguanadonFound && allosaurusFound;
    }
    
    /**
     * Get the list of dinosaurs.
     */
    public List<Dinosaur> getDinosaurs()
    {
        return dinosaurs;
    }

    /**
     * Return the depth of the field.
     * @return The depth of the field.
     */
    public int getDepth()
    {
        return depth;
    }
    
    /**
     * Return the width of the field.
     * @return The width of the field.
     */
    public int getWidth()
    {
        return width;
    }
}
