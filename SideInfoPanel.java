import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class SideInfoPanel extends JPanel{
    private GameState gameState;
    private GamePanel gamePanel;

    public static final int panelWidth = 200;
    private static final int buttonHeight = 25;
    private static final int buySpritePreviewSize = 32;
    private static final int buyButtonWidth = 120;

    private final Color defaultButtonColor = new Color(50, 120, 200);
    private final Color hoverButtonColor = new Color(70, 150, 220);
    private final Color pressedButtonColor = new Color(40, 100, 180);
    private final Color disabledButtonColor = Color.GRAY;
    private final Color buttonTextColor = Color.WHITE;

    private JLabel waveStatusLabel;
    private JButton startGameButton;
    private JLabel livesLabel;

    public SideInfoPanel(GameState gameState, GamePanel gamePanel){
        this.gameState = gameState;
        this.gamePanel = gamePanel;

        setPreferredSize(new Dimension(panelWidth, 0));
        setBackground(new Color(220, 220, 220));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel moneyLabel = new JLabel("Money: $" + gameState.getMoney());
        moneyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(moneyLabel);

        livesLabel = new JLabel("Lives: " + gameState.getLives());
        livesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(livesLabel);

        JLabel bloonsKilledLabel = new JLabel("Killed: " + gameState.getBloonsKilled());
        bloonsKilledLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(bloonsKilledLabel);

        waveStatusLabel = new JLabel("Press Start Game");
        waveStatusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(waveStatusLabel);

        add(Box.createRigidArea(new Dimension(0, 15)));
        JPanel startGameButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        startGameButtonPanel.setBackground(this.getBackground());
        startGameButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        startGameButton = new JButton("Start Game");
        styleFlatButton(startGameButton, defaultButtonColor, hoverButtonColor, pressedButtonColor, buttonTextColor);
        startGameButton.addActionListener(e ->{
            if (gamePanel.canStartFirstWave()){
                gamePanel.startNextWave();
            }
        });
        Dimension actualButtonSize = new Dimension(panelWidth - 60,buttonHeight + 5);
        startGameButton.setPreferredSize(actualButtonSize);
        startGameButtonPanel.add(startGameButton);
        startGameButtonPanel.setMaximumSize(new Dimension(panelWidth - 20,buttonHeight + 10));
        add(startGameButtonPanel);


        add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel buyTitleLabel = new JLabel("Buy Monkeys:");
        buyTitleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        add(buyTitleLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        BufferedImage monkeyIdleSprite = SpriteManager.getScaledSprite("monkey_base_idle.png", buySpritePreviewSize, buySpritePreviewSize);
        addBuyButtonWithSprite("Dart Monkey", Monkey.COST, "Standard dart monkey.", monkeyIdleSprite, "Monkey");

        BufferedImage monkeyBIdleSprite = SpriteManager.getScaledSprite("monkey_bomber_idle.png", buySpritePreviewSize, buySpritePreviewSize);
        addBuyButtonWithSprite("Bomb Tower", MonkeyB.COST, "Explosive bombs. Sees camo.", monkeyBIdleSprite, "MonkeyB");

        BufferedImage monkeyCIdleSprite = SpriteManager.getScaledSprite("monkey_slow_idle.png", buySpritePreviewSize, buySpritePreviewSize);
        addBuyButtonWithSprite("Ice Monkey", MonkeyC.COST, "Slows humans in AoE.", monkeyCIdleSprite, "MonkeyC");


        Timer updateTimer = new Timer(100, e ->{
            moneyLabel.setText("Money: $" + gameState.getMoney());
            livesLabel.setText("Lives: " + gameState.getLives());
            bloonsKilledLabel.setText("Killed: " + gameState.getBloonsKilled());

            int currentWave = gameState.getCurrentWave();

            if (gameState.isGameOver()){
                waveStatusLabel.setText("GAME OVER!");
                startGameButton.setText("Restart to Play");
                startGameButton.setEnabled(false);
            } else if (currentWave == 0){
                waveStatusLabel.setText("Press Start Game");
                startGameButton.setText("Start Game");
                startGameButton.setEnabled(gamePanel.canStartFirstWave());
            } else{
                waveStatusLabel.setText("Wave: " + currentWave);
                startGameButton.setText("Wave " + currentWave);
                startGameButton.setEnabled(false);
            }

            if (startGameButton.isEnabled()){
                Point mousePos = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(mousePos, startGameButton);
                if (startGameButton.getBounds().contains(mousePos)){
                    startGameButton.setBackground(hoverButtonColor);
                } else{
                    startGameButton.setBackground(defaultButtonColor);
                }
            } else{
                startGameButton.setBackground(disabledButtonColor);
            }


            boolean buyButtonsEnabled = !gameState.isGameOver();
            Component[] components = this.getComponents();
            for (Component comp : components){
                if (comp instanceof JPanel){
                    JButton button = null;
                    for(Component innerComp : ((JPanel) comp).getComponents()){
                        if(innerComp instanceof JButton){
                            button = (JButton) innerComp;
                            break;
                        }
                    }
                    if (button != null && button != startGameButton){
                        String type = (String) button.getClientProperty("monkeyType");
                        if (type != null){
                            int cost = 0;
                            if ("Monkey".equals(type)) cost = Monkey.COST;
                            else if ("MonkeyB".equals(type)) cost = MonkeyB.COST;
                            else if ("MonkeyC".equals(type)) cost = MonkeyC.COST;

                            boolean affordable = gameState.canAfford(cost);
                            button.setEnabled(buyButtonsEnabled && affordable);
                            
                            if (!button.isEnabled()){
                                button.setBackground(disabledButtonColor);
                            } else{
                                Point mousePos = MouseInfo.getPointerInfo().getLocation();
                                SwingUtilities.convertPointFromScreen(mousePos, button);
                                if (button.getBounds().contains(mousePos)){
                                    button.setBackground(hoverButtonColor);
                                } else{
                                    button.setBackground(defaultButtonColor);
                                }
                            }
                        }
                    }
                }
            }
             revalidate();
             repaint();
        });
        updateTimer.start();
    }

    private void styleFlatButton(JButton button, Color defaultBg, Color hoverBg, Color pressedBg, Color fg){
        button.setForeground(fg);
        button.setBackground(defaultBg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFocusable(false);

        button.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent e){ if (button.isEnabled()) button.setBackground(hoverBg); }
            @Override
            public void mouseExited(MouseEvent e){ if (button.isEnabled()) button.setBackground(defaultBg); }
            @Override
            public void mousePressed(MouseEvent e){ if (button.isEnabled()) button.setBackground(pressedBg); }
            @Override
            public void mouseReleased(MouseEvent e){
                if (button.isEnabled()){
                    if (button.getBounds().contains(e.getPoint())) button.setBackground(hoverBg);
                    else button.setBackground(defaultBg);
                }
            }
        });
    }

    private void addBuyButtonWithSprite(String buttonText, int cost, String tooltip, BufferedImage sprite, String monkeyTypeToPlace){
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.X_AXIS));
        itemPanel.setBackground(this.getBackground());
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        int parentInsetsWidth = getInsets().left + getInsets().right;
        itemPanel.setMaximumSize(new Dimension(panelWidth - parentInsetsWidth, buySpritePreviewSize + 4));


        JLabel spriteLabel = new JLabel(new ImageIcon(sprite));
        itemPanel.add(spriteLabel);
        itemPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        JButton button = new JButton(buttonText + " ($" + cost + ")");
        styleFlatButton(button, defaultButtonColor, hoverButtonColor, pressedButtonColor, buttonTextColor);
        button.setToolTipText(tooltip + " Cost: " + cost);
        button.setFont(new Font("Arial", Font.PLAIN, 10));
        button.putClientProperty("monkeyType", monkeyTypeToPlace);

        Dimension buttonSize = new Dimension(buyButtonWidth,buttonHeight);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);

        button.addActionListener(e ->{
            if (!gameState.isGameOver() && gameState.canAfford(cost)){
                gamePanel.startPlacingMonkey(monkeyTypeToPlace);
            }
        });
        itemPanel.add(button);
        add(itemPanel);
        add(Box.createRigidArea(new Dimension(0, 3)));
    }
}