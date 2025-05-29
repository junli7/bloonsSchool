
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.awt.Image;

public class Human {
    private double x, y;
    private double speed;
    private int hitboxDiameter;
    private int health;
    private int currentPathIndex;
    private boolean camo;
    private boolean slowed;
    private boolean reachedEnd;

    private BufferedImage sprite;
    private static final String HUMAN_NORMAL_SPRITE_PATH = "human_normal.png";
    private static final String HUMAN_CAMO_SPRITE_PATH = "human_camo.png";

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
        g2d.fillOval((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);

        if (sprite != null) {
            g2d.drawImage(sprite, (int) (x - sprite.getWidth() / 2.0), (int) (y - sprite.getHeight() / 2.0), null);
        }

        if (camo && (sprite == null || sprite == SpriteManager.getSprite(""))) { // Check if placeholder is used
            g2d.setColor(Color.DARK_GRAY);
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
        // updateSpriteVisual(); // Uncomment if you have sprites for different health states
    }

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

    public double getDistanceToWaypoint(Point waypoint) {
        if (waypoint == null) return Double.MAX_VALUE;
        double dx = waypoint.getX() - this.x;
        double dy = waypoint.getY() - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}