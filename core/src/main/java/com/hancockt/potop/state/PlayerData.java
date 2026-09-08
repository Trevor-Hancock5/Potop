package com.hancockt.potop.state;

import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;

import java.util.List;

public class PlayerData {
    // Everything belonging to the current state of the game in relation to the player
    // everything about the player this run: x, y, physics, etc.

    public float x;
    public float y;
    public float velocityY;
    public float velocityX;
    public float impactVelocity; //velocity when landing! use for screen shake

    public boolean onGround = false;
    public boolean isDead = false;
    public boolean justLanded = false; //for screen shake
    public boolean touchingWallLeft = false;
    public boolean touchingWallRight = false;
    public boolean lastWallLeft = false;
    public boolean lastWallRight = false;

    public float coyoteSeconds; //number of seconds off ground
    public float wallCoyoteSeconds; //number of seconds off wall
    public float jumpBuffer; // number of seconds since pressed jump
    public float wallJumpCooldown = 0f;

    public final int playerWidth = 20;
    public final int playerHeight = 50;

    public float reset(List<Checkpoint> checkpoints, LevelType currentLevel){
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
