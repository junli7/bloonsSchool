import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.image.BufferedImage;

public class Projectile {
    private double x, y;
    private double dx, dy;
    private int radius; // Visual radius of the projectile itself
    private Color color; // Fallback color
    private double speed;
    private int damage;
    private Human target;

    private boolean isExplosive;
    private double aoeRadius;

    private boolean isSlowingProjectile;
    private int slowDurationMillis;

    private enum State { ACTIVE, EXPLODING, SPENT }
    private State currentState = State.ACTIVE;

    private int explosionAgeTicks = 0;
    private int explosionDurationTicks = 20; // Default, can be overridden
    private Color explosionVisualColor = Color.ORANGE; // Default
    private double explosionCenterX, explosionCenterY;
    private BufferedImage explosionSprite; // Sprite for the explosion visual effect

    private BufferedImage sprite; // The sprite for the flying projectile
    // Define paths for different projectile types
    private static final String DEFAULT_PROJECTILE_SPRITE_PATH = "projectile_dart.png";
    private static final String BOMB_PROJECTILE_SPRITE_PATH = "projectile_bomb.png";
    private static final String SLOW_PROJECTILE_SPRITE_PATH = "project_slow.png"; // Renamed for clarity: this is the flying slow projectile
                                                                                        // Your MonkeyC used "project_slow.png" for explosion,
                                                                                        // this should be different if the flying part is different.

    private static final double HIT_THRESHOLD_DISTANCE = 5.0; // How close to target to register a hit

    public Projectile(double startX, double startY, Human targetHuman, double speed, int projectileVisualRadius,
                      Color projectileColor, int damage, boolean isExplosive, double aoeRadius,
                      Color explosionVisualEffectColor, int explosionVisualEffectDuration,
                      String projectileExplosionSpritePath, // Sprite for the *explosion* animation
                      boolean isSlowing, int slowDurationMillis) {
        this.x = startX;
        this.y = startY;
        this.target = targetHuman;
        this.speed = speed;
        this.radius = projectileVisualRadius; // This is for the fallback circle and can guide sprite scaling
        this.color = projectileColor; // Fallback color if sprite fails
        this.damage = damage;
        this.isExplosive = isExplosive;
        this.aoeRadius = aoeRadius;

        this.isSlowingProjectile = isSlowing;
        this.slowDurationMillis = slowDurationMillis;

        this.explosionVisualColor = explosionVisualEffectColor; // Set regardless, used if no explosion sprite
        this.explosionDurationTicks = explosionVisualEffectDuration;

        // Determine the sprite for the FLYING projectile
        String flyingProjectileSpritePath;
        if (this.isSlowingProjectile) {
            flyingProjectileSpritePath = SLOW_PROJECTILE_SPRITE_PATH;
        } else if (this.isExplosive) { // Non-slowing but explosive (e.g., MonkeyB's bombs)
            flyingProjectileSpritePath = BOMB_PROJECTILE_SPRITE_PATH;
        } else { // Standard non-slowing, non-explosive projectile (e.g., Monkey's darts)
            flyingProjectileSpritePath = DEFAULT_PROJECTILE_SPRITE_PATH;
        }

        // Load the FLYING projectile sprite
        int projectileSpriteDiameter = this.radius * 2; // Use visual radius to hint at sprite size
        if (projectileSpriteDiameter <=0 ) projectileSpriteDiameter = 10; // Default small size if radius is 0

        this.sprite = SpriteManager.getScaledSprite(flyingProjectileSpritePath, projectileSpriteDiameter, projectileSpriteDiameter);
        if (this.sprite == SpriteManager.getPlaceholderSprite()) {
            System.err.println("Warning: Placeholder used for flying projectile: " + flyingProjectileSpritePath);
            // Keep the placeholder, draw() will handle it or use fallback color
        }


        // Load the EXPLOSION sprite (if applicable and path provided)
        if (this.isExplosive && projectileExplosionSpritePath != null && !projectileExplosionSpritePath.isEmpty()) {
            int explosionSpriteSize = (int) (this.aoeRadius * 2); // Explosion visual tied to AoE radius
            if (explosionSpriteSize <= 0) explosionSpriteSize = 30; // Default if AoE is tiny

            this.explosionSprite = SpriteManager.getScaledSprite(projectileExplosionSpritePath, explosionSpriteSize, explosionSpriteSize);
            if (this.explosionSprite == SpriteManager.getPlaceholderSprite()) {
                System.err.println("Warning: Placeholder used for explosion sprite: " + projectileExplosionSpritePath);
                this.explosionSprite = null; // Don't use placeholder for explosion, fall back to colored circle
            }
        } else {
            this.explosionSprite = null; // No explosion sprite if not explosive or no path
        }


        if (this.target != null) {
            calculateDirectionToTarget();
        } else {
            this.dx = 0; this.dy = 0; this.currentState = State.SPENT; // Mark as spent if no target
        }
    }

