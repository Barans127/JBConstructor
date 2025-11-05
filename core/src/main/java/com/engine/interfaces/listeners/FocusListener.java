package com.engine.interfaces.listeners;

import com.engine.interfaces.controls.Interface;

public interface FocusListener {
	/** Called when control loses focus. */
	void onLostFocus(Interface e);
	/** Called when control gain focus. */
	void onFocus(Interface e);
}
