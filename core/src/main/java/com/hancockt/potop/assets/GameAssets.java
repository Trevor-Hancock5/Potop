package com.hancockt.potop.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameAssets {
    public Texture faucet;
    public Texture logo;
    public Texture checkpoint_inactive;
    public Texture platform;
    public Texture boundaryWall;
    public Texture winFaucet;
    public Texture currency;
    public Texture prestigeCurrency;

    public SpriteAnimator sparkAnimator;
    public SpriteAnimator checkpointAnimator;

    public Music backgroundMusic;

    public void load(){
        //Art
        faucet = new Texture(Gdx.files.internal("pixelArt/faucet.png"));
        logo = new Texture(Gdx.files.internal("pixelArt/logo.png"));
        checkpoint_inactive = new Texture(Gdx.files.internal("pixelArt/checkpoint_inactive.png"));
        platform = new Texture(Gdx.files.internal("pixelArt/platform.png"));
        boundaryWall = new Texture(Gdx.files.internal("pixelArt/boundaryWall.png"));
        winFaucet = new Texture(Gdx.files.internal("pixelArt/winFaucet.png"));
        currency = new Texture(Gdx.files.internal("pixelArt/currency.png"));
        prestigeCurrency = new Texture(Gdx.files.internal("pixelArt/prestigeCurrency1.png"));

        //animations
        loadAnimations();

        //music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_song.ogg"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.6f);
        backgroundMusic.play();
    }

    private void loadAnimations(){
        //sparkAnimator
        Texture spark = new Texture(Gdx.files.internal("pixelArt/spark.png"));

        //separate animations
        TextureRegion[] idle = sliceRow(spark, 0, 13, 32, 64);
        TextureRegion[] runLeft = sliceRow(spark, 1, 9, 32, 64);
        TextureRegion[] jump = sliceRow(spark, 2, 11, 32, 64);
        TextureRegion[] fall = sliceRow(spark, 3, 7, 32, 64);
        TextureRegion[] death = sliceRow(spark, 4, 6, 32, 64);
        TextureRegion[] runRight = sliceRow(spark, 5, 9, 32, 64);

        //get default frame
        TextureRegion defaultFrame = idle[0]; //(maybe default to last frame of current animation)

        //create animations
        Animation<TextureRegion> idleAnimation = new Animation<>(0.1f, idle);
        Animation<TextureRegion> runLeftAnimation = new Animation<>(0.1f, runLeft);
        Animation<TextureRegion> jumpAnimation = new Animation<>(0.1f, jump);
        Animation<TextureRegion> fallAnimation = new Animation<>(0.1f, fall);
        Animation<TextureRegion> deathAnimation = new Animation<>(0.1f, death);
        Animation<TextureRegion> runRightAnimation = new Animation<>(0.1f, runRight);

        //set looping if so
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        runLeftAnimation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        jumpAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        fallAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        deathAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        runRightAnimation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        //set up animator object
        sparkAnimator = new SpriteAnimator(defaultFrame);
        sparkAnimator.register(AnimationKey.IDLE, idleAnimation);
        sparkAnimator.register(AnimationKey.RUN_LEFT, runLeftAnimation);
        sparkAnimator.register(AnimationKey.JUMP, jumpAnimation);
        sparkAnimator.register(AnimationKey.FALL, fallAnimation);
        sparkAnimator.register(AnimationKey.DEATH, deathAnimation);
        sparkAnimator.register(AnimationKey.RUN_RIGHT, runRightAnimation);

        //checkpointAnimator
        Texture checkpoint_active = new Texture(Gdx.files.internal("pixelArt/checkpoint_active.png"));
        TextureRegion[] checkpoint = sliceRow(checkpoint_active, 0, 6, 40, 100);
        Animation<TextureRegion> checkpointAnimation = new Animation<>(0.1f, checkpoint);
        checkpointAnimation.setPlayMode(Animation.PlayMode.LOOP);
        checkpointAnimator = new SpriteAnimator(new TextureRegion(checkpoint_inactive));
        checkpointAnimator.register(AnimationKey.CHECKPOINT_ACTIVE, checkpointAnimation);

    }

    private TextureRegion[] sliceRow(Texture texture, int row, int frameCount, int frameWidth, int frameHeight){
        TextureRegion[][] ret = TextureRegion.split(texture, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[frameCount];
        System.arraycopy(ret[row], 0, frames, 0, frameCount);
        return frames;
    }

    public void dispose(){
        faucet.dispose();
        logo.dispose();
        checkpoint_inactive.dispose();
        platform.dispose();
        boundaryWall.dispose();
        winFaucet.dispose();
        backgroundMusic.dispose();
    }
}
