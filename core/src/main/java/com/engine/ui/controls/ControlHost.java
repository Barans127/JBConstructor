package com.engine.ui.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;
import com.engine.ui.controls.Window.Position;
import com.engine.ui.controls.widgets.TabControl;
import com.engine.ui.listeners.FocusListener;

import java.util.ArrayList;
import java.util.List;

/** Default control handler. No restrictions for controls. */
public class ControlHost {
	private final ArrayList<Control> control; // visos kontroles
	private final Array<Connected> connections; // connectionai. radio button etc.
	private final Array<ActionListener> actionListeners; // action listenners.
	private boolean react = true, viewRequireUpdate, requireCordsTranslation = true; // vidaus veikimas
	private Form form; // forma
//	private int focusedItem, fixedItemStart;
	private int fixedItemStart; // nuo kur prasides fixed item.
	private int positioning = Window.absoluteView; // kokia sio kontrolerio pozicija.
	private final Vector3 mouseCoords; // keitimui del input.
    private final CoordTranslator coordTranslator = new CoordTranslator();

	// focus
	private Control focusedItem; // item, kuris yra siuo metu sufokusuotas.

	// frustum.
	boolean enableFrustum = false; // defaulitskai isjungiam.
	float prevCamX, prevCamY, prevZoom; // kad nekviestume frustum kaskart be reikalo.
	int frustumUpdateId; // nustatyt kelintas cameros update.

	public ControlHost(){
		connections = new Array<>(10);
		actionListeners = new Array<>(10);
		control = new ArrayList<>();
		mouseCoords = new Vector3();
	}

	public float getOffsetX(){
		return 0;
	}

	public float getOffsetY(){
		return 0;
	}

	/* Frustum igalinimas */

	/** Is controls frustum checked. Controls outside camera bounds will not be drawn or handled. */
    @SuppressWarnings("unused") // To not show warnings. This is to get value. Other projects might use it.
	public boolean isFrustumEnabled(){
		return enableFrustum;
	}

	/** Enables/disables controls frustum. Frustum is not checked on fixed controls therefore only controller
	 * with absolute positioning can use this feature.
	 * For simple panel, popUp or interface controller: Default: false.
	 * For scrollView, Listview: default: true. NOTE: Scroll views and listViews use different frustum method (positioning doesn't apply to it therefore both absolute and fixed controls have
	 * frustum feature).*/
	public void setEnableFrustum(boolean enableFrustum){
		this.enableFrustum = enableFrustum;
		positioningChanged(); // cia patikrins ar galima frustum naudot.
	}

	/* focus */

	/** Currently focused item. If nothing focused then null. */
	public Control getFocusedItem(){
		return focusedItem;
	}

	/** Removes focus if there was one. Calls listener if focused item have one. */
	public void removeFocus(){
		if (focusedItem != null){ // paziurim ar kazkas buvo fokusuotas.
			if (focusedItem.getStatus() == Control.OVER){
				focusedItem.normalStatus();
			}
			FocusListener focusListener = focusedItem.getFocusListener();
			focusedItem.onLostFocus(); // iskvieciam vidini listener.
			if (focusListener != null){ // jei buvo tai pranesam, kad nebera focuso.
				focusListener.onLostFocus(focusedItem);
			}
			focusedItem = null; // nuimam focusa.
		}
	}

	void focusMe(Control e){
		// paziurim ar fokusuot ja galima. turi but enablinta ir matoma.
		if (focusedItem != e) { // aisku jei tas pats kviecia, tai nenuimam.
			// buna atveju (pvz label), nefokusuojama kontrole, ant kurios paspaudzia. Nedingsta focus nuo text block, nes label nefokusuojama. tai cia toks fix
			removeFocus(); // isimam focusa. jeigu bet kas kviecia, bet kokiu atveju vistiek nuimam fokusa.
			if (e.isFocusable() && e.isEnabled() && e.isVisible()) { // nedarom tos pacios fokuso. Kad nebutu double call.
				focusedItem = e; // nustatom nauja fokusuota item.
				focusedItem.onStatus();
				e.onFocus(); // vidini listener kvieciam.
				FocusListener focusListener = e.getFocusListener();
				if (focusListener != null) {
					focusListener.onFocus(focusedItem);
				}
			}
		}
	}

