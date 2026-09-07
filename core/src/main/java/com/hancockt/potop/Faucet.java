package com.hancockt.potop;

public class Faucet {
    private float x;
    private float y;
    private float width;
    private float height;
    private boolean turnedOff;
    private float speedReduction;
    private float progress;
    private boolean winFaucet;
    private float rotation = 0f;

    public Faucet(float x, float y, float width, float height, float speedReduction, boolean winFaucet){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
}
