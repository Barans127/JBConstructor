package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.interfaces.controls.TextBlock;

public class Label extends TextBlock {
//	private boolean underline, hasBorder;
//	private float lineWeight, lineLenght, strokeWeight;
//	private int lineColor, lx, ly; // line x, line y

	// teksto apvedimai.
	private boolean hasBorder;
	private float strokeWeight;
	private int borderColor;

//	private float oldLine, oldBorder;
	// background jei toki turi.
	private Drawable background;

	public Label(){
		this("New Label");
	}

	public Label(LabelStyle style){
		super(style);
//		underline = style.underline;
		hasBorder = style.border;
//		lineWeight = style.lineWeight;
		strokeWeight = style.borderWeight;
//		lineColor = style.lineColor;
		borderColor = style.borderColor;
		background = style.background;
		auto();
//		update = true;
//		if (underline || hasBorder){
		if (hasBorder) {
//			if (underline)
//				checkLine();
			enableShapeTransform(true);
		}
	}

	public Label(String name){
		this(name, new LabelStyle());
	}

	public Label(String name, LabelStyle style) {
		this(style);
//		setFocusable(false);
//		if (!hasBackground) { // nesamone
//			barBackground = null;
//		}
		setText(name); // Šitas iškvies autoSize().
//		if (underline || hasBorder){
		if (hasBorder){
//			if (underline)
//				checkLine();
			enableShapeTransform(true);
		}
	}

//	@Override
//	public void appear(float speed) {
//		super.appear(speed);
//		oldBorder = (borderColor >> 24 & 0xFF)/255f;
//		oldLine = (lineColor >> 24 & 0xFF)/255f;
//	}

//	@Override
//	protected void tint(float value) {
//		super.tint(value);
//		if (hasBorder)
//			borderColor = borderColor | ((int) (value * 255 * oldBorder) << 24);
//		if (underline)
//			lineColor = lineColor | ((int) (value * 255 * oldLine) << 24);
//
//	}

	@Override
	protected void isvaizda(float x, float y) {
//		if (hasBorder || underline){
		if (hasBorder){
			transformMatrix(x, y);
		}else {
			if (background != null){
				drawDrawable(background, x, y, true);
			}else {
//				p.noFill();
//				p.stroke(0);
//				p.rect(x, y, xSize, ySize);
				drawText();
			}
		}
	}

	@Override
	protected void drawTransformed(float x, float y) {
		if (background != null) {
			background.draw(p.getBatch(), x, y, xSize, ySize);
		}
		if (hasBorder){
			p.stroke(borderColor);
			p.strokeWeight(strokeWeight);
			p.noFill();
			p.rect(x, y, xSize, ySize);
		}
//		if (underline) {
//			p.strokeWeight(lineWeight);
//			p.stroke(lineColor);
//			p.line(lx + v.getOffsetX(), ly + v.getOffsetY(), lx + lineLenght, ly);
//		}
		drawText(x, y, 0);
	}

//	@Deprecated
//	public void underlineText(boolean under){
//		underline = under;
//		if (under){
//			enableShapeTransform(true);
//			checkLine();
//		}else {
//			if (!hasBorder)
//				enableShapeTransform(false);
//		}
//	}
	
//	private void checkLine(){ // o jei multiline?
//		p.textSize(getTextSize()); // kad zinotu koks dydis.
//		lineLenght = p.textWidth(getText());
////		lineLenght = lineLenght;
//		Vector2 pos = getPosition();
//		switch(getVerticalAlign()){
//		case Align.left:
//			lx = MathUtils.round(pos.x);
//			break;
//		case Align.right:
//			lx = (int) (pos.x+xSize-lineLenght);
//			break;
//		case Align.center:
//			lx = (int) ((xSize/2) + pos.x - (lineLenght/2));
//			break;
//		}
//		switch(getHorizontalAlign()){
//		case Align.top:
//			float a = p.textHeight();
////			a = a;
//			ly = (int) (pos.y + a);
//			break;
//		case Align.center:
//			float b = p.textHeight();
////			b = b;
//			ly = (int) (pos.y+(ySize/2) + b);
//			break;
//		case Align.bottom:
//			ly = MathUtils.round(pos.y+ySize);
//			break;
//		}
//		ly += lineWeight/2;
//	}

//	@Deprecated
//	public void setLineWeight(float a){
//		lineWeight = a;
//	}

//	@Deprecated
//	public void setLineColor(int color){
//		lineColor = color;
//	}
	
//	public void setBackground(int color){
//		hasBackground = true;
//		normalColor = color;
//		normalStatus();
//		barBackground = null;
//	}

	public void setBackground(Drawable e){
//		hasBackground = e != null;
		background = e;
	}
	
//	public void noBackground(){
//		hasBackground = false;
//	}

	public void setBorderColor(int color){
		borderColor = color;
	}

	public void setBorderWeight(float weight){
		if (weight > 0){
			hasBorder = true;
			enableShapeTransform(true);
		}else if (weight <= 0){
			hasBorder = false;
//			if (!underline)
			enableShapeTransform(false);
			return;
		}
		strokeWeight = weight;
	}
	
//	@Override
//	public void release(){}

	/* style */

	@Override
	public LabelStyle getStyle(){
		LabelStyle e = new LabelStyle();
		copyStyle(e);
		return e;
	}

	public void copyStyle(LabelStyle st){
		super.copyStyle(st);
		st.background = background;
//		st.underline = underline;
//		st.lineWeight = lineWeight;
//		st.lineColor = lineColor;
		st.border = hasBorder;
		st.borderWeight = strokeWeight;
		st.borderColor = borderColor;
	}

	public void readStyle(LabelStyle st){
		super.readStyle(st);
		setBackground(st.background);
//		underline = st.underline;
//		lineWeight = st.lineWeight;
//		lineColor = st.lineColor;
		// bug sukeldavo, nes border weight ijungdavo borderio piesima.
		setBorderWeight(st.borderWeight);
		setBorderColor(st.borderColor);
		hasBorder = st.border;
	}

	public static class LabelStyle extends TextStyle{
		public Drawable background = null;
//		@Deprecated
//		public boolean underline = false;
//		@Deprecated
//		public float lineWeight = 1f;
//		@Deprecated
//		public int lineColor = textColor;
		public boolean border = false;
		public float borderWeight = 3f;
		public int borderColor = 0xFF000000;

		public LabelStyle(){
			focusable = false;
			text = "New Label";
		}

		@Override
		public Label createInterface() {
			return new Label(this);
		}
	}
}
