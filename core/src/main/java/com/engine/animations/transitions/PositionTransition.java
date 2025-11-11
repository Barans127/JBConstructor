package com.engine.animations.transitions;

import com.badlogic.gdx.math.Vector2;
import com.engine.animations.Counter;
import com.engine.ui.controls.Control;

/** Position transitions allows you to change position smoothly (from one value to another), over a given duration.
 * {@link Vector2} and {@link Control} positions can be moved smoothly. */
public class PositionTransition {
    private Counter counter;

    private Vector2 vectorPosition;
    private Control controlPosition;

    private boolean isInterface;
    private boolean isMoving;

    private float startX, startY, difX, difY;

    private ObjectStepListener objectStepListener;
    private ReachedGoalListener reachedGoalListener;

    public PositionTransition(){
        counter = new Counter();
        counter.setUninterruptible(true); // nestabdom tranziciju.
        counter.setCounterInformer(new Counter.CounterInformer() {
            @Override
            public void update(float oldValue, float currentValue) {
                Object e;
                if (isInterface){
                    controlPosition.setPosition(startX + difX*currentValue, startY + difY*currentValue);
                    e = controlPosition;
                }else {
                    vectorPosition.set(startX + difX*currentValue, startY + difY*currentValue);
                    e = vectorPosition;
                }
                step(e);
                if (currentValue == counter.getGoalValue()){ // viskas baige darba.
                    vectorPosition = null;
                    controlPosition = null;
                    reachedGoal(e);
                }
            }
        });
    }

    /** Interface to transit to another position.
     * @param x to transit x cord
     * @param y to transit y cord
     * @param time time in seconds how long transition should occur.*/
    public void transit(Control e, float x, float y, float time){
        isInterface = true;
        controlPosition = e;
        Vector2 pos = e.getPosition();
        startX = pos.x; // pasizymim kur pradzios stovejimas buvo.
        startY = pos.y;
        difX = x - pos.x; // surandam skirtma, kiek nutole nuo norimo tasko.
        difY = y - pos.y;

        counter.startCount(0, 1, time);
        isMoving = true;
    }

    /** Vector2 to transit to another coordinates.
     * @param x to transit x cord
     * @param y to transit y cord
     * @param time time in seconds how long transition should occur.*/
    public void transit(Vector2 e, float x, float y, float time){
        isInterface = false;
        vectorPosition = e;
        startY = e.y;
        startX = e.x;
        difY = y - e.y;
        difX = x - e.x;

        counter.startCount(0, 1, time);
        isMoving = true;
    }

    /** Cancel transition. Will not return to first starting position */
    public void cancel(){
        counter.cancel();
    }

    /** If transition was canceled it can be resumed. Does nothing if transition was not cancelled. */
    public void resume(){
        if (isMoving)
            counter.resumeCount();
    }

    protected void reachedGoal(Object e){
        if (reachedGoalListener != null)
            reachedGoalListener.objectReachGoal(e);
    }

    protected void step(Object e){
        if (objectStepListener != null){
            objectStepListener.objectStepped(e);
        }
    }

    /* listeners */

    /** listen when interface of vector reaches it's position. Returns as object. */
    public void setReachGoalListener(ReachedGoalListener e){
        reachedGoalListener = e;
    }

    /** Called every time object is moved. */
    public void setObjectStepListener(ObjectStepListener e){
        objectStepListener = e;
    }

    public ObjectStepListener getObjectStepListener(){
        return objectStepListener;
    }

    public ReachedGoalListener getReachedGoalListener(){
        return reachedGoalListener;
    }

    public interface ReachedGoalListener{
        /** called when object reaches it's goal */
        void objectReachGoal(Object e);
    }

    public interface ObjectStepListener{
        /** called when object steps. */
        void objectStepped(Object e);
    }
}
