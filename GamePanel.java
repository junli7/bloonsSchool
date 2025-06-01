import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel{
    public static final String defaultMonkey = "Monkey";
    public static final String bomberMonkey = "MonkeyB";
    public static final String slowMonkey = "MonkeyC";

    private List<Monkey> monkeys;
    private List<Human> humans;
    private Monkey selectedMonkey = null;
    private final UpgradeControlPanel upgradeControlPanelRef;
    private final GameState gameState;
    private final Map map;
    private final Random random = new Random();

    private BufferedImage mapBackgroundSprite;
    private static final String mapSprite = "map_background.png";

    private List<WaveDefinition> waveDefinitions;

    private boolean isPlacingMonkey = false;
    private String placingMonkeyType = null;
    private Monkey placementPreviewMonkey = null;
    private boolean placementValid = false;
    private int placementMouseX, placementMouseY;

    private static final double separationBuffer = -15;

    private boolean gameIsOver = false;
    private Rectangle restartButtonBounds;
    private final Color restartButtonColor = new Color(0, 150, 0);
    private final Color restartButtonHoverColor = new Color(0, 200, 0);
    private boolean hoverOnRestartButton = false;


    public GamePanel(GameState gameState, UpgradeControlPanel upgradeControlPanel){
        this.gameState = gameState;
        this.upgradeControlPanelRef = upgradeControlPanel;

        this.mapBackgroundSprite = SpriteManager.getSprite(mapSprite);

        ArrayList<Point> mapCoordinates = new ArrayList<>();
        mapCoordinates.add(new Point(0, 175));
        mapCoordinates.add(new Point(200, 175));
        mapCoordinates.add(new Point(200, 75));
        mapCoordinates.add(new Point(100, 75));
        mapCoordinates.add(new Point(100, 275));
        mapCoordinates.add(new Point(350, 275));
        mapCoordinates.add(new Point(350, 175));
        mapCoordinates.add(new Point(725, 175));
        mapCoordinates.add(new Point(725, 350));
        mapCoordinates.add(new Point(525, 350));
        mapCoordinates.add(new Point(525, 450));
        mapCoordinates.add(new Point(200, 450));
        mapCoordinates.add(new Point(200, 375));
        mapCoordinates.add(new Point(275, 375));
        mapCoordinates.add(new Point(275, 540));
        mapCoordinates.add(new Point(800, 540));
        map = new Map(mapCoordinates);

        monkeys = new ArrayList<>();
        humans = new ArrayList<>();

        initializeWaveDefinitions();
        restartButtonBounds = new Rectangle(0, 0, 0, 0);

        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent x){
                handleMousePress(x);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseMoved(MouseEvent x){
                handleMouseMotion(x);
            }
        });


        Timer gameLoopTimer = new Timer(16, new ActionListener(){ // like 60 FPS
            public void actionPerformed(ActionEvent a){
                updateGame();
                repaint();
            }
        });
        gameLoopTimer.start();
    }

    private void handleMousePress(MouseEvent b){
        if (gameIsOver){
            if (restartButtonBounds.contains(b.getX(), b.getY())){
                restartGame();
            }
            return;
        }

        int clickX = b.getX();
        int clickY = b.getY();

        if (isPlacingMonkey){
            handlePlacingMonkeyMousePress(b);
        } else{
            handleMonkeySelectionMousePress(clickX, clickY);
        }
    }

    

    private void handlePlacingMonkeyMousePress(MouseEvent b){
        if (SwingUtilities.isRightMouseButton(b)){
            cancelPlacingMonkey();
        }

        if (SwingUtilities.isLeftMouseButton(b) && placementPreviewMonkey != null){
            if (placementValid){
                int cost = getMonkeyCost(placingMonkeyType);
                if (gameState.canAfford(cost)){
                    Monkey newMonkeyToPlace = createMonkey(placingMonkeyType, b.getX(), b.getY(), 1);
                    if (newMonkeyToPlace != null){
                        gameState.spendMoney(cost);
                        monkeys.add(newMonkeyToPlace);
                        cancelPlacingMonkey();
                    }
                } 
            }
        }
    }

    private void handleMonkeySelectionMousePress(int clickX, int clickY){
        Monkey clickedMonkey = null;
        for (Monkey m : monkeys){
            if (m.contains(clickX, clickY)){
                clickedMonkey = m;
                break;
            }
        }
        setSelectedMonkey(clickedMonkey);
    }

    private void handleMouseMotion(MouseEvent e){
        if (gameIsOver){
            hoverOnRestartButton = restartButtonBounds.contains(e.getX(), e.getY());
            repaint();
        }
        placementMouseX = e.getX();
        placementMouseY = e.getY();
        if (isPlacingMonkey && placementPreviewMonkey != null){
            placementPreviewMonkey.setPosition(placementMouseX, placementMouseY);
            placementValid = isValidPlacement(placementMouseX, placementMouseY,placementPreviewMonkey.getHitbox(), placementPreviewMonkey.getRange());
            repaint();
        }
    }

    private void restartGame(){
        gameState.reset();
        monkeys.clear();
        humans.clear();

        setSelectedMonkey(null);

        isPlacingMonkey = false;
        placementPreviewMonkey = null;
        placingMonkeyType = null;

        waveInProgress = false;
        waveSpawningPhaseActive = false;
        currentWaveSpawnInstructionIndex = 0;
        unitsLeftToSpawnInCurrentInstruction = 0;
        currentSpawnCooldownTicks = 0;
        lastProceduralWaveGenerated = 0;
        currentProceduralWaveDefinition = null;

        gameIsOver = false;
        hoverOnRestartButton = false;
        repaint();
    }

    public void sellMonkey(Monkey monkeyToSell){

        int sellValue = monkeyToSell.getSellValue();
        gameState.addMoney(sellValue);
        monkeys.remove(monkeyToSell);
        if (this.selectedMonkey == monkeyToSell){
            setSelectedMonkey(null);
        }
        repaint();
    }

    private void setSelectedMonkey(Monkey monkey){
        boolean selectionChanged = this.selectedMonkey != monkey;
        boolean repaintNeeded = false;

        if (this.selectedMonkey == monkey && monkey != null &&
            upgradeControlPanelRef != null && !upgradeControlPanelRef.isVisible()){
            if (!this.selectedMonkey.isSelected()){
                this.selectedMonkey.setSelected(true);
                repaintNeeded = true;
            }
            upgradeControlPanelRef.setSelectedMonkey(this.selectedMonkey);
            upgradeControlPanelRef.setVisible(true);
            if (repaintNeeded) repaint();
        }

        if (selectionChanged){
            if (this.selectedMonkey != null){
                this.selectedMonkey.setSelected(false);
                repaintNeeded = true;
            }
            this.selectedMonkey = monkey;
            if (this.selectedMonkey != null){
                this.selectedMonkey.setSelected(true);
                repaintNeeded = true;
            }

            if (upgradeControlPanelRef != null){
                upgradeControlPanelRef.setSelectedMonkey(this.selectedMonkey);
                upgradeControlPanelRef.setVisible(this.selectedMonkey != null);
            }
            if (repaintNeeded) repaint();
        }
    }


    public void startPlacingMonkey(String type){
        if (gameIsOver) return;

        setSelectedMonkey(null);

        int costToCheck = getMonkeyCost(type);
        
        if (!gameState.canAfford(costToCheck)){
             return;
        }

        this.isPlacingMonkey = true;
        this.placingMonkeyType = type;
        this.placementPreviewMonkey = createMonkey(type, placementMouseX, placementMouseY, 1);

        if (this.placementPreviewMonkey != null){
             placementPreviewMonkey.setPosition(placementMouseX, placementMouseY);
             placementValid = isValidPlacement(placementMouseX, placementMouseY,
                                               placementPreviewMonkey.getHitbox(),
                                               placementPreviewMonkey.getRange());
        }
        repaint();
    }

    public void cancelPlacingMonkey(){
        isPlacingMonkey = false;
        placingMonkeyType = null;
        placementPreviewMonkey = null;
        placementValid = false;
        repaint();
    }

    private boolean isValidPlacement(int x, int y, double newMonkeyHitbox, double newMonkeyRange){
        double newMonkeyRadius = newMonkeyHitbox / 2.0;

        if (x - newMonkeyRadius < 0 || x + newMonkeyRadius > getWidth() ||
            y - newMonkeyRadius < 0 || y + newMonkeyRadius > getHeight()){
            return false;
        }

        for (Monkey m : monkeys){
            double existingMonkeyRadius = m.getHitbox() / 2.0;
            double distanceBetweenCenters = Math.hypot(x - m.getX(), y - m.getY());
            double minimumCenterDistance = newMonkeyRadius + existingMonkeyRadius + separationBuffer;
            if (distanceBetweenCenters < minimumCenterDistance){
                return false;
            }
        }
        
        if (map != null && map.getPath() != null && !map.getPath().isEmpty()){
            ArrayList<Point> pathPoints = map.getPath();
            double pathVisualThickness = 20;
            double minDistanceToPathCenterline = newMonkeyRadius + (pathVisualThickness / 2.0) + separationBuffer;

            for (int i = 0; i < pathPoints.size() - 1; i++){
                Point p1 = pathPoints.get(i);
                Point p2 = pathPoints.get(i + 1);
                java.awt.geom.Line2D.Double pathSegment = new java.awt.geom.Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                if (pathSegment.ptSegDist(x, y) < minDistanceToPathCenterline){
                    return false;
                }
            }
        }
        return true;
    }

    private Monkey createMonkey(String type, int x, int y, int level){
        switch (type){
            case defaultMonkey: return new Monkey(x, y, level);
            case bomberMonkey:return new MonkeyB(x, y, level);
            case slowMonkey:return new MonkeyC(x, y, level);
        }
        return null;
    }

    private int getMonkeyCost(String type){
        switch (type){
            case defaultMonkey:return Monkey.COST;
            case bomberMonkey:return MonkeyB.COST;
            case slowMonkey:return MonkeyC.COST;
            
        }
        return -1;
    }

    private int currentWaveSpawnInstructionIndex;
    private int unitsLeftToSpawnInCurrentInstruction;
    private int currentSpawnCooldownTicks;
    private boolean waveInProgress = false;
    private boolean waveSpawningPhaseActive = false;
    private WaveDefinition currentProceduralWaveDefinition;
    private int lastProceduralWaveGenerated = 0;


    private void initializeWaveDefinitions(){
        this.waveDefinitions = new ArrayList<>();
        addWave(new SpawnInstruction("normal", 5, 10, 30, false));

        addWave(new SpawnInstruction("normal", 10, 10, 25, false),
                new SpawnInstruction("kid", 5, 10, 40, false));

        addWave(new SpawnInstruction("normal", 10, 10, 30, false),
                new SpawnInstruction("kid", 10, 50, 20, false));

        addWave(new SpawnInstruction("kid", 5, 5, 50, false));

        addWave(new SpawnInstruction("businessman", 5, 0, 60, false),
                new SpawnInstruction("normal", 10, 30, 30, false));

        addWave(new SpawnInstruction("bodybuilder", 1, 0, 100, false),
                new SpawnInstruction("ninja", 20, 60, 40, true));

        addWave(new SpawnInstruction("normal", 20, 0, 15, false),
                new SpawnInstruction("bodybuilder", 2, 30, 80, false));

        addWave(new SpawnInstruction("businessman", 10, 0, 30, false),
                new SpawnInstruction("kid", 20, 20, 15, firePropertyChange));

        addWave(new SpawnInstruction("bodybuilder", 5, 0, 60, false),
                new SpawnInstruction("bossbaby", 1, 120, 0, false),
                new SpawnInstruction("ninja", 15, 30, 20, true));

        addWave(new SpawnInstruction("bossbaby", 2, 20, 180, false),
                new SpawnInstruction("bodybuilder", 8, 60, 40, false),
                new SpawnInstruction("businessman", 10, 30, 25, false));

        addWave(new SpawnInstruction("bossninja", 1, 20, 180, true));

        addWave(new SpawnInstruction("bodybuilder", 5, 0, 60, false),
                new SpawnInstruction("bossbaby", 5, 120, 0, false),
                new SpawnInstruction("ninja", 15, 30, 20, true));
        
        addWave(new SpawnInstruction("bossninja", 2, 0, 60, false),
                new SpawnInstruction("ninja", 30, 30, 20, true));
    }

    private void addWave(SpawnInstruction... instructions){
        waveDefinitions.add(new WaveDefinition(Arrays.asList(instructions)));
    }

    public boolean canStartFirstWave(){
        return gameState.getCurrentWave() == 0 && !waveSpawningPhaseActive && humans.isEmpty() && !gameIsOver;
    }

    public void startNextWave(){
        if (gameIsOver) return;


        gameState.incrementWave();
        int waveToStart = gameState.getCurrentWave();

        currentWaveSpawnInstructionIndex = 0;
        unitsLeftToSpawnInCurrentInstruction = 0;
        currentSpawnCooldownTicks = 0;
        waveInProgress = true;
        waveSpawningPhaseActive = true;

        if (waveToStart > waveDefinitions.size()){
            if (lastProceduralWaveGenerated != waveToStart || currentProceduralWaveDefinition == null){
                generateProceduralWave(waveToStart);
                lastProceduralWaveGenerated = waveToStart;
            }
        }
        loadNextSpawnInstruction();
    }

    private WaveDefinition getCurrentActiveWaveDefinition(){
        int currentWaveNum = gameState.getCurrentWave();
        if (currentWaveNum <= 0) return null;
        if (currentWaveNum <= waveDefinitions.size()){
            return waveDefinitions.get(currentWaveNum - 1);
        } else{
            return currentProceduralWaveDefinition;
        }
    }

    private void loadNextSpawnInstruction(){
        WaveDefinition activeWaveDef = getCurrentActiveWaveDefinition();
        if (activeWaveDef == null || currentWaveSpawnInstructionIndex >= activeWaveDef.spawns.size()){
            waveSpawningPhaseActive = false;
            return;
        }
        SpawnInstruction instruction = activeWaveDef.spawns.get(currentWaveSpawnInstructionIndex);
        unitsLeftToSpawnInCurrentInstruction = instruction.count;
        currentSpawnCooldownTicks = instruction.delayTicksAfterPreviousGroup;
    }

    private void updateWaveSpawning(){
        if (!waveSpawningPhaseActive) return;

        if (currentSpawnCooldownTicks > 0){
            currentSpawnCooldownTicks--;
            return;
        }

        WaveDefinition activeWaveDef = getCurrentActiveWaveDefinition();
        if (activeWaveDef == null){
             waveSpawningPhaseActive = false; return;
        }

        if (unitsLeftToSpawnInCurrentInstruction <= 0){
            currentWaveSpawnInstructionIndex++;
            loadNextSpawnInstruction();
            if (!waveSpawningPhaseActive || currentSpawnCooldownTicks > 0) return;
        }

        if (waveSpawningPhaseActive && currentWaveSpawnInstructionIndex < activeWaveDef.spawns.size()){
            SpawnInstruction currentInstruction = activeWaveDef.spawns.get(currentWaveSpawnInstructionIndex);
            if (map != null && unitsLeftToSpawnInCurrentInstruction > 0){
                Human newHuman = map.spawnHumanByType(currentInstruction.humanType, currentInstruction.isCamo);
                if (newHuman != null){
                    humans.add(newHuman);
                }
                unitsLeftToSpawnInCurrentInstruction--;

                if (unitsLeftToSpawnInCurrentInstruction > 0){
                    currentSpawnCooldownTicks = currentInstruction.intervalTicksPerUnit;
                } else{
                    currentSpawnCooldownTicks = 0;
                }
            }
        }
    }

    private void generateProceduralWave(int waveNum){
        List<SpawnInstruction> spawns = new ArrayList<>();
        int difficultyFactor = waveNum - waveDefinitions.size();
        int numGroups = Math.min(8, 2 + difficultyFactor + random.nextInt(2));

        for (int i = 0; i < numGroups; i++){
            String type;
            int typeRoll = random.nextInt(100) + difficultyFactor * 3;
            boolean camo = false;
            
            if (typeRoll > 100 && difficultyFactor > 1){
                type = "bossninja+";
                camo = true;
            }
            else if (typeRoll > 85) type = "bossninja";
            else if (typeRoll > 50) type = "ninja";
            else type = "bossbaby";



            int count = 3 + random.nextInt(2 + difficultyFactor / 2) + difficultyFactor / 3;
            count = Math.min(type.equals("bossbaby") ? (2 + difficultyFactor/3) : 15, count);

            int delayAfterPrevious = Math.max(10, 100 - difficultyFactor * 8);
            int intervalPerUnit = Math.max(5, 35 - difficultyFactor * 2);

            spawns.add(new SpawnInstruction(type, count, (i == 0 ? 0 : delayAfterPrevious), intervalPerUnit, camo));
        }
        this.currentProceduralWaveDefinition = new WaveDefinition(spawns);
    }

    private void updateGame(){
        if (gameIsOver){
            if (!humans.isEmpty()) humans.clear();
            return;
        }

        if (waveSpawningPhaseActive){
            updateWaveSpawning();
        }

        for (Monkey m : monkeys){
            m.updateAndTarget(getWidth(), getHeight(), humans, map.getPath());
        }

        Iterator<Human> humanIterator = humans.iterator();
        while (humanIterator.hasNext()){
            Human h = humanIterator.next();
            h.update(map.getPath());
            if (!h.isAlive()){
                gameState.incrementBloonsKilled(1);
                gameState.addMoney(h.getMoneyReward());
                humanIterator.remove();
            } else if (h.hasReachedEnd()){
                gameState.loseLife(h.getHealth()/10);
                humanIterator.remove();
                if (gameState.isGameOver()){
                    handleGameOver();
                    return;
                }
            }
        }

        if (waveInProgress && !waveSpawningPhaseActive && humans.isEmpty() && !gameIsOver){
            gameState.addMoney(100 + gameState.getCurrentWave());
            waveInProgress = false;
            startNextWave();
        }
    }

    private void handleGameOver(){
        gameIsOver = true;
        waveInProgress = false;
        waveSpawningPhaseActive = false;
        setSelectedMonkey(null);
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.drawImage(mapBackgroundSprite, 0, 0, getWidth(), getHeight(), null);
        //removelater
        for (Human h : new ArrayList<>(humans)) h.draw(g2d);
        for (Monkey m : new ArrayList<>(monkeys)) m.draw(g2d);
        
        if (isPlacingMonkey && placementPreviewMonkey != null && !gameIsOver){
            drawPlacementPreview(g2d);
        }
       
        if (gameIsOver){
            drawGameOverScreen(g2d);
        }
    }

    private void drawPlacementPreview(Graphics2D g2d){
        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
        g2dCopy.setComposite(ac);
        placementPreviewMonkey.draw(g2dCopy);
        g2dCopy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        Color validityColorFill = placementValid ? new Color(0, 255, 0, 50) : new Color(255, 0, 0, 50);
        Color validityColorBorder = placementValid ? new Color(0, 200, 0, 150) : new Color(200, 0, 0, 150);

        int previewRange = (int) placementPreviewMonkey.getRange();
        int previewX = placementPreviewMonkey.getX();
        int previewY = placementPreviewMonkey.getY();

        g2dCopy.setColor(validityColorFill);
        g2dCopy.fillOval(previewX - previewRange, previewY - previewRange, previewRange * 2, previewRange * 2);
        g2dCopy.setColor(validityColorBorder);
        g2dCopy.drawOval(previewX - previewRange, previewY - previewRange, previewRange * 2, previewRange * 2);

        int previewHitboxRadius = (int) (placementPreviewMonkey.getHitbox() / 2.0);
        Color hitboxOutlineColor = placementValid ? new Color(0,150,0, 200) : new Color(150,0,0, 200);
        g2dCopy.setColor(hitboxOutlineColor);
        g2dCopy.drawOval(previewX - previewHitboxRadius, previewY - previewHitboxRadius,
                         previewHitboxRadius * 2, previewHitboxRadius * 2);
        g2dCopy.dispose();
    }

    private void drawGameOverScreen(Graphics2D g2d){
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        Font gameOverFont = new Font("Arial", Font.BOLD, 60);
        g2d.setFont(gameOverFont);
        g2d.setColor(Color.RED);
        String gameOverText = "GAME OVER";
        FontMetrics fmGameOver = g2d.getFontMetrics();
        int textWidthGameOver = fmGameOver.stringWidth(gameOverText);
        g2d.drawString(gameOverText, (getWidth() - textWidthGameOver) / 2, getHeight() / 2 - 50);

        Font waveReachedFont = new Font("Arial", Font.PLAIN, 24);
        g2d.setFont(waveReachedFont);
        g2d.setColor(Color.WHITE);
        String waveReachedText = "You reached wave: " + gameState.getCurrentWave();
        FontMetrics fmWaveReached = g2d.getFontMetrics();
        int textWidthWaveReached = fmWaveReached.stringWidth(waveReachedText);
        g2d.drawString(waveReachedText, (getWidth() - textWidthWaveReached) / 2, getHeight() / 2 + fmWaveReached.getAscent() - 10);

        int buttonWidth = 200;
        int buttonHeight = 50;
        restartButtonBounds.setBounds((getWidth() - buttonWidth) / 2, getHeight() / 2 + 40, buttonWidth, buttonHeight);

        g2d.setColor(hoverOnRestartButton ? restartButtonHoverColor : restartButtonColor);
        g2d.fillRect(restartButtonBounds.x, restartButtonBounds.y, restartButtonBounds.width, restartButtonBounds.height);
        
        Font restartButtonFont = new Font("Arial", Font.BOLD, 24);
        g2d.setFont(restartButtonFont);
        g2d.setColor(Color.WHITE);
        String restartText = "Restart Game";
        FontMetrics fmRestart = g2d.getFontMetrics();
        int textWidthRestart = fmRestart.stringWidth(restartText);
        g2d.drawString(restartText, 
                       restartButtonBounds.x + (buttonWidth - textWidthRestart) / 2, 
                       restartButtonBounds.y + fmRestart.getAscent() + (buttonHeight - fmRestart.getHeight()) / 2);
        
        g2d.setColor(Color.BLACK);
        g2d.drawRect(restartButtonBounds.x, restartButtonBounds.y, restartButtonBounds.width, restartButtonBounds.height);
    }

    private static class SpawnInstruction{
        String humanType;
        int count;
        int delayTicksAfterPreviousGroup;
        int intervalTicksPerUnit;
        boolean isCamo;

        public SpawnInstruction(String ht, int c, int d, int i, boolean camo){
            humanType=ht; count=c; delayTicksAfterPreviousGroup=d; intervalTicksPerUnit=i; this.isCamo=camo;
        }
    }

    private static class WaveDefinition{
        List<SpawnInstruction> spawns;
        public WaveDefinition(List<SpawnInstruction> s){spawns=s;}
    }
}