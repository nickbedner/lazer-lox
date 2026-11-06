package graphics;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Sprite {
  private final BufferedImage image;
  private int width;
  private int height;
  private float x;
  private float y;
  private double rotationRadians;
  private float opacity = 1.0f;

  public Sprite(BufferedImage image) {
    this(image, 32, 32);
  }

  public Sprite(BufferedImage image, int width, int height) {
    this.image = image;
    setSize(width, height);
  }

  // Draw relative to the caller's g coordinate system using this sprite's
  // transform added to it
  public void render(Graphics2D g) {
    if (image == null || opacity <= 0.0f)
      return;

    Graphics2D copy = (Graphics2D) g.create();
    try {
      copy.translate(x, y);
      copy.rotate(rotationRadians);
      copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
      copy.drawImage(image, -width / 2, -height / 2, width, height, null);
    } finally {
      copy.dispose();
    }
  }

  public BufferedImage getImage() {
    return image;
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

  public double getRotationRadians() {
    return rotationRadians;
  }

  public void setRotationRadians(double rotationRadians) {
    this.rotationRadians = rotationRadians;
  }

  public void setRotationDegrees(double degrees) {
    setRotationRadians(Math.toRadians(degrees));
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public float getOpacity() {
    return opacity;
  }

  public void setOpacity(float opacity) {
    this.opacity = opacity;
  }
}
