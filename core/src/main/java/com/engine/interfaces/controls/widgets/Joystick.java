package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.animations.Counter;
import com.engine.core.MoreUtils;
import com.engine.interfaces.controls.Draggable;
import com.engine.root.GdxPongy;

/**
 * Joystick, touchscreenui. veiktu ir su pele, ir su mygtukais.
 */

public class Joystick extends Draggable {
    /* tiesiog zinojimui */
    private float strongX, strongY, angle;

    /* piesimui */
    private Drawable background, stick;
    private final Color tint;
    private float stickX, stickY;
    private float disappearTime;
    private Counter vanish;
    // skirti isimint sena alpha value, nes pati kontrole dings is vaizdo.
    private float oldAlpha, oldTint;
    private boolean show = true;

    /* veikimui. */
    private float speed;
    private final Rectangle fixedBounds;
    private final Vector2 tmp;
    private float offsetX, offsetY;

    /* ka galima */
    private boolean allowKeys;
    private boolean up, down, left, right;
    private boolean fixed, isVisible;

    private JoystickMovedListener listener;

    public Joystick(){
        this(new JoystickStyle());
    }

    public Joystick(JoystickStyle style){
        super(style);
        background = style.background;
        stick = style.stick;
        setStickSize(style.stickWidth, style.stickHeight);
        allowKeys = style.allowKeysInput;
        speed = MoreUtils.abs(style.maxSpeed);
        fixed = style.fixedPosition;
        setIdleVisible(style.isVisibleIdle);
        disappearTime = style.disappearTime;
        fixedBounds = new Rectangle();
        fixedBounds.set(style.bounds);
        tint = new Color();
        Color.argb8888ToColor(tint, style.tintBackground);
        oldTint = tint.a;
        oldAlpha = statusColor.a;
        tmp = new Vector2();
        if (!isVisible){
            show = false;
        }
        update = true;
    }

    /* para keitimas */

    public Joystick setBackground(Drawable e){
        if (e != null){
            background = e;
        }
        return this;
    }

    public Joystick setStickDrawable(Drawable e){
        if (e != null)
            stick = e;
        return this;
    }

    /** size in 0-1 format. */
    public Joystick setStickSize(float xRatio, float yRatio){
        stickX = MoreUtils.inBounds(xRatio, 0, 1);
        stickY = MoreUtils.inBounds(yRatio, 0, 1);
        return this;
    }

    /** time in seconds. */
    public Joystick setDisappearTime(float time){
        disappearTime = time;
        return this;
    }

    /** speed to calculate. */
    public Joystick setMaxSpeed(float speed){
        this.speed = MoreUtils.abs(speed);
        return this;
    }

    /** if position is not fixed, joystick will move in those bounds. */
    public Joystick setMovingBounds(float x, float y, float width, float height){
        fixedBounds.set(x, y, width, height);
        return this;
    }

    public Joystick enableKeyInput(boolean allow){
        allowKeys = allow;
        return this;
    }

    /** should joystick change its position or not. */
    public Joystick setFixedPosition(boolean fixed){
        this.fixed = fixed;
        return this;
    }

    /** should joystick disappear while not in use. */
    public Joystick setIdleVisible(boolean visible){
        isVisible = visible;
//        if (isOpen != old){
        if (!isVisible){
            if (vanish != null) {
//                MoreUtils.freeCounter(vanish);
                vanish.reset(); // kam dabar ji ten kist, o po to vel pasiimt?
            }else
                vanish = MoreUtils.getCounter();
            vanish.setUninterruptible(true);
            vanish.setCounterInformer(new Counter.CounterInformer() {
                @Override
                public void update(float oldValue, float currentValue) {
                    if (currentValue == 0) {
                        show = false;
                        statusColor.a = oldAlpha;
                        tint.a = oldTint;
                    }
                    statusColor.a = currentValue * oldAlpha;
                    tint.a = currentValue * oldTint;
                }
            });
        }else {
            MoreUtils.freeCounter(vanish);
            vanish = null;
        }
//        }
        return this;
    }

    /** ARGB format */
    public void tintBackground(int color){
        Color.argb8888ToColor(tint, color);
        oldTint = tint.a;
    }

    /* busena */

    public boolean isIdleVisible(){
        return isVisible;
    }

    public boolean isFixedPosition(){
        return fixed;
    }

    public boolean isKeyInputEnabled(){
        return allowKeys;
    }

    public float getMaxSpeed(){
        return speed;
    }

    public float getDisappearTime(){
        return disappearTime;
    }

    public Drawable getBackground(){
        return background;
    }

    public Drawable getStickDrawable(){
        return stick;
    }

    public float getStickX(){
        return stickX;
    }

    public float getStickY(){
        return stickY;
    }

    public Rectangle getMovingBounds(){
        return fixedBounds;
    }

    public Vector2 getDestination(){
        return tmp;
    }

    public int getTintColor(){
        return Color.argb8888(tint);
    }

    public float getStrengthX(){
        return strongX;
    }

    public float getStrengthY(){
        return strongY;
    }

