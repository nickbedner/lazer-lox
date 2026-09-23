import javax.swing.*;
import java.awt.*;

class App extends JPanel {
    private final boolean[] goalRing = new boolean[12];
    private final int[][] rings = new int[3][12];

    public App() {
        for(int i = 0; i < 12; i++){
            goalRing[i] = true;
        }
        for(int ring = 0; ring < 3; ring++){
            for(int i = 0; i < 12; i++){
                rings[ring][i] = 4;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int centerX = 320;
        int centerY = 210;
        int offset = 40;
        for(int i = 0; i < 12; i++){
            if(goalRing[i] == true){
                g2d.drawOval((int) Math.round((centerX + offset * 5 * Math.cos(i * Math.PI / 6))), (int)Math.round((centerY + offset * 5 * Math.sin(i * Math.PI / 6))), 10, 10);
            }
        }
        for(int ring = 0; ring < 3; ring++){
            for(int i = 0; i < 12; i++){
                if(rings[ring][i] == 1){
                    g2d.setColor(Color.RED);
                   g2d.fillRect((int) Math.round((centerX + offset * (ring + 1) * Math.cos(i * Math.PI/6))), (int)Math.round((centerY + offset * (ring + 1) * Math.sin(i * Math.PI/6))), 5, 15); 
                }else if(rings[ring][i] == 4){
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect((int) Math.round((centerX + offset * (ring + 1) * Math.cos(i * Math.PI/6))), (int)Math.round((centerY + offset * (ring + 1) * Math.sin(i * Math.PI/6))), 10, 10);
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
