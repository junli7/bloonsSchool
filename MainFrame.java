import javax.swing.*;
import java.awt.*;

public class MainFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("johntig Tower Defense"); // Changed title slightly
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GameState gameState = new GameState(500); // Start with 500 money

            GamePanel gamePanel = new GamePanel(gameState); // Create GamePanel first

            // Pass gamePanel to SideInfoPanel
            SideInfoPanel infoPanel = new SideInfoPanel(gameState, gamePanel);

            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(gamePanel, BorderLayout.CENTER);
            frame.getContentPane().add(infoPanel, BorderLayout.EAST);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            gamePanel.requestFocusInWindow();
        });
    }
}