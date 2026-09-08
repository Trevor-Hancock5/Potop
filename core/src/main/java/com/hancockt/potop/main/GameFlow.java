package com.hancockt.potop.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.hancockt.potop.assets.AnimationKey;
import com.hancockt.potop.assets.GameAssets;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.GameState;
import com.hancockt.potop.state.PersistentData;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;
import com.hancockt.potop.systems.*;

public class GameFlow {
    public static GameState GAME_STATE = GameState.MENU; //start with menu

    public RunData update(float delta, RunData runData, PersistentData persistentData, PlayerData playerData,
                          GameAssets gameAssets,
                          RenderingSystem renderingSystem, CameraSystem cameraSystem, LevelSystem levelSystem,
                          WaterSystem waterSystem, FaucetSystem faucetSystem, CurrencySystem currencySystem,
                          CheckpointSystem checkpointSystem, PhysicsEngine physicsEngine, LevelGeneration levelGeneration){
        RunData tempRunData = null;
        switch(GAME_STATE) {
            case MENU:
                tempRunData = handleMenuInput(runData, playerData, persistentData, levelSystem, waterSystem, faucetSystem);
                renderingSystem.drawMenu(cameraSystem.getHudCam(), gameAssets);
                break;
            case PAUSE:
                tempRunData = handlePauseInput(runData, playerData, levelSystem, waterSystem,
                    faucetSystem, checkpointSystem, levelGeneration);
                renderingSystem.drawShapes(runData, playerData, gameAssets, cameraSystem.getCamera(), waterSystem, faucetSystem); //still draw game in background
                renderingSystem.drawHUD(runData, persistentData, playerData, cameraSystem.getHudCam(), runData.time);
                renderingSystem.drawPauseOverlay(cameraSystem.getHudCam());
                break;
            case DEATH:
                //update animations!
                gameAssets.checkpointAnimator.update(delta);
                gameAssets.sparkAnimator.update(delta);

                gameAssets.sparkAnimator.play(AnimationKey.DEATH);
                renderingSystem.drawShapes(runData, playerData, gameAssets, cameraSystem.getCamera(), waterSystem, faucetSystem);
                renderingSystem.drawHUD(runData, persistentData, playerData, cameraSystem.getHudCam(), runData.time);
                renderingSystem.drawDeathText(cameraSystem.getCamera(), runData.deathXValues, waterSystem);
                renderingSystem.drawDeathOverlay(cameraSystem.getHudCam(), playerData);
                tempRunData = handleDeathInput(runData, playerData, levelGeneration, levelSystem, waterSystem,
                    faucetSystem, checkpointSystem);
                break;
            case WIN:
                tempRunData = handleWinInput(runData, persistentData, playerData, levelSystem, waterSystem, faucetSystem,
                    checkpointSystem, levelGeneration);
                renderingSystem.drawShapes(runData, playerData, gameAssets, cameraSystem.getCamera(), waterSystem, faucetSystem);
                renderingSystem.drawHUD(runData, persistentData, playerData, cameraSystem.getHudCam(), runData.time);
                renderingSystem.drawWinOverlay(cameraSystem.getHudCam());
                break;
            case PLAYING:
                //update animations!
                gameAssets.checkpointAnimator.update(delta);
                gameAssets.sparkAnimator.update(delta);

                if(GameFlow.GAME_STATE != GameState.DEATH){
                    AnimationKey sparkAnimationState = physicsEngine.updatePhysics(delta, runData, persistentData, playerData,
                                                    cameraSystem, levelSystem, waterSystem, faucetSystem,
                                                    currencySystem, checkpointSystem, levelGeneration);
                    gameAssets.sparkAnimator.play(sparkAnimationState);
                }

                if(GameFlow.GAME_STATE == GameState.DEATH || !runData.deathXValues.isEmpty()){
                    renderingSystem.drawDeathText(cameraSystem.getCamera(), runData.deathXValues, waterSystem);
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
                    levelSystem.restart(runData, playerData, waterSystem, faucetSystem);
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
                    tempRunData = levelSystem.fullReset(runData, playerData, levelGeneration, waterSystem, faucetSystem,
                        checkpointSystem);
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
                    GAME_STATE = GameState.PAUSE;
                }

                renderingSystem.drawShapes(runData, playerData, gameAssets, cameraSystem.getCamera(), waterSystem, faucetSystem);
                cameraSystem.updateCamera(delta, playerData, waterSystem, physicsEngine);
                renderingSystem.drawHUD(runData, persistentData, playerData, cameraSystem.getHudCam(), runData.time);

                renderingSystem.instructions(runData, cameraSystem.getCamera());
                break;
        }

        return tempRunData;
    }

    private RunData handleMenuInput(RunData runData, PlayerData playerData, PersistentData persistentData, LevelSystem levelSystem,
                                    WaterSystem waterSystem, FaucetSystem faucetSystem){
        RunData tempRunData = null;
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            tempRunData = levelSystem.loadLevel(LevelType.LEARNING_MODE);
            levelSystem.restart(runData, playerData, waterSystem, faucetSystem);
            GAME_STATE = GameState.PLAYING;
        }


        if(Gdx.input.isKeyJustPressed(Input.Keys.I)){
            if(persistentData.learningModeBeat) {
                tempRunData = levelSystem.loadLevel(LevelType.INFINITE);
                levelSystem.restart(runData, playerData, waterSystem, faucetSystem);
                GAME_STATE = GameState.PLAYING;
            } else if(Gdx.input.isKeyJustPressed(Input.Keys.I)){
                //TODO - remove, just for testing!
                tempRunData = levelSystem.loadLevel(LevelType.INFINITE);
                levelSystem.restart(runData, playerData, waterSystem, faucetSystem);
                GAME_STATE = GameState.PLAYING;
            }
        }

        return tempRunData;
    }

    private RunData handlePauseInput(RunData runData, PlayerData playerData, LevelSystem levelSystem, WaterSystem waterSystem,
                                     FaucetSystem faucetSystem, CheckpointSystem checkpointSystem, LevelGeneration levelGeneration){
        RunData tempRunData = null;
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
            GAME_STATE = GameState.PLAYING;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.M)){
            tempRunData = levelSystem.fullReset(runData, playerData, levelGeneration, waterSystem, faucetSystem,
                checkpointSystem);
            GAME_STATE = GameState.MENU;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            GAME_STATE = GameState.PLAYING;
            levelSystem.restart(runData, playerData, waterSystem, faucetSystem);
        }

        return tempRunData;
    }

    private RunData handleDeathInput(RunData runData, PlayerData playerData, LevelGeneration levelGeneration, LevelSystem levelSystem, WaterSystem waterSystem,
                                     FaucetSystem faucetSystem, CheckpointSystem checkpointSystem){
        RunData tempRunData = null;
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            levelSystem.restart(runData, playerData, waterSystem, faucetSystem);
            GAME_STATE = GameState.PLAYING;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            tempRunData = levelSystem.fullReset(runData, playerData, levelGeneration, waterSystem, faucetSystem,
                checkpointSystem);
            GAME_STATE = GameState.PLAYING;
        }

        return tempRunData;
    }

    private RunData handleWinInput(RunData runData, PersistentData persistentData, PlayerData playerData, LevelSystem levelSystem,
                                   WaterSystem waterSystem, FaucetSystem faucetSystem, CheckpointSystem checkpointSystem,
                                   LevelGeneration levelGeneration){
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
                checkpointSystem.reset(runData);
                RunData nextLevel = LevelGeneration.getAllLevels().get(next);
                playerData.x = nextLevel.getStartPoint().x;
                playerData.y = nextLevel.getStartPoint().y;
                runData.setCurrentLevel(next);
                tempRunData = levelSystem.loadLevel(next);
                levelSystem.restart(runData, playerData, waterSystem, faucetSystem);
                GAME_STATE = GameState.PLAYING;
            } else{
                GAME_STATE = GameState.MENU;
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            tempRunData = levelSystem.fullReset(runData, playerData, levelGeneration, waterSystem, faucetSystem,
                checkpointSystem);
            GAME_STATE = GameState.PLAYING;
        }

        return tempRunData;
    }
}
