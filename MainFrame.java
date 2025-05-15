import javax.swing.*;

public class MainFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Monkey Shooter");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new GamePanel());
            frame.pack();
            frame.setLocationRelativeTo(null); 
            frame.setVisible(true);
        });
    }
}