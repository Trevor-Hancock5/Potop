package com.hancockt.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import java.util.ArrayList;
import java.util.List;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {
    //Rendering
    ShapeRenderer shapeRenderer;
    SpriteBatch batch;
    BitmapFont font;
    OrthographicCamera camera;
    Viewport viewport;
    OrthographicCamera hudCam;
    float viewportWidth = 800f;
    float viewportHeight = 480f;
    float screenShakeAccumulation;
    LevelType currentLevel;
    GameState state = GameState.MENU; //start with menu
    GlyphLayout layout = new GlyphLayout(); // create once, reuse

    //Player
    float x,y;
    float prevX, prevY;
    float velocityY;
    float velocityX;
    boolean onGround = false;
    boolean isDead = false;
    float score;
    float coyoteSeconds; //number of seconds off ground
    float wallCoyoteSeconds; //number of seconds off wall
    float jumpBuffer; // number of seconds since pressed jump
    boolean justLanded = false; //for screen shake
    float impactVelocity; //velocity when landing! use for screen shake
    boolean touchingWallLeft = false;
    boolean touchingWallRight = false;
    boolean lastWallLeft = false;
    boolean lastWallRight = false;
    float wallJumpCooldown = 0f;
    Checkpoint currCheckpoint; //current checkpoint player has made it to
    float deathWaterLevel; //water level when user hit current checkpoint
    Rectangle winZone;
    boolean infiniteUnlocked = false;

    //PLAYER TUNING!
    int accelerationX = 1600;
    int maxSpeedX = 425;
    int maxFallSpeed = -1200;
    float groundFriction = 0.935f;
    float airFriction = 0.99f;
    float gravity = 1000f;
    float fallGravityMultiplier = 1.6f;
    float jump_force = 400f;
    float jump_hold_force = 200f;
    int playerWidth = 20;
    int playerHeight = 50;
    float cameraLerpSpeed = .3f;
    float cameraDeadZoneDivisor = 5f;
    float coyoteThreshold = 0.1f; //Can be off platform for x seconds and still jump
    float jumpBufferThreshold = 0.1f;
    float downforceFallAcceleration = -300f;
    float screenShake = 0.3f; //time of shaking
    float screenShakeIntensity = 2f;
    float collisionThreshold = 10f; //number of pixels into platform and still pop out one side
    float nudgeSpeed = 50f;
    float nudgeThreshold = 5f;
    float airControlDivisor = 6f;
    float wallSlideMaxSpeed = -115f;
    float wallProximity = 5f; //how many pixels count as on wall
    float wallJumpCooldownThreshold = 0.25f; //seconds until wall jump again - prevents wall scaling
    float collisionPlayerHeightDivisor = 10f; //What amount of player height needs to be above a platform to land on top
    float checkpointMinWaterBuffer = 200f; //how far under checkpoint water starts at a minimum!
    float maxWaterRiseSpeed = 50f;

    //water
    float waterLevel;
    float water_rise_speed;
    Rectangle waterTriggerZone;
    Rectangle waterFixZone;

    List<Platform> platforms;
    List<Float> deathXValues;
    List<Checkpoint> checkpoints;
    boolean pressedI = false;
    boolean pressedN = false;

    //game reference
    lol game;

    // Controls
    boolean waterRise = false;


    public FirstScreen(lol game, LevelType type){
        this.game = game;
        currentLevel = type;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(viewportWidth, viewportHeight, camera);
        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, viewportWidth, viewportHeight);
        deathXValues = new ArrayList<>();

        loadLevel(currentLevel);
        restart();
    }

    //REMEMBER: Everything in here is made 60 times a second!
    //Reward commiting to jumps
    //maybe in 'tutorial' say once you jump, you commit!

    @Override
    public void render(float delta) {
        clearScreen();
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) Gdx.app.exit();

        switch(state){
            case MENU:
                handleMenuInput();
                drawMenu();
                return; //skip everything else
            case PAUSE:
                handlePauseInput();
                drawShapes(); //still draw game in background
                drawHUD();
                drawPauseOverlay();
                return;
            case DEATH:
                drawShapes();
                drawHUD();
                drawDeathText(delta);
                drawDeathOverlay();
                handleDeathInput();
                return;
            case WIN:
                drawWinOverlay();
                handleWinInput();
                drawShapes();
                drawHUD();
                return;
            case PLAYING:
                if(!isDead){
                    updatePhysics(delta);
                } else{
                    waterLevel += water_rise_speed * 10 * delta;
                }

                if(isDead || !deathXValues.isEmpty()){
                    drawDeathText(delta);
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
                    restart();
                }

                //TODO - change key, add wasd
                if(Gdx.input.isKeyPressed(Input.Keys.S)){
                    currCheckpoint = null;
                    restart();
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
                    state = GameState.PAUSE;
                }

                drawShapes();
                updateCamera(delta);
                drawHUD();
        }

    }

    private void clearScreen(){
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void updatePhysics(float delta){
        //update jump buffer
        jumpBuffer += delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            jumpBuffer = 0; //just tried to jump
        }

        //update previous x,y values to not tunnel through platforms
        prevX = x;
        prevY = y;

        //update wall jump cooldown
        wallJumpCooldown += delta;

        //gravity
        float currGravity = velocityY < 0 ? gravity * fallGravityMultiplier : gravity;
        velocityY -= currGravity * delta;

        //Holding jump
        if ((Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.SPACE)) && !onGround && velocityY > 0) {
            velocityY += jump_hold_force * delta;
        }

        //Or holding down
        float currFriction = groundFriction;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
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
            if (y + playerHeight > p.y && velocityY > 0 && y < p.y + p.height){
                if(x + playerWidth > p.x && x + playerWidth < p.x + nudgeThreshold){
                    velocityX += delta * nudgeSpeed;
                }

                if(x < p.x + p.width && x > p.x + p.width - nudgeThreshold){
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
            Rectangle platformRect = new Rectangle(p.x, p.y, p.width, p.height);


            //Player lands on top
            float collisionPoint = p.y;
            if(p.height > collisionThreshold){
                collisionPoint = p.height + p.y - collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && velocityY < 0 && prevY + ((float) playerHeight / collisionPlayerHeightDivisor) >= collisionPoint) {
                impactVelocity = Math.abs(velocityY);
                y = p.y + p.height;
                velocityY = 0;
                onGround = true;
                justLanded = true;
                lastWallRight = false;
                lastWallLeft = false;
            }

            //left of player hits right side of wall when moving left
            collisionPoint = p.x + (float) (p.width / 2);
            if ((float) p.width / 2 > collisionThreshold) {
                collisionPoint = p.x + p.width - collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && velocityX < 0 && prevX + (float) (playerWidth / 2) >= collisionPoint
                    && y + (float)(playerHeight / collisionPlayerHeightDivisor) < p.y + p.height) {
                x = p.x + p.width;
                velocityX = 0;
                touchingWallRight = true;
            }


            //right of player hits left side of wall when moving right
            collisionPoint = p.x + (float) (p.width / 2);
            if ((float) p.width / 2 > collisionThreshold) {
                collisionPoint = p.x + collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && velocityX > 0 && prevX + (float) (playerWidth / 2) <= collisionPoint
                    && y + (float)(playerHeight / collisionPlayerHeightDivisor) < p.y + p.height) {
                x = p.x - playerWidth;
                velocityX = 0;
                touchingWallLeft = true;
            }

        }

        //check if on wall
        boolean onBoundingWall = false;
        for (Platform p : platforms){
            if (y + playerHeight < p.y || y > p.y + p.height) continue; //not same height, skip!

            //left of player, right of wall
            float distToRightWall = x - (p.x + p.width);
            if (distToRightWall >= 0 && distToRightWall < wallProximity && velocityX <= 0){
                touchingWallRight = true;
                if(p.isBoundaryWall){
                    onBoundingWall = true;
                } else{
                    onBoundingWall = false;
                }
            }

            //right of player, left of wall
            float distToLeftWall = p.x - (x + playerWidth);
            if(distToLeftWall >= 0 && distToLeftWall < wallProximity && velocityX >= 0){
                touchingWallLeft = true;
                if(p.isBoundaryWall){
                    onBoundingWall = true;
                } else{
                    onBoundingWall = false;
                }
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
                || jumpBuffer < jumpBufferThreshold)
            && !onGround){
            //jump fires, reset cooldown
            wallJumpCooldown = 0f;

            boolean wasRight = touchingWallRight || lastWallRight;
            boolean wasLeft = touchingWallLeft || lastWallLeft;

            boolean holdingInto = (wasRight && Gdx.input.isKeyPressed(Input.Keys.LEFT))
                       || (wasLeft && Gdx.input.isKeyPressed(Input.Keys.RIGHT));
            boolean holdingAway = (wasRight && Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                       || (wasLeft && Gdx.input.isKeyPressed(Input.Keys.LEFT));

            float upForce, sideForce;
            if(holdingInto){
                if(onBoundingWall) {
                    upForce = jump_force * .55f;
                    sideForce = jump_force * .15f;
                } else{
                    upForce = jump_force * .8f;
                    sideForce = jump_force * .1f;
                }
            } else if (holdingAway){
                upForce = jump_force * .6f;
                sideForce = jump_force * 1.2f;
            } else{
                //not into or away
                upForce = jump_force;
                sideForce = .85f * jump_force;
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
        float dangerBoost = Math.max (0, 1 - (distanceFromWater / 400f));
        dangerBoost = 1 + (dangerBoost / 4);

        //horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            if(onGround) {
                velocityX -= accelerationX * delta * dangerBoost;
            } else{
                velocityX -= (accelerationX * delta * dangerBoost) / airControlDivisor;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if(onGround) {
                velocityX += accelerationX * delta * dangerBoost;
            } else{
                velocityX += (accelerationX * delta * dangerBoost) / airControlDivisor;
            }
        }

        //friction & velocity cap
        velocityX *= onGround ? currFriction : airFriction;
        velocityX = Math.max(-maxSpeedX, Math.min(maxSpeedX, velocityX));

        //Jumping
        if ((Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || jumpBuffer < jumpBufferThreshold)
                && (onGround || coyoteSeconds <= coyoteThreshold)) {
            coyoteSeconds = 100; //reset so can't double jump
            jumpBuffer = 100; //reset so can't double jump!
            velocityY = jump_force * dangerBoost;
            onGround = false;
            justLanded = false;
            screenShakeAccumulation = 0;

            // Form of dash when you jump
            float speedRatio = Math.abs(velocityX) / maxSpeedX; // 0 to 1
            velocityX *= (1f + speedRatio * 0.5f);
        }

        //rising water
        if(waterRise) {
            if(currentLevel == LevelType.INFINITE) {
                if(water_rise_speed <= maxWaterRiseSpeed) {
                    water_rise_speed += 2 * delta;
                }
            }
            waterLevel += water_rise_speed * delta;
        }


        //Checkpoints
        Rectangle playerRectFinal = new Rectangle(x, y, playerWidth, playerHeight);
        for(Checkpoint c : checkpoints){
            Rectangle checkpoint = new Rectangle(c.x, c.y, playerWidth * 2, playerHeight * 2);
            if(!c.activated && checkpoint.overlaps(playerRectFinal)){
                c.activated = true;
                if(currCheckpoint != null) {
                    if (c.order > currCheckpoint.order) {
                        currCheckpoint = c;
                        deathWaterLevel = waterLevel;
                    }
                } else{
                    currCheckpoint = c;
                    deathWaterLevel = waterLevel;
                }
            }
        }

        // for tutorial level danger boost example
        if(waterTriggerZone != null) {
            if (playerRectFinal.overlaps(waterTriggerZone)) {
                waterLevel = y - 100;
            }
        }

        if(waterFixZone != null){
            if(playerRectFinal.overlaps(waterFixZone)){
                waterLevel = 10;
            }
        }

        //Check win condition before death
        if(winZone != null && winZone.overlaps(playerRectFinal)){
            state = GameState.WIN;
        }

        //death check
        Rectangle waterRect = new Rectangle (-50000, 0, 100000, waterLevel);

        if(waterRect.overlaps(playerRectFinal)){
            if(!isDead){
                deathXValues.add(x);
            }
            isDead = true;
            state = GameState.DEATH;
        }

        // reset flags for wall slide/jump
        if(touchingWallLeft) lastWallLeft = true;
        if(touchingWallRight) lastWallRight = true;

        touchingWallLeft = false;
        touchingWallRight = false;

        //update score LAST
        score = Math.max(score, y);
    }

    private void updateCamera(float delta) {
        float deadzone = camera.viewportWidth / cameraDeadZoneDivisor;
        float deadzoneRight = camera.position.x + (deadzone / 2);
        float deadzoneLeft = camera.position.x - (deadzone / 2);

        float targetX = camera.position.x; //default is to stay put

        if(x < deadzoneLeft){
            targetX = x + (deadzone / 2);
        } else if (x > deadzoneRight){
            targetX = x - (deadzone / 2);
        }

        float vertLookAhead = velocityY * 0.1f;
        float targetY = isDead ? Math.max(waterLevel, 240) : Math.max(y+25+vertLookAhead, 240);

        camera.position.x += (targetX - camera.position.x) * cameraLerpSpeed;
        camera.position.y += (targetY - camera.position.y) * (cameraLerpSpeed / 3);

        if(justLanded && screenShakeAccumulation < screenShake){
            if(impactVelocity / maxFallSpeed > 0.5){
                screenShakeAccumulation += delta / 2;
            } else {
                screenShakeAccumulation += delta;
            }
            camera.position.x += (float) ((Math.random() - 0.5) * (screenShakeIntensity * (2 + impactVelocity / maxFallSpeed)));
            camera.position.y += (float) ((Math.random() - 0.5) * (screenShakeIntensity * (2 + impactVelocity / maxFallSpeed)));
        }
        camera.update();
    }

    private void drawShapes(){
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        //platforms
        for(Platform p : platforms){
            shapeRenderer.setColor(p.isBoundaryWall ? Color.DARK_GRAY : Color.WHITE);
            shapeRenderer.rect(p.x, p.y, p.width, p.height);
        }

        //checkpoints
        for(Checkpoint c : checkpoints) {
            shapeRenderer.setColor(c.activated ? new Color(0,1,0,0.6f) : new Color(1,0.5f,0.3f,0.6f)); //TODO -
            // change alpha!
            shapeRenderer.rect(c.x, c.y, playerWidth * 2, playerHeight * 2);
        }

        //winzone
        if(winZone != null) {
            shapeRenderer.setColor(Color.GOLDENROD);
            shapeRenderer.rect(winZone.x, winZone.y, winZone.width, winZone.height);
        }

        if(!isDead){
            shapeRenderer.setColor(Color.OLIVE);
            shapeRenderer.rect(x, y, playerWidth, playerHeight);
        }

        //water
        shapeRenderer.setColor(new Color(17/255f, 132/255f, 214/255f, .65f));

        float camLeft = camera.position.x - camera.viewportWidth / 2;
        shapeRenderer.rect(camLeft, 0, camera.viewportWidth, waterLevel);

        shapeRenderer.end();
    }

    private void drawDeathText(float delta) {
        float bob = (float) Math.sin(TimeUtils.millis() / 300.0) * 2.5f;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for(Float f : deathXValues) {
            font.draw(batch, "lol", f, waterLevel + 10 + bob);
        }
        batch.end();
    }

    private void restart(){
        if(currCheckpoint != null){
            x = currCheckpoint.x;
            y = currCheckpoint.y;
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
                    c.activated = false;
                }
            }
        }
        velocityY = 0;
        velocityX = 0;
        // scale the reset speed with how high the checkpoint/respawn point is
        water_rise_speed = Math.min(50f, 8f + (y / 200f)); //full speed always after ~8k height
        onGround = false;
        isDead = false;
        coyoteSeconds = 100; //reset
        wallCoyoteSeconds = 100; //reset
        jumpBuffer = 100; //reset
        justLanded = false; //reset
        screenShakeAccumulation = 0;
        wallJumpCooldown = 0;
        touchingWallRight = false;
        touchingWallLeft = false;
        lastWallLeft = false;
        lastWallRight = false;
    }

    private void loadLevel(LevelType levelType){
        currentLevel = levelType;

        LevelData data = LevelLibrary.ALL_LEVELS.get(levelType);
        platforms = data.platforms;
        checkpoints = data.checkpoints;
        waterTriggerZone = data.waterTriggerZone;
        waterFixZone = data.waterFixZone;
        winZone = data.winZone;
        waterRise = data.waterStartsActive;
        x = data.startPoint.x;
        y = data.startPoint.y;

        //TODO - implement logic for next level
    }

    private void drawHUD(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();
        font.draw(batch, "Score: " + Math.round(score), viewportWidth - 100, viewportHeight - 10);
        font.draw(batch, "x:" + Math.round(x) + " y:" + Math.round(y), 10, viewportHeight - 10);
        font.draw(batch, "water rise: " + water_rise_speed, 10, viewportHeight - 50);
        font.draw(batch, "waterLevel: " + waterLevel, 10, viewportHeight - 90);
        batch.end();
    }

    private void drawCenteredText(String text, float yOffset, float scale){
        font.getData().setScale(scale);

        layout.setText(font, text);
        float x = (viewportWidth - layout.width) / 2;
        float y = (viewportHeight - layout.height) / 2 + yOffset;
        font.draw(batch, text, x, y);

        font.getData().setScale(1f); //reset
    }

    private void drawMenu(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        drawCenteredText("The MC", 70, 2);
        drawCenteredText("Press SPACE to start tutorial", 10, 1);
        drawCenteredText("Press I to start infinite (Must complete tutorial first)", -10, 1);
        //TODO - unless hit 5 times in quick succession, then go there

        batch.end();
    }

    private void handleMenuInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            loadLevel(LevelType.TUTORIAL);
            restart();
            state = GameState.PLAYING;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.I)){
            if(infiniteUnlocked) {
                loadLevel(LevelType.INFINITE);
                restart();
                state = GameState.PLAYING;
            }
            pressedI = true;
        }
        if(pressedI && Gdx.input.isKeyJustPressed(Input.Keys.N)){
            pressedN = true;
        }
        if(pressedI && pressedN && Gdx.input.isKeyJustPressed(Input.Keys.F)){
            //Skip straight to infinite mode
            loadLevel(LevelType.INFINITE);
            restart();
            state = GameState.PLAYING;
        }
    }

    private void drawPauseOverlay(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        drawCenteredText("PAUSED", 35, 2);
        drawCenteredText("Press P to resume", 0, 1);

        batch.end();
    }

    private void handlePauseInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
            state = GameState.PLAYING;
        }
    }

    private void drawDeathOverlay(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        drawCenteredText("You drowned. Score: " + Math.round(score), 30, 1.5f);
        drawCenteredText("Press R to retry!", 0, 1);

        batch.end();
    }

    private void handleDeathInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            restart();
            state = GameState.PLAYING;
        }
    }

    private void drawWinOverlay(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        drawCenteredText("YOU MADE IT!!", 65, 3);
        drawCenteredText("Press ENTER to continue", 0, 1);

        batch.end();
    }

    private void handleWinInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            LevelData data = LevelLibrary.ALL_LEVELS.get(currentLevel);
            LevelType next = data.nextLevel;
            if(next != null){
                if(currentLevel == LevelType.TUTORIAL){
                    infiniteUnlocked = true;
                }
                deathWaterLevel = -50f;
                currCheckpoint = null;
                checkpoints = null;
                LevelData nextLevel = LevelLibrary.ALL_LEVELS.get(next);
                x = nextLevel.startPoint.x;
                y = nextLevel.startPoint.y;
                currentLevel = next;
                loadLevel(next);
                restart();
                state = GameState.PLAYING;
            } else{
                state = GameState.MENU;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if(width <= 0 || height <= 0) return;

        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
