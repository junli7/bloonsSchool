import java.awt.*;

public class Projectile {
    public double x, y;
    private Bloon target;
    private double speed;
    private int damage;
    public boolean active = true;
    public static final int SIZE = 8;

    public Projectile(Tower shooter, Bloon target) {
        this.x = shooter.getCenter().x;
        this.y = shooter.getCenter().y;
        this.target = target;
        this.speed = 200; // Pixels per second
        this.damage = 1; // Damage dealt
    }

    public void update(double deltaTime) {
        if (!active) return;

        if (!target.active) { // Target already popped or gone
            active = false;
            return;
        }

        double targetX = target.getCenter().x;
        double targetY = target.getCenter().y;

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < speed * deltaTime || distance < Bloon.SIZE / 2.0) { // Hit target
            target.takeDamage(damage);
            active = false;
        } else {
            x += (dx / distance) * speed * deltaTime;
            y += (dy / distance) * speed * deltaTime;
        }
    }

    public void draw(Graphics g) {
        if (!active) return;
        g.setColor(Color.BLACK);
        g.fillOval((int) (x - SIZE / 2.0), (int) (y - SIZE / 2.0), SIZE, SIZE);
    }
}