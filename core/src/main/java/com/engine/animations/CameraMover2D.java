package com.engine.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.engine.core.Engine;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.listeners.MainDraw;

/** Update to {@link CameraSlider}. Reworked inside workings to be more efficient.
 * This class enables animated camera movement. It can:
 * -Go to a point smoothly.
 * -Follow a point.
 * -Just move to a given direction.
 * All moving is smooth and no teleporting. Only moving through x and y axis.*/
public class CameraMover2D implements MainDraw {
    private OrthographicCamera camera;

    /* Stats for camera. Can be modified by user. */
    // Maximum speed.
    private float maxSpeed;

    // Time which needs to get to full speed.
    private float accelerationTime;

    // Bounds which camera cannot cross.
    private boolean hasBounds;
    private final Rectangle allowedBounds;
    private boolean lockXaxis, lockYaxis;

    // Point to follow. Instance passed by user.
    private Vector2 followPoint;
    // Point could be treated as circle.
    private float pointRadius;

    // Camera offset.
    private float offsetX, offsetY;


    /* Current parameters for moving camera. */

    // Which method of move is used
    private CameraMoveStyle cameraMoveStyle;

    // Is camera moving right now. To determine if it should move.
    // Camera could be still, but if it follows point than it should
    // monitor that.
    private boolean isMoving;

    // Camera's current speed.
//    private float currentSpeed;
    private final Vector2 speed = new Vector2();
    private float angleRad; // where camera is heading to.

    // to manipulate numbers of camera before pushing them to camera itself.
    private final Vector2 cameraCords = new Vector2();
    // Counting how many times camera did not move. Stopping after some tries.
    // Will be used only in simple move method.
    private int cameraNotMovedCounter;


    /** Creates instance with default Engine absolute camera.
     * Default speed: 20
     * default acceleration time: 0.5 second.
     * default bounds disabled.*/
    public CameraMover2D(){
        this(Engine.getInstance().getAbsoluteCamera());
    }

    public CameraMover2D(OrthographicCamera camera){
        if (camera == null){
            this.camera = Engine.getInstance().getAbsoluteCamera();
        }else{
            this.camera = camera;
        }

        if (camera == null)
            throw new NullPointerException("Camera instance cannot be null");

        // Some default parameters.
        maxSpeed = 20f;
        accelerationTime = 0.5f;

        allowedBounds = new Rectangle();
        cameraCords.set(camera.position.x, camera.position.y);
    }

    /* Camera handling here. */

    /** Checking if camera is inside bounds.
     * If bounds are disabled than does nothing.
     * if outside bounds, than it will correct cameraCord vector.
     * This method is called inside {@link CameraMover2D#pushToCamera()}
     * */
    private void checkBounds(){
        if (hasBounds){

        }
    }

    // Pushing new cords to camera. Real moving happens here.
    private boolean pushToCamera(){
        checkBounds();
        if (cameraCords.x == camera.position.x && cameraCords.y == camera.position.y
            || lockXaxis && lockYaxis){
            return false; // Nothing changed or both axis are locked.
        }

        // Apply new cords to camera.
        if (!lockXaxis)
            camera.position.x = cameraCords.x;
        else
            cameraCords.x = camera.position.x; // reset camera position. It cannot change.

        if (!lockYaxis)
            camera.position.y = cameraCords.y;
        else
            cameraCords.y = camera.position.y; // reset camera position. It cannot change.

        // And now the magic.
        camera.update();
        return true;
    }

    // User gave direction, so camera will move to that direction.
    private void moveInDirection(){
        if (speed.isZero()){ // First time.
            if (accelerationTime > 0) {
                // start the move.
                float startSpeed = Gdx.app.getGraphics().getDeltaTime() / accelerationTime;
                if (startSpeed > maxSpeed) // We should not move faster than max speed.
                    startSpeed = maxSpeed;

                speed.set(startSpeed, 0); // Doesn't matter. Just giving the speed.
                speed.setAngleRad(angleRad); // Now it fixes the angle.
            }else {
                // Just go full speed.
                speed.set(maxSpeed, 0);
                speed.setAngleRad(angleRad);
            }
        }else if (speed.len2() < maxSpeed*maxSpeed){ // It kinda is faster than len. No point in root.
            if (accelerationTime > 0) {
                // I tried to avoid using Math.sqrt in if statement, but here using it two times.
                float oldSpeed = speed.len();
                float newSpeed = oldSpeed + Gdx.app.getGraphics().getDeltaTime() / accelerationTime;

                if (newSpeed > maxSpeed) // Not going faster than max speed.
                    newSpeed = maxSpeed;

                speed.setLength(newSpeed);
            }else {
                // Something went wrong. It was not going full speed.
                // Resetting speed to max.
                speed.setLength(maxSpeed);
            }
        }

        // Now try to move the camera.
        cameraCords.add(speed); // Before applying to camera here to see how it would look.
        if (pushToCamera()) { // Try to update.
            cameraNotMovedCounter = 0; // Always resetting on success...
        }else {
            cameraNotMovedCounter++;
        }

        if (cameraNotMovedCounter > 10){
            // To many times camera did not move.
            // Most likely stuck in bounds.
            // Stopping camera.
            // Hard stop as anyway it did not move.
            isMoving = false;
        }
    }