	// cia tiesiog supaprastint, kad susigaudytu kokie mygtukai spaudziami.
	boolean focusNavigation(int k){
		if (Gdx.input.isTouched()){
			return false;
		}
		if (k == Input.Keys.LEFT || k == Input.Keys.UP){
//			focus(-1); // einam atgal
			moveFocus(false);
			return true;
		}else if (k == Input.Keys.RIGHT || k == Input.Keys.DOWN || k == Input.Keys.TAB) {
			// tab, desine, zemyn tas pats.
//			focus(1); // einam i prieki.
			moveFocus(true);
			return true;
		}
		return false;
	}

	private void moveFocus(boolean forward){
		if (control.size() <= 1){
			// 0 arba 1 kontrole. ka ten fokusuosi.
			return; // ner ka fokusuot.
		}
		int index = -1;
		if (focusedItem != null){
			// paklausiam ar leidzia keist focusa.
			if (focusedItem.moveFocus(forward)) {
				// darom savo nuoziura.
				for (int a = 0; a < control.size(); a++) {
					Control in = control.get(a);
					if (in == focusedItem) {
						index = a; // surandam indexa.
					}
				}

				if (index == -1){ // nerado nieko??
					//cia reisk susidurem su tuo tab control.
					// ieskom tab control.
					for (int a = 0; a < control.size(); a++) {
						Control in = control.get(a);
						if (in instanceof TabControl){
							if (((TabControl) in).containsControl(focusedItem)){
								// radom musu tab.
								// bandom keist focusa.
								if (!in.moveFocus(forward)){
									return; // sake nekeist viduj pakeis./
								}else { // jeigu leidzia keist, tai duodam tabo indexa.
									index = a;
								}
							}
						}
					}
				}

				// judinam i ta puse kuria reik.
				if (forward){
					index++;
					if (index >= control.size()){
						index = 0;
					}
				}else {
					index--;
					if (index < 0){
						index = control.size()-1;
					}
				}
			}else { // neleido.
				return; // nieko nedarom.
			}
		}else {
			// nieks nefokusuota tai ir pradedam nuo ten kur reik.
			if (forward)
				index = 0;
			else {
				index = control.size()-1;
			}
		}

		// cia bandysim fokusuot.
		tryFocus(index, forward ? 1 : -1, false);
	}

	// no corection, kad neitu rato is naujo.
	// cia tam, kad paneliui sustotu. tik del jo.
	// true nepavyko fokuso suteikt, false pavyko.
	/** Tries to focus control. If fail to focus control then true. */
	boolean tryFocus(int startingIndex, int way, boolean noCorrection){
		int old = startingIndex;
		int count = 0; // antroji apsauga ka nebutu infinity loop.
		List<Control> controls = getControls();
		if (startingIndex < 0 || startingIndex >= controls.size() || controls.size() <= 1){
			return true; // kazkas negerai.
		}
		while (true){
			// index ka itilptu.
			if (startingIndex >= controls.size()){
				startingIndex = 0;
				if (noCorrection && way > 0){
					// eina i prieki ir pasiek riba.
					return true; // visks.
				}
			}else if (startingIndex < 0){
				startingIndex = controls.size()-1;
				if (noCorrection && way < 0){
					// pasieke riba. eina atgal.
					return true;// vsio.
				}
			}

			// bandom fokusint.
			Control in = controls.get(startingIndex);
			if (in.isFocusable() && in.isEnabled() && in.isVisible()){
				if (in.isFocused()){
					return true; // pasieke ta pati.
				}
				in.getFocus(); // fokusuojam.
				return false; // pranesamo kad ok.
			}

			startingIndex += way; // judam toliau.

			// pasiekem pradini taska.
			if (startingIndex == old){
				break;
			}
			// dar viena apsauga del infinity loop.
			if (count >= controls.size()){ // cia jau karta praejo lista. stop.
				break;
			}
			count++;
		}
		return true;
	}

