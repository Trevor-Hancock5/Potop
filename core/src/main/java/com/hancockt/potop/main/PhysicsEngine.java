package com.hancockt.potop.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.assets.AnimationKey;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.state.GameState;
import com.hancockt.potop.state.PersistentData;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;
import com.hancockt.potop.systems.*;

public class PhysicsEngine {
    private final int accelerationX = 1600;
    private final int maxSpeedX = 425;
    private final float groundFriction = 0.935f;
    private final float airFriction = 0.99f;
    private final float gravity = 1000f;
    private final float fallGravityMultiplier = 1.6f;
    public final int maxFallSpeed = -1200;
    private final float jumpForce = 350f;
    private final float jumpHoldForce = 350f;
    private final float coyoteThreshold = 0.1f; //Can be off platform for x seconds and still jump
    private final float jumpBufferThreshold = 0.1f;
    private final float downforceFallAcceleration = -300f;
    private final float collisionThreshold = 5f; //number of pixels into platform and still pop out one side
    private final float nudgeSpeed = 50f;
    private final float nudgeThreshold = 5f;
    private final float airControlDivisor = 6f;
    private final float wallSlideMaxSpeed = -115f;
    private final float wallProximity = 5f; //how many pixels count as on wall
    private final float wallJumpCooldownThreshold = 0.25f; //seconds until wall jump again - prevents wall scaling
    private final float collisionPlayerHeightDivisor = 10f; //What amount of player height needs to be above a platform to land on top

