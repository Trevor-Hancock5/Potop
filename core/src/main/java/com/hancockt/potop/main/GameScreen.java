package com.hancockt.potop.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.hancockt.potop.assets.GameAssets;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.PersistentData;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;
import com.hancockt.potop.systems.*;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private final GameAssets gameAssets = new GameAssets();
    private RunData runData;
    private PersistentData persistentData;
    private PlayerData playerData;
    private long seed;

    RenderingSystem renderingSystem;
    CameraSystem cameraSystem;
    GameFlow gameFlow;
    LevelSystem levelSystem;
    WaterSystem waterSystem;
    FaucetSystem faucetSystem;
    CurrencySystem currencySystem;
    CheckpointSystem checkpointSystem;
    PhysicsEngine physicsEngine;
    LevelGeneration levelGeneration;

    @Override
    public void show() {
        Gdx.input.setCursorCatched(true);

        seed = System.currentTimeMillis();
        gameAssets.load();
        playerData = new PlayerData();
        levelGeneration = new LevelGeneration(seed);
        levelSystem = new LevelSystem();
        runData = levelSystem.loadLevel(LevelType.LEARNING_MODE);

        cameraSystem = new CameraSystem();
        renderingSystem = new RenderingSystem();
        gameFlow = new GameFlow();
        waterSystem = new WaterSystem();
        faucetSystem = new FaucetSystem();
        currencySystem = new CurrencySystem();
        checkpointSystem = new CheckpointSystem();
        physicsEngine = new PhysicsEngine();

        persistentData = new PersistentData();


        levelSystem.restart(runData, playerData, waterSystem, faucetSystem);
    }

    //REMEMBER: Everything in here is made 60 times a second!
    @Override
    public void render(float delta) {
        renderingSystem.clearScreen();
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        RunData temp = gameFlow.update(delta, runData, persistentData, playerData, gameAssets, renderingSystem, cameraSystem,
                                    levelSystem, waterSystem, faucetSystem, currencySystem, checkpointSystem,
                                    physicsEngine, levelGeneration);
        if(temp != null){
            runData = temp;
        }
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
