package com.hancockt.potop.features;

public class Platform {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private boolean isBoundaryWall = false; //so can't wall jump boundary walls

    public Platform(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Platform(float x, float y, float width, float height) {
        this((int) x, (int) y, (int) width, (int) height);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isBoundaryWall() {
        return isBoundaryWall;
    }

    public void setBoundaryWall(boolean boundaryWall) {
        isBoundaryWall = boundaryWall;
    }
}
