package com.hancockt.potop.systems;

import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

public class WaterSystem {
    private float waterLevel;
    private float waterRiseSpeed;

    public static final float defaultWaterLevelOnStart = -50f; // TUNABLE
    private static final float maxWaterRiseSpeed = 50; // TUNABLE
    private static final float baseWaterRiseSpeed = 8f; // TUNABLE
    private static final float waterRiseSpeedAlwaysAtMaxAboveY = 8000f; // TUNABLE
    private static final int waterAccelerationPixelsPerSecond = 2; // TUNABLE

    private static final float dangerBoostDistance = 400f; // TUNABLE
    private static final float maxDangerBoost = 1.25f; //TUNABLE

    public float getWaterLevel(){
        return waterLevel;
    }

    public void restart(RunData runData, PlayerData playerData){
        waterLevel = playerData.reset(runData.getCurrentLevel());
        float divisor = waterRiseSpeedAlwaysAtMaxAboveY / (maxWaterRiseSpeed - baseWaterRiseSpeed);
        waterRiseSpeed = Math.min(maxWaterRiseSpeed, baseWaterRiseSpeed + (playerData.y / divisor));
    }

    public float calculateDangerBoost(PlayerData playerData){
        float distanceFromWater = playerData.y - waterLevel;
        float dangerBoost = Math.max(0, 1 - (distanceFromWater / dangerBoostDistance));
        dangerBoost = 1 + dangerBoost * (maxDangerBoost - 1);
        return dangerBoost;
    }

    public void updateWater(RunData runData, float delta){
        //rising water
        if(runData.getCurrentLevel() == LevelType.INFINITE) {
            if(waterRiseSpeed <= maxWaterRiseSpeed) {
                waterRiseSpeed += waterAccelerationPixelsPerSecond * delta; // increase water speed by 2 pixels every second (as delta accumulates)
            }
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
