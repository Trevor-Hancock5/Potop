package com.hancockt.potop.state;

import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.systems.WaterSystem;

public class PlayerData {
    // Everything belonging to the current state of the game in relation to the player
    // everything about the player this run: x, y, physics, etc.

    public float x;
    public float y;
    public float velocityY;
    public float velocityX;
    public float impactVelocity; //velocity when landing! use for screen shake
    public boolean receivedInput; //Don't update water until player presses movement keys
    public float dangerBoost;

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
    private static final float dangerBoostDistance = 400f; // TUNABLE
    private static final float maxDangerBoost = 1.25f; //TUNABLE

    public Rectangle playerRect;
    public float sparkHeadX;
    public float sparkHeadY;

    public void update(){
        playerRect = new Rectangle(x, y, playerWidth, playerHeight);
        sparkHeadX = x + ((float) playerWidth / 2);
        sparkHeadY = y + playerHeight - 15;
    }

    public void calculateDangerBoost(WaterSystem waterSystem){
        float distanceFromWater = y - waterSystem.getWaterLevel();
        float dangerBoost = Math.max(0, 1 - (distanceFromWater / dangerBoostDistance));
        this.dangerBoost = 1 + dangerBoost * (maxDangerBoost - 1);
    }

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
