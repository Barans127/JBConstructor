package com.engine.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.engine.animations.FadeAway;
import com.engine.animations.SwitchAnimation;
import com.engine.core.ErrorMenu.ErrorType;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.controls.Window;
import com.engine.root.GdxPongy;

import java.util.HashMap;
import java.util.Set;

public class Engine extends GdxPongy { // Testas..

	// main kintamieji.
	private static Engine main;
	private int currentMenu; // zaidimo busena. 5 turi buti pradziai.
	//LibGdx camera viska sutvarko, nera reikalo dar uzkraut.
//	private static float SIZE = 1; // naudojama skirtingom rezoliucijom.
	private ErrorMenu err; // error handler

	// perjungimo animacija.
	private SwitchAnimation changer;

	// Meniu. siaip tai formos cia.
	private Window[] menu; // visi meniu
	private HashMap<String, Integer> menuMaps; // meniu ids.
	private TopPainter topper;

	// test
//	private float minFps = 60f, maxFps = 0f;
//	private float maxFps = 0f;
	private static boolean debug;
//	private int laikas;

	/* pagrindinis */

	public static boolean isDebug(){
		return debug;
	}

	/** @return this engine instance. */
	public static Engine getInstance(){
		return main;
	}

	/** initializes engine, prepares for work. Listener will be called when engine is ready. */
	public static Engine initialize(StartListener listener){
		if (main == null) {
			main = new Engine();
			IntroduceForm.setStartListener(listener);
		} else
//			main.setError("Too many initialize calls", ErrorMenu.ErrorType.UnknowError);
            throw new RuntimeException("Too many initialize calls");
		return main;
	}

	private Engine(){ } // nieks nekurs daugiau instanciju.

    /** Inside workings. Will do nothing. Ignore this method. */
	public void setTopper(TopPainter e){
		if (topper == null){
			topper = e;
		}
	}

	@Override
	public void setup() { // init
//		int laikas = millis();
		TopPainter.initialize();
//		err = new ErrorMenu(); // nereik jo uzkraut, uzkrausim tik kai reiks jo.
		debug = Resources.getProperty("debug", "0").equals("1");
//		err = new ErrorMenu(); // error langas...
		// kalbos uzkrovimas.
//		Resources.loadLang();
		changer = new FadeAway(); // perjungimo animacija. default
		menu = new Window[0];
		menuMaps = new HashMap<>();
		IntroduceForm load = new IntroduceForm(this);
		currentMenu = addForm(load, "IntroduceForm");
		load.show();
//		System.out.println("setup laikas: " + (millis()-laikas));
//		Gdx.app.log("Engine", "Setup took: " + (millis() - laikas) + "ms");
	}

	@Override
	public void draw() {
//		laikas = millis();
		if (currentMenu == -3){
			changer.action();
		}else if (currentMenu < 0 || currentMenu > menu.length){
			err.handle();
		}else{
			menu[currentMenu].handle();
		}
		if (currentMenu != -3)
			topper.handle();
		Resources.disposeDisposables(); // texture dispose.
	}

