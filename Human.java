import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Human { // Consider renaming to Balloon or Enemy
    private double x, y; // Current position (center of the balloon)
    private double speed;
    private int hitboxDiameter; // Diameter of the balloon
    private int health;
    private int currentPathIndex; // Index of the current target point in the path
    private boolean camo;
    private boolean slowed;
    private boolean reachedEnd;

    // How close the balloon needs to be to a waypoint to consider it "reached"
    private static final double WAYPOINT_THRESHOLD = 5.0; // Pixels

    public Human(double startX, double startY, double speed, int health, int hitboxDiameter, boolean isCamo) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.health = health;
        this.hitboxDiameter = hitboxDiameter;
        this.camo = isCamo;
        this.slowed = false;
        this.reachedEnd = false;
        this.currentPathIndex = 0; // Initially targets the first point in the path
                                   // Or 1 if it starts AT path.get(0) and targets path.get(1)
                                   // Let's assume it starts at (startX, startY) and targets path.get(0)
                                   // Then path.get(1) etc.
    }

    public void draw(Graphics2D g2d) {
        // Example: Color changes based on health or type
        if (health > 10) g2d.setColor(Color.RED);
        else if (health > 5) g2d.setColor(Color.BLUE);
        else g2d.setColor(Color.GREEN);

        if (camo) {
            g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(), 100)); // Semi-transparent
        }

        // Draw oval expects top-left corner, so adjust from center
        g2d.fillOval((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);

        if (camo) { // Draw a border or indicator for camo
            g2d.setColor(Color.BLACK);
            g2d.drawOval((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);
        }
    }

    /**
     * Updates the balloon's position along the given path.
     * @param path The list of waypoints (Points) the balloon should follow.
     */
    public void update(ArrayList<Point> path) {
        if (reachedEnd || path == null || path.isEmpty()) {
            return;
        }

        if (currentPathIndex >= path.size()) {
            reachedEnd = true; // Reached the end of the defined path
            return;
        }

        Point targetPoint = path.get(currentPathIndex);
        double targetX = targetPoint.getX();
        double targetY = targetPoint.getY();

        // Calculate vector from current position to target point
        double deltaX = targetX - this.x;
        double deltaY = targetY - this.y;

        double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distanceToTarget < WAYPOINT_THRESHOLD || distanceToTarget < this.speed) {
            // Reached the waypoint (or close enough/will overshoot if we move by full speed)
            this.x = targetX; // Snap to waypoint
            this.y = targetY;
            currentPathIndex++; // Move to the next waypoint
            if (currentPathIndex >= path.size()) {
                reachedEnd = true; // Reached the actual end of the path
            }
        } else {
            // Move towards the target waypoint
            // Normalize the direction vector
            double moveX = (deltaX / distanceToTarget) * speed;
            double moveY = (deltaY / distanceToTarget) * speed;

            this.x += moveX;
            this.y += moveY;
        }
    }

    public void takeDamage(int damage) {
        if (camo /* && !attackerCanSeeCamo */) { // Add logic for camo detection by attacker later
            // return; // For now, all attacks hit camo
        }
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0;
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getSpeed() { return speed; }
    public int getHealth() { return health; }
    public boolean isAlive() { return health > 0; }
    public boolean isCamo() { return camo; }
    public boolean isSlowed() { return slowed; }
    public boolean hasReachedEnd() { return reachedEnd; }
    public int getCurrentPathIndex() { return currentPathIndex; } // Old getPosition()

    public Rectangle getBounds() {
        return new Rectangle((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);
    }
}