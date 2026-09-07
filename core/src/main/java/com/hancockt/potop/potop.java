package com.hancockt.potop;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class potop extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen(this, LevelType.TUTORIAL));
    }
}
