package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.engine.interfaces.controls.Field;

public class LoadIcon extends Field {
	private float radius;
	private float ballRadius;
	private float spinningSpeed; // Default was 5.

	public LoadIcon(LoadIconStyle style) {
		super(style);
		spinningSpeed = style.spinningSpeed;
		setFocusable(false);
		if (style.iconRadiusX > 0)
			setSize(style.iconRadiusX, style.iconRadiusY);
		auto();
	}

	public LoadIcon(){
		this(new LoadIconStyle());
	}

	@Override
	public void setSize(float xSize, float ySize) {
		super.setSize(xSize, ySize);
//		if (nxSize >= nySize)
//			ballRadius = nxSize/5;
//		else
//			ballRadius = nySize/5;
		ballRadius = Math.max(this.xSize, this.ySize) / 5;
		radius = 0;
	}

	@Override
	protected void autoSize() {
		if (xSize == 0 || ySize == 0){
			setSize(20, 20);
		}
//		sizeUpdated();
	}

	@Override
	protected void isvaizda(float nx, float ny) {
		p.fill(statusColor);
		p.noStroke();
		float spin = radius;
		for (int a = 0; a < 5; a++, spin += 40*MathUtils.degreesToRadians){
			float x, y;
			x = MathUtils.cos(spin) * xSize/2 + nx+xSize/2;
			y = MathUtils.sin(spin) * ySize/2 + ny+ySize/2;
			p.ellipse(x, y, ballRadius, ballRadius);
		}
		if (isEnabled()){
            float frameRate = 60f; // Assuming we have 60 timeframes as usually games have.
			radius += spinningSpeed * MathUtils.degreesToRadians * Gdx.graphics.getDeltaTime() * frameRate;
			if (radius > MathUtils.PI2){
				radius -= MathUtils.PI2;
			}else if (radius < -MathUtils.PI2){
				radius += MathUtils.PI2;
			}
		}
	}

	public void setSpinSpeed(float speed){
		spinningSpeed = speed;
	}

//	@Override
//	public void release(){}

	/* style */

	@Override
	public LoadIconStyle getStyle(){
		LoadIconStyle e = new LoadIconStyle();
		copyStyle(e);
		return e;
	}

	public void copyStyle(LoadIconStyle st){
		super.copyStyle(st);
		st.spinningSpeed = spinningSpeed;
		st.iconRadiusX = getWidth();
		st.iconRadiusY = getHeight();
	}

	public void readStyle(LoadIconStyle st){
		super.readStyle(st);
		setSpinSpeed(st.spinningSpeed);
		setSize(st.iconRadiusX, st.iconRadiusY);
	}

	public static class LoadIconStyle extends FieldStyle{
		public float spinningSpeed = 5f;
		public float iconRadiusX = 20;
		public float iconRadiusY = 20;

		public LoadIconStyle(){
			normalColor = 0xFFFF0000;
			width = 20;
			height = 20;
			autoSize = false;
		}

		@Override
		public LoadIcon createInterface() {
			return new LoadIcon(this);
		}
	}
}
