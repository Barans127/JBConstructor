package com.engine.interfaces.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;
import com.engine.jbconstructor.Template;

/**
 * Meniu langas. Klasė kuri valdo visas kontroles. (Buttons, textInput, Pop up ir t.t.).
 */
public abstract class Form implements Window {
    private InterfacesController v; // default controller.
//    private ArrayList<PopUp> exist; // laiko pop up. kad nedingtu.
    private Array<PopUp> show; // dabar ant vaizdo esantys pop up
    private Array<Object> somethingOnTop; // top holder. leis disablint viska ant virsaus.
    /** Those holds camera previous position before form was hidden. It will not hold previous position if form change was silent. */
    private float cameraX, cameraY, cameraZoom;
    private int backgroundColor = 255; // white

    private Template formsTemplate; // template from jbc constructor.

    // formos id. Id nustato engine arba user.
    private String id;

    /** set custom main interface controller. */
    public Form(InterfacesController v){
        if (v==null)
            this.v = new InterfacesController();
        else
            this.v = v;
        this.v.setForm(this);
//        exist = new ArrayList<>();
        show = new Array<>(10);
        somethingOnTop = new Array<>();
//        OrthographicCamera cam = GdxPongy.getInstance().getAbsoluteCamera(); // atsimint kokia buvo busena pries uzdarant langa.
        Engine p = Engine.getInstance();
        cameraX = p.getPreferredScreenWidth()/2; // default. pradiniai tegul buna
        cameraY = p.getPreferredScreenHeight()/2;
        cameraZoom = 1f;
    }

    /** Form with default interface Controller. */
    public Form() {
        this(new InterfacesController());
    }

    /** Template for this form. Template can be created with JBConstructor.
     * template has interface info, some resources info and also physics polygons info.
     * Template drawing occurs after {@link #background()} method and before all interfaces drawing.*/
    public void setFormsTemplate(Template e){
        if (formsTemplate == null) {
            formsTemplate = e;
            if (e != null){
                setBackgroundColor(e.getBackgroundColor());
            }
        }else { // toks tarsi ispejimas, bet ne klaida.
//            System.out.println("Form already has defined template. Remove old template before adding new one.");
            Gdx.app.log("Form", "Form already has defined template. Remove old template before adding new one.");
        }
    }

    /* formos id suteikimas, gavimas */

    /** Id of this form. You shouldn't set it yourself as this id is set when form is added to Engine list.
     * Engine sets this value same as key which is set when form is added to Engine list with method {@link Engine#addForm(Window, String)}*/
    public void setFormId(String id){
        this.id = id;
    }

    /** Id of this form. This id is automatically set when form is added to Engine list.
     * @return id of this form. Use this id to switch to this form. Null if form was not added to Engine list or was not set. */
    public String getFormId(){
        return id;
    }

    /* JBConstructor template idejimas */

    /** @return this forms template. If template was not set then null is returned. */
    public Template getFormsTemplate(){
        return formsTemplate;
    }

    /** removes template from this form. Interfaces will be removed and entities will not be drawn anymore.
     * @param destroyPhysicsBodies destroys bodies from physics world. if false then bodies will not be destroyed. */
    public void removeFormsTemplate(boolean destroyPhysicsBodies){
        if (formsTemplate != null){
            for (Interface e : formsTemplate.getInterfaces()){
                removeControl(e);
            }
            if (destroyPhysicsBodies){ // istrins viska is world.
                formsTemplate.destroyPhysicsBodies();
            }
            formsTemplate = null;
        }
    }

    /* handlinimas, piesimas */

    public final void handle() {
        background();
        if (formsTemplate != null){
            formsTemplate.handle();
        }
        v.handle();
        for (int a = 0; a < show.size; a++) {
            show.get(a).handle();
        }
        lastDraw();
    }

    public final void fixHandle() {
        fixBackground();
        if (formsTemplate != null)
            formsTemplate.fixHandle();
        v.fixHandle();
        for (int a = 0; a < show.size; a++) {
            show.get(a).fixHandle();
        }
        fixedLastDraw();
    }

//    // nenaudojamas!!
//    @Deprecated
//    public void recalculateRez() { // visai formai rezoliucijos pakeitimas
//        v.recalculateRez();
//        for (int a = 0; a < exist.size(); a++) {
//            exist.get(a).recalculateRez();
//        }
//    }

    protected void background() {
        Engine.getInstance().background(backgroundColor);
    }

    protected void fixBackground() {
    }

    /** Called after all drawings */
    protected void lastDraw(){

    }

