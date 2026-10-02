import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class App extends JPanel {
    private final boolean[] goalRing = new boolean[12];
    private final LoxNode[][] rings = new LoxNode[3][12];

    // Input state
    private boolean leftPressed = false;
    private boolean leftHeld = false;

    private boolean rightPressed = false;
    private boolean rightHeld = false;

    private boolean upPressed = false;
    private boolean upHeld = false;

    private boolean downPressed = false;
    private boolean downHeld = false;

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
                if (ring == 0 && i < 6)
                    rings[ring][i] = new LaserBasic(ring + 1, true, i, new int[]{0, 0}, 1);
                else if (ring == 1 && i < 9 && i > 5)
                    rings[ring][i] = new BlockerBasic(ring + 1, true, i, new int[]{0, 0}, 4);
                else if (ring == 2 && i > 8)
                    rings[ring][i] = new BlockerBasic(ring, true, i, new int[]{0, 0}, 4);
                else
                    rings[ring][i] = null;
            }
        }

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

        // UP pressed
        inputMap.put(
                KeyStroke.getKeyStroke("pressed UP"),
                "upPressed");

        actionMap.put("upPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!upHeld)
                    upPressed = true;

                upHeld = true;
            }
        });

        // UP released
        inputMap.put(
                KeyStroke.getKeyStroke("released UP"),
                "upReleased");

        actionMap.put("upReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upHeld = false;
            }
        });

        // DOWN pressed
        inputMap.put(
                KeyStroke.getKeyStroke("pressed DOWN"),
                "downPressed");

        actionMap.put("downPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!downHeld)
                    downPressed = true;

                downHeld = true;
            }
        });

        // DOWN released
        inputMap.put(
                KeyStroke.getKeyStroke("released DOWN"),
                "downReleased");

        actionMap.put("downReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downHeld = false;
            }
        });

        // LEFT pressed
        inputMap.put(
                KeyStroke.getKeyStroke("pressed LEFT"),
                "leftPressed");

        actionMap.put("leftPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!leftHeld)
                    leftPressed = true;

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

        // For up or down press, shift all the lasers in the selected ring clockwise or
        // counterclockwise
        if (upPressed) {
            System.out.println("Shifting ring " + selected_ring + " counterclockwise");
            LoxNode first = rings[selected_ring][0];
            for (int i = 0; i < 11; i++)
                rings[selected_ring][i] = rings[selected_ring][i + 1];
            rings[selected_ring][11] = first;
        }

        if (downPressed) {
            System.out.println("Shifting ring " + selected_ring + " clockwise");
            LoxNode last = rings[selected_ring][11];
            for (int i = 11; i > 0; i--)
                rings[selected_ring][i] = rings[selected_ring][i - 1];
            rings[selected_ring][0] = last;
        }

        // Clear pressed state at end of frame
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;

        //Check if Lasers meet goals
        System.out.println("Lasers Hit Goals? " + DoGoalCheck());
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
                if (rings[ring][i] != null && rings[ring][i].GetType() == 1) {
                    g2d.setColor(Color.RED);
                    g2d.fillRect((int) Math.round((centerX + offset * (ring + 1) * Math.cos(i * Math.PI / 6))),
                            (int) Math.round((centerY + offset * (ring + 1) * Math.sin(i * Math.PI / 6))), 5, 15);
                } else if (rings[ring][i] != null && rings[ring][i].GetType() == 4) {
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
    //Returns true if there is a laser node facing a goal point with nothing blocking it
    private boolean DoGoalCheck() {
        int tot_goals = goalRing.length;            //Effectively 12
        int goal_pts = 0;                           //How many goals in the puzzle
                                                    //Note that a puzzle cannot have more than 6 goals, nor more goals
                                                    //than lasers if it is to be possible.
        //Loop through the Goal Ring
        for (int g = 0; g < goalRing.length; g++) {
            //Find if a goal is on
            if (goalRing[g]) {
                //Assume there is no laser by default
                boolean has_laser = false;
                //Go through all three player rings
                for (int r = rings.length - 1; r > -1; r--) {
                    //If the goal is within the first 6 slots, we check the slot 6 spaces away on each
                    //player ring. This grabs the nodes facing the goal.
                    if (g < 6 && rings[r][g + 6] != null && rings[r][g + 6].GetType() > 0) {
                        int tmp = rings[r][g + 6].GetType();
                        //Check that a laser is on one of the player rings. 
                        if (tmp == 1) {
                            has_laser = true;
                        //Only state that there is no laser if a laser if a laser was found prior to a
                        //blocker. Note we work back to front since a laser blocking another laser on
                        //this side of the ring does not actually stop the goal from being hit
                        } else if (has_laser && tmp == 4) {
                            has_laser = false;
                            break;
                        }
                    }
                    //This is the same code as above, but we subtract 6 to get the opposite slot of the back of
                    //the goal ring.
                    if (g > 5 && rings[r][g - 6] != null && rings[r][g - 6].GetType() > 0) {
                        int tmp = rings[r][g - 6].GetType();
                        if (tmp == 1) {
                            has_laser = true;
                        } else if (has_laser && tmp == 4) {
                            has_laser = false;
                            break;
                        }
                    }
                }
                //If a laser was not blocked in the opposite side, now check in front of the goal.
                if (has_laser) {
                    //Go through each player ring in the slot directly in front of the goal
                    for (int r = rings.length - 1; r > -1; r--) {
                        //If there is anything in front of the goal, save a theoretical tunnel or portal,
                        //the goal is blocked. Lasers block other lasers on this side of the ring.
                        if (rings[r][g] != null && rings[r][g].GetType() > 0 &&  rings[r][g].GetType() < 5) {
                            has_laser = false;
                            break;
                        }
                    }
                    //If nothing was blocked, the goal is hit and we add 1 to the goal points. We need goal points
                    //to match with total goals to know if we won.
                    if (has_laser)
                        goal_pts++;
                }
            } else {
                //Everytime a goal is off, reduce the total goals by 1.
                tot_goals--;
            }
        }
        //If there are no goals, then the level is impossible.
        if (tot_goals == 0) return false;
        
        //If the goals hit matches the total number of goals, the level is won.
        return goal_pts == tot_goals;
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("LazerLox");
        
        frame.add(new App());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setVisible(true);
    }
}
