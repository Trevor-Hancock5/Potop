package com.hancockt.potop.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import com.hancockt.potop.assets.AnimationKey;
import com.hancockt.potop.assets.GameAssets;
import com.hancockt.potop.features.Checkpoint;
import com.hancockt.potop.features.Currency;
import com.hancockt.potop.features.Faucet;
import com.hancockt.potop.features.Platform;
import com.hancockt.potop.level.LevelType;
import com.hancockt.potop.state.PersistentData;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

import java.util.ArrayList;
import java.util.List;

public class RenderingSystem {
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private static final Color water = new Color(17/255f, 132/255f, 214/255f, .65f); // TUNABLE
    private static final float potopLogoYOffset = 100f; // TUNABLE
    private static final float logoW = 500; // TUNABLE
    private static final float logoH = 250; // TUNABLE
    private static final float faucetGray = 0.5f; // TUNABLE
    private static final String DEATH_TEXT = "lol"; // TUNABLE
    private static final float DEATH_TEXT_BOB_PERIOD = 300f; // TUNABLE
    private static final float DEATH_TEXT_BOB_AMOUNT = 2.5f; // TUNABLE
    private static final float DEATH_TEXT_WATER_OFFSET = 10f; // TUNABLE

    public RenderingSystem(){
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        layout = new GlyphLayout();
    }

    public void drawMenu(boolean learningModeBeat, OrthographicCamera hudCam, GameAssets gameAssets){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        // logo centered
        batch.draw(gameAssets.logo,
            (CameraSystem.VIEWPORT_WIDTH - logoW) / 2f,
            (CameraSystem.VIEWPORT_HEIGHT- logoH) / 2f + potopLogoYOffset,
            logoW, logoH);

        // existing text below
        if(!learningModeBeat) {
            drawCenteredText("Press SPACE to start learning mode", -35, 1.5f);
        }
        drawCenteredText("\nType I for Infinite Mode", -55, 1.5f);

        batch.end();
    }

    public void drawPauseOverlay(OrthographicCamera hudCam){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        drawCenteredText("PAUSED", 35, 2);
        drawCenteredText("Press P to resume", 0, 1);
        drawCenteredText("Press M to go to menu", -35, 1);
        drawCenteredText("Reminder: Press R to restart at previous checkpoint", -70, 1);

        batch.end();
    }

    public void drawDeathOverlay(OrthographicCamera hudCam, PlayerData playerData, PersistentData persistentData){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        if(persistentData.highScore > persistentData.previousHighScore){
            drawCenteredText("NEW HIGH SCORE: " + Math.round(persistentData.highScore), 75, 2f);
        }

        drawCenteredText("You drowned. Score: " + Math.round(playerData.y), 30, 1.5f);
        drawCenteredText("Press R to retry!", 0, 1);

        batch.end();
    }

    public void drawWinOverlay(OrthographicCamera hudCam){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        drawCenteredText("YOU MADE IT!!", 65, 3);
        drawCenteredText("Press ENTER to continue", 0, 1);

        batch.end();
    }

    public void drawShapes(RunData runData, PlayerData playerData, GameAssets gameAssets, OrthographicCamera camera, WaterSystem waterSystem,
                           FaucetSystem faucetSystem){
        //Draw sprites first
        drawSprites(runData, playerData, gameAssets, camera, faucetSystem);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        //water should be drawn last!
        shapeRenderer.setColor(water);

        float camLeft = camera.position.x - CameraSystem.VIEWPORT_WIDTH / 2;
        shapeRenderer.rect(camLeft, 0, CameraSystem.VIEWPORT_WIDTH, waterSystem.getWaterLevel());

        shapeRenderer.end();
    }

    public void drawSprites(RunData runData, PlayerData playerData, GameAssets gameAssets, OrthographicCamera camera, FaucetSystem faucetSystem){
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        //platforms
        for(Platform p : runData.getActivePlatforms()){
            Texture asset = p.isBoundaryWall() ? gameAssets.boundaryWall : gameAssets.platform;
            batch.draw(asset, p.getX(), p.getY(), p.getWidth(), p.getHeight());
        }

        //checkpoints
        for(Checkpoint c : runData.getActiveCheckpoints()){
            if(c.isActivated()){
                gameAssets.checkpointAnimator.play(AnimationKey.CHECKPOINT_ACTIVE);
                TextureRegion asset = gameAssets.checkpointAnimator.getCurrentFrame();
                batch.draw(asset, c.getX(), c.getY(), c.getWidth(), c.getHeight());
            } else {
                Texture asset = gameAssets.checkpoint_inactive;
                batch.draw(asset, c.getX(), c.getY(), c.getWidth(), c.getHeight());
            }
        }

        // faucets
        for (Faucet f : runData.getActiveFaucets()) {
            Texture texture = f.isWinFaucet() ? gameAssets.winFaucet : gameAssets.faucet;

            float width = f.getWidth();
            float height = f.getHeight();
            float centerX = width / 2f;  // rotate around center
            float centerY = height / 2f;

            //Tint based on state for regular faucets
            if(!f.isWinFaucet() && f.getProgress() > 0){
                float ratio = f.getProgress() / faucetSystem.getFaucetInteractionTime();
                float r = 1 - ratio * (1 - faucetGray);
                float g = 1 - ratio * (1 - faucetGray);
                float b = 1 - ratio * (1 - faucetGray);

                batch.setColor(r,g,b,1f);
            }

            batch.draw(
                texture,
                f.getX(), f.getY(),          // position
                centerX, centerY,             // origin point for rotation
                width, height,                // size
                1f, 1f,                       // scaleX, scaleY
                -f.getRotation(),             // rotation in degrees
                0, 0,                         // source x, y in texture
                texture.getWidth(), texture.getHeight(), // source width, height
                false, false                  // flipX, flipY
            );

            batch.setColor(Color.WHITE); // reset tint so other sprites aren't affected
        }

        //Currency
        for(Currency c : runData.getActiveCurrency()){
            Texture asset = c.isPrestige() ? gameAssets.prestigeCurrency : gameAssets.currency;
            batch.draw(asset, c.getX(), c.getY(), Currency.WIDTH, Currency.HEIGHT);
        }

        //SPARK
        batch.draw(gameAssets.sparkAnimator.getCurrentFrame(), playerData.x, playerData.y, PlayerData.playerWidth, PlayerData.playerHeight);

        batch.end();
    }

