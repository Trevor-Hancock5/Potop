package com.hancockt.potop.level;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.features.Currency;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.logging.Level;


public class LevelLibrary {
    private static final Map<LevelType, RunData> ALL_LEVELS = new HashMap<>();
    private static Random rng;
    private static long seed;
    private static int highestInfiniteY = 0;
    private static final int BAND_HEIGHT = 250;
    private static Platform highestPlatform;
    private static final float LEVEL_WIDTH = 3000;


    static {
        ALL_LEVELS.put(LevelType.LEARNING_MODE, buildTutorialLevel());
        ALL_LEVELS.put(LevelType.INFINITE, buildInfiniteLevel());
    }

    public static Map<LevelType, RunData> getAllLevels(){
        return ALL_LEVELS;
    }

    public static int getHighestYInInfinite(){
        return highestInfiniteY;
    }

    private static RunData buildTutorialLevel(){
        //PR = 33.487s
        List<Checkpoint> checkpoints = new ArrayList<>();
        checkpoints.add(new Checkpoint(1930, 265, 1));
        checkpoints.add(new Checkpoint(3580, 285, 2));
        checkpoints.add(new Checkpoint(4760, 1825, 3));
        checkpoints.add(new Checkpoint(6435, 1910, 4));

        List<Faucet> faucets = new ArrayList<>();
        faucets.add(new Faucet(6870, 2015, 25, 25, 10, false));
        faucets.add(new Faucet(8132.5f, 2198, 25, 25, 0, true));

        List<Currency> currencies = new ArrayList<>();

        //platforms
        List<Module> tutorialSequence = new ArrayList<>();
        tutorialSequence.add(getModuleByName("walk_and_run"));
        tutorialSequence.add(getModuleByName("small_jump"));
        tutorialSequence.add(getModuleByName("variable_jumps"));
        tutorialSequence.add(getModuleByName("momentum"));
        tutorialSequence.add(getModuleByName("vert_jumping_series"));
        tutorialSequence.add(getModuleByName("falls"));
        tutorialSequence.add(getModuleByName("wall_jumping"));
        tutorialSequence.add(getModuleByName("danger_boost_vertical"));
        tutorialSequence.add(getModuleByName("danger_boost_horizontal"));
        tutorialSequence.add(getModuleByName("precision_jumps"));
        List<Platform> platforms = buildLevelFromModules(tutorialSequence, new Vector2(0, 0));

        Vector2 startPoint = new Vector2(0,25);

        return new RunData(
            checkpoints, platforms,
            startPoint, faucets, currencies, LevelType.LEARNING_MODE, LevelType.INFINITE
        );
    }

    private static RunData buildInfiniteLevel(){
        //use seeds for reproducibility in infinite mode and leaderboard
        seed = System.currentTimeMillis();
        rng = new Random(seed);

        List<Checkpoint> checkpoints = new ArrayList<>();
        List<Platform> platforms = new ArrayList<>();
        List<Faucet> faucets = new ArrayList<>();
        List<Currency> currencies = new ArrayList<>();

        //just call generate band for each band until height
        highestPlatform = new Platform(0,0,75,25);
        for(int i = 0; i <= 2000; i += BAND_HEIGHT) {
            highestPlatform = generateBand(platforms, checkpoints, i, highestPlatform,
                                            faucets, currencies);
        }

        return new RunData(
            checkpoints, platforms, new Vector2(0,0), faucets, currencies, LevelType.INFINITE, null
        );
    }

    public static void addToInfiniteLevel(List<Platform> platforms, List<Checkpoint> checkpoints,
                                               List<Faucet> faucets, List<Currency> currencies){
        //Add another 2000 height to the infinite level
        int maxAdditionY = highestInfiniteY + 2000;
        for(int i = highestInfiniteY; i < maxAdditionY; i += BAND_HEIGHT){
            highestPlatform = generateBand(platforms, checkpoints, i,
                highestPlatform, faucets, currencies);
        }
    }

