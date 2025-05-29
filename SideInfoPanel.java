import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter; // For hover effects
import java.awt.event.MouseEvent;   // For hover effects
import java.awt.image.BufferedImage;

public class SideInfoPanel extends JPanel {
    private GameState gameState;
    private GamePanel gamePanel;

    private static final int PANEL_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 25;
    private static final int BUY_BUTTON_SPRITE_PREVIEW_SIZE = 32;
    private static final int FIXED_BUY_BUTTON_WIDTH = 120;
    private static final String MONKEY_C_IDLE_SPRITE_PATH = "monkey_ice_idle.png";

    // --- Button Colors (similar to UpgradeGUI) ---
    private final Color defaultButtonColor = new Color(50, 120, 200); // A pleasant blue
    private final Color hoverButtonColor = new Color(70, 150, 220);   // Lighter blue for hover
    private final Color pressedButtonColor = new Color(40, 100, 180);  // Darker blue for pressed
    private final Color disabledButtonColor = Color.GRAY;
    private final Color buttonTextColor = Color.WHITE;


    public SideInfoPanel(GameState gameState, GamePanel gamePanel) {
        this.gameState = gameState;
        this.gamePanel = gamePanel;

        // ToolTipManager.sharedInstance().setInitialDelay(300); // Optional

        setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        setBackground(new Color(220, 220, 220));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Info Labels ---
        // ... (info labels remain the same) ...
        JLabel moneyLabel = new JLabel("Money: $0");
        moneyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        moneyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(moneyLabel);

        JLabel bloonsKilledLabel = new JLabel("Killed: 0");
        bloonsKilledLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        bloonsKilledLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(bloonsKilledLabel);

        JLabel waveLabel = new JLabel("Wave: 1");
        waveLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        waveLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(waveLabel);

        add(Box.createRigidArea(new Dimension(0, 20)));


        // --- Next Wave Button ---
        JPanel nextWaveButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        nextWaveButtonPanel.setBackground(this.getBackground());
        nextWaveButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton nextWaveButton = new JButton("Next Wave");
        styleFlatButton(nextWaveButton, defaultButtonColor, hoverButtonColor, pressedButtonColor, buttonTextColor); // Apply style
        nextWaveButton.addActionListener(e -> gameState.incrementWave());
        // setFocusable is done in styleFlatButton
        Dimension actualButtonSizeNW = new Dimension(PANEL_WIDTH - 60, BUTTON_HEIGHT + 5); // Slightly taller
        nextWaveButton.setPreferredSize(actualButtonSizeNW);
        nextWaveButtonPanel.add(nextWaveButton);
        nextWaveButtonPanel.setMaximumSize(new Dimension(PANEL_WIDTH - 20, BUTTON_HEIGHT + 10));
        add(nextWaveButtonPanel);

        add(Box.createRigidArea(new Dimension(0, 20)));

        // --- Buy Monkeys Section ---
        JLabel buyTitleLabel = new JLabel("Buy Monkeys:");
        buyTitleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        buyTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(buyTitleLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        BufferedImage monkeyIdleSprite = SpriteManager.getScaledSprite("monkey_base_idle.png", BUY_BUTTON_SPRITE_PREVIEW_SIZE, BUY_BUTTON_SPRITE_PREVIEW_SIZE);
        addBuyButtonWithSprite("Monkey", Monkey.COST, "Standard dart-throwing monkey.", monkeyIdleSprite, "Monkey");

        BufferedImage monkeyBIdleSprite = SpriteManager.getScaledSprite("monkey_bomber_idle.png", BUY_BUTTON_SPRITE_PREVIEW_SIZE, BUY_BUTTON_SPRITE_PREVIEW_SIZE);
        addBuyButtonWithSprite("Bomber", MonkeyB.COST, "Explosive bomb-thrower. Sees camo.", monkeyBIdleSprite, "MonkeyB");

        BufferedImage monkeyCIdleSprite = SpriteManager.getScaledSprite(MONKEY_C_IDLE_SPRITE_PATH, BUY_BUTTON_SPRITE_PREVIEW_SIZE, BUY_BUTTON_SPRITE_PREVIEW_SIZE);
        addBuyButtonWithSprite("Ice Monkey", MonkeyC.COST, "Slows humans in an area.", monkeyCIdleSprite, "MonkeyC");


        Timer updateTimer = new Timer(200, e -> {
            moneyLabel.setText("Money: $" + gameState.getMoney());
            bloonsKilledLabel.setText("Killed: " + gameState.getBloonsKilled());
            waveLabel.setText("Wave: " + gameState.getCurrentWave());

            // Update button enabled state and appearance if disabled
            Component[] components = this.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    JButton button = null;
                    for (Component innerComp : panel.getComponents()) {
                        if (innerComp instanceof JButton) {
                            button = (JButton) innerComp;
                            break;
                        }
                    }
                    if (button != null) {
                        String type = (String) button.getClientProperty("monkeyType");
                        boolean affordable = true; // Assume affordable for NextWave button
                        if (type != null) { // It's a buy button
                            int cost = 0;
                            if ("Monkey".equals(type)) cost = Monkey.COST;
                            else if ("MonkeyB".equals(type)) cost = MonkeyB.COST;
                            else if ("MonkeyC".equals(type)) cost = MonkeyC.COST;
                            affordable = gameState.canAfford(cost);
                        }
                        button.setEnabled(affordable);
                        if (!affordable) {
                            button.setBackground(disabledButtonColor); // Set disabled color
                        } else {
                            // Restore default color if it was previously disabled and now enabled
                            // The MouseListener will handle hover/default for enabled buttons
                            // Check if mouse is over it, if so, set hover, else default
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
        });
        updateTimer.start();
    }

    /**
     * Helper method to style a JButton to look flat with hover effects.
     */
    private void styleFlatButton(JButton button, Color defaultBg, Color hoverBg, Color pressedBg, Color fg) {
        button.setForeground(fg);
        button.setBackground(defaultBg);
        button.setFocusPainted(false);
        button.setBorderPainted(false); // Remove L&F border
        button.setOpaque(true); // Ensure background is painted
        button.setFocusable(false); // From your original code

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(defaultBg);
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(pressedBg);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                 if (button.isEnabled()) {
                    // Check if mouse is still over the button
                    if (button.getBounds().contains(e.getPoint())) {
                         button.setBackground(hoverBg);
                    } else {
                         button.setBackground(defaultBg);
                    }
                }
            }
        });
    }

