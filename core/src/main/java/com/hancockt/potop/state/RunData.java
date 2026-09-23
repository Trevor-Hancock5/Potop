package com.hancockt.potop.state;

import com.badlogic.gdx.math.Vector2;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.features.Currency;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.level.LevelType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RunData {
    // Everything belonging to the current attempt
    // This is everything but stuff related to the player: platforms, checkpoints, etc.
    public int runMatches;

    public Checkpoint currCheckpoint; //current checkpoint player has made it to
    public float checkpointWaterLevel; //water level when user hit current checkpoint
    public List<Float> deathXValues;

    public static final float checkpointMinWaterBuffer = 200f; //TUNABLE

    private final List<Checkpoint> checkpoints;
    private final List<Faucet> faucets;
    private final List<Currency> currencies;
    private final List<Platform> platforms; // already built, not modules

    private final List<Checkpoint> activeCheckpoints;
    private final List<Faucet> activeFaucets;
    private final List<Currency> activeCurrency;
    private final List<Platform> activePlatforms;

    private float currentActiveTimeAmount;
    private static final float checkUpdateActive = 1.0f; //TUNABLE - seconds
    private static final int yDifferentialToTriggerActiveListsUpdate = 500; // TUNABLE - update acitve lists when player moves by 500
    private final static int activeListsRange = 1000; // TUNABLE - active lists are 1000 above and below player
    private float lastUpdatedYForActive;

    private final Vector2 startPoint;
    private LevelType currentLevel;
    private final LevelType nextLevel;

    // Random
    public long seed;
    public Random rng;

    public RunData(List<Checkpoint> checkpoints, List<Platform> platforms, Vector2 startPoint,
                     List<Faucet> faucets, List<Currency> currencies, LevelType currLevel,
                     LevelType nextLevel, long seed) {
        currCheckpoint = null;
        checkpointWaterLevel = -checkpointMinWaterBuffer;
        this.checkpoints = checkpoints;
        this.faucets = faucets;
        this.currencies = currencies;
        this.platforms = platforms;

        activeCheckpoints = new ArrayList<>();
        activeFaucets = new ArrayList<>();
        activeCurrency = new ArrayList<>();
        activePlatforms = new ArrayList<>();

        currentActiveTimeAmount = 5;
        lastUpdatedYForActive = -1000;
        updateActiveLists(0, 0, checkpointWaterLevel); //default values
        this.startPoint = startPoint;
        runMatches = 0;
        this.currentLevel = currLevel;
        this.nextLevel = nextLevel;
        deathXValues = new ArrayList<>();
        this.seed = seed;
        this.rng = new Random(seed);
    }

    public RunData(List<Checkpoint> checkpoints, List<Platform> platforms, Vector2 startPoint,
                   List<Faucet> faucets, List<Currency> currencies, LevelType currLevel,
                   LevelType nextLevel){
        this(checkpoints, platforms, startPoint, faucets, currencies,  currLevel, nextLevel, System.currentTimeMillis());
    }

    public void updateActiveLists(float playerY, float delta, float waterLevel){
        currentActiveTimeAmount += delta;
        if(currentActiveTimeAmount < checkUpdateActive) {
            return;
        }
        if(Math.abs(playerY - lastUpdatedYForActive) > yDifferentialToTriggerActiveListsUpdate){
            // update base lists to remove outdated objects
            removeOutdatedObjects(waterLevel);

            // Update the lists
            float lowYThreshold = playerY - activeListsRange;
            float upperYThreshold = playerY + activeListsRange;

            for(Platform p : platforms){
                float platY = p.getY();
                if(platY > lowYThreshold && platY < upperYThreshold){
                    activePlatforms.add(p);
                }
            }

            for(Checkpoint c : checkpoints){
                float checkY = c.getY();
                if(checkY > lowYThreshold && checkY < upperYThreshold){
                    activeCheckpoints.add(c);
                }
            }

            for(Currency curr : currencies){
                float currencyY = curr.getY();
                if(currencyY > lowYThreshold && currencyY < upperYThreshold){
                    activeCurrency.add(curr);
                }
            }


            for(Faucet f : faucets){
                float faucetY = f.getY();
                if(faucetY > lowYThreshold && faucetY < upperYThreshold){
                    activeFaucets.add(f);
                }
            }

            lastUpdatedYForActive = playerY;
            currentActiveTimeAmount = 0;
        }
    }

    private void removeOutdatedObjects(float waterLevel){
        //Update the lists as well to remove any outdated features
        // Anything that is under the water, but also below the current checkpoint by 1000,
        // then below the checkpoint water level!

        //since we don't want to remove anything the player might interact with, be safe and do the lowest y value
        float removeBelow;
        if(currCheckpoint == null){
            removeBelow = -1000;
        } else {
            removeBelow = Math.min(currCheckpoint.getY() - 1000, Math.min(waterLevel - 250, checkpointWaterLevel));
        }

        List<Platform> toRemoveP = new ArrayList<>();
        for(Platform p : platforms){
            if (p.top() < removeBelow){
                toRemoveP.add(p);
            }
        }
        platforms.removeAll(toRemoveP);

        List<Checkpoint> toRemoveCheck = new ArrayList<>();
        for(Checkpoint check : checkpoints){
            if (check.getY() < removeBelow){
                toRemoveCheck.add(check);
            }
        }
        checkpoints.removeAll(toRemoveCheck);

        List<Faucet> toRemoveF = new ArrayList<>();
        for(Faucet f : faucets){
            if (f.getY() < removeBelow){
                toRemoveF.add(f);
            }
        }
        faucets.removeAll(toRemoveF);

        List<Currency> toRemoveCurr = new ArrayList<>();
        for(Currency curr : currencies){
            if (curr.getY() < removeBelow){
                toRemoveCurr.add(curr);
            }
        }
        currencies.removeAll(toRemoveCurr);
    }

    public List<Checkpoint> getActiveCheckpoints() {
        return activeCheckpoints;
    }

    public List<Faucet> getActiveFaucets(){
        return activeFaucets;
    }

    public List<Currency> getActiveCurrency(){
        return activeCurrency;
    }

    public List<Platform> getActivePlatforms(){
        return activePlatforms;
    }

    public List<Checkpoint> getCheckpoints() {
        return checkpoints == null ? new ArrayList<>() : checkpoints;
    }

    public List<Faucet> getFaucets() {
        return faucets;
    }

    public List<Currency> getCurrencies(){
        return currencies;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public Vector2 getStartPoint() {
        return startPoint;
    }

    public LevelType getNextLevel(){
        return Objects.requireNonNullElse(nextLevel, LevelType.INFINITE);
    }

    public LevelType getCurrentLevel(){
        return currentLevel;
    }

    public void setCurrentLevel(LevelType levelType){
        currentLevel = levelType;
    }
}
