package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.TextBlock;
import com.engine.root.GdxPongy;

import static com.badlogic.gdx.Gdx.input;

public class TextBox extends TextBlock {
	/* basic */
	private int maxLength;
	private TextChangedListener list;
//	private String realText;
	private StringBuilder realText;
	private boolean blink;
	private int blinkCounter, blinkSpeed;

	/* text control */
	private int start, end; // rodomas tekstas
	private int cursorPos, offsetTextCount;
	private float cursorX, cursorY;
	private boolean lockText;

	/* multiline */
	private boolean multiline;
	private int linesCount;
//	private int currentLine = 0;


	/* maske */
	private final Color tintBackground;
	private Drawable background;
	private boolean hasMask;
	private char mask;

	/* border */
	private boolean hasBorder;
	private float borderWidth;

	public TextBox(TextBoxStyle style){
		super(style, false);
		realText = new StringBuilder();
		tintBackground = new Color();
		background = style.background;
//		if (background == null)
//			p.setError("Background cannot be null", ErrorType.WrongPara);
		hasMask = style.hasMask;
		mask = style.mask;
		maxLength = style.maxLength;
		hasBorder = style.hasBorders;
		borderWidth = style.borderWidth;
		blinkSpeed = style.blinkSpeed;
		lockText = style.lockText;
		Color.argb8888ToColor(tintBackground, style.tintBackground);
		enableShapeTransform(true);
		update = true;
		setText(style.text);
	}

	public TextBox(){
		this(new TextBoxStyle());
	}

	@Override
	protected void autoSize() {
		if (xSize <= 0 || ySize <= 0){ // apytiksliai dydis bus vienos eilutes ir apie 50?? raidziu ploto.
//			xSize = getTextController().getFontScaled().getSpaceWidth() * 50;
			xSize = getTextController().getFontScaled().getSpaceXadvance() * 50;
//			getTextController().getFont().getData().getGlyph('m').
			ySize = getTextSize();
		}
//		sizeUpdated();
	} // nope..

	@Override
	protected void sizeUpdated() {
		super.sizeUpdated();
		updateCursorPos();
	}

	@Override
	protected void isvaizda(float x, float y) {
		transformMatrix(x, y);
	}

	@Override
	protected void drawTransformed(float x, float y) {
		if (hasBorder){
			p.tint(tintBackground);
			background.draw(p.getBatch(), x, y, xSize, ySize);
			p.stroke(statusColor);
			p.noFill();
			p.strokeWeight(borderWidth);
			p.rect(x, y, xSize, ySize);
			p.noTint();
		}else{
			p.tint(statusColor);
			background.draw(p.getBatch(), x, y, xSize, ySize);
			p.noTint();
		}
		Vector2 pos = getPosition();
		drawText(pos.x, pos.y, 0); // BUG FIX kai ant scroll view vaizdas nesikeisdavo, nes sitam metode dar karta prideda scrollinimo offseta.
		if (isFocused()){
			if (blink) {
//				GlyphLayout l = getTextController().getLayout();
//				float cy = (l.runs.size > 0 ? l.runs.get(currentLine).y : 0)
//						+ y + nySize;
				BitmapFont f = getTextController().getFontScaled();
				float size = f.getCapHeight() - f.getDescent();
				p.strokeWeight(2f);
				p.stroke(0);
				float offsetX = v.getOffsetX() + getTextOffsetX(), offsetY = v.getOffsetY()+ getTextOffsetY();
				p.line(cursorX + offsetX, cursorY + offsetY, cursorX + offsetX, cursorY + offsetY - size);
			}
			if (blinkCounter + blinkSpeed < p.millis()){
				blink = !blink;
				blinkCounter = p.millis();
            }
			if (!Gdx.graphics.isContinuousRendering()){
				Gdx.graphics.requestRendering();
			}
		}
	}

	@Override
	public void onFocus() {
		super.onFocus();
		blink = true;
		blinkCounter = p.millis() + 300; // kad ilgiau uzsilaikytu tik paspaudus.
	}

	@Override
	public boolean keyUp(int keycode) {
		boolean e = super.keyUp(keycode);
		return isFocused() || e; // kai focused, tai visus input is kliavaturos paims ir visks.
	}

