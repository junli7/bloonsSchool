import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter; // For hover effects
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private List<Monkey> monkeys;
    private Monkey selectedMonkey = null; // To track the currently selected monkey
    private UpgradeGUI upgradePanel;
    private GameState gameState;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.LIGHT_GRAY);

        gameState = new GameState(500); // Start with 500 money
        upgradePanel = new UpgradeGUI();

        monkeys = new ArrayList<>();
        // Initialize monkeys (all start at level 1 for easier testing of upgrades)
        monkeys.add(new Monkey(100, 100, 120, 30, 1));
        monkeys.add(new Monkey(250, 300, 150, 40, 1));
        monkeys.add(new Monkey(400, 500, 100, 35, 1));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int clickX = e.getX();
                int clickY = e.getY();

                // 1. Check if click is on the Upgrade GUI (if a monkey is selected)
                if (selectedMonkey != null && selectedMonkey.isSelected()) {
                    if (upgradePanel.handleClick(clickX, clickY, selectedMonkey, gameState)) {
                        repaint(); // Repaint to reflect changes (level, cost, money)
                        return; // Click handled by upgrade GUI, stop further processing
                    }
                }

                // 2. Handle Monkey Selection
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
                    // A monkey was clicked
                    if (selectedMonkey != currentlyClickedMonkey) { // If it's a new monkey or first selection
                        if (selectedMonkey != null) {
                            selectedMonkey.setSelected(false); // Deselect the old one
                        }
                        selectedMonkey = currentlyClickedMonkey; // Select the new one
                        selectedMonkey.setSelected(true);
                    }
                    // If clicking the same monkey, it remains selected (no change here)
                } else {
                    // Clicked on empty space, deselect any currently selected monkey
                    if (selectedMonkey != null) {
                        selectedMonkey.setSelected(false);
                        selectedMonkey = null;
                    }
                }

                // 3. Handle Shooting (only if the click wasn't handled by the GUI)
                // All monkeys attempt to shoot at the click location
                for (Monkey m : monkeys) {
                    m.shoot(clickX, clickY);
                }
                repaint(); // Repaint to show projectiles or selection changes
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (selectedMonkey != null && selectedMonkey.isSelected()) {
                    upgradePanel.updateHover(e.getX(), e.getY(), selectedMonkey, gameState);
                    repaint(); // Repaint to show hover effect
                } else { // Ensure hover state is reset if no monkey is selected
                     upgradePanel.updateHover(-1, -1, null, gameState); // Pass invalid coords
                     repaint();
                }
            }
        });

        Timer timer = new Timer(16, ev -> {
            updateGame();
            repaint(); // repaint() is often called here, but also after specific actions
        });
        timer.start();
    }

    private void updateGame() {
        for (Monkey m : monkeys) {
            m.update(getWidth(), getHeight());
        }
        // Other game logic updates can go here
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw all monkeys (they handle their own range and level drawing)
        for (Monkey m : monkeys) {
            m.draw(g2d);
        }

        // Draw the Upgrade GUI if a monkey is selected and it's supposed to be visible
        if (selectedMonkey != null && selectedMonkey.isSelected()) {
            upgradePanel.draw(g2d, selectedMonkey, gameState);
        }

        // Draw GameState info (e.g., money)
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Money: $" + gameState.getMoney(), 10, 20);
    }
}