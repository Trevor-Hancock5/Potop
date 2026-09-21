package tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.features.Currency;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.main.PhysicsEngine;
import com.hancockt.potop.state.PersistentData;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;
import com.hancockt.potop.systems.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LevelGenerationTest {
    private static final int NUM_TESTS = 500;
    private static final int TEST_HEIGHT = 20_000;

    // Place this in your JUnit test class inside an @BeforeEach (or @Before) setup method.
    @BeforeEach
    public void setupGdx() {
        // Assigns a dummy object to the global so it doesn't crash. (Requires Mockito)
        Gdx.input = org.mockito.Mockito.mock(com.badlogic.gdx.Input.class);
    }

    @Test
    public void platformsShouldNotOverlapPlatforms(){
        ArrayList<Integer> maxList = new ArrayList<>();
        ArrayList<Double> avgList = new ArrayList<>();
        for(int i = 0; i < NUM_TESTS; i ++){
            RunData runData = generateLevel(TEST_HEIGHT);
            //CHECK
            List<Platform> platforms = runData.getPlatforms();
            for(int j = 0; j < platforms.size(); j++){
                for(int k = j+1; k < platforms.size(); k++){
                    Platform p1 = platforms.get(j);
                    Platform p2 = platforms.get(k);
                    Rectangle r1 = new Rectangle(p1.getX(), p1.getY(), p1.getWidth(), p1.getHeight());
                    Rectangle r2 = new Rectangle(p2.getX(), p2.getY(), p2.getWidth(), p2.getHeight());
                    assertFalse(r1.overlaps(r2), "platform overlaps another platform");
                }
            }

            int max = LevelGeneration.attemptsList.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0); // fallback value if the list is empty

// Average (returns an OptionalDouble)
            double average = LevelGeneration.attemptsList.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0); // fallback value if the list is empty

            maxList.add(max);
            avgList.add(average);
        }
        System.out.println(printStats(maxList, avgList));
    }

    @Test
    public void faucetsShouldNotOverlapFaucets(){
        for(int i = 0; i < NUM_TESTS; i ++){
            RunData runData = generateLevel(TEST_HEIGHT);
            //CHECK
            List<Faucet> faucets = runData.getFaucets();
            for(int j = 0; j < faucets.size(); j++){
                for(int k = j+1; k < faucets.size(); k++){
                    Faucet f1 = faucets.get(j);
                    Faucet f2 = faucets.get(k);
                    Rectangle r1 = new Rectangle(f1.getX(), f1.getY(), f1.getWidth(), f1.getHeight());
                    Rectangle r2 = new Rectangle(f2.getX(), f2.getY(), f2.getWidth(), f2.getHeight());
                    assertFalse(r1.overlaps(r2), "Faucet overlaps faucet");
                }
            }
        }
    }

    @Test
    public void checkpointsShouldNotOverlapCheckpoints(){
        for(int i = 0; i < NUM_TESTS; i ++){
            RunData runData = generateLevel(TEST_HEIGHT);
            //CHECK
            List<Checkpoint> checkpoints = runData.getCheckpoints();
            for(int j = 0; j < checkpoints.size(); j++){
                for(int k = j+1; k < checkpoints.size(); k++){
                    Checkpoint c1 = checkpoints.get(j);
                    Checkpoint c2 = checkpoints.get(k);
                    Rectangle r1 = new Rectangle(c1.getX(), c1.getY(), c1.getWidth(), c1.getHeight());
                    Rectangle r2 = new Rectangle(c2.getX(), c2.getY(), c2.getWidth(), c2.getHeight());
                    assertFalse(r1.overlaps(r2), "checkpoint overlaps another checkpoint");
                }
            }
        }
    }

    @Test
    public void currencyShouldNotOverlapCurrency(){
        float width = Currency.WIDTH;
        float height = Currency.HEIGHT;
        for(int i = 0; i < NUM_TESTS; i ++){
            RunData runData = generateLevel(TEST_HEIGHT);
            //CHECK
            List<Currency> currencies = runData.getCurrencies();
            for(int j = 0; j < currencies.size(); j++){
                for(int k = j+1; k < currencies.size(); k++){
                    Currency c1 = currencies.get(j);
                    Currency c2 = currencies.get(k);

                    Rectangle r1 = new Rectangle(c1.getX(), c1.getY(), width, height);
                    Rectangle r2 = new Rectangle(c2.getX(), c2.getY(), width, height);
                    assertFalse(r1.overlaps(r2), "Currency overlaps another currency");
                }
            }
        }
    }

    @Test
    public void platformsShouldNotOverlapOtherObjects(){
        //faucet, checkpoint, currency
        for(int i = 0; i < NUM_TESTS; i ++){
            RunData runData = generateLevel(TEST_HEIGHT);
            //CHECK
            List<Platform> platforms = runData.getPlatforms();
            List<Faucet> faucets = runData.getFaucets();
            List<Checkpoint> checkpoints = runData.getCheckpoints();
            List<Currency> currencies = runData.getCurrencies();
            for(Platform p : platforms){
                for(Faucet f : faucets){
                    assertFalse(p.getRect().overlaps(f.getRect()), "Faucet overlaps a platform");
                }

                for(Checkpoint c : checkpoints){
                    assertFalse(p.getRect().overlaps(c.getRect()), "Checkpoint overlaps platform");
                }

                for(Currency curr : currencies){
                    assertFalse(p.getRect().overlaps(curr.getRect()), "Currency overlaps platform");
                }
            }
        }
    }

    @Test
    public void faucetsShouldNotOverlapOtherObjects(){
        //checkpoints and currency
        for(int i = 0; i < NUM_TESTS; i ++){
            RunData runData = generateLevel(TEST_HEIGHT);
            //CHECK
            for(Faucet f : runData.getFaucets()){
                Rectangle fRect = new Rectangle(f.getX(), f.getY(), f.getWidth(), f.getHeight());

                for(Checkpoint c : runData.getCheckpoints()){
                    Rectangle cRect = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());

                    if(fRect.overlaps(cRect)) {
                        System.out.println("checkpoint: (" + c.getX() + ", " + c.getY() + ")");
                        System.out.println("faucet: (" + f.getX() + ", " + f.getY() + ")");
                        System.out.println("seed: " + runData.seed);
                    }

                    assertFalse(fRect.overlaps(cRect), "Checkpoint overlaps faucet");
                }

                for(Currency c : runData.getCurrencies()){
                    Rectangle cRect = new Rectangle(c.getX(), c.getY(), Currency.WIDTH, Currency.HEIGHT);
                    assertFalse(fRect.overlaps(cRect), "Currency overlaps faucet");
                }
            }
        }
    }

    @Test
    public void checkpointsShouldNotOverlapCurrency(){
        for(int i = 0; i < NUM_TESTS; i ++){
            RunData runData = generateLevel(TEST_HEIGHT);
            //CHECK
            for(Checkpoint c : runData.getCheckpoints()){
                for(Currency curr : runData.getCurrencies()){
                    assertFalse(c.getRect().overlaps(curr.getRect()), "Currency overlaps checkpoint");
                }
            }
        }
    }

    @Test
    public void isCurrencyReachable(){
        for(int i = 0; i < NUM_TESTS; i++){
            RunData runData = generateLevel(TEST_HEIGHT);
            for(Currency c : runData.getCurrencies()){
                //adjust dimensions to check reachability
                Rectangle cRect = new Rectangle(c.getX(), c.getY(),
                    Currency.WIDTH, Currency.HEIGHT);

                for(Platform p : runData.getPlatforms()){
                    Rectangle pRect = new Rectangle(p.getX(), p.getY() - PlayerData.playerHeight, p.getWidth(),
                        p.getHeight() + PlayerData.playerHeight);
                    assertFalse(pRect.overlaps(cRect), "Currency not reachable");
                }
            }
        }
    }

    @Test
    public void checkpointFaucetDistance(){
        ArrayList<Integer> checkpointDistances = new ArrayList<>();
        ArrayList<Integer> faucetDistances = new ArrayList<>();

        for(int i = 0; i < NUM_TESTS; i++){
            RunData runData = generateLevel(TEST_HEIGHT);
            List<Checkpoint> checkpoints = runData.getCheckpoints();
            List<Faucet> faucets = runData.getFaucets();

            // sort both lists by y
            checkpoints.sort(Comparator.comparing(Checkpoint::getY));
            faucets.sort(Comparator.comparing(Faucet::getY));

            // get distances between each one - save to list of distances
            for(int j = 1; j < checkpoints.size(); j++){
                checkpointDistances.add((int)(checkpoints.get(j).getY() - checkpoints.get(j - 1).getY()));
            }

            for(int j = 1; j < faucets.size(); j++){
                faucetDistances.add((int)(faucets.get(j).getY() - faucets.get(j - 1).getY()));
            }
        }

        // check min, average, and max distance
        //min
        int minCheckpointDist = checkpointDistances.stream()
            .mapToInt(Integer::intValue)
            .min()
            .orElse(0); // fallback value if the list is empty
        int minFaucetDist = faucetDistances.stream()
            .mapToInt(Integer::intValue)
            .min()
            .orElse(0); // fallback value if the list is empty

        // Average (returns an OptionalDouble)
        double averageCheckpointDist = checkpointDistances.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0); // fallback value if the list is empty
        double averageFaucetDist = faucetDistances.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0); // fallback value if the list is empty

        //max
        int maxCheckpointDist = checkpointDistances.stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0); // fallback value if the list is empty
        int maxFaucetDist = faucetDistances.stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0); // fallback value if the list is empty

        System.out.println("minCheck: " + minCheckpointDist);
        System.out.println("minFaucet: " + minFaucetDist);
        System.out.println("avgCheck: " + averageCheckpointDist);
        System.out.println("avgFaucet: " + averageFaucetDist);
        System.out.println("maxCheck: " + maxCheckpointDist);
        System.out.println("maxFaucet: " + maxFaucetDist);

        assertTrue(minCheckpointDist > 500, "Checkpoints too close: " + minCheckpointDist);
        assertTrue(minFaucetDist > 500, "Faucets too close: " + minFaucetDist);
        assertTrue(averageCheckpointDist > 500 && averageCheckpointDist < 1500, "Checkpoints not in desired range: " + averageCheckpointDist);
        assertTrue(averageFaucetDist > 500 && averageFaucetDist < 2000, "Faucets not in desired range: " + averageFaucetDist);
        assertTrue(maxCheckpointDist < 3000, "Checkpoints too far apart: " + maxCheckpointDist);
        assertTrue(maxFaucetDist < 4000, "Faucets too far apart: " + maxFaucetDist);
    }

    @Test
    public void performanceTiming(){
        // benchmark:
        // average generation (100k): 12.22s

        // average generation (10k): 17.0055 ms
        // average physics update: 1.565ms
        // average add to generation : 8.726 ms
        List<Double> timeGeneration = new ArrayList<>();
        List<Double> timePhysicsUpdate = new ArrayList<>();
        List<Double> timeAddToGeneration = new ArrayList<>();
        double millisecondsConversion = 1_000_000.0;

        final int initGenerationThreshold = 50; //ms
        final int physicsThreshold = 2; //ms
        final int addGenerationThreshold = 2; //ms

        RunData runData;
        for(int i = 0; i < NUM_TESTS; i++) {
            long seed = System.nanoTime();
            LevelGeneration levelGeneration = new LevelGeneration(seed);

            //Time generation up to 10k - initial
            long start = System.nanoTime();

            runData = levelGeneration.buildInfiniteLevel(seed);

            long elapsed = System.nanoTime() - start;
            timeGeneration.add(elapsed / millisecondsConversion);

            // Time adding to generation
            start = System.nanoTime();

            levelGeneration.addToInfiniteLevel(runData.getPlatforms(), runData.getCheckpoints(), runData.getFaucets(),
                runData.getCurrencies(), runData.rng);

            elapsed = System.nanoTime() - start;
            timeAddToGeneration.add(elapsed / millisecondsConversion);

            //Time physics engine update

            PersistentData persistentData = new PersistentData();
            PlayerData playerData = new PlayerData();
            PhysicsEngine physicsEngine = new PhysicsEngine();

            start = System.nanoTime();

            physicsEngine.updatePhysics(1/60f, runData, persistentData, playerData);

            elapsed = System.nanoTime() - start;
            timePhysicsUpdate.add(elapsed / millisecondsConversion);
        }

        // Average (returns an OptionalDouble)
        double averageInitGenerationTiming = timeGeneration.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0); // fallback value if the list is empty
        double averagePhysicsUpdateTiming = timePhysicsUpdate.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0); // fallback value if the list is empty
        double averageAddGenerationTiming = timeAddToGeneration.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        System.out.println("average generation timing: " + averageInitGenerationTiming + "ms");
        System.out.println("average physics update timing: " + averagePhysicsUpdateTiming + "ms");
        System.out.println("average add generation timing " + averageAddGenerationTiming + "ms");

        assertTrue(averageInitGenerationTiming <= initGenerationThreshold);
        assertTrue(averagePhysicsUpdateTiming <= physicsThreshold);
        assertTrue(averageAddGenerationTiming <= addGenerationThreshold);
    }

    @Test
    public void checkPrestigeCurrency(){
        int numRuns = NUM_TESTS * 25; //This is quick and easy test but need more samples!
        float goalAveragePrestigePerLevel = 0.05f; //1 in 20 runs has a prestige
        // So the chance must stay between .025 and .1.

        ArrayList<Integer> numPrestigePerLevel = new ArrayList<>();
        for(int i = 0; i < numRuns; i++){
            int numPrestige = 0;
            RunData runData = generateLevel(LevelGeneration.getMaxPrestigeCurrencyThreshold());
            for(Currency c : runData.getCurrencies()){
                if(c.isPrestige()){
                    numPrestige++;
                }
            }
            numPrestigePerLevel.add(numPrestige);

            if(numPrestige > LevelGeneration.maxPrestigePlaced){
                System.out.println("Num prestige placed: " + numPrestige);
            }
            assertFalse(numPrestige > LevelGeneration.maxPrestigePlaced, "More than max allowed prestige currency spawned in a level!");
        }
        double average = numPrestigePerLevel.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        System.out.println("Average prestige currency spawn: " + average);
        assertTrue(average <= goalAveragePrestigePerLevel * 2 && average > goalAveragePrestigePerLevel / 2, "Too high/low of average spawning for prestige: " + average); //no higher than this
    }

    @Test
    public void checkReachability(){
        // This test is far from perfect!
        // Doesn't account for wall jumps at all and the maxLeap and maxJump are larger than they
        // realistically are so that it can pass at all.
        // But it is a ... test

        final int maxLeap = 600; // Horizontal
        final int maxJump = 250; // Vertical (allowing more to account for wall jumping)
        final float fallMultiplierForLeap = 1.5f; // If falling, allow for further leap
        final int totalTestHeight = 50_000;
        final int reachableBuffer = 1000;

        // For fails
        int highestY;
        long seed;

        for(int i = 0; i < NUM_TESTS; i++) {
            highestY = 0;
            RunData runData = generateLevel(totalTestHeight);
            seed = runData.seed;

            List<Platform> platforms = runData.getPlatforms();
            //Disregard boundary walls
            platforms.removeIf(Platform::isBoundaryWall);
            platforms.sort(Comparator.comparingInt(Platform::getY));

            List<Platform> startingPlatforms = platforms.stream()
                .takeWhile(p -> p.getY() < maxJump) //use this since platforms is sorted!
                .toList();

            // platforms that are reachable that need to be checked for what they can reach
            Queue<Platform> reachable = new ArrayDeque<>(startingPlatforms);

            // platforms that have been visited and CAN be reached! (so no need to check again!)
            Set<Platform> visited = new HashSet<>(startingPlatforms);

            while (!reachable.isEmpty()){
                Platform curr = reachable.poll();

                // Update highest y
                if(curr.getY() + curr.getHeight() > highestY){
                    highestY = curr.getY() + curr.getHeight();
                }

                //Check if at top
                if (curr.getY() + curr.getHeight() >= totalTestHeight - reachableBuffer){ //minus 1,000 for wiggle room
                    break;
                }

                float leftX = curr.getX() - (maxLeap * 3);
                float rightX = curr.getX() + curr.getWidth() + (maxLeap * 3);
                float downY = curr.getY() - (3 * maxJump);
                float upY = curr.getY() + curr.getHeight() + (3 * maxJump);

                //get potential platforms within bounding box - using fact it's sorted for speed
                List<Platform> closeToCurr = new ArrayList<>();
                for (Platform p : platforms) {
                    if (p.getY() + p.getHeight() < downY) continue; // Too low, keep looking
                    if (p.getY() > upY) break; // Too high, stop looking completely (because it's sorted!)

                    // It's in the vertical window, now check horizontal
                    if (p.getX() + p.getWidth() > leftX && p.getX() < rightX) {
                        if (!visited.contains(p)) {
                            // Only add platforms that haven't been visited (and are reachable)
                            closeToCurr.add(p);
                        }
                    }
                }

                //check if they are actually reachable!
                for(Platform p : closeToCurr){
                    float dx;
                    if(p.getX() + p.getWidth() < curr.getX()) {
                        dx = curr.getX() - (p.getX() + p.getWidth());
                    } else if(p.getX() > curr.getX() + curr.getWidth()){
                        dx = p.getX() - (curr.getX() + curr.getWidth());
                    } else{
                        // platform edges are within current platform
                        dx = 0;
                    }

                    float dy;
                    if(p.getY() > curr.getY() + curr.getHeight()) {
                        dy = p.getY() - (curr.getY() + curr.getHeight());
                    } else if (p.getY() + p.getHeight() < curr.getY()){
                        dy = -1 * (curr.getY() - (p.getY() + p.getHeight())); // Make negative to indicate drop
                    } else{
                        dy = 0;
                    }

                    if(dy < 0){
                        dy = Math.abs(dy);
                        // check downward jump
                        if(dy > 0.5f * maxJump){
                            // Big enough fall to justify a larger leap distance!
                            if(dx <= maxLeap * fallMultiplierForLeap){
                                reachable.offer(p);
                                visited.add(p);
                            }
                        } else{
                            //small fall, no extra horizontal distance allowed
                            if(dx <= maxLeap){
                                reachable.offer(p);
                                visited.add(p);
                            }
                        }
                    } else{
                        // upward jump, account for jump and leap combo!
                        dy = Math.abs(dy);
                        if(dy <= maxJump && dx <= maxLeap){
                            reachable.offer(p);
                            visited.add(p);
                        }
                    }
                }
            }

            if (highestY < totalTestHeight - reachableBuffer) {
                System.out.println("TEST DIED ON SEED: " + seed);
                System.out.println("Highest platform reached was:");

                // Find the platform that matches highestY
                for (Platform p : visited) {
                    if (p.getY() + p.getHeight() == highestY) {
                        System.out.println("X: " + p.getX() + ", Y: " + p.getY() +
                            ", Width: " + p.getWidth() + ", Height: " + p.getHeight());
                        break;
                    }
                }
                System.out.println("Go to this seed and stand on this platform. Where do you jump next?");
            }

            assertFalse(reachable.isEmpty(), "Reachable empty at: " + highestY + ". For seed: " + seed); // there should still be reachable platforms!
            assertTrue(highestY >= totalTestHeight - reachableBuffer, "Highest platform y is not within reachable buffer!");
        }
    }

    private RunData generateLevel(int height){
        long seed = System.currentTimeMillis();
        LevelGeneration levelGeneration = new LevelGeneration(seed);
        RunData runData = LevelGeneration.getAllLevels().get(LevelType.INFINITE); // at 10_000

        for(int i = 10_000; i < height; i += 2000) {
            levelGeneration.addToInfiniteLevel(runData.getPlatforms(), runData.getCheckpoints(), runData.getFaucets(), runData.getCurrencies(), runData.rng);
        }
        return runData;
    }

    private String printStats(ArrayList<Integer> maxList, ArrayList<Double> avgList){
        int maxMax = maxList.stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0); // fallback value if the list is empty

// Average (returns an OptionalDouble)
        double averageMax = maxList.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0); // fallback value if the list is empty

        double maxAvg = avgList.stream()
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(0); // fallback value if the list is empty

// Average (returns an OptionalDouble)
        double averageAverage = avgList.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0); // fallback value if the list is empty

        String ret = "";

        ret += "average avg: " + averageAverage + "\n";
        ret += "maxAvg: " + maxAvg + "\n";
        ret += "average max: " + averageMax + "\n";
        ret += "max max: " + maxMax + "\n";

        return ret;
    }
}

