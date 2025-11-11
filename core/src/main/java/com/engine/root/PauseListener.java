package com.engine.root;

/** Pause listeners, when app is paused. For android -> when user switches to different app this
 * will be called. */
public interface PauseListener {
    /** Called when user exits application or application loses focus. */
    public void onPause();

    /** Called when user comes back to application or application got focus. */
    public void onResume();
}