	/* control valdymas */

	/** pašalins iš sąrašo, nurodytą kontrolę. Pašalinta kontrolė, nebebus formoj. */
	public void removeControl(Control e){
		synchronized (control) {
//			disableFocus();
//			focusedItem = 0; // kad nebutu nesamoniu...
			if (focusedItem == e){ // jeigu salinamas item yra focusuotas tai salinam focusa.
				removeFocus();
			}
			if (!control.remove(e)) {
//				System.out.println("Failed removing control");
				Gdx.app.log("InterfacesController", "Control was not removed");
				return;
			}
			e.onRemove(); // pries isimant kontroleri.
			e.removeController();
			viewRequireUpdate = true;
		}
	}

	/** Get all controls in this controller. */
	public List<Control> getControls(){
		synchronized (control) {
			return control;
		}
	}

	/** Get control by it's id. */
    @SuppressWarnings("unused") // Might be used by other project in the future.
	public Control getControlById(String id){
	    for (Control e : control){
	        if (e.getIdName().trim().equals(id)){
	            return e;
            }
        }
        return null;
    }

	protected boolean checkControlIdName(Control e, String futureName){
		if (futureName == null) {
			return false;
		}
		futureName = futureName.trim();
		if (futureName.isEmpty())
			return false;
		for (Control a : control){
			if (a == e){ // save ignorins
				continue;
			}
			if (a.getIdName().equals(futureName)){
				return false;
			}
		}
		return true;
	}

	/** asks Controller if interface is visible. manipulate this to control your interfaces. */
	protected boolean amIVisible(Control e){
		if (enableFrustum) {
			if (e.getPositioning() == Window.fixedView || getPositioning() == Window.fixedView){
				// fixed interface arba sis kontroleris fixed tai viskas, nieko nebeziurim daugiau.
				return true; // fixed interfecam net netikrinam.
			}else {
				if (e instanceof Field) {
					// absolute field interface.
					OrthographicCamera camera = Engine.getInstance().getAbsoluteCamera();
					// paziurim ar kameros coord keites.
					if (camera.position.x == prevCamX && camera.position.y == prevCamY && camera.zoom == prevZoom){
						// sutampa.
						if (frustumUpdateId == e.frustumUpdateId) { // sutampa update id.
							return e.frustumVisible; // grazinam viska kaip priklauso.
						}
					}else { // nesutapo, atsinaujinam.
						// surenkam info.
						prevCamX = camera.position.x;
						prevCamY = camera.position.y;
						prevZoom = camera.zoom;
						frustumUpdateId++; // pazymim, kad naujas update.
					}
					// toliau updatinam controle.
					Field field = (Field) e;
					if (field.update){
						// mums reik updato!
						// darom update.
						field.auto();
						if (field.getPositioning() == Window.relativeView)
							onAdd(field); // pasikeitus dydziui, pranes pop up..

						field.update = false;
					}
					Vector2 pos = field.getMiddlePoint();
					// pries frustum issisaugom senus parametrus.
					// ziurim ar versta detale.
					float width, height;
					if (field.isRotatable() && field.getAngle() != 0){
						// vertimo atveju naudojam kubo metoda.
						float max = Math.max(field.getWidth(), field.getHeight());
						width = max/2;
						height = max/2;
					}else {
						// paprastai tai imam taip kaip yra.
						width = field.getWidth()/2;
						height = field.getHeight()/2;
					}
					boolean seen = camera.frustum.boundsInFrustum(pos.x + getOffsetX(), pos.y + getOffsetY(), 0, width, height, 0);
					// suzymim viska i interface.
					e.frustumVisible = seen;
					e.frustumUpdateId = frustumUpdateId;
					return seen;
				}else {
					return true; // Jeigu ne field tai neaisku kaip cia piesimas vyksta.
				}
			}
		}else {
			return true;
		}
	}

