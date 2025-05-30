import java.awt.Color;

public class MonkeyC extends Monkey {
    public static final int COST = 200;
    private static final String monkeyCIDLEspritePath = "monkey_slow_idle.png";
    private static final String monkeyCSHOOTspritePath = "monkey_slow_shoot.png";
    private static final String iceExplosionspritePath = "project_slow.png";

    protected int slowDurationMillis;

    public MonkeyC(int nx, int ny, int nlevel) {
        super(nx, ny, nlevel);
        this.range = 70.0;
        this.hitbox = 55.0;

        super.idleSpritePath = monkeyCIDLEspritePath;
        super.shootingSpritePath = monkeyCSHOOTspritePath;
        super.loadSprites();

        this.projectileColor = new Color(100, 150, 255);
        this.projectileRadius = 7;
        this.projectileSpeed = 3.5;
        this.shootCooldown = 900;
        this.canSeeCamo = false;
        this.projectileDamage = 0;
        this.projectileIsExplosive = true;
        this.projectileAoeRadius = 40.0;
        this.projectileExplosionVisualColor = new Color(173, 216, 230, 150);
        this.projectileExplosionVisualDuration = 15;
        this.slowDurationMillis = 1000;
        
        calculateUpgradeCost();
    }
    
    public void applyMonkeyCArchetypeStats(String archetypeKey) {
        if (archetypeKey.equals(archetypeICE_PERMAFROST)) {
            this.slowDurationMillis = (int)(this.slowDurationMillis * 2.5);
            this.shootCooldown = (long)(this.shootCooldown * 1.15);
            this.range*=2;
            this.canSeeCamo = true;
        } else if (archetypeKey.equals(archetypeICE_BRITTLE)) {
            this.projectileDamage = 10;
        }
    }

    @Override
    public void upgrade() {
        super.upgrade();
        this.slowDurationMillis += 300;
        this.projectileAoeRadius += 6;
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
            this.projectileExplosionSpritePath != null ? this.projectileExplosionSpritePath : iceExplosionspritePath,
            true, this.slowDurationMillis
        );
        projectiles.add(newProjectile);
        lastShotTime = System.currentTimeMillis();
    }
}