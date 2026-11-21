package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class LaserBeam extends Entity {
  private static final float MAX_DISTANCE = 10000f;
  private static final float COLLISION_RADIUS = 16f;
  private static final int UNBLOCKED_BEAM_LENGTH = 1000;

  // Laser appearance
  private static final int SPRITE_SIZE = 20;
  private static final int END_HIT_OVERLAP = 8;

  private final Laser source;
  private final BufferedImage bodyTexture;
  private final BufferedImage endTexture;

  private float endX;
  private float endY;
  private RingEntity hitEntity;

  public LaserBeam(Laser source, BufferedImage bodyTexture, BufferedImage endTexture) {
    super(source.getX(), source.getY(), 0.5f);
    this.source = source;
    this.bodyTexture = bodyTexture;
    this.endTexture = endTexture;
  }

  @Override
  public void update(float deltaTime) {
    // Collision is recalculated by GameState
  }

  @Override
  public void render(Graphics2D g2d) {
    if (!source.IsOn())
      return;

    double angle = source.getRotationRadians() + Math.PI / 2;
    boolean hit = hitEntity != null;

    // If no collision, extend far beyond the visible windowGameState will clip the
    // beam at the window border
    int length = hit
        ? Math.round((float) Math.hypot(endX - getX(), endY - getY()))
        : UNBLOCKED_BEAM_LENGTH;

    if (length <= 0)
      return;

    Graphics2D g = (Graphics2D) g2d.create();

    try {
      g.translate(getX(), getY());
      g.rotate(angle);

      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

      if (bodyTexture != null && endTexture != null) {

        int endWidth = Math.min(SPRITE_SIZE, length);
        int bodyWidth = hit ? Math.max(0, length - endWidth + END_HIT_OVERLAP) : length;

        // Draw the laser beam unblocked beams continue until the window clips them.
        g.drawImage(bodyTexture, 0, -SPRITE_SIZE / 2, bodyWidth, SPRITE_SIZE, null);

        // Only draw LaserEnd when an entity is hit
        if (hit) {

          // IMPORTANT: This slightly shrinks the endpoint of the laser so it matches the
          // width of the laser beam
          int endHeight = SPRITE_SIZE - 4;

          // Draw the endpoint flipped horizontally
          g.drawImage(endTexture, bodyWidth, -endHeight / 2, bodyWidth + endWidth, endHeight / 2, endTexture.getWidth(),
              0, 0, endTexture.getHeight(), null);
        }

      } else {
        // Fallback rendering if textures are unavailable
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(255, 0, 0, 70));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g.draw(new Line2D.Float(0, 0, length, 0));

        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g.draw(new Line2D.Float(0, 0, length, 0));
      }

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
      if (entity == source
          || !(entity instanceof RingEntity)
          || (entity instanceof LoxNode
              && !((LoxNode) entity).IsOn()))
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