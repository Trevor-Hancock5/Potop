package com.hancockt.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.hancockt.lol.PlayerData.*;
import static com.hancockt.lol.GameScreen.*;

public class PhysicsEngine {
    private static final int accelerationX = 1600;
    private static final int maxSpeedX = 425;
    private static final float groundFriction = 0.935f;
    private static final float airFriction = 0.99f;
    private static final float gravity = 1000f;
    private static final float fallGravityMultiplier = 1.6f;
    private static final float jumpForce = 350f;
    private static final float jumpHoldForce = 350f;
    private static final float coyoteThreshold = 0.1f; //Can be off platform for x seconds and still jump
    private static final float jumpBufferThreshold = 0.1f;
    private static final float downforceFallAcceleration = -300f;
    private static final float collisionThreshold = 5f; //number of pixels into platform and still pop out one side
    private static final float nudgeSpeed = 50f;
    private static final float nudgeThreshold = 5f;
    private static final float airControlDivisor = 6f;
    private static final float wallSlideMaxSpeed = -115f;
    private static final float wallProximity = 5f; //how many pixels count as on wall
    private static final float wallJumpCooldownThreshold = 0.25f; //seconds until wall jump again - prevents wall scaling
    private static final float collisionPlayerHeightDivisor = 10f; //What amount of player height needs to be above a platform to land on top
    private static final float maxWaterRiseSpeed = 50f;
    private static final float faucetRange = 30f; //how close to faucet to interact
    private static final float faucetRotationSpeed = 240f; //degrees per second it spins
    private static final float currencyRange = 8f;
    private static final Random rng = new Random(LevelLibrary.getRng());

