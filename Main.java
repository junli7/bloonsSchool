import javax.swing.*;
import java.awt.*;

public class Main {
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simple Bloons TD Clone");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            frame.setResizable(false);

            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);
            frame.pack(); // Adjusts frame size to fit preferred size of components
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);

            gamePanel.startGameLoop(); // Start the game loop
        });
    }
}