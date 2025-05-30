import java.awt.Color;

public class MonkeyB extends Monkey {
    public static final int COST = 150;
    public static final double monkeyBInitialRange = 80.0;
    public static final double monkeyBInitialHitbox = 60.0; 
    private static final String monkeyBIDLE_SPRITE_PATH = "monkey_bomber_idle.png";
    private static final String monkeyBSHOOT_SPRITE_PATH = "monkey_bomber_shoot.png";
    private static final String EXPLOSION_SPRITE_PATH = "explosion_effect.png";

    public MonkeyB(int nx, int ny, int nlevel) {
        super(nx, ny, nlevel);
        this.range = monkeyBInitialRange;
        this.hitbox = monkeyBInitialHitbox;

        super.idleSpritePath = monkeyBIDLE_SPRITE_PATH;
        super.shootingSpritePath = monkeyBSHOOT_SPRITE_PATH;
        super.loadSprites();

        this.projectileColor = Color.BLACK;
        this.projectileRadius = 8;
        this.projectileSpeed = 20.0;
        this.shootCooldown = 2000; // 2 seconds cooldown
        this.canSeeCamo = true;
        this.projectileDamage = 20;
        this.projectileIsExplosive = true;
        this.projectileAoeRadius = 45.0;
        this.projectileExplosionVisualColor = new Color(255, 100, 0, 180);
        this.projectileExplosionVisualDuration = 25;
        this.projectileExplosionSpritePath = EXPLOSION_SPRITE_PATH;
        
        calculateUpgradeCost();
    }

    @Override
    public void upgrade() {
        super.upgrade();
        this.projectileAoeRadius += 5;
        this.projectileDamage += 10;
    }
}