	@Override
	public void fixedDraw() {
		if (currentMenu == -3){
			changer.fixAction();
			if (!Gdx.graphics.isContinuousRendering()){
				Gdx.graphics.requestRendering();
			}
		}else if (currentMenu < 0 || currentMenu > menu.length){
			err.fixHandle();
		}else{
			menu[currentMenu].fixHandle();
		}
		if (currentMenu != -3)
			topper.fixHandle();

//		if (debug){
//			if (getFont() == null){
//				return;
//			}
//			float textSize = 42;
//			float startY = getScreenHeight();
//			fill(255,0,0);
//			textSize(textSize);
//			textAlign(Align.left);
//			float x = 50;
////			text("Input cords: " + InputHandler.mouseX + "-" + InputHandler.mouseY, c, getHeight()-70*SIZE);
////			if (game != null)
////				text("Isviso kamuoliu: " + String.valueOf(game.getBallsCount()), c, getHeight()-80*SIZE);
//			text("paliesta vieta: " + Gdx.input.getX() + "-" + (Gdx.input.getY()) + ":" + (getScreenHeight()-Gdx.input.getY()), x, startY);
//			text("fps: " + MathUtils.round(Gdx.graphics.getFramesPerSecond()), x, startY-textSize*2);
//			text("max fps: " + MathUtils.round(maxFps), x, startY-textSize*3);
////			text("min fps: " + MathUtils.round(minFps), x, startY-textSize);
////			if (minFps > Gdx.graphics.getFramesPerSecond()) {
////				minFps = Gdx.graphics.getFramesPerSecond();
////			}
//			if (maxFps < Gdx.graphics.getFramesPerSecond()) {
//				maxFps = Gdx.graphics.getFramesPerSecond();
//			}
//			int time = millis() - laikas;
//			text("Laikas: " + time, x + getScreenOffsetX(), startY-textSize/4 + getScreenOffsetY());
//			getFont().draw(getBatch(), "Laikas: " + time, x, startY-textSize/4);
//			 glyphLayout.height = -5;

//			GlyphLayout layout = new GlyphLayout();
//			layout.width = glyphLayout.width;
//			layout.height = glyphLayout.height;
//			for (GlyphLayout.GlyphRun run : glyphLayout.runs) {
//				GlyphLayout.GlyphRun a = new GlyphLayout.GlyphRun();
//				a.glyphs.addAll(run.glyphs);
//				a.xAdvances.addAll(run.xAdvances);
//				a.x = run.x;
//				a.color.set(run.color);
//				a.y = run.y;
//				a.width = run.width;
//				layout.runs.add(a);
//			}
//
//
////			text(layout, x, startY-textSize);
//			getFont().draw(getBatch(), layout, x, startY-textSize);
//			if (maxlaikas < time){
//			    maxlaikas = time;
//			    System.out.println("Laikas padidejo: " + maxlaikas);
//            }
//		}
	}

	/* Menu procedūros. Menu ar formu??? Kodel menu?? */

	/** Adds form to list. Sets form id as provided key.
     * Window and key cannot be null.
     * If key already exists - throws error.
	 * @return index of newly added form. */
	public int addForm(Window screen, String key){
		if (screen == null || key == null){
//			setError("Screen or key cannot be null", ErrorType.WrongPara);
            throw new IllegalArgumentException("Screen or key cannot be null!");
//			return -1;
		}
		if (key.isEmpty()){
//			setError("Key length is too short", ErrorType.WrongPara);
//			return -1;
            throw new IllegalArgumentException("Key cannot be empty!");
		}
		if (menuMaps.containsKey(key)){
//			setError("Key already exists: " + key, ErrorType.WrongPara);
//			return -1;
            throw new IllegalArgumentException("Key already exists: " + key);
		}
		int id = menu.length;
		menu = expandMenus(menu, menu.length+1);
		menu[menu.length-1] = screen;
		menuMaps.put(key, id);
		screen.setFormId(key); // suteikiam id.
		return id;
	}

	/**
	 * padarys esama meniu null ir vistiek laikys jo raktą kaip užimtą. Jeigu esamas langas implementinęs
	 * Disposable, tai dispose bus iškviesta.
	 * @param key - esamo meniu raktas
     */
	public void releaseForm(String key){
		if (!existFormKey(key)){
			return;
		}
		Window e = menu[menuMaps.get(key)];
		menu[menuMaps.get(key)] = null;
		if (e instanceof Disposable){
			Resources.addDisposable((Disposable) e);
		}
	}

	public boolean existFormKey(String key){
		return menuMaps.containsKey(key);
	}

