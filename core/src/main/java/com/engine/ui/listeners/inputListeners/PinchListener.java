package com.engine.ui.listeners.inputListeners;

import com.badlogic.gdx.math.Vector2;

public interface PinchListener {
    boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2);
}
