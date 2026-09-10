package tests;

import com.badlogic.gdx.math.Rectangle;
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
                    assertFalse(r1.overlaps(r2), new StringBuilder().append("i,j,k: ").append(i).append(",").append(j).append(",").append(k).append(". seed: ").append(runData.seed).append(". p1 (x,y): (").append(p1.getX()).append(",").append(p1.getY()).append("). ").append("p2 (x,y): (").append(p2.getX()).append(",").append(p2.getY()).append("). \n").append(printStats(maxList, avgList)).toString());
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
        }
    }

    @Test
    public void checkpointsShouldNotOverlapCheckpoints(){
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
        }
    }

    @Test
    public void currencyShouldNotOverlapCurrency(){
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
        }
    }

    @Test
    public void platformsShouldNotOverlapOtherObjects(){
        //faucet, checkpoint, currency
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
        }
    }

    @Test
    public void faucetsShouldNotOverlapOtherObjects(){
        //checkpoints and currency
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
        }
    }

    @Test
    public void checkpointsShouldNotOverlapCurrency(){
        for(int i = 0; i < NUM_GENERATED_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
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

