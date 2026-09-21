package com.hancockt.potop.features;

import com.badlogic.gdx.math.Rectangle;

public class Faucet {
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private boolean turnedOff;
    private final float speedReduction;
    private float progress;
    private final boolean winFaucet;
    private float rotation = 0f;
    public static final float defaultDimensions = 25f;

    public Faucet(float x, float y, float speedReduction, boolean winFaucet){
        this.x = x;
        this.y = y;
        this.width = defaultDimensions;
        this.height = defaultDimensions;
        turnedOff = false;
        this.speedReduction = speedReduction;
        progress = 0;
        this.winFaucet = winFaucet;
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

    public boolean isTurnedOff() {
        return turnedOff;
    }

    public float getSpeedReduction() {
        return speedReduction;
    }

    public float getProgress() {
        return progress;
    }

    public boolean isWinFaucet() {
        return winFaucet;
    }

    public float getRotation() {
        return rotation;
    }

    public void setTurnedOff(boolean turnedOff) {
        this.turnedOff = turnedOff;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float top() {
        return y + height;
    }

    public float right(){
        return x + width;
    }

    public Rectangle getRect(){
        return new Rectangle(x, y, width, height);
    }
}
