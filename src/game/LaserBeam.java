package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
    super(source.getX(), source.getY(), 0.5f);
    this.source = source;
  }

  @Override
  public void update(float deltaTime) {
    // TODO: Should beam collision he in here?
  }

  @Override
  public void render(Graphics2D g2d) {
    if (!source.IsOn())
      return;

    Graphics2D g = (Graphics2D) g2d.create();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(new Color(255, 0, 0, 70));
      g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.draw(new Line2D.Float(getX(), getY(), endX, endY));

      g.setColor(Color.RED);
      g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.draw(new Line2D.Float(getX(), getY(), endX, endY));
    } finally {
      g.dispose();
    }
  }

  public void recalculateCollision(List<Entity> entities) {
    double angle = source.getRotationRadians() + Math.PI / 2;
    float dx = (float) Math.cos(angle);
    float dy = (float) Math.sin(angle);

    setPosition(source.getX(), source.getY());
    float startX = getX();
    float startY = getY();
    float closest = MAX_DISTANCE;
    hitEntity = null;

    for (Entity entity : entities) {
      if (entity == source || !(entity instanceof RingEntity))
        continue;

      RingEntity target = (RingEntity) entity;
      float ox = target.getX() - startX;
      float oy = target.getY() - startY;
      float projection = ox * dx + oy * dy;
      float distanceSquared = ox * ox + oy * oy - projection * projection;
      float radiusSquared = COLLISION_RADIUS * COLLISION_RADIUS;
      if (distanceSquared > radiusSquared)
        continue;

      float offset = (float) Math.sqrt(Math.max(0f, radiusSquared - distanceSquared));
      float hitDistance = projection - offset;
      if (hitDistance < 0)
        hitDistance = projection + offset;

      if (hitDistance >= 0 && hitDistance < closest) {
        closest = hitDistance;
        hitEntity = target;
      }
    }

    endX = startX + dx * closest;
    endY = startY + dy * closest;
  }
}
