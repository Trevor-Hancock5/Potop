package com.hancockt.potop.features;


//TODO update for what you want and what you need, this is just AI generated for a template
public class Upgrade {
    public final String key;        // unique save key, e.g. "speed"
    public final String displayName; // shown in UI, e.g. "Movement Speed"
    public final int maxLevel;
    public int currentLevel;

    // cost to purchase the next level
    public final int[] matchCostPerLevel; // index = level you're buying

    // the actual effect value at each level
    public final float[] valuePerLevel; // index = current level

    public Upgrade(String key, String displayName, int maxLevel,
                   int[] matchCostPerLevel, float[] valuePerLevel) {
        this.key = key;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.matchCostPerLevel = matchCostPerLevel;
        this.valuePerLevel = valuePerLevel;
    }

    public boolean isMaxed() { return currentLevel >= maxLevel; }

    public int nextCost() {
        if(isMaxed()) return 0;
        return matchCostPerLevel[currentLevel];
    }

    public float currentValue() {
        return valuePerLevel[currentLevel];
    }

    public void levelUp() {
        if(!isMaxed()) currentLevel++;
    }
}
