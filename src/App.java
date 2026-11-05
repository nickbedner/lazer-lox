import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

class App extends JPanel {
    // Level Info
    private final boolean[] goalRing = new boolean[12];
    private final LoxNode[][] rings = new LoxNode[3][12];
    private String currentLevel = "Win Test|Efrice|0000101101|11|4|110000001101|00";
    // "Win Test|Efrice|0000101101|11|4|110000001101|00";
    // "Level 1|Efrice|110000101000|000010004400|000000010140|144444444100|00";
    // "Level 2 Test|Efrice|1010001001|000010001|1444441|41014|00";
    LevelLoader lvl_inst;

    private int cLevelInd = 0;

    //Win Variables
    private int winTimerMax = 60; // 60frames ~ 1 sec
    private int winTimer = 0;
    private boolean hasWon = false;

    // Input state
    private boolean spacePressed = false;
    private boolean spaceHeld = false;

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

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private boolean downloadingLevel = false;

    public App() {

        hasWon = false;
        cLevelInd = 0;
        lvl_inst = new LevelLoader(currentLevel);
        LoxNode[][] ndRings = lvl_inst.SetupNodes();
        for (int r = 0; r < 3; r++) {
            for (int i = 0; i < 12; i++) {
                rings[r][i] = ndRings[r][i];
            }
        }
        boolean[] gRing = lvl_inst.SetupGoals();
        for (int i = 0; i < 12; i++) {
            goalRing[i] = gRing[i];
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

        //SPACE pressed
        inputMap.put(
                KeyStroke.getKeyStroke("pressed SPACE"),
                "spacePressed");

        actionMap.put("spacePressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle space key press
                if (!spaceHeld)
                    spacePressed = true;

                spaceHeld = true;
            }
        });

        //SPACE released
        inputMap.put(
                KeyStroke.getKeyStroke("released SPACE"),
                "spaceReleased");

        actionMap.put("spaceReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle space key press
                spaceHeld = false;
            }
        });

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

        // L Download and load Level 1 from Azure
        inputMap.put(
                KeyStroke.getKeyStroke("pressed L"),
                "loadRemoteLevel");

        actionMap.put("loadRemoteLevel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLevelFromServer(1);
            }
        });
    }

    private void loadLevelFromServer(int levelId) {
        if (downloadingLevel)
            return;

        downloadingLevel = true;

        String url = "http://130.131.97.156/api/level.php?id=" + levelId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        System.out.println("Downloading level " + levelId);

        // Make the request asynchronously so the game doesn't freeze while waiting for
        // the server
        httpClient.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8))
                .whenComplete((response, error) -> {

                    SwingUtilities.invokeLater(() -> {

                        downloadingLevel = false;

                        if (error != null) {
                            System.err.println(
                                    "Download failed: " + error.getMessage());
                            return;
                        }

                        if (response.statusCode() != 200) {
                            System.err.println(
                                    "Server returned HTTP " + response.statusCode());
                            return;
                        }

                        String downloadedLevel = response.body().stripTrailing();
                        String[] parts = downloadedLevel.split("\\|", -1);
                        if (parts.length != 7 ||
                                parts[0].isBlank() || parts[0].length() > 32 ||
                                parts[1].isBlank() || parts[1].length() > 32 ||
                                !parts[2].matches("[01]{12}") ||
                                !parts[3].matches("[0-6]{12}") ||
                                !parts[4].matches("[0-6]{12}") ||
                                !parts[5].matches("[0-6]{12}") ||
                                !parts[6].matches("[01]{2}")) {

                            System.err.println("Invalid level data received.");
                            return;
                        }

                        System.out.println("Downloaded: " + downloadedLevel);

                        // Reset state from the previous level
                        hasWon = false;
                        winTimer = 0;
                        selected_ring = 0;

                        leftPressed = false;
                        rightPressed = false;
                        upPressed = false;
                        downPressed = false;

                        currentLevel = downloadedLevel;
                        lvl_inst = new LevelLoader(currentLevel);
                        LoxNode[][] ndRings = lvl_inst.SetupNodes();
                        for (int r = 0; r < 3; r++) {
                            for (int i = 0; i < 12; i++) {
                                rings[r][i] = ndRings[r][i];
                            }
                        }
                        boolean[] gRing = lvl_inst.SetupGoals();
                        for (int i = 0; i < 12; i++) {
                            goalRing[i] = gRing[i];
                        }

                        repaint();

                        System.out.println("Successfully loaded level " + levelId);
                    });
                });
    }

    private void updateGame() {
        if (!hasWon) {
            doLevelInteract();
        } else {
            winTimer++;
            System.out.println("Win Timer: " + winTimer);
            if (winTimer >= winTimerMax) {
                winTimer = 0;
                cLevelInd++;
                boolean findLvl = lvl_inst.LoadLevel(cLevelInd);;
                if (findLvl) {
                    currentLevel = lvl_inst.GetCurrentLevel(); 
                    LoxNode[][] ndRings = lvl_inst.SetupNodes();
                    for (int r = 0; r < 3; r++) {
                        for (int i = 0; i < 12; i++) {
                            rings[r][i] = ndRings[r][i];
                        }
                    }
                    boolean[] gRing = lvl_inst.SetupGoals();
                    for (int i = 0; i < 12; i++) {
                        goalRing[i] = gRing[i];
                    }
                } 
                hasWon = false;
            }
        }
    }

    private void doLevelInteract() {
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

        if (spacePressed) {
            if (lvl_inst.isTogglePuzzle()) {
                System.out.println("Toggling ring " + selected_ring);
                for (int i = 0; i < 12; i++) {
                    if (rings[selected_ring][i] != null){
                        rings[selected_ring][i].Toggle();
                    }
                }
            }
        }

        //Check if the puzzle is won after each move.
        if (leftPressed || rightPressed || upPressed || downPressed || spacePressed) {
            //Puzzle is assumed to always allow double lasers. checkWin then worries about whether a double laser exists.
            //Toggle and Carry are temp always false
            WinChecker wc = new WinChecker(lvl_inst.hasMegaLaser(), true, lvl_inst.getTunnelCount() > 0, lvl_inst.isPortalPuzzle(), false, false);
            if (wc.checkWin(goalRing, rings)) {
                System.out.println("Level Complete!");
                hasWon = true;
            }
        }

        // Clear pressed state at end of frame
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;
        spacePressed = false;
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

                    //When a ring is turned off, its elements are made semi-transparent to show
                    //they have no effect on anything
                    if (rings[ring][i].IsOn()){
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    }
                    else{
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    } 

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

                    if (rings[ring][i].IsOn()){
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    }
                    else{
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    }

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
        int tmp = -1;
        for (int i = 0; i < 12; i ++){
            if (rings[selected_ring][i] != null) {
                tmp = i;
            }
        }
        if (tmp > -1 && rings[selected_ring][tmp].IsOn()) {
            g2d.setColor(new Color(0,255,0,255));
        }
        else if (tmp > -1){
            g2d.setColor(new Color(255,0,0,128));
        }
        int radius = ring_spcr * (selected_ring + 1);
        int diameter = radius * 2;
        g2d.drawOval(centerX - radius, centerY - radius, diameter, diameter);
    }

    // Returns true if there is a laser node facing a goal point with nothing
    // blocking it
    

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
