package com.engine.ui.controls.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.ControlHost;
import com.engine.ui.controls.Window;
import com.engine.ui.controls.views.Panel;
import com.engine.ui.controls.widgets.Button.ButtonStyle;
import com.engine.ui.listeners.ClickListener;

import java.util.ArrayList;
import java.util.List;

/** Laiko tabus, juos perjunginėja. Neturi dydžio nustatymų, visi tabai gali skirtis dydžiu. */
public class TabControl extends Control {
	private ArrayList<Panel> body;
	private ArrayList<Button> controls;
	private int index;
	private float maxButtonWidth, buttonsOffsetX; // mygtukų talpinimas, offset mygtuku.
	private ButtonStyle buttonStyle;
	private float maxWidth, maxHeight;
	private float currentWidth = 0, currentHeight = 0; // nustatyt koks dabar yra dydziausias dydis. Bug fix kai ten max dydi iskart imdavo ir nesvarbo koks tab dydis.
	private ButtonsPosition buttonsPosition;

	private TabIndexChangedListener listener;

//	private FocusListener focusListener;

	public TabControl(TabControlStyle style){
		super(style);
		body = new ArrayList<>(5);
		controls = new ArrayList<>(5);
//		maxButtonWidth = style.maxButtonWidth;
		setMaxButtonWidth(style.maxButtonWidth);
		buttonStyle = style.buttonsStyle;
		buttonsOffsetX = style.buttonsOffsetX;
		maxWidth = style.maxWidth;
		maxHeight = style.maxHeight;
		buttonsPosition = style.buttonsPosition;
//		focusListener = new FocusListener() {
//			@Override
//			public void onLostFocus(Interface e) {
//				TabControl.this.loseFocus();
//			}
//
//			@Override
//			public void onFocus(Interface e) {
//				TabControl.this.getFocus();
//			}
//		};
	}

	public List<Panel> getBodies(){
		return body;
	}

//	public TabControl(int x, int y){
//		body = new ArrayList<>();
//		controls = new ArrayList<>();
//		position.set(x, y);
//		setVisible(true);
//		setEnabled(true);
//	}

	// valdymas

	/** position where buttons should be placed on tab control. */
	public void setButtonsPosition(ButtonsPosition e){
		buttonsPosition = e;
		checkBodies();
		checkButtons();
	}

	/** position where buttons is placed on TabControl. */
	public ButtonsPosition getButtonsPosition(){
		return buttonsPosition;
	}

	public int getOpenedTabIndex(){
		return index;
	}

	public void openTabByIndex(int index){
		if (index >= 0 && index < controls.size()){
//			this.index = index;
			controls.get(index).performClick(true);
		}
	}

	/** how many tabs are in tab control. */
	public int getTabsCount(){
		return controls.size();
	}

	// pagrindiniai.

	/** sets new style for future buttons. All added buttons before will not be affected. */
	public void setbuttonStyle(ButtonStyle e){
		if (e != null)
			buttonStyle = e;
	}

	public ButtonStyle getButtonStyle() {
		return buttonStyle;
	}

	/** offset of how much tab buttons will be placed. */
	public void setButtonsOffsetX(float offsetX){
		buttonsOffsetX = offsetX;
	}

	public float getButtonsOffsetX(){
		return buttonsOffsetX;
	}

	/** max width which tab button can be. */
	public float getMaxButtonWidth(){
		return maxButtonWidth;
	}

	/** max width which tab button can be. Negative value will be treated as positive. */
	public void setMaxButtonWidth(float width){
		maxButtonWidth = MoreUtils.abs(width);
	}

	public void setMaxSize(float maxWidth, float maxHeight){
		if (maxWidth > 0){
			this.maxWidth = maxWidth;
		}
		if (maxHeight > 0){
			this.maxHeight = maxHeight;
		}
	}

	/** max width which tab can be. All <code>Panel</code> instances will be resized if it's size is higher than this value. */
	public float getMaxWidth(){
		return maxWidth;
	}

	/** max width which tab can be. All <code>Panel</code> instances will be resized if it's size is higher than this value. */
	public float getMaxHeight(){
		return maxHeight;
	}

	/** real width of this tabControl. Biggest <code>panel</code> plus button size. */
	public float getWidth(){
		switch (buttonsPosition){
			case TOP:
			case BOTTOM:
				return currentWidth;
			case LEFT:
			case RIGHT: // pridedam ilgiausio mygtuko ilgi prie ilgiausio tabo.
				float max = 0;
				for (Button e : controls){
					if (e.getWidth() > max){
						max = e.getWidth();
					}
				}
				return currentWidth + max;
		}
		return currentWidth;
	}

