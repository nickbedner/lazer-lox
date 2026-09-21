public class LaserBasic implements LoxNode {
    private int ring;
    private int pos;
    private int[] posAbsolute;
    private int type;

    //Quick and dirty simple laser.
    public LaserBasic(int ring, int pos){
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
        this.posAbsolute = new int[]{0,0};
        this.type = 1;
    }

    //For the weird Laser Variants if we make any.
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
    }

    //Get the Ring the Laser is in.
    @Override
    public int GetRing() {
        return ring;
    }

    //Get the Ring Position the Laser is in.
    @Override
    public int GetPos() {
        return pos;
    }

    //Get the screen position of this Laser.
    @Override
    public int[] GetPosAbsolute() {
        return posAbsolute;
    }

    //Get what type of Laser this is.
    //1 - Basic Laser
    @Override
    public int GetType() {
        return type;
    }
}
