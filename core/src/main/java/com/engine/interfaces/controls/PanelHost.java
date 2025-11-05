package com.engine.interfaces.controls;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.core.Engine;
import com.engine.interfaces.controls.views.Panel;

import java.util.List;

public class PanelHost extends PopUp{
	private InterfacesController v;
	private boolean hasBackground;
	private boolean enabled;
	private int disabledColor;
	private final ActionChanger listener;
	float controlX, controlY;
//	private Interface owner;
//	private int strokeColor, strokeLength;
	private Panel owner; // tas, kuris valdo sita hosta.

//	private final float rez6;

	public PanelHost(float x, float y, float xS, float yS) {
		super(x, y, xS, yS);
		requireCordsTranslation(false); // nereikia verst kordinaciu du kartus.
		disabledColor = Engine.color(100, 100, 100, 204); // pilka.
		setVisible(true);
		hideCloseButton(true);
		enabled = true;
		listener = new ActionChanger();
//		rez6 = Engine.getWithRez(6);
	}

//	public void setOwner(Interface e){
//	    owner = e;
//    }

	/** owner is set in panel automatically. */
	public void setOwner(Panel owner){
		this.owner = owner;
	}

	@Override
	public float getOffsetX() {
		return controlX;
	}

	@Override
	public float getOffsetY() {
		return controlY;
	}

	public void setControllerOffset(float x, float y){
		controlX = x;
		controlY = y;
	}

	public void setController(InterfacesController v){
		if (this.v != null){
			v.removeActionListener(listener);
		}
		controlX = 0;
		controlY = 0;
		this.v = v;
		if (v != null)
			v.addActionListener(listener);
	}

	public InterfacesController getController() {
		return v;
	}

	public void onRemove(){
		if (v != null){
			v.removeActionListener(listener);
		}
	}

	@Override
	protected void popBackground() {
		Vector2 pos = getPosition();
		if (hasBackground) {
			drawBackground(pos.x + controlX, pos.y + controlY, xSize, ySize);
		}
		disabledView(pos.x + controlX, pos.y + controlY, xSize, ySize);
	}

	protected void disabledView(float x, float y, float w, float h){
		if (!enabled) {
			p.fill(disabledColor);
			p.noStroke();
			p.rect(x, y, w, h);
		}
	}

	boolean hasBackground(){
		return hasBackground;
	}

	public void setDisabledColor(int color){
		disabledColor = color;
	}

	@Override
	public void setBackground(Drawable color){
		background = color;
		hasBackground = color != null;
	}

	@Override
	public void setVisible(boolean visible){
		show = visible;
		setAction(show && enabled);
	}

	public void setEnabled(boolean enabled){
		if (this.enabled == enabled)
			return;
		this.enabled = enabled;
		if (enabled){
			setAction(v.isActive());
		}else {
			setAction(false);
		}
	}

	public boolean isEnabled(){
		return enabled;
	}

	@Override
	public Form getForm(){ // panelio bloga forma.
		return v.getForm();
	}

	/* focus. */

	@Override
	void focusMe(Interface e) {
		super.focusMe(e); // cia fokusuos.
		if (getFocusedItem() != null){
			// kazka sufokusavo. Paziurim ar musu owner turi fokusa.
			// ir paziurim ar isvis owner yra.
			if (owner != null && !owner.isFocused()){
				// neturi fokuso. Jam reik atiduot fokusa.
				owner.getFocus();
			}
		}
	}

	@Override
	protected boolean focusNavigation(int e){
		return false;
	}  // nereikalingas. tik trugdytu

