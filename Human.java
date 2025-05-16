import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Human {
    private double x, y;
    private double speed;
    private int hitboxDiameter;
    private int health;
    private int currentPathIndex;
    private boolean camo;
    private boolean slowed;
    private boolean reachedEnd;

    private static final double WAYPOINT_THRESHOLD = 5.0;

   



    public Human(double startX, double startY, double speed, int health, int hitboxDiameter, boolean isCamo) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.health = health;
        this.hitboxDiameter = hitboxDiameter;
        this.camo = isCamo;
        this.slowed = false;
        this.reachedEnd = false;
        this.currentPathIndex = 0;
    }

    public void draw(Graphics2D g2d) {
        if (health > 10) g2d.setColor(Color.RED);
        else if (health > 5) g2d.setColor(Color.BLUE);
        else g2d.setColor(Color.GREEN);

        if (camo) {
            g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(), 100));
        }
        g2d.fillOval((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);
        if (camo) {
            g2d.setColor(Color.DARK_GRAY); // Camo outline
            g2d.drawOval((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);
        }
    }

    public void update(ArrayList<Point> path) {
        if (reachedEnd || path == null || path.isEmpty()) {
            return;
        }
        if (currentPathIndex >= path.size()) {
            reachedEnd = true;
            return;
        }

        Point targetPoint = path.get(currentPathIndex);
        double targetX = targetPoint.getX();
        double targetY = targetPoint.getY();
        double deltaX = targetX - this.x;
        double deltaY = targetY - this.y;
        double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distanceToTarget < WAYPOINT_THRESHOLD || distanceToTarget < this.speed) {
            this.x = targetX;
            this.y = targetY;
            currentPathIndex++;
            if (currentPathIndex >= path.size()) {
                reachedEnd = true;
            }
        } else {
            double moveX = (deltaX / distanceToTarget) * speed;
            double moveY = (deltaY / distanceToTarget) * speed;
            this.x += moveX;
            this.y += moveY;
        }
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0;
        }
    }

    // --- Getters and Setters ---
    public double getX() { return x; }
    public double getY() { return y; }
    public double getSpeed() { return speed; }
    public int getHealth() { return health; }
    public boolean isAlive() { return health > 0; }
    public boolean isCamo() { return camo; }
    public boolean isSlowed() { return slowed; }
    public boolean hasReachedEnd() { return reachedEnd; }
    public int getCurrentPathIndex() { return currentPathIndex; }

    public Rectangle getBounds() {
        return new Rectangle((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);
    }

    /**
     * Calculates the distance from this human to a given waypoint.
     * Used for tie-breaking when two humans are on the same path segment.
     */
    public double getDistanceToWaypoint(Point waypoint) {
        if (waypoint == null) return Double.MAX_VALUE;
        double dx = waypoint.getX() - this.x;
        double dy = waypoint.getY() - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

}