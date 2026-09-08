package com.hancockt.potop.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hancockt.potop.assets.AnimationKey;
import com.hancockt.potop.assets.GameAssets;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.GameState;
import com.hancockt.potop.state.PersistentData;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;
import com.hancockt.potop.systems.*;

import java.util.ArrayList;
import java.util.List;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private final GameAssets gameAssets = new GameAssets();

    public static GameState gameState = GameState.MENU; //start with menu

    private static RunData runData;
    public static List<Float> deathXValues;
    private boolean pressedI = false;
    private boolean pressedN = false;
    public static float time = 0f;
    public static float timeAtDeath = 0f;

    CameraSystem cameraSystem;
    RenderingSystem renderingSystem;

    // Controls
    private static boolean enteringSeed = false;


    public GameScreen(){}

    @Override
    public void show() {
        Gdx.input.setCursorCatched(true);

        gameAssets.load();

        cameraSystem = new CameraSystem();
        renderingSystem = new RenderingSystem();

        deathXValues = new ArrayList<>();

        loadLevel(LevelType.LEARNING_MODE);
        restart();
    }

    //REMEMBER: Everything in here is made 60 times a second!

    @Override
    public void render(float delta) {
        renderingSystem.clearScreen();
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        switch(gameState) {
            case MENU:
                handleMenuInput();
                renderingSystem.drawMenu(cameraSystem.getHudCam(), gameAssets);
                break; //skip everything else
            case PAUSE:
                handlePauseInput();
                renderingSystem.drawShapes(runData, gameAssets, cameraSystem.getCamera()); //still draw game in background
                renderingSystem.drawHUD(runData, cameraSystem.getHudCam(), time);
                renderingSystem.drawPauseOverlay(cameraSystem.getHudCam());
                break;
            case DEATH:
                //update animations!
                gameAssets.checkpointAnimator.update(delta);
                gameAssets.sparkAnimator.update(delta);

                gameAssets.sparkAnimator.play(AnimationKey.DEATH);
                renderingSystem.drawShapes(runData, gameAssets, cameraSystem.getCamera());
                renderingSystem.drawHUD(runData, cameraSystem.getHudCam(), time);
                renderingSystem.drawDeathText(cameraSystem.getCamera(), deathXValues);
                renderingSystem.drawDeathOverlay(cameraSystem.getHudCam());
                handleDeathInput();
                break;
            case WIN:
                handleWinInput();
                renderingSystem.drawShapes(runData, gameAssets, cameraSystem.getCamera());
                renderingSystem.drawHUD(runData, cameraSystem.getHudCam(), time);
                renderingSystem.drawWinOverlay(cameraSystem.getHudCam());
                break;
            case PLAYING:
                //update animations!
                gameAssets.checkpointAnimator.update(delta);
                gameAssets.sparkAnimator.update(delta);

                if(!PlayerData.isDead){
                    AnimationKey sparkAnimationState = PhysicsEngine.updatePhysics(delta, runData, cameraSystem);
                    gameAssets.sparkAnimator.play(sparkAnimationState);
                }

                if(PlayerData.isDead || !deathXValues.isEmpty()){
                    renderingSystem.drawDeathText(cameraSystem.getCamera(), deathXValues);
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
                    restart();
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
                    onQ();
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
                    gameState = GameState.PAUSE;
                }

                //infinite mode seed input
                if(runData.getCurrentLevel() == LevelType.INFINITE){
                    if(Gdx.input.isKeyJustPressed(Input.Keys.T)){
                        enteringSeed = true;
                    }
                }
                renderingSystem.drawShapes(runData, gameAssets, cameraSystem.getCamera());
                cameraSystem.updateCamera(delta);
                renderingSystem.drawHUD(runData, cameraSystem.getHudCam(), time);

                renderingSystem.instructions(runData, cameraSystem.getCamera());
                break;
        }

    }

    private void restart() {
        WaterSystem.restart(runData);

        time = timeAtDeath;
        cameraSystem.resetScreenShake();

        FaucetSystem.reset(runData);
    }

    private void loadLevel(LevelType levelType) {
        runData = LevelGeneration.getAllLevels().get(levelType);
        PlayerData.x = runData.getStartPoint().x;
        PlayerData.y = runData.getStartPoint().y;
    }

    private void handleMenuInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            loadLevel(LevelType.LEARNING_MODE);
            restart();
            gameState = GameState.PLAYING;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.I)){
            if(PersistentData.learningModeBeat) {
                loadLevel(LevelType.INFINITE);
                restart();
                gameState = GameState.PLAYING;
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
            gameState = GameState.PLAYING;
        }
    }

    private void handlePauseInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
            gameState = GameState.PLAYING;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.M)){
            onQ();
            gameState = GameState.MENU;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            gameState = GameState.PLAYING;
            restart();
        }
    }

    private void handleDeathInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            restart();
            gameState = GameState.PLAYING;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            onQ();
            gameState = GameState.PLAYING;
        }
    }

    private void handleWinInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            time = 0f;
            timeAtDeath = 0f;
            RunData data = LevelGeneration.getAllLevels().get(runData.getCurrentLevel());
            LevelType next = data.getNextLevel();
            if(next != null){
                if(runData.getCurrentLevel() == LevelType.LEARNING_MODE){
                    PersistentData.learningModeBeat = true;
                }
                WaterSystem.updateCheckpointWaterLevel(runData, -50f);
                CheckpointSystem.reset(runData);
                RunData nextLevel = LevelGeneration.getAllLevels().get(next);
                PlayerData.x = nextLevel.getStartPoint().x;
                PlayerData.y = nextLevel.getStartPoint().y;
                runData.setCurrentLevel(next);
                loadLevel(next);
                restart();
                gameState = GameState.PLAYING;
            } else{
                gameState = GameState.MENU;
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            onQ();
            gameState = GameState.PLAYING;
        }
    }

    private void onQ(){
        if (runData.getCurrentLevel() == LevelType.INFINITE) {
            WaterSystem.updateCheckpointWaterLevel(runData, -50f);
            deathXValues = new ArrayList<>(); //reset
            LevelGeneration.regenerateInfinite();
            loadLevel(LevelType.INFINITE); //regenerate!
        }
        timeAtDeath = 0f;
        time = 0f;
        CheckpointSystem.reset(runData);
        restart();
    }

    @Override
    public void resize(int width, int height) {
        if(width <= 0 || height <= 0) {
            return;
        }

        cameraSystem.resize(width, height);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        renderingSystem.dispose();
        gameAssets.dispose();
    }
}
