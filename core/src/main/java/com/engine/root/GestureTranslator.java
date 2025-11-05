package com.engine.root;

import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;

public class GestureTranslator implements GestureListener {
    private final GdxPongy p;

    GestureTranslator(GdxPongy p) {
        this.p = p;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
//        System.out.println("touchDown");
        return p.touchDown(x, y, pointer, button);
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
//        System.out.println("tap");
        return p.tap(x, y, count, button);
    }

    @Override
    public boolean longPress(float x, float y) {
        return p.longPress(x, y);
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return p.fling(velocityX, velocityY, button);
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
//        System.out.println("pan");
        return p.pan(x, y, deltaX, deltaY);
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
//        System.out.println("panStop");
        return p.panStop(x, y, pointer, button);
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return p.zoom(initialDistance, distance);
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
//        System.out.println("pinch");
        return p.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
    }

    @Override
    public void pinchStop() {
//        System.out.println("pinchStop");
        p.pinchStop();
    }
}
