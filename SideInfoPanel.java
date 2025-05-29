
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SideInfoPanel extends JPanel {
    private GameState gameState;
    private JButton nextWaveButton;
    private Rectangle nextWaveButtonBounds; // For custom drawing if not using JButton

    private static final int PANEL_WIDTH = 200;

    public SideInfoPanel(GameState gameState) {
        this.gameState = gameState;
        setPreferredSize(new Dimension(PANEL_WIDTH, 0)); // Width is fixed, height will stretch
        setBackground(new Color(220, 220, 220)); // Light gray, slightly different from game panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical arrangement
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add some padding

        // --- Using standard Swing components for simplicity ---
        JLabel moneyLabel = new JLabel("Money: $0");
        moneyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(moneyLabel);

        JLabel bloonsKilledLabel = new JLabel("Bloons Killed: 0");
        bloonsKilledLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        add(bloonsKilledLabel);

        JLabel waveLabel = new JLabel("Wave: 1");
        waveLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        add(waveLabel);

        add(Box.createRigidArea(new Dimension(0, 20))); // Spacer

        nextWaveButton = new JButton("Next Wave");
        nextWaveButton.addActionListener(e -> {
            gameState.incrementWave();
            // Potentially trigger game logic to start the next wave here
            // For now, just updates the display via repaint
            this.getParent().repaint(); // Repaint the container (which includes this panel)
        });
        nextWaveButton.setFocusable(false); // Prevent button from stealing focus from GamePanel
        // Align button to the center (within its own cell in BoxLayout)
        nextWaveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(nextWaveButton);

        // Add a timer to update labels periodically (or you can trigger updates on events)
        Timer updateTimer = new Timer(100, e -> { // Update 10 times per second
            moneyLabel.setText("Money: $" + gameState.getMoney());
            bloonsKilledLabel.setText("Bloons Killed: " + gameState.getBloonsKilled());
            waveLabel.setText("Wave: " + gameState.getCurrentWave());
        });
        updateTimer.start();
    }

    // paintComponent is still useful if you want more custom drawing
    // than JLabels/JButtons provide, but for this, components are easier.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // If not using JLabels, you would draw your text here:
        // Graphics2D g2d = (Graphics2D) g;
        // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // g2d.setColor(Color.BLACK);
        // g2d.setFont(new Font("Arial", Font.BOLD, 16));
        // g2d.drawString("Money: $" + gameState.getMoney(), 10, 30);
        // g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        // g2d.drawString("Bloons Killed: " + gameState.getBloonsKilled(), 10, 60);
        // g2d.drawString("Wave: " + gameState.getCurrentWave(), 10, 90);
    }
}