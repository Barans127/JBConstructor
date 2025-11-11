package com.jbconstructor.editor.root;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.TopPainter;
import com.engine.ui.controls.Window;
import com.engine.ui.listeners.MainDraw;
import com.engine.root.GdxWrapper;

/**
 * Judins vaizdą, absolucia kamera.
 */

public class FieldMover extends Control {
    private boolean isPressed;
    private OrthographicCamera camera;

    private float sensitivity;

    private float firstX, firstY;

    private boolean enableButtons = true, enableDragging = true;
    private float keyJumpSensitivity;
    private int keyStatus;
    private Worker worker;

    private Resizer sizer;

    public FieldMover(){
        this(new FieldMoverStyle());
    }

    public FieldMover(InterfaceStyle style){
        super(style);
        sensitivity = Resources.getPropertyFloat("moverSensitivity", 1f);
        keyJumpSensitivity = Resources.getPropertyFloat("moverKeySensitivity", 10);
        enableButtons = Resources.getPropertyBoolean("moverEnableButtons", true);
        enableDragging = Resources.getPropertyBoolean("moverEnableDragging", true);
        camera = GdxWrapper.getInstance().getAbsoluteCamera();
        worker = new Worker();
        setFocusable(false);
    }

    /** how sensitive should it react to mouse dragging. Default is 1 */
    public void setSensitivity(float sensitivity){
        this.sensitivity = MoreUtils.abs(sensitivity);
    }

    public void setKeyJumpSensitivity(float keyJumpSensitivity){
        this.keyJumpSensitivity = MoreUtils.abs(keyJumpSensitivity);
    }

    /** enable or disable keyboard camera jumps. */
    public void enableKeyJump(boolean enable){
        enableButtons = enable;
    }

    public void enableDragging(boolean enable){
        enableDragging = enable;
    }

    public boolean isDraggingEnabled(){
        return enableDragging;
    }

    public boolean isKeyJumpEnable(){
        return enableButtons;
    }

    public void setResizer(Resizer e){
        sizer = e;
    }

    @Override
    public void setEnabled(boolean enable) { // enable nenaudos, disable atveju tiesiog nematoma daryt.
        setVisible(enable);
    }

    @Override
    protected void isvaizda(float x, float y) {}

    @Override
    public void release() {
        isPressed = false;
        keyStatus = 0;
    }

    @Override
    protected void auto() {}

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (enableDragging && button == Input.Buttons.LEFT) {
            isPressed = true;
            firstX = x;
            firstY = y;
            return true;
        }
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (isPressed){
            float difX = (x - firstX)*sensitivity * camera.zoom;
            float difY = (y - firstY)*sensitivity * camera.zoom;
            camera.position.x = camera.position.x - difX;
            camera.position.y = camera.position.y - difY;
            if (getPositioning() != Window.fixedView) {
                firstX = x - difX; // cord kompensavimas. cord asis sukas, tai reik kompensuot posuki.
                firstY = y - difY;
            }else { // fixed asis nesisuka, nieko kompensuot nereik.
                firstX = x;
                firstY = y;
            }
            camera.update();
            if (sizer != null)
                sizer.update();
        }
        return isPressed;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (isPressed){
            isPressed = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (isPressed){
            isPressed = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (enableButtons){
            switch (keycode){
                case Input.Keys.LEFT:
                    keyStatus = Input.Keys.LEFT;
                    break;
                case Input.Keys.RIGHT:
                    keyStatus = Input.Keys.RIGHT;
                    break;
                case Input.Keys.UP:
                    keyStatus = Input.Keys.UP;
                    break;
                case Input.Keys.DOWN:
                    keyStatus = Input.Keys.DOWN;
                    break;
                default:
                    keyStatus = 0; // jei kartais praleistu keyUp event, kad bet kuris mygtukas atstrigintu.
                    return false;
            }
            TopPainter.addPaintOnTop(worker);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (enableButtons){
            if (keyStatus != 0){
                keyStatus = 0;
                return true;
            }
        }
        return false;
    }

//    public interface fieldMovedListener{
//        public void fieldMoved(float x, float y);
//    }

    /** kadangi keydown event nesikartoja, tai sis imituoja kartojima. */
    private class Worker implements MainDraw{

        @Override
        public void draw() {
            if (enableButtons){
                switch (keyStatus){
                    case Input.Keys.LEFT:
                        camera.position.x += keyJumpSensitivity;
                        break;
                    case Input.Keys.RIGHT:
                        camera.position.x -= keyJumpSensitivity;
                        break;
                    case Input.Keys.UP:
                        camera.position.y -= keyJumpSensitivity;
                        break;
                    case Input.Keys.DOWN:
                        camera.position.y += keyJumpSensitivity;
                        break;
                    default:
                        TopPainter.removeTopPaint(this);
                        return;
                }
                camera.update();
                if (sizer != null)
                    sizer.update();
            }else {
                TopPainter.removeTopPaint(this);
            }
        }

        @Override
        public boolean drop(int reason) {
            return false;
        }
    }

    @Override
    public InterfaceStyle getStyle() { // nereikalingas, tikrai nereik...
        return null;
    }

    public static class FieldMoverStyle extends InterfaceStyle{ // nereikalingas sitam. Tikrai nedes...
        @Override
        public Control createInterface() {
            return null;
        }
    }
}
