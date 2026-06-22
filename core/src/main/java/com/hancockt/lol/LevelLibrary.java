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

import java.util.*;

public class LevelLibrary {
    public static Map<LevelType, LevelData> ALL_LEVELS = new HashMap<>();
    public static Random rng;

    static {
        ALL_LEVELS.put(LevelType.TUTORIAL, buildTutorialLevel());
        // ALL_LEVELS.put(LevelType.LEVEL_1, buildLevel1());
    }

    private static LevelData buildTutorialLevel(){
        //PR = 41s
        List<Checkpoint> checkpoints = new ArrayList<>();
        checkpoints.add(new Checkpoint(1930, 265, 1));
        checkpoints.add(new Checkpoint(3580, 285, 2));
        checkpoints.add(new Checkpoint(4760, 1825, 3));
        checkpoints.add(new Checkpoint(6435, 1910, 4));

        Rectangle waterTriggerZone = new Rectangle(6245, 1800, 50, 1000);
        Rectangle waterFixZone = new Rectangle(6920, 2000, 50, 1000);
        Rectangle winZone = new Rectangle(8125, 2163, 25, 75);

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
        boolean waterStartsActive = false;
        LevelType nextLevel = LevelType.INFINITE;


        return new LevelData(
            LevelType.TUTORIAL, checkpoints, waterTriggerZone, waterFixZone, winZone, platforms,
            startPoint, waterStartsActive, nextLevel
        );
    }

    private static LevelData buildInfiniteLevel(){
        //use seeds for reproduceability in infinite mode and leaderboard
        rng = new Random(System.currentTimeMillis());

        List<Checkpoint> checkpoints = new ArrayList<>();
        List<Platform> platforms = new ArrayList<>();

        //just call generate band for each band until height
        Platform highestPlatform = new Platform(0,0,75,25);
        for(int i = 0; i <= 10000; i += 250) {
            highestPlatform = generateBand(platforms, checkpoints, i, 250, 2000, highestPlatform);
        }
        Rectangle waterTriggerZone = null;
        Rectangle waterFixZone = null;
        Rectangle winZone = null;


        Vector2 startPoint = new Vector2(0,0);
        boolean waterStartsActive = true;
        LevelType nextLevel = null;

        return new LevelData(
            LevelType.INFINITE, checkpoints, waterTriggerZone, waterFixZone, winZone, platforms, startPoint,
            waterStartsActive, nextLevel
        );
    }

