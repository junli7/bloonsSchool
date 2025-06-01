import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

public class Human{
    private double x, y;
    private double currentSpeed;
    private double originalSpeed;
    private int hitboxDiameter;
    private int health;
    private int currentPathIndex;
    private boolean camo;
    private boolean reachedEnd;

    private BufferedImage sprite;
    private double currentAngleRadians = 0.0;

    private String humanType;
    private int moneyReward;

    private long slowEffectEndTimeMillis = 0;
    private static final double slowRate = 0.5;
    private static final double waypointThreshold = 5.0;
    private static final Random random = new Random();

    private List<String> currentSpriteVariants;
    private boolean defaultCamoForType;


    public Human(String humanType, double startX, double startY, boolean isCamoFromSpawnInstruction, ArrayList<Point> path){
        this.x = startX;
        this.y = startY;
        this.humanType = humanType;

        if ("baby".equalsIgnoreCase(humanType)){
            this.originalSpeed = 5.0;
            this.health = 200;
            this.hitboxDiameter = 50;
            this.currentSpriteVariants = List.of("human_baby.png");
            this.defaultCamoForType = false;
            this.moneyReward = 25;
        } 
        else if ("kid".equalsIgnoreCase(humanType)){
            this.originalSpeed = 2;
            this.health = 50;
            this.hitboxDiameter = 50;
            this.currentSpriteVariants = List.of("human_kid0.png", "human_kid1.png");
            this.defaultCamoForType = false;
            this.moneyReward = 11;
        } 
        else if ("normal".equalsIgnoreCase(humanType)){
            this.originalSpeed = 1;
            this.health = 40;
            this.hitboxDiameter = 50;
            this.currentSpriteVariants = List.of("human_normal0.png", "human_normal1.png", "human_normal2.png");
            this.defaultCamoForType = false;
            this.moneyReward = 15;
        } 
        else if ("bodybuilder".equalsIgnoreCase(humanType)){
            this.originalSpeed = 0.8;
            this.health = 900;
            this.hitboxDiameter = 80;
            this.currentSpriteVariants = List.of("human_bodybuilder.png");
            this.defaultCamoForType = false;
            this.moneyReward = 20;
        } 
        else if ("businessman".equalsIgnoreCase(humanType)){
            this.originalSpeed = 1.3;
            this.health = 300;
            this.hitboxDiameter = 60;
            this.currentSpriteVariants = List.of("human_businessman.png");
            this.defaultCamoForType = false;
            this.moneyReward = 30;
        } 
        else if ("bossbaby".equalsIgnoreCase(humanType)){
            this.originalSpeed = 0.5;
            this.health = 10000;
            this.hitboxDiameter = 200;
            this.currentSpriteVariants = List.of("human_bigbaby.png");
            this.moneyReward = 200;
        }
        else if ("ninja".equalsIgnoreCase(humanType)){
            this.originalSpeed = 3;
            this.health = 100;
            this.hitboxDiameter = 50;
            this.currentSpriteVariants = List.of("human_ninja.png");
            this.defaultCamoForType = true;
            this.moneyReward = 50;
        }
        else if ("bossninja".equalsIgnoreCase(humanType)){
            this.originalSpeed = 2.2;
            this.health = 10000;
            this.hitboxDiameter = 200;
            this.currentSpriteVariants = List.of("human_bossninja.png");
            this.defaultCamoForType = true;
            this.moneyReward = 200;
        }
        else if ("bossninja+".equalsIgnoreCase(humanType)){
            this.originalSpeed = 2.2;
            this.health = 50000;
            this.hitboxDiameter = 300;
            this.currentSpriteVariants = List.of("human_bossninja.png");
            this.defaultCamoForType = true;
            this.moneyReward = 1000;
        }

        this.currentSpeed = this.originalSpeed;
        this.camo = isCamoFromSpawnInstruction || this.defaultCamoForType;


        this.reachedEnd = false;
        this.currentPathIndex = 0;

        String chosenSpritePath = selectRandomSpritePath(this.currentSpriteVariants);
        this.sprite = SpriteManager.getScaledSprite(chosenSpritePath, this.hitboxDiameter, this.hitboxDiameter);


        calculateInitialAngle(path);
    }

    private String selectRandomSpritePath(List<String> availablePaths){
        if (availablePaths == null || availablePaths.isEmpty()){
            return "";
        }
        if (availablePaths.size() == 1){
            return availablePaths.get(0);
        }
        return availablePaths.get(random.nextInt(availablePaths.size()));
    }

