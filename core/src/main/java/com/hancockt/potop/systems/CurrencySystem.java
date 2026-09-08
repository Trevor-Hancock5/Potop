package com.hancockt.potop.systems;

import com.hancockt.potop.features.Currency;
import com.hancockt.potop.state.RunData;

public class CurrencySystem {


    public static void spawnCurrency(RunData runData, int numOfCurrency, float xRef, float yRef, float width, float height){
        //TODO - add constants in currency for this!
        // todo - move to currency!
        for(int i = 0; i < numOfCurrency; i++){
            float explodeXVelocity = RunData.rng.nextFloat(0, 450) * Math.signum(RunData.rng.nextFloat(-1, 1));
            float explodeYVelocity = RunData.rng.nextFloat(0, 450) * Math.signum(RunData.rng.nextFloat(-1, 1));

            runData.getCurrencies().add(new Currency(xRef + width / 2,
                yRef + height / 2,
                false, explodeXVelocity,
                explodeYVelocity,
//                rng.nextFloat(300, 450),
                RunData.rng.nextFloat(0.05f, 0.15f))); // time
        }
    }
}
