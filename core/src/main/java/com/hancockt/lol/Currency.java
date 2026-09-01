package com.hancockt.lol;

public class Currency {
    private float x;
    private float y;
    private boolean prestige;
    public final static float WIDTH = 16;
    public final static float HEIGHT = 16;

    public Currency(float x, float y, boolean prestige){
        this.x = x;
        this.y = y;
        this.prestige = prestige;
    }

    public Currency(float x, float y){
        this(x, y, false); //default is not prestige currency
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isPrestige() {
        return prestige;
    }
}
