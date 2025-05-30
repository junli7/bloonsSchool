import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class UpgradeControlPanel extends JPanel {
    private UpgradeGUI upgradeGUI_instance;
    private Monkey currentSelectedMonkey;
    private GameState gameState;
    private GamePanel gamePanel_ref;

    public static final int panelWidth = 190;

    public UpgradeControlPanel(GameState gameState) {
        this.gameState = gameState;
        this.upgradeGUI_instance = new UpgradeGUI();
        this.currentSelectedMonkey = null;

        setBackground(new Color(210, 210, 210)); 
        setOpaque(true); 
        
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        
        setVisible(false); 

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentSelectedMonkey != null && isVisible()) {
                    int actionResult = upgradeGUI_instance.handleClick(e.getX(), e.getY(), currentSelectedMonkey, gameState);
                    
                    if (actionResult == UpgradeGUI.actionSOLD) {
                        if (gamePanel_ref != null) {
                            gamePanel_ref.sellMonkey(currentSelectedMonkey);
                        }
                       
                    } else if (actionResult == UpgradeGUI.actionUpgradedOrArchetypeChosen) {
                        if (gamePanel_ref != null) gamePanel_ref.repaint(); 
                        repaint();
                    } else {
                        repaint();
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentSelectedMonkey != null && isVisible()) {
                    upgradeGUI_instance.updateHover(e.getX(), e.getY(), currentSelectedMonkey, gameState);
                    repaint();
                }
            }
        
        });
    }

    public void setGamePanelRef(GamePanel gamePanel) {
        this.gamePanel_ref = gamePanel;
    }

    public void setSelectedMonkey(Monkey monkey) {
        this.currentSelectedMonkey = monkey;
        if (this.currentSelectedMonkey != null && this.isVisible()) {
             Point mousePosRelativeToThisPanel = getMousePosition(); 
             if (mousePosRelativeToThisPanel != null) {
                upgradeGUI_instance.updateHover(mousePosRelativeToThisPanel.x, mousePosRelativeToThisPanel.y, this.currentSelectedMonkey, gameState);
             } else {
                upgradeGUI_instance.updateHover(-100, -100, this.currentSelectedMonkey, gameState);
             }
        } else {
            upgradeGUI_instance.updateHover(-100, -100, null, gameState);
        }
        repaint(); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (currentSelectedMonkey != null) {
            upgradeGUI_instance.draw(g2d, currentSelectedMonkey, gameState, getWidth(), getHeight());
        }
    }
}