    /** Called after all drawings */
    protected void fixedLastDraw(){

    }

//    public synchronized boolean addPopUp(PopUp a) {
//        if (a instanceof PanelHost) {
//            System.out.println("Failed adding Pop up. You cannot add " + a.getClass().getSimpleName() + " as pop up.");
//            return false;
//        }
//        for (PopUp e : exist) {
//            if (e == a) {
//                System.out.println("Pop up is already in form");
//                return false;
//            }
//        }
//        exist.add(a);
//        a.setForm(this);
//        if (a.isOpen()) {
//            show.add(a);
//        }
//        return true;
//    }

//    public boolean removePopUp(PopUp a){
//        showPopUp(a, false); // isjunkt jei matomas
//        a.setForm(null);
//        return exist.remove(a); // istrint is saraso.
//    }

    /* kontroliu valdymas. */

    public boolean addControl(Interface a) {
        return v.addControl(a);
    }

    public void addControl(Interface... a) {
        for (Interface k : a) {
            addControl(k);
        }
    }

    public boolean addControl(Interface e, int index){
        return v.addControl(e, index);
    }

    public void removeControl(Interface... e) {
        for (Interface k : e) {
            removeControl(k);
        }
    }

    public void removeControl(Interface e) {
        v.removeControl(e);
    }

    /* pozicija */

    /**
     * Nustato pozicija, negalioja Pop up.
     *
     * @param e pozicija, kuria naudos default interfaceController.
     */
    public void setPositioning(Position e) {
        v.setPositioning(e);
    }

    /**
     * pop up iskvietus visa kita disablina.
     * kvieciama kai pop up tapo visible arba nematomu.
     */
    void showPopUp(PopUp a, boolean show) {
        boolean success;
        if (show) {
            if (this.show.contains(a, false)){
                success = false; // jau idetas i forma.
            }else {
                this.show.add(a);
                success = true;
            }
        } else {
            success = this.show.removeValue(a, true);
        }
        if (!success){ // jeigu pvz pop up jau yra pridetas arba nebuvo pasalintas, tai nieko nedaryt, nes niekas nesikeite.
            return;
        }
        TopPainter.release(TopPainter.popupShowUpDrop); // kad nebugintu controles su toppaintais.
        int size = this.show.size;
        if (size > 0) {
            v.setAction(false);
            this.show.get(size - 1).setAction(true); // leis naudotis
            if (size > 1) { // uzdraus pries tai esancius popUp'us. Leis tik dabartini.
                for (int aa = 0; aa < size - 1; aa++) {
                    this.show.get(aa).setAction(false);
                }
            }
        } else {
            v.setAction(true);
        }
//		mouseMoved();
    }

    /* parametrai */

    /** This color is used in {@link #background()} method. ARGB format */
    public int getBackgroundColor(){
        return backgroundColor;
    }

    /** ARGB format. This color is used in {@link #background()} method. */
    public void setBackgroundColor(int color){
        backgroundColor = color;
    }

    /** main interface controller */
    public InterfacesController getController() {
        return v;
    }

    /** Save camera x position. This position is used to bring back camera to last known position when forms were switched. */
    public float getSavedCameraX(){
        return cameraX;
    }

    /** Save camera y position. This position is used to bring back camera to last known position when forms were switched. */
    public float getSavedCameraY(){
        return cameraY;
    }

    /** Save camera zoom. This parameter is used to set camera zoom to previous zoom when forms were switched. */
    public float getSavedCameraZoom(){
        return cameraZoom;
    }

    /** set custom saved camera parameters. This parameters are used when form is shown up. */
    public void setCustomCameraSettings(float cameraX, float cameraY, float cameraZoom){
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZoom = cameraZoom;
    }

    /** is any pop up visible on this form. */
    public boolean isPopUpShown(){
        return show.size > 0;
    }

    /** Current visible pop ups on this form. */
    public Array<PopUp> getActivePopUps(){
        return show;
    }

    /** true if this form is currently active form. */
    public boolean isActiveForm(){
        return Engine.getInstance().getActiveForm() == this;
    }

