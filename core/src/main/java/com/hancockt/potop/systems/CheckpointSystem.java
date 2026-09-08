package com.hancockt.potop.systems;

import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.main.GameScreen;
import com.hancockt.potop.state.RunData;

public class CheckpointSystem {
    public void reset(RunData runData){
        runData.currCheckpoint = null;
    }

    public void update(RunData runData, Rectangle playerRect, WaterSystem waterSystem, CurrencySystem currencySystem){
        //Checkpoints
        for(Checkpoint c : runData.getCheckpoints()){
            Rectangle checkpoint = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());
            if(!c.isActivated() && checkpoint.overlaps(playerRect)){
                c.setActivated(true);
                if(runData.currCheckpoint != null) {
                    if(runData.getCurrentLevel() == LevelType.INFINITE){
                        if (c.getY() > runData.currCheckpoint.getY()){
                            runData.currCheckpoint = c;
                            waterSystem.updateCheckpointWaterLevel(runData);
                            runData.timeAtDeath = runData.time;

                            // todo checkpoint currency amount! (make somewhat random? - define in checkpoint)
                            currencySystem.spawnCurrency(runData, 25, c.getX(), c.getY(), c.getWidth(), c.getHeight());
                        }
                    } else {
                        if (c.getOrder() > runData.currCheckpoint.getOrder()) {
                            runData.currCheckpoint = c;
                            waterSystem.updateCheckpointWaterLevel(runData);
                            runData.timeAtDeath = runData.time;
                        }
                    }
                } else{
                    runData.currCheckpoint = c;
                    waterSystem.updateCheckpointWaterLevel(runData);
                    runData.timeAtDeath = runData.time;
                }
            }
        }
    }
}
