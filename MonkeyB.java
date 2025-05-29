import java.awt.Color;

public class MonkeyB extends Monkey {

    public static final int COST = 150; // Cost to buy this monkey

    private double baseAoeRadius = 60.0;
    private int baseDamage = 2;

    public static final double monkeyBInitialRange = 80.0;
    public static final double monkeyBInitialHitbox = 80.0; // Slightly larger hitbox visually

    private static final String monkeyBIDLE_SPRITE_PATH = "monkey_bomber_idle.png";
    private static final String monkeyBSHOOT_SPRITE_PATH = "monkey_bomber_shoot.png";
    private static final String EXPLOSION_SPRITE_PATH = "explosion_effect.png";

    public MonkeyB(int nx, int ny, int nlevel) {
        super(nx, ny, nlevel); // Calls Monkey constructor

        // Override defaults for MonkeyB
        this.range = monkeyBInitialRange + ((nlevel - 1) * 4);
        this.hitbox = monkeyBInitialHitbox; // Set MonkeyB's specific hitbox

        // Set MonkeyB's specific sprite paths AFTER super() and AFTER setting its hitbox
        super.idleSpritePath = monkeyBIDLE_SPRITE_PATH;
        super.shootingSpritePath = monkeyBSHOOT_SPRITE_PATH;
        super.loadSprites(); // Reload with MonkeyB's specific paths AND its new hitbox

        // MonkeyB specific properties
        // setColor(Color.DARK_GRAY, Color.BLACK); // Less relevant if sprites are working
        setProjectileRadius(20); // Projectile visual size
        setProjectileSpeed(20.0); // Bombs are often slower
        setShootCooldown(1200); // Slower fire rate for bombs
        this.canSeeCamo = true; // Bomber can see camo by default (or make it an upgrade)

        this.projectileIsExplosive = true;
        this.projectileExplosionVisualColor = new Color(255, 100, 0, 180); // Orange-Red, semi-transparent
        this.projectileExplosionVisualDuration = 25; // Slightly longer explosion visual
        this.projectileExplosionSpritePath = EXPLOSION_SPRITE_PATH;

        updateMonkeyBProperties(); // Apply level-based damage and AoE
    }

    private void updateMonkeyBProperties() {
        this.projectileDamage = this.baseDamage + (this.level - 1); // Damage increases with level
        this.projectileAoeRadius = this.baseAoeRadius + ((this.level - 1) * 5); // AoE increases
    }

    @Override
    public void upgrade() {
        super.upgrade(); // Calls Monkey's upgrade, which updates base stats and calls loadSprites()
        updateMonkeyBProperties(); // Update MonkeyB specific properties like AoE radius and damage

        System.out.println("MonkeyB upgraded. New AoE: " + this.projectileAoeRadius + ", Damage: " + this.projectileDamage);
    }
}