	/** real height of this tabControl. Biggest <code>panel</code> plus button size. */
	public float getHeight(){
		switch (buttonsPosition){
			case BOTTOM:
			case TOP: // pridedam auksti prie dydziausio tabo aukscio.
				float max = 0;
				for (Button e : controls){
					if (e.getHeight() > max){
						max = e.getHeight();
					}
				}
				return currentHeight + max;
			case RIGHT:
			case LEFT:
				return currentHeight;
		}
		return currentHeight;
	}

	/** Removes tab from this tabControl.*/
	public void removeTab(int index){
		if (index > 0 && index < body.size()) {
			if (this.index >= index){
				this.index--;
			}
			controls.remove(index);
			v.removeControl(body.get(index));
			body.remove(index);
			checkBodies();
			checkButtons();
			// perskaiciuojam indexus.
			for (int a = 0; a < controls.size(); a++){
				TabButtonAction action = (TabButtonAction) controls.get(a).getClickListener();
				action.id = a;
			}
		} // Perskaičiuot mygtukų pozicijas.
	}

	/**
	 * Tabo pridėjimas. Tabo dydis priklauso nuo panel dydžio. Skirtingi panelių dydžiai, rodys skirtingų dydžių tabus.
	 * @param tab - panel, kuriame bus kontrolės
	 * @param name Pavadinimas, kuris bus ant mygtuko.
     */
	public void addTab(Panel tab, String name){
		if (body.contains(tab)){
			System.out.println("Same panel already exist in tab");
			return;
		}
		Vector2 position = getPosition();
		final int max = body.size();
		tab.setPosition(position);
		tab.setVisible(true);

		tab.setController(v);
//		tab.setFocusListener(focusListener);
		body.add(tab);
//		float place = position.x+buttonsOffsetX;
//		for (int a = 0; a < controls.size(); a++){
//			float lx = controls.get(a).getWidth();
//			place += lx;
//		}
//		buttonStyle.x = place;
//		buttonStyle.y = position.y;
		Button con = new Button(name, buttonStyle);
		con.setController(v);
		con.setClickListener(new TabButtonAction(controls.size(), con));
		controls.add(con);
		con.auto(); // kad pasigautu dydi ir checkButtons tinkamai suveiktu.
		checkControlsPositioning(); // bug fix. sitas turi pirmas eit, kitaip nepasigauna tinkamo view ir skirtingai ismeto tabus.
		checkBodies();
		checkButtons();
		if (max == 0){ // musu pirmasis tabas, reik ji pazymet.
			con.setEnabled(false);
		}
//		if (max == index){
//			nextTab();
//		}
	}

	private void checkBodies(){ // nepilns. reik isdestyma normalos.
		checkCurrentSize(); // nustatom max size.
		for (Panel e : body){
			// dydzio nustatymas.
			float width = e.getWidth();
			float height = e.getHeight();
			boolean needChange = false;
			if (width > maxWidth){
				width = maxWidth;
				needChange = true;
			}
			if (height > maxHeight){
				height = maxHeight;
				needChange = true;
			}
			if (needChange){
				e.setSize(width, height);
			}
			// isdestymas
			e.setPosition(getPosition()); // tiesiog taip. nieko mandro.
//			Vector2 pos = getPosition();
//			switch (buttonsPosition){
//				case BOTTOM:
//				case LEFT:
//				case RIGHT:
//				case TOP:
//					e.setPosition(pos);
//					break;
////				case RIGHT:
////					float offsetX = maxWidth - e.getWidth();
////					e.setPosition(pos.x + offsetX, pos.y);
////					break;
////				case TOP:
////					float offsetY = maxHeight - e.getHeight(); // ???
////					e.setPosition(pos.x, pos.y + offsetY);
////					break;
//			}
		}
	}

