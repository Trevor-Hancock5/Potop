package com.hancockt.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
    OrthogonalTiledMapRenderer mapRenderer;

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

    //water
    float waterLevel;
    float water_rise_speed;

    List<Platform> platforms;
    List<Float> deathXValues;
    List<Checkpoint> checkpoints;

    //game reference
    lol game;

    // Controls
    int levelDesign = 2;
    boolean waterRise = false;


    public FirstScreen(lol game){
        this.game = game;
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

        restart();

        //Set up platforms and checkpoints
        platforms = new ArrayList<>();
        checkpoints = new ArrayList<>();

        if(levelDesign == 0) {
            checkpoints.add(new Checkpoint(16,0));
            checkpoints.add(new Checkpoint(16,1100));
            checkpoints.add(new Checkpoint(16,900));
            checkpoints.add(new Checkpoint(1000,2145));
            checkpoints.add(new Checkpoint(2000,1800));
            checkpoints.add(new Checkpoint(1250,1150));
            TiledMap map = new TmxMapLoader().load("tuning.tmx");
            MapLayer platformLayer = map.getLayers().get("platforms");

            for (MapObject o : platformLayer.getObjects()) {
                if (o instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) o).getRectangle();
                    Platform p = new Platform(rect.x, rect.y, rect.width, rect.height);
                    platforms.add(p);
                }
            }

            MapLayer wallLayer = map.getLayers().get("walls");
            for (MapObject o : wallLayer.getObjects()) {
                if (o instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) o).getRectangle();
                    Platform p = new Platform(rect.x, rect.y, rect.width, rect.height);
                    platforms.add(p);
                }
            }


            mapRenderer = new OrthogonalTiledMapRenderer(map);
        } else if (levelDesign == 1){
            platforms.add(new Platform(-40, 0, 40, 9000));
            platforms.add(new Platform(2000, 0, 40, 9000));
            for (int i = 0; i < 400; i++) {
                boolean noCollision;
                do {
                    int xVal = (int) (Math.random() * 2000);
                    int Yval = (int) (Math.random() * 8000);
                    int width = (int) (Math.random() * 175 + 25);
                    int height = (int) (Math.random() * 90 + 10);
                    platforms.add(new Platform(xVal, Yval, width, height));
                    noCollision = true;
                    for (int j = 0; j < platforms.size(); j++) {
                        Platform p = platforms.get(i);
                        Rectangle p1 = new Rectangle(p.x, p.y, p.width, p.height);
                        Rectangle p2 = new Rectangle(xVal, Yval, width, height);
                        if (p1.overlaps(p2)) {
                            noCollision = false;
                            break;
                        }
                    }
                } while (!noCollision);
            }

            for(int i = 0; i < 8501; i += 1500){
                float xVal = (float) (Math.random() * 1800);
                checkpoints.add(new Checkpoint(xVal, i));
            }
        } else if(levelDesign == 2){
            //tutorial level
            List<Module> tutorialSequence = new ArrayList<>();
            tutorialSequence.add(getModuleByName("walk_and_run"));
            tutorialSequence.add(getModuleByName("small_jump"));
            tutorialSequence.add(getModuleByName("variable_jumps"));
            tutorialSequence.add(getModuleByName("momentum"));
            tutorialSequence.add(getModuleByName("vert_jumping_series"));
            tutorialSequence.add(getModuleByName("falls"));
            tutorialSequence.add(getModuleByName("wall_jumping"));
            tutorialSequence.add(getModuleByName("danger_boost_vertical"));
            tutorialSequence.add(getModuleByName("danger_boost_horizontal"));
            tutorialSequence.add(getModuleByName("precision_jumps"));
            platforms = buildLevelFromModules(tutorialSequence, new Vector2(0,0));
        }


        deathXValues = new ArrayList<>();
    }

    //REMEMBER: Everything in here is made 60 times a second!
    //Reward commiting to jumps
    //maybe in 'tutorial' say once you jump, you commit!

    @Override
    public void render(float delta) {
        clearScreen();
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) Gdx.app.exit();

        if(!isDead){
            updatePhysics(delta);
        } else{
            waterLevel += water_rise_speed * 10 * delta;
        }

        if(isDead || !deathXValues.isEmpty()){
            drawDeathText(delta);
            if(Gdx.input.isKeyJustPressed(Input.Keys.R) && isDead){
                restart();
            }
        }

        drawShapes();
        updateCamera(delta);
        drawHUD();
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
        for (Platform p : platforms){
            if (y + playerHeight < p.y || y > p.y + p.height) continue; //not same height, skip!

            //left of player, right of wall
            float distToRightWall = x - (p.x + p.width);
            if (distToRightWall >= 0 && distToRightWall < wallProximity && velocityX <= 0){
                touchingWallRight = true;
            }

            //right of player, left of wall
            float distToLeftWall = p.x - (x + playerWidth);
            if(distToLeftWall >= 0 && distToLeftWall < wallProximity && velocityX >= 0){
                touchingWallLeft = true;
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
                upForce = jump_force * .75f;
                sideForce = jump_force * .15f;
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
            water_rise_speed += delta / 2;
            waterLevel += water_rise_speed * delta;
        }

        //Checkpoints
        Rectangle playerRectFinal = new Rectangle(x, y, playerWidth, playerHeight);
        for(Checkpoint c : checkpoints){
            Rectangle checkpoint = new Rectangle(c.x, c.y, playerWidth * 2, playerHeight * 2);
            if(!c.activated && checkpoint.overlaps(playerRectFinal)){
                c.activated = true;
                if(currCheckpoint != null) {
                    if (c.y > currCheckpoint.y) {
                        currCheckpoint = c;
                        deathWaterLevel = waterLevel;
                    }
                } else{
                    currCheckpoint = c;
                    deathWaterLevel = waterLevel;
                }
            }
        }

        //death check
        Rectangle waterRect = new Rectangle (-1000, 0, 10000, waterLevel);

        if(waterRect.overlaps(playerRectFinal)){
            if(!isDead){
                deathXValues.add(x);
            }
            isDead = true;
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
        shapeRenderer.setColor(Color.WHITE);
        for(Platform p : platforms){
            shapeRenderer.rect(p.x, p.y, p.width, p.height);
        }

        if(levelDesign == 0) {
            //background load (Tiled)
            mapRenderer.setView(camera);
            mapRenderer.render();
        }

        //checkpoints
        for(Checkpoint c : checkpoints) {
            shapeRenderer.setColor(c.activated ? Color.GREEN : Color.CORAL); //TODO - change alpha!
            shapeRenderer.rect(c.x, c.y, playerWidth * 2, playerHeight * 2);
        }

        if(!isDead){
            shapeRenderer.setColor(Color.GOLD);
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
//            x = viewportWidth / 2;
            x = 0;
            y = 0;
            waterLevel = -50f;
            score = 0;
        }
        velocityY = 0;
        velocityX = 0;
        water_rise_speed = 8f;
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

    private void drawHUD(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();
        font.draw(batch, "Score: " + Math.round(score), viewportWidth - 100, viewportHeight - 10);
        font.draw(batch, "x:" + Math.round(x) + " y:" + Math.round(y), 10, viewportHeight - 10);
        batch.end();
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

    private List<Platform> buildLevelFromModules(List<Module> moduleSequence, Vector2 startPoint) {
        List<Platform> result = new ArrayList<>();
        Vector2 currFront = new Vector2(startPoint);

        for (Module m : moduleSequence) {
            for (Platform r : m.platforms) {
                result.add(new Platform(currFront.x + r.x, currFront.y + r.y, r.width, r.height));
            }
            currFront.add(m.endOffset);
        }

        return result;
    }

    private Module getModuleByName(String name) {
        for (Module m : ModuleLibrary.ALL_MODULES) {
            if (m.name.equals(name)) return m;
        }
        throw new RuntimeException("Module not found: " + name);
    }
}
