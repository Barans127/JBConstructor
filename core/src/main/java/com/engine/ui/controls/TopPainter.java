package com.engine.ui.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.engine.core.Engine;
import com.engine.ui.listeners.MainDraw;

import java.util.ArrayList;

/** Drawing over everything else. */
public class TopPainter {
	private static TopPainter instance;

	// esami daiktai.
	private final ArrayList<MainDraw> tops = new ArrayList<>(10); // tiesiog leis piešt ant viršaus
	private final ArrayList<Boolean> topsPosition = new ArrayList<>(10);
	private final ArrayList<Inputs> inputs = new ArrayList<>(10); // leis kontroles būt pirmom.
	private final ArrayList<Toast> toast = new ArrayList<>(10); // toast.

	public static final int userDrop = 0;
	public static final int formChangeDrop = 1;
	public static final int popupShowUpDrop = 2;

	private TopPainter() {}

	/** initializes top painter instance. This is done automatically when engine is initialized. Call this if you need to use <code>TopPainter</code>
	 * before initializing Engine. */
	public static void initialize(){
		if (instance == null){
			instance = new TopPainter();
		}
		Engine p = Engine.getInstance();
		if (p != null) {
			Engine.getInstance().setTopper(instance);
		}
	}

	/**
	 * leis kontrolei gaut visus inputus pirmai, be jokių apribojimų (Tik input, nepiešimas).
	 * @param e kontrolė, kuriai bus leista gaut visus inputus.
     */
	public static void addInputsListener(Inputs e){
		if (e == null || instance.inputs.contains(e)){ // neprides to pacio ir null.
			return;
		}
		instance.inputs.add(e);
	}

	/**
	 * Pašalins kontrole iš sąrašo ir kontrolė nebegaus inputų pirma.
	 * @param e kontrolė, kuri bus pašalinta
	 * @return jeigu kontrolė buvo pašalinta true, false jei nebuvo jos sąraše.
     */
	public static boolean removeInputsListener(Inputs e){
		return instance.inputs.remove(e);
	}

	public static boolean containsMainDraw(MainDraw e){
		return instance.tops.contains(e);
	}

	public static boolean containsInputs(Inputs e){
		return instance.inputs.contains(e);
	}

	/** Pieš fixed valdymą. */
	public static void addPaintOnTop(MainDraw e){
		addPaintOnTop(e, true); // default piešia fixed vaizdą.
	}

	/**
	 * Leidžia piešt ant viršaus
	 * @param e MainDraw, kuris bus piešiamas
	 * @param isFixed true: pieš ant viršaus visko, false pieš prieš fixed piešimą.
     */
	public static void addPaintOnTop(MainDraw e, boolean isFixed){
		if (e == null){ // neprides null.
			return;
		}
//		for (MainDraw a : instance.tops){
//			if (e == a){
//				return;
//			}
//		}
        if (instance.tops.contains(e)){
            return; // no duplicates.
        }
		instance.tops.add(e);
		instance.topsPosition.add(isFixed);
		if (Gdx.graphics != null && !Gdx.graphics.isContinuousRendering())
			Gdx.graphics.requestRendering();
	}

	static synchronized void addToast(Toast e){
//		for (MainDraw a : toast){
//			if (e == a){
//				return;
//			}
//		}
		if (instance.toast.contains(e)){
			return; // nededam duplikatu
		}
		instance.toast.add(e);
		if (instance.toast.size() == 1){
			instance.toast.get(0).appear();
			if (!Gdx.graphics.isContinuousRendering())
				Gdx.graphics.requestRendering();
		}
	}

	static synchronized void removeToast(Toast e){
		instance.toast.remove(e);
		if (!instance.toast.isEmpty()){
//			((ToastOld) toast.get(0)).update();
			instance.toast.get(0).appear();
		}
	}

