import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class App extends JPanel {
    // Level Info
    private final boolean[] goalRing = new boolean[12];
    private final LoxNode[][] rings = new LoxNode[3][12];
    private String currentLevel = "Win Test|Efrice|0000101101|11|4|110000001101|00";
    // "Win Test|Efrice|0000101101|11|4|110000001101|00"
    // "Level 1|Efrice|110000101000|000010004400|000000010140|144444444100|00"
    private int cLevelInd = 0;
    private boolean levelLoaded = false;

    // Level Win Check Info
    private int tunnelCount = 0;
    private boolean megaLaser = false;
    private boolean portalPuzzle = false;
    private boolean hasWon = false;
    private int winTimerMax = 60;  //60frames ~ 1 sec
    private int winTimer = 0;

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

    // Sprite Info
    private final static String sprPath = "Sprite pngs";
    private static BufferedImage laserSprite;
    private static BufferedImage blockerSprite;
    private static BufferedImage goalSprite;

    private static final double LASER_SPRITE_FORWARD_ANGLE = Math.PI / 2;

    public App() {
        
        hasWon = false;
        cLevelInd = 0;
        setupLevel();

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

    private void setupLevel() {
        LevelLoader levelLoader = new LevelLoader();
        if (!levelLoaded) {
            if ((currentLevel == null || currentLevel.isEmpty()) && levelLoader.LoadLevel(cLevelInd)) {
                currentLevel = levelLoader.GetCurrentLevel();
            }
            String[] levelParts = currentLevel.split("\\|");

            for (int i = 0; i < 12; i++) {
                if (i < levelParts[2].length() && levelParts[2].charAt(i) == '1') {
                    goalRing[i] = true;
                } else
                    goalRing[i] = false;
            }
            for (int ring = 0; ring < 3; ring++) {
                for (int i = 0; i < 12; i++) {
                    if (i >= levelParts[ring + 3].length() || levelParts[ring + 3].charAt(i) == '0') {
                        rings[ring][i] = null;
                    } else {
                        char nodeType = levelParts[ring + 3].charAt(i);
                        switch (nodeType) {
                            // Regular Laser
                            case '1':
                                rings[ring][i] = new LaserBasic(ring + 1, true, i, new int[] { 0, 0 }, 1);
                                break;
                            // Double-Sided Laser
                            case '2':
                                rings[ring][i] = new LaserBasic(ring + 1, true, i, new int[] { 0, 0 }, 2);
                                break;
                            // Mega Laser
                            case '3':
                                rings[ring][i] = new LaserBasic(ring + 1, true, i, new int[] { 0, 0 }, 3);
                                megaLaser = true;
                                break;
                            // Regular Blocker
                            case '4':
                                rings[ring][i] = new BlockerBasic(ring + 1, true, i, new int[] { 0, 0 }, 4);
                                break;
                            // Tunnel
                            case '5':
                                rings[ring][i] = new BlockerBasic(ring + 1, true, i, new int[] { 0, 0 }, 5);
                                tunnelCount++;
                                break;
                            // Portal
                            case '6':
                                rings[ring][i] = new BlockerBasic(ring + 1, true, i, new int[] { 0, 0 }, 6);
                                portalPuzzle = true;
                                break;
                            // Unknown Node means just put nothing
                            default:
                                System.out.println("Unknown node type: " + nodeType);
                                rings[ring][i] = null;
                                break;
                        }
                    }
                }
            }
            levelLoaded = true;
        }
    }

    private void updateGame() {
        if (!hasWon){
            doLevelInteract();
        }
        else{
            winTimer++;
            System.out.println("Win Timer: " + winTimer);
            if (winTimer >= winTimerMax){
                winTimer = 0;
                cLevelInd++;
                currentLevel = "";
                levelLoaded = false;
                setupLevel();
                hasWon = false;
            }
        }
    }

    private void doLevelInteract(){
        if (downPressed) {
            System.out.println("Moving down a ring: " + selected_ring);
            selected_ring = (selected_ring - 1 + 3) % 3;
        }

        if (upPressed) {
            System.out.println("Moving up a ring: " + selected_ring);
            selected_ring = (selected_ring + 1) % 3;
        }

        // For up or down press, shift all the lasers in the selected ring clockwise or
        // counterclockwise
        if (leftPressed) {
            System.out.println("Shifting ring " + selected_ring + " counterclockwise");
            LoxNode first = rings[selected_ring][0];
            for (int i = 0; i < 11; i++)
                rings[selected_ring][i] = rings[selected_ring][i + 1];
            rings[selected_ring][11] = first;
        }

        if (rightPressed) {
            System.out.println("Shifting ring " + selected_ring + " clockwise");
            LoxNode last = rings[selected_ring][11];
            for (int i = 11; i > 0; i--)
                rings[selected_ring][i] = rings[selected_ring][i - 1];
            rings[selected_ring][0] = last;
        }

        if (leftPressed || rightPressed || upPressed || downPressed) {
            if (DoGoalCheck()) {
                System.out.println("Level Complete!");
                hasWon = true;
            }
        }

        // Clear pressed state at end of frame
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        // Setting BG Color
        g2d.setColor(new Color(61, 88, 179));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int centerX = getWidth() / 2; // Horizontal center of window
        int centerY = getHeight() / 2; // Vertical center of window
        int ring_spcr = 40; // distance between rings
        int offset = 32; // literal sprite offset
        for (int i = 0; i < 12; i++) {
            if (goalRing[i] == true) {
                // g2d.drawOval((int) Math.round((centerX + offset * 5 * Math.cos(i * Math.PI /
                // 6))),
                // (int) Math.round((centerY + offset * 5 * Math.sin(i * Math.PI / 6))), 10,
                // 10);
                int ring_mult = 4; // Increases distance from other rings
                g2d.drawImage(goalSprite,
                        (int) Math.round((centerX + ring_spcr * ring_mult * Math.cos(i * Math.PI / 6)) - offset / 2),
                        (int) Math.round((centerY + ring_spcr * ring_mult * Math.sin(i * Math.PI / 6)) - offset / 2),
                        32, 32, null);
            }
        }
        for (int ring = 0; ring < 3; ring++) {
            for (int i = 0; i < 12; i++) {
                if (rings[ring][i] != null && rings[ring][i].GetType() == 1) {
                    /*
                     * g2d.setColor(Color.RED);
                     * g2d.fillRect((int) Math.round((centerX + offset * (ring + 1) * Math.cos(i *
                     * Math.PI / 6))),
                     * (int) Math.round((centerY + offset * (ring + 1) * Math.sin(i * Math.PI /
                     * 6))), 5, 15);
                     */
                    int type = rings[ring][i].GetType();

                    // Position of the laser
                    double angle = i * Math.PI / 6.0;

                    double laserX = centerX + ring_spcr * (ring + 1) * Math.cos(angle);
                    double laserY = centerY + ring_spcr * (ring + 1) * Math.sin(angle);

                    // Determine which goal direction to face
                    int targetSlot = (i + 6) % 12;

                    if (type == 2 && goalRing[i]) {
                        targetSlot = i;
                    }

                    double targetAngle = targetSlot * Math.PI / 6.0;

                    double targetX = centerX + ring_spcr * 4 * Math.cos(targetAngle);
                    double targetY = centerY + ring_spcr * 4 * Math.sin(targetAngle);

                    // Calculate rotation toward goal
                    double rotation = Math.atan2(
                            targetY - laserY,
                            targetX - laserX);

                    // Draw rotated laser
                    Graphics2D rotated = (Graphics2D) g2d.create();

                    rotated.translate(laserX, laserY);
                    rotated.rotate(rotation - LASER_SPRITE_FORWARD_ANGLE);

                    rotated.drawImage(laserSprite, -16, -16, 32, 32, null);

                    rotated.dispose();

                } else if (rings[ring][i] != null && rings[ring][i].GetType() == 4) {
                    /*
                     * g2d.setColor(Color.BLACK);
                     * g2d.fillRect((int) Math.round((centerX + offset * (ring + 1) * Math.cos(i *
                     * Math.PI / 6))),
                     * (int) Math.round((centerY + offset * (ring + 1) * Math.sin(i * Math.PI /
                     * 6))), 10, 10);
                     */
                    g2d.drawImage(blockerSprite,
                            (int) Math
                                    .round((centerX + ring_spcr * (ring + 1) * Math.cos(i * Math.PI / 6)) - offset / 2),
                            (int) Math
                                    .round((centerY + ring_spcr * (ring + 1) * Math.sin(i * Math.PI / 6)) - offset / 2),
                            32, 32, null);
                }
            }
        }

        // Make it so it draws a circle around the selected ring
        g2d.setColor(Color.GREEN);
        int radius = ring_spcr * (selected_ring + 1);
        int diameter = radius * 2;
        g2d.drawOval(centerX - radius, centerY - radius, diameter, diameter);
    }

    // Returns true if there is a laser node facing a goal point with nothing
    // blocking it
    private boolean DoGoalCheck() {
        boolean hitGoal = false;
        boolean canMega = megaLaser; // If a mega laser exists, we can ignore 1 blocking node
        boolean needMega = false; // If a blocking node was found, we need to find a mega laser
                                  // All lasers are blocking nodes so any other laser being found
                                  // first immediately ends the check as a fail
        // Loop through the Goal Ring
        for (int g = 0; g < goalRing.length; g++) {
            if (goalRing[g]) {
                hitGoal = false;
                boolean hasLaser = false;
                for (int r = rings.length - 1; r > -1; r--) {
                    if (rings[r][g] != null && rings[r][g].GetType() > 0) {
                        int tmp = rings[r][g].GetType();
                        // Double-sided laser with nothing blocking it
                        if (!needMega && tmp == 2) {
                            System.out.println("Double-Sided Laser found at goal " + g + ", goal is hit.");
                            hasLaser = true;
                            hitGoal = true;
                            break;
                        }
                        // First blocking node found
                        else if (canMega && tmp > 0 && tmp != 5) {
                            canMega = false;
                            needMega = true;
                        }
                        // Additional blocking node found, goal is blocked
                        else if (!canMega && tmp > 0 && tmp != 5) {
                            System.out.println("Too many obstacles at goal " + g + ", goal is blocked.");
                            needMega = false;
                            return false;
                        }
                    }
                }
                if (g < goalRing.length && !hasLaser) {
                    for (int r = 0; r < rings.length; r++) {
                        // Checking nodes on the opposite side of the same ring as the goal.
                        int offset = g + 6;
                        if (offset > 11)
                            offset -= 12;
                        if (rings[r][offset] != null && rings[r][offset].GetType() > 0) {
                            int tmp = rings[r][offset].GetType();
                            if (!hasLaser && tmp < 4) {
                                // Laser found with nothing blocking it
                                if (!needMega) {
                                    System.out.println("Laser found at goal " + g + ", goal is hit.");
                                    hasLaser = true;
                                    hitGoal = true;
                                    break;
                                }
                                // There is one blocking node between this mega laser and the goal
                                else if (tmp == 3) {
                                    System.out.println("Mega Laser found at goal " + g + ", goal is hit.");
                                    hasLaser = true;
                                    needMega = false;
                                    hitGoal = true;
                                    break;
                                }
                                // there is one blocking node between this laser and the goal
                                else {
                                    System.out.println("Too many obstacles at goal " + g + ", goal is blocked.");
                                    needMega = false;
                                    return false;
                                }
                            }
                            // The first blocking node was hit. We now need the laser to be a mega laser
                            else if (canMega && tmp > 0 && tmp != 5) {
                                canMega = false;
                            }
                            // Another blocking node was found, goal is blocked
                            else if (!canMega && tmp > 0 && tmp != 5) {
                                System.out.println("Too many obstacles at goal " + g + ", goal is blocked.");
                                needMega = false;
                                return false;
                            }
                        }
                    }
                }
                if (g < goalRing.length && !hitGoal) {
                    System.out.println("No laser found at goal " + g + ".");
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        goalSprite = ImageIO.read(new File(sprPath + "/Goal.png"));
        laserSprite = ImageIO.read(new File(sprPath + "/lasperPointer.png"));
        blockerSprite = ImageIO.read(new File(sprPath + "/Blocker.png"));

        JFrame frame = new JFrame("LazerLox");

        frame.add(new App());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setVisible(true);
    }
}
