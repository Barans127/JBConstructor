package com.engine.interfaces.controls;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.engine.animations.Counter;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.interfaces.controls.Window.Position;
import com.engine.interfaces.listeners.FocusListener;
import com.engine.interfaces.listeners.inputListeners.FlingListener;
import com.engine.interfaces.listeners.inputListeners.KeyDownListener;
import com.engine.interfaces.listeners.inputListeners.KeyTypedListener;
import com.engine.interfaces.listeners.inputListeners.KeyUpListener;
import com.engine.interfaces.listeners.inputListeners.LongPressListener;
import com.engine.interfaces.listeners.inputListeners.MouseMovedListener;
import com.engine.interfaces.listeners.inputListeners.PanListener;
import com.engine.interfaces.listeners.inputListeners.PanStopListener;
import com.engine.interfaces.listeners.inputListeners.PinchListener;
import com.engine.interfaces.listeners.inputListeners.PinchStopListener;
import com.engine.interfaces.listeners.inputListeners.ScrolledListener;
import com.engine.interfaces.listeners.inputListeners.TapListener;
import com.engine.interfaces.listeners.inputListeners.TouchDownListener;
import com.engine.interfaces.listeners.inputListeners.ZoomListener;
import com.engine.root.GdxPongy;

public abstract class Interface implements Inputs{
	// main kintamieji
//	protected final float r = Engine.getSize();
	protected final Engine p = GdxPongy.getInstance();
	protected InterfacesController v;
	protected boolean update;
	private String idName;

	// būsena
	private boolean enabled;
	private boolean visible;
//	private boolean focused;
    private float radius;
    // frustum
    boolean frustumVisible; // naudoja interface controller. Pasizymi ar si klase buvo handled before cameros pasikeitimus.
	int frustumUpdateId = -1; // kad updatintu jei to reikia.

	// kontrolės pozicija, dydis, spalva ir t.t.
	final Vector2 position = new Vector2();
	private int backgroundColor, status;
	/** Const status of control. Use {@link #getStatus()} to check at which status control currently is. */
	public static final int NORMAL = 0, PRESSED = 2, OVER = 1;
	/** Control tint color. Changes with status. Use {@link #setColors(int, int, int)} to set different colors for different states */
	protected final Color statusColor;
	// statuso spalvos.
	private int pressedColor, normalColor, onColor;
	// ar galima sia kontrole focusuot.
	private boolean focusable; // true by default.
	private int disabledColor; // default papilkejes
	private int positionStatus = Window.relativeView; // default relative koordinatės.
	private boolean isPositionSet;

	// animaciniai
	private Counter anime; // dingimo ir atsiradimo .
	private boolean isAppearing;
//	private float oldAlpha;
//	private Counter.CounterListener counterListener;

	// listeners
	private FocusListener lost;

	// inputlisteners
	private FlingListener flingListener;
	private KeyDownListener keyDownListener;
	private KeyTypedListener keyTypedListener;
	private KeyUpListener keyUpListener;
	private LongPressListener longPressListener;
	private MouseMovedListener mouseMovedListener;
	private PanListener panListener;
	private PanStopListener panStopListener;
	private PinchListener pinchListener;
	private PinchStopListener pinchStopListener;
	private ScrolledListener scrolledListener;
	private TapListener tapListener;
	private TouchDownListener touchDownListener;
	private ZoomListener zoomListener;

	/* Veiksmingumas */

	public Interface(InterfaceStyle style){
		if (style != null) {
			radius = style.angle;
			visible = style.visible;
			enabled = style.enabled;
			focusable = style.focusable;
			disabledColor = style.disabledColor;
			normalColor = style.normalColor;
			pressedColor = style.pressedColor;
			onColor = style.onColor;
			position.set(style.x, style.y);
			if (style.positioning != null)
				positionStatus = style.positioning.getPosition();
		}
		statusColor = new Color();
		normalStatus();
	}

	/* appear disappear animation. */

	/**
	 * @param speed in seconds.
     */
	public void appear(float speed){
		if (!visible){
			visible = true;
		}
		setAppearCounter();
		isAppearing = true;
		anime.startCount(0, 1, speed);
	}

	/**
	 * @param speed in seconds
     */
	public void disappear(float speed){
		if (!visible) // jau nematomas.
			return;
		setAppearCounter();
		isAppearing = false;
		anime.startCount(1, 0, speed);
	}

