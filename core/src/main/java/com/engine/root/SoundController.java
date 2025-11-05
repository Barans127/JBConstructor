package com.engine.root;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.engine.animations.Counter;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.core.SoundLoader;

import java.util.HashMap;
import java.util.Set;

/** This class manages sounds, music. It handles all mutes, volumes, sound loading. Sounds and music have their own volume and mute. There also is master volume
 * and mute to change volume and mute all sounds and music ant once. */
public class SoundController implements Disposable {
    // bendri nustatymai.
    private float masterVolume = 1f;
    private boolean mute = false;
    //sound nustatymai.
    private float soundVolume = 1f;
    private boolean muteSound = false;
    //music nustatymai
    private float musicVolume = 1f;
    private boolean muteMusic = false;

    // garso laikymas.
//    private Array<SoundWrapper> soundWrappers;
//    private Array<MusicWrapper> musicWrappers;
    private HashMap<String, SoundWrapper> soundWrappers;
    private HashMap<String, MusicWrapper> musicWrappers;

    // garso efektu pool
    private Pool<MusicFadeEffect> musicFadeEffectPool;
    private Pool<SoundFadeEffect> soundFadeEffectPool;

    public SoundController(){
        soundWrappers = new HashMap<>();
        musicWrappers = new HashMap<>();

        // pools.
        musicFadeEffectPool = new Pool<MusicFadeEffect>() {
            @Override
            protected MusicFadeEffect newObject() {
                return new MusicFadeEffect();
            }
        };
        soundFadeEffectPool = new Pool<SoundFadeEffect>() {
            @Override
            protected SoundFadeEffect newObject() {
                return new SoundFadeEffect();
            }
        };
    }

    /* bendras valdymas */
    /* volume keitimas. */

    /** Changes music volume (sound is not affected). Volume [0-1] */
    public void setMusicVolume(float volume){
        if (this.musicVolume != volume){
            volume = MoreUtils.inBounds(volume, 0,1f);
            this.musicVolume = volume;

            changeMusicVolume();
        }
    }

    /** Changes sound volume (music is not affected). Volume [0-1]. (Sound which is currently playing will not be affected). */
    public void setSoundVolume(float volume){
        if (this.soundVolume != volume){
            volume = MoreUtils.inBounds(volume, 0,1f);
            this.soundVolume = volume;
        }
    }

    /** Change master volume. This will change all sound and music volume (Music will be affected immanently but Sound which is currently playing will not be affected).
     * Volume [0-1] */
    public void setMasterVolume(float volume){
        if (this.masterVolume != volume) {
            volume = MoreUtils.inBounds(volume, 0, 1f);

            this.masterVolume = volume;

            changeMusicVolume();
        }
    }

    private void changeMusicVolume(){
        Set<String> musicSet = musicWrappers.keySet();
        for (String e : musicSet){
            MusicWrapper m = musicWrappers.get(e);
            m.setVolume(m.desiredVolume); // keiciam i nauja master volume.
        }
    }

    /* mutinimas. */

    /** Mute or unmute music (doesn't affect sound). Muting music will cause all currently playing music to stop.*/
    public void muteMusic(boolean mute){
        if (muteMusic != mute){
            if (mute){
                stopMusic();
            }
            muteMusic = mute;
        }
    }

    /** Mute or unmute sounds (doesn't affect music). Muting will cause all currently playing sounds to stop. */
    public void muteSound(boolean mute){
        if (muteSound != mute){
            if (mute){
                stopSounds();
            }
            this.muteSound = mute;
        }
    }

    /** Mute or unmute all sounds and music. Muting sounds will cause all current sounds and music to stop. */
    public void mute(boolean mute){
        if (this.mute != mute){
            if (mute) {
                // stabdom visus grojimus.
//                stopMusic();
//                stopSounds();
                stopAllSounds();
            }
            this.mute = mute;
        }
    }

    /* bendras visu garsu valdymas. */

    /** stops all sounds (music and sounds). */
    public void stopAllSounds(){
        stopMusic();
        stopSounds();
    }

