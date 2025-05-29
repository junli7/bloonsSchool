import java.awt.*;

public class UpgradeGUI {
    private Rectangle upgradeButtonBounds;
    private static final int BUTTON_WIDTH = 130;
    private static final int BUTTON_HEIGHT = 30;
    private static final int PADDING = 5;

    // Define colors for the flat look
    private Color defaultButtonColor = new Color(0, 100, 0);     // Dark Green (as before)
    private Color hoverButtonColor = new Color(0, 150, 0);       // Brighter Green (as before)
    private Color disabledButtonColor = new Color(120, 120, 120); // Darker Gray for disabled
    private Color currentButtonColor; // Used for hover effect, now stores the actual color to draw

    private Color buttonBorderColor = new Color(50, 50, 50); // Darker border for a subtle definition
    private Color buttonTextColor = Color.WHITE;

    public UpgradeGUI() {
        this.upgradeButtonBounds = new Rectangle();
        this.currentButtonColor = defaultButtonColor; // Initialize
    }

    public void updateHover(int mouseX, int mouseY, Monkey selectedMonkey, GameState gameState) {
        if (selectedMonkey != null && selectedMonkey.isSelected() && upgradeButtonBounds.contains(mouseX, mouseY)) {
            if (gameState.canAfford(selectedMonkey.getUpgradeCost())) {
                currentButtonColor = hoverButtonColor;
            } else {
                // Hovering over a disabled button, could use a slightly different disabled hover color
                // For simplicity, we'll just use the standard disabled color,
                // or a slightly lighter version of it.
                currentButtonColor = new Color(150, 150, 150); // Lighter disabled hover
            }
        } else if (selectedMonkey != null && selectedMonkey.isSelected()){
             if (gameState.canAfford(selectedMonkey.getUpgradeCost())) {
                currentButtonColor = defaultButtonColor;
            } else {
                currentButtonColor = disabledButtonColor;
            }
        } else {
            // No monkey selected or mouse is not over the button area
            currentButtonColor = defaultButtonColor; // Reset to default (will be overridden by disabled if needed)
        }
    }


    public void draw(Graphics2D g2d, Monkey selectedMonkey, GameState gameState) {
        if (selectedMonkey == null || !selectedMonkey.isSelected()) {
            upgradeButtonBounds.setBounds(0,0,0,0);
            return;
        }

        int yOffset = (int) selectedMonkey.getHitbox() / 2 + g2d.getFontMetrics().getHeight() + PADDING * 3;
        int guiX = selectedMonkey.getX() - BUTTON_WIDTH / 2;
        int guiY = Math.max(0, selectedMonkey.getY() - yOffset - BUTTON_HEIGHT);

        upgradeButtonBounds.setBounds(guiX, guiY, BUTTON_WIDTH, BUTTON_HEIGHT);

        // Determine the final button color to draw based on state
        Color finalDrawColor;
        boolean canAfford = gameState.canAfford(selectedMonkey.getUpgradeCost());

        if (upgradeButtonBounds.contains(MouseInfo.getPointerInfo().getLocation().x - g2d.getClipBounds().x, // Adjust for panel location on screen if necessary
                                        MouseInfo.getPointerInfo().getLocation().y - g2d.getClipBounds().y)) { // Or pass mouseX, mouseY if available
            finalDrawColor = canAfford ? hoverButtonColor : new Color(150, 150, 150); // Hover color or disabled hover
        } else {
            finalDrawColor = canAfford ? defaultButtonColor : disabledButtonColor; // Default or disabled
        }


        // --- Draw Button Background (Flat Style) ---
        g2d.setColor(finalDrawColor);
        g2d.fillRect(upgradeButtonBounds.x, upgradeButtonBounds.y, upgradeButtonBounds.width, upgradeButtonBounds.height);

        // --- Draw Button Border (Optional, for definition) ---
        // You can disable this if you want a completely borderless flat look.
        g2d.setColor(buttonBorderColor); // Use a defined border color
        g2d.drawRect(upgradeButtonBounds.x, upgradeButtonBounds.y, upgradeButtonBounds.width, upgradeButtonBounds.height);
        // For no border, comment out the two lines above.

        // --- Draw Button Text ---
        g2d.setColor(buttonTextColor); // Use defined text color
        String text = "Upgrade (L" + (selectedMonkey.getLevel() + 1) + ")";
        FontMetrics fm = g2d.getFontMetrics(); // Use existing font metrics
        int textWidth = fm.stringWidth(text);
        // Center text
        int textX = upgradeButtonBounds.x + (upgradeButtonBounds.width - textWidth) / 2;
        int textY = upgradeButtonBounds.y + fm.getAscent() + (upgradeButtonBounds.height - fm.getHeight()) / 2;
        g2d.drawString(text, textX, textY);

        // --- Draw Cost Text ---
        String costText = "Cost: " + selectedMonkey.getUpgradeCost();
        // Use a slightly different font or color for cost if desired
        Font costFont = new Font("Arial", Font.BOLD, 12); // Re-declare or ensure it's available
        g2d.setFont(costFont);
        FontMetrics costFm = g2d.getFontMetrics();
        int costTextWidth = costFm.stringWidth(costText);
        g2d.setColor(Color.YELLOW); // Or another contrasting color
        g2d.drawString(costText, guiX + (BUTTON_WIDTH - costTextWidth) / 2, guiY + BUTTON_HEIGHT + costFm.getAscent() + PADDING);

        g2d.setFont(fm.getFont()); // Reset font to what it was before drawing cost text
    }

    public boolean handleClick(int clickX, int clickY, Monkey selectedMonkey, GameState gameState) {
        if (selectedMonkey != null && selectedMonkey.isSelected() && upgradeButtonBounds.contains(clickX, clickY)) {
            if (gameState.canAfford(selectedMonkey.getUpgradeCost())) {
                gameState.spendMoney(selectedMonkey.getUpgradeCost());
                selectedMonkey.upgrade();
                // No need to explicitly call updateHover here for color, as draw will recalculate
                return true;
            } else {
                System.out.println("Cannot afford upgrade for monkey at ("+selectedMonkey.getX()+","+selectedMonkey.getY()+"). Cost: " + selectedMonkey.getUpgradeCost() + ", Money: " + gameState.getMoney());
                return true;
            }
        }
        return false;
    }
}