	/** pakeis esama meniu, jei meniu su nurodytu raktu neegzistuoja, tai bus sukurtas naujas raktas su šiuo meniu
	 * @param screen new screen
	 * @param key window key
     * @return old Window
     */
	public Window changeForm(Window screen, String key){
		if (screen == null || key == null){
//			Window old = menu[menuMaps.get(key)];
			setError("Screen or key cannot be null", ErrorType.WrongPara);
			return null;
		}
		if (!existFormKey(key)){
			addForm(screen, key);
			return null;
		}
		Window old = menu[menuMaps.get(key)];
		if (screen == old){
			return old;
		}
		menu[menuMaps.get(key)] = screen;
		screen.setFormId(key); // nustatom nauja id.
		return old;
	}

	/** Removes form from list. Form will no longer be handled or accessed via forms change.
	 * Form's id will be set to null.*/
	public synchronized boolean removeForm(String key){
		if (!existFormKey(key)){
			return false;
		}
		int id = menuMaps.get(key);
		menuMaps.remove(key);
		Window[] a = new Window[menu.length - 1];
		int add = 0;
		int breakPoint = menu.length;
		for (int k = 0; k < menu.length; k++) {
			if (menu[k] == menu[id]) {
				if (menu[k] instanceof Disposable){
					Resources.addDisposable((Disposable) menu[k]);
				}
//				a[k-1] = menu[k];
				menu[k].setFormId(null); // salinam id.
				menu[k] = null;
				add = 1; // tiesiog prasoks per trinama forma.
				breakPoint = k;
			} else {
				a[k-add] = menu[k];
			}
		}

		for (String raktas : menuMaps.keySet()){ // id setu surealinimas
			int cid = menuMaps.get(raktas);
			if (cid >= breakPoint){
				menuMaps.put(raktas, cid-1); // turetu sumazint id ir nebepjaut grybo.
			}
		}
		menu = a;
		if (currentMenu == breakPoint){
			currentMenu = 0; // tegul soka i intrduce.
		}else if (currentMenu >= breakPoint){
			currentMenu--;
		}
		return true;
	}

	private Window[] expandMenus(Window[] kala, int ind) {
		Window[] a = new Window[ind];
		for (int k = 0; k < kala.length && k < a.length; k++) {
			a[k] = kala[k];
		}
		return a;
	}

	/**
	 * gražins meniu id, pagal duotą raktą
	 * @param key meniu raktas
	 * @return meniu id.
     */
	public int getFormKeyIndex(String key){
		if (!existFormKey(key)){
			return -1;
		}
		return menuMaps.get(key);
	}

	/** @return given form key. if form is not in list then null is returned. */
	public String getFormKey(Window e){
		int a = getFormKeyIndex(e);
		if (a >= 0){
//			int count = 0;
			for (String nm : menuMaps.keySet()){
				int num = menuMaps.get(nm);
				if (a == num){
					return nm;
				}
//				if (a == count){
//					return nm;
//				}
//				count++;
			}
		}
		return null;
	}

	/** @return given form id. if form not found -1 is returned. */
	public int getFormKeyIndex(Window e){
		for (int a = 0; a < menu.length; a++){
			if (menu[a] == e){
				return a;
			}
		}
		return -1;
	}

	public Window getFormById(int index){
		if (index < 0 || index > menu.length)
			return null;
		return menu[index];
	}

	public Window getForm(String key){
		if (!existFormKey(key)){
			return null;
		}
		return getFormById(menuMaps.get(key));
	}

	/** Form which is displayed to user.
	 * @return current active form. */
	public Window getActiveForm() {
		if (currentMenu < 0 || currentMenu >= menu.length){
			return null; // ner tokios formos
		}
		return menu[currentMenu];
	}

	/** @return size of available forms. */
	public int getFormsCount(){
		return menu.length;
	}

	/* Inputs */

