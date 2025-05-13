import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Path {
    private List<Point> waypoints;

    public Path() {
        waypoints = new ArrayList<>();
        // Define a simple path (you can make this more complex)
        waypoints.add(new Point(0, 100));
        waypoints.add(new Point(200, 100));
        waypoints.add(new Point(200, 300));
        waypoints.add(new Point(500, 300));
        waypoints.add(new Point(500, 100));
        waypoints.add(new Point(Main.SCREEN_WIDTH, 100)); // Exit point
    }

    public List<Point> getWaypoints() {
        return waypoints;
    }

    public Point getWaypoint(int index) {
        if (index >= 0 && index < waypoints.size()) {
            return waypoints.get(index);
        }
        return null; // Or throw exception
    }

    public int getNumWaypoints() {
        return waypoints.size();
    }
}