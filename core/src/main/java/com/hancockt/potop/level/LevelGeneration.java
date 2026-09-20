package com.hancockt.potop.level;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.features.Currency;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

import java.util.*;


public class LevelGeneration {
    private static final Map<LevelType, RunData> ALL_LEVELS = new HashMap<>();
    private int highestInfiniteY = 0;
    private Platform highestPlatform;
    private int checkedCurrencyUpToHere; //used for checking currency overlap

    private static final int BAND_HEIGHT = 250; //TUNABLE
    private static final float LEVEL_WIDTH = 3000; //TUNABLE
    private static final int PRE_GENERATION_HEIGHT = 10_000; //TUNABLE
    public static final int GENERATION_HEIGHT = 2000; //TUNABLE

    private static final float minCheckpointHeightDistance = 800f; //TUNABLE
    private static final float maxCheckpointHeightDistance = 1600f; //TUNABLE
    private static final float skipCheckpointThreshold = 0.08f; //TUNABLE

    //means to divide all 3 above and below by this and then subtract their values by that
    // thus increasing # of checkpoints and faucets!
    private static final float increasedDivisorUpHigh = 4f; //TUNABLE
    private static final float increasedChanceHeight = 8_000f; //TUNABLE

    private static final float minFaucetHeightDistance = 750f; //TUNABLE
    private static final float maxFaucetHeightDistance = 1500f; //TUNABLE
    private static final float skipFaucetThreshold = 0.25f; //TUNABLE
    private static final float faucetGapAbovePlatform = 10f; // pixels floating above platform - TUNABLE
    private static final float minFaucetSpeedReduction = 10; //TUNABLE
    private static final float maxFaucetSpeedReduction = 50; //TUNABLE

    private static final float minNumPlatformsPerBand = 5; //TUNABLE
    private static final int maxAttemptsPlacingPlatforms = 500; //TUNABLE

    private static final int maxPrestigeCurrencyThreshold = 10_000; //TUNABLE
    private static final int minPrestigeCurrencyThreshold = 1_000; //TUNABLE
    private static final int chanceDecreaseThreshold = 5_000; //TUNABLE
    private static final float prestigeCurrChanceMax = 0.0002f; // TUNABLE
    private static final float prestigeCurrChanceMin = 0.000001f; //TUNABLE
    private static final float minCurrencyChanceOfPlacement = 0.05f; // only on 5% of platforms to start - TUNABLE
    private static final float maxCurrencyChanceOfPlacement = 0.5f; //TUNABLE
    private static int numPrestigePlaced = 0;
    public static final int maxPrestigePlaced = 5;

    private static final int reachableHorizontalMax = 400; //TUNABLE
    private static final int reachableVerticalMax = 90; //TUNABLE


    public static ArrayList<Integer> attemptsList = new ArrayList<>(); //For testing class //TODO - remove for release

    public LevelGeneration(long seed){
        numPrestigePlaced = 0; //reset
        checkedCurrencyUpToHere = 0;
        ALL_LEVELS.put(LevelType.LEARNING_MODE, buildTutorialLevel());
        ALL_LEVELS.put(LevelType.INFINITE, buildInfiniteLevel(seed));
    }

    public static Map<LevelType, RunData> getAllLevels(){
        return ALL_LEVELS;
    }

    public int getHighestYInInfinite(){
        return highestInfiniteY;
    }

