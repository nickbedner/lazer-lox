
public class LaserBasic extends Entity implements LoxNode {

    private int ring;
    private boolean ring_on;
    private int pos;
    private int[] posAbsolute;
    private int type;

    // Quick and dirty simple laser.
    public LaserBasic(int ring, boolean ring_on, int pos) {
        this(ring, ring_on, pos, new int[] { 0, 0 }, 1);
    }

    // For the weird Laser Variants if we make any.
    public LaserBasic(int ring, boolean ring_on, int pos,
            int[] posAbsolute, int type) {

        // Initialize the parent Entity.
        // UUID is generated automatically by Entity.
        super("lasperPointer.png", 0, 0, 0);

        if (ring < 1 || ring > 3) {
            throw new IllegalArgumentException(
                    "There are 3 Player Rings, so the ring must be between 1 and 3.");
        }

        if (pos < 0 || pos > 11) {
            throw new IllegalArgumentException(
                    "There are 12 slots in a ring, so the position must be between 0 and 11.");
        }

        if (type < 1 || type > 3) {
            throw new IllegalArgumentException(
                    "Lasers are of types 1, 2, or 3 only.");
        }

        this.ring = ring;
        this.ring_on = ring_on;
        this.pos = pos;
        this.type = type;

        SetAbsolutePosition(posAbsolute);
    }

    @Override
    public void Toggle() {
        ring_on = !ring_on;
    }

    @Override
    public int GetRing() {
        return ring;
    }

    @Override
    public boolean IsOn() {
        return ring_on;
    }

    @Override
    public int GetPos() {
        return pos;
    }

    @Override
    public int[] GetPosAbsolute() {
        return posAbsolute.clone();
    }

    @Override
    public int GetType() {
        return type;
    }

    @Override
    public void SetAbsolutePosition(int[] newPosition) {
        if (newPosition == null || newPosition.length != 2) {
            throw new IllegalArgumentException(
                    "Position must contain exactly two coordinates.");
        }

        this.posAbsolute = newPosition.clone();

        // Keep Entity's coordinates synchronized.
        setX(newPosition[0]);
        setY(newPosition[1]);
    }
}
