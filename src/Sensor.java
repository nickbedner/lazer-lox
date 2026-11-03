
public class Sensor extends Entity {

  private final int pos;
  private boolean activated;

  // Create a sensor at a ring position.
  public Sensor(int pos) {
    this(pos, 0, 0, 0);
  }

  // Create a sensor with an absolute position.
  public Sensor(int pos, float x, float y, float zDepth) {

    // Initialize Entity with the sensor texture.
    super("Goal.png", x, y, zDepth);

    if (pos < 0 || pos > 11) {
      throw new IllegalArgumentException(
          "There are 12 sensor positions, so position must be between 0 and 11.");
    }

    this.pos = pos;
    this.activated = false;
  }

  // Get the sensor's position on the outer ring.
  public int getPos() {
    return pos;
  }

  // Check whether the sensor is activated.
  public boolean isActivated() {
    return activated;
  }

  // Set whether the sensor is activated.
  public void setActivated(boolean activated) {
    this.activated = activated;
  }

  @Override
  public void update(float deltaTime) {
    // Sensor-specific logic can go here later.
  }
}
