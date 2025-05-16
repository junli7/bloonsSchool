//ENEMIES -> Balloons

import java.util.List;
import java.util.ArrayList;
import java.awt.*;

public class Human {
    
    private int speed;
    private int dx;
    private int dy;
    private int hitbox;
    private int health;
    private int x;
    private int y;
    private int position; //index of point that the balloon is following, idk how to explain
    private boolean camo; //property, only certain towers can attack
    private boolean slowed; //temporary effect

    public Human(int speedc, int healthc, int hit, int x1, int y1){
        this.x = x1;
        this.y = y1;
        speed = speedc;
        health = healthc;
        hitbox = hit;
        position = 0;
        camo = false;
        slowed = false;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);
        g2d.drawOval(x, y, hitbox, hitbox); //not sure if hitbox is radius or diameter, check later
        
    }

    public void update(double dx1, double dy1) {
        x += dx1; //dx calculated from path
        y += dy1;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getSpeed(){
        return speed;
    }



    public int getPosition(){
        return position;
    }

    public void setDir(){ //sets the dx and dy

    }

    public boolean isCamo(){
        return camo;
    }

    public boolean isSlowed(){
        return slowed;
    }

}

