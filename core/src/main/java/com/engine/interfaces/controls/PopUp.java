package com.engine.interfaces.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.engine.core.Engine;
import com.engine.core.ErrorMenu;
import com.engine.core.Resources;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.listeners.Background;
import com.engine.interfaces.listeners.ClickListener;

/**
 * Pop up can handle controls, and show on top everything. Fixed pop up cannot handle absolute controls (absolute controls will be changed to fixed).
 * Absolute pop up can handle both fixed and absolute controls.
 */
public class PopUp extends InterfacesController {
	private Vector2 position;
	float xSize, ySize;
//	protected float nx, ny, nxSize, nySize;
//	protected int barBackground;
	protected Drawable background;
	protected Engine p = Engine.getInstance();
	boolean show;
	private Button turnOff;
	private float closeButtonOffsetX = -3, closeButtonOffsetY = -3;
	private OpenCloseListener listener;
	private Background back;
	protected int tintBackground = 0xFFFFFFFF;

//	private final float rez6;

	/** fixed positioning. */
	public PopUp(float x, float y, float xS, float yS){
		this(x, y, xS, yS, Window.Position.fixed);
	}

	public PopUp(float x, float y, float xS, float yS, Window.Position pos) {
//		Resources.addImage("PopUpDefault", "resources/ui/popupdefault.png");
		Drawable e = Resources.getDrawable("defaultPopUpBackground");
		if (e instanceof TextureRegionDrawable || e instanceof SpriteDrawable) { // texture region ir sprite tinka.
//			background = new NinePatchDrawable(new NinePatch(((TextureRegionDrawable) e).getRegion(), 12, 12, 12, 12));
			background = Resources.createNinePatchDrawable("defaultPopUpBackground", 12, 12, 12, 12);
		}else {
			background = e;
		}
		position = new Vector2(x, y);
		xSize = xS;
		ySize = yS;
//		setRezolution();
		setCloseButton();
		setPositioning(pos);
//		rez6 = Engine.getWithRez(6);
	}

	/** Only Pop up size. Automatically center in screen center. Fixed positioning */
	public PopUp(float xS, float yS){
		this((Engine.getInstance().getPreferredScreenWidth() - xS)/2, (Engine.getInstance().getPreferredScreenHeight() - yS) / 2, xS, yS);
	}

	private void setCloseButton(){
//		Resources.addImage("PopUpExitButton", "resources/ui/exit.png");
		Button.ButtonStyle style = new Button.ButtonStyle();
		style.background = Resources.getDrawable("defaultPopUpExit");
		style.width = 30;
		style.height = 30;
		style.autoSize = false;
		style.positioning = Window.Position.absolute;
		turnOff = new Button("", style);
		turnOff.setClickListener(new ClickListener() {
			@Override
			public void onClick() {
				if (!closeButtonPressed())
					PopUp.this.close();
			}
		});
		addControl(turnOff);
		updateCloseButtonLocation();
	}

	/** called when close button was pressed.
	 * @return false close button will close pop up, true close button will not close this pop up */
	protected boolean closeButtonPressed(){
		return false;
	}

//	protected void setRezolution(){
//		final float r = Engine.getSize();
//		nx = position.x * r;
//		ny = position.y * r;
//		nxSize = xSize * r;
//		nySize = ySize * r;
//	}

	@Override
	protected void onAdd(Interface v){
		Vector2 pos = v.getPosition();
//		int[] size = v.getCordLength();
		float nx, ny;
		if (v.isPositionSet()){
			nx = pos.x;
			ny = pos.y;
		}else {
			nx = pos.x + position.x;
			ny = pos.y + position.y;
		}
		float oldX = nx, oldY = ny;
		float width = 0;
		float height = 0;
		float offsetX = 0, offsetY = 0;
		if (v instanceof Field){
			width = ((Field) v).getWidth();
			height = ((Field) v).getHeight();
		}
		if (width > xSize || nx <= position.x){
			nx = position.x+1;
			offsetX = nx - oldX;
		} else if (nx + width > position.x + xSize){
			nx = position.x + xSize - width;
			offsetX = nx - oldX;
		}
//		else if (nx <= position.x){
//			nx = position.x+1;
//			offsetX = nx - oldX;
//		}
		if (height > ySize || ny < position.y){
			ny = position.y + 1;
			offsetY = ny - oldY;
		}else if (ny + height > position.y + ySize){
			ny = position.y + ySize - height;
			offsetY = ny - oldY;
		}
//		else if(ny < position.y){
//			ny = position.y + 1;
//			offsetY = ny - oldY;
//		}
		if (interfaceOffsetX(offsetX)){
			nx = oldX;
		}
		if (interfaceOffsetY(offsetY)){
			ny = oldY;
		}
		v.giveCords(nx, ny);
	}

