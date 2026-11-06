package core;

import java.awt.Graphics2D;

public interface State {
  void update(float deltaTime, InputManager input);

  void render(Graphics2D g);
}
