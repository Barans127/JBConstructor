package com.engine.interfaces.controls;

public interface ToastAnimation {
    /** Called when toast is shown. */
    void appear();
    /** Called when toast is closing. Toast must be closed manually by calling {@link Toast#clearToast()}*/
    void disappear();
    /** Called when animation is assigned to toast. */
    void setToast(Toast owner);
    /** Called every frame. */
    void update();
    /** Called before any toast drawing */
    void drawBackground();
    /** Called after all toast drawing. */
    void drawOnTop();
}
