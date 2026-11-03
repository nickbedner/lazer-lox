import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class App extends JPanel {
    private static final int SLOTS = 12;
    private static final int RING_COUNT = 3;
    private static final int RING_SPACING = 40;
    private static final int SPRITE_SIZE = 32;
    private static final int FPS = 60;
    private static final int FRAME_TIME = 1000 / FPS;
    private static final double LASER_SPRITE_FORWARD_ANGLE = Math.PI / 2;

    // Larger zDepth = in front. All goals are behind all movable nodes.
    private static final float SENSOR_DEPTH = 0.0f;
    private static final float NODE_DEPTH = 1.0f;

    // Gameplay structure used by the existing WinChecker.
    private final boolean[] goalRing = new boolean[SLOTS];
    private final LoxNode[][] rings = new LoxNode[RING_COUNT][SLOTS];

    // Render/update structure. Contains the SAME instances as the ring grid,
    // plus the Sensor entities; no images or entities are copied.
    private final List<Entity> entities = new ArrayList<>();

    private String currentLevel = "Win Test|Efrice|0000101101|11|4|110000001101|00";
    // "Level 1|Efrice|110000101000|000010004400|000000010140|144444444100|00";
    // "Level 2 Test|Efrice|1010001001|000010001|1444441|41014|00";
    private int cLevelInd = 0;
    private boolean levelLoaded = false;

    private int tunnelCount = 0;
    private boolean megaLaser = false;
    private boolean portalPuzzle = false;
    private boolean togglePuzzle = false;
    private boolean carryPuzzle = false;
    private boolean hasWon = false;
    private final int winTimerMax = 60;
    private int winTimer = 0;

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

    // The ring grid stores logical slots, not screen coordinates. Recalculate
    // sprite positions only after a move, level change, or panel resize.
    private boolean layoutDirty = true;
    private int lastLayoutWidth = -1;
    private int lastLayoutHeight = -1;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private boolean downloadingLevel = false;

    public App() {
        setupLevel();
        setupInput();

        Timer gameLoop = new Timer(FRAME_TIME, e -> {
            updateGame();
            repaint();
        });
        gameLoop.start();
    }

    private void setupInput() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("pressed SPACE"), "spacePressed");
        actionMap.put("spacePressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!spaceHeld)
                    spacePressed = true;
                spaceHeld = true;
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("released SPACE"), "spaceReleased");
        actionMap.put("spaceReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spaceHeld = false;
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("pressed UP"), "upPressed");
        actionMap.put("upPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!upHeld)
                    upPressed = true;
                upHeld = true;
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("released UP"), "upReleased");
        actionMap.put("upReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upHeld = false;
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("pressed DOWN"), "downPressed");
        actionMap.put("downPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!downHeld)
                    downPressed = true;
                downHeld = true;
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("released DOWN"), "downReleased");
        actionMap.put("downReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downHeld = false;
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("pressed LEFT"), "leftPressed");
        actionMap.put("leftPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!leftHeld)
                    leftPressed = true;
                leftHeld = true;
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("released LEFT"), "leftReleased");
        actionMap.put("leftReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leftHeld = false;
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("pressed RIGHT"), "rightPressed");
        actionMap.put("rightPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!rightHeld)
                    rightPressed = true;
                rightHeld = true;
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("released RIGHT"), "rightReleased");
        actionMap.put("rightReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rightHeld = false;
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("pressed ESCAPE"), "exitGame");
        actionMap.put("exitGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("pressed L"), "loadRemoteLevel");
        actionMap.put("loadRemoteLevel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLevelFromServer(1);
            }
        });
    }

    private void setupLevel() {
        if (levelLoaded)
            return;

        if (currentLevel == null || currentLevel.isEmpty()) {
            LevelLoader levelLoader = new LevelLoader();
            if (!levelLoader.LoadLevel(cLevelInd)) {
                System.err.println("No level available at index " + cLevelInd);
                return;
            }
            currentLevel = levelLoader.GetCurrentLevel();
        }

        String[] levelParts = currentLevel.split("\\|", -1);
        if (levelParts.length != 7) {
            throw new IllegalArgumentException("Expected seven level fields");
        }

        // Clear BOTH structures before building a new level. Old UUIDs and
        // entity instances should not remain in the render list.
        entities.clear();
        tunnelCount = 0;
        megaLaser = false;
        portalPuzzle = false;
        togglePuzzle = false;
        carryPuzzle = false;
        winTimer = 0;
        hasWon = false;
        selected_ring = 0;

        // Turn goal bits into proper Sensor entities for the renderer.
        for (int slot = 0; slot < SLOTS; slot++) {
            goalRing[slot] = slot < levelParts[2].length()
                    && levelParts[2].charAt(slot) == '1';
            if (goalRing[slot]) {
                entities.add(new Sensor(slot, 0, 0, SENSOR_DEPTH));
            }
        }

        for (int ring = 0; ring < RING_COUNT; ring++) {
            for (int slot = 0; slot < SLOTS; slot++) {
                LoxNode node = null;
                char type = (slot < levelParts[ring + 3].length())
                        ? levelParts[ring + 3].charAt(slot)
                        : '0';

                if (type >= '1' && type <= '3') {
                    node = new LaserBasic(ring + 1, true, slot,
                            new int[] { 0, 0 }, type - '0');
                    if (type == '3')
                        megaLaser = true;
                } else if (type >= '4' && type <= '6') {
                    node = new BlockerBasic(ring + 1, true, slot,
                            new int[] { 0, 0 }, type - '0');
                    if (type == '5')
                        tunnelCount++;
                    if (type == '6')
                        portalPuzzle = true;
                } else if (type != '0') {
                    System.out.println("Unknown node type: " + type);
                }

                rings[ring][slot] = node;
                if (node != null) {
                    // LaserBasic and BlockerBasic both extend Entity.
                    Entity entity = (Entity) node;
                    entity.setZDepth(NODE_DEPTH);
                    entities.add(entity);
                }
            }
        }

        layoutDirty = true;
        levelLoaded = true;
    }

    private void loadLevelFromServer(int levelId) {
        if (downloadingLevel)
            return;
        downloadingLevel = true;

        String url = "http://130.131.97.156/api/level.php?id=" + levelId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET().build();

        System.out.println("Downloading level " + levelId);
        httpClient.sendAsync(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((response, error) -> SwingUtilities.invokeLater(() -> {
                    downloadingLevel = false;
                    if (error != null) {
                        System.err.println("Download failed: " + error.getMessage());
                        return;
                    }
                    if (response.statusCode() != 200) {
                        System.err.println("Server returned HTTP " + response.statusCode());
                        return;
                    }

                    String downloadedLevel = response.body().stripTrailing();
                    String[] parts = downloadedLevel.split("\\|", -1);
                    if (parts.length != 7
                            || parts[0].isBlank() || parts[0].length() > 32
                            || parts[1].isBlank() || parts[1].length() > 32
                            || !parts[2].matches("[01]{12}")
                            || !parts[3].matches("[0-6]{12}")
                            || !parts[4].matches("[0-6]{12}")
                            || !parts[5].matches("[0-6]{12}")
                            || !parts[6].matches("[01]{2}")) {
                        System.err.println("Invalid level data received.");
                        return;
                    }

                    leftPressed = rightPressed = upPressed = downPressed = spacePressed = false;
                    currentLevel = downloadedLevel;
                    levelLoaded = false;
                    setupLevel();
                    repaint();
                    System.out.println("Successfully loaded level " + levelId);
                }));
    }

    private void updateGame() {
        if (!hasWon) {
            doLevelInteract();
        } else {
            winTimer++;
            if (winTimer >= winTimerMax) {
                winTimer = 0;
                cLevelInd++;
                currentLevel = "";
                levelLoaded = false;
                setupLevel();
            }
        }

        // The entity list is also a convenient update list.
        for (Entity entity : entities) {
            entity.update(1.0f / FPS);
        }
    }

    private void doLevelInteract() {
        if (downPressed)
            selected_ring = (selected_ring - 1 + RING_COUNT) % RING_COUNT;
        if (upPressed)
            selected_ring = (selected_ring + 1) % RING_COUNT;

        if (leftPressed) {
            LoxNode first = rings[selected_ring][0];
            for (int i = 0; i < SLOTS - 1; i++) {
                rings[selected_ring][i] = rings[selected_ring][i + 1];
            }
            rings[selected_ring][SLOTS - 1] = first;
            layoutDirty = true;
        }

        if (rightPressed) {
            LoxNode last = rings[selected_ring][SLOTS - 1];
            for (int i = SLOTS - 1; i > 0; i--) {
                rings[selected_ring][i] = rings[selected_ring][i - 1];
            }
            rings[selected_ring][0] = last;
            layoutDirty = true;
        }

        if (spacePressed) {
            for (LoxNode node : rings[selected_ring]) {
                if (node != null)
                    node.Toggle();
            }
        }

        if (leftPressed || rightPressed || upPressed || downPressed || spacePressed) {
            WinChecker wc = new WinChecker(megaLaser, true, tunnelCount > 0,
                    portalPuzzle, togglePuzzle, carryPuzzle);
            if (wc.checkWin(goalRing, rings)) {
                System.out.println("Level Complete!");
                hasWon = true;
            }
        }

        leftPressed = rightPressed = upPressed = downPressed = spacePressed = false;
    }

    /** Sync gameplay slots to drawable entity positions and laser rotations. */
    private void layoutEntities() {
        int width = getWidth();
        int height = getHeight();
        if (!layoutDirty && width == lastLayoutWidth && height == lastLayoutHeight) {
            return;
        }
        int centerX = width / 2;
        int centerY = height / 2;

        // Goal sensor centers are on the 4th circle.
        for (Entity entity : entities) {
            if (entity instanceof Sensor) {
                Sensor sensor = (Sensor) entity;
                double angle = sensor.getPos() * Math.PI / 6.0;
                sensor.setX((float) (centerX + RING_SPACING * 4 * Math.cos(angle)));
                sensor.setY((float) (centerY + RING_SPACING * 4 * Math.sin(angle)));
            }
        }

        // Do NOT rely on node.GetPos(): a ring movement shifts the nodes
        // between slots without changing their old GetPos() value.
        for (int ring = 0; ring < RING_COUNT; ring++) {
            for (int slot = 0; slot < SLOTS; slot++) {
                LoxNode node = rings[ring][slot];
                if (node == null)
                    continue;

                double angle = slot * Math.PI / 6.0;
                double x = centerX + RING_SPACING * (ring + 1) * Math.cos(angle);
                double y = centerY + RING_SPACING * (ring + 1) * Math.sin(angle);
                node.SetAbsolutePosition(new int[] { (int) Math.round(x), (int) Math.round(y) });

                if (node instanceof LaserBasic) {
                    Entity laser = (Entity) node;
                    int targetSlot = (slot + 6) % SLOTS;
                    if (node.GetType() == 2 && goalRing[slot]) {
                        targetSlot = slot;
                    }
                    double targetAngle = targetSlot * Math.PI / 6.0;
                    double targetX = centerX + RING_SPACING * 4 * Math.cos(targetAngle);
                    double targetY = centerY + RING_SPACING * 4 * Math.sin(targetAngle);
                    double facing = Math.atan2(targetY - y, targetX - x);
                    laser.setRotationRadians(facing - LASER_SPRITE_FORWARD_ANGLE);
                }
            }
        }

        lastLayoutWidth = width;
        lastLayoutHeight = height;
        layoutDirty = false;
    }

    private void insertionSortEntitiesByDepth() {
        for (int i = 1; i < entities.size(); i++) {
            Entity key = entities.get(i);
            int j = i - 1;
            while (j >= 0 && entities.get(j).getZDepth() > key.getZDepth()) {
                entities.set(j + 1, entities.get(j));
                j--;
            }
            entities.set(j + 1, key);
        }
    }

    private void drawEntity(Graphics2D g2d, Entity entity) {
        if (entity.getTextureId() == null)
            return;

        float alpha = entity.getOpacity();
        // The on/off effect multiplies (rather than overwrites) custom opacity
        if (entity instanceof LoxNode && !((LoxNode) entity).IsOn()) {
            alpha *= 0.5f;
        }
        if (alpha <= 0.0f)
            return;

        BufferedImage image = TextureManager.getTexture(entity.getTextureId());
        if (image == null)
            return;

        // Isolate each entity's transform and composite from subsequent sprites
        Graphics2D spriteGraphics = (Graphics2D) g2d.create();
        try {
            spriteGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            spriteGraphics.translate(entity.getX(), entity.getY());
            spriteGraphics.rotate(entity.getRotationRadians());
            spriteGraphics.drawImage(image, -SPRITE_SIZE / 2, -SPRITE_SIZE / 2,
                    SPRITE_SIZE, SPRITE_SIZE, null);
        } finally {
            spriteGraphics.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setColor(new Color(61, 88, 179));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            layoutEntities();
            insertionSortEntitiesByDepth();
            for (Entity entity : entities) {
                drawEntity(g2d, entity);
            }

            // UI overlay always renders after world entities.
            drawSelectedRing(g2d);
        } finally {
            g2d.dispose();
        }
    }

    private void drawSelectedRing(Graphics2D g2d) {
        Color ringColor = Color.BLUE;
        for (LoxNode node : rings[selected_ring]) {
            if (node != null) {
                ringColor = node.IsOn() ? Color.GREEN : new Color(255, 0, 0, 128);
                break;
            }
        }
        g2d.setColor(ringColor);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int radius = RING_SPACING * (selected_ring + 1);
        g2d.drawOval(getWidth() / 2 - radius, getHeight() / 2 - radius,
                radius * 2, radius * 2);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // TextureManager methods are static; no TextureManager instance needed
            TextureManager.getTexture("Goal.png");
            TextureManager.getTexture("lasperPointer.png");
            TextureManager.getTexture("Blocker.png");

            JFrame frame = new JFrame("LazerLox");
            frame.add(new App());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 480);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