	@Override
	public boolean keyDown(int keycode) {
		if (super.keyDown(keycode))
			return true;
		if (isFocused()) {
			if (Input.Keys.LEFT == keycode) {
				if (cursorPos > 0) {
					cursorPos--;
					updateCursorPos();
					blink = true;
					blinkCounter = p.millis();
				}
//				return true;
			} else if (Input.Keys.RIGHT == keycode) {
				if (cursorPos < realText.length()) {
					cursorPos++;
					updateCursorPos();
					blink = true;
					blinkCounter = p.millis();
				}
//				return true;
			}else if (Input.Keys.ESCAPE == keycode){
				if (v != null) {
					v.removeFocus();
				}
			}
			return true; // su focusu viska paimt.
//			else if (Input.Keys.UP == keycode){
//				if (currentLine > 0){
//
//				}
//			}
		}
		return false;
	}

	@Override
	public boolean keyTyped(char e){
		if (super.keyTyped(e))
			return true;
		if (isFocused()){
			if (lockText)
				return true;
//			String old, current;
//			old = realText.toString();
//			if (hasMask){
//				old = realText;
//			}else {
//				old = getText();
//			}
//			current = old;
			if (e == 0 || e == 27 || e == 9){ // 9 - tab, gal palikt?, 27 - ESC, 127 - DEL
				return true;
			}else if (input.isKeyPressed(Input.Keys.CONTROL_LEFT) || input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
				return true;
			}else if (e == 127){ // del
				changeText(e, 3);
				return true;
			}else if (e == 8){ // backspace
				if (realText.length() <= 0){
					return true;
				}
//				current = current.substring(0, current.length()-1);
				changeText(e, 0);
				return true;
			}else if (e == 13){ // enter
				if (multiline){
					if (realText.length() < maxLength)
						changeText(e, 1);
				}else{
//					loseFocus(true); // iskvies lost focus listeneri.
					onEnterPress(Input.Keys.ENTER);
				}
				return true;
			}else // paprastas raides pridejimas
				if (realText.length() < maxLength){
//					current += e;
					changeText(e, 2);
					return true;
				}
		}
		return false;
	}

	/** action 0: delete, 1: enter/nauja eilute, 2:paprastas pridejimas, 3: del is desines*/
	private void changeText(char e, int action){
		String old = realText.toString();
		int pos = cursorPos;
		switch (action){
			case 0:
				if (pos == 0)
					return;
//				if (pos == realText.length()){
//					realText.length--;
//				}else
					realText.deleteCharAt(pos-1);
				pos--;
				break;
			case 1:
				realText.insert(pos, '\n');
				pos++; // TODO bugina.
				break;
			case 2:
				realText.insert(pos, e);
				pos++;
				break;
			case 3:
				if (pos == realText.length){
					return;
				}
				realText.deleteCharAt(pos);
				break;
		}
		if (list != null){
			String name = realText.toString();
			if (list.textChanged(old, name, e, this)){
				realText.deleteCharAt(cursorPos);
				return;
			}
		}
//		BitmapFont font = getTextController().getFontScaled();
//		BitmapFont.Glyph gl = font.getData().getGlyph(e);
//		float size = (gl.xoffset + gl.width) * font.getScaleX();

		/* blink efektas, kad nemirksertu kai vedi teksta. */
		blink = true;
		blinkCounter = p.millis();
//		if (currentLength + size > nxSize){ // nebetilpo
//
//		}
		if (multiline) {
			super.setText(realText.toString());
		}else {
			float textWidth = getTextWidth();
			float capWid = 0;
			if (e == 127 || e == 8){ // istrinimas.

			}else { // pridejimas
//				if (textWidth < xSize) {
//					BitmapFont.Glyph let = getTextController().getFont().getData().getGlyph(e);
//					capWid = (let.width + let.xoffset) * getTextController().getFontScaled().getScaleX();
//					if (textWidth+capWid > xSize){
//
//					}
//				}else { // tekstas ir taip netelpa...
//
//				}
			}
//			super.setText(realText.substring(offsetTextCount, linesCount)); // tuscia. substring nuo 0 iki 0, reisk nieko.
			super.setText(realText.toString()); // kol nebaigta daryt.
		}
		getTextController().updateBounds(); // nes neupdatina..
		cursorPos = pos;
		updateCursorPos();
	}

