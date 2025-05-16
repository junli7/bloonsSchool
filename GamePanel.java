import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel {
    private List<Monkey> monkeys;
    private List<Human> humans;
    private Monkey selectedMonkey = null;
    private UpgradeGUI upgradePanel;
    private GameState gameState;
    private int gameTick;
    private Map map;

    private static final int MONEY_PER_POP = 5;

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
        monkeys.add(new Monkey(600, 200, 150, 40, 3));
        monkeys.add(new Monkey(400, 400, 100, 35, 1));
        monkeys.add(new MonkeyB(300, 400, 130, 30, 1));

        humans = new ArrayList<>();
        spawnTestHuman(); // Initial humans

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

    private void spawnTestHuman() {
        if (map != null) {
            Human newHuman = map.spawnHuman(1.5, 5, 25, false);
            if (newHuman != null) humans.add(newHuman);
            Human camoHuman = map.spawnHuman(1.2, 3, 25, true);
            if (camoHuman != null) humans.add(camoHuman);
        }
    }

    private void updateGame() {
        gameTick++;

        if (gameTick % 240 == 0) {
            Human newHuman = map.spawnHuman((gameTick % 480 == 0) ? 2.0 : 1.5,
                    5 + (gameState.getCurrentWave()), 25, (gameTick % 720 == 0));
            if (newHuman != null) humans.add(newHuman);
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
                gameState.addMoney(MONEY_PER_POP + (h.isCamo() ? 2 : 0));
                humanIterator.remove();
            } else if (h.hasReachedEnd()) {
                System.out.println("Human reached end! Lives would be lost.");
                // gameState.loseLife(1); // Implement if you have lives
                humanIterator.remove();
            }
        }
        // No longer need checkCollisions() here, as projectiles handle their own hits
    }

    // The checkCollisions() method is removed as homing projectiles manage their own hits.
    // If you add non-homing projectiles later, you might re-introduce a collision system.

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (map != null) map.draw(g2d);
        for (Human h : humans) h.draw(g2d); // Draw humans before monkeys so projectiles can appear on top
        for (Monkey m : monkeys) m.draw(g2d); // Monkeys draw their own projectiles
        if (selectedMonkey != null && selectedMonkey.isSelected()) {
            upgradePanel.draw(g2d, selectedMonkey, gameState);
        }
    }
}