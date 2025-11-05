package com.engine.interfaces.listeners;

public interface MainDraw {
	void draw();
	/** Iškviečia jeigu mainDraw sąrašas buvo ištuštinas. pvz per formų perjungimą.
	 * 0 - user drop, 1 - form change, 2 - pop up drop.
	 * @param reason what reason drop occurred. Can be pop up show up or close. Form change. User drop.
	 * @return false bus pašalintas iš sąrašo, true paliks sąraše.
	 **/
	boolean drop(int reason);
}
