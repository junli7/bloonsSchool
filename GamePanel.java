
import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {
    private List<Monkey> monkeys;
    private List<Human> humans;
    private Monkey selectedMonkey = null;
    private UpgradeGUI upgradePanel;
    private GameState gameState;
    private int gameTick;
    private Map map;

    // private List<ExplosionVisual> activeExplosions; // REMOVED

    private static final int MONEY_PER_POP = 5;
    private int baseSpawnCooldownTicks = 100;
    private int currentSpawnCooldown;
    private int humansPerSpawn = 3;
    private double chanceForCamoSpawn = 0.15;
    private Random random = new Random();

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
        monkeys.add(new MonkeyB(100, 100,  1));
        monkeys.add(new Monkey(300, 300,  1));
        monkeys.add(new MonkeyB(500, 300, 2));

        humans = new ArrayList<>();
        this.currentSpawnCooldown = baseSpawnCooldownTicks;

        // ... (MouseListeners remain the same) ...
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


        Timer gameLoopTimer = new Timer(16, ev -> {
            updateGame();
            repaint();
        });
        gameLoopTimer.start();
        setFocusable(true);
        requestFocusInWindow();
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

        // Update Monkeys - they handle their projectiles' lifecycle including explosion visuals
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

        // Explosions are drawn by the projectiles themselves when they are in the EXPLODING state.
        // So, no separate loop for explosions needed here.

        // Draw humans
        List<Human> humansToDraw = new ArrayList<>(humans);
        for (Human h : humansToDraw) h.draw(g2d);

        // Draw monkeys (which will in turn draw their projectiles, including any active explosion visuals)
        List<Monkey> monkeysToDraw = new ArrayList<>(monkeys);
        for (Monkey m : monkeysToDraw) m.draw(g2d);


        if (selectedMonkey != null && selectedMonkey.isSelected()) {
            upgradePanel.draw(g2d, selectedMonkey, gameState);
        }
    }

    public void setBaseSpawnCooldownTicks(int ticks) { if (ticks > 0) this.baseSpawnCooldownTicks = ticks; }
    public void setHumansPerSpawn(int count) { if (count > 0) this.humansPerSpawn = count; }
    public void setChanceForCamoSpawn(double chance) { if (chance >= 0.0 && chance <= 1.0) this.chanceForCamoSpawn = chance; }
}