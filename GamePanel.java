import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.awt.AlphaComposite; // For transparency

public class GamePanel extends JPanel {
    private List<Monkey> monkeys;
    private List<Human> humans;
    private Monkey selectedMonkey = null;
    private UpgradeGUI upgradePanel;
    private GameState gameState;
    private int gameTick;
    private Map map;

    private static final int MONEY_PER_POP = 5;
    private int baseSpawnCooldownTicks = 100;
    private int currentSpawnCooldown;
    private int humansPerSpawn = 3;
    private double chanceForCamoSpawn = 0.15;
    private Random random = new Random();

    // --- Placement State ---
    private boolean isPlacingMonkey = false;
    private String placingMonkeyType = null;
    private Monkey placementPreviewMonkey = null;
    private boolean placementValid = false;
    private int placementMouseX, placementMouseY;

    private static final double MIN_PATH_PLACEMENT_BUFFER = -20;
    private static final double MONKEY_PLACEMENT_SEPARATION_BUFFER = -20;


    public GamePanel(GameState gameState) {
        this.gameState = gameState;
        gameTick = 0;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.LIGHT_GRAY);

        ArrayList<Point> mapCoordinates = new ArrayList<>();
        mapCoordinates.add(new Point(0, 150));
        mapCoordinates.add(new Point(600, 100));
        mapCoordinates.add(new Point(200, 500));
        mapCoordinates.add(new Point(100, 250));
        mapCoordinates.add(new Point(800, 350));
        map = new Map(mapCoordinates);
        upgradePanel = new UpgradeGUI();

        monkeys = new ArrayList<>();
        humans = new ArrayList<>();
        this.currentSpawnCooldown = baseSpawnCooldownTicks;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int clickX = e.getX();
                int clickY = e.getY();