    /** Stops all sound instances (music is not affected). */
    public void stopSounds(){
        // stabdom visus grojimus.
        Set<String> soundSet = soundWrappers.keySet();
        for (String e : soundSet){
            soundWrappers.get(e).stop();
        }
    }

    /** stops all music instances (sound is not affected). */
    public void stopMusic(){
        Set<String> musicSet = musicWrappers.keySet();
        for (String e : musicSet){
            musicWrappers.get(e).stop();
        }
    }

    /** Pauses all sounds (music and sounds). */
    public void pauseAllSounds(){
        pauseSounds();
        pauseMusic();
    }

    /** pause sounds (music is not affected). */
    public void pauseSounds(){
        // stabdom visus grojimus.
        Set<String> soundSet = soundWrappers.keySet();
        for (String e : soundSet){
            soundWrappers.get(e).pause();
        }
    }

    /** pause music (sound is not affected). */
    public void pauseMusic(){
        Set<String> musicSet = musicWrappers.keySet();
        for (String e : musicSet){
            musicWrappers.get(e).pause();
        }
    }

    /** Resumes all sounds (music and sounds). If sounds/music were not paused then they are ignored. */
    public void resumeAllSounds(){
        resumeSounds();
        resumeMusic();
    }

    /** Resume sounds (music is not affected). If sound were not paused then it is ignored. */
    public void resumeSounds(){
        Set<String> soundSet = soundWrappers.keySet();
        for (String e : soundSet){
            soundWrappers.get(e).resume();
        }
    }

    /** Resume music (sound is not affected). If music was not paused then it is ignored. */
    public void resumeMusic(){
        Set<String> musicSet = musicWrappers.keySet();
        for (String e : musicSet){
            musicWrappers.get(e).resume();
        }
    }

    /* getters */

    /** is music muted (not sound). */
    public boolean isMuteMusic(){
        return muteMusic;
    }

    /** music volume (not sound). */
    public float getMusicVolume(){
        return musicVolume;
    }

    /** is sound muted (not music). */
    public boolean isMuteSound(){
        return muteSound;
    }

    /** sound volume (not music). */
    public float getSoundVolume(){
        return soundVolume;
    }

    /** is all sounds muted */
    public boolean isMute(){
        return mute;
    }

    /** volume of all sounds. */
    public float getMasterVolume(){
        return masterVolume;
    }

    /* garso gavimas. */

    /** Sound instance. This instance's volume and mute is controlled by {@link SoundController}. */
    public SoundWrapper getSound(String id){
        return soundWrappers.get(id);
    }

    /** Music instance. This instance's volume and mute is controlled by {@link SoundController}.*/
    public MusicWrapper getMusic(String id){
        return musicWrappers.get(id);
    }

    /* garso uzkrovimas. */

    /** Adds sound to this controller. {@link SoundWrapper} instance is returned. */
    public SoundWrapper addSound(String id, Sound sound){
        if (id == null || id.length() == 0){
            Engine.getInstance().setError("SoundController", "Sound id cannot be zero length");
            return null;
        }
        if (sound == null){
            Engine.getInstance().setError("SoundController", "Sound cannot be null");
            return null;
        }
        if (soundWrappers.containsKey(id)){
            Engine.getInstance().setError("SoundController", "Key already exists: " + id);
            return null;
        }
        SoundWrapper e = new SoundWrapper(sound);
        soundWrappers.put(id, e);
        return e;
    }

    /** Adds music to this controller. {@link MusicWrapper} instance is returned. */
    public MusicWrapper addMusic(String id, Music music){
        if (id == null || id.length() == 0){
            Engine.getInstance().setError("SoundController", "Music id cannot be zero length");
            return null;
        }
        if (music == null){
            Engine.getInstance().setError("SoundController", "Music cannot be null");
            return null;
        }
        if (musicWrappers.containsKey(id)){
            Engine.getInstance().setError("SoundController", "Key already exists: " + id);
            return null;
        }
        MusicWrapper e = new MusicWrapper(music);
        musicWrappers.put(id, e);
        return e;
    }

