import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class App extends JPanel {
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        Random random = new Random();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int offset = 30;
        int offsetx = offset;
        int offsety = offset;
        for (int c = 0; c < 11; c++) {
            g2d.drawLine(offsetx, offsety, offsetx, offsety + 320);
            offsetx += 32;
        }
        offsetx = offset;
        for (int r = 0; r < 11; r++) {
            g2d.drawLine(offsetx, offsety, offsetx + 320, offsety);
            offsety += 32;
        }
        offsetx = 40;
        for (int i = 0; i < 10; i++) {
            int randColor = random.nextInt(6);
            switch (randColor) {
                case 0:
                    g.setColor(Color.RED);
                    break;
                case 1:
                    g.setColor(Color.ORANGE);
                    break;
                case 2:
                    g.setColor(Color.YELLOW);
                    break;
                case 3:
                    g.setColor(Color.GREEN);
                    break;
                case 4:
                    g.setColor(Color.BLUE);
                    break;
                case 5:
                    g.setColor(Color.BLACK);
                    break;
            }
            int height = random.nextInt(310) + 10;
            g2d.fillRect(offsetx, 350 - height, 15, height);
            offsetx += 32;
        }
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("Random Rectangles");

        JButton button = new JButton("Redraw");
        frame.add(button, BorderLayout.SOUTH);

        button.addActionListener(e -> {
            frame.repaint();
        });

        frame.add(new App());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setVisible(true);
    }
}
