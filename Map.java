import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.BasicStroke;
import java.util.ArrayList;

public class Map {
    private ArrayList<Point> pathCoordinates;

    public Map(ArrayList<Point> coords) {
        this.pathCoordinates = coords;
    }


    public Human spawnHumanByType(String humanType, boolean isCamoFromSpawnInstruction) {
        if (pathCoordinates == null || pathCoordinates.isEmpty()) {
            System.err.println("Cannot spawn human: Path is empty or null.");
            return null;
        }
        Point startPoint = pathCoordinates.get(0);
        Human newHuman = new Human(humanType, startPoint.getX(), startPoint.getY(), isCamoFromSpawnInstruction, this.pathCoordinates);
        return newHuman;
    }

    public ArrayList<Point> getPath() {
        return this.pathCoordinates;
    }
}