	private void checkButtons(){
//		float currentWidth = 0;
//		for (int a = 0; a < controls.size(); a++){ // nustatyt bendrą dydį
//			currentWidth += controls.get(a).getWidth();
//		}
//		float x = getPosition().x + buttonsOffsetX;
//		int all = controls.size();
//		float maxButtonSize = maxButtonWidth /(float) all;
//		for (int a = 0; a < controls.size(); a++) { // sumažins jei reik ir perkels į naują poziciją.
//			Button e = controls.get(a);
//			if (currentWidth > maxButtonWidth) {
//				if (e.getWidth() > maxButtonSize) {
//					all -= e.getWidth();
//					e.setSize(maxButtonSize, e.getHeight());
//					all += e.getWidth();
//					maxButtonSize = maxButtonWidth / (float) all;
//				}
//			}
//			Vector2 pos = e.getPosition();
//			if (pos.x > x) {
//				e.setPosition(x, pos.y);
//			}
//			x += e.getWidth();
//		}
		float maxWidth = currentWidth, maxHeight = currentHeight; // turi ne max dydi gaudyt, o toki koks dabar maximalus yra.
		Vector2 pos = getPosition(); // bet gi reik sito butinai.
		float x, y;
		float maxWid;
		maxWid = (maxWidth-buttonsOffsetX) / controls.size(); // didziausias leistinas mygtuko dydis.
		if (maxWid > maxButtonWidth){
			maxWid = maxButtonWidth;
		}
		float maxHei;
		switch (buttonsPosition){
			case TOP:
//				x = pos.x + buttonsOffsetX;
//				y = pos.y + maxHeight;
				x = buttonsOffsetX;
				y = maxHeight;
				for (Button e : controls){
					if (e.getWidth() > maxWid){
						e.setSize(maxWid, e.getHeight());
					}
					e.setPosition(x +  pos.x, y + pos.y); // dadeta tabo pozicija.
					x += e.getWidth();
				}
				break;
			case LEFT:
				x = 0;
				y = 0;
				maxWid = maxButtonWidth;
				maxHei = maxHeight / controls.size();
				for (Button e : controls){
					float sX = e.getWidth(), sY = e.getHeight();
					boolean change = false;
					if (e.getWidth() > maxWid){
						sX = maxWid;
						change = true;
					}
					if (e.getHeight() > maxHei){
						sY = maxHei;
						change = true;
					}
					if (change){
						e.setSize(sX, sY); // nekeis teksto dydzio, kas gali lemt teksto isnykima.
					}
					e.setPosition(x - e.getWidth() + pos.x, y + pos.y);
					y += e.getHeight();
				}
				break;
			case RIGHT:
				x = maxWidth;
				y = 0;
				maxWid = maxButtonWidth;
				maxHei = maxHeight / controls.size();
				for (Button e : controls){
					float sX = e.getWidth(), sY = e.getHeight();
					boolean change = false;
					if (e.getWidth() > maxWid){
						sX = maxWid;
						change = true;
					}
					if (e.getHeight() > maxHei){
						sY = maxHei;
						change = true;
					}
					if (change){
						e.setSize(sX, sY); // nekeis teksto dydzio, kas gali lemt teksto isnykima.
					}
					e.setPosition(x + pos.x, y + pos.y);
					y += e.getHeight();
				}
				break;
			case BOTTOM:
				x = buttonsOffsetX;
				y = 0;
				for (Button e : controls){
					if (e.getWidth() > maxWid){
						e.setSize(maxWid, e.getHeight());
					}
					e.setPosition(x + pos.x, y - e.getHeight() + pos.y);
					x += e.getWidth();
				}
				break;
		}

	}

	private void checkCurrentSize(){
		float maxX = 0, maxY = 0;
		for (Panel e : body){
			if (e.getWidth() > maxX){
				maxX = e.getWidth();
			}
			if (e.getHeight() > maxY){
				maxY = e.getHeight();
			}
		}
		currentWidth = maxX;
		currentHeight = maxY;
	}

	private void checkControlsPositioning(){
	    boolean isFixed;
//	    if (getPositioning() == Window.relativeView) { // Bug fix taba idejus i paneli visi tabai nuskrenda kazkur ne ten.
		// visos kontroles turi buti fixed arba absolute, tik pats tabas bus keliojamas paneliu, taip tabControl pats nusitemps visas kontroles ir ju
		// panelis neper delios kur nereik.
		isFixed = getAbstractPositioning() == Window.fixedView;
//			for (Button e : controls){
//				e.setPositioning(Window.Position.relative);
//			}
//			for (Panel e : body){
//				e.setPositioning(Window.Position.relative);
//			}
//			return;
//            isFixed = v != null && v.getPositioning() != Window.absoluteView;
//        }else

//        else isFixed = getPositioning() != Window.absoluteView;
        for (Button e : controls){
	        e.setPositioning(isFixed ? Window.Position.fixed : Window.Position.absolute);
        }
        for (Panel e : body){
	        e.setPositioning(isFixed ? Window.Position.fixed : Window.Position.absolute);
        }
    }

