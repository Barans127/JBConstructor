package com.engine.interfaces.controls;

import com.badlogic.gdx.math.Vector2;
import com.engine.animations.Counter;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.interfaces.listeners.MainDraw;

/**
 * implemantation for ballon pop up.
 */
public abstract class Balloon implements MainDraw{
    private int dropLevel = 3;
    private Vector2 position;
    private float offsetX, offsetY;
    private float width, height, lostWidt, lostHeight;
//    public Color tint;
//    protected float oldA;

    private float animatingTime = 1f;
    private boolean useAnimation = true;
    private Counter count;

    private int time, last;
    private boolean isTime;

    public Balloon(){
        position = new Vector2();
//        tint = new Color(1, 1, 1, 1);
        count = MoreUtils.getCounter();
        count.setCounterInformer(new Counter.CounterInformer() {
            @Override
            public void update(float oldValue, float currentValue) {
                if (count.isIncreasing()){
                    showing(currentValue);
                }else {
                    hiding(currentValue);
                    if (currentValue == 0){ // baige darba.
//                        tint.a = oldA;
                        TopPainter.removeTopPaint(Balloon.this);
                        onHide();
                    }
                }
            }
        });
//        count.setCounterListiner(new Counter.CounterListener() {
//            @Override
//            public void finished(float currentValue) {
//                System.out.print(0);
////                GdxPongy.getInstance().background(0);
////                GdxPongy.getInstance().getBatch().flush();
//                Gdx.graphics.requestRendering();
//            }
//
//            @Override
//            public boolean cancel(int reason) {
//                return false;
//            }
//        });
    }

    public void setPosition(Vector2 e){
        setPosition(e.x, e.y);
    }

    public void setPosition(float x, float y){
        position.set(x, y);
        checkPosition();
    }

    /**
     * sets drop level. Higher means balloon will be shut down after drop.
     * @param dropLevel 0 - no drop, 1 - user drop, 2 - user drop, form change, 3 - any drop.
     */
    public void setDropLevel(int dropLevel){
        this.dropLevel = dropLevel;
    }

    /**
     * Sets time for balloon animating time. value of 0 will turn off all animation counting. Default time is 1.
     * @param time in seconds.
     */
    public void setAnimatingTime(float time){
        animatingTime = MoreUtils.abs(time);
    }

    /** Use default animation or not. Animation methods will not be affected.*/
    public void useAnimation(boolean use){
        useAnimation = use;
    }

    public void setSize(float width, float height){
        this.width = width;
        this.height = height;
        checkPosition();
    }

    public void setBalloon(float x, float y, float width, float height){
        setPosition(x, y);
        setSize(width, height);
    }

    private void checkPosition(){
        boolean changedPos = false, changedSize = false;
        Engine p = Engine.getInstance();
        float screenWidth = p.getScreenWidth();
        float screenHeight = p.getScreenHeight();
        offsetX = 0;
        offsetY = 0;
        lostHeight = 0;
        lostWidt = 0;
        if (width > screenWidth){
            lostWidt = width - screenWidth;
            width = screenWidth;
            changedSize = true;
        }
        if (height > screenHeight){
            lostHeight = height - screenHeight;
            height = screenHeight;
            changedSize = true;
        }
        if (position.x < 0){
            offsetX = position.x;
            position.x = 0;
            changedPos = true;
        }else if(position.x + width > screenWidth){
            float old = position.x;
            position.x = screenWidth - width;
            offsetX = position.x - old;
            changedPos = true;
        }
        if (position.y < 0){
            offsetY = position.y;
            position.y = 0;
            changedPos = true;
        }else if (position.y + height > screenHeight){
            float old = position.y;
            position.y = screenHeight - height;
            offsetY = position.y - old;
            changedPos = true;
        }
        if (changedPos){
            positionRearranged(position.x, position.y, offsetX, offsetY);
        }
        if (changedSize){
            sizeRearranged(width, height, lostWidt, lostHeight);
        }
    }

    /** hides automatically balloon after time interval. Time in seconds. */
    public void setTimer(int timeInterVal){
        if (timeInterVal <= 0){
            isTime = false;
        }else {
            isTime = true;
            last = timeInterVal;
        }
    }

    public Vector2 getPosition(){
        return position;
    }

    public float getOffsetX(){
        return offsetX;
    }

    public float getOffsetY(){
        return offsetY;
    }

    public float getWidth(){
        return width;
    }

    public float getHeight(){
        return height;
    }

    public float getLostWidth(){
        return lostWidt;
    }

    public float getLostHeight(){
        return lostHeight;
    }