    /** Closes all pop ups which are opened on this form. If no pop up is opened than this method does nothing. */
    public void closeAllPopUps(){
        for (int a = show.size-1; a >= 0; a--){
            show.get(a).close();
        }
    }

//    /** return all pop ups which are added to this form. */
//    public List<PopUp> getPopUpList(){
//        return exist;
//    }

//	public final boolean onPress(int m, int k){
//		if (show.size > 0){
//			return show.get(show.size-1).onPress(m, k);
//		}else if (v.isEnabled()){
//			if (m != -1){
//				if (onMousePress(m)){
//					return true;
//				}
//			}else if (k != -1){
//				if (onKeyPress(k)){
//					return true;
//				}
//			}
//		}
//		return v.onPress(m, k);
//	}
//
//	public final boolean onRelease(int m, int k){
//		if (show.size > 0){
//			return show.get(show.size-1).onRelease(m, k);
//		}else if (v.isEnabled()){
//			if (m != -1){
//				if (onMouseRelease(m)){
//					return true;
//				}
//			}else if (k != -1){
//				if (onKeyRelease(k)){
//					return true;
//				}
//			}
//		}
//		return v.onRelease(m, k);
//	}

    /* inputs*/

    @Override
    public final boolean mouseMoved(float x, float y) {
        if (beforeMouseMove(x, y))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).mouseMoved(x, y);
        }
        return v.mouseMoved(x, y) || afterMouseMove(x, y);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforeMouseMove(float x, float y) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterMouseMove(float x, float y){
        return false;
    }

    @Override
    public final boolean tap(float x, float y, int count, int button) {
        if (beforeTap(x, y, count, button))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).tap(x, y, count, button);
        }
        return v.tap(x, y, count, button) || afterTap(x, y, count, button);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforeTap(float x, float y, int count, int button) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterTap(float x, float y, int count, int button){
        return false;
    }

    @Override
    public final boolean longPress(float x, float y) {
        if (beforeLongPress(x, y))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).longPress(x, y);
        }
        return v.longPress(x, y) || afterLongPress(x, y);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforeLongPress(float x, float y) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterLongPress(float x, float y){
        return false;
    }

    @Override
    public final boolean fling(float velocityX, float velocityY, int button) {
        if (beforeFling(velocityX, velocityY, button))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).fling(velocityX, velocityY, button);
        }
        return v.fling(velocityX, velocityY, button) || afterFling(velocityX, velocityY, button);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforeFling(float velocityX, float velocityY, int button) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterFling(float velocityX, float velocityY, int button){
        return false;
    }

    @Override
    public final boolean pan(float x, float y, float deltaX, float deltaY) {
        if (beforePan(x, y, deltaX, deltaY))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).pan(x, y, deltaX, deltaY);
        }
        return v.pan(x, y, deltaX, deltaY) || afterPan(x, y, deltaX, deltaY);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforePan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterPan(float x, float y, float deltaX, float deltaY){
        return false;
    }

    @Override
    public final boolean panStop(float x, float y, int pointer, int button) {
        if (beforePanStop(x, y, pointer, button))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).panStop(x, y, pointer, button);
        }
        return v.panStop(x, y, pointer, button) || afterPanStop(x, y, pointer, button);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforePanStop(float x, float y, int pointer, int button) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterPanStop(float x, float y, int pointer, int button){
        return false;
    }

    @Override
    public final boolean zoom(float initialDistance, float distance) {
        if (beforeZoom(initialDistance, distance))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).zoom(initialDistance, distance);
        }
        return v.zoom(initialDistance, distance) || afterZoom(initialDistance, distance);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforeZoom(float initialDistance, float distance) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterZoom(float initialDistance, float distance){
        return false;
    }

    @Override
    public final boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        if (beforePinch(initialPointer1, initialPointer2, pointer1, pointer2))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).pinch(initialPointer1, initialPointer2, pointer1, pointer2);
        }
        return v.pinch(initialPointer1, initialPointer2, pointer1, pointer2) ||
                afterPinch(initialPointer1, initialPointer2, pointer1, pointer2);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforePinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterPinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2){
        return false;
    }

    @Override
    public final void pinchStop() {
        onPinchStop();
        if (show.size > 0) {
            show.get(show.size - 1).pinchStop();
            return;
        }
        v.pinchStop();
    }

    /** Called when pinch stop event occured. */
    protected void onPinchStop() {
    }

    /* forms reaction */

    /** determines that this object is on top now. Form will no longer react to any input. */
    public void addOnTop(Object item){
        if (item != null){
            somethingOnTop.add(item);
            setAction(false);
        }
    }

    /** if object was on top, than this object is removed and if there are no more items on top then this form will start react again. */
    public void removeTopItem(Object item){
        if (item != null && somethingOnTop.contains(item, false)){
            somethingOnTop.removeValue(item, false);
            setAction(somethingOnTop.size == 0);
        }
    }

    /** @return the most top item which prevents form from inputs (Inputs are handled in {@link InterfacesController} and not form).
     *  If there are no items on top null is returned. */
    public Object getTopItem(){
        if (somethingOnTop.size > 0)
            return somethingOnTop.get(somethingOnTop.size-1);
        return null;
    }

    /** @return array which has all top items. if there is no items on top then zero length array is returned. */
    public Array<Object> getAllTopItems() {
        return somethingOnTop;
    }

    private void setAction(boolean act) { // ant virsaus, leis viska uzdisablint...
        if (show.size == 0) {
            v.setAction(act);
        } else {
            show.get(show.size - 1).setAction(act);
        }
    }

    /** tells if form is reacting on any input or not. */
    public boolean isEnabled(){
        if (show.size == 0){
            return v.isActive();
        }else {
            return show.get(show.size - 1).isActive();
        }
    }

