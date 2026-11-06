package game;

import graphics.Sprite;
import java.awt.Graphics2D;
import java.util.Objects;

public class Sensor extends RingEntity {
  private final Sprite sprite;
  private boolean activated;

  public Sensor(Sprite sprite, int slot, float zDepth) {
    super(4, slot, zDepth);
    this.sprite = Objects.requireNonNull(sprite, "sprite");
  }

  public Sprite getSprite() {
    return sprite;
  }

  @Override
  public void render(Graphics2D g) {
    Graphics2D local = (Graphics2D) g.create();
    try {
      local.translate(getX(), getY());
      local.rotate(getRotationRadians());
      sprite.render(local);
    } finally {
      local.dispose();
    }
  }

  public int getPos() {
    return getSlot();
  }

  public boolean isActivated() {
    return activated;
  }

  public void setActivated(boolean activated) {
    this.activated = activated;
  }
}
