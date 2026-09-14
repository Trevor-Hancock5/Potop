package com.hancockt.potop.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.state.PersistentData;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;
import com.hancockt.potop.systems.*;

public class PhysicsEngine {
    private static final int accelerationX = 1600; //TUNABLE
    private static final int maxSpeedX = 425; //TUNABLE
    private static final float groundFriction = 0.935f; //TUNABLE
    private static final float airFriction = 0.99f; //TUNABLE
    private static final float gravity = 1000f; //TUNABLE
    private static final float fallGravityMultiplier = 1.6f; //TUNABLE
    public static final int maxFallSpeed = -1200; //TUNABLE
    private static final float jumpForce = 350f; //TUNABLE
    private static final float jumpHoldForce = 350f; //TUNABLE
    private static final float coyoteThreshold = 0.1f; //Can be off platform for x seconds and still jump - TUNABLE
    private static final float jumpBufferThreshold = 0.1f; //TUNABLE
    private static final float downforceFallAcceleration = -300f; //TUNABLE
    private static final float collisionThreshold = 5f; //number of pixels into platform and still pop out one side - TUNABLE
    private static final float nudgeSpeed = 50f; //TUNABLE
    private static final float nudgeThreshold = 5f; //TUNABLE
    private static final float airControlDivisor = 6f; //TUNABLE
    private static final float collisionPlayerHeightDivisor = 10f; //percent of player height needs to be above a platform to land on top - TUNABLE

    private static final float wallSlideMaxSpeed = -115f; //TUNABLE
    private static final float wallProximity = 5f; //how many pixels count as on wall - TUNABLE
    private static final float wallJumpCooldownThreshold = 0.25f; //seconds until wall jump again - prevents wall scaling - TUNABLE
    private static final float jumpPercentClimbingBoundaryWallUp = 0.5f; //TUNABLE
    private static final float jumpPercentClimbingBoundaryWallSide = 0.15f; //TUNABLE
    private static final float jumpPercentClimbingPlatformUp = 0.8f; //TUNABLE
    private static final float jumpPercentClimbingPlatformSide = 0.1f; //TUNABLE
    private static final float jumpPercentWallLaunchUp = 0.6f; //TUNABLE
    private static final float jumpPercentWallLaunchSide = 1.2f; //TUNABLE
    private static final float jumpPercentWallJumpUp = 1f; //TUNABLE
    private static final float jumpPercentWallJumpSide = .85f; //TUNABLE

    private static final int framesPerSecond = 60;


