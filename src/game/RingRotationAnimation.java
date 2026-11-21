package game;

public final class RingRotationAnimation {
  public static final float DURATION_SECONDS = 0.125f;

  private final LoxNode[] nodes;
  private final int direction;
  private float elapsedSeconds;
  private boolean complete;

  public RingRotationAnimation(LoxNode[] nodes, int direction) {
    if (nodes == null || nodes.length != RingEntity.SLOTS)
      throw new IllegalArgumentException("A ring must have exactly 12 slots.");

    if (direction != -1 && direction != 1)
      throw new IllegalArgumentException("Direction must be -1 or +1.");

    this.nodes = nodes;
    this.direction = direction;
  }

  public boolean update(float deltaTime) {
    if (complete)
      return true;

    elapsedSeconds = Math.min(DURATION_SECONDS, elapsedSeconds + Math.max(0.0f, deltaTime));

    double t = elapsedSeconds / DURATION_SECONDS;

    // Smoothstep: accelerate, then decelerate
    double eased = t * t * (3.0 - 2.0 * t);

    double angleStep = 2.0 * Math.PI / RingEntity.SLOTS;

    for (LoxNode node : nodes) {
      if (node == null)
        continue;

      RingEntity entity = (RingEntity) node;

      int startSlot = entity.getSlot();
      int endSlot = Math.floorMod(startSlot + direction, RingEntity.SLOTS);

      entity.setVisualAngleRadians((startSlot + direction * eased) * angleStep);

      // Rotate laser emitters smoothly as they travel
      if (node instanceof Laser) {
        Laser laser = (Laser) node;

        double startAngle = laser.getFacingRadiansForSlot(startSlot);
        double endAngle = laser.getFacingRadiansForSlot(endSlot);
        double shortestTurn = Math.atan2(Math.sin(endAngle - startAngle), Math.cos(endAngle - startAngle));

        laser.setRotationRadians(startAngle + shortestTurn * eased);
      }
    }

    if (elapsedSeconds < DURATION_SECONDS)
      return false;

    // Commit the discrete puzzle state after animation
    if (direction == 1) {
      LoxNode last = nodes[RingEntity.SLOTS - 1];

      for (int i = RingEntity.SLOTS - 1; i > 0; i--)
        nodes[i] = nodes[i - 1];

      nodes[0] = last;
    } else {
      LoxNode first = nodes[0];

      for (int i = 0; i < RingEntity.SLOTS - 1; i++)
        nodes[i] = nodes[i + 1];

      nodes[RingEntity.SLOTS - 1] = first;
    }

    for (int slot = 0; slot < RingEntity.SLOTS; slot++) {
      if (nodes[slot] != null)
        ((RingEntity) nodes[slot]).setRingSlot(slot);
    }

    complete = true;
    return true;
  }
}