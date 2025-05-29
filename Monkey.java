import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.AffineTransform; // For rotation
import java.awt.image.BufferedImage;

public class Monkey {
    protected int x;
    protected int y;
    protected double range; // No longer a constructor parameter
    protected double hitbox; // No longer a constructor parameter
    protected int upgradeCost;
    protected int level;
    protected List<Projectile> projectiles;
    protected Color monkeyColor = Color.ORANGE; // Fallback color

    // Default values for base Monkey
    public static final double DEFAULT_INITIAL_RANGE = 80.0;
    public static final double DEFAULT_INITIAL_HITBOX = 30.0;

    // Projectile specific properties
    protected Color projectileColor = Color.RED;
    protected int projectileRadius = 5;
    protected double projectileSpeed = 5.0;
    protected int projectileDamage;
    protected boolean projectileIsExplosive;
    protected double projectileAoeRadius;
    protected Color projectileExplosionVisualColor = Color.ORANGE;
    protected int projectileExplosionVisualDuration = 20;
    protected String projectileExplosionSpritePath;

    protected long lastShotTime = 0;
    protected long shootCooldown = 500; // Milliseconds
    protected boolean isSelected = false;
    protected boolean canSeeCamo = false;

    // Sprite and Animation Fields
    protected transient BufferedImage idleSprite;
    protected transient BufferedImage shootingSprite;
    protected String idleSpritePath = "monkey_base_idle.png";
    protected String shootingSpritePath = "monkey_base_shoot.png";

    protected boolean isAnimatingShot = false;
    protected long shotAnimationEndTime = 0;
    protected static final long SHOT_ANIMATION_DURATION_MS = 400; // 0.2 seconds

    protected double lastShotAngleRadians = 0;

    // MODIFIED CONSTRUCTOR: Removed nrange, nhitbox
    public Monkey(int nx, int ny, int nlevel) {
        this.x = nx;
        this.y = ny;
        this.level = nlevel;

        // Initialize with defaults
        this.range = DEFAULT_INITIAL_RANGE + ((nlevel -1) * 5); // Example: range increases slightly with initial level
        this.hitbox = DEFAULT_INITIAL_HITBOX; // Hitbox might be more fixed initially

        this.projectiles = new ArrayList<>();
        loadSprites(); // Load initial sprites based on current paths and default hitbox

        // Default projectile properties
        this.monkeyColor = Color.ORANGE;
        this.projectileColor = Color.RED;
        this.projectileRadius = 5;
        this.projectileSpeed = 5.0;
        this.projectileDamage = 1 + (this.level / 2);
        this.projectileIsExplosive = false;
        this.projectileAoeRadius = 0.0;
        this.projectileExplosionVisualColor = Color.ORANGE;
        this.projectileExplosionVisualDuration = 20;
        this.projectileExplosionSpritePath = null;

        calculateUpgradeCost();
        if (this.level > 2) {
            this.canSeeCamo = true;
        }
    }

    protected void loadSprites() {
        int spriteSize = (int) this.hitbox;
        if (spriteSize <= 0) spriteSize = 32;

        if (this.idleSpritePath != null && !this.idleSpritePath.isEmpty()) {
            this.idleSprite = SpriteManager.getScaledSprite(this.idleSpritePath, spriteSize, spriteSize);
        } else {
            this.idleSprite = SpriteManager.getPlaceholderSprite();
        }

        if (this.shootingSpritePath != null && !this.shootingSpritePath.isEmpty()) {
            this.shootingSprite = SpriteManager.getScaledSprite(this.shootingSpritePath, spriteSize, spriteSize);
        } else {
            this.shootingSprite = this.idleSprite != null ? this.idleSprite : SpriteManager.getPlaceholderSprite();
        }
    }

    private void calculateUpgradeCost() {
        this.upgradeCost = 50 + (this.level * 75);
    }

    public void upgrade() {
        this.level++;
        calculateUpgradeCost();
        this.range += 5; // Example: range increases with each upgrade
        // this.hitbox might also change for some upgrades, e.g. this.hitbox +=2;
        this.projectileSpeed += 0.1;
        this.shootCooldown = Math.max(100, this.shootCooldown - 20);
        this.projectileDamage = 1 + (this.level / 2);

        loadSprites(); // Reload sprites in case hitbox changed

        if (this.level > 2 && !this.canSeeCamo) {
            this.canSeeCamo = true;
            System.out.println(this.getClass().getSimpleName() + " gained camo detection!");
        }
        System.out.println(this.getClass().getSimpleName() + " at (" + x + "," + y + ") upgraded to level " + this.level +
                ". New range: " + this.range + ", Damage: " + this.projectileDamage +
                ", Next upgrade cost: " + this.upgradeCost);
    }

