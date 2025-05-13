import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList; // Important for concurrent modification

public class GamePanel extends JPanel {
    private Thread gameThread;
    private boolean running = false;

    private Path gamePath;
    private List<Bloon> bloons;
    private List<Tower> towers;
    private List<Projectile> projectiles;

    public static int lives;
    public static int money;
    private int waveNumber;
    private double waveTimer; // Time until next wave or next bloon in wave
    private int bloonsToSpawnInWave;

    public GamePanel() {
        setPreferredSize(new Dimension(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT));
        setBackground(Color.LIGHT_GRAY);

        gamePath = new Path();
        bloons = new CopyOnWriteArrayList<>(); // Use CopyOnWriteArrayList to avoid ConcurrentModificationException
        towers = new CopyOnWriteArrayList<>();
        projectiles = new CopyOnWriteArrayList<>();

        lives = 20;
        money = 200; // Starting money
        waveNumber = 0;
        waveTimer = 5.0; // Time before first wave
        bloonsToSpawnInWave = 0;

        // Simple mouse listener for placing towers
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (money >= Tower.COST) {
                    towers.add(new Tower(e.getX(), e.getY()));
                    money -= Tower.COST;
                } else {
                    System.out.println("Not enough money to place tower!");
                }
            }
        });
    }

    public void startGameLoop() {
        running = true;
        gameThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            double nsPerTick = 1000000000.0 / 60.0; // 60 FPS/UPS
            double delta = 0;

            while (running) {
                long now = System.nanoTime();
                delta += (now - lastTime) / nsPerTick;
                lastTime = now;

                boolean shouldRender = false;
                while (delta >= 1) {
                    update(1.0/60.0); // Pass deltaTime in seconds
                    delta -= 1;
                    shouldRender = true;
                }

                if (shouldRender) {
                    repaint();
                }

                // Yield to other threads and prevent busy-waiting
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameThread.start();
    }

    private void update(double deltaTime) {
        if (lives <= 0) {
            // Game Over logic (simple for now)
            System.out.println("Game Over!");
            running = false; // Stop the loop
            // You might want to display a game over message on screen
            return;
        }
        // Wave management
        waveTimer -= deltaTime;
        if (waveTimer <= 0 && bloonsToSpawnInWave == 0 && bloons.isEmpty()) { // Start new wave
            waveNumber++;
            bloonsToSpawnInWave = 5 + waveNumber * 2; // More bloons each wave
            waveTimer = 1.0; // Time between bloon spawns in a wave
            System.out.println("Starting Wave: " + waveNumber);
        }

        if (bloonsToSpawnInWave > 0 && waveTimer <= 0) {
            spawnBloon();
            bloonsToSpawnInWave--;
            waveTimer = 0.5 - (waveNumber * 0.02); // Bloons spawn faster in later waves
            if (waveTimer < 0.1) waveTimer = 0.1; // Minimum spawn interval
        }


        // Update Bloons
        for (Bloon b : bloons) {
            b.update(deltaTime);
        }

        // Update Towers
        for (Tower t : towers) {
            t.update(deltaTime, bloons, projectiles);
        }

        // Update Projectiles
        for (Projectile p : projectiles) {
            p.update(deltaTime);
        }

        // Remove inactive entities
        bloons.removeIf(b -> !b.active);
        projectiles.removeIf(p -> !p.active);
    }

    private void spawnBloon() {
        int health = 1 + (waveNumber / 5); // Bloons get tougher in later waves
        double speed = 50 + (waveNumber * 2);
        if (speed > 150) speed = 150; // Max speed
        bloons.add(new Bloon(gamePath, speed, health));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Path (optional visualization)
        g2d.setColor(Color.DARK_GRAY);
        List<Point> waypoints = gamePath.getWaypoints();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Point p1 = waypoints.get(i);
            Point p2 = waypoints.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Draw Towers
        for (Tower t : towers) {
            t.draw(g2d);
        }

        // Draw Bloons
        for (Bloon b : bloons) {
            b.draw(g2d);
        }

        // Draw Projectiles
        for (Projectile p : projectiles) {
            p.draw(g2d);
        }

        // Draw UI
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Lives: " + lives, 10, 20);
        g2d.drawString("Money: $" + money, 10, 40);
        g2d.drawString("Wave: " + waveNumber, 10, 60);

        if (lives <= 0) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            String gameOverMsg = "GAME OVER!";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(gameOverMsg);
            g2d.drawString(gameOverMsg, (Main.SCREEN_WIDTH - msgWidth) / 2, Main.SCREEN_HEIGHT / 2);
        }
    }
} 
    

