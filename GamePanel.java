import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator; // For safe removal from list
import java.util.List;

public class GamePanel extends JPanel {
    private List<Monkey> monkeys;
    private List<Human> humans; // Changed to List for consistency
    private Monkey selectedMonkey = null;
    private UpgradeGUI upgradePanel;
    private GameState gameState;
    private int gameTick;
    private Map map; // Keep as Map

    private static final int MONEY_PER_POP = 5; // Example money for popping a balloon
    private static final int BLOON_DAMAGE_TO_PLAYER = 1; // Lives lost if balloon reaches end

    public GamePanel(GameState gameState) {
        this.gameState = gameState;
        gameTick = 0;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.LIGHT_GRAY);

        // Define map coordinates
        ArrayList<Point> mapCoordinates = new ArrayList<>();
        mapCoordinates.add(new Point(0, 300));       // Start (e.g., off-screen left)
        mapCoordinates.add(new Point(100, 300));
        mapCoordinates.add(new Point(100, 100));
        mapCoordinates.add(new Point(700, 100));
        mapCoordinates.add(new Point(700, 500));
        mapCoordinates.add(new Point(100, 500));
        mapCoordinates.add(new Point(100, getHeight() + 50)); // End (e.g., off-screen bottom, adjust if getHeight() is 0 here)
                                                          // Using a fixed value if getHeight() is unreliable here:
                                                          // mapCoordinates.add(new Point(100, 650));

        map = new Map(mapCoordinates); // Initialize map

        upgradePanel = new UpgradeGUI();

        monkeys = new ArrayList<>();
        monkeys.add(new Monkey(150, 200, 120, 30, 1));
        monkeys.add(new Monkey(600, 200, 150, 40, 1));
        monkeys.add(new Monkey(400, 400, 100, 35, 1));
        monkeys.add(new MonkeyB(300, 400, 120, 30, 1));

        humans = new ArrayList<>(); // Initialize empty list
        // Spawn an initial human for testing:
        spawnTestHuman();


        // --- MOUSE LISTENERS (NO CHANGE NEEDED HERE from your provided code) ---
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
                        if (selectedMonkey != null) {
                            selectedMonkey.setSelected(false);
                        }
                        selectedMonkey = currentlyClickedMonkey;
                        selectedMonkey.setSelected(true);
                    }
                } else {
                    if (selectedMonkey != null) {
                        selectedMonkey.setSelected(false);
                        selectedMonkey = null;
                    }
                }

                // Let monkeys shoot towards the click if not on UI
                // Consider changing monkeys to auto-target nearest balloon later
                for (Monkey m : monkeys) {
                    m.shoot(clickX, clickY); // Keep for now, or adapt to target balloons
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
                repaint(); // Repaint to show hover effect
            }
        });
        // --- END MOUSE LISTENERS ---

        Timer gameLoopTimer = new Timer(16, ev -> { // Roughly 60 FPS
            updateGame();
            repaint();
        });
        gameLoopTimer.start();
        setFocusable(true);
        requestFocusInWindow();
    }

    // Example method to spawn a human
    private void spawnTestHuman() {
        if (map != null) {
            // Use the Map's spawnHuman method
            Human newHuman = map.spawnHuman(1.5, 5, 25, false); // speed, health, hitbox, camo
            if (newHuman != null) {
                humans.add(newHuman);
            }
        }
    }


    private void updateGame() {
        gameTick++;

        // Spawn new humans based on gameTick or wave logic (example: spawn one every few seconds)
        if (gameTick % 180 == 0) { // Spawn a human every ~3 seconds (180 ticks / 60 fps)
            spawnTestHuman();
        }
        if (gameTick % 300 == 0) { // Spawn a camo human every 5 seconds
             Human newHuman = map.spawnHuman(1.2, 8, 25, true);
             if (newHuman != null) humans.add(newHuman);
        }


        // Update Monkeys (projectiles are updated within monkey.update)
        for (Monkey m : monkeys) {
            // We'll handle projectile removal due to off-screen in Monkey.update
            // And projectile removal due to collision in checkCollisions
            m.update(getWidth(), getHeight());
        }

        // Update Humans
        Iterator<Human> humanIterator = humans.iterator();
        while (humanIterator.hasNext()) {
            Human h = humanIterator.next();
            h.update(map.getPath()); // Human follows the map's path

            if (!h.isAlive()) {
                gameState.incrementBloonsKilled(1);
                gameState.addMoney(MONEY_PER_POP);
                humanIterator.remove(); // Remove dead balloon
            } else if (h.hasReachedEnd()) {
                // gameState.loseLife(BLOON_DAMAGE_TO_PLAYER); // Implement loseLife in GameState
                System.out.println("Balloon reached end! Player would lose a life.");
                humanIterator.remove(); // Remove balloon that reached the end
            }
        }

        // Check for collisions between projectiles and humans
        checkCollisions();
    }

    private void checkCollisions() {
        for (Monkey monkey : monkeys) {
            // Need a way to get projectiles from monkey if not already public
            // Assuming Monkey class has: public List<Projectile> getProjectiles()
            List<Projectile> projectiles = monkey.getProjectiles(); // You'll need to add this getter to Monkey
            Iterator<Projectile> projectileIterator = projectiles.iterator();

            while (projectileIterator.hasNext()) {
                Projectile p = projectileIterator.next();
                boolean hit = false;

                Iterator<Human> humanIterator = humans.iterator();
                while (humanIterator.hasNext()) {
                    Human h = humanIterator.next();
                    if (p.getBounds().intersects(h.getBounds())) { // Need getBounds() in Projectile
                        h.takeDamage(p.getDamage()); // Need getDamage() in Projectile
                        hit = true;
                        // Projectile is removed after hitting one human
                        break;
                    }
                }

                if (hit || p.isOffScreen(getWidth(), getHeight())) {
                    projectileIterator.remove(); // Remove projectile if it hit or went off-screen
                }
            }
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw map first (so it's behind everything)
        if (map != null) {
            map.draw(g2d);
        }

        // Draw all monkeys (they draw their own projectiles internally)
        for (Monkey m : monkeys) {
            m.draw(g2d);
        }

        // Draw all humans
        for (Human h : humans) {
            h.draw(g2d);
        }

        // Draw Upgrade GUI if a monkey is selected
        if (selectedMonkey != null && selectedMonkey.isSelected()) {
            upgradePanel.draw(g2d, selectedMonkey, gameState);
        }

        // GameState info (like money) is handled by SideInfoPanel
    }
}