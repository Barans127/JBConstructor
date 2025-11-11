package com.engine.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.MoreUtils;
import com.engine.ui.controls.TopPainter;
import com.engine.ui.listeners.MainDraw;

/**
 * increment or decrement counting in a period of time.
 */

public class Counter implements MainDraw, Pool.Poolable{
    private float count, goal, step, start;
    private boolean isCounting, decrement;
    private CounterListener list;
    private CounterInformer informer;
    private boolean doNotDie;

    private boolean firstFrame; // work around bug, kai necontinous drawing.

    public Counter(){}

    /** @return if counter is counting or not. If it was removed from {@link TopPainter} lists then it will not be updated.
     * If that happens you can call {@link #resumeCount()} to resume counting. */
    public boolean isCounting(){
        return isCounting;
    }

    /**
     * pradeda skaičiavimą
     * @param start pradžios skaičius
     * @param goal rezultatas, kurį norima pasiekt
     * @param step per kiek rezultatas turi būti pasiektas (sekundem). turėtų būt teigiamas.
     */
    public void startCount(float start, float goal, float step){
        setValues(start, goal, step);
        startCount();
    }

    /** Sets counter values but doesn't start the counter. Start counter manually form {@link #startCount()} method. */
    public void setValues(float start, float goal, float step){
        if (start == goal){ // tik pasižymės, bet nieko nedarys.
            count = start;
            this.start = start;
            this.goal = goal;
            return;
        }
        float requiredAmount;
        if (goal > start){
            this.start = start;
            count = start;
            this.goal = goal;
            decrement = false;
            requiredAmount = goal - start;
        }else{
            this.start = goal;
            count = goal;
            this.goal = start;
            decrement = true;
            requiredAmount = start - goal;
        }
        if (step == 0){
//            GdxPongy.getInstance().setError("Counter: Step cannot be 0", ErrorMenu.ErrorType.WrongPara);
//            return;
            throw new IllegalArgumentException("Counter step cannot be 0");
        }
        this.step = requiredAmount / MoreUtils.abs(step);
    }

    /** Starts counter counting. Values must be set before starting via {@link #setValues(float, float, float)} method. */
    public void startCount(){
        if (start == goal){ // nieko tokio atveju nedarom.
            count = start;
            return;
        }
        count = start;
        // paleidziam skaiciavima.
        isCounting = true;
        if (!Gdx.graphics.isContinuousRendering())
            firstFrame = true;
        TopPainter.addPaintOnTop(this);
    }

    /** If counter was interrupted or stopped then call this method and counting will be resumed.
     * For example,  counting was stopped because pause was pressed. After pause counting can be continued. */
    public void resumeCount(){
        if (step == 0){ // neskaiciuos jeigu step = 0.
            return;
        }
        if (start == goal || count == goal){
            return; // cia irgi ner ko skaiciuot.
        }
        isCounting = true;
        if (!Gdx.graphics.isContinuousRendering())
            firstFrame = true;
        TopPainter.addPaintOnTop(this);
    }

    /** cancels current counting */
    public void cancel(){
        if (isCounting){
//            count = goal;
//            TopPainter.removeTopPaint(this);// sukelia bug jei sita naudosim drope. Vistiek pasalins is toppainterio kai vyks piesimas.
            isCounting = false;
        }
    }

    /** @return current value, if counter is counting, this value will change. */
    public float getCurrentValue(){
        if (decrement){
            return goal - count + start;
        }else {
            return count;
        }
    }

    /**@return start value, from where counter started counting. */
    public float getStartValue(){
        if (decrement){
            return goal;
        }else{
            return start;
        }
    }

    /** @return value which counteer is reaching. */
    public float getGoalValue(){
        if (decrement){
            return start;
        }else {
            return goal;
        }
    }

    /**
     * @return true if counter value is increasing, false otherwise
     */
    public boolean isIncreasing(){
        return !decrement;
    }

    /**
     * Disables counter cancelation. Method <code>cancel()</code> still can cancel counter.
     * @param allow false: cancel will be called when form changes, popup shows up or user calls drop.
     *              true: counter will not be cancelled by form changing, popup show up or user drop.
     */
    public void setUninterruptible(boolean allow){
        doNotDie = allow;
    }

    public void setCounterListiner(CounterListener e){
        list = e;
    }

    public void setCounterInformer(CounterInformer e){
        informer = e;
    }

    /** Called every time when value is changed.
     * Listener is called here. */
    protected void step(float old, float current){
        if (informer != null){
            informer.update(old, current);
        }
    }

    /** Called when counter finished counting.
     * Listener is called here. */
    protected void finished(){
        if (list != null){
            list.finished(getCurrentValue());
        }
    }

    @Override
    public void draw() { // naudos nepiešimui.
        if (isCounting){
            float time;
            if (firstFrame){ // work around, when not continuous drawing, bug when animation jumps forward very fast.
                time = 1f/60f; // Imagine something like having 60 timeframes, so here is one frame.
                firstFrame = false;
            }else{
                time = Gdx.graphics.getDeltaTime();
            }
            float old = getCurrentValue();
            count += step * time;
            if (count >= goal){
                count = goal;
                isCounting = false; // visada tokie pries visus listener turetu but.
//                if (informer != null) { // paskutini kart pranesam.
//                    informer.update(old, getCurrentValue());
//                }
                step(old, getCurrentValue());
//                if (list != null){
//                    list.finished(getCurrentValue());
//                }
                finished();
                return;
            }
//            if (informer != null)
//                informer.update(old, getCurrentValue());
            step(old, getCurrentValue());
        }else{
            TopPainter.removeTopPaint(this);
        }
    }

    @Override
    public boolean drop(int reason) {
        if (isCounting){
            if (doNotDie)
                return true;
            if (list != null) {
                if (list.cancel(reason)){
                    return true;
                }
//                else {
//                    isCounting = false;
//                }
            }
            isCounting = false; // bug fix kai be listener nepasizymedavo, kad nebeskaiciuos, kai nera listener.
        }
        return false;
    }

    @Override
    public void reset() {
        informer = null;
        list = null;
        isCounting = false;
        doNotDie = false;
    }

    public interface CounterListener{
        /** Called when {@link Counter} finnish counting. */
        void finished(float currentValue);
        /** Called when counter is being cancelled.
         * @return true if you do not want counter to stop counting. */
        boolean cancel(int reason);
    }

    public interface CounterInformer{
        /** Called every time when value is changed. */
        void update(float oldValue, float currentValue);
    }
}
