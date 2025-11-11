package com.engine.ui.controls.toasts;

import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.ui.controls.Toast;
import com.engine.ui.controls.toastAnimation.ToastFadeAnimation;

/** Similar to android toast. Focused on toast with simple plain text. Wraps text to fit in the screen.
 * This toast uses default {@link com.engine.ui.controls.Toast.ToastTextStyle} text style.*/
public class AlertToast extends Toast { // panasus i android toast. viens prie vieno beveik.
//    private float textSize = 30f;
    private float maxTextWidth = 0.95f;

    /** Constructs new alert toast on bottom of the screen. */
    public AlertToast(){
        super(Toast.SHORT, new ToastFadeAnimation(), 0.2f, 0.5f);

//        ToastTextStyle tst = getToastTextStyle();

//        setToastAlign(0.5f, 0.2f);
//        setToastAnimation(new ToastFadeAnimation());
    }

    /** Constructs new alert toast on bottom of the screen. */
    public AlertToast(String text){
        this();
        if (text == null)
            text = "null";

        setText(text);
    }

    /** Constructs new alert toast on bottom of the screen.
     * @param time in milliseconds.*/
    public AlertToast(String text, int time){
        this(text);
        setToastTime(time);
    }

//    /** Set text size of this toast. */
//    public void setTextSize(float textSize){
//        if (textSize > 0){
//            this.textSize = textSize;
//        }
//    }

    /** Max width in percentage where text will be wrapped. Width between 0-1. Default 0.95*/
    public void setMaxTextWidth(float maxTextWidth){
        if (maxTextWidth > 0) {
            this.maxTextWidth = MoreUtils.inBounds(maxTextWidth, 0, 1);
        }
    }

    @Override
    public void setText(String text) {
        if (text == null){
            text = "null"; // kad nebutu null pointer, daugiau viska priims.
        }
        ToastTextStyle textStyle = getToastTextStyle();

        Engine p = Engine.getInstance();
        p.textSize(textStyle.textSize); // sito reik, kad normaliai dydi nustatytu.
        float textWidth = p.textWidth(text, p.getScreenWidth() * maxTextWidth);
        float textHeight = p.textHeight();

        // paimam nustatymus, kuriuos keisim
//        float old = textStyle.textSize; // nepametam default parametro.
        boolean autoSize = textStyle.autoSize;
        float width = textStyle.width;
        float height = textStyle.height;

        // sukuriam text pagal sita.
//        textStyle.textSize = textSize;
        textStyle.autoSize = false;
        textStyle.width = textWidth;
        textStyle.height = textHeight;
        super.setText(text);

        // grazinam pakeistus parametrus atgal.
//        textStyle.textSize = old;
        textStyle.autoSize = autoSize;
        textStyle.width = width;
        textStyle.height = height;
    }
}