    /// Returns state of Spark (player) for animating
    // TODO - remove all system classes and gameflow calls out of here, this should be JUST physics
    public AnimationKey updatePhysics(float delta, RunData runData, PersistentData persistentData, PlayerData playerData,
                                      CameraSystem cameraSystem,
                                      LevelSystem levelSystem, WaterSystem waterSystem,
                                      FaucetSystem faucetSystem, CurrencySystem currencySystem,
                                      CheckpointSystem checkpointSystem, LevelGeneration levelGeneration){
        //initialize animationKey variable to return - default to idle
        AnimationKey currAnimation = AnimationKey.IDLE;

        //update jump buffer
        playerData.jumpBuffer += delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)){
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
            playerData.velocityY += jumpHoldForce * delta;
        }

        //Or holding down
        float currFriction = groundFriction;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)){
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
            if (playerData.y + playerData.playerHeight > p.getY() && playerData.velocityY > 0 && playerData.y < p.getY() + p.getHeight()){
                if(playerData.x + playerData.playerWidth > p.getX() && playerData.x + playerData.playerWidth < p.getX() + nudgeThreshold){
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
            Rectangle playerRect = new Rectangle(playerData.x, playerData.y, playerData.playerWidth, playerData.playerHeight);
            Rectangle platformRect = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());


            //Player lands on top
            float collisionPoint = p.getY();
            if(!p.isBoundaryWall()) {
                if (p.getHeight() > collisionThreshold) {
                    collisionPoint = p.getHeight() + p.getY() - collisionThreshold;
                }
                if (playerRect.overlaps(platformRect) && playerData.velocityY < 0 && prevY + ((float) playerData.playerHeight / collisionPlayerHeightDivisor) >= collisionPoint) {
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
            if (playerRect.overlaps(platformRect) && playerData.velocityX < 0 && prevX + (float) (playerData.playerWidth / 2) >= collisionPoint
                    && playerData.y + (playerData.playerHeight / collisionPlayerHeightDivisor) < p.getY() + p.getHeight()) {
                playerData.x = p.getX() + p.getWidth();
                playerData.velocityX = -1;
                playerData.touchingWallRight = true;
            }


            //right of player hits left side of wall when moving right
            collisionPoint = p.getX() + (float) (p.getWidth() / 2);
            if ((float) p.getWidth() / 2 > collisionThreshold) {
                collisionPoint = p.getX() + collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && playerData.velocityX > 0 && prevX + (float) (playerData.playerWidth / 2) <= collisionPoint
                    && playerData.y + (playerData.playerHeight / collisionPlayerHeightDivisor) < p.getY() + p.getHeight()) {
                playerData.x = p.getX() - playerData.playerWidth;
                playerData.velocityX = 1;
                playerData.touchingWallLeft = true;
            }

        }

        //check if on wall
        boolean onBoundingWall = false;
        for (Platform p : runData.getPlatforms()){
            if (playerData.y + playerData.playerHeight < p.getY() || playerData.y > p.getY() + p.getHeight()) {
                continue; //not same height, skip!
            }

            //left of player, right of wall
            float distToRightWall = playerData.x - (p.getX() + p.getWidth());
            if (distToRightWall >= 0 && distToRightWall < wallProximity && playerData.velocityX <= 0){
                playerData.touchingWallRight = true;
                onBoundingWall = p.isBoundaryWall();
            }

            //right of player, left of wall
            float distToLeftWall = p.getX() - (playerData.x + playerData.playerWidth);
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
                //TODO when moved into platform system, add constants for these
                if(onBoundingWall) {
                    upForce = jumpForce * .5f;
                    sideForce = jumpForce * .15f;
                } else{
                    upForce = jumpForce * .8f;
                    sideForce = jumpForce * .1f;
                }
            } else if (holdingAway){
                upForce = jumpForce * .6f;
                sideForce = jumpForce * 1.2f;
            } else{
                //not into or away
                upForce = jumpForce;
                sideForce = .85f * jumpForce;
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
            cameraSystem.resetScreenShake();
            playerData.onGround = false;
        }

        //Coyote timer
        if(!playerData.onGround){
            playerData.coyoteSeconds += delta;
        } else{
            playerData.coyoteSeconds = 0;
        }

        float dangerBoost = waterSystem.calculateDangerBoost(playerData);

        //horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)){
            if(playerData.onGround) {
                playerData.velocityX -= accelerationX * delta * dangerBoost;
            } else{
                playerData.velocityX -= (accelerationX * delta * dangerBoost) / airControlDivisor;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            if(playerData.onGround) {
                playerData.velocityX += accelerationX * delta * dangerBoost;
            } else{
                playerData.velocityX += (accelerationX * delta * dangerBoost) / airControlDivisor;
            }
        }

        //friction & velocity cap
        float frameGroundFriction = (float) Math.pow(currFriction, delta * 60);
        float frameAirFriction = (float) Math.pow(airFriction, delta * 60);
        playerData.velocityX *= playerData.onGround ? frameGroundFriction : frameAirFriction;
        playerData.velocityX = Math.clamp(playerData.velocityX, -maxSpeedX, maxSpeedX);

        //Jumping
        if ((Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                    || Gdx.input.isKeyJustPressed(Input.Keys.W)|| playerData.jumpBuffer < jumpBufferThreshold)
                    && (playerData.onGround || playerData.coyoteSeconds <= coyoteThreshold)) {
            playerData.coyoteSeconds = 100; //reset so can't double jump
            playerData.jumpBuffer = 100; //reset so can't double jump!
            playerData.velocityY = jumpForce * dangerBoost;
            playerData.onGround = false;
            playerData.justLanded = false;
            cameraSystem.resetScreenShake();

            // Form of dash when you jump
            float speedRatio = Math.abs(playerData.velocityX) / maxSpeedX; // 0 to 1
            playerData.velocityX *= 1f + (speedRatio * 0.5f); // max of 1.5
        }

        waterSystem.updateWater(runData, delta);

        Rectangle playerRectFinal = new Rectangle(playerData.x, playerData.y, playerData.playerWidth, playerData.playerHeight);
        checkpointSystem.update(runData, playerRectFinal, waterSystem, currencySystem);

        // Define spark's head for future operations
        float sparkHeadX = playerData.x + ((float) playerData.playerWidth / 2);
        float sparkHeadY = playerData.y + playerData.playerHeight - 15; // TODO - define as constant somewhere!

        currencySystem.update(runData, persistentData, playerData, delta, sparkHeadX, sparkHeadY, playerRectFinal);

        // Update currency first before faucets in case it's a win faucet which changes to win state!
        faucetSystem.update(runData, delta, playerRectFinal, waterSystem, currencySystem);

        //death check
        Rectangle waterRect = new Rectangle(-50000, 0, 100000, waterSystem.getWaterLevel());

        //Only dead if head hits water (because flame Afro!), so top 15 pixels of player rect
        if(waterRect.overlaps(playerRectFinal) && waterSystem.getWaterLevel() >= sparkHeadY){
            if(!playerData.isDead){
                runData.deathXValues.add(playerData.x);
            }
            playerData.isDead = true;
            GameFlow.GAME_STATE = GameState.DEATH; //TODO - just use game state instead of isDead and other booleans?
            faucetSystem.reset(runData);
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

        //update infinite level if needed
        levelSystem.updateInfinite(runData, playerData, levelGeneration);

        //update animation from least to most important (to choose which animation is shown - default is idle!)
        //todo -define constant
        final float showRunAnimation = 50f;
        final float showFallAnimation = -100f;
        if(playerData.velocityX < -showRunAnimation){
            currAnimation = AnimationKey.RUN_LEFT;
        } else if(playerData.velocityX > showRunAnimation){
            currAnimation = AnimationKey.RUN_RIGHT;
        }

        if(playerData.velocityY < showFallAnimation){
            currAnimation = AnimationKey.FALL;
        } else if(playerData.velocityY > 0){
            currAnimation = AnimationKey.JUMP;
        }

        if(playerData.isDead){
            currAnimation = AnimationKey.DEATH;
        }

        return currAnimation;
    }


}
