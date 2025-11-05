package com.engine.interfaces.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.engine.core.MoreUtils;
import com.engine.root.GdxPongy;
import com.engine.root.TextContainer;

/**
 * Interface with bounds and text in it.
 */
public abstract class TextBlock extends Clickable {
    private TextContainer text;
    private float textSize;

    private float customX, customY;
    private float textOffsetX, textOffsetY;
    private boolean useCustom;

//    private float oldOpValue;

    // teksto mazinimas, kad tilptu i musu kontrole.
    private boolean shrinkText;
    private float textShrinkStep, textShrinkLine;
    private boolean textIsShrunk;

    public TextBlock(TextStyle style, boolean changeText){
        super(style);
        text = new TextContainer();
        if (style != null) {
            textSize = style.textSize;
            text.setTextSize(style.textSize);
            text.setStyle(style.textFont);
            setTextOffset(style.textOffsetX, style.textOffsetY);
            setTextColor(style.textColor);
            setTextAlign(style.horizontalAlign, style.verticalAlign);
            if (changeText) // bug apejimas, kaikurios kontroles su mandresniu text valdymu, gauna null pointer.
                setText(style.text);

            setTextShrink(style.shrinkText, style.minTextSize, style.shrinkTextStep);
        }
    }

    public TextBlock(TextStyle style) {
        this(style, true);
    }

    /** Shrinks text if text shrink is enabled and text don't fit on control. */
    protected void shrinkText(){
        if (shrinkText){
            float size = textSize;
            this.text.updateBounds(); // sugaudom teksta.
            while (!this.text.isTextFit()){ // netelpa tekstas
                float old = size;
                size -= textShrinkStep; // sumazinam
                if (size < textShrinkLine){ // paziurim ar ribos nekerta.
                    size = textShrinkLine;
                }
                if (old == size)
                    break; // nieko nekeiciam, pasieke riba.
                // iki cia atejo, keiciam teksto dydi.
                text.setTextSize(size);
                this.text.updateBounds();
            }
            textIsShrunk = size != textSize; // pasizymim, kad dydis mazesnis nei originalus.
        }
    }

    /**
     * updates text bounds if interface bounds was changed.
     * usually is called via <code>sizeUpdated()</code>
     */
    public void updateTextBounds() {
        if (useCustom)
            text.setBounds(customX, customY);
        else
            text.setBounds(xSize, ySize);
        shrinkText();
    }

    /* parametrai */

    /** if text shrink should be turned on or off. Text will be shrunk if not fit.
     * @param minTextSize minimum size to which text can be shrunk. This size cannot be bigger or equal then text size
     * @param shrinkStep this step is used to reduce text size if text doesn't fit. Step cannot be null (negative values will be treated as positive).*/
    public void setTextShrink(boolean shrinkText, float minTextSize, float shrinkStep){
        this.shrinkText = shrinkText;
        textShrinkLine = minTextSize;
        this.textShrinkStep = MoreUtils.abs(shrinkStep);
        if (textShrinkLine >= textSize || this.textShrinkStep == 0){
            this.shrinkText = false; // nu wtf? toks pat dydis, tai jis nieko nedarys
        }
        if (textShrinkLine < 0)
            textShrinkLine = 0;
        update = true;
    }

    public void setTextAlign(int horizontal, int vertical) {
        text.setTextAlign(horizontal, vertical);
    }

    public void setTextSize(float size) {
        if (size <= 0)
            return;
        textSize = size;
        text.setTextSize(size);
        textIsShrunk = false;
        if (shrinkText){ // nu cai paziuroim ar visks gerai.
            if (textShrinkLine > textSize){
                textShrinkLine = textSize;
                shrinkText = false;
            }
        }
        update = true;
//        shrinkText();
    }

    /** Set this control text. */
    public void setText(String text) {
        this.text.setText(text);
        if (textIsShrunk)
            setTextSize(textSize);
        update = true;
//        shrinkText();
    }

    public void setTextStyle(BitmapFont e) {
        text.setStyle(e);
//        shrinkText();
        if (textIsShrunk)
            setTextSize(textSize);
        update = true;
    }

    public void setTextColor(int color) {
        text.setTextColor(color);
//        shrinkText();
        if (textIsShrunk)
            setTextSize(textSize);
        update = true;
    }

    /**
     * custom text draw space. Width or height with zero value will disable custom bounds.
     */
    public void setCustomTextBounds(float width, float height) {
        if (width <= 0 || height <= 0) {
            useCustom = false;
        } else {
            useCustom = true;
            customX = width;
            customY = height;
        }
        if (textIsShrunk)
            setTextSize(textSize);
        updateTextBounds();
//        shrinkText();
    }

    public void setTextOffset(float x, float y) {
        textOffsetX = x;
        textOffsetY = y;
    }

    /* būsena */

    /** is text being shrunk */
    public boolean isTextShrinkEnabled(){
        return shrinkText;
    }

    /** Minimum text size. Text cannot be resized to lower size then this. */
    public float getMinTextSize(){
        return textShrinkLine;
    }

    /** if shrink is enabled and text didn't fit then text size will be resized by this step till it fits or reaches min text size. */
    public float getShrinkTextStep(){
        return textShrinkStep;
    }

    public String getText() {
        return text.getText();
    }

    public Color getTextColor() {
        return text.getTextColor();
    }

    /** This is preferred text size. Usually this is correct size unless text was shrunk then this size is not same as visible size.
     * To get visible size you need to get text controller by calling {@link #getTextController()} and call {@link TextContainer#getTextSize()}.  */
    public float getTextSize() {
        return textSize;
    }

    public int getVerticalAlign() {
        return text.getVerticalAlign();
    }

    public int getHorizontalAlign() {
        return text.getHorizontalAlign();
    }

    public float getTextWidth() {
        return text.getTextWidth();
    }

    public float getTextHeight() {
        return text.getTextHeight();
    }

    public float getTextOffsetX() {
        return textOffsetX;
    }

    public float getTextOffsetY() {
        return textOffsetY;
    }

    public TextContainer getTextController() {
        return text;
    }

    /* piešimas */

    /**
     * pieš pagal interface koordinates
     */
    protected void drawText() {
        drawText(position.x, position.y, getAngle());
    }

    /**
     * custom. pieš pagal nurodytas koordinates.
     * NOTE: DO NOT USE CORDS FROM {@link #isvaizda(float, float)} AS THIS WILL DOUBLE ADD SCROLL OFFSET!
     *      *  USE {@link #getPosition()} INSTEAD!
     */
    protected void drawText(float x, float y) {
        drawText(x, y, getAngle());
    }

    /** Custom cords and angle. NOTE: DO NOT USE CORDS FROM {@link #isvaizda(float, float)} AS THIS WILL DOUBLE ADD SCROLL OFFSET!
     *  USE {@link #getPosition()} INSTEAD! */
    protected void drawText(float x, float y, float radius) {
        if (!isRotatable || radius == 0) {
            text.drawText(x + textOffsetX + v.getOffsetX(), y + textOffsetY + v.getOffsetY());
        } else {
            SpriteBatch e = p.getBatch();
            Matrix4 mat = e.getTransformMatrix();
//            e.end();
            mat.setToRotation(0, 0, 1, radius);
            mat.trn(x + radiusOrigin.x, y + radiusOrigin.y, 0);
            p.getBatch().setTransformMatrix(mat);
//            e.begin();
            text.drawText(-radiusOrigin.x+ textOffsetX + v.getOffsetX(), -radiusOrigin.y + textOffsetY + v.getOffsetY()); // turėtu piešt su pakeista matrix.
//            e.end();
            mat.trn(0, 0, 0);
            mat.setToRotation(0, 0, 1, 0); // turėtų atstatyt.
//            e.begin();
            p.getBatch().setTransformMatrix(mat);
        }
    }

    @Override
    protected void drawAddition(boolean isMatrixTransformed) {
        if (isMatrixTransformed){
            drawText(-radiusOrigin.x, -radiusOrigin.y, 0);
        }else {
            drawText(position.x, position.y, getAngle()); // nes jau viskas transformed.
        }                                                            // cord jau paruostos.
    }

    /* override */

//    @Override
//    protected void setAppearCounter() {
//        super.setAppearCounter();
//        oldOpValue = text.getTextColor().a;
//    }

//    @Override
//    protected void tint(float value) {
//        Color e = text.getTextColor();
//        text.setTextColor(e.r, e.g, e.b, oldOpValue * value);
//    }

    @Override
    protected void sizeUpdated() {
        updateTextBounds();
    }

    @Override
    protected void autoSize() {
        if (text.getText().equals("")) {
            xSize = textSize;
            ySize = textSize;
//            sizeUpdated();
            return;
        }
        BitmapFont e = text.getFontScaled(textSize);
//        xSize = p.textWidth(text.getText(), e) + textSize;
        xSize = p.textWidth(text.getText(), e);
        ySize = p.textHeight() - e.getDescent();

        // kadangi sitie skaiciavimui netikslus ir atsiranda klaidu, tai mes apvalinam i virsu
        // pvz jei value 102.2 -> 103. 56.8 -> 57.
        xSize = (int) (xSize + 1);
        ySize = (int) (ySize + 1);
//        sizeUpdated();
    }

    /* style */

    public void copyStyle(TextStyle st){
        super.copyStyle(st);
        st.textSize = textSize;
        st.textColor = Color.argb8888(getTextColor());
        st.horizontalAlign = getHorizontalAlign();
        st.verticalAlign = getVerticalAlign();
        st.textFont = getTextController().getFont();
        st.text = getText();
        st.textOffsetX = getTextOffsetX();
        st.textOffsetY = getTextOffsetY();
    }

    public void readStyle(TextStyle st){
        super.readStyle(st);
        setTextSize(st.textSize);
        setTextColor(st.textColor);
        setTextAlign(st.horizontalAlign, st.verticalAlign);
        setTextStyle(st.textFont);
        setText(st.text);
        setTextOffset(st.textOffsetX, st.textOffsetY);
    }

    public static abstract class TextStyle extends ClickableStyle {
        public float textSize = 44;
        public float textOffsetX = 0;
        public float textOffsetY = 0;
        public BitmapFont textFont = GdxPongy.getInstance().getFont();
        public int textColor = 0xFF000000; // juoda.
        /** left, center, right. Use <code>Align</code> class for aligment. */
        public int horizontalAlign = Align.left;
        /** top, center, bottom. Use <code>Align</code> class for aligment. */
        public int verticalAlign = Align.top;
        public String text = "";

        /** if text won't fit in control then text will be resized to smaller size till it fits. Default: true */
        public boolean shrinkText = true;
        /** if {@link #shrinkText} is true and text didn't fit then text will be resized by this number. default: 5 */
        public float shrinkTextStep = 5f;
        /** if text being resized to fit it won't be smaller then this size. default: 20 */
        public float minTextSize = 20;
    }
}
