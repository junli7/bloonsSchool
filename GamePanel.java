import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage; // Import for BufferedImage
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {
    private List<Monkey> monkeys;
    private List<Human> humans;
    private Monkey selectedMonkey = null;
    private UpgradeControlPanel upgradeControlPanel_ref; 
    private GameState gameState;
    private Map map;
    private Random random = new Random();

    private BufferedImage mapBackgroundSprite;
    private static final String MAP_BACKGROUND_SPRITE_PATH = "map_background.png"; // IMPORTANT: Change to your actual image file name

    private static final int MONEY_PER_POP = 50;

    private boolean isPlacingMonkey = false;
    private String placingMonkeyType = null;
    private Monkey placementPreviewMonkey = null;
    private boolean placementValid = false;
    private int placementMouseX, placementMouseY;

    private static final double MIN_PATH_PLACEMENT_BUFFER = -15; 
    private static final double MONKEY_PLACEMENT_SEPARATION_BUFFER = -10; 

    public GamePanel(GameState gameState, UpgradeControlPanel upgradeControlPanel) {
        this.gameState = gameState;
        this.upgradeControlPanel_ref = upgradeControlPanel; 
        // setBackground(Color.LIGHT_GRAY); // Will be covered by image, but can be a fallback

        // --- Load Map Background Sprite ---
        // We load the original sprite here. It will be scaled in paintComponent.
        this.mapBackgroundSprite = SpriteManager.getSprite(MAP_BACKGROUND_SPRITE_PATH);
        if (this.mapBackgroundSprite == SpriteManager.getPlaceholderSprite()) {
            System.err.println("WARNING: Map background image '" + MAP_BACKGROUND_SPRITE_PATH + "' not found or failed to load. Using default background.");
            setBackground(Color.LIGHT_GRAY); // Set fallback if image fails
        }
        // --- End Load Map Background Sprite ---

        ArrayList<Point> mapCoordinates = new ArrayList<>();
        mapCoordinates.add(new Point(0, 175));
        mapCoordinates.add(new Point(200, 175));
        mapCoordinates.add(new Point(200, 75));
        mapCoordinates.add(new Point(100, 75));
        mapCoordinates.add(new Point(100, 275));
        mapCoordinates.add(new Point(350, 275));
        mapCoordinates.add(new Point(350, 75));
        mapCoordinates.add(new Point(500, 75));
        mapCoordinates.add(new Point(500, 275));
        mapCoordinates.add(new Point(575, 275));
        mapCoordinates.add(new Point(575, 75));
        mapCoordinates.add(new Point(725, 75));
        mapCoordinates.add(new Point(725, 450));
        mapCoordinates.add(new Point(200, 450));
        mapCoordinates.add(new Point(200, 375));
        mapCoordinates.add(new Point(275, 375));
        mapCoordinates.add(new Point(275, 540));
        mapCoordinates.add(new Point(800, 540));
        map = new Map(mapCoordinates);

        monkeys = new ArrayList<>();
        humans = new ArrayList<>();

        initializeWaveDefinitions();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int clickX = e.getX();
                int clickY = e.getY();

                if (upgradeControlPanel_ref != null && upgradeControlPanel_ref.isVisible()) {
                    int ucpX = getWidth() - upgradeControlPanel_ref.getWidth(); 
                    int ucpWidth = upgradeControlPanel_ref.getWidth();
                    if (clickX >= ucpX && clickX < (ucpX + ucpWidth) &&
                        clickY >= 0 && clickY < getHeight()) { 
                        return; 
                    }
                }

                if (isPlacingMonkey) {
                    if (SwingUtilities.isRightMouseButton(e) || e.getButton() == MouseEvent.BUTTON3) {
                        cancelPlacingMonkey();
                        return;
                    }
                    if (e.getButton() == MouseEvent.BUTTON1 && placementPreviewMonkey != null) {
                        if (placementValid) {
                            int cost = 0;
                            Monkey newMonkeyToPlace = null;
                            boolean canAffordPlacement = false;

                            if (placingMonkeyType.equals("Monkey")) {
                                cost = Monkey.COST;
                                if (gameState.canAfford(cost)) {
                                    newMonkeyToPlace = new Monkey(clickX, clickY, 1);
                                    canAffordPlacement = true;
                                }
                            } else if (placingMonkeyType.equals("MonkeyB")) {
                                cost = MonkeyB.COST;
                                if (gameState.canAfford(cost)) {
                                    newMonkeyToPlace = new MonkeyB(clickX, clickY, 1);
                                    canAffordPlacement = true;
                                }
                            } else if (placingMonkeyType.equals("MonkeyC")) {
                                cost = MonkeyC.COST;
                                if (gameState.canAfford(cost)) {
                                    newMonkeyToPlace = new MonkeyC(clickX, clickY, 1);
                                    canAffordPlacement = true;
                                }
                            }

                            if (canAffordPlacement && newMonkeyToPlace != null) {
                                gameState.spendMoney(cost);
                                monkeys.add(newMonkeyToPlace);
                                cancelPlacingMonkey();
                                if (upgradeControlPanel_ref != null) {
                                    upgradeControlPanel_ref.setSelectedMonkey(null);
                                    upgradeControlPanel_ref.setVisible(false);
                                }
                            } else if (!canAffordPlacement){
                                System.out.println("Cannot afford " + placingMonkeyType + ". Cost: " + cost);
                            }
                        } else {
                             System.out.println("Invalid placement for " + placingMonkeyType);
                        }
                        return;
                    }
                }

                boolean aMonkeyWasClickedThisTime = false;
                Monkey currentlyClickedMonkey = null;
                for (Monkey m : monkeys) {
                    if (m.contains(clickX, clickY)) {
                        currentlyClickedMonkey = m;
                        aMonkeyWasClickedThisTime = true;
                        break;
                    }
                }

                if (aMonkeyWasClickedThisTime) {
                    if (selectedMonkey != currentlyClickedMonkey) {
                        if (selectedMonkey != null) selectedMonkey.setSelected(false);
                        selectedMonkey = currentlyClickedMonkey;
                        selectedMonkey.setSelected(true);
                        if (upgradeControlPanel_ref != null) {
                            upgradeControlPanel_ref.setSelectedMonkey(selectedMonkey);
                            upgradeControlPanel_ref.setVisible(true);
                        }
                    } else {
                        if (upgradeControlPanel_ref != null && !upgradeControlPanel_ref.isVisible()) {
                            upgradeControlPanel_ref.setSelectedMonkey(selectedMonkey);
                            upgradeControlPanel_ref.setVisible(true);
                        }
                    }
                } else { 
                    if (selectedMonkey != null) {
                        selectedMonkey.setSelected(false);
                        selectedMonkey = null;
                        if (upgradeControlPanel_ref != null) {
                            upgradeControlPanel_ref.setSelectedMonkey(null);
                            upgradeControlPanel_ref.setVisible(false);
                        }
                    }
                }
                repaint(); 
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                placementMouseX = e.getX();
                placementMouseY = e.getY();
                if (isPlacingMonkey && placementPreviewMonkey != null) {
                    placementPreviewMonkey.setPosition(placementMouseX, placementMouseY);
                    placementValid = isValidPlacement(placementMouseX, placementMouseY,
                            placementPreviewMonkey.getHitbox(), placementPreviewMonkey.getRange());
                     repaint();
                }
            }
        });

        Timer gameLoopTimer = new Timer(16, ev -> {
            updateGame();
            repaint();
        });
        gameLoopTimer.start();
        setFocusable(true); 
    }

    public void sellMonkey(Monkey monkeyToSell) {
        if (monkeyToSell == null || !monkeys.contains(monkeyToSell)) {
            System.err.println("Error: Attempted to sell a monkey that doesn't exist or isn't tracked.");
            return;
        }
        int sellValue = monkeyToSell.getSellValue();
        gameState.addMoney(sellValue);
        monkeys.remove(monkeyToSell);

        System.out.println("Sold " + monkeyToSell.getClass().getSimpleName() + " for $" + sellValue);

        if (this.selectedMonkey == monkeyToSell) {
            this.selectedMonkey.setSelected(false); 
            this.selectedMonkey = null;
            if (upgradeControlPanel_ref != null) {
                upgradeControlPanel_ref.setSelectedMonkey(null);
                upgradeControlPanel_ref.setVisible(false);
            }
        }
        repaint();
    }
    
    private void initializeWaveDefinitions() {
        this.waveDefinitions = new ArrayList<>();
        List<SpawnInstruction> wave1 = new ArrayList<>();
        wave1.add(new SpawnInstruction("baby", 10, 0, 30, false));
        waveDefinitions.add(new WaveDefinition(wave1));
        List<SpawnInstruction> wave2 = new ArrayList<>();
        wave2.add(new SpawnInstruction("baby", 15, 0, 25, false));
        wave2.add(new SpawnInstruction("kid", 5, 60, 40, false));
        waveDefinitions.add(new WaveDefinition(wave2));
        List<SpawnInstruction> wave3 = new ArrayList<>();
        wave3.add(new SpawnInstruction("kid", 10, 0, 30, false));
        wave3.add(new SpawnInstruction("baby", 10, 50, 20, true));
        waveDefinitions.add(new WaveDefinition(wave3));
        List<SpawnInstruction> wave4 = new ArrayList<>();
        wave4.add(new SpawnInstruction("normal", 8, 0, 50, false));
        wave4.add(new SpawnInstruction("kid", 10, 40, 25, false));
        waveDefinitions.add(new WaveDefinition(wave4));
        List<SpawnInstruction> wave5 = new ArrayList<>();
        wave5.add(new SpawnInstruction("businessman", 5, 0, 60, false));
        wave5.add(new SpawnInstruction("normal", 10, 30, 30, true));
        waveDefinitions.add(new WaveDefinition(wave5));
        List<SpawnInstruction> wave6 = new ArrayList<>();
        wave6.add(new SpawnInstruction("bodybuilder", 3, 0, 100, false));
        wave6.add(new SpawnInstruction("businessman", 7, 60, 40, false));
        wave6.add(new SpawnInstruction("kid", 15, 40, 20, true));
        waveDefinitions.add(new WaveDefinition(wave6));
        List<SpawnInstruction> wave7 = new ArrayList<>();
        wave7.add(new SpawnInstruction("normal", 20, 0, 15, false));
        wave7.add(new SpawnInstruction("bodybuilder", 2, 30, 80, true));
        waveDefinitions.add(new WaveDefinition(wave7));
        List<SpawnInstruction> wave8 = new ArrayList<>();
        wave8.add(new SpawnInstruction("businessman", 10, 0, 30, true));
        wave8.add(new SpawnInstruction("kid", 20, 20, 15, true));
        waveDefinitions.add(new WaveDefinition(wave8));
        List<SpawnInstruction> wave9 = new ArrayList<>();
        wave9.add(new SpawnInstruction("bodybuilder", 5, 0, 60, false));
        wave9.add(new SpawnInstruction("bossbaby", 1, 120, 0, false));
        wave9.add(new SpawnInstruction("normal", 15, 30, 20, true));
        waveDefinitions.add(new WaveDefinition(wave9));
        List<SpawnInstruction> wave10 = new ArrayList<>();
        wave10.add(new SpawnInstruction("bossbaby", 2, 0, 180, true));
        wave10.add(new SpawnInstruction("bodybuilder", 8, 60, 40, true));
        wave10.add(new SpawnInstruction("businessman", 10, 30, 25, true));
        waveDefinitions.add(new WaveDefinition(wave10));
    }

    public boolean canStartNextWave() {
        return !waveSpawningPhaseActive && humans.isEmpty();
    }

    public void startNextWave() {
        if (!canStartNextWave() && gameState.getCurrentWave() > 0) {
            System.out.println("Cannot start wave " + (gameState.getCurrentWave() +1) + " yet.");
            return;
        }
        gameState.incrementWave();
        int waveToStart = gameState.getCurrentWave();
        System.out.println("Starting wave " + waveToStart);
        currentWaveSpawnInstructionIndex = 0;
        unitsLeftToSpawnInCurrentInstruction = 0;
        currentSpawnCooldownTicks = 0;
        waveInProgress = true;
        waveSpawningPhaseActive = true;
        if (waveToStart > waveDefinitions.size()) {
            if (lastProceduralWaveGenerated != waveToStart || currentProceduralWaveDefinition == null) {
                generateProceduralWave(waveToStart);
                lastProceduralWaveGenerated = waveToStart;
            }
        }
        loadNextSpawnInstruction();
    }
    private WaveDefinition currentProceduralWaveDefinition;
    private int lastProceduralWaveGenerated = 0;

    private WaveDefinition getCurrentActiveWaveDefinition() {
        int currentWaveNum = gameState.getCurrentWave();
        if (currentWaveNum <= 0) return null;
        if (currentWaveNum <= waveDefinitions.size()) {
            return waveDefinitions.get(currentWaveNum - 1);
        } else {
            return currentProceduralWaveDefinition;
        }
    }
    
    private int currentWaveSpawnInstructionIndex; 
    private int unitsLeftToSpawnInCurrentInstruction;
    private int currentSpawnCooldownTicks;      
    private boolean waveInProgress = false;
    private boolean waveSpawningPhaseActive = false;

    private void loadNextSpawnInstruction() {
        WaveDefinition activeWaveDef = getCurrentActiveWaveDefinition();
        if (activeWaveDef == null || currentWaveSpawnInstructionIndex >= activeWaveDef.spawns.size()) {
            waveSpawningPhaseActive = false;
            System.out.println("All spawn instructions for wave " + gameState.getCurrentWave() + " completed.");
            return;
        }
        SpawnInstruction instruction = activeWaveDef.spawns.get(currentWaveSpawnInstructionIndex);
        unitsLeftToSpawnInCurrentInstruction = instruction.count;
        currentSpawnCooldownTicks = instruction.delayTicksAfterPreviousGroup;
    }

    private void updateWaveSpawning() {
        if (!waveSpawningPhaseActive) {
            if (waveInProgress && humans.isEmpty()) {
                System.out.println("Wave " + gameState.getCurrentWave() + " cleared of humans.");
                waveInProgress = false;
            }
            return;
        }
        if (currentSpawnCooldownTicks > 0) {
            currentSpawnCooldownTicks--;
            return;
        }
        WaveDefinition activeWaveDef = getCurrentActiveWaveDefinition();
        if (activeWaveDef == null) {
             waveSpawningPhaseActive = false; return;
        }
        if (unitsLeftToSpawnInCurrentInstruction <= 0) {
            currentWaveSpawnInstructionIndex++;
            loadNextSpawnInstruction();
            if(!waveSpawningPhaseActive || currentSpawnCooldownTicks > 0) return;
        }
        if (currentWaveSpawnInstructionIndex < activeWaveDef.spawns.size()) {
            SpawnInstruction currentInstruction = activeWaveDef.spawns.get(currentWaveSpawnInstructionIndex);
            if (map != null && unitsLeftToSpawnInCurrentInstruction > 0) {
                Human newHuman = map.spawnHumanByType(currentInstruction.humanType, currentInstruction.isCamo);
                if (newHuman != null) {
                    humans.add(newHuman);
                }
                unitsLeftToSpawnInCurrentInstruction--;
                if (unitsLeftToSpawnInCurrentInstruction > 0) {
                    currentSpawnCooldownTicks = currentInstruction.intervalTicksPerUnit;
                } else {
                    currentSpawnCooldownTicks = 0;
                }
            }
        } else {
            waveSpawningPhaseActive = false;
        }
    }

    private void generateProceduralWave(int waveNum) {
        List<SpawnInstruction> spawns = new ArrayList<>();
        int difficultyFactor = waveNum - waveDefinitions.size();
        int numGroups = Math.min(8, 2 + difficultyFactor + random.nextInt(2));
        for (int i = 0; i < numGroups; i++) {
            String type;
            int typeRoll = random.nextInt(100) + difficultyFactor * 2;
            if (typeRoll > 100 && difficultyFactor > 1) type = "bossbaby";
            else if (typeRoll > 85) type = "bodybuilder";
            else if (typeRoll > 70) type = "businessman";
            else if (typeRoll > 50) type = "normal";
            else if (typeRoll > 25) type = "kid";
            else type = "baby";
            if (type.equals("bossbaby") && (difficultyFactor < 3 || random.nextInt(difficultyFactor) < 2) && i < numGroups / 2) {
                type = "bodybuilder";
            }
            int count = 1 + random.nextInt(2 + difficultyFactor / 2) + difficultyFactor / 3;
            count = Math.min(type.equals("bossbaby") ? (2 + difficultyFactor/3) : 15, count);
            int delayAfterPrevious = Math.max(10, 100 - difficultyFactor * 8);
            int intervalPerUnit = Math.max(5, 35 - difficultyFactor * 2);
            boolean camo = random.nextDouble() < (0.15 + difficultyFactor * 0.07);
            spawns.add(new SpawnInstruction(type, count, i == 0 ? 0 : delayAfterPrevious, intervalPerUnit, camo));
        }
        this.currentProceduralWaveDefinition = new WaveDefinition(spawns);
        System.out.println("Generated procedural wave " + waveNum + " with " + spawns.size() + " groups.");
    }

    private void updateGame() {
        if (waveSpawningPhaseActive || waveInProgress) {
            updateWaveSpawning();
        }
        for (Monkey m : monkeys) {
            m.updateAndTarget(getWidth(), getHeight(), humans, map.getPath());
        }
        Iterator<Human> humanIterator = humans.iterator();
        while (humanIterator.hasNext()) {
            Human h = humanIterator.next();
            h.update(map.getPath());
            if (!h.isAlive()) {
                gameState.incrementBloonsKilled(1);
                gameState.addMoney(MONEY_PER_POP + (h.isCamo() ? 2 : 0) + (h.getType().equals("bossbaby") ? 50 : 0));
                humanIterator.remove();
            } else if (h.hasReachedEnd()) {
                System.out.println("Human of type " + h.getType() + " reached end! Lives would be lost.");
                humanIterator.remove();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // This is important if using setBackground as a fallback
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- Draw Map Background Sprite FIRST ---
        if (mapBackgroundSprite != null && mapBackgroundSprite != SpriteManager.getPlaceholderSprite()) {
            // Draw the image scaled to fit the panel's current size
            g2d.drawImage(mapBackgroundSprite, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Fallback: if image didn't load, super.paintComponent already painted the setBackground color.
            // Or, you could draw a solid color here explicitly if super wasn't called or did something else.
        }
        // --- End Draw Map Background Sprite ---

        if (map != null) map.draw(g2d); // Path is drawn ON TOP of the background image

        List<Human> humansToDraw = new ArrayList<>(humans);
        for (Human h : humansToDraw) h.draw(g2d);

        List<Monkey> monkeysToDraw = new ArrayList<>(monkeys);
        for (Monkey m : monkeysToDraw) {
            m.draw(g2d); 
            
        }

        if (isPlacingMonkey && placementPreviewMonkey != null) {
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
            g2dCopy.setComposite(ac);
            placementPreviewMonkey.draw(g2dCopy);
            g2dCopy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            Color validityColorFill = placementValid ? new Color(0, 255, 0, 50) : new Color(255, 0, 0, 50);
            Color validityColorBorder = placementValid ? new Color(0, 200, 0, 150) : new Color(200, 0, 0, 150);
            int previewRange = (int) placementPreviewMonkey.getRange();
            g2dCopy.setColor(validityColorFill);
            g2dCopy.fillOval(placementPreviewMonkey.getX() - previewRange, placementPreviewMonkey.getY() - previewRange,
                         previewRange * 2, previewRange * 2);
            g2dCopy.setColor(validityColorBorder);
            g2dCopy.drawOval(placementPreviewMonkey.getX() - previewRange, placementPreviewMonkey.getY() - previewRange,
                         previewRange * 2, previewRange * 2);
            int previewHitbox = (int) placementPreviewMonkey.getHitbox();
            Color hitboxOutlineColor = placementValid ? new Color(0,150,0, 200) : new Color(150,0,0, 200);
            g2dCopy.setColor(hitboxOutlineColor);
            g2dCopy.drawOval(placementPreviewMonkey.getX() - previewHitbox/2, placementPreviewMonkey.getY() - previewHitbox/2,
                         previewHitbox, previewHitbox);
            g2dCopy.dispose();
        }
    }
    
    public void startPlacingMonkey(String type) {
        if (this.selectedMonkey != null) {
            this.selectedMonkey.setSelected(false);
            this.selectedMonkey = null;
             if (upgradeControlPanel_ref != null) {
                upgradeControlPanel_ref.setSelectedMonkey(null);
                upgradeControlPanel_ref.setVisible(false);
            }
        }
        this.isPlacingMonkey = true;
        this.placingMonkeyType = type;
        this.placementPreviewMonkey = null;
        int costToCheck = 0;
        if (type.equals("Monkey")) costToCheck = Monkey.COST;
        else if (type.equals("MonkeyB")) costToCheck = MonkeyB.COST;
        else if (type.equals("MonkeyC")) costToCheck = MonkeyC.COST;
        if (!gameState.canAfford(costToCheck)) {
             System.out.println("Cannot afford " + type + ". Cost: " + costToCheck);
             cancelPlacingMonkey();
             return;
        }
        switch (type) {
            case "Monkey":
                placementPreviewMonkey = new Monkey(placementMouseX, placementMouseY, 1);
                break;
            case "MonkeyB":
                placementPreviewMonkey = new MonkeyB(placementMouseX, placementMouseY, 1);
                break;
            case "MonkeyC":
                placementPreviewMonkey = new MonkeyC(placementMouseX, placementMouseY, 1);
                break;
            default:
                cancelPlacingMonkey();
                return;
        }
        if (placementPreviewMonkey != null) {
             placementPreviewMonkey.setPosition(placementMouseX, placementMouseY);
             placementValid = isValidPlacement(placementMouseX, placementMouseY,
                                           placementPreviewMonkey.getHitbox(), placementPreviewMonkey.getRange());
        }
        repaint();
    }

    public void cancelPlacingMonkey() {
        isPlacingMonkey = false;
        placingMonkeyType = null;
        placementPreviewMonkey = null;
        placementValid = false;
        repaint(); 
    }

    private boolean isValidPlacement(int x, int y, double newMonkeyHitbox, double newMonkeyRange) {
        double newMonkeyRadius = newMonkeyHitbox / 2.0;
        if (x - newMonkeyRadius < 0 || x + newMonkeyRadius > getWidth() ||
            y - newMonkeyRadius < 0 || y + newMonkeyRadius > getHeight()) {
            return false;
        }
        for (Monkey m : monkeys) {
            double existingMonkeyRadius = m.getHitbox() / 2.0;
            double distanceBetweenCenters = Math.sqrt(Math.pow(x - m.getX(), 2) + Math.pow(y - m.getY(), 2));
            double minimumCenterDistance = newMonkeyRadius + existingMonkeyRadius + MONKEY_PLACEMENT_SEPARATION_BUFFER;
            if (distanceBetweenCenters < minimumCenterDistance) {
                return false;
            }
        }
        if (map != null && map.getPath() != null && !map.getPath().isEmpty()) {
            ArrayList<Point> pathPoints = map.getPath();
            float pathVisualThickness = 20f; 
            double minDistanceToPathCenterline = newMonkeyRadius + (pathVisualThickness / 2.0) + MIN_PATH_PLACEMENT_BUFFER;
            for (int i = 0; i < pathPoints.size() - 1; i++) {
                Point p1 = pathPoints.get(i);
                Point p2 = pathPoints.get(i + 1);
                java.awt.geom.Line2D.Double pathSegment = new java.awt.geom.Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                if (pathSegment.ptSegDist(x, y) < minDistanceToPathCenterline) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class SpawnInstruction {
        String humanType;
        int count;
        int delayTicksAfterPreviousGroup;
        int intervalTicksPerUnit;
        boolean isCamo;
        public SpawnInstruction(String ht, int c, int d, int i, boolean camo) {
            humanType=ht; count=c; delayTicksAfterPreviousGroup=d; intervalTicksPerUnit=i; this.isCamo=camo;
        }
    }
    private static class WaveDefinition {
        List<SpawnInstruction> spawns;
        public WaveDefinition(List<SpawnInstruction> s) {spawns=s;}
    }
    private List<WaveDefinition> waveDefinitions;
}