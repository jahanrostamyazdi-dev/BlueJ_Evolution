import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/*
 * Tuning window for changing values while the sim runs.
 * Has a "global" tab and one tab per species.
 * This started as a quick UI and then grew a bit lol.
 */
public class TuningWindow extends JFrame
{
    private final Simulator simulator;

    private final JButton runBtn = new JButton("Run");
    private final JButton pauseBtn = new JButton("Pause");
    private final JButton stepBtn = new JButton("Step");
    private final JButton resetBtn = new JButton("Reset");
    private final JButton applyBtn = new JButton("Apply");

    private final JTabbedPane tabs = new JTabbedPane();

    private JSpinner delayMs;
    private JSpinner vegInitMin, vegInitMax, vegRegrowChance, vegGrowDay, vegGrowNight;
    private JSpinner seedInf, outbreakChance;
    private JSpinner infMinDur, infMaxDur, adjSpreadChance, predEatInfChance, extraDrain, surviveThresh, immuneDur;
    private JSpinner wClearSpinner, wRainSpinner, wFogSpinner, wHeatSpinner;
    private JSpinner pAllo, pCarno, pDilo, pIgu, pDiablo, pAnky;

    // Stores per-species "apply" hooks (one per tab basically)
    private final java.util.Map<SpeciesType, Consumer<SpeciesTuning>> speciesApply =
            new java.util.EnumMap<>(SpeciesType.class);

