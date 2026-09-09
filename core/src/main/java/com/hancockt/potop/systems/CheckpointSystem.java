package com.hancockt.potop.systems;

import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.RunData;

public class CheckpointSystem {
    private static final int checkpointCurrencyMin = 20; //TUNABLE
    private static final int checkpointCurrencyMax = 30; //TUNABLE
    private final WaterSystem waterSystem;
    private final CurrencySystem currencySystem;

    public CheckpointSystem(WaterSystem waterSystem, CurrencySystem currencySystem) {
        this.waterSystem = waterSystem;
        this.currencySystem = currencySystem;
    }

    public void reset(RunData runData){
        runData.currCheckpoint = null;
    }

    public void update(RunData runData, Rectangle playerRect){
        //Checkpoints
        for(Checkpoint c : runData.getCheckpoints()){
            Rectangle checkpoint = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());
            if(!c.isActivated() && checkpoint.overlaps(playerRect)){
                c.setActivated(true);
                if(runData.currCheckpoint != null) {
                    if(runData.getCurrentLevel() == LevelType.INFINITE){
                        if (c.getY() > runData.currCheckpoint.getY()){
                            runData.currCheckpoint = c;
                            waterSystem.updateCheckpointWaterLevel(runData, false);
                            runData.timeAtDeath = runData.time;

                            int numCurrency = runData.rng.nextInt(checkpointCurrencyMin, checkpointCurrencyMax);
                            currencySystem.spawnCurrency(runData, numCurrency,
                                new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
                        }
                    } else {
                        if (c.getOrder() > runData.currCheckpoint.getOrder()) {
                            runData.currCheckpoint = c;
                            waterSystem.updateCheckpointWaterLevel(runData, false);
                            runData.timeAtDeath = runData.time;
                        }
                    }
                } else{
                    runData.currCheckpoint = c;
                    waterSystem.updateCheckpointWaterLevel(runData, false);
                    runData.timeAtDeath = runData.time;
                }
            }
        }
    }
}
