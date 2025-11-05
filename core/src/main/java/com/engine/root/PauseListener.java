package com.engine.root;

public interface PauseListener {
    /** Called when user exits application or application loses focus. */
    public void onPause();

    /** Called when user comes back to application or application got focus. */
    public void onResume();
}
