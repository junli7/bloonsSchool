
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.awt.Graphics2D;
import java.awt.Image;

public class SpriteManager{
    private static Map<String, BufferedImage> spriteCache=new HashMap<>();
    private static BufferedImage placeholderSprite;

    public static BufferedImage getPlaceholderSprite(){
        return placeholderSprite;
    }

    
     public static BufferedImage getSprite(String resourcePath){
        if (spriteCache.containsKey(resourcePath)){
            return spriteCache.get(resourcePath);
        }

        try{
            InputStream inputStream=SpriteManager.class.getResourceAsStream(resourcePath);
            BufferedImage sprite =ImageIO.read(inputStream);
            spriteCache.put(resourcePath, sprite);
            return sprite;
        } catch (IOException e){
            System.out.println("Error loading sprite: " + resourcePath + ". Using placeholder.");
            e.printStackTrace();
            spriteCache.put(resourcePath, placeholderSprite);
            return placeholderSprite;
        }
    }

    public static BufferedImage getScaledSprite(String resourcePath, int width, int height){
        BufferedImage originalSprite=getSprite(resourcePath);

        Image scaledInstance=originalSprite.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage scaledImage=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d=scaledImage.createGraphics();
        g2d.drawImage(scaledInstance, 0, 0, null);
        g2d.dispose();
        return scaledImage;
    }
}