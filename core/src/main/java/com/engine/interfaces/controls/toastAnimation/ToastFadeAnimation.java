package com.engine.interfaces.controls.toastAnimation;

import com.engine.animations.Counter;
import com.engine.core.Engine;
import com.engine.interfaces.controls.Toast;
import com.engine.interfaces.controls.ToastAnimation;

/** Toast appears and fades away. */
public class ToastFadeAnimation implements ToastAnimation {
    private Toast owner;
    private float animationTime = 0.5f;

    private final Counter counter;
    private boolean isTinted = false; // nustatysim ar tikroji spalva modifikuota.
//    private Color prevColor; // laikysim tikraja spalva.

//    private SpriteBatch batch = Engine.getInstance().getBatch();

    public ToastFadeAnimation(){
        counter = new Counter();
        counter.setUninterruptible(true); // kad nedingtu kai nereik.
        counter.setCounterListiner(new Counter.CounterListener() {
            @Override
            public void finished(float currentValue) {
                if (!counter.isIncreasing()){
                    owner.clearToast();
//                    clearColor(); // man atrodo cia jo nereik.
                }
            }

            @Override
            public boolean cancel(int reason) {
                return true;
            }
        });
    }

    /* veikimas */

    private void clearColor(){
        if (isTinted){ // cia atstatys atgal i normalia spalva.
            isTinted = false;
//            batch.setColor(prevColor);
            Engine.getInstance().noForceTint();
        }
    }

    /** set animation speed in seconds */
    public void setAnimationTime(float time){
        if (time > 0){
            animationTime = time;
        }
    }

    public float getAnimationTime() {
        return animationTime;
    }

    /* override */

    @Override
    public void appear() {
        if (!counter.isCounting())
            counter.startCount(0, 1f, animationTime);
    }

    @Override
    public void disappear() {
        if (!counter.isCounting())
            counter.startCount(1f, 0, animationTime);
    }

    @Override
    public void setToast(Toast owner) {
        this.owner = owner;
    }

    @Override
    public void update() {

    }

    @Override
    public void drawBackground() {
        if (counter.isCounting()){ // sitoj dali darys toki pilkejima ir dingima.
            isTinted = true;
//            prevColor = batch.getColor(); // Tinka, spalva neturi keistis.
//            batch.setColor(prevColor.r, prevColor.g, prevColor.b, prevColor.a * counter.getCurrentValue());
            Engine.getInstance().forceTint(counter.getCurrentValue(), 1, 1, 1);

        }
    }

    @Override
    public void drawOnTop() {
        clearColor();
    }
}
