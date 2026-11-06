public abstract class RingEntity extends Entity {

  public static final int SLOTS = 12;
  public static final int RING_SPACING = 40;

  private final int ring;
  private int slot;

  public RingEntity(String texture, int ring, int slot, float zDepth) {
    super(texture, 0, 0, zDepth);

    if (ring < 1 || ring > 4)
      throw new IllegalArgumentException("Invalid ring.");

    this.ring = ring;
    updateSlotPosition(slot);
  }

  private void updateSlotPosition(int slot) {
    if (slot < 0 || slot >= SLOTS)
      throw new IllegalArgumentException("Invalid slot.");

    this.slot = slot;

    double angle = slot * 2 * Math.PI / SLOTS;
    double radius = ring * RING_SPACING;

    setX((float) (radius * Math.cos(angle)));
    setY((float) (radius * Math.sin(angle)));
  }

  public void setRingSlot(int slot) {
    updateSlotPosition(slot);
  }

  public int getRing() {
    return ring;
  }

  public int getSlot() {
    return slot;
  }

  protected void faceTowards(float targetX, float targetY) {
    double angle = Math.atan2(
        targetY - getY(),
        targetX - getX());

    setRotationRadians(angle - Math.PI / 2);
  }
}
