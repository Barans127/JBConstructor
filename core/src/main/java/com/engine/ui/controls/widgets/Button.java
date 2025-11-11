package com.engine.ui.controls.widgets;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.engine.core.Resources;
import com.engine.ui.controls.TextBlock;
import com.engine.root.GdxWrapper;

public class Button extends TextBlock{

	private Drawable background, pressedBackground, onBackground;

	private boolean extendSize;

	public Button(String text, ButtonStyle style){
		this(style);
		setText(text);
	}

	public Button(String text){
		this(text, new ButtonStyle());
	}

	public Button(ButtonStyle style){
		super(style);
//		enableLongPress = style.enableLongPress;
		background = style.background;
		pressedBackground = style.pressedBackground;
		onBackground = style.onBackground;

		extendSize = style.extendWidth;
	}

	public Button(){
		this(new ButtonStyle());
	}

	@Override
	protected void autoSize() {
		super.autoSize();
//		xSize *= 1.1f; // mazas kaip button gaunas, todel padidinam ploti.
		if (getText().length() > 0 && extendSize)
			xSize += getTextSize();
	}

	@Override
	protected void isvaizda(float x, float y){
        // TEST. To see where is button field.
//        p.stroke(0);
//        p.noFill();
//        Vector2 pos = getPosition();
//        p.rect(x, y, getWidth(), getHeight());

		Drawable looks;
		p.tint(statusColor);
		if (getStatus() == OVER){
			if (onBackground != null){
				looks = onBackground;
			}else {
				looks = background;
			}
		}else if (getStatus() == PRESSED) {
			if (pressedBackground != null){
				looks = pressedBackground;
			}else {
				looks = background;
			}
		}else {
			looks = background;
		}
		drawDrawable(looks, x, y, true); // nupieš viską.
		p.noTint();
	}

	public Drawable getBackground(){
		return background;
	}

	/** this button background. */
	public void setBackground(Drawable e){
		if (e == null)
			return;
		background = e;
	}

	public Drawable getPressedBackground(){
		return pressedBackground;
	}

	/** This drawable is when button is pressed.. If null then tinting on background occurs. */
	public void setPressedBackground(Drawable e){
		pressedBackground = e;
	}

	public Drawable getOnBackground(){
		return onBackground;
	}

	/** This drawable is when mouse is placed on button. If null then tinting on background occurs. */
	public void setOnBackground(Drawable e){
		onBackground = e;
	}

	/** Should autoSize add textSize to width. This is only used if autoSize enabled. */
	public boolean isSizeExtended(){
		return extendSize;
	}

	/** Should autoSize add textSize to width. This is only used if autoSize enabled. */
	public void setExtendSize(boolean extendSize){
		this.extendSize = extendSize;
	}

	@Override
	public boolean keyDown(int e){
		if (super.keyDown(e))
			return true;
		if (onEnterPress(e)){
			performClick();
			return true;
		}
		return false;
	}

	/** performs click. If click sound is set then it will be played. */
	public final void performClick(){
		performClick(false);
	}

	/** imitate click.
	 * @param silent if click sound is set then you can specify if it should be played or not. */
	public final void performClick(boolean silent){
		onClick();
		if (!silent) {
			Sound sound = getClickSound();
			if (sound != null) {
				sound.play();
			}
		}
	}

	/** Align this button in given cord. Button will center placed on this cord. x axis. */
	public void alignHorizontal(float x){
		float length = getWidth()/2;
		setPosition(x-length, getPosition().y);
	}

	/* style */

	@Override
	public ButtonStyle getStyle(){
		ButtonStyle e = new ButtonStyle();
		copyStyle(e);
		return e;
	}

	public void copyStyle(ButtonStyle st){
		super.copyStyle(st);
		st.background = getBackground();
		st.onBackground = onBackground;
		st.pressedBackground = pressedBackground;
		st.extendWidth = extendSize;
	}

	public void readStyle(ButtonStyle st){
		super.readStyle(st);
		setBackground(st.background);
		pressedBackground = st.pressedBackground;
		onBackground = st.onBackground;
		extendSize = st.extendWidth;
	}

	public static class ButtonStyle extends TextStyle{
		/** Default background is white image. */
		public Drawable background = Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor"));
		public Drawable pressedBackground = null; // vietoj spalvų keitimo keis paveikslėlius.
		public Drawable onBackground = null;

		/** Extends button length by textSize. This is only used if autoSize is set to true. */
		public boolean extendWidth = true;

		public ButtonStyle(){ // default parametrų pakeitmas į button default.
			pressedColor = GdxWrapper.color(200, 0, 0);
			normalColor = 0xFFFFFFFF;
			onColor = GdxWrapper.color(255, 0, 0);
			horizontalAlign = Align.center;
			verticalAlign = Align.center;
			text = "New Button";
		}

		@Override
		public Button createInterface() {
			return new Button(this);
		}
	}
}
