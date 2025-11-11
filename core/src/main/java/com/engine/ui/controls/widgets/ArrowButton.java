package com.engine.ui.controls.widgets;

import com.badlogic.gdx.graphics.Color;

public class ArrowButton extends Button {
	private int way;
	private float w3 = 3;
	private final Color lineColor = new Color();
//	private float old;

	public enum LookWay{
		Down(1), Up(0), Left(2), Right(3);
		private final int way;

		LookWay(int way){
			this.way = way;
		}

		public int getWay(){
			return way;
		}
	}

	public ArrowButton(ArrowButtonStyle st){
		super("", st);
		lineColor.a = 1;
		this.way = st.direction.getWay();
//		setSize(30, 30); // default
		enableShapeTransform(true);
		update = true;
	}

	public ArrowButton() {
		this(new ArrowButtonStyle());
	}

	public ArrowButton(LookWay w){
		this(new ArrowButtonStyle());
		this.way = w.getWay();
	}

	/** range:
	 * r g b: 0 - 255
	 * a: 0-1 */
	public void setLineColor(int r, int g, int b, float a){
		lineColor.set(r/255, g/255, b/255, a);
	}

	@Override
	protected void autoSize(){
		setSize(30, 30);
	}

	@Override
	protected void sizeUpdated() {
		super.sizeUpdated();
		w3 = Math.min(xSize, ySize) / 10;
	}

//	@Override
//	protected void setAppearCounter() {
//		super.setAppearCounter();
//		old = lineColor.a;
//	}

	public void changeArrowSide(LookWay e){
		way = e.getWay();
	}

	@Override
	protected void isvaizda(float nx, float ny){
		transformMatrix(nx, ny);
	}

//	@Override
//	protected void tint(float value) {
//		super.tint(value);
//		lineColor.a = value * old;
//	}

	@Override
	protected void drawTransformed(float nx, float ny) {
		p.tint(statusColor);
		getBackground().draw(p.getBatch(), nx, ny, xSize, ySize);
		p.noTint();
		p.strokeWeight(w3);
		p.stroke(lineColor);
		float start1x=0, start1y=0, start2x=0, start2y=0, endx=0, endy=0;
		switch (way){
			case 0: // zemyn
				start1x = nx+(xSize/5);
				start2x = nx+(xSize/5)*4;
				endx = nx+(xSize/2);
				endy = ny+(ySize/5)*4;
				start1y = ny+(ySize/5);
				start2y = ny+(ySize/5);
				break;
			case 1: // aukstyn
				start1x = nx+(xSize/5);
				start2x = nx+(xSize/5)*4;
				endx = nx+(xSize/2);
				endy = ny+(ySize/5);
				start1y = ny+(ySize/5)*4;
				start2y = ny+(ySize/5)*4;
				break;
			case 2: // kairen
				start1x = nx+(xSize/5)*4;
				start2x = nx+(xSize/5)*4;
				endx = nx+(xSize/5);
				endy = ny+(ySize/2);
				start1y = ny+(ySize/5);
				start2y = ny+(ySize/5)*4;
				break;
			case 3: // desinen
				start1x = nx+(xSize/5);
				start2x = nx+(xSize/5);
				endx = nx+(xSize/5)*4;
				endy = ny+(ySize/2);
				start1y = ny+(ySize/5)*4;
				start2y = ny+(ySize/5);
				break;
		}
		p.line(start1x, start1y, endx, endy);
		p.line(start2x, start2y, endx, endy);
	}

	@Override
	public ArrowButtonStyle getStyle(){
		ArrowButtonStyle st = new ArrowButtonStyle();
		copyStyle(st);
		return st;
	}

	public void copyStyle(ArrowButtonStyle st){
		super.copyStyle(st);
		LookWay e;
		switch (way){
			case 0:
				e = LookWay.Up;
				break;
			case 1:
				e = LookWay.Down;
				break;
			case 2:
				e = LookWay.Left;
				break;
			default:
				e = LookWay.Right;
				break;
		}
		st.direction = e;
	}

	public void readStyle(ArrowButtonStyle st){
		super.readStyle(st);
		way = st.direction.getWay();
	}

	public static class ArrowButtonStyle extends ButtonStyle{
		public LookWay direction = LookWay.Up;

		@Override
		public ArrowButton createInterface() {
			return new ArrowButton(this);
		}
	}
}
