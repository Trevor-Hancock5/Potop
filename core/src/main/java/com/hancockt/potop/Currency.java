package com.hancockt.potop;

public class Currency {
    private float x;
    private float y;
    private boolean prestige;
    private float velocityX;
    private float velocityY;
    private float explodeTime;
    private float absorbTime;

    public final static float ABSORB_ACCELERATION = 3000f;
    private final static float MAX_ABSORB_VELOCITY = 800f;
    public final static float WIDTH = 16;
    public final static float HEIGHT = 16;
    public final static float MAX_ABSORB_TIME = 0.2f;

    public Currency(float x, float y, boolean prestige, float velocityX, float velocityY,
                        float explodeTime){
        this.x = x;
        this.y = y;
        this.prestige = prestige;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
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

    public void moveX(float change){
        x += change;
    }

    public void moveY(float change){
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

    public float getAbsorbTime(){
        return absorbTime;
    }

    public void updateAbsorbTime(float delta){
        absorbTime += delta;
    }

    public float getVelocityX(){
        return velocityX;
    }

    public float getVelocityY(){
        return velocityY;
    }

    public void changeVelocityX(float change){
        velocityX += velocityX + change < MAX_ABSORB_VELOCITY ? change : 0;
    }

    public void changeVelocityY(float change){
        velocityY += velocityY + change < MAX_ABSORB_VELOCITY ? change : 0;
    }

    public void setVelocityX(float velocityX){
        this.velocityX = velocityX;
    }

    public void setVelocityY(float velocityY){
        this.velocityY = velocityY;
    }
}
