package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.engine.core.MoreUtils;
import com.engine.interfaces.controls.TextBlock;

public class ProgressBar extends TextBlock {
    private float cPercent, step;
    private float percent; // tikrasis procentas
//    private int laikas, inactiveTime;
    private int time;
    private FullBarListener listener;
    private boolean showPercent; // ar rodyt skaičių
    private boolean completed;

    private Drawable background;
    private Drawable bar;
    private int tintBar;

    private float padX, padY;

//    private float oldLength;

    public ProgressBar() {
        this(new ProgressBarStyle());
    }

    public ProgressBar(float x, float y, float width, float height) {
        this(new ProgressBarStyle());
        setPosition(x, y);
        setSize(width, height);
    }

    public ProgressBar(ProgressBarStyle style) {
        super(style);
//        background = style.barBackground;
//        barBackground = style.progressBar;
        setBackground(style.barBackground);
        setBarDrawable(style.progressBar); // turi old dydi pasigaut.
//        if (barBackground == null || background == null) {
////            p.setError("ProgressBar drawables cannot be null", ErrorType.WrongPara);
//            //  default drawables? ne
//        }
//        setBarDrawable(barBackground);
        time = p.millis();
        showPercent = style.showPercent;
        setBackgroundPadding(style.backgroundPadX, style.backgroundPadY);
        setStepSpeed(style.stepSpeed);
        setPercentage(style.percent);
//        inactiveTime = MoreUtils.abs(style.inactiveTime);
//        onColor = style.inactiveColor;
        setProgressBarTint(style.tintProgressBar);
        setFocusable(false);
        setText("0 %");
        update = true;
    }

    /**
     * @param show if percentage should be shown on progress bar.
     */
    public void showPercent(boolean show) {
        showPercent = show;
    }

    @Override
    protected void sizeUpdated() {
        super.sizeUpdated();
//        float textWidth = size;
        float textWidth = xSize * cPercent;
        String text = getText();
//        float wid = getTextController().getFontScaled().getSpaceWidth() * text.length() + getTextController().getTextSize();
        float wid = getTextController().getFontScaled().getSpaceXadvance() * text.length() + getTextController().getTextSize();
        if (textWidth < wid)
            textWidth = wid;
        setCustomTextBounds(textWidth, ySize);
    }

    @Override
    protected void autoSize() {
        if (background != null){ // darysim dydi pagal background drawable.
            float width = background.getMinWidth();
            float height = background.getMinHeight();
            xSize = width;
            ySize = height;
        }
//        sizeUpdated();
    }

    private void progress() {
        if (percent != cPercent) {
            if (percent > cPercent) {
                cPercent += step * Gdx.graphics.getDeltaTime();
                if (percent < cPercent) { // strigciojimo bug fix
                    cPercent = percent;
                }
            } else {
                cPercent -= step * Gdx.graphics.getDeltaTime();
                if (percent > cPercent) {
                    cPercent = percent;
                }
            }
            if (cPercent > 1)
                cPercent = 1;
            else if (cPercent < 0)
                cPercent = 0;
//            size = xSize * cPercent;
            float textWidth = xSize * cPercent;
            float height = ySize;
            String text = (int) (cPercent * 100f) + " %";
//            float wid = getTextController().getFontScaled().getSpaceWidth() * text.length() + getTextController().getTextSize();
            float wid = getTextController().getFontScaled().getSpaceXadvance() * text.length() + getTextController().getTextSize();
            if (textWidth < wid)
                textWidth = wid;
            setCustomTextBounds(textWidth, height);
            setText(text);
            if (getStatus() != NORMAL) {
                normalStatus();
            }
            time = p.millis();
//        } else if (cPercent != 1 && getStatus() == NORMAL) {
//            if (inactiveTime != 0 && p.millis() - laikas > inactiveTime) { // jei 7 sekundes nieko nedarys. paliks raudonas.
//                onStatus();
//            }
        } else {
            if (cPercent == 1 && p.millis() - time > 300) { // biski uzlaikys.
                if (listener != null && !completed) { // tik viena kart kviesim.
                    listener.onFullBar();
                }
                completed = true;
                normalStatus();
            }
        }
    }

//    @Override
//    public void mouseEnter() {} // kad nekeistu spalvos kai pele ant. Desktop versija.

