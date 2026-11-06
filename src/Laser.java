public class Laser extends Blocker {

    private final boolean[] goalRing;

    public Laser(String texture, int ring, boolean ring_on, int pos, boolean[] goalRing) {
        this(texture, ring, ring_on, pos, 1, goalRing);
    }

    public Laser(String texture, int ring, boolean ring_on, int pos, int type, boolean[] goalRing) {

        super(texture, ring, ring_on, pos, type);

        if (type < 1 || type > 3)
            throw new IllegalArgumentException(
                    "Lasers are of types 1, 2, or 3 only.");

        if (goalRing == null || goalRing.length != SLOTS)
            throw new IllegalArgumentException("Invalid goal ring.");

        this.goalRing = goalRing;

        setRingSlot(pos);
    }

    @Override
    public void setRingSlot(int slot) {
        super.setRingSlot(slot);

        int targetSlot = (slot + SLOTS / 2) % SLOTS;

        if (GetType() == 2 && goalRing[slot])
            targetSlot = slot;

        double angle = targetSlot * 2 * Math.PI / SLOTS;
        double radius = RING_SPACING * 4;

        float targetX = (float) (radius * Math.cos(angle));
        float targetY = (float) (radius * Math.sin(angle));

        faceTowards(targetX, targetY);
    }
}