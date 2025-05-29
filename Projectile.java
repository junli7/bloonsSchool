import java.awt.Color;
import java.awt.Graphics2D;
// import java.awt.Rectangle; // Not directly used
import java.util.List;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.image.BufferedImage;
// import java.awt.Image; // Not directly used

public class Projectile {
    private double x, y;
    private double dx, dy;
    private int radius;
    private Color color;
    private double speed;
    private int damage; // Will be 0 for MonkeyC's projectiles
    private Human target;

    private boolean isExplosive;
    private double aoeRadius;

    // New fields for slowing effect
    private boolean isSlowingProjectile;
    private int slowDurationMillis; // Duration of the slow effect

    private enum State { ACTIVE, EXPLODING, SPENT }
    private State currentState = State.ACTIVE;

    private int explosionAgeTicks = 0;
    private int explosionDurationTicks = 20;
    private Color explosionVisualColor = Color.ORANGE;
    private double explosionCenterX, explosionCenterY;
    private BufferedImage explosionSprite;

    private BufferedImage sprite;
    private static final String DEFAULT_PROJECTILE_SPRITE_PATH = "projectile_dart.png";
    private static final String BOMB_PROJECTILE_SPRITE_PATH = "projectile_bomb.png";
    private static final String SLOW_PROJECTILE_SPRITE_PATH = "projectile_ice.png"; // Example sprite for slowing

    private static final double HIT_THRESHOLD_DISTANCE = 5.0;

    // MODIFIED CONSTRUCTOR to include slowing parameters
    public Projectile(double startX, double startY, Human targetHuman, double speed, int projectileVisualRadius,
                      Color projectileColor, int damage, boolean isExplosive, double aoeRadius,
                      Color explosionVisualEffectColor, int explosionVisualEffectDuration,
                      String projectileExplosionSpritePath,
                      boolean isSlowing, int slowDurationMillis) { // Added slowing parameters
        this.x = startX;
        this.y = startY;
        this.target = targetHuman;
        this.speed = speed;
        this.radius = projectileVisualRadius;
        this.color = projectileColor;
        this.damage = damage; // Will be 0 for MonkeyC
        this.isExplosive = isExplosive;
        this.aoeRadius = aoeRadius;

        this.isSlowingProjectile = isSlowing;
        this.slowDurationMillis = slowDurationMillis;

        String projectileOwnSpritePath;

        if (this.isSlowingProjectile) { // Prioritize slowing sprite if applicable
            projectileOwnSpritePath = SLOW_PROJECTILE_SPRITE_PATH;
            // Slowing projectiles might also be explosive (e.g., an ice bomb)
            if (this.isExplosive) {
                 this.explosionVisualColor = explosionVisualEffectColor;
                 this.explosionDurationTicks = explosionVisualEffectDuration;
                 if (projectileExplosionSpritePath != null && !projectileExplosionSpritePath.isEmpty()) {
                    int explosionSpriteSize = (int) (this.aoeRadius * 2);
                    if (explosionSpriteSize > 0) {
                        this.explosionSprite = SpriteManager.getScaledSprite(projectileExplosionSpritePath, explosionSpriteSize, explosionSpriteSize);
                    } else {
                        this.explosionSprite = SpriteManager.getSprite(projectileExplosionSpritePath);
                         if(this.explosionSprite == SpriteManager.getPlaceholderSprite()){ this.explosionSprite = null; }
                    }
                } else { this.explosionSprite = null; }
            } else { this.explosionSprite = null; } // Non-explosive slowing projectile
        }
        else if (this.isExplosive) {
            this.explosionVisualColor = explosionVisualEffectColor;
            this.explosionDurationTicks = explosionVisualEffectDuration;
            projectileOwnSpritePath = BOMB_PROJECTILE_SPRITE_PATH;
            if (projectileExplosionSpritePath != null && !projectileExplosionSpritePath.isEmpty()) {
                int explosionSpriteSize = (int) (this.aoeRadius * 2);
                if (explosionSpriteSize > 0) {
                    this.explosionSprite = SpriteManager.getScaledSprite(projectileExplosionSpritePath, explosionSpriteSize, explosionSpriteSize);
                } else {
                    this.explosionSprite = SpriteManager.getSprite(projectileExplosionSpritePath);
                     if(this.explosionSprite == SpriteManager.getPlaceholderSprite()){ this.explosionSprite = null; }
                }
            } else { this.explosionSprite = null; }
        } else {
            projectileOwnSpritePath = DEFAULT_PROJECTILE_SPRITE_PATH;
            this.explosionSprite = null;
        }

        int projectileSpriteDiameter = this.radius * 2;
        if (projectileSpriteDiameter > 0) {
            this.sprite = SpriteManager.getScaledSprite(projectileOwnSpritePath, projectileSpriteDiameter, projectileSpriteDiameter);
        } else {
             this.sprite = SpriteManager.getSprite(projectileOwnSpritePath);
             if(this.sprite == SpriteManager.getPlaceholderSprite()){ this.sprite = null; }
        }

        if (this.target != null) {
            calculateDirectionToTarget();
        } else {
            this.dx = 0; this.dy = 0; this.currentState = State.SPENT;
        }
    }

