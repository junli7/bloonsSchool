import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.AffineTransform; // For rotation
import java.awt.image.BufferedImage;

public class Monkey {
    protected int x;
    protected int y;
    protected double range;
    protected double hitbox; // Used for collision and as base for sprite scaling
    protected int upgradeCost;
    protected int level;
    protected List<Projectile> projectiles;
    protected Color monkeyColor = Color.ORANGE; // Fallback color

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
    protected transient BufferedImage idleSprite; // Made transient for potential serialization
    protected transient BufferedImage shootingSprite;
    protected String idleSpritePath = "monkey_default_idle.png"; // Default paths
    protected String shootingSpritePath = "monkey_default_shoot.png"; // Default paths

    protected boolean isAnimatingShot = false;
    protected long shotAnimationEndTime = 0;
    protected static final long SHOT_ANIMATION_DURATION_MS = 200; // 0.2 seconds

    protected double lastShotAngleRadians = 0; // Angle in radians for rotation

    // This 'sprite' field from before is no longer the primary one;
    // idleSprite and shootingSprite are. We'll use a getter or logic to pick one.
    // For simplicity, we can still have a 'currentActualSprite' or update one of them.
    // Let's make it simple: the draw method will choose.

    public Monkey(int nx, int ny, double nrange, double nhitbox, int nlevel) {
        this.x = nx;
        this.y = ny;
        this.range = nrange;
        this.hitbox = nhitbox;
        this.level = nlevel;
        this.projectiles = new ArrayList<>();

        // Initialize sprite paths (subclasses can override these strings *before* super() or loadSprites())
        // For this example, subclasses will set their paths and then call loadSprites.
        // Or, subclasses can set idleSpritePath/shootingSpritePath in their constructor *before* calling loadSprites.

        loadSprites(); // Load initial sprites based on current paths

        // Default projectile properties
        this.monkeyColor = Color.ORANGE; // Fallback
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

    /**
     * Loads/reloads sprites based on current sprite paths and hitbox.
     * Should be called in constructor and after hitbox changes (e.g., in upgrade).
     */
    protected void loadSprites() {
        int spriteSize = (int) this.hitbox;
        if (spriteSize <= 0) spriteSize = 32; // Default small size if hitbox is invalid

        if (this.idleSpritePath != null && !this.idleSpritePath.isEmpty()) {
            this.idleSprite = SpriteManager.getScaledSprite(this.idleSpritePath, spriteSize, spriteSize);
        } else {
            this.idleSprite = SpriteManager.getPlaceholderSprite(); // Use placeholder if path is bad
        }

        if (this.shootingSpritePath != null && !this.shootingSpritePath.isEmpty()) {
            this.shootingSprite = SpriteManager.getScaledSprite(this.shootingSpritePath, spriteSize, spriteSize);
        } else {
            // If no shooting sprite path, use idle sprite or placeholder for shooting state too
            this.shootingSprite = this.idleSprite;
            if (this.shootingSprite == null) this.shootingSprite = SpriteManager.getPlaceholderSprite();
        }
    }


    private void calculateUpgradeCost() {
        this.upgradeCost = 50 + (this.level * 75);
    }

    public void upgrade() {
        this.level++;
        calculateUpgradeCost();
        this.range += 5;
        // Example: maybe hitbox increases slightly with upgrades for some monkeys
        
        if (this.level % 3 == 0) this.hitbox += 2; 

        this.projectileSpeed += 0.1;
        this.shootCooldown = Math.max(100, this.shootCooldown - 20);
        this.projectileDamage = 1 + (this.level / 2);

        loadSprites(); // Reload sprites in case hitbox changed or for general refresh

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
            g2d.setColor(new Color(150, 150, 150, 100)); // Range indicator
            g2d.fillOval((int) (x - range), (int) (y - range), (int) (range * 2), (int) (range * 2));
        }

        BufferedImage currentSprite = isAnimatingShot ? this.shootingSprite : this.idleSprite;
        if (currentSprite == null || currentSprite == SpriteManager.getPlaceholderSprite()) {
            // Fallback drawing if sprite is null or placeholder
            g2d.setColor(monkeyColor);
            g2d.fillOval((int) (x - hitbox / 2), (int) (y - hitbox / 2), (int) hitbox, (int) hitbox);
            if (currentSprite == SpriteManager.getPlaceholderSprite()){
                 g2d.drawImage(currentSprite, (int) (x - hitbox / 2.0), (int) (y - hitbox / 2.0), (int)hitbox, (int)hitbox, null);
            }
        } else {
            // Apply rotation
            AffineTransform oldTransform = g2d.getTransform(); // Save current transform
            double rotationAngle = this.lastShotAngleRadians + Math.PI / 2.0;

            // Rotate around the center of the monkey
            // The angle lastShotAngleRadians should be such that 0 is right, PI/2 is down, PI is left, -PI/2 is up
            // Your sprite should be designed to face "right" (0 radians) by default.
            g2d.rotate(rotationAngle, x, y);

            int spriteWidth = currentSprite.getWidth();
            int spriteHeight = currentSprite.getHeight();
            g2d.drawImage(currentSprite, (int) (x - spriteWidth / 2.0), (int) (y - spriteHeight / 2.0), null);

            g2d.setTransform(oldTransform); // Restore original transform
        }

        g2d.setColor(Color.BLACK);
        String levelText = "Lvl: " + level;
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(levelText);
        // Adjust text position if monkey rotates, or draw it unrotated
        // For simplicity, drawing unrotated for now:
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
        if (currentTime - lastShotTime < shootCooldown) {
            return;
        }

        Human currentTarget = findTarget(allHumans, mapPath);
        if (currentTarget != null) {
            shootAtTarget(currentTarget);
        }
    }

