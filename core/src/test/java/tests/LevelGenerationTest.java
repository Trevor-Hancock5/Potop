package tests;

import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.features.Currency;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LevelGenerationTest {
    private static final int NUM_GENERATED_LEVELS = 100;
    private static final int TEST_HEIGHT = 14_000;

    @Test
    public void platformsShouldNotOverlapPlatforms(){
        ArrayList<Integer> maxList = new ArrayList<>();
        ArrayList<Double> avgList = new ArrayList<>();
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
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
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
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
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
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
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
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
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
            RunData runData = generateLevel(TEST_HEIGHT);
            //CHECK
            List<Platform> platforms = runData.getPlatforms();
            List<Faucet> faucets = runData.getFaucets();
            List<Checkpoint> checkpoints = runData.getCheckpoints();
            List<Currency> currencies = runData.getCurrencies();
            for(Platform p : platforms){
                Rectangle platRect = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());
                for(Faucet f : faucets){
                    Rectangle fRect = new Rectangle(f.getX(), f.getY(), f.getWidth(), f.getHeight());
                    assertFalse(platRect.overlaps(fRect), "Faucet overlaps a platform");
                }

                for(Checkpoint c : checkpoints){
                    Rectangle checkRect = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());
                    assertFalse(platRect.overlaps(checkRect), "Checkpoint overlaps platform");
                }

                for(Currency curr : currencies){
                    Rectangle currRect = new Rectangle(curr.getX(), curr.getY(), Currency.WIDTH, Currency.HEIGHT);
                    assertFalse(platRect.overlaps(currRect), "Currency overlaps platform");
                }
            }
        }
    }

    @Test
    public void faucetsShouldNotOverlapOtherObjects(){
        //checkpoints and currency
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
            RunData runData = generateLevel(TEST_HEIGHT);
            //CHECK
            for(Faucet f : runData.getFaucets()){
                Rectangle fRect = new Rectangle(f.getX(), f.getY(), f.getWidth(), f.getHeight());

                for(Checkpoint c : runData.getCheckpoints()){
                    Rectangle cRect = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());
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
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
            RunData runData = generateLevel(TEST_HEIGHT);
            //CHECK
            for(Checkpoint c : runData.getCheckpoints()){
                Rectangle checkRect = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());

                for(Currency curr : runData.getCurrencies()){
                    Rectangle currRect = new Rectangle(curr.getX(), curr.getY(), Currency.WIDTH, Currency.HEIGHT);
                    assertFalse(checkRect.overlaps(currRect), "Currency overlaps checkpoint");
                }
            }
        }
    }

    // TODO - currency reachable!
    @Test
    public void isCurrencyReachable(){
        for(int i = 0; i < NUM_GENERATED_LEVELS; i++){
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

    // TODO - distance check between checkpoints and faucets
    //(max height of checkpoint, and average distance between checkpoints and faucets)
    //more info check then test!

    //TODO - check platform reachability
    @Test
    public void checkReachability(){
        // limited amount of failures - assume wall jumping can fix?

        final int maxLeap = 550; // Horizontal
        final int maxJump = 150; // Vertical
        final float fallMultiplierForLeap = 1.5f; // If falling, allow for further leap //TODO - check!
        final int totalTestHeight = 100_000;
        final int reachableBuffer = 1000;
        final float percentLeapAtMaxJump = 0.9f;

        // For fails
        int highestY = 0;
        long seed;

        for(int i = 0; i < NUM_GENERATED_LEVELS / 5; i++) {
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

                float leftX = curr.getX() - (maxLeap * fallMultiplierForLeap);
                float rightX = curr.getX() + curr.getWidth() + (maxLeap * fallMultiplierForLeap);
                float downY = curr.getY() - (2 * maxJump);
                float upY = curr.getY() + curr.getHeight() + maxJump;

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
                    if(p.getX() + p.getWidth() < curr.getX()){
                        dx = curr.getX() - (p.getX() + p.getWidth());
                    } else if(p.getX() > curr.getX() + curr.getWidth()){
                        dx = p.getX() - (curr.getX() + curr.getWidth());
                    } else{
                        //platform edges are within current platform
                        dx = 0;
                    }

                    float dy;
                    if(p.getY() > curr.getY() + curr.getHeight()){
                        dy = p.getY() - (curr.getY() + curr.getHeight());
                    } else if (p.getY() + p.getHeight() < curr.getY()){
                        dy = -1 * (curr.getY() - (p.getY() + p.getHeight())); // Make negative to indicate drop
                    } else{
                        dy = 0;
                    }

                    if(dy < 0){
                        // check downward jump
                        if((-1 * dy) > 0.5f * maxJump){
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
                        if(dy <= maxJump){
                            float forgiveness = 1.0f - percentLeapAtMaxJump;
                            float allowedDx = maxLeap * (1.0f - (forgiveness * (dy / maxJump)));

                            if(dx < allowedDx){
                                reachable.offer(p);
                                visited.add(p);
                            }
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

    @Test
    public void checkPrestigeCurrency(){
        int numRuns = NUM_GENERATED_LEVELS * 25; //This is quick and easy test but need more samples!
        float goalAveragePrestigePerLevel = 0.05f; //1 in 20 runs has a prestige

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


    private RunData generateLevel(int height){
        long seed = System.currentTimeMillis();
        PlayerData playerData = new PlayerData();
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



    // TODO - use?
    public class JumpMetrics {

        public static double computeMaxLeap(double vx, double jumpVelocity, double gravity) {
            double y = 0;
            double x = 0;
            double vy = jumpVelocity; // upward impulse

            // Simulate frame-by-frame until the player lands back at y <= 0
            while (true) {
                x += vx;
                y += vy;
                vy -= gravity;

                if (y <= 0 && vy < 0) {
                    break; // Hit the ground
                }
            }
            return x;
        }

        public static double computeMaxJump(double jumpVelocity, double gravity) {
            double y = 0;
            double vy = jumpVelocity;

            // Simulate until the apex (vy <= 0)
            while (vy > 0) {
                y += vy;
                vy -= gravity;
            }
            return y;
        }
    }
}