    public TuningWindow(Simulator simulator)
    {
        this.simulator = simulator;

        setTitle("Simulation Tuning");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        tabs.addTab("Global", buildGlobalPanel());
        tabs.addTab("Allosaurus", buildSpeciesPanel(SpeciesType.ALLOSAURUS));
        tabs.addTab("Carnotaurus", buildSpeciesPanel(SpeciesType.CARNOTAURUS));
        tabs.addTab("Dilophosaurus", buildSpeciesPanel(SpeciesType.DILOPHOSAURUS));
        tabs.addTab("Iguanadon", buildSpeciesPanel(SpeciesType.IGUANADON));
        tabs.addTab("Diabloceratops", buildSpeciesPanel(SpeciesType.DIABLOCERATOPS));
        tabs.addTab("Ankylosaurus", buildSpeciesPanel(SpeciesType.ANKYLOSAURUS));

        add(tabs, BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        pack();
        setLocation(140, 80);
        setVisible(true);

        wireButtons();
    }

    // Bottom button bar
    private JPanel buildBottomBar()
    {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        p.add(applyBtn);
        p.add(resetBtn);
        p.add(stepBtn);
        p.add(runBtn);
        p.add(pauseBtn);
        return p;
    }

    // Hooks up button actions
    private void wireButtons()
    {
        applyBtn.addActionListener(e -> applyAll());

        resetBtn.addActionListener(e -> {
            simulator.stopContinuous();
            simulator.reset();
        });

        stepBtn.addActionListener(e -> simulator.simulateOneStep());
        runBtn.addActionListener(e -> simulator.startContinuous());
        pauseBtn.addActionListener(e -> simulator.stopContinuous());
    }

    // Builds the global tab
    private JPanel buildGlobalPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = baseGC();

        delayMs = spinnerInt(Tuning.simDelayMs, 0, 500, 5);

        vegInitMin = spinnerInt(Tuning.vegInitialMin, 0, 100, 1);
        vegInitMax = spinnerInt(Tuning.vegInitialMax, 0, 100, 1);

        vegRegrowChance = spinnerDouble(Tuning.vegRegrowChance, 0.0, 1.0, 0.01);
        vegGrowDay = spinnerInt(Tuning.vegRegrowAmountDay, 0, 10, 1);
        vegGrowNight = spinnerInt(Tuning.vegRegrowAmountNight, 0, 10, 1);

        seedInf = spinnerInt(Tuning.initialInfections, 0, 200, 1);
        outbreakChance = spinnerDouble(Tuning.spontaneousOutbreakChance, 0.0, 0.05, 0.0005);

        infMinDur = spinnerInt(Tuning.infectionMinDuration, 0, 200, 1);
        infMaxDur = spinnerInt(Tuning.infectionMaxDuration, 0, 200, 1);
        adjSpreadChance = spinnerDouble(Tuning.adjacentSpreadChance, 0.0, 1.0, 0.01);
        predEatInfChance = spinnerDouble(Tuning.predatorEatInfectedChance, 0.0, 1.0, 0.01);
        extraDrain = spinnerInt(Tuning.extraInfectedEnergyLoss, 0, 10, 1);
        surviveThresh = spinnerInt(Tuning.surviveEnergyThreshold, 0, 50, 1);
        immuneDur = spinnerInt(Tuning.immunityDuration, 0, 200, 1);

        wClearSpinner = spinnerDouble(Tuning.wClear, 0.0, 1.0, 0.01);
        wRainSpinner = spinnerDouble(Tuning.wRain, 0.0, 1.0, 0.01);
        wFogSpinner = spinnerDouble(Tuning.wFog, 0.0, 1.0, 0.01);
        wHeatSpinner = spinnerDouble(Tuning.wHeat, 0.0, 1.0, 0.01);

        pAllo = spinnerDouble(Tuning.pAllosaurus, 0.0, 0.20, 0.001);
        pCarno = spinnerDouble(Tuning.pCarnotaurus, 0.0, 0.20, 0.001);
        pDilo = spinnerDouble(Tuning.pDilophosaurus, 0.0, 0.20, 0.001);

        pIgu = spinnerDouble(Tuning.pIguanadon, 0.0, 0.40, 0.001);
        pDiablo = spinnerDouble(Tuning.pDiabloceratops, 0.0, 0.40, 0.001);
        pAnky = spinnerDouble(Tuning.pAnkylosaurus, 0.0, 0.40, 0.001);

        int r = 0;
        addRow(panel, gc, r++, "Sim delay (ms)", delayMs);

        addRow(panel, gc, r++, "Veg init min", vegInitMin);
        addRow(panel, gc, r++, "Veg init max", vegInitMax);
        addRow(panel, gc, r++, "Veg regrow chance", vegRegrowChance);
        addRow(panel, gc, r++, "Veg grow day", vegGrowDay);
        addRow(panel, gc, r++, "Veg grow night", vegGrowNight);

        addRow(panel, gc, r++, "Initial infections", seedInf);
        addRow(panel, gc, r++, "Spontaneous outbreak chance", outbreakChance);
        addRow(panel, gc, r++, "Infection min duration", infMinDur);
        addRow(panel, gc, r++, "Infection max duration", infMaxDur);
        addRow(panel, gc, r++, "Adjacent spread chance", adjSpreadChance);
        addRow(panel, gc, r++, "Predator eat infected chance", predEatInfChance);
        addRow(panel, gc, r++, "Extra energy drain (infected)", extraDrain);
        addRow(panel, gc, r++, "Survive energy threshold", surviveThresh);
        addRow(panel, gc, r++, "Immunity duration", immuneDur);

        addSeparator(panel, gc, r++);
        addRow(panel, gc, r++, "Weather: Clear weight", wClearSpinner);
        addRow(panel, gc, r++, "Weather: Rain weight", wRainSpinner);
        addRow(panel, gc, r++, "Weather: Fog weight", wFogSpinner);
        addRow(panel, gc, r++, "Weather: Heatwave weight", wHeatSpinner);

        addSeparator(panel, gc, r++);
        addRow(panel, gc, r++, "Spawn p(Allosaurus)", pAllo);
        addRow(panel, gc, r++, "Spawn p(Carnotaurus)", pCarno);
        addRow(panel, gc, r++, "Spawn p(Dilophosaurus)", pDilo);
        addRow(panel, gc, r++, "Spawn p(Iguanadon)", pIgu);
        addRow(panel, gc, r++, "Spawn p(Diabloceratops)", pDiablo);
        addRow(panel, gc, r++, "Spawn p(Ankylosaurus)", pAnky);

        return panel;
    }

