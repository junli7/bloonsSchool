import java.awt.Color;

public class MonkeyC extends Monkey {

    public static final int COST = 200;
    private static final String MONKEY_C_IDLE_SPRITE_PATH = "monkey_slow_idle.png";
    private static final String MONKEY_C_SHOOT_SPRITE_PATH = "monkey_slow_idle.png"; // Use idle if no shoot sprite
    private static final String ICE_EXPLOSION_SPRITE_PATH = "project_slow.png"; // Optional specific explosion for ice

    // MonkeyC specific stats
    protected int slowDurationMillis; // Moved from projectileIsSlowing in base Monkey

    public MonkeyC(int nx, int ny, int nlevel) {
        super(nx, ny, nlevel);

        this.range = 70.0;
        this.hitbox = 55.0;

        super.idleSpritePath = MONKEY_C_IDLE_SPRITE_PATH;
        super.shootingSpritePath = MONKEY_C_SHOOT_SPRITE_PATH; // Make sure this exists or use idle
        super.loadSprites();

        // L1 MonkeyC specific properties
        this.projectileColor = new Color(100, 150, 255); // Fallback
        this.projectileRadius = 7;
        this.projectileSpeed = 3.5;
        this.shootCooldown = 900;
        this.canSeeCamo = false; // Base ice monkey cannot see camo

        this.projectileDamage = 0; // L1 Ice monkey does no damage
        this.projectileIsExplosive = true; // AoE slow
        this.projectileAoeRadius = 40.0;   // L1 slow AoE
        this.projectileExplosionVisualColor = new Color(173, 216, 230, 150);
        this.projectileExplosionVisualDuration = 15;
        //this.projectileExplosionSpritePath = ICE_EXPLOSION_SPRITE_PATH; // If you have one

        // Slowing properties
        this.slowDurationMillis = 1000; // L1 slow duration
        
        // Recalculate L1->L2 cost
        calculateUpgradeCost();
    }
    
    // MonkeyC applies its specific archetype stats
    public void applyMonkeyCArchetypeStats(String archetypeKey) {
        // First, call base class's applyArchetypeStats if it has relevant general changes
        // super.applyArchetypeStats(archetypeKey); // But Monkey.applyArchetypeStats filters by class.
                                                 // So MonkeyC needs to handle its own fully or call a generic part.
                                                 // For now, Monkey.applyArchetypeStats handles the common ones.
        if (archetypeKey.equals(ARCHETYPE_ICE_PERMAFROST)) {
            this.slowDurationMillis = (int)(this.slowDurationMillis * 2.5); // Significantly longer slow
            this.shootCooldown = (long)(this.shootCooldown * 1.15); // Slightly slower attack
            System.out.println("Ice Monkey: Permafrost chosen. Slow Duration: " + this.slowDurationMillis);
        } else if (archetypeKey.equals(ARCHETYPE_ICE_BRITTLE)) {
            this.projectileDamage = 1; // Ice monkey now does damage
            this.slowDurationMillis = (int)(this.slowDurationMillis * 0.8); // Slight reduction in slow duration as trade-off
            this.canSeeCamo = true; // Brittle ice can see camo
            System.out.println("Ice Monkey: Brittle Ice chosen. Now deals damage: " + this.projectileDamage + ". Can see camo.");
        }
    }


    @Override
    public void upgrade() { // Standard upgrade path L2+
        super.upgrade(); // Applies generic Monkey upgrades

        // MonkeyC specific standard upgrade benefits:
        this.slowDurationMillis += 300; // Each standard level increases slow duration
        this.projectileAoeRadius += 6; // And AoE of slow effect
        
        // If BRITTLE path, damage was already increased by Monkey.upgrade()'s generic +1.
        // If Permafrost, no damage to increase.

        System.out.println("MonkeyC standard upgraded. AoE: " + this.projectileAoeRadius + ", Slow Duration: " + this.slowDurationMillis);
    }

    @Override
    protected void fireProjectile(Human target) {
        if (target == null) return;
        Projectile newProjectile = new Projectile(
                this.x, this.y, target,
                this.projectileSpeed, this.projectileRadius, this.projectileColor,
                this.projectileDamage,
                this.projectileIsExplosive, this.projectileAoeRadius,
                this.projectileExplosionVisualColor, this.projectileExplosionVisualDuration,
                this.projectileExplosionSpritePath != null ? this.projectileExplosionSpritePath : ICE_EXPLOSION_SPRITE_PATH, // Pass correct explosion sprite
                true, // isSlowing = true
                this.slowDurationMillis
        );
        // Overwrite projectile's flying sprite path if it's specific for ice
        // This is a bit of a hack due to Projectile constructor complexity.
        // A better way would be for Projectile to take a flyingSpritePath parameter.
        // For now, this specific projectile type might need its own path logic in Projectile.java
        // Or, ensure Projectile.java uses SLOW_PROJECTILE_SPRITE_PATH correctly.
        // The current Projectile constructor uses SLOW_PROJECTILE_SPRITE_PATH if isSlowingProjectile is true.

        projectiles.add(newProjectile);
        lastShotTime = System.currentTimeMillis();
    }
}