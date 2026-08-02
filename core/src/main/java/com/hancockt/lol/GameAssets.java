package com.hancockt.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;

public class GameAssets {
    public Texture mc;
    public Texture faucet;
    public Texture logo;
    public Texture checkpoint_inactive;
    public Texture checkpoint_active;
    public Texture platform;
    public Texture boundaryWall;
    public Texture winFaucet;
    public Music backgroundMusic;

    public void load(){
        mc = new Texture(Gdx.files.internal("pixelArt/mc.png"));
        faucet = new Texture(Gdx.files.internal("pixelArt/faucet.png"));
        logo = new Texture(Gdx.files.internal("pixelArt/logo.png"));
        checkpoint_inactive = new Texture(Gdx.files.internal("pixelArt/checkpoint_inactive.png"));
        checkpoint_active = new Texture(Gdx.files.internal("pixelArt/checkpoint_active.png"));
        platform = new Texture(Gdx.files.internal("pixelArt/platform.png"));
        boundaryWall = new Texture(Gdx.files.internal("pixelArt/boundaryWall.png"));
        winFaucet = new Texture(Gdx.files.internal("pixelArt/winFaucet.png"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_song.ogg"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.6f);
        backgroundMusic.play();
    }

    public void dispose(){
        mc.dispose();
        faucet.dispose();
        logo.dispose();
        checkpoint_inactive.dispose();
        checkpoint_active.dispose();
        platform.dispose();
        boundaryWall.dispose();
        winFaucet.dispose();
        backgroundMusic.dispose();
    }
}
