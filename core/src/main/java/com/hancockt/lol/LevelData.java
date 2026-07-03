package com.hancockt.lol;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class LevelData {
    private final LevelType type;
    private final List<Checkpoint> checkpoints;
    private final List<Faucet> faucets;
    private final Rectangle waterTriggerZone;
    private final Rectangle waterFixZone;
    private final Rectangle winZone;
    private final List<Platform> platforms; // already built, not modules
    private final Vector2 startPoint;
    private final boolean waterStartsActive;
    private final LevelType nextLevel;

    public LevelData(LevelType type, List<Checkpoint> checkpoints, Rectangle waterTriggerZone,
                     Rectangle waterFixZone, Rectangle winZone, List<Platform> platforms,
                     Vector2 startPoint, boolean waterStartsActive, LevelType nextLevel, List<Faucet> faucets) {
        this.type = type;
        this.checkpoints = checkpoints;
        this.faucets = faucets;
        this.waterTriggerZone = waterTriggerZone;
        this.waterFixZone = waterFixZone;
        this.winZone = winZone;
        this.platforms = platforms;
        this.startPoint = startPoint;
        this.waterStartsActive = waterStartsActive;
        this.nextLevel = nextLevel;
    }

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public List<Faucet> getFaucets() {
        return faucets;
    }

    public Rectangle getWaterTriggerZone() {
        return waterTriggerZone;
    }

    public Rectangle getWaterFixZone() {
        return waterFixZone;
    }

    public Rectangle getWinZone() {
        return winZone;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public Vector2 getStartPoint() {
        return startPoint;
    }

    public boolean isWaterStartsActive() {
        return waterStartsActive;
    }

    public LevelType getNextLevel() {
        return nextLevel;
    }
}