    /// Returns state of Spark (player) for animating
    public static AnimationKey updatePhysics(float delta){
        //initialize animationKey variable to return - default to idle
        AnimationKey currAnimation = AnimationKey.IDLE;

        //update jump buffer
        jumpBuffer += delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)){
            jumpBuffer = 0; //just tried to jump
        }

        //update previous x,y values to not tunnel through platforms
        float prevX = x;
        float prevY = y;

        //update wall jump cooldown
        wallJumpCooldown += delta;

        //gravity
        float currGravity = velocityY < 0 ? gravity * fallGravityMultiplier : gravity;
        velocityY -= currGravity * delta;

        //Holding jump
        if ((Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyPressed(Input.Keys.W)) && !onGround && velocityY > 0) {
            velocityY += jumpHoldForce * delta;
        }

        //Or holding down
        float currFriction = groundFriction;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)){
            if(!onGround) {
                velocityY += downforceFallAcceleration * delta;
            } else{
                currFriction = (float) Math.pow(groundFriction, 2);
            }
        }

        //falling cap speed
        velocityY = Math.max(maxFallSpeed, velocityY);

        //update x and y
        y += velocityY * delta;
        x += velocityX * delta;

        //Edge Nudging
        for(Platform p : platforms) {
            if (y + playerHeight > p.getY() && velocityY > 0 && y < p.getY() + p.getHeight()){
                if(x + playerWidth > p.getX() && x + playerWidth < p.getX() + nudgeThreshold){
                    velocityX += delta * nudgeSpeed;
                }

                if(x < p.getX() + p.getWidth() && x > p.getX() + p.getWidth() - nudgeThreshold){
                    velocityX -= delta * nudgeSpeed;
                }
            }
        }

        //Reset to avoid jumping mid-air
        onGround = false;

        //floor collision
        if (y <= 0) {
            impactVelocity = Math.abs(velocityY);
            onGround = true;
            y = 0;
            velocityY = 0;
            justLanded = true;
            lastWallRight = false;
            lastWallLeft = false;
        }

        //platform collision
        for (Platform p : platforms) {
            Rectangle playerRect = new Rectangle(x, y, playerWidth, playerHeight);
            Rectangle platformRect = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());


            //Player lands on top
            float collisionPoint = p.getY();
            if(!p.isBoundaryWall()) {
                if (p.getHeight() > collisionThreshold) {
                    collisionPoint = p.getHeight() + p.getY() - collisionThreshold;
                }
                if (playerRect.overlaps(platformRect) && velocityY < 0 && prevY + ((float) playerHeight / collisionPlayerHeightDivisor) >= collisionPoint) {
                    impactVelocity = Math.abs(velocityY);
                    y = p.getY() + p.getHeight();
                    velocityY = 0;
                    onGround = true;
                    justLanded = true;
                    lastWallRight = false;
                    lastWallLeft = false;
                }
            }

            //left of player hits right side of wall when moving left
            collisionPoint = p.getX() + (float) (p.getWidth() / 2);
            if ((float) p.getWidth() / 2 > collisionThreshold) {
                collisionPoint = p.getX() + p.getWidth() - collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && velocityX < 0 && prevX + (float) (playerWidth / 2) >= collisionPoint
                    && y + (playerHeight / collisionPlayerHeightDivisor) < p.getY() + p.getHeight()) {
                x = p.getX() + p.getWidth();
                velocityX = -1;
                touchingWallRight = true;
            }


            //right of player hits left side of wall when moving right
            collisionPoint = p.getX() + (float) (p.getWidth() / 2);
            if ((float) p.getWidth() / 2 > collisionThreshold) {
                collisionPoint = p.getX() + collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && velocityX > 0 && prevX + (float) (playerWidth / 2) <= collisionPoint
                    && y + (playerHeight / collisionPlayerHeightDivisor) < p.getY() + p.getHeight()) {
                x = p.getX() - playerWidth;
                velocityX = 1;
                touchingWallLeft = true;
            }

        }

        //check if on wall
        boolean onBoundingWall = false;
        for (Platform p : platforms){
            if (y + playerHeight < p.getY() || y > p.getY() + p.getHeight()) {
                continue; //not same height, skip!
            }

            //left of player, right of wall
            float distToRightWall = x - (p.getX() + p.getWidth());
            if (distToRightWall >= 0 && distToRightWall < wallProximity && velocityX <= 0){
                touchingWallRight = true;
                onBoundingWall = p.isBoundaryWall();
            }

            //right of player, left of wall
            float distToLeftWall = p.getX() - (x + playerWidth);
            if(distToLeftWall >= 0 && distToLeftWall < wallProximity && velocityX >= 0){
                touchingWallLeft = true;
                onBoundingWall = p.isBoundaryWall();
            }
        }

        //wall slide/jump
        boolean onWall = (touchingWallLeft || touchingWallRight) && !onGround;

        //wall slide
        if (onWall && velocityY < wallSlideMaxSpeed){
            velocityY = wallSlideMaxSpeed;
        }

        // coyote wall update
        if(onWall){
            wallCoyoteSeconds = 0;
        } else{
            wallCoyoteSeconds += delta;
        }

        //wall jump
        if((onWall || wallCoyoteSeconds < coyoteThreshold)
            && wallJumpCooldown >= wallJumpCooldownThreshold
            && (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                    || Gdx.input.isKeyJustPressed(Input.Keys.W) || jumpBuffer < jumpBufferThreshold)
            && !onGround){
            //jump fires, reset cooldown
            wallJumpCooldown = 0f;

            boolean wasRight = touchingWallRight || lastWallRight;
            boolean wasLeft = touchingWallLeft || lastWallLeft;

            boolean holdingInto =
                (wasRight && (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)))
                       || (wasLeft && (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)));
            boolean holdingAway = (wasRight && (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)))
                       || (wasLeft && (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)));

            float upForce;
            float sideForce;
            if(holdingInto){
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

            velocityY = upForce;

            //guarenteed either touching wall on right or left
            velocityX = wasRight ? sideForce : -sideForce;
            touchingWallRight = false;
            touchingWallLeft = false;
            lastWallRight = false;
            lastWallLeft = false;
            wallCoyoteSeconds = 100;
            coyoteSeconds = 100;
            jumpBuffer = 100;
            justLanded = false;
            screenShakeAccumulation = 0;
            onGround = false;
        }

        //Coyote timer
        if(!onGround){
            coyoteSeconds += delta;
        } else{
            coyoteSeconds = 0;
        }

        //danger boost
        float distanceFromWater = y - waterLevel;
        float dangerBoost = Math.max(0, 1 - (distanceFromWater / 400f));
        dangerBoost = 1 + (dangerBoost / 4);

        //horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)){
            if(onGround) {
                velocityX -= accelerationX * delta * dangerBoost;
            } else{
                velocityX -= (accelerationX * delta * dangerBoost) / airControlDivisor;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            if(onGround) {
                velocityX += accelerationX * delta * dangerBoost;
            } else{
                velocityX += (accelerationX * delta * dangerBoost) / airControlDivisor;
            }
        }

        //friction & velocity cap
        float frameGroundFriction = (float) Math.pow(currFriction, delta * 60);
        float frameAirFriction = (float) Math.pow(airFriction, delta * 60);
        velocityX *= onGround ? frameGroundFriction : frameAirFriction;
        velocityX = Math.clamp(velocityX, -maxSpeedX, maxSpeedX);

        //Jumping
        if ((Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                    || Gdx.input.isKeyJustPressed(Input.Keys.W)|| jumpBuffer < jumpBufferThreshold)
                    && (onGround || coyoteSeconds <= coyoteThreshold)) {
            coyoteSeconds = 100; //reset so can't double jump
            jumpBuffer = 100; //reset so can't double jump!
            velocityY = jumpForce * dangerBoost;
            onGround = false;
            justLanded = false;
            screenShakeAccumulation = 0;

            // Form of dash when you jump
            float speedRatio = Math.abs(velocityX) / maxSpeedX; // 0 to 1
            velocityX *= 1f + (speedRatio * 0.5f);
        }

        //rising water
        if(waterRise) {
            if(currentLevel == LevelType.INFINITE) {
                if(waterRiseSpeed <= maxWaterRiseSpeed) {
                    waterRiseSpeed += 2 * delta;
                }
            }
            waterLevel += waterRiseSpeed * delta;
        }


        //Checkpoints
        Rectangle playerRectFinal = new Rectangle(x, y, playerWidth, playerHeight);
        for(Checkpoint c : checkpoints){
            Rectangle checkpoint = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());
            if(!c.isActivated() && checkpoint.overlaps(playerRectFinal)){
                c.setActivated(true);
                if(currCheckpoint != null) {
                    if(currentLevel == LevelType.INFINITE){
                        if (c.getY() > currCheckpoint.getY()){
                            currCheckpoint = c;
                            deathWaterLevel = waterLevel;
                            timeAtDeath = time;

                            spawnCurrency(25, c.getX(), c.getY(), c.getWidth(), c.getHeight());
                        }
                    } else {
                        if (c.getOrder() > currCheckpoint.getOrder()) {
                            currCheckpoint = c;
                            deathWaterLevel = waterLevel;
                            timeAtDeath = time;
                        }
                    }
                } else{
                    currCheckpoint = c;
                    deathWaterLevel = waterLevel;
                    timeAtDeath = time;
                }
            }
        }

        //Faucets
        Faucet nearFaucet = null;
        for(Faucet f : faucets) {
            if(!f.isTurnedOff()){
                Rectangle faucet = new Rectangle(f.getX() - faucetRange, f.getY() - faucetRange, f.getWidth() + 2*faucetRange,
                    f.getHeight() + 2*faucetRange);
                if(playerRectFinal.overlaps(faucet)){
                    nearFaucet = f;
                    break;
                }
            }
        }

        boolean finishedFinalFaucet = false;
        if(nearFaucet != null && (Gdx.input.isKeyPressed(Input.Keys.E) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))){
            nearFaucet.setProgress(nearFaucet.getProgress() + delta);
            nearFaucet.setRotation(nearFaucet.getRotation() + (faucetRotationSpeed * delta));
            if(nearFaucet.getProgress() > faucetInteractionTime){
                nearFaucet.setTurnedOff(true);
                nearFaucet.setRotation(360);
                waterRiseSpeed -= nearFaucet.getSpeedReduction();
                if(nearFaucet.isWinFaucet()){
                    finishedFinalFaucet = true;
                }
                spawnCurrency(15, nearFaucet.getX(), nearFaucet.getY(), nearFaucet.getWidth(), nearFaucet.getHeight());
            }
        } else if(nearFaucet != null){
            //not holding down, start spinning back
            nearFaucet.setProgress(Math.max(0, nearFaucet.getProgress() - delta));
            if(nearFaucet.getRotation() > 0) {
                nearFaucet.setRotation(nearFaucet.getRotation() - (faucetRotationSpeed * delta));
            } else{
                nearFaucet.setRotation(0);
            }
        }

        //update currency positions and explode times, once time is up, switch velocity towards spark's head
        float sparkHeadX = x + ((float) playerWidth / 2);
        float sparkHeadY = y + playerHeight - 15;
        for (Currency c : currencies){
            if(c.getExplodeTime() > 0){
                c.moveX(c.getVelocityX() * delta);
                c.moveY(c.getVelocityY() * delta);
                c.updateExplodeTime(delta);
            } else if (c.getExplodeTime() < 0){
                float dx = sparkHeadX - c.getX();
                float dy = sparkHeadY - c.getY();

                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                float directionX = 0;
                float directionY = 0;
                if (distance > 0){
                    directionX = dx / distance;
                    directionY = dy / distance;
                }

                c.updateAbsorbTime(delta);

                if(c.getAbsorbTime() <= Currency.MAX_ABSORB_TIME) {
                    //better looking
                    boolean entropy = rng.nextBoolean();

                    if (entropy) {
                        directionX *= rng.nextFloat(1, 2);
                        directionY *= rng.nextFloat(1, 2);
                    }
                    c.changeVelocityX(directionX * Currency.ABSORB_ACCELERATION * delta);
                    c.changeVelocityY(directionY * Currency.ABSORB_ACCELERATION * delta);
                } else {
                    //quick absorb!
                    c.changeVelocityX(dx / .2f);
                    c.changeVelocityY(dy / .2f);
                }

                c.moveX(c.getVelocityX() * delta);
                c.moveY(c.getVelocityY() * delta);
            }
        }

        //check if overlapping currency
        Rectangle sparkHead = new Rectangle(x + ((float) playerWidth / 4), y + playerHeight - 10,
                                        (float) playerWidth / 2, 10);
        List<Currency> toRemove = new ArrayList<>();
        for(Currency c : currencies){
            Rectangle curr = new Rectangle(c.getX() - currencyRange, c.getY() - currencyRange,
                Currency.WIDTH + 2*currencyRange, Currency.HEIGHT + 2*currencyRange);
            if(playerRectFinal.overlaps(curr)){
                // This notes it is within range and then the next for loop moves it to absorb!
                c.updateExplodeTime(delta);
            }

            if(sparkHead.overlaps(curr) && c.getExplodeTime() < 0){
                //Check if overlapping spark's head!
                if(!c.isPrestige()) {
                    numMatches += 1;
                } else{
                    numFirewood += 1;
                }
                toRemove.add(c);
            }
        }

        currencies.removeAll(toRemove);

        // for tutorial level danger boost example
        if(waterTriggerZone != null) {
            if (playerRectFinal.overlaps(waterTriggerZone)) {
                waterLevel = 1784;
            }
        }

        if(waterFixZone != null){
            //close enough to waterFix zone, so there is also a faucet nearby that needs to be activated
            if(playerRectFinal.overlaps(waterFixZone) && nearFaucet != null && nearFaucet.isTurnedOff()){
                waterLevel = 10;
            }
        }

        //Check win condition before death
        if(finishedFinalFaucet){
            gameState = GameState.WIN;
        }

        //death check
        Rectangle waterRect = new Rectangle(-50000, 0, 100000, waterLevel);

        //Only dead if head hits water (because flame afro!), so top 15 pixels of player rect
        if(waterRect.overlaps(playerRectFinal) && waterLevel >= y + playerHeight - 15){
            if(!isDead){
                deathXValues.add(x);
            }
            isDead = true;
            gameState = GameState.DEATH;
            for(Faucet f : faucets){
                f.setTurnedOff(false);
                f.setRotation(0);
                f.setProgress(0);
            }
        }

        // reset flags for wall slide/jump
        if(touchingWallLeft) {
            lastWallLeft = true;
        }
        if(touchingWallRight) {
            lastWallRight = true;
        }

        touchingWallLeft = false;
        touchingWallRight = false;

        //update score LAST
        score = Math.max(score, y);

        time += delta;

        //update infinite level if needed
        if(currentLevel == LevelType.INFINITE) {
            if (y > LevelLibrary.getHighestYInInfinite() - 2000) {
                LevelData data = LevelLibrary.addToInfiniteLevel(platforms, checkpoints, faucets, currencies);
                GameScreen.updateLists(data);
            }
        }

        //update animation from least to most important (to choose which animation is shown - default is idle!)
        if(velocityX < -50){
            currAnimation = AnimationKey.RUN_LEFT;
        } else if(velocityX > 50){
            currAnimation = AnimationKey.RUN_RIGHT;
        }

        if(velocityY < -100){
            currAnimation = AnimationKey.FALL;
        } else if(velocityY > 0){
            currAnimation = AnimationKey.JUMP;
        }

        if(isDead){
            currAnimation = AnimationKey.DEATH;
        }

        return currAnimation;
    }

    private static void spawnCurrency(int numOfCurrency, float xRef, float yRef, float width, float height){
        for(int i = 0; i < numOfCurrency; i++){
            float xVelo = rng.nextFloat(300, 450) * Math.signum(rng.nextFloat(-1, 1));

            currencies.add(new Currency(xRef + width / 2,
                yRef + height / 2,
                false, xVelo,
                rng.nextFloat(300, 450),
                rng.nextFloat(0.05f, 0.15f)));
        }
    }
}
