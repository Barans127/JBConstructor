package com.engine.ui.listeners.inputListeners;

import com.engine.ui.controls.Control;

public interface RightClickListener {
    /**@param owner Interface on which right click event occurred
     * @param offsetX offsetY - if interface has an offset. Usually scrollview gives interface an offset.*/
    public void rightClick(Control owner, float x, float y, float offsetX, float offsetY);
}
