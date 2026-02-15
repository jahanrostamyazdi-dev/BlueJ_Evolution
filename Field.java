import java.util.*;

/*
 * Represents the simulation grid.
 * Stores dinos by Location + also has vegetation (0..100) per tile.
 * Simulator builds a new Field each step, so there's also copyVegetationFrom.
 */
public class Field
{
    private static final Random rand = Randomizer.getRandom();

    private final int depth, width;

    private final Map<Location, Dinosaur> field = new HashMap<>();
    private final List<Dinosaur> dinosaurs = new ArrayList<>();

    private final int[][] vegetation;

    // Makes a field with random vegetation to start
    public Field(int depth, int width)
    {
        this.depth = depth;
        this.width = width;

        vegetation = new int[depth][width];
        randomizeVegetation();
    }

    // Places a dino (and keeps the list in sync)
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

    // Gets whatever is at a location (or null)
    public Dinosaur getDinosaurAt(Location location)
    {
        return field.get(location);
    }

    // Returns adjacent locations that are free (or contain dead dinos)
    public List<Location> getFreeAdjacentLocations(Location location)
    {
        List<Location> free = new LinkedList<>();
        List<Location> adjacent = getAdjacentLocations(location);

        for(Location next : adjacent) {
            Dinosaur d = field.get(next);
            if(d == null) {
                free.add(next);
            }
            else if(!d.isAlive()) {
                free.add(next);
            }
        }

        return free;
    }

    // Returns shuffled adjacent positions (8-neighbour)
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

    // Gets all tiles within a radius (square radius, not circle)
    public List<Location> getLocationsWithinRadius(Location center, int radius)
    {
        List<Location> locs = new ArrayList<>();
        if(center == null) return locs;

        for(int dr = -radius; dr <= radius; dr++) {
            for(int dc = -radius; dc <= radius; dc++) {
                if(dr == 0 && dc == 0) continue;

                int r = center.row() + dr;
                int c = center.col() + dc;

                if(r >= 0 && r < depth && c >= 0 && c < width) {
                    locs.add(new Location(r, c));
                }
            }
        }

        Collections.shuffle(locs, Randomizer.getRandom());
        return locs;
    }

    // Console stats (quick sanity check)
    public void fieldStats()
    {
        int numAllosaurs = 0, numIguanadons = 0;
        int numCarnotaurus = 0, numDilophosaurus = 0;
        int numDiabloceratops = 0, numAnkylosaurus = 0;

        for(Dinosaur d : field.values()) {
            if(d == null || !d.isAlive()) continue;

            if(d instanceof Allosaurus) numAllosaurs++;
            else if(d instanceof Iguanadon) numIguanadons++;
            else if(d instanceof Carnotaurus) numCarnotaurus++;
            else if(d instanceof Dilophosaurus) numDilophosaurus++;
            else if(d instanceof Diabloceratops) numDiabloceratops++;
            else if(d instanceof Ankylosaurus) numAnkylosaurus++;
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

    // Clears all dinos and resets veg
    public void clear()
    {
        field.clear();
        dinosaurs.clear();
        randomizeVegetation();
    }

    // Checks if sim should keep going (needs at least 1 herb + 1 carn)
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

    // Returns the internal list (Simulator uses it)
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

    // Gets vegetation 0..100 at a tile
    public int getVegetationAt(Location location)
    {
        return vegetation[location.row()][location.col()];
    }

    // Takes vegetation from a tile, returns how much we actually managed to eat
    public int consumeVegetationAt(Location loc, int amount)
    {
        int r = loc.row();
        int c = loc.col();

        int available = vegetation[r][c];
        int taken = Math.min(available, Math.max(0, amount));

        vegetation[r][c] = available - taken;
        return taken;
    }

    // Regrow vegetation with time/weather effects
    public void regrowVegetation(TimeOfDay timeOfDay, WeatherState weather)
    {
        double p = 0.25;

        // (day/night amount currently same but I left it as a variable)
        int baseGrow = (timeOfDay == TimeOfDay.NIGHT) ? 1 : 1;

        double mult = WeatherManager.vegetationRegrowMultiplier();
        int cap = WeatherManager.vegetationCap();

        double pScaled = p * mult;
        if(pScaled > 0.85) pScaled = 0.85;

        int grow = baseGrow;
        if(mult >= 1.4) grow = baseGrow + 1;
        if(mult <= 0.60) grow = 0;

        for(int r = 0; r < depth; r++) {
            for(int c = 0; c < width; c++) {

                // heatwave pushes high veg down towards cap
                if(weather == WeatherState.HEATWAVE && vegetation[r][c] > cap) {
                    vegetation[r][c] = Math.max(cap, vegetation[r][c] - 1);
                }

                if(rand.nextDouble() < pScaled) {
                    int v = vegetation[r][c] + grow;
                    if(v > cap) v = cap;
                    vegetation[r][c] = v;
                }

                // tiny recovery even in heatwaves (otherwise it can go dead forever)
                if(weather == WeatherState.HEATWAVE && vegetation[r][c] < cap && rand.nextDouble() < 0.05) {
                    vegetation[r][c] = Math.min(cap, vegetation[r][c] + 1);
                }
            }
        }
    }

    // Copies vegetation grid from old field into new one
    public void copyVegetationFrom(Field other)
    {
        for(int r = 0; r < depth; r++) {
            System.arraycopy(other.vegetation[r], 0, this.vegetation[r], 0, width);
        }
    }

    // Random start veg (so herbivores don't instantly die)
    private void randomizeVegetation()
    {
        for(int r = 0; r < depth; r++) {
            for(int c = 0; c < width; c++) {
                vegetation[r][c] = 60 + rand.nextInt(41);
            }
        }

        // System.out.println("[veg] initialised");
    }
}