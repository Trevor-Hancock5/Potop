package com.hancockt.lol;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class lol extends Game {
    @Override
    public void create() {
        setScreen(new FirstScreen(this, LevelType.TUTORIAL));
    }
}
