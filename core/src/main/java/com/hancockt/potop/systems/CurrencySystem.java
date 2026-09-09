package com.hancockt.potop.systems;

import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.features.Currency;
import com.hancockt.potop.main.FrameRuntimeContext;
import com.hancockt.potop.state.PersistentData;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

import java.util.ArrayList;
import java.util.List;

public class CurrencySystem {
    private static final float currencyRange = 8f; //TUNABLE
    private static final float explodeVelocityMax = 450; //TUNABLE
    private static final float minExplodeTime = 0.05f; //TUNABLE
    private static final float maxExplodeTime = 0.15f; //TUNABLE

    public void update(FrameRuntimeContext frameRuntimeContext, Rectangle playerRect){
        RunData runData = frameRuntimeContext.runData();
        PersistentData persistentData = frameRuntimeContext.persistentData();
        PlayerData playerData = frameRuntimeContext.playerData();
        float delta = frameRuntimeContext.delta();
        float sparkHeadX = playerData.x + ((float) PlayerData.playerWidth / 2);
        float sparkHeadY = playerData.y + PlayerData.playerHeight - 15;

        //update currency positions and explode times, once time is up, switch velocity towards spark's head
        for (Currency c : runData.getCurrencies()){
            if(c.getExplodeTime() > 0){
                c.moveX(c.getVelocityX() * delta);
                c.moveY(c.getVelocityY() * delta);
                c.updateExplodeTime(delta);
            } else if (c.getExplodeTime() < 0){
                // less than 0 indicates that player has interacted with currency and explosion is done,
                    // meaning it needs absorption
                float dx = sparkHeadX - c.getX();
                float dy = sparkHeadY - c.getY();

                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                float directionX = 0;
                float directionY = 0;
                if (distance > 0){
                    directionX = dx / distance;
                    directionY = dy / distance;
                }

                c.updateAbsorbTime(delta);

                if(c.getAbsorbTime() <= (Currency.MAX_ABSORB_TIME / 2)) {
                    //better looking
                    boolean entropy = runData.rng.nextBoolean();

                    if (entropy) {
                        directionX *= runData.rng.nextFloat(1, 2);
                        directionY *= runData.rng.nextFloat(1, 2);
                    }
                    c.changeVelocityX(directionX * Currency.ABSORB_ACCELERATION * delta);
                    c.changeVelocityY(directionY * Currency.ABSORB_ACCELERATION * delta);
                } else {
                    //quick absorb!
                    //time to get absorbed
                    float t = Math.max(Currency.MAX_ABSORB_TIME - c.getAbsorbTime(), 0);
                    c.setVelocityX(dx / t);
                    c.setVelocityY(dy / t);
                }

                c.moveX(c.getVelocityX() * delta);
                c.moveY(c.getVelocityY() * delta);
            }
        }

        //check if overlapping currency
        Rectangle sparkHead = new Rectangle(playerData.x + ((float) playerData.playerWidth / 4),
            playerData.y + playerData.playerHeight - 10, (float) playerData.playerWidth / 2, 10);
        List<Currency> toRemove = new ArrayList<>();
        for(Currency c : runData.getCurrencies()){
            Rectangle curr = new Rectangle(c.getX() - currencyRange, c.getY() - currencyRange,
                Currency.WIDTH + 2*currencyRange, Currency.HEIGHT + 2*currencyRange);
            if(playerRect.overlaps(curr)){
                // This notes it is within range and then the next for loop moves it to absorb!
                c.updateExplodeTime(delta);
            }

            if(sparkHead.overlaps(curr) && c.getExplodeTime() < 0){
                //Check if overlapping spark's head, this is what absorbs the currency
                if(!c.isPrestige()) {
                    runData.runMatches += 1;
                } else{
                    persistentData.unlockedFactory = true;
                }
                toRemove.add(c);
            }
        }

        runData.getCurrencies().removeAll(toRemove);
    }

    public void spawnCurrency(RunData runData, int numOfCurrency, Rectangle spawnBounds){
        for(int i = 0; i < numOfCurrency; i++){
            float explodeXVelocity = runData.rng.nextFloat(0, explodeVelocityMax) * Math.signum(runData.rng.nextFloat(-1, 1));
            float explodeYVelocity = runData.rng.nextFloat(0, explodeVelocityMax) * Math.signum(runData.rng.nextFloat(-1, 1));
            float explodeTime = runData.rng.nextFloat(minExplodeTime, maxExplodeTime);

            runData.getCurrencies().add(new Currency(spawnBounds.x + spawnBounds.width / 2,
                spawnBounds.y + spawnBounds.height / 2,false,
                explodeXVelocity, explodeYVelocity, explodeTime));
        }
    }
}
