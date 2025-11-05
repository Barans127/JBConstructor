package com.engine.core;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.engine.interfaces.controls.widgets.ProgressBar;

public class SoundLoader {
    // lists.
    private Array<String> path, name;
    private Array<Boolean> type;

    // bendri.
    private boolean loading = false, asynchronousLoad;
    private int maxWaitTime = 5000; // def 5s.
//    private boolean addToResources = true; // nustatyt ar naudot resurce klase.

    // listeners.
    private SoundLoaderListener soundLoaderListener;
    private SoundLoaderTracker soundLoaderTracker;

    // progress bar pasidalinimas.
    private ImagesLoader imagesLoader; // su situo dalinsimes.
    private float ratio; // kiek vieno dalis.
    private ProgressBar progressBar;
    float percent;

    public SoundLoader(){
        path = new Array<>();
        name = new Array<>();
        type = new Array<>();
    }

    /* progress bar. */

    void progressBarWasShared(float ratio, ImagesLoader imagesLoader){
        this.imagesLoader = imagesLoader;
        this.ratio = ratio;
    }

    /** All progress of loading will be reflected on this progress bar.
     * progress will be divided by ratio to images loader and to sound loader.
     * Example: ratio: 0.7 - sound loader sets 70% progress bar while images loader 30%*/
    public void shareProgressBar(ProgressBar e, ImagesLoader imagesLoader, float ratio){
        if (imagesLoader == null || e == null){
            setProgressBar(e);
            return;
        }
        imagesLoader.shareProgressBar(e, this, 1f-ratio);
    }

    /** All progress of loading will be reflected on this progress bar.
     * This only works if asynchronous loading is started.
     * Set to null to disable reflection to progress bar.*/
    public void setProgressBar(ProgressBar progressBar){
        ratio = 1f;
        this.progressBar = progressBar;
        if (progressBar != null){
            progressBar.reset(0f);
        }
    }

    /* getters setters */

    /** Only for android. Sounds are checked if they are loaded. If sounds are not ready after this time, then loading will be finished anyway (big sounds may load for a long time).
     * Negative or zero will disable sound checking. Default 5s. */
    public void setMaxWaitTime(int time){
        maxWaitTime = time;
    }

    /** Only for android. Time which is given to check if sounds are loaded (ready to play). */
    public int getMaxWaitTime(){
        return maxWaitTime;
    }

    /** Set sound listener. This listener will be called when loading is completed. */
    public void setSoundLoaderListener(SoundLoaderListener loaderListener){
        soundLoaderListener = loaderListener;
    }

    /** Set sound tracker. This listener will be called every time sound is loaded. */
    public void setSoundLoaderTracker(SoundLoaderTracker soundLoaderTracker){
        this.soundLoaderTracker = soundLoaderTracker;
    }

//    /** Should loaded sounds be added to resources class or not. If you set this to false you must use {@link SoundLoaderTracker} listener!*/
//    public void setAddToResources(boolean addToResources){
//        if (loading){
//            Engine.getInstance().setError("SoundLoader", "Sounds are loading... This variable is locked!");
//            return;
//        }
//        this.addToResources = addToResources;
//    }

//    /** is loaded sounds added to resources. */
//    public boolean isAddToResources(){
//        return addToResources;
//    }

    /** is sounds loading. */
    public boolean isLoading(){
        return loading;
    }

    /* pridejimas i lista. */

    /** Adds sound info to list. Sound will be loaded later.
     * @param name - id name of sound
     *  @param path - sound file path.
     *  @param type - true if it is music instance, false otherwise.*/
    public void addSound(String name, String path, boolean type){
        if (loading){
            Engine.getInstance().setError("SoundLoader", "Sounds are loading... Lists are locked!");
            return;
        }
        if (name == null || path == null){
            Engine.getInstance().setError("SoundLoader", "Sound id name or path cannot be null.");
            return;
        }
        if (name.length() == 0 || path.length() == 0){
            Engine.getInstance().setError("SoundLoader", "Sound id name or path cannot be empty.");
            return;
        }

        this.name.add(name);
        this.path.add(MoreUtils.fixPath(path));
        this.type.add(type);
    }

    /* pridejimas i lista is failo (tokia pat sistema kaip ImagesLoader). */

    /** Reads a file and loads resources from it.
     * File content example: resources/sound/sound.ogg : type(optional) : idName(optional).
     * type - 0 - sound, 1 - music, if not defined then sound type will be chosen.
     * idName - idName to access from {@link Resources} class. If idName is not defined then file name is used as idName. Note: to define id name type also must be defined.*/
    public void readFileForResources(String path){
        if (path == null || path.length() == 0){
            Engine.getInstance().setError("SoundLoader", "Path cannot be empty.");
            return;
        }
        readFileForResources(Gdx.files.internal(path));
    }

