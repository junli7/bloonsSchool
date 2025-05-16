import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Projectile {
    private double x, y;        // Current position
    private double dx, dy;      // Movement vector (recalculated for homing)
    private int radius;
    private Color color;
    private double speed;
    private int damage;         // Damage this projectile deals
    private Human target;       // The specific human this projectile is homing on

    // Threshold for considering a hit. Should be small, e.g., projectile speed or radius.
    private static final double HIT_THRESHOLD_DISTANCE = 5.0;


    public Projectile(double startX, double startY, Human targetHuman, double speed, int radius, Color color, int damage) {
        this.x = startX;
        this.y = startY;
        this.target = targetHuman; // Store the target
        this.speed = speed;
        this.radius = radius;
        this.color = color;
        this.damage = damage;

        // Initial dx, dy calculation (will be updated each frame by homing logic)
        if (this.target != null) {
            calculateDirectionToTarget();
        } else {
            // No target, or target initially invalid (should ideally not happen if logic is correct)
            this.dx = 0; // Projectile won't move or will be removed immediately
            this.dy = 0;
        }
    }

    private void calculateDirectionToTarget() {
        double targetX = target.getX();
        double targetY = target.getY();
        double deltaX = targetX - this.x;
        double deltaY = targetY - this.y;
        double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distanceToTarget > 0) { // Avoid division by zero if already at target
            this.dx = (deltaX / distanceToTarget) * speed;
            this.dy = (deltaY / distanceToTarget) * speed;
        } else {
            this.dx = 0;
            this.dy = 0;
        }
    }

    /**
     * Updates the projectile's position, homes on the target, and checks for a hit.
     * @return true if the projectile should be removed (hit target, target died, or went off-screen), false otherwise.
     */
    public boolean updateAndCheckHit() {
        // 1. Check if the target is still valid
        if (target == null || !target.isAlive()) {
            return true; // Mark for removal: Target is gone
        }

        // 2. Homing: Recalculate direction to target's current position
        calculateDirectionToTarget();

        // 3. Move the projectile
        this.x += dx;
        this.y += dy;

        // 4. Check for hit: If projectile is very close to its target
        double currentDistanceToTarget = Math.sqrt(Math.pow(target.getX() - this.x, 2) + Math.pow(target.getY() - this.y, 2));

        if (currentDistanceToTarget < HIT_THRESHOLD_DISTANCE || currentDistanceToTarget < this.speed) {
            // Considered a hit if we are within threshold or would pass it in the next move
            target.takeDamage(this.damage);
            return true; // Mark for removal: Hit target
        }

        return false; // Projectile continues to exist
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
    }

    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return x < -radius || x > screenWidth + radius || y < -radius || y > screenHeight + radius;
    }

    // Getters might still be useful for debugging or other features
    public double getX() { return x; }
    public double getY() { return y; }
    public int getDamage() { return damage; }

    // getBounds() is less relevant for homing projectiles if they only hit their specific target.
    // Kept for potential other uses or if you mix projectile types.
    public Rectangle getBounds() {
        return new Rectangle((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
    }
}