	private void updateCursorPos() {
		GlyphLayout e = getTextController().getLayout();
		if (realText.length == 0) { // ner teksto
			alignHorizontalCursor(0);
//			if (e.runs.size == 0) {
			cursorPos = 0;
//			}
			alignVerticalCursor(0);
//			cursorX = nx;
//			getFocus();
			return;
		}else { // kartais buva kad nespeja update bounds padaryt ir nesutampa tekstai.
			if (!getTextController().isBoundsUpdated())
				getTextController().updateBounds();
		}

//		int cursor = cursorPos;
		int prevGlypCount = 0;
		int usedGlyph = 0;
		float lineLength = getTextController().getFontScaled().getLineHeight();
		if (realText.length > 0) { // jeigu prieki tuscios eilutes
			enterLoop:
			while (realText.charAt(prevGlypCount) == '\n') {
				prevGlypCount++;
				if (cursorPos == prevGlypCount){ // sitoj eilutej det cursoriu.
					float y = -lineLength * prevGlypCount; // dabartine eilute
					for (GlyphLayout.GlyphRun run : e.runs){
						if (y <= run.y && y >= run.y - lineLength){ // eilute turi teksta...  judet tolyn.
							break enterLoop;
						}
					}
					alignHorizontalCursor(0);
					alignVerticalCursor(-lineLength * prevGlypCount);
//					currentLine = prevGlypCount;
					return;
				}else if (cursorPos == 0){ // cursor pradzioj teksto, nieko ypatingo nereik.
					alignHorizontalCursor(0);
					alignVerticalCursor(0);
//					currentLine = 0;
					return;
				}
			}
		}

		for (int lineIndex = 0; lineIndex < e.runs.size; lineIndex++) { // eilutes nustatymas. tuscia ar su tekstu
			GlyphLayout.GlyphRun nLine = e.runs.get(lineIndex);
			if (cursorPos > nLine.glyphs.size + prevGlypCount) { // jeigu cursor toliau, ne ant sitos eilutes
				prevGlypCount += e.runs.get(lineIndex).glyphs.size;
				int additional = 0;
				while (prevGlypCount < realText.length && realText.charAt(prevGlypCount) == '\n') { // jei gale enter.
					prevGlypCount++; // pridet viena, nes enter zenklo nezymi.
					if (prevGlypCount == cursorPos && (lineIndex == e.runs.size-1
						|| prevGlypCount < realText.length && realText.charAt(prevGlypCount) == '\n')){ // už šitą entere. tuscia eilute.
						int realLine = 0;
						float y = 0;
						while (y >= nLine.y){
							realLine++;
							y -= lineLength;
						}
						alignHorizontalCursor(0);
						alignVerticalCursor((realLine + additional) * -lineLength);
//						currentLine = lineIndex;
						return;
					}
					additional++;
				}
			} else { // eilute turi teksta
				usedGlyph = lineIndex;
				break;
			}
		}

		GlyphLayout.GlyphRun line = e.runs.get(usedGlyph); // eilute su tekstu.
		float length = getPosition().x + line.x + line.xAdvances.get(0);
		if (line.glyphs.size > 0) { // netuscia eilute, cursoriaus x pozicija.
			for (int a = 0; a < line.glyphs.size && a < cursorPos - prevGlypCount; a++) {
				length += line.xAdvances.get(a + 1);
			}
		}
//		currentLine = usedGlyph;
		cursorX = length;
		alignVerticalCursor(line.y);
	}

	private void alignHorizontalCursor(float offset){
//		offset = getTextOffsetX();
		offset -= getTextOffsetX();
		Vector2 pos = getPosition();
		switch (getHorizontalAlign()){
			case Align.left:
				cursorX = pos.x + offset;
				break;
			case Align.center:
				cursorX = pos.x + xSize/2 + offset;
				break;
			case Align.right:
				cursorX = pos.x + xSize + offset;
				break;
		}
	}

	private void alignVerticalCursor(float offset){
//		offset = getTextOffsetY();
		offset -= getTextOffsetY();
		//				float cy = (l.runs.size > 0 ? l.runs.get(currentLine).y : 0)
//						+ y + nySize;
		Vector2 pos = getPosition();
		switch (getVerticalAlign()){
			case Align.bottom:
				cursorY = pos.y + getTextController().getFontScaled().getLineHeight() + offset;
				break;
			case Align.center:
				cursorY = pos.y + ySize/2 + getTextController().getFontScaled().getLineHeight()/2 + offset;
//				cursorY = pos.y + ySize/2 + offset;
				break;
			case Align.top:
				cursorY = pos.y + ySize + offset;
				break;
		}
	}

	@Override
	public String getText() {
//		if (hasMask){
//			return realText;
//		}else {
//			return super.getText();
//		}
		return realText.toString();
	}

