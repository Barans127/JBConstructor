package com.engine.animations.transitions;

import com.engine.core.Engine;
import com.engine.root.PostTask;
import com.engine.root.SoundController;

/** Manages music switch. Smooth switch. switch off first music and then runs other. */
public class SmoothMusicSwitcher {
    private PostTask postTask;

    private SoundController.MusicWrapper toSwitch, toFadeOut;
    private float fadeInTime, fadeOutTime;

    public SmoothMusicSwitcher(){
        this(new PostTask());
    }

    /** Constructs switcher with custom post task. */
    public SmoothMusicSwitcher(PostTask postTask){
        if (postTask == null){
            Engine.getInstance().setError("SmoothMusicSwitcher", "post task cannot be null.");
            return;
        }

        this.postTask = postTask;
        postTask.setRunnable(new Runnable() {
            @Override
            public void run() {
                if (toSwitch.isPlaying() && toSwitch.isFadingOut()){
                    toSwitch.stop();
                }
                toSwitch.fadeIn(fadeInTime);
            }
        });
    }

    /* isoriniai. */

    public void setPostTask(PostTask postTask){
        if (postTask != null){
            if (this.postTask.isPosted()){
                this.postTask.cancel();
            }
            this.postTask = postTask;
        }
    }

    /** is Music switching in progress. */
    public boolean isSwitching(){
        return postTask.isPosted();
    }

    /** Sets values but does not start switching.
     *      * @param toSwitch music which will be played.
     *      * @param playingMusic current playing music which will fade out.
     *      * @param fadeIn time in which new music will fade in. Time in seconds.
     *      * @param fadeOut time in which currently playing music will fade out. Time in seconds.  */
    public void setValues(SoundController.MusicWrapper toSwitch, SoundController.MusicWrapper playingMusic, float fadeIn, float fadeOut){
        this.toSwitch = toSwitch;
        this.toFadeOut = playingMusic;
        fadeInTime = fadeIn;
        fadeOutTime = fadeOut;
    }

    /** Switches musics. Playing music should be playing.
     * @param toSwitch music which will be played.
     * @param playingMusic current playing music which will fade out.
     * @param fadeIn time in which new music will fade in. Time in seconds.
     * @param fadeOut time in which currently playing music will fade out. Time in seconds. */
    public void switchMusic(SoundController.MusicWrapper toSwitch, SoundController.MusicWrapper playingMusic, float fadeIn, float fadeOut){
        setValues(toSwitch, playingMusic, fadeIn, fadeOut);
        switchMusic();
    }

    /** Switches musics. Playing music should be playing. Values must be set before running via method {@link #setValues(SoundController.MusicWrapper, SoundController.MusicWrapper, float, float)} */
    public void switchMusic(){
        if (toSwitch == null || toFadeOut == null){
            throw new RuntimeException("Music is not set!");
        }
        if (fadeOutTime <= 0){
            // nera fade out. Sis efektas nereikalingas...
            toFadeOut.stop();
            if (toSwitch.isPlaying() && toSwitch.isFadingOut()){
                toSwitch.stop();
            }
            toSwitch.fadeIn(fadeInTime);
            return;
        }

        // fade outinam ir po to jungsim nauja muzika.
        toFadeOut.fadeOut(fadeOutTime);

        postTask.setTime((int) (fadeOutTime*1000));
        postTask.post();
    }

    /** Cancels music switch. */
    public void cancel(){
        postTask.cancel();
    }
}
