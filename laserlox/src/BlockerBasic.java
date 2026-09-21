public class BlockerBasic implements LoxNode {
    private int ring;
    private int pos;
    private int[] posAbsolute;
    private int type;

    public BlockerBasic() {
        this.ring = 1;
        this.pos = 0;
        this.posAbsolute = new int[]{0, 0};
        this.type = 4;
    }

    public BlockerBasic(int ring, int pos, int[] posAbsolute) {
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
        if (type < 4 || type > 6) {
            throw new IllegalArgumentException("Blockers are of types 4, 5, or 6 only.");
        } else {
            this.type = type;
        }
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

}
