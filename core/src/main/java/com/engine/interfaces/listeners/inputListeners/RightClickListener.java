package com.engine.interfaces.listeners.inputListeners;

import com.engine.interfaces.controls.Interface;

public interface RightClickListener {
    /**@param owner Interface on which right click event occurred
     * @param offsetX offsetY - if interface has an offset. Usually scrollview gives interface an offset.*/
    public void rightClick(Interface owner, float x, float y, float offsetX, float offsetY);
}
