package com.engine.root;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class CustomShapeRenderer extends ShapeRenderer {
	private ImmediateModeRenderer renderer;
	// nustatyt ar reik projection update.
//	private long frameCount;
	private OrthographicCamera lastCamera;

	private GdxPongy pongy;
	
	public CustomShapeRenderer(GdxPongy pongy){
		renderer = getRenderer();
		this.pongy = pongy;
	}

	@Override
	public void begin() {
		updateProjection();

		super.begin();
	}

	@Override
	public void begin(ShapeType type) {
		updateProjection();

		super.begin(type);
	}

	private void updateProjection(){
//		long frameId = Gdx.graphics.getFrameId();
		OrthographicCamera camera = pongy.getActiveCamera();
		// priklauso nuo kameros ir kada buvo update darytas.
		// jeigu kameros nesutampa, tada atnaujinam projection matrix.
		if (camera != lastCamera){
			setProjectionMatrix(camera.combined);

			lastCamera = camera;
//			frameCount = frameId;
		}
	}

	/** Call this to force shape renderer update it's projection. */
	public void update(){
		lastCamera = null;
	}

	@Override
	public void arc (float x, float y, float radius, float start, float degrees, int segments) {
		// Copied from libgdx. Modifed to remove inside lines.
		if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
		float colorBits = getColor().toFloatBits();
		float theta = (2 * MathUtils.PI * (degrees / 360.0f)) / segments;
		float cos = MathUtils.cos(theta);
		float sin = MathUtils.sin(theta);
		float cx = radius * MathUtils.cos(start * MathUtils.degreesToRadians);
		float cy = radius * MathUtils.sin(start * MathUtils.degreesToRadians);

		if (getCurrentType() == ShapeType.Line) {
//			checkedBox(ShapeType.Line, ShapeType.Filled, segments * 2 + 2);
//			ShapeType a = getCurrentType();
//			end();
//			begin(a);

//			renderer.color(colorBits);
//			renderer.vertex(x, y, 0);
//			renderer.color(colorBits);
//			renderer.vertex(x + cx, y + cy, 0);
			for (int i = 0; i < segments; i++) {
				renderer.color(colorBits);
				renderer.vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				renderer.color(colorBits);
				renderer.vertex(x + cx, y + cy, 0);
			}
//			renderer.color(colorBits);
//			renderer.vertex(x + cx, y + cy, 0);
////			cx = 0;
////			cy = 0;
//			renderer.color(colorBits);
//			renderer.vertex(x + cx, y + cy, 0);
			
		} else {
//			checkedBox(ShapeType.Line, ShapeType.Filled, segments * 3 + 3);

			for (int i = 0; i < segments; i++) {
				renderer.color(colorBits);
				renderer.vertex(x, y, 0);
				renderer.color(colorBits);
				renderer.vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				renderer.color(colorBits);
				renderer.vertex(x + cx, y + cy, 0);
			}
			renderer.color(colorBits);
			renderer.vertex(x, y, 0);
			renderer.color(colorBits);
			renderer.vertex(x + cx, y + cy, 0);
			cx = 0;
			cy = 0;
			renderer.color(colorBits);
			renderer.vertex(x + cx, y + cy, 0);
		}
	}
}
