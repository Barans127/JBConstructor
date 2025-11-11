package com.engine.ui.controls.widgets;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.ui.controls.Draggable;
import com.engine.root.GdxWrapper;

public class ScrollBar extends Draggable {
	private boolean onGround, inBar;
	private float offset, len, minLen;
	private float width, height;
//	private float nlen, noffset; // rezoliucijai

	private int jumps, currentJump;
	private boolean smooth, fixedStop;

	private Drawable background;
	private Color tintBackground;
//	private float oldAlpha;
	private Drawable bar;
	private ScrollListener list;

	// background padding.
	private float padX, padY;

	public ScrollBar(ScrollBarStyle style){
		super(style);
		onGround = style.horizontal;
		background = style.background;
		bar = style.scrollBar;
		jumps = style.jumps;
		if (jumps <= 0){
			jumps = 1;
		}
		// paskaiciuojam dydi.
//		minLen = style.minBarSize;
		setMinLen(style.minBarSize);
//		len = barWidth;
		setBarWidth(style.barSize);
		setBackgroundPadding(style.backgroundPaddingX, style.backgroundPaddingY);
		smooth = style.smooth;
		currentJump = style.value;
		fixedStop = style.fixedStop;
		width = style.width;
		height = style.height;
		tintBackground = new Color(1, 1, 1, 1);
		Color.argb8888ToColor(tintBackground, style.tintBackground);
		if (currentJump < 0 || currentJump >= jumps){
			currentJump = 0;
		}

		countCorrectOffset();
//		autoSize();
		auto();
		update = false;
	}

	public ScrollBar(){
		this(new ScrollBarStyle());
	}

	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	protected void autoSize() {
		if (width == 0 || height == 0){
			if (background == null){
				setVisible(false);
				return;
			}
			width = background.getMinWidth();
			height = background.getMinHeight();
		}
		if (onGround){
			xSize = width;
			ySize = height;
		}else{
			ySize = width;
			xSize = height;
		}
//		if (len == 0){
//			len = width/jumps;
//		}else if (len > xSize){
//			len = xSize;
//		}
		// perpatikrinam.
		setBarWidth(len);
//		sizeUpdated();
	}

//	@Override
//	protected void setAppearCounter() {
//		super.setAppearCounter();
//		oldAlpha = tintBackground.a;
//	}
//
//	@Override
//	protected void tint(float value) {
//		tintBackground.a = value * oldAlpha;
//	}

//	@Override
//	public void sizeUpdated(){
//		super.sizeUpdated();
//		nlen = len * r;
//		noffset = offset*r;
//	}

	@Override
	protected void isvaizda(float nx, float ny) {
		p.tint(tintBackground);
		drawBackground(background, nx, ny, xSize, ySize);
		p.tint(statusColor); // tintins tik barBackground. background paliks original color.
		if (onGround){
//			drawDrawable(bar, nx + offset, ny, len, ySize, radiusOrigin.x - offset,
//					radiusOrigin.y, false);
			drawStick(bar, nx + offset, ny, len, ySize, radiusOrigin.x - offset, radiusOrigin.y);
		}else{
//			drawDrawable(bar, nx, ny + offset, xSize, len, radiusOrigin.x,
//					radiusOrigin.y - offset, false);
			drawStick(bar, nx, ny + offset, xSize, len, radiusOrigin.x, radiusOrigin.y - offset);
		}
		p.noTint();
		/*test*/
//		p.stroke(0);
//		p.strokeWeight(3);
//		for (int a = 0; a < jumps; a++){
//			if (onGround) {
//				float status = nx + width / (jumps - 1) * a;
//				p.line(status, ny + nySize, status, ny);
//			}else {
//				float status = ny + width / (jumps - 1) * a;
//				p.line(nx, status, nx + nxSize, status);
//			}
//		}
	}

	/** background drawing. */
	protected void drawBackground(Drawable background, float x, float y, float width, float height){
		float rWitdth = width - width * padX;
		float rHeight = height - height * padY;
		float offsetX = (width - rWitdth)/2;
		float offsetY = (height - rHeight)/2;
		drawDrawable(background, x + offsetX, y + offsetY, rWitdth, rHeight, false);
	}

