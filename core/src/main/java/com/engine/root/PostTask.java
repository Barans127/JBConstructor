package com.engine.root;

import com.engine.ui.controls.TopPainter;
import com.engine.ui.listeners.MainDraw;

/** post a task with given time. It will be executed on main thread. */
public class PostTask implements MainDraw {
    private int time;
    private int currentTime;

    private Runnable runnable;

    public PostTask(){}

    public PostTask(Runnable runnable, int time){
        this.time = time;
        this.runnable = runnable;
    }

    /** is a task posted */
    public boolean isPosted(){
        return TopPainter.containsMainDraw(this);
    }

    /** Executes runnable after given time. Time in milliseconds. */
    public void post(){
        currentTime = getMillisTime();
        TopPainter.addPaintOnTop(this);
    }

    /** Executes runnable after given time. Time in milliseconds. */
    public void post(Runnable runnable, int time){
        this.runnable = runnable;
        this.time = time;
        post();
    }

    /** Set time for post. Time in milliseconds. */
    public void setTime(int time){
        this.time = time;
    }

    /** Set new runnable. */
    public void setRunnable(Runnable runnable){
        this.runnable = runnable;
    }

    /** This post task runnable. */
    public Runnable getRunnable(){
        return runnable;
    }

    /** for how long will delay till execution. */
    public int getLifeTime(){
        return time;
    }

    /** Cancel task if it is posted. */
    public void cancel(){
        TopPainter.removeTopPaint(this);
    }

    protected void run(){
        if (runnable != null){
            runnable.run();
        }
    }

    /** Override this if you want to change time counting. Etc: in game time should not be counted while on pause.
     * default: time is taken from {@link GdxWrapper#millis()}  */
    protected int getMillisTime(){
        return GdxWrapper.getInstance().millis();
    }

    @Override
    public void draw() {
        if (currentTime + time < getMillisTime()){
            TopPainter.removeTopPaint(this);
            run();
        }
    }

    @Override
    public boolean drop(int reason) {
        return true;
    }
}
