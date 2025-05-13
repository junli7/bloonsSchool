import java.awt.*;

public class Bloon {
    public double x, y;
    private double speed;
    private int health;
    private Path path;
    private int currentWaypointIndex;
    public boolean active = true; // Is the bloon still in play?
    public static final int SIZE = 20; // Diameter

    public Bloon(Path path, double speed, int health) {
        this.path = path;
        this.speed = speed;
        this.health = health;
        this.currentWaypointIndex = 0;
        Point startPoint = path.getWaypoint(0);
        if (startPoint != null) {
            this.x = startPoint.x;
            this.y = startPoint.y;
        } else {
            this.x = -100; // Off-screen if path is invalid
            this.y = -100;
            active = false;
        }
    }

    public void update(double deltaTime) {
        if (!active) return;

        Point targetWaypoint = path.getWaypoint(currentWaypointIndex);
        if (targetWaypoint == null) { // Reached end of path
            active = false; // Bloon escaped
            GamePanel.lives--; // Oh no!
            return;
        }

        double targetX = targetWaypoint.x;
        double targetY = targetWaypoint.y;

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < speed * deltaTime) { // Reached waypoint
            x = targetX;
            y = targetY;
            currentWaypointIndex++;
            if (currentWaypointIndex >= path.getNumWaypoints()) {
                active = false; // Bloon escaped
                GamePanel.lives--;
            }
        } else { // Move towards waypoint
            x += (dx / distance) * speed * deltaTime;
            y += (dy / distance) * speed * deltaTime;
        }
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            active = false;
            GamePanel.money += 5; // Reward for popping
        }
    }

    public void draw(Graphics g) {
        if (!active) return;
        if (health > 1) g.setColor(Color.RED);
        else g.setColor(Color.BLUE); // Simple color change for health
        g.fillOval((int) (x - SIZE / 2.0), (int) (y - SIZE / 2.0), SIZE, SIZE);
    }

    public Point getCenter() {
        return new Point((int)x, (int)y);
    }
}