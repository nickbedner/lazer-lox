import java.util.Objects;
import java.util.UUID;

/** Game data only: rendering backends consume these properties. */
public class Entity {
  private final UUID uuid;
  private String textureId;

  // X and Y are the sprite CENTER in screen coordinates for this renderer.
  private float x;
  private float y;
  private float zDepth;

  // 1 = opaque, 0 = fully transparent. Rotation is in radians.
  private float opacity = 1.0f;
  private double rotationRadians = 0.0;

  public Entity(String textureId, float x, float y, float zDepth) {
    this(UUID.randomUUID(), textureId, x, y, zDepth);
  }

  // Used when restoring the same entity from a saved level.
  public Entity(UUID uuid, String textureId, float x, float y, float zDepth) {
    this.uuid = Objects.requireNonNull(uuid, "uuid");
    this.textureId = textureId;
    this.x = x;
    this.y = y;
    this.zDepth = zDepth;
  }

  public void update(float deltaTime) {
    // Entity-specific behavior may be added by subclasses.
  }

  public UUID getUuid() {
    return uuid;
  }

  public String getTextureId() {
    return textureId;
  }

  public void setTextureId(String textureId) {
    this.textureId = textureId;
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  /** Higher values are drawn later (in front of lower values). */
  public float getZDepth() {
    return zDepth;
  }

  public void setZDepth(float zDepth) {
    if (!Float.isFinite(zDepth)) {
      throw new IllegalArgumentException("zDepth must be finite");
    }
    this.zDepth = zDepth;
  }

  public float getOpacity() {
    return opacity;
  }

  public void setOpacity(float opacity) {
    if (!Float.isFinite(opacity) || opacity < 0.0f || opacity > 1.0f) {
      throw new IllegalArgumentException("Opacity must be between 0 and 1");
    }
    this.opacity = opacity;
  }

  public double getRotationRadians() {
    return rotationRadians;
  }

  public void setRotationRadians(double rotationRadians) {
    if (!Double.isFinite(rotationRadians)) {
      throw new IllegalArgumentException("Rotation must be finite");
    }
    this.rotationRadians = rotationRadians;
  }

  public void setRotationDegrees(double degrees) {
    setRotationRadians(Math.toRadians(degrees));
  }
}