	@Override
	public void setText(String text) {
		if (text == null)
			text = "null";
		if (text.length() > maxLength){
			text = text.substring(0, maxLength);
		}
		if (!multiline && text.contains("\n")){
			text = text.split("\n")[0]; // o gal geriau visus enter pasalint?
		}
//		realText.clear();
		realText.length = 0;
//		realText.clear();
//		realText.append(text);
		if (hasMask){
//			realText = text;
//			StringBuilder mask = new StringBuilder(text.length());
			for (int a = 0; a < text.length(); a++){
				realText.append(this.mask);
			}
			super.setText(realText.toString());
			realText.length = 0;
			realText.append(text);
		}else
			realText.append(text);
			super.setText(text);
	}

//	@Override
//	public boolean onLongClick(float x, float y) {
//		return false;
//	}

	@Override
	protected void onPress(float x, float y) {
		super.onPress(x, y);

		if (Gdx.app.getType() == Application.ApplicationType.Android || Gdx.app.getType() == Application.ApplicationType.iOS){
			input.setOnscreenKeyboardVisible(true);
		}

		// kad nemirksetu aspaudus.
		blink = true;
		blinkCounter = p.millis() + 300; // kad ilgiau uzsilaikytu tik paspaudus.
		// apejimas. nes offset nemato.
		x -= getTextOffsetX();
		y -= getTextOffsetY();
		GlyphLayout e = getTextController().getLayout();
		if (realText.length == 0){ // ner teksto
			cursorPos = 0;
			alignHorizontalCursor(0);
			alignVerticalCursor(0);
//			getFocus();
			return;
		}
		Vector2 pos = getPosition();
		// jeigu enter yra pradziai.
		int prevGlyps = 0;
//		if (realText.length > 0){
//			while (realText.charAt(prevGlyps) == '\n'){
//				prevGlyps++;
//			}
//		}

		// sugaudom kurioj eilutej buvo paspausta.
		float lineSize = getTextController().getFontScaled().getLineHeight();
//		int lineCount =  MathUtils.round(nySize / lineSize); // kiek telpa eiluciu duotam boxe.
		float startY = pos.y + ySize;
		float lineMove = 0;
		int glyphIndex = 0;
		for (int a = 0, usedGlyph = 0; a < MathUtils.round(ySize / lineSize); a++){
			GlyphLayout.GlyphRun nLine = null;
			if (glyphIndex < e.runs.size){
				float py = e.runs.get(glyphIndex).y;
				if (py == lineMove){
					nLine = e.runs.get(glyphIndex);
				}
			}
			if (y < startY + lineMove && y > startY + lineMove - lineSize){ // eilute kurioj paspausde
//				int usedGlyph = 0;
				if (nLine == null){
					// tuscia eilute. terp glyph saraso nera.
//					currentLine = index;
//					alignHorizontalCursor(0);
//					alignVerticalCursor(lineMove);
					if (usedGlyph == realText.length && glyphIndex == e.runs.size){ // auksciau teksto.
						int lastGlyph = usedGlyph - 1;
						if (!(lastGlyph < realText.length && realText.charAt(lastGlyph) == '\n')) {
							glyphIndex--;
							prevGlyps += usedGlyph - e.runs.get(glyphIndex).glyphs.size;
							break;
						}
					}
					cursorPos = usedGlyph;
					updateCursorPos();
//					getFocus();
					return;
				}else {
//					currentLine = index;
					prevGlyps += usedGlyph;
				}
				break;
			}
			lineMove -= lineSize;
			if (nLine != null){
				usedGlyph += e.runs.get(glyphIndex).glyphs.size;
				if (usedGlyph < realText.length && realText.charAt(usedGlyph) == '\n'){
					usedGlyph++;
				}
				glyphIndex++;
			}else {
				if (usedGlyph < realText.length && realText.charAt(usedGlyph) == '\n'){
					usedGlyph++;
				}
			}
		}

//		if (e.runs.size > 1){
//			for (int a = 0; a < e.runs.size; a++){
//				GlyphLayout.GlyphRun nLine = e.runs.get(a);
//				if (y > startY + nLine.y - lineSize){
//					currentLine = a;
//					break;
//				}
//				prevGlyps += nLine.glyphs.size;
//				while (prevGlyps < realText.length && realText.charAt(prevGlyps) == '\n'){
//					prevGlyps++;
//				}
//			}
//		}else {
//			currentLine = 0;
//		}

		if (e.runs.size == glyphIndex)
			return; // pastoviai luzta del sito.
		// kurioj eilutes vietoj cursorius.
		GlyphLayout.GlyphRun line = e.runs.get(glyphIndex);
		float length = pos.x + line.x + line.xAdvances.get(0);
		int cursor = 0;
		float distX = length;
		if (line.glyphs.size > 0) { // netuscia eilute
			if (x > length + line.width) { // uz teksto
				cursor = line.glyphs.size;
				distX = length + line.width;
			} else if (x < length + line.xAdvances.get(1) / 2) { // pries teksta arba pirma raide
				cursor = 0;
				distX = length;
			} else {
				for (int a = 0; a < line.glyphs.size; a++) {
					float size = line.xAdvances.get(a + 1);
					if (x < length + size / 2) {
						distX = length;
						cursor = a;
						break;
					}
					if (a == line.glyphs.size - 1) { // paskutine raide.
						distX = length + size;
						cursor = a+1; // nes 1 daugiau.
						break;
					}
					length += size;
				}
			}
		}
		cursorPos = cursor + prevGlyps;
		cursorX = distX;
		alignVerticalCursor(line.y);
//		getFocus();
	}

//	@Override
//	public void onRelease(float x, float y) {
//
//	}

//	@Override
//	public void onClick() {
//
//	}

