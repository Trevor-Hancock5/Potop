package com.hancockt.potop.features;

public class Checkpoint {
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private boolean activated;
    private final int order;
    private static final float defaultWidth = 40f;
    private static final float defaultHeight = 100f;

    public Checkpoint(float x, float y, float width, float height, boolean activated, int order){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.activated = activated;
        this.order = order;
    }

    public Checkpoint(float x, float y, float width, float height, int order){
        this(x, y, width, height, false, order);
    }

    public Checkpoint(float x, float y, int order){
        this(x, y, defaultWidth, defaultHeight, false, order); //default width and height
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isActivated() {
        return activated;
    }

    public int getOrder() {
        return order;
    }

    public void setActivated (boolean activated) {
        this.activated = activated;
    }
}
