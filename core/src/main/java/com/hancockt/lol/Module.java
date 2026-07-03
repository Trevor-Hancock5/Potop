package com.hancockt.lol;

import com.badlogic.gdx.math.Vector2;

public class Module {
    String name;
    Vector2 endOffset;
    Platform[] platforms;

    public Module(String name, Vector2 endOffset, Platform[] platforms){
        this.name = name;
        this.endOffset = endOffset;
        this.platforms = platforms;
    }
}
