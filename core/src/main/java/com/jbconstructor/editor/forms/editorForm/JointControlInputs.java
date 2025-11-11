package com.jbconstructor.editor.forms.editorForm;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.engine.core.MoreUtils;
import com.engine.ui.controls.Inputs;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.Window;
import com.jbconstructor.editor.editors.ChainEdging;
import com.jbconstructor.editor.root.Element;
import com.jbconstructor.editor.root.PhysicsHolder;
import com.jbconstructor.editor.root.SelectiveInterfaceController;

public class JointControlInputs implements Inputs, SelectiveInterfaceController.SelectedInterfaceListener {
    private EditForm editForm;
    private JointControlPanel controlPanel;
    private JointControlPointPanel pointPanel;
    // mode, ka butent turi daryt.
    /*
    0 - bet koks body pasirinkimas. A body.
    1 - bet koks body pasirinkimas. B body.
     */
    private int mode;

    private boolean bodyChooseOn = false;
    private boolean pointTracking = false, touchedWhileTracking = false;

    public JointControlInputs(EditForm editForm, JointControlPanel controlPanel){
        this.editForm = editForm;
        this.controlPanel = controlPanel;
    }

    void setPointPanel(JointControlPointPanel pointPanel){
        this.pointPanel = pointPanel;
    }

    void changeMode(int mode){
        this.mode = mode;
        switch (mode){
            case 0: // a body pasirinkimas
            case 1: // b body pasirinkimas.
                bodyChooseOn = true;
                pointTracking = false;
                touchedWhileTracking = false;
                break;
            case 2: // point tracking
                pointTracking = true;
                bodyChooseOn = false;
                touchedWhileTracking = false;
                break;
            default:
                release();
                break;
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (editForm.getMover().isVisible()){ // nieko nedarom jei moveris enablintas, lai dirba savo darba.
            // siaip keicia mover enablinima, bet del kazkokio sumastymo enablinimas overdintas ir keiciamas visibility!!!
            // NETIKRINK ENABLINIMO!!! ANAS VISADA TRUE!
            return false;
        }
        if (bodyChooseOn){
            ChainEdging edging = editForm.getChainEdging();
            for (ChainEdging.Chains e : edging.getChains()){
                if (e.x.size > 1) { // pats chain turi turet bent du taskus, kad linija sudaryt.
                    for (int a = 0; a < e.x.size; a++) {
                        float ax = e.x.get(a);
                        float ay = e.y.get(a);
                        float bx, by;
                        if (a + 1 < e.x.size) { // paziurim ar yra sekantis taskas. arba ar chain loop.
                            bx = e.x.get(a+1); // imam sekanti taska
                            by = e.y.get(a+1);
                        } else if (e.loop) {
                            // imam pradini taska. loop kilpa darom.
                            bx = e.x.get(0);
                            by = e.y.get(0);
                        }else {
                            break;
                        }
                        if (MoreUtils.isLineCrossingCircle(ax, ay, bx, by, x, y, 7)){ // 7?
                            // pasirinko butent si kuna.
                            controlPanel.bodySelected(mode == 0, e.name);
                            bodyChooseOn = false;
                            return true;
                        }
                    }
                }
            }
        }else if (pointTracking && button == Input.Buttons.LEFT){
            touchedWhileTracking = true;
            pointPanel.pointChangedPlace(x, y);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean mouseMoved(float x, float y) {
        return false;
    }

    @Override
    public boolean keyTyped(char e) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (pointTracking && touchedWhileTracking){
            touchedWhileTracking = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (pointTracking && touchedWhileTracking){
            pointPanel.pointChangedPlace(x, y);
            return true;
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (pointTracking && touchedWhileTracking){
            pointPanel.pointChangedPlace(x, y);
            touchedWhileTracking = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

    @Override
    public void release() {
        bodyChooseOn = false;
        pointTracking = false;
        touchedWhileTracking = false;
    }

    @Override
    public int getPositioning() {
        return Window.absoluteView;
    }

    @Override
    public void interfaceSelected(Control e) {
        if (bodyChooseOn) { // rinksimes tik tuo atveju, jeigu tai ijungta.
            if (e instanceof Element) {
                PhysicsHolder physicsHolder = ((Element) e).getPhysicsHolder();
                if (physicsHolder.hasShapes()) {
                    controlPanel.bodySelected(mode == 0, e.getIdName()); // pasirinktas kunas.
                    bodyChooseOn = false; // pats issijungs.
                }
            }
        }
    }
}
