import java.awt.Color;

public class MonkeyB extends Monkey {

    private double baseAoeRadius = 40.0;
    private int baseDamage = 2;

    // MonkeyB specific default values if needed, or they can inherit from Monkey's defaults
    public static final double MONKEY_B_INITIAL_RANGE = 70.0;
    public static final double MONKEY_B_INITIAL_HITBOX = 100.0;

    // Sprite Paths specific to MonkeyB
    private static final String MONKEY_B_IDLE_SPRITE_PATH = "monkey_bomber_idle.png";
    private static final String MONKEY_B_SHOOT_SPRITE_PATH = "monkey_bomber_shoot.png";
    private static final String EXPLOSION_SPRITE_PATH = "human_normal.png"; // For projectile's explosion

    // MODIFIED CONSTRUCTOR
    public MonkeyB(int nx, int ny, int nlevel) {
        super(nx, ny, nlevel); // Calls Monkey constructor, which sets default range/hitbox & loads default sprites

        // Override defaults for MonkeyB if they are different
        this.range = MONKEY_B_INITIAL_RANGE + ((nlevel-1) * 4); // MonkeyB specific range scaling
        this.hitbox = MONKEY_B_INITIAL_HITBOX;

        // Set MonkeyB's specific sprite paths
        super.idleSpritePath = MONKEY_B_IDLE_SPRITE_PATH;
        super.shootingSpritePath = MONKEY_B_SHOOT_SPRITE_PATH;
        super.loadSprites(); // Reload with MonkeyB's specific paths AND its new hitbox

        // MonkeyB specific properties
        setColor(Color.DARK_GRAY, Color.BLACK); // Fallback color, projectile color
        setProjectileRadius(20);
        setProjectileSpeed(20);
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
        super.upgrade(); // This calls loadSprites() in Monkey.java after updating base stats
        updateMonkeyBProperties(); // Update MonkeyB specific properties like AoE

        // Note: super.upgrade() already calls loadSprites().
        // If MonkeyB's sprite paths are correctly set (which they are in the constructor and not changed by upgrade),
        // the sprites will be reloaded/rescaled correctly if the hitbox changed in super.upgrade().
        // System.out.println("MonkeyB (" + x + "," + y + ") upgraded. Idle path: " + super.idleSpritePath);
    }
}