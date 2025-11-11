package com.engine.animations;

/** Implementation of animation between Engine Window forms.*/
public interface SwitchAnimation {
//	int animationIndex = -3;
	void changeState(int index);
	void action();
	void fixAction();
}
