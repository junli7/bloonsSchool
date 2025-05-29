import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Monkey {
    protected int x;
    protected int y;
    protected double range;
    protected double hitbox;
    protected int upgradeCost;
    protected int level;
    protected List<Projectile> projectiles;
    protected Color monkeyColor = Color.ORANGE;

    public static final int COST = 75;

    public static final double DEFAULT_INITIAL_RANGE = 80.0;
    public static final double DEFAULT_INITIAL_HITBOX = 50.0;

    protected Color projectileColor = Color.RED;
    protected int projectileRadius = 10; // Visual radius of the projectile itself
    protected double projectileSpeed = 5.0;
    protected int projectileDamage;
    protected boolean projectileIsExplosive;
    protected double projectileAoeRadius; // For explosive damage/effect area
    protected Color projectileExplosionVisualColor = Color.ORANGE;
    protected int projectileExplosionVisualDuration = 20;
    protected String projectileExplosionSpritePath; // Sprite for the explosion visual

    protected long lastShotTime = 0;
    protected long shootCooldown = 500;
    protected boolean isSelected = false;
    protected boolean canSeeCamo = false;

    protected transient BufferedImage idleSprite;
    protected transient BufferedImage shootingSprite;
    protected String idleSpritePath = "monkey_base_idle.png";
    protected String shootingSpritePath = "monkey_base_shoot.png";

    protected boolean isAnimatingShot = false;
    protected long shotAnimationEndTime = 0;
    protected static final long SHOT_ANIMATION_DURATION_MS = 400;

    protected double lastShotAngleRadians = 0;

    public Monkey(int nx, int ny, int nlevel) {
        this.x = nx;
        this.y = ny;
        this.level = nlevel;

        this.range = DEFAULT_INITIAL_RANGE + ((nlevel - 1) * 5);
        this.hitbox = DEFAULT_INITIAL_HITBOX;

        this.projectiles = new ArrayList<>();
        loadSprites();

        // Default projectile properties for a standard Monkey
        this.monkeyColor = Color.ORANGE; // Fallback if sprites fail
        this.projectileColor = Color.RED; // Fallback for projectile if its sprite fails
        this.projectileRadius = 5; // Visual size of the dart/projectile
        this.projectileSpeed = 5.0;
        this.projectileDamage = 1 + (this.level / 2); // Standard monkey does damage
        this.projectileIsExplosive = false; // Standard monkey is not explosive
        this.projectileAoeRadius = 0.0;
        this.projectileExplosionVisualColor = Color.ORANGE; // Not used if not explosive
        this.projectileExplosionVisualDuration = 20;    // Not used if not explosive
        this.projectileExplosionSpritePath = null;      // No explosion sprite if not explosive

        calculateUpgradeCost();
        if (this.level > 2) {
            this.canSeeCamo = true;
        }
    }

    protected void loadSprites() {
        int spriteSize = (int) this.hitbox;
        if (spriteSize <= 0) spriteSize = (int) DEFAULT_INITIAL_HITBOX;

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
        this.range += 5;
        this.projectileSpeed += 0.1;
        this.shootCooldown = Math.max(100, this.shootCooldown - 20);
        this.projectileDamage = 1 + (this.level / 2);

        loadSprites();

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
            double rotationAngle = this.lastShotAngleRadians + Math.PI / 2.0;
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
                double totalDistToNextWaypoint = this.range; // Default if at first waypoint
                if(human.getCurrentPathIndex() > 0) {
                    Point prevWaypoint = mapPath.get(human.getCurrentPathIndex()-1);
                    totalDistToNextWaypoint = prevWaypoint.distance(nextWaypoint);
                }
                if(totalDistToNextWaypoint == 0) totalDistToNextWaypoint = 1;

                double distToWaypoint = human.getDistanceToWaypoint(nextWaypoint);
                currentHumanProgress += (1.0 - Math.min(1.0, distToWaypoint / totalDistToNextWaypoint ));
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
        fireProjectile(target); // This will now call the updated fireProjectile
    }

    /**
     * Creates and fires a projectile at the target.
     * This base version fires a non-slowing projectile.
     * Subclasses (like MonkeyC) that fire slowing projectiles MUST override this method.
     * @param target The Human to target.
     */
    protected void fireProjectile(Human target) {
        if (target == null) return;
        Projectile newProjectile = new Projectile(
                this.x, this.y, target,
                this.projectileSpeed, this.projectileRadius, this.projectileColor,
                this.projectileDamage,
                this.projectileIsExplosive, this.projectileAoeRadius,
                this.projectileExplosionVisualColor, this.projectileExplosionVisualDuration,
                this.projectileExplosionSpritePath,
                // Default values for slowing parameters for base Monkey & MonkeyB (unless overridden)
                false, // isSlowing
                0      // slowDurationMillis
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

    public double getRange() { return range; }
    public double getHitbox() { return hitbox; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getUpgradeCost() { return upgradeCost; }
    public int getLevel() { return level; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public boolean canDetectCamo() { return canSeeCamo; }
    public boolean isSelected() { return isSelected; }

    public void setSelected(boolean selected) { this.isSelected = selected; }
    public void setColor(Color m, Color p) { monkeyColor = m; projectileColor = p; }
    public void setProjectileSpeed(double s) { projectileSpeed = s > 0 ? s : this.projectileSpeed; }
    public void setProjectileRadius(int s) { projectileRadius = s > 0 ? s : this.projectileRadius; }
    public void setShootCooldown(long s) { shootCooldown = s > 0 ? s : this.shootCooldown; }
    public void setRange(double newRange) { if (newRange > 0) this.range = newRange; }

    public void setHitbox(double newHitbox) {
        if (newHitbox > 0) {
            this.hitbox = newHitbox;
            loadSprites();
        }
    }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}