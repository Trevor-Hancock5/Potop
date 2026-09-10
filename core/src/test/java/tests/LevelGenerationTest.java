package tests;

import com.badlogic.gdx.math.Rectangle;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.level.LevelGeneration;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class LevelGenerationTest {
    private static final int NUM_LEVELS = 100;
    private static final int TEST_HEIGHT = 20_000;
    //seed todo

    @Test
    public void platformsShouldNotOverlapPlatforms(){
        for(int i = 0; i < NUM_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
            List<Platform> platforms = runData.getPlatforms();
            for(int j = 0; j < platforms.size(); j++){
                for(int k = j+1; k < platforms.size(); k++){
                    Platform p1 = platforms.get(j);
                    Platform p2 = platforms.get(k);
                    Rectangle r1 = new Rectangle(p1.getX(), p1.getY(), p1.getWidth(), p1.getHeight());
                    Rectangle r2 = new Rectangle(p2.getX(), p2.getY(), p2.getWidth(), p2.getHeight());
                    assertFalse(r1.overlaps(r2), "i,j,k: " + i + "," + j + "," + k +
                        ". seed: " + runData.seed +
                        ". p1 (x,y): (" + p1.getX() + "," + p1.getY() + "). " +
                        "p2 (x,y): (" + p2.getX() + "," + p2.getY() + "). ");
                }
            }
        }
    }

    @Test
    public void faucetsShouldNotOverlapFaucets(){
        for(int i = 0; i < NUM_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
        }
    }

    @Test
    public void checkpointsShouldNotOverlapCheckpoints(){
        for(int i = 0; i < NUM_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
        }
    }

    @Test
    public void currencyShouldNotOverlapCurrency(){
        for(int i = 0; i < NUM_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
        }
    }

    @Test
    public void platformsShouldNotOverlapOtherObjects(){
        //faucet, checkpoint, currency
        for(int i = 0; i < NUM_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
        }
    }

    @Test
    public void faucetsShouldNotOverlapOtherObjects(){
        //checkpoints and currency
        for(int i = 0; i < NUM_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
        }
    }

    @Test
    public void checkpointsShouldNotOverlapCurrency(){
        for(int i = 0; i < NUM_LEVELS; i ++){
            RunData runData = generateLevel();
            //CHECK
        }
    }



    private RunData generateLevel(){
        long seed = System.currentTimeMillis();
        PlayerData playerData = new PlayerData();
        LevelGeneration levelGeneration = new LevelGeneration(seed, playerData);
        RunData runData = LevelGeneration.getAllLevels().get(LevelType.INFINITE); // at 2000

        for(int i = 2000; i < TEST_HEIGHT; i += 2000) {
            levelGeneration.addToInfiniteLevel(runData.getPlatforms(), runData.getCheckpoints(), runData.getFaucets(), runData.getCurrencies(), runData.rng, playerData);
        }
        return runData;
    }
}