    private void calculateDirectionToTarget() {
        if (target == null || currentState != State.ACTIVE || !target.isAlive()) {
            this.dx = 0; this.dy = 0; // Stop if target is gone or invalid
            // Optionally, could mark as spent here: this.currentState = State.SPENT;
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
        } else { // Already at target (unlikely with double precision but handle it)
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
            return false; // Still exploding, don't remove yet
        }

        // If target is lost while projectile is active
        if (target == null || !target.isAlive()) {
            currentState = State.SPENT;
            return true;
        }

        calculateDirectionToTarget(); // Recalculate in case target moves (homing)
        this.x += dx;
        this.y += dy;

        double targetCurrentX = target.getX();
        double targetCurrentY = target.getY();
        double distanceToTargetPos = Math.sqrt(Math.pow(targetCurrentX - this.x, 2) + Math.pow(targetCurrentY - this.y, 2));

        // Check for hit
        if (distanceToTargetPos < HIT_THRESHOLD_DISTANCE || distanceToTargetPos < this.speed) { // Hit!
            this.explosionCenterX = this.x; // Impact point for AoE
            this.explosionCenterY = this.y;

            if (isExplosive && aoeRadius > 0) {
                // Apply AoE effects
                for (Human humanInRange : allHumans) {
                    if (humanInRange.isAlive()) {
                        double distToImpactCenter = Math.sqrt(Math.pow(humanInRange.getX() - this.explosionCenterX, 2) + Math.pow(humanInRange.getY() - this.explosionCenterY, 2));
                        if (distToImpactCenter <= this.aoeRadius) {
                            if (this.damage > 0) humanInRange.takeDamage(this.damage);
                            if (this.isSlowingProjectile) humanInRange.applySlow(this.slowDurationMillis);
                        }
                    }
                }
                currentState = State.EXPLODING; // Start explosion animation
                return false; // Don't remove yet, let explosion animate
            } else { // Single target hit (non-explosive, or explosive with 0 AoE)
                if (target.isAlive()) { // Target might have died from another source
                    if (this.damage > 0) target.takeDamage(this.damage);
                    if (this.isSlowingProjectile) target.applySlow(this.slowDurationMillis);
                }
                currentState = State.SPENT; // Hit processed, mark as spent
                return true; // Remove projectile
            }
        }
        return false; // Still active, not hit, not off-screen yet
    }

    public void draw(Graphics2D g2d) {
        if (currentState == State.EXPLODING) {
            float alpha = 1.0f - ((float) explosionAgeTicks / explosionDurationTicks);
            alpha = Math.max(0, Math.min(1, alpha)); // Clamp alpha between 0 and 1

            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(ac);

            if (this.explosionSprite != null) { // Check if we have a specific explosion sprite
                int spriteWidth = this.explosionSprite.getWidth();
                int spriteHeight = this.explosionSprite.getHeight();
                g2d.drawImage( this.explosionSprite, (int) (explosionCenterX - spriteWidth / 2.0), (int) (explosionCenterY - spriteHeight / 2.0), null );
            } else { // Fallback to drawing a colored circle for explosion
                g2d.setColor(this.explosionVisualColor);
                int visualExplosionRadius = (int) this.aoeRadius;
                g2d.fillOval( (int) (explosionCenterX - visualExplosionRadius), (int) (explosionCenterY - visualExplosionRadius), (int) (visualExplosionRadius * 2), (int) (visualExplosionRadius * 2) );
            }
            g2d.setComposite(originalComposite); // Restore original composite

        } else if (currentState == State.ACTIVE) {
            // Draw the FLYING projectile
            if (this.sprite != null && this.sprite != SpriteManager.getPlaceholderSprite()) {
                // Assuming projectile sprites are designed to be centered on x,y
                g2d.drawImage(this.sprite, (int) (x - this.sprite.getWidth() / 2.0), (int) (y - this.sprite.getHeight() / 2.0), null);
            } else { // Fallback: draw a colored circle for the flying projectile
                g2d.setColor(this.color);
                g2d.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
            }
        }
        // If state is SPENT, nothing is drawn
    }

    public boolean checkOffScreenAndMarkSpent(int screenWidth, int screenHeight) {
        if (currentState == State.ACTIVE &&
            (x < -radius * 5 || x > screenWidth + radius * 5 || // Increased buffer for off-screen check
             y < -radius * 5 || y > screenHeight + radius * 5)) {
            currentState = State.SPENT;
            return true;
        }
        return false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}