package com.hancockt.potop.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.hancockt.potop.assets.AnimationKey;
import com.hancockt.potop.assets.GameAssets;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.GameState;
import com.hancockt.potop.state.RunData;
import com.hancockt.potop.systems.*;

public class GameFlow {
    public static GameState GAME_STATE = GameState.MENU; //start with menu
    private final GameAssets gameAssets;
    private final RenderingSystem renderingSystem;
    private final CameraSystem cameraSystem;
    private final LevelSystem levelSystem;
    private final WaterSystem waterSystem;
    private final FaucetSystem faucetSystem;
    private final PhysicsEngine physicsEngine;

    public GameFlow(GameAssets gameAssets, RenderingSystem renderingSystem, CameraSystem cameraSystem,
                    LevelSystem levelSystem, WaterSystem waterSystem, FaucetSystem faucetSystem,
                    PhysicsEngine physicsEngine) {
        this.gameAssets = gameAssets;
        this.renderingSystem = renderingSystem;
        this.cameraSystem = cameraSystem;
        this.levelSystem = levelSystem;
        this.waterSystem = waterSystem;
        this.faucetSystem = faucetSystem;
        this.physicsEngine = physicsEngine;
    }

    public RunData update(FrameRuntimeContext frameRuntimeContext){
        var runData = frameRuntimeContext.runData();
        var persistentData = frameRuntimeContext.persistentData();
        var playerData = frameRuntimeContext.playerData();
        float delta = frameRuntimeContext.delta();

        RunData tempRunData = null;
        switch(GAME_STATE) {
            case MENU:
                tempRunData = handleMenuInput(frameRuntimeContext);
                renderingSystem.drawMenu(cameraSystem.getHudCam(), gameAssets);
                break;
            case PAUSE:
                tempRunData = handlePauseInput(frameRuntimeContext);
                renderingSystem.drawShapes(buildRenderWorldView(frameRuntimeContext), gameAssets, cameraSystem.getCamera()); //still draw game in background
                renderingSystem.drawHUD(buildHudView(frameRuntimeContext), cameraSystem.getHudCam());
                renderingSystem.drawPauseOverlay(cameraSystem.getHudCam());
                break;
            case DEATH:
                //update animations!
                gameAssets.checkpointAnimator.update(delta);
                gameAssets.sparkAnimator.update(delta);

                gameAssets.sparkAnimator.play(AnimationKey.DEATH);
                renderingSystem.drawShapes(buildRenderWorldView(frameRuntimeContext), gameAssets, cameraSystem.getCamera());
                renderingSystem.drawHUD(buildHudView(frameRuntimeContext), cameraSystem.getHudCam());
                renderingSystem.drawDeathText(cameraSystem.getCamera(), runData.deathXValues, waterSystem.getWaterLevel());
                renderingSystem.drawDeathOverlay(cameraSystem.getHudCam(), playerData);
                tempRunData = handleDeathInput(frameRuntimeContext);
                break;
            case WIN:
                tempRunData = handleWinInput(frameRuntimeContext);
                renderingSystem.drawShapes(buildRenderWorldView(frameRuntimeContext), gameAssets, cameraSystem.getCamera());
                renderingSystem.drawHUD(buildHudView(frameRuntimeContext), cameraSystem.getHudCam());
                renderingSystem.drawWinOverlay(cameraSystem.getHudCam());
                break;
            case PLAYING:
                //update animations!
                gameAssets.checkpointAnimator.update(delta);
                gameAssets.sparkAnimator.update(delta);

                if(GameFlow.GAME_STATE != GameState.DEATH){
                    AnimationKey sparkAnimationState = physicsEngine.updatePhysics(frameRuntimeContext);
                    gameAssets.sparkAnimator.play(sparkAnimationState);
                }

                if(GameFlow.GAME_STATE == GameState.DEATH || !runData.deathXValues.isEmpty()){
                    renderingSystem.drawDeathText(cameraSystem.getCamera(), runData.deathXValues, waterSystem.getWaterLevel());
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
                    levelSystem.restart(runData, playerData);
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
                    tempRunData = levelSystem.fullReset(runData, playerData);
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
                    GAME_STATE = GameState.PAUSE;
                }

                renderingSystem.drawShapes(buildRenderWorldView(frameRuntimeContext), gameAssets, cameraSystem.getCamera());
                cameraSystem.updateCamera(new CameraUpdateContext(delta, playerData, GAME_STATE,
                    waterSystem.getWaterLevel(), PhysicsEngine.maxFallSpeed));
                renderingSystem.drawHUD(buildHudView(frameRuntimeContext), cameraSystem.getHudCam());

                renderingSystem.instructions(runData, cameraSystem.getCamera());
                break;
        }

        return tempRunData;
    }

