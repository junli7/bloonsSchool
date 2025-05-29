
import javax.swing.*;
import java.awt.*; // Import BorderLayout

public class MainFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("johntig");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create GameState - this will be shared
            GameState gameState = new GameState(500); // Start with 500 money

            // Create the main game panel
            GamePanel gamePanel = new GamePanel(gameState);

            // Create the side info panel
            SideInfoPanel infoPanel = new SideInfoPanel(gameState);

            // Set layout for the JFrame's content pane
            frame.getContentPane().setLayout(new BorderLayout());

            // Add panels to the frame
            frame.getContentPane().add(gamePanel, BorderLayout.CENTER);
            frame.getContentPane().add(infoPanel, BorderLayout.EAST);

            frame.pack(); // Pack the frame to fit its components
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);

            gamePanel.requestFocusInWindow(); // Ensure game panel can receive focus
        });
    }
}