    protected Human findTarget(List<Human> allHumans, ArrayList<Point> mapPath) {
        // ... (findTarget logic remains the same)
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
                currentHumanProgress += (1.0 - Math.min(1.0, distToWaypoint / (this.range / 2.0) ) );
            }

            if (currentHumanProgress > maxProgress) {
                maxProgress = currentHumanProgress;
                bestTarget = human;
            }
        }
        return bestTarget;
    }

    /**
     * Handles shooting logic: sets animation, calculates angle, and fires projectile.
     * This can now be common for all monkeys.
     */
    protected void shootAtTarget(Human target) {
        if (target == null || isAnimatingShot) {
             // Don't shoot if no target or already mid-animation to prevent re-triggering issues.
             // Or, you could allow re-targeting, but that adds complexity.
            return;
        }

        // Calculate angle to target for rotation
        double deltaX = target.getX() - this.x;
        double deltaY = target.getY() - this.y;
        this.lastShotAngleRadians = Math.atan2(deltaY, deltaX); // atan2 gives angle in radians from -PI to PI

        // Trigger animation
        this.isAnimatingShot = true;
        // The draw method will pick shootingSprite based on isAnimatingShot
        this.shotAnimationEndTime = System.currentTimeMillis() + SHOT_ANIMATION_DURATION_MS;

        fireProjectile(target);
    }

    protected void fireProjectile(Human target) {
        if (target == null) return;
        Projectile newProjectile = new Projectile( /* ... all projectile parameters ... */
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
            // The draw method will switch back to idleSprite automatically
        }
    }

    // ... (contains, setSelected, getters/setters remain mostly the same) ...
    public boolean contains(int pX, int pY) {
        double distanceSquared = Math.pow(pX - this.x, 2) + Math.pow(pY - this.y, 2);
        double radiusSquared = Math.pow(this.hitbox / 2.0, 2);
        return distanceSquared <= radiusSquared;
    }

    public void setSelected(boolean selected) { this.isSelected = selected; }
    public boolean isSelected() { return isSelected; }
    public void setColor(Color m, Color p) { monkeyColor = m; projectileColor = p; }
    public void setProjectileSpeed(double s) { projectileSpeed = s; }
    public void setProjectileRadius(int s) { projectileRadius = s; }
    public void setShootCooldown(long s) { shootCooldown = s; }
    public int getX() { return x; }
    public int getY() { return y; }
    public double getRange() { return range; }
    public double getHitbox() { return hitbox; }
    public int getUpgradeCost() { return upgradeCost; }
    public int getLevel() { return level; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public boolean canDetectCamo() { return canSeeCamo; }
}