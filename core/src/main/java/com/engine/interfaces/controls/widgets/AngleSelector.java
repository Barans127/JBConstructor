package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.interfaces.controls.Draggable;

/** class to select desired angle. Angle is in radians! */
public class AngleSelector extends Draggable {
    private Drawable circle, pointer;

    private float angle;
    private float pointerThickness;

    private AngleSelectorListener angleSelectorListener;

    private boolean multiSpin;

    public AngleSelector(){
        this(new AngleSelectorStyle());
    }

    public AngleSelector(AngleSelectorStyle style) {
        super(style);
        setCircleDrawable(style.circle);
        setPointerDrawable(style.pointer);
        setAngle(style.viewingAngle);
        setPointerThickness(style.pointerThickness);
        enableMultiSpin(style.enableMultiSpin);
    }

    /* getters setters */

    /** set angle of pointer. This will change pointer drawing. Angle in radians. */
    public void setViewingAngle(float angle){
        this.angle = angle;
    }

    /** @return pointer angle */
    public float getViewingAngle(){
        return angle;
    }

    public void setCircleDrawable(Drawable e){
        if (e != null){
            circle = e;
        }
    }

    public void setPointerDrawable(Drawable e){
        if (e != null){
            pointer = e;
        }
    }

    public Drawable getCircleDrawable(){
        return circle;
    }

    public Drawable getPointerDrawable(){
        return pointer;
    }

    /** set thickness of viewingAngle pointer. Thickness should not surpass height of this selector. */
    public void setPointerThickness(float thickness){
        if (thickness > 0 && thickness < ySize){
            pointerThickness = thickness;
        }
    }

    /** pointer thickness. */
    public float getPointerThickness(){
        return pointerThickness;
    }

    public void setAngleSelectorListener(AngleSelectorListener listener){
        angleSelectorListener = listener;
    }

    public AngleSelectorListener getAngleSelectorListener(){
        return angleSelectorListener;
    }

    public void enableMultiSpin(boolean enable){
        multiSpin = enable;
    }

    public boolean isMultiSpinEnabled(){
        return multiSpin;
    }

