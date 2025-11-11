package com.engine.ui.listeners;

public interface DrawListener {
    /** called before any drawings */
    void preDraw();
    /** called after all drawings */
    void postDraw();
}
