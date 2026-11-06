package game;

import graphics.Sprite;
import java.awt.Graphics2D;

public class Blocker extends RingEntity implements LoxNode {
    private final Sprite sprite;
    private boolean ring_on;
    private final int type;

    public Blocker(Sprite sprite, int ring, boolean ring_on, int slot, int type) {
        super(ring, slot, 1.0f);
        if (ring > 3)
            throw new IllegalArgumentException("Invalid player ring.");
        if (type < 1 || type > 6)
            throw new IllegalArgumentException("Invalid node type.");

        this.sprite = sprite;
        this.ring_on = ring_on;
        this.type = type;
        sprite.setOpacity(ring_on ? 1.0f : 0.5f);
    }

    public Sprite getSprite() {
        return sprite;
    }

    @Override
    public void render(Graphics2D g) {
        Graphics2D local = (Graphics2D) g.create();
        try {
            local.translate(getX(), getY());
            local.rotate(getRotationRadians());
            sprite.render(local);
        } finally {
            local.dispose();
        }
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
        sprite.setOpacity(ring_on ? 1.0f : 0.5f);
    }
}
