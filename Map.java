
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.BasicStroke;
import java.util.ArrayList;

public class Map {
    private ArrayList<Point> pathCoordinates;

    public Map(ArrayList<Point> coords) {
        if (coords == null || coords.isEmpty()) {
            System.err.println("Map initialized with no path coordinates! Creating a default path.");
            this.pathCoordinates = new ArrayList<>();
            // Example default path
            this.pathCoordinates.add(new Point(0, 300));
            this.pathCoordinates.add(new Point(750, 300));
        } else {
            this.pathCoordinates = coords;
        }
    }

    public void draw(Graphics2D g2d) {
        if (pathCoordinates.size() < 2) {
            return; // Not enough points to draw a path
        }
        g2d.setColor(new Color(160, 82, 45)); // Sienna color for path
        g2d.setStroke(new BasicStroke(20)); // Make the path visually thicker

        for (int i = 0; i < pathCoordinates.size() - 1; i++) {
            Point p1 = pathCoordinates.get(i);
            Point p2 = pathCoordinates.get(i + 1);
            // Corrected drawLine:
            g2d.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
        }
        g2d.setStroke(new BasicStroke(1)); // Reset stroke to default for other drawings
    }

    /**
     * Spawns a new Human (Balloon) at the start of this map's path.
     * @param speed Speed of the human.
     * @param health Health of the human.
     * @param hitboxDiameter Diameter of the human's hitbox.
     * @param isCamo Whether the human is a camo type.
     * @return A new Human object, or null if path is not defined.
     */
    public Human spawnHuman(double speed, int health, int hitboxDiameter, boolean isCamo) {
        if (pathCoordinates.isEmpty()) {
            System.err.println("Cannot spawn human: Path is empty.");
            return null;
        }
        Point startPoint = pathCoordinates.get(0); // Spawn at the very first point of the path
        // The Human will target pathCoordinates.get(0) and immediately advance to target pathCoordinates.get(1)
        Human newHuman = new Human(startPoint.getX(), startPoint.getY(), speed, health, hitboxDiameter, isCamo);
        // If you want the first target to be the *second* point in the path:
        // human.currentPathIndex = 1; // (after ensuring path has at least 2 points)
        return newHuman;
    }

    public ArrayList<Point> getPath() {
        return this.pathCoordinates;
    }

    // The setTargetSpeedX, setTargetSpeedY, and old update(ArrayList<Human>) methods
    // are removed from Map as Human now handles its own movement along the path.
}