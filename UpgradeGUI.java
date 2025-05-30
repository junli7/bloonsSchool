import java.awt.*;

public class UpgradeGUI {
    // --- Action constants for handleClick return value ---
    public static final int ACTION_NONE = 0;
    public static final int ACTION_UPGRADED_OR_ARCHETYPE_CHOSEN = 1;
    public static final int ACTION_SOLD = 2;

    private Rectangle upgradeButtonBounds;
    private Rectangle archetype1ButtonBounds;
    private Rectangle archetype2ButtonBounds;
    private Rectangle sellButtonBounds;

    private static final int BUTTON_WIDTH = 140;
    private static final int ARCHETYPE_BUTTON_WIDTH = 140;
    private static final int SELL_BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 30;
    private static final int ARCHETYPE_BUTTON_HEIGHT = 35;
    private static final int SELL_BUTTON_HEIGHT = 28;
    private static final int PADDING = 5;
    private static final int SECTION_SPACING = 10; 
    private static final int TOOLTIP_SPACING_AFTER_SELL = 15; // Space between sell button and tooltip

    private Color defaultButtonColor = new Color(0, 100, 0);
    private Color hoverButtonColor = new Color(0, 150, 0);
    private Color disabledButtonColor = new Color(120, 120, 120);
    private Color disabledHoverButtonColor = new Color(150, 150, 150);
    
    private Color defaultSellButtonColor = new Color(200, 80, 0); 
    private Color hoverSellButtonColor = new Color(230, 100, 20);
    
    private Color currentButtonColor; 
    private Color currentArchetype1Color;
    private Color currentArchetype2Color;
    private Color currentSellButtonColor; 

    private Color buttonBorderColor = new Color(50, 50, 50);
    private Color buttonTextColor = Color.WHITE;
    private Font buttonFont;
    private Font headerFont;
    private Font statsFont;
    private Font costFont;
    private Font tooltipFont;
    private Font sellButtonFont;

    private String currentTooltipText = "";

    public UpgradeGUI() {
        this.upgradeButtonBounds = new Rectangle();
        this.archetype1ButtonBounds = new Rectangle();
        this.archetype2ButtonBounds = new Rectangle();
        this.sellButtonBounds = new Rectangle(); 
        
        this.currentButtonColor = defaultButtonColor;
        this.currentArchetype1Color = defaultButtonColor;
        this.currentArchetype2Color = defaultButtonColor;
        this.currentSellButtonColor = defaultSellButtonColor;
        
        this.buttonFont = new Font("SansSerif", Font.BOLD, 11);
        this.headerFont = new Font("SansSerif", Font.BOLD, 14);
        this.statsFont = new Font("SansSerif", Font.PLAIN, 12);
        this.costFont = new Font("SansSerif", Font.BOLD, 12);
        this.tooltipFont = new Font("SansSerif", Font.ITALIC, 11);
        this.sellButtonFont = new Font("SansSerif", Font.BOLD, 12);
    }
    
    public Color getCurrentButtonColor() { return currentButtonColor; }
    public Color getCurrentArchetype1Color() { return currentArchetype1Color; }
    public Color getCurrentArchetype2Color() { return currentArchetype2Color; }
    public Color getCurrentSellButtonColor() { return currentSellButtonColor; }