	/** Moving stick drawing. */
	protected void drawStick(Drawable stick, float x, float y, float width, float height, float originX, float originY){
		drawDrawable(stick, x, y, width, height, originX, originY, false);
	}

//	@Override
//	public boolean onLongClick(float x, float y) {
//		return false;
//	}

	@Override
	protected void onPress(float x, float y) {
		super.onPress(x, y);
		if (jumps == 1){
			offset = 0;
//			noffset = 0;
			return;
		}
		if (mouseInBar(x, y)){
			inBar = true;
		}else{
			normalStatus();
			if (getAngle() != 0){
				Vector2 a = rotatePoint(x, y, -getAngle());
				x = a.x;
				y = a.y;
			}
			inBar = false;
			float half = width / (jumps-1)/2;
			Vector2 pos = getPosition();
			float cord = onGround ? pos.x : pos.y;
			float point = onGround ? x : y;
			for (int a = 0; a < jumps; a++){
				float status = cord + width/(jumps-1) * a;
				if (point > status - half && point < status + half){
					setValue(a);
					scrollTriggered();
					break;
				}
			}
		}
	}

	@Override
	protected void onRelease(float x, float y) {
		super.onRelease(x, y);
		inBar = false;
	}

	@Override
	protected void onDragging(float x, float y, float deltaX, float deltaY) {
		if (inBar) {
			if (jumps == 1){
				offset = 0;
//				noffset = 0;
				return;
			}
			if (getAngle() != 0){
				Vector2 a = rotatePoint(x, y, -getAngle());
				x = a.x;
				y = a.y;
			}
			float half = width / (jumps-1)/2;// puse suolio.
			Vector2 pos = getPosition();
			float cord = onGround ? pos.x : pos.y; // pradzios cord
			float point = onGround ? x : y; // pelytes cord
			float length = onGround ? xSize : ySize; // i ploti ar ilgi
			float jumpSize = width/(jumps-1); // suolio dydis

			float dif = point - cord + half;
			int ans = (int) (dif/jumpSize);
			if (ans < 0)
				ans = 0;
			else if (ans >= jumps)
				ans = jumps-1;
			if (smooth){
				if (point > cord && point < cord + length) {
					offset += deltaX;
					if (offset + len > width) {
						offset = width - len;
					} else if (offset < 0) {
						offset = 0;
					}
				}else if (point < cord){
				    offset = 0;
                }else {
				    offset = width-len;
                }
			}
			if (ans != currentJump){ // nieks nesikeicia.
				currentJump = ans;
				scrollTriggered();
				if (!smooth)
					countCorrectOffset();
			}


//			Vector2 position = getPosition();
//			float jumpSize = width/(jumps-1)/2; // is 2 kad kazkur per viduri perjunktu.
//			float currentPos = width/(jumps-1)*currentJump;
//			float dif;
//			boolean update = false;
//			if (onGround) {
//				dif = x - (position.x + currentPos);
//				if (smooth) {
//					if (x > position.x && x < position.x + xSize) {
//						offset += deltaX;
//						update = true;
//					}
//				}
//			} else {
//				dif = y - (position.y + currentPos);
//				if (smooth) {
//					if (y > position.y && y < position.y + ySize) {
//						offset += deltaY;
//						update = true;
//					}
//				}
//			}
//			float side = 1f;
//			if (dif < 0){
//				side = -1f;
//			}
//			dif = Resources.abs(dif);
//			int count = 0;
//			while (dif > jumpSize){
//				count++;
//				dif -= jumpSize;
//			}
//			if (count > 0){
//				int number = currentJump;
//				count *= side;
//				number += count;
//				if (number < 0){
//					number = 0;
//				}else if (number >= jumps){
//					number = jumps-1;
//				}
//				currentJump = number;
//				scrollTriggered();
//				update = true;
//			}
//			if (update) {
//				if (smooth) {
//					if (offset + len > width) {
//						offset = width - len;
//					} else if (offset < 0) {
//						offset = 0;
//					}
////					noffset = offset * r;
//				} else
//					countCorrectOffset();
//			}
		}
	}

	@Override
	protected void onDrop(float x, float y, float deltaX, float deltaY) {
		if (smooth && fixedStop)
			countCorrectOffset();
	}

