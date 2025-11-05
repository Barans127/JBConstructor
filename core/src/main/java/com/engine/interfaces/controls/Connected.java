package com.engine.interfaces.controls;

/**
 * Leidžia grupuot elementus ir bendraut tarpusavy.
 */
public interface Connected {
    public void inform(int reason);
    public int getGroup();
}