    private void calculateDirectionToTarget() {
        // ... (no change here)
        if (target == null || currentState != State.ACTIVE || !target.isAlive()) {
            this.dx = 0;
            this.dy = 0;
            return;
        }
        double targetX = target.getX();
        double targetY = target.getY();
        double deltaX = targetX - this.x;
        double deltaY = targetY - this.y;
        double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distanceToTarget > 0) {
            this.dx = (deltaX / distanceToTarget) * speed;
            this.dy = (deltaY / distanceToTarget) * speed;
        } else {
            this.dx = 0;
            this.dy = 0;
        }
    }

    public boolean updateAndCheckRemoval(List<Human> allHumans) {
        if (currentState == State.SPENT) return true;

        if (currentState == State.EXPLODING) {
            explosionAgeTicks++;
            if (explosionAgeTicks >= explosionDurationTicks) {
                currentState = State.SPENT; return true;
            }
            return false;
        }

        if (target == null || !target.isAlive()) {
            currentState = State.SPENT; return true;
        }

        calculateDirectionToTarget();
        this.x += dx;
        this.y += dy;

        double targetCurrentX = target.getX();
        double targetCurrentY = target.getY();
        double distanceToTargetPos = Math.sqrt(Math.pow(targetCurrentX - this.x, 2) + Math.pow(targetCurrentY - this.y, 2));

        if (distanceToTargetPos < HIT_THRESHOLD_DISTANCE || distanceToTargetPos < this.speed) {
            this.explosionCenterX = this.x;
            this.explosionCenterY = this.y;

            if (isExplosive && aoeRadius > 0) {
                for (Human humanInRange : allHumans) {
                    if (humanInRange.isAlive()) {
                        double distToImpactCenter = Math.sqrt(Math.pow(humanInRange.getX() - this.explosionCenterX, 2) + Math.pow(humanInRange.getY() - this.explosionCenterY, 2));
                        if (distToImpactCenter <= this.aoeRadius) {
                            if (this.damage > 0) humanInRange.takeDamage(this.damage); // Apply damage if any
                            if (this.isSlowingProjectile) humanInRange.applySlow(this.slowDurationMillis); // Apply slow
                        }
                    }
                }
                currentState = State.EXPLODING;
                return false;
            } else { // Single target hit (non-explosive, or explosive with 0 AoE)
                if (target.isAlive()) {
                    if (this.damage > 0) target.takeDamage(this.damage);
                    if (this.isSlowingProjectile) target.applySlow(this.slowDurationMillis);
                }
                currentState = State.SPENT;
                return true;
            }
        }
        return false;
    }

    public void draw(Graphics2D g2d) {
        // ... (drawing logic for explosion remains the same)
        // ... (drawing logic for active projectile sprite/color remains the same)
        if (currentState == State.EXPLODING) {
            float alpha = 1.0f - ((float) explosionAgeTicks / explosionDurationTicks);
            alpha = Math.max(0, Math.min(1, alpha));

            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(ac);

            if (this.explosionSprite != null && this.explosionSprite != SpriteManager.getPlaceholderSprite()) {
                int spriteWidth = this.explosionSprite.getWidth();
                int spriteHeight = this.explosionSprite.getHeight();
                g2d.drawImage( this.explosionSprite, (int) (explosionCenterX - spriteWidth / 2.0), (int) (explosionCenterY - spriteHeight / 2.0), null );
            } else {
                g2d.setColor(this.explosionVisualColor);
                int visualExplosionRadius = (int) this.aoeRadius;
                g2d.fillOval( (int) (explosionCenterX - visualExplosionRadius), (int) (explosionCenterY - visualExplosionRadius), (int) (visualExplosionRadius * 2), (int) (visualExplosionRadius * 2) );
            }
            g2d.setComposite(originalComposite);

        } else if (currentState == State.ACTIVE) {
            if (this.sprite != null && this.sprite != SpriteManager.getPlaceholderSprite()) {
                g2d.drawImage(sprite, (int) (x - this.sprite.getWidth() / 2.0), (int) (y - this.sprite.getHeight() / 2.0), null);
            } else {
                g2d.setColor(this.color);
                g2d.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
            }
        }
    }

    public boolean checkOffScreenAndMarkSpent(int screenWidth, int screenHeight) {
        // ... (no change here)
        if (currentState == State.ACTIVE &&
            (x < -radius * 2 || x > screenWidth + radius * 2 ||
             y < -radius * 2 || y > screenHeight + radius * 2)) {
            currentState = State.SPENT;
            return true;
        }
        return false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}