//ENEMIES -> Balloons

public class Human {
    
    private int speed;
    private int hitbox;
    private int health;
    private boolean camo; //property, only certain towers can attack
    private boolean slowed; //temporary effect

    public Human(int s, int h, int hit){
        speed = s;
        health = h;
        hitbox = hit;
        camo = false;
        slowed = false;
    }

        boolean isCamo(){
            return camo;
    }

        boolean isSlowed(){
        return slowed;
    }

}
