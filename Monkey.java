import java.util.List;
import java.util.ArrayList; // Import ArrayList
import java.awt.*;

public class Monkey {
    private int x; // Center X
    private int y; // Center Y
    private double range;
    private double hitbox; // Let's use this as the monkey's size (diameter)

    private int level;
    private List<Projectile> projectiles;
    private Color monkeyColor = Color.ORANGE; // Monkey's color
    private Color projectileColor = Color.RED; // Projectile color
    private int projectileRadius = 5;
    private double projectileSpeed = 5.0;

    // Cooldown for shooting
    private long lastShotTime = 0;
    private long shootCooldown = 500; // milliseconds (0.5 seconds)


    public Monkey(int nx, int ny, double nrange, double nhitbox, int nlevel) {
        this.x = nx;
        this.y = ny;
        this.range = nrange;
        this.hitbox = nhitbox;
        this.level = nlevel;
        this.projectiles = new ArrayList<>();
    }

    // Method to draw the monkey and its range
    public void draw(Graphics2D g2d) {
        // Draw range indicator (optional, but good for tower defense games)
        g2d.setColor(new Color(150, 150, 150, 100)); // Semi-transparent gray
        g2d.drawOval((int) (x - range), (int) (y - range), (int) (range * 2), (int) (range * 2));

        // Draw the monkey (simple circle for now)
        g2d.setColor(monkeyColor);
        g2d.fillOval((int) (x - hitbox / 2), (int) (y - hitbox / 2), (int) hitbox, (int) hitbox);

        // Draw all active projectiles
        // Use a copy or iterator to avoid ConcurrentModificationException if removing
        List<Projectile> projectilesCopy = new ArrayList<>(projectiles);
        for (Projectile p : projectilesCopy) {
            p.draw(g2d);
        }
    }

    // Method to update the monkey's state (e.g., move projectiles)
    public void update(int screenWidth, int screenHeight) {
        // Update and remove off-screen projectiles
        projectiles.removeIf(p -> {
            p.update();
            return p.isOffScreen(screenWidth, screenHeight);
        });
    }

    // Method for the monkey to shoot a projectile towards a target
    public void shoot(int targetX, int targetY) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < shootCooldown) {
            return; // Cooldown active, don't shoot
        }
        lastShotTime = currentTime;

        // Calculate distance to target
        double distanceToTarget = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));

        // Only shoot if target is within range (optional check)
        if (distanceToTarget <= range) {
            Projectile newProjectile = new Projectile(
                    this.x, 
                    this.y, 
                    targetX,
                    targetY,
                    projectileSpeed + (level -1), 
                    projectileRadius,
                    projectileColor
            );
            projectiles.add(newProjectile);
            System.out.println("Monkey shot! Projectiles: " + projectiles.size());
        } else {
            System.out.println("Target out of range.");
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getRange() {
        return range;
    }
}