import java.util.*;

/**
 * Represent a rectangular grid of field positions.
 * Each position can store a single dinosaur.
 * Also stores vegetation (0..100) for each tile.
 */
public class Field
{
    private static final Random rand = Randomizer.getRandom();

    private final int depth, width;

    // Dinosaurs mapped by location.
    private final Map<Location, Dinosaur> field = new HashMap<>();
    // The dinosaurs list (used by Simulator and viability).
    private final List<Dinosaur> dinosaurs = new ArrayList<>();

    // Vegetation layer: 0..100 per tile
    private final int[][] vegetation;

    public Field(int depth, int width)
    {
        this.depth = depth;
        this.width = width;

        vegetation = new int[depth][width];
        randomizeVegetation();
    }

    // ---------------------------
    // Dinosaurs
    // ---------------------------

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

    public Dinosaur getDinosaurAt(Location location)
    {
        return field.get(location);
    }

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

    public List<Location> getAdjacentLocations(Location location)
    {
        List<Location> locations = new ArrayList<>();
        if(location != null) {
            int row = location.row();
            int col = location.col();
            for(int roffset = -1; roffset <= 1; roffset++) {
                int nextRow = row + roffset;
                if(nextRow >= 0 && nextRow < depth) {
                    for(int coffset = -1; coffset <= 1; coffset++) {
                        int nextCol = col + coffset;
                        if(nextCol >= 0 && nextCol < width && (roffset != 0 || coffset != 0)) {
                            locations.add(new Location(nextRow, nextCol));
                        }
                    }
                }
            }
            Collections.shuffle(locations, rand);
        }
        return locations;
    }

    public void fieldStats()
    {
        int numAllosaurs = 0, numIguanadons = 0;
        int numCarnotaurus = 0, numDilophosaurus = 0;
        int numDiabloceratops = 0, numAnkylosaurus = 0;

        for(Dinosaur anDinosaur : field.values()) {
            if(anDinosaur == null || !anDinosaur.isAlive()) continue;

            if(anDinosaur instanceof Allosaurus) numAllosaurs++;
            else if(anDinosaur instanceof Iguanadon) numIguanadons++;
            else if(anDinosaur instanceof Carnotaurus) numCarnotaurus++;
            else if(anDinosaur instanceof Dilophosaurus) numDilophosaurus++;
            else if(anDinosaur instanceof Diabloceratops) numDiabloceratops++;
            else if(anDinosaur instanceof Ankylosaurus) numAnkylosaurus++;
        }

        System.out.println(
            "Iguanadons: " + numIguanadons +
            " Diabloceratops: " + numDiabloceratops +
            " Ankylosaurus: " + numAnkylosaurus +
            " | Allosaurs: " + numAllosaurs +
            " Carnotaurus: " + numCarnotaurus +
            " Dilophosaurus: " + numDilophosaurus
        );
    }

    public void clear()
    {
        field.clear();
        dinosaurs.clear();
        randomizeVegetation();
    }

    public boolean isViable()
    {
        boolean herbFound = false;
        boolean carniFound = false;

        Iterator<Dinosaur> it = dinosaurs.iterator();
        while(it.hasNext() && !(herbFound && carniFound)) {
            Dinosaur d = it.next();
            if(d == null || !d.isAlive()) continue;

            if(d instanceof Herbivore) herbFound = true;
            else if(d instanceof Carnivore) carniFound = true;
        }
        return herbFound && carniFound;
    }

    public List<Dinosaur> getDinosaurs()
    {
        return dinosaurs;
    }

    public int getDepth()
    {
        return depth;
    }

    public int getWidth()
    {
        return width;
    }

    // ---------------------------
    // Vegetation
    // ---------------------------

    public int getVegetationAt(Location location)
    {
        return vegetation[location.row()][location.col()];
    }

    /**
     * Consume up to 'amount' vegetation at a tile.
     * @return how much was actually consumed.
     */
    public int consumeVegetationAt(Location location, int amount)
    {
        if(amount <= 0) return 0;
        int r = location.row();
        int c = location.col();
        int available = vegetation[r][c];
        int taken = Math.min(available, amount);
        vegetation[r][c] = available - taken;
        return taken;
    }

    /**
     * Vegetation regrows a bit everywhere each step.
     * Night regrows slightly faster; day slightly slower.
     */
    public void regrowVegetation(TimeOfDay timeOfDay, WeatherState weather)
    {
        // patchy regrowth so it doesn't fill evenly
        double p = 0.25;
        int baseGrow = (timeOfDay == TimeOfDay.NIGHT) ? 1 : 1;
    
        double mult = WeatherManager.vegetationRegrowMultiplier();
        int cap = WeatherManager.vegetationCap();
    
        // convert multiplier into either larger grow or higher probability
        // (keeps integer math simple)
        double pScaled = p * mult;
        if(pScaled > 0.85) pScaled = 0.85;
    
        int grow = baseGrow;
        if(mult >= 1.4) grow = baseGrow + 1;     // rain pushes growth a bit faster
        if(mult <= 0.60) grow = 0;               // heatwave: no active regrowth, only rare recovery below
    
        for(int r = 0; r < depth; r++) {
            for(int c = 0; c < width; c++) {
    
                // Heatwave can also "stress" plants by pulling high vegetation down toward cap.
                if(weather == WeatherState.HEATWAVE && vegetation[r][c] > cap) {
                    vegetation[r][c] = Math.max(cap, vegetation[r][c] - 1);
                }
    
                // Probabilistic regrowth
                if(rand.nextDouble() < pScaled) {
                    int v = vegetation[r][c] + grow;
                    if(v > cap) v = cap;
                    vegetation[r][c] = v;
                }
    
                // During heatwave, allow a tiny recovery chance even if grow==0
                if(weather == WeatherState.HEATWAVE && vegetation[r][c] < cap && rand.nextDouble() < 0.05) {
                    vegetation[r][c] = Math.min(cap, vegetation[r][c] + 1);
                }
            }
        }
    }

    /**
     * Copy the vegetation layer from an existing field into this one.
     * (Needed because Simulator creates a new Field each step.)
     */
    public void copyVegetationFrom(Field other)
    {
        for(int r = 0; r < depth; r++) {
            System.arraycopy(other.vegetation[r], 0, this.vegetation[r], 0, width);
        }
    }

    private void randomizeVegetation()
    {
        for(int r = 0; r < depth; r++) {
            for(int c = 0; c < width; c++) {
                // Start with moderate vegetation so herbivores don't instantly starve.
                vegetation[r][c] = 60 + rand.nextInt(41); // 60..100
            }
        }
    }
}