import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

public class Map {
    
    private ArrayList<Point> coordinates = new ArrayList<Point>();

    public Map(ArrayList<Point> coords){
        coordinates = coords;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        for (int i = 1; i<coordinates.size(); i++){
            g2d.drawLine(coordinates.get(i-1).getX(),coordinates.get(i-1).getX(),coordinates.get(i).getX(),coordinates.get(i).getX());
        }
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