    /** Loads sounds into this controller.
     * @param soundLoader which is used to load resources. SoundLoaderTracker listener will be replaced by this controller listeners.
     * @param asynchronousLoad true if sounds should load in other thread or false if sounds should load all now and here.*/
    public void loadSounds(SoundLoader soundLoader, boolean asynchronousLoad){
        loadSounds(soundLoader, null, asynchronousLoad);
    }

    /** Loads sounds into this controller.
     * @param soundLoader which is used to load resources. SoundLoaderTracker listener will be replaced by this controller listeners.
     *  @param loadedSoundList list where all loaded sound id is placed. Can be null. List will not be cleared!
     *  @param asynchronousLoad true if sounds should load in other thread or false if sounds should load all now and here.*/
    public void loadSounds(SoundLoader soundLoader, final Array<String> loadedSoundList, boolean asynchronousLoad){
        if (soundLoader == null){
//            Engine.getInstance().setError("SoundController", "SoundLoader cannot be null.");
//            return;
            throw new NullPointerException("SoundLoader cannot be null.");
        }
        if (soundLoader.isLoading()){
//            Engine.getInstance().setError("SoundController", "SoundLoader is busy.");
//            return;
            throw new RuntimeException("SoundLoader is already loading sounds");
        }
        soundLoader.setSoundLoaderTracker(new SoundLoader.SoundLoaderTracker() {
            @Override
            public void soundLoaded(Sound sound, String id) {
//                SoundWrapper e = new SoundWrapper(sound);
//                soundWrappers.put(id, e);
                addSound(id, sound);
                if (loadedSoundList != null){
                    loadedSoundList.add(id);
                }
            }

            @Override
            public void musicLoaded(Music music, String id) {
//                MusicWrapper e = new MusicWrapper(music);
//                musicWrappers.put(id, e);
                addMusic(id, music);
                if (loadedSoundList != null){
                    loadedSoundList.add(id);
                }
            }
        });
//        soundLoader.setAddToResources(false); // sitas sound controller atsakingas uz soundus.
        if (asynchronousLoad)
            soundLoader.startLoadAsynchronous();
        else
            soundLoader.loadAllNow();
    }

    /* naikinimas */

    /** Removes sound or/and music instance and disposes it. */
    public void removeSound(Array<String> keys){
        for (String key : keys){
            removeSound(key);
        }
    }

    /** Removes sound or/and music instance and disposes it. */
    public void removeSound(String key){
        boolean found = false;
        if (soundWrappers.containsKey(key)){
            SoundWrapper e = soundWrappers.get(key);
            soundWrappers.remove(key);
            e.stop(); // nes toliau groja??
            e.dispose();
            Gdx.app.log("SoundController", "Disposing sound: " + key);
            found = true;
        }
        if (musicWrappers.containsKey(key)){
            MusicWrapper e = musicWrappers.get(key);
            musicWrappers.remove(key);
            e.stop();
            e.dispose();
            Gdx.app.log("SoundController", "Disposing music: " + key);
            found = true;
        }
        if (found) {
            // jeigu kazka istrine tai paleidziam ir pools.
            musicFadeEffectPool.clear();
            soundFadeEffectPool.clear();
        }else
            Gdx.app.log("SoundController", "Cannot find any sound with id: " + key);
    }

    @Override
    public void dispose() {
        boolean disposed = false;
        /// sound trinimas
        Set<String> soundSet = soundWrappers.keySet();
        for (String e : soundSet){
            SoundWrapper wrapper = soundWrappers.get(e);
            wrapper.stop(); // nes toliau groja po dispose???
            wrapper.dispose();
            disposed = true;
        }
        soundWrappers.clear();

        /// music trinimas
        Set<String> musicSet = musicWrappers.keySet();
        for (String e : musicSet){
            MusicWrapper wrapper = musicWrappers.get(e);
            wrapper.stop();
            wrapper.dispose();
            disposed = true;
        }
        musicWrappers.clear();

        // paleidziam pools.
        musicFadeEffectPool.clear();
        soundFadeEffectPool.clear();

        if (disposed)
            Gdx.app.log("SoundController", "Sounds were disposed.");
    }

    /* wrapper classes. */

    /** Wrapper for sound class. */
    public class SoundWrapper implements Sound{
        private Sound sound;
        private int delay = 0, delayTime;

