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
            if (i > 5)
                goalRing[i] = true;
            else
                goalRing[i] = false;
        }
        for (int ring = 0; ring < 3; ring++) {
            for (int i = 0; i < 12; i++) {
                if (ring == 1 && i < 6)
                    rings[ring][i] = 1;
                else
                    rings[ring][i] = 0;
            }
        }
        rings[0][0] = 1;
        rings[1][0] = 1;
        rings[2][0] = 1;

        setupInput();

        // Runs update + repaint approximately 60 times per second
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

        // LEFT pressed
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

        // LEFT released
        inputMap.put(
                KeyStroke.getKeyStroke("released LEFT"),
                "leftReleased");

        actionMap.put("leftReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leftHeld = false;
            }
        });

        // RIGHT pressed
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

        // RIGHT released
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
            selected_ring = (selected_ring - 1 + 3) % 3;
        }

        if (rightPressed) {
            System.out.println("Moving up a ring: " + selected_ring);
            selected_ring = (selected_ring + 1) % 3;
        }

        // Other game logic goes here
        // Clear "pressed" state at end of frame
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
        int centerY = 210;
        int offset = 40;
        for (int i = 0; i < 12; i++) {
            if (goalRing[i] == true) {
                g2d.drawOval((int) Math.round((centerX + offset * 5 * Math.cos(i * Math.PI / 6))),
                        (int) Math.round((centerY + offset * 5 * Math.sin(i * Math.PI / 6))), 10, 10);
            }
        }
        for (int ring = 0; ring < 3; ring++) {
            for (int i = 0; i < 12; i++) {
                if (rings[ring][i] == 1) {
                    g2d.setColor(Color.RED);
                    g2d.fillRect((int) Math.round((centerX + offset * (ring + 1) * Math.cos(i * Math.PI / 6))),
                            (int) Math.round((centerY + offset * (ring + 1) * Math.sin(i * Math.PI / 6))), 5, 15);
                } else if (rings[ring][i] == 4) {
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect((int) Math.round((centerX + offset * (ring + 1) * Math.cos(i * Math.PI / 6))),
                            (int) Math.round((centerY + offset * (ring + 1) * Math.sin(i * Math.PI / 6))), 10, 10);
                }
            }
        }

        // Make it so it draws a circle around the selected ring
        g2d.setColor(Color.GREEN);
        int radius = offset * (selected_ring + 1);
        int diameter = radius * 2;
        g2d.drawOval(centerX - radius, centerY - radius, diameter, diameter);
    }

    private boolean DoGoalCheck() {
        int tot_goals = goalRing.length;
        int goal_pts = 0;
        for (int g = 0; g < goalRing.length; g++) {
            if (goalRing[g]) {
                boolean has_laser = false;
                for (int r = rings.length - 1; r > -1; r--) {
                    if (g < 6 && rings[r][g + 6] > 0) {
                        int tmp = rings[r][g + 6];
                        if (tmp == 1) {
                            has_laser = true;
                        } else if (has_laser && tmp == 4) {
                            has_laser = false;
                            break;
                        }
                    }
                    if (g > 5 && rings[r][g - 6] > 0) {
                        int tmp = rings[r][g - 6];
                        if (tmp == 1) {
                            has_laser = true;
                        } else if (has_laser && tmp == 4) {
                            has_laser = false;
                            break;
                        }
                    }
                }
                if (has_laser) {
                    for (int r = rings.length - 1; r > -1; r--) {
                        if (rings[r][g] == 1 || rings[r][g] == 4) {
                            has_laser = false;
                            break;
                        }
                    }
                    if (has_laser)
                        goal_pts++;
                }
            } else {
                tot_goals--;
            }
        }
        return goal_pts == tot_goals;
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("LazerLox");

        frame.add(new App());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setVisible(true);

        App app = new App();
        System.out.println("Lasers Hit Goals? " + app.DoGoalCheck());
    }
}