    public float getAnimatingTime(){
        return animatingTime;
    }

    /** if currently animation is in progress  */
    public boolean isAnimating(){
        return count.isCounting();
    }

    /** @return true if this balloon is currently visible. */
    public boolean isShown(){
        return TopPainter.containsMainDraw(this);
    }

    /** @return current animation progress. if animation is not in progress than value of 1 will be returned */
    public float getAnimationProgress(){
        if (count.isCounting()){
            return count.getCurrentValue();
        }else {
            return 1f;
        }
    }

    /**
     * Pieš balioną nurodytoj vietoj
     * @param x x pozicija
     * @param y y pozicija
     * @param width baliono ilgis
     * @param height baliono aukstis
     * @param offsetX perkelto x taško skirtumas
     * @param offsetY perkelto y taško skirtumas
     */
    protected abstract void draw(float x, float y, float width, float height, float offsetX, float offsetY);

    /** called when animating is in progress. Will not be called if animation time is 0.
     * progress goes form 0 to 1. */
    protected void showing(float progress){
//        tint.a = progress*oldA;
    }

    /** called when animating is in progress. Will not be called if animation time is 0.
     * progress goes form 1 to 0. */
    protected void hiding(float progress){
//        tint.a = progress*oldA;
    }

    /** will be called when balloon is shown. */
    protected void onShow(){

    }

    /** will be called when balloon is hidden. */
    protected void onHide(){

    }

    /** called if position was changed due to not whole balloon was in screen. */
    protected void positionRearranged(float x, float y, float offsetX, float offsetY){

    }

    /** called if size was changed due to size didn't fit in screen */
    protected void sizeRearranged(float width, float height, float lostWidth, float lostHeight){

    }

    @Override
    public final void draw() {
        Engine p = Engine.getInstance();
        if (isTime){
            if (time + last < p.millis()){
                hide();
            }
        }
        float width, height;
//        boolean ignoreScissor;
        if (useAnimation && count.isCounting()){
            width = this.width * count.getCurrentValue();
            height = this.height * count.getCurrentValue();
//            ignoreScissor = false;
        }else {
            width = this.width;
            height = this.height;
//            ignoreScissor = true;
        }
        if (p.pushScissor(position.x, position.y, width, height)) {
            draw(position.x, position.y, this.width, this.height, offsetX, offsetY);
            p.popScissor();
        }
//        else{
//            System.out.println("As ignoruoju piesima");
//        }

//        Engine p = GdxPongy.getInstance(); // tessst
//        p.text("test", 100, 100);
//        p.noFill();
//        p.stroke(0);
//        p.strokeWeight(3f);
//        p.rect(position.x, position.y, width, height);
    }

    @Override
    public boolean drop(int reason) { // dropins visi.
        if (reason > dropLevel-1){
            return true;
        }
        if (count.isCounting()) {
////            tint.a = oldA;
//            if (!count.isIncreasing()){ // reiskia dabar hidinas.
////                TopPainter.removeTopPaint(this); // bugint gali, drope vistiek gi sita ismes.
//                onHide();
//            }
            count.cancel();
        }
        onHide(); // gi turi visada pranest, kad uzdaro.
        return false;
    }

    public final void show(){
        if (TopPainter.containsMainDraw(this)){
            return; // jau yra.
        }
        if (isTime){
            time = Engine.getInstance().millis();
        }
        if (width == 0 || height == 0){
            System.out.println("Balloon: Cannot open balloon when size == 0");
            return; // kam reik, jeigu vistiek nera dydzio
        }
//        if (count.isCounting()){
//            tint.a = 0;
//        }else {
//            oldA = tint.a;
//        }
//        oldA = tint.a;
        if (animatingTime > 0)
            count.startCount(0, 1, animatingTime);
        TopPainter.addPaintOnTop(this);
        onShow();
    }

    public final void hide(){
//        oldA = tint.a;
//        if (count.isCounting()){
//            tint.a = oldA;
//        }else {
//            oldA = tint.a;
//        }
        if (!TopPainter.containsMainDraw(this)){
            return; // jeigu nera tai nedarom dar karta hide.
        }
        if (animatingTime > 0) {
            boolean countT = true;
            if (count.isCounting()){
                if (!(count.getStartValue() == 0)){
                    countT = false;
                }
            }
            if (countT)
                count.startCount(1, 0, animatingTime);
        }else {
            TopPainter.removeTopPaint(this);
            onHide();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (count != null){
            MoreUtils.freeCounter(count);
            count = null;
        }
    }
}
