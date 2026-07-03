package com.hancockt.lol;

public class Platform {
    int x,y,width,height;
    boolean isBoundaryWall = false; //so can't wall jump boundary walls

    public Platform(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Platform(float x, float y, float width, float height) {
        this((int) x, (int) y, (int) width, (int) height);
    }
}
