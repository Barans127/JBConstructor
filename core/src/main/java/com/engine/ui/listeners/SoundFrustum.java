package com.engine.ui.listeners;

/** Interface to detect from which side sound is coming from and to not play sound if the object is too far.
 * This interface gives info about where specified object is. It does not handle sound or music. */
public interface SoundFrustum {
//    float viewPortHalfWidth = Engine.getInstance().getScreenWidth()/2f;

    /** updates pan and volume. */
    void update(float pan, float volume);

    /** maximum distance in which sound will be panned. If distance is greater then -1 or 1 pan is used (depending on which side object is). */
    float getPanningWidth();

    /** When object is in minimum distance then panning will be set to 0. */
    float getMinPanningWidth();

    /** maximum distance in which object can be heard. */
    float getVolumeWidth();

    /** When object is in minimum distance then volume will be played at max. */
    float getMinVolumeWidth();

    /** whether sound is playing and should be stopped if too far or should sound be started if not playing. */
    boolean isPlaying();

    /** Called when sound is playing and object is too far and sound should be stopped.
     * This method will be called right after {@link #update(float, float)} method. */
    void stop();

    /** Called when object comes close and sound is not playing.
     * This method will be called right after {@link #update(float, float)} method. */
    void start();

    /** This time is used to delay {@link #start()} call (a lot of start calls may produce audio bug - audio stops playing). Time in milliseconds. */
    int getDelayTime();
}