    public void updateHover(int mouseX, int mouseY, Monkey selectedMonkey, GameState gameState) {
        currentTooltipText = ""; 
        if (selectedMonkey == null) {
            currentButtonColor = defaultButtonColor;
            currentArchetype1Color = defaultButtonColor;
            currentArchetype2Color = defaultButtonColor;
            currentSellButtonColor = defaultSellButtonColor;
            return;
        }

        boolean canAffordCurrentUpgrade = gameState.canAfford(selectedMonkey.getUpgradeCost());
        boolean hoverOnAnyButton = false; // Flag to manage tooltip priority

        // Sell button hover (highest priority for tooltip if hovered)
        if (sellButtonBounds.contains(mouseX, mouseY)) {
            currentSellButtonColor = hoverSellButtonColor;
            currentTooltipText = "Sell tower for $" + selectedMonkey.getSellValue();
            hoverOnAnyButton = true;
        } else {
            currentSellButtonColor = defaultSellButtonColor;
        }

        if (selectedMonkey.getLevel() == 1 && !selectedMonkey.hasChosenArchetype()) {
            String[] archetypes = selectedMonkey.getArchetypeChoices();
            if (archetypes.length > 0) {
                if (archetype1ButtonBounds.contains(mouseX, mouseY)) {
                    currentArchetype1Color = canAffordCurrentUpgrade ? hoverButtonColor : disabledHoverButtonColor;
                    if (!hoverOnAnyButton) currentTooltipText = selectedMonkey.getArchetypeDescription(archetypes[0]);
                    hoverOnAnyButton = true;
                } else {
                    currentArchetype1Color = canAffordCurrentUpgrade ? defaultButtonColor : disabledButtonColor;
                }
            }
            if (archetypes.length > 1) {
                if (archetype2ButtonBounds.contains(mouseX, mouseY)) {
                    currentArchetype2Color = canAffordCurrentUpgrade ? hoverButtonColor : disabledHoverButtonColor;
                    if (!hoverOnAnyButton) currentTooltipText = selectedMonkey.getArchetypeDescription(archetypes[1]);
                    hoverOnAnyButton = true;
                } else {
                    currentArchetype2Color = canAffordCurrentUpgrade ? defaultButtonColor : disabledButtonColor;
                }
            }
            currentButtonColor = defaultButtonColor; 
        } else if (selectedMonkey.getLevel() < 10) {
            if (upgradeButtonBounds.contains(mouseX, mouseY)) {
                 currentButtonColor = canAffordCurrentUpgrade ? hoverButtonColor : disabledHoverButtonColor;
                 if (!hoverOnAnyButton) currentTooltipText = "Standard improvements to stats.";
                 hoverOnAnyButton = true;
            } else {
                currentButtonColor = canAffordCurrentUpgrade ? defaultButtonColor : disabledButtonColor;
            }
            currentArchetype1Color = defaultButtonColor;
            currentArchetype2Color = defaultButtonColor;
        } else { 
            currentButtonColor = disabledButtonColor;
            currentArchetype1Color = disabledButtonColor;
            currentArchetype2Color = disabledButtonColor;
        }
        
        // If no button is hovered that provides a tooltip, clear it
        if (!hoverOnAnyButton) {
            currentTooltipText = "";
        }
    }