	public static boolean removeTopPaint(MainDraw e){
		for (int a= 0; a < instance.tops.size(); a++){
			if (instance.tops.get(a) == e){
				instance.topsPosition.remove(a);
				instance.tops.remove(a);
				return true;
			}
		}
		return false;
	}

	public void handle(){
		boolean request = false;
		for (int a = 0; a < tops.size(); a++){
			if (!topsPosition.get(a)) {
				tops.get(a).draw();
				request = true;
			}
		}
		if (!Gdx.graphics.isContinuousRendering() && request)
			Gdx.graphics.requestRendering();
	}

	public void fixHandle(){
		boolean request = false;
		for (int a = 0; a < tops.size(); a++){
			if (topsPosition.get(a)) {
				tops.get(a).draw();
				request = true;
			}
		}
		if (!toast.isEmpty()) {
			toast.get(0).draw();
			request = true;
		}
		if (!Gdx.graphics.isContinuousRendering() && request)
			Gdx.graphics.requestRendering();
	}

	/** Pašalins visus draw ir inputs. */
	public static void release(){
		release(userDrop);
	}

	public void formChange(){
		release(formChangeDrop);
	}

	static void release(int reason){
		instance.inputs.clear(); // inputs betkokiu atveju šalina. salinam pirmus, jei kartais tai surista su main draw tai convienent ji grazint.
		synchronized (instance.tops) {
			for (int a = instance.tops.size() - 1; a >= 0; a--) {
				if (!instance.tops.get(a).drop(reason)) { // pašalins tik tuos, kurie grąžina false.
					instance.tops.remove(a);
					instance.topsPosition.remove(a);
				}
			}
		}
//		tops.clear();
//		topsPosition.clear();
	}

