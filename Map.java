import java.awt.*;
import java.util.ArrayList;

public class Map {
    
    private ArrayList<Point> coordinates = new ArrayList<Point>();
    private ArrayList<Human> humans;

    public Map(ArrayList<Point> coords){
        humans = new ArrayList<Human>();
        coordinates = coords;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        for (int i = 1; i<coordinates.size(); i++){
            g2d.drawLine((int)coordinates.get(i-1).getX(),(int)coordinates.get(i-1).getX(),(int)coordinates.get(i).getX(),(int)coordinates.get(i).getX());
        }
    }

    public void update(){
        List<Human> toRemove = new ArrayList<>();

        for (Human h : humans) {
            h.update();
            if (h.getX()>coordinates.get(coordinates.size()-1).getX()) {
                toRemove.add(h);
            }
        }
        humans.removeAll(toRemove);
        
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