    /// Returns state of Spark (player) for animating
    public void updatePhysics(float delta, RunData runData, PersistentData persistentData,
                                      PlayerData playerData){
        //update jump buffer
        playerData.jumpBuffer += delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)){
            playerData.receivedInput = true;
            playerData.jumpBuffer = 0; //just tried to jump
        }

        //update previous x,y values to not tunnel through platforms
        float prevX = playerData.x;
        float prevY = playerData.y;

        //update wall jump cooldown
        playerData.wallJumpCooldown += delta;

        //gravity
        float currGravity = playerData.velocityY < 0 ? gravity * fallGravityMultiplier : gravity;
        playerData.velocityY -= currGravity * delta;

        //Holding jump
        if ((Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyPressed(Input.Keys.W)) && !playerData.onGround && playerData.velocityY > 0) {
            playerData.receivedInput = true;
            playerData.velocityY += jumpHoldForce * delta;
        }

        //Or holding down
        float currFriction = groundFriction;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)){
            playerData.receivedInput = true;
            if(!playerData.onGround) {
                playerData.velocityY += downforceFallAcceleration * delta;
            } else{
                currFriction = (float) Math.pow(groundFriction, 2);
            }
        }

        //falling cap speed
        playerData.velocityY = Math.max(maxFallSpeed, playerData.velocityY);

        //update x and y
        playerData.y += playerData.velocityY * delta;
        playerData.x += playerData.velocityX * delta;

        //Edge Nudging
        for(Platform p : runData.getPlatforms()) {
            if (playerData.y + PlayerData.playerHeight > p.getY() && playerData.velocityY > 0 && playerData.y < p.getY() + p.getHeight()){
                if(playerData.x + PlayerData.playerWidth > p.getX() && playerData.x + PlayerData.playerWidth < p.getX() + nudgeThreshold){
                    playerData.velocityX += delta * nudgeSpeed;
                }

                if(playerData.x < p.getX() + p.getWidth() && playerData.x > p.getX() + p.getWidth() - nudgeThreshold){
                    playerData.velocityX -= delta * nudgeSpeed;
                }
            }
        }

        //Reset to avoid jumping midair
        playerData.onGround = false;

        //floor collision
        if (playerData.y <= 0) {
            playerData.impactVelocity = Math.abs(playerData.velocityY);
            playerData.onGround = true;
            playerData.y = 0;
            playerData.velocityY = 0;
            playerData.justLanded = true;
            playerData.lastWallRight = false;
            playerData.lastWallLeft = false;
        }

        //platform collision
        for (Platform p : runData.getPlatforms()) {
            Rectangle playerRect = new Rectangle(playerData.x, playerData.y, PlayerData.playerWidth, PlayerData.playerHeight);
            Rectangle platformRect = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());


            //Player lands on top
            float collisionPoint = p.getY();
            if(!p.isBoundaryWall()) {
                if (p.getHeight() > collisionThreshold) {
                    collisionPoint = p.getHeight() + p.getY() - collisionThreshold;
                }
                if (playerRect.overlaps(platformRect) && playerData.velocityY < 0 && prevY + ((float) PlayerData.playerHeight / collisionPlayerHeightDivisor) >= collisionPoint) {
                    playerData.impactVelocity = Math.abs(playerData.velocityY);
                    playerData.y = p.getY() + p.getHeight();
                    playerData.velocityY = 0;
                    playerData.onGround = true;
                    playerData.justLanded = true;
                    playerData.lastWallRight = false;
                    playerData.lastWallLeft = false;
                }
            }

            //left of player hits right side of wall when moving left
            collisionPoint = p.getX() + (float) (p.getWidth() / 2);
            if ((float) p.getWidth() / 2 > collisionThreshold) {
                collisionPoint = p.getX() + p.getWidth() - collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && playerData.velocityX < 0 && prevX + (float) (PlayerData.playerWidth / 2) >= collisionPoint
                    && playerData.y + (PlayerData.playerHeight / collisionPlayerHeightDivisor) < p.getY() + p.getHeight()) {
                playerData.x = p.getX() + p.getWidth();
                playerData.velocityX = -1;
                playerData.touchingWallRight = true;
            }


            //right of player hits left side of wall when moving right
            collisionPoint = p.getX() + (float) (p.getWidth() / 2);
            if ((float) p.getWidth() / 2 > collisionThreshold) {
                collisionPoint = p.getX() + collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && playerData.velocityX > 0 && prevX + (float) (PlayerData.playerWidth / 2) <= collisionPoint
                    && playerData.y + (PlayerData.playerHeight / collisionPlayerHeightDivisor) < p.getY() + p.getHeight()) {
                playerData.x = p.getX() - PlayerData.playerWidth;
                playerData.velocityX = 1;
                playerData.touchingWallLeft = true;
            }

        }

        //check if on wall
        boolean onBoundingWall = false;
        for (Platform p : runData.getPlatforms()){
            if (playerData.y + PlayerData.playerHeight < p.getY() || playerData.y > p.getY() + p.getHeight()) {
                continue; //not same height, skip!
            }

            //left of player, right of wall
            float distToRightWall = playerData.x - (p.getX() + p.getWidth());
            if (distToRightWall >= 0 && distToRightWall < wallProximity && playerData.velocityX <= 0){
                playerData.touchingWallRight = true;
                onBoundingWall = p.isBoundaryWall();
            }

            //right of player, left of wall
            float distToLeftWall = p.getX() - (playerData.x + PlayerData.playerWidth);
            if(distToLeftWall >= 0 && distToLeftWall < wallProximity && playerData.velocityX >= 0){
                playerData.touchingWallLeft = true;
                onBoundingWall = p.isBoundaryWall();
            }
        }

        //wall slide/jump
        boolean onWall = (playerData.touchingWallLeft || playerData.touchingWallRight) && !playerData.onGround;

        //wall slide
        if (onWall && playerData.velocityY < wallSlideMaxSpeed){
            playerData.velocityY = wallSlideMaxSpeed;
        }

        // coyote wall update
        if(onWall){
            playerData.wallCoyoteSeconds = 0;
        } else{
            playerData.wallCoyoteSeconds += delta;
        }

        //wall jump
        if((onWall || playerData.wallCoyoteSeconds < coyoteThreshold)
                && playerData.wallJumpCooldown >= wallJumpCooldownThreshold
                && (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                        || Gdx.input.isKeyJustPressed(Input.Keys.W) || playerData.jumpBuffer < jumpBufferThreshold)
                && !playerData.onGround){

            playerData.receivedInput = true;
            //jump fires, reset cooldown
            playerData.wallJumpCooldown = 0f;

            boolean wasRight = playerData.touchingWallRight || playerData.lastWallRight;
            boolean wasLeft = playerData.touchingWallLeft || playerData.lastWallLeft;

            boolean holdingInto =
                (wasRight && (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)))
                       || (wasLeft && (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)));
            boolean holdingAway = (wasRight && (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)))
                       || (wasLeft && (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)));

            float upForce;
            float sideForce;

            if(holdingInto){
                if(onBoundingWall) {
                    upForce = jumpForce * jumpPercentClimbingBoundaryWallUp;
                    sideForce = jumpForce * jumpPercentClimbingBoundaryWallSide;
                } else{
                    upForce = jumpForce * jumpPercentClimbingPlatformUp;
                    sideForce = jumpForce * jumpPercentClimbingPlatformSide;
                }
            } else if (holdingAway){
                upForce = jumpForce * jumpPercentWallLaunchUp;
                sideForce = jumpForce * jumpPercentWallLaunchSide;
            } else{
                //not into or away
                upForce = jumpForce * jumpPercentWallJumpUp;
                sideForce = jumpForce * jumpPercentWallJumpSide;
            }

            playerData.velocityY = upForce;

            //guaranteed either touching wall on right or left
            playerData.velocityX = wasRight ? sideForce : -sideForce;
            playerData.touchingWallRight = false;
            playerData.touchingWallLeft = false;
            playerData.lastWallRight = false;
            playerData.lastWallLeft = false;
            playerData.wallCoyoteSeconds = 100;
            playerData.coyoteSeconds = 100;
            playerData.jumpBuffer = 100;
            playerData.justLanded = false;
            playerData.onGround = false;
        }

        //Coyote timer
        if(!playerData.onGround){
            playerData.coyoteSeconds += delta;
        } else{
            playerData.coyoteSeconds = 0;
        }

        //horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)){
            playerData.receivedInput = true;
            if(playerData.onGround) {
                playerData.velocityX -= accelerationX * delta * playerData.dangerBoost;
            } else{
                playerData.velocityX -= (accelerationX * delta * playerData.dangerBoost) / airControlDivisor;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerData.receivedInput = true;
            if(playerData.onGround) {
                playerData.velocityX += accelerationX * delta * playerData.dangerBoost;
            } else{
                playerData.velocityX += (accelerationX * delta * playerData.dangerBoost) / airControlDivisor;
            }
        }

        //friction & velocity cap
        float frameGroundFriction = (float) Math.pow(currFriction, delta * framesPerSecond);
        float frameAirFriction = (float) Math.pow(airFriction, delta * framesPerSecond);
        playerData.velocityX *= playerData.onGround ? frameGroundFriction : frameAirFriction;
        playerData.velocityX = Math.clamp(playerData.velocityX, -maxSpeedX, maxSpeedX);

        //Jumping
        if ((Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                    || Gdx.input.isKeyJustPressed(Input.Keys.W)|| playerData.jumpBuffer < jumpBufferThreshold)
                    && (playerData.onGround || playerData.coyoteSeconds <= coyoteThreshold)) {
            playerData.receivedInput = true;
            playerData.coyoteSeconds = 100; //reset so can't double jump
            playerData.jumpBuffer = 100; //reset so can't double jump!
            playerData.velocityY = jumpForce * playerData.dangerBoost;
            playerData.onGround = false;
            playerData.justLanded = false;

            // Form of dash when you jump
            float speedRatio = Math.abs(playerData.velocityX) / maxSpeedX; // 0 to 1
            playerData.velocityX *= 1f + (speedRatio * 0.5f); // max of 1.5
        }

        // reset flags for wall slide/jump
        if(playerData.touchingWallLeft) {
            playerData.lastWallLeft = true;
        }
        if(playerData.touchingWallRight) {
            playerData.lastWallRight = true;
        }

        playerData.touchingWallLeft = false;
        playerData.touchingWallRight = false;

        //update score LAST
        persistentData.highScore = Math.max(persistentData.highScore, playerData.y);

        runData.time += delta;
    }
}