    public float getAngle(){
        return angle;
    }

    /* override */

    @Override
    protected void onDragging(float x, float y, float deltaX, float deltaY) {
        Vector2 pos = getPosition();
        float mx = pos.x + xSize/2, my = pos.y + ySize/2, maxDist = Math.min(xSize/2, ySize/2);
        float dist = Math.min(MoreUtils.dist(x, y, mx, my), maxDist);
        float angle = MathUtils.atan2(y - my, x - mx);
        calculate(angle, dist, maxDist);
    }

    private void calculate(float angle, float dist, float maxDist){
        if (dist < MathUtils.FLOAT_ROUNDING_ERROR){
            offsetX = 0;
            offsetY = 0;
            tmp.set(0, 0);
            this.strongY = 0;
            this.strongX = 0;
            return;
        }
        float strongX, strongY;
        float px, py;
        px = MathUtils.cos(angle) * dist;
        py = MathUtils.sin(angle) * dist;
        strongX = px / maxDist;
        strongY = py / maxDist;
        offsetX = px;
        offsetY = py;
        tmp.set(speed * strongX, speed * strongY);
        this.strongY = strongY;
        this.strongX = strongX;
        this.angle = angle * MathUtils.radiansToDegrees;
    }

    @Override
    protected void onDrop(float x, float y, float deltaX, float deltaY) {}

//    @Override
//    protected void tint(float value) {
//        tint.a = oldTint * value;
//    }

    @Override
    protected void isvaizda(float x, float y) {
        if (show) {
            p.tint(tint);
            drawDrawable(background, x, y, false);
            p.tint(statusColor);
            float stickWidth = xSize * stickX, stickHeight = ySize * stickY;
            float sx = x + xSize / 2 - stickWidth / 2 + offsetX, sy = y + ySize / 2 - stickHeight / 2 + offsetY;
            drawDrawable(stick, sx, sy, stickWidth, stickHeight, false);
            p.noTint();
            if (listener != null) {
                if (strongX != 0 || strongY != 0) {
                    listener.move(tmp, strongX, strongY, angle);
                }
            }
            /* test. */
//            p.noFill();
//            p.stroke(0);
//            float max = Math.min(nxSize, nySize);
//            p.ellipse(x + nxSize/2, ny + nySize/2, max, max);
        }
    }

