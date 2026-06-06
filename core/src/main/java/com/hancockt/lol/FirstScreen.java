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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

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

    //Player
    float x,y;
    float velocityY;
    float velocityX;
    boolean onGround = false;
    boolean isDead = false;
    float score;
    float coyoteSeconds; //number of seconds off ground
    float jumpBuffer; // number of seconds since pressed jump
    boolean justLanded = false; //for screen shake

    //PLAYER TUNING!
    int accelerationX = 1500;
    int maxSpeedX = 400;
    int maxFallSpeed = -1200;
    float groundFriction = 0.93f;
    float airFriction = 0.999f;
    float gravity = 900f;
    float jump_force = 400f;
    float jump_hold_force = 225f;
    int playerWidth = 20;
    int playerHeight = 50;
    float cameraLerpSpeed = .1f;
    float cameraDeadZoneDivisor = 5;
    float coyoteThreshold = 0.1f; //Can be off platform for x seconds and still jump
    float jumpBufferThreshold = 0.1f;
    float downforceFallAcceleration = -250f;
    float screenShake = 0.2f; //time of shaking
    float screenShakeIntensity = 5f;

    //water
    float waterLevel;
    float water_rise_speed;

    List<Platform> platforms;
    List<Float> deathXValues;

    //game reference
    lol game;


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

        platforms = new ArrayList<>();
        platforms.add(new Platform(100, 100, 80, 10));
        platforms.add(new Platform(300, 75, 80, 10));
        platforms.add(new Platform(600, 30, 80, 10));

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
            waterLevel += water_rise_speed * 5 * delta;
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
        //gravity
        float currGravity = velocityY < 0 ? gravity * 1.1f : gravity;
        velocityY -= currGravity * delta;

        //Holding jump
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && !onGround && velocityY > 0) {
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

        //Reset to avoid jumping mid-air
        onGround = false;

        //floor collision
        if (y <= 0) {
            onGround = true;
            y = 0;
            velocityY = 0;
            justLanded = true;
        }

        //platform collision
        Rectangle playerRect = new Rectangle(x, y, playerWidth, playerHeight);

        for (Platform p : platforms) {
            Rectangle platformRect = new Rectangle(p.x, p.y, p.width, p.height);

            if (playerRect.overlaps(platformRect) && velocityY < 0 && y + ((float) playerHeight / 5) >= p.y) {
                y = p.y + p.height;
                velocityY = 0;
                onGround = true;
                justLanded = true;
            }
        }

        //Coyote timer
        if(!onGround){
            coyoteSeconds += delta;
        } else{
            coyoteSeconds = 0;
        }

        //danger boost
        float distanceFromWater = y - waterLevel;
        float dangerBoost = Math.max (0, 1 - (distanceFromWater / 300f));
        dangerBoost = 1 + (dangerBoost / 4);

        //horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && onGround){
            velocityX -= accelerationX * delta * dangerBoost;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && onGround) {
            velocityX += accelerationX * delta * dangerBoost;
        }

        //friction & velocity cap
        velocityX *= onGround ? currFriction : airFriction;
        velocityX = Math.max(-maxSpeedX, Math.min(maxSpeedX, velocityX));

        jumpBuffer += delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
            jumpBuffer = 0; //just tried to jump
        }

        //Jumping
        if ((Gdx.input.isKeyJustPressed(Input.Keys.UP) || jumpBuffer < jumpBufferThreshold)
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
        waterLevel += water_rise_speed * delta;

        //death check
        Rectangle playerRectFinal = new Rectangle(x, y, playerWidth, playerHeight);
        Rectangle waterRect = new Rectangle (-1000, 0, 10000, waterLevel);

        if(waterRect.overlaps(playerRectFinal)){
            if(!isDead){
                deathXValues.add(x);
            }
            isDead = true;
        }

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
        camera.position.y += (targetY - camera.position.y) * cameraLerpSpeed;

        if(justLanded && screenShakeAccumulation < screenShake){
            screenShakeAccumulation += delta;
            camera.position.x += (Math.random() - 0.5) * screenShakeIntensity;
            camera.position.y += (Math.random() - 0.5) * screenShakeIntensity;
        }
        camera.update();
    }

    private void drawShapes(){
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);


        if(!isDead){
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(x, y, playerWidth, playerHeight);
        }

        //platforms
        shapeRenderer.setColor(Color.WHITE);
        for(Platform p : platforms){
            shapeRenderer.rect(p.x, p.y, p.width, p.height);
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
        x = 25;
        y = 10;
        velocityY = 0;
        velocityX = 0;
        waterLevel = -50f;
        water_rise_speed = 5f;
        onGround = false;
        isDead = false;
        score = 0;
        coyoteSeconds = 100; //reset
        jumpBuffer = 100; //reset
        justLanded = false; //reset
        screenShakeAccumulation = 0;
    }

    private void drawHUD(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();
        font.draw(batch, "Score: " + Math.round(score), viewportWidth - 100, viewportHeight - 10);
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
}
