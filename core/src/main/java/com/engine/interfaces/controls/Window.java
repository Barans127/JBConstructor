package com.engine.interfaces.controls;

import com.badlogic.gdx.math.Vector2;

public interface Window {
    int relativeView = 2;
    int fixedView = 1;
    int absoluteView = 0;

    void handle();

    void fixHandle();

    void show();

    void hide();

    void release();

    boolean touchDown(float x, float y, int pointer, int button);

    boolean keyDown(int keycode);

    boolean keyUp(int keycode);

    boolean scrolled(float amountX, float amountY);

    boolean keyTyped(char e);

    boolean mouseMoved(float x, float y);

    boolean tap(float x, float y, int count, int button);

    boolean longPress(float x, float y);

    boolean fling(float velocityX, float velocityY, int button);

    boolean pan(float x, float y, float deltaX, float deltaY);

    boolean panStop(float x, float y, int pointer, int button);

    boolean zoom(float initialDistance, float distance);

    boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2);

    void pinchStop();

    void setFormId(String id);

    String getFormId();

    enum Position {
        relative(Window.relativeView), absolute(Window.absoluteView), fixed(Window.fixedView);

        private int position;

        Position(int pos) {
            position = pos;
        }

        public int getPosition() {
            return position;
        }
    }
}
