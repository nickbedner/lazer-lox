public class LaserBasic implements LoxNode {
    private int ring;
    private int pos;
    private int[] posAbsolute;
    private int type;
    private boolean blocked;

    public LaserBasic() {
        this.ring = 1;
        this.pos = 0;
        this.posAbsolute = new int[]{0, 0};
        this.type = 1;
        blocked = false;
    }

    public LaserBasic(int ring, int pos, int[] posAbsolute, int type) {
        if (ring < 1 || ring > 3) {
            throw new IllegalArgumentException("There are 3 Player Rings, so the ring must be between 1 and 3.");
        } else{
            this.ring = ring;
        }
        if (pos < 0 || pos > 11) {
            throw new IllegalArgumentException("There are 12 slots in a ring, so the position must be between 0 and 11.");
        } else {
            this.pos = pos;
        }
        this.posAbsolute = posAbsolute;
        if (type < 1 || type > 3) {
            throw new IllegalArgumentException("Lasers are of types 1, 2, or 3 only.");
        } else {
            this.type = type;
        }
        blocked = false;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public int GetRing() {
        return ring;
    }

    @Override
    public int GetPos() {
        return pos;
    }

    @Override
    public int[] GetPosAbsolute() {
        return posAbsolute;
    }

    @Override
    public int GetType() {
        return type;
    }

    public boolean isBlocked() {
        return blocked;
    }
}
