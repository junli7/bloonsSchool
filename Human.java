import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
// import java.awt.Image; // Not directly used

public class Human {
    private double x, y;
    private double currentSpeed; // Renamed from 'speed' to reflect it can change
    private double originalSpeed; // To store the speed before slowing
    private int hitboxDiameter;
    private int health;
    private int currentPathIndex;
    private boolean camo;
    // private boolean slowed; // We'll use slowTimer instead
    private boolean reachedEnd;

    private BufferedImage sprite;
    private static final String HUMAN_NORMAL_SPRITE_PATH = "human_normal.png";
    private static final String HUMAN_CAMO_SPRITE_PATH = "human_camo.png";

    private static final double WAYPOINT_THRESHOLD = 5.0;

    // Slow effect variables
    private long slowEffectEndTimeMillis = 0; // Time when the slow effect wears off
    private static final double SLOW_FACTOR = 0.5; // e.g., reduces speed by 50%

    public Human(double startX, double startY, double speed, int health, int hitboxDiameter, boolean isCamo) {
        this.x = startX;
        this.y = startY;
        this.originalSpeed = speed; // Store original speed
        this.currentSpeed = speed;  // Initial speed is original speed
        this.health = health;
        this.hitboxDiameter = hitboxDiameter;
        this.camo = isCamo;
        this.reachedEnd = false;
        this.currentPathIndex = 0;

        updateSpriteVisual();
    }

    private void updateSpriteVisual() {
        String spritePath = this.camo ? HUMAN_CAMO_SPRITE_PATH : HUMAN_NORMAL_SPRITE_PATH;
        this.sprite = SpriteManager.getScaledSprite(spritePath, this.hitboxDiameter, this.hitboxDiameter);
    }

    public void draw(Graphics2D g2d) {
        Color baseColor;
        if (health > 10) baseColor = Color.RED;
        else if (health > 5) baseColor = Color.BLUE;
        else baseColor = Color.GREEN;

        if (camo) {
            g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 100));
        } else {
            g2d.setColor(baseColor);
        }

        // If slowed, draw a visual indicator (e.g., light blue outline or tint)
        if (isSlowed()) {
            g2d.setColor(new Color(173, 216, 230, 150)); // Light blue, semi-transparent
            g2d.fillOval((int) (x - hitboxDiameter / 2.0 - 2), (int) (y - hitboxDiameter / 2.0 - 2), hitboxDiameter + 4, hitboxDiameter + 4);
            g2d.setColor(baseColor); // Reset color for the main oval if not camo
            if (camo) g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 100));

        }

        g2d.fillOval((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);


        if (sprite != null) {
            g2d.drawImage(sprite, (int) (x - sprite.getWidth() / 2.0), (int) (y - sprite.getHeight() / 2.0), null);
        }

        if (camo && (sprite == null || sprite == SpriteManager.getPlaceholderSprite())) {
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawOval((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);
        }
    }

    public void update(ArrayList<Point> path) {
        if (reachedEnd || path == null || path.isEmpty()) {
            return;
        }

        // Check and revert slow effect if time is up
        if (isSlowed() && System.currentTimeMillis() >= slowEffectEndTimeMillis) {
            revertSpeed();
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

        if (distanceToTarget < WAYPOINT_THRESHOLD || distanceToTarget < this.currentSpeed) {
            this.x = targetX;
            this.y = targetY;
            currentPathIndex++;
            if (currentPathIndex >= path.size()) {
                reachedEnd = true;
            }
        } else {
            double moveX = (deltaX / distanceToTarget) * currentSpeed; // Use currentSpeed
            double moveY = (deltaY / distanceToTarget) * currentSpeed; // Use currentSpeed
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

    /**
     * Applies a slow effect to the human for a specified duration.
     * The slow effect stacks by refreshing the duration if already slowed.
     * @param durationMillis The duration of the slow effect in milliseconds.
     */
    public void applySlow(long durationMillis) {
        if (!isSlowed()) { // If not already slowed, apply the slow factor
            this.currentSpeed = this.originalSpeed * SLOW_FACTOR;
        }
        // Set/Refresh the end time for the slow effect
        this.slowEffectEndTimeMillis = System.currentTimeMillis() + durationMillis;
        // System.out.println("Human slowed. New speed: " + currentSpeed + " until " + slowEffectEndTimeMillis);
    }

    /**
     * Reverts the human's speed to its original speed.
     * Called when the slow effect duration expires.
     */
    private void revertSpeed() {
        this.currentSpeed = this.originalSpeed;
        this.slowEffectEndTimeMillis = 0; // Reset timer
        // System.out.println("Human speed reverted to: " + currentSpeed);
    }

    public boolean isSlowed() {
        return System.currentTimeMillis() < slowEffectEndTimeMillis;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getSpeed() { return currentSpeed; } // Return current speed
    public int getHealth() { return health; }
    public boolean isAlive() { return health > 0; }
    public boolean isCamo() { return camo; }
    // public boolean isSlowed() { return slowed; } // Replaced by method above
    public boolean hasReachedEnd() { return reachedEnd; }
    public int getCurrentPathIndex() { return currentPathIndex; }

    public Rectangle getBounds() {
        return new Rectangle((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);
    }

    public double getDistanceToWaypoint(Point waypoint) {
        if (waypoint == null) return Double.MAX_VALUE;
        double dx = waypoint.getX() - this.x;
        double dy = waypoint.getY() - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}