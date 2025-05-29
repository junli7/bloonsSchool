import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class SideInfoPanel extends JPanel {
    private GameState gameState;
    private GamePanel gamePanel; // To call startNextWave

    private static final int PANEL_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 25;
    private static final int BUY_BUTTON_SPRITE_PREVIEW_SIZE = 32;
    private static final int FIXED_BUY_BUTTON_WIDTH = 120;

    private final Color defaultButtonColor = new Color(50, 120, 200);
    private final Color hoverButtonColor = new Color(70, 150, 220);
    private final Color pressedButtonColor = new Color(40, 100, 180);
    private final Color disabledButtonColor = Color.GRAY;
    private final Color buttonTextColor = Color.WHITE;

    private JLabel waveStatusLabel; // To show wave status

    public SideInfoPanel(GameState gameState, GamePanel gamePanel) {
        this.gameState = gameState;
        this.gamePanel = gamePanel;

        setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        setBackground(new Color(220, 220, 220));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel moneyLabel = new JLabel("Money: $" + gameState.getMoney());
        moneyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(moneyLabel);

        JLabel bloonsKilledLabel = new JLabel("Killed: " + gameState.getBloonsKilled());
        bloonsKilledLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(bloonsKilledLabel);

        waveStatusLabel = new JLabel("Wave: 0"); // Initial wave status
        waveStatusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(waveStatusLabel);

        add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel nextWaveButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        nextWaveButtonPanel.setBackground(this.getBackground());
        nextWaveButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton nextWaveButton = new JButton("Start Wave");
        styleFlatButton(nextWaveButton, defaultButtonColor, hoverButtonColor, pressedButtonColor, buttonTextColor);
        nextWaveButton.addActionListener(e -> {
            if (gamePanel.canStartNextWave()) {
                // GameState.incrementWave() is now called *inside* gamePanel.startNextWave()
                // or at least the GameState wave number is updated based on GamePanel's logic.
                // For now, GamePanel handles advancing GameState's wave counter before starting.
                gamePanel.startNextWave();
            } else {
                System.out.println("Cannot start next wave yet.");
                // Optionally, update a status label or provide visual feedback
            }
        });
        Dimension actualButtonSizeNW = new Dimension(PANEL_WIDTH - 60, BUTTON_HEIGHT + 5);
        nextWaveButton.setPreferredSize(actualButtonSizeNW);
        nextWaveButtonPanel.add(nextWaveButton);
        nextWaveButtonPanel.setMaximumSize(new Dimension(PANEL_WIDTH - 20, BUTTON_HEIGHT + 10));
        add(nextWaveButtonPanel);


        add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel buyTitleLabel = new JLabel("Buy Monkeys:");
        buyTitleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        add(buyTitleLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        BufferedImage monkeyIdleSprite = SpriteManager.getScaledSprite("monkey_base_idle.png", BUY_BUTTON_SPRITE_PREVIEW_SIZE, BUY_BUTTON_SPRITE_PREVIEW_SIZE);
        addBuyButtonWithSprite("Dart Monkey", Monkey.COST, "Standard dart monkey.", monkeyIdleSprite, "Monkey");

        BufferedImage monkeyBIdleSprite = SpriteManager.getScaledSprite("monkey_bomber_idle.png", BUY_BUTTON_SPRITE_PREVIEW_SIZE, BUY_BUTTON_SPRITE_PREVIEW_SIZE);
        addBuyButtonWithSprite("Bomb Tower", MonkeyB.COST, "Explosive bombs. Sees camo.", monkeyBIdleSprite, "MonkeyB");
        
        BufferedImage monkeyCIdleSprite = SpriteManager.getScaledSprite("monkey_slow_idle.png", BUY_BUTTON_SPRITE_PREVIEW_SIZE, BUY_BUTTON_SPRITE_PREVIEW_SIZE);
        addBuyButtonWithSprite("Ice Monkey", MonkeyC.COST, "Slows humans in AoE.", monkeyCIdleSprite, "MonkeyC");


        Timer updateTimer = new Timer(100, e -> { // Faster update for responsiveness
            moneyLabel.setText("Money: $" + gameState.getMoney());
            bloonsKilledLabel.setText("Killed: " + gameState.getBloonsKilled());
            if (gameState.getCurrentWave() == 0) {
                waveStatusLabel.setText("Press Start Wave");
                nextWaveButton.setText("Start Wave");
            } else {
                waveStatusLabel.setText("Wave: " + gameState.getCurrentWave());
                 if (gamePanel.canStartNextWave()) {
                    nextWaveButton.setText("Next Wave (" + (gameState.getCurrentWave()+1) +")");
                    nextWaveButton.setEnabled(true);
                    // Reset to default color, mouse listener will handle hover
                    Point mousePos = MouseInfo.getPointerInfo().getLocation();
                    SwingUtilities.convertPointFromScreen(mousePos, nextWaveButton);
                    if (nextWaveButton.getBounds().contains(mousePos)) {
                        nextWaveButton.setBackground(hoverButtonColor);
                    } else {
                        nextWaveButton.setBackground(defaultButtonColor);
                    }
                } else {
                    nextWaveButton.setText("Wave " + gameState.getCurrentWave() + "...");
                    nextWaveButton.setEnabled(false);
                    nextWaveButton.setBackground(disabledButtonColor);
                }
            }


            // Update buy button enabled state
            Component[] components = this.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) { // The itemPanel
                    JButton button = null;
                    for(Component innerComp : ((JPanel) comp).getComponents()){
                        if(innerComp instanceof JButton){
                            button = (JButton) innerComp;
                            break;
                        }
                    }
                    if (button != null && button != nextWaveButton) { // Exclude the nextWaveButton
                        String type = (String) button.getClientProperty("monkeyType");
                        if (type != null) {
                            int cost = 0;
                            if ("Monkey".equals(type)) cost = Monkey.COST;
                            else if ("MonkeyB".equals(type)) cost = MonkeyB.COST;
                            else if ("MonkeyC".equals(type)) cost = MonkeyC.COST;

                            boolean affordable = gameState.canAfford(cost);
                            button.setEnabled(affordable);
                            if (!affordable) {
                                button.setBackground(disabledButtonColor);
                            } else {
                                // Restore default/hover color if it was previously disabled
                                Point mousePos = MouseInfo.getPointerInfo().getLocation();
                                SwingUtilities.convertPointFromScreen(mousePos, button);
                                if (button.getBounds().contains(mousePos)) {
                                    button.setBackground(hoverButtonColor);
                                } else {
                                    button.setBackground(defaultButtonColor);
                                }
                            }
                        }
                    }
                }
            }
             revalidate(); // Revalidate for layout changes if any
             repaint();    // Repaint for color changes
        });
        updateTimer.start();
    }

    private void styleFlatButton(JButton button, Color defaultBg, Color hoverBg, Color pressedBg, Color fg) {
        button.setForeground(fg);
        button.setBackground(defaultBg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFocusable(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { if (button.isEnabled()) button.setBackground(hoverBg); }
            @Override
            public void mouseExited(MouseEvent e) { if (button.isEnabled()) button.setBackground(defaultBg); }
            @Override
            public void mousePressed(MouseEvent e) { if (button.isEnabled()) button.setBackground(pressedBg); }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.isEnabled()) {
                    if (button.getBounds().contains(e.getPoint())) button.setBackground(hoverBg);
                    else button.setBackground(defaultBg);
                }
            }
        });
    }

    private void addBuyButtonWithSprite(String buttonText, int cost, String tooltip, BufferedImage sprite, String monkeyTypeToPlace) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.X_AXIS));
        itemPanel.setBackground(this.getBackground());
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Calculate max width properly
        int parentInsetsWidth = getInsets().left + getInsets().right;
        itemPanel.setMaximumSize(new Dimension(PANEL_WIDTH - parentInsetsWidth, BUY_BUTTON_SPRITE_PREVIEW_SIZE + 4));


        JLabel spriteLabel = new JLabel(new ImageIcon(sprite));
        itemPanel.add(spriteLabel);
        itemPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        JButton button = new JButton(buttonText + " ($" + cost + ")");
        styleFlatButton(button, defaultButtonColor, hoverButtonColor, pressedButtonColor, buttonTextColor);
        button.setToolTipText(tooltip + " Cost: " + cost);
        button.setFont(new Font("Arial", Font.PLAIN, 10)); // Smaller font for more text
        button.putClientProperty("monkeyType", monkeyTypeToPlace); // Store type for affordability check
        
        Dimension buttonSize = new Dimension(FIXED_BUY_BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);

        button.addActionListener(e -> {
            if (gameState.canAfford(cost)) {
                gamePanel.startPlacingMonkey(monkeyTypeToPlace);
            } else {
                System.out.println("Not enough money to buy " + buttonText);
            }
        });
        itemPanel.add(button);
        add(itemPanel);
        add(Box.createRigidArea(new Dimension(0, 3))); // Tighter spacing
    }

    // paintComponent can be removed if not doing custom painting here
}