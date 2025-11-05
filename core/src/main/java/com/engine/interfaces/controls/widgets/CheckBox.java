package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.engine.core.Resources;
import com.engine.interfaces.controls.TextBlock;

public class CheckBox extends TextBlock {
	private boolean checked;
	private CheckedListener listener;
	//rez
	private float rectSize;
	private Drawable check;
	private Drawable box;

	private boolean hasText;

	private float rTextOffsetX;

	public CheckBox(){
		this("New CheckBox");
	}

	public CheckBox(String text){
		this(text, new CheckBoxStyle());
	}

	public CheckBox(CheckBoxStyle style){
		super(style);
		check = style.checkedBox;
		box = style.box;
		if (box == null){
//			p.setError("CheckBox drawables cannot be null", ErrorType.WrongPara);
			box = Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor"));
		}
		if (check == null){
			check = Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor"));
		}
		checked = style.checked;
		rectSize = style.boxSize;

//		rTextOffsetX = style.textOffsetX;
	}

	public CheckBox(String text, CheckBoxStyle style){
		this(style);
		setText(text);
	}

	@Override
	protected void sizeUpdated(){
		super.sizeUpdated();
		if (!isAuto()){
			if (rectSize == 0)
				rectSize = ySize;
		}
		super.setTextOffset(rectSize + rTextOffsetX, getTextOffsetY());
		setCustomTextBounds(xSize-rectSize-rTextOffsetX, ySize);
	}

	@Override
	protected void autoSize() {
		super.autoSize();
		rectSize = ySize;
		xSize = rectSize + xSize + rTextOffsetX;
//		sizeUpdated();
	}

	@Override
	public void setSize(float xSize, float ySize) {
		rectSize = ySize;
		super.setSize(xSize, ySize);
	}

	@Override
	public void setTextOffset(float x, float y) {
		rTextOffsetX = x;
		super.setTextOffset(x, y);
	}

	@Override
	protected void isvaizda(float nx, float ny) {
		/* test */
//		p.stroke(255,0,0);
//		p.noFill();
//		p.strokeWeight(4);
//		p.rect(nx, ny, nxSize, nySize);

		p.tint(statusColor);
		if (hasText)
			drawDrawable(checked ? check : box, nx, ny, rectSize, rectSize, true);
		else
			drawDrawable(checked ? check : box, nx, ny, xSize, ySize, false); // ner to teksto.
		p.noTint();
	}

	@Override
	public boolean keyDown(int e){
		if (super.keyDown(e))
			return true;
		if (onEnterPress(e)){
			checkStateTriggered();
			return true;
		}
		return false;
	}

	/** is it checked. */
	public boolean isChecked(){
		return checked;
	}

	/** Check or uncheck checkbox. */
	public void setChecked(boolean check){
		checked = check;
	}

	void checkStateTriggered(){
		checked = !checked;
		if (listener != null){
			listener.onCheck(checked);
		}
	}

	public void setCheckListener(CheckedListener e){
		listener = e;
	}

	public CheckedListener getCheckListener(){
		return listener;
	}

	/** size of box. then you can change it's size here */
	public void setBoxSize(float size){
		rectSize = size;
	}

	/** size of box */
	public float getBoxSize(){
		return rectSize;
	}

	@Override
	protected void onClick() {
		super.onClick();
		checkStateTriggered();
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		hasText = !(text != null && text.equals(""));
	}

	public interface CheckedListener {
		/** Called when check state changes. */
		void onCheck(boolean checked);
	}

	/* style */

	@Override
	public CheckBoxStyle getStyle(){
		CheckBoxStyle e = new CheckBoxStyle();
		copyStyle(e);
		return e;
	}

	public void copyStyle(CheckBoxStyle st){
		super.copyStyle(st);
		st.box = box;
		st.checkedBox = check;
		st.checked = isChecked();
		st.boxSize = rectSize;
	}

	public void readStyle(CheckBoxStyle st){
		super.readStyle(st);
		box = st.box;
		check = st.checkedBox;
		rectSize = st.boxSize;
		setChecked(st.checked);
	}

	public static class CheckBoxStyle extends TextStyle{
		/** Unchecked box. */
		public Drawable box;
		/** Checked box. */
		public Drawable checkedBox;
		/** should it be check or unchecked. */
		public boolean checked = false;

		/** Box drawable size. If auto size is false then this will be used. If 0 then it will be changed to height. */
		public float boxSize = 0;

		public CheckBoxStyle(){
			verticalAlign = Align.center;
			pressedColor = 0xFFAAAAAA;
			text = "New CheckBox";

			textOffsetX = 3; // ka nebut prilipes.
		}

		@Override
		public CheckBox createInterface() {
			return new CheckBox(this);
		}
	}
}
