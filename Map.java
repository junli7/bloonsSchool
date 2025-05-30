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
    //remove once derek drawsmap
    public void draw(Graphics2D g2d) {
        if (pathCoordinates == null || pathCoordinates.size() < 2) {
            return;
        }
        g2d.setColor(new Color(160, 82, 45));
        g2d.setStroke(new BasicStroke(20));

        for (int i = 0; i < pathCoordinates.size() - 1; i++) {
            Point p1 = pathCoordinates.get(i);
            Point p2 = pathCoordinates.get(i + 1);
            g2d.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
        }
        g2d.setStroke(new BasicStroke(1));
    }

    // Modified to spawn by type
    public Human spawnHumanByType(String humanType, boolean isCamoFromSpawnInstruction) {
        if (pathCoordinates == null || pathCoordinates.isEmpty()) {
            System.err.println("Cannot spawn human: Path is empty or null.");
            return null;
        }
        Point startPoint = pathCoordinates.get(0);
        // Human constructor now takes humanType, startX, startY, isCamoFromSpawn, path
        Human newHuman = new Human(humanType, startPoint.getX(), startPoint.getY(), isCamoFromSpawnInstruction, this.pathCoordinates);
        return newHuman;
    }

    public ArrayList<Point> getPath() {
        return this.pathCoordinates;
    }
}