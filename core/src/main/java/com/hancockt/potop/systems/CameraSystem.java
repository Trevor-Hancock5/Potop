package com.hancockt.potop.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hancockt.potop.main.PhysicsEngine;
import com.hancockt.potop.state.PlayerData;

public class CameraSystem {
    private final OrthographicCamera camera;
    private final OrthographicCamera hudCam;

    private final float cameraLerpSpeed = .3f;
    private final float cameraDeadZoneDivisor = 5f;
    private final float screenShake = 0.3f; //time of shaking
    private final float screenShakeIntensity = 2f;
    private float screenShakeAccumulation;

    private final Viewport viewport;
    public static final float VIEWPORT_WIDTH = 800f;
    public static final float VIEWPORT_HEIGHT = 480f;

    public CameraSystem(){
        camera = new OrthographicCamera();
        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
    }

    public void updateCamera(float delta, WaterSystem waterSystem) {
        final float lowestCameraY = 240f;

        float deadzone = camera.viewportWidth / cameraDeadZoneDivisor;
        float deadzoneRight = camera.position.x + (deadzone / 2);
        float deadzoneLeft = camera.position.x - (deadzone / 2);

        float targetX = camera.position.x; //default is to stay put

        if(PlayerData.x < deadzoneLeft){
            targetX = PlayerData.x + (deadzone / 2);
        } else if (PlayerData.x > deadzoneRight){
            targetX = PlayerData.x - (deadzone / 2);
        }

        float vertLookAhead = PlayerData.velocityY * 0.1f;
        float targetY = PlayerData.isDead ? Math.max(waterSystem.getWaterLevel(), lowestCameraY) : Math.max(PlayerData.y+25+vertLookAhead, lowestCameraY);

        camera.position.x += (targetX - camera.position.x) * cameraLerpSpeed;
        camera.position.y += (targetY - camera.position.y) * (cameraLerpSpeed / 3);

        if(PlayerData.justLanded && screenShakeAccumulation < screenShake){
            if(PlayerData.impactVelocity / PhysicsEngine.maxFallSpeed > 0.5){ //TODO
                screenShakeAccumulation += delta / 2;
            } else {
                screenShakeAccumulation += delta;
            }

            // For both, we are changing from -3 to 3
            camera.position.x += (float) ((Math.random() - 0.5) * (screenShakeIntensity * (2 + PlayerData.impactVelocity / PhysicsEngine.maxFallSpeed))); //TODO
            camera.position.y += (float) ((Math.random() - 0.5) * (screenShakeIntensity * (2 + PlayerData.impactVelocity / PhysicsEngine.maxFallSpeed)));
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
