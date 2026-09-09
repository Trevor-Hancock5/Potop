package com.hancockt.potop.systems;

import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

import java.util.ArrayList;

public class LevelSystem {
    private final LevelGeneration levelGeneration;
    private final WaterSystem waterSystem;
    private final FaucetSystem faucetSystem;
    private final CheckpointSystem checkpointSystem;

    public LevelSystem(LevelGeneration levelGeneration, WaterSystem waterSystem, FaucetSystem faucetSystem,
                       CheckpointSystem checkpointSystem) {
        this.levelGeneration = levelGeneration;
        this.waterSystem = waterSystem;
        this.faucetSystem = faucetSystem;
        this.checkpointSystem = checkpointSystem;
    }

    public void restart(RunData runData, PlayerData playerData) {
        waterSystem.restart(runData, playerData);

        runData.time = runData.timeAtDeath;

        faucetSystem.reset(runData);
    }

    public RunData loadLevel(LevelType levelType) {
        return LevelGeneration.getAllLevels().get(levelType);
    }

    public void resetCheckpointProgress(RunData runData) {
        checkpointSystem.reset(runData);
    }

    public RunData fullReset(RunData runData, PlayerData playerData){
        // Called when user hits 'Q'
        long seed = System.currentTimeMillis();
        RunData tempRunData = null;
        if (runData.getCurrentLevel() == LevelType.INFINITE) {
            waterSystem.updateCheckpointWaterLevel(runData, true);
            runData.deathXValues = new ArrayList<>(); //reset
            levelGeneration.regenerateInfinite(seed, playerData);
            tempRunData = loadLevel(LevelType.INFINITE); //regenerate!
        }
        if(tempRunData != null){
            tempRunData.seed = seed;
            tempRunData.timeAtDeath = 0f;
            tempRunData.time = 0f;
            checkpointSystem.reset(tempRunData);
            restart(tempRunData, playerData);
            return tempRunData;
        } else {
            runData.seed = seed;
            runData.timeAtDeath = 0f;
            runData.time = 0f;
            checkpointSystem.reset(runData);
            restart(runData, playerData);
            return runData;
        }
    }

    public void updateInfinite(RunData runData, PlayerData playerData){
        if(runData.getCurrentLevel() == LevelType.INFINITE) {
            if (playerData.y > levelGeneration.getHighestYInInfinite() - LevelGeneration.GENERATION_HEIGHT) {
                levelGeneration.addToInfiniteLevel(runData, playerData);
            }
        }
    }
}
