import java.awt.Point;
import java.util.ArrayList;

public class Map {
    
    private ArrayList<Point> coordinates = new ArrayList<Point>();

    public Map(ArrayList<Point> coords){
        coordinates = coords;
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

