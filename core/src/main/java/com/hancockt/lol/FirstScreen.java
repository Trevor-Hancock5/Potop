package com.hancockt.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {
    ShapeRenderer shapeRenderer;
    float x,y;
    float velocityY;
    boolean onGround = false;

    OrthographicCamera camera;
    Viewport viewport;

    @Override
    public void show() {
        // Prepare your screen here.
        shapeRenderer = new ShapeRenderer();
        x = 100;
        y = 150;

        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
    }

    @Override
    public void render(float delta) {

        // Draw your screen here. "delta" is the time since last render in seconds.
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //make it so that it gradually slows down (not dead stop! especially in air!)
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) x -= 200*delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) x += 200*delta;

        if(y <= 0) { onGround = true; y=0; velocityY = 0; }
        if(Gdx.input.isKeyPressed(Input.Keys.UP) && onGround) { velocityY = 300; onGround = false; }
        velocityY -= 600*delta; //gravity
        y += velocityY*delta;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x,y,50,50);
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