    // Builds one species tab and registers an apply hook for that species
    private JPanel buildSpeciesPanel(SpeciesType type)
    {
        SpeciesTuning t = Tuning.get(type);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = baseGC();
        int r = 0;

        JSpinner maxEnergy = spinnerInt(t.maxEnergy, 1, 200, 1);
        JSpinner stepLoss = spinnerInt(t.stepEnergyLoss, 0, 10, 1);
        JSpinner maxAge = spinnerInt(t.maxAge, 1, 1000, 1);

        JSpinner breedingAge = spinnerInt(t.breedingAge, 0, 200, 1);
        JSpinner breedProb = spinnerDouble(t.breedingProbability, 0.0, 1.0, 0.01);
        JSpinner maxLitter = spinnerInt(t.maxLitterSize, 0, 20, 1);
        JSpinner breedEnergy = spinnerInt(t.breedingEnergyThreshold, 0, 200, 1);
        JSpinner babyCost = spinnerInt(t.energyCostPerBaby, 0, 50, 1);

        addRow(panel, gc, r++, "Max energy", maxEnergy);
        addRow(panel, gc, r++, "Energy loss/step", stepLoss);
        addRow(panel, gc, r++, "Max age", maxAge);

        addSeparator(panel, gc, r++);
        addRow(panel, gc, r++, "Breeding age", breedingAge);
        addRow(panel, gc, r++, "Breeding probability", breedProb);
        addRow(panel, gc, r++, "Max litter size", maxLitter);
        addRow(panel, gc, r++, "Breed energy threshold", breedEnergy);
        addRow(panel, gc, r++, "Energy cost/baby", babyCost);

        boolean carn = (type == SpeciesType.ALLOSAURUS || type == SpeciesType.CARNOTAURUS || type == SpeciesType.DILOPHOSAURUS);

        if(carn) {
            addSeparator(panel, gc, r++);

            JSpinner attack = spinnerInt(t.attack, 0, 50, 1);
            JSpinner baseKill = spinnerDouble(t.baseKillChance, 0.0, 1.0, 0.01);
            JSpinner dayMod = spinnerDouble(t.dayKillMod, 0.0, 2.0, 0.01);
            JSpinner nightMod = spinnerDouble(t.nightKillMod, 0.0, 2.0, 0.01);

            addRow(panel, gc, r++, "Attack", attack);
            addRow(panel, gc, r++, "Base kill chance", baseKill);
            addRow(panel, gc, r++, "Day kill modifier", dayMod);
            addRow(panel, gc, r++, "Night kill modifier", nightMod);

            JCheckBox nightOnly = new JCheckBox("Hunt only at night", t.huntOnlyAtNight);
            gc.gridx = 0; gc.gridy = r; gc.gridwidth = 2;
            panel.add(nightOnly, gc);
            r++;

            attachApplyHook(type, s -> {
                s.maxAge = (int) maxAge.getValue();
                s.maxEnergy = (int) maxEnergy.getValue();
                s.stepEnergyLoss = (int) stepLoss.getValue();
                s.breedingAge = (int) breedingAge.getValue();
                s.breedingProbability = (double) breedProb.getValue();
                s.maxLitterSize = (int) maxLitter.getValue();
                s.breedingEnergyThreshold = (int) breedEnergy.getValue();
                s.energyCostPerBaby = (int) babyCost.getValue();

                s.attack = (int) attack.getValue();
                s.baseKillChance = (double) baseKill.getValue();
                s.dayKillMod = (double) dayMod.getValue();
                s.nightKillMod = (double) nightMod.getValue();
                s.huntOnlyAtNight = nightOnly.isSelected();
            });
        }
        else {
            addSeparator(panel, gc, r++);

            JSpinner defence = spinnerInt(t.defence, 0, 50, 1);
            JSpinner biteSize = spinnerInt(t.biteSize, 0, 200, 1);
            JSpinner energyPerVeg = spinnerInt(t.energyPerVeg, 1, 50, 1);
            JSpinner minVegBreed = spinnerInt(t.minVegToBreed, 0, 100, 1);

            JCheckBox heavy = new JCheckBox("Heavy (slow in rain)", t.heavy);
            JSpinner rainSkip = spinnerDouble(t.rainMoveSkipChance, 0.0, 1.0, 0.05);

            addRow(panel, gc, r++, "Defence", defence);
            addRow(panel, gc, r++, "Bite size", biteSize);
            addRow(panel, gc, r++, "Energy per veg", energyPerVeg);
            addRow(panel, gc, r++, "Min veg to breed", minVegBreed);

            gc.gridx = 0; gc.gridy = r; gc.gridwidth = 2;
            panel.add(heavy, gc);
            r++;
            addRow(panel, gc, r++, "Rain move skip chance", rainSkip);

            attachApplyHook(type, s -> {
                s.maxAge = (int) maxAge.getValue();
                s.maxEnergy = (int) maxEnergy.getValue();
                s.stepEnergyLoss = (int) stepLoss.getValue();
                s.breedingAge = (int) breedingAge.getValue();
                s.breedingProbability = (double) breedProb.getValue();
                s.maxLitterSize = (int) maxLitter.getValue();
                s.breedingEnergyThreshold = (int) breedEnergy.getValue();
                s.energyCostPerBaby = (int) babyCost.getValue();

                s.defence = (int) defence.getValue();
                s.biteSize = (int) biteSize.getValue();
                s.energyPerVeg = (int) energyPerVeg.getValue();
                s.minVegToBreed = (int) minVegBreed.getValue();

                s.heavy = heavy.isSelected();
                s.rainMoveSkipChance = (double) rainSkip.getValue();
            });
        }

        return panel;
    }