    private void moveToPoint(){

    }

    private void followPoint(){

    }


    // This method is called at the end of each timeframe. Here updating camera pos, speed etc...
    @Override
    public void draw() {
        if (isMoving){
            switch (cameraMoveStyle){
                case MoveToPoint:
                    moveToPoint();
                    break;
                case FollowPoint:
                    followPoint();
                    break;
                case MoveInDirection:
                    moveInDirection();
                    break;
                case SlowDown: // Simply slow down camera. And stop.
                    // If no acceleration was used, than just stop. Nothing more.
                    // Using speed len2 as it is faster. If speed is 0, it is
                    // 0, so no point in root calculations...
                    if (accelerationTime == 0 || speed.len2() == 0){
                        isMoving = false;
                    }else {
                        // TODO stop the camera.
                    }
                    break;
            }
        }else {
            TopPainter.removeTopPaint(this);
            speed.setZero(); // Remove any speed, camera is not moving anymore.
        }
    }

    // Any drop will stop camera. It could be form change, PopUp show up or user called drop.
    @Override
    public boolean drop(int reason) {
        stop(true);
        return false;
    }

    /* Camera Mover control */

    /** Stops camera.
     * @param hardStop should camera slowdown or stop immediately. */
    public void stop(boolean hardStop){
        if (isMoving) {
            if (hardStop) {
                speed.setZero();
                isMoving = false;
            } else {
                // Starting slow down process.
                cameraMoveStyle = CameraMoveStyle.SlowDown;
            }
        }
    }

    /** Moves camera to this point. */
    public void moveTo(Vector2 point){
        moveTo(point.x, point.y);
    }

    /** Moves camera to this point. */
    public void moveTo(float x, float y){
        // TODO do something...

        cameraMoveStyle = CameraMoveStyle.MoveToPoint;

    }

    public void followPoint(Vector2 point){
        followPoint(point, 0);
    }

    /** Camera will follow this point now. It will monitor it's location and will follow it if it changes.
     * @param point Point to follow.
     * @param radius Radius of which camera will not follow. 0 - disable radius. */
    public void followPoint(Vector2 point, float radius){
        if (point == null)
            throw new NullPointerException("Point cannot be null!");
        if (radius < 0)
            throw new IllegalArgumentException("Radius cannot be negative!");

        followPoint = point;
        pointRadius = radius;

        cameraMoveStyle = CameraMoveStyle.FollowPoint;

        // TODO start moving.
    }

    /** Camera will start moving to given direction. It will move until it is stopped.
     * To stop camera us {@link CameraMover2D#stop(boolean)} method. */
    public void move(float angleDegrees){
        if (lockXaxis && lockYaxis){
            Gdx.app.log("CameraMover2D", "Both axis are locked! Cannot move the camera!");
            return;
        }

        angleRad = angleDegrees * MathUtils.degreesToRadians;

        isMoving = true; // anyway it will have to move.
        // Resetting camera cords.
        // It might lose cords while this mover was not active.
        cameraCords.set(camera.position.x, camera.position.y);
        TopPainter.addPaintOnTop(this); // It will not add second time same instance.

        cameraMoveStyle = CameraMoveStyle.MoveInDirection;
    }



    /* Parameters change. */

    /** Camera point is in the middle of the screen. To offset that point use this.
     * For example: using negative x would offset camera left, while positive would offset to right.
     * Using negative y would offset camera to downside, while positive would offset to upside.
     * @param x Offset in x axis.
     * @param y Offset in y axis.  */
    public void setOffset(float x, float y){
        offsetX = x;
        offsetY = y;
    }

