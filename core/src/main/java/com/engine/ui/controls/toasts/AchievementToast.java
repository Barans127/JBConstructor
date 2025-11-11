package com.engine.ui.controls.toasts;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.ui.controls.Toast;
import com.engine.ui.controls.widgets.SymbolButton;

/** Focused on toast with image and text on it (SymbolButton achieves that easy). Use default
 * {@link com.engine.ui.controls.Toast.ToastSymbolStyle}. Modifies some parameters with it's own.
 * Wraps text to fit on the screen. */
public class AchievementToast extends Toast {
    private float maxTextWidth = 0.95f;
//    private final ToastSymbolStyle st;
    private float textSize = TOAST_SYMBOL_STYLE.textSize;

    public AchievementToast(){
        setToastVerticalAlign(0.93f);
    }

    public AchievementToast(Drawable e, String name){
        this();

        setSymbolText(e, name);
    }

    public AchievementToast(Drawable e, String text, int time){
        this(e, text);
        setToastTime(time);
    }

//    /** {@link com.engine.interfaces.controls.widgets.SymbolButton.SymbolButtonStyle} style which is used for this toast. */
//    public ToastSymbolStyle getStyle(){
//        return st;
//    }

    @Override
    public void setSymbolText(Drawable symbol, String text) {
        if (symbol == null || text == null){
            throw new IllegalArgumentException("drawable and name cannot be null");
        }

        float width, height, textSize;
        boolean autoSize;

        Engine p = Engine.getInstance(); // talpinam netelpanti teksta
        ToastSymbolStyle st = getToastSymbolStyle();

        // surenkam values.
        width = st.width;
        height = st.height;
        textSize = st.textSize;
        autoSize = st.autoSize;
        //nustatom mum reikalingas values
        st.autoSize = false;
        st.textSize = this.textSize;

        p.textSize(st.textSize); // sito reik, kad normaliai dydi nustatytu.
        float offsetX = 0, offsetY = 0;
        if (st.position == SymbolButton.TextPosition.RIGHT || st.position == SymbolButton.TextPosition.LEFT){
            offsetX = st.symbolWidth;
        }else {
            offsetY = st.symbolHeight;
        }
        float textWidth = p.textWidth(text, p.getScreenWidth() * maxTextWidth) + offsetX;
        float textHeight = p.textHeight() + offsetY;

        st.width = textWidth; // sudedam parametrus reikalingus.
        st.height = textHeight;
        st.symbol = symbol;
        st.text = text;

        if (st.toastBackground != null) {
            setBackground(st.toastBackground);
            tintBackground(st.toastTintBackground);
        }

        super.setSymbolText(st); // paleidziam toast.

        // grazinam values.
        st.autoSize = autoSize;
        st.textSize = textSize;
        st.width = width;
        st.height = height;
    }

    /** Max width in percentage where text will be wrapped. Width between 0-1. Default 0.95*/
    public void setMaxTextWidth(float maxTextWidth){
        if (maxTextWidth > 0) {
            this.maxTextWidth = MoreUtils.inBounds(maxTextWidth, 0, 1);
        }
    }

    /** sets text size for toast style. To get effect on text size use {@link #setSymbolText(Drawable, String)} after this method. */
    public void setTextSize(float textSize){
        if (textSize > 0){
            this.textSize = textSize;
        }
    }
}
