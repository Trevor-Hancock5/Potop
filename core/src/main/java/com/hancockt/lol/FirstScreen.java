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
    float velocityY;
    float velocityX;
    boolean onGround = false;
    boolean isDead = false;
    float score;
    float coyoteSeconds; //number of seconds off ground
    float jumpBuffer; // number of seconds since pressed jump
    boolean justLanded = false; //for screen shake
    float impactVelocity; //velocity when landing! use for screen shake

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
    float bounceRatio = .5f;
    float airControlDivisor = 6f;
    float wallSlideDivisor = 2; //how fast slows down vertically when sliding down wall
    float wallJumpRatio = 0.5f; // how much horizontal velocity is maintained
    float wallSlideThreshold = 5; // how close to wall to jump

    //water
    float waterLevel;
    float water_rise_speed;

    List<Platform> platforms;
    List<Float> deathXValues;

    //game reference
    lol game;

    // Controls
    boolean levelDesign = true;
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

        //Set up platforms
        platforms = new ArrayList<>();

        if(levelDesign) {
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
        } else {
            platforms.add(new Platform(-40, 0, 40, 9000));
            platforms.add(new Platform(2000, 0, 40, 9000));

            for (int i = 0; i < 750; i++) {
                boolean noCollision;
                do {
                    int xVal = (int) (Math.random() * 2000);
                    int Yval = (int) (Math.random() * 8000);
                    int width = (int) (Math.random() * 175 + 25);
                    int height = (int) (Math.random() * 30 + 10);
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
        }

        //platform collision
        Rectangle playerRect = new Rectangle(x, y, playerWidth, playerHeight);

        for (Platform p : platforms) {
            Rectangle platformRect = new Rectangle(p.x, p.y, p.width, p.height);

            //Player lands on top
            float collisionPoint = p.y;
            if(p.height > collisionThreshold){
                collisionPoint = p.height + p.y - collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && velocityY < 0 && y + ((float) playerHeight / 5) >= collisionPoint) {
                impactVelocity = Math.abs(velocityY);
                y = p.y + p.height;
                velocityY = 0;
                onGround = true;
                justLanded = true;
            }
            //left of player hits right side of wall when moving left
            collisionPoint = p.x + (float) (p.width / 2);
            if ((float) p.width / 2 > collisionThreshold) {
                collisionPoint = p.x + p.width - collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && velocityX < 0 && x + (float) (playerWidth / 2) >= collisionPoint
                    && y + (float)(playerHeight / 2) < p.y + p.height) {
                x = p.x + p.width;
                velocityX = bounceRatio * -velocityX; //TODO - change this for when doing wall jumps!
            }


            //right of player hits left side of wall when moving right
            collisionPoint = p.x + (float) (p.width / 2);
            if ((float) p.width / 2 > collisionThreshold) {
                collisionPoint = p.x + collisionThreshold;
            }
            if (playerRect.overlaps(platformRect) && velocityX > 0 && x + (float) (playerWidth / 2) <= collisionPoint
                    && y + (float)(playerHeight / 2) < p.y + p.height) {
                x = p.x - playerWidth;
                velocityX = bounceRatio * -velocityX; //TODO - change this for when doing wall jumps!
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

        jumpBuffer += delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            jumpBuffer = 0; //just tried to jump
        }

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


        if(!isDead){
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(x, y, playerWidth, playerHeight);
        }

        //platforms
        shapeRenderer.setColor(Color.WHITE);
        for(Platform p : platforms){
            shapeRenderer.rect(p.x, p.y, p.width, p.height);
        }

        if(levelDesign) {
            //background load (Tiled)
            mapRenderer.setView(camera);
            mapRenderer.render();
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
        x = viewportWidth / 2;
        y = 0;
        velocityY = 0;
        velocityX = 0;
        waterLevel = -50f;
        water_rise_speed = 8f;
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