	public void pinchStop() {
		for (int a = inputs.size()-1; a >= 0; a--){
			inputs.get(a).pinchStop();
		}
	}

	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		for (int a = inputs.size()-1; a >= 0; a--){
			Engine p = Engine.getInstance();
			Inputs k = inputs.get(a);
			Vector3 mouseCoords;
			int contrPos = -1;
			if (k instanceof Control){
				contrPos = ((Control) k).getController().getPositioning();
			}
			if (k.getPositioning() == Window.fixedView || (k.getPositioning() == Window.relativeView && contrPos == Window.fixedView)){
//				mouseCoords.set(initialPointer1, 0); // kiekvieno vectoriaus cord pakeitimas į fixed world kordinates.
				mouseCoords = p.screenToFixedCoords(initialPointer1.x, initialPointer1.y);
				initialPointer1.set(mouseCoords.x, mouseCoords.y);
				mouseCoords.set(initialPointer2, 0);
				p.screenToFixedCoords(mouseCoords);
				initialPointer2.set(mouseCoords.x, mouseCoords.y);
				mouseCoords.set(pointer1, 0);
				p.screenToFixedCoords(mouseCoords);
				pointer1.set(mouseCoords.x, mouseCoords.y);
				mouseCoords.set(pointer2, 0);
				p.screenToFixedCoords(mouseCoords);
				pointer2.set(mouseCoords.x, mouseCoords.y);
			}else{
//				mouseCoords.set(initialPointer1, 0); // kiekvieno vectoriaus cord pakeitimas į world kordinates.
				mouseCoords = p.screenToWorldCoords(initialPointer1.x, initialPointer1.y);
				initialPointer1.set(mouseCoords.x, mouseCoords.y);
				mouseCoords.set(initialPointer2, 0);
				p.screenToWorldCoords(mouseCoords);
				initialPointer2.set(mouseCoords.x, mouseCoords.y);
				mouseCoords.set(pointer1, 0);
				p.screenToWorldCoords(mouseCoords);
				pointer1.set(mouseCoords.x, mouseCoords.y);
				mouseCoords.set(pointer2, 0);
				p.screenToWorldCoords(mouseCoords);
				pointer2.set(mouseCoords.x, mouseCoords.y);
			}
			if (k.pinch(initialPointer1, initialPointer2, pointer1, pointer2)){
				return true;
			}
		}
		return false;
	}

	public boolean zoom(float initialDistance, float distance) {
		for (int a = inputs.size()-1; a >= 0; a--){
			if (inputs.get(a).zoom(initialDistance, distance)){
				return true;
			}
		}
		return false;
	}

	public boolean panStop(float x, float y, int pointer, int button) {
		for (int a = inputs.size()-1; a >= 0; a--){
			Inputs k = inputs.get(a);
			Vector3 e = realCoords(k, x, y);
			if (k.panStop(e.x, e.y, pointer, button)){
				return true;
			}
		}
		return false;
	}

	public boolean pan(float x, float y, float deltaX, float deltaY) {
		for (int a = inputs.size()-1; a >= 0; a--){
			Inputs k = inputs.get(a);
			Vector3 e = realCoords(k, x, y);
			if (k.pan(e.x, e.y, deltaX, deltaY)){
				return true;
			}
		}
		return false;
	}

	public boolean fling(float velocityX, float velocityY, int button) {
		for (int a = inputs.size()-1; a >= 0; a--){
			if (inputs.get(a).fling(velocityX, velocityY, button)){
				return true;
			}
		}
		return false;
	}

	public boolean longPress(float x, float y) {
		for (int a = inputs.size()-1; a >= 0; a--){
			Inputs k = inputs.get(a);
			Vector3 e = realCoords(k, x, y);
			if (k.longPress(e.x, e.y)){
				return true;
			}
		}
		return false;
	}

	public boolean tap(float x, float y, int count, int button) {
		for (int a = inputs.size()-1; a >= 0; a--){
			Inputs k = inputs.get(a);
			Vector3 e = realCoords(k, x, y);
			if (k.tap(e.x, e.y, count, button)){
				return true;
			}
		}
		return false;
	}

	public boolean scrolled(float amountX, float amountY) {
		for (int a = inputs.size()-1; a >= 0; a--){
			if (inputs.get(a).scrolled(amountX, amountY)){
				return true;
			}
		}
		return false;
	}

	public boolean keyTyped(char e) {
		for (int a = inputs.size()-1; a >= 0; a--){
			if (inputs.get(a).keyTyped(e)){
				return true;
			}
		}
		return false;
	}

	public boolean mouseMoved(float x, float y) {
		for (int a = inputs.size()-1; a >= 0; a--){
			Inputs k = inputs.get(a);
			Vector3 e = realCoords(k, x, y);
			if (k.mouseMoved(e.x, e.y)){
				return true;
			}
		}
		return false;
	}

	public boolean keyUp(int keycode) {
		for (int a = inputs.size()-1; a >= 0; a--){
			if (inputs.get(a).keyUp(keycode)){
				return true;
			}
		}
		return false;
	}

	public boolean keyDown(int keycode) {
		for (int a = inputs.size()-1; a >= 0; a--){
			if (inputs.get(a).keyDown(keycode)){
				return true;
			}
		}
		return false;
	}

	public boolean touchDown(float x, float y, int pointer, int button) {
		for (int a = inputs.size()-1; a >= 0; a--){
			Inputs k = inputs.get(a);
			Vector3 e = realCoords(k, x, y);
			if (k.touchDown(e.x, e.y, pointer, button)){
				return true;
			}
		}
		return false;
	}

	private Vector3 realCoords(Inputs e, float x, float y){
//		mouseCoords.set(screenX, screenY, 0);
		int contrPos = -1;
		if (e instanceof Control){
			contrPos = ((Control) e).getAbstractPositioning();
		}
		if (e.getPositioning() == Window.fixedView || (e.getPositioning() == Window.relativeView && contrPos == Window.fixedView)){
			return Engine.getInstance().screenToFixedCoords(x, y);
		}else{
			return Engine.getInstance().screenToWorldCoords(x, y);
		}
	}
}
