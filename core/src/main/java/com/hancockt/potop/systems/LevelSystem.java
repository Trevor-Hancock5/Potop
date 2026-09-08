package com.hancockt.potop.systems;

import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

import javax.swing.*;
import java.util.ArrayList;

public class LevelSystem {
    public void restart(RunData runData, WaterSystem waterSystem, FaucetSystem faucetSystem) {
        waterSystem.restart(runData);

        runData.time = runData.timeAtDeath;

        faucetSystem.reset(runData);
    }

    public RunData loadLevel(LevelType levelType) {
        RunData runData = LevelGeneration.getAllLevels().get(levelType);
        PlayerData.x = runData.getStartPoint().x; // TODO - NO!
        PlayerData.y = runData.getStartPoint().y;

        return runData;
    }

    public RunData fullReset(RunData runData, WaterSystem waterSystem, FaucetSystem faucetSystem, CheckpointSystem checkpointSystem){
        // Called when user hits 'Q'
        RunData tempRunData = null;
        if (runData.getCurrentLevel() == LevelType.INFINITE) {
            waterSystem.updateCheckpointWaterLevel(runData, -50f);
            runData.deathXValues = new ArrayList<>(); //reset
            LevelGeneration.regenerateInfinite();
            tempRunData = loadLevel(LevelType.INFINITE); //regenerate!
        }
        if(tempRunData != null){
            tempRunData.timeAtDeath = 0f;
            tempRunData.time = 0f;
            checkpointSystem.reset(tempRunData);
            restart(tempRunData, waterSystem, faucetSystem);
            return tempRunData;
        } else {
            runData.timeAtDeath = 0f;
            runData.time = 0f;
            checkpointSystem.reset(runData);
            restart(runData, waterSystem, faucetSystem);
            return runData;
        }
    }

    public void updateInfinite(RunData runData){
        if(runData.getCurrentLevel() == LevelType.INFINITE) {
            // Todo - 2000? why and where, define constant! 2000 is sprinkled in many areas!
            if (PlayerData.y > LevelGeneration.getHighestYInInfinite() - 2000) {
                LevelGeneration.addToInfiniteLevel(runData.getPlatforms(), runData.getCheckpoints(), runData.getFaucets(), runData.getCurrencies());
            }
        }
    }
}
