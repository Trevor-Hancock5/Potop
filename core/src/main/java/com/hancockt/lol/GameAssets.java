package com.hancockt.lol;

import com.badlogic.gdx.Gdx;
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

    public void load(){
        mc = new Texture(Gdx.files.internal("mc.png"));
        faucet = new Texture(Gdx.files.internal("faucet.png"));
        logo = new Texture(Gdx.files.internal("logo.png"));
        checkpoint_inactive = new Texture(Gdx.files.internal("checkpoint_inactive.png"));
        checkpoint_active = new Texture(Gdx.files.internal("checkpoint_active.png"));
        platform = new Texture(Gdx.files.internal("platform.png"));
        boundaryWall = new Texture(Gdx.files.internal("boundaryWall.png"));
        winFaucet = new Texture(Gdx.files.internal("winFaucet.png"));
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
    }
}
