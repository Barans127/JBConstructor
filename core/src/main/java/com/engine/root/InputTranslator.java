package com.engine.root;

import com.badlogic.gdx.InputProcessor;

/** Handling desktop inputs. Key presses and mouse movement. Touch inputs and gestures are handled in gestureDetector. */
public class InputTranslator implements InputProcessor {
	private final GdxWrapper p;

	public InputTranslator(GdxWrapper p) {
		this.p = p;
	}

    /* These controls are not handled by gesture detector. Usually they are used in desktop version. */
	@Override
	public boolean keyDown(int keycode) {
		return p.keyDown(keycode);
	}

	@Override
	public boolean keyUp(int keycode) {
		return p.keyUp(keycode);
	}

	@Override
	public boolean keyTyped(char character) {
		return p.keyTyped(character);
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return p.mouseMoved(screenX, screenY);
	}

	@Override
	public boolean scrolled(float amountX, float amountY){
        return p.scrolled(amountX, amountY);
    }

    /* These are handled by gesture detector, so not handling it here. */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}
