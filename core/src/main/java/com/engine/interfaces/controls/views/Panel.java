package com.engine.interfaces.controls.views;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;
import com.engine.core.ErrorMenu;
import com.engine.interfaces.controls.Field;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.InterfacesController;
import com.engine.interfaces.controls.PanelHost;
import com.engine.interfaces.controls.Window;

/**
 * Panel can hold other interfaces in it.
 * Panel with absolute positioning cannot handle fixed controls. Panel with fixed positioning cannot handle absolute controls.
 * Controls with positioning that cannot be handled by panel will be changed to panel's current positioning.
 */
public class Panel extends Field {
    PanelHost host;

    public Panel(){
        this(new PanelStyle());
    }

    public Panel(PanelStyle style){
        this(style, new PanelHost(style.x, style.y, style.width, style.height));
    }

    Panel(PanelStyle style, PanelHost e){
        super(style);
//        Vector2 pos = getPosition();
        host = e;
//        host.setOwner(this);
        host.setDisabledColor(style.disabledColor);
        host.setBackground(style.background);
        host.tintBackground(style.tintBackground);
        host.setPosition(style.x, style.y); // nesusigaudo is interface.
        host.setOwner(this);
        setVisible(super.isVisible()); // nes nesusigaudo po super priskyrimo.
        setEnabled(super.isEnabled());
        recheckPositioning();
        for (InterfaceStyle st : style.controls){
            addControl(st.createInterface());
        }
    }
    public void tintBackground(int color){
        host.tintBackground(color);
    }

    public void addControl(Interface... e){
        for (Interface k : e){
            addControl(k);
        }
    }

    public void addControl(Interface e){
        if (e == this){
            Engine.getInstance().setError("Panel: Cannot add itself to controls list", ErrorMenu.ErrorType.ControlsError);
            return;
        }
        host.addControl(e);
    }

    /** adds control to specified index. If index out of bounds then adds control to the end of the list. */
    public void addControl(Interface e, int index){
        if (e == this){
            Engine.getInstance().setError("Panel: Cannot add itself to controls list", ErrorMenu.ErrorType.ControlsError);
            return;
        }
        host.addControl(e, index);
    }

    public void removeControl(Interface e){
        host.removeControl(e);
    }

    /** removes focus. */
    public void removeFocus(){
        if (isFocused()){
            v.removeFocus();
        }
    }

    @Override
    public float getWidth(){
        return host.getWidth();
    }

    @Override
    public float getHeight(){
        return host.getHeight();
    }

    /** set size width and height. */
    public void setSize(float xy){
        setSize(xy, xy);
    }

    @Override
    public void setSize(float width, float height){
        super.setSize(width, height);
        host.setSize(super.getWidth(), super.getHeight());
    }

    @Override
    public void setDisabledColor(int color){
        host.setDisabledColor(color);
    }

    public void setBackground(Drawable e){
        host.setBackground(e);
    }

    /** host for holding controls. */
    public PanelHost getHost(){
        return host;
    }

    private void recheckPositioning(){
        Window.Position pos = Window.Position.relative;
        if (getPositioning() == Window.relativeView){
            if (v != null) {
                switch (v.getPositioning()){
                    case Window.absoluteView:
                        pos = Window.Position.absolute;
                        break;
                    case Window.fixedView:
                        pos = Window.Position.fixed;
                        break;
                }
            }
//            host.setPositioning(pos);
        } else {
            switch (getPositioning()){
                case Window.absoluteView:
                    pos = Window.Position.absolute;
                    break;
                case Window.fixedView:
                    pos = Window.Position.fixed;
                    break;
            }
        }
        host.setPositioning(pos);
    }

    /* focus. */

    @Override
    protected void onFocus() {
        host.acquireFocus(); // suteikiam fokusa. jeigu ir nera kur, vistiek tures fokusa.
    }

    @Override
    protected void onLostFocus() {
        host.removeFocus(); // pametam fokusa.
    }

    /** Move focus inside panel. Usually used by {@link InterfacesController}. */
    @Override
    /* naudoja dar ir tab controle. Del to public. */
    public boolean moveFocus(boolean forward) {
        return host.changeFocus(forward); // bandom keist.
    }

    /* kitka. */

    @Override
    protected void onRemove() {
        host.onRemove();
    }

