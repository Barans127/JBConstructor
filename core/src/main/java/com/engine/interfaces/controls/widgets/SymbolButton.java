package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.animations.Counter;
import com.engine.core.ErrorMenu;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;

/** same as  button but uses another drawable as symbol which is not tinted as background drawable.
 * Can be used with text. */
public class SymbolButton extends Button {
    private int tintSymbol;
    private Drawable symbol;

    private float symbolWidth, symbolHeight, symbolOffsetX, symbolOffsetY;
    private TextPosition textPosition;

    private float textOffsetX, textOffsetY;

    // paspaudimo efektas. Turi but igalintas.
    private boolean scaleEffect;
//    private float scaleX, scaleY;
    private Counter scale;
    private float scaleTime, scaleRatio;

    public SymbolButton(SymbolButtonStyle st){
        super(st);
//        if (st.background == null || st.symbol == null){
//            p.setError("", ErrorMenu.ErrorType.ControlsError);
//        }
        symbol = st.symbol;
        tintSymbol = st.symbolTint;
        setSymbolSize(st.symbolWidth, st.symbolHeight);
        textPosition = st.position;
        if (getBackground() == null){ // imam default balta spalva
            setBackground(Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor")));
        }

        // scale effect
        scale = new Counter();
        scale.setUninterruptible(true);

        enableScaling(st.enableScaling);
        setScaleRatio(st.scaleRatio);
        setScaleTime(st.scaleTime);
//        scale.setCounterInformer(new Counter.CounterInformer() {
//            @Override
//            public void update(float oldValue, float currentValue) {
//                scaleX = currentValue * xSize;
//                scaleY = currentValue * ySize;
//            }
//        });
    }

    /* parametru keitimas */

    /** Should scaling animation be enabled */
    public void enableScaling(boolean scaling){
        this.scaleEffect = scaling;
    }

    /** Scaling time. Seconds*/
    public void setScaleTime(float time){
        scaleTime = time;
    }

    /** Scaling ratio. Percents */
    public void setScaleRatio(float ratio){
        scaleRatio = ratio;
    }

    public void setSymbol(Drawable e){
        if (e != null){
            symbol = e;
        }
    }

    /** ARGB format */
    public void setSymbolTint(int color){
        tintSymbol = color;
    }

    public void setSymbolSize(float width, float height){
        symbolWidth = MoreUtils.abs(width);
        symbolHeight = MoreUtils.abs(height);
        if (!isAuto()){
            if (symbolWidth > xSize)
                symbolWidth = xSize;
            if (symbolHeight > ySize)
                symbolHeight = ySize;
        }
    }

    /** set custom offset of symbol. This offset is changed by any changes of size, symbol size or text. You must set this custom offset values every time on any of this button
     * attributes change. */
    public void setCustomSymbolSizeOffset(float offsetX, float offsetY){
        auto(); // padarom tikra dydi, nes po sitio daugiau neupdatinsim.
        symbolOffsetX = offsetX;
        symbolOffsetY = offsetY;
        update = false; // kad nekeistu musu offseto
    }

    /** Where text will be placed. left - text on the left and symbol on right. right - text on the right and symbol on left.
     * up - text on top and symbol down. down - text down while symbol on top.*/
    public void setTextPosition(TextPosition textPosition) {
        this.textPosition = textPosition;
        update = true;
    }

    /* informacijos gavimas */

    /** is scaling animation enabled */
    public boolean isScalingEnabled(){
        return scaleEffect;
    }

    /** how long scaling animation lasts */
    public float getScaleTime(){
        return scaleTime;
    }

    /** how much is symbol size changing */
    public float getScaleRatio(){
        return scaleRatio;
    }

    public float getSymbolWidth(){
        return symbolWidth;
    }

    public float getSymbolHeight(){
        return symbolHeight;
    }

    public Drawable getSymbol() {
        return symbol;
    }

    public int getTintSymbol(){
        return tintSymbol;
    }

    public TextPosition getTextPosition() {
        return textPosition;
    }

    public float getSymbolOffsetX(){
        return symbolOffsetX;
    }

    public float getSymbolOffsetY() {
        return symbolOffsetY;
    }

    /** works as autosize, but uses custom symbol size and sets whole new size by symbol size and text position. */
    public void customSymbolSize(float width, float height){
        if (width < 1 || height < 1){
            p.setError("SymbolButton: symbol size cannot be set lower than 1.", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        if (getText().equals("")){
            symbolOffsetX = symbolOffsetY = 0;
            xSize = symbolWidth = width;
            ySize = symbolHeight = height;
        }
        BitmapFont e = getTextController().getFontScaled(getTextSize());
        float textWidth = p.textWidth(getTextController().getText(), e);
        float textHeight = p.textHeight() - e.getDescent();
        symbolHeight = height; // teksto dydzio
        symbolWidth = width;
        switch (textPosition){
            case LEFT:
                xSize = symbolWidth + textWidth + getTextSize()/3;
                ySize = textHeight;
                symbolOffsetX = xSize - symbolWidth;
                if (symbolHeight < ySize){
                    symbolOffsetY = ySize/2 - symbolHeight/2;
                }else {
                    symbolOffsetY = 0;
                }
                setCustomTextBounds(xSize-symbolWidth, ySize);
                break;
            case RIGHT:
                super.setTextOffset(symbolWidth + textOffsetX, textOffsetY); // paslenkam
                symbolOffsetX = 0;
                xSize = symbolWidth + textWidth + getTextSize()/3;
                ySize = textHeight;
                if (symbolHeight < ySize){
                    symbolOffsetY = ySize/2 - symbolHeight/2;
                }else {
                    symbolOffsetY = 0;
                }
                setCustomTextBounds(xSize-symbolWidth, ySize);
                break;
            case DOWN:
                if (symbolWidth*4 < textWidth){ // neleisim virsyt du kartus dydzio
                    textWidth = p.textWidth(getText(), symbolWidth*4, e);
                    textHeight = p.textHeight();
                }
                xSize = Math.max(symbolWidth, textWidth);
                ySize = symbolHeight + textHeight + getTextSize()/3;
                super.setTextOffset(textOffsetX, textOffsetY);
                symbolOffsetX = xSize/2 - symbolWidth/2;
                symbolOffsetY = ySize - symbolHeight;
                setCustomTextBounds(xSize, ySize-symbolHeight);
                break;
            case UP:
                if (symbolWidth*4 < textWidth){ // neleisim virsyt du kartus dydzio
                    textWidth = p.textWidth(getText(), symbolWidth*4, e);
                    textHeight = p.textHeight();
                }
                xSize = Math.max(symbolWidth, textWidth);
                ySize = symbolHeight + textHeight + getTextSize()/3;
                super.setTextOffset(textOffsetX, symbolHeight + textOffsetY);
                symbolOffsetX = xSize/2 - symbolWidth/2;
                symbolOffsetY = 0;
                setCustomTextBounds(xSize, ySize-symbolHeight);
                break;
        }
        sizeUpdated();
        update = false;
    }

    /* override metodai */

    @Override
    public void setTextOffset(float x, float y) {
        super.setTextOffset(x, y);
        textOffsetX = x;
        textOffsetY = y;
    }

    @Override
    public void setSize(float xSize, float ySize) {
        super.setSize(xSize, ySize);
        if (getText().equals("")){
            if (symbolWidth == 0) {
                symbolWidth = this.xSize;
                symbolOffsetX = 0;
            }
            if (symbolHeight == 0) {
                symbolHeight = this.ySize;
                symbolOffsetY = 0;
            }
        } // o del teksto?
    }

    @Override
    protected void autoSize() {
        if (getText().equals("")){ // ner teksto, tiesiog symbolio dydzio.
            xSize = symbol.getMinWidth();
            ySize = symbol.getMinHeight();
            symbolWidth = xSize;
            symbolHeight = ySize;
//            sizeUpdated();
        }else {
            switch (textPosition){
                case LEFT:
                case RIGHT:
                    BitmapFont e = getTextController().getFontScaled(getTextSize());
                    p.textWidth(getTextController().getText(), e);
                    float textHeight = p.textHeight() - e.getDescent();
                    customSymbolSize(textHeight, textHeight);
                    break;
                case DOWN:
                case UP:
                    customSymbolSize(symbol.getMinWidth(), symbol.getMinHeight());
                    break;
            }
        }
    }

    @Override
    protected void sizeUpdated() {
        if (!isAuto() && !getText().equals("")){ // kai ner auto, tiesiog symbol offseta nustatyt.
            switch (textPosition){
                case DOWN:
                    symbolOffsetY = ySize - symbolHeight;
                    symbolOffsetX = xSize/2 - symbolWidth/2;
                    super.setTextOffset(textOffsetX, textOffsetY);
                    setCustomTextBounds(xSize, symbolOffsetY); // nereikia, kad tekstas listu is ribu.
                    break;
                case UP:
                    symbolOffsetX = xSize/2 - symbolWidth/2;
                    symbolOffsetY = 0;
                    super.setTextOffset(textOffsetX, symbolHeight + textOffsetY);
                    setCustomTextBounds(xSize, ySize-symbolHeight);
                    break;
                case RIGHT:
                    super.setTextOffset(symbolWidth + textOffsetX, textOffsetY); // paslenkam
                    if (symbolHeight < ySize){
                        symbolOffsetY = ySize/2 - symbolHeight/2;
                    }else {
                        symbolOffsetY = 0;
                    }
                    symbolOffsetX = 0;
                    setCustomTextBounds(xSize-symbolWidth, ySize);
                    break;
                case LEFT:
                    if (symbolHeight < ySize){
                        symbolOffsetY = ySize/2 - symbolHeight/2;
                    }else {
                        symbolOffsetY = 0;
                    }
                    symbolOffsetX = xSize-symbolWidth;
                    setCustomTextBounds(xSize-symbolWidth, ySize);
                    break;
            }
        }
        super.sizeUpdated();
    }

    @Override
    protected void isvaizda(float x, float y) {
        super.isvaizda(x, y);
        p.tint(tintSymbol);
        float xScale, yScale;
        if (scaleEffect){
            xScale = symbolWidth * scale.getCurrentValue();
            yScale = symbolHeight * scale.getCurrentValue();
        }else {
            xScale = 0;
            yScale = 0;
        }
        drawDrawable(symbol, x+symbolOffsetX + xScale/2, y+symbolOffsetY + yScale/2,
                symbolWidth - xScale, symbolHeight - yScale,
                radiusOrigin.x - xScale/2, radiusOrigin.y - yScale/2, false); // false, nu nes super metode piesia teksta.
        p.noTint();
    }

    @Override
    protected void onPress(float x, float y) {
        super.onPress(x, y);
        if (scaleEffect) {
            if (scaleTime == 0) {
                // iskart
//                scaleX = scaleRatio * xSize;
//                scaleY = scaleRatio * ySize;
                scale.startCount(scaleRatio, scaleRatio, 1); // pasizymim.
            } else {
                if (scale.isCounting()) {
                    if (scale.getCurrentValue() == scaleRatio) { // jei sutampa, counter nepasileis.
                        scale.cancel();
//                        scaleX = scaleRatio * xSize;
//                        scaleY = scaleRatio * ySize; // kad nebutu bedu.
                    } else { // paleidziam per naujo counter ir tiek.
                        scale.startCount(scale.getCurrentValue(), scaleRatio, scaleTime);
                    }
                } else {
                    scale.startCount(0, scaleRatio, scaleTime);
                }
            }
        }
    }

    @Override
    protected void onRelease(float x, float y) {
        super.onRelease(x, y);
        if (scaleEffect) {
            if (scaleTime == 0) {
                // no animation
//                scaleX = 0;
//                scaleY = 0;
                scale.startCount(0, 0, 1);
            } else {
                if (scale.isCounting()) {
                    if (scale.getCurrentValue() == 0) { // vel tiesiog apeit, kad neuzstrigtu.
                        scale.cancel();
//                        scaleX = 0;
//                        scaleY = 0;
                    } else {
                        scale.startCount(scale.getCurrentValue(), 0, scaleTime);
                    }
                } else {
                    scale.startCount(scaleRatio, 0, scaleTime);
                }
            }
        }
    }

    /* enum del teksto padeties. */

    public enum TextPosition{
        UP, DOWN, LEFT, RIGHT
    }

    /* style */

    @Override
    public SymbolButtonStyle getStyle(){
        SymbolButtonStyle st = new SymbolButtonStyle();
        copyStyle(st);
        return st;
    }

    public void copyStyle(SymbolButtonStyle st){
        super.copyStyle(st);
        st.symbol = symbol;
        st.symbolTint = tintSymbol;
        st.position = textPosition;
        st.symbolWidth = getSymbolWidth();
        st.symbolHeight = getSymbolHeight();
        st.enableScaling = isScalingEnabled();
        st.scaleRatio = getScaleRatio();
        st.scaleTime = getScaleTime();
    }

    public void readStyle(SymbolButtonStyle st){
        super.readStyle(st);
        symbol = st.symbol;
        tintSymbol = st.symbolTint;
        textPosition = st.position;
        setSymbolSize(st.symbolWidth, st.symbolHeight);
        enableScaling(st.enableScaling);
        setScaleTime(st.scaleTime);
        setScaleRatio(st.scaleRatio);
        update = true; // reik, nes pozicija turi susigaudyt.
    }

    public static class SymbolButtonStyle extends ButtonStyle{
        public Drawable symbol;
        public int symbolTint = 0xFFFFFFFF;

        public TextPosition position = TextPosition.RIGHT;
        /** to use this, autoSize should be set to false. */
        public float symbolWidth = textSize, symbolHeight = textSize;

        /* scale efekto */
        /** when user press on button symbol will changes sizes (animation effect). Default true */
        public boolean enableScaling = true;
        /** time used for scaling animation. Default 0.1 */
        public float scaleTime = 0.1f;
        /** how much symbol changes size (percents). Default 0.1 */
        public float scaleRatio = 0.1f;

        public SymbolButtonStyle(){
            text = "";
        }

        @Override
        public SymbolButton createInterface() {
            return new SymbolButton(this);
        }
    }
}
