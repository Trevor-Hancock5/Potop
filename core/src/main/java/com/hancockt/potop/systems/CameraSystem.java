package com.hancockt.potop.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hancockt.potop.state.GameState;
import com.hancockt.potop.state.PlayerData;

public class CameraSystem {
    private final OrthographicCamera camera;
    private final OrthographicCamera hudCam;
    private final Viewport viewport;
    private float screenShakeAccumulation;

    private static final float cameraLerpSpeed = .3f; //TUNABLE
    private static final float cameraDeadZoneDivisor = 5f; //TUNABLE
    private static final float screenShake = 0.3f; //time of shaking - TUNABLE
    private static final float screenShakeIntensity = 2f; //TUNABLE
    public static final float VIEWPORT_WIDTH = 800f; //TUNABLE
    public static final float VIEWPORT_HEIGHT = 480f; //TUNABLE
    private static final float lowestCameraY = 240f; //TUNABLE
    private static final float verticalLookaheadFactor = 0.1f; //TUNABLE

    public CameraSystem(){
        camera = new OrthographicCamera();
        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
    }

    public void updateCamera(CameraUpdateContext cameraUpdateContext) {
        PlayerData playerData = cameraUpdateContext.playerData();
        float delta = cameraUpdateContext.delta();
        float maxFallSpeed = cameraUpdateContext.maxFallSpeed();
        float deadzone = camera.viewportWidth / cameraDeadZoneDivisor;
        float deadzoneRight = camera.position.x + (deadzone / 2);
        float deadzoneLeft = camera.position.x - (deadzone / 2);

        float targetX = camera.position.x; //default is to stay put

        if(playerData.x < deadzoneLeft){
            targetX = playerData.x + (deadzone / 2);
        } else if (playerData.x > deadzoneRight){
            targetX = playerData.x - (deadzone / 2);
        }

        float vertLookAhead = playerData.velocityY * verticalLookaheadFactor;
        float targetY = cameraUpdateContext.gameState() == GameState.DEATH
            ? Math.max(cameraUpdateContext.waterLevel(), lowestCameraY)
            : Math.max(playerData.y+25+vertLookAhead, lowestCameraY);

        camera.position.x += (targetX - camera.position.x) * cameraLerpSpeed;
        camera.position.y += (targetY - camera.position.y) * (cameraLerpSpeed / 3);

        if(playerData.justLanded && screenShakeAccumulation < screenShake){
            if(playerData.impactVelocity / maxFallSpeed > 0.5){
                screenShakeAccumulation += delta / 2;
            } else {
                screenShakeAccumulation += delta;
            }

            // For both, we are changing from -3 to 3
            camera.position.x += (float) ((Math.random() - 0.5) * (screenShakeIntensity * (2 + playerData.impactVelocity / maxFallSpeed)));
            camera.position.y += (float) ((Math.random() - 0.5) * (screenShakeIntensity * (2 + playerData.impactVelocity / maxFallSpeed)));
        }
        camera.update();
    }

    public void resetScreenShake(){
        screenShakeAccumulation = 0;
    }

    public void resize(int width, int height){
        viewport.update(width, height, true);
    }

    public OrthographicCamera getHudCam(){
        return hudCam;
    }

    public OrthographicCamera getCamera(){
        return camera;
    }
}