        private Array<SoundFadeEffect> soundFadeEffects;

        SoundWrapper(Sound sound){
            this.sound = sound;
            soundFadeEffects = new Array<>();
        }

        /** Sometimes sound instance may get too many play calls. To prevent that spam you can set delay so this sound instance will only play
         * in some time. Etc: collecting a lot of gold coins cause a lot of play calls, setting delay to 100 so only in every 100ms new sound
         * instance will be played any other call before that time will be ignored.
         * NOTE: Delayer only works for single play, all loop calls ignores this delayer.
         * Time in milliseconds. Default: 0. Set to 0 to disable delayer. */
        public void setDelayTime(int time){
            if (time >= 0){
                delay = time;
            }
        }

        /** Delay time to prevent playing spam.
         * @return time in milliseconds. 0 - delayer is disabled. Default: delayer disabled. */
        public int getDelayTime(){
            return delay;
        }

        /** Sound will start and go more and more louder until it reaches it's desired volume. Volume is set to 1.
         * @param time how fast should it reach it's desired volume.
         * @param loop should sound be played in a loop. */
        public long fadeIn(float time, boolean loop){
            return fadeIn(time, loop, 1f);
        }

        /** Sound will start and go more and more louder until it reaches it's desired volume.
         * @param time how fast should it reach it's desired volume.
         * @param loop should sound be played in a loop.
         * @param volume desired volume. Usually it is just 1f.*/
        public long fadeIn(float time, boolean loop, float volume){
            // paleidziam garsa.
            long id;
            if (loop){
                id = loop(volume);
            }else {
                id = play(volume);
            }

            if (id != -1) {// nepasileido.

                interrupt(id); // check if not exist.

                SoundFadeEffect e = soundFadeEffectPool.obtain();
                e.startFade(this, id, false, volume, time);
                soundFadeEffects.add(e);
            }
            return id;
        }

        /** Sound will go quieter more and more till it stops. starting volume is set to 1.
         * @param id - sound id.
         * @param time time in which sound will stop.*/
        public void fadeOut(long id, float time){
            fadeOut(id, 1f, time);
        }

        /** Sound will go quieter more and more till it stops.
         * @param id - sound id.
         * @param startingVolume sound doesn't save volume, so you have to provide starting volume. Usually it is just 1.
         * @param time time in which sound will stop.*/
        public void fadeOut(long id, float startingVolume, float time){
            interrupt(id); // check if not exist.

            SoundFadeEffect e = soundFadeEffectPool.obtain();
            e.startFade(this, id,  true, startingVolume, time);
            soundFadeEffects.add(e);
        }

        private void interrupt(long id){
            // paziurim ar tokio nera.
            for (int a = 0; a < soundFadeEffects.size; a++){
                SoundFadeEffect e = soundFadeEffects.get(a);
                if (e.soundId == id){
                    e.interrupt();
                    break;
                }
            }
        }

        /** Plays sound with default volume. If sound is muted then does nothing. */
        @Override
        public long play(){
            return play(masterVolume * soundVolume);
        }

        /** Plays sound with given volume. If sound is muted then does nothing. */
        @Override
        public long play(float volume){
            if (mute || muteSound){
                return -1;
            }else {
                if (delay > 0){ // delayer ijungtas
                    int time = Engine.getInstance().millis();
                    if (delayTime < time){
                        // viskas gerai, leidziam.
                        delayTime = time + delay; // nustatom nauja delay laika.
                    }else {
                        // dar nepraejo laikas
                        return -1; // draudziam play leidima.
                    }
                }
                return sound.play(volume * masterVolume * soundVolume);
            }
        }

        @Override
        public long play(float volume, float pitch, float pan){
            if (mute || muteSound)
                return -1;
            else {
                if (delay > 0){ // delayer ijungtas
                    int time = Engine.getInstance().millis();
                    if (delayTime < time){
                        // viskas gerai, leidziam.
                        delayTime = time + delay; // nustatom nauja delay laika.
                    }else {
                        // dar nepraejo laikas
                        return -1; // draudziam play leidima.
                    }
                }
                return sound.play(volume * masterVolume * soundVolume, pitch, pan);
            }
        }

