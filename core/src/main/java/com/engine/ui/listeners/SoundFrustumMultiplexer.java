package com.engine.ui.listeners;

import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;

/** Class which allows to add more than one {@link SoundFrustumManager} instances to one entity.
 * NOTE: biggest volumes width will be used to calc object distance. Frustums should have same distances. */
public class SoundFrustumMultiplexer implements SoundFrustum {
    private Array<SoundFrustumManager> soundFrustumManagers; // musu frustumai.

    // max kintamieji, kad nereiktu ieskot kas kart kur daugiau.
    private float minPanning, maxPanning;
    private float minVolume, maxVolume;

    public SoundFrustumMultiplexer(SoundFrustumManager... e){
        soundFrustumManagers = new Array<>();
        addFrustums(e);
    }

    /** Adds all frustum to list. */
    public void addFrustums(SoundFrustumManager... e){
        // nedarom update. ant gali padarysim.
        for (SoundFrustumManager frustumManager : e){
            addFrustum(-1, frustumManager, false);
        }
        updateBounds();
    }

    /** add frustum. */
    public void addFrustum(SoundFrustumManager e){
        addFrustum(-1, e);
    }

    /** Adds frustum to specified index. */
    public void addFrustum(int index, SoundFrustumManager e){
        addFrustum(index, e, true);
    }

    /** Adds frustum to specified index. */
    private void addFrustum(int index, SoundFrustumManager e, boolean update){
        if (e == null){
            Engine.getInstance().setError("SoundFrustumMultiplexer", "Frustum cannot be null");
            return;
        }
        if (index >=0 && index < soundFrustumManagers.size){
            soundFrustumManagers.insert(index, e);
        }else {
            soundFrustumManagers.add(e);
        }
        if (update)
            updateBounds();
    }

    /** clear all frustums */
    public void clear(){
        soundFrustumManagers.clear();
    }

    /** Removes sound frustum at specified index.
     * @return removed value.*/
    public SoundFrustumManager remove(int index){
        SoundFrustumManager e = soundFrustumManagers.removeIndex(index);
        updateBounds();
        return e;
    }

    /** Removes sound frustum */
    public void remove(SoundFrustumManager e){
        soundFrustumManagers.removeValue(e, true);
        updateBounds();
    }

    /** all managers. */
    public Array<SoundFrustumManager> getSoundFrustumManagers(){
        return soundFrustumManagers;
    }

    /** Updates bounds. Frustums have different bounds. Biggest bounds will be chosen. If you changed frustum bounds then you should call this method. */
    public void updateBounds(){
        float minP = 0, maxP = 0;
        float minV = 0, maxV = 0;

        // sugaudom max reiksmes.
        for (SoundFrustumManager e : soundFrustumManagers){
            float mp = e.getMinPanningWidth();
            float xp = e.getPanningWidth();
            float mv = e.getMinVolumeWidth();
            float xv = e.getVolumeWidth();
            if (mp > minP){
                minP = mp;
            }
            if (xp > maxP){
                maxP = xp;
            }
            if (mv > minV){
                minV = mv;
            }
            if (xv > maxV){
                maxV = xv;
            }
        }

        minPanning = minP;
        maxPanning = maxP;
        minVolume = minV;
        maxVolume = maxV;
    }

    @Override
    public void update(float pan, float volume) {
        for (SoundFrustumManager e : soundFrustumManagers){
            e.update(pan, volume);
        }
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
    public int getDelayTime() {
        int time = 0;
        for (SoundFrustumManager e : soundFrustumManagers){
            time = Math.max(time, e.getDelayTime());
        }
        return time;
    }

    @Override
    public boolean isPlaying() {
        for (SoundFrustumManager e : soundFrustumManagers){
            if (e.isPlaying()){
                return true;
            }
        }
        return false;
    }

    @Override
    public void stop() {
        for (SoundFrustumManager e : soundFrustumManagers){
            e.stop();
        }
    }

    @Override
    public void start() {
        for (SoundFrustumManager e : soundFrustumManagers){
            e.start();
        }
    }
}