    private void calculateInitialAngle(ArrayList<Point> path){
        if (path != null && path.size() > 1){
            Point nextWayPoint = path.get(1);
            double deltaX = nextWayPoint.getX() - this.x;
            double deltaY = nextWayPoint.getY() - this.y;
            if (deltaX != 0 || deltaY != 0){
                this.currentAngleRadians = Math.atan2(deltaY, deltaX);
            } 
            
        }
    }

    public void draw(Graphics2D g2d){
        if (isSlowed()){
            g2d.setColor(new Color(173, 216, 230, 150));
            
            g2d.fillOval((int) (x - hitboxDiameter / 2.0 - 2), (int) (y - hitboxDiameter / 2.0 - 2), hitboxDiameter + 4, hitboxDiameter + 4);
        }

        AffineTransform oldTransform = g2d.getTransform();
        double rotationForSpriteDrawing = this.currentAngleRadians;
        g2d.rotate(rotationForSpriteDrawing, this.x, this.y);

        if (this.sprite != null){
            int spriteWidth = this.sprite.getWidth();
            int spriteHeight = this.sprite.getHeight();
            g2d.drawImage(this.sprite, (int) (this.x - spriteWidth / 2.0), (int) (this.y - spriteHeight / 2.0), null);

            
            if (this.camo ){
                Color camoOverlay = new Color(50, 50, 50, 70);
                g2d.setColor(camoOverlay);
                g2d.fillOval(
                        (int) (this.x - this.hitboxDiameter / 2.0),
                        (int) (this.y - this.hitboxDiameter / 2.0),
                        this.hitboxDiameter,
                        this.hitboxDiameter);
            }
        }
 
        g2d.setTransform(oldTransform);
    }

    public void update(ArrayList<Point> path){
        if (reachedEnd || path == null || path.isEmpty()){
            return;
        }
        if (this.slowEffectEndTimeMillis > 0 && System.currentTimeMillis() >= this.slowEffectEndTimeMillis){
            revertSpeed();
        }
        if (currentPathIndex >= path.size()){
            reachedEnd = true;
            return;
        }

        Point targetPoint = path.get(currentPathIndex);
        double targetX = targetPoint.getX();
        double targetY = targetPoint.getY();
        double deltaX = targetX - this.x;
        double deltaY = targetY - this.y;
        double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (deltaX != 0 || deltaY != 0){
            this.currentAngleRadians = Math.atan2(deltaY, deltaX);
        }

        if (distanceToTarget < waypointThreshold || distanceToTarget < this.currentSpeed){
            this.x = targetX;
            this.y = targetY;
            currentPathIndex++;
            if (currentPathIndex >= path.size()){
                reachedEnd = true;
            } else{
                Point newTargetPoint = path.get(currentPathIndex);
                double newDeltaX = newTargetPoint.getX() - this.x;
                double newDeltaY = newTargetPoint.getY() - this.y;
                if (newDeltaX != 0 || newDeltaY != 0){
                    this.currentAngleRadians = Math.atan2(newDeltaY, newDeltaX);
                }
            }
        } else{
            double moveX = (deltaX / distanceToTarget) * currentSpeed;
            double moveY = (deltaY / distanceToTarget) * currentSpeed;
            this.x += moveX;
            this.y += moveY;
        }
    }

    public void takeDamage(int damage){
        this.health -= damage;
        if (this.health < 0){
            this.health = 0;
        }
    }

    public void applySlow(long durationMillis){
        if (!isSlowed()){
            this.currentSpeed = this.originalSpeed * slowRate;
        }
        this.slowEffectEndTimeMillis = System.currentTimeMillis() + durationMillis;
    }

    private void revertSpeed(){
        this.currentSpeed = this.originalSpeed;
        this.slowEffectEndTimeMillis = 0;
    }

    public boolean isSlowed(){
        return this.slowEffectEndTimeMillis > 0 && System.currentTimeMillis() < this.slowEffectEndTimeMillis;
    }

    public double getX(){ return x; }
    public double getY(){ return y; }
    public double getSpeed(){ return currentSpeed; }
    public int getHealth(){ return health; }
    public boolean isAlive(){ return health > 0; }
    public boolean isCamo(){ return camo; }
    public boolean hasReachedEnd(){ return reachedEnd; }
    public int getCurrentPathIndex(){ return currentPathIndex; }
    public String getType(){ return humanType; }
    public int getMoneyReward(){ return moneyReward; }

    public Rectangle getBounds(){
        return new Rectangle((int) (x - hitboxDiameter / 2.0), (int) (y - hitboxDiameter / 2.0), hitboxDiameter, hitboxDiameter);
    }

    public double getDistanceToWaypoint(Point waypoint){
        if (waypoint == null) return Double.MAX_VALUE;
        double dx = waypoint.getX() - this.x;
        double dy = waypoint.getY() - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}