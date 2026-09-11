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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class LevelGenerationTest {
    private static final int NUM_GENERATED_LEVELS = 100;
    private static final int TEST_HEIGHT = 14_000;

    @Test
    public void platformsShouldNotOverlapPlatforms(){
        ArrayList<Integer> maxList = new ArrayList<>();
        ArrayList<Double> avgList = new ArrayList<>();
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
            RunData runData = generateLevel();
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
            RunData runData = generateLevel();
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
            RunData runData = generateLevel();
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
            RunData runData = generateLevel();
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
            RunData runData = generateLevel();
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
            RunData runData = generateLevel();
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
            RunData runData = generateLevel();
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



    private RunData generateLevel(){
        long seed = System.currentTimeMillis();
        PlayerData playerData = new PlayerData();
        LevelGeneration levelGeneration = new LevelGeneration(seed, playerData);
        RunData runData = LevelGeneration.getAllLevels().get(LevelType.INFINITE); // at 10_000

        for(int i = 10_000; i < TEST_HEIGHT; i += 2000) {
            levelGeneration.addToInfiniteLevel(runData.getPlatforms(), runData.getCheckpoints(), runData.getFaucets(), runData.getCurrencies(), runData.rng, playerData);
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
            .mapToInt(Double::intValue)
            .max()
            .orElse(0); // fallback value if the list is empty

// Average (returns an OptionalDouble)
        double averageAverage = avgList.stream()
            .mapToInt(Double::intValue)
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