        @Override
        public long loop() {
            return loop(masterVolume * soundVolume);
        }

        @Override
        public long loop(float volume) {
            if (mute || muteSound){
                return -1;
            }else {
                return sound.loop(volume * masterVolume * soundVolume);
            }
        }

        @Override
        public long loop(float volume, float pitch, float pan) {
            if (mute || muteSound){
                return -1;
            }else {
                return sound.loop(volume * masterVolume * soundVolume, pitch, pan);
            }
        }

        @Override
        public void stop() {
            if (!mute && !muteSound){
                // visus affektina.
                for (int a = 0; a < soundFadeEffects.size; a++){
                    soundFadeEffects.get(a).interrupt();
                }
                sound.stop();
            }
        }

        @Override
        public void pause() {
            if (!mute && !muteSound){
                // visus affektina.
                for (int a = 0; a < soundFadeEffects.size; a++){
                    soundFadeEffects.get(a).interrupt();
                }
                sound.pause();
            }
        }

        @Override
        public void resume() {
            if (!mute && !muteSound){
                // visus affektina.
                for (int a = 0; a < soundFadeEffects.size; a++){
                    soundFadeEffects.get(a).interrupt();
                }
                sound.resume();
            }
        }

        @Override
        public void dispose() {
            // visus metam.
            for (int a = 0; a < soundFadeEffects.size; a++){
                soundFadeEffects.get(a).interrupt();
            }
            sound.dispose();
        }

        @Override
        public void stop(long soundId) {
            if (!mute && !muteSound){
                interrupt(soundId);
                sound.stop(soundId);
            }
        }

        @Override
        public void pause(long soundId) {
            if (!mute && !muteSound){
                interrupt(soundId);
                sound.pause(soundId);
            }
        }

        @Override
        public void resume(long soundId) {
            if (!mute && !muteSound){
                interrupt(soundId);
                sound.resume(soundId);
            }
        }

        @Override
        public void setLooping(long soundId, boolean looping) {
            if (!mute && !muteSound){
                sound.setLooping(soundId, looping);
            }
        }

        @Override
        public void setPitch(long soundId, float pitch) {
            if (!mute && !muteSound){
                sound.setPitch(soundId, pitch);
            }
        }

        @Override
        public void setVolume(long soundId, float volume) {
            if (!mute && !muteSound){
                sound.setVolume(soundId, volume * masterVolume * soundVolume);
            }
        }

        @Override
        public void setPan(long soundId, float pan, float volume) {
            if (!mute && !muteSound){
                sound.setPan(soundId, pan, volume * masterVolume * soundVolume);
            }
        }
    }

    public class MusicWrapper implements Music{
        private Music music;
        private float desiredVolume = 1f; // issaugot norima volume. Si volume bus paveikta master volume.

        private boolean paused = false;

        // fade effect.
        private MusicFadeEffect musicFadeEffect;

        MusicWrapper(Music music){
            this.music = music;
        }

//        void forgetFadeEffect(){
//            musicFadeEffect = null; // pametam.
//        }

        /** is music currently fading out. */
        public boolean isFadingOut(){
            if (musicFadeEffect != null){
                return musicFadeEffect.fadeDown;
            }
            return false;
        }

        /** is music currently fading in. */
        public boolean isFadingIn(){
            if (musicFadeEffect != null){
                return !musicFadeEffect.fadeDown;
            }
            return false;
        }

        /** Music will go more and more quieter till it stops. Does nothing if music is not playing.
         * @param time time in which music will stop. Time in seconds. */
        public void fadeOut(float time){
            if (isPlaying()){
                if (musicFadeEffect != null){
                    musicFadeEffect.interrupt();
                }
                if (time <= 0){
                    stop();
                    return;
                }
                musicFadeEffect = musicFadeEffectPool.obtain();
                musicFadeEffect.startFade(this, true, time);
            }
        }

        /** Music will go more and more louder till it reaches it's normal volume. Music must not be playing.
         * @param time time of how fast should music go louder. Time in seconds. */
        public void fadeIn(float time){
            if (!mute && !muteMusic && !isPlaying()){ // nemutinta ir negroja.
                if (musicFadeEffect != null){
                    musicFadeEffect.interrupt();
                }
                if (time <= 0){
                    play();
                    return;
                }
                MusicFadeEffect musicFadeEffect = musicFadeEffectPool.obtain();
                musicFadeEffect.startFade(this, false, time);
                this.musicFadeEffect = musicFadeEffect; // kitu atveja pats save interruptins.
            }
        }

        /** Resumes music if it was paused. If it was not paused then does nothing. */
        public void resume(){
            if (paused){
                play();
            }
        }

        @Override
        public void play() {
            if (!mute && !muteMusic){
                // interrupted fade effect.
                if (musicFadeEffect != null){
                    musicFadeEffect.interrupt();
                }
                music.setVolume(masterVolume * musicVolume * desiredVolume);
                try {
                    music.play();
                }catch (GdxRuntimeException ex){ // cia tas durns desktop bugas. Ka nelauzt programos, tiesiog pranesam, kas negerai, bet nestabdom.
                    ex.printStackTrace();

                    Gdx.app.log("MusicWrapper", "Music was not played.");
                }
                paused = false;
            }
        }

        @Override
        public void pause() {
            if (!mute && !muteMusic && isPlaying()){
                // interrupted fade effect.
                if (musicFadeEffect != null){
                    musicFadeEffect.interrupt();
                }
                music.pause();
                paused = true;
            }
        }

        @Override
        public void stop() {
            if (!mute && !muteMusic){
                // interrupted fade effect.
                if (musicFadeEffect != null){
                    musicFadeEffect.interrupt();
                }
                music.stop();
                paused = false;
            }
        }

        @Override
        public boolean isPlaying() {
//            return mute && music.isPlaying(); // blogai sitas.
            if (mute || muteMusic){
                return false;
            }
            return music.isPlaying();
        }

        @Override
        public void setLooping(boolean isLooping) {
            if (!mute && !muteMusic){
                music.setLooping(isLooping);
            }
        }

        @Override
        public boolean isLooping() {
            return music.isLooping();
        }

        @Override
        public void setVolume(float volume) {
//            if (musicFadeEffect != null){ // gi sitas pats volume keic??
//                musicFadeEffect.interrupt();
//            }
            desiredVolume = volume;
            music.setVolume(volume * masterVolume * musicVolume);
        }

        @Override
        public float getVolume() {
            return desiredVolume;
        }

        /** Real volume which music is playing. Volume is affected by {@link SoundController} volume parameters and may be different than
         * returned volume in method {@link #getVolume()} - this method returns desired volume for this music not actual one.
         * @return real volume at which music is playing.*/
        public float getRealVolume(){
            return music.getVolume();
        }

        @Override
        public void setPan(float pan, float volume) {
            if (!mute && !muteMusic){
                desiredVolume = volume;
                music.setPan(pan, volume * masterVolume * musicVolume);
            }
        }

        @Override
        public void setPosition(float position) {
            music.setPosition(position);
        }

        @Override
        public float getPosition() {
            return music.getPosition();
        }

        @Override
        public void dispose() {
            if (musicFadeEffect != null){
                musicFadeEffect.interrupt();
            }
            music.dispose();
        }

        @Override
        public void setOnCompletionListener(OnCompletionListener listener) {
            music.setOnCompletionListener(listener);
        }
    }

    /* garso fade effects. */

    private class SoundFadeEffect implements Counter.CounterInformer, Counter.CounterListener, Pool.Poolable{
        private Counter owner;
        private SoundWrapper soundWrapper;
        private float desiredVolume;
        private long soundId;

        private boolean fadeDown;

        void startFade(SoundWrapper soundWrapper, long id, boolean fadeDown, float desiredVolume, float time){
            if (owner != null){
                Engine.getInstance().setError("SoundController", "Cannot start sound fade effect twice!");
                return;
            }
            if (time <= 0){
                Engine.getInstance().setError("SoundController", "Fade time cannot be 0 or less");
                return;
            }

            owner = MoreUtils.getCounter();
            this.soundWrapper = soundWrapper;
            this.desiredVolume = MoreUtils.inBounds(desiredVolume, 0, 1f);
            soundId = id;
            this.fadeDown = fadeDown;

            // butinai listener sudedam/
            owner.setCounterListiner(this);
            owner.setCounterInformer(this);

            if (fadeDown){
                // mazejam.
                owner.startCount(this.desiredVolume, 0, time);
                // tiesiogiai kriepiames.
                soundWrapper.sound.setLooping(id, false); // isjungiam loop.
            }else {
                owner.startCount(0, this.desiredVolume, time);
            }
        }

        void interrupt(){
            owner.cancel();

//            MoreUtils.freeCounter(owner);
//            owner = null;

            soundWrapper.setVolume(soundId, desiredVolume);
            if (fadeDown){
                soundWrapper.sound.stop(soundId);
            }

            soundFadeEffectPool.free(this);
        }

        @Override
        public void reset() {
            if (soundWrapper != null){
                soundWrapper.soundFadeEffects.removeValue(this, true);
            }

            soundWrapper = null;
            MoreUtils.freeCounter(owner);
            owner = null;
        }

        @Override
        public void finished(float currentValue) {
            soundWrapper.setVolume(soundId, desiredVolume);

            if (fadeDown){
                soundWrapper.sound.stop(soundId); // kad save neinteruptintu kvieciam tiesiogiai i sound.
            }

            soundFadeEffectPool.free(this);
        }

        @Override
        public boolean cancel(int reason) {
            return true; // visada turi test darba.
        }

        @Override
        public void update(float oldValue, float currentValue) {
            soundWrapper.setVolume(soundId, currentValue);
        }
    }

    // this class is responsible for music fade effect.
    private class MusicFadeEffect implements Counter.CounterInformer, Counter.CounterListener, Pool.Poolable {
        private Counter owner;
        private MusicWrapper musicWrapper;
        private float desiredVolume;

        private boolean fadeDown; // is it fading down or fading up..

        void startFade(MusicWrapper musicWrapper, boolean fadeDown, float time){
            // dar paziurim, kad klaidu nebut.
            if (owner != null && owner.isCounting()){
                Engine.getInstance().setError("SoundController", "Cannot run music fade effect twice!");
                return;
            }
            if (time <= 0){
                Engine.getInstance().setError("SoundController", "Time cannot be 0 or less. Failed starting fade effect.");
                return;
            }
            // paruosiam viska.
            owner = MoreUtils.getCounter();
            this.musicWrapper = musicWrapper;
            desiredVolume = musicWrapper.desiredVolume;
            this.fadeDown = fadeDown;

            // sudedam listenerius.
            owner.setCounterInformer(this);
            owner.setCounterListiner(this);

            // galim praded veiksma.
            if (fadeDown){
//                musicWrapper.setVolume(0);
                owner.startCount(desiredVolume, 0, time);
//                musicWrapper.music.setLooping(false); // isjungiam.
            }else {
                musicWrapper.setVolume(0f);
                owner.startCount(0, desiredVolume, time);
                musicWrapper.play();
            }
        }

        /** interrupt fade effect and stops it. Changes volume as it was before effect. */
        void interrupt(){
            owner.cancel();
            if (fadeDown){
                musicWrapper.music.stop();
            }
            musicWrapper.setVolume(desiredVolume);

            musicFadeEffectPool.free(this);
        }

        @Override
        public void update(float oldValue, float currentValue) {
            musicWrapper.setVolume(currentValue);
//            System.out.println(currentValue);
        }

        @Override
        public void finished(float currentValue) {
            musicWrapper.setVolume(desiredVolume); // grazinam senaji garsa.
            if (fadeDown){ // jeigu mazejo, tai stabdom.
                musicWrapper.music.stop();
            }

            musicFadeEffectPool.free(this);
        }

        @Override
        public boolean cancel(int reason) {
            return true; // reik, kad visada uzbaigtu darba.
        }

        @Override
        public void reset() {
            if (musicWrapper != null){
                musicWrapper.musicFadeEffect = null;
            }
            musicWrapper = null; // pametam musu wraperi.
            MoreUtils.freeCounter(owner); // grazinam counter.
            owner = null; // ate counter.
        }
    }
}