    public void draw(Graphics2D g2d) {
        if (isSelected) {
            g2d.setColor(new Color(150, 150, 150, 100));
            g2d.fillOval((int) (x - range), (int) (y - range), (int) (range * 2), (int) (range * 2));
        }

        BufferedImage currentSprite = isAnimatingShot ? this.shootingSprite : this.idleSprite;
        if (currentSprite == null || currentSprite == SpriteManager.getPlaceholderSprite()) {
            g2d.setColor(monkeyColor);
            g2d.fillOval((int) (x - hitbox / 2), (int) (y - hitbox / 2), (int) hitbox, (int) hitbox);
            if (currentSprite == SpriteManager.getPlaceholderSprite()){
                 g2d.drawImage(currentSprite, (int) (x - hitbox / 2.0), (int) (y - hitbox / 2.0), (int)hitbox, (int)hitbox, null);
            }
        } else {
            AffineTransform oldTransform = g2d.getTransform();
            double rotationAngle = this.lastShotAngleRadians + Math.PI / 2.0; // For UP-facing sprites
            g2d.rotate(rotationAngle, x, y);
            int spriteWidth = currentSprite.getWidth();
            int spriteHeight = currentSprite.getHeight();
            g2d.drawImage(currentSprite, (int) (x - spriteWidth / 2.0), (int) (y - spriteHeight / 2.0), null);
            g2d.setTransform(oldTransform);
        }

        g2d.setColor(Color.BLACK);
        String levelText = "Lvl: " + level;
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(levelText);
        g2d.drawString(levelText, x - textWidth / 2, y + (int) (hitbox / 2) + fm.getAscent() + 2);

        List<Projectile> projectilesToDraw = new ArrayList<>(projectiles);
        for (Projectile p : projectilesToDraw) {
            p.draw(g2d);
        }
    }

    public void updateAndTarget(int screenWidth, int screenHeight, List<Human> allHumans, ArrayList<Point> mapPath) {
        updateAnimationState();
        projectiles.removeIf(p -> p.updateAndCheckRemoval(allHumans) || p.checkOffScreenAndMarkSpent(screenWidth, screenHeight));
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < shootCooldown) return;
        Human currentTarget = findTarget(allHumans, mapPath);
        if (currentTarget != null) shootAtTarget(currentTarget);
    }

    protected Human findTarget(List<Human> allHumans, ArrayList<Point> mapPath) {
        Human bestTarget = null;
        double maxProgress = -1.0;
        for (Human human : allHumans) {
            if (!human.isAlive() || human.hasReachedEnd()) continue;
            if (human.isCamo() && !this.canSeeCamo) continue;
            double distanceToHuman = Math.sqrt(Math.pow(human.getX() - this.x, 2) + Math.pow(human.getY() - this.y, 2));
            if (distanceToHuman > this.range) continue;
            double currentHumanProgress = human.getCurrentPathIndex();
            if (mapPath != null && !mapPath.isEmpty() && human.getCurrentPathIndex() < mapPath.size()) {
                Point nextWaypoint = mapPath.get(human.getCurrentPathIndex());
                double distToWaypoint = human.getDistanceToWaypoint(nextWaypoint);
                currentHumanProgress += (1.0 - Math.min(1.0, distToWaypoint / (this.range * 0.8) ) ); // Adjusted heuristic slightly
            }
            if (currentHumanProgress > maxProgress) {
                maxProgress = currentHumanProgress;
                bestTarget = human;
            }
        }
        return bestTarget;
    }

    protected void shootAtTarget(Human target) {
        if (target == null || isAnimatingShot) return;
        double deltaX = target.getX() - this.x;
        double deltaY = target.getY() - this.y;
        this.lastShotAngleRadians = Math.atan2(deltaY, deltaX);
        this.isAnimatingShot = true;
        this.shotAnimationEndTime = System.currentTimeMillis() + SHOT_ANIMATION_DURATION_MS;
        fireProjectile(target);
    }

    protected void fireProjectile(Human target) {
        if (target == null) return;
        Projectile newProjectile = new Projectile(
                this.x, this.y, target, this.projectileSpeed, this.projectileRadius,
                this.projectileColor, this.projectileDamage, this.projectileIsExplosive,
                this.projectileAoeRadius, this.projectileExplosionVisualColor,
                this.projectileExplosionVisualDuration, this.projectileExplosionSpritePath
        );
        projectiles.add(newProjectile);
        lastShotTime = System.currentTimeMillis();
    }

    protected void updateAnimationState() {
        if (isAnimatingShot && System.currentTimeMillis() >= shotAnimationEndTime) {
            isAnimatingShot = false;
        }
    }

    public boolean contains(int pX, int pY) {
        double distanceSquared = Math.pow(pX - this.x, 2) + Math.pow(pY - this.y, 2);
        return distanceSquared <= Math.pow(this.hitbox / 2.0, 2);
    }

    // GETTERS
    public double getRange() { return range; }
    public double getHitbox() { return hitbox; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getUpgradeCost() { return upgradeCost; }
    public int getLevel() { return level; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public boolean canDetectCamo() { return canSeeCamo; }
    public boolean isSelected() { return isSelected; }


    // SETTERS
    public void setSelected(boolean selected) { this.isSelected = selected; }
    public void setColor(Color m, Color p) { monkeyColor = m; projectileColor = p; } // Less relevant with sprites
    public void setProjectileSpeed(double s) { projectileSpeed = s > 0 ? s : this.projectileSpeed; }
    public void setProjectileRadius(int s) { projectileRadius = s > 0 ? s : this.projectileRadius; }
    public void setShootCooldown(long s) { shootCooldown = s > 0 ? s : this.shootCooldown; }

    public void setRange(double newRange) {
        if (newRange > 0) this.range = newRange;
    }

    public void setHitbox(double newHitbox) {
        if (newHitbox > 0) {
            this.hitbox = newHitbox;
            loadSprites(); // Crucial: reload sprites if hitbox changes as scale depends on it
        }
    }
}