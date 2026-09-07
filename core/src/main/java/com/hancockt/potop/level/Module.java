package com.hancockt.potop.level;

import com.badlogic.gdx.math.Vector2;
import com.hancockt.potop.features.Platform;

public class Module {
    private final String name;
    private final Vector2 endOffset;
    private Platform[] platforms;

    public Module(String name, Vector2 endOffset, Platform[] platforms){
        this.name = name;
        this.endOffset = endOffset;
        this.platforms = platforms;
    }

    public String getName() {
        return name;
    }

    public Vector2 getEndOffset() {
        return endOffset;
    }

    public Platform[] getPlatforms() {
        return platforms;
    }
}