    @Override
    protected void isvaizda(float x, float y) {
        if (isEnabled()) {
            progress();
        }
        transformMatrix(x, y);
    }

    @Override
    protected void drawTransformed(float x, float y) {
        /* background */
        p.tint(statusColor);
//        background.draw(p.getBatch(), x, y, xSize, ySize);
        drawBackground(background, x, y, xSize, ySize);

        /* ta juosta */
        p.tint(tintBar); // nustatom is anksto.
        drawProgressBar(bar, x, y, xSize * cPercent, cPercent);

        /* tekstas. */
        if (showPercent)
            drawText(x, y, 0);
        p.noTint();
    }

    /** Override this if you want to change background drawing. This draws background at full size with padding (padding is applied here). */
    protected void drawBackground(Drawable background, float x, float y, float width, float height){
        float rWitdth = width - width * padX;
        float rHeight = height - height * padY;
        float offsetX = (width - rWitdth)/2;
        float offsetY = (height - rHeight)/2;
        background.draw(p.getBatch(), x + offsetX, y + offsetY, rWitdth, rHeight);
    }

    /** Override this if you want to change progress barBackground itself drawing. Now it just clips ending of image by percentage.
     * @param size this is {@link #getWidth()} * <code>percent</code>. It thought that progress barBackground is always laying down.
     * @param percent current percentage of progress. [0-1] */
    protected void drawProgressBar(Drawable bar, float x, float y, float size, float percent){
        if (percent == 0)
            return; // No drawing if 0.

        // Clipping doesn't work with rotation.
        // Need to recalculate.
        p.startClippingMaskDrawing();
        p.noStroke();
        p.fill(0);
        float angle = getAngle();

        if (isRotatable && angle != 0) {
//            p.rectMode(Align.left);
            Vector2 pos = getPosition();

            p.rect(pos.x + v.getOffsetX(), pos.y + v.getOffsetY(), size,
                ySize, radiusOrigin.x, radiusOrigin.y, angle);
        }else {
            // no rotation, nothing special.
            p.rect(x, y, size, ySize);
        }

        p.startDrawWithClippingMask();
        bar.draw(p.getBatch(), x, y, xSize, ySize);
        p.endClippingMaskDrawing();
    }

    /** Adds given value to current value. [0-1]*/
    public void addPercentage(float percent){
        float old = this.percent;
        old += percent;
        if (old > 1){
            old = 1;
        }else if (old < 0){
            old = 0;
        }
        this.percent = old;
    }

    /** sets new value. [0-1]*/
    public void setPercentage(float percent) {
        if (percent < 0)
            percent = 0;
        else if (percent > 1)
            percent = 1;
        this.percent = percent;
    }

//    /** after this time progress barBackground will change it's tint color to inactive tint color. Set to 0 to disable inactive color */
//    public void setInactiveTime(int time){
//        inactiveTime = MoreUtils.abs(time);
//    }

    /** How fast should progress bar move. Default 1f. */
    public void setStepSpeed(float speed) {
        speed = MoreUtils.abs(speed);
        if (speed > 0) {
            step = speed;
        }
    }

    /** tint of progress bar drawable */
    public void setProgressBarTint(int color){
        tintBar = color;
    }

    /** Background of this progress bar. */
    public void setBackground(Drawable e){
        if (e != null)
            background = e;
    }

    /** Bar who reflects percantage of bar. */
    public void setBarDrawable(Drawable e){
        if (e == null)
            return;
        bar = e;
    }