	/** Called before appearing or disappearing animation. */
	protected void setAppearCounter(){
		if (anime == null) {
			anime = MoreUtils.getCounter();

		}
		anime.setUninterruptible(true);
//		if (counterListener == null){
		anime.setCounterListiner(new Counter.CounterListener() {
				@Override
				public void finished(float currentValue) {
					setVisible(isAppearing);
				}

				@Override
				public boolean cancel(int reason) {
					return true;
				}

		});
//		anime.setCounterListiner(counterListener);
		normalStatus(); // kad nemaišytų kitos spalvos. Ims tik iš paprastos būsenos.
//		oldAlpha = statusColor.a;
	}

//	protected void tint(float value){}

	/* controller, positioning. */

	protected final void checkPositioning(){
		if (v != null && positionStatus == Window.relativeView) {
			v.onAdd(this);
			isPositionSet = true;
		}
	}

	/** Usually is set when added to form. */
	public void setController(InterfacesController v){
		if (this.v != null && positionStatus == Window.relativeView){
			this.v.removeControl(this); // pati pasisalins is seno kontrolerio, taip nebus duplikatu.
			if (this.v instanceof PopUp){
				Vector2 e = ((PopUp) this.v).getPosition();
				position.set(position.x - e.x, position.y - e.y);
				isPositionSet = false;

				frustumUpdateId = -1;
			}
		}
		this.v = v;
		checkPositioning();
	}

	/** interfaceController use this when control is removed from it. */
	void removeController(){
		v = null;
	}

	/* focus methods. */

	/** @return true if control is focused and enter is pressed. */
	protected boolean onEnterPress(int e){
		if (e == Input.Keys.ENTER && enabled){
			if (isFocused()){
//				focused = false; // o gal palikt true?
				if (v != null){
					v.removeFocus();
				}
				normalStatus();
				return true;
			}
		}
		return false;
	}

	/** Asks for focus. Control must be focusable, enabled and visible. Also control must be owned by interface controller. */
	public void getFocus(){
//		if (v != null && focusable && enabled && visible){
		if (v != null)
			v.focusMe(this); // ten viduj viska padarys.
//		}else {
//			if (v != null){
//				v.removeFocus(); // jeigu kazkas ne taip, vistiek nuimam.
//			}
//		}
	}

	/** Called when focus needs to be moved (tab press etc).
	 * Override this if you manage more controls inside and want to switch focus between them.
	 * @param forward do focus move forward or backward.
	 * @return true - this control lose focus, false - control will not lose focus */
	protected boolean moveFocus(boolean forward){
		return true;
	}

//	protected void loseFocus(boolean callList){ // nekvies listener. Ar bus gerai taip?
//		if (focused){
//			focused = false;
//			if (visible && enabled) { // kad nekeistu spalvos uzdisablintom controlem.
//				normalStatus();
//			}
//			onLostFocus();
//			if (callList){
//				if (lost != null){
//					lost.onLostFocus(this);
//				}
//			}
//		}
//	}

//	/** Control will lose focus. */
//	public void loseFocus(){
//		loseFocus(false);
//	}

//	/** sufokusuos, jei ši kontrolė yra focusable, enabled ir matoma. */
//	public void getFocus(){
//		if (focusable && enabled && visible){
//			v.focusMe(this);
//		}
//	}

	/* handlinimas, piesimas. */

	public final void handle(){
		if (update) {
			auto();
			if (positionStatus == Window.relativeView)
				v.onAdd(this); // pasikeitus dydziui, pranes pop up..

			frustumUpdateId = -1;
			update = false;
		}
		boolean changedColor = false;
		float oldAlpha = 0;
		Color color = null;
		if (anime != null) {
			if (anime.isCounting()) {
				float op = anime.getCurrentValue(); // dabartine counter value.
				color = p.getForceColor(); // paimam esama force tint.
				oldAlpha = color.a; // issisaugom sena alpha
				color.a = oldAlpha*op; // idedam nauja alpha
				p.forceTint(color); // imetam nauja spalva
				changedColor = true; // pranesam, kad atkeistu kai baigs.

//				float op = anime.getCurrentValue() / 100f;
//				statusColor.a = op*oldAlpha;
//				changedColor = true;
//				tint(op);
			}else {
				MoreUtils.freeCounter(anime);
				anime = null;
			}
		}
		isvaizda(position.x + v.getOffsetX(), position.y + v.getOffsetY());
		if (changedColor){ // keite spalva.
//			statusColor.a = oldAlpha;
//			tint(1);
//			Color color = p.getForceColor();
			color.a = oldAlpha; // atkeiciam atgal
			p.forceTint(color); // nustatom senaja spalva.
		}
	}

