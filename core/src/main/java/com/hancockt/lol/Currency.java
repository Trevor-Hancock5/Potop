package com.hancockt.lol;

public class Currency {
    private float x;
    private float y;
    private boolean prestige;
    private float explodeVelocityX;
    private float explodeVelocityY;
    private float explodeTime;

    public final static float ABSORB_VELOCITY = 300f;
    public final static float WIDTH = 16;
    public final static float HEIGHT = 16;

    public Currency(float x, float y, boolean prestige, float explodeVelocityX, float explodeVelocityY,
                        float explodeTime){
        this.x = x;
        this.y = y;
        this.prestige = prestige;
        this.explodeVelocityX = explodeVelocityX;
        this.explodeVelocityY = explodeVelocityY;
        this.explodeTime = explodeTime;
    }

    public Currency(float x, float y, boolean prestige){
        this(x, y, prestige, 0,0,0);
    }

    public Currency(float x, float y){
        this(x, y, false, 0,0,0); //default is not prestige currency
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float change){
        x += change;
    }

    public void setY(float change){
        y += change;
    }

    public boolean isPrestige() {
        return prestige;
    }

    public float getExplodeTime(){
        return explodeTime;
    }

    public void updateExplodeTime(float delta){
        explodeTime -= delta;
        if(explodeTime == 0){
            // Set to be under 0, so we know this match is being absorbed
            explodeTime -= 1;
        }
    }

    public float getExplodeVelocityX(){
        return explodeVelocityX;
    }

    public float getExplodeVelocityY(){
        return explodeVelocityY;
    }
}