    public void draw(Graphics2D g2d, Monkey selectedMonkey, GameState gameState, int panelWidth, int panelHeight) {
        if (selectedMonkey == null) return; 

        Font originalFont = g2d.getFont(); 
        
        int costForThisUpgrade = selectedMonkey.getUpgradeCost();
        boolean canAffordUpgrade = gameState.canAfford(costForThisUpgrade);

        int currentY = PADDING * 2; 
        int centerX = panelWidth / 2;

        // --- Header ---
        g2d.setColor(Color.BLACK);
        g2d.setFont(this.headerFont);
        FontMetrics headerFm = g2d.getFontMetrics();
        String monkeyNameDisplay = selectedMonkey.getClass().getSimpleName();
        if (monkeyNameDisplay.equals("Monkey")) monkeyNameDisplay = "Brr Brr Patapim";
        else if (monkeyNameDisplay.equals("MonkeyB")) monkeyNameDisplay = "Mr. Sahur";
        else if (monkeyNameDisplay.equals("MonkeyC")) monkeyNameDisplay = "Lirili Larila";
        
        String headerText = monkeyNameDisplay + " - L" + selectedMonkey.getLevel();
        if(selectedMonkey.hasChosenArchetype()){
            headerText += " (" + selectedMonkey.tộcViếtTắt(selectedMonkey.getChosenArchetype()) + ")";
        }
        int headerWidth = headerFm.stringWidth(headerText);
        g2d.drawString(headerText, centerX - headerWidth / 2, currentY + headerFm.getAscent());
        currentY += headerFm.getHeight() + headerFm.getDescent() + PADDING * 2;
        
        // --- Upgrade/Archetype Buttons & Cost ---
        if (selectedMonkey.getLevel() == 1 && !selectedMonkey.hasChosenArchetype()) {
            String[] archetypeKeys = selectedMonkey.getArchetypeChoices();
            if (archetypeKeys.length > 0) {
                archetype1ButtonBounds.setBounds(centerX - ARCHETYPE_BUTTON_WIDTH / 2, currentY, ARCHETYPE_BUTTON_WIDTH, ARCHETYPE_BUTTON_HEIGHT);
                drawSingleButton(g2d, selectedMonkey.archetypeKeyToString(archetypeKeys[0]), this.buttonFont, archetype1ButtonBounds, currentArchetype1Color, canAffordUpgrade);
                currentY += ARCHETYPE_BUTTON_HEIGHT + PADDING;
            }
            if (archetypeKeys.length > 1) {
                archetype2ButtonBounds.setBounds(centerX - ARCHETYPE_BUTTON_WIDTH / 2, currentY, ARCHETYPE_BUTTON_WIDTH, ARCHETYPE_BUTTON_HEIGHT);
                drawSingleButton(g2d, selectedMonkey.archetypeKeyToString(archetypeKeys[1]), this.buttonFont, archetype2ButtonBounds, currentArchetype2Color, canAffordUpgrade);
                currentY += ARCHETYPE_BUTTON_HEIGHT + PADDING;
            }

            g2d.setFont(this.costFont);
            String costText = "Cost: " + costForThisUpgrade;
            FontMetrics costFm = g2d.getFontMetrics();
            int costTextWidth = costFm.stringWidth(costText);
            g2d.setColor(canAffordUpgrade ? new Color(204,153,0) : Color.RED); 
            g2d.drawString(costText, centerX - costTextWidth / 2, currentY + costFm.getAscent());
            currentY += costFm.getHeight() + costFm.getDescent() + PADDING;

        } else if (selectedMonkey.getLevel() < 10) { 
            upgradeButtonBounds.setBounds(centerX - BUTTON_WIDTH / 2, currentY, BUTTON_WIDTH, BUTTON_HEIGHT);
            drawSingleButton(g2d, "Upgrade (L" + (selectedMonkey.getLevel() + 1) + ")", this.buttonFont, upgradeButtonBounds, currentButtonColor, canAffordUpgrade);
            currentY += BUTTON_HEIGHT + PADDING;
            
            g2d.setFont(this.costFont);
            String costText = "Cost: " + costForThisUpgrade;
            FontMetrics costFm = g2d.getFontMetrics();
            int costTextWidth = costFm.stringWidth(costText);
            g2d.setColor(canAffordUpgrade ? new Color(204,153,0) : Color.RED);
            g2d.drawString(costText, centerX - costTextWidth / 2, currentY + costFm.getAscent());
            currentY += costFm.getHeight() + costFm.getDescent() + PADDING;
        } else { 
            upgradeButtonBounds.setBounds(0,0,0,0); 
            archetype1ButtonBounds.setBounds(0,0,0,0);
            archetype2ButtonBounds.setBounds(0,0,0,0);

            g2d.setFont(this.headerFont);
            String maxLevelText = "Max Level Reached";
            FontMetrics maxFm = g2d.getFontMetrics();
            int textWidth = maxFm.stringWidth(maxLevelText);
            g2d.setColor(new Color(0, 80, 0)); 
            g2d.drawString(maxLevelText, centerX - textWidth/2, currentY + maxFm.getAscent());
            currentY += maxFm.getHeight() + maxFm.getDescent() + PADDING;
        }
        
        // --- Stats Display ---
        currentY += SECTION_SPACING; 
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(this.statsFont);
        FontMetrics statsFm = g2d.getFontMetrics();
        int statsLeftMargin = PADDING * 3;

        g2d.drawString("Damage: " + selectedMonkey.projectileDamage, statsLeftMargin, currentY + statsFm.getAscent());
        currentY += statsFm.getHeight() + PADDING / 2;
        g2d.drawString("Range: " + String.format("%.1f", selectedMonkey.getRange()), statsLeftMargin, currentY + statsFm.getAscent());
        currentY += statsFm.getHeight() + PADDING / 2;
        g2d.drawString("Cooldown: " + String.format("%.2f", selectedMonkey.shootCooldown / 1000.0) + "s", statsLeftMargin, currentY + statsFm.getAscent());
        currentY += statsFm.getHeight() + PADDING / 2;
        if(selectedMonkey.projectileIsExplosive){
            g2d.drawString("AoE Radius: " + String.format("%.1f",selectedMonkey.projectileAoeRadius), statsLeftMargin, currentY + statsFm.getAscent());
            currentY += statsFm.getHeight() + PADDING / 2;
        }
        if(selectedMonkey instanceof MonkeyC){ 
            g2d.drawString("Slow Dur: " + String.format("%.2f",((MonkeyC)selectedMonkey).slowDurationMillis/1000.0) + "s", statsLeftMargin, currentY + statsFm.getAscent());
            currentY += statsFm.getHeight() + PADDING / 2;
        }
        g2d.drawString("Camo Detect: " + (selectedMonkey.canDetectCamo() ? "Yes" : "No"), statsLeftMargin, currentY + statsFm.getAscent());
        currentY += statsFm.getHeight() + SECTION_SPACING; 

        // --- Sell Button ---
        sellButtonBounds.setBounds(centerX - SELL_BUTTON_WIDTH / 2, currentY, SELL_BUTTON_WIDTH, SELL_BUTTON_HEIGHT);
        drawSingleButton(g2d, "Sell ($" + selectedMonkey.getSellValue() + ")", this.sellButtonFont, sellButtonBounds, currentSellButtonColor, true); 
        currentY += SELL_BUTTON_HEIGHT + TOOLTIP_SPACING_AFTER_SELL; // Add space for tooltip after sell button

        // --- Tooltip Display Area (MOVED TO BELOW SELL BUTTON) ---
        if (!currentTooltipText.isEmpty()) {
            g2d.setFont(this.tooltipFont);
            FontMetrics tooltipFm = g2d.getFontMetrics();
            g2d.setColor(new Color(40,40,40)); 
            
            int tooltipStartX = PADDING * 3; 
            int tooltipContentWidth = panelWidth - (PADDING * 6); 
            
            String[] words = currentTooltipText.split(" ");
            StringBuilder line = new StringBuilder();
            int linesDrawn = 0;
            for (String word : words) {
                if (tooltipFm.stringWidth(line.toString() + word) > tooltipContentWidth && line.length() > 0) {
                    g2d.drawString(line.toString().trim(), tooltipStartX, currentY);
                    currentY += tooltipFm.getHeight();
                    line = new StringBuilder(word + " ");
                    linesDrawn++;
                    if (linesDrawn >= 2 && currentTooltipText.length() > 40) break; 
                } else {
                    line.append(word).append(" ");
                }
            }
            if (line.length() > 0) { 
                g2d.drawString(line.toString().trim(), tooltipStartX, currentY);
                // currentY += tooltipFm.getHeight(); // No need to increment Y further for this section if it's the last
            }
        }
        // currentY += SECTION_SPACING; // No need for spacing after tooltip if it's the last element

        g2d.setFont(originalFont); 
    }

