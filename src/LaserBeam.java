import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

public class LaserBeam extends Entity {

  private static final float MAX_DISTANCE = 10000f;
  private static final float COLLISION_RADIUS = 16f;

  private final Laser source;
  private float endX;
  private float endY;
  private RingEntity hitEntity;

  public LaserBeam(Laser source) {
    super(null, 0, 0, 0.5f);
    this.source = source;
  }

  @Override
  public void render(Graphics2D g2d, TextureManager textureManager) {
    // Don't render beams from disabled lasers
    if (!source.IsOn())
      return;

    Graphics2D g = (Graphics2D) g2d.create();

    try {
      g.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

      // Outer glow
      g.setColor(new Color(255, 0, 0, 70));
      g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

      g.draw(new Line2D.Float(
          source.getX(), source.getY(),
          endX, endY));

      // Bright laser core
      g.setColor(Color.RED);
      g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

      g.draw(new Line2D.Float(
          source.getX(), source.getY(),
          endX, endY));

    } finally {
      g.dispose();
    }
  }

  public void recalculateCollision(List<Entity> entities) {
    double angle = source.getRotationRadians() + Math.PI / 2;

    float dx = (float) Math.cos(angle);
    float dy = (float) Math.sin(angle);

    float startX = source.getX();
    float startY = source.getY();

    float closest = MAX_DISTANCE;
    hitEntity = null;

    for (Entity entity : entities) {
      if (entity == source || !(entity instanceof RingEntity))
        continue;

      float ox = entity.getX() - startX;
      float oy = entity.getY() - startY;

      float projection = ox * dx + oy * dy;

      float distanceSquared = ox * ox + oy * oy - projection * projection;

      float radiusSquared = COLLISION_RADIUS * COLLISION_RADIUS;

      if (distanceSquared > radiusSquared)
        continue;

      float offset = (float) Math.sqrt(
          Math.max(0f, radiusSquared - distanceSquared));

      float hitDistance = projection - offset;

      if (hitDistance < 0)
        hitDistance = projection + offset;

      if (hitDistance >= 0 && hitDistance < closest) {
        closest = hitDistance;
        hitEntity = (RingEntity) entity;
      }
    }

    endX = startX + dx * closest;
    endY = startY + dy * closest;
  }
}
