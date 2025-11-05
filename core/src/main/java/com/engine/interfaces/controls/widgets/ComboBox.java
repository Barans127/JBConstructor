package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.engine.core.MoreUtils;
import com.engine.interfaces.controls.Balloon;
import com.engine.interfaces.controls.TextBlock;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.controls.Window;
import com.engine.root.GdxPongy;
import com.engine.root.TextContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComboBox extends TextBlock {
	private Drawable background;
	private final DropList listPop = new DropList();

	private boolean opened; // ar atidarytas sarasas.
	private ArrayList<String> list;
	private int index = -1, cursorOn = -1;
	private IndexChangeListener listener;
	private String defaultText;

	// max list items
	private int maxItems = 15;
	private int focusId;
	private boolean toSky;
	private int startItem;
	private float offset; // nes tik x te yra tai...
	private boolean isDragging;
//	private ScrollBar barBackground;
	private int oldPosition; // pasakys kur fokusuot.

	public ComboBox(){
		this(new ComboBoxStyle());
	}

	public ComboBox(ComboBoxStyle style){
		super(style);
		list = new ArrayList<>();
//		barBackground = new ScrollBar();
		background = style.background;
		defaultText = style.defaultWord;
		listPop.listBackground = style.listBackground;
		listPop.selectionColor = style.selectionColor;
		Color.argb8888ToColor(listPop.tint, style.listBackgroundColor);
		maxItems = style.maxShowableItem;
		toSky = style.upside;
		if (defaultText == null)
			defaultText = "null";
		setText(defaultText);
//		if (background == null || listPop.listBackground == null){
//			p.setError("ComboBox drawables cannot be null", ErrorType.WrongPara);
//		}
		listPop.setAnimatingTime(MoreUtils.abs(style.animationTime));
		listPop.listText.setTextSize(getTextSize());
		listPop.fullScreen = style.fullscreenListView;
		listPop.allowAnimation = style.enableAnimation;
		if (!style.enableAnimation){
			listPop.setAnimatingTime(0f);
		}
		focusId = -1;
		update = true;
	}
	
//	@Override
//	public void setController(InterfacesController v) {
//		super.setController(v);
//		barBackground.setController(v);
//	}

	public List<String> getItems(){
		return list;
	}

	public String getItem(int index){
		if (index >= list.size() || index < 0)
			return "";
		return list.get(index);
	}
	
	public void insert(String[] text, int index){
		if (index >= list.size() || index < 0)
			return;
		List<String> sar = Arrays.asList(text);
		list.addAll(index, sar);
		checkIndex();
		update = true;
	}
	
	public void insert(String text, int index){
		if (index >= list.size() || index < 0)
			return;
		list.add(index, text.trim());
		checkIndex();
		update = true;
	}

	/** clears combo box.  */
	public void clear(){
		list.clear();
		startItem = 0;
		checkIndex();
	}
	
	public boolean remove(int index){
		if (index >= list.size() || index < 0)
			return false;
		list.remove(index);
		checkIndex();
		return true;
	}
	
	public boolean remove(String item){
		boolean a = list.remove(item);
		checkIndex();
		return a;
	}
	
	public void append(String... items){
		for (String e : items){
			append(e);
		}
	}
	
	public void append(String item){
		if (item == null){
			item = "null";
		}
		list.add(item.trim());
		checkIndex();
		update = true;
	}
	
	public void setSelectedIndex(int index){
		if (list.size() <= index || index < 0){
			setText(defaultText);
		}else{
			focusId = index;
			oldPosition = 0;
			super.setText(list.get(index));
			this.index = index;
		}
		checkShowable(index);
	}

	public void setListBackgroundColor(int color){
		Color.argb8888ToColor(listPop.tint, color);
	}
	
	/**
	 * leidzia nustatyt ar dabartinis parinktas item yra matomas.
	 */
	private void checkShowable(int id){
		if (maxItems + startItem <= id){
			startItem += id - (maxItems + startItem) + 1;
			if (startItem + maxItems > list.size())
				startItem = list.size()-1;
			offset = 0;
		}else if (startItem > id){
			startItem -= startItem-id;
			if (startItem < 0)
				startItem = 0;
			offset = 0;
		}
	}
	
	@Override
	protected void sizeUpdated() {
		super.sizeUpdated();
		int size = list.size() > maxItems ? maxItems : list.size();
		if (size == 0)
			size = 3; // 3 bloku dydzio jei tuscias
		if (!listPop.fullScreen) {
			listPop.setSize(xSize, ySize * size);
//			listPop.listText.setTextSize(getTextController().getTextSize());
		}else {
			listPop.setSize(p.getScreenWidth()*0.8f, p.getScreenHeight()*0.8f);
//			listPop.listText.setTextSize();
		}
		listPop.listText.setTextColor(getTextController().getTextColor());
		listPop.oldAlpha = getTextController().getTextColor().a;
//		barBackground.setSize(maxItems*ySize, 20);
		setBarPlace();
	}

	public int getSelectedIndex(){
		return index;
	}
	
	public int getSize(){
		return list.size();
	}
	
	@Override
	public void setText(String text){
		super.setText(text);
		index = -1;
	}
	
	@Override
	protected void isvaizda(float nx, float ny){ // nepilnai veikianti rezoliucija... nu kaip ir ok.
		p.tint(statusColor);
		drawDrawable(background, nx, ny, true);
		p.noTint();
	}

	@Override
	public boolean mouseMoved(float x, float y){
		if (opened){
//			if (barBackground.isOpen() && barBackground.isEnabled())
//				if (barBackground.mouseMoved(x, y))
//					return true;
			cursorOn = listPop.mouseOnItem(x, y);
			if (cursorOn > -1 && focusId == -1) {
				oldPosition = cursorOn + 1;
				return true;
			}
		}
		return super.mouseMoved(x, y);
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		return super.tap(x, y, count, button) || listPop.tap(x, y, count, button);
	}

	@Override
	public boolean keyUp(int keycode) {
		if (super.keyUp(keycode))
			return true;
		if (opened){
//			if (barBackground.keyUp(keycode))
//				return true;
			if (keycode == Input.Keys.ESCAPE){
				release();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (super.keyDown(keycode))
			return true;
		if (opened){
			if (keycode == Input.Keys.DOWN){
				if (toSky)
					focus(-1);
				else
					focus(1);
				return true;
			}else if (keycode == Input.Keys.UP){
				if (toSky)
					focus(1);
				else
					focus(-1);
				return true;
			}else if (keycode == Input.Keys.ENTER && cursorOn >= 0){
				indexTriggered();
				return true;
			}
//			if (barBackground.keyDown(keycode))
//				return true;
		}else if (onEnterPress(keycode)){
			float x = Gdx.input.getX(), y = Gdx.input.getY();
			Vector3 n;
			if (getPositioning() == Window.absoluteView || v.getPositioning() == Window.absoluteView && getPositioning() == Window.relativeView){
				n = p.screenToWorldCoords(x, y);
			}else{
				n = p.screenToFixedCoords(x, y);
			}
			listPop.openList(n.x, n.y);
			if (focusId == -2)
				focusId = cursorOn;
			return true;
		}
		return false;
	}

	private void focus(int where){
		int id = focusId + oldPosition + where;
		if (id >= list.size()){
			id = 0;
		}else if (id < 0){
			id = list.size() - 1;
		}
		checkShowable(id);
		focusId = id;
		cursorOn = focusId;
		oldPosition = 0;
	}

	@Override
	protected void autoSize(){
		BitmapFont f = getTextController().getFontScaled(); // fontas pagal reikiama dydi.
		int ilgisX = (int) p.textWidth(getText(), f);
		if (!listPop.fullScreen) {
			for (int a = 0; a < list.size(); a++) {
				int il = (int) p.textWidth(list.get(a), f);
				if (ilgisX < il) {
					ilgisX = il;
				}
			}
		}
		xSize = ilgisX + getTextSize();
		ySize = p.textHeight()*1.2f; // cia ok
//		sizeUpdated();
//		listPop.setSize(nxSize, nySize*maxItems);
//		listPop.listText.setTextSize(getTextSize() * r);
//		setBarPlace();
//		barBackground.setSize(maxItems*ySize, 20);
//		sizeUpdated();
	}

	private void selectedIndexChanged(int old, int current){
		if (listener != null){
			listener.selectedIndexChanged(old, current);
		}
	}

	public void setIndexChangeListener(IndexChangeListener e){
		listener = e;
	}

	@Override
	public String toString(){
		if (index < 0 || index >= list.size()){
			return "";
		}else{
			return list.get(index);
		}
	}

	private void checkIndex(){
		if (list.size() <= maxItems){
			startItem = 0;
//			barBackground.setVisible(false);
		}
//		else{
//			barBackground.setVisible(true);
//		}
		focusId = -1;
		if (index < 0) return;
		if (index >= list.size()){
			setText(defaultText);
		}else{
//			int old = index;
			super.setText(list.get(index));
//			index = old;
		}
	}

	/**
	 * all list will go on top of comboBox.
	 */
	public void setListOnTop(boolean top){
		toSky = top;
		setBarPlace();
	}

	/**
	 * nustatys kiek itemu galima rodyt esanciam drop liste.
	 */
	public void setMaxShowableItems(int max){
		if (max <= 0){
			return;
		}
		maxItems = max;
//		barBackground.setSize(maxItems*getHeight(), 20);
		setBarPlace();
	}

	private void setBarPlace(){
		Vector2 position = getPosition();
		float x, y;
		if (!listPop.fullScreen) {
			if (toSky) {
//			y = position.y + getHeight()*maxItems+1;
				y = position.y + ySize;
			} else {
				y = position.y - listPop.getHeight();
			}
			x = position.x;
		}else {
			float wx = listPop.getWidth() / 2;
			float wy = listPop.getHeight() / 2;
			x = p.getScreenWidth()/2 - wx;
			y = p.getScreenHeight()/2 - wy;
		}
		float offsetX = v == null ? 0 : v.getOffsetX();
		float offsetY = v == null ? 0 : v.getOffsetY();
		listPop.setPosition(x + offsetX, y + offsetY);
//		barBackground.setPosition(position.x + getWidth() + listPop.getOffsetX(), y + listPop.getOffsetY());
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		return super.pan(x, y, deltaX, deltaY) || listPop.pan(x,y,deltaX, deltaY);
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		return listPop.panStop(x, y, pointer, button) || super.panStop(x, y, pointer, button);
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		return opened || super.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
	}

	@Override
	public void release(){
		super.release();
		listPop.release();
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		return listPop.touchDown(x, y, pointer, button) || super.touchDown(x, y, pointer, button);
	}

//	@Override
//	public boolean onLongClick(float x, float y) {
//		return false;
//	}

	@Override
	public void onPress(float x, float y) {
		super.onPress(x, y);
		listPop.openList(x, y);
	}

//	@Override
//	public void onRelease(float x, float y) {
//
//	}

//	@Override
//	public void onClick() {
//
//	}

	public interface IndexChangeListener {
		public void selectedIndexChanged(int old, int current);
	}

	private void indexTriggered(){
		if (index != cursorOn){
			int old = index;
			index = cursorOn;
//			super.setText(list.get(index));
			getTextController().setText(list.get(index));
			cursorOn = -1;
			selectedIndexChanged(old, index);
		}
		release();
	}

	protected void drawItem(TextContainer text, float x, float y, float width, float height){
		p.stroke(0);
		p.strokeWeight(1f);
		p.line(x, y, x + width, y);
		text.drawText(x, y);
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		setBarPlace();
	}

	@Override
	protected void giveCords(float x, float y) {
		super.giveCords(x, y);
		setBarPlace();
	}

	private class DropList extends Balloon{
		private Drawable listBackground;
		private TextContainer listText;
		private int selectionColor;
		private float oldAlpha;

		private float itemHeight;
		private boolean fullScreen, allowAnimation;

		private float oldMY;
		private boolean listIsOpening;

		private Color tint;

		public DropList(){
			tint = new Color(1, 1, 1, 1);
			listText = new TextContainer();
			listText.setTextAlign(Align.center, Align.center);
			useAnimation(false);
		}

		public boolean panStop(float x, float y, int pointer, int button) {
			if (opened) {
//				if (barBackground.panStop(x, y, pointer, button))
//					return true;
				if (isDragging) {
					isDragging = false;
					offset = 0;
					return true;
				}
			}
			return false;
		}

		public void release(){
			if (isDragging){
				isDragging = false;
//				barBackground.release();
				offset = 0;
				if (cursorOn < startItem || cursorOn > startItem + maxItems)
					cursorOn = -1;
			}
			listPop.hide();
			if (!listIsOpening) {
				if (opened) {
					v.getForm().removeTopItem(this);
				}
				TopPainter.removeInputsListener(ComboBox.this);
				opened = false;
			}
		}

		public boolean tap(float x, float y, int count, int button) {
			if (opened) {
				if (isDragging) {
					isDragging = false;
					offset = 0;
//					barBackground.tap(x, y, count, button);
					if (cursorOn < startItem || cursorOn > startItem + maxItems)
						cursorOn = -1;
					return true;
				}
//				if (barBackground.tap(x, y, count, button)) {
//					return true;
//				}
				if (ComboBox.this.button == button && cursorOn >= 0) {
					if (focusId > -2){ // jei -2, tai turi ignoruot.
						indexTriggered();
					}else {
						focusId = cursorOn;
					}
					return true;
				}
			}
			return false;
		}

		public boolean touchDown(float x, float y, int pointer, int button) {
			if (opened){
//				if (barBackground.touchDown(x, y, pointer, button)) {
//					return true;
//				}
				cursorOn = mouseOnItem(x, y);
				if (cursorOn == -1 || focusId != -1) {
					normalStatus();
					release();
				}
				return true;
			}
			return false;
		}

		public boolean pan(float x, float y, float deltaX, float deltaY) {
			if (opened){
//				if (barBackground.pan(x, y, deltaX, deltaY)){
//					return true;
//				}
				y += listPop.getOffsetY();
				if (!isDragging){
					isDragging = true;
					oldMY = y - offset;
				}
				if (maxItems >= list.size()){
					return true;
				}
				offset = y-oldMY;
				/* // atkomentuot, kad neleistu iseit is ribu..
					if (startItem == 0){
						if (toSky){
							if (offset < 0){
								offset = 0;
								return true;
							}
						}else{
							if (offset > 0){
								offset = 0;
								return true;
							}
						}
					}else if (startItem + maxItems >= list.size()){
						if (toSky){
							if (offset > 0){
								offset = 0;
								return true;
							}
						}else{
							if (offset < 0){
								offset = 0;
								return true;
							}
						}
					}
				 */
				if (offset > 0){
					if (offset > itemHeight*0.6f){
						if (!toSky){
							if (startItem+maxItems < list.size())
								startItem++;
							else
								return true;
						}else{
							if (startItem > 0)
								startItem--;
							else
								return true;
						}
						offset -= itemHeight;
						oldMY += itemHeight;
					}
				}else if (offset < 0){
					if (offset*-1 > itemHeight*0.6f){
						if (!toSky){
							if (startItem > 0)
								startItem--;
							else
								return true;
						}else if (startItem+maxItems < list.size()){
							startItem++;
						}else
							return true;
						offset += itemHeight;
						oldMY -= itemHeight;
					}
				}
				return true;
			}
			return false;
		}

		private int mouseOnItem(float mx, float my){ // ant atidarytu daiktu
			Vector2 position = getPosition();
			my = my - offset;
			float xSize = getWidth();
//			float ySize = listText.getFontScaled().getLineHeight();
			float ySize = itemHeight;
			int max = (list.size() > maxItems ? maxItems : list.size()) - 1;
			for (int a = 0; a < list.size() && a < maxItems; a++){
				float Y;
				if (toSky){
//					Y = position.y - ySize * (a + 1);
					Y = position.y + ySize * (a);
				}else{
					Y = position.y + ySize * (max - a);
				}
				if (mx > position.x && mx < position.x + xSize && my >= Y && my <= Y + ySize){
					focusId = -1;
					return a + startItem;
				}
				// nu ce da reiks biski :D
			}
			return focusId; // jei ne ant itemo
		}

		private void openList(float x, float y){
			if (opened){
				release();
			}else {
				listIsOpening = true;
				v.getForm().addOnTop(this);
				opened = true;
				focusId = index; // iskart focus ant parinkto item.
				if (focusId < startItem && focusId > -1){
					startItem = focusId;
				}else if (startItem + maxItems < focusId){
					startItem = focusId-maxItems+1;
				}
				cursorOn = mouseOnItem(x, y);
				focusId = -2; // kad po touch release zinotu, kad nereik keist indexo.
//				Vector2 pos = ComboBox.this.getPosition();
//				setPosition(pos.x + v.getOffsetX(), pos.y + v.getOffsetY()); // kad suprastu kur nuscrollintas yra.
				setBarPlace();
				show();
				TopPainter.addInputsListener(ComboBox.this);
				listIsOpening = false;
			}
			offset = 0;
			oldPosition = 0;
		}

		@Override
		public void setSize(float width, float height) {
			super.setSize(width, height);
			float nySize;
			if (fullScreen){
				float length = GdxPongy.getInstance().getScreenHeight() * 0.8f;
				nySize = length / maxItems;
				listText.setTextSize(nySize*0.8f);
			}else {
				nySize = ComboBox.this.ySize;
				listText.setTextSize(ComboBox.this.getTextController().getTextSize());
			}
			itemHeight = nySize;
			listText.setBounds(width, nySize);
//			listText.setTextSize();
		}

		@Override
		protected void showing(float progress) {
			super.showing(progress);
			if (allowAnimation) {
				Color e = listText.getTextColor();
				e.a = progress * oldAlpha;
				listText.setTextColor(e);
				this.progress = progress;
			}
		}

		@Override
		protected void hiding(float progress) {
			super.hiding(progress);
			if (allowAnimation) {
				Color e = listText.getTextColor();
				e.a = progress * oldAlpha;
				listText.setTextColor(e);
				this.progress = progress;
			}
		}

		private float progress = 1f; // atsiradimo animacijai.

		@Override
		protected void draw(float x, float y, float width, float height, float offsetX, float offsetY) {
//			if (barBackground.isOpen())
//				barBackground.handle();
			int sk = list.size() > maxItems ? maxItems : list.size();
				// tuscias comboboxas
				// bus 3 bloku dydzio
			float dif = 0;
			if (!toSky)
				dif = height - height*progress;
			if (p.pushScissor(x, y + dif, width, height*progress)) {
				p.tint(tint);
				listBackground.draw(p.getBatch(), x, y, width, height);
				p.noTint();
				if (sk > 0) {
					int realSK = sk;
					int start = 0;
					if (offset != 0) {
						sk++;
						if (startItem > 0) {
							start = -1;
						}
					}
					for (int a = start; a < sk && a + startItem < list.size(); a++) {
						float Y;
						if (toSky) {
//							Y = ny - nySize * (a + 1) - 1 + (offset * r) + offsetY;
							Y = y + itemHeight * a + offset;
						} else {
//							Y = ny + nySize * (a + 1) + 1 + (offset * r) + offsetY;
//							Y = y + nySize - nySize * (a) + 1 + (offset * r) - offsetY;
//							Y = position.y + ySize * (max - a);
//							Y = y - nySize * (a+1) + offset - offsetY;
//							a -= start;
							Y = y + itemHeight * (realSK - a - 1) + offset;
						}
						if (cursorOn == a + startItem) {
							p.fill(selectionColor, (int) (tint.a * 255));
							p.noStroke();
							p.rect(x, Y, width, itemHeight);
						}
						listText.setText(list.get(a + startItem));
						drawItem(listText, x ,Y, width, height);
//						p.stroke(0);
//						p.strokeWeight(1f);
//						p.line(x, Y, x + width, Y);
//						listText.setText(list.get(a + startItem));
//						listText.drawText(x, Y);
					}
				}
				p.popScissor();
			}
		}
	}

	/* style */

	@Override
	public ComboBoxStyle getStyle(){
		ComboBoxStyle e = new ComboBoxStyle();
		copyStyle(e);
		return e;
	}

	public void copyStyle(ComboBoxStyle st){
		super.copyStyle(st);
		st.background = background;
		st.listBackground = listPop.listBackground;
		st.maxShowableItem = maxItems;
		st.defaultWord = defaultText;
		st.listBackgroundColor = Color.argb8888(listPop.tint);
		st.upside = toSky;
		st.selectionColor = listPop.selectionColor;
		st.fullscreenListView = listPop.fullScreen;
		st.enableAnimation = listPop.allowAnimation;
		st.animationTime = listPop.getAnimatingTime();
	}

	public void readStyle(ComboBoxStyle st){
		super.readStyle(st);
		background = st.background;
		listPop.listBackground = st.listBackground;
		setMaxShowableItems(st.maxShowableItem);
		defaultText = st.defaultWord;
		setListBackgroundColor(st.listBackgroundColor);
		setListOnTop(st.upside);
		listPop.selectionColor = st.selectionColor;
		listPop.fullScreen = st.fullscreenListView;
		listPop.allowAnimation = st.enableAnimation;
		listPop.setAnimatingTime(st.animationTime);
	}

	public static class ComboBoxStyle extends TextStyle{
		public Drawable background;
		public Drawable listBackground;
		public int maxShowableItem = (Gdx.app.getType() == Application.ApplicationType.Android ||
				Application.ApplicationType.iOS == Gdx.app.getType()) ? 5 : 15;
		public String defaultWord = "-Select-";
		public int listBackgroundColor = 0xFFFFFFFF;
		/** true: list goes from bottom to top, false: list goes from top to bottom. */
		public boolean upside = false; // ar į viršų.

		public int selectionColor = GdxPongy.color(120, 170, 255);
		/* listo */
		public boolean fullscreenListView = Gdx.app.getType() == Application.ApplicationType.Android ||
				Application.ApplicationType.iOS == Gdx.app.getType();
		/** List will show up with animation. */
		public boolean enableAnimation = true;
		/** time in seconds. */
		public float animationTime = 0.5f;


		/* perskirt juosta itemus. */
		// TODO ivelt i koda :D dabar hard coded.
		public boolean useIterator = true;
		public int iteratorColor = 0xFF000000;
		public float iteratorWidth = 1f;

		@Override
		public ComboBox createInterface() {
			return new ComboBox(this);
		}
	}
}
