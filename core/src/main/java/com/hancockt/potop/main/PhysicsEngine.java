package com.hancockt.potop.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.assets.AnimationKey;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.features.Currency;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.level.LevelData;
import com.hancockt.potop.level.LevelLibrary;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.GameState;
import com.hancockt.potop.state.PlayerStateOutdated;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private static final float maxWaterRiseSpeed = 50f; // TODO - one location!
    private static final float faucetRange = 30f; //how close to faucet to interact
    private static final float faucetRotationSpeed = 240f; //degrees per second it spins
    private static final float currencyRange = 8f;
    private static final Random rng = new Random(LevelLibrary.getRng());

    /// Returns state of Spark (player) for animating
    public static AnimationKey updatePhysics(float delta){
        //initialize animationKey variable to return - default to idle
        AnimationKey currAnimation = AnimationKey.IDLE;

        //update jump buffer
        PlayerStateOutdated.jumpBuffer += delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)){
            PlayerStateOutdated.jumpBuffer = 0; //just tried to jump
        }

        //update previous x,y values to not tunnel through platforms
        float prevX = PlayerStateOutdated.x;
        float prevY = PlayerStateOutdated.y;

        //update wall jump cooldown
        PlayerStateOutdated.wallJumpCooldown += delta;

        //gravity
        float currGravity = PlayerStateOutdated.velocityY < 0 ? gravity * fallGravityMultiplier : gravity;
        PlayerStateOutdated.velocityY -= currGravity * delta;

        //Holding jump
        if ((Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyPressed(Input.Keys.W)) && !PlayerStateOutdated.onGround && PlayerStateOutdated.velocityY > 0) {
            PlayerStateOutdated.velocityY += jumpHoldForce * delta;
        }

        //Or holding down
        float currFriction = groundFriction;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)){
            if(!PlayerStateOutdated.onGround) {
                PlayerStateOutdated.velocityY += downforceFallAcceleration * delta;
            } else{
                currFriction = (float) Math.pow(groundFriction, 2);
            }
        }

        //falling cap speed
        PlayerStateOutdated.velocityY = Math.max(GameScreen.maxFallSpeed, PlayerStateOutdated.velocityY);

        //update x and y
        PlayerStateOutdated.y += PlayerStateOutdated.velocityY * delta;
        PlayerStateOutdated.x += PlayerStateOutdated.velocityX * delta;

        //Edge Nudging
        for(Platform p : GameScreen.platforms) {
            if (PlayerStateOutdated.y + PlayerStateOutdated.playerHeight > p.getY() && PlayerStateOutdated.velocityY > 0 && PlayerStateOutdated.y < p.getY() + p.getHeight()){
                if(PlayerStateOutdated.x + PlayerStateOutdated.playerWidth > p.getX() && PlayerStateOutdated.x + PlayerStateOutdated.playerWidth < p.getX() + nudgeThreshold){
                    PlayerStateOutdated.velocityX += delta * nudgeSpeed;
                }

                if(PlayerStateOutdated.x < p.getX() + p.getWidth() && PlayerStateOutdated.x > p.getX() + p.getWidth() - nudgeThreshold){
                    PlayerStateOutdated.velocityX -= delta * nudgeSpeed;
                }
            }
        }

        //Reset to avoid jumping midair
        PlayerStateOutdated.onGround = false;

        //floor collision
        if (PlayerStateOutdated.y <= 0) {
            PlayerStateOutdated.impactVelocity = Math.abs(PlayerStateOutdated.velocityY);
            PlayerStateOutdated.onGround = true;
            PlayerStateOutdated.y = 0;
            PlayerStateOutdated.velocityY = 0;
            PlayerStateOutdated.justLanded = true;
            PlayerStateOutdated.lastWallRight = false;
            PlayerStateOutdated.lastWallLeft = false;
        }

        //platform collision
        for (Platform p : GameScreen.platforms) {
            Rectangle playerRect = new Rectangle(PlayerStateOutdated.x, PlayerStateOutdated.y, PlayerStateOutdated.playerWidth, PlayerStateOutdated.playerHeight);
            Rectangle platformRect = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());


            //Player lands on top
            float collisionPoint = p.getY();
            if(!p.isBoundaryWall()) {
                if (p.getHeight() > collisionThreshold) {
                    collisionPoint = p.getHeight() + p.getY() - collisionThreshold;
                }
                if (playerRect.overlaps(platformRect) && PlayerStateOutdated.velocityY < 0 && prevY + ((float) PlayerStateOutdated.playerHeight / collisionPlayerHeightDivisor) >= collisionPoint) {
                    PlayerStateOutdated.impactVelocity = Math.abs(PlayerStateOutdated.velocityY);
                    PlayerStateOutdated.y = p.getY() + p.getHeight();
                    PlayerStateOutdated.velocityY = 0;
                    PlayerStateOutdated.onGround = true;
                    PlayerStateOutdated.justLanded = true;
                    PlayerStateOutdated.lastWallRight = false;
                    PlayerStateOutdated.lastWallLeft = false;
                }
            }

            //left of player hits right side of wall when moving left
            collisionPoint = p.getX() + (float) (p.getWidth() / 2);
            if ((float) p.getWidth() / 2 > collisionThreshold) {
                collisionPoint = p.getX() + p.getWidth() - collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && PlayerStateOutdated.velocityX < 0 && prevX + (float) (PlayerStateOutdated.playerWidth / 2) >= collisionPoint
                    && PlayerStateOutdated.y + (PlayerStateOutdated.playerHeight / collisionPlayerHeightDivisor) < p.getY() + p.getHeight()) {
                PlayerStateOutdated.x = p.getX() + p.getWidth();
                PlayerStateOutdated.velocityX = -1;
                PlayerStateOutdated.touchingWallRight = true;
            }


            //right of player hits left side of wall when moving right
            collisionPoint = p.getX() + (float) (p.getWidth() / 2);
            if ((float) p.getWidth() / 2 > collisionThreshold) {
                collisionPoint = p.getX() + collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && PlayerStateOutdated.velocityX > 0 && prevX + (float) (PlayerStateOutdated.playerWidth / 2) <= collisionPoint
                    && PlayerStateOutdated.y + (PlayerStateOutdated.playerHeight / collisionPlayerHeightDivisor) < p.getY() + p.getHeight()) {
                PlayerStateOutdated.x = p.getX() - PlayerStateOutdated.playerWidth;
                PlayerStateOutdated.velocityX = 1;
                PlayerStateOutdated.touchingWallLeft = true;
            }

        }

        //check if on wall
        boolean onBoundingWall = false;
        for (Platform p : GameScreen.platforms){
            if (PlayerStateOutdated.y + PlayerStateOutdated.playerHeight < p.getY() || PlayerStateOutdated.y > p.getY() + p.getHeight()) {
                continue; //not same height, skip!
            }

            //left of player, right of wall
            float distToRightWall = PlayerStateOutdated.x - (p.getX() + p.getWidth());
            if (distToRightWall >= 0 && distToRightWall < wallProximity && PlayerStateOutdated.velocityX <= 0){
                PlayerStateOutdated.touchingWallRight = true;
                onBoundingWall = p.isBoundaryWall();
            }

            //right of player, left of wall
            float distToLeftWall = p.getX() - (PlayerStateOutdated.x + PlayerStateOutdated.playerWidth);
            if(distToLeftWall >= 0 && distToLeftWall < wallProximity && PlayerStateOutdated.velocityX >= 0){
                PlayerStateOutdated.touchingWallLeft = true;
                onBoundingWall = p.isBoundaryWall();
            }
        }

        //wall slide/jump
        boolean onWall = (PlayerStateOutdated.touchingWallLeft || PlayerStateOutdated.touchingWallRight) && !PlayerStateOutdated.onGround;

        //wall slide
        if (onWall && PlayerStateOutdated.velocityY < wallSlideMaxSpeed){
            PlayerStateOutdated.velocityY = wallSlideMaxSpeed;
        }

        // coyote wall update
        if(onWall){
            PlayerStateOutdated.wallCoyoteSeconds = 0;
        } else{
            PlayerStateOutdated.wallCoyoteSeconds += delta;
        }

        //wall jump
        if((onWall || PlayerStateOutdated.wallCoyoteSeconds < coyoteThreshold)
            && PlayerStateOutdated.wallJumpCooldown >= wallJumpCooldownThreshold
            && (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                    || Gdx.input.isKeyJustPressed(Input.Keys.W) || PlayerStateOutdated.jumpBuffer < jumpBufferThreshold)
            && !PlayerStateOutdated.onGround){
            //jump fires, reset cooldown
            PlayerStateOutdated.wallJumpCooldown = 0f;

            boolean wasRight = PlayerStateOutdated.touchingWallRight || PlayerStateOutdated.lastWallRight;
            boolean wasLeft = PlayerStateOutdated.touchingWallLeft || PlayerStateOutdated.lastWallLeft;

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

            PlayerStateOutdated.velocityY = upForce;

            //guaranteed either touching wall on right or left
            PlayerStateOutdated.velocityX = wasRight ? sideForce : -sideForce;
            PlayerStateOutdated.touchingWallRight = false;
            PlayerStateOutdated.touchingWallLeft = false;
            PlayerStateOutdated.lastWallRight = false;
            PlayerStateOutdated.lastWallLeft = false;
            PlayerStateOutdated.wallCoyoteSeconds = 100;
            PlayerStateOutdated.coyoteSeconds = 100;
            PlayerStateOutdated.jumpBuffer = 100;
            PlayerStateOutdated.justLanded = false;
            GameScreen.screenShakeAccumulation = 0;
            PlayerStateOutdated.onGround = false;
        }

        //Coyote timer
        if(!PlayerStateOutdated.onGround){
            PlayerStateOutdated.coyoteSeconds += delta;
        } else{
            PlayerStateOutdated.coyoteSeconds = 0;
        }

        //danger boost
        float distanceFromWater = PlayerStateOutdated.y - GameScreen.waterLevel;
        //TODO -move to water system
        final float dangerBoostDistance = 400f;
        float dangerBoost = Math.max(0, 1 - (distanceFromWater / dangerBoostDistance));
        dangerBoost = 1 + (dangerBoost / 4); // from 1 to 1.25!

        //horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)){
            if(PlayerStateOutdated.onGround) {
                PlayerStateOutdated.velocityX -= accelerationX * delta * dangerBoost;
            } else{
                PlayerStateOutdated.velocityX -= (accelerationX * delta * dangerBoost) / airControlDivisor;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            if(PlayerStateOutdated.onGround) {
                PlayerStateOutdated.velocityX += accelerationX * delta * dangerBoost;
            } else{
                PlayerStateOutdated.velocityX += (accelerationX * delta * dangerBoost) / airControlDivisor;
            }
        }

        //friction & velocity cap
        float frameGroundFriction = (float) Math.pow(currFriction, delta * 60);
        float frameAirFriction = (float) Math.pow(airFriction, delta * 60);
        PlayerStateOutdated.velocityX *= PlayerStateOutdated.onGround ? frameGroundFriction : frameAirFriction;
        PlayerStateOutdated.velocityX = Math.clamp(PlayerStateOutdated.velocityX, -maxSpeedX, maxSpeedX);

        //Jumping
        if ((Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                    || Gdx.input.isKeyJustPressed(Input.Keys.W)|| PlayerStateOutdated.jumpBuffer < jumpBufferThreshold)
                    && (PlayerStateOutdated.onGround || PlayerStateOutdated.coyoteSeconds <= coyoteThreshold)) {
            PlayerStateOutdated.coyoteSeconds = 100; //reset so can't double jump
            PlayerStateOutdated.jumpBuffer = 100; //reset so can't double jump!
            PlayerStateOutdated.velocityY = jumpForce * dangerBoost;
            PlayerStateOutdated.onGround = false;
            PlayerStateOutdated.justLanded = false;
            GameScreen.screenShakeAccumulation = 0;

            // Form of dash when you jump
            float speedRatio = Math.abs(PlayerStateOutdated.velocityX) / maxSpeedX; // 0 to 1
            PlayerStateOutdated.velocityX *= 1f + (speedRatio * 0.5f); // max of 1.5
        }

        //rising water
        if(GameScreen.waterRise) {
            if(GameScreen.currentLevel == LevelType.INFINITE) {
                if(GameScreen.waterRiseSpeed <= maxWaterRiseSpeed) {
                    //TODO make constant, pixels per second!
                    GameScreen.waterRiseSpeed += 2 * delta; // increase water speed by 2 pixels every second (as delta accumulates)
                }
            }
            GameScreen.waterLevel += GameScreen.waterRiseSpeed * delta;
        }


        //Checkpoints
        Rectangle playerRectFinal = new Rectangle(PlayerStateOutdated.x, PlayerStateOutdated.y, PlayerStateOutdated.playerWidth, PlayerStateOutdated.playerHeight);
        for(Checkpoint c : GameScreen.checkpoints){
            Rectangle checkpoint = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());
            if(!c.isActivated() && checkpoint.overlaps(playerRectFinal)){
                c.setActivated(true);
                if(PlayerStateOutdated.currCheckpoint != null) {
                    if(GameScreen.currentLevel == LevelType.INFINITE){
                        if (c.getY() > PlayerStateOutdated.currCheckpoint.getY()){
                            PlayerStateOutdated.currCheckpoint = c;
                            PlayerStateOutdated.checkpointWaterLevel = GameScreen.waterLevel;
                            GameScreen.timeAtDeath = GameScreen.time;

                            // todo checkpoint currency amount! (make somewhat random? - define in checkpoint)
                            spawnCurrency(25, c.getX(), c.getY(), c.getWidth(), c.getHeight());
                        }
                    } else {
                        if (c.getOrder() > PlayerStateOutdated.currCheckpoint.getOrder()) {
                            PlayerStateOutdated.currCheckpoint = c;
                            PlayerStateOutdated.checkpointWaterLevel = GameScreen.waterLevel;
                            GameScreen.timeAtDeath = GameScreen.time;
                        }
                    }
                } else{
                    PlayerStateOutdated.currCheckpoint = c;
                    PlayerStateOutdated.checkpointWaterLevel = GameScreen.waterLevel;
                    GameScreen.timeAtDeath = GameScreen.time;
                }
            }
        }

        //Faucets
        Faucet nearFaucet = null;
        for(Faucet f : GameScreen.faucets) {
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
            if(nearFaucet.getProgress() > GameScreen.faucetInteractionTime){
                nearFaucet.setTurnedOff(true);
                nearFaucet.setRotation(360);
                GameScreen.waterRiseSpeed -= nearFaucet.getSpeedReduction();
                if(nearFaucet.isWinFaucet()){
                    finishedFinalFaucet = true;
                }

                // todo faucet currency amount! (make somewhat random? - define in faucet)
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
        float sparkHeadX = PlayerStateOutdated.x + ((float) PlayerStateOutdated.playerWidth / 2);
        float sparkHeadY = PlayerStateOutdated.y + PlayerStateOutdated.playerHeight - 15; // TODO - define as constant somewhere!
        for (Currency c : GameScreen.currencies){
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
                    //time to get absorbed
                    float t = Math.max((Currency.MAX_ABSORB_TIME * 2) - c.getAbsorbTime(), 0);
                    c.setVelocityX(dx / t);
                    c.setVelocityY(dy / t);
                }

                c.moveX(c.getVelocityX() * delta);
                c.moveY(c.getVelocityY() * delta);
            }
        }

        //check if overlapping currency
        Rectangle sparkHead = new Rectangle(PlayerStateOutdated.x + ((float) PlayerStateOutdated.playerWidth / 4), PlayerStateOutdated.y + PlayerStateOutdated.playerHeight - 10,
                                        (float) PlayerStateOutdated.playerWidth / 2, 10);
        List<Currency> toRemove = new ArrayList<>();
        for(Currency c : GameScreen.currencies){
            Rectangle curr = new Rectangle(c.getX() - currencyRange, c.getY() - currencyRange,
                Currency.WIDTH + 2*currencyRange, Currency.HEIGHT + 2*currencyRange);
            if(playerRectFinal.overlaps(curr)){
                // This notes it is within range and then the next for loop moves it to absorb!
                c.updateExplodeTime(delta);
            }

            if(sparkHead.overlaps(curr) && c.getExplodeTime() < 0){
                //Check if overlapping spark's head!
                if(!c.isPrestige()) {
                    PlayerStateOutdated.numMatches += 1;
                } else{
                    PlayerStateOutdated.numFirewood += 1;
                }
                toRemove.add(c);
            }
        }

        GameScreen.currencies.removeAll(toRemove);

        // for tutorial level danger boost example
        if(GameScreen.waterTriggerZone != null) {
            if (playerRectFinal.overlaps(GameScreen.waterTriggerZone)) {
                GameScreen.waterLevel = 1784;
            }
        }

        if(GameScreen.waterFixZone != null){
            //close enough to waterFix zone, so there is also a faucet nearby that needs to be activated
            if(playerRectFinal.overlaps(GameScreen.waterFixZone) && nearFaucet != null && nearFaucet.isTurnedOff()){
                GameScreen.waterLevel = 10;
            }
        }

        //Check win condition before death
        if(finishedFinalFaucet){
            GameScreen.gameState = GameState.WIN;
        }

        //death check
        Rectangle waterRect = new Rectangle(-50000, 0, 100000, GameScreen.waterLevel);

        //Only dead if head hits water (because flame Afro!), so top 15 pixels of player rect
        if(waterRect.overlaps(playerRectFinal) && GameScreen.waterLevel >= sparkHeadY){
            if(!PlayerStateOutdated.isDead){
                GameScreen.deathXValues.add(PlayerStateOutdated.x);
            }
            PlayerStateOutdated.isDead = true;
            GameScreen.gameState = GameState.DEATH;
            for(Faucet f : GameScreen.faucets){
                f.setTurnedOff(false);
                f.setRotation(0);
                f.setProgress(0);
            }
        }

        // reset flags for wall slide/jump
        if(PlayerStateOutdated.touchingWallLeft) {
            PlayerStateOutdated.lastWallLeft = true;
        }
        if(PlayerStateOutdated.touchingWallRight) {
            PlayerStateOutdated.lastWallRight = true;
        }

        PlayerStateOutdated.touchingWallLeft = false;
        PlayerStateOutdated.touchingWallRight = false;

        //update score LAST
        PlayerStateOutdated.score = Math.max(PlayerStateOutdated.score, PlayerStateOutdated.y);

        GameScreen.time += delta;

        //update infinite level if needed
        if(GameScreen.currentLevel == LevelType.INFINITE) {
            // Todo - 2000? why and where, define constant! 2000 is sprinkled in many areas!
            if (PlayerStateOutdated.y > LevelLibrary.getHighestYInInfinite() - 2000) {
                LevelData data = LevelLibrary.addToInfiniteLevel(GameScreen.platforms, GameScreen.checkpoints, GameScreen.faucets, GameScreen.currencies);
                GameScreen.updateLists(data);
            }
        }

        //update animation from least to most important (to choose which animation is shown - default is idle!)
        //todo -define constant
        final float showRunAnimation = 50f;
        final float showFallAnimation = -100f;
        if(PlayerStateOutdated.velocityX < -showRunAnimation){
            currAnimation = AnimationKey.RUN_LEFT;
        } else if(PlayerStateOutdated.velocityX > showRunAnimation){
            currAnimation = AnimationKey.RUN_RIGHT;
        }

        if(PlayerStateOutdated.velocityY < showFallAnimation){
            currAnimation = AnimationKey.FALL;
        } else if(PlayerStateOutdated.velocityY > 0){
            currAnimation = AnimationKey.JUMP;
        }

        if(PlayerStateOutdated.isDead){
            currAnimation = AnimationKey.DEATH;
        }

        return currAnimation;
    }

    private static void spawnCurrency(int numOfCurrency, float xRef, float yRef, float width, float height){
        //TODO - add constants in currency for this!
        // todo - move to currency!
        for(int i = 0; i < numOfCurrency; i++){
            float explodeXVelocity = rng.nextFloat(0, 450) * Math.signum(rng.nextFloat(-1, 1));
            float explodeYVelocity = rng.nextFloat(0, 450) * Math.signum(rng.nextFloat(-1, 1));

            GameScreen.currencies.add(new Currency(xRef + width / 2,
                yRef + height / 2,
                false, explodeXVelocity,
                explodeYVelocity,
//                rng.nextFloat(300, 450),
                rng.nextFloat(0.05f, 0.15f))); // time
        }
    }
}
