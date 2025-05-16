import java.awt.Color;
import java.util.List; // If you manipulate projectiles directly, not needed for basic setup
import java.util.ArrayList; // If you manipulate projectiles directly

public class MonkeyB extends Monkey {

    public MonkeyB(int nx, int ny, double nrange, double nhitbox, int nlevel) {
        super(nx, ny, nrange, nhitbox, nlevel);
        setColor(Color.BLUE, Color.BLACK);
        setProjectileRadius(10);
        setProjectileSpeed(5); // Adjusted speed
        setShootCooldown(300L);   // Use L for long, slightly faster cooldown
        this.canSeeCamo = true; // Example: MonkeyB can always see camo
    }

    // You can override findTarget or shootAtTarget for unique behavior
    // For example, MonkeyB might prioritize "Strongest" instead of "First"
}