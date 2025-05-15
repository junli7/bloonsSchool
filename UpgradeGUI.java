import java.awt.*;

public class UpgradeGUI {
    private Rectangle upgradeButtonBounds;
    private static final int BUTTON_WIDTH = 130;
    private static final int BUTTON_HEIGHT = 30;
    private static final int PADDING = 5;

    private Color defaultButtonColor = new Color(0, 100, 0); // Dark Green
    private Color hoverButtonColor = new Color(0, 150, 0); // Brighter Green
    private Color disabledButtonColor = Color.GRAY;
    private Color currentButtonColor;


    public UpgradeGUI() {
        this.upgradeButtonBounds = new Rectangle(); // Will be updated in draw
        this.currentButtonColor = defaultButtonColor;
    }

    // Call this from GamePanel's mouseMoved if you want hover effects
    public void updateHover(int mouseX, int mouseY, Monkey selectedMonkey, GameState gameState) {
        if (selectedMonkey != null && selectedMonkey.isSelected() && upgradeButtonBounds.contains(mouseX, mouseY)) {
            if (gameState.canAfford(selectedMonkey.getUpgradeCost())) {
                currentButtonColor = hoverButtonColor;
            } else {
                currentButtonColor = disabledButtonColor; // Still hover, but show disabled
            }
        } else if (selectedMonkey != null && selectedMonkey.isSelected()){
             if (gameState.canAfford(selectedMonkey.getUpgradeCost())) {
                currentButtonColor = defaultButtonColor;
            } else {
                currentButtonColor = disabledButtonColor;
            }
        } else {
            currentButtonColor = defaultButtonColor; // Reset if not hovering or no monkey selected
        }
    }


    public void draw(Graphics2D g2d, Monkey selectedMonkey, GameState gameState) {
        if (selectedMonkey == null || !selectedMonkey.isSelected()) {
            upgradeButtonBounds.setBounds(0,0,0,0); // Invalidate bounds
            return; // Don't draw if no monkey is selected
        }

        // Position the GUI, e.g., above the monkey
        // Adjust yOffset to place it relative to the monkey's hitbox and level text
        int yOffset = (int) selectedMonkey.getHitbox() / 2 + g2d.getFontMetrics().getHeight() + PADDING * 3;
        int guiX = selectedMonkey.getX() - BUTTON_WIDTH / 2;
        int guiY = Math.max(0, selectedMonkey.getY() - yOffset - BUTTON_HEIGHT); // Ensure not off-screen top

        upgradeButtonBounds.setBounds(guiX, guiY, BUTTON_WIDTH, BUTTON_HEIGHT);

        // Determine button color based on affordability
        Color actualButtonColor = currentButtonColor; // Use hover/default color
        if (!gameState.canAfford(selectedMonkey.getUpgradeCost())) {
            if (currentButtonColor != hoverButtonColor) { // If not hovering, use disabled color
                 actualButtonColor = disabledButtonColor;
            }
            // if hovering over a disabled button, hover logic in updateHover already sets it
        }


        // Draw button background
        g2d.setColor(actualButtonColor);
        g2d.fillRect(upgradeButtonBounds.x, upgradeButtonBounds.y, upgradeButtonBounds.width, upgradeButtonBounds.height);

        // Draw button border
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(upgradeButtonBounds.x, upgradeButtonBounds.y, upgradeButtonBounds.width, upgradeButtonBounds.height);

        // Draw text
        g2d.setColor(Color.WHITE);
        String text = "Upgrade (L" + (selectedMonkey.getLevel() + 1) + ")";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, guiX + (BUTTON_WIDTH - textWidth) / 2, guiY + fm.getAscent() + (BUTTON_HEIGHT - fm.getHeight()) / 2);

        // Draw cost below button
        String costText = "Cost: " + selectedMonkey.getUpgradeCost();
        int costTextWidth = fm.stringWidth(costText);
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 12)); // Slightly smaller font for cost
        g2d.drawString(costText, guiX + (BUTTON_WIDTH - costTextWidth) / 2, guiY + BUTTON_HEIGHT + fm.getAscent() + PADDING);
        g2d.setFont(fm.getFont()); // Reset font
    }

    public boolean handleClick(int clickX, int clickY, Monkey selectedMonkey, GameState gameState) {
        if (selectedMonkey != null && selectedMonkey.isSelected() && upgradeButtonBounds.contains(clickX, clickY)) {
            if (gameState.canAfford(selectedMonkey.getUpgradeCost())) {
                gameState.spendMoney(selectedMonkey.getUpgradeCost());
                selectedMonkey.upgrade();
                // After upgrade, check affordability for the new button color state
                updateHover(clickX, clickY, selectedMonkey, gameState); // To reset color if needed
                return true; // Click handled, upgrade performed
            } else {
                System.out.println("Cannot afford upgrade for monkey at ("+selectedMonkey.getX()+","+selectedMonkey.getY()+"). Cost: " + selectedMonkey.getUpgradeCost() + ", Money: " + gameState.getMoney());
                // Optionally, add a sound effect or visual cue for "can't afford"
                return true; // Click was on the button, but action was blocked
            }
        }
        return false; // Click was not on the button or button not active
    }
}