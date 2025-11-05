package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.animations.Counter;

public class ImageButton extends Button {
//    private float scaleX, scaleY;
    private Counter scale;
    private float scaleTime, scaleRatio;


    public ImageButton(ImageButtonStyle style) {
        super(style);

        scaleRatio = style.buttonSizeRatio;
        scaleTime = style.buttonSizeRatioTime;
        scale = new Counter();
//        scale.setCounterInformer(new Counter.CounterInformer() {
//            @Override
//            public void update(float oldValue, float currentValue) {
//                scaleX = currentValue * xSize;
//                scaleY = currentValue * ySize;
//            }
//        });
        update = true;
    }

    @Override
    protected void isvaizda(float x, float y) {
        p.tint(statusColor);

        // tiesiog reikejo angle pakeist, kad neslidinetu paversta detale.
        float scaleX = scale.getCurrentValue() * xSize;
        float scaleY = scale.getCurrentValue() * ySize;
        drawDrawable(getBackground(), x - scaleX/2, y - scaleY/2, xSize + scaleX, ySize + scaleY,
                radiusOrigin.x + scaleX/2, radiusOrigin.y + scaleY/2, false);
        p.noTint();
    }

    @Override
    protected void onPress(float x, float y) {
        super.onPress(x, y);
        if (scaleTime == 0){
            // iskart
//            scaleX = scaleRatio * xSize;
//            scaleY = scaleRatio * ySize;
            scale.startCount(scaleRatio, scaleRatio, 1f);
        }else {
            if (scale.isCounting()){
                if (scale.getCurrentValue() == scaleRatio){ // jei sutampa, counter nepasileis.
                    scale.cancel();
//                    scaleX = scaleRatio * xSize;
//                    scaleY = scaleRatio * ySize; // kad nebutu bedu.
                }else { // paleidziam per naujo counter ir tiek.
                    scale.startCount(scale.getCurrentValue(), scaleRatio, scaleTime);
                }
            }else {
                scale.startCount(0, scaleRatio, scaleTime);
            }
        }
    }

    @Override
    protected void onRelease(float x, float y) {
        super.onRelease(x, y);
        if (scaleTime == 0){
            // no animation
//            scaleX = 0;
//            scaleY = 0;
            scale.startCount(0, 0, 1);
        }else {
            if (scale.isCounting()){
                if (scale.getCurrentValue() == 0){ // vel tiesiog apeit, kad neuzstrigtu.
                    scale.cancel();
//                    scaleX = 0;
//                    scaleY = 0;
                }else {
                    scale.startCount(scale.getCurrentValue(), 0, scaleTime);
                }
            }else {
                scale.startCount(scaleRatio, 0, scaleTime);
            }
        }
    }

    @Override
    protected void autoSize() {
        Drawable img = getBackground();
        if (img == null) // nes kartais autoSize kviečia dar prieš idedant image.
            return;
        xSize = img.getMinWidth();
        ySize = img.getMinHeight();
//        sizeUpdated();
    }

    public float getButtonSizeRatio(){
        return scaleRatio;
    }

    public float getButtonSizeRatioTime(){
        return scaleTime;
    }

    /** ratio of how much button will grow when pressed. */
    public void setButtonSizeRatio(float ratio){
        scaleRatio = ratio;
    }

    /** how fast will button grow. */
    public void setButtonSizeRatioTime(float ratio){
        scaleTime = ratio;
    }

    /* style */

    @Override
    public ImageButtonStyle getStyle(){
        ImageButtonStyle e = new ImageButtonStyle();
        copyStyle(e);
        return e;
    }

    public void copyStyle(ImageButtonStyle st){
        super.copyStyle(st);
        st.buttonSizeRatio = getButtonSizeRatio();
        st.buttonSizeRatioTime = getButtonSizeRatioTime();
    }

    public void readStyle(ImageButtonStyle st){
        super.readStyle(st);
        setButtonSizeRatio(st.buttonSizeRatio);
        setButtonSizeRatioTime(st.buttonSizeRatioTime);
    }

    public static class ImageButtonStyle extends ButtonStyle{
        /** ratio of how much button will grow when pressed. */
        public float buttonSizeRatio = 0.1f;
        /** how fast will button grow. */
        public float buttonSizeRatioTime = 0.1f;

        public ImageButtonStyle(){
            normalColor = 0xFFFFFFFF;
            onColor = 0xFFFFFFFF;
            pressedColor = 0xFFFFFFFF;
        }

        @Override
        public ImageButton createInterface() {
            return new ImageButton(this);
        }
    }
}
