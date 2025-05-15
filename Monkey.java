import java.util.List;
import java.util.ArrayList;
import java.awt.*;

public class Monkey {
    private int x; // Center X
    private int y; // Center Y
    private double range;
    private double hitbox; // Monkey's diameter
    private int upgradeCost;

    private int level;
    private List<Projectile> projectiles;
    private Color monkeyColor = Color.ORANGE;
    private Color projectileColor = Color.RED;
    private int projectileRadius = 5;
    private double projectileSpeed = 5.0;

    private long lastShotTime = 0;
    private long shootCooldown = 500; // milliseconds

    private boolean isSelected = false;

    public Monkey(int nx, int ny, double nrange, double nhitbox, int nlevel) {
        this.x = nx;
        this.y = ny;
        this.range = nrange;
        this.hitbox = nhitbox;
        this.level = nlevel;
        calculateUpgradeCost(); // Calculate initial cost
        this.projectiles = new ArrayList<>();
    }


    private void calculateUpgradeCost() {
        // Example: cost increases with level
        // @placeholders
        this.upgradeCost = 50 + (this.level * 75);
    }
    //@placeholders
    public void upgrade() {
        this.level++;
        calculateUpgradeCost(); // Update cost for the next level
        // You can also enhance other stats here:
        this.range+=0; // Increase range placeholder
        this.projectileSpeed += 0; // Increase projectile speed placeholder
       
        this.shootCooldown = Math.max(100, this.shootCooldown - 20); // Decrease cooldown, min 100ms
        System.out.println("Monkey at (" + x + "," + y + ") upgraded to level " + this.level +
                           ". New range: " + this.range + ", Next upgrade cost: " + this.upgradeCost);


        //can make it so level>x then create a new thing at where this monkey was.
    }

    public void draw(Graphics2D g2d) {
        if (isSelected) {
            // Draw range indicator
            g2d.setColor(new Color(150, 150, 150, 100));
            g2d.fillOval((int) (x - range), (int) (y - range), (int) (range * 2), (int) (range * 2));
        }

        // Draw the monkey
        g2d.setColor(monkeyColor);
        g2d.fillOval((int) (x - hitbox / 2), (int) (y - hitbox / 2), (int) hitbox, (int) hitbox);

        // Draw level text on/near monkey
        g2d.setColor(Color.BLACK);
        String levelText = "Lvl: " + level;
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(levelText);
        // Position below monkey, or adjust as needed
        g2d.drawString(levelText, x - textWidth / 2, y + (int)(hitbox/2) + fm.getAscent() + 2);


        List<Projectile> projectilesCopy = new ArrayList<>(projectiles);
        for (Projectile p : projectilesCopy) {
            p.draw(g2d);
        }
    }

    public int update(int screenWidth, int screenHeight) {
        int projectilesRemoved = 0;
        List<Projectile> toRemove = new ArrayList<>();

        for (Projectile p : projectiles) {
            p.update();
            if (p.isOffScreen(screenWidth, screenHeight)) {
                toRemove.add(p);
                projectilesRemoved++;
            }
        }
        projectiles.removeAll(toRemove);
        return projectilesRemoved;
    }

    public void shoot(int targetX, int targetY) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < shootCooldown) {
            return;
        }

        double distanceToTarget = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));

        if (distanceToTarget <= range) { // Only shoot if target is within range
            lastShotTime = currentTime; // Set lastShotTime only if a shot is fired
            Projectile newProjectile = new Projectile(
                    this.x, this.y,
                    targetX, targetY,
                    projectileSpeed, // Base speed, level bonus removed here as it's now part of upgrade()
                    projectileRadius,
                    projectileColor
            );
            projectiles.add(newProjectile);
        }
    }

    public boolean contains(int pX, int pY) {
        double distanceSquared = Math.pow(pX - this.x, 2) + Math.pow(pY - this.y, 2);
        double radiusSquared = Math.pow(this.hitbox / 2.0, 2);
        return distanceSquared <= radiusSquared;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }
    public void setColor(Color m, Color p){ monkeyColor = m; projectileColor =p;}
    public void setprojectileSpeed(double s){ projectileSpeed =s;}
    public void setprojectRadius(int s){ projectileRadius =s;}
    public void setShootCooldown(int s){ shootCooldown =s;}




    public int getX() { return x; }
    public int getY() { return y; }
    public double getRange() { return range; }
    public double getHitbox() { return hitbox; } // Added getter
    public int getUpgradeCost() { return upgradeCost; }
    public int getLevel() { return level; } // Added getter

    
}