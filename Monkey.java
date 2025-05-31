import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.AffineTransform;
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

    public static final int COST = 75;

    public static final double DEFAULT_INITIAL_RANGE = 80.0;
    public static final double DEFAULT_INITIAL_HITBOX = 50.0;

    protected Color projectileColor = Color.RED;
    protected int projectileRadius = 10;
    protected double projectileSpeed = 5.0;
    protected int projectileDamage;
    protected boolean projectileIsExplosive;
    protected double projectileAoeRadius;
    protected Color projectileExplosionVisualColor = Color.ORANGE;
    protected int projectileExplosionVisualDuration = 20;
    protected String projectileExplosionSpritePath;

    protected long lastShotTime = 0;
    protected long shootCooldown = 500;
    protected boolean isSelected = false;
    protected boolean canSeeCamo = true;

    protected transient BufferedImage idleSprite;
    protected transient BufferedImage shootingSprite;
    protected String idleSpritePath = "monkey_base_idle.png";
    protected String shootingSpritePath = "monkey_base_shoot.png";

    protected boolean isAnimatingShot = false;
    protected long shotAnimationEndTime = 0;
    protected static final long SHOT_ANIMATION_DURATION_MS = 400;
    protected double lastShotAngleRadians = 0;

    public static final String archetypeNONE = "NONE";
    public static final String archetypeDART_SNIPER = "DART_SNIPER";
    public static final String archetypeDART_QUICKFIRE = "DART_QUICKFIRE";
    public static final String archetypeBOMB_FRAGS = "BOMB_FRAGS";
    public static final String archetypeBOMB_CONCUSSION = "BOMB_CONCUSSION";
    public static final String archetypeICE_PERMAFROST = "ICE_PERMAFROST";
    public static final String archetypeICE_BRITTLE = "ICE_BRITTLE";

    protected String chosenArchetype = archetypeNONE;
    protected boolean hasChosenArchetype = false;

    protected int totalSpentOnMonkey = COST;
    public static final double sellPercentage = 0.7;

    public Monkey(int nx, int ny, int nlevel){
        this.x = nx;
        this.y = ny;
        this.level = nlevel;

        this.range = DEFAULT_INITIAL_RANGE;
        this.hitbox = DEFAULT_INITIAL_HITBOX;

        this.projectiles = new ArrayList<>();
        loadSprites();

        this.monkeyColor = Color.ORANGE;
        this.projectileColor = Color.RED;
        this.projectileRadius = 5;
        this.projectileSpeed = 5.0;
        this.projectileDamage = 10;
        this.projectileIsExplosive = false;
        this.projectileAoeRadius = 0.0;

      
        if (this instanceof MonkeyB){
            this.totalSpentOnMonkey = MonkeyB.COST;
        } else if (this instanceof MonkeyC){
            this.totalSpentOnMonkey = MonkeyC.COST;
        }

        calculateUpgradeCost();
    }

    public int getSellValue(){
        return (int) (totalSpentOnMonkey * sellPercentage);
    }

    protected void loadSprites(){
        int spriteSize = (int) this.hitbox;
        if (spriteSize <= 0) spriteSize = (int) DEFAULT_INITIAL_HITBOX;

        if (this.idleSpritePath != null && !this.idleSpritePath.isEmpty()){
            this.idleSprite = SpriteManager.getScaledSprite(this.idleSpritePath, spriteSize, spriteSize);
        } else{
            this.idleSprite = SpriteManager.getPlaceholderSprite();
        }

        if (this.shootingSpritePath != null && !this.shootingSpritePath.isEmpty()){
            this.shootingSprite = SpriteManager.getScaledSprite(this.shootingSpritePath, spriteSize, spriteSize);
        } else{
            this.shootingSprite = this.idleSprite != null ? this.idleSprite : SpriteManager.getPlaceholderSprite();
        }
    }

    protected void calculateUpgradeCost(){
        this.upgradeCost = 50 + (this.level * 75);
    }

    public String getChosenArchetype(){ return chosenArchetype; }
    public boolean hasChosenArchetype(){ return hasChosenArchetype; }

    public String[] getArchetypeChoices(){
        if (this instanceof MonkeyB){
            return new String[]{archetypeBOMB_FRAGS, archetypeBOMB_CONCUSSION};
        } else if (this instanceof MonkeyC){
            return new String[]{archetypeICE_PERMAFROST, archetypeICE_BRITTLE};
        } else if (this.getClass() == Monkey.class){
            return new String[]{archetypeDART_SNIPER, archetypeDART_QUICKFIRE};
        }
        return new String[0];
    }

    public String archetypeKeyToString(String key){
        switch (key){
            case archetypeDART_SNIPER: return "Sniper Path";
            case archetypeDART_QUICKFIRE: return "Quickfire Path";
            case archetypeBOMB_FRAGS: return "Frag Path";
            case archetypeBOMB_CONCUSSION: return "Concussion Path";
            case archetypeICE_PERMAFROST: return "Permafrost Path";
            case archetypeICE_BRITTLE: return "Brittle Ice Path";
            default: return "Unknown Path";
        }
    }
    public String archetypetype(String key){ 
        switch (key){
            case archetypeDART_SNIPER: return "SN";
            case archetypeDART_QUICKFIRE: return "QF";
            case archetypeBOMB_FRAGS: return "FR";
            case archetypeBOMB_CONCUSSION: return "CC";
            case archetypeICE_PERMAFROST: return "PF";
            case archetypeICE_BRITTLE: return "BR";
            default: return "";
        }
    }

    public String getArchetypeDescription(String key){
        if (this.getClass() == Monkey.class){
            if (key.equals(archetypeDART_SNIPER)) return "+Range, +DMG, -Fire Rate";
            if (key.equals(archetypeDART_QUICKFIRE)) return "+Fire Rate, +DMG";
        } else if (this instanceof MonkeyB){
            if (key.equals(archetypeBOMB_FRAGS)) return "+Blast Radius";
            if (key.equals(archetypeBOMB_CONCUSSION)) return "+DMG";
        } else if (this instanceof MonkeyC){
            if (key.equals(archetypeICE_PERMAFROST)) return "+Slow Duration, +Range, +Camo Detection, +Fire Rate";
            if (key.equals(archetypeICE_BRITTLE)) return "Attacks Now Deal Damage";
        }
        return "Select an upgrade path.";
    }

    protected void applyArchetypeStats(String archetypeKey){
        if (this.getClass() == Monkey.class){  //BRR BRR
            if (archetypeKey.equals(archetypeDART_SNIPER)){
                this.range *= 1.8; 
                this.shootCooldown = (long)(this.shootCooldown * 3);
                this.projectileDamage = 120;
                this.projectileSpeed = 20;

            } else if (archetypeKey.equals(archetypeDART_QUICKFIRE)){
                this.shootCooldown = (long)(this.shootCooldown * 0.2); 
            }
        } else if (this instanceof MonkeyB){ //TUNG TUNG
            if (archetypeKey.equals(archetypeBOMB_FRAGS)){
                this.projectileAoeRadius *= 1.5;
            } else if (archetypeKey.equals(archetypeBOMB_CONCUSSION)){
                this.projectileDamage *= 2;
            }
        }
    }

    public void selectArchetypeAndUpgrade(String archetypeKey, GameState gameState){
        if (this.level != 1 || this.hasChosenArchetype) return;
        
        int costOfThisUpgrade = this.getUpgradeCost();
        if (!gameState.canAfford(costOfThisUpgrade)) return;

        gameState.spendMoney(costOfThisUpgrade);
        this.totalSpentOnMonkey += costOfThisUpgrade;

        this.chosenArchetype = archetypeKey;
        this.hasChosenArchetype = true;
        
        if (this instanceof MonkeyC){
            ((MonkeyC)this).applyMonkeyCArchetypeStats(archetypeKey);
        } else{
            applyArchetypeStats(archetypeKey); 
        }

        this.level++; 
        calculateUpgradeCost(); 
        loadSprites(); 

        System.out.println(this.getClass().getSimpleName() + " chose " + archetypeKeyToString(archetypeKey) + " and upgraded to level " + this.level +". Next cost: " + this.upgradeCost + ". Total spent: " + this.totalSpentOnMonkey);
    }
    
    public void upgrade(){
        if (!this.hasChosenArchetype && this.level == 1) return;
        if (this.level >= 10) return;
        
        int costOfThisUpgrade = this.getUpgradeCost(); 
        this.totalSpentOnMonkey += costOfThisUpgrade;

        this.level++;
        calculateUpgradeCost(); 

        this.range *=1.1;
        this.projectileSpeed *=1.25;
        this.shootCooldown = Math.max(100, this.shootCooldown - 25);

        if (this.projectileDamage > 0){
            this.projectileDamage*=1.25;
        }
        if (this.level >= 5 && !this.canSeeCamo){
            boolean grantedByArchetype = false; 
            if(this instanceof MonkeyC && ((MonkeyC)this).chosenArchetype.equals(archetypeICE_BRITTLE)){
                 grantedByArchetype = true; // BrittleIce gives camo
            }
            if(!grantedByArchetype){
                this.canSeeCamo = true;
            }
        }
        loadSprites(); 

        System.out.println(this.getClass().getSimpleName() + " standard upgraded to level " + this.level +
                ". Next cost: " + this.upgradeCost + ". Total spent: " + this.totalSpentOnMonkey);
    }


    public void draw(Graphics2D g2d){
        if (isSelected){
             g2d.setColor(new Color(150, 150, 150, 100));
             g2d.fillOval((int) (x - range), (int) (y - range), (int) (range * 2), (int) (range * 2));
         }

        BufferedImage currentSprite = isAnimatingShot ? this.shootingSprite : this.idleSprite;
        if (currentSprite == null || currentSprite == SpriteManager.getPlaceholderSprite()){
            g2d.setColor(monkeyColor);
            g2d.fillOval((int) (x - hitbox / 2), (int) (y - hitbox / 2), (int) hitbox, (int) hitbox);
            if (currentSprite == SpriteManager.getPlaceholderSprite()){
                 g2d.drawImage(currentSprite, (int) (x - hitbox / 2.0), (int) (y - hitbox / 2.0), (int)hitbox, (int)hitbox, null);
            }
        } else{
            AffineTransform oldTransform = g2d.getTransform();
            double rotationAngle = this.lastShotAngleRadians + Math.PI / 2.0;
            g2d.rotate(rotationAngle, x, y);
            int spriteWidth = currentSprite.getWidth();
            int spriteHeight = currentSprite.getHeight();
            g2d.drawImage(currentSprite, (int) (x - spriteWidth / 2.0), (int) (y - spriteHeight / 2.0), null);
            g2d.setTransform(oldTransform);
        }

        g2d.setColor(Color.BLACK);
        String levelText = "L" + level + (chosenArchetype.equals(archetypeNONE) || !hasChosenArchetype ? "" : " " + archetypetype(chosenArchetype));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(levelText);
        g2d.drawString(levelText, x - textWidth / 2, y + (int) (hitbox / 2) + fm.getAscent() + 2);

        List<Projectile> projectilesToDraw = new ArrayList<>(projectiles);
        for (Projectile p : projectilesToDraw){
            p.draw(g2d);
        }
    }

    public void updateAndTarget(int screenWidth, int screenHeight, List<Human> allHumans, ArrayList<Point> mapPath){
        updateAnimationState();
        projectiles.removeIf(p -> p.updateAndCheckRemoval(allHumans) || p.checkOffScreenAndMarkSpent(screenWidth, screenHeight));
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < shootCooldown) return;
        Human currentTarget = findTarget(allHumans, mapPath);
        if (currentTarget != null) shootAtTarget(currentTarget);
    }

    protected Human findTarget(List<Human> allHumans, ArrayList<Point> mapPath){
        Human bestTarget = null;
        double maxProgress = -1.0;
        for (Human human : allHumans){
            if (!human.isAlive() || human.hasReachedEnd()) continue;
            if (human.isCamo() && !this.canSeeCamo) continue;
            double distanceToHuman = Math.sqrt(Math.pow(human.getX() - this.x, 2) + Math.pow(human.getY() - this.y, 2));
            if (distanceToHuman > this.range) continue;
            double currentHumanProgress = human.getCurrentPathIndex();
            if (mapPath != null && !mapPath.isEmpty() && human.getCurrentPathIndex() < mapPath.size()){
                Point nextWaypoint = mapPath.get(human.getCurrentPathIndex());
                double totalDistToNextWaypoint = this.range;
                if(human.getCurrentPathIndex() > 0){
                    Point prevWaypoint = mapPath.get(human.getCurrentPathIndex()-1);
                    totalDistToNextWaypoint = prevWaypoint.distance(nextWaypoint);
                }
                if(totalDistToNextWaypoint == 0) totalDistToNextWaypoint = 1;

                double distToWaypoint = human.getDistanceToWaypoint(nextWaypoint);
                currentHumanProgress += (1.0 - Math.min(1.0, distToWaypoint / totalDistToNextWaypoint ));
            }
            if (currentHumanProgress > maxProgress){
                maxProgress = currentHumanProgress;
                bestTarget = human;
            }
        }
        return bestTarget;
    }

    protected void shootAtTarget(Human target){
        if (target == null || isAnimatingShot) return;
        double deltaX = target.getX() - this.x;
        double deltaY = target.getY() - this.y;
        this.lastShotAngleRadians = Math.atan2(deltaY, deltaX);
        this.isAnimatingShot = true;
        this.shotAnimationEndTime = System.currentTimeMillis() + SHOT_ANIMATION_DURATION_MS;
        fireProjectile(target);
    }

    protected void fireProjectile(Human target){
        if (target == null) return;
        Projectile newProjectile = new Projectile(
                this.x, this.y, target,
                this.projectileSpeed, this.projectileRadius, this.projectileColor,
                this.projectileDamage,
                this.projectileIsExplosive, this.projectileAoeRadius,
                this.projectileExplosionVisualColor, this.projectileExplosionVisualDuration,
                this.projectileExplosionSpritePath,
                false, 0 
        );
        projectiles.add(newProjectile);
        lastShotTime = System.currentTimeMillis();
    }

    protected void updateAnimationState(){
        if (isAnimatingShot && System.currentTimeMillis() >= shotAnimationEndTime){
            isAnimatingShot = false;
        }
    }

    public boolean contains(int pX, int pY){
        double distanceSquared = Math.pow(pX - this.x, 2) + Math.pow(pY - this.y, 2);
        return distanceSquared <= Math.pow(this.hitbox / 2.0, 2);
    }

    public double getRange(){ return range; }
    public double getHitbox(){ return hitbox; }
    public int getX(){ return x; }
    public int getY(){ return y; }
    public int getUpgradeCost(){ return upgradeCost; }
    public int getLevel(){ return level; }
    public List<Projectile> getProjectiles(){ return projectiles; }
    public boolean canDetectCamo(){ return canSeeCamo; }
    public boolean isSelected(){ return isSelected; }

    public void setSelected(boolean selected){ this.isSelected = selected; }
    public void setColor(Color m, Color p){ monkeyColor = m; projectileColor = p; }
    public void setProjectileSpeed(double s){ projectileSpeed = s > 0 ? s : this.projectileSpeed; }
    public void setProjectileRadius(int s){ projectileRadius = s > 0 ? s : this.projectileRadius; }
    public void setShootCooldown(long s){ shootCooldown = s > 0 ? s : this.shootCooldown; }
    public void setRange(double newRange){ if (newRange > 0) this.range = newRange; }

    public void setHitbox(double newHitbox){
        if (newHitbox > 0){
            this.hitbox = newHitbox;
            loadSprites();
        }
    }
    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
    }
}