    private void addBuyButtonWithSprite(String buttonText, int cost, String tooltip, BufferedImage sprite, String monkeyTypeToPlace) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.X_AXIS));
        itemPanel.setBackground(this.getBackground());
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        int itemPanelHeight = BUY_BUTTON_SPRITE_PREVIEW_SIZE + 4;
        Insets parentBorderInsets = this.getBorder() != null ? this.getBorder().getBorderInsets(this) : new Insets(0,0,0,0);
        int availableWidthForItemPanel = PANEL_WIDTH - parentBorderInsets.left - parentBorderInsets.right;
        itemPanel.setMaximumSize(new Dimension(availableWidthForItemPanel, itemPanelHeight));
        itemPanel.setPreferredSize(new Dimension(availableWidthForItemPanel, itemPanelHeight));

        JLabel spriteLabel = new JLabel(new ImageIcon(sprite));
        spriteLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        itemPanel.add(spriteLabel);
        itemPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        JButton button = new JButton(buttonText + " (" + cost + ")");
        styleFlatButton(button, defaultButtonColor, hoverButtonColor, pressedButtonColor, buttonTextColor); // Apply style
        button.setToolTipText(tooltip);
        Dimension buttonSize = new Dimension(FIXED_BUY_BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);
        // button.setMargin(new Insets(2, 5, 2, 5)); // Not needed if borderPainted is false
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.putClientProperty("monkeyType", monkeyTypeToPlace);
        button.setAlignmentY(Component.CENTER_ALIGNMENT);

        button.addActionListener(e -> { /* action listener remains the same */
            if (gameState.canAfford(cost)) {
                gamePanel.startPlacingMonkey(monkeyTypeToPlace);
            } else {
                System.out.println("Not enough money to buy " + buttonText + ". Need: " + cost);
            }
        });
        // setFocusable is done in styleFlatButton
        itemPanel.add(button);
        itemPanel.add(Box.createHorizontalGlue());
        add(itemPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}