	protected void scrollTriggered(){
		if (list != null){
			list.onScroll(currentJump);
		}
	}

	/* po smooth dropo, arba jei nera smooth */
	private void countCorrectOffset(){
		if (jumps == 1){
			offset = 0;
//			noffset = 0;
			return;
		}
		offset = (width/(jumps-1)*currentJump) - len/2; // is len, kad butu kazkur per viduri drawable.
		if (offset + len > width) {
			offset = width - len;
		} else if (offset < 0) {
			offset = 0;
		}
//		noffset = offset * r;
	}

//	@Override
//	public void onClick() {
//
//	}

	/** Background padding. In Percents. (0.5 means half). Values bigger then 1 may lead to undefined behavior */
	public void setBackgroundPadding(float padX, float padY){
		this.padX = padX;
		this.padY = padY;
	}

	/** Background padding. In Percents. (0.5 means half). */
	public float getBackgroundPaddingX(){
		return padX;
	}

	/** Background padding. In Percents. (0.5 means half). */
	public float getBackgroundPaddingY(){
		return padY;
	}

	/** where scroll bar currently is. */
	public int getValue(){
		return currentJump;
	}

	/** set where scroll bar should be now. */
	public void setValue(int value){
		int realValue = value;
		if (value < 0){
			realValue = 0;
		}else if (value >= jumps){
			realValue = jumps - 1;
		}
		currentJump = realValue;
		if (jumps > 1)
			countCorrectOffset();
	}

	/**
	 * @param color argb8888 format
     */
	public void setBackgroundTint(int color){
		Color.argb8888ToColor(tintBackground, color);
	}

	/**
	 * nustatys ar scroll progressBar bus horizontalus ar vertikalus
	 * @param ground true: horizontalus, false: vertikalus.
	 */
	public void setHorizontal(boolean ground){
		onGround = ground;
//		autoSize();
		auto();
		update = false;
	}

	/** how many jumps should this scroll bar have. When you scroll the scroll bar jumps over these jump count.*/
	public void setJumpsCount(int jumps){
		if (jumps > 0){
			int realValue = currentJump;
			if (realValue < 0){
				realValue = 0;
			}else if (realValue >= jumps){
				realValue = jumps - 1;
			}
			currentJump = realValue;
			this.jumps = jumps;
			if (jumps > 1){
				countCorrectOffset();
			}else {
				offset = 0;
//				noffset = 0;
			}
		}
	}

	@Override
	public boolean keyDown(int keycode) {
//		if (onEnterPress(keycode)){
//			System.out.println("boom");
//		}
		if (super.keyDown(keycode))
			return true;
		if (isFocused()){
			if (keycode == Input.Keys.DOWN || keycode == Input.Keys.RIGHT){
				setValue(getValue()+1);
				return true;
			}else if (keycode == Input.Keys.UP || keycode == Input.Keys.LEFT){
				setValue(getValue()-1);
				return true;
			}
		}
		return false;
	}

