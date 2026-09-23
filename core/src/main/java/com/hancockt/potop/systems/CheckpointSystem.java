package com.hancockt.potop.systems;

import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.RunData;

public class CheckpointSystem {
    private static final int checkpointCurrencyMin = 20; //TUNABLE
    private static final int checkpointCurrencyMax = 30; //TUNABLE

    public void reset(RunData runData){
        runData.currCheckpoint = null;
    }

    public void update(RunData runData, Rectangle playerRect, WaterSystem waterSystem, CurrencySystem currencySystem){
        //Checkpoints
        for(Checkpoint c : runData.getActiveCheckpoints()){
            Rectangle checkpoint = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());
            if(!c.isActivated() && checkpoint.overlaps(playerRect)){
                c.setActivated(true);
                if(runData.currCheckpoint != null) {
                    if (c.getY() > runData.currCheckpoint.getY()){
                        runData.currCheckpoint = c;
                        waterSystem.updateCheckpointWaterLevel(runData, false);

                        int numCurrency = runData.rng.nextInt(checkpointCurrencyMin, checkpointCurrencyMax);
                        currencySystem.spawnCurrency(runData, numCurrency, c.getX(), c.getY(), c.getWidth(), c.getHeight());
                    }
                } else{
                    runData.currCheckpoint = c;
                    waterSystem.updateCheckpointWaterLevel(runData, false);
                }
            }
        }
    }
}
