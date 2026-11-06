package game;

import java.awt.Graphics2D;
import java.util.UUID;

public abstract class Entity {
  private final UUID uuid;
  private float x;
  private float y;
  private float zDepth;
  private double rotationRadians;

  protected Entity(float x, float y, float zDepth) {
    this(UUID.randomUUID(), x, y, zDepth);
  }

  protected Entity(UUID uuid, float x, float y, float zDepth) {
    this.uuid = uuid;
    setPosition(x, y);
    setZDepth(zDepth);
  }

  public UUID getUuid() {
    return uuid;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public void setPosition(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public void setX(float x) {
    setPosition(x, y);
  }

  public void setY(float y) {
    setPosition(x, y);
  }

  public float getZDepth() {
    return zDepth;
  }

  public void setZDepth(float zDepth) {
    this.zDepth = zDepth;
  }

  public double getRotationRadians() {
    return rotationRadians;
  }

  public void setRotationRadians(double rotationRadians) {
    this.rotationRadians = rotationRadians;
  }

  public void setRotationDegrees(double degrees) {
    setRotationRadians(Math.toRadians(degrees));
  }

  public abstract void update(float deltaTime);

  public abstract void render(Graphics2D g);
}
