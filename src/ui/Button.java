package ui;

import core.InputManager;
import game.Entity;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class Button extends Entity {
  private final String label;
  private final Runnable action;
  private int width;
  private int height;
  private boolean selected;
  private boolean hovered;

  public Button(String label, Runnable action) {
    super(0, 0, 100.0f);
    this.label = Objects.requireNonNull(label, "label");
    this.action = Objects.requireNonNull(action, "action");
  }

  public void setBounds(int x, int y, int width, int height) {
    setPosition(x + width / 2.0f, y + height / 2.0f);
    this.width = width;
    this.height = height;
  }

  private Rectangle getBounds() {
    return new Rectangle(Math.round(getX() - width / 2.0f), Math.round(getY() - height / 2.0f), width, height);
  }

  public boolean isHovered(InputManager input) {
    return getBounds().contains(input.getMouseX(), input.getMouseY());
  }

  public boolean wasClicked(InputManager input) {
    int button = MouseEvent.BUTTON1;
    Rectangle bounds = getBounds();
    return input.wasMouseReleased(button)
        && bounds.contains(input.getMousePressX(button), input.getMousePressY(button))
        && bounds.contains(input.getMouseReleaseX(button), input.getMouseReleaseY(button));
  }

  public void setVisualState(boolean selected, boolean hovered) {
    this.selected = selected;
    this.hovered = hovered;
  }

  public void activate() {
    action.run();
  }

  @Override
  public void update(float deltaTime) {
    // UI state and input are managed by MenuState for now.
  }

  @Override
  public void render(Graphics2D g2d) {
    Graphics2D g = (Graphics2D) g2d.create();
    try {
      g.translate(getX(), getY());
      g.rotate(getRotationRadians());
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int left = -width / 2;
      int top = -height / 2;
      g.setColor((selected || hovered) ? new Color(231, 239, 255) : new Color(31, 47, 106));
      g.fillRoundRect(left, top, width, height, 16, 16);

      g.setColor((selected || hovered) ? new Color(40, 69, 153) : new Color(148, 174, 235));
      g.setStroke(new BasicStroke(2f));
      g.drawRoundRect(left, top, width, height, 16, 16);

      g.setColor((selected || hovered) ? new Color(29, 49, 118) : Color.WHITE);
      g.setFont(new Font("SansSerif", Font.BOLD, 22));
      FontMetrics fm = g.getFontMetrics();
      int x = -fm.stringWidth(label) / 2;
      int y = (fm.getAscent() - fm.getDescent()) / 2;
      g.drawString(label, x, y);
    } finally {
      g.dispose();
    }
  }
}
