package com.hancockt.lol;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class SpriteAnimator {
    private HashMap<AnimationKey, Animation<TextureRegion>> animations;
    private AnimationKey currKey;
    private float stateTime;
    private TextureRegion defaultFrame;
    private AnimationKey group; //have an instance of SpriteAnimator for each thing to animate (spark vs checkpoint)

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

        if(!animation.isAnimationFinished(stateTime)) {
//            stateTime += delta;
        }
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
