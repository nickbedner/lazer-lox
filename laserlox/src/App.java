import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class App extends JPanel {
    private final boolean[] goalRing = new boolean[12];
    private final int[][] rings = new int[3][12];

    // Input state
    private boolean leftPressed = false;
    private boolean leftHeld = false;
    private boolean rightPressed = false;
    private boolean rightHeld = false;

    private int selected_ring = 0;

    // 60 FPS game loop
    private static final int FPS = 60;
    private static final int FRAME_TIME = 1000 / FPS;

    public App() {
        for (int i = 0; i < 12; i++) {
            goalRing[i] = true;
        }

        setupInput();

        // Runs update and repaint approximately 60 times per second
        Timer gameLoop = new Timer(FRAME_TIME, e -> {
            updateGame();
            repaint();
        });

        gameLoop.start();
    }

    private void setupInput() {
        // Input from user
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Action to perform from input
        ActionMap actionMap = getActionMap();

        // Left pressed
        inputMap.put(
                KeyStroke.getKeyStroke("pressed LEFT"),
                "leftPressed");

        actionMap.put("leftPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!leftHeld) {
                    leftPressed = true;
                }

                leftHeld = true;
            }
        });

        // Left released
        inputMap.put(
                KeyStroke.getKeyStroke("released LEFT"),
                "leftReleased");

        actionMap.put("leftReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leftHeld = false;
            }
        });

        // Right pressed
        inputMap.put(
                KeyStroke.getKeyStroke("pressed RIGHT"),
                "rightPressed");

        actionMap.put("rightPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!rightHeld) {
                    rightPressed = true;
                }

                rightHeld = true;
            }
        });

        // Right released
        inputMap.put(
                KeyStroke.getKeyStroke("released RIGHT"),
                "rightReleased");

        actionMap.put("rightReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rightHeld = false;
            }
        });

        // ESCAPE closes the program
        inputMap.put(
                KeyStroke.getKeyStroke("pressed ESCAPE"),
                "exitGame");

        actionMap.put("exitGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void updateGame() {
        if (leftPressed) {
            System.out.println("Moving down a ring: " + selected_ring);
            selected_ring = (selected_ring - 1 + 4) % 4;
        }

        if (rightPressed) {
            System.out.println("Moving up a ring: " + selected_ring);
            selected_ring = (selected_ring + 1) % 4;
        }

        // Other game logic goes here
        // Clear pressed state at end of frame
        leftPressed = false;
        rightPressed = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int centerX = 320;
        int centerY = 240;
        int offset = 30;
        for (int i = 0; i < 12; i++) {
            if (goalRing[i] == true) {
                g2d.drawOval((int) Math.round((centerX + offset * 4 * Math.cos(i * Math.PI / 6))),
                        (int) Math.round((centerY + offset * 4 * Math.sin(i * Math.PI / 6))), 10, 10);
            }
        }
        for (int ring = 0; ring < 3; ring++) {
            for (int i = 0; i < 12; i++) {
                if (rings[ring][i] == 1) {
                    g2d.setColor(Color.RED);
                    g2d.fillRect(i * offset, (ring + 1) * offset, 5, 15);
                } else if (rings[ring][i] == 4) {
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(i * offset, (ring + 1) * offset, 10, 10);
                }
            }

            if (ring == selected_ring) {
                g2d.setColor(Color.GREEN);
                g2d.drawRect(0, (ring + 1) * offset, 12 * offset, 15);
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
