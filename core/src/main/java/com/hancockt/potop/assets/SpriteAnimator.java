package com.hancockt.potop.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class SpriteAnimator {
    private final HashMap<AnimationKey, Animation<TextureRegion>> animations;
    private AnimationKey currKey;
    private float stateTime;
    private final TextureRegion defaultFrame;

    public SpriteAnimator(TextureRegion defaultFrame){
        animations = new HashMap<>();
        this.defaultFrame = defaultFrame;
    }

    public void register(AnimationKey key, Animation<TextureRegion> animation){
        animations.put(key, animation);
    }

    public void play(AnimationKey key){
        if(currKey != key){
            currKey = key;
            stateTime = 0;
        }
    }

    public void update(float delta){
        Animation<TextureRegion> animation = animations.get(currKey);
        if(animation == null){
            return;
        }
        stateTime += delta;
    }

    public TextureRegion getCurrentFrame(){
        Animation<TextureRegion> animation = animations.get(currKey);
        if(animation == null){
            return getDefaultFrame();
        }

        return animation.getKeyFrame(stateTime);
    }

    public TextureRegion getDefaultFrame(){
        return defaultFrame;
    }
}
