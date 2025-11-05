package com.engine.interfaces.controls;

import com.badlogic.gdx.math.Vector2;

/**
 * implement this to catch inputs with <code>TopPainter</code>
 */
public interface Inputs {
    public boolean touchDown(float x, float y, int pointer, int button);

    public boolean keyDown(int keycode);

    public boolean keyUp(int keycode);

    public boolean mouseMoved(float x, float y);

    public boolean keyTyped(char e);

    public boolean scrolled(float amountX, float amountY);

    public boolean tap(float x, float y, int count, int button);

    public boolean longPress(float x, float y);

    public boolean fling(float velocityX, float velocityY, int button);

    public boolean pan(float x, float y, float deltaX, float deltaY);

    public boolean panStop(float x, float y, int pointer, int button);

    public boolean zoom(float initialDistance, float distance);

    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2);

    public void pinchStop();

    public void release();

    /** coords will be translated by using <code>Window.fixed</code> or <code>Window.absolute</code> */
    public int getPositioning();
}
