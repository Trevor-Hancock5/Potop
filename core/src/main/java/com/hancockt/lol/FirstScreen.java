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
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

//TODO - make an initial small level!
//TODO - death screen
//TODO - menu screen
//TODO - when restart, keep all the lol on the surface of the water haha

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {
    //Rendering
    ShapeRenderer shapeRenderer;
    SpriteBatch batch;
    BitmapFont font;
    OrthographicCamera camera;
    Viewport viewport;

    //Player
    float x,y;
    float velocityY;
    float velocityX;
    boolean onGround = false;
    boolean isDead = false;

    //PLAYER TUNING!
    int accelerationX = 1500;
    int maxSpeedX = 350;
    float groundFriction = 0.93f;
    float airFriction = 0.999f;
    float gravity = 600f;
    float jump_force = 325f;
    float jump_hold_force = 200f;
    int playerWidth = 50;
    int playerHeight = 50;

    //water
    float waterLevel = -100f;
    float water_rise_speed = 5f;

    List<Platform> platforms;

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
        viewport = new ExtendViewport(800, 480, camera);

        x = 100;
        y = 150;

        platforms = new ArrayList<>();
        platforms.add(new Platform(100, 100, 50, 10));
        platforms.add(new Platform(300, 75, 50, 10));
        platforms.add(new Platform(600, 30, 50, 10));
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

        if(isDead){
            drawDeathText(delta);
        }

        drawShapes();
        updateCamera();
    }

    private void clearScreen(){
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    }

    private void updatePhysics(float delta){
        //reset onGround so can't jump in midair
        onGround = false;

        //floor collision
        if (y <= 0) {
            onGround = true;
            y = 0;
            velocityY = 0;
        }

        //platform collision
        Rectangle playerRect = new Rectangle(x, y, playerWidth, playerHeight);

        for (Platform p : platforms) {
            Rectangle platformRect = new Rectangle(p.x, p.y, p.width, p.height);

            if (playerRect.overlaps(platformRect) && velocityY < 0 && y > p.y + ((float) p.height / 2)) {
                y = p.y + p.height;
                velocityY = 0;
                onGround = true;
            }
        }

        //horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && onGround){
            velocityX -= accelerationX * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && onGround) {
            velocityX += accelerationX * delta;
        }

        //friction & velocity updates
        velocityX *= onGround ? groundFriction : airFriction;
        velocityX = Math.max (-maxSpeedX, Math.min(maxSpeedX, velocityX));
        x += velocityX*delta;


        //Jumping
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && onGround) {
            velocityY = jump_force;
            onGround = false;
        }
        //Holding jump
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && !onGround && velocityY > 0) {
            velocityY += jump_hold_force * delta;
        }

        //gravity
        velocityY -= gravity * delta; //gravity
        y += velocityY * delta;

        //rising water
        waterLevel += water_rise_speed * delta;

        //death check
        Rectangle playerRectFinal = new Rectangle(x, y, playerWidth, playerHeight);
        Rectangle waterRect = new Rectangle (-1000, 0, 10000, waterLevel);

        if(waterRect.overlaps(playerRectFinal)){
            isDead = true;
        }
    }

    private void updateCamera() {
        camera.position.set(400, Math.max(y+25, 240), 0);
        camera.update();
    }

    private void drawShapes(){
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);


        if(!isDead){
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(x, y, 50, 50);
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
        float bob = 0f; // how much to bob
        if(Math.random() > 0.8){
            bob = (float) ((5*Math.random()) - 1);
        }
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.draw(batch, "lol", x, waterLevel + 10 + bob);
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