    public static int getMaxPrestigeCurrencyThreshold(){
        return maxPrestigeCurrencyThreshold;
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

    public RunData buildInfiniteLevel(long seed){
        List<Checkpoint> checkpoints = new ArrayList<>();
        List<Platform> platforms = new ArrayList<>();
        List<Faucet> faucets = new ArrayList<>();
        List<Currency> currencies = new ArrayList<>();

        Random rng = new Random(seed);
        //just call generate band for each band until height
        highestPlatform = new Platform(0,0,75,25);
        // Generate up to PRE_GENERATION_HEIGHT to improve performance
        for(int i = 0; i <= PRE_GENERATION_HEIGHT; i += BAND_HEIGHT) {
            highestPlatform = generateBand(platforms, checkpoints, i, highestPlatform,
                                            faucets, currencies, rng);
        }

        removeOverlappingCurrency(currencies, platforms, checkpoints, faucets);

        return new RunData(
            checkpoints, platforms, new Vector2(0,0), faucets, currencies, LevelType.INFINITE, null, seed
        );
    }

    public void addToInfiniteLevel(List<Platform> platforms, List<Checkpoint> checkpoints,
                                               List<Faucet> faucets, List<Currency> currencies, Random rng){
        //Add another 2000 height to the infinite level
        int maxAdditionY = highestInfiniteY + GENERATION_HEIGHT;
        for(int i = highestInfiniteY; i < maxAdditionY; i += BAND_HEIGHT){
            highestPlatform = generateBand(platforms, checkpoints, i,
                highestPlatform, faucets, currencies, rng);
        }
        removeOverlappingCurrency(currencies, platforms, checkpoints, faucets);
    }

    private Platform generateBand(List<Platform> platforms, List<Checkpoint> checkpoints, float bandY,
                                         Platform highestPlatform, List<Faucet> faucets, List<Currency> currencies,
                                         Random rng){

        float minimumDistance = increaseChanceCheck(bandY, minCheckpointHeightDistance);
        float maximumDistance = increaseChanceCheck(bandY, maxCheckpointHeightDistance);
        float chanceToSkip = increaseChanceCheck(bandY, skipCheckpointThreshold);

        //add checkpoint
        float checkpointHeightDistance = rng.nextFloat(minimumDistance, maximumDistance); //add some randomness for kicks and giggles!
        boolean checkpointAdded = false;
        if(checkpoints.isEmpty() || (bandY - checkpoints.getLast().getY() > checkpointHeightDistance && rng.nextFloat()
                > chanceToSkip)){
            //add a checkpoint to the highest platform since it shouldn't overlap any other platform!
            checkpointAdded = true;
            float platformMidpoint = highestPlatform.getX() + (float)(highestPlatform.getWidth() / 2);
            float checkpointWidth = Math.min(25, highestPlatform.getWidth());
            float checkpointHeight = 75;
            float checkpointX = platformMidpoint - (checkpointWidth / 2);
            float checkpointY = highestPlatform.getY() + highestPlatform.getHeight();

            Checkpoint checkpoint = new Checkpoint(checkpointX, checkpointY, checkpointWidth, checkpointHeight,
                checkpoints.size());

            // check last 3 faucets
            boolean noOverlap = true;
            for(int i = Math.max(0, faucets.size() - 3); i < faucets.size(); i++){
                if(checkpoint.getRect().overlaps(faucets.get(i).getRect())){
                    noOverlap = false;
                }
            }

            if(!checkpoints.isEmpty() && checkpoints.getLast().getRect().overlaps(checkpoint.getRect())){
                noOverlap = false;
            }
            if(noOverlap) {
                checkpoints.add(checkpoint);
            }
        }

        minimumDistance = increaseChanceCheck(bandY, minFaucetHeightDistance);
        maximumDistance = increaseChanceCheck(bandY, maxFaucetHeightDistance);
        chanceToSkip = increaseChanceCheck(bandY, skipFaucetThreshold);

        //add faucets
        float faucetHeightDistance = rng.nextFloat(minimumDistance, maximumDistance);
        if(!checkpointAdded && (faucets.isEmpty() || (bandY - faucets.getLast().getY() > faucetHeightDistance
                && rng.nextFloat() > chanceToSkip))){
            //add a faucet to highest platform
            float platformMidpoint = highestPlatform.getX() + (float)(highestPlatform.getWidth() / 2);
            float faucetDimensions = 25; //width/height
            float faucetX = platformMidpoint - faucetDimensions / 2;
            float faucetY = highestPlatform.getY() + highestPlatform.getHeight() + faucetGapAbovePlatform; //float in air
            float speedReduction = rng.nextFloat(minFaucetSpeedReduction, maxFaucetSpeedReduction); //how much to slow the water_rise_speed

            Faucet faucet = new Faucet(faucetX, faucetY, faucetDimensions, faucetDimensions, speedReduction, false);

            // check last 3 checkpoints
            boolean noOverlap = true;
            for(int i = Math.max(0, checkpoints.size() - 3); i < checkpoints.size(); i++){
                if(faucet.getRect().overlaps(checkpoints.get(i).getRect())){
                    noOverlap = false;
                }
            }

            if(!faucets.isEmpty() && faucets.getLast().getRect().overlaps(faucet.getRect())){
                noOverlap = false;
            }
            if(noOverlap) {
                faucets.add(faucet);
            }
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
        float avgWidth = (maxWidth + minWidth) / 2f;
        float avgHeight = (maxHeight + minHeight) / 2f;
        float avgPlatformArea = avgWidth * avgHeight;

        float targetCoveredArea = bandArea * coverageRatio;
        int numPlatforms = (int) Math.max(minNumPlatformsPerBand, targetCoveredArea / avgPlatformArea);

        //platforms
        boolean reachable = false; //reachable from previous band

        //for overlapping, since only platforms in this band can overlap other ones
        List<Platform> previousBandPlatforms = new ArrayList<>();
        for(Platform p : platforms){
            // add platforms to band platforms from previous band to check for overlap
            // this is also a performance improvement so not all platforms are checked for overlap
            if(p.getY() > bandY - BAND_HEIGHT){
                previousBandPlatforms.add(p);
            }
        }
        List<Platform> bandPlatforms = new ArrayList<>(previousBandPlatforms); //start with previous band platforms!

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
                for(Platform allP : bandPlatforms){
                    if(allP.getY() > p.getY() + p.getHeight()
                            || allP.getY() + allP.getHeight() < p.getY()){
                        continue;
                    }

                    // All gaps between platforms horizontally should be playerWidth wide
                    Rectangle currP = new Rectangle(p.getX() - ((float) PlayerData.playerWidth), p.getY(),
                        p.getWidth() + ((float) PlayerData.playerWidth), p.getHeight());
                    Rectangle bandP = new Rectangle(allP.getX() - ((float) PlayerData.playerWidth), allP.getY(),
                        allP.getWidth() + ((float) PlayerData.playerWidth), allP.getHeight());

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
                    overlapsFaucet = p.getRect().overlaps(faucetRect);
                }

                overlapsCheckpoint = false;
                if (!checkpoints.isEmpty()) {
                    Checkpoint c = checkpoints.getLast();
                    Rectangle checkRect = new Rectangle(c.getX(), c.getY(), c.getWidth() * 2, c.getHeight() * 2);
                    overlapsCheckpoint = p.getRect().overlaps(checkRect);
                }

                //check reachability - need at least one. First one added is reachable!
                if(checkReachability(p, highestPlatform)){
                    reachable = true;
                }

                // until next band
            } while ((attempts < maxAttemptsPlacingPlatforms) && (needsRetry || overlapsFaucet || overlapsCheckpoint || !reachable));


            if(!(needsRetry || overlapsFaucet || overlapsCheckpoint)){
                // Only actually place if not overlapping!
                attemptsList.add(attempts);
                platforms.add(p);
                bandPlatforms.add(p);
            }
        }

        //by having the highest platform update outside the for loop, the do-while loop continually tries to make a
        // platform that the previous band's highest platform can reach

        //remove previous band platforms before adding currency
        bandPlatforms.removeAll(previousBandPlatforms);

        // check if new highest platform for checkpoint making and add currency!
        for(Platform p : bandPlatforms) {
            if (p.top() > highestPlatform.top()) {
                highestPlatform = p;
            }

            //prestige currency chance, super low chance just in case player can't reach factory threshold
            boolean hasFactory = false; //TODO - once logic in place, get this dynamically!

            boolean justPlacedPrestige = false;
            if(!hasFactory && p.getY() < maxPrestigeCurrencyThreshold && p.getY() > minPrestigeCurrencyThreshold
                    && numPrestigePlaced < maxPrestigePlaced){ // Only place 1 prestige currency per generation!
                //The factory unlocks at 10k height or if they get a firewood before that point
                //So this is for players that can't get to 10k, thus, the lower they are the higher chance it is so they
                //have the chance to still unlock the factory

                float chance = p.getY() < chanceDecreaseThreshold ? prestigeCurrChanceMax : prestigeCurrChanceMin;

                if(rng.nextFloat() < chance){
                    int placementX = rng.nextInt(p.getX(), p.right() - (int)Currency.WIDTH);
                    Currency curr = new Currency(placementX, p.top(), true);
                    if(checkNoCurrencyOverlap(curr, platforms, checkpoints, faucets)) {
                        justPlacedPrestige = true;
                        currencies.add(curr);
                        numPrestigePlaced++;
                    }
                }
            }

            //standard currency getting placed
            if(!justPlacedPrestige){
                //how many can I place on this platform?
                int maxPlacements = Math.max((int) (p.getWidth() / Currency.WIDTH), 1); //at least 1 possible place on each platform

                //how many do I want to place on this platform
                int numPlacements = rng.nextInt(maxPlacements + 1); //+1 b/c exclusive bound

                float chance = Math.min(minCurrencyChanceOfPlacement + (bandY / maxPrestigeCurrencyThreshold), maxCurrencyChanceOfPlacement);
                if(numPlacements > 0 && rng.nextFloat() <= chance){
                    //which side do I place from
                    boolean placeFromLeft = rng.nextBoolean();
                    Currency curr;
                    if(placeFromLeft){
                        for(int i = 0; i < numPlacements; i++){
                            curr = new Currency(p.getX() + i * Currency.WIDTH, p.top());
                            if(checkNoCurrencyOverlap(curr, platforms, checkpoints, faucets)) {
                                currencies.add(curr);
                            }
                        }
                    } else{
                        //From 1 to numPlacements so it doesn't hang over the edge
                        for(int i = 1; i <= numPlacements; i++){
                            curr = new Currency(p.right() - i * Currency.WIDTH, p.top());
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

    private float increaseChanceCheck(float bandY, float val){
        return bandY > increasedChanceHeight ?
            val - val / increasedDivisorUpHigh :
            val;
    }

    private boolean checkNoCurrencyOverlap(Currency curr, List<Platform> platforms, List<Checkpoint> checkpoints,
                                                  List<Faucet> faucets){
        Rectangle currencyRectangle = new Rectangle(curr.getX(), curr.getY(),
                                                Currency.WIDTH, Currency.HEIGHT);

        boolean platformOverlap = false;
        for(Platform p : platforms){
            if (p.getY() - PlayerData.playerHeight > curr.getY() + Currency.HEIGHT || p.getY() + p.getHeight() < curr.getY()){
                continue;
            }

            platformOverlap = currencyRectangle.overlaps(new Rectangle(p.getX(), p.getY() - PlayerData.playerHeight, p.getWidth(), p.getHeight() + PlayerData.playerHeight));
            if(platformOverlap){
                break;
            }
        }

        boolean checkpointOverlap = false;
        for(Checkpoint c : checkpoints){
            if (c.getY() > curr.getY() + Currency.HEIGHT || c.getY() + c.getHeight() < curr.getY()){
                continue;
            }

            checkpointOverlap = currencyRectangle.overlaps(new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
            if(checkpointOverlap){
                break;
            }
        }

        boolean faucetOverlap = false;
        for(Faucet f : faucets){
            if (f.getY() > curr.getY() + Currency.HEIGHT || f.getY() + f.getHeight() < curr.getY()){
                continue;
            }

            faucetOverlap = currencyRectangle.overlaps(new Rectangle(f.getX(), f.getY() - faucetGapAbovePlatform, f.getWidth(), f.getHeight() + faucetGapAbovePlatform));
            if(faucetOverlap){
                break;
            }
        }

        return !platformOverlap && !checkpointOverlap && !faucetOverlap;
    }

    private void removeOverlappingCurrency(List<Currency> currencies, List<Platform> platforms,
                                           List<Checkpoint> checkpoints, List<Faucet> faucets){
        ArrayList<Currency> toRemove = new ArrayList<>();
        int highestNewCurrY = checkedCurrencyUpToHere;
        for(Currency c : currencies){
            if(c.getY() < checkedCurrencyUpToHere - BAND_HEIGHT){
                continue;
            }

            if(!checkNoCurrencyOverlap(c, platforms, checkpoints, faucets)){
                toRemove.add(c);
            }
            highestNewCurrY = Math.max(checkedCurrencyUpToHere, (int) c.getY());
        }

        checkedCurrencyUpToHere = highestNewCurrY;
        currencies.removeAll(toRemove);
    }

    private static boolean checkReachability(Platform currPlatform, Platform highestPlatform){
        //check horizontal
        if(currPlatform.getX() > highestPlatform.getX()){
            //check if right side of highest platform is within reach of left side of current platform
            if(currPlatform.getX() - (highestPlatform.getX() + highestPlatform.getWidth()) > reachableHorizontalMax){
                return false;
            }
        } else{
            //check if right side of curr platform is within reach of left side of highest platform
            if(highestPlatform.getX() - (currPlatform.getX() + currPlatform.getWidth()) > reachableHorizontalMax){
                return false;
            }
        }

        //check vertical
        return Math.abs((currPlatform.getY() + currPlatform.getHeight()) - (highestPlatform.getY() + highestPlatform.getHeight())) <= reachableVerticalMax;

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

    public void regenerateInfinite(long seed){
        ALL_LEVELS.put(LevelType.INFINITE, buildInfiniteLevel(seed));
    }
}
