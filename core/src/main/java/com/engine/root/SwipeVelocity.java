package com.engine.root;

import com.badlogic.gdx.math.Vector2;
import com.engine.core.MoreUtils;
import com.engine.ui.controls.TopPainter;
import com.engine.ui.listeners.MainDraw;

/**
 * Swipe velocity after user swipe. It creates animation that it is still going.
 * Still in development...
 */

public class SwipeVelocity implements MainDraw{
    private final Vector2 velocity, rez;
    private float damping, maxSpeed;
    private boolean isMoving;
    private SwipeVelocityListener list;

    private float isNegativeX, isNegativeY;

    public SwipeVelocity(){
        velocity = new Vector2();
        rez = new Vector2();
    }

    public SwipeVelocity(SwipeVelocityListener list){
        this();
        addSwipeVelocityListener(list);
    }

    public void setDamping(float damping){
        this.damping = damping;
    }

    public void setMaxSpeed(float maxSpeed){
        this.maxSpeed = maxSpeed;
    }

    public void cancel(){
        isMoving = false;
    }

    public void stop(){
        isMoving = false;
        if (list != null){
            list.onStop();
        }
    }

    public void addSwipeVelocityListener(SwipeVelocityListener e){
        list = e;
    }

    public boolean fling(float velocityX, float velocityY){
        isNegativeX = velocityX < 0 ? -1 : 1;
        isNegativeY = velocityY >= 0 ? -1 : 1;
        velocity.set(MoreUtils.abs(velocityX), MoreUtils.abs(velocityY));
        isMoving = true;
        TopPainter.addPaintOnTop(this);
        return false;
    }

    @Override
    public void draw() {
        if (isMoving){
            float x, y;
            x = velocity.x - damping;
            y = velocity.y - damping;

        }else {
            TopPainter.removeTopPaint(this);
        }
    }

    @Override
    public boolean drop(int reason) {
        return true;
    }

    public interface SwipeVelocityListener{
        public void onMove(Vector2 move);
        public void onStop();
    }
}
