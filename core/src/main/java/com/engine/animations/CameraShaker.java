package com.engine.animations;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.engine.core.Engine;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.listeners.MainDraw;

/** Shakes camera. Creates effect as earth quake or something that was hit very hard. */
public class CameraShaker implements MainDraw {
    // para
    private OrthographicCamera camera;
    private int shakeTime = 200; // kiek laiko truks viskas.
    private float shakeStrength = 25f; // kaip stipriai kratys.
    // sitas nurodys kokiu greiciu kratyt. greiciau leciau. Leciau nei frame tai neis.
    // frame greitis kazkur 15-16 jeigu 60fps. Nustacius mazesni, gausis, kad tiesiog ant kiekvieno frame kratyt.
    private int shakeIntensity = 30;
    private boolean xAxis = false, yAxis = true; // kurias axis kratyt.

    // -1: nieks nenudropins, uninterruptible. 0: user drop tik, 1: user drop ir form change., 2: visi dropina.
    private int dropReason = 2; // nuo visko. imanoma jokio drop ir cia padaryt.
//    private boolean uninterruptible = false; // o, gal isvis jokio drop?

    //working.
    private int time, lastTime, intensityTime;
    private boolean isShaking;
    private boolean isDown, isLeft, firstTime;

    public CameraShaker(){
        setCamera(null);
    }

    public CameraShaker(OrthographicCamera camera){
        setCamera(camera);
    }

    public CameraShaker(int shakeTime){
        setShakeTime(shakeTime);
    }

    public CameraShaker(int shakeTime, float shakeIntensity){
        this(shakeTime);
        setShakeStrength(shakeIntensity);
    }

    // setters
    /** Set this to set shake speed. Speed is in time milliseconds.
     * ETC: 30 - means every 30ms camera will be shaken. Default: 30. */
    public void setShakeIntensity(int intensity){
        shakeIntensity = intensity;
    }

    /** Set camera for which shake effect will be used. Leave it null and default absolute camera will be used. */
    public void setCamera(OrthographicCamera camera){
        if (camera != null)
            this.camera = camera;
        else
            this.camera = Engine.getInstance().getAbsoluteCamera();
    }

    /** how long should shake effect last. Time in milliseconds. Default: 200*/
    public void setShakeTime(int time){
        shakeTime = time;
    }

    /** how strong should shake be. Shake strength in camera units. Default: 25*/
    public void setShakeStrength(float shakeStrength){
        this.shakeStrength = shakeStrength;
    }

    /** Which axis should be shook. Default xAxis: false, yAxis: true. */
    public void setShakingAxis(boolean xAxis, boolean yAxis){
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    /** this effect uses {@link MainDraw} to shake. This effect can be cancelled by some reasons. Set reason to prevent
     * cancellation or to cancel on some events. Default: always cancel from anything.
     * -1: uninterruptible, will not be cancelled by anything.
     * 0: can be cancelled only if user called {@link TopPainter#release()} method.
     * 1: can be cancelled by user drop or form change drop.
     * 2: will be cancelled on any event.*/
    public void setDropReason(int dropReason){
        this.dropReason = dropReason;
    }

    // getters

    public int getShakeIntensity(){
        return shakeIntensity;
    }

    public OrthographicCamera getCamera(){
        return camera;
    }

    public int getDropReason(){
        return dropReason;
    }

    public boolean isXAxisShaking(){
        return xAxis;
    }

    public boolean isYAxisShaking(){
        return yAxis;
    }

    public float getShakeStrength(){
        return shakeStrength;
    }

    public int getShakeTime(){
        return shakeTime;
    }

    /** How much this effect will last.
     * @return 0 if effect is not active. If you want to know how long this effect last use {@link #getShakeTime()} */
    public int getLeftShakeTime(){
        if (isShaking)
            return time;
        else
            return 0;
    }

    /** is this effect currently active */
    public boolean isShaking(){
        return isShaking;
    }

    // keitimui

    /** millis time in seconds. Default uses Engine's method {@link Engine#millis()}.
     * Override this if you need other time counting. */
    protected int getMillisTime(){
        return Engine.getInstance().millis();
    }

    // working.

    /** @param down where should it start on yAxis. down or up.
     *  @param isLeft where should it start on xAxis. right or left.*/
    public void start(boolean down, boolean isLeft){
        if (!xAxis && !yAxis){
            return; // ner ka pradet.
        }
        isShaking = true;
        TopPainter.addPaintOnTop(this);
        lastTime = getMillisTime();
        time = shakeTime;
        intensityTime = 0; // kad iskart reaguotu.

        // apverciam nes jei bus down true, tai leks i virsu tada, o pagal para mes norim i apacia etc.
        isDown = !down;
        this.isLeft = !isLeft;
        firstTime = true;
    }

    /** @param down where should it start: up or down. For xAxis: left or right. */
    public void start(boolean down){
        start(down, down);
    }

    public void start(){
        start(isDown, isLeft); // naudojam paliktus senus.
    }

    /** Continue to shake if there is still time left. */
    public void resumeShaking(){
        if (time > 0){
            isShaking = true;
            intensityTime = 0;

            lastTime = getMillisTime();

            firstTime = true;
            TopPainter.addPaintOnTop(this);
        }
    }

    public void stop(){
        if (!firstTime){
            // Turejo bent kazka padaryt.
            // dabar atstatom camera ten kur turi but.
            jumpCamera(shakeStrength/2); // atsistatys pati.
        }
        isShaking = false;
//        TopPainter.removeTopPaint(this); si metoda naudosim drope. Negalim liest saraso drope.
    }

    private void jumpCamera(float jump){
        float x = 0, y = 0;
        // ziurim xAxi
        if (xAxis){
            // jeigu kairej, tai varysim i desine
            // jei desinej, tai i kaire.
            x = isLeft ? jump : -jump;
            isLeft = !isLeft;
        }

        // ziurim yAxi
        if (yAxis){
            y = isDown ? jump : -jump; // ta pati sistema kaip su xAxim.
            isDown = !isDown; // apverciam.
        }

        // po visko atnaujinam viska i kamera.
        camera.position.set(camera.position.x + x, camera.position.y + y, 0);
        camera.update();
    }

    @Override
    public void draw() {
        if (isShaking){
            int clock = getMillisTime();
            time -= clock - lastTime; // atimam kiek praejo. Sitaip perdaryta, kad butu galima nutraukt ir test shakinima lyg niekur nieko.
            lastTime = clock; // pasizymim koks dabar.
            if (time <= 0){ // visks. baigiam efekta
                stop();
                return;
            }
            // laiko dar yra, bet paziurim ar galim kreipt kamera
            if (clock >= intensityTime){
                // galim kreip kamera. Perstatom laika is naujo.
                intensityTime = clock + shakeIntensity;
            }else { // dar negalim judint. Laukiam.
                return;
            }
            // jei atejo iki cia shakinam toliau.
            // surandam kokio suolio dydzio reiks.
            float jump;
            if (firstTime){ // pirma kart per puse.
                jump = shakeStrength /2;
                firstTime = false;
            }else { // toliau normaliai
                jump = shakeStrength;
            }

            jumpCamera(jump);
        }else { // is not shaking? Metam is sarasu.
            TopPainter.removeTopPaint(this);
        }
    }

    @Override
    public boolean drop(int reason) {
        if (reason <= dropReason){
            stop(); // drop atveju reik kompensuot ta puse suolio, kuri padare. Grazint kamera i vieta. Cia tai ir atliksim.
            return true;
        }
        return false;
    }
}