    // Registers a hook that runs when Apply is pressed
    private void attachApplyHook(SpeciesType type, Consumer<SpeciesTuning> hook)
    {
        speciesApply.put(type, hook);
    }

    // Copies UI values back into Tuning
    private void applyAll()
    {
        Tuning.simDelayMs = (int) delayMs.getValue();

        Tuning.vegInitialMin = (int) vegInitMin.getValue();
        Tuning.vegInitialMax = (int) vegInitMax.getValue();
        Tuning.vegRegrowChance = (double) vegRegrowChance.getValue();
        Tuning.vegRegrowAmountDay = (int) vegGrowDay.getValue();
        Tuning.vegRegrowAmountNight = (int) vegGrowNight.getValue();

        Tuning.initialInfections = (int) seedInf.getValue();
        Tuning.spontaneousOutbreakChance = (double) outbreakChance.getValue();
        Tuning.infectionMinDuration = (int) infMinDur.getValue();
        Tuning.infectionMaxDuration = (int) infMaxDur.getValue();
        Tuning.adjacentSpreadChance = (double) adjSpreadChance.getValue();
        Tuning.predatorEatInfectedChance = (double) predEatInfChance.getValue();
        Tuning.extraInfectedEnergyLoss = (int) extraDrain.getValue();
        Tuning.surviveEnergyThreshold = (int) surviveThresh.getValue();
        Tuning.immunityDuration = (int) immuneDur.getValue();

        Tuning.wClear = (double) wClearSpinner.getValue();
        Tuning.wRain = (double) wRainSpinner.getValue();
        Tuning.wFog = (double) wFogSpinner.getValue();
        Tuning.wHeat = (double) wHeatSpinner.getValue();

        Tuning.pAllosaurus = (double) pAllo.getValue();
        Tuning.pCarnotaurus = (double) pCarno.getValue();
        Tuning.pDilophosaurus = (double) pDilo.getValue();

        Tuning.pIguanadon = (double) pIgu.getValue();
        Tuning.pDiabloceratops = (double) pDiablo.getValue();
        Tuning.pAnkylosaurus = (double) pAnky.getValue();

        for(SpeciesType st : SpeciesType.values()) {
            Consumer<SpeciesTuning> hook = speciesApply.get(st);
            if(hook != null) hook.accept(Tuning.get(st));
        }

        // System.out.println("[tuning] applied");
    }

    // GridBag default settings (makes layout easier)
    private GridBagConstraints baseGC()
    {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 8, 4, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        return gc;
    }

    // Adds a label + component row
    private void addRow(JPanel panel, GridBagConstraints gc, int row, String label, JComponent comp)
    {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1;
        panel.add(new JLabel(label), gc);

        gc.gridx = 1; gc.gridy = row;
        panel.add(comp, gc);
    }

    // Adds a horizontal separator
    private void addSeparator(JPanel panel, GridBagConstraints gc, int row)
    {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        panel.add(sep, gc);
    }

    // Int spinner helper
    private JSpinner spinnerInt(int value, int min, int max, int step)
    {
        return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }

    // Double spinner helper
    private JSpinner spinnerDouble(double value, double min, double max, double step)
    {
        return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }
}