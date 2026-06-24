/*
 * Course: CSC1110A-111
 * Fall 2024
 * Assignment
 * Name: Trevor Hancock
 * Last Updated: 6/21/2026
 */
package com.hancockt.lol;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class LevelData {
    public LevelType type;
    public List<Checkpoint> checkpoints;
    public List<Faucet> faucets;
    public Rectangle waterTriggerZone;
    public Rectangle waterFixZone;
    public Rectangle winZone;
    public List<Platform> platforms; // already built, not modules
    public Vector2 startPoint;
    public boolean waterStartsActive;
    public LevelType nextLevel;

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
}