    //todo - maybe more checkpoints when higher?
    //TODO - rework so that it uses LevelData for lists instead of passing them as arguments!
    private static Platform generateBand(List<Platform> platforms, List<Checkpoint> checkpoints, float bandY,
                                         Platform highestPlatform, List<Faucet> faucets, List<Currency> currencies){
        //add checkpoint
        float checkpointHeightDistance = rng.nextFloat(1000, 2000); //add some randomness for kicks and giggles!
        float skipCheckpointThreshold = 0.1f;
        boolean checkpointAdded = false;
        if(checkpoints.isEmpty() || (bandY - checkpoints.getLast().getY() > checkpointHeightDistance && rng.nextFloat() > skipCheckpointThreshold)){
            //add a checkpoint to the highest platform since it shouldn't overlap any other platform!
            checkpointAdded = true;
            float platformMidpoint = highestPlatform.getX() + (float)(highestPlatform.getWidth() / 2);
            float checkpointWidth = Math.min(25, highestPlatform.getWidth());
            float checkpointHeight = 75;
            float checkpointX = platformMidpoint - (checkpointWidth / 2);
            float checkpointY = highestPlatform.getY() + highestPlatform.getHeight();
            checkpoints.add(new Checkpoint(checkpointX, checkpointY, checkpointWidth, checkpointHeight,
                            checkpoints.size()));
        }

        //add faucets
        float faucetHeightDistance = rng.nextFloat(500,1000);
        float skipFaucetThreshold = 0.33f;
        if(!checkpointAdded && (faucets.isEmpty() || (bandY - faucets.getLast().getY() > faucetHeightDistance
                && rng.nextFloat() > skipFaucetThreshold))){
            //add a faucet to highest platform
            float platformMidpoint = highestPlatform.getX() + (float)(highestPlatform.getWidth() / 2);
            float faucetDimensions = 25; //width/height
            float faucetX = platformMidpoint - faucetDimensions / 2;
            float faucetY = highestPlatform.getY() + highestPlatform.getHeight() + 10; //float in air
            float speedReduction = rng.nextFloat(10, 60); //how much to slow the water_rise_speed
            faucets.add(new Faucet(faucetX, faucetY, faucetDimensions, faucetDimensions, speedReduction, false));
        }

        //Add boundary walls
        Platform leftWall = new Platform(-500, bandY, 500, BAND_HEIGHT);
        Platform rightWall = new Platform(LEVEL_WIDTH, bandY, 500, BAND_HEIGHT);
        leftWall.setBoundaryWall(true);
        rightWall.setBoundaryWall(true);
        platforms.add(leftWall);
        platforms.add(rightWall);

        float bandArea = LEVEL_WIDTH * BAND_HEIGHT;
        float decayMultiplier = bandY / 1000;

        //Density
        float baseCoverageRatio = 0.25f;
        float minCoverageRatio = 0.02f;
        float coverageDecay = 0.02f;
        float coverageRatio = Math.max(minCoverageRatio, baseCoverageRatio - (decayMultiplier * coverageDecay));

        //width and height
        float baseMaxWidth = 300;
        float baseMaxHeight = 150;
        float minMaxWidth = 50;
        float minMaxHeight = 30;
        float widthDecay = 20;
        float heightDecay = 10;
        float maxWidth = Math.max(baseMaxWidth - (decayMultiplier * widthDecay), minMaxWidth);
        float maxHeight = Math.max(baseMaxHeight - (decayMultiplier * heightDecay), minMaxHeight);
        float minWidth = 20;
        float minHeight = 10;

        //dynamically choose numPlatforms based on density
        float minNumPlatforms = 5;
        float avgWidth = (maxWidth + minWidth) / 2f;
        float avgHeight = (maxHeight + minHeight) / 2f;
        float avgPlatformArea = avgWidth * avgHeight;

        float targetCoveredArea = bandArea * coverageRatio;
        int numPlatforms = (int) Math.max(minNumPlatforms, targetCoveredArea / avgPlatformArea);

        //platforms
        boolean reachable = false; //reachable from previous band

        //for overlapping, since only platforms in this band can overlap other ones
        List<Platform> bandPlatforms = new ArrayList<>();
        for(int i = 0; i < numPlatforms; i++) {
            Platform p;
            int attempts = 0;
            boolean overlapsFaucet;
            boolean overlapsCheckpoint;
            boolean needsRetry;

            do {
                needsRetry = false;
                attempts++;
                float width = rng.nextFloat(minWidth, maxWidth); //20 and max width!
                float height = rng.nextFloat(minHeight, maxHeight); // 10 and max height!
                float xVal = rng.nextFloat(LEVEL_WIDTH - width); //btwn 0 and (LEVEL_WIDTH - width)
                float yVal = rng.nextFloat(bandY, Math.max(bandY, bandY + BAND_HEIGHT - height)); //btwn bandY and
                // (bandY + BAND_HEIGHT - height)

                p = new Platform(xVal, yVal, width, height);

                //check overlap and checkpoints (check last checkpoint only)
                for(Platform bp : bandPlatforms){
                    Rectangle currP = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());
                    Rectangle bandP = new Rectangle(bp.getX(), bp.getY(), bp.getWidth(), bp.getHeight());

                    boolean overlapPlatform = currP.overlaps(bandP);
                    if(overlapPlatform){
                        needsRetry = true;
                        break;
                    }
                }

                // after the bandPlatforms overlap loop, before the reachability check
                overlapsFaucet = false;
                if (!faucets.isEmpty()) {
                    Faucet f = faucets.getLast();
                    Rectangle faucetRect = new Rectangle(f.getX(), f.getY(), f.getWidth() * 2, f.getHeight() * 2);
                    overlapsFaucet = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight()).overlaps(faucetRect);
                }

                overlapsCheckpoint = false;
                if (!checkpoints.isEmpty()) {
                    Checkpoint c = checkpoints.getLast();
                    Rectangle checkRect = new Rectangle(c.getX(), c.getY(), c.getWidth() * 2, c.getHeight() * 2);
                    overlapsCheckpoint = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight()).overlaps(checkRect);
                }

