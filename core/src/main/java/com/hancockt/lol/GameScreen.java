package com.hancockt.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

import static com.hancockt.lol.PlayerData.*;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    //Rendering
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private OrthographicCamera hudCam;
    private final float viewportWidth = 800f;
    private final float viewportHeight = 480f;
    public static float screenShakeAccumulation;
    public static LevelType currentLevel;
    public static GameState gameState = GameState.MENU; //start with menu
    private final GlyphLayout layout = new GlyphLayout(); // create once, reuse

    //PLAYER TUNING!
    public static final int maxFallSpeed = -1200;
    public static final float cameraLerpSpeed = .3f;
    public static final float cameraDeadZoneDivisor = 5f;
    public static final float screenShake = 0.3f; //time of shaking
    public static final float screenShakeIntensity = 2f;
    public static final float faucetInteractionTime = 1.5f; //seconds

    //water
    public static float waterLevel;
    public static float waterRiseSpeed;
    public static Rectangle waterTriggerZone;
    public static Rectangle waterFixZone;
    private Rectangle winZone;

    public static List<Platform> platforms;
    public static List<Float> deathXValues;
    public static List<Checkpoint> checkpoints;
    public static List<Faucet> faucets;
    private boolean pressedI = false;
    private boolean pressedN = false;
    public static float time = 0f;
    public static float timeAtDeath = 0f;

    //game reference
    private final lol game;
    private GameAssets gameAssets = new GameAssets();

    // Controls
    public static boolean waterRise = false;
    private static boolean enteringSeed = false;


    public GameScreen(lol game, LevelType type){
        this.game = game;
        currentLevel = type;
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(true);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        gameAssets.load();

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(viewportWidth, viewportHeight, camera);
        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, viewportWidth, viewportHeight);
        deathXValues = new ArrayList<>();

        loadLevel(currentLevel);
        restart();
    }

    //REMEMBER: Everything in here is made 60 times a second!

    @Override
    public void render(float delta) {
        clearScreen();
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        //update animations!
        gameAssets.checkpointAnimator.update(delta);
        gameAssets.sparkAnimator.update(delta);

        switch(gameState) {
            case MENU:
                handleMenuInput();
                drawMenu();
                break; //skip everything else
            case PAUSE:
                handlePauseInput();
                drawShapes(); //still draw game in background
                drawHUD();
                drawPauseOverlay();
                break;
            case DEATH:
                gameAssets.sparkAnimator.play(AnimationKey.DEATH);
                drawShapes();
                drawHUD();
                drawDeathText();
                drawDeathOverlay();
                handleDeathInput();
                break;
            case WIN:
                handleWinInput();
                drawShapes();
                drawHUD();
                drawWinOverlay();
                break;
            case PLAYING:
                if(!isDead){
                    AnimationKey sparkAnimationState = PhysicsEngine.updatePhysics(delta);
                    gameAssets.sparkAnimator.play(sparkAnimationState);
                }

                if(isDead || !deathXValues.isEmpty()){
                    drawDeathText();
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
                    restart();
                }

                if(Gdx.input.isKeyPressed(Input.Keys.Q)){
                    onQ();
                }

                if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
                    gameState = GameState.PAUSE;
                }

                //infinite mode seed input
                if(currentLevel == LevelType.INFINITE){
                    if(Gdx.input.isKeyJustPressed(Input.Keys.T)){
                        enteringSeed = true;
                    }
                }
                drawShapes();
                updateCamera(delta);
                drawHUD();

                instructions();
                break;
        }

    }

    private void drawShapes(){
        //Draw sprites first
        drawSprites();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        //water should be drawn last!
        shapeRenderer.setColor(new Color(17/255f, 132/255f, 214/255f, .65f));

        float camLeft = camera.position.x - camera.viewportWidth / 2;
        shapeRenderer.rect(camLeft, 0, camera.viewportWidth, waterLevel);

        shapeRenderer.end();
    }

    private void drawSprites(){
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        //platforms
        for(Platform p : platforms){
            Texture asset = p.isBoundaryWall() ? gameAssets.boundaryWall : gameAssets.platform;
            batch.draw(asset, p.getX(), p.getY(), p.getWidth(), p.getHeight());
        }

        //checkpoints
        for(Checkpoint c : checkpoints){
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
        for (Faucet f : faucets) {
            Texture texture = f.isWinFaucet() ? gameAssets.winFaucet : gameAssets.faucet;

            float width = f.getWidth();
            float height = f.getHeight();
            float originX = width / 2f;  // rotate around center
            float originY = height / 2f;

//            // tint based on state, same logic as your old color code
//            if (f.isTurnedOff()) {
//                batch.setColor(f.isWinFaucet() ? new Color(1f, 0.8431f, 0f, 1f) : Color.GRAY);
//            } else if (f.getProgress() > faucetInteractionTime / 2) {
//                batch.setColor(f.isWinFaucet() ? new Color(1f, 0.9255f, 0.4541f, 1f) : new Color(0.85f, 0.7f, 0.7f, 1f));
//            } else {
//                batch.setColor(f.isWinFaucet() ? new Color(0.8314f, 0.6863f, 0.2157f, 1f) : new Color(1f, 0.6f, 0.6f, 1f));
//            }

            //Tint based on state for regular faucets
            if(!f.isWinFaucet() && f.getProgress() > 0){
                float ratio = f.getProgress() / faucetInteractionTime;
                float gray = 0.5f;

                float r = 1 - ratio * (1 - gray);
                float g = 1 - ratio * (1 - gray);
                float b = 1 - ratio * (1 - gray);

                batch.setColor(r,g,b,1f);
            }

            batch.draw(
                texture,
                f.getX(), f.getY(),          // position
                originX, originY,             // origin point for rotation
                width, height,                // size
                1f, 1f,                       // scaleX, scaleY
                -f.getRotation(),             // rotation in degrees
                0, 0,                         // source x, y in texture
                texture.getWidth(), texture.getHeight(), // source width, height
                false, false                  // flipX, flipY
            );

            batch.setColor(Color.WHITE); // reset tint so other sprites aren't affected
        }

        //SPARK
        batch.draw(gameAssets.sparkAnimator.getCurrentFrame(), x, y, playerWidth, playerHeight);

        batch.end();
    }

    private void drawDeathText() {
        float bob = (float) Math.sin(TimeUtils.millis() / 300.0) * 2.5f;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for(Float f : deathXValues) {
            font.draw(batch, "lol", f, waterLevel + 10 + bob);
        }
        batch.end();
    }

    private void drawHUD(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        if(enteringSeed){
            //TODO? Maybe change this to be in the paused state or change state to paused???
        } else {

            if (Gdx.input.isKeyPressed(Input.Keys.L)) {
                font.getData().setScale(2);
                font.draw(batch, "Seed: " + LevelLibrary.getRng(), viewportWidth / 2, viewportHeight / 2);
            }

            font.getData().setScale(1.25f);
            if (currentLevel == LevelType.INFINITE) {
                font.draw(batch, "Score: " + Math.round(y), viewportWidth - 175, viewportHeight - 30);
                font.draw(batch, "High Score: " + Math.round(score), viewportWidth - 175, viewportHeight - 10);
            } else {
                font.draw(batch, "Time: " + String.format("%.1f", time), viewportWidth - 125, viewportHeight - 10);
            }
        }

        font.getData().setScale(1f); //reset

        batch.end();
    }

    private void instructions(){
        //Instructions
        List<String> text = new ArrayList<>();
        List<Float> xValues = new ArrayList<>();
        List<Float> yValues = new ArrayList<>();
        List<Float> scaleValues = new ArrayList<>();

        if (currentLevel == LevelType.TUTORIAL) {
            text.add("Watch out for the water\nOr you DROWN");
            xValues.add(-125f);
            yValues.add(50f);
            scaleValues.add(0.75f);

            text.add("WASD / Arrow Keys to move\nW / UP /SPACE to jump\nP to PAUSE\nQ to COMPLETELY " +
                "RESTART\nESC " +
                "to EXIT game");
            xValues.add(50f);
            yValues.add(145f);
            scaleValues.add(0.75f);

            text.add("Hold UP / W / SPACE\njump higher");
            xValues.add(800f);
            yValues.add(225f);
            scaleValues.add(0.75f);

            text.add("CHECKPOINT\n\nHold DOWN / S\nslow down or fall faster");
            xValues.add(1900f);
            yValues.add(425f);
            scaleValues.add(0.75f);

            text.add("JUUUUMMMMPPPPPP");
            xValues.add(4925f);
            yValues.add(1875f);
            scaleValues.add(1f);

            text.add("You can WALL JUMP in 3 ways:\n- Just JUMP to go diagonal\n- Hold INTO the wall " +
                "and jump to climb\n- Hold AWAY from the wall and jump to fling sideways");
            xValues.add(5925f);
            yValues.add(1275f);
            scaleValues.add(0.75f);

            text.add("As the water gets closer to you\nyou jump higher and move faster!");
            xValues.add(6225f);
            yValues.add(1900f);
            scaleValues.add(0.75f);

            text.add("Hold E or SHIFT to turn the faucet\n to slow down the water!");
            xValues.add(6740f);
            yValues.add(2090f);
            scaleValues.add(0.75f);

            text.add("Finish the faucet to WIN!");
            xValues.add(8085f);
            yValues.add(2245f);
            scaleValues.add(0.75f);

            drawText(text, xValues, yValues, scaleValues);
        }

        if (currentLevel == LevelType.INFINITE) {
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

            drawText(text, xValues, yValues, scaleValues);
        }
    }

    private void drawText(List<String> text, List<Float> xPos, List<Float> yPos, List<Float> scale){
        batch.begin();
        batch.setProjectionMatrix(camera.combined);

        for(int i = 0; i < text.size(); i++) {
            font.getData().setScale(scale.get(i));

            font.draw(batch, text.get(i), xPos.get(i), yPos.get(i));
        }

        batch.end();
        font.getData().setScale(1f); //reset
    }

    private void clearScreen(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void updateCamera(float delta) {
        float deadzone = camera.viewportWidth / cameraDeadZoneDivisor;
        float deadzoneRight = camera.position.x + (deadzone / 2);
        float deadzoneLeft = camera.position.x - (deadzone / 2);

        float targetX = camera.position.x; //default is to stay put

        if(x < deadzoneLeft){
            targetX = x + (deadzone / 2);
        } else if (x > deadzoneRight){
            targetX = x - (deadzone / 2);
        }

        float vertLookAhead = velocityY * 0.1f;
        float targetY = isDead ? Math.max(waterLevel, 240) : Math.max(y+25+vertLookAhead, 240);

        camera.position.x += (targetX - camera.position.x) * cameraLerpSpeed;
        camera.position.y += (targetY - camera.position.y) * (cameraLerpSpeed / 3);

        if(justLanded && screenShakeAccumulation < screenShake){
            if(impactVelocity / maxFallSpeed > 0.5){
                screenShakeAccumulation += delta / 2;
            } else {
                screenShakeAccumulation += delta;
            }
            camera.position.x += (float) ((Math.random() - 0.5) * (screenShakeIntensity * (2 + impactVelocity / maxFallSpeed)));
            camera.position.y += (float) ((Math.random() - 0.5) * (screenShakeIntensity * (2 + impactVelocity / maxFallSpeed)));
        }
        camera.update();
    }

    private void drawCenteredText(String text, float yOffset, float scale) {
        font.getData().setScale(scale);

        //batch.draw(); - TODO: surround text with black box, so white on black, or some other contrast
        layout.setText(font, text);
        float x = (viewportWidth - layout.width) / 2;
        float y = (viewportHeight - layout.height) / 2 + yOffset;
        font.draw(batch, text, x, y);

        font.getData().setScale(1f); //reset
    }

    private void restart() {
        waterLevel = PlayerData.reset(checkpoints, currentLevel);

        time = timeAtDeath;
        waterRiseSpeed = Math.min(50f, 8f + (y / 200f)); //full speed always after ~8k height
        screenShakeAccumulation = 0;
        for (Faucet f : faucets) {
            f.setTurnedOff(false); //reset
            f.setRotation(0);
            f.setProgress(0);
        }
    }

    private void loadLevel(LevelType levelType) {
        currentLevel = levelType;

        LevelData data = LevelLibrary.getAllLevels().get(levelType);
        platforms = data.getPlatforms();
        checkpoints = data.getCheckpoints();
        waterTriggerZone = data.getWaterTriggerZone();
        waterFixZone = data.getWaterFixZone();
        winZone = data.getWinZone();
        waterRise = data.isWaterStartsActive();
        faucets = data.getFaucets();
        x = data.getStartPoint().x;
        y = data.getStartPoint().y;
    }

    public static void updateLists(LevelData data) {
        platforms.addAll(data.getPlatforms());
        checkpoints.addAll(data.getCheckpoints());
        faucets.addAll(data.getFaucets());

        //remove anything under death water level that won't appear above the water
        List<Platform> platformsUnderWater =
            platforms.stream().filter(platform -> platform.getY() < deathWaterLevel - 150).toList();

        List<Checkpoint> checkpointsUnderWater =
            checkpoints.stream().filter(checkpoint -> checkpoint.getY() < deathWaterLevel).toList();

        List<Faucet> faucetsUnderWater =
            faucets.stream().filter(faucet -> faucet.getY() < deathWaterLevel).toList();

        if (!platformsUnderWater.isEmpty()) {
            for (Platform p : platformsUnderWater) {
                platforms.remove(p);
            }
        }

        if (!checkpointsUnderWater.isEmpty()) {
            for (Checkpoint c : checkpointsUnderWater) {
                checkpoints.remove(c);
            }
        }

        if (!faucetsUnderWater.isEmpty()) {
            for (Faucet f : faucetsUnderWater) {
                faucets.remove(f);
            }
        }
    }

    private void drawMenu(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        // logo centered
        float logoW = 500;
        float logoH = 250;
        batch.draw(gameAssets.logo,
            (viewportWidth - logoW) / 2f,
            (viewportHeight - logoH) / 2f + 100f,
            logoW, logoH);

        // existing text below
        drawCenteredText("Press SPACE to start tutorial", -35, 1.5f);
        drawCenteredText("\nType INF for Infinite Mode", -55, 1.5f);

        batch.end();

//        batch.setProjectionMatrix(hudCam.combined);
//        batch.begin();
//
//        drawCenteredText("The MC", 70, 2);
//        drawCenteredText("Press SPACE to start tutorial", 10, 1);
//        drawCenteredText("Press I to start infinite (Must complete tutorial first)", -10, 1);
//
//        batch.end();
    }

    private void handleMenuInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            loadLevel(LevelType.TUTORIAL);
            restart();
            gameState = GameState.PLAYING;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.I)){
            if(infiniteUnlocked) {
                loadLevel(LevelType.INFINITE);
                restart();
                gameState = GameState.PLAYING;
            }
            pressedI = true;
        }
        if(pressedI && Gdx.input.isKeyJustPressed(Input.Keys.N)){
            pressedN = true;
        }
        if(pressedI && pressedN && Gdx.input.isKeyJustPressed(Input.Keys.F)){
            //Skip straight to infinite mode
            loadLevel(LevelType.INFINITE);
            restart();
            gameState = GameState.PLAYING;
        }
    }

    private void drawPauseOverlay(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        drawCenteredText("PAUSED", 35, 2);
        drawCenteredText("Press P to resume", 0, 1);
        drawCenteredText("Press M to go to menu", -35, 1);
        drawCenteredText("Reminder: Press R to restart at previous checkpoint", -70, 1);

        batch.end();
    }

    private void handlePauseInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
            gameState = GameState.PLAYING;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.M)){
            onQ();
            gameState = GameState.MENU;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            gameState = GameState.PLAYING;
            restart();
        }
    }

    private void drawDeathOverlay(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        drawCenteredText("You drowned. Score: " + Math.round(score), 30, 1.5f);
        drawCenteredText("Press R to retry!", 0, 1);

        batch.end();
    }

    private void handleDeathInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            restart();
            gameState = GameState.PLAYING;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            onQ();
            gameState = GameState.PLAYING;
        }
    }

    private void drawWinOverlay(){
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        drawCenteredText("YOU MADE IT!!", 65, 3);
        drawCenteredText("Press ENTER to continue", 0, 1);

        batch.end();
    }

    private void handleWinInput(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            time = 0f;
            timeAtDeath = 0f;
            LevelData data = LevelLibrary.getAllLevels().get(currentLevel);
            LevelType next = data.getNextLevel();
            if(next != null){
                if(currentLevel == LevelType.TUTORIAL){
                    infiniteUnlocked = true;
                }
                deathWaterLevel = -50f;
                currCheckpoint = null;
                checkpoints = null;
                LevelData nextLevel = LevelLibrary.getAllLevels().get(next);
                x = nextLevel.getStartPoint().x;
                y = nextLevel.getStartPoint().y;
                currentLevel = next;
                loadLevel(next);
                restart();
                gameState = GameState.PLAYING;
            } else{
                gameState = GameState.MENU;
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            onQ();
            gameState = GameState.PLAYING;
        }
    }

    private void onQ(){
        if (currentLevel == LevelType.INFINITE) {
            deathWaterLevel = -50f;
            deathXValues = new ArrayList<>(); //reset
            LevelLibrary.regenerateInfinite();
            loadLevel(LevelType.INFINITE); //regenerate!
        }
        timeAtDeath = 0f;
        time = 0f;
        currCheckpoint = null;
        restart();
    }

    @Override
    public void resize(int width, int height) {
        if(width <= 0 || height <= 0) {
            return;
        }

        viewport.update(width, height, true);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        gameAssets.dispose();
    }
}