    /** @return camera offset in x axis. */
    public float getOffsetX(){
        return offsetX;
    }

    /** @return camera offset in y axis. */
    public float getOffsetY(){
        return offsetY;
    }

    /** Bounds which camera cannot cross.
     * NOTE: If camera is outside bounds, than it will be teleported inside bounds!
     * If width or height is smaller than camera's viewport width or height, than it will
     * lock camera's axis.
     * To remove bounds use {@link CameraMover2D#removeCameraBounds()}.*/
    public void setCameraBounds(float x, float y, float width, float height) {
        allowedBounds.set(x, y, width, height);
        hasBounds = true;
    }

    /** Bounds which camera cannot cross.
     * NOTE: If camera is outside bounds, than it will be teleported inside bounds!
     * If width or height is smaller than camera's viewport width or height, than it will
     * lock camera's axis.
     * To remove bounds use {@link CameraMover2D#removeCameraBounds()}.*/
    public void setCameraBounds(Rectangle bounds){
        if (bounds == null)
            throw new IllegalArgumentException("Bounds cannot be null!");
        allowedBounds.set(bounds);
        hasBounds = true;
    }

    /** Camera's bounds in which camera can move.
     * It could be disabled using {@link CameraMover2D#removeCameraBounds()}.
     * To re-enable bounds you need to reset bounds again.*/
    public Rectangle getCameraBounds(){
        return allowedBounds;
    }

    /** Removes camera's bounds. */
    public void removeCameraBounds(){
        hasBounds = false;
    }

    /** Lock x axis and camera will not move through x axis.
     * By default it is unlocked. */
    public void lockXaxis(boolean lock){
        lockXaxis = lock;
        checkAxisLock();
    }

    /** Lock y axis and camera will not move through y axis.
     * By default it is unlocked. */
    public void lockYaxis(boolean lock){
        lockYaxis = lock;
        checkAxisLock();
    }

    private void checkAxisLock(){
        if (lockXaxis && lockYaxis && isMoving){
            // Camera cannot move while both axis are locked.
            isMoving = false;
        }
    }

    /** @return  whether can camera move through x axis. */
    public boolean isXaxisLocked(){
        return lockXaxis;
    }

    /** @return whether can camera move through y axis */
    public boolean isYaxisLocked() {
        return lockYaxis;
    }

    /** Is camera active. It can follow point, move to a specified point or just move.
     * It can stay still while following a point if that point is in the center of the camera.
     * @return whether camera is active*/
    public boolean isCameraActive(){
        return isMoving;
    }

    /** Sets camera's acceleration time. Time must be positive.
     * This time is used to determine how much time camera needs to get to full speed.
     * Using 0 will disable acceleration. Camera will move at full speed.
     * @param timeInSeconds Time in seconds. */
    public void setAccelerationTime(float timeInSeconds){
        if (timeInSeconds < 0)
            throw new IllegalArgumentException("Time cannot be negative!");

        accelerationTime = timeInSeconds;
    }

    /** Camera's acceleration time in seconds. It determines how much
     * time camera needs to get to full speed. Using 0 will disable acceleration. */
    public float getAccelerationTime(){
        return accelerationTime;
    }

    /** Sets camera's maximum speed. Speed must be positive and not zero.
     * New speed might not apply if camera is already moving. Best to set speed
     * before starting any movement.
     * @param speed Maximum speed of camera. */
    public void setSpeed(float speed){
        if (speed <= 0)
            throw new IllegalArgumentException("Speed must be positive and not zero!");

        maxSpeed = speed;
    }

    /** Camera's maximum speed. */
    public float getSpeed(){
        return maxSpeed;
    }


    /** Sets camera to work with */
    public void setCamera(OrthographicCamera camera){
        if (camera == null)
            throw new NullPointerException("Camera instance cannot be null");
        if (this.camera == camera){
            Gdx.app.log("CameraMover2D", "Trying to assign same camera instance!");
            return;
        }
        // Stopping old camera before assigning new one.
        if (isMoving)
            stop(true);
        this.camera = camera;
        cameraCords.set(camera.position.x, camera.position.y);
    }

    /** @return Camera which is being controlled. */
    public OrthographicCamera getCamera(){
        return camera;
    }

    // This will only be used inside.
    private enum CameraMoveStyle{
        MoveToPoint,
        FollowPoint,
        MoveInDirection,
        SlowDown
    }
}
