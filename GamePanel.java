import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random; // For more varied spawns

public class GamePanel extends JPanel {
    private List<Monkey> monkeys;
    private List<Human> humans;
    private Monkey selectedMonkey = null;
    private UpgradeGUI upgradePanel;
    private GameState gameState;
    private int gameTick;
    private Map map;

    private static final int MONEY_PER_POP = 5;

    // --- Spawn Control Variables ---
    private int baseSpawnCooldownTicks = 1; // Cooldown in game ticks (e.g., 180 ticks = 3 seconds at 60 FPS)
    private int currentSpawnCooldown;       // Ticks remaining until next spawn
    private int humansPerSpawn = 1;         // Number of humans to spawn at each event
    private double chanceForCamoSpawn = 0.15; // 15% chance a spawned human is camo
    private Random random = new Random();   // For random elements in spawning

    public GamePanel(GameState gameState) {
        this.gameState = gameState;
        gameTick = 0;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.LIGHT_GRAY);

        ArrayList<Point> mapCoordinates = new ArrayList<>();
        mapCoordinates.add(new Point(0, 300));
        mapCoordinates.add(new Point(100, 300));
        mapCoordinates.add(new Point(100, 100));
        mapCoordinates.add(new Point(700, 100));
        mapCoordinates.add(new Point(700, 500));
        mapCoordinates.add(new Point(100, 500));
        mapCoordinates.add(new Point(100, 650));

        map = new Map(mapCoordinates);
        upgradePanel = new UpgradeGUI();

        monkeys = new ArrayList<>();
        monkeys.add(new MonkeyB(150, 200, 120, 30, 1));
        monkeys.add(new Monkey(150, 250, 150, 40, 3));
        monkeys.add(new Monkey(100, 300, 100, 35, 1));
        monkeys.add(new MonkeyB(300, 400, 130, 30, 1));

        humans = new ArrayList<>();
        // Initialize first spawn cooldown
        this.currentSpawnCooldown = baseSpawnCooldownTicks; // Start with the full cooldown

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int clickX = e.getX();
                int clickY = e.getY();

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
                if (selectedMonkey != null && selectedMonkey.isSelected()) {
                    upgradePanel.updateHover(e.getX(), e.getY(), selectedMonkey, gameState);
                } else {
                    upgradePanel.updateHover(-1, -1, null, gameState);
                }
                repaint();
            }
        });

        Timer gameLoopTimer = new Timer(16, ev -> { // Approx 60 FPS
            updateGame();
            repaint();
        });
        gameLoopTimer.start();
        setFocusable(true);
        requestFocusInWindow();
    }

    // Method to handle spawning logic
    private void trySpawnHumans() {
        currentSpawnCooldown--; // Decrement cooldown each tick

        if (currentSpawnCooldown <= 0) {
            // Time to spawn!
            for (int i = 0; i < humansPerSpawn; i++) {
                if (map != null) {
                    // Customize human properties here
                    double speed = 1.0 + random.nextDouble() * 1.0; // Random speed between 1.0 and 2.0
                    int health = 3 + gameState.getCurrentWave() + random.nextInt(3); // Health increases with wave, some randomness
                    int hitbox = 20 + random.nextInt(11); // Random hitbox between 20-30
                    boolean isCamo = random.nextDouble() < chanceForCamoSpawn;

                    Human newHuman = map.spawnHuman(speed, health, hitbox, isCamo);
                    if (newHuman != null) {
                        humans.add(newHuman);
                    }
                }
            }
            // Reset cooldown for the next spawn
            // You can make this dynamic, e.g., cooldown decreases as waves progress
            currentSpawnCooldown = baseSpawnCooldownTicks - (gameState.getCurrentWave() * 5); // Example: cooldown slightly decreases
            if (currentSpawnCooldown < 30) currentSpawnCooldown = 30; // Minimum cooldown (e.g., 0.5 seconds)

            System.out.println("Spawned " + humansPerSpawn + " humans. Next spawn in " + currentSpawnCooldown + " ticks.");
        }
    }


    private void updateGame() {
        gameTick++;

        // Handle human spawning
        trySpawnHumans();

        // Update Monkeys
        for (Monkey m : monkeys) {
            m.updateAndTarget(getWidth(), getHeight(), humans, map.getPath());
        }

        // Update Humans
        Iterator<Human> humanIterator = humans.iterator();
        while (humanIterator.hasNext()) {
            Human h = humanIterator.next();
            h.update(map.getPath());

            if (!h.isAlive()) {
                gameState.incrementBloonsKilled(1);
                gameState.addMoney(MONEY_PER_POP + (h.isCamo() ? 2 : 0));
                humanIterator.remove();
            } else if (h.hasReachedEnd()) {
                System.out.println("Human reached end! Lives would be lost.");
                // gameState.loseLife(1); // Implement if you have lives
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
        for (Human h : humans) h.draw(g2d);
        for (Monkey m : monkeys) m.draw(g2d);
        if (selectedMonkey != null && selectedMonkey.isSelected()) {
            upgradePanel.draw(g2d, selectedMonkey, gameState);
        }
    }

    // --- Public methods to change spawn settings ---
    public void setBaseSpawnCooldownTicks(int ticks) {
        if (ticks > 0) {
            this.baseSpawnCooldownTicks = ticks;
            // Optionally, reset currentSpawnCooldown if a new base is set mid-game
            // this.currentSpawnCooldown = this.baseSpawnCooldownTicks;
            System.out.println("Base spawn cooldown set to: " + ticks + " ticks.");
        }
    }

    public void setHumansPerSpawn(int count) {
        if (count > 0) {
            this.humansPerSpawn = count;
            System.out.println("Humans per spawn set to: " + count);
        }
    }

    public void setChanceForCamoSpawn(double chance) {
        if (chance >= 0.0 && chance <= 1.0) {
            this.chanceForCamoSpawn = chance;
            System.out.println("Chance for camo spawn set to: " + (chance * 100) + "%");
        }
    }
}