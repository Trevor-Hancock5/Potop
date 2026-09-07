package com.hancockt.potop.state;

import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.level.LevelData;
import com.hancockt.potop.level.LevelLibrary;
import com.hancockt.potop.level.LevelType;

import java.util.List;

public class PlayerStateOutdated {
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
    public static float checkpointWaterLevel; //water level when user hit current checkpoint
    public static boolean infiniteUnlocked = false;
    public static int numMatches = 0;
    public static int numFirewood = 0;
    public static final int playerWidth = 20;
    public static final int playerHeight = 50;

    //how far under checkpoint water starts at a minimum!
    private static final float checkpointMinWaterBuffer = 200f;

    public static float reset(List<Checkpoint> checkpoints, LevelType currentLevel){
        float waterLevel;
        LevelData data = LevelLibrary.getAllLevels().get(currentLevel);

        if(currCheckpoint != null){
            x = currCheckpoint.getX();
            y = currCheckpoint.getY();
            waterLevel = Math.min(checkpointWaterLevel, y - checkpointMinWaterBuffer);
        } else {
            x = data.getStartPoint().x;
            y = data.getStartPoint().y;
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
        return waterLevel;
    }
}
