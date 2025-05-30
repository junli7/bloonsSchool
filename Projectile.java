import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.image.BufferedImage;

public class Projectile{
    private double x, y;
    private double dx, dy;
    private int radius;
    private Color color;
    private double speed;
    private int damage;
    private Human target;

    private boolean isExplosive;
    private double aoeRadius;

    private boolean isSlowingProjectile;
    private int slowDurationMillis;

    private enum State{active, exploding, spent}
    private State currentState = State.active;

    private int explosionAgeTicks = 0;
    private int explosionDurationTicks = 20;
    private double explosionCenterX, explosionCenterY;
    private BufferedImage explosionSprite;

    private BufferedImage sprite;
    private static final String defaultSpritePath = "projectile_dart.png";
    private static final String bombSpritePath = "projectile_bomb.png";
    private static final String slowSpritePath = "project_slow.png";
    private static final double hitThreshold = 5.0;

    public Projectile(double startX, double startY, Human targetHuman, double speed, int projectileVisualRadius,Color projectileColor, int damage, boolean isExplosive, double aoeRadius,Color explosionVisualEffectColor, int explosionVisualEffectDuration,String projectileExplosionSpritePath,boolean isSlowing, int slowDurationMillis){
        this.x = startX;
        this.y = startY;
        this.target = targetHuman;
        this.speed = speed;
        this.radius = projectileVisualRadius;
        this.color = projectileColor;
        this.damage = damage;
        this.isExplosive = isExplosive;
        this.aoeRadius = aoeRadius;

        this.isSlowingProjectile = isSlowing;
        this.slowDurationMillis = slowDurationMillis;

        this.explosionDurationTicks = explosionVisualEffectDuration;

        String flyingProjectileSpritePath;
        if (this.isSlowingProjectile){
            flyingProjectileSpritePath = slowSpritePath;
        } else if (this.isExplosive){
            flyingProjectileSpritePath = bombSpritePath;
        } else{
            flyingProjectileSpritePath = defaultSpritePath;
        }

        int projectileSpriteDiameter = this.radius * 2;
        if (projectileSpriteDiameter <=0 ) projectileSpriteDiameter = 10;

        this.sprite = SpriteManager.getScaledSprite(flyingProjectileSpritePath, projectileSpriteDiameter, projectileSpriteDiameter);
        


        if (this.isExplosive && projectileExplosionSpritePath != null && !projectileExplosionSpritePath.isEmpty()){
            int explosionSpriteSize = (int) (this.aoeRadius * 2);
            if (explosionSpriteSize <= 0) explosionSpriteSize = 30;

            this.explosionSprite = SpriteManager.getScaledSprite(projectileExplosionSpritePath, explosionSpriteSize, explosionSpriteSize);
            
        }


        if (this.target != null){
            calculateDirectionToTarget();
        } else{
            this.dx = 0; this.dy = 0; this.currentState = State.spent;
        }
    }

    private void calculateDirectionToTarget(){
        if (target == null || currentState != State.active || !target.isAlive()){
           this.dx = 0; this.dy = 0; 
            return;
        }
        double targetX = target.getX();
        double targetY = target.getY();
        double deltaX = targetX - this.x;
        double deltaY = targetY - this.y;
        double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distanceToTarget > 0){
            this.dx = (deltaX / distanceToTarget) * speed;
            this.dy = (deltaY / distanceToTarget) * speed;
        } else{
            this.dx = 0;
            this.dy = 0;
        }
    }

    public boolean updateAndCheckRemoval(List<Human> allHumans){
        if (currentState == State.spent) return true;

        if (currentState == State.exploding){
            explosionAgeTicks++;
            if (explosionAgeTicks >= explosionDurationTicks){
                currentState = State.spent; return true;
            }
            return false;
        }

        if (target == null || !target.isAlive()){
            currentState = State.spent;
            return true;
        }

        calculateDirectionToTarget();
        this.x += dx;
        this.y += dy;

        double targetCurrentX = target.getX();
        double targetCurrentY = target.getY();
        double distanceToTargetPos = Math.sqrt(Math.pow(targetCurrentX - this.x, 2) + Math.pow(targetCurrentY - this.y, 2));

        if (distanceToTargetPos < hitThreshold || distanceToTargetPos < this.speed){
            this.explosionCenterX = this.x;
            this.explosionCenterY = this.y;

            if (isExplosive && aoeRadius > 0){
                for (Human humanInRange : allHumans){
                    if (humanInRange.isAlive()){
                        double distToImpactCenter = Math.sqrt(Math.pow(humanInRange.getX() - this.explosionCenterX, 2) + Math.pow(humanInRange.getY() - this.explosionCenterY, 2));
                        if (distToImpactCenter <= this.aoeRadius){
                            if (this.damage > 0) humanInRange.takeDamage(this.damage);
                            if (this.isSlowingProjectile) humanInRange.applySlow(this.slowDurationMillis);
                        }
                    }
                }
                currentState = State.exploding;
                return false;
            } else{
                if (target.isAlive()){
                    if (this.damage > 0) target.takeDamage(this.damage);
                    if (this.isSlowingProjectile) target.applySlow(this.slowDurationMillis);
                }
                currentState = State.spent;
                return true;
            }
        }
        return false;
    }

    public void draw(Graphics2D g2d){
        if (currentState == State.exploding){
            float alpha = 1.0f - ((float) explosionAgeTicks / explosionDurationTicks);
            alpha = Math.max(0, Math.min(1, alpha));

            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(ac);

            if (this.explosionSprite != null){
                int spriteWidth = this.explosionSprite.getWidth();
                int spriteHeight = this.explosionSprite.getHeight();
                g2d.drawImage( this.explosionSprite, (int) (explosionCenterX - spriteWidth / 2.0), (int) (explosionCenterY - spriteHeight / 2.0), null );
            }
            g2d.setComposite(originalComposite);

        } else if (currentState == State.active){
            if (this.sprite != null && this.sprite != SpriteManager.getPlaceholderSprite()){
                g2d.drawImage(this.sprite, (int) (x - this.sprite.getWidth() / 2.0), (int) (y - this.sprite.getHeight() / 2.0), null);
            } else{
                g2d.setColor(this.color);
                g2d.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
            }
        }
    }

    public boolean checkOffScreenAndMarkSpent(int screenWidth, int screenHeight){
        if (currentState == State.active &&
            (x < -radius * 5 || x > screenWidth + radius * 5 || y < -radius * 5 || y > screenHeight + radius * 5)){
            currentState = State.spent;
            return true;
        }
        return false;
    }

    public double getX(){ return x; }
    public double getY(){ return y; }
}