	private void enableControls(int except){
		for (int a = 0; a < controls.size(); a++){
			if (except != a){
				controls.get(a).setEnabled(true);
			}
		}
	}

	private void nextTab(){
		int id = index + 1;
		if (id >= controls.size()){
			id = 0;
		}
//		index = id;
		controls.get(id).performClick(); // cia bus mygtuko paspaudimas, todel pakeist ten.
	}

//	private void checkStatus(int nInt){
//		if (v == null){
////			p.setError("add TabControl to form before adding tabs", ErrorType.ControlsError);
//			body.get(index).setAction(true);
//			body.get(nInt).setAction(false);
//			return;
//		}
//		boolean g = v.getReact();
//		if (!g){
//			body.get(index).setAction(true);
//			body.get(nInt).setAction(false);
//		}
//	}


	@Override
	public void setPositioning(Window.Position e) {
		super.setPositioning(e);
		checkControlsPositioning();
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		checkBodies();
		checkButtons();
//		checkControlsPositioning();
	}

	@Override
	protected void giveCords(float x, float y) {
		super.giveCords(x, y);
		checkBodies();
		checkButtons();
//		checkControlsPositioning();
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		if (super.touchDown(x, y, pointer,button))
			return true;
		if (body.get(index).touchDown(x, y, pointer, button)){
			return true;
		}
		for (int a = 0; a < controls.size(); a++){
			if (controls.get(a).touchDown(x, y, pointer, button))
				return true;
		}
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		if (super.tap(x, y, count, button))
			return true;
		if (body.get(index).tap(x, y, count, button))
			return true;
		for (int a = 0; a < controls.size(); a++){
			if (controls.get(a).tap(x, y, count, button)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (super.keyDown(keycode))
			return true;
		if (keycode == Input.Keys.TAB && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isTouched()){
			nextTab();
			return true;
		}
		if (body.get(index).keyDown(keycode)){
			return true;
		}
		for (int a = 0; a < controls.size(); a++){
			if (controls.get(a).keyDown(keycode)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (super.keyUp(keycode))
			return true;
		if (body.get(index).keyUp(keycode)){
			return true;
		}
		for (int a = 0; a < controls.size(); a++){
			if (controls.get(a).keyUp(keycode)){
				return true;
			}
		}
		return false;
	}

//	@Override
//	public void autoSize() {} // nenaudojamas.

	@Override
	public boolean mouseMoved(float x, float y) {
		if (super.mouseMoved(x, y))
			return true;
		int size = body.size();
		if (size == 0)
			return false;
		boolean isActive = false;
		for (int a = 0; a < size; a++){
			Button e = controls.get(a);
			if (isActive)
				e.release();
			else if (e.isEnabled() && e.isVisibleOnScreen()) {
				if (e.mouseMoved(x, y))
					isActive = true;
			}
		}
		if (isActive){
			body.get(index).release();
			return true;
		}
		return body.get(index).mouseMoved(x, y);
	}

	@Override
	protected void isvaizda(float x, float y) {
		int size = body.size();
		if (size == 0) {
			setVisible(false);
			return;
		}
		for (int a = 0; a < size; a++){
			if (index != a){
				Button e = controls.get(a);
				if (e.isVisibleOnScreen())
					e.handle();
			}
		}
		body.get(index).handle();
//		body.get(index).handle(); // nu ne tas. panelio reik..... ne pop up... ok.
		Button e = controls.get(index);
		if (e.isVisibleOnScreen())
			e.handle();
	}

	@Override
	public void setController(ControlHost e){
		super.setController(e);
		for (int a = 0; a < body.size(); a++){
			body.get(a).setController(e);
			controls.get(a).setController(e);
		}
		checkBodies(); // be situ mygtukai perstatomi neteisingai.
		checkButtons();
		checkControlsPositioning();
	}

	/* focus */
	/** Check if control is owned by tab control. Usually used by {@link ControlHost} */
	public boolean containsControl(Control e){
		// paziurim gal kartais mygtukas
		if (e instanceof Button){
			if (controls.contains(e)){
				return true;
			}
		}
		// gal pats panelis.
		if (e instanceof Panel){
			if (body.contains(e)){
				return true;
			}
		}
		// o, gal panelio dalis?
		for (Control in : body.get(index).getHost().getControls()){
			if (e == in){
				return true;
			}
		}
		// neup, ne is cia.
		return false;
	}

	@Override
	protected boolean moveFocus(boolean forward) {
		Panel panel = body.get(index);
		if (panel.isFocused()){
			boolean answ = panel.moveFocus(forward);
			if (answ){ // panel nebenori focuso.
				if (!forward){
					// bandom fokusuot mygtukus.
					for (int a = controls.size()-1; a >= 0; a--){
						Button button = controls.get(a);
						if (button.isEnabled()){ // tik paziurim, kad butu enablintas.
							button.getFocus();
							return false;
						}
					}
				}else { // i prieki eina. tai tiesiog atidodam fokusa.
					return true;
				}
			}else { // viskas tvarkoj.
				return false; // nieko nekeiciam.
			}
		}else {
			// jeigu ne panel, tai ziurim is mygtuku.
			for (int a = 0; a < controls.size(); a++){
				Button button = controls.get(a);
				if (button.isFocused()){
					if (forward){
						if (a + 1 >= controls.size()){
							// cia vadinas, kad paskutinis button. Perduodam focusa i paneli.
							if (panel.getHost().getControls().size() > 1) {
								panel.getFocus();
							}else {
								return true; // tuscias panel.
							}
						}else { // kitu atveju darom taip.
							int ind = a+1;
							// paziurim ar galim focusint kita button.
							for (int k = ind; k < controls.size(); k++){
								Button button1 = controls.get(k);
								if (button1.isEnabled() && button1.isVisible() && button1.isFocusable()){
									button1.getFocus();
									return false;
								}
							}
							// deja nepavyko.
							// cia vadinas, kad paskutinis button. Perduodam focusa i paneli.
							if (panel.getHost().getControls().size() > 1) {
								panel.getFocus(); // cia irgi gali nepaeit. bet tiek to.
							}else {
								return true; // tuscias panel.
							}
//							controls.get(a+1).getFocus(); // kita mygtuka fokusuojam.
						}
						return false; // pasakom, kad niekon edaryt.
					}else {
						// atgal.
						if (a - 1 < 0){ // is pirmo mygtuko atgal.
							return true; // lai pasiima.
						}else {
							// bandom zemesni.
							for (int k = a-1; k >= 0; k--){
								Control in = controls.get(k);
								if (in.isFocusable() && in.isEnabled() && in.isVisible()){
									in.getFocus();
									return false;
								}
							}
//							controls.get(a-1).getFocus(); // fokusuojam zemesni.
							// nepavyko, atiduodam fokusa.
							return true;
						}
					}
				}
			}
		}
		return true; // kazkas ne to, lai keicia.
	}

	//	@Override
//	protected boolean allowChangeFocus(int way){
////		boolean a = !body.get(index).getHost().changeFocus(way);
////		if (a){
////			body.get(index).getHost().resetFocus(way != 1);
////		}
////		return a;
//        return body.get(index).getHost().changeFocus(way);
//	}

//	@Override
//	protected boolean setFocused(){
//		if (isEnabled() && isOpen() && isFocusable()){
//			body.get(index).getHost().enableFocus();
//		}
//		return super.setFocused();
//	}


    @Override
    public void onFocus() {
		// tiesiog mygtukus fokusuosim.
		for (int a = 0; a < controls.size(); a++){
			Button button = controls.get(a);
			if (button.isEnabled()){
				button.getFocus(); // sioj vietoj tabas praras focusa.
				return;
			}
		}
    }

    // on lost focus nereik, nes pats save cancelins.
//    @Override
//	public void onLostFocus() {
//		for (Button e : controls){
//			e.loseFocus();
//		}
//		body.get(index).loseFocus();
//	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		if (super.pan(x, y, deltaX,deltaY) || body.get(index).pan(x, y, deltaX, deltaY)){
			return true;
		}
		for (Button e : controls){
			if (e.pan(x, y, deltaX, deltaY)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		if (super.panStop(x, y, pointer, button) || body.get(index).panStop(x, y, pointer, button)){
			return true;
		}
		for (Button e : controls){
			if (e.panStop(x, y, pointer, button)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		if (super.longPress(x, y) || body.get(index).longPress(x, y)){
			return true;
		}
		for (Button e : controls){
			if (e.longPress(x, y)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		return super.fling(velocityX, velocityY, button) || body.get(index).fling(velocityX, velocityY, button);
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return super.scrolled(amountX, amountY) || body.get(index).scrolled(amountX, amountY);
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		return super.zoom(initialDistance, distance) || body.get(index).zoom(initialDistance, distance);
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		if (super.pinch(initialPointer1, initialPointer2, pointer1, pointer2) ||
				body.get(index).pinch(initialPointer1, initialPointer2, pointer1, pointer2)){
			return true;
		}
		for (Button e : controls){
			if (e.pinch(initialPointer1, initialPointer2, pointer1, pointer2)){
				return true;
			}
		}
		return false;
	}

	@Override
	public void pinchStop() {
		super.pinchStop();
		body.get(index).pinchStop();
		for (Button e : controls){
			e.pinchStop();
		}
	}

	@Override
	public boolean keyTyped(char e){
		return super.keyTyped(e) || body.get(index).keyTyped(e);
	}

	@Override
	public void release(){
//		super.release();
		for (int a = 0; a < body.size(); a++){
			body.get(a).release();
			Button e = controls.get(a);
			if (e.isVisible() && e.isEnabled()){
				e.release();
			}
		}
	}

	@Override
	protected void auto() {

	}

	/* listener */

	private boolean indexChanged(int old, int current){
		if (listener != null){
			return !listener.tabIndexChange(old, current);
		}
		return true;
	}

	/** set listener to listen when tab is being changed. */
	public void setTabIndexChangedListener(TabIndexChangedListener e){
		listener = e;
	}

	/** listener of this tab */
	public TabIndexChangedListener getTabIndexChangedListener(){
		return listener;
	}

	private class TabButtonAction implements ClickListener{
		private int id;
		private Button owner;

		TabButtonAction(int id, Button owner){
			this.id = id;
			this.owner = owner;
		}

//		public void setNewId(final int id){ // leis salint tabus
//			this.id = id;
//		}

		@Override
		public void onClick(){
//				checkStatus(id);
			if (indexChanged(index, id)) { // pazourim ar listener leidzia perjungt.
				body.get(index).removeFocus(); // salinam fokusa jei toks yra.
				index = id;
				owner.setEnabled(false);
				enableControls(id);
			}
//			backgroundColor = 255; // kazkas tokio // reik default disabled color pakeist.
		}
	}

	public enum ButtonsPosition{
		TOP, BOTTOM, LEFT, RIGHT
	}

	public interface TabIndexChangedListener{
		/** called when index is being changed.
		 * @return true to cancel index change */
		boolean tabIndexChange(int old, int current);
	}

	/* style */

	@Override
	public TabControlStyle getStyle(){
		TabControlStyle e = new TabControlStyle();
		copyStyle(e);
		return e;
	}

	public void copyStyle(TabControlStyle st){
		super.copyStyle(st);
		st.buttonsStyle = getButtonStyle();
		st.maxButtonWidth = getMaxButtonWidth();
		st.buttonsOffsetX = getButtonsOffsetX();
		st.maxWidth = getMaxWidth();
		st.maxHeight = getMaxHeight();
		st.buttonsPosition = getButtonsPosition();
	}

	public void readStyle(TabControlStyle st){
		super.readStyle(st);
		setbuttonStyle(st.buttonsStyle);
//		maxButtonWidth = st.maxButtonWidth;
		setMaxButtonWidth(st.maxButtonWidth);
		setButtonsOffsetX(st.buttonsOffsetX);
		setMaxSize(st.maxWidth, st.maxHeight);
		setButtonsPosition(st.buttonsPosition);
	}

	public static class TabControlStyle extends InterfaceStyle{
		public ButtonStyle buttonsStyle = new ButtonStyle();
		public float maxButtonWidth = Engine.getInstance().getScreenWidth()*0.5f;
		public float buttonsOffsetX = 0f;
		public float maxWidth = Engine.getInstance().getScreenWidth(),
				maxHeight = Engine.getInstance().getScreenHeight();
		public ButtonsPosition buttonsPosition = ButtonsPosition.TOP;

		public TabControlStyle(){
			buttonsStyle.textSize = 28;
			buttonsStyle.disabledColor = 0xFFFFFFFF; // balta.
		}

		@Override
		public TabControl createInterface() {
			return new TabControl(this);
		}
	}
}
