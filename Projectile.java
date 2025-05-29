import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.image.BufferedImage;
import java.awt.Image;

public class Projectile {
    private double x, y;
    private double dx, dy;
    private int radius; // Visual radius of the base projectile (for its own sprite/shape)
    private Color color; // Projectile's own color (if not using sprite or for fallback)
    private double speed;
    private int damage;
    private Human target;

    private boolean isExplosive;
    private double aoeRadius; // Effective radius of the explosion damage and visual size

    private enum State { ACTIVE, EXPLODING, SPENT }
    private State currentState = State.ACTIVE;

    // Explosion specific visual properties
    private int explosionAgeTicks = 0;
    private int explosionDurationTicks = 20; // Default, can be overridden by monkey
    private Color explosionVisualColor = Color.ORANGE; // Fallback/tint for explosion visual
    private double explosionCenterX, explosionCenterY;
    private BufferedImage explosionSprite; // Sprite for the explosion visual effect

    // Projectile's own sprite
    private BufferedImage sprite;
    private static final String DEFAULT_PROJECTILE_SPRITE_PATH = "projectile_dart.png";
    private static final String BOMB_PROJECTILE_SPRITE_PATH = "projectile_bomb.png";

    private static final double HIT_THRESHOLD_DISTANCE = 5.0;

    public Projectile(double startX, double startY, Human targetHuman, double speed, int projectileVisualRadius,
                      Color projectileColor, int damage, boolean isExplosive, double aoeRadius,
                      Color explosionVisualEffectColor, int explosionVisualEffectDuration,
                      String projectileExplosionSpritePath) { // Note: parameter name changed slightly for clarity
        this.x = startX;
        this.y = startY;
        this.target = targetHuman;
        this.speed = speed;
        this.radius = projectileVisualRadius; // For the projectile's own visual size if not using sprite
        this.color = projectileColor;
        this.damage = damage;
        this.isExplosive = isExplosive;
        this.aoeRadius = aoeRadius; // For damage and explosion visual scaling

        String projectileOwnSpritePath; // Path for the projectile itself (dart, bomb)

        if (this.isExplosive) {
            this.explosionVisualColor = explosionVisualEffectColor; // Used for fallback circle or tint
            this.explosionDurationTicks = explosionVisualEffectDuration;
            projectileOwnSpritePath = BOMB_PROJECTILE_SPRITE_PATH; // Bomb projectile uses bomb sprite

            // Load the explosion effect sprite
            if (projectileExplosionSpritePath != null && !projectileExplosionSpritePath.isEmpty()) {
                // Scale the explosion sprite to the AOE diameter
                int explosionSpriteSize = (int) (this.aoeRadius * 2);
                if (explosionSpriteSize > 0) {
                    this.explosionSprite = SpriteManager.getScaledSprite(projectileExplosionSpritePath, explosionSpriteSize, explosionSpriteSize);
                } else {
                    // Handle case where aoeRadius might be zero or negative for some reason
                    this.explosionSprite = SpriteManager.getSprite(projectileExplosionSpritePath); // Load unscaled or let SpriteManager handle
                    if(this.explosionSprite == SpriteManager.getPlaceholderSprite()){
                         this.explosionSprite = null; // If placeholder due to bad path or 0 size, fallback to circle
                    }
                }
            } else {
                this.explosionSprite = null; // No path provided for explosion sprite
            }

        } else {
            projectileOwnSpritePath = DEFAULT_PROJECTILE_SPRITE_PATH; // Non-explosive uses default dart sprite
            this.explosionSprite = null; // Non-explosive projectiles don't have an explosion sprite
        }

        // Load the projectile's own sprite (dart or bomb image)
        // Scale it based on 'radius' which is meant for the projectile's visual size
        int projectileSpriteDiameter = this.radius * 2;
        if (projectileSpriteDiameter > 0) {
            this.sprite = SpriteManager.getScaledSprite(projectileOwnSpritePath, projectileSpriteDiameter, projectileSpriteDiameter);
        } else {
             this.sprite = SpriteManager.getSprite(projectileOwnSpritePath); // Load unscaled or placeholder
             if(this.sprite == SpriteManager.getPlaceholderSprite()){
                this.sprite = null; // Fallback to drawing a colored circle for projectile if its own sprite fails
             }
        }


        if (this.target != null) {
            calculateDirectionToTarget();
        } else {
            this.dx = 0;
            this.dy = 0;
            this.currentState = State.SPENT; // No target, projectile is immediately spent
        }
    }

    private void calculateDirectionToTarget() {
        if (target == null || currentState != State.ACTIVE || !target.isAlive()) { // Stop chasing dead targets
            this.dx = 0;
            this.dy = 0;
            // Optional: Mark as spent if target becomes invalid while projectile is active
            // if (currentState == State.ACTIVE) currentState = State.SPENT;
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
            // Already at target (or very close), prepare for impact
            this.dx = 0;
            this.dy = 0;
        }
    }

