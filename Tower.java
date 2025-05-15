import java.awt.*;
import java.util.List;

public class Tower {
    public int x, y;
    private int range;
    private double fireRate; // Shots per second
    private double fireCooldown; // Time until next shot
    public static final int SIZE = 30;
    public static final int COST = 100;

    public Tower(int x, int y) {
        this.x = x;
        this.y = y;
        this.range = 100; // Pixels
        this.fireRate = 1.0; // 1 shot per second
        this.fireCooldown = 0;
    }

    public void update(double deltaTime, List<Bloon> bloons, List<Projectile> projectiles) {
        if (fireCooldown > 0) {
            fireCooldown -= deltaTime;
        }

        if (fireCooldown <= 0) {
            Bloon target = findTarget(bloons);
            if (target != null) {
                projectiles.add(new Projectile(this, target,1));
                fireCooldown = 1.0 / fireRate; // Reset cooldown
            }
        }
    }

    private Bloon findTarget(List<Bloon> bloons) {
        Bloon closestBloon = null;
        double minDistance = Double.MAX_VALUE;

        for (Bloon b : bloons) {
            if (b.active) {
                double dx = b.x - this.x;
                double dy = b.y - this.y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance <= range && distance < minDistance) {
                    minDistance = distance;
                    closestBloon = b;
                }
            }
        }
        return closestBloon;
    }

    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);

        // Draw range (optional)
        g.setColor(new Color(0, 255, 0, 50)); // Semi-transparent green
        g.drawOval(x - range, y - range, range * 2, range * 2);
    }

    public Point getCenter() {
        return new Point(x, y);
    }
}