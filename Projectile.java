import java.awt.*;

public class Projectile {
    private double x, y; // Current position
    private double dx, dy; // Movement vector (change in x and y per update)
    private int radius;
    private Color color;
    private double speed;
    private int damage;

    public Projectile(double startX, double startY, double targetX, double targetY, double speed, int radius, Color color) {
        this.x = startX;
        this.y = startY;
        this.radius = radius;
        this.color = color;
        this.speed = speed;
        damage=1;

        // Calculate direction vector
        double angle = Math.atan2(targetY - startY, targetX - startX);
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        // Draw oval expects top-left corner, so adjust for center
        g2d.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
    }

    // Optional: Check if projectile is off-screen
    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return x < -radius || x > screenWidth + radius || y < -radius || y > screenHeight + radius;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }
    public Rectangle getBounds() {
        // Assuming radius ias half the diameter for the bounding box
        return new Rectangle((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
    }
}