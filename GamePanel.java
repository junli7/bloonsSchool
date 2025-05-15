import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private List<Monkey> monkeys;
    private Monkey selectedMonkey = null;
    private UpgradeGUI upgradePanel;
    private GameState gameState; // Will be passed from MainFrame
    private Map map;

    // MONEY_PER_POP is just for demo purposes, as projectiles going off-screen = pop
    private static final int MONEY_PER_POP = 2;


    public GamePanel(GameState gameState) { // Constructor now takes GameState
        this.gameState = gameState;
        // The preferred size is for the game area itself.
        // The SideInfoPanel will add to the total window width.
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.LIGHT_GRAY); // Main game area background

        //map
        ArrayList<Point> mapCoordinates = new ArrayList<Point>();
        mapCoordinates.add(new Point(0,500));
        mapCoordinates.add(new Point(1000,500));


        map = new Map(mapCoordinates);
        
        upgradePanel = new UpgradeGUI();

        monkeys = new ArrayList<>();
        monkeys.add(new Monkey(100, 100, 120, 30, 1));
        monkeys.add(new Monkey(250, 300, 150, 40, 1));
        monkeys.add(new Monkey(400, 500, 100, 35, 1));
        monkeys.add(new MonkeyB(300, 100, 120, 30, 1));


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

                for (Monkey m : monkeys) {
                    m.shoot(clickX, clickY);
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
        setFocusable(true); // Important for potential keyboard input later
        requestFocusInWindow();
    }

    private void updateGame() {
        for (Monkey m : monkeys) {
            int projectilesPopped = m.update(getWidth(), getHeight());
            if (projectilesPopped > 0) {
                gameState.incrementBloonsKilled(projectilesPopped);
                gameState.addMoney(projectilesPopped * MONEY_PER_POP); // Give money for "popped" projectiles
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Monkey m : monkeys) {
            m.draw(g2d);
        }

        if (selectedMonkey != null && selectedMonkey.isSelected()) {
            upgradePanel.draw(g2d, selectedMonkey, gameState);
        }
        map.draw(g2d);

        // Money and other stats are no longer drawn here; they are in SideInfoPanel
    }
}