                //check reachability - need at least one. First one added is reachable!
                if(checkReachability(p, highestPlatform)){
                    reachable = true;
                }

                // until next band
            } while ((attempts < 50) && (needsRetry || overlapsFaucet || overlapsCheckpoint || !reachable));

            platforms.add(p);
            bandPlatforms.add(p);
        }

        //by having the highest platform update outside the for loop, the do-while loop continually tries to make a
        // platform that the previous band's highest platform can reach

        // check if new highest platform for checkpoint making and add currency!
        for(Platform p : bandPlatforms) {
            if (p.getY() + p.getHeight() > highestPlatform.getY() + highestPlatform.getHeight()) {
                highestPlatform = p;
            }

            int currencyWidth = 16;

            //prestige currency chance, super low chance just in case player can't reach factory threshold
            int maxPrestigeCurrencyThreshold = 10_000;
            int minPrestigeCurrencyThreshold = 1_000;
            float chanceDecreaseThreshold = 5_000;
            boolean hasFactory = false; //TODO - once logic in place, get this dynamically!
            float prestigeCurrChanceMax = 0.0001f; //todo - tune this, use a test. Generate up to 10k 100 times and in 50% of runs >=1 is placed
            float prestigeCurrChanceMin = 0.000001f;

            boolean placedPrestige = false;
            if(!hasFactory && p.getY() < maxPrestigeCurrencyThreshold && p.getY() > minPrestigeCurrencyThreshold){
                //The factory unlocks at 10k height or if they get a firewood before that point
                //So this is for players that can't get to 10k, thus, the lower they are the higher chance it is so they
                //have the chance to still unlock the factory

                float chance = p.getY() < chanceDecreaseThreshold ? prestigeCurrChanceMax : prestigeCurrChanceMin;
                if(rng.nextFloat() < chance){
                    placedPrestige = true;
                }

                if(placedPrestige){
                    int placementX = rng.nextInt(p.getX(), p.getX() + p.getWidth() - currencyWidth);
                    Currency curr = new Currency(placementX, p.getY() + p.getHeight(), true);
                    if(checkNoCurrencyOverlap(curr, platforms, checkpoints, faucets)) {
                        currencies.add(curr);
                    }
                }
            }

            //standard currency getting placed
            if(!placedPrestige){
                //how many can I place on this platform?
                int maxPlacements = Math.max(p.getWidth() / currencyWidth, 1); //at least 1 possible place on each platform

                //how many do I want to place on this platform
                int numPlacements = rng.nextInt(maxPlacements + 1); //+1 b/c exclusive bound

                float chanceOfPlacement = 0.05f; // only on 5% of platforms to start
                float maxChanceOfPlacement = 0.5f;
                float chance = Math.min(chanceOfPlacement + (bandY / maxPrestigeCurrencyThreshold), maxChanceOfPlacement);
                if(numPlacements > 0 && rng.nextFloat() <= chance){
                    //which side do I place from
                    boolean placeFromLeft = rng.nextBoolean();
                    Currency curr;
                    if(placeFromLeft){
                        for(int i = 0; i < numPlacements; i++){
                            curr = new Currency(p.getX() + i * Currency.WIDTH, p.getY() + p.getHeight());
                            if(checkNoCurrencyOverlap(curr, platforms, checkpoints, faucets)) {
                                currencies.add(curr);
                            }
                        }
                    } else{
                        //From 1 to numPlacements so it doesn't hang over the edge
                        for(int i = 1; i <= numPlacements; i++){
                            curr = new Currency(p.getX() + p.getWidth() - i * Currency.WIDTH, p.getY() + p.getHeight());
                            if(checkNoCurrencyOverlap(curr, platforms, checkpoints, faucets)) {
                                currencies.add(curr);
                            }
                        }
                    }
                }
            }
        }

        highestInfiniteY = (int) (bandY + BAND_HEIGHT);
        return highestPlatform;
    }

    private static boolean checkNoCurrencyOverlap(Currency curr, List<Platform> platforms, List<Checkpoint> checkpoints,
                                                  List<Faucet> faucets){
        Rectangle currencyRectangle = new Rectangle(curr.getX(), curr.getY(),
                                                Currency.WIDTH, Currency.HEIGHT);

        //base check of overlap
        boolean platformOverlap = platforms.stream()
            .anyMatch(p ->
                new Rectangle(p.getX(), p.getY() - PlayerData.playerHeight, p.getWidth(), p.getHeight() + PlayerData.playerHeight).overlaps(currencyRectangle));

        boolean checkpointOverlap = checkpoints.stream()
            .anyMatch(c ->
                new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight()).overlaps(currencyRectangle));

        boolean faucetOverlap = faucets.stream()
            .anyMatch(f ->
                new Rectangle(f.getX(), f.getY() - 10, f.getWidth(), f.getHeight() + 10).overlaps(currencyRectangle));
        // -10/+10 because the faucet floats 10 pixels above platform!

        return !platformOverlap && !checkpointOverlap && !faucetOverlap;
    }

    private static boolean checkReachability(Platform currPlatform, Platform highestPlatform){
        //check horizontal
        int horizontalMax = 325;
        if(currPlatform.getX() > highestPlatform.getX()){
            //check if right side of highest platform is within reach of left side of current platform
            if(currPlatform.getX() - (highestPlatform.getX() + highestPlatform.getWidth()) > horizontalMax){
                return false;
            }
        } else{
            //check if right side of curr platform is within reach of left side of highest platform
            if(highestPlatform.getX() - (currPlatform.getX() + currPlatform.getWidth()) > horizontalMax){
                return false;
            }
        }

        //check vertical
        int verticalMax = 100;
        return Math.abs((currPlatform.getY() + currPlatform.getHeight()) - (highestPlatform.getY() + highestPlatform.getHeight())) <= verticalMax;

        //none failed, reachable!
    }

    private static List<Platform> buildLevelFromModules(List<Module> moduleSequence, Vector2 startPoint) {
        List<Platform> result = new ArrayList<>();
        Vector2 currFront = new Vector2(startPoint);

        for (Module m : moduleSequence) {
            for (Platform r : m.getPlatforms()) {
                result.add(new Platform(currFront.x + r.getX(), currFront.y + r.getY(), r.getWidth(), r.getHeight()));
            }
            currFront.add(m.getEndOffset());
        }

        return result;
    }

    private static Module getModuleByName(String name) {
        for (Module m : ModuleLibrary.ALL_MODULES) {
            if (m.getName().equals(name)) return m;
        }
        throw new RuntimeException("Module not found: " + name);
    }

    public static void regenerateInfinite(){
        ALL_LEVELS.put(LevelType.INFINITE, buildInfiniteLevel());
    }

    public static Random getRng(){
        return rng;
    }

    public static long getSeed(){
        return seed;
    }
}