    private static Platform generateBand(List<Platform> platforms, List<Checkpoint> checkpoints, float bandY,
                                     float bandHeight, float levelWidth, Platform highestPlatform){
        //add checkpoint
        float checkpointHeightDistance = 2000f;
        float skipCheckpointThreshold = 0.1f;
        if(checkpoints.isEmpty() || (bandY - checkpoints.getLast().y > checkpointHeightDistance && rng.nextFloat() > skipCheckpointThreshold)){
            //add a checkpoint to the highest platform since it shouldn't overlap any other platform!
            float platformMidpoint = highestPlatform.x + (float)(highestPlatform.width / 2);
            float checkpointWidth = Math.min(25, highestPlatform.width);
            float checkpointHeight = 75;
            float checkpointX = platformMidpoint - (checkpointWidth / 2);
            float checkpointY = highestPlatform.y + highestPlatform.height;
            checkpoints.add(new Checkpoint(checkpointX, checkpointY, checkpointWidth, checkpointHeight,
                            checkpoints.size()));
        }

        //Difficulty
        float baseNumPlatforms = 10;
        float baseMaxWidth = 300;
        float baseMaxHeight = 150;
        float minNumPlatforms = 2;
        float minMaxWidth = 50;
        float minMaxHeight = 30;

        //TUNE - TODO
        //per 1000 score gained
        float numPlatformDecay = 0.7f;
        float widthDecay = 20;
        float heightDecay = 10;
        float decayMultiplier = bandY / 1000;

        //Define max ranges for randomness
        float numPlatforms = Math.max(baseNumPlatforms - (decayMultiplier * numPlatformDecay), minNumPlatforms);
        float maxWidth = Math.max(baseMaxWidth - (decayMultiplier * widthDecay), minMaxWidth);
        float maxHeight = Math.max(baseMaxHeight - (decayMultiplier * heightDecay), minMaxHeight);

        //platforms
        boolean reachable = false; //reachable from previous band

        //for overlapping, since only platforms in this band can overlap other ones
        List<Platform> bandPlatforms = new ArrayList<>();
        for(int i = 0; i < numPlatforms; i++) {
            Platform p;
            int attempts = 0;
            do {
                attempts++;
                float width = rng.nextFloat(20, maxWidth); //20 and max width!
                float height = rng.nextFloat(10, maxHeight); // 10 and max height!
                float xVal = rng.nextFloat(levelWidth - width); //btwn 0 and (levelWidth - width)
                float yVal = rng.nextFloat(bandY, Math.max(bandY, bandY + bandHeight - height)); //btwn bandY and
                // (bandY + bandHeight -
                // height)

                p = new Platform(xVal, yVal, width, height);

                //check overlap and checkpoints (check last checkpoint only)
                for(Platform bp : bandPlatforms){
                    Rectangle currP = new Rectangle(p.x, p.y, p.width, p.height);
                    Rectangle bandP = new Rectangle(bp.x, bp.y, bp.width, bp.height);

                    Checkpoint c1 = checkpoints.getLast();
                    Rectangle check = new Rectangle(c1.x, c1.y, c1.width, c1.height);

                    boolean overlapPlatform = currP.overlaps(bandP);
                    boolean overlapCheckpoint = currP.overlaps(check);
                    if(overlapPlatform || overlapCheckpoint){
                        float moveThreshold = 0.7f;
                        if(rng.nextFloat() > moveThreshold){
                            //move platform vertically
                            if(!overlapCheckpoint && bp.y + bp.height + p.height > bandY + bandHeight){
                                //move down
                                p.y = bp.y - p.height;
                            } else{
                                //move up
                                p.y = (int) Math.max(bp.y + bp.height, c1.y + c1.height);
                            }
                        } else{
                            //move platform horizontally
                            if(bp.x + bp.width + p.width > levelWidth){
                                //move left
                                p.x = bp.x - p.width;
                            } else{
                                //move right
                                p.x = bp.x + bp.width;
                            }
                        }
                    }
                }

                //check reachability - need at least one. First one added is reachable!
                reachable = checkReachability(p, highestPlatform) || reachable; // once true, stays true until next band
            } while (!reachable && attempts < 50);

            platforms.add(p);
            bandPlatforms.add(p);
        }

        //by having the highest platform update outside the for loop, the do-while loop continually tries to make a
        // platform that the previous band's highest platform can reach

        // check if new highest platform for checkpoint making
        for(Platform p : bandPlatforms) {
            if (p.y + p.height > highestPlatform.y + highestPlatform.height) {
                highestPlatform = p;
            }
        }

        return highestPlatform;
    }

    private static boolean checkReachability(Platform currPlatform, Platform highestPlatform){
        //check horizontal
        int horizontalMax = 325; //TODO - tune
        if(currPlatform.x > highestPlatform.x){
            //check if right side of highest platform is within reach of left side of current platform
            if(currPlatform.x - (highestPlatform.x + highestPlatform.width) > horizontalMax){
                return false;
            }
        } else{
            //check if right side of curr platform is within reach of left side of highest platform
            if(highestPlatform.x - (currPlatform.x + currPlatform.width) > horizontalMax){
                return false;
            }
        }

        //check vertical
        int verticalMax = 100; //TODO - tune
        if(Math.abs((currPlatform.y + currPlatform.height) - (highestPlatform.y + highestPlatform.height)) > verticalMax){
            return false;
        }

        //none failed, reachable!
        return true;
    }

    private static List<Platform> buildLevelFromModules(List<Module> moduleSequence, Vector2 startPoint) {
        List<Platform> result = new ArrayList<>();
        Vector2 currFront = new Vector2(startPoint);

        for (Module m : moduleSequence) {
            for (Platform r : m.platforms) {
                result.add(new Platform(currFront.x + r.x, currFront.y + r.y, r.width, r.height));
            }
            currFront.add(m.endOffset);
        }

        return result;
    }

    private static Module getModuleByName(String name) {
        for (Module m : ModuleLibrary.ALL_MODULES) {
            if (m.name.equals(name)) return m;
        }
        throw new RuntimeException("Module not found: " + name);
    }
}
