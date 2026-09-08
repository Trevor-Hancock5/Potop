package com.hancockt.potop.state;

import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;

import java.util.List;

public class PlayerData {
    // Everything belonging to the current state of the game in relation to the player
    // everything about the player this run: x, y, physics, etc.

    public static float x;
    public static float y;
    public static float velocityY;
    public static float velocityX;
    public static float impactVelocity; //velocity when landing! use for screen shake

    public static boolean onGround = false;
    public static boolean isDead = false;
    public static boolean justLanded = false; //for screen shake
    public static boolean touchingWallLeft = false;
    public static boolean touchingWallRight = false;
    public static boolean lastWallLeft = false;
    public static boolean lastWallRight = false;

    public static float coyoteSeconds; //number of seconds off ground
    public static float wallCoyoteSeconds; //number of seconds off wall
    public static float jumpBuffer; // number of seconds since pressed jump
    public static float wallJumpCooldown = 0f;

    public static final int playerWidth = 20;
    public static final int playerHeight = 50;

    public static float reset(List<Checkpoint> checkpoints, LevelType currentLevel){
        float waterLevel;
        RunData data = LevelGeneration.getAllLevels().get(currentLevel);

        if(data.currCheckpoint != null){
            x = data.currCheckpoint.getX();
            y = data.currCheckpoint.getY();
            waterLevel = Math.min(data.checkpointWaterLevel, y - RunData.checkpointMinWaterBuffer);
        } else {
            x = data.getStartPoint().x;
            y = data.getStartPoint().y;
            waterLevel = -50f;
            if(currentLevel == LevelType.LEARNING_MODE){
                waterLevel = 10f;
            }

            //reset checkpoints, todo - why?
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
