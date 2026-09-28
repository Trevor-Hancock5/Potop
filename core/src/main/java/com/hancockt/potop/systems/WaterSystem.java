package com.hancockt.potop.systems;

import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.main.GameFlow;
import com.hancockt.potop.state.GameState;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

public class WaterSystem {
    private float waterLevel;
    private float waterRiseSpeed;

    public static final float defaultWaterLevelOnStart = -50f; // TUNABLE
    private static final float maxWaterRiseSpeedNormal = 80; // TUNABLE
    private static final float maxWaterRiseSpeedLearningMode = 40; // TUNABLE
    private static final float baseWaterRiseSpeed = 15f; // TUNABLE
    private static final float waterRiseSpeedAlwaysAtMaxAboveY = 8000f; // TUNABLE
    private static final int waterAccelerationPixelsPerSecond = 2; // TUNABLE

    public float getWaterLevel(){
        return waterLevel;
    }

    public void restart(RunData runData, PlayerData playerData){
        waterLevel = playerData.reset(runData.getCurrentLevel());

        float divisor = waterRiseSpeedAlwaysAtMaxAboveY / (getMaxSpeed(runData) - baseWaterRiseSpeed);
        waterRiseSpeed = Math.min(getMaxSpeed(runData), baseWaterRiseSpeed + (playerData.y / divisor));
    }

    private float getMaxSpeed(RunData runData){
        return runData.getCurrentLevel() == LevelType.LEARNING_MODE ? maxWaterRiseSpeedLearningMode : maxWaterRiseSpeedNormal;
    }

    public void updateWater(RunData runData, PlayerData playerData, float delta){
        //death check
        Rectangle waterRect = new Rectangle(-50000, 0, 100000, waterLevel);

        //Only dead if head hits water (because flame Afro!), so top 15 pixels of player rect
        if(waterRect.overlaps(playerData.playerRect) && waterLevel >= playerData.sparkHeadY){
            if(GameFlow.GAME_STATE != GameState.DEATH){
                runData.deathXValues.add(playerData.x);
            }
            GameFlow.GAME_STATE = GameState.DEATH;
        }

        //rising water
        if(waterRiseSpeed <= getMaxSpeed(runData)) {
            waterRiseSpeed += waterAccelerationPixelsPerSecond * delta; // increase water speed by 2 pixels every second (as delta accumulates)
        }

        waterLevel += waterRiseSpeed * delta;
    }

    public void updateCheckpointWaterLevel(RunData runData, float waterLevel){
        runData.checkpointWaterLevel = waterLevel;
    }

    public void updateCheckpointWaterLevel(RunData runData, boolean useDefault){
        if(useDefault){
            updateCheckpointWaterLevel(runData, defaultWaterLevelOnStart);
        } else {
            updateCheckpointWaterLevel(runData, waterLevel);
        }
    }

    public void updateWaterRiseSpeed(float change){
        //Used for faucet change!
        waterRiseSpeed += change;
    }
}
