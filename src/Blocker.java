public class Blocker extends RingEntity implements LoxNode {

    private boolean ring_on;
    private int type;

    public Blocker(String texture, int ring,
            boolean ring_on, int slot, int type) {

        super(texture, ring, slot, 1.0f);

        if (ring > 3)
            throw new IllegalArgumentException("Invalid player ring.");

        if (type < 1 || type > 6)
            throw new IllegalArgumentException("Invalid node type.");

        this.ring_on = ring_on;
        this.type = type;
    }

    @Override
    public int GetRing() {
        return getRing();
    }

    @Override
    public int GetPos() {
        return getSlot();
    }

    @Override
    public int GetType() {
        return type;
    }

    @Override
    public boolean IsOn() {
        return ring_on;
    }

    @Override
    public void Toggle() {
        ring_on = !ring_on;
    }

    @Override
    protected float getRenderOpacity() {
        return ring_on ? getOpacity() : getOpacity() * 0.5f;
    }
}
