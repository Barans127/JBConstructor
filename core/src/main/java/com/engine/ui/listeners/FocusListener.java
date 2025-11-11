package com.engine.ui.listeners;

import com.engine.ui.controls.Control;

public interface FocusListener {
	/** Called when control loses focus. */
	void onLostFocus(Control e);
	/** Called when control gain focus. */
	void onFocus(Control e);
}
