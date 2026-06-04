package com.hancockt.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {
    ShapeRenderer shapeRenderer;
    float x,y;
    float velocityY;
    float velocityX;
    boolean onGround = false;

    int accelerationX = 1500;
    int maxSpeedX = 350;
    float groundFriction = 0.93f;
    float airFriction = 0.999f;

    OrthographicCamera camera;
    Viewport viewport;

    List<Platform> platforms;

    @Override
    public void show() {
        // Prepare your screen here.
        shapeRenderer = new ShapeRenderer();
        x = 100;
        y = 150;

        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);

        platforms = new ArrayList<>();
        platforms.add(new Platform(100, 100, 50, 10));
        platforms.add(new Platform(300, 75, 50, 10));
        platforms.add(new Platform(600, 30, 50, 10));
    }

    //REMEMBER: Everything in here is made 60 times a second!
    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Reward commiting to jumps
        //maybe in 'tutorial' say once you jump, you commit!

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && onGround) velocityX -= accelerationX * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && onGround) velocityX += accelerationX * delta;

        //groundFriction
        if(onGround) {
            velocityX *= groundFriction;
        } else{
            velocityX *= airFriction;
        }
        //cap speed
        velocityX = Math.max(-maxSpeedX, Math.min(maxSpeedX, velocityX));

        x += velocityX * delta;

        if(y <= 0) { onGround = true; y=0; velocityY = 0; }
        if(Gdx.input.isKeyPressed(Input.Keys.UP) && onGround) { velocityY = 325; onGround = false; }
        if(Gdx.input.isKeyPressed(Input.Keys.UP) && !onGround && velocityY > 0) velocityY += 200*delta;
        velocityY -= 600*delta; //gravity
        y += velocityY*delta;

        camera.position.set(400, Math.max(y + 25, 240), 0);
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        Rectangle playerRect = new Rectangle(x, y, 50, 50);

        shapeRenderer.rect(x,y,50,50);

        //TODO - make it so that player hits bottom and side of platforms!
        for(Platform p : platforms){
            shapeRenderer.rect(p.x, p.y, p.width, p.height);
            Rectangle platformRect = new Rectangle(p.x, p.y, p.width, p.height);

            if(playerRect.overlaps(platformRect) && y > p.y && velocityY < 0){
                onGround = true;
                y = p.y + p.height;
                velocityY = 0;
            }
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        shapeRenderer.dispose();
    }
}