                if (isPlacingMonkey) {
                    if (SwingUtilities.isRightMouseButton(e) || e.getButton() == MouseEvent.BUTTON3) {
                        cancelPlacingMonkey();
                        repaint();
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
                            } else if (placingMonkeyType.equals("MonkeyC")) { // Add MonkeyC
                                cost = MonkeyC.COST;
                                if (gameState.canAfford(cost)) {
                                    newMonkeyToPlace = new MonkeyC(clickX, clickY, 1);
                                    canAffordPlacement = true;
                                }
                            }


                            if (canAffordPlacement && newMonkeyToPlace != null) {
                                gameState.spendMoney(cost);
                                monkeys.add(newMonkeyToPlace);
                                System.out.println("Placed " + placingMonkeyType + " at " + clickX + "," + clickY);
                                cancelPlacingMonkey();
                            } else if (!canAffordPlacement){
                                System.out.println("Cannot afford " + placingMonkeyType + " at placement time. Cost: " + cost);
                            }
                        } else {
                            System.out.println("Invalid placement location for " + placingMonkeyType);
                        }
                        repaint();
                        return;
                    }
                }


                if (selectedMonkey != null && selectedMonkey.isSelected()) {
                    if (upgradePanel.handleClick(clickX, clickY, selectedMonkey, gameState)) {
                        repaint();
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
                    }
                } else {
                    if (selectedMonkey != null) {
                        selectedMonkey.setSelected(false);
                        selectedMonkey = null;
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
                } else if (selectedMonkey != null && selectedMonkey.isSelected()) {
                    upgradePanel.updateHover(placementMouseX, placementMouseY, selectedMonkey, gameState);
                } else {
                    upgradePanel.updateHover(-1, -1, null, gameState);
                }
                repaint();
            }
        });


        Timer gameLoopTimer = new Timer(16, ev -> {
            updateGame();
            repaint();
        });
        gameLoopTimer.start();
        setFocusable(true);
        requestFocusInWindow();
    }

    public void startPlacingMonkey(String type) {
        if (this.selectedMonkey != null) {
            this.selectedMonkey.setSelected(false);
            this.selectedMonkey = null;
        }
        upgradePanel.updateHover(-1, -1, null, gameState);

        this.isPlacingMonkey = true;
        this.placingMonkeyType = type;
        this.placementPreviewMonkey = null;

        int costToCheck = 0;
        if (type.equals("Monkey")) costToCheck = Monkey.COST;
        else if (type.equals("MonkeyB")) costToCheck = MonkeyB.COST;
        else if (type.equals("MonkeyC")) costToCheck = MonkeyC.COST; // Add MonkeyC

        if (!gameState.canAfford(costToCheck)) {
             System.out.println("Cannot afford to start placing " + type + ". Cost: " + costToCheck);
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
            case "MonkeyC": // Add MonkeyC
                placementPreviewMonkey = new MonkeyC(placementMouseX, placementMouseY, 1);
                break;
            default:
                System.err.println("Unknown monkey type to place: " + type);
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
            if (distanceBetweenCenters < (newMonkeyRadius + existingMonkeyRadius + MONKEY_PLACEMENT_SEPARATION_BUFFER)) {
                return false;
            }
        }

        if (map != null && map.getPath() != null && map.getPath().size() > 1) {
            ArrayList<Point> pathPoints = map.getPath();
            float pathVisualThickness = 20f;
            double minDistanceToPathCenterline = newMonkeyRadius + (pathVisualThickness / 2.0) + MIN_PATH_PLACEMENT_BUFFER;

            for (int i = 0; i < pathPoints.size() - 1; i++) {
                Point p1 = pathPoints.get(i);
                Point p2 = pathPoints.get(i + 1);
                java.awt.geom.Line2D.Double pathSegment = new java.awt.geom.Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                double distSqToSegmentCenterline = pathSegment.ptSegDistSq(x, y);
                if (Math.sqrt(distSqToSegmentCenterline) < minDistanceToPathCenterline) {
                    return false;
                }
            }
        }
        return true;
    }


    private void trySpawnHumans() {
        currentSpawnCooldown--;
        if (currentSpawnCooldown <= 0) {
            for (int i = 0; i < humansPerSpawn; i++) {
                if (map != null) {
                    double speed = 1.0 + random.nextDouble() * 1.0;
                    int health = 3 + gameState.getCurrentWave() + random.nextInt(3);
                    int hitbox = 50 + random.nextInt(5);
                    boolean isCamo = random.nextDouble() < chanceForCamoSpawn;
                    Human newHuman = map.spawnHuman(speed, health, hitbox, isCamo);
                    if (newHuman != null) {
                        humans.add(newHuman);
                    }
                }
            }
            currentSpawnCooldown = baseSpawnCooldownTicks - (gameState.getCurrentWave() * 5);
            if (currentSpawnCooldown < 30) currentSpawnCooldown = 30;
        }
    }

    private void updateGame() {
        gameTick++;
        trySpawnHumans();

        for (Monkey m : monkeys) {
            m.updateAndTarget(getWidth(), getHeight(), humans, map.getPath());
        }

        Iterator<Human> humanIterator = humans.iterator();
        while (humanIterator.hasNext()) {
            Human h = humanIterator.next();
            h.update(map.getPath()); // Human update now handles slow effect reverting
            if (!h.isAlive()) {
                gameState.incrementBloonsKilled(1);
                gameState.addMoney(MONEY_PER_POP + (h.isCamo() ? 2 : 0));
                humanIterator.remove();
            } else if (h.hasReachedEnd()) {
                System.out.println("Human reached end! Lives would be lost.");
                humanIterator.remove();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (map != null) map.draw(g2d);

        List<Human> humansToDraw = new ArrayList<>(humans);
        for (Human h : humansToDraw) h.draw(g2d);

        List<Monkey> monkeysToDraw = new ArrayList<>(monkeys);
        for (Monkey m : monkeysToDraw) m.draw(g2d);


        if (selectedMonkey != null && selectedMonkey.isSelected()) {
            upgradePanel.draw(g2d, selectedMonkey, gameState);
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

    public void setBaseSpawnCooldownTicks(int ticks) { if (ticks > 0) this.baseSpawnCooldownTicks = ticks; }
    public void setHumansPerSpawn(int count) { if (count > 0) this.humansPerSpawn = count; }
    public void setChanceForCamoSpawn(double chance) { if (chance >= 0.0 && chance <= 1.0) this.chanceForCamoSpawn = chance; }
}