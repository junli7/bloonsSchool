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

    public Human(int s, int h, int hit, int x, int y){
        this.x = x;
        this.y = y;
        speed = s;
        health = h;
        hitbox = hit;
        position = 0;
        camo = false;
        slowed = false;
    }

    public void draw(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.BLUE);
        g2d.drawOval(x, y, hitbox, hitbox); //not sure if hitbox is radius or diameter, check later
        
        
    }

    public void update() {
        x += dx;
        y += dy;
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

    public int getdx(){
        return dx;
    }

    public int getdy(){
        return dy;
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

