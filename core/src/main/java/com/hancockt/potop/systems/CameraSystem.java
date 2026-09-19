package com.hancockt.potop.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hancockt.potop.main.GameFlow;
import com.hancockt.potop.main.PhysicsEngine;
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

    public void updateCamera(float delta, PlayerData playerData, WaterSystem waterSystem) {
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
        float targetY = GameFlow.GAME_STATE == GameState.DEATH ? Math.max(waterSystem.getWaterLevel(), lowestCameraY) : Math.max(playerData.y+25+vertLookAhead, lowestCameraY);

        float lerpFactor = 1f - (float)Math.pow(1f - cameraLerpSpeed, delta * 60f);

        camera.position.x += (targetX - camera.position.x) * lerpFactor;
        camera.position.y += (targetY - camera.position.y) * (lerpFactor / 3f);

        if(playerData.justLanded && screenShakeAccumulation < screenShake){
            if(playerData.impactVelocity / PhysicsEngine.maxFallSpeed > 0.5){
                screenShakeAccumulation += delta / 2;
            } else {
                screenShakeAccumulation += delta;
            }

            // For both, we are changing from -3 to 3
            float shakeScale = delta * 60f; // normalizes to 60fps equivalent
            camera.position.x += (float)((Math.random() - 0.5) * (screenShakeIntensity * (2 + playerData.impactVelocity / PhysicsEngine.maxFallSpeed)) * shakeScale);
            camera.position.y += (float)((Math.random() - 0.5) * (screenShakeIntensity * (2 + playerData.impactVelocity / PhysicsEngine.maxFallSpeed)) * shakeScale);
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
