package com.hancockt.lol;

import java.util.List;

public class PlayerData {
    //Player
    public static float x;
    public static float y;
    public static float velocityY;
    public static float velocityX;
    public static boolean onGround = false;
    public static boolean isDead = false;
    public static float score;
    public static float coyoteSeconds; //number of seconds off ground
    public static float wallCoyoteSeconds; //number of seconds off wall
    public static float jumpBuffer; // number of seconds since pressed jump
    public static boolean justLanded = false; //for screen shake
    public static float impactVelocity; //velocity when landing! use for screen shake
    public static boolean touchingWallLeft = false;
    public static boolean touchingWallRight = false;
    public static boolean lastWallLeft = false;
    public static boolean lastWallRight = false;
    public static float wallJumpCooldown = 0f;
    public static Checkpoint currCheckpoint; //current checkpoint player has made it to
    public static float deathWaterLevel; //water level when user hit current checkpoint
    public static boolean infiniteUnlocked = false;
    public static final int playerWidth = 20;
    public static final int playerHeight = 50;

    public static void reset(List<Checkpoint> checkpoints, float waterLevel, LevelType currentLevel){
        float checkpointMinWaterBuffer = 200f; //how far under checkpoint water starts at a minimum!

        if(currCheckpoint != null){
            x = currCheckpoint.getX();
            y = currCheckpoint.getY();
            waterLevel = Math.min(deathWaterLevel, y - checkpointMinWaterBuffer);
        } else {
            x = 0;
            y = 25;
            waterLevel = -50f;
            if(currentLevel == LevelType.TUTORIAL){
                waterLevel = 10f;
            }
            score = 0;

            //reset checkpoints
            if(checkpoints != null) {
                for (Checkpoint c : checkpoints) {
                    c.setActivated(false);
                }
            }
        }
        velocityY = 0;
        velocityX = 0;
        // scale the reset speed with how high the checkpoint/respawn point is
        onGround = false;
        isDead = false;
        coyoteSeconds = 100; //reset
        wallCoyoteSeconds = 100; //reset
        jumpBuffer = 100; //reset
        justLanded = false; //reset
        wallJumpCooldown = 0;
        touchingWallRight = false;
        touchingWallLeft = false;
        lastWallLeft = false;
        lastWallRight = false;
    }
}
