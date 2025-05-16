import java.awt.*;
import java.util.ArrayList;

public class Map {
    

    private ArrayList<Point> coordinates = new ArrayList<Point>();


    public Map(ArrayList<Point> coords){
        coordinates = coords;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        for (int i = 1; i<coordinates.size(); i++){
            g2d.drawLine((int)coordinates.get(i-1).getX(),(int)coordinates.get(i-1).getX(),(int)coordinates.get(i).getX(),(int)coordinates.get(i).getX());
        }
    }

    public void update(ArrayList<Human> humans){
        ArrayList<Human> toRemove = new ArrayList<>();

        for (Human h : humans) {
            h.update(setTargetSpeedX(h),setTargetSpeedY(h));
            if (h.getX()>coordinates.get(coordinates.size()-1).getX()) {
                toRemove.add(h);
            }
        }
        humans.removeAll(toRemove);
        
    }

    public ArrayList<Human> spawnHuman(ArrayList<Human> humans){
        humans = new ArrayList<>();
        humans.add(new Human(1, 1, 50,30, 40));

        return humans;
    }

    public double setTargetSpeedX(Human a){
        Point p = coordinates.get(a.getPosition());
        double dx = a.getX() - p.getX();
        double dy = a.getY() - p.getY();

        //unit vector it
        dx/=Math.sqrt((dx*dx)+(dy*dy));

        //mult by speed
        dx*=a.getSpeed();
        
        return dx;
    }

    public double setTargetSpeedY(Human a){
        Point p = coordinates.get(a.getPosition());
        double dx = a.getX() - p.getX();
        double dy = a.getY() - p.getY();

        //unit vector it
        dy/=Math.sqrt((dx*dx)+(dy*dy));

        //mult by speed
        dy*=a.getSpeed();
        
        return dy;

    }


}