	@Override
	public void setPositioning(Window.Position e) {
		if (getPositioning() == e.getPosition())
			return;
		super.setPositioning(e);
		if (getPositioning() == Window.fixedView) {
			for (Interface k : getControls()) {
				if (k.getPositioning() == Window.absoluteView){
					k.setPositioning(Window.Position.fixed);
				}
			}
		}else {
			if (turnOff.getPositioning() == Window.fixedView){
				turnOff.setPositioning(Window.Position.absolute);
			}
		}
	}

	@Override
	void interfaceViewHasChanged(Interface e) { // jeigu pop up fixed, tai jokiu absolute kontroliu.
		if (getPositioning() == Window.fixedView){
			if (e.getPositioning() == Window.absoluteView){
				e.setPositioning(Window.Position.fixed);
			}
		}
		super.interfaceViewHasChanged(e);
	}

	boolean interfaceOffsetX(float offsetX){
		return false;
	}

	boolean interfaceOffsetY(float offsetY){
		return false;
	}

	@Override
	public void handle(){ // reiks nusileidimo animacijos.
		if (show)
			super.handle();
	}

	@Override
	public void fixHandle() {
		if (show)
			super.fixHandle();
	}

	@Override
	void notifyCoordsChange(Interface a, float naujasX, float naujasY){
		if (a.getPositioning() == Window.relativeView) { // neturi liest kontroliu, kurios ne relative.
			float difX = naujasX - position.x; // nesvarbu neigiamas ar ne.
			float difY = naujasY - position.y;
			Vector2 oldCoords = a.getPosition();
			a.giveCords(oldCoords.x + difX, oldCoords.y + difY);
		}
//		setRezolution(); // kad nepamestu piesimo coords. ne cia pataike.
	} // leis judint visa pop up.

	@Override
	void notifyCoordsChange(float naujasX, float naujasY){
//		float difX = naujasX - position.x; // nesvarbu neigiamas ar ne.
//		float difY = naujasY - position.y;
//		Vector2 oldCoords = turnOff.getPosition();
//		turnOff.giveCords(oldCoords.x + difX, oldCoords.y + difY);
//		turnOff.giveCords(naujasX + getWidth() - turnOff.getWidth()/2, naujasY + getHeight() - turnOff.getHeight()/2);
		position.set(naujasX, naujasY);
		updateCloseButtonLocation();
//		setRezolution();
	} // pakeist visam pop up'ui coordinates.

	@Override
	public void drawBackground(){ // rezoliucija.... ok.
		if (getPositioning() == Window.absoluteView){
			popBackground();
		}
	}

	@Override
	public void drawFixedBackground() {
		if (getPositioning() == Window.fixedView){
			popBackground();
		}
	}

	protected void popBackground(){
		drawBackground(position.x, position.y, xSize, ySize);
	}

	protected void drawBackground(float x, float y, float w, float h){
		p.tint(tintBackground);
		background.draw(p.getBatch(),x, y, w, h);
		p.noTint();
		if (back != null)
			back.background();
	}

	void setVisible(boolean visible){
		if (visible != show){
			show = visible;
			if (getForm() != null){
				getForm().showPopUp(this, visible);
			}
			setAction(visible);
			if (!visible){
				onClose();
			}else {
				onOpen();
			}
		}
	}

	/** Opens this pop up in given form.
	 * @param e if form null pop up will be opened anyway but won't be displayed. You will have manually display it. */
	public final void open(Form e){
		if (!isOpen()) {
			setForm(e);
			setVisible(true);
		}else { // tik perspejam, nereguojam isvis.
			Window activeForm = p.getActiveForm();
			// paziurim kurioj formoj atsidares, jei toj pacioj, kur nori user, tai pranesm, kad open.
			if (e == activeForm) {
				Gdx.app.log("PopUp", "PopUp is already open!");
			}else {
				// jeigu kitoj formoj, tai uzdarom senoj formoj ir atidarom naujoje.
				close();
				open(e);
			}
		}
	}

