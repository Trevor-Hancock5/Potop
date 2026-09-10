package com.hancockt.potop.state;

import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.systems.WaterSystem;

import java.util.List;

public class PlayerData {
    // Everything belonging to the current state of the game in relation to the player
    // everything about the player this run: x, y, physics, etc.

    public float x;
    public float y;
    public float velocityY;
    public float velocityX;
    public float impactVelocity; //velocity when landing! use for screen shake
    public boolean receivedInput; //Don't update water until player presses movement keys

    public boolean onGround = false;
    public boolean justLanded = false; //for screen shake
    public boolean touchingWallLeft = false;
    public boolean touchingWallRight = false;
    public boolean lastWallLeft = false;
    public boolean lastWallRight = false;

    public float coyoteSeconds; //number of seconds off ground
    public float wallCoyoteSeconds; //number of seconds off wall
    public float jumpBuffer; // number of seconds since pressed jump
    public float wallJumpCooldown = 0f;

    public static final int playerWidth = 20; //TUNABLE
    public static final int playerHeight = 50; //TUNABLE

    public float reset(LevelType currentLevel){
        float waterLevel;
        RunData data = LevelGeneration.getAllLevels().get(currentLevel);
        receivedInput = false;

        if(data.currCheckpoint != null){
            x = data.currCheckpoint.getX();
            y = data.currCheckpoint.getY();
            waterLevel = Math.min(data.checkpointWaterLevel, y - RunData.checkpointMinWaterBuffer);
        } else {
            x = data.getStartPoint().x;
            y = data.getStartPoint().y;
            waterLevel = WaterSystem.defaultWaterLevelOnStart;
        }
        velocityY = 0;
        velocityX = 0;
        // scale the reset speed with how high the checkpoint/respawn point is
        onGround = false;
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
