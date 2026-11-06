import java.util.Objects;
import java.util.UUID;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class Entity {
  private static final int SPRITE_SIZE = 32;

  private final UUID uuid;
  private String textureId;

  private float x;
  private float y;
  private float zDepth;

  private float opacity = 1.0f;
  private double rotationRadians = 0.0;

  public Entity(String textureId, float x, float y, float zDepth) {
    this(UUID.randomUUID(), textureId, x, y, zDepth);
  }

  public Entity(UUID uuid, String textureId, float x, float y, float zDepth) {
    this.uuid = Objects.requireNonNull(uuid, "uuid");
    this.textureId = textureId;
    this.x = x;
    this.y = y;
    this.zDepth = zDepth;
  }

  public void update(float deltaTime) {
  }

  public void render(Graphics2D g2d, TextureManager textureManager) {
    if (textureId == null)
      return;

    float alpha = getRenderOpacity();

    if (alpha <= 0.0f)
      return;

    BufferedImage image = textureManager.getTexture(textureId);

    if (image == null)
      return;

    Graphics2D g = (Graphics2D) g2d.create();

    try {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

      g.translate(x, y);
      g.rotate(rotationRadians);

      g.drawImage(image, -SPRITE_SIZE / 2, -SPRITE_SIZE / 2, SPRITE_SIZE, SPRITE_SIZE, null);

    } finally {
      g.dispose();
    }
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

  public float getZDepth() {
    return zDepth;
  }

  public void setZDepth(float zDepth) {
    if (!Float.isFinite(zDepth))
      throw new IllegalArgumentException("zDepth must be finite");

    this.zDepth = zDepth;
  }

  public float getOpacity() {
    return opacity;
  }

  public void setOpacity(float opacity) {
    if (!Float.isFinite(opacity) || opacity < 0.0f || opacity > 1.0f)
      throw new IllegalArgumentException("Opacity must be between 0 and 1");

    this.opacity = opacity;
  }

  public double getRotationRadians() {
    return rotationRadians;
  }

  public void setRotationRadians(double rotationRadians) {
    if (!Double.isFinite(rotationRadians))
      throw new IllegalArgumentException("Rotation must be finite");

    this.rotationRadians = rotationRadians;
  }

  public void setRotationDegrees(double degrees) {
    setRotationRadians(Math.toRadians(degrees));
  }

  protected float getRenderOpacity() {
    return opacity;
  }
}