	protected abstract void isvaizda(float x, float y);

//	protected boolean setFocused(){
//		if (focusable && !focused){
//			if (enabled && visible){ // nefokusuos uzdisablintu ar nematomu.
//				focused = true;
//				onStatus();
//				onFocus();
//				if (lost != null){
//					lost.onFocus(this);
//				}
//			}
//		}
//		return focused;
//	}

	/* status changing. */

	/** Changes status to normal. */
	protected void normalStatus(){
		int old = backgroundColor;
		backgroundColor = enabled ? normalColor : disabledColor;
//		statusColor.set(backgroundColor);
		Color.argb8888ToColor(statusColor, backgroundColor);
		if (enabled && old != backgroundColor){
			status = 0;
			statusNormal();
		}
	}

	/** Changes status to pressed. */
	protected void pressed(){
		int old = backgroundColor;
		backgroundColor = enabled ? pressedColor : disabledColor;
		Color.argb8888ToColor(statusColor, backgroundColor);
		if (enabled && old != backgroundColor){
			status = 2;
			statusPressed();
		}
	}

	/** Changes status to mouse over control. */
	protected void onStatus(){
		int old = backgroundColor;
		backgroundColor = enabled ? onColor : disabledColor;
		Color.argb8888ToColor(statusColor, backgroundColor);
		if (enabled && old != backgroundColor){
			status = 1;
			statusOver();
		}
	}

	/* listeners */

	/** called when status changes to normal */
	protected void statusNormal(){}

	/** called  when status changes to mouse over. */
	protected void statusOver(){}

	/** called  when status changes to pressed. */
	protected void statusPressed(){}

	/**Called when control gains focus */
	protected void onFocus(){}

	/** Called when control loses focus. */
	protected void onLostFocus(){}

	/** Kai kontrolė iš enabled pereina į disabled */
	protected void onDisable(){}

	/** Kai kontrolė iš disabled pereina į enabled */
	protected void onEnable(){}

	/** Called when control is being removed from form. */
	protected void onRemove(){}

	/** Sets focus listener for this control. Listener can be only one. */
	public void setFocusListener(FocusListener e){
		lost = e;
	}

	/** Focus listener of this control. Can be null if not set. */
	public FocusListener getFocusListener(){
		return lost;
	}

	public void setTouchDownListener(TouchDownListener e){
		touchDownListener = e;
	}

	public TouchDownListener getTouchDownListener(){
		return touchDownListener;
	}

	public void setKeyDownListener(KeyDownListener e){
		keyDownListener = e;
	}

	public KeyDownListener getKeyDownListener(){
		return keyDownListener;
	}

	public void setKeyUpListener(KeyUpListener e){
		keyUpListener = e;
	}

	public KeyUpListener getKeyUpListener(){
		return keyUpListener;
	}

	public void setMouseMovedListener(MouseMovedListener e){
		mouseMovedListener = e;
	}

	public MouseMovedListener getMouseMovedListener(){
		return mouseMovedListener;
	}

	public void setKeyTypedListener(KeyTypedListener e){
		keyTypedListener = e;
	}

	public KeyTypedListener getKeyTypedListener(){
		return keyTypedListener;
	}

	public void setScrolledListener(ScrolledListener e){
		scrolledListener = e;
	}

	public ScrolledListener getScrolledListener(){
		return scrolledListener;
	}

	public void setTapListener(TapListener e){
		tapListener = e;
	}

	public TapListener getTapListener(){
		return tapListener;
	}

	public void setLongPressListener(LongPressListener longPressListener) {
		this.longPressListener = longPressListener;
	}

	public LongPressListener getLongPressListener(){
		return longPressListener;
	}

	public void setFlingListener(FlingListener e){
		flingListener = e;
	}

	public FlingListener getFlingListener(){
		return flingListener;
	}

	public void setPanListener(PanListener e){
		panListener = e;
	}

	public PanListener getPanListener(){
		return panListener;
	}

	public void setPanStopListener(PanStopListener e){
		panStopListener = e;
	}

	public PanStopListener getPanStopListener(){
		return panStopListener;
	}

	public void setZoomListener(ZoomListener e){
		zoomListener = e;
	}

	public ZoomListener getZoomListener(){
		return zoomListener;
	}

	public void setPinchListener(PinchListener e){
		pinchListener = e;
	}

	public PinchListener getPinchListener(){
		return pinchListener;
	}

	public void setPinchStopListener(PinchStopListener e){
		pinchStopListener = e;
	}

	public PinchStopListener getPinchStopListener(){
		return pinchStopListener;
	}

	public boolean touchDown(float x, float y, int pointer, int button) {
		return touchDownListener != null && touchDownListener.touchDown(x, y, pointer, button);
	}

