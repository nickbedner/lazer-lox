package game;

/** An entity whose world position is determined by its ring and slot. */
public abstract class RingEntity extends Entity {
  public static final int SLOTS = 12;
  public static final int RING_SPACING = 40;

  private final int ring;
  private int slot;

  protected RingEntity(int ring, int slot, float zDepth) {
    super(0, 0, zDepth);
    if (ring < 1 || ring > 4)
      throw new IllegalArgumentException("Invalid ring.");
    validateSlot(slot);
    this.ring = ring;
    this.slot = slot;
    updatePosition();
    // Do not call setRingSlot() here; Laser overrides it and needs
    // its own fields initialized before that override can run.
  }

  private static void validateSlot(int slot) {
    if (slot < 0 || slot >= SLOTS)
      throw new IllegalArgumentException("Invalid slot.");
  }

  private void updatePosition() {
    double angle = slot * 2.0 * Math.PI / SLOTS;
    float radius = ring * RING_SPACING;
    setPosition((float) (radius * Math.cos(angle)),
        (float) (radius * Math.sin(angle)));
  }

  public int getRing() {
    return ring;
  }

  public int getSlot() {
    return slot;
  }

  public void setRingSlot(int slot) {
    validateSlot(slot);
    this.slot = slot;
    updatePosition();
  }

  protected void faceTowards(float targetX, float targetY) {
    double angle = Math.atan2(targetY - getY(), targetX - getX());
    setRotationRadians(angle - Math.PI / 2);
  }

  @Override
  public void update(float deltaTime) {
  }
}
