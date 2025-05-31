import javax.swing.*;
import java.awt.*;

public class MainFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame= new JFrame("Italian Brainrot Tower Defense");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GameState gameState= new GameState(750, 100); //initial money initial lives

            int gamePanelNominalWidth= 800; 
            int gamePanelHeight= 600;
            int upgradePanelWidth= UpgradeControlPanel.panelWidth;

            UpgradeControlPanel upgradeControlPanel = new UpgradeControlPanel(gameState);
            GamePanel gamePanel = new GamePanel(gameState, upgradeControlPanel);
            SideInfoPanel infoPanel = new SideInfoPanel(gameState, gamePanel);

            upgradeControlPanel.setGamePanelRef(gamePanel);

            JLayeredPane gameAreaWithOverlay = new JLayeredPane();
            gameAreaWithOverlay.setPreferredSize(new Dimension(gamePanelNominalWidth, gamePanelHeight));

            gamePanel.setBounds(0, 0, gamePanelNominalWidth, gamePanelHeight);
            gameAreaWithOverlay.add(gamePanel, JLayeredPane.DEFAULT_LAYER);

            int upgradePanelX =gamePanelNominalWidth - upgradePanelWidth;
            upgradeControlPanel.setBounds(upgradePanelX, 0, upgradePanelWidth, gamePanelHeight);
            upgradeControlPanel.setVisible(false); 
            gameAreaWithOverlay.add(upgradeControlPanel, JLayeredPane.PALETTE_LAYER); 

            JPanel mainContentPanel =new JPanel(new BorderLayout());
            mainContentPanel.add(gameAreaWithOverlay, BorderLayout.CENTER); 
            mainContentPanel.add(infoPanel, BorderLayout.EAST);        

            frame.getContentPane().add(mainContentPanel);

            frame.pack();
            frame.setVisible(true);

        });
    }
}