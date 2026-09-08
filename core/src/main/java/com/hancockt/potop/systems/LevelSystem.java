package com.hancockt.potop.systems;

import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

import java.util.ArrayList;

public class LevelSystem {
    public void restart(RunData runData) {
        WaterSystem.restart(runData);

        runData.time = runData.timeAtDeath;

        FaucetSystem.reset(runData);
    }

    public RunData loadLevel(LevelType levelType) {
        RunData runData = LevelGeneration.getAllLevels().get(levelType);
        PlayerData.x = runData.getStartPoint().x; // TODO - NO!
        PlayerData.y = runData.getStartPoint().y;

        return runData;
    }

    public RunData fullReset(RunData runData){
        // Called when user hits 'Q'
        RunData tempRunData = null;
        if (runData.getCurrentLevel() == LevelType.INFINITE) {
            WaterSystem.updateCheckpointWaterLevel(runData, -50f);
            RunData.deathXValues = new ArrayList<>(); //reset
            LevelGeneration.regenerateInfinite();
            tempRunData = loadLevel(LevelType.INFINITE); //regenerate!
        }
        if(tempRunData != null){
            tempRunData.timeAtDeath = 0f;
            tempRunData.time = 0f;
            CheckpointSystem.reset(tempRunData);
            restart(tempRunData);
            return tempRunData;
        } else {
            runData.timeAtDeath = 0f;
            runData.time = 0f;
            CheckpointSystem.reset(runData);
            restart(runData);
            return runData;
        }
    }

}
