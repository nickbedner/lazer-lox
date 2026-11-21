package states;

import laserlox.App;
import core.InputManager;
import core.LevelLoader;
import core.State;
import game.Entity;
import ui.Button;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class LevelSelectState implements State {
  private static final int BUTTON_WIDTH = 220;
  private static final int BUTTON_HEIGHT = 50;
  private static final int BUTTON_GAP = 16;

  private final App app;
  private final List<Entity> entities = new ArrayList<>();
  private int selectedIndex = 0;

  public LevelSelectState(App app) {
    this.app = app;
    // Two buttons, play to enter game state and exit to quit program
    LevelLoader ll = new LevelLoader();
    int levels = ll.GetNumLevels();
    entities.add(new Button("Return", () -> app.changeState(new MenuState(app))));
    entities.add(new Button("Prev Page", () -> app.changeState(new MenuState(app))));
    entities.add(new Button("Next Page", () -> app.changeState(new MenuState(app))));
    for (int i = 0; i < levels; i ++) {
      int tmp = i;
      //Remember level 0 is level 1!
      if (i < 9)
        entities.add(new Button("0" + (tmp + 1), () -> app.changeState(new GameState(app, tmp))));
      else
        entities.add(new Button("" + (tmp + 1), () -> app.changeState(new GameState(app, tmp))));
    }
  }

  @Override
  public void update(float deltaTime, InputManager input) {
    layoutButtons();
    for (Entity entity : entities)
      entity.update(deltaTime);

    // Escape quits from the main menu; Escape in GameState returns here.
    if (input.wasPressed(KeyEvent.VK_ESCAPE)) {
      System.exit(0);
      return;
    }

    if (input.wasPressed(KeyEvent.VK_UP)) {
      selectedIndex = (selectedIndex - 1 + entities.size()) % entities.size();
    }
    if (input.wasPressed(KeyEvent.VK_DOWN)) {
      selectedIndex = (selectedIndex + 1) % entities.size();
    }

    // Move selection when the mouse actually moves, rather than every frame.
    if (input.wasMouseMoved()) {
      for (int i = 0; i < entities.size(); i++) {
        if (buttonAt(i).isHovered(input)) {
          selectedIndex = i;
          break;
        }
      }
    }

    // Mouse click must begin and end over the same button
    for (int i = 0; i < entities.size(); i++) {
      if (buttonAt(i).wasClicked(input)) {
        selectedIndex = i;
        buttonAt(i).activate();
        return;
      }
    }

    // Space to select a button
    if (input.wasPressed(KeyEvent.VK_ENTER) || input.wasPressed(KeyEvent.VK_SPACE))
      buttonAt(selectedIndex).activate();
  }

  @Override
  public void render(Graphics2D g) {
    // Positions buttons on the screen
    layoutButtons();
    int width = app.getWidth();
    int height = app.getHeight();

    // Draws background blue
    g.setColor(new Color(61, 88, 179));
    g.fillRect(0, 0, width, height);

    // Title text and font
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(Color.WHITE);
    g.setFont(new Font("SansSerif", Font.BOLD, 48));
    drawCentered(g, "Level Selector", height / 2 - 125);

    // Update state of buttons using input manager
    InputManager input = app.getInputManager();
    for (int i = 0; i < entities.size(); i++)
      buttonAt(i).setVisualState(i == selectedIndex, buttonAt(i).isHovered(input));

    // Depth sort the entites so they render in the correct order
    entities.sort((a, b) -> Float.compare(a.getZDepth(), b.getZDepth()));
    for (Entity entity : entities)
      entity.render(g);
  }

  private Button buttonAt(int index) {
    return (Button) entities.get(index);
  }

  private void layoutButtons() {
    //Normal Button Positioning
    int x1 = (app.getWidth() - BUTTON_WIDTH) / 2;
    //Grid Button Positioning
    int x2 = (app.getWidth() - BUTTON_HEIGHT * 4 - BUTTON_GAP * 3) / 2;
    int y2 = (app.getHeight() - BUTTON_HEIGHT * 4 - BUTTON_GAP * 3) / 2 + 20;
    for (int i = 0; i < entities.size(); i++) {
      //Normal Buttons are placed like any other menu
      if (i == 0)
        buttonAt(i).setBounds(1, 1, BUTTON_WIDTH, BUTTON_HEIGHT);
      else if (i == 1)
        buttonAt(i).setBounds(1, app.getHeight() - BUTTON_HEIGHT - 1, BUTTON_WIDTH, BUTTON_HEIGHT);
      else if (i == 2)
        buttonAt(i).setBounds(app.getWidth() - BUTTON_WIDTH - 1, app.getHeight() - BUTTON_HEIGHT - 1, BUTTON_WIDTH, BUTTON_HEIGHT);

      //Grid Buttons are placed in a grid.
      else {
        int row = (i - 3) / 4;
        int col = (i - 3) % 4;
        buttonAt(i).setBounds(x2 + col * (BUTTON_HEIGHT + BUTTON_GAP), y2 + row * (BUTTON_HEIGHT + BUTTON_GAP), BUTTON_HEIGHT, BUTTON_HEIGHT);
      }
    }
  }

  private void drawCentered(Graphics2D g, String text, int y) {
    FontMetrics fm = g.getFontMetrics();
    g.drawString(text, (app.getWidth() - fm.stringWidth(text)) / 2, y);
  }
}
