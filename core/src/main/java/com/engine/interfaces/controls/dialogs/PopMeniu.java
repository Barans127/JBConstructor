package com.engine.interfaces.controls.dialogs;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.engine.core.ErrorMenu;
import com.engine.interfaces.controls.Balloon;
import com.engine.interfaces.controls.Form;
import com.engine.interfaces.controls.Inputs;
import com.engine.interfaces.controls.InterfacesController;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.views.Panel;
import com.engine.root.GdxPongy;

/** This class allows to call balloon with interfaces in it. */
public class PopMeniu extends Balloon implements Inputs {
    private Panel controller;
//    private boolean lastReact;
    private Form form; // used to determine reaction.

    public PopMeniu(){
        this(new Panel.PanelStyle());
    }

    public PopMeniu(Panel.PanelStyle st){
        this(new Panel(st));
    }

    public PopMeniu(Panel controller){
        InterfacesController v = new InterfacesController(); // reikalingas paneliui. daugiau nereik.
        if (controller == null){
            GdxPongy.getInstance().setError("PopMeniu: Panel cannot be null", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        this.controller = controller;
        v.addControl(controller);
        controller.setPositioning(Window.Position.fixed); // o kam?
        setPosition(controller.getPosition()); // sugaudom dydi is panelio.
        setSize(controller.getWidth(), controller.getHeight());
    }

    /* bisk gavimo metodu. */

    public Panel getController() {
        return controller;
    }

    /* override metodai, controlerio valdymas pagal balloon normas. */

    @Override
    public void setSize(float width, float height) {
        controller.setSize(width, height);
        super.setSize(width, height);
    }

    @Override
    public void setBalloon(float x, float y, float width, float height) {
        controller.setPosition(x, y);
        controller.setSize(width, height);
        super.setBalloon(x, y, width, height);
    }

    @Override
    public void setPosition(float x, float y) {
        controller.setPosition(x, y);
        super.setPosition(x, y);
    }

    @Override
    protected void sizeRearranged(float width, float height, float lostWidth, float lostHeight) {
        controller.setSize(width, height);
    }

    @Override
    protected void positionRearranged(float x, float y, float offsetX, float offsetY) {
        controller.setPosition(x, y);
    }

    @Override
    protected void onShow() {
        Window form = GdxPongy.getInstance().getActiveForm();
        if (form instanceof Form){
            ((Form) form).addOnTop(this);
            this.form = (Form) form;
        }
        TopPainter.addInputsListener(this);
    }

    @Override
    protected void onHide() {
//        controller.getHost().disableFocus(true);
        controller.getHost().removeFocus();
        Window form = GdxPongy.getInstance().getActiveForm();
        if (form instanceof Form){
            ((Form) form).removeTopItem(this);
        }
        TopPainter.removeInputsListener(this);
    }

    /* implemented methods. */

    @Override
    protected void draw(float x, float y, float width, float height, float offsetX, float offsetY) {
        controller.handle();
//        Engine p = GdxPongy.getInstance();
//        p.noFill();
//        p.stroke(0);
//        p.strokeWeight(3f);
//        p.rect(x, y, width,height);

    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (form != null && form.getTopItem() != this){
            return false;
        }
        if (controller.touchDown(x, y, pointer, button)){
            return true;
        }
        Vector2 pos = getPosition();
        if (x < pos.x || x > pos.x + getWidth() || y < pos.y || y > pos.y+getHeight()){
            hide(); // paspaudus uz ribu uzsidarys
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (form != null && form.getTopItem() != this){
            return false;
        }
        if (controller.keyDown(keycode)){
            return true;
        }else if (keycode == Input.Keys.ESCAPE){
            hide();
            return true;
        }else
            return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return (form == null || form.getTopItem() == this) && controller.keyUp(keycode);
    }

    @Override
    public boolean mouseMoved(float x, float y) {
        return (form == null || form.getTopItem() == this) && controller.mouseMoved(x, y);
    }

    @Override
    public boolean keyTyped(char e) {
        return (form == null || form.getTopItem() == this) && controller.keyTyped(e);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return (form == null || form.getTopItem() == this) && controller.scrolled(amountX, amountY);
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return (form == null || form.getTopItem() == this) && controller.tap(x, y, count, button);
    }

    @Override
    public boolean longPress(float x, float y) {
        return (form == null || form.getTopItem() == this) && controller.longPress(x, y);
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return (form == null || form.getTopItem() == this) && controller.fling(velocityX, velocityY, button);
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return (form == null || form.getTopItem() == this) && controller.pan(x, y, deltaX, deltaY);
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return (form == null || form.getTopItem() == this) && controller.panStop(x, y, pointer, button);
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return (form == null || form.getTopItem() == this) && controller.zoom(initialDistance, distance);
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return (form == null || form.getTopItem() == this) && controller.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
    }

    @Override
    public void pinchStop() {
        if (form != null && form.getTopItem() != this){
            return;
        }
        controller.pinchStop();
    }

    @Override
    public void release() {
        controller.release();
    }

    @Override
    public int getPositioning() {
        return Window.fixedView; // Pop meniu tik fixed bus.
    }
}
