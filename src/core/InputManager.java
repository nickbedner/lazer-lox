package core;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InputManager {
  private final Set<Integer> held = new HashSet<>();
  private final Set<Integer> pressed = new HashSet<>();
  private final Set<Integer> released = new HashSet<>();

  private final Set<Integer> mouseHeld = new HashSet<>();
  private final Set<Integer> mousePressed = new HashSet<>();
  private final Set<Integer> mouseReleased = new HashSet<>();
  private final Map<Integer, Point> mousePressPositions = new HashMap<>();
  private final Map<Integer, Point> mouseReleasePositions = new HashMap<>();
  private int mouseX = -1;
  private int mouseY = -1;
  private boolean mouseMovedThisFrame = false;

  public void keyPressed(int keyCode) {
    if (held.add(keyCode))
      pressed.add(keyCode);
  }

  public void keyReleased(int keyCode) {
    if (held.remove(keyCode))
      released.add(keyCode);
  }

  public boolean isDown(int keyCode) {
    return held.contains(keyCode);
  }

  public boolean wasPressed(int keyCode) {
    return pressed.contains(keyCode);
  }

  public boolean wasReleased(int keyCode) {
    return released.contains(keyCode);
  }

  public void mouseMoved(int x, int y) {
    if (mouseX != x || mouseY != y)
      mouseMovedThisFrame = true;

    mouseX = x;
    mouseY = y;
  }

  public void mouseExited() {
    mouseMoved(-1, -1);
  }

  public void mousePressed(int button, int x, int y) {
    mouseMoved(x, y);
    if (mouseHeld.add(button)) {
      mousePressed.add(button);
      mousePressPositions.put(button, new Point(x, y));
    }
  }

  public void mouseReleased(int button, int x, int y) {
    mouseMoved(x, y);
    if (mouseHeld.remove(button)) {
      mouseReleased.add(button);
      mouseReleasePositions.put(button, new Point(x, y));
    }
  }

  public int getMouseX() {
    return mouseX;
  }

  public int getMouseY() {
    return mouseY;
  }

  public boolean wasMouseMoved() {
    return mouseMovedThisFrame;
  }

  public boolean isMouseDown(int button) {
    return mouseHeld.contains(button);
  }

  public boolean wasMousePressed(int button) {
    return mousePressed.contains(button);
  }

  public boolean wasMouseReleased(int button) {
    return mouseReleased.contains(button);
  }

  public int getMousePressX(int button) {
    Point point = mousePressPositions.get(button);
    return point == null ? -1 : point.x;
  }

  public int getMousePressY(int button) {
    Point point = mousePressPositions.get(button);
    return point == null ? -1 : point.y;
  }

  public int getMouseReleaseX(int button) {
    Point point = mouseReleasePositions.get(button);
    return point == null ? -1 : point.x;
  }

  public int getMouseReleaseY(int button) {
    Point point = mouseReleasePositions.get(button);
    return point == null ? -1 : point.y;
  }

  public void endFrame() {
    clearTransitions();
  }

  public void clearTransitions() {
    pressed.clear();
    released.clear();
    mousePressed.clear();
    mouseReleased.clear();
    mouseMovedThisFrame = false;
  }

  public void reset() {
    held.clear();
    mouseHeld.clear();
    mousePressPositions.clear();
    mouseReleasePositions.clear();
    mouseExited();
    clearTransitions();
  }
}
