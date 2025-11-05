package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.engine.interfaces.controls.TextBlock;

/** Draw image. Can keep it's ratio so it will not be stretched.*/
public class SImage extends TextBlock {
    private Drawable img;
    private boolean showText;

    private boolean hasCont;

    private boolean keepRatio;
    private float imageSizeX, imageSizeY;
    private float offsetX, offsetY;

    public SImage(SImageStyle style) {
        super(style);
        setColors(style.tintImage, style.tintImage, style.tintImage);
        showText = style.showText;
        img = style.image;
        hasCont = style.showContour;
        keepRatio = style.keepRatio;
        setFocusable(hasCont);
        if (hasCont)
            enableShapeTransform(true);
        update = true;
        if (keepRatio){
            proportions();
        }
    }

    public SImage(Texture e) {
        this(new TextureRegion(e));
    }

    public SImage(TextureRegion e) {
        this(new SImageStyle());
        setImage(new TextureRegionDrawable(e));
    }

    public SImage(Drawable e){
        this(new SImageStyle());
        setImage(e);
    }

    private void proportions(){
        if (img != null) {
            float width = img.getMinWidth();
            float height = img.getMinHeight();
            float ratio = width / height;

            float tmpHeight = xSize / ratio;
            if (tmpHeight > ySize) {

                imageSizeX = ySize * ratio;
                imageSizeY = ySize;

            } else {
                imageSizeX = xSize;
                imageSizeY = tmpHeight;
            }
            offsetX = (xSize - imageSizeX)/2;
            offsetY = (ySize - imageSizeY)/2;
        }
    }

//    @Override
//    public void setSize(float xSize, float ySize) {
//        super.setSize(xSize, ySize);
//    }

    @Override
    protected void autoSize() {
        if (img == null)
            return;
        xSize = img.getMinWidth();
        ySize = img.getMinHeight();
//        imageSizeX = xSize;
//        imageSizeY = ySize;
//        sizeUpdated();
    }

    @Override
    protected void sizeUpdated() {
        super.sizeUpdated();
        if (keepRatio){
            proportions();
        }else { // jeigu nera proprotions tai reik pazymet, kad dydis butent toks koks yra.
            imageSizeX = xSize;
            imageSizeY = ySize;
            offsetX = 0;
            offsetY = 0;
        }
    }

    @Override
    protected void isvaizda(float x, float y) {
        p.tint(statusColor);
        if (hasCont && (getStatus() == PRESSED || getStatus() == OVER)) { // paspaudus.
            transformMatrix(x+offsetX, y+offsetY);
        } else
            drawDrawable(img, x+offsetX, y+offsetY, imageSizeX, imageSizeY, showText);
        p.noTint();
    }

    @Override
    protected void drawTransformed(float x, float y) {
        p.rectMode(Align.left);
        p.noFill();
        p.strokeWeight(4);
        p.stroke(0);
        p.rect(x, y, xSize, ySize);
        img.draw(p.getBatch(), x, y, imageSizeX, imageSizeY);
        if (showText)
            drawText(x, y, 0);
    }

    /** set image. */
    public void setImage(Drawable e) {
        if (e != null) {
            img = e;
            update = true;
            if (keepRatio){
                proportions();
            }
        }
    }

    /** ARGB format */
    public void tintImage(int color){
        setColors(color, color, color);
    }

    /** tints image */
    public void tintImage(Color color){
        int argb = Color.argb8888(color);
        setColors(argb, argb, argb);
    }

    /** draws bounds around image when pressed or mouse over image.*/
    public void enableContour(boolean enable) {
        hasCont = enable;
        enableShapeTransform(hasCont);
    }

    /** get image. */
    public Drawable getImage() {
        return img;
    }

    /** tint of this image. */
    public int getImageTint(){
        return getNormalColor();
    }

    /** Is image proportions kept. With and height ratio is kept. */
    public boolean isKeepRatio(){
        return keepRatio;
    }

    /** Keep image width and height ratio. */
    public void setKeepRatio(boolean keepRatio){
        this.keepRatio = keepRatio;
        update = true; // turi pasigaut nauja dydi paveiksliuko.
    }

    /* style */

    @Override
    public SImageStyle getStyle(){
        SImageStyle e = new SImageStyle();
        copyStyle(e);
        return e;
    }

    public void copyStyle(SImageStyle st){
        super.copyStyle(st);
        st.showText = showText;
        st.showContour = hasCont;
        st.image = getImage();
        st.tintImage = getImageTint();
        st.keepRatio = keepRatio;
    }

    public void readStyle(SImageStyle st){
        super.readStyle(st);
        showText = st.showText;
        hasCont = st.showContour;
        setImage(st.image);
        tintImage(st.tintImage);
        setKeepRatio(st.keepRatio);
    }

    public static class SImageStyle extends TextStyle {
        public boolean showText = false;
        public boolean showContour = true;
        public Drawable image;
        public int tintImage = 0xFFFFFFFF;
        public boolean keepRatio = true;

        public SImageStyle() {
            rotatable = true;
        }

        @Override
        public SImage createInterface() {
            return new SImage(this);
        }
    }
}