	/** switch focus inside. If currently focused last control and tries to focus forward then no focus occurs and return true. */
	public boolean changeFocus(boolean forward){
		if (getControls().size() <= 1){
			removeFocus();
			return true; // lai keicia fokusa.
		}

		if (getFocusedItem() == null){
			return true; // nieko ner fokusuoto.
		}

		// surandam fokusuota item.
		List<Interface> list = getControls();
		for (int a = 0; a < list.size(); a++){
			Interface in = list.get(a);
			if (in.isFocused()){
				// fokusuota.
				if (forward){
					if (a + 1 >= list.size()){
						// eina uz ribu. metam fokusa.
						removeFocus();
						return true;
					}else {
						// fokusuojam kita detale.
//						list.get(a+1).getFocus();
						return tryFocus(a+1, 1, true);
					}
				}else {
					if (a - 1 < 0){
						// uz ribu.
						removeFocus();
						return true;
					}else {
						// fokusuojam.
//						list.get(a-1).getFocus();
						return tryFocus(a-1, -1, true);
					}
				}
			}
		}

		// nieko neradom.
		removeFocus(); // del viso pikto.
		return true;
	}

	/** will get focus if there was no focus. */
	public void acquireFocus(){
		// tiesiog fokusuojam pirma detale.
		if (getFocusedItem() == null){
			// nera fokusinto. bandom fokusint nuo pirmo.
			tryFocus(0, 1, true);
		}
	}

	//	public boolean changeFocus(int way){
////		if (!allowChangeFocus(way)){
////			return true;
////		}
//		if (getControls().size() == 0){
//			return true;
//		}
//		if (!allowChangeFocus(way))
//			return false;
//		int old = getFocusedItemId();
//		focus(way);
//		if (way > 0){
//			if (getFocusedItemId() <= old){ // reiskia prasoko ir grizo i pirmuosius arba net i ta pati.
//				disableFocus();
//				return true;
//			}
//		}else {
//			if (getFocusedItemId() >= old){ // tas pats tik atvirksciai
//				disableFocus();
//				return true;
//			}
//		}
//		return false; // viskas normaliai focus persoko
//		int id = getFocusedItemId() + way;
//		if (id >= getControlsCount()){
//			resetFocus(true);
//			disableFocus();
//			return false;
//		}else if (id < 0){
//			resetFocus(false);
//			disableFocus();
//			return false;
//		}
//		focus(way);
//		int dabartinis = getFocusedItemId();
//		if (way == 1){
//			if (dabartinis < id){
//				resetFocus(false);
//				disableFocus();
//				return false;
//			}
//		}else{
//			if (dabartinis > id){
//				resetFocus(true);
//				disableFocus();
//				return false;
//			}
//		}
//		return true;
//	}

//    @Override
//    void focusMe(Interface e) {
//        owner.getFocus();
//        if (owner.isFocused())
//	        super.focusMe(e);
//    }

    @Override
	void interfaceViewHasChanged(Interface e) {
		super.interfaceViewHasChanged(e);
		if (getPositioning() == Window.absoluteView){
			if (e.getPositioning() == Window.fixedView){
				e.setPositioning(Window.Position.absolute);
			}
		}
	}

	@Override
	public void setPositioning(Window.Position e) {
		super.setPositioning(e);
		if (getPositioning() == Window.absoluteView){
			for (Interface k : getControls()){
				if (k.getPositioning() == Window.fixedView){
					k.setPositioning(Window.Position.absolute);
				}
			}
		}
	}

//	@Override
//	public int getPositioning() {
//		if (v == null)
//			return super.getPositioning();
//		return v.getPositioning();
//	}

	/** normal <code>getPositioning()</code> gives wrong position if there are multi panels. This method should be used instead. */
	public int getRealPositioning(){
		int pos = getPositioning();
		if (pos == Window.fixedView){
			return pos;
		}
		InterfacesController e = v;
		if (v != null) {
			pos = v.getPositioning();
//		if (v.getPositioning() == Window.fixedView)
//			return Window.fixedView;
			while (pos == Window.absoluteView && e instanceof PanelHost) { // jeigu bus fixed tai stabdys. jeigu ne tai eis iki galo.
				e = ((PanelHost) e).getController();
				pos = e.getPositioning();
			}
			return pos;
		}else {
			if (pos == Window.relativeView){
				pos = Window.absoluteView;
			}
			return pos;
		}
	}

	private class ActionChanger implements ActionListener {

		@Override
		public void actionChanged(boolean action) {
			if (enabled && show)
				setAction(action);
			else {
				setAction(false);
			}
		}
	}
}