//	@Override
//	public boolean touchDragged(int mx, int my, int pointer){
//		if (show.size > 0){
//			return show.get(show.size-1).mouseDragged(mx, my, pointer);
//		}
//		return v.mouseDragged(mx, my, pointer);
//	}

    @Override
    public final boolean keyTyped(char e) {
        if (beforeKeyTyped(e))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).keyTyped(e);
        }
        return v.keyTyped(e) || afterKeyTyped(e);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforeKeyTyped(char e) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterKeyTyped(char e){
        return false;
    }

    @Override
    public final void show() {
        OrthographicCamera camera = Engine.getInstance().getAbsoluteCamera();
        boolean update = false; // susigaudom ar kas nors pasikeite
        if (camera.position.x != cameraX){
            camera.position.x = cameraX;
            update = true;
        }
        if (camera.position.y != cameraY){
            camera.position.y = cameraY;
            update = true;
        }
        if (camera.zoom != cameraZoom){
            camera.zoom = cameraZoom;
            update = true;
        }
        if (update){ // jei pasikeite keiciam i pirmine padeeti.
            camera.update();
        }
        onShow();
    }

    @Override
    public final void hide() {
        OrthographicCamera cam = Engine.getInstance().getAbsoluteCamera(); // atsimint kokia buvo busena pries uzdarant langa.
        cameraX = cam.position.x;
        cameraY = cam.position.y;
        cameraZoom = cam.zoom;
        onHide();
    }

    /** called when form change occurs and this form is shown. will not be shown if form change is silent. */
    protected void onShow(){}

    /** called when form change occurs and this form is hidden. will not be shown if form change is silent. */
    protected void onHide(){}

    public void release() {
        v.release();
        for (PopUp e : show) {
            e.release();
        }
    }

    @Override
    public final boolean touchDown(float x, float y, int pointer, int button) {
        if (beforeTouchDown(x, y, pointer, button))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).touchDown(x, y, pointer, button);
        }
        return v.touchDown(x, y, pointer, button) || afterTouchDown(x, y, pointer, button);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforeTouchDown(float x, float y, int pointer, int button) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterTouchDown(float x, float y, int pointer, int button){
        return false;
    }

    @Override
    public final boolean keyDown(int keycode) {
        if (beforeKeyDown(keycode))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).keyDown(keycode);
        }
        return v.keyDown(keycode) || afterKeyDown(keycode);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforeKeyDown(int keycode) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterKeyDown(int keycode){
        return false;
    }

    @Override
    public final boolean keyUp(int keycode) {
        if (beforeKeyUp(keycode))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).keyUp(keycode);
        }
        return v.keyUp(keycode) || afterKeyUp(keycode);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforeKeyUp(int keycode) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterKeyUp(int keycode){
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (beforeScrolled(amountX, amountY))
            return true;
        if (show.size > 0) {
            return show.get(show.size - 1).scrolled(amountX, amountY);
        }
        return v.scrolled(amountX, amountY) || afterScrolled(amountX, amountY);
    }

    /** This will be called before pop up or interface controller events.
     * NOTE: this method will be called even if inputs are blocked (see more in method {@link #addOnTop(Object)}. To check if inputs are blocked use
     * {@link #isEnabled()} method.
     * @return true if input should not reach pop up or interface controller input events. */
    protected boolean beforeScrolled(float amountX, float amountY) {
        return false;
    }

    /** This will be called after default {@link InterfacesController} events if they return false (no buttons reactions etc).
     * NOTE: this method will not be called if {@link PopUp} is open on this form.*/
    protected boolean afterScrolled(float amountX, float amountY) {
        return false;
    }
}
