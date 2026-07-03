package com.hancockt.lol;

public class Checkpoint {
    float x,y, width,height;
    boolean activated;
    int order;

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
        this(x, y, 25, 75, order);
    }
}
