package states;

import laserlox.*;
import core.*;
import game.*;
import graphics.*;

import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GameState implements State {
  private static final int SLOTS = 12;
  private static final int RING_COUNT = 3;
  private static final int RING_SPACING = 40;

  // Sensor should have lowest depth value because farthest from the camera
  private static final float SENSOR_DEPTH = 0.0f;

  private final boolean[] goalRing = new boolean[SLOTS];
  private final LoxNode[][] rings = new LoxNode[RING_COUNT][SLOTS];

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

  private int selected_ring = 0;

  private final App app;

  private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  private boolean downloadingLevel = false;

  public GameState(App app) {
    this.app = app;
    setupLevel();
  }

  @Override
  public void update(float deltaTime, InputManager input) {
    updateGame(deltaTime, input);
  }

  @Override
  public void render(Graphics2D g2d) {
    // Background blue color
    g2d.setColor(new Color(61, 88, 179));
    g2d.fillRect(0, 0, app.getWidth(), app.getHeight());

    Graphics2D centered = (Graphics2D) g2d.create();
    try {
      // Translate to middle of screen
      centered.translate(app.getWidth() / 2.0, app.getHeight() / 2.0);

      // Depth sort the game entities for rendering
      entities.sort((a, b) -> Float.compare(a.getZDepth(), b.getZDepth()));
      for (Entity entity : entities) {
        entity.render(centered);
      }
    } finally {
      centered.dispose();
    }

    // Draw the selection right
    drawSelectedRing(g2d);
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

    entities.clear();
    tunnelCount = 0;
    megaLaser = false;
    portalPuzzle = false;
    togglePuzzle = false;
    carryPuzzle = false;
    winTimer = 0;
    hasWon = false;
    selected_ring = 0;

    for (int slot = 0; slot < SLOTS; slot++) {
      goalRing[slot] = slot < levelParts[2].length() && levelParts[2].charAt(slot) == '1';
      if (goalRing[slot])
        entities.add(new Sensor(new Sprite(app.getTextureManager().getTexture("Goal.png")), slot, SENSOR_DEPTH));

    }

    for (int ring = 0; ring < RING_COUNT; ring++) {
      for (int slot = 0; slot < SLOTS; slot++) {
        LoxNode node = null;
        char type = (slot < levelParts[ring + 3].length()) ? levelParts[ring + 3].charAt(slot) : '0';

        if (type >= '1' && type <= '3') {
          node = new Laser(new Sprite(app.getTextureManager().getTexture("lasperPointer.png")), ring + 1, true, slot,
              type - '0', goalRing);
          if (type == '3')
            megaLaser = true;
        } else if (type >= '4' && type <= '6') {
          node = new Blocker(new Sprite(app.getTextureManager().getTexture("Blocker.png")), ring + 1, true, slot,
              type - '0');
          if (type == '5')
            tunnelCount++;
          if (type == '6')
            portalPuzzle = true;
        } else if (type != '0')
          System.out.println("Unknown node type: " + type);

        rings[ring][slot] = node;
        if (node != null) {
          Entity entity = (Entity) node;
          entities.add(entity);

          if (node instanceof Laser) {
            entities.add(new LaserBeam((Laser) node));
          }
        }
      }
    }

    refreshLaserBeams();
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
          if (app.getCurrentState() != this)
            return;
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

          app.getInputManager().clearTransitions();
          currentLevel = downloadedLevel;
          levelLoaded = false;
          setupLevel();
          app.repaint();
          System.out.println("Successfully loaded level " + levelId);
        }));
  }

  private void updateGame(float deltaTime, InputManager input) {
    // Pressing escape switches to menu state
    if (input.wasPressed(KeyEvent.VK_ESCAPE)) {
      app.changeState(new MenuState(app));
      return;
    }
    // TODO: This is temp, pressing L loads a level from the server
    if (input.wasPressed(KeyEvent.VK_L))
      loadLevelFromServer(1);

    if (!hasWon)
      doLevelInteract(input);
    else {
      winTimer++;
      if (winTimer >= winTimerMax) {
        winTimer = 0;
        cLevelInd++;
        currentLevel = "";
        levelLoaded = false;
        setupLevel();
        if (!levelLoaded) {
          app.changeState(new MenuState(app));
          return;
        }
      }
    }

    // Update game entity logics
    for (Entity entity : entities)
      entity.update(deltaTime);
  }

  private void refreshLaserBeams() {
    for (Entity entity : entities) {
      if (entity instanceof LaserBeam)
        ((LaserBeam) entity).recalculateCollision(entities);
    }
  }

  private void doLevelInteract(InputManager input) {
    boolean downPressed = input.wasPressed(KeyEvent.VK_DOWN);
    boolean upPressed = input.wasPressed(KeyEvent.VK_UP);
    boolean leftPressed = input.wasPressed(KeyEvent.VK_LEFT);
    boolean rightPressed = input.wasPressed(KeyEvent.VK_RIGHT);
    boolean spacePressed = input.wasPressed(KeyEvent.VK_SPACE);

    if (downPressed)
      selected_ring = (selected_ring - 1 + RING_COUNT) % RING_COUNT;
    if (upPressed)
      selected_ring = (selected_ring + 1) % RING_COUNT;

    if (leftPressed) {
      LoxNode first = rings[selected_ring][0];
      for (int i = 0; i < SLOTS - 1; i++)
        rings[selected_ring][i] = rings[selected_ring][i + 1];

      rings[selected_ring][SLOTS - 1] = first;
    }
    if (rightPressed) {
      LoxNode last = rings[selected_ring][SLOTS - 1];
      for (int i = SLOTS - 1; i > 0; i--)
        rings[selected_ring][i] = rings[selected_ring][i - 1];

      rings[selected_ring][0] = last;
    }
    if (leftPressed || rightPressed) {
      for (int slot = 0; slot < SLOTS; slot++) {
        LoxNode node = rings[selected_ring][slot];

        if (node != null)
          ((RingEntity) node).setRingSlot(slot);
      }

      refreshLaserBeams();
    }

    if (spacePressed) {
      for (LoxNode node : rings[selected_ring]) {
        if (node != null)
          node.Toggle();
      }
    }

    if (leftPressed || rightPressed || upPressed || downPressed || spacePressed) {
      WinChecker wc = new WinChecker(megaLaser, true, tunnelCount > 0, portalPuzzle, togglePuzzle, carryPuzzle);
      if (wc.checkWin(goalRing, rings)) {
        System.out.println("Level Complete!");
        hasWon = true;
      }
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
    g2d.drawOval(app.getWidth() / 2 - radius, app.getHeight() / 2 - radius,
        radius * 2, radius * 2);
  }

}