    public void drawDeathText(OrthographicCamera camera, List<Float> deathXValues, WaterSystem waterSystem) {
        float bob = (float) Math.sin(TimeUtils.millis() / DEATH_TEXT_BOB_PERIOD) * DEATH_TEXT_BOB_AMOUNT;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for(Float f : deathXValues) {
            font.draw(batch, DEATH_TEXT, f, waterSystem.getWaterLevel() + DEATH_TEXT_WATER_OFFSET + bob);
        }
        batch.end();
    }

    public void drawCenteredText(String text, float yOffset, float scale) {
        font.getData().setScale(scale);

        layout.setText(font, text);
        float x = (CameraSystem.VIEWPORT_WIDTH - layout.width) / 2;
        float y = (CameraSystem.VIEWPORT_HEIGHT - layout.height) / 2 + yOffset;
        font.draw(batch, text, x, y);

        font.getData().setScale(1f); //reset
    }

    public void drawHUD(RunData runData, PersistentData persistentData, PlayerData playerData, OrthographicCamera hudCam){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();
        try {
            if (Gdx.input.isKeyPressed(Input.Keys.L)) {
                font.getData().setScale(2);
                font.draw(batch, "Seed: " + runData.seed, CameraSystem.VIEWPORT_WIDTH / 2, CameraSystem.VIEWPORT_HEIGHT / 2);
            }

            font.getData().setScale(1.25f);
            font.draw(batch, "Score: " + Math.round(playerData.y), CameraSystem.VIEWPORT_WIDTH - 175, CameraSystem.VIEWPORT_HEIGHT - 50);
            font.draw(batch, "High Score: " + Math.round(persistentData.highScore), CameraSystem.VIEWPORT_WIDTH - 175, CameraSystem.VIEWPORT_HEIGHT - 30);
            font.draw(batch, "Matches: " + runData.runMatches, CameraSystem.VIEWPORT_WIDTH - 175, CameraSystem.VIEWPORT_HEIGHT - 10);

            font.setColor(Color.WHITE);

        } finally {
            font.getData().setScale(1f); //reset

            batch.end();
        }
    }

    //TODO
    public void instructions(RunData runData, OrthographicCamera camera){
        //Instructions
        List<String> text = new ArrayList<>();
        List<Float> xValues = new ArrayList<>();
        List<Float> yValues = new ArrayList<>();
        List<Float> scaleValues = new ArrayList<>();

        if (runData.getCurrentLevel() == LevelType.LEARNING_MODE) {
            text.add("LEARNING MODE");
            xValues.add(45f);
            yValues.add(175f);
            scaleValues.add(1f);

            text.add("Get to 10,000 height to beat LEARNING MODE!");
            xValues.add(5f);
            yValues.add(160f);
            scaleValues.add(0.75f);

            text.add("Press R to restart on death\nP to pause");
            xValues.add(5f);
            yValues.add(140f);
            scaleValues.add(0.75f);

            drawText(text, xValues, yValues, scaleValues, camera);
        }

        if (runData.getCurrentLevel() == LevelType.INFINITE) {
            text.add("INFINITE MODE");
            xValues.add(45f);
            yValues.add(175f);
            scaleValues.add(1f);

            text.add("Warning: Water rises faster and faster");
            xValues.add(5f);
            yValues.add(160f);
            scaleValues.add(0.75f);

            text.add("Click Q to regenerate");
            xValues.add(5f);
            yValues.add(140f);
            scaleValues.add(0.75f);

            drawText(text, xValues, yValues, scaleValues, camera);
        }
    }

    public void drawText(List<String> text, List<Float> xPos, List<Float> yPos, List<Float> scale, OrthographicCamera camera){
        batch.begin();
        batch.setProjectionMatrix(camera.combined);

        for(int i = 0; i < text.size(); i++) {
            font.getData().setScale(scale.get(i));

            font.draw(batch, text.get(i), xPos.get(i), yPos.get(i));
        }

        batch.end();
        font.getData().setScale(1f); //reset
    }

    public void clearScreen(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void dispose(){
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
