
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;

public class SpriteManager {
    private static Map<String, BufferedImage> spriteCache = new HashMap<>();
    private static BufferedImage placeholderSprite; // This is your static placeholder

    static {
        // Create a simple placeholder sprite (e.g., a magenta square)
        placeholderSprite = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholderSprite.createGraphics();
        g.setColor(new Color(255, 0, 255, 150)); // Magenta, slightly transparent
        g.fillRect(0, 0, 32, 32);
        g.setColor(Color.BLACK);
        g.drawString("N/A", 5, 22);
        g.dispose();
    }

    public static BufferedImage getPlaceholderSprite() {
        return placeholderSprite;
    }

    
     public static BufferedImage getSprite(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            //System.err.println("Sprite path is null or empty. Using placeholder.");
            // spriteCache.put(resourcePath, placeholderSprite); // Don't cache for null/empty key
            return placeholderSprite;
        }
        if (spriteCache.containsKey(resourcePath)) {
            return spriteCache.get(resourcePath);
        }

        try {
            InputStream inputStream = SpriteManager.class.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                System.err.println("Sprite resource not found: " + resourcePath + ". Using placeholder. Ensure it's in classpath (e.g., 'resources' folder).");
                spriteCache.put(resourcePath, placeholderSprite);
                return placeholderSprite;
            }
            BufferedImage sprite = ImageIO.read(inputStream);
            spriteCache.put(resourcePath, sprite);
            // System.out.println("Successfully loaded sprite: " + resourcePath);
            return sprite;
        } catch (IOException e) {
            System.err.println("Error loading sprite: " + resourcePath + ". Using placeholder.");
            e.printStackTrace();
            spriteCache.put(resourcePath, placeholderSprite);
            return placeholderSprite;
        }
    }

    public static BufferedImage getScaledSprite(String resourcePath, int width, int height) {
        BufferedImage originalSprite = getSprite(resourcePath);

        if (width <= 0 || height <= 0) { // Invalid dimensions
             if (originalSprite == placeholderSprite) { // If already placeholder, return it as is if dims invalid
                return placeholderSprite;
             }
             // If valid sprite but invalid dims, return original unscaled
             // or consider returning placeholder based on your preference.
             // For now, let's return the original if it's not placeholder.
             return originalSprite;
        }
        
        // If original is the placeholder, scale the placeholder
        if (originalSprite == placeholderSprite) {
            BufferedImage scaledPlaceholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaledPlaceholder.createGraphics();
            g.drawImage(placeholderSprite, 0, 0, width, height, null);
            g.dispose();
            return scaledPlaceholder;
        }

        // If it's a valid sprite and valid dimensions, scale it
        Image scaledInstance = originalSprite.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(scaledInstance, 0, 0, null);
        g2d.dispose();
        return scaledImage;
    }
}