    @Override
    protected void autoSize() {
        if (background == null){
            setVisible(false);
//            System.out.println("Joystick: No drawable found.");
            Gdx.app.log("Joystick", "No drawable found.");
            return;
        }
        xSize = ySize = Math.min(background.getMinWidth(), background.getMinHeight());
        if (xSize == 0){
            setVisible(false);
//            System.out.println("Size cannot be 0");
            Gdx.app.log("Joystick", "Size cannot be 0");
        }
//        sizeUpdated();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (!fixed && isEnabled() && !isPressed() && pointer == 0){
            if (x > fixedBounds.x && x < fixedBounds.x + fixedBounds.width &&
                    y > fixedBounds.y && y < fixedBounds.y + fixedBounds.height){
                setPosition(x - xSize/2, y - ySize/2); // turetu pernest ir padaryt taip, kad viduri pirsto.
            }
        }
        return super.touchDown(x, y, pointer, button);
    }

    @Override
    protected void onPress(float x, float y) {
        super.onPress(x, y);
        joystickActive();
    }

    private void joystickActive(){
        show = true;
        if (vanish != null){
            vanish.cancel();
            statusColor.a = oldAlpha;
            tint.a = oldTint;
        }
    }

    private void joystickIdle(){
        offsetX = 0;
        offsetY = 0;
        tmp.set(0, 0);
        strongX = 0;
        strongY = 0;
        if (!isVisible){
            if (disappearTime > 0) {
                oldAlpha = statusColor.a;
                vanish.startCount(1, 0, disappearTime);
            }else {
                show = false;
            }
        }
    }

    @Override
    protected void onRelease(float x, float y) {
        super.onRelease(x, y);
        joystickIdle();
    }

    @Override
    protected void onRemove() {
        if (vanish != null){
            MoreUtils.freeCounter(vanish);
            vanish = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (vanish != null){
            MoreUtils.freeCounter(vanish);
            vanish = null;
        }
    }

    /* cenais tik su allow key input. */

    @Override
    public boolean keyDown(int keycode) {
        if (super.keyDown(keycode))
            return true;
        if (allowKeys){
            float angle = 0;
            boolean press = false;
            if (Input.Keys.UP == keycode){
                joystickActive();
                up = true;
                press = true;
                if (down){
                    down = false;
                }
                if (left){
                    angle = 135f;
                }else if (right){
                    angle = 45f;
                }else {
                    angle = 90f;
                }
            }else if (Input.Keys.DOWN == keycode){
                joystickActive();
                down = true;
                press = true;
                if (up){
                    up = false;
                }
                if (left){
                    angle = -135f;
                }else if (right){
                    angle = -45f;
                }else {
                    angle = -90f;
                }
            }else if (Input.Keys.LEFT == keycode){
                joystickActive();
                left = true;
                press = true;
                if (right){
                    right = false;
                }
                if (up){
                    angle = 135f;
                }else if (down){
                    angle = -135f;
                }else {
                    angle = 180f;
                }
            }else if (Input.Keys.RIGHT == keycode){
                joystickActive();
                right = true;
                press = true;
                if (left){
                    left = false;
                }
                if (up){
                    angle = 45f;
                }else if (down){
                    angle = -45f;
                }else {
                    angle = 0f;
                }
            }
            if (press){
                float max = Math.min(xSize/2, ySize/2);
                calculate(angle * MathUtils.degreesToRadians, max, max);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (super.keyUp(keycode))
            return true;
        if (allowKeys){
            float angle = 0;
            boolean press = false;
            if (Input.Keys.UP == keycode){
                joystickActive();
                up = false;
                press = true;
                if (down){
                    down = false;
                }
                if (left){
                    angle = 180f;
                }else if (right){
                    angle = 0f;
                }else {
                    joystickIdle();
                    return true;
                }
            }else if (Input.Keys.DOWN == keycode){
                joystickActive();
                down = false;
                press = true;
                if (up){
                    up = false;
                }
                if (left){
                    angle = 180f;
                }else if (right){
                    angle = 0f;
                }else {
                    joystickIdle();
                    return true;
                }
            }else if (Input.Keys.LEFT == keycode){
                joystickActive();
                left = false;
                press = true;
                if (right){
                    right = false;
                }
                if (up){
                    angle = 90f;
                }else if (down){
                    angle = -90f;
                }else {
                    joystickIdle();
                    return true;
                }
            }else if (Input.Keys.RIGHT == keycode){
                joystickActive();
                right = false;
                if (left){
                    left = false;
                }
                if (up){
                    angle = 90f;
                }else if (down){
                    angle = -90f;
                }else {
                    joystickIdle();
                    return true;
                }
            }
            if (press){
                float max = Math.min(xSize/2, ySize/2);
                calculate(angle * MathUtils.degreesToRadians, max, max);
                return true;
            }
        }
        return false;
    }

    /* listener */

    public void setJoystickMoveListener(JoystickMovedListener e){
        listener = e;
    }

    public interface JoystickMovedListener{
        /** @param strengthX 0-1. 0 - not moving, 1 - max moving.
         *  @param destination judejimo kryptis pagal nurodyta judejimo greitį.
         *  @param angle judėjimo kryptis degrees.*/
        void move(Vector2 destination, float strengthX, float strengthY, float angle);
    }

    /* style */

    @Override
    public JoystickStyle getStyle(){
        JoystickStyle e = new JoystickStyle();
        copyStyle(e);
        return e;
    }

    public void copyStyle(JoystickStyle st){
        super.copyStyle(st);
        st.background = getBackground();
        st.stick = getStickDrawable();
        st.tintBackground = getTintColor();
        st.stickWidth = getStickX();
        st.stickHeight = getStickY();
        st.allowKeysInput = isKeyInputEnabled();
        st.maxSpeed = getMaxSpeed();
        st.fixedPosition = isFixedPosition();
        st.isVisibleIdle = isIdleVisible();
        st.disappearTime = getDisappearTime();
        st.bounds.set(getMovingBounds());
    }

    public void readStyle(JoystickStyle st){
        super.readStyle(st);
        setBackground(st.background);
        setStickDrawable(st.stick);
        tintBackground(st.tintBackground);
        setStickSize(st.stickWidth, st.stickHeight);
        allowKeys = st.allowKeysInput;
        setMaxSpeed(st.maxSpeed);
        setFixedPosition(st.fixedPosition);
        setIdleVisible(st.isVisibleIdle);
        setDisappearTime(st.disappearTime);
        setMovingBounds(st.bounds.x, st.bounds.y, st.bounds.width, st.bounds.height);
    }

    public static class JoystickStyle extends ClickableStyle{
        public Drawable background, stick;
        public int tintBackground = 0xFFFFFFFF;
        /** stick size in format 0-1 */
        public float stickWidth = 0.33f, stickHeight = 0.33f;
        public boolean allowKeysInput = true;
        /** moving forward speed. */
        public float maxSpeed = 1;
        /** ar joystickui likt toj pacioj pozicijoj (true) ar sekt pirsto paspaudima (false). */
        public boolean fixedPosition = true;
        /** kai nera paspausta, joystick bus nematomas. */
        public boolean isVisibleIdle = true;
        /** if <code>isVisibleIdle</code> is false then after joystick enters idle mode, he will start disappearing.
         * time in seconds. */
        public float disappearTime = 1;
        /** if position is not fixed, this area will be used as joysticks jumping area (finger following). */
        public final Rectangle bounds = new Rectangle(0, 0, GdxPongy.getInstance().getScreenWidth(), GdxPongy.getInstance().getScreenHeight());

        @Override
        public Joystick createInterface() {
            return new Joystick(this);
        }
    }
}
