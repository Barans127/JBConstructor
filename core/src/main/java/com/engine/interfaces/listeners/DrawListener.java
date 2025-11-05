package com.engine.interfaces.listeners;

public interface DrawListener {
    /** called before any drawings */
    void preDraw();
    /** called after all drawings */
    void postDraw();
}
