import java.awt.Color;
// No need to import BufferedImage directly if not creating new instances here.
// Monkey.java handles idleSprite and shootingSprite fields.

public class MonkeyB extends Monkey {

    private double baseAoeRadius = 40.0;
    private int baseDamage = 2;

    // Sprite Paths specific to MonkeyB
    // These override the default paths from Monkey if set BEFORE super() or if constructor is adapted.
    // A cleaner way is to set them in the constructor.
    private static final String MONKEY_B_IDLE_SPRITE_PATH = "elephant.png";
    private static final String MONKEY_B_SHOOT_SPRITE_PATH = "monkey_bomber_shoot.png";
    private static final String EXPLOSION_SPRITE_PATH = "explosion_effect.png";

    public MonkeyB(int nx, int ny, double nrange, double nhitbox, int nlevel) {
        // Set specific paths BEFORE calling super constructor if super relies on them immediately,
        // OR set them after super() and then call loadSprites() if super() doesn't load them.
        // With the current Monkey constructor calling loadSprites(), we need to set paths first.
        // This is a bit tricky with inheritance. A common pattern is to pass paths to super.
        // Alternative:
        super(nx, ny, nrange, nhitbox, nlevel); // Calls Monkey constructor which calls loadSprites with default paths

        // NOW, override the paths and reload sprites specifically for MonkeyB
        super.idleSpritePath = MONKEY_B_IDLE_SPRITE_PATH;
        super.shootingSpritePath = MONKEY_B_SHOOT_SPRITE_PATH;
        super.loadSprites(); // Reload with MonkeyB's specific paths

        // MonkeyB specific properties
        setColor(Color.DARK_GRAY, Color.BLACK);
        setProjectileRadius(8);
        setProjectileSpeed(3.5);
        setShootCooldown(1200);
        this.canSeeCamo = true;

        this.projectileIsExplosive = true;
        this.projectileExplosionVisualColor = new Color(255, 100, 0);
        this.projectileExplosionVisualDuration = 25;
        this.projectileExplosionSpritePath = EXPLOSION_SPRITE_PATH;

        updateMonkeyBProperties();
    }

    private void updateMonkeyBProperties() {
        this.projectileDamage = this.baseDamage + (this.level - 1);
        this.projectileAoeRadius = this.baseAoeRadius + ((this.level - 1) * 5);
    }

    @Override
    public void upgrade() {
        super.upgrade(); // This calls loadSprites() in Monkey.java with current paths
        updateMonkeyBProperties();
        // No need to explicitly reload sprites here if super.upgrade() already calls loadSprites()
        // and the paths (idleSpritePath, shootingSpritePath) are correctly set for MonkeyB.
        // System.out.println just for confirmation.
         System.out.println("MonkeyB (" + x + "," + y + ") specific upgrade logic. Idle: " + super.idleSpritePath);
    }

    // MonkeyB no longer needs to override shootAtTarget or updateAnimationState
    // as the generic versions in Monkey.java handle it using the sprite paths.
    // It only needs to ensure its idleSpritePath and shootingSpritePath are set.
}