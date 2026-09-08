package com.hancockt.potop.systems;

import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

public class WaterSystem {
    private static float waterLevel;
    private static float waterRiseSpeed;
    private static final boolean waterRise = true;
    private static final float maxWaterRiseSpeed = 50;


    public static float getWaterLevel(){
        return waterLevel;
    }

    public static float getMaxWaterRiseSpeed(){
        return maxWaterRiseSpeed;
    }

    public static void restart(RunData runData){
        waterLevel = PlayerData.reset(runData.getCheckpoints(), runData.getCurrentLevel());
        waterRiseSpeed = Math.min(50f, 8f + (PlayerData.y / 200f)); //full speed always after ~8k height
    }

    public static float calculateDangerBoost(){
        float distanceFromWater = PlayerData.y - waterLevel;
        final float dangerBoostDistance = 400f;
        float dangerBoost = Math.max(0, 1 - (distanceFromWater / dangerBoostDistance));
        dangerBoost = 1 + (dangerBoost / 4); // from 1 to 1.25!
        return dangerBoost;
    }

    public static void updateWater(RunData runData, float delta){
        //rising water
        if(waterRise) {
            if(runData.getCurrentLevel() == LevelType.INFINITE) {
                if(waterRiseSpeed <= maxWaterRiseSpeed) {
                    //TODO make constant, pixels per second!
                    waterRiseSpeed += 2 * delta; // increase water speed by 2 pixels every second (as delta accumulates)
                }
            }
            waterLevel += waterRiseSpeed * delta;
        }
    }

    public static void updateCheckpointWaterLevel(RunData runData, float waterLevel){
        runData.checkpointWaterLevel = waterLevel;
    }

    public static void updateCheckpointWaterLevel(RunData runData){
        updateCheckpointWaterLevel(runData, waterLevel);
    }

    public static void updateWaterRiseSpeed(float change){
        //Used for faucet change!
        waterRiseSpeed += change;
    }
}