    public void readFileForResources(FileHandle list){
        if (loading){
            Engine.getInstance().setError("SoundLoader", "Sounds are loading... Lists are locked!");
            return;
        }
        if (list.exists()){
            String content;
            try {
                content = list.readString();
            }catch (GdxRuntimeException ex){
                Engine.getInstance().setError("SoundLoader", "Failed to read from file: " + list.path());
                return;
            }

            // nustatom kur failo pradzia.
            String filePath = list.path();
            String root; // failo pradzia.
            if (filePath.contains("/")){
                root = filePath.substring(0, filePath.lastIndexOf("/")) + "/"; // aprasymo pradzia.
            }else {
                root = ""; // tiesiog nieko.
            }

            String[] lines = content.split("\\r?\\n");
            for (String line : lines){
                String inside;
                if (line.contains("//")){
                    inside = line.split("//", 2)[0];
                }else {
                    inside = line;
                }

                inside = inside.trim();
                // del kazkokiu encodinimo bullshit sitas nematomas simbolis atsirand tekste,
                //del ko visa image loader logika pabeg ir anas galvoj, kad eilute ne tuscia. Krc
                // sita nesamone, kuri net yra nematoma reik trint, kitaip nulauz loader ir kartu programa.
                if (inside.contains("\uFEFF")){ // kazkoks chujne, kuris cia viska gadin.
                    inside = inside.replaceAll("\uFEFF", "");
                }

                if (inside.length() == 0){
                    continue; // ignorijan tuscia eilute.
                }

                if (inside.contains(":")){
                    // turi parametrus.
                    String[] splits = inside.split(":");
                    String path = MoreUtils.fixPath(root + splits[0].trim()); // path tai lengva. Imam ir viskas.
                    int type;
                    String idName;
                    // toliau reik ziuret ar yra type.
                    if (splits.length > 1){
                        // bandom type perverst.
                        try {
                            type = Integer.parseInt(splits[1].trim());
                        }catch (NumberFormatException ex){
                            // jeigu klaida tai imam default 0
                            type = 0;
                        }
                        // dar paziurim ar id name yra.
                        if (splits.length > 2){
                            // kazkas yra.
                            idName = splits[2].trim();
                            if (idName.length() == 0){
                                // tuscias.
                                idName = MoreUtils.getFileName(path);
                            }
                        }else { // nera idname tago.
                            idName = MoreUtils.getFileName(path);
                        }
                    }else {
                        // ner kitu dvieju splitu???
                        type = 0;
                        idName = MoreUtils.getFileName(path);
                    }

                    // sugaudzius visa info metam i ten kur reik.
                    this.path.add(path);
                    this.name.add(idName);
                    this.type.add(type == 1);
                }else { // tiesiog idetas path nieko daugiau.
                    // neturi parametru. Jeigu neturi labai paprasta. Imam tiesiog path.
                    String path = MoreUtils.fixPath(root + inside);
                    this.path.add(path);
                    name.add(MoreUtils.getFileName(path)); // failo vardas tiesiog.
                    type.add(false); // defaultinis sound type.
                }
            }
        }else {
            Engine.getInstance().setError("SoundLoader", "Cannot locate file: " + list.path());
        }
    }

    /* loadinimo pradzia. */

    /** Loads all music and sound on this thread. On android may cause audio not load immediately (android loads sound on other threads). */
    public void loadAllNow(){
        // jeigu nedes i resources klase ir nera kas trakina tai kur sound eis? memory leak.
        if (soundLoaderTracker == null){
//            Engine.getInstance().setError("SoundLoader", "Trying to load sounds without capturing it!? Before loading sounds add SoundLoaderTracker" +
//                    " listener to SoundLoader or load sounds using SoundController.");
//            return;
            throw new RuntimeException("Trying to load sounds without capturing it!? Before loading sounds add SoundLoaderTracker" +
                    " listener to SoundLoader or load sounds using SoundController.");
        }
        asynchronousLoad = false;
        run();
    }

