package com.engine.ui.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.engine.ui.listeners.ClickListener;
import com.engine.ui.listeners.PressedListener;
import com.engine.ui.listeners.inputListeners.RightClickListener;

/**
 * Lengvai implementina paspaudimus, pasako, kada buvo paliesta ant kontrolės ir kada atleista.
 */
public abstract class Clickable extends Field {
    // veikiamieji.
    private boolean pressed, rightPressed;
    protected int button;
    private int pointer; // nustatyt, kuris pirstas liecia ekrana.

    // uzdisablint paspaudimus.
    private boolean unClickable;

    // listeners
    private ClickListener clickListener;
//    private LongPressListener longPressListener;
    private PressedListener pressedListener;
    private RightClickListener rightClickListener;

    // click garsai.
    private Sound pressedSound, releasedSound, clickSound;

    public Clickable(ClickableStyle style){ // Kad būtų galima normaliai style panaudot.
        super(style);

        button = style.button;
        unClickable = style.unClickable;
        pressedSound = style.pressedSound;
        releasedSound = style.releasedSound;
        clickSound = style.clickedSound;
    }

    /** imitates click if control is visible and is enabled and is clickable (see {@link #setUnClickable(boolean)}). */
    public void imitateClick(float x, float y){
        if (isEnabled() && isVisible() && !isUnClickable()) {
            pressed = true;
            pressed();
            onPress(x, y);
        }
    }

    /* getters setters */

    /** sets sound which is played when user presses and releases control. To disable sounds set values to null. */
    public void setPressedReleasedSound(Sound pressedSound, Sound releasedSound){
        this.pressedSound = pressedSound;
        this.releasedSound = releasedSound;
    }

    /** Set sound which will be played when user clicks on this control. To disable sound set value to null. */
    public void setClickSound(Sound clickSound){
        this.clickSound = clickSound;
    }

    /** Sound which is played when control is being pressed. */
    public Sound getPressedSound(){
        return pressedSound;
    }

    /** Sound which is played when control is being released. */
    public Sound getReleasedSound(){
        return releasedSound;
    }

    /** Sound which is played when control is was clicked. */
    public Sound getClickSound(){
        return clickSound;
    }

    /** If you don't want that this control handles any inputs, then you can use this method.
     * Control will ignore all inputs. Controls under this control will be able to catch inputs to prevent that you should just disable this control
     * with {@link #setEnabled(boolean)}. */
    public void setUnClickable(boolean unClickable){
        this.unClickable = unClickable;
    }

    /** is this control ignoring inputs. */
    public boolean isUnClickable(){
        return unClickable;
    }

    /** is this control pressed. */
    public boolean isPressed() {
        return pressed;
    }

    /** which mouse button should press this control. On touch devices this should be left button or else control will no react. */
    public void setButton(int button){
        this.button = button;
    }

    public void setClickListener(ClickListener e){
        clickListener = e;
    }

//    public void setLongPressListener(LongPressListener e){
//        longPressListener = e;
//    }

    public void setPressedListener(PressedListener e){
        pressedListener = e;
    }

    public void setRightClickListener(RightClickListener e){
        rightClickListener = e;
    }

    public RightClickListener getRightClickListener() {
        return rightClickListener;
    }

    public ClickListener getClickListener() {
        return clickListener;
    }

//    public LongPressListener getLongPressListener() {
//        return longPressListener;
//    }

    public PressedListener getPressedListener() {
        return pressedListener;
    }

