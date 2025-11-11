package com.engine.ui.listeners;

import com.engine.ui.controls.widgets.TextBox;

import static com.engine.ui.controls.widgets.TextBox.*;

/** Class to manage user given text. It only allows numbers in it. */
public class OnlyNumbers implements TextChangedListener {
	private int maxLen = 20; // default.
	private boolean hasLow, hasHigh, isFloat;
	private int low, high;

	public OnlyNumbers() {} // sitas tik integeriam.

	public OnlyNumbers(int maxLen){
		this.maxLen = maxLen;
	}

	@Override
	public boolean textChanged(String old, String current, char a, TextBox owner) {
		if (isFloat){
			if (a == 'f' || a == 'd'){
				return true;
			}
		}
		if (current.equals("")){
			return false;
		}else if (current.equals("-")){ // leis su minus rasyt.
			return false;
		}else if (current.contains("-")){
			if (isFloat && current.contains(".")){
				if (current.length() > maxLen+2){
					return true;
				}
			}else if (current.length() > maxLen+1){ // minuso neskaiciuos kaip skaiciaus.
				return true;
			}
		}else if (isFloat && old.contains(".")){
			if (current.length() > maxLen+1 || a == '.'){ // tasko neskaiciuos kaip skaiciaus. neleis antro tasko det
				return true;
			}
		}else{
			if (current.length() > maxLen){
				return true;
			}
		}
		try{
			float e;
			if (isFloat){
				e = Float.parseFloat(current);
			} else{
				e = Integer.valueOf(current);
			}
			if (hasHigh){
				if (e > high){
					owner.setText(high+"");
					return false;
				}
			}
			if (hasLow){
				if (e < low){
					owner.setText(low+"");
					return false;
				}
			}
			return false;
		}catch (NumberFormatException e){
			return true;
		}
	}

	public void setMaxLength(int len){
		if (len < 1){
			return;
		}
		maxLen = len;
	}

	public void setMaxNumber(int max){
		hasHigh = true;
		high = max;
	}

	public void setLowestNumber(int low){
		hasLow = true;
		this.low = low;
	}

	public void allowFloat(boolean allow){
		isFloat = true;
	}
}