    private RunData handleMenuInput(FrameRuntimeContext frameRuntimeContext){
        var runData = frameRuntimeContext.runData();
        var playerData = frameRuntimeContext.playerData();
        var persistentData = frameRuntimeContext.persistentData();
        RunData tempRunData = null;
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            tempRunData = levelSystem.loadLevel(LevelType.LEARNING_MODE);
            levelSystem.restart(runData, playerData);
            GAME_STATE = GameState.PLAYING;
        }


        if(Gdx.input.isKeyJustPressed(Input.Keys.I)){
            if(persistentData.learningModeBeat) {
                tempRunData = levelSystem.loadLevel(LevelType.INFINITE);
                levelSystem.restart(runData, playerData);
                GAME_STATE = GameState.PLAYING;
            } else if(Gdx.input.isKeyJustPressed(Input.Keys.I)){
                //TODO - remove, just for testing!
                tempRunData = levelSystem.loadLevel(LevelType.INFINITE);
                levelSystem.restart(runData, playerData);
                GAME_STATE = GameState.PLAYING;
            }
        }

        return tempRunData;
    }

    private RunData handlePauseInput(FrameRuntimeContext frameRuntimeContext){
        var runData = frameRuntimeContext.runData();
        var playerData = frameRuntimeContext.playerData();
        RunData tempRunData = null;
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
            GAME_STATE = GameState.PLAYING;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.M)){
            tempRunData = levelSystem.fullReset(runData, playerData);
            GAME_STATE = GameState.MENU;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            GAME_STATE = GameState.PLAYING;
            levelSystem.restart(runData, playerData);
        }

        return tempRunData;
    }

    private RunData handleDeathInput(FrameRuntimeContext frameRuntimeContext){
        var runData = frameRuntimeContext.runData();
        var playerData = frameRuntimeContext.playerData();
        RunData tempRunData = null;
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            levelSystem.restart(runData, playerData);
            GAME_STATE = GameState.PLAYING;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            tempRunData = levelSystem.fullReset(runData, playerData);
            GAME_STATE = GameState.PLAYING;
        }

        return tempRunData;
    }

    private RunData handleWinInput(FrameRuntimeContext frameRuntimeContext){
        var runData = frameRuntimeContext.runData();
        var persistentData = frameRuntimeContext.persistentData();
        var playerData = frameRuntimeContext.playerData();
        RunData tempRunData = null;
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            runData.time = 0f;
            runData.timeAtDeath = 0f;
            RunData data = LevelGeneration.getAllLevels().get(runData.getCurrentLevel());
            LevelType next = data.getNextLevel();
            if(next != null){
                if(runData.getCurrentLevel() == LevelType.LEARNING_MODE){
                    persistentData.learningModeBeat = true;
                }
                waterSystem.updateCheckpointWaterLevel(runData, true);
                levelSystem.resetCheckpointProgress(runData);
                RunData nextLevel = LevelGeneration.getAllLevels().get(next);
                playerData.x = nextLevel.getStartPoint().x;
                playerData.y = nextLevel.getStartPoint().y;
                runData.setCurrentLevel(next);
                tempRunData = levelSystem.loadLevel(next);
                levelSystem.restart(runData, playerData);
                GAME_STATE = GameState.PLAYING;
            } else{
                GAME_STATE = GameState.MENU;
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            tempRunData = levelSystem.fullReset(runData, playerData);
            GAME_STATE = GameState.PLAYING;
        }

        return tempRunData;
    }

    private RenderWorldView buildRenderWorldView(FrameRuntimeContext frameRuntimeContext) {
        return new RenderWorldView(frameRuntimeContext.runData(),
            frameRuntimeContext.playerData(), waterSystem.getWaterLevel(), faucetSystem.getFaucetInteractionTime());
    }

    private HudView buildHudView(FrameRuntimeContext frameRuntimeContext) {
        return new HudView(frameRuntimeContext.runData(), frameRuntimeContext.persistentData(),
            frameRuntimeContext.playerData(), frameRuntimeContext.runData().time);
    }
}
