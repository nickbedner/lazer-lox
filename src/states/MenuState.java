package states;

import laserlox.App;
import core.InputManager;
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

public class MenuState implements State {
  private static final int BUTTON_WIDTH = 220;
  private static final int BUTTON_HEIGHT = 50;
  private static final int BUTTON_GAP = 16;

  private final App app;
  private final List<Entity> entities = new ArrayList<>();
  private int selectedIndex = 0;

  public MenuState(App app) {
    this.app = app;
    // Two buttons, play to enter game state and exit to quit progran
    entities.add(new Button("Play", () -> app.changeState(new GameState(app))));
    entities.add(new Button("Exit", () -> System.exit(0)));
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
    drawCentered(g, "LAZER LOX", height / 2 - 95);

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
    int x = (app.getWidth() - BUTTON_WIDTH) / 2;
    int startY = app.getHeight() / 2 - 30;
    for (int i = 0; i < entities.size(); i++) {
      buttonAt(i).setBounds(x, startY + i * (BUTTON_HEIGHT + BUTTON_GAP), BUTTON_WIDTH, BUTTON_HEIGHT);
    }
  }

  private void drawCentered(Graphics2D g, String text, int y) {
    FontMetrics fm = g.getFontMetrics();
    g.drawString(text, (app.getWidth() - fm.stringWidth(text)) / 2, y);
  }
}