	@Override
	public void pinchStop() {
		topper.pinchStop();
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				err.pinchStop();
		} else {
			menu[currentMenu].pinchStop();
		}
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		if (topper.pinch(initialPointer1, initialPointer2, pointer1, pointer2))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
		} else {
			return menu[currentMenu].pinch(initialPointer1, initialPointer2, pointer1, pointer2);
		}
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		if (topper.zoom(initialDistance, distance))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.zoom(initialDistance, distance);
		} else {
			return menu[currentMenu].zoom(initialDistance, distance);
		}
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		if (topper.panStop(x, y, pointer, button))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.panStop(x, y, pointer, button);
		} else {
			return menu[currentMenu].panStop(x, y, pointer, button);
		}
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		if (topper.pan(x, y, deltaX, deltaY))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.pan(x, y, deltaX, deltaY);
		} else {
			return menu[currentMenu].pan(x, y, deltaX, deltaY);
		}
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		if (topper.fling(velocityX, velocityY, button))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.fling(velocityX, velocityY, button);
		} else {
			return menu[currentMenu].fling(velocityX, velocityY, button);
		}
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		if (topper.longPress(x, y))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.longPress(x, y);
		} else {
			return menu[currentMenu].longPress(x, y);
		}
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		if (topper.tap(x, y, count, button))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.tap(x, y, count, button);
		} else {
			return menu[currentMenu].tap(x, y, count, button);
		}
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		if (topper.scrolled(amountX, amountY))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.scrolled(amountX, amountY);
		} else {
			return menu[currentMenu].scrolled(amountX, amountY);
		}
		return false;
	}

	@Override
	public boolean keyTyped(char e) {
		if (topper.keyTyped(e))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.keyTyped(e);
		} else {
			return menu[currentMenu].keyTyped(e);
		}
		return false;
	}

	@Override
	public boolean mouseMoved(float x, float y) {
		if (topper.mouseMoved(x, y))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.mouseMoved(x, y);
		} else {
			return menu[currentMenu].mouseMoved(x, y);
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (topper.keyUp(keycode))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.keyUp(keycode);
		} else {
			return menu[currentMenu].keyUp(keycode);
		}
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (topper.keyDown(keycode))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.keyDown(keycode);
		} else {
			return menu[currentMenu].keyDown(keycode);
		}
		return false;
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		if (topper.touchDown(x, y, pointer, button))
			return true;
		if (currentMenu < 0 || currentMenu > menu.length) {
			if (currentMenu != -3)
				return err.touchDown(x, y, pointer, button);
		} else {
			return menu[currentMenu].touchDown(x, y, pointer, button);
		}
		return false;
	}

	/* Ekranų perjungimas */

	/** sets animation changing animation.
     * Default animation is FadeAway.
	 * @param e changing animation. if e is null then animation will not be changed.
	 *          */
	public void setSwitchAnimation(SwitchAnimation e){
//		SwitchAnimation old = changer;
		if (e != null){
			changer = e;
		}
	}

	/** @return current forms changing animation instance. */
	public SwitchAnimation getActiveChangingAnimation() {
		return changer;
	}

	public void changeState(int state){
		changeState(state, true); // default informuos.
	}

	/**
	 * keičia meniu vaizdą į kitą nurodytą meniu.
	 * @param state meniu id
	 * @param inform false atveju tylus perjungimas nekviečiant show ir hide metodų.
     */
	public void changeState(int state, boolean inform){
		if (currentMenu == -1 || currentMenu == state)
			return;
		int old = currentMenu;
		currentMenu = state;
		if (inform && currentMenu >= 0){
			Engine p = Engine.getInstance();
			if (old >= 0){
				p.menu[old].release(); // kad atleistu kontroles. tai yra jei jos uzspaustos.
				p.menu[old].hide(); // pranes, kad paslepia.
			}
			Engine.getInstance().topper.formChange(); // taip todel, kad pranestu pries show.
			p.menu[currentMenu].show(); // naujam meniu prane�, kad jis bus rodomas
		}else {
			Engine.getInstance().topper.formChange(); // jei jungia ne i kita forma, o pvz error forma tai nereik tu show.
		}
//		Engine p = GdxPongy.getInstance(); // integruota i forma. pati atsimenu kameros buvimo vieta.
//		OrthographicCamera camera = p.getAbsoluteCamera();
//		camera.position.set(p.getScreenWidth()/2, p.getScreenHeight()/2, 0);// pakeitus forma perstatom kamera vel i viduri.
//		camera.update();
	}

	public void achangeState(int state){
		changer.changeState(state);
	}

	public void achangeState(String key){
		if (!existFormKey(key)){
//			setError("Menu key doesn't exist: " + key, ErrorType.ControlsError);
//			return;
            throw new RuntimeException("Menu key doesn't exist: " + key);
		}
		achangeState(menuMaps.get(key));
	}

	public void changeState(String key){
		if (!menuMaps.containsKey(key)){
//			GdxPongy.getInstance().setError("Menu key doesn't exist: " + key, ErrorType.ControlsError);
//			return;
            throw new RuntimeException("Menu key doesn't exist: " + key);
		}
		changeState(menuMaps.get(key));
	}

	public void changeState(String key, boolean inform){
		if (!menuMaps.containsKey(key)){
//			GdxPongy.getInstance().setError("Menu key doesn't exist: " + key, ErrorType.ControlsError);
//			return;
            throw new RuntimeException("Menu key doesn't exist: " + key);
		}
		changeState(menuMaps.get(key), inform);
	}

	/* rezoliucija, properties, loader, errors */

	public int getActiveFormId(){
		return currentMenu;
	}

	/** @return Active form's key. */
	public String getActiveFormKey(){
		Set<String> e = menuMaps.keySet();
//		int ind = 0; // bug. Sitaip negalima tikrint, reik is hashmapo imt.
		for (String a : e){
			int index = menuMaps.get(a);
			if (index == currentMenu){
				return a;
			}
//			ind++;
		}
		return ""; // neradus, gražins tuščia eilute.
	}