	private void giveIdName(Control e){
		String startName;
		if (e.getIdName() != null){
			if (e.setIdName(e.getIdName())){
				return; // name turi jau ir nereik keist.
			}
			startName = e.getIdName(); // nepametam seno name, tiesiog pridesim skaiciuka prie jo.
		}else {
			startName = e.getClass().getSimpleName(); // jei isvis vardo neturi tai imsim klases varda.
		}
		int count = 0;
		String finalName;
		do {
			count++;
			finalName = startName + count;
		} while (!e.setIdName(finalName));
	}

	public void addControl(Control... a){
		for (Control e : a){
			addControl(e);
		}
	}

	/** Prideda nurodytą kontrolę į sąrašą, jei sąraše kontrolė egzistuoja, tai antra kartą ji nebus pridėta. */
	public boolean addControl(Control contr){
		synchronized (control) {
			if (contr == null) {
				return false;
			}
			for (int a = 0; a < control.size(); a++) {
				if (contr == control.get(a)) { // neprides tu paciu kontroliu.
					return false;
				}
			}
			contr.setController(this);
			giveIdName(contr);
			control.add(contr);
			interfaceViewHasChanged(contr); // kad pranestu...
			contr.frustumUpdateId = -1;
			return true;
		}
	}

	public boolean addControl(Control contr, int index){
		if (contr == null){
			return false;
		}
		if (control.contains(contr)){
			return false;
		}
		if (index < 0 || index >= control.size()){
			return addControl(contr);
		}
		synchronized (control) {
			contr.setController(this);
			giveIdName(contr);
			control.add(index, contr);
			interfaceViewHasChanged(contr);

			contr.frustumUpdateId = -1;
			return true;
		}
	}

	/* forms. */

	void setForm(Form a){
//		onFormChange(a, form);
		form = a;
	}

	public Form getForm(){
		return form;
	}

	/* handling. drawing. */

	/** absolute background. */
	protected void drawBackground(){}

	/** Handles absolute controls. Also remap view if needed. */
	public void handle(){
		if (viewRequireUpdate){
			remapView();
			viewRequireUpdate = false;
		}
		drawBackground();
		drawInterfaces(true);
	}

	protected void drawInterfaces(boolean isAbsoluteDraw){ // cia tik absolute!!
		if (isAbsoluteDraw) {
			for (int a = 0; a < fixedItemStart; a++) { // visus is eiles.
				Control e = control.get(a);
				if (e.isVisibleOnScreen() && drawInterface(e, false))
					e.handle();
			}
		}else {
			for (int a = fixedItemStart; a < control.size(); a++){
				Control e = control.get(a);
				if (e.isVisibleOnScreen() && drawInterface(e, true)){
					e.handle();
				}
			}
		}
	}

	/** override this to control interface drawing.
	 * @return returns true - interface will be drawn. false - interface will not be drawn. */
	protected boolean drawInterface(Control e, boolean isFixed){
		return true;
	}

	/** fixed background. */
	public void drawFixedBackground(){}

	/** Draws fixed controls. */
	public void fixHandle(){
		drawFixedBackground();
		drawInterfaces(false);
	}

	/* action control. Should react or not. */

	/** Ar controleris turi reaguot į paspaudimus ir kitus event. */
	public void setAction(boolean rec){ // uzdraus regavima i pelyte ar paspaudimus.
		if (rec == react)
			return;
		react = rec;
		if (!rec){
			release();
		}
		for (int a = 0; a < actionListeners.size; a++){
			actionListeners.get(a).actionChanged(rec);
		}
	}

	public interface ActionListener {
		void actionChanged(boolean action);
	}

	/*
	 * paneliui, kad zinotu, kas kur yra...
	 */
	public void addActionListener(ActionListener e){ // add kai pridedama i sarasa. set - kai tik vienas gali but.
		synchronized (actionListeners) {
			if (e != null)
				actionListeners.add(e);
		}
	}

	public void removeActionListener(ActionListener e) {
		synchronized (actionListeners) {
			if (e != null) {
				actionListeners.removeValue(e, true);
			}
		}
	}

	public boolean isActive(){ // suzinot ar reguoja forma.
		return react;
	}

	/* place stuff. Used in panel ir pop up. */

	protected void onAdd(Control peace){} // pop up ar kitom klasem.