    @Override
    public void setPosition(float x, float y) {
        host.setPosition(x, y);
        super.setPosition(x, y);
    }

    @Override
    protected void giveCords(float x, float y) {
        super.giveCords(x, y);
        host.setPosition(x, y);
    }

    @Override
    public void setPositioning(Window.Position e) {
        super.setPositioning(e);
        recheckPositioning();
    }

    @Override
    public void setVisible(boolean visible) {
        host.setVisible(visible);
        super.setVisible(visible);
    }

    @Override
    public void setEnabled(boolean enable) {
        host.setEnabled(enable);
    }

    @Override
    public boolean isEnabled() {
        return host.isEnabled();
    }

//    @Override
//    public boolean isVisible() {
//        return host.isOpen();
//    }

    @Override
    public void setController(InterfacesController v) {
        super.setController(v);
        host.setController(v);
        recheckPositioning(); // bug kai paneli esancios controles, naudojancios toppainter negalejo gaut fixed input.
    }

    @Override
    protected void isvaizda(float x, float y) {
        host.setControllerOffset(v.getOffsetX(), v.getOffsetY());
        if (host.getPositioning() == Window.absoluteView){
            host.handle();
        }else { // fixed tada tik lieka.
            host.fixHandle();
        }
    }

    @Override
    public void release() {
        super.release();
        host.release();
    }

    @Override
    protected void autoSize() {
//        sizeUpdated();
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return super.pan(x, y, deltaX, deltaY) || host.pan(x, y, deltaX, deltaY);
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return super.panStop(x, y, pointer, button) || host.panStop(x, y, pointer, button);
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return super.tap(x, y, count, button) || host.tap(x, y, count, button);
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return super.touchDown(x, y, pointer, button) || host.touchDown(x, y, pointer, button);
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return super.fling(velocityX, velocityY, button) || host.fling(velocityX, velocityY, button);
    }

    @Override
    public void pinchStop() {
        super.pinchStop();
        host.pinchStop();
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return super.pinch(initialPointer1, initialPointer2, pointer1, pointer2) ||
                host.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return super.zoom(initialDistance, distance) || host.zoom(initialDistance, distance);
    }

    @Override
    public boolean longPress(float x, float y) {
        return super.longPress(x, y) || host.longPress(x, y);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return super.scrolled(amountX, amountY) || host.scrolled(amountX, amountY);
    }

    @Override
    public boolean keyTyped(char e) {
        return super.keyTyped(e) || host.keyTyped(e);
    }

    @Override
    public boolean mouseMoved(float x, float y) {
        return super.mouseMoved(x, y) || host.mouseMoved(x, y);
    }

    @Override
    public boolean keyUp(int keycode) {
        return super.keyUp(keycode) || host.keyUp(keycode);
    }

    @Override
    public boolean keyDown(int keycode) {
        return super.keyDown(keycode) || host.keyDown(keycode);
    }

//    @Override
//    protected boolean allowChangeFocus(int way) {
//        return host.changeFocus(way);
//    }

    /* style */

    @Override
    public PanelStyle getStyle() {
        PanelStyle e = new PanelStyle();
        copyStyle(e);
        return e;
    }

    public void copyStyle(PanelStyle st){
        super.copyStyle(st);
        st.background = getHost().getBackground();
        st.tintBackground = getHost().getTintBackground();
        st.controls.clear(); // kadangi copy darom, tai padarom svaru pries dedant kontroles.
        for (Interface e : getHost().getControls()){
            st.controls.add(e.getStyle());
        }
    }

    public void readStyle(PanelStyle st){
        super.readStyle(st);
        setBackground(st.background);
        tintBackground(st.tintBackground);
        for (InterfaceStyle e : st.controls){
            addControl(e.createInterface());
        }
    }

    public static class PanelStyle extends FieldStyle{
        public Drawable background = null; // no barBackground
        public int tintBackground = 0xFFFFFFFF; // white
        /** add style, and interface with given style will be created with panel. */
        public final Array<InterfaceStyle> controls = new Array<>();
//        public int disabledColor = GdxPongy.color(100, 100, 100, 204); // papilkejus

        public PanelStyle(){
            width = 400;
            height = 400;
            disabledColor = Engine.color(100, 100, 100, 204);
        }

        @Override
        public Panel createInterface() {
            return new Panel(this);
        }
    }
}