    /* inputs */

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
//        countAngle(x, y);
        if (super.touchDown(x, y, pointer, button)){
            countAngle(x, y);
            return true;
        }
        return false;
    }

    @Override
    protected void onDragging(float x, float y, float deltaX, float deltaY) {
        countAngle(x,y);
    }

    @Override
    protected void onDrop(float x, float y, float deltaX, float deltaY) {}

    private void countAngle(float x, float y){
        Vector2 pos = getMiddlePoint();

        float old = angle;
        // kazkiek kompensuojam jei paversta. Jei neversta tai nieko ir nebus.
        float nangle = MathUtils.atan2(y - pos.y, x - pos.x) - this.getAngle()*MathUtils.degreesToRadians;

        // supaprastinam viewingAngle ties 0, 90, -90, 180.
        // cia tam, kad viewingAngle sustotu ties siais kampais (nes niekada ties jais nesustoja, nors jie kaip ir pagrindiniai).
        float tolerance = 1 * MathUtils.degreesToRadians; // 1 degree tolerance ties 4 taskais.
        float pi = MathUtils.PI;
        if (nangle < tolerance && nangle > -tolerance){ // nangle stovi ties [-1:1]
            nangle = 0;
        }else if (nangle > pi/2 - tolerance && nangle < pi/2 + tolerance){ // [89:91]
            nangle = pi/2;
        }else if (nangle < -pi/2 + tolerance && nangle > -pi/2 - tolerance){ // [-91:-89]
            nangle = -pi/2;
        }else if (nangle > pi - tolerance || nangle < -pi + tolerance){ // daugiau 179 arba maziau -179
            nangle = pi;
        }

        // jeigu mums leidziama sukiot per asi kelis kartus.
        if (multiSpin){
            int jumpSize = 0; // cia kai viewingAngle maziau -180 arba daugiau 180.
            float a = angle;
            boolean anglePlius;// musu viewingAngle yra pliusine arba minusine.
            // paziurim ar viewingAngle virs horizonto (maziau -180 arba daugiau 180)
            // normalizuojam musu viewingAngle ir paziurim ar pasizymim ar per ribas perlipo. Padarom viewingAngle [-180:180]
            if (a > 0){ // pliusinis.
                anglePlius = true;
                while (a > pi){
                    a -= 2*pi;
                    jumpSize++; // pazymim, per kiek mazinam.
                }
            }else { // minusinis.
                anglePlius = false;
                while (a < -pi){
                    a += 2*pi;
                    jumpSize++; // pazymim per kiek didinam.
                }
            }

            // su normalizuotu viewingAngle reik paziurim ar musu naujas viewingAngle perlipo 180 barjera.
            // senojo viewingAngle pozicija turi buti tuose ketvirciuose, kuriuose gali ivykt suolis.
            // su atan2 pasiekus 180 ir paejus zemiau gaunam minusini skaiciu, todel turim issiaiskint
            // ar per lipom si barjera ir apeit sia sistema, kad atrodytu viskas sklandziai.
            if (a != nangle) { // kampai neturi but vienodi.
                if (a <= pi && a > pi / 2) { // 90-180 didinam.
                    if (nangle < 0) { // ivyko suolis.
                        if (anglePlius){
                            jumpSize++; // perlipus barjera, musu viewingAngle tiesiog pakilo, todel pridedam.
                        }else {
                            jumpSize--; // minusiniam viewingAngle reik sumazint didinima.
                        }
                    }
                } else if (a >= -pi && a < -pi / 2) { // -90 - -180 mazinam
                    if (nangle > 0) { // perlipo barjera
                        if (anglePlius){
                            jumpSize--; // pliusinam viewingAngle mazinam
                        }else {
                            jumpSize++; // minusiniam didinam
                        }
                    }
                }
            }

            // sudedam suolius.
            if (anglePlius){
                while (jumpSize > 0){
                    nangle += 2*pi;
                    jumpSize--;
                }
            }else {
                while (jumpSize > 0){
                    nangle -= 2*pi;
                    jumpSize--;
                }
            }

        }
        angle = nangle;

        // kvieciam listener.
        if (angleSelectorListener != null)
            angleSelectorListener.angleChanged(old, angle);
    }

    /* sizing */

    @Override
    protected void autoSize() {
        if (xSize == 0 || ySize == 0){
            if (circle != null){ // pagal paveiksliuka. Bet jei nera isvis size.
                xSize = circle.getMinWidth();
                ySize = circle.getMinHeight();
            }
        }
    }

    /* vaizdas */

    @Override
    protected void isvaizda(float x, float y) {
        drawDrawable(circle, x, y, false); // piesiam circle.

        // piesiam musu pointeri pagal turima viewingAngle
        drawDrawable(pointer, x + xSize/2, y + ySize/2 - pointerThickness/2, xSize/2, pointerThickness,
                0, pointerThickness/2, angle * MathUtils.radiansToDegrees, false);
    }

    /* listener */

    public interface AngleSelectorListener{
        /** Called when viewingAngle is being changed. Angles are in radians.
         * @param old previous viewingAngle
         * @param current new selected viewingAngle*/
        void angleChanged(float old, float current);
    }

    /* style */

    @Override
    public AngleSelectorStyle getStyle() {
        AngleSelectorStyle st = new AngleSelectorStyle();
        copyStyle(st);
        return st;
    }

    public void readStyle(AngleSelectorStyle st){
        super.readStyle(st);
        // tiesiogiai drawable, kad null neignorintu.
        circle = st.circle;
        pointer = st.pointer;
        setAngle(st.viewingAngle);
        setPointerThickness(st.pointerThickness);
        enableMultiSpin(st.enableMultiSpin);
    }

    public void copyStyle(AngleSelectorStyle st){
        super.copyStyle(st);
        st.circle = getCircleDrawable();
        st.pointer = getPointerDrawable();
        st.viewingAngle = getAngle();
        st.pointerThickness = getPointerThickness();
        st.enableMultiSpin = isMultiSpinEnabled();
    }

    public static class AngleSelectorStyle extends ClickableStyle{
        /** viewingAngle in radians. */
        public float viewingAngle = 0;
        /** This is a background of viewingAngle selector. Usually this is normal circle. */
        public Drawable circle;
        /** This is an arrow which shows direction of viewingAngle. */
        public Drawable pointer;
        /** Thickness of pointer. */
        public float pointerThickness = 5;
        /** multi spin enables more then one rotation. Etc you spin two times and you will get 720 degrees viewingAngle. */
        public boolean enableMultiSpin = true;

        @Override
        public AngleSelector createInterface() {
            return new AngleSelector(this);
        }
    }
}
