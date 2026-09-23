import javax.swing.*;
import java.awt.*;

class App extends JPanel {
    private final boolean[] goalRing = new boolean[12];
    private final int[][] rings = new int[4][12];

    public App() {
        goalRing[0] = false;
        goalRing[1] = true;
        rings[0][0] = 0;
        rings[0][1] = 1;
        rings[0][2] = 4;
        rings[1][0] = 1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int offsetx = 30;
        int offsety = 30;
        for(int i = 0; i < 12; i++){
            if(goalRing[i] == true){
                g2d.fillOval(i * offsetx, 0, 10, 10);
            }
        }
        for(int ring = 0; ring < 4; ring++){
            for(int i = 0; i < 12; i++){
                if(rings[ring][i] == 1){
                    g2d.setColor(Color.RED);
                   g2d.fillRect(i * offsetx, (ring + 1) * offsety, 5, 15); 
                }else if(rings[ring][i] == 4){
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(i * offsetx, (ring + 1) * offsety, 10, 10);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("LazerLox");

        frame.add(new App());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setVisible(true);
    }
}
