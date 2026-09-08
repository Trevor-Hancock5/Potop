package com.hancockt.potop.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.hancockt.potop.state.PersistentData;

// Note if ever a high score is made, preferences is a file that a user can change!

public class SaveSystem {
    private static final String PREFS_NAME = "potop_save";
    private Preferences prefs;

    public SaveSystem() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public void saveCurrency(int numMatches) {
        prefs.putInteger("currency", numMatches);
        prefs.flush();
    }

    public void savePrestigeCurrency(int numLogs){
        prefs.putInteger("prestigeCurrency", numLogs);
        prefs.flush();
    }

    public int loadCurrency() {
        return prefs.getInteger("currency", 0);
    }

    public int loadPrestigeCurrency() {
        return prefs.getInteger("prestigeCurrency", 0);
    }

    public void saveHighScore(float score, long seed) {
        float current = (float) loadHighScore()[0];
        if(score > current) {
            prefs.putFloat("highScore", score);
            prefs.putLong("seed", seed);
            prefs.flush();
        }
    }

    public long[] loadHighScore() {
        long[] ret = new long[2];
        ret[0] = (long) prefs.getFloat("highScore", 0f);
        ret[1] = prefs.getLong("seed", 0);
        return ret;
    }

    public void saveUpgrade(String upgradeKey, int level) {
        prefs.putInteger(upgradeKey, level);
        prefs.flush();
    }

    public int loadUpgrade(String upgradeKey) {
        return prefs.getInteger(upgradeKey, 0); // 0 = not purchased
    }

    public void saveAll() {
//        saveCurrency(PersistentData.numMatches);
//        savePrestigeCurrency(PersistentData.numFirewood);
//        saveHighScore(PlayerData.); TODO

//        prefs.putInteger("currency", PlayerData.numMatches);
//        prefs.putInteger("firewood", progress.firewood);
//        prefs.putFloat("highScore", progress.highScore);
//        prefs.putInteger("freeFlares", progress.freeFlares);
//        prefs.putBoolean("factoryUnlocked", progress.factoryUnlocked);
//        // save each upgrade level by key
//        for(Map.Entry<String, Integer> entry : progress.upgradeLevels.entrySet()) {
//            prefs.putInteger("upgrade_" + entry.getKey(), entry.getValue());
//        }
//        prefs.flush();
    }

//    public PlayerProgress loadAll() {
//        PlayerProgress progress = new PlayerProgress();
//        progress.matches = prefs.getInteger("matches", 0);
//        progress.firewood = prefs.getInteger("firewood", 0);
//        progress.highScore = prefs.getFloat("highScore", 0f);
//        progress.freeFlares = prefs.getInteger("freeFlares", 3);
//        progress.factoryUnlocked = prefs.getBoolean("factoryUnlocked", false);
//        return progress;
//    }

    public void deleteAll() {
        prefs.clear();
        prefs.flush();
    }
}
