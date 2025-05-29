import java.awt.Color;

public class MonkeyC extends Monkey { // Assuming it extends Monkey, can also extend MonkeyB if it shares bomb logic

    public static final int COST = 200; // Example cost
    private static final String MONKEY_C_IDLE_SPRITE_PATH = "monkey_slow_idle.png";   // Needs new sprite
    private static final String MONKEY_C_SHOOT_SPRITE_PATH = "monkey_slow_shoot.png"; // Needs new sprite
    // If MonkeyC's projectile is also explosive (e.g., an ice bomb that shatters)
    private static final String ICE_EXPLOSION_SPRITE_PATH = "ice_explosion_effect.png"; // Optional

    // Slowing specific properties
    private int slowDurationMillis = 1000; // 2 seconds
    private double baseAoeRadiusForSlow = 50.0; // If its slow is AoE

    public MonkeyC(int nx, int ny, int nlevel) {
        super(nx, ny, nlevel);

        // Override defaults for MonkeyC
        this.range = 75.0 + ((nlevel - 1) * 5); // Example range
        this.hitbox = 55.0; // Example hitbox

        super.idleSpritePath = MONKEY_C_IDLE_SPRITE_PATH;
        super.shootingSpritePath = MONKEY_C_SHOOT_SPRITE_PATH;
        super.loadSprites();

        // MonkeyC specific properties
        //setColor(Color.CYAN, new Color(100, 150, 255)); // Light blue projectile fallback
        setProjectileRadius(7); // Example
        setProjectileSpeed(4.0);  // Example
        setShootCooldown(800); // Example

        // --- Crucial for MonkeyC ---
        this.projectileDamage = 0; // Does no direct damage
        super.projectileIsExplosive = true; // Let's make it AoE slow
        super.projectileAoeRadius = this.baseAoeRadiusForSlow + ((this.level -1) * 3);
        super.projectileExplosionVisualColor = new Color(173, 216, 230, 150); // Light blue explosion
        super.projectileExplosionVisualDuration = 15; // Shorter visual for ice
        super.projectileExplosionSpritePath = ICE_EXPLOSION_SPRITE_PATH; // Optional specific explosion sprite

        // --- Set projectile to be slowing ---
        super.canSeeCamo = false; // Example: basic MonkeyC can't see camo, upgrade needed
        // These would be new fields in the base Monkey class if we want all monkeys to potentially slow
        // For now, we pass them directly to the Projectile constructor from MonkeyC's fireProjectile.
        // Or, add them as protected fields in Monkey.java:
        // protected boolean projectileIsSlowing = false;
        // protected int projectileSlowDurationMillis = 0;
        // Then set them here:
        // super.projectileIsSlowing = true;
        // super.projectileSlowDurationMillis = this.slowDurationMillis;

    }



    @Override
    public void upgrade() {
        this.level++;
        calculateUpgradeCost();
        this.slowDurationMillis+=2000;
        this.range += 10;
        this.projectileSpeed += 5;
        this.shootCooldown = Math.max(100, this.shootCooldown - 20);
        this.projectileAoeRadius+=100;
        if (this.level > 3) { // Example: camo detection at higher level
            this.canSeeCamo = true;
        }
        loadSprites();

 
        System.out.println(this.getClass().getSimpleName() + " at (" + x + "," + y + ") upgraded to level " + this.level +
                ". New range: " + this.range + ", Damage: " + this.projectileDamage +
                ", Next upgrade cost: " + this.upgradeCost);
        // Example: increase slow duration or AoE with upgrades
        // this.slowDurationMillis += 200;
        System.out.println("MonkeyC upgraded. AoE Slow Radius: " + this.projectileAoeRadius);
    }
    private void calculateUpgradeCost() {
        this.upgradeCost = 50 + (this.level * 75);
    }
    // We need to override fireProjectile to pass the slowing parameters
    @Override
    protected void fireProjectile(Human target) {
        if (target == null) return;
        Projectile newProjectile = new Projectile(
                this.x, this.y, target,
                this.projectileSpeed, this.projectileRadius, this.projectileColor,
                this.projectileDamage, // Will be 0
                this.projectileIsExplosive, this.projectileAoeRadius,
                this.projectileExplosionVisualColor, this.projectileExplosionVisualDuration,
                this.projectileExplosionSpritePath,
                true, // isSlowing = true
                this.slowDurationMillis // pass the slow duration
        );
        projectiles.add(newProjectile);
        lastShotTime = System.currentTimeMillis();
    }
}