	public boolean keyDown(int keycode) {
		return keyDownListener != null && keyDownListener.keyDown(keycode);
	}

	public boolean keyUp(int keycode) {
		return keyUpListener != null && keyUpListener.keyUp(keycode);
	}

	public boolean mouseMoved(float x, float y) {
		return mouseMovedListener != null && mouseMovedListener.mouseMoved(x, y);
	}

	public boolean keyTyped(char e) {
		return keyTypedListener != null && keyTypedListener.keyTyped(e);
	}

	public boolean scrolled(float amountX, float amountY){
		return scrolledListener != null && scrolledListener.scrolled(amountX, amountY);
	}

	public boolean tap(float x, float y, int count, int button) {
		return tapListener != null && tapListener.tap(x, y, count, button);
	}

	public boolean longPress(float x, float y) {
		return longPressListener != null && longPressListener.onLongPress(x, y);
	}

	public boolean fling(float velocityX, float velocityY, int button) {
		return flingListener != null && flingListener.fling(velocityX, velocityY, button);
	}

	public boolean pan(float x, float y, float deltaX, float deltaY) {
		return panListener != null && panListener.pan(x, y, deltaX, deltaY);
	}

	public boolean panStop(float x, float y, int pointer, int button) {
		return panStopListener != null && panStopListener.panStop(x, y, pointer, button);
	}

	public boolean zoom(float initialDistance, float distance) {
		return zoomListener != null && zoomListener.zoom(initialDistance, distance);
	}

	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		return pinchListener != null && pinchListener.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
	}

	public void pinchStop() {
		if (pinchStopListener != null)
			pinchStopListener.pinchStop();
	}

	public abstract void release();
//		if (isFocused()){
//			v.removeFocus();
//		}
//	}

	protected abstract void auto();

	/* būsenos keitimas. */

//	protected boolean allowChangeFocus(int way){ // sita funkcija bus skirta perrasymui. pvz listview leis, per jo item nar�yt
//		return true; // leis nustatyt controlem su vidinem kontrolem focusa, netrugdant isorinems kontrolems.
//	}

	/** Ar kontrolė gali būt fokusuojama */
	public void setFocusable(boolean focusable){
		this.focusable = focusable;
	}

	/* spalvu keitimas */

	/** ARGB format */
	public void setColors(int pressed, int normal, int over){
		pressedColor = pressed;
		normalColor = normal;
		onColor = over;
		normalStatus();
	}

	/** ARGB format. Set normal color for this control. */
	public void setNormalColor(int normal){
		normalColor = normal;
		normalStatus();
	}

	/** ARGB format. Set color when mouse is over control or finger over it. */
	public void setOverColor(int over){
		onColor = over;
		normalStatus();
	}

	/** ARGB format. Set color when control is pressed. */
	public void setPressedColor(int pressedColor){
		this.pressedColor = pressedColor;
		normalStatus();
	}

	/* pozicija dydis. */

	/** can be used to determine any change of size or any other attribute of interface. */
	protected void sizeUpdated(){
//		nx = position.x * r;
//		ny = position.y * r;
	}

	public void setPosition(Vector2 e){
		setPosition(e.x, e.y);
	}

	public void setPosition(float x, float y){
		position.set(x, y);
		isPositionSet = false;
		checkPositioning();

		frustumUpdateId = -1;
//		nx = position.x * r;
//		ny = position.y * r;
	}

	protected void giveCords(float x, float y){
		position.set(x, y);

		frustumUpdateId = -1;
//		nx = position.x * r;
//		ny = position.y * r;
	}

	/* kitos busenos. */

	public void setEnabled(boolean enable){
		if (this.enabled == enable){
			return;
		}
		this.enabled = enable;
		backgroundColor = enable ? normalColor : disabledColor; // papilkejes kai disabled.
		Color.argb8888ToColor(statusColor, backgroundColor);
		release();
		if (!enable){
			onDisable();
		}else {
			onEnable();
		}
	}

	public void setDisabledColor(Color c){
		setDisabledColor(Color.argb8888(c));
	}

	/** @return true if name was changed. If control doesn't have controller then name will be given but will be changed when added
	 * to controller if control with this name already exists. */
	public boolean setIdName(String name){
		if (name == null)
			return false;
//			name = "null";
		if (v == null){ // jei ner kontrolerio tai duosim iskart varda. nesilauzom.
			idName = name;
			return true;
		}
		if (v.checkControlIdName(this, name)){
			idName = name;
			return true;
		}
		return false;
	}

	public void setDisabledColor(int color){
		disabledColor = color;
		if (!enabled){
			backgroundColor = disabledColor;
//			statusColor.set(disabledColor);
			Color.argb8888ToColor(statusColor, disabledColor);
		}
	}

	public void setVisible(boolean visible){
		this.visible = visible;
		release();

		frustumUpdateId = -1;// tegul naujinasi.
	}

	public void setPositioning(Position e){
		positionStatus = e.getPosition();
		if (v != null) {
			v.interfaceViewHasChanged(this);
			checkPositioning();
		}
	}

	/** Rotation in degrees. */
	public void setAngle(float radiusDegrees) {
		this.radius = radiusDegrees;
	}

	/* kontrolės apibūdinimui */

	/** Appearing, disappearing, transition etc. */
	public boolean isAnimating(){
		return anime != null && anime.isCounting();
	}

	/** returns angle in degrees */
	public float getAngle(){
		return radius;
	}

	/** pressed, normal or mouse over control. */
	public int getStatus(){
		return status;
	}

	public int getNormalColor(){
		return normalColor;
	}

	public int getOverColor(){
		return onColor;
	}

	public int getPressedColor(){
		return pressedColor;
	}