    private void drawSingleButton(Graphics2D g2d, String text, Font fontToUse, Rectangle bounds, Color bgColor, boolean enabled) {
        g2d.setFont(fontToUse); 
        g2d.setColor(enabled ? bgColor : disabledButtonColor);
        g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        g2d.setColor(buttonBorderColor);
        g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        g2d.setColor(buttonTextColor);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = bounds.x + (bounds.width - textWidth) / 2;
        int textY = bounds.y + fm.getAscent() + (bounds.height - fm.getHeight()) / 2;
        g2d.drawString(text, textX, textY);
    }

    public int handleClick(int clickX, int clickY, Monkey selectedMonkey, GameState gameState) {
        if (selectedMonkey == null) return ACTION_NONE;

        if (sellButtonBounds.contains(clickX, clickY)) {
            return ACTION_SOLD;
        }

        int costForThisUpgrade = selectedMonkey.getUpgradeCost();

        if (selectedMonkey.getLevel() == 1 && !selectedMonkey.hasChosenArchetype()) {
            String[] archetypeKeys = selectedMonkey.getArchetypeChoices();
            if (archetypeKeys.length > 0 && archetype1ButtonBounds.contains(clickX, clickY)) {
                if (gameState.canAfford(costForThisUpgrade)) {
                    selectedMonkey.selectArchetypeAndUpgrade(archetypeKeys[0], gameState);
                    return ACTION_UPGRADED_OR_ARCHETYPE_CHOSEN;
                } else { System.out.println("Cannot afford " + selectedMonkey.archetypeKeyToString(archetypeKeys[0])); }
                return ACTION_NONE; 
            }
            if (archetypeKeys.length > 1 && archetype2ButtonBounds.contains(clickX, clickY)) {
                 if (gameState.canAfford(costForThisUpgrade)) {
                    selectedMonkey.selectArchetypeAndUpgrade(archetypeKeys[1], gameState);
                    return ACTION_UPGRADED_OR_ARCHETYPE_CHOSEN;
                } else { System.out.println("Cannot afford " + selectedMonkey.archetypeKeyToString(archetypeKeys[1])); }
                return ACTION_NONE; 
            }
        } else if (selectedMonkey.getLevel() < 10) { 
            if (upgradeButtonBounds.contains(clickX, clickY)) {
                if (gameState.canAfford(costForThisUpgrade)) {
                    gameState.spendMoney(costForThisUpgrade); 
                    selectedMonkey.upgrade(); 
                    return ACTION_UPGRADED_OR_ARCHETYPE_CHOSEN;
                } else {
                    System.out.println("Cannot afford upgrade. Cost: " + costForThisUpgrade + ", Money: " + gameState.getMoney());
                }
                return ACTION_NONE; 
            }
        }
        return ACTION_NONE; 
    }
}