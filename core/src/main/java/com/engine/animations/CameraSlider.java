package com.engine.animations;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.engine.core.Engine;
import com.engine.core.ErrorMenu;
import com.engine.core.MoreUtils;
import com.engine.ui.controls.TopPainter;
import com.engine.ui.listeners.MainDraw;

/**
 * This class enables easy way to move camera through world. It can follow a point ( {@link Vector2} instance). Move to given point or just move to
 * given direction. Bounds can be set to avoid camera crossing it. This class does not teleport camera, camera moves through world.
 */
public class CameraSlider implements MainDraw{
    /*
    Ribos neleidziancios iseit kamerai is ribu
    Judejimas i nurodyta taska
    Judejimas nurodyta kryptim (Reiks stabdymo funkcijos)
    Tasko sekiojimas (Vector2 tasko cord)
     */
    // judinama camera.
    private OrthographicCamera camera;
    private final Vector2 c; // kameros koordinates.

    // ribos.
    private boolean hasBounds;
    private Rectangle bounds;
//    private boolean lockXAxis = false; // sitie leis uzrakint kameros judejima per tam tikra koordinaciu asi.
//    private boolean lockYAxis = false; // patogu, jei zaidimas turi keliaut tik viena kryptim. pvz tik x asim.

    // nurodyto tasko judejimas arba sekiojimas.
    private final Vector2 defaultPoint;
    private Vector2 point;
    private Rectangle followPointBounds;
    private float offsetX, offsetY;
    private float stoppingRadius = 0f;

    // pacio judejimo par
    private float speed, angle;
    private final float timeFrames = 60f; // Why? Why you need this??
    private boolean isMoving;
    private float accelerationTime = 300f;
    private float acceleration;

    // 0 - taskas, 1 - sekioja, 2 - juda tiesiog.
    private int movingStatus;

    /** default abosule camera and default speed: 20 */
    public CameraSlider(){
        this(Engine.getInstance().getAbsoluteCamera(), 20); // paims default kamerą.
    }

