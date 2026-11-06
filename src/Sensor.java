public class Sensor extends RingEntity {

  private boolean activated;

  public Sensor(String texture, int slot, float zDepth) {
    super(texture, 4, slot, zDepth);
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
