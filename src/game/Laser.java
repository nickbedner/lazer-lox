package game;

import graphics.Sprite;

public class Laser extends Blocker {

    private final boolean[] goalRing;

    public Laser(Sprite sprite, int ring, boolean ring_on, int pos, boolean[] goalRing) {
        this(sprite, ring, ring_on, pos, 1, goalRing);
    }

    public Laser(Sprite sprite, int ring, boolean ring_on, int pos, int type, boolean[] goalRing) {
        super(sprite, ring, ring_on, pos, type);

        if (type < 1 || type > 3)
            throw new IllegalArgumentException("Lasers are of types 1, 2, or 3 only.");

        if (goalRing == null || goalRing.length != SLOTS)
            throw new IllegalArgumentException("Invalid goal ring.");

        this.goalRing = goalRing;

        setRingSlot(pos);
    }

    public double getFacingRadiansForSlot(int slot) {
        if (slot < 0 || slot >= SLOTS)
            throw new IllegalArgumentException("Invalid slot.");

        int targetSlot = (slot + SLOTS / 2) % SLOTS;

        if (GetType() == 2 && goalRing[slot])
            targetSlot = slot;

        double sourceAngle = slot * 2.0 * Math.PI / SLOTS;
        double targetAngle = targetSlot * 2.0 * Math.PI / SLOTS;

        double sourceRadius = getRing() * RING_SPACING;
        double targetRadius = 4.0 * RING_SPACING;

        double sourceX = sourceRadius * Math.cos(sourceAngle);
        double sourceY = sourceRadius * Math.sin(sourceAngle);

        double targetX = targetRadius * Math.cos(targetAngle);
        double targetY = targetRadius * Math.sin(targetAngle);

        return Math.atan2(targetY - sourceY, targetX - sourceX) - Math.PI / 2;
    }

    @Override
    public void setRingSlot(int slot) {
        super.setRingSlot(slot);
        setRotationRadians(getFacingRadiansForSlot(slot));
    }
}