    public CameraSlider(OrthographicCamera cam, float speed){
        camera = cam;
//        accelerator = new Counter();
        defaultPoint = new Vector2();
        c = new Vector2();
        Engine p = Engine.getInstance();
        if (camera == null){
            p.setError("CameraSlider: Camera cannot be null", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        bounds = new Rectangle();
        float halfw = p.getScreenWidth()/2;
        float halfh = p.getScreenHeight()/2;
        followPointBounds = new Rectangle(halfw/2, halfh/2, halfw, halfh);
        point = defaultPoint; // kad nebutu null pointer exception
        this.speed = speed * timeFrames;
    }

    /* Klases valdymo metodai ir parametru gavimo metodai */

    /** when following a point and camera is by that radius of following point than slider will began to slowdown (it will not stop at point
     * it just slows down). You should use this when you have disabled follow point bounds.
     * Set to 0 to disable it.*/
    public void setFollowPointRadius(float radius){
        stoppingRadius = MoreUtils.abs(radius);
    }

    /** When following a point or moving to point camera will stop at middle of the point.
     * If you do not want that camera stops at middle you can set offset of how much you want camera to stop away from point.*/
    public void setPointOffset(float x, float y){
        offsetX = x;
        offsetY = y;
    }

    /** sets time in which camera accelerates to it's full speed.
     * Lower value faster it accelerates. 0 - means full speed immediately.
     * Number with negative value will be converted to positive.
     * NOTE: Low value on following point may produce flickering. Use atleast 120 to avoid filckering when following a point with a follow bounds.
     * Bigger value smoother it goes but also bigger value slower it accelerates.*/
    public void setAccelerationTime(float time){
        accelerationTime = MoreUtils.abs(time);
        if (accelerationTime == 0){
            acceleration = 1f; // statiskai 1 ir viskas.
        }
    }

    /** sets max moving speed of camera. */
    public CameraSlider setMaxMovingSpeed(float speed){
        this.speed = speed * timeFrames;
        return this;
    }

    /** follows {@link Vector2} instance point. If there is bounds and point appears outside bounds camera will not
     * cross bounds it will stay next to bounds. */
    public void followPoint(Vector2 point){
        this.point = point;
        defaultPoint.set(point);
//        isFollowing = true;
        movingStatus = 1;
        startMoving();
    }

    /** stops any camera moving. */
    public void stop(){
        isMoving = false;
    }

    /** Moves camera to given direction. Camera will not stop it needs to be stopped manually unless there is bounds.
     * If there is bounds camera will stop when it hits bounds */
    public void moveTo(float angleDegrees){
        startMoving();
//        isFollowing = false;
        movingStatus = 2;
        angle = angleDegrees*MathUtils.degreesToRadians;
    }

    /** Moves camera to given point. If point is outside bounds camera will stop at the end of the bounds. */
    public void moveToPoint(Vector2 e){
        moveToPoint(e.x, e.y);
    }

    /** Moves camera to given point. If point is outside bounds camera will stop at the end of the bounds. */
    public void moveToPoint(float x, float y){
        point = defaultPoint;
        point.set(x, y);
//        isFollowing = false;
        movingStatus = 0;
        startMoving();
    }

    /** Bounds which camera cannot cross.
     * set width and height to zero to disable bounds.
     * For example setting width to 0 and height to positive value will bring x axis without bounds but
     * y axis with given bounds.
     * Setting width or height to less then camera viewport width or height will bring x or y axis lock.
     * for example width = 0, height = 1 - x axis is free to move and y axis is locked and camera will
     * not move through y axis.
     * width and height with negative value will be treated as positive value. */
    public CameraSlider setCameraBounds(Rectangle e){
        return setCameraBounds(e.x, e.y, e.width, e.height);
    }

    /** Bounds which camera cannot cross.
     * set width and height to zero to disable bounds.
     * For example setting width to 0 and height to positive value will bring x axis without bounds but
     * y axis with given bounds.
     * Setting width or height to less then camera viewport width or height will bring x or y axis lock,
     * for example width = 0, height = 1 - x axis is free to move and y axis is locked and camera will
     * not move through y axis.
     * width and height with negative value will be treated as positive value. */
    public CameraSlider setCameraBounds(float x, float y, float width, float height){
        if (width == 0 && height == 0){
            hasBounds = false;
        }else {
            hasBounds = true;
            float bwidth = MoreUtils.abs(width);
            float bheight = MoreUtils.abs(height);
            if (width != 0 && bwidth < camera.viewportWidth){
                bwidth = camera.viewportWidth;
            }
            if (height != 0 && bheight < camera.viewportHeight){
                bheight = camera.viewportHeight;
            }
            bounds.set(x, y, bwidth, bheight);
        }
        return this;
    }

    /** bounds in visible screen (0 - screenWidth)(0 - screenHeight).
     * If a point, which is followed, is in those bounds camera will not move until point is outside those bounds.
     * set width or height to 0 or negative value to disable those bounds.*/
    public void setFollowPointBounds(float x, float y, float width, float height){
        Engine p = Engine.getInstance();
        if (x >= 0 && y >= 0 && width <= p.getScreenWidth() && height <= p.getScreenHeight()){ // turi itilpt i matoma ekrana.
            followPointBounds.set(x, y, width,height);
        }
    }

    public float getFollowPointRadius(){
        return stoppingRadius;
    }

    /** offset of how much camera stops at point. */
    public float getOffsetX(){
        return offsetX;
    }

    /** offset of how much camera stops at point. */
    public float getOffsetY(){
        return offsetY;
    }

    /** @return camera which is moving */
    public OrthographicCamera getCamera(){
        return camera;
    }

    /** max speed of this camera. */
    public float getMovingSpeed(){
        return speed / timeFrames;
    }

    /** @return jump or follow point (depends if jump or follow point is/was set)*/
    public Vector2 getPoint() {
        return point;
    }

    /** @return if camera is moving or following a point right now. */
    public boolean isMoving(){
        return isMoving;
    }

    /** set camera to control. */
    public void setCamera(OrthographicCamera camera){
        if (camera != null)
            this.camera = camera;
    }

    /* veikimo metodai */

    /** sets acceleration of this camera to 0. Camera will have to accelerate again. */
    public void resetAcceleration(){
        acceleration = 0;
    }

    private void countMovingAngle(){
        angle = MathUtils.atan2((point.y+offsetY) - camera.position.y, (point.x+offsetX) - camera.position.x);
//        System.out.println(angle*MathUtils.radiansToDegrees);
    }

    private void startMoving(){
        isMoving = true;
        TopPainter.addPaintOnTop(this);
        countMovingAngle();
    }

    /** bounds - rects bounds. else ellipse bounds. */
    private void slowDown(boolean bounds){ // TODO prastai atrodo. Reiktu perdaryt stabdyma.
        if (accelerationTime != 0 && acceleration > 0) {
            acceleration -= 1 / accelerationTime; // letinam.
//                return;
            if (acceleration <= 0) {
                acceleration = 0;
            } else {
                acceleration /= 1.25f;
//                float pointX = point.x + offsetX, pointY = point.y + offsetY;
                float speed = this.speed * Engine.getDelta() * acceleration;
                float x = MathUtils.cos(angle) * speed;
                float y = MathUtils.sin(angle) * speed;
//                float cx, cy;

//                if (MoreUtils.abs(pointX - camera.position.x) < speed) { // kad neprasoktu.
////                    c.x = pointX;
//                } else {
                    c.x = camera.position.x + x;
//
//                }
//                if (MoreUtils.abs(pointY - camera.position.y) < speed) {
////                    c.y = pointY;
//                    acceleration /= 2;
//                } else {
                    c.y = camera.position.y + y;
//                }

//                c.x = camera.position.x + x; // negaudom tasko. Teritorijoj tegul buna.
//                c.y = camera.position.y + y;
                checkBounds();
                camera.position.x = c.x;
                camera.position.y = c.y;
                camera.update();
            }
        }
    }

    private void accelerate(){
        if (acceleration < 1) {
            if (accelerationTime != 0) {
                acceleration += 1 / accelerationTime;
                if (acceleration > 1)
                    acceleration = 1;
            }else {
                acceleration = 1f;
            }
        }
    }

    private void checkBounds(){
        if (hasBounds){ // ar neiseina is ribu.
            float difX = camera.viewportWidth/2 * camera.zoom;
            float difY = camera.viewportHeight/2 * camera.zoom;
            if (bounds.width > 0) { // nustatom ar turi plocio apribojimus.
                if (camera.viewportWidth * camera.zoom > bounds.width){
                    c.x = bounds.x + difX;
                }else {
                    if (c.x - difX < bounds.x) {
                        c.x = bounds.x + difX;
                    } else if (c.x + difX > bounds.x + bounds.width) {
                        c.x = bounds.x + bounds.width - difX;
                    }
                }
            }
            if (bounds.height > 0) { // ar turi ilgio apribojimus.
                if (camera.viewportHeight * camera.zoom > bounds.height){
                    c.y = bounds.y + difY;
                }else {
                    if (c.y - difY < bounds.y) {
                        c.y = bounds.y + difY;
                    } else if (c.y + difY > bounds.y + bounds.height) {
                        c.y = bounds.y + bounds.height - difY;
                    }
                }
            }
        }
    }

    /* implemented metodai */

    @Override
    public void draw() {
        if (isMoving){
//            boolean moved = false;
            float pointX = point.x + offsetX, pointY = point.y + offsetY;
            if (movingStatus == 1){ // jeigu sekioja taska reik perskaiciuot angle.
                float x, y, width, height;
                x = camera.position.x - (camera.viewportWidth/2)*camera.zoom + followPointBounds.x;
                y = camera.position.y - (camera.viewportHeight/2)*camera.zoom + followPointBounds.y;
                width = followPointBounds.width;
                height = followPointBounds.height;
                if (pointX >= x && pointX <= x+width
                        && pointY >= y && pointY <= y+height){ // taskas yra teritorijoj kur nereik judint kameros.
                    slowDown(true); // letinam.
                    return;
                }
                if (defaultPoint.x != point.x || defaultPoint.y != point.y){ // taskas pakeite vieta, reik perskaiciuot.
                    countMovingAngle();
                    defaultPoint.set(point);
                }
            }
            accelerate(); // tiesiog pagreitinam ir tiek.
            float speed = this.speed * Engine.getDelta() * acceleration;
            float x = MathUtils.cos(angle) * speed;
            float y = MathUtils.sin(angle) * speed;
            if (movingStatus < 2) { // juda i taska. kur sekioja nesvarbu.
//                speed = speed * acceleration;
//                boolean slowDown = false;
                if (movingStatus == 1){
                    float distance = MoreUtils.dist(pointX, pointY, camera.position.x, camera.position.y);
                    if (distance < stoppingRadius){
                        slowDown(false);
                        return;
                    }
                }
                if (MoreUtils.abs(pointX - camera.position.x) < speed) { // kad neprasoktu.  movingStatus != 1 sukelia flickering buga, kai ner bounds ir stopping radius.
//                    if (movingStatus == 1){ // jeigu sekioja, tai geriau tegul leteja, o ne su smugiu sustoja.
////                        slowDown(); // o, jeigu y asi atsitraukes toli?
//                        slowDown = true;
////                        return;
//                    }else
                        c.x = pointX;
                } else {
                    c.x = camera.position.x + x;
                }
                if (MoreUtils.abs(pointY - camera.position.y) < speed) {
//                    if (movingStatus == 1 && slowDown) {
//                        slowDown();
//                        return;
//                    }else
                        c.y = pointY;
                } else {
                    c.y = camera.position.y + y;
                }
                // jeigu tiesiog juda arba turi sekiot taska, tai stabdyt nereik, tai judam tolyn.
                if (movingStatus == 0 && c.x == pointX && c.y == pointY){ // tikslas pasiektas.
                    isMoving = false; // vistiek reik po sito bounds patikrint.
                }
            }else {
                c.x = camera.position.x + x;
                c.y = camera.position.y + y;
            }
            checkBounds(); // patikrinam.
            // camera nepajudejo. (pvz pastrigo tarp kampu ir nebejuda) jei sekioja, tai gali stovet vietoj.
            if (camera.position.x == c.x && camera.position.y == c.y){
                if (movingStatus != 1) { // nesekioja tasko.
                    isMoving = false;
                    acceleration = 0; // atsitrenke. stabdom viska.
                }
            }else { // camera pajudejo
                camera.position.x = c.x;
                camera.position.y = c.y;
                camera.update();
            }
        }else {
            TopPainter.removeTopPaint(this);
            if (accelerationTime != 0)
                acceleration = 0; // nuresetinam, nes saus i prieki.
        }
    }

    @Override
    public boolean drop(int reason) {
        isMoving = false;
        if (accelerationTime != 0)
            acceleration = 0; // nuresetinam, nes saus i prieki.
        return false;
    }
}
