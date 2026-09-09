package com.hancockt.potop.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.main.GameFlow;
import com.hancockt.potop.state.GameState;
import com.hancockt.potop.state.RunData;

public class FaucetSystem {
    private static final float faucetRange = 30f; //how close to faucet to interact - TUNABLE
    private static final float faucetRotationSpeed = 240f; //degrees per second it spins - TUNABLE
    private static final float faucetInteractionTime = 1.5f; //seconds - TUNABLE
    private static final int faucetCurrencyMin = 10; // TUNABLE
    private static final int faucetCurrencyMax = 20; // TUNABLE
    private final WaterSystem waterSystem;
    private final CurrencySystem currencySystem;

    public FaucetSystem(WaterSystem waterSystem, CurrencySystem currencySystem) {
        this.waterSystem = waterSystem;
        this.currencySystem = currencySystem;
    }

    public void reset(RunData runData){
        for (Faucet f : runData.getFaucets()) {
            f.setTurnedOff(false); //reset
            f.setRotation(0);
            f.setProgress(0);
        }
    }

    public void update (RunData runData, float delta, Rectangle playerRect){
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
                waterSystem.updateWaterRiseSpeed(-nearFaucet.getSpeedReduction());
                if(nearFaucet.isWinFaucet()){
                    finishedFinalFaucet = true;
                }

                int numCurrency = runData.rng.nextInt(faucetCurrencyMin, faucetCurrencyMax);
                currencySystem.spawnCurrency(runData, numCurrency,
                    new Rectangle(nearFaucet.getX(), nearFaucet.getY(), nearFaucet.getWidth(), nearFaucet.getHeight()));
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

    public float getFaucetInteractionTime(){
        return faucetInteractionTime;
    }
}
