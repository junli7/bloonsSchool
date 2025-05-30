import javax.swing.*;
import java.awt.*;

public class MainFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("johntig Tower Defense");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GameState gameState = new GameState(200);

            // Panel Dimensions (adjust as needed)
            int gamePanelNominalWidth = 800; // GamePanel will effectively take this space
            int gamePanelHeight = 600;
            int upgradePanelWidth = UpgradeControlPanel.PANEL_WIDTH;

            // Instantiate panels
            UpgradeControlPanel upgradeControlPanel = new UpgradeControlPanel(gameState);
            GamePanel gamePanel = new GamePanel(gameState, upgradeControlPanel);
            SideInfoPanel infoPanel = new SideInfoPanel(gameState, gamePanel);

            // Set cross-references
            upgradeControlPanel.setGamePanelRef(gamePanel);

            // Create JLayeredPane that will hold GamePanel and UpgradeControlPanel
            JLayeredPane gameAreaWithOverlay = new JLayeredPane();
            // The JLayeredPane will occupy the space normally taken by GamePanel
            gameAreaWithOverlay.setPreferredSize(new Dimension(gamePanelNominalWidth, gamePanelHeight));

            // Add GamePanel to the base layer of JLayeredPane
            gamePanel.setBounds(0, 0, gamePanelNominalWidth, gamePanelHeight);
            gameAreaWithOverlay.add(gamePanel, JLayeredPane.DEFAULT_LAYER);

            // Add UpgradeControlPanel to a higher layer.
            // Position it to the left of where the SideInfoPanel would start,
            // but within the bounds of the gameAreaWithOverlay.
            // Its X coordinate will be gamePanelNominalWidth - sideInfoPanelWidth - upgradePanelWidth,
            // but this seems wrong. It should overlay the GamePanel, left of SideInfoPanel.
            // Let's position it X = gamePanelNominalWidth - upgradePanelWidth, if gamePanelNominalWidth
            // is the total width available for GamePanel + UpgradeControlPanel.
            // OR, if GamePanel truly fills the center and UCP overlays its right edge:
            // X coordinate for UpgradeControlPanel:
            // gamePanelNominalWidth - upgradePanelWidth (if SideInfoPanel is to the right of this entire block)
            // Let's assume GamePanel gets the full width initially, and UCP overlays its right side.
            // No, you want UCP to the left of SideInfoPanel. So gamePanelNominalWidth should be smaller.
            
            // Corrected logic:
            // The overall frame center will be JLayeredPane.
            // GamePanel is inside JLayeredPane.
            // UpgradeControlPanel is also inside JLayeredPane, positioned over GamePanel.

            // UpgradeControlPanel position:
            // It should appear on the GamePanel, but to the left of where SideInfoPanel starts.
            // If GamePanel has width W_gp, SideInfoPanel has width W_sip.
            // The UpgradeControlPanel (width W_ucp) should be at x = W_gp - W_ucp.
            // But this is relative to the JLayeredPane.
            int upgradePanelX = gamePanelNominalWidth - upgradePanelWidth;

            upgradeControlPanel.setBounds(upgradePanelX, 0, upgradePanelWidth, gamePanelHeight);
            upgradeControlPanel.setVisible(false); // Start hidden
            gameAreaWithOverlay.add(upgradeControlPanel, JLayeredPane.PALETTE_LAYER); // Or MODAL_LAYER

            // Main content panel
            JPanel mainContentPanel = new JPanel(new BorderLayout());
            mainContentPanel.add(gameAreaWithOverlay, BorderLayout.CENTER); // Layered pane in the center
            mainContentPanel.add(infoPanel, BorderLayout.EAST);        // Side info panel on the right

            frame.getContentPane().add(mainContentPanel);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            gamePanel.requestFocusInWindow();
        });
    }
}