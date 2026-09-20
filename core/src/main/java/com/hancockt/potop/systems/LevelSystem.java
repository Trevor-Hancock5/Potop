package com.hancockt.potop.systems;

import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

import javax.swing.*;
import java.util.ArrayList;

public class LevelSystem {

    public void restart(RunData runData, PlayerData playerData, WaterSystem waterSystem, FaucetSystem faucetSystem) {
        waterSystem.restart(runData, playerData);

        runData.time = runData.timeAtDeath;

        faucetSystem.reset(runData);
    }

    public RunData loadLevel(LevelType levelType) {
        return LevelGeneration.getAllLevels().get(levelType);
    }

    public RunData fullReset(RunData runData, PlayerData playerData, LevelGeneration levelGeneration, WaterSystem waterSystem,
                             FaucetSystem faucetSystem, CheckpointSystem checkpointSystem){
        // Called when user hits 'Q'
        long seed = System.currentTimeMillis();
        RunData tempRunData = null;
        if (runData.getCurrentLevel() == LevelType.INFINITE) {
            waterSystem.updateCheckpointWaterLevel(runData, true);
            runData.deathXValues = new ArrayList<>(); //reset
            levelGeneration.regenerateInfinite(seed);
            tempRunData = loadLevel(LevelType.INFINITE); //regenerate!
        }
        if(tempRunData != null){
            tempRunData.seed = seed;
            tempRunData.timeAtDeath = 0f;
            tempRunData.time = 0f;
            checkpointSystem.reset(tempRunData);
            restart(tempRunData, playerData, waterSystem, faucetSystem);
            playerData.x = 0;
            playerData.y = 8000;
            return tempRunData;
        } else {
            runData.seed = seed;
            runData.timeAtDeath = 0f;
            runData.time = 0f;
            checkpointSystem.reset(runData);
            restart(runData, playerData, waterSystem, faucetSystem);
            return runData;
        }
    }

    public void updateInfinite(RunData runData, PlayerData playerData, LevelGeneration levelGeneration){
        if(runData.getCurrentLevel() == LevelType.INFINITE) {
            if (playerData.y > levelGeneration.getHighestYInInfinite() - LevelGeneration.GENERATION_HEIGHT) {
                levelGeneration.addToInfiniteLevel(runData.getPlatforms(), runData.getCheckpoints(),
                    runData.getFaucets(), runData.getCurrencies(), runData.rng);
            }
        }
    }
}
