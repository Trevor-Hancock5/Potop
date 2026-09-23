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
    private static int numPrestigePlaced = 0;
    private static final int faucetGapAbovePlatform = 10;
    public static ArrayList<Integer> attemptsList = new ArrayList<>(); //For testing class //TODO - remove for release

    public LevelGeneration(long seed){
        numPrestigePlaced = 0; //reset
        checkedCurrencyUpToHere = 0;
        ALL_LEVELS.put(LevelType.LEARNING_MODE, buildInfiniteLevel(seed, LevelType.LEARNING_MODE));
        ALL_LEVELS.put(LevelType.INFINITE, buildInfiniteLevel(seed, LevelType.INFINITE));
    }

    public static Map<LevelType, RunData> getAllLevels(){
        return ALL_LEVELS;
    }

    public int getHighestYInInfinite(){
        return highestInfiniteY;
    }

    public static int getMaxPrestigeCurrencyThreshold(LevelConfig lvCon){
        return lvCon.maxPrestigeCurrencyThreshold();
    }

    public RunData buildInfiniteLevel(long seed, LevelType levelType){
        List<Checkpoint> checkpoints = new ArrayList<>();
        List<Platform> platforms = new ArrayList<>();
        List<Faucet> faucets = new ArrayList<>();
        List<Currency> currencies = new ArrayList<>();
        Random rng = new Random(seed);
        LevelConfig lvCon = levelType == LevelType.LEARNING_MODE ? LevelConfig.beginnerMode() :
                LevelConfig.defaultInfinite();
        //just call generate band for each band until height
        highestPlatform = new Platform(0,-25,75,25);
        // Generate up to PRE_GENERATION_HEIGHT to improve performance
        for(int i = 0; i <= PRE_GENERATION_HEIGHT; i += BAND_HEIGHT) {
            highestPlatform = generateBand(platforms, checkpoints, i, highestPlatform,
                                            faucets, currencies, rng, lvCon);
        }

        removeOverlappingCurrency(currencies, platforms, checkpoints, faucets);

        return new RunData(
            checkpoints, platforms, new Vector2(0,0), faucets, currencies, LevelType.INFINITE, null, seed
        );
    }

    public void addToInfiniteLevel(List<Platform> platforms, List<Checkpoint> checkpoints,
                                               List<Faucet> faucets, List<Currency> currencies, Random rng,
                                               LevelType levelType){
        checkedCurrencyUpToHere = 0; //Reset check! TODO - why needed and fix currency generation
        //Add another 2000 height to the infinite level
        int maxAdditionY = highestInfiniteY + GENERATION_HEIGHT;
        LevelConfig lvCon = levelType == LevelType.LEARNING_MODE ? LevelConfig.beginnerMode() :
            LevelConfig.defaultInfinite();
        for(int i = highestInfiniteY; i < maxAdditionY; i += BAND_HEIGHT){
            highestPlatform = generateBand(platforms, checkpoints, i,
                highestPlatform, faucets, currencies, rng, lvCon);
        }
        removeOverlappingCurrency(currencies, platforms, checkpoints, faucets);
    }

    private Platform generateBand(List<Platform> platforms, List<Checkpoint> checkpoints, float bandY,
                                         Platform highestPlatform, List<Faucet> faucets, List<Currency> currencies,
                                         Random rng, LevelConfig lvCon){

        float minimumDistance = increaseChanceCheck(bandY, lvCon.minCheckpointHeightDistance(), lvCon);
        float maximumDistance = increaseChanceCheck(bandY, lvCon.maxCheckpointHeightDistance(), lvCon);
        float chanceToSkip = increaseChanceCheck(bandY, lvCon.skipCheckpointThreshold(), lvCon);

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

        minimumDistance = increaseChanceCheck(bandY, lvCon.minFaucetHeightDistance(), lvCon);
        maximumDistance = increaseChanceCheck(bandY, lvCon.maxFaucetHeightDistance(), lvCon);
        chanceToSkip = increaseChanceCheck(bandY, lvCon.skipFaucetThreshold(), lvCon);

        //add faucets
        float faucetHeightDistance = rng.nextFloat(minimumDistance, maximumDistance);
        if(!checkpointAdded && (faucets.isEmpty() || (bandY - faucets.getLast().getY() > faucetHeightDistance
                && rng.nextFloat() > chanceToSkip))){
            //add a faucet to highest platform
            float platformMidpoint = highestPlatform.getX() + (float)(highestPlatform.getWidth() / 2);
            float faucetX = platformMidpoint - Faucet.defaultDimensions / 2;
            float faucetY = highestPlatform.getY() + highestPlatform.getHeight() + faucetGapAbovePlatform; //float in air
            float speedReduction = rng.nextFloat(lvCon.minFaucetSpeedReduction(), lvCon.maxFaucetSpeedReduction()); //how much to slow the water_rise_speed

            Faucet faucet = new Faucet(faucetX, faucetY, speedReduction, false);

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

        //Density
        float coverageDecay = (lvCon.baseCoverageRatio() - lvCon.minCoverageRatio()) / (lvCon.maxDifficultyHeight());
        float coverageRatio = Math.max(lvCon.minCoverageRatio(), lvCon.baseCoverageRatio() - (bandY * coverageDecay));

        //width and height
        float widthDecay = (lvCon.basePlatformMaxWidth() - lvCon.minMaxPlatformWidth()) / (lvCon.maxDifficultyHeight());
        float heightDecay = (lvCon.basePlatformMaxHeight() - lvCon.minMaxPlatformHeight()) / (lvCon.maxDifficultyHeight());
        float maxWidth = Math.max(lvCon.basePlatformMaxWidth() - (bandY * widthDecay), lvCon.minMaxPlatformWidth());
        float maxHeight = Math.max(lvCon.basePlatformMaxHeight() - (bandY * heightDecay), lvCon.minMaxPlatformHeight());

        //dynamically choose numPlatforms based on density
        float avgWidth = (maxWidth + lvCon.minPlatformWidth()) / 2f;
        float avgHeight = (maxHeight + lvCon.minPlatformHeight()) / 2f;
        float avgPlatformArea = avgWidth * avgHeight;

        float targetCoveredArea = bandArea * coverageRatio;
        int numPlatforms = (int) Math.max(lvCon.minNumPlatformsPerBand(), targetCoveredArea / avgPlatformArea);

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
                float width = rng.nextFloat(lvCon.minPlatformWidth(), maxWidth); //20 and max width!
                float height = rng.nextFloat(lvCon.minPlatformHeight(), maxHeight); // 10 and max height!

                //btwn 0 and (LEVEL_WIDTH - width), but higher likelihood to spawn in center!
                float usableWidth = LEVEL_WIDTH - width;

                float uniformGeneration = rng.nextFloat();
                float triangleGeneration = (rng.nextFloat() + rng.nextFloat()) * 0.5f; // focus on more platforms in middle

                float finalRatio = (float) (uniformGeneration * (1.0 - lvCon.centerBiasForPlatforms()) + triangleGeneration * lvCon.centerBiasForPlatforms());
                float xVal = finalRatio * usableWidth;

                // btwn bandY and (bandY + BAND_HEIGHT - height)
                float yVal = rng.nextFloat(bandY, Math.max(bandY + 1, bandY + BAND_HEIGHT - height));

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
                if(checkReachability(p, highestPlatform, lvCon)){
                    reachable = true;
                }

                // until next band
            } while ((attempts < lvCon.maxAttemptsPlacingPlatforms()) && (needsRetry || overlapsFaucet || overlapsCheckpoint || !reachable));


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
            if(!hasFactory && p.getY() < lvCon.maxPrestigeCurrencyThreshold() && p.getY() > lvCon.minPrestigeCurrencyThreshold()
                    && numPrestigePlaced < lvCon.maxPrestigePlaced()){ // Only place 1 prestige currency per generation!
                //The factory unlocks at 10k height or if they get a firewood before that point
                //So this is for players that can't get to 10k, thus, the lower they are the higher chance it is so they
                //have the chance to still unlock the factory

                float chance = p.getY() < lvCon.chancePrestigeDecreaseThreshold() ? lvCon.prestigeCurrChanceMax() : lvCon.prestigeCurrChanceMin();

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

                float chance = Math.min(lvCon.minCurrencyChanceOfPlacement() + (bandY / lvCon.maxPrestigeCurrencyThreshold()), lvCon.maxCurrencyChanceOfPlacement());
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

    private float increaseChanceCheck(float bandY, float val, LevelConfig lvCon){
        return bandY > lvCon.increasedChanceHeight() ?
            val - val / lvCon.increasedDivisorUpHigh() :
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
        if(!platformOverlap) {
            for (Checkpoint c : checkpoints) {
                if (c.getY() > curr.getY() + Currency.HEIGHT || c.getY() + c.getHeight() < curr.getY()) {
                    continue;
                }

                checkpointOverlap = currencyRectangle.overlaps(new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
                if (checkpointOverlap) {
                    break;
                }
            }
        }

        boolean faucetOverlap = false;
        if(!platformOverlap && !checkpointOverlap) {
            for (Faucet f : faucets) {
                if (f.getY() > curr.getY() + Currency.HEIGHT || f.getY() + f.getHeight() < curr.getY()) {
                    continue;
                }

                faucetOverlap = currencyRectangle.overlaps(new Rectangle(f.getX(), f.getY() - faucetGapAbovePlatform, f.getWidth(), f.getHeight() + faucetGapAbovePlatform));
                if (faucetOverlap) {
                    break;
                }
            }
        }

        return !platformOverlap && !checkpointOverlap && !faucetOverlap;
    }

    private void removeOverlappingCurrency(List<Currency> currencies, List<Platform> platforms,
                                           List<Checkpoint> checkpoints, List<Faucet> faucets){
        ArrayList<Currency> toRemove = new ArrayList<>();
        int highestNewCurrY = checkedCurrencyUpToHere;
        for(Currency c : currencies){
            if(c.getY() < checkedCurrencyUpToHere - (2 * BAND_HEIGHT)){
                continue;
            }

            if(!checkNoCurrencyOverlap(c, platforms, checkpoints, faucets)){
                toRemove.add(c);
            }
            highestNewCurrY = Math.max(highestNewCurrY, (int) c.getY());
        }

        checkedCurrencyUpToHere = highestNewCurrY;
        currencies.removeAll(toRemove);
    }

    private static boolean checkReachability(Platform currPlatform, Platform highestPlatform, LevelConfig lvCon){
        //check horizontal
        if(currPlatform.getX() > highestPlatform.getX()){
            //check if right side of highest platform is within reach of left side of current platform
            if(currPlatform.getX() - (highestPlatform.getX() + highestPlatform.getWidth()) > lvCon.reachableHorizontalMax()){
                return false;
            }
        } else{
            //check if right side of curr platform is within reach of left side of highest platform
            if(highestPlatform.getX() - (currPlatform.getX() + currPlatform.getWidth()) > lvCon.reachableHorizontalMax()){
                return false;
            }
        }

        //check vertical
        return Math.abs((currPlatform.getY() + currPlatform.getHeight()) - (highestPlatform.getY() + highestPlatform.getHeight())) <= lvCon.reachableVerticalMax();

        //none failed, reachable!
    }

    public void regenerateInfinite(long seed){
        ALL_LEVELS.put(LevelType.INFINITE, buildInfiniteLevel(seed, LevelType.INFINITE));
    }
}
