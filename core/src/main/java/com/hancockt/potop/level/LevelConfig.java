package com.hancockt.potop.level;

public record LevelConfig(

    // variable // (default value, beginner mode value)
    float minCheckpointHeightDistance, //(800,500)
    float maxCheckpointHeightDistance, //(1600, 1000)
    float skipCheckpointThreshold, //(0.08, 0.05)

    //means to divide all 3 above and below - checkpoint/faucet
    // by this and then subtract their values by that
    // thus increasing # of checkpoints and faucets!
    float increasedDivisorUpHigh, //(4f,5)
    float increasedChanceHeight, //(8_000f,5000)

    float minFaucetHeightDistance, //(750f,500)
    float maxFaucetHeightDistance, //(1500f,1000)
    float skipFaucetThreshold, //(0.25f,0.15f)
    float minFaucetSpeedReduction, //(10,20)
    float maxFaucetSpeedReduction, //(50,60)

    float minNumPlatformsPerBand, //(5,8)
    int maxAttemptsPlacingPlatforms, //(500,600)
    float baseCoverageRatio, //(0.3f,0.4f)
    float minCoverageRatio, //(0.025f,0.05)
    float maxDifficultyHeight, //(12_000,8_000)
    float basePlatformMaxWidth, //(300,300)
    float basePlatformMaxHeight, //(150,150)
    float minMaxPlatformWidth, //(50,100)
    float minMaxPlatformHeight, //(30,60)
    float minPlatformWidth, //(20,40)
    float minPlatformHeight, //(10,20)
    float centerBiasForPlatforms, //(0.15f, 0.10) // bias towards platforms in center instead of by walls

    int maxPrestigeCurrencyThreshold, //(10_000,0)
    int minPrestigeCurrencyThreshold, //(1_000,0)
    int chancePrestigeDecreaseThreshold, //(5_000,0)
    float prestigeCurrChanceMax, //(0.0002f,0)
    float prestigeCurrChanceMin, //(0.000001f,0)
    float minCurrencyChanceOfPlacement, //(0.05f, 0.005)// only on 5% of platforms to start - TUNABLE
    float maxCurrencyChanceOfPlacement, //(0.5f, 0.05)
    int maxPrestigePlaced, //(5, 0)

    int reachableHorizontalMax, //(400, 300)
    int reachableVerticalMax //(90,60)
) {

    public static LevelConfig defaultInfinite(){
        return new LevelConfig(
            800f,
            1600f,
            0.08f,
            4f,
            8_000f,
            750f,
            1500f,
            0.25f,
            10,
            50,
            5,
            500,
            0.3f,
            0.025f,
            12_000,
            300,
            150,
            50,
            30,
            20,
            10,
            0.15f,
            10_000,
            1_000,
            5_000,
            0.0002f,
            0.000001f,
            0.05f,
            0.5f,
            5,
            400,
            90
        );
    }

    // TODO - add these to comments above!
    public static LevelConfig beginnerMode(){
        return new LevelConfig(
            500f,
            1_000f,
            0.05f,
            5f,
            5_000f,
            500f,
            1_000f,
            0.15f,
            20,
            60,
            8,
            600,
            0.4f,
            0.05f,
            8_000,
            300,
            150,
            100,
            60,
            40,
            20,
            0.10f,
            0,
            0,
            0,
            0,
            0,
            0.005f,
            0.05f,
            0,
            300,
            60
        );
    }
}