//	protected int getStatusColor(){
//		return backgroundColor;
//	}

	/** Can this control be focused. */
	public boolean isFocusable(){
		return focusable;
	}

	/** Is this control focused. */
	public boolean isFocused(){
		return v != null && v.getFocusedItem() == this;
	}

	@Override
	public int getPositioning(){
		return positionStatus;
	}

	/** @return only absolute or fixed positioning. if interface positioning is relative then it's controller positioning will be used. */
	public int getAbstractPositioning(){
		return getPositioning() == Window.relativeView ? v instanceof PanelHost ? ((PanelHost) v).getRealPositioning() :
				v != null ? v.getPositioning() : Window.absoluteView : getPositioning();
	}

	public Vector2 getPosition(){
		return position;
	}

	public InterfacesController getController(){
		return v;
	}

	public String getIdName(){
		return idName;
	}

	public boolean isEnabled(){
		return enabled;
	}
	/** Is this control visible.
	 * To check if control is visible and not out of screen bounds use method: {@link #isVisibleOnScreen()}. */
	public boolean isVisible(){
//		return visible && (v == null || v.amIVisible(this));
		return visible;
	}

	/** Is this control visible and is not out of visible screen bounds (If frustum is enabled or else this method works same as {@link #isVisible()}). */
	public boolean isVisibleOnScreen(){
		return visible && (v == null || v.amIVisible(this));
	}

	public int getDisabledColor(){
		return disabledColor;
	}

	boolean isPositionSet(){
		return isPositionSet;
	}

	/* style */

	public abstract InterfaceStyle getStyle();
//		InterfaceStyle st = new InterfaceStyle();
//		copyStyle(st);
//		return st;
//	}

	/** puts all interface parameters to given style */
	public void copyStyle(InterfaceStyle st){
		st.angle = getAngle();
		st.visible = isVisible();
		st.enabled = isEnabled();
		st.focusable = isFocusable();
		st.x = position.x;
		st.y = position.y;
		Position e;
		switch (getPositioning()){
			case Window.absoluteView:
				e = Position.absolute;
				break;
			case Window.fixedView:
				e = Position.fixed;
				break;
			default:
				e = Position.relative;
				break;
		}
		st.positioning = e;
		st.normalColor = normalColor;
		st.onColor = onColor;
		st.pressedColor = pressedColor;
		st.disabledColor = disabledColor;
	}

	/** Sets all parameters from given style to this interface */
	public void readStyle(InterfaceStyle st){
		setAngle(st.angle);
		setVisible(st.visible);
		setEnabled(st.enabled);
		setFocusable(st.focusable);
		setPosition(st.x, st.y);
		setPositioning(st.positioning);
		setColors(st.pressedColor, st.normalColor, st.onColor);
		setDisabledColor(st.disabledColor);
	}

	public static abstract class InterfaceStyle{
		/** Interface rotation in degrees. */
		public float angle = 0;
		public boolean visible = true;
		public boolean enabled = true;
		public boolean focusable = true;
		public float x;
		public float y;
//		public int positioning = Window.relativeView;
		public Position positioning = Position.relative;
		public int normalColor = 0xFFFFFFFF, pressedColor = 0xFFFFFFFF, // netintins.
				onColor = 0xFFFFFFFF, disabledColor = GdxPongy.color(80,80,80, 255);

		/** creats new interface with this style */
		public abstract Interface createInterface();
	}
}