	public final void setPosition(float x, float y){ // naudojama terp pop up ar kt. cia nenaudojama....
		for (int a = 0; a < control.size(); a++){
			notifyCoordsChange(control.get(a), x, y);
		}
		notifyCoordsChange(x, y); // paleidziamas paskutinis, kad nepamestu senu cord.
	}

	public final void setPosition(Vector2 e){
		setPosition(e.x, e.y);
	}

	void notifyCoordsChange(Control a, float x, float y){} // ... naudos pop up.

	void notifyCoordsChange(float x, float y){} // bendrai visam langui.

	/* positioning. */

	// patikrint ar galima naudot frustuma.
	protected void positioningChanged(){
		ControlHost controller = this;
		int pos;
		if (controller instanceof PanelHost){
			pos = ((PanelHost) controller).getRealPositioning();
		}else {
			pos = getPositioning();
		}

		if (pos == Window.fixedView){
			enableFrustum = false;
		}
	}

	public void setPositioning(Position e){
		if (e.getPosition() == positioning)
			return;
		if (e.getPosition() == Window.relativeView){ // controleriui relative nieko neduotu.
			// uzknisi su sitais..
//			System.out.println("relative view for " + this.getClass().getSimpleName() + " is same as absoluteView");
			if (positioning == Window.absoluteView){
				positioningChanged();
				return;
			}
			positioning = Window.absoluteView;
		}else{
			positioning = e.getPosition();
		}
		viewRequireUpdate = true;
		positioningChanged();
	}

	public int getPositioning(){
		return positioning;
	}

	/* remapinimas, perdeliojimas fixed i virsu. */

	/**
	 * Sudėlioja kontroles pagal jų positioning nustatymus.
	 */
	public void updateView(){
		viewRequireUpdate = true;
	}

	void interfaceViewHasChanged(Control e){
		viewRequireUpdate = true;
	}

	private void remapView(){
//		if (checkFixSize){
		synchronized (control) {
			int size = 0;
			for (Control e : control) {
				if (e.getPositioning() == Window.fixedView) {
					size++;
				} else if (positioning == Window.fixedView && e.getPositioning() == Window.relativeView) { // kai fixed view, tai kontroles su relative view irgi skaito kaip fixed.
					size++;
				}
			}
			fixedItemStart = control.size() - size;
			if (fixedItemStart == 0) { // jeigu visos kontrolės fixed, tai nieko keist nereik.
				return;
			} else if (fixedItemStart == control.size() && positioning == Window.absoluteView) { // jeigu nėra fixed controliu ir vaizdas absolute, nieko keist nereik.
				return;
			}
			// kontroliu perdėliojimas nauja tvarka.
			Control[] tmpList = new Control[control.size()];
			for (int a = 0; a < control.size(); a++) { // visų esamu kontrolių įtraukims į sąraša
				tmpList[a] = control.get(a);
			}
			control.clear(); // išvalymas, ness iš naujo bus perdėliota.
			for (Control aTmpList : tmpList) { // absolute paimimas.
				if (aTmpList.getPositioning() == Window.absoluteView ||
						(positioning == Window.absoluteView && aTmpList.getPositioning() == Window.relativeView)) {
					control.add(aTmpList);
				}
			}
			for (Control aTmpList : tmpList) { // fixed paimimas.
				if (aTmpList.getPositioning() == Window.fixedView ||
						(positioning == Window.fixedView && aTmpList.getPositioning() == Window.relativeView)) {
					control.add(aTmpList);
				}
			}
		}
	}

	/* connections. radio button etc. */

	public Array<Connected> getConnections() {
		return connections;
	}

	public boolean addConnection(Connected e){
		synchronized (connections) {
			for (Connected a : connections) {
				if (e == a)
					return false;
			}
			connections.add(e);
			return true;
		}
	}

	public boolean removeConnection(Connected e){
		synchronized (connections) {
			return connections.removeValue(e, true);
		}
	}

	/** Inform other connections that something happened. */
	public void inform(Connected e, int reason){
		int group = e.getGroup();
		for (int a = 0; a < connections.size; a++) { // visus is eiles.
			Connected k = connections.get(a);
			if (k != e && k.getGroup() == group){
				k.inform(reason);
			}
		}
	}

