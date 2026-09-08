package com.hancockt.potop.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.main.GameScreen;
import com.hancockt.potop.state.GameState;
import com.hancockt.potop.state.RunData;

public class FaucetSystem {
    private static final float faucetRange = 30f; //how close to faucet to interact
    private static final float faucetRotationSpeed = 240f; //degrees per second it spins
    private static final float faucetInteractionTime = 1.5f; //seconds TODO, owned by faucet!

    public static void reset(RunData runData){
        for (Faucet f : runData.getFaucets()) {
            f.setTurnedOff(false); //reset TODO - why?
            f.setRotation(0);
            f.setProgress(0);
        }
    }

    public static void update (RunData runData, float delta, Rectangle playerRect){
        //Faucets
        Faucet nearFaucet = null;
        for(Faucet f : runData.getFaucets()) {
            if(!f.isTurnedOff()){
                Rectangle faucet = new Rectangle(f.getX() - faucetRange, f.getY() - faucetRange, f.getWidth() + 2*faucetRange,
                    f.getHeight() + 2*faucetRange);
                if(playerRect.overlaps(faucet)){
                    nearFaucet = f;
                    break;
                }
            }
        }

        boolean finishedFinalFaucet = false;
        if(nearFaucet != null && (Gdx.input.isKeyPressed(Input.Keys.E) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))){
            nearFaucet.setProgress(nearFaucet.getProgress() + delta);
            nearFaucet.setRotation(nearFaucet.getRotation() + (faucetRotationSpeed * delta));
            if(nearFaucet.getProgress() > faucetInteractionTime){
                nearFaucet.setTurnedOff(true);
                nearFaucet.setRotation(360);
                WaterSystem.updateWaterRiseSpeed(-nearFaucet.getSpeedReduction());
                if(nearFaucet.isWinFaucet()){
                    finishedFinalFaucet = true;
                }

                // todo faucet currency amount! (make somewhat random? - define in faucet)
                CurrencySystem.spawnCurrency(runData, 15, nearFaucet.getX(), nearFaucet.getY(), nearFaucet.getWidth(), nearFaucet.getHeight());
            }
        } else if(nearFaucet != null){
            //not holding down, start spinning back
            nearFaucet.setProgress(Math.max(0, nearFaucet.getProgress() - delta));
            if(nearFaucet.getRotation() > 0) {
                nearFaucet.setRotation(nearFaucet.getRotation() - (faucetRotationSpeed * delta));
            } else{
                nearFaucet.setRotation(0);
            }
        }

        //Check win condition before death
        if(finishedFinalFaucet){
            GameFlow.GAME_STATE = GameState.WIN;
        }
    }

    public static float getFaucetInteractionTime(){
        return faucetInteractionTime;
    }
}
