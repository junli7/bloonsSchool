import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private Monkey monkey;
    private List<Monkey> monkeys; // If you want multiple monkeys

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.LIGHT_GRAY);

        // Create a monkey instance
        monkey = new Monkey(100, 300, 150, 30, 1); // x, y, range, size, level

        // If you want multiple monkeys, initialize them here
        monkeys = new ArrayList<>();
        monkeys.add(new Monkey(100, 100, 120, 25, 1));
        monkeys.add(new Monkey(100, 500, 200, 35, 2));


        // Add a mouse listener to trigger shooting
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Tell all monkeys to try shooting at the click location
                for (Monkey m : monkeys) {
                    m.shoot(e.getX(), e.getY());
                }
                // Or just one specific monkey:
                // monkey.shoot(e.getX(), e.getY());
            }
        });

        // Game loop timer
        Timer timer = new Timer(16, e -> { // Roughly 60 FPS (1000ms / 16ms ~= 62.5)
            updateGame();
            repaint();
        });
        timer.start();
    }

    private void updateGame() {
        // Update all monkeys (which in turn update their projectiles)
        for (Monkey m : monkeys) {
            m.update(getWidth(), getHeight());
        }
        // Or just one specific monkey:
        // monkey.update(getWidth(), getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw all monkeys
        for (Monkey m : monkeys) {
            m.draw(g2d);
        }
        // Or just one specific monkey:
        // monkey.draw(g2d);
    }
}