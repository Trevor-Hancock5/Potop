package com.hancockt.potop;

public class Checkpoint {
    private float x;
    private float y;
    private float width;
    private float height;
    private boolean activated;
    private int order;

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
        this(x, y, 40, 100, false, order); //default width and height
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