	/** Opens this pop up in current active form. */
	public final void open(){
		if (!isOpen()) { // nelendam jei jau matomas, o jei kitoj formoj matomas? Ten uzstrigs sarase.
			Window e = p.getActiveForm(); // imama dabartine forma.
			if (e instanceof Form) { // forma gali nebut ta veikiancioji. gali but kita.
				setForm((Form) e);
			} else {
				// ne formos instance. Implementintas kazkoks windows savadarbis...
				Gdx.app.log("PopUp", "it's open but active forms instance is not Form instace. Cannot display pop up. Pop up will have to be displayed manually or it will not be displayed." +
						"To display pop up call handle() or fixHandle() methods in Window methods handle() or fixHandle(). Also don't forget to call input methods. Or just consider using Form instances " +
						"instead of implementing Window interface.");
			}
			setVisible(true);
		}else {
			// dar pries kazka darant paziurim kur atidarytas musu pop up.
			// user nenurode kurioj formoj atidaryt sita pop up tai galim panaglavot.
			// jeigu pop up atidarytas kitoj formoj nei dabar aktyvi, tai uzdarom pop up ir atidarom
			// dabar aktyvioj formoj! Realiai user nori atidaryto pop up ir nesvarbu, kad sis atsidares
			// kitoj formoj.
			Form form = getForm(); // forma kurioj atsidares pop up. Gali but ir null...
			Window activeForm = p.getActiveForm(); // dabar user matoma forma.
			if (form == activeForm) {
				// bandoma atidaryt toj pacioj formoj.. Paliekam log ir viskas...
				Gdx.app.log("PopUp", "PopUp is already open!");
			}else { // bandoma atidaryt kitoj formoj nei pats pop up yra atsidares.
				close(); // uzdarom musu pop up senoj formoj.
				open(); // atidarom is naujo naujoje formoje.
			}
		}
	}

	public final void close(){
		if (isOpen()) {
			setVisible(false);
			setForm(null); // paleidziam forma, kam ja laikyt?
		}else {
//			System.out.println("PopUp: this popUp is not opened.");
			Gdx.app.log("PopUp", "It's not open!");
		}
	}

	/** set to white to disable tinting. ARGB */
	public void tintBackground(int color){
		tintBackground = color;
	}

	public void setCloseButtonOffset(float offsetX, float offsetY){
		closeButtonOffsetX = offsetX;
		closeButtonOffsetY = offsetY;
	}

	public void setBackground(Drawable e){
		if (e != null){
			background = e;
		}
	}

	public void enableCloseButton(boolean enable){
		turnOff.setEnabled(enable);
	}

	public void hideCloseButton(boolean disable){
		turnOff.setVisible(!disable);
	}

	/** on close listener is called here */
	protected void onClose(){
		if (listener != null){
			listener.onClose();
		}
	}

	/** on open listener is called here */
	protected void onOpen(){
		if (listener != null){
			listener.onOpen();
		}
	}

	public boolean isOpen(){
		return show;
	}

	public void setSize(float size){
		setSize(size, size);
	}

	public void setSize(float width, float height){
		if (width <= 0 || height <= 0){
			p.setError("Pop up size cannot be <= 0", ErrorMenu.ErrorType.WrongPara);
			return;
		}
		xSize = width;
		ySize = height;
//		setRezolution();
		for (Interface e : getControls()){
			if (e.getPositioning() == Window.relativeView){
				onAdd(e);
			}
		}
		turnOff.setPosition((position.x + xSize - 3)-15, position.y + ySize - 12);
	}

	public float getWidth(){
		return xSize;
	}

	public float getHeight(){
		return ySize;
	}

	/** how much button is moved. */
	public float getCloseButtonOffsetX() {
		return closeButtonOffsetX;
	}

	/** how much button is moved. */
	public float getCloseButtonOffsetY() {
		return closeButtonOffsetY;
	}

	public Drawable getBackground() {
		return background;
	}

	public int getTintBackground(){
		return tintBackground;
	}

	/** aligns close button in upper left corner. */
	public void updateCloseButtonLocation(){
		Vector2 pos = getPosition();
		turnOff.giveCords(pos.x + getWidth() - turnOff.getWidth()/2 + closeButtonOffsetX,
				pos.y + getHeight() - turnOff.getHeight()/2 + closeButtonOffsetY);
	}

	/** set turn off button background. If null then make sure to hide close button. */
	public void setCloseButtonBackground(Drawable e){
		turnOff.setBackground(e);
	}

	/** turn off button of this pop up. If you change size of button or any other attribute then call {@link #updateCloseButtonLocation()} to
	 * update it's location. */
	public Button getCloseButton(){
		return turnOff;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (super.keyDown(keycode)){
			return true;
		}else{
			// escape arba atgal mygtukas reaguos. Taip pat isjungimo mygtukas turi but matomas ir enablintas.
			if ((keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) && turnOff.isVisible() && turnOff.isEnabled()){
				this.setVisible(false);
				return true;
			}
			return false;
		}
	}

	public void setCloseListener(OpenCloseListener e){
		listener = e;
	}

//	public int[] getCoords(){
//		return new int[]{x, y};
//	}

	public Vector2 getPosition(){
		return position;
	}

	public void addBackgroundListener(Background e){
		back = e;
	}

	public interface OpenCloseListener {
		/** Called when pop up is closing */
		public void onClose();
		/** Called when pop up is opening */
		public void onOpen();
	}
}