    public boolean updateAndCheckRemoval(List<Human> allHumans) {
        if (currentState == State.SPENT) {
            return true;
        }

        if (currentState == State.EXPLODING) {
            explosionAgeTicks++;
            if (explosionAgeTicks >= explosionDurationTicks) {
                currentState = State.SPENT;
                return true; // Explosion finished, remove projectile
            }
            return false; // Explosion ongoing, keep projectile
        }

        // ACTIVE state
        if (target == null || !target.isAlive()) { // Target lost or died before impact
            currentState = State.SPENT;
            return true;
        }

        calculateDirectionToTarget(); // Recalculate direction in case target moves
        this.x += dx;
        this.y += dy;

        double targetCurrentX = target.getX();
        double targetCurrentY = target.getY();
        double distanceToTargetPos = Math.sqrt(Math.pow(targetCurrentX - this.x, 2) + Math.pow(targetCurrentY - this.y, 2));

        // Check for hit: distance less than a threshold OR less than projectile's speed (to catch fast projectiles)
        if (distanceToTargetPos < HIT_THRESHOLD_DISTANCE || distanceToTargetPos < this.speed) {
            this.explosionCenterX = this.x; // Impact point for visual explosion
            this.explosionCenterY = this.y;

            if (isExplosive && aoeRadius > 0) {
                // Apply AOE damage
                for (Human humanInRange : allHumans) {
                    if (humanInRange.isAlive()) {
                        // Check distance from the *impact point* (explosionCenterX/Y) to other humans
                        double distToImpactCenter = Math.sqrt(Math.pow(humanInRange.getX() - this.explosionCenterX, 2) + Math.pow(humanInRange.getY() - this.explosionCenterY, 2));
                        if (distToImpactCenter <= this.aoeRadius) {
                            humanInRange.takeDamage(this.damage);
                        }
                    }
                }
                currentState = State.EXPLODING; // Transition to explosion visual state
                return false; // Don't remove yet, let explosion animation play
            } else {
                // Non-explosive hit, or explosive with no AOE (direct hit only)
                if (target.isAlive()) { // Target might have died from another source just now
                    target.takeDamage(this.damage);
                }
                currentState = State.SPENT; // Projectile is done
                return true; // Remove projectile
            }
        }
        return false; // Projectile still active and moving, not yet removed
    }

    public void draw(Graphics2D g2d) {
        if (currentState == State.EXPLODING) {
            // Calculate alpha for fade-out effect
            float alpha = 1.0f - ((float) explosionAgeTicks / explosionDurationTicks);
            alpha = Math.max(0, Math.min(1, alpha)); // Clamp alpha between 0 and 1

            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(ac);

            if (this.explosionSprite != null && this.explosionSprite != SpriteManager.getPlaceholderSprite()) {
                // Draw the explosion sprite
                int spriteWidth = this.explosionSprite.getWidth();
                int spriteHeight = this.explosionSprite.getHeight();
                g2d.drawImage(
                    this.explosionSprite,
                    (int) (explosionCenterX - spriteWidth / 2.0),
                    (int) (explosionCenterY - spriteHeight / 2.0),
                    null
                );
            } else {
                // Fallback to drawing the circle if sprite is null or placeholder
                g2d.setColor(this.explosionVisualColor); // Use the defined color
                int visualExplosionRadius = (int) this.aoeRadius; // Base size of explosion visual
                // You could add effects like shrinking/growing here if desired
                // e.g. int currentVisualRadius = (int) (this.aoeRadius * (1.0f - alpha)); // Shrinks as it fades
                g2d.fillOval(
                    (int) (explosionCenterX - visualExplosionRadius),
                    (int) (explosionCenterY - visualExplosionRadius),
                    (int) (visualExplosionRadius * 2),
                    (int) (visualExplosionRadius * 2)
                );
            }
            g2d.setComposite(originalComposite); // Restore original composite

        } else if (currentState == State.ACTIVE) {
            // Drawing the active projectile (e.g., dart or bomb model before impact)
            if (this.sprite != null && this.sprite != SpriteManager.getPlaceholderSprite()) {
                // Draw the projectile's own sprite
                g2d.drawImage(sprite, (int) (x - this.sprite.getWidth() / 2.0), (int) (y - this.sprite.getHeight() / 2.0), null);
            } else {
                // Fallback to drawing a simple circle for the projectile if its sprite is missing
                g2d.setColor(this.color);
                g2d.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
            }
        }
        // If currentState is SPENT, nothing is drawn
    }

    public boolean checkOffScreenAndMarkSpent(int screenWidth, int screenHeight) {
        if (currentState == State.ACTIVE &&
            (x < -radius * 2 || x > screenWidth + radius * 2 || // Increased off-screen buffer slightly
             y < -radius * 2 || y > screenHeight + radius * 2)) {
            currentState = State.SPENT;
            return true;
        }
        return false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}