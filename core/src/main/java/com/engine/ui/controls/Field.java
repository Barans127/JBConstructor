package com.engine.ui.controls;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.engine.core.ErrorMenu;
import com.engine.core.MoreUtils;

/** Interface with bounds. */
public abstract class Field extends Control {
    protected float xSize, ySize;
//    protected float nxSize, nySize;
    private boolean useAuto, mouseIn;
    private final Vector2 tmp = new Vector2();
    protected final Vector2 radiusOrigin = new Vector2();
    protected boolean isRotatable;
    private boolean transformShapes = false;

//    public Field(){
//        this(new FieldStyle());
//    }

    public Field(FieldStyle style){
        super(style);
        if (style != null) {
            useAuto = style.autoSize;
            xSize = MoreUtils.abs(style.width);
            ySize = MoreUtils.abs(style.height);
            isRotatable = style.rotatable;
            setOrigin(style.originX, style.originY);
            enableShapeTransform(style.transformShapes);
        }
    }

    protected void mouseEvents(float x, float y, boolean mouseMoving){ // sita iskvietus effect'e veiks mouseenter ir leave eventai.
        boolean currentPos = pointIn(x, y);
        if (currentPos != mouseIn){
            mouseIn = currentPos;
            if (mouseMoving) {
                if (mouseIn) {
                    if (isEnabled())
                        mouseEnter();
                } else {
                    if (isEnabled())
                        mouseLeave();
                }
            }
        }
    }

    /** calls <code>autoSize</code> if it's enabled, and after that calls <code>sizeUpdated</code>
     * (this method called even if auto size is disabled). */
    @Override
    public final void auto(){
        if (useAuto){
            autoSize();
        }
        sizeUpdated();
    }

    /* parametrų nustatymai */

    /** enables shapes transforming which are drawn by shapeRenderer */
    public void enableShapeTransform(boolean enable){
        synchronized (this) {
            transformShapes = enable;
        }
    }

    /** Place interface in middle by given point. */
    public void placeInMiddle(float x, float y){
        if (update){ // bug kai daroma place in middle, o dydis dar nera tinkamas.
            auto();
        }
        setPosition(x-xSize/2, y - ySize/2);
    }

    /** Allows to setSize manually, disables <code>autoSize()</code> call. Negative size causes undefined behavior*/
    public void setSize(float xSize, float ySize){
        // kvieciant sia funkcija automatinis dydis butu panaikintas.
        if (xSize < 0) xSize = 1;
        if (ySize < 0) ySize = 1;
        this.xSize = xSize;
        this.ySize = ySize;
        checkPositioning();
        setAutoSizing(false);
        frustumUpdateId = -1; // kad per naujo frustum pasidarytu.
        update = true;
    }

    public void setAutoSizing(boolean auto){
        useAuto = auto;
        if (useAuto)
            update = true;
    }

    public void setOrigin(Vector2 e) {
        setOrigin(e.x, e.y);
    }

    public void setOrigin(float x, float y) {
        radiusOrigin.set(x, y);
    }

    /** sets origin to middle. if size changes this must be called to apply origin to middle again. */
    public void setOriginMiddle(){
        setOrigin(xSize / 2, ySize / 2);
    }

    public void setRotatable(boolean rotatable){
        this.isRotatable = rotatable;
    }

    /* būsena */

    /** if control can be rotated by <code>setRotate()</code> method */
    public boolean isRotatable(){
        return isRotatable;
    }

    public Vector2 getRadiusOrigin() {
        return tmp.set(radiusOrigin);
    }

    public float getWidth(){
        return xSize;
    }

    public float getHeight(){
        return ySize;
    }

    /** ar duotas taškas yra kontrolės teritorijoj. */
    public boolean pointIn(float x, float y) {
//        Vector2 e = getMiddlePoint();
//        float rx = e.x, ry = e.y;
        if (getAngle() != 0){
            // minus, nes kordinates verciamos is realiu i virtualias.
            Vector2 a = rotatePoint(x, y, position.x, position.y, radiusOrigin.x, radiusOrigin.y, -getAngle());
            x = a.x;
            y = a.y;
        }
        return x > position.x && x < position.x + xSize && y > position.y && y < position.y + ySize;
    }

    public Vector2 getMiddlePoint() { // daugiausiai naudos gridas.
        if (getAngle() == 0)
            return tmp.set(position.x + xSize / 2, position.y + ySize / 2);
        else {
            // gerRadius plius, nes verciama cord is virtualiu i realias.
            return rotatePoint(position.x + xSize / 2, position.y + ySize / 2, position.x, position.y,
                    radiusOrigin.x, radiusOrigin.y, getAngle());
        }
    }

    /** if autoSizing on: true, false otherwise */
    public boolean isAuto(){
        return useAuto;
    }

    /** pranest, kad pelytė nebėra kontrolės teritoijoj. */
    protected void mouseOut(){
        mouseIn = false;
    }

    /**
     * Pasako ar pelytė ant kontrolės. Prieš tai reik iškviest <code>mouseEvents()</code> metodą
     */
    protected boolean isMouseIn(){
        return mouseIn;
    }

    /* listeners */

    protected void mouseLeave(){}

    protected void mouseEnter(){}

    /* helpers */

    /** Draws single drawable and also calls <code>drawAddition</code> method. Usually drawAddition draws text. */
    protected void drawDrawable(Drawable e, float x, float y, boolean drawAddition){
        drawDrawable(e, x, y, xSize, ySize, drawAddition);
    }

    /** Draws single drawable and also calls <code>drawAddition</code> method. Usually drawAddition draws text. */
    public void drawDrawable(Drawable e, float x, float y, float nxSize, float nySize, boolean drawAddition){
        drawDrawable(e, x ,y, nxSize, nySize, radiusOrigin.x, radiusOrigin.y, drawAddition);
    }

    /** Draws single drawable and also calls <code>drawAddition</code> method. Usually drawAddition draws text. */
    public void drawDrawable(Drawable e, float x, float y, float nxSize, float nySize, float originX, float originY, boolean drawAddition){
        drawDrawable(e, x, y, nxSize, nySize, originX, originY, isRotatable ? getAngle() : 0f, drawAddition);
    }

    /** Draws single drawable and also calls <code>drawAddition</code> method. Usually drawAddition draws text.
     * @param angle in degrees */
    protected void drawDrawable(Drawable e, float x, float y, float nxSize, float nySize, float originX, float originY, float angle, boolean drawAddition){
        if (e == null){
            p.setError(this.getClass().getSimpleName() + ": drawable cannot be null. Did you give this control a drawable?", ErrorMenu.ErrorType.ControlsError);
            return;
        }
        if (angle != 0) { // tik jei rotate yra.
            if (e instanceof TransformDrawable){
                ((TransformDrawable) e).draw(p.getBatch(), x, y, originX, originY, nxSize, nySize, 1, 1, angle);
                if (drawAddition){
                    drawAddition(false);
                }
            }else {
                Matrix4 mat = p.getBatch().getTransformMatrix();
//            e.end();
                mat.setToRotation(0, 0, 1, angle);
                mat.trn(x + originX, y + originY, 0);
                p.getBatch().setTransformMatrix(mat);
                if (transformShapes)
                    p.getShapeRender().setTransformMatrix(mat);
//            e.begin();
                e.draw(p.getBatch(), -originX, -originY, nxSize, nySize);
                if (drawAddition){
                    drawAddition(true);
                }
//            e.end();
                mat.trn(0, 0, 0);
                mat.setToRotation(0, 0, 1, 0); // turėtų atstatyt.
//            e.begin();
                p.getBatch().setTransformMatrix(mat);
                if (transformShapes)
                    p.getShapeRender().setTransformMatrix(mat);
            }
        }else {
            e.draw(p.getBatch(), x, y, nxSize, nySize);
            if (drawAddition){
                drawAddition(false);
            }
        }
    }

    protected void drawAddition(boolean isMatrixTransformed){}

    /** Transforms current matrix if control is rotatable and angle != 0 also calls {@link #drawTransformed(float, float)} method.
     * DO NOT USE ANY OF {@link #drawDrawable(Drawable, float, float, boolean)} method here!! */
    protected void transformMatrix(float x, float y){
        if (isRotatable && getAngle() != 0){
            float radius = getAngle();
            Matrix4 mat = p.getBatch().getTransformMatrix();
//            e.end();
            mat.setToRotation(0, 0, 1, radius);
            mat.trn(x + radiusOrigin.x, y + radiusOrigin.y, 0);
            p.getBatch().setTransformMatrix(mat);
            if (transformShapes)
                p.getShapeRender().setTransformMatrix(mat);
//            e.begin();
//            e.draw(p.getBatch(), -radiusOrigin.x, -radiusOrigin.y, nxSize, nySize);
            drawTransformed(-radiusOrigin.x, -radiusOrigin.y);
//            e.end();
            mat.trn(0, 0, 0);
            mat.setToRotation(0, 0, 1, 0); // turėtų atstatyt.
//            e.begin();
            p.getBatch().setTransformMatrix(mat);
            if (transformShapes)
                p.getShapeRender().setTransformMatrix(mat);
        }else{
            drawTransformed(x, y);
        }
    }

    /** called when <code>transformMatrix(x, y)</code> is used in <code>isvaizda()</code>.
     * DO NOT USE ANY OF {@link #drawDrawable(Drawable, float, float, boolean)} method here!! */
    protected void drawTransformed(float x, float y){

    }

    protected Vector2 rotatePoint(float pointX, float pointY){
        return rotatePoint(pointX, pointY, position.x, position.y, radiusOrigin.x, radiusOrigin.y, getAngle());
    }

    protected Vector2 rotatePoint(float pointX, float pointY, float degrees){
        return rotatePoint(pointX, pointY, position.x, position.y, radiusOrigin.x, radiusOrigin.y, degrees);
    }

    protected Vector2 rotatePoint(float pointX, float pointY, float rotateX, float rotateY, float originX, float originY, float degrees){
        float posX = rotateX + originX;
        float posY = rotateY + originY;
        float len = MoreUtils.dist(posX, posY, pointX, pointY);
        float rad = degrees * MathUtils.degreesToRadians;
        float angle = MathUtils.atan2(pointY - posY, pointX - posX) + rad;
        float rx, ry;
        rx = MathUtils.cos(angle) * len + posX;
        ry = MathUtils.sin(angle) * len + posY;
        return tmp.set(rx, ry);
    }

    /* override */

    @Override
    public float getAngle() {
        if (isRotatable){
            return super.getAngle();
        }else {
            return 0;
        }
    }

    @Override
    public void release() {
//        super.release();
        mouseOut();
        normalStatus();
    }

    protected abstract void autoSize(); // reikia overridint.

    /* style */

    public void copyStyle(FieldStyle st){
        super.copyStyle(st);
        st.width = xSize;
        st.height = ySize;
        st.autoSize = useAuto;
        st.rotatable = isRotatable;
        st.originX = radiusOrigin.x;
        st.originY = radiusOrigin.y;
        st.transformShapes = transformShapes;
    }

    public void readStyle(FieldStyle st) {
        super.readStyle(st);
        setSize(st.width, st.height);
        setAutoSizing(st.autoSize);
        setRotatable(st.rotatable);
        setOrigin(st.originX, st.originY);
        enableShapeTransform(st.transformShapes);
    }

    public static abstract class FieldStyle extends InterfaceStyle{
        public float width;
        public float height;
        public boolean autoSize = true;
        public boolean rotatable = false;
        /** point where this should be rotated by. */
        public float originX = 0, originY = 0;
        /** {@link #drawTransformed(float, float)} method transforms matrix except if shapes are used they are not transformed. if you want shapes to be
         * transformed too you need to set this to true. */
        public boolean transformShapes = false;
    }
}