//	@Deprecated
//	public synchronized static float getSize(){
//		return SIZE;
//	}

//	public static float getWithRez(float a){
//		return SIZE * a;
//	}

//	public IntroduceForm getLoader(){
//		return (IntroduceForm) getForm("IntroduceForm");
//	}
//
//	public void startLoad(String key){
//		startLoad(key, true);
//	}
//
//	public void startLoad(String key, boolean useAnimation){
//		((IntroduceForm) getForm("IntroduceForm")).startLoad(key, useAnimation);
//	}

    @Deprecated
	/** Set error to display this error on error screen. */
	public void setError(String msg, ErrorType gameError){ // turetu viska stabdyt. visus krovimus.
		if (msg == null){
			msg = "null";
		}
		if (topper == null){
			throw new RuntimeException("Failed initializing game. Error: " + msg);
		}
		if (err == null){
			err = new ErrorMenu();
		}
		if (getFont() == null){
			throw new RuntimeException(msg); // nera fonto.
		}
		if (gameError == null){
			err.setErrorText(msg);
		}else
			err.setErrorText(msg, gameError);
		changeState(-1);
		try {
			throw new RuntimeException(msg);
		}catch (RuntimeException ex){
			ex.printStackTrace();
		}
	}

    @Deprecated
	/** Set error to display this error on error screen. */
	public void setError(String title, String msg, ErrorType gameError){
		if (title == null){
			title = "null";
		}
		if (msg == null){
			msg = "null";
		}
		String text = title + ": " + msg;
		setError(text, gameError);
	}

    @Deprecated
	/** Set error to display this error on error screen. */
	public void setError(String title, String msg){
		if (title == null){
			title = "null";
		}
		if (msg == null){
			msg = "null";
		}
		String text = title + ": " + msg;
		setError(text, (ErrorType) null);
	}
}