    /** Background padding in percents. (etc 0.5 means half size). Values bigger then 1 may lead to undefined behavior */
    public void setBackgroundPadding(float padX, float padY){
        this.padY = padY;
        this.padX = padX;
    }

    /** bar tint. */
    public int getProgressBarTint(){
        return tintBar;
    }

    /** background. */
    public Drawable getBackgroundDrawable(){
        return background;
    }

    /** bar drawable. */
    public Drawable getBarDrawable(){
        return bar;
    }

    /** Percantage max step speed. */
    public float getStepSpeed(){
        return step;
    }

    /** How much background padding occurs. In percents. */
    public float getBackgroundPaddingX(){
        return padX;
    }

    /** How much background padding occurs. In percents.  */
    public float getBackgroundPaddingY(){
        return padY;
    }

//    public int getInactiveTime(){
//        return inactiveTime;
//    }

    /** is text shown. */
    public boolean isShowPercent() {
        return showPercent;
    }

    /** Current progress bar percentage. */
    public float getPercentage() {
        return percent;
    }

    /** sets value to given percent */
    public void reset(float startPercent){
        reset(startPercent, true);
    }

    /** Resets bar. Complete listener can be called again after this. */
    public void reset(float startPercent, boolean fullReset) {
        setPercentage(startPercent);
        if (fullReset)
            cPercent = 0;
//        size = 0;
        if (showPercent)
            setText("0 %");
        completed = false;
    }

    public void setFullProgressListener(FullBarListener e) {
        listener = e;
    }

    public interface FullBarListener {
        /** Called when progress bar hits 100%. Called only once. To call twice you must reset progress bar with method {@link #reset(float, boolean)} */
        void onFullBar();
    }

    /* style */

    @Override
    public ProgressBarStyle getStyle(){
        ProgressBarStyle st = new ProgressBarStyle();
        copyStyle(st);
        return st;
    }

    public void copyStyle(ProgressBarStyle st){
        super.copyStyle(st);
        st.barBackground = getBackgroundDrawable();
        st.progressBar = getBarDrawable();
        st.percent = getPercentage();
//        st.inactiveTime = getInactiveTime();
        st.stepSpeed = getStepSpeed();
//        st.inactiveColor = getOverColor();
        st.showPercent = isShowPercent();
        st.tintProgressBar = getProgressBarTint();
        st.backgroundPadX = padX;
        st.backgroundPadY = padY;
    }

    public void readStyle(ProgressBarStyle st){
        super.readStyle(st);
        setBackground(st.barBackground);
        setBarDrawable(st.progressBar);
        setPercentage(st.percent);
//        setInactiveTime(st.inactiveTime);
        setStepSpeed(st.stepSpeed);
//        onColor = st.inactiveColor;
        showPercent(st.showPercent);
        setProgressBarTint(st.tintProgressBar);
        setBackgroundPadding(st.backgroundPadX, st.backgroundPadY);
    }

    public static class ProgressBarStyle extends TextStyle {
        /** background of this progress bar */
        public Drawable barBackground;
        /** bar which will be used to show percentage of progress bar. */
        public Drawable progressBar;
        /** value of progress. */
        public float percent = 0;
//        /** set this to 0 to disable changing colors to inactive color. Time in milliseconds */
//        public int inactiveTime = 7000;
        public float stepSpeed = 1f;
//        public int inactiveColor;
        public boolean showPercent = true;
        public int tintProgressBar = 0xFFFFFFFF;

        public float backgroundPadX = 0;
        public float backgroundPadY = 0f;

        public ProgressBarStyle() {
//            onColor = GdxPongy.color(255, 0, 0); // kai sustabdyta.
//            inactiveColor = onColor;
            verticalAlign = Align.center;
            horizontalAlign = Align.center;
        }

        @Override
        public ProgressBar createInterface() {
            return new ProgressBar(this);
        }
    }
}
