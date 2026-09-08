package com.hancockt.potop.state;

import com.badlogic.gdx.math.Vector2;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.features.Currency;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.level.LevelType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RunData {
    // TODO - convert this to object and no static, so then you can separate runs!

    // Everything belonging to the current attempt
    // This is everything but stuff related to the player: platforms, checkpoints, etc.
    public static int runMatches;

    public Checkpoint currCheckpoint; //current checkpoint player has made it to
    public float checkpointWaterLevel; //water level when user hit current checkpoint
    protected static final float checkpointMinWaterBuffer = 200f;
    public float time = 0f;
    public float timeAtDeath = 0f;
    public static List<Float> deathXValues = new ArrayList<>();

    private List<Checkpoint> checkpoints;
    private final List<Faucet> faucets;
    private final List<Currency> currencies;
    private final List<Platform> platforms; // already built, not modules
    private final Vector2 startPoint;
    private LevelType currentLevel;
    private final LevelType nextLevel;

    // Random
    public static final long seed = System.currentTimeMillis();;
    public static final Random rng = new Random(seed);

    public RunData(List<Checkpoint> checkpoints, List<Platform> platforms, Vector2 startPoint,
                     List<Faucet> faucets, List<Currency> currencies, LevelType currLevel, LevelType nextLevel) {
        currCheckpoint = null;
        checkpointWaterLevel = -checkpointMinWaterBuffer;
        this.checkpoints = checkpoints;
        this.faucets = faucets;
        this.currencies = currencies;
        this.platforms = platforms;
        this.startPoint = startPoint;
        runMatches = 0;
        this.currentLevel = currLevel;
        this.nextLevel = nextLevel;
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
        if(nextLevel == null){
            return LevelType.INFINITE;
        } else{
            return nextLevel;
        }
    }

    public LevelType getCurrentLevel(){
        return currentLevel;
    }

    public void setCurrentLevel(LevelType levelType){
        currentLevel = levelType;
    }
}
