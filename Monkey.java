import java.util.List;
import java.util.ArrayList;
import java.awt.*;

public class Monkey {
    protected int x;
    protected int y;
    protected double range;
    protected double hitbox;
    protected int upgradeCost;
    protected int level;
    protected List<Projectile> projectiles;
    protected Color monkeyColor = Color.ORANGE;
    protected Color projectileColor = Color.RED;
    protected int projectileRadius = 5;
    protected double projectileSpeed = 5.0;
    protected long lastShotTime = 0;
    protected long shootCooldown = 500;
    protected boolean isSelected = false;
    protected boolean canSeeCamo = false;

    public Monkey(int nx, int ny, double nrange, double nhitbox, int nlevel) {
        this.x = nx;
        this.y = ny;
        this.range = nrange;
        this.hitbox = nhitbox;
        this.level = nlevel;
        this.projectiles = new ArrayList<>();
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
        if (this.level > 2 && !this.canSeeCamo) {
            this.canSeeCamo = true;
            System.out.println("Monkey gained camo detection!");
        }
        System.out.println("Monkey at (" + x + "," + y + ") upgraded to level " + this.level +
                ". New range: " + this.range + ", Next upgrade cost: " + this.upgradeCost);
    }

    public void draw(Graphics2D g2d) {
        if (isSelected) {
            g2d.setColor(new Color(150, 150, 150, 100));
            g2d.fillOval((int) (x - range), (int) (y - range), (int) (range * 2), (int) (range * 2));
        }
        g2d.setColor(monkeyColor);
        g2d.fillOval((int) (x - hitbox / 2), (int) (y - hitbox / 2), (int) hitbox, (int) hitbox);
        g2d.setColor(Color.BLACK);
        String levelText = "Lvl: " + level;
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(levelText);
        g2d.drawString(levelText, x - textWidth / 2, y + (int) (hitbox / 2) + fm.getAscent() + 2);

        List<Projectile> projectilesCopy = new ArrayList<>(projectiles);
        for (Projectile p : projectilesCopy) {
            p.draw(g2d);
        }
    }

    public void updateAndTarget(int screenWidth, int screenHeight, List<Human> allHumans, ArrayList<Point> mapPath) {
        // 1. Update existing projectiles and remove them if they hit, target dies, or go off-screen
        projectiles.removeIf(p -> p.updateAndCheckHit() || p.isOffScreen(screenWidth, screenHeight));

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < shootCooldown) {
            return;
        }

        Human currentTarget = findTarget(allHumans, mapPath);
        if (currentTarget != null) {
            shootAtTarget(currentTarget); // Pass the Human object
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
            if (human.getCurrentPathIndex() < mapPath.size()) { // Ensure path index is valid
                Point nextWaypoint = mapPath.get(human.getCurrentPathIndex());
                currentHumanProgress += (1.0 - (human.getDistanceToWaypoint(nextWaypoint) / 1000.0)); // Favor those closer to their next waypoint
            }


            if (currentHumanProgress > maxProgress) {
                maxProgress = currentHumanProgress;
                bestTarget = human;
            }
        }
        return bestTarget;
    }

    protected void shootAtTarget(Human target) { // Takes Human object
        if (target == null) return;

        Projectile newProjectile = new Projectile(
                this.x, this.y,         // Start from monkey's center
                target,                 // Pass the Human object itself
                this.projectileSpeed,
                this.projectileRadius,
                this.projectileColor,
                1 + (level / 2)         // Example: Damage increases with monkey level
        );
        projectiles.add(newProjectile);
    }

    // Manual shoot (e.g. on mouse click) is no longer the primary way, but can be kept for other uses.
    // If kept, it should also create a Projectile that either doesn't home or homes on a coordinate.
    // For now, we focus on auto-targeting.
    public void shoot(int targetX, int targetY) {
        // This method is less relevant with auto-homing projectiles based on Human targets.
        // If you want to keep it for some special ability, it would create a non-homing projectile
        // or a projectile that homes on a fixed point.
        // For simplicity, let's assume all primary attacks are homing on Humans.
        System.out.println("Manual shoot method called - consider adapting for homing or removing if unused.");
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