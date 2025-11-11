package com.engine.ui.listeners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.root.SoundController;

/** Sound instance frustum control unit. */
public class SoundFrustumManager implements SoundFrustum {
    // reikalingieji.
    private float minPanning, maxPanning;
    private float minVolume, maxVolume;

    // sound.
    private SoundController.SoundWrapper sound;
    private long id = -1;
    private float pitch = 1f;
    private float fadeOutTime = 0.3f;
    private int delay = 300;
    private float maxAvailableVolume = 1f;

    // veikiamieji.
    // is playing skirtas tam, kad nustatyt ar sound gali but paleistas, Tai nereisk, kad sound is playing.
    // su situo nustatom ar galima paleist sound ar objeckt per daug nutoles ir sound negalim paleist.
    private boolean isPlaying, isLoopPlay;
    private float pan, volume;
    private boolean loop;

    /** Sound frustum with default parameters. minPanningWidth and minVolumeWidth - 120 units.
     * maxPanningWidth - visible screen width 50%.
     * maxVolumeWidth - visible screen width 75%.*/
    public SoundFrustumManager(SoundController.SoundWrapper sound, boolean loop){
        this(sound, 120f, Engine.getInstance().getScreenWidth()/2f, 120f,
                Engine.getInstance().getScreenWidth()/2f * 1.5f, loop);
    }

    public SoundFrustumManager(SoundController.SoundWrapper sound, float minPanningWidth, float maxPanningWidth,
                               float minVolumeWidth, float maxVolumeWidth, boolean loop){
        if (sound == null){
            Engine.getInstance().setError("SoundFrustumManager", "Sound instance cannot be null.");
            return;
        }
        this.sound = sound;

        setMinPanningWidth(minPanningWidth);
        setMaxPanningWidth(maxPanningWidth);
        setMinVolumeWidth(minVolumeWidth);
        setMaxVolumeWidth(maxVolumeWidth);
        setLoop(loop, false);
    }

    /** Plays current sound with frustum parameters. If object is out of range sound
     * will not be played and -1 returned. This frustum volume, pitch and sound is used for playing this sound.
     * This sound will not be controlled by frustum therefore frustum parameters should be set manually.
     * @return played sound id or if object is too far -1.*/
    public long play(Sound sound){
        if (volume > 0){
            return sound.play(volume * maxAvailableVolume, pitch, pan);
        }
        return -1;
    }

    /** Custom sound playing. Sound can only be played if object is closer to camera by maxVolumeWidth.
     * Sound's pan and volume will be automatically set by frustum parameters (depends where object is).
     * This sound instance volume and pan will be updated.
     * @return sound id. If object is too far then sound is not played and -1 is returned.*/
    public long play(){
        return play(pitch);
    }

    /** Custom sound playing. Sound can only be played if object is closer to camera by maxVolumeWidth.
     * Sound's pan and volume will be automatically set by frustum parameters (depends where object is).
     * *This sound instance volume and pan will be updated.
     * @return sound id. If object is too far then sound is not played and -1 is returned.*/
    public long play(float pitch){
        if (volume > 0){
            if (id != -1 && isLoopPlay){
//                stop(); // jeigu loop ar dar kas buvo, tai reik ana stabdyt.
//                sound.setLooping(id, false); // tiesiog isjungiam loop.
                sound.stop(id); // bugina tas libgdx garsai...
            }
            id = sound.play(volume * maxAvailableVolume, pitch, pan);
            isLoopPlay = false;
            return id;
        }
        return -1;
    }

    /** Custom sound playing. Sound can only be played if object is closer to camera by maxVolumeWidth.
     * Sound's pan and volume will be automatically set by frustum parameters (depends where object is).
     * This sound instance volume and pan will be updated. Loop will be switched off if object and camera
     * distance become greater than {@link #getVolumeWidth()} distance.
     * @return sound id. If object is too far then sound is not played and -1 is returned.*/
    public long loop(){
        return loop(pitch);
    }

    /** Custom sound playing. Sound can only be played if object is closer to camera by maxVolumeWidth.
     * Sound's pan and volume will be automatically set by frustum parameters (depends where object is).
     * This sound instance volume and pan will be updated. Loop will be switched off if object and camera
     * distance become greater than {@link #getVolumeWidth()} distance.
     * @return sound id. If object is too far then sound is not played and -1 is returned.*/
    public long loop(float pitch){
        if (volume > 0){
            if (id != -1 && isLoopPlay){ // jeigu pries tai loop
//                stop();
                // stabdom loopinius, nes po ta kaupias anie.
//                sound.setLooping(id, false);
                sound.stop(id); // bugina tas libgdx garsai...
            }
            id = sound.loop(volume * maxAvailableVolume, pitch, pan);
            isLoopPlay = true;
            return id;
        }
        return -1;
    }

    /** Custom sound playing. Sound can only be played if object is closer to camera by maxVolumeWidth.
     * Sound's pan and volume will be automatically set by frustum parameters (depends where object is).
     * This sound instance volume and pan will be updated. Loop will be switched off if object and camera
     * distance become greater than {@link #getVolumeWidth()} distance.
     * @return sound id. If object is too far then sound is not played and -1 is returned.*/
    public long fadeIn(float time, boolean loop){
        return fadeIn(time, pitch, loop);
    }

    /** Custom sound playing. Sound can only be played if object is closer to camera by maxVolumeWidth.
     * Sound's pan and volume will be automatically set by frustum parameters (depends where object is).
     * This sound instance volume and pan will be updated. Loop will be switched off if object and camera
     * distance become greater than {@link #getVolumeWidth()} distance.
     * @return sound id. If object is too far then sound is not played and -1 is returned.*/
    public long fadeIn(float time, float pitch, boolean loop){
        if (volume > 0){
            if (id != -1 && isLoopPlay){
                sound.stop(id);
            }
            id = sound.fadeIn(time, loop);
            sound.setPitch(id, pitch);
            isLoopPlay = loop;
            return id;
        }
        return -1;
    }

    @Override
    public void stop() {
        if (isPlaying){
//            sound.setLooping(id, false);
//            if (loop) {
            if (id != -1) { // jei kazkas vyksta - stabdom.
                if (fadeOutTime > 0) {
                    sound.fadeOut(id, volume, fadeOutTime);
                } else {
                    sound.stop(id);
                }
                isLoopPlay = false;
                id = -1;
            }
//            }
            volume = 0; // kadangi nebe playing, tai dedam volume 0, kuris tarsi pazymes, kad viskas.
            isPlaying = false;
//            System.out.println(this + " stop");
        }else if (id != -1 && isLoopPlay){
            sound.stop(id);
            isLoopPlay = false;
        }
    }

    @Override
    public void start() {
        if (!isPlaying){
            if (loop) {
                if (isLoopPlay && id != -1){ // jei kartais buvo loop stabdom.
//                    sound.setLooping(id, false);
                    sound.stop(id); // bugina tas libgdx garsai...
                }
                id = sound.loop(volume * maxAvailableVolume, pitch, pan);
                isLoopPlay = true;
            }
            isPlaying = true;
//            System.out.println(this + " start");
        }
    }

    @Override
    public void update(float pan, float volume) {
        this.pan = pan;
        this.volume = volume;

        sound.setPan(id, pan, volume * maxAvailableVolume);

        // apejimas, kai nezinom ar garsas pasileides ar ne.
        // kadangi sound neturi tikrinomo del einamo garso, tai mes sitaip pazesim ar garsa vis del to
        // groja. Su -1, vadinas garsas nebuvo paleistas, todel mes dar karta ana paleidziam.
        if (loop && isPlaying){ // patikrinam ar su garsu ner problemu jei loop rezime.
            if (id == -1 && volume > 0){
                id = sound.loop(volume * maxAvailableVolume, pitch, pan);
                isLoopPlay = true;
            }
        }

//        Gdx.app.log("Frustum", "Pan: " + pan + "; Volume: " + volume);
    }

    /* setters */

    /** max volume of this frustum. Default- 1. Value must be in [0:1] bounds. */
    public void setMaxAvailableVolume(float volume){
        maxAvailableVolume = MoreUtils.inBounds(volume, 0, 1);
    }

    /** Delay which is used to prevent spamming method {@link #start()}. Time in milliseconds. Default 300 */
    public void setDelayTime(int time){
        if (time >= 0){
            delay = time;
        }
    }

    /** Time for sound to fade out when object is out of range. Only for loop!
     * Time in seconds. Default - 0.3 */
    public void setFadeOutTime(float time){
        this.fadeOutTime = time;
    }

    /** pitch for this sound. Default - 1; */
    public void setPitch(float pitch){
        this.pitch = pitch;
    }

    /** set new sound. If old sound was on loop, then loop will be switched off.
     * @param stopOldSound should old sound be stopped before switching to new one. */
    public void setSound(SoundController.SoundWrapper sound, boolean stopOldSound){
        if (sound != null) {
            if (stopOldSound) {
                stop();
            }else if (isLoopPlay){
//                this.sound.setLooping(id, false);
                this.sound.stop(id); // bugina tas libgdx garsai...
                isLoopPlay = false;
            }
            this.sound = sound;
            id = -1;
        }else {
            Gdx.app.log("SoundFrustumManager", "Sound is null. Cannot set null sound.");
        }
    }

    /** If object is in this distance then panning is set to 0 (both sides equally). Default: 120*/
    public void setMinPanningWidth(float minPanning){
        this.minPanning = minPanning;
    }

    /** max panning width. If distance is bigger then panning will be 1 or -1 (depends on side).
     * default: screen width 50% */
    public void setMaxPanningWidth(float width){
        maxPanning = width;
    }

    /** If object is in this minimum distance then volume is set to max. Default: 120 */
    public void setMinVolumeWidth(float minVolume) {
        this.minVolume = minVolume;
    }

    /** max volume width. If distance is greater then sound is stopped.
     * Default: screen width 75%*/
    public void setMaxVolumeWidth(float maxVolume){
        this.maxVolume = maxVolume;
    }

    /** Sound automatization. If loop true then sound will be played when object is near camera.
     * To custom run sound use {@link #play()} method.
     * @param stopSound should sound be stopped before changing loop parameters.*/
    public void setLoop(boolean loop, boolean stopSound){
        if (this.loop != loop){
            if (stopSound) { // senasis loop kaip ir eina. darom stop.
                stop(); // jei groja stabdys, jei ne - tai nieko nedarys
            }else if (isLoopPlay){ // vistiek isjungiam loop.
//                sound.setLooping(id, false);
                this.sound.stop(id); // bugina tas libgdx garsai...
                isLoopPlay = false;
            }
            this.loop = loop; // keicaim tik jei reik to.
        }
    }

    /* getters */

    /** max volume of this frustum. Default: 1f */
    public float getMaxAvailableVolume(){
        return maxAvailableVolume;
    }

    /** current playing sound id. If sound is not played then -1. */
    public long getSoundId(){
        return id;
    }

    /** Fade out effect time (occurring for loops when object is out of range). Only for loop!
     * Time in seconds. Default - 0.3 */
    public float getFadeOutTime(){
        return fadeOutTime;
    }

    /** pitch of this sound. */
    public float getPitch(){
        return pitch;
    }

    /** This frustum volume. This volume is calculated by camera and object distance. */
    public float getVolume(){
        return volume;
    }

    /** This frustum pan. This pan is calculated by camera and object positions. */
    public float getPan(){
        return pan;
    }

    /** Sound automatization. If loop true then sound will be played when object is near camera.
     * To custom run sound use {@link #play()} method.
     * Default: true. */
    public boolean isLoop(){
        return loop;
    }

    /** This frustum sound. */
    public SoundController.SoundWrapper getSound(){
        return sound;
    }

    @Override
    public float getPanningWidth() {
        return maxPanning;
    }

    @Override
    public float getMinPanningWidth() {
        return minPanning;
    }

    @Override
    public float getVolumeWidth() {
        return maxVolume;
    }

    @Override
    public float getMinVolumeWidth() {
        return minVolume;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public int getDelayTime() {
        return delay;
    }
}
