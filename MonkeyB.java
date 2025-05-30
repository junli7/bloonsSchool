import java.awt.Color;

public class MonkeyB extends Monkey {

    public static final int COST = 150;

    public static final double monkeyBInitialRange = 70.0; // Slightly less than base monkey to differentiate
    public static final double monkeyBInitialHitbox = 60.0; 

    private static final String monkeyBIDLE_SPRITE_PATH = "monkey_bomber_idle.png";
    private static final String monkeyBSHOOT_SPRITE_PATH = "monkey_bomber_shoot.png";
    private static final String EXPLOSION_SPRITE_PATH = "explosion_effect.png";

    public MonkeyB(int nx, int ny, int nlevel) {
        super(nx, ny, nlevel); // Calls Monkey constructor

        this.range = monkeyBInitialRange;
        this.hitbox = monkeyBInitialHitbox;

        super.idleSpritePath = monkeyBIDLE_SPRITE_PATH;
        super.shootingSpritePath = monkeyBSHOOT_SPRITE_PATH;
        super.loadSprites(); 

        // L1 MonkeyB specific properties
        this.projectileColor = Color.BLACK; // Fallback
        this.projectileRadius = 8; // Visual size of bomb
        this.projectileSpeed = 3.0; // Bombs are slower
        this.shootCooldown = 1200; // Slower fire rate
        this.canSeeCamo = true; // Bomber sees camo by default

        this.projectileDamage = 2; // L1 bomb damage
        this.projectileIsExplosive = true;
        this.projectileAoeRadius = 45.0; // L1 AoE
        this.projectileExplosionVisualColor = new Color(255, 100, 0, 180);
        this.projectileExplosionVisualDuration = 25;
        this.projectileExplosionSpritePath = EXPLOSION_SPRITE_PATH;
        
        // Recalculate upgrade cost based on MonkeyB's L1 state
        calculateUpgradeCost(); 
    }

    // MonkeyB's standard upgrade path enhancements
    @Override
    public void upgrade() {
        super.upgrade(); // Applies generic Monkey upgrades (range, speed, cooldown, +1 damage if applicable)

        // MonkeyB specific standard upgrade benefits:
        this.projectileAoeRadius += 8; // Bombs get more AoE per standard level
        this.projectileDamage += 1;    // Bombs get an *additional* +1 damage on top of generic one from super.upgrade()

        System.out.println("MonkeyB standard upgraded. New AoE: " + this.projectileAoeRadius + ", Damage: " + this.projectileDamage);
    }
    
    // Note: applyArchetypeStats from Monkey.java will handle MonkeyB's archetypes for now.
    // If more complex stats unique to MonkeyB were needed, override applyArchetypeStats here.
}