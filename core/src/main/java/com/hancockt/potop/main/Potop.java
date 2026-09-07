package com.hancockt.potop.main;

import com.badlogic.gdx.Game;
import com.hancockt.potop.level.LevelType;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Potop extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