	@Override
	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
//		autoSize();
		auto();
		update = false;
	}

	/** how big bar width should be. If zero then width/height will be used and if smaller then minimum size then minimum size will be used.
	 * width cannot be samller then minimum size. Change minimum size if need with {@link #setMinLen(float)} method.*/
	public void setBarWidth(float width){
		float num = width;
		if (width == 0){
			num = width/((float) jumps);
			if (num < minLen){
				num = minLen;
			}
		}else if (width < minLen){
			num = minLen;
		}
		len = num;
//		len = width;
//		autoSize();
//		auto();
		update = true;
	}

	/** Scroll bar will slide smoothly (no correction during sliding).
	 * If false then correction will occur and bar will jump in it's jump locations. */
	public void setSmoothSliding(boolean smooth){
		this.smooth = smooth;
	}

	/** Should scroll bar stop at it's fixed position (correnction will occur after drag) or stay where it is left. */
	public void setFixedStop(boolean fixedStop){
		this.fixedStop = fixedStop;
	}

	/**
	 * Pasakys ar pelyte yra butent ant slankiojamo bloko.
	 */
	private boolean mouseInBar(float mx, float my){
//		int mx = InputHandler.mouseX;
//		int my = InputHandler.mouseY;
		float stX, stY, endX, endY;
		Vector2 position = getPosition();
		if (onGround){
			stX = position.x + offset;
			stY = position.y;
			endX = stX + len;
			endY = position.y + ySize;
		}else{
			stX = position.x;
			stY = position.y + offset;
			endX = position.x + xSize;
			endY = stY + len;
		}
		if (getAngle() == 0) {
			return mx > stX && mx < endX && my > stY && my < endY;
		}else {
			Vector2 t = rotatePoint(mx, my);
			return t.x > stX && t.x < endX && t.y > stY && t.y < endY;
		}
	}

	/** Sets minimum lenght of bar if size is to small. */
	public void setMinLen(float min){
		if (min > 0){
			minLen = min;
//			setBarWidth(len);
		}
	}

	/** jumps of this scroll bar. */
	public int getJumps(){
		return jumps;
	}

	/** Min len that bar can be. */
	public float getMinLen(){
		return minLen;
	}

	/** is this bar horizontal or vertical. */
	public boolean isBarHorizontal(){
		return onGround;
	}

	/** This is not the same as {@link #getWidth()}. If horizontal it returns width or else it will return height */
	public float getWidthSize(){
		return width;
	}

	/** This is not the same as {@link #getHeight()} ()}. If horizontal it returns height or else it will return width */
	public float getHeightSize(){
		return height;
	}

	/** Set scroll listener for scroll bar. */
	public void setScrollListener(ScrollListener e){
		list = e;
	}

	/** Scroll listener of this scroll bar. */
	public ScrollListener getScrollListener(){
		return list;
	}

	public interface ScrollListener{
		void onScroll(int currentValue);
	}

	/* style */

	@Override
	public ScrollBarStyle getStyle(){
		ScrollBarStyle e = new ScrollBarStyle();
		copyStyle(e);
		return e;
	}

	public void copyStyle(ScrollBarStyle st){
		super.copyStyle(st);
		st.background = background;
		st.tintBackground = Color.argb8888(tintBackground);
		st.scrollBar = bar;
		st.horizontal = onGround;
		st.smooth = smooth;
		st.fixedStop = fixedStop;
		st.jumps = jumps;
		st.value = getValue();
		st.barSize = len;
		st.minBarSize = minLen;
		st.backgroundPaddingX = padX;
		st.backgroundPaddingY = padY;
	}

	public void readStyle(ScrollBarStyle st){
		super.readStyle(st);
		background = st.background;
		setBackgroundTint(st.tintBackground);
		bar = st.scrollBar;
		setHorizontal(st.horizontal);
		setSmoothSliding(st.smooth);
		setFixedStop(st.fixedStop);
		setJumpsCount(st.jumps);
		setValue(st.value);
		setMinLen(st.minBarSize);
		setBarWidth(st.barSize);
		setBackgroundPadding(st.backgroundPaddingX, st.backgroundPaddingY);
	}

	public static class ScrollBarStyle extends ClickableStyle{
		public Drawable background;
		/** tints background. Background color usually don't change. If you want to tint bar drawable use {@link #normalColor} etc.*/
		public int tintBackground = 0xFFFFFFFF;
		public Drawable scrollBar;
		/** true - scroll barBackground will lie on ground, false - scroll barBackground will go up */
		public boolean horizontal = true;
//		/** if true jumps will be used otherwise percents will be used. */
//		public boolean useJumps = true;
		public boolean smooth = true;
		/** if true after releasing drag, barBackground will be changed to position equal to current value position. */
		public boolean fixedStop = true;
		public int jumps = 10;
		public int value = 0;
		/** if 0, then default will be taken width/jumps.
		 * If default is used then bar will not go less then {@link #minBarSize} */
		public float barSize = 0;
		/** Minimum bar size if bar size is not defined. Bar size will not go any less then this value. */
		public float minBarSize = 20;

		/** background padding */
		public float backgroundPaddingX = 0;
		/** background padding */
		public float backgroundPaddingY = 0.5f;

		public ScrollBarStyle(){
//			width = 300; // ?????
//			height = 20;
			pressedColor = GdxWrapper.color(180, 180, 180, 255);
		}

		@Override
		public ScrollBar createInterface() {
			return new ScrollBar(this);
		}
	}
}
