package com.hancockt.lol;

public class Faucet {
    float x,y,width,height;
    boolean turnedOff;
    float speedReduction;
    float progress;
    boolean winFaucet;
    float rotation = 0f;

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
}