	public void setBackground(Drawable e){
		if (e !=null)
			background = e;
	}

	public Drawable getBackground() {
		return background;
	}

	public void setMaxLength(int len){
		maxLength = len;
		String name = getText();
		if (name.length() > len){
			name = name.substring(0, len);
			setText(name);
		}
	}

	public void lockText(boolean lock){
		lockText = lock;
	}

	public boolean istextLocked(){
		return lockText;
	}

	/* mask */

	public void setMask(char mask){
		this.mask = mask;
		enableMask(true);
	}

	public void enableMask(boolean enable){
		if (hasMask != enable) {
//			String text = getText();
			hasMask = enable;
//			if (hasMask) {
//				setText(text);
//			} else {
//				setText(realText);
//			}
			setText(realText.toString());
		}
	}

	/* borders. */

	public void enableBorders(boolean enable){
		hasBorder = enable;
	}

	public void setBorderWidth(float width){
		borderWidth = MoreUtils.abs(width);
	}
	
	public void setMultiline(boolean mult){
		multiline = mult;
	}
	
	public void setTextListener(TextChangedListener e){
		list = e;
	}

	public interface TextChangedListener {
		/** gražinus true, tekstas nebus pakeistas. */
		boolean textChanged(String old, String current, char a, TextBox owner);
	}

	/* style */

	@Override
	public TextBoxStyle getStyle(){
		TextBoxStyle e = new TextBoxStyle();
		copyStyle(e);
		return e;
	}

	public void copyStyle(TextBoxStyle st){
		super.copyStyle(st);
		st.tintBackground = Color.argb8888(tintBackground);
		st.background = background;
		st.mask = mask;
		st.hasMask = hasMask;
		st.maxLength = maxLength;
		st.hasBorders = hasBorder;
		st.borderWidth = borderWidth;
		st.blinkSpeed = blinkSpeed;
		st.lockText = istextLocked();
	}

	public void readStyle(TextBoxStyle st){
		super.readStyle(st);
		Color.argb8888ToColor(tintBackground, st.tintBackground);
		background = st.background;
		setMask(st.mask);
		hasMask = st.hasMask;
		setMaxLength(st.maxLength);
		hasBorder = st.hasBorders;
		setBorderWidth(st.borderWidth);
		blinkSpeed = st.blinkSpeed;
		lockText(st.lockText);
	}

	public static class TextBoxStyle extends TextStyle{
		/** if hasBorders true then this will be used to tint backgournd */
		public int tintBackground = 0xFFFFFFFF;
		public Drawable background = Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor"));
		public boolean hasMask = false;
		public char mask = '*';
		public int maxLength = 200;
		public boolean hasBorders = true;
		public float borderWidth = 1f;
		/** speed in milliseconds  */
		public int blinkSpeed = 1000;
		/** if true, text cannot be changed by keyboard. */
		public boolean lockText = false;
//		public int borderColor = 0xFF000000;
//		public int borderFocusedColor = GdxPongy.color(255,100,20);

		public TextBoxStyle(){
			normalColor = 0xFF000000;
			onColor = GdxPongy.color(255, 100, 20);
			pressedColor = 0xFFAAAAAA;
			width = textSize*2.5f;
			height = textSize;
		}

		@Override
		public TextBox createInterface() {
			return new TextBox(this);
		}
	}
}
