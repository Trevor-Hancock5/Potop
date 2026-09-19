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
    public float time;
    public float timeAtDeath;
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
    private final float checkUpdateActive = 1.0f; //seconds - TUNABLE
    private final int yDifferentialToTriggerActiveListsUpdate = 500; // update acitve lists when player moves by 500
    private final int activeListsRange = 1000; // active lists are 1000 above and below player
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
        updateActiveLists(0, 0);
        this.startPoint = startPoint;
        runMatches = 0;
        this.currentLevel = currLevel;
        this.nextLevel = nextLevel;
        time = 0f;
        timeAtDeath = 0f;
        deathXValues = new ArrayList<>();
        this.seed = seed;
        this.rng = new Random(seed);
    }

    public RunData(List<Checkpoint> checkpoints, List<Platform> platforms, Vector2 startPoint,
                   List<Faucet> faucets, List<Currency> currencies, LevelType currLevel,
                   LevelType nextLevel){
        this(checkpoints, platforms, startPoint, faucets, currencies,  currLevel, nextLevel, System.currentTimeMillis());
    }

    public void updateActiveLists(float playerY, float delta){
        currentActiveTimeAmount += delta;
        if(currentActiveTimeAmount < checkUpdateActive) {
            return;
        }
        if(Math.abs(playerY - lastUpdatedYForActive) > yDifferentialToTriggerActiveListsUpdate){
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
