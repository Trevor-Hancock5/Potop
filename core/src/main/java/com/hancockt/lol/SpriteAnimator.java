package com.hancockt.lol;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class SpriteAnimator {
    private HashMap<AnimationKey, Animation<TextureRegion>> animations;
    private AnimationKey currKey;
    private float stateTime;
    private TextureRegion defaultSparkFrame;
    private TextureRegion defaultCheckpointFrame;
    private AnimationKey group; //have an instance of SpriteAnimator for each thing to animate (spark vs checkpoint)

    public SpriteAnimator(TextureRegion defaultFrame, AnimationKey group){
        animations = new HashMap<>();
        this.group = group;
        if(group == AnimationKey.CHECKPOINT){
            defaultCheckpointFrame = defaultFrame;
        } else if(group == AnimationKey.SPARK){
            defaultSparkFrame = defaultFrame;
        }
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
        stateTime += delta;
    }

    public TextureRegion getCurrentFrame(){
        boolean loop = animations.get(currKey).getPlayMode() == Animation.PlayMode.LOOP;
        return animations.get(currKey).getKeyFrame(stateTime, loop);
    }

    public boolean isFinished(){
        return animations.get(currKey).isAnimationFinished(stateTime);
    }
}
