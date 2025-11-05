package com.engine.interfaces.controls.toastAnimation;

import com.engine.interfaces.controls.Toast;
import com.engine.interfaces.controls.ToastAnimation;

/** Toast animation with no animation. Toast appears and disappears without animation. */
public class ToastNoAnimation implements ToastAnimation {
    private Toast owner;
    @Override
    public void appear() { }

    @Override
    public void disappear() {
        owner.clearToast(); // tiesiog trinam is saraso.
    }

    @Override
    public void setToast(Toast owner) {
        this.owner = owner;
    }

    @Override
    public void update() { }

    @Override
    public void drawBackground() { }

    @Override
    public void drawOnTop() { }
}