    /** Loads all music and sounds on other thread. */
    public void startLoadAsynchronous(){
        // jeigu nedes i resources klase ir nera kas trakina tai kur sound eis? memory leak.
        if (soundLoaderTracker == null){
            Engine.getInstance().setError("SoundLoader", "Trying to load sounds without capturing it!? Before loading sounds add SoundLoaderTracker" +
                    " listener to SoundLoader or load sounds using SoundController.");
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SoundLoader.this.run();
            }
        }, "Sound loader");

        asynchronousLoad = true;
        thread.start();
    }

    /* uzkrovimas. */

    // visu sound uzkrovimas.
    private void run(){
        Array<Sound> loadedSound = new Array<>();
//        Array<Music> loadedMusic = new Array<>();

        boolean normalLoading = Resources.getPropertyInt("enableSounds", 1) == 1;

        percent = 0f;
        loading = true;
        Engine p = Engine.getInstance();
        for (int a = 0; a < path.size; a++){
            String path = this.path.get(a);
            String name = this.name.get(a);
            boolean type = this.type.get(a);

            if (name.isEmpty()){
//                p.setError("SoundLoader", "Zero length id name for file: " + path);
//                continue;
                throw new RuntimeException("Zero length id name for file: " + path);
            }

            FileHandle fileHandle = Gdx.files.internal(path);
            if (fileHandle.exists()){
                if (type) {
                    // music
                    Music music;
                    if (normalLoading) {
                        music = Gdx.audio.newMusic(fileHandle);
                    }else { // apejimas sound krovimo.
                        music = new NoAudio.NoMusic();
                    }
//                        if (addToResources)
//                            Resources.addMusic(name, music);
                    if (soundLoaderTracker != null){
                        soundLoaderTracker.musicLoaded(music, name);
                    }else {
                        // ner trackario, tai pakibs cia viskas
                        music.dispose();
                        throw new RuntimeException("No sound loader tracker found!");
                    }

                    // dedam i lista del checko.
//                        loadedMusic.add(music);
                } else {
                    // sound
                    Sound sound;
                    if (normalLoading) {
                        sound = Gdx.audio.newSound(fileHandle);
                    }else { // apejimas sound krovimo.
                        sound = new NoAudio.NoSound();
                    }
//                        if (addToResources)
//                            Resources.addSound(name, sound);
                    if (soundLoaderTracker != null){
                        soundLoaderTracker.soundLoaded(sound, name);
                    }else {
                        sound.dispose();
                        throw new RuntimeException("No sound loader tracker found!");
                    }

                    // dedam i lista del checko.
                    loadedSound.add(sound);
                }
            }else {
//                p.setError("SoundLoader", "Cannot locate file: " + path);
//                return; // tegul visus parodo.
                throw new RuntimeException("Cannot locate file: " + path);
            }

            // percent perskaiciavimas
            float end = this.path.size;
            percent = (float) a / end * ratio;

            if (imagesLoader == null && progressBar != null){
                progressBar.setPercentage(percent); // nustatom kiek yra.
            }else if (imagesLoader != null){
                if (!imagesLoader.isLoading()){ // nekeiciam jei images dar veik. tas ir pakeis.
                    // baige anas.
                    imagesLoader.setPercentage(); // ten viska sugaudys.
                }
            }
        }

        // dabar tyliai tikrinam ar viskas ok.
        if (asynchronousLoad){
            // tik kitame thread sita darom, kad neuzmigtu main thread...
            // sita beda yra tik naujesniam androide, todel darom taip, ka apeit ta buga.
            // tikrinam ant visu android.
            if (Gdx.app.getType() == Application.ApplicationType.Android){
                int start = p.millis();
                MAIN:
                while (true){ // eisim kol viskas uzsikraus.
                    for (Sound sound : loadedSound){
                        long id = sound.play(0); // tyliai.
                        if (id > -1){
                            sound.stop(); // garsas geras. Stabdom.
                        }else {
                            // blogas garsas. Dar tikriausiai neuzsikroves.
                            // pamiegam, leidziam garsui uzsikraut.
                            synchronized (this) {
                                try {
                                    wait(500); // apie 100 millisekundziu.
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            // po miego tikrinsim is naujo.
                            // dedam timeri. Duodam 15s max. Pasiekus riba stabdom, kad neatrodytu pakibes.
                            if (start + maxWaitTime < p.millis()){
                                break MAIN;
                            }
                            continue MAIN;
                        }
                    }

                    // jei cia atejo, vadinas visi garsai geri.
                    break;
                }
            }
        }

        percent = ratio;
        if (imagesLoader == null && progressBar != null){
            progressBar.setPercentage(1f); // tiesiog taip.
            progressBar = null;
        }else if (imagesLoader != null){
            if (!imagesLoader.isLoading()){ // nekeiciam jei images dar veik. tas ir pakeis.
                // baige anas. baige ir sitas.
                imagesLoader.finishPercentage(); // pranesam, kad vsio.
            }
            imagesLoader = null; // nebereik.
        }

        path.clear();
        name.clear();
        type.clear();

//        loadedMusic.clear();
        loadedSound.clear();

        loading = false;

        if (soundLoaderListener != null){
            soundLoaderListener.onFinishLoad();
        }
    }

    /* listeners */

    public interface SoundLoaderListener{
        /** Called when sounds load completed. */
        void onFinishLoad();
    }

    public interface SoundLoaderTracker{
        /** Called when sound is loaded. */
        void soundLoaded(Sound sound, String id);
        /** Called when music is loaded. */
        void musicLoaded(Music music, String id);
    }
}
