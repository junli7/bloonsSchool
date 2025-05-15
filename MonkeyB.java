import java.util.List;
import java.util.ArrayList;
import java.awt.*;

public class MonkeyB extends Monkey {
    
    public MonkeyB(int nx, int ny, double nrange, double nhitbox, int nlevel){
        super(nx,  ny,  nrange,  nhitbox,  nlevel);
        setColor(Color.BLUE, Color.BLACK);
        setprojectRadius(10);
        setprojectileSpeed(20);
        setShootCooldown(5);


    }
}
