

import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.util.Iterator;
import java.awt.image.BufferedImage;

public class Monkey {
    protected int x;
    protected int y;
    protected double range;
    protected double hitbox;
    protected int upgradeCost;
    protected int level;
    protected List<Projectile> projectiles;
    protected Color monkeyColor = Color.ORANGE;

    // Projectile specific properties
    protected Color projectileColor = Color.RED;
    protected int projectileRadius = 5;
    protected double projectileSpeed = 5.0;
    protected int projectileDamage;
    protected boolean projectileIsExplosive;
    protected double projectileAoeRadius;
    protected Color projectileExplosionVisualColor = Color.ORANGE;
    protected int projectileExplosionVisualDuration;
    protected String projectileExplosionSpritePath;

    protected long lastShotTime = 0;
    protected long shootCooldown = 500;
    protected boolean isSelected = false;
    protected boolean canSeeCamo = false;

    protected BufferedImage sprite;
    protected static final String DEFAULT_MONKEY_SPRITE_PATH = "monkey_default.png";


    public Monkey(int nx, int ny, double nrange, double nhitbox, int nlevel) {
        this.x = nx;
        this.y = ny;
        this.range = nrange;
        this.hitbox = nhitbox;
        this.level = nlevel;
        this.projectiles = new ArrayList<>();

        this.sprite = SpriteManager.getScaledSprite(DEFAULT_MONKEY_SPRITE_PATH, (int)this.hitbox, (int)this.hitbox);

        this.monkeyColor = Color.ORANGE;
        this.projectileColor = Color.RED;
        this.projectileRadius = 5;
        this.projectileSpeed = 5.0;
        this.projectileDamage = 1 + (this.level / 2);
        this.projectileIsExplosive = false;
        this.projectileAoeRadius = 0.0;
        this.projectileExplosionVisualColor = Color.ORANGE;
        this.projectileExplosionVisualDuration = 20;
        this.projectileExplosionSpritePath = null;

        calculateUpgradeCost();
        if (this.level > 2) {
            this.canSeeCamo = true;
        }
    }

    private void calculateUpgradeCost() {
        this.upgradeCost = 50 + (this.level * 75);
    }

    public void upgrade() {
        this.level++;
        calculateUpgradeCost();
        this.range += 5;
        this.projectileSpeed += 0.1;
        this.shootCooldown = Math.max(100, this.shootCooldown - 20);
        this.projectileDamage = 1 + (this.level / 2);

        // Rescale sprite if hitbox changes with upgrade (it doesn't currently)
        // if (this.hitbox changes) {
        //     this.sprite = SpriteManager.getScaledSprite(DEFAULT_MONKEY_SPRITE_PATH, (int)this.hitbox, (int)this.hitbox);
        // }

        if (this.level > 2 && !this.canSeeCamo) {
            this.canSeeCamo = true;
            System.out.println("Monkey gained camo detection!");
        }
        System.out.println("Monkey at (" + x + "," + y + ") upgraded to level " + this.level +
                ". New range: " + this.range + ", Damage: " + this.projectileDamage +
                ", Next upgrade cost: " + this.upgradeCost);
    }

    public void draw(Graphics2D g2d) {
        if (isSelected) {
            g2d.setColor(new Color(150, 150, 150, 100));
            g2d.fillOval((int) (x - range), (int) (y - range), (int) (range * 2), (int) (range * 2));
        }

        g2d.setColor(monkeyColor);
        g2d.fillOval((int) (x - hitbox / 2), (int) (y - hitbox / 2), (int) hitbox, (int) hitbox);

        if (sprite != null) {
            g2d.drawImage(sprite, (int) (x - sprite.getWidth() / 2.0), (int) (y - sprite.getHeight() / 2.0), null);
        }

        g2d.setColor(Color.BLACK);
        String levelText = "Lvl: " + level;
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(levelText);
        g2d.drawString(levelText, x - textWidth / 2, y + (int) (hitbox / 2) + fm.getAscent() + 2);

        List<Projectile> projectilesToDraw = new ArrayList<>(projectiles);
        for (Projectile p : projectilesToDraw) {
            p.draw(g2d);
        }
    }

    public void updateAndTarget(int screenWidth, int screenHeight, List<Human> allHumans, ArrayList<Point> mapPath) {
        projectiles.removeIf(p -> p.updateAndCheckRemoval(allHumans) || p.checkOffScreenAndMarkSpent(screenWidth, screenHeight));

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < shootCooldown) {
            return;
        }

        Human currentTarget = findTarget(allHumans, mapPath);
        if (currentTarget != null) {
            shootAtTarget(currentTarget);
            lastShotTime = currentTime;
        }
    }

    protected Human findTarget(List<Human> allHumans, ArrayList<Point> mapPath) {
        Human bestTarget = null;
        double maxProgress = -1.0;

        for (Human human : allHumans) {
            if (!human.isAlive() || human.hasReachedEnd()) continue;
            if (human.isCamo() && !this.canSeeCamo) continue;

            double distanceToHuman = Math.sqrt(Math.pow(human.getX() - this.x, 2) + Math.pow(human.getY() - this.y, 2));
            if (distanceToHuman > this.range) continue;

            double currentHumanProgress = human.getCurrentPathIndex();
            if (mapPath != null && !mapPath.isEmpty() && human.getCurrentPathIndex() < mapPath.size()) {
                Point nextWaypoint = mapPath.get(human.getCurrentPathIndex());
                currentHumanProgress += (1.0 - (human.getDistanceToWaypoint(nextWaypoint) / 1000.0));
            }

            if (currentHumanProgress > maxProgress) {
                maxProgress = currentHumanProgress;
                bestTarget = human;
            }
        }
        return bestTarget;
    }

    protected void shootAtTarget(Human target) {
        if (target == null) return;

        Projectile newProjectile = new Projectile(
                this.x, this.y,
                target,
                this.projectileSpeed,
                this.projectileRadius,
                this.projectileColor,
                this.projectileDamage,
                this.projectileIsExplosive,
                this.projectileAoeRadius,
                this.projectileExplosionVisualColor,
                this.projectileExplosionVisualDuration,
                this.projectileExplosionSpritePath
        );
        projectiles.add(newProjectile);
    }

    public boolean contains(int pX, int pY) {
        double distanceSquared = Math.pow(pX - this.x, 2) + Math.pow(pY - this.y, 2);
        double radiusSquared = Math.pow(this.hitbox / 2.0, 2);
        return distanceSquared <= radiusSquared;
    }

    public void setSelected(boolean selected) { this.isSelected = selected; }
    public boolean isSelected() { return isSelected; }
    public void setColor(Color m, Color p) { monkeyColor = m; projectileColor = p; }
    public void setProjectileSpeed(double s) { projectileSpeed = s; }
    public void setProjectileRadius(int s) { projectileRadius = s; }
    public void setShootCooldown(long s) { shootCooldown = s; }
    public int getX() { return x; }
    public int getY() { return y; }
    public double getRange() { return range; }
    public double getHitbox() { return hitbox; }
    public int getUpgradeCost() { return upgradeCost; }
    public int getLevel() { return level; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public boolean canDetectCamo() { return canSeeCamo; }
}