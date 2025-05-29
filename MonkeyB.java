
import java.awt.Color;
// No need to import BufferedImage if only using inherited `sprite` field

public class MonkeyB extends Monkey {

    private double baseAoeRadius = 40.0;
    private int baseDamage = 2;
    private static final String MONKEY_B_SPRITE_PATH = "monkey_bomber.png";
    private static final String EXPLOSION_SPRITE_PATH = "explosion_effect.png";

    public MonkeyB(int nx, int ny, double nrange, double nhitbox, int nlevel) {
        super(nx, ny, nrange, nhitbox, nlevel);

        // Override the sprite from superclass
        this.sprite = SpriteManager.getScaledSprite(MONKEY_B_SPRITE_PATH, (int)this.hitbox, (int)this.hitbox);

        setColor(Color.DARK_GRAY, Color.BLACK);
        setProjectileRadius(8);
        setProjectileSpeed(40);
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
        this.projectileAoeRadius = this.baseAoeRadius + ((this.level - 1) * 10);
    }

    @Override
    public void upgrade() {
        super.upgrade();
        updateMonkeyBProperties();

        // Rescale sprite if hitbox changes (it doesn't currently)
        // this.sprite = SpriteManager.getScaledSprite(MONKEY_B_SPRITE_PATH, (int)this.hitbox, (int)this.hitbox);

        System.out.println("MonkeyB at (" + x + "," + y + ") upgraded to level " + this.level +
                ". New AoE Radius: " + this.projectileAoeRadius +
                ", New Damage: " + this.projectileDamage +
                ", Explosion Visual Duration: " + this.projectileExplosionVisualDuration +
                ", Next upgrade cost: " + this.upgradeCost);
    }
}