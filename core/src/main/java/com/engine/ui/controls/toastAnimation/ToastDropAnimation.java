package com.engine.ui.controls.toastAnimation;

import com.badlogic.gdx.math.Vector2;
import com.engine.animations.Counter;
import com.engine.core.Engine;
import com.engine.ui.controls.PanelHost;
import com.engine.ui.controls.Toast;
import com.engine.ui.controls.ToastAnimation;

/** Toast animation. Makes toast to drop down from top and hide back. */
public class ToastDropAnimation implements ToastAnimation {
    private Counter counter;
    private float animationTime = 0.5f; // dedam puse sekundes animacijos trukme.
    private Toast owner;

    private float ry; // issaugosim tikruosius cord. y cord.

    public ToastDropAnimation(){
        counter = new Counter();
        counter.setUninterruptible(true);
        counter.setCounterInformer(new Counter.CounterInformer() {
            @Override
            public void update(float oldValue, float currentValue) {
                PanelHost e = owner.getHost();
                e.setPosition(e.getPosition().x, currentValue); // tiesiog nustatysim naujas cord kiekviena kart.
            }
        });
        counter.setCounterListiner(new Counter.CounterListener() {
            @Override
            public void finished(float currentValue) {
                if (counter.isIncreasing()){ // cia reiskia kyla aukstyn
                    owner.clearToast(); // salinam is vaizdo sita
                    PanelHost e = owner.getHost();
                    e.setPosition(e.getPosition().x, ry); // perstatom i senasias cord, kur turetu but.
                }
            }

            @Override
            public boolean cancel(int reason) {
                return true;
            }
        });
    }

    /** animation time in seconds. */
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
        if (!counter.isCounting()) { // neturi but vykdomas skaiciavimas.
            Vector2 pos = owner.getHost().getPosition();
            ry = pos.y;
            float screenHeight = Engine.getInstance().getScreenHeight();
            counter.startCount(screenHeight, ry, animationTime);
        }
    }

    @Override
    public void disappear() {
        if (!counter.isCounting()){
            Vector2 pos = owner.getHost().getPosition();
            ry = pos.y;
            float screenHeight = Engine.getInstance().getScreenHeight();
            counter.startCount(ry, screenHeight, animationTime);
        }
    }

    @Override
    public void setToast(Toast owner) {
        this.owner = owner;
    }

    @Override
    public void update() { }

    @Override
    public void drawBackground() { }

    @Override
    public void drawOnTop() { }
}