	/** Inform all group in connection that something happened. */
	public void informGroup(int group, int reason){
		for (int a = 0; a < connections.size; a++) { // visus is eiles.
			Connected k = connections.get(a);
			if (k.getGroup() == group){
				k.inform(reason);
			}
		}
	}

	/* inputs. */

	// ar pries kvieciant input keist x ir y i absolute ar fixed.
	void requireCordsTranslation(boolean require){
		requireCordsTranslation = require;
	}

	protected void keyTyped(Control c, char e){}

	public boolean keyTyped(char e){
		if (react){
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisible() && k.isEnabled()){
					if (k.keyTyped(e)){
						keyTyped(k, e);
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Releases controls from pressed status etc. */
	public void release(){
		for (int a = 0; a < control.size(); a++){
			Control e = control.get(a);
			if (e.isVisible()){
				e.release();
			}
		}
	}

	protected void touchDown(Control c, float x, float y, int pointer, int button){}

	public boolean touchDown(float x, float y, int pointer, int button) {
		if (react){
            coordTranslator.reset(); // Preparing...
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisibleOnScreen()){
//					setCoordinates(k, x, y);
                    coordTranslator.prepareCoords(k, x, y); // Now it does it's magic. No duplicates.
                    // Test purposes for inputs.
//                    TestPointClick click = new TestPointClick();
//                    click.pointDraw(mouseCoords.x, mouseCoords.y, coordTranslator.isPositionFixed(k));
					if (k.touchDown(mouseCoords.x, mouseCoords.y, pointer, button)){
						touchDown(k, mouseCoords.x, mouseCoords.y, pointer, button);
						return true;
					}
				}
			}
			// jeigu niekas nesuregavo, tai nuimam fokusa.
			removeFocus();
		}
		return false;
	}

	protected void keyDown(Control c, int keycode){}

	public boolean keyDown(int keycode) {
		if (react){
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisible() && k.isEnabled()){
					if (k.keyDown(keycode)){
						keyDown(k, keycode);
						return true;
					}
				}
			}
			if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
				if (keycode == Input.Keys.ESCAPE) {
//					disableFocus();
					removeFocus(); // escape irgi nuims. ar tai gera ideja?
//					return true; // jokiu true po sito.
				}else
					return focusNavigation(keycode);
			}
		}
		return false;
	}

	protected void keyUp(Control c, int keycode){}

	public boolean keyUp(int keycode) {
		if (react){
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisible() && k.isEnabled()){
					if (k.keyUp(keycode)){
						keyUp(k, keycode);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void mouseMoved(Control c, float x, float y){}

	public boolean mouseMoved(float x, float y) {
		boolean isActive = false; // kad prasuktu pro visus ir atleistu uzejima.
		if (react){
            coordTranslator.reset(); // Preparing...
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisibleOnScreen()){
//					setCoordinates(k, x, y);
                    coordTranslator.prepareCoords(k, x, y); // No duplicate projection.
					if (isActive){
						k.release();
					}else if (k.mouseMoved(mouseCoords.x, mouseCoords.y)){
						mouseMoved(k, mouseCoords.x, mouseCoords.y);
						isActive = true;
					}
				}
			}
		}
		return isActive;
	}

	protected void scrolled(Control c, float amountX, float amountY){}

	public boolean scrolled(float amountX, float amountY){
		if (react){
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisibleOnScreen() && k.isEnabled()){
					if (k.scrolled(amountX, amountY)){
						scrolled(k, amountX, amountY);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void tap(Control c, float x, float y, int count, int button){}

	public boolean tap(float x, float y, int count, int button) {
		if (react){
            coordTranslator.reset();
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisibleOnScreen()){
//					setCoordinates(k, x, y);
                    coordTranslator.prepareCoords(k, x, y);
					if (k.tap(mouseCoords.x, mouseCoords.y, count, button)){
						tap(k, mouseCoords.x, mouseCoords.y, count, button);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void longPress(Control c, float x, float y){}

	public boolean longPress(float x, float y) {
		if (react){
            coordTranslator.reset();
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisibleOnScreen()){
//					setCoordinates(k, x, y);
                    coordTranslator.prepareCoords(k, x, y);
					if (k.longPress(mouseCoords.x, mouseCoords.y)){
						longPress(k, mouseCoords.x, mouseCoords.y);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void fling(Control c, float velocityX, float velocityY, int button){}

	public boolean fling(float velocityX, float velocityY, int button) {
		if (react){
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisibleOnScreen() && k.isEnabled()){
					if (k.fling(velocityX, velocityY, button)){ // pixels per second
						fling(k, velocityX, velocityY, button);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void pan(Control c, float x, float y, float deltaX, float deltaY){}

	public boolean pan(float x, float y, float deltaX, float deltaY) {
		if (react){
            coordTranslator.reset();
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisibleOnScreen() && k.isEnabled()){
//					setCoordinates(k, x, y);
                    coordTranslator.prepareCoords(k, x, y);
					if (k.pan(mouseCoords.x, mouseCoords.y, deltaX, deltaY)){
						pan(k, mouseCoords.x, mouseCoords.y, deltaX, deltaY);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void panStop(Control c, float x, float y, int pointer, int button){}

	public boolean panStop(float x, float y, int pointer, int button) {
		if (react){
            coordTranslator.reset();
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisibleOnScreen()){
//					setCoordinates(k, x, y);
                    coordTranslator.prepareCoords(k, x, y);
					if (k.panStop(mouseCoords.x, mouseCoords.y, pointer, button)){
						panStop(k, mouseCoords.x, mouseCoords.y, pointer, button);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void zoom(Control c, float initialDistance, float distance){}

	public boolean zoom(float initialDistance, float distance) {
		if (react){
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisibleOnScreen() && k.isEnabled()){
					if (k.zoom(initialDistance, distance)){
						zoom(k, initialDistance, distance);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void pinch(Control c, Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1,
                         Vector2 pointer2){}

	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		if (react){
            coordTranslator.reset(); // Preparing for the translation.
			for (int a = control.size()-1; a >= 0; a--){
				Control k = control.get(a);
				if (k.isVisibleOnScreen()){
                    // Vector info will be transfered to coordTransaltor intances. Not touching original
                    // vectors.
                    coordTranslator.prepareVectors(k, initialPointer1, initialPointer2, pointer1, pointer2);
					if (k.pinch(coordTranslator.vector1, coordTranslator.vector2, coordTranslator.vector3,
                            coordTranslator.vector4)){
						pinch(k, coordTranslator.vector1, coordTranslator.vector2, coordTranslator.vector3,
                            coordTranslator.vector4);
						return true;
					}
				}
			}
		}
		return false;
	}

	public void pinchStop() {
		if (react){
			for (int a = control.size()-1; a >= 0; a--){ // pirma effect po to paint.
				// pradeda nuo paskutinio iki pirmo.
				Control e = control.get(a);
				if (e.isVisibleOnScreen())
					e.pinchStop();
			}
		}
	}

    private class CoordTranslator{
        private final Engine p = Engine.getInstance();
        private float absoluteX, absoluteY;
        private float fixedX, fixedY;

        private boolean isAbsolutePrepared = false, isFixedPrepared = false;

        // for pinch only.
        private final Vector2 vector1, vector2, vector3, vector4;

        private CoordTranslator(){
            vector1 = new Vector2();
            vector2 = new Vector2();
            vector3 = new Vector2();
            vector4 = new Vector2();
        }

        /** Gives absolute or fixed coords. Does not project multiple times. */
        private void prepareCoords(Control k, float screenX, float screenY){
            if (requireCordsTranslation){
                if (isPositionFixed(k)){
                    // fixed coords.
                    if (isFixedPrepared) // Coords were already prepared.
                        mouseCoords.set(fixedX, fixedY, 0);
                    else {
                        // Preparing coords.
                        p.screenToFixedCoords(mouseCoords.set(screenX, screenY, 0));
                        fixedX = mouseCoords.x;
                        fixedY = mouseCoords.y;
                        isFixedPrepared = true;
                    }
                }else {
                    // absolute coords.
                    if (isAbsolutePrepared) // Already set.
                        mouseCoords.set(absoluteX, absoluteY, 0);
                    else {
                        p.screenToWorldCoords(mouseCoords.set(screenX, screenY, 0));
                        absoluteX = mouseCoords.x;
                        absoluteY = mouseCoords.y;
                        isAbsolutePrepared = true;
                    }
                }
            }else { // giving same coords. No need to check. Faster approach just to set it.
                mouseCoords.set(screenX, screenY, 0);
            }
        }

        private void prepareVectors(Control k, Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2){
            if (requireCordsTranslation){
                if (isPositionFixed(k)){
                    // Here preparing fixed.
                    // Knowing that interfaces goes in order - from fixed down to absolute, so
                    // absolute will go last. It will never mix fixed and absolute order.
                    if (!isFixedPrepared){
                        // We only need to do something if it is not ready.
                        // If it was prepared, than it is already set.
                        p.screenToFixedCoords(mouseCoords.set(initialPointer1, 0));
                        setVector(vector1);
                        p.screenToFixedCoords(mouseCoords.set(initialPointer2, 0));
                        setVector(vector2);
                        p.screenToFixedCoords(mouseCoords.set(pointer1, 0));
                        setVector(vector3);
                        p.screenToFixedCoords(mouseCoords.set(pointer2, 0));
                        setVector(vector4);

                        isFixedPrepared = true;
                    }
                }else {
                    // Here preparing absolute.
                    if (!isAbsolutePrepared){
                        // Same as fixed. This must come after fixed coords was used. It will
                        // never go for fixed again in this loop.
                        p.screenToWorldCoords(mouseCoords.set(initialPointer1, 0));
                        setVector(vector1);
                        p.screenToWorldCoords(mouseCoords.set(initialPointer2, 0));
                        setVector(vector2);
                        p.screenToWorldCoords(mouseCoords.set(pointer1, 0));
                        setVector(vector3);
                        p.screenToWorldCoords(mouseCoords.set(pointer2, 0));
                        setVector(vector4);

                        isAbsolutePrepared = true;
                    }
                }
            }else { // Take same values.
                vector1.set(initialPointer1);
                vector2.set(initialPointer2);
                vector3.set(pointer1);
                vector4.set(pointer2);
            }
        }

        /** Setting given Vector by mouseCoords. Ignoring z value. */
        private void setVector(Vector2 keeper){
            keeper.set(mouseCoords.x, mouseCoords.y);
        }

        /** Postion of the given interface. Easier access to it's positioning. */
        private boolean isPositionFixed(Control k){
            return k.getPositioning() == Window.fixedView || (k.getPositioning() == Window.relativeView && positioning == Window.fixedView);
        }

        /** This must be called before any coord translation event. */
        private void reset(){
            isAbsolutePrepared = false;
            isFixedPrepared = false;
        }
    }

    // Only for test purposes to see where is input pressed.
//    private class TestPointClick implements MainDraw {
//        private final Counter counter;
//        float x, y;
//        private final Engine p = Engine.getInstance();
//
//        public TestPointClick(){
//            counter = new Counter();
//            counter.setCounterListiner(new Counter.CounterListener() {
//                @Override
//                public void finished(float currentValue) {
//                    drop();
//                }
//
//                @Override
//                public boolean cancel(int reason) {
//                    drop();
//                    return false;
//                }
//            });
//        }
//
//        public void pointDraw(float x, float y, boolean fixDraw){
//            this.x = x;
//            this.y = y;
//            counter.startCount(0, 1, 5);
//            TopPainter.addPaintOnTop(this, fixDraw);
//        }
//
//        @Override
//        public void draw() {
//            p.noStroke();
//            p.fill(0xAAFFA500);
//
//            p.ellipse(x, y, 20, 20);
//        }
//
//        @Override
//        public boolean drop(int reason) {
//            return false;
//        }
//
//        private void drop(){
//            TopPainter.removeTopPaint(this);
//        }
//    }
}