    /* Inputs. */

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (super.tap(x, y, count, button))
            return true;
        if (button == Input.Buttons.RIGHT && rightPressed){
            rightPressed = false;
            if (rightClickListener != null){
                rightClickListener.rightClick(this, x, y, v.getOffsetX(), v.getOffsetY());
            }

            // leidziam atleidimo garsa.
            if (releasedSound != null){
                releasedSound.play();
            }
            return true;
        } else if (this.button == button && pressed) {
//            mouseEvents(x, y); // nuo touch down turėtų nesiskirt.
            pressed = false;
            if (isEnabled()) {
                if (isMouseIn()) {
                    onClick();

                    // leidziam click sound.
                    if (clickSound != null){
                        clickSound.play();
                    }
                }
                onRelease(x, y);

                // leidziam atleidimo garsa.
                if (releasedSound != null){
                    releasedSound.play();
                }
            }
            release();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (super.touchDown(x, y, pointer, button))
            return true;
//        if (pointer > 0 && !Gdx.input.isTouched()) { // wtf????!
//            return false;
//        }
        // jeigu ijungta unclickable metodas, tai ignorinam paspaudimus. toliau visa kita automatiskai ignorinsis.
        if (unClickable){
            return false;
        }

        if (button == Input.Buttons.RIGHT && rightClickListener != null){ // desinys peles
            mouseEvents(x, y, false);
            if (isMouseIn()) { // nereik true grazinet, jei nera listener.
                rightPressed = true;
                getFocus(); // fokusuojam.

                // leidziam press sound.
                if (pressedSound != null){
                    pressedSound.play();
                }
                return true;
            }
        } else if (this.button == button) {
            mouseEvents(x, y, false);
            if (isMouseIn()) {
                this.pointer = pointer; // turim zinot, kuris pirstas liecia ekrana.
                pressed = true;
//                backgroundColor = pressedColor; // pakeis spalva arba tiesiog pozicija.
//                statusColor.set(backgroundColor);
                if (isEnabled()) { // event kvies jei tik enablinta.
                    getFocus();
                    pressed();
                    onPress(x, y);

                    // leidziam press sound.
                    if (pressedSound != null){
                        pressedSound.play();
                    }
                }else{
                    if (v != null){
                        v.removeFocus(); // kad vistiek pamestu fokusa, nors ir ant disable spaudzia.
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
//        mouseEvents(x, y); // nereikia, nes jis nuo touchDown nesiskira.
        if (pressed && onLongClick(x, y)) {
            pressed = false;
            if (isEnabled()) {
                onRelease(x, y);

                // leidziam atleidimo garsa.
                if (releasedSound != null){
                    releasedSound.play();
                }
            }
            release();
            return true;
        }
        return false;
    }

    @Override
    public void pinchStop() { // mobiliuosiuos nustatyt skirtingais pirstais ar lieciama.
        super.pinchStop();
        if (pressed && !Gdx.input.isTouched(pointer)){ // sitas leidzia multiple presses ant mygtuku keliu iskart su skirtingais pirstais.
            pressed = false;
            if (isEnabled()) {
                Vector2 pos = getPosition(); // reik kazkokiu cord, kadangi ju cia nera, tai desim tiesiog controles cord.
                onRelease(pos.x, pos.y);

                // leidziam atleidimo garsa.
                if (releasedSound != null){
                    releasedSound.play();
                }
            }
            release();
        }
    }

    //    @Override
//    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
//        return super.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
////        if (pressed) {
////            pressed = false;
////            if (isEnabled())
////                onRelease(pointer1.x, pointer1.y);
////            release();
//////            return true;
////        }
//    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (super.panStop(x, y, pointer, button))
            return true;
        if (rightPressed){
            rightPressed = false;
            mouseEvents(x, y, false);
            if (isMouseIn() && rightClickListener != null){
                rightClickListener.rightClick(this, x, y, v.getOffsetX(), v.getOffsetY());
            }

            // leidziam atleidimo garsa.
            if (releasedSound != null){
                releasedSound.play();
            }
            return true;
        } else if (pressed) {
            pressed = false;
            if (isEnabled()) {
                mouseEvents(x, y, false);
                if (isMouseIn()) {
                    onClick();

                    // leidziam click sound.
                    if (clickSound != null){
                        clickSound.play();
                    }
                }
                onRelease(x, y);

                // leidziam atleidimo garsa.
                if (releasedSound != null){
                    releasedSound.play();
                }
            }
            release();
            return true;
        }
        return false;
    }

//    @Override
//    public boolean pan(float x, float y, float deltaX, float deltaY) {
//        return pressed;
//    }

    @Override
    public void release() {
        if (pressed) {
            pressed = false;
            if (isEnabled()) {
                onRelease(-1, -1); // nes nebuvo atleidimo.

                // leidziam atleidimo garsa.
                if (releasedSound != null){
                    releasedSound.play();
                }
            }
        }
        rightPressed = false;
        super.release();
    }

    @Override
    public boolean mouseMoved(float x, float y) {
        if (super.mouseMoved(x, y))
            return true;
        if (unClickable){
            return false;
        }
        mouseEvents(x, y, true);
        return isMouseIn();
    }

    @Override
    protected void mouseEnter(){
        onStatus();
    }

    @Override
    protected void mouseLeave(){
        normalStatus();
    }

    /** Kai paspausta ir palaikytą, tam tikrą laiką. */
    protected boolean onLongClick(float x, float y) {
        return super.longPress(x, y);
    }

    /** Kai buvo paliesta kontrolė */
    protected void onPress(float x, float y){
        if (pressedListener != null){
            pressedListener.onPress(x, y);
        }
    }

    /** Kai atleido kontrole */
    protected void onRelease(float x, float y){
        if (pressedListener != null){
            pressedListener.onRelease(x, y);
        }
    }

    /** Kai buvo paspausta ant kontrolės */
    protected void onClick(){
        if (clickListener != null){
            clickListener.onClick();
        }
    }

    /* style */

    public void copyStyle(ClickableStyle st){
        super.copyStyle(st);
        st.button = button;
        st.clickedSound = clickSound;
        st.pressedSound = pressedSound;
        st.releasedSound = releasedSound;
        st.unClickable = unClickable;
    }

    public void readStyle(ClickableStyle st) {
        super.readStyle(st);
        setUnClickable(st.unClickable);
        setClickSound(st.clickedSound);
        setPressedReleasedSound(st.pressedSound, st.releasedSound);
    }

    public abstract static class ClickableStyle extends FieldStyle{
        /** main button. on android button is always left therefore this button should not be touched. */
        public int button = Input.Buttons.LEFT;
        /** Should this control catch inputs or ignore them. */
        public boolean unClickable = false;

        // garsai
        /** Sound will be played on events. null - no sound. By default there are no sounds. */
        public Sound pressedSound, releasedSound, clickedSound;
    }
}
