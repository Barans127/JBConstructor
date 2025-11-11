package com.engine.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.engine.animations.Counter;
import com.engine.ui.controls.ControlHost;
import com.engine.ui.controls.Window;
import com.engine.ui.controls.widgets.Label;

/* pervadinta is Loader i IntroduceForm. Nes realiai tokio loader nereikia...
 * Atsisakyta visu loader galimybiu, nes jos vistiek nelabai naudingos.
 * Sita forma rodys tik viena karta pries pradzia, pries startup listeneerio iskvietima. */
public class IntroduceForm implements Window {
	private final ControlHost v; // label neveiks be kontrolerio
	private final Engine p; // pats varikliukas
	private static StartListener start; // listener, per kuri vartotojas turetu perjunkt i kita forma, pats kalts, jei neperjunks
//	private int timer; // dar neaisku kam panaudot, gal jei neperjunge vartotojs i kita forma, pranest jam?

	private Texture texture;
	private float x, y;

	private Counter counter;

	IntroduceForm(Engine p){
		this.p = p;
		v = new ControlHost();
		String path = Resources.getProperty("startingLogo", "null");
		float width = 0, height = 0;
		if (!path.equals("null")){
			FileHandle file = Gdx.files.internal(path);
			if (file.exists()){
				try {
					texture = new Texture(file);
					width = Math.min(texture.getWidth(), p.getPreferredScreenWidth());
					height = Math.min(texture.getHeight(), p.getPreferredScreenHeight());
				}catch (GdxRuntimeException ignored){}
			}
		}
		if (p.getFont() != null) {// negalim kurt be fonto.
			Label.LabelStyle lst = new Label.LabelStyle();
			lst.positioning = Position.fixed;
			lst.textColor = 0xFFFFFFFF;
			lst.textSize = 28;
			lst.horizontalAlign = Align.center;
			lst.verticalAlign = Align.center;
			lst.text = "127 productions";
			// pradzios uzrasas.
			Label support = new Label(lst);
			support.auto();
			float supX = p.getPreferredScreenWidth() / 2;
			float supY = p.getPreferredScreenHeight() / 2 - 50;
//			support.placeInMiddle(p.getScreenWidth() / 2, p.getScreenHeight() / 2 - 50); // 50, nes introduce toks aukstis.

			Label introduce = new Label("Mr Salto games");
			introduce.setTextSize(67);
			introduce.setTextAlign(Align.center, Align.center);
			introduce.setSize(600, 70);
//			introduce.placeInMiddle(p.getScreenWidth() / 2, p.getScreenHeight() / 2);
			float inX = p.getPreferredScreenWidth()/2;
			float inY = p.getPreferredScreenHeight()/2;
			introduce.setPositioning(Position.fixed);
			introduce.setTextColor(255);

			if (texture == null){
				// ner tekstures. imam paprastai.
				support.placeInMiddle(supX, supY);
				introduce.placeInMiddle(inX, inY);
				v.addControl(support);
				v.addControl(introduce);
			}else {
				float pWidth = p.getPreferredScreenWidth();
				float pHeight = p.getPreferredScreenHeight();
				float needHeight = introduce.getHeight() + support.getHeight() + 15; // zodz. 70 introduce, 30 support. 15 tarpas tarp support ir introduce.
				// 10 tarpas...
				if (height + needHeight + 10 <= pHeight){
					// telpa.
					float rHeight = needHeight + height + 10;
					float top = pHeight/2 + rHeight/2;
					y = top - height; // image start pozicija.
					introduce.placeInMiddle(inX, y - 10 - introduce.getHeight()/2);
					support.placeInMiddle(supX, introduce.getPosition().y - 50);

					v.addControl(support);
					v.addControl(introduce);
				}else if (height + introduce.getHeight() + 10 <= pHeight){
					// telpa tik main. uzrasas.
					float rHeight = height + introduce.getHeight() + 10;
					float top = pHeight/2 + rHeight/2;
					y = top - height; // image start pozicija.
					introduce.placeInMiddle(inX, y - 10 - introduce.getHeight()/2);

					v.addControl(introduce);
				}else {
					// netelpa uzrasai. Nededam anu.
					y = pHeight/2 - height/2;
				}
				x = pWidth/2 - width/2;
			}
		}else {
			float pWidth = p.getPreferredScreenWidth();
			float pHeight = p.getPreferredScreenHeight();

			x = pWidth/2 - width/2;
			y = pHeight/2 - height/2;
		}

		counter = new Counter();
		counter.setCounterListiner(new Counter.CounterListener() {
			@Override
			public void finished(float currentValue) {
				if (start != null) {
					start.startup();
					start = null;
				}
				counter = null;
			}

			@Override
			public boolean cancel(int reason) {
				return true;
			}
		});
		counter.setValues(0,1, 4);
	}

	/** This listener will be called after all initialization is completed and introduce form have shown its welcome message */
	public static void setStartListener(StartListener e){
		start = e;
	}

	@Override
	public void handle(){
		p.background(0);
		v.handle();
	}

	@Override
	public void fixHandle() {
		v.fixHandle();

		if (texture != null){
			float alpha;
			if (counter != null) {
				alpha = Math.min(1f, counter.getCurrentValue() * 4);
			}else {
				alpha = 1f;
			}
			float width = Math.min(texture.getWidth(), p.getPreferredScreenWidth());
			float height = Math.min(texture.getHeight(), p.getPreferredScreenHeight());
			p.tintf(1f, 1f, 1f, alpha);
			p.getBatch().draw(texture, x, y, width, height);
			p.noTint();
		}
//		if (!introduce.isAnimating()) {
//			if (start != null) { // start listenerio iskvietimas
//				start.startup();
//				start = null;
//				timer = p.millis(); // po listener skaiciuosim kiek laiko praejo
//			} else if (timer > 0 && timer + 2000 < p.millis()) {
//				// kažkur nukelt ane???? Ka daryt jei formos nepakeite vartotojs?
//
//			}
//		}
	}

	/* Inputs, nereikalingi loaderiui. Bent jau kol kas */

	@Override
	public void show() {
//		if (firstTime){
//		support.appear(2f);
//		introduce.appear(2f);
//		}
		counter.startCount();
	}

	@Override
	public void hide() {
		if (texture != null){
			Resources.addDisposable(texture);
			texture = null;
		}
	}

	@Override
	public void release() {
		v.release();
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char e) {
		return false;
	}

	@Override
	public boolean mouseMoved(float x, float y) {
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
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
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
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

	/** Does nothing. */
	@Override
	public void setFormId(String id) {

	}

	/** @return hard coded name for this name. Name: IntroduceForm */
	@Override
	public String getFormId() {
		return "